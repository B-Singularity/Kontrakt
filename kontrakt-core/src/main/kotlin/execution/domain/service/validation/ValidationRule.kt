package execution.domain.service.validation

import execution.domain.generator.GenerationRequest
import execution.exception.ConflictingAnnotationsException
import kotlin.reflect.KClass

/**
 * Represents a single validation rule to be applied against a [GenerationRequest].
 *
 * This sealed interface allows the definition of various types of validation logic
 * (e.g., mutually exclusive annotations, dependency requirements) that are decoupled
 * from the specific generators.
 */
sealed interface ValidationRule {
    /**
     * Validates the given request against this rule.
     *
     * @param request The generation request containing the annotations to check.
     * @throws ConflictingAnnotationsException If the rule is violated.
     */
    fun validate(request: GenerationRequest)
}

/**
 * [Rule Type 1] Mutually Exclusive Rule.
 *
 * Enforces that only one annotation from the provided [annotations] set exists
 * on the target field. If two or more exist simultaneously, it is considered a conflict.
 *
 * Example: A field cannot be both `@AssertTrue` and `@AssertFalse`.
 *
 * @property name The identifier for this rule (for debugging/logging).
 * @property annotations The set of annotations that are mutually exclusive.
 * @property reason The mandatory human-readable explanation of why these annotations cannot coexist.
 */
data class MutuallyExclusiveRule(
    val name: String,
    val annotations: Set<KClass<out Annotation>>,
    val reason: String,
) : ValidationRule {
    override fun validate(request: GenerationRequest) {
        val presentAnnotations =
            request.annotations
                .map { it.annotationClass }
                .toSet()

        val intersection = annotations.intersect(presentAnnotations)

        if (intersection.size > 1) {
            throw ConflictingAnnotationsException(
                fieldName = request.name,
                annotations = intersection.map { "@${it.simpleName}" },
                reason = this.reason, // Passing the mandatory reason
            )
        }
    }
}

/**
 * [Rule Type 2] Forbidden Combination Rule.
 *
 * Enforces that if a specific [trigger] annotation is present, none of the
 * annotations in the [forbidden] set are allowed to exist.
 *
 * Example: If a field is marked `@Null` (trigger), it cannot have `@Size` (forbidden),
 * because a null value cannot have a size.
 *
 * @property name The identifier for this rule.
 * @property trigger The annotation that triggers this validation check.
 * @property forbidden The set of annotations that are not allowed if the trigger is present.
 * @property reason The mandatory human-readable explanation of the conflict.
 */
data class ForbiddenCombinationRule(
    val name: String,
    val trigger: KClass<out Annotation>,
    val forbidden: Set<KClass<out Annotation>>,
    val reason: String,
) : ValidationRule {
    override fun validate(request: GenerationRequest) {
        // If the trigger annotation is not present, this rule does not apply.
        if (request.annotations.none { it.annotationClass == trigger }) return

        val presentAnnotations =
            request.annotations
                .map { it.annotationClass }
                .toSet()

        val conflicts = forbidden.intersect(presentAnnotations)

        if (conflicts.isNotEmpty()) {
            throw ConflictingAnnotationsException(
                fieldName = request.name,
                annotations = (conflicts + trigger).map { "@${it.simpleName}" },
                reason = this.reason, // Passing the mandatory reason
            )
        }
    }
}

/**
 * [Rule Type 3] Type Compatibility Rule.
 *
 * Checks if the [targetAnnotation] is applied to a field of a compatible type.
 * If the field type is not one of the [allowedTypes], it throws an exception.
 *
 * Example: `@Pattern` is only valid on `String` or `CharSequence`.
 * If applied to an `Int` field, this rule will trigger.
 */
data class TypeCompatibilityRule(
    val name: String,
    val targetAnnotation: KClass<out Annotation>,
    val allowedTypes: Set<KClass<*>>,
    val reason: String,
) : ValidationRule {
    override fun validate(request: GenerationRequest) {
        // 1. If the annotation is not present, skip validation.
        if (request.annotations.none { it.annotationClass == targetAnnotation }) return

        val fieldType = request.type.classifier as? KClass<*> ?: return

        // 2. Compatibility Check (Is fieldType a subtype of any allowedType?)
        // handling nullable types implicitly by checking the classifier
        val isCompatible =
            allowedTypes.any { allowed ->
                allowed.java.isAssignableFrom(fieldType.java)
            }

        if (!isCompatible) {
            throw ConflictingAnnotationsException(
                fieldName = request.name,
                annotations = listOf("@${targetAnnotation.simpleName}"),
                reason = "$reason (Actual Type: '${fieldType.simpleName}', Allowed: ${allowedTypes.map { it.simpleName }})",
            )
        }
    }
}

/**
 * [Rule Type 4] Annotation Value Logic Rule.
 *
 * Checks the internal values of a specific annotation instance.
 * Unlike other rules that check relationships between annotations, this rule
 * validates that a single annotation's parameters make logical sense.
 *
 * Example: Checking that `@Size(min=5, max=2)` throws an exception because min > max.
 *
 * @param T The type of annotation to validate.
 * @property name The identifier for this rule.
 * @property target The annotation class to look for.
 * @property validator A lambda that takes the annotation instance and performs checks.
 * It should throw an exception (e.g., InvalidAnnotationValueException) if valid.
 */
class AnnotationValueRule<T : Annotation>(
    val name: String,
    val target: KClass<T>,
    val validator: (GenerationRequest, T) -> Unit,
) : ValidationRule {
    override fun validate(request: GenerationRequest) {
        @Suppress("UNCHECKED_CAST")
        val annotation =
            request.annotations
                .find { it.annotationClass == target } as? T ?: return

        validator(request, annotation)
    }
}
