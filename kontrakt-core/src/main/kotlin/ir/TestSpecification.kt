package kontrakt.ir

import kontrakt.ir.exception.IrProtocolViolationException
import java.util.Collections
import java.util.Objects
import java.util.TreeMap

/**
 * [IR] Unresolved Test Specification (The Sovereign Protocol).
 *
 * ## Constitution
 * 1. **No Backdoor**: Plain `class` (no data class) to disable `copy()`.
 * 2. **Perfect Hash**: hashCode matches ALL structural fields.
 * 3. **Deep Immutability**: All nested structures are wrapped in unmodifiable layers.
 * 4. **Determinism**: Metadata uses TreeMap for consistent ordering.
 * 5. **Concurrency Safety**: Input collections are snapshotted immediately upon entry.
 */
class TestSpecification private constructor(
    val target: TypeId,
    val subject: SubjectDescriptor,
    val key: SpecKey,
    val modes: List<TestMode>,
    val metadata: Map<String, String>
) {
    init {
        // [Invariant] Key consistency check
        val expectedKey = SpecKey(target, subject.concrete)
        if (key != expectedKey) {
            throw IrProtocolViolationException("Protocol Violation: Key $key does not match structure $expectedKey")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestSpecification) return false

        if (key != other.key) return false
        if (target != other.target) return false
        if (subject != other.subject) return false
        if (modes != other.modes) return false
        if (metadata != other.metadata) return false
        return true
    }

    override fun hashCode(): Int = Objects.hash(target, subject, key, modes, metadata)

    override fun toString(): String =
        "TestSpecification(key=$key, target=$target, subject=$subject, modes=$modes)"

    /**
     * Describes the relationship between the declared type and the concrete implementation.
     */
    class SubjectDescriptor(val declared: TypeId, val concrete: TypeId) {
        override fun equals(other: Any?): Boolean =
            other is SubjectDescriptor && declared == other.declared && concrete == other.concrete

        override fun hashCode(): Int = Objects.hash(declared, concrete)
        override fun toString(): String = "SubjectDescriptor($declared -> $concrete)"
    }

    companion object {
        /**
         * [Factory] Creates a highly immutable and deterministic TestSpecification.
         * Enforces sorting, defensive copying, and immediate snapshotting for concurrency safety.
         */
        fun create(
            target: TypeId,
            declared: TypeId,
            concrete: TypeId,
            modes: Collection<TestMode>,
            metadata: Map<String, String> = emptyMap()
        ): TestSpecification {
            // [Concurrency Defense 1] Snapshot Input Collection immediately
            // Prevents concurrent modification exceptions during iteration/sorting.
            val snapshotModes = ArrayList(modes)

            // [Concurrency Defense 2] Snapshot Metadata immediately
            // Prevents issues if the input map is modified while we build the TreeMap.
            val snapshotMetadata = HashMap(metadata)

            // [Determinism] 1. Deep Sort Modes
            val sortedModes = snapshotModes
                .map { normalizeMode(it) }
                .distinct()
                .sortedWith(TestModeComparator)

            // [Immutability] 2. Deep Defensive Copy (ArrayList -> Unmodifiable)
            val safeModes = Collections.unmodifiableList(ArrayList(sortedModes))

            // [Determinism & Immutability] 3. Metadata (TreeMap -> Unmodifiable)
            val safeMetadata = Collections.unmodifiableMap(TreeMap(snapshotMetadata))

            return TestSpecification(
                target = target,
                subject = SubjectDescriptor(declared, concrete),
                key = SpecKey(target, concrete),
                modes = safeModes,
                metadata = safeMetadata
            )
        }

        private fun normalizeMode(mode: TestMode): TestMode {
            return when (mode) {
                is TestMode.ContractAuto -> {
                    // [Concurrency Defense 3] Snapshot nested list inside TestMode
                    // The 'contractTypes' list inside the mode might also be mutable and shared.
                    val snapshotTypes = ArrayList(mode.contractTypes)

                    // [Deep Immutability] Sort and wrap in unmodifiable list
                    val sortedTypes = snapshotTypes
                        .distinct()
                        .sorted()
                    mode.copy(contractTypes = Collections.unmodifiableList(ArrayList(sortedTypes)))
                }

                else -> mode
            }
        }
    }
}

// [Total Order Comparator]
private object TestModeComparator : Comparator<TestMode> {
    override fun compare(o1: TestMode, o2: TestMode): Int {
        val rank1 = getRank(o1)
        val rank2 = getRank(o2)
        if (rank1 != rank2) return rank1 - rank2

        return when {
            o1 is TestMode.ContractAuto && o2 is TestMode.ContractAuto ->
                compareLists(o1.contractTypes, o2.contractTypes)

            o1 is TestMode.DataCompliance && o2 is TestMode.DataCompliance ->
                o1.dataContractType.compareTo(o2.dataContractType)

            else -> 0
        }
    }

    private fun getRank(mode: TestMode): Int = when (mode) {
        is TestMode.UserScenario -> 0
        is TestMode.ContractAuto -> 1
        is TestMode.DataCompliance -> 2
    }

    private fun compareLists(l1: List<TypeId>, l2: List<TypeId>): Int {
        val minLen = minOf(l1.size, l2.size)
        for (i in 0 until minLen) {
            val cmp = l1[i].compareTo(l2[i])
            if (cmp != 0) return cmp
        }
        return l1.size - l2.size
    }
}