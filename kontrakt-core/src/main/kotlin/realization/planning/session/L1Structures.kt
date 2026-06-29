package realization.planning.session

import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import stage.lowering.contract.BreakpointStage
import java.util.Arrays

/**
 * Worker-local primitive planning substrate.
 *
 * Design goals:
 * - hot-path state lives in primitive arrays
 * - depth-based cycle metadata is explicit and deterministic
 * - reset is fail-closed and leaves no semantically reachable residue
 * - RMQ structures are reset-safe through epoch invalidation
 *
 * Depth / stack invariant:
 * - depth `0` is reserved as the "not active" sentinel
 * - active semantic depths are `1..stackPointer`
 * - `activeStack[depth - 1]` stores the nodeId currently active at that depth
 * - `depthOfNodeId[nodeId] == 0` means "not on the active path"
 *
 * Parent pointer contract:
 * - `parentPointers[nodeId]` is worker-local transient topology state
 * - active nodes are cleared during reset
 * - inactive dense ids are reinitialized through `initNode(nodeId)` before reuse
 * - therefore stale values must never become semantically reachable across sessions
 */
class L1Structures private constructor(
    maxNodeIdCap: Int,
    maxSemanticDepth: Int,
    private val rmqBase: Int,
) {
    companion object {
        /**
         * Sentinel values for hot-path arrays.
         *
         * `INF_RANK = -1L` is used as unsigned +INF.
         * This relies on the order rule that a valid canonical edge rank must never use
         * the all-ones bit pattern reserved for the sentinel.
         */
        private const val NO_STAGE_TAG: Byte = 0
        private const val INF_RANK: Long = -1L
        private const val NO_INDEX: Int = Int.MAX_VALUE
        private const val NO_EXECUTION_INDEX: Int = -1
        private const val NO_MEMBER_INDEX: Int = -1

        /**
         * Named hot-path stage tags.
         *
         * We intentionally keep primitive byte tags in the arrays for density and cache locality,
         * but the meaning is derived from the domain enum rather than hardcoded magic bytes.
         */
        private const val STAGE_NONE_TAG: Byte = 0
        private val STAGE_SUBSTITUTABLE_TAG: Byte = BreakpointStage.SUBSTITUTABLE.tag
        private val STAGE_DEFERRED_TAG: Byte = BreakpointStage.DEFERRED.tag

        @JvmStatic
        fun issue(
            maxNodeIdCap: Int,
            maxSemanticDepth: Int,
        ): L1Structures {
            if (maxNodeIdCap <= 0) {
                throw PlanningProtocolIntegrityException(
                    "maxNodeIdCap must be > 0: $maxNodeIdCap",
                )
            }
            if (maxSemanticDepth <= 0) {
                throw PlanningProtocolIntegrityException(
                    "maxSemanticDepth must be > 0: $maxSemanticDepth",
                )
            }

            val rmqBase = computeSafeRmqBase(maxSemanticDepth)

            return L1Structures(
                maxNodeIdCap = maxNodeIdCap,
                maxSemanticDepth = maxSemanticDepth,
                rmqBase = rmqBase,
            )
        }

        /**
         * Computes a power-of-two RMQ leaf base with explicit overflow protection.
         *
         * This method is deliberately fail-closed.
         * If the requested semantic depth would force an impossible or unsafe tree size,
         * we throw instead of silently overflowing Int arithmetic.
         *
         * Rationale:
         * - ADR-level capacity rules treat impossible `nextPowerOfTwo(...)` derivation as a
         *   order/sizing failure, not as best-effort behavior.
         * - RMQ backing arrays must stay in valid positive Int ranges.
         */
        @JvmStatic
        private fun computeSafeRmqBase(maxSemanticDepth: Int): Int {
            val requestedLeaves = maxSemanticDepth.toLong() + 2L
            if (requestedLeaves <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "RMQ leaf count overflow for maxSemanticDepth=$maxSemanticDepth",
                )
            }

            var candidate = 1L
            while (candidate < requestedLeaves) {
                candidate = candidate shl 1
                if (candidate <= 0L) {
                    throw PlanningProtocolIntegrityException(
                        "nextPowerOfTwo overflow while computing RMQ base for maxSemanticDepth=$maxSemanticDepth",
                    )
                }
            }

            val maxSafeBase = Int.MAX_VALUE.toLong() / 2L
            if (candidate > maxSafeBase) {
                throw PlanningProtocolIntegrityException(
                    "RMQ base would overflow backing tree size: base=$candidate, maxSafeBase=$maxSafeBase, maxSemanticDepth=$maxSemanticDepth",
                )
            }

            return candidate.toInt()
        }
    }

    /**
     * Dense topology arrays indexed by nodeId.
     */
    val parentPointers = IntArray(maxNodeIdCap) { -1 }
    val depthOfNodeId = IntArray(maxNodeIdCap)

    /**
     * Active path stack.
     *
     * Index mapping:
     * - depth 1  -> activeStack[0]
     * - depth 2  -> activeStack[1]
     * - depth d  -> activeStack[d - 1]
     */
    val activeStack = IntArray(maxSemanticDepth + 2)

    /**
     * Number of currently active depths.
     *
     * This is also the current top semantic depth because depth zero is reserved.
     */
    var stackPointer: Int = 0

    /**
     * Per-depth incoming edge metadata.
     *
     * These arrays are authoritative for active-cycle-segment breakpoint selection.
     * A depth either has:
     * - a valid incoming edge descriptor, or
     * - a cleared sentinel state
     */
    val incomingEdgeRankAtDepth = LongArray(maxSemanticDepth + 2) { INF_RANK }
    val incomingEdgeStageTagAtDepth = ByteArray(maxSemanticDepth + 2) { NO_STAGE_TAG }
    val incomingExpandExecutionIndexAtDepth = IntArray(maxSemanticDepth + 2) { NO_EXECUTION_INDEX }
    val incomingMemberIndexAtDepth = IntArray(maxSemanticDepth + 2) { NO_MEMBER_INDEX }

    /**
     * Session-owned traversal cursor stack.
     *
     * Important:
     * - the values live here, not inside execution frames
     * - rollback captures only the stack pointer, never the values themselves
     */
    val memberCursorStack = IntArray(maxSemanticDepth + 2)
    var memberCursorStackPointer: Int = 0

    /**
     * RMQ backing tree size.
     *
     * The tree is 1-indexed logically:
     * - leaves start at `rmqBase`
     * - total storage is `rmqBase * 2`
     */
    private val rmqSize: Int = rmqBase shl 1

    /**
     * Separate RMQ trees for each breakable stage.
     *
     * This keeps the stage priority external to the tree:
     * - query DEFERRED first
     * - then query SUBSTITUTABLE
     */
    private val deferredRankTree = LongArray(rmqSize)
    private val deferredArgTree = IntArray(rmqSize)
    private val deferredEpochTree = IntArray(rmqSize)

    private val substitutableRankTree = LongArray(rmqSize)
    private val substitutableArgTree = IntArray(rmqSize)
    private val substitutableEpochTree = IntArray(rmqSize)

    /**
     * Epoch-based invalidation for RMQ trees.
     *
     * Instead of eagerly clearing every tree slot on session reset, we advance the epoch.
     * Tree nodes written under older epochs become semantically invisible.
     */
    private var currentRmqEpoch: Int = 1

    /**
     * Initializes dense per-node transient state before semantic use.
     *
     * Contract:
     * - every newly reusable nodeId must pass through here
     * - this is the authoritative point where stale topology data becomes unreachable
     */
    fun initNode(nodeId: Int) {
        parentPointers[nodeId] = -1
        depthOfNodeId[nodeId] = 0
    }

    /**
     * Binds the incoming edge of a newly active depth.
     *
     * This method is fail-closed:
     * - valid stage tags are NONE / SUBSTITUTABLE / DEFERRED only
     * - unknown raw stage bytes are rejected immediately
     */
    fun bindIncomingEdge(
        depth: Int,
        edgeRank: Long,
        stageTag: Byte,
        ownerExecutionIndex: Int,
        memberIndex: Int,
    ) {
        validateDepth(depth)

        incomingEdgeRankAtDepth[depth] = edgeRank
        incomingEdgeStageTagAtDepth[depth] = stageTag
        incomingExpandExecutionIndexAtDepth[depth] = ownerExecutionIndex
        incomingMemberIndexAtDepth[depth] = memberIndex

        when (stageTag) {
            STAGE_DEFERRED_TAG -> {
                updateDeferred(depth, edgeRank)
                updateSubstitutable(depth, INF_RANK)
            }

            STAGE_SUBSTITUTABLE_TAG -> {
                updateDeferred(depth, INF_RANK)
                updateSubstitutable(depth, edgeRank)
            }

            STAGE_NONE_TAG -> {
                updateDeferred(depth, INF_RANK)
                updateSubstitutable(depth, INF_RANK)
            }

            else -> {
                throw PlanningProtocolIntegrityException(
                    "Unknown stageTag=$stageTag at depth=$depth",
                )
            }
        }
    }

    /**
     * Clears all active-depth metadata for a depth.
     *
     * This operation updates both:
     * - the explicit per-depth metadata arrays
     * - the stage-specific RMQ trees
     */
    fun clearDepthMetadata(depth: Int) {
        validateDepth(depth)

        incomingEdgeRankAtDepth[depth] = INF_RANK
        incomingEdgeStageTagAtDepth[depth] = NO_STAGE_TAG
        incomingExpandExecutionIndexAtDepth[depth] = NO_EXECUTION_INDEX
        incomingMemberIndexAtDepth[depth] = NO_MEMBER_INDEX

        updateDeferred(depth, INF_RANK)
        updateSubstitutable(depth, INF_RANK)
    }

    /**
     * Returns the best DEFERRED breakpoint candidate in the inclusive depth range.
     */
    fun queryBestDeferred(
        leftDepthInclusive: Int,
        rightDepthInclusive: Int,
    ): DepthWinner? =
        queryRange(
            leftDepthInclusive = leftDepthInclusive,
            rightDepthInclusive = rightDepthInclusive,
            rankTree = deferredRankTree,
            argTree = deferredArgTree,
            epochTree = deferredEpochTree,
        )

    /**
     * Returns the best SUBSTITUTABLE breakpoint candidate in the inclusive depth range.
     */
    fun queryBestSubstitutable(
        leftDepthInclusive: Int,
        rightDepthInclusive: Int,
    ): DepthWinner? =
        queryRange(
            leftDepthInclusive = leftDepthInclusive,
            rightDepthInclusive = rightDepthInclusive,
            rankTree = substitutableRankTree,
            argTree = substitutableArgTree,
            epochTree = substitutableEpochTree,
        )

    /**
     * Reset all session-local primitive state to a semantically clean baseline.
     *
     * Reset behavior:
     * - active nodes are popped depth-by-depth
     * - each popped depth clears both visible metadata and RMQ visibility
     * - parentPointers of active nodes are explicitly neutralized
     * - member cursor values are zeroed while rewinding the cursor stack
     * - RMQ trees are epoch-invalidated to guarantee reset safety even if future
     *   maintenance changes leave untouched internal nodes behind
     *
     * The `poppedDepth` variable is intentionally captured *before* decrementing
     * `stackPointer`. This makes the depth/index relationship explicit:
     *
     * - top active depth = current stackPointer
     * - backing array slot = stackPointer - 1
     */
    fun reset() {
        while (stackPointer > 0) {
            val poppedDepth = stackPointer
            stackPointer--

            val nodeId = activeStack[stackPointer]
            activeStack[stackPointer] = 0

            depthOfNodeId[nodeId] = 0
            parentPointers[nodeId] = -1

            clearDepthMetadata(poppedDepth)
        }

        while (memberCursorStackPointer > 0) {
            memberCursorStack[--memberCursorStackPointer] = 0
        }

        advanceRmqEpoch()
    }

    private fun updateDeferred(
        depth: Int,
        rank: Long,
    ) {
        updateTree(
            depth = depth,
            rank = rank,
            rankTree = deferredRankTree,
            argTree = deferredArgTree,
            epochTree = deferredEpochTree,
        )
    }

    private fun updateSubstitutable(
        depth: Int,
        rank: Long,
    ) {
        updateTree(
            depth = depth,
            rank = rank,
            rankTree = substitutableRankTree,
            argTree = substitutableArgTree,
            epochTree = substitutableEpochTree,
        )
    }

    /**
     * Point-update into the segment tree.
     *
     * Comparison contract:
     * - lower unsigned rank wins
     * - if ranks are equal, lower depth wins
     */
    private fun updateTree(
        depth: Int,
        rank: Long,
        rankTree: LongArray,
        argTree: IntArray,
        epochTree: IntArray,
    ) {
        var idx = rmqBase + depth
        setTreeNode(
            idx = idx,
            rank = rank,
            arg = if (rank == INF_RANK) NO_INDEX else depth,
            rankTree = rankTree,
            argTree = argTree,
            epochTree = epochTree,
        )

        idx = idx ushr 1
        while (idx > 0) {
            val left = idx shl 1
            val right = left + 1

            val leftRank = nodeRank(left, rankTree, epochTree)
            val leftArg = nodeArg(left, argTree, epochTree)

            val rightRank = nodeRank(right, rankTree, epochTree)
            val rightArg = nodeArg(right, argTree, epochTree)

            if (isBetter(leftRank, leftArg, rightRank, rightArg)) {
                setTreeNode(
                    idx = idx,
                    rank = leftRank,
                    arg = leftArg,
                    rankTree = rankTree,
                    argTree = argTree,
                    epochTree = epochTree,
                )
            } else {
                setTreeNode(
                    idx = idx,
                    rank = rightRank,
                    arg = rightArg,
                    rankTree = rankTree,
                    argTree = argTree,
                    epochTree = epochTree,
                )
            }

            idx = idx ushr 1
        }
    }

    /**
     * Inclusive range query over one stage-specific RMQ tree.
     */
    private fun queryRange(
        leftDepthInclusive: Int,
        rightDepthInclusive: Int,
        rankTree: LongArray,
        argTree: IntArray,
        epochTree: IntArray,
    ): DepthWinner? {
        validateDepth(leftDepthInclusive)
        validateDepth(rightDepthInclusive)

        if (leftDepthInclusive > rightDepthInclusive) {
            return null
        }

        var l = rmqBase + leftDepthInclusive
        var r = rmqBase + rightDepthInclusive

        var bestRank = INF_RANK
        var bestDepth = NO_INDEX

        while (l <= r) {
            if ((l and 1) == 1) {
                val rank = nodeRank(l, rankTree, epochTree)
                val arg = nodeArg(l, argTree, epochTree)
                if (isBetter(rank, arg, bestRank, bestDepth)) {
                    bestRank = rank
                    bestDepth = arg
                }
                l++
            }

            if ((r and 1) == 0) {
                val rank = nodeRank(r, rankTree, epochTree)
                val arg = nodeArg(r, argTree, epochTree)
                if (isBetter(rank, arg, bestRank, bestDepth)) {
                    bestRank = rank
                    bestDepth = arg
                }
                r--
            }

            l = l ushr 1
            r = r ushr 1
        }

        return if (bestDepth == NO_INDEX) {
            null
        } else {
            DepthWinner.issue(
                edgeRank = bestRank,
                depth = bestDepth,
            )
        }
    }

    private fun nodeRank(
        idx: Int,
        rankTree: LongArray,
        epochTree: IntArray,
    ): Long =
        if (epochTree[idx] == currentRmqEpoch) {
            rankTree[idx]
        } else {
            INF_RANK
        }

    private fun nodeArg(
        idx: Int,
        argTree: IntArray,
        epochTree: IntArray,
    ): Int =
        if (epochTree[idx] == currentRmqEpoch) {
            argTree[idx]
        } else {
            NO_INDEX
        }

    private fun setTreeNode(
        idx: Int,
        rank: Long,
        arg: Int,
        rankTree: LongArray,
        argTree: IntArray,
        epochTree: IntArray,
    ) {
        rankTree[idx] = rank
        argTree[idx] = arg
        epochTree[idx] = currentRmqEpoch
    }

    /**
     * Comparator for RMQ winners.
     *
     * Ordering:
     * - invalid entries lose
     * - smaller unsigned rank wins
     * - on equal rank, smaller depth wins
     */
    private fun isBetter(
        leftRank: Long,
        leftDepth: Int,
        rightRank: Long,
        rightDepth: Int,
    ): Boolean {
        if (leftDepth == NO_INDEX) return false
        if (rightDepth == NO_INDEX) return true

        val cmp = java.lang.Long.compareUnsigned(leftRank, rightRank)
        return cmp < 0 || (cmp == 0 && leftDepth < rightDepth)
    }

    /**
     * Advances RMQ epoch for O(1) logical invalidation.
     *
     * On rare epoch exhaustion, the epoch arrays are physically cleared and the epoch
     * counter is restarted from 1.
     */
    private fun advanceRmqEpoch() {
        currentRmqEpoch++
        if (currentRmqEpoch == Int.MAX_VALUE) {
            Arrays.fill(deferredEpochTree, 0)
            Arrays.fill(substitutableEpochTree, 0)
            currentRmqEpoch = 1
        }
    }

    private fun validateDepth(depth: Int) {
        if (depth <= 0 || depth >= incomingEdgeRankAtDepth.size) {
            throw PlanningProtocolIntegrityException(
                "Depth out of bounds for L1Structures: depth=$depth, max=${incomingEdgeRankAtDepth.size - 1}",
            )
        }
    }
}

class DepthWinner private constructor(
    val edgeRank: Long,
    val depth: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            edgeRank: Long,
            depth: Int,
        ): DepthWinner =
            DepthWinner(
                edgeRank = edgeRank,
                depth = depth,
            )
    }
}
