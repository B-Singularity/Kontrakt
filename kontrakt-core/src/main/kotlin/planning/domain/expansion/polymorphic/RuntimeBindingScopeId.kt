package metamodel.domain.vo

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Identity of one runtime binding scope.
 *
 * This is not:
 *
 * - a mutable binding context;
 * - a runtime object handle;
 * - a reflection scope;
 * - a KSP symbol scope;
 * - a graph node implementation;
 * - a cache route key;
 * - or a persisted fingerprint.
 *
 * Scope law:
 *
 * A scope id is the tuple:
 *
 * - scopeName;
 * - parentScopeName;
 * - depth;
 * - ratificationFingerprint;
 * - schemaVersion.
 *
 * Coherence law:
 *
 * - root depth is 0;
 * - root scope must not have a parent scope name;
 * - non-root scope must have a parent scope name;
 * - a scope must not name itself as its own parent.
 *
 * This prevents structurally impossible ids such as:
 *
 * - depth=0 with parent;
 * - depth>0 without parent;
 * - scopeName == parentScopeName.
 *
 * Fingerprint law:
 *
 * ratificationFingerprint proves the ratified runtime binding snapshot surface.
 * This VO includes the fingerprint in equality and hashCode.
 *
 * However, this VO cannot enforce global snapshot replacement rules such as:
 *
 *     "if fingerprint changes, a factory must issue a new scope lineage"
 *
 * That belongs to RuntimeBindingScopeFactory / registry / ratifier policy.
 *
 * Resource law:
 *
 * Scope names are bounded protocol ids. This prevents allocation-based DoS in
 * maps, sorting, diagnostics, and equality paths.
 *
 * Hash law:
 *
 * hashCode is precomputed because RuntimeBindingScopeId is expected to be used
 * frequently as an in-memory map key.
 *
 * The cached hash is not:
 *
 * - a canonical fingerprint;
 * - a persisted identity;
 * - a route key;
 * - a cross-runtime protocol hash.
 *
 * Interning law:
 *
 * This class does not intern scope names. Name interning belongs to the later
 * allocation / flyweight / canonical table phase.
 */
class RuntimeBindingScopeId private constructor(
    val scopeName: String,
    val parentScopeName: String?,
    val depth: RuntimeBindingScopeDepth,
    val ratificationFingerprint: RuntimeBindingRatificationFingerprint,
    val schemaVersion: Int,
    private val precomputedHashCode: Int,
) {
    val isRoot: Boolean
        get() = depth.value == 0

    fun renderSummary(): String {
        return "RuntimeBindingScopeId(" +
                "scopeName=$scopeName, " +
                "parentScopeName=${parentScopeName ?: "<root>"}, " +
                "depth=${depth.value}, " +
                "fingerprint=${ratificationFingerprint.renderSummary()}, " +
                "schemaVersion=$schemaVersion" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingScopeId) return false

        /*
         * Use the precomputed hash only as a cheap negative filter.
         * Equality remains structural.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return scopeName == other.scopeName &&
                parentScopeName == other.parentScopeName &&
                depth == other.depth &&
                ratificationFingerprint == other.ratificationFingerprint &&
                schemaVersion == other.schemaVersion
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1

        /**
         * Protocol cap for scope names.
         *
         * Runtime binding scope names should be short internal protocol ids.
         * Raising this cap should be treated as a metamodel protocol amendment.
         */
        const val MAX_RUNTIME_BINDING_SCOPE_NAME_CHARS: Int = 128

        @JvmStatic
        fun issue(
            scopeName: String,
            parentScopeName: String?,
            depth: RuntimeBindingScopeDepth,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId {
            requireScopeName(
                field = "RuntimeBindingScopeId.scopeName",
                value = scopeName,
            )

            if (parentScopeName != null) {
                requireScopeName(
                    field = "RuntimeBindingScopeId.parentScopeName",
                    value = parentScopeName,
                )
            }

            requireDepthParentCoherence(
                scopeName = scopeName,
                parentScopeName = parentScopeName,
                depth = depth,
            )

            return RuntimeBindingScopeId(
                scopeName = scopeName,
                parentScopeName = parentScopeName,
                depth = depth,
                ratificationFingerprint = ratificationFingerprint,
                schemaVersion = CURRENT_SCHEMA_VERSION,
                precomputedHashCode = computeHashCode(
                    scopeName = scopeName,
                    parentScopeName = parentScopeName,
                    depth = depth,
                    ratificationFingerprint = ratificationFingerprint,
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                ),
            )
        }

        @JvmStatic
        fun root(
            scopeName: String,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId {
            return issue(
                scopeName = scopeName,
                parentScopeName = null,
                depth = RuntimeBindingScopeDepth.root(),
                ratificationFingerprint = ratificationFingerprint,
            )
        }

        @JvmStatic
        fun child(
            scopeName: String,
            parentScopeName: String,
            parentDepth: RuntimeBindingScopeDepth,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId {
            return issue(
                scopeName = scopeName,
                parentScopeName = parentScopeName,
                depth = parentDepth.next(),
                ratificationFingerprint = ratificationFingerprint,
            )
        }

        private fun requireScopeName(
            field: String,
            value: String,
        ) {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = field,
                value = value,
                maxChars = MAX_RUNTIME_BINDING_SCOPE_NAME_CHARS,
            )
        }

        private fun requireDepthParentCoherence(
            scopeName: String,
            parentScopeName: String?,
            depth: RuntimeBindingScopeDepth,
        ) {
            if (depth.value == 0 && parentScopeName != null) {
                throw MetamodelFactContractViolationException(
                    "RuntimeBindingScopeId root scope must not have parentScopeName: " +
                            "scopeName=$scopeName, parentScopeName=$parentScopeName",
                )
            }

            if (depth.value > 0 && parentScopeName == null) {
                throw MetamodelFactContractViolationException(
                    "RuntimeBindingScopeId non-root scope must have parentScopeName: " +
                            "scopeName=$scopeName, depth=${depth.value}",
                )
            }

            if (parentScopeName != null && scopeName == parentScopeName) {
                throw MetamodelFactContractViolationException(
                    "RuntimeBindingScopeId must not reference itself as parent: " +
                            "scopeName=$scopeName",
                )
            }
        }

        private fun computeHashCode(
            scopeName: String,
            parentScopeName: String?,
            depth: RuntimeBindingScopeDepth,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
            schemaVersion: Int,
        ): Int {
            var result = scopeName.hashCode()
            result = 31 * result + (parentScopeName?.hashCode() ?: 0)
            result = 31 * result + depth.hashCode()
            result = 31 * result + ratificationFingerprint.hashCode()
            result = 31 * result + schemaVersion
            return result
        }
    }
}