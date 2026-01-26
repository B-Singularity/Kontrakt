package execution.port.incoming

import discovery.domain.aggregate.TestSpecification
import execution.domain.aggregate.TestExecution
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

interface KontraktRuntimeFactoryContract {

    fun createSut(): KontraktRuntimeFactory

    fun createTestSpecification(): TestSpecification

    fun createTestScenarioExecutor(): TestScenarioExecutor

    @Test
    fun `createExecutor - returns a valid and configured TestScenarioExecutor`() {
        val sut = createSut()

        val executor = sut.createExecutor()

        assertThat(executor).isNotNull
        assertThat(executor).isInstanceOf(TestScenarioExecutor::class.java)
    }

    @Test
    fun `createExecution - assembles the full TestExecution aggregate with all dependencies`() {
        val sut = createSut()
        val spec = createTestSpecification()
        val executor = createTestScenarioExecutor()

        val execution = sut.createExecution(spec, executor)

        assertThat(execution).isNotNull
        assertThat(execution).isInstanceOf(TestExecution::class.java)
    }
}