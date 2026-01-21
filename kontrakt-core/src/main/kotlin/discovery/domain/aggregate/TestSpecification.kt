package discovery.domain.aggregate

import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import kotlin.reflect.KClass

/**
 * [Aggregate Root] Test Specification.
 *
 * Represents the blueprint for a specific test execution.
 * It encapsulates "What to test" (Target) and "How to test it" (Mode).
 *
 * This aggregate is created during the Discovery Phase and consumed by the Execution Phase.
 */
@OptIn(ExperimentalStdlibApi::class)
@ConsistentCopyVisibility
data class TestSpecification private constructor(
    val target: DiscoveredTestTarget,
    val modes: Set<TestMode>,
    val requiredDependencies: List<DependencyMetadata>,
    val seed: Long?,
) {
    /**
     * Defines the strategy used to execute the test.
     */
    sealed interface TestMode {
        /**
         * Mode for verifying standard interfaces marked with `@Contract`.
         * Focuses on interface compliance and Liskov Substitution Principle.
         */
        data class ContractAuto(
            val contractInterface: KClass<*>,
        ) : TestMode

        /**
         * Mode for verifying Data Classes / Value Objects annotated with `@DataContract`.
         * Focuses on:
         * 1. Structural Integrity (Constructors).
         * 2. Validation Constraints (Fuzzing).
         * 3. standard Data Contracts (equals, hashCode consistency).
         */
        data class DataCompliance(
            val dataClass: KClass<*>,
        ) : TestMode

        /**
         * Mode for executing user-defined scenarios annotated with `@KontraktTest`.
         */
        data object UserScenario : TestMode
    }

    companion object {
        fun create(
            target: DiscoveredTestTarget,
            modes: Set<TestMode>,
            requiredDependencies: List<DependencyMetadata>,
            seed: Long? = null,
        ): Result<TestSpecification> {
            if (modes.isEmpty()) {
                return Result.failure(IllegalArgumentException("At least one test mode is required."))
            }
            return Result.success(TestSpecification(target, modes, requiredDependencies, seed))
        }
    }
}
