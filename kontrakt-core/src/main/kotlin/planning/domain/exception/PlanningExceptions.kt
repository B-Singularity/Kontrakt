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

/**
 * Raised when projected active members collide on a uniqueness key.
 *
 * This is a Planning semantic violation, not a raw metamodel fact-boundary error.
 */
class DuplicateActiveMemberKeyException(
    val ownerTypeFqcn: String,
    val keyKind: String,
    val duplicateKey: String,
) : ActiveMemberProjectionException(
    "Duplicate active member key: ownerType=$ownerTypeFqcn, keyKind=$keyKind, duplicateKey=$duplicateKey",
)

/**
 * Raised when canonical ordering comparator cannot produce a strict deterministic
 * order over already projected active members.
 */
class AmbiguousActiveMemberOrderingException(
    val ownerTypeFqcn: String,
    val reason: String,
) : ActiveMemberProjectionException(
    "Ambiguous active member ordering: ownerType=$ownerTypeFqcn, reason=$reason",
)


/**
 * Raised when a shape says a child type exists, but the corresponding child
 * reference is missing at the point where the expansion pipeline lowers the
 * shape into a concrete expansion decision.
 *
 * This should normally be unreachable because ResolvedTypeShape validates shape
 * cardinality at construction.
 */
class InvalidResolvedTypeShapeException(
    val subjectTypeId: String,
    val shapeKind: String,
    val reason: String,
) : PlanningExpansionException(
    "Invalid resolved type shape: subjectTypeId=$subjectTypeId, shapeKind=$shapeKind, reason=$reason"
)

/**
 * Base exception for planning type-expansion pipeline failures.
 *
 * This exception family belongs to Planning domain orchestration.
 * It is not a metamodel raw-fact error and not an adapter error.
 */
open class PlanningExpansionException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "PLANNING"
}

/**
 * Raised when TypeShapeProvider returns a shape for a different TypeReference.
 *
 * The mismatch fields are explicit because id, signature, and cycleId can drift
 * independently.
 */
class TypeShapeSubjectMismatchException(
    val expectedTypeId: String,
    val actualTypeId: String,
    val expectedSignature: String,
    val actualSignature: String,
    val expectedCycleId: String,
    val actualCycleId: String,
    val mismatchFields: String,
) : PlanningExpansionException(
    "Type shape subject mismatch: " +
            "mismatchFields=$mismatchFields, " +
            "expectedTypeId=$expectedTypeId, actualTypeId=$actualTypeId, " +
            "expectedSignature=$expectedSignature, actualSignature=$actualSignature, " +
            "expectedCycleId=$expectedCycleId, actualCycleId=$actualCycleId",
)

/**
 * Defensive guard for a ResolvedTypeShape whose cardinality law is violated.
 *
 * For shapes created through ResolvedTypeShape factories this should not happen.
 * It is still kept as a fail-closed guard against binary drift, future enum
 * expansion, malformed test fixtures, or non-factory construction mistakes.
 */
class CorruptResolvedTypeShapeException(
    val subjectTypeId: String,
    val shapeKind: String,
    val reason: String,
) : PlanningExpansionException(
    "Corrupt resolved type shape: subjectTypeId=$subjectTypeId, shapeKind=$shapeKind, reason=$reason",
)

/**
 * Raised when the planner reaches a type expansion kind that is intentionally
 * not implemented in the current step.
 *
 * The core must not silently treat unsupported expansion kinds as COMPOSITE or ATOMIC.
 */
class UnsupportedTypeExpansionException(
    val subjectTypeId: String,
    val shapeKind: String,
    val reason: String,
) : PlanningExpansionException(
    "Unsupported type expansion: subjectTypeId=$subjectTypeId, shapeKind=$shapeKind, reason=$reason",
)

/**
 * Raised when a lowered primitive identity uses a reserved sentinel value.
 *
 * Negative values are not rejected in general because a signed Long may carry
 * a valid 64-bit identity bit pattern. Only reserved sentinels are rejected.
 */
class InvalidLoweredTypeIdentityException(
    val ownerTypeFqcn: String,
    val typeIdentity64: Long,
) : PlanningExpansionException(
    "Invalid lowered type identity: ownerType=$ownerTypeFqcn, typeIdentity64=$typeIdentity64",
)


/**
 * Raised when RawTypeFactsProvider returns raw facts that do not belong to the
 * TypeReference requested by the expansion pipeline.
 *
 * This prevents adapter subject drift from reaching cache/interner/canonical
 * planning surfaces.
 */
class RawTypeFactsSubjectMismatchException(
    val expectedTypeId: String,
    val expectedSignature: String,
    val expectedCycleId: String,
    val expectedTypeIdentity64: Long,
    val actualOwnerTypeFqcn: String,
    val actualTypeIdentity64: Long,
    val expectedAlgorithmId: String,
    val actualAlgorithmId: String,
    val expectedAlgorithmVersion: Long,
    val actualAlgorithmVersion: Long,
    val mismatchFields: String,
) : PlanningExpansionException(
    "Raw type facts subject mismatch: " +
            "mismatchFields=$mismatchFields, " +
            "expectedTypeId=$expectedTypeId, " +
            "expectedSignature=$expectedSignature, " +
            "expectedCycleId=$expectedCycleId, " +
            "expectedTypeIdentity64=$expectedTypeIdentity64, " +
            "actualOwnerTypeFqcn=$actualOwnerTypeFqcn, " +
            "actualTypeIdentity64=$actualTypeIdentity64, " +
            "expectedAlgorithm=$expectedAlgorithmId@$expectedAlgorithmVersion, " +
            "actualAlgorithm=$actualAlgorithmId@$actualAlgorithmVersion",
)


class TypeCycleIdentityContractViolationException(
    val reason: String,
) : PlanningExpansionException(
    "TypeCycleIdentity contract violation: reason=$reason",
)


class TypeCycleIdentitySubjectMismatchException(
    val expectedTypeId: String,
    val actualTypeId: String,
    val expectedSignature: String,
    val actualSignature: String,
    val expectedCycleId: String,
    val actualCycleId: String,
    val expectedAlgorithmId: String,
    val actualAlgorithmId: String,
    val expectedAlgorithmVersion: Long,
    val actualAlgorithmVersion: Long,
    val mismatchFields: String,
) : PlanningExpansionException(
    "Type cycle identity subject mismatch: " +
            "mismatchFields=$mismatchFields, " +
            "expectedTypeId=$expectedTypeId, actualTypeId=$actualTypeId, " +
            "expectedSignature=$expectedSignature, actualSignature=$actualSignature, " +
            "expectedCycleId=$expectedCycleId, actualCycleId=$actualCycleId, " +
            "expectedAlgorithm=$expectedAlgorithmId@$expectedAlgorithmVersion, " +
            "actualAlgorithm=$actualAlgorithmId@$actualAlgorithmVersion",
)

class ActiveCycleWithoutBreakpointException(
    val subjectTypeId: String,
    val cycleDepth: Int,
    val identityAlgorithmId: String,
    val identityAlgorithmVersion: Long,
) : PlanningExpansionException(
    "Active cycle detected without deterministic breakpoint: " +
            "subjectTypeId=$subjectTypeId, cycleDepth=$cycleDepth, " +
            "identityAlgorithm=$identityAlgorithmId@$identityAlgorithmVersion",
)

class CompositeExpansionPlanContractViolationException(
    val reason: String,
) : PlanningExpansionException(
    "CompositeExpansionPlan contract violation: reason=$reason",
)

class UnsupportedPreflightShapeException(
    val subjectTypeId: String,
    val shapeKind: String,
    val reason: String,
) : PlanningExpansionException(
    "Unsupported preflight shape: subjectTypeId=$subjectTypeId, shapeKind=$shapeKind, reason=$reason",
)


/**
 * Raised when canonical material produced under one canonical version tuple is
 * compared, loaded, or interned under an incompatible version tuple.
 *
 * This exception is a protocol guard.
 *
 * Version mismatch must not silently degrade into:
 * - exact-match comparison;
 * - cache hit;
 * - replay acceptance;
 * - structural reuse.
 */
class CanonicalVersionMismatchException(
    val storedVersion: String,
    val currentVersion: String,
) : PlanningProtocolException(
    "Canonical version mismatch: stored=$storedVersion, current=$currentVersion",
)

/**
 * Raised when type-expansion domain material violates a ratified expansion
 * protocol.
 *
 * This exception means "Core-owned expansion material is malformed".
 *
 * It is not:
 * - an adapter failure;
 * - a cache miss;
 * - a runtime DI failure;
 * - a recoverable transient L2 event.
 */
class TypeExpansionContractViolationException(
    val reason: String,
) : PlanningExpansionException(
    "Type expansion contract violation: reason=$reason",
)

/**
 * Raised when canonical-domain material violates a ratified canonical protocol.
 *
 * Canonical protocol violations are not adapter failures and must not be
 * degraded into cache misses.
 *
 * Examples:
 * - malformed canonical version tuple;
 * - invalid canonical text component;
 * - invalid canonical signature byte material;
 * - illegal canonical comparison boundary.
 */
class CanonicalContractViolationException(
    val reason: String,
) : PlanningProtocolException(
    "Canonical contract violation: reason=$reason",
)