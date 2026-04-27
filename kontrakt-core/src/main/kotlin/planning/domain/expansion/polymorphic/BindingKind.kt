package planning.domain.expansion.polymorphic

enum class BindingKind(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    HOST_EXPLICIT_PRIMARY(
        protocolOrder = 10,
        protocolToken = "host_explicit_primary",
    ),

    HOST_EXPLICIT_QUALIFIER(
        protocolOrder = 20,
        protocolToken = "host_explicit_qualifier",
    ),

    HOST_SINGLE_VISIBLE_BINDING(
        protocolOrder = 30,
        protocolToken = "host_single_visible_binding",
    ),

    DISCOVERED_SINGLE_IMPLEMENTATION(
        protocolOrder = 40,
        protocolToken = "discovered_single_implementation",
    ),

    IDENTITY_MATERIALIZATION(
        protocolOrder = 50,
        protocolToken = "identity_materialization",
    ),
}