package discovery.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
import java.util.stream.Stream
import kotlin.reflect.KClass

class ConstraintsSpecTest {
    companion object {
        @JvmStatic
        fun provideConstraintAnnotations(): Stream<KClass<out Annotation>> =
            Stream.of(
                NotNull::class,
                Null::class,
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
    }

    @ParameterizedTest
    @MethodSource("provideConstraintAnnotations")
    fun `must have RUNTIME retention`(annotationClass: KClass<out Annotation>) {
        val retention = annotationClass.annotations.find { it is Retention } as? Retention

        assertThat(retention)
            .withFailMessage { "${annotationClass.simpleName} must have @Retention annotation" }
            .isNotNull

        assertThat(retention!!.value)
            .withFailMessage { "${annotationClass.simpleName} retention must be RUNTIME" }
            .isEqualTo(AnnotationRetention.RUNTIME)
    }
}
