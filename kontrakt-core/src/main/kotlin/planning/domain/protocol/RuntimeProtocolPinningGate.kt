package planning.domain.protocol

import kontrakt.planning.domain.protocol.HashInputEncodingSpec
import kontrakt.planning.domain.protocol.PrimitiveHash
import kontrakt.planning.domain.protocol.SentinelRemapper
import planning.domain.exception.EnvironmentIntegrityException
import planning.domain.exception.PortContractViolationException

/**
 * Boot-time runtime pinning gate for order-critical primitives.
 *
 * Purpose:
 * - Verify that the *actual* runtime environment honors the pinned order assets.
 * - Fail closed before application work begins if the environment drifts.
 *
 * Scope:
 * - Strict encoding vectors (including REJECT cases)
 * - MurmurHash3 x64 128-bit golden vectors
 * - Sentinel remapping golden vectors
 *
 * Where to call:
 * - Application composition root / bootstrap
 *
 * Where NOT to call:
 * - Global static init of the domain module
 *
 * Rationale:
 * - This keeps dependency injection explicit and preserves hexagonal boundaries.
 * - Verification is host-triggered, not hidden in global initialization.
 */
object RuntimeProtocolPinningGate {
    /**
     * Verifies all order-critical runtime pinning laws.
     *
     * Throws [EnvironmentIntegrityException] on any drift.
     */
    @JvmStatic
    fun verifyAll(encoding: HashInputEncodingSpec) {
        verifyEncodingVectors(encoding)
        verifyHashVectors()
        verifySentinelVectors()
    }

    /**
     * Verifies UTF-8 + LE32 length-prefix encoding and REJECT behavior.
     *
     * Rules:
     * - accepted vectors must encode to the exact pinned hex payload
     * - rejected vectors must fail specifically with PortContractViolationException
     */
    @JvmStatic
    fun verifyEncodingVectors(encoding: HashInputEncodingSpec) {
        for (vector in ProtocolGoldenVectors.ENCODING_VECTORS) {
            try {
                val bytes = encoding.encodeStrict(vector.inputString)
                val actualHex = bytes.toLowerHex()

                if (vector.expectedHex == null) {
                    throw EnvironmentIntegrityException(
                        "Runtime order drift: accepted input that MUST be rejected. " +
                                "description='${vector.description}'",
                    )
                }

                if (!actualHex.equals(vector.expectedHex, ignoreCase = true)) {
                    throw EnvironmentIntegrityException(
                        "Runtime order drift: encoding mismatch. " +
                                "description='${vector.description}', expected='${vector.expectedHex}', actual='$actualHex'",
                    )
                }
            } catch (e: PortContractViolationException) {
                if (vector.expectedHex != null) {
                    throw EnvironmentIntegrityException(
                        "Runtime order drift: rejected input that MUST be accepted. " +
                                "description='${vector.description}'",
                        e,
                    )
                }
            }
        }
    }

    /**
     * Verifies MurmurHash3 x64 128-bit outputs against golden vectors.
     *
     * Additional invariant:
     * - hash64(input, seed) MUST equal hash128Into(...)[0]
     */
    @JvmStatic
    fun verifyHashVectors() {
        val out = LongArray(2)

        for (vector in ProtocolGoldenVectors.HASH_VECTORS) {
            val input = vector.inputHex.hexToBytes()

            PrimitiveHash.hash128Into(input, vector.seed, out, 0)

            val actualH1 = out[0]
            val actualH2 = out[1]
            val expectedH1 = vector.expectedH1Hex.parseHexU64ToLong()
            val expectedH2 = vector.expectedH2Hex.parseHexU64ToLong()

            if (actualH1 != expectedH1 || actualH2 != expectedH2) {
                throw EnvironmentIntegrityException(
                    "Runtime order drift: hash mismatch. " +
                            "description='${vector.description}', " +
                            "expected(h1,h2)=(${vector.expectedH1Hex},${vector.expectedH2Hex}), " +
                            "actual(h1,h2)=(${actualH1.toUnsignedHex()},${actualH2.toUnsignedHex()})",
                )
            }

            val hotPath = PrimitiveHash.hash64(input, vector.seed)
            if (hotPath != actualH1) {
                throw EnvironmentIntegrityException(
                    "Runtime order drift: hash64 != hash128Into()[0]. " +
                            "description='${vector.description}', " +
                            "hash64=${hotPath.toUnsignedHex()}, h1=${actualH1.toUnsignedHex()}",
                )
            }
        }
    }

    /**
     * Verifies deterministic sentinel remapping.
     *
     * Rules:
     * - reserved inputs must map to the exact pinned remapped output
     * - pass-through inputs must remain identity-preserving
     * - remapped outputs must never be reserved sentinels
     */
    @JvmStatic
    fun verifySentinelVectors() {
        for (vector in ProtocolGoldenVectors.SENTINEL_VECTORS) {
            val actual =
                when (vector.kind) {
                    ProtocolGoldenVectors.SentinelKind.NON_ZERO ->
                        SentinelRemapper.remapNonZero(vector.input, vector.seed)

                    ProtocolGoldenVectors.SentinelKind.NON_MAX ->
                        SentinelRemapper.remapNonMax(vector.input, vector.seed)
                }

            val expected = vector.expectedRemappedHex.parseHexU64ToLong()

            if (actual != expected) {
                throw EnvironmentIntegrityException(
                    "Runtime order drift: sentinel remap mismatch. " +
                            "description='${vector.description}', " +
                            "expected='${vector.expectedRemappedHex}', actual='${actual.toUnsignedHex()}'",
                )
            }

            if (actual == 0L || actual == -1L) {
                throw EnvironmentIntegrityException(
                    "Runtime order drift: sentinel remap produced a reserved value. " +
                            "description='${vector.description}', actual='${actual.toUnsignedHex()}'",
                )
            }
        }
    }

    private fun ByteArray.toLowerHex(): String =
        joinToString(separator = "") { byte ->
            (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
        }

    private fun Long.toUnsignedHex(): String = this.toULong().toString(16).padStart(16, '0')

    private fun String.hexToBytes(): ByteArray {
        if (isEmpty()) return ByteArray(0)
        if (length % 2 != 0) {
            throw EnvironmentIntegrityException(
                "Invalid order asset: hex length must be even. length=$length",
            )
        }

        val out = ByteArray(length / 2)
        var src = 0
        var dst = 0
        while (src < length) {
            out[dst] = substring(src, src + 2).toInt(16).toByte()
            src += 2
            dst += 1
        }
        return out
    }

    private fun String.parseHexU64ToLong(): Long = toULong(16).toLong()
}
