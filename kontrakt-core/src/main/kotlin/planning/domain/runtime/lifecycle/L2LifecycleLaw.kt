package planning.domain.runtime.lifecycle

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Single source of truth for closed top-level lifecycle legality across the Planning L2 runtime.
 *
 * Scope:
 * - shared-slot lifecycle
 * - waiter lifecycle
 * - builder-handle lifecycle
 * - commit-right lifecycle
 * - partition-region lifecycle
 * - speculative-lease lifecycle
 *
 * Architectural role:
 * - This object is the semantic/runtime law surface for closed-state legality.
 * - It is intentionally separate from:
 *   - reason taxonomy,
 *   - delivery mechanics,
 *   - scheduling,
 *   - CAS layout,
 *   - and adapter-owned execution details.
 *
 * Why this file exists:
 * - `canTransition(...)` alone is not sufficient if production code forgets to call it.
 * - Therefore this file provides both:
 *   - query-style legality checks, and
 *   - runtime enforcement surfaces via `requireTransition(...)`.
 *
 * Design intent:
 * - keep top-level state sets closed,
 * - keep transition legality explicit,
 * - keep cross-axis invariants visible,
 * - and provide one boring, stable place where lifecycle law can be read mechanically.
 *
 * This file is intentionally explicit rather than clever.
 * That is a feature.
 */
object L2LifecycleLaw {

    // ─────────────────────────────────────────────────────────────
    // Shared-slot lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: SharedSlotState,
        to: SharedSlotState,
    ): Boolean {
        return when (from) {
            SharedSlotState.PENDING ->
                to == SharedSlotState.SUCCESS ||
                        to == SharedSlotState.FAILED ||
                        to == SharedSlotState.DROPPED

            SharedSlotState.SUCCESS,
            SharedSlotState.FAILED,
            SharedSlotState.DROPPED -> false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: SharedSlotState,
        to: SharedSlotState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal SharedSlotState transition: $from -> $to"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Waiter lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: WaiterState,
        to: WaiterState,
    ): Boolean {
        return when (from) {
            WaiterState.ATTACHED ->
                to == WaiterState.RESUMED ||
                        to == WaiterState.TIMED_OUT ||
                        to == WaiterState.CANCELLED

            WaiterState.RESUMED,
            WaiterState.TIMED_OUT,
            WaiterState.CANCELLED -> false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: WaiterState,
        to: WaiterState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal WaiterState transition: $from -> $to"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Builder-handle lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: BuilderHandleState,
        to: BuilderHandleState,
    ): Boolean {
        return when (from) {
            BuilderHandleState.OPEN ->
                to == BuilderHandleState.COMMITTED ||
                        to == BuilderHandleState.ABORTED

            BuilderHandleState.COMMITTED,
            BuilderHandleState.ABORTED -> false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: BuilderHandleState,
        to: BuilderHandleState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal BuilderHandleState transition: $from -> $to"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Commit-right lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: CommitRightState,
        to: CommitRightState,
    ): Boolean {
        return when (from) {
            CommitRightState.UNCLAIMED -> to == CommitRightState.CLAIMED
            CommitRightState.CLAIMED -> to == CommitRightState.RELEASED
            CommitRightState.RELEASED -> false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: CommitRightState,
        to: CommitRightState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal CommitRightState transition: $from -> $to"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Partition-region lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: PartitionRegionState,
        to: PartitionRegionState,
    ): Boolean {
        return when (from) {
            PartitionRegionState.OPEN -> to == PartitionRegionState.CLOSE_PUBLISHED
            PartitionRegionState.CLOSE_PUBLISHED -> to == PartitionRegionState.RECLAIMED
            PartitionRegionState.RECLAIMED -> false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: PartitionRegionState,
        to: PartitionRegionState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal PartitionRegionState transition: $from -> $to"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Speculative-lease lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: SpeculativeLeaseState,
        to: SpeculativeLeaseState,
    ): Boolean {
        return when (from) {
            SpeculativeLeaseState.ISSUED -> to == SpeculativeLeaseState.RELEASED
            SpeculativeLeaseState.RELEASED -> false
        }
    }

    @JvmStatic
    fun requireTransition(
        from: SpeculativeLeaseState,
        to: SpeculativeLeaseState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal SpeculativeLeaseState transition: $from -> $to"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Cross-axis helpers
    //
    // These helpers do not replace per-axis transition legality.
    // They make cross-axis invariants explicit at runtime call sites.
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns true if the shared-slot state represents an authoritative shared terminal signal
     * that may lawfully back waiter convergence through RESUMED.
     *
     * Shared-slot terminal signal sources:
     * - SUCCESS
     * - FAILED
     * - DROPPED
     *
     * Non-source:
     * - PENDING
     */
    @JvmStatic
    fun isSharedTerminalSignalSource(
        sharedState: SharedSlotState,
    ): Boolean {
        return sharedState == SharedSlotState.SUCCESS ||
                sharedState == SharedSlotState.FAILED ||
                sharedState == SharedSlotState.DROPPED
    }

    /**
     * Returns true if the waiter terminalization is purely waiter-local and must not
     * mutate or imply mutation of the shared-slot terminal state.
     */
    @JvmStatic
    fun isWaiterLocalOnly(
        waiterTarget: WaiterState,
    ): Boolean {
        return waiterTarget == WaiterState.TIMED_OUT ||
                waiterTarget == WaiterState.CANCELLED
    }

    /**
     * Returns true if the waiter terminalization is caused by observation of an
     * authoritative shared terminal signal.
     */
    @JvmStatic
    fun isWaiterSharedSignalConvergence(
        waiterTarget: WaiterState,
    ): Boolean {
        return waiterTarget == WaiterState.RESUMED
    }

    /**
     * Cross-axis legality:
     * - ATTACHED is only meaningful while the shared slot is still pending
     * - RESUMED requires an authoritative shared terminal signal source
     * - TIMED_OUT and CANCELLED are waiter-local and therefore do not require
     *   the shared slot to be terminal
     */
    @JvmStatic
    fun canSharedStateBackWaiterTransition(
        sharedState: SharedSlotState,
        waiterTarget: WaiterState,
    ): Boolean {
        return when (waiterTarget) {
            WaiterState.ATTACHED ->
                sharedState == SharedSlotState.PENDING

            WaiterState.RESUMED ->
                isSharedTerminalSignalSource(sharedState)

            WaiterState.TIMED_OUT,
            WaiterState.CANCELLED ->
                true
        }
    }

    @JvmStatic
    fun requireSharedStateBacksWaiterTransition(
        sharedState: SharedSlotState,
        waiterTarget: WaiterState,
    ) {
        if (!canSharedStateBackWaiterTransition(sharedState, waiterTarget)) {
            throw PlanningProtocolIntegrityException(
                "Illegal cross-axis transition: sharedState=$sharedState cannot back waiterTarget=$waiterTarget"
            )
        }
    }

    /**
     * Explicit runtime-readable invariant:
     * waiter-local timeout/cancel must never be interpreted as shared-slot mutation.
     *
     * This function always returns false because the invariant is constitutional,
     * but naming it explicitly makes call sites and tests far less ambiguous.
     */
    @JvmStatic
    fun canWaiterTerminalizationMutateSharedSlot(
        waiterTarget: WaiterState,
    ): Boolean {
        return false
    }

    @JvmStatic
    fun requireWaiterTerminalizationNotMutateSharedSlot(
        waiterTarget: WaiterState,
    ) {
        if (canWaiterTerminalizationMutateSharedSlot(waiterTarget)) {
            throw PlanningProtocolIntegrityException(
                "Illegal waiter/shared coupling: waiterTarget=$waiterTarget must not mutate shared-slot state"
            )
        }
    }

    /**
     * Region-level admission rule for attach/build entry.
     *
     * CLOSE_PUBLISHED and RECLAIMED are both non-admitting states from the perspective
     * of fresh attach admission.
     */
    @JvmStatic
    fun canRegionAdmitFreshLifecycleWork(
        regionState: PartitionRegionState,
    ): Boolean {
        return regionState == PartitionRegionState.OPEN
    }

    @JvmStatic
    fun requireRegionOpenForFreshLifecycleWork(
        regionState: PartitionRegionState,
    ) {
        if (!canRegionAdmitFreshLifecycleWork(regionState)) {
            throw PlanningProtocolIntegrityException(
                "Region is not open for fresh lifecycle work: regionState=$regionState"
            )
        }
    }

    /**
     * Shared-slot admission rule for fresh waiter attach.
     */
    @JvmStatic
    fun canSharedSlotAdmitFreshAttach(
        sharedState: SharedSlotState,
    ): Boolean {
        return sharedState == SharedSlotState.PENDING
    }

    @JvmStatic
    fun requireSharedSlotPendingForFreshAttach(
        sharedState: SharedSlotState,
    ) {
        if (!canSharedSlotAdmitFreshAttach(sharedState)) {
            throw PlanningProtocolIntegrityException(
                "Shared slot is not pending for fresh attach: sharedState=$sharedState"
            )
        }
    }

    /**
     * Publication-entry rule:
     * - builder authority must still be OPEN
     * - commit-right must already be CLAIMED
     * - shared slot must still be PENDING
     *
     * This helper does not replace bucket-level exact re-verification or publication law.
     * It only makes the top-level preconditions explicit.
     */
    @JvmStatic
    fun canEnterAuthoritativePublication(
        builderState: BuilderHandleState,
        commitRightState: CommitRightState,
        sharedState: SharedSlotState,
    ): Boolean {
        return builderState == BuilderHandleState.OPEN &&
                commitRightState == CommitRightState.CLAIMED &&
                sharedState == SharedSlotState.PENDING
    }

    @JvmStatic
    fun requireAuthoritativePublicationEntry(
        builderState: BuilderHandleState,
        commitRightState: CommitRightState,
        sharedState: SharedSlotState,
    ) {
        if (!canEnterAuthoritativePublication(builderState, commitRightState, sharedState)) {
            throw PlanningProtocolIntegrityException(
                "Illegal publication entry: builderState=$builderState, commitRightState=$commitRightState, sharedState=$sharedState"
            )
        }
    }

    /**
     * Constitutional invariant:
     * waiter-local terminalization must never mutate shared-slot terminal state.
     *
     * This is intentionally represented as an explicit API even though the answer is
     * always false, because runtime code and tests should be able to call it directly
     * and express the invariant at the call site.
     */
    @JvmStatic
    fun canWaiterEventAffectSharedSlot(
        waiterEvent: WaiterState,
    ): Boolean {
        return false
    }

    /**
     * Preferred runtime enforcement surface.
     *
     * Use this at call sites such as:
     * - waiter timeout
     * - waiter cancellation
     *
     * The method name is phrased from the event perspective because these call sites
     * are event-driven in infrastructure code.
     */
    @JvmStatic
    fun requireWaiterEventDoesNotAffectSharedSlot(
        waiterEvent: WaiterState,
    ) {
        if (canWaiterEventAffectSharedSlot(waiterEvent)) {
            throw PlanningProtocolIntegrityException(
                "Illegal cross-axis coupling: waiterEvent=$waiterEvent must not mutate shared-slot state"
            )
        }
    }
}