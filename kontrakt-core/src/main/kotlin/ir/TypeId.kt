package kontrakt.ir

import kontrakt.ir.exception.IrProtocolViolationException

/**
 * [Identity] A strongly-typed wrapper for JVM Runtime Class Names (Reference Types only).
 *
 * ## Contract & Safety
 * - **Factory Usage**: Must be created via [TypeId.of].
 * - **Strict Exception Policy**: Throws ONLY [IrProtocolViolationException] for syntax violations.
 * - **Scope**: Strictly for **Reference Types**. Primitives are BANNED.
 */
@JvmInline
value class TypeId private constructor(val value: String) : Comparable<TypeId> {

    override fun compareTo(other: TypeId): Int = value.compareTo(other.value)
    override fun toString(): String = value

    companion object {
        private val FORBIDDEN_CHARS = charArrayOf('<', '>', '[', ']', ';', '(', ')', ':')
        private val PRIMITIVE_NAMES = setOf(
            "byte", "char", "double", "float", "int", "long", "short", "boolean", "void"
        )

        /**
         * Safe Factory Method.
         * Validates syntax only. No external policy enforcement.
         */
        @JvmStatic
        fun of(value: String?): TypeId {
            if (value.isNullOrBlank()) {
                throw IrProtocolViolationException("TypeId syntax error: value is null or blank")
            }

            // [Syntax 1] Internal Separators & Empty Segments
            if (value.contains('/')) throw IrProtocolViolationException("TypeId syntax error: contains '/' ($value)")
            if (value.contains("..")) throw IrProtocolViolationException("TypeId syntax error: empty segment ($value)")
            if (value.startsWith(".") || value.endsWith(".")) throw IrProtocolViolationException("TypeId syntax error: starts/ends with dot ($value)")

            // [Syntax 2] Content Hygiene
            if (value.any { it.isWhitespace() || it.isISOControl() }) {
                throw IrProtocolViolationException("TypeId syntax error: contains whitespace/control chars ($value)")
            }

            // [Syntax 3] Block Signatures & Generics
            if (value.any { it in FORBIDDEN_CHARS }) {
                throw IrProtocolViolationException("TypeId syntax error: contains illegal char ($value)")
            }

            // [Syntax 4] Block Primitives
            if (value in PRIMITIVE_NAMES) {
                throw IrProtocolViolationException("TypeId syntax error: primitive type not allowed ($value)")
            }

            return TypeId(value)
        }
    }
}