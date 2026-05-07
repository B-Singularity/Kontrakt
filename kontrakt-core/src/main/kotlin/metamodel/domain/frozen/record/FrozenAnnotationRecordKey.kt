package metamodel.domain.frozen.record

import metamodel.domain.vo.AnnotationQualifiedName
import metamodel.domain.vo.TypeReference

/**
 * Backend-neutral annotation identity key.
 *
 * TODO:
 * Introduce FrozenAnnotationUseSiteTarget / FrozenAnnotationPayload once the
 * annotation frozen-record law is implemented.
 */
class FrozenAnnotationRecordKey private constructor(
    val annotationType: TypeReference,
    val annotationQualifiedName: AnnotationQualifiedName,
    val useSiteTarget: String,
    val canonicalPayloadKey: String,
) {
    companion object {
        @JvmStatic
        fun issue(
            annotationType: TypeReference,
            annotationQualifiedName: AnnotationQualifiedName,
            useSiteTarget: String,
            canonicalPayloadKey: String,
        ): FrozenAnnotationRecordKey {
            return FrozenAnnotationRecordKey(
                annotationType = annotationType,
                annotationQualifiedName = annotationQualifiedName,
                useSiteTarget = useSiteTarget,
                canonicalPayloadKey = canonicalPayloadKey,
            )
        }
    }
}