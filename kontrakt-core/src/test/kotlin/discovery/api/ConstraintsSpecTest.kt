package discovery.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KClass

class ConstraintsSpecTest {

    companion object {
        @JvmStatic
        fun provideConstraintAnnotations(): Stream<KClass<out Annotation>> {
            return Stream.of(
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
                NotEmpty::class
            )
        }
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