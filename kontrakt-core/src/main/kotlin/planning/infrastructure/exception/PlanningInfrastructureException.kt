package planning.infrastructure.exception

import exception.KontraktException

/**
 * Root for planning infrastructure/runtime conditions.
 *
 * These are NOT order SSOT violations.
 * They represent infrastructure-local failures or governance signals that
 * adapter/region layers must translate into domain-level fault semantics.
 */
open class PlanningInfrastructureException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING_INFRA"
}
