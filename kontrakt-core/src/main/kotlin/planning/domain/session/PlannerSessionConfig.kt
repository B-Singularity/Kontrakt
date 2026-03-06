package planning.domain.session

import planning.domain.exception.PlanningProtocolException
import kotlin.math.ln

/**
 * [Value Object] Session configuration + capacity (memory) law.
 *
 * This is not a "tuning bag". It is a *protocol-enforced* capacity model.
 *
 * Key goals:
 * - OOM defense: derive dense capacities from a fixed memory budget.
 * - Determinism: keep the capacity model stable and explicit (no hidden allocations).
 * - Fail-closed: when resources exceed caps, the runtime must throw rather than degrade silently.
 *
 * Memory model overview (Conservative Worst-Case):
 * - Indexer table (sparse, power-of-two)
 * - Dense per-node arrays (nodeIdCap)
 * - RMQ tables (depth-based, O(depth * log depth))
 * - Undo log (depth-based upper bound)
 * - Traversal stack (depth)
 * - Signature slab (bounded by ratio)
 * - JVM slack (fixed margin)
 *
 * IMPORTANT:
 * - This model is intentionally conservative: it should err on the side of underestimating capacity.
 */
data class PlannerSessionConfig(
    val normalizationSpecVersion: Long,
    val entropySeed: Long = 0L,

    // Limits
    val maxPlannerBytes: Long = 10 * 1024 * 1024, // 10MB
    val maxPhysicalSteps: Int = 1_000_000,
    val maxSemanticWorkUnits: Int = 100_000, // Semantic budget (units)

    // DoS defense
    val maxSignatureLen: Int = 8192,

    // Signature slab sizing ratio
    val signatureMemoryRatio: Double = 0.2
) {
    val maxSignatureBytes: Int = (maxPlannerBytes * signatureMemoryRatio).toInt()
    private val structBytesAvailable: Long = maxPlannerBytes - maxSignatureBytes - 16384 // 16KB JVM slack

    val maxNodeIdCap: Int
    val indexerTableCap: Int
    val undoLogCap: Int
    val maxSemanticDepth: Int

    init {
        var low = 1
        var high = 2_000_000
        var bestNodeCap = 0
        var bestTableCap = 0
        var bestUndoCap = 0
        var bestDepth = 0

        while (low <= high) {
            val midNodeCap = (low + high) / 2

            // Depth policy: derived from node cap with explicit bounds (as provided).
            val targetDepth = (midNodeCap / 10).coerceIn(256, 4096)

            val tableCap = nextPowerOfTwo((midNodeCap * 2).coerceAtLeast(1024))
            val undoCap = targetDepth * 8

            // 1) Indexer table (keys 8 + heads 4 + stamps 4 = 16 bytes)
            val tableBytes = tableCap.toLong() * 16

            // 2) Dense node arrays (Indexer 12 + L1 topo/grey 12 = 24 bytes)
            val nodeBytes = midNodeCap.toLong() * 24

            // 3) RMQ depth-based structures
            val logDepth = (ln(targetDepth.toDouble()) / ln(2.0)).toInt() + 2
            val flatSize = targetDepth * logDepth
            val rmqBytes = (targetDepth * 12) + (flatSize * 12)

            // 4) Undo log (6 ints = 24 bytes per record)
            val undoBytes = undoCap.toLong() * 24

            // 5) Stack (IntArray depth = 4 bytes per slot)
            val stackBytes = targetDepth.toLong() * 4

            val total = tableBytes + nodeBytes + rmqBytes + undoBytes + stackBytes

            if (total <= structBytesAvailable) {
                bestNodeCap = midNodeCap
                bestTableCap = tableCap
                bestUndoCap = undoCap
                bestDepth = targetDepth
                low = midNodeCap + 1
            } else {
                high = midNodeCap - 1
            }
        }

        if (bestNodeCap == 0) {
            throw PlanningProtocolException("Insufficient memory for minimal planner session.")
        }

        maxNodeIdCap = bestNodeCap
        indexerTableCap = bestTableCap
        undoLogCap = bestUndoCap
        maxSemanticDepth = bestDepth
    }

    companion object {
        private fun nextPowerOfTwo(v: Int): Int {
            var n = v - 1
            n = n or (n ushr 1)
            n = n or (n ushr 2)
            n = n or (n ushr 4)
            n = n or (n ushr 8)
            n = n or (n ushr 16)
            return if (n < 0) 1 else n + 1
        }
    }
}