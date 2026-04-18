package metamodel.domain.dto

import metamodel.domain.exception.InvalidMetamodelCanonicalComponentException

/**
 * Shared canonical component validation for metamodel DTOs.
 *
 * This helper intentionally does NOT call java.text.Normalizer or any other
 * platform normalizer directly.
 *
 * Normalization is a version-bound Kontrakt protocol concern.
 * It must be enforced by the adapter-owned normalization boundary using the
 * ratified Kontrakt normalization service / engine before DTO issuance.
 *
 * This helper only rejects malformed component shapes that are unconditionally
 * illegal at the DTO boundary:
 *
 * - blank component
 * - reserved delimiter
 * - ISO control characters
 *
 * Rationale:
 * calling JDK normalization here would create a second normalization authority
 * and bypass the project-level normalization law.
 */
internal fun validateCanonicalComponent(
    field: String,
    value: String,
) {
    if (value.isBlank()) {
        throw InvalidMetamodelCanonicalComponentException(
            field = field,
            value = value,
            reason = "Component must not be blank."
        )
    }

    if (value.contains('|')) {
        throw InvalidMetamodelCanonicalComponentException(
            field = field,
            value = value,
            reason = "Component must not contain reserved delimiter '|'."
        )
    }

    var i = 0
    while (i < value.length) {
        val ch = value[i]
        if (ch.isISOControl()) {
            throw InvalidMetamodelCanonicalComponentException(
                field = field,
                value = value,
                reason = "Component must not contain ISO control characters."
            )
        }
        i++
    }
}