package kontrakt.planning.domain.protocol

/**
 * [SSOT] Deterministic hashing primitives.
 *
 * Algorithm:
 * - MurmurHash3 x64 128-bit variant (SMHasher / reference C++ implementation).
 *
 * Output contract:
 * - hash64() returns out[0] (h1) of the 128-bit output.
 * - hash128Into() is used for golden verification without forcing allocations.
 *
 * Seed contract:
 * - Reference uses 32-bit seed. We consume only the low 32 bits of the provided Long.
 * - Higher bits are ignored intentionally to remain bit-for-bit with SMHasher.
 *
 * Performance law:
 * - hash64() MUST be allocation-free (no arrays, no wrappers).
 * - hash128Into() is the verification API: allocation-free if caller supplies buffer.
 */
object PrimitiveHash {
    // MurmurHash3_x64_128 constants
    private const val C1: Long = -0x783c846eeebdac2bL // 0x87c37b91114253d5
    private const val C2: Long = 0x4cf5ad432745937fL // 0x4cf5ad432745937f

    /**
     * Returns out[0] (h1) of MurmurHash3_x64_128.
     *
     * Allocation-free hot-path API.
     */
    @JvmStatic
    fun hash64(
        data: ByteArray,
        seed: Long,
    ): Long {
        val seed32 = seed and 0xFFFF_FFFFL // unsigned 32-bit seed
        var h1 = seed32
        var h2 = seed32

        val len = data.size
        var i = 0

        // Body: 16-byte blocks
        while (i + 15 < len) {
            val k1 = getLongLE(data, i)
            val k2 = getLongLE(data, i + 8)

            h1 = h1 xor mixK1(k1)
            h1 = java.lang.Long.rotateLeft(h1, 27)
            h1 += h2
            h1 = h1 * 5 + 0x52dce729L

            h2 = h2 xor mixK2(k2)
            h2 = java.lang.Long.rotateLeft(h2, 31)
            h2 += h1
            h2 = h2 * 5 + 0x38495ab5L

            i += 16
        }

        // Tail
        var k1 = 0L
        var k2 = 0L
        val rem = len - i

        // Matches the reference fall-through semantics.
        when (rem) {
            15 -> {
                k2 = k2 xor ((data[i + 14].toLong() and 0xFFL) shl 48)
                k2 = k2 xor ((data[i + 13].toLong() and 0xFFL) shl 40)
                k2 = k2 xor ((data[i + 12].toLong() and 0xFFL) shl 32)
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            14 -> {
                k2 = k2 xor ((data[i + 13].toLong() and 0xFFL) shl 40)
                k2 = k2 xor ((data[i + 12].toLong() and 0xFFL) shl 32)
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            13 -> {
                k2 = k2 xor ((data[i + 12].toLong() and 0xFFL) shl 32)
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            12 -> {
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            11 -> {
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            10 -> {
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            9 -> {
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            8 -> {
                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            7 -> {
                k1 = k1 xor ((data[i + 6].toLong() and 0xFFL) shl 48)
                k1 = k1 xor ((data[i + 5].toLong() and 0xFFL) shl 40)
                k1 = k1 xor ((data[i + 4].toLong() and 0xFFL) shl 32)
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            6 -> {
                k1 = k1 xor ((data[i + 5].toLong() and 0xFFL) shl 40)
                k1 = k1 xor ((data[i + 4].toLong() and 0xFFL) shl 32)
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            5 -> {
                k1 = k1 xor ((data[i + 4].toLong() and 0xFFL) shl 32)
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            4 -> {
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            3 -> {
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            2 -> {
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            1 -> {
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }
        }

        // Finalization
        h1 = h1 xor len.toLong()
        h2 = h2 xor len.toLong()

        h1 += h2
        h2 += h1

        h1 = fmix64(h1)
        h2 = fmix64(h2)

        h1 += h2
        h2 += h1

        return h1
    }

    /**
     * Computes both words (out[0], out[1]) of MurmurHash3_x64_128 into a caller-provided buffer.
     *
     * This is the allocation-free verification API.
     *
     * Buffer contract:
     * - out.size >= outOffset + 2
     */
    @JvmStatic
    internal fun hash128Into(
        data: ByteArray,
        seed: Long,
        out: LongArray,
        outOffset: Int = 0,
    ) {
        require(outOffset >= 0 && outOffset + 1 < out.size) {
            "Output buffer too small: need at least 2 longs at offset=$outOffset"
        }

        val seed32 = seed and 0xFFFF_FFFFL
        var h1 = seed32
        var h2 = seed32

        val len = data.size
        var i = 0

        while (i + 15 < len) {
            val k1 = getLongLE(data, i)
            val k2 = getLongLE(data, i + 8)

            h1 = h1 xor mixK1(k1)
            h1 = java.lang.Long.rotateLeft(h1, 27)
            h1 += h2
            h1 = h1 * 5 + 0x52dce729L

            h2 = h2 xor mixK2(k2)
            h2 = java.lang.Long.rotateLeft(h2, 31)
            h2 += h1
            h2 = h2 * 5 + 0x38495ab5L

            i += 16
        }

        var k1 = 0L
        var k2 = 0L
        val rem = len - i

        when (rem) {
            15 -> {
                k2 = k2 xor ((data[i + 14].toLong() and 0xFFL) shl 48)
                k2 = k2 xor ((data[i + 13].toLong() and 0xFFL) shl 40)
                k2 = k2 xor ((data[i + 12].toLong() and 0xFFL) shl 32)
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            14 -> {
                k2 = k2 xor ((data[i + 13].toLong() and 0xFFL) shl 40)
                k2 = k2 xor ((data[i + 12].toLong() and 0xFFL) shl 32)
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            13 -> {
                k2 = k2 xor ((data[i + 12].toLong() and 0xFFL) shl 32)
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            12 -> {
                k2 = k2 xor ((data[i + 11].toLong() and 0xFFL) shl 24)
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            11 -> {
                k2 = k2 xor ((data[i + 10].toLong() and 0xFFL) shl 16)
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            10 -> {
                k2 = k2 xor ((data[i + 9].toLong() and 0xFFL) shl 8)
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            9 -> {
                k2 = k2 xor (data[i + 8].toLong() and 0xFFL)
                h2 = h2 xor mixK2(k2)

                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            8 -> {
                k1 = getLongLE(data, i)
                h1 = h1 xor mixK1(k1)
            }

            7 -> {
                k1 = k1 xor ((data[i + 6].toLong() and 0xFFL) shl 48)
                k1 = k1 xor ((data[i + 5].toLong() and 0xFFL) shl 40)
                k1 = k1 xor ((data[i + 4].toLong() and 0xFFL) shl 32)
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            6 -> {
                k1 = k1 xor ((data[i + 5].toLong() and 0xFFL) shl 40)
                k1 = k1 xor ((data[i + 4].toLong() and 0xFFL) shl 32)
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            5 -> {
                k1 = k1 xor ((data[i + 4].toLong() and 0xFFL) shl 32)
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            4 -> {
                k1 = k1 xor ((data[i + 3].toLong() and 0xFFL) shl 24)
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            3 -> {
                k1 = k1 xor ((data[i + 2].toLong() and 0xFFL) shl 16)
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            2 -> {
                k1 = k1 xor ((data[i + 1].toLong() and 0xFFL) shl 8)
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }

            1 -> {
                k1 = k1 xor (data[i].toLong() and 0xFFL)
                h1 = h1 xor mixK1(k1)
            }
        }

        h1 = h1 xor len.toLong()
        h2 = h2 xor len.toLong()

        h1 += h2
        h2 += h1

        h1 = fmix64(h1)
        h2 = fmix64(h2)

        h1 += h2
        h2 += h1

        out[outOffset] = h1
        out[outOffset + 1] = h2
    }

    /**
     * 64-bit avalanche mixer (fmix64 from MurmurHash3).
     *
     * This is used by SentinelRemapper as a deterministic, retryable mixer.
     */
    @JvmStatic
    fun mix64(k: Long): Long = fmix64(k)

    private fun mixK1(k: Long): Long {
        var x = k
        x *= C1
        x = java.lang.Long.rotateLeft(x, 31)
        x *= C2
        return x
    }

    private fun mixK2(k: Long): Long {
        var x = k
        x *= C2
        x = java.lang.Long.rotateLeft(x, 33)
        x *= C1
        return x
    }

    private fun fmix64(k: Long): Long {
        var x = k
        x = x xor (x ushr 33)
        x *= -0xae502812aa7333L // 0xff51afd7ed558ccd
        x = x xor (x ushr 33)
        x *= -0x3b314601e57a13adL // 0xc4ceb9fe1a85ec53
        x = x xor (x ushr 33)
        return x
    }

    /**
     * Reads a 64-bit little-endian value from [data] at [offset].
     *
     * This avoids ByteBuffer (endianness pitfalls, allocations, slower paths).
     */
    private fun getLongLE(
        data: ByteArray,
        offset: Int,
    ): Long =
        (data[offset].toLong() and 0xFFL) or
            ((data[offset + 1].toLong() and 0xFFL) shl 8) or
            ((data[offset + 2].toLong() and 0xFFL) shl 16) or
            ((data[offset + 3].toLong() and 0xFFL) shl 24) or
            ((data[offset + 4].toLong() and 0xFFL) shl 32) or
            ((data[offset + 5].toLong() and 0xFFL) shl 40) or
            ((data[offset + 6].toLong() and 0xFFL) shl 48) or
            ((data[offset + 7].toLong() and 0xFFL) shl 56)
}
