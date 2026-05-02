package planning.domain.fault

/**
 * Tier-2 fault categories exposed to the Domain Core.
 *
 * Contract:
 * - TRANSIENT:
 *   The caller degrades to a cache miss and performs local build.
 *
 * - CIRCUIT_OPEN:
 *   The caller bypasses Tier-2 for the remainder of the current session.
 *
 * No diagnostic payload is allowed here by policy.
 */
enum class L2FaultKind {
    /**
     * Temporary failure or bounded-wait degradation.
     *
     * Examples:
     * - join timeout / poll threshold exhaustion
     * - unexpected publication exception
     * - benign race that requires miss-degrade
     */
    TRANSIENT,

    /**
     * Governance failure that opens or seals the affected Tier-2 scope.
     *
     * Examples:
     * - partition capacity breach
     * - primitive routing table saturation
     * - administrative partition close / bulk drop
     */
    CIRCUIT_OPEN,
}
