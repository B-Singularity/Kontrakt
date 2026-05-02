package metamodel.domain.dto

import metamodel.domain.exception.InvalidTypeFactShapeException
import metamodel.domain.protocol.MetamodelProtocolTextGuards
import metamodel.domain.vo.DeclarationOrdinal
import metamodel.domain.vo.TypeReference

/**
 * Raw normalized property fact.
 *
 * This is not:
 *
 * - a projected active member;
 * - a selected property;
 * - a demotion result;
 * - a final traversal-order record;
 * - a reflection KProperty wrapper;
 * - a source property node;
 * - or a planning-domain decision object.
 *
 * This DTO represents a property before core-owned eligibility, demotion, and
 * active-member ordering evaluation.
 *
 * Boundary law:
 *
 * PropertyFact is already inside the metamodel fact boundary. Adapter-local
 * reflection/KSP/bytecode details must have been lowered before this object is
 * issued.
 *
 * Surface law:
 *
 * ownerTypeFqcn and name are normalized protocol text surfaces.
 *
 * They must:
 *
 * - be non-empty;
 * - be length-bounded;
 * - contain no reserved protocol delimiter '|';
 * - contain no C0/C1 control characters;
 * - contain no ASCII whitespace.
 *
 * This DTO does not normalize. The adapter boundary must perform NFC rejection
 * before issuing raw facts.
 *
 * TypeReference law:
 *
 * typeReference is a final domain-issued VO.
 *
 * This DTO must not revalidate typeReference.id or typeReference.signature.
 * Their integrity is already enforced by:
 *
 * - CanonicalTypeId;
 * - CanonicalTypeSignature;
 * - TypeIdentityCoherenceProof;
 * - TypeReference.issue(...).
 *
 * Revalidating those surfaces here would duplicate work and risk drifting from
 * the domain-issued TypeReference law.
 *
 * Mutability law:
 *
 * - READ_ONLY properties must not expose setterVisibility.
 * - MUTABLE properties must expose setterVisibility.
 *
 * Storage law:
 *
 * LATEINIT is only coherent for mutable properties.
 *
 * Diagnostic law:
 *
 * toString() returns a compact summary and must not dump full TypeReference
 * internals.
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
    val typeSignatureNormalizationVersion: Long,
) {
    fun renderSummary(): String {
        return "PropertyFact(" +
                "owner=$ownerTypeFqcn, " +
                "name=$name, " +
                "typeShape=${typeReference.shapeSummary.kind.protocolToken}, " +
                "nullability=$nullability, " +
                "declaredVisibility=$declaredVisibility, " +
                "setterVisibility=${setterVisibility ?: "<none>"}, " +
                "origin=$origin, " +
                "mutability=$mutability, " +
                "storageKind=$storageKind, " +
                "normalizationVersion=$typeSignatureNormalizationVersion" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        /**
         * Keep aligned with other raw fact owner-type caps.
         *
         * If a later ADR introduces a central raw-fact type-name cap, delegate to
         * that shared law instead of keeping this local constant.
         */
        const val MAX_OWNER_TYPE_FQCN_CHARS: Int = 512

        /**
         * Property names should be small.
         *
         * 256 is intentionally generous for Java/Kotlin/backend-emitted property
         * metadata while preventing allocation-based DoS from malformed adapter
         * output.
         */
        const val MAX_PROPERTY_NAME_CHARS: Int = 256

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
            typeSignatureNormalizationVersion: Long,
        ): PropertyFact {
            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "PropertyFact.ownerTypeFqcn",
                value = ownerTypeFqcn,
                maxChars = MAX_OWNER_TYPE_FQCN_CHARS,
            )

            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "PropertyFact.name",
                value = name,
                maxChars = MAX_PROPERTY_NAME_CHARS,
            )

            requireMutabilitySetterCoherence(
                ownerTypeFqcn = ownerTypeFqcn,
                name = name,
                mutability = mutability,
                setterVisibility = setterVisibility,
            )

            requireStorageMutabilityCoherence(
                ownerTypeFqcn = ownerTypeFqcn,
                name = name,
                mutability = mutability,
                storageKind = storageKind,
            )

            if (typeSignatureNormalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "PropertyFact",
                    reason = "typeSignatureNormalizationVersion must be >= 0: " +
                            typeSignatureNormalizationVersion,
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
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )
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
                    factKind = "PropertyFact",
                    reason = "$field must not be empty.",
                )
            }

            if (value.length > maxChars) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = "PropertyFact",
                    reason = "$field exceeds protocol cap=$maxChars.",
                )
            }

            var index = 0
            while (index < value.length) {
                val c = value[index]

                if (MetamodelProtocolTextGuards.isReservedProtocolOrControl(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "PropertyFact",
                        reason = "$field contains reserved protocol/control material at index=$index.",
                    )
                }

                if (MetamodelProtocolTextGuards.isAsciiWhitespace(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "PropertyFact",
                        reason = "$field must not contain ASCII whitespace: index=$index.",
                    )
                }

                index += 1
            }
        }

        private fun requireMutabilitySetterCoherence(
            ownerTypeFqcn: String,
            name: String,
            mutability: PropertyMutability,
            setterVisibility: VisibilityKind?,
        ) {
            when (mutability) {
                PropertyMutability.READ_ONLY -> {
                    if (setterVisibility != null) {
                        throw InvalidTypeFactShapeException(
                            owner = ownerTypeFqcn,
                            factKind = "PropertyFact",
                            reason = "READ_ONLY property must not have setterVisibility: name=$name",
                        )
                    }
                }

                PropertyMutability.MUTABLE -> {
                    if (setterVisibility == null) {
                        throw InvalidTypeFactShapeException(
                            owner = ownerTypeFqcn,
                            factKind = "PropertyFact",
                            reason = "MUTABLE property must have setterVisibility: name=$name",
                        )
                    }
                }

                PropertyMutability.UNKNOWN -> {
                    /*
                     * UNKNOWN must not be treated as mutable or read-only implicitly.
                     *
                     * Some adapters may know that a setter exists but not have reliable
                     * mutability semantics, while others may not expose setter data at
                     * all. Preserve the raw uncertainty here and let later eligibility /
                     * demotion policy decide how UNKNOWN should be handled.
                     */
                }
            }
        }

        private fun requireStorageMutabilityCoherence(
            ownerTypeFqcn: String,
            name: String,
            mutability: PropertyMutability,
            storageKind: PropertyStorageKind,
        ) {
            if (
                storageKind == PropertyStorageKind.LATEINIT &&
                mutability != PropertyMutability.MUTABLE
            ) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "PropertyFact",
                    reason = "LATEINIT property must be MUTABLE: name=$name, mutability=$mutability",
                )
            }
        }
    }
}