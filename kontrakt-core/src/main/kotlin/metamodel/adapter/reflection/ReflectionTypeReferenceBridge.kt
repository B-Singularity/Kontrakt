package metamodel.adapter.reflection

import metamodel.domain.service.CanonicalTypeReferenceIssuer
import metamodel.domain.service.CanonicalTypeReferenceMaterial
import metamodel.domain.vo.TypeReference
import kotlin.reflect.KType

/**
 * Reflection adapter wiring bridge for TypeReference issuance.
 *
 * This bridge is not canonical authority.
 * This bridge is not a TypeReference factory.
 * This bridge is not a NormalizationEngine.
 * This bridge is not an interner or cache.
 *
 * It only connects reflection-local rendering to the metamodel-domain issuance
 * service.
 *
 * Authority split:
 *
 * ```text
 * KType
 * -> ReflectionTypeIdentityMaterialRenderer.render(...)
 * -> ReflectionTypeShapeRatificationFingerprintProvider.fingerprintFor(...)
 * -> CanonicalTypeReferenceMaterial.issue(...)
 * -> CanonicalTypeReferenceIssuer.issue(...)
 * -> ReflectionTypeHandleRegistry.bind(...)
 * -> TypeReference
 * ```
 *
 * The bridge must not:
 *
 * - call TypeReference.issue(...) directly;
 * - call CanonicalTypeText.ratify(...) directly;
 * - call CanonicalTypeId / TypeCycleKey / CanonicalTypeSignature factories;
 * - become a ReflectionTypeReferenceIssuer;
 * - own cache/interner semantics;
 * - silently return a TypeReference that is not bound to its reflection handle.
 *
 * Consistency rule:
 *
 * [issueReference] returns only after the adapter-local handle registry has
 * accepted the binding. If binding fails, the TypeReference is not returned to
 * the caller. This prevents planner-visible orphan references.
 */
class ReflectionTypeReferenceBridge private constructor(
    private val renderer: ReflectionTypeIdentityMaterialRenderer,
    private val typeReferenceIssuer: CanonicalTypeReferenceIssuer,
    private val fingerprintProvider: ReflectionTypeShapeRatificationFingerprintProvider,
    private val typeHandleRegistry: ReflectionTypeHandleRegistry,
    private val classifierId: String,
    private val classifierVersion: String,
) {
    val typeSignatureNormalizationVersion: Long
        get() = renderer.typeSignatureNormalizationVersion

    fun issueReference(
        type: KType,
    ): TypeReference {
        val rendered = renderer.render(type)

        val fingerprint =
            fingerprintProvider.fingerprintFor(
                idText = rendered.idText,
                shapeSummary = rendered.shapeSummary,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
            )

        val material =
            CanonicalTypeReferenceMaterial.issue(
                idText = rendered.idText,
                cycleText = rendered.cycleText,
                signatureText = rendered.signatureText,
                shapeSummary = rendered.shapeSummary,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
                ratificationFingerprint = fingerprint,
                useSiteAnnotations = rendered.useSiteAnnotations,
                typeNestingDepth = rendered.typeNestingDepth,
            )

        val reference = typeReferenceIssuer.issue(material)

        /*
         * Important:
         *
         * The TypeReference is domain-valid after issuance, but it must not be
         * returned from this reflection bridge unless the adapter-local handle
         * binding has also completed.
         *
         * This does not make the adapter a canonical authority. It only makes
         * the adapter-local sidecar registry consistent from the caller's point
         * of view.
         */
        bindIssuedReference(
            reference = reference,
            type = type,
        )

        return reference
    }

    private fun bindIssuedReference(
        reference: TypeReference,
        type: KType,
    ) {
        typeHandleRegistry.bindOrVerify(
            reference = reference,
            kType = type,
        )
    }

    companion object {
        @JvmStatic
        fun issue(
            renderer: ReflectionTypeIdentityMaterialRenderer,
            typeReferenceIssuer: CanonicalTypeReferenceIssuer,
            fingerprintProvider: ReflectionTypeShapeRatificationFingerprintProvider,
            typeHandleRegistry: ReflectionTypeHandleRegistry,
            classifierId: String,
            classifierVersion: String,
        ): ReflectionTypeReferenceBridge {
            ReflectionAdapterProtocolTokenLaw.requireBridgeProtocolIdToken(
                field = "classifierId",
                value = classifierId,
            )

            ReflectionAdapterProtocolTokenLaw.requireBridgeProtocolIdToken(
                field = "classifierVersion",
                value = classifierVersion,
            )

            return ReflectionTypeReferenceBridge(
                renderer = renderer,
                typeReferenceIssuer = typeReferenceIssuer,
                fingerprintProvider = fingerprintProvider,
                typeHandleRegistry = typeHandleRegistry,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
            )
        }
    }

}