package execution.domain.vo.verification

import kotlin.reflect.KClass

/**
 * [Value Object] Assertion Rule
 *
 * Defines the "Standard", "Law", or "Logic" that was applied during a verification step.
 * This sealed hierarchy categorizes the source and nature of a test result (Verdict).
 *
 * It answers the question: "Which rule was being checked when this result occurred?"
 */
sealed interface AssertionRule {
    /**
     * A machine-readable identifier for the rule (e.g., "NotNull", "ConstructorSanity").
     * Used for reporting, logging, and statistics.
     */
    val key: String
}

// =============================================================================
// [Category 1] Contract & Annotation Rules
// =============================================================================

/**
 * Represents a check based on a specific annotation contract.
 * e.g., @NotNull, @Positive, @Size
 */
data class AnnotationRule(
    val annotation: KClass<out Annotation>,
) : AssertionRule {
    override val key: String = annotation.simpleName ?: "AnonymousAnnotation"
}

// =============================================================================
// [Category 2] Structural Integrity Rules (Fuzzing / Defensive checks)
// =============================================================================

/**
 * Marker interface for rules regarding the structural integrity of the object.
 */
sealed interface IntegrityRule : AssertionRule

/**
 * Rule: "A constructor MUST create an instance when provided with valid arguments."
 * Failure here means the object is untestable or broken.
 */
data object ConstructorSanityRule : IntegrityRule {
    override val key: String = "ConstructorSanity"
}

/**
 * Rule: "A constructor MUST throw an exception when provided with invalid arguments."
 * This represents defensive programming checks (Fuzzing success criteria).
 */
data object DefensiveCheckRule : IntegrityRule {
    override val key: String = "DefensiveValidation"
}

// =============================================================================
// [Category 3] Data Contract Specific Rules
// =============================================================================

/**
 * Represents strict compliance rules for Data Classes and Value Objects.
 * These rules ensure the object behaves correctly as a data carrier.
 *
 * @property checkName The specific aspect of the data contract being verified.
 */
sealed class DataContractRule(
    val checkName: String,
) : AssertionRule {
    override val key: String = "DataContract.$checkName"

    // --- Structural Integrity ---

    /**
     * Rule: Structure Validity.
     * Checks meta-requirements, such as the existence of a Primary Constructor.
     */
    data object Structure : DataContractRule("Structure")

    /**
     * Rule: Constructor Constraint (Fuzzing).
     * The constructor must reject invalid inputs (defensive programming).
     */
    data object ConstructorConstraint : DataContractRule("ConstructorConstraint")

    // --- Equality Contract (equals) ---

    /**
     * Rule: Not-Null Equality (x.equals(null) must be false).
     */
    data object NotNullEquality : DataContractRule("NotNullEquality")

    /**
     * Rule: Reflexivity (x.equals(x) must be true).
     */
    data object Reflexivity : DataContractRule("Reflexivity")

    /**
     * Rule: Symmetry (x.equals(y) must imply y.equals(x)).
     */
    data object Symmetry : DataContractRule("Symmetry")

    /**
     * Rule: Equals Consistency.
     * Repeated calls to `equals()` on the same object must return the same result.
     * (Formerly just 'Consistency')
     */
    data object Consistency : DataContractRule("Consistency")

    // --- Hashing Contract (hashCode) ---

    /**
     * Rule: HashCode Stability.
     * hashCode() must remain constant for the same object (assuming immutable state).
     */
    data object HashStability : DataContractRule("HashStability")

    /**
     * Rule: HashCode Consistency.
     * x.equals(y) must imply x.hashCode() == y.hashCode().
     */
    data object HashConsistency : DataContractRule("HashConsistency")
}

// =============================================================================
// [Category 4] Standard Assertions (User Test Logic)
// =============================================================================

/**
 * Represents a standard assertion made manually by the user (e.g., JUnit `assertEquals`, Kotlin `check`).
 * This distinguishes "User Logic Failure" from "Contract Violation".
 *
 * (Replaces the legacy 'UserAssertionRule')
 */
data object StandardAssertion : AssertionRule {
    override val key: String = "StandardAssertion"
}

// =============================================================================
// [Category 5] Exceptions & Errors (Runtime Failures)
// =============================================================================

/**
 * Represents an unexpected exception thrown by the user's code during execution.
 * (e.g., NullPointerException, IndexOutOfBoundsException)
 */
data class UserExceptionRule(
    val exceptionType: String,
) : AssertionRule {
    override val key: String = exceptionType
}

/**
 * Represents an unexpected system failure or framework crash not related to domain logic.
 * This implies a bug in the Kontrakt framework itself.
 */
data class SystemErrorRule(
    val exceptionType: String,
) : AssertionRule {
    override val key: String = "SystemError"
}

/**
 * Represents a misconfiguration in the test setup.
 * (e.g., Missing dependency, ambiguous annotations, invalid annotation values).
 */
data object ConfigurationErrorRule : AssertionRule {
    override val key: String = "ConfigurationError"
}
