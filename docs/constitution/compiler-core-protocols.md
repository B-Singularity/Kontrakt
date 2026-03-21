# [FINAL] SOTA Compiler Core: 10 Absolute Enforcement Protocols

**Enforcement Mechanism Key:**

* **[A] Compile-Time Blockade:** Types, Visibility, Constructors, Module Boundaries.
* **[B] Static Analysis & Architecture Tests:** ArchUnit, Detekt, Bytecode Inspection.
* **[C] Runtime / Property / Stress Tests:** CI Gates, Fuzzing, Linearizability Verification.

<!-- AMENDED(2026-03-21): Clarified completion-continuation execution-path safety and telemetry signal distinction without changing protocol semantics. -->

---

### 1. Iterative Explicit Stack (Native Recursion Ban)

* **[B] Static Analysis:** The `StructuralPlanner` package MUST NOT contain any JVM method recursion, indirect mutual
  recursion, or re-entrant coroutine callbacks. ArchUnit tests MUST enforce a strict acyclic call graph within the
  planner logic.
* **[A] Compile-Time:** All graph traversals MUST be structurally implemented using `while` loops and heap-based
  Explicit Stacks.

### 2. Type-State Pre-Commit Isolation

* **[A] Compile-Time:** The tree assembly state MUST be physically separated into `LocalPlanNode` and
  `CanonicalPlanNode`.
    * `LocalPlanNode` MUST be `internal` to the session package, making external leakage physically impossible.
    * Parent nodes MUST only accept `CanonicalPlanNode` in their constructors.
    * The transition from `Local` to `Canonical` MUST only occur via a single, strictly typed `commit()` factory method
      that delegates to `L2.intern()`.

### 3. Deep Immutability, Deterministic Collections, and Safe Publication Physical Seal

This protocol is intentionally **behavioral** rather than library-prescriptive. The goal is **byte-for-byte
reproducibility**, **cache-blind semantics**, and **JMM-safe publication**.

#### 3.1 Canonical Surface (Type Seal)

* **[A] Compile-Time:** `CanonicalPlanNode` MUST be a `sealed interface` as its public surface.
* **[A] Compile-Time:** All concrete implementations MUST be strictly `final class`es with zero `open` methods.
* **[A] Compile-Time:** Canonical implementations MUST NOT be `data class` (the `copy()` backdoor is forbidden).
* **[A] Compile-Time:** Constructors MUST be non-public (prefer `private`) and instantiation MUST be routed via an
  authorized factory (e.g., `PlanInterner`, sealed deterministic wrappers).

#### 3.2 Deep Immutability (Including Element-Type Seal)

* **[B] Static Analysis:** ArchUnit MUST block the presence of `var`, `lazy` delegates, mutable buffers, or exposed
  mutable references within Canonical implementations.
* **[A] Compile-Time:** All fields reachable from a `CanonicalPlanNode` MUST be deeply immutable by construction:
    * Value Objects must be physically sealed (e.g., `TypeId`, `CanonicalIdentifier`, `CanonicalSignature`).
    * Byte arrays / primitive arrays MUST be encapsulated and MUST NOT leak references. Any inbound arrays MUST be
      defensively copied.
    * Nested collections MUST be deterministic sealed types (see 3.3). Raw `List`/`Set`/`Map` fields are forbidden.
* **[B] Static Analysis:** `this-escape` during initialization (callback registration, global registry pushing,
  publishing `this` to another thread) is strictly forbidden.

#### 3.3 Deterministic Collections (Total Order + Canonical List Semantics)

**BANNED (always):**

* `java.util.HashMap`, `java.util.HashSet`, and standard `MutableList`/`MutableMap`/`MutableSet` as stored state in
  Canonical nodes.
* `kotlinx.collections.immutable.PersistentMap` / Hash-Trie based maps when they do not guarantee deterministic key
  order.

**REQUIRED (canonical storage types):**

All collections stored inside `CanonicalPlanNode` MUST be wrapped in sealed deterministic types:

* `DeterministicMap<K, V>`
* `DeterministicSet<T>`
* `DeterministicList<T>`

##### 3.3.1 Map/Set Total Order (Comparator Consistency)

* **[A] Compile-Time:** `DeterministicMap/Set` MUST enforce a total order internally (e.g., `TreeMap`/`TreeSet`).
* **[A] Compile-Time:** The comparator MUST be consistent with equals. If `compare(a,b) == 0` then the keys MUST be
  semantically equal for canonical identity purposes. Comparator inconsistencies are a correctness bug (key loss) and
  are constitutionally forbidden.
* **[A] Compile-Time:** Ordering MUST NOT depend on:
    * Reflection enumeration order
    * HashMap iteration order
    * JVM identity hash codes
    * per-run random seeds
* **[A] Compile-Time:** If ordering is derived from versions (e.g., `edgeOrderingVersion`), the lowering function MUST
  be deterministic and version-bound (no per-process randomization).

##### 3.3.2 DeterministicList Semantics (Canonical Set-as-List)

`DeterministicList` is a **canonicalization wrapper** for “order-insensitive multi-sources”.

* **[A] Compile-Time:** `DeterministicList` MUST:
    * **deduplicate** (set semantics at construction),
    * **sort** (total order),
    * and then **freeze** into an unmodifiable view.
* **[A] Compile-Time:** Therefore, `DeterministicList` MUST NOT be used when:
    * duplicate elements are semantically meaningful,
    * or original source order must be preserved.
      In those cases, define a separate sealed wrapper with explicitly specified ordering rules (and corresponding
      ArchUnit enforcement). (Do NOT store raw `List`.)

#### 3.4 Safe Publication (JMM Happens-Before Seal)

* **[A] Compile-Time:** Safe publication for cross-thread reuse MUST be guaranteed by routing Canonical node publication
  through a JMM-compliant happens-before mechanism in L2:
    * `ConcurrentHashMap.putIfAbsent` (or equivalent), OR
    * an in-flight gate that ultimately publishes via `putIfAbsent` and only completes waiters after publication.
* **[A] Compile-Time:** If the L2 cache transitions to CircuitOpen and the session bypasses L2, the returned node MUST
  remain deeply immutable, but reference-uniqueness is best-effort (semantic determinism remains mandatory).

#### 3.5 Reference Implementation (Normative Shape, Kotlin)

> **NOTE:** Illustrative Kotlin. Exact APIs may differ. Invariants are normative.

```kotlin
class DeterministicMap<K : Comparable<K>, V> private constructor(
    private val delegate: Map<K, V>
) : Map<K, V> by delegate {
    companion object {
        fun <K : Comparable<K>, V> of(
            input: Map<K, V>,
            limit: Int,
            valueValidator: (V) -> Unit = {}
        ): DeterministicMap<K, V> {
            if (input.size > limit) throw IrProtocolViolationException("DeterministicMap entry limit exceeded ($limit).")
            input.values.forEach(valueValidator)

            val normalized = TreeMap<K, V>() // total order
            normalized.putAll(input)

            return DeterministicMap(Collections.unmodifiableMap(normalized))
        }
    }
}

class DeterministicSet<T : Comparable<T>> private constructor(
    private val delegate: Set<T>
) : Set<T> by delegate {
    companion object {
        fun <T : Comparable<T>> of(elements: Collection<T>, limit: Int): DeterministicSet<T> {
            val uniqueSorted = TreeSet<T>() // total order + dedup
            for (e in elements) {
                uniqueSorted.add(e)
                if (uniqueSorted.size > limit) throw IrProtocolViolationException("DeterministicSet unique limit exceeded ($limit).")
            }
            return DeterministicSet(Collections.unmodifiableSet(uniqueSorted))
        }
    }
}

class DeterministicList<T : Comparable<T>> private constructor(
    private val delegate: List<T>
) : List<T> by delegate {
    companion object {
        fun <T : Comparable<T>> of(elements: Collection<T>, limit: Int): DeterministicList<T> {
            val uniqueSorted = TreeSet<T>() // canonical set-as-list: total order + dedup
            for (e in elements) {
                uniqueSorted.add(e)
                if (uniqueSorted.size > limit) throw IrProtocolViolationException("DeterministicList unique limit exceeded ($limit).")
            }
            return DeterministicList(Collections.unmodifiableList(ArrayList(uniqueSorted)))
        }
    }
}
```

### 4. Centralized Fuel (Budget) Gateway

* **[A] Compile-Time:** `MaxFinalizeSteps` counters MUST be `private` state within `PlannerSession`. The ONLY mutation
  path MUST be the `step(costCenter: CostCenter)` method.
* **[B] Static Analysis:** ArchUnit MUST verify that no other logic mutates the fuel counters.
* **[C] Runtime / CI:** Property-based tests MUST verify that the step counter increments exactly according to the
  deterministic cost models (no missing increments, no bypasses).

### 5. Metamodel Pollution Ban & Monomorphic Hot-Paths

* **[B] Static Analysis:** The Domain Core MUST NOT import `kotlin.reflect.*`, `java.lang.reflect.*`, or `KS*`. ArchUnit
  MUST enforce zero reflections in the core. The core MUST only consume `TypeFactsDTO`.

* **[A] Compile-Time:** Hot-path Outbound Ports (`NodeIdIndexer`, `CanonicalEdgeKeyProvider`) MUST use narrow primitive
  signatures (`ULong`, `Int`).

* **[A] Compile-Time (AMENDED — ULong Boxing Avoidance):**
  Kotlin/JVM unsigned value classes (`ULong`, `UInt`) are **boxed when used as type arguments** in generic collections.
  Therefore, within hot-path core logic:
    * **BANNED:** `Map<ULong, *>`, `Set<ULong>`, `List<ULong>` (and the same for `UInt`) in `kontrakt-planning` hot
      paths.
    * **REQUIRED:** store 64-bit identities as **raw `Long` bit patterns** in primitive arrays / primitive maps.
      `ULong` may exist at API boundaries (DTOs, port method parameters) but MUST be converted immediately to `Long`
      for indexing/storage.

* **[A] Compile-Time:** The DI or binding mechanism MUST inject these ports exactly once at initialization. Reassignment
  or mutable delegate bindings are forbidden to guarantee JIT Devirtualization (Monomorphic callsites).

### 6. Sentinel Reservation & Deterministic Remapping

* **[A] Compile-Time:** `ULong.MAX_VALUE` (`-1L`) is strictly reserved as the `+INF` sentinel for RMQ.
* **[A] Compile-Time (Resolution):** `CanonicalEdgeKeyProvider` MUST NEVER emit `ULong.MAX_VALUE`. If the lowering
  function yields this value, it MUST be remapped to a non-MAX value using a **strictly deterministic function bound to
  the `edgeOrderingVersion`**. Any resulting collisions are treated as equivalent and resolved securely by Protocol #7.

### 7. Two-Phase Identity & False Cycle Extinction

* **[A] Compile-Time:** The `NodeIdIndexer` MUST implement a strict Two-Phase Verification yielding a dense
  `nodeId: Int`.
    * *Phase 1:* Fast bucket routing via `nodeIdentity64: ULong`.
    * *Phase 2:* Absolute byte-equality comparison of the Canonical Signature.

* **[A] Compile-Time (AMENDED — Primitive Map Enforcement):**
  `NodeIdIndexer` MUST be implemented as a **primitive open-addressing index**:
    * Routing/index keys stored as `Long` bit patterns (`identity64.toLong()`).
    * Core tables MUST be `LongArray`/`IntArray` plus an epoch/stamp array for O(1) reset.
    * **BANNED:** `HashMap`, `MutableMap`, or any `Map<ULong, *>` usage inside `NodeIdIndexer` (boxing + GC storm).

* **[A] Compile-Time (Resolution):** The Canonical Signature used in Phase 2 MUST be generated by a Single Source of
  Truth (SSOT) function **strictly bound to the `normalizationVersion`**. This function MUST output a stable UTF-8 byte
  array with complete determinism by: strictly stripping nullability, type aliases, and use-site annotations; enforcing
  exact ordering of generic arguments; standardizing nested type notation; and resolving absolute canonical
  package/class names.

* **[A] Compile-Time:** `edgeRank` collisions are treated as equivalent. The SSOT `less()` function MUST
  deterministically break ties favoring the smaller `stackIndex`.

### 8. Absolute Zero-Residue Cleanup

* **[A] Compile-Time:** The `PlannerSession` execution block MUST be wrapped in a `try/finally` block where
  `resetToCleanState()` is hardcoded in the `finally` clause.
* **[C] Runtime / CI:** Stress tests using thread pools MUST intentionally throw Hard Aborts and immediately reuse the
  worker to verify that `depth=0`, `GreyMap` traces, and `NodeIdIndexer` epochs are perfectly reset without leaking
  references to ThreadLocals or telemetry payloads.

### 9. Persistent Pipeline Integrity (Discard Rule)

* **[A] Compile-Time:** Deserialized payload objects from disk/network MUST be mapped to a distinct `RawPayloadNode`
  type. Because `CanonicalPlanNode` is a `sealed interface`, `RawPayloadNode` is physically prevented from implementing
  it.
* **[A] Compile-Time:** The only way to convert a `RawPayloadNode` into a `CanonicalPlanNode` is by passing it through
  `L2.intern()`. The raw instance MUST fall out of scope immediately, enforcing garbage collection.

### 10. Cache-Blind Determinism Automation

* **[C] Runtime / CI:** The CI pipeline MUST include Fuzzing and Property-based Tests that execute identical structural
  requests against an artificially "Cold" L2 cache and an artificially "Hot/Concurrent" L2 cache.
* **[C] Runtime / CI:** The test MUST assert that the resulting `treeSemanticCostUpperBound` and the topological
  structure are bit-for-bit identical, and that concurrent linearizability (no dirty reads of incomplete nodes) is
  strictly maintained.
* **[C] Runtime / CI (AMENDED):** The above MUST also run under:
    * In-Flight Gate OFF vs ON/AUTO
    * Partition bulk-drop between runs ensuring semantics remain cache-blind.

### 11. SSOT vs Policy Boundary (Resource Budgets)

SSOT (protocol) fixes:

- CostCenter identities and tick definitions,
- track mapping (PhysicalOnly / SemanticAlso),
- determinism invariants (cache-blind semantic determinism),
- fail-closed conditions for overflow and contract violations.

Policy (runtime/execution strategy) provides:

- numeric budget defaults and environment-sensitive sizing,
- worker count and per-worker memory budget selection.

Protocol MUST NOT depend on environment introspection.
Environment discovery MUST be implemented via ports/adapters, and policy values are injected into protocol-bound runtime
objects.

### 12. Session-Fixed Policy Snapshot Immutability (AMENDED)

* **[A] Compile-Time / Boundary Rule:** Protocol-bound runtime objects inside the compiler/planner core MUST consume
  only
  already-resolved, immutable policy snapshots.
* **[A] Compile-Time:** The core MUST NOT read live environment signals such as:
    * heap state,
    * cgroup/container memory,
    * CPU count,
    * GC kind,
    * scheduler state.
* **[A] Compile-Time:** The core MUST NOT read live adaptive telemetry in order to mutate:
    * step budgets,
    * semantic budgets,
    * join timeout values,
    * speculative quota,
    * or any equivalent governance value
      during an already-running session.
* **[C] Runtime / CI:** If a newer policy snapshot is installed concurrently, an already-running session MUST continue
  using the snapshot it started with. Newer snapshots may apply only to subsequently created sessions.

### 13. Telemetry Payload Discipline & Zero-Residue Boundary (AMENDED)

* **[A] Compile-Time:** Telemetry payloads emitted from planner/L2 code MUST be numeric/event-oriented and MUST NOT
  retain references to planner object graphs, mutable worker-local state, or canonical node subgraphs.
* **[A] Compile-Time:** If an adapter-local telemetry sink exists, it MUST remain best-effort / non-throwing and MUST
  NOT become a semantic dependency of the planner core.
* **[B] Static Analysis:** ArchUnit / bytecode inspection SHOULD verify that telemetry helpers do not capture
  `PlannerSession`, `CanonicalPlanNode`, `LocalPlanNode`, or equivalent mutable session state into long-lived stores.
* **[C] Runtime / CI:** Worker reuse tests MUST verify no semantic state remains reachable through telemetry pipelines
  after reset / hard abort.

#### 13.1 Completion Continuation Execution-Path Safety (AMENDED)

* **[A] Compile-Time / Boundary Rule:** Completion continuation execution on the builder publication path MUST NOT
  re-enter the L2 shard path.
* **[A] Compile-Time / Boundary Rule:** Implementations MAY dispatch waiter continuations to a separate executor or an
  equivalent completion queue to prevent lock inversion or publication-path contamination.
* **[A] Compile-Time / Boundary Rule:** This requirement does not alter publication-before-completion ordering; it only
  constrains how waiter continuations may execute after terminalization becomes observable.

#### 13.2 Telemetry Signal Distinction Rule (AMENDED)

* **[A] Compile-Time / Boundary Rule:** Telemetry for waiter-attach rejection and telemetry for speculative-builder
  quota exhaustion MUST remain distinct signals and MUST NOT be merged into a single counter or event kind.
* **[C] Runtime / CI:** Telemetry validation tests SHOULD verify that implementations can distinguish:
    * ordinary attach rejection, and
    * speculative quota exhaustion after timeout/degrade handling.

### 14. Wall-Clock Policy Separation (AMENDED)

* **[A] Compile-Time:** Wall-clock elapsed-time policy is NOT protocol fuel semantics.
* **[A] Compile-Time:** Step counters and cost-center tick semantics MUST NOT be reinterpreted as wall-clock durations
  through a dynamic exchange-rate inside the core.
* **[A] Compile-Time:** If the runtime introduces a session-level elapsed-time watchdog, it belongs to a separate
  runtime/boundary policy concern and MUST NOT be folded into:
    * `step(costCenter)` semantics,
    * `DeterministicList/Map/Set` rules,
    * `NodeIdIndexer` two-phase identity law,
    * or cache exact-match correctness.
* **[C] Runtime / CI:** Wall-clock policy changes may alter abort timing or operational survivability, but MUST NOT
  alter:
    * topology,
    * canonical signatures,
    * protocol-comparator-driven truncation choice,
    * or semantic outputs declared semantic by protocol.

### 15. Policy Registry Publication Safety (AMENDED)

* **[A] Compile-Time / Boundary Rule:** If a resolved policy snapshot is published to future sessions through a runtime
  registry, that publication MUST be safe under the JVM memory model.
* **[A] Compile-Time:** Minimum acceptable publication forms include:
    * `@Volatile` immutable snapshot reference, or
    * `AtomicReference<PolicyEpoch>` / equivalent immutable snapshot holder.
* **[A] Compile-Time:** If epoch identifiers are used, they MUST increase monotonically.
* **[C] Runtime / CI:** Tests MUST verify that newly created sessions observe either the old or the new fully-published
  snapshot, but never a partially-installed one.

### 16. Adaptive Resolver Stability & Cold-Start Rule (AMENDED)

* **[A] Compile-Time / Boundary Rule:** Adaptive policy resolution is allowed only outside the protocol core.
* **[A] Compile-Time:** If `AUTO` or telemetry-driven resolution is used, the resolver MUST include a stability control
  to prevent oscillation between successive policy snapshots.
  Allowed techniques include:
    * exponential moving average (EMA),
    * hysteresis bands,
    * minimum hold epochs,
    * clamped update steps,
    * equivalent smoothing controls.
* **[A] Compile-Time:** Cold-start resolution (no usable historical telemetry) MUST choose deterministic conservative
  defaults.
* **[C] Runtime / CI:** Adaptive policy changes may alter throughput / waiting / survivability behavior only.
  They MUST NOT alter semantic output.