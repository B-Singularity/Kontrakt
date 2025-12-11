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
        val strategy = DependencyMetadata.MockingStrategy.Real

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
        val strategy = DependencyMetadata.MockingStrategy.Real

        val result = DependencyMetadata.create(validName, type, strategy)

        assertTrue(result.isSuccess, "Should succeed for valid name")
        val metadata = result.getOrThrow()
        assertEquals(validName, metadata.name)
        assertEquals(type, metadata.type)
        assertEquals(strategy, metadata.strategy)
    }

    @Test
    fun `create should correctly store all types of mocking strategies`() {

        val strategies = listOf(
            DependencyMetadata.MockingStrategy.Real,
            DependencyMetadata.MockingStrategy.StatelessMock,
            DependencyMetadata.MockingStrategy.StatefulFake,
            DependencyMetadata.MockingStrategy.Environment(DependencyMetadata.EnvType.TIME),
            DependencyMetadata.MockingStrategy.Environment(DependencyMetadata.EnvType.SECURITY)
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