package stage.lowering.material.polymorphic

import metamodel.domain.vo.RuntimeBindingRatificationFingerprint
import metamodel.domain.vo.RuntimeBindingScopeDepth
import stage.lowering.diagnostics.TypeExpansionContractViolationException

/**
 * Closed scope id for one runtime binding snapshot.
 *
 * This is not:
 *
 * - a mutable runtime binding scope;
 * - a snapshot payload;
 * - a graph node implementation;
 * - a cache route key;
 * - a persisted fingerprint;
 * - or a serialization DTO.
 *
 * Identity law:
 *
 * RuntimeBindingScopeId is the composite ratification identity for the current
 * runtime binding order.
 *
 * It includes:
 *
 * - scopeName;
 * - depth;
 * - parentScopeName;
 * - ratificationFingerprint;
 * - schemaVersion.
 *
 * Snapshot ownership law:
 *
 * In the current order, RuntimeBindingSnapshotId is a type-safe identity view
 * over RuntimeBindingScopeId. The relationship is exactly 1:1.
 *
 * Therefore RuntimeBindingScopeId owns exactly one RuntimeBindingSnapshotId.
 *
 * Callers must obtain the snapshot id through:
 *
 *     scopeId.snapshotId()
 *
 * RuntimeBindingSnapshotId.issue(scopeId) delegates to the same owned instance.
 *
 * This is not global interning. It is local derived identity ownership.
 *
 * Coherence law:
 *
 * - root depth is 0;
 * - root scope must not have a parent scope name;
 * - non-root scope must have a parent scope name;
 * - a scope must not name itself as parent.
 *
 * Serialization law:
 *
 * RuntimeBindingScopeId and RuntimeBindingSnapshotId form a deliberate owned
 * identity cycle. These objects must not be serialized directly by reflective
 * serializers. Export a dedicated DTO or renderSummary() output instead.
 *
 * Hash law:
 *
 * hashCode is precomputed because this id is expected to be used frequently as
 * an in-memory map key.
 *
 * This cached hash is not a canonical fingerprint, persisted key, route key, or
 * cross-runtime order hash.
 */
class RuntimeBindingScopeId private constructor(
    val scopeName: String,
    val depth: RuntimeBindingScopeDepth,
    val parentScopeName: String?,
    val ratificationFingerprint: RuntimeBindingRatificationFingerprint,
    val schemaVersion: Int,
    private val precomputedHashCode: Int,
) {
    private val ownedSnapshotId: RuntimeBindingSnapshotId =
        RuntimeBindingSnapshotId.issueFromScope(
            scopeId = this,
            precomputedHashCode = precomputedHashCode,
        )

    val isRoot: Boolean
        get() = depth.value == 0

    fun snapshotId(): RuntimeBindingSnapshotId = ownedSnapshotId

    fun renderSummary(): String =
        "RuntimeBindingScopeId(" +
                "scopeName=$scopeName, " +
                "depth=${depth.value}, " +
                "parentScopeName=${parentScopeName ?: "<root>"}, " +
                "fingerprint=${ratificationFingerprint.renderSummary()}, " +
                "schemaVersion=$schemaVersion" +
                ")"

    /**
     * Safe one-line identity view for RuntimeBindingSnapshotId diagnostics.
     *
     * This avoids recursive object rendering through the owned identity cycle.
     */
    internal fun renderSnapshotIdentitySummary(): String =
        "scopeName=$scopeName, " +
                "depth=${depth.value}, " +
                "parentScopeName=${parentScopeName ?: "<root>"}, " +
                "fingerprint=${ratificationFingerprint.renderSummary()}, " +
                "schemaVersion=$schemaVersion"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingScopeId) return false

        /*
         * Cheap negative filter only.
         * Structural equality remains authoritative.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return scopeName == other.scopeName &&
                depth == other.depth &&
                parentScopeName == other.parentScopeName &&
                ratificationFingerprint == other.ratificationFingerprint &&
                schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int = precomputedHashCode

    override fun toString(): String = renderSummary()

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        const val MAX_RUNTIME_BINDING_SCOPE_NAME_CHARS: Int = 128

        @JvmStatic
        fun issue(
            scopeName: String,
            depth: RuntimeBindingScopeDepth,
            parentScopeName: String?,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId {
            RuntimeBindingScopeNameLaw.requireScopeName(
                field = "RuntimeBindingScopeId.scopeName",
                value = scopeName,
            )

            if (parentScopeName != null) {
                RuntimeBindingScopeNameLaw.requireScopeName(
                    field = "RuntimeBindingScopeId.parentScopeName",
                    value = parentScopeName,
                )
            }

            requireDepthParentCoherence(
                scopeName = scopeName,
                depth = depth,
                parentScopeName = parentScopeName,
            )

            return RuntimeBindingScopeId(
                scopeName = scopeName,
                depth = depth,
                parentScopeName = parentScopeName,
                ratificationFingerprint = ratificationFingerprint,
                schemaVersion = CURRENT_SCHEMA_VERSION,
                precomputedHashCode =
                    computeHashCode(
                        scopeName = scopeName,
                        depth = depth,
                        parentScopeName = parentScopeName,
                        ratificationFingerprint = ratificationFingerprint,
                        schemaVersion = CURRENT_SCHEMA_VERSION,
                    ),
            )
        }

        @JvmStatic
        fun root(
            scopeName: String,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId =
            issue(
                scopeName = scopeName,
                depth = RuntimeBindingScopeDepth.root(),
                parentScopeName = null,
                ratificationFingerprint = ratificationFingerprint,
            )

        @JvmStatic
        fun child(
            scopeName: String,
            parentScopeName: String,
            parentDepth: RuntimeBindingScopeDepth,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId =
            issue(
                scopeName = scopeName,
                depth = parentDepth.next(),
                parentScopeName = parentScopeName,
                ratificationFingerprint = ratificationFingerprint,
            )

        private fun requireDepthParentCoherence(
            scopeName: String,
            depth: RuntimeBindingScopeDepth,
            parentScopeName: String?,
        ) {
            if (depth.value == 0 && parentScopeName != null) {
                throw TypeExpansionContractViolationException(
                    reason =
                        "RuntimeBindingScopeId root scope must not have parentScopeName: " +
                                "scopeName=$scopeName, parentScopeName=$parentScopeName",
                )
            }

            if (depth.value > 0 && parentScopeName == null) {
                throw TypeExpansionContractViolationException(
                    reason =
                        "RuntimeBindingScopeId non-root scope must have parentScopeName: " +
                                "scopeName=$scopeName, depth=${depth.value}",
                )
            }

            if (parentScopeName != null && scopeName == parentScopeName) {
                throw TypeExpansionContractViolationException(
                    reason =
                        "RuntimeBindingScopeId must not reference itself as parent: " +
                                "scopeName=$scopeName",
                )
            }
        }

        private fun computeHashCode(
            scopeName: String,
            depth: RuntimeBindingScopeDepth,
            parentScopeName: String?,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
            schemaVersion: Int,
        ): Int {
            var result = scopeName.hashCode()
            result = 31 * result + depth.hashCode()
            result = 31 * result + (parentScopeName?.hashCode() ?: 0)
            result = 31 * result + ratificationFingerprint.hashCode()
            result = 31 * result + schemaVersion
            return result
        }
    }
}

/**
 * Local order guard for runtime binding scope names.
 *
 * This deliberately avoids stage.canonicalization.contract.CanonicalTextLaw.
 *
 * Allowed:
 *
 * - A-Z
 * - a-z
 * - 0-9
 * - -
 * - _
 * - .
 *
 * This is an internal order id surface, not a user-facing source identifier.
 */
private object RuntimeBindingScopeNameLaw {
    fun requireScopeName(
        field: String,
        value: String,
    ) {
        if (value.isEmpty()) {
            throw TypeExpansionContractViolationException(
                reason = "$field must not be empty.",
            )
        }

        if (value.length > RuntimeBindingScopeId.MAX_RUNTIME_BINDING_SCOPE_NAME_CHARS) {
            throw TypeExpansionContractViolationException(
                reason = "$field exceeds maximum allowed length.",
            )
        }

        var index = 0
        while (index < value.length) {
            val c = value[index]
            val ok =
                c in 'A'..'Z' ||
                        c in 'a'..'z' ||
                        c in '0'..'9' ||
                        c == '-' ||
                        c == '_' ||
                        c == '.'

            if (!ok) {
                throw TypeExpansionContractViolationException(
                    reason = "$field contains a non-canonical order-id character at index=$index.",
                )
            }

            index += 1
        }
    }
}
