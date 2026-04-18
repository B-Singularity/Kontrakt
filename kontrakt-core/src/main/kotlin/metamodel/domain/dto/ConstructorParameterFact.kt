package metamodel.domain.dto

import metamodel.domain.exception.InvalidTypeFactShapeException
import metamodel.domain.vo.TypeReference

/**
 * Raw normalized constructor-parameter fact.
 *
 * This is not a projected active member.
 * It is raw fact material emitted by the metamodel adapter boundary.
 *
 * Important:
 * - parameterIndex is constructor-local and must be non-negative.
 * - typeReference must already be normalized and version-bound by the adapter.
 * - DTO boundary still validates TypeReference id/signature shape defensively.
 * - defaultValuePresence is explicit and must not be inferred by the Core from missing data.
 */
class ConstructorParameterFact private constructor(
    val ownerTypeFqcn: String,
    val name: String,
    val typeReference: TypeReference,
    val parameterIndex: Int,
    val nullability: NullabilityKind,
    val defaultValuePresence: DefaultValuePresence,
    val typeSignatureNormalizationVersion: Long
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            name: String,
            typeReference: TypeReference,
            parameterIndex: Int,
            nullability: NullabilityKind,
            defaultValuePresence: DefaultValuePresence,
            typeSignatureNormalizationVersion: Long
        ): ConstructorParameterFact {
            validateCanonicalComponent("ConstructorParameterFact.ownerTypeFqcn", ownerTypeFqcn)
            validateCanonicalComponent("ConstructorParameterFact.name", name)
            validateCanonicalComponent("ConstructorParameterFact.typeReference.id", typeReference.id)
            validateCanonicalComponent("ConstructorParameterFact.typeReference.signature", typeReference.signature)

            if (parameterIndex < 0) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorParameterFact",
                    reason = "parameterIndex must be >= 0: $parameterIndex"
                )
            }

            if (typeSignatureNormalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorParameterFact",
                    reason = "typeSignatureNormalizationVersion must be >= 0: $typeSignatureNormalizationVersion"
                )
            }

            return ConstructorParameterFact(
                ownerTypeFqcn = ownerTypeFqcn,
                name = name,
                typeReference = typeReference,
                parameterIndex = parameterIndex,
                nullability = nullability,
                defaultValuePresence = defaultValuePresence,
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion
            )
        }
    }
}