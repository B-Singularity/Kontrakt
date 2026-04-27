package planning.domain.expansion.polymorphic

import planning.domain.exception.TypeExpansionContractViolationException

class RuntimeBindingScopeDepth private constructor(
    val value: Int,
) {
    override fun equals(other: Any?): Boolean {
        return other is RuntimeBindingScopeDepth && value == other.value
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return "scopeDepth($value)"
    }

    companion object {
        @JvmStatic
        fun root(): RuntimeBindingScopeDepth {
            return RuntimeBindingScopeDepth(0)
        }

        @JvmStatic
        fun of(value: Int): RuntimeBindingScopeDepth {
            if (value < 0) {
                throw TypeExpansionContractViolationException(
                    reason = "RuntimeBindingScopeDepth must be >= 0: $value",
                )
            }

            return RuntimeBindingScopeDepth(value)
        }
    }
}