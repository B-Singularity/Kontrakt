package planning.domain.protocol

/**
 * [SSOT] Golden Vectors for Planning Protocol / Constitution.
 *
 * Purpose:
 * - A protocol is only "real" if it can be pinned by concrete vectors.
 * - These vectors must pass bit-for-bit in every supported runtime.
 *
 * Coverage policy (minimum viable passable set):
 * - Encoding: UTF-8 + length prefix, NFC-REJECT, surrogate rejection.
 * - Hash: Murmur3 x64 128 tail lengths 0..15, plus multi-block boundaries.
 * - Sentinel: remap functions must never return reserved values.
 */
object ProtocolGoldenVectors {

    data class HashVector(
        val inputHex: String,
        val seed: Long,
        val expectedH1Hex: String,
        val expectedH2Hex: String,
        val description: String,
    )

    data class EncodingVector(
        val inputString: String,
        val expectedHex: String?, // null = REJECT
        val description: String,
    )

    enum class SentinelKind { NON_ZERO, NON_MAX }

    data class SentinelVector(
        val kind: SentinelKind,
        val input: Long,
        val seed: Long,
        val expectedRemappedHex: String,
        val description: String,
    )

    // ─────────────────────────────────────────────────────────────
    // Hash Vectors
    // Generated from MurmurHash3_x64_128 reference semantics (lower 64 = h1).
    // Seed uses low 32 bits only.
    // ─────────────────────────────────────────────────────────────

    val HASH_VECTORS: List<HashVector> = listOf(
        // Tail length 0..15 for "a" repeated, seed=0
        HashVector("", 0L, "0000000000000000", "0000000000000000", "Len 0 (empty)"),
        HashVector("61", 0L, "85555565f6597889", "e6b53a48510e895a", "Len 1 ('a')"),
        HashVector("6161", 0L, "2c91cb24366eb7a8", "6625d6db6916695c", "Len 2 ('aa')"),
        HashVector("616161", 0L, "136d696c010a2af6", "8e0915e545b2bc08", "Len 3"),
        HashVector("61616161", 0L, "f61cfdbfdae0f65e", "58f93db16236ba2b", "Len 4"),
        HashVector("6161616161", 0L, "416badf75f54c737", "bf9054d748a3e428", "Len 5"),
        HashVector("616161616161", 0L, "fbb97d784b1c59f4", "a54c211d6c1e6b1d", "Len 6"),
        HashVector("61616161616161", 0L, "c6be8a493af9714a", "40948f9d17425c71", "Len 7"),
        HashVector("6161616161616161", 0L, "187f343ff3b0d249", "b11e0e63e3aa0c34", "Len 8"),
        HashVector("616161616161616161", 0L, "0f3ae5442d91c557", "4ce49ced1def61db", "Len 9"),
        HashVector("61616161616161616161", 0L, "e1a55f48f1c10d5f", "47268a4343d49f44", "Len 10"),
        HashVector("6161616161616161616161", 0L, "f4cdd514303c5382", "41baebada3d81025", "Len 11"),
        HashVector("616161616161616161616161", 0L, "d8bb9d456ed6144b", "a6bc4c4bf6887b15", "Len 12"),
        HashVector("61616161616161616161616161", 0L, "d52d19e503eec6a0", "ed98335c09c83689", "Len 13"),
        HashVector("6161616161616161616161616161", 0L, "b0c344f01ce073be", "e19cbe0ac3a564fe", "Len 14"),
        HashVector("616161616161616161616161616161", 0L, "bd9fe677c36b6240", "46c1c1f5375b2115", "Len 15"),

        // Multi-block boundaries (seed=0)
        HashVector("61616161616161616161616161616161", 0L, "ec78db0c8b199e8a", "84cedd7dc194e391", "Len 16 (1 block)"),
        HashVector(
            "6161616161616161616161616161616161",
            0L,
            "7e45349e0f3b13e7",
            "48dda138e8168031",
            "Len 17 (1 block + 1 tail)"
        ),
        HashVector(
            "61616161616161616161616161616161616161616161616161616161616161",
            0L,
            "2f1c4cde0f73a10e",
            "db1b39fc408412ba",
            "Len 31 (1 block + 15 tail)"
        ),
        HashVector(
            "616161616161616161616161616161616161616161616161616161616161616161",
            0L,
            "504857b82da63359",
            "946261f5cf3e5261",
            "Len 33 (2 blocks + 1 tail)",
        ),

        // Seed diversity
        HashVector("74657374", 12345L, "b3dd93fa6464603d", "5a22b0ce644fb688", "Seed=12345, input='test'"),
        HashVector(
            "74657374",
            -1L,
            "fbcc84705faf0762",
            "77923427b407dd8a",
            "Seed=-1 (low32=0xFFFF_FFFF), input='test'"
        ),
    )

    // ─────────────────────────────────────────────────────────────
    // Encoding Vectors
    // expectedHex: [lenLE32][utf8Bytes]
    // ─────────────────────────────────────────────────────────────

    val ENCODING_VECTORS: List<EncodingVector> = listOf(
        EncodingVector("abc", "03000000616263", "ASCII"),
        EncodingVector("가", "03000000eab080", "Hangul (NFC)"),
        EncodingVector("\u1100\u1161", null, "Hangul (NFD) -> Reject"),
        EncodingVector("\uD800", null, "Unpaired high surrogate -> Reject"),
        EncodingVector("\uDC00", null, "Unpaired low surrogate -> Reject"),
        EncodingVector("👍", "04000000f09f918d", "Emoji"),
        EncodingVector("a\u0000b", "03000000610062", "Contains NULL (allowed)"),
    )

    // ─────────────────────────────────────────────────────────────
    // Sentinel Vectors
    // These values are derived from PrimitiveHash.mix64(tag ^ seed ^ attempt).
    // ─────────────────────────────────────────────────────────────

    val SENTINEL_VECTORS: List<SentinelVector> = listOf(
        SentinelVector(
            SentinelKind.NON_ZERO,
            0L,
            123L,
            "a52f271a264cfc93",
            "remapNonZero(0L, seed=123)",
        ),
        SentinelVector(
            SentinelKind.NON_MAX,
            -1L,
            456L,
            "a2ae2b2f6003d27d",
            "remapNonMax(-1L, seed=456)",
        ),
    )
}