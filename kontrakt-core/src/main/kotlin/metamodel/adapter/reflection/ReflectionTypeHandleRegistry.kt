package metamodel.adapter.reflection

import metamodel.domain.exception.StrictModeViolationException
import metamodel.domain.vo.TypeReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType

/**
 * Public wiring surface for adapter-local reflection handles.
 *
 * This is public because public reflection adapter factories/providers need to
 * share one handle registry without exposing an internal type in their public
 * method signatures.
 *
 * This is still adapter-local:
 *
 * - domain code must not depend on it;
 * - planning core must not depend on it;
 * - it is not canonical state;
 * - it is not persisted;
 * - it is not a cache-key authority.
 */
interface ReflectionTypeHandleRegistry {
    fun bind(
        reference: TypeReference,
        kType: KType,
    )

    fun requireKType(
        reference: TypeReference,
    ): KType

    companion object {
        @JvmStatic
        fun issue(): ReflectionTypeHandleRegistry {
            return DefaultReflectionTypeHandleRegistry()
        }
    }
}

private class DefaultReflectionTypeHandleRegistry : ReflectionTypeHandleRegistry {
    private val handles: ConcurrentHashMap<TypeReference, KType> =
        ConcurrentHashMap()

    override fun bind(
        reference: TypeReference,
        kType: KType,
    ) {
        val previous = handles.putIfAbsent(reference, kType)

        if (previous != null && previous != kType) {
            throw StrictModeViolationException(
                "Reflection type handle collision for TypeReference: " +
                        "reference=${reference.renderSummary()}, previous=$previous, actual=$kType",
            )
        }
    }

    override fun requireKType(
        reference: TypeReference,
    ): KType {
        return handles[reference]
            ?: throw StrictModeViolationException(
                "Reflection adapter cannot resolve KType for TypeReference. " +
                        "The reference was not issued by this reflection adapter registry: " +
                        reference.renderSummary(),
            )
    }
}