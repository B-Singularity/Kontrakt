package realization.runtime.orchestration

import migration.quarantine.RuntimePolicyEpoch
import stage.lowering.diagnostics.CapacityExceededException
import stage.lowering.diagnostics.PlanningProtocolIntegrityException
import statemachine.state.material.condition.PlanningRunState
import statemachine.transition.contract.PlanningRunLifecycleLaw
import statemachine.transition.material.move.PlanningResumePoint
import statemachine.transition.material.move.PlanningRunEpoch
import statemachine.transition.material.move.PlanningRunRemainingBudget

/**
 * Runtime-boundary aggregate owning one logical end-to-end planning run.
 *
 * OWNERSHIP
 * - one PlanningRunEpoch
 * - one pinned RuntimePolicyEpoch
 * - one PlanningRunState
 * - one run-scoped remaining execution budget ledger
 * - zero or one active worker-session lease
 * - zero or one current joined-wait suspension descriptor
 * - and one terminal cause when aborted or panic-isolated
 *
 * This aggregate does NOT own:
 * - worker-local primitive backing
 * - planner-core frame stack
 * - L2 lifecycle-host truth
 * - runtime-policy installation
 * - adapter-owned completion-dispatch infrastructure
 *
 * Concurrency stance:
 * - one monitor-protected authority surface
 * - explicit state-machine invariant checks
 * - no volatile/mixed authority surface
 */
class PlanningRunContext private constructor(
    val runEpoch: PlanningRunEpoch,
    val pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
    initialRemainingBudget: PlanningRunRemainingBudget,
) {
    private var state: PlanningRunState = PlanningRunState.INITIALIZED
    private var remainingBudget: PlanningRunRemainingBudget = initialRemainingBudget
    private var activeWorkerSessionLease: PlanningRunWorkerSessionLease? = null
    private var currentSuspension: PlanningRunSuspension? = null
    private var terminalCause: Throwable? = null
    private var nextLeaseOrdinal: Long = 1L

    @Synchronized
    fun stateAcquire(): PlanningRunState = state

    @Synchronized
    fun remainingBudgetAcquire(): PlanningRunRemainingBudget = remainingBudget

    @Synchronized
    fun remainingPhysicalStepsAcquire(): Int = remainingBudget.remainingPhysicalSteps

    @Synchronized
    fun remainingSemanticWorkUnitsAcquire(): Int = remainingBudget.remainingSemanticWorkUnits

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
     * Lawful first admission of one worker-local session lease.
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
                "INITIALIZED -> RUNNING requires no pre-existing worker-session lease.",
            )
        }

        val lease = issueNextLease()
        activeWorkerSessionLease = lease
        state = PlanningRunState.RUNNING

        assertStructuralInvariant()
        return lease
    }

    /**
     * Debits run-scoped remaining physical-step budget.
     *
     * This is separate from pinned policy snapshot ownership.
     */
    @Synchronized
    fun debitPhysicalSteps(units: Int): PlanningRunRemainingBudget {
        assertStructuralInvariant()

        if (state != PlanningRunState.RUNNING) {
            throw PlanningProtocolIntegrityException(
                "Physical-step debit requires RUNNING state: $state",
            )
        }

        remainingBudget = remainingBudget.debitPhysicalSteps(units)
        return remainingBudget
    }

    /**
     * Debits run-scoped remaining semantic-work budget.
     *
     * This keeps run-level boundedness aligned with the fact that the current
     * PlannerSession model tracks both physical and semantic counters.
     */
    @Synchronized
    fun debitSemanticWorkUnits(units: Int): PlanningRunRemainingBudget {
        assertStructuralInvariant()

        if (state != PlanningRunState.RUNNING) {
            throw PlanningProtocolIntegrityException(
                "Semantic-work debit requires RUNNING state: $state",
            )
        }

        remainingBudget = remainingBudget.debitSemanticWorkUnits(units)
        return remainingBudget
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
     */
    @Synchronized
    fun markReadyToRestart() {
        assertStructuralInvariant()

        requireCurrentSuspension(
            expectedState = PlanningRunState.SUSPENDED_ON_JOIN,
        )

        markReadyToRestartUnderLock()
    }

    /**
     * Best-effort asynchronous readiness publication for joined completion.
     *
     * This method exists so that adapter-owned completion delivery may safely notify
     * the planning-run axis without treating post-abort / post-complete races as
     * hard failures.
     *
     * Semantics:
     * - SUSPENDED_ON_JOIN -> READY_TO_RESTART : returns true
     * - all other states                     : returns false
     *
     * This method is intentionally callback-safe.
     * Async delivery callbacks must not be required to throw in order to report that
     * readiness publication is no longer meaningful.
     */
    @Synchronized
    fun tryMarkReadyToRestart(): Boolean {
        assertStructuralInvariant()

        return if (state == PlanningRunState.SUSPENDED_ON_JOIN) {
            markReadyToRestartUnderLock()
            true
        } else {
            false
        }
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

        val suspension =
            requireCurrentSuspension(
                expectedState = PlanningRunState.READY_TO_RESTART,
            )

        if (!remainingBudget.hasRemainingPhysicalSteps()) {
            throw CapacityExceededException(
                limitType = "RUN_SCOPED_PHYSICAL_STEPS",
                value = 1L,
            )
        }
        if (activeWorkerSessionLease != null) {
            throw PlanningProtocolIntegrityException(
                "Restart admission requires no active worker-session lease.",
            )
        }

        val lease = issueNextLease()
        activeWorkerSessionLease = lease
        currentSuspension = null
        state = PlanningRunState.RUNNING

        assertStructuralInvariant()

        return PlanningRunRestartAdmission.issue(
            workerSessionLease = lease,
            runEpoch = runEpoch,
            pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
            resumePoint = suspension.resumePoint,
            remainingBudget = remainingBudget,
            suspensionHandle = suspension.suspensionHandle,
        )
    }

    /**
     * RUNNING -> COMPLETED
     */
    @Synchronized
    fun complete(activeLease: PlanningRunWorkerSessionLease) {
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
     * Non-panic unsuccessful terminalization.
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
     * Panic-grade isolated terminalization.
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
                "PlanningRunContext.terminate supports only ABORTED or PANIC_ISOLATED target states: $targetState",
            )
        }

        when (state) {
            PlanningRunState.INITIALIZED -> {
                if (activeLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED -> $targetState must not receive an active worker-session lease.",
                    )
                }

                PlanningRunLifecycleLaw.requireTransition(
                    from = state,
                    to = targetState,
                )
            }

            PlanningRunState.RUNNING -> {
                val lease =
                    activeLease
                        ?: throw PlanningProtocolIntegrityException(
                            "RUNNING -> $targetState requires the active worker-session lease.",
                        )

                requireMatchingActiveLease(lease)

                PlanningRunLifecycleLaw.requireTransition(
                    from = state,
                    to = targetState,
                )
            }

            PlanningRunState.SUSPENDED_ON_JOIN,
            PlanningRunState.READY_TO_RESTART,
                -> {
                if (activeLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state -> $targetState must not receive an active worker-session lease.",
                    )
                }

                val suspension =
                    requireCurrentSuspension(
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
            PlanningRunState.PANIC_ISOLATED,
                -> {
                throw PlanningProtocolIntegrityException(
                    "Cannot terminate terminal planning-run state: $state",
                )
            }
        }

        currentSuspension = null
        activeWorkerSessionLease = null
        terminalCause = cause
        state = targetState

        assertStructuralInvariant()
    }

    private fun requireMatchingActiveLease(lease: PlanningRunWorkerSessionLease) {
        val active =
            activeWorkerSessionLease
                ?: throw PlanningProtocolIntegrityException(
                    "No active worker-session lease is currently bound to PlanningRunContext.",
                )

        if (active != lease) {
            throw PlanningProtocolIntegrityException(
                "PlanningRunContext worker-session lease mismatch: expected=$active, actual=$lease",
            )
        }
    }

    private fun requireCurrentSuspension(expectedState: PlanningRunState): PlanningRunSuspension =
        currentSuspension
            ?: throw PlanningProtocolIntegrityException(
                "PlanningRunContext requires a current suspension in state $expectedState.",
            )

    private fun issueNextLease(): PlanningRunWorkerSessionLease =
        PlanningRunWorkerSessionLease.issue(
            runEpoch = runEpoch,
            ordinal = nextLeaseOrdinal++,
        )

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
                        "INITIALIZED PlanningRunContext must not retain an active worker-session lease.",
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED PlanningRunContext must not retain a current suspension.",
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "INITIALIZED PlanningRunContext must not retain a terminal cause.",
                    )
                }
            }

            PlanningRunState.RUNNING -> {
                if (activeWorkerSessionLease == null) {
                    throw PlanningProtocolIntegrityException(
                        "RUNNING PlanningRunContext requires an active worker-session lease.",
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "RUNNING PlanningRunContext must not retain a current suspension.",
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "RUNNING PlanningRunContext must not retain a terminal cause.",
                    )
                }
            }

            PlanningRunState.SUSPENDED_ON_JOIN,
            PlanningRunState.READY_TO_RESTART,
                -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain an active worker-session lease.",
                    )
                }
                if (currentSuspension == null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext requires a current suspension.",
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain a terminal cause.",
                    )
                }
            }

            PlanningRunState.COMPLETED -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "COMPLETED PlanningRunContext must not retain an active worker-session lease.",
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "COMPLETED PlanningRunContext must not retain a current suspension.",
                    )
                }
                if (terminalCause != null) {
                    throw PlanningProtocolIntegrityException(
                        "COMPLETED PlanningRunContext must not retain a terminal cause.",
                    )
                }
            }

            PlanningRunState.ABORTED,
            PlanningRunState.PANIC_ISOLATED,
                -> {
                if (activeWorkerSessionLease != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain an active worker-session lease.",
                    )
                }
                if (currentSuspension != null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext must not retain a current suspension.",
                    )
                }
                if (terminalCause == null) {
                    throw PlanningProtocolIntegrityException(
                        "$state PlanningRunContext requires a terminal cause.",
                    )
                }
            }
        }
    }

    /**
     * Non-reentrant helper for READY_TO_RESTART publication.
     *
     * This helper assumes the caller already owns the PlanningRunContext monitor.
     * It exists to avoid nested synchronized method calls while keeping the
     * state-transition logic single-sourced.
     *
     * Precondition:
     * - state == SUSPENDED_ON_JOIN
     * - structural invariant already holds
     */
    private fun markReadyToRestartUnderLock() {
        PlanningRunLifecycleLaw.requireTransition(
            from = state,
            to = PlanningRunState.READY_TO_RESTART,
        )

        state = PlanningRunState.READY_TO_RESTART
        assertStructuralInvariant()
    }

    companion object {
        /**
         * Creates a new logical planning run with its initial remaining execution budget
         * derived from the pinned runtime-policy snapshot.
         */
        @JvmStatic
        fun issue(
            runEpoch: PlanningRunEpoch,
            pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
        ): PlanningRunContext =
            PlanningRunContext(
                runEpoch = runEpoch,
                pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
                initialRemainingBudget =
                    PlanningRunRemainingBudget.issueFrom(
                        pinnedRuntimePolicyEpoch.policy.sessionBudget,
                    ),
            )

        /**
         * Explicit testing / recovery-oriented constructor for cases that must restore
         * a specific remaining run budget while keeping the same pinned policy snapshot.
         */
        @JvmStatic
        fun issueWithRemainingBudget(
            runEpoch: PlanningRunEpoch,
            pinnedRuntimePolicyEpoch: RuntimePolicyEpoch,
            initialRemainingBudget: PlanningRunRemainingBudget,
        ): PlanningRunContext =
            PlanningRunContext(
                runEpoch = runEpoch,
                pinnedRuntimePolicyEpoch = pinnedRuntimePolicyEpoch,
                initialRemainingBudget = initialRemainingBudget,
            )
    }
}
