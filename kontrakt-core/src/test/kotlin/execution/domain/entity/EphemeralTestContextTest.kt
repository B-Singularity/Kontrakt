package execution.domain.entity

import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class EphemeralTestContextTest {
    private val mockSpec: TestSpecification = mock()
    private val mockEngine: MockingEngine = mock()
    private val mockControl: ScenarioControl = mock()

    private fun createSUT() = EphemeralTestContext(mockSpec, mockEngine, mockControl)

    @Test
    fun `constructor - should correctly hold reference properties`() {
        val sut = createSUT()

        assertSame(mockSpec, sut.specification)
        assertSame(mockEngine, sut.mockingEngine)
        assertSame(mockControl, sut.scenarioControl)
    }

    @Test
    fun `dependencies - should retrieve registered dependency`() {
        val sut = createSUT()
        val dependencyInstance = "I am a dependency"
        val type = String::class

        sut.registerDependency(type, dependencyInstance)

        val result = sut.getDependency(type)
        assertEquals(dependencyInstance, result)
    }

    @Test
    fun `dependencies - should return null for unregistered dependency`() {
        val sut = createSUT()

        val result = sut.getDependency(String::class)

        assertNull(result, "Should return null if map key does not exist")
    }

    @Test
    fun `dependencies - should overwrite existing dependency if registered again`() {
        val sut = createSUT()
        val oldInstance = "Old"
        val newInstance = "New"
        val type = String::class

        sut.registerDependency(type, oldInstance)
        sut.registerDependency(type, newInstance)

        assertEquals(newInstance, sut.getDependency(type))
    }

    @Test
    fun `getTestTarget - should return target instance if initialized`() {
        val sut = createSUT()
        val target = "MyTargetObject"

        sut.registerTarget(target)

        val result = sut.getTestTarget()
        assertEquals(target, result)
    }

    @Test
    fun `getTestTarget - should throw KontraktInternalException if NOT initialized`() {
        val sut = createSUT()

        val exception =
            assertFailsWith<KontraktInternalException> {
                sut.getTestTarget()
            }

        assertNotNull(exception.message)
        assertEquals(
            "[Internal Error] Test Target has not been initialized. Factory execution might have failed.",
            exception.message,
        )
    }
}
