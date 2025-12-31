package discovery.domain.vo

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DependencyMetadataTest {
    @Test
    fun `create should fail when name is blank`() {
        val invalidNames = listOf("", "   ", "\t\n")
        val type = String::class
        // Updated: Real strategy requires an implementation class argument.
        val strategy = DependencyMetadata.MockingStrategy.Real(String::class)

        invalidNames.forEach { invalidName ->
            val result = DependencyMetadata.create(invalidName, type, strategy)

            assertTrue(result.isFailure, "Should fail for blank name: '$invalidName'")
            val exception = result.exceptionOrNull()
            assertIs<IllegalArgumentException>(exception)
            assertEquals("Name cannot be blank", exception.message)
        }
    }

    @Test
    fun `create should succeed when name is valid`() {
        val validName = "userRepository"
        val type = String::class
        // Updated: Pass the implementation class (using String::class for dummy test)
        val strategy = DependencyMetadata.MockingStrategy.Real(String::class)

        val result = DependencyMetadata.create(validName, type, strategy)

        assertTrue(result.isSuccess, "Should succeed for valid name")
        val metadata = result.getOrThrow()
        assertEquals(validName, metadata.name)
        assertEquals(type, metadata.type)
        assertEquals(strategy, metadata.strategy)
    }

    @Test
    fun `create should correctly store all types of mocking strategies`() {
        val strategies =
            listOf(
                // Updated: Instantiating Real strategy with a dummy implementation class
                DependencyMetadata.MockingStrategy.Real(Any::class),
                DependencyMetadata.MockingStrategy.StatelessMock,
                DependencyMetadata.MockingStrategy.StatefulFake,
                DependencyMetadata.MockingStrategy.Environment(DependencyMetadata.EnvType.TIME),
                DependencyMetadata.MockingStrategy.Environment(DependencyMetadata.EnvType.SECURITY),
            )

        val name = "dependency"
        val type = Any::class

        strategies.forEachIndexed { index, strategy ->
            val result = DependencyMetadata.create(name, type, strategy)

            assertTrue(result.isSuccess, "Scenario #$index failed")
            assertEquals(strategy, result.getOrThrow().strategy, "Strategy mismatch for $strategy")
        }
    }
}
