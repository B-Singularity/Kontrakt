package planning.infrastructure.cache

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
 * This is a fixed-capacity variant. Capacity governance belongs to the owning
 * shard/region adapter, not to this primitive itself.
 */
class LongKeyTable<V : Any>(
    capacity: Int,
    stripeCount: Int = 64,
) {
    companion object {
        private const val STATE_EMPTY = 0
        private const val STATE_OCCUPIED = 1
        private const val STATE_TOMBSTONE = 2
    }

    private val tableCapacity = capacity
    private val tableMask = capacity - 1

    private val stripesCount = stripeCount
    private val segmentSize = capacity / stripeCount
    private val segmentMask = segmentSize - 1
    private val segmentShift = Integer.numberOfTrailingZeros(segmentSize)

    private val keysBits = AtomicLongArray(capacity)
    private val states = AtomicIntegerArray(capacity)
    private val values = AtomicReferenceArray<Any?>(capacity)

    private val stripes = Array(stripeCount) { ReentrantLock() }

    /**
     * Telemetry / governance hint only.
     * Do not treat this as a linearization oracle.
     */
    private val occupiedCount = LongAdder()

    val approxSize: Long
        get() = occupiedCount.sum()

    init {
        require(capacity > 0 && (capacity and tableMask) == 0) {
            "capacity must be power-of-two"
        }
        require(stripeCount > 0 && (stripeCount and (stripeCount - 1)) == 0) {
            "stripeCount must be power-of-two"
        }
        require(capacity % stripeCount == 0) {
            "capacity must be divisible by stripeCount"
        }
        require(segmentSize >= 8) {
            "segmentSize must be >= 8"
        }
    }

    fun get(keyBits: Long): V? {
        requireNonZeroKey(keyBits)

        val startIdx = startIndex(keyBits)
        val segmentIdx = segmentIndexOf(startIdx)
        val segmentBase = segmentBaseOf(segmentIdx)
        val offset = segmentOffsetOf(startIdx)

        for (i in 0 until segmentSize) {
            val idx = segmentBase + ((offset + i) and segmentMask)
            val state = states.get(idx)

            if (state == STATE_EMPTY) return null
            if (state == STATE_OCCUPIED && keysBits.get(idx) == keyBits) {
                @Suppress("UNCHECKED_CAST")
                return values.get(idx) as V?
            }
        }

        return null
    }

    /**
     * Returns:
     * - existing value if key already exists
     * - null if newly installed
     */
    fun putIfAbsent(keyBits: Long, value: V): V? {
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
                        if (firstTombstone < 0) firstTombstone = idx
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

    fun removeIfSame(keyBits: Long, expectedValue: V): Boolean {
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

                if (state == STATE_EMPTY) return false

                if (state == STATE_OCCUPIED && keysBits.get(idx) == keyBits) {
                    @Suppress("UNCHECKED_CAST")
                    val actual = values.get(idx) as V?

                    if (actual === expectedValue) {
                        states.set(idx, STATE_TOMBSTONE)
                        values.set(idx, null)
                        occupiedCount.decrement()
                        return true
                    }

                    return false
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
     */
    internal inline fun forEachOccupiedValueForClosedPartitionDrop(
        action: (V) -> Unit,
    ) {
        for (i in 0 until tableCapacity) {
            if (states.get(i) == STATE_OCCUPIED) {
                @Suppress("UNCHECKED_CAST")
                val value = values.get(i) as V?
                if (value != null) action(value)
            }
        }
    }

    /**
     * Internal mixer for table distribution only.
     *
     * NOT the protocol SSOT hash.
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

    private fun publishOccupied(idx: Int, keyBits: Long, value: V) {
        values.set(idx, value)
        keysBits.set(idx, keyBits)
        states.set(idx, STATE_OCCUPIED)
    }

    private fun startIndex(keyBits: Long): Int =
        mixForIndex(keyBits) and tableMask

    private fun segmentIndexOf(startIdx: Int): Int =
        startIdx ushr segmentShift

    private fun segmentBaseOf(segmentIdx: Int): Int =
        segmentIdx shl segmentShift

    private fun segmentOffsetOf(startIdx: Int): Int =
        startIdx and segmentMask

    private fun requireNonZeroKey(keyBits: Long) {
        require(keyBits != 0L) {
            "0L is reserved as EMPTY sentinel; upstream must remap deterministically."
        }
    }
}