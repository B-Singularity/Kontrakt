package stage.input.material

import stage.canonicalization.material.CanonicalTypeId
import stage.canonicalization.material.CanonicalTypeShapeKind
import stage.canonicalization.material.CanonicalTypeSignature
import stage.canonicalization.material.TypeShapeRatificationFingerprint
import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Scope-local proof that a TypeReference identity tuple was assembled by the
 * metamodel identity factory as one coherent unit.
 *
 * This is not:
 *
 * - a cryptographic signature;
 * - a security bearer token;
 * - a cache key;
 * - an interning key;
 * - a canonical byte encoding;
 * - a runtime Class handle;
 * - a reflection/KSP artifact;
 * - a transport DTO;
 * - or a persistence DTO.
 *
 * Why this exists:
 *
 * TypeReference has multiple identity axes:
 *
 * - CanonicalTypeId;
 * - TypeCycleKey;
 * - CanonicalTypeSignature;
 * - OrderedUseSiteAnnotations;
 * - type nesting depth.
 *
 * If those values are manually assembled from unrelated sources, the planner can
 * receive a TypeReference whose id, cycle identity, signature, annotation
 * ordering, and depth do not describe the same semantic type.
 *
 * Binding law:
 *
 * The proof is bound to a deterministic tuple-binding material derived from the
 * exact tuple it certifies. A proof issued for type X must not be reusable for
 * type Y.
 *
 * The binding does not use hashCode() as authority. hashCode() is for in-memory
 * hash buckets only and may collide. This proof stores exact bounded scalar
 * material extracted from the tuple.
 *
 * Issuance law:
 *
 * issueFromFactory(...) is internal. Adapters must not mint coherence proofs.
 * Reflection/KSP/bytecode/static-source adapters provide normalized materials;
 * the domain factory issues the proof after deriving all identity axes from one
 * ratified source.
 *
 * Token law:
 *
 * proofId is diagnostic/admission material only. It is not a bearer token and
 * must not be treated as proof authority by itself.
 *
 * Do not require random entropy or wall-clock-derived nonces here. This proof
 * must remain deterministic and replayable inside the same ratified metamodel
 * construction flow.
 *
 * Memory law:
 *
 * The proof does not retain the full identity axis objects. It stores a compact
 * exact binding material and verifies future tuples by recomputing the same
 * material.
 *
 * Serialization law:
 *
 * This object must not cross transport or persistence boundaries directly.
 * Expose only renderSummary() or a dedicated summary DTO outside the domain
 * process boundary.
 */
class TypeIdentityCoherenceProof private constructor(
    val proofId: String,
    val factoryId: String,
    val factoryVersion: String,
    private val coveredBinding: TypeIdentityCoherenceBinding,
    val schemaVersion: Int,
) {
    /**
     * Verifies that this proof covers the exact TypeReference tuple being
     * issued.
     *
     * This is the substitution-attack guard.
     *
     * It includes typeNestingDepth, so a caller cannot reuse a proof produced for
     * the same apparent id/signature/annotations but a different lowered type
     * depth.
     */
    fun requireCovers(
        id: CanonicalTypeId,
        cycleKey: TypeCycleKey,
        signature: CanonicalTypeSignature,
        useSiteAnnotations: OrderedUseSiteAnnotations,
        typeNestingDepth: Int,
    ) {
        TypeNestingDepthLaw.requireWithinLimit(
            field = "TypeIdentityCoherenceProof.typeNestingDepth",
            depth = typeNestingDepth,
        )

        coveredBinding.requireMatches(
            id = id,
            cycleKey = cycleKey,
            signature = signature,
            useSiteAnnotations = useSiteAnnotations,
            typeNestingDepth = typeNestingDepth,
        )
    }

    fun renderSummary(): String =
        "TypeIdentityCoherenceProof(" +
                "schemaVersion=$schemaVersion, " +
                "factory=$factoryId@$factoryVersion, " +
                "proofId=<redacted>, " +
                "binding=${coveredBinding.renderSummary()}" +
                ")"

    override fun toString(): String = renderSummary()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeIdentityCoherenceProof) return false

        return proofId == other.proofId &&
                factoryId == other.factoryId &&
                factoryVersion == other.factoryVersion &&
                coveredBinding == other.coveredBinding &&
                schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        var result = proofId.hashCode()
        result = 31 * result + factoryId.hashCode()
        result = 31 * result + factoryVersion.hashCode()
        result = 31 * result + coveredBinding.hashCode()
        result = 31 * result + schemaVersion
        return result
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 4

        private const val MAX_PROOF_ID_CHARS: Int = 128
        private const val MAX_FACTORY_TOKEN_CHARS: Int = 128

        /**
         * Issues a coherence proof from the domain factory boundary.
         *
         * This method is internal on purpose.
         *
         * The proof is bound to:
         *
         * - CanonicalTypeId material;
         * - TypeCycleKey material available to this layer;
         * - CanonicalTypeSignature material;
         * - OrderedUseSiteAnnotations material available to this layer;
         * - typeNestingDepth.
         */
        @JvmStatic
        internal fun issueFromFactory(
            proofId: String,
            factoryId: String,
            factoryVersion: String,
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            typeNestingDepth: Int,
        ): TypeIdentityCoherenceProof {
            requireProofId(proofId)

            val canonicalFactoryId =
                canonicalizeProtocolId(
                    field = "TypeIdentityCoherenceProof.factoryId",
                    value = factoryId,
                )

            requireProtocolToken(
                field = "TypeIdentityCoherenceProof.factoryVersion",
                value = factoryVersion,
                maxChars = MAX_FACTORY_TOKEN_CHARS,
            )

            TypeNestingDepthLaw.requireWithinLimit(
                field = "TypeIdentityCoherenceProof.typeNestingDepth",
                depth = typeNestingDepth,
            )

            requireAxisCoherence(
                id = id,
                cycleKey = cycleKey,
                signature = signature,
            )

            return TypeIdentityCoherenceProof(
                proofId = proofId,
                factoryId = canonicalFactoryId,
                factoryVersion = factoryVersion,
                coveredBinding =
                    TypeIdentityCoherenceBinding.fromTuple(
                        id = id,
                        cycleKey = cycleKey,
                        signature = signature,
                        useSiteAnnotations = useSiteAnnotations,
                        typeNestingDepth = typeNestingDepth,
                    ),
                schemaVersion = CURRENT_SCHEMA_VERSION,
            )
        }

        private fun requireAxisCoherence(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
        ) {
            if (id.shapeSummary != signature.shapeSummary) {
                throw MetamodelFactContractViolationException(
                    "TypeIdentityCoherenceProof cannot be issued for incoherent id/signature shape: " +
                            "idShape=${id.shapeSummary}, signatureShape=${signature.shapeSummary}",
                )
            }

            if (cycleKey.shapeSummary.kind != signature.shapeSummary.kind) {
                throw MetamodelFactContractViolationException(
                    "TypeIdentityCoherenceProof cannot be issued for incoherent cycle/signature kind: " +
                            "cycleShape=${cycleKey.shapeSummary}, signatureShape=${signature.shapeSummary}",
                )
            }
        }

        private fun requireProofId(value: String) {
            requireProtocolToken(
                field = "TypeIdentityCoherenceProof.proofId",
                value = value,
                maxChars = MAX_PROOF_ID_CHARS,
            )

            /*
             * Do not add entropy/randomness validation here.
             *
             * proofId is not the source of authority. Authority comes from:
             *
             * - internal factory issuance;
             * - axis coherence checks;
             * - exact tuple binding;
             * - TypeReference.issue(...) calling requireCovers(...).
             */
        }

        private fun canonicalizeProtocolId(
            field: String,
            value: String,
        ): String {
            requireProtocolToken(
                field = field,
                value = value,
                maxChars = MAX_FACTORY_TOKEN_CHARS,
            )

            var requiresLowercaseCopy = false

            for (index in value.indices) {
                val c = value[index]

                when (c) {
                    in 'A'..'Z' -> {
                        requiresLowercaseCopy = true
                    }

                    in 'a'..'z',
                    in '0'..'9',
                    '-',
                    '_',
                    '.',
                        -> {
                        // Already canonical-safe ASCII order material.
                    }

                    else -> {
                        throw MetamodelFactContractViolationException(
                            "$field contains a non-canonical order-id character.",
                        )
                    }
                }
            }

            if (!requiresLowercaseCopy) {
                return value
            }

            val builder = StringBuilder(value.length)

            for (index in value.indices) {
                val c = value[index]
                val lowered =
                    if (c in 'A'..'Z') {
                        (c.code + 32).toChar()
                    } else {
                        c
                    }

                builder.append(lowered)
            }

            return builder.toString()
        }

        private fun requireProtocolToken(
            field: String,
            value: String,
            maxChars: Int,
        ) {
            if (value.isEmpty()) {
                throw MetamodelFactContractViolationException(
                    "$field must not be empty.",
                )
            }

            if (value.length > maxChars) {
                throw MetamodelFactContractViolationException(
                    "$field exceeds maximum allowed token length.",
                )
            }

            if (
                value.indexOf('|') >= 0 ||
                value.indexOf('\u0000') >= 0 ||
                value.indexOf('\n') >= 0 ||
                value.indexOf('\r') >= 0 ||
                value.indexOf('\t') >= 0
            ) {
                throw MetamodelFactContractViolationException(
                    "$field contains a reserved order/control character.",
                )
            }
        }
    }
}

/**
 * Exact bounded binding material for the TypeReference identity tuple.
 *
 * This class intentionally avoids storing strong references to full axis objects,
 * but it also does not use hashCode() as proof authority.
 *
 * It stores scalar bounded material that is already part of the ratified
 * metamodel identity surface.
 */
private class TypeIdentityCoherenceBinding private constructor(
    private val typeText: String,
    private val shapeSummary: TypeShapeSummary,
    private val classifierId: String,
    private val classifierVersion: String,
    private val ratificationFingerprint: TypeShapeRatificationFingerprint,
    private val cycleShapeKind: CanonicalTypeShapeKind,
    private val signatureValue: String,
    private val signatureShapeSummary: TypeShapeSummary,
    private val signatureSchemaVersion: Int,
    private val annotationCount: Int,
    private val annotationMaxNestingDepth: Int,
    private val annotationTable: OrderedUseSiteAnnotations,
    private val typeNestingDepth: Int,
) {
    fun requireMatches(
        id: CanonicalTypeId,
        cycleKey: TypeCycleKey,
        signature: CanonicalTypeSignature,
        useSiteAnnotations: OrderedUseSiteAnnotations,
        typeNestingDepth: Int,
    ) {
        if (typeText != id.value) {
            mismatch(
                field = "typeText",
                expected = typeText,
                actual = id.value,
            )
        }

        if (shapeSummary != id.shapeSummary) {
            mismatch(
                field = "id.shapeSummary",
                expected = shapeSummary.toString(),
                actual = id.shapeSummary.toString(),
            )
        }

        if (classifierId != id.classifierId) {
            mismatch(
                field = "classifierId",
                expected = classifierId,
                actual = id.classifierId,
            )
        }

        if (classifierVersion != id.classifierVersion) {
            mismatch(
                field = "classifierVersion",
                expected = classifierVersion,
                actual = id.classifierVersion,
            )
        }

        if (ratificationFingerprint != id.ratificationFingerprint) {
            throw MetamodelFactContractViolationException(
                "TypeIdentityCoherenceProof does not cover supplied TypeReference tuple: " +
                        "field=ratificationFingerprint, " +
                        "expected=${ratificationFingerprint.redacted()}, " +
                        "actual=${id.ratificationFingerprint.redacted()}, " +
                        "binding=${renderSummary()}",
            )
        }

        if (cycleShapeKind != cycleKey.shapeSummary.kind) {
            mismatch(
                field = "cycleShapeKind",
                expected = cycleShapeKind.protocolToken,
                actual = cycleKey.shapeSummary.kind.protocolToken,
            )
        }

        if (signatureValue != signature.value) {
            mismatch(
                field = "signatureValue",
                expected = signatureValue,
                actual = signature.value,
            )
        }

        if (signatureShapeSummary != signature.shapeSummary) {
            mismatch(
                field = "signature.shapeSummary",
                expected = signatureShapeSummary.toString(),
                actual = signature.shapeSummary.toString(),
            )
        }

        if (signatureSchemaVersion != signature.schemaVersion) {
            mismatch(
                field = "signatureSchemaVersion",
                expected = signatureSchemaVersion.toString(),
                actual = signature.schemaVersion.toString(),
            )
        }

        if (annotationCount != useSiteAnnotations.size) {
            mismatch(
                field = "annotationCount",
                expected = annotationCount.toString(),
                actual = useSiteAnnotations.size.toString(),
            )
        }

        if (annotationMaxNestingDepth != useSiteAnnotations.maxAnnotationValueNestingDepth) {
            mismatch(
                field = "annotationMaxNestingDepth",
                expected = annotationMaxNestingDepth.toString(),
                actual = useSiteAnnotations.maxAnnotationValueNestingDepth.toString(),
            )
        }

        /*
         * This is intentionally exact structural equality.
         *
         * The table is already bounded by OrderedUseSiteAnnotations.
         * Do not replace this with hashCode().
         */
        if (annotationTable != useSiteAnnotations) {
            throw MetamodelFactContractViolationException(
                "TypeIdentityCoherenceProof does not cover supplied TypeReference tuple: " +
                        "field=annotationTable, " +
                        "expected=${annotationTable.renderSummary()}, " +
                        "actual=${useSiteAnnotations.renderSummary()}, " +
                        "binding=${renderSummary()}",
            )
        }

        if (this.typeNestingDepth != typeNestingDepth) {
            mismatch(
                field = "typeNestingDepth",
                expected = this.typeNestingDepth.toString(),
                actual = typeNestingDepth.toString(),
            )
        }
    }

    private fun mismatch(
        field: String,
        expected: String,
        actual: String,
    ): Nothing =
        throw MetamodelFactContractViolationException(
            "TypeIdentityCoherenceProof does not cover supplied TypeReference tuple: " +
                    "field=$field, expected=$expected, actual=$actual, binding=${renderSummary()}",
        )

    fun renderSummary(): String =
        "TypeIdentityCoherenceBinding(" +
                "type=$typeText, " +
                "shape=${shapeSummary.kind.protocolToken}, " +
                "signatureSchema=$signatureSchemaVersion, " +
                "annotations=$annotationCount, " +
                "annotationDepth=$annotationMaxNestingDepth, " +
                "typeDepth=$typeNestingDepth" +
                ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeIdentityCoherenceBinding) return false

        return typeText == other.typeText &&
                shapeSummary == other.shapeSummary &&
                classifierId == other.classifierId &&
                classifierVersion == other.classifierVersion &&
                ratificationFingerprint == other.ratificationFingerprint &&
                cycleShapeKind == other.cycleShapeKind &&
                signatureValue == other.signatureValue &&
                signatureShapeSummary == other.signatureShapeSummary &&
                signatureSchemaVersion == other.signatureSchemaVersion &&
                annotationCount == other.annotationCount &&
                annotationMaxNestingDepth == other.annotationMaxNestingDepth &&
                annotationTable == other.annotationTable &&
                typeNestingDepth == other.typeNestingDepth
    }

    override fun hashCode(): Int {
        var result = typeText.hashCode()
        result = 31 * result + shapeSummary.hashCode()
        result = 31 * result + classifierId.hashCode()
        result = 31 * result + classifierVersion.hashCode()
        result = 31 * result + ratificationFingerprint.hashCode()
        result = 31 * result + cycleShapeKind.hashCode()
        result = 31 * result + signatureValue.hashCode()
        result = 31 * result + signatureShapeSummary.hashCode()
        result = 31 * result + signatureSchemaVersion
        result = 31 * result + annotationCount
        result = 31 * result + annotationMaxNestingDepth
        result = 31 * result + annotationTable.hashCode()
        result = 31 * result + typeNestingDepth
        return result
    }

    companion object {
        fun fromTuple(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            typeNestingDepth: Int,
        ): TypeIdentityCoherenceBinding =
            TypeIdentityCoherenceBinding(
                typeText = id.value,
                shapeSummary = id.shapeSummary,
                classifierId = id.classifierId,
                classifierVersion = id.classifierVersion,
                ratificationFingerprint = id.ratificationFingerprint,
                cycleShapeKind = cycleKey.shapeSummary.kind,
                signatureValue = signature.value,
                signatureShapeSummary = signature.shapeSummary,
                signatureSchemaVersion = signature.schemaVersion,
                annotationCount = useSiteAnnotations.size,
                annotationMaxNestingDepth = useSiteAnnotations.maxAnnotationValueNestingDepth,
                annotationTable = useSiteAnnotations,
                typeNestingDepth = typeNestingDepth,
            )
    }
}
