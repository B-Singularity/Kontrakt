package metamodel.domain.frozen.record

import metamodel.domain.vo.AnnotationDescriptor

/**
 * Frozen annotation record.
 *
 * AnnotationDescriptor is already metamodel-domain material and must not carry
 * backend annotation handles.
 */
class FrozenAnnotationRecord private constructor(
    val key: FrozenAnnotationRecordKey,
    val descriptor: AnnotationDescriptor,
) {
    companion object {
        @JvmStatic
        fun issue(
            key: FrozenAnnotationRecordKey,
            descriptor: AnnotationDescriptor,
        ): FrozenAnnotationRecord {
            return FrozenAnnotationRecord(
                key = key,
                descriptor = descriptor,
            )
        }
    }
}