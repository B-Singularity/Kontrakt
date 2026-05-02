package ir.spec

import java.util.Objects

/**
 * Stable composite key for TestSpecification.
 *
 * Why:
 * - Prevents accidental mismatch between (target, subject) and externally supplied keys.
 * - Used as a stable map key upstream.
 */
class SpecKey private constructor(
    val target: TypeId,
    val subjectConcrete: TypeId,
) {
    override fun equals(other: Any?): Boolean = other is SpecKey && target == other.target && subjectConcrete == other.subjectConcrete

    override fun hashCode(): Int = Objects.hash(target, subjectConcrete)

    override fun toString(): String = "SpecKey($target::$subjectConcrete)"

    companion object {
        @JvmStatic
        fun of(
            target: TypeId,
            subjectConcrete: TypeId,
        ): SpecKey = SpecKey(target, subjectConcrete)
    }
}
