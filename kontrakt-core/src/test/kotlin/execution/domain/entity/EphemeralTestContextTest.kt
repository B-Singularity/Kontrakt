package execution.domain.entity

import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioControl
import execution.port.outgoing.ScenarioTrace
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EphemeralTestContextTest {

    // Mocks for constructor arguments
    private val specification = mockk<TestSpecification>()
    private val mockingEngine = mockk<MockingEngine>()
    private val scenarioControl = mockk<ScenarioControl>()
    private val trace = mockk<ScenarioTrace>()

    private lateinit var sut: EphemeralTestContext

    @BeforeEach
    fun setUp() {
        sut = EphemeralTestContext(
            specification = specification,
            mockingEngine = mockingEngine,
            scenarioControl = scenarioControl,
            trace = trace
        )
    }

    // =========================================================================
    // 1. Dependency Management Tests
    // =========================================================================

    @Test
    fun `registerDependency - stores and retrieves dependency by type`() {
        // Given
        val dummyDependency = "I am a dependency"
        val type = String::class

        // When
        sut.registerDependency(type, dummyDependency)
        val result = sut.getDependency(type)

        // Then
        assertThat(result).isEqualTo(dummyDependency)
    }

    @Test
    fun `getDependency - returns null for unregistered type`() {
        // When
        val result = sut.getDependency(Int::class)

        // Then
        assertThat(result).isNull()
    }

    // =========================================================================
    // 2. Target Instance Lifecycle Tests
    // =========================================================================

    @Test
    fun `registerTarget - initializes the target instance`() {
        // Given
        val target = Any()

        // When
        sut.registerTarget(target)

        // Then
        assertThat(sut.getTestTarget()).isSameAs(target)
    }

    @Test
    fun `getTestTarget - throws exception if target is not initialized`() {
        // Given: registerTarget is NOT called

        // When & Then
        assertThatThrownBy {
            sut.getTestTarget()
        }.isInstanceOf(KontraktInternalException::class.java)
            .hasMessageContaining("Test Target has not been initialized")
    }

    // =========================================================================
    // 3. Target Method Property Tests
    // =========================================================================

    @Test
    fun `targetMethod - holds reference to the method under test`() {
        // Given
        val method = String::class.java.methods.first()

        // When
        sut.targetMethod = method

        // Then
        assertThat(sut.targetMethod).isEqualTo(method)
    }

    @Test
    fun `constructor - initializes properties correctly`() {
        // Verify that the constructor properties are accessible
        assertThat(sut.specification).isSameAs(specification)
        assertThat(sut.mockingEngine).isSameAs(mockingEngine)
        assertThat(sut.scenarioControl).isSameAs(scenarioControl)
        assertThat(sut.trace).isSameAs(trace)
    }
}