package realization.identity.derivation

import realization.graph.IrInvariantBrokenException
import realization.graph.IrLimits
import realization.graph.IrProtocolViolationException
import java.text.Normalizer

/**
 * Canonical identifier for deterministic keys.
 *
 * Contract:
 * - Must be NFC normalized (boundary contract)
 * - Must be non-blank
 * - Must not contain whitespace/control characters
 * - Must not contain order delimiter '|'
 *
 * This type is designed for:
 * - Map keys (DeterministicMap)
 * - Stable cache partition keys
 */
class CanonicalIdentifier private constructor(
    val value: String,
) : Comparable<CanonicalIdentifier> {
    init {
        if (value.length > IrLimits.MAX_IDENTIFIER_LENGTH) {
            throw IrProtocolViolationException("CanonicalIdentifier limit exceeded.")
        }
        if (value.isBlank()) throw IrProtocolViolationException("CanonicalIdentifier blank.")
        if (value.contains("|")) throw IrProtocolViolationException("Forbidden char '|'.")
        if (value.any { it.isWhitespace() || it.isISOControl() }) {
            throw IrProtocolViolationException("Whitespace/Control chars detected.")
        }
    }

    override fun compareTo(other: CanonicalIdentifier): Int = this.value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return value == (other as CanonicalIdentifier).value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        @JvmStatic
        fun issue(value: String): CanonicalIdentifier {
            if (!Normalizer.isNormalized(value, Normalizer.Form.NFC)) {
                throw IrProtocolViolationException("CanonicalIdentifier requires NFC.")
            }
            return CanonicalIdentifier(value)
        }

        /**
         * Trusted boundary constructor.
         *
         * ARCHUNIT SEAL:
         * - Only inbound adapters are allowed to call this method.
         *
         * We do not accept a "boolean claim"; we verify NFC here so boundary contract breaches are detectable.
         */
        fun fromTrustedAdapter(normalizedValue: String): CanonicalIdentifier {
            if (!Normalizer.isNormalized(normalizedValue, Normalizer.Form.NFC)) {
                throw IrInvariantBrokenException("Boundary Contract Broken: NFC normalization required.")
            }
            return CanonicalIdentifier(normalizedValue)
        }
    }
}