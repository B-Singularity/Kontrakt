package metamodel.domain.frozen.table

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.exception.FrozenMetamodelSequenceIndexOutOfBoundsException
import metamodel.domain.frozen.image.FrozenMetamodelImageId
import metamodel.domain.frozen.image.FrozenMetamodelImageSchemaVersion

/**
 * Object-array-backed FrozenRawFactTable.
 *
 * This is the Level 1 materialized-DTO raw fact table.
 *
 * It is still compliant with ADR-0039 because it stores adapter-neutral
 * RawTypeFactsDTO values, not backend-native handles.
 *
 * Lookup law:
 *
 * This table is addressed only by FrozenTypeReferenceIndex image-local frozen
 * type ordinal.
 *
 * Invalid ordinal access is a domain contract violation and must not be hidden
 * as a nullable miss.
 *
 * Difference:
 *
 * - valid ordinal + null slot:
 *   missing raw-fact coverage;
 *
 * - invalid ordinal:
 *   caller/index/table contract violation.
 *
 * Materialization law:
 *
 * findFactsAt(...) returns pre-frozen adapter-neutral DTO material.
 *
 * It must not:
 *
 * - perform backend discovery;
 * - reopen backend handles;
 * - allocate closure-backed lazy cells;
 * - be metered as backend raw fact resolution.
 *
 * Future table implementations may replace this representation with:
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
    private val imageId: FrozenMetamodelImageId,
    override val schemaVersion: FrozenMetamodelImageSchemaVersion,
    private val facts: Array<RawTypeFactsDTO?>,
) : FrozenRawFactTable {
    override val size: Int
        get() = facts.size

    override fun containsAt(
        frozenOrdinal: Int,
    ): Boolean {
        requireValidOrdinal(
            frozenOrdinal = frozenOrdinal,
        )

        return facts[frozenOrdinal] != null
    }

    override fun findFactsAt(
        frozenOrdinal: Int,
    ): RawTypeFactsDTO? {
        requireValidOrdinal(
            frozenOrdinal = frozenOrdinal,
        )

        return facts[frozenOrdinal]
    }

    private fun requireValidOrdinal(
        frozenOrdinal: Int,
    ) {
        if (frozenOrdinal >= 0 && frozenOrdinal < facts.size) {
            return
        }

        throw FrozenMetamodelSequenceIndexOutOfBoundsException(
            imageId = imageId,
            sequenceTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE.name,
            index = frozenOrdinal,
            size = facts.size,
        )
    }

    override fun toString(): String {
        return "ObjectArrayFrozenRawFactTable(size=$size, schemaVersion=$schemaVersion)"
    }

    companion object {
        @JvmStatic
        fun issue(
            imageId: FrozenMetamodelImageId,
            schemaVersion: FrozenMetamodelImageSchemaVersion,
            facts: Array<RawTypeFactsDTO?>,
        ): ObjectArrayFrozenRawFactTable {
            return ObjectArrayFrozenRawFactTable(
                imageId = imageId,
                schemaVersion = schemaVersion,
                facts = facts.copyOf(),
            )
        }
    }
}