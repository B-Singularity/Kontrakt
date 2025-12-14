package execution.domain.generator

import discovery.api.NotEmpty
import discovery.api.Size
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollectionTypeGeneratorTest {

    private val generator = CollectionTypeGenerator()

    @Suppress("UNUSED_PARAMETER")
    fun plainList(list: List<String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun plainMap(map: Map<String, String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun plainSet(set: Set<String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun notCollection(str: String) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun notEmptyList(@NotEmpty list: List<String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun sizedList(@Size(min = 2, max = 5) list: List<String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun largeMaxList(@Size(min = 1, max = 200) list: List<String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun sizedMap(@Size(min = 3) map: Map<String, String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun overlapRedundant(@Size(min = 3) @NotEmpty list: List<String>) {
    }

    @Suppress("UNUSED_PARAMETER")
    fun overlapConflict(@Size(min = 0) @NotEmpty list: List<String>) {
    }

    private fun getParam(func: KFunction<*>): KParameter {
        return func.parameters.first()
    }

    @Test
    fun `supports - returns true for Collection, List, Set, and Map`() {
        assertTrue(generator.supports(getParam(::plainList)), "Should support List")
        assertTrue(generator.supports(getParam(::plainSet)), "Should support Set")
        assertTrue(generator.supports(getParam(::plainMap)), "Should support Map")
    }

    @Test
    fun `supports - returns false for non-collection types`() {
        assertFalse(generator.supports(getParam(::notCollection)), "Should not support String")
    }

    @Test
    fun `generate - always returns null (delegates to recursion)`() {
        val param = getParam(::plainList)
        val result = generator.generate(param)
        assertNull(result, "generate() should return null to allow FixtureGenerator to handle recursion")
    }

    @Test
    fun `generateInvalid - always returns empty list`() {
        val param = getParam(::plainList)
        val result = generator.generateInvalid(param)
        assertTrue(result.isEmpty(), "generateInvalid() should currently return empty list")
    }

    @Test
    fun `Boundaries - Plain List (Default)`() {
        val param = getParam(::plainList)
        val boundaries = generator.generateValidBoundaries(param)

        // Default: min=0, max=MAX_INT
        // Should generate empty list (size 0)
        assertTrue(boundaries.isNotEmpty())
        val first = boundaries[0] as List<*>
        assertTrue(first.isEmpty(), "Should generate empty list by default")
    }

    @Test
    fun `Boundaries - NotEmpty List`() {
        val param = getParam(::notEmptyList)
        val boundaries = generator.generateValidBoundaries(param)

        // @NotEmpty implies min=1
        // Should generate list with size 1
        val hasSizeOne = boundaries.any { (it as List<*>).size == 1 }
        assertTrue(hasSizeOne, "Should generate list with size 1 for @NotEmpty")
    }

    @Test
    fun `Boundaries - Sized List (Min and Max)`() {
        val param = getParam(::sizedList) // min=2, max=5
        val boundaries = generator.generateValidBoundaries(param)

        // Should generate list of size 2 (min) and size 5 (max)
        val sizes = boundaries.map { (it as List<*>).size }

        assertTrue(sizes.contains(2), "Should contain min size (2)")
        assertTrue(sizes.contains(5), "Should contain max size (5)")
    }

    @Test
    fun `Boundaries - Large Max List (Memory Safety)`() {
        val param = getParam(::largeMaxList) // min=1, max=200

        val boundaries = generator.generateValidBoundaries(param)
        val sizes = boundaries.map { (it as List<*>).size }

        // Should contain min size (1)
        assertTrue(sizes.contains(1), "Should contain min size (1)")

        // Should NOT contain max size (200) because it exceeds the safety limit (100)
        assertFalse(sizes.contains(200), "Should skip max size if it is too large (> 100)")
    }

    @Test
    fun `Boundaries - Map Size`() {
        val param = getParam(::sizedMap) // min=3
        val boundaries = generator.generateValidBoundaries(param)

        // Logic for Map only adds the min size
        val mapBoundary = boundaries.first() as Map<*, *>
        assertEquals(3, mapBoundary.size, "Should generate map with size equal to min (3)")

        // Check content format "k1"="v1"
        assertTrue(mapBoundary.containsKey("k1"))
        assertTrue(mapBoundary.containsValue("v1"))
    }

    @Test
    fun `Boundaries - Overlap Redundant (Size min 3 + NotEmpty)`() {
        val param = getParam(::overlapRedundant)
        val boundaries = generator.generateValidBoundaries(param)
        val sizes = boundaries.map { (it as List<*>).size }

        // Logic: max(3, 1) -> 3
        // Should contain size 3
        assertTrue(sizes.contains(3), "Should respect the stricter @Size(min=3) constraint")

        // Should NOT contain size 1 (from NotEmpty) because 3 is the effective minimum
        assertFalse(sizes.contains(1), "Should not generate size 1 because @Size(min=3) overrides it")
    }

    @Test
    fun `Boundaries - Overlap Conflict (Size min 0 + NotEmpty)`() {
        val param = getParam(::overlapConflict)
        val boundaries = generator.generateValidBoundaries(param)
        val sizes = boundaries.map { (it as List<*>).size }

        // Logic: max(0, 1) -> 1
        // Should contain size 1 (Intersection of Size(0) and NotEmpty)
        assertTrue(sizes.contains(1), "Should upgrade min size to 1 due to @NotEmpty")

        // Should NOT contain size 0 (Empty List)
        assertFalse(sizes.contains(0), "Should not generate empty list because @NotEmpty forbids it")
    }
}