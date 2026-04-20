package planning.domain.expansion

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.service.TypeIdentity64Deriver
import metamodel.domain.vo.TypeKind
import metamodel.domain.vo.TypeReference
import planning.domain.exception.CorruptResolvedTypeShapeException
import planning.domain.exception.RawTypeFactsSubjectMismatchException
import planning.domain.exception.TypeShapeSubjectMismatchException
import planning.domain.port.outgoing.RawTypeFactsProvider
import planning.domain.port.outgoing.TypeShapeProvider
import planning.domain.projection.ActiveMemberOrderer
import planning.domain.projection.ActiveMemberProjector
import planning.domain.projection.CapabilityProfile

/**
 * Domain service that prepares type expansion decisions for StructuralPlannerCore.
 *
 * DDD role:
 * - Planning domain service
 * - owns type expansion dispatch rules
 *
 * Hexagonal role:
 * - depends on outbound ports TypeShapeProvider and RawTypeFactsProvider
 * - does not know whether facts come from reflection, KSP, bytecode, or static source
 *
 * Compiler-style role:
 * - performs staged lowering:
 *
 *   TypeReference
 *   -> ResolvedTypeShape
 *   -> TypeExpansionDecision
 *
 * For COMPOSITE:
 *
 *   TypeReference
 *   -> RawTypeFactsDTO
 *   -> subject-continuity verification
 *   -> ActiveMemberProjectionResult
 *   -> OrderedActiveMembers
 *   -> CompositeExpansionPlan
 *
 * Accounting:
 * - The pipeline does not own PlannerSession.
 * - The caller must pass a session/run-bound TypeExpansionWorkMeter.
 * - Every meaningful stage records a closed TypeExpansionWorkEvent.
 */
class TypeExpansionPipeline private constructor(
    private val typeShapeProvider: TypeShapeProvider,
    private val rawTypeFactsProvider: RawTypeFactsProvider,
    private val typeIdentity64Deriver: TypeIdentity64Deriver,
    private val activeMemberProjector: ActiveMemberProjector,
    private val activeMemberOrderer: ActiveMemberOrderer,
) {
    fun prepareExpansion(
        reference: TypeReference,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision {
        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_SHAPE_RESOLUTION,
            subject = reference,
        )

        val shape = typeShapeProvider.resolveTypeShape(reference)

        requireShapeSubjectMatchesReference(
            expected = reference,
            shape = shape,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_SHAPE_LOWERING,
            subject = reference,
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
                workMeter.record(
                    event = TypeExpansionWorkEvent.ATOMIC_EXPANSION_DECISION,
                    subject = reference,
                )

                TypeExpansionDecision.AtomicExpansion.issue(
                    subject = reference,
                )
            }

            TypeKind.COMPOSITE -> {
                prepareCompositeExpansion(
                    reference = reference,
                    capabilityProfile = capabilityProfile,
                    workMeter = workMeter,
                )
            }

            TypeKind.COLLECTION -> {
                workMeter.record(
                    event = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                    subject = reference,
                )

                TypeExpansionDecision.CollectionExpansion.issue(
                    subject = reference,
                    elementType = requireElementType(shape),
                )
            }

            TypeKind.ARRAY -> {
                workMeter.record(
                    event = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                    subject = reference,
                )

                TypeExpansionDecision.ArrayExpansion.issue(
                    subject = reference,
                    componentType = requireComponentType(shape),
                )
            }

            TypeKind.MAP -> {
                workMeter.record(
                    event = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                    subject = reference,
                )

                TypeExpansionDecision.MapExpansion.issue(
                    subject = reference,
                    keyType = requireKeyType(shape),
                    valueType = requireValueType(shape),
                )
            }

            TypeKind.INTERFACE -> {
                workMeter.record(
                    event = TypeExpansionWorkEvent.INTERFACE_EXPANSION_DECISION,
                    subject = reference,
                )

                TypeExpansionDecision.InterfaceExpansion.issue(
                    subject = reference,
                )
            }
        }
    }

    private fun prepareCompositeExpansion(
        reference: TypeReference,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision.CompositeExpansion {
        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_RESOLUTION,
            subject = reference,
        )

        val rawFacts = rawTypeFactsProvider.resolveRawFacts(reference)

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK,
            subject = reference,
        )

        requireRawFactsSubjectMatchesReference(
            reference = reference,
            rawFacts = rawFacts,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_PROJECTION,
            subject = reference,
        )

        val projection = activeMemberProjector.project(
            facts = rawFacts,
            capabilityProfile = capabilityProfile,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_ORDERING,
            subject = reference,
        )

        val orderedMembers = activeMemberOrderer.order(projection)

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_EXPANSION_PLAN_ISSUE,
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

    private fun requireRawFactsSubjectMatchesReference(
        reference: TypeReference,
        rawFacts: RawTypeFactsDTO,
    ) {
        val expectedTypeIdentity64 = typeIdentity64Deriver.deriveIdentity64(reference)

        val identityMismatch = rawFacts.typeIdentity64 != expectedTypeIdentity64
        val algorithmIdMismatch = rawFacts.typeIdentityAlgorithmId != typeIdentity64Deriver.identityAlgorithmId
        val algorithmVersionMismatch =
            rawFacts.typeIdentityAlgorithmVersion != typeIdentity64Deriver.identityAlgorithmVersion

        if (identityMismatch || algorithmIdMismatch || algorithmVersionMismatch) {
            throw RawTypeFactsSubjectMismatchException(
                expectedTypeId = reference.id,
                expectedSignature = reference.signature,
                expectedCycleId = reference.cycleId,
                expectedTypeIdentity64 = expectedTypeIdentity64,
                actualOwnerTypeFqcn = rawFacts.ownerTypeFqcn,
                actualTypeIdentity64 = rawFacts.typeIdentity64,
                expectedAlgorithmId = typeIdentity64Deriver.identityAlgorithmId,
                actualAlgorithmId = rawFacts.typeIdentityAlgorithmId,
                expectedAlgorithmVersion = typeIdentity64Deriver.identityAlgorithmVersion,
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
        val builder = StringBuilder()
        var wrote = false

        if (idMismatch) {
            builder.append("id")
            wrote = true
        }

        if (signatureMismatch) {
            if (wrote) {
                builder.append(',')
            }
            builder.append("signature")
            wrote = true
        }

        if (cycleIdMismatch) {
            if (wrote) {
                builder.append(',')
            }
            builder.append("cycleId")
        }

        return builder.toString()
    }

    private fun renderRawFactMismatchFields(
        identityMismatch: Boolean,
        algorithmIdMismatch: Boolean,
        algorithmVersionMismatch: Boolean,
    ): String {
        val builder = StringBuilder()
        var wrote = false

        if (identityMismatch) {
            builder.append("typeIdentity64")
            wrote = true
        }

        if (algorithmIdMismatch) {
            if (wrote) {
                builder.append(',')
            }
            builder.append("typeIdentityAlgorithmId")
            wrote = true
        }

        if (algorithmVersionMismatch) {
            if (wrote) {
                builder.append(',')
            }
            builder.append("typeIdentityAlgorithmVersion")
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

    companion object {
        @JvmStatic
        fun issue(
            typeShapeProvider: TypeShapeProvider,
            rawTypeFactsProvider: RawTypeFactsProvider,
            typeIdentity64Deriver: TypeIdentity64Deriver,
            activeMemberProjector: ActiveMemberProjector,
            activeMemberOrderer: ActiveMemberOrderer,
        ): TypeExpansionPipeline {
            return TypeExpansionPipeline(
                typeShapeProvider = typeShapeProvider,
                rawTypeFactsProvider = rawTypeFactsProvider,
                typeIdentity64Deriver = typeIdentity64Deriver,
                activeMemberProjector = activeMemberProjector,
                activeMemberOrderer = activeMemberOrderer,
            )
        }
    }
}