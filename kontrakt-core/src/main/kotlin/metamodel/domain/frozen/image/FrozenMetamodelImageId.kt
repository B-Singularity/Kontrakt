package metamodel.domain.frozen.image

import metamodel.domain.protocol.MetamodelProtocolTextGuards
import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Diagnostic identity for one frozen metamodel image instance.
 *
 * This value is intentionally not semantic identity material.
 *
 * It is not:
 *
 * - a TypeReference;
 * - an L2 plan cache key;
 * - a route64;
 * - a canonical plan identity;
 * - a schema version;
 * - a persistent frozen-image digest;
 * - a hidden adapter handle key.
 *
 * It must not encode:
 *
 * - backend handle identity;
 * - classloader identity;
 * - object identity;
 * - adapter registry ordinal;
 * - acquisition slot id;
 * - host name;
 * - local filesystem path;
 * - process id;
 * - wall-clock timestamp;
 * - environment variable material.
 *
 * Identity scope law:
 *
 * [acquisitionScopeId] identifies the deterministic acquisition scope that owns
 * image publication.
 *
 * It must not be a coarse adapter kind such as:
 *
 * ```text
 * "reflection"
 * "ksp"
 * "bytecode"
 * ```
 *
 * Those values are source adapter provenance, not image-id scope.
 *
 * A compliant scope id should be issued by the acquisition/bootstrap boundary
 * that owns the image-build sequence. It must be stable for the acquisition
 * scope and sanitized as a order id token.
 *
 * [imageBuildOrdinal] is a scope-local monotonic build ordinal.
 *
 * It is not globally unique by itself. It is meaningful only together with
 * [acquisitionScopeId].
 *
 * Equality law:
 *
 * Equality is structural over:
 *
 * - acquisitionScopeId
 * - imageBuildOrdinal
 *
 * This is required because diagnostic maps, compatibility reports, envelope
 * validation, and telemetry may need to merge observations for the same image id.
 *
 * Reference equality would make two separately issued but structurally identical
 * ids appear different, which would corrupt diagnostic aggregation.
 *
 * Hash law:
 *
 * hashCode is precomputed at issuance time for in-memory collections only.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - route key;
 * - cross-runtime order hash;
 * - serialized order digest.
 *
 * Persistent frozen-image identity requires a separate canonical encoding /
 * digest ADR.
 */
class FrozenMetamodelImageId private constructor(
    val acquisitionScopeId: String,
    val imageBuildOrdinal: Long,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String {
        return "FrozenMetamodelImageId(" +
                "scope=$acquisitionScopeId, " +
                "ordinal=$imageBuildOrdinal" +
                ")"
    }

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenMetamodelImageId) return false

        /*
         * Cheap negative filter for diagnostic maps/sets.
         *
         * Structural equality remains explicit below. The precomputed hash is
         * never authoritative order identity.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return acquisitionScopeId == other.acquisitionScopeId &&
                imageBuildOrdinal == other.imageBuildOrdinal
    }

    override fun hashCode(): Int {
        return precomputedHashCode
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        fun issue(
            acquisitionScopeId: String,
            imageBuildOrdinal: Long,
        ): FrozenMetamodelImageId {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenMetamodelImageId.acquisitionScopeId",
                value = acquisitionScopeId,
                maxChars = MAX_SCOPE_ID_CHARS,
            )

            if (imageBuildOrdinal < 0L) {
                throw MetamodelFactContractViolationException(
                    "FrozenMetamodelImageId.imageBuildOrdinal must be non-negative: " +
                            "imageBuildOrdinal=$imageBuildOrdinal",
                )
            }

            return FrozenMetamodelImageId(
                acquisitionScopeId = acquisitionScopeId,
                imageBuildOrdinal = imageBuildOrdinal,
                precomputedHashCode = computeHashCode(
                    acquisitionScopeId = acquisitionScopeId,
                    imageBuildOrdinal = imageBuildOrdinal,
                ),
            )
        }

        private fun computeHashCode(
            acquisitionScopeId: String,
            imageBuildOrdinal: Long,
        ): Int {
            var result = acquisitionScopeId.hashCode()
            result = 31 * result + imageBuildOrdinal.hashCode()
            return result
        }

        private const val MAX_SCOPE_ID_CHARS: Int = 128
    }
}