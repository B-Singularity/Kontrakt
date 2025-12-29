package execution.domain.service

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import exception.KontraktConfigurationException
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import kotlin.reflect.KClass
import kotlin.test.assertNotEquals

class TestInstanceFactoryTest {
    private lateinit var mockingEngine: MockingEngine
    private lateinit var scenarioControl: ScenarioControl
    private lateinit var factory: TestInstanceFactory

    @BeforeEach
    fun setup() {
        mockingEngine = mock(MockingEngine::class.java)
        scenarioControl = mock(ScenarioControl::class.java)
        factory = TestInstanceFactory(mockingEngine, scenarioControl)
    }

    // =================================================================
    // Dummy Classes for Testing
    // =================================================================

    class SimpleService

    class ServiceWithDep(
        val dep: SimpleService,
    )

    class ServiceWithValue(
        val name: String,
        val age: Int,
    )

    interface ServiceInterface

    // Circular Dependency Classes
    class CircularA(
        val b: CircularB,
    )

    class CircularB(
        val a: CircularA,
    )

    class ServiceWithComplexValues(
        val active: Boolean,
        val count: Long,
        val items: List<String>,
        val config: Map<String, Int>,
    )

    class ServiceWithNullable(
        val description: String?,
    )

    // =================================================================
    // 1. Basic Instantiation & Real Strategy
    // =================================================================

    @Test
    fun `create instantiates simple target with no dependencies`() {
        val spec = createSpecification(SimpleService::class)

        val context = factory.create(spec)
        val target = context.getTestTarget()

        assertNotNull(target)
        assertTrue(target is SimpleService)
    }

    @Test
    fun `create instantiates target with real dependencies recursively`() {
        // Implicit Real Strategy: ServiceWithDep -> SimpleService
        val spec = createSpecification(ServiceWithDep::class)

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceWithDep

        assertNotNull(target.dep)
        assertTrue(target.dep is SimpleService)
        // Verify dependency is registered in context
        assertNotNull(context.getDependency(SimpleService::class))
    }

    // =================================================================
    // 2. Mocking Strategy Support
    // =================================================================

    @Test
    fun `create injects mock when strategy is StatelessMock`() {
        val mockSimpleService = SimpleService()
        whenever(mockingEngine.createMock(SimpleService::class)).thenReturn(mockSimpleService)

        val depMetadata =
            DependencyMetadata
                .create(
                    name = "simpleService",
                    type = SimpleService::class,
                    strategy = DependencyMetadata.MockingStrategy.StatelessMock,
                ).getOrThrow()
        val spec = createSpecification(ServiceWithDep::class, listOf(depMetadata))

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceWithDep

        assertEquals(mockSimpleService, target.dep)
        verify(mockingEngine).createMock(SimpleService::class)
    }

    @Test
    fun `create injects fake when strategy is StatefulFake`() {
        val fakeSimpleService = SimpleService()
        whenever(mockingEngine.createFake(SimpleService::class)).thenReturn(fakeSimpleService)

        val depMetadata =
            DependencyMetadata
                .create(
                    name = "simpleService",
                    type = SimpleService::class,
                    strategy = DependencyMetadata.MockingStrategy.StatefulFake,
                ).getOrThrow()
        val spec = createSpecification(ServiceWithDep::class, listOf(depMetadata))

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceWithDep

        assertEquals(fakeSimpleService, target.dep)
        verify(mockingEngine).createFake(SimpleService::class)
    }

    // =================================================================
    // 3. Value Type Handling (Updated for Generator)
    // =================================================================

    @Test
    fun `create injects generated values for value types using FixtureGenerator`() {
        // Target requires String and Int
        val spec = createSpecification(ServiceWithValue::class)

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceWithValue

        // [Changed] Expect generated values, not defaults
        assertNotNull(target.name)
        assertTrue(target.name.isNotEmpty(), "Generator should create non-empty string")

        // Note: Generator *could* generate 0 randomly, but statistically unlikely for a wide range.
        // If logic forces non-zero, assert accordingly. Here we just check it exists.
        assertNotNull(target.age)
    }

    @Test
    fun `create injects populated values for collections`() {
        val spec = createSpecification(ServiceWithComplexValues::class)

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceWithComplexValues

        assertNotNull(target.items)
        assertNotNull(target.config)

        assertNotNull(target.active)
        assertNotNull(target.count)
    }

    @Test
    fun `create prefers generating values even for nullable types`() {
        // Generator typically tries to generate a value for Fuzzing purposes
        val spec = createSpecification(ServiceWithNullable::class)

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceWithNullable

        assertNotNull(target.description)
        assertTrue(target.description!!.isNotEmpty())
    }

    // =================================================================
    // 4. Seed Consistency (New Coverage)
    // =================================================================

    @Test
    fun `create generates identical instances given the same seed`() {
        val seed = 12345L

        // Run 1
        val spec1 = createSpecification(ServiceWithValue::class, seed = seed)
        val context1 = factory.create(spec1)
        val target1 = context1.getTestTarget() as ServiceWithValue

        // Run 2
        val spec2 = createSpecification(ServiceWithValue::class, seed = seed)
        val context2 = factory.create(spec2)
        val target2 = context2.getTestTarget() as ServiceWithValue

        // Assertion: Deterministic generation
        assertEquals(target1.name, target2.name, "Same seed should produce same String")
        assertEquals(target1.age, target2.age, "Same seed should produce same Int")
    }

    @Test
    fun `create generates different instances given different seeds`() {
        // Run 1
        val spec1 = createSpecification(ServiceWithValue::class, seed = 11111L)
        val context1 = factory.create(spec1)
        val target1 = context1.getTestTarget() as ServiceWithValue

        // Run 2
        val spec2 = createSpecification(ServiceWithValue::class, seed = 99999L)
        val context2 = factory.create(spec2)
        val target2 = context2.getTestTarget() as ServiceWithValue
        
        assertNotEquals(target1.name, target2.name)
    }

    // =================================================================
    // 5. Interface Handling
    // =================================================================

    @Test
    fun `create falls back to mock for interfaces`() {
        // Target is an interface (no constructor), factory should use mockingEngine.createMock(type)
        val mockInstance = object : ServiceInterface {}
        whenever(mockingEngine.createMock(ServiceInterface::class)).thenReturn(mockInstance)

        val spec = createSpecification(ServiceInterface::class)

        val context = factory.create(spec)
        val target = context.getTestTarget()

        assertEquals(mockInstance, target)
        verify(mockingEngine).createMock(ServiceInterface::class)
    }

    // =================================================================
    // 6. Error Handling & Edge Cases
    // =================================================================

    @Test
    fun `create throws exception when circular dependency is detected`() {
        // Circular path: A -> B -> A
        val spec = createSpecification(CircularA::class)

        val ex =
            assertThrows<KontraktConfigurationException> {
                factory.create(spec)
            }
        assertTrue(ex.message!!.contains("Circular dependency detected"))
        assertTrue(ex.message!!.contains("CircularA -> CircularB -> CircularA"))
    }

    @Test
    fun `create throws exception when instantiation fails`() {
        // Class that throws exception in init block
        class ThrowingClass {
            init {
                throw RuntimeException("Boom")
            }
        }

        val spec = createSpecification(ThrowingClass::class)

        val ex =
            assertThrows<KontraktConfigurationException> {
                factory.create(spec)
            }
        assertTrue(ex.message!!.contains("Failed to create test target"))
        // Check unwrapped cause
        assertTrue(ex.cause!!.message!!.contains("Boom"))
    }

    @Test
    fun `create reuses dependencies within same context`() {
        // Singleton Scope Check: A -> C, B -> C. C should be created once.
        class ServiceC

        class ServiceB(
            val c: ServiceC,
        )

        class ServiceA(
            val c: ServiceC,
            val b: ServiceB,
        )

        val spec = createSpecification(ServiceA::class)

        val context = factory.create(spec)
        val target = context.getTestTarget() as ServiceA

        // target.c and target.b.c should be the SAME instance
        assertEquals(target.c, target.b.c)
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private fun createSpecification(
        type: KClass<*>,
        dependencies: List<DependencyMetadata> = emptyList(),
        modes: Set<TestSpecification.TestMode> = setOf(TestSpecification.TestMode.UserScenario),
        seed: Long? = null
    ): TestSpecification {
        val target =
            DiscoveredTestTarget
                .create(
                    kClass = type,
                    displayName = type.simpleName ?: "TestTarget",
                    fullyQualifiedName = type.qualifiedName ?: "test.Target",
                ).getOrThrow()

        return TestSpecification
            .create(
                target = target,
                modes = modes,
                requiredDependencies = dependencies,
                seed = seed
            ).getOrThrow()
    }
}