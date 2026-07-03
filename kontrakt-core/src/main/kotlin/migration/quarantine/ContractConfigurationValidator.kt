package migration.quarantine

import execution.domain.exception.InvalidAnnotationValueException
import realization.execution.generation.GenerationRequest
import stage.input.contract.AssertFalse
import stage.input.contract.AssertTrue
import stage.input.contract.DecimalMax
import stage.input.contract.DecimalMin
import stage.input.contract.Digits
import stage.input.contract.DoubleRange
import stage.input.contract.Email
import stage.input.contract.Future
import stage.input.contract.FutureOrPresent
import stage.input.contract.IntRange
import stage.input.contract.LongRange
import stage.input.contract.Negative
import stage.input.contract.NegativeOrZero
import stage.input.contract.NotBlank
import stage.input.contract.NotEmpty
import stage.input.contract.NotNull
import stage.input.contract.Null
import stage.input.contract.Past
import stage.input.contract.PastOrPresent
import stage.input.contract.Pattern
import stage.input.contract.Positive
import stage.input.contract.PositiveOrZero
import stage.input.contract.Size
import stage.input.contract.StringLength
import stage.input.contract.Url
import stage.input.contract.Uuid
import java.time.temporal.Temporal
import java.util.Date

/**
 * Validates the logical consistency of annotations *before* any value generation attempts.
 *
 * Unlike [ContractValidator] (which checks actual values),
 * this validator checks if the *annotations themselves* form a valid contract configuration.
 */
object ContractConfigurationValidator {
    private val VALUE_CONSTRAINTS =
        setOf(
            AssertTrue::class,
            AssertFalse::class,
            IntRange::class,
            LongRange::class,
            DoubleRange::class,
            DecimalMin::class,
            DecimalMax::class,
            Digits::class,
            Positive::class,
            PositiveOrZero::class,
            Negative::class,
            NegativeOrZero::class,
            StringLength::class,
            NotBlank::class,
            Pattern::class,
            Email::class,
            Url::class,
            Uuid::class,
            Past::class,
            PastOrPresent::class,
            Future::class,
            FutureOrPresent::class,
            Size::class,
            NotEmpty::class,
        )

    private val RULES: List<ValidationRule> =
        listOf(
            MutuallyExclusiveRule(
                name = "NullabilityConflict",
                annotations = setOf(Null::class, NotNull::class),
                reason = "A field cannot be strictly marked as both @Null and @NotNull. Please choose one.",
            ),
            MutuallyExclusiveRule(
                name = "BooleanLogicConflict",
                annotations = setOf(AssertTrue::class, AssertFalse::class),
                reason = "A boolean value cannot be required to be both True and False at the same time.",
            ),
            ForbiddenCombinationRule(
                name = "NullAndValueConstraint",
                trigger = Null::class,
                forbidden = VALUE_CONSTRAINTS,
                reason = "A field marked with @Null implies no value exists. Therefore, it cannot have value constraints.",
            ),
            MutuallyExclusiveRule(
                name = "TimeConstraintConflict",
                annotations =
                    setOf(
                        Past::class,
                        PastOrPresent::class,
                        Future::class,
                        FutureOrPresent::class,
                    ),
                reason = "Time constraints are mutually exclusive or redundant. Please apply only one time constraint per field.",
            ),
            MutuallyExclusiveRule(
                name = "SignConflict",
                annotations =
                    setOf(
                        Positive::class,
                        PositiveOrZero::class,
                        Negative::class,
                        NegativeOrZero::class,
                    ),
                reason = "A numeric value cannot be strictly Positive and strictly Negative simultaneously.",
            ),
            MutuallyExclusiveRule(
                name = "StringFormatConflict",
                annotations = setOf(Email::class, Url::class, Uuid::class),
                reason = "Field cannot enforce multiple conflicting formats (Email, URL, UUID) simultaneously.",
            ),
            TypeCompatibilityRule(
                name = "StringAnnotationMismatch",
                targetAnnotation = Pattern::class,
                allowedTypes = setOf(CharSequence::class),
                reason = "@Pattern can only be applied to String or CharSequence types.",
            ),
            TypeCompatibilityRule(
                name = "EmailTypeMismatch",
                targetAnnotation = Email::class,
                allowedTypes = setOf(CharSequence::class),
                reason = "@Email can only be applied to String types.",
            ),
            // [Numeric Only] Positive, Negative, Digits -> Number
            TypeCompatibilityRule(
                name = "PositiveTypeMismatch",
                targetAnnotation = Positive::class,
                allowedTypes = setOf(Number::class), // Int, Long, Double, BigDecimal...
                reason = "@Positive can only be applied to Numeric types.",
            ),
            TypeCompatibilityRule(
                name = "DigitsTypeMismatch",
                targetAnnotation = Digits::class,
                allowedTypes = setOf(Number::class),
                reason = "@Digits can only be applied to Numeric types.",
            ),
            // [Time Only] Past, Future -> Temporal
            TypeCompatibilityRule(
                name = "FutureTypeMismatch",
                targetAnnotation = Future::class,
                allowedTypes = setOf(Temporal::class, Date::class),
                reason = "@Future can only be applied to Time/Date types.",
            ),
            // [Collection/String Only] Size -> Collection, Map, Array, String
            TypeCompatibilityRule(
                name = "SizeAnnotationMismatch",
                targetAnnotation = Size::class,
                allowedTypes = setOf(Collection::class, Map::class, Array::class, CharSequence::class),
                reason = "@Size can only be applied to Collections, Maps, Arrays, or Strings.",
            ),
            AnnotationValueRule(
                name = "SizeLogicValidation",
                target = Size::class,
            ) { request, size ->
                if (size.min < 0) {
                    throw InvalidAnnotationValueException(
                        fieldName = request.name,
                        value = "min=${size.min}",
                        reason = "@Size min must be non-negative.",
                    )
                }
                if (size.max != Int.MAX_VALUE && size.min > size.max) {
                    throw InvalidAnnotationValueException(
                        fieldName = request.name,
                        value = "min=${size.min}, max=${size.max}",
                        reason = "@Size min cannot be greater than max.",
                    )
                }
            },
            AnnotationValueRule(
                name = "StringLengthLogicValidation",
                target = StringLength::class,
            ) { request, len ->
                if (len.min < 0) {
                    throw InvalidAnnotationValueException(
                        fieldName = request.name,
                        value = "min=${len.min}",
                        reason = "@StringLength min must be non-negative.",
                    )
                }
                if (len.max != Int.MAX_VALUE && len.min > len.max) {
                    throw InvalidAnnotationValueException(
                        fieldName = request.name,
                        value = "min=${len.min}, max=${len.max}",
                        reason = "@StringLength min cannot be greater than max.",
                    )
                }
            },
        )

    fun validate(request: GenerationRequest) {
        RULES.forEach { rule ->
            rule.validate(request)
        }
    }
}
