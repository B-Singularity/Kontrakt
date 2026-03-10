package planning.domain.service

import ir.plan.node.CanonicalPlanNode
import metamodel.domain.dto.MemberFact
import metamodel.domain.dto.MemberOrigin
import metamodel.domain.dto.TypeFactsDTO
import metamodel.domain.vo.TypeReference
import planning.domain.exception.AmbiguousEdgeKeyException
import planning.domain.exception.AmbiguousEntropyTargetKeyException
import planning.domain.exception.CapacityExceededException
import planning.domain.exception.CycleDetectedException
import planning.domain.exception.FaultKind
import planning.domain.exception.InvalidCanonicalKeyComponentException
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.exception.PortContractViolationException
import planning.domain.port.outgoing.CanonicalEdgeKeyProvider
import planning.domain.port.outgoing.CanonicalSignatureProvider
import planning.domain.port.outgoing.EntropyTargetKeyProvider
import planning.domain.port.outgoing.NormalizationEngine
import planning.domain.port.outgoing.TypeFactsProvider
import planning.domain.protocol.CostCenter
import planning.domain.runtime.CommittedPlanNode
import planning.domain.runtime.LocalPlanNode
import planning.domain.session.AllocateFrame
import planning.domain.session.ExpandEdgeFrame
import planning.domain.session.IterateMembersFrame
import planning.domain.session.PlanNodeFrame
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Constitutional planning engine.
 *
 * Guarantees:
 * - no native recursion
 * - explicit frame machine
 * - strict reality defense before key assembly
 * - pre-commit isolation via passive IR -> committed runtime wrapper
 * - definitive semantic budget check at the root
 */
class StructuralPlannerCore private constructor(
    private val factsProvider: TypeFactsProvider,
    private val signatureProvider: CanonicalSignatureProvider,
    private val edgeKeyProvider: CanonicalEdgeKeyProvider,
    private val entropyTargetKeyProvider: EntropyTargetKeyProvider,
    private val normalizationEngine: NormalizationEngine,
    private val interner: PlanInterner,
    private val keyFactory: PlanKeyFactory,
) {

    /**
     * Starts planning from the supplied root type reference.
     *
     * The core owns the root-frame seeding so that execution-frame internals
     * remain encapsulated inside the planning runtime.
     */
    fun plan(
        partitionId: PartitionId,
        rootTypeReference: TypeReference,
        session: PlannerSession,
    ): CommittedPlanNode {
        return try {
            session.startSession()
            session.pushExecutionFrame(
                PlanNodeFrame.issue(rootTypeReference)
            )

            val root = executeDfs(partitionId, session)

            if (root.treeSemanticCostUpperBound > session.config.maxSemanticWorkUnits) {
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
                is PlanNodeFrame -> handlePlanNode(frame, session)
                is IterateMembersFrame -> handleRealityAndUniqueness(frame, session)
                is ExpandEdgeFrame -> handleExpand(frame, session)
                is AllocateFrame -> handleAllocate(partitionId, frame, session)
            }
        }

        return session.getRootResult()
    }

    private fun handlePlanNode(
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
            val breakNode = session.attemptDeterministicBreak(cycleDepth)
            if (breakNode != null) {
                session.completeFrame(frame, breakNode)
                return
            }

            throw CycleDetectedException(
                faultKind = deriveFaultKind(
                    facts = facts,
                    offendingMembers = facts.members,
                    session = session,
                ),
                capabilityDemotions = session.collectDemotionEvidence(cycleDepth),
                truncated = false,
            )
        }

        session.transitionToIterate(frame, facts)
    }

    private fun handleRealityAndUniqueness(
        frame: IterateMembersFrame,
        session: PlannerSession,
    ) {
        val facts = frame.facts
        val edgeTracker = session.acquireEdgeTracker()
        val entropyTracker = session.acquireEntropyTracker()

        for (member in facts.members) {
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
                val offending: List<MemberFact> = listOf(previousEdge, member)

                throw AmbiguousEdgeKeyException(
                    key = edgeKey,
                    faultKind = deriveFaultKind(
                        facts = facts,
                        offendingMembers = offending,
                        session = session,
                    ),
                    evidence = offending.map { "${it.origin}:${it.name}" },
                )
            }
            edgeTracker.mark(edgeKey, member)

            val entropyKey = entropyTargetKeyProvider.deriveEntropyKey(
                member.name,
                member.typeReference,
            )
            val previousEntropy: MemberFact? = entropyTracker.findCollision(entropyKey)
            if (previousEntropy != null) {
                val offending: List<MemberFact> = listOf(previousEntropy, member)

                throw AmbiguousEntropyTargetKeyException(
                    key = entropyKey,
                    faultKind = deriveFaultKind(
                        facts = facts,
                        offendingMembers = offending,
                        session = session,
                    ),
                    evidence = offending.map { "${it.origin}:${it.name}" },
                )
            }
            entropyTracker.mark(entropyKey, member)
        }

        session.transitionToExpand(frame, facts)
    }

    /**
     * Current minimal explicit transition.
     *
     * Replace this with the real member-expansion loop that pushes child plan frames
     * and returns to allocation only after descendants are complete.
     */
    private fun handleExpand(
        frame: ExpandEdgeFrame,
        session: PlannerSession,
    ) {
        val signature = signatureProvider.deriveSignature(frame.facts)
        session.transitionToAllocate(frame, signature)
    }

    private fun handleAllocate(
        partitionId: PartitionId,
        frame: AllocateFrame,
        session: PlannerSession,
    ) {
        val passiveIrNode = assemblePassiveIrNode(frame, frame.facts, session)

        val cacheKey = keyFactory.issue(
            partitionId = partitionId,
            equalityKey = frame.signature,
            session = session,
        )

        val canonicalIrNode = interner.resolve(
            partitionId = partitionId,
            key = cacheKey,
            session = session,
        ) {
            passiveIrNode
        }

        val local = LocalPlanNode.issue(
            irNode = canonicalIrNode,
            children = session.collectChildResults(frame),
        )

        val committed = local.commit(cacheKey)
        val finalResult = session.findSubstitution(cacheKey) ?: committed

        session.recordSubstitution(cacheKey, finalResult)
        session.completeFrame(frame, finalResult)
    }

    /**
     * Passive IR assembly hook.
     *
     * Wire the existing passive IR assembler here.
     * This method intentionally throws a custom exception until that integration exists.
     */
    private fun assemblePassiveIrNode(
        frame: AllocateFrame,
        facts: TypeFactsDTO,
        session: PlannerSession,
    ): CanonicalPlanNode {
        throw PlanningProtocolIntegrityException(
            "Passive IR assembler is not wired."
        )
    }

    private fun deriveFaultKind(
        facts: TypeFactsDTO,
        offendingMembers: List<MemberFact>,
        session: PlannerSession,
    ): FaultKind {
        val declaredOnly = offendingMembers.all { it.origin == MemberOrigin.DECLARED }
        val versionMatch = facts.normalizationVersion == session.currentNormalizationVersion()

        return if (declaredOnly && versionMatch) {
            FaultKind.USER_MODEL_INVALID
        } else {
            FaultKind.FRAMEWORK_INVARIANT_BROKEN
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            factsProvider: TypeFactsProvider,
            signatureProvider: CanonicalSignatureProvider,
            edgeKeyProvider: CanonicalEdgeKeyProvider,
            entropyTargetKeyProvider: EntropyTargetKeyProvider,
            normalizationEngine: NormalizationEngine,
            interner: PlanInterner,
            keyFactory: PlanKeyFactory,
        ): StructuralPlannerCore {
            return StructuralPlannerCore(
                factsProvider = factsProvider,
                signatureProvider = signatureProvider,
                edgeKeyProvider = edgeKeyProvider,
                entropyTargetKeyProvider = entropyTargetKeyProvider,
                normalizationEngine = normalizationEngine,
                interner = interner,
                keyFactory = keyFactory,
            )
        }
    }
}