package metamodel.domain.frozen.record

import metamodel.domain.frozen.availability.FrozenMetadataAvailability
import metamodel.domain.frozen.sequence.FrozenAnnotationRecordSequence
import metamodel.domain.frozen.sequence.FrozenConstructorParameterRecordSequence

/**
 * Frozen constructor record.
 *
 * Availability is value/state material, not identity-key material.
 */
class FrozenConstructorRecord private constructor(
    val key: FrozenConstructorRecordKey,
    val parameterRecords: FrozenConstructorParameterRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val declarationAvailability: FrozenMetadataAvailability,
    val sourceAvailability: FrozenMetadataAvailability,
) {
    companion object {
        @JvmStatic
        fun issue(
            key: FrozenConstructorRecordKey,
            parameterRecords: FrozenConstructorParameterRecordSequence,
            annotationRecords: FrozenAnnotationRecordSequence,
            declarationAvailability: FrozenMetadataAvailability,
            sourceAvailability: FrozenMetadataAvailability,
        ): FrozenConstructorRecord {
            return FrozenConstructorRecord(
                key = key,
                parameterRecords = parameterRecords,
                annotationRecords = annotationRecords,
                declarationAvailability = declarationAvailability,
                sourceAvailability = sourceAvailability,
            )
        }
    }
}