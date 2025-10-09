package discovery.domain.vo

import kotlin.reflect.KClass

data class DependencyMetadata(
    val name: String,
    val type: KClass<*>
)
