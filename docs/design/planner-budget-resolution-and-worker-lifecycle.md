# Design Note: Planner Budget Resolution and Worker Lifecycle

Date: 2026-03-14

Status: Accepted
<!-- AMENDED(2026-03-21): Clarified attach terminal-signal completeness, post-insertion attach reconciliation,
completion-continuation execution-path safety, builder-handle terminalization discipline,
speculative-builder reservation release, and close-gate terminalization completeness
without changing previously accepted runtime-policy semantics. -->
<!-- AMENDED(2026-03-21): Corrected policy snapshot reference shapes to remove data-class / copy()-backdoor examples
for the policy snapshot family, and removed require()-style validation from illustrative runtime-policy registry code. -->
<!-- AMENDED(2026-03-21): Clarified request-bounded restart semantics for joined waiters, explicit boundary-orchestrated
fresh-session restart, slot-owned speculative leases, and wall-clock separation from protocol fuel / waiter-local join
governance. -->

## Overview

This note bridges:

- `ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics`
- `docs/design/l1-planner-session-primitive-data-structures.md`
- `docs/design/l2-plan-interner-partitioned-tier2-with-governance.md`

It defines:

1. how resolved runtime budget becomes concrete planner caps,
2. how worker-local planner state is acquired / used / reset / returned,
3. what is semantic output vs internal implementation detail,
4. how L1 structural budgets and L2 governance budgets remain separate but compatible.

This note does **not** redefine the primitive byte ledger itself.
The primitive byte ledger remains normative in the L1 planner-session design note.

---

## 1. Scope and Responsibility Split

### 1.1 This note defines

- `ResolvedSessionBudget`
- `ResolvedPlannerSessionCaps`
- budget-to-capacity resolution flow
- worker lifecycle contract
- quarantine / reset-failure behavior
- semantic output boundary

### 1.2 This note does not define

- environment introspection algorithms
- primitive byte ledger formulas
- L2 bucket/shard internals
- canonical identity materialization details

Those remain defined elsewhere.

---

## 2. Core Principle

The planner core MUST remain ignorant of the environment.

The planner core only consumes:

- resolved numeric budgets,
- immutable version tuple,
- implementation-internal resolved calibration.

The planner core MUST NOT:

- read heap state,
- read cgroup limits,
- read container memory,
- inspect CPU count,
- dynamically resize capacities from host state during a session.

---

## 3. Public vs Internal Surface

### 3.1 User / Operator Facing Surface

The default user experience SHOULD remain zero-config.

If any high-level control surface is exposed, it SHOULD remain at the level of:

- `AUTO`
- `SMALL`
- `STANDARD`
- `LARGE`

Users MUST NOT be required to understand or configure internal budget-allocation parameters such as:

- signature reserve ratio,
- preferred node/depth divisor,
- undo density,
- sparse-table scaling constants.

### 3.2 Public / Cross-Boundary Contract

The planner runtime consumes two resolved contracts at the boundary:

- the planner-core structural/runtime budget contract, represented by `ResolvedSessionBudget`, and
- the L2 join/governance contract, represented by `ResolvedJoinGovernance`.

Normative boundary rule:

- `ResolvedSessionBudget` governs planner-core structural execution limits,
- `ResolvedJoinGovernance` governs L2 wait / join / degrade behavior,
- the two contracts MAY be resolved from the same external `ResourceProfile`,
- but they MUST remain semantically distinct and MUST NOT be collapsed into one byte-budget formula.

### 3.3 Internal Calibration Contract

Budget-allocation heuristics MAY still exist,
but they remain implementation-internal calibration and MUST NOT be part of the user-facing or public planner contract.

---

## 4. Resolved Budget Contract

### 4.1 `ResolvedSessionBudget`

`ResolvedSessionBudget` represents the numeric runtime budget already resolved outside the Domain Core.

Required fields:

- `maxPlannerBytesPerWorker: Long`
- `maxPhysicalSteps: Int`
- `maxSemanticWorkUnits: Int`
- `maxSignatureLen: Int`
- `fixedHeadroomBytes: Long`

### 4.2 Invariants

- all fields MUST be immutable once resolved;
- the same resolved budget MUST produce the same planner caps;
- the resolved budget MUST remain fixed for the lifetime of a `PlannerSession`;
- as part of the policy snapshot family, this type SHOULD be factory-issued and MUST NOT rely on `data class`-generated
  `copy()` semantics as a reconstruction backdoor.

### 4.3 Kotlin Reference Shape

```kotlin
class ResolvedSessionBudget private constructor(
    val maxPlannerBytesPerWorker: Long,
    val maxPhysicalSteps: Int,
    val maxSemanticWorkUnits: Int,
    val maxSignatureLen: Int,
    val fixedHeadroomBytes: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxPlannerBytesPerWorker: Long,
            maxPhysicalSteps: Int,
            maxSemanticWorkUnits: Int,
            maxSignatureLen: Int,
            fixedHeadroomBytes: Long,
        ): ResolvedSessionBudget {
            return ResolvedSessionBudget(
                maxPlannerBytesPerWorker = maxPlannerBytesPerWorker,
                maxPhysicalSteps = maxPhysicalSteps,
                maxSemanticWorkUnits = maxSemanticWorkUnits,
                maxSignatureLen = maxSignatureLen,
                fixedHeadroomBytes = fixedHeadroomBytes,
            )
        }
    }
}
```

### 4.4 `ResolvedJoinGovernance`

`ResolvedJoinGovernance` represents already-resolved L2 wait / join / degrade policy.

Required fields:

- `joinWaitTimeoutNanos: Long`
- `maxWaitersPerKey: Int`
- `maxSpeculativeBuildersPerKey: Int`
- `failFastOnQuotaExhaustion: Boolean`

### 4.5 Invariants

- all fields MUST be immutable once resolved;
- `joinWaitTimeoutNanos > 0`;
- `maxWaitersPerKey > 0`;
- `maxSpeculativeBuildersPerKey >= 0`;
- timeout MUST be interpreted as a **monotonic elapsed-time deadline**, not wall-clock time;
- timeout expiration MUST be treated as a **waiter lifecycle event**, not as shared-slot failure;
- as part of the policy snapshot family, this type SHOULD be factory-issued and MUST NOT rely on `data class`-generated
  `copy()` semantics as a reconstruction backdoor.

### 4.6 Kotlin Reference Shape

```kotlin
class ResolvedJoinGovernance private constructor(
    val joinWaitTimeoutNanos: Long,
    val maxWaitersPerKey: Int,
    val maxSpeculativeBuildersPerKey: Int,
    val failFastOnQuotaExhaustion: Boolean,
) {
    companion object {
        @JvmStatic
        fun issue(
            joinWaitTimeoutNanos: Long,
            maxWaitersPerKey: Int,
            maxSpeculativeBuildersPerKey: Int,
            failFastOnQuotaExhaustion: Boolean,
        ): ResolvedJoinGovernance {
            return ResolvedJoinGovernance(
                joinWaitTimeoutNanos = joinWaitTimeoutNanos,
                maxWaitersPerKey = maxWaitersPerKey,
                maxSpeculativeBuildersPerKey = maxSpeculativeBuildersPerKey,
                failFastOnQuotaExhaustion = failFastOnQuotaExhaustion,
            )
        }
    }
}
```

### 4.7 `ResolvedRuntimePolicy`

`ResolvedRuntimePolicy` is the top-level resolved runtime policy bundle passed into the runtime boundary.

````kotlin
class ResolvedRuntimePolicy private constructor(
    val sessionBudget: ResolvedSessionBudget,
    val joinGovernance: ResolvedJoinGovernance,
) {
    companion object {
        @JvmStatic
        fun issue(
            sessionBudget: ResolvedSessionBudget,
            joinGovernance: ResolvedJoinGovernance,
        ): ResolvedRuntimePolicy {
            return ResolvedRuntimePolicy(
                sessionBudget = sessionBudget,
                joinGovernance = joinGovernance,
            )
        }
    }
}
````

Normative rule:

- `ResolvedRuntimePolicy` MAY be created from one external profile,
- but `sessionBudget` and `joinGovernance` MUST remain independently reasoned contracts,
- and L2 timeout/quota policy MUST NOT be back-propagated into L1 byte-ledger math;
- as part of the policy snapshot family, `ResolvedRuntimePolicy` MUST be immutable and SHOULD be factory-issued rather
  than reconstructed through `data class` copy semantics.

---

## 5. Internal Calibration Contract

### 5.1 Purpose

The planner still requires internal calibration to split a resolved numeric budget across:

- signature slab,
- node capacity,
- depth capacity,
- undo capacity,
- structural reserve.

However, these calibration inputs are NOT part of the public contract.

### 5.2 Rules

Internal calibration:

- MUST be deterministic,
- MUST be resolved before hot-path execution,
- MUST remain immutable for the session,
- MUST NOT be exposed as a required user-facing API,
- MUST be diagnosable via internal telemetry or debug reporting.

### 5.3 Kotlin Reference Shape

```kotlin
internal data class ResolvedSizingCalibration(
    val signatureReserveRatio: Double,
    val preferredDepthDivisor: Int,
    val undoRecordsPerDepth: Int,
    val secureWipeOnReset: Boolean = false,
)
```

---

## 6. Concrete Capacity Result

### 6.1 `ResolvedPlannerSessionCaps`

This is the concrete output of capacity solving.

Required fields:

- `maxNodeIdCap`
- `maxDepthCap`
- `indexerTableCap`
- `undoLogCap`
- `maxSignatureBytes`
- `structBudgetBytes`

### 6.2 Kotlin Reference Shape

```kotlin
data class ResolvedPlannerSessionCaps(
    val maxNodeIdCap: Int,
    val maxDepthCap: Int,
    val indexerTableCap: Int,
    val undoLogCap: Int,
    val maxSignatureBytes: Int,
    val structBudgetBytes: Long,
)
```

### 6.3 Invariants

- all caps MUST be positive;
- `maxDepthCap <= Int.MAX_VALUE - 2`;
- `maxSignatureBytes >= maxSignatureLen`;
- all intermediate arithmetic MUST be performed in `Long`;
- any overflow MUST fail closed;
- any impossible minimal layout MUST fail closed.

---

## 7. Capacity Resolution Flow

### 7.1 Conceptual Flow

```text
ResourceProfile
  -> ResolvedSessionBudget
  -> ResolvedSizingCalibration (internal)
  -> Capacity Solver
  -> ResolvedPlannerSessionCaps

ResourceProfile
  -> ResolvedJoinGovernance

ResolvedPlannerSessionCaps + ResolvedJoinGovernance
  -> PlannerSessionConfig + L2GovernanceConfig
```

### 7.2 Determinism Rule

For identical:

- version tuple,
- resolved session budget,
- resolved internal calibration,

the capacity solver MUST produce identical `ResolvedPlannerSessionCaps`.

### 7.3 Desired vs Feasible Rule

The capacity solver MUST distinguish:

- `desiredDepth(nodeCap)` from internal calibration
- `feasibleDepth(nodeCap, structBudget)` from the primitive byte ledger

Normative rule:

```text
targetDepth = min(desiredDepth(nodeCap), feasibleDepth(nodeCap, structBudget))
```

### 7.4 Kotlin Reference Shape

```kotlin
interface PlannerCapacityResolver {
    fun resolve(budget: ResolvedSessionBudget): ResolvedPlannerSessionCaps
}
```

Internal implementations MAY depend on internal calibration, but that dependency is not part of the public contract.

---

## 8. Join Governance State Model

### 8.1 Shared Slot State

`SharedSlotState` models the lifecycle of the per-key shared in-flight slot.

Allowed states:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `DROPPED`

Normative rule:

- a shared slot MUST have exactly one terminal state;
- `SUCCESS` MUST correspond to publication that has already linearized at the bucket insertion point;
- `FAILED` MUST correspond to transient or implementation failure propagated to all attached waiters;
- `DROPPED` MUST correspond to partition close / bulk-drop terminalization.

### 8.2 Waiter State

`WaiterState` models the lifecycle of one attached joiner/waiter.

Allowed states:

- `ATTACHED`
- `TIMED_OUT`
- `CANCELLED`
- `RESUMED`

Normative rule:

- waiter timeout MUST NOT transition the shared slot to `FAILED`;
- waiter cancellation MUST NOT cancel the shared slot;
- shared-slot terminalization MUST resume or fail all still-attached waiters;
- no waiter may remain indefinitely pending after shared-slot terminalization or partition drop.

### 8.2.1 Attach Terminal-Signal Completeness (AMENDED)

A waiter attachment is considered successful only if the implementation guarantees that the attachment will later
receive exactly one terminal outcome:

- normal resume,
- exceptional completion caused by shared-slot terminalization,
- waiter timeout,
- or waiter cancellation.

No successfully attached waiter may remain in a state where no terminal signal is any longer reachable.

### 8.2.2 Post-Insertion Attach Reconciliation (AMENDED)

After waiter-list insertion, the implementation MUST re-verify shared-slot state.

If the shared slot has already transitioned out of `PENDING` at that point, the implementation MUST either:

- immediately deliver the terminal signal to that attachment, or
- remove the attachment and reject the attach.

An implementation MUST NOT leave a post-insertion attachment in a state where terminalization is no longer reachable.

### 8.2.3 Completion Continuation Execution-Path Safety (AMENDED)

Completion continuation execution on the builder publication path MUST NOT re-enter the L2 shard path.

Implementations MAY dispatch waiter continuations to a separate executor or equivalent completion queue to prevent lock
inversion or publication-path contamination.

This requirement does not alter publication-before-completion ordering; it only constrains how waiter continuations may
execute after terminalization becomes observable.

### 8.3 Attach / Drop Race Rule

Attach and partition-drop MAY race.

Required behavior:

- if `DROPPED` wins before attach linearizes, attach MUST fail immediately with a dropped terminal result;
- if attach linearizes before `DROPPED`, the subsequent drop sweep MUST wake that waiter exceptionally before region
  reclamation;
- region reclamation MUST NOT occur until all inflight slots visible at close have been terminalized.

### 8.3.1 Close-Gate Terminalization Completeness (AMENDED)

A close-gate publication MUST occur before final region reclamation.

The implementation MUST ensure that any in-flight slot visible after close publication is terminalized before final
region removal.

This MAY be achieved by:

- stable repeated sweep,
- post-insert close check with immediate drop,
- or an equivalent linearizable mechanism.

This requirement also applies to any slot created after close-gate publication but before final reclamation; such slots
MUST be terminalized before region removal completes.

### 8.4 Slot-Owned Speculative Leases (AMENDED)

If timeout/degrade handling promotes a waiter into a speculative builder under governance quota, the resulting
speculative reservation MUST be modeled as a **slot-owned lease**.

Normative rule:

- lease issuance originates from the shared slot,
- normal speculative-builder completion MAY release the lease,
- but shared-slot terminalization MUST also force-release any still-live speculative leases owned by that slot.

This rule prevents lease-release correctness from depending on caller/handle discipline alone.

---

## 9. Worker Lifecycle Contract

### 9.1 Acquire

When a worker-local planner state is acquired from the pool:

- it MUST already be in a clean reusable baseline state, OR
- it MUST be initialized into one before first use.

Required acquire-time assumptions:

- `currentDepth == 0`
- no reachable `GreyMap` membership residue
- no reachable stale `NodeIdIndexer` chain
- no reachable stale signature slice

### 9.2 Use

During session execution:

- planner state is exclusively owned by one active session;
- no concurrent session may mutate the same worker-local planner state;
- hot-path data structures remain primitive and allocation-free per operation.

### 9.3 Reset / Return

On any exit path, including hard abort:

- `resetToCleanState()` MUST execute from `finally`;
- `NodeIdIndexer.reset()` MUST run;
- active membership MUST be cleared;
- `currentDepth` MUST return to sentinel baseline;
- semantic zero-residue MUST hold before the state is returned to the pool.

### 9.4 Quarantine

If reset fails, or if post-reset invariants cannot be proven:

- the worker-local state MUST be quarantined,
- it MUST NOT be returned to the pool,
- it MUST be discarded or rebuilt according to runtime policy.

### 9.5 Kotlin Reference Shape

```kotlin
interface PlannerStatePool {
    fun acquire(): PlannerWorkerState
    fun release(state: PlannerWorkerState)
    fun quarantine(state: PlannerWorkerState, cause: Throwable)
}
```

### 9.6 Joined-Waiter Fresh-Session Restart Rule (AMENDED)

If an attached/joined waiter is resumed through fresh-session restart rather than suspended-session continuation, the
current session MUST still obey the ordinary worker lifecycle contract above.

Normative consequences:

- the current session MUST exit through the same `finally`-guarded cleanup path as any other session exit,
- worker-local primitive state MUST NOT remain suspended in partially-executed form while awaiting L2 completion,
- resumed work MUST start from a fresh planner session rather than by mutating a half-retained worker-local state.

This keeps join resumption compatible with zero-residue worker reuse.

### 9.7 Request-Scoped Physical-Budget Carry-Forward (AMENDED)

If joined-waiter resumption uses fresh-session restart, boundedness MUST be enforced by carrying forward
request-scoped **physical-step** budget into the restarted session.

Normative rule:

- each restarted session receives an effective physical-step bound clamped by the remaining request-scoped physical
  budget,
- exhaustion of that carried-forward physical budget MUST terminate through the existing hard-abort / fail-closed
  budget path,
- no additional retry-count policy surface or retry fault kind is introduced by this note.

---

## 10. Semantic Output Boundary

### 10.1 Semantic Output

The following are semantic:

- final topology of the canonical result,
- canonical signatures,
- cycle-truncation choice as determined by the protocol comparator,
- semantic work accounting outputs explicitly defined as semantic.

### 10.2 Non-Semantic Internal Variation

The following are non-semantic implementation details:

- concrete `nodeId` numbering,
- shard count,
- cache warmness,
- bucket placement,
- probe count,
- exact pooled array reuse pattern,
- telemetry payload shape,
- secure wipe enablement.

### 10.3 Rule

Changes in non-semantic internal details MUST NOT alter semantic output.

---

## 11. L1 / L2 Boundary

### 11.1 L1 Structural Budget

L1 uses `ResolvedPlannerSessionCaps` to size worker-local primitive structures such as:

- `NodeIdIndexer`
- `ActiveStack`
- `GreyMap`
- RMQ parallel arrays
- undo log
- signature slab

### 11.2 L2 Governance Budget

L2 governance budgets remain repository-level controls, such as:

- `maxEntries`
- `maxApproxBytes`
- per-partition caps
- circuit-open thresholds

These govern reuse, retention, throughput, and survivability.
They do not redefine L1 planner structural caps.

---

## 12. Failure Semantics

### 12.1 L1

L1 sizing and structural-cap failures are fail-closed.

Examples:

- minimal layout does not fit,
- slab contract violation,
- overflow during capacity solving,
- impossible `nextPowerOfTwo(...)`,
- cap exceeded during execution.

### 12.2 L2

L2 governance failure degrades by:

- miss,
- bypass,
- circuit-open.

L2 governance changes MUST NOT alter semantic output.

### 12.2.1 Attach Rejection vs Quota Exhaustion (AMENDED)

Ordinary attach rejection and speculative-builder quota exhaustion are distinct events.

Examples of ordinary attach rejection include:

- region already closed,
- shared slot already terminalized,
- waiter cap reached.

`L2_INFLIGHT_QUOTA_EXHAUST` applies only to speculative-builder quota denial after timeout/degrade handling.
It MUST NOT be used as a generic label for all attach rejection paths.

### 12.2.2 Speculative-Builder Reservation Lifecycle (AMENDED)

If timeout handling promotes a waiter into a speculative builder under governance quota, that promotion MUST acquire a
speculative-builder reservation.

That reservation MUST be released exactly once when the speculative build attempt terminates, whether by:

- successful publish,
- abort,
- or unrecoverable fault.

The release of speculative-builder reservation MUST NOT depend on the shared slot’s own terminal transition.

### 12.3 Builder-Handle Terminalization Discipline (AMENDED)

If the L2 port returns a builder-owned handle (or equivalent miss-commit token), the caller MUST guarantee eventual
terminalization of that handle by invoking exactly one of:

- `commit(...)`, or
- `abort(cause)`.

If local building or pre-publication preparation throws before `commit(...)`, the caller MUST invoke `abort(cause)`.

Implementations SHOULD document or provide a usage discipline equivalent to `try/finally` so that pending miss-owned
slots do not remain orphaned.

### 12.4 Handle-Level vs Slot-Level Terminalization Defense (AMENDED)

If builder-owned handles are used, implementations MAY enforce terminalization safety at two distinct layers:

- handle-level terminalization guards, which prevent caller misuse such as double `commit/abort` or abandoned handles,
- slot-level CAS terminalization, which arbitrates concurrent shared-slot terminal races such as `SUCCESS`, `FAILED`,
  or `DROPPED`.

These two layers serve different failure paths and MUST NOT be treated as redundant by default.

---

## 13. Compliance Tests

The following tests are required for this bridge layer:

- `ResolvedSessionBudgetDeterminismTest`
- `ResolvedJoinGovernanceDeterminismTest`
- `CapacitySolverDeterminismTest`
- `ResolvedPlannerSessionCapsInvariantTest`
- `WorkerLifecycleResetComplianceTest`
- `WorkerQuarantineOnResetFailureTest`
- `SemanticOutputBoundaryEquivalenceTest`
- `JoinTimeoutNonSemanticEquivalenceTest`
- `SharedSlotAndWaiterStateIndependenceTest`
- `PartitionDropAttachRaceWakeupCompletenessTest`
- `AttachRejectedVsQuotaExhaustedTelemetrySeparationTest`
- `RequestScopedPhysicalBudgetCarryForwardTest`
- `SpeculativeLeaseForceReleaseOnSlotTerminalizationTest`

---

## 14. Amendment Targets

This note is intended to be referenced by:

- `ADR-0032`
- `docs/design/l1-planner-session-primitive-data-structures.md`

---

## 15. Policy Resolution Epoch Rule (AMENDED)

### 15.1 Stable Resolution Time Only

Adaptive policy resolution, including `AUTO` mode, MUST occur only at a stable policy-resolution boundary outside the
planner hot path, for example:

- process bootstrap,
- explicit policy refresh,
- worker-pool generation rollover,
- another equivalent runtime-boundary installation point.

The following are forbidden during an already-running session:

- recomputing `ResolvedSessionBudget`,
- recomputing `ResolvedSizingCalibration`,
- recomputing `ResolvedPlannerSessionCaps`,
- recomputing `ResolvedJoinGovernance`,
- mutating already-installed resolved values in response to live telemetry.

### 15.2 Session-Fixed Snapshot Rule

Each `PlannerSession` MUST observe one fixed resolved runtime-policy snapshot for its entire lifetime.

Normative consequences:

- a session starts with one resolved budget/governance snapshot,
- all planner/lifecycle/join decisions for that session MUST use that snapshot only,
- a newer resolved snapshot may apply only to subsequently created sessions.

### 15.3 Determinism Rationale

This rule prevents mid-session policy drift from changing:

- capacity solving inputs,
- join timeout behavior,
- speculative quota behavior,
- worker reset/reuse assumptions,
- bypass vs fail-fast governance decisions,

for an already-running session.

### 15.4 Kotlin Reference Shape (Illustrative)

`````kotlin
class PolicyEpoch private constructor(
    val id: Long,
    val policy: ResolvedRuntimePolicy,
) {
    companion object {
        @JvmStatic
        fun issue(
            id: Long,
            policy: ResolvedRuntimePolicy,
        ): PolicyEpoch {
            return PolicyEpoch(
                id = id,
                policy = policy,
            )
        }
    }
}
`````

If an implementation uses an epoch-tagged snapshot like `PolicyEpoch`, the identifier MUST increase monotonically and
the tagged policy snapshot MUST remain immutable after installation.

---

## 16. Adaptive Resolver Stability & Cold-Start Rules (AMENDED)

### 16.1 Stability Requirement

If `AUTO` mode or any adaptive policy resolver uses telemetry feedback, it MUST include a stability mechanism to avoid
epoch-to-epoch oscillation.

Allowed techniques include:

- exponential moving average (EMA),
- hysteresis bands,
- minimum hold epochs,
- clamped update steps,
- equivalent smoothing/stability controls.

Forbidden anti-pattern:

- immediate threshold-crossing flips on every epoch without damping.

### 16.2 Cold-Start Rule

If no usable historical telemetry exists, the resolver MUST choose deterministic conservative defaults.

Cold-start defaults SHOULD favor:

- non-aggressively short join timeouts,
- low speculative-builder quota,
- fail-fast or bypass-safe behavior on quota exhaustion,
- stable initial behavior over premature aggressiveness.

### 16.3 Telemetry Scope Rule

Telemetry gathered during session `S` MAY influence a later resolved snapshot, but it MUST NOT mutate the already-fixed
resolved policy of session `S`.

### 16.4 Policy Boundary Rule

Adaptive policy resolution remains outside the Domain Core.

The core and worker-local planner state MUST consume only already-resolved values.

---

## 17. Runtime Policy Registry Publication Safety (AMENDED)

### 17.1 Safe Publication Rule

If resolved runtime-policy snapshots are installed into a runtime registry for future sessions, the registry MUST
provide safe cross-thread publication.

Minimum acceptable implementations include:

- a `@Volatile` snapshot reference, or
- `AtomicReference<PolicyEpoch>` (preferred when monotonic installation checks are required).

### 17.2 One-Way Installation Rule

Installing a newer policy snapshot MUST NOT silently roll back to an older snapshot.

If concurrent installation is possible, the runtime SHOULD enforce monotonic policy progression.

### 17.3 Stale Read Tolerance Rule

A newly created session may observe either the old or the new fully-published snapshot depending on the exact handoff
timing, but it MUST NEVER observe a partially-installed snapshot.

### 17.4 Kotlin Reference Shape (Illustrative)

``````kotlin
class RuntimePolicyRegistry(initial: PolicyEpoch) {
    private val ref = java.util.concurrent.atomic.AtomicReference(initial)

    fun currentEpoch(): PolicyEpoch = ref.get()

    fun install(next: PolicyEpoch) {
        while (true) {
            val prev = ref.get()
            if (next.id <= prev.id) {
                throw PlanningProtocolIntegrityException(
                    "RuntimePolicyRegistry.install requires monotonically increasing PolicyEpoch.id: next=${next.id}, prev=${prev.id}"
                )
            }
            if (ref.compareAndSet(prev, next)) return
        }
    }
}

``````

## 18. Wall-Clock Policy Separation (AMENDED)

### 18.1 Rationale

Planner structural/runtime budget and elapsed wall-clock watchdog policy are related operational concerns, but they are
not the same kind of contract.

`ResolvedSessionBudget` describes:

- planner-core structural bytes,
- physical steps,
- semantic work units,
- signature length / headroom constraints.

Elapsed wall-clock policy depends more directly on external runtime conditions such as:

- JIT warmup,
- GC pause behavior,
- scheduler delays,
- host/container variability.

### 18.2 Separation Rule

A session-level elapsed-time limit, if introduced, SHOULD be represented as a separate runtime contract rather than
folded into `ResolvedSessionBudget`.

Illustrative shape:

```kotlin
class ResolvedWallClockPolicy private constructor(
    val maxSessionElapsedNanos: Long,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxSessionElapsedNanos: Long,
        ): ResolvedWallClockPolicy {
            return ResolvedWallClockPolicy(
                maxSessionElapsedNanos = maxSessionElapsedNanos,
            )
        }
    }
}
```

As part of the policy snapshot family, `ResolvedWallClockPolicy` SHOULD be factory-issued and MUST NOT rely on
`data class`-generated `copy()` semantics as a reconstruction backdoor.

### 18.3 Boundary Rule

Elapsed wall-clock policy MUST NOT be folded into:

- primitive byte-ledger math,
- `PlannerCapacityResolver` sizing formulas,
- `PlannerSession.step(costCenter)` semantics,
- `ResolvedJoinGovernance` waiter-lifecycle meaning.

### 18.4 Failure Rule

If a runtime introduces session-level elapsed-time watchdog behavior, that watchdog belongs to the runtime boundary /
worker lifecycle side, not to the planner-core capacity solver itself.

A session elapsed-time timeout is therefore a runtime-boundary execution limit, not an L2 waiter timeout and not an
exact-match/cache-correctness signal.

### 18.5 Restart Boundedness Separation (AMENDED)

If joined-waiter resumption uses fresh-session restart, restart boundedness MUST still be enforced through
carried-forward
request-scoped physical-step budget rather than through elapsed wall-clock timeout thresholds.

Elapsed wall-clock limits MAY still abort work at the runtime boundary, but they MUST NOT become the authority for
L2 restart boundedness or L1 structural budget exhaustion.

## 19. Adapter-Owned Completion Dispatch Lifecycle (AMENDED)

### 19.1 Rationale

If joined waiters are resumed through non-blocking completion delivery rather than worker-thread polling, the runtime
requires explicit completion-dispatch infrastructure.

That infrastructure is not part of planner-core structural budget solving, but it does affect:

- worker lifecycle boundaries,
- join completion delivery,
- fresh-session restart orchestration,
- shutdown safety.

Therefore this note must state where that infrastructure is owned and how it relates to worker/session lifecycle.

### 19.2 Ownership Rule

If the runtime uses any of the following to implement joined-waiter completion delivery:

- completion mailbox,
- completion executor,
- timeout scheduler,
- equivalent dispatch queue / mailbox infrastructure,

that infrastructure MUST be **adapter-owned** rather than:

- slot-owned,
- waiter-owned,
- worker-owned,
- or partition-owned.

Normative consequences:

- completion-dispatch resources are created and destroyed at adapter lifecycle boundaries,
- partition drop MUST NOT implicitly destroy adapter-owned completion-dispatch resources,
- worker-local planner state MUST NOT own or retain completion-dispatch resources across session cleanup.

### 19.3 Shutdown Rule

Adapter shutdown MUST proceed in an order that preserves terminal-signal delivery guarantees.

Required order:

1. prevent new joined-waiter admission,
2. force shared-slot terminalization visibility (`SUCCESS`, `FAILED`, or `DROPPED`),
3. allow bounded completion drain,
4. only then perform final dispatch-resource shutdown.

Abrupt shutdown that discards pending completion work before corresponding terminalization has been made visible is
forbidden.

### 19.4 Partition Drop Rule

Partition drop and adapter shutdown are distinct lifecycle events.

Normative rule:

- dropping one partition MUST NOT destroy adapter-owned completion-dispatch infrastructure,
- a dropped partition MAY enqueue exceptional completion work for already-attached waiters,
- that completion work remains the responsibility of the adapter-owned completion-dispatch path until delivered or
  deterministically closed during adapter shutdown.

### 19.5 Worker Boundary Rule

Completion dispatch MUST remain outside worker-local planner-state ownership.

That means:

- worker-local planner state MAY be cleaned, returned, or quarantined independently of completion-dispatch resources,
- joined-waiter fresh-session restart MUST be scheduled through the runtime boundary,
- no worker-local planner primitive state may remain retained merely because completion delivery has not yet occurred.

---

## 20. Implementation Order Dependency for Joined-Waiter Dispatch (AMENDED)

### 20.1 Rationale

Timeout handling and completion handling may execute on different runtime threads once non-blocking join is introduced.

Examples:

- timeout scheduler thread,
- completion executor thread,
- partition-drop sweep thread.

If waiter terminalization semantics are not already centralized, these threads can race outside a single state machine
and
re-open the exact attach/timeout/drop correctness gaps that the amendments are trying to close.

### 20.2 Ordering Rule

The runtime MUST establish waiter-state terminalization semantics as the single source of truth **before**
attaching timeout-scheduler or completion-dispatch infrastructure.

In practice this means:

1. dual-axis slot/waiter state machine first,
2. waiter-state CAS terminalization semantics second,
3. timeout and completion dispatch infrastructure only after that.

### 20.3 Single Terminalization Authority Rule

Exactly one waiter-terminal transition must win for any attached waiter.

Allowed winning terminal waiter states are:

- `RESUMED`
- `TIMED_OUT`
- `CANCELLED`

Normative rule:

- timeout-triggered transition and completion-triggered transition MUST race only through waiter-state CAS,
- external dispatch infrastructure MUST NOT invent a second terminalization path outside the waiter state machine,
- attach success semantics remain valid only if terminalization remains centralized this way.

### 20.4 Fresh-Session Restart Dependency

If joined waiters resume through fresh-session restart, completion-dispatch infrastructure MUST NOT be attached before
the
runtime has already fixed the following restart semantics:

- current session exits through ordinary `finally` cleanup,
- worker-local primitive state is reset before reuse,
- resumed work starts from a fresh planner session,
- carried-forward request-scoped physical budget is applied to the restarted session.

Without this ordering, completion delivery could resume work against an undefined session-lifecycle boundary.

### 20.5 Verification Requirement

Implementations of non-blocking joined-waiter resumption MUST include a compliance check ensuring that dispatch
infrastructure was introduced only after waiter-state CAS semantics became the sole terminalization authority.

Illustrative test name:

- `WaiterStateCasBeforeDispatchInfrastructureTest`