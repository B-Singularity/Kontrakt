package metamodel.adapter.reflection

import metamodel.domain.exception.MetamodelNormalizationViolationException
import metamodel.domain.exception.safeDiagnosticValue
import planning.domain.port.outgoing.NormalizationEngine

/**
 * Reflection adapter-local normalization guard.
 *
 * This helper does not normalize.
 * It enforces Kontrakt's NFC-REJECT boundary through the injected NormalizationEngine.
 *
 * Adapter-specific canonical spelling, such as replacing JVM '$' with '.', must
 * happen before this guard is called.
 */
internal class ReflectionNormalizationGuard private constructor(
    private val normalizationEngine: NormalizationEngine,
) {
    fun requireNormalizedComponent(
        field: String,
        value: String,
    ) {
        if (value.isBlank()) {
            throw violation(
                field = field,
                value = value,
                reason = "Component must not be blank.",
            )
        }

        if (value.contains('|')) {
            throw violation(
                field = field,
                value = value,
                reason = "Component must not contain reserved delimiter '|'.",
            )
        }

        var i = 0
        while (i < value.length) {
            if (value[i].isISOControl()) {
                throw violation(
                    field = field,
                    value = value,
                    reason = "Component must not contain ISO control characters.",
                )
            }
            i++
        }

        if (!normalizationEngine.isNfc(value)) {
            throw violation(
                field = field,
                value = value,
                reason = "Component must already be NFC-normalized.",
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
        @JvmStatic
        fun issue(
            normalizationEngine: NormalizationEngine,
        ): ReflectionNormalizationGuard {
            return ReflectionNormalizationGuard(normalizationEngine)
        }
    }
}