package kontrakt.planning.domain.protocol

import stage.lowering.diagnostics.EnvironmentIntegrityException

/**
 * [SSOT] Sentinel Safety Law.
 *
 * Problem:
 * - Many primitive tables reserve special values:
 *   - 0L often means EMPTY / UNSET
 *   - -1L (0xFFFF...) often means +INF / TERMINATOR
 *
 * If a real, computed key equals a reserved sentinel:
 * - collisions can cause data loss
 * - determinism can break (depending on insertion order / probing)
 *
 * Law:
 * - remapNonZero(value, versionSeed): remaps ONLY if value == 0L
 * - remapNonMax(value, versionSeed): remaps ONLY if value == -1L
 * - versionSeed is mandatory (no version-less remap API allowed).
 *
 * Fail-closed:
 * - If we cannot generate a non-reserved value within MAX_RETRIES, we throw.
 */
object SentinelRemapper {
    private const val RESERVED_EMPTY: Long = 0L
    private const val RESERVED_INF: Long = -1L

    private const val TAG_NON_ZERO: Long = 0x5E20_1D10_7A91L
    private const val TAG_NON_MAX: Long = 0x1A2B_3C4D_5E6FL

    private const val MAX_RETRIES: Int = 5

    /**
     * Ensures the returned value is not 0L and not -1L.
     */
    @JvmStatic
    fun remapNonZero(
        value: Long,
        versionSeed: Long,
    ): Long {
        if (value != RESERVED_EMPTY) return value

        var attempt = 0
        while (attempt < MAX_RETRIES) {
            val mixInput = versionSeed xor TAG_NON_ZERO xor attempt.toLong()
            val current = PrimitiveHash.mix64(mixInput)
            if (current != RESERVED_EMPTY && current != RESERVED_INF) return current
            attempt++
        }

        throw EnvironmentIntegrityException("Failed to remap 0L. Entropy failure.")
    }

    /**
     * Ensures the returned value is not 0L and not -1L.
     */
    @JvmStatic
    fun remapNonMax(
        value: Long,
        versionSeed: Long,
    ): Long {
        if (value != RESERVED_INF) return value

        var attempt = 0
        while (attempt < MAX_RETRIES) {
            val mixInput = versionSeed xor TAG_NON_MAX xor attempt.toLong()
            val current = PrimitiveHash.mix64(mixInput)
            if (current != RESERVED_EMPTY && current != RESERVED_INF) return current
            attempt++
        }

        throw EnvironmentIntegrityException("Failed to remap -1L. Entropy failure.")
    }
}
