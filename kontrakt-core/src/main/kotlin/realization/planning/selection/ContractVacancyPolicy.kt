package realization.planning.selection

/**
 * Policy for contract-subject interfaces with no concrete implementations.
 *
 * Never use enum ordinal.
 */
enum class ContractVacancyPolicy(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    WARN_AND_DEFER(
        protocolOrder = 10,
        protocolToken = "warn_and_defer",
    ),

    FAIL_STRICT(
        protocolOrder = 20,
        protocolToken = "fail_strict",
    ),
}
