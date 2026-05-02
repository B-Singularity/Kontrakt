package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelNormalizationViolationException
import metamodel.domain.exception.safeDiagnosticValue
import metamodel.domain.port.outgoing.NormalizationEngine

/**
 * Reflection adapter-local normalization guard.
 *
 * This helper does not normalize.
 * It enforces Kontrakt's NFC-REJECT boundary through the injected
 * NormalizationEngine.
 *
 * Adapter-specific canonical spelling, such as replacing JVM '$' with '.', must
 * happen before this guard is called.
 *
 * Boundary law:
 *
 * This guard is adapter-local. It protects the reflection adapter boundary
 * before raw reflection names are lowered into metamodel domain VOs.
 *
 * It deliberately throws MetamodelNormalizationViolationException rather than
 * MetamodelFactContractViolationException because failures here are
 * normalization/lowering boundary failures, not already-ratified domain fact
 * violations.
 *
 * Performance law:
 *
 * The cheap surface checks are performed in one pass:
 *
 * - blank component detection;
 * - reserved delimiter detection;
 * - C0/C1 control character detection.
 *
 * The expensive NFC check is performed only after the cheap guards pass.
 *
 * Unicode law:
 *
 * This class does not call Character.*, Char.isWhitespace(), Char.isISOControl(),
 * or locale-sensitive APIs. Control characters are rejected by explicit C0/C1
 * ranges to avoid host-JRE Unicode classification drift.
 *
 * Resource law:
 *
 * The component length is capped before scanning and before the NFC check. This
 * prevents malformed reflection input from forcing expensive normalization
 * checks on unbounded strings.
 */
internal class ReflectionNormalizationGuard private constructor(
    private val normalizationEngine: NormalizationEngine,
) {
    fun requireNormalizedComponent(
        field: String,
        value: String,
    ) {
        requireComponentSurface(
            field = field,
            value = value,
        )

        if (!normalizationEngine.isNfc(value)) {
            throw violation(
                field = field,
                value = value,
                reason = "Component must already be NFC-normalized.",
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

            if (c == '|') {
                throw violation(
                    field = field,
                    value = value,
                    reason = "Component must not contain reserved delimiter '|'.",
                )
            }

            val code = c.code
            val isC0Control = code in 0x0000..0x001F
            val isC1Control = code in 0x007F..0x009F

            if (isC0Control || isC1Control) {
                throw violation(
                    field = field,
                    value = value,
                    reason = "Component must not contain C0/C1 control characters.",
                )
            }

            /*
             * This preserves the old "blank component" intent without calling
             * Char.isWhitespace().
             *
             * C0 whitespace such as tab/newline is already rejected above.
             * The remaining common blank-only component is ASCII space.
             *
             * This is not a full Unicode whitespace policy. Rich Unicode
             * classification belongs to NormalizationEngine / ratified domain
             * text boundaries.
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
            engineId = normalizationEngine.engineId,
            engineVersion = normalizationEngine.engineVersion,
            reason = reason,
        )
    }

    companion object {
        /**
         * Reflection component cap.
         *
         * This guard receives canonicalized reflection-name components, not
         * arbitrarily large blobs. 512 chars is intentionally generous for JVM /
         * Kotlin declaration material while still preventing allocation/CPU DoS
         * before NFC inspection.
         *
         * If a later protocol introduces a global metamodel component cap, this
         * value should delegate to that shared law.
         */
        private const val MAX_REFLECTION_COMPONENT_CHARS: Int = 512

        private const val ASCII_SPACE: Char = ' '

        @JvmStatic
        fun issue(
            normalizationEngine: NormalizationEngine,
        ): ReflectionNormalizationGuard {
            return ReflectionNormalizationGuard(
                normalizationEngine = normalizationEngine,
            )
        }
    }
}