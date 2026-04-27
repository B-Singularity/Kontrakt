package planning.domain.expansion.seed

/**
 * Run-ratified deterministic seed surface.
 *
 * The raw seed is not exposed.
 * Seed derivation must go through SeedMaterializer / entropy derivation code.
 */
class DeterministicSeedSurface private constructor(
    val id: DeterministicSeedSurfaceId,
    private val seedMaterial: SeedMaterial,
    val versionTuple: SeedVersionTuple,
    val rootTimeEpochMillis: RootTimeEpochMillis,
) {
    fun copySeedBytesForDerivation(): ByteArray {
        return seedMaterial.copyBytesForDerivation()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeterministicSeedSurface) return false

        return id == other.id &&
                seedMaterial == other.seedMaterial &&
                versionTuple == other.versionTuple &&
                rootTimeEpochMillis == other.rootTimeEpochMillis
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + seedMaterial.hashCode()
        result = 31 * result + versionTuple.hashCode()
        result = 31 * result + rootTimeEpochMillis.hashCode()
        return result
    }

    override fun toString(): String {
        return "DeterministicSeedSurface(id=${id.value}, seed=<redacted>, versionTuple=$versionTuple, rootTime=$rootTimeEpochMillis)"
    }

    companion object {
        @JvmStatic
        fun issue(
            id: DeterministicSeedSurfaceId,
            seedMaterial: SeedMaterial,
            versionTuple: SeedVersionTuple,
            rootTimeEpochMillis: RootTimeEpochMillis,
        ): DeterministicSeedSurface {
            return DeterministicSeedSurface(
                id = id,
                seedMaterial = seedMaterial,
                versionTuple = versionTuple,
                rootTimeEpochMillis = rootTimeEpochMillis,
            )
        }
    }
}