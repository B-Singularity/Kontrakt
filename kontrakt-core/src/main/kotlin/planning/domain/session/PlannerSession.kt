package planning.domain.session

import ir.identity.CanonicalSignature
import ir.plan.signature.PlanCacheKey
import metamodel.domain.dto.TypeFactsDTO
import planning.domain.exception.FuelExhaustedException
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.exception.PlanningRuntimeInvariantException
import planning.domain.protocol.BudgetTrack
import planning.domain.protocol.CostCenter
import planning.domain.runtime.CommittedPlanNode
import planning.domain.runtime.DeferredCommittedPlanNode
import planning.domain.runtime.SubstitutionCommittedPlanNode
import planning.domain.session.PlannerSession.Companion.issue

/**
 * Aggregate root for the worker-local planning runtime.
 *
 * This object is the constitutional owner of the Phase-4 planning session state.
 * It centralizes:
 *
 * 1. Budget metering
 *    - [step] is the single legal mutation gate for physical/semantic counters.
 *    - counters are monotonic and are never rolled back.
 *
 * 2. Worker-local execution substrate
 *    - [L1Structures]
 *    - [NodeIdIndexer]
 *    - explicit execution-frame stack
 *
 * 3. Runtime orchestration state
 *    - child result accumulation
 *    - local substitution consistency during L2 degradation / bypass
 *    - primitive uniqueness trackers reused across hot paths
 *
 * 4. Zero-residue cleanup
 *    - all worker-local mutable state is reset in [resetToCleanState]
 *    - this is required to make session reuse deterministic after abort / unwind
 *
 * Design notes:
 * - This is intentionally NOT a generic state bag.
 * - It is the aggregate boundary that enforces the planning runtime laws.
 * - Constructors are blocked to force creation through [issue].
 */
class PlannerSession private constructor(
    /**
     * Immutable session configuration.
     *
     * This contains:
     * - memory/capacity ceilings
     * - version seeds
     * - physical/semantic budget limits
     */
    val config: PlannerSessionConfig,
) : SessionKernel, AutoCloseable {

    /**
     * Worker-local pooled primitive structures.
     *
     * Owns:
     * - active stack
     * - GREY map
     * - RMQ-related dense arrays
     */
    internal val structures = L1Structures(config)

    /**
     * Dense node identity allocator / indexer.
     *
     * Maps deterministic identity bits + signature bytes to a dense node id.
     */
    internal val indexer = NodeIdIndexer(config)

    /**
     * Internal bridge reference used when primitives need to call back into the session
     * for metering or lifecycle hooks without depending on the concrete aggregate type.
     */
    private val kernel: SessionKernel = this

    // -------------------------------------------------------------------------
    // Monotonic counters
    // -------------------------------------------------------------------------

    /**
     * Cumulative physical work consumed since the lifetime of this session object.
     *
     * Important:
     * - this is monotonic
     * - this is NOT rolled back by transactional unwind
     * - session-local usage is computed via per-session baselines
     */
    private var cumulativePhysicalSteps: Long = 0L

    /**
     * Cumulative semantic work consumed since the lifetime of this session object.
     *
     * Important:
     * - this is monotonic
     * - this is NOT rolled back by transactional unwind
     * - it follows the same baseline scheme as physical counters
     */
    private var cumulativeSemanticWork: Long = 0L

    // -------------------------------------------------------------------------
    // Session baselines
    // -------------------------------------------------------------------------

    /**
     * Physical baseline captured at [startSession].
     *
     * The current session's physical usage is:
     * current cumulative physical - this baseline
     */
    private var sessionStartPhysical: Long = 0L

    /**
     * Semantic baseline captured at [startSession].
     *
     * The current session's semantic usage is:
     * current cumulative semantic - this baseline
     */
    private var sessionStartSemantic: Long = 0L

    /**
     * Sticky abort flag for the current session run.
     *
     * This is set only when the runtime fails closed due to budget exhaustion.
     */
    var isAborted: Boolean = false
        private set

    // -------------------------------------------------------------------------
    // Frame-local checkpoints
    // -------------------------------------------------------------------------

    /**
     * Frame-local soft checkpoint.
     *
     * This is intentionally NOT the global semantic budget counter.
     * It represents rollback-scoped local planner state that may be restored by
     * [TransactionalFrame.rollback].
     */
    private var softCheckpoint: Long = 0L

    /**
     * Local placeholder counter used during assembly / planning.
     *
     * This belongs to rollback-scoped execution state, not to global monotonic budgets.
     */
    private var placeholderCounter: Int = 0

    /**
     * Watermark into [childResults].
     *
     * Transactional rollback may restore this to discard children produced by
     * a failed speculative branch.
     */
    private var builderLogPos: Int = 0

    /**
     * Cache/local governance log position checkpoint.
     *
     * This is rollback-scoped local state, not a global cost counter.
     */
    private var cacheLogPos: Int = 0

    // -------------------------------------------------------------------------
    // Runtime-owned collections
    // -------------------------------------------------------------------------

    /**
     * Explicit execution stack for the iterative DFS machine.
     *
     * Native recursion is constitutionally forbidden; execution state must be carried here.
     */
    private val executionStack = ArrayList<ExecutionFrame>(64)

    /**
     * Session-local substitution map.
     *
     * Purpose:
     * - preserve local canonical consistency when L2 degrades or is bypassed
     * - guarantee "same key -> same committed node" within the current session
     */
    private val substitutionMap = HashMap<PlanCacheKey, CommittedPlanNode>()

    /**
     * Accumulated committed child results.
     *
     * Allocate frames consume a suffix of this list according to their child-result watermark.
     */
    private val childResults = ArrayList<CommittedPlanNode>(128)

    /**
     * Reusable primitive tracker for CanonicalEdgeKey uniqueness checks.
     */
    private val edgeTracker = PrimitiveMemberTracker.issue(256)

    /**
     * Reusable primitive tracker for EntropyTargetKey uniqueness checks.
     */
    private val entropyTracker = PrimitiveMemberTracker.issue(256)

    private var l2Bypassed: Boolean = false

    /**
     * Starts a new logical planning run on this session object.
     *
     * This does not allocate a fresh aggregate; instead it:
     * - captures new baselines for physical/semantic counters
     * - resets frame-local checkpoint state
     * - clears the abort flag for the new run
     *
     * Important:
     * - cumulative counters remain monotonic across runs
     * - session-local usage is computed relative to the newly captured baselines
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
     * Single legal mutation gate for cost accounting.
     *
     * Rules enforced here:
     * - every call increments the physical counter
     * - only [BudgetTrack.SEMANTIC_ALSO] increments the semantic counter
     * - both are checked against per-session baselines
     * - any overflow fails closed through [FuelExhaustedException]
     *
     * This method is the constitutional choke point for runtime metering.
     */
    override fun step(center: CostCenter) {
        cumulativePhysicalSteps++
        val physicalUsed = cumulativePhysicalSteps - sessionStartPhysical
        if (physicalUsed > config.maxPhysicalSteps.toLong()) {
            abort("Physical budget exhausted ($physicalUsed steps)")
        }

        if (center.track == BudgetTrack.SEMANTIC_ALSO) {
            cumulativeSemanticWork++
            val semanticUsed = cumulativeSemanticWork - sessionStartSemantic
            if (semanticUsed > config.maxSemanticWorkUnits.toLong()) {
                abort("Semantic budget exhausted ($semanticUsed units)")
            }
        }
    }

    /**
     * Lifecycle hook invoked strictly when a fresh dense node id is allocated.
     *
     * This ensures reused dense arrays are initialized before being observed by the runtime.
     */
    override fun onNodeAllocated(nodeId: Int) {
        structures.initNode(nodeId)
    }

    /**
     * Bridge method used by transactional rollback.
     *
     * The rollback loop inside [NodeIdIndexer] is metered via the session kernel,
     * so unwind work is not "free".
     */
    fun performIndexerRollback(ptr: Int) {
        indexer.rollback(ptr, kernel)
    }

    /**
     * Returns the current frame-local soft checkpoint.
     *
     * This is rollback-scoped local state, not the global semantic budget counter.
     */
    fun currentSoftCheckpoint(): Long = softCheckpoint

    /**
     * Returns the current placeholder counter.
     */
    fun currentPlaceholderCounter(): Int = placeholderCounter

    /**
     * Returns the current builder log position.
     */
    fun currentBuilderLogPos(): Int = builderLogPos

    /**
     * Returns the current cache log position.
     */
    fun currentCacheLogPos(): Int = cacheLogPos

    /**
     * Restores rollback-scoped checkpoint state after transactional unwind.
     *
     * Important:
     * - this does NOT touch monotonic physical/semantic counters
     * - this may truncate [childResults] back to the saved builder watermark
     */
    fun restoreCheckpointState(
        softCheckpoint: Long,
        placeholderCounter: Int,
        builderLogPos: Int,
        cacheLogPos: Int,
    ) {
        this.softCheckpoint = softCheckpoint
        this.placeholderCounter = placeholderCounter
        this.builderLogPos = builderLogPos
        this.cacheLogPos = cacheLogPos

        while (childResults.size > builderLogPos) {
            childResults.removeAt(childResults.size - 1)
        }
    }

    /**
     * Returns true iff there is at least one active execution frame.
     */
    fun hasActiveFrames(): Boolean = executionStack.isNotEmpty()

    /**
     * Peeks the current execution frame.
     *
     * Fails with a custom runtime invariant exception if the stack is empty.
     */
    internal fun peekExecutionFrame(): ExecutionFrame {
        if (executionStack.isEmpty()) {
            throw PlanningRuntimeInvariantException(
                "peekExecutionFrame() called on an empty execution stack."
            )
        }
        return executionStack.last()
    }

    /**
     * Pushes a new execution frame.
     *
     * The frame's transactional snapshot is captured immediately at push time.
     */
    internal fun pushExecutionFrame(frame: ExecutionFrame) {
        frame.tx.snap(this)
        executionStack.add(frame)
    }

    /**
     * Pops the current execution frame.
     *
     * Fails with a custom runtime invariant exception if the stack is empty.
     */
    internal fun popExecutionFrame(): ExecutionFrame {
        if (executionStack.isEmpty()) {
            throw PlanningRuntimeInvariantException(
                "popExecutionFrame() called on an empty execution stack."
            )
        }
        return executionStack.removeAt(executionStack.size - 1)
    }

    /**
     * Replaces the current top frame with a new frame.
     *
     * This is used for explicit state-machine transitions between planning phases.
     */
    private fun replaceTopExecutionFrame(frame: ExecutionFrame) {
        if (executionStack.isEmpty()) {
            throw PlanningRuntimeInvariantException(
                "replaceTopExecutionFrame() called on an empty execution stack."
            )
        }
        executionStack.removeAt(executionStack.size - 1)
        pushExecutionFrame(frame)
    }

    /**
     * Transitions a [PlanNodeFrame] into the member-iteration phase.
     */
    internal fun transitionToIterate(
        frame: PlanNodeFrame,
        facts: TypeFactsDTO,
    ) {
        replaceTopExecutionFrame(
            IterateMembersFrame.issue(
                typeReference = frame.typeReference,
                facts = facts,
            )
        )
    }

    /**
     * Transitions an [IterateMembersFrame] into the edge-expansion phase.
     */
    internal fun transitionToExpand(
        frame: IterateMembersFrame,
        facts: TypeFactsDTO,
    ) {
        replaceTopExecutionFrame(
            ExpandEdgeFrame.issue(
                typeReference = frame.typeReference,
                facts = facts,
            )
        )
    }

    /**
     * Transitions an [ExpandEdgeFrame] into the bottom-up allocation/commit phase.
     */
    internal fun transitionToAllocate(
        frame: ExpandEdgeFrame,
        signature: CanonicalSignature,
    ) {
        replaceTopExecutionFrame(
            AllocateFrame.issue(
                typeReference = frame.typeReference,
                facts = frame.facts,
                signature = signature,
                childResultStart = builderLogPos,
            )
        )
    }

    /**
     * Completes the current execution frame and appends its committed result.
     *
     * Special rule:
     * - only [AllocateFrame] completion unwinds active-path GREY membership
     * - this keeps cycle membership aligned with bottom-up canonical completion
     */
    internal fun completeFrame(
        frame: ExecutionFrame,
        result: CommittedPlanNode,
    ) {
        if (executionStack.isEmpty() || executionStack.last() !== frame) {
            throw PlanningRuntimeInvariantException(
                "completeFrame() observed an execution-frame order violation."
            )
        }
        executionStack.removeAt(executionStack.size - 1)

        if (frame is AllocateFrame) {
            if (structures.stackPointer <= 0) {
                throw PlanningProtocolIntegrityException(
                    "Missing active-stack membership for ALLOCATE completion."
                )
            }
            val nodeId = structures.activeStack[--structures.stackPointer]
            structures.depthOfNodeId[nodeId] = 0
        }

        childResults.add(result)
        builderLogPos = childResults.size
    }

    /**
     * Returns the final root result.
     *
     * Fails with a custom runtime invariant exception if no result was produced.
     */
    fun getRootResult(): CommittedPlanNode {
        if (childResults.isEmpty()) {
            throw PlanningRuntimeInvariantException(
                "Planner produced no root result."
            )
        }
        return childResults.last()
    }

    /**
     * Returns the child-result slice owned by the supplied allocate frame.
     *
     * The slice starts at the frame's child-result watermark and extends to the current tail.
     */
    internal fun collectChildResults(frame: AllocateFrame): List<CommittedPlanNode> {
        val start = frame.childResultStart
        if (start < 0 || start > childResults.size) {
            throw PlanningProtocolIntegrityException(
                "Invalid child-result watermark: $start"
            )
        }
        return ArrayList(childResults.subList(start, childResults.size))
    }

    /**
     * Records a session-local substitution result.
     */
    fun recordSubstitution(
        key: PlanCacheKey,
        node: CommittedPlanNode,
    ) {
        substitutionMap[key] = node
    }

    /**
     * Looks up a session-local substitution result by full cache key.
     */
    fun findSubstitution(key: PlanCacheKey): CommittedPlanNode? = substitutionMap[key]

    /**
     * Returns the reusable edge uniqueness tracker, reset to empty state.
     */
    internal fun acquireEdgeTracker(): PrimitiveMemberTracker = edgeTracker.apply { reset() }

    /**
     * Returns the reusable entropy uniqueness tracker, reset to empty state.
     */
    internal fun acquireEntropyTracker(): PrimitiveMemberTracker = entropyTracker.apply { reset() }

    /**
     * Two-phase active-path membership check.
     *
     * Algorithm:
     * 1. resolve/assign a dense node id via [NodeIdIndexer]
     * 2. inspect GREY membership via [L1Structures.depthOfNodeId]
     * 3. if absent, push onto the active path
     *
     * Returns:
     * - -1 if this node is not yet on the active path
     * - existing 1-based depth if a cycle is detected
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
     * Minimal deterministic break selector.
     *
     * Current conservative rule:
     * - pick minimum route64 among already-materialized deferred results
     * - otherwise pick minimum route64 among already-materialized substitution results
     *
     * This is intentionally narrow and can be refined by a richer capability-based
     * candidate collector later without changing the external contract.
     */
    fun attemptDeterministicBreak(cycleDepth: Int): CommittedPlanNode? {
        val deferred = childResults.filterIsInstance<DeferredCommittedPlanNode>()
        val minDeferred = deferred.minByOrNull { it.cacheKey.route64 }
        if (minDeferred != null) {
            return minDeferred
        }

        val substitutable = childResults.filterIsInstance<SubstitutionCommittedPlanNode>()
        return substitutable.minByOrNull { it.cacheKey.route64 }
    }

    /**
     * Collects deterministic demotion evidence for diagnostics.
     *
     * Evidence is gathered from the current execution stack and sorted for stability.
     */
    fun collectDemotionEvidence(cycleDepth: Int): List<String> {
        val start = (cycleDepth - 1).coerceAtLeast(0)
        return executionStack
            .subList(start, executionStack.size)
            .map { it.typeReference.toString() }
            .sorted()
    }

    /**
     * Returns the normalization version currently observed by the core.
     *
     * This is session-bound and derived from the pinned configuration.
     */
    fun currentNormalizationVersion(): Long = config.normalizationSpecVersion

    /**
     * Zero-residue cleanup for all worker-local mutable state.
     *
     * Guarantees:
     * - GREY membership is cleared
     * - indexer is reset
     * - substitution map and execution stack are cleared
     * - reusable trackers are reset
     * - frame-local checkpoints are zeroed
     *
     * Monotonic counters are intentionally preserved.
     */
    internal fun resetToCleanState() {
        while (structures.stackPointer > 0) {
            val id = structures.activeStack[--structures.stackPointer]
            structures.depthOfNodeId[id] = 0
        }

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

    /**
     * Delegates to zero-residue cleanup.
     */
    override fun close() {
        resetToCleanState()
    }

    /**
     * Fails closed due to runtime budget exhaustion.
     */
    private fun abort(reason: String): Nothing {
        isAborted = true
        throw FuelExhaustedException(reason)
    }

    internal fun isL2Bypassed(): Boolean = l2Bypassed

    internal fun markL2Bypassed() {
        l2Bypassed = true
    }

    companion object {
        /**
         * Canonical factory for session issuance.
         *
         * Construction is intentionally blocked at the primary constructor to force
         * creation through this explicit issuance path.
         */
        @JvmStatic
        fun issue(config: PlannerSessionConfig): PlannerSession {
            return PlannerSession(config)
        }
    }
}