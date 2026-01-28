package metamodel.domain.vo

/**
 * [Value Object] Unique Identifier for a Type (Canonical ID).
 *
 * This ID serves as the **Node Key** within the Type Graph.
 * It ensures identity consistency: even if the JVM runtime instances differ (e.g., due to class reloading),
 * this ID must remain identical for logically equivalent types.
 *
 * @property value The canonical identifier string (e.g., "kotlin.collections.List<kotlin.String>").
 */
@JvmInline
value class TypeId(val value: String) {
    init {
        require(value.isNotBlank()) { "TypeId cannot be blank" }
    }

    override fun toString(): String = value
}