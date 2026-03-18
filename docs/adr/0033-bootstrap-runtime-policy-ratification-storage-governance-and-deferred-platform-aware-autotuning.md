# ADR-0033: Bootstrap Runtime Policy Ratification, Storage Governance, and Deferred Platform-Aware Autotuning

Date: 2026-03-18

Status: Proposed

Related / Normative References:

- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- compiler-core-protocols.md
- planner-budget-resolution-and-worker-lifecycle.md
- l1-planner-session-primitive-data-structures.md
- l2-plan-interner-partitioned-tier2-with-governance.md
- ADR-0024: Adoption of Paranoid Quality Assurance Strategy

---

## Context

Kontrakt’s planning engine is a deterministic compiler-style state machine.

The framework already commits to:

- cache-blind semantic determinism,
- policy / protocol separation,
- explicit primitive byte-ledger sizing,
- session-fixed resolved policy snapshots,
- worker-local planner state and zero-residue reuse,
- L1 structural budget vs L2 governance budget separation,
- exact-match verification after fast routing.

At the same time, v1 cannot remain underspecified about runtime policy.

Specifically, v1 needs concrete answers for:

1. **bootstrap worker-local planner budgets**
2. **bootstrap L2 join governance**
3. **bootstrap L2 storage governance**
4. **what is externally justified law vs what is Kontrakt-specific ratified constant**
5. **how bootstrap constants are approved without pretending that all exact values are externally standardized**

The framework must be commercially defensible.

That means:

- it must not hide magic numbers inside the protocol core,
- it must not claim false external authority for Kontrakt-specific exact values,
- it must still provide concrete, reviewable, deterministic bootstrap policy for v1.

In addition, the project wants to preserve a future path toward compiler-style / autotuning-style platform
specialization in v2,
but v1 must not depend on runtime search, hot-path adaptation, or online policy mutation.

Therefore this ADR defines:

- the v1 bootstrap runtime-policy surface,
- the current approved bootstrap constants and laws,
- the ratification rules for Kontrakt-specific constants,
- the storage-governance contract added now,
- and the boundary that defers platform-aware autotuning to a future ADR.

---

## Decision

### 1. v1 adopts explicit bootstrap runtime policy

Kontrakt v1 WILL ship with an explicit, deterministic bootstrap runtime policy.

That policy is represented at the runtime boundary as:

- `ResolvedSessionBudget`
- `ResolvedJoinGovernance`
- `ResolvedStorageGovernance`
- optional `ResolvedWallClockPolicy`
- bundled into `ResolvedRuntimePolicy`

The Domain Core and worker-local planner state MUST consume only these already-resolved immutable values.

The core MUST NOT:

- inspect heap state,
- inspect CPU count,
- inspect cgroup/container memory,
- inspect live telemetry,
- mutate already-installed budgets or governance during an already-running session.

---

### 2. Public resource surface remains high-level

The user/operator facing surface remains:

- `AUTO`
- `SMALL`
- `STANDARD`
- `LARGE`

v1 does NOT expose:

- signature reserve ratio,
- preferred depth divisor,
- undo density,
- sparse-table scaling internals,
- exact adaptive policy formulas,
- storage multiplier tuning knobs,
- join governance tuning knobs.

These remain internal runtime-policy resolution concerns.

---

### 3. v1 `AUTO` aliases `STANDARD`

For v1 only:

```text
AUTO = STANDARD
```

Rationale:

- v1 does not yet standardize platform-aware autotuning,
- v1 does not yet standardize environment-sensitive policy classification beyond the approved bootstrap profiles,
- deterministic conservative defaults are preferred over premature adaptive branching.

This alias MAY be replaced in v2 by a future platform-aware policy resolver, but that future resolver must still produce
one immutable resolved snapshot per session.

---

### 4. Profiles are defined primarily by worker-byte plateaus, not by semantic node floors

The primary public definition of v1 resource profiles is:

- per-worker planner byte budget plateau,
- not semantic node-floor promises,
- not hard-coded semantic topology targets.

Node/depth/table caps remain **derived outputs** of the deterministic capacity solver.

This is constitutionally important.

It means:

- the ADR does NOT define profiles as “128k nodes”, “256k nodes”, etc.
- instead, the ADR defines profiles as approved worker-byte plateaus,
- and the current solver deterministically derives the corresponding caps.

This keeps the profile definition aligned with:

- explicit primitive byte-ledger law,
- deterministic solver outputs,
- policy/protocol separation,
- and future solver evolution without breaking profile meaning.

---

### 5. `ResolvedStorageGovernance` is added now

v1 formally adds a separate L2 storage-governance contract.

Illustrative shape:

```kotlin
data class ResolvedStorageGovernance(
    val maxApproxBytesPerPartition: Long,
    val maxEntriesPerPartition: Int,
    val circuitOpenOnStorageExhaustion: Boolean,
)
```

Normative rule:

- storage governance is distinct from L1 structural planner sizing,
- storage governance is distinct from L2 waiter/join governance,
- storage governance affects retention, survivability, and degradation behavior only,
- storage governance MUST NOT redefine:
    - exact-match semantics,
    - routing identity,
    - semantic output,
    - or planner-core structural caps.

---

### 6. Bootstrap policy uses three categories of authority

v1 bootstrap policy is defined by three distinct authority classes.

#### 6.1 External law / adjacent-system convention

These are borrowed from adjacent JVM/compiler/cache systems and are considered acceptable normative rationale for v1:

- open-addressing / hash-table sizing should retain conservative headroom rather than running near saturation,
- cache/storage governance may be expressed in bytes/weight rather than entry count only,
- same-key in-flight collapse is a valid concurrency pattern,
- build/compile JVM environments form reasonable bootstrap deployment envelopes for v1.

#### 6.2 Internal deterministic derivation

Some values are not guessed.
They are derived by current Kontrakt law from:

- the primitive byte ledger,
- the resolved worker-byte budget,
- the internal deterministic sizing calibration,
- and the deterministic capacity solver.

#### 6.3 Kontrakt-specific bootstrap constants requiring ratification

Some exact values are not externally standardized and are not derivable purely from public law.

These must be treated honestly as:

- Kontrakt-specific bootstrap constants,
- approved for v1 only after representative-corpus ratification,
- subject to re-ratification when their trigger conditions are met.

---

## 7. Session Budget Law

### 7.1 Public contract

`ResolvedSessionBudget` remains:

```kotlin
data class ResolvedSessionBudget(
    val maxPlannerBytesPerWorker: Long,
    val maxPhysicalSteps: Int,
    val maxSemanticWorkUnits: Int,
    val maxSignatureLen: Int,
    val fixedHeadroomBytes: Long,
)
```

Normative rule:

- this contract is resolved outside the Domain Core,
- all fields are immutable once installed,
- identical resolved inputs MUST produce identical `ResolvedPlannerSessionCaps`.

---

### 7.2 Current solver boundary

For the current implementation family, capacity solving follows the already-approved split:

```text
reservedSignatureBytes = max(B * signatureReserveRatio, maxSignatureLen)

structBudget = B - reservedSignatureBytes - fixedHeadroomBytes
```

where:

- `B = maxPlannerBytesPerWorker`

and the solver then derives:

- `nodeCap`
- `depthCap`
- `indexerTableCap`
- `undoLogCap`
- `maxSignatureBytes`
- `structBudgetBytes`

under the primitive byte ledger.

Important boundary rule:

The commonly-used simplified form:

```text
requiredBudget(nodeFloor, depthFloor)
  = ( TotalStructBytes(nodeFloor, depthFloor) + fixedHeadroomBytes ) / (1 - signatureReserveRatio)
```

is valid only when:

```text
B * signatureReserveRatio >= maxSignatureLen
```

If that condition does not hold, the full unsimplified equation MUST be used.

This condition is currently satisfied by the approved v1 bootstrap worker plateaus,
but the condition MUST remain explicit in the documentation because future profile additions or future `maxSignatureLen`
changes may invalidate the simplification.

---

### 7.3 Worker-byte plateau law

The approved v1 worker-local bootstrap plateaus are:

```text
SMALL    = 10 MiB
STANDARD = 20 MiB
LARGE    = 40 MiB
AUTO     = STANDARD
```

These plateaus are approved as:

- deterministic bootstrap budgets,
- conservative v1 worker-local planner budgets,
- chosen as the smallest round plateaus that clear meaningful current solver plateaus under the current primitive ledger
  and current internal calibration.

They are not claimed to be universal JVM constants.

They are Kontrakt-approved bootstrap plateaus.

---

### 7.4 Step/work-unit bootstrap law

`maxPhysicalSteps` and `maxSemanticWorkUnits` are not intended as performance-tuning knobs.
They are runaway-prevention fuses.

For v1 bootstrap policy:

```text
maxPhysicalSteps     = 16 × derived maxNodeIdCap
maxSemanticWorkUnits =  4 × derived maxNodeIdCap
```

This keeps step/work-unit ceilings tied to the currently derived structural plateau instead of inventing unrelated
arbitrary limits.

Normative intent:

- structural byte budget remains the primary capacity control,
- physical/semantic step ceilings remain secondary runaway fuses,
- these counters are not wall-clock substitutes,
- these counters are not mutable mid-session.

---

### 7.5 Approved v1 bootstrap session-budget table

```text
SMALL
- maxPlannerBytesPerWorker = 10 MiB
- maxPhysicalSteps         = 2,097,152
- maxSemanticWorkUnits     =   524,288
- maxSignatureLen          = 4096          (provisional bootstrap cap; ratification target)
- fixedHeadroomBytes       = 512 KiB       (approved v1 calibration point)

STANDARD
- maxPlannerBytesPerWorker = 20 MiB
- maxPhysicalSteps         = 4,194,304
- maxSemanticWorkUnits     = 1,048,576
- maxSignatureLen          = 4096          (provisional bootstrap cap; ratification target)
- fixedHeadroomBytes       = 512 KiB       (approved v1 calibration point)

LARGE
- maxPlannerBytesPerWorker = 40 MiB
- maxPhysicalSteps         = 8,388,608
- maxSemanticWorkUnits     = 2,097,152
- maxSignatureLen          = 4096          (provisional bootstrap cap; ratification target)
- fixedHeadroomBytes       = 512 KiB       (approved v1 calibration point)
```

Notes:

- `maxSignatureLen = 4096` is NOT claimed to be externally standardized.
  It is a Kontrakt bootstrap candidate cap and a ratification target.
- `fixedHeadroomBytes = 512 KiB` is NOT a semantic law.
  It is a currently approved calibration point under the current primitive ledger and current internal sizing
  assumptions.

---

## 8. Join Governance Law

### 8.1 Independent contract rule

`ResolvedJoinGovernance` remains a contract independent from worker-byte profile scaling.

Illustrative shape:

```kotlin
data class ResolvedJoinGovernance(
    val joinWaitTimeoutNanos: Long,
    val maxWaitersPerKey: Int,
    val maxSpeculativeBuildersPerKey: Int,
    val failFastOnQuotaExhaustion: Boolean,
)
```

Normative v1 rule:

- `ResolvedJoinGovernance` is treated as a **profile-independent cold-start bootstrap contract**
- v1 does NOT scale join governance as a function of `SMALL / STANDARD / LARGE`
- v1 keeps join governance fixed while L1 structural budget varies by profile
- any future coupling between environment class and join governance belongs to a later policy-resolution ADR

Rationale:

- session budget and join governance are independently reasoned contracts,
- memory abundance does not by itself justify longer waiter windows,
- v1 prioritizes stability and conservative burst collapse over aggressive platform-sensitive tuning.

---

### 8.2 Approved v1 bootstrap join governance

```text
ResolvedJoinGovernance
- joinWaitTimeoutNanos         = 2_000_000    // 2 ms
- maxWaitersPerKey             = 8
- maxSpeculativeBuildersPerKey = 0
- failFastOnQuotaExhaustion    = false
```

Interpretation rules:

- timeout is a monotonic elapsed-time waiter deadline,
- timeout is a waiter-lifecycle event only,
- timeout MUST NOT fail the shared slot,
- cancellation MUST NOT cancel the shared slot,
- `maxSpeculativeBuildersPerKey = 0` is the v1 cold-start bootstrap rule,
- quota exhaustion degrades by bypass-safe behavior rather than forcing fail-fast by default.

Classification:

- `maxSpeculativeBuildersPerKey = 0` is directly justified by the v1 cold-start conservative-default rule.
- `failFastOnQuotaExhaustion = false` is an approved conservative bootstrap behavior.
- `joinWaitTimeoutNanos = 2 ms` and `maxWaitersPerKey = 8` are Kontrakt bootstrap constants and ratification targets.

---

## 9. Storage Governance Law

### 9.1 Separation rule

L2 storage governance is a distinct contract from both:

- worker-local planner structural sizing,
- and L2 waiter/join governance.

It exists to control:

- retention,
- repository survivability,
- storage-triggered degradation,
- circuit-open transitions,
- and approximate byte footprint.

It does not change:

- semantic output,
- exact-match law,
- routing semantics,
- semantic identity,
- or L1 structural caps.

---

### 9.2 v1 bootstrap storage law

For v1 bootstrap policy:

```text
maxApproxBytesPerPartition = 2 × maxPlannerBytesPerWorker
maxEntriesPerPartition     = lowerPowerOfTwo(maxApproxBytesPerPartition / 1024)
circuitOpenOnStorageExhaustion = true
```

Rationale:

- `2×` is a simple bootstrap law that makes L2 meaningfully larger than the worker-local structural planner budget,
  while remaining conservative enough for shared build/CI JVM environments.
- `maxEntriesPerPartition` is a secondary hard fuse and deliberately avoids pretending to know the exact average entry
  size.
- `circuitOpenOnStorageExhaustion = true` matches the approved L2 survivability model:
  storage exhaustion is governance degradation, not semantic corruption.

---

### 9.3 Approved v1 bootstrap storage governance table

```text
SMALL
- maxApproxBytesPerPartition      = 20 MiB
- maxEntriesPerPartition          = 16,384
- circuitOpenOnStorageExhaustion  = true

STANDARD
- maxApproxBytesPerPartition      = 40 MiB
- maxEntriesPerPartition          = 32,768
- circuitOpenOnStorageExhaustion  = true

LARGE
- maxApproxBytesPerPartition      = 80 MiB
- maxEntriesPerPartition          = 65,536
- circuitOpenOnStorageExhaustion  = true
```

Classification:

- the entry-cap derivation law is approved,
- `circuitOpenOnStorageExhaustion = true` is approved,
- `storageMultiplier = 2×` is a Kontrakt bootstrap constant and ratification target.

---

## 10. Ratification Targets

The following are v1 bootstrap constants requiring explicit ratification.

### 10.1 Target List

1. `maxSignatureLen`
2. `joinWaitTimeoutNanos`
3. `maxWaitersPerKey`
4. `storageMultiplier`
5. `depthCap sufficiency for each approved worker-budget plateau`

Important clarification:

- `depthCap` is not a separate public policy constant.
- It is a derived solver output.
- However, each approved worker-budget plateau MUST be ratified by verifying that its derived `maxDepthCap` is
  sufficient for the representative corpus.

---

## 11. Ratification Rules

### 11.1 Representative Corpus

Ratification MUST use a small representative Kontrakt corpus.

The corpus SHOULD include:

- ordinary local/CI-like test graphs,
- large synthetic recursive graphs,
- hot-key / duplication-heavy planning scenarios,
- pathological rollback/depth scenarios,
- partition-drop / L2 contention scenarios,
- signature-heavy scenarios.

This is not global environment benchmarking.

It is targeted ratification of Kontrakt-specific bootstrap constants.

### 11.2 Execution Discipline

Ratification SHOULD be run under the framework’s existing quality discipline:

- quick repetition on local/PR lanes,
- deeper repetition on nightly/release lanes,
- deterministic seed capture,
- reproducible stress invocation.

### 11.3 Approval Rule Style

Unless otherwise stated, bootstrap ratification chooses:

> the smallest candidate that satisfies the required safety/survival condition.

This keeps v1 conservative.

---

### 11.4 `maxSignatureLen` ratification rule

Method:

- collect canonical signature length histogram across the representative corpus.

Approval rule:

- choose the smallest approved bootstrap cap such that:
    - no valid signature exceeds the cap,
    - and the slab contract `maxSignatureBytes >= maxSignatureLen` remains satisfiable under the approved worker-byte
      plateaus.

Initial v1 bootstrap candidate:

```text
maxSignatureLen = 4096
```

Re-ratification trigger:

- canonical signature materialization changes,
- normalization/version tuple changes affecting signature size,
- representative corpus produces any signature length exceeding the approved cap,
- or a smaller profile is introduced such that `B * signatureReserveRatio < maxSignatureLen` becomes relevant.

---

### 11.5 `joinWaitTimeoutNanos` ratification rule

Method:

- sweep a small candidate set, e.g.:
    - `0.5 ms`
    - `1 ms`
    - `2 ms`
    - `5 ms`

Observe:

- timeout count,
- bypass/degraded read count,
- duplicate-build ratio,
- join success after attach.

Approval rule:

- choose the smallest timeout after which additional waiting produces diminishing operational benefit,
- while preserving conservative cold-start behavior,
- and without introducing worker-availability collapse.

Initial v1 bootstrap candidate:

```text
joinWaitTimeoutNanos = 2 ms
```

Re-ratification trigger:

- material changes to join path implementation,
- asynchronous attach/resume contract changes,
- representative corpus shows systematic timeouts that disappear at the next larger candidate,
- or future platform-aware resolution is introduced.

---

### 11.6 `maxWaitersPerKey` ratification rule

Method:

- measure attached waiter-count distribution for same-key bursts across the representative corpus.

Approval rule:

- choose the smallest waiter cap that avoids attach rejection or pathological degradation in representative workloads.

Initial v1 bootstrap candidate:

```text
maxWaitersPerKey = 8
```

Re-ratification trigger:

- worker-concurrency model changes materially,
- shard/gate behavior changes materially,
- representative corpus shows same-key bursts repeatedly exhausting the approved cap,
- or future environment-sensitive join policy is introduced.

---

### 11.7 `storageMultiplier` ratification rule

Method:

- sweep a small approved candidate set, e.g.:
    - `1.0x`
    - `1.5x`
    - `2.0x`
    - `3.0x`

Observe:

- storage-triggered `CircuitOpen` transitions,
- storage-triggered bypass/degrade events,
- duplicate-build ratio,
- partition-drop pressure,
- join-wait side effects.

Approval rule:

- choose the smallest multiplier for which the representative corpus observes **zero storage-triggered `CircuitOpen`
  transitions**.

Tie-breaker rule:

- if multiple candidates satisfy the survival condition,
  prefer the smallest candidate;
- duplicate-build ratio and join-wait telemetry MAY be used only as tie-breakers.

Initial v1 bootstrap candidate:

```text
storageMultiplier = 2.0x
```

Re-ratification trigger:

- storage entry model changes materially,
- approximate-byte accounting changes materially,
- partition/shard/bucket retention behavior changes materially,
- representative corpus observes storage-triggered circuit-open under the approved multiplier.

---

### 11.8 `depthCap` sufficiency ratification rule

Method:

- for each approved worker-byte plateau, observe the derived `maxDepthCap`,
- run the representative corpus under that plateau,
- record whether any session exceeds the derived depth cap.

Approval rule:

- a worker-byte plateau is approvable only if the representative corpus observes **zero depth-cap breaches** under that
  plateau.
- if multiple candidate plateaus satisfy this condition, approve the smallest plateau.

This is not a separate public depth constant.
It is the sufficiency rule for approving worker-byte plateaus.

Re-ratification trigger:

- primitive ledger changes materially,
- desired-depth / feasible-depth solver logic changes materially,
- representative corpus adds deeper graph families,
- any approved profile observes a real depth-cap breach.

---

## 12. Current v1 Bootstrap Approval State

Until the first explicit ratification pass completes, the following are treated as:

### 12.1 Approved v1 bootstrap laws

- worker-byte profile definition by plateau
- `AUTO = STANDARD`
- `tableCap = nextPowerOfTwo(max(8, 2 * nodeCap))`
- `maxPhysicalSteps = 16 × derived maxNodeIdCap`
- `maxSemanticWorkUnits = 4 × derived maxNodeIdCap`
- join governance is profile-independent in v1
- `maxSpeculativeBuildersPerKey = 0`
- `failFastOnQuotaExhaustion = false`
- `maxApproxBytesPerPartition = 2 × maxPlannerBytesPerWorker`
- `maxEntriesPerPartition = lowerPowerOfTwo(maxApproxBytesPerPartition / 1024)`
- `circuitOpenOnStorageExhaustion = true`

### 12.2 Approved v1 bootstrap candidates pending ratification

- `maxSignatureLen = 4096`
- `joinWaitTimeoutNanos = 2 ms`
- `maxWaitersPerKey = 8`
- `storageMultiplier = 2.0x`
- worker-byte plateaus `10 / 20 / 40 MiB` as approved candidate plateaus pending depth-cap sufficiency ratification

---

## 13. Wall-Clock Separation

This ADR does NOT fold elapsed wall-clock policy into:

- `ResolvedSessionBudget`
- `ResolvedJoinGovernance`
- `ResolvedStorageGovernance`
- `PlannerCapacityResolver`
- `step(costCenter)` semantics

If a session-level elapsed-time watchdog is introduced, it belongs to a separate runtime policy contract.

---

## 14. Deferred Platform-Aware Autotuning

v1 does NOT standardize:

- live policy mutation,
- hot-path policy search,
- NAS-style structure search,
- reinforcement-learning policy controllers,
- runtime policy adaptation that changes an already-running session.

However, v1 preserves extension points for v2.

Future v2 work MAY introduce:

- compiler-style cost-based policy search,
- equality-preserving / semantics-preserving policy extraction,
- program-autotuning-style platform specialization,
- Pareto-frontier selection across non-semantic runtime-governance surfaces.

Any such future work MUST remain confined to:

- non-semantic runtime governance,
- stable boundary resolution,
- immutable per-session snapshots,
- and separate follow-up ADRs.

---

## 15. Required Implementation Consequences

### 15.1 Runtime policy model

`ResolvedRuntimePolicy` MUST be expanded to include storage governance.

Illustrative shape:

```kotlin
data class ResolvedRuntimePolicy(
    val sessionBudget: ResolvedSessionBudget,
    val joinGovernance: ResolvedJoinGovernance,
    val storageGovernance: ResolvedStorageGovernance,
    val wallClockPolicy: ResolvedWallClockPolicy? = null,
)
```

### 15.2 Resolver responsibilities

The runtime policy resolver for v1 MUST:

- map `AUTO -> STANDARD`,
- emit the approved worker-byte plateau for the selected profile,
- emit the profile-independent bootstrap join governance,
- emit the derived storage governance by the approved storage law,
- avoid all live hot-path mutation.

### 15.3 Adapter/runtime responsibilities

The runtime boundary MUST preserve:

- session-fixed snapshot installation,
- monotonic policy epoch publication,
- best-effort/non-throwing telemetry emission,
- and strict separation between current-session behavior and future policy inputs.

---

## 16. Required Compliance Tests

The following tests are required in addition to already-existing L1/L2 policy tests.

### 16.1 Bootstrap policy tests

- `BootstrapPolicyTableDeterminismTest`
    - verifies identical resolved profile inputs produce identical `ResolvedRuntimePolicy`

- `ResolvedStorageGovernanceDeterminismTest`
    - verifies storage governance is deterministic for identical bootstrap inputs

- `AutoAliasesStandardTest`
    - verifies v1 `AUTO` resolves exactly to `STANDARD`

### 16.2 Ratification gate tests

- `SignatureLengthHistogramRatificationTest`
- `JoinTimeoutSweepRatificationTest`
- `WaiterCapSweepRatificationTest`
- `StorageMultiplierSweepRatificationTest`
- `DepthCapSufficiencyRatificationTest`

These may begin as build-internal verification tasks if they are too heavy for ordinary PR execution,
but they MUST exist before v1 freeze.

### 16.3 Existing invariants remain mandatory

Nothing in this ADR weakens existing mandatory tests for:

- capacity solver determinism,
- minimal layout fail-closed behavior,
- overflow guards,
- zero-residue worker reuse,
- join timeout / slot independence,
- cancellation / slot independence,
- partition drop wake-up completeness,
- semantic equivalence under governance change.

---

## 17. Non-Goals

This ADR does NOT decide:

- future exact adaptive/autotuning algorithm,
- future environment-classification algorithm for `AUTO`,
- future learned cost model,
- future benchmark corpus for platform specialization,
- future wall-clock watchdog defaults,
- future public exposure of low-level numeric tuning knobs.

Those belong to later ADRs.

---

## Consequences

### Positive

- v1 no longer leaves runtime policy numerically undefined.
- Worker-local budget, join governance, and storage governance are explicitly separated.
- Exact Kontrakt-specific constants are treated honestly as ratified bootstrap values instead of pretending to be
  universal law.
- The framework gains immediate storage-governance structure without waiting for v2 autotuning.
- Future platform-aware specialization remains open without contaminating current semantic/protocol boundaries.

### Negative

- Some exact v1 values remain bootstrap candidates pending explicit ratification.
- v1 still uses conservative bootstrap policy rather than platform-specialized policy.
- Additional corpus-based ratification tasks are now mandatory before v1 freeze.
- A future ADR will still be needed if `AUTO` becomes environment-sensitive or if autotuning becomes normative.

---

## Summary

Kontrakt v1 adopts:

- explicit bootstrap runtime policy,
- explicit storage governance,
- worker-byte plateaus as the primary profile definition,
- profile-independent cold-start join governance,
- and an explicit ratification framework for Kontrakt-specific bootstrap constants.

Kontrakt v1 does NOT claim that all exact numeric values are externally standardized.

Instead:

- external law justifies structure and range,
- the deterministic solver justifies derived outputs,
- and Kontrakt-specific exact bootstrap constants are ratified against a representative corpus.

This is the approved v1 policy posture until a later ADR standardizes platform-aware autotuning.