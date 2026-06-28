package stage.lowering.material

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

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
value class PartitionId private constructor(
    val value: String,
) {
    override fun toString(): String = value

    companion object {
        /**
         * Canonical factory for PartitionId.
         *
         * We intentionally avoid require()/check() to preserve consistent
         * order-specific exception semantics.
         */
        @JvmStatic
        fun issue(value: String): PartitionId {
            if (value.isBlank()) {
                throw PlanningProtocolIntegrityException("PartitionId must not be blank.")
            }
            return PartitionId(value)
        }
    }
}
