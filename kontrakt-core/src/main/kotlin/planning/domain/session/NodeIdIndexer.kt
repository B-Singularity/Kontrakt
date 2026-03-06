package planning.domain.session

import kontrakt.planning.domain.protocol.PrimitiveHash
import planning.domain.exception.PlanningProtocolException
import planning.domain.protocol.CostCenter

/**
 * [L1 Primitive] Node identity -> dense nodeId indexer.
 *
 * Requirements:
 * - No boxing / no Map<Long, *> on hot paths.
 * - Open addressing for routing, chained nodeId list for collision groups.
 * - Epoch-stamp clearing with fail-closed overflow handling (metered).
 * - Transactional undo log for exact rollback of table mutations.
 *
 * Failure model:
 * - Any slab/undo overflow fails closed via [PlanningProtocolException].
 */
class NodeIdIndexer(
    private val config: PlannerSessionConfig
) {
    // --- Sparse table ---
    private val tableKeys = LongArray(config.indexerTableCap)
    private val tableHeads = IntArray(config.indexerTableCap) { -1 }
    private val tableStamps = IntArray(config.indexerTableCap)

    // --- Dense node state ---
    private val nextByNodeId = IntArray(config.maxNodeIdCap) { -1 }
    private val sigSlab = ByteArray(config.maxSignatureBytes)
    private var sigSlabPtr = 0
    private val sigOffsets = IntArray(config.maxNodeIdCap)
    private val sigLengths = IntArray(config.maxNodeIdCap)

    private var _nextId = 0
    val size: Int get() = _nextId

    private var currentEpoch = 1
    private val mask = config.indexerTableCap - 1

    // --- Undo log (6 ints/record) ---
    private val undoLog = IntArray(config.undoLogCap * 6)
    private var undoLogPtr = 0
    private val UNDO_OP_INSERT = 1
    private val UNDO_OP_APPEND = 2

    fun reset(kernel: SessionKernel) {
        currentEpoch++
        if (currentEpoch < 0) {
            kernel.step(CostCenter.NODEID_RESET_EPOCH)
            tableStamps.fill(0)
            currentEpoch = 1
        }
        _nextId = 0
        sigSlabPtr = 0
        undoLogPtr = 0
    }

    fun snap(): Int = undoLogPtr

    fun rollback(snapPtr: Int, kernel: SessionKernel) {
        while (undoLogPtr > snapPtr) {
            kernel.step(CostCenter.NODEID_ROLLBACK_STEP)

            undoLogPtr -= 6
            val type = undoLog[undoLogPtr]
            val slot = undoLog[undoLogPtr + 1]
            val oldHead = undoLog[undoLogPtr + 2]

            if (type == UNDO_OP_INSERT) {
                val oldStamp = undoLog[undoLogPtr + 3]
                val oldKeyLow = undoLog[undoLogPtr + 4]
                val oldKeyHigh = undoLog[undoLogPtr + 5]

                // Correct restoration (unsigned halves).
                val oldKey =
                    ((oldKeyHigh.toLong() and 0xFFFFFFFFL) shl 32) or
                            (oldKeyLow.toLong() and 0xFFFFFFFFL)

                tableHeads[slot] = oldHead
                tableStamps[slot] = oldStamp
                tableKeys[slot] = oldKey
            } else if (type == UNDO_OP_APPEND) {
                tableHeads[slot] = oldHead
            }
        }
    }

    fun rollbackCount(targetCount: Int, targetSigPtr: Int) {
        _nextId = targetCount
        sigSlabPtr = targetSigPtr
    }

    fun currentSigPtr(): Int = sigSlabPtr

    fun findOrAssign(identityBits: Long, sigBytes: ByteArray, kernel: SessionKernel): Int {
        if (sigBytes.size > config.maxSignatureLen) {
            throw PlanningProtocolException(
                "Signature length ${sigBytes.size} exceeds limit ${config.maxSignatureLen}"
            )
        }

        kernel.step(CostCenter.NODEID_PHASE1_ROUTE)

        var slot = (PrimitiveHash.mix64(identityBits).toInt()) and mask
        var probeDist = 0

        while (true) {
            kernel.step(CostCenter.NODEID_PROBE_STEP)
            val stamp = tableStamps[slot]

            if (stamp != currentEpoch) {
                return allocateNewInSlot(slot, identityBits, sigBytes, kernel)
            }

            if (tableKeys[slot] == identityBits) {
                var currId = tableHeads[slot]
                while (currId != -1) {
                    kernel.step(CostCenter.NODEID_PHASE2_SCAN)
                    if (signatureEquals(currId, sigBytes)) {
                        return currId
                    }
                    currId = nextByNodeId[currId]
                }
                return appendToChain(slot, sigBytes, kernel)
            }

            slot = (slot + 1) and mask
            probeDist++
            if (probeDist > config.indexerTableCap) {
                throw PlanningProtocolException("L1 Table Full (Probe limit)")
            }
        }
    }

    private fun allocateNewInSlot(slot: Int, key: Long, sigBytes: ByteArray, kernel: SessionKernel): Int {
        val newId = _nextId
        if (newId >= config.maxNodeIdCap) throw PlanningProtocolException("L1 Node Cap Exceeded")

        kernel.step(CostCenter.NODEID_ALLOCATE)
        kernel.onNodeAllocated(newId)

        val oldHead = tableHeads[slot]
        val oldStamp = tableStamps[slot]
        val oldKey = tableKeys[slot]

        logUndo(UNDO_OP_INSERT, slot, oldHead, oldStamp, oldKey)

        _nextId++

        tableKeys[slot] = key
        tableStamps[slot] = currentEpoch
        tableHeads[slot] = newId

        nextByNodeId[newId] = -1
        copySignature(newId, sigBytes)

        return newId
    }

    private fun appendToChain(slot: Int, sigBytes: ByteArray, kernel: SessionKernel): Int {
        val newId = _nextId
        if (newId >= config.maxNodeIdCap) throw PlanningProtocolException("L1 Node Cap Exceeded")

        kernel.step(CostCenter.NODEID_ALLOCATE)
        kernel.onNodeAllocated(newId)

        val oldHead = tableHeads[slot]

        logUndo(UNDO_OP_APPEND, slot, oldHead, 0, 0)

        _nextId++
        tableHeads[slot] = newId
        nextByNodeId[newId] = oldHead
        copySignature(newId, sigBytes)

        return newId
    }

    private fun logUndo(type: Int, slot: Int, oldHead: Int, oldStamp: Int, oldKey: Long) {
        if (undoLogPtr + 6 > undoLog.size) {
            throw PlanningProtocolException("L1 Undo Log Overflow")
        }
        undoLog[undoLogPtr++] = type
        undoLog[undoLogPtr++] = slot
        undoLog[undoLogPtr++] = oldHead
        undoLog[undoLogPtr++] = oldStamp
        undoLog[undoLogPtr++] = oldKey.toInt()
        undoLog[undoLogPtr++] = (oldKey ushr 32).toInt()
    }

    private fun copySignature(nodeId: Int, bytes: ByteArray) {
        if (sigSlabPtr + bytes.size > sigSlab.size) {
            throw PlanningProtocolException("Signature Slab Overflow")
        }
        System.arraycopy(bytes, 0, sigSlab, sigSlabPtr, bytes.size)
        sigOffsets[nodeId] = sigSlabPtr
        sigLengths[nodeId] = bytes.size
        sigSlabPtr += bytes.size
    }

    private fun signatureEquals(nodeId: Int, bytes: ByteArray): Boolean {
        val len = sigLengths[nodeId]
        if (len != bytes.size) return false
        val off = sigOffsets[nodeId]
        for (i in 0 until len) {
            if (sigSlab[off + i] != bytes[i]) return false
        }
        return true
    }
}