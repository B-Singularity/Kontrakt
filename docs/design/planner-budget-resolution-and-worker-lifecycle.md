# Design Note: Planner Budget Resolution and Worker Lifecycle

Date: 2026-03-14  
Status: Proposed

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
- `SMALL_HEAP`
- `DEFAULT`
- `SERVER`

Users MUST NOT be required to understand or configure internal budget-allocation parameters such as:

- signature reserve ratio,
- preferred node/depth divisor,
- undo density,
- sparse-table scaling constants.

### 3.2 Public / Cross-Boundary Contract

The only cross-boundary budget contract required by the planner core is a resolved numeric budget.

That contract is represented by `ResolvedSessionBudget`.

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
- the resolved budget MUST remain fixed for the lifetime of a `PlannerSession`.

### 4.3 Kotlin Reference Shape

```kotlin
data class ResolvedSessionBudget(
    val maxPlannerBytesPerWorker: Long,
    val maxPhysicalSteps: Int,
    val maxSemanticWorkUnits: Int,
    val maxSignatureLen: Int,
    val fixedHeadroomBytes: Long,
)
```

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
  -> PlannerSessionConfig
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

## 8. Worker Lifecycle Contract

### 8.1 Acquire

When a worker-local planner state is acquired from the pool:

- it MUST already be in a clean reusable baseline state, OR
- it MUST be initialized into one before first use.

Required acquire-time assumptions:

- `currentDepth == 0`
- no reachable `GreyMap` membership residue
- no reachable stale `NodeIdIndexer` chain
- no reachable stale signature slice

### 8.2 Use

During session execution:

- planner state is exclusively owned by one active session;
- no concurrent session may mutate the same worker-local planner state;
- hot-path data structures remain primitive and allocation-free per operation.

### 8.3 Reset / Return

On any exit path, including hard abort:

- `resetToCleanState()` MUST execute from `finally`;
- `NodeIdIndexer.reset()` MUST run;
- active membership MUST be cleared;
- `currentDepth` MUST return to sentinel baseline;
- semantic zero-residue MUST hold before the state is returned to the pool.

### 8.4 Quarantine

If reset fails, or if post-reset invariants cannot be proven:

- the worker-local state MUST be quarantined,
- it MUST NOT be returned to the pool,
- it MUST be discarded or rebuilt according to runtime policy.

### 8.5 Kotlin Reference Shape

```kotlin
interface PlannerStatePool {
    fun acquire(): PlannerWorkerState
    fun release(state: PlannerWorkerState)
    fun quarantine(state: PlannerWorkerState, cause: Throwable)
}
```

---

## 9. Semantic Output Boundary

### 9.1 Semantic Output

The following are semantic:

- final topology of the canonical result,
- canonical signatures,
- cycle-truncation choice as determined by the protocol comparator,
- semantic work accounting outputs explicitly defined as semantic.

### 9.2 Non-Semantic Internal Variation

The following are non-semantic implementation details:

- concrete `nodeId` numbering,
- shard count,
- cache warmness,
- bucket placement,
- probe count,
- exact pooled array reuse pattern,
- telemetry payload shape,
- secure wipe enablement.

### 9.3 Rule

Changes in non-semantic internal details MUST NOT alter semantic output.

---

## 10. L1 / L2 Boundary

### 10.1 L1 Structural Budget

L1 uses `ResolvedPlannerSessionCaps` to size worker-local primitive structures such as:

- `NodeIdIndexer`
- `ActiveStack`
- `GreyMap`
- RMQ parallel arrays
- undo log
- signature slab

### 10.2 L2 Governance Budget

L2 governance budgets remain repository-level controls, such as:

- `maxEntries`
- `maxApproxBytes`
- per-partition caps
- circuit-open thresholds

These govern reuse, retention, throughput, and survivability.
They do not redefine L1 planner structural caps.

---

## 11. Failure Semantics

### 11.1 L1

L1 sizing and structural-cap failures are fail-closed.

Examples:

- minimal layout does not fit,
- slab contract violation,
- overflow during capacity solving,
- impossible `nextPowerOfTwo(...)`,
- cap exceeded during execution.

### 11.2 L2

L2 governance failure degrades by:

- miss,
- bypass,
- circuit-open.

L2 governance changes MUST NOT alter semantic output.

---

## 12. Compliance Tests

The following tests are required for this bridge layer:

- `ResolvedSessionBudgetDeterminismTest`
- `CapacitySolverDeterminismTest`
- `ResolvedPlannerSessionCapsInvariantTest`
- `WorkerLifecycleResetComplianceTest`
- `WorkerQuarantineOnResetFailureTest`
- `SemanticOutputBoundaryEquivalenceTest`

---

## 13. Amendment Targets

This note is intended to be referenced by:

- `ADR-0032`
- `docs/design/l1-planner-session-primitive-data-structures.md`
- `docs/design/l2-plan-interner-partitioned-tier2-with-governance.md`