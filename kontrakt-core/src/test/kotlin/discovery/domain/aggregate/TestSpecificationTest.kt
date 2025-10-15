package discovery.domain.aggregate

import discovery.domain.vo.DependencyMetadata
import discovery.domain.vo.DiscoveredTestTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestSpecificationTest {
    private val dummyTarget: DiscoveredTestTarget =
        DiscoveredTestTarget
            .create(
                kClass = String::class,
                displayName = "String",
                fullyQualifiedName = "kotlin.String",
            ).getOrThrow()

    private val dummyDependency: DependencyMetadata =
        DependencyMetadata
            .create(
                name = "engine",
                type = List::class,
            ).getOrThrow()

    @Test
    fun `should create successfully with valid arguments`() {
        val result =
            TestSpecification.create(
                target = dummyTarget,
                requiredDependencies = listOf(dummyDependency),
            )

        val spec = result.getOrNull()
        assertNotNull(spec, "TestSpecification creation should have been successful.")

        assertEquals(dummyTarget, spec.target)
        assertEquals(1, spec.requiredDependencies.size)
    }
}
