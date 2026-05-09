package metamodel.domain.port.outgoing

import metamodel.domain.vo.CanonicalTypeTextInspectionPolicy
import metamodel.domain.vo.CanonicalTypeTextInspectionResult

/**
 * Hexagonal Port: pinned Unicode normalization and canonical type-text inspection.
 *
 * This port is intentionally not a string normalizer.
 *
 * It is the deterministic boundary where high-entropy external text is inspected
 * and converted into machine-readable lexical facts before the metamodel domain
 * may issue CanonicalTypeText.
 *
 * The domain core must not depend on:
 *
 * - java.text.Normalizer;
 * - java.lang.Character Unicode tables;
 * - ICU APIs directly;
 * - java.lang.reflect.*;
 * - kotlin.reflect.*;
 * - KSP symbols;
 * - bytecode-library handles;
 * - JVM descriptor parsing;
 * - backend enumeration order;
 * - or host-runtime incidental Unicode behavior.
 *
 * Reflection and KSP are adapter concerns only.
 * KSP migration must not require changing this domain port.
 */
interface NormalizationEngine {
    /**
     * Stable engine identifier for diagnostics and order pinning.
     */
    val engineId: String

    /**
     * Stable behavior version.
     *
     * This must describe packaged inspection behavior, not merely the host JVM
     * version.
     */
    val engineVersion: String

    /**
     * Stable Unicode/profile version.
     */
    val unicodeProfileVersion: String

    /**
     * Identifier of the golden-vector set used to verify this implementation.
     */
    val goldenVectorSetId: String

    /**
     * Digest of the golden-vector set expected by this implementation.
     */
    val goldenVectorDigest: String

    /**
     * Cheap predicate for callers that need NFC checks outside canonical
     * type-text ratification.
     *
     * CanonicalTypeText.ratify(...) should normally not call this before
     * inspectCanonicalTypeText(...), because inspectCanonicalTypeText(...) is the
     * policy-aware unified boundary and owns snapshot capture.
     */
    fun isNfc(input: CharSequence): Boolean

    /**
     * Performs full canonical type-text inspection.
     *
     * Mandatory implementation order:
     *
     * 1. Read input.length.
     * 2. If input.length > policy.maxUtf16CodeUnitsBeforeSnapshot,
     *    return Rejected(LENGTH_LIMIT_EXCEEDED).
     * 3. Capture immutable snapshot with bounded copy.
     * 4. Re-check snapshot.length against policy.maxUtf16CodeUnitsBeforeSnapshot.
     * 5. Fast-fail invalid / unpaired surrogate structure.
     * 6. Perform exact scalar / code-point inspection.
     * 7. Enforce exact maxCodePoints.
     * 8. Perform NFC verification.
     * 9. Perform category / script / token / delimiter / density inspection.
     * 10. Perform gross combining mark guard before expensive grapheme-heavy logic.
     * 11. Perform per-token combining and grapheme-cluster inspection.
     *
     * Accepted MUST contain:
     *
     * - the immutable snapshot inspected by the engine;
     * - the lexical profile derived from that exact snapshot.
     */
    fun inspectCanonicalTypeText(
        input: CharSequence,
        policy: CanonicalTypeTextInspectionPolicy,
    ): CanonicalTypeTextInspectionResult
}
