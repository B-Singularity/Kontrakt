package discovery.domain.aggregate

import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestSpecificationTest {
    @Test
    fun `create should succeed for all valid mode combinations`() {
        val validScenarios =
            listOf(
                // Case 1: Only UserScenario
                setOf(TestSpecification.TestMode.UserScenario),
                // Case 2: Only ContractAuto
                setOf(TestSpecification.TestMode.ContractAuto(String::class)),
                // Case 3: Both (Hybrid)
                setOf(TestSpecification.TestMode.UserScenario, TestSpecification.TestMode.ContractAuto(String::class)),
            )

        val target = createDummyTarget()
        val dependencies = listOf(createDummyDependency())

        validScenarios.forEachIndexed { index, modes ->
            val result = TestSpecification.create(target, modes, dependencies)

            assertTrue(result.isSuccess, "Scenario #$index failed. Modes: $modes")

            val spec = result.getOrThrow()
            assertEquals(modes, spec.modes, "Modes mismatch in scenario #$index")
            assertEquals(target, spec.target)
            assertEquals(dependencies, spec.requiredDependencies)
            assertNull(spec.seed, "Seed should be null by default")
        }
    }

    @Test
    fun `create should fail when verification modes are empty`() {
        val target = createDummyTarget()
        val emptyModes = emptySet<TestSpecification.TestMode>()
        val dependencies = listOf(createDummyDependency())

        val result = TestSpecification.create(target, emptyModes, dependencies)

        assertTrue(result.isFailure, "Should fail if modes are empty")
        val exception = result.exceptionOrNull()
        assertIs<IllegalArgumentException>(exception)
        assertEquals("At least one test mode is required.", exception.message)
    }

    @Test
    fun `create should store seed when provided`() {
        val target = createDummyTarget()
        val modes = setOf(TestSpecification.TestMode.UserScenario)
        val dependencies = listOf(createDummyDependency())
        val expectedSeed = 123456789L

        // Explicitly pass the seed
        val result = TestSpecification.create(target, modes, dependencies, expectedSeed)

        assertTrue(result.isSuccess)
        val spec = result.getOrThrow()
        assertEquals(expectedSeed, spec.seed, "Seed should be stored correctly")
    }

    private fun createDummyTarget(): DiscoveredTestTarget {
        val constructor = DiscoveredTestTarget::class.primaryConstructor!!
        constructor.isAccessible = true
        return constructor.call(
            Any::class, // kClass
            "DummyTarget", // displayName
            "com.example.DummyTarget", // fullyQualifiedName
        )
    }

    private fun createDummyDependency(): DependencyMetadata {
        val constructor = DependencyMetadata::class.primaryConstructor!!
        constructor.isAccessible = true

        // Updated: Real strategy now requires an implementation class.
        // We use String::class as a dummy implementation.
        val realStrategy = DependencyMetadata.MockingStrategy.Real(String::class)

        return constructor.call(
            "dummyDep",
            String::class,
            realStrategy,
        )
    }
}
