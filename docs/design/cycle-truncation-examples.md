# Examples: Edge-Aware Deterministic Cycle Truncation (ADR-0030)

This document provides concrete examples for `StructuralPlanner` implementing the **Iterative Explicit Stack** model.

> **Normative Authority Boundary**
> This document is **illustrative**.
> Normative authority for protocol, budgeting, worker lifecycle, safe publication, and Tier-2 join governance belongs
> to:
> * `ADR-0030`
> * `ADR-0032`
> * `compiler-core-protocols.md`
> * `l1-planner-session-primitive-data-structures.md`
> * `l2-plan-interner-partitioned-tier2-with-governance.md`
> * `planner-budget-resolution-and-worker-lifecycle.md`
>
> If any example in this document conflicts with those normative documents, the normative documents win.

> **AMENDED (Hot-Path Identity Rule):**
> All identity routing used by the planner/session MUST respect Kotlin/JVM boxing realities:
> * `ULong` / `UInt` are boxed when used as generic type arguments.
> * Therefore, any hot-path identity indexing (e.g., `NodeIdIndexer`, GreyMap membership) MUST be implemented with
    >   **primitive arrays / primitive maps** over raw `Long` bit patterns (store `identity64.toLong()`),
    >   and MUST NOT use `Map<ULong, *>` / `Map<Long, *>` in the hot path.

> **AMENDED (Breakpoint Interpretation Boundary):**
> Cycle breakpoint selection in these examples is a **planner-time semantic decision**.
> Therefore:
> * the candidate breakpoint MUST be chosen from the **current active cycle segment**,
> * the examples MUST NOT be interpreted as permitting selection based on previously materialized child results,
> * concrete realization forms (`null`, empty collection, skip assignment, diagnostic stub) belong to downstream
    >   linking/materialization behavior, not to the breakpoint-selection comparator itself.

---

## 1. Exception Mapping, Domain Purity & Reality Defense

### Scenario 1.1: Invalid Canonical Key Component (User Error)

**Context:** Kotlin developer uses backticks with reserved characters (`var \`prop|with|pipes\`: String = ""`).

* **Core Action:** Reality Defense detects `|` in the `Name` component.
* **Result:** `PlanningException.InvalidCanonicalKeyComponent(faultKind = USER_MODEL_INVALID)`.
* **Adapter Translation:** "Your class contains a property with an invalid character '|'."

### Scenario 1.2: Invalid Canonical Key Component (Build Pipeline Error)

**Context:** The user code is completely normal. However, a bytecode obfuscator (e.g., ProGuard/R8) renamed a field to
`a|b`.
The JVM retains this without marking it synthetic.

* **Core Action:** Reality Defense detects `|` in the `Name` component.
* **Result:** `PlanningException.InvalidCanonicalKeyComponent(faultKind = USER_MODEL_INVALID)`.
* **Adapter Translation:** "An invalid property name containing '|' was detected ('a|b'). If you are using a bytecode
  obfuscator or compiler plugin, please adjust its dictionary settings to avoid reserved characters."

### Scenario 1.3: Port Contract Violation (Strict NFC Failure)

**Context:** A custom `TypeResolver` adapter returns a property name using decomposed Unicode characters.

* **Core Action:** Reality Defense assertion `name == normalizeNFC(name)` fails.
* **Result:** Immediately aborts with
  `PlanningException.PortContractViolation("Non-NFC component provided by Port", faultKind = FRAMEWORK_INVARIANT_BROKEN)`.

### Scenario 1.4: Ambiguous Entropy Target Key (Dynamic Attribution - Framework Error)

**Context:** Two properties generate the exact same `EntropyTargetKey` (omitting `DeclarationIndex`).

* **Evaluation:** The Core applies the **Dynamic Fault Rule** to determine responsibility.
* **Evidence:** The Port reports one member as `DECLARED` and the other as `SYNTHETIC` (e.g., a compiler-generated
  delegate field). Alternatively, the `typeSignatureNormalizationVersion` is `null`.
* **Result:** Because non-declared members or missing version info are involved, the Core defaults to
  `PlanningException.AmbiguousEntropyTargetKey(faultKind = FRAMEWORK_INVARIANT_BROKEN)`.

---

## 2. Cycle Breaking (Tri-Stage Priority)

### Scenario 2.1: Capability-Restricted Cycle with Demotion Evidence

**Context:** Cycle `A -> B -> A`.

* **Facts:** `A.b` is `internal var`. `CapabilityProfile` is `PUBLIC_API_ONLY`.
* **Core Action:** Planner demotes `A.b` to **Ignored** and logs a
  `DemotionRecord(owner="A", name="b", reason="Visibility restricted", requiredCapabilityHint="ALLOW_INTERNAL_SETTERS")`.
* **Result:** Stage 3 Fail-Fast. Throws `PlanningException.CycleDetected`.
* **Payload Insight:**
    * `capabilityDemotions` is sorted by `EntropyTargetKey` to ensure diagnostics are deterministic even if
      `DeclarationIndex` changes.
    * `truncated`: `false`
    * `faultKind`: `CAPABILITY_RESTRICTED`

### Scenario 2.2 (AMENDED): Candidate Breakpoint Is Chosen from the Active Cycle Segment

**Context:**  
Traversal reaches the following active stack:

* `Root`
* `Order`
* `Customer`
* `Address`
* `Order`

The re-entry into `Order` is detected while expanding `Address.owner`.

The active cycle segment is therefore:

* `Order -> Customer -> Address -> Order`

Visible edges in that segment:

* `Order.customer` = **Strong**
* `Customer.primaryAddress` = **Deferred**
* `Address.owner` = **Substitutable**

1. The planner detects active-path re-entry for `Order`.
2. The planner computes the **current active cycle segment** only.
3. The planner evaluates only the participating active edges of that segment.
4. The planner applies tri-stage ordering:
    * Deferred first
    * then Substitutable
    * otherwise fail-fast
5. Result:
    * `Customer.primaryAddress` wins because it is the minimum eligible **Deferred** candidate in the cycle segment.

> **Rule:**  
> The planner MUST NOT pick a breakpoint from:
> * a non-participating sibling edge,
> * a previously materialized child result,
> * a cache winner that happened to complete earlier.

### Scenario 2.3 (NEW): Stage 1 Deferred Edge Wins over a Lower-Priority Substitutable Edge

**Context:** Cycle `A -> B -> C -> A`.

Cycle-segment edges:

* `A.depB` = **Strong**
* `B.lateService` = **Deferred**
* `C.optionalBackRef` = **Substitutable**

Comparator inputs:

* `B.lateService` canonical key = `com.example.B|PROPERTY|lateService|com.example.Service|2`
* `C.optionalBackRef` canonical key = `com.example.C|PROPERTY|optionalBackRef|com.example.A?|0`

1. The planner detects the cycle.
2. It first filters only **Deferred** edges.
3. The candidate set contains exactly one edge: `B.lateService`.
4. The planner inserts `UnlinkedDeferredNode` at that breakpoint.
5. The Substitutable candidate is ignored because Stage 1 already succeeded.

**Interpretation:**  
Even if `C.optionalBackRef` has a lexicographically smaller or "more convenient" downstream runtime realization, Stage 1
priority dominates.

### Scenario 2.4 (NEW): Stage 2 Substitutable Edge Wins When No Deferred Edge Exists

**Context:** Cycle `Parent -> Child -> Parent`.

Cycle-segment edges:

* `Parent.child` = **Strong**
* `Child.parent` = **Substitutable** (`Parent?`)

No Deferred edges exist.

1. The planner detects the cycle on `Parent`.
2. Stage 1 finds no eligible Deferred edge.
3. Stage 2 evaluates Substitutable candidates.
4. `Child.parent` is selected.
5. The planner inserts `SubstitutionNode(reason = NULL, structuralPath = "...")`.

**Important Boundary:**  
The planner-time selection ends at `SubstitutionNode`.
The later effect may be realized downstream as:

* `null`,
* skipped assignment,
* an empty substitute for a protocol-specific container,
* or another valid protocol realization.

The planner comparator itself does **not** choose among those runtime realization forms.

### Scenario 2.5 (NEW): No Breakable Edge Exists -> Deterministic Fail-Fast

**Context:** Cycle `Engine -> Piston -> Engine`.

Cycle-segment edges:

* `Engine.primaryPiston` = **Strong**
* `Piston.engine` = **Strong**

There are:

* no Deferred edges
* no Substitutable edges
* no capability-based demotions that would make either edge breakable

1. The planner detects the cycle.
2. Stage 1 finds no Deferred candidate.
3. Stage 2 finds no Substitutable candidate.
4. Stage 3 triggers deterministic failure.
5. Result:
    * `PlanningException.CycleDetected`
    * `truncated = false`

> **Reason:**  
> The planner must not invent unsafe dummy values merely to avoid failure.

### Scenario 2.6 (NEW): Cache State Must Not Perturb Breakpoint Choice

**Context:**  
The same recursive schema is planned twice with identical semantic inputs:

* same root seed
* same `CapabilityProfile`
* same normalized type facts
* same constructor selection

The cycle segment is:

* `Invoice -> LineItem -> Invoice`

Eligible candidates in the segment:

* `Invoice.lines` = **Substitutable**
* `LineItem.owner` = **Deferred**

#### Run A: Cold Cache

* no Tier-2 entry exists
* all children are planned freshly

#### Run B: Hot Cache / Reuse / Different Join Timing

* some neighboring children are Tier-2 hits
* one branch returns earlier due to reuse
* thread scheduling differs

#### Required Outcome

Both runs MUST choose the same breakpoint:

* `LineItem.owner` wins in both runs because it is the Stage 1 Deferred candidate.

The following are allowed to differ:

* reuse count
* object sharing
* retention
* throughput
* latency

The following MUST NOT differ:

* chosen breakpoint
* final IR topology
* canonical signatures
* semantic truncation meaning

> **Boundary Rule:**  
> Cache state and governance may change performance characteristics, but MUST NOT alter the protocol-comparator-driven
> truncation choice.

---

## 3. Budgeting & Limits (Iterative Execution)

### Scenario 3.1 (AMENDED): Monotonic Physical/Semantic Counters vs Rollback-Scoped Checkpoints

**Context:**  
`maxPhysicalSteps = 100`, `maxSemanticWorkUnits = 40`.

The current run has already consumed:

* physical = `50`
* semantic = `11`

The current frame-local rollback checkpoint contains:

* `stackPointer = 7`
* `placeholderCounter = 3`
* `builderLogPos = 9`
* `cacheLogPos = 4`

1. **Dispatch:** Planner explores a speculative branch from Frame A.
2. **Work Performed:**
    * physical counter increases from `50 -> 51 -> 52`
    * semantic counter remains `11` if no semantic-also event occurs
3. **Cycle / Failure:** The speculative branch is rejected and the planner rolls back to Frame A.
4. **Rollback-Scoped Restoration:**
    * `stackPointer` is restored to `7`
    * `placeholderCounter` is restored to `3`
    * `builderLogPos` is restored to `9`
    * `cacheLogPos` is restored to `4`
5. **Monotonicity Rule:**
    * physical counter remains **52**
    * semantic counter remains **11**
    * neither counter is rolled back
6. **Rationale:**  
   Rollback can restore planner-local checkpoints, but it MUST NOT erase already-consumed physical work.
   This prevents infinite rollback loops from becoming a free CPU-DoS vector.

> **Invariant:**  
> Rollback restores only **rollback-scoped checkpoints**.  
> Monotonic physical / semantic counters remain outside the snapshot boundary.

### Scenario 3.2 (AMENDED): ResolvedPlannerSessionCaps Violation Fails Closed with Semantic Zero-Residue

**Context:**  
The runtime was constructed from `ResolvedPlannerSessionCaps`.

Relevant caps:

* `maxNodeIdCap = 10`
* `indexerTableCap = 32`
* `undoLogCap = 64`
* `maxDepthCap = 16`

Current state:

* `nextNodeId = 10`
* a new `EXPAND_EDGE` branch attempts one more node allocation

1. **Action:** The planner attempts to allocate Node 11.
2. **Check:** `nextNodeId + 1 > maxNodeIdCap`.
3. **Violation Classification:** This is a **hard capacity violation**, not a soft degradation signal.
4. **Fail-Closed Sequence:**
    1. rollback branch-local state
    2. abort the current planning run with
       `CapacityExceededException(limitType = "NODE_ID_CAP", faultKind = RESOURCE_EXHAUSTED)`
    3. execute session cleanup / worker-local reset
5. **Zero-Residue Interpretation:**  
   Primitive arrays or slabs MAY still physically contain old bytes after reset.
   That is acceptable **iff** those bytes are no longer semantically reachable through:
    * active stack pointers
    * node counts
    * indexer heads / epochs
    * builder log positions
    * cache log positions
6. **Result:**  
   The run fails deterministically and leaves no semantically reachable residue for the next run.

> **Invariant:**  
> Zero-residue does **not** require full memory zero-fill.  
> It requires **semantic non-reachability** of discarded state.

### Scenario 3.3 (AMENDED): L2 Capacity Governor -> CircuitOpen -> Deterministic Bypass

**Context:** Partition `P` exceeds `maxEntries` during a commit-heavy run.

1. **Step N:** Planner enters `L2_CAPACITY_CHECK` and detects `entries + 1 > maxEntries`.
2. **Action:** L2 transitions to `CircuitOpen` for that partition and emits telemetry.
3. **Domain Reaction:** Treat as `Fault(CircuitOpen)` and bypass L2 for the remainder of the session.
4. **Determinism Rule:** The resulting semantic output MUST remain identical to the non-circuit-open run:
    * final IR topology
    * canonical signatures
    * exact-match canonical correctness
    * protocol-comparator-driven truncation choice
    * `treeSemanticCostUpperBound`
5. **Allowed Differences:** Only non-semantic dimensions may differ:
    * reference sharing
    * cache retention
    * throughput / latency
    * memory survival behavior

> **Boundary Rule:**  
> L2 governance MAY change reuse behavior, but MUST NOT perturb semantic output.

### Scenario 3.4 (AMENDED): In-Flight Join Uses Event-Based Waiter Lifecycle and Degrades Safely

**Context:** 64 threads miss the same hot `PlanCacheKey` concurrently.

1. **Builder Election**
    * Thread `T0` performs `L2_INFLIGHT_ACQUIRE`, wins the shared in-flight slot, and becomes the designated builder.
    * The shared slot remains in `PENDING` state until publication is completed.

2. **Joiner Admission**
    * Threads `T1..T63` observe the existing shared slot.
    * Each joiner performs a fast-path peek and records `L2_INFLIGHT_ATTACH`.
    * If the slot is already completed, the joiner returns immediately after authoritative re-verification.
    * If the slot is not yet completed, the joiner attempts waiter attach subject to governance:
        * reject attach if the partition is already closed
        * reject attach if the shared slot is already `DROPPED`
        * reject attach if waiter count has reached `maxWaitersPerKey`

3. **Attached Waiters Are Non-Blocking**
    * A successful joiner transitions only its own waiter lifecycle to `ATTACHED`.
    * The current worker thread MUST be released.
    * Waiting MUST proceed via completion continuation / callback, not bounded `parkNanos` polling.
    * The wait deadline is interpreted as a **monotonic elapsed-time deadline**:
      `slot.startedAtNanos + joinWaitTimeoutNanos`.

4. **Builder Publication and Successful Resume**
    * `T0` publishes via `L2_PUBLISH_PUT_IF_ABSENT`.
    * Publication correctness is still sealed only by:
        * bucket-level exact re-check, and
        * publication-before-completion ordering.
    * Only after publication has linearized may the shared slot complete successfully.
    * When the shared slot completes normally:
        * each resumed waiter records `L2_INFLIGHT_RESUME`
        * each resumed waiter transitions from `ATTACHED` to `RESUMED`
        * each resumed waiter observes the canonical winner only after publication
        * a waiter that previously timed out does not invalidate the later successful observation by other waiters

5. **Timeout Path**
    * If a waiter reaches its monotonic deadline before slot completion:
        * record `L2_INFLIGHT_TIMEOUT`
        * transition only that waiter to `TIMED_OUT`
    * Timeout MUST NOT fail the shared slot.
    * Timeout MUST NOT automatically promote the waiter to builder.

6. **Speculative Builder Quota**
    * A timed-out waiter MAY become a speculative builder only if:
      `slot.speculativeBuilders < maxSpeculativeBuildersPerKey`.
    * If speculative promotion is denied because quota is exhausted:
        * record `L2_INFLIGHT_QUOTA_EXHAUST`
        * policy MUST choose one of:
            * deterministic bypass (`builder()` without intern), or
            * fail-fast / degraded miss according to governance policy

7. **Cancellation Path**
    * If an attached waiter is cancelled:
        * record `L2_INFLIGHT_CANCEL`
        * transition only that waiter to `CANCELLED`
    * Waiter cancellation MUST NOT cancel the shared slot.

8. **Determinism Rule**
    * Governance differences in join waiting MUST NOT alter:
        * final IR topology
        * canonical signatures
        * exact-match canonical correctness
        * protocol-comparator-driven truncation choice
        * `treeSemanticCostUpperBound`
    * Only sharing, retention, latency, or throughput may differ.

> **AMENDED (Hot-Path Guarantee):**
> * In-flight tracking MUST NOT be implemented as `ConcurrentHashMap<ULong, …>` / `ConcurrentHashMap<Long, …>`
    >   where the key is a boxed type argument.
> * If the in-flight gate keys include any 64-bit identity components, they MUST be stored/routed using primitive
    >   structures (e.g., shard-local primitive tables), or keyed by an already-allocated dense `Int` handle.

### Scenario 3.5 (NEW): Partition Drop / Attach Race Completes Without Hang

**Context:**  
Partition `P` is being administratively dropped while a joiner is attempting to attach to an existing in-flight slot.

Shared slot state before the race:

* slot state = `PENDING`
* region state = `OPEN`

Two concurrent actions occur:

* **Action A:** Joiner `T7` tries to attach.
* **Action B:** Admin thread closes the region and performs partition drop.

#### Branch A: `DROPPED` Wins Before Attach Linearizes

1. The close winner performs one-way close transition for the region.
2. The shared slot is terminalized as `DROPPED` before `T7` attach becomes visible.
3. `T7` observes `DROPPED` and fails attach immediately.
4. Result:
    * `T7` does **not** remain parked / pending
    * no new waiter is admitted after close

#### Branch B: Attach Linearizes Before `DROPPED`

1. `T7` successfully transitions its waiter lifecycle to `ATTACHED`.
2. The admin thread then performs region close and begins in-flight sweep.
3. The sweep MUST terminalize every visible attached waiter before region reclamation.
4. `T7` is resumed exceptionally and does **not** remain pending forever.
5. Result:
    * attach/drop race is resolved without hang
    * reclamation occurs only after visible in-flight terminalization

#### Forbidden Outcome

The following outcome is forbidden:

* region reclaimed
* attached waiter still pending
* no completion / failure signal delivered

> **Liveness Rule:**  
> `DROPPED` beating attach causes immediate attach failure.  
> Attach beating `DROPPED` requires guaranteed wake-up / terminalization before reclamation.

### Scenario 3.6 (AMENDED): NodeIdIndexer Probing is Fuel-Bounded and Boxing-Free

**Context:** An attacker crafts a model that triggers many near-collisions on `nodeIdentity64`, causing long probe
chains.

1. **Step N:** Planner calls `NODEID_PHASE1_ROUTE` to compute `identity64.toLong()` and the initial slot.
2. **Probe Loop:**
    * Each open-addressing step MUST charge `NODEID_PROBE_STEP`.
    * Each candidate signature compare MUST charge `NODEID_PHASE2_SCAN`.
3. **Fuel Exhaustion:**
    * If physical budget is exhausted mid-probe, the planner MUST hard-abort deterministically.
    * No partial publication may escape the failed branch.
4. **Boxing Rule:**
    * The indexer MUST NOT allocate boxed keys (`Map<ULong, *>`, `Map<Long, *>`) at any point on the hot path.
    * All routing MUST remain `LongArray` / `IntArray`-based.

### Scenario 3.7 (NEW): Policy Epoch Freeze Prevents Mid-Session Recalibration Drift

**Context:**  
The runtime boundary has already resolved a `ResolvedRuntimePolicy` for epoch `41`.

1. A session starts under epoch `41`.
2. During the session, runtime telemetry indicates that join waits are slower than expected.
3. The system MAY compute a new policy snapshot for epoch `42`.
4. However, the currently running session MUST continue using epoch `41` values until it ends.
5. Only sessions created after epoch `42` installation may observe the new timeout/quota values.

> **Rule:**  
> Policy adaptation is allowed only at a stable policy-resolution boundary.  
> Mid-session policy mutation is forbidden.

### Scenario 3.8 (NEW): Cold Start Uses Conservative Defaults

**Context:**  
The runtime starts with no prior telemetry.

1. `AUTO` resolution is requested.
2. No historical `RecentTelemetry` snapshot is available.
3. The resolver chooses conservative defaults:
    * timeout is not aggressively short,
    * speculative builder quota is low,
    * fail-fast on quota exhaustion may be enabled,
    * no session-local recalibration occurs after session start.
4. Telemetry gathered from completed sessions may inform the next policy epoch.

> **Rule:**  
> Cold-start behavior MUST remain deterministic, conservative, and compatible with fail-closed semantics.

---

## 4. Exception Payload (Plan Violation at Runtime)

**Context:** User accesses `lateinit` property `dep` in `init` block during VM generation phase.

```json
{
  "exception": "PlanViolationException",
  "payload": {
    "path": [
      "<root>:com.example.PrematureAccess",
      "[PROPERTY]dep:com.example.Service"
    ],
    "edgeStrength": "DEFERRED",
    "memberKind": "PROPERTY",
    "substitutionKind": null,
    "causeType": "kotlin.UninitializedPropertyAccessException",
    "sanitizedMessage": "lateinit property dep has not been initialized",
    "runtimeFaultKind": "USER_CODE_VIOLATION",
    "capabilityFingerprint": "sha-256:a1b2c3d4e5f60718293a4b5c6d7e8f901234567890abcdef1234567890abcdef",
    "seed": "18446744073709551615",
    "entropyVersion": "TDE-SHA256-v1",
    "rootTime": "2026-01-01T00:00:00Z"
  }
}
```

---

## 5. Type-Derived Entropy & Clock Examples

### Type-Stable Determinism (The Cost of TDE)

**Context:** The type `com.example.Order` appears twice in the graph.

* **Result:** Cache Hit returns the exact same `PlanNode`.
  Both instances share the **exact same structural blueprint**.
  Data diversity for the two orders MUST be supplied by Runtime Value Generators.

### Temporal Variance & Index Shift Immunity

**Context:** Node `Order` needs a randomized `createdAt` date.

* `RootTime`: `2026-01-01T00:00:00Z`
* Max Offset: `2592000000L` ms
* TargetKey: `"com.example.Order|PROPERTY|createdAt|java.time.Instant"`
  *(DeclarationIndex is deliberately omitted)*.

1. **Derivation:** Execute SHA-256 on the exact UTF-8 byte sequence of
   `"12345|com.example.Order|sha-256:abcd...|TIME_OFFSET|com.example.Order|PROPERTY|createdAt|java.time.Instant"`.
2. **Mapping:** Extract unsigned 64-bit bits into a raw `Long` value `uBits` (bit pattern). Compute modulo using
   unsigned semantics:
   `Long.remainderUnsigned(uBits, 2592000000L)`.
   > **AMENDED (Boxing Avoidance):** do NOT model `u` as `ULong` inside hot paths or store it in `List<ULong>` /
   `Map<ULong, *>`.
   > Keep it as a raw `Long` bit pattern and use `Long.compareUnsigned` / `Long.remainderUnsigned`.
3. **Result:** `createdAt = RootTime.plusMillis(OffsetMillis)`.