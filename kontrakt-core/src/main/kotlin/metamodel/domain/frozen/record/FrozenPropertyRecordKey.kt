package metamodel.domain.frozen.record

import metamodel.domain.vo.TypeReference

/**
 * Backend-neutral property identity key.
 *
 * Availability does not participate in this key.
 *
 * TODO:
 * Replace propertyName String with CanonicalPropertyName once the VO is
 * ratified.
 */
class FrozenPropertyRecordKey private constructor(
    val ownerType: TypeReference,
    val propertyName: String,
    val propertyType: TypeReference,
    val visibilityRank: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerType: TypeReference,
            propertyName: String,
            propertyType: TypeReference,
            visibilityRank: Int,
        ): FrozenPropertyRecordKey {
            return FrozenPropertyRecordKey(
                ownerType = ownerType,
                propertyName = propertyName,
                propertyType = propertyType,
                visibilityRank = visibilityRank,
            )
        }
    }
}