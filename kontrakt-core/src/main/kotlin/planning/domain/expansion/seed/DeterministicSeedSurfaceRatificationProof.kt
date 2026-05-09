package planning.domain.expansion.seed

import planning.domain.exception.TypeExpansionContractViolationException

/**
 * Proof that a DeterministicSeedSurface tuple was accepted by the run
 * ratification boundary.
 *
 * This is not:
 *
 * - a security bearer token;
 * - a random nonce;
 * - a cache key;
 * - a persisted fingerprint;
 * - or a cryptographic signature.
 *
 * It is a deterministic domain proof that the seed-surface tuple was assembled
 * by the authorized run-ratification path.
 *
 * Binding law:
 *
 * The proof covers:
 *
 * - DeterministicSeedSurfaceId;
 * - SeedVersionTuple;
 * - RootTimeEpochMillis.
 *
 * It deliberately does not expose or duplicate raw seed bytes.
 *
 * SeedMaterial is still included in DeterministicSeedSurface equality, but the
 * proof does not print or expose seed material.
 *
 * Issuance law:
 *
 * issueFromRunRatifier(...) is internal. Adapters must not mint this proof.
 * The run-ratifier / SeedMaterializer boundary issues it after resolving the
 * active deterministic seed law.
 */
class DeterministicSeedSurfaceRatificationProof private constructor(
    val proofId: String,
    val ratifierId: String,
    val ratifierVersion: String,
    private val coveredId: DeterministicSeedSurfaceId,
    private val coveredVersionTuple: SeedVersionTuple,
    private val coveredRootTimeEpochMillis: RootTimeEpochMillis,
    val schemaVersion: Int,
) {
    fun requireCovers(
        id: DeterministicSeedSurfaceId,
        versionTuple: SeedVersionTuple,
        rootTimeEpochMillis: RootTimeEpochMillis,
    ) {
        if (coveredId != id) {
            mismatch(
                field = "id",
                expected = coveredId.value,
                actual = id.value,
            )
        }

        if (coveredVersionTuple != versionTuple) {
            mismatch(
                field = "versionTuple",
                expected = coveredVersionTuple.toString(),
                actual = versionTuple.toString(),
            )
        }

        if (coveredRootTimeEpochMillis != rootTimeEpochMillis) {
            mismatch(
                field = "rootTimeEpochMillis",
                expected = coveredRootTimeEpochMillis.toString(),
                actual = rootTimeEpochMillis.toString(),
            )
        }
    }

    fun renderSummary(): String =
        "DeterministicSeedSurfaceRatificationProof(" +
                "schemaVersion=$schemaVersion, " +
                "ratifier=$ratifierId@$ratifierVersion, " +
                "proofId=<redacted>, " +
                "surfaceId=${coveredId.value}, " +
                "versionTuple=$coveredVersionTuple, " +
                "rootTime=$coveredRootTimeEpochMillis" +
                ")"

    override fun toString(): String = renderSummary()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeterministicSeedSurfaceRatificationProof) return false

        return proofId == other.proofId &&
                ratifierId == other.ratifierId &&
                ratifierVersion == other.ratifierVersion &&
                coveredId == other.coveredId &&
                coveredVersionTuple == other.coveredVersionTuple &&
                coveredRootTimeEpochMillis == other.coveredRootTimeEpochMillis &&
                schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        var result = proofId.hashCode()
        result = 31 * result + ratifierId.hashCode()
        result = 31 * result + ratifierVersion.hashCode()
        result = 31 * result + coveredId.hashCode()
        result = 31 * result + coveredVersionTuple.hashCode()
        result = 31 * result + coveredRootTimeEpochMillis.hashCode()
        result = 31 * result + schemaVersion
        return result
    }

    private fun mismatch(
        field: String,
        expected: String,
        actual: String,
    ): Nothing =
        throw TypeExpansionContractViolationException(
            reason =
                "DeterministicSeedSurfaceRatificationProof does not cover tuple: " +
                        "field=$field, expected=$expected, actual=$actual, proof=${renderSummary()}",
        )

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        private const val MAX_PROOF_ID_CHARS: Int = 128
        private const val MAX_RATIFIER_TOKEN_CHARS: Int = 128

        @JvmStatic
        internal fun issueFromRunRatifier(
            proofId: String,
            ratifierId: String,
            ratifierVersion: String,
            id: DeterministicSeedSurfaceId,
            versionTuple: SeedVersionTuple,
            rootTimeEpochMillis: RootTimeEpochMillis,
        ): DeterministicSeedSurfaceRatificationProof {
            requireProtocolToken(
                field = "DeterministicSeedSurfaceRatificationProof.proofId",
                value = proofId,
                maxChars = MAX_PROOF_ID_CHARS,
            )

            val canonicalRatifierId =
                canonicalizeProtocolId(
                    field = "DeterministicSeedSurfaceRatificationProof.ratifierId",
                    value = ratifierId,
                )

            requireProtocolToken(
                field = "DeterministicSeedSurfaceRatificationProof.ratifierVersion",
                value = ratifierVersion,
                maxChars = MAX_RATIFIER_TOKEN_CHARS,
            )

            return DeterministicSeedSurfaceRatificationProof(
                proofId = proofId,
                ratifierId = canonicalRatifierId,
                ratifierVersion = ratifierVersion,
                coveredId = id,
                coveredVersionTuple = versionTuple,
                coveredRootTimeEpochMillis = rootTimeEpochMillis,
                schemaVersion = CURRENT_SCHEMA_VERSION,
            )
        }

        private fun canonicalizeProtocolId(
            field: String,
            value: String,
        ): String {
            requireProtocolToken(
                field = field,
                value = value,
                maxChars = MAX_RATIFIER_TOKEN_CHARS,
            )

            var requiresLowercaseCopy = false

            for (index in value.indices) {
                val c = value[index]

                when (c) {
                    in 'A'..'Z' -> requiresLowercaseCopy = true

                    in 'a'..'z',
                    in '0'..'9',
                    '-',
                    '_',
                    '.',
                        -> {
                        // Already canonical-safe order id material.
                    }

                    else -> {
                        throw TypeExpansionContractViolationException(
                            reason = "$field contains a non-canonical order-id character.",
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
                builder.append(
                    if (c in 'A'..'Z') {
                        (c.code + 32).toChar()
                    } else {
                        c
                    },
                )
            }

            return builder.toString()
        }

        private fun requireProtocolToken(
            field: String,
            value: String,
            maxChars: Int,
        ) {
            if (value.isEmpty()) {
                throw TypeExpansionContractViolationException(
                    reason = "$field must not be empty.",
                )
            }

            if (value.length > maxChars) {
                throw TypeExpansionContractViolationException(
                    reason = "$field exceeds maximum allowed token length.",
                )
            }

            if (
                value.indexOf('|') >= 0 ||
                value.indexOf('\u0000') >= 0 ||
                value.indexOf('\n') >= 0 ||
                value.indexOf('\r') >= 0 ||
                value.indexOf('\t') >= 0
            ) {
                throw TypeExpansionContractViolationException(
                    reason = "$field contains a reserved order/control character.",
                )
            }
        }
    }
}
