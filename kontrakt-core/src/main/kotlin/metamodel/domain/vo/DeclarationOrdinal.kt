package metamodel.domain.vo

import metamodel.domain.exception.InvalidDeclarationOrdinalException

/**
 * Semantic declaration-ordinal fact.
 *
 * This is a metamodel value object, not a primitive sentinel.
 *
 * ADR-0030 / Active Member Projection protocol require declaration ordinal
 * availability to be explicit:
 *
 * - Present(n)
 * - Unavailable
 *
 * The semantic layer must never treat `-1` as the meaning itself.
 * Primitive hot-path lowering MAY encode Unavailable as -1, but only as a
 * mechanical representation detail.
 *
 * IMPORTANT:
 * This file deliberately does not use `data object` or `data class`.
 * Kontrakt value/snapshot surfaces avoid copy-style reconstruction backdoors
 * and prefer factory-issued immutable objects.
 */
sealed interface DeclarationOrdinal {

    val isPresent: Boolean

    /**
     * Mechanical primitive lowering.
     *
     * This is permitted only at primitive storage / comparator lowering boundaries.
     *
     * Semantic DTOs and semantic algorithms must continue to speak in terms of
     * DeclarationOrdinal.Present / DeclarationOrdinal.Unavailable.
     */
    fun lowerForPrimitiveOrdering(): Int

    /**
     * Deterministically reconstructed declaration ordinal.
     */
    class Present private constructor(
        val ordinal: Int,
    ) : DeclarationOrdinal {

        override val isPresent: Boolean = true

        override fun lowerForPrimitiveOrdering(): Int = ordinal

        override fun toString(): String = "Present($ordinal)"

        override fun equals(other: Any?): Boolean {
            return other is Present && other.ordinal == ordinal
        }

        override fun hashCode(): Int = ordinal

        companion object {
            @JvmStatic
            fun issue(
                ordinal: Int,
            ): Present {
                if (ordinal < 0) {
                    throw InvalidDeclarationOrdinalException(
                        ordinal = ordinal,
                        reason = "DeclarationOrdinal.Present.ordinal must be >= 0."
                    )
                }

                return Present(ordinal)
            }
        }
    }

    /**
     * Explicit unavailable declaration ordinal.
     *
     * This means:
     * - the adapter could not deterministically reconstruct declaration order.
     *
     * It does NOT mean:
     * - first declaration,
     * - zero,
     * - raw backend enumeration order,
     * - invented fallback order.
     *
     * This is a regular singleton object, not `data object`.
     */
    object Unavailable : DeclarationOrdinal {

        override val isPresent: Boolean = false

        override fun lowerForPrimitiveOrdering(): Int = UNAVAILABLE_PRIMITIVE_SENTINEL

        override fun toString(): String = "Unavailable"
    }

    companion object {
        const val UNAVAILABLE_PRIMITIVE_SENTINEL: Int = -1

        @JvmStatic
        fun present(
            ordinal: Int,
        ): DeclarationOrdinal = Present.issue(ordinal)

        @JvmStatic
        fun unavailable(): DeclarationOrdinal = Unavailable

        /**
         * Use only when importing an already-lowered primitive representation from
         * a mechanically constrained boundary.
         *
         * New semantic DTO construction should prefer present(...) or unavailable().
         */
        @JvmStatic
        fun fromPrimitiveLowering(
            value: Int,
        ): DeclarationOrdinal {
            return if (value == UNAVAILABLE_PRIMITIVE_SENTINEL) {
                Unavailable
            } else {
                Present.issue(value)
            }
        }
    }
}