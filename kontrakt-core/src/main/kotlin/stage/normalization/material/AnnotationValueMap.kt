package stage.normalization.material

import governance.budget.material.DiagnosticBudget
import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Deterministic annotation argument map.
 *
 * This is not:
 *
 * - a HashMap;
 * - a LinkedHashMap;
 * - a reflection annotation argument map;
 * - a KSP argument bag;
 * - a serialized annotation payload;
 * - or a planning-domain structure.
 *
 * Dependency law:
 *
 * The metamodel domain must not depend on planning.domain.*.
 *
 * Ordering law:
 *
 * Entries are ordered by AnnotationArgumentName.compareTo(...). Therefore this
 * map follows the argument-name order order:
 *
 * - "value" sorts first;
 * - all other names sort by ASCII code-unit order;
 * - case is significant;
 * - locale, Collator, Unicode collation, and planning-domain text law are not used.
 *
 * Duplicate law:
 *
 * One annotation argument name may appear at most once in this map.
 *
 * If the same name appears twice, the map is rejected even if the values are
 * equal. Duplicate source material must be resolved before entering this domain
 * boundary.
 *
 * Storage law:
 *
 * Internally this VO stores entries in a private Array.
 *
 * This is intentional. The structure is a compiler-like sorted table:
 *
 * - compact;
 * - cache-friendly;
 * - efficient to sort in place during issuance;
 * - efficient to index and iterate;
 * - free of wrapper/list indirection on the hot path.
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
 * toString() is intentionally compact. Use renderDiagnostic() for bounded
 * human-readable argument details.
 *
 * This VO does not cache hashCode and does not intern entries.
 */
class AnnotationValueMap private constructor(
    private val entries: Array<AnnotationValueEntry>,
) : AbstractList<AnnotationValueEntry>() {
    override val size: Int
        get() = entries.size

    override fun get(index: Int): AnnotationValueEntry = entries[index]

    /**
     * Bounded diagnostic rendering.
     *
     * This is not canonical encoding.
     * This is not a cache key.
     * This is not fingerprint material.
     */
    fun renderDiagnostic(): String {
        if (entries.isEmpty()) {
            return "AnnotationValueMap()"
        }

        val budget =
            DiagnosticBudget(
                remaining = MAX_RENDERED_DIAGNOSTIC_CHARS,
            )

        val builder = StringBuilder()
        budget.append(builder, "AnnotationValueMap(")

        val limit =
            if (entries.size < MAX_RENDERED_ENTRIES) {
                entries.size
            } else {
                MAX_RENDERED_ENTRIES
            }

        var index = 0
        while (index < limit && budget.hasRemaining()) {
            if (index > 0) {
                budget.append(builder, ",")
            }

            val entry = entries[index]
            budget.append(builder, entry.name.value)
            budget.append(builder, "=")
            budget.append(builder, entry.value.renderDiagnostic())

            index += 1
        }

        if (entries.size > limit) {
            if (limit > 0) {
                budget.append(builder, ",")
            }
            budget.append(builder, DiagnosticBudget.TRUNCATION_MARKER)
        }

        budget.append(builder, ")")
        return builder.toString()
    }

    fun renderSummary(): String = "AnnotationValueMap(size=${entries.size})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnnotationValueMap) return false

        return entries.contentEquals(other.entries)
    }

    override fun hashCode(): Int = entries.contentHashCode()

    override fun toString(): String = renderSummary()

    companion object {
        /**
         * Annotation argument count cap.
         *
         * Real annotations normally have very few arguments. This cap prevents
         * malformed adapter output from creating huge sorted arrays and expensive
         * equality/hash/diagnostic work.
         */
        const val MAX_ANNOTATION_ARGUMENTS: Int = 256

        private const val MAX_RENDERED_ENTRIES: Int = 16
        private const val MAX_RENDERED_DIAGNOSTIC_CHARS: Int = 2_048

        private val EMPTY = AnnotationValueMap(emptyArray())

        private val ORDER: Comparator<AnnotationValueEntry> =
            Comparator { left, right ->
                left.name.compareTo(right.name)
            }

        @JvmStatic
        fun empty(): AnnotationValueMap = EMPTY

        @JvmStatic
        fun issue(entries: Collection<AnnotationValueEntry>): AnnotationValueMap {
            if (entries.isEmpty()) {
                return EMPTY
            }

            if (entries.size > MAX_ANNOTATION_ARGUMENTS) {
                throw MetamodelFactContractViolationException(
                    "AnnotationValueMap.entries exceeds order cap=$MAX_ANNOTATION_ARGUMENTS.",
                )
            }

            /*
             * This is the only required copy.
             *
             * Collection input may be externally mutable, so we must detach from
             * it. The resulting array is local to this method, sorted in place,
             * validated, and then transferred into the VO without another copy.
             */
            val buffer = entries.toTypedArray()
            buffer.sortWith(ORDER)

            requireNoDuplicateNames(buffer)

            return AnnotationValueMap(buffer)
        }

        private fun requireNoDuplicateNames(entries: Array<AnnotationValueEntry>) {
            var index = 1

            while (index < entries.size) {
                val previous = entries[index - 1]
                val current = entries[index]

                if (previous.name == current.name) {
                    throw MetamodelFactContractViolationException(
                        "Duplicate annotation argument: ${current.name.value}",
                    )
                }

                index += 1
            }
        }
    }
}
