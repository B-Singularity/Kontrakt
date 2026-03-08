package planning.infrastructure.cache.adapter.outgoing

import ir.plan.signature.PlanCacheKey
import kontrakt.planning.domain.protocol.PrimitiveHash
import kontrakt.planning.domain.protocol.SentinelRemapper
import planning.domain.protocol.CostCenter
import planning.domain.session.PlannerSession

/**
 * Adapter-private deterministic lowering from the full PlanCacheKey tuple
 * to a primitive 64-bit routing key for hot-path Tier-2 tables.
 *
 * Why this helper exists in Phase 3:
 * - Tier-2 constitutionally requires primitive 64-bit hot routing
 * - PlanKeyFactory belongs to a later phase
 * - therefore Phase 3 needs a temporary but deterministic lowering path
 *
 * Non-negotiable rules:
 * - include all semantic version fields
 * - include partition and equality dimensions
 * - use deterministic hashing only
 * - remap reserved sentinel values deterministically
 */
internal object PlanCacheRouteKeyDeriver {

    fun derive(
        key: PlanCacheKey,
        session: PlannerSession,
    ): Long {
        session.step(CostCenter.PLAN_CACHE_KEY_MATERIALIZE)
        session.step(CostCenter.CANONICAL_SIGNATURE_MATERIALIZE)

        val partitionBytes = key.partitionKey.value.encodeToByteArray()
        val equalityBytes = key.equalityKey.bytesCopy()

        val totalSize =
            (5 * Long.SIZE_BYTES) +
                    Int.SIZE_BYTES + partitionBytes.size +
                    Int.SIZE_BYTES + equalityBytes.size

        val payload = ByteArray(totalSize)
        var offset = 0

        offset = putLongLE(payload, offset, key.workAccountingVersion)
        offset = putLongLE(payload, offset, key.normalizationVersion)
        offset = putLongLE(payload, offset, key.edgeOrderingVersion)
        offset = putLongLE(payload, offset, key.capabilityProfileVersion)
        offset = putLongLE(payload, offset, key.entropyVersion)

        offset = putBytesLengthPrefixed(payload, offset, partitionBytes)
        putBytesLengthPrefixed(payload, offset, equalityBytes)

        val seed = deriveSeed(key)
        val raw = PrimitiveHash.hash64(payload, seed)

        /*
         * The Tier-2 key path must route through SentinelRemapper.
         * We seal both reserved spaces:
         * - -1L : reserved sentinel family
         * -  0L : reserved by LongKeyTable as EMPTY
         */
        val nonMax = SentinelRemapper.remapNonMax(raw, key.edgeOrderingVersion)
        return SentinelRemapper.remapNonZero(nonMax, key.normalizationVersion)
    }

    private fun deriveSeed(key: PlanCacheKey): Long {
        var seed = 0L
        seed = PrimitiveHash.mix64(seed xor key.workAccountingVersion)
        seed = PrimitiveHash.mix64(seed xor key.normalizationVersion)
        seed = PrimitiveHash.mix64(seed xor key.edgeOrderingVersion)
        seed = PrimitiveHash.mix64(seed xor key.capabilityProfileVersion)
        seed = PrimitiveHash.mix64(seed xor key.entropyVersion)
        return seed
    }

    private fun putBytesLengthPrefixed(
        target: ByteArray,
        offset: Int,
        bytes: ByteArray,
    ): Int {
        var cursor = putIntLE(target, offset, bytes.size)
        bytes.copyInto(target, destinationOffset = cursor)
        cursor += bytes.size
        return cursor
    }

    private fun putIntLE(
        target: ByteArray,
        offset: Int,
        value: Int,
    ): Int {
        target[offset] = (value and 0xFF).toByte()
        target[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        target[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        target[offset + 3] = ((value ushr 24) and 0xFF).toByte()
        return offset + Int.SIZE_BYTES
    }

    private fun putLongLE(
        target: ByteArray,
        offset: Int,
        value: Long,
    ): Int {
        target[offset] = (value and 0xFFL).toByte()
        target[offset + 1] = ((value ushr 8) and 0xFFL).toByte()
        target[offset + 2] = ((value ushr 16) and 0xFFL).toByte()
        target[offset + 3] = ((value ushr 24) and 0xFFL).toByte()
        target[offset + 4] = ((value ushr 32) and 0xFFL).toByte()
        target[offset + 5] = ((value ushr 40) and 0xFFL).toByte()
        target[offset + 6] = ((value ushr 48) and 0xFFL).toByte()
        target[offset + 7] = ((value ushr 56) and 0xFFL).toByte()
        return offset + Long.SIZE_BYTES
    }
}