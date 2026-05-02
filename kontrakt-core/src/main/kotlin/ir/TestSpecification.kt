package kontrakt.ir.spec

import ir.IrLimits
import ir.exception.IrProtocolViolationException
import ir.identity.CanonicalIdentifier
import ir.spec.SpecKey
import ir.spec.TestMode
import ir.spec.TypeId
import ir.structure.DeterministicList
import ir.structure.DeterministicMap
import java.util.Objects

/**
 * Immutable protocol representation of a test specification.
 *
 * Core properties:
 * - Deterministic containers only (no raw Map/List fields)
 * - Strict invariants:
 *   - key must match (target, subject.concrete)
 *   - exactly one mode must exist (current spec)
 * - Metadata size/value limits enforced
 */
class TestSpecification private constructor(
    val target: TypeId,
    val subject: SubjectDescriptor,
    val key: SpecKey,
    val modes: DeterministicList<TestMode>,
    val metadata: DeterministicMap<CanonicalIdentifier, String>,
) {
    init {
        val expectedKey = SpecKey(target, subject.concrete)
        if (key != expectedKey) throw IrProtocolViolationException("Key mismatch.")
        if (modes.size != 1) throw IrProtocolViolationException("Exactly one TestMode required.")
    }

    val mode: TestMode get() = modes[0]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestSpecification) return false
        return key == other.key && modes == other.modes && metadata == other.metadata
    }

    override fun hashCode(): Int = Objects.hash(target, subject, key, modes, metadata)

    override fun toString(): String = "TestSpecification(key=$key, mode=$mode)"

    /**
     * Subject type descriptor.
     *
     * declared: the type as declared by the user
     * concrete: the resolved concrete type used for execution
     */
    class SubjectDescriptor(
        val declared: TypeId,
        val concrete: TypeId,
    ) {
        override fun equals(other: Any?): Boolean = other is SubjectDescriptor && declared == other.declared && concrete == other.concrete

        override fun hashCode(): Int = Objects.hash(declared, concrete)

        override fun toString(): String = "SubjectDescriptor(declared=$declared, concrete=$concrete)"
    }

    companion object {
        /**
         * Factory that enforces protocol invariants and deterministic structures.
         *
         * Notes:
         * - Exactly one TestMode is enforced as a hard protocol constraint.
         * - All containers are normalized into DeterministicList/Map.
         */
        fun create(
            target: TypeId,
            declared: TypeId,
            concrete: TypeId,
            modes: Collection<TestMode>,
            metadata: Map<CanonicalIdentifier, String> = emptyMap(),
        ): TestSpecification {
            // Surface contributor mistakes: duplicates still count as >1 here.
            if (modes.size != 1) {
                throw IrProtocolViolationException("Exactly one TestMode required (got ${modes.size}).")
            }

            val safeModes = DeterministicList.of(modes, IrLimits.MAX_MODES)

            val safeMetadata =
                DeterministicMap.of(metadata, IrLimits.MAX_METADATA_ENTRIES) { value ->
                    if (value.length > IrLimits.MAX_METADATA_VALUE_LENGTH) {
                        throw IrProtocolViolationException("Metadata value exceeds max length.")
                    }
                }

            return TestSpecification(
                target = target,
                subject = SubjectDescriptor(declared, concrete),
                key = SpecKey(target, concrete),
                modes = safeModes,
                metadata = safeMetadata,
            )
        }
    }
}
