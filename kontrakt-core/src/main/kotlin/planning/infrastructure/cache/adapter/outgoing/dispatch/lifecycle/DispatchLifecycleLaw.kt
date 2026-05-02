package planning.infrastructure.cache.adapter.outgoing.dispatch.lifecycle

import planning.domain.exception.PlanningProtocolIntegrityException

/**
 * Single source of truth for closed top-level lifecycle legality across the dispatch plane.
 *
 * Scope:
 * - delivery-entry lifecycle
 * - dispatch-lane lifecycle
 *
 * Architectural role:
 * - This object is the operational lifecycle law surface for adapter-owned joined-wait
 *   delivery mechanics.
 * - It is intentionally separate from:
 *   - SharedSlotState / WaiterState / BuilderHandleState / CommitRightState law,
 *   - reason taxonomy,
 *   - queue implementation details,
 *   - cache-line layout,
 *   - timer substrate choice,
 *   - and packed-word layout.
 *
 * Why this file exists:
 * - A state enum alone is not enough.
 * - Production code must not silently encode transition legality ad hoc inside multiple
 *   branch sites.
 * - Therefore this file provides both:
 *   - query-style legality checks, and
 *   - runtime enforcement surfaces via requireTransition(...).
 *
 * Design intent:
 * - keep top-level operational vocabularies closed,
 * - keep transition legality explicit,
 * - keep reclamation legality visible,
 * - and keep lane-stop/quiescence preconditions mechanically readable.
 *
 * This file is intentionally explicit rather than clever.
 * That is a feature.
 */
internal object DispatchLifecycleLaw {
    // ─────────────────────────────────────────────────────────────
    // Delivery-entry lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: DeliveryEntryState,
        to: DeliveryEntryState,
    ): Boolean =
        when (from) {
            DeliveryEntryState.EMPTY ->
                to == DeliveryEntryState.REGISTERED

            DeliveryEntryState.REGISTERED ->
                to == DeliveryEntryState.SIGNALED ||
                    to == DeliveryEntryState.ABANDONED

            DeliveryEntryState.SIGNALED ->
                to == DeliveryEntryState.QUEUED ||
                    to == DeliveryEntryState.ABANDONED

            DeliveryEntryState.QUEUED ->
                to == DeliveryEntryState.DELIVERING ||
                    to == DeliveryEntryState.ABANDONED

            DeliveryEntryState.DELIVERING ->
                to == DeliveryEntryState.DONE

            DeliveryEntryState.DONE ->
                to == DeliveryEntryState.EMPTY

            DeliveryEntryState.ABANDONED ->
                to == DeliveryEntryState.EMPTY
        }

    @JvmStatic
    fun requireTransition(
        from: DeliveryEntryState,
        to: DeliveryEntryState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal DeliveryEntryState transition: $from -> $to",
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Dispatch-lane lifecycle
    // ─────────────────────────────────────────────────────────────

    @JvmStatic
    fun canTransition(
        from: DispatchLaneState,
        to: DispatchLaneState,
    ): Boolean =
        when (from) {
            DispatchLaneState.OPEN ->
                to == DispatchLaneState.CLOSE_REQUESTED

            DispatchLaneState.CLOSE_REQUESTED ->
                to == DispatchLaneState.DRAINING

            DispatchLaneState.DRAINING ->
                to == DispatchLaneState.STOPPED

            DispatchLaneState.STOPPED ->
                false
        }

    @JvmStatic
    fun requireTransition(
        from: DispatchLaneState,
        to: DispatchLaneState,
    ) {
        if (!canTransition(from, to)) {
            throw PlanningProtocolIntegrityException(
                "Illegal DispatchLaneState transition: $from -> $to",
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Entry-axis helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Fresh continuation registration is lawful only from EMPTY.
     */
    @JvmStatic
    fun canAdmitFreshDeliveryRegistration(entryState: DeliveryEntryState): Boolean = entryState == DeliveryEntryState.EMPTY

    @JvmStatic
    fun requireEmptyForFreshDeliveryRegistration(entryState: DeliveryEntryState) {
        if (!canAdmitFreshDeliveryRegistration(entryState)) {
            throw PlanningProtocolIntegrityException(
                "Delivery entry is not empty for fresh registration: entryState=$entryState",
            )
        }
    }

    /**
     * Ready-queue ownership is lawful only after a delivery signal has become visible.
     */
    @JvmStatic
    fun canAcquireReadyQueueOwnership(entryState: DeliveryEntryState): Boolean = entryState == DeliveryEntryState.SIGNALED

    @JvmStatic
    fun requireSignaledForReadyQueueOwnership(entryState: DeliveryEntryState) {
        if (!canAcquireReadyQueueOwnership(entryState)) {
            throw PlanningProtocolIntegrityException(
                "Delivery entry is not signaled for ready-queue ownership: entryState=$entryState",
            )
        }
    }

    /**
     * Close-request abandonment is lawful only for entries that have not yet entered
     * callback execution.
     *
     * DELIVERING is intentionally excluded:
     * - once callback execution has begun, the lane must let that execution converge
     *   normally and then reclaim through DONE -> EMPTY.
     */
    @JvmStatic
    fun canBeCloseAbandoned(entryState: DeliveryEntryState): Boolean =
        entryState == DeliveryEntryState.REGISTERED ||
            entryState == DeliveryEntryState.SIGNALED ||
            entryState == DeliveryEntryState.QUEUED

    @JvmStatic
    fun requireCloseAbandonable(entryState: DeliveryEntryState) {
        if (!canBeCloseAbandoned(entryState)) {
            throw PlanningProtocolIntegrityException(
                "Delivery entry is not lawful for close-time abandonment: entryState=$entryState",
            )
        }
    }

    /**
     * Live operational entries are those that still contribute to quiescence debt.
     */
    @JvmStatic
    fun isLiveOperational(entryState: DeliveryEntryState): Boolean =
        entryState == DeliveryEntryState.REGISTERED ||
            entryState == DeliveryEntryState.SIGNALED ||
            entryState == DeliveryEntryState.QUEUED ||
            entryState == DeliveryEntryState.DELIVERING

    /**
     * Reclamation to EMPTY is lawful only after the current episode has terminalized.
     */
    @JvmStatic
    fun canReclaimToEmpty(entryState: DeliveryEntryState): Boolean =
        entryState == DeliveryEntryState.DONE ||
            entryState == DeliveryEntryState.ABANDONED

    @JvmStatic
    fun requireTerminalForReclaimToEmpty(entryState: DeliveryEntryState) {
        if (!canReclaimToEmpty(entryState)) {
            throw PlanningProtocolIntegrityException(
                "Delivery entry is not terminal for reclaim-to-empty: entryState=$entryState",
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Lane-axis helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Fresh registration admission is lawful only while the lane remains OPEN.
     */
    @JvmStatic
    fun canLaneAdmitFreshWork(laneState: DispatchLaneState): Boolean = laneState == DispatchLaneState.OPEN

    @JvmStatic
    fun requireLaneOpenForFreshWork(laneState: DispatchLaneState) {
        if (!canLaneAdmitFreshWork(laneState)) {
            throw PlanningProtocolIntegrityException(
                "Dispatch lane is not open for fresh work: laneState=$laneState",
            )
        }
    }

    /**
     * DRAINING may be entered only after CLOSE_REQUESTED has already been published.
     */
    @JvmStatic
    fun canLaneEnterDraining(laneState: DispatchLaneState): Boolean = laneState == DispatchLaneState.CLOSE_REQUESTED

    @JvmStatic
    fun requireLaneCloseRequestedForDraining(laneState: DispatchLaneState) {
        if (!canLaneEnterDraining(laneState)) {
            throw PlanningProtocolIntegrityException(
                "Dispatch lane is not in CLOSE_REQUESTED for DRAINING entry: laneState=$laneState",
            )
        }
    }

    /**
     * STOPPED is lawful only when the published operational debt is fully cleared.
     *
     * Parameters are intentionally passed in published/snapshot form rather than by
     * exposing lane-owned mutable arrays directly.
     */
    @JvmStatic
    fun canLanePublishStopped(
        laneState: DispatchLaneState,
        commandRingIsEmpty: Boolean,
        readyQueuePublishedSize: Int,
        activeCallbackCount: Int,
        liveOperationalEntryCount: Int,
        dirtyShardCount: Int,
    ): Boolean =
        laneState == DispatchLaneState.DRAINING &&
            commandRingIsEmpty &&
            readyQueuePublishedSize == 0 &&
            activeCallbackCount == 0 &&
            liveOperationalEntryCount == 0 &&
            dirtyShardCount == 0

    @JvmStatic
    fun requireLaneMayPublishStopped(
        laneState: DispatchLaneState,
        commandRingIsEmpty: Boolean,
        readyQueuePublishedSize: Int,
        activeCallbackCount: Int,
        liveOperationalEntryCount: Int,
        dirtyShardCount: Int,
    ) {
        if (
            !canLanePublishStopped(
                laneState = laneState,
                commandRingIsEmpty = commandRingIsEmpty,
                readyQueuePublishedSize = readyQueuePublishedSize,
                activeCallbackCount = activeCallbackCount,
                liveOperationalEntryCount = liveOperationalEntryCount,
                dirtyShardCount = dirtyShardCount,
            )
        ) {
            throw PlanningProtocolIntegrityException(
                "Dispatch lane may not publish STOPPED: " +
                    "laneState=$laneState, " +
                    "commandRingIsEmpty=$commandRingIsEmpty, " +
                    "readyQueuePublishedSize=$readyQueuePublishedSize, " +
                    "activeCallbackCount=$activeCallbackCount, " +
                    "liveOperationalEntryCount=$liveOperationalEntryCount, " +
                    "dirtyShardCount=$dirtyShardCount",
            )
        }
    }
}
