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
     * Kotlin Unit-like value.
     *
     * Unit has a value-level semantic unlike Java void, but it remains terminal
     * for planning purposes.
     */
    UNIT(
        protocolOrder = 6,
        protocolToken = "unit",
    ),

    /**
     * Primitive/string/number/temporal/UUID/value-object leaf.
     *
     * ENUM is intentionally not hidden here. Enum is a closed set and receives
     * its own kind.
     */
    ATOMIC(
        protocolOrder = 10,
        protocolToken = "atomic",
    ),

    /**
     * Closed value set.
     *
     * Enum is leaf-like in object graph expansion, but it carries contract
     * material that ordinary atomic values do not: allowed constants, constant
     * ordering, and future enum-constant metadata.
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
     *
     * Abstract class is related to polymorphic expansion, but it is not identical
     * to interface. It may have constructors, state, partial implementation, or
     * subclass constraints.
     */
    ABSTRACT_CLASS(
        protocolOrder = 35,
        protocolToken = "abstract_class",
    ),

    /**
     * Container with one element type.
     */
    COLLECTION(
        protocolOrder = 40,
        protocolToken = "collection",
    ),

    /**
     * Array with one component type and a rank.
     */
    ARRAY(
        protocolOrder = 50,
        protocolToken = "array",
    ),

    /**
     * Container with key/value roles.
     *
     * The summary records arity only. Key/value role details belong to
     * ResolvedTypeShape / MapExpansionPlan.
     */
    MAP(
        protocolOrder = 60,
        protocolToken = "map",
    ),
}