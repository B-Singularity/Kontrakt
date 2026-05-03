package kontrakt.ir.spec

import ir.spec.TypeId

/**
 * Subject binding for a test specification.
 *
 * This is not:
 *
 * - a broad descriptor;
 * - a type resolver result;
 * - a polymorphic implementation selection;
 * - or an execution object.
 *
 * It records the relationship between:
 *
 * - declared: the type surface declared by the user/spec contributor;
 * - concrete: the concrete type selected for execution.
 *
 * The two are intentionally separate.
 *
 * A spec may be declared against an interface, abstract contract, or user-facing
 * subject surface while executing against a concrete implementation.
 */
class TestSubjectBinding private constructor(
    val declared: TypeId,
    val concrete: TypeId,
) {
    override fun equals(
        other: Any?,
    ): Boolean {
        if (this === other) return true
        if (other !is TestSubjectBinding) return false

        return declared == other.declared &&
                concrete == other.concrete
    }

    override fun hashCode(): Int {
        var result = declared.hashCode()
        result = 31 * result + concrete.hashCode()
        return result
    }

    override fun toString(): String {
        return "TestSubjectBinding(declared=$declared, concrete=$concrete)"
    }

    companion object {
        fun issue(
            declared: TypeId,
            concrete: TypeId,
        ): TestSubjectBinding {
            return TestSubjectBinding(
                declared = declared,
                concrete = concrete,
            )
        }
    }
}