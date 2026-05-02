package planning.domain.expansion.seed

/**
 * Run-ratified deterministic seed surface.
 *
 * This is the frozen deterministic entropy surface for one run/session.
 *
 * The raw seed is not exposed.
 * Seed derivation must go through SeedMaterializer / entropy derivation code.
 *
 * This is not:
 *
 * - a wall-clock reader;
 * - a random generator;
 * - a mutable session entropy stream;
 * - a public adapter-created DTO;
 * - a cache key;
 * - or a persisted seed manifest.
 *
 * Issuance law:
 *
 * DeterministicSeedSurface must be issued only by the run-ratification boundary.
 * Public construction is forbidden because arbitrary adapter-side assembly would
 * allow incoherent tuples such as:
 *
 *     id from one run + version tuple from another + root time from a third
 *
 * Coherence law:
 *
 * issueFromRatification(...) requires a
 * DeterministicSeedSurfaceRatificationProof that covers the exact tuple:
 *
 * - id;
 * - versionTuple;
 * - rootTimeEpochMillis.
 *
 * Root time is not interpreted here as wall-clock freshness. It is deterministic
 * seed material. Any policy such as "root time must not be after/before X"
 * belongs to the run-ratifier / policy boundary, not this VO.
 *
 * Security law:
 *
 * - seed material is redacted from diagnostics;
 * - copySeedBytesForDerivation() returns a defensive copy;
 * - toString() never prints seed bytes.
 *
 * Hash law:
 *
 * hashCode() is for in-memory equality collections only.
 * It must not be used as:
 *
 * - canonical fingerprint;
 * - persisted identity;
 * - cache route key;
 * - cross-runtime protocol hash;
 * - seed digest.
 */
class DeterministicSeedSurface private constructor(
    val id: DeterministicSeedSurfaceId,
    private val seedMaterial: SeedMaterial,
    val versionTuple: SeedVersionTuple,
    val rootTimeEpochMillis: RootTimeEpochMillis,
    val ratificationProof: DeterministicSeedSurfaceRatificationProof,
) {
    fun copySeedBytesForDerivation(): ByteArray {
        return seedMaterial.copyBytesForDerivation()
    }

    fun renderSummary(): String {
        return "DeterministicSeedSurface(" +
                "id=${id.value}, " +
                "seed=<redacted>, " +
                "versionTuple=$versionTuple, " +
                "rootTime=$rootTimeEpochMillis, " +
                "proof=${ratificationProof.renderSummary()}" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeterministicSeedSurface) return false

        return id == other.id &&
                seedMaterial == other.seedMaterial &&
                versionTuple == other.versionTuple &&
                rootTimeEpochMillis == other.rootTimeEpochMillis &&
                ratificationProof == other.ratificationProof
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + seedMaterial.hashCode()
        result = 31 * result + versionTuple.hashCode()
        result = 31 * result + rootTimeEpochMillis.hashCode()
        result = 31 * result + ratificationProof.hashCode()
        return result
    }

    override fun toString(): String {
        return renderSummary()
    }

    companion object {
        @JvmStatic
        internal fun issueFromRatification(
            id: DeterministicSeedSurfaceId,
            seedMaterial: SeedMaterial,
            versionTuple: SeedVersionTuple,
            rootTimeEpochMillis: RootTimeEpochMillis,
            ratificationProof: DeterministicSeedSurfaceRatificationProof,
        ): DeterministicSeedSurface {
            ratificationProof.requireCovers(
                id = id,
                versionTuple = versionTuple,
                rootTimeEpochMillis = rootTimeEpochMillis,
            )

            return DeterministicSeedSurface(
                id = id,
                seedMaterial = seedMaterial,
                versionTuple = versionTuple,
                rootTimeEpochMillis = rootTimeEpochMillis,
                ratificationProof = ratificationProof,
            )
        }
    }
}