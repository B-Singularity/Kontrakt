package planning.domain.runtime.lifecycle

/**
 * Top-level runtime-boundary orchestration lifecycle for one logical planning run.
 *
 * This state machine is orthogonal to:
 * - SharedSlotState
 * - WaiterState
 * - BuilderHandleState
 * - CommitRightState
 * - PartitionRegionState
 *
 * It exists because one logical planning run may now span more than one
 * worker-local PlannerSession through fresh-session restart.
 */
enum class PlanningRunState(
    val isTerminal: Boolean,
) {
    /**
     * The logical run has been created but no worker-local session lease has yet
     * been admitted into active execution.
     *
     * This supports queueing / admission control before the first RUNNING session.
     */
    INITIALIZED(isTerminal = false),

    /**
     * Exactly one worker-local session lease currently owns execution.
     */
    RUNNING(isTerminal = false),

    /**
     * The logical run has suspended because it is awaiting one joined-wait outcome.
     * No worker-local session lease remains active.
     */
    SUSPENDED_ON_JOIN(isTerminal = false),

    /**
     * Joined completion is available and the runtime may admit one fresh worker-local
     * session lease back into RUNNING.
     */
    READY_TO_RESTART(isTerminal = false),

    /**
     * The logical run has produced its final semantic result.
     */
    COMPLETED(isTerminal = true),

    /**
     * The logical run terminated unsuccessfully without panic-grade backing isolation.
     */
    ABORTED(isTerminal = true),

    /**
     * The logical run terminated under panic-grade isolation semantics.
     *
     * This state is reserved for failures that require the runtime boundary to treat
     * the surrounding worker/backing as potentially contaminated or otherwise unsafe.
     */
    PANIC_ISOLATED(isTerminal = true),
}