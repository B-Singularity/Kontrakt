package discovery.domain.vo

import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
@ConsistentCopyVisibility
data class DependencyMetadata private constructor(
    val name: String,
    val type: KClass<*>,
) {
    companion object {
        fun create(
            name: String,
            type: KClass<*>,
        ): Result<DependencyMetadata> {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be blank"))
            }
            return Result.success(DependencyMetadata(name, type))
        }
    }
}
