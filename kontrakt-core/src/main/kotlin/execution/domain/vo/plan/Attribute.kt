package execution.domain.vo.plan

import metamodel.domain.vo.MetamodelAnnotationValue

/**
 * Represents a constraint or metadata attached to a plan node.
 */
sealed class Attribute {
    abstract val name: String
    abstract val origin: AttributeOrigin

    data class AnnotationAttribute(
        override val name: String,
        override val origin: AttributeOrigin,
        val values: Map<String, MetamodelAnnotationValue>
    ) : Attribute()
}

/**
 * Meta-data indicating where an attribute came from (e.g., Declaration site vs Use site).
 */
enum class AttributeOrigin {
    TYPE_DECLARATION,   // @Entity class User
    FIELD_DECLARATION,  // @Id val id
    FIELD_TYPE_USE,     // List<@NotNull String>
    MAP_KEY_TYPE_USE,
    MAP_VALUE_TYPE_USE,
    ELEMENT_TYPE_USE
}