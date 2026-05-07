package metamodel.domain.frozen.table

import metamodel.domain.dto.ResolvedTypeShape
import metamodel.domain.vo.TypeReference

/**
 * Frozen type shape table.
 *
 * Implementations must be immutable after freeze.
 *
 * Implementation law:
 *
 * Table implementations must be plain-data or index/slab-backed.
 *
 * They must not store:
 *
 * - lambdas;
 * - suppliers;
 * - lazy delegates;
 * - service locators;
 * - callbacks;
 * - closures capturing KType/KClass/KSType/KSDeclaration;
 * - registry keys that can recover backend handles.
 */
interface FrozenTypeShapeTable {
    fun contains(
        reference: TypeReference,
    ): Boolean

    fun findShape(
        reference: TypeReference,
    ): ResolvedTypeShape?
}