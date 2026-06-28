package governance.policy

/**
 * Top-level immutable runtime-policy snapshot pinned at session start.
 *
 * This is the runtime-boundary bundle consumed by bootstrap assembly.
 * The planner core and worker-local state MUST observe only already-resolved
 * immutable values derived from this snapshot.
 *
 * ADR alignment:
 * - ADR-0033 v1 runtime-policy surface:
 *   ResolvedSessionBudget + ResolvedJoinGovernance + ResolvedStorageGovernance
 *   + optional ResolvedWallClockPolicy
 */
class ResolvedRuntimePolicy private constructor(
    val sessionBudget: ResolvedSessionBudget,
    val joinGovernance: ResolvedJoinGovernance,
    val storageGovernance: ResolvedStorageGovernance,
    val wallClockPolicy: ResolvedWallClockPolicy?,
) {
    companion object {
        @JvmStatic
        fun issue(
            sessionBudget: ResolvedSessionBudget,
            joinGovernance: ResolvedJoinGovernance,
            storageGovernance: ResolvedStorageGovernance,
            wallClockPolicy: ResolvedWallClockPolicy? = null,
        ): ResolvedRuntimePolicy =
            ResolvedRuntimePolicy(
                sessionBudget = sessionBudget,
                joinGovernance = joinGovernance,
                storageGovernance = storageGovernance,
                wallClockPolicy = wallClockPolicy,
            )
    }
}
