package planning.domain.expansion.polymorphic

/**
 * Planning status produced by polymorphic expansion.
 *
 * This is intentionally not named ContractExecutionStatus.
 *
 * Planning does not execute implementations; it produces executable expansion
 * obligations or deferred planning outcomes.
 *
 * Never use enum ordinal.
 */
enum class PolymorphicExpansionStatus(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    READY(
        protocolOrder = 10,
        protocolToken = "ready",
    ),

    DEFERRED_NO_IMPLEMENTATION(
        protocolOrder = 20,
        protocolToken = "deferred_no_implementation",
    ),
}
