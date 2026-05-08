package metamodel.domain.frozen.image

import metamodel.domain.protocol.MetamodelProtocolTextGuards

/**
 * Compatibility version for the frozen metamodel image structure.
 *
 * This value describes the interpretation law of:
 *
 * - the frozen type index;
 * - the frozen type-shape table;
 * - the frozen cycle-identity table;
 * - the frozen raw-fact table;
 * - nested frozen record sequences;
 * - table coverage semantics;
 * - image-local ordinal semantics.
 *
 * It is not:
 *
 * - an image id;
 * - an acquisition scope id;
 * - a route64;
 * - a PlanCacheKey;
 * - a BLAKE3 digest version;
 * - a canonical byte encoding version;
 * - a persistent frozen-image content digest;
 * - a source adapter provenance token.
 *
 * Equality law:
 *
 * Equality is structural over [value].
 *
 * This is required because schema versions may be issued independently by:
 *
 * - the image;
 * - the type index;
 * - the shape table;
 * - the cycle identity table;
 * - the raw fact table;
 * - a compatibility checker;
 * - a diagnostic envelope loader.
 *
 * All of those independently-issued objects must compare equal when they carry
 * the same schema token. Reference equality would make valid images fail
 * integrity validation merely because the same schema string was wrapped in
 * two different objects.
 *
 * Compatibility law:
 *
 * This VO currently models exact schema identity only.
 *
 * Backward-compatible reading, major/minor negotiation, migration, or tolerant
 * loaders must be introduced through a separate compatibility policy surface,
 * not by weakening equality in this VO.
 *
 * Rationale:
 *
 * Schema compatibility is not always equivalent to string equality. However,
 * allowing this VO to decide compatibility would make a small value object own
 * loader policy, migration policy, and persistence policy. Those are separate
 * architecture boundaries.
 *
 * Hash law:
 *
 * hashCode is precomputed for in-memory equality collections only.
 *
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - route key;
 * - cross-runtime protocol hash;
 * - serialized protocol digest.
 */
class FrozenMetamodelImageSchemaVersion private constructor(
    val value: String,
    private val precomputedHashCode: Int,
) {
    fun renderSummary(): String =
        "FrozenMetamodelImageSchemaVersion(value=$value)"

    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is FrozenMetamodelImageSchemaVersion) return false

        /*
         * Cheap negative filter.
         *
         * Structural equality remains value-only.
         */
        if (precomputedHashCode != other.precomputedHashCode) {
            return false
        }

        return value == other.value
    }

    override fun hashCode(): Int =
        precomputedHashCode

    override fun toString(): String =
        value

    companion object {
        const val CURRENT_VALUE: String =
            "frozen-metamodel-image.v1"

        private val CURRENT_INSTANCE: FrozenMetamodelImageSchemaVersion =
            issue(CURRENT_VALUE)

        @JvmStatic
        fun current(): FrozenMetamodelImageSchemaVersion =
            CURRENT_INSTANCE

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
                precomputedHashCode = computeHashCode(value),
            )
        }

        /**
         * Computes the transitional JVM hashCode companion.
         *
         * This deliberately keeps the current project-wide metamodel behavior
         * aligned with the existing VO family until the later BLAKE3 /
         * metadata-hash refactoring replaces the hash policy globally.
         *
         * Do not treat this value as protocol material.
         */
        private fun computeHashCode(
            value: String,
        ): Int =
            value.hashCode()

        private const val MAX_SCHEMA_VERSION_CHARS: Int = 128
    }
}