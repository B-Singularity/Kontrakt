package planning.domain.session

import stage.lowering.diagnostics.PlanningProtocolIntegrityException

/**
 * Fixed-capacity frame stack.
 *
 * Capacity is pinned from session caps to avoid runtime reallocation.
 */
internal class ExecutionFrameStack private constructor(
    capacity: Int,
) {
    private val elements: Array<ExecutionFrame?> = arrayOfNulls(capacity)
    private var size: Int = 0

    fun isNotEmpty(): Boolean = size > 0

    fun last(): ExecutionFrame {
        if (size == 0) {
            throw PlanningProtocolIntegrityException("ExecutionFrameStack.last() on empty stack.")
        }
        return elements[size - 1]
            ?: throw PlanningProtocolIntegrityException("ExecutionFrameStack top is unexpectedly null.")
    }

    fun lastIndex(): Int {
        if (size == 0) {
            throw PlanningProtocolIntegrityException("ExecutionFrameStack.lastIndex() on empty stack.")
        }
        return size - 1
    }

    fun push(frame: ExecutionFrame) {
        if (size >= elements.size) {
            throw PlanningProtocolIntegrityException(
                "ExecutionFrameStack capacity exceeded: size=$size, capacity=${elements.size}",
            )
        }
        elements[size++] = frame
    }

    fun pop(): ExecutionFrame {
        if (size == 0) {
            throw PlanningProtocolIntegrityException("ExecutionFrameStack.pop() on empty stack.")
        }
        val idx = size - 1
        val frame =
            elements[idx]
                ?: throw PlanningProtocolIntegrityException("ExecutionFrameStack top is unexpectedly null.")
        elements[idx] = null
        size = idx
        return frame
    }

    fun replaceTop(frame: ExecutionFrame) {
        if (size == 0) {
            throw PlanningProtocolIntegrityException("ExecutionFrameStack.replaceTop() on empty stack.")
        }
        elements[size - 1] = frame
    }

    fun get(index: Int): ExecutionFrame {
        if (index < 0 || index >= size) {
            throw PlanningProtocolIntegrityException("ExecutionFrameStack index out of bounds: $index")
        }
        return elements[index]
            ?: throw PlanningProtocolIntegrityException("ExecutionFrameStack[$index] is unexpectedly null.")
    }

    fun clear() {
        while (size > 0) {
            elements[--size] = null
        }
    }

    companion object {
        @JvmStatic
        fun issue(capacity: Int): ExecutionFrameStack {
            if (capacity <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ExecutionFrameStack capacity must be > 0: $capacity",
                )
            }
            return ExecutionFrameStack(capacity)
        }
    }
}
