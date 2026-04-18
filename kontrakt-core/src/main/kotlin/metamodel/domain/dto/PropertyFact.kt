package metamodel.domain.dto

import metamodel.domain.exception.InvalidTypeFactShapeException
import metamodel.domain.vo.DeclarationOrdinal
import metamodel.domain.vo.TypeReference

/**
 * Raw normalized property fact.
 *
 * This DTO represents a property before Core-owned eligibility and demotion evaluation.
 *
 * It must not encode:
 * - whether the property is selected into the Active Member Set,
 * - whether it is demoted,
 * - final traversal order.
 */
class PropertyFact private constructor(
    val ownerTypeFqcn: String,
    val name: String,
    val typeReference: TypeReference,
    val declarationOrdinal: DeclarationOrdinal,
    val nullability: NullabilityKind,
    val declaredVisibility: VisibilityKind,
    val setterVisibility: VisibilityKind?,
    val origin: MemberOrigin,
    val mutability: PropertyMutability,
    val storageKind: PropertyStorageKind,
    val typeSignatureNormalizationVersion: Long
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            name: String,
            typeReference: TypeReference,
            declarationOrdinal: DeclarationOrdinal,
            nullability: NullabilityKind,
            declaredVisibility: VisibilityKind,
            setterVisibility: VisibilityKind?,
            origin: MemberOrigin,
            mutability: PropertyMutability,
            storageKind: PropertyStorageKind,
            typeSignatureNormalizationVersion: Long
        ): PropertyFact {
            validateCanonicalComponent("PropertyFact.ownerTypeFqcn", ownerTypeFqcn)
            validateCanonicalComponent("PropertyFact.name", name)
            validateCanonicalComponent("PropertyFact.typeReference.id", typeReference.id)
            validateCanonicalComponent("PropertyFact.typeReference.signature", typeReference.signature)

            if (typeSignatureNormalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "PropertyFact",
                    reason = "typeSignatureNormalizationVersion must be >= 0: $typeSignatureNormalizationVersion"
                )
            }

            return PropertyFact(
                ownerTypeFqcn = ownerTypeFqcn,
                name = name,
                typeReference = typeReference,
                declarationOrdinal = declarationOrdinal,
                nullability = nullability,
                declaredVisibility = declaredVisibility,
                setterVisibility = setterVisibility,
                origin = origin,
                mutability = mutability,
                storageKind = storageKind,
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion
            )
        }
    }
}