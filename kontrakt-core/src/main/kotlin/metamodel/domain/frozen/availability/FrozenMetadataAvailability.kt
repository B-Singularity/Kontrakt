package metamodel.domain.frozen.availability

/**
 * Backend-neutral metadata availability state.
 *
 * Availability is record state, not primary identity-key material.
 *
 * Do not place availability values in frozen record keys unless a future ADR
 * explicitly ratifies availability-sensitive identity.
 *
 * This enum describes why a piece of frozen metamodel material is present,
 * unavailable, incomplete, excluded, or failed.
 *
 * It must not encode backend-native handles.
 * It must not encode adapter-local registry ids.
 * It must not participate in TypeReference equality.
 */
enum class FrozenMetadataAvailability {
    /**
     * The metadata was observed, lowered, and frozen successfully.
     */
    PRESENT,

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
     * The framework cannot determine whether the metadata is present.
     *
     * This differs from UNAVAILABLE_FROM_BACKEND:
     *
     * - UNAVAILABLE_FROM_BACKEND means the backend capability is known not to
     *   provide the metadata.
     * - UNKNOWN means the acquisition result is inconclusive.
     */
    UNKNOWN,

    /**
     * The metadata was rejected because using it would be unsafe or would
     * violate a protocol/security/sanity law.
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