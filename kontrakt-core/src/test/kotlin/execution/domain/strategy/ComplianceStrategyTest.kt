package execution.domain.strategy

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class ComplianceStrategyTest {
    abstract val strategy: ComplianceStrategy

    data class SupportTestCase(
        val kClass: KClass<*>,
        val expected: Boolean,
    )

    data class DecideTestCase(
        val kClass: KClass<*>,
        val expected: StrategyResult,
    )

    abstract fun provideSupportTestCases(): List<SupportTestCase>

    abstract fun provideDecideTestCases(): List<DecideTestCase>

    @Test
    fun testSupports() {
        provideSupportTestCases().forEach { (kClass, expected) ->
            val result = strategy.supports(kClass)
            if (expected) {
                assertTrue(result, "Expected supports() to return TRUE for ${kClass.simpleName}")
            } else {
                assertFalse(result, "Expected supports() to return FALSE for ${kClass.simpleName}")
            }
        }
    }

    @Test
    fun testDecide() {
        provideDecideTestCases().forEach { (kClass, expected) ->
            val result = strategy.decide(kClass)
            assertEquals(expected, result, "Failed decide() check for ${kClass.simpleName}")
        }
    }
}
