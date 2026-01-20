package common.util

import exception.ContractViolationException
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.domain.vo.verification.AssertionRule
import execution.domain.vo.verification.SourceLocation
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationTargetException

/**
 * [ADR-025] Test Interface Pattern.
 * Defines the contract for [ExceptionUtils] behavior.
 * Now includes edge cases for 100% Branch Coverage.
 */
interface ExceptionUtilsContract {

    // region Property: unwrapped

    @Test
    fun `unwrapped - returns the target KontraktException when wrapped in InvocationTargetException`() {
        val rootCause = KontraktInternalException("Framework Bug")
        val wrapped = InvocationTargetException(rootCause)
        assertThat(wrapped.unwrapped).isEqualTo(rootCause)
    }

    @Test
    fun `unwrapped - recursively unwraps multiple layers of wrappers`() {
        val rootCause = KontraktConfigurationException("Invalid Configuration")
        val layer1 = InvocationTargetException(rootCause)
        val layer2 = InvocationTargetException(layer1)
        val layer3 = InvocationTargetException(layer2)
        assertThat(layer3.unwrapped).isEqualTo(rootCause)
    }

    @Test
    fun `unwrapped - returns the exception itself if it is not wrapped`() {
        val ex = NullPointerException("User Code NPE")
        assertThat(ex.unwrapped).isEqualTo(ex)
    }

    @Test
    fun `unwrapped - handles InvocationTargetException with null target (Edge Case)`() {
        // [Branch Coverage]: 'current = current.targetException ?: break'
        // If target is null, it should break and return the current wrapper.
        val wrapped = InvocationTargetException(null)
        assertThat(wrapped.unwrapped).isEqualTo(wrapped)
    }

    // endregion

    // region Function: extractSourceLocation

    @Test
    fun `extractSourceLocation - prioritizes returning the target class location if present`() {
        val ex = KontraktInternalException("Error")
        val targetKClass = String::class // Use standard class to avoid null qualifiedName

        ex.stackTrace = arrayOf(
            StackTraceElement(targetKClass.qualifiedName, "targetMethod", "TargetFile.kt", 20),
            StackTraceElement("com.user.Caller", "call", "Caller.kt", 30)
        )

        val location = ex.extractSourceLocation(targetKClass)

        assertThat(location).isInstanceOf(SourceLocation.Exact::class.java)
        val exact = location as SourceLocation.Exact
        assertThat(exact.className).isEqualTo(targetKClass.qualifiedName)
        assertThat(exact.lineNumber).isEqualTo(20)
    }

    @Test
    fun `extractSourceLocation - falls back to Smart Filter if target class is not found`() {
        val ex = KontraktInternalException("Error")
        val targetKClass = String::class

        ex.stackTrace = arrayOf(
            StackTraceElement("com.user.Caller", "call", "Caller.kt", 30)
        )

        val location = ex.extractSourceLocation(targetKClass)

        assertThat(location).isInstanceOf(SourceLocation.Exact::class.java)
        assertThat((location as SourceLocation.Exact).className).isEqualTo("com.user.Caller")
    }

    @Test
    fun `extractSourceLocation - returns first user code frame skipping ignored framework prefixes`() {
        val ex = RuntimeException("User Error")
        ex.stackTrace = arrayOf(
            StackTraceElement("execution.Engine", "run", "Engine.kt", 10),
            StackTraceElement("com.myservice.UserService", "createUser", "UserService.kt", 55),
            StackTraceElement("org.junit.runner.Runner", "run", "Runner.java", 100)
        )

        val location = ex.extractSourceLocation()

        assertThat(location).isInstanceOf(SourceLocation.Exact::class.java)
        val exact = location as SourceLocation.Exact
        assertThat(exact.className).isEqualTo("com.myservice.UserService")
        assertThat(exact.lineNumber).isEqualTo(55)
    }

    @Test
    fun `extractSourceLocation - returns Unknown if all frames are ignored`() {
        val ex = KontraktInternalException("Internal")
        ex.stackTrace = arrayOf(
            StackTraceElement("execution.Internal", "run", "Internal.kt", 1)
        )

        val location = ex.extractSourceLocation()
        assertThat(location).isEqualTo(SourceLocation.Unknown)
    }

    @Test
    fun `extractSourceLocation - handles StackTraceElement with null fileName (Edge Case)`() {
        // [Branch Coverage]: 'fileName = this.fileName ?: "UnknownSource"'
        val ex = Exception()
        val nullFileElement = StackTraceElement("com.user.Unknown", "method", null, -1)
        ex.stackTrace = arrayOf(nullFileElement)

        val location = ex.extractSourceLocation()

        assertThat(location).isInstanceOf(SourceLocation.Exact::class.java)
        val exact = location as SourceLocation.Exact
        assertThat(exact.fileName).isEqualTo("UnknownSource")
    }

    // endregion

    // region Function: sanitizeStackTrace

    @Test
    fun `sanitizeStackTrace - does nothing if verbose mode is true`() {
        val ex = KontraktInternalException("Error")
        val originalTrace = arrayOf(
            StackTraceElement("execution.Internal", "run", "Internal.kt", 1)
        )
        ex.stackTrace = originalTrace

        val result = ex.sanitizeStackTrace(verbose = true)
        assertThat(result.stackTrace).isEqualTo(originalTrace)
    }

    @Test
    fun `sanitizeStackTrace - filters out ignored prefixes if verbose is false`() {
        val ex = Exception()
        ex.stackTrace = arrayOf(
            StackTraceElement("execution.Internal", "run", "Internal.kt", 1), // Remove
            StackTraceElement("com.user.BizLogic", "doIt", "Biz.kt", 10) // Keep
        )

        ex.sanitizeStackTrace(verbose = false)
        assertThat(ex.stackTrace).hasSize(1)
        assertThat(ex.stackTrace[0].className).isEqualTo("com.user.BizLogic")
    }

    @Test
    fun `sanitizeStackTrace - keeps the top frame if filtering removes all frames`() {
        val ex = KontraktInternalException("Deep Internal Error")
        val internalFrame = StackTraceElement("execution.Core", "fail", "Core.kt", 50)
        ex.stackTrace = arrayOf(internalFrame)

        ex.sanitizeStackTrace(verbose = false)
        assertThat(ex.stackTrace).hasSize(1)
        assertThat(ex.stackTrace[0]).isEqualTo(internalFrame)
    }

    @Test
    fun `sanitizeStackTrace - sanitizes the cause recursively`() {
        val cause = KontraktInternalException("Root Cause")
        cause.stackTrace = arrayOf(
            StackTraceElement("execution.Internal", "run", "Internal.kt", 1),
            StackTraceElement("com.user.Logic", "fail", "Logic.kt", 10)
        )
        val wrapper = RuntimeException("Wrapper", cause)
        wrapper.stackTrace = arrayOf(StackTraceElement("com.user.Main", "main", "Main.kt", 1))

        wrapper.sanitizeStackTrace(verbose = false)
        assertThat(wrapper.cause!!.stackTrace).hasSize(1)
        assertThat(wrapper.cause!!.stackTrace[0].className).isEqualTo("com.user.Logic")
    }

    @Test
    fun `sanitizeStackTrace - handles empty stack trace (Edge Case)`() {
        // [Branch Coverage]: 'if (filteredTrace.isEmpty() && originalTrace.isNotEmpty())'
        // Need to test when originalTrace IS empty.
        val ex = Exception()
        ex.stackTrace = emptyArray()

        ex.sanitizeStackTrace(verbose = false)
        assertThat(ex.stackTrace).isEmpty()
    }

    // endregion

    // region Function: analyzeBlame

    @Test
    fun `analyzeBlame - categorizes AssertionError as TEST_FAILURE`() {
        assertThat(AssertionError().analyzeBlame()).isEqualTo(Blame.TEST_FAILURE)
    }

    @Test
    fun `analyzeBlame - categorizes ContractViolationException as TEST_FAILURE`() {
        // [Branch Coverage]: 'is AssertionError, is ContractViolationException'
        // Must explicitly hit the second type in the OR condition.
        val rule = mockk<AssertionRule>()
        val ex = ContractViolationException(rule, "violation")
        assertThat(ex.analyzeBlame()).isEqualTo(Blame.TEST_FAILURE)
    }

    @Test
    fun `analyzeBlame - categorizes configuration exceptions as SETUP_FAILURE`() {
        assertThat(KontraktConfigurationException("Bad Config").analyzeBlame()).isEqualTo(Blame.SETUP_FAILURE)
    }

    @Test
    fun `analyzeBlame - categorizes internal framework exceptions as INTERNAL_ERROR`() {
        assertThat(KontraktInternalException("Bug").analyzeBlame()).isEqualTo(Blame.INTERNAL_ERROR)
    }

    @Test
    fun `analyzeBlame - categorizes unexpected user runtime exceptions as EXECUTION_FAILURE`() {
        assertThat(NullPointerException().analyzeBlame()).isEqualTo(Blame.EXECUTION_FAILURE)
        assertThat(IllegalArgumentException().analyzeBlame()).isEqualTo(Blame.EXECUTION_FAILURE)
    }

    // endregion
}

/**
 * [ADR-025] Concrete Implementation.
 */
class ExceptionUtilsTest : ExceptionUtilsContract