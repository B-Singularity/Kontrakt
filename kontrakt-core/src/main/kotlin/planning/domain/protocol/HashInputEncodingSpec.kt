package kontrakt.planning.domain.protocol

import metamodel.domain.port.outgoing.NormalizationEngine
import stage.lowering.diagnostics.PortContractViolationException

/**
 * [SSOT] Hash Input Encoding + Normalization Rejection Law (NFC-REJECT).
 *
 * Encoding law:
 *
 * - UTF-8 bytes;
 * - preceded by a 4-byte little-endian byte-length prefix;
 * - no platform/default charset;
 * - no CharsetEncoder state;
 * - no normalization in core.
 *
 * Validation law:
 *
 * - input is an immutable String snapshot;
 * - unpaired UTF-16 surrogates are rejected;
 * - non-NFC input is rejected;
 * - input character length is capped;
 * - encoded UTF-8 byte length is capped;
 * - arithmetic is checked with Long before narrowing.
 *
 * Hexagonal rule:
 *
 * This order depends only on the NormalizationEngine port.
 * ICU/JDK/etc normalization engines are adapters outside the domain.
 *
 * Determinism rule:
 *
 * This class does not use java.nio CharsetEncoder. UTF-8 length calculation and
 * byte emission are explicitly implemented here so the hash input order is
 * pinned at the byte level.
 *
 * Note on normalizationSpecVersion:
 *
 * This value is a order seed component. If the normalization engine/version
 * changes, the order version must be bumped by policy.
 */
class HashInputEncodingSpec(
    private val normalizationEngine: NormalizationEngine,
    val normalizationSpecVersion: Long,
    private val maxInputChars: Int = DEFAULT_MAX_INPUT_CHARS,
    private val maxUtf8Bytes: Int = DEFAULT_MAX_UTF8_BYTES,
) {
    init {
        if (normalizationSpecVersion < 0L) {
            throw PortContractViolationException(
                "normalizationSpecVersion must be >= 0: $normalizationSpecVersion",
            )
        }

        if (maxInputChars < 0) {
            throw PortContractViolationException(
                "maxInputChars must be >= 0: $maxInputChars",
            )
        }

        if (maxUtf8Bytes < 0) {
            throw PortContractViolationException(
                "maxUtf8Bytes must be >= 0: $maxUtf8Bytes",
            )
        }
    }

    /**
     * Encodes [input] into [sink] at [offset].
     *
     * Format:
     *
     *     [lenLE32][utf8Bytes]
     *
     * Returns the number of bytes written.
     *
     * This method does not allocate an intermediate UTF-8 byte array.
     */
    fun appendEncoded(
        sink: ByteArray,
        offset: Int,
        input: String,
    ): Int {
        val utf8Length = validateStrictAndMeasureUtf8Length(input)
        validateSinkRange(
            sink = sink,
            offset = offset,
            utf8Length = utf8Length,
        )

        writeInt32LittleEndian(
            destination = sink,
            offset = offset,
            value = utf8Length,
        )

        writeUtf8Bytes(
            destination = sink,
            offset = offset + LENGTH_PREFIX_BYTES,
            input = input,
        )

        return LENGTH_PREFIX_BYTES + utf8Length
    }

    /**
     * Encodes [input] into a new byte array.
     *
     * Format:
     *
     *     [lenLE32][utf8Bytes]
     */
    fun encodeStrict(
        input: String,
    ): ByteArray {
        val utf8Length = validateStrictAndMeasureUtf8Length(input)
        val totalLength = checkedTotalLength(utf8Length)

        val result = ByteArray(totalLength)

        writeInt32LittleEndian(
            destination = result,
            offset = 0,
            value = utf8Length,
        )

        writeUtf8Bytes(
            destination = result,
            offset = LENGTH_PREFIX_BYTES,
            input = input,
        )

        return result
    }

    /**
     * Strict validation plus UTF-8 length measurement.
     *
     * Performs in one pass:
     *
     * - character count cap check;
     * - UTF-16 surrogate integrity check;
     * - UTF-8 byte length calculation;
     * - encoded byte length cap check.
     *
     * NFC validation is performed after surrogate validation because the
     * normalization engine must never receive malformed UTF-16 material from
     * this order boundary.
     */
    private fun validateStrictAndMeasureUtf8Length(
        input: String,
    ): Int {
        if (input.length > maxInputChars) {
            throw PortContractViolationException(
                "Input exceeds maximum character length: max=$maxInputChars actual=${input.length}",
            )
        }

        val utf8Length = measureUtf8LengthAfterSurrogateCheck(input)

        if (utf8Length > maxUtf8Bytes) {
            throw PortContractViolationException(
                "Input exceeds maximum UTF-8 byte length: max=$maxUtf8Bytes actual=$utf8Length",
            )
        }

        if (!normalizationEngine.isNfc(input)) {
            throw PortContractViolationException(
                "Input is not NFC " +
                        "(engine=${normalizationEngine.engineId} ${normalizationEngine.engineVersion}). " +
                        "Core does not normalize.",
            )
        }

        return utf8Length
    }

    private fun measureUtf8LengthAfterSurrogateCheck(
        input: String,
    ): Int {
        var byteLength = 0L
        var index = 0

        while (index < input.length) {
            val c = input[index]
            val code = c.code

            when {
                code <= 0x007F -> {
                    byteLength += 1L
                }

                code <= 0x07FF -> {
                    byteLength += 2L
                }

                isHighSurrogateCode(code) -> {
                    val nextIndex = index + 1

                    if (nextIndex >= input.length) {
                        throw PortContractViolationException(
                            "Unpaired high surrogate at index=$index",
                        )
                    }

                    val low = input[nextIndex]
                    val lowCode = low.code

                    if (!isLowSurrogateCode(lowCode)) {
                        throw PortContractViolationException(
                            "Unpaired high surrogate at index=$index",
                        )
                    }

                    byteLength += 4L
                    index += 1
                }

                isLowSurrogateCode(code) -> {
                    throw PortContractViolationException(
                        "Unpaired low surrogate at index=$index",
                    )
                }

                else -> {
                    byteLength += 3L
                }
            }

            if (byteLength > Int.MAX_VALUE.toLong()) {
                throw PortContractViolationException(
                    "UTF-8 byte length exceeds Int range.",
                )
            }

            index += 1
        }

        return byteLength.toInt()
    }

    private fun validateSinkRange(
        sink: ByteArray,
        offset: Int,
        utf8Length: Int,
    ) {
        if (offset < 0 || offset > sink.size) {
            throw IndexOutOfBoundsException(
                "Invalid encoding offset: offset=$offset cap=${sink.size}",
            )
        }

        val required = LENGTH_PREFIX_BYTES.toLong() + utf8Length.toLong()
        val available = sink.size.toLong() - offset.toLong()

        if (required > available) {
            throw IndexOutOfBoundsException(
                "Buffer overflow in encoding: offset=$offset required=$required cap=${sink.size}",
            )
        }
    }

    private fun checkedTotalLength(
        utf8Length: Int,
    ): Int {
        val total = LENGTH_PREFIX_BYTES.toLong() + utf8Length.toLong()

        if (total > Int.MAX_VALUE.toLong()) {
            throw PortContractViolationException(
                "Encoded byte array length exceeds Int range.",
            )
        }

        return total.toInt()
    }

    private fun writeInt32LittleEndian(
        destination: ByteArray,
        offset: Int,
        value: Int,
    ) {
        /*
         * offset is already range-checked by the caller.
         */
        destination[offset] = (value and 0xFF).toByte()
        destination[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        destination[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        destination[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private fun writeUtf8Bytes(
        destination: ByteArray,
        offset: Int,
        input: String,
    ) {
        var out = offset
        var index = 0

        while (index < input.length) {
            val c = input[index]
            val code = c.code

            when {
                code <= 0x007F -> {
                    destination[out] = code.toByte()
                    out += 1
                }

                code <= 0x07FF -> {
                    destination[out] = (0xC0 or (code ushr 6)).toByte()
                    destination[out + 1] = (0x80 or (code and 0x3F)).toByte()
                    out += 2
                }

                isHighSurrogateCode(code) -> {
                    val lowCode = input[index + 1].code
                    val codePoint =
                        0x10000 +
                                ((code - HIGH_SURROGATE_START) shl 10) +
                                (lowCode - LOW_SURROGATE_START)

                    destination[out] = (0xF0 or (codePoint ushr 18)).toByte()
                    destination[out + 1] = (0x80 or ((codePoint ushr 12) and 0x3F)).toByte()
                    destination[out + 2] = (0x80 or ((codePoint ushr 6) and 0x3F)).toByte()
                    destination[out + 3] = (0x80 or (codePoint and 0x3F)).toByte()
                    out += 4
                    index += 1
                }

                else -> {
                    destination[out] = (0xE0 or (code ushr 12)).toByte()
                    destination[out + 1] = (0x80 or ((code ushr 6) and 0x3F)).toByte()
                    destination[out + 2] = (0x80 or (code and 0x3F)).toByte()
                    out += 3
                }
            }

            index += 1
        }
    }

    private fun isHighSurrogateCode(
        code: Int,
    ): Boolean {
        return code in HIGH_SURROGATE_START..HIGH_SURROGATE_END
    }

    private fun isLowSurrogateCode(
        code: Int,
    ): Boolean {
        return code in LOW_SURROGATE_START..LOW_SURROGATE_END
    }

    companion object {
        private const val LENGTH_PREFIX_BYTES: Int = 4

        private const val HIGH_SURROGATE_START: Int = 0xD800
        private const val HIGH_SURROGATE_END: Int = 0xDBFF
        private const val LOW_SURROGATE_START: Int = 0xDC00
        private const val LOW_SURROGATE_END: Int = 0xDFFF

        /**
         * Conservative order cap for one hash input component.
         *
         * This should be aligned with the largest canonical type/signature or
         * planning order field cap. Raise by order amendment, not by
         * adapter workaround.
         */
        const val DEFAULT_MAX_INPUT_CHARS: Int = 8_192

        /**
         * UTF-8 byte cap for one hash input component.
         *
         * Kept separate from char cap because non-ASCII material can expand.
         */
        const val DEFAULT_MAX_UTF8_BYTES: Int = 24_576
    }
}