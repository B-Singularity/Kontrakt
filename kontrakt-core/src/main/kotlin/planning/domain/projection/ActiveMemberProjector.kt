package planning.domain.projection

import planning.domain.exception.AmbiguousConstructorSelectionException
import planning.domain.exception.NoEligibleConstructorSelectionException
import stage.input.material.ConstructorCandidateFact
import stage.input.material.DeclarationOrdinal
import stage.input.material.DefaultValuePresence
import stage.input.material.NullabilityKind
import stage.input.material.RawTypeFactsDTO

/**
 * Core-owned semantic projector.
 *
 * Responsibilities:
 * - constructor admission
 * - deterministic constructor selection
 * - property admission / demotion
 * - Active Member projection
 *
 * Non-responsibilities:
 * - raw fact discovery
 * - source normalization
 * - canonical ordering
 * - uniqueness verification
 * - traversal frame creation
 * - cache/interner key issuance
 */
class ActiveMemberProjector private constructor() {
    fun project(
        facts: RawTypeFactsDTO,
        capabilityProfile: CapabilityProfile,
    ): ActiveMemberProjectionResult {
        val selectedConstructor =
            selectConstructor(
                facts = facts,
                capabilityProfile = capabilityProfile,
            )

        val projectedMembers =
            ArrayList<ProjectedActiveMember>(
                selectedConstructor.candidate.parameters.size + facts.properties.size,
            )

        projectConstructorParameters(
            selectedConstructor = selectedConstructor,
            destination = projectedMembers,
        )

        val demotions = ArrayList<PropertyDemotionRecord>()

        projectProperties(
            facts = facts,
            capabilityProfile = capabilityProfile,
            destination = projectedMembers,
            demotions = demotions,
        )

        return ActiveMemberProjectionResult.issue(
            ownerTypeFqcn = facts.ownerTypeFqcn,
            selectedConstructor = selectedConstructor,
            members = projectedMembers,
            propertyDemotions = demotions,
        )
    }

    private fun selectConstructor(
        facts: RawTypeFactsDTO,
        capabilityProfile: CapabilityProfile,
    ): SelectedConstructor {
        val admitted = ArrayList<ConstructorScore>()
        val rejections = ArrayList<ConstructorRejectionRecord>()

        var i = 0
        while (i < facts.constructors.size) {
            val candidate = facts.constructors[i]
            val decision = capabilityProfile.admitConstructor(candidate)

            if (decision.isAdmitted) {
                admitted.add(score(candidate))
            } else {
                val rejected = decision as ConstructorAdmissionDecision.Rejected
                rejections.add(
                    ConstructorRejectionRecord.issue(
                        ownerTypeFqcn = candidate.ownerTypeFqcn,
                        constructorSignature = candidate.constructorSignature,
                        constructorSignatureNormalizationVersion = candidate.constructorSignatureNormalizationVersion,
                        declarationOrdinal = candidate.declarationOrdinal,
                        visibility = candidate.visibility,
                        origin = candidate.origin,
                        reason = rejected.reason,
                    ),
                )
            }

            i++
        }

        if (admitted.isEmpty()) {
            throw NoEligibleConstructorSelectionException(
                ownerTypeFqcn = facts.ownerTypeFqcn,
                candidateCount = facts.constructors.size,
                rejectionEvidence = rejections,
            )
        }

        admitted.sortWith(CONSTRUCTOR_SELECTION_COMPARATOR)

        if (admitted.size > 1 &&
            CONSTRUCTOR_SELECTION_COMPARATOR.compare(admitted[0], admitted[1]) == 0
        ) {
            throw AmbiguousConstructorSelectionException(
                ownerTypeFqcn = facts.ownerTypeFqcn,
                tiedConstructorSignatures = collectTiedConstructorSignatures(admitted),
            )
        }

        val winner = admitted[0]

        return SelectedConstructor.issue(
            candidate = winner.candidate,
            metrics = winner.metrics,
        )
    }

    private fun projectConstructorParameters(
        selectedConstructor: SelectedConstructor,
        destination: MutableCollection<ProjectedActiveMember>,
    ) {
        val candidate = selectedConstructor.candidate

        var i = 0
        while (i < candidate.parameters.size) {
            val parameter = candidate.parameters[i]

            destination.add(
                ProjectedActiveMember.issue(
                    ownerTypeFqcn = parameter.ownerTypeFqcn,
                    memberKind = MemberKind.CTOR_PARAM,
                    name = parameter.name,
                    typeReference = parameter.typeReference,
                    typeSignatureNormalizationVersion = parameter.typeSignatureNormalizationVersion,
                    declarationOrdinal = DeclarationOrdinal.present(parameter.parameterIndex),
                    nullability = parameter.nullability,
                    sourceRef =
                        ProjectionSourceRef.SelectedConstructorParameterRef.issue(
                            constructorSignature = candidate.constructorSignature,
                            constructorSignatureNormalizationVersion = candidate.constructorSignatureNormalizationVersion,
                            constructorDeclarationOrdinal = candidate.declarationOrdinal,
                            parameterIndex = parameter.parameterIndex,
                            defaultValuePresence = parameter.defaultValuePresence,
                            origin = candidate.origin,
                            visibility = candidate.visibility,
                        ),
                ),
            )

            i++
        }
    }

    private fun projectProperties(
        facts: RawTypeFactsDTO,
        capabilityProfile: CapabilityProfile,
        destination: MutableCollection<ProjectedActiveMember>,
        demotions: MutableCollection<PropertyDemotionRecord>,
    ) {
        var i = 0
        while (i < facts.properties.size) {
            val property = facts.properties[i]
            val decision = capabilityProfile.admitProperty(property)

            if (decision.isAdmitted) {
                destination.add(
                    ProjectedActiveMember.issue(
                        ownerTypeFqcn = property.ownerTypeFqcn,
                        memberKind = MemberKind.PROPERTY,
                        name = property.name,
                        typeReference = property.typeReference,
                        typeSignatureNormalizationVersion = property.typeSignatureNormalizationVersion,
                        declarationOrdinal = property.declarationOrdinal,
                        nullability = property.nullability,
                        sourceRef =
                            ProjectionSourceRef.EligiblePropertyRef.issue(
                                propertyDeclarationOrdinal = property.declarationOrdinal,
                                origin = property.origin,
                                declaredVisibility = property.declaredVisibility,
                                setterVisibility = property.setterVisibility,
                                mutability = property.mutability,
                                storageKind = property.storageKind,
                            ),
                    ),
                )
            } else {
                val demoted = decision as PropertyAdmissionDecision.Demoted
                demotions.add(
                    PropertyDemotionRecord.issue(
                        property = property,
                        reason = demoted.reason,
                    ),
                )
            }

            i++
        }
    }

    private fun score(candidate: ConstructorCandidateFact): ConstructorScore {
        var strong = 0
        var defaultAvailable = 0
        var nullableAvailable = 0

        var i = 0
        while (i < candidate.parameters.size) {
            val parameter = candidate.parameters[i]

            /*
             * ADR-0030 conservative nullability rule:
             * UNKNOWN nullability is treated as STRONG, not as nullable.
             *
             * Rationale:
             * unknown must not optimistically loosen constructor/cycle reasoning.
             */
            when (parameter.nullability) {
                NullabilityKind.NON_NULL,
                NullabilityKind.UNKNOWN,
                    -> strong++

                NullabilityKind.NULLABLE -> nullableAvailable++
            }

            if (parameter.defaultValuePresence == DefaultValuePresence.PRESENT) {
                defaultAvailable++
            }

            i++
        }

        return ConstructorScore.issue(
            candidate = candidate,
            metrics =
                ConstructorSelectionMetrics.issue(
                    strongSatisfiableCount = strong,
                    defaultAvailableCount = defaultAvailable,
                    nullableAvailableCount = nullableAvailable,
                    totalParameterCount = candidate.parameters.size,
                ),
        )
    }

    private fun collectTiedConstructorSignatures(sortedScores: List<ConstructorScore>): List<String> {
        val result = ArrayList<String>()
        val first = sortedScores[0]

        var i = 0
        while (i < sortedScores.size) {
            val current = sortedScores[i]

            if (CONSTRUCTOR_SELECTION_COMPARATOR.compare(first, current) == 0) {
                result.add(current.candidate.constructorSignature)
            }

            i++
        }

        /*
         * Constructor signatures are already normalized by the metamodel boundary.
         * This is deterministic lexical ordering, not locale-sensitive collation.
         */
        result.sortWith(CONSTRUCTOR_SIGNATURE_TEXT_COMPARATOR)

        return result
    }

    companion object {
        @JvmStatic
        fun issue(): ActiveMemberProjector = ActiveMemberProjector()

        private val CONSTRUCTOR_SIGNATURE_TEXT_COMPARATOR: Comparator<String> =
            Comparator { left, right -> left.compareTo(right) }

        /**
         * Best constructor sorts first.
         *
         * Selection tuple:
         * 1. larger strong-satisfiable count
         * 2. larger default-available count
         * 3. larger nullable-available count
         * 4. smaller total parameter count
         * 5. lexicographically stable normalized constructor signature
         * 6. constructor signature normalization version
         *
         * If the comparator still ties after signature+version, constructor selection
         * is ambiguous. Under the raw-fact boundary, same signature+version should
         * already have been rejected as duplicate constructor facts.
         */
        private val CONSTRUCTOR_SELECTION_COMPARATOR: Comparator<ConstructorScore> =
            Comparator { left, right ->
                compareIntsDescending(
                    left.metrics.strongSatisfiableCount,
                    right.metrics.strongSatisfiableCount,
                ).takeIfNonZero()
                    ?: compareIntsDescending(
                        left.metrics.defaultAvailableCount,
                        right.metrics.defaultAvailableCount,
                    ).takeIfNonZero()
                    ?: compareIntsDescending(
                        left.metrics.nullableAvailableCount,
                        right.metrics.nullableAvailableCount,
                    ).takeIfNonZero()
                    ?: compareIntsAscending(
                        left.metrics.totalParameterCount,
                        right.metrics.totalParameterCount,
                    ).takeIfNonZero()
                    ?: left.candidate.constructorSignature
                        .compareTo(right.candidate.constructorSignature)
                        .takeIfNonZero()
                    ?: java.lang.Long.compare(
                        left.candidate.constructorSignatureNormalizationVersion,
                        right.candidate.constructorSignatureNormalizationVersion,
                    )
            }

        private fun compareIntsDescending(
            left: Int,
            right: Int,
        ): Int = java.lang.Integer.compare(right, left)

        private fun compareIntsAscending(
            left: Int,
            right: Int,
        ): Int = java.lang.Integer.compare(left, right)

        private fun Int.takeIfNonZero(): Int? = if (this != 0) this else null
    }
}

/**
 * Internal constructor score for deterministic selection.
 *
 * Not a data class: copy-style reconstruction is intentionally avoided.
 */
private class ConstructorScore private constructor(
    val candidate: ConstructorCandidateFact,
    val metrics: ConstructorSelectionMetrics,
) {
    companion object {
        @JvmStatic
        fun issue(
            candidate: ConstructorCandidateFact,
            metrics: ConstructorSelectionMetrics,
        ): ConstructorScore =
            ConstructorScore(
                candidate = candidate,
                metrics = metrics,
            )
    }
}
