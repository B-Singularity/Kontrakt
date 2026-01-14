package execution.domain.trace

import execution.domain.vo.trace.TraceEvent
import execution.spi.trace.ScenarioTrace
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class InMemoryScenarioTrace(
    override val runId: String = UUID.randomUUID().toString(),
) : ScenarioTrace {
    override val decisions: MutableList<TraceEvent> =
        Collections.synchronizedList(ArrayList())

    override val generatedArguments: MutableList<Any?> =
        CopyOnWriteArrayList()

    override fun clear() {
        decisions.clear()
        generatedArguments.clear()
    }

    override fun recordGeneratedArguments(args: List<Any?>) {
        generatedArguments.clear()
        generatedArguments.addAll(args)
    }
}
