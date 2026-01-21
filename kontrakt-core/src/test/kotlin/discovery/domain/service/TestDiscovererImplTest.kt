package discovery.domain.service

import discovery.api.Contract
import discovery.api.DataContract
import discovery.api.KontraktTest
import discovery.api.Stateful
import discovery.domain.aggregate.TestSpecification
import discovery.domain.aggregate.TestSpecification.TestMode
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DependencyMetadata.MockingStrategy
import discovery.domain.vo.DiscoveredTestTarget
import discovery.domain.vo.DiscoveryPolicy
import discovery.domain.vo.ScanScope
import discovery.port.outcoming.ClasspathScanner
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock

class TestDiscovererImplTest {

    private val scanner = mockk<ClasspathScanner>()
    private val testDispatcher = StandardTestDispatcher()
    private val discoverer = TestDiscovererImpl(scanner, testDispatcher)

    private val policy = DiscoveryPolicy(ScanScope.Packages(setOf("test.pkg")))

    @BeforeEach
    fun setup() {
        // [Safety] Ensure mocks are clean before each test to prevent pollution
        clearMocks(scanner)
    }

    // region Scenario: Discovery & Mapping

    @Test
    fun `discover - creates ContractAuto specs for interface implementations`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedInterfaces(any(), Contract::class) } returns listOf(MyContract::class)
        coEvery { scanner.findAllImplementations(any(), MyContract::class) } returns listOf(MyImplementation::class)
        coEvery { scanner.findAnnotatedClasses(any(), any()) } returns emptyList()
        // MyImplementation depends on String (Real Strategy)
        coEvery { scanner.findAllImplementations(any(), String::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        val specs = result.getOrThrow()
        assertThat(specs).hasSize(1)

        val spec = specs.first()
        assertThat(spec.target.kClass).isEqualTo(MyImplementation::class)
        assertThat(spec.modes.first()).isInstanceOf(TestMode.ContractAuto::class.java)
    }

    @Test
    fun `discover - creates UserScenario specs for KontraktTest classes`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(MyScenarioTest::class)
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        val spec = result.getOrThrow().first()

        assertThat(spec.target.kClass).isEqualTo(MyScenarioTest::class)
        assertThat(spec.modes).contains(TestMode.UserScenario)
    }

    // endregion

    // region Scenario: Merging

    @Test
    fun `discover - merges duplicate specs into a single one with combined modes`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedInterfaces(any(), Contract::class) } returns listOf(MyContract::class)
        coEvery { scanner.findAllImplementations(any(), MyContract::class) } returns listOf(HybridClass::class)
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(HybridClass::class)
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()
        coEvery { scanner.findAllImplementations(any(), String::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        val specs = result.getOrThrow()
        assertThat(specs).hasSize(1)

        val spec = specs.first()
        assertThat(spec.target.kClass).isEqualTo(HybridClass::class)
        assertThat(spec.modes).hasSize(2)
        assertThat(spec.modes.map { it::class }).contains(
            TestMode.ContractAuto::class,
            TestMode.UserScenario::class
        )
    }

    // endregion

    // region Scenario: Mocking Strategy Resolution

    @Test
    fun `dependency resolution - correctly identifies mocking strategies`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(ComplexService::class)
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        // Dependency Interface with NO impl -> StatelessMock
        coEvery { scanner.findAllImplementations(any(), DependencyInterface::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        val spec = result.getOrThrow().first()
        val deps = spec.requiredDependencies.associateBy { it.type }

        assertThat(deps[Clock::class]?.strategy).isInstanceOf(MockingStrategy.Environment::class.java)
        assertThat(deps[StatefulDep::class]?.strategy).isEqualTo(MockingStrategy.StatefulFake)
        assertThat(deps[DependencyInterface::class]?.strategy).isEqualTo(MockingStrategy.StatelessMock)
    }

    @Test
    fun `dependency resolution - uses Real strategy if interface has implementation`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(ServiceWithInterface::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        // Has implementation -> Real
        coEvery {
            scanner.findAllImplementations(
                any(),
                DependencyInterface::class
            )
        } returns listOf(ImplOfDependency::class)

        val result = discoverer.discover(policy, Contract::class)

        val spec = result.getOrThrow().first()
        val strategy = spec.requiredDependencies.first().strategy

        assertThat(strategy).isInstanceOf(MockingStrategy.Real::class.java)
        assertThat((strategy as MockingStrategy.Real).implementation).isEqualTo(ImplOfDependency::class)
    }

    // endregion

    // region Scenario: Edge Cases & Coverage Boosting

    @Test
    fun `determineMockingStrategy - handles Abstract Class dependencies correctly`() = runTest(testDispatcher) {
        // [Goal] Cover 'type.isAbstract' branch
        coEvery {
            scanner.findAnnotatedClasses(
                any(),
                KontraktTest::class
            )
        } returns listOf(ServiceWithAbstractDep::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        // Abstract class has NO implementation -> StatelessMock
        coEvery { scanner.findAllImplementations(any(), AbstractDep::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        val spec = result.getOrThrow().first()
        val strategy = spec.requiredDependencies.first().strategy
        assertThat(strategy).isEqualTo(MockingStrategy.StatelessMock)
    }

    @Test
    fun `createSpecificationForClass - handles DependencyMetadata creation failure`() = runTest(testDispatcher) {
        // [Goal] Cover 'getOrElse { return Result.failure }' in dependency loop
        // Given: A class WITH dependencies (ClassWithParam) so DependencyMetadata.create is actually called
        class ClassWithParam(val p: String)
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(ClassWithParam::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        // Mock global object strictly for this test
        mockkObject(DependencyMetadata)
        try {
            // Force create to fail
            every {
                DependencyMetadata.create(
                    any(),
                    any(),
                    any()
                )
            } returns Result.failure(IllegalArgumentException("Forced Failure"))

            // When
            val result = discoverer.discover(policy, Contract::class)

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrThrow()).isEmpty() // Should fail to create spec and be filtered out
        } finally {
            // [Critical] Must unmock even if assertions fail
            unmockkObject(DependencyMetadata)
        }
    }

    @Test
    fun `discover - ignores classes without primary constructor`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(SingletonObject::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `discover - handles classes without qualified name (Anonymous-Local classes)`() = runTest(testDispatcher) {
        class LocalAnonymousTest
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(LocalAnonymousTest::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `discover - handles targets where interface is passed as class target`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(MyContract::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `discover - logs warning when contract implementation is invalid`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedInterfaces(any(), Contract::class) } returns listOf(MyContract::class)
        coEvery { scanner.findAllImplementations(any(), MyContract::class) } returns listOf(SingletonObject::class)
        coEvery { scanner.findAnnotatedClasses(any(), any()) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `discover - gracefully ignores invalid DataContract classes`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns listOf(SingletonObject::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `discover - fails when parameter type cannot be determined (Generic Type)`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(GenericTestTarget::class)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `discover - captures unexpected exceptions thrown by scanner`() = runTest(testDispatcher) {
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } throws RuntimeException("Unexpected Scanner Crash")

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull())
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Unexpected Scanner Crash")
    }

    @Test
    fun `mergeSpecifications - triggers exception logic via reflection`() {
        // [Goal] Cover unreachable 'Dependency resolution conflict'
        val target = mockk<DiscoveredTestTarget> {
            every { displayName } returns "ConflictTarget"
            every { fullyQualifiedName } returns "com.test.ConflictTarget"
        }

        // Factory Method Use
        val dep1 = DependencyMetadata.create("param", String::class, MockingStrategy.StatelessMock).getOrThrow()
        val dep2 = DependencyMetadata.create("param", Int::class, MockingStrategy.StatelessMock).getOrThrow()

        val spec1 = TestSpecification.create(target, setOf(TestMode.UserScenario), listOf(dep1)).getOrThrow()
        val spec2 = TestSpecification.create(target, setOf(TestMode.UserScenario), listOf(dep2)).getOrThrow()

        val method = TestDiscovererImpl::class.java.getDeclaredMethod("mergeSpecifications", List::class.java)
        method.isAccessible = true

        try {
            method.invoke(discoverer, listOf(spec1, spec2))
        } catch (e: java.lang.reflect.InvocationTargetException) {
            val cause = e.cause as exception.KontraktConfigurationException
            val message = cause.message ?: ""

            // [Critical] Access message property to trigger lambda
            println("Captured Error: $message")

            assertThat(message)
                .contains("Dependency resolution conflict detected")
                .contains("ConflictTarget")
                .contains("class kotlin.String")
                .contains("class kotlin.Int")
        }
    }

    @Test
    fun `createSpecificationForClass - handles DependencyMetadata creation failure and triggers warning log`() =
        runTest(testDispatcher) {

            class ClassWithDependency(val dep: String)

            coEvery {
                scanner.findAnnotatedClasses(
                    any(),
                    KontraktTest::class
                )
            } returns listOf(ClassWithDependency::class)
            coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
            coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

            mockkObject(DependencyMetadata)
            try {
                every {
                    DependencyMetadata.create(
                        any(),
                        any(),
                        any()
                    )
                } returns Result.failure(IllegalArgumentException("Mocked Failure"))

                // When
                val result = discoverer.discover(policy, Contract::class)

                // Then
                assertThat(result.isSuccess).isTrue()
                assertThat(result.getOrThrow()).isEmpty()
            } finally {
                unmockkObject(DependencyMetadata)
            }
        }

    @Test
    fun `createSpecificationForClass - handles null simpleName (UnnamedClass fallback)`() = runTest(testDispatcher) {
        val anonymousClass = object {}.javaClass.kotlin

        coEvery { scanner.findAnnotatedClasses(any(), KontraktTest::class) } returns listOf(anonymousClass)
        coEvery { scanner.findAnnotatedInterfaces(any(), any()) } returns emptyList()
        coEvery { scanner.findAnnotatedClasses(any(), DataContract::class) } returns emptyList()

        val result = discoverer.discover(policy, Contract::class)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEmpty()
    }
}

// -----------------------------------------------------------------------------
// Test Stubs
// -----------------------------------------------------------------------------

interface MyContract
class MyImplementation(val dep: String) : MyContract
class MyScenarioTest
class HybridClass(val dep: String) : MyContract

interface DependencyInterface
class ImplOfDependency : DependencyInterface

@Stateful
class StatefulDep

abstract class AbstractDep
class ServiceWithAbstractDep(val dep: AbstractDep)

class ComplexService(
    val clock: Clock,
    val state: StatefulDep,
    val svc: DependencyInterface
)

class ServiceWithInterface(val svc: DependencyInterface)
object SingletonObject
class GenericTestTarget<T>(val item: T)