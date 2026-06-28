package stage.lowering.material.projection

import stage.lowering.diagnostics.ActiveMemberProjectionException
import java.util.AbstractList

/**
 * Immutable deterministic sequence for planning projection-stage outputs.
 *
 * This is NOT a metamodel raw-fact collection.
 * Do not use MetamodelFactSequence here; that type belongs to the metamodel
 * raw fact boundary.
 *
 * This sequence exists for projection-domain results such as:
 * - projected active members before final canonical ordering
 * - property demotion evidence
 *
 * It never deduplicates input silently.
 *
 * DDD role:
 * - planning-domain collection value object
 *
 * Compiler-style role:
 * - freezes one projection-stage output so later diagnostic/traversal stages do
 *   not need to recompute semantic choices.
 *
 * Important:
 * A producer-order sequence is deterministic only when the producer is itself
 * deterministic. ActiveMemberProjector satisfies this by iterating:
 *
 * - selected constructor parameters from MetamodelFactSequence compact index order
 * - raw properties from MetamodelFactSequence deterministic order
 */
class ProjectionSequence<T : Any> private constructor(
    private val snapshot: Array<Any?>,
) : AbstractList<T>(),
    RandomAccess {
    override val size: Int
        get() = snapshot.size

    @Suppress("UNCHECKED_CAST")
    override fun get(index: Int): T = snapshot[index] as T

    fun copyTo(destination: MutableCollection<T>) {
        var i = 0
        while (i < snapshot.size) {
            @Suppress("UNCHECKED_CAST")
            destination.add(snapshot[i] as T)
            i++
        }
    }

    companion object {
        /**
         * Freezes the order emitted by a deterministic producer.
         *
         * This method does not sort.
         * It is appropriate only when the producer has a documented deterministic
         * emission law.
         *
         * ActiveMemberProjector uses this mode because projection order is:
         *
         * 1. selected constructor parameters in compact parameterIndex order
         * 2. admitted properties in RawTypeFactsDTO deterministic property order
         *
         * Final traversal order is still produced later by ActiveMemberOrderer.
         *
         * The input collection is expected not to mutate during capture. If it grows
         * or shrinks during capture, this factory fails with ActiveMemberProjectionException
         * rather than leaking ArrayIndexOutOfBoundsException or trailing null slots.
         */
        @JvmStatic
        fun <T : Any> captureDeterministicProducerOrder(
            ownerTypeFqcn: String,
            sequenceKind: String,
            elements: Collection<T>,
        ): ProjectionSequence<T> {
            validateSequenceKind(sequenceKind)

            val expectedSize = elements.size
            val snapshot = arrayOfNulls<Any?>(expectedSize)
            val iterator = elements.iterator()

            var index = 0
            while (iterator.hasNext()) {
                if (index >= expectedSize) {
                    throw ActiveMemberProjectionException(
                        "ProjectionSequence input collection grew during capture: " +
                                "ownerType=$ownerTypeFqcn, sequenceKind=$sequenceKind, " +
                                "expectedSize=$expectedSize, attemptedIndex=$index",
                    )
                }

                val element: Any? = iterator.next()

                if (element == null) {
                    throw ActiveMemberProjectionException(
                        "ProjectionSequence input collection contained null element: " +
                                "ownerType=$ownerTypeFqcn, sequenceKind=$sequenceKind, index=$index",
                    )
                }

                snapshot[index] = element
                index++
            }

            if (index != expectedSize) {
                throw ActiveMemberProjectionException(
                    "ProjectionSequence input collection shrank during capture: " +
                            "ownerType=$ownerTypeFqcn, sequenceKind=$sequenceKind, " +
                            "expectedSize=$expectedSize, actualCaptured=$index, " +
                            "trailingNullSlots=${expectedSize - index}",
                )
            }

            return ProjectionSequence(snapshot)
        }

        /**
         * Verifies and freezes a sequence that the caller claims is already ordered.
         *
         * This method DOES NOT sort.
         *
         * The caller must supply input that is already strictly ascending according
         * to the provided comparator. If sorting is needed, the caller must sort before
         * calling this method or use another domain-specific sequencing factory that
         * owns sorting.
         *
         * This method exists for boundaries where silently sorting would hide a
         * order bug.
         */
        @JvmStatic
        fun <T : Any> verifiedStrictOrder(
            ownerTypeFqcn: String,
            sequenceKind: String,
            elements: Collection<T>,
            comparator: Comparator<in T>,
        ): ProjectionSequence<T> {
            validateSequenceKind(sequenceKind)

            val buffer = ArrayList<T>(elements.size)
            val iterator = elements.iterator()

            while (iterator.hasNext()) {
                val element: Any? = iterator.next()

                if (element == null) {
                    throw ActiveMemberProjectionException(
                        "ProjectionSequence input collection contained null element: " +
                                "ownerType=$ownerTypeFqcn, sequenceKind=$sequenceKind",
                    )
                }

                @Suppress("UNCHECKED_CAST")
                buffer.add(element as T)
            }

            var i = 1
            while (i < buffer.size) {
                val comparison = comparator.compare(buffer[i - 1], buffer[i])

                if (comparison >= 0) {
                    val reason =
                        if (comparison == 0) {
                            "Sequence contains a comparator tie and is not strictly ordered."
                        } else {
                            "Sequence is not sorted according to the supplied deterministic comparator."
                        }

                    throw ActiveMemberProjectionException(
                        "Invalid ProjectionSequence order: ownerType=$ownerTypeFqcn, " +
                                "sequenceKind=$sequenceKind, reason=$reason",
                    )
                }

                i++
            }

            return fromOrderedBuffer(buffer)
        }

        private fun <T : Any> fromOrderedBuffer(buffer: List<T>): ProjectionSequence<T> {
            val snapshot = arrayOfNulls<Any?>(buffer.size)

            var i = 0
            while (i < buffer.size) {
                snapshot[i] = buffer[i]
                i++
            }

            return ProjectionSequence(snapshot)
        }

        private fun validateSequenceKind(sequenceKind: String) {
            if (sequenceKind.isBlank()) {
                throw ActiveMemberProjectionException(
                    "ProjectionSequence.sequenceKind must not be blank.",
                )
            }

            if (sequenceKind.contains('|')) {
                throw ActiveMemberProjectionException(
                    "ProjectionSequence.sequenceKind must not contain reserved delimiter '|': $sequenceKind",
                )
            }

            var i = 0
            while (i < sequenceKind.length) {
                val ch = sequenceKind[i]

                if (ch.isISOControl()) {
                    throw ActiveMemberProjectionException(
                        "ProjectionSequence.sequenceKind must not contain ISO control characters.",
                    )
                }

                i++
            }
        }
    }
}
