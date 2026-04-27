package metamodel.domain.vo

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Immutable canonical annotation value.
 *
 * This is intentionally narrow for the current metamodel layer.
 * Complex values must be lowered by adapters into canonical scalar/list/nested
 * forms before entering TypeReference material.
 */
sealed interface AnnotationValue {
    fun compareToSameKind(other: AnnotationValue): Int

    val kindOrder: Int

    class StringValue private constructor(
        val value: String,
    ) : AnnotationValue {
        override val kindOrder: Int = 10

        override fun compareToSameKind(other: AnnotationValue): Int {
            require(other is StringValue)
            return CanonicalTextLaw.compareCanonicalStrings(value, other.value)
        }

        override fun equals(other: Any?): Boolean {
            return other is StringValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "StringValue($value)"
        }

        companion object {
            @JvmStatic
            fun issue(value: String): StringValue {
                CanonicalTextLaw.validateCanonicalTextValue(
                    field = "AnnotationValue.StringValue.value",
                    value = value,
                    allowEmpty = true,
                )
                return StringValue(value)
            }
        }
    }

    class BooleanValue private constructor(
        val value: Boolean,
    ) : AnnotationValue {
        override val kindOrder: Int = 20

        override fun compareToSameKind(other: AnnotationValue): Int {
            require(other is BooleanValue)
            return value.compareTo(other.value)
        }

        override fun equals(other: Any?): Boolean {
            return other is BooleanValue && value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "BooleanValue($value)"
        }

        companion object {
            @JvmStatic
            fun issue(value: Boolean): BooleanValue {
                return BooleanValue(value)
            }
        }
    }

    class IntValue private constructor(
        val value: Int,
    ) : AnnotationValue {
        override val kindOrder: Int = 30

        override fun compareToSameKind(other: AnnotationValue): Int {
            require(other is IntValue)
            return value.compareTo(other.value)
        }

        override fun equals(other: Any?): Boolean {
            return other is IntValue && value == other.value
        }

        override fun hashCode(): Int {
            return value
        }

        override fun toString(): String {
            return "IntValue($value)"
        }

        companion object {
            @JvmStatic
            fun issue(value: Int): IntValue {
                return IntValue(value)
            }
        }
    }

    companion object {
        fun compare(left: AnnotationValue, right: AnnotationValue): Int {
            val kind = left.kindOrder.compareTo(right.kindOrder)
            if (kind != 0) {
                return kind
            }

            return left.compareToSameKind(right)
        }
    }
}