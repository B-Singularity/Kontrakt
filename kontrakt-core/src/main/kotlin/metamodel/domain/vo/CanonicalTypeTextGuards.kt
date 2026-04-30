package metamodel.domain.vo

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Type-specific canonical text guards.
 *
 * These guards run after:
 * - Unicode scalar validation;
 * - NFC verification through NormalizationEngine.
 *
 * They reject:
 * - JVM descriptors;
 * - JVM internal names;
 * - JVM binary nested-class names;
 * - raw nullable markers unless explicitly allowed;
 * - raw star projection unless explicitly allowed;
 * - source variance tokens;
 * - whitespace;
 * - control / invisible / private / unassigned Unicode code points.
 *
 * This object does not normalize.
 */
internal object CanonicalTypeTextGuards {

    private val JVM_PRIMITIVE_DESCRIPTORS: Set<String> = setOf(
        "B", // byte
        "C", // char
        "D", // double
        "F", // float
        "I", // int
        "J", // long
        "S", // short
        "Z", // boolean
        "V", // void
    )

    fun validateAlreadyRatifiedTypeText(
        field: String,
        value: String,
        allowNullableMarker: Boolean,
        allowStarProjection: Boolean,
    ) {
        if (value.isBlank()) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be blank.",
            )
        }

        if (value.contains('|')) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not contain reserved delimiter '|': $value",
            )
        }

        rejectJvmDescriptorLikeMaterial(field, value)

        scanOnce(
            field = field,
            value = value,
            allowNullableMarker = allowNullableMarker,
            allowStarProjection = allowStarProjection,
        )
    }

    private fun rejectJvmDescriptorLikeMaterial(
        field: String,
        value: String,
    ) {
        if (value in JVM_PRIMITIVE_DESCRIPTORS) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be a JVM primitive descriptor: $value",
            )
        }

        if (value.startsWith("L") && value.endsWith(";")) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be a JVM object descriptor: $value",
            )
        }

        if (value.startsWith("[")) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be a JVM array descriptor: $value",
            )
        }

        if (value.contains("<L") || value.contains(";>") || value.contains(";,")) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not contain embedded JVM descriptor material: $value",
            )
        }
    }

    private fun scanOnce(
        field: String,
        value: String,
        allowNullableMarker: Boolean,
        allowStarProjection: Boolean,
    ) {
        var index = 0

        while (index < value.length) {
            val codePoint = value.codePointAt(index)

            when {
                isForbiddenUnicodeCategory(codePoint) -> {
                    throw TypeExpansionContractViolationException(
                        reason = "$field must not contain invisible/control/private/unassigned Unicode code point at index=$index: U+${
                            codePoint.toString(
                                16
                            )
                        }",
                    )
                }

                Character.isWhitespace(codePoint) -> {
                    throw TypeExpansionContractViolationException(
                        reason = "$field must not contain whitespace at index=$index: $value",
                    )
                }

                codePoint == '/'.code -> {
                    throw TypeExpansionContractViolationException(
                        reason = "$field must not contain JVM internal-name separator '/' at index=$index: $value",
                    )
                }

                codePoint == '$'.code -> {
                    throw TypeExpansionContractViolationException(
                        reason = "$field must use canonical source-style nesting, not JVM binary '$' names at index=$index: $value",
                    )
                }

                codePoint == '?'.code && !allowNullableMarker -> {
                    throw TypeExpansionContractViolationException(
                        reason = "$field must not contain nullable marker '?' at index=$index. Nullability must be lowered before this boundary: $value",
                    )
                }

                codePoint == '*'.code && !allowStarProjection -> {
                    throw TypeExpansionContractViolationException(
                        reason = "$field must not contain raw star projection '*' at index=$index. Star projection must be lowered before this boundary: $value",
                    )
                }

                isIdentifierStart(codePoint) -> {
                    index = scanIdentifierToken(
                        field = field,
                        value = value,
                        startIndex = index,
                    )
                    continue
                }
            }

            index += Character.charCount(codePoint)
        }
    }

    private fun scanIdentifierToken(
        field: String,
        value: String,
        startIndex: Int,
    ): Int {
        var index = startIndex + Character.charCount(value.codePointAt(startIndex))

        while (index < value.length) {
            val codePoint = value.codePointAt(index)
            if (!isIdentifierPart(codePoint)) {
                break
            }

            index += Character.charCount(codePoint)
        }

        val token = value.substring(startIndex, index)
        if (token == "in" || token == "out") {
            throw TypeExpansionContractViolationException(
                reason = "$field must not contain source variance token '$token' at index=$startIndex. Variance must be lowered before this boundary: $value",
            )
        }

        return index
    }

    private fun isForbiddenUnicodeCategory(codePoint: Int): Boolean {
        return when (Character.getType(codePoint)) {
            Character.CONTROL.toInt(),
            Character.FORMAT.toInt(),
            Character.PRIVATE_USE.toInt(),
            Character.SURROGATE.toInt(),
            Character.UNASSIGNED.toInt() -> true

            else -> false
        }
    }

    private fun isIdentifierStart(codePoint: Int): Boolean {
        return Character.isUnicodeIdentifierStart(codePoint)
    }

    private fun isIdentifierPart(codePoint: Int): Boolean {
        return Character.isUnicodeIdentifierPart(codePoint)
    }
}