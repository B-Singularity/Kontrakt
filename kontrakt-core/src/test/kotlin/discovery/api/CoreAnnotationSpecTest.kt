package discovery.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

class CoreAnnotationSpecTest {

    companion object {
        /**
         * [Group 1] Class-level Annotations (Interfaces/Classes)
         * These define the type of contract or test container.
         */
        @JvmStatic
        fun provideClassLevelAnnotations(): Stream<KClass<out Annotation>> {
            return Stream.of(
                Contract::class,
                DataContract::class,
                KontraktTest::class,
                Stateful::class
            )
        }

        /**
         * [Group 2] Function-level Annotations
         * These mark specific methods as executable tests.
         */
        @JvmStatic
        fun provideFunctionLevelAnnotations(): Stream<KClass<out Annotation>> {
            return Stream.of(
                Test::class
            )
        }
    }

    // 1. Common Rule: All framework annotations must be available at RUNTIME.
    @ParameterizedTest
    @MethodSource("provideClassLevelAnnotations", "provideFunctionLevelAnnotations")
    fun `must have RUNTIME retention`(annotationClass: KClass<out Annotation>) {
        val retention = annotationClass.annotations.find { it is Retention } as? Retention

        assertThat(retention)
            .withFailMessage { "${annotationClass.simpleName} must have @Retention(RUNTIME)" }
            .isNotNull

        assertThat(retention!!.value)
            .withFailMessage { "${annotationClass.simpleName} retention must be RUNTIME" }
            .isEqualTo(RUNTIME)
    }

    // 2. Class Rule: Markers like @Contract must target CLASS.
    @ParameterizedTest
    @MethodSource("provideClassLevelAnnotations")
    fun `class-level annotations must target CLASS`(annotationClass: KClass<out Annotation>) {
        val target = annotationClass.annotations.find { it is Target } as? Target

        assertThat(target)
            .withFailMessage { "${annotationClass.simpleName} must have @Target(CLASS)" }
            .isNotNull

        assertThat(target!!.allowedTargets)
            .withFailMessage { "${annotationClass.simpleName} must target CLASS" }
            .contains(CLASS)
    }

    // 3. Function Rule: Executable markers like @Test must target FUNCTION.
    @ParameterizedTest
    @MethodSource("provideFunctionLevelAnnotations")
    fun `function-level annotations must target FUNCTION`(annotationClass: KClass<out Annotation>) {
        val target = annotationClass.annotations.find { it is Target } as? Target

        assertThat(target)
            .withFailMessage { "${annotationClass.simpleName} must have @Target(FUNCTION)" }
            .isNotNull

        assertThat(target!!.allowedTargets)
            .withFailMessage { "${annotationClass.simpleName} must target FUNCTION" }
            .contains(FUNCTION)
    }
}