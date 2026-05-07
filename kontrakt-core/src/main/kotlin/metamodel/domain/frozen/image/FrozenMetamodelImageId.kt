package metamodel.domain.frozen.image

import metamodel.domain.exception.MetamodelFactContractViolationException
import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Diagnostic identity for one frozen metamodel image instance.
 *
 * This id is intentionally not semantic identity material.
 *
 * It is not:
 * - a TypeReference;
 * - an L2 plan cache key;
 * - a route64;
 * - a canonical plan identity;
 * - a schema version;
 * - a hidden adapter handle key.
 *
 * It must not encode:
 * - backend handle identity;
 * - classloader identity;
 * - object identity;
 * - adapter registry ordinal;
 * - acquisition slot id.
 */
class FrozenMetamodelImageId private constructor(
    val acquisitionScopeId: String,
    val imageBuildOrdinal: Long,
) {
    override fun toString(): String {
        return "FrozenMetamodelImageId(scope=$acquisitionScopeId, ordinal=$imageBuildOrdinal)"
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
            )
        }

        private const val MAX_SCOPE_ID_CHARS: Int = 128
    }
}