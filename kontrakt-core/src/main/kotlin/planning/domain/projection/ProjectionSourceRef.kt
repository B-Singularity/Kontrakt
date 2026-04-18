package planning.domain.projection

import metamodel.domain.dto.DefaultValuePresence
import metamodel.domain.dto.MemberOrigin
import metamodel.domain.dto.PropertyMutability
import metamodel.domain.dto.PropertyStorageKind
import metamodel.domain.dto.VisibilityKind
import metamodel.domain.vo.DeclarationOrdinal
import planning.domain.exception.ActiveMemberProjectionException

/**
 * Provenance reference for one projected active member.
 *
 * This object exists for diagnostics, dynamic fault attribution, and auditability.
 *
 * It is NOT:
 * - canonical ordering primary tuple,
 * - interner key material,
 * - semantic equality material,
 * - route64 material.
 *
 * The projected member itself carries the semantic fields needed for ordering.
 * The source reference carries enough original fact provenance to explain how the
 * projected member was produced.
 *
 * Source file / line / column anchors are intentionally NOT included in Step 0.
 * That cross-domain diagnostic-provenance system will be introduced later.
 */
sealed interface ProjectionSourceRef {

    class SelectedConstructorParameterRef private constructor(
        val constructorSignature: String,
        val constructorSignatureNormalizationVersion: Long,
        val constructorDeclarationOrdinal: DeclarationOrdinal,
        val parameterIndex: Int,
        val defaultValuePresence: DefaultValuePresence,
        val origin: MemberOrigin,
        val visibility: VisibilityKind,
    ) : ProjectionSourceRef {

        companion object {
            @JvmStatic
            fun issue(
                constructorSignature: String,
                constructorSignatureNormalizationVersion: Long,
                constructorDeclarationOrdinal: DeclarationOrdinal,
                parameterIndex: Int,
                defaultValuePresence: DefaultValuePresence,
                origin: MemberOrigin,
                visibility: VisibilityKind,
            ): SelectedConstructorParameterRef {
                validatePlanningCanonicalComponent(
                    field = "SelectedConstructorParameterRef.constructorSignature",
                    value = constructorSignature,
                )

                if (constructorSignatureNormalizationVersion < 0L) {
                    throw ActiveMemberProjectionException(
                        "SelectedConstructorParameterRef.constructorSignatureNormalizationVersion must be >= 0: " +
                                constructorSignatureNormalizationVersion
                    )
                }

                if (parameterIndex < 0) {
                    throw ActiveMemberProjectionException(
                        "SelectedConstructorParameterRef.parameterIndex must be >= 0: $parameterIndex"
                    )
                }

                return SelectedConstructorParameterRef(
                    constructorSignature = constructorSignature,
                    constructorSignatureNormalizationVersion = constructorSignatureNormalizationVersion,
                    constructorDeclarationOrdinal = constructorDeclarationOrdinal,
                    parameterIndex = parameterIndex,
                    defaultValuePresence = defaultValuePresence,
                    origin = origin,
                    visibility = visibility,
                )
            }
        }
    }

    class EligiblePropertyRef private constructor(
        val propertyDeclarationOrdinal: DeclarationOrdinal,
        val origin: MemberOrigin,
        val declaredVisibility: VisibilityKind,
        val setterVisibility: VisibilityKind?,
        val mutability: PropertyMutability,
        val storageKind: PropertyStorageKind,
    ) : ProjectionSourceRef {

        companion object {
            @JvmStatic
            fun issue(
                propertyDeclarationOrdinal: DeclarationOrdinal,
                origin: MemberOrigin,
                declaredVisibility: VisibilityKind,
                setterVisibility: VisibilityKind?,
                mutability: PropertyMutability,
                storageKind: PropertyStorageKind,
            ): EligiblePropertyRef {
                return EligiblePropertyRef(
                    propertyDeclarationOrdinal = propertyDeclarationOrdinal,
                    origin = origin,
                    declaredVisibility = declaredVisibility,
                    setterVisibility = setterVisibility,
                    mutability = mutability,
                    storageKind = storageKind,
                )
            }
        }
    }
}