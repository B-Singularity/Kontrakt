package metamodel.domain.frozen.record

import metamodel.domain.dto.RawTypeFactsDTO
import metamodel.domain.frozen.availability.FrozenMetadataAvailability
import metamodel.domain.frozen.sequence.FrozenAnnotationRecordSequence
import metamodel.domain.frozen.sequence.FrozenConstructorRecordSequence
import metamodel.domain.frozen.sequence.FrozenPropertyRecordSequence
import metamodel.domain.vo.TypeReference

/**
 * Frozen adapter-neutral raw fact material.
 *
 * This record may materialize RawTypeFactsDTO without returning to backend
 * handles.
 *
 * It must not contain:
 * - KType;
 * - KClass;
 * - KSType;
 * - KSDeclaration;
 * - classloader-local ids;
 * - registry ordinals;
 * - resolver-local ids;
 * - lambdas/suppliers capturing backend handles.
 */
class FrozenRawFactRecord private constructor(
    val reference: TypeReference,
    val constructorRecords: FrozenConstructorRecordSequence,
    val propertyRecords: FrozenPropertyRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val sourceAvailability: FrozenMetadataAvailability,
    private val materializedFacts: RawTypeFactsDTO,
) {
    fun materializeFacts(): RawTypeFactsDTO {
        /*
         * This is intentionally trivial in the first foundation cut.
         *
         * Later cuts may materialize from frozen record sequences, but they must
         * still not return to backend handles.
         */
        return materializedFacts
    }

    companion object {
        @JvmStatic
        fun issue(
            reference: TypeReference,
            constructorRecords: FrozenConstructorRecordSequence,
            propertyRecords: FrozenPropertyRecordSequence,
            annotationRecords: FrozenAnnotationRecordSequence,
            sourceAvailability: FrozenMetadataAvailability,
            materializedFacts: RawTypeFactsDTO,
        ): FrozenRawFactRecord {
            return FrozenRawFactRecord(
                reference = reference,
                constructorRecords = constructorRecords,
                propertyRecords = propertyRecords,
                annotationRecords = annotationRecords,
                sourceAvailability = sourceAvailability,
                materializedFacts = materializedFacts,
            )
        }
    }
}