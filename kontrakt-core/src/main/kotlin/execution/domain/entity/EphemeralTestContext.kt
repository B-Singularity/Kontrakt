package execution.domain.entity

import discovery.domain.aggregate.TestSpecification
import execution.spi.MockingEngine
import kotlin.reflect.KClass

class EphemeralTestContext(
    val specification: TestSpecification,
    val mockingEngine: MockingEngine,
) {
    private val dependencies = mutableMapOf<KClass<*>, Any>()
    private lateinit var targetInstance: Any

    fun registerDependency(type: KClass<*>, instance: Any) {
        dependencies[type] = instance
    }

    fun getDependency(type: KClass<*>): Any? = dependencies[type]

    fun registerTarget(instance: Any) {
        this.targetInstance = instance
    }

    fun getTestTarget(): Any = targetInstance

}