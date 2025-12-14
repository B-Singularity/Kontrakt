package execution.domain.generator

import kotlin.reflect.KParameter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class TypeGeneratorContractTest<T : TypeGenerator> {

    abstract val generator: T
    abstract val supportedTestCase: KParameter
    abstract val unsupportedTestCase: KParameter

    @Test
    fun `supports returns true for valid parameter`() {
        assertTrue(generator.supports(supportedTestCase), "Should support valid parameter")
    }

    @Test
    fun `supports returns false for invalid parameter`() {
        assertFalse(generator.supports(unsupportedTestCase), "Should not support invalid parameter")
    }

    @Test
    fun `generate returns value when supported`() {
        if (generator.supports(supportedTestCase)) {
            val result = generator.generate(supportedTestCase)
            assertNotNull(result, "Generated value should not be null for supported type")
        }
    }

    @Test
    fun `generateValidBoundaries returns non-empty list`() {
        if (generator.supports(supportedTestCase)) {
            val boundaries = generator.generateValidBoundaries(supportedTestCase)
            assertTrue(boundaries.isNotEmpty(), "Boundaries should not be empty")
        }
    }
}