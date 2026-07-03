package stage.canonicalization.material

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.input.presentation.raw.TypeShapeSummary

/**
 * Canonical rendered type signature.
 *
 * This value object represents the canonical signature surface after raw
 * reflection/KSP/source material has been lowered by adapters and ratified by
 * metamodel identity rules.
 *
 * This is not:
 *
 * - a JVM descriptor;
 * - a JVM generic signature;
 * - a reflection Type;
 * - a KSP symbol;
 * - a parser AST;
 * - a cache key;
 * - a lowercase-normalized name;
 * - or a Unicode normalization boundary.
 *
 * Case law:
 *
 * Type signatures are case-sensitive. This VO must not lowercase or uppercase
 * the signature text. In Kotlin/JVM-like ecosystems, User and user can legally
 * represent different names. Case folding would destroy semantic identity.
 *
 * NFC law:
 *
 * This VO does not call NormalizationEngine directly.
 *
 * The signature value passed here must already be composed from ratified
 * CanonicalTypeText / CanonicalTypeId / TypeReference material, or from a
 * dedicated CanonicalTypeSignatureIssuer that owns NormalizationEngine access.
 *
 * Do not feed raw adapter/reflection/KSP strings directly into this VO.
 *
 * Shape law:
 *
 * shapeSummary is carried to avoid reparsing the signature on hot paths, but
 * this VO does not treat shapeSummary as blindly trusted. It performs cheap
 * contradiction checks between the rendered signature and the supplied summary.
 *
 * These checks are intentionally not a full parser. Full structural truth
 * belongs to ResolvedTypeShape and ExpansionPlan.
 *
 * Resource law:
 *
 * Signature length and generic nesting depth are capped. Very large generated
 * or adversarial signatures are rejected at this boundary to prevent
 * allocation-based DoS, stack-depth hazards in downstream parsers, and expensive
 * hash/equality work later.
 *
 * Persistence law:
 *
 * issue(...) creates current-schema signatures only. Persisted or imported
 * signature material must be restored through a separate artifact verifier /
 * migration boundary. Do not add silent schema upgrading here.
 */
class CanonicalTypeSignature private constructor(
    val value: String,
    val shapeSummary: TypeShapeSummary,
    val schemaVersion: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanonicalTypeSignature) return false

        return value == other.value &&
                shapeSummary == other.shapeSummary &&
                schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + shapeSummary.hashCode()
        result = 31 * result + schemaVersion
        return result
    }

    override fun toString(): String = value

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        /**
         * Protocol cap for canonical type signatures.
         *
         * This is deliberately larger than CanonicalTypeText because signatures
         * may include generic arguments and array suffixes. Raising this cap
         * should be treated as a order amendment and covered by golden
         * vectors.
         */
        const val MAX_SIGNATURE_CHARS: Int = 2_048

        /**
         * Protocol cap for generic nesting depth.
         *
         * This protects downstream recursive parsers/planners even though this
         * VO itself scans in O(N).
         */
        const val MAX_GENERIC_DEPTH: Int = 32

        @JvmStatic
        fun issue(
            value: String,
            shapeSummary: TypeShapeSummary,
        ): CanonicalTypeSignature {
            requireSignatureSurface(value)
            requireFastShapeCrossCheck(
                value = value,
                shapeSummary = shapeSummary,
            )

            return CanonicalTypeSignature(
                value = value,
                shapeSummary = shapeSummary,
                schemaVersion = CURRENT_SCHEMA_VERSION,
            )
        }

        private fun requireSignatureSurface(value: String) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value must not be empty.",
                )
            }

            if (value.length > MAX_SIGNATURE_CHARS) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value exceeds order cap=$MAX_SIGNATURE_CHARS.",
                )
            }

            rejectReservedProtocolMaterial(value)
            rejectControlCharacters(value)
            rejectAsciiSpace(value)
            rejectJvmDescriptorResidue(value)
            requireBalancedGenericDelimiters(value)
            requireArraySuffixPairs(value)
        }

        private fun rejectReservedProtocolMaterial(value: String) {
            if (value.indexOf('|') >= 0) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value contains reserved order delimiter '|'.",
                )
            }
        }

        /**
         * Rejects C0 and C1 control characters without relying on Character.*.
         *
         * This keeps the domain core independent from host JRE Unicode category
         * tables while still blocking non-printing control material.
         */
        private fun rejectControlCharacters(value: String) {
            for (index in value.indices) {
                val code = value[index].code
                val isC0Control = code in 0x0000..0x001F
                val isC1Control = code in 0x007F..0x009F

                if (isC0Control || isC1Control) {
                    throw MetamodelFactContractViolationException(
                        "CanonicalTypeSignature.value contains a control character at index=$index.",
                    )
                }
            }
        }

        private fun rejectAsciiSpace(value: String) {
            val index = value.indexOf(' ')
            if (index >= 0) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value must not contain ASCII space at index=$index.",
                )
            }
        }

        private fun rejectJvmDescriptorResidue(value: String) {
            if (value.length == 1 && isJvmPrimitiveDescriptor(value[0])) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value must not be a JVM primitive descriptor.",
                )
            }

            if (value.startsWith("[")) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value must not be a JVM array descriptor.",
                )
            }

            if (value.length >= 3 && value[0] == 'L' && value[value.length - 1] == ';') {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value must not be a JVM object descriptor.",
                )
            }

            if (
                value.indexOf('/') >= 0 ||
                value.indexOf('$') >= 0 ||
                value.indexOf(';') >= 0
            ) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value contains JVM descriptor/internal/binary-name residue.",
                )
            }
        }

        private fun isJvmPrimitiveDescriptor(c: Char): Boolean =
            c == 'B' ||
                    c == 'C' ||
                    c == 'D' ||
                    c == 'F' ||
                    c == 'I' ||
                    c == 'J' ||
                    c == 'S' ||
                    c == 'Z' ||
                    c == 'V'

        private fun requireBalancedGenericDelimiters(value: String) {
            var depth = 0

            for (index in value.indices) {
                when (value[index]) {
                    '<' -> {
                        depth += 1

                        if (depth > MAX_GENERIC_DEPTH) {
                            throw MetamodelFactContractViolationException(
                                "CanonicalTypeSignature.value exceeds max generic depth=$MAX_GENERIC_DEPTH.",
                            )
                        }
                    }

                    '>' -> {
                        depth -= 1

                        if (depth < 0) {
                            throw MetamodelFactContractViolationException(
                                "CanonicalTypeSignature.value contains unmatched '>' at index=$index.",
                            )
                        }
                    }
                }
            }

            if (depth != 0) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature.value contains unbalanced generic delimiters.",
                )
            }
        }

        private fun requireArraySuffixPairs(value: String) {
            var index = 0

            while (index < value.length) {
                val c = value[index]

                if (c == '[') {
                    val nextIndex = index + 1
                    if (nextIndex >= value.length || value[nextIndex] != ']') {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature.value must use array suffix pair '[]'.",
                        )
                    }

                    index += 2
                    continue
                }

                if (c == ']') {
                    throw MetamodelFactContractViolationException(
                        "CanonicalTypeSignature.value contains unmatched ']'.",
                    )
                }

                index += 1
            }
        }

        /**
         * Performs cheap contradiction checks between signature text and
         * TypeShapeSummary.
         *
         * This is not a parser and must not become one. The goal is only to
         * reject impossible combinations early, for example:
         *
         * - ARRAY summary without trailing [] suffixes;
         * - ATOMIC summary with generic delimiters;
         * - MAP summary without at least two top-level generic arguments;
         * - non-array summary with trailing array suffixes.
         */
        private fun requireFastShapeCrossCheck(
            value: String,
            shapeSummary: TypeShapeSummary,
        ) {
            val lexicalFacts = SignatureLexicalFacts.scan(value)

            if (lexicalFacts.trailingArrayRank != shapeSummary.arrayRank) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature array rank mismatch: " +
                            "signatureRank=${lexicalFacts.trailingArrayRank}, " +
                            "summaryRank=${shapeSummary.arrayRank}.",
                )
            }

            when (shapeSummary.kind) {
                CanonicalTypeShapeKind.VOID,
                CanonicalTypeShapeKind.UNIT,
                CanonicalTypeShapeKind.ATOMIC,
                CanonicalTypeShapeKind.ENUM,
                    -> {
                    if (lexicalFacts.hasGenericDelimiters) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature terminal shape must not contain generic delimiters: " +
                                    "kind=${shapeSummary.kind.protocolToken}.",
                        )
                    }

                    if (lexicalFacts.trailingArrayRank != 0) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature terminal shape must not contain array suffixes: " +
                                    "kind=${shapeSummary.kind.protocolToken}.",
                        )
                    }
                }

                CanonicalTypeShapeKind.ARRAY -> {
                    if (lexicalFacts.trailingArrayRank <= 0) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature ARRAY summary requires trailing array suffix.",
                        )
                    }

                    if (
                        shapeSummary.hasGenericComponent &&
                        !lexicalFacts.componentPartBeforeArraySuffixContainsGenericDelimiters
                    ) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature ARRAY summary reports generic component, " +
                                    "but signature component has no generic delimiters.",
                        )
                    }

                    if (
                        !shapeSummary.hasGenericComponent &&
                        shapeSummary.componentGenericArityHint != null &&
                        shapeSummary.componentGenericArityHint!! > 0
                    ) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature ARRAY summary contains inconsistent generic component hint.",
                        )
                    }
                }

                CanonicalTypeShapeKind.COLLECTION,
                CanonicalTypeShapeKind.MAP,
                    -> {
                    requireTopLevelGenericArityAtLeast(
                        value = value,
                        lexicalFacts = lexicalFacts,
                        kind = shapeSummary.kind,
                        minimum = shapeSummary.kind.minimumGenericArity,
                    )
                }

                CanonicalTypeShapeKind.COMPOSITE,
                CanonicalTypeShapeKind.INTERFACE,
                CanonicalTypeShapeKind.SEALED_INTERFACE,
                CanonicalTypeShapeKind.ABSTRACT_CLASS,
                CanonicalTypeShapeKind.SEALED_CLASS,
                    -> {
                    if (lexicalFacts.trailingArrayRank != 0) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature non-array structural/polymorphic shape must not contain array suffixes: " +
                                    "kind=${shapeSummary.kind.protocolToken}.",
                        )
                    }

                    if (
                        shapeSummary.genericArity > 0 &&
                        !lexicalFacts.hasGenericDelimiters
                    ) {
                        throw MetamodelFactContractViolationException(
                            "CanonicalTypeSignature summary reports generic arity but signature has no generic delimiters: " +
                                    "kind=${shapeSummary.kind.protocolToken}, " +
                                    "genericArity=${shapeSummary.genericArity}.",
                        )
                    }
                }
            }
        }

        private fun requireTopLevelGenericArityAtLeast(
            value: String,
            lexicalFacts: SignatureLexicalFacts,
            kind: CanonicalTypeShapeKind,
            minimum: Int,
        ) {
            if (!lexicalFacts.hasGenericDelimiters) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature ${kind.protocolToken} summary requires generic delimiters.",
                )
            }

            val actual = countTopLevelGenericArguments(value)

            if (actual < minimum) {
                throw MetamodelFactContractViolationException(
                    "CanonicalTypeSignature ${kind.protocolToken} summary requires at least $minimum " +
                            "top-level generic arguments, actual=$actual.",
                )
            }
        }

        private fun countTopLevelGenericArguments(value: String): Int {
            val openIndex = value.indexOf('<')
            if (openIndex < 0) return 0

            val closeIndex = findMatchingTopLevelClose(value, openIndex)
            if (closeIndex <= openIndex + 1) return 0

            var count = 1
            var depth = 0
            var index = openIndex + 1

            while (index < closeIndex) {
                when (value[index]) {
                    '<' -> depth += 1
                    '>' -> depth -= 1
                    ',' -> {
                        if (depth == 0) {
                            count += 1
                        }
                    }
                }

                index += 1
            }

            return count
        }

        private fun findMatchingTopLevelClose(
            value: String,
            openIndex: Int,
        ): Int {
            var depth = 0
            var index = openIndex

            while (index < value.length) {
                when (value[index]) {
                    '<' -> depth += 1
                    '>' -> {
                        depth -= 1
                        if (depth == 0) {
                            return index
                        }
                    }
                }

                index += 1
            }

            return -1
        }
    }
}

/**
 * Cheap lexical facts extracted from a canonical signature string.
 *
 * This is not a full parser. It is intentionally small and ASCII-only.
 */
private class SignatureLexicalFacts private constructor(
    val hasGenericDelimiters: Boolean,
    val trailingArrayRank: Int,
    val componentPartBeforeArraySuffixContainsGenericDelimiters: Boolean,
) {
    companion object {
        fun scan(value: String): SignatureLexicalFacts {
            val rank = countTrailingArrayRank(value)
            val componentEndExclusive = value.length - (rank * 2)
            val componentHasGenericDelimiters =
                componentEndExclusive > 0 &&
                        containsGenericDelimiterBefore(
                            value = value,
                            endExclusive = componentEndExclusive,
                        )

            return SignatureLexicalFacts(
                hasGenericDelimiters = value.indexOf('<') >= 0 || value.indexOf('>') >= 0,
                trailingArrayRank = rank,
                componentPartBeforeArraySuffixContainsGenericDelimiters = componentHasGenericDelimiters,
            )
        }

        private fun countTrailingArrayRank(value: String): Int {
            var rank = 0
            var index = value.length - 2

            while (index >= 0) {
                if (value[index] == '[' && value[index + 1] == ']') {
                    rank += 1
                    index -= 2
                } else {
                    break
                }
            }

            return rank
        }

        private fun containsGenericDelimiterBefore(
            value: String,
            endExclusive: Int,
        ): Boolean {
            var index = 0

            while (index < endExclusive) {
                val c = value[index]
                if (c == '<' || c == '>') {
                    return true
                }
                index += 1
            }

            return false
        }
    }
}
