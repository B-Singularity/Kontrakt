package stage.lowering.material.projection

import stage.canonicalization.material.representation.TypeReference
import stage.input.presentation.raw.DeclarationOrdinal
import stage.input.presentation.raw.NullabilityKind
import stage.lowering.diagnostics.ActiveMemberProjectionException

/**
 * Planner-facing projected active member.
 *
 * This is the semantic bridge between raw metamodel facts and ordered traversal input.
 *
 * It unifies:
 * - selected constructor parameters,
 * - eligible properties.
 *
 * It does not represent:
 * - raw constructor candidate,
 * - raw property candidate,
 * - adapter discovery order,
 * - cache/interner identity.
 */
class ProjectedActiveMember private constructor(
    val ownerTypeFqcn: String,
    val memberKind: MemberKind,
    val name: String,
    val typeReference: TypeReference,
    val typeSignatureNormalizationVersion: Long,
    val declarationOrdinal: DeclarationOrdinal,
    val nullability: NullabilityKind,
    val sourceRef: ProjectionSourceRef,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            memberKind: MemberKind,
            name: String,
            typeReference: TypeReference,
            typeSignatureNormalizationVersion: Long,
            declarationOrdinal: DeclarationOrdinal,
            nullability: NullabilityKind,
            sourceRef: ProjectionSourceRef,
        ): ProjectedActiveMember {
            validatePlanningCanonicalComponent("ProjectedActiveMember.ownerTypeFqcn", ownerTypeFqcn)
            validatePlanningCanonicalComponent("ProjectedActiveMember.name", name)

            if (typeSignatureNormalizationVersion < 0L) {
                throw ActiveMemberProjectionException(
                    "ProjectedActiveMember.typeSignatureNormalizationVersion must be >= 0: " +
                            typeSignatureNormalizationVersion,
                )
            }

            return ProjectedActiveMember(
                ownerTypeFqcn = ownerTypeFqcn,
                memberKind = memberKind,
                name = name,
                typeReference = typeReference,
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
                declarationOrdinal = declarationOrdinal,
                nullability = nullability,
                sourceRef = sourceRef,
            )
        }
    }
}
