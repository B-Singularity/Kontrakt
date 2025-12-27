package discovery.domain.aggregate

import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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

        val realStrategy = DependencyMetadata.MockingStrategy.Real

        return constructor.call(
            "dummyDep",
            String::class,
            realStrategy,
        )
    }
}
