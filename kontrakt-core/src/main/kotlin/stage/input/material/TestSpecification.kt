package kontrakt.ir.spec

import ir.IrLimits
import ir.exception.IrProtocolViolationException
import ir.identity.CanonicalIdentifier
import ir.structure.DeterministicList
import ir.structure.DeterministicMap
import stage.input.material.SpecKey
import stage.input.material.TestMode
import stage.input.material.TypeId

/**
 * Immutable order representation of a test specification.
 *
 * This is not:
 *
 * - a mutable builder;
 * - an execution result;
 * - a runtime test instance;
 * - a discovery record;
 * - or a cache/interner key by itself.
 *
 * Core properties:
 *
 * - deterministic containers only;
 * - strict key coherence;
 * - current order allows exactly one TestMode;
 * - metadata size/value limits are enforced at the factory boundary.
 *
 * Mode layout law:
 *
 * TestSpecification intentionally stores modes as DeterministicList<TestMode>,
 * even though the current order requires exactly one mode.
 *
 * The list-shaped layout preserves forward compatibility for a later order
 * amendment where a specification may carry a deterministic combination of
 * modes.
 *
 * Today:
 *
 * - modes.size must be exactly 1;
 * - mode returns modes[0];
 * - downstream code must treat TestSpecification as single-mode;
 * - callers must not infer multi-mode support from the container shape.
 *
 * Identity law:
 *
 * TestSpecification identity includes:
 *
 * - target;
 * - subject.declared;
 * - subject.concrete;
 * - key;
 * - modes;
 * - metadata.
 *
 * key alone is not enough because key is derived from target + concrete and does
 * not preserve the user-declared subject type.
 *
 * Hash law:
 *
 * hashCode uses the same semantic axes as equals.
 *
 * This implementation intentionally does not precompute hashCode yet. Hash
 * precomputation belongs to the later interning/allocation phase.
 */
class TestSpecification private constructor(
    val target: TypeId,
    val subject: TestSubjectBinding,
    val key: SpecKey,
    val modes: DeterministicList<TestMode>,
    val metadata: DeterministicMap<CanonicalIdentifier, String>,
) {
    init {
        val expectedKey =
            SpecKey.of(
                target,
                subject.concrete,
            )

        if (key != expectedKey) {
            throw IrProtocolViolationException(
                "TestSpecification key mismatch.",
            )
        }

        if (modes.size != CURRENT_MODE_COUNT) {
            throw IrProtocolViolationException(
                "Exactly one TestMode required by the current TestSpecification order.",
            )
        }
    }

    /**
     * Current single-mode view.
     *
     * Downstream execution, planning, and reporting code should consume this
     * property rather than manually indexing modes while the order remains
     * single-mode.
     */
    val mode: TestMode
        get() = modes[0]

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is TestSpecification) return false

        return target == other.target &&
                subject == other.subject &&
                key == other.key &&
                modes == other.modes &&
                metadata == other.metadata
    }

    override fun hashCode(): Int {
        var result = target.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + modes.hashCode()
        result = 31 * result + metadata.hashCode()
        return result
    }

    override fun toString(): String {
        return "TestSpecification(" +
                "key=$key, " +
                "subject=$subject, " +
                "mode=$mode, " +
                "metadata=${metadata.size}" +
                ")"
    }

    companion object {
        private const val CURRENT_MODE_COUNT: Int = 1

        /**
         * Issues a TestSpecification after enforcing order invariants and
         * deterministic container normalization.
         *
         * Notes:
         *
         * - The modes container is retained for future-compatible order layout.
         * - The current order still requires exactly one TestMode.
         * - Duplicate modes still count as multiple modes at this boundary.
         * - Containers are normalized into DeterministicList/DeterministicMap.
         */
        fun issue(
            target: TypeId,
            declared: TypeId,
            concrete: TypeId,
            modes: Collection<TestMode>,
            metadata: Map<CanonicalIdentifier, String> = emptyMap(),
        ): TestSpecification {
            if (modes.size != CURRENT_MODE_COUNT) {
                throw IrProtocolViolationException(
                    "Exactly one TestMode required by the current TestSpecification order " +
                            "(got ${modes.size}).",
                )
            }

            val safeModes =
                DeterministicList.of(
                    modes,
                    IrLimits.MAX_MODES,
                )

            val safeMetadata =
                DeterministicMap.of(
                    metadata,
                    IrLimits.MAX_METADATA_ENTRIES,
                ) { value ->
                    if (value.length > IrLimits.MAX_METADATA_VALUE_LENGTH) {
                        throw IrProtocolViolationException(
                            "Metadata value exceeds max length.",
                        )
                    }
                }

            val subject =
                TestSubjectBinding.issue(
                    declared = declared,
                    concrete = concrete,
                )

            return TestSpecification(
                target = target,
                subject = subject,
                key = SpecKey.of(
                    target,
                    concrete,
                ),
                modes = safeModes,
                metadata = safeMetadata,
            )
        }
    }
}