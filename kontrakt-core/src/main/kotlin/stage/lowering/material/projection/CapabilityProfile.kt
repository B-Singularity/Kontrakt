package stage.lowering.material.projection

import stage.input.presentation.dto.ConstructorCandidateFact
import stage.input.presentation.dto.PropertyFact
import stage.input.presentation.raw.MemberOrigin
import stage.input.presentation.raw.PropertyMutability
import stage.input.presentation.raw.PropertyStorageKind
import stage.input.presentation.raw.VisibilityKind
import stage.lowering.diagnostics.ActiveMemberProjectionException

/**
 * Immutable Core-owned capability profile.
 *
 * DDD role:
 * - domain policy value object
 *
 * Hexagonal role:
 * - not supplied by metamodel adapters
 * - adapters provide raw facts only
 * - the Planning Core evaluates those facts under this profile
 *
 * Compiler-style role:
 * - explicit versioned policy input for semantic projection
 *
 * This profile controls admission/demotion only.
 * It must not perform ordering, traversal, cache-key issuance, or interner routing.
 */
class CapabilityProfile private constructor(
    val capabilityProfileVersion: Long,
    private val allowPublic: Boolean,
    private val allowProtected: Boolean,
    private val allowInternal: Boolean,
    private val allowPrivate: Boolean,
    private val allowMutableProperties: Boolean,
    private val allowLateinitProperties: Boolean,
    private val allowDelegatedProperties: Boolean,
    private val allowComputedProperties: Boolean,
    private val allowAdapterInferredMembers: Boolean,
) {
    fun admitConstructor(candidate: ConstructorCandidateFact): ConstructorAdmissionDecision {
        if (candidate.origin == MemberOrigin.SYNTHETIC) {
            return ConstructorAdmissionDecision.Rejected.issue(
                ConstructorRejectionReason.SYNTHETIC_CONSTRUCTOR,
            )
        }

        if (candidate.origin == MemberOrigin.UNKNOWN) {
            return ConstructorAdmissionDecision.Rejected.issue(
                ConstructorRejectionReason.UNKNOWN_ORIGIN_REJECTED_BY_CONSERVATIVE_RULE,
            )
        }

        if (candidate.origin == MemberOrigin.ADAPTER_INFERRED && !allowAdapterInferredMembers) {
            return ConstructorAdmissionDecision.Rejected.issue(
                ConstructorRejectionReason.ADAPTER_INFERRED_REJECTED_BY_CAPABILITY_PROFILE,
            )
        }

        if (candidate.visibility == VisibilityKind.UNKNOWN) {
            return ConstructorAdmissionDecision.Rejected.issue(
                ConstructorRejectionReason.UNKNOWN_VISIBILITY_REJECTED_BY_CONSERVATIVE_RULE,
            )
        }

        if (!isVisibilityAllowed(candidate.visibility)) {
            return ConstructorAdmissionDecision.Rejected.issue(
                ConstructorRejectionReason.NOT_VISIBLE_UNDER_CAPABILITY_PROFILE,
            )
        }

        return ConstructorAdmissionDecision.Admitted
    }

    fun admitProperty(property: PropertyFact): PropertyAdmissionDecision {
        if (property.origin == MemberOrigin.SYNTHETIC) {
            return PropertyAdmissionDecision.Demoted.issue(
                PropertyDemotionReason.SYNTHETIC_PROPERTY,
            )
        }

        if (property.origin == MemberOrigin.UNKNOWN) {
            return PropertyAdmissionDecision.Demoted.issue(
                PropertyDemotionReason.UNKNOWN_ORIGIN_REJECTED_BY_CONSERVATIVE_RULE,
            )
        }

        if (property.origin == MemberOrigin.ADAPTER_INFERRED && !allowAdapterInferredMembers) {
            return PropertyAdmissionDecision.Demoted.issue(
                PropertyDemotionReason.ADAPTER_INFERRED_REJECTED_BY_CAPABILITY_PROFILE,
            )
        }

        if (property.declaredVisibility == VisibilityKind.UNKNOWN) {
            return PropertyAdmissionDecision.Demoted.issue(
                PropertyDemotionReason.UNKNOWN_VISIBILITY_REJECTED_BY_CONSERVATIVE_RULE,
            )
        }

        if (!isVisibilityAllowed(property.declaredVisibility)) {
            return PropertyAdmissionDecision.Demoted.issue(
                PropertyDemotionReason.NOT_VISIBLE_UNDER_CAPABILITY_PROFILE,
            )
        }

        when (property.mutability) {
            PropertyMutability.READ_ONLY -> Unit

            PropertyMutability.MUTABLE -> {
                if (!allowMutableProperties) {
                    return PropertyAdmissionDecision.Demoted.issue(
                        PropertyDemotionReason.MUTABLE_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
                    )
                }
            }

            PropertyMutability.UNKNOWN -> {
                return PropertyAdmissionDecision.Demoted.issue(
                    PropertyDemotionReason.UNKNOWN_MUTABILITY_REJECTED_BY_CONSERVATIVE_RULE,
                )
            }
        }

        when (property.storageKind) {
            PropertyStorageKind.BACKING_FIELD -> Unit

            PropertyStorageKind.LATEINIT -> {
                if (!allowLateinitProperties) {
                    return PropertyAdmissionDecision.Demoted.issue(
                        PropertyDemotionReason.LATEINIT_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
                    )
                }
            }

            PropertyStorageKind.DELEGATED -> {
                if (!allowDelegatedProperties) {
                    return PropertyAdmissionDecision.Demoted.issue(
                        PropertyDemotionReason.DELEGATED_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
                    )
                }
            }

            PropertyStorageKind.COMPUTED -> {
                if (!allowComputedProperties) {
                    return PropertyAdmissionDecision.Demoted.issue(
                        PropertyDemotionReason.COMPUTED_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
                    )
                }
            }

            PropertyStorageKind.UNKNOWN -> {
                return PropertyAdmissionDecision.Demoted.issue(
                    PropertyDemotionReason.UNKNOWN_STORAGE_KIND_REJECTED_BY_CONSERVATIVE_RULE,
                )
            }
        }

        return PropertyAdmissionDecision.Admitted
    }

    private fun isVisibilityAllowed(visibility: VisibilityKind): Boolean =
        when (visibility) {
            VisibilityKind.PUBLIC -> allowPublic
            VisibilityKind.PROTECTED -> allowProtected
            VisibilityKind.INTERNAL -> allowInternal
            VisibilityKind.PRIVATE -> allowPrivate
            VisibilityKind.UNKNOWN -> false
        }

    companion object {
        /**
         * Conservative default profile.
         *
         * It admits only public, declared/inherited, non-synthetic constructors and
         * only read-only backing-field properties.
         */
        @JvmStatic
        fun conservativeDefault(capabilityProfileVersion: Long): CapabilityProfile =
            issue(
                capabilityProfileVersion = capabilityProfileVersion,
                allowPublic = true,
                allowProtected = false,
                allowInternal = false,
                allowPrivate = false,
                allowMutableProperties = false,
                allowLateinitProperties = false,
                allowDelegatedProperties = false,
                allowComputedProperties = false,
                allowAdapterInferredMembers = false,
            )

        @JvmStatic
        fun issue(
            capabilityProfileVersion: Long,
            allowPublic: Boolean,
            allowProtected: Boolean,
            allowInternal: Boolean,
            allowPrivate: Boolean,
            allowMutableProperties: Boolean,
            allowLateinitProperties: Boolean,
            allowDelegatedProperties: Boolean,
            allowComputedProperties: Boolean,
            allowAdapterInferredMembers: Boolean,
        ): CapabilityProfile {
            if (capabilityProfileVersion < 0L) {
                throw ActiveMemberProjectionException(
                    "CapabilityProfile.capabilityProfileVersion must be >= 0: $capabilityProfileVersion",
                )
            }

            return CapabilityProfile(
                capabilityProfileVersion = capabilityProfileVersion,
                allowPublic = allowPublic,
                allowProtected = allowProtected,
                allowInternal = allowInternal,
                allowPrivate = allowPrivate,
                allowMutableProperties = allowMutableProperties,
                allowLateinitProperties = allowLateinitProperties,
                allowDelegatedProperties = allowDelegatedProperties,
                allowComputedProperties = allowComputedProperties,
                allowAdapterInferredMembers = allowAdapterInferredMembers,
            )
        }
    }
}
