package kontrakt.ir

import java.util.Objects

/**
 * [Structure] A composite key to uniquely identify a Test Specification within the IR.
 *
 * Implemented as a Plain Class to prevent `copy()` backdoors, aligning with the Sovereign Protocol philosophy.
 */
class SpecKey(
    val target: TypeId,
    val subjectConcrete: TypeId
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpecKey) return false
        return target == other.target && subjectConcrete == other.subjectConcrete
    }

    override fun hashCode(): Int = Objects.hash(target, subjectConcrete)

    override fun toString(): String = "SpecKey($target::$subjectConcrete)"
}