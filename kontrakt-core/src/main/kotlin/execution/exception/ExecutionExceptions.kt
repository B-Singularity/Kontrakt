package execution.domain.exception

import exception.KontraktException
import metamodel.domain.vo.TypeId

enum class LifecyclePhase { SETUP, EXECUTION, CLEANUP }
enum class FaultKind { USER_CONFIG, GRAPH_CONTRACT, SYSTEM_INTERNAL, SAFETY_GUARD, LIFECYCLE, ENVIRONMENT }

abstract class ExecutionDomainException(
    message: String,
    cause: Throwable? = null,
    val faultKind: FaultKind,
    val lifecyclePhase: LifecyclePhase = LifecyclePhase.SETUP
) : KontraktException(message, cause) {

    final override val domain: String = "EXECUTION"
    protected abstract val errorCode: String
    protected open val errorData: Map<String, Any?> = emptyMap()

    final override val details: Map<String, Any?> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        mapOf(
            "domain" to domain,
            "code" to "EXEC.$errorCode",
            "phase" to lifecyclePhase.name,
            "faultKind" to faultKind.name,
            "data" to errorData
        ).also { validateJsonSafety(it) } // Safe: passing local map
    }
}

// --- Bucket 1: Configuration ---
sealed class TestConfigurationException(msg: String, cause: Throwable? = null) :
    ExecutionDomainException(msg, cause, FaultKind.USER_CONFIG)

class GeneratorNotFoundException(val typeId: TypeId, val attributes: Set<String>) :
    TestConfigurationException("No generator for '$typeId'.") {
    override val errorCode = "GENERATOR_NOT_FOUND"
    override val errorData = mapOf("typeId" to typeId.toString(), "attributes" to attributes.toList())
}

class ImplementationResolutionException(val typeId: TypeId, details: String) :
    TestConfigurationException("Missing impl for '$typeId': $details") {
    override val errorCode = "IMPLEMENTATION_MISSING"
    override val errorData = mapOf("typeId" to typeId.toString(), "reason" to details)
}

class ConfigurationConflictException(val typeId: TypeId, val fieldName: String?, details: String) :
    TestConfigurationException("Config conflict in '$typeId': $details") {
    override val errorCode = "CONFIG_CONFLICT"
    override val errorData = mapOf("typeId" to typeId.toString(), "fieldName" to fieldName, "reason" to details)
}

class InvalidConfigurationValueException(val typeId: TypeId, val fieldName: String?, val value: Any?, reason: String) :
    TestConfigurationException("Invalid value '$value' in '$typeId': $reason") {
    override val errorCode = "CONFIG_INVALID_VALUE"
    override val errorData = mapOf(
        "typeId" to typeId.toString(),
        "fieldName" to fieldName,
        "value" to (value?.toString() ?: "null"),
        "reason" to reason
    )
}

// --- Bucket 2: Synthesis ---
sealed class TestSynthesisException(
    msg: String,
    cause: Throwable? = null,
    kind: FaultKind = FaultKind.SYSTEM_INTERNAL
) : ExecutionDomainException(msg, cause, kind)

class StructuralPlanningException(val typeId: TypeId, details: String, cause: Throwable? = null) :
    TestSynthesisException("Planning failed for '$typeId': $details", cause) {
    override val errorCode = "PLANNING_FAILED"
    override val errorData = mapOf("typeId" to typeId.toString(), "reason" to details)
}

sealed class LinkageException(msg: String, cause: Throwable? = null, kind: FaultKind = FaultKind.SYSTEM_INTERNAL) :
    TestSynthesisException(msg, cause, kind)

class CircularDependencyException(val path: List<TypeId>, details: String) : LinkageException(
    "[Cycle] Path: ${path.joinToString(" -> ")}. $details",
    null,
    FaultKind.GRAPH_CONTRACT
) {
    override val errorCode = "CYCLE_DETECTED"
    override val errorData = mapOf("path" to path.map { it.toString() }, "reason" to details)
}

// --- Bucket 3: Runtime ---
sealed class VirtualMachineException(
    msg: String,
    cause: Throwable? = null,
    kind: FaultKind = FaultKind.SYSTEM_INTERNAL
) : ExecutionDomainException(msg, cause, kind)

class VMRuntimeException(val typeId: TypeId, msg: String, cause: Throwable? = null) :
    VirtualMachineException("VM failed for '$typeId': $msg", cause) {
    override val errorCode = "VM_EXECUTION_FAILED"
    override val errorData = mapOf("typeId" to typeId.toString())
}

class CollectionSizeLimitExceededException(val typeId: TypeId, val current: Int, val limit: Int) :
    VirtualMachineException("Size $current > $limit for '$typeId'.", null, FaultKind.SAFETY_GUARD) {
    override val errorCode = "SIZE_LIMIT_EXCEEDED"
    override val errorData = mapOf("typeId" to typeId.toString(), "current" to current, "limit" to limit)
}

// --- Cross-Cutting ---
class KontraktLifecycleException(
    val component: String,
    val action: String,
    val reason: String,
    phase: LifecyclePhase,
    cause: Throwable? = null
) : ExecutionDomainException("Lifecycle violation: $reason", cause, FaultKind.LIFECYCLE, phase) {
    override val errorCode = "LIFECYCLE_VIOLATION"
    override val errorData = mapOf("component" to component, "action" to action, "reason" to reason)
}

class KontraktEnvironmentException(
    val component: String,
    val resource: String,
    val reason: String,
    phase: LifecyclePhase,
    cause: Throwable? = null
) : ExecutionDomainException("Environment error: $reason", cause, FaultKind.ENVIRONMENT, phase) {
    override val errorCode = "ENVIRONMENT_ERROR"
    override val errorData = mapOf("component" to component, "resource" to resource, "reason" to reason)
}