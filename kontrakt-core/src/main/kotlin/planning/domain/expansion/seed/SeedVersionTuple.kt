package planning.domain.expansion.seed

import planning.domain.canonical.text.CanonicalTextLaw

/**
 * Version tuple for deterministic seed materialization.
 *
 * This value separates:
 * - entropy derivation law version;
 * - seed snapshot format/version.
 *
 * Compatibility between both versions is resolved at the run-ratification boundary.
 * This value records the ratified pair; it does not perform adaptive policy resolution.
 */
class SeedVersionTuple private constructor(
    val entropyVersion: String,
    val seedSnapshotVersion: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SeedVersionTuple) return false

        return entropyVersion == other.entropyVersion &&
            seedSnapshotVersion == other.seedSnapshotVersion
    }

    override fun hashCode(): Int {
        var result = entropyVersion.hashCode()
        result = 31 * result + seedSnapshotVersion.hashCode()
        return result
    }

    override fun toString(): String = "SeedVersionTuple(entropyVersion=$entropyVersion, seedSnapshotVersion=$seedSnapshotVersion)"

    companion object {
        @JvmStatic
        fun issue(
            entropyVersion: String,
            seedSnapshotVersion: String,
        ): SeedVersionTuple {
            CanonicalTextLaw.validateCanonicalComponent(
                field = "SeedVersionTuple.entropyVersion",
                value = entropyVersion,
            )
            CanonicalTextLaw.validateCanonicalComponent(
                field = "SeedVersionTuple.seedSnapshotVersion",
                value = seedSnapshotVersion,
            )

            return SeedVersionTuple(
                entropyVersion = entropyVersion,
                seedSnapshotVersion = seedSnapshotVersion,
            )
        }
    }
}
