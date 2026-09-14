# ADR-0034: Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority

- Status: Accepted
- Date: 2026-03-26

<!-- AMENDED(2026-03-30): Added explicit planning-run orchestration lifecycle axis,
named epoch/generation taxonomy (`RuntimePolicyEpoch`, `PlanningRunEpoch`, `WorkerBackingEpoch`, `L2HostGeneration`),
fresh-session restart authority boundaries, and run-level transition / invariant law
without weakening any previously declared L2 lifecycle semantics. -->

## Document Relationship and Precedence

### Amends / Clarifies

This ADR amends and clarifies the lifecycle semantics implied by:

- ADR-0031: Two-Tier Transactional Memoization and Structural Interning
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- Design Note: Planner Budget Resolution and Worker Lifecycle
- Design Note: L2 Plan Interner (Partition → Shard → Bucket(2-Phase)) with Governance
- Constitution: Compiler Core Protocols

### Precedence

For the scope covered by this ADR, this document is the authoritative source for:

- top-level L2 lifecycle states,
- legal and illegal transitions,
- terminalization authority,
- race arbitration law,
- publication-before-completion ordering,
- cross-axis invariants,
- and evolution rules governing what requires ADR amendment.

The following remain authoritative in their respective domains:

- cost-center IDs and budget-track law remain governed by `RequiredCostCentersSpec`,
- policy snapshot resolution remains governed by ADR-0032 and related policy notes,
- primitive field packing, CAS encoding, visibility primitives, and layout isolation details remain governed by the
  follow-up design note for this ADR.

If a future document conflicts with this ADR on lifecycle legality, this ADR takes precedence unless superseded by a
later ADR.

---

## Context

The current L2 join / in-flight coordination model has already outgrown an implicit `future + counter` style
coordination helper.

Accepted planning/runtime documents now require explicit lifecycle guarantees across several dimensions:

- shared-slot terminalization,
- waiter-local timeout and cancellation independence,
- publication-before-completion,
- attach/drop race linearizability,
- exact-once terminalization,
- governance-aware degradation,
- and adapter-owned dispatch/scheduling that must not become semantic authority.

At the same time, the current implementation surface is still materially thinner than the required behavioral model.

A representative example of the current thin surface is an in-flight coordination object centered on:

- one shared completion primitive,
- one start timestamp,
- and one waiter count,

rather than on explicit shared-slot and waiter states.

That surface is insufficient to express, defend, or verify the normative lifecycle obligations already declared
elsewhere.

More concretely, the current model suffers from the following structural problems.

### 1. Shared-slot state is implicit rather than first-class

A shared completion primitive alone cannot explicitly distinguish:

- lawful shared success,
- shared operational failure,
- region-driven drop,
- waiter-local timeout,
- or waiter-local cancellation.

As a result, shared-slot meaning risks being inferred from implementation behavior rather than declared by lifecycle
law.

### 2. Waiter-local terminalization and shared-slot terminalization are not cleanly separated

Without an explicit waiter lifecycle, timeout and cancellation can only be represented as control-flow behavior rather
than as first-class semantic states.

That makes it too easy for waiter-local timeout or cancellation behavior to drift toward shared-slot terminalization
semantics.

### 3. Attach admission and slot terminalization are not yet governed by one explicit authority law

If attach admission, waiter counting, and shared-slot closure are not treated as one atomic lifecycle concern, a race
may create a zombie waiter:
the waiter count may be advanced while the slot simultaneously becomes terminally closed.

The lifecycle law must prevent that class of race, even if the final physical encoding is deferred to the design note.

### 4. Completion, timeout, cancellation, and drop can become competing terminalization sources

Builder completion, waiter timeout, waiter cancellation, partition drop, and region close are all legitimate event
sources.  
Without one explicit state machine, they may compete through fragmented logic rather than through one authority surface.

### 5. Re-verification failure currently has no explicit panic/isolation contract

The lifecycle documents already require publication-before-completion and authoritative re-verification through the
committed bucket path.  
However, if that re-verification fails, the runtime must not silently degrade or “treat it like a miss”.

That condition is an integrity-class failure and requires an explicit panic / isolation rule.

### 6. Cost-center metering is necessary but insufficient

Cost-center vocabulary can meter lifecycle events, but metering does not by itself define:

- legal transitions,
- illegal transitions,
- exact-once terminalization,
- race arbitration,
- or visibility ordering.

### 7. Future roadmap changes increase the risk of semantic drift

The roadmap is expected to evolve across:

- join strategy,
- speculative behavior,
- governance policies,
- timeout/restart strategies,
- dispatch mechanics,
- and adapter internals.

Without an explicit lifecycle law, such changes would encourage accidental semantic drift inside shard logic and
infrastructure code.

### 8. Builder progress cannot remain an unbounded assumption

The miss-path currently depends on the builder eventually choosing exactly one of:

- commit,
- or abort.

That assumption is not strong enough as a lifecycle law.

If builder progress disappears, stalls indefinitely, or is lost after authority has been issued, the system requires a
lawful supervisory convergence path rather than an indefinite `OPEN` handle.

### 9. Build permission and publish permission are not semantically identical

Speculative or duplicate-build scenarios reveal that “allowed to build” and “allowed to publish” are separate lifecycle
concerns.

If they remain conflated, duplicate builders can leak into duplicate publication or undefined arbitration behavior.

### 10. Reclamation cannot remain a purely local cleanup notion

If region reclamation is allowed before visible waiters, pending terminal deliveries, and stale generation windows have
converged, then stale callbacks, stale timeout signals, or recycled lifecycle hosts can reappear after reclamation.

Therefore reclamation meaning must be strengthened at the lifecycle-law level, not left entirely to mechanical cleanup.

### 11. Fresh-session restart introduces a new runtime-boundary lifecycle axis (AMENDED)

Once joined-waiter resumption is modeled as fresh-session restart rather than blocked-worker continuation, the runtime
no longer has only L2-internal lifecycle concerns.

It also acquires a separate runtime-boundary orchestration concern:

- one logical planning run may span more than one `PlannerSession`,
- the old session must still exit through ordinary cleanup,
- a later fresh session may resume the same logical run,
- and that run must preserve deterministic continuity across restart.

This is not reducible to shared-slot state, waiter state, builder-handle state, or region state.

### 12. Policy-version continuity and worker-backing freshness must not be conflated (AMENDED)

The runtime already distinguishes policy snapshots from worker-local state cleanup.

Fresh-session restart makes that separation architectural rather than merely operational.

The system now requires explicit separation among:

- runtime policy versioning,
- logical planning-run continuity,
- worker-backing freshness for reset/reuse,
- and L2 lifecycle-host generation for stale-reference defense.

Without explicit naming and ownership boundaries, these different “time axes” drift into one another and weaken
determinism.

Kontrakt requires compiler-grade runtime semantics: explicit, enumerable, sealed, deterministic, and testable.  
Therefore the L2 join lifecycle must be promoted from an implicit coordination pattern to an explicit state machine.

---

## Decision

Kontrakt SHALL adopt an **explicit dual-axis L2 join lifecycle state machine** as the single source of truth for
shared-slot, waiter, and builder-handle terminalization.

The lifecycle model is intentionally split across orthogonal axes:

1. shared-slot lifecycle,
2. per-waiter lifecycle,
3. builder-handle lifecycle,
4. partition-region lifecycle,
5. and, where enabled by governance, speculative-lease lifecycle as an orthogonal sub-machine.

These axes MUST remain semantically distinct.

This ADR further declares that when joined-waiter completion is consumed through fresh-session restart, the runtime
boundary SHALL introduce an additional **planning-run orchestration lifecycle axis** that is orthogonal to the L2
lifecycle axes above.

Therefore the full semantic model covered by this ADR consists of:

1. shared-slot lifecycle,
2. per-waiter lifecycle,
3. builder-handle lifecycle,
4. partition-region lifecycle,
5. speculative-lease lifecycle where enabled,
6. and planning-run orchestration lifecycle where fresh-session restart is enabled.

These axes MUST remain semantically distinct.

This ADR defines:

- the top-level lifecycle states,
- the allowed and forbidden transitions,
- the authority model for terminalization,
- race arbitration rules,
- ordering and visibility laws,
- irreversibility and sealing rules,
- and the evolution rules that determine when ordinary refactoring is sufficient and when an ADR amendment is required.

This ADR does **not** freeze one exact low-level implementation technique such as `VarHandle` vs
`AtomicLongFieldUpdater`, or one exact bit-packing layout.  
Those mechanical details belong in a separate design note.  
However, this ADR does define the normative implementation constraints that any compliant implementation must satisfy.

This ADR also does **not** define the full worker-backing reset mechanics for fresh-session restart.  
Those remain governed by the worker-lifecycle design note.  
However, the orchestration state semantics and version-axis naming law introduced here are normative.

---

## Design Principles

### 1. Closed semantic core, open operational perimeter

Top-level lifecycle states are intentionally closed.  
Operational evolution must be absorbed through:

- policy snapshots,
- reason taxonomies,
- orthogonal sub-machines,
- and adapter-owned execution mechanisms,

before introducing new top-level lifecycle states.

### 2. Deterministic semantic law, replaceable runtime mechanics

The semantic law must remain stable even when the runtime algorithm changes.

The following may evolve without changing the top-level lifecycle model:

- timeout scheduling strategy,
- completion-dispatch strategy,
- speculative admission policy,
- telemetry calibration,
- primitive table layout,
- and routing optimizations.

### 3. Domain meaning separated from adapter mechanics

The meaning of states and transitions is part of the domain/runtime contract.  
The storage layout, CAS encoding, dispatch queues, timeout wheels, completion executors, and memory-layout strategies
belong to infrastructure.

### 4. Single terminalization authority

No runtime component may terminalize shared slots or waiters through ad hoc side channels.  
Timeout, completion, drop, and cancellation are transition sources, not semantic authorities.

### 5. Publication-before-completion is physical, not merely logical

A successful shared-slot terminalization is not merely a conceptual ordering.  
It is also a visibility guarantee.  
A waiter that resumes from success must be able to observe the published committed winner immediately through the
authoritative bucket path.

### 6. Evolution must preserve invariants before expanding state

Future changes must first attempt to evolve through:

- reason taxonomies,
- policy snapshots,
- and orthogonal sub-machines.

Only if those are insufficient may top-level lifecycle state be revisited.

### 7. Explicit naming for version axes (AMENDED)

Epoch and generation names are semantic boundary labels and MUST be explicit.

Normative naming rule:

- `*Epoch` names are reserved for immutable versioned snapshots at governance, planning-run orchestration, or
  worker-backing reuse boundaries.
- `*Generation` names are reserved for reusable lifecycle-host episode discriminators used for stale-reference defense
  and future reclamation safety.

Bare names such as:

- `Epoch`,
- `Generation`,
- `RequestEpoch`,
- `SessionEpoch`,

are forbidden as normative architecture vocabulary because they conceal ownership and semantic purpose.

---

## Scope

This ADR covers the L2 lifecycle semantics for:

- in-flight shared-slot coordination,
- waiter attachment and terminalization,
- builder-handle terminalization,
- commit-right arbitration,
- partition close/drop interaction,
- speculative lease lifecycle where applicable,
- attach/timeout/cancel/complete/drop race interaction,
- and the ordering rules required before adapter-owned dispatch infrastructure may be attached.

This ADR additionally covers the planning-run orchestration semantics that become normative once joined-waiter
resumption is modeled as fresh-session restart.

This ADR does not redefine:

- cost-center IDs,
- physical-vs-semantic accounting tracks,
- L1 structural budget math,
- canonical equality semantics,
- exact-match bucket authority,
- bootstrap policy resolution,
- worker-backing reset mechanics,
- worker-pool quarantine mechanics,
- or planner-session primitive slab layout.

Those remain governed by their respective ADRs and design notes.

---

## Version-Axis Separation (AMENDED)

The runtime SHALL distinguish the following version / episode axes.

| Name                 | Owner / Boundary                       | Semantic Role                                                                 | Must Not Be Confused With                                       |
|----------------------|----------------------------------------|-------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `RuntimePolicyEpoch` | runtime policy registry / governance   | immutable runtime-policy snapshot version                                     | run continuity, worker-backing freshness, L2 host reuse         |
| `PlanningRunEpoch`   | planning-run orchestration boundary    | one logical planning run continuity, including suspend / restart identity     | policy snapshot version, worker-backing freshness               |
| `WorkerBackingEpoch` | worker-backing / planner-state backing | freshness/version marker for worker-local backing reuse after reset           | logical planning-run identity                                   |
| `L2HostGeneration`   | L2 shard / lifecycle-host issuance     | stale-reference defense and future reclamation safety for slot/waiter/handles | policy snapshot version, planning-run continuity, backing reuse |

Normative consequences:

- `RuntimePolicyEpoch` MUST remain about runtime-policy snapshot version only.
- `PlanningRunEpoch` MUST remain about one end-to-end planning run continuity only.
- `WorkerBackingEpoch` MUST remain about worker-backing freshness / reuse only.
- `L2HostGeneration` MUST remain an episode discriminator for reusable L2 lifecycle hosts only.

A compliant implementation MAY physically represent these with plain `Long` values, inline/value classes, or other
factory-issued immutable wrappers, but the semantic naming separation above is normative.

---

## State Model

## SharedSlotState

The shared in-flight slot MUST expose exactly the following top-level lifecycle states:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `DROPPED`

These states have the following meanings.

`PENDING` means the shared slot exists, has not terminalized, and may still accept lawful lifecycle activity according
to governance and attach rules.

`SUCCESS` means the authoritative committed winner has already been published, and attached waiters may now lawfully
complete by observing that shared terminal signal.

`FAILED` means the shared slot has terminalized unsuccessfully as a shared-slot failure.  
This is a shared terminal state, not a waiter-local terminal state.

`DROPPED` means the shared slot was terminalized by region/partition closure or bulk-drop governance.

## WaiterState

Each successfully attached waiter MUST expose exactly the following top-level lifecycle states:

- `ATTACHED`
- `RESUMED`
- `TIMED_OUT`
- `CANCELLED`

These states have the following meanings.

`ATTACHED` means the waiter has successfully entered the shared-slot lifecycle and exactly one future terminal waiter
outcome remains reachable.

`RESUMED` means the waiter terminalized by observing the authoritative shared terminal signal.  
That shared terminal signal may correspond to:

- successful shared completion,
- shared failure,
- or region-driven drop,

provided that the terminal shared outcome is lawfully and authoritatively observed.

`TIMED_OUT` means the waiter terminalized locally due to its own join timeout policy.

`CANCELLED` means the waiter terminalized locally due to cancellation or interruption policy.

### Attach rejection is not a waiter state

Attach rejection does not create a waiter lifecycle object.

If attach is rejected, the outcome is a pre-lifecycle admission result, not a waiter-state transition.  
Therefore attach rejection does not introduce a new waiter top-level state.

## BuilderHandleState

If the shared-slot miss path returns a builder-owned handle, that handle MUST expose exactly the following top-level
states:

- `OPEN`
- `COMMITTED`
- `ABORTED`

The builder handle is a separate protection layer and is not redundant with shared-slot terminalization.

### Builder handle liveness meaning

`OPEN` is supervision-bound rather than indefinitely passive.

An `OPEN` builder handle means that commit/abort authority is outstanding under a lawful supervisory regime.  
It does **not** mean that the system may wait forever without convergence.

A compliant implementation MUST guarantee that every issued builder handle converges to exactly one of:

- `COMMITTED`,
- `ABORTED`,

either by builder action or by supervisory force-abort.

## CommitRightState

Where multiple speculative or duplicate builders may exist, authoritative publication MUST be guarded by an orthogonal
commit-right lifecycle.

The commit-right lifecycle MUST expose at least:

- `UNCLAIMED`
- `CLAIMED`
- `RELEASED`

These states have the following meanings.

`UNCLAIMED` means publication authority has not yet been won.

`CLAIMED` means exactly one contender has acquired the right to enter authoritative publication.

`RELEASED` means commit-right arbitration has terminalized and can no longer admit another publication winner for that
slot lifecycle.

Build permission and publish permission are distinct.  
A runtime may permit more than one builder to execute, but only one commit-right may enter authoritative publication.

## PartitionRegionState

The partition/region lifecycle MUST expose an explicit lifecycle surface sufficient to support close publication,
no-new-admission behavior, and reclamation completion.

At minimum, the semantic lifecycle MUST distinguish:

- `OPEN`
- `CLOSE_PUBLISHED`
- `RECLAIMED`

These states have the following meanings.

`OPEN` means the region may still admit lawful shared-slot and waiter activity.

`CLOSE_PUBLISHED` means no new lifecycle admission may rely on the region being open, and visible in-flight state must
now converge toward lawful terminalization before reclamation.

`RECLAIMED` means the region has completed terminalization convergence, pending terminal deliveries have become
unreachable as pending work, and the required grace barrier has completed.  
Only then may the region cease hosting mutable lifecycle state.

This ADR intentionally defines only the minimum semantic lifecycle required by its invariants.  
A follow-up document may refine implementation-facing region sub-states, but it must not weaken the semantic meaning
above.

## SpeculativeLeaseState

Where speculative-builder promotion is enabled by governance, speculative promotion MUST be modeled as an orthogonal
slot-owned lease machine rather than by mutating the top-level shared-slot or waiter state model.

A speculative lease lifecycle, once created, MUST expose at least:

- `ISSUED`
- `RELEASED`

Lease absence is modeled as **absence of a lease object**, not as a lifecycle state.

This sub-machine is orthogonal.  
It does not replace or redefine shared-slot, waiter, builder-handle, or commit-right states.

## PlanningRunState (AMENDED)

Where joined-waiter completion is consumed through fresh-session restart, the runtime boundary MUST expose exactly the
following top-level planning-run orchestration states:

- `INITIALIZED`
- `RUNNING`
- `SUSPENDED_ON_JOIN`
- `READY_TO_RESTART`
- `COMPLETED`
- `ABORTED`
- `PANIC_ISOLATED`

These states have the following meanings.

`INITIALIZED` means the logical planning run has been created, but no worker-local session lease has yet been admitted
into active execution.

This state exists to support queueing, admission control, or equivalent runtime-boundary delay before the first
`RUNNING` session begins.

`RUNNING` means one logical planning run is actively executing through one live `PlannerSession`.

`SUSPENDED_ON_JOIN` means the logical planning run has suspended because it is awaiting a non-blocking joined-waiter
outcome, and the current `PlannerSession` is no longer permitted to remain retained as half-live worker-local state.

`READY_TO_RESTART` means the joined-waiter outcome has become available and the runtime boundary may now create a fresh
`PlannerSession` to continue the same logical planning run.

`COMPLETED` means the logical planning run has produced its final semantic result and no further restart is reachable.

`ABORTED` means the logical planning run has terminalized unsuccessfully and no restart is reachable.

`PANIC_ISOLATED` means the logical planning run terminalized under panic-grade isolation semantics.

This state is stronger than ordinary `ABORTED`.
It records that the runtime boundary must treat the surrounding worker/backing or equivalent execution substrate as
potentially contaminated, quarantine-requiring, or otherwise unsafe for ordinary reuse until the worker lifecycle
boundary proves safety.

### Planning-run state is not a replacement for L2 state

`PlanningRunState` is orthogonal to:

- shared-slot state,
- waiter state,
- builder-handle state,
- commit-right state,
- and partition-region state.

It exists because fresh-session restart introduces a separate runtime-boundary concern:
logical run continuity across more than one worker-local session.

### Planning-run identity and pinned policy meaning

A planning run, once created, owns one immutable `PlanningRunEpoch`.

A compliant implementation MUST also define which `RuntimePolicyEpoch` is pinned for that run.

This ADR adopts the following rule:

- all sessions belonging to one logical planning run MUST observe the same pinned `RuntimePolicyEpoch`.

A fresh restarted session therefore begins with a fresh worker-local session boundary, but not with a newly chosen
runtime-policy snapshot.

---

## State Meanings Must Remain Stable

The following meanings are constitutional.

`SUCCESS` always means publication is already complete and visible.  
It must never degrade into “publication has started” or “publication is probably complete.”

`FAILED` always means the shared slot terminalized as a shared failure.  
It must not be overloaded to also mean waiter-local timeout or cancellation.

`DROPPED` always means region-driven terminalization.  
It must not be reused for application-level failure.

`RESUMED` always means the waiter terminalized by observing the authoritative shared terminal signal.  
The payload or reason may vary; the waiter top-level state does not.

`TIMED_OUT` and `CANCELLED` are waiter-local states only.  
They must never be treated as shared-slot failure.

`OPEN` for builder handles is a supervision-bound outstanding authority, not an indefinite passive wait state.

`UNCLAIMED`, `CLAIMED`, and `RELEASED` for commit-right arbitration must remain about **publication authority only**,
not builder execution progress.

`CLOSE_PUBLISHED` always means the close gate is already visible to participants.  
It must never be interpreted as a merely local flag with no admission consequence.

`RECLAIMED` always means the region is no longer allowed to host mutable lifecycle state and the required grace barrier
has completed.

`INITIALIZED` always means the run exists but no worker-local session lease has yet been admitted into active
execution.

It must not be collapsed into `RUNNING` merely because creation and first admission happen close together in one
implementation.

`PANIC_ISOLATED` always means panic-grade terminalization with stronger isolation semantics than ordinary `ABORTED`.

It must not be reused as a synonym for ordinary logical failure.

`RUNNING` for planning-run orchestration always means exactly one live worker-local session currently owns execution for
that logical run.

`SUSPENDED_ON_JOIN` always means the logical run is waiting on joined completion without retaining a half-live
worker-local planner substrate as the continuation owner.

`READY_TO_RESTART` always means joined completion is available and a fresh worker-local session may lawfully resume the
same logical run.

`COMPLETED` and `ABORTED` for planning-run orchestration are terminal.  
They must not be reopened.

`RuntimePolicyEpoch` always means runtime-policy snapshot version only.  
It must not be overloaded to mean logical run identity.

`PlanningRunEpoch` always means end-to-end planning-run continuity only.  
It must not be reused as backing-freshness or policy-snapshot version.

`WorkerBackingEpoch` always means worker-backing freshness / reuse version only.  
It must not be reused as planning-run continuity.

`L2HostGeneration` always means reusable L2 lifecycle-host episode discrimination only.

---

## Failure Taxonomy

Top-level state explosion is forbidden.  
Therefore `FAILED` remains a single top-level shared-slot terminal state.

However, the failure **reason taxonomy** is explicitly open for future extension.

A compliant implementation MUST support failure reasons that distinguish at least:

- integrity/protocol violation,
- operational/shared execution failure,
- governance-imposed abort,
- supervisory builder-abandonment or equivalent liveness failure,
- and equivalent future reason subclasses.

Partition governance and higher-level reaction policy may differentiate these reasons.  
For example, an integrity-shattering failure may trigger immediate partition isolation or panic propagation, while an
operational failure may permit degrade/retry handling.

This distinction belongs to **reason taxonomy and governance reaction**, not to top-level lifecycle-state proliferation.

Planning-run `ABORTED` remains a closed top-level run outcome.  
Its root cause taxonomy MAY evolve independently from shared-slot failure taxonomy.

---

## Transition Law

### Shared slot transitions

The only legal shared-slot transitions are:

- `PENDING -> SUCCESS`
- `PENDING -> FAILED`
- `PENDING -> DROPPED`

All other shared-slot transitions are forbidden.

In particular, the following are forbidden:

- any transition out of `SUCCESS`,
- any transition out of `FAILED`,
- any transition out of `DROPPED`,
- `SUCCESS -> FAILED`,
- `FAILED -> DROPPED`,
- `DROPPED -> SUCCESS`,
- or any equivalent re-terminalization.

### Waiter transitions

The only legal waiter transitions are:

- attachment success creates `ATTACHED`,
- `ATTACHED -> RESUMED`,
- `ATTACHED -> TIMED_OUT`,
- `ATTACHED -> CANCELLED`.

All other waiter transitions are forbidden.

In particular, the following are forbidden:

- resume after timeout,
- resume after cancellation,
- timeout after resume,
- cancellation after resume,
- double timeout,
- double cancellation,
- or any second terminal waiter outcome.

### Builder handle transitions

The only legal handle transitions are:

- `OPEN -> COMMITTED`
- `OPEN -> ABORTED`

All other handle transitions are forbidden.

In particular, the following are forbidden:

- double commit,
- double abort,
- commit after abort,
- abort after commit.

### Commit-right transitions

The only legal commit-right transitions are:

- `UNCLAIMED -> CLAIMED`
- `CLAIMED -> RELEASED`

All other commit-right transitions are forbidden.

In particular, the following are forbidden:

- `CLAIMED -> CLAIMED` by a second winner,
- `RELEASED -> CLAIMED`,
- or any second authoritative publication winner.

### Partition region transitions

The only legal region transitions at this semantic level are:

- `OPEN -> CLOSE_PUBLISHED`
- `CLOSE_PUBLISHED -> RECLAIMED`

All other region transitions are forbidden.

In particular, the following are forbidden:

- `RECLAIMED -> OPEN`,
- `RECLAIMED -> CLOSE_PUBLISHED`,
- direct reclamation that bypasses close publication,
- reclamation before grace completion,
- and continued mutable lifecycle admission after reclamation.

### Speculative lease transitions

Where speculative leases exist, the only legal conceptual transitions are:

- `ISSUED -> RELEASED`

No speculative lease may survive terminal shared-slot completion, shared-slot failure, shared-slot drop, or region
reclamation.

### Planning-run transitions (AMENDED)

The only legal planning-run orchestration transitions are:

- `INITIALIZED -> RUNNING`
- `INITIALIZED -> ABORTED`
- `INITIALIZED -> PANIC_ISOLATED`
- `RUNNING -> SUSPENDED_ON_JOIN`
- `RUNNING -> COMPLETED`
- `RUNNING -> ABORTED`
- `RUNNING -> PANIC_ISOLATED`
- `SUSPENDED_ON_JOIN -> READY_TO_RESTART`
- `SUSPENDED_ON_JOIN -> ABORTED`
- `SUSPENDED_ON_JOIN -> PANIC_ISOLATED`
- `READY_TO_RESTART -> RUNNING`
- `READY_TO_RESTART -> ABORTED`
- `READY_TO_RESTART -> PANIC_ISOLATED`

All other planning-run transitions are forbidden.

In particular, the following are forbidden:

- `INITIALIZED -> READY_TO_RESTART`
- `INITIALIZED -> SUSPENDED_ON_JOIN`
- `COMPLETED -> RUNNING`
- `ABORTED -> RUNNING`
- `PANIC_ISOLATED -> RUNNING`
- `READY_TO_RESTART -> COMPLETED` without an intervening lawful resumed execution path,
- retaining more than one live worker-local session for the same `PlanningRunEpoch`,
- and reopening a terminal planning run.

---

## Cross-Axis Invariants

The following invariants are constitutional.

1. Waiter timeout MUST NOT mutate shared-slot terminal state.
2. Waiter cancellation MUST NOT mutate shared-slot terminal state.
3. Shared-slot `SUCCESS` MUST only occur after authoritative publication has linearized.
4. A waiter that reaches `RESUMED` MUST observe the authoritative shared terminal signal.
5. If the shared terminal signal is `SUCCESS`, the waiter MUST be able to observe the published winner through the
   authoritative bucket path.
6. Successful attach implies that exactly one future terminal waiter outcome remains reachable.
7. Attach success before drop publication implies eventual waiter terminalization completeness.
8. Drop publication before attach success implies no lawful attach success may occur afterward.
9. Shared-slot `FAILED` MUST also converge all visible attached waiters to lawful terminal waiter outcomes; failure is
   not permitted to strand attached waiters.
10. Partition drop MUST terminalize visible attached waiters before reclamation completes.
11. Close publication MUST happen-before final region reclamation.
12. `RECLAIMED` MUST NOT be reached before the required grace barrier completes.
13. No side channel may create a second terminal waiter outcome after the first terminal waiter transition has won.
14. No side channel may re-terminalize a terminal shared slot.
15. Slot-owned speculative leases MUST be released when their owning shared slot terminalizes.
16. A region in `RECLAIMED` state must not permit any mutable lifecycle transition.
17. Build execution and publication authority MUST remain distinct; more than one builder may exist, but no more than
    one commit-right winner may enter authoritative publication.
18. Every issued builder handle MUST converge under supervision; orphaned builder authority is illegal.
19. Delivery failure or delivery delay MUST NOT rewrite lifecycle truth.
20. Cost-center metering may observe lifecycle events, but must never replace lifecycle legality.
21. A planning run suspended on joined wait MUST NOT retain worker-local mutable planner substrate as suspended
    continuation state.
22. All sessions belonging to one `PlanningRunEpoch` MUST observe the same pinned `RuntimePolicyEpoch`.
23. Fresh-session restart MUST create a new worker-local session boundary even when the logical planning run remains the
    same.
24. `WorkerBackingEpoch` continuity/reuse correctness MUST remain orthogonal to `PlanningRunEpoch` continuity.
25. Completion dispatch MAY advance planning-run orchestration state, but it MUST NOT rewrite L2 lifecycle truth.
26. `L2HostGeneration` MUST NOT be used as a substitute for `RuntimePolicyEpoch`, `PlanningRunEpoch`, or
    `WorkerBackingEpoch`.
27. `INITIALIZED` planning runs MUST NOT retain an active worker-session lease.
28. `PANIC_ISOLATED` planning runs MUST NOT retain an active worker-session lease or a restartable suspension handle.
29. Panic-grade terminalization of a planning run MUST remain stronger than ordinary `ABORTED`; it is not permitted to
    degrade to ordinary abort merely because the worker/session boundary cleanup succeeded.

---

## Authority Model

### Shared-slot terminalization authority

Exactly one shared-slot terminal transition may win.  
That winning transition must be represented by a single shared-slot transition authority.

No timeout path, cancellation path, or stale callback may directly overwrite a terminal shared-slot state.

### Waiter terminalization authority

Exactly one waiter terminal transition may win per attached waiter.  
The winning transition must be decided by the waiter lifecycle machine only.

Timeout threads, completion executors, partition drop sweepers, and interruption handlers are event sources only.  
They are not terminalization authorities.

### Builder-handle terminalization authority

Exactly one builder-handle terminal transition may win per issued handle.

Any attempt to commit or abort after terminalization is illegal.

Supervisory force-abort is a lawful builder-handle terminalization source.

### Commit-right arbitration authority

Exactly one commit-right transition to `CLAIMED` may win for any publication episode.

Commit-right authority governs entry into authoritative publication.  
It does not replace builder-handle authority and does not replace shared-slot terminalization authority.

### Region close / reclaim authority

Partition/region lifecycle changes must also pass through explicit lifecycle authority.  
Close publication and reclamation must not be inferred from incidental cleanup side effects.

### Planning-run orchestration authority (AMENDED)

Exactly one planning-run state transition may win for the authoritative run-state surface at any point in time.

The planning-run orchestration authority governs:

- run suspension on joined wait,
- readiness for restart,
- restart admission,
- run completion,
- run abortion.

It does not replace:

- shared-slot terminalization authority,
- waiter terminalization authority,
- builder-handle terminalization authority,
- commit-right authority,
- or region authority.

The planning-run orchestration authority also governs:

- initial admission from `INITIALIZED` to `RUNNING`,
- ordinary unsuccessful terminalization to `ABORTED`,
- and panic-grade terminalization to `PANIC_ISOLATED`.

It is therefore the single authority for run-level queue/admit/suspend/restart/terminal decisions.

### Runtime-policy snapshot authority (AMENDED)

`RuntimePolicyEpoch` publication remains governance authority, not run-state authority.

A planning run may pin one `RuntimePolicyEpoch`, but the pinning rule does not turn runtime-policy publication into
planning-run state authority.

### Dispatch and scheduling are not semantic authorities

Timeout schedulers, completion executors, restart controllers, and drop sweepers may inject events.  
They may not redefine state legality or override a terminal transition already won by the lifecycle machine.

Delivery infrastructure may delay observation.  
It may not rewrite authoritative lifecycle truth.

---

## Race Arbitration Law

This ADR adopts an explicit **winner-takes-all** rule for lifecycle races.

### General rule

Whenever two or more legal contenders race to terminalize the same lifecycle object, the winner is the first successful
atomic transition on the authoritative state field for that axis.

All losing contenders MUST:

- observe the already-terminal state,
- become semantic no-ops for that axis,
- and perform only any remaining lawful cleanup that does not mutate the terminal state.

There is no second winner.

### Shared-slot `PENDING` race law

For a shared slot in `PENDING`, the contenders may include:

- success publication,
- shared failure,
- drop terminalization.

If `PENDING -> SUCCESS` wins first:

- the slot is terminally successful,
- later drop or failure attempts must not overwrite it,
- region-level cleanup may still proceed without mutating the shared terminal state.

If `PENDING -> DROPPED` wins first:

- publication must not subsequently produce `SUCCESS`,
- builder paths must observe the dropped terminal state,
- and any attached waiters must converge toward lawful terminalization consistent with drop.

If `PENDING -> FAILED` wins first:

- subsequent success or drop attempts must not overwrite the failed terminal state.

### Attach vs close/terminalization law

Attach admission and terminal shared-slot closure must be governed as one atomic lifecycle concern.

A compliant implementation MUST ensure that no race may produce the following illegal outcome:

- waiter admission succeeds semantically,
- but the shared slot has already terminally closed in a way that makes that admission unreachable or orphaned.

If attach wins before close/terminal publication becomes authoritative, the waiter is lawfully attached and must later
receive exactly one terminal waiter outcome.

If close/terminal publication wins before attach succeeds, attach must fail and no attached waiter may be created.

The exact packing or field layout used to enforce this law belongs to the design note.  
The law itself is normative here.

### Timeout vs completion law

Timeout and completion may race only through waiter-terminal authority.  
Whichever waiter-terminal transition wins first becomes final for that waiter.

A completion path losing the race to timeout may still observe the waiter as terminal, but may not overwrite that waiter
state.

A timeout path losing the race to resume may still perform non-mutating cleanup, but may not overwrite that waiter
state.

### Cancellation vs completion law

Cancellation and completion follow the same law as timeout vs completion.  
Exactly one waiter terminal state wins.

### Builder liveness vs publication law

Loss of builder progress, supervisory force-abort, and commit-right arbitration may interact, but only one lawful
convergence path may win.

A builder that loses commit-right must not be allowed to enter authoritative publication later through stale control
flow.

### Delivery failure law

Failure to enqueue or deliver an observation does not invalidate terminal lifecycle truth.

It is an infrastructure-plane failure and must be handled without reopening lifecycle state.

### Fresh-session restart race law (AMENDED)

The following events may race at the planning-run boundary:

- joined completion becomes available,
- current session exits through cleanup,
- restart admission is attempted,
- runtime-boundary abort wins.

A compliant implementation MUST ensure:

- no resumed execution starts before the previous worker-local session has lawfully exited,
- no more than one fresh restarted session may enter `RUNNING` for the same `PlanningRunEpoch`,
- no stale restart signal may reopen a planning run already in `COMPLETED` or `ABORTED`,
- and no stale signal may pin a different `RuntimePolicyEpoch` for the same planning run.

---

## Ordering and Visibility Law

### Publication-before-completion

`SUCCESS` is not merely a logical marker.  
It is a visibility contract.

A shared slot MUST NOT transition to `SUCCESS` until the committed winner is already authoritative and visible from the
bucket path.

### Release / acquire semantics

A compliant implementation MUST guarantee that successful publication is paired with visibility semantics strong enough
to ensure the following law:

- the success transition performs the equivalent of release publication for the committed winner,
- any resumed waiter performs the equivalent of acquire observation before consuming the winner.

This ADR does not freeze the exact JVM primitive used to implement that ordering.  
However, the law itself is normative.

### Re-verification law

A resumed waiter must not trust “success” blindly.  
It MUST re-verify through the authoritative committed bucket path.

If a shared terminal signal that implies publication visibility is observed but the committed winner is not visible
through the bucket path, this is a protocol integrity failure.

### Panic / isolation law for re-verification failure

A re-verification failure is not an ordinary cache miss, transient coordination glitch, or recoverable attach outcome.

It is an integrity-class failure.

At minimum, a compliant implementation MUST:

- fail the current operation/session,
- isolate the containing shard/region from further mutable lifecycle admission,
- propagate a panic-grade signal to partition governance,
- and force the region/partition into a state functionally equivalent to circuit-open or stronger isolation.

The exact operational recovery path may be refined by policy and implementation documents, but the minimum semantic
response is isolation, not silent degradation.

### Verification feasibility note

This law cannot be treated as a trivial unit-test property.  
Its verification requires stress-oriented concurrency testing and memory-ordering-sensitive validation at the runtime
level.

Therefore the verification strategy for this law MUST include, at minimum:

- repeated stress execution under contention,
- race-focused lifecycle tests,
- and a memory-ordering-sensitive harness or equivalent tooling appropriate for the JVM execution model.

The exact harness/tool choice belongs to the design note and verification plan, but the requirement to validate this law
beyond ordinary unit tests is normative.

### Fresh-session restart ordering law (AMENDED)

If joined-waiter completion is consumed through fresh-session restart, the following order is normative:

1. joined completion becomes lawfully visible through waiter-terminal and run-boundary signaling,
2. the currently active `PlannerSession` exits through ordinary cleanup,
3. the planning run transitions to `READY_TO_RESTART`,
4. a fresh worker-local session is acquired,
5. resumed execution re-enters `RUNNING` under the same `PlanningRunEpoch`,
6. the resumed session observes the same pinned `RuntimePolicyEpoch` as the original run.

This law does not require new physical array/slab allocation for each restart.  
It requires fresh worker-local session semantics, not mandatory fresh backing allocation.

---

## Physical Sealing and Irreversibility

Terminalization must be physically sealed, not merely conceptually terminal.

After a terminal shared-slot transition wins:

- all write-access handles to shared-slot terminal state are considered invalid,
- no stale callback may modify the slot again,
- no timeout path may overwrite success,
- no cancellation path may overwrite failure,
- and no drop path may re-terminalize an already terminal slot.

After a terminal waiter transition wins:

- all further waiter terminalization attempts are invalid,
- stale timeout callbacks and stale completion callbacks must be ignored.

After a terminal builder-handle transition wins:

- all further commit/abort writes are invalid.

After a commit-right transition reaches `RELEASED`:

- no later contender may lawfully enter authoritative publication for that publication episode.

After a planning run reaches `COMPLETED` or `ABORTED`:

- no restart signal may reopen it,
- no stale completion signal may transition it back to `RUNNING`,
- and no new `RuntimePolicyEpoch` may be pinned for that run.

Terminal slots, waiters, handles, and arbitration surfaces may remain readable.  
They must not remain writable.

This is the runtime equivalent of hard sealing.

---

## Mechanical Constraints

The exact mechanism will be specified in a design note.  
However, any compliant implementation MUST satisfy the following hot-path mechanical constraints:

- state storage on the hot path MUST be primitive-field based,
- hot-path state transitions MUST be allocation-free,
- terminalization MUST complete without creating new state objects,
- terminal-state observation MUST not require boxing-based indirection,
- the implementation MUST be compatible with CAS-style atomic transition enforcement,
- attach admission and terminal-slot closure MUST be physically coordinated strongly enough to prevent zombie waiter
  creation,
- commit-right arbitration MUST be exact-once and physically separate from mere build permission,
- builder supervision MUST guarantee convergence rather than indefinite passive waiting,
- and state-bearing hot fields MUST be layout-isolated strongly enough to prevent correctness or performance drift from
  false sharing under expected contention.

A compliant implementation MAY satisfy layout isolation using padding, `@Contended`, equivalent field separation, packed
primitive fields, or another mechanically sound JVM-compatible strategy.  
The exact strategy belongs to the design note.

A compliant implementation using fresh-session restart MUST additionally satisfy:

- planning-run state storage remains distinct from worker-local primitive planner backing,
- worker-backing freshness/versioning remains distinct from planning-run continuity,
- `L2HostGeneration` remains distinct from both `PlanningRunEpoch` and `WorkerBackingEpoch`,
- and no restart protocol may require retaining a half-live worker-local backing merely to preserve logical run
  continuity.

This ADR intentionally does not freeze one exact API surface such as `VarHandle` or `AtomicLongFieldUpdater`, nor one
exact packed-field layout.  
Those decisions belong to the implementation design document.

---

## Reason / Policy / Sub-Machine Separation

Top-level lifecycle states are closed.  
Reasons, policies, and orthogonal sub-machines are open.

The following are expected to evolve without changing the top-level lifecycle states:

- attach rejection reasons,
- failure reasons,
- drop reasons,
- cancellation reasons,
- speculative admission policy,
- supervisory builder timeout/deadline policy,
- retry/degrade policy,
- timeout scheduling strategy,
- dispatch strategy,
- telemetry-driven calibration.

If a future feature cannot be modeled by extending reason taxonomies or by adding an orthogonal sub-machine, only then
may a new top-level lifecycle state be considered.

Adding a new top-level lifecycle state is a constitutional change.

Adding a new planning-run resume site, new immutable resume payload schema, or refined worker-backing reset mechanics
does not by itself require a new top-level lifecycle state, provided the existing state meanings remain stable.

---

## Evolution Policy

The following changes are ordinary refactors and do **not** require ADR amendment:

- changing timeout scheduler implementation,
- changing completion dispatch implementation,
- changing primitive layout details while preserving laws,
- changing failure-reason taxonomy,
- changing attach-rejection reason taxonomy,
- adding or refining a speculative-lease policy,
- refining supervisory deadlines without changing builder-handle meaning,
- adding an orthogonal sub-machine,
- changing telemetry or governance calibration,
- adding new immutable planning-run resume-site payload schemas while preserving existing run-state meanings,
- refining worker-backing reset mechanics while preserving the fresh-session restart law,
- refining `L2HostGeneration` mechanics while preserving its episode-discriminator meaning.
- changing the meaning of `INITIALIZED` or `PANIC_ISOLATED`,
- collapsing `PANIC_ISOLATED` back into ordinary `ABORTED`,
- or removing the explicit initial-admission state while queueing/admission remains a supported runtime behavior,

The following changes **do** require ADR amendment:

- adding or removing a top-level lifecycle state,
- changing the meaning of `SUCCESS`, `FAILED`, `DROPPED`, `ATTACHED`, `RESUMED`, `TIMED_OUT`, `CANCELLED`, `OPEN`,
  `COMMITTED`, `ABORTED`, `UNCLAIMED`, `CLAIMED`, `RELEASED`, `OPEN`, `CLOSE_PUBLISHED`, `RECLAIMED`,
  `RUNNING`, `SUSPENDED_ON_JOIN`, `READY_TO_RESTART`, `COMPLETED`, or `ABORTED`,
- changing publication-before-completion law,
- changing exact-once terminalization law,
- allowing waiter-local timeout/cancel to mutate shared-slot terminal state,
- allowing more than one terminal waiter outcome,
- changing the single terminalization authority rule,
- weakening the region close-before-reclaim invariant,
- weakening builder liveness convergence,
- allowing build permission and publication permission to collapse back into one undifferentiated authority,
- collapsing `RuntimePolicyEpoch`, `PlanningRunEpoch`, `WorkerBackingEpoch`, and `L2HostGeneration` back into one
  semantically ambiguous version axis,
- or allowing one planning run to drift across more than one pinned `RuntimePolicyEpoch`.

---

## Normative Transition Matrix

This matrix has normative force.

Any transition not listed here is illegal.

### Shared slot matrix

| Current State | Event                                                          | Next State | Legal |
|---------------|----------------------------------------------------------------|------------|-------|
| `PENDING`     | builder publish succeeds and committed winner is authoritative | `SUCCESS`  | Yes   |
| `PENDING`     | shared-slot terminal failure                                   | `FAILED`   | Yes   |
| `PENDING`     | partition close/drop terminalization                           | `DROPPED`  | Yes   |
| `SUCCESS`     | any terminal event                                             | —          | No    |
| `FAILED`      | any terminal event                                             | —          | No    |
| `DROPPED`     | any terminal event                                             | —          | No    |

### Waiter matrix

| Current State | Event                                        | Next State                         | Legal |
|---------------|----------------------------------------------|------------------------------------|-------|
| not attached  | attach accepted                              | `ATTACHED`                         | Yes   |
| not attached  | attach rejected                              | no waiter lifecycle object created | Yes   |
| `ATTACHED`    | observe authoritative shared terminal signal | `RESUMED`                          | Yes   |
| `ATTACHED`    | waiter-local timeout wins                    | `TIMED_OUT`                        | Yes   |
| `ATTACHED`    | waiter-local cancellation wins               | `CANCELLED`                        | Yes   |
| `RESUMED`     | any terminal event                           | —                                  | No    |
| `TIMED_OUT`   | any terminal event                           | —                                  | No    |
| `CANCELLED`   | any terminal event                           | —                                  | No    |

### Builder handle matrix

| Current State | Event                        | Next State  | Legal |
|---------------|------------------------------|-------------|-------|
| `OPEN`        | builder commit wins          | `COMMITTED` | Yes   |
| `OPEN`        | builder abort wins           | `ABORTED`   | Yes   |
| `OPEN`        | supervisory force-abort wins | `ABORTED`   | Yes   |
| `COMMITTED`   | any terminal event           | —           | No    |
| `ABORTED`     | any terminal event           | —           | No    |

### Commit-right matrix

| Current State | Event                                       | Next State | Legal |
|---------------|---------------------------------------------|------------|-------|
| `UNCLAIMED`   | one contender wins publication authority    | `CLAIMED`  | Yes   |
| `CLAIMED`     | authoritative publication episode completes | `RELEASED` | Yes   |
| `RELEASED`    | any new publication contender               | —          | No    |

### Partition region matrix

| Current State     | Event                                                                               | Next State        | Legal |
|-------------------|-------------------------------------------------------------------------------------|-------------------|-------|
| `OPEN`            | close publication becomes authoritative                                             | `CLOSE_PUBLISHED` | Yes   |
| `CLOSE_PUBLISHED` | reclamation completes after lawful terminalization convergence and grace completion | `RECLAIMED`       | Yes   |
| `RECLAIMED`       | any mutable lifecycle event                                                         | —                 | No    |

### Speculative lease matrix

| Current State | Event                                                     | Next State | Legal |
|---------------|-----------------------------------------------------------|------------|-------|
| `ISSUED`      | slot terminalizes or lease is otherwise lawfully released | `RELEASED` | Yes   |
| `RELEASED`    | any mutable lease event                                   | —          | No    |

### Planning-run matrix (AMENDED)

| Current State       | Event                                        | Next State          | Legal |
|---------------------|----------------------------------------------|---------------------|-------|
| `INITIALIZED`       | first worker-session admission wins          | `RUNNING`           | Yes   |
| `INITIALIZED`       | ordinary run-boundary abort wins             | `ABORTED`           | Yes   |
| `INITIALIZED`       | panic-grade isolation wins                   | `PANIC_ISOLATED`    | Yes   |
| `RUNNING`           | joined-wait suspension becomes authoritative | `SUSPENDED_ON_JOIN` | Yes   |
| `RUNNING`           | final semantic result becomes authoritative  | `COMPLETED`         | Yes   |
| `RUNNING`           | ordinary run-boundary abort wins             | `ABORTED`           | Yes   |
| `RUNNING`           | panic-grade isolation wins                   | `PANIC_ISOLATED`    | Yes   |
| `SUSPENDED_ON_JOIN` | joined completion becomes restart-eligible   | `READY_TO_RESTART`  | Yes   |
| `SUSPENDED_ON_JOIN` | ordinary run-boundary abort wins             | `ABORTED`           | Yes   |
| `SUSPENDED_ON_JOIN` | panic-grade isolation wins                   | `PANIC_ISOLATED`    | Yes   |
| `READY_TO_RESTART`  | fresh-session restart admission wins         | `RUNNING`           | Yes   |
| `READY_TO_RESTART`  | ordinary run-boundary abort wins             | `ABORTED`           | Yes   |
| `READY_TO_RESTART`  | panic-grade isolation wins                   | `PANIC_ISOLATED`    | Yes   |
| `COMPLETED`         | any state-changing event                     | —                   | No    |
| `ABORTED`           | any state-changing event                     | —                   | No    |
| `PANIC_ISOLATED`    | any state-changing event                     | —                   | No    |

### Race arbitration matrix

| Contention Case                                | Winner Rule                                                                        | Loser Rule                                                                                  |
|------------------------------------------------|------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `PENDING->SUCCESS` vs `PENDING->FAILED`        | first successful shared-slot terminal CAS wins                                     | losing contender observes terminal state and becomes a no-op for shared-slot terminal state |
| `PENDING->SUCCESS` vs `PENDING->DROPPED`       | first successful shared-slot terminal CAS wins                                     | losing contender may perform cleanup but may not overwrite terminal state                   |
| `ATTACHED->RESUMED` vs `ATTACHED->TIMED_OUT`   | first successful waiter-terminal CAS wins                                          | losing contender must not overwrite waiter state                                            |
| `ATTACHED->RESUMED` vs `ATTACHED->CANCELLED`   | first successful waiter-terminal CAS wins                                          | losing contender must not overwrite waiter state                                            |
| `OPEN->COMMITTED` vs `OPEN->ABORTED`           | first successful builder-handle terminal authority wins                            | losing contender must not overwrite handle state                                            |
| `UNCLAIMED->CLAIMED` among multiple contenders | first successful commit-right arbitration wins                                     | losing contenders must not enter authoritative publication                                  |
| attach success vs close/terminal publication   | whichever authoritative transition wins first determines whether waiter is created | loser must not create a contradictory second outcome                                        |
| `READY_TO_RESTART->RUNNING` vs run abort       | first successful planning-run state transition wins                                | losing contender must not reopen or duplicate run execution                                 |

---

## Verification Consequences

The following verification obligations become mandatory.

1. Waiter timeout does not fail the shared slot.
2. Waiter cancellation does not cancel the shared slot.
3. Attach success implies exactly one terminal waiter outcome.
4. Attach rejection does not create a waiter lifecycle object.
5. Publication-before-completion is observable through authoritative bucket re-verification.
6. Re-verification failure triggers panic/isolation rather than silent degrade.
7. Shared-slot failure converges visible attached waiters to lawful terminal waiter outcomes.
8. Partition drop terminalizes visible attached waiters before reclamation completes.
9. Terminal shared slots reject zombie writes.
10. Terminal waiters reject zombie terminal callbacks.
11. Builder handles terminalize exactly once.
12. Supervisory force-abort closes orphaned builder authority.
13. Commit-right arbitration admits exactly one authoritative publication winner.
14. Speculative leases are force-released on slot terminalization.
15. Region close publication happens-before reclamation.
16. `RECLAIMED` is not reachable before grace completion.
17. Delivery failure or delay does not rewrite lifecycle truth.
18. Any transition absent from the normative matrix is rejected as illegal.
19. Cost-center traces remain consistent with the lifecycle model, but cost-center metering is not itself a substitute
    for lifecycle law.
20. Visibility law is validated through stress-oriented concurrency testing rather than ordinary unit tests alone.
21. One logical planning run does not drift across more than one pinned `RuntimePolicyEpoch`.
22. Fresh-session restart does not retain worker-local mutable planner substrate across suspension.
23. `WorkerBackingEpoch` freshness is not used as a substitute for `PlanningRunEpoch` continuity.
24. Stale `L2HostGeneration` signals do not reopen a terminated planning run.
25. Any planning-run transition absent from the normative matrix is rejected as illegal.
26. `INITIALIZED` planning runs do not retain an active worker-session lease before first admission.
27. `PANIC_ISOLATED` planning runs do not admit restart and require stronger isolation semantics than ordinary
    `ABORTED`.
28. Panic-grade terminalization during `SUSPENDED_ON_JOIN` cancels the current suspension handle and closes restart
    reachability.

---

## Implementation Consequences

The codebase must introduce explicit lifecycle state definitions and lifecycle transition authorities.

At minimum, the implementation will require explicit state types for:

- shared-slot lifecycle,
- waiter lifecycle,
- builder-handle lifecycle,
- commit-right arbitration lifecycle,
- partition-region lifecycle,
- speculative-lease lifecycle where applicable,
- and planning-run orchestration lifecycle where fresh-session restart is enabled.

The existing in-flight coordination structure must be upgraded into a true lifecycle owner rather than remaining a thin
coordination helper.

The shard-level coordinator must be reduced to orchestration, routing, publication, authoritative re-verification, and
interaction with lifecycle authorities.  
It must not remain the hidden owner of lifecycle truth.

The runtime boundary must additionally introduce explicit objects or equivalents for:

- one planning-run context owning `PlanningRunEpoch`,
- one pinned `RuntimePolicyEpoch` reference for that run,
- immutable resume-point descriptors for restart sites,
- and worker-backing freshness/versioning separate from the run epoch.

A dedicated design note will define:

- primitive field layout,
- state encoding,
- CAS mechanics,
- visibility primitives,
- false-sharing mitigation strategy,
- attach-admission / slot-closure atomic coordination strategy,
- builder supervision mechanics,
- commit-right arbitration mechanics,
- grace-barrier reclamation mechanics,
- lifecycle-host layout constraints,
- run-context and resume-point mechanics,
- worker-backing freshness/versioning mechanics,
- and fresh-session restart compatibility with primitive-array / slab reuse.

---

## Migration Plan

### Phase A — Authority declaration

Adopt this ADR and freeze the lifecycle law.

### Phase B — State introduction

Introduce explicit lifecycle state definitions and transition authorities.

### Phase C — Slot upgrade

Upgrade the in-flight coordination object into an explicit lifecycle host.

### Phase D — Arbitration upgrade

Separate build execution from commit-right arbitration and introduce builder supervision.

### Phase E — Adapter refactor

Reduce shard logic to orchestration and move terminalization legality into lifecycle authorities.

### Phase F — Planning-run orchestration introduction (AMENDED)

Introduce:

- planning-run orchestration state,
- `PlanningRunEpoch`,
- pinned `RuntimePolicyEpoch` continuity for one logical planning run,
- immutable resume-point descriptors,
- and explicit restart admission law.

### Phase G — Dispatch attachment

Attach timeout scheduling and completion dispatch only after lifecycle authority is centralized and fresh-session
restart
semantics are fixed.

### Phase H — Verification closure

Update tests, golden vectors, and race coverage so that all lifecycle law is mechanically enforced.

### Phase I — Mechanical closure

Publish the companion design note defining low-level layout, CAS encoding, visibility mechanics, attach/close atomic
coordination, builder supervision, commit-right arbitration, grace-barrier reclamation, run-context / resume-point
mechanics, worker-backing freshness/versioning, and false-sharing mitigation strategy consistent with this ADR.

---

## Rejected Alternatives

### Keep cost-center specification as the only lifecycle authority

Rejected because cost-center metering does not define legal transitions, exact-once terminalization, or visibility
ordering.

### Keep lifecycle implicit inside `CompletableFuture`

Rejected because future semantics alone do not define waiter-local timeout independence, attach/drop linearizability,
terminalization authority, builder supervision, commit-right arbitration, or zombie-write sealing.

### Allow top-level state growth as the default evolution mechanism

Rejected because it weakens invariants and turns routine operational evolution into uncontrolled semantic expansion.

### Freeze one exact JVM primitive strategy in the ADR

Rejected because the ADR should define lifecycle law and implementation constraints, while the design note should define
the exact runtime mechanical strategy.

### Collapse build permission and publication permission into one authority

Rejected because speculative or duplicate-builder scenarios require build execution and authoritative publication to
remain distinct.

### Reuse `PolicyEpoch` as the logical planning-run epoch (AMENDED)

Rejected because runtime-policy snapshot version and logical planning-run continuity are distinct semantic axes.

`RuntimePolicyEpoch` belongs to governance snapshot publication.  
`PlanningRunEpoch` belongs to run continuity across suspend / restart.

Collapsing them would make policy publication semantics and run-orchestration semantics ambiguous.

### Require fresh physical backing allocation for every fresh-session restart (AMENDED)

Rejected because fresh-session restart requires fresh worker-local session semantics, not mandatory fresh
primitive-array
or slab allocation.

Worker-backing freshness/versioning must remain a distinct axis from logical run continuity.

---

## Consequences

### Positive

The runtime gains an explicit, enumerable, verifiable lifecycle law.  
The implementation becomes more compatible with DDD, hexagonal architecture, and compiler-style determinism.  
Future operational changes can be absorbed without destabilizing the semantic core.

The runtime also gains explicit separation among:

- runtime-policy snapshot version,
- planning-run continuity,
- worker-backing freshness,
- and L2 lifecycle-host generation,

which reduces semantic drift across future optimization waves.

### Negative

Implementation complexity increases.  
Infrastructure must carry stronger lifecycle ownership.  
Verification burden increases substantially.

### Neutral

This ADR changes where lifecycle truth is stored and enforced.  
It does not change exact-match authority, canonical equality, or semantic result correctness.

---

## Final Normative Statement

Kontrakt SHALL treat L2 join lifecycle semantics as an explicit dual-axis state machine with single terminalization
authority.

Where fresh-session restart is enabled, Kontrakt SHALL additionally treat planning-run orchestration as an explicit
orthogonal runtime-boundary lifecycle axis.

Cost-center metering remains a separate protocol vocabulary.  
Lifecycle legality, exact-once terminalization, visibility ordering, irreversibility, race arbitration, region-aware
terminalization, builder supervision, publication-right arbitration, grace-aware reclamation, planning-run continuity,
runtime-policy pinning, and panic-grade integrity handling become explicit runtime law.

From this ADR onward, any implementation that relies on implicit future semantics, polling-era coordination as lifecycle
truth, fragmented terminalization authority, undefined race resolution, silent degradation after integrity failure,
indefinite orphaned builder authority, conflation of build permission with publish permission, conflation of
`RuntimePolicyEpoch` with `PlanningRunEpoch`, conflation of `PlanningRunEpoch` with `WorkerBackingEpoch`, or conflation
of governance snapshot version with L2 host generation is non-compliant.