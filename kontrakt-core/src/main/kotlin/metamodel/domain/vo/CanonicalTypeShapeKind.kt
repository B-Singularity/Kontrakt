package metamodel.domain.vo

/**
 * Lowered structural kind of a canonical type reference.
 *
 * This is not the same surface as planning.domain.vo.TypeKind.
 *
 * CanonicalTypeShapeKind is a compact metamodel summary used to avoid reparsing
 * canonical type text in planning hot paths. It answers "what structural family
 * is this type reference in?", not "how should the planner fully expand it?".
 *
 * Detailed expansion metadata still belongs to ResolvedTypeShape and later
 * TypeExpansionDecision surfaces.
 *
 * Never use enum ordinal.
 */
enum class CanonicalTypeShapeKind(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    /**
     * No runtime value.
     *
     * Used for Java void / no-result type surfaces.
     * This must never be confused with Kotlin Unit.
     */
    VOID(
        protocolOrder = 5,
        protocolToken = "void",
    ),

    /**
     * Kotlin Unit-like terminal value.
     *
     * Unit has a value-level semantic unlike Java void, but it remains terminal
     * for planning graph expansion.
     */
    UNIT(
        protocolOrder = 6,
        protocolToken = "unit",
    ),

    /**
     * Primitive/string/number/temporal/UUID leaf.
     *
     * User-defined domain leaf/value-object classification is represented by
     * TypeShapeSummary.atomicFamily, not by changing this enum.
     */
    ATOMIC(
        protocolOrder = 10,
        protocolToken = "atomic",
    ),

    /**
     * Closed value set.
     */
    ENUM(
        protocolOrder = 15,
        protocolToken = "enum",
    ),

    /**
     * Instantiable object-like structure with projected active members.
     */
    COMPOSITE(
        protocolOrder = 20,
        protocolToken = "composite",
    ),

    /**
     * Pure interface contract surface.
     */
    INTERFACE(
        protocolOrder = 30,
        protocolToken = "interface",
    ),

    /**
     * Abstract class surface.
     */
    ABSTRACT_CLASS(
        protocolOrder = 35,
        protocolToken = "abstract_class",
    ),

    /**
     * Container with one element role.
     */
    COLLECTION(
        protocolOrder = 40,
        protocolToken = "collection",
    ),

    /**
     * Array with one component role and one or more dimensions.
     */
    ARRAY(
        protocolOrder = 50,
        protocolToken = "array",
    ),

    /**
     * Container with key/value roles.
     */
    MAP(
        protocolOrder = 60,
        protocolToken = "map",
    );

    val isPolymorphicSurface: Boolean
        get() = this == INTERFACE || this == ABSTRACT_CLASS

    val isTerminalLeaf: Boolean
        get() = this == VOID ||
                this == UNIT ||
                this == ATOMIC ||
                this == ENUM

    val isContainerSurface: Boolean
        get() = this == COLLECTION ||
                this == ARRAY ||
                this == MAP
}