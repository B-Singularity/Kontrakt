package governance.policy

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Immutable runtime-policy snapshot for adapter-owned dispatch-lane execution.
 *
 * Architectural role:
 * - stable dispatch execution policy snapshot
 * - resolved only at the runtime-policy boundary
 * - fixed for the lifetime of the installed runtime-policy epoch
 *
 * This snapshot governs only adapter-owned delivery mechanics:
 * - lane count
 * - loop budgets
 * - bounded capacities
 * - quiescence grace values for partition drop / adapter close
 *
 * It does NOT govern:
 * - planner-core semantic output
 * - shared-slot lifecycle meaning
 * - waiter lifecycle meaning
 * - routing identity semantics
 */
class ResolvedDispatchLanePolicy private constructor(
    val laneCount: Int,
    val commandBatchBudget: Int,
    val timeoutBatchBudget: Int,
    val dirtyShardBatchBudget: Int,
    val replayBatchBudgetPerShard: Int,
    val deliveryBatchBudget: Int,
    val commandRingCapacity: Int,
    val readyQueueCapacity: Int,
    val registrationStoreCapacityPerShard: Int,
    val deadlineHeapCapacity: Int,
    val partitionDropQuiescenceTimeoutNanos: Long,
    val adapterCloseQuiescenceTimeoutNanos: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            laneCount: Int,
            commandBatchBudget: Int,
            timeoutBatchBudget: Int,
            dirtyShardBatchBudget: Int,
            replayBatchBudgetPerShard: Int,
            deliveryBatchBudget: Int,
            commandRingCapacity: Int,
            readyQueueCapacity: Int,
            registrationStoreCapacityPerShard: Int,
            deadlineHeapCapacity: Int,
            partitionDropQuiescenceTimeoutNanos: Long,
            adapterCloseQuiescenceTimeoutNanos: Long,
        ): ResolvedDispatchLanePolicy {
            validatePositivePowerOfTwo("laneCount", laneCount)
            validatePositive("commandBatchBudget", commandBatchBudget)
            validatePositive("timeoutBatchBudget", timeoutBatchBudget)
            validatePositive("dirtyShardBatchBudget", dirtyShardBatchBudget)
            validatePositive("replayBatchBudgetPerShard", replayBatchBudgetPerShard)
            validatePositive("deliveryBatchBudget", deliveryBatchBudget)
            validatePositive("commandRingCapacity", commandRingCapacity)
            validatePositive("readyQueueCapacity", readyQueueCapacity)
            validatePositive("registrationStoreCapacityPerShard", registrationStoreCapacityPerShard)
            validatePositive("deadlineHeapCapacity", deadlineHeapCapacity)

            if (commandBatchBudget > commandRingCapacity) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.commandBatchBudget must be <= commandRingCapacity: " +
                            "$commandBatchBudget > $commandRingCapacity",
                )
            }

            if (deliveryBatchBudget > readyQueueCapacity) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.deliveryBatchBudget must be <= readyQueueCapacity: " +
                            "$deliveryBatchBudget > $readyQueueCapacity",
                )
            }

            if (replayBatchBudgetPerShard > registrationStoreCapacityPerShard) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.replayBatchBudgetPerShard must be <= " +
                            "registrationStoreCapacityPerShard: " +
                            "$replayBatchBudgetPerShard > $registrationStoreCapacityPerShard",
                )
            }

            if (partitionDropQuiescenceTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.partitionDropQuiescenceTimeoutNanos must be > 0: " +
                            partitionDropQuiescenceTimeoutNanos,
                )
            }

            if (adapterCloseQuiescenceTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.adapterCloseQuiescenceTimeoutNanos must be > 0: " +
                            adapterCloseQuiescenceTimeoutNanos,
                )
            }

            return ResolvedDispatchLanePolicy(
                laneCount = laneCount,
                commandBatchBudget = commandBatchBudget,
                timeoutBatchBudget = timeoutBatchBudget,
                dirtyShardBatchBudget = dirtyShardBatchBudget,
                replayBatchBudgetPerShard = replayBatchBudgetPerShard,
                deliveryBatchBudget = deliveryBatchBudget,
                commandRingCapacity = commandRingCapacity,
                readyQueueCapacity = readyQueueCapacity,
                registrationStoreCapacityPerShard = registrationStoreCapacityPerShard,
                deadlineHeapCapacity = deadlineHeapCapacity,
                partitionDropQuiescenceTimeoutNanos = partitionDropQuiescenceTimeoutNanos,
                adapterCloseQuiescenceTimeoutNanos = adapterCloseQuiescenceTimeoutNanos,
            )
        }

        private fun validatePositive(
            name: String,
            value: Int,
        ) {
            if (value <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.$name must be > 0: $value",
                )
            }
        }

        private fun validatePositivePowerOfTwo(
            name: String,
            value: Int,
        ) {
            if (value <= 0 || value.countOneBits() != 1) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedDispatchLanePolicy.$name must be a positive power-of-two: $value",
                )
            }
        }
    }
}