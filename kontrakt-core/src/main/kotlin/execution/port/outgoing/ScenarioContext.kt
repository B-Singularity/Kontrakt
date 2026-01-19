package execution.port.outgoing

interface ScenarioContext {
    infix fun <T> every(methodCall: () -> T): StubbingBuilder<T>
}

interface StubbingBuilder<T> {
    infix fun returns(value: T)

    infix fun throws(exception: Throwable)
}
