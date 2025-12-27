package discovery.domain.vo

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DiscoveredTestTargetTest {
    @Test
    fun `create should succeed when invalid arguments are provided`() {
        val kClass = String::class
        val displayName = "String"
        val fullyQualifiedName = "java.lang.String"
        val result = DiscoveredTestTarget.create(kClass, displayName, fullyQualifiedName)

        assertTrue(result.isSuccess, "Creation should succeed with valid inputs")
        val target = result.getOrThrow()

        assertEquals(kClass, target.kClass)
        assertEquals(displayName, target.displayName)
        assertEquals(fullyQualifiedName, target.fullyQualifiedName)
    }

    @Test
    fun `create should fail when displayName is blank`() {
        val kClass = String::class
        val invalidDisplayName = "   "
        val fullyQualifiedName = "java.lang.String"

        val result = DiscoveredTestTarget.create(kClass, invalidDisplayName, fullyQualifiedName)

        assertTrue(result.isFailure, "Should fail if displayName is blank")

        val exception = result.exceptionOrNull()
        assertIs<IllegalArgumentException>(exception)
        assertEquals("Display name cannot be blank", exception.message)
    }

    @Test
    fun `create should fail when fullyQualifiedName is blank`() {
        val kClass = String::class
        val displayName = "String"
        val invalidQualifiedName = ""

        val result = DiscoveredTestTarget.create(kClass, displayName, invalidQualifiedName)

        assertTrue(result.isFailure, "Should fail if fullyQualifiedName is blank")

        val exception = result.exceptionOrNull()
        assertIs<IllegalArgumentException>(exception)
        assertEquals("Fully qualified name cannot be blank", exception.message)
    }
}
