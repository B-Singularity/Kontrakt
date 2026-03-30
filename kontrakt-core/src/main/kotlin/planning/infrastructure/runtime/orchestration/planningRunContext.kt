package planning.infrastructure.runtime.orchestration

import planning.domain.exception.CapacityExceededException
import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.runtime.lifecycle.PlanningRunLifecycleLaw
import planning.domain.runtime.lifecycle.PlanningRunState
import planning.domain.runtime.orchestration.PlanningResumePoint
import planning.domain.runtime.orchestration.PlanningRunEpoch
import planning.infrastructure.runtime.policy.RuntimePolicyEpoch

/**
 * Runtime-boundary aggregate owning one logical end-to-end planning run.
 *
 * OWNERSHIP
 * - one PlanningRunEpoch
 * - one pinned RuntimePolicyEpoch
 * - one PlanningRunState
 * - one run-scoped remaining physical budget
 * - zero or one active worker-session lease
 * - zero or one current joined-wait suspension descriptor
 * - and one terminal cause when aborted or panic-isolated
 *
 * This object deliberately uses one monitor-protected authority surface.
 * It is a runtime-boundary orchestration aggregate, not a planner hot-path primitive host.
 */
class PlanningRunContext private constructor(
    val runEpoch: PlanningRunEpoch,
    val pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
    initialRemainingPhysicalBudget: Int,
) {
    private var state: PlanningRunState = PlanningRunState.INITIALIZED
    private var remainingPhysicalBudget: Int = initialRemainingPhysicalBudget
    private var activeWorkerSessionLease: PlanningRunWorkerSessionLease? = null
    private var currentSuspension: PlanningRunSuspension? = null
    private var terminalCause: Throwable? = null
    private var nextLeaseOrdinal: Long = 1L

    @Synchronized
    fun stateAcquire(): PlanningRunState = state

    @Synchronized
    fun remainingPhysicalBudgetAcquire(): Int = remainingPhysicalBudget

    @Synchronized
    fun activeWorkerSessionLeaseAcquire(): PlanningRunWorkerSessionLease? = activeWorkerSessionLease

    @Synchronized
    fun currentResumePointAcquire(): PlanningResumePoint? = currentSuspension?.resumePoint

    @Synchronized
    fun terminalCauseAcquire(): Throwable? = terminalCause

    @Synchronized
    fun hasActiveWorkerSessionAcquire(): Boolean = activeWorkerSessionLease != null

    @Synchronized
    fun isPanicIsolatedAcquire(): Boolean = state == PlanningRunState.PANIC_ISOLATED

    /**
     * INITIALIZED -> RUNNING
     *
     * This is the lawful first admission of one worker-local session lease.
     * It exists so that the run may be queued/admitted before first execution.
     */
    @Synchronized
    fun admitInitialRun(): PlanningRunWorkerSessionLease {
        assertStructuralInvariant()

        PlanningRunLifecycleLaw.requireTransition(
            from = state,
            to = PlanningRunState.RUNNING,
        )

        if (activeWorkerSessionLease != null) {
            throw PlanningProtocolIntegrityException(
                "INITIALIZED -> RUNNING requires no pre-existing worker-session lease."
            )
        }

        val newLease = issueNextLease()
        activeWorkerSessionLease = newLease
        state = PlanningRunState.RUNNING

        assertStructuralInvariant()
        return newLease
    }

    /**
     * Debits run-scoped remaining physical budget.
     *
     * This budget is run-scoped, not session-scoped.
     * A restarted session therefore continues consuming from the same remaining pool.
     *
     * This operation is legal only while the run is actively executing.
     */
    @Synchronized
    fun debitPhysicalBudget(
        units: Int,
    ): Int {
        assertStructuralInvariant()

        if (state != PlanningRunState.RUNNING) {
            throw PlanningProtocolIntegrityException(
                "Physical-budget debit requires RUNNING state: $state"
            )
        }
        if (units <= 0) {
            throw PlanningProtocolIntegrityException(
                "PlanningRunContext.debitPhysicalBudget requires units > 0: $units"
            )
        }
        if (units > remainingPhysicalBudget) {
            throw CapacityExceededException(
                limitType = "RUN_SCOPED_PHYSICAL_BUDGET",
                value = units.toLong(),
            )
        }

        remainingPhysicalBudget -= units
        return remainingPhysicalBudget
    }

    /**
     * RUNNING -> SUSPENDED_ON_JOIN
     *
     * This semantically detaches the active worker-session lease from the run.
     * The caller remains responsible for ordinary PlannerSession finally-cleanup.
     */
    @Synchronized
    fun suspendOnJoin(
        activeLease: PlanningRunWorkerSessionLease,
        suspension: PlanningRunSuspension,
    ) {
        assertStructuralInvariant()
        requireMatchingActiveLease(activeLease)

        PlanningRunLifecycleLaw.requireTransition(
            from = state,
            to = PlanningRunState.SUSPENDED_ON_JOIN,
        )

        currentSuspension = suspension
        activeWorkerSessionLease = null
        state = PlanningRunState.SUSPENDED_ON_JOIN

        assertStructuralInvariant()
    }

    /**
     * SUSPENDED_ON_JOIN -> READY_TO_RESTART
     *
     * Joined completion is now available and a fresh worker-local session may be admitted.
     */
    @Synchronized
    fun markReadyToRestart() {
        assertStructuralInvariant()

        requireCurrentSuspension(
            expectedState = PlanningRunState.SUSPENDED_ON_JOIN,
        )

        PlanningRunLifecycleLaw.requireTransition(
            from = state,
            to = PlanningRunState.READY_TO_RESTART,
        )
        state = PlanningRunState.READY_TO_RESTART

        assertStructuralInvariant()
    }

    /**
     * READY_TO_RESTART -> RUNNING
     *
     * Admits exactly one fresh worker-local session back into execution for the
     * same PlanningRunEpoch and same pinned RuntimePolicyEpoch.
     */
    @Synchronized
    fun admitRestart(): PlanningRunRestartAdmission {
        assertStructuralInvariant()

        PlanningRunLifecycleLaw.requireTransition(
            from = state,
            to = PlanningRunState.RUNNING,
        )

        val suspension = requireCurrentSuspension(
            expectedState = PlanningRunState.READY_TO_RESTART,
        )

        if (remainingPhysicalBudget <= 0) {
            throw CapacityExceededException(
                limitType = "RUN_SCOPED_PHYSICAL_BUDGET",
                value = 1L,
            )
        }
        if (activeWorkerSessionLease != null) {
            throw PlanningProtocolIntegrityException(
                "Restart admission requires no active worker-session lease."
            )
        }

        val newLease = issueNextLease()
        activeWorkerSessionLease = newLease
        currentSuspension = null
        state = PlanningRunState.RUNNING

        assertStructuralInvariant()

        return PlanningRunRestartAdmission.issue(
            workerSessionLease = newLease,
            runEpoch = runEpoch,
            pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
            resumePoint = suspension.resumePoint,
            remainingPhysicalBudget = remainingPhysicalBudget,
        )
    }

    /**
     * RUNNING -> COMPLETED
     */
    @Synchronized
    fun complete(
        activeLease: PlanningRunWorkerSessionLease,
    ) {
        assertStructuralInvariant()
        requireMatchingActiveLease(activeLease)

        PlanningRunLifecycleLaw.requireTransition(
            from = state,
            to = PlanningRunState.COMPLETED,
        )

        activeWorkerSessionLease = null
        currentSuspension = null
        terminalCause = null
        state = PlanningRunState.COMPLETED

        assertStructuralInvariant()
    }

    /**
     * Non-panic terminalization.
     *
     * Allowed from:
     * - INITIALIZED
     * - RUNNING
     * - SUSPENDED_ON_JOIN
     * - READY_TO_RESTART
     */
    @Synchronized
    fun abort(
        cause: Throwable,
        activeLease: PlanningRunWorkerSessionLease? = null,
    ) {
        terminate(
            targetState = PlanningRunState.ABORTED,
            cause = cause,
            activeLease = activeLease,
        )
    }

    /**
     * Panic-grade terminalization.
     *
     * This state records that the failure is not merely logical/request-level failure,
     * but one that may require worker/backing quarantine or stronger isolation policy.
     */
    @Synchronized
    fun panicIsolate(
        cause: Throwable,
        activeLease: PlanningRunWorkerSessionLease? = null,
    ) {
        terminate(
            targetState = PlanningRunState.PANIC_ISOLATED,
            cause = cause,
            activeLease = activeLease,
        )
    }

    @Synchronized
    private fun terminate(
        targetState: PlanningRunState,
        cause: Throwable,
        activeLease: PlanningRunWorkerSessionLease?,
    ) {
        assertStructuralInvariant()

        if (targetState != PlanningRunState.ABORTED && targetState != PlanningRunState.PANIC_ISOLATED) {
            throw PlanningProtocolIntegrityException(
                "PlanningRunContext.terminate supports only ABORTED or PANIC_ISOLATED target states: $targetState"
            )
        }

        when (state) {
            PlanningRunState.INITIALIZED -> {
                if (activeLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED -> $targetState must not receive an active worker-session lease."
                    )
                }
                PlanningRunLifecycleLaw.requireTransition(
                    from = state,
                    to = targetState,
                )
            }

            PlanningRunState.RUNNING -> {
                val lease = activeLease
                    ?: throw PlanningProtocolIntegrityException(
                        "RUNNING -> $targetState requires the active worker-session lease."
                    )
                requireMatchingActiveLease(lease)

                PlanningRunLifecycleLaw.requireTransition(
                    from = state,
                    to = targetState,
                )
            }

            PlanningRunState.SUSPENDED_ON_JOIN,
            PlanningRunState.READY_TO_RESTART -> {
                if (activeLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state -> $targetState must not receive an active worker-session lease."
                    )
                }

                val suspension = requireCurrentSuspension(
                    expectedState = state,
                )

                PlanningRunLifecycleLaw.requireTransition(
                    from = state,
                    to = targetState,
                )

                suspension.suspensionHandle.cancel(cause)
            }

            PlanningRunState.COMPLETED,
            PlanningRunState.ABORTED,
            PlanningRunState.PANIC_ISOLATED -> {
                throw PlanningProtocolIntegrityException(
                    "Cannot terminate terminal planning-run state: $state"
                )
            }
        }

        currentSuspension = null
        activeWorkerSessionLease = null
        terminalCause = cause
        state = targetState

        assertStructuralInvariant()
    }

    private fun requireMatchingActiveLease(
        lease: PlanningRunWorkerSessionLease,
    ) {
        val active = activeWorkerSessionLease
            ?: throw PlanningProtocolIntegrityException(
                "No active worker-session lease is currently bound to PlanningRunContext."
            )

        if (active != lease) {
            throw PlanningProtocolIntegrityException(
                "PlanningRunContext worker-session lease mismatch: expected=$active, actual=$lease"
            )
        }
    }

    private fun requireCurrentSuspension(
        expectedState: PlanningRunState,
    ): PlanningRunSuspension {
        return currentSuspension
            ?: throw PlanningProtocolIntegrityException(
                "PlanningRunContext requires a current suspension in state $expectedState."
            )
    }

    private fun issueNextLease(): PlanningRunWorkerSessionLease {
        return PlanningRunWorkerSessionLease.issue(
            runEpoch = runEpoch,
            ordinal = nextLeaseOrdinal++,
        )
    }

    /**
     * Structural invariant of the runtime-boundary state machine.
     *
     * INITIALIZED:
     * - active lease absent
     * - suspension absent
     * - terminal cause absent
     *
     * RUNNING:
     * - active lease present
     * - suspension absent
     * - terminal cause absent
     *
     * SUSPENDED_ON_JOIN / READY_TO_RESTART:
     * - active lease absent
     * - suspension present
     * - terminal cause absent
     *
     * COMPLETED:
     * - active lease absent
     * - suspension absent
     * - terminal cause absent
     *
     * ABORTED / PANIC_ISOLATED:
     * - active lease absent
     * - suspension absent
     * - terminal cause present
     */
    private fun assertStructuralInvariant() {
        when (state) {
            PlanningRunState.INITIALIZED -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED PlanningRunContext must not retain an active worker-session lease."
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED PlanningRunContext must not retain a current suspension."
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED PlanningRunContext must not retain a terminal cause."
                    )
                }
            }

            PlanningRunState.RUNNING -> {
                if (activeWorkerSessionLease == null) {
                    throw PlanningProtocolIntegrityException(
                        "RUNNING PlanningRunContext requires an active worker-session lease."
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "RUNNING PlanningRunContext must not retain a current suspension."
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "RUNNING PlanningRunContext must not retain a terminal cause."
                    )
                }
            }

            PlanningRunState.SUSPENDED_ON_JOIN,
            PlanningRunState.READY_TO_RESTART -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain an active worker-session lease."
                    )
                }
                if (currentSuspension == null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext requires a current suspension."
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain a terminal cause."
                    )
                }
            }

            PlanningRunState.COMPLETED -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "COMPLETED PlanningRunContext must not retain an active worker-session lease."
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "COMPLETED PlanningRunContext must not retain a current suspension."
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "COMPLETED PlanningRunContext must not retain a terminal cause."
                    )
                }
            }

            PlanningRunState.ABORTED,
            PlanningRunState.PANIC_ISOLATED -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain an active worker-session lease."
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain a current suspension."
                    )
                }
                if (terminalCause == null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext requires a terminal cause."
                    )
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun issue(
            runEpoch: PlanningRunEpoch,
            pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
            initialRemainingPhysicalBudget: Int,
        ): PlanningRunContext {
            if (initialRemainingPhysicalBudget <= 0) {
                throw PlanningProtocolIntegrityException(
                    "PlanningRunContext.initialRemainingPhysicalBudget must be > 0: $initialRemainingPhysicalBudget"
                )
            }

            return PlanningRunContext(
                runEpoch = runEpoch,
                pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
                initialRemainingPhysicalBudget = initialRemainingPhysicalBudget,
            )
        }
    }
}