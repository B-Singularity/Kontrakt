package stage.lowering.material.expansion

import stage.canonicalization.material.TypeReference
import stage.input.presentation.dto.RawTypeFactsDTO
import stage.input.presentation.raw.ResolvedTypeShape
import stage.input.presentation.raw.TypeKind
import stage.lowering.boundary.RawTypeFactsProvider
import stage.lowering.boundary.RawTypeFactsResolutionKind
import stage.lowering.boundary.TypeCycleIdentityProvider
import stage.lowering.boundary.TypeShapeProvider
import stage.lowering.diagnostics.CorruptResolvedTypeShapeException
import stage.lowering.diagnostics.PlanningExpansionException
import stage.lowering.diagnostics.RawTypeFactsSubjectMismatchException
import stage.lowering.diagnostics.TypeCycleIdentitySubjectMismatchException
import stage.lowering.diagnostics.TypeShapeSubjectMismatchException
import stage.lowering.diagnostics.UnsupportedTypeExpansionException
import stage.lowering.material.projection.ActiveMemberOrderer
import stage.lowering.material.projection.ActiveMemberProjector
import stage.lowering.material.projection.CapabilityProfile

/**
 * Compiler-style type expansion pipeline.
 *
 * ADR-0037 split:
 *
 *   preflight:
 *     TypeReference
 *     -> ResolvedTypeShape
 *     -> TypeCycleIdentity
 *     -> TypeExpansionPreflightDecision
 *
 *   materialization after cycle miss:
 *     PreflightDecision
 *     -> TypeExpansionDecision
 *
 * Composite materialization:
 *     CompositePreflight
 *     -> RawTypeFactsResolution
 *     -> ActiveMemberProjectionResult
 *     -> OrderedActiveMembers
 *     -> CompositeExpansionPlan
 *
 * Boundary rules:
 * - no PlannerSession dependency;
 * - no raw facts on cycle-hit path;
 * - no projection/order on cycle-hit path;
 * - non-composite decisions remain in the domain vocabulary even if core frames
 *   are not implemented yet;
 * - all work events are recorded after successful stage completion.
 */
class TypeExpansionPipeline private constructor(
    private val typeShapeProvider: TypeShapeProvider,
    private val typeCycleIdentityProvider: TypeCycleIdentityProvider,
    private val rawTypeFactsProvider: RawTypeFactsProvider,
    private val identityAlgorithmIdSnapshot: String,
    private val identityAlgorithmVersionSnapshot: Long,
    private val activeMemberProjector: ActiveMemberProjector,
    private val activeMemberOrderer: ActiveMemberOrderer,
) {
    /**
     * Prepare only shape + cycle identity.
     *
     * This method must not resolve RawTypeFactsDTO.
     * It is lawful on paths that may later turn out to be active-cycle hits.
     */
    fun preparePreflight(
        reference: TypeReference,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionPreflightDecision {
        requireIdentityProviderStable()

        val shape = typeShapeProvider.resolveTypeShape(reference)

        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_SHAPE_RESOLUTION,
            subject = reference,
        )

        requireShapeSubjectMatchesReference(
            expected = reference,
            shape = shape,
        )

        val cycleIdentity = typeCycleIdentityProvider.resolveCycleIdentity(reference)

        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_CYCLE_IDENTITY_RESOLUTION,
            subject = reference,
        )

        requireCycleIdentityMatchesReference(
            expected = reference,
            cycleIdentity = cycleIdentity,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_CYCLE_IDENTITY_CONTINUITY_CHECK,
            subject = reference,
        )

        val preflight =
            lowerShapeToPreflight(
                reference = reference,
                shape = shape,
                cycleIdentity = cycleIdentity,
            )

        /*
         * Successful lowering is recorded only after the shape has been converted
         * into a lawful preflight decision.
         *
         * INTERFACE throws before this point because no executable interface
         * resolution path exists yet. Therefore it does not receive a successful
         * TYPE_SHAPE_LOWERING charge.
         */
        workMeter.record(
            event = TypeExpansionWorkEvent.TYPE_SHAPE_LOWERING,
            subject = reference,
        )

        return preflight
    }

    /**
     * Materialize a full expansion decision after active-cycle detection reports
     * cycle miss.
     *
     * This method may resolve raw facts only for composite preflight.
     * Non-composite decisions are materialized from preflight data alone.
     */
    fun materializeAfterCycleMiss(
        preflight: TypeExpansionPreflightDecision,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision {
        requireIdentityProviderStable()

        return when (preflight) {
            is TypeExpansionPreflightDecision.AtomicPreflight -> {
                val decision =
                    TypeExpansionDecision.AtomicExpansion.issue(
                        subject = preflight.subject,
                    )

                workMeter.record(
                    event = TypeExpansionWorkEvent.ATOMIC_EXPANSION_DECISION,
                    subject = preflight.subject,
                )

                decision
            }

            is TypeExpansionPreflightDecision.CollectionPreflight -> {
                val decision =
                    TypeExpansionDecision.CollectionExpansion.issue(
                        subject = preflight.subject,
                        elementType = preflight.elementType,
                    )

                workMeter.record(
                    event = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                    subject = preflight.subject,
                )

                decision
            }

            is TypeExpansionPreflightDecision.ArrayPreflight -> {
                val decision =
                    TypeExpansionDecision.ArrayExpansion.issue(
                        subject = preflight.subject,
                        componentType = preflight.componentType,
                    )

                workMeter.record(
                    event = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                    subject = preflight.subject,
                )

                decision
            }

            is TypeExpansionPreflightDecision.MapPreflight -> {
                val decision =
                    TypeExpansionDecision.MapExpansion.issue(
                        subject = preflight.subject,
                        keyType = preflight.keyType,
                        valueType = preflight.valueType,
                    )

                workMeter.record(
                    event = TypeExpansionWorkEvent.CONTAINER_EXPANSION_DECISION,
                    subject = preflight.subject,
                )

                decision
            }

            is TypeExpansionPreflightDecision.CompositePreflight -> {
                materializeCompositeAfterCycleMiss(
                    preflight = preflight,
                    capabilityProfile = capabilityProfile,
                    workMeter = workMeter,
                )
            }
        }
    }

    private fun materializeCompositeAfterCycleMiss(
        preflight: TypeExpansionPreflightDecision.CompositePreflight,
        capabilityProfile: CapabilityProfile,
        workMeter: TypeExpansionWorkMeter,
    ): TypeExpansionDecision.CompositeExpansion {
        val rawResolution = rawTypeFactsProvider.resolveRawFacts(preflight.subject)

        workMeter.record(
            event = rawFactsResolutionEvent(rawResolution.kind),
            subject = preflight.subject,
        )

        val rawFacts = rawResolution.facts

        requireRawFactsSubjectMatchesCycleIdentity(
            preflight = preflight,
            rawFacts = rawFacts,
        )

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_SUBJECT_CONTINUITY_CHECK,
            subject = preflight.subject,
        )

        val projection =
            activeMemberProjector.project(
                facts = rawFacts,
                capabilityProfile = capabilityProfile,
            )

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_PROJECTION,
            subject = preflight.subject,
        )

        val orderedMembers = activeMemberOrderer.order(projection)

        workMeter.record(
            event = TypeExpansionWorkEvent.COMPOSITE_ACTIVE_MEMBER_ORDERING,
            subject = preflight.subject,
        )

        val plan =
            CompositeExpansionPlan.issue(
                ownerTypeFqcn = rawFacts.ownerTypeFqcn,
                typeIdentity64 = rawFacts.typeIdentity64,
                selectedConstructor = projection.selectedConstructor,
                orderedMembers = orderedMembers,
                propertyDemotions = projection.propertyDemotions,
            )

        return TypeExpansionDecision.CompositeExpansion.issue(
            subject = preflight.subject,
            plan = plan,
        )
    }

    private fun lowerShapeToPreflight(
        reference: TypeReference,
        shape: ResolvedTypeShape,
        cycleIdentity: TypeCycleIdentity,
    ): TypeExpansionPreflightDecision =
        when (shape.kind) {
            TypeKind.ATOMIC -> {
                TypeExpansionPreflightDecision.AtomicPreflight.issue(
                    subject = reference,
                    cycleIdentity = cycleIdentity,
                )
            }

            TypeKind.COMPOSITE -> {
                TypeExpansionPreflightDecision.CompositePreflight.issue(
                    subject = reference,
                    cycleIdentity = cycleIdentity,
                )
            }

            TypeKind.COLLECTION -> {
                TypeExpansionPreflightDecision.CollectionPreflight.issue(
                    subject = reference,
                    cycleIdentity = cycleIdentity,
                    elementType = requireElementType(shape),
                )
            }

            TypeKind.ARRAY -> {
                TypeExpansionPreflightDecision.ArrayPreflight.issue(
                    subject = reference,
                    cycleIdentity = cycleIdentity,
                    componentType = requireComponentType(shape),
                )
            }

            TypeKind.MAP -> {
                TypeExpansionPreflightDecision.MapPreflight.issue(
                    subject = reference,
                    cycleIdentity = cycleIdentity,
                    keyType = requireKeyType(shape),
                    valueType = requireValueType(shape),
                )
            }

            TypeKind.INTERFACE -> {
                throw UnsupportedTypeExpansionException(
                    subjectTypeId = reference.id,
                    shapeKind = shape.kind.name,
                    reason = "Interface implementation-resolution policy is not yet implemented.",
                )
            }
        }

    private fun rawFactsResolutionEvent(kind: RawTypeFactsResolutionKind): TypeExpansionWorkEvent =
        when (kind) {
            RawTypeFactsResolutionKind.CACHE_HIT ->
                TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_CACHE_HIT

            RawTypeFactsResolutionKind.ACTUAL_RESOLUTION ->
                TypeExpansionWorkEvent.COMPOSITE_RAW_FACT_RESOLVE
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
                mismatchFields =
                    renderMismatchFields(
                        "id" to idMismatch,
                        "signature" to signatureMismatch,
                        "cycleId" to cycleIdMismatch,
                    ),
            )
        }
    }

    private fun requireCycleIdentityMatchesReference(
        expected: TypeReference,
        cycleIdentity: TypeCycleIdentity,
    ) {
        val idMismatch = cycleIdentity.subject.id != expected.id
        val signatureMismatch = cycleIdentity.subject.signature != expected.signature
        val cycleIdMismatch = cycleIdentity.subject.cycleId != expected.cycleId
        val algorithmIdMismatch = cycleIdentity.identityAlgorithmId != identityAlgorithmIdSnapshot
        val algorithmVersionMismatch =
            cycleIdentity.identityAlgorithmVersion != identityAlgorithmVersionSnapshot

        if (idMismatch ||
            signatureMismatch ||
            cycleIdMismatch ||
            algorithmIdMismatch ||
            algorithmVersionMismatch
        ) {
            throw TypeCycleIdentitySubjectMismatchException(
                expectedTypeId = expected.id,
                actualTypeId = cycleIdentity.subject.id,
                expectedSignature = expected.signature,
                actualSignature = cycleIdentity.subject.signature,
                expectedCycleId = expected.cycleId,
                actualCycleId = cycleIdentity.subject.cycleId,
                expectedAlgorithmId = identityAlgorithmIdSnapshot,
                actualAlgorithmId = cycleIdentity.identityAlgorithmId,
                expectedAlgorithmVersion = identityAlgorithmVersionSnapshot,
                actualAlgorithmVersion = cycleIdentity.identityAlgorithmVersion,
                mismatchFields =
                    renderMismatchFields(
                        "id" to idMismatch,
                        "signature" to signatureMismatch,
                        "cycleId" to cycleIdMismatch,
                        "identityAlgorithmId" to algorithmIdMismatch,
                        "identityAlgorithmVersion" to algorithmVersionMismatch,
                    ),
            )
        }
    }

    private fun requireRawFactsSubjectMatchesCycleIdentity(
        preflight: TypeExpansionPreflightDecision.CompositePreflight,
        rawFacts: RawTypeFactsDTO,
    ) {
        val identityMismatch = rawFacts.typeIdentity64 != preflight.cycleIdentity.identityBits64
        val algorithmIdMismatch = rawFacts.typeIdentityAlgorithmId != identityAlgorithmIdSnapshot
        val algorithmVersionMismatch =
            rawFacts.typeIdentityAlgorithmVersion != identityAlgorithmVersionSnapshot

        if (identityMismatch || algorithmIdMismatch || algorithmVersionMismatch) {
            throw RawTypeFactsSubjectMismatchException(
                expectedTypeId = preflight.subject.id,
                expectedSignature = preflight.subject.signature,
                expectedCycleId = preflight.subject.cycleId,
                expectedTypeIdentity64 = preflight.cycleIdentity.identityBits64,
                actualOwnerTypeFqcn = rawFacts.ownerTypeFqcn,
                actualTypeIdentity64 = rawFacts.typeIdentity64,
                expectedAlgorithmId = identityAlgorithmIdSnapshot,
                actualAlgorithmId = rawFacts.typeIdentityAlgorithmId,
                expectedAlgorithmVersion = identityAlgorithmVersionSnapshot,
                actualAlgorithmVersion = rawFacts.typeIdentityAlgorithmVersion,
                mismatchFields =
                    renderMismatchFields(
                        "typeIdentity64" to identityMismatch,
                        "typeIdentityAlgorithmId" to algorithmIdMismatch,
                        "typeIdentityAlgorithmVersion" to algorithmVersionMismatch,
                    ),
            )
        }
    }

    private fun renderMismatchFields(vararg fields: Pair<String, Boolean>): String {
        val builder = StringBuilder()
        var count = 0

        var i = 0
        while (i < fields.size) {
            val pair = fields[i]
            if (pair.second) {
                if (count > 0) {
                    builder.append(',')
                }
                builder.append(pair.first)
                count++
            }
            i++
        }

        return if (count == 0) "unknown" else builder.toString()
    }

    private fun requireElementType(shape: ResolvedTypeShape): TypeReference =
        shape.elementType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "COLLECTION shape is missing elementType after shape cardinality validation.",
            )

    private fun requireComponentType(shape: ResolvedTypeShape): TypeReference =
        shape.componentType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "ARRAY shape is missing componentType after shape cardinality validation.",
            )

    private fun requireKeyType(shape: ResolvedTypeShape): TypeReference =
        shape.keyType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "MAP shape is missing keyType after shape cardinality validation.",
            )

    private fun requireValueType(shape: ResolvedTypeShape): TypeReference =
        shape.valueType
            ?: throw CorruptResolvedTypeShapeException(
                subjectTypeId = shape.subject.id,
                shapeKind = shape.kind.name,
                reason = "MAP shape is missing valueType after shape cardinality validation.",
            )

    private fun requireIdentityProviderStable() {
        if (typeCycleIdentityProvider.identityAlgorithmId != identityAlgorithmIdSnapshot ||
            typeCycleIdentityProvider.identityAlgorithmVersion != identityAlgorithmVersionSnapshot
        ) {
            throw PlanningExpansionException(
                "TypeCycleIdentityProvider drift detected: " +
                        "expected=$identityAlgorithmIdSnapshot@$identityAlgorithmVersionSnapshot, " +
                        "actual=${typeCycleIdentityProvider.identityAlgorithmId}@${typeCycleIdentityProvider.identityAlgorithmVersion}",
            )
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            typeShapeProvider: TypeShapeProvider,
            typeCycleIdentityProvider: TypeCycleIdentityProvider,
            rawTypeFactsProvider: RawTypeFactsProvider,
            activeMemberProjector: ActiveMemberProjector,
            activeMemberOrderer: ActiveMemberOrderer,
        ): TypeExpansionPipeline {
            val algorithmId = typeCycleIdentityProvider.identityAlgorithmId

            if (algorithmId.isBlank()) {
                throw PlanningExpansionException(
                    "TypeExpansionPipeline requires non-blank TypeCycleIdentityProvider.identityAlgorithmId.",
                )
            }

            if (algorithmId.contains('|')) {
                throw PlanningExpansionException(
                    "TypeExpansionPipeline identityAlgorithmId must not contain reserved delimiter '|': $algorithmId",
                )
            }

            val algorithmVersion = typeCycleIdentityProvider.identityAlgorithmVersion

            if (algorithmVersion < 0L) {
                throw PlanningExpansionException(
                    "TypeExpansionPipeline requires identityAlgorithmVersion >= 0: $algorithmVersion",
                )
            }

            return TypeExpansionPipeline(
                typeShapeProvider = typeShapeProvider,
                typeCycleIdentityProvider = typeCycleIdentityProvider,
                rawTypeFactsProvider = rawTypeFactsProvider,
                identityAlgorithmIdSnapshot = algorithmId,
                identityAlgorithmVersionSnapshot = algorithmVersion,
                activeMemberProjector = activeMemberProjector,
                activeMemberOrderer = activeMemberOrderer,
            )
        }
    }
}
