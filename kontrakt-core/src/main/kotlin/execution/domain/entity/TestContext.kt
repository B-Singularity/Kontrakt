package execution.domain.entity

import discovery.domain.aggregate.TestSpecification
import execution.spi.MockingEngine
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class TestContext(
    private val specification: TestSpecification,
    private val mockingEngine: MockingEngine,
) {
    private val mocks: MutableMap<KClass<*>, Any> = mutableMapOf()
    private lateinit var testTargetInstance: Any

    fun prepare() {
        specification.requiredDependencies.forEach { dependency ->
            val mock = mockingEngine.createMock(dependency::class)
            mocks[dependency.type] = mock
        }

        val constructor = specification.target.kClass.primaryConstructor
            ?: throw IllegalStateException(
                "'${specification.target.displayName}' must have a primary constructor."
            )

        val arguments = constructor.parameters
            .map { mocks[it.type.classifier as KClass<*>]}
            .toTypedArray()

        this.testTargetInstance = constructor.call(*arguments)
    }

    fun getTestTarget(): Any = testTargetInstance

    fun getSpecification() = specification

    fun cleanup() {
        mocks.clear()
    }

}