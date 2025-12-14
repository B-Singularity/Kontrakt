package execution.domain.generator

import discovery.api.AssertFalse
import discovery.api.AssertTrue
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BooleanTypeGeneratorTest {

    private val generator = BooleanTypeGenerator()

    @Suppress("UNUSED_PARAMETER")
    fun plainBoolean(arg: Boolean) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun assertTrue(@AssertTrue arg: Boolean) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun assertFalse(@AssertFalse arg: Boolean) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun notBoolean(arg: String) {
    }

    private fun getParam(func: KFunction<*>): KParameter {
        return func.parameters.first()
    }

    @Test
    fun `support returns true only for Boolean type`() {
        val boolParam = getParam(::plainBoolean)
        val stringParam = getParam(::notBoolean)

        assertTrue(generator.supports(boolParam), "Should support Boolean type")
        assertFalse(generator.supports(stringParam), "Should not support String type")
    }

    @Test
    fun `Plain Boolean - generate both and false boundaries`() {
        val param = getParam(::plainBoolean)

        val boundaries = generator.generateValidBoundaries(param)
        assertEquals(2, boundaries.size, "Should generate 2 boundary values")
        assertTrue(boundaries.contains(true), "Should contain true")
        assertTrue(boundaries.contains(false), "Should contain false")

        val generated = generator.generate(param)
        assertIs<Boolean>(generated, "Generated value should be of type Boolean")

        val invalids = generator.generateInvalid(param)
        assertTrue(invalids.isEmpty(), "There should be no invalid values for plain boolean")
    }

    @Test
    fun `AsserTrue - generates only true as valid`() {
        val param = getParam(::assertTrue)

        val boundaries = generator.generateValidBoundaries(param)
        assertEquals(1, boundaries.size)
        assertEquals(true, boundaries.first(), "Should only contain true")

        val generated = generator.generate(param)
        assertEquals(true, generated, "Should generate true")

        val invalids = generator.generateInvalid(param)
        assertEquals(1, invalids.size)
        assertEquals(false, invalids.first(), "Invalid value should be false")
    }

    @Test
    fun `AssertFalse - generates only false as valid`() {
        val param = getParam(::assertFalse)

        val boundaries = generator.generateValidBoundaries(param)
        assertEquals(1, boundaries.size)
        assertEquals(false, boundaries.first(), "Should only contain false")

        val generated = generator.generate(param)
        assertEquals(false, generated, "Should generate false")

        val invalids = generator.generateInvalid(param)
        assertEquals(1, invalids.size)
        assertEquals(true, invalids.first(), "Invalid value should be true")
    }
}