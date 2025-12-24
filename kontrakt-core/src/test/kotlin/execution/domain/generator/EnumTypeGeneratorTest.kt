package execution.domain.generator

import execution.exception.GenerationFailedException
import java.time.Clock
import kotlin.random.Random
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EnumTypeGeneratorTest {

    private val generator = EnumTypeGenerator()

    private val context = GenerationContext(
        seededRandom = Random(42),
        clock = Clock.systemDefaultZone()
    )

    // =================================================================
    // 1. Test Data
    // =================================================================

    // [Case 1] Standard Enum
    enum class RGB { RED, GREEN, BLUE }

    // [Case 2] Single Value Enum
    enum class Singleton { ONLY }

    // [Case 3] Empty Enum (Edge Case)
    enum class EmptyEnum {; }

    // [Case 4] Non-Enum Class (Invalid Parameter)
    data class NotAnEnum(val name: String)

    // =================================================================
    // 2. Helpers
    // =================================================================

    @Suppress("UNUSED_PARAMETER")
    class TestTargets {
        fun rgb(e: RGB) {}
        fun singleton(e: Singleton) {}
        fun empty(e: EmptyEnum) {}
        fun notEnum(n: NotAnEnum) {}
        fun <T> generic(t: T) {}
    }

    private fun request(funcName: String): GenerationRequest {
        val func = TestTargets::class.functions.find { it.name == funcName }!!
        val param = func.parameters.last()
        return GenerationRequest.from(param)
    }

    // =================================================================
    // 3. Support Contract
    // =================================================================

    @Test
    fun `Support Contract - returns true for valid Enums`() {
        // [Existing] Standard Enum
        assertTrue(generator.supports(request("rgb")), "Should support standard Enum")

        // [Existing] Singleton Enum
        assertTrue(generator.supports(request("singleton")), "Should support singleton Enum")

        // [Existing] Empty Enum
        assertTrue(generator.supports(request("empty")), "Should support empty Enum")
    }

    @Test
    fun `Support Contract - returns false for invalid types`() {
        // [Existing] Non-Enum Class
        assertFalse(generator.supports(request("notEnum")), "Should NOT support regular Class")

        // [Existing] Generic Type Parameter
        assertFalse(generator.supports(request("generic")), "Should NOT support Generic Type Parameters")
    }

    // =================================================================
    // 4. Generation Contract
    // =================================================================

    @Test
    fun `Generation Contract - generates valid enum constants`() {
        // [Existing] Standard Enum Random Selection
        val req = request("rgb")
        repeat(10) {
            val result = generator.generate(req, context)
            assertIs<RGB>(result)
            assertTrue(result in RGB.values())
        }
    }

    @Test
    fun `Generation Contract - works for single value enum`() {
        // [Existing] Singleton Enum
        val req = request("singleton")
        val result = generator.generate(req, context)
        assertEquals(Singleton.ONLY, result)
    }

    // =================================================================
    // 5. Exception Contract & Parameter Tests
    // =================================================================

    @Test
    fun `Exception Contract - throws exception when enum is empty`() {
        // [Existing & Branch Coverage] constants is Empty
        val req = request("empty")

        val ex = assertFailsWith<GenerationFailedException> {
            generator.generate(req, context)
        }

        assertTrue(ex.message!!.contains("defines no constants"))
        assertEquals(req.type, ex.type)
    }

    @Test
    fun `Parameter Contract - throws exception when non-enum is passed bypassing supports`() {
        val req = request("notEnum")

        val ex = assertFailsWith<GenerationFailedException> {
            generator.generate(req, context)
        }

        // Logic: kClass.java.enumConstants returns null -> Caught by isNullOrEmpty()
        assertTrue(ex.message!!.contains("defines no constants"))
        assertEquals(req.type, ex.type)
    }

    // =================================================================
    // 6. Boundaries Contract
    // =================================================================

    @Test
    fun `Boundaries Contract - returns all constants for standard enum`() {
        // [Existing] Standard Enum
        val boundaries = generator.generateValidBoundaries(request("rgb"), context)
        assertEquals(3, boundaries.size)
        assertTrue(boundaries.containsAll(listOf(RGB.RED, RGB.GREEN, RGB.BLUE)))
    }

    @Test
    fun `Boundaries Contract - returns single constant for singleton enum`() {
        // [Existing] Singleton Enum
        val boundaries = generator.generateValidBoundaries(request("singleton"), context)
        assertEquals(1, boundaries.size)
        assertEquals(Singleton.ONLY, boundaries[0])
    }

    @Test
    fun `Boundaries Contract - returns empty list for empty enum`() {
        // [Existing] Empty Enum
        val boundaries = generator.generateValidBoundaries(request("empty"), context)
        assertTrue(boundaries.isEmpty(), "Should return empty list for Empty Enum")
    }

    @Test
    fun `Boundaries Contract - returns empty list for non-enum parameter`() {
        // Logic: enumConstants is null -> safe call ?.toList() ?: emptyList()
        val boundaries = generator.generateValidBoundaries(request("notEnum"), context)
        assertTrue(boundaries.isEmpty(), "Should return empty list for non-enum type")
    }
}