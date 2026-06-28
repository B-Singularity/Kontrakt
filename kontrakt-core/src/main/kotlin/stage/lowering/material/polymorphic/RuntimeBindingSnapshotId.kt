package stage.lowering.material.polymorphic

/**
 * Type-safe id for a run-ratified RuntimeBindingSnapshot.
 *
 * This is not:
 *
 * - a Kotlin value class;
 * - a typealias;
 * - a mutable snapshot handle;
 * - a snapshot payload;
 * - a cache route key;
 * - or a serialization DTO.
 *
 * Identity law:
 *
 * RuntimeBindingSnapshotId is currently a type-safe identity view over
 * RuntimeBindingScopeId.
 *
 * The current order is 1:1:
 *
 *     one RuntimeBindingScopeId -> one RuntimeBindingSnapshotId
 *
 * Allocation law:
 *
 * RuntimeBindingSnapshotId.issue(scopeId) must not allocate repeatedly.
 * It delegates to the RuntimeBindingScopeId-owned snapshot id.
 *
 * The only allocation happens during RuntimeBindingScopeId construction.
 *
 * Serialization law:
 *
 * This object points back to its owning RuntimeBindingScopeId. Do not serialize
 * it directly with reflective serializers. Use renderSummary() or a dedicated
 * DTO.
 *
 * Evolution law:
 *
 * If the order later allows multiple snapshots inside one scope without
 * changing RuntimeBindingScopeId.ratificationFingerprint, this class must gain a
 * new identity axis:
 *
 * - snapshotOrdinal;
 * - snapshotSequence;
 * - or snapshotFingerprint.
 */
class RuntimeBindingSnapshotId private constructor(
    val scopeId: RuntimeBindingScopeId,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String =
        "RuntimeBindingSnapshotId(" +
                scopeId.renderSnapshotIdentitySummary() +
                ")"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingSnapshotId) return false

        /*
         * Cheap negative filter only.
         * Structural equality remains authoritative.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return scopeId == other.scopeId
    }

    override fun hashCode(): Int = precomputedHashCode

    override fun toString(): String = renderSummary()

    companion object {
        /**
         * Ergonomic entry point.
         *
         * This returns the RuntimeBindingScopeId-owned instance and therefore
         * does not allocate on repeated calls.
         */
        @JvmStatic
        fun issue(scopeId: RuntimeBindingScopeId): RuntimeBindingSnapshotId = scopeId.snapshotId()

        @JvmStatic
        fun fromScopeId(scopeId: RuntimeBindingScopeId): RuntimeBindingSnapshotId = issue(scopeId)

        /**
         * Used only by RuntimeBindingScopeId during its own construction.
         *
         * Do not call this from arbitrary code. Snapshot identity is owned by
         * scope identity.
         */
        internal fun issueFromScope(
            scopeId: RuntimeBindingScopeId,
            precomputedHashCode: Int,
        ): RuntimeBindingSnapshotId =
            RuntimeBindingSnapshotId(
                scopeId = scopeId,
                precomputedHashCode = precomputedHashCode,
            )
    }
}
