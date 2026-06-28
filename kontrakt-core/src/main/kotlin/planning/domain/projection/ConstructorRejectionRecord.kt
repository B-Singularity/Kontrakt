package planning.domain.projection

import stage.input.material.VisibilityKind
import planning.domain.exception.ActiveMemberProjectionException
import stage.input.material.DeclarationOrdinal
import stage.input.material.MemberOrigin

/**
 * Structured, deterministically sortable rejection evidence for constructor selection.
 *
 * This is diagnostic evidence.
 * It is not a semantic ordering key and must not be used as a hidden selection tie-break.
 */
class ConstructorRejectionRecord private constructor(
    val ownerTypeFqcn: String,
    val constructorSignature: String,
    val constructorSignatureNormalizationVersion: Long,
    val declarationOrdinal: DeclarationOrdinal,
    val visibility: VisibilityKind,
    val origin: MemberOrigin,
    val reason: ConstructorRejectionReason,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            constructorSignature: String,
            constructorSignatureNormalizationVersion: Long,
            declarationOrdinal: DeclarationOrdinal,
            visibility: VisibilityKind,
            origin: MemberOrigin,
            reason: ConstructorRejectionReason,
        ): ConstructorRejectionRecord {
            validatePlanningCanonicalComponent("ConstructorRejectionRecord.ownerTypeFqcn", ownerTypeFqcn)
            validatePlanningCanonicalComponent("ConstructorRejectionRecord.constructorSignature", constructorSignature)

            if (constructorSignatureNormalizationVersion < 0L) {
                throw ActiveMemberProjectionException(
                    "ConstructorRejectionRecord.constructorSignatureNormalizationVersion must be >= 0: " +
                            constructorSignatureNormalizationVersion,
                )
            }

            return ConstructorRejectionRecord(
                ownerTypeFqcn = ownerTypeFqcn,
                constructorSignature = constructorSignature,
                constructorSignatureNormalizationVersion = constructorSignatureNormalizationVersion,
                declarationOrdinal = declarationOrdinal,
                visibility = visibility,
                origin = origin,
                reason = reason,
            )
        }
    }
}
