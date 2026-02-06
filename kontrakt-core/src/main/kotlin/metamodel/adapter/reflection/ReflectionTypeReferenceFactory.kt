package metamodel.adapter.reflection

import infrastructure.registry.RuntimeHandleRegistry
import metamodel.domain.exception.DeterminismViolationException
import metamodel.domain.exception.StrictModeViolationException
import metamodel.domain.vo.AnnotationDescriptor
import metamodel.domain.vo.TypeReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

/**
 * [Adapter] Factory for creating [TypeReference]s via Java Reflection.
 *
 * ## Responsibilities
 * 1. **Strict Validation**: Rejects unsupported types (Wildcards, TypeVariables).
 * 2. **Determinism**: Enforces sorting of annotations to ensure consistent behavior.
 * 3. **Normalization**: Converts platform-specific names (e.g., '$') to domain standards.
 * 4. **Registration**: Registers the raw JVM type payload into the [RuntimeHandleRegistry].
 */
class ReflectionTypeReferenceFactory(
    private val registry: RuntimeHandleRegistry
) {

    fun create(type: Type, useSiteAnnotations: List<AnnotationDescriptor> = emptyList()): TypeReference {
        // 1. Strict Mode: Fail fast on unresolved generics
        if (type is TypeVariable<*> || type is WildcardType) {
            throw StrictModeViolationException(
                "Strict Mode: Unresolved Generics (T, ?) are not supported. Type: $type"
            )
        }

        // 2. Determinism: Reject duplicates as Reflection order is undefined
        val distinctNames = useSiteAnnotations.map { it.qualifiedName }.toSet()
        if (distinctNames.size != useSiteAnnotations.size) {
            throw DeterminismViolationException(
                "Duplicate annotations found. Reflection cannot guarantee order for duplicates."
            )
        }

        // 3. Determinism: Sort annotations by name
        val sortedAnnotations = useSiteAnnotations.sortedBy { it.qualifiedName }

        val typeName = type.typeName

        // 4. Normalize CycleId ($ -> .)
        val normalizedId = typeName.replace('$', '.')

        val ref = ReflectionTypeReferenceImpl(
            id = normalizedId,
            cycleId = normalizedId, // Java Types imply non-nullable signature
            signature = computeSafeSignature(type),
            useSiteAnnotations = sortedAnnotations
        )

        // 5. Register Payload (Run-Scope)
        registry.register(ref, type)

        return ref
    }

    /**
     * Generates a safe signature string handling generics.
     */
    private fun computeSafeSignature(type: Type): String {
        return when (type) {
            is Class<*> -> type.simpleName
            is ParameterizedType -> {
                val raw = (type.rawType as Class<*>).simpleName
                val args = type.actualTypeArguments.joinToString(", ") { computeSafeSignature(it) }
                "$raw<$args>"
            }

            else -> type.typeName
        }
    }

    private data class ReflectionTypeReferenceImpl(
        override val id: String,
        override val cycleId: String,
        override val signature: String,
        override val useSiteAnnotations: List<AnnotationDescriptor>
    ) : TypeReference
}