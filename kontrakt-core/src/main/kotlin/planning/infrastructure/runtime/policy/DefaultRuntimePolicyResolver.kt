package planning.infrastructure.runtime.policy

import planning.domain.session.policy.ResolvedJoinGovernance
import planning.domain.session.policy.ResolvedRuntimePolicy
import planning.domain.session.policy.ResolvedSessionBudget
import planning.domain.session.policy.ResolvedStorageGovernance

/**
 * Official v1 bootstrap runtime-policy resolver.
 *
 * Design intent:
 * - deterministic cold-start bootstrap policy
 * - AUTO aliases STANDARD in v1
 * - no live mutation of an already-running session
 * - no hidden runtime heuristics in the planner core
 *
 * This resolver intentionally issues the ratified bootstrap values directly
 * from the approved v1 tables rather than smuggling a node-floor framing into
 * the profile definition.
 */
class DefaultRuntimePolicyResolver : RuntimePolicyResolver {

    override fun resolve(
        profile: ResourceProfile,
        environment: RuntimeEnvironment,
        telemetry: RecentTelemetry?,
        previousPolicy: ResolvedRuntimePolicy?,
    ): ResolvedRuntimePolicy {
        val activeProfile = when (profile) {
            ResourceProfile.AUTO -> ResourceProfile.STANDARD
            else -> profile
        }

        val sessionBudget = resolveSessionBudget(activeProfile)
        val joinGovernance = resolveJoinGovernance()
        val storageGovernance = resolveStorageGovernance(sessionBudget)

        return ResolvedRuntimePolicy.issue(
            sessionBudget = sessionBudget,
            joinGovernance = joinGovernance,
            storageGovernance = storageGovernance,
            wallClockPolicy = null,
        )
    }

    private fun resolveSessionBudget(profile: ResourceProfile): ResolvedSessionBudget {
        return when (profile) {
            ResourceProfile.SMALL -> ResolvedSessionBudget.issue(
                maxPlannerBytesPerWorker = 10L * 1024L * 1024L,
                maxPhysicalSteps = 2_097_152,
                maxSemanticWorkUnits = 524_288,
                maxSignatureLen = 4096,
                fixedHeadroomBytes = 512L * 1024L,
                physicalStepMultiplier = 16,
                semanticWorkMultiplier = 4,
            )

            ResourceProfile.STANDARD -> ResolvedSessionBudget.issue(
                maxPlannerBytesPerWorker = 20L * 1024L * 1024L,
                maxPhysicalSteps = 4_194_304,
                maxSemanticWorkUnits = 1_048_576,
                maxSignatureLen = 4096,
                fixedHeadroomBytes = 512L * 1024L,
                physicalStepMultiplier = 16,
                semanticWorkMultiplier = 4,
            )

            ResourceProfile.LARGE -> ResolvedSessionBudget.issue(
                maxPlannerBytesPerWorker = 40L * 1024L * 1024L,
                maxPhysicalSteps = 8_388_608,
                maxSemanticWorkUnits = 2_097_152,
                maxSignatureLen = 4096,
                fixedHeadroomBytes = 512L * 1024L,
                physicalStepMultiplier = 16,
                semanticWorkMultiplier = 4,
            )

            ResourceProfile.AUTO ->
                throw IllegalStateException("AUTO must be normalized before resolveSessionBudget")
        }
    }

    private fun resolveJoinGovernance(): ResolvedJoinGovernance {
        return ResolvedJoinGovernance.issue(
            joinWaitTimeoutNanos = 2_000_000L,
            maxWaitersPerKey = 8,
            maxSpeculativeBuildersPerKey = 0,
            failFastOnQuotaExhaustion = false,
        )
    }

    private fun resolveStorageGovernance(
        budget: ResolvedSessionBudget,
    ): ResolvedStorageGovernance {
        val storageMultiplier = 2.0
        val storageEntryBytesBaseline = 1024L

        val maxApproxBytesPerPartition =
            (budget.maxPlannerBytesPerWorker * storageMultiplier).toLong()

        val rawEntries = maxApproxBytesPerPartition / storageEntryBytesBaseline
        val maxEntriesPerPartition = lowerPowerOfTwo(rawEntries.toInt())

        return ResolvedStorageGovernance.issue(
            maxApproxBytesPerPartition = maxApproxBytesPerPartition,
            maxEntriesPerPartition = maxEntriesPerPartition,
            circuitOpenOnStorageExhaustion = true,
        )
    }

    private fun lowerPowerOfTwo(n: Int): Int {
        var result = 1
        while (result <= n / 2) {
            result *= 2
        }
        return result
    }
}