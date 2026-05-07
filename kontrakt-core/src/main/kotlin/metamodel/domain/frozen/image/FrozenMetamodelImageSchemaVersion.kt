package metamodel.domain.frozen.image

import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Compatibility version for the frozen metamodel image structure.
 *
 * This version describes the interpretation law of:
 * - type index;
 * - shape table;
 * - cycle identity table;
 * - raw fact table;
 * - nested frozen record sequence surfaces.
 *
 * It is not:
 * - an image id;
 * - a route64;
 * - a PlanCacheKey;
 * - a BLAKE3 digest version;
 * - a canonical byte encoding version.
 */
class FrozenMetamodelImageSchemaVersion private constructor(
    val value: String,
) {
    override fun toString(): String {
        return value
    }

    companion object {
        const val CURRENT_VALUE: String =
            "frozen-metamodel-image.v1"

        @JvmStatic
        fun current(): FrozenMetamodelImageSchemaVersion {
            return issue(CURRENT_VALUE)
        }

        @JvmStatic
        fun issue(
            value: String,
        ): FrozenMetamodelImageSchemaVersion {
            MetamodelProtocolTextGuards.requireAsciiProtocolIdToken(
                field = "FrozenMetamodelImageSchemaVersion.value",
                value = value,
                maxChars = MAX_SCHEMA_VERSION_CHARS,
            )

            return FrozenMetamodelImageSchemaVersion(
                value = value,
            )
        }

        private const val MAX_SCHEMA_VERSION_CHARS: Int = 128
    }
}