package stage.canonicalization.material.representation

/**
 * Lowered structural family of a canonical type reference.
 *
 * This is not planning.domain.vo.TypeKind.
 *
 * CanonicalTypeShapeKind is a compact metamodel summary used to avoid reparsing
 * canonical type text and to prevent high-level surface ambiguity before the
 * heavier ResolvedTypeShape / ExpansionPlan stages.
 *
 * It answers:
 *
 *     "Which structural family does this canonical type identity belong to?"
 *
 * It does not answer:
 *
 *     "Which exact TypeExpansionDecision should be executed?"
 *     "Which constructor should be called?"
 *     "Which sealed subtype should be selected?"
 *     "Which collection element policy should be used?"
 *
 * Detailed execution material still belongs to:
 *
 * - ResolvedTypeShape,
 * - PolymorphicExpansionPlan,
 * - CollectionExpansionPlan,
 * - ArrayExpansionPlan,
 * - MapExpansionPlan,
 * - and TypeExpansionDecision.
 *
 * Field law:
 *
 * - expansionSurface is the coarse execution family.
 * - minimumGenericArity is a order lower bound, not a full generic schema.
 * - directInstantiationAllowed prevents abstract/sealed/interface surfaces from
 *   accidentally entering constructor-selection paths.
 * - finiteSubtypeUniverse marks surfaces whose subtype/value universe is closed
 *   enough to be ratified by metamodel/KSP material.
 *
 * Never use enum ordinal. protocolOrder is the only stable ordering surface.
 */
enum class CanonicalTypeShapeKind(
    val protocolOrder: Int,
    val protocolToken: String,
    val expansionSurface: CanonicalExpansionSurface,
    val minimumGenericArity: Int,
    val directInstantiationAllowed: Boolean,
    val finiteSubtypeUniverse: Boolean,
) {
    /**
     * No runtime value.
     *
     * Java void / no-result surface. Must never be confused with Kotlin Unit.
     */
    VOID(
        protocolOrder = 5,
        protocolToken = "void",
        expansionSurface = CanonicalExpansionSurface.TERMINAL,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Kotlin Unit-like terminal value.
     */
    UNIT(
        protocolOrder = 6,
        protocolToken = "unit",
        expansionSurface = CanonicalExpansionSurface.TERMINAL,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Primitive/string/number/temporal/UUID/domain-leaf surface.
     *
     * The mutually-exclusive atomic family is carried by TypeShapeSummary.
     */
    ATOMIC(
        protocolOrder = 10,
        protocolToken = "atomic",
        expansionSurface = CanonicalExpansionSurface.TERMINAL,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Closed value set.
     *
     * Enum is terminal from the shape-summary perspective, but its value universe
     * is finite and may be used later by deterministic value materialization.
     */
    ENUM(
        protocolOrder = 15,
        protocolToken = "enum",
        expansionSurface = CanonicalExpansionSurface.TERMINAL,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = true,
    ),

    /**
     * Instantiable object-like structure with projected active members.
     *
     * This is the only ordinary object-like surface that may enter direct
     * structural member projection and constructor selection.
     */
    COMPOSITE(
        protocolOrder = 20,
        protocolToken = "composite",
        expansionSurface = CanonicalExpansionSurface.STRUCTURAL,
        minimumGenericArity = 0,
        directInstantiationAllowed = true,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Open interface contract / dependency surface.
     *
     * Interface expansion must be handled by polymorphic resolution law, not by
     * direct constructor selection.
     */
    INTERFACE(
        protocolOrder = 30,
        protocolToken = "interface",
        expansionSurface = CanonicalExpansionSurface.POLYMORPHIC,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Sealed interface surface.
     *
     * This is explicitly separate from INTERFACE because the subtype universe is
     * finite and should be resolved through sealed-hierarchy material rather
     * than open runtime discovery alone.
     */
    SEALED_INTERFACE(
        protocolOrder = 32,
        protocolToken = "sealed_interface",
        expansionSurface = CanonicalExpansionSurface.POLYMORPHIC,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = true,
    ),

    /**
     * Abstract class surface.
     *
     * Abstract classes are polymorphic surfaces. They must not be treated as
     * ordinary COMPOSITE values merely because they may carry constructor-like
     * metadata.
     */
    ABSTRACT_CLASS(
        protocolOrder = 35,
        protocolToken = "abstract_class",
        expansionSurface = CanonicalExpansionSurface.POLYMORPHIC,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Sealed class surface.
     *
     * This is intentionally separate from ABSTRACT_CLASS and COMPOSITE:
     *
     * - unlike COMPOSITE, the sealed class itself is not directly instantiated;
     * - unlike open ABSTRACT_CLASS, the subtype universe is finite and can be
     *   ratified by the metamodel adapter / KSP adapter.
     */
    SEALED_CLASS(
        protocolOrder = 37,
        protocolToken = "sealed_class",
        expansionSurface = CanonicalExpansionSurface.POLYMORPHIC,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = true,
    ),

    /**
     * Iterable container with at least one element role.
     *
     * Generic arity is not fixed to one because custom iterable/container types
     * may carry additional type parameters while still exposing one primary
     * element role at the expansion-plan boundary.
     */
    COLLECTION(
        protocolOrder = 40,
        protocolToken = "collection",
        expansionSurface = CanonicalExpansionSurface.CONTAINER,
        minimumGenericArity = 1,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Array with one component role and one or more dimensions.
     *
     * The array type itself has genericArity = 0. Generic component information
     * must be represented through TypeShapeSummary component hints and later
     * verified by ArrayExpansionPlan.
     */
    ARRAY(
        protocolOrder = 50,
        protocolToken = "array",
        expansionSurface = CanonicalExpansionSurface.CONTAINER,
        minimumGenericArity = 0,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),

    /**
     * Map-like container with at least key/value roles.
     *
     * Generic arity is not fixed to two because custom maps/multimaps may expose
     * extra generic roles while still lowering through a map expansion plan.
     */
    MAP(
        protocolOrder = 60,
        protocolToken = "map",
        expansionSurface = CanonicalExpansionSurface.CONTAINER,
        minimumGenericArity = 2,
        directInstantiationAllowed = false,
        finiteSubtypeUniverse = false,
    ),
    ;

    val isPolymorphicSurface: Boolean
        get() = expansionSurface == CanonicalExpansionSurface.POLYMORPHIC

    val isTerminalLeaf: Boolean
        get() = expansionSurface == CanonicalExpansionSurface.TERMINAL

    val isContainerSurface: Boolean
        get() = expansionSurface == CanonicalExpansionSurface.CONTAINER

    val isSealedSurface: Boolean
        get() = this == SEALED_CLASS || this == SEALED_INTERFACE

    val requiresPolymorphicResolution: Boolean
        get() = isPolymorphicSurface

    val canEnterStructuralProjectionDirectly: Boolean
        get() = expansionSurface == CanonicalExpansionSurface.STRUCTURAL && directInstantiationAllowed
}

/**
 * Coarse expansion surface bound at the metamodel summary level.
 *
 * This is still not a full expansion decision. It exists to prevent accidental
 * COMPOSITE / ABSTRACT / SEALED / INTERFACE drift from remaining invisible until
 * deep inside StructuralPlannerCore.
 */
enum class CanonicalExpansionSurface(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    TERMINAL(10, "terminal"),
    STRUCTURAL(20, "structural"),
    POLYMORPHIC(30, "polymorphic"),
    CONTAINER(40, "container"),
}
