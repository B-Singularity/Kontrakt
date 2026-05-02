package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.DiagnosticBudget

/**
 * Deterministically ordered use-site annotation collection.
 *
 * This is not:
 *
 * - a mutable annotation list;
 * - a reflection annotation array;
 * - a KSP annotation list;
 * - a source-order preserving bag;
 * - a planning-domain object;
 * - or a serialized annotation payload.
 *
 * Dependency law:
 *
 * The metamodel domain must not depend on planning.domain.*.
 *
 * Ordering law:
 *
 * This collection delegates all annotation ordering to
 * AnnotationDescriptor.compareTo(...).
 *
 * That means ordering is defined by:
 *
 * 1. AnnotationQualifiedName order;
 * 2. AnnotationValueMap structural order;
 * 3. AnnotationValue structural order.
 *
 * Do not reimplement annotation comparison here. If descriptor ordering changes,
 * this collection must automatically follow it.
 *
 * Multiplicity law:
 *
 * This collection does not reject repeated descriptors.
 *
 * Repeatable annotations and repeated equivalent annotation descriptors are a
 * language/schema concern. If a specific annotation type is not repeatable, that
 * must be enforced by an annotation schema validator, not by this generic
 * ordered collection.
 *
 * Resource law:
 *
 * The number of annotations is capped. This prevents malicious or broken
 * adapters from creating huge annotation arrays and forcing O(N log N) sorting
 * work inside the metamodel boundary.
 *
 * Storage law:
 *
 * Internally this VO stores annotations in a private Array.
 *
 * This is intentional for a compiler-like sorted table:
 *
 * - compact;
 * - cache-friendly;
 * - efficient to sort in place during issuance;
 * - efficient to index and iterate;
 * - free of List wrapper indirection on the hot path.
 *
 * Immutability is guaranteed by:
 *
 * - defensive input copy via toTypedArray();
 * - in-place sorting only before publication;
 * - private array storage;
 * - read-only public API.
 *
 * This VO does not attempt to defend against privileged reflective mutation
 * inside the same JVM process. Reflection mutation is outside the metamodel
 * domain threat model.
 *
 * Diagnostic law:
 *
 * toString() is intentionally compact.
 * renderDiagnostic() provides bounded human-readable details.
 *
 * Hash law:
 *
 * hashCode() is for in-memory equality collections only.
 * It must not be used as a canonical fingerprint, persisted key, route key, or
 * cross-runtime protocol hash.
 */
class OrderedUseSiteAnnotations private constructor(
    private val annotations: Array<AnnotationDescriptor>,
) : AbstractList<AnnotationDescriptor>() {
    override val size: Int
        get() = annotations.size

    override fun get(index: Int): AnnotationDescriptor = annotations[index]

    /**
     * Maximum nested annotation value depth across all descriptors.
     *
     * This is useful for higher-level defensive assertions without requiring
     * callers to traverse annotation values recursively.
     */
    val maxAnnotationValueNestingDepth: Int =
        computeMaxAnnotationValueNestingDepth(
            annotations = annotations,
        )

    fun renderSummary(): String = "OrderedUseSiteAnnotations(size=${annotations.size})"

    /**
     * Bounded diagnostic rendering.
     *
     * This is not canonical encoding.
     * This is not a cache key.
     * This is not fingerprint material.
     */
    fun renderDiagnostic(): String {
        if (annotations.isEmpty()) {
            return "OrderedUseSiteAnnotations()"
        }

        val budget =
            DiagnosticBudget(
                remaining = MAX_RENDERED_DIAGNOSTIC_CHARS,
            )

        val builder = StringBuilder()
        budget.append(builder, "OrderedUseSiteAnnotations(")

        val limit =
            if (annotations.size < MAX_RENDERED_ANNOTATIONS) {
                annotations.size
            } else {
                MAX_RENDERED_ANNOTATIONS
            }

        var index = 0
        while (index < limit && budget.hasRemaining()) {
            if (index > 0) {
                budget.append(builder, ",")
            }

            budget.append(
                builder = builder,
                value = annotations[index].renderDiagnostic(),
            )

            index += 1
        }

        if (annotations.size > limit) {
            if (limit > 0) {
                budget.append(builder, ",")
            }

            budget.append(builder, DiagnosticBudget.TRUNCATION_MARKER)
        }

        budget.append(builder, ")")
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderedUseSiteAnnotations) return false

        return annotations.contentEquals(other.annotations)
    }

    override fun hashCode(): Int = annotations.contentHashCode()

    override fun toString(): String = renderSummary()

    companion object {
        /**
         * Protocol cap for annotations attached to one use-site.
         *
         * Real-world use-sites normally have very few annotations. 1,024 is
         * intentionally generous but still prevents unbounded O(N log N) sorting
         * and diagnostic expansion.
         */
        const val MAX_USE_SITE_ANNOTATIONS: Int = 1_024

        private const val MAX_RENDERED_ANNOTATIONS: Int = 16
        private const val MAX_RENDERED_DIAGNOSTIC_CHARS: Int = 4_096

        private val EMPTY = OrderedUseSiteAnnotations(emptyArray())

        private val ORDER: Comparator<AnnotationDescriptor> =
            Comparator { left, right ->
                left.compareTo(right)
            }

        @JvmStatic
        fun empty(): OrderedUseSiteAnnotations = EMPTY

        @JvmStatic
        fun issue(annotations: Collection<AnnotationDescriptor>): OrderedUseSiteAnnotations {
            if (annotations.isEmpty()) {
                return EMPTY
            }

            if (annotations.size > MAX_USE_SITE_ANNOTATIONS) {
                throw MetamodelFactContractViolationException(
                    "OrderedUseSiteAnnotations.annotations exceeds protocol cap=$MAX_USE_SITE_ANNOTATIONS.",
                )
            }

            /*
             * This is the only required copy.
             *
             * Collection input may be externally mutable, so we must detach from
             * it. The resulting array is local to this method, sorted in place,
             * validated, and then transferred into the VO without another copy.
             */
            val buffer = annotations.toTypedArray()
            buffer.sortWith(ORDER)

            return OrderedUseSiteAnnotations(buffer)
        }

        private fun computeMaxAnnotationValueNestingDepth(annotations: Array<AnnotationDescriptor>): Int {
            var maxDepth = 0
            var index = 0

            while (index < annotations.size) {
                val depth = annotations[index].annotationValueNestingDepth

                if (depth > maxDepth) {
                    maxDepth = depth
                }

                index += 1
            }

            return maxDepth
        }
    }
}
