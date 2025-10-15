package discovery.domain.service

import discovery.api.Contract
import discovery.api.TestDiscoverer
import discovery.spi.ClasspathScanner
import kotlinx.coroutines.test.runTest
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@Contract
private interface TestContract

private interface TestDependency

private class ValidImplementation(val dependency: TestDependency) : TestContract

private class ImplementationWithNoPrimaryCtor : TestContract {
    constructor(dependency: TestDependency)
}

private class FakeClasspathScanner(
    private val implementationsToReturn: List<KClass<*>>
) : ClasspathScanner {
    override suspend fun findAnnotatedInterfaces(
        basePackage: String,
        annotation: KClass<out Annotation>
    ): List<KClass<*>> = listOf(TestContract::class)

    override suspend fun findAllImplementations(
        basePackage: String,
        targetInterface: KClass<*>
    ): List<KClass<*>> = implementationsToReturn
}


class TestDiscovererImplTest {

    @Test
    fun `should create specification for a valid class`() = runTest {
        val fakeScanner = FakeClasspathScanner(implementationsToReturn = listOf(ValidImplementation::class))
        val discoverer: TestDiscoverer = TestDiscovererImpl(fakeScanner)

        val result = discoverer.discover("com.some.user.package", Contract::class)

        assertTrue(result.isSuccess, "Result should be Success")
        val specifications = result.getOrThrow()

        assertEquals(1, specifications.size)
        val spec = specifications.first()
        assertEquals("ValidImplementation", spec.target.displayName)
        assertEquals(1, spec.requiredDependencies.size)
        assertEquals("dependency", spec.requiredDependencies.first().name)
        assertEquals(TestDependency::class, spec.requiredDependencies.first().type)

    }

    @Test
    fun `shoulld skip class with no primary constructor`() = runTest {
        val fakeScanner = FakeClasspathScanner(implementationsToReturn = listOf(ImplementationWithNoPrimaryCtor::class))
        val discoverer: TestDiscoverer = TestDiscovererImpl(fakeScanner)

        val result = discoverer.discover("com.some.user.package", Contract::class)

        assertTrue(result.isSuccess, "Result should be Success")
        val specifications = result.getOrThrow()
        assertTrue(specifications.isEmpty(), "List should be empty as the only candidate class is invalid.")
    }

    @Test
    fun `should return empty list when scanner finds nothing`() = runTest {
        val fakeScanner = FakeClasspathScanner(implementationsToReturn = emptyList())
        val discoverer: TestDiscoverer = TestDiscovererImpl(fakeScanner)
        val result = discoverer.discover("com.some.user.package", Contract::class)
        assertTrue(result.isSuccess, "Result should be Success")
        val specifications = result.getOrThrow()
        assertTrue(specifications.isEmpty(), "List should be empty as the only candidate class is invalid.")
    }
}