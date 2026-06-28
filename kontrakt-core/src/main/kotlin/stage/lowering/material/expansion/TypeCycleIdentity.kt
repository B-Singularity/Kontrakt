package stage.lowering.material.expansion

import ir.identity.CanonicalSignature
import stage.lowering.diagnostics.TypeCycleIdentityContractViolationException
import stage.canonicalization.material.TypeReference

/**
 * Minimal identity material for active-cycle detection.
 *
 * This value object is intentionally smaller than RawTypeFactsDTO.
 *
 * It answers:
 *
 *   "Is this canonical type identity already active on the planning stack?"
 *
 * It does not answer:
 *
 *   "Which constructor/properties should be traversed?"
 *
 * DDD role:
 * - immutable domain value object;
 * - no adapter handles;
 * - no reflection/KSP leakage;
 * - no data-class/copy backdoor.
 *
 * Compiler role:
 * - symbol-like identity preflight result;
 * - cheap cycle detection input;
 * - independent from active-member projection/order.
 *
 * Hot-path role:
 * - identityBits64 routes/probes the primitive NodeIdIndexer;
 * - canonicalSignature remains the exact equality authority.
 */
class TypeCycleIdentity private constructor(
    val subject: TypeReference,
    val identityBits64: Long,
    val canonicalSignature: CanonicalSignature,
    val identityAlgorithmId: String,
    val identityAlgorithmVersion: Long,
) {
    companion object {
        /**
         * Reserved sentinel values.
         *
         * These must not appear as real cycle identity bits because primitive
         * hot-path tables frequently use 0 / -1 for empty, deleted, unavailable,
         * or terminal sentinels.
         *
         * If the hash/identity derivation produces one of these values, the
         * identity derivation layer must remap it before issuing TypeCycleIdentity.
         */
        private const val RESERVED_ZERO_IDENTITY: Long = 0L
        private const val RESERVED_MINUS_ONE_IDENTITY: Long = -1L

        @JvmStatic
        fun issue(
            subject: TypeReference,
            identityBits64: Long,
            canonicalSignature: CanonicalSignature,
            identityAlgorithmId: String,
            identityAlgorithmVersion: Long,
        ): TypeCycleIdentity {
            if (identityBits64 == RESERVED_ZERO_IDENTITY) {
                throw TypeCycleIdentityContractViolationException(
                    reason = "identityBits64 must not be reserved zero sentinel.",
                )
            }

            if (identityBits64 == RESERVED_MINUS_ONE_IDENTITY) {
                throw TypeCycleIdentityContractViolationException(
                    reason = "identityBits64 must not be reserved minus-one sentinel.",
                )
            }

            if (identityAlgorithmId.isBlank()) {
                throw TypeCycleIdentityContractViolationException(
                    reason = "identityAlgorithmId must not be blank.",
                )
            }

            if (identityAlgorithmId.contains('|')) {
                throw TypeCycleIdentityContractViolationException(
                    reason = "identityAlgorithmId must not contain reserved delimiter '|': $identityAlgorithmId",
                )
            }

            if (identityAlgorithmVersion < 0L) {
                throw TypeCycleIdentityContractViolationException(
                    reason = "identityAlgorithmVersion must be >= 0: $identityAlgorithmVersion",
                )
            }

            return TypeCycleIdentity(
                subject = subject,
                identityBits64 = identityBits64,
                canonicalSignature = canonicalSignature,
                identityAlgorithmId = identityAlgorithmId,
                identityAlgorithmVersion = identityAlgorithmVersion,
            )
        }
    }
}
