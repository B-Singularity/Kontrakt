package planning.domain.service

import governance.budget.CostCenter
import metamodel.port.outgoing.NormalizationEngine
import stage.lowering.diagnostics.ActiveCycleWithoutBreakpointException
import stage.lowering.diagnostics.AmbiguousEdgeKeyException
import stage.lowering.diagnostics.AmbiguousEntropyTargetKeyException
import stage.lowering.diagnostics.CapacityExceededException
import stage.lowering.diagnostics.InvalidCanonicalKeyComponentException
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import stage.lowering.diagnostics.PortContractViolationException
import stage.lowering.diagnostics.UnsupportedTypeExpansionException
import stage.lowering.material.expansion.SessionTypeExpansionWorkMeter
import stage.lowering.material.expansion.TypeExpansionPipeline
import stage.lowering.material.expansion.TypeExpansionPreflightDecision
import planning.domain.interner.InternerInvocationSite
import planning.domain.interner.InternerStepResult
import planning.domain.interner.PlanInterner
import planning.domain.interner.PlanKeyFactory
import stage.lowering.material.projection.CapabilityProfile
import stage.lowering.contract.TraversalDisposition
import planning.domain.runtime.CommittedPlanNode
import planning.domain.service.assembly.CycleBreakPayloadAssembler
import planning.domain.service.assembly.PassiveIrAssembler
import planning.domain.service.derivation.CanonicalEdgeKeyProvider
import planning.domain.service.derivation.CanonicalSignatureProvider
import planning.domain.service.derivation.CycleEdgeSemanticsProvider
import planning.domain.service.derivation.EntropyTargetKeyProvider
import planning.domain.session.AllocateFrame
import planning.domain.session.ExpandEdgeFrame
import planning.domain.session.IterateMembersFrame
import planning.domain.session.PlanNodeFrame
import planning.domain.session.PlannerSession
import stage.lowering.material.PartitionId
import stage.canonicalization.material.TypeReference
import stage.input.material.MemberFact
import stage.lowering.material.CanonicalPlanNode

/**
 * Compiler-style structural planner core.
 *
 * ADR-0037 position:
 * - active-cycle detection is identity-first
 * - raw facts are fact-lazy
 * - raw facts / projection / ordering are skipped on cycle-hit paths
 *
 * This core owns orchestration order, not metamodel extraction.
 *
 * Non-responsibilities:
 * - reflection/KSP/bytecode details
 * - raw type-fact extraction
 * - active-member projection internals
 * - active-member ordering internals
 * - runtime joined-wait suspension orchestration
 */
class StructuralPlannerCore private constructor(
    private val typeExpansionPipeline: TypeExpansionPipeline,
    private val signatureProvider: CanonicalSignatureProvider,
    private val edgeKeyProvider: CanonicalEdgeKeyProvider,
    private val entropyTargetKeyProvider: EntropyTargetKeyProvider,
    private val normalizationEngine: NormalizationEngine,
    private val edgeSemanticsProvider: CycleEdgeSemanticsProvider,
    private val passiveIrAssembler: PassiveIrAssembler,
    private val cycleBreakPayloadAssembler: CycleBreakPayloadAssembler,
    private val committedPlanNodeFactory: CommittedPlanNodeFactory,
    private val interner: PlanInterner,
    private val keyFactory: PlanKeyFactory,
) {
    fun plan(
        partitionId: PartitionId,
        rootTypeReference: TypeReference,
        capabilityProfile: CapabilityProfile,
        session: PlannerSession,
    ): CommittedPlanNode =
        try {
            session.startSession()
            session.pushExecutionFrame(PlanNodeFrame.issue(rootTypeReference))

            val workMeter = SessionTypeExpansionWorkMeter.issue(session)

            val root =
                executeDfs(
                    partitionId = partitionId,
                    capabilityProfile = capabilityProfile,
                    session = session,
                    workMeter = workMeter,
                )

            if (root.treeSemanticCostUpperBound >
                session.config.budget.maxSemanticWorkUnits
                    .toLong()
            ) {
                throw CapacityExceededException(
                    limitType = "SEMANTIC_BUDGET",
                    value = root.treeSemanticCostUpperBound,
                )
            }

            root
        } finally {
            session.resetToCleanState()
        }

    private fun executeDfs(
        partitionId: PartitionId,
        capabilityProfile: CapabilityProfile,
        session: PlannerSession,
        workMeter: SessionTypeExpansionWorkMeter,
    ): CommittedPlanNode {
        while (session.hasActiveFrames()) {
            session.step(CostCenter.FRAME_DISPATCH)

            when (val frame = session.peekExecutionFrame()) {
                is PlanNodeFrame -> {
                    handlePlanNode(
                        partitionId = partitionId,
                        capabilityProfile = capabilityProfile,
                        frame = frame,
                        session = session,
                        workMeter = workMeter,
                    )
                }

                is IterateMembersFrame -> {
                    handleRealityAndUniqueness(
                        frame = frame,
                        session = session,
                    )
                }

                is ExpandEdgeFrame -> {
                    handleExpand(
                        frame = frame,
                        session = session,
                    )
                }

                is AllocateFrame -> {
                    handleAllocate(
                        partitionId = partitionId,
                        frame = frame,
                        session = session,
                    )
                }
            }
        }

        return session.getRootResult()
    }

    private fun handlePlanNode(
        partitionId: PartitionId,
        capabilityProfile: CapabilityProfile,
        frame: PlanNodeFrame,
        session: PlannerSession,
        workMeter: SessionTypeExpansionWorkMeter,
    ) {
        val preflight =
            typeExpansionPipeline.preparePreflight(
                reference = frame.typeReference,
                workMeter = workMeter,
            )

        val cycleDepth =
            session.enterOrDetectCycle(
                identityBits = preflight.cycleIdentity.identityBits64,
                signature = preflight.cycleIdentity.canonicalSignature,
            )

        if (cycleDepth != -1) {
            completeCycleHit(
                partitionId = partitionId,
                frame = frame,
                preflight = preflight,
                cycleDepth = cycleDepth,
                session = session,
            )
            return
        }

        session.bindIncomingEdgeAtCurrentDepth(frame)

        when (preflight) {
            is TypeExpansionPreflightDecision.CompositePreflight -> {
                val decision =
                    typeExpansionPipeline.prepareCompositeExpansion(
                        preflight = preflight,
                        capabilityProfile = capabilityProfile,
                        workMeter = workMeter,
                    )

                session.transitionToIterate(
                    frame = frame,
                    orderedMembers = decision.plan.orderedMembers,
                )
            }

            is TypeExpansionPreflightDecision.AtomicPreflight -> {
                throw UnsupportedTypeExpansionException(
                    subjectTypeId = preflight.subject.id,
                    shapeKind = "ATOMIC",
                    reason = "Atomic leaf/generator frame is not implemented in the current StructuralPlannerCore phase.",
                )
            }

            is TypeExpansionPreflightDecision.CollectionPreflight -> {
                throw UnsupportedTypeExpansionException(
                    subjectTypeId = preflight.subject.id,
                    shapeKind = "COLLECTION",
                    reason = "Collection expansion frame is not implemented in the current StructuralPlannerCore phase.",
                )
            }

            is TypeExpansionPreflightDecision.ArrayPreflight -> {
                throw UnsupportedTypeExpansionException(
                    subjectTypeId = preflight.subject.id,
                    shapeKind = "ARRAY",
                    reason = "Array expansion frame is not implemented in the current StructuralPlannerCore phase.",
                )
            }

            is TypeExpansionPreflightDecision.MapPreflight -> {
                throw UnsupportedTypeExpansionException(
                    subjectTypeId = preflight.subject.id,
                    shapeKind = "MAP",
                    reason = "Map expansion frame is not implemented in the current StructuralPlannerCore phase.",
                )
            }
        }
    }

    private fun completeCycleHit(
        partitionId: PartitionId,
        frame: PlanNodeFrame,
        preflight: TypeExpansionPreflightDecision,
        cycleDepth: Int,
        session: PlannerSession,
    ) {
        val decision =
            session.attemptDeterministicBreak(
                cycleDepth = cycleDepth,
                backEdge = frame,
            )

        if (decision == null) {
            /*
             * ADR-0037 forbids resolving raw facts merely to diagnose the current
             * cycle-hit type. Fail closed with identity-level evidence.
             */
            throw ActiveCycleWithoutBreakpointException(
                subjectTypeId = preflight.subject.id,
                cycleDepth = cycleDepth,
                identityAlgorithmId = preflight.cycleIdentity.identityAlgorithmId,
                identityAlgorithmVersion = preflight.cycleIdentity.identityAlgorithmVersion,
            )
        }

        val owner = session.resolveBreakpointOwnerFacts(decision)
        val member = session.resolveBreakpointMember(decision)

        val assembly =
            cycleBreakPayloadAssembler.assemble(
                ownerFacts = owner.ownerFacts(),
                member = member,
                stage = decision.stage,
            )

        val cacheKey =
            keyFactory.issue(
                partitionId = partitionId,
                equalityKey = assembly.equalityKey,
                session = session,
            )

        val canonical =
            requireImmediateInternerCompletion(
                result =
                    interner.resolveCycleBreak(
                        partitionId = partitionId,
                        key = cacheKey,
                        session = session,
                        rawPayload = assembly.payload,
                    ),
                site = InternerInvocationSite.CYCLE_BREAK,
            )

        val committed =
            committedPlanNodeFactory.createCycleBreak(
                irNode = canonical,
                cacheKey = cacheKey,
                assembly = assembly,
            )

        session.recordSubstitution(cacheKey, committed)
        session.completeFrame(frame, committed)
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
                    reason = "Reserved delimiter '|' detected.",
                )
            }

            if (!normalizationEngine.isNfc(member.name)) {
                throw PortContractViolationException(
                    "Non-NFC component provided by Port: '${member.name}'",
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
                    faultKind =
                        resolveCollisionFaultKind(
                            facts = facts,
                            offendingMembers = offending,
                            session = session,
                        ),
                    evidence =
                        listOf(
                            "${previousEdge.origin}:${previousEdge.name}",
                            "${member.origin}:${member.name}",
                        ),
                )
            }
            edgeTracker.mark(edgeKey, member)

            val entropyKey =
                entropyTargetKeyProvider.deriveEntropyKey(
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
                    faultKind =
                        resolveCollisionFaultKind(
                            facts = facts,
                            offendingMembers = offending,
                            session = session,
                        ),
                    evidence =
                        listOf(
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
            ),
        )
    }

    private fun handleAllocate(
        partitionId: PartitionId,
        frame: AllocateFrame,
        session: PlannerSession,
    ) {
        val assembly =
            passiveIrAssembler.assemble(
                facts = frame.orderedMembers.ownerFacts(),
                children = session.bindChildDescriptorCursor(frame),
            )

        val cacheKey =
            keyFactory.issue(
                partitionId = partitionId,
                equalityKey = assembly.equalityKey,
                session = session,
            )

        val canonicalIrNode =
            requireImmediateInternerCompletion(
                result =
                    interner.resolve(
                        partitionId = partitionId,
                        key = cacheKey,
                        session = session,
                        rawPayload = assembly.payload,
                    ),
                site = InternerInvocationSite.ORDINARY_PAYLOAD,
            )

        val totalSemanticCost =
            assembly.selfSemanticCostUpperBound + session.collectChildSemanticCost(frame)

        val committed =
            committedPlanNodeFactory.createFinal(
                irNode = canonicalIrNode,
                cacheKey = cacheKey,
                treeSemanticCostUpperBound = totalSemanticCost,
            )

        val finalResult = session.findSubstitution(cacheKey) ?: committed
        session.recordSubstitution(cacheKey, finalResult)
        session.completeFrame(frame, finalResult)
    }

    /**
     * Transitional bridge.
     *
     * Existing collision fault attribution still uses the prior resolver contract.
     * This method exists so future identity-only diagnostics do not reintroduce
     * raw-fact resolution on cycle-hit paths.
     */
    private fun resolveCollisionFaultKind(
        facts: Any,
        offendingMembers: List<MemberFact>,
        session: PlannerSession,
    ): planning.domain.protocol.PlanningFaultKind {
        /*
         * Keep the old resolver call here if FaultKindResolver still exists.
         * If the old resolver type remains in your codebase, inject it and replace
         * this body with:
         *
         * faultKindResolver.resolveForCollision(
         *     facts = facts,
         *     offendingMembers = offendingMembers,
         *     expectedNormalizationVersion = session.currentNormalizationVersion(),
         * )
         *
         * This placeholder prevents this refactor from using raw facts on cycle-hit
         * paths while preserving the collision call-site shape.
         */
        throw InvalidTypeFactShapeException(
            owner = "StructuralPlannerCore",
            factKind = "FaultKindResolver",
            reason = "Collision fault resolver must be reconnected after TypeFactsDTO -> RawTypeFactsDTO migration.",
        )
    }

    private fun requireImmediateInternerCompletion(
        result: InternerStepResult,
        site: InternerInvocationSite,
    ): CanonicalPlanNode =
        when (result) {
            is InternerStepResult.Completed -> result.node

            is InternerStepResult.SuspendedOnJoin -> {
                throw PlanningProtocolIntegrityException(
                    "StructuralPlannerCore encountered InternerStepResult.SuspendedOnJoin during ${site.diagnosticLabel} " +
                            "before runtime-boundary orchestration uplift. Phase 7 must route this through " +
                            "PlanningRunContext / PlanningRunJoinBridge instead of forcing immediate completion.",
                )
            }
        }

    companion object {
        @JvmStatic
        fun issue(
            typeExpansionPipeline: TypeExpansionPipeline,
            signatureProvider: CanonicalSignatureProvider,
            edgeKeyProvider: CanonicalEdgeKeyProvider,
            entropyTargetKeyProvider: EntropyTargetKeyProvider,
            normalizationEngine: NormalizationEngine,
            edgeSemanticsProvider: CycleEdgeSemanticsProvider,
            passiveIrAssembler: PassiveIrAssembler,
            cycleBreakPayloadAssembler: CycleBreakPayloadAssembler,
            committedPlanNodeFactory: CommittedPlanNodeFactory,
            interner: PlanInterner,
            keyFactory: PlanKeyFactory,
        ): StructuralPlannerCore =
            StructuralPlannerCore(
                typeExpansionPipeline = typeExpansionPipeline,
                signatureProvider = signatureProvider,
                edgeKeyProvider = edgeKeyProvider,
                entropyTargetKeyProvider = entropyTargetKeyProvider,
                normalizationEngine = normalizationEngine,
                edgeSemanticsProvider = edgeSemanticsProvider,
                passiveIrAssembler = passiveIrAssembler,
                cycleBreakPayloadAssembler = cycleBreakPayloadAssembler,
                committedPlanNodeFactory = committedPlanNodeFactory,
                interner = interner,
                keyFactory = keyFactory,
            )
    }
}
