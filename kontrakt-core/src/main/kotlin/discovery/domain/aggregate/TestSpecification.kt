package discovery.domain.aggregate

import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
@ConsistentCopyVisibility
data class TestSpecification private constructor(
    val target: DiscoveredTestTarget,
    val modes: Set<TestMode>,
    val requiredDependencies: List<DependencyMetadata>,
) {
    sealed interface TestMode {
        data class ContractAuto(
            val contractInterface: KClass<*>,
        ) : TestMode

        data object UserScenario : TestMode
    }

    companion object {
        fun create(
            target: DiscoveredTestTarget,
            modes: Set<TestMode>,
            requiredDependencies: List<DependencyMetadata>,
        ): Result<TestSpecification> {
            if (modes.isEmpty()) {
                return Result.failure(IllegalArgumentException("At least one test mode is required."))
            }
            return Result.success(TestSpecification(target, modes, requiredDependencies))
        }
    }
}
