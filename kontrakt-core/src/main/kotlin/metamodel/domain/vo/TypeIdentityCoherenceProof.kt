package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException

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
 * - OrderedUseSiteAnnotations.
 *
 * If those values are manually assembled from unrelated sources, the planner can
 * receive a TypeReference whose id, cycle identity, signature, and annotation
 * ordering do not describe the same semantic type.
 *
 * Binding law:
 *
 * The proof is bound to a lightweight tuple-binding summary derived from the
 * exact tuple it certifies. A proof issued for type X must not be reusable for
 * type Y.
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
 * binding summary and verifies future tuples by recomputing the same summary.
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
     */
    fun requireCovers(
        id: CanonicalTypeId,
        cycleKey: TypeCycleKey,
        signature: CanonicalTypeSignature,
        useSiteAnnotations: OrderedUseSiteAnnotations,
    ) {
        val actualBinding = TypeIdentityCoherenceBinding.fromTuple(
            id = id,
            cycleKey = cycleKey,
            signature = signature,
            useSiteAnnotations = useSiteAnnotations,
        )

        if (coveredBinding != actualBinding) {
            throw MetamodelFactContractViolationException(
                "TypeIdentityCoherenceProof does not cover supplied TypeReference tuple: " +
                        "proof=${renderSummary()}, " +
                        "expected=${coveredBinding.renderSummary()}, " +
                        "actual=${actualBinding.renderSummary()}",
            )
        }
    }

    fun renderSummary(): String {
        return "TypeIdentityCoherenceProof(" +
                "schemaVersion=$schemaVersion, " +
                "factory=$factoryId@$factoryVersion, " +
                "proofId=<redacted>, " +
                "binding=${coveredBinding.renderSummary()}" +
                ")"
    }

    override fun toString(): String {
        return renderSummary()
    }

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
        const val CURRENT_SCHEMA_VERSION: Int = 3

        private const val MAX_PROOF_ID_CHARS: Int = 128
        private const val MAX_FACTORY_TOKEN_CHARS: Int = 128

        /**
         * Issues a coherence proof from the domain factory boundary.
         *
         * This method is internal on purpose.
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
        ): TypeIdentityCoherenceProof {
            requireProofId(proofId)

            val canonicalFactoryId = canonicalizeProtocolId(
                field = "TypeIdentityCoherenceProof.factoryId",
                value = factoryId,
            )

            requireProtocolToken(
                field = "TypeIdentityCoherenceProof.factoryVersion",
                value = factoryVersion,
                maxChars = MAX_FACTORY_TOKEN_CHARS,
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
                coveredBinding = TypeIdentityCoherenceBinding.fromTuple(
                    id = id,
                    cycleKey = cycleKey,
                    signature = signature,
                    useSiteAnnotations = useSiteAnnotations,
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

        private fun requireProofId(
            value: String,
        ) {
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
             * - tuple binding summary;
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
                    '.' -> {
                        // Already canonical-safe ASCII protocol material.
                    }

                    else -> {
                        throw MetamodelFactContractViolationException(
                            "$field contains a non-canonical protocol-id character.",
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
                val lowered = if (c in 'A'..'Z') {
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
                    "$field contains a reserved protocol/control character.",
                )
            }
        }
    }
}

/**
 * Lightweight binding summary for the TypeReference identity tuple.
 *
 * This class intentionally stores compact structural fingerprints instead of
 * strong references to the full axis objects.
 *
 * It is not cryptographic. It is a deterministic in-domain substitution guard.
 * A stronger digest-backed binding can be introduced later by the canonical
 * encoding / fingerprint-deriver phase without changing TypeReference semantics.
 */
private class TypeIdentityCoherenceBinding private constructor(
    private val idHash: Int,
    private val cycleKeyHash: Int,
    private val signatureHash: Int,
    private val useSiteAnnotationsHash: Int,
    private val typeTextHash: Int,
    private val shapeSummaryHash: Int,
    private val annotationCount: Int,
) {
    fun renderSummary(): String {
        return "TypeIdentityCoherenceBinding(" +
                "typeHash=$typeTextHash, " +
                "shapeHash=$shapeSummaryHash, " +
                "annotationCount=$annotationCount" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeIdentityCoherenceBinding) return false

        return idHash == other.idHash &&
                cycleKeyHash == other.cycleKeyHash &&
                signatureHash == other.signatureHash &&
                useSiteAnnotationsHash == other.useSiteAnnotationsHash &&
                typeTextHash == other.typeTextHash &&
                shapeSummaryHash == other.shapeSummaryHash &&
                annotationCount == other.annotationCount
    }

    override fun hashCode(): Int {
        var result = idHash
        result = 31 * result + cycleKeyHash
        result = 31 * result + signatureHash
        result = 31 * result + useSiteAnnotationsHash
        result = 31 * result + typeTextHash
        result = 31 * result + shapeSummaryHash
        result = 31 * result + annotationCount
        return result
    }

    companion object {
        fun fromTuple(
            id: CanonicalTypeId,
            cycleKey: TypeCycleKey,
            signature: CanonicalTypeSignature,
            useSiteAnnotations: OrderedUseSiteAnnotations,
        ): TypeIdentityCoherenceBinding {
            return TypeIdentityCoherenceBinding(
                idHash = id.hashCode(),
                cycleKeyHash = cycleKey.hashCode(),
                signatureHash = signature.hashCode(),
                useSiteAnnotationsHash = useSiteAnnotations.hashCode(),
                typeTextHash = id.value.hashCode(),
                shapeSummaryHash = id.shapeSummary.hashCode(),
                annotationCount = useSiteAnnotations.size,
            )
        }
    }
}