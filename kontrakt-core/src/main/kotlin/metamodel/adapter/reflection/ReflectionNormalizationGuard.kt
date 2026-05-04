package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelNormalizationViolationException
import metamodel.domain.exception.safeDiagnosticValue

/**
 * Reflection adapter-local text-surface preflight guard.
 *
 * This helper does not normalize.
 * This helper does not perform NFC checks.
 * This helper does not issue metamodel domain value objects.
 *
 * Its role is intentionally small:
 *
 * - reject obviously invalid reflection-lowered component text before it reaches
 *   the canonical type-text ratification boundary;
 * - keep cheap adapter-local DoS and log/control-injection defenses close to
 *   the reflection adapter;
 * - avoid depending on host-JRE Unicode classification behavior.
 *
 * Canonical normalization authority:
 *
 * The canonical boundary is:
 *
 *     CanonicalTypeText.ratify(...)
 *       -> NormalizationEngine.inspectCanonicalTypeText(...)
 *
 * Therefore this guard must not call:
 *
 * - NormalizationEngine.isNfc(...);
 * - java.text.Normalizer;
 * - Character.*;
 * - Char.isWhitespace();
 * - Char.isISOControl();
 * - Regex-based Unicode classification.
 *
 * Adapter-specific spelling law:
 *
 * Reflection-specific spelling lowering, such as replacing JVM '$' with '.',
 * must happen before this guard is called.
 *
 * Boundary law:
 *
 * Failures here are reflection-lowering / normalization-boundary admission
 * failures, not already-ratified domain fact violations. Therefore this class
 * throws [MetamodelNormalizationViolationException].
 *
 * Performance law:
 *
 * The cheap surface checks are performed in one pass:
 *
 * - blank component detection;
 * - reserved delimiter detection;
 * - C0/C1 control character detection.
 *
 * Resource law:
 *
 * Component length is capped before scanning. This prevents malformed reflection
 * input from forcing expensive downstream canonical type-text inspection on
 * unbounded strings.
 *
 * Diagnostic law:
 *
 * The offending value is never echoed directly. Diagnostics use
 * [safeDiagnosticValue].
 */
internal class ReflectionNormalizationGuard private constructor() {
    /**
     * Performs cheap adapter-local preflight for reflection-lowered component
     * text.
     *
     * This method is deliberately named "surface" rather than "normalized"
     * because NFC and Unicode policy inspection are owned by
     * CanonicalTypeText.ratify(...).
     */
    fun requireReflectionComponentSurface(
        field: String,
        value: String,
    ) {
        requireFieldName(field)
        requireComponentSurface(
            field = field,
            value = value,
        )
    }

    /**
     * Compatibility shim for older call sites.
     *
     * Prefer [requireReflectionComponentSurface] in new code.
     *
     * This method intentionally no longer checks NFC. The name is kept only to
     * avoid unnecessary churn while the reflection adapter is migrated file by
     * file.
     */
    fun requireNormalizedComponent(
        field: String,
        value: String,
    ) {
        requireReflectionComponentSurface(
            field = field,
            value = value,
        )
    }

    private fun requireFieldName(
        field: String,
    ) {
        if (field.isEmpty()) {
            throw MetamodelNormalizationViolationException(
                field = "ReflectionNormalizationGuard.field",
                valueSample = "<empty>",
                engineId = REFLECTION_PREFLIGHT_ENGINE_ID,
                engineVersion = REFLECTION_PREFLIGHT_ENGINE_VERSION,
                reason = "Diagnostic field name must not be empty.",
            )
        }
    }

    private fun requireComponentSurface(
        field: String,
        value: String,
    ) {
        if (value.length > MAX_REFLECTION_COMPONENT_CHARS) {
            throw violation(
                field = field,
                value = value,
                reason = "Component exceeds maximum allowed length.",
            )
        }

        if (value.isEmpty()) {
            throw violation(
                field = field,
                value = value,
                reason = "Component must not be empty.",
            )
        }

        /*
         * Single cheap pass.
         *
         * Do not replace this with:
         *
         * - value.isBlank()
         * - value.contains('|')
         * - Char.isISOControl()
         *
         * Those either add redundant scans or depend on host-JRE Unicode
         * classification behavior.
         */
        var hasNonAsciiSpace = false
        var index = 0

        while (index < value.length) {
            val c = value[index]

            if (c == RESERVED_COMPONENT_DELIMITER) {
                throw violation(
                    field = field,
                    value = value,
                    reason = "Component must not contain reserved delimiter '|'.",
                )
            }

            val code = c.code
            val isC0Control = code in C0_CONTROL_START..C0_CONTROL_END
            val isC1Control = code in C1_CONTROL_START..C1_CONTROL_END

            if (isC0Control || isC1Control) {
                throw violation(
                    field = field,
                    value = value,
                    reason = "Component must not contain C0/C1 control characters.",
                )
            }

            /*
             * This preserves the previous "blank component" intent without using
             * Unicode whitespace classification.
             *
             * C0 whitespace such as tab/newline is already rejected above. The
             * remaining common blank-only component is ASCII space.
             *
             * This is not a full Unicode whitespace policy. Rich Unicode
             * classification belongs to NormalizationEngine inspection.
             */
            if (c != ASCII_SPACE) {
                hasNonAsciiSpace = true
            }

            index += 1
        }

        if (!hasNonAsciiSpace) {
            throw violation(
                field = field,
                value = value,
                reason = "Component must not be blank.",
            )
        }
    }

    private fun violation(
        field: String,
        value: String,
        reason: String,
    ): MetamodelNormalizationViolationException {
        return MetamodelNormalizationViolationException(
            field = field,
            valueSample = safeDiagnosticValue(value),
            engineId = REFLECTION_PREFLIGHT_ENGINE_ID,
            engineVersion = REFLECTION_PREFLIGHT_ENGINE_VERSION,
            reason = reason,
        )
    }

    companion object {
        /**
         * Reflection component cap.
         *
         * This guard receives canonicalized reflection-name components, not
         * arbitrarily large blobs.
         *
         * 512 chars is intentionally generous for JVM / Kotlin declaration
         * material while still preventing allocation/CPU DoS before canonical
         * type-text inspection.
         *
         * If a later protocol introduces a global metamodel component cap, this
         * value should delegate to that shared law.
         */
        private const val MAX_REFLECTION_COMPONENT_CHARS: Int = 512

        private const val ASCII_SPACE: Char = ' '
        private const val RESERVED_COMPONENT_DELIMITER: Char = '|'

        private const val C0_CONTROL_START: Int = 0x0000
        private const val C0_CONTROL_END: Int = 0x001F
        private const val C1_CONTROL_START: Int = 0x007F
        private const val C1_CONTROL_END: Int = 0x009F

        /**
         * Diagnostic provenance for adapter-local preflight failures.
         *
         * This is not a Unicode normalization engine id. It identifies the
         * reflection adapter's cheap admission preflight.
         */
        private const val REFLECTION_PREFLIGHT_ENGINE_ID: String =
            "reflection-adapter-preflight"

        private const val REFLECTION_PREFLIGHT_ENGINE_VERSION: String =
            "reflection-adapter-preflight-v1"

        @JvmStatic
        fun issue(): ReflectionNormalizationGuard {
            return ReflectionNormalizationGuard()
        }
    }
}