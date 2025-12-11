package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import discovery.domain.vo.DiscoveredTestTarget
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.ClassSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KontraktTestDescriptorTest {

    private val uniqueId = UniqueId.forEngine("kontrakt-engine")

    @Test
    fun `getType should return CONTAINER when spec is null (Root Node)`() {

        val descriptor = KontraktTestDescriptor(uniqueId, "Root", null)

        val type = descriptor.type

        assertEquals(TestDescriptor.Type.CONTAINER, type, "Root descriptor without spec should be a CONTAINER")
    }

    @Test
    fun `getType should return TEST when spec is present (Leaf Node)`() {

        val mockSpec = mock<TestSpecification>()
        val descriptor = KontraktTestDescriptor(uniqueId, "MyTest", mockSpec)

        val type = descriptor.type

        assertEquals(TestDescriptor.Type.TEST, type, "Descriptor with spec should be a TEST")
    }

    @Test
    fun `getSource should return empty Optional when spec is null`() {
        val descriptor = KontraktTestDescriptor(uniqueId, "Root", null)

        val source = descriptor.source

        assertFalse(source.isPresent, "Source should be empty for root descriptor")
    }

    @Test
    fun `getSource should return ClassSource when spec is present`() {
        val targetClass = String::class
        val mockTarget = mock<DiscoveredTestTarget>()
        whenever(mockTarget.kClass).thenReturn(targetClass)

        val mockSpec = mock<TestSpecification>()
        whenever(mockSpec.target).thenReturn(mockTarget)

        val descriptor = KontraktTestDescriptor(uniqueId, "MyTest", mockSpec)

        val source = descriptor.source

        assertTrue(source.isPresent, "Source should be present for test descriptor")
        val classSource = source.get() as ClassSource
        assertEquals(targetClass.java, classSource.javaClass, "Source should point to the target class")
    }
}