package stage.canonicalization.material.representation

import realization.graph.IrLimits
import realization.graph.IrProtocolViolationException
import stage.lowering.diagnostics.CanonicalContractViolationException
import stage.lowering.diagnostics.CanonicalVersionMismatchException
import versioning.coordinate.material.value.CanonicalVersionTuple
import java.util.Arrays

/**
 * Version-aware canonical signature.
 *
 * A CanonicalSignature is exact semantic equality material.
 *
 * It is not:
 * - a hash;
 * - an Identity64;
 * - a digest;
 * - a cache bucket key;
 * - a display label.
 *
 * The byte payload is defensively copied on input and output. Canonical byte
 * arrays must never retain caller-owned mutable arrays.
 */
class CanonicalSignature private constructor(
    val versionTuple: CanonicalVersionTuple,
    private val bytes: ByteArray,
) {
    val sizeBytes: Int
        get() = bytes.size

    fun copyBytes(): ByteArray = bytes.copyOf()

    /**
     * Exact byte comparison after mandatory version compatibility check.
     *
     * Version mismatch is a order failure, not a simple false result.
     */
    fun exactBytesEqual(other: CanonicalSignature): Boolean {
        requireSameVersionAs(other)
        return bytes.contentEquals(other.bytes)
    }

    fun requireSameVersionAs(other: CanonicalSignature) {
        if (!versionTuple.sameProtocolAs(other.versionTuple)) {
            throw CanonicalVersionMismatchException(
                storedVersion = other.versionTuple.renderForDiagnostics(),
                currentVersion = versionTuple.renderForDiagnostics(),
            )
        }
    }

    /**
     * Value-object equality.
     *
     * This intentionally does not throw on version mismatch.
     *
     * Kotlin/JVM collection equality and hashing APIs are not exception-safe
     * order comparison APIs. Therefore:
     *
     * - equals/hashCode provide value-object behavior;
     * - exactBytesEqual(...) provides order-gated semantic comparison;
     * - cache/interner exact-match paths should call exactBytesEqual(...), not
     *   rely only on generic equals(...).
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is CanonicalSignature) {
            return false
        }

        return versionTuple == other.versionTuple &&
                bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = versionTuple.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

    override fun toString(): String = "CanonicalSignature(version=$versionTuple, sizeBytes=${bytes.size})"

    companion object {
        @JvmStatic
        fun issue(
            versionTuple: CanonicalVersionTuple,
            bytes: ByteArray,
        ): CanonicalSignature {
            if (bytes.isEmpty()) {
                throw CanonicalContractViolationException(
                    reason = "CanonicalSignature bytes must not be empty.",
                )
            }

            return CanonicalSignature(
                versionTuple = versionTuple,
                bytes = bytes.copyOf(),
            )
        }
    }
}

/**
 * Canonical byte signature used for deterministic equality keys (cache, interning, etc.).
 *
 * Guarantees:
 * - Defensive copy on ingress
 * - Size limit enforcement
 * - Structural equality based on byte content
 *
 * Note:
 * - This is intentionally not a data class to avoid accidental copying semantics.
 */
class CanonicalSignature(
    bytes: ByteArray,
) {
    private val payload: ByteArray

    init {
        if (bytes.size > IrLimits.MAX_SIGNATURE_BYTES) {
            throw IrProtocolViolationException("Signature limit exceeded.")
        }
        this.payload = Arrays.copyOf(bytes, bytes.size)
    }

    fun bytesCopy(): ByteArray = Arrays.copyOf(payload, payload.size)

    fun contentEquals(other: CanonicalSignature): Boolean = Arrays.equals(this.payload, other.payload)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return Arrays.equals(payload, (other as CanonicalSignature).payload)
    }

    override fun hashCode(): Int = Arrays.hashCode(payload)

    override fun toString(): String {
        if (payload.isEmpty()) return "CanonicalSignature(empty)"
        val head = "%02x".format(payload[0].toInt() and 0xFF)
        val tail = "%02x".format(payload[payload.size - 1].toInt() and 0xFF)
        return "CanonicalSignature(len=${payload.size}, [$head...$tail])"
    }
}