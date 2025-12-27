package execution.domain.service

import discovery.api.AssertFalse
import discovery.api.AssertTrue
import discovery.api.DecimalMin
import discovery.api.Digits
import discovery.api.Email
import discovery.api.Future
import discovery.api.IntRange
import discovery.api.NegativeOrZero
import discovery.api.NotBlank
import discovery.api.NotEmpty
import discovery.api.NotNull
import discovery.api.Null
import discovery.api.Past
import discovery.api.Pattern
import discovery.api.Positive
import discovery.api.Size
import discovery.api.StringLength
import discovery.api.Url
import exception.ContractViolationException
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.reflect.KAnnotatedElement
import kotlin.test.Test

class ContractValidatorTest {
    // Test environment: Fixed at 2025-01-01 12:00:00 UTC
    private val fixedInstant = Instant.parse("2025-01-01T12:00:00Z")
    private val clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))
    private val validator = ContractValidator(clock)

    // =================================================================
    // 1. Nullability Tests
    // =================================================================

    @Test
    fun `validate throws exception when NotNull is violated`() {
        val notNull = mockAnnotation<NotNull>()
        val element = mockElement(notNull)

        assertThrows<ContractViolationException> {
            validator.validate(element, null)
        }
    }

    @Test
    fun `validate throws exception when Null is violated`() {
        val nullAnno = mockAnnotation<Null>()
        val element = mockElement(nullAnno)

        assertThrows<ContractViolationException> {
            validator.validate(element, "should be null")
        }
    }

    // =================================================================
    // 2. Boolean Tests
    // =================================================================

    @Test
    fun `validate boolean assertions`() {
        // AssertTrue
        val assertTrue = mockAnnotation<AssertTrue>()
        val trueElement = mockElement(assertTrue)

        assertDoesNotThrow { validator.validate(trueElement, true) }
        assertThrows<ContractViolationException> { validator.validate(trueElement, false) }

        // AssertFalse
        val assertFalse = mockAnnotation<AssertFalse>()
        val falseElement = mockElement(assertFalse)

        assertDoesNotThrow { validator.validate(falseElement, false) }
        assertThrows<ContractViolationException> { validator.validate(falseElement, true) }
    }

    // =================================================================
    // 3. Numeric Tests
    // =================================================================

    @Test
    fun `validate numeric ranges`() {
        // @IntRange(min=1, max=10)
        val intRange =
            mockAnnotation<IntRange> {
                Mockito.`when`(min).thenReturn(1)
                Mockito.`when`(max).thenReturn(10)
            }
        val intElement = mockElement(intRange)

        assertDoesNotThrow { validator.validate(intElement, 5) }
        assertThrows<ContractViolationException> { validator.validate(intElement, 11) }

        // @DecimalMin(value="10.5", inclusive=true)
        val decimalMin =
            mockAnnotation<DecimalMin> {
                Mockito.`when`(value).thenReturn("10.5")
                Mockito.`when`(inclusive).thenReturn(true)
            }
        val decimalElement = mockElement(decimalMin)

        assertDoesNotThrow { validator.validate(decimalElement, 10.5) }
        assertThrows<ContractViolationException> { validator.validate(decimalElement, 10.4) }
    }

    @Test
    fun `validate numeric signs`() {
        // @Positive
        val positive = mockAnnotation<Positive>()
        val positiveElement = mockElement(positive)

        assertDoesNotThrow { validator.validate(positiveElement, 1) }
        assertThrows<ContractViolationException> { validator.validate(positiveElement, 0) }
        assertThrows<ContractViolationException> { validator.validate(positiveElement, -1) }

        // @NegativeOrZero
        val negativeOrZero = mockAnnotation<NegativeOrZero>()
        val negElement = mockElement(negativeOrZero)

        assertDoesNotThrow { validator.validate(negElement, 0) }
        assertDoesNotThrow { validator.validate(negElement, -1) }
        assertThrows<ContractViolationException> { validator.validate(negElement, 1) }
    }

    @Test
    fun `validate digits`() {
        // @Digits(integer=3, fraction=2)
        val digits =
            mockAnnotation<Digits> {
                Mockito.`when`(integer).thenReturn(3)
                Mockito.`when`(fraction).thenReturn(2)
            }
        val element = mockElement(digits)

        assertDoesNotThrow { validator.validate(element, BigDecimal("123.45")) }
        assertThrows<ContractViolationException> {
            validator.validate(
                element,
                BigDecimal("1234.5"),
            )
        } // integer part fail
        assertThrows<ContractViolationException> {
            validator.validate(
                element,
                BigDecimal("123.456"),
            )
        } // fraction part fail
    }

    // =================================================================
    // 4. String Tests
    // =================================================================

    @Test
    fun `validate string length and blank`() {
        // @StringLength(min=2, max=5)
        val length =
            mockAnnotation<StringLength> {
                Mockito.`when`(min).thenReturn(2)
                Mockito.`when`(max).thenReturn(5)
            }
        val lenElement = mockElement(length)

        assertDoesNotThrow { validator.validate(lenElement, "Hi") }
        assertThrows<ContractViolationException> { validator.validate(lenElement, "A") }
        assertThrows<ContractViolationException> { validator.validate(lenElement, "TooLong") }

        // @NotBlank
        val notBlank = mockAnnotation<NotBlank>()
        val blankElement = mockElement(notBlank)

        assertThrows<ContractViolationException> { validator.validate(blankElement, "  ") }
    }

    @Test
    fun `validate pattern`() {
        // @Pattern(regexp="^[a-z]+$")
        val pattern =
            mockAnnotation<Pattern> {
                Mockito.`when`(regexp).thenReturn("^[a-z]+$")
            }
        val element = mockElement(pattern)

        assertDoesNotThrow { validator.validate(element, "abc") }
        assertThrows<ContractViolationException> { validator.validate(element, "123") }
    }

    @Test
    fun `validate email`() {
        // @Email(allow=["company.com"], block=["spam.com"])
        val email =
            mockAnnotation<Email> {
                Mockito.`when`(allow).thenReturn(arrayOf("company.com"))
                Mockito.`when`(block).thenReturn(arrayOf("spam.com"))
            }
        val element = mockElement(email)

        assertDoesNotThrow { validator.validate(element, "user@company.com") }
        assertThrows<ContractViolationException> { validator.validate(element, "invalid-email") }
        assertThrows<ContractViolationException> { validator.validate(element, "user@other.com") } // Not Allowed
        assertThrows<ContractViolationException> { validator.validate(element, "user@spam.com") } // Blocked
    }

    @Test
    fun `validate url`() {
        // @Url(protocol=["https"], hostAllow=["secure.com"])
        val url =
            mockAnnotation<Url> {
                Mockito.`when`(protocol).thenReturn(arrayOf("https"))
                Mockito.`when`(hostAllow).thenReturn(arrayOf("secure.com"))
                Mockito.`when`(hostBlock).thenReturn(emptyArray())
            }
        val element = mockElement(url)

        assertDoesNotThrow { validator.validate(element, "https://secure.com/api") }
        assertThrows<ContractViolationException> { validator.validate(element, "http://secure.com") } // Wrong Protocol
        assertThrows<ContractViolationException> { validator.validate(element, "https://hacker.com") } // Wrong Host
    }

    // =================================================================
    // 5. Collection Tests
    // =================================================================

    @Test
    fun `validate collection size`() {
        // @Size(min=1, max=2)
        val size =
            mockAnnotation<Size> {
                Mockito.`when`(min).thenReturn(1)
                Mockito.`when`(max).thenReturn(2)
            }
        val element = mockElement(size)

        assertDoesNotThrow { validator.validate(element, listOf(1)) }
        assertThrows<ContractViolationException> { validator.validate(element, emptyList<Int>()) }
        assertThrows<ContractViolationException> { validator.validate(element, listOf(1, 2, 3)) }
    }

    @Test
    fun `validate not empty collection`() {
        val notEmpty = mockAnnotation<NotEmpty>()
        val element = mockElement(notEmpty)

        assertThrows<ContractViolationException> { validator.validate(element, emptyList<Int>()) }
    }

    // =================================================================
    // 6. Time Tests
    // =================================================================

    @Test
    fun `validate time constraints`() {
        val pastTime = fixedInstant.minus(1, ChronoUnit.HOURS)
        val futureTime = fixedInstant.plus(1, ChronoUnit.HOURS)

        // @Past
        val past = mockAnnotation<Past>()
        val pastElement = mockElement(past)

        assertDoesNotThrow { validator.validate(pastElement, pastTime) }
        assertThrows<ContractViolationException> { validator.validate(pastElement, futureTime) }

        // @Future
        val future = mockAnnotation<Future>()
        val futureElement = mockElement(future)

        assertDoesNotThrow { validator.validate(futureElement, futureTime) }
        assertThrows<ContractViolationException> { validator.validate(futureElement, pastTime) }
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private fun mockElement(vararg annotations: Annotation): KAnnotatedElement {
        val element = Mockito.mock(KAnnotatedElement::class.java)
        Mockito.`when`(element.annotations).thenReturn(annotations.toList())
        return element
    }

    /**
     * [Core] Helper method to mock Annotations.
     * solved generic type inference and `annotationType()` call issues.
     */
    private inline fun <reified T : Annotation> mockAnnotation(crossinline configure: T.() -> Unit = {}): T {
        val annotationClass = T::class.java
        val annotation = Mockito.mock(annotationClass)

        // Use doReturn(...).when(...) to bypass generic type inference issues.
        // We must cast the mock to java.lang.annotation.Annotation to access `annotationType()`.
        Mockito
            .doReturn(annotationClass)
            .`when`(annotation as java.lang.annotation.Annotation)
            .annotationType()

        annotation.configure()
        return annotation
    }
}
