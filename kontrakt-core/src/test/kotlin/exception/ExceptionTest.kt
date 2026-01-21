package exception

import execution.domain.vo.verification.AssertionRule
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KontraktExceptionTest {

    // -- Base Exception (Direct Instantiation for 100% Coverage) --

    @Test
    fun `base - creates instance directly with default cause`() {
        // [Key Point] 하위 클래스가 아니라, KontraktException을 '직접' 생성해야
        // (String, Throwable, int, DefaultConstructorMarker) 생성자가 호출됩니다.
        val exception = KontraktException("Base error")

        assertThat(exception.message).isEqualTo("Base error")
        assertThat(exception.cause).isNull()
    }

    @Test
    fun `base - creates instance directly with explicit cause`() {
        val cause = RuntimeException("Cause")
        val exception = KontraktException("Base error", cause)

        assertThat(exception.message).isEqualTo("Base error")
        assertThat(exception.cause).isEqualTo(cause)
    }

    // -- Configuration Exception --

    @Test
    fun `config - creates instance with explicit cause`() {
        val cause = IllegalArgumentException("Root cause")
        val exception = KontraktConfigurationException("Invalid setup", cause)

        assertThat(exception.message).isEqualTo("[Configuration Error] Invalid setup")
        assertThat(exception.cause).isEqualTo(cause)
    }

    @Test
    fun `config - creates instance with default cause (null)`() {
        val exception = KontraktConfigurationException("Invalid setup")

        assertThat(exception.message).isEqualTo("[Configuration Error] Invalid setup")
        assertThat(exception.cause).isNull()
    }

    // -- Contract Violation Exception --

    @Test
    fun `violation - creates instance with default cause`() {
        val rule = mockk<AssertionRule>()
        val exception = ContractViolationException(rule, "Value violation")

        assertThat(exception.message).isEqualTo("[Contract Violation] Value violation")
        assertThat(exception.rule).isEqualTo(rule)
        assertThat(exception.cause).isNull()
    }

    @Test
    fun `violation - creates instance with explicit cause`() {
        val rule = mockk<AssertionRule>()
        val cause = IllegalStateException("State error")
        val exception = ContractViolationException(rule, "Value violation", cause)

        assertThat(exception.message).isEqualTo("[Contract Violation] Value violation")
        assertThat(exception.cause).isEqualTo(cause)
    }

    // -- Internal Exception --

    @Test
    fun `internal - creates instance with explicit cause`() {
        val cause = NullPointerException("NPE")
        val exception = KontraktInternalException("Reflection failed", cause)

        assertThat(exception.message).isEqualTo("[Internal Error] Reflection failed")
        assertThat(exception.cause).isEqualTo(cause)
    }

    @Test
    fun `internal - creates instance with default cause (null)`() {
        val exception = KontraktInternalException("Reflection failed")

        assertThat(exception.message).isEqualTo("[Internal Error] Reflection failed")
        assertThat(exception.cause).isNull()
    }
}