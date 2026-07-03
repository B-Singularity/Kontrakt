package stage.admission.diagnostics.evidence

import stage.canonicalization.material.frozen.image.FrozenMetamodelImageId
import stage.diagnostic.material.KontraktException

/**
 * Base exception for Metamodel domain errors.
 *
 * This type integrates metamodel failures into Kontrakt's unified exception hierarchy.
 *
 * IMPORTANT:
 * DTO / VO factories should normally not throw this base type directly.
 * Prefer one of the specific subtypes below so callers can distinguish malformed
 * raw facts, invalid canonical components, duplicate facts, ownership mismatch,
 * strict-mode violations, and deterministic-boundary violations.
 */
open class MetamodelException(
    message: String,
    cause: Throwable? = null,
) : KontraktException(message, cause) {
    override val domain: String = "METAMODEL"
}

/**
 * Base exception for normalized metamodel fact-boundary contract violations.
 *
 * Use this family when an adapter attempts to emit malformed raw structural facts.
 */
open class MetamodelFactContractViolationException(
    message: String,
    cause: Throwable? = null,
) : MetamodelException(message, cause)

/**
 * Thrown when a canonical string component violates metamodel fact-boundary rules.
 *
 * Examples:
 * - blank owner type name
 * - blank member name
 * - reserved delimiter in canonical component
 * - ISO control characters in canonical component
 */
class InvalidMetamodelCanonicalComponentException(
    val field: String,
    val value: String,
    val reason: String,
) : MetamodelFactContractViolationException(
    "Invalid metamodel canonical component: field=$field, reason=$reason, value=${safeDiagnosticValue(value)}",
)

/**
 * Thrown when DeclarationOrdinal violates its semantic contract.
 *
 * Example:
 * - Present(-1)
 */
class InvalidDeclarationOrdinalException(
    val ordinal: Int,
    val reason: String,
) : MetamodelFactContractViolationException(
    "Invalid declaration ordinal: ordinal=$ordinal, reason=$reason",
)

/**
 * Thrown when one raw metamodel DTO has internally malformed shape.
 *
 * Examples:
 * - negative normalization version
 * - constructor parameter index out of compact range
 * - duplicate constructor parameter index
 */
class InvalidTypeFactShapeException(
    val owner: String,
    val factKind: String,
    val reason: String,
) : MetamodelFactContractViolationException(
    "Invalid type-fact shape: owner=$owner, factKind=$factKind, reason=$reason",
)

/**
 * Thrown when one raw fact DTO contains duplicated facts that should have been
 * reconciled or rejected by the adapter boundary.
 */
class DuplicateMetamodelFactException(
    val owner: String,
    val factKind: String,
    val duplicateKey: String,
    val reason: String,
) : MetamodelFactContractViolationException(
    "Duplicate metamodel fact: owner=$owner, factKind=$factKind, duplicateKey=${safeDiagnosticValue(duplicateKey)}, reason=$reason",
)

/**
 * Thrown when a child fact claims a different owner than its enclosing raw DTO
 * or enclosing constructor candidate.
 */
class MetamodelFactOwnershipMismatchException(
    val expectedOwner: String,
    val actualOwner: String,
    val factKind: String,
) : MetamodelFactContractViolationException(
    "Metamodel fact ownership mismatch: factKind=$factKind, expectedOwner=$expectedOwner, actualOwner=$actualOwner",
)

/**
 * Thrown when strict mode policies are violated.
 *
 * Example:
 * - using unresolved generics
 * - accepting wildcards where strict mode forbids them
 */
class StrictModeViolationException(
    message: String,
) : MetamodelException(message)

/**
 * Thrown when non-deterministic behavior is detected at the metamodel boundary.
 *
 * Example:
 * - preserving reflection enumeration order as if it were stable declaration order
 * - duplicate annotations whose source order cannot be deterministically reconciled
 */
class DeterminismViolationException(
    message: String,
) : MetamodelException(message)

/**
 * Thrown when a caller passes a TypeReference that this adapter cannot resolve.
 *
 * Example:
 * - ReflectionRawTypeFactsProvider receives a KSP-backed TypeReference.
 */
class UnsupportedMetamodelSourceException(
    message: String,
) : MetamodelException(message)

/**
 * Thrown when adapter-local metamodel infrastructure observes an impossible
 * lifecycle or identity-binding state.
 *
 * Examples:
 * - a reflection TypeReference is bound to one KType and later to another;
 * - the same KType is bound to two different TypeReference values in one
 *   adapter scope;
 * - a closed adapter-local registry is used after shutdown;
 * - a forward/reverse sidecar binding invariant is violated.
 *
 * This is not a strict-mode modeling error.
 * This is not a malformed raw DTO shape.
 * This is adapter-local order/infrastructure integrity failure.
 */
class MetamodelAdapterStateViolationException(
    message: String,
) : MetamodelException(message)

/**
 * Thrown when an adapter composition root is miswired.
 *
 * Examples:
 *
 * - an injected derivation algorithm advertises an empty id;
 * - an algorithm version is outside the ratified order range;
 * - classifier governance tokens are invalid;
 * - an adapter assembler fails while constructing a bundle.
 *
 * This exception identifies bootstrap/composition blame.
 *
 * It is not:
 *
 * - a user model strict-mode violation;
 * - a raw fact DTO contract violation;
 * - a runtime planning failure;
 * - an L2 cache/governance failure.
 */
class MetamodelAdapterAssemblyException(
    message: String,
) : MetamodelException(message)

/**
 * Thrown when the adapter emits or receives a component that violates the
 * ratified Kontrakt normalization boundary.
 *
 * Kontrakt uses NFC-REJECT at this boundary.
 * The adapter may canonicalize JVM-specific spelling such as '$' to '.', but it
 * must not silently Unicode-normalize strings behind the order's back.
 */
class MetamodelNormalizationViolationException(
    val field: String,
    val valueSample: String,
    val engineId: String,
    val engineVersion: String,
    val reason: String,
) : MetamodelFactContractViolationException(
    "Metamodel normalization violation: field=$field, engine=$engineId@$engineVersion, " +
            "reason=$reason, valueSample=$valueSample",
)

/**
 * Base exception family for frozen metamodel image integrity failures.
 *
 * This family is intentionally a concrete open class, not an abstract class,
 * to stay aligned with the existing metamodel exception hierarchy.
 *
 * DTO / VO factories and providers should normally prefer one of the specific
 * subtypes below so callers can distinguish:
 *
 * - unknown references;
 * - incomplete frozen tables;
 * - schema / compatibility violations;
 * - lifecycle violations;
 * - backend-handle reachability leakage;
 * - deterministic sequence violations;
 * - frozen-record materialization failures.
 *
 * This family is not:
 *
 * - an adapter assembly exception;
 * - an adapter state exception;
 * - a strict-mode modeling exception;
 * - an ordinary cache miss;
 * - an L2 cache/governance failure.
 */
open class FrozenMetamodelImageException(
    message: String,
    cause: Throwable? = null,
) : MetamodelException(message, cause)

/**
 * Thrown when a planning-facing frozen provider receives a TypeReference that
 * is not part of the frozen image type index.
 *
 * This is not a cache miss.
 *
 * It usually means one of:
 *
 * - the caller mixed TypeReference values from another frozen image;
 * - the acquisition scope did not include the required transitive type;
 * - planning provider wiring mixed images/scopes.
 */
class FrozenMetamodelUnknownTypeReferenceException(
    val imageId: Any,
    val referenceSummary: String,
    val requestedTable: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image does not contain requested TypeReference: " +
            "image=$imageId, requestedTable=$requestedTable, reference=$referenceSummary",
)

/**
 * Thrown when a TypeReference exists in the frozen image type index but the
 * requested table has no corresponding record.
 *
 * This means freeze produced an incomplete image.
 */
class FrozenMetamodelIncompleteTableException(
    val imageId: Any,
    val referenceSummary: String,
    val missingTable: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image table is incomplete: " +
            "image=$imageId, missingTable=$missingTable, reference=$referenceSummary, reason=$reason",
)

/**
 * Thrown when a frozen image cannot be consumed because schema, source,
 * algorithm, or compatibility law does not match the provider's expected law.
 */
class FrozenMetamodelImageCompatibilityException(
    val imageId: Any,
    val expectedSchemaVersion: String,
    val actualSchemaVersion: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image compatibility violation: " +
            "image=$imageId, expectedSchema=$expectedSchemaVersion, " +
            "actualSchema=$actualSchemaVersion, reason=$reason",
)

/**
 * Thrown when an acquisition lane, arena, or frozen image is used in an illegal
 * lifecycle state.
 *
 * Examples:
 * - write after freeze;
 * - second freeze call under non-idempotent freeze policy;
 * - acquire after close;
 * - freeze after close;
 * - provider read from acquisition-only state.
 */
class FrozenMetamodelImageLifecycleException(
    val imageId: Any?,
    val operation: String,
    val state: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image lifecycle violation: " +
            "image=$imageId, operation=$operation, state=$state, reason=$reason",
)

/**
 * Thrown when frozen material directly or indirectly retains backend-native
 * handle reachability.
 *
 * This exists because “not storing KType directly” is insufficient.
 *
 * Examples:
 * - KType field in a frozen record;
 * - KSDeclaration field in a frozen record;
 * - lambda/supplier capturing KType;
 * - registry ordinal that can recover KType;
 * - classloader-local lookup key;
 * - KSP resolver-local id.
 */
class FrozenMetamodelBackendReachabilityException(
    val imageId: Any,
    val recordKind: String,
    val forbiddenMaterialKind: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image retains forbidden backend-handle reachability: " +
            "image=$imageId, recordKind=$recordKind, " +
            "forbiddenMaterial=$forbiddenMaterialKind, reason=$reason",
)

/**
 * Thrown when a frozen deterministic sequence violates ordering, duplicate,
 * strict-total-order, compact-index, or local-ordinal law.
 *
 * Examples:
 * - duplicate constructor key;
 * - duplicate property key;
 * - duplicate non-repeatable annotation key;
 * - comparator equality between distinct records;
 * - non-compact parameter index;
 * - local ordinal assigned before deterministic ordering;
 * - backend enumeration order used as semantic order.
 */
class FrozenMetamodelSequenceViolationException(
    val imageId: Any,
    val sequenceTable: String,
    val referenceSummary: String?,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel deterministic sequence violation: " +
            "image=$imageId, sequence=$sequenceTable, " +
            "reference=$referenceSummary, reason=$reason",
)

/**
 * Thrown when a frozen adapter-neutral record exists but cannot materialize its
 * planning-facing DTO.
 *
 * This differs from FrozenMetamodelIncompleteTableException:
 *
 * - IncompleteTable:
 *   the table entry is missing.
 *
 * - RecordMaterialization:
 *   the table entry exists but cannot produce a valid DTO.
 */
class FrozenMetamodelRecordMaterializationException(
    val imageId: Any,
    val referenceSummary: String,
    val recordTable: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel record materialization failed: " +
            "image=$imageId, table=$recordTable, reference=$referenceSummary, reason=$reason",
)

open class FrozenMetamodelIntegrityViolationException(
    val imageId: FrozenMetamodelImageId,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image integrity violation: imageId=$imageId, reason=$reason",
)

/**
 * Raised when a frozen deterministic sequence is read outside its valid
 * ordinal range.
 *
 * This exception represents an indexed-access contract violation on an already
 * published frozen sequence.
 *
 * It is intentionally separate from FrozenMetamodelSequenceViolationException.
 *
 * Difference:
 *
 * - FrozenMetamodelSequenceViolationException:
 *   sequence publication failed because input material violated deterministic
 *   frozen sequence law.
 *
 * - FrozenMetamodelSequenceIndexOutOfBoundsException:
 *   an already-published frozen sequence was accessed with an invalid index.
 *
 * This exception is still a metamodel-domain exception because frozen sequences
 * are domain-owned deterministic structures, not ordinary Kotlin collections.
 */
class FrozenMetamodelSequenceIndexOutOfBoundsException(
    val imageId: FrozenMetamodelImageId,
    val sequenceTable: String,
    val index: Int,
    val size: Int,
) : MetamodelException(
    "Frozen metamodel sequence index out of bounds: " +
            "imageId=${imageId.renderSummary()}, " +
            "sequenceTable=$sequenceTable, " +
            "index=$index, " +
            "size=$size",
)
