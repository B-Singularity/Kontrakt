package execution.domain.exception

import diagnostic.retention.diagnostics.exception.KontraktException

enum class LifecyclePhase { SETUP, EXECUTION, CLEANUP }

enum class FaultKind { USER_CONFIG, GRAPH_CONTRACT, SYSTEM_INTERNAL, SAFETY_GUARD, LIFECYCLE, ENVIRONMENT, RUNTIME_ERROR }

abstract class ExecutionDomainException(
    message: String,
    cause: Throwable? = null,
    val faultKind: FaultKind,
    val lifecyclePhase: LifecyclePhase = LifecyclePhase.EXECUTION,
) : KontraktException(message, cause) {
    final override val domain: String = "EXECUTION"
    protected abstract val errorCode: String
    protected open val errorData: Map<String, Any?> = emptyMap()

    override val payload: Map<String, Any?> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        mapOf(
            "domain" to domain,
            "code" to "EXEC.$errorCode",
            "phase" to lifecyclePhase.name,
            "faultKind" to faultKind.name,
            "data" to errorData,
        )
    }
}

class RuntimeInstantiationException(
    message: String,
    cause: Throwable? = null,
) : ExecutionDomainException(
    message,
    cause,
    FaultKind.SYSTEM_INTERNAL,
    LifecyclePhase.EXECUTION,
) {
    override val errorCode = "RUNTIME_INSTANTIATION_FAILED"
}

class KontraktLifecycleException(
    val component: String,
    val action: String,
    val reason: String,
    phase: LifecyclePhase = LifecyclePhase.EXECUTION,
    cause: Throwable? = null,
) : ExecutionDomainException("Lifecycle violation: $reason", cause, FaultKind.LIFECYCLE, phase) {
    override val errorCode = "LIFECYCLE_VIOLATION"
    override val errorData = mapOf("component" to component, "action" to action, "reason" to reason)
}
