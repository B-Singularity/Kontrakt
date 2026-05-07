package metamodel.domain.frozen.table

import metamodel.domain.vo.TypeReference
import planning.domain.expansion.TypeCycleIdentity

/**
 * Frozen type cycle identity table.
 *
 * Implementations must be immutable after freeze.
 *
 * The value returned from this table must already be adapter-neutral cycle
 * identity material. It must not be derived from KType/KSP symbols at provider
 * read time.
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
interface FrozenTypeCycleIdentityTable {
    fun contains(
        reference: TypeReference,
    ): Boolean

    fun findCycleIdentity(
        reference: TypeReference,
    ): TypeCycleIdentity?
}