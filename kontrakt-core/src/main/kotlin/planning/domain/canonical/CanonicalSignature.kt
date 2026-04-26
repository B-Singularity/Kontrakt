package planning.domain.canonical

import planning.domain.exception.CanonicalContractViolationException
import planning.domain.exception.CanonicalVersionMismatchException

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

    fun copyBytes(): ByteArray {
        return bytes.copyOf()
    }

    /**
     * Exact byte comparison after mandatory version compatibility check.
     *
     * Version mismatch is a protocol failure, not a simple false result.
     */
    fun exactBytesEqual(
        other: CanonicalSignature,
    ): Boolean {
        requireSameVersionAs(other)
        return bytes.contentEquals(other.bytes)
    }

    fun requireSameVersionAs(
        other: CanonicalSignature,
    ) {
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
     * protocol comparison APIs. Therefore:
     *
     * - equals/hashCode provide value-object behavior;
     * - exactBytesEqual(...) provides protocol-gated semantic comparison;
     * - cache/interner exact-match paths should call exactBytesEqual(...), not
     *   rely only on generic equals(...).
     */
    override fun equals(
        other: Any?,
    ): Boolean {
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

    override fun toString(): String {
        return "CanonicalSignature(version=$versionTuple, sizeBytes=${bytes.size})"
    }

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