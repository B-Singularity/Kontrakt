package planning.domain.expansion.polymorphic

import metamodel.domain.vo.RuntimeBindingRatificationFingerprint
import metamodel.domain.vo.RuntimeBindingScopeDepth
import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Closed scope id for one runtime binding snapshot.
 *
 * RuntimeBindingSnapshot is not mergeable by default.
 * Layering/merge requires a new ratification boundary and a new scope id.
 */
class RuntimeBindingScopeId private constructor(
    val scopeName: String,
    val depth: RuntimeBindingScopeDepth,
    val parentScopeName: String?,
    val ratificationFingerprint: RuntimeBindingRatificationFingerprint,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuntimeBindingScopeId) return false

        return scopeName == other.scopeName &&
                depth == other.depth &&
                parentScopeName == other.parentScopeName &&
                ratificationFingerprint == other.ratificationFingerprint
    }

    override fun hashCode(): Int {
        var result = scopeName.hashCode()
        result = 31 * result + depth.hashCode()
        result = 31 * result + (parentScopeName?.hashCode() ?: 0)
        result = 31 * result + ratificationFingerprint.hashCode()
        return result
    }

    override fun toString(): String {
        return "RuntimeBindingScopeId(scopeName=$scopeName, depth=$depth, parent=$parentScopeName, fingerprint=$ratificationFingerprint)"
    }

    companion object {
        @JvmStatic
        fun issue(
            scopeName: String,
            depth: RuntimeBindingScopeDepth,
            parentScopeName: String?,
            ratificationFingerprint: RuntimeBindingRatificationFingerprint,
        ): RuntimeBindingScopeId {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "RuntimeBindingScopeId.scopeName",
                value = scopeName,
            )

            if (parentScopeName != null) {
                CanonicalTextLaw.validateCanonicalComponent(
                    field = "RuntimeBindingScopeId.parentScopeName",
                    value = parentScopeName,
                )
            }

            return RuntimeBindingScopeId(
                scopeName = scopeName,
                depth = depth,
                parentScopeName = parentScopeName,
                ratificationFingerprint = ratificationFingerprint,
            )
        }
    }
}