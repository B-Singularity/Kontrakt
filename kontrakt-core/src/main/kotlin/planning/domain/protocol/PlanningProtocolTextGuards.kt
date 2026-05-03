package planning.domain.protocol

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Planning-domain protocol text guards.
 *
 * This object is the planning layer's single entry point for cheap protocol text
 * surface checks.
 *
 * This is not:
 *
 * - a Unicode normalizer;
 * - a canonical type-text validator;
 * - a metamodel ratifier;
 * - a source-language identifier classifier;
 * - a Kotlin/JVM reflection classifier;
 * - an adapter buffer validator;
 * - or a canonical byte encoder.
 *
 * Why this exists:
 *
 * The metamodel layer owns canonical type identity ratification.
 *
 * Planning, however, still owns several planning-local text surfaces:
 *
 * - projection member names;
 * - active-member keys;
 * - entropy target keys;
 * - HID selector local semantic identities;
 * - implementation canonical identifiers;
 * - diagnostic/protocol rendering fields.
 *
 * These surfaces must share one planning-side guard so each VO does not
 * duplicate C0/C1 control checks, delimiter checks, length caps, and whitespace
 * policy.
 *
 * Snapshot law:
 *
 * This guard accepts String, not CharSequence.
 *
 * Reason:
 *
 * Planning-domain guards validate immutable domain-boundary surfaces. Mutable
 * buffers such as StringBuilder or CharBuffer belong to adapter-local code. They
 * must be converted into stable String snapshots before reaching planning VO/DTO
 * issuance.
 *
 * Unicode law:
 *
 * requireBoundedProtocolText(...) allows non-ASCII characters unless they are
 * explicit protocol/control material.
 *
 * This is intentional. Unicode normalization, bidi/format checks, homoglyph
 * policy, and script policy are not planning-layer responsibilities.
 *
 * If a field requires ASCII-only material, use requireBoundedProtocolIdentifier.
 *
 * Layering law:
 *
 * Planning code should throw planning-domain exceptions for planning-domain
 * boundary failures. Do not leak metamodel guard exceptions through planning
 * value objects.
 *
 * Determinism law:
 *
 * This guard deliberately avoids:
 *
 * - Character.getType;
 * - Character.isWhitespace;
 * - Character.isUnicodeIdentifierStart;
 * - Character.isUnicodeIdentifierPart;
 * - java.text.Normalizer;
 * - locale-sensitive APIs;
 * - Regex.
 *
 * It uses only explicit ASCII/C0/C1 checks.
 *
 * Encoding law:
 *
 * This guard does not encode. Canonical byte encoding must be handled by a
 * tagged, length-prefixed encoder at the canonical signature / HID boundary.
 */
object PlanningProtocolTextGuards {
    /**
     * Validates a bounded planning protocol text surface.
     *
     * This allows non-ASCII material. It only rejects:
     *
     * - empty input when allowEmpty=false;
     * - length overflow;
     * - reserved protocol delimiter '|';
     * - C0/C1 control characters;
     * - ASCII whitespace when rejectAsciiWhitespace=true.
     */
    fun requireBoundedProtocolText(
        field: String,
        value: String,
        maxChars: Int,
        allowEmpty: Boolean = false,
        rejectAsciiWhitespace: Boolean = true,
    ) {
        requireFieldName(field)
        requireMaxChars(field, maxChars)

        if (!allowEmpty && value.isEmpty()) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be empty.",
            )
        }

        if (value.length > maxChars) {
            throw TypeExpansionContractViolationException(
                reason = "$field exceeds planning protocol cap=$maxChars.",
            )
        }

        var index = 0
        while (index < value.length) {
            val c = value[index]

            if (isReservedProtocolOrControl(c)) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not contain reserved protocol/control material: index=$index.",
                )
            }

            if (rejectAsciiWhitespace && isAsciiWhitespace(c)) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not contain ASCII whitespace: index=$index.",
                )
            }

            index += 1
        }
    }

    /**
     * Validates a bounded ASCII protocol identifier in a single pass.
     *
     * Allowed characters:
     *
     * - A-Z
     * - a-z
     * - 0-9
     * - _
     * - .
     * - -
     * - :
     *
     * This is intentionally not a source-language identifier rule.
     *
     * It is for compact planning protocol identifiers such as stable tokens,
     * canonical provider identifiers, implementation identifiers, and local
     * planning keys.
     */
    fun requireBoundedProtocolIdentifier(
        field: String,
        value: String,
        maxChars: Int,
        allowEmpty: Boolean = false,
    ) {
        requireFieldName(field)
        requireMaxChars(field, maxChars)

        if (!allowEmpty && value.isEmpty()) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be empty.",
            )
        }

        if (value.length > maxChars) {
            throw TypeExpansionContractViolationException(
                reason = "$field exceeds planning protocol cap=$maxChars.",
            )
        }

        var index = 0
        while (index < value.length) {
            val c = value[index]

            if (isReservedProtocolOrControl(c)) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not contain reserved protocol/control material: index=$index.",
                )
            }

            if (isAsciiWhitespace(c)) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not contain ASCII whitespace: index=$index.",
                )
            }

            if (!isProtocolIdentifierChar(c)) {
                throw TypeExpansionContractViolationException(
                    reason = "$field contains non-canonical protocol identifier character: index=$index.",
                )
            }

            index += 1
        }
    }

    fun isReservedProtocolOrControl(
        value: Char,
    ): Boolean {
        return isReservedProtocolDelimiter(value) ||
                isC0OrC1Control(value)
    }

    fun isReservedProtocolDelimiter(
        value: Char,
    ): Boolean {
        return value == '|'
    }

    fun isC0OrC1Control(
        value: Char,
    ): Boolean {
        val code = value.code

        return code in 0x0000..0x001F ||
                code in 0x007F..0x009F
    }

    fun isAsciiWhitespace(
        value: Char,
    ): Boolean {
        return value == ' ' ||
                value == '\t' ||
                value == '\n' ||
                value == '\r' ||
                value == '\u000B' ||
                value == '\u000C'
    }

    fun isProtocolIdentifierChar(
        value: Char,
    ): Boolean {
        return value in 'A'..'Z' ||
                value in 'a'..'z' ||
                value in '0'..'9' ||
                value == '_' ||
                value == '.' ||
                value == '-' ||
                value == ':'
    }

    private fun requireFieldName(
        field: String,
    ) {
        if (field.isEmpty()) {
            throw TypeExpansionContractViolationException(
                reason = "Planning protocol guard field name must not be empty.",
            )
        }
    }

    private fun requireMaxChars(
        field: String,
        maxChars: Int,
    ) {
        if (maxChars < 0) {
            throw TypeExpansionContractViolationException(
                reason = "$field maxChars must be >= 0.",
            )
        }
    }
}