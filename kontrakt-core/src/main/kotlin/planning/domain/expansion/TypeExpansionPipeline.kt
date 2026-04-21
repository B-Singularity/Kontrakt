package planning.domain.expansion

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.service.TypeIdentity64Deriver
import metamodel.domain.vo.TypeKind
import metamodel.domain.vo.TypeReference
import planning.domain.exception.CorruptResolvedTypeShapeException
import planning.domain.exception.PlanningExpansionException
import planning.domain.exception.RawTypeFactsSubjectMismatchException
import planning.domain.exception.TypeShapeSubjectMismatchException
import planning.domain.exception.UnsupportedTypeExpansionException
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.RawTypeFactsResolutionKind
import planning.domain.port.outgoing.TypeShapeProvider
import planning.domain.projection.ActiveMemberOrderer
import planning.domain.projection.ActiveMemberProjector
import planning.domain.projection.CapabilityProfile

/**
 * Domain service that prepares type expansion decisions for StructuralPlannerCore.
 *
 * DDD role:
 * - Planning domain service.
 * - Owns type expansion dispatch rules.
 *
 * Hexagonal role:
 * - Depends on outbound ports TypeShapeProvider and RawTypeFactsProvider.
 * - Does not know whether facts come from reflection, KSP, bytecode, or static source.
 *
 * Compiler-style role:
 * - Performs staged lowering:
 *
 *   TypeReference
 *   -> ResolvedTypeShape
 *   -> TypeExpansionDecision
 *
 * Boundary rule:
 * - Does not depend on PlannerSession.
 * - Does not mutate session counters directly.
 * - Emits TypeExpansionWorkEvent through the caller-provided meter.
 *
 * Accounting rule:
 * - Work events are recorded after the corresponding stage succeeds.
 * - If a later stage fails, already-recorded successful work remains consumed.
 * - Rollback/reset must not rewind physical or semantic metering counters.
 * - Failure-path accounting must be added through explicit failure/fault cost centers,
 *   not by pre-charging successful-stage events.
 *
 * Identity rule:
 * - The injected TypeIdentity64Deriver must be resolved before the session/run starts.
 * - Its algorithm id/version are snapshotted at pipeline creation.
 * - Each prepareExpansion call verifies that the deriver has not drifted.
 * - The pipeline independently derives the expected identity to verify adapter output.
 */
class TypeExpansionPipeline private constructor(
    private val typeShapeProvider: TypeShapeProvider,
    private val rawTypeFactsProvider: RawTypeFactsProvider,
    private val typeIdentity64Deriver: TypeIdentity64Deriver,
    private val identityAlgorithmIdSnapshot: String,
    private val identityAlgorithmVersionSnapshot: Long,
    private val activeMemberProjector: ActiveMemberProjector,
    private val activeMemberOrderer: ActiveMemberOrderer,
) {
    fun prepareExpansion(
        reference: TypeReference,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision {
        requireIdentityDeriverStable()

        val shape = typeShapeProvider.resolveTypeShape(reference)

        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_SHAPE_RESOLUTION,
            subject = reference,
        )

        requireShapeSubjectMatchesReference(
            expected = reference,
            shape = shape,
        )

        return lowerShapeToDecision(
            reference = reference,
            shape = shape,
            capabilityProfile = capabilityProfile,
            workMeter = workMeter,
        )
    }

    private fun lowerShapeToDecision(
        reference: TypeReference,
        shape: ResolvedTypeShape,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision {
        return when (shape.kind) {
            TypeKind.ATOMIC -> {
                val decision = TypeExpansionDecision.AtomicExpansion.issue(
                    subject = reference,
                )

                recordShapeLoweringAndDecision(
                    workMeter = workMeter,
                    reference = reference,
                    decisionEvent = TypeExpansionWorkEvent.ATOMIC_EXPANSION_DECISION,
                )

                decision
            }

            TypeKind.COMPOSITE -> {
                val decision = prepareCompositeExpansion(
                    reference = reference,
                    capabilityProfile = capabilityProfile,
                    workMeter = workMeter,
                )

                /*
                 * COMPOSITE lowering is recorded only after the composite decision is
                 * fully materialized.
                 *
                 * Unlike container/atomic shapes, a composite decision requires raw
                 * fact retrieval, subject-continuity verification, projection, ordering,
                 * and CompositeExpansionPlan issuance before the decision is usable.
                 */
                workMeter.record(
                    event = TypeExpansionWorkEvent.TYPE_SHAPE_LOWERING,
                    subject = reference,
                )

                decision
            }

            TypeKind.COLLECTION -> {
                val elementType = requireElementType(shape)

                val decision = TypeExpansionDecision.CollectionExpansion.issue(
                    subject = reference,
                    elementType = elementType,
                )

                recordShapeLoweringAndDecision(
                    workMeter = workMeter,
                    reference = reference,
                    decisionEvent = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                )

                decision
            }

            TypeKind.ARRAY -> {
                val componentType = requireComponentType(shape)

                val decision = TypeExpansionDecision.ArrayExpansion.issue(
                    subject = reference,
                    componentType = componentType,
                )

                recordShapeLoweringAndDecision(
                    workMeter = workMeter,
                    reference = reference,
                    decisionEvent = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                )

                decision
            }

            TypeKind.MAP -> {
                val keyType = requireKeyType(shape)
                val valueType = requireValueType(shape)

                val decision = TypeExpansionDecision.MapExpansion.issue(
                    subject = reference,
                    keyType = keyType,
                    valueType = valueType,
                )

                recordShapeLoweringAndDecision(
                    workMeter = workMeter,
                    reference = reference,
                    decisionEvent = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                )

                decision
            }

            TypeKind.INTERFACE -> {
                /*
                 * Do not record TYPE_SHAPE_LOWERING here.
                 * There is no implemented interface-resolution path yet.
                 */
                throw UnsupportedTypeExpansionException(
                    subjectTypeId = reference.id,
                    shapeKind = shape.kind.name,
                    reason = "Interface implementation-resolution policy is not yet implemented.",
                )
            }
        }
    }

    private fun recordShapeLoweringAndDecision(
        workMeter: TypeExpansionWorkMeter,
        reference: TypeReference,
        decisionEvent: TypeExpansionWorkEvent,
    ) {
        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_SHAPE_LOWERING,
            subject = reference,
        )

        workMeter.record(
            event = decisionEvent,
            subject = reference,
        )
    }

    private fun prepareCompositeExpansion(
        reference: TypeReference,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision.CompositeExpansion {
        val rawResolution = rawTypeFactsProvider.resolveRawFacts(reference)

        workMeter.record(
            event = rawFactsResolutionEvent(rawResolution.kind),
            subject = reference,
        )

        val rawFacts = rawResolution.facts
        val expectedTypeIdentity64 = typeIdentity64Deriver.deriveIdentity64(reference)

        requireRawFactsSubjectMatchesReference(
            reference = reference,
            rawFacts = rawFacts,
            expectedTypeIdentity64 = expectedTypeIdentity64,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK,
            subject = reference,
        )

        val projection = activeMemberProjector.project(
            facts = rawFacts,
            capabilityProfile = capabilityProfile,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_PROJECTION,
            subject = reference,
        )

        val orderedMembers = activeMemberOrderer.order(projection)

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_ORDERING,
            subject = reference,
        )

        val plan = CompositeExpansionPlan.issue(
            ownerTypeFqcn = rawFacts.ownerTypeFqcn,
            typeIdentity64 = rawFacts.typeIdentity64,
            selectedConstructor = projection.selectedConstructor,
            orderedMembers = orderedMembers,
            propertyDemotions = projection.propertyDemotions,
        )

        return TypeExpansionDecision.CompositeExpansion.issue(
            subject = reference,
            plan = plan,
        )
    }

    private fun rawFactsResolutionEvent(
        kind: RawTypeFactsResolutionKind,
    ): TypeExpansionWorkEvent {
        return when (kind) {
            RawTypeFactsResolutionKind.CACHE_HIT ->
                TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_CACHE_HIT

            RawTypeFactsResolutionKind.ACTUAL_RESOLUTION ->
                TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_RESOLVE
        }
    }

    private fun requireRawFactsSubjectMatchesReference(
        reference: TypeReference,
        rawFacts: RawTypeFactsDTO,
        expectedTypeIdentity64: Long,
    ) {
        val identityMismatch = rawFacts.typeIdentity64 != expectedTypeIdentity64
        val algorithmIdMismatch = rawFacts.typeIdentityAlgorithmId != identityAlgorithmIdSnapshot
        val algorithmVersionMismatch =
            rawFacts.typeIdentityAlgorithmVersion != identityAlgorithmVersionSnapshot

        if (identityMismatch || algorithmIdMismatch || algorithmVersionMismatch) {
            throw RawTypeFactsSubjectMismatchException(
                expectedTypeId = reference.id,
                expectedSignature = reference.signature,
                expectedCycleId = reference.cycleId,
                expectedTypeIdentity64 = expectedTypeIdentity64,
                actualOwnerTypeFqcn = rawFacts.ownerTypeFqcn,
                actualTypeIdentity64 = rawFacts.typeIdentity64,
                expectedAlgorithmId = identityAlgorithmIdSnapshot,
                actualAlgorithmId = rawFacts.typeIdentityAlgorithmId,
                expectedAlgorithmVersion = identityAlgorithmVersionSnapshot,
                actualAlgorithmVersion = rawFacts.typeIdentityAlgorithmVersion,
                mismatchFields = renderRawFactMismatchFields(
                    identityMismatch = identityMismatch,
                    algorithmIdMismatch = algorithmIdMismatch,
                    algorithmVersionMismatch = algorithmVersionMismatch,
                ),
            )
        }
    }

    private fun requireShapeSubjectMatchesReference(
        expected: TypeReference,
        shape: ResolvedTypeShape,
    ) {
        val idMismatch = shape.subject.id != expected.id
        val signatureMismatch = shape.subject.signature != expected.signature
        val cycleIdMismatch = shape.subject.cycleId != expected.cycleId

        if (idMismatch || signatureMismatch || cycleIdMismatch) {
            throw TypeShapeSubjectMismatchException(
                expectedTypeId = expected.id,
                actualTypeId = shape.subject.id,
                expectedSignature = expected.signature,
                actualSignature = shape.subject.signature,
                expectedCycleId = expected.cycleId,
                actualCycleId = shape.subject.cycleId,
                mismatchFields = renderShapeMismatchFields(
                    idMismatch = idMismatch,
                    signatureMismatch = signatureMismatch,
                    cycleIdMismatch = cycleIdMismatch,
                ),
            )
        }
    }

    private fun renderShapeMismatchFields(
        idMismatch: Boolean,
        signatureMismatch: Boolean,
        cycleIdMismatch: Boolean,
    ): String {
        val fields = arrayOfNulls<String>(3)
        var count = 0

        if (idMismatch) {
            fields[count] = "id"
            count++
        }

        if (signatureMismatch) {
            fields[count] = "signature"
            count++
        }

        if (cycleIdMismatch) {
            fields[count] = "cycleId"
            count++
        }

        return renderNonEmptyMismatchFields(fields, count)
    }

    private fun renderRawFactMismatchFields(
        identityMismatch: Boolean,
        algorithmIdMismatch: Boolean,
        algorithmVersionMismatch: Boolean,
    ): String {
        val fields = arrayOfNulls<String>(3)
        var count = 0

        if (identityMismatch) {
            fields[count] = "typeIdentity64"
            count++
        }

        if (algorithmIdMismatch) {
            fields[count] = "typeIdentityAlgorithmId"
            count++
        }

        if (algorithmVersionMismatch) {
            fields[count] = "typeIdentityAlgorithmVersion"
            count++
        }

        return renderNonEmptyMismatchFields(fields, count)
    }

    private fun renderNonEmptyMismatchFields(
        fields: Array<String?>,
        count: Int,
    ): String {
        if (count == 0) {
            return "unknown"
        }

        val builder = StringBuilder()

        var i = 0
        while (i < count) {
            if (i > 0) {
                builder.append(',')
            }

            builder.append(fields[i])
            i++
        }

        return builder.toString()
    }

    private fun requireElementType(
        shape: ResolvedTypeShape,
    ): TypeReference {
        return shape.elementType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "COLLECTION shape is missing elementType after shape cardinality validation.",
            )
    }

    private fun requireComponentType(
        shape: ResolvedTypeShape,
    ): TypeReference {
        return shape.componentType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "ARRAY shape is missing componentType after shape cardinality validation.",
            )
    }

    private fun requireKeyType(
        shape: ResolvedTypeShape,
    ): TypeReference {
        return shape.keyType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "MAP shape is missing keyType after shape cardinality validation.",
            )
    }

    private fun requireValueType(
        shape: ResolvedTypeShape,
    ): TypeReference {
        return shape.valueType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "MAP shape is missing valueType after shape cardinality validation.",
            )
    }

    private fun requireIdentityDeriverStable() {
        if (typeIdentity64Deriver.identityAlgorithmId != identityAlgorithmIdSnapshot ||
            typeIdentity64Deriver.identityAlgorithmVersion != identityAlgorithmVersionSnapshot
        ) {
            throw PlanningExpansionException(
                "TypeIdentity64Deriver drift detected inside TypeExpansionPipeline: " +
                        "expected=${identityAlgorithmIdSnapshot}@${identityAlgorithmVersionSnapshot}, " +
                        "actual=${typeIdentity64Deriver.identityAlgorithmId}@${typeIdentity64Deriver.identityAlgorithmVersion}",
            )
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            typeShapeProvider: TypeShapeProvider,
            rawTypeFactsProvider: RawTypeFactsProvider,
            typeIdentity64Deriver: TypeIdentity64Deriver,
            activeMemberProjector: ActiveMemberProjector,
            activeMemberOrderer: ActiveMemberOrderer,
        ): TypeExpansionPipeline {
            val identityAlgorithmId = typeIdentity64Deriver.identityAlgorithmId

            if (identityAlgorithmId.isBlank()) {
                throw PlanningExpansionException(
                    "TypeExpansionPipeline requires non-blank TypeIdentity64Deriver.identityAlgorithmId.",
                )
            }

            val identityAlgorithmVersion = typeIdentity64Deriver.identityAlgorithmVersion

            if (identityAlgorithmVersion < 0L) {
                throw PlanningExpansionException(
                    "TypeExpansionPipeline requires TypeIdentity64Deriver.identityAlgorithmVersion >= 0: " +
                            identityAlgorithmVersion,
                )
            }

            return TypeExpansionPipeline(
                typeShapeProvider = typeShapeProvider,
                rawTypeFactsProvider = rawTypeFactsProvider,
                typeIdentity64Deriver = typeIdentity64Deriver,
                identityAlgorithmIdSnapshot = identityAlgorithmId,
                identityAlgorithmVersionSnapshot = identityAlgorithmVersion,
                activeMemberProjector = activeMemberProjector,
                activeMemberOrderer = activeMemberOrderer,
            )
        }
    }
}