package execution.adapter.junit

import discovery.domain.aggregate.TestSpecification
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import java.util.Optional

class KontraktTestDescriptor(
    uniqueId: UniqueId,
    displayName: String,
    val spec: TestSpecification? = null,
) : AbstractTestDescriptor(uniqueId, displayName) {
    override fun getType(): TestDescriptor.Type = if (spec == null) TestDescriptor.Type.CONTAINER else TestDescriptor.Type.TEST

    override fun getSource(): Optional<TestSource> =
        if (spec != null) {
            Optional.of(ClassSource.from(spec.target.kClass.java))
        } else {
            Optional.empty()
        }
}
