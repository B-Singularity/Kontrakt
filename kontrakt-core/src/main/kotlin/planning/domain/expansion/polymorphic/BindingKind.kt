package planning.domain.expansion.polymorphic

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Runtime/discovery binding fact kind.
 *
 * This is not:
 *
 * - a selection mode;
 * - a materialization kind;
 * - a DI container enum;
 * - a reflection artifact;
 * - a cache key;
 * - or a deserialization fallback bucket.
 *
 * BindingKind describes why a requested type is bound to a selected
 * implementation in a run-ratified binding snapshot.
 *
 * Protocol law:
 *
 * - protocolOrder is stable deterministic ordering material.
 * - protocolToken is stable textual protocol material.
 * - enum ordinal must never be used.
 * - protocolOrder is not selection precedence.
 *
 * Unknown-value law:
 *
 * The core domain does not contain UNRECOGNIZED.
 *
 * Unknown protocol tokens/orders are rejected at the boundary by
 * fromProtocolToken(...) or fromProtocolOrder(...). A tolerant external parser
 * may return null before entering the domain, but ratified BindingKind values
 * must always be known.
 *
 * Multiplicity law:
 *
 * Most binding kinds are single-winner facts for a given requested type and
 * binding kind.
 *
 * COLLECTION_ELEMENT is different: multiple selected implementations may
 * lawfully contribute to the same requested collection/plugin surface.
 *
 * Precedence law:
 *
 * Fallback/default/test override precedence is not encoded by protocolOrder.
 * Resolver policy must decide precedence before producing ResolvedBinding.
 */
enum class BindingKind(
    val protocolOrder: Int,
    val protocolToken: String,
    /**
     * Whether multiple selected implementations may coexist for the same
     * requested type and same binding kind.
     *
     * This must remain false for ordinary single-winner bindings.
     */
    val allowsMultipleSelectedImplementations: Boolean,
) {
    /**
     * Host runtime explicitly marked one binding as primary.
     */
    HOST_EXPLICIT_PRIMARY(
        protocolOrder = 10,
        protocolToken = "host_explicit_primary",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * Host runtime explicitly selected a binding by qualifier/key/name.
     */
    HOST_EXPLICIT_QUALIFIER(
        protocolOrder = 20,
        protocolToken = "host_explicit_qualifier",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * Host runtime exposed exactly one visible binding for the requested type.
     */
    HOST_SINGLE_VISIBLE_BINDING(
        protocolOrder = 30,
        protocolToken = "host_single_visible_binding",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * The metamodel/discovery pipeline found exactly one concrete implementation
     * without needing a host binding fact.
     */
    DISCOVERED_SINGLE_IMPLEMENTATION(
        protocolOrder = 40,
        protocolToken = "discovered_single_implementation",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * Requested type is already concrete/materializable and does not require
     * polymorphic lowering.
     */
    IDENTITY_MATERIALIZATION(
        protocolOrder = 50,
        protocolToken = "identity_materialization",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * Default binding used only when no stronger binding is available.
     *
     * This enum does not enforce fallback precedence. The resolver must emit this
     * kind only after stronger candidates have been ruled out.
     */
    FALLBACK_DEFAULT(
        protocolOrder = 60,
        protocolToken = "fallback_default",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * One element in a multi-binding collection/plugin surface.
     *
     * Multiple selected implementations are lawful for the same requested type
     * and this binding kind.
     */
    COLLECTION_ELEMENT(
        protocolOrder = 70,
        protocolToken = "collection_element",
        allowsMultipleSelectedImplementations = true,
    ),

    /**
     * Decorator/proxy/wrapper binding that intentionally wraps another binding.
     *
     * Chain ordering is not encoded here. If multiple decorators are present,
     * the resolver must supply an explicit deterministic chain policy before
     * emitting final bindings.
     */
    DECORATOR_WRAPPER(
        protocolOrder = 80,
        protocolToken = "decorator_wrapper",
        allowsMultipleSelectedImplementations = false,
    ),

    /**
     * Test-run override supplied by the test/runtime ratification boundary.
     *
     * This is intentionally explicit so test overrides do not masquerade as host
     * production bindings.
     */
    TEST_OVERRIDE_STUB(
        protocolOrder = 90,
        protocolToken = "test_override_stub",
        allowsMultipleSelectedImplementations = false,
    ),
    ;

    companion object {
        private const val MAX_PROTOCOL_TOKEN_CHARS: Int = 64
        private const val MIN_PROTOCOL_ORDER: Int = 10
        private const val MAX_PROTOCOL_ORDER: Int = 90

        private val PROTOCOL_ORDERED: Array<BindingKind> =
            arrayOf(
                HOST_EXPLICIT_PRIMARY,
                HOST_EXPLICIT_QUALIFIER,
                HOST_SINGLE_VISIBLE_BINDING,
                DISCOVERED_SINGLE_IMPLEMENTATION,
                IDENTITY_MATERIALIZATION,
                FALLBACK_DEFAULT,
                COLLECTION_ELEMENT,
                DECORATOR_WRAPPER,
                TEST_OVERRIDE_STUB,
            )

        @JvmStatic
        fun protocolOrderedValues(): Array<BindingKind> = PROTOCOL_ORDERED.copyOf()

        /**
         * Strict boundary parser from protocol token.
         *
         * This does not return UNRECOGNIZED. Unknown protocol material must not
         * enter the ratified domain.
         */
        @JvmStatic
        fun fromProtocolToken(protocolToken: String): BindingKind {
            requireProtocolTokenSurface(protocolToken)

            return when (protocolToken) {
                "host_explicit_primary" -> HOST_EXPLICIT_PRIMARY
                "host_explicit_qualifier" -> HOST_EXPLICIT_QUALIFIER
                "host_single_visible_binding" -> HOST_SINGLE_VISIBLE_BINDING
                "discovered_single_implementation" -> DISCOVERED_SINGLE_IMPLEMENTATION
                "identity_materialization" -> IDENTITY_MATERIALIZATION
                "fallback_default" -> FALLBACK_DEFAULT
                "collection_element" -> COLLECTION_ELEMENT
                "decorator_wrapper" -> DECORATOR_WRAPPER
                "test_override_stub" -> TEST_OVERRIDE_STUB
                else -> throw TypeExpansionContractViolationException(
                    reason = "Unknown BindingKind protocol token.",
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
        fun tryFromProtocolToken(protocolToken: String): BindingKind? {
            requireProtocolTokenSurface(protocolToken)

            return when (protocolToken) {
                "host_explicit_primary" -> HOST_EXPLICIT_PRIMARY
                "host_explicit_qualifier" -> HOST_EXPLICIT_QUALIFIER
                "host_single_visible_binding" -> HOST_SINGLE_VISIBLE_BINDING
                "discovered_single_implementation" -> DISCOVERED_SINGLE_IMPLEMENTATION
                "identity_materialization" -> IDENTITY_MATERIALIZATION
                "fallback_default" -> FALLBACK_DEFAULT
                "collection_element" -> COLLECTION_ELEMENT
                "decorator_wrapper" -> DECORATOR_WRAPPER
                "test_override_stub" -> TEST_OVERRIDE_STUB
                else -> null
            }
        }

        /**
         * Strict boundary parser from protocol order.
         *
         * This avoids values().find { ... } and makes the protocol table
         * explicit.
         */
        @JvmStatic
        fun fromProtocolOrder(protocolOrder: Int): BindingKind =
            when (protocolOrder) {
                10 -> HOST_EXPLICIT_PRIMARY
                20 -> HOST_EXPLICIT_QUALIFIER
                30 -> HOST_SINGLE_VISIBLE_BINDING
                40 -> DISCOVERED_SINGLE_IMPLEMENTATION
                50 -> IDENTITY_MATERIALIZATION
                60 -> FALLBACK_DEFAULT
                70 -> COLLECTION_ELEMENT
                80 -> DECORATOR_WRAPPER
                90 -> TEST_OVERRIDE_STUB
                else -> throw TypeExpansionContractViolationException(
                    reason = "Unknown BindingKind protocol order.",
                )
            }

        @JvmStatic
        fun tryFromProtocolOrder(protocolOrder: Int): BindingKind? =
            when (protocolOrder) {
                10 -> HOST_EXPLICIT_PRIMARY
                20 -> HOST_EXPLICIT_QUALIFIER
                30 -> HOST_SINGLE_VISIBLE_BINDING
                40 -> DISCOVERED_SINGLE_IMPLEMENTATION
                50 -> IDENTITY_MATERIALIZATION
                60 -> FALLBACK_DEFAULT
                70 -> COLLECTION_ELEMENT
                80 -> DECORATOR_WRAPPER
                90 -> TEST_OVERRIDE_STUB
                else -> null
            }

        private fun requireProtocolTokenSurface(protocolToken: String) {
            if (protocolToken.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "BindingKind protocol token must not be empty.",
                )
            }

            if (protocolToken.length > MAX_PROTOCOL_TOKEN_CHARS) {
                throw TypeExpansionContractViolationException(
                    reason = "BindingKind protocol token exceeds maximum allowed length.",
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
                        reason = "BindingKind protocol token contains a non-canonical character at index=$index.",
                    )
                }

                index += 1
            }
        }

        @JvmStatic
        fun requireProtocolOrderInRange(protocolOrder: Int) {
            if (protocolOrder < MIN_PROTOCOL_ORDER || protocolOrder > MAX_PROTOCOL_ORDER) {
                throw TypeExpansionContractViolationException(
                    reason = "BindingKind protocol order is outside known protocol range.",
                )
            }
        }
    }
}
