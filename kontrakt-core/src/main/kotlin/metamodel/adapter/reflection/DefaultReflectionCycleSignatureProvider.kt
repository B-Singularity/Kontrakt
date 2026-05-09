package metamodel.adapter.reflection

import ir.identity.CanonicalSignature
import metamodel.domain.exception.MetamodelAdapterStateViolationException
import metamodel.domain.vo.TypeReference
import java.nio.charset.StandardCharsets

/**
 * Default reflection cycle-signature bridge.
 *
 * This is a temporary V1 bridge, not the final canonical byte-encoding law.
 *
 * Do not add custom UTF-8 validation or byte-layout code here. The canonical
 * byte-encoding phase must introduce a dedicated law/order with golden
 * vectors.
 */
class DefaultReflectionCycleSignatureProvider private constructor() :
    ReflectionCycleSignatureProvider {
    override fun deriveCycleSignature(
        reference: TypeReference,
    ): CanonicalSignature {
        val cycleKeyValue = reference.cycleKey.value

        if (cycleKeyValue.isEmpty()) {
            throw MetamodelAdapterStateViolationException(
                "Cannot derive reflection cycle signature from empty TypeReference.cycleKey.value: " +
                        "reference=${reference.renderSummary()}",
            )
        }

        /*
         * Minimal bridge only.
         *
         * UTF-8 is explicit to avoid platform default charset drift.
         * Full schema-tagged canonical byte encoding is intentionally deferred
         * to the canonical encoding / HID phase.
         */
        return CanonicalSignature(
            cycleKeyValue.toByteArray(StandardCharsets.UTF_8),
        )
    }

    companion object {
        @JvmStatic
        fun issue(): DefaultReflectionCycleSignatureProvider {
            return DefaultReflectionCycleSignatureProvider()
        }
    }
}