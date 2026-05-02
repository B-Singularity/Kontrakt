package planning.infrastructure.cache

import planning.domain.exception.PlanningProtocolIntegrityException
import planning.domain.runtime.lifecycle.CommitRightState
import planning.domain.runtime.lifecycle.L2LifecycleLaw
import planning.domain.runtime.lifecycle.SharedSlotState
import planning.domain.runtime.lifecycle.SpeculativeLeaseState
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle

/**
 * Object identity shell and explicit lifecycle host for one routed shared key in Planning L2.
 *
 * -----------------------------------------------------------------------------
 * ARCHITECTURAL POSITION
 * -----------------------------------------------------------------------------
 *
 * This class is the lifecycle host for one routed shared key.
 *
 * It owns:
 * - shared-slot top-level terminal truth,
 * - admitted attached waiter count,
 * - waiter registry root,
 * - builder-handle registry root,
 * - commit-right arbitration state,
 * - slot-owned speculative lease surface,
 * - terminal payload visibility,
 * - delivery-pending visibility,
 * - slot-local panic-isolation marking,
 * - and immutable generation-tagged identity for stale-reference defense.
 *
 * It does NOT own:
 * - region lifecycle truth,
 * - bucket publication linearization,
 * - adapter-owned delivery execution,
 * - worker-local planner primitive substrates,
 * - or governance reaction mapping.
 *
 * -----------------------------------------------------------------------------
 * OBJECT SHELL + PRIMITIVE AUTHORITY
 * -----------------------------------------------------------------------------
 *
 * This implementation intentionally combines:
 *
 * 1. object identity shell
 *    - stable asynchronous ownership boundary
 *    - no casual identity rebinding
 *    - explicit per-slot lifecycle hosting
 *
 * 2. primitive authority fields
 *    - slotWord        : shared-slot truth + admitted waiter count + hot flags
 *    - commitRightWord : publication arbitration truth
 *    - leaseWord       : slot-owned speculative lease-surface truth
 *
 * Semantic authority is carried by those primitive words.
 * Payload holders and registry linkage are auxiliary visibility surfaces only.
 *
 * -----------------------------------------------------------------------------
 * GENERATION LAW
 * -----------------------------------------------------------------------------
 *
 * `generation` is:
 * - immutable,
 * - factory-issued,
 * - shard-local in meaning,
 * - opaque,
 * - and NOT lifecycle truth.
 *
 * It exists only as an episode discriminator for:
 * - stale-reference defense,
 * - delayed callback neutralization,
 * - future grace-aware reclamation,
 * - and future delayed reuse policies.
 *
 * This class intentionally does not treat generation as:
 * - a timeout policy,
 * - a publication policy,
 * - a shard ordering clock,
 * - or a replacement for the slot authority words.
 *
 * -----------------------------------------------------------------------------
 * POLICY-NEUTRAL DECISION SURFACE
 * -----------------------------------------------------------------------------
 *
 * This class intentionally exposes only lifecycle-taxonomy decisions.
 *
 * It may say:
 * - shared success requires bucket re-verification
 * - shared failure terminal is visible
 * - shared drop terminal is visible
 * - publication entry is allowed or denied by slot-owned authority
 * - waiter attach is pending / cap-exhausted / already terminal
 *
 * It must not say:
 * - maps to TRANSIENT
 * - maps to CIRCUIT_OPEN
 * - bypass now
 * - retry now
 *
 * Governance reaction belongs to shard / governance layer, not to the slot.
 *
 * -----------------------------------------------------------------------------
 * RECLAMATION STANCE
 * -----------------------------------------------------------------------------
 *
 * This implementation assumes:
 * - no immediate pooling,
 * - no identity rebinding,
 * - and no reuse before grace-aware reclamation allows it.
 *
 * Correctness floor is prioritized over aggressive lifecycle-host reuse.
 */
internal class InFlightSlot<N : Any> private constructor(
    private val maxAttachedWaiters: Int,
    startedAtNanos: Long,
    generation: Long,
) {
    /**
     * Shared-slot authority surface.
     *
     * This field and only this field decides:
     * - shared-slot top-level state
     * - admitted attached waiter count
     * - hot flags for operational convergence
     */
    @Volatile
    private var slotWord: Long = encodeInitialSlotWord()

    /**
     * Operational root of the waiter registry.
     *
     * This is not semantic lifecycle authority.
     */
    @Volatile
    private var waiterHead: WaiterCell? = null

    /**
     * Winner-published shared-success payload.
     *
     * Not lifecycle authority.
     * Must be interpreted only together with slotWord.
     */
    @Volatile
    private var successNode: Any? = null

    /**
     * Winner-published shared terminal failure/drop payload.
     *
     * Not lifecycle authority.
     * Must be interpreted only together with slotWord.
     */
    @Volatile
    private var terminalFailure: Throwable? = null

    /**
     * Operational root of the builder-handle registry.
     *
     * This registry exists for supervision and slot-local scans.
     * It is not semantic lifecycle authority.
     */
    @Volatile
    private var builderHead: BuilderHandleCell? = null

    /**
     * Commit-right arbitration authority field.
     *
     * This field decides only who may enter authoritative publication.
     * It does not model builder progress.
     */
    @Volatile
    private var commitRightWord: Int = encodeInitialCommitRightWord()

    /**
     * Slot-local speculative lease-surface authority field.
     *
     * Current representation is intentionally minimal:
     * - one top-level ISSUED / RELEASED surface only
     * - no lease count packing
     * - no quota packing
     */
    @Volatile
    private var leaseWord: Int = encodeInitialLeaseWord()

    /**
     * Immutable monotonic start time for this slot episode.
     *
     * Operational metadata only.
     */
    private val startedAtNanos: Long = startedAtNanos

    /**
     * Immutable generation tag for stale-reference defense and future
     * grace-aware reclamation / delayed reuse policies.
     *
     * This field is not lifecycle authority.
     * It is an episode discriminator only.
     */
    private val generation: Long = generation

    // -------------------------------------------------------------------------
    // Shared-slot reads
    // -------------------------------------------------------------------------

    fun readSharedStateAcquire(): SharedSlotState {
        val word = SLOT_WORD_HANDLE.getAcquire(this) as Long
        return decodeSharedState(word)
    }

    fun readAttachedWaiterCountAcquire(): Int {
        val word = SLOT_WORD_HANDLE.getAcquire(this) as Long
        return decodeAttachedWaiterCount(word)
    }

    fun readStartedAtNanos(): Long = startedAtNanos

    fun readGeneration(): Long = generation

    fun isTerminalAcquire(): Boolean = readSharedStateAcquire().isTerminal

    /**
     * Returns true if the slot's auxiliary sealing flag is already visible.
     *
     * This is an operational mirror of terminal sealing, not a replacement
     * for the semantic shared-state code.
     */
    fun isFrozenAcquire(): Boolean {
        val word = SLOT_WORD_HANDLE.getAcquire(this) as Long
        return hasFlag(word, FLAG_FROZEN)
    }

    fun isPanicIsolatedAcquire(): Boolean {
        val word = SLOT_WORD_HANDLE.getAcquire(this) as Long
        return hasFlag(word, FLAG_PANIC_ISOLATED)
    }

    fun isDeliveryPendingAcquire(): Boolean {
        val word = SLOT_WORD_HANDLE.getAcquire(this) as Long
        return hasFlag(word, FLAG_DELIVERY_PENDING)
    }

    fun isCompletionVisibleAcquire(): Boolean {
        val word = SLOT_WORD_HANDLE.getAcquire(this) as Long
        return hasFlag(word, FLAG_COMPLETION_VISIBLE)
    }

    @Suppress("UNCHECKED_CAST")
    fun readSuccessNodeAcquire(): N? {
        val state = readSharedStateAcquire()
        if (state != SharedSlotState.SUCCESS) {
            return null
        }
        return SUCCESS_NODE_HANDLE.getAcquire(this) as N?
    }

    fun readTerminalFailureAcquire(): Throwable? =
        when (readSharedStateAcquire()) {
            SharedSlotState.FAILED,
            SharedSlotState.DROPPED,
            -> TERMINAL_FAILURE_HANDLE.getAcquire(this) as Throwable?

            SharedSlotState.PENDING,
            SharedSlotState.SUCCESS,
            -> null
        }

    fun readCommitRightAcquire(): CommitRightState {
        val word = COMMIT_RIGHT_WORD_HANDLE.getAcquire(this) as Int
        return decodeCommitRightState(word)
    }

    fun readLeaseStateAcquire(): SpeculativeLeaseState {
        val word = LEASE_WORD_HANDLE.getAcquire(this) as Int
        return decodeLeaseState(word)
    }

    // -------------------------------------------------------------------------
    // Closed decision surface
    // -------------------------------------------------------------------------

    /**
     * Resolves the currently visible shared terminal taxonomy, if any.
     *
     * This surface is intentionally policy-neutral.
     * Shard/governance code must map this resolution to domain/public reactions.
     */
    fun resolveSharedTerminalAcquire(): SharedTerminalResolution? =
        when (readSharedStateAcquire()) {
            SharedSlotState.PENDING -> null
            SharedSlotState.SUCCESS -> SharedTerminalResolution.SuccessRequiresBucketReverification
            SharedSlotState.FAILED -> SharedTerminalResolution.SharedFailureTerminal
            SharedSlotState.DROPPED -> SharedTerminalResolution.SharedDropTerminal
        }

    /**
     * Decides whether authoritative publication entry is currently lawful from the
     * slot-owned authority surface.
     *
     * Builder-handle state is intentionally not consulted here because builder-handle
     * convergence belongs to BuilderHandleCell, not to slot authority.
     */
    fun decidePublicationEntryAcquire(): PublicationEntryDecision {
        val commitRightState = readCommitRightAcquire()
        if (commitRightState != CommitRightState.CLAIMED) {
            return PublicationEntryDecision.CommitRightNotClaimed
        }

        val sharedState = readSharedStateAcquire()
        if (sharedState != SharedSlotState.PENDING) {
            return PublicationEntryDecision.SlotNotPending
        }

        return PublicationEntryDecision.Allowed
    }

    // -------------------------------------------------------------------------
    // Waiter admission
    // -------------------------------------------------------------------------

    /**
     * Attempts fresh waiter admission against this slot.
     *
     * Reservation sequence:
     * 1. read slot authority
     * 2. reject unless shared state is PENDING
     * 3. reject if waiter cap reached
     * 4. CAS-reserve admitted waiter count in slotWord
     * 5. install waiter into operational registry
     * 6. re-read shared state and return a reconciliation decision
     *
     * Post-insertion reconciliation exists because slot terminalization may win after
     * attach reservation but before or during registry linkage.
     *
     * Flag preservation note:
     * - attach reservation preserves currently visible orthogonal hot flags
     * - the attach path does not author those flags itself
     * - by construction, FLAG_FROZEN is only set together with terminalization CAS,
     *   so a lawful PENDING slot is not expected to appear as PENDING + FROZEN
     */
    fun tryAttachWaiter(waiter: WaiterCell): JoinAdmissionDecision {
        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            val observedState = decodeSharedState(observed)

            if (observedState != SharedSlotState.PENDING) {
                return JoinAdmissionDecision.AlreadyTerminal(
                    resolution = resolveTerminalFromState(observedState),
                )
            }

            val currentCount = decodeAttachedWaiterCount(observed)
            if (currentCount >= maxAttachedWaiters || currentCount >= MAX_PACKED_ATTACHED_WAITERS) {
                return JoinAdmissionDecision.WaiterCapExceeded
            }

            val updated =
                encodeSlotWordUnchecked(
                    state = SharedSlotState.PENDING,
                    attachedWaiterCount = currentCount + 1,
                    flags = extractFlags(observed),
                )

            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                installWaiter(waiter)

                val afterInstall = SLOT_WORD_HANDLE.getAcquire(this) as Long
                val afterState = decodeSharedState(afterInstall)

                return if (afterState == SharedSlotState.PENDING) {
                    JoinAdmissionDecision.AttachedPending
                } else {
                    JoinAdmissionDecision.AlreadyTerminal(
                        resolution = resolveTerminalFromState(afterState),
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Builder-handle registry
    // -------------------------------------------------------------------------

    /**
     * Registers a builder-handle episode with this slot.
     *
     * Builder linkage and commit-right arbitration remain distinct:
     * - a builder may exist without winning publication
     * - commit-right is the exact publication gate
     *
     * If terminal state becomes visible after registration but before reconciliation,
     * the caller must immediately converge the handle lawfully.
     */
    fun registerBuilderHandle(handle: BuilderHandleCell): BuilderHandleRegisterDecision {
        val currentResolution = resolveSharedTerminalAcquire()
        if (currentResolution != null) {
            return BuilderHandleRegisterDecision.RejectedSlotTerminal(currentResolution)
        }

        while (true) {
            val observed = BUILDER_HEAD_HANDLE.getAcquire(this) as BuilderHandleCell?
            handle.next = observed
            if (BUILDER_HEAD_HANDLE.compareAndSet(this, observed, handle)) {
                val afterResolution = resolveSharedTerminalAcquire()
                return if (afterResolution == null) {
                    BuilderHandleRegisterDecision.RegisteredPending
                } else {
                    BuilderHandleRegisterDecision.RejectedSlotTerminal(afterResolution)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Commit-right arbitration
    // -------------------------------------------------------------------------

    /**
     * Attempts to claim authoritative publication entry.
     *
     * Build permission and publication permission are distinct.
     * Multiple builders may exist, but only one contender may win commit-right.
     */
    fun tryClaimCommitRight(): Boolean {
        L2LifecycleLaw.requireTransition(
            CommitRightState.UNCLAIMED,
            CommitRightState.CLAIMED,
        )

        return COMMIT_RIGHT_WORD_HANDLE.compareAndSet(
            this,
            CommitRightState.UNCLAIMED.code,
            CommitRightState.CLAIMED.code,
        )
    }

    /**
     * Releases commit-right after the current publication episode converged.
     *
     * This does not reopen publication. It closes the arbitration surface for
     * the current slot episode.
     */
    fun tryReleaseCommitRight(): Boolean {
        L2LifecycleLaw.requireTransition(
            CommitRightState.CLAIMED,
            CommitRightState.RELEASED,
        )

        return COMMIT_RIGHT_WORD_HANDLE.compareAndSet(
            this,
            CommitRightState.CLAIMED.code,
            CommitRightState.RELEASED.code,
        )
    }

    // -------------------------------------------------------------------------
    // Slot-owned speculative lease surface
    // -------------------------------------------------------------------------

    /**
     * Attempts to begin a new speculative lease episode on this slot-local lease surface.
     */
    fun tryIssueSpeculativeLease(): Boolean =
        LEASE_WORD_HANDLE.compareAndSet(
            this,
            SpeculativeLeaseState.RELEASED.code,
            SpeculativeLeaseState.ISSUED.code,
        )

    /**
     * Attempts to release a currently live speculative lease episode.
     */
    fun tryReleaseSpeculativeLease(): Boolean {
        L2LifecycleLaw.requireTransition(
            SpeculativeLeaseState.ISSUED,
            SpeculativeLeaseState.RELEASED,
        )

        return LEASE_WORD_HANDLE.compareAndSet(
            this,
            SpeculativeLeaseState.ISSUED.code,
            SpeculativeLeaseState.RELEASED.code,
        )
    }

    // -------------------------------------------------------------------------
    // Shared terminalization
    // -------------------------------------------------------------------------

    /**
     * Attempts shared success publication visibility for this slot.
     *
     * Precondition:
     * - commit-right must already be CLAIMED before publication entry
     *
     * Ordering:
     * 1. publish success payload with release semantics
     * 2. CAS shared slot from PENDING to SUCCESS
     * 3. mark completion-visible / delivery-pending flags
     * 4. force best-effort lease release
     *
     * This preserves publication-before-completion law.
     */
    fun tryPublishSuccess(
        publishedWinner: N,
        markDeliveryPending: Boolean = true,
    ): Boolean {
        if (readCommitRightAcquire() != CommitRightState.CLAIMED) {
            throw PlanningProtocolIntegrityException(
                "InFlightSlot.tryPublishSuccess requires commit-right to be CLAIMED before publication entry.",
            )
        }

        L2LifecycleLaw.requireTransition(
            SharedSlotState.PENDING,
            SharedSlotState.SUCCESS,
        )

        SUCCESS_NODE_HANDLE.setRelease(this, publishedWinner)

        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            if (decodeSharedState(observed) != SharedSlotState.PENDING) {
                return false
            }

            var flags = extractFlags(observed)
            flags = flags or FLAG_FROZEN or FLAG_COMPLETION_VISIBLE
            if (markDeliveryPending) {
                flags = flags or FLAG_DELIVERY_PENDING
            }

            val updated =
                encodeSlotWordUnchecked(
                    state = SharedSlotState.SUCCESS,
                    attachedWaiterCount = decodeAttachedWaiterCount(observed),
                    flags = flags,
                )

            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                forceReleaseSpeculativeLeaseBestEffort()
                return true
            }
        }
    }

    /**
     * Attempts shared failure terminalization for this slot.
     *
     * Failure payload is published first, then shared-slot truth transitions to FAILED.
     */
    fun tryFailShared(
        cause: Throwable,
        markDeliveryPending: Boolean = true,
    ): Boolean {
        L2LifecycleLaw.requireTransition(
            SharedSlotState.PENDING,
            SharedSlotState.FAILED,
        )

        TERMINAL_FAILURE_HANDLE.setRelease(this, cause)

        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            if (decodeSharedState(observed) != SharedSlotState.PENDING) {
                return false
            }

            var flags = extractFlags(observed)
            flags = flags or FLAG_FROZEN or FLAG_COMPLETION_VISIBLE
            if (markDeliveryPending) {
                flags = flags or FLAG_DELIVERY_PENDING
            }

            val updated =
                encodeSlotWordUnchecked(
                    state = SharedSlotState.FAILED,
                    attachedWaiterCount = decodeAttachedWaiterCount(observed),
                    flags = flags,
                )

            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                forceReleaseSpeculativeLeaseBestEffort()
                return true
            }
        }
    }

    /**
     * Attempts region-driven drop terminalization for this slot.
     *
     * Drop payload is published first, then shared-slot truth transitions to DROPPED.
     */
    fun tryDropShared(
        cause: Throwable,
        markDeliveryPending: Boolean = true,
    ): Boolean {
        L2LifecycleLaw.requireTransition(
            SharedSlotState.PENDING,
            SharedSlotState.DROPPED,
        )

        TERMINAL_FAILURE_HANDLE.setRelease(this, cause)

        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            if (decodeSharedState(observed) != SharedSlotState.PENDING) {
                return false
            }

            var flags = extractFlags(observed)
            flags = flags or FLAG_FROZEN or FLAG_COMPLETION_VISIBLE
            if (markDeliveryPending) {
                flags = flags or FLAG_DELIVERY_PENDING
            }

            val updated =
                encodeSlotWordUnchecked(
                    state = SharedSlotState.DROPPED,
                    attachedWaiterCount = decodeAttachedWaiterCount(observed),
                    flags = flags,
                )

            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                forceReleaseSpeculativeLeaseBestEffort()
                return true
            }
        }
    }

    // -------------------------------------------------------------------------
    // Panic / delivery flags
    // -------------------------------------------------------------------------

    /**
     * Marks this slot as panic-isolated.
     *
     * Panic isolation is orthogonal to the top-level shared state and therefore
     * updates only a hot flag, not the shared-state code.
     */
    fun markPanicIsolated(): Boolean {
        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            if (hasFlag(observed, FLAG_PANIC_ISOLATED)) {
                return false
            }

            val updated = observed or FLAG_PANIC_ISOLATED
            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    /**
     * Marks terminal delivery as pending.
     *
     * This is an operational convergence flag only.
     * It must never redefine shared semantic truth.
     */
    fun markDeliveryPending(): Boolean {
        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            if (hasFlag(observed, FLAG_DELIVERY_PENDING)) {
                return false
            }

            val updated = observed or FLAG_DELIVERY_PENDING
            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    /**
     * Clears the delivery-pending flag after delivery convergence.
     *
     * This is legal even after terminal semantic sealing because only an
     * operational convergence flag is being cleared.
     */
    fun clearDeliveryPending(): Boolean {
        while (true) {
            val observed = SLOT_WORD_HANDLE.getAcquire(this) as Long
            if (!hasFlag(observed, FLAG_DELIVERY_PENDING)) {
                return false
            }

            val updated = observed and FLAG_DELIVERY_PENDING.inv()
            if (SLOT_WORD_HANDLE.compareAndSet(this, observed, updated)) {
                return true
            }
        }
    }

    // -------------------------------------------------------------------------
    // Best-effort operational traversal
    // -------------------------------------------------------------------------

    fun forEachVisibleWaiter(action: (WaiterCell) -> Unit) {
        var cursor = WAITER_HEAD_HANDLE.getAcquire(this) as WaiterCell?
        while (cursor != null) {
            action(cursor)
            cursor = cursor.next
        }
    }

    fun forEachVisibleBuilderHandle(action: (BuilderHandleCell) -> Unit) {
        var cursor = BUILDER_HEAD_HANDLE.getAcquire(this) as BuilderHandleCell?
        while (cursor != null) {
            action(cursor)
            cursor = cursor.next
        }
    }

    // -------------------------------------------------------------------------
    // Internal mechanics
    // -------------------------------------------------------------------------

    private fun installWaiter(waiter: WaiterCell) {
        while (true) {
            val observed = WAITER_HEAD_HANDLE.getAcquire(this) as WaiterCell?
            waiter.next = observed
            if (WAITER_HEAD_HANDLE.compareAndSet(this, observed, waiter)) {
                return
            }
        }
    }

    /**
     * Best-effort lease release on shared terminalization.
     *
     * Lease lifecycle remains orthogonal, so this does not change shared semantic truth.
     * It only ensures that a slot-owned outstanding lease does not remain live after the
     * slot itself has terminalized.
     */
    private fun forceReleaseSpeculativeLeaseBestEffort() {
        while (true) {
            val observed = LEASE_WORD_HANDLE.getAcquire(this) as Int
            if (observed == SpeculativeLeaseState.RELEASED.code) {
                return
            }

            if (
                LEASE_WORD_HANDLE.compareAndSet(
                    this,
                    SpeculativeLeaseState.ISSUED.code,
                    SpeculativeLeaseState.RELEASED.code,
                )
            ) {
                return
            }
        }
    }

    private fun resolveTerminalFromState(state: SharedSlotState): SharedTerminalResolution =
        when (state) {
            SharedSlotState.SUCCESS -> SharedTerminalResolution.SuccessRequiresBucketReverification
            SharedSlotState.FAILED -> SharedTerminalResolution.SharedFailureTerminal
            SharedSlotState.DROPPED -> SharedTerminalResolution.SharedDropTerminal
            SharedSlotState.PENDING -> throw PlanningProtocolIntegrityException(
                "Cannot resolve terminal taxonomy from PENDING shared-slot state.",
            )
        }

    companion object {
        private const val STATE_BITS: Int = 3
        private const val STATE_MASK: Long = (1L shl STATE_BITS) - 1L

        private const val COUNT_SHIFT: Int = 3
        private const val COUNT_BITS: Int = 21
        private const val COUNT_MASK: Long = ((1L shl COUNT_BITS) - 1L) shl COUNT_SHIFT
        private const val MAX_PACKED_ATTACHED_WAITERS: Int = (1 shl COUNT_BITS) - 1

        private const val FLAG_FROZEN: Long = 1L shl 24
        private const val FLAG_PANIC_ISOLATED: Long = 1L shl 25
        private const val FLAG_COMPLETION_VISIBLE: Long = 1L shl 26
        private const val FLAG_DELIVERY_PENDING: Long = 1L shl 27

        private const val FLAGS_MASK: Long =
            FLAG_FROZEN or FLAG_PANIC_ISOLATED or FLAG_COMPLETION_VISIBLE or FLAG_DELIVERY_PENDING

        private val LOOKUP = MethodHandles.lookup()

        private val SLOT_WORD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "slotWord",
                Long::class.javaPrimitiveType,
            )

        private val WAITER_HEAD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "waiterHead",
                WaiterCell::class.java,
            )

        private val SUCCESS_NODE_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "successNode",
                Any::class.java,
            )

        private val TERMINAL_FAILURE_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "terminalFailure",
                Throwable::class.java,
            )

        private val BUILDER_HEAD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "builderHead",
                BuilderHandleCell::class.java,
            )

        private val COMMIT_RIGHT_WORD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "commitRightWord",
                Int::class.javaPrimitiveType,
            )

        private val LEASE_WORD_HANDLE: VarHandle =
            LOOKUP.findVarHandle(
                InFlightSlot::class.java,
                "leaseWord",
                Int::class.javaPrimitiveType,
            )

        @JvmStatic
        fun <N : Any> issue(
            maxAttachedWaiters: Int,
            startedAtNanos: Long,
            generation: Long,
        ): InFlightSlot<N> {
            if (maxAttachedWaiters <= 0) {
                throw PlanningProtocolIntegrityException(
                    "InFlightSlot.maxAttachedWaiters must be > 0: $maxAttachedWaiters",
                )
            }
            if (maxAttachedWaiters > MAX_PACKED_ATTACHED_WAITERS) {
                throw PlanningProtocolIntegrityException(
                    "InFlightSlot.maxAttachedWaiters exceeds packed capacity: requested=$maxAttachedWaiters, max=$MAX_PACKED_ATTACHED_WAITERS",
                )
            }
            if (startedAtNanos < 0L) {
                throw PlanningProtocolIntegrityException(
                    "InFlightSlot.startedAtNanos must be >= 0: $startedAtNanos",
                )
            }
            if (generation <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "InFlightSlot.generation must be > 0: $generation",
                )
            }

            return InFlightSlot(
                maxAttachedWaiters = maxAttachedWaiters,
                startedAtNanos = startedAtNanos,
                generation = generation,
            )
        }

        private fun encodeInitialSlotWord(): Long =
            encodeSlotWordUnchecked(
                state = SharedSlotState.PENDING,
                attachedWaiterCount = 0,
                flags = 0L,
            )

        private fun encodeInitialCommitRightWord(): Int = CommitRightState.UNCLAIMED.code

        private fun encodeInitialLeaseWord(): Int = SpeculativeLeaseState.RELEASED.code

        private fun encodeSlotWordUnchecked(
            state: SharedSlotState,
            attachedWaiterCount: Int,
            flags: Long,
        ): Long {
            val stateBits = state.code.toLong()
            val countBits = attachedWaiterCount.toLong() shl COUNT_SHIFT
            val flagBits = flags and FLAGS_MASK
            return stateBits or countBits or flagBits
        }

        private fun decodeSharedState(word: Long): SharedSlotState = SharedSlotState.fromCode((word and STATE_MASK).toInt())

        private fun decodeAttachedWaiterCount(word: Long): Int = ((word and COUNT_MASK) ushr COUNT_SHIFT).toInt()

        private fun extractFlags(word: Long): Long = word and FLAGS_MASK

        private fun hasFlag(
            word: Long,
            flag: Long,
        ): Boolean = (word and flag) != 0L

        private fun decodeCommitRightState(word: Int): CommitRightState = CommitRightState.fromCode(word)

        private fun decodeLeaseState(word: Int): SpeculativeLeaseState = SpeculativeLeaseState.fromCode(word)
    }
}

/**
 * Closed attach-admission surface emitted by InFlightSlot.
 *
 * This surface is intentionally policy-neutral.
 * Governance reaction remains shard-owned.
 */
sealed interface JoinAdmissionDecision {
    data object AttachedPending : JoinAdmissionDecision

    data object WaiterCapExceeded : JoinAdmissionDecision

    class AlreadyTerminal(
        val resolution: SharedTerminalResolution,
    ) : JoinAdmissionDecision
}

/**
 * Closed shared terminal-resolution surface emitted by InFlightSlot.
 *
 * This is lifecycle taxonomy only.
 * It must not embed fault policy.
 */
sealed interface SharedTerminalResolution {
    data object SuccessRequiresBucketReverification : SharedTerminalResolution

    data object SharedFailureTerminal : SharedTerminalResolution

    data object SharedDropTerminal : SharedTerminalResolution
}

/**
 * Closed slot-side publication-entry decision.
 *
 * Builder-handle state is intentionally excluded because it belongs to a distinct
 * builder-handle lifecycle axis.
 */
sealed interface PublicationEntryDecision {
    data object Allowed : PublicationEntryDecision

    data object CommitRightNotClaimed : PublicationEntryDecision

    data object SlotNotPending : PublicationEntryDecision
}

/**
 * Closed builder-handle linkage decision emitted by InFlightSlot.
 *
 * This surface answers only whether the slot may still be treated as pending for
 * builder progress purposes.
 */
sealed interface BuilderHandleRegisterDecision {
    data object RegisteredPending : BuilderHandleRegisterDecision

    class RejectedSlotTerminal(
        val resolution: SharedTerminalResolution,
    ) : BuilderHandleRegisterDecision
}
