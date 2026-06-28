package stage.lowering.material.projection

/**
 * Structured reason vocabulary for property demotion during Core-owned projection.
 *
 * Demotion means:
 * - the property was present in raw metamodel facts,
 * - but it is not admitted into the Active Member Set under the active
 *   CapabilityProfile and conservative unknown-fact rules.
 */
enum class PropertyDemotionReason {
    NOT_VISIBLE_UNDER_CAPABILITY_PROFILE,
    SYNTHETIC_PROPERTY,
    MUTABLE_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
    DELEGATED_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
    COMPUTED_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
    LATEINIT_PROPERTY_REJECTED_BY_CAPABILITY_PROFILE,
    UNKNOWN_VISIBILITY_REJECTED_BY_CONSERVATIVE_RULE,
    UNKNOWN_ORIGIN_REJECTED_BY_CONSERVATIVE_RULE,
    UNKNOWN_MUTABILITY_REJECTED_BY_CONSERVATIVE_RULE,
    UNKNOWN_STORAGE_KIND_REJECTED_BY_CONSERVATIVE_RULE,
    ADAPTER_INFERRED_REJECTED_BY_CAPABILITY_PROFILE,
}
