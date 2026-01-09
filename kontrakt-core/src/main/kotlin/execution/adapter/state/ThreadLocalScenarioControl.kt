package execution.adapter.state

import exception.KontraktInternalException
import execution.adapter.MockitoScenarioContext
import execution.api.ScenarioContext
import execution.domain.vo.ExecutionEnvironment
import execution.spi.ScenarioControl
import kotlinx.coroutines.asContextElement
import kotlin.coroutines.CoroutineContext

internal class ThreadLocalScenarioControl : ScenarioControl {
    companion object {
        private val STORAGE = ThreadLocal<ExecutionEnvironment>()

        fun get(): ExecutionEnvironment =
            STORAGE.get()
                ?: throw KontraktInternalException(
                    "No active ExecutionEnvironment found. (Internal Error: Lifecycle mismatch)",
                )

        fun bind(env: ExecutionEnvironment) {
            if (STORAGE.get() != null) STORAGE.remove()
            STORAGE.set(env)
        }

        fun clear() {
            STORAGE.remove()
        }

        fun requireCoroutineContext(): CoroutineContext = STORAGE.asContextElement(value = get())
    }

    override fun createScenarioContext(): ScenarioContext = MockitoScenarioContext()
}
