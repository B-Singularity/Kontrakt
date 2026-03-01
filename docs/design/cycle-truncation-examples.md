# Examples: Edge-Aware Deterministic Cycle Truncation (ADR-0030)

This document provides concrete examples for `StructuralPlanner` implementing the **Iterative Explicit Stack** model.

> **AMENDED (Hot-Path Identity Rule):** All identity routing used by the planner/session MUST respect Kotlin/JVM boxing
> realities:
> * `ULong`/`UInt` are boxed when used as generic type arguments.
> * Therefore, any hot-path identity indexing (e.g., `NodeIdIndexer`, GreyMap membership) MUST be implemented with *
    *primitive arrays / primitive maps** over raw `Long` bit patterns (store `identity64.toLong()`), and MUST NOT use
    `Map<ULong, *>` / `Map<Long, *>` in the hot path.

---

## 1. Exception Mapping, Domain Purity & Reality Defense

### Scenario 1.1: Invalid Canonical Key Component (User Error)

**Context:** Kotlin developer uses backticks with reserved characters (`var `prop|with|pipes`: String = ""`).

* **Core Action:** Reality Defense detects `|` in the `Name` component.
* **Result:** `PlanningException.InvalidCanonicalKeyComponent(faultKind = USER_MODEL_INVALID)`.
* **Adapter Translation:** "Your class contains a property with an invalid character '|'."

### Scenario 1.2: Invalid Canonical Key Component (Build Pipeline Error)

**Context:** The user code is completely normal. However, a bytecode obfuscator (e.g., ProGuard/R8) renamed a field to
`a|b`. The JVM retains this without marking it synthetic.

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

---

## 3. Budgeting & Limits (Iterative Execution)

### Scenario 3.1: Step Count Monotonicity (CPU-DoS Defense)

**Context:** `MaxPlanningSteps = 100`.

1. **Step 50:** Planner pushes Frame A.
2. **Step 51:** Planner pushes Frame B.
3. **Step 52:** Cycle detected. **Rollback** to Frame A.
    * **State Restoration:** Stack, Budget snapshots (excluding step counter), Cache Log restored.
    * **Step Counter:** Remains **52** (Strictly Monotonic. Explicitly excluded from snapshot to prevent CPU-DoS via
      infinite rollback loops).
4. **Step 53:** Planner tries alternative path for A.

### Scenario 3.2: Hard Cap Violation (Fatal with Cleanup)

**Context:** `MaxLiveNodeCap = 10`. `CurrentActiveNodes = 10`.

1. **Action:** `EXPAND_EDGE` tries to allocate Node 11.
2. **Check:** `10 + 1 > 10`.
3. **Fatal Sequence:**
    1. **Cleanup:** Rollback Transaction Log & Cache Log to initial state (ensure zero residue).
    2. **Abort:** Throw `CapacityExceededException(limitType="LiveNodeCap", faultKind=RESOURCE_EXHAUSTED)`.

### Scenario 3.3 (AMENDED): L2 Capacity Governor -> CircuitOpen -> Deterministic Bypass

**Context:** Partition `P` exceeds `maxEntries` during a commit-heavy run.

1. **Step N:** Planner enters `L2_CAPACITY_CHECK` and detects `entries + 1 > maxEntries`.
2. **Action:** L2 transitions to `CircuitOpen` for that partition and emits telemetry.
3. **Domain Reaction:** Treat as `Fault(CircuitOpen)` and bypass L2 for the remainder of the session.
4. **Determinism Rule:** The resulting IR topology and `treeSemanticCostUpperBound` MUST remain identical to the
   non-circuit-open run. Only sharing / throughput differs.

### Scenario 3.4 (AMENDED): In-Flight Gate Join is Fuel-Bounded and Degrades Safely

**Context:** 64 threads miss the same hot PlanCacheKey concurrently.

1. Thread T0 acquires the in-flight slot (`L2_INFLIGHT_ACQUIRE`) and becomes the builder.
2. Threads T1..T63 observe the in-flight slot and join:
    * Each join tick consumes `L2_INFLIGHT_WAIT` fuel.
    * If fuel is low, joiners degrade to bypass (or blind race) deterministically.
3. Completion:
    * T0 publishes via `L2_PUBLISH_PUT_IF_ABSENT` (safe publication).
    * Only after publication do joiners observe the canonical instance.

> **AMENDED (Hot-Path Guarantee):**
> * In-flight tracking MUST NOT be implemented as `ConcurrentHashMap<ULong, …>` / `ConcurrentHashMap<Long, …>` where the
    key is a boxed type argument.
> * If the in-flight gate keys include any 64-bit identity components, they MUST be stored/routed using primitive
    structures (e.g., shard-local primitive tables), or keyed by an already-allocated dense `Int` handle.

### Scenario 3.5 (AMENDED): NodeIdIndexer Probing is Fuel-Bounded and Boxing-Free

**Context:** An attacker crafts a model that triggers many near-collisions on `nodeIdentity64` causing long probe
chains.

1. **Step N:** Planner calls `NODEID_PHASE1_ROUTE` to compute `identity64.toLong()` and initial slot.
2. **Probe Loop:**
    * Each open-addressing step MUST charge `NODEID_PROBE_STEP`.
    * Each candidate signature compare MUST charge `NODEID_PHASE2_SCAN`.
3. **Fuel Exhaustion:**
    * If fuel is exhausted mid-probe, the planner MUST HardAbort deterministically (no partial publication).
4. **Boxing Rule:**
    * The indexer MUST NOT allocate boxed keys (`Map<ULong, *>`, `Map<Long, *>`) at any point on the hot path.
    * All routing MUST remain `LongArray`/`IntArray`-based.

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

**Type-Stable Determinism (The Cost of TDE):**
**Context:** The type `com.example.Order` appears twice in the graph.

* **Result:** Cache Hit returns the exact same `PlanNode`. Both instances share the *exact same structural blueprint*.
  Data diversity for the two orders MUST be supplied by Runtime Value Generators.

**Temporal Variance & Index Shift Immunity:**
**Context:** Node `Order` needs a randomized `createdAt` date.

* `RootTime`: `2026-01-01T00:00:00Z`
* Max Offset: `2592000000L` ms
* TargetKey: `"com.example.Order|PROPERTY|createdAt|java.time.Instant"` *(DeclarationIndex is deliberately omitted)*.

1. **Derivation:** Execute SHA-256 on the exact UTF-8 byte sequence of
   `"12345|com.example.Order|sha-256:abcd...|TIME_OFFSET|com.example.Order|PROPERTY|createdAt|java.time.Instant"`.
2. **Mapping:** Extract unsigned 64-bit bits into a raw `Long` value `uBits` (bit pattern). Compute modulo using
   unsigned semantics:
   `Long.remainderUnsigned(uBits, 2592000000L)`.
   > **AMENDED (Boxing Avoidance):** do NOT model `u` as `ULong` inside hot paths or store it in `List<ULong>` /
   `Map<ULong, *>`.
   > Keep it as a raw `Long` bit pattern and use `Long.compareUnsigned` / `Long.remainderUnsigned`.
3. **Result:** `createdAt = RootTime.plusMillis(OffsetMillis)`.# Examples: Edge-Aware Deterministic Cycle Truncation (
   ADR-0030)

This document provides concrete examples for `StructuralPlanner` implementing the **Iterative Explicit Stack** model.

> **AMENDED (Hot-Path Identity Rule):** All identity routing used by the planner/session MUST respect Kotlin/JVM boxing
> realities:
> * `ULong`/`UInt` are boxed when used as generic type arguments.
> * Therefore, any hot-path identity indexing (e.g., `NodeIdIndexer`, GreyMap membership) MUST be implemented with *
    *primitive arrays / primitive maps** over raw `Long` bit patterns (store `identity64.toLong()`), and MUST NOT use
    `Map<ULong, *>` / `Map<Long, *>` in the hot path.

---

## 1. Exception Mapping, Domain Purity & Reality Defense

### Scenario 1.1: Invalid Canonical Key Component (User Error)

**Context:** Kotlin developer uses backticks with reserved characters (`var `prop|with|pipes`: String = ""`).

* **Core Action:** Reality Defense detects `|` in the `Name` component.
* **Result:** `PlanningException.InvalidCanonicalKeyComponent(faultKind = USER_MODEL_INVALID)`.
* **Adapter Translation:** "Your class contains a property with an invalid character '|'."

### Scenario 1.2: Invalid Canonical Key Component (Build Pipeline Error)

**Context:** The user code is completely normal. However, a bytecode obfuscator (e.g., ProGuard/R8) renamed a field to
`a|b`. The JVM retains this without marking it synthetic.

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

---

## 3. Budgeting & Limits (Iterative Execution)

### Scenario 3.1: Step Count Monotonicity (CPU-DoS Defense)

**Context:** `MaxPlanningSteps = 100`.

1. **Step 50:** Planner pushes Frame A.
2. **Step 51:** Planner pushes Frame B.
3. **Step 52:** Cycle detected. **Rollback** to Frame A.
    * **State Restoration:** Stack, Budget snapshots (excluding step counter), Cache Log restored.
    * **Step Counter:** Remains **52** (Strictly Monotonic. Explicitly excluded from snapshot to prevent CPU-DoS via
      infinite rollback loops).
4. **Step 53:** Planner tries alternative path for A.

### Scenario 3.2: Hard Cap Violation (Fatal with Cleanup)

**Context:** `MaxLiveNodeCap = 10`. `CurrentActiveNodes = 10`.

1. **Action:** `EXPAND_EDGE` tries to allocate Node 11.
2. **Check:** `10 + 1 > 10`.
3. **Fatal Sequence:**
    1. **Cleanup:** Rollback Transaction Log & Cache Log to initial state (ensure zero residue).
    2. **Abort:** Throw `CapacityExceededException(limitType="LiveNodeCap", faultKind=RESOURCE_EXHAUSTED)`.

### Scenario 3.3 (AMENDED): L2 Capacity Governor -> CircuitOpen -> Deterministic Bypass

**Context:** Partition `P` exceeds `maxEntries` during a commit-heavy run.

1. **Step N:** Planner enters `L2_CAPACITY_CHECK` and detects `entries + 1 > maxEntries`.
2. **Action:** L2 transitions to `CircuitOpen` for that partition and emits telemetry.
3. **Domain Reaction:** Treat as `Fault(CircuitOpen)` and bypass L2 for the remainder of the session.
4. **Determinism Rule:** The resulting IR topology and `treeSemanticCostUpperBound` MUST remain identical to the
   non-circuit-open run. Only sharing / throughput differs.

### Scenario 3.4 (AMENDED): In-Flight Gate Join is Fuel-Bounded and Degrades Safely

**Context:** 64 threads miss the same hot PlanCacheKey concurrently.

1. Thread T0 acquires the in-flight slot (`L2_INFLIGHT_ACQUIRE`) and becomes the builder.
2. Threads T1..T63 observe the in-flight slot and join:
    * Each join tick consumes `L2_INFLIGHT_WAIT` fuel.
    * If fuel is low, joiners degrade to bypass (or blind race) deterministically.
3. Completion:
    * T0 publishes via `L2_PUBLISH_PUT_IF_ABSENT` (safe publication).
    * Only after publication do joiners observe the canonical instance.

> **AMENDED (Hot-Path Guarantee):**
> * In-flight tracking MUST NOT be implemented as `ConcurrentHashMap<ULong, …>` / `ConcurrentHashMap<Long, …>` where the
    key is a boxed type argument.
> * If the in-flight gate keys include any 64-bit identity components, they MUST be stored/routed using primitive
    structures (e.g., shard-local primitive tables), or keyed by an already-allocated dense `Int` handle.

### Scenario 3.5 (AMENDED): NodeIdIndexer Probing is Fuel-Bounded and Boxing-Free

**Context:** An attacker crafts a model that triggers many near-collisions on `nodeIdentity64` causing long probe
chains.

1. **Step N:** Planner calls `NODEID_PHASE1_ROUTE` to compute `identity64.toLong()` and initial slot.
2. **Probe Loop:**
    * Each open-addressing step MUST charge `NODEID_PROBE_STEP`.
    * Each candidate signature compare MUST charge `NODEID_PHASE2_SCAN`.
3. **Fuel Exhaustion:**
    * If fuel is exhausted mid-probe, the planner MUST HardAbort deterministically (no partial publication).
4. **Boxing Rule:**
    * The indexer MUST NOT allocate boxed keys (`Map<ULong, *>`, `Map<Long, *>`) at any point on the hot path.
    * All routing MUST remain `LongArray`/`IntArray`-based.

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

**Type-Stable Determinism (The Cost of TDE):**
**Context:** The type `com.example.Order` appears twice in the graph.

* **Result:** Cache Hit returns the exact same `PlanNode`. Both instances share the *exact same structural blueprint*.
  Data diversity for the two orders MUST be supplied by Runtime Value Generators.

**Temporal Variance & Index Shift Immunity:**
**Context:** Node `Order` needs a randomized `createdAt` date.

* `RootTime`: `2026-01-01T00:00:00Z`
* Max Offset: `2592000000L` ms
* TargetKey: `"com.example.Order|PROPERTY|createdAt|java.time.Instant"` *(DeclarationIndex is deliberately omitted)*.

1. **Derivation:** Execute SHA-256 on the exact UTF-8 byte sequence of
   `"12345|com.example.Order|sha-256:abcd...|TIME_OFFSET|com.example.Order|PROPERTY|createdAt|java.time.Instant"`.
2. **Mapping:** Extract unsigned 64-bit bits into a raw `Long` value `uBits` (bit pattern). Compute modulo using
   unsigned semantics:
   `Long.remainderUnsigned(uBits, 2592000000L)`.
   > **AMENDED (Boxing Avoidance):** do NOT model `u` as `ULong` inside hot paths or store it in `List<ULong>` /
   `Map<ULong, *>`.
   > Keep it as a raw `Long` bit pattern and use `Long.compareUnsigned` / `Long.remainderUnsigned`.
3. **Result:** `createdAt = RootTime.plusMillis(OffsetMillis)`.