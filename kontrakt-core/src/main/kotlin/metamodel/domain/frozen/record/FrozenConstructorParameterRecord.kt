package metamodel.domain.frozen.record

import metamodel.domain.frozen.availability.FrozenMetadataAvailability
import metamodel.domain.vo.TypeReference

/**
 * Frozen constructor parameter record.
 *
 * The parameter index must be compact and validated by the sequence builder.
 */
class FrozenConstructorParameterRecord private constructor(
    val key: FrozenConstructorParameterRecordKey,
    val parameterType: TypeReference,
    val declarationAvailability: FrozenMetadataAvailability,
) {
    companion object {
        @JvmStatic
        fun issue(
            key: FrozenConstructorParameterRecordKey,
            parameterType: TypeReference,
            declarationAvailability: FrozenMetadataAvailability,
        ): FrozenConstructorParameterRecord {
            return FrozenConstructorParameterRecord(
                key = key,
                parameterType = parameterType,
                declarationAvailability = declarationAvailability,
            )
        }
    }
}