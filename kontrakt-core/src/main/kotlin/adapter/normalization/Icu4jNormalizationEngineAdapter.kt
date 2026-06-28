package adapter.normalization

import adapter.normalization.Icu4jNormalizationEngineAdapter.Companion.MAX_RETAINED_SCRATCH_CODE_POINTS
import adapter.normalization.Icu4jNormalizationEngineAdapter.Companion.SCRIPT_POLICY_KONTRAKT_TYPE_TEXT_V1
import com.ibm.icu.lang.UCharacter
import com.ibm.icu.lang.UCharacterCategory
import com.ibm.icu.text.Normalizer2
import com.ibm.icu.util.VersionInfo
import metamodel.domain.port.outgoing.NormalizationEngine
import stage.canonicalization.material.CanonicalTypeLexicalProfile
import stage.canonicalization.material.CanonicalTypeTextInspectionPolicy
import stage.canonicalization.material.CanonicalTypeTextInspectionResult
import stage.canonicalization.material.CanonicalTypeTextViolationCode

/**
 * ICU4J-backed implementation of [metamodel.domain.port.outgoing.NormalizationEngine].
 *
 * This is an adapter.
 *
 * The metamodel domain owns the [metamodel.domain.port.outgoing.NormalizationEngine] port and the canonical
 * type-text value objects. This adapter owns the concrete ICU4J-backed Unicode
 * inspection profile.
 *
 * Architectural boundary:
 *
 * - Domain Core defines the port.
 * - This adapter implements the port.
 * - ICU4J stays outside the domain model.
 * - Reflection/KSP/bytecode/source handles never cross this boundary.
 *
 * This adapter does not issue:
 *
 * - CanonicalTypeText
 * - CanonicalTypeId
 * - TypeCycleKey
 * - CanonicalTypeSignature
 * - TypeReference
 *
 * It only returns:
 *
 * - [CanonicalTypeTextInspectionResult.Accepted] with an immutable snapshot and
 *   lexical profile; or
 * - [CanonicalTypeTextInspectionResult.Rejected] with a stable violation code
 *   and bounded diagnostic reason.
 *
 * NFC law:
 *
 * Kontrakt uses NFC-REJECT, not NFC-REPAIR.
 *
 * This adapter never normalizes, repairs, lowercases, trims, canonicalizes, or
 * rewrites input. It only checks whether the captured snapshot is already NFC.
 *
 * Snapshot / TOCTOU law:
 *
 * The port accepts [CharSequence] because adapters may receive mutable text
 * buffers. This adapter therefore:
 *
 * 1. reads the initial length;
 * 2. rejects impossible length before snapshot;
 * 3. copies into an immutable [String] snapshot;
 * 4. re-checks the snapshot length;
 * 5. inspects only the snapshot from that point onward;
 * 6. returns the exact snapshot in [CanonicalTypeTextInspectionResult.Accepted].
 *
 * Mandatory inspection order:
 *
 * This adapter follows the order required by [metamodel.domain.port.outgoing.NormalizationEngine]:
 *
 * 1. read input length;
 * 2. early length cap;
 * 3. snapshot capture;
 * 4. snapshot length recheck;
 * 5. surrogate validation;
 * 6. scalar/code-point inspection;
 * 7. maxCodePoints enforcement;
 * 8. NFC verification;
 * 9. category/script/token/delimiter/density inspection;
 * 10. gross combining mark guard;
 * 11. per-token combining/grapheme inspection.
 *
 * Optimization law:
 *
 * Logical order and physical implementation are deliberately separated.
 *
 * The scalar pass stores decoded Unicode scalar values into a primitive
 * [IntArray] scratch buffer. NFC verification still uses the immutable [String]
 * snapshot. The lexical pass then consumes the buffered code points instead of
 * rescanning UTF-16 and decoding surrogate pairs again.
 *
 * This preserves the mandated inspection order while avoiding duplicate
 * surrogate decoding.
 *
 * Scanner law:
 *
 * Lexical inspection is implemented as a private adapter-local primitive state
 * machine.
 *
 * The state machine uses primitive Int modes and explicit transition legality.
 * It does not allocate transition objects.
 *
 * It is not:
 *
 * - a domain service;
 * - a domain state machine;
 * - a planning state machine;
 * - a parser API;
 * - or a public order object.
 *
 * Scratch allocation law:
 *
 * A [ThreadLocal] scratch buffer is used only inside this adapter. It is not
 * semantic state. It is not observable by the domain. It is not reused across
 * threads.
 *
 * To avoid retaining unexpectedly large arrays forever, this adapter only
 * reuses arrays up to [MAX_RETAINED_SCRATCH_CODE_POINTS]. Larger inputs allocate
 * a temporary bounded array for that call.
 *
 * Scratch cleanup law:
 *
 * ThreadLocal cleanup is current-thread scoped.
 *
 * [clearCurrentThreadScratch] removes only the scratch buffer associated with
 * the calling thread. It is therefore useful at worker/session teardown points,
 * but it must not be treated as global adapter shutdown.
 *
 * The domain [metamodel.domain.port.outgoing.NormalizationEngine] port intentionally does not expose cleanup.
 * Scratch cleanup is adapter-local infrastructure hygiene.
 *
 * Script policy law:
 *
 * This implementation supports only [SCRIPT_POLICY_KONTRAKT_TYPE_TEXT_V1].
 *
 * v1 is intentionally ASCII-only for identifier material. Under this policy,
 * grapheme clusters per identifier token equal identifier-token code points.
 *
 * If a future policy admits non-ASCII identifier material, do not reuse this
 * scanner. Add a new policy token and implement a dedicated ICU BreakIterator-
 * backed token/grapheme inspection path.
 *
 * Diagnostic law:
 *
 * This adapter emits stable [CanonicalTypeTextViolationCode] values directly.
 * It never parses exception messages to infer violation codes.
 */
class Icu4jNormalizationEngineAdapter private constructor(
    override val engineId: String,
    override val engineVersion: String,
    override val unicodeProfileVersion: String,
    override val goldenVectorSetId: String,
    override val goldenVectorDigest: String,
    private val nfc: Normalizer2,
) : NormalizationEngine {
    override fun isNfc(
        input: CharSequence,
    ): Boolean {
        return nfc.isNormalized(input.toString())
    }

    override fun inspectCanonicalTypeText(
        input: CharSequence,
        policy: CanonicalTypeTextInspectionPolicy,
    ): CanonicalTypeTextInspectionResult {
        val initialLength = input.length

        if (initialLength == 0) {
            return rejected(
                code = CanonicalTypeTextViolationCode.EMPTY_INPUT,
                reason = "canonical type text is empty",
            )
        }

        if (initialLength > policy.maxUtf16CodeUnitsBeforeSnapshot) {
            return rejected(
                code = CanonicalTypeTextViolationCode.LENGTH_LIMIT_EXCEEDED,
                reason = "input length exceeds pre-snapshot UTF-16 cap",
            )
        }

        /*
         * Immutable bounded snapshot capture.
         *
         * After this line, do not read from input again. This is the TOCTOU
         * boundary for mutable CharSequence implementations.
         */
        val snapshot = input.toString()

        if (snapshot.isEmpty()) {
            return rejected(
                code = CanonicalTypeTextViolationCode.EMPTY_INPUT,
                reason = "captured canonical type text snapshot is empty",
            )
        }

        if (snapshot.length > policy.maxUtf16CodeUnitsBeforeSnapshot) {
            return rejected(
                code = CanonicalTypeTextViolationCode.LENGTH_LIMIT_EXCEEDED,
                reason = "snapshot length exceeds UTF-16 cap",
            )
        }

        val scratch =
            acquireScratch(
                requestedCapacity = policy.maxCodePoints,
            )

        val scalarInspection =
            inspectScalarsAndBufferCodePoints(
                snapshot = snapshot,
                policy = policy,
                scratch = scratch,
            )

        if (scalarInspection is ScalarInspectionResult.Rejected) {
            return scalarInspection.result
        }

        val scalarAccepted =
            scalarInspection as ScalarInspectionResult.Accepted

        /*
         * NFC is intentionally after surrogate/scalar/code-point inspection and
         * before Unicode category/script/token inspection, matching the port
         * contract.
         */
        if (!nfc.isNormalized(snapshot)) {
            return rejected(
                code = CanonicalTypeTextViolationCode.NON_NFC,
                reason = "canonical type text is not NFC",
            )
        }

        val lexicalInspection =
            LexicalScanner(
                snapshot = snapshot,
                codePoints = scratch.codePoints,
                codePointCount = scalarAccepted.codePointCount,
                policy = policy,
            ).scan()

        if (lexicalInspection is LexicalInspectionResult.Rejected) {
            return lexicalInspection.result
        }

        val acceptedLexicalInspection =
            lexicalInspection as LexicalInspectionResult.Accepted

        val lexicalProfile =
            try {
                CanonicalTypeLexicalProfile.accepted(
                    isNfc = true,
                    utf16CodeUnitCount = snapshot.length,
                    codePointCount = acceptedLexicalInspection.codePointCount,
                    identifierTokenCount =
                        acceptedLexicalInspection.identifierTokenCount,
                    longestIdentifierTokenCodePoints =
                        acceptedLexicalInspection.longestIdentifierTokenCodePoints,
                    totalDelimiterCodePoints =
                        acceptedLexicalInspection.totalDelimiterCodePoints,
                    nonIdentifierCodePointCount =
                        acceptedLexicalInspection.nonIdentifierCodePointCount,
                    grossCombiningMarkCount =
                        acceptedLexicalInspection.grossCombiningMarkCount,
                    maxCombiningMarksPerIdentifierToken =
                        acceptedLexicalInspection.maxCombiningMarksPerIdentifierToken,
                    maxGraphemeClustersPerIdentifierToken =
                        acceptedLexicalInspection.maxGraphemeClustersPerIdentifierToken,
                    genericDepth = acceptedLexicalInspection.genericDepth,
                    hasGenericDelimiters =
                        acceptedLexicalInspection.hasGenericDelimiters,
                    hasArraySuffix = acceptedLexicalInspection.hasArraySuffix,
                    hasNullableMarker = acceptedLexicalInspection.hasNullableMarker,
                    hasStarProjection = acceptedLexicalInspection.hasStarProjection,
                    hasAsciiWhitespace = acceptedLexicalInspection.hasAsciiWhitespace,
                    hasSourceVarianceToken =
                        acceptedLexicalInspection.hasSourceVarianceToken,
                )
            } catch (exception: RuntimeException) {
                /*
                 * The scanner is supposed to produce a self-consistent profile.
                 * If the profile VO rejects it, the adapter violated its own
                 * contract. Return a stable code instead of parsing exception
                 * messages into policy semantics.
                 */
                return rejected(
                    code = CanonicalTypeTextViolationCode.POLICY_CONTRACT_VIOLATION,
                    reason = "lexical profile construction failed",
                )
            }

        /*
         * Do not call lexicalProfile.requireWithinPolicy(policy) here.
         *
         * The scanner emits policy violations directly with stable violation
         * codes. CanonicalTypeText.ratify(...) performs the defensive final
         * cross-check. If that check fails after an Accepted result, it is an
         * engine contract violation, not an adapter-side diagnostic mapping task.
         */
        return CanonicalTypeTextInspectionResult.Accepted.issue(
            snapshot = snapshot,
            lexicalProfile = lexicalProfile,
        )
    }

    /**
     * Clears this adapter's scratch buffer for the current thread only.
     *
     * This method exists because the adapter uses ThreadLocal scratch storage to
     * avoid repeated IntArray allocation during canonical type-text inspection.
     *
     * Important:
     *
     * - This does not clear scratch buffers owned by other worker threads.
     * - This must be called from the same thread that used the adapter.
     * - Calling this from a coordinator/main thread does not clean worker-thread
     *   ThreadLocal values.
     * - This is adapter lifecycle hygiene, not a domain-port contract.
     *
     * Intended call sites:
     *
     * - worker task finally block;
     * - planner/metamodel session teardown running on the worker thread;
     * - test fixture teardown when the same test thread used the adapter.
     */
    fun clearCurrentThreadScratch() {
        THREAD_LOCAL_SCRATCH.remove()
    }

    private fun inspectScalarsAndBufferCodePoints(
        snapshot: String,
        policy: CanonicalTypeTextInspectionPolicy,
        scratch: CodePointScratch,
    ): ScalarInspectionResult {
        var index = 0
        var codePointCount = 0

        while (index < snapshot.length) {
            val decoded =
                decodeCodePoint(
                    text = snapshot,
                    index = index,
                )

            if (decoded is DecodedCodePoint.Invalid) {
                return ScalarInspectionResult.Rejected(
                    rejected(
                        code = CanonicalTypeTextViolationCode.INVALID_SURROGATE,
                        reason = decoded.reason,
                    ),
                )
            }

            val valid = decoded as DecodedCodePoint.Valid

            codePointCount += 1

            if (codePointCount > policy.maxCodePoints) {
                return ScalarInspectionResult.Rejected(
                    rejected(
                        code = CanonicalTypeTextViolationCode.LENGTH_LIMIT_EXCEEDED,
                        reason = "code point count exceeds policy cap",
                    ),
                )
            }

            scratch.codePoints[codePointCount - 1] = valid.codePoint

            index += valid.consumedCodeUnits
        }

        return ScalarInspectionResult.Accepted(
            codePointCount = codePointCount,
        )
    }

    private fun acquireScratch(
        requestedCapacity: Int,
    ): CodePointScratch {
        if (requestedCapacity <= MAX_RETAINED_SCRATCH_CODE_POINTS) {
            val scratch = THREAD_LOCAL_SCRATCH.get()

            if (scratch.codePoints.size < requestedCapacity) {
                scratch.codePoints = IntArray(requestedCapacity)
            }

            return scratch
        }

        return CodePointScratch(
            codePoints = IntArray(requestedCapacity),
        )
    }

    /**
     * Adapter-local primitive lexical scanner state machine.
     *
     * The scanner has explicit modes and explicit transition legality, but keeps
     * modes as primitive Int values to avoid per-transition allocation and to
     * make hot-path behavior predictable.
     */
    private inner class LexicalScanner(
        private val snapshot: String,
        private val codePoints: IntArray,
        private val codePointCount: Int,
        private val policy: CanonicalTypeTextInspectionPolicy,
    ) {
        private var mode: Int = LexicalScannerModeLaw.MODE_EXPECT_TOKEN

        private var position: Int = 0
        private var utf16Index: Int = 0

        private var identifierTokenCount: Int = 0
        private var currentIdentifierTokenCodePoints: Int = 0
        private var currentIdentifierStartIndex: Int = NO_ACTIVE_IDENTIFIER_START
        private var previousStructuralDelimiter: Int = NO_PREVIOUS_STRUCTURAL_DELIMITER
        private var longestIdentifierTokenCodePoints: Int = 0

        private var totalDelimiterCodePoints: Int = 0
        private var nonIdentifierCodePointCount: Int = 0

        private var grossCombiningMarkCount: Int = 0
        private var currentIdentifierCombiningMarks: Int = 0
        private var maxCombiningMarksPerIdentifierToken: Int = 0

        private var genericDepth: Int = 0
        private var maxGenericDepth: Int = 0

        private var hasGenericDelimiters: Boolean = false
        private var hasNullableMarker: Boolean = false
        private var hasStarProjection: Boolean = false
        private var hasAsciiWhitespace: Boolean = false
        private var hasSourceVarianceToken: Boolean = false

        private var pendingArrayOpenBracket: Boolean = false
        private var sawArraySuffix: Boolean = false

        fun scan(): LexicalInspectionResult {
            if (policy.scriptPolicyToken != SCRIPT_POLICY_KONTRAKT_TYPE_TEXT_V1) {
                return reject(
                    code = CanonicalTypeTextViolationCode.POLICY_CONTRACT_VIOLATION,
                    reason = "unsupported canonical type text script policy",
                )
            }

            while (position < codePointCount) {
                val codePoint = codePoints[position]
                val consumedCodeUnits = utf16CodeUnitCountOf(codePoint)

                val arrayPreflight =
                    requireArraySuffixContinuationIfNeeded(
                        codePoint = codePoint,
                    )

                if (arrayPreflight is LexicalInspectionResult.Rejected) {
                    return arrayPreflight
                }

                val category = categoryOf(codePoint)

                val categoryPreflight =
                    rejectForbiddenCategoryOrWhitespace(
                        codePoint = codePoint,
                        category = category,
                    )

                if (categoryPreflight is LexicalInspectionResult.Rejected) {
                    return categoryPreflight
                }

                val combiningPreflight =
                    consumeCombiningMarkIfPresent(
                        category = category,
                    )

                if (combiningPreflight is LexicalInspectionResult.Rejected) {
                    return combiningPreflight
                }

                val transition =
                    consumeCodePoint(
                        codePoint = codePoint,
                    )

                if (transition is LexicalInspectionResult.Rejected) {
                    return transition
                }

                val densityPreflight =
                    requireNonIdentifierDensityWithinPolicy()

                if (densityPreflight is LexicalInspectionResult.Rejected) {
                    return densityPreflight
                }

                advance(
                    consumedCodeUnits = consumedCodeUnits,
                )
            }

            return finish()
        }

        private fun requireArraySuffixContinuationIfNeeded(
            codePoint: Int,
        ): LexicalInspectionResult? {
            if (mode != LexicalScannerModeLaw.MODE_ARRAY_SUFFIX_OPEN) {
                return null
            }

            if (pendingArrayOpenBracket && codePoint == CODE_POINT_CLOSE_BRACKET) {
                return null
            }

            return reject(
                code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                reason = "array suffix open bracket is not followed by close bracket",
            )
        }

        private fun categoryOf(
            codePoint: Int,
        ): Int {
            return if (codePoint <= ASCII_MAX) {
                ASCII_CATEGORY_LUT[codePoint]
            } else {
                UCharacter.getType(codePoint)
            }
        }

        private fun rejectForbiddenCategoryOrWhitespace(
            codePoint: Int,
            category: Int,
        ): LexicalInspectionResult? {
            if (isAsciiWhitespace(codePoint)) {
                hasAsciiWhitespace = true

                return reject(
                    code = CanonicalTypeTextViolationCode.FORBIDDEN_WHITESPACE,
                    reason = "ASCII whitespace is forbidden in canonical type text",
                )
            }

            if (isForbiddenCategory(category)) {
                return reject(
                    code = categoryViolationCode(category),
                    reason = "forbidden Unicode category in canonical type text",
                )
            }

            return null
        }

        private fun consumeCombiningMarkIfPresent(
            category: Int,
        ): LexicalInspectionResult? {
            if (!isCombiningMark(category)) {
                return null
            }

            grossCombiningMarkCount += 1
            currentIdentifierCombiningMarks += 1

            if (grossCombiningMarkCount > policy.maxGrossCombiningMarks) {
                return reject(
                    code =
                        CanonicalTypeTextViolationCode.GROSS_COMBINING_MARK_LIMIT_EXCEEDED,
                    reason = "gross combining mark count exceeds policy cap",
                )
            }

            if (
                currentIdentifierCombiningMarks >
                policy.maxCombiningMarksPerIdentifierToken
            ) {
                return reject(
                    code =
                        CanonicalTypeTextViolationCode.COMBINING_MARK_LIMIT_EXCEEDED,
                    reason =
                        "combining mark count per identifier token exceeds policy cap",
                )
            }

            return null
        }

        private fun consumeCodePoint(
            codePoint: Int,
        ): LexicalInspectionResult? {
            return when {
                isAsciiIdentifierPart(codePoint) -> {
                    consumeIdentifierPart()
                }

                isStructuralDelimiter(codePoint) -> {
                    consumeStructuralDelimiter(
                        codePoint = codePoint,
                    )
                }

                codePoint == CODE_POINT_SLASH -> {
                    reject(
                        code = CanonicalTypeTextViolationCode.JVM_INTERNAL_NAME_SYNTAX,
                        reason = "JVM internal-name separator is forbidden",
                    )
                }

                codePoint == CODE_POINT_SEMICOLON -> {
                    reject(
                        code = CanonicalTypeTextViolationCode.JVM_DESCRIPTOR_SYNTAX,
                        reason = "JVM descriptor separator is forbidden",
                    )
                }

                codePoint == CODE_POINT_DOLLAR -> {
                    reject(
                        code = CanonicalTypeTextViolationCode.JVM_BINARY_NAME_SYNTAX,
                        reason = "JVM binary nested-class marker is forbidden",
                    )
                }

                else -> {
                    rejectUnsupportedCodePoint(
                        codePoint = codePoint,
                    )
                }
            }
        }

        private fun consumeIdentifierPart(): LexicalInspectionResult? {
            val modeCheck =
                requireModeAllowsIdentifierPart()

            if (modeCheck is LexicalInspectionResult.Rejected) {
                return modeCheck
            }

            val transition =
                transitionTo(
                    target = LexicalScannerModeLaw.MODE_IDENTIFIER,
                    reason = "identifier part",
                )

            if (transition is LexicalInspectionResult.Rejected) {
                return transition
            }

            if (currentIdentifierTokenCodePoints == 0) {
                currentIdentifierStartIndex = utf16Index
                identifierTokenCount += 1

                if (identifierTokenCount > policy.maxIdentifierTokenCount) {
                    return reject(
                        code =
                            CanonicalTypeTextViolationCode.IDENTIFIER_TOKEN_LIMIT_EXCEEDED,
                        reason = "identifier token count exceeds policy cap",
                    )
                }
            }

            currentIdentifierTokenCodePoints += 1

            if (currentIdentifierTokenCodePoints > longestIdentifierTokenCodePoints) {
                longestIdentifierTokenCodePoints =
                    currentIdentifierTokenCodePoints
            }

            if (
                currentIdentifierTokenCodePoints >
                policy.maxIdentifierTokenCodePoints
            ) {
                return reject(
                    code =
                        CanonicalTypeTextViolationCode.IDENTIFIER_TOKEN_LIMIT_EXCEEDED,
                    reason = "identifier token length exceeds policy cap",
                )
            }

            return null
        }

        private fun requireModeAllowsIdentifierPart(): LexicalInspectionResult? {
            return when (mode) {
                LexicalScannerModeLaw.MODE_EXPECT_TOKEN,
                LexicalScannerModeLaw.MODE_IDENTIFIER,
                    -> null

                LexicalScannerModeLaw.MODE_ARRAY_SUFFIX_OPEN ->
                    reject(
                        code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                        reason = "identifier appears inside malformed array suffix bracket",
                    )

                else ->
                    reject(
                        code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                        reason = "identifier appears where canonical type text expects a delimiter",
                    )
            }
        }

        private fun consumeStructuralDelimiter(
            codePoint: Int,
        ): LexicalInspectionResult? {
            val finishOutcome = finishIdentifierTokenIfAny()

            if (finishOutcome is LexicalInspectionResult.Rejected) {
                return finishOutcome
            }

            finishIdentifierAccounting()

            totalDelimiterCodePoints += 1
            nonIdentifierCodePointCount += 1

            if (totalDelimiterCodePoints > policy.maxDelimiterCodePoints) {
                return reject(
                    code = CanonicalTypeTextViolationCode.DELIMITER_LIMIT_EXCEEDED,
                    reason = "delimiter count exceeds policy cap",
                )
            }

            val transition =
                when (codePoint) {
                    CODE_POINT_DOT -> consumeDotDelimiter()
                    CODE_POINT_LT -> consumeGenericOpenDelimiter()
                    CODE_POINT_GT -> consumeGenericCloseDelimiter()
                    CODE_POINT_COMMA -> consumeCommaDelimiter()
                    CODE_POINT_QUESTION -> consumeNullableMarker()
                    CODE_POINT_STAR -> consumeStarProjection()
                    CODE_POINT_OPEN_BRACKET -> consumeArrayOpenBracket()
                    CODE_POINT_CLOSE_BRACKET -> consumeArrayCloseBracket()
                    else -> null
                }

            if (transition is LexicalInspectionResult.Rejected) {
                return transition
            }

            previousStructuralDelimiter = codePoint
            return null
        }

        private fun consumeDotDelimiter(): LexicalInspectionResult? {
            if (mode != LexicalScannerModeLaw.MODE_IDENTIFIER) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "dot delimiter must follow an identifier token",
                )
            }

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_EXPECT_TOKEN,
                reason = "dot delimiter",
            )
        }

        private fun consumeGenericOpenDelimiter(): LexicalInspectionResult? {
            if (mode != LexicalScannerModeLaw.MODE_IDENTIFIER) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic open delimiter must follow an identifier token",
                )
            }

            if (pendingArrayOpenBracket) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic delimiter appears inside array suffix",
                )
            }

            genericDepth += 1

            if (genericDepth > maxGenericDepth) {
                maxGenericDepth = genericDepth
            }

            if (genericDepth > policy.maxGenericDepth) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic depth exceeds policy cap",
                )
            }

            hasGenericDelimiters = true

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_EXPECT_TOKEN,
                reason = "generic open delimiter",
            )
        }

        private fun consumeGenericCloseDelimiter(): LexicalInspectionResult? {
            if (
                mode != LexicalScannerModeLaw.MODE_IDENTIFIER &&
                mode != LexicalScannerModeLaw.MODE_AFTER_TOKEN
            ) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic close delimiter appears without a completed token",
                )
            }

            if (pendingArrayOpenBracket) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic delimiter appears inside array suffix",
                )
            }

            if (genericDepth == 0) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic close delimiter appears without open delimiter",
                )
            }

            genericDepth -= 1
            hasGenericDelimiters = true

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_AFTER_TOKEN,
                reason = "generic close delimiter",
            )
        }

        private fun consumeCommaDelimiter(): LexicalInspectionResult? {
            if (
                mode != LexicalScannerModeLaw.MODE_IDENTIFIER &&
                mode != LexicalScannerModeLaw.MODE_AFTER_TOKEN
            ) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "comma delimiter must follow a completed type argument",
                )
            }

            if (genericDepth == 0) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "comma appears outside generic argument list",
                )
            }

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_EXPECT_TOKEN,
                reason = "comma delimiter",
            )
        }

        private fun consumeNullableMarker(): LexicalInspectionResult? {
            if (
                mode != LexicalScannerModeLaw.MODE_IDENTIFIER &&
                mode != LexicalScannerModeLaw.MODE_AFTER_TOKEN
            ) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "nullable marker must follow a completed type token",
                )
            }

            if (pendingArrayOpenBracket) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "nullable marker appears inside array suffix",
                )
            }

            hasNullableMarker = true

            if (!policy.allowNullableMarker) {
                return reject(
                    code = CanonicalTypeTextViolationCode.RAW_NULLABLE_MARKER,
                    reason = "nullable marker is forbidden by inspection policy",
                )
            }

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_AFTER_TOKEN,
                reason = "nullable marker",
            )
        }

        private fun consumeStarProjection(): LexicalInspectionResult? {
            if (mode != LexicalScannerModeLaw.MODE_EXPECT_TOKEN) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "star projection must appear where a type argument is expected",
                )
            }

            if (genericDepth == 0) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "star projection appears outside generic argument list",
                )
            }

            if (pendingArrayOpenBracket) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "star projection appears inside array suffix",
                )
            }

            hasStarProjection = true

            if (!policy.allowStarProjection) {
                return reject(
                    code = CanonicalTypeTextViolationCode.RAW_STAR_PROJECTION,
                    reason = "star projection is forbidden by inspection policy",
                )
            }

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_AFTER_TOKEN,
                reason = "star projection",
            )
        }

        private fun consumeArrayOpenBracket(): LexicalInspectionResult? {
            if (
                mode != LexicalScannerModeLaw.MODE_IDENTIFIER &&
                mode != LexicalScannerModeLaw.MODE_AFTER_TOKEN
            ) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "array suffix open bracket must follow a completed type token",
                )
            }

            if (pendingArrayOpenBracket) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "nested array suffix open bracket",
                )
            }

            pendingArrayOpenBracket = true

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_ARRAY_SUFFIX_OPEN,
                reason = "array suffix open bracket",
            )
        }

        private fun consumeArrayCloseBracket(): LexicalInspectionResult? {
            if (mode != LexicalScannerModeLaw.MODE_ARRAY_SUFFIX_OPEN) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "array suffix close bracket appears without open bracket",
                )
            }

            if (!pendingArrayOpenBracket) {
                return reject(
                    code = CanonicalTypeTextViolationCode.POLICY_CONTRACT_VIOLATION,
                    reason = "array suffix mode and bracket flag drift detected",
                )
            }

            pendingArrayOpenBracket = false
            sawArraySuffix = true

            return transitionTo(
                target = LexicalScannerModeLaw.MODE_AFTER_TOKEN,
                reason = "array suffix close bracket",
            )
        }

        private fun finishIdentifierAccounting() {
            if (
                currentIdentifierCombiningMarks >
                maxCombiningMarksPerIdentifierToken
            ) {
                maxCombiningMarksPerIdentifierToken =
                    currentIdentifierCombiningMarks
            }

            currentIdentifierTokenCodePoints = 0
            currentIdentifierStartIndex = NO_ACTIVE_IDENTIFIER_START
            currentIdentifierCombiningMarks = 0
        }

        private fun finishIdentifierTokenIfAny(): LexicalInspectionResult? {
            if (
                currentIdentifierStartIndex == NO_ACTIVE_IDENTIFIER_START ||
                currentIdentifierStartIndex >= utf16Index
            ) {
                return null
            }

            if (
                previousStructuralDelimiter == CODE_POINT_LT ||
                previousStructuralDelimiter == CODE_POINT_COMMA
            ) {
                val isIn =
                    utf16Index - currentIdentifierStartIndex == 2 &&
                            snapshot[currentIdentifierStartIndex] == 'i' &&
                            snapshot[currentIdentifierStartIndex + 1] == 'n'

                val isOut =
                    utf16Index - currentIdentifierStartIndex == 3 &&
                            snapshot[currentIdentifierStartIndex] == 'o' &&
                            snapshot[currentIdentifierStartIndex + 1] == 'u' &&
                            snapshot[currentIdentifierStartIndex + 2] == 't'

                if (isIn || isOut) {
                    hasSourceVarianceToken = true

                    if (!policy.allowSourceVarianceTokens) {
                        return reject(
                            code = CanonicalTypeTextViolationCode.SOURCE_VARIANCE_TOKEN,
                            reason =
                                "source variance token is forbidden in canonical type text",
                        )
                    }
                }
            }

            return null
        }

        private fun rejectUnsupportedCodePoint(
            codePoint: Int,
        ): LexicalInspectionResult.Rejected {
            nonIdentifierCodePointCount += 1

            if (!isAllowedByScriptPolicyV1(codePoint)) {
                return reject(
                    code = CanonicalTypeTextViolationCode.FORBIDDEN_SCRIPT,
                    reason =
                        "non-ASCII script material is forbidden by current type-text policy",
                )
            }

            return reject(
                code = CanonicalTypeTextViolationCode.FORBIDDEN_UNICODE_CATEGORY,
                reason = "unsupported code point in canonical type text",
            )
        }

        private fun requireNonIdentifierDensityWithinPolicy(): LexicalInspectionResult? {
            if (nonIdentifierCodePointCount <= 0) {
                return null
            }

            val scannedCodePointCount = position + 1

            val ratioBasisPoints =
                ((nonIdentifierCodePointCount.toLong() * 10_000L) /
                        scannedCodePointCount.toLong()).toInt()

            if (ratioBasisPoints > policy.maxNonIdentifierCodePointRatioBasisPoints) {
                return reject(
                    code =
                        CanonicalTypeTextViolationCode.NON_IDENTIFIER_DENSITY_EXCEEDED,
                    reason = "non-identifier code point density exceeds policy cap",
                )
            }

            return null
        }

        private fun transitionTo(
            target: Int,
            reason: String,
        ): LexicalInspectionResult? {
            if (!LexicalScannerModeLaw.canTransition(mode, target)) {
                return reject(
                    code = CanonicalTypeTextViolationCode.POLICY_CONTRACT_VIOLATION,
                    reason =
                        "illegal lexical scanner transition: " +
                                "${LexicalScannerModeLaw.render(mode)} -> " +
                                "${LexicalScannerModeLaw.render(target)} while consuming $reason",
                )
            }

            mode = target
            return null
        }

        private fun advance(
            consumedCodeUnits: Int,
        ) {
            utf16Index += consumedCodeUnits
            position += 1
        }

        private fun finish(): LexicalInspectionResult {
            if (pendingArrayOpenBracket || mode == LexicalScannerModeLaw.MODE_ARRAY_SUFFIX_OPEN) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "array suffix open bracket is not closed",
                )
            }

            if (genericDepth != 0) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "generic delimiters are not balanced",
                )
            }

            val finalTokenOutcome = finishIdentifierTokenIfAny()

            if (finalTokenOutcome is LexicalInspectionResult.Rejected) {
                return finalTokenOutcome
            }

            if (
                currentIdentifierCombiningMarks >
                maxCombiningMarksPerIdentifierToken
            ) {
                maxCombiningMarksPerIdentifierToken =
                    currentIdentifierCombiningMarks
            }

            if (identifierTokenCount <= 0) {
                return reject(
                    code = CanonicalTypeTextViolationCode.IDENTIFIER_TOKEN_LIMIT_EXCEEDED,
                    reason = "canonical type text contains no identifier token",
                )
            }

            if (mode == LexicalScannerModeLaw.MODE_EXPECT_TOKEN) {
                return reject(
                    code = CanonicalTypeTextViolationCode.MALFORMED_GENERIC_SYNTAX,
                    reason = "canonical type text ended while a type token was expected",
                )
            }

            val maxGraphemeClustersPerIdentifierToken =
                longestIdentifierTokenCodePoints

            if (
                maxGraphemeClustersPerIdentifierToken >
                policy.maxGraphemeClustersPerIdentifierToken
            ) {
                return reject(
                    code =
                        CanonicalTypeTextViolationCode.GRAPHEME_CLUSTER_LIMIT_EXCEEDED,
                    reason =
                        "grapheme cluster count per identifier token exceeds policy cap",
                )
            }

            return LexicalInspectionResult.Accepted(
                codePointCount = codePointCount,
                identifierTokenCount = identifierTokenCount,
                longestIdentifierTokenCodePoints = longestIdentifierTokenCodePoints,
                totalDelimiterCodePoints = totalDelimiterCodePoints,
                nonIdentifierCodePointCount = nonIdentifierCodePointCount,
                grossCombiningMarkCount = grossCombiningMarkCount,
                maxCombiningMarksPerIdentifierToken =
                    maxCombiningMarksPerIdentifierToken,
                maxGraphemeClustersPerIdentifierToken =
                    maxGraphemeClustersPerIdentifierToken,
                genericDepth = maxGenericDepth,
                hasGenericDelimiters = hasGenericDelimiters,
                hasArraySuffix = sawArraySuffix,
                hasNullableMarker = hasNullableMarker,
                hasStarProjection = hasStarProjection,
                hasAsciiWhitespace = hasAsciiWhitespace,
                hasSourceVarianceToken = hasSourceVarianceToken,
            )
        }

        private fun reject(
            code: CanonicalTypeTextViolationCode,
            reason: String,
        ): LexicalInspectionResult.Rejected {
            return LexicalInspectionResult.Rejected(
                result = rejected(
                    code = code,
                    reason = reason,
                ),
            )
        }
    }

    /**
     * Closed primitive mode law for the adapter-local lexical scanner.
     *
     * This intentionally mirrors the project's preference for explicit lifecycle
     * legality surfaces while remaining private to the adapter.
     *
     * The mode values are primitive Ints. No transition object is allocated on
     * the hot path.
     */
    private object LexicalScannerModeLaw {
        const val MODE_EXPECT_TOKEN: Int = 10
        const val MODE_IDENTIFIER: Int = 20
        const val MODE_AFTER_TOKEN: Int = 30
        const val MODE_ARRAY_SUFFIX_OPEN: Int = 40

        fun canTransition(
            from: Int,
            to: Int,
        ): Boolean {
            return when (from) {
                MODE_EXPECT_TOKEN ->
                    to == MODE_IDENTIFIER ||
                            to == MODE_AFTER_TOKEN

                MODE_IDENTIFIER ->
                    to == MODE_IDENTIFIER ||
                            to == MODE_EXPECT_TOKEN ||
                            to == MODE_AFTER_TOKEN ||
                            to == MODE_ARRAY_SUFFIX_OPEN

                MODE_AFTER_TOKEN ->
                    to == MODE_EXPECT_TOKEN ||
                            to == MODE_AFTER_TOKEN ||
                            to == MODE_ARRAY_SUFFIX_OPEN

                MODE_ARRAY_SUFFIX_OPEN ->
                    to == MODE_AFTER_TOKEN

                else ->
                    false
            }
        }

        fun render(
            mode: Int,
        ): String {
            return when (mode) {
                MODE_EXPECT_TOKEN -> "EXPECT_TOKEN"
                MODE_IDENTIFIER -> "IDENTIFIER"
                MODE_AFTER_TOKEN -> "AFTER_TOKEN"
                MODE_ARRAY_SUFFIX_OPEN -> "ARRAY_SUFFIX_OPEN"
                else -> "UNKNOWN($mode)"
            }
        }
    }

    private fun rejected(
        code: CanonicalTypeTextViolationCode,
        reason: String,
    ): CanonicalTypeTextInspectionResult.Rejected {
        return CanonicalTypeTextInspectionResult.Rejected.issue(
            violationCode = code,
            reason = reason,
        )
    }

    private fun decodeCodePoint(
        text: String,
        index: Int,
    ): DecodedCodePoint {
        val firstCode = text[index].code

        if (firstCode in HIGH_SURROGATE_START..HIGH_SURROGATE_END) {
            val secondIndex = index + 1

            if (secondIndex >= text.length) {
                return DecodedCodePoint.Invalid(
                    reason = "unpaired high surrogate at end of input",
                )
            }

            val secondCode = text[secondIndex].code

            if (secondCode !in LOW_SURROGATE_START..LOW_SURROGATE_END) {
                return DecodedCodePoint.Invalid(
                    reason = "high surrogate not followed by low surrogate",
                )
            }

            val codePoint =
                SUPPLEMENTARY_CODE_POINT_START +
                        ((firstCode - HIGH_SURROGATE_START) shl 10) +
                        (secondCode - LOW_SURROGATE_START)

            return DecodedCodePoint.Valid(
                codePoint = codePoint,
                consumedCodeUnits = 2,
            )
        }

        if (firstCode in LOW_SURROGATE_START..LOW_SURROGATE_END) {
            return DecodedCodePoint.Invalid(
                reason = "unpaired low surrogate",
            )
        }

        return DecodedCodePoint.Valid(
            codePoint = firstCode,
            consumedCodeUnits = 1,
        )
    }

    private fun utf16CodeUnitCountOf(
        codePoint: Int,
    ): Int {
        return if (codePoint >= SUPPLEMENTARY_CODE_POINT_START) {
            2
        } else {
            1
        }
    }

    private fun isForbiddenCategory(
        category: Int,
    ): Boolean {
        return category == CATEGORY_CONTROL ||
                category == CATEGORY_FORMAT ||
                category == CATEGORY_PRIVATE_USE ||
                category == CATEGORY_SURROGATE ||
                category == CATEGORY_UNASSIGNED
    }

    private fun categoryViolationCode(
        category: Int,
    ): CanonicalTypeTextViolationCode {
        return when (category) {
            CATEGORY_CONTROL ->
                CanonicalTypeTextViolationCode.FORBIDDEN_UNICODE_CATEGORY

            CATEGORY_FORMAT ->
                CanonicalTypeTextViolationCode.FORBIDDEN_INVISIBLE_OR_BIDI

            CATEGORY_PRIVATE_USE,
            CATEGORY_SURROGATE,
            CATEGORY_UNASSIGNED ->
                CanonicalTypeTextViolationCode.INVALID_SCALAR

            else ->
                CanonicalTypeTextViolationCode.FORBIDDEN_UNICODE_CATEGORY
        }
    }

    private fun isCombiningMark(
        category: Int,
    ): Boolean {
        return category == CATEGORY_NON_SPACING_MARK ||
                category == CATEGORY_COMBINING_SPACING_MARK ||
                category == CATEGORY_ENCLOSING_MARK
    }

    private fun isAsciiWhitespace(
        codePoint: Int,
    ): Boolean {
        return codePoint == CODE_POINT_SPACE ||
                codePoint == CODE_POINT_TAB ||
                codePoint == CODE_POINT_LF ||
                codePoint == CODE_POINT_CR ||
                codePoint == CODE_POINT_FORM_FEED
    }

    private fun isAsciiIdentifierPart(
        codePoint: Int,
    ): Boolean {
        return codePoint in CODE_POINT_UPPER_A..CODE_POINT_UPPER_Z ||
                codePoint in CODE_POINT_LOWER_A..CODE_POINT_LOWER_Z ||
                codePoint in CODE_POINT_ZERO..CODE_POINT_NINE ||
                codePoint == CODE_POINT_UNDERSCORE
    }

    private fun isStructuralDelimiter(
        codePoint: Int,
    ): Boolean {
        return codePoint == CODE_POINT_DOT ||
                codePoint == CODE_POINT_LT ||
                codePoint == CODE_POINT_GT ||
                codePoint == CODE_POINT_COMMA ||
                codePoint == CODE_POINT_OPEN_BRACKET ||
                codePoint == CODE_POINT_CLOSE_BRACKET ||
                codePoint == CODE_POINT_QUESTION ||
                codePoint == CODE_POINT_STAR
    }

    private fun isAllowedByScriptPolicyV1(
        codePoint: Int,
    ): Boolean {
        return codePoint <= ASCII_MAX
    }

    private class CodePointScratch(
        var codePoints: IntArray,
    )

    private sealed interface DecodedCodePoint {
        class Valid(
            val codePoint: Int,
            val consumedCodeUnits: Int,
        ) : DecodedCodePoint

        class Invalid(
            val reason: String,
        ) : DecodedCodePoint
    }

    private sealed interface ScalarInspectionResult {
        class Accepted(
            val codePointCount: Int,
        ) : ScalarInspectionResult

        class Rejected(
            val result: CanonicalTypeTextInspectionResult.Rejected,
        ) : ScalarInspectionResult
    }

    private sealed interface LexicalInspectionResult {
        class Accepted(
            val codePointCount: Int,
            val identifierTokenCount: Int,
            val longestIdentifierTokenCodePoints: Int,
            val totalDelimiterCodePoints: Int,
            val nonIdentifierCodePointCount: Int,
            val grossCombiningMarkCount: Int,
            val maxCombiningMarksPerIdentifierToken: Int,
            val maxGraphemeClustersPerIdentifierToken: Int,
            val genericDepth: Int,
            val hasGenericDelimiters: Boolean,
            val hasArraySuffix: Boolean,
            val hasNullableMarker: Boolean,
            val hasStarProjection: Boolean,
            val hasAsciiWhitespace: Boolean,
            val hasSourceVarianceToken: Boolean,
        ) : LexicalInspectionResult

        class Rejected(
            val result: CanonicalTypeTextInspectionResult.Rejected,
        ) : LexicalInspectionResult
    }

    companion object {
        const val SCRIPT_POLICY_KONTRAKT_TYPE_TEXT_V1: String =
            "kontrakt_type_text_script_policy_v1"

        private const val MAX_RETAINED_SCRATCH_CODE_POINTS: Int = 8_192

        private const val NO_ACTIVE_IDENTIFIER_START: Int = -1
        private const val NO_PREVIOUS_STRUCTURAL_DELIMITER: Int = -1

        private const val HIGH_SURROGATE_START: Int = 0xD800
        private const val HIGH_SURROGATE_END: Int = 0xDBFF
        private const val LOW_SURROGATE_START: Int = 0xDC00
        private const val LOW_SURROGATE_END: Int = 0xDFFF
        private const val SUPPLEMENTARY_CODE_POINT_START: Int = 0x10000

        private const val ASCII_MAX: Int = 0x7F

        private const val CODE_POINT_TAB: Int = 0x09
        private const val CODE_POINT_LF: Int = 0x0A
        private const val CODE_POINT_CR: Int = 0x0D
        private const val CODE_POINT_FORM_FEED: Int = 0x0C
        private const val CODE_POINT_SPACE: Int = 0x20

        private const val CODE_POINT_DOLLAR: Int = 0x24
        private const val CODE_POINT_STAR: Int = 0x2A
        private const val CODE_POINT_COMMA: Int = 0x2C
        private const val CODE_POINT_DOT: Int = 0x2E
        private const val CODE_POINT_SLASH: Int = 0x2F
        private const val CODE_POINT_ZERO: Int = 0x30
        private const val CODE_POINT_NINE: Int = 0x39
        private const val CODE_POINT_SEMICOLON: Int = 0x3B
        private const val CODE_POINT_LT: Int = 0x3C
        private const val CODE_POINT_GT: Int = 0x3E
        private const val CODE_POINT_QUESTION: Int = 0x3F
        private const val CODE_POINT_UPPER_A: Int = 0x41
        private const val CODE_POINT_UPPER_Z: Int = 0x5A
        private const val CODE_POINT_OPEN_BRACKET: Int = 0x5B
        private const val CODE_POINT_CLOSE_BRACKET: Int = 0x5D
        private const val CODE_POINT_UNDERSCORE: Int = 0x5F
        private const val CODE_POINT_LOWER_A: Int = 0x61
        private const val CODE_POINT_LOWER_Z: Int = 0x7A

        private val CATEGORY_CONTROL: Int =
            UCharacterCategory.CONTROL.toInt()

        private val CATEGORY_FORMAT: Int =
            UCharacterCategory.FORMAT.toInt()

        private val CATEGORY_PRIVATE_USE: Int =
            UCharacterCategory.PRIVATE_USE.toInt()

        private val CATEGORY_SURROGATE: Int =
            UCharacterCategory.SURROGATE.toInt()

        private val CATEGORY_UNASSIGNED: Int =
            UCharacterCategory.UNASSIGNED.toInt()

        private val CATEGORY_NON_SPACING_MARK: Int =
            UCharacterCategory.NON_SPACING_MARK.toInt()

        private val CATEGORY_COMBINING_SPACING_MARK: Int =
            UCharacterCategory.COMBINING_SPACING_MARK.toInt()

        private val CATEGORY_ENCLOSING_MARK: Int =
            UCharacterCategory.ENCLOSING_MARK.toInt()

        private val CATEGORY_UPPERCASE_LETTER: Int =
            UCharacterCategory.UPPERCASE_LETTER.toInt()

        private val CATEGORY_LOWERCASE_LETTER: Int =
            UCharacterCategory.LOWERCASE_LETTER.toInt()

        private val CATEGORY_DECIMAL_DIGIT_NUMBER: Int =
            UCharacterCategory.DECIMAL_DIGIT_NUMBER.toInt()

        private val CATEGORY_CONNECTOR_PUNCTUATION: Int =
            UCharacterCategory.CONNECTOR_PUNCTUATION.toInt()

        private val CATEGORY_OTHER_PUNCTUATION: Int =
            UCharacterCategory.OTHER_PUNCTUATION.toInt()

        private val THREAD_LOCAL_SCRATCH: ThreadLocal<CodePointScratch> =
            ThreadLocal.withInitial {
                CodePointScratch(
                    codePoints = IntArray(0),
                )
            }

        private val ASCII_CATEGORY_LUT: IntArray =
            buildAsciiCategoryLut()

        private fun buildAsciiCategoryLut(): IntArray {
            val table = IntArray(ASCII_MAX + 1)

            var codePoint = 0
            while (codePoint <= ASCII_MAX) {
                table[codePoint] =
                    when {
                        codePoint in 0x00..0x1F ->
                            CATEGORY_CONTROL

                        codePoint == 0x7F ->
                            CATEGORY_CONTROL

                        codePoint in CODE_POINT_UPPER_A..CODE_POINT_UPPER_Z ->
                            CATEGORY_UPPERCASE_LETTER

                        codePoint in CODE_POINT_LOWER_A..CODE_POINT_LOWER_Z ->
                            CATEGORY_LOWERCASE_LETTER

                        codePoint in CODE_POINT_ZERO..CODE_POINT_NINE ->
                            CATEGORY_DECIMAL_DIGIT_NUMBER

                        codePoint == CODE_POINT_UNDERSCORE ->
                            CATEGORY_CONNECTOR_PUNCTUATION

                        else ->
                            CATEGORY_OTHER_PUNCTUATION
                    }

                codePoint += 1
            }

            return table
        }

        @JvmStatic
        fun issue(
            goldenVectorSetId: String,
            goldenVectorDigest: String,
        ): Icu4jNormalizationEngineAdapter {
            val icuVersion = VersionInfo.ICU_VERSION.toString()
            val unicodeVersion = UCharacter.getUnicodeVersion().toString()

            return Icu4jNormalizationEngineAdapter(
                engineId = "icu4j",
                engineVersion = "icu4j-$icuVersion",
                unicodeProfileVersion = "unicode-$unicodeVersion",
                goldenVectorSetId = goldenVectorSetId,
                goldenVectorDigest = goldenVectorDigest,
                nfc = Normalizer2.getNFCInstance(),
            )
        }

        @JvmStatic
        fun issueForTests(): Icu4jNormalizationEngineAdapter {
            return issue(
                goldenVectorSetId = "test-golden-vectors",
                goldenVectorDigest = "not-for-production",
            )
        }
    }
}