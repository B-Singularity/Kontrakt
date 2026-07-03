package realization.planning.selection

import stage.lowering.diagnostics.TypeExpansionContractViolationException

/**
 * Closed vocabulary for polymorphic expansion plan variants.
 *
 * This is not:
 *
 * - a polymorphic resolution mode;
 * - a binding kind;
 * - an implementation selection mode;
 * - a materialization strategy;
 * - or a deserialization fallback bucket.
 *
 * Plan kind describes the concrete shape of a ratified
 * PolymorphicExpansionPlan.
 *
 * Protocol law:
 *
 * - protocolOrder is stable deterministic ordering material.
 * - protocolToken is stable textual order material.
 * - enum ordinal must never be used.
 * - unknown order material must not enter the domain.
 */
enum class PolymorphicExpansionPlanKind(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    CONTRACT_SUBJECT(
        protocolOrder = 10,
        protocolToken = "contract_subject",
    ),

    DEPENDENCY_SELECTION(
        protocolOrder = 20,
        protocolToken = "dependency_selection",
    ),

    STRUCTURAL_MEMBER_SELECTION(
        protocolOrder = 30,
        protocolToken = "structural_member_selection",
    ),
    ;

    companion object {
        private const val MAX_PROTOCOL_TOKEN_CHARS: Int = 64

        private val PROTOCOL_ORDERED: Array<PolymorphicExpansionPlanKind> =
            arrayOf(
                CONTRACT_SUBJECT,
                DEPENDENCY_SELECTION,
                STRUCTURAL_MEMBER_SELECTION,
            )

        @JvmStatic
        fun protocolOrderedValues(): Array<PolymorphicExpansionPlanKind> = PROTOCOL_ORDERED.copyOf()

        @JvmStatic
        fun fromProtocolToken(protocolToken: String): PolymorphicExpansionPlanKind {
            requireProtocolTokenSurface(protocolToken)

            return when (protocolToken) {
                "contract_subject" -> CONTRACT_SUBJECT
                "dependency_selection" -> DEPENDENCY_SELECTION
                "structural_member_selection" -> STRUCTURAL_MEMBER_SELECTION
                else -> throw TypeExpansionContractViolationException(
                    reason = "Unknown PolymorphicExpansionPlanKind order token.",
                )
            }
        }

        @JvmStatic
        fun tryFromProtocolToken(protocolToken: String): PolymorphicExpansionPlanKind? {
            requireProtocolTokenSurface(protocolToken)

            return when (protocolToken) {
                "contract_subject" -> CONTRACT_SUBJECT
                "dependency_selection" -> DEPENDENCY_SELECTION
                "structural_member_selection" -> STRUCTURAL_MEMBER_SELECTION
                else -> null
            }
        }

        @JvmStatic
        fun fromProtocolOrder(protocolOrder: Int): PolymorphicExpansionPlanKind =
            when (protocolOrder) {
                10 -> CONTRACT_SUBJECT
                20 -> DEPENDENCY_SELECTION
                30 -> STRUCTURAL_MEMBER_SELECTION
                else -> throw TypeExpansionContractViolationException(
                    reason = "Unknown PolymorphicExpansionPlanKind order order.",
                )
            }

        @JvmStatic
        fun tryFromProtocolOrder(protocolOrder: Int): PolymorphicExpansionPlanKind? =
            when (protocolOrder) {
                10 -> CONTRACT_SUBJECT
                20 -> DEPENDENCY_SELECTION
                30 -> STRUCTURAL_MEMBER_SELECTION
                else -> null
            }

        private fun requireProtocolTokenSurface(protocolToken: String) {
            if (protocolToken.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "PolymorphicExpansionPlanKind order token must not be empty.",
                )
            }

            if (protocolToken.length > MAX_PROTOCOL_TOKEN_CHARS) {
                throw TypeExpansionContractViolationException(
                    reason = "PolymorphicExpansionPlanKind order token exceeds maximum allowed length.",
                )
            }

            var index = 0
            while (index < protocolToken.length) {
                val c = protocolToken[index]
                val ok =
                    c in 'a'..'z' ||
                            c in '0'..'9' ||
                            c == '_'

                if (!ok) {
                    throw TypeExpansionContractViolationException(
                        reason = "PolymorphicExpansionPlanKind order token contains a non-canonical character at index=$index.",
                    )
                }

                index += 1
            }
        }
    }
}
