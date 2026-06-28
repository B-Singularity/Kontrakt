package stage.canonicalization.contract

import planning.domain.exception.CanonicalContractViolationException
import java.text.Normalizer

/**
 * Canonical text law for Kontrakt domain protocols.
 *
 * This object is a law/helper, not canonical material.
 *
 * Responsibilities:
 * - verify that text entering canonical/order values is already normalized;
 * - provide Kontrakt-owned string / identifier comparison semantics;
 * - avoid direct dependence on locale collation;
 * - keep byte encoding separate from semantic ordering.
 *
 * Important:
 * - This object does not normalize.
 * - Normalization authority belongs at metadata / adapter boundaries.
 * - UTF-8 belongs to canonical encoding / hashing / serialization law.
 * - Canonical ordering is defined at the Unicode scalar semantic layer.
 */
internal object CanonicalTextLaw {
    private const val RESERVED_RENDER_DELIMITER: Char = '|'
    private const val JVM_BINARY_NESTED_CLASS_SEPARATOR: Char = '$'

    fun validateCanonicalComponent(
        field: String,
        value: String,
    ) {
        validateCanonicalTextValue(
            field = field,
            value = value,
            allowEmpty = false,
        )

        if (value.contains(RESERVED_RENDER_DELIMITER)) {
            throw CanonicalContractViolationException(
                reason = "$field must not contain reserved delimiter '$RESERVED_RENDER_DELIMITER': $value",
            )
        }
    }

    fun validateCanonicalTextValue(
        field: String,
        value: String,
        allowEmpty: Boolean,
    ) {
        if (!allowEmpty && value.isBlank()) {
            throw CanonicalContractViolationException(
                reason = "$field must not be blank.",
            )
        }

        if (value.isEmpty() && allowEmpty) {
            return
        }

        requireValidUnicodeScalarSequence(field, value)

        if (!Normalizer.isNormalized(value, Normalizer.Form.NFC)) {
            throw CanonicalContractViolationException(
                reason = "$field must already be NFC-normalized: $value",
            )
        }
    }

    /**
     * Validates a canonical qualified identifier such as implementationFqcn.
     *
     * This is intentionally not a source-language keyword validator.
     *
     * Language keywords are syntax-surface concerns. Reflection/KSP/bytecode
     * adapters must lower backend-specific names into this canonical semantic
     * identifier representation before entering the planning domain.
     */
    fun validateCanonicalQualifiedIdentifier(
        field: String,
        value: String,
    ) {
        validateCanonicalComponent(field, value)

        if (value.startsWith('.')) {
            throw CanonicalContractViolationException(
                reason = "$field must not start with '.': $value",
            )
        }

        if (value.endsWith('.')) {
            throw CanonicalContractViolationException(
                reason = "$field must not end with '.': $value",
            )
        }

        if (value.contains("..")) {
            throw CanonicalContractViolationException(
                reason = "$field must not contain empty identifier segment '..': $value",
            )
        }

        if (value.contains(JVM_BINARY_NESTED_CLASS_SEPARATOR)) {
            throw CanonicalContractViolationException(
                reason = "$field must use canonical source-style nesting, not JVM binary '$' names: $value",
            )
        }

        var segmentStart = 0
        var index = 0

        while (index <= value.length) {
            if (index == value.length || value[index] == '.') {
                if (index == segmentStart) {
                    throw CanonicalContractViolationException(
                        reason = "$field must not contain an empty identifier segment: $value",
                    )
                }

                validateIdentifierSegment(
                    field = field,
                    value = value,
                    startInclusive = segmentStart,
                    endExclusive = index,
                )

                segmentStart = index + 1
            }

            index++
        }
    }

    /**
     * Kontrakt canonical string order.
     *
     * This does not delegate to String.compareTo().
     *
     * Ordering is lexicographic ascending over Unicode scalar values after NFC
     * normalization. Locale-dependent collation and UTF-8 byte ordering are not
     * the authority for this semantic ordering law.
     */
    fun compareCanonicalStrings(
        left: String,
        right: String,
    ): Int {
        var leftIndex = 0
        var rightIndex = 0

        while (leftIndex < left.length && rightIndex < right.length) {
            val leftCodePoint = left.codePointAt(leftIndex)
            val rightCodePoint = right.codePointAt(rightIndex)

            if (leftCodePoint != rightCodePoint) {
                return leftCodePoint.compareTo(rightCodePoint)
            }

            leftIndex += Character.charCount(leftCodePoint)
            rightIndex += Character.charCount(rightCodePoint)
        }

        return when {
            leftIndex == left.length && rightIndex == right.length -> 0
            leftIndex == left.length -> -1
            else -> 1
        }
    }

    fun compareCanonicalIdentifiers(
        left: String,
        right: String,
    ): Int = compareCanonicalStrings(left, right)

    private fun requireValidUnicodeScalarSequence(
        field: String,
        value: String,
    ) {
        var index = 0

        while (index < value.length) {
            val ch = value[index]

            if (Character.isHighSurrogate(ch)) {
                if (index + 1 >= value.length || !Character.isLowSurrogate(value[index + 1])) {
                    throw CanonicalContractViolationException(
                        reason = "$field contains an unpaired high surrogate.",
                    )
                }

                index += 2
                continue
            }

            if (Character.isLowSurrogate(ch)) {
                throw CanonicalContractViolationException(
                    reason = "$field contains an unpaired low surrogate.",
                )
            }

            if (ch.isISOControl()) {
                throw CanonicalContractViolationException(
                    reason = "$field must not contain ISO control characters.",
                )
            }

            index++
        }
    }

    private fun validateIdentifierSegment(
        field: String,
        value: String,
        startInclusive: Int,
        endExclusive: Int,
    ) {
        var index = startInclusive
        var first = true

        while (index < endExclusive) {
            val codePoint = value.codePointAt(index)

            val valid =
                if (first) {
                    Character.isJavaIdentifierStart(codePoint)
                } else {
                    Character.isJavaIdentifierPart(codePoint)
                }

            if (!valid) {
                val detail =
                    if (first && Character.isDigit(codePoint)) {
                        "identifier segment must not start with a digit"
                    } else {
                        "invalid identifier character"
                    }

                throw CanonicalContractViolationException(
                    reason = "$field contains $detail: $value",
                )
            }

            if (codePoint == JVM_BINARY_NESTED_CLASS_SEPARATOR.code) {
                throw CanonicalContractViolationException(
                    reason = "$field must not contain JVM binary nested-class separator '$JVM_BINARY_NESTED_CLASS_SEPARATOR': $value",
                )
            }

            first = false
            index += Character.charCount(codePoint)
        }
    }
}