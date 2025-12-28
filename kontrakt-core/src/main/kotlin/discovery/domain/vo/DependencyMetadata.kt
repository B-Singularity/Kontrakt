package discovery.domain.vo

import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
@ConsistentCopyVisibility
data class DependencyMetadata private constructor(
    val name: String,
    val type: KClass<*>,
    val strategy: MockingStrategy,
) {
    sealed interface MockingStrategy {
        data object StatelessMock : MockingStrategy

        data object StatefulFake : MockingStrategy

        data class Environment(
            val type: EnvType,
        ) : MockingStrategy

        data class Real(val implementation: KClass<*>) : MockingStrategy
    }

    enum class EnvType {
        TIME,
        SECURITY,
    }

    companion object {
        fun create(
            name: String,
            type: KClass<*>,
            strategy: MockingStrategy,
        ): Result<DependencyMetadata> {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be blank"))
            }
            return Result.success(DependencyMetadata(name, type, strategy))
        }
    }
}
