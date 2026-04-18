package metamodel.domain.dto

import metamodel.domain.exception.MetamodelException
import metamodel.domain.vo.TypeReference
import java.util.Collections

/**
 * Immutable structural member fact consumed by the planning core.
 *
 * The planner uses these facts for:
 * - reality defense
 * - deterministic ordering
 * - edge lowering
 *
 * This object is intentionally a plain class with a private constructor.
 */
class MemberFact private constructor(
    val ownerTypeFqcn: String,
    val name: String,
    val typeReference: TypeReference,
    val declarationIndex: Int,
    val isNullable: Boolean,
    val visibility: String,
    val origin: MemberOrigin,
    val typeSignatureNormalizationVersion: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            ownerTypeFqcn: String,
            name: String,
            typeReference: TypeReference,
            declarationIndex: Int,
            isNullable: Boolean,
            visibility: String,
            origin: MemberOrigin,
            typeSignatureNormalizationVersion: Long,
        ): MemberFact {
            if (ownerTypeFqcn.isBlank()) {
                throw MetamodelException("MemberFact.ownerTypeFqcn must not be blank.")
            }
            if (name.isBlank()) {
                throw MetamodelException("MemberFact.name must not be blank.")
            }
            if (declarationIndex < 0) {
                throw MetamodelException("MemberFact.declarationIndex must be >= 0: $declarationIndex")
            }
            if (visibility.isBlank()) {
                throw MetamodelException("MemberFact.visibility must not be blank.")
            }
            if (typeSignatureNormalizationVersion < 0L) {
                throw MetamodelException(
                    "MemberFact.typeSignatureNormalizationVersion must be >= 0: $typeSignatureNormalizationVersion"
                )
            }

            return MemberFact(
                ownerTypeFqcn = ownerTypeFqcn,
                name = name,
                typeReference = typeReference,
                declarationIndex = declarationIndex,
                isNullable = isNullable,
                visibility = visibility,
                origin = origin,
                typeSignatureNormalizationVersion = typeSignatureNormalizationVersion,
            )
        }
    }
}

/**
 * Immutable snapshot of a type's structural facts.
 *
 * The domain core MUST consume DTOs like this instead of reflection / bytecode APIs directly.
 */
class TypeFactsDTO private constructor(
    val nodeIdentity64: Long,
    val ownerTypeFqcn: String,
    private val _members: List<MemberFact>,
    val normalizationVersion: Long,
) {
    val members: List<MemberFact> = _members

    companion object {
        @JvmStatic
        fun issue(
            nodeIdentity64: Long,
            ownerTypeFqcn: String,
            members: Collection<MemberFact>,
            normalizationVersion: Long,
        ): TypeFactsDTO {
            if (ownerTypeFqcn.isBlank()) {
                throw MetamodelException("TypeFactsDTO.ownerTypeFqcn must not be blank.")
            }
            if (normalizationVersion < 0L) {
                throw MetamodelException(
                    "TypeFactsDTO.normalizationVersion must be >= 0: $normalizationVersion"
                )
            }

            val copied = Collections.unmodifiableList(ArrayList(members))
            return TypeFactsDTO(
                nodeIdentity64 = nodeIdentity64,
                ownerTypeFqcn = ownerTypeFqcn,
                _members = copied,
                normalizationVersion = normalizationVersion,
            )
        }
    }
}