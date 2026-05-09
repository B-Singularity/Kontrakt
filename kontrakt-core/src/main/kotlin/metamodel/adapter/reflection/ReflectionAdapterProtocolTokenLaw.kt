package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelAdapterAssemblyException
import metamodel.domain.exception.MetamodelAdapterStateViolationException
import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Reflection-adapter wrapper for metamodel order-token validation.
 *
 * This object does not define a new token grammar.
 *
 * The grammar authority is:
 *
 *     MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(...)
 *
 * This wrapper exists only to preserve adapter-specific blame:
 *
 * - assembly-time invalid token
 *     -> MetamodelAdapterAssemblyException
 *
 * - bridge/runtime wiring invalid token
 *     -> MetamodelAdapterStateViolationException
 *
 * Do not copy/paste ASCII token loops into adapter classes.
 * Do not allow ':' here unless MetamodelProtocolTextGuards changes first.
 * Do not use ReflectionNormalizationGuard for order/governance tokens.
 */
internal object ReflectionAdapterProtocolTokenLaw {
    private const val MAX_PROTOCOL_TOKEN_CHARS: Int = 128

    fun requireAssemblyProtocolIdToken(
        field: String,
        value: String,
    ) {
        try {
            requireProtocolIdToken(
                field = field,
                value = value,
            )
        } catch (e: MetamodelFactContractViolationException) {
            throw MetamodelAdapterAssemblyException(
                "Reflection metamodel adapter assembly rejected invalid order token. " +
                        "blame=$field, cause=${e.message}",
            )
        }
    }

    fun requireBridgeProtocolIdToken(
        field: String,
        value: String,
    ) {
        try {
            requireProtocolIdToken(
                field = field,
                value = value,
            )
        } catch (e: MetamodelFactContractViolationException) {
            throw MetamodelAdapterStateViolationException(
                "Reflection TypeReference bridge rejected invalid order token. " +
                        "blame=$field, cause=${e.message}",
            )
        }
    }

    private fun requireProtocolIdToken(
        field: String,
        value: String,
    ) {
        MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
            field = field,
            value = value,
            maxChars = MAX_PROTOCOL_TOKEN_CHARS,
        )
    }
}