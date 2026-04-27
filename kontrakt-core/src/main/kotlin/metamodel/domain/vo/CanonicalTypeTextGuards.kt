package metamodel.domain.vo

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Type-specific canonical text guards.
 *
 * CanonicalTextLaw checks generic canonical text validity.
 * This object rejects backend/binary syntax and under-lowered type notation that
 * must not enter canonical metamodel values.
 */
internal object CanonicalTypeTextGuards {

    fun rejectJvmBinaryDescriptor(
        field: String,
        value: String,
    ) {
        if (value.startsWith("L") && value.endsWith(";")) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be a JVM object descriptor: $value",
            )
        }

        if (value.startsWith("[L") || value.startsWith("[")) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be a JVM array descriptor: $value",
            )
        }

        if (value.contains('/')) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not contain JVM internal-name separator '/': $value",
            )
        }
    }

    fun rejectWhitespace(
        field: String,
        value: String,
    ) {
        var i = 0
        while (i < value.length) {
            if (value[i].isWhitespace()) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not contain whitespace: $value",
                )
            }
            i++
        }
    }

    fun rejectNullableMarker(
        field: String,
        value: String,
    ) {
        if (value.contains('?')) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not contain nullable marker '?'. Nullability must be lowered before this boundary: $value",
            )
        }
    }

    fun requireNoRawVarianceMarker(
        field: String,
        value: String,
    ) {
        if (value.contains(" out ") || value.contains(" in ")) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not contain source variance tokens. Variance must be lowered before this boundary: $value",
            )
        }
    }
}