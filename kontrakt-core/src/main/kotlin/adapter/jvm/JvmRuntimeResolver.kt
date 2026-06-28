package adapter.jvm

import execution.port.outgoing.RuntimeTypeHandle
import execution.port.outgoing.RuntimeTypeResolver
import infrastructure.exception.RegistryException
import infrastructure.registry.RuntimeHandleRegistry
import stage.canonicalization.material.TypeReference
import java.lang.reflect.Array
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * [Adapter] JVM implementation of RuntimeTypeResolver.
 * Consumes payloads from the Registry and converts them to [JvmClassHandle].
 */
class JvmRuntimeResolver(
    private val registry: RuntimeHandleRegistry,
) : RuntimeTypeResolver {
    override fun resolveHandle(type: TypeReference): RuntimeTypeHandle {
        try {
            val payload =
                registry.get(type)
                    ?: throw RuntimeResolutionException(
                        "Missing payload for '${type.cycleId}'. Check Discovery phase.",
                        cycleId = type.cycleId,
                    )

            val javaType =
                payload as? Type
                    ?: throw RuntimeResolutionException(
                        "Invalid payload type: ${payload::class.java.name}. Expected java.lang.reflect.Type.",
                        cycleId = type.cycleId,
                    )

            val rawClass = resolveRawClass(javaType)
            return JvmClassHandle(rawClass)
        } catch (e: RegistryException) {
            // Translate Infrastructure exception to Domain exception to preserve layers
            throw RuntimeResolutionException(
                message = "Registry Access Failed",
                cycleId = type.cycleId,
                cause = e,
            )
        }
    }

    private fun resolveRawClass(type: Type): Class<*> =
        when (type) {
            is Class<*> -> type
            is ParameterizedType -> type.rawType as Class<*>
            is GenericArrayType -> {
                val componentRaw = resolveRawClass(type.genericComponentType)
                Array
                    .newInstance(componentRaw, 0)
                    .javaClass
            }

            else -> throw RuntimeResolutionException("Unsupported Runtime Type: $type")
        }
}

data class JvmClassHandle(
    val clazz: Class<*>,
) : RuntimeTypeHandle
