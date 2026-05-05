package metamodel.domain.exception

import exception.KontraktException

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
 * This is adapter-local protocol/infrastructure integrity failure.
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
 * - an algorithm version is outside the ratified protocol range;
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
 * must not silently Unicode-normalize strings behind the protocol's back.
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
