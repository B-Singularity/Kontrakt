package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveredTestTarget
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.ClassSource

class KontraktTestDescriptorTest {

    // =================================================================================================================
    // Type Resolution Tests (getType)
    // =================================================================================================================

    @Test
    fun `getType should return CONTAINER when spec is null`() {
        val uniqueId = UniqueId.forEngine("kontrakt")
        val displayName = "Kontrakt Root"

        // Initialize without spec (null by default)
        val descriptor = KontraktTestDescriptor(uniqueId, displayName)

        assertThat(descriptor.type).isEqualTo(TestDescriptor.Type.CONTAINER)
    }

    @Test
    fun `getType should return TEST when spec is provided`() {
        val uniqueId = UniqueId.forEngine("kontrakt").append("test", "MyTest")
        val displayName = "My Scenario Test"
        val mockSpec = mockk<TestSpecification>()

        val descriptor = KontraktTestDescriptor(uniqueId, displayName, mockSpec)

        assertThat(descriptor.type).isEqualTo(TestDescriptor.Type.TEST)
    }

    // =================================================================================================================
    // Source Resolution Tests (getSource)
    // =================================================================================================================

    @Test
    fun `getSource should return empty Optional when spec is null`() {
        val uniqueId = UniqueId.forEngine("kontrakt")
        val descriptor = KontraktTestDescriptor(uniqueId, "Root")

        val source = descriptor.source

        assertThat(source).isEmpty()
    }

    @Test
    fun `getSource should return ClassSource derived from target kClass when spec is provided`() {
        // Given
        val uniqueId = UniqueId.forEngine("kontrakt").append("test", "MyTest")
        val targetClass = String::class // Example class

        // Mock chain: TestSpecification -> DiscoveredTestTarget -> KClass
        val mockTarget = mockk<DiscoveredTestTarget> {
            every { kClass } returns targetClass
        }
        val mockSpec = mockk<TestSpecification> {
            every { target } returns mockTarget
        }

        val descriptor = KontraktTestDescriptor(uniqueId, "MyTest", mockSpec)

        // When
        val source = descriptor.source

        // Then
        assertThat(source).isPresent
        assertThat(source.get()).isInstanceOf(ClassSource::class.java)

        val classSource = source.get() as ClassSource
        assertThat(classSource.javaClass).isEqualTo(targetClass.java)
    }

    // =================================================================================================================
    // Property Integrity Tests
    // =================================================================================================================

    @Test
    fun `constructor should correctly set uniqueId and displayName`() {
        val uniqueId = UniqueId.forEngine("kontrakt").append("segment", "value")
        val displayName = "Display Name"

        val descriptor = KontraktTestDescriptor(uniqueId, displayName)

        assertThat(descriptor.uniqueId).isEqualTo(uniqueId)
        assertThat(descriptor.displayName).isEqualTo(displayName)
    }
}