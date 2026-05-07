package metamodel.domain.frozen.record

import metamodel.domain.vo.TypeReference

/**
 * Backend-neutral constructor identity key.
 *
 * Availability does not participate in this key.
 *
 * TODO:
 * Replace constructorSignature/parameterShapeSignature String placeholders with
 * dedicated CanonicalConstructorSignature / CanonicalParameterShapeSignature VOs
 * when those VOs are ratified.
 */
class FrozenConstructorRecordKey private constructor(
    val ownerType: TypeReference,
    val constructorSignature: String,
    val parameterShapeSignature: String,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerType: TypeReference,
            constructorSignature: String,
            parameterShapeSignature: String,
        ): FrozenConstructorRecordKey {
            return FrozenConstructorRecordKey(
                ownerType = ownerType,
                constructorSignature = constructorSignature,
                parameterShapeSignature = parameterShapeSignature,
            )
        }
    }
}