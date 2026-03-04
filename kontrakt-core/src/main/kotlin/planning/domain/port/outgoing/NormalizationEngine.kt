package planning.domain.port.outgoing

/**
 * Hexagonal Port: Unicode normalization law checker.
 *
 * Why this exists:
 * - The domain protocol MUST NOT depend on a specific Unicode implementation (JDK, ICU, etc.).
 * - We still must enforce a deterministic "NFC-REJECT" policy.
 *
 * Contract:
 * - The engine MUST provide a stable, pinned behavior within a released protocol version.
 * - If the engine behavior changes across versions, that is a protocol version change.
 */
interface NormalizationEngine {

    /**
     * A stable identifier for diagnostics (e.g., "icu4j").
     */
    val engineId: String

    /**
     * A stable engine version string for diagnostics and pinning.
     *
     * This MUST correspond to the packaged engine version (not JVM version).
     */
    val engineVersion: String

    /**
     * Returns true if [input] is already NFC (Normalization Form C).
     *
     * IMPORTANT:
     * - This must be a pure predicate. The domain core MUST NOT normalize.
     * - The caller performs surrogate checks before calling this.
     */
    fun isNfc(input: String): Boolean
}