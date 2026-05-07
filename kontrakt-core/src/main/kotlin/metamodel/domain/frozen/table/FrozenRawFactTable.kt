package metamodel.domain.frozen.table

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.vo.TypeReference

/**
 * Frozen raw fact table.
 *
 * The table may return an already materialized RawTypeFactsDTO or may
 * materialize it from frozen adapter-neutral raw fact records.
 *
 * It must not return to backend-native handles.
 *
 * Coverage law:
 *
 * contains(reference) means this table has explicit frozen coverage for the
 * reference.
 *
 * Coverage may be:
 *
 * - a materialized RawTypeFactsDTO;
 * - a frozen raw fact record;
 * - a deterministic sentinel record such as TRUNCATED;
 * - a deterministic sentinel record such as FILTERED_BY_POLICY;
 * - a deterministic sentinel record such as UNAVAILABLE_FROM_BACKEND;
 * - an ACQUISITION_FAILED diagnostic record.
 *
 * Missing coverage is an incomplete frozen image.
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
interface FrozenRawFactTable {
    fun contains(
        reference: TypeReference,
    ): Boolean

    fun findFacts(
        reference: TypeReference,
    ): RawTypeFactsDTO?
}