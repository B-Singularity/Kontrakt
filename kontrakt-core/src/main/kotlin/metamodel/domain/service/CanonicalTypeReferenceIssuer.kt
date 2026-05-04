package metamodel.domain.service

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.port.outgoing.NormalizationEngine
import metamodel.domain.vo.CanonicalTypeSignature
import metamodel.domain.vo.CanonicalTypeText
import metamodel.domain.vo.CanonicalTypeTextInspectionPolicy
import metamodel.domain.vo.OrderedUseSiteAnnotations
import metamodel.domain.vo.TypeIdentityCoherenceProof
import metamodel.domain.vo.TypeReference
import metamodel.domain.vo.TypeShapeRatificationFingerprint
import metamodel.domain.vo.TypeShapeSummary

/**
 * Domain service that issues [TypeReference] from adapter-rendered identity
 * material.
 *
 * This is the only canonical TypeReference issuance authority in the metamodel
 * domain.
 *
 * Hexagonal boundary:
 *
 * - adapters may observe backend-native type material;
 * - adapters may render deterministic candidate material;
 * - adapters must not issue TypeReference directly;
 * - adapters must not own CanonicalTypeText.ratify(...) orchestration;
 * - adapters must not create adapter-local `*TypeReferenceIssuer` authorities.
 *
 * Authority split:
 *
 * ```text
 * Adapter:
 *   backend type handle -> rendered candidate material
 *
 * Metamodel domain:
 *   rendered candidate material
 *   -> CanonicalTypeText.ratify(...)
 *   -> CanonicalTypeId
 *   -> TypeCycleKey
 *   -> CanonicalTypeSignature
 *   -> TypeIdentityCoherenceProof
 *   -> TypeReference
 * ```
 *
 * This service is not:
 *
 * - a reflection utility;
 * - a KSP utility;
 * - a cache;
 * - an interner;
 * - a fingerprint deriver;
 * - a shape classifier.
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
         * Same rendered text may be reused only if the complete inspection
         * context is equal. String equality alone is not enough because
         * admission depends on policy, Unicode profile, engine provenance, and
         * lexical inspection law.
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
         * TypeIdentityCoherenceProof authority comes from exact tuple binding
         * inside issueFromFactory(...), not from a giant delimiter-joined string.
         *
         * Do not append id/cycle/signature material here. That reintroduces the
         * giant proof-id anti-pattern and turns diagnostic material into a fake
         * equality surface.
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
 * Domain-side input material for [CanonicalTypeReferenceIssuer].
 *
 * This is not canonical identity by itself.
 * This is not an adapter DTO.
 * This is not a cache key.
 *
 * It is the domain service input contract for already-lowered candidate
 * material supplied by a backend adapter.
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