package stage.lowering.material.polymorphic

import stage.lowering.diagnostics.TypeExpansionContractViolationException

/**
 * Implementation selection mode.
 *
 * This is not:
 *
 * - a binding kind;
 * - a DI container strategy;
 * - a materialization kind;
 * - a runtime proxy strategy;
 * - a policy admission mode;
 * - or a deserialization fallback bucket.
 *
 * BindingKind explains why a binding fact exists.
 * ImplementationSelectionMode explains how the selected implementation relates
 * to the requested type after binding/discovery has already been ratified.
 *
 * Protocol law:
 *
 * - protocolOrder is stable deterministic ordering material.
 * - protocolToken is stable textual order material.
 * - enum ordinal must never be used.
 * - values().find { ... } must not be used for order lookup.
 *
 * Unknown-value law:
 *
 * The core domain does not contain UNRECOGNIZED.
 *
 * Unknown order tokens/orders are rejected at the boundary by
 * fromProtocolToken(...) or fromProtocolOrder(...). A tolerant external parser
 * may call tryFromProtocolToken(...) / tryFromProtocolOrder(...) before entering
 * the ratified domain.
 *
 * Conservative vocabulary law:
 *
 * Do not add modes for policy states that do not produce an implementation
 * selection.
 *
 * For example, "no polymorphism allowed" is an expansion policy/admission rule,
 * not a final implementation selection mode.
 *
 * Do not add modes for materialization mechanics.
 *
 * For example, proxy/lazy/delegating implementation should be represented by
 * materialization strategy/kind, not by selection mode, unless a later ADR
 * explicitly changes this boundary.
 */
enum class ImplementationSelectionMode(
    val protocolOrder: Int,
    val protocolToken: String,
) {
    /**
     * The requested abstract/interface/polymorphic surface was lowered to a
     * distinct selected implementation.
     */
    STRICT_POLYMORPHIC_LOWERING(
        protocolOrder = 10,
        protocolToken = "strict_polymorphic_lowering",
    ),

    /**
     * The requested type is already the selected implementation.
     *
     * This is used for concrete/materializable requested types where no
     * polymorphic lowering is needed.
     */
    IDENTITY_MATERIALIZATION(
        protocolOrder = 20,
        protocolToken = "identity_materialization",
    ),
    ;

    companion object {
        private const val MAX_PROTOCOL_TOKEN_CHARS: Int = 64
        private const val MIN_PROTOCOL_ORDER: Int = 10
        private const val MAX_PROTOCOL_ORDER: Int = 20

        private val PROTOCOL_ORDERED: Array<ImplementationSelectionMode> =
            arrayOf(
                STRICT_POLYMORPHIC_LOWERING,
                IDENTITY_MATERIALIZATION,
            )

        /**
         * Deterministic order-order iteration.
         *
         * Returns a defensive copy so callers cannot mutate the internal table.
         */
        @JvmStatic
        fun protocolOrderedValues(): Array<ImplementationSelectionMode> = PROTOCOL_ORDERED.copyOf()

        /**
         * Strict boundary parser from order token.
         *
         * This does not return UNRECOGNIZED. Unknown order material must not
         * enter the ratified domain.
         */
        @JvmStatic
        fun fromProtocolToken(protocolToken: String): ImplementationSelectionMode {
            requireProtocolTokenSurface(protocolToken)

            return when (protocolToken) {
                "strict_polymorphic_lowering" -> STRICT_POLYMORPHIC_LOWERING
                "identity_materialization" -> IDENTITY_MATERIALIZATION
                else -> throw TypeExpansionContractViolationException(
                    reason = "Unknown ImplementationSelectionMode order token.",
                )
            }
        }

        /**
         * Tolerant boundary parser.
         *
         * Use this before entering the ratified domain if an adapter needs to
         * report unknown external data without throwing immediately.
         */
        @JvmStatic
        fun tryFromProtocolToken(protocolToken: String): ImplementationSelectionMode? {
            requireProtocolTokenSurface(protocolToken)

            return when (protocolToken) {
                "strict_polymorphic_lowering" -> STRICT_POLYMORPHIC_LOWERING
                "identity_materialization" -> IDENTITY_MATERIALIZATION
                else -> null
            }
        }

        /**
         * Strict boundary parser from order order.
         *
         * This avoids values().find { ... } and keeps the order table
         * explicit.
         */
        @JvmStatic
        fun fromProtocolOrder(protocolOrder: Int): ImplementationSelectionMode =
            when (protocolOrder) {
                10 -> STRICT_POLYMORPHIC_LOWERING
                20 -> IDENTITY_MATERIALIZATION
                else -> throw TypeExpansionContractViolationException(
                    reason = "Unknown ImplementationSelectionMode order order.",
                )
            }

        @JvmStatic
        fun tryFromProtocolOrder(protocolOrder: Int): ImplementationSelectionMode? =
            when (protocolOrder) {
                10 -> STRICT_POLYMORPHIC_LOWERING
                20 -> IDENTITY_MATERIALIZATION
                else -> null
            }

        @JvmStatic
        fun requireProtocolOrderInRange(protocolOrder: Int) {
            if (protocolOrder < MIN_PROTOCOL_ORDER || protocolOrder > MAX_PROTOCOL_ORDER) {
                throw TypeExpansionContractViolationException(
                    reason = "ImplementationSelectionMode order order is outside known order range.",
                )
            }
        }

        private fun requireProtocolTokenSurface(protocolToken: String) {
            if (protocolToken.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "ImplementationSelectionMode order token must not be empty.",
                )
            }

            if (protocolToken.length > MAX_PROTOCOL_TOKEN_CHARS) {
                throw TypeExpansionContractViolationException(
                    reason = "ImplementationSelectionMode order token exceeds maximum allowed length.",
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
                        reason = "ImplementationSelectionMode order token contains a non-canonical character at index=$index.",
                    )
                }

                index += 1
            }
        }
    }
}
