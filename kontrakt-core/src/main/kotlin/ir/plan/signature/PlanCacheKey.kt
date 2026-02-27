package ir.plan.signature

import ir.identity.CanonicalIdentifier
import ir.identity.CanonicalSignature
import java.util.Objects


/**
 * Deterministic cache key for canonical plans.
 *
 * Requirements:
 * - MUST include all versioning fields that affect plan semantics.
 * - MUST include partition and equality keys.
 * - MUST be immutable and have complete equals/hashCode.
 *
 * If any field is omitted, cache pollution and silent plan mismatch becomes possible.
 */
class PlanCacheKey private constructor(
    val workAccountingVersion: Long,
    val normalizationVersion: Long,
    val edgeOrderingVersion: Long,
    val capabilityProfileVersion: Long,
    val entropyVersion: Long,
    val partitionKey: CanonicalIdentifier,
    val equalityKey: CanonicalSignature
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

    override fun hashCode(): Int = Objects.hash(
        workAccountingVersion,
        normalizationVersion,
        edgeOrderingVersion,
        capabilityProfileVersion,
        entropyVersion,
        partitionKey,
        equalityKey
    )

    override fun toString(): String =
        "PlanCacheKey(w=$workAccountingVersion,n=$normalizationVersion,e=$edgeOrderingVersion," +
                "c=$capabilityProfileVersion,r=$entropyVersion,partition=$partitionKey,eq=$equalityKey)"

    companion object {
        fun issue(
            workAccountingVersion: Long,
            normalizationVersion: Long,
            edgeOrderingVersion: Long,
            capabilityProfileVersion: Long,
            entropyVersion: Long,
            partitionKey: CanonicalIdentifier,
            equalityKey: CanonicalSignature
        ): PlanCacheKey {
            return PlanCacheKey(
                workAccountingVersion,
                normalizationVersion,
                edgeOrderingVersion,
                capabilityProfileVersion,
                entropyVersion,
                partitionKey,
                equalityKey
            )
        }
    }
}