package realization.identity

import stage.input.material.TypeShapeSummary
import stage.input.diagnostics.MetamodelFactContractViolationException
import stage.input.material.TypeCycleKey

/**
 * Scope-local coherence guard for TypeCycleKey issuance.
 *
 * This is not:
 *
 * - a value object;
 * - a global singleton;
 * - an interner;
 * - a cache;
 * - canonical persisted state;
 * - or a route-key table.
 *
 * This is a mutable, short-lived domain service used by a TypeReference factory
 * or ratifier while issuing a coherent group of type identities.
 *
 * Law:
 *
 * Within one issuance / ratification scope:
 *
 *     same cycle-key value -> same TypeShapeSummary
 *
 * This protects cycle detection from metamodel drift where two adapters or
 * classifier paths emit the same nullability-erased structural key with
 * different shape metadata.
 *
 * Why this is not inside TypeCycleKey:
 *
 * TypeCycleKey is a pure structural VO. It can validate its own text surface,
 * but it cannot know what has already been issued in the current scope.
 *
 * Scope-local mutable state belongs here, not in the VO.
 *
 * HashMap law:
 *
 * The HashMap below is a scope-local work buffer.
 * It is not canonical state, not persisted, and not iterated for deterministic
 * output.
 *
 * If profiling later shows this path to be a hot bottleneck, replace the map
 * with a primitive/order-specific table during the interning/allocation phase.
 */
class TypeCycleKeyCoherenceScope private constructor() {
    private val shapeByCycleValue: MutableMap<String, TypeShapeSummary> =
        HashMap()

    fun issue(
        value: String,
        shapeSummary: TypeShapeSummary,
    ): TypeCycleKey {
        val previous = shapeByCycleValue.putIfAbsent(
            value,
            shapeSummary,
        )

        if (previous != null && previous != shapeSummary) {
            throw MetamodelFactContractViolationException(
                "TypeCycleKey metamodel drift: same cycle value issued with different shape summary. " +
                        "value=$value, previous=$previous, actual=$shapeSummary",
            )
        }

        return TypeCycleKey.issue(
            value = value,
            shapeSummary = shapeSummary,
        )
    }

    companion object {
        @JvmStatic
        fun open(): TypeCycleKeyCoherenceScope {
            return TypeCycleKeyCoherenceScope()
        }
    }
}