package planning.infrastructure.runtime.policy

/**
 * High-level external control surface for runtime policy resolution.
 *
 * This surface should remain small and stable.
 * Internal budget-allocation knobs are intentionally hidden behind the resolver.
 */
enum class ResourceProfile {
    AUTO,
    SMALL,
    STANDARD,
    LARGE,
}
