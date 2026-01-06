package execution.domain.entity


import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.domain.trace.InMemoryScenarioTrace
import execution.spi.MockingEngine
import execution.spi.ScenarioControl
import execution.spi.trace.ScenarioTrace
import java.lang.reflect.Method
import kotlin.reflect.KClass

class EphemeralTestContext(
    val specification: TestSpecification,
    val mockingEngine: MockingEngine,
    val scenarioControl: ScenarioControl,
    val trace: ScenarioTrace = InMemoryScenarioTrace()
) {
    lateinit var targetMethod: Method

    private val dependencies = mutableMapOf<KClass<*>, Any>()
    private lateinit var targetInstance: Any

    fun registerDependency(
        type: KClass<*>,
        instance: Any,
    ) {
        dependencies[type] = instance
    }

    fun getDependency(type: KClass<*>): Any? = dependencies[type]

    fun registerTarget(instance: Any) {
        this.targetInstance = instance
    }

    fun getTestTarget(): Any {
        if (!::targetInstance.isInitialized) {
            throw KontraktInternalException("Test Target has not been initialized. Factory execution might have failed.")
        }
        return targetInstance
    }
}
