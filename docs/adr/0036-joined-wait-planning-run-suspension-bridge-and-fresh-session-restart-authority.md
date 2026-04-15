# ADR-0036: Joined-Wait Planning-Run Suspension Bridge and Fresh-Session Restart Authority

- Status: Proposed
- Date: 2026-04-15
- Related:
    - ADR-0034: Explicit dual-axis L2 join lifecycle and single terminalization authority
    - ADR-0035: Deterministic balanced lanes for Tier-2 join completion delivery

---

## Context

ADR-0034 already defines the high-level planning-run lifecycle around joined wait:

- `RUNNING -> SUSPENDED_ON_JOIN`
- `SUSPENDED_ON_JOIN -> READY_TO_RESTART`
- `READY_TO_RESTART -> RUNNING`

It also fixes the architectural direction:

- joined wait must release the current worker/session
- completion must arrive asynchronously
- restart must occur through a fresh session boundary
- the same `PlanningRunEpoch` must continue across suspension and restart
- the pinned runtime-policy snapshot must not drift during the run

However, one concrete runtime-boundary problem remains under-specified:

> Who is allowed to publish `READY_TO_RESTART`, through which object, and by what bridge?

Without a dedicated bridge design, the implementation risks collapsing important authority boundaries:

- the dispatch plane may start mutating planning-run state directly
- `JoinHandle` may leak into orchestration code as an adapter detail
- already-ready and asynchronously-ready cases may diverge semantically
- restart admission may not carry the operational handle needed to consume the result in the fresh session
- one-shot readiness publication may become race-prone or duplicated

This ADR closes that gap.

---

## Decision

We introduce a dedicated runtime-boundary bridge between:

- adapter-owned joined-wait completion delivery
- planning-run orchestration lifecycle
- fresh-session restart admission

The bridge is built around the following rules.

### 1. `JoinHandle` does not directly become orchestration state

The raw adapter-level `JoinHandle` is not stored as planning-run orchestration state directly.

Instead, it is wrapped behind a runtime-boundary abstraction:

- `PlanningRunSuspensionHandle`

This prevents adapter-specific pending-join mechanics from leaking upward into planning-run orchestration.

### 2. `PlanningRunSuspensionHandle` is the runtime-boundary abstraction for one suspended join episode

`PlanningRunSuspensionHandle` is responsible for:

- one-shot registration of a readiness callback
- fresh-session result consumption
- best-effort cancellation
- exposing the monotonic deadline for the suspended episode

This abstraction is operational runtime state, not immutable resume-point data.

### 3. `PlanningResumePoint` and `PlanningRunSuspensionHandle` are separate axes

The bridge must keep these two things separate:

- `PlanningResumePoint`
    - immutable semantic restart location
- `PlanningRunSuspensionHandle`
    - operational runtime handle for joined-wait completion

They must not be collapsed into one type.

### 4. `READY_TO_RESTART` publication authority belongs to the runtime bridge, not to the dispatch plane

The dispatch plane is allowed to do exactly this:

- execute the registered continuation callback

The dispatch plane is **not** allowed to mutate planning-run lifecycle state directly.

The runtime bridge owns the translation:

- adapter callback delivery
- into planning-run readiness publication

Concretely:

1. the dispatch plane executes the one-shot continuation callback
2. the continuation callback enters the runtime bridge
3. the runtime bridge calls `PlanningRunContext.tryMarkReadyToRestart()`

Therefore:

- the dispatch plane is the delivery executor
- the runtime bridge is the readiness publisher
- `PlanningRunContext` remains the lifecycle host

### 5. `READY_TO_RESTART` publication is one-shot

The bridge must collapse all duplicate readiness races into one runtime-visible publication.

This applies to both:

- asynchronous callback delivery
- already-ready registration cases

Exactly one successful publication into the planning-run lifecycle is allowed per suspended join episode.

### 6. Already-ready and asynchronously-ready cases are semantically unified

Two cases exist:

- terminal truth is already visible when callback registration occurs
- terminal truth becomes visible later and arrives asynchronously

Both must converge to the same planning-run lifecycle outcome:

- `SUSPENDED_ON_JOIN -> READY_TO_RESTART`

They differ only in timing, not in semantic meaning.

### 7. Fresh-session restart is mandatory

Restart after joined wait must occur only through a fresh session boundary.

Forbidden:

- same-session resume after joined wait readiness
- restarting on a stale worker-local session
- mutating the suspended worker-local session back into execution state

The bridge exists specifically to preserve this boundary.

### 8. Restart admission must carry both immutable and operational restart material

`PlanningRunRestartAdmission` must carry:

- `PlanningRunEpoch`
- pinned `RuntimePolicyEpoch`
- `PlanningResumePoint`
- carried-forward `PlanningRunRemainingBudget`
- `PlanningRunSuspensionHandle`
- fresh worker/session lease

This is required because the restarted execution needs both:

- immutable semantic location
- operational handle to consume the ready result

### 9. Pinned runtime policy and remaining run budget are carried forward unchanged

The bridge must not re-resolve runtime policy on readiness publication.

The following are carried forward from the suspended run:

- the same `PlanningRunEpoch`
- the same pinned `RuntimePolicyEpoch`
- the same run-scoped remaining budget ledger

Fresh-session restart means a fresh worker/session boundary, not a new logical run.

### 10. Cancellation remains best-effort and does not revoke published readiness retroactively

If cancellation loses a race against readiness publication:

- already-published readiness is not revoked
- the run may still restart and then consume the ready result lawfully

The bridge must not attempt retroactive un-publication of `READY_TO_RESTART`.

### 11. The bridge is runtime-boundary infrastructure, not domain semantic authority

This bridge belongs to runtime/orchestration infrastructure.

It does not own:

- semantic planner laws
- shared-slot truth
- waiter truth
- builder truth
- lane delivery truth

It owns only the runtime-boundary translation from joined completion delivery into planning-run restart readiness.

---

## Required Types

At minimum, the implementation must define the following runtime-boundary types.

### `PlanningRunSuspensionHandle`

Responsibilities:

- `registerReadyToRestartCallback(onReadyToRestart)`
- `consumeReadyResult(session)`
- `cancel(cause)`
- `deadlineNanos()`

### `PlanningRunSuspension`

Responsibilities:

- carry `PlanningResumePoint`
- carry `PlanningRunSuspensionHandle`

### `PlanningRunRestartAdmission`

Responsibilities:

- carry fresh worker/session lease
- carry pinned runtime-policy epoch
- carry same run epoch
- carry same remaining budget
- carry resume point
- carry suspension handle

### `PlanningRunJoinBridge`

Responsibilities:

- wrap `JoinHandle` into `PlanningRunSuspensionHandle`
- install the one-shot readiness callback
- suspend the `PlanningRunContext`
- publish readiness into `PlanningRunContext.tryMarkReadyToRestart()`

---

## Authority Split

### Adapter / dispatch plane owns

- joined completion delivery execution
- callback invocation timing
- no direct planning-run state mutation

### Runtime bridge owns

- callback-to-readiness translation
- one-shot publication discipline
- hiding adapter pending-join details from orchestration

### `PlanningRunContext` owns

- planning-run lifecycle state
- suspension slot ownership
- restart admission ownership
- run epoch continuity
- remaining budget continuity
- pinned runtime-policy continuity

---

## Invariants

The following invariants must hold.

1. At most one active suspension exists per `PlanningRunContext`.
2. At most one successful readiness publication exists per suspended join episode.
3. `READY_TO_RESTART` may be published only from `SUSPENDED_ON_JOIN`.
4. Restart admission may be produced only from `READY_TO_RESTART`.
5. Restart continues the same `PlanningRunEpoch`.
6. Restart uses the same pinned `RuntimePolicyEpoch`.
7. Restart carries forward the same remaining run budget ledger.
8. `consumeReadyResult(session)` must occur only through the fresh restart session.
9. The dispatch plane never mutates `PlanningRunContext` directly.
10. `JoinHandle` does not leak as raw orchestration state beyond the bridge boundary.

---

## Rejected Alternatives

### A. Let the dispatch plane mutate `PlanningRunContext` directly

Rejected because it would:

- blur adapter-delivery authority and orchestration authority
- couple dispatch infrastructure to planning-run lifecycle host details
- weaken lifecycle clarity
- make testing and reasoning harder

### B. Store raw `JoinHandle` directly in orchestration state

Rejected because it would:

- leak adapter-level pending-join details upward
- weaken the runtime-boundary abstraction
- make fresh-session restart consumption less explicit

### C. Resume on the same worker/session after readiness

Rejected because it violates the fresh-session restart rule and reintroduces exactly the session-coupling that the
joined-wait architecture was designed to remove.

### D. Treat already-ready as a separate semantic restart path

Rejected because already-ready and asynchronously-ready differ only in timing.
They must converge to the same lifecycle law.

---

## Consequences

### Positive

- closes the missing runtime-boundary authority for `READY_TO_RESTART`
- preserves adapter/orchestration separation
- makes fresh-session restart consumption explicit
- keeps run epoch, pinned policy, and remaining budget continuity mechanically visible
- unifies already-ready and asynchronous-ready semantics
- fits the existing closed-state discipline of the planning runtime

### Negative

- introduces another runtime-boundary abstraction layer
- increases orchestration code volume
- requires careful one-shot race handling around callback publication

### Neutral / Expected

- dispatch design remains separate from planning-run orchestration design
- detailed callback timing, lease handoff mechanics, and restart execution sequencing remain design-level concerns, not
  ADR-level algorithm text

---

## Implementation Guidance

The implementation should prefer the following shape:

`````kotlin
interface PlanningRunSuspensionHandle {
    fun registerReadyToRestartCallback(
        onReadyToRestart: () -> Unit,
    ): PlanningRunSuspensionRegistrationDecision

    fun consumeReadyResult(
        session: PlannerSession,
    ): JoinResumeStep

    fun cancel(cause: Throwable): Boolean

    fun deadlineNanos(): Long
}
`````

`````kotlin
object PlanningRunJoinBridge {
    fun suspendOnJoin(
        context: PlanningRunContext,
        activeLease: PlanningRunWorkerSessionLease,
        resumePoint: PlanningResumePoint,
        joinHandle: JoinHandle,
    ): PlanningRunSuspensionRegistrationDecision
}
`````

The bridge callback should converge to a form equivalent to:

`````kotlin
suspensionHandle.registerReadyToRestartCallback {
    context.tryMarkReadyToRestart()
}
`````

The exact implementation may vary, but the authority split defined by this ADR must remain intact.

---

## Final Statement

Joined-wait completion delivery and planning-run restart readiness are connected, but they are not the same authority.

ADR-0036 therefore ratifies a dedicated runtime-boundary bridge:

- dispatch plane delivers readiness
- bridge publishes planning-run readiness
- planning-run context owns lifecycle transition
- fresh-session restart consumes the ready result

That separation is required to keep joined wait:

- non-blocking
- authority-clean
- fresh-session safe
- and consistent with the broader closed-state discipline of Kontrakt.