package stage.input.presentation.dto

import stage.admission.diagnostics.evidence.InvalidTypeFactShapeException
import stage.admission.diagnostics.evidence.MetamodelFactOwnershipMismatchException
import stage.canonicalization.contract.representative.MetamodelProtocolOrdering
import stage.canonicalization.contract.representative.MetamodelProtocolTextGuards
import stage.input.presentation.raw.MetamodelFactRanks
import stage.input.presentation.raw.MetamodelFactSequence

/**
 * Raw normalized type-facts DTO.
 *
 * This is the raw-fact boundary DTO used by RawTypeFactsProvider.
 *
 * This is not:
 *
 * - a selected constructor result;
 * - a projected active-member set;
 * - an ordered traversal view;
 * - a capability-demotion result;
 * - an interner key;
 * - a cache key;
 * - or a planning-domain decision object.
 *
 * Boundary law:
 *
 * RawTypeFactsDTO is the deterministic freezing boundary for raw constructor and
 * property facts emitted by a metamodel adapter.
 *
 * It separates:
 *
 * - raw constructor candidates;
 * - raw property facts;
 * - lowered type identity;
 * - identity algorithm surface;
 * - normalization version.
 *
 * Sequence law:
 *
 * All raw fact collections are MetamodelFactSequence values.
 *
 * They are deterministically ordered and duplicate-checked. They are not stored
 * as arbitrary List values.
 *
 * Ownership law:
 *
 * Every constructor and property fact must belong to ownerTypeFqcn before
 * deterministic sequencing is attempted.
 *
 * Resource law:
 *
 * Constructor and property collection sizes are capped before ownership checks,
 * sorting, key projection, and sequence freezing. This prevents malformed
 * adapters from forcing unbounded O(N log N) sort work or sequence allocation.
 *
 * Duplicate law:
 *
 * Constructor duplicate identity is constructorSignature@normalizationVersion.
 *
 * Property duplicate identity is property name within the owner type.
 *
 * Rationale:
 *
 * A property name is the semantic member identity at this raw-fact layer. If an
 * adapter emits two facts for the same property name but with different
 * visibility, origin, mutability, storage kind, nullability, or type, that is not
 * two valid properties. It is conflicting raw metadata and must be rejected
 * before active-member projection.
 *
 * This intentionally rejects more than exact duplicates.
 *
 * Ordering law:
 *
 * Ordering is deterministic and order-defined. It must not depend on
 * reflection enumeration order, enum ordinal, JVM identity hash, locale, or
 * String.compareTo as an implicit domain law.
 *
 * Hash/sentinel law:
 *
 * typeIdentity64 is a lowered 64-bit bit pattern stored in signed Long. Negative
 * values can be valid. Reserved sentinel handling belongs to the planning
 * expansion boundary that owns sentinel policy.
 *
 * Diagnostic law:
 *
 * toString() returns a compact summary and must not dump all facts.
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
    fun renderSummary(): String {
        return "RawTypeFactsDTO(" +
                "owner=$ownerTypeFqcn, " +
                "typeIdentity64=<bits>, " +
                "identityAlgorithm=$typeIdentityAlgorithmId@$typeIdentityAlgorithmVersion, " +
                "normalizationVersion=$normalizationVersion, " +
                "constructors=${constructors.size}, " +
                "properties=${properties.size}" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        const val MAX_OWNER_TYPE_FQCN_CHARS: Int = 512
        const val MAX_TYPE_IDENTITY_ALGORITHM_ID_CHARS: Int = 128

        /**
         * Most JVM/Kotlin classes have very few constructors.
         *
         * 128 is deliberately generous while preventing pathological adapter
         * output from causing unbounded sort/key-allocation work.
         */
        const val MAX_TOTAL_CONSTRUCTORS: Int = 128

        /**
         * 1,024 properties is far beyond normal application/library classes but
         * still bounded enough to protect DTO freezing from resource abuse.
         */
        const val MAX_TOTAL_PROPERTIES: Int = 1_024

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
            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "RawTypeFactsDTO.ownerTypeFqcn",
                value = ownerTypeFqcn,
                maxChars = MAX_OWNER_TYPE_FQCN_CHARS,
            )

            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "RawTypeFactsDTO.typeIdentityAlgorithmId",
                value = typeIdentityAlgorithmId,
                maxChars = MAX_TYPE_IDENTITY_ALGORITHM_ID_CHARS,
            )

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

            requireFactCountsWithinLimit(
                ownerTypeFqcn = ownerTypeFqcn,
                constructors = constructors,
                properties = properties,
            )

            /*
             * Do not reject all negative typeIdentity64 values.
             *
             * The identity is a 64-bit lowered bit pattern stored in signed Long.
             * Negative values can be valid. Reserved sentinels are rejected later
             * at the planning expansion boundary where sentinel policy is owned.
             */
            validateConstructorOwnership(
                ownerTypeFqcn = ownerTypeFqcn,
                constructors = constructors,
            )

            validatePropertyOwnership(
                ownerTypeFqcn = ownerTypeFqcn,
                properties = properties,
            )

            val orderedConstructors =
                MetamodelFactSequence.orderedUniqueBy(
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
             * Property duplicate identity is intentionally name-only.
             *
             * If the adapter emits the same property name twice with different
             * metadata, that is conflicting raw fact material, not two unique raw
             * properties.
             */
            val orderedProperties =
                MetamodelFactSequence.orderedUniqueBy(
                    owner = ownerTypeFqcn,
                    factKind = "PropertyFact",
                    duplicateKeyName = "propertyName",
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
                    ?: compareStrings(
                        left.typeReference.signature.value,
                        right.typeReference.signature.value,
                    ).takeIfNonZero()
                    ?: compareStrings(
                        left.typeReference.id.value,
                        right.typeReference.id.value,
                    ).takeIfNonZero()
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
            }

        private fun requireProtocolTextSurface(
            owner: String,
            field: String,
            value: String,
            maxChars: Int,
        ) {
            if (value.isEmpty()) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = "RawTypeFactsDTO",
                    reason = "$field must not be empty.",
                )
            }

            if (value.length > maxChars) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = "RawTypeFactsDTO",
                    reason = "$field exceeds order cap=$maxChars.",
                )
            }

            var index = 0
            while (index < value.length) {
                val c = value[index]

                if (MetamodelProtocolTextGuards.isReservedProtocolOrControl(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "RawTypeFactsDTO",
                        reason = "$field contains reserved order/control material at index=$index.",
                    )
                }

                if (MetamodelProtocolTextGuards.isAsciiWhitespace(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "RawTypeFactsDTO",
                        reason = "$field must not contain ASCII whitespace: index=$index.",
                    )
                }

                index += 1
            }
        }

        private fun requireFactCountsWithinLimit(
            ownerTypeFqcn: String,
            constructors: Collection<ConstructorCandidateFact>,
            properties: Collection<PropertyFact>,
        ) {
            if (constructors.size > MAX_TOTAL_CONSTRUCTORS) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "RawTypeFactsDTO",
                    reason = "Constructor count exceeds order cap=$MAX_TOTAL_CONSTRUCTORS: ${constructors.size}",
                )
            }

            if (properties.size > MAX_TOTAL_PROPERTIES) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "RawTypeFactsDTO",
                    reason = "Property count exceeds order cap=$MAX_TOTAL_PROPERTIES: ${properties.size}",
                )
            }
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

        private fun compareStrings(
            left: String,
            right: String,
        ): Int {
            return MetamodelProtocolOrdering.compareUtf16CodeUnits(
                left = left,
                right = right,
            )
        }

        private fun compareLongs(
            left: Long,
            right: Long,
        ): Int {
            return MetamodelProtocolOrdering.compareLong(
                left = left,
                right = right,
            )
        }

        private fun compareInts(
            left: Int,
            right: Int,
        ): Int {
            return MetamodelProtocolOrdering.compareInt(
                left = left,
                right = right,
            )
        }

        private fun Int.takeIfNonZero(): Int? {
            return if (this != 0) this else null
        }
    }
}

/**
 * Constructor duplicate identity.
 *
 * The constructor signature is expected to be the full normalized constructor
 * signature. If two constructor candidates share constructorSignature@version,
 * the adapter emitted duplicate raw facts even if other metadata differs.
 */
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

/**
 * Property duplicate identity.
 *
 * This is intentionally name-only within one owner type.
 *
 * A raw property fact with the same name but different metadata is conflicting
 * adapter output, not a second unique property.
 */
private class RawPropertyDuplicateKey private constructor(
    val name: String,
) {
    fun render(): String {
        return "name=${renderField(name)}"
    }

    companion object {
        @JvmStatic
        fun issue(
            fact: PropertyFact,
        ): RawPropertyDuplicateKey {
            return RawPropertyDuplicateKey(
                name = fact.name,
            )
        }
    }
}

private fun renderField(
    value: String,
): String {
    return value.length.toString() + ":" + value
}