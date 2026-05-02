package planning.domain.session

import kontrakt.planning.domain.protocol.PrimitiveHash
import planning.domain.exception.PlanningProtocolException
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.protocol.CostCenter
import planning.domain.session.policy.ResolvedPlannerSessionCaps

/**
 * Worker-local primitive two-phase identity indexer.
 *
 * Architectural role:
 * - maps a 64-bit abstract node identity to a dense worker-local `nodeId: Int`
 * - performs routing by raw 64-bit identity bits
 * - performs exact verification by canonical-signature byte equality
 *
 * Why this exists:
 * - Kotlin/JVM boxes `Long`/`ULong` when used as generic map keys
 * - the planner hot path must avoid boxed-key churn and per-operation heap traffic
 * - worker-local primitive arrays give deterministic layout, predictable latency,
 *   and O(1) reset via epoch invalidation
 *
 * Boundary rules:
 * - this type consumes only already-resolved structural inputs
 * - it does NOT perform capacity solving
 * - it does NOT inspect runtime environment or live telemetry
 * - it does NOT own wall-clock policy or any L2 governance concern
 *
 * Snapshot discipline:
 * - `caps` and `maxSignatureLen` are session-fixed inputs
 * - the indexer must behave deterministically for the lifetime of one session
 *
 * Failure model:
 * - any impossible state, overflow, or cap breach fails closed via
 *   [PlanningProtocolException] or [PlanningProtocolIntegrityException]
 *
 * Rollback model:
 * - routing-table mutations are restored exactly through a primitive undo log
 * - dense counters (`_nextId`, `sigSlabPtr`) are restored separately by the caller
 * - rollback correctness is defined by semantic non-reachability, not by physical zeroing
 */
internal class NodeIdIndexer private constructor(
    private val caps: ResolvedPlannerSessionCaps,
    private val maxSignatureLen: Int,
) {
    /**
     * Sparse routing table:
     *
     * - `tableKeys[slot]`   : routed raw identity bits
     * - `tableHeads[slot]`  : head nodeId of the collision chain for that identity
     * - `tableStamps[slot]` : epoch ownership marker for O(1) reset
     */
    private val tableKeys = LongArray(caps.indexerTableCap)
    private val tableHeads = IntArray(caps.indexerTableCap) { NO_NODE_ID }
    private val tableStamps = IntArray(caps.indexerTableCap)

    /**
     * Dense node state indexed by worker-local nodeId.
     *
     * `nextByNodeId` stores the collision-chain link.
     * Signature bytes live in one reserved slab to avoid per-node byte-array allocation.
     */
    private val nextByNodeId = IntArray(caps.maxNodeIdCap) { NO_NODE_ID }
    private val sigSlab = ByteArray(caps.maxSignatureBytes)
    private val sigOffsets = IntArray(caps.maxNodeIdCap)
    private val sigLengths = IntArray(caps.maxNodeIdCap)

    /**
     * Dense allocation cursors.
     *
     * `_nextId` is the next unused dense nodeId.
     * `sigSlabPtr` is the next free byte in the signature slab.
     */
    private var _nextId = 0
    private var sigSlabPtr = 0

    /**
     * Epoch marker used for O(1) logical reset.
     *
     * When the epoch wraps, we fail closed into a full stamp clear and restart from epoch 1.
     * That full clear is intentionally rare and remains outside the ordinary hot path.
     */
    private var currentEpoch = 1

    /**
     * Open-addressing mask.
     *
     * `ResolvedPlannerSessionCaps` already guarantees `indexerTableCap` is a positive power-of-two.
     */
    private val mask = caps.indexerTableCap - 1

    /**
     * Primitive undo log.
     *
     * Each record occupies 6 ints (= 24 bytes), aligned with the L1 byte ledger:
     * - op
     * - slot
     * - oldHead
     * - oldStamp
     * - oldKeyLow32
     * - oldKeyHigh32
     */
    private val undoLog = IntArray(caps.undoLogCap * UNDO_RECORD_WIDTH)
    private var undoLogPtr = 0

    /**
     * Number of dense ids issued in the current logical session.
     */
    val size: Int
        get() = _nextId

    /**
     * Resets the indexer for a new logical session.
     *
     * This is intentionally O(1) in the common case:
     * we invalidate prior sparse-table entries by moving to a fresh epoch.
     *
     * Dense allocation cursors and undo state are also reset because they are
     * session-local worker state, not cross-session semantic state.
     */
    fun reset(kernel: SessionKernel) {
        currentEpoch++
        if (currentEpoch <= 0) {
            kernel.step(CostCenter.NODEID_RESET_EPOCH)
            tableStamps.fill(0)
            currentEpoch = 1
        }

        _nextId = 0
        sigSlabPtr = 0
        undoLogPtr = 0
    }

    /**
     * Returns the current undo-log cursor.
     *
     * The returned value is a structural rollback checkpoint and is valid only
     * for this indexer instance within the same session lifecycle.
     */
    fun snap(): Int = undoLogPtr

    /**
     * Restores sparse-table mutations back to `snapPtr`.
     *
     * This method restores only table-level mutations.
     * The caller must separately restore `_nextId` and `sigSlabPtr` through
     * [rollbackCount] so that dense allocation state matches the same checkpoint.
     */
    fun rollback(
        snapPtr: Int,
        kernel: SessionKernel,
    ) {
        requireValidUndoSnapshot(snapPtr)

        while (undoLogPtr > snapPtr) {
            kernel.step(CostCenter.NODEID_ROLLBACK_STEP)

            undoLogPtr -= UNDO_RECORD_WIDTH

            val op = undoLog[undoLogPtr + OFFSET_OP]
            val slot = undoLog[undoLogPtr + OFFSET_SLOT]
            val oldHead = undoLog[undoLogPtr + OFFSET_OLD_HEAD]

            when (op) {
                UNDO_OP_INSERT -> {
                    val oldStamp = undoLog[undoLogPtr + OFFSET_OLD_STAMP]
                    val oldKeyLow = undoLog[undoLogPtr + OFFSET_OLD_KEY_LOW]
                    val oldKeyHigh = undoLog[undoLogPtr + OFFSET_OLD_KEY_HIGH]

                    val oldKey =
                        ((oldKeyHigh.toLong() and UINT32_MASK) shl 32) or
                            (oldKeyLow.toLong() and UINT32_MASK)

                    tableHeads[slot] = oldHead
                    tableStamps[slot] = oldStamp
                    tableKeys[slot] = oldKey
                }

                UNDO_OP_APPEND -> {
                    tableHeads[slot] = oldHead
                }

                else -> {
                    throw PlanningProtocolIntegrityException(
                        "Unknown NodeIdIndexer undo op: $op",
                    )
                }
            }
        }
    }

    /**
     * Restores dense allocation cursors to a previously captured checkpoint.
     *
     * This method does not physically wipe arrays.
     * Correctness relies on semantic non-reachability:
     * ids `>= targetCount` and bytes `>= targetSigPtr` must become unreachable
     * after rollback.
     */
    fun rollbackCount(
        targetCount: Int,
        targetSigPtr: Int,
    ) {
        if (targetCount < 0 || targetCount > _nextId) {
            throw PlanningProtocolIntegrityException(
                "NodeIdIndexer.rollbackCount targetCount out of range: target=$targetCount, nextId=$_nextId",
            )
        }
        if (targetSigPtr < 0 || targetSigPtr > sigSlabPtr) {
            throw PlanningProtocolIntegrityException(
                "NodeIdIndexer.rollbackCount targetSigPtr out of range: target=$targetSigPtr, sigSlabPtr=$sigSlabPtr",
            )
        }

        _nextId = targetCount
        sigSlabPtr = targetSigPtr
    }

    /**
     * Returns the current signature-slab write cursor.
     */
    fun currentSigPtr(): Int = sigSlabPtr

    /**
     * Finds an existing dense nodeId for the `(identityBits, signatureBytes)` pair,
     * or allocates a new dense nodeId if no exact match exists.
     *
     * Two-phase lookup:
     * 1. Route by `identityBits` using primitive open addressing.
     * 2. Verify by canonical-signature byte equality on the collision chain.
     *
     * This eliminates false identity hits while keeping routing allocation-free.
     */
    fun findOrAssign(
        identityBits: Long,
        sigBytes: ByteArray,
        kernel: SessionKernel,
    ): Int {
        if (sigBytes.size > maxSignatureLen) {
            throw PlanningProtocolException(
                "Signature length ${sigBytes.size} exceeds limit $maxSignatureLen",
            )
        }

        kernel.step(CostCenter.NODEID_PHASE1_ROUTE)

        var slot = startSlot(identityBits)
        var probes = 0

        while (true) {
            kernel.step(CostCenter.NODEID_PROBE_STEP)

            val stamp = tableStamps[slot]
            if (stamp != currentEpoch) {
                return allocateNewInSlot(
                    slot = slot,
                    key = identityBits,
                    sigBytes = sigBytes,
                    kernel = kernel,
                )
            }

            if (tableKeys[slot] == identityBits) {
                var currId = tableHeads[slot]
                while (currId != NO_NODE_ID) {
                    kernel.step(CostCenter.NODEID_PHASE2_SCAN)
                    if (signatureEquals(currId, sigBytes)) {
                        return currId
                    }
                    currId = nextByNodeId[currId]
                }

                return appendToChain(
                    slot = slot,
                    sigBytes = sigBytes,
                    kernel = kernel,
                )
            }

            slot = (slot + 1) and mask
            probes++
            if (probes > caps.indexerTableCap) {
                throw PlanningProtocolException(
                    "NodeIdIndexer probe limit exceeded. tableCap=${caps.indexerTableCap}",
                )
            }
        }
    }

    /**
     * Allocates a brand-new sparse-table slot and the first dense id under that key.
     *
     * This path is taken when the routed slot is logically empty in the current epoch.
     */
    private fun allocateNewInSlot(
        slot: Int,
        key: Long,
        sigBytes: ByteArray,
        kernel: SessionKernel,
    ): Int {
        val newId = _nextId
        if (newId >= caps.maxNodeIdCap) {
            throw PlanningProtocolException(
                "L1 maxNodeIdCap exceeded: nextId=$newId, cap=${caps.maxNodeIdCap}",
            )
        }

        kernel.step(CostCenter.NODEID_ALLOCATE)
        kernel.onNodeAllocated(newId)

        val oldHead = tableHeads[slot]
        val oldStamp = tableStamps[slot]
        val oldKey = tableKeys[slot]

        logUndoInsert(
            slot = slot,
            oldHead = oldHead,
            oldStamp = oldStamp,
            oldKey = oldKey,
        )

        _nextId++

        tableKeys[slot] = key
        tableStamps[slot] = currentEpoch
        tableHeads[slot] = newId

        nextByNodeId[newId] = NO_NODE_ID
        copySignature(
            nodeId = newId,
            bytes = sigBytes,
        )

        return newId
    }

    /**
     * Appends a new dense id to an existing identity collision chain.
     *
     * The sparse slot already routes to the same `identityBits`, but the exact
     * signature was not found in the chain.
     */
    private fun appendToChain(
        slot: Int,
        sigBytes: ByteArray,
        kernel: SessionKernel,
    ): Int {
        val newId = _nextId
        if (newId >= caps.maxNodeIdCap) {
            throw PlanningProtocolException(
                "L1 maxNodeIdCap exceeded: nextId=$newId, cap=${caps.maxNodeIdCap}",
            )
        }

        kernel.step(CostCenter.NODEID_ALLOCATE)
        kernel.onNodeAllocated(newId)

        val oldHead = tableHeads[slot]
        logUndoAppend(
            slot = slot,
            oldHead = oldHead,
        )

        _nextId++
        tableHeads[slot] = newId
        nextByNodeId[newId] = oldHead

        copySignature(
            nodeId = newId,
            bytes = sigBytes,
        )

        return newId
    }

    /**
     * Records a full sparse-slot mutation in the undo log.
     */
    private fun logUndoInsert(
        slot: Int,
        oldHead: Int,
        oldStamp: Int,
        oldKey: Long,
    ) {
        ensureUndoCapacity()

        undoLog[undoLogPtr++] = UNDO_OP_INSERT
        undoLog[undoLogPtr++] = slot
        undoLog[undoLogPtr++] = oldHead
        undoLog[undoLogPtr++] = oldStamp
        undoLog[undoLogPtr++] = oldKey.toInt()
        undoLog[undoLogPtr++] = (oldKey ushr 32).toInt()
    }

    /**
     * Records a chain-head replacement in the undo log.
     */
    private fun logUndoAppend(
        slot: Int,
        oldHead: Int,
    ) {
        ensureUndoCapacity()

        undoLog[undoLogPtr++] = UNDO_OP_APPEND
        undoLog[undoLogPtr++] = slot
        undoLog[undoLogPtr++] = oldHead
        undoLog[undoLogPtr++] = 0
        undoLog[undoLogPtr++] = 0
        undoLog[undoLogPtr++] = 0
    }

    /**
     * Copies canonical signature bytes into the reserved slab.
     *
     * The slab is a session-local primitive reservoir sized from resolved caps.
     * Exceeding it is a structural-cap failure and therefore fail-closed.
     */
    private fun copySignature(
        nodeId: Int,
        bytes: ByteArray,
    ) {
        if (sigSlabPtr + bytes.size > sigSlab.size) {
            throw PlanningProtocolException(
                "NodeIdIndexer signature slab overflow: ptr=$sigSlabPtr, len=${bytes.size}, slab=${sigSlab.size}",
            )
        }

        System.arraycopy(bytes, 0, sigSlab, sigSlabPtr, bytes.size)
        sigOffsets[nodeId] = sigSlabPtr
        sigLengths[nodeId] = bytes.size
        sigSlabPtr += bytes.size
    }

    /**
     * Byte-equality verifier for phase-2 exact matching.
     */
    private fun signatureEquals(
        nodeId: Int,
        bytes: ByteArray,
    ): Boolean {
        val len = sigLengths[nodeId]
        if (len != bytes.size) {
            return false
        }

        val off = sigOffsets[nodeId]
        for (i in 0 until len) {
            if (sigSlab[off + i] != bytes[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Computes the initial open-addressing slot.
     *
     * Narrowing to Int here is intentional and safe because the result is always
     * masked by a power-of-two table mask.
     */
    private fun startSlot(identityBits: Long): Int = PrimitiveHash.mix64(identityBits).toInt() and mask

    /**
     * Ensures that one additional undo record fits.
     */
    private fun ensureUndoCapacity() {
        if (undoLogPtr + UNDO_RECORD_WIDTH > undoLog.size) {
            throw PlanningProtocolException(
                "NodeIdIndexer undo log overflow: ptr=$undoLogPtr, capacity=${undoLog.size}",
            )
        }
    }

    /**
     * Validates that `snapPtr` is a valid undo-log checkpoint.
     */
    private fun requireValidUndoSnapshot(snapPtr: Int) {
        if (snapPtr < 0 || snapPtr > undoLogPtr || snapPtr % UNDO_RECORD_WIDTH != 0) {
            throw PlanningProtocolIntegrityException(
                "Invalid NodeIdIndexer undo snapshot: snapPtr=$snapPtr, undoLogPtr=$undoLogPtr",
            )
        }
    }

    companion object {
        private const val NO_NODE_ID = -1
        private const val UNDO_RECORD_WIDTH = 6

        private const val OFFSET_OP = 0
        private const val OFFSET_SLOT = 1
        private const val OFFSET_OLD_HEAD = 2
        private const val OFFSET_OLD_STAMP = 3
        private const val OFFSET_OLD_KEY_LOW = 4
        private const val OFFSET_OLD_KEY_HIGH = 5

        private const val UNDO_OP_INSERT = 1
        private const val UNDO_OP_APPEND = 2

        private const val UINT32_MASK = 0xFFFF_FFFFL

        /**
         * Issues a session-fixed NodeIdIndexer from already-resolved structural inputs.
         *
         * This is the preferred boundary:
         * - `caps` drives primitive layout sizing
         * - `maxSignatureLen` enforces the per-signature contract
         *
         * We intentionally do not accept the full `PlannerSessionConfig` here,
         * because the indexer should not depend on unrelated policy subtrees.
         */
        @JvmStatic
        fun issue(
            caps: ResolvedPlannerSessionCaps,
            maxSignatureLen: Int,
        ): NodeIdIndexer {
            if (maxSignatureLen <= 0) {
                throw PlanningProtocolIntegrityException(
                    "NodeIdIndexer.maxSignatureLen must be > 0: $maxSignatureLen",
                )
            }
            if (maxSignatureLen > caps.maxSignatureBytes) {
                throw PlanningProtocolIntegrityException(
                    "NodeIdIndexer.maxSignatureLen must be <= caps.maxSignatureBytes: " +
                        "maxSignatureLen=$maxSignatureLen, maxSignatureBytes=${caps.maxSignatureBytes}",
                )
            }

            return NodeIdIndexer(
                caps = caps,
                maxSignatureLen = maxSignatureLen,
            )
        }
    }
}
