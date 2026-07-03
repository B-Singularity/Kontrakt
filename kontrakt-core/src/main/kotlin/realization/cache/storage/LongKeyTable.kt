package realization.cache.storage

import realization.cache.diagnostics.L2TableSegmentSaturatedException
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.atomic.AtomicLongArray
import java.util.concurrent.atomic.AtomicReferenceArray
import java.util.concurrent.atomic.LongAdder
import java.util.concurrent.locks.ReentrantLock

/**
 * Primitive routing table for hot-path 64-bit keys.
 *
 * Design intent:
 * - boxed Long/ULong generic keys are forbidden on hot routing paths
 * - readers are bounded and lock-free
 * - writers are segment-linearizable
 * - correctness does not depend on read linearizability
 *
 * This is a fixed-capacity variant.
 * Capacity governance belongs to the owning shard/region adapter, not to this primitive itself.
 *
 * Normative invariants:
 * - keyBits == 0L is forbidden (EMPTY sentinel is reserved)
 * - publication is State-Last:
 *   values.set(i, v) -> keysBits.set(i, k) -> states.set(i, OCCUPIED)
 * - readers MUST consult state first
 * - writers are serialized per segment by stripe lock
 *
 * This primitive is NOT a general-purpose utility.
 * It is a Planning-order-specific machine for Tier-2 hot-path routing.
 * Therefore, order-integrity failures are surfaced as
 * [PlanningProtocolIntegrityException].
 */
class LongKeyTable<V : Any> private constructor(
    capacity: Int,
    stripeCount: Int,
) {
    private val tableCapacity: Int = capacity
    private val tableMask: Int = capacity - 1

    private val stripesCount: Int = stripeCount
    private val segmentSize: Int = capacity / stripeCount
    private val segmentMask: Int = segmentSize - 1
    private val segmentShift: Int = Integer.numberOfTrailingZeros(segmentSize)

    private val keysBits = AtomicLongArray(capacity)
    private val states = AtomicIntegerArray(capacity)
    private val values = AtomicReferenceArray<Any?>(capacity)

    private val stripes = Array(stripeCount) { ReentrantLock() }

    /**
     * Telemetry / governance hint only.
     *
     * This is not a linearization oracle.
     * It is acceptable for this counter to lag or race slightly relative to exact table state.
     */
    private val occupiedCount = LongAdder()

    val approxSize: Long
        get() = occupiedCount.sum()

    /**
     * Lock-free bounded read.
     *
     * Reader rule:
     * - consult state first
     * - only if state == OCCUPIED may key/value be read
     *
     * A racy miss is acceptable by design.
     * Correctness does not depend on read linearizability.
     */
    fun get(keyBits: Long): V? {
        requireNonZeroKey(keyBits)

        val startIdx = startIndex(keyBits)
        val segmentIdx = segmentIndexOf(startIdx)
        val segmentBase = segmentBaseOf(segmentIdx)
        val offset = segmentOffsetOf(startIdx)

        for (i in 0 until segmentSize) {
            val idx = segmentBase + ((offset + i) and segmentMask)
            val state = states.get(idx)

            if (state == STATE_EMPTY) {
                return null
            }

            if (state == STATE_OCCUPIED && keysBits.get(idx) == keyBits) {
                @Suppress("UNCHECKED_CAST")
                return values.get(idx) as V?
            }

            if (state != STATE_EMPTY && state != STATE_OCCUPIED && state != STATE_TOMBSTONE) {
                throw PlanningProtocolIntegrityException(
                    "LongKeyTable state corruption: unknown state=$state at index=$idx.",
                )
            }
        }

        return null
    }

    /**
     * Returns:
     * - existing value if key already exists
     * - null if newly installed
     *
     * Writer rule:
     * - linearized per segment under stripe lock
     * - publication MUST be State-Last
     */
    fun putIfAbsent(
        keyBits: Long,
        value: V,
    ): V? {
        requireNonZeroKey(keyBits)

        val startIdx = startIndex(keyBits)
        val segmentIdx = segmentIndexOf(startIdx)
        val segmentBase = segmentBaseOf(segmentIdx)
        val offset = segmentOffsetOf(startIdx)

        val stripe = stripes[segmentIdx]
        stripe.lock()
        try {
            var firstTombstone = -1

            for (i in 0 until segmentSize) {
                val idx = segmentBase + ((offset + i) and segmentMask)

                when (states.get(idx)) {
                    STATE_OCCUPIED -> {
                        if (keysBits.get(idx) == keyBits) {
                            @Suppress("UNCHECKED_CAST")
                            return values.get(idx) as V?
                        }
                    }

                    STATE_EMPTY -> {
                        val target = if (firstTombstone >= 0) firstTombstone else idx
                        publishOccupied(target, keyBits, value)
                        occupiedCount.increment()
                        return null
                    }

                    STATE_TOMBSTONE -> {
                        if (firstTombstone < 0) {
                            firstTombstone = idx
                        }
                    }

                    else -> {
                        throw PlanningProtocolIntegrityException(
                            "LongKeyTable state corruption: unknown state=${states.get(idx)} at index=$idx.",
                        )
                    }
                }
            }

            if (firstTombstone >= 0) {
                publishOccupied(firstTombstone, keyBits, value)
                occupiedCount.increment()
                return null
            }

            throw L2TableSegmentSaturatedException(
                segmentIndex = segmentIdx,
                tableCapacity = tableCapacity,
                stripeCount = stripesCount,
                approxOccupiedCount = approxSize,
            )
        } finally {
            stripe.unlock()
        }
    }

    /**
     * Removes the entry only if the current stored reference is exactly [expectedValue].
     *
     * Removal is linearized under the segment stripe lock.
     * State transitions to TOMBSTONE; the key slot is left intact intentionally.
     */
    fun removeIfSame(
        keyBits: Long,
        expectedValue: V,
    ): Boolean {
        requireNonZeroKey(keyBits)

        val startIdx = startIndex(keyBits)
        val segmentIdx = segmentIndexOf(startIdx)
        val segmentBase = segmentBaseOf(segmentIdx)
        val offset = segmentOffsetOf(startIdx)

        val stripe = stripes[segmentIdx]
        stripe.lock()
        try {
            for (i in 0 until segmentSize) {
                val idx = segmentBase + ((offset + i) and segmentMask)
                val state = states.get(idx)

                if (state == STATE_EMPTY) {
                    return false
                }

                if (state == STATE_OCCUPIED && keysBits.get(idx) == keyBits) {
                    @Suppress("UNCHECKED_CAST")
                    val actual = values.get(idx) as V?

                    if (actual === expectedValue) {
                        /*
                         * Tombstone commit:
                         * - mark tombstone first so future readers no longer treat the slot as live
                         * - clear reference afterwards to release the payload strongly
                         */
                        states.set(idx, STATE_TOMBSTONE)
                        values.set(idx, null)
                        occupiedCount.decrement()
                        return true
                    }

                    return false
                }

                if (state != STATE_EMPTY && state != STATE_OCCUPIED && state != STATE_TOMBSTONE) {
                    throw PlanningProtocolIntegrityException(
                        "LongKeyTable state corruption: unknown state=$state at index=$idx.",
                    )
                }
            }

            return false
        } finally {
            stripe.unlock()
        }
    }

    /**
     * Restricted lifecycle hook for adapter-owned partition drop.
     *
     * Must be called only after the owning region is already closed.
     * This path is intentionally linear and not a hot-path operation.
     */
    internal fun forEachOccupiedValueForClosedPartitionDrop(action: (V) -> Unit) {
        for (i in 0 until tableCapacity) {
            val state = states.get(i)
            if (state == STATE_OCCUPIED) {
                @Suppress("UNCHECKED_CAST")
                val value = values.get(i) as V?
                if (value != null) {
                    action(value)
                }
            } else if (state != STATE_EMPTY && state != STATE_TOMBSTONE) {
                throw PlanningProtocolIntegrityException(
                    "LongKeyTable state corruption: unknown state=$state at index=$i.",
                )
            }
        }
    }

    /**
     * Internal mixer for table distribution only.
     *
     * This is NOT the order SSOT hash.
     * It exists solely to spread already-derived route keys across table indices.
     */
    private fun mixForIndex(keyBits: Long): Int {
        var h = keyBits
        h = h xor (h ushr 33)
        h *= -0xae502812aa7333L
        h = h xor (h ushr 33)
        h *= -0x3b3146010f6d7dL
        h = h xor (h ushr 33)
        return h.toInt() and Int.MAX_VALUE
    }

    /**
     * State-Last publication.
     *
     * Publication order is normative:
     * 1. values.set
     * 2. keysBits.set
     * 3. states.set(OCCUPIED)
     *
     * Readers MUST consult state first.
     */
    private fun publishOccupied(
        idx: Int,
        keyBits: Long,
        value: V,
    ) {
        values.set(idx, value)
        keysBits.set(idx, keyBits)
        states.set(idx, STATE_OCCUPIED)
    }

    private fun startIndex(keyBits: Long): Int = mixForIndex(keyBits) and tableMask

    private fun segmentIndexOf(startIdx: Int): Int = startIdx ushr segmentShift

    private fun segmentBaseOf(segmentIdx: Int): Int = segmentIdx shl segmentShift

    private fun segmentOffsetOf(startIdx: Int): Int = startIdx and segmentMask

    private fun requireNonZeroKey(keyBits: Long) {
        if (keyBits == 0L) {
            throw PlanningProtocolIntegrityException(
                "0L is reserved as EMPTY sentinel; upstream must remap deterministically.",
            )
        }
    }

    companion object {
        private const val STATE_EMPTY = 0
        private const val STATE_OCCUPIED = 1
        private const val STATE_TOMBSTONE = 2

        /**
         * Canonical factory for LongKeyTable issuance.
         *
         * We intentionally centralize layout validation here so that
         * "table existence eligibility" is decided before instance construction.
         */
        @JvmStatic
        fun <V : Any> issue(
            capacity: Int,
            stripeCount: Int = 64,
        ): LongKeyTable<V> {
            validateLayout(capacity, stripeCount)
            return LongKeyTable(capacity, stripeCount)
        }

        private fun validateLayout(
            capacity: Int,
            stripeCount: Int,
        ) {
            if (capacity <= 0 || (capacity and (capacity - 1)) != 0) {
                throw PlanningProtocolIntegrityException(
                    "LongKeyTable capacity must be a positive power-of-two: capacity=$capacity",
                )
            }

            if (stripeCount <= 0 || (stripeCount and (stripeCount - 1)) != 0) {
                throw PlanningProtocolIntegrityException(
                    "LongKeyTable stripeCount must be a positive power-of-two: stripeCount=$stripeCount",
                )
            }

            if (capacity % stripeCount != 0) {
                throw PlanningProtocolIntegrityException(
                    "LongKeyTable capacity must be divisible by stripeCount: capacity=$capacity, stripeCount=$stripeCount",
                )
            }

            val segmentSize = capacity / stripeCount
            if (segmentSize < 8) {
                throw PlanningProtocolIntegrityException(
                    "LongKeyTable segmentSize must be >= 8: segmentSize=$segmentSize",
                )
            }
        }
    }
}