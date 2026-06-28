package metamodel.domain.vo

import metamodel.domain.protocol.MetamodelProtocolOrdering
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Canonical annotation argument name.
 *
 * Identifier law:
 *
 * Annotation argument names are restricted to an ASCII identifier subset:
 *
 *     [A-Za-z_][A-Za-z0-9_]*
 *
 * Additional restrictions:
 *
 * - must not start with a digit;
 * - must not be the single underscore "_";
 * - must not be a Kotlin/Java reserved word;
 * - "value" is explicitly allowed because it is the Kotlin/JVM default
 *   annotation argument name.
 *
 * Ordering law:
 *
 * - "value" sorts first;
 * - all other names sort by UTF-16 code-unit order;
 * - case is significant;
 * - no locale, Collator, Unicode collation, or planning-domain text law.
 *
 * Hash law:
 *
 * hashCode() may use String.hashCode() for in-memory hash tables only.
 * It must not be used as a persisted fingerprint, route key, canonical digest,
 * or cross-runtime order hash.
 */
class AnnotationArgumentName private constructor(
    val value: String,
) : Comparable<AnnotationArgumentName> {
    override fun compareTo(other: AnnotationArgumentName): Int =
        AnnotationArgumentNameOrder.compare(
            left = this,
            right = other,
        )

    override fun equals(other: Any?): Boolean =
        other is AnnotationArgumentName &&
                value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        const val MAX_ANNOTATION_ARGUMENT_NAME_CHARS: Int = 128

        /**
         * Default annotation argument name in Kotlin/JVM annotation syntax.
         *
         * "value" is intentionally allowed even though it may appear as a
         * contextual/soft keyword in other language positions.
         */
        val DEFAULT_VALUE: AnnotationArgumentName = issue("value")

        private val RESERVED_WORDS: Set<String> =
            setOf(
                "as",
                "break",
                "class",
                "continue",
                "do",
                "else",
                "false",
                "for",
                "fun",
                "if",
                "in",
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
                "actual",
                "abstract",
                "annotation",
                "by",
                "catch",
                "companion",
                "const",
                "constructor",
                "crossinline",
                "data",
                "dynamic",
                "enum",
                "expect",
                "external",
                "field",
                "file",
                "final",
                "finally",
                "get",
                "import",
                "init",
                "inline",
                "inner",
                "internal",
                "lateinit",
                "noinline",
                "open",
                "operator",
                "out",
                "override",
                "param",
                "private",
                "property",
                "protected",
                "public",
                "receiver",
                "reified",
                "sealed",
                "set",
                "setparam",
                "suspend",
                "tailrec",
                "vararg",
                "where",
                "assert",
                "boolean",
                "byte",
                "case",
                "char",
                "default",
                "double",
                "extends",
                "float",
                "goto",
                "implements",
                "instanceof",
                "int",
                "long",
                "native",
                "new",
                "short",
                "static",
                "strictfp",
                "switch",
                "synchronized",
                "throws",
                "transient",
                "void",
                "volatile",
            )

        @JvmStatic
        fun issue(value: String): AnnotationArgumentName {
            MetamodelProtocolTextGuards.requireAsciiIdentifierToken(
                field = "AnnotationArgumentName.value",
                value = value,
                maxChars = MAX_ANNOTATION_ARGUMENT_NAME_CHARS,
            )

            requireSourceSafeIdentifier(value)

            return AnnotationArgumentName(value)
        }

        private fun requireSourceSafeIdentifier(value: String) {
            if (value == "_") {
                throw MetamodelFactContractViolationException(
                    "AnnotationArgumentName.value must not be the single underscore '_'.",
                )
            }

            if (value[0] in '0'..'9') {
                throw MetamodelFactContractViolationException(
                    "AnnotationArgumentName.value must not start with a digit.",
                )
            }

            if (value != "value" && value in RESERVED_WORDS) {
                throw MetamodelFactContractViolationException(
                    "AnnotationArgumentName.value must not be a reserved source keyword: $value",
                )
            }
        }
    }
}

/**
 * Protocol-defined ordering for annotation argument names.
 *
 * This object owns only the annotation-argument-specific policy:
 *
 * - "value" first;
 * - otherwise shared UTF-16 code-unit ordering.
 */
private object AnnotationArgumentNameOrder {
    fun compare(
        left: AnnotationArgumentName,
        right: AnnotationArgumentName,
    ): Int {
        if (left.value == right.value) {
            return 0
        }

        val leftIsDefault = left.value == "value"
        val rightIsDefault = right.value == "value"

        if (leftIsDefault && !rightIsDefault) {
            return -1
        }

        if (!leftIsDefault && rightIsDefault) {
            return 1
        }

        return MetamodelProtocolOrdering.compareUtf16CodeUnits(
            left = left.value,
            right = right.value,
        )
    }
}
