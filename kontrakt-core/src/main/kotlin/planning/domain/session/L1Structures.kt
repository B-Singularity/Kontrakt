package planning.domain.session

import kotlin.math.ln

/**
 * [Internal Primitive] Pooled L1 data structures (worker-local).
 *
 * This class owns dense arrays for:
 * - Topology (per nodeId)
 * - GreyMap (depthOfNodeId)
 * - Traversal stack (activeStack)
 * - RMQ buffers and flat doubling tables (depth-based)
 *
 * GreyMap contract:
 * - depthOfNodeId[nodeId] == 0  => White (not on the current active path)
 * - depthOfNodeId[nodeId]  > 0  => Grey, stores 1-based depth (position in active stack)
 *
 * Zero-residue model:
 * - reset() clears GreyMap via the stack trace (O(delta)).
 * - per-node arrays are overwritten at allocation via initNode(nodeId).
 */
class L1Structures(config: PlannerSessionConfig) {

    // --- Topology (dense per nodeId) ---
    val parentPointers = IntArray(config.maxNodeIdCap) { -1 }
    val depthOfNodeId = IntArray(config.maxNodeIdCap)

    // --- RMQ (depth-based) ---
    val incomingEdgeRankAtDepth = LongArray(config.maxSemanticDepth + 2)
    val floorLog2 = IntArray(config.maxSemanticDepth + 2)

    private val logDepth = (ln(config.maxSemanticDepth.toDouble()) / ln(2.0)).toInt() + 2
    private val flatSize = config.maxSemanticDepth * logDepth

    val flatMinEdgeRankUp = LongArray(flatSize)
    val flatArgminUp = IntArray(flatSize)

    // --- Traversal stack (depth) ---
    val activeStack = IntArray(config.maxSemanticDepth)
    var stackPointer: Int = 0

    /**
     * Mandatory per-node initialization on allocation (0-based reuse safety).
     */
    fun initNode(nodeId: Int) {
        parentPointers[nodeId] = -1
        depthOfNodeId[nodeId] = 0
        // RMQ is depth-based; not cleared here.
    }

    /**
     * Initialize RMQ slot when a depth is pushed.
     *
     * This prevents accidental use of stale per-depth values.
     */
    fun initRMQSlot(depth: Int) {
        incomingEdgeRankAtDepth[depth] = -1L
    }

    /**
     * [Protocol #8] O(delta) zero-residue reset.
     *
     * Clears only GreyMap entries for nodes that were actually pushed to the active stack.
     */
    fun reset() {
        while (stackPointer > 0) {
            val nodeId = activeStack[--stackPointer]
            depthOfNodeId[nodeId] = 0
        }
    }
}