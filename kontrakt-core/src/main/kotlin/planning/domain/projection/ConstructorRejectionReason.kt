package planning.domain.projection

/**
 * Structured reason vocabulary for constructor rejection during Core-owned selection.
 *
 * This replaces stringly-typed rejection evidence.
 */
enum class ConstructorRejectionReason {
    NOT_VISIBLE_UNDER_CAPABILITY_PROFILE,
    SYNTHETIC_CONSTRUCTOR,
    UNKNOWN_VISIBILITY_REJECTED_BY_CONSERVATIVE_RULE,
    UNKNOWN_ORIGIN_REJECTED_BY_CONSERVATIVE_RULE,
}