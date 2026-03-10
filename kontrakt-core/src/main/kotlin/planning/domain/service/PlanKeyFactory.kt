package planning.domain.service

import ir.identity.CanonicalIdentifier
import ir.identity.CanonicalSignature
import ir.plan.signature.PlanCacheKey
import kontrakt.planning.domain.protocol.HashInputEncodingSpec
import kontrakt.planning.domain.protocol.PrimitiveHash
import kontrakt.planning.domain.protocol.SentinelRemapper
import planning.domain.exception.EnvironmentIntegrityException
import planning.domain.exception.SentinelIntegrityException
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession
import planning.domain.vo.PartitionId

/**
 * Domain service for deterministic plan-cache key issuance.
 *
 * Current alignment:
 * - [PlanCacheKey] carries the full exact-match tuple and route64.
 * - [PartitionId] remains the domain-level argument.
 * - lowering to [ir.identity.CanonicalIdentifier] happens only inside this service.
 */
class PlanKeyFactory private constructor(
    private val encodingSpec: HashInputEncodingSpec,
) {

    fun issue(
        partitionId: PartitionId,
        equalityKey: CanonicalSignature,
        session: PlannerSession,
    ): PlanCacheKey {
        ensureNormalizationVersionMatches(session)

        session.step(CostCenter.PLAN_CACHE_KEY_MATERIALIZE)
        session.step(CostCenter.CANONICAL_SIGNATURE_MATERIALIZE)

        val route64 = deriveRoute64(
            partitionId = partitionId,
            equalityKey = equalityKey,
            session = session,
        )

        return PlanCacheKey.issue(
            workAccountingVersion = session.config.workAccountingVersion,
            normalizationVersion = session.config.normalizationSpecVersion,
            edgeOrderingVersion = session.config.edgeOrderingVersion,
            capabilityProfileVersion = session.config.capabilityProfileVersion,
            entropyVersion = session.config.entropyVersion,
            partitionKey = lowerPartitionId(partitionId),
            equalityKey = equalityKey,
            route64 = route64,
        )
    }

    private fun deriveRoute64(
        partitionId: PartitionId,
        equalityKey: CanonicalSignature,
        session: PlannerSession,
    ): Long {
        val partitionEncoded = encodingSpec.encodeStrict(partitionId.value)
        val equalityEncoded = wrapLengthPrefix(equalityKey.bytesCopy())

        val payload = ByteArray(partitionEncoded.size + equalityEncoded.size)
        System.arraycopy(partitionEncoded, 0, payload, 0, partitionEncoded.size)
        System.arraycopy(equalityEncoded, 0, payload, partitionEncoded.size, equalityEncoded.size)

        var seed = 0L
        seed = PrimitiveHash.mix64(seed xor session.config.workAccountingVersion)
        seed = PrimitiveHash.mix64(seed xor session.config.normalizationSpecVersion)
        seed = PrimitiveHash.mix64(seed xor session.config.edgeOrderingVersion)
        seed = PrimitiveHash.mix64(seed xor session.config.capabilityProfileVersion)
        seed = PrimitiveHash.mix64(seed xor session.config.entropyVersion)

        val raw = PrimitiveHash.hash64(payload, seed)
        val nonZero = SentinelRemapper.remapNonZero(raw, session.config.normalizationSpecVersion)
        val nonMax = SentinelRemapper.remapNonMax(nonZero, session.config.edgeOrderingVersion)

        if (nonMax == 0L || nonMax == -1L) {
            throw SentinelIntegrityException(
                "PlanKeyFactory failed to produce a non-reserved route64."
            )
        }

        return nonMax
    }

    /**
     * Converts the domain partition VO into the IR-level canonical identifier shape.
     *
     * Replace the factory call below with the actual issuance API exposed by CanonicalIdentifier
     * in your codebase.
     */
    private fun lowerPartitionId(partitionId: PartitionId): CanonicalIdentifier {
        return CanonicalIdentifier.issue(partitionId.value)
    }

    /**
     * Applies the same length-prefix law to already-materialized raw bytes.
     */
    private fun wrapLengthPrefix(bytes: ByteArray): ByteArray {
        val result = ByteArray(Int.SIZE_BYTES + bytes.size)
        val len = bytes.size

        result[0] = (len and 0xFF).toByte()
        result[1] = ((len ushr 8) and 0xFF).toByte()
        result[2] = ((len ushr 16) and 0xFF).toByte()
        result[3] = ((len ushr 24) and 0xFF).toByte()

        System.arraycopy(bytes, 0, result, Int.SIZE_BYTES, bytes.size)
        return result
    }

    private fun ensureNormalizationVersionMatches(session: PlannerSession) {
        if (session.config.normalizationSpecVersion != encodingSpec.normalizationSpecVersion) {
            throw EnvironmentIntegrityException(
                "Normalization version mismatch: session=${session.config.normalizationSpecVersion}, " +
                        "encodingSpec=${encodingSpec.normalizationSpecVersion}"
            )
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            encodingSpec: HashInputEncodingSpec,
        ): PlanKeyFactory {
            return PlanKeyFactory(
                encodingSpec = encodingSpec,
            )
        }
    }
}