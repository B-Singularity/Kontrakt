package planning.domain.exception

import exception.KontraktException
import planning.domain.projection.ConstructorRejectionRecord

/**
 * Root exception for Planning Protocol SSOT violations.
 *
 * Use this only for failures that must crash the app during boot / classloading,
 * or for hard invariants where continuing would corrupt determinism.
 */
open class PlanningProtocolException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING_PROTOCOL"
}

/**
 * Domain-specific integrity violation for planning protocol SSOT types.
 *
 * Raised during class loading / bootstrapping to prevent running with a corrupted protocol definition.
 */
class PlanningProtocolIntegrityException(
    message: String,
    cause: Throwable? = null,
) : PlanningProtocolException(message, cause)

/**
 * Raised when sentinel remapping fails to produce a non-reserved value.
 *
 * Hard protocol failure: continuing would corrupt the canonical identity space.
 */
class SentinelIntegrityException(
    message: String,
    cause: Throwable? = null,
) : PlanningProtocolException(message, cause)

/**
 * Raised when an input violates the port contract before entering the protocol layer.
 */
class PortContractViolationException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING_PORT"
}

/**
 * Raised when the current runtime does not satisfy the protocol's pinned environment behavior.
 *
 * This is an application boot failure by design.
 */
class EnvironmentIntegrityException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "ENVIRONMENT_LAW"
}


class PlanningProtocolDecodingException(message: String, cause: Throwable? = null) :
    PlanningProtocolException(message, cause)

/**
 * Runtime abort due to budget exhaustion.
 *
 * This is NOT a protocol SSOT violation:
 * - It is an expected fail-closed guardrail during execution.
 * - The protocol remains valid; the current run is aborted by design.
 */
class FuelExhaustedException(msg: String) : KontraktException(msg) {
    override val domain: String = "PLANNING_RUNTIME"
}


/**
 * Machine-readable domain fault attribution used by planning diagnostics.
 */
enum class FaultKind {
    USER_MODEL_INVALID,
    CAPABILITY_RESTRICTED,
    FRAMEWORK_INVARIANT_BROKEN,
    RESOURCE_EXHAUSTED,
}

/**
 * Root for planning-domain failures that are neither pure protocol boot failures
 * nor infrastructure-local exceptions.
 */
open class PlanningDomainException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING_DOMAIN"
}

/**
 * Runtime invariant failure inside the planner execution machine.
 *
 * This class intentionally replaces generic IllegalStateException / UnsupportedOperationException
 * within the planning core.
 */
class PlanningRuntimeInvariantException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING_RUNTIME"
}

/**
 * Raised when a user/schema-side component violates canonical key assembly rules.
 */
class InvalidCanonicalKeyComponentException(
    val component: String,
    val reason: String,
    val faultKind: FaultKind = FaultKind.USER_MODEL_INVALID,
) : PlanningDomainException(
    message = "Invalid canonical key component '$component': $reason"
)

/**
 * Raised when CanonicalEdgeKey collision is detected inside the Active Member Set.
 */
class AmbiguousEdgeKeyException(
    val key: Long,
    val faultKind: FaultKind,
    val evidence: List<String>,
) : PlanningDomainException(
    message = "Ambiguous CanonicalEdgeKey: key=$key evidence=$evidence"
)

/**
 * Raised when EntropyTargetKey collision is detected inside the Active Member Set.
 */
class AmbiguousEntropyTargetKeyException(
    val key: Long,
    val faultKind: FaultKind,
    val evidence: List<String>,
) : PlanningDomainException(
    message = "Ambiguous EntropyTargetKey: key=$key evidence=$evidence"
)

/**
 * Raised when deterministic cycle breaking fails after Stage-1 / Stage-2 attempts.
 */
class CycleDetectedException(
    val faultKind: FaultKind,
    val capabilityDemotions: List<String>,
    val truncated: Boolean,
) : PlanningDomainException(
    message = "Deterministic cycle break failed. truncated=$truncated demotions=$capabilityDemotions"
)

/**
 * Raised when a hard semantic/resource cap is exceeded.
 */
class CapacityExceededException(
    val limitType: String,
    val value: Long,
    val faultKind: FaultKind = FaultKind.RESOURCE_EXHAUSTED,
) : PlanningDomainException(
    message = "Capacity exceeded: limitType=$limitType value=$value"
)


/**
 * Base exception for Active Member projection / ordering semantic-choice failures.
 *
 * This exception is intentionally not a MetamodelException.
 *
 * MetamodelException is for raw metamodel fact-boundary violations.
 * This exception is for Planning Core semantic-choice failures over already emitted facts.
 */
open class ActiveMemberProjectionException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING"
}


/**
 * Raised when the Core cannot select any constructor for an expansion episode.
 *
 * This is distinct from AmbiguousConstructorSelectionException.
 *
 * Meaning:
 * - zero raw constructor candidates, or
 * - all candidates rejected by capability profile / synthetic filtering / conservative unknown rules.
 */
class NoEligibleConstructorSelectionException(
    val ownerTypeFqcn: String,
    val candidateCount: Int,
    val rejectionEvidence: List<ConstructorRejectionRecord>,
) : ActiveMemberProjectionException(
    "No eligible constructor can be selected for ownerType=$ownerTypeFqcn, " +
            "candidateCount=$candidateCount, rejectionEvidenceCount=${rejectionEvidence.size}"
)


/**
 * Raised when constructor selection survives every deterministic selection dimension
 * with more than one candidate still tied.
 *
 * This is distinct from NoEligibleConstructorSelectionException.
 */
class AmbiguousConstructorSelectionException(
    val ownerTypeFqcn: String,
    val tiedConstructorSignatures: List<String>,
) : ActiveMemberProjectionException(
    "Ambiguous constructor selection for ownerType=$ownerTypeFqcn, " +
            "tiedConstructorSignatures=$tiedConstructorSignatures"
)