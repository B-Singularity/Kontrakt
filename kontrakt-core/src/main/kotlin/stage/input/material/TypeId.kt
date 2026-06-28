package stage.input.material

import ir.IrLimits
import ir.exception.IrProtocolViolationException

/**
 * Protocol-safe type identifier.
 *
 * This type is meant to represent a stable, human-readable type token
 * while rejecting JVM descriptor/internal-name leakage.
 *
 * Hygiene constraints:
 * - No whitespace / control chars
 * - No descriptor-ish tokens: < > [ ] ; ( ) :
 * - No internal-name separator: /
 * - No order delimiter: |
 * - No primitives (to enforce explicit modeling decisions upstream)
 */
@JvmInline
value class TypeId private constructor(
    val value: String,
) : Comparable<TypeId> {
    override fun compareTo(other: TypeId): Int = value.compareTo(other.value)

    override fun toString(): String = value

    companion object {
        /**
         * We forbid descriptor-ish chars and order delimiters.
         * - '/' blocks JVM internal names leaking into the order
         * - '|' blocks delimiter/injectivity attacks across composite keys/logging
         */
        private val FORBIDDEN =
            charArrayOf(
                '<',
                '>',
                '[',
                ']',
                ';',
                '(',
                ')',
                ':',
                '/',
                '|',
            )

        private val PRIMITIVES =
            setOf(
                "byte",
                "char",
                "double",
                "float",
                "int",
                "long",
                "short",
                "boolean",
                "void",
            )

        @JvmStatic
        fun of(value: String?): TypeId {
            if (value.isNullOrBlank()) throw IrProtocolViolationException("TypeId blank.")
            if (value.length > IrLimits.MAX_TYPE_ID_LENGTH) throw IrProtocolViolationException("TypeId too long.")
            if (value.any { it.isWhitespace() || it.isISOControl() }) throw IrProtocolViolationException("Hygiene failed.")
            if (value.any { it in FORBIDDEN }) throw IrProtocolViolationException("Illegal char.")
            if (value in PRIMITIVES) throw IrProtocolViolationException("Primitive not allowed.")
            return TypeId(value)
        }
    }
}