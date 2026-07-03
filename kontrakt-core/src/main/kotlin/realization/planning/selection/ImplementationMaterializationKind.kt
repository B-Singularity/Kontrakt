package realization.planning.selection

/**
 * Materialization capability for a concrete implementation reference.
 *
 * This is supplied by the provider after consulting normalized type shape.
 * TypeReference alone is not enough to prove concreteness.
 *
 * Never use enum ordinal.
 */
enum class ImplementationMaterializationKind(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    CONCRETE_CONSTRUCTOR(
        protocolOrder = 10,
        protocolToken = "concrete_constructor",
    ),

    CONCRETE_OBJECT_SINGLETON(
        protocolOrder = 20,
        protocolToken = "concrete_object_singleton",
    ),
}
