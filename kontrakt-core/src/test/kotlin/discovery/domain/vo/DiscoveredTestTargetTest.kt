package discovery.domain.vo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DiscoveredTestTargetTest {
    @Test
    fun `should create a valid instance with correct properties`() {
        val result =
            DiscoveredTestTarget.create(
                kClass = String::class,
                displayName = "String",
                fullyQualifiedName = "kotlin.String",
            )

        assertTrue(result.isSuccess, "Creation should be successful")

        val target = result.getOrThrow()
        assertEquals(String::class, target.kClass)
        assertEquals("String", target.displayName)
        assertEquals("kotlin.String", target.fullyQualifiedName)
    }

    @Test
    fun `should fail to create if displayName is blank`() {
        val result =
            DiscoveredTestTarget.create(
                kClass = String::class,
                displayName = "  ",
                fullyQualifiedName = "kotlin.String",
            )

        assertTrue(result.isFailure, "Creation should be failure")
        val exception =
            assertFailsWith<IllegalArgumentException> {
                result.getOrThrow()
            }

        assertEquals("Display name cannot be blank", exception.message)
    }

    @Test
    fun `should fail to create if fullyQualifiedName is blank`() {
        val result =
            DiscoveredTestTarget.create(
                kClass = String::class,
                displayName = "String",
                fullyQualifiedName = " ",
            )

        assertTrue(result.isFailure, "Creation should be failure")
        val exception =
            assertFailsWith<IllegalArgumentException> {
                result.getOrThrow()
            }

        assertEquals("Fully qualified name cannot be blank", exception.message)
    }
}
