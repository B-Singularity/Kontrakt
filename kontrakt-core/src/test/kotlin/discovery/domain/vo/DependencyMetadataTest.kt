package discovery.domain.vo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DependencyMetadataTest {

    @Test
    fun `should create a valid instance with correct properties`() {
        val result = DependencyMetadata.create(
            name = "String",
            type = String::class,
        )

        assertTrue(result.isSuccess, "Result should be Successful")
        val target = result.getOrThrow()
        assertEquals("String", target.name)
        assertEquals(String::class, target.type)
    }

    @Test
    fun `should fail to create if name is blank`() {
        val  result = DependencyMetadata.create(
            name = " ",
            type = String::class
        )
        assertTrue(result.isFailure, "Result should be a failure")
        val exception = assertFailsWith<IllegalArgumentException> {
            result.getOrThrow()
        }

        assertEquals("Name cannot be blank", exception.message)
    }
}