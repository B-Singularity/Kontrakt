# Design: Primitive Lane-Owned Delivery for Tier-2 Joined-Wait Completion

- Status: Draft
- Related ADR: ADR-0035
- Date: 2026-04-12
- Scope: Concrete implementation design for Tier-2 joined-wait completion delivery
- Audience: Planning runtime / cache / orchestration implementers

---

## 1. Purpose

This document defines the concrete implementation strategy for the execution model adopted by ADR-0035.

ADR-0035 standardizes the architectural choice:

- joined-wait completion delivery uses deterministic balanced lanes (M:N)
- shards remain the authority units
- lanes remain delivery-execution units
- shard-to-lane mapping is static
- global dispatch structures are forbidden
- caller-thread direct callback execution is forbidden
- live adaptive rebalance is forbidden

This Design document specifies how to realize that decision on the JVM while preserving:

- non-blocking join
- adapter-owned completion delivery
- fresh-session restart compatibility
- shard-local authority separation
- bounded memory and bounded execution work
- primitive-friendly implementation discipline

This document intentionally prefers primitive, fixed-capacity, array-backed, single-writer techniques over globally
shared dynamic collections.

---

## 2. Design Summary

The target implementation is:

- `N` logical shards
- `M` fixed dispatch lanes, where `M < N` is allowed and expected
- one dedicated worker thread per lane
- one lane-local bounded command ring
- one lane-local single-writer registration store
- one lane-local single-writer deadline heap
- one lane-local dirty-shard bitmap
- one lane-local ready queue
- no global `ConcurrentHashMap`
- no global blocking queue
- no caller-thread direct table writes
- no full-table rescan
- no live rebalance

The design principle is:

> **Shards own truth and routing.  
> Lanes own delivery execution.  
> All lane-local mutable delivery state is mutated only by the lane thread.**

This keeps the architecture aligned with the rest of the planning engine:

- explicit ownership
- fixed-capacity state
- primitive routing
- bounded replay
- no hidden global contention points

---

## 3. Architectural Position

### 3.1 Domain vs Infrastructure

The dispatch plane is adapter-internal infrastructure.

It is not:

- a Domain Core port
- semantic authority
- lifecycle-truth authority
- routing authority

The Domain Core continues to see only the existing `PlanInternRepository` boundary and the non-blocking join algebra.

The dispatch implementation exists entirely behind the adapter boundary.

### 3.2 Authority Split

#### Shard owns

- exact-match pre-screening
- in-flight coordination
- builder vs join branching
- governance reaction mapping
- authoritative bucket re-verification

#### Slot / waiter / builder / region own

- shared-slot truth
- waiter-local truth
- builder-handle truth
- commit-right truth
- region lifecycle truth

#### Lane owns

- continuation registration storage
- timeout scheduling and timeout execution
- terminal sweep execution
- restart-ready callback execution
- lane-local replay of missed terminal signals
- lane-local quiescence accounting

#### Plane owns

- lane composition
- shard-to-lane static mapping
- lane worker lifecycle
- adapter-wide close / quiescence coordination

---

## 4. Hard Constraints

The following are architectural constraints, not implementation suggestions.

1. No caller-thread direct callback execution
2. No caller-thread direct registration-table mutation
3. No global dispatch registry
4. No global delivery queue
5. No live rebalance
6. No work stealing
7. No full-table rescan
8. No silent delivery-task drop
9. No unbounded queue growth
10. No independent dispatch routing authority

---

## 5. Lane Topology

### 5.1 Lane Count

`laneCount` must satisfy:

- power of two
- `1 <= laneCount <= shardCount`

Recommended bootstrap baseline:

`````text
laneCount = min(
    shardCount,
    nextPowerOfTwo(max(1, availableProcessors / 2))
)
`````

Rationale:

- joined-wait completion delivery is not the dominant semantic hot path
- one lane per shard is operationally excessive
- one global lane is architecturally too centralizing
- a half-core baseline is a reasonable JVM execution starting point for a dedicated secondary path

`laneCount` is fixed at adapter bootstrap time and remains immutable for the adapter lifetime.

### 5.2 Shard-to-Lane Mapping

Routing law:

1. `route64 -> shardIndex`
2. `laneIndex = shardIndex & (laneCount - 1)`

The dispatch plane does not re-derive lane placement from semantic keys.
It follows shard ownership only.

This preserves the rule:

> lane routing is a deterministic function of shard routing.

### 5.3 Dispatch Lane Policy Binding

The concrete dispatch-lane capacities and loop budgets are not defined as local constants in this document.

They are resolved from `ResolvedDispatchLanePolicy` at the stable runtime-policy boundary.

This document defines:

- the meaning of those values,
- where they apply,
- and the invariants they must satisfy inside the lane implementation.

The authoritative bootstrap numeric values belong to the runtime-policy document, not to this design note.

For v1:

- the dispatch-lane policy is profile-bound,
- `AUTO = STANDARD`,
- the installed values remain fixed for the adapter lifetime,
- and no adaptive mutation is allowed after installation.

The same policy snapshot also owns dispatch quiescence grace values used by:

- partition-drop convergence
- adapter shutdown convergence

Those values are policy-managed wall-clock grace limits for adapter-owned dispatch infrastructure only.
They are not planner-session elapsed-time watchdog values.

---

## 6. Internal Lane Structure

Each lane owns the following state.

### 6.1 Worker Thread

Exactly one dedicated worker thread per lane.

Responsibilities:

- drain lane command ring
- mutate lane registration store
- mutate lane deadline heap
- perform terminal sweeps
- perform timeout actions
- enqueue ready deliveries
- invoke callbacks
- perform bounded dirty-shard replay
- maintain lane-local quiescence state

The lane thread is the only writer of lane-local mutable state.

### 6.2 Command Ring

Each lane owns one bounded MPSC command ring.

This ring is the only ingress path for lane-owned work.

All of the following enter the lane through commands:

- registration
- cancellation
- slot-terminal-visible signals
- shutdown markers
- optional control signals

The ring is fixed-capacity and array-backed.

The target implementation should use:

- primitive scalar fields in parallel arrays where possible
- object references only where unavoidable (for example slot reference, waiter reference, continuation reference)
- sequence-based bounded ring discipline
- no blocking queue abstraction

#### Why one command ring?

We explicitly choose a single lane-owned command ingress because it gives:

- one clear ownership boundary
- no caller-thread direct table writes
- no multi-writer table mutation
- no queue-splitting ambiguity about precedence

This is a compiler/runtime style choice: one ingress boundary, one owner, explicit internal scheduling.

### 6.3 Registration Store

Each lane owns a lane-local registration store.

The registration store is:

- fixed-capacity
- array-backed
- primitive-keyed
- single-writer
- lane-owned

Recommended structure:

- one registration segment per shard owned by the lane
- each segment is an open-addressing table keyed by `deliveryKey64`

Each registration entry stores at minimum:

- `deliveryKey64`
- `shardIndex`
- `slotRef`
- `waiterRef`
- `continuationRef`
- `deadlineNanos`
- `deliveryStateBits`

The registration store is never directly mutated by caller threads.

#### Why shard-segmented instead of one flat lane table?

Because overflow recovery must be shard-affine and bounded.

Segmenting by shard gives:

- bounded replay scope
- explicit hot-shard visibility
- no need for global lane-wide scans
- better conceptual alignment with static shard affinity

### 6.4 Deadline Heap

Each lane owns one bounded deadline heap.

The deadline heap is:

- single-writer
- lane-local
- primitive-friendly
- ordered by `deadlineNanos`

Each heap entry minimally contains:

- `deliveryKey64`
- `deadlineNanos`
- `shardIndex`

We choose a deadline heap rather than a timing wheel for this cut because:

- joined-wait suspension is a secondary path, not the dominant throughput path
- exact monotonic deadlines are easier to reason about with a heap
- heap semantics remain deterministic and simple
- the implementation is easier to harden on the JVM without bucket drift semantics

A future timing-wheel variant may be explored later if profiling justifies it.
It is not the baseline design.

### 6.5 Ready Queue

Each lane owns one bounded ready queue.

The ready queue is:

- single-consumer
- lane-local
- bounded
- array-backed
- written by the lane thread only

Its purpose is to separate:

- signal acceptance
- callback execution scheduling

Recommended payload:

- `deliveryKey64`

Optional payload extensions may include:

- compact opcode
- shard-local segment hint
- diagnostic flags

The ready queue must not be globally shared and must not be implemented as a global blocking queue.

### 6.6 Dirty-Shard Bitmap

Each lane owns one dirty-shard bitmap for the shards assigned to that lane.

The bitmap is used only for bounded replay recovery when a terminal-visible signal could not be enqueued normally.

It is not a substitute for the main delivery path.

The bitmap must support:

- atomic set by producer threads
- lane-thread clear after replay pass

A dirty shard means:

> One or more terminal-visible signals for this shard may not have been processed through the normal command path.  
> Perform bounded replay over this shard's registration segment.

---

## 7. Primitive Identity Model

### 7.1 Delivery Key

The dispatch system uses an immutable primitive registration key:

- `deliveryKey64`

Required properties:

1. unique for the registration episode
2. stable for the episode lifetime
3. not reused before safe reclamation
4. opaque outside infrastructure
5. cheap to store and compare

Recommended composition:

- embed the owning lane index in the high bits
- embed shard-local or adapter-local monotonic sequence material in the remaining bits

This is not a semantic cache key.
It is an infrastructure delivery identity.

#### Why embed lane index?

Not because the lane needs an independent routing authority.

The lane is already derived from `shardIndex`.

Embedding the lane index serves only as:

- a hard diagnostic invariant
- an extra guard against accidental cross-lane contamination
- a cheap sanity check in debugging and validation paths

Lane embedding must never replace the primary shard-to-lane mapping law.

---

## 8. Delivery Entry State Machine

Delivery progress is modeled as a lane-owned operational state machine.

This state machine is intentionally distinct from:

- `SharedSlotState`
- `WaiterState`
- `BuilderHandleState`
- `PartitionRegionState`

Those axes describe semantic/runtime truth.
The delivery entry state machine describes only callback delivery progress inside the adapter-owned lane infrastructure.

### 8.1 States

- `EMPTY`
- `REGISTERED`
- `SIGNALED`
- `QUEUED`
- `DELIVERING`
- `DONE`
- `ABANDONED`

### 8.2 State Meanings

#### `EMPTY`

No live registration entry is present in the slot.
EMPTY is both the initial state and the post-reclamation reusable state.

#### `REGISTERED`

A continuation registration is durably installed in the lane-owned registration store, and no delivery-eligible signal
has yet been accepted for this entry.

#### `SIGNALED`

The lane has accepted a delivery-eligible signal for this entry, but the entry has not yet acquired ready-queue
ownership.

This state exists to separate:

- signal acceptance
- queue ownership

That separation is required for correct dirty-shard replay.

#### `QUEUED`

The entry has successfully acquired ready-queue ownership and is awaiting dequeue by the lane worker.

#### `DELIVERING`

The lane worker is currently executing the callback for this entry.

#### `DONE`

Exactly-once callback delivery completed successfully.
After callback completion is durably observed by the lane, the entry must be reclaimed to EMPTY.

#### `ABANDONED`

The entry was retired without callback completion, such as shutdown/drop cleanup or equivalent terminal cleanup policy.
After abandonment cleanup is complete, the entry must be reclaimed to EMPTY.

### 8.3 Transition Authority

All delivery-entry state transitions are lane-thread-owned.

Producer threads may:

- enqueue commands
- set dirty-shard bits

Producer threads must not mutate delivery-entry state directly.

### 8.4 Allowed Transitions

- `EMPTY -> REGISTERED`
- `REGISTERED -> SIGNALED`
- `SIGNALED -> QUEUED`
- `QUEUED -> DELIVERING`
- `DELIVERING -> DONE`
- `REGISTERED -> ABANDONED`
- `SIGNALED -> ABANDONED`
- `QUEUED -> ABANDONED`
- `DONE -> EMPTY`
- `ABANDONED -> EMPTY`

No other transitions are allowed.

In particular:

- `DONE` is terminal
- `ABANDONED` is terminal
- `QUEUED -> SIGNALED` is forbidden
- producer-thread state mutation is forbidden

### 8.5 Replay Rule

Dirty-shard replay operates only on entries in `SIGNALED`.

Replay must not target entries in:

- `QUEUED`
- `DELIVERING`
- `DONE`
- `ABANDONED`

This guarantees that replay only recovers entries that accepted a signal but did not yet obtain ready-queue ownership.

### 8.6 Already-Terminal Registration Rule

If terminal visibility is observed during registration, the lane thread must still own the transition sequence.

A valid sequence is:

- `EMPTY -> REGISTERED`
- `REGISTERED -> SIGNALED`
- `SIGNALED -> QUEUED`
- `QUEUED -> DELIVERING`

or an equivalent lane-owned fast path that preserves the same state ownership and exactly-once guarantees.

---

## 9. Core Algorithms

### 9.1 Registration Path

#### Inputs

The caller has:

- shard ownership already known via the joined `JoinHandle`
- waiter reference
- slot reference
- continuation
- deadline

#### Algorithm

1. Caller reads slot terminal visibility.
2. If terminal truth is already visible:
    - return `AlreadyReady`
    - do not store registration
    - do not enqueue lane registration
    - runtime boundary restarts immediately through the normal already-ready path
3. Otherwise:
    - derive `laneIndex` from `shardIndex`
    - derive `deliveryKey64`
    - enqueue `REGISTER` command to the owning lane
4. Lane worker processes `REGISTER`:
    - inserts entry into shard registration segment
    - inserts deadline into lane heap
    - re-checks slot terminal visibility
    - if terminal became visible before or during registration finalization:
        - perform lane-owned terminal handling
        - transition `REGISTERED -> SIGNALED`
        - attempt `SIGNALED -> QUEUED`
        - if dequeued immediately in the same service slice, continue to `DELIVERING`

#### Important consequences

- caller thread never mutates the registration store directly
- registration store remains single-writer
- queue pressure is explicit and bounded
- already-terminal cases do not consume registration capacity beyond lane-owned fast handling

### 9.2 Cancellation Path

Cancellation is lane-owned after registration exists.

Algorithm:

1. caller enqueues `CANCEL(deliveryKey64, reason)` to the owning lane
2. lane worker:
    - finds registration
    - asks waiter to cancel if still eligible
    - if entry is still live and not yet delivering, transition to `ABANDONED`
    - remove/deactivate registration
    - remove or tombstone deadline entry

Cancellation is a waiter-local event and does not mutate shared-slot truth.

### 9.3 Slot Terminalization Path

When shard logic observes a slot terminalization that should wake joined waiters:

1. shard does not invoke callbacks directly
2. shard enqueues `SLOT_TERMINAL_VISIBLE(slotRef, shardIndex)` to the owning lane
3. lane worker processes the command:
    - iterates `slotRef.forEachVisibleWaiter(...)`
    - for each waiter:
        - obtain `deliveryKey64`
        - look up lane registration segment for this shard
        - if no registration exists, skip
        - if entry is not in `REGISTERED`, skip
        - if waiter is already terminal through timeout/cancel, skip
        - transition `REGISTERED -> SIGNALED`
        - attempt ready-queue admission:
            - on success, `SIGNALED -> QUEUED`
            - on failure, leave entry in `SIGNALED` and set dirty shard

This keeps callback execution lane-owned while slot/waiter truth remains in the lifecycle hosts.

### 9.4 Timeout Path

Timeout is lane-owned.

Algorithm:

1. registration inserted with `deadlineNanos`
2. lane worker consults deadline heap during its loop
3. for each due entry:
    - lookup registration
    - if registration already gone, discard heap entry
    - if entry is `REGISTERED`, ask waiter to timeout
    - if timeout wins:
        - transition `REGISTERED -> SIGNALED`
        - attempt `SIGNALED -> QUEUED`
        - on queue failure, leave `SIGNALED` and set dirty shard
    - if entry is already `QUEUED`, `DELIVERING`, `DONE`, or `ABANDONED`, no timeout transition is applied

Timeout remains waiter-local and does not mutate shared-slot truth.

### 9.5 Delivery Execution Path

The lane worker drains the ready queue.

For each `deliveryKey64`:

1. lookup registration
2. require state `QUEUED`
3. transition `QUEUED -> DELIVERING`
4. invoke callback
5. transition `DELIVERING -> DONE`
6. remove/deactivate registration
7. retire deadline entry if still present

The `READY -> DELIVERING` equivalent transition is therefore:

- `SIGNALED -> QUEUED`
- `QUEUED -> DELIVERING`

Only the lane thread performs that progression.

### 9.6 Reclamation Path

After a delivery entry reaches `DONE` or `ABANDONED`, the lane thread must eventually reclaim it back to `EMPTY`.

Reclamation responsibilities:

1. remove or clear continuation ownership
2. retire or clear waiter / slot references as required
3. retire or clear deadline ownership
4. clear delivery-state bits
5. return the storage slot to `EMPTY`

Reclamation is lane-thread-owned.

Producer threads must not reclaim entries directly.

The fixed-capacity registration store relies on this reclamation loop to preserve bounded reuse.
Without `DONE -> EMPTY` and `ABANDONED -> EMPTY`, the store would eventually saturate permanently.

---

## 10. Bounded Replay Instead of Full Rescan

The previous draft used a full-table rescan concept.
That design is rejected.

### 10.1 Rejected Mechanism

- lane-wide or table-wide O(N) rescan after overflow

### 10.2 Reason for Rejection

- too much latency jitter
- weak execution-time predictability
- overbroad replay scope
- poor fit for a bounded deterministic engine

### 10.3 Chosen Mechanism

**dirty-shard bounded replay**

If a producer cannot enqueue `SLOT_TERMINAL_VISIBLE` because the command ring is full:

1. do not silently drop
2. do not block the caller
3. do not scan the whole lane later
4. atomically set `dirtyShardBit[shardIndexWithinLane] = 1`

Lane worker replay loop:

- after each normal command batch, the lane checks dirty shards
- for each dirty shard selected in this cycle:
    - replay only that shard segment
    - replay is bounded by `replayBatchBudget`
    - replay uses a persistent per-shard cursor
    - if unfinished, the dirty bit remains set
    - if complete, the dirty bit is cleared

Replay step logic:

- examine registration entry at current cursor
- target only entries in `SIGNALED`
- if entry can acquire ready-queue ownership, transition `SIGNALED -> QUEUED`
- advance cursor
- stop when `replayBatchBudget` is exhausted

This converts overflow recovery from:

- lane-wide O(N) scan

into:

- shard-affine O(batch) replay with stable upper bounds per worker iteration

### 10.4 Why This Is Acceptable

- no silent delivery loss
- no caller blocking
- no full-table jitter spike
- replay work is bounded and inspectable
- static shard affinity is preserved

---

## 11. Overflow and Saturation Policy

Boundedness is mandatory.
Therefore overflow handling must be explicit.

### 11.1 Registration Command Ring Full

If `REGISTER` cannot be enqueued:

- fail closed immediately
- do not attempt caller-thread direct insertion
- do not silently ignore registration
- signal adapter-local infrastructure saturation

Recommended handling:

- throw a lane-saturation infrastructure exception
- upper adapter/runtime layer converts this to a fail-safe joined-wait degradation path
- the waiter must not remain as an unowned suspended registration

This is intentionally strict.
If the system cannot own the continuation safely, it must not pretend that registration succeeded.

### 11.2 Slot-Terminal-Visible Command Ring Full

If `SLOT_TERMINAL_VISIBLE` cannot be enqueued:

- do not drop
- do not block
- mark `dirtyShardBit`
- record telemetry
- rely on bounded dirty-shard replay

This is valid because registrations already exist in lane-owned storage.

### 11.3 Cancellation Command Ring Full

If `CANCEL` cannot be enqueued:

- fail closed
- record telemetry
- allow the caller to retry or abandon according to higher-level shutdown rules

This path should be rare and operationally visible.

### 11.4 Ready Queue Full

If an entry in `SIGNALED` cannot acquire ready-queue ownership:

- do not drop
- do not block
- leave the entry in `SIGNALED`
- set dirty shard
- record telemetry

This is the precise case for bounded dirty-shard replay.

### 11.5 Deadline Heap Saturation

If the deadline heap is full:

- treat it as lane-local infrastructure saturation
- fail closed
- do not silently disable timeout ownership

Because timeout is a correctness-related operational contract, missing timeout ownership is not acceptable.

---

## 12. Lane Worker Loop

### Required policy-managed budgets

The following loop budgets are resolved from `ResolvedDispatchLanePolicy`:

- `commandBatchBudget`
- `timeoutBatchBudget`
- `dirtyShardBatchBudget`
- `replayBatchBudgetPerShard`
- `deliveryBatchBudget`

These values are fixed for the lifetime of the installed adapter/runtime-policy snapshot.

`dirtyShardBatchBudget` limits the number of dirty shards selected in one loop iteration.

`replayBatchBudgetPerShard` limits the number of replayed registration entries for one selected dirty shard in one
replay visit.

The replay upper bound per loop iteration is therefore:

`````text
dirtyShardBatchBudget * replayBatchBudgetPerShard
`````

The following bounded capacities are also resolved from `ResolvedDispatchLanePolicy`:

- `commandRingCapacity`
- `readyQueueCapacity`
- `registrationStoreCapacityPerShard`
- `deadlineHeapCapacity`

This document treats them as fixed bounded infrastructure limits, not as live tuning surfaces.

---

## 13. Quiescence Definition

A lane is quiescent only when all of the following hold:

- command ring is empty
- ready queue is empty
- no callback is currently executing
- no entry remains in `REGISTERED`
- no entry remains in `SIGNALED`
- no entry remains in `QUEUED`
- no entry remains in `DELIVERING`
- dirty-shard bitmap is clear

Queue emptiness alone is not sufficient for quiescence.

### 13.1 Quiescence Grace Policy

Quiescence detection and quiescence grace are distinct concerns.

This document defines the semantic/operational meaning of quiescence.

The maximum wall-clock time allowed for waiting on that quiescence is resolved from `ResolvedDispatchLanePolicy`
through:

- `partitionDropQuiescenceTimeoutNanos`
- `adapterCloseQuiescenceTimeoutNanos`

These values are:

- immutable after policy installation
- adapter-lifetime stable
- not locally hard-coded in adapter code
- not subject to live adaptive mutation

---

## 14. Telemetry and Hot-Lane Skew

This design intentionally does not solve skew with live rebalance.

Instead, it standardizes observability.

Each lane must record at minimum:

- current registration occupancy
- peak registration occupancy
- current command ring depth
- peak command ring depth
- current ready queue depth
- peak ready queue depth
- registration enqueue failures
- terminal-visible enqueue failures
- ready-queue admission failures
- deadline-heap occupancy
- timeout count
- callback count
- callback service-time buckets
- dirty-shard set count
- replay passes
- replayed entries
- quiescence wait duration
- per-shard contribution counts if feasible

### Purpose of Telemetry

- detect hot-lane skew
- detect hot-shard concentration within a lane
- support next-bootstrap / next-epoch lane-count tuning
- support shard-count changes
- support incident debugging

### Explicit Rule

Telemetry informs next-epoch tuning only.

It must not trigger live rebalance.

Telemetry may justify a later re-ratification of dispatch-lane policy constants, but it must not mutate the
already-installed policy snapshot of the current adapter lifetime.

---

## 15. Trade-Off Analysis

### 15.1 Chosen Design

#### Primitive single-writer lane model

Chosen because it provides:

- the cleanest ownership boundary
- no concurrent table mutation
- no global contention point
- bounded queue semantics
- bounded replay semantics
- strong alignment with compiler-style runtime engineering

#### Cost

- more code than a `ConcurrentHashMap` plus queue prototype
- explicit key issuance required
- explicit saturation paths required
- explicit replay cursor logic required
- explicit delivery-state progression required

This complexity is accepted because it buys deterministic ownership and avoids long-term architectural debt.

### 15.2 Rejected: lane-local `HashMap` plus `LinkedBlockingQueue`

Rejected as the target design because:

- object-heavy
- weak fit with the primitive-first direction of the rest of the engine
- weak memory discipline
- easy to accidentally keep forever as a temporary implementation

This combination may be useful only as a bring-up prototype.
It is not the target design.

### 15.3 Rejected: caller-thread direct registration-table insertion

Rejected because:

- it breaks single-writer ownership
- it forces either locks or concurrent-safe tables
- it complicates correctness reasoning
- it weakens lane ownership discipline

### 15.4 Rejected: full-table rescan

Rejected because:

- it turns overflow recovery into uncontrolled lane-wide work
- it introduces avoidable latency spikes
- it is too blunt for a bounded deterministic delivery engine

### 15.5 Rejected: live adaptive rebalance

Rejected because:

- it violates ADR-0035
- it makes runtime behavior harder to reason about
- it turns a next-epoch tuning concern into a live ownership mutation

---

## 16. File / Type Impact

Expected implementation impact:

- `L2JoinDispatchPlane` becomes a façade over fixed lanes
- new internal `DispatchLane`
- new internal primitive `LaneCommandRing`
- new internal shard-segmented `LaneRegistrationStore`
- new internal `LaneDeadlineHeap`
- new internal `LaneReadyQueue`
- new internal `DirtyShardBitmap`
- `WaiterCell` or related registration machinery gains `deliveryKey64`
- `L2Shard` enqueues registration, cancel, and slot-terminal commands to the owning lane
- `PartitionRegion` and adapter bootstrap own lane count and lane construction

The design does **not** move semantic authority into the lane layer.

---

## 17. Recommended Implementation Sequence

1. Add `deliveryKey64` issuance for registration episodes.
2. Implement lane-local bounded command ring.
3. Implement lane-local shard-segmented registration store.
4. Implement lane-local deadline heap.
5. Implement lane-local ready queue.
6. Implement one worker thread per lane.
7. Implement static `shardIndex -> laneIndex` mapping.
8. Convert registration to command-enqueued single-writer ownership.
9. Convert terminal-visible delivery to lane-owned sweep commands.
10. Implement delivery entry state machine with `SIGNALED` and `QUEUED`.
11. Implement dirty-shard bounded replay.
12. Add saturation exceptions and telemetry.
13. Integrate lane quiescence into partition drop / adapter shutdown.
14. Only then profile for further micro-optimizations.
15. Implement lane-owned reclamation from DONE / ABANDONED back to EMPTY.
16. resolve dispatch-lane budgets and capacities from `ResolvedDispatchLanePolicy` rather than from local ad hoc
    constants
17. route partition-drop and adapter-close quiescence waiting through `ResolvedDispatchLanePolicy` rather than through
    local default timeout literals

---

## 18. Final Recommendation

The target implementation for ADR-0035 is:

- deterministic balanced M:N lanes
- static shard-to-lane affinity
- one worker thread per lane
- one bounded lane-local command ring
- one single-writer lane-local registration store
- one single-writer lane-local deadline heap
- one bounded lane-local ready queue
- one lane-local dirty-shard bitmap
- explicit lane-owned delivery entry state machine
- no global `ConcurrentHashMap`
- no global blocking queue
- no caller-thread direct registration-table mutation
- no full-table rescan
- no silent task drop
- no live rebalance

This is the strongest fit with Kontrakt’s current direction:
**compiler-like ownership, explicit runtime mechanics, bounded infrastructure, primitive-first engineering, and strict
authority separation.**