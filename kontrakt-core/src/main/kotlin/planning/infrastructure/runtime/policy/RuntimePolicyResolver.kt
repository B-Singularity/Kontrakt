package planning.infrastructure.runtime.policy

import planning.domain.session.policy.ResolvedRuntimePolicy

/**
 * Resolves a new immutable runtime policy snapshot from:
 * - a high-level resource profile,
 * - an environment snapshot,
 * - optional historical telemetry,
 * - and optionally the previous resolved policy.
 *
 * Normative rules:
 * - resolution happens only at a stable policy-resolution boundary
 * - the returned policy is immutable
 * - the returned policy applies only to future sessions
 * - cold-start (telemetry == null) must choose conservative deterministic defaults
 * - adaptive implementations must include stability controls
 */
interface RuntimePolicyResolver {
    fun resolve(
        profile: ResourceProfile,
        environment: RuntimeEnvironment,
        telemetry: RecentTelemetry?,
        previousPolicy: ResolvedRuntimePolicy? = null,
    ): ResolvedRuntimePolicy
}