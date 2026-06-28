package stage.lowering.boundary

import stage.canonicalization.material.TypeReference
import stage.input.material.ResolvedTypeShape

/**
 * Outbound type-shape port.
 *
 * Architectural role:
 * - Hexagonal driven port
 * - implemented by metamodel adapters such as reflection, KSP, bytecode, or static-source adapters
 *
 * This port answers only:
 *
 * "What coarse expansion shape does this TypeReference have?"
 *
 * It does not provide:
 * - constructor facts,
 * - property facts,
 * - active members,
 * - canonical traversal order,
 * - generator payloads.
 *
 * Planning Core uses this shape to choose the next expansion strategy.
 */
interface TypeShapeProvider {
    fun resolveTypeShape(reference: TypeReference): ResolvedTypeShape
}