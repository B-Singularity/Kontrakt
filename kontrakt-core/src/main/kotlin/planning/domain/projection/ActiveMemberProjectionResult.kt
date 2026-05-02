package planning.domain.projection

import planning.domain.exception.ActiveMemberProjectionException

/**
 * Result of Core-owned Active Member projection.
 *
 * This result is not yet the canonical ordered traversal view.
 *
 * ActiveMemberOrderer must still:
 * - verify uniqueness,
 * - ratify canonical ordering,
 * - freeze OrderedActiveMembers.
 *
 * The collections are ProjectionSequence values, not arbitrary lists.
 * This preserves projection-stage determinism explicitly.
 */
class ActiveMemberProjectionResult private constructor(
    val ownerTypeFqcn: String,
    val selectedConstructor: SelectedConstructor,
    val members: ProjectionSequence<ProjectedActiveMember>,
    val propertyDemotions: ProjectionSequence<PropertyDemotionRecord>,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            selectedConstructor: SelectedConstructor,
            members: Collection<ProjectedActiveMember>,
            propertyDemotions: Collection<PropertyDemotionRecord>,
        ): ActiveMemberProjectionResult {
            ProjectionOrderingPrimitives.requireCanonicalComponentShape(
                field = "ActiveMemberProjectionResult.ownerTypeFqcn",
                value = ownerTypeFqcn,
            )

            validateSelectedConstructorOwner(
                ownerTypeFqcn = ownerTypeFqcn,
                selectedConstructor = selectedConstructor,
            )

            validateProjectedMemberOwnership(
                ownerTypeFqcn = ownerTypeFqcn,
                members = members,
            )

            validatePropertyDemotionOwnership(
                ownerTypeFqcn = ownerTypeFqcn,
                propertyDemotions = propertyDemotions,
            )

            val frozenMembers =
                ProjectionSequence.captureDeterministicProducerOrder(
                    ownerTypeFqcn = ownerTypeFqcn,
                    sequenceKind = "projected-active-members",
                    elements = members,
                )

            val frozenDemotions =
                ProjectionSequence.captureDeterministicProducerOrder(
                    ownerTypeFqcn = ownerTypeFqcn,
                    sequenceKind = "property-demotions",
                    elements = propertyDemotions,
                )

            return ActiveMemberProjectionResult(
                ownerTypeFqcn = ownerTypeFqcn,
                selectedConstructor = selectedConstructor,
                members = frozenMembers,
                propertyDemotions = frozenDemotions,
            )
        }

        private fun validateSelectedConstructorOwner(
            ownerTypeFqcn: String,
            selectedConstructor: SelectedConstructor,
        ) {
            if (selectedConstructor.ownerTypeFqcn != ownerTypeFqcn) {
                throw ActiveMemberProjectionException(
                    "ActiveMemberProjectionResult selectedConstructor owner mismatch: " +
                        "expected=$ownerTypeFqcn, actual=${selectedConstructor.ownerTypeFqcn}",
                )
            }
        }

        private fun validateProjectedMemberOwnership(
            ownerTypeFqcn: String,
            members: Collection<ProjectedActiveMember>,
        ) {
            val iterator = members.iterator()

            while (iterator.hasNext()) {
                val member = iterator.next()

                if (member.ownerTypeFqcn != ownerTypeFqcn) {
                    throw ActiveMemberProjectionException(
                        "ActiveMemberProjectionResult projected member owner mismatch: " +
                            "expected=$ownerTypeFqcn, actual=${member.ownerTypeFqcn}, " +
                            "memberName=${member.name}",
                    )
                }
            }
        }

        private fun validatePropertyDemotionOwnership(
            ownerTypeFqcn: String,
            propertyDemotions: Collection<PropertyDemotionRecord>,
        ) {
            val iterator = propertyDemotions.iterator()

            while (iterator.hasNext()) {
                val demotion = iterator.next()

                if (demotion.ownerTypeFqcn != ownerTypeFqcn) {
                    throw ActiveMemberProjectionException(
                        "ActiveMemberProjectionResult property demotion owner mismatch: " +
                            "expected=$ownerTypeFqcn, actual=${demotion.ownerTypeFqcn}, " +
                            "propertyName=${demotion.propertyName}",
                    )
                }
            }
        }
    }
}
