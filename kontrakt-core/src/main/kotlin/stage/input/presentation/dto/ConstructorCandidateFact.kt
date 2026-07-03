package stage.input.presentation.dto

import stage.admission.diagnostics.evidence.InvalidTypeFactShapeException
import stage.admission.diagnostics.evidence.MetamodelFactOwnershipMismatchException
import stage.canonicalization.contract.representative.MetamodelProtocolTextGuards
import stage.input.presentation.raw.DeclarationOrdinal
import stage.input.presentation.raw.MemberOrigin
import stage.input.presentation.raw.MetamodelFactSequence
import stage.input.presentation.raw.VisibilityKind

/**
 * Raw normalized constructor candidate fact.
 *
 * This DTO represents one constructor candidate before core-owned semantic
 * constructor selection.
 *
 * This is not:
 *
 * - a selected constructor result;
 * - a capability-demotion result;
 * - a projection result;
 * - a traversal-order records;
 * - a reflection KFunction wrapper;
 * - or a planning-domain decision object.
 *
 * Boundary law:
 *
 * ConstructorCandidateFact is already inside the metamodel fact boundary.
 * Adapter-local reflection details must have been lowered before this object is
 * issued.
 *
 * Surface law:
 *
 * ownerTypeFqcn and constructorSignature are order text surfaces, not source
 * text and not arbitrary diagnostic strings.
 *
 * They must:
 *
 * - be non-empty;
 * - be length-bounded;
 * - contain no reserved order delimiter '|';
 * - contain no C0/C1 control characters;
 * - contain no ASCII whitespace.
 *
 * constructorSignature is intentionally not validated as an ASCII identifier
 * because constructor signatures contain punctuation such as:
 *
 * - '('
 * - ')'
 * - ','
 * - '<'
 * - '>'
 * - '?'
 *
 * Normalization law:
 *
 * The adapter must perform NFC rejection before entering this DTO. This DTO does
 * not normalize. It only protects the already-normalized fact surface from
 * malformed order material.
 *
 * Parameter law:
 *
 * The parameter collection is a MetamodelFactSequence, not a generic List.
 * It is compact-index validated and deterministically ordered by parameterIndex.
 *
 * Ownership law:
 *
 * Every ConstructorParameterFact must belong to the same ownerTypeFqcn before
 * deterministic sequencing is attempted.
 *
 * This reports adapter fact-boundary corruption before ordering or compact-index
 * validation work is performed.
 *
 * Resource law:
 *
 * Constructor parameter count is capped before ownership validation and sequence
 * freezing. This prevents malformed adapter output from forcing large sorting or
 * compact-index validation work.
 *
 * Diagnostic law:
 *
 * toString() returns a compact summary. It must not dump all parameters.
 */
class ConstructorCandidateFact private constructor(
    val ownerTypeFqcn: String,
    val constructorSignature: String,
    val constructorSignatureNormalizationVersion: Long,
    val declarationOrdinal: DeclarationOrdinal,
    val visibility: VisibilityKind,
    val origin: MemberOrigin,
    val parameters: MetamodelFactSequence<ConstructorParameterFact>,
) {
    fun renderSummary(): String {
        return "ConstructorCandidateFact(" +
                "owner=$ownerTypeFqcn, " +
                "signature=$constructorSignature, " +
                "normalizationVersion=$constructorSignatureNormalizationVersion, " +
                "visibility=$visibility, " +
                "origin=$origin, " +
                "parameters=${parameters.size}" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        /**
         * Owner type names are usually much shorter than this.
         *
         * Keep this aligned with reflection/component text caps unless a later ADR
         * introduces a central metamodel type-name cap.
         */
        const val MAX_OWNER_TYPE_FQCN_CHARS: Int = 512

        /**
         * Constructor signatures can include nested generic type material.
         *
         * This cap is intentionally larger than ownerTypeFqcn but still bounded
         * to prevent allocation-based DoS in hashing, comparison, and diagnostics.
         */
        const val MAX_CONSTRUCTOR_SIGNATURE_CHARS: Int = 2_048

        /**
         * Real constructors rarely have many parameters.
         *
         * 256 is intentionally generous while preventing malformed adapter output
         * from forcing unbounded compact-index validation work.
         */
        const val MAX_CONSTRUCTOR_PARAMETER_COUNT: Int = 256

        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            constructorSignature: String,
            constructorSignatureNormalizationVersion: Long,
            declarationOrdinal: DeclarationOrdinal,
            visibility: VisibilityKind,
            origin: MemberOrigin,
            parameters: Collection<ConstructorParameterFact>,
        ): ConstructorCandidateFact {
            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "ConstructorCandidateFact.ownerTypeFqcn",
                value = ownerTypeFqcn,
                maxChars = MAX_OWNER_TYPE_FQCN_CHARS,
            )

            requireProtocolTextSurface(
                owner = ownerTypeFqcn,
                field = "ConstructorCandidateFact.constructorSignature",
                value = constructorSignature,
                maxChars = MAX_CONSTRUCTOR_SIGNATURE_CHARS,
            )

            requireConstructorSignatureBelongsToOwner(
                ownerTypeFqcn = ownerTypeFqcn,
                constructorSignature = constructorSignature,
            )

            if (constructorSignatureNormalizationVersion < 0L) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorCandidateFact",
                    reason =
                        "constructorSignatureNormalizationVersion must be >= 0: " +
                                constructorSignatureNormalizationVersion,
                )
            }

            requireParameterCountWithinLimit(
                ownerTypeFqcn = ownerTypeFqcn,
                parameters = parameters,
            )

            /*
             * Validate ownership before deterministic sequencing.
             *
             * If an adapter emits a parameter owned by another type, the error is
             * fact-boundary corruption and should be reported before any ordering
             * or compact-index validation work is performed.
             */
            validateParameterOwnership(
                ownerTypeFqcn = ownerTypeFqcn,
                parameters = parameters,
            )

            val orderedParameters =
                MetamodelFactSequence.compactIndexedBy(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorParameterFact",
                    indexName = "parameterIndex",
                    elements = parameters,
                    indexOf = { fact -> fact.parameterIndex },
                )

            return ConstructorCandidateFact(
                ownerTypeFqcn = ownerTypeFqcn,
                constructorSignature = constructorSignature,
                constructorSignatureNormalizationVersion = constructorSignatureNormalizationVersion,
                declarationOrdinal = declarationOrdinal,
                visibility = visibility,
                origin = origin,
                parameters = orderedParameters,
            )
        }

        private fun requireProtocolTextSurface(
            owner: String,
            field: String,
            value: String,
            maxChars: Int,
        ) {
            /*
             * Use the shared metamodel order guard for length and reserved
             * order/control material, but throw fact-shape exceptions for the
             * DTO boundary.
             *
             * If MetamodelProtocolTextGuards itself throws, that still indicates
             * metamodel order corruption. The local checks below add
             * fact-specific diagnostics and ASCII-whitespace rejection.
             */
            try {
                MetamodelProtocolTextGuards.requireLength(
                    field = field,
                    value = value,
                    maxChars = maxChars,
                    allowEmpty = false,
                )
            } catch (t: RuntimeException) {
                throw InvalidTypeFactShapeException(
                    owner = owner,
                    factKind = "ConstructorCandidateFact",
                    reason = "$field violates order length law: ${t.message}",
                )
            }

            var index = 0
            while (index < value.length) {
                val c = value[index]

                try {
                    MetamodelProtocolTextGuards.requireProtocolChar(
                        field = "$field[$index]",
                        value = c,
                    )
                } catch (t: RuntimeException) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "ConstructorCandidateFact",
                        reason = "$field contains reserved order/control material at index=$index.",
                    )
                }

                if (MetamodelProtocolTextGuards.isAsciiWhitespace(c)) {
                    throw InvalidTypeFactShapeException(
                        owner = owner,
                        factKind = "ConstructorCandidateFact",
                        reason = "$field must not contain ASCII whitespace: index=$index",
                    )
                }

                index += 1
            }
        }

        /**
         * Cheap coherence check between owner and constructor signature.
         *
         * The reflection adapter renders constructor signatures as:
         *
         *     ownerTypeFqcn '(' parameter-signatures ')'
         *
         * This DTO does not parse full type signatures, but it can still reject
         * obviously incoherent constructor facts whose signature does not start
         * with the owner type followed by '('.
         */
        private fun requireConstructorSignatureBelongsToOwner(
            ownerTypeFqcn: String,
            constructorSignature: String,
        ) {
            if (!constructorSignature.startsWith(ownerTypeFqcn)) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorCandidateFact",
                    reason = "constructorSignature must start with ownerTypeFqcn.",
                )
            }

            val ownerLength = ownerTypeFqcn.length

            if (
                constructorSignature.length <= ownerLength ||
                constructorSignature[ownerLength] != '(' ||
                constructorSignature[constructorSignature.length - 1] != ')'
            ) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorCandidateFact",
                    reason = "constructorSignature must use ownerTypeFqcn(...) form.",
                )
            }
        }

        private fun requireParameterCountWithinLimit(
            ownerTypeFqcn: String,
            parameters: Collection<ConstructorParameterFact>,
        ) {
            if (parameters.size > MAX_CONSTRUCTOR_PARAMETER_COUNT) {
                throw InvalidTypeFactShapeException(
                    owner = ownerTypeFqcn,
                    factKind = "ConstructorCandidateFact",
                    reason = "Constructor parameter count exceeds order cap=" +
                            "$MAX_CONSTRUCTOR_PARAMETER_COUNT: ${parameters.size}",
                )
            }
        }

        private fun validateParameterOwnership(
            ownerTypeFqcn: String,
            parameters: Collection<ConstructorParameterFact>,
        ) {
            val iterator = parameters.iterator()

            while (iterator.hasNext()) {
                val parameter = iterator.next()

                if (parameter.ownerTypeFqcn != ownerTypeFqcn) {
                    throw MetamodelFactOwnershipMismatchException(
                        expectedOwner = ownerTypeFqcn,
                        actualOwner = parameter.ownerTypeFqcn,
                        factKind = "ConstructorParameterFact",
                    )
                }
            }
        }
    }
}