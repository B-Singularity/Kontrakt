package realization.planning.selection

/**
 * Semantic role of an interface / abstract type reference.
 *
 * This mode must be propagated explicitly through expansion context.
 * It must not be inferred from TypeReference alone.
 *
 * Never use enum ordinal.
 *
 * protocolOrder is a ratified internal order for deterministic order surfaces
 * that need to compare modes.
 *
 * protocolToken is the stable canonical token used by canonical encoding.
 */
enum class PolymorphicResolutionMode(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    CONTRACT_SUBJECT(
        protocolOrder = 10,
        protocolToken = "contract_subject",
    ),

    DEPENDENCY_SITE(
        protocolOrder = 20,
        protocolToken = "dependency_site",
    ),

    STRUCTURAL_MEMBER(
        protocolOrder = 30,
        protocolToken = "structural_member",
    ),
}
