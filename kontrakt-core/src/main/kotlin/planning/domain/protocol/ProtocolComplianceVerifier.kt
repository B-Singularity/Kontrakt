package planning.domain.protocol

import kontrakt.planning.domain.protocol.HashInputEncodingSpec
import kontrakt.planning.domain.protocol.PrimitiveHash
import kontrakt.planning.domain.protocol.SentinelRemapper
import planning.domain.exception.EnvironmentIntegrityException
import planning.domain.exception.PortContractViolationException

/**
 * Runtime pinning gate.
 *
 * This is an optional but strongly recommended boot-time verification:
 * - Verifies encoding golden vectors (including rejection cases)
 * - Verifies hashing golden vectors (h1 + h2)
 * - Verifies sentinel remapping golden vectors
 *
 * Where to call:
 * - Application composition root / bootstrap (NOT as a global init in domain).
 *
 * Why not a global init:
 * - Global init makes dependency injection awkward and violates hexagonal boundaries.
 * - Boot-time verification should be explicitly triggered by the host application.
 */
object ProtocolComplianceVerifier {

    @JvmStatic
    fun verifyAll(encoding: HashInputEncodingSpec) {
        verifyEncodingVectors(encoding)
        verifyHashVectors()
        verifySentinelVectors()
    }

    private fun verifyEncodingVectors(encoding: HashInputEncodingSpec) {
        for (v in ProtocolGoldenVectors.ENCODING_VECTORS) {
            try {
                val bytes = encoding.encodeStrict(v.inputString)
                val hex = toHex(bytes)

                if (v.expectedHex == null) {
                    throw EnvironmentIntegrityException(
                        "Runtime violation: accepted input that MUST be rejected. desc=${v.description}"
                    )
                }
                if (!hex.equals(v.expectedHex, ignoreCase = true)) {
                    throw EnvironmentIntegrityException(
                        "Runtime violation: encoding mismatch. desc=${v.description} expected=${v.expectedHex} got=$hex"
                    )
                }
            } catch (e: PortContractViolationException) {
                if (v.expectedHex != null) {
                    throw EnvironmentIntegrityException(
                        "Runtime violation: rejected input that MUST be accepted. desc=${v.description}",
                        e,
                    )
                }
            }
        }
    }

    private fun verifyHashVectors() {
        val out = LongArray(2)

        for (v in ProtocolGoldenVectors.HASH_VECTORS) {
            val input = hexToBytes(v.inputHex)

            PrimitiveHash.hash128Into(input, v.seed, out, 0)
            val h1 = out[0]
            val h2 = out[1]

            val expectedH1 = parseHexU64ToLong(v.expectedH1Hex)
            val expectedH2 = parseHexU64ToLong(v.expectedH2Hex)

            if (h1 != expectedH1 || h2 != expectedH2) {
                throw EnvironmentIntegrityException(
                    "Runtime violation: hash mismatch. desc=${v.description} " +
                            "expected(h1,h2)=(${v.expectedH1Hex},${v.expectedH2Hex}) " +
                            "got(h1,h2)=(${h1.toULong().toString(16).padStart(16, '0')}," +
                            "${h2.toULong().toString(16).padStart(16, '0')})"
                )
            }

            // Additional invariant: hash64 == out[0]
            val hot = PrimitiveHash.hash64(input, v.seed)
            if (hot != h1) {
                throw EnvironmentIntegrityException(
                    "Runtime violation: hash64 != hash128Into[0]. desc=${v.description}"
                )
            }
        }
    }

    private fun verifySentinelVectors() {
        for (v in ProtocolGoldenVectors.SENTINEL_VECTORS) {
            val remapped = when (v.kind) {
                ProtocolGoldenVectors.SentinelKind.NON_ZERO -> SentinelRemapper.remapNonZero(v.input, v.seed)
                ProtocolGoldenVectors.SentinelKind.NON_MAX -> SentinelRemapper.remapNonMax(v.input, v.seed)
            }

            val expected = parseHexU64ToLong(v.expectedRemappedHex)
            if (remapped != expected) {
                throw EnvironmentIntegrityException(
                    "Runtime violation: sentinel remap mismatch. desc=${v.description} " +
                            "expected=${v.expectedRemappedHex} got=${remapped.toULong().toString(16)}"
                )
            }

            // Must never be reserved
            if (remapped == 0L || remapped == -1L) {
                throw EnvironmentIntegrityException(
                    "Runtime violation: sentinel remap produced reserved value. desc=${v.description}"
                )
            }
        }
    }

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString(separator = "") { b -> (b.toInt() and 0xFF).toString(16).padStart(2, '0') }

    private fun hexToBytes(hex: String): ByteArray {
        if (hex.isEmpty()) return ByteArray(0)
        require(hex.length % 2 == 0) { "Hex length must be even: len=${hex.length}" }

        val out = ByteArray(hex.length / 2)
        var i = 0
        var o = 0
        while (i < hex.length) {
            out[o] = hex.substring(i, i + 2).toInt(16).toByte()
            i += 2
            o += 1
        }
        return out
    }

    private fun parseHexU64ToLong(hex: String): Long =
        hex.toULong(16).toLong()
}