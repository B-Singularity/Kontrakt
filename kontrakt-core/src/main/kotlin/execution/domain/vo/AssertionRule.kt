package execution.domain.vo

import kotlin.reflect.KClass

/**
 * [Value Object] Assertion Rule
 *
 * Defines the "Standard" or "Law" that was applied during a verification step.
 * This sealed hierarchy categorizes the source and nature of a test result.
 */
sealed interface AssertionRule {
    /**
     * A machine-readable identifier for the rule (e.g., "NotNull", "ConstructorSanity").
     * Used for reporting, logging, and statistics.
     */
    val key: String
}

data class AnnotationRule(
    val annotation: KClass<out Annotation>
) : AssertionRule {
    override val key: String = annotation.simpleName ?: "AnonymousAnnotation"
}

/**
 * Marker interface for rules regarding the structural integrity of the object.
 */
sealed interface IntegrityRule : AssertionRule

/**
 * Rule: "A constructor MUST create an instance when provided with valid arguments."
 */
data object ConstructorSanityRule : IntegrityRule {
    override val key: String = "ConstructorSanity"
}

/**
 * Rule: "A constructor MUST throw an exception when provided with invalid arguments."
 * This represents defensive programming checks.
 */
data object DefensiveCheckRule : IntegrityRule {
    override val key: String = "DefensiveValidation"
}

/**
 * Represents an assertion made manually by the user within a @Test method.
 */
data object UserAssertionRule : AssertionRule {
    override val key: String = "UserAssertion"
}

data class UserExceptionRule(
    val exceptionType: String
) : AssertionRule {
    override val key: String = "UserException"
}


/**
 * Represents an unexpected system failure (runtime exception) not related to domain logic.
 */
data class SystemErrorRule(
    val exceptionType: String
) : AssertionRule {
    override val key: String = "SystemError"
}

/**
 * Represents a misconfiguration in the test setup itself.
 */
data object ConfigurationErrorRule : AssertionRule {
    override val key: String = "ConfigurationError"
}