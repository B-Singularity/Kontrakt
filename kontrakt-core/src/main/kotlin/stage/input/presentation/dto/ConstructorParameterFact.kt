package stage.input.presentation.dto

import stage.admission.diagnostics.evidence.InvalidTypeFactShapeException
import stage.canonicalization.contract.representative.MetamodelProtocolTextGuards
import stage.canonicalization.material.representation.TypeReference
import stage.input.presentation.raw.DefaultValuePresence
import stage.input.presentation.raw.NullabilityKind

/**
 * Raw normalized constructor-parameter fact.
 *
 * This is not:
 *
 * - a projected active member;
 * - a selected constructor parameter;
 * - a reflection KParameter wrapper;
 * - a source parameter node;
 * - or a planning-domain decision object.
 *
 * This is raw fact material emitted by the metamodel adapter boundary after
 * adapter-local normalization and before core-owned constructor selection.
 *
 * Ownership law:
 *
 * ownerTypeFqcn identifies the type that owns the constructor candidate that
 * owns this parameter.
 *
 * ConstructorCandidateFact validates that all parameters attached to a candidate
 * share the same owner. This DTO still validates its own owner surface.
 *
 * Name law:
 *
 * name is the constructor-local parameter name.
 *
 * The reflection adapter rejects unnamed parameters before issuing this DTO
 * because parameter names participate in canonical active-member identity.
 *
 * This DTO requires the name to be:
 *
 * - non-empty;
 * - length-bounded;
 * - free of pipe delimiter '|';
 * - free of C0/C1 control characters;
 * - free of ASCII whitespace.
 *
 * It does not require the name to be an ASCII identifier. Some backends may
 * expose legal-but-unusual source names. If source-safe identifier restrictions
 * are needed later, add a dedicated parameter-name law rather than reusing
 * annotation-argument-name rules.
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
 * Index law:
 *
 * parameterIndex is constructor-local and compact-indexed later by
 * ConstructorCandidateFact through MetamodelFactSequence.compactIndexedBy(...).
 *
 * This DTO validates the local physical range before the candidate-level compact
 * sequence check.
 *
 * Default-value law:
 *
 * defaultValuePresence is explicit and must not be inferred by core logic from
 * missing data.
 *
 * Diagnostic law:
 *
 * toString() returns a compact summary and must not dump full TypeReference
 * internals.
 */
class ConstructorParameterFact private constructor(
    val ownerTypeFqcn: String,
    val name: String,
    val typeReference: TypeReference,
    val parameterIndex: Int,
    val nullability: NullabilityKind,
    val defaultValuePresence: DefaultValuePresence,
    val typeSignatureNormalizationVersion: Long,
) {
    fun renderSummary(): String {
        return "ConstructorParameterFact(" +
                "owner=$ownerTypeFqcn, " +
                "name=$name, " +
                "parameterIndex=$parameterIndex, " +
                "nullability=$nullability, " +
                "defaultValuePresence=$defaultValuePresence, " +
                "normalizationVersion=$typeSignatureNormalizationVersion, " +
                "typeShape=${typeReference.shapeSummary.kind.protocolToken}" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        /**
         * Keep aligned with ConstructorCandidateFact.MAX_OWNER_TYPE_FQCN_CHARS.
         *
         * Duplicated as a local DTO constant for now because the two DTOs should
         * not need to know each other's companion constants to validate their own
         * boundary.
         */
        const val MAX_OWNER_TYPE_FQCN_CHARS: Int = 512

        /**
         * Constructor parameter names should be small.
         *
         * 128 is intentionally generous for Java/Kotlin parameter metadata while
         * preventing allocation-based DoS from malformed adapter output.
         */
        const val MAX_PARAMETER_NAME_CHARS: Int = 128

        /**
         * Must stay consistent with ConstructorCandidateFact's parameter-count
         * cap.
         *
         * If a constructor candidate can carry at most 256 parameters, a single
         * parameter fact should not advertise an index beyond 255.
         */
        const val MAX_PARAMETER_INDEX: Int = 255

        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            name: String,
            typeReference: TypeReference,
            parameterIndex: Int,
            nullability: NullabilityKind,
            defaultValuePresence: DefaultValuePresence,
            typeSignatureNormalizationVersion: Long,
        ): ConstructorParameterFact {
            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "ConstructorParameterFact.ownerTypeFqcn",
                value = ownerTypeFqcn,
                maxChars = MAX_OWNER_TYPE_FQCN_CHARS,
            )

            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "ConstructorParameterFact.name",
                value = name,
                maxChars = MAX_PARAMETER_NAME_CHARS,
            )

            requireParameterIndexWithinLimit(
                ownerTypeFqcn = ownerTypeFqcn,
                parameterIndex = parameterIndex,
            )

            if (typeSignatureNormalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorParameterFact",
                    reason = "typeSignatureNormalizationVersion must be >= 0: " +
                            typeSignatureNormalizationVersion,
                )
            }

            return ConstructorParameterFact(
                ownerTypeFqcn = ownerTypeFqcn,
                name = name,
                typeReference = typeReference,
                parameterIndex = parameterIndex,
                nullability = nullability,
                defaultValuePresence = defaultValuePresence,
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
                    factKind = "ConstructorParameterFact",
                    reason = "$field must not be empty.",
                )
            }

            if (value.length > maxChars) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = "ConstructorParameterFact",
                    reason = "$field exceeds order cap=$maxChars.",
                )
            }

            var index = 0
            while (index < value.length) {
                val c = value[index]

                if (MetamodelProtocolTextGuards.isReservedProtocolOrControl(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "ConstructorParameterFact",
                        reason = "$field contains reserved order/control material at index=$index.",
                    )
                }

                if (MetamodelProtocolTextGuards.isAsciiWhitespace(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "ConstructorParameterFact",
                        reason = "$field must not contain ASCII whitespace: index=$index.",
                    )
                }

                index += 1
            }
        }

        private fun requireParameterIndexWithinLimit(
            ownerTypeFqcn: String,
            parameterIndex: Int,
        ) {
            if (parameterIndex < 0) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorParameterFact",
                    reason = "parameterIndex must be >= 0: $parameterIndex",
                )
            }

            if (parameterIndex > MAX_PARAMETER_INDEX) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorParameterFact",
                    reason = "parameterIndex exceeds order cap=$MAX_PARAMETER_INDEX: $parameterIndex",
                )
            }
        }
    }
}