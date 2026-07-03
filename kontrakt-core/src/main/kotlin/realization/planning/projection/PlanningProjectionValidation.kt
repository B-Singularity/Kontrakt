package realization.planning.projection

import stage.lowering.diagnostics.ActiveMemberProjectionException

/**
 * Shared validation for planning-owned projection objects.
 *
 * This is not metamodel DTO validation.
 * It protects Core-owned projected semantic material.
 *
 * This helper intentionally does not perform platform normalization.
 * Normalization is a version-bound adapter/metamodel responsibility before facts
 * reach planning.
 */
internal fun validatePlanningCanonicalComponent(
    field: String,
    value: String,
) {
    if (value.isBlank()) {
        throw ActiveMemberProjectionException("$field must not be blank.")
    }

    if (value.contains('|')) {
        throw ActiveMemberProjectionException(
            "$field must not contain reserved delimiter '|': $value",
        )
    }

    var i = 0
    while (i < value.length) {
        val ch = value[i]
        if (ch.isISOControl()) {
            throw ActiveMemberProjectionException(
                "$field must not contain ISO control characters.",
            )
        }
        i++
    }
}
