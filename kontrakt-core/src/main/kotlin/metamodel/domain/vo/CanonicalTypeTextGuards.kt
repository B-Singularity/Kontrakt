package metamodel.domain.vo

import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Low-cost order guards for already-inspected canonical type text snapshots.
 *
 * This object is deliberately NOT a Unicode validator.
 * This object is deliberately NOT an identifier classifier.
 * This object is deliberately NOT a normalizer.
 *
 * Unicode scalar validity, NFC, whitespace, invisible characters, bidi/format
 * characters, Unicode categories, scripts, identifier token boundaries,
 * combining marks, and grapheme cluster limits belong to NormalizationEngine.
 *
 * Why this file still exists:
 *
 * NormalizationEngine is an adapter-owned inspection boundary. It returns an
 * immutable snapshot plus lexical profile. This guard provides a final cheap
 * domain-side order assertion over ASCII-only syntax markers that are safe
 * to inspect without depending on host JRE Unicode tables.
 *
 * Dependency rule:
 *
 * - metamodel must not depend on planning.
 * - this guard must throw metamodel-domain exceptions only.
 * - this guard must not import planning.domain.*.
 *
 * Determinism rule:
 *
 * - do not call Character.getType;
 * - do not call Character.isWhitespace;
 * - do not call Character.isUnicodeIdentifierStart;
 * - do not call Character.isUnicodeIdentifierPart;
 * - do not call java.text.Normalizer;
 * - do not call reflection or KSP APIs.
 *
 * Reflection/KSP/JVM specific recognition belongs to adapters. This guard only
 * rejects ASCII order residues that must never appear in canonical type text.
 */
internal object CanonicalTypeTextGuards {
    private val JVM_PRIMITIVE_DESCRIPTORS: Set<String> =
        setOf(
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

    /**
     * Source-level variance tokens must be lowered before canonical type text.
     */
    private val SOURCE_VARIANCE_TOKENS: Set<String> =
        setOf(
            "in",
            "out",
        )

    /**
     * Source-level declaration keywords must not be accepted as canonical type
     * identifier tokens.
     *
     * This is intentionally conservative and ASCII-only. It prevents obvious
     * source syntax from leaking into canonical type identity material.
     *
     * Language-specific escaping/lowering must happen in the adapter before this
     * boundary. The core does not attempt to repair escaped source names.
     */
    private val SOURCE_RESERVED_TOKENS: Set<String> =
        setOf(
            "as",
            "break",
            "catch",
            "class",
            "continue",
            "do",
            "else",
            "false",
            "finally",
            "for",
            "fun",
            "if",
            "interface",
            "is",
            "null",
            "object",
            "package",
            "return",
            "super",
            "this",
            "throw",
            "true",
            "try",
            "typealias",
            "typeof",
            "val",
            "var",
            "when",
            "while",
        )

    /**
     * Validates a snapshot already accepted by NormalizationEngine.
     *
     * The input must be the immutable snapshot returned by
     * CanonicalTypeTextInspectionResult.Accepted, not the original caller-owned
     * CharSequence.
     */
    fun validateInspectedSnapshot(
        field: String,
        snapshot: String,
        allowNullableMarker: Boolean,
        allowStarProjection: Boolean,
    ) {
        if (field.isEmpty()) {
            throw MetamodelFactContractViolationException(
                "Canonical type text guard field name must not be empty.",
            )
        }

        if (snapshot.isEmpty()) {
            throw MetamodelFactContractViolationException(
                "$field must not be empty.",
            )
        }

        rejectReservedProtocolDelimiters(
            field = field,
            value = snapshot,
        )

        rejectJvmDescriptorLikeMaterial(
            field = field,
            value = snapshot,
        )

        scanAsciiProtocolMarkers(
            field = field,
            value = snapshot,
            allowNullableMarker = allowNullableMarker,
            allowStarProjection = allowStarProjection,
        )
    }

    private fun rejectReservedProtocolDelimiters(
        field: String,
        value: String,
    ) {
        if (value.indexOf('|') >= 0) {
            throw MetamodelFactContractViolationException(
                "$field must not contain reserved order delimiter '|': $value",
            )
        }

        if (value.indexOf('\u0000') >= 0) {
            throw MetamodelFactContractViolationException(
                "$field must not contain NUL character.",
            )
        }

        if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\t') >= 0) {
            throw MetamodelFactContractViolationException(
                "$field must not contain ASCII control whitespace.",
            )
        }
    }

    /**
     * Rejects JVM descriptor / internal-name / binary-name material.
     *
     * This is intentionally ASCII-only. Full platform classification belongs to
     * reflection/KSP adapters and NormalizationEngine.
     */
    private fun rejectJvmDescriptorLikeMaterial(
        field: String,
        value: String,
    ) {
        if (value in JVM_PRIMITIVE_DESCRIPTORS) {
            throw MetamodelFactContractViolationException(
                "$field must not be a JVM primitive descriptor: $value",
            )
        }

        if (value.startsWith("[", ignoreCase = false)) {
            throw MetamodelFactContractViolationException(
                "$field must not be a JVM array descriptor: $value",
            )
        }

        if (isWholeJvmObjectDescriptor(value)) {
            throw MetamodelFactContractViolationException(
                "$field must not be a JVM object descriptor: $value",
            )
        }

        if (containsEmbeddedJvmObjectDescriptor(value)) {
            throw MetamodelFactContractViolationException(
                "$field must not contain embedded JVM descriptor material: $value",
            )
        }

        if (value.indexOf('/') >= 0) {
            throw MetamodelFactContractViolationException(
                "$field must not contain JVM internal-name separator '/': $value",
            )
        }

        if (value.indexOf('$') >= 0) {
            throw MetamodelFactContractViolationException(
                "$field must use canonical source-style nesting, not JVM binary '$' names: $value",
            )
        }

        if (value.indexOf(';') >= 0) {
            throw MetamodelFactContractViolationException(
                "$field must not contain JVM descriptor terminator ';': $value",
            )
        }
    }

    private fun isWholeJvmObjectDescriptor(value: String): Boolean =
        value.length >= 3 &&
                value[0] == 'L' &&
                value[value.length - 1] == ';'

    /**
     * Detects descriptor-like fragments such as:
     *
     * - Lpkg/Foo;
     * - Lpkg.Foo;
     * - <Lpkg/Foo;>
     * - Map<Lpkg/Foo;,Bar>
     *
     * This does not attempt to parse JVM signatures. It only catches forbidden
     * residue patterns that should never reach canonical type text.
     */
    private fun containsEmbeddedJvmObjectDescriptor(value: String): Boolean {
        var index = 0

        while (index < value.length) {
            if (value[index] == 'L') {
                val terminatorIndex = value.indexOf(';', startIndex = index + 1)
                if (terminatorIndex > index + 1) {
                    val body = value.substring(index + 1, terminatorIndex)
                    if (looksLikeDescriptorBody(body)) {
                        return true
                    }
                }
            }

            index += 1
        }

        return false
    }

    private fun looksLikeDescriptorBody(body: String): Boolean {
        if (body.isEmpty()) return false

        var hasNameLikeAscii = false

        for (index in body.indices) {
            val c = body[index]

            when {
                c == '/' -> return true
                c == '$' -> return true
                c == '.' -> hasNameLikeAscii = true
                isAsciiIdentifierPart(c) -> hasNameLikeAscii = true
                else -> return false
            }
        }

        return hasNameLikeAscii
    }

    private fun scanAsciiProtocolMarkers(
        field: String,
        value: String,
        allowNullableMarker: Boolean,
        allowStarProjection: Boolean,
    ) {
        var index = 0

        while (index < value.length) {
            val c = value[index]

            when {
                isAsciiHorizontalOrLineWhitespace(c) -> {
                    throw MetamodelFactContractViolationException(
                        "$field must not contain ASCII whitespace at index=$index: $value",
                    )
                }

                c == '?' && !allowNullableMarker -> {
                    throw MetamodelFactContractViolationException(
                        "$field must not contain nullable marker '?' at index=$index. " +
                                "Nullability must be lowered before this boundary: $value",
                    )
                }

                c == '*' && !allowStarProjection -> {
                    throw MetamodelFactContractViolationException(
                        "$field must not contain raw star projection '*' at index=$index. " +
                                "Star projection must be lowered before this boundary: $value",
                    )
                }

                c == '[' -> {
                    requireArrayBracketPair(
                        field = field,
                        value = value,
                        index = index,
                    )
                    index += 2
                    continue
                }

                c == ']' -> {
                    throw MetamodelFactContractViolationException(
                        "$field contains unmatched array bracket ']' at index=$index: $value",
                    )
                }

                isAsciiIdentifierStart(c) -> {
                    index =
                        scanAsciiIdentifierToken(
                            field = field,
                            value = value,
                            startIndex = index,
                        )
                    continue
                }
            }

            index += 1
        }
    }

    private fun requireArrayBracketPair(
        field: String,
        value: String,
        index: Int,
    ) {
        val nextIndex = index + 1

        if (nextIndex >= value.length || value[nextIndex] != ']') {
            throw MetamodelFactContractViolationException(
                "$field must use canonical array suffix pairs '[]'. " +
                        "Invalid '[' at index=$index: $value",
            )
        }
    }

    private fun scanAsciiIdentifierToken(
        field: String,
        value: String,
        startIndex: Int,
    ): Int {
        var index = startIndex + 1

        while (index < value.length) {
            val c = value[index]
            if (!isAsciiIdentifierPart(c)) {
                break
            }
            index += 1
        }

        val token = value.substring(startIndex, index)

        if (token in SOURCE_VARIANCE_TOKENS) {
            throw MetamodelFactContractViolationException(
                "$field must not contain source variance token '$token' at index=$startIndex. " +
                        "Variance must be lowered before this boundary: $value",
            )
        }

        if (token in SOURCE_RESERVED_TOKENS) {
            throw MetamodelFactContractViolationException(
                "$field must not contain source reserved token '$token' at index=$startIndex. " +
                        "Source syntax must be lowered before this boundary: $value",
            )
        }

        if (isJvmDescriptorToken(token)) {
            throw MetamodelFactContractViolationException(
                "$field must not contain JVM descriptor token '$token' at index=$startIndex: $value",
            )
        }

        return index
    }

    private fun isJvmDescriptorToken(token: String): Boolean =
        token in JVM_PRIMITIVE_DESCRIPTORS ||
                isWholeJvmObjectDescriptor(token)

    private fun isAsciiIdentifierStart(c: Char): Boolean = c in 'A'..'Z' || c in 'a'..'z' || c == '_'

    private fun isAsciiIdentifierPart(c: Char): Boolean = isAsciiIdentifierStart(c) || c in '0'..'9'

    private fun isAsciiHorizontalOrLineWhitespace(c: Char): Boolean =
        c == ' ' ||
                c == '\n' ||
                c == '\r' ||
                c == '\t' ||
                c == '\u000B' ||
                c == '\u000C'
}
