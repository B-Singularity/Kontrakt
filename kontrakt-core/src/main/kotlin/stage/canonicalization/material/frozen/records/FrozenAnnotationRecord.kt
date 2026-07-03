package stage.canonicalization.material.frozen.records

import stage.admission.diagnostics.evidence.MetamodelFactContractViolationException
import stage.canonicalization.material.presentation.AnnotationDescriptor

/**
 * Frozen annotation records.
 *
 * This is the Level 1 frozen object-records representation for one annotation.
 *
 * It is intentionally still an object records:
 *
 * - not a primitive slab row;
 * - not a bit-packed metadata segment;
 * - not a canonical encoded payload;
 * - not a backend annotation handle.
 *
 * Later freeze-memory work may lower annotation records into ordinal-indexed
 * slabs, but this foundation cut keeps the semantic records explicit so the
 * frozen-records law can be validated before physical layout is compressed.
 *
 * Backend-erasure law:
 *
 * [AnnotationDescriptor] is already metamodel-domain material. It must not carry:
 *
 * - java.lang.annotation.Annotation;
 * - KType;
 * - KClass;
 * - KAnnotatedElement;
 * - KSP KSAnnotation;
 * - KSP KSDeclaration;
 * - bytecode annotation handles;
 * - source AST/PSI handles;
 * - adapter-local registry ids;
 * - closures, suppliers, or lazy delegates that can recover backend handles.
 *
 * This class does not attempt a runtime "is lowered" self-attestation check.
 *
 * Rationale:
 *
 * A boolean-style `isLowered()` check would be weaker than the type boundary and
 * architecture tests. The correct enforcement is:
 *
 * - AnnotationDescriptor remains a closed metamodel-domain VO;
 * - frozen records store AnnotationDescriptor, not backend-native annotation
 *   objects;
 * - architecture tests reject backend handle reachability from frozen records;
 * - later slab lowering removes object-records overhead without changing this
 *   semantic law.
 *
 * Key/descriptor continuity law:
 *
 * The key's annotation qualified name must match the descriptor's qualified
 * name:
 *
 * ```text
 * key.annotationQualifiedName == descriptor.qualifiedName
 * ```
 *
 * This prevents a records from being indexed as annotation A while carrying the
 * descriptor payload for annotation B.
 *
 * The key's annotation TypeReference is not cross-checked here because
 * AnnotationDescriptor currently owns the annotation qualified name and value
 * map, not a TypeReference. A future ADR may ratify a stronger annotation type
 * identity bridge.
 *
 * Equality law:
 *
 * Equality is structural over:
 *
 * - key;
 * - descriptor.
 *
 * This supports deterministic sequence duplicate detection, diagnostics, and
 * local tests. It must not be used as persistent frozen-image identity or L2
 * semantic material.
 *
 * Hash law:
 *
 * hashCode is a transitional in-memory equality-collection companion.
 *
 * It currently composes the existing metamodel VO hashCode surfaces and must
 * not be used as:
 *
 * - canonical fingerprint;
 * - persistent image identity;
 * - route key;
 * - L1/L2 partition key;
 * - PlanCacheKey material;
 * - cross-runtime order digest.
 *
 * The later BLAKE3 / metadata-hash refactoring may replace this transitional
 * hashCode strategy across the metamodel VO family. Do not introduce a local
 * hash family in this records.
 */
class FrozenAnnotationRecord private constructor(
    val key: FrozenAnnotationRecordKey,
    val descriptor: AnnotationDescriptor,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenAnnotationRecord(" +
                "key=${key.renderSummary()}, " +
                "descriptor=${descriptor.renderSummary()}" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenAnnotationRecord) return false

        /*
         * Cheap negative filter only.
         *
         * Structural equality remains explicit below.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return key == other.key &&
                descriptor == other.descriptor
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        fun issue(
            key: FrozenAnnotationRecordKey,
            descriptor: AnnotationDescriptor,
        ): FrozenAnnotationRecord {
            requireKeyDescriptorContinuity(
                key = key,
                descriptor = descriptor,
            )

            return FrozenAnnotationRecord(
                key = key,
                descriptor = descriptor,
                precomputedHashCode = computeHashCode(
                    key = key,
                    descriptor = descriptor,
                ),
            )
        }

        private fun requireKeyDescriptorContinuity(
            key: FrozenAnnotationRecordKey,
            descriptor: AnnotationDescriptor,
        ) {
            if (key.annotationQualifiedName == descriptor.qualifiedName) {
                return
            }

            throw MetamodelFactContractViolationException(
                "FrozenAnnotationRecord key/descriptor mismatch: " +
                        "key.annotationQualifiedName=${key.annotationQualifiedName}, " +
                        "descriptor.qualifiedName=${descriptor.qualifiedName}",
            )
        }

        /**
         * Computes the transitional JVM hashCode companion.
         *
         * This deliberately follows the current metamodel VO family until the
         * later BLAKE3 / metadata-hash refactoring replaces hash policy
         * globally.
         *
         * Do not treat this value as order material.
         */
        private fun computeHashCode(
            key: FrozenAnnotationRecordKey,
            descriptor: AnnotationDescriptor,
        ): Int {
            var result = key.hashCode()
            result = 31 * result + descriptor.hashCode()
            return result
        }
    }
}