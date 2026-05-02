package planning.domain.projection

/**
 * Structured reason vocabulary for constructor rejection during Core-owned selection.
 *
 * This replaces stringly-typed rejection evidence.
 *
 * These reasons belong to Planning Core semantic choice, not to the metamodel
 * adapter boundary. The adapter reports facts; the Core rejects candidates under
 * the active CapabilityProfile.
 */
enum class ConstructorRejectionReason {
    NOT_VISIBLE_UNDER_CAPABILITY_PROFILE,
    SYNTHETIC_CONSTRUCTOR,
    UNKNOWN_VISIBILITY_REJECTED_BY_CONSERVATIVE_RULE,
    UNKNOWN_ORIGIN_REJECTED_BY_CONSERVATIVE_RULE,
    ADAPTER_INFERRED_REJECTED_BY_CAPABILITY_PROFILE,
}
