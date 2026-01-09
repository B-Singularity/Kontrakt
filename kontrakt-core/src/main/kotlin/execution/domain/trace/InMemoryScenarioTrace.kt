package execution.domain.trace

import execution.domain.vo.trace.TraceEvent
import execution.spi.trace.ScenarioTrace
import java.util.UUID

class InMemoryScenarioTrace(
    override val runId: String = UUID.randomUUID().toString(),
) : ScenarioTrace {
    override val decisions: MutableList<TraceEvent> = mutableListOf()

    override val generatedArguments: MutableList<Any?> = mutableListOf()

    override fun clear() {
        decisions.clear()
        generatedArguments.clear()
    }
}
