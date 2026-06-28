package stage.canonicalization.material.frozen

/**
 * Backend-neutral metadata availability state.
 *
 * Availability is record state, not primary identity-key material.
 *
 * This enum is intentionally not allowed to contain a generic UNKNOWN state.
 *
 * Determinism law:
 *
 * A published FrozenMetamodelImage must not contain generic inconclusive
 * availability. If acquisition cannot classify a metadata surface deterministically,
 * freeze must either:
 *
 * - map it to a more precise state such as ABSENT, UNAVAILABLE_FROM_BACKEND,
 *   REJECTED_UNSAFE, FILTERED_BY_POLICY, TRUNCATED, or ACQUISITION_FAILED;
 * - lower it into a field-specific closed vocabulary whose consuming order
 *   defines deterministic conservative handling;
 * - or fail closed before image publication.
 *
 * Field-specific unknown states may still exist in narrow domain vocabularies
 * such as NullabilityKind.UNKNOWN or DefaultValuePresence.UNKNOWN, but only when
 * their consuming order defines deterministic conservative or fail-closed
 * behavior.
 *
 * State-code law:
 *
 * This enum is only a closed state code.
 *
 * It must not carry:
 *
 * - Throwable;
 * - Exception;
 * - stack trace;
 * - KType;
 * - KClass;
 * - KSType;
 * - KSDeclaration;
 * - bytecode handles;
 * - source AST/PSI handles;
 * - adapter-local registry ids;
 * - classloader-local ids;
 * - closures;
 * - suppliers;
 * - lazy delegates.
 *
 * If a containing record needs diagnostic detail, that detail must be carried
 * as backend-neutral, bounded, sanitized diagnostic material such as:
 *
 * - a closed error code;
 * - a primitive numeric code;
 * - a bounded sanitized String.
 *
 * Diagnostic detail must not participate in TypeReference equality, frozen
 * record key equality, route64, PlanCacheKey, canonical IR lowering, or L2
 * identity material unless a future ADR explicitly ratifies it.
 */
enum class FrozenMetadataAvailability {
    /**
     * The metadata was observed, lowered, and frozen successfully.
     */
    PRESENT,

    /**
     * The backend inspected the metadata surface and determined that the target
     * does not have this metadata.
     *
     * Example:
     * - a specific annotation is not present;
     * - a default value is not present;
     * - a setter is not present;
     * - a backing field is not present.
     *
     * This is not a backend capability boundary.
     * This is not a failed acquisition.
     * This is not missing table coverage.
     */
    ABSENT,

    /**
     * The backend does not expose this metadata surface.
     *
     * Example:
     * - a reflection backend cannot expose a source-only declaration attribute;
     * - a bytecode backend cannot expose a source-only KSP symbol attribute.
     *
     * This is not a failure.
     * This is a backend capability boundary.
     */
    UNAVAILABLE_FROM_BACKEND,

    /**
     * The metadata was rejected because using it would be unsafe or would
     * violate a order/security/sanity law.
     *
     * Example:
     * - polluted JVM-internal classifier material;
     * - malformed annotation payload;
     * - unsafe backend-specific descriptor;
     * - invalid canonical text candidate.
     */
    REJECTED_UNSAFE,

    /**
     * The metadata was intentionally cut by a deterministic truncation law.
     *
     * Example:
     * - depth limit;
     * - active-cycle truncation;
     * - bounded acquisition budget;
     * - framework-defined recursion cutoff.
     *
     * This is not backend unavailability.
     * This is a framework-controlled boundary.
     */
    TRUNCATED,

    /**
     * The metadata was intentionally excluded by a user/framework policy.
     *
     * Example:
     * - private members excluded by visibility policy;
     * - synthetic members excluded by member policy;
     * - annotations excluded by configured acquisition policy;
     * - platform members excluded by scope policy.
     *
     * This is not unsafe.
     * This is out-of-scope by policy.
     */
    FILTERED_BY_POLICY,

    /**
     * The framework attempted to acquire the metadata, but the acquisition
     * operation failed.
     *
     * Example:
     * - reflection access failure;
     * - ClassNotFoundException-like backend failure;
     * - KSP resolver failure;
     * - bytecode parser failure;
     * - source index read failure.
     *
     * This differs from UNAVAILABLE_FROM_BACKEND:
     *
     * - UNAVAILABLE_FROM_BACKEND means the backend normally does not provide the
     *   metadata.
     * - ACQUISITION_FAILED means acquisition was attempted but failed.
     */
    ACQUISITION_FAILED,

}