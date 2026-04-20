package planning.domain.expansion

import planning.domain.exception.InvalidLoweredTypeIdentityException
import planning.domain.exception.PlanningExpansionException
import planning.domain.projection.OrderedActiveMembers
import planning.domain.projection.ProjectionSequence
import planning.domain.projection.PropertyDemotionRecord
import planning.domain.projection.SelectedConstructor

/**
 * Prepared composite expansion plan.
 *
 * This is the only composite expansion material that StructuralPlannerCore
 * should use to create member-iteration frames.
 *
 * It intentionally does not expose:
 * - RawTypeFactsDTO,
 * - ConstructorCandidateFact collections,
 * - PropertyFact collections,
 * - ActiveMemberProjectionResult.members as traversal input.
 *
 * Traversal must consume OrderedActiveMembers only.
 *
 * Empty orderedMembers is valid:
 * - zero-argument constructor,
 * - no admitted properties,
 * - no constructor parameters.
 *
 * In that case the core should complete the member-iteration frame immediately
 * or lower it to an empty composite plan according to the frame protocol.
 */
class CompositeExpansionPlan private constructor(
    val ownerTypeFqcn: String,
    val typeIdentity64: Long,
    val selectedConstructor: SelectedConstructor,
    val orderedMembers: OrderedActiveMembers,
    val propertyDemotions: ProjectionSequence<PropertyDemotionRecord>,
) {
    companion object {
        private const val RESERVED_ZERO_IDENTITY: Long = 0L
        private const val RESERVED_MINUS_ONE_IDENTITY: Long = -1L

        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            typeIdentity64: Long,
            selectedConstructor: SelectedConstructor,
            orderedMembers: OrderedActiveMembers,
            propertyDemotions: ProjectionSequence<PropertyDemotionRecord>,
        ): CompositeExpansionPlan {
            if (ownerTypeFqcn.isBlank()) {
                throw PlanningExpansionException(
                    "CompositeExpansionPlan.ownerTypeFqcn must not be blank.",
                )
            }

            if (typeIdentity64 == RESERVED_ZERO_IDENTITY ||
                typeIdentity64 == RESERVED_MINUS_ONE_IDENTITY
            ) {
                throw InvalidLoweredTypeIdentityException(
                    ownerTypeFqcn = ownerTypeFqcn,
                    typeIdentity64 = typeIdentity64,
                )
            }

            if (selectedConstructor.ownerTypeFqcn != ownerTypeFqcn) {
                throw PlanningExpansionException(
                    "CompositeExpansionPlan selectedConstructor owner mismatch: " +
                            "expected=$ownerTypeFqcn, actual=${selectedConstructor.ownerTypeFqcn}",
                )
            }

            return CompositeExpansionPlan(
                ownerTypeFqcn = ownerTypeFqcn,
                typeIdentity64 = typeIdentity64,
                selectedConstructor = selectedConstructor,
                orderedMembers = orderedMembers,
                propertyDemotions = propertyDemotions,
            )
        }
    }
}