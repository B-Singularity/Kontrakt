package stage.input.material

import stage.canonicalization.material.CanonicalTypeShapeKind
import stage.input.diagnostics.MetamodelFactContractViolationException

/**
 * Non-authoritative hint about an array component.
 *
 * This value must never be treated as actual resolved component shape.
 *
 * Correct use:
 *
 * - use it to decide that ArrayExpansionPlan resolution is required;
 * - use it to select a conservative preparation path;
 * - assert it against the actual component shape after resolution.
 *
 * Incorrect use:
 *
 * - directly branch into a committed layout from this hint;
 * - skip actual component resolution because a hint exists;
 * - use the hint as a cache key without verification.
 */
class ArrayComponentShapeHint private constructor(
    val hasGenericComponent: Boolean,
    val componentGenericArityHint: Int?,
    val componentShapeKindHint: CanonicalTypeShapeKind?,
) {
    fun validateForArray() {
        if (hasGenericComponent && componentGenericArityHint == null) {
            throw MetamodelFactContractViolationException(
                "ArrayComponentShapeHint with hasGenericComponent=true must provide componentGenericArityHint.",
            )
        }

        if (!hasGenericComponent && componentGenericArityHint != null && componentGenericArityHint > 0) {
            throw MetamodelFactContractViolationException(
                "ArrayComponentShapeHint componentGenericArityHint > 0 requires hasGenericComponent=true.",
            )
        }

        if (componentGenericArityHint != null) {
            TypeShapeSummary.requireGenericArityWithinCap(
                field = "ArrayComponentShapeHint.componentGenericArityHint",
                value = componentGenericArityHint,
            )
        }

        if (componentShapeKindHint == CanonicalTypeShapeKind.VOID) {
            throw MetamodelFactContractViolationException(
                "ArrayComponentShapeHint.componentShapeKindHint must not be VOID.",
            )
        }

        if (componentShapeKindHint == CanonicalTypeShapeKind.ARRAY) {
            throw MetamodelFactContractViolationException(
                "Nested array shape must be represented by TypeShapeSummary.arrayRank, " +
                        "not by ARRAY componentShapeKindHint.",
            )
        }
    }

    fun requireMatchesResolvedComponent(
        actualComponentKind: CanonicalTypeShapeKind,
        actualComponentGenericArity: Int,
    ) {
        TypeShapeSummary.requireGenericArityWithinCap(
            field = "actualComponentGenericArity",
            value = actualComponentGenericArity,
        )

        if (componentShapeKindHint != null && componentShapeKindHint != actualComponentKind) {
            throw MetamodelFactContractViolationException(
                "Array component shape hint mismatch: " +
                        "hint=${componentShapeKindHint.protocolToken}, actual=${actualComponentKind.protocolToken}",
            )
        }

        if (componentGenericArityHint != null &&
            componentGenericArityHint != actualComponentGenericArity
        ) {
            throw MetamodelFactContractViolationException(
                "Array component generic arity hint mismatch: " +
                        "hint=$componentGenericArityHint, actual=$actualComponentGenericArity",
            )
        }

        val actualHasGenericComponent = actualComponentGenericArity > 0
        if (hasGenericComponent != actualHasGenericComponent) {
            throw MetamodelFactContractViolationException(
                "Array component generic presence hint mismatch: " +
                        "hint=$hasGenericComponent, actual=$actualHasGenericComponent",
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArrayComponentShapeHint) return false

        return hasGenericComponent == other.hasGenericComponent &&
                componentGenericArityHint == other.componentGenericArityHint &&
                componentShapeKindHint == other.componentShapeKindHint
    }

    override fun hashCode(): Int {
        var result = hasGenericComponent.hashCode()
        result = 31 * result + (componentGenericArityHint ?: -1)
        result = 31 * result + (componentShapeKindHint?.protocolOrder ?: 0)
        return result
    }

    override fun toString(): String =
        buildString {
            append("ArrayComponentShapeHint(")
            append("hasGenericComponent=")
            append(hasGenericComponent)
            append(", componentGenericArityHint=")
            append(componentGenericArityHint)
            append(", componentShapeKindHint=")
            append(componentShapeKindHint?.protocolToken)
            append(')')
        }

    companion object {
        @JvmStatic
        fun issue(
            hasGenericComponent: Boolean,
            componentGenericArityHint: Int?,
            componentShapeKindHint: CanonicalTypeShapeKind?,
        ): ArrayComponentShapeHint {
            val hint =
                ArrayComponentShapeHint(
                    hasGenericComponent = hasGenericComponent,
                    componentGenericArityHint = componentGenericArityHint,
                    componentShapeKindHint = componentShapeKindHint,
                )
            hint.validateForArray()
            return hint
        }
    }
}
