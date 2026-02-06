package metamodel.domain.vo

/**
 * [Taxonomy] High-level classification of types for structural planning.
 * Used by the Planner to determine the traversal strategy.
 */
enum class TypeKind {
    /**
     * Leaf nodes (e.g. String, Int, UUID, Enum).
     * No further traversal is needed; handled by simple generators.
     */
    ATOMIC,

    /**
     * Complex objects composed of fields (e.g. Class, Data Class).
     * Requires traversal of properties.
     */
    COMPOSITE,

    /**
     * Linear containers (e.g. List, Set).
     * Requires traversal of the element type.
     */
    COLLECTION,

    /**
     * Fixed-size memory chunks (e.g. Array).
     * Similar to COLLECTION but with fixed size constraints.
     */
    ARRAY,

    /**
     * Key-Value pairs (e.g. Map).
     * Requires traversal of both key and value types.
     */
    MAP,

    /**
     * Polymorphic types (e.g. Interface, Abstract Class).
     * Requires resolution to a concrete implementation type before linking.
     */
    INTERFACE
}