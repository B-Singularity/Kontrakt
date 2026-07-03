package stage.lowering.material.candidate

import realization.graph.IrLimits
import realization.graph.IrProtocolViolationException

/**
 * Raw payload node in the plan order.
 *
 * Constraints:
 * - payload size is enforced in BYTES (UTF-8) to match IrLimits.MAX_PAYLOAD_BYTES
 * - intended as a hard DoS boundary for untrusted inbound payloads
 */
class RawPayloadNode private constructor(
    val typeId: RawTypeId,
    val serializedPayload: String,
) {
    init {
        val bytes = serializedPayload.toByteArray(Charsets.UTF_8).size
        if (bytes > IrLimits.MAX_PAYLOAD_BYTES) {
            throw IrProtocolViolationException(
                "RawPayloadNode payload exceeds max bytes (${IrLimits.MAX_PAYLOAD_BYTES}).",
            )
        }
    }

    override fun toString(): String =
        "RawPayloadNode(typeId=$typeId, payloadBytes=${serializedPayload.toByteArray(Charsets.UTF_8).size})"

    companion object {
        @JvmStatic
        fun issue(
            typeId: RawTypeId,
            serializedPayload: String,
        ): RawPayloadNode =
            RawPayloadNode(
                typeId = typeId,
                serializedPayload = serializedPayload,
            )
    }
}
