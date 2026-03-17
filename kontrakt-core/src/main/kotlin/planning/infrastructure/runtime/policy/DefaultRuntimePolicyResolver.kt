package planning.infrastructure.runtime.policy

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.session.policy.ResolvedRuntimePolicy

/**
 * Placeholder implementation boundary.
 *
 * Deliberately left without a concrete default heuristic because:
 * - the current normative documents define the contract shape,
 * - but do not yet ratify concrete numeric policy defaults,
 * - and do not yet ratify a production adaptive control formula.
 *
 * A production implementation must be introduced only after:
 * - cold-start defaults,
 * - adaptive stability controls,
 * - and operational numeric defaults
 * are formally documented.
 */
class DeferredRuntimePolicyResolver private constructor() : RuntimePolicyResolver {

    override fun resolve(
        profile: ResourceProfile,
        environment: RuntimeEnvironment,
        telemetry: RecentTelemetry?,
        previousPolicy: ResolvedRuntimePolicy?,
    ): ResolvedRuntimePolicy {
        throw PlanningProtocolIntegrityException(
            "DeferredRuntimePolicyResolver has no ratified production heuristic yet."
        )
    }

    companion object {
        @JvmStatic
        fun issue(): DeferredRuntimePolicyResolver = DeferredRuntimePolicyResolver()
    }
}