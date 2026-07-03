package stage.canonicalization.contract.representative

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException

/**
 * Shared low-level order text guards for metamodel value objects.
 *
 * This is the single source of truth for small order-safety checks.
 *
 * This object is deliberately small and ASCII/control oriented.
 *
 * It does not:
 *
 * - normalize Unicode;
 * - call Character.*;
 * - classify Unicode categories;
 * - inspect scripts;
 * - perform NFC checks;
 * - parse Kotlin/JVM syntax;
 * - or replace NormalizationEngine.
 *
 * It only enforces transport/order safety rules shared by metamodel VOs:
 *
 * - bounded length;
 * - no pipe delimiter;
 * - no NUL;
 * - no C0/C1 control characters;
 * - optional ASCII identifier-token shape.
 */
object MetamodelProtocolTextGuards {
    fun requireBoundedProtocolText(
        field: String,
        value: String,
        maxChars: Int,
        allowEmpty: Boolean,
    ) {
        requireLength(
            field = field,
            value = value,
            maxChars = maxChars,
            allowEmpty = allowEmpty,
        )

        for (index in value.indices) {
            requireProtocolChar(
                field = "$field[$index]",
                value = value[index],
            )
        }
    }

    /**
     * Stronger guard for ASCII identifier-like order tokens.
     *
     * This intentionally does not call requireBoundedProtocolText(...), because
     * the ASCII identifier loop is already stricter than the pipe/control guard.
     *
     * Allowed:
     *
     * - A-Z
     * - a-z
     * - 0-9
     * - _
     *
     * If this loop passes, the token cannot contain '|', NUL, or C0/C1 control
     * material. This avoids redundant O(N) scans.
     */
    fun requireAsciiIdentifierToken(
        field: String,
        value: String,
        maxChars: Int,
    ) {
        requireLength(
            field = field,
            value = value,
            maxChars = maxChars,
            allowEmpty = false,
        )

        val first = value[0]

        val firstOk =
            first in 'A'..'Z' ||
                    first in 'a'..'z' ||
                    first == '_'

        if (!firstOk) {
            throw MetamodelFactContractViolationException(
                "$field must start with an ASCII identifier-start character.",
            )
        }

        var index = 1
        while (index < value.length) {
            val c = value[index]
            val ok =
                c in 'A'..'Z' ||
                        c in 'a'..'z' ||
                        c in '0'..'9' ||
                        c == '_'

            if (!ok) {
                throw MetamodelFactContractViolationException(
                    "$field contains a non-canonical ASCII identifier-part character at index=$index.",
                )
            }

            index += 1
        }
    }

    /**
     * Guard for order id-like tokens.
     *
     * Allowed:
     *
     * - A-Z
     * - a-z
     * - 0-9
     * - -
     * - _
     * - .
     *
     * This is suitable for classifier ids, verifier ids, policy ids, algorithm
     * ids, factory ids, and similar order labels. It does not lowercase the
     * value. Callers that need canonical lowercase must perform canonicalization
     * separately after this guard.
     */
    fun requireAsciiProtocolIdToken(
        field: String,
        value: String,
        maxChars: Int,
    ) {
        requireLength(
            field = field,
            value = value,
            maxChars = maxChars,
            allowEmpty = false,
        )

        for (index in value.indices) {
            val c = value[index]
            val ok =
                c in 'A'..'Z' ||
                        c in 'a'..'z' ||
                        c in '0'..'9' ||
                        c == '-' ||
                        c == '_' ||
                        c == '.'

            if (!ok) {
                throw MetamodelFactContractViolationException(
                    "$field contains a non-canonical order-id character at index=$index.",
                )
            }
        }
    }

    /**
     * Single-character order guard.
     *
     * Use this inside other domain loops to avoid scanning the same string twice.
     */
    fun requireProtocolChar(
        field: String,
        value: Char,
    ) {
        if (value == '|') {
            throw MetamodelFactContractViolationException(
                "$field contains reserved order delimiter.",
            )
        }

        val code = value.code
        val isC0Control = code in 0x0000..0x001F
        val isC1Control = code in 0x007F..0x009F

        if (isC0Control || isC1Control) {
            throw MetamodelFactContractViolationException(
                "$field contains a control character.",
            )
        }
    }

    fun requireLength(
        field: String,
        value: String,
        maxChars: Int,
        allowEmpty: Boolean,
    ) {
        if (maxChars <= 0) {
            throw MetamodelFactContractViolationException(
                "$field maxChars must be > 0.",
            )
        }

        if (!allowEmpty && value.isEmpty()) {
            throw MetamodelFactContractViolationException(
                "$field must not be empty.",
            )
        }

        if (value.length > maxChars) {
            throw MetamodelFactContractViolationException(
                "$field exceeds maximum allowed length.",
            )
        }
    }

    fun isAsciiWhitespace(
        value: Char,
    ): Boolean {
        return value == ' ' ||
                value == '\t' ||
                value == '\n' ||
                value == '\r' ||
                value == '\u000C'
    }

    fun isC0OrC1Control(
        value: Char,
    ): Boolean {
        val code = value.code
        return code in 0x0000..0x001F ||
                code in 0x007F..0x009F
    }

    fun isReservedProtocolDelimiter(
        value: Char,
    ): Boolean {
        return value == '|'
    }

    fun isReservedProtocolOrControl(
        value: Char,
    ): Boolean {
        return isReservedProtocolDelimiter(value) ||
                isC0OrC1Control(value)
    }

    fun requireBoundedDiagnosticText(
        field: String,
        value: String,
        maxChars: Int,
    ) {
        if (field.isEmpty()) {
            throw MetamodelFactContractViolationException(
                "Metamodel diagnostic guard field name must not be empty.",
            )
        }

        if (value.isEmpty()) {
            throw MetamodelFactContractViolationException(
                "$field must not be empty.",
            )
        }

        if (maxChars <= 0) {
            throw MetamodelFactContractViolationException(
                "$field maxChars must be > 0: $maxChars",
            )
        }

        if (value.length > maxChars) {
            throw MetamodelFactContractViolationException(
                "$field exceeds diagnostic cap=$maxChars.",
            )
        }

        var index = 0
        while (index < value.length) {
            val c = value[index]

            if (isC0OrC1Control(c)) {
                throw MetamodelFactContractViolationException(
                    "$field must not contain C0/C1 control material: index=$index.",
                )
            }

            index += 1
        }
    }
}
