package discovery.api

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.ScanScope
import kotlinx.coroutines.test.runTest
import java.time.Clock
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

abstract class TestDiscovererTest {
    protected abstract val discoverer: TestDiscoverer

    protected abstract fun setupScanResult(
        interfaces: List<KClass<*>> = emptyList(),
        classes: List<KClass<*>> = emptyList(),
        implementations: Map<KClass<*>, List<KClass<*>>> = emptyMap(),
    )

    @Contract
    interface TargetContract

    class StandardImpl : TargetContract

    @KontraktTest
    class ManualTestClass

    @KontraktTest
    class HybridService : TargetContract

    @KontraktTest
    object InvalidObjectTest

    @Stateful
    interface MyStore

    class ComplexService(
        val store: MyStore,
        val clock: Clock,
        val helper: String,
    ) : TargetContract

    @Test
    fun `Implicit Mode - should create ContractAuto spec`() =
        runTest {
            setupScanResult(
                interfaces = listOf(TargetContract::class),
                implementations = mapOf(TargetContract::class to listOf(StandardImpl::class)),
            )

            val result = discoverer.discover(ScanScope.All, Contract::class).getOrThrow()

            assertEquals(1, result.size)
            val spec = result.first()
            assertEquals(StandardImpl::class, spec.target.kClass)
            assertIs<TestSpecification.TestMode.ContractAuto>(spec.modes.first())
        }

    @Test
    fun `Explicit Mode - should create UserScenario spec`() =
        runTest {
            setupScanResult(
                classes = listOf(ManualTestClass::class),
            )

            val result = discoverer.discover(ScanScope.All, Contract::class).getOrThrow()

            assertEquals(1, result.size)
            val spec = result.first()
            assertEquals(ManualTestClass::class, spec.target.kClass)
            assertIs<TestSpecification.TestMode.UserScenario>(spec.modes.first())
        }

    @Test
    fun `Merge Logic - should merge modes for hybrid class`() =
        runTest {
            setupScanResult(
                interfaces = listOf(TargetContract::class),
                classes = listOf(HybridService::class),
                implementations = mapOf(TargetContract::class to listOf(HybridService::class)),
            )

            val result = discoverer.discover(ScanScope.All, Contract::class).getOrThrow()

            assertEquals(1, result.size, "Should be merged into one spec")
            val spec = result.first()
            assertEquals(HybridService::class, spec.target.kClass)
            assertEquals(2, spec.modes.size)
            assertTrue(spec.modes.any { it is TestSpecification.TestMode.ContractAuto })
            assertTrue(spec.modes.any { it is TestSpecification.TestMode.UserScenario })
        }

    @Test
    fun `Dependency Analysis - should determine correct strategies`() =
        runTest {
            setupScanResult(
                interfaces = listOf(TargetContract::class),
                implementations = mapOf(TargetContract::class to listOf(ComplexService::class)),
            )

            val result = discoverer.discover(ScanScope.All, Contract::class).getOrThrow()

            val spec = result.first()
            val deps = spec.requiredDependencies

            val storeDep = deps.find { it.type == MyStore::class }!!
            assertIs<DependencyMetadata.MockingStrategy.StatefulFake>(storeDep.strategy)

            val clockDep = deps.find { it.type == Clock::class }!!
            assertIs<DependencyMetadata.MockingStrategy.Environment>(clockDep.strategy)

            val helperDep = deps.find { it.type == String::class }!!
            assertIs<DependencyMetadata.MockingStrategy.Real>(helperDep.strategy)
        }
}
