package ir.identity

import ir.IrLimits
import ir.exception.IrProtocolViolationException
import java.util.Arrays

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
class CanonicalSignature(bytes: ByteArray) {

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