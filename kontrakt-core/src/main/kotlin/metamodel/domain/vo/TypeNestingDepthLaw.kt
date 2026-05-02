package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

/**
 * Shared structural type-depth law.
 *
 * This is separated from TypeReference and TypeIdentityCoherenceProof so both
 * sides enforce the same maximum without duplicating constants.
 *
 * Depth law:
 *
 * - leaf type depth = 1
 * - Array<T> depth = 1 + T.depth
 * - Collection<T> depth = 1 + T.depth
 * - Map<K, V> depth = 1 + max(K.depth, V.depth)
 * - Composite<T...> depth = 1 + max(typeArgument.depth)
 *
 * The issuing factory is responsible for computing the value while lowering the
 * type graph. Domain VOs only validate the already-computed depth.
 */
internal object TypeNestingDepthLaw {
    const val MAX_TYPE_NESTING_DEPTH: Int = 64

    fun requireWithinLimit(
        field: String,
        depth: Int,
    ) {
        if (depth <= 0) {
            throw MetamodelFactContractViolationException(
                "$field must be > 0: $depth",
            )
        }

        if (depth > MAX_TYPE_NESTING_DEPTH) {
            throw MetamodelFactContractViolationException(
                "$field exceeds protocol cap=$MAX_TYPE_NESTING_DEPTH: $depth",
            )
        }
    }
}
