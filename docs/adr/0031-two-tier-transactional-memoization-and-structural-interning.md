# 31. Two-Tier Transactional Memoization and Structural Interning

Date: 2026-02-21

Status: Accepted

Normative
References: [ADR-0017](0017-worker-based-isolation.md), [ADR-0030](0030-edge-aware-deterministic-cycle-truncation-strategy.md)

## Context

Following the adoption of the Iterative Explicit Stack architecture (ADR-0030), the `StructuralPlanner` requires a
highly robust memoization (caching) strategy. The caching mechanism is not merely a performance optimization; it is a *
*Domain Core constraint** that guarantees Type-Stable Determinism (TDE), prevents infinite loops, and mitigates DoS
attacks.

We evaluated multiple compiler-level caching architectures. Global immediate-publishing or coroutine-based cooperative
single-flight sharing were rejected because they risk deadlocks during cycle evaluation and cache pollution upon
rollback.

To achieve State-of-the-Art (SOTA) memory efficiency while preserving Domain Purity, Worker Isolation, and Zero-Residue
Rollbacks, we need an architecture that seamlessly integrates transaction-local state tracking with global lock-free
structural sharing (Interning). Furthermore, the design must survive JVM realities, prevent "slow-death" operational
degradation, and defend against schema evolution.

## Decision

We adopt a **Two-Tier Transactional Memoization Architecture** enhanced by **Deterministic Structural Interning**. The
Domain Core strictly owns the memoization and canonicalization policies, while global storage semantics, fault
translation, and observable SLAs are pushed to Outbound Ports (Hexagonal Architecture).

### 1. Strict Key Separation via Type System (Domain Invariant MUST)

To prevent logical collisions, the Domain Core **MUST** strictly isolate identifier keys using the language's strong
type system (e.g., Kotlin `@JvmInline value class`).

1. **`NodeIdentityKey` (Cycle Detection):** `StrippedIdentitySignature`. Used by `ActiveStack` to track "Grey Nodes".
2. **`PlanCacheKey` (Memoization / Interning):** `[FullCanonicalTypeSignature, RootSeed, capabilityFingerprint]`.
   Identifies fully resolved blueprints.
3. **`EntropyTargetKey` / `CanonicalEdgeKey` (Deterministic Choice):** Used for edge ordering and TDE expansion.

### 2. Tier 1 (L1): Domain Session (Worker-Local Transaction)

The L1 cache is a pure Domain object (`PlannerSession`) instantiated per worker thread.

* **Active Path Tracking:** MUST maintain a set/stack of `NodeIdentityKey`s currently being explored.
* **Staging Log (Ordered):** MUST maintain an insertion-ordered record of `PlanCacheKey -> PlanNode`.
* **Staging Uniqueness (MUST):** The L1 Session MUST NOT finalize and stage the same `PlanCacheKey` twice within a
  single un-rolled-back execution path.
    * *Diagnostics:* Violations MUST throw `PlannerInvariantViolation.DuplicateFinalize` enriched with diagnostic
      payloads to distinguish true framework bugs from legal re-entries.
* **Zero-Residue Rollback:** Upon exploring a discarded branch, the L1 Session MUST restore its internal snapshot,
  discarding only the staged nodes created after the snapshot index.

### 3. Tier 2 (L2): Global Plan Repository (Outbound Port)

The L2 cache is an Outbound Port representing a cross-worker shared storage. The Port contract dictates *observable
behaviors*, not internal implementations.

* **Explicit Result API Contract (MUST):** To eliminate null-ambiguity and exception-based control flow, the Port MUST
  expose explicit Algebraic Data Types (ADTs):
    1. `get(key): CacheQuery<PlanNode>` *(where `CacheQuery = Hit(T) | Miss | Fault(CacheFault)`)*
    2. `intern(key, node): CacheOperation<PlanNode>` *(where `CacheOperation = Success(T) | Fault(CacheFault)`)*
* **Strict Fault Taxonomy (CRITICAL MUST):** To prevent infrastructure details from leaking into the Domain, the
  `CacheFault` ADT MUST be strictly closed to exactly two variants: `Transient` and `CircuitOpen`. Adapters MUST NEVER
  leak specific critical causes (e.g., OOM, Data Corruption) externally. They MUST internally absorb these errors and
  translate them strictly into an internal circuit open state (yielding `Fault(CircuitOpen)`) or downgrade them to
  `Fault(Transient)`.
* **Linearizability, Read Consistency & Safe Publication (MUST):** 1.  **Linearizability:** The `intern` operation MUST
  guarantee strict `putIfAbsent` semantics.
    2. **Read Consistency:** The `get` operation MAY return `Miss` at any time (e.g., due to eviction races or memory
       pressure). However, if it returns `Hit(node)`, the returned instance MUST strictly satisfy three conditions: (a)
       it correctly matches the `PlanCacheKey` semantics, (b) it satisfies all Safe Publication guarantees, and (c) **it
       MUST be the exact canonical instance held by the L2 cache at that moment** (returning a newly allocated,
       structurally equivalent instance is a specification violation).
    3. **Safe Publication:** Adapters MUST ensure *happens-before* JVM guarantees by utilizing JMM-compliant concurrent
       structures. `PlanNode` MUST be deeply immutable. *Recommendation:* Relying solely on Kotlin's read-only
       interfaces backed by mutable state is strongly discouraged; use structurally immutable/persistent collections or
       defensive copies.
* **Observable SLA & Prohibited Actions:** 1. Cross-worker `await`/`join` and synchronous Disk/Network I/O in the hot
  path are **STRICTLY PROHIBITED**.
    2. **Wall-Clock Budgets (MUST):** Adapters MUST enforce strict bounds on `max_wall_clock_duration`. Exceeding the
       budget MUST immediately yield `Fault(Transient)`. *Implementation Note:* Adapters SHOULD utilize sampling or
       interval-based telemetry rather than timing every single invocation to minimize overhead.
* **Telemetry Separation:** Adapters MUST expose operational telemetry via a separate `CacheTelemetrySink` port.
* **Domain Fault Reaction (MUST):** The Core reacts strictly to the two allowed Faults:
    1. **`Fault(Transient)`:** The Core degrades the operation to a **Cache Miss**.
    2. **`Fault(CircuitOpen)`:** While the Adapter's internal circuit is `OPEN`, it fast-fails with this fault. The Core
       MUST bypass L2 for the remainder of the **Session** (defined strictly as the lifespan of the current
       `PlannerSession` evaluating a single root request). *Non-normative Note:* Upon a new Session, the Adapter MAY
       transition to `HALF_OPEN` according to its own retry policies.

### 4. Deterministic Structural Interning & Commit Protocol (MUST)

"Address Identity" (Pointer Equality, `===`) is a **best-effort memory optimization**. Eviction policies MAY remove
canonical instances but MUST NEVER break structural equality.

The Domain Core MUST execute the following **Bottom-Up Canonicalization Protocol**:

1. **Cycle Check Precedence (CRITICAL MUST):** Before querying any cache, the Core MUST check the `ActiveStack`. **If a
   cycle is detected, ADR-0030 rules apply immediately, bypassing all cache lookups.**
2. **Read-Only Re-use During Traversal:** If no cycle exists, L2 `Hit(node)` results via `get` are reused immediately.
3. **Topological Guarantee:** Nodes are appended to the L1 Staging Log exactly once, strictly upon the successful
   completion of their **`FINALIZE_NODE`** frame.
4. **Pre-Commit Isolation (CRITICAL MUST):** `L2.intern` MUST ONLY be invoked during the bottom-up commit phase.
   Invoking `intern` during the active traversal phase is a fatal violation of rollback purity.
5. **Canonical Substitution Map:** The Core initializes a temporary `CanonicalSubstitutionMap<PlanCacheKey, PlanNode>`.
6. **Interning & Substitution (CRITICAL MUST):** For each local node in the Staging Log:
    * **Reassembly Minimality:** The Core resolves dependencies via `canonicalMap[childKey] ?: localChild`. It MUST
      reinstantiate a new immutable parent node *if and only if* at least one child reference changes, resulting in
      `nodeToIntern`.
    * The Core invokes `L2.intern(key, nodeToIntern)`.
    * **DAG Consistency Guarantee:** The Core MUST store the final chosen instance into the `CanonicalSubstitutionMap`
      regardless of the L2 outcome. On `Success(CanonicalNode)`, it stores the canonical node. On `Fault(_)`, it MUST
      store `nodeToIntern`.

### 5. Persistent Cache Adapters & Fast-Path Schema Correctness

Implementations **MAY** provide Persistent Adapters for the L2 Port. **If provided, they MUST comply with the following
constraints**:

* **Environment-Aware Transitive Fingerprinting:** 1.  **Fast-Path (Delegation):** The Adapter MAY leverage strong
  environment-provided ABI hashes. Mismatches result in `Miss`.
    2. **Slow-Path (Merkle-Lite):** The Adapter MUST compute
       `TransitiveSchemaFingerprint = SHA-256( LocalFacts(A) + Sorted(Fingerprint(Child_1) ... Fingerprint(Child_N)) )`.
* **LocalFacts Definition Constraint:** `LocalFacts` MUST include all raw facts affecting 'PlanNode Semantics', strictly
  excluding volatile data like `memberOrigin`.
* **Load-Time Reality Defense (MUST):** Upon retrieval, the Core MUST explicitly assert version matches, OOM safety
  limits, and NFC/Injectivity.

## Invariant Checklist for Property-Based Testing

* [ ] **Key Immutability:** Identifiers are strongly typed.
* [ ] **Pre-Commit Isolation (Zero L2 Mutation):** Exceptions thrown prior to entering the commit phase guarantee
  exactly 0 modifications to the L2 Port from the aborted session.
* [ ] **Partial Commit Resilience:** Injecting a fault mid-way through the commit phase MUST NOT alter the structural
  determinism or equivalence of the resulting `PlanNode` graph. Partial L2 writes are semantically harmless.
* [ ] **CanonicalMap Totality during Commit:** By the end of the commit phase, every key present in the L1 Staging Log
  MUST have a corresponding entry in the `CanonicalSubstitutionMap`, regardless of the L2 `intern` outcome.
* [ ] **DAG Consistency under Faults:** Injecting a `Fault(Transient)` during the `intern` of a child node MUST NOT
  cause its parent node to lose the structural updates applied to that child.
* [ ] **Reassembly Minimality:** Re-committing an unmodified tree results in $0$ new object instantiations.
* [ ] **Cycle Overwrite Prevention:** Caches are never queried if the `ActiveStack` confirms a cycle.
* [ ] **Safe Publication:** Concurrent readers of an L2 `Hit` observe a deeply immutable and fully initialized graph
  state.
* [ ] **Adapter Resilience:** Injecting a `Fault(Transient)` gracefully yields a `Miss`. Injecting a `CRITICAL` fault
  triggers an `OPEN` circuit within the Adapter, successfully shifting the Domain to L1-only mode via
  `Fault(CircuitOpen)`.

## Consequences

* **Positive:** Hexagonal boundaries are mathematically strict. The Domain owns correctness, while Adapters are
  constrained by a tightly closed ADT (`Transient` | `CircuitOpen`) preventing taxonomy leaks.
* **Positive:** Operational resilience. Wall-clock latency budgets (optimized via sampling) and state-machined Circuit
  Breakers prevent infrastructure failures from causing system-wide "slow-death".
* **Positive:** Sound memory reduction via Synchronous Bottom-Up Canonicalization, absolutely resilient to partial
  commit failures without breaking DAG consistency.