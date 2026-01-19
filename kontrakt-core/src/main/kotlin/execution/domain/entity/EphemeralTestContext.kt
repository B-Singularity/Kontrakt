package execution.domain.entity

import discovery.domain.aggregate.TestSpecification
import exception.KontraktInternalException
import execution.port.outgoing.MockingEngine
import execution.port.outgoing.ScenarioControl
import execution.port.outgoing.ScenarioTrace
import java.lang.reflect.Method
import kotlin.reflect.KClass

class EphemeralTestContext(
    val specification: TestSpecification,
    val mockingEngine: MockingEngine,
    val scenarioControl: ScenarioControl,
    val trace: ScenarioTrace,
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
