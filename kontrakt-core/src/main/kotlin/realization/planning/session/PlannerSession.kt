package realization.planning.session

import governance.budget.contract.BudgetTrack
import governance.budget.contract.CostCenter
import realization.identity.interning.PlanCacheKey
import realization.planning.assembly.ChildResultSlice
import stage.canonicalization.material.CanonicalSignature
import stage.input.presentation.dto.MemberFact
import stage.lowering.contract.BreakpointStage
import stage.lowering.diagnostics.FuelExhaustedException
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import stage.lowering.diagnostics.PlanningRuntimeInvariantException
import stage.lowering.material.OrderedActiveMembers
import statemachine.transition.material.CommittedPlanNode

/**
 * Aggregate root for the worker-local semantic planning runtime.
 *
 * Architectural role:
 * - owns the worker-local L1 substrate
 * - owns the execution-frame stack
 * - owns session-local rollback checkpoints and transient child-result buffers
 * - owns runtime-only governance flags such as session-remainder L2 bypass
 *
 * Important boundary rules:
 * - execution frames are immutable descriptors
 * - mutable traversal state is stored in session-owned primitive structures
 * - rollback restores checkpointed session state, not arbitrary object fields
 * - physical / semantic counters remain monotonic and are NOT rolled back
 *
 * This type is intentionally stateful:
 * it is the runtime aggregate that governs one worker-local planning session lifecycle.
 */
class PlannerSession private constructor(
    val config: PlannerSessionConfig,
) : SessionKernel,
    AutoCloseable {
    /**
     * Worker-local primitive planning substrate.
     *
     * Owns:
     * - active path topology
     * - incoming edge metadata at each active depth
     * - member cursor stack
     * - RMQ state for breakpoint selection
     */
    internal val structures =
        L1Structures.issue(
            maxNodeIdCap = config.caps.maxNodeIdCap,
            maxSemanticDepth = config.caps.maxDepthCap,
        )

    /**
     * Dense node identity indexer used by the planner hot path.
     *
     * This is worker-local and reset between sessions.
     */
    internal val indexer =
        NodeIdIndexer.issue(
            caps = config.caps,
            maxSignatureLen = config.budget.maxSignatureLen,
        )

    /**
     * SessionKernel self-reference used by components that require kernel callbacks.
     */
    private val kernel: SessionKernel = this

    /**
     * Monotonic cumulative counters.
     *
     * These counters represent already-consumed work and MUST NOT be rolled back.
     * Session-local budget usage is measured relative to [sessionStartPhysical] and
     * [sessionStartSemantic].
     */
    private var cumulativePhysicalSteps: Long = 0L
    private var cumulativeSemanticWork: Long = 0L
    private var sessionStartPhysical: Long = 0L
    private var sessionStartSemantic: Long = 0L

    /**
     * Sticky abort flag for the current session.
     */
    var isAborted: Boolean = false
        private set

    /**
     * Rollback-scoped planner checkpoints.
     *
     * These values are restored by TransactionalFrame snapshots and represent
     * session-local mutable state that is safe to rewind.
     */
    private var softCheckpoint: Long = 0L
    private var placeholderCounter: Int = 0
    private var builderLogPos: Int = 0
    private var cacheLogPos: Int = 0

    /**
     * Explicit execution-frame stack.
     *
     * Capacity is fixed from session caps so runtime expansion does not allocate.
     */
    private val executionStack = ExecutionFrameStack.issue(config.caps.maxDepthCap + 8)

    /**
     * Session-local substitution table.
     *
     * Used when a computed canonical result is replaced by a semantically equivalent
     * substitution / deferred form for downstream observation.
     */
    private val substitutionMap = HashMap<PlanCacheKey, CommittedPlanNode>()

    /**
     * Child results produced by completed frames.
     *
     * AllocateFrame uses [childResultStart] as a watermark to interpret the suffix
     * belonging to one local assembly operation.
     */
    private val childResults = ArrayList<CommittedPlanNode>(128)

    /**
     * Primitive collision trackers reused across uniqueness checks.
     */
    private val edgeTracker = PrimitiveMemberTracker.issue(256)
    private val entropyTracker = PrimitiveMemberTracker.issue(256)

    /**
     * Reusable cursor view over [childResults].
     *
     * This avoids per-allocation child descriptor list churn.
     */
    private val childCursor = SessionChildDescriptorCursor.issue(childResults)

    /**
     * Session-remainder L2 bypass flag.
     *
     * Once raised, the planner no longer attempts L2 reads during the same session.
     */
    private var l2Bypassed: Boolean = false

    /**
     * Starts a new logical planning session on this worker-local runtime.
     *
     * This does NOT recreate the object; instead, it resets the session-relative
     * baseline against the monotonic counters.
     */
    fun startSession() {
        sessionStartPhysical = cumulativePhysicalSteps
        sessionStartSemantic = cumulativeSemanticWork
        isAborted = false
        l2Bypassed = false

        softCheckpoint = 0L
        placeholderCounter = 0
        builderLogPos = 0
        cacheLogPos = 0
    }

    /**
     * Charges one unit of work to the configured cost center.
     *
     * Rules:
     * - physical work always increments
     * - semantic work increments only when the center is semantic-also
     * - budgets are fail-closed
     */
    override fun step(center: CostCenter) {
        cumulativePhysicalSteps++
        val physicalUsed = cumulativePhysicalSteps - sessionStartPhysical
        if (physicalUsed > config.budget.maxPhysicalSteps.toLong()) {
            abort("Physical budget exhausted ($physicalUsed steps)")
        }

        if (center.track == BudgetTrack.SEMANTIC_ALSO) {
            cumulativeSemanticWork++
            val semanticUsed = cumulativeSemanticWork - sessionStartSemantic
            if (semanticUsed > config.budget.maxSemanticWorkUnits.toLong()) {
                abort("Semantic budget exhausted ($semanticUsed units)")
            }
        }
    }

    /**
     * Callback invoked when a new dense node id becomes live for this session.
     *
     * This is the authoritative moment where worker-local per-node primitive state
     * is reinitialized before semantic reuse.
     */
    override fun onNodeAllocated(nodeId: Int) {
        structures.initNode(nodeId)
    }

    /**
     * Delegates an indexer rollback to the underlying node id indexer.
     */
    fun performIndexerRollback(ptr: Int) {
        indexer.rollback(ptr, kernel)
    }

    /**
     * Snapshot accessors used by TransactionalFrame.
     */
    fun currentSoftCheckpoint(): Long = softCheckpoint

    fun currentPlaceholderCounter(): Int = placeholderCounter

    fun currentBuilderLogPos(): Int = builderLogPos

    fun currentCacheLogPos(): Int = cacheLogPos

    fun currentMemberCursorStackPointer(): Int = structures.memberCursorStackPointer

    /**
     * Restores rollback-scoped session state from a transaction snapshot.
     *
     * Important:
     * - this method does NOT touch monotonic counters
     * - childResults are rewound to the saved builder watermark
     * - member cursor values are logically rewound by reducing the cursor-stack pointer
     */
    fun restoreCheckpointState(
        softCheckpoint: Long,
        placeholderCounter: Int,
        builderLogPos: Int,
        cacheLogPos: Int,
        memberCursorStackPointer: Int,
    ) {
        this.softCheckpoint = softCheckpoint
        this.placeholderCounter = placeholderCounter
        this.builderLogPos = builderLogPos
        this.cacheLogPos = cacheLogPos

        while (childResults.size > builderLogPos) {
            childResults.removeAt(childResults.size - 1)
        }

        while (structures.memberCursorStackPointer > memberCursorStackPointer) {
            structures.memberCursorStack[--structures.memberCursorStackPointer] = 0
        }
    }

    /**
     * Returns whether there are frames left to execute.
     */
    fun hasActiveFrames(): Boolean = executionStack.isNotEmpty()

    /**
     * Returns the current frame at the top of the explicit execution stack.
     */
    internal fun peekExecutionFrame(): ExecutionFrame = executionStack.last()

    /**
     * Returns the current top execution index.
     *
     * This is used when a child PlanNodeFrame needs to remember the ExpandEdgeFrame
     * that emitted the incoming edge.
     */
    internal fun currentExecutionIndex(): Int = executionStack.lastIndex()

    /**
     * Pushes a new execution frame after first taking its transactional snapshot.
     */
    internal fun pushExecutionFrame(frame: ExecutionFrame) {
        frame.tx.snap(this)
        executionStack.push(frame)
    }

    /**
     * Pops the current execution frame.
     */
    internal fun popExecutionFrame(): ExecutionFrame = executionStack.pop()

    /**
     * Replaces the current top execution frame.
     *
     * The incoming frame is snapshotted against the post-transition state.
     */
    private fun replaceTopExecutionFrame(frame: ExecutionFrame) {
        frame.tx.snap(this)
        executionStack.replaceTop(frame)
    }

    /**
     * Transitions from PLAN_NODE to ITERATE_MEMBERS.
     */
    internal fun transitionToIterate(
        frame: PlanNodeFrame,
        orderedMembers: OrderedActiveMembers,
    ) {
        replaceTopExecutionFrame(
            IterateMembersFrame.issue(
                typeReference = frame.typeReference,
                orderedMembers = orderedMembers,
            ),
        )
    }

    /**
     * Transitions from ITERATE_MEMBERS to EXPAND_EDGE.
     *
     * A session-owned cursor slot is allocated first and then attached to the frame.
     * If the frame transition fails before ownership transfer completes, the allocation
     * is rolled back in LIFO order.
     */
    internal fun transitionToExpand(frame: IterateMembersFrame) {
        val cursorSlot = allocateMemberCursorSlot()
        try {
            replaceTopExecutionFrame(
                ExpandEdgeFrame.issue(
                    typeReference = frame.typeReference,
                    orderedMembers = frame.orderedMembers,
                    memberCursorSlot = cursorSlot,
                    memberCount = frame.orderedMembers.size(),
                ),
            )
        } catch (t: Throwable) {
            rollbackMemberCursorAllocation(cursorSlot)
            throw t
        }
    }

    /**
     * Transitions from EXPAND_EDGE to ALLOCATE.
     *
     * Ordering is intentional:
     * 1. release the expansion cursor slot
     * 2. snapshot the post-release state for ALLOCATE
     *
     * ALLOCATE no longer owns traversal-cursor state.
     */
    internal fun transitionToAllocate(
        frame: ExpandEdgeFrame,
        signature: CanonicalSignature,
    ) {
        releaseMemberCursorSlot(frame)

        replaceTopExecutionFrame(
            AllocateFrame.issue(
                typeReference = frame.typeReference,
                orderedMembers = frame.orderedMembers,
                signature = signature,
                childResultStart = builderLogPos,
            ),
        )
    }

    /**
     * Completes the current frame and appends its committed result to the child buffer.
     *
     * For ALLOCATE completion:
     * - active-path membership is unwound here
     * - per-depth edge metadata is cleared here
     *
     * This preserves the rule that active-path membership is removed only after
     * the node result has reached completed state.
     */
    internal fun completeFrame(
        frame: ExecutionFrame,
        result: CommittedPlanNode,
    ) {
        if (peekExecutionFrame() !== frame) {
            throw PlanningRuntimeInvariantException(
                "completeFrame() observed an execution-frame order violation.",
            )
        }
        popExecutionFrame()

        if (frame is AllocateFrame) {
            if (structures.stackPointer <= 0) {
                throw PlanningProtocolIntegrityException(
                    "Missing active-stack membership for ALLOCATE completion.",
                )
            }

            val depth = structures.stackPointer
            structures.stackPointer--

            val nodeId = structures.activeStack[structures.stackPointer]
            structures.depthOfNodeId[nodeId] = 0
            structures.clearDepthMetadata(depth)
        }

        childResults.add(result)
        builderLogPos = childResults.size
    }

    /**
     * Returns the final root result after DFS completion.
     */
    fun getRootResult(): CommittedPlanNode {
        if (childResults.isEmpty()) {
            throw PlanningRuntimeInvariantException("Planner produced no root result.")
        }
        return childResults.last()
    }

    /**
     * Rebinds the reusable child cursor to the suffix owned by [frame].
     *
     * This is the bounded child-result window used by passive assembly.
     */
    internal fun bindChildDescriptorCursor(frame: AllocateFrame): ChildResultSlice {
        childCursor.rebind(frame.childResultStart, childResults.size)
        return childCursor
    }

    /**
     * Computes the total semantic cost of the child-result suffix owned by [frame].
     */
    internal fun collectChildSemanticCost(frame: AllocateFrame): Long {
        var total = 0L
        var idx = frame.childResultStart
        while (idx < childResults.size) {
            total += childResults[idx].treeSemanticCostUpperBound
            idx++
        }
        return total
    }

    /**
     * Records a substitution result under the given cache key.
     */
    fun recordSubstitution(
        key: PlanCacheKey,
        node: CommittedPlanNode,
    ) {
        substitutionMap[key] = node
    }

    /**
     * Returns a substitution result if one was recorded for the key.
     */
    fun findSubstitution(key: PlanCacheKey): CommittedPlanNode? = substitutionMap[key]

    /**
     * Reusable trackers for edge-key and entropy-key uniqueness checks.
     */
    internal fun acquireEdgeTracker(): PrimitiveMemberTracker = edgeTracker.apply { reset() }

    internal fun acquireEntropyTracker(): PrimitiveMemberTracker = entropyTracker.apply { reset() }

    /**
     * Enters the active path for the given identity or reports an existing active depth.
     *
     * Returns:
     * - `-1` when the node was newly entered
     * - existing active depth when the identity is already on the active path
     */
    fun enterOrDetectCycle(
        identityBits: Long,
        signature: CanonicalSignature,
    ): Int {
        val nodeId = indexer.findOrAssign(identityBits, signature.bytesCopy(), this)
        val existingDepth = structures.depthOfNodeId[nodeId]
        if (existingDepth > 0) {
            return existingDepth
        }

        val newDepth = structures.stackPointer + 1
        structures.activeStack[structures.stackPointer++] = nodeId
        structures.depthOfNodeId[nodeId] = newDepth
        return -1
    }

    /**
     * Binds the incoming edge metadata of the current node to the active depth
     * that was just entered by [enterOrDetectCycle].
     *
     * Root frames carry sentinel incoming-edge metadata and therefore clear the slot.
     */
    internal fun bindIncomingEdgeAtCurrentDepth(frame: PlanNodeFrame) {
        val depth = structures.stackPointer
        if (depth <= 0) {
            throw PlanningProtocolIntegrityException(
                "bindIncomingEdgeAtCurrentDepth() requires an active depth.",
            )
        }

        if (!frame.hasIncomingEdge()) {
            structures.clearDepthMetadata(depth)
            return
        }

        structures.bindIncomingEdge(
            depth = depth,
            edgeRank = frame.incomingEdgeRank,
            stageTag = frame.incomingEdgeStageTag,
            ownerExecutionIndex = frame.incomingExpandExecutionIndex,
            memberIndex = frame.incomingMemberIndex,
        )
    }

    /**
     * Returns whether EXPAND_EDGE still has members left to consume.
     */
    internal fun hasMoreMembers(frame: ExpandEdgeFrame): Boolean =
        structures.memberCursorStack[frame.memberCursorSlot] < frame.memberCount

    /**
     * Consumes one order-ordered member index from the session-owned cursor slot.
     *
     * The cursor lives in the session substrate, never in the frame object.
     */
    internal fun consumeNextMemberIndex(frame: ExpandEdgeFrame): Int {
        val slot = frame.memberCursorSlot
        val current = structures.memberCursorStack[slot]
        if (current >= frame.memberCount) {
            throw PlanningProtocolIntegrityException(
                "Member cursor overflow for slot=${frame.memberCursorSlot}, count=${frame.memberCount}",
            )
        }
        structures.memberCursorStack[slot] = current + 1
        return current
    }

    /**
     * Returns the deterministic breakpoint decision for the current active cycle segment.
     *
     * Ordering:
     * - DEFERRED stage first
     * - then SUBSTITUTABLE stage
     * - inside one stage: smaller unsigned edgeRank wins
     * - tie-breaker: smaller depth / stack position wins
     *
     * Candidate universe:
     * - current active cycle segment incoming edges
     * - current back-edge
     */
    internal fun attemptDeterministicBreak(
        cycleDepth: Int,
        backEdge: PlanNodeFrame,
    ): CycleBreakpointDecision? {
        val currentDepth = structures.stackPointer
        val segmentStart = cycleDepth + 1

        val deferredWinner = structures.queryBestDeferred(segmentStart, currentDepth)
        val backIsDeferred =
            backEdge.hasIncomingEdge() && backEdge.incomingEdgeStageTag == BreakpointStage.DEFERRED.tag
        if (deferredWinner != null || backIsDeferred) {
            return chooseBestStageWinner(
                stage = BreakpointStage.DEFERRED,
                segmentWinner = deferredWinner,
                backEdge = backEdge,
                currentDepth = currentDepth,
            )
        }

        val substitutableWinner = structures.queryBestSubstitutable(segmentStart, currentDepth)
        val backIsSubstitutable =
            backEdge.hasIncomingEdge() && backEdge.incomingEdgeStageTag == BreakpointStage.SUBSTITUTABLE.tag
        if (substitutableWinner != null || backIsSubstitutable) {
            return chooseBestStageWinner(
                stage = BreakpointStage.SUBSTITUTABLE,
                segmentWinner = substitutableWinner,
                backEdge = backEdge,
                currentDepth = currentDepth,
            )
        }

        return null
    }

    /**
     * Resolves the ordered-member owner view for the winning breakpoint edge.
     */
    internal fun resolveBreakpointOwnerFacts(decision: CycleBreakpointDecision): OrderedActiveMembers {
        val frame =
            executionStack.get(decision.ownerExecutionIndex) as? ExpandEdgeFrame
                ?: throw PlanningProtocolIntegrityException(
                    "Breakpoint owner must resolve to ExpandEdgeFrame at index=${decision.ownerExecutionIndex}",
                )
        return frame.orderedMembers
    }

    /**
     * Resolves the concrete member selected by the winning breakpoint decision.
     */
    internal fun resolveBreakpointMember(decision: CycleBreakpointDecision): MemberFact {
        val frame =
            executionStack.get(decision.ownerExecutionIndex) as? ExpandEdgeFrame
                ?: throw PlanningProtocolIntegrityException(
                    "Breakpoint owner must resolve to ExpandEdgeFrame at index=${decision.ownerExecutionIndex}",
                )
        return frame.orderedMembers.memberAt(decision.memberIndex)
    }

    /**
     * Collects deterministic cycle-demotion evidence for diagnostics.
     *
     * This is intentionally cold-path and explicit.
     */
    fun collectDemotionEvidence(cycleDepth: Int): List<String> {
        val out = ArrayList<String>(8)
        val startDepth = cycleDepth.coerceAtLeast(1)
        var depth = startDepth
        while (depth <= structures.stackPointer) {
            val execIndex = structures.incomingExpandExecutionIndexAtDepth[depth]
            val memberIndex = structures.incomingMemberIndexAtDepth[depth]
            if (execIndex >= 0 && memberIndex >= 0) {
                val frame = executionStack.get(execIndex) as? ExpandEdgeFrame
                if (frame != null) {
                    val member = frame.orderedMembers.memberAt(memberIndex)
                    out.add("${frame.typeReference}#${member.name}")
                }
            }
            depth++
        }
        out.sort()
        return out
    }

    /**
     * Returns the normalization version pinned for the current session.
     */
    fun currentNormalizationVersion(): Long = config.versions.normalizationSpecVersion.toLong()

    /**
     * Returns whether the session is in remainder-bypass mode for L2.
     */
    internal fun isL2Bypassed(): Boolean = l2Bypassed

    /**
     * Raises the session-remainder L2 bypass flag.
     */
    internal fun markL2Bypassed() {
        l2Bypassed = true
    }

    /**
     * Restores the entire worker-local session state to a clean reusable baseline.
     *
     * This is the authoritative session cleanup boundary.
     */
    internal fun resetToCleanState() {
        structures.reset()
        indexer.reset(this)
        substitutionMap.clear()
        executionStack.clear()
        childResults.clear()
        edgeTracker.reset()
        entropyTracker.reset()

        softCheckpoint = 0L
        placeholderCounter = 0
        builderLogPos = 0
        cacheLogPos = 0
    }

    override fun close() {
        resetToCleanState()
    }

    /**
     * Fail-closed abort path used by budget enforcement.
     */
    private fun abort(reason: String): Nothing {
        isAborted = true
        throw FuelExhaustedException(reason)
    }

    /**
     * Allocates one member cursor slot in session-owned primitive storage.
     */
    private fun allocateMemberCursorSlot(): Int {
        val slot = structures.memberCursorStackPointer
        if (slot >= structures.memberCursorStack.size) {
            throw PlanningProtocolIntegrityException(
                "Member cursor stack exhausted at slot=$slot",
            )
        }
        structures.memberCursorStack[slot] = 0
        structures.memberCursorStackPointer = slot + 1
        return slot
    }

    /**
     * Rolls back a just-allocated cursor slot when frame-transition ownership fails.
     *
     * This is a structural guard for transition failure before the new frame has been
     * successfully installed on the execution stack.
     */
    private fun rollbackMemberCursorAllocation(slot: Int) {
        if (structures.memberCursorStackPointer != slot + 1) {
            throw PlanningProtocolIntegrityException(
                "Cursor allocation rollback must be LIFO. slot=$slot, pointer=${structures.memberCursorStackPointer}",
            )
        }
        structures.memberCursorStack[slot] = 0
        structures.memberCursorStackPointer = slot
    }

    /**
     * Releases the cursor slot owned by an EXPAND_EDGE frame.
     *
     * The release must be LIFO to preserve stack-discipline guarantees.
     */
    private fun releaseMemberCursorSlot(frame: ExpandEdgeFrame) {
        val expectedTop = frame.memberCursorSlot + 1
        if (structures.memberCursorStackPointer != expectedTop) {
            throw PlanningProtocolIntegrityException(
                "Member cursor release must be LIFO. slot=${frame.memberCursorSlot}, pointer=${structures.memberCursorStackPointer}",
            )
        }
        structures.memberCursorStack[frame.memberCursorSlot] = 0
        structures.memberCursorStackPointer = frame.memberCursorSlot
    }

    /**
     * Chooses the winning breakpoint candidate within one stage.
     *
     * Candidates:
     * - the best segment winner from RMQ
     * - the current back-edge, modeled as a virtual depth `currentDepth + 1`
     */
    private fun chooseBestStageWinner(
        stage: BreakpointStage,
        segmentWinner: DepthWinner?,
        backEdge: PlanNodeFrame,
        currentDepth: Int,
    ): CycleBreakpointDecision {
        val backEdgePresent = backEdge.hasIncomingEdge() && backEdge.incomingEdgeStageTag == stage.tag

        if (!backEdgePresent && segmentWinner != null) {
            return buildDecisionFromDepth(stage, segmentWinner.depth)
        }

        if (backEdgePresent && segmentWinner == null) {
            return CycleBreakpointDecision.issue(
                stage = stage,
                ownerExecutionIndex = backEdge.incomingExpandExecutionIndex,
                memberIndex = backEdge.incomingMemberIndex,
                selectedStackIndex = currentDepth + 1,
                isBackEdge = true,
            )
        }

        val segment =
            segmentWinner
                ?: throw PlanningProtocolIntegrityException("Expected segment winner for stage=$stage")

        val virtualIndex = currentDepth + 1
        val backRank = backEdge.incomingEdgeRank
        val segRank = segment.edgeRank

        val cmp = java.lang.Long.compareUnsigned(backRank, segRank)
        return if (cmp < 0 || (cmp == 0 && virtualIndex < segment.depth)) {
            return CycleBreakpointDecision.issue(
                stage = stage,
                ownerExecutionIndex = backEdge.incomingExpandExecutionIndex,
                memberIndex = backEdge.incomingMemberIndex,
                selectedStackIndex = virtualIndex,
                isBackEdge = true,
            )
        } else {
            buildDecisionFromDepth(stage, segment.depth)
        }
    }

    /**
     * Builds a breakpoint decision from one active depth.
     */
    private fun buildDecisionFromDepth(
        stage: BreakpointStage,
        depth: Int,
    ): CycleBreakpointDecision =
        CycleBreakpointDecision.issue(
            stage = stage,
            ownerExecutionIndex = structures.incomingExpandExecutionIndexAtDepth[depth],
            memberIndex = structures.incomingMemberIndexAtDepth[depth],
            selectedStackIndex = depth,
            isBackEdge = false,
        )

    companion object {
        @JvmStatic
        fun issue(config: PlannerSessionConfig): PlannerSession = PlannerSession(config)
    }
}

/**
 * Immutable planner-time breakpoint decision.
 *
 * This is cold-path output:
 * - produced only when a cycle is detected
 * - consumed by the orchestration layer to drive downstream materialization
 */
internal class CycleBreakpointDecision private constructor(
    val stage: BreakpointStage,
    val ownerExecutionIndex: Int,
    val memberIndex: Int,
    val selectedStackIndex: Int,
    val isBackEdge: Boolean,
) {
    companion object {
        @JvmStatic
        fun issue(
            stage: BreakpointStage,
            ownerExecutionIndex: Int,
            memberIndex: Int,
            selectedStackIndex: Int,
            isBackEdge: Boolean,
        ): CycleBreakpointDecision =
            CycleBreakpointDecision(
                stage = stage,
                ownerExecutionIndex = ownerExecutionIndex,
                memberIndex = memberIndex,
                selectedStackIndex = selectedStackIndex,
                isBackEdge = isBackEdge,
            )
    }
}

/**
 * Reusable zero-allocation child-result slice view.
 *
 * This object never owns child results.
 * It merely exposes a bounded suffix window over the session-local child buffer.
 */
internal class SessionChildDescriptorCursor private constructor(
    private val backing: ArrayList<CommittedPlanNode>,
) : ChildResultSlice {
    private var start: Int = 0
    private var endExclusive: Int = 0

    /**
     * Rebinds this view to a new suffix window.
     */
    fun rebind(
        start: Int,
        endExclusive: Int,
    ) {
        this.start = start
        this.endExclusive = endExclusive
    }

    override fun size(): Int = endExclusive - start

    override fun canonicalIrNodeAt(index: Int) = backing[start + index].irNode

    override fun semanticCostUpperBoundAt(index: Int): Long = backing[start + index].treeSemanticCostUpperBound

    companion object {
        @JvmStatic
        fun issue(backing: ArrayList<CommittedPlanNode>): SessionChildDescriptorCursor =
            SessionChildDescriptorCursor(backing)
    }
}
