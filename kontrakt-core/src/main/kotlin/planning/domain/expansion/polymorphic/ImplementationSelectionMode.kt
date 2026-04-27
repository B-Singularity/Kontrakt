package planning.domain.expansion.polymorphic

enum class ImplementationSelectionMode(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    STRICT_POLYMORPHIC_LOWERING(
        protocolOrder = 10,
        protocolToken = "strict_polymorphic_lowering",
    ),

    IDENTITY_MATERIALIZATION(
        protocolOrder = 20,
        protocolToken = "identity_materialization",
    ),
}