package planning.domain.service

import metamodel.domain.dto.MemberFact
import metamodel.domain.vo.TypeReference
import planning.domain.exception.AmbiguousEdgeKeyException
import planning.domain.exception.AmbiguousEntropyTargetKeyException
import planning.domain.exception.CapacityExceededException
import planning.domain.exception.CycleDetectedException
import planning.domain.exception.InvalidCanonicalKeyComponentException
import planning.domain.exception.PortContractViolationException
import planning.domain.port.outgoing.ActiveMemberOrderingGate
import planning.domain.port.outgoing.CanonicalEdgeKeyProvider
import planning.domain.port.outgoing.CanonicalSignatureProvider
import planning.domain.port.outgoing.CycleBreakPayloadAssembler
import planning.domain.port.outgoing.CycleEdgeSemanticsProvider
import planning.domain.port.outgoing.EntropyTargetKeyProvider
import planning.domain.port.outgoing.NormalizationEngine
import planning.domain.port.outgoing.PassiveIrAssembler
import planning.domain.port.outgoing.TraversalDisposition
import planning.domain.port.outgoing.TypeFactsProvider
import planning.domain.protocol.CostCenter
import planning.domain.runtime.CommittedPlanNode
import planning.domain.session.AllocateFrame
import planning.domain.session.ExpandEdgeFrame
import planning.domain.session.IterateMembersFrame
import planning.domain.session.PlanNodeFrame
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 *
 * Guarantees:
 * - no native recursion
 * - immutable execution descriptors
 * - rollback-safe session-owned traversal state
 * - active-cycle-segment breakpoint selection
 * - canonical sealing only through the intern boundary
 *
 * Non-responsibilities:
 * - fault-kind policy
 * - committed-node wrapper selection
 * - stage-specific cycle-break defaulting
 */
class StructuralPlannerCore private constructor(
    private val factsProvider: TypeFactsProvider,
    private val orderingGate: ActiveMemberOrderingGate,
    private val signatureProvider: CanonicalSignatureProvider,
    private val edgeKeyProvider: CanonicalEdgeKeyProvider,
    private val entropyTargetKeyProvider: EntropyTargetKeyProvider,
    private val normalizationEngine: NormalizationEngine,
    private val edgeSemanticsProvider: CycleEdgeSemanticsProvider,
    private val passiveIrAssembler: PassiveIrAssembler,
    private val cycleBreakPayloadAssembler: CycleBreakPayloadAssembler,
    private val faultKindResolver: FaultKindResolver,
    private val committedPlanNodeFactory: CommittedPlanNodeFactory,
    private val interner: PlanInterner,
    private val keyFactory: PlanKeyFactory,
) {

    fun plan(
        partitionId: PartitionId,
        rootTypeReference: TypeReference,
        session: PlannerSession,
    ): CommittedPlanNode {
        return try {
            session.startSession()
            session.pushExecutionFrame(PlanNodeFrame.issue(rootTypeReference))

            val root = executeDfs(partitionId, session)

            if (root.treeSemanticCostUpperBound > session.config.budget.maxSemanticWorkUnits.toLong()) {
                throw CapacityExceededException(
                    limitType = "SEMANTIC_BUDGET",
                    value = root.treeSemanticCostUpperBound,
                )
            }

            root
        } finally {
            session.resetToCleanState()
        }
    }

    private fun executeDfs(
        partitionId: PartitionId,
        session: PlannerSession,
    ): CommittedPlanNode {
        while (session.hasActiveFrames()) {
            session.step(CostCenter.FRAME_DISPATCH)

            when (val frame = session.peekExecutionFrame()) {
                is PlanNodeFrame -> handlePlanNode(partitionId, frame, session)
                is IterateMembersFrame -> handleRealityAndUniqueness(frame, session)
                is ExpandEdgeFrame -> handleExpand(frame, session)
                is AllocateFrame -> handleAllocate(partitionId, frame, session)
            }
        }

        return session.getRootResult()
    }

    private fun handlePlanNode(
        partitionId: PartitionId,
        frame: PlanNodeFrame,
        session: PlannerSession,
    ) {
        val facts = factsProvider.resolveFacts(frame.typeReference)
        val signature = signatureProvider.deriveSignature(facts)

        val cycleDepth = session.enterOrDetectCycle(
            identityBits = facts.nodeIdentity64,
            signature = signature,
        )

        if (cycleDepth != -1) {
            val decision = session.attemptDeterministicBreak(
                cycleDepth = cycleDepth,
                backEdge = frame,
            )

            if (decision != null) {
                val owner = session.resolveBreakpointOwnerFacts(decision)
                val member = session.resolveBreakpointMember(decision)
                val assembly = cycleBreakPayloadAssembler.assemble(
                    ownerFacts = owner.ownerFacts(),
                    member = member,
                    stage = decision.stage,
                )

                val cacheKey = keyFactory.issue(
                    partitionId = partitionId,
                    equalityKey = assembly.equalityKey,
                    session = session,
                )

                val canonical = interner.resolveCycleBreak(
                    partitionId = partitionId,
                    key = cacheKey,
                    session = session,
                ) {
                    assembly.payload
                }

                val committed = committedPlanNodeFactory.createCycleBreak(
                    irNode = canonical,
                    cacheKey = cacheKey,
                    assembly = assembly,
                )

                session.recordSubstitution(cacheKey, committed)
                session.completeFrame(frame, committed)
                return
            }

            throw CycleDetectedException(
                faultKind = faultKindResolver.resolveForCollision(
                    facts = facts,
                    offendingMembers = facts.members,
                    expectedNormalizationVersion = session.currentNormalizationVersion(),
                ),
                capabilityDemotions = session.collectDemotionEvidence(cycleDepth),
                truncated = false,
            )
        }

        session.bindIncomingEdgeAtCurrentDepth(frame)
        val ordered = orderingGate.ratify(facts)
        session.transitionToIterate(frame, ordered)
    }

    private fun handleRealityAndUniqueness(
        frame: IterateMembersFrame,
        session: PlannerSession,
    ) {
        val ordered = frame.orderedMembers
        val facts = ordered.ownerFacts()

        val edgeTracker = session.acquireEdgeTracker()
        val entropyTracker = session.acquireEntropyTracker()

        var idx = 0
        while (idx < ordered.size()) {
            val member = ordered.memberAt(idx)

            if (member.name.contains('|')) {
                throw InvalidCanonicalKeyComponentException(
                    component = member.name,
                    reason = "Reserved delimiter '|' detected."
                )
            }

            if (!normalizationEngine.isNfc(member.name)) {
                throw PortContractViolationException(
                    "Non-NFC component provided by Port: '${member.name}'"
                )
            }

            val edgeKey = edgeKeyProvider.deriveEdgeKey(member.name, member.origin)
            val previousEdge: MemberFact? = edgeTracker.findCollision(edgeKey)
            if (previousEdge != null) {
                val offending = ArrayList<MemberFact>(2)
                offending.add(previousEdge)
                offending.add(member)

                throw AmbiguousEdgeKeyException(
                    key = edgeKey,
                    faultKind = faultKindResolver.resolveForCollision(
                        facts = facts,
                        offendingMembers = offending,
                        expectedNormalizationVersion = session.currentNormalizationVersion(),
                    ),
                    evidence = listOf(
                        "${previousEdge.origin}:${previousEdge.name}",
                        "${member.origin}:${member.name}",
                    ),
                )
            }
            edgeTracker.mark(edgeKey, member)

            val entropyKey = entropyTargetKeyProvider.deriveEntropyKey(
                member.name,
                member.typeReference,
            )
            val previousEntropy: MemberFact? = entropyTracker.findCollision(entropyKey)
            if (previousEntropy != null) {
                val offending = ArrayList<MemberFact>(2)
                offending.add(previousEntropy)
                offending.add(member)

                throw AmbiguousEntropyTargetKeyException(
                    key = entropyKey,
                    faultKind = faultKindResolver.resolveForCollision(
                        facts = facts,
                        offendingMembers = offending,
                        expectedNormalizationVersion = session.currentNormalizationVersion(),
                    ),
                    evidence = listOf(
                        "${previousEntropy.origin}:${previousEntropy.name}",
                        "${member.origin}:${member.name}",
                    ),
                )
            }
            entropyTracker.mark(entropyKey, member)
            idx++
        }

        session.transitionToExpand(frame)
    }

    /**
     * Final-form explicit expansion loop.
     *
     * One dispatch consumes at most one protocol-ordered member.
     */
    private fun handleExpand(
        frame: ExpandEdgeFrame,
        session: PlannerSession,
    ) {
        if (!session.hasMoreMembers(frame)) {
            val signature = signatureProvider.deriveSignature(frame.orderedMembers.ownerFacts())
            session.transitionToAllocate(frame, signature)
            return
        }

        session.step(CostCenter.EDGE_EXPAND)

        val memberIndex = session.consumeNextMemberIndex(frame)
        val member = frame.orderedMembers.memberAt(memberIndex)
        val edge = edgeSemanticsProvider.describe(frame.orderedMembers.ownerFacts(), member)

        if (edge.traversalDisposition == TraversalDisposition.SKIP) {
            return
        }

        session.pushExecutionFrame(
            PlanNodeFrame.issue(
                typeReference = member.typeReference,
                incomingEdgeRank = edge.edgeRank,
                incomingEdgeStageTag = edge.breakpointStage.tag,
                incomingExpandExecutionIndex = session.currentExecutionIndex(),
                incomingMemberIndex = memberIndex,
            )
        )
    }

    private fun handleAllocate(
        partitionId: PartitionId,
        frame: AllocateFrame,
        session: PlannerSession,
    ) {
        val assembly = passiveIrAssembler.assemble(
            facts = frame.orderedMembers.ownerFacts(),
            children = session.bindChildDescriptorCursor(frame),
        )

        val cacheKey = keyFactory.issue(
            partitionId = partitionId,
            equalityKey = assembly.equalityKey,
            session = session,
        )

        val canonicalIrNode = interner.resolve(
            partitionId = partitionId,
            key = cacheKey,
            session = session,
        ) {
            assembly.payload
        }

        val totalSemanticCost =
            assembly.selfSemanticCostUpperBound + session.collectChildSemanticCost(frame)

        val committed = committedPlanNodeFactory.createFinal(
            irNode = canonicalIrNode,
            cacheKey = cacheKey,
            treeSemanticCostUpperBound = totalSemanticCost,
        )

        val finalResult = session.findSubstitution(cacheKey) ?: committed
        session.recordSubstitution(cacheKey, finalResult)
        session.completeFrame(frame, finalResult)
    }

    companion object {
        @JvmStatic
        fun issue(
            factsProvider: TypeFactsProvider,
            orderingGate: ActiveMemberOrderingGate,
            signatureProvider: CanonicalSignatureProvider,
            edgeKeyProvider: CanonicalEdgeKeyProvider,
            entropyTargetKeyProvider: EntropyTargetKeyProvider,
            normalizationEngine: NormalizationEngine,
            edgeSemanticsProvider: CycleEdgeSemanticsProvider,
            passiveIrAssembler: PassiveIrAssembler,
            cycleBreakPayloadAssembler: CycleBreakPayloadAssembler,
            faultKindResolver: FaultKindResolver,
            committedPlanNodeFactory: CommittedPlanNodeFactory,
            interner: PlanInterner,
            keyFactory: PlanKeyFactory,
        ): StructuralPlannerCore {
            return StructuralPlannerCore(
                factsProvider = factsProvider,
                orderingGate = orderingGate,
                signatureProvider = signatureProvider,
                edgeKeyProvider = edgeKeyProvider,
                entropyTargetKeyProvider = entropyTargetKeyProvider,
                normalizationEngine = normalizationEngine,
                edgeSemanticsProvider = edgeSemanticsProvider,
                passiveIrAssembler = passiveIrAssembler,
                cycleBreakPayloadAssembler = cycleBreakPayloadAssembler,
                faultKindResolver = faultKindResolver,
                committedPlanNodeFactory = committedPlanNodeFactory,
                interner = interner,
                keyFactory = keyFactory,
            )
        }
    }
}