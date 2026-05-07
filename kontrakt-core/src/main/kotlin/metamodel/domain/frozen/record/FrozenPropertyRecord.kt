package metamodel.domain.frozen.record

import metamodel.domain.frozen.availability.FrozenMetadataAvailability
import metamodel.domain.frozen.sequence.FrozenAnnotationRecordSequence

/**
 * Frozen property record.
 *
 * Availability is value/state material, not identity-key material.
 */
class FrozenPropertyRecord private constructor(
    val key: FrozenPropertyRecordKey,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val declarationAvailability: FrozenMetadataAvailability,
    val sourceAvailability: FrozenMetadataAvailability,
) {
    companion object {
        @JvmStatic
        fun issue(
            key: FrozenPropertyRecordKey,
            annotationRecords: FrozenAnnotationRecordSequence,
            declarationAvailability: FrozenMetadataAvailability,
            sourceAvailability: FrozenMetadataAvailability,
        ): FrozenPropertyRecord {
            return FrozenPropertyRecord(
                key = key,
                annotationRecords = annotationRecords,
                declarationAvailability = declarationAvailability,
                sourceAvailability = sourceAvailability,
            )
        }
    }
}