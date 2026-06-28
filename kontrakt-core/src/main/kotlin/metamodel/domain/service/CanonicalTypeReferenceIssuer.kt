package metamodel.domain.service

import metamodel.domain.port.outgoing.NormalizationEngine
import metamodel.domain.vo.CanonicalTypeSignature
import metamodel.domain.vo.CanonicalTypeText
import metamodel.domain.vo.CanonicalTypeTextInspectionPolicy
import metamodel.domain.vo.TypeReference
import metamodel.domain.vo.TypeShapeRatificationFingerprint
import stage.input.material.TypeShapeSummary
import stage.input.diagnostics.MetamodelFactContractViolationException
import stage.input.material.OrderedUseSiteAnnotations
import stage.input.material.TypeIdentityCoherenceProof

/**
 * Domain service that issues TypeReference from adapter-rendered identity
 * material.
 *
 * This is the canonical TypeReference issuance authority.
 *
 * Adapters must not:
 *
 * - implement TypeReference;
 * - call TypeReference.issue(...) directly;
 * - call CanonicalTypeText.ratify(...) directly as issuance orchestration;
 * - call CanonicalTypeId issuance directly;
 * - call TypeCycleKey.issue(...) directly;
 * - call CanonicalTypeSignature.issue(...) directly;
 * - assemble id/cycle/signature from unrelated sources.
 *
 * Adapter responsibility stops at providing already-lowered candidate material:
 *
 * - id text;
 * - cycle-key text;
 * - signature text;
 * - shape summary observed from backend metadata;
 * - classifier law identity;
 * - ratification fingerprint;
 * - ordered annotation material;
 * - type nesting depth.
 *
 * This service owns the domain issuance bridge:
 *
 * ```text
 * adapter-rendered text
 * -> CanonicalTypeText.ratify(...)
 * -> CanonicalTypeId via TypeShapeIdentityIssuer
 * -> TypeCycleKey via TypeCycleKeyCoherenceScope
 * -> CanonicalTypeSignature
 * -> TypeIdentityCoherenceProof
 * -> TypeReference
 * ```
 *
 * Boundary law:
 *
 * The incoming text values are not trusted domain identity values. They are raw
 * candidate text emitted by a backend adapter after backend-specific spelling
 * lowering.
 *
 * All text must pass through CanonicalTypeText.ratify(...), except when this
 * service can prove that an already-ratified text is being reused under the same
 * inspection context.
 *
 * Coherence law:
 *
 * idText, cycleText, and signatureText are treated as one identity tuple.
 *
 * This service fails closed if:
 *
 * - id and signature are ratified under different inspection contexts;
 * - id and signature receive different shape summaries;
 * - cycle key shape kind drifts away from signature shape kind;
 * - the caller supplies an impossible type nesting depth;
 * - annotation material is unordered or invalid. The annotation object itself
 *   owns that invariant.
 *
 * This service is not:
 *
 * - a cache;
 * - an interner;
 * - an adapter;
 * - a reflection utility;
 * - a KSP utility;
 * - a shape classifier;
 * - a fingerprint deriver.
 *
 * Shape classification and fingerprint derivation are supplied by the caller
 * through already pinned governance / classifier boundaries.
 */
class CanonicalTypeReferenceIssuer private constructor(
    private val normalizationEngine: NormalizationEngine,
    private val idInspectionPolicy: CanonicalTypeTextInspectionPolicy,
    private val cycleInspectionPolicy: CanonicalTypeTextInspectionPolicy,
    private val signatureInspectionPolicy: CanonicalTypeTextInspectionPolicy,
    private val typeShapeIdentityIssuer: TypeShapeIdentityIssuer,
    private val typeCycleKeyCoherenceScope: TypeCycleKeyCoherenceScope,
) {
    fun issue(
        material: CanonicalTypeReferenceMaterial,
    ): TypeReference {
        requireMaterialShape(material)

        val idText =
            ratify(
                rawValue = material.idText,
                inspectionPolicy = idInspectionPolicy,
            )

        val cycleText =
            ratify(
                rawValue = material.cycleText,
                inspectionPolicy = cycleInspectionPolicy,
            )

        val signatureText =
            ratifySignatureText(
                material = material,
                alreadyRatifiedIdText = idText,
            )

        /*
         * id/signature context coherence remains explicit even when
         * ratifySignatureText(...) reuses idText.
         */
        idText.requireSameInspectionContextAs(signatureText)

        val id =
            typeShapeIdentityIssuer.issue(
                text = idText,
                shapeSummary = material.shapeSummary,
                classifierId = material.classifierId,
                classifierVersion = material.classifierVersion,
                ratificationFingerprint = material.ratificationFingerprint,
            )

        val cycleKey =
            typeCycleKeyCoherenceScope.issue(
                value = cycleText.value,
                shapeSummary = material.shapeSummary,
            )

        val signature =
            CanonicalTypeSignature.issue(
                value = signatureText.value,
                shapeSummary = material.shapeSummary,
            )

        val coherenceProof =
            TypeIdentityCoherenceProof.issueFromFactory(
                proofId = TYPE_REFERENCE_PROOF_ID,
                factoryId = TYPE_REFERENCE_FACTORY_ID,
                factoryVersion = TYPE_REFERENCE_FACTORY_VERSION,
                id = id,
                cycleKey = cycleKey,
                signature = signature,
                useSiteAnnotations = material.useSiteAnnotations,
                typeNestingDepth = material.typeNestingDepth,
            )

        return TypeReference.issue(
            id = id,
            cycleKey = cycleKey,
            signature = signature,
            useSiteAnnotations = material.useSiteAnnotations,
            coherenceProof = coherenceProof,
            typeNestingDepth = material.typeNestingDepth,
        )
    }

    private fun ratify(
        rawValue: CharSequence,
        inspectionPolicy: CanonicalTypeTextInspectionPolicy,
    ): CanonicalTypeText {
        return CanonicalTypeText.ratify(
            rawValue = rawValue,
            normalizationEngine = normalizationEngine,
            inspectionPolicy = inspectionPolicy,
        )
    }

    private fun ratifySignatureText(
        material: CanonicalTypeReferenceMaterial,
        alreadyRatifiedIdText: CanonicalTypeText,
    ): CanonicalTypeText {
        /*
         * Same rendered string is not enough to reuse a ratified text.
         *
         * Reuse is allowed only when this service can prove that the signature
         * text is the same candidate material and the signature inspection law is
         * the same as the id inspection law.
         *
         * If policies differ, signature text must pass through its own
         * ratification boundary. The later requireSameInspectionContextAs(...)
         * call will fail closed if id/signature contexts are incompatible.
         */
        if (
            material.signatureText == material.idText &&
            signatureInspectionPolicy == idInspectionPolicy
        ) {
            return alreadyRatifiedIdText
        }

        return ratify(
            rawValue = material.signatureText,
            inspectionPolicy = signatureInspectionPolicy,
        )
    }

    private fun requireMaterialShape(
        material: CanonicalTypeReferenceMaterial,
    ) {
        requireNonEmpty(
            field = "CanonicalTypeReferenceMaterial.idText",
            value = material.idText,
        )
        requireNonEmpty(
            field = "CanonicalTypeReferenceMaterial.cycleText",
            value = material.cycleText,
        )
        requireNonEmpty(
            field = "CanonicalTypeReferenceMaterial.signatureText",
            value = material.signatureText,
        )
        requireNonEmpty(
            field = "CanonicalTypeReferenceMaterial.classifierId",
            value = material.classifierId,
        )
        requireNonEmpty(
            field = "CanonicalTypeReferenceMaterial.classifierVersion",
            value = material.classifierVersion,
        )

        if (material.typeNestingDepth <= 0) {
            throw MetamodelFactContractViolationException(
                "CanonicalTypeReferenceMaterial.typeNestingDepth must be > 0: " +
                        material.typeNestingDepth,
            )
        }
    }

    private fun requireNonEmpty(
        field: String,
        value: String,
    ) {
        if (value.isEmpty()) {
            throw MetamodelFactContractViolationException(
                "$field must not be empty.",
            )
        }
    }

    companion object {
        private const val TYPE_REFERENCE_FACTORY_ID: String =
            "canonical-type-reference-issuer"

        private const val TYPE_REFERENCE_FACTORY_VERSION: String =
            "v1"

        /*
         * Deliberately compact.
         *
         * proofId is diagnostic/admission material only. It is not the authority
         * over the tuple. Authority comes from:
         *
         * - internal domain factory issuance;
         * - TypeIdentityCoherenceProof.issueFromFactory(...) axis checks;
         * - exact TypeIdentityCoherenceBinding material;
         * - TypeReference.issue(...) calling requireCovers(...).
         *
         * Do not append id/cycle/signature material here. That reintroduces the
         * giant proof-id anti-pattern and turns diagnostic material into a fake
         * equality/canonical surface.
         */
        private const val TYPE_REFERENCE_PROOF_ID: String =
            "type-reference-coherence"

        @JvmStatic
        fun issue(
            normalizationEngine: NormalizationEngine,
            idInspectionPolicy: CanonicalTypeTextInspectionPolicy,
            cycleInspectionPolicy: CanonicalTypeTextInspectionPolicy,
            signatureInspectionPolicy: CanonicalTypeTextInspectionPolicy,
            typeShapeIdentityIssuer: TypeShapeIdentityIssuer,
            typeCycleKeyCoherenceScope: TypeCycleKeyCoherenceScope,
        ): CanonicalTypeReferenceIssuer {
            return CanonicalTypeReferenceIssuer(
                normalizationEngine = normalizationEngine,
                idInspectionPolicy = idInspectionPolicy,
                cycleInspectionPolicy = cycleInspectionPolicy,
                signatureInspectionPolicy = signatureInspectionPolicy,
                typeShapeIdentityIssuer = typeShapeIdentityIssuer,
                typeCycleKeyCoherenceScope = typeCycleKeyCoherenceScope,
            )
        }
    }
}

/**
 * Domain-side issuance material for TypeReference.
 *
 * This is not an adapter DTO, even though adapters usually provide the values.
 *
 * It is the domain service input contract that says:
 *
 * ```text
 * Here is the already-lowered candidate material from a backend adapter;
 * now the domain must ratify and issue the TypeReference.
 * ```
 *
 * The strings are intentionally raw candidate text, not canonical VOs.
 */
class CanonicalTypeReferenceMaterial private constructor(
    val idText: String,
    val cycleText: String,
    val signatureText: String,
    val shapeSummary: TypeShapeSummary,
    val classifierId: String,
    val classifierVersion: String,
    val ratificationFingerprint: TypeShapeRatificationFingerprint,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val typeNestingDepth: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            idText: String,
            cycleText: String,
            signatureText: String,
            shapeSummary: TypeShapeSummary,
            classifierId: String,
            classifierVersion: String,
            ratificationFingerprint: TypeShapeRatificationFingerprint,
            useSiteAnnotations: OrderedUseSiteAnnotations,
            typeNestingDepth: Int,
        ): CanonicalTypeReferenceMaterial {
            return CanonicalTypeReferenceMaterial(
                idText = idText,
                cycleText = cycleText,
                signatureText = signatureText,
                shapeSummary = shapeSummary,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
                ratificationFingerprint = ratificationFingerprint,
                useSiteAnnotations = useSiteAnnotations,
                typeNestingDepth = typeNestingDepth,
            )
        }
    }
}