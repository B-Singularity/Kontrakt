package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.MetamodelProtocolOrdering
import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Canonical qualified name of an annotation type.
 *
 * This is not:
 *
 * - a JVM descriptor;
 * - a JVM internal name;
 * - a reflection Class name;
 * - a KSP declaration handle;
 * - a source import statement;
 * - or a planning-domain canonical text object.
 *
 * Dependency law:
 *
 * The metamodel domain must not depend on planning.domain.*.
 * This VO uses only metamodel-owned protocol guards.
 *
 * Shape law:
 *
 * The qualified name is a dot-separated ASCII identifier path:
 *
 *     segment('.'segment)*
 *
 * where each segment follows:
 *
 *     [A-Za-z_][A-Za-z0-9_]*
 *
 * This intentionally rejects:
 *
 * - empty segments;
 * - leading dot;
 * - trailing dot;
 * - consecutive dots;
 * - JVM internal-name slash '/';
 * - JVM binary-name dollar '$';
 * - descriptor terminator ';';
 * - pipe delimiter '|';
 * - control characters;
 * - spaces and punctuation;
 * - Unicode confusables.
 *
 * Case law:
 *
 * Case is significant. Do not lowercase or uppercase this value.
 *
 * Ordering law:
 *
 * Ordering is protocol-defined:
 *
 * - ASCII code-unit order over the full qualified name;
 * - case-sensitive;
 * - dot participates by its ASCII code point;
 * - no locale, Collator, Unicode collation, or planning-domain text law.
 *
 * Resource law:
 *
 * Qualified name length is capped to prevent allocation-based DoS in sorting,
 * hashing, equality, and map/set membership.
 *
 * Hash law:
 *
 * hashCode() may use String.hashCode() for in-memory hash tables only.
 * It must not be used as a persisted fingerprint, route key, canonical digest,
 * or cross-runtime protocol hash.
 */
class AnnotationQualifiedName private constructor(
    val value: String,
) : Comparable<AnnotationQualifiedName> {
    override fun compareTo(
        other: AnnotationQualifiedName,
    ): Int {
        return AnnotationQualifiedNameOrder.compare(
            left = this,
            right = other,
        )
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        return other is AnnotationQualifiedName &&
                value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

    companion object {
        const val MAX_ANNOTATION_QUALIFIED_NAME_CHARS: Int = 512

        private const val MAX_DIAGNOSTIC_VALUE_CHARS: Int = 128

        @JvmStatic
        fun issue(
            value: String,
        ): AnnotationQualifiedName {
            requireQualifiedNameSurface(value)

            return AnnotationQualifiedName(value)
        }

        private fun requireQualifiedNameSurface(
            value: String,
        ) {
            MetamodelProtocolTextGuards.requireLength(
                field = "AnnotationQualifiedName.value",
                value = value,
                maxChars = MAX_ANNOTATION_QUALIFIED_NAME_CHARS,
                allowEmpty = false,
            )

            var segmentStart = 0
            var index = 0

            while (index < value.length) {
                val c = value[index]

                /*
                 * Dot is the only delimiter allowed by this VO.
                 *
                 * Handle it explicitly before calling generic protocol-char
                 * guards so future changes to MetamodelProtocolTextGuards cannot
                 * accidentally invalidate qualified-name delimiter semantics.
                 */
                if (c == '.') {
                    requireNonEmptySegment(
                        value = value,
                        segmentStart = segmentStart,
                        segmentEndExclusive = index,
                    )

                    segmentStart = index + 1
                    index += 1
                    continue
                }

                MetamodelProtocolTextGuards.requireProtocolChar(
                    field = "AnnotationQualifiedName.value[$index]",
                    value = c,
                )

                val segmentOffset = index - segmentStart

                if (segmentOffset == 0) {
                    if (!isAsciiIdentifierStart(c)) {
                        throw MetamodelFactContractViolationException(
                            "AnnotationQualifiedName segment must start with an ASCII identifier-start character: " +
                                    "index=$index, value=${diagnosticValue(value)}",
                        )
                    }
                } else {
                    if (!isAsciiIdentifierPart(c)) {
                        throw MetamodelFactContractViolationException(
                            "AnnotationQualifiedName segment contains a non-canonical ASCII identifier-part character: " +
                                    "index=$index, value=${diagnosticValue(value)}",
                        )
                    }
                }

                index += 1
            }

            requireNonEmptySegment(
                value = value,
                segmentStart = segmentStart,
                segmentEndExclusive = value.length,
            )
        }

        private fun requireNonEmptySegment(
            value: String,
            segmentStart: Int,
            segmentEndExclusive: Int,
        ) {
            if (segmentStart >= segmentEndExclusive) {
                throw MetamodelFactContractViolationException(
                    "AnnotationQualifiedName must not contain empty segments: ${diagnosticValue(value)}",
                )
            }
        }

        private fun isAsciiIdentifierStart(
            c: Char,
        ): Boolean {
            return c in 'A'..'Z' ||
                    c in 'a'..'z' ||
                    c == '_'
        }

        private fun isAsciiIdentifierPart(
            c: Char,
        ): Boolean {
            return isAsciiIdentifierStart(c) ||
                    c in '0'..'9'
        }

        private fun diagnosticValue(
            value: String,
        ): String {
            if (value.length <= MAX_DIAGNOSTIC_VALUE_CHARS) {
                return value
            }

            return value.substring(0, MAX_DIAGNOSTIC_VALUE_CHARS) + "...<truncated>"
        }
    }
}

/**
 * Protocol-defined ordering for annotation qualified names.
 */
private object AnnotationQualifiedNameOrder {
    fun compare(
        left: AnnotationQualifiedName,
        right: AnnotationQualifiedName,
    ): Int {
        if (left.value == right.value) {
            return 0
        }

        return MetamodelProtocolOrdering.compareUtf16CodeUnits(
            left = left.value,
            right = right.value,
        )
    }
}