package realization.runtime.dispatch

import migration.quarantine.JoinContinuation
import migration.quarantine.JoinRegistrationDecision
import migration.quarantine.JoinResumeSignal
import migration.quarantine.ResolvedDispatchLanePolicy
import realization.cache.interner.join.WaiterCell
import realization.cache.storage.InFlightSlot
import realization.planning.diagnostics.PlanningInfrastructureException
import realization.runtime.time.MonotonicTimeSource
import stage.lowering.material.candidate.CanonicalPlanNode
import statemachine.state.material.condition.DeliveryEntryState
import statemachine.state.material.condition.DispatchLaneState
import statemachine.transition.contract.DispatchLifecycleLaw
import java.util.Arrays
import java.util.IdentityHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicLongArray
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicReferenceArray
import java.util.concurrent.locks.LockSupport

/**
 * Adapter-owned delivery plane for Tier-2 joined-wait completion.
 *
 * Architectural role:
 * - continuation registration owner
 * - terminal sweep owner
 * - timeout cleanup owner
 * - quiescence accounting owner
 *
 * This is adapter-internal infrastructure.
 * It is intentionally not a Domain Core port.
 */
internal interface L2JoinDispatchPlane : AutoCloseable {
    /**
     * Registers a continuation for one already-attached waiter episode.
     *
     * Return algebra:
     * - Registered   -> lane-owned entry accepted
     * - AlreadyReady -> shared terminal truth already visible; no entry created
     */
    fun registerOrDeliverImmediate(
        shardIndex: Int,
        slot: InFlightSlot<CanonicalPlanNode>,
        waiter: WaiterCell,
        continuation: JoinContinuation,
        deadlineNanos: Long,
    ): JoinRegistrationDecision

    /**
     * Enqueues terminal sweep work for one routed shared slot.
     *
     * The producer never invokes callbacks directly.
     */
    fun enqueueTerminalSweep(
        shardIndex: Int,
        slot: InFlightSlot<CanonicalPlanNode>,
    )

    /**
     * Enqueues waiter-local cancellation cleanup.
     *
     * Returns false only when the lane command ring is already saturated or closing.
     */
    fun enqueueCancellation(
        shardIndex: Int,
        waiter: WaiterCell,
    ): Boolean

    /**
     * Waits until all lanes become quiescent or the grace expires.
     *
     * This is an administrative wait path, not a joiner wait path.
     * It must therefore rely only on published lane state and must not read
     * lane-owned mutable arrays directly.
     */
    fun awaitQuiescence(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean

    override fun close()
}

/**
 * Deterministic M:N balanced lane implementation.
 *
 * Design alignment:
 * - fixed lane count from immutable runtime-policy snapshot
 * - static shard-to-lane affinity
 * - lane-owned single-writer mutable state
 * - bounded MPSC command ring
 * - bounded ready queue
 * - bounded deadline heap
 * - shard-affine dirty replay
 * - no global CHM
 * - no blocking queue
 * - no lock-based command draining
 *
 * Important current design choices:
 * - dispatch plane now uses explicit operational state machines
 * - entry axis and lane axis are distinct
 * - operational quiescence is judged only from published snapshots
 * - lane-local mutable counters are plain fields and are published in batches
 */
internal class DeterministicL2JoinDispatchPlane private constructor(
    private val policy: ResolvedDispatchLanePolicy,
    private val shardCount: Int,
    private val timeSource: MonotonicTimeSource,
) : L2JoinDispatchPlane {
    private val closing = AtomicBoolean(false)
    private val deliverySeq = AtomicLong(0L)

    /**
     * A single administrative waiter is sufficient because the adapter serializes
     * administrative operations.
     */
    private val quiescenceWaiter = AtomicReference<Thread?>(null)

    private val effectiveLaneCount: Int = minOf(policy.laneCount, shardCount)

    private val lanes: Array<DispatchLane> =
        Array(effectiveLaneCount) { laneIndex ->
            DispatchLane(
                laneIndex = laneIndex,
                effectiveLaneCount = effectiveLaneCount,
                shardCount = shardCount,
                policy = policy,
                timeSource = timeSource,
                onPublishedStateChange = ::signalQuiescenceChange,
            )
        }

    init {
        for (lane in lanes) {
            lane.start()
        }
    }

    override fun registerOrDeliverImmediate(
        shardIndex: Int,
        slot: InFlightSlot<CanonicalPlanNode>,
        waiter: WaiterCell,
        continuation: JoinContinuation,
        deadlineNanos: Long,
    ): JoinRegistrationDecision {
        val resolution = slot.resolveSharedTerminalAcquire()
        if (resolution != null) {
            return JoinRegistrationDecision.AlreadyReady
        }

        val lane = laneForShard(shardIndex)
        val deliveryKey = issueDeliveryKey(lane.laneIndex)

        val offered =
            lane.offerCommand(
                LaneCommand.Register(
                    deliveryKey = deliveryKey,
                    shardIndex = shardIndex,
                    slot = slot,
                    waiter = waiter,
                    continuation = continuation,
                    deadlineNanos = deadlineNanos,
                ),
            )
        if (!offered) {
            throw DispatchLaneSaturatedException(
                "Dispatch registration ring is saturated for lane=${lane.laneIndex}.",
            )
        }

        return JoinRegistrationDecision.Registered
    }

    override fun enqueueTerminalSweep(
        shardIndex: Int,
        slot: InFlightSlot<CanonicalPlanNode>,
    ) {
        val lane = laneForShard(shardIndex)

        val offered =
            lane.offerCommand(
                LaneCommand.SlotTerminalVisible(
                    shardIndex = shardIndex,
                    slot = slot,
                ),
            )

        if (!offered) {
            lane.markDirtyShard(shardIndex)
            lane.signalWorker()
        }
    }

    override fun enqueueCancellation(
        shardIndex: Int,
        waiter: WaiterCell,
    ): Boolean {
        val lane = laneForShard(shardIndex)
        return lane.offerCommand(
            LaneCommand.Cancel(
                shardIndex = shardIndex,
                waiter = waiter,
            ),
        )
    }

    override fun awaitQuiescence(
        timeout: Long,
        unit: TimeUnit,
    ): Boolean {
        val current = Thread.currentThread()
        if (!quiescenceWaiter.compareAndSet(null, current)) {
            if (quiescenceWaiter.get() !== current) {
                throw PlanningInfrastructureException(
                    "Concurrent awaitQuiescence callers are forbidden.",
                )
            }
        }

        try {
            val timeoutNanos = unit.toNanos(timeout)
            if (timeoutNanos <= 0L) {
                return lanes.all { it.isQuiescentPublished() }
            }

            val deadlineNanos = timeSource.nowNanos() + timeoutNanos

            while (true) {
                if (lanes.all { it.isQuiescentPublished() }) {
                    return true
                }

                val remaining = deadlineNanos - timeSource.nowNanos()
                if (remaining <= 0L) {
                    return lanes.all { it.isQuiescentPublished() }
                }

                LockSupport.parkNanos(this, remaining)

                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt()
                    return lanes.all { it.isQuiescentPublished() }
                }
            }
        } finally {
            quiescenceWaiter.compareAndSet(current, null)
        }
    }

    override fun close() {
        if (!closing.compareAndSet(false, true)) {
            return
        }

        for (lane in lanes) {
            lane.requestClose()
        }

        val joinDeadline = timeSource.nowNanos() + policy.adapterCloseQuiescenceTimeoutNanos
        for (lane in lanes) {
            val remaining = (joinDeadline - timeSource.nowNanos()).coerceAtLeast(0L)
            lane.joinUntilStopped(remaining)
        }

        if (lanes.any { !it.isStoppedPublished() }) {
            throw PlanningInfrastructureException(
                "DeterministicL2JoinDispatchPlane.close() failed to stop all lanes within the bounded close grace.",
            )
        }
    }

    private fun laneForShard(shardIndex: Int): DispatchLane {
        val laneIndex = shardIndex and (effectiveLaneCount - 1)
        return lanes[laneIndex]
    }

    private fun issueDeliveryKey(laneIndex: Int): Long {
        val seq = deliverySeq.incrementAndGet()
        if (seq <= 0L) {
            throw PlanningInfrastructureException(
                "Dispatch delivery-key sequence overflowed the positive Long domain.",
            )
        }

        if (seq > 0x00FF_FFFF_FFFF_FFFFL) {
            throw PlanningInfrastructureException(
                "Dispatch delivery-key sequence exceeded the 56-bit payload domain.",
            )
        }

        return (laneIndex.toLong() shl 56) or seq
    }

    private fun signalQuiescenceChange() {
        quiescenceWaiter.get()?.let(LockSupport::unpark)
    }

    companion object {
        @JvmStatic
        internal fun issue(
            policy: ResolvedDispatchLanePolicy,
            shardCount: Int,
            timeSource: MonotonicTimeSource,
        ): DeterministicL2JoinDispatchPlane {
            if (shardCount <= 0 || shardCount.countOneBits() != 1) {
                throw PlanningInfrastructureException(
                    "DeterministicL2JoinDispatchPlane.shardCount must be a positive power-of-two: $shardCount",
                )
            }

            val effectiveLaneCount = minOf(policy.laneCount, shardCount)
            if (effectiveLaneCount <= 0 || effectiveLaneCount.countOneBits() != 1) {
                throw PlanningInfrastructureException(
                    "DeterministicL2JoinDispatchPlane.effectiveLaneCount must be a positive power-of-two: $effectiveLaneCount",
                )
            }

            return DeterministicL2JoinDispatchPlane(
                policy = policy,
                shardCount = shardCount,
                timeSource = timeSource,
            )
        }
    }

    // =========================================================================
    // Lane
    // =========================================================================

    private class DispatchLane(
        val laneIndex: Int,
        effectiveLaneCount: Int,
        shardCount: Int,
        private val policy: ResolvedDispatchLanePolicy,
        private val timeSource: MonotonicTimeSource,
        private val onPublishedStateChange: () -> Unit,
    ) {
        private val closingRequested = AtomicBoolean(false)
        private val worker =
            Thread(::runLoop, "planning-l2-lane-$laneIndex").apply {
                isDaemon = true
            }

        private val commandRing = LaneCommandRing(policy.commandRingCapacity)
        private val readyQueue = LaneReadyQueue(policy.readyQueueCapacity)
        private val dirtyShards = DirtyShardBitmap(shardCount / effectiveLaneCount)
        private val registrationStore =
            LaneRegistrationStore(
                laneIndex = laneIndex,
                effectiveLaneCount = effectiveLaneCount,
                shardCount = shardCount,
                registrationStoreCapacityPerShard = policy.registrationStoreCapacityPerShard,
            )
        private val deadlineHeap = LaneDeadlineHeap(policy.deadlineHeapCapacity)

        /**
         * Lane-owned local counters.
         *
         * These are plain mutable fields because only the lane thread writes them.
         * They are published in batches through [publishSnapshot].
         */
        private var localActiveCallbackCount: Int = 0

        /**
         * Lane-owned operational lifecycle.
         */
        private var laneState: DispatchLaneState = DispatchLaneState.OPEN

        /**
         * Published snapshot visible to administrative readers.
         *
         * This is the only source of truth for external quiescence judgment.
         */
        private val publishedSnapshot =
            AtomicReference(
                PublishedLaneSnapshot(
                    laneState = DispatchLaneState.OPEN,
                    commandRingEmpty = true,
                    readyQueueSize = 0,
                    activeCallbackCount = 0,
                    liveOperationalEntryCount = 0,
                    dirtyShardCount = 0,
                ),
            )

        fun start() {
            worker.start()
        }

        fun isStoppedPublished(): Boolean = publishedSnapshot.get().laneState == DispatchLaneState.STOPPED

        fun isQuiescentPublished(): Boolean {
            val snapshot = publishedSnapshot.get()
            return snapshot.commandRingEmpty &&
                    snapshot.readyQueueSize == 0 &&
                    snapshot.activeCallbackCount == 0 &&
                    snapshot.liveOperationalEntryCount == 0 &&
                    snapshot.dirtyShardCount == 0
        }

        fun offerCommand(command: LaneCommand): Boolean {
            if (closingRequested.get()) {
                return false
            }

            if (!DispatchLifecycleLaw.canLaneAdmitFreshWork(laneState)) {
                return false
            }

            val offered = commandRing.offer(command)
            if (offered) {
                onPublishedStateChange()
            }
            return offered
        }

        fun signalWorker() {
            commandRing.signalConsumer()
        }

        fun requestClose() {
            closingRequested.set(true)

            DispatchLifecycleLaw.requireTransition(
                from = laneState,
                to = DispatchLaneState.CLOSE_REQUESTED,
            )
            laneState = DispatchLaneState.CLOSE_REQUESTED

            publishSnapshot()
            commandRing.signalConsumer()
            onPublishedStateChange()
        }

        fun joinUntilStopped(timeoutNanos: Long) {
            if (timeoutNanos <= 0L) {
                return
            }

            val millis = timeoutNanos / 1_000_000L
            val nanosPart = (timeoutNanos % 1_000_000L).toInt()
            worker.join(millis, nanosPart)
        }

        private fun runLoop() {
            val batch = ArrayList<LaneCommand>(policy.commandBatchBudget)

            while (true) {
                var progress = 0

                progress += commandRing.drainUpTo(policy.commandBatchBudget, batch)
                for (command in batch) {
                    handleCommand(command)
                }
                if (batch.isNotEmpty()) {
                    batch.clear()
                }

                progress += processDueTimeouts(policy.timeoutBatchBudget)
                progress +=
                    processDirtyShards(
                        maxDirtyShards = policy.dirtyShardBatchBudget,
                        replayBatchBudgetPerShard = policy.replayBatchBudgetPerShard,
                    )
                progress += drainReadyQueue(policy.deliveryBatchBudget)

                if (closingRequested.get()) {
                    if (laneState == DispatchLaneState.CLOSE_REQUESTED) {
                        DispatchLifecycleLaw.requireTransition(
                            from = laneState,
                            to = DispatchLaneState.DRAINING,
                        )
                        laneState = DispatchLaneState.DRAINING
                    }

                    abandonCloseableEntries()

                    if (canPublishStoppedNow()) {
                        DispatchLifecycleLaw.requireLaneMayPublishStopped(
                            laneState = laneState,
                            commandRingIsEmpty = commandRing.isEmpty(),
                            readyQueuePublishedSize = readyQueue.localSize(),
                            activeCallbackCount = localActiveCallbackCount,
                            liveOperationalEntryCount = registrationStore.localLiveOperationalCount(),
                            dirtyShardCount = dirtyShards.localDirtyCount(),
                        )

                        DispatchLifecycleLaw.requireTransition(
                            from = laneState,
                            to = DispatchLaneState.STOPPED,
                        )
                        laneState = DispatchLaneState.STOPPED

                        /*
                         * Final lane-owned clear on worker exit.
                         * No external thread clears lane-owned mutable state.
                         */
                        readyQueue.clear()
                        registrationStore.forceClearAll()
                        deadlineHeap.clear()
                        dirtyShards.clearAll()

                        publishSnapshot()
                        onPublishedStateChange()
                        break
                    }
                }

                if (progress == 0) {
                    publishSnapshot()
                    commandRing.awaitConsumerSignal()
                } else {
                    publishSnapshot()
                    onPublishedStateChange()
                }
            }
        }

        private fun handleCommand(command: LaneCommand) {
            when (command) {
                is LaneCommand.Register -> handleRegister(command)
                is LaneCommand.SlotTerminalVisible -> handleTerminalVisible(command)
                is LaneCommand.Cancel -> handleCancellation(command)
            }
        }

        private fun handleRegister(command: LaneCommand.Register) {
            val resolution = command.slot.resolveSharedTerminalAcquire()
            if (resolution != null) {
                return
            }

            val entry =
                RegistrationEntry(
                    deliveryKey = command.deliveryKey,
                    shardIndex = command.shardIndex,
                    slot = command.slot,
                    waiter = command.waiter,
                    continuation = command.continuation,
                    deadlineNanos = command.deadlineNanos,
                    state = DeliveryEntryState.EMPTY,
                )

            DispatchLifecycleLaw.requireEmptyForFreshDeliveryRegistration(entry.state)
            transitionEntry(entry, DeliveryEntryState.REGISTERED)

            registrationStore.insert(entry)
            deadlineHeap.offer(entry)

            val afterInsertResolution = command.slot.resolveSharedTerminalAcquire()
            if (afterInsertResolution != null) {
                if (command.waiter.tryResumeFromSharedSignal(null) || command.waiter.isTerminalAcquire()) {
                    transitionEntry(entry, DeliveryEntryState.SIGNALED)
                    tryAcquireReadyQueueOwnership(entry)
                }
            }
        }

        private fun handleTerminalVisible(command: LaneCommand.SlotTerminalVisible) {
            command.slot.forEachVisibleWaiter { waiter ->
                val entry = registrationStore.findByWaiter(command.shardIndex, waiter) ?: return@forEachVisibleWaiter

                when (entry.state) {
                    DeliveryEntryState.REGISTERED -> {
                        if (waiter.tryResumeFromSharedSignal(null) || waiter.isTerminalAcquire()) {
                            transitionEntry(entry, DeliveryEntryState.SIGNALED)
                            tryAcquireReadyQueueOwnership(entry)
                        }
                    }

                    DeliveryEntryState.SIGNALED -> {
                        tryAcquireReadyQueueOwnership(entry)
                    }

                    DeliveryEntryState.QUEUED,
                    DeliveryEntryState.DELIVERING,
                    DeliveryEntryState.DONE,
                    DeliveryEntryState.ABANDONED,
                    DeliveryEntryState.EMPTY,
                        -> Unit
                }
            }

            command.slot.clearDeliveryPending()
        }

        private fun handleCancellation(command: LaneCommand.Cancel) {
            val entry = registrationStore.findByWaiter(command.shardIndex, command.waiter) ?: return

            when (entry.state) {
                DeliveryEntryState.REGISTERED,
                DeliveryEntryState.SIGNALED,
                DeliveryEntryState.QUEUED,
                    -> {
                    DispatchLifecycleLaw.requireCloseAbandonable(entry.state)
                    transitionEntry(entry, DeliveryEntryState.ABANDONED)
                    reclaim(entry)
                }

                DeliveryEntryState.DELIVERING,
                DeliveryEntryState.DONE,
                DeliveryEntryState.ABANDONED,
                DeliveryEntryState.EMPTY,
                    -> Unit
            }
        }

        private fun processDueTimeouts(budget: Int): Int {
            var processed = 0

            while (processed < budget) {
                val head = deadlineHeap.peek() ?: break
                if (head.deadlineNanos > timeSource.nowNanos()) {
                    break
                }

                deadlineHeap.poll()

                val entry = head.entry
                if (!registrationStore.isStillLive(entry)) {
                    continue
                }

                if (entry.state == DeliveryEntryState.REGISTERED) {
                    val waiter = entry.waiter
                    if (waiter != null && waiter.tryTimeout()) {
                        DispatchLifecycleLaw.requireCloseAbandonable(entry.state)
                        transitionEntry(entry, DeliveryEntryState.ABANDONED)
                        reclaim(entry)
                    }
                }

                processed++
            }

            return processed
        }

        private fun processDirtyShards(
            maxDirtyShards: Int,
            replayBatchBudgetPerShard: Int,
        ): Int {
            var processed = 0
            var shardVisits = 0

            while (shardVisits < maxDirtyShards) {
                val dirtyOrdinal = dirtyShards.nextSetBitAndClear()
                if (dirtyOrdinal < 0) {
                    break
                }

                val replayed =
                    registrationStore.replaySegment(
                        localShardOrdinal = dirtyOrdinal,
                        replayBatchBudget = replayBatchBudgetPerShard,
                    ) { entry ->
                        if (entry.state == DeliveryEntryState.SIGNALED) {
                            tryAcquireReadyQueueOwnership(entry)
                        }
                    }

                processed += replayed
                shardVisits++
            }

            return processed
        }

        private fun drainReadyQueue(budget: Int): Int {
            var processed = 0

            while (processed < budget) {
                val token = readyQueue.poll() ?: break
                val entry =
                    registrationStore.findByDeliveryKey(
                        shardIndex = token.shardIndex,
                        deliveryKey = token.deliveryKey,
                    ) ?: run {
                        processed++
                        continue
                    }

                if (entry.state != DeliveryEntryState.QUEUED) {
                    processed++
                    continue
                }

                transitionEntry(entry, DeliveryEntryState.DELIVERING)
                localActiveCallbackCount++

                try {
                    entry.continuation?.resume(JoinResumeSignal.ReadyForRestart)
                    entry.waiter?.markDeliveryDoneRelease()
                    transitionEntry(entry, DeliveryEntryState.DONE)
                } finally {
                    localActiveCallbackCount--
                    reclaim(entry)
                }

                processed++
            }

            return processed
        }

        private fun tryAcquireReadyQueueOwnership(entry: RegistrationEntry) {
            DispatchLifecycleLaw.requireSignaledForReadyQueueOwnership(entry.state)

            val offered =
                readyQueue.offer(
                    shardIndex = entry.shardIndex,
                    deliveryKey = entry.deliveryKey,
                )
            if (offered) {
                entry.waiter?.tryMarkDeliveryQueued()
                transitionEntry(entry, DeliveryEntryState.QUEUED)
            } else {
                markDirtyShard(entry.shardIndex)
            }
        }

        fun markDirtyShard(shardIndex: Int) {
            dirtyShards.set(registrationStore.localShardOrdinal(shardIndex))
        }

        /**
         * Abandons entries that are still closeable under lane ownership.
         *
         * DELIVERING is intentionally not abandoned here.
         * The currently executing callback is allowed to finish, after which the lane
         * thread reclaims the entry itself through DONE -> EMPTY.
         */
        private fun abandonCloseableEntries() {
            registrationStore.forEachLiveEntry { entry ->
                when (entry.state) {
                    DeliveryEntryState.REGISTERED,
                    DeliveryEntryState.SIGNALED,
                    DeliveryEntryState.QUEUED,
                        -> {
                        DispatchLifecycleLaw.requireCloseAbandonable(entry.state)
                        transitionEntry(entry, DeliveryEntryState.ABANDONED)
                        reclaim(entry)
                    }

                    DeliveryEntryState.DELIVERING,
                    DeliveryEntryState.DONE,
                    DeliveryEntryState.ABANDONED,
                    DeliveryEntryState.EMPTY,
                        -> Unit
                }
            }

            readyQueue.clear()
            deadlineHeap.clear()
            dirtyShards.clearAll()
        }

        /**
         * Entry reclamation.
         *
         * This is lane-owned.
         * External threads must never clear or remove lane-owned registrations directly.
         */
        private fun reclaim(entry: RegistrationEntry) {
            DispatchLifecycleLaw.requireTerminalForReclaimToEmpty(entry.state)

            entry.continuation = null
            entry.slot = null
            entry.waiter = null

            registrationStore.remove(entry)
            transitionEntry(entry, DeliveryEntryState.EMPTY)
        }

        private fun transitionEntry(
            entry: RegistrationEntry,
            to: DeliveryEntryState,
        ) {
            DispatchLifecycleLaw.requireTransition(
                from = entry.state,
                to = to,
            )
            entry.state = to
        }

        private fun canPublishStoppedNow(): Boolean =
            DispatchLifecycleLaw.canLanePublishStopped(
                laneState = laneState,
                commandRingIsEmpty = commandRing.isEmpty(),
                readyQueuePublishedSize = readyQueue.localSize(),
                activeCallbackCount = localActiveCallbackCount,
                liveOperationalEntryCount = registrationStore.localLiveOperationalCount(),
                dirtyShardCount = dirtyShards.localDirtyCount(),
            )

        private fun publishSnapshot() {
            publishedSnapshot.set(
                PublishedLaneSnapshot(
                    laneState = laneState,
                    commandRingEmpty = commandRing.isEmpty(),
                    readyQueueSize = readyQueue.localSize(),
                    activeCallbackCount = localActiveCallbackCount,
                    liveOperationalEntryCount = registrationStore.localLiveOperationalCount(),
                    dirtyShardCount = dirtyShards.localDirtyCount(),
                ),
            )
        }
    }

    // =========================================================================
    // Commands
    // =========================================================================

    private sealed interface LaneCommand {
        class Register(
            val deliveryKey: Long,
            val shardIndex: Int,
            val slot: InFlightSlot<CanonicalPlanNode>,
            val waiter: WaiterCell,
            val continuation: JoinContinuation,
            val deadlineNanos: Long,
        ) : LaneCommand

        class SlotTerminalVisible(
            val shardIndex: Int,
            val slot: InFlightSlot<CanonicalPlanNode>,
        ) : LaneCommand

        class Cancel(
            val shardIndex: Int,
            val waiter: WaiterCell,
        ) : LaneCommand
    }

    private class RegistrationEntry(
        val deliveryKey: Long,
        val shardIndex: Int,
        var slot: InFlightSlot<CanonicalPlanNode>?,
        var waiter: WaiterCell?,
        var continuation: JoinContinuation?,
        val deadlineNanos: Long,
        var state: DeliveryEntryState,
    )

    private data class PublishedLaneSnapshot(
        val laneState: DispatchLaneState,
        val commandRingEmpty: Boolean,
        val readyQueueSize: Int,
        val activeCallbackCount: Int,
        val liveOperationalEntryCount: Int,
        val dirtyShardCount: Int,
    )

    // =========================================================================
    // Lane-local substrates
    // =========================================================================

    /**
     * Sequence-slot bounded MPSC ring.
     *
     * This replaces the earlier nullable-slot + null-break design.
     *
     * Why:
     * - reserving a tail index before publication can temporarily leave a slot
     *   unpublished while later producers already publish later slots
     * - a consumer that treats `null` as "break" can therefore lose delivery
     *   visibility for commands that are already published behind the gap
     *
     * Sequence-based publication fixes that:
     * - each slot has an explicit publication epoch
     * - consumer distinguishes "not yet published" from "empty"
     * - no delivery-loss null-gap bug
     */
    private class LaneCommandRing(
        capacity: Int,
    ) {
        private val actualCapacity = normalizeCapacity(capacity)
        private val mask = actualCapacity - 1

        private val sequences = AtomicLongArray(actualCapacity)
        private val commands = AtomicReferenceArray<LaneCommand?>(actualCapacity)

        private val producerIndex = AtomicLong(0L)
        private val consumerIndex = AtomicLong(0L)
        private val consumerThread = AtomicReference<Thread?>(null)

        init {
            for (i in 0 until actualCapacity) {
                sequences.set(i, i.toLong())
            }
        }

        fun offer(command: LaneCommand): Boolean {
            while (true) {
                val p = producerIndex.get()
                val index = (p and mask.toLong()).toInt()
                val sequence = sequences.get(index)
                val diff = sequence - p

                when {
                    diff == 0L -> {
                        if (producerIndex.compareAndSet(p, p + 1)) {
                            commands.set(index, command)
                            sequences.set(index, p + 1)
                            signalConsumer()
                            return true
                        }
                    }

                    diff < 0L -> {
                        return false
                    }

                    else -> {
                        Thread.onSpinWait()
                    }
                }
            }
        }

        /**
         * Drains commands into a caller-owned local batch.
         *
         * Consumer logic is executed outside the ring implementation.
         */
        fun drainUpTo(
            maxCount: Int,
            output: MutableList<LaneCommand>,
        ): Int {
            bindConsumerThreadIfNeeded()

            var drained = 0
            var c = consumerIndex.get()

            while (drained < maxCount) {
                val index = (c and mask.toLong()).toInt()
                val sequence = sequences.get(index)
                val expected = c + 1
                val diff = sequence - expected

                when {
                    diff == 0L -> {
                        val command =
                            commands.get(index)
                                ?: throw PlanningInfrastructureException(
                                    "LaneCommandRing observed a published sequence without a command payload.",
                                )

                        commands.set(index, null)
                        sequences.set(index, c + actualCapacity.toLong())
                        c++
                        consumerIndex.set(c)

                        output.add(command)
                        drained++
                    }

                    diff < 0L -> {
                        break
                    }

                    else -> {
                        break
                    }
                }
            }

            return drained
        }

        fun awaitConsumerSignal() {
            bindConsumerThreadIfNeeded()

            if (!isEmpty()) {
                return
            }

            LockSupport.park(this)
        }

        fun signalConsumer() {
            consumerThread.get()?.let(LockSupport::unpark)
        }

        fun isEmpty(): Boolean = consumerIndex.get() >= producerIndex.get()

        private fun bindConsumerThreadIfNeeded() {
            val current = Thread.currentThread()
            consumerThread.compareAndSet(null, current)
        }

        companion object {
            private fun normalizeCapacity(requested: Int): Int {
                if (requested <= 0) {
                    throw DispatchLaneSaturatedException(
                        "LaneCommandRing capacity must be positive: $requested",
                    )
                }

                if (requested.countOneBits() == 1) {
                    return requested
                }

                var n = 1
                while (n < requested) {
                    n = n shl 1
                }
                return n
            }
        }
    }

    /**
     * Lane-local ready queue.
     *
     * Single-writer / single-consumer under the lane thread.
     * External readers do not inspect the queue directly.
     */
    private class LaneReadyQueue(
        capacity: Int,
    ) {
        private val actualCapacity = normalizeCapacity(capacity)
        private val mask = actualCapacity - 1
        private val shardIndexes = IntArray(actualCapacity)
        private val deliveryKeys = LongArray(actualCapacity)

        private var head: Int = 0
        private var tail: Int = 0
        private var size: Int = 0

        fun offer(
            shardIndex: Int,
            deliveryKey: Long,
        ): Boolean {
            if (size == actualCapacity) {
                return false
            }

            shardIndexes[tail] = shardIndex
            deliveryKeys[tail] = deliveryKey
            tail = (tail + 1) and mask
            size++
            return true
        }

        fun poll(): ReadyToken? {
            if (size == 0) {
                return null
            }

            val token =
                ReadyToken(
                    shardIndex = shardIndexes[head],
                    deliveryKey = deliveryKeys[head],
                )

            head = (head + 1) and mask
            size--
            return token
        }

        fun localSize(): Int = size

        fun clear() {
            head = 0
            tail = 0
            size = 0
        }

        class ReadyToken(
            val shardIndex: Int,
            val deliveryKey: Long,
        )

        companion object {
            private fun normalizeCapacity(requested: Int): Int {
                if (requested <= 0) {
                    throw DispatchLaneSaturatedException(
                        "LaneReadyQueue capacity must be positive: $requested",
                    )
                }

                if (requested.countOneBits() == 1) {
                    return requested
                }

                var n = 1
                while (n < requested) {
                    n = n shl 1
                }
                return n
            }
        }
    }

    /**
     * Single-writer deadline heap.
     *
     * External readers never inspect the heap directly.
     */
    private class LaneDeadlineHeap(
        capacity: Int,
    ) {
        private val entries = arrayOfNulls<RegistrationEntry>(capacity)
        private val deadlines = LongArray(capacity)
        private var size: Int = 0

        fun offer(entry: RegistrationEntry) {
            if (size == entries.size) {
                throw DispatchLaneSaturatedException(
                    "Lane deadline heap is saturated.",
                )
            }

            var idx = size++
            entries[idx] = entry
            deadlines[idx] = entry.deadlineNanos
            siftUp(idx)
        }

        fun peek(): DeadlineHead? {
            if (size == 0) {
                return null
            }

            val entry = entries[0] ?: return null
            return DeadlineHead(
                entry = entry,
                deadlineNanos = deadlines[0],
            )
        }

        fun poll(): DeadlineHead? {
            if (size == 0) {
                return null
            }

            val head =
                DeadlineHead(
                    entry = entries[0]!!,
                    deadlineNanos = deadlines[0],
                )

            size--
            entries[0] = entries[size]
            deadlines[0] = deadlines[size]
            entries[size] = null
            if (size > 0) {
                siftDown(0)
            }

            return head
        }

        fun clear() {
            Arrays.fill(entries, null)
            size = 0
        }

        private fun siftUp(index: Int) {
            var i = index
            while (i > 0) {
                val parent = (i - 1) ushr 1
                if (deadlines[parent] <= deadlines[i]) {
                    return
                }
                swap(parent, i)
                i = parent
            }
        }

        private fun siftDown(index: Int) {
            var i = index
            while (true) {
                val left = (i shl 1) + 1
                val right = left + 1
                if (left >= size) {
                    return
                }

                var smallest = left
                if (right < size && deadlines[right] < deadlines[left]) {
                    smallest = right
                }

                if (deadlines[i] <= deadlines[smallest]) {
                    return
                }

                swap(i, smallest)
                i = smallest
            }
        }

        private fun swap(
            a: Int,
            b: Int,
        ) {
            val e = entries[a]
            entries[a] = entries[b]
            entries[b] = e

            val d = deadlines[a]
            deadlines[a] = deadlines[b]
            deadlines[b] = d
        }

        class DeadlineHead(
            val entry: RegistrationEntry,
            val deadlineNanos: Long,
        )
    }

    /**
     * Lane-local dirty-shard bitmap.
     *
     * External readers do not inspect the bitmap directly.
     */
    private class DirtyShardBitmap(
        bitCount: Int,
    ) {
        private val words = AtomicLongArray((bitCount + 63) / 64)
        private var localDirtyCount: Int = 0

        fun set(bitIndex: Int) {
            val wordIndex = bitIndex ushr 6
            val bitMask = 1L shl (bitIndex and 63)

            while (true) {
                val observed = words.get(wordIndex)
                if ((observed and bitMask) != 0L) {
                    return
                }

                val updated = observed or bitMask
                if (words.compareAndSet(wordIndex, observed, updated)) {
                    localDirtyCount++
                    return
                }
            }
        }

        fun nextSetBitAndClear(): Int {
            for (wordIndex in 0 until words.length()) {
                while (true) {
                    val observed = words.get(wordIndex)
                    if (observed == 0L) {
                        break
                    }

                    val lsb = observed and -observed
                    val updated = observed xor lsb
                    if (words.compareAndSet(wordIndex, observed, updated)) {
                        localDirtyCount--
                        val bit =
                            java.lang.Long
                                .numberOfTrailingZeros(lsb)
                                .toInt()
                        return (wordIndex shl 6) + bit
                    }
                }
            }
            return -1
        }

        fun localDirtyCount(): Int = localDirtyCount

        fun clearAll() {
            for (i in 0 until words.length()) {
                words.set(i, 0L)
            }
            localDirtyCount = 0
        }
    }

    /**
     * Shard-segmented lane-local registration store.
     *
     * Primary key: deliveryKey64
     * Auxiliary lookup: waiter identity
     *
     * External quiescence readers never inspect segment arrays directly.
     * They rely on the lane-published snapshot only.
     */
    private class LaneRegistrationStore(
        private val laneIndex: Int,
        val effectiveLaneCount: Int,
        shardCount: Int,
        registrationStoreCapacityPerShard: Int,
    ) {
        private val segmentCount = shardCount / effectiveLaneCount
        private val segments: Array<RegistrationSegment> =
            Array(segmentCount) {
                RegistrationSegment(registrationStoreCapacityPerShard)
            }

        private var localLiveOperationalCount: Int = 0

        fun localShardOrdinal(shardIndex: Int): Int {
            val ordinal = (shardIndex - laneIndex) / effectiveLaneCount
            if (ordinal < 0 || ordinal >= segments.size) {
                throw DispatchLaneSaturatedException(
                    "Shard $shardIndex does not belong to lane $laneIndex.",
                )
            }
            return ordinal
        }

        fun insert(entry: RegistrationEntry) {
            segments[localShardOrdinal(entry.shardIndex)].insert(entry)
            localLiveOperationalCount++
        }

        fun findByWaiter(
            shardIndex: Int,
            waiter: WaiterCell,
        ): RegistrationEntry? = segments[localShardOrdinal(shardIndex)].findByWaiter(waiter)

        fun findByDeliveryKey(
            shardIndex: Int,
            deliveryKey: Long,
        ): RegistrationEntry? = segments[localShardOrdinal(shardIndex)].findByDeliveryKey(deliveryKey)

        fun remove(entry: RegistrationEntry) {
            segments[localShardOrdinal(entry.shardIndex)].remove(entry)
            localLiveOperationalCount--
        }

        fun localLiveOperationalCount(): Int = localLiveOperationalCount

        fun isStillLive(entry: RegistrationEntry): Boolean = DispatchLifecycleLaw.isLiveOperational(entry.state)

        fun replaySegment(
            localShardOrdinal: Int,
            replayBatchBudget: Int,
            consumer: (RegistrationEntry) -> Unit,
        ): Int =
            segments[localShardOrdinal].replayFromCursor(
                replayBatchBudget = replayBatchBudget,
                consumer = consumer,
            )

        fun forEachLiveEntry(consumer: (RegistrationEntry) -> Unit) {
            for (segment in segments) {
                segment.forEachLiveEntry(consumer)
            }
        }

        fun forceClearAll() {
            for (segment in segments) {
                segment.forceClear()
            }
            localLiveOperationalCount = 0
        }

        private class RegistrationSegment(
            capacity: Int,
        ) {
            private val keys = LongArray(capacity)
            private val states = ByteArray(capacity) // 0 empty, 1 occupied, 2 tombstone
            private val values = arrayOfNulls<RegistrationEntry>(capacity)
            private val waiterIndex = IdentityHashMap<WaiterCell, RegistrationEntry>()
            private var cursor: Int = 0

            fun insert(entry: RegistrationEntry) {
                val slot = findSlotForInsert(entry.deliveryKey)
                keys[slot] = entry.deliveryKey
                states[slot] = 1
                values[slot] = entry
                entry.waiter?.let { waiterIndex[it] = entry }
            }

            fun findByWaiter(waiter: WaiterCell): RegistrationEntry? = waiterIndex[waiter]

            fun findByDeliveryKey(deliveryKey: Long): RegistrationEntry? {
                val slot = findSlotForLookup(deliveryKey)
                return if (slot >= 0) values[slot] else null
            }

            fun remove(entry: RegistrationEntry) {
                val slot = findSlotForLookup(entry.deliveryKey)
                if (slot >= 0) {
                    states[slot] = 2
                    values[slot] = null
                }
                entry.waiter?.let { waiterIndex.remove(it) }
            }

            fun replayFromCursor(
                replayBatchBudget: Int,
                consumer: (RegistrationEntry) -> Unit,
            ): Int {
                var visited = 0
                var consumed = 0

                while (visited < replayBatchBudget) {
                    val idx = cursor
                    cursor = (cursor + 1) % keys.size
                    visited++

                    if (states[idx].toInt() != 1) {
                        continue
                    }

                    val entry = values[idx] ?: continue
                    consumer(entry)
                    consumed++
                }

                return consumed
            }

            fun forEachLiveEntry(consumer: (RegistrationEntry) -> Unit) {
                for (entry in values) {
                    if (entry != null && DispatchLifecycleLaw.isLiveOperational(entry.state)) {
                        consumer(entry)
                    }
                }
            }

            fun forceClear() {
                Arrays.fill(states, 0)
                Arrays.fill(values, null)
                waiterIndex.clear()
                cursor = 0
            }

            private fun findSlotForLookup(key: Long): Int {
                val start = mix(key) % keys.size
                for (i in keys.indices) {
                    val idx = (start + i) % keys.size
                    when (states[idx].toInt()) {
                        0 -> return -1
                        1 -> if (keys[idx] == key) return idx
                        2 -> Unit
                    }
                }
                return -1
            }

            private fun findSlotForInsert(key: Long): Int {
                val start = mix(key) % keys.size
                var firstTombstone = -1

                for (i in keys.indices) {
                    val idx = (start + i) % keys.size
                    when (states[idx].toInt()) {
                        0 -> return if (firstTombstone >= 0) firstTombstone else idx
                        1 -> if (keys[idx] == key) return idx
                        2 -> if (firstTombstone < 0) firstTombstone = idx
                    }
                }

                if (firstTombstone >= 0) {
                    return firstTombstone
                }

                throw DispatchLaneSaturatedException(
                    "Lane registration segment is saturated.",
                )
            }

            private fun mix(key: Long): Int {
                var h = key
                h = h xor (h ushr 33)
                h *= -0xae502812aa7333L
                h = h xor (h ushr 33)
                h *= -0x3b3146010f6d7dL
                h = h xor (h ushr 33)
                return (h.toInt() and Int.MAX_VALUE)
            }
        }
    }

    private class DispatchLaneSaturatedException(
        message: String,
    ) : PlanningInfrastructureException(message)
}
