package discovery.domain.vo

import kotlin.reflect.KClass

@OptIn(ExperimentalStdlibApi::class)
@ConsistentCopyVisibility
data class DiscoveredTestTarget private constructor(
    val kClass: KClass<*>,
    val displayName: String,
    val fullyQualifiedName: String,
) {
    companion object {
        fun create(
            kClass: KClass<*>,
            displayName: String,
            fullyQualifiedName: String,
        ): Result<DiscoveredTestTarget> {
            if (displayName.isBlank()) {
                return Result.failure(IllegalArgumentException("Display name cannot be blank"))
            }
            if (fullyQualifiedName.isBlank()) {
                return Result.failure(IllegalArgumentException("Fully qualified name cannot be blank"))
            }
            return Result.success(DiscoveredTestTarget(kClass, displayName, fullyQualifiedName))
        }
    }
}
