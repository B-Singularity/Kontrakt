package kontrakt.planning.domain.protocol

import metamodel.port.outgoing.NormalizationEngine
import planning.domain.exception.PortContractViolationException
import java.nio.charset.StandardCharsets

/**
 * [SSOT] Hash Input Encoding + Normalization Rejection Law (NFC-REJECT).
 *
 * Encoding law:
 * - UTF-8 bytes, preceded by a 4-byte little-endian length prefix (byte length).
 *
 * Validation law:
 * - Unpaired UTF-16 surrogates -> REJECT (PortContractViolationException).
 * - Non-NFC input -> REJECT (core MUST NOT normalize).
 *
 * Hexagonal rule:
 * - This protocol depends only on the [NormalizationEngine] port.
 * - ICU/JDK/etc are adapters outside the domain.
 *
 * Note on normalizationSpecVersion:
 * - This value is a *protocol seed component* (planner config 7-tuple).
 * - If the normalization engine/version changes, the protocol version MUST be bumped.
 */
class HashInputEncodingSpec(
    private val normalizationEngine: NormalizationEngine,
    val normalizationSpecVersion: Long,
) {

    /**
     * Encodes [input] into [sink] at [offset], returning the number of bytes written.
     *
     * Format: [lenLE32][utf8Bytes]
     */
    fun appendEncoded(sink: ByteArray, offset: Int, input: String): Int {
        validateStrict(input)

        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        val len = bytes.size

        if (offset + 4 + len > sink.size) {
            throw IndexOutOfBoundsException("Buffer overflow in encoding: offset=$offset len=$len cap=${sink.size}")
        }

        sink[offset] = (len and 0xFF).toByte()
        sink[offset + 1] = ((len ushr 8) and 0xFF).toByte()
        sink[offset + 2] = ((len ushr 16) and 0xFF).toByte()
        sink[offset + 3] = ((len ushr 24) and 0xFF).toByte()

        System.arraycopy(bytes, 0, sink, offset + 4, len)
        return 4 + len
    }

    /**
     * Encodes [input] into a new byte array.
     *
     * Format: [lenLE32][utf8Bytes]
     */
    fun encodeStrict(input: String): ByteArray {
        validateStrict(input)

        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        val len = bytes.size

        val result = ByteArray(4 + len)
        result[0] = (len and 0xFF).toByte()
        result[1] = ((len ushr 8) and 0xFF).toByte()
        result[2] = ((len ushr 16) and 0xFF).toByte()
        result[3] = ((len ushr 24) and 0xFF).toByte()

        System.arraycopy(bytes, 0, result, 4, len)
        return result
    }

    /**
     * Strict validation:
     * - rejects unpaired surrogates
     * - rejects non-NFC
     */
    private fun validateStrict(input: String) {
        // 1) Surrogate integrity check (fail-closed before normalization engine)
        val len = input.length
        var i = 0
        while (i < len) {
            val c = input[i]
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= len || !Character.isLowSurrogate(input[i + 1])) {
                    throw PortContractViolationException("Unpaired high surrogate at index=$i")
                }
                i++ // skip the low surrogate partner
            } else if (Character.isLowSurrogate(c)) {
                throw PortContractViolationException("Unpaired low surrogate at index=$i")
            }
            i++
        }

        // 2) NFC-REJECT policy (core MUST NOT normalize)
        if (!normalizationEngine.isNfc(input)) {
            throw PortContractViolationException(
                "Input is not NFC (engine=${normalizationEngine.engineId} ${normalizationEngine.engineVersion}). " +
                        "Core does not normalize."
            )
        }
    }
}