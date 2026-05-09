package metamodel.domain.service

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.vo.TypeShapeRatification
import metamodel.domain.vo.TypeShapeRatificationFingerprint

/**
 * Scope-level coherence authority.
 *
 * This is not an L2 cache.
 * This is not a performance interner.
 * This is not a runtime handle registry.
 *
 * It exists to enforce the semantic law:
 *
 *     same CanonicalTypeText within one ratified scope
 *     -> exactly one TypeShapeSummary
 *     -> exactly one classifier law
 *     -> exactly one ratification fingerprint
 *
 * The scope owns the admission decision.
 * TypeShapeRatification owns the semantic proof.
 * TypeShapeCoherenceReceipt only proves that the ratification was admitted by
 * this scope.
 *
 * A concrete implementation may be:
 *
 * - resolver-local;
 * - discovery-scope-local;
 * - planning-session-local;
 * - or policy-epoch-local.
 *
 * It must not be a hidden process-global mutable registry.
 *
 * Epoch law:
 *
 * TypeShapeCoherenceScopeEpoch is a value object and only validates local shape.
 * Monotonicity across reused scope ids must be enforced by the scope manager /
 * scope factory / repository boundary that creates scopes.
 *
 * The VO cannot prove global monotonicity by itself.
 */
interface TypeShapeCoherenceScope {
    val scopeId: TypeShapeCoherenceScopeId
    val scopeEpoch: TypeShapeCoherenceScopeEpoch

    fun registerOrVerify(ratification: TypeShapeRatification): TypeShapeCoherenceReceipt
}

/**
 * Stable identifier for one type-shape coherence scope.
 *
 * This is not a cache partition id.
 * This is not a runtime ClassLoader id.
 * This is not a process-global singleton key.
 *
 * It identifies the semantic scope in which the law below is enforced:
 *
 *     same canonical text -> same shape ratification
 */
class TypeShapeCoherenceScopeId private constructor(
    val value: String,
) {
    override fun equals(other: Any?): Boolean = other is TypeShapeCoherenceScopeId && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        private const val MAX_SCOPE_ID_CHARS: Int = 192

        @JvmStatic
        fun issue(value: String): TypeShapeCoherenceScopeId {
            requireToken(
                field = "TypeShapeCoherenceScopeId.value",
                value = value,
                maxChars = MAX_SCOPE_ID_CHARS,
            )

            return TypeShapeCoherenceScopeId(value)
        }
    }
}

/**
 * Monotonic or content-derived epoch for a coherence scope.
 *
 * This value lets the receipt prove that admission happened under a specific
 * immutable scope generation.
 *
 * It must not be wall-clock time.
 * It must not be random.
 * It must not be host-runtime identity.
 *
 * Monotonicity is not enforced here. It belongs to the scope manager that issues
 * scope ids and epochs.
 */
class TypeShapeCoherenceScopeEpoch private constructor(
    val value: Long,
) {
    init {
        if (value < 0L) {
            throw MetamodelFactContractViolationException(
                "TypeShapeCoherenceScopeEpoch.value must be >= 0: $value",
            )
        }
    }

    override fun equals(other: Any?): Boolean = other is TypeShapeCoherenceScopeEpoch && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()

    companion object {
        @JvmStatic
        fun issue(value: Long): TypeShapeCoherenceScopeEpoch = TypeShapeCoherenceScopeEpoch(value)
    }
}

/**
 * Scope-issued admission token.
 *
 * This token is not a type-shape fingerprint.
 * This token is not canonical type identity.
 * This token is not a cache key.
 *
 * It proves that a specific ratification fingerprint was admitted by a specific
 * TypeShapeCoherenceScope at a specific scope epoch.
 *
 * Binding law:
 *
 * A compliant scope implementation must derive this token from at least:
 *
 * - scope id;
 * - scope epoch;
 * - accepted ratification fingerprint;
 * - scope admission law id/version;
 * - optional resolver/discovery snapshot id.
 *
 * The token must not be a process-wide constant.
 * The token must not contain random material.
 * The token must not contain wall-clock time.
 *
 * The domain core does not compute or decode this token. If persisted token
 * replay verification is required, add a dedicated admission-token verifier at
 * the scope implementation boundary.
 */
class TypeShapeCoherenceAdmissionToken private constructor(
    val value: String,
) {
    override fun equals(other: Any?): Boolean = other is TypeShapeCoherenceAdmissionToken && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "<type-shape-coherence-admission-token:redacted>"

    companion object {
        private const val MAX_ADMISSION_TOKEN_CHARS: Int = 512

        @JvmStatic
        fun issue(value: String): TypeShapeCoherenceAdmissionToken {
            requireToken(
                field = "TypeShapeCoherenceAdmissionToken.value",
                value = value,
                maxChars = MAX_ADMISSION_TOKEN_CHARS,
            )

            return TypeShapeCoherenceAdmissionToken(value)
        }
    }
}

/**
 * Receipt proving that TypeShapeCoherenceScope admitted one ratification
 * fingerprint.
 *
 * B-plan design:
 *
 * This receipt intentionally does NOT duplicate:
 *
 * - CanonicalTypeText;
 * - TypeShapeSummary;
 * - classifierId;
 * - classifierVersion.
 *
 * Those are semantic facts and remain owned by TypeShapeRatification.
 *
 * The receipt only stores:
 *
 * - scope id;
 * - scope epoch;
 * - admitted ratification fingerprint;
 * - scope admission token.
 *
 * This keeps the receipt as an admission proof rather than a second semantic
 * fact object.
 */
class TypeShapeCoherenceReceipt private constructor(
    val scopeId: TypeShapeCoherenceScopeId,
    val scopeEpoch: TypeShapeCoherenceScopeEpoch,
    val acceptedRatificationFingerprint: TypeShapeRatificationFingerprint,
    val admissionToken: TypeShapeCoherenceAdmissionToken,
) {
    /**
     * Verifies that this receipt admits the supplied ratification.
     *
     * This does not re-verify the semantic fingerprint cryptographically.
     * That is TypeShapeRatificationVerifier's responsibility before
     * TypeShapeRatification is issued.
     *
     * This method only verifies that the coherence scope admitted this exact
     * ratification fingerprint.
     */
    fun requireAccepts(ratification: TypeShapeRatification) {
        if (acceptedRatificationFingerprint != ratification.ratificationFingerprint) {
            throw MetamodelFactContractViolationException(
                "TypeShapeCoherenceReceipt does not accept ratification fingerprint: " +
                        "scope=$scopeId@$scopeEpoch, " +
                        "expected=${acceptedRatificationFingerprint.redacted()}, " +
                        "actual=${ratification.ratificationFingerprint.redacted()}",
            )
        }
    }

    /**
     * Verifies that this receipt was issued by the expected scope generation.
     *
     * This is useful in the identity issuer path where the caller still has the
     * TypeShapeCoherenceScope that returned the receipt.
     */
    fun requireIssuedBy(scope: TypeShapeCoherenceScope) {
        if (scopeId != scope.scopeId || scopeEpoch != scope.scopeEpoch) {
            throw MetamodelFactContractViolationException(
                "TypeShapeCoherenceReceipt scope mismatch: " +
                        "receipt=$scopeId@$scopeEpoch, " +
                        "scope=${scope.scopeId}@${scope.scopeEpoch}",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeShapeCoherenceReceipt) return false

        return scopeId == other.scopeId &&
                scopeEpoch == other.scopeEpoch &&
                acceptedRatificationFingerprint == other.acceptedRatificationFingerprint &&
                admissionToken == other.admissionToken
    }

    override fun hashCode(): Int {
        var result = scopeId.hashCode()
        result = 31 * result + scopeEpoch.hashCode()
        result = 31 * result + acceptedRatificationFingerprint.hashCode()
        result = 31 * result + admissionToken.hashCode()
        return result
    }

    override fun toString(): String =
        "TypeShapeCoherenceReceipt(" +
                "scope=$scopeId@$scopeEpoch, " +
                "fingerprint=${acceptedRatificationFingerprint.redacted()}, " +
                "admissionToken=$admissionToken" +
                ")"

    companion object {
        /**
         * Issues a scope admission receipt.
         *
         * This should be called by TypeShapeCoherenceScope implementations after
         * they have enforced:
         *
         *     same CanonicalTypeText -> same TypeShapeRatificationFingerprint
         *
         * within the scope.
         */
        @JvmStatic
        fun issue(
            scopeId: TypeShapeCoherenceScopeId,
            scopeEpoch: TypeShapeCoherenceScopeEpoch,
            acceptedRatificationFingerprint: TypeShapeRatificationFingerprint,
            admissionToken: TypeShapeCoherenceAdmissionToken,
        ): TypeShapeCoherenceReceipt =
            TypeShapeCoherenceReceipt(
                scopeId = scopeId,
                scopeEpoch = scopeEpoch,
                acceptedRatificationFingerprint = acceptedRatificationFingerprint,
                admissionToken = admissionToken,
            )
    }
}

private fun requireToken(
    field: String,
    value: String,
    maxChars: Int,
) {
    if (value.isEmpty()) {
        throw MetamodelFactContractViolationException(
            "$field must not be empty.",
        )
    }

    if (value.length > maxChars) {
        throw MetamodelFactContractViolationException(
            "$field exceeds maximum allowed length.",
        )
    }

    if (
        value.indexOf('|') >= 0 ||
        value.indexOf('\u0000') >= 0 ||
        value.indexOf('\n') >= 0 ||
        value.indexOf('\r') >= 0 ||
        value.indexOf('\t') >= 0
    ) {
        throw MetamodelFactContractViolationException(
            "$field contains a reserved order/control character.",
        )
    }
}
