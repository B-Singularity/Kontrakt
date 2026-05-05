package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelAdapterAssemblyException
import metamodel.domain.exception.MetamodelAdapterStateViolationException
import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Reflection-adapter protocol token guard.
 *
 * This is a thin adapter-layer wrapper around the shared metamodel protocol
 * text guards.
 *
 * Why this exists:
 *
 * - MetamodelProtocolTextGuards is the shared protocol law.
 * - Reflection adapter assembly wants assembly-blame diagnostics.
 * - Reflection bridge wants adapter-state / wiring diagnostics.
 *
 * This object does not define a new token grammar.
 * It delegates grammar to MetamodelProtocolTextGuards.
 */
internal object ReflectionAdapterProtocolTokenLaw {
    private const val MAX_PROTOCOL_TOKEN_CHARS: Int = 128

    fun requireAssemblyProtocolIdToken(
        field: String,
        value: String,
    ) {
        try {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = field,
                value = value,
                maxChars = MAX_PROTOCOL_TOKEN_CHARS,
            )
        } catch (e: MetamodelFactContractViolationException) {
            throw MetamodelAdapterAssemblyException(
                "Reflection metamodel adapter assembly rejected invalid protocol token. " +
                        "blame=$field, cause=${e.message}",
            )
        }
    }

    fun requireBridgeProtocolIdToken(
        field: String,
        value: String,
    ) {
        try {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = field,
                value = value,
                maxChars = MAX_PROTOCOL_TOKEN_CHARS,
            )
        } catch (e: MetamodelFactContractViolationException) {
            throw MetamodelAdapterStateViolationException(
                "Reflection TypeReference bridge rejected invalid protocol token. " +
                        "blame=$field, cause=${e.message}",
            )
        }
    }
}