# ADR-0033: Bootstrap Runtime Policy Ratification, Storage Governance, and Deferred Platform-Aware Autotuning

Date: 2026-03-18

Status: Accepted

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

In addition, policy snapshot objects themselves MUST follow snapshot-integrity rules.

Normative rule:

- policy snapshot objects MUST be immutable,
- policy snapshot objects MUST NOT expose public primary constructors for arbitrary reconstruction,
- policy snapshot objects MUST NOT use `data class`-generated `copy()` as a mutation/reconstruction backdoor,
- policy snapshot objects SHOULD be issued through factory methods,
- invariant enforcement MUST throw explicit custom exceptions,
- `require()`, `check()`, `error()`, and standard `IllegalArgumentException`-style validation paths are forbidden for
  snapshot-integrity enforcement.

This rule applies to the policy snapshot family, including:

- `ResolvedSessionBudget`
- `ResolvedJoinGovernance`
- `ResolvedStorageGovernance`
- `ResolvedRuntimePolicy`
- optional `ResolvedWallClockPolicy`

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
class ResolvedStorageGovernance private constructor(
    val maxApproxBytesPerPartition: Long,
    val maxEntriesPerPartition: Int,
    val circuitOpenOnStorageExhaustion: Boolean,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxApproxBytesPerPartition: Long,
            maxEntriesPerPartition: Int,
            circuitOpenOnStorageExhaustion: Boolean,
        ): ResolvedStorageGovernance {
            if (maxApproxBytesPerPartition <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedStorageGovernance.maxApproxBytesPerPartition must be > 0: $maxApproxBytesPerPartition"
                )
            }
            if (maxEntriesPerPartition <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedStorageGovernance.maxEntriesPerPartition must be > 0: $maxEntriesPerPartition"
                )
            }

            return ResolvedStorageGovernance(
                maxApproxBytesPerPartition = maxApproxBytesPerPartition,
                maxEntriesPerPartition = maxEntriesPerPartition,
                circuitOpenOnStorageExhaustion = circuitOpenOnStorageExhaustion,
            )
        }
    }
}
```

Normative rule:

- storage governance is distinct from L1 structural planner sizing,
- storage governance is distinct from L2 waiter/join governance,
- storage governance affects retention, survivability, and degradation behavior only,
- storage governance MUST follow policy snapshot integrity rules,
- issuance-time invariant enforcement MUST use explicit custom exceptions,
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

class ResolvedSessionBudget private constructor(
    val maxPlannerBytesPerWorker: Long,
    val maxPhysicalSteps: Int,
    val maxSemanticWorkUnits: Int,
    val maxSignatureLen: Int,
    val fixedHeadroomBytes: Long,
    val physicalStepMultiplier: Int,
    val semanticWorkMultiplier: Int,
) {
    companion object {
        @JvmStatic
        fun issue(
            maxPlannerBytesPerWorker: Long,
            maxPhysicalSteps: Int,
            maxSemanticWorkUnits: Int,
            maxSignatureLen: Int,
            fixedHeadroomBytes: Long,
            physicalStepMultiplier: Int,
            semanticWorkMultiplier: Int,
        ): ResolvedSessionBudget {
            if (maxPlannerBytesPerWorker <= 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxPlannerBytesPerWorker must be > 0: $maxPlannerBytesPerWorker"
                )
            }
            if (maxPhysicalSteps <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxPhysicalSteps must be > 0: $maxPhysicalSteps"
                )
            }
            if (maxSemanticWorkUnits <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxSemanticWorkUnits must be > 0: $maxSemanticWorkUnits"
                )
            }
            if (maxSignatureLen <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.maxSignatureLen must be > 0: $maxSignatureLen"
                )
            }
            if (fixedHeadroomBytes < 0L) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.fixedHeadroomBytes must be >= 0: $fixedHeadroomBytes"
                )
            }
            if (physicalStepMultiplier <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.physicalStepMultiplier must be > 0: $physicalStepMultiplier"
                )
            }
            if (semanticWorkMultiplier <= 0) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedSessionBudget.semanticWorkMultiplier must be > 0: $semanticWorkMultiplier"
                )
            }

            return ResolvedSessionBudget(
                maxPlannerBytesPerWorker = maxPlannerBytesPerWorker,
                maxPhysicalSteps = maxPhysicalSteps,
                maxSemanticWorkUnits = maxSemanticWorkUnits,
                maxSignatureLen = maxSignatureLen,
                fixedHeadroomBytes = fixedHeadroomBytes,
                physicalStepMultiplier = physicalStepMultiplier,
                semanticWorkMultiplier = semanticWorkMultiplier,
            )
        }
    }
}
```

Normative rule:

- this contract is resolved outside the Domain Core,
- all fields are immutable once installed,
- issuance MUST validate basic invariants,
- issuance-time invariant enforcement MUST use explicit custom exceptions,
- policy snapshot integrity rules apply,
- `physicalStepMultiplier` and `semanticWorkMultiplier` are explicit policy constants, not hidden implementation ratios,
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

For v1 bootstrap policy, the current step/work-unit derivation is made explicit through policy constants:

```text
physicalStepMultiplier = 16
semanticWorkMultiplier = 4

maxPhysicalSteps     = physicalStepMultiplier × derived maxNodeIdCap
maxSemanticWorkUnits = semanticWorkMultiplier × derived maxNodeIdCap
```

Normative intent:

- structural byte budget remains the primary capacity control,
- physical/semantic step ceilings remain secondary runaway fuses,
- these counters are not wall-clock substitutes,
- these counters are not mutable mid-session,
- the multipliers themselves are policy constants and MUST NOT be hidden as unexplained implementation defaults.

Classification:

- `physicalStepMultiplier` and `semanticWorkMultiplier` are Kontrakt bootstrap policy constants,
- they are not externally standardized laws,
- they MUST be explicitly ratified against the representative corpus.

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
            if (joinWaitTimeoutNanos <= 0L) {
                throw PlanningProtocolIntegrityException("...")
            }
            if (maxWaitersPerKey <= 0) {
                throw PlanningProtocolIntegrityException("...")
            }
            if (maxSpeculativeBuildersPerKey < 0) {
                throw PlanningProtocolIntegrityException("...")
            }

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
storageMultiplier         = 2.0x
storageEntryBytesBaseline = 1024

maxApproxBytesPerPartition =
  storageMultiplier × maxPlannerBytesPerWorker

maxEntriesPerPartition =
  lowerPowerOfTwo(maxApproxBytesPerPartition / storageEntryBytesBaseline)

circuitOpenOnStorageExhaustion = true
```

Rationale:

- `storageMultiplier` is a bootstrap storage-policy constant that makes L2 meaningfully larger than the worker-local
  structural planner budget, while remaining conservative enough for shared build/CI JVM environments.
- `storageEntryBytesBaseline` is a bootstrap storage-calibration constant used only to derive a conservative secondary
  entry fuse from the approximate byte budget.
- `storageEntryBytesBaseline` is NOT an exact statement about real entry size.
- `maxEntriesPerPartition` deliberately avoids pretending to know the exact average entry size.
- `circuitOpenOnStorageExhaustion = true` matches the approved L2 survivability model:
  storage exhaustion is governance degradation, not semantic corruption.

Classification:

- `storageMultiplier` is a Kontrakt bootstrap constant and ratification target.
- `storageEntryBytesBaseline` is a Kontrakt bootstrap calibration constant and ratification target.
- the entry-cap derivation law is approved.
- `circuitOpenOnStorageExhaustion = true` is approved.

---

### 9.3 Approved v1 bootstrap storage governance table

```text
SMALL
- storageMultiplier               = 2.0x         (bootstrap ratification target)
- storageEntryBytesBaseline       = 1024         (bootstrap ratification target)
- maxApproxBytesPerPartition      = 20 MiB
- maxEntriesPerPartition          = 16,384
- circuitOpenOnStorageExhaustion  = true

STANDARD
- storageMultiplier               = 2.0x         (bootstrap ratification target)
- storageEntryBytesBaseline       = 1024         (bootstrap ratification target)
- maxApproxBytesPerPartition      = 40 MiB
- maxEntriesPerPartition          = 32,768
- circuitOpenOnStorageExhaustion  = true

LARGE
- storageMultiplier               = 2.0x         (bootstrap ratification target)
- storageEntryBytesBaseline       = 1024         (bootstrap ratification target)
- maxApproxBytesPerPartition      = 80 MiB
- maxEntriesPerPartition          = 65,536
- circuitOpenOnStorageExhaustion  = true
```

Notes:

- `storageMultiplier = 2.0x` is not claimed to be an externally standardized law.
  It is a Kontrakt bootstrap storage-policy constant.
- `storageEntryBytesBaseline = 1024` is not claimed to be a true average entry-size law.
  It is a Kontrakt bootstrap calibration constant used only for deriving a secondary entry fuse.

Classification:

- the entry-cap derivation law is approved,
- `circuitOpenOnStorageExhaustion = true` is approved,
- `storageMultiplier = 2×` is a Kontrakt bootstrap constant and ratification target.

---

### 10. Dispatch Lane Governance Law

#### 10.1 Separation rule

Dispatch-lane execution governance is distinct from:

- L1 structural planner sizing,
- L2 join governance,
- L2 storage governance.

It exists to control only:

- adapter-owned joined-wait completion delivery throughput,
- bounded delivery infrastructure sizing,
- bounded replay/recovery work,
- lane-local queue/store saturation thresholds.

It does not change:

- semantic output,
- shared-slot lifecycle meaning,
- waiter lifecycle meaning,
- exact-match law,
- routing identity,
- or planner-core structural caps.

#### 10.2 Internal policy snapshot rule

v1 formally adds a separate internal runtime-policy snapshot:

- `ResolvedDispatchLanePolicy`

This snapshot is:

- immutable,
- factory-issued,
- resolved only at the stable runtime-policy boundary,
- fixed for the lifetime of the installed adapter/runtime-policy epoch.

It is intentionally an internal runtime-policy concern.
It is not a user-facing low-level tuning surface.

#### 10.3 Public profile binding rule

`ResolvedDispatchLanePolicy` is derived from the same high-level public profile surface:

- `AUTO`
- `SMALL`
- `STANDARD`
- `LARGE`

For v1:

- `AUTO = STANDARD`

Dispatch-lane policy values are therefore bootstrap-profile-bound and do not adapt during the lifetime of an
already-installed policy snapshot.

#### 10.4 Required fields

`ResolvedDispatchLanePolicy` MUST define at least:

- `laneCount`
- `commandBatchBudget`
- `timeoutBatchBudget`
- `dirtyShardBatchBudget`
- `replayBatchBudgetPerShard`
- `deliveryBatchBudget`
- `commandRingCapacity`
- `readyQueueCapacity`
- `registrationStoreCapacityPerShard`
- `deadlineHeapCapacity`
- `partitionDropQuiescenceTimeoutNanos`
- `adapterCloseQuiescenceTimeoutNanos`

#### 10.5 Semantics

`commandBatchBudget`
: maximum number of lane commands drained in one worker-loop iteration

`timeoutBatchBudget`
: maximum number of due timeout entries processed in one worker-loop iteration

`dirtyShardBatchBudget`
: maximum number of dirty shards selected for replay in one worker-loop iteration

`replayBatchBudgetPerShard`
: maximum number of replayed registration entries for one selected dirty shard in one replay visit

`deliveryBatchBudget`
: maximum number of ready-queue deliveries executed in one worker-loop iteration

`commandRingCapacity`
: maximum number of pending lane commands

`readyQueueCapacity`
: maximum number of queued delivery-ready entries

`registrationStoreCapacityPerShard`
: maximum number of live registration entries allowed in one shard-owned registration segment within a lane

`deadlineHeapCapacity`
: maximum number of timeout-owned live registrations in one lane

`partitionDropQuiescenceTimeoutNanos`
: maximum wall-clock grace allowed when waiting for dispatch-lane quiescence during one partition-drop operation

`adapterCloseQuiescenceTimeoutNanos`
: maximum wall-clock grace allowed when waiting for dispatch-lane quiescence during whole-adapter shutdown

Normative clarification:

These values are **not** planner-session elapsed-time watchdogs.
They are adapter-owned shutdown/drop grace limits for dispatch convergence only.

#### 10.6 v1 ratified bootstrap dispatch-lane profile table

`````text
AUTO -> STANDARD

SMALL:
  laneCount                         = 2
  commandBatchBudget                = 32
  timeoutBatchBudget                = 16
  dirtyShardBatchBudget             = 4
  replayBatchBudgetPerShard         = 16
  deliveryBatchBudget               = 32
  commandRingCapacity               = 256
  readyQueueCapacity                = 256
  registrationStoreCapacityPerShard = 128
  deadlineHeapCapacity              = 512
  partitionDropQuiescenceTimeoutNanos = 5_000_000_000
  adapterCloseQuiescenceTimeoutNanos  = 30_000_000_000

STANDARD:
  laneCount                         = 4
  commandBatchBudget                = 64
  timeoutBatchBudget                = 32
  dirtyShardBatchBudget             = 8
  replayBatchBudgetPerShard         = 32
  deliveryBatchBudget               = 64
  commandRingCapacity               = 512
  readyQueueCapacity                = 512
  registrationStoreCapacityPerShard = 256
  deadlineHeapCapacity              = 1024
  partitionDropQuiescenceTimeoutNanos = 5_000_000_000
  adapterCloseQuiescenceTimeoutNanos  = 30_000_000_000

LARGE:
  laneCount                         = 8
  commandBatchBudget                = 128
  timeoutBatchBudget                = 64
  dirtyShardBatchBudget             = 16
  replayBatchBudgetPerShard         = 64
  deliveryBatchBudget               = 128
  commandRingCapacity               = 1024
  readyQueueCapacity                = 1024
  registrationStoreCapacityPerShard = 512
  deadlineHeapCapacity              = 2048
  partitionDropQuiescenceTimeoutNanos = 5_000_000_000
  adapterCloseQuiescenceTimeoutNanos  = 30_000_000_000
  
`````

For v1, quiescence grace values are profile-carried but currently profile-invariant.
They remain part of the immutable dispatch-lane policy snapshot so that adapter shutdown/drop behavior is stable for the
installed runtime-policy epoch.

#### 10.7 Effective lane-count rule

The effective lane count must be:

`````text
effectiveLaneCount = min(configuredLaneCount, shardCount)
`````

If needed, the effective value must still satisfy the power-of-two rule.

#### 10.8 v1 fixed-policy rule

v1 does not standardize:

- adaptive batch resizing,
- runtime feedback control,
- live queue-capacity retuning,
- live lane-count mutation.

Any future optimization of these values requires a later ADR and a newly ratified policy snapshot family.

#### 10.9 Ratification classification

The exact dispatch-lane constants above are Kontrakt-specific bootstrap constants.

They are not claimed as externally standardized values.
They are v1 ratification targets in the same sense as other Kontrakt bootstrap runtime-policy constants.

The exact quiescence grace values are also Kontrakt-specific bootstrap constants.
They are not claimed as externally standardized values.

---

## 11. Ratification Targets

The following are v1 bootstrap constants requiring explicit ratification.

### 11.1 Target List

1. `maxSignatureLen`
2. `joinWaitTimeoutNanos`
3. `maxWaitersPerKey`
4. `storageMultiplier`
5. `storageEntryBytesBaseline`
6. `depthCap sufficiency for each approved worker-budget plateau`
7. `physicalStepMultiplier`
8. `semanticWorkMultiplier`
9. `partitionDropQuiescenceTimeoutNanos`
   10 .`adapterCloseQuiescenceTimeoutNanos`

Important clarification:

- `depthCap` is not a separate public policy constant.
- It is a derived solver output.
- However, each approved worker-budget plateau MUST be ratified by verifying that its derived `maxDepthCap` is
  sufficient for the representative corpus.
- `physicalStepMultiplier` and `semanticWorkMultiplier` are explicit policy constants governing runaway-prevention
  fuses.
- `storageEntryBytesBaseline` is a storage-calibration constant used only to derive the secondary entry fuse from the
  approximate byte budget.

---

## 12. Ratification Rules

### 12.1 Representative Corpus

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

### 12.2 Execution Discipline

Ratification SHOULD be run under the framework’s existing quality discipline:

- quick repetition on local/PR lanes,
- deeper repetition on nightly/release lanes,
- deterministic seed capture,
- reproducible stress invocation.

### 12.3 Approval Rule Style

Unless otherwise stated, bootstrap ratification chooses:

> the smallest candidate that satisfies the required safety/survival condition.

This keeps v1 conservative.

---

### 12.4 `maxSignatureLen` ratification rule

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

### 12.5 `joinWaitTimeoutNanos` ratification rule

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

### 12.6 `maxWaitersPerKey` ratification rule

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

### 12.7 `storageMultiplier` ratification rule

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

### 12.8 `storageEntryBytesBaseline` ratification rule

Method:

- sweep a small approved candidate set, e.g.:
    - `512`
    - `1024`
    - `2048`

Observe:

- entry-cap-triggered `CircuitOpen` or degradation,
- whether entry-cap exhaustion occurs materially earlier than approximate-byte exhaustion,
- duplicate-build ratio,
- partition-drop pressure.

Approval rule:

- choose the smallest baseline for which the representative corpus does not observe premature entry-cap-triggered
  degradation relative to the approximate-byte budget.

Tie-breaker rule:

- if multiple candidates satisfy the condition,
  prefer the smallest candidate;
- duplicate-build ratio and partition-drop pressure MAY be used as tie-breakers.

Initial v1 bootstrap candidate:

```text
storageEntryBytesBaseline = 1024
```

Re-ratification trigger:

- canonical storage entry shape changes materially,
- approximate-byte accounting changes materially,
- representative corpus shows repeated premature entry-cap-triggered degradation,
- or storage-governance derivation is revised.

---

### 12.9 `physicalStepMultiplier` ratification rule

Method:

- sweep a small approved candidate set, e.g.:
    - `8`
    - `16`
    - `32`

Observe:

- premature physical-step exhaustion under ordinary representative workloads,
- deterministic abort under pathological rollback / branch-storm scenarios,
- whether physical-step exhaustion occurs materially earlier than structural byte exhaustion in non-pathological
  workloads.

Approval rule:

- choose the smallest multiplier that:
    - produces zero premature physical-step exhaustion in ordinary representative workloads,
    - and still acts as an effective deterministic runaway fuse for pathological workloads.

Initial v1 bootstrap candidate:

```text
physicalStepMultiplier = 16
```

Re-ratification trigger:

- planner step accounting semantics change materially,
- rollback accounting semantics change materially,
- representative corpus shows premature physical-step exhaustion in ordinary workloads,
- or step/fuel protocol boundaries change.

---

### 12.10 `semanticWorkMultiplier` ratification rule

Method:

- sweep a small approved candidate set, e.g.:
    - `2`
    - `4`
    - `8`

Observe:

- premature semantic-work exhaustion under ordinary representative workloads,
- deterministic abort under semantic expansion storms,
- whether semantic-work exhaustion becomes so loose that it loses value as a coarse runaway fuse.

Approval rule:

- choose the smallest multiplier that:
    - produces zero premature semantic-work exhaustion in ordinary representative workloads,
    - while preserving coarse-grained runaway protection.

Initial v1 bootstrap candidate:

```text
semanticWorkMultiplier = 4
```

Re-ratification trigger:

- semantic work-unit accounting changes materially,
- representative corpus shows premature semantic-work exhaustion in ordinary workloads,
- or semantic-work accounting becomes materially broader or narrower than the current protocol meaning.

---

## 13. Current v1 Bootstrap Approval State

Until the first explicit ratification pass completes, the following are treated as:

### 13.1 Approved v1 bootstrap laws

- worker-byte profile definition by plateau
- `AUTO = STANDARD`
- `tableCap = nextPowerOfTwo(max(8, 2 * nodeCap))`
- join governance is profile-independent in v1
- `maxSpeculativeBuildersPerKey = 0`
- `failFastOnQuotaExhaustion = false`
- `maxApproxBytesPerPartition = storageMultiplier × maxPlannerBytesPerWorker`
- `maxEntriesPerPartition = lowerPowerOfTwo(maxApproxBytesPerPartition / storageEntryBytesBaseline)`
- `circuitOpenOnStorageExhaustion = true`

### 13.2 Approved v1 bootstrap candidates pending ratification

- `maxSignatureLen = 4096`
- `joinWaitTimeoutNanos = 2 ms`
- `maxWaitersPerKey = 8`
- `storageMultiplier = 2.0x`
- `storageEntryBytesBaseline = 1024`
- `physicalStepMultiplier = 16`
- `semanticWorkMultiplier = 4`
- `partitionDropQuiescenceTimeoutNanos = 5 s`
- `adapterCloseQuiescenceTimeoutNanos = 30 s`
- worker-byte plateaus `10 / 20 / 40 MiB` as approved candidate plateaus pending depth-cap sufficiency ratification

---

## 14. Wall-Clock Separation

This ADR does NOT fold elapsed wall-clock policy into:

- `ResolvedSessionBudget`
- `ResolvedJoinGovernance`
- `ResolvedStorageGovernance`
- `PlannerCapacityResolver`
- `step(costCenter)` semantics

If a session-level elapsed-time watchdog is introduced, it belongs to a separate runtime policy contract.

---

## 15. Deferred Compiler-Style Policy Optimization and Platform Specialization

v1 does NOT standardize:

- live policy mutation,
- hot-path policy search,
- runtime policy adaptation that changes an already-running session,
- online search over semantic/planner-core decisions,
- reinforcement-learning controllers on the planner hot path.

However, v1 preserves explicit extension points for v2.

Future v2 work MAY introduce **compiler-/systems-style policy optimization** techniques, provided they remain outside
the semantic planner core and outside the already-running session.

The currently acknowledged candidate families are:

### 15.1 Cascades / Volcano Family (DB Optimizer Theory)

Core idea:

- memoization,
- cost-based search,
- budgeted extraction.

Interpretation:

This family provides the most direct explanatory model for Kontrakt’s future policy optimization surface.

The relevant viewpoint is:

> maintain a space of equivalent or acceptable candidates,
> then choose one by explicit cost rather than by ad hoc threshold heuristics.

Why this is relevant to Kontrakt:

- Kontrakt already contains memoization/interning structure,
- reuse and governance choices can be modeled as candidate operational policies,
- future optimization may need to choose among approved policy points under bounded resource budgets.

Intended future use:

- memoized search over approved runtime-policy candidates,
- cost-based comparison across policy points,
- bounded extraction under explicit memory / latency / survivability budgets.

Normative boundary:

- this family may optimize only **non-semantic runtime governance**,
- it MUST NOT mutate canonical semantic identity, exact-match law, or truncation correctness.

### 15.2 E-Graph / Equality Saturation Family

Core idea:

- preserve semantically equivalent alternatives in one space,
- perform equality-preserving search,
- extract the cheapest acceptable alternative under an explicit cost model.

Interpretation:

This family matches Kontrakt’s requirement that semantic output remain fixed.

The relevant viewpoint is:

> maintain equivalence,
> then extract the most operationally efficient choice without changing semantic meaning.

Why this is relevant to Kontrakt:

- Kontrakt requires cache-blind semantic determinism,
- governance changes may alter latency, memory, throughput, and survivability,
  but MUST NOT alter semantic output,
- future optimization may therefore need an equality-preserving extraction mindset rather than unconstrained search.

Intended future use:

- equality-preserving selection among approved non-semantic governance alternatives,
- cost-guided extraction under fixed semantic constraints,
- explicit separation between semantic equivalence and operational optimization.

Normative boundary:

- this family may be used only where semantic equivalence is already guaranteed by protocol law,
- it MUST NOT enlarge the semantic tuning surface.

### 15.3 Program Autotuning / Autoscheduling Family

Core idea:

- program autotuning,
- autoscheduler-style search,
- beam-search or bounded-search over schedule/policy spaces,
- cost-model-guided platform specialization,
- multi-objective / Pareto-frontier selection.

Interpretation:

This family provides the most accurate future model for platform-aware specialization in Kontrakt.

The relevant viewpoint is:

> search a bounded operational space,
> evaluate cost under deployment-specific constraints,
> then choose a platform-specialized policy point.

Why this is relevant to Kontrakt:

- Kontrakt is expected to operate across diverse deployment envelopes,
- future policy optimization may need to balance memory, latency, throughput, and survivability,
- platform-aware specialization is better explained by program autotuning than by NAS-style semantic structure search.

Intended future use:

- benchmark-driven runtime-policy specialization,
- Pareto-frontier selection across approved governance surfaces,
- deployment-envelope-specific policy selection,
- future v2 compiler-/KSP-era optimization workflows.

Normative boundary:

- specialization MUST occur only at stable boundary resolution points,
- specialization MUST publish only immutable per-session snapshots,
- specialization MUST NOT alter an already-running session.

### 15.4 Candidate Priority and Current Position

For future v2 exploration, the currently preferred order of interest is:

1. **Cascades / Volcano-style cost-based memo search**
2. **Program autotuning / autoscheduling / Pareto-frontier specialization**
3. **E-graph / equality-preserving extraction**

Interpretation:

- cost-based memo search is the most direct theoretical analogue for bounded policy selection,
- program autotuning is the most direct analogue for platform specialization,
- equality-preserving extraction is the clearest semantic-safety framing for non-semantic optimization.

At the present time, these are acknowledged as **future candidate families only**.

v1 does NOT standardize:

- any exact future optimization algorithm,
- any exact future cost-model formulation,
- any exact future benchmark corpus,
- any exact future Pareto objective set,
- any learned ranking or learned controller.

Normative restrictions for any future v2 optimization remain unchanged:

- optimization MUST remain confined to **non-semantic runtime governance**,
- optimization MUST occur only at a **stable boundary resolution point**,
- optimization MUST publish only **immutable per-session snapshots**,
- optimization MUST NOT mutate the behavior of an already-running session,
- optimization MUST NOT tune:
    - canonical semantic identity,
    - exact-match verification law,
    - protocol comparator semantics,
    - truncation correctness rules,
    - semantic output topology,
    - cache-blind semantic guarantees,
    - or L1 primitive byte-ledger laws.

Any future standardization of these techniques requires a separate follow-up ADR that defines:

- the allowed optimization surface,
- candidate representation,
- objective metrics,
- benchmark corpus requirements,
- cost-model / search / extraction method,
- publication / rollout rules,
- and required semantic-equivalence / determinism compliance tests.

---

## 16. Required Implementation Consequences

### 16.1 Runtime policy model

`ResolvedRuntimePolicy` MUST be expanded to include storage governance and MUST use factory-issued immutable snapshot
objects rather than `data class`-style copyable value carriers.

All issuance-time invariant enforcement in the policy snapshot family MUST use explicit custom exceptions.
Standard precondition helpers such as `require()`, `check()`, `error()`, and standard `IllegalArgumentException` paths
are forbidden.

Illustrative shape:

```kotlin
class ResolvedRuntimePolicy private constructor(
    val sessionBudget: ResolvedSessionBudget,
    val joinGovernance: ResolvedJoinGovernance,
    val storageGovernance: ResolvedStorageGovernance,
    val wallClockPolicy: ResolvedWallClockPolicy? = null,
) {
    companion object {
        @JvmStatic
        fun issue(
            sessionBudget: ResolvedSessionBudget?,
            joinGovernance: ResolvedJoinGovernance?,
            storageGovernance: ResolvedStorageGovernance?,
            wallClockPolicy: ResolvedWallClockPolicy? = null,
        ): ResolvedRuntimePolicy {
            if (sessionBudget == null) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedRuntimePolicy.sessionBudget must not be null"
                )
            }
            if (joinGovernance == null) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedRuntimePolicy.joinGovernance must not be null"
                )
            }
            if (storageGovernance == null) {
                throw PlanningProtocolIntegrityException(
                    "ResolvedRuntimePolicy.storageGovernance must not be null"
                )
            }

            return ResolvedRuntimePolicy(
                sessionBudget = sessionBudget,
                joinGovernance = joinGovernance,
                storageGovernance = storageGovernance,
                wallClockPolicy = wallClockPolicy,
            )
        }
    }
}
```

Normative rule:

- `ResolvedRuntimePolicy` is a policy snapshot object, not a mutable configuration bag,
- `ResolvedRuntimePolicy` MUST follow policy snapshot integrity rules,
- issuance MUST occur only at the stable policy-resolution boundary,
- issuance-time invariant enforcement MUST use explicit custom exceptions,
- installed instances MUST be treated as immutable session-fixed snapshots.

### 16.2 Resolver responsibilities

The runtime policy resolver for v1 MUST:

- map `AUTO -> STANDARD`,
- emit the approved worker-byte plateau for the selected profile,
- emit the profile-independent bootstrap join governance,
- emit the derived storage governance by the approved storage law,
- issue validated immutable policy snapshot objects,
- use explicit custom exceptions for invariant failures,
- avoid all live hot-path mutation.

### 16.3 Adapter/runtime responsibilities

The runtime boundary MUST preserve:

- session-fixed snapshot installation,
- monotonic policy epoch publication,
- best-effort/non-throwing telemetry emission,
- strict separation between current-session behavior and future policy inputs,
- and no post-install reconstruction or mutation path through public copy-style APIs.

---

## 17. Required Compliance Tests

The following tests are required in addition to already-existing L1/L2 policy tests.

### 17.1 Bootstrap policy tests

- `BootstrapPolicyTableDeterminismTest`
    - verifies identical resolved profile inputs produce identical `ResolvedRuntimePolicy`

- `ResolvedStorageGovernanceDeterminismTest`
    - verifies storage governance is deterministic for identical bootstrap inputs

- `AutoAliasesStandardTest`
    - verifies v1 `AUTO` resolves exactly to `STANDARD`

### 17.2 Ratification gate tests

- `SignatureLengthHistogramRatificationTest`
- `JoinTimeoutSweepRatificationTest`
- `WaiterCapSweepRatificationTest`
- `StorageMultiplierSweepRatificationTest`
- `DepthCapSufficiencyRatificationTest`

These may begin as build-internal verification tasks if they are too heavy for ordinary PR execution,
but they MUST exist before v1 freeze.

### 17.3 Existing invariants remain mandatory

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

## 18. Non-Goals

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
- explicit `physicalStepMultiplier` and `semanticWorkMultiplier` policy constants for runaway-prevention fuses,
- explicit `storageMultiplier` and `storageEntryBytesBaseline` bootstrap storage-policy/calibration constants,
- an explicit ratification framework for Kontrakt-specific bootstrap constants,
- and factory-issued immutable policy snapshot objects without `data class copy()` backdoors.

Kontrakt v1 does NOT claim that all exact numeric values are externally standardized.

Instead:

- external law justifies structure and range,
- the deterministic solver justifies derived outputs,
- Kontrakt-specific exact bootstrap constants are ratified against a representative corpus,
- installed policy snapshots remain immutable for the lifetime of the session,
- and issuance-time invariant enforcement uses explicit custom exceptions rather than standard precondition helpers.

This is the approved v1 policy posture until a later ADR standardizes platform-aware autotuning.