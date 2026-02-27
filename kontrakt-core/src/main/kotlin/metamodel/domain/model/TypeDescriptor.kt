package metamodel.domain.model

import metamodel.domain.vo.AnnotationDescriptor
import metamodel.domain.vo.TypeKind
import metamodel.domain.vo.TypeReference

/**
 * Resolved metadata for a type, acting as the structural blueprint.
 */
data class TypeDescriptor(
    val kind: TypeKind,
    val name: String,

    /**
     * The nullability of the type at the usage site.
     * This is the Single Source of Truth for ADR-027 Rule #1 (Nullable Truncation).
     */
    val isNullable: Boolean,

    val annotations: List<AnnotationDescriptor>,
    val properties: List<PropertyDescriptor> = emptyList(),

    // Container specifics
    val elementType: TypeReference? = null,
    val keyType: TypeReference? = null,
    val valueType: TypeReference? = null,
    val componentType: TypeReference? = null
)

data class PropertyDescriptor(
    val name: String,
    val type: TypeReference,
    val annotations: List<AnnotationDescriptor>,

    /**
     * Metadata indicating the origin of the property.
     * Essential for the Planner to determine the correct EdgeModel.
     */
    val source: PropertySource
)

enum class PropertySource {
    FIELD,
    CONSTRUCTOR_PARAMETER,
    METHOD
}