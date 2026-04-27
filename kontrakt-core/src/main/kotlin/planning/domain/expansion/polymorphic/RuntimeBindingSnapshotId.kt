package planning.domain.expansion.polymorphic

/**
 * Lightweight id for a run-ratified RuntimeBindingSnapshot.
 *
 * The scope id is the composite ratification identity.
 */
class RuntimeBindingSnapshotId private constructor(
    val scopeId: RuntimeBindingScopeId,
) {
    override fun equals(other: Any?): Boolean {
        return other is RuntimeBindingSnapshotId && scopeId == other.scopeId
    }

    override fun hashCode(): Int {
        return scopeId.hashCode()
    }

    override fun toString(): String {
        return "RuntimeBindingSnapshotId(scope=$scopeId)"
    }

    companion object {
        @JvmStatic
        fun issue(scopeId: RuntimeBindingScopeId): RuntimeBindingSnapshotId {
            return RuntimeBindingSnapshotId(scopeId)
        }
    }
}