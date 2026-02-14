package kontrakt.ir

/**
 * [Intent] Defines the execution mode for a specific test target.
 */
sealed interface TestMode {
    /** Executes user-defined scenarios annotated with @Scenario. */
    data object UserScenario : TestMode

    /** Automatically verifies contract compliance for the specified contract types. */
    data class ContractAuto(val contractTypes: List<TypeId>) : TestMode

    /** Verifies data class compliance (e.g., equals/hashCode, copy). */
    data class DataCompliance(val dataContractType: TypeId) : TestMode
}