package metamodel.domain.frozen.table

import metamodel.domain.dto.RawTypeFactsDTO

/**
 * Object-array-backed FrozenRawFactTable.
 *
 * This is the transitional materialized-DTO raw fact table.
 *
 * It is still compliant with ADR-0039 because it stores adapter-neutral
 * RawTypeFactsDTO values, not backend-native handles.
 *
 * Future table implementations may replace this with:
 *
 * - frozen raw fact records;
 * - sentinel records;
 * - ordinal-indexed slabs;
 * - primitive metadata slabs.
 *
 * Provider contracts do not need to change as long as findFactsAt(...) remains
 * addressed by image-local frozen type ordinal.
 */
class ObjectArrayFrozenRawFactTable private constructor(
    private val facts: Array<RawTypeFactsDTO?>,
) : FrozenRawFactTable {
    override val size: Int
        get() = facts.size

    override fun containsAt(
        frozenTypeOrdinal: Int,
    ): Boolean =
        isInBounds(frozenTypeOrdinal) && facts[frozenTypeOrdinal] != null

    override fun findFactsAt(
        frozenTypeOrdinal: Int,
    ): RawTypeFactsDTO? {
        if (!isInBounds(frozenTypeOrdinal)) {
            return null
        }

        return facts[frozenTypeOrdinal]
    }

    private fun isInBounds(
        frozenTypeOrdinal: Int,
    ): Boolean =
        frozenTypeOrdinal >= 0 && frozenTypeOrdinal < facts.size

    companion object {
        @JvmStatic
        fun issue(
            facts: Array<RawTypeFactsDTO?>,
        ): ObjectArrayFrozenRawFactTable =
            ObjectArrayFrozenRawFactTable(
                facts = facts.copyOf(),
            )
    }
}