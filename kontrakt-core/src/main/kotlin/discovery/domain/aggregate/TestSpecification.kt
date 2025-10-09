package discovery.domain.aggregate

import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget

data class TestSpecification private constructor(
    val target: DiscoveredTestTarget,
    val requiredDependencies: List<DependencyMetadata>
) {
    companion object {
        fun create(
            target: DiscoveredTestTarget,
            requiredDependencies: List<DependencyMetadata>
        ): Result<TestSpecification> {
            return Result.success(TestSpecification(target, requiredDependencies))
        }
    }
}