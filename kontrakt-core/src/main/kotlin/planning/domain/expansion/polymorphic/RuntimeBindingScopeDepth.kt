package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

/**
 * Depth of a runtime binding scope.
 *
 * This is not:
 *
 * - a type nesting depth;
 * - a graph traversal depth;
 * - a coroutine depth;
 * - a call-stack depth;
 * - or a cache key.
 *
 * It represents the logical depth of runtime binding expansion/ratification
 * scopes.
 *
 * Root law:
 *
 * The root binding scope has depth 0.
 * root() and of(0) return the same singleton instance.
 *
 * Resource law:
 *
 * Runtime binding depth is capped. This prevents malicious or broken
 * polymorphic expansion paths from creating extremely deep binding scopes that
 * later cause stack overflow, recursive comparison hazards, or unbounded
 * resource consumption.
 *
 * Arithmetic law:
 *
 * next() and plus(...) perform checked arithmetic through Long widening before
 * converting back to Int. This prevents integer overflow from silently wrapping
 * the scope depth.
 *
 * Allocation law:
 *
 * - root() returns a singleton.
 * - of(0) returns the same singleton.
 * - plus(0) returns this.
 * - positive increments return a new immutable VO.
 *
 * Do not introduce a general small-value cache here yet. That belongs to the
 * later flyweight / interning / allocation-policy phase.
 *
 * Hash law:
 *
 * hashCode() returns the integer value for in-memory equality collections only.
 * It must not be used as a canonical fingerprint, persisted key, route key, or
 * cross-runtime protocol hash.
 */
class RuntimeBindingScopeDepth private constructor(
    val value: Int,
) : Comparable<RuntimeBindingScopeDepth> {
    fun next(): RuntimeBindingScopeDepth = plus(1)

    fun plus(increment: Int): RuntimeBindingScopeDepth {
        if (increment < 0) {
            throw MetamodelFactContractViolationException(
                "RuntimeBindingScopeDepth increment must be >= 0: $increment",
            )
        }

        if (increment == 0) {
            return this
        }

        val result = value.toLong() + increment.toLong()

        if (result > RuntimeBindingScopeDepthLaw.MAX_RUNTIME_BINDING_SCOPE_DEPTH) {
            throw MetamodelFactContractViolationException(
                "RuntimeBindingScopeDepth overflow: " +
                    "current=$value, increment=$increment, " +
                    "max=${RuntimeBindingScopeDepthLaw.MAX_RUNTIME_BINDING_SCOPE_DEPTH}",
            )
        }

        return of(result.toInt())
    }

    /**
     * Returns primitive Int after checked addition.
     *
     * Use this in tight loops when callers want to avoid allocating a new
     * RuntimeBindingScopeDepth for every intermediate step.
     *
     * The final value should be wrapped with RuntimeBindingScopeDepth.of(...)
     * at the domain boundary.
     */
    fun plusToInt(increment: Int): Int {
        if (increment < 0) {
            throw MetamodelFactContractViolationException(
                "RuntimeBindingScopeDepth increment must be >= 0: $increment",
            )
        }

        if (increment == 0) {
            return value
        }

        val result = value.toLong() + increment.toLong()

        if (result > RuntimeBindingScopeDepthLaw.MAX_RUNTIME_BINDING_SCOPE_DEPTH) {
            throw MetamodelFactContractViolationException(
                "RuntimeBindingScopeDepth overflow: " +
                    "current=$value, increment=$increment, " +
                    "max=${RuntimeBindingScopeDepthLaw.MAX_RUNTIME_BINDING_SCOPE_DEPTH}",
            )
        }

        return result.toInt()
    }

    override fun compareTo(other: RuntimeBindingScopeDepth): Int =
        when {
            value < other.value -> -1
            value > other.value -> 1
            else -> 0
        }

    override fun equals(other: Any?): Boolean =
        other is RuntimeBindingScopeDepth &&
            value == other.value

    override fun hashCode(): Int = value

    override fun toString(): String = "runtimeBindingScopeDepth($value)"

    companion object {
        private val ROOT = RuntimeBindingScopeDepth(0)

        @JvmStatic
        fun of(value: Int): RuntimeBindingScopeDepth {
            RuntimeBindingScopeDepthLaw.requireWithinLimit(
                field = "RuntimeBindingScopeDepth.value",
                depth = value,
            )

            if (value == 0) {
                return ROOT
            }

            return RuntimeBindingScopeDepth(value)
        }

        @JvmStatic
        fun root(): RuntimeBindingScopeDepth = ROOT

        @JvmStatic
        fun maxValue(): Int = RuntimeBindingScopeDepthLaw.MAX_RUNTIME_BINDING_SCOPE_DEPTH
    }
}

/**
 * Shared law for runtime binding scope depth.
 *
 * This is separated from RuntimeBindingScopeDepth so factories, binders, and
 * verification services can reuse the same cap without duplicating constants.
 */
internal object RuntimeBindingScopeDepthLaw {
    /**
     * Conservative protocol cap for runtime binding scope depth.
     *
     * Runtime binding scopes should be shallow in normal use. A depth of 256 is
     * intentionally generous while still preventing pathological recursive
     * expansion from entering the domain.
     *
     * Raising this value should be treated as a metamodel protocol amendment.
     */
    const val MAX_RUNTIME_BINDING_SCOPE_DEPTH: Int = 256

    fun requireWithinLimit(
        field: String,
        depth: Int,
    ) {
        if (depth < 0) {
            throw MetamodelFactContractViolationException(
                "$field must be >= 0: $depth",
            )
        }

        if (depth > MAX_RUNTIME_BINDING_SCOPE_DEPTH) {
            throw MetamodelFactContractViolationException(
                "$field exceeds protocol cap=$MAX_RUNTIME_BINDING_SCOPE_DEPTH: $depth",
            )
        }
    }
}
