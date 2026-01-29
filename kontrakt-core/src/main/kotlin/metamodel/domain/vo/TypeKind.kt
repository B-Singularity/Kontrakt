package metamodel.domain.vo

/**
 * [Value Object] The top-level structural classification of a Type.
 *
 * The Generator uses this 'Kind' to determine the primary generation strategy.
 * This enum defines **"What it is" (Structure)**, not **"How to generate it" (Strategy)**.
 */
enum class TypeKind {
    /**
     * An atomic value that cannot be decomposed further.
     * e.g., String, Int, Enum, UUID, Primitives.
     */
    VALUE,

    /**
     * A collection of elements (excluding Maps).
     * e.g., List, Set, Iterable, Collection.
     * Note: Whether it is ordered or unique is determined by the implementation (Generator).
     */
    CONTAINER,

    /**
     * A Key-Value pair structure.
     * e.g., Map, HashMap, Dictionary.
     */
    MAP,

    /**
     * A fixed-size, continuous memory structure.
     * e.g., Array, IntArray.
     */
    ARRAY,

    /**
     * A complex object with properties/fields.
     * e.g., Data Class, POJO.
     */
    STRUCTURAL,

    /**
     * An abstract definition without implementation.
     * e.g., Interface, Abstract Class.
     */
    INTERFACE,

    /**
     * Types that cannot be analyzed or are not supported.
     */
    UNKNOWN
}