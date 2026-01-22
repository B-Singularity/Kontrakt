package execution.adapter.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserControlOptionsTest {


    @Test
    fun `default values should be set correctly when using empty constructor`() {
        val options = UserControlOptions()

        assertThat(options.traceMode).isFalse()
        assertThat(options.testPatterns).isEmpty()
        assertThat(options.packageScope).isNull()
        assertThat(options.archiveMode).isFalse()
        assertThat(options.verbosity).isEqualTo(UserControlOptions.Verbosity.NORMAL)
        assertThat(options.seed).isNull()
        assertThat(options.stackTraceLimit).isEqualTo(15)
    }

    @Test
    fun `DEFAULT constant should match default constructor values`() {
        val defaultOptions = UserControlOptions.DEFAULT
        val newOptions = UserControlOptions()

        assertThat(defaultOptions).isEqualTo(newOptions)
    }

    // =================================================================================================================
    // Helper Properties Tests
    // =================================================================================================================

    @Test
    fun `isVerbose should return true when verbosity is VERBOSE`() {
        val options = UserControlOptions(verbosity = UserControlOptions.Verbosity.VERBOSE)

        assertThat(options.isVerbose).isTrue()
    }

    @Test
    fun `isVerbose should return false when verbosity is NORMAL`() {
        val options = UserControlOptions(verbosity = UserControlOptions.Verbosity.NORMAL)

        assertThat(options.isVerbose).isFalse()
    }

    @Test
    fun `isVerbose should return false when verbosity is QUIET`() {
        val options = UserControlOptions(verbosity = UserControlOptions.Verbosity.QUIET)

        assertThat(options.isVerbose).isFalse()
    }

    // =================================================================================================================
    // Value Object Semantics Tests
    // =================================================================================================================

    @Test
    fun `equality check should work correctly based on content`() {
        val options1 = UserControlOptions(traceMode = true, seed = 123L)
        val options2 = UserControlOptions(traceMode = true, seed = 123L)
        val options3 = UserControlOptions(traceMode = false, seed = 123L)

        assertThat(options1).isEqualTo(options2)
        assertThat(options1).isNotEqualTo(options3)
        assertThat(options1.hashCode()).isEqualTo(options2.hashCode())
    }

    @Test
    fun `copy should allow modifying individual properties`() {
        val original = UserControlOptions(traceMode = false, seed = 100L)

        val modified = original.copy(traceMode = true)

        assertThat(modified.traceMode).isTrue()
        assertThat(modified.seed).isEqualTo(100L) // Should remain unchanged
        assertThat(original.traceMode).isFalse() // Original should remain unchanged
    }
}