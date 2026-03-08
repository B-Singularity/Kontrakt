package planning.domain.vo

/**
 * Domain-safe physical partition boundary for Tier-2 storage.
 *
 * This is not a cosmetic identifier.
 * It is the physical scoping unit for:
 * - partition bulk drop
 * - capacity governance
 * - shard ownership
 */
@JvmInline
value class PartitionId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "PartitionId must not be blank."
        }
    }

    override fun toString(): String = value
}