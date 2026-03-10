package ir.plan.signature

import ir.identity.CanonicalIdentifier
import ir.identity.CanonicalSignature
import java.util.Objects


/**
 * Deterministic cache key for canonical plans.
 *
 * Semantics:
 * - Exact equality is defined by the full 7-tuple:
 *   workAccountingVersion, normalizationVersion, edgeOrderingVersion,
 *   capabilityProfileVersion, entropyVersion, partitionKey, equalityKey.
 *
 * Routing:
 * - [route64] is a deterministic primitive routing artifact for Tier-2 hot paths.
 * - It MUST be derived from the same semantic tuple, but it does NOT participate in equality.
 *
 * Rationale:
 * - exact-match correctness belongs to the full tuple
 * - fast routing belongs to the primitive route key
 */
class PlanCacheKey private constructor(
    val workAccountingVersion: Long,
    val normalizationVersion: Long,
    val edgeOrderingVersion: Long,
    val capabilityProfileVersion: Long,
    val entropyVersion: Long,
    val partitionKey: CanonicalIdentifier,
    val equalityKey: CanonicalSignature,
    val route64: Long,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlanCacheKey) return false

        return workAccountingVersion == other.workAccountingVersion &&
                normalizationVersion == other.normalizationVersion &&
                edgeOrderingVersion == other.edgeOrderingVersion &&
                capabilityProfileVersion == other.capabilityProfileVersion &&
                entropyVersion == other.entropyVersion &&
                partitionKey == other.partitionKey &&
                equalityKey == other.equalityKey
    }

    /**
     * Hash code follows exact-match semantics, not routing semantics.
     *
     * This intentionally excludes [route64] so that equality and hashCode remain aligned
     * with the full exact-match tuple.
     */
    override fun hashCode(): Int = Objects.hash(
        workAccountingVersion,
        normalizationVersion,
        edgeOrderingVersion,
        capabilityProfileVersion,
        entropyVersion,
        partitionKey,
        equalityKey,
    )

    override fun toString(): String =
        "PlanCacheKey(" +
                "w=$workAccountingVersion," +
                "n=$normalizationVersion," +
                "e=$edgeOrderingVersion," +
                "c=$capabilityProfileVersion," +
                "r=$entropyVersion," +
                "partition=$partitionKey," +
                "eq=$equalityKey," +
                "route64=$route64" +
                ")"

    companion object {
        @JvmStatic
        fun issue(
            workAccountingVersion: Long,
            normalizationVersion: Long,
            edgeOrderingVersion: Long,
            capabilityProfileVersion: Long,
            entropyVersion: Long,
            partitionKey: CanonicalIdentifier,
            equalityKey: CanonicalSignature,
            route64: Long,
        ): PlanCacheKey {
            return PlanCacheKey(
                workAccountingVersion = workAccountingVersion,
                normalizationVersion = normalizationVersion,
                edgeOrderingVersion = edgeOrderingVersion,
                capabilityProfileVersion = capabilityProfileVersion,
                entropyVersion = entropyVersion,
                partitionKey = partitionKey,
                equalityKey = equalityKey,
                route64 = route64,
            )
        }
    }
}