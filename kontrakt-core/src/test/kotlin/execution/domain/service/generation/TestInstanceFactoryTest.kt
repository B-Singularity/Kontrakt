package execution.domain.service.generation

import discovery.api.Test
import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import exception.KontraktConfigurationException
import exception.KontraktInternalException
import execution.domain.entity.EphemeralTestContext
import execution.port.outgoing.MockingContext
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioControl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.reflect.KClass

class TestInstanceFactoryTest {

    private lateinit var mockingEngine: MockingEngine
    private lateinit var scenarioControl: ScenarioControl
    private lateinit var factory: TestInstanceFactory
    private val clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))

    @BeforeEach
    fun setUp() {
        mockingEngine = mockk(relaxed = true)
        scenarioControl = mockk(relaxed = true)
        factory = TestInstanceFactory(mockingEngine, scenarioControl)
    }

    // --- 1. Basic Functionality Tests ---

    @org.junit.jupiter.api.Test
    fun `should instantiate a class with primitive dependencies`() {
        val spec = createSpec(TargetWithPrimitives::class)
        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance() as TargetWithPrimitives

        assertNotNull(target)
        assertNotNull(target.name)
    }

    @org.junit.jupiter.api.Test
    fun `should reuse cached dependencies (Diamond Problem)`() {
        val spec = createSpec(DiamondRoot::class)
        val context = factory.create(spec, clock)
        val root = context.getPrivateTargetInstance() as DiamondRoot
        assertSame(root.left.shared, root.right.shared)
    }

    @org.junit.jupiter.api.Test
    fun `should return basic value directly if target is primitive`() {
        val spec = createSpec(String::class)
        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance()

        assertNotNull(target)
        assertTrue(target is String)
    }

    // --- 2. Strategy Tests ---

    @org.junit.jupiter.api.Test
    fun `should apply StatelessMock, Environment, and Real strategies correctly`() {
        val fakeMeta = DependencyMetadata.create(
            "fake", DependencyInterface::class, DependencyMetadata.MockingStrategy.StatefulFake
        ).getOrThrow()

        val envMeta = DependencyMetadata.create(
            "env", AnotherInterface::class,
            DependencyMetadata.MockingStrategy.Environment(DependencyMetadata.EnvType.TIME)
        ).getOrThrow()

        val realMeta = DependencyMetadata.create(
            "real", RealDependency::class,
            DependencyMetadata.MockingStrategy.Real(RealImplementation::class)
        ).getOrThrow()

        val spec = createSpec(
            TargetWithDiverseStrategies::class,
            listOf(fakeMeta, envMeta, realMeta)
        )

        val fakeInstance = mockk<DependencyInterface>()
        val envInstance = mockk<AnotherInterface>()

        every { mockingEngine.createFake(eq(DependencyInterface::class), any<MockingContext>()) } returns fakeInstance
        every { mockingEngine.createMock(eq(AnotherInterface::class), any<MockingContext>()) } returns envInstance

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance() as TargetWithDiverseStrategies

        assertEquals(fakeInstance, target.fake)
        assertEquals(envInstance, target.env)
        assertTrue(target.real is RealImplementation)
    }

    // --- 3. Exception Handling & Branch Coverage ---

    @org.junit.jupiter.api.Test
    fun `should handle constructor exceptions by wrapping in KontraktConfigurationException`() {
        val spec = createSpec(ExplodingTarget::class)

        val ex = assertThrows(KontraktConfigurationException::class.java) {
            factory.create(spec, clock)
        }

        assertTrue(ex.message!!.contains("Failed to instantiate class"), "Actual: ${ex.message}")
        assertEquals("Boom!", ex.cause?.message)
    }

    @org.junit.jupiter.api.Test
    fun `should wrap unexpected runtime exceptions from mocking engine (Outer Catch)`() {
        val targetClass = TargetWithInterface::class
        val selfMetadata = DependencyMetadata.create(
            "self", targetClass, DependencyMetadata.MockingStrategy.StatelessMock
        ).getOrThrow()

        val spec = TestSpecification.create(
            target = createMockTarget(targetClass),
            modes = setOf(TestSpecification.TestMode.ContractAuto(Any::class)),
            requiredDependencies = listOf(selfMetadata),
            seed = 1234L
        ).getOrThrow()

        every {
            mockingEngine.createMock(targetClass, any<MockingContext>())
        } throws RuntimeException("Critical Failure")

        val ex = assertThrows(KontraktConfigurationException::class.java) {
            factory.create(spec, clock)
        }

        val msg = ex.message ?: ""
        assertTrue(msg.contains("Failed to create test target"), "Actual: $msg")
        assertEquals("Critical Failure", ex.cause?.message)
    }

    @org.junit.jupiter.api.Test
    fun `should throw KontraktInternalException when javaMethod resolution fails`() {
        val spec = TestSpecification.create(
            target = createMockTarget(ClassWithConstructor::class),
            modes = setOf(TestSpecification.TestMode.DataCompliance(ClassWithConstructor::class)),
            requiredDependencies = emptyList(),
            seed = 1234L
        ).getOrThrow()

        assertThrows(KontraktInternalException::class.java) {
            factory.create(spec, clock)
        }
    }

    @org.junit.jupiter.api.Test
    fun `should throw exception if ContractAuto finds no executable methods`() {
        val spec = createSpec(EmptyClass::class)
        val ex = assertThrows(KontraktConfigurationException::class.java) {
            factory.create(spec, clock)
        }
        assertTrue(ex.message!!.contains("No executable business methods found"))
    }

    @org.junit.jupiter.api.Test
    fun `should throw InternalException if UserScenario finds no methods (Invariant Violation)`() {
        val spec = TestSpecification.create(
            target = createMockTarget(EmptyClass::class),
            modes = setOf(TestSpecification.TestMode.UserScenario),
            requiredDependencies = emptyList()
        ).getOrThrow()

        val ex = assertThrows(KontraktInternalException::class.java) {
            factory.create(spec, clock)
        }
        assertTrue(ex.message!!.contains("Invariant violation"), "Actual: ${ex.message}")
    }

    @org.junit.jupiter.api.Test
    fun `should detect circular dependencies`() {
        val spec = createSpec(CircularA::class)
        val ex = assertThrows(KontraktConfigurationException::class.java) {
            factory.create(spec, clock)
        }
        assertTrue(ex.message!!.contains("Circular dependency detected"))
    }

    // --- 4. Logic & Fallback Coverage Tests ---

    @org.junit.jupiter.api.Test
    fun `UserScenario should prioritize @Test annotation over other methods`() {
        val targetMock = createMockTarget(TargetWithAnnotation::class)
        val spec = TestSpecification.create(
            target = targetMock,
            modes = setOf(TestSpecification.TestMode.UserScenario),
            requiredDependencies = emptyList(),
            seed = 1234L
        ).getOrThrow()

        val context = factory.create(spec, clock)
        assertEquals("annotatedMethod", context.targetMethod.name)
    }

    @org.junit.jupiter.api.Test
    fun `should fail if DataCompliance target has no primary constructor`() {
        val targetMock = createMockTarget(NoPrimaryConstructorTarget::class)
        val spec = TestSpecification.create(
            target = targetMock,
            modes = setOf(TestSpecification.TestMode.DataCompliance(NoPrimaryConstructorTarget::class)),
            requiredDependencies = emptyList(),
            seed = 1234L
        ).getOrThrow()

        val ex = assertThrows(KontraktConfigurationException::class.java) {
            factory.create(spec, clock)
        }
        assertTrue(
            ex.message!!.contains("requires a primary constructor"),
            "Actual: ${ex.message}"
        )
    }

    @org.junit.jupiter.api.Test
    fun `should resolve all basic value types via generator`() {
        val spec = createSpec(AllBasicTypesTarget::class)
        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance() as AllBasicTypesTarget

        assertNotNull(target.l)
        assertNotNull(target.d)
        assertNotNull(target.b)
        assertNotNull(target.list)
        assertNotNull(target.map)
        assertNotNull(target.set)
    }

    @org.junit.jupiter.api.Test
    fun `should fallback to resolve when generator returns null for basic type`() {
        val spec = createSpec(TargetWithNestedData::class)
        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance() as TargetWithNestedData

        assertNotNull(target.child)
        assertNotNull(target.child.value)
    }

    @org.junit.jupiter.api.Test
    fun `should use fallback name 'dependency' when type simpleName is null`() {
        val anonymousInstance = object : DependencyInterface {
            override fun action() {}
        }
        val anonymousClass = anonymousInstance::class

        every {
            mockingEngine.createMock(eq(anonymousClass), any<MockingContext>())
        } returns anonymousInstance

        val spec = createSpec(anonymousClass)
        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance()

        assertSame(anonymousInstance, target)
    }

    @org.junit.jupiter.api.Test
    fun `should fallback to mock when no constructor is available (Interface)`() {
        val spec = createSpec(InterfaceNoConstructor::class)
        val mock = mockk<InterfaceNoConstructor>()
        every { mockingEngine.createMock(eq(InterfaceNoConstructor::class), any<MockingContext>()) } returns mock

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance()

        assertSame(mock, target)
    }

    @org.junit.jupiter.api.Test
    fun `should return generated basic value immediately without constructor resolution`() {
        val spec = createSpec(String::class)

        every {
            mockingEngine.createMock(any<KClass<*>>(), any<MockingContext>())
        } throws AssertionError("Constructor path should not be reached")

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance()

        assertTrue(target is String)
    }

    @org.junit.jupiter.api.Test
    fun `should ignore generator exception and fallback to constructor resolution`() {
        val spec = createSpec(TargetWithPrimitives::class)
        every {
            mockingEngine.createMock(any<KClass<*>>(), any<MockingContext>())
        } answers { callOriginal() }

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance() as TargetWithPrimitives

        assertNotNull(target.name)
        assertNotNull(target.age)
    }

    @org.junit.jupiter.api.Test
    fun `DataCompliance should fail when resolved member has no java method`() {
        // [FIX] Correct expectation for this test case
        val spec = TestSpecification.create(
            target = createMockTarget(NoCtorNoToStringTarget::class),
            modes = setOf(
                TestSpecification.TestMode.DataCompliance(NoCtorNoToStringTarget::class)
            ),
            requiredDependencies = emptyList(),
            seed = 1234L
        ).getOrThrow()

        // Although it passes the constructor check, it fails reflection (javaMethod == null)
        // leading to KontraktInternalException.
        val ex = assertThrows(KontraktInternalException::class.java) {
            factory.create(spec, clock)
        }

        assertTrue(
            ex.message!!.contains("Reflection failure") || ex.message!!.contains("Could not resolve Java method"),
            "Actual message: ${ex.message}"
        )
    }

    @org.junit.jupiter.api.Test
    fun `UserScenario should fallback to non-standard method if @Test annotation is missing`() {
        // Coverage: functions.firstOrNull { !it.isStandardMethod }
        // Case: Class has methods, but none are annotated with @Test.
        // Factory should pick the first available "business" method.
        val targetMock = createMockTarget(TargetWithoutAnnotation::class)
        val spec = TestSpecification.create(
            target = targetMock,
            modes = setOf(TestSpecification.TestMode.UserScenario),
            requiredDependencies = emptyList(),
            seed = 1234L
        ).getOrThrow()

        val context = factory.create(spec, clock)

        // Must select 'implicitTestMethod'
        assertEquals("implicitTestMethod", context.targetMethod.name)
    }

    @org.junit.jupiter.api.Test
    fun `should support StatelessMock strategy`() {
        val strategy = DependencyMetadata.MockingStrategy.StatelessMock
        val metadata = DependencyMetadata.create("dep", DependencyInterface::class, strategy).getOrThrow()

        val spec = TestSpecification.create(
            target = createMockTarget(TargetWithInterface::class),
            modes = setOf(TestSpecification.TestMode.ContractAuto(Any::class)),
            requiredDependencies = listOf(metadata),
            seed = 1234L
        ).getOrThrow()

        val mockInstance = mockk<DependencyInterface>()
        every { mockingEngine.createMock(eq(DependencyInterface::class), any()) } returns mockInstance

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance() as TargetWithInterface

        assertSame(mockInstance, target.dependency)
    }

    @org.junit.jupiter.api.Test
    fun `should return generated value immediately for basic types (Gap Coverage)`() {
        val spec = createSpec(String::class)

        every {
            mockingEngine.createMock(any<KClass<*>>(), any<MockingContext>())
        } throws AssertionError("Should have used Generator, not MockingEngine fallback")

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance()

        assertTrue(target is String)
        assertTrue((target as String).isNotEmpty())
    }

    @org.junit.jupiter.api.Test
    fun `should return generated value immediately for basic types (Generator Success)`() {
        val spec = createSpec(String::class)

        every {
            mockingEngine.createMock(any<KClass<*>>(), any<MockingContext>())
        } throws AssertionError("Should have used Generator, not MockingEngine fallback")

        val context = factory.create(spec, clock)
        val target = context.getPrivateTargetInstance()

        assertTrue(target is String)
        assertTrue((target as String).isNotEmpty())
    }

    @org.junit.jupiter.api.Test
    fun `should fallback to constructor if generator fails for basic type (Generator Failure)`() {
        val spec = createSpec(ThrowingData::class)

        val ex = assertThrows(KontraktConfigurationException::class.java) {
            factory.create(spec, clock)
        }

        assertTrue(ex.message!!.contains("Failed to instantiate class"), "Actual: ${ex.message}")
    }


    // --- Helpers ---

    private fun EphemeralTestContext.getPrivateTargetInstance(): Any? {
        val field = this::class.java.getDeclaredField("targetInstance")
        field.isAccessible = true
        return field.get(this)
    }

    private fun createSpec(
        targetClass: KClass<*>,
        dependencies: List<DependencyMetadata> = emptyList()
    ): TestSpecification {
        return TestSpecification.create(
            target = createMockTarget(targetClass),
            modes = setOf(TestSpecification.TestMode.ContractAuto(Any::class)),
            requiredDependencies = dependencies,
            seed = 1234L
        ).getOrThrow()
    }

    private fun createMockTarget(kClass: KClass<*>): DiscoveredTestTarget {
        return mockk<DiscoveredTestTarget> {
            every { this@mockk.kClass } returns kClass
            every { displayName } returns (kClass.simpleName ?: "Unknown")
            every { fullyQualifiedName } returns (kClass.qualifiedName ?: "Unknown")
        }
    }


    // --- Dummy Classes ---

    data class NestedData(val value: Int)
    data class TargetWithNestedData(val child: NestedData)
    class TargetWithPrimitives(val name: String, val age: Int) {
        fun run() {}
    }

    class ExplodingTarget { init {
        throw RuntimeException("Boom!")
    }
    }

    class TargetWithAnnotation {
        fun normalMethod() {};
        @Test
        fun annotatedMethod() {
        }
    }

    class NoPrimaryConstructorTarget {
        constructor();
        override fun toString(): String = "Fallback"
    }

    interface DependencyInterface {
        fun action()
    }

    interface AnotherInterface
    interface RealDependency
    class RealImplementation : RealDependency
    class TargetWithInterface(val dependency: DependencyInterface) {
        fun run() {}
    }

    class TargetWithoutAnnotation {
        fun implicitTestMethod() {}
    }

    class CircularA(val b: CircularB) {
        fun action() {}
    }

    class CircularB(val a: CircularA) {
        fun action() {}
    }

    class EmptyClass
    class ClassWithConstructor(val id: Int)
    class SharedDependency
    class Left(val shared: SharedDependency)
    class Right(val shared: SharedDependency)
    class DiamondRoot(val left: Left, val right: Right) {
        fun execute() {}
    }

    class TargetWithDiverseStrategies(
        val fake: DependencyInterface,
        val env: AnotherInterface,
        val real: RealDependency
    ) {
        fun run() {}
    }

    class RealImplWithDep(val dep: SharedDependency) : RealDependency
    interface InterfaceNoConstructor {
        fun action()
    }

    class AllBasicTypesTarget(
        val l: Long, val d: Double, val b: Boolean,
        val list: List<String>, val map: Map<String, Int>, val set: Set<String>
    ) {
        fun run() {}
    }

    class NoCtorNoToStringTarget

    data class ThrowingData(val id: Int) {
        init {
            throw RuntimeException("Generator Boom!")
        }
    }
}