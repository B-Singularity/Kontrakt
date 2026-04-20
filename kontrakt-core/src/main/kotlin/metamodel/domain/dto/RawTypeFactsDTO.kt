package metamodel.domain.dto

import metamodel.domain.exception.InvalidTypeFactShapeException
import metamodel.domain.exception.MetamodelFactOwnershipMismatchException
import metamodel.domain.structure.MetamodelFactSequence

/**
 * Raw normalized type-facts DTO.
 *
 * This is the raw-fact boundary DTO used by RawTypeFactsProvider.
 *
 * It intentionally separates:
 * - raw constructor candidates
 * - raw property facts
 * - lowered type identity
 * - identity algorithm surface
 * - normalization version
 *
 * It must not carry:
 * - selected constructor
 * - projected active member set
 * - ordered traversal view
 * - capability demotion result
 * - interner/cache key material
 *
 * All raw fact collections are MetamodelFactSequence values.
 * They are deterministically ordered and never silently deduplicated.
 */
class RawTypeFactsDTO private constructor(
    val typeIdentity64: Long,
    val typeIdentityAlgorithmId: String,
    val typeIdentityAlgorithmVersion: Long,
    val ownerTypeFqcn: String,
    val normalizationVersion: Long,
    val constructors: MetamodelFactSequence<ConstructorCandidateFact>,
    val properties: MetamodelFactSequence<PropertyFact>,
) {
    companion object {
        @JvmStatic
        fun issue(
            typeIdentity64: Long,
            typeIdentityAlgorithmId: String,
            typeIdentityAlgorithmVersion: Long,
            ownerTypeFqcn: String,
            normalizationVersion: Long,
            constructors: Collection<ConstructorCandidateFact>,
            properties: Collection<PropertyFact>,
        ): RawTypeFactsDTO {
            validateCanonicalComponent("RawTypeFactsDTO.ownerTypeFqcn", ownerTypeFqcn)
            validateCanonicalComponent("RawTypeFactsDTO.typeIdentityAlgorithmId", typeIdentityAlgorithmId)

            if (typeIdentityAlgorithmVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "RawTypeFactsDTO",
                    reason = "typeIdentityAlgorithmVersion must be >= 0: $typeIdentityAlgorithmVersion",
                )
            }

            if (normalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "RawTypeFactsDTO",
                    reason = "normalizationVersion must be >= 0: $normalizationVersion",
                )
            }

            /*
             * Do not reject all negative typeIdentity64 values.
             *
             * The identity is a 64-bit lowered bit pattern stored in signed Long.
             * Negative values can be valid. Reserved sentinels are rejected later at
             * the planning expansion boundary where sentinel policy is owned.
             */
            validateConstructorOwnership(ownerTypeFqcn, constructors)
            validatePropertyOwnership(ownerTypeFqcn, properties)

            val orderedConstructors = MetamodelFactSequence.orderedUniqueBy(
                owner = ownerTypeFqcn,
                factKind = "ConstructorCandidateFact",
                duplicateKeyName = "constructorSignature@version",
                elements = constructors,
                orderingComparator = CONSTRUCTOR_COMPARATOR,
                keyOf = { fact ->
                    ConstructorDuplicateKey.issue(
                        signature = fact.constructorSignature,
                        version = fact.constructorSignatureNormalizationVersion,
                    )
                },
                keyComparator = CONSTRUCTOR_DUPLICATE_KEY_COMPARATOR,
                keyToString = { key -> key.render() },
            )

            /*
             * Exact duplicate properties are detected by RawPropertyDuplicateKey
             * before PROPERTY_COMPARATOR tie validation.
             */
            val orderedProperties = MetamodelFactSequence.orderedUniqueBy(
                owner = ownerTypeFqcn,
                factKind = "PropertyFact",
                duplicateKeyName = "rawPropertyIdentity",
                elements = properties,
                orderingComparator = PROPERTY_COMPARATOR,
                keyOf = { fact -> RawPropertyDuplicateKey.issue(fact) },
                keyComparator = RAW_PROPERTY_DUPLICATE_KEY_COMPARATOR,
                keyToString = { key -> key.render() },
            )

            return RawTypeFactsDTO(
                typeIdentity64 = typeIdentity64,
                typeIdentityAlgorithmId = typeIdentityAlgorithmId,
                typeIdentityAlgorithmVersion = typeIdentityAlgorithmVersion,
                ownerTypeFqcn = ownerTypeFqcn,
                normalizationVersion = normalizationVersion,
                constructors = orderedConstructors,
                properties = orderedProperties,
            )
        }

        /*
         * Constructor duplicate identity intentionally remains constructorSignature@version.
         *
         * The constructor signature is expected to be the full normalized constructor
         * signature. If two constructor candidates share this identity, the adapter
         * emitted duplicate raw facts even if other metadata differs.
         */
        private val CONSTRUCTOR_COMPARATOR: Comparator<ConstructorCandidateFact> =
            Comparator { left, right ->
                compareStrings(left.constructorSignature, right.constructorSignature)
                    .takeIfNonZero()
                    ?: compareLongs(
                        left.constructorSignatureNormalizationVersion,
                        right.constructorSignatureNormalizationVersion,
                    ).takeIfNonZero()
                    ?: compareInts(
                        left.declarationOrdinal.lowerForPrimitiveOrdering(),
                        right.declarationOrdinal.lowerForPrimitiveOrdering(),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.visibilityRank(left.visibility),
                        MetamodelFactRanks.visibilityRank(right.visibility),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.originRank(left.origin),
                        MetamodelFactRanks.originRank(right.origin),
                    ).takeIfNonZero()
                    ?: compareInts(left.parameters.size, right.parameters.size)
            }

        private val PROPERTY_COMPARATOR: Comparator<PropertyFact> =
            Comparator { left, right ->
                compareStrings(left.name, right.name)
                    .takeIfNonZero()
                    ?: compareStrings(left.typeReference.signature, right.typeReference.signature)
                        .takeIfNonZero()
                    ?: compareStrings(left.typeReference.id, right.typeReference.id)
                        .takeIfNonZero()
                    ?: compareLongs(
                        left.typeSignatureNormalizationVersion,
                        right.typeSignatureNormalizationVersion,
                    ).takeIfNonZero()
                    ?: compareInts(
                        left.declarationOrdinal.lowerForPrimitiveOrdering(),
                        right.declarationOrdinal.lowerForPrimitiveOrdering(),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.nullabilityRank(left.nullability),
                        MetamodelFactRanks.nullabilityRank(right.nullability),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.visibilityRank(left.declaredVisibility),
                        MetamodelFactRanks.visibilityRank(right.declaredVisibility),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.nullableVisibilityRank(left.setterVisibility),
                        MetamodelFactRanks.nullableVisibilityRank(right.setterVisibility),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.originRank(left.origin),
                        MetamodelFactRanks.originRank(right.origin),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.mutabilityRank(left.mutability),
                        MetamodelFactRanks.mutabilityRank(right.mutability),
                    ).takeIfNonZero()
                    ?: compareInts(
                        MetamodelFactRanks.storageKindRank(left.storageKind),
                        MetamodelFactRanks.storageKindRank(right.storageKind),
                    )
            }

        private val CONSTRUCTOR_DUPLICATE_KEY_COMPARATOR: Comparator<ConstructorDuplicateKey> =
            Comparator { left, right ->
                compareStrings(left.signature, right.signature)
                    .takeIfNonZero()
                    ?: compareLongs(left.version, right.version)
            }

        private val RAW_PROPERTY_DUPLICATE_KEY_COMPARATOR: Comparator<RawPropertyDuplicateKey> =
            Comparator { left, right ->
                compareStrings(left.name, right.name)
                    .takeIfNonZero()
                    ?: compareStrings(left.typeSignature, right.typeSignature)
                        .takeIfNonZero()
                    ?: compareStrings(left.typeId, right.typeId)
                        .takeIfNonZero()
                    ?: compareLongs(left.typeSignatureNormalizationVersion, right.typeSignatureNormalizationVersion)
                        .takeIfNonZero()
                    ?: compareInts(left.declarationOrdinalLowered, right.declarationOrdinalLowered)
                        .takeIfNonZero()
                    ?: compareInts(left.nullabilityRank, right.nullabilityRank)
                        .takeIfNonZero()
                    ?: compareInts(left.declaredVisibilityRank, right.declaredVisibilityRank)
                        .takeIfNonZero()
                    ?: compareInts(left.setterVisibilityRank, right.setterVisibilityRank)
                        .takeIfNonZero()
                    ?: compareInts(left.originRank, right.originRank)
                        .takeIfNonZero()
                    ?: compareInts(left.mutabilityRank, right.mutabilityRank)
                        .takeIfNonZero()
                    ?: compareInts(left.storageKindRank, right.storageKindRank)
            }

        private fun validateConstructorOwnership(
            ownerTypeFqcn: String,
            constructors: Collection<ConstructorCandidateFact>,
        ) {
            val iterator = constructors.iterator()
            while (iterator.hasNext()) {
                val constructor = iterator.next()

                if (constructor.ownerTypeFqcn != ownerTypeFqcn) {
                    throw MetamodelFactOwnershipMismatchException(
                        expectedOwner = ownerTypeFqcn,
                        actualOwner = constructor.ownerTypeFqcn,
                        factKind = "ConstructorCandidateFact",
                    )
                }
            }
        }

        private fun validatePropertyOwnership(
            ownerTypeFqcn: String,
            properties: Collection<PropertyFact>,
        ) {
            val iterator = properties.iterator()
            while (iterator.hasNext()) {
                val property = iterator.next()

                if (property.ownerTypeFqcn != ownerTypeFqcn) {
                    throw MetamodelFactOwnershipMismatchException(
                        expectedOwner = ownerTypeFqcn,
                        actualOwner = property.ownerTypeFqcn,
                        factKind = "PropertyFact",
                    )
                }
            }
        }

        private fun compareStrings(left: String, right: String): Int {
            return left.compareTo(right)
        }

        private fun compareLongs(left: Long, right: Long): Int {
            return java.lang.Long.compare(left, right)
        }

        private fun compareInts(left: Int, right: Int): Int {
            return java.lang.Integer.compare(left, right)
        }

        private fun Int.takeIfNonZero(): Int? {
            return if (this != 0) this else null
        }
    }
}

private class ConstructorDuplicateKey private constructor(
    val signature: String,
    val version: Long,
) {
    fun render(): String {
        return "signature=${renderField(signature)};version=$version"
    }

    companion object {
        @JvmStatic
        fun issue(
            signature: String,
            version: Long,
        ): ConstructorDuplicateKey {
            return ConstructorDuplicateKey(
                signature = signature,
                version = version,
            )
        }
    }
}

private class RawPropertyDuplicateKey private constructor(
    val name: String,
    val typeSignature: String,
    val typeId: String,
    val typeSignatureNormalizationVersion: Long,
    val declarationOrdinalLowered: Int,
    val nullabilityRank: Int,
    val declaredVisibilityRank: Int,
    val setterVisibilityRank: Int,
    val originRank: Int,
    val mutabilityRank: Int,
    val storageKindRank: Int,
) {
    fun render(): String {
        return "name=${renderField(name)};" +
                "typeSignature=${renderField(typeSignature)};" +
                "typeId=${renderField(typeId)};" +
                "typeSignatureNormalizationVersion=$typeSignatureNormalizationVersion;" +
                "declarationOrdinalLowered=$declarationOrdinalLowered;" +
                "nullabilityRank=$nullabilityRank;" +
                "declaredVisibilityRank=$declaredVisibilityRank;" +
                "setterVisibilityRank=$setterVisibilityRank;" +
                "originRank=$originRank;" +
                "mutabilityRank=$mutabilityRank;" +
                "storageKindRank=$storageKindRank"
    }

    companion object {
        @JvmStatic
        fun issue(
            fact: PropertyFact,
        ): RawPropertyDuplicateKey {
            return RawPropertyDuplicateKey(
                name = fact.name,
                typeSignature = fact.typeReference.signature,
                typeId = fact.typeReference.id,
                typeSignatureNormalizationVersion = fact.typeSignatureNormalizationVersion,
                declarationOrdinalLowered = fact.declarationOrdinal.lowerForPrimitiveOrdering(),
                nullabilityRank = MetamodelFactRanks.nullabilityRank(fact.nullability),
                declaredVisibilityRank = MetamodelFactRanks.visibilityRank(fact.declaredVisibility),
                setterVisibilityRank = MetamodelFactRanks.nullableVisibilityRank(fact.setterVisibility),
                originRank = MetamodelFactRanks.originRank(fact.origin),
                mutabilityRank = MetamodelFactRanks.mutabilityRank(fact.mutability),
                storageKindRank = MetamodelFactRanks.storageKindRank(fact.storageKind),
            )
        }
    }
}

private fun renderField(
    value: String,
): String {
    return value.length.toString() + ":" + value
}