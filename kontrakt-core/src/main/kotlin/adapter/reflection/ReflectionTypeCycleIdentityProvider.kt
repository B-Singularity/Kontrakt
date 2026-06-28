package adapter.reflection

import metamodel.domain.service.TypeIdentity64Deriver
import metamodel.domain.vo.TypeReference
import planning.domain.expansion.TypeCycleIdentity
import planning.domain.port.outgoing.TypeCycleIdentityProvider

/**
 * Reflection adapter implementation of TypeCycleIdentityProvider.
 *
 * This provider is intentionally reflection-light.
 *
 * It does not use KType for identity derivation.
 * It does not enumerate constructors.
 * It does not enumerate properties.
 * It does not project active members.
 * It does not order members.
 *
 * Cycle identity is derived from an already-ratified TypeReference.
 *
 * This follows the ADR-0037 identity-first / fact-lazy pipeline:
 *
 * ```text
 * TypeReference
 * -> TypeShapeProvider.resolveTypeShape(reference)
 * -> TypeCycleIdentityProvider.resolveCycleIdentity(reference)
 * -> PlannerSession.enterOrDetectCycle(...)
 * -> if cycle hit: no RawTypeFactsProvider call
 * -> if cycle miss: RawTypeFactsProvider.resolveRawFacts(reference)
 * ```
 *
 * The returned TypeCycleIdentity contains:
 *
 * - identityBits64: primitive routing/probing identity;
 * - canonicalSignature: exact equality authority;
 * - identityAlgorithmId/version: pinned derivation law identity.
 *
 * identityBits64 is not the only equality authority. Planning hot paths must
 * route by bits and verify by canonical signature, consistent with the L1
 * two-phase cycle identity law.
 */
class ReflectionTypeCycleIdentityProvider private constructor(
    private val typeIdentity64Deriver: TypeIdentity64Deriver,
    private val cycleSignatureProvider: ReflectionCycleSignatureProvider,
) : TypeCycleIdentityProvider {
    override val identityAlgorithmId: String
        get() = typeIdentity64Deriver.identityAlgorithmId

    override val identityAlgorithmVersion: Long
        get() = typeIdentity64Deriver.identityAlgorithmVersion

    override fun resolveCycleIdentity(
        reference: TypeReference,
    ): TypeCycleIdentity {
        return TypeCycleIdentity.issue(
            subject = reference,
            identityBits64 = typeIdentity64Deriver.deriveIdentity64(reference),
            canonicalSignature =
                cycleSignatureProvider.deriveCycleSignature(
                    reference = reference,
                ),
            identityAlgorithmId = identityAlgorithmId,
            identityAlgorithmVersion = identityAlgorithmVersion,
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            typeIdentity64Deriver: TypeIdentity64Deriver,
            cycleSignatureProvider: ReflectionCycleSignatureProvider,
        ): ReflectionTypeCycleIdentityProvider {
            return ReflectionTypeCycleIdentityProvider(
                typeIdentity64Deriver = typeIdentity64Deriver,
                cycleSignatureProvider = cycleSignatureProvider,
            )
        }
    }
}
