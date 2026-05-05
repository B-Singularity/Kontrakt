package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelAdapterAssemblyException
import metamodel.domain.service.CanonicalTypeReferenceIssuer
import metamodel.domain.service.TypeIdentity64Deriver

/**
 * Reflection metamodel adapter composition root.
 *
 * Visibility law:
 *
 * This assembler is intentionally `internal`.
 *
 * It is infrastructure wiring, not public domain API. External planning code
 * should receive only planning-facing ports from [ReflectionMetamodelAdapterBundle]:
 *
 * - TypeShapeProvider
 * - TypeCycleIdentityProvider
 * - RawTypeFactsProvider
 *
 * The domain must not depend on this assembler or on reflection-specific
 * implementation details.
 *
 * Responsibility:
 *
 * This object wires already-pinned authorities into reflection adapter
 * components.
 *
 * It does not:
 *
 * - issue TypeReference directly;
 * - call CanonicalTypeText.ratify(...);
 * - derive canonical type identity;
 * - resolve runtime policy;
 * - allocate PlannerSession;
 * - own cache/interner behavior;
 * - own lifecycle after assembly.
 *
 * Lifecycle:
 *
 * The returned [ReflectionMetamodelAdapterBundle] owns adapter-local lifecycle
 * and implements AutoCloseable. The assembler itself owns no resources.
 *
 * Compiler-style wiring:
 *
 * ```text
 * CanonicalTypeReferenceIssuer
 * + TypeIdentity64Deriver
 * + ReflectionTypeShapeRatificationFingerprintProvider
 * + ReflectionCycleSignatureProvider
 *
 * -> ReflectionTypeHandleRegistry
 * -> ReflectionTypeIdentityMaterialRenderer
 * -> ReflectionTypeReferenceBridge
 * -> ReflectionTypeShapeProvider
 * -> ReflectionTypeCycleIdentityProvider
 * -> ReflectionRawTypeFactsProvider
 * -> ReflectionMetamodelAdapterBundle
 * ```
 *
 * V2/KSP migration note:
 *
 * This composition root should later be mirrored by a KSP-backed assembler:
 *
 * ```text
 * KspMetamodelAdapterAssembler
 * ```
 *
 * The shared contract must remain the planning-facing outbound ports, not the
 * reflection-specific classes. If KSP requires different backend handles, those
 * handles must stay behind the KSP adapter boundary exactly as KType stays
 * behind the reflection boundary here.
 */
internal object ReflectionMetamodelAdapterAssembler {
    @JvmStatic
    fun assemble(
        typeReferenceIssuer: CanonicalTypeReferenceIssuer,
        typeIdentity64Deriver: TypeIdentity64Deriver,
        fingerprintProvider: ReflectionTypeShapeRatificationFingerprintProvider,
        cycleSignatureProvider: ReflectionCycleSignatureProvider,
        classifierId: String,
        classifierVersion: String,
        typeSignatureNormalizationVersion: Long,
    ): ReflectionMetamodelAdapterBundle {
        validateAssemblyInputs(
            typeIdentity64Deriver = typeIdentity64Deriver,
            classifierId = classifierId,
            classifierVersion = classifierVersion,
            typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
        )

        return try {
            assembleVerified(
                typeReferenceIssuer = typeReferenceIssuer,
                typeIdentity64Deriver = typeIdentity64Deriver,
                fingerprintProvider = fingerprintProvider,
                cycleSignatureProvider = cycleSignatureProvider,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )
        } catch (e: MetamodelAdapterAssemblyException) {
            throw e
        } catch (e: RuntimeException) {
            throw MetamodelAdapterAssemblyException(
                "Reflection metamodel adapter assembly failed. " +
                        "blame=reflection-adapter-composition, " +
                        "classifierId=$classifierId, " +
                        "classifierVersion=$classifierVersion, " +
                        "typeIdentityAlgorithmId=${safeAlgorithmId(typeIdentity64Deriver)}, " +
                        "typeIdentityAlgorithmVersion=${safeAlgorithmVersion(typeIdentity64Deriver)}, " +
                        "typeSignatureNormalizationVersion=$typeSignatureNormalizationVersion, " +
                        "cause=${e::class.qualifiedName}: ${e.message}",
            )
        }
    }

    private fun assembleVerified(
        typeReferenceIssuer: CanonicalTypeReferenceIssuer,
        typeIdentity64Deriver: TypeIdentity64Deriver,
        fingerprintProvider: ReflectionTypeShapeRatificationFingerprintProvider,
        cycleSignatureProvider: ReflectionCycleSignatureProvider,
        classifierId: String,
        classifierVersion: String,
        typeSignatureNormalizationVersion: Long,
    ): ReflectionMetamodelAdapterBundle {
        val typeHandleRegistry =
            ReflectionTypeHandleRegistry.issue()

        val renderer =
            ReflectionTypeIdentityMaterialRenderer.issue(
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )

        val bridge =
            ReflectionTypeReferenceBridge.issue(
                renderer = renderer,
                typeReferenceIssuer = typeReferenceIssuer,
                fingerprintProvider = fingerprintProvider,
                typeHandleRegistry = typeHandleRegistry,
                classifierId = classifierId,
                classifierVersion = classifierVersion,
            )

        val typeShapeProvider =
            ReflectionTypeShapeProvider.issue(
                typeReferenceBridge = bridge,
                typeHandleRegistry = typeHandleRegistry,
            )

        val typeCycleIdentityProvider =
            ReflectionTypeCycleIdentityProvider.issue(
                typeIdentity64Deriver = typeIdentity64Deriver,
                cycleSignatureProvider = cycleSignatureProvider,
            )

        val rawTypeFactsProvider =
            ReflectionRawTypeFactsProvider.issue(
                typeReferenceBridge = bridge,
                typeIdentity64Deriver = typeIdentity64Deriver,
                typeHandleRegistry = typeHandleRegistry,
            )

        return ReflectionMetamodelAdapterBundle.issue(
            typeHandleRegistry = typeHandleRegistry,
            typeReferenceBridge = bridge,
            typeShapeProvider = typeShapeProvider,
            typeCycleIdentityProvider = typeCycleIdentityProvider,
            rawTypeFactsProvider = rawTypeFactsProvider,
        )
    }

    private fun validateAssemblyInputs(
        typeIdentity64Deriver: TypeIdentity64Deriver,
        classifierId: String,
        classifierVersion: String,
        typeSignatureNormalizationVersion: Long,
    ) {
        ReflectionAdapterProtocolTokenLaw.requireAssemblyProtocolIdToken(
            field = "classifierId",
            value = classifierId,
        )

        ReflectionAdapterProtocolTokenLaw.requireAssemblyProtocolIdToken(
            field = "classifierVersion",
            value = classifierVersion,
        )

        ReflectionAdapterProtocolTokenLaw.requireAssemblyProtocolIdToken(
            field = "typeIdentityAlgorithmId",
            value = typeIdentity64Deriver.identityAlgorithmId,
        )

        if (typeIdentity64Deriver.identityAlgorithmVersion < 0L) {
            throw MetamodelAdapterAssemblyException(
                "Reflection metamodel adapter assembly rejected invalid type identity algorithm version. " +
                        "blame=typeIdentity64Deriver, " +
                        "algorithmId=${typeIdentity64Deriver.identityAlgorithmId}, " +
                        "algorithmVersion=${typeIdentity64Deriver.identityAlgorithmVersion}",
            )
        }

        if (typeSignatureNormalizationVersion < 0L) {
            throw MetamodelAdapterAssemblyException(
                "Reflection metamodel adapter assembly rejected invalid type signature normalization version. " +
                        "blame=reflection-renderer-config, " +
                        "typeSignatureNormalizationVersion=$typeSignatureNormalizationVersion",
            )
        }
    }


    private fun safeAlgorithmId(
        deriver: TypeIdentity64Deriver,
    ): String {
        return try {
            deriver.identityAlgorithmId
        } catch (_: RuntimeException) {
            "<unavailable>"
        }
    }

    private fun safeAlgorithmVersion(
        deriver: TypeIdentity64Deriver,
    ): String {
        return try {
            deriver.identityAlgorithmVersion.toString()
        } catch (_: RuntimeException) {
            "<unavailable>"
        }
    }
    
}