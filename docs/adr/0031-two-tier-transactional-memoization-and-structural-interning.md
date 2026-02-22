# 31. Two-Tier Transactional Memoization and Structural Interning

Date: 2026-02-22
Status: Accepted
Normative
References: [ADR-0017](0017-worker-based-isolation.md), [ADR-0030](0030-edge-aware-deterministic-cycle-truncation-strategy.md)

## Context

Following the adoption of the Iterative Explicit Stack architecture (ADR-0030), the `StructuralPlanner` requires a
highly robust memoization (caching) strategy. The caching mechanism is not merely a performance optimization; it is a *
*Domain Core constraint** that guarantees Type-Stable Determinism (TDE), prevents infinite loops, and mitigates DoS
attacks.

We must establish explicit Hexagonal boundaries via DTOs for KSP readiness, enforce strict operational DoS limits
through resource accounting (which is cache-sensitive by nature), while ensuring that semantic determinism remains
strictly cache-blind.

## Decision

We adopt a **Two-Tier Transactional Memoization Architecture** enhanced by **Deterministic Structural Interning**.

### 1. Strict Key Separation via Type System (Domain Invariant MUST)

1. **`NodeIdentityKey` (Cycle Detection):** Abstract key representing active path identity.
    * **CRITICAL MUST:** A collision in this key causes a "false cycle," altering truncation semantics. This key MUST be
      mathematically bound to the `normalizationVersion` and MUST be collision-resistant.
    * **CRITICAL MUST:** To prevent hot-path allocation, `nodeIdentity64: ULong` MUST be precomputed by the
      `TypeFactsProvider` and provided within the `TypeFactsDTO`.
    * **CRITICAL MUST:** If a keyed hash is used, the key MUST be deterministic and version-bound (derived solely from
      `normalizationVersion` and stable configuration) and MUST NOT be randomized per process/run.
2. **`PlanCacheKey` (Memoization / Interning):** A comprehensive tuple:
   `[workAccountingVersion, normalizationVersion, edgeOrderingVersion, capabilityProfileVersion, entropyVersion, partitionKey, equalityKey]`.
3. **`CanonicalEdgeKey` (Deterministic Choice):** Derived from the canonical ordering tuple via a deterministic lowering
   into a 64-bit ordering key (`edgeRank: UInt64`).
    * **CRITICAL MUST:** The lowering function (and any internal seeds/keys) MUST be deterministic and bound to
      `edgeOrderingVersion` (no per-run randomization).
    * **CRITICAL MUST (Sentinel Reservation):** `ULong.MAX_VALUE` is strictly reserved for `+INF`. The
      `CanonicalEdgeKeyProvider` MUST NEVER emit this value; if the lowering algorithm yields it, it MUST be
      deterministically remapped.
    * **CRITICAL MUST (Collision Equivalency):** `edgeRank` defines the canonical ordering key. If distinct edges map to
      an identical `edgeRank` (due to hash abbreviation), they MUST be treated as semantically equivalent for ordering
      purposes. Ties MUST be resolved solely via the stack-index rule (prioritizing the edge closest to the root).

### 2. Tier 1 (L1): Domain Session (Worker-Local Transaction)

* **Cycle Segment Definition:** The cycle edge set is strictly defined as
  `{incomingEdgeRankAtDepth[cycleStartIndex + 1] ... incomingEdgeRankAtDepth[currentDepth]} U {backEdge}`.
* **Worker-Local State Cleanup (CRITICAL MUST):** The worker MUST reset the `PlannerSession` to a clean state on ANY
  exit path (Success, Fault, or Hard Abort). This MUST be done in a `finally` block by unwinding to `depth = 0`,
  resetting the `NodeIdIndexer` (e.g., via epoch increment or safe per-request recreation), and clearing membership
  strictly along the active stack.
* **Dual-Track Budgeting Model (CRITICAL MUST):** 1.  **Physical DoS Limits (Cache-Sensitive):** `MaxFinalizeSteps`,
  `MaxDepthCap`, and `MaxNodeIdCap`. `MaxFinalizeSteps` MUST increment on every hot-path cost center: frame dispatch,
  key materialization, RMQ updates, and every `L2.get/intern` attempt.
    2. **Semantic Work Budget (Cache-Blind Determinism):** The `MaxSemanticWorkUnits` evaluates the logical weight of
       the final graph. The Core MUST Hard Abort if and only if
       `root.treeSemanticCostUpperBound > MaxSemanticWorkUnits`. This definitive evaluation guarantees identical
       outcomes regardless of L2 cache states (Hot vs. Cold).

### 3. Tier 2 (L2): Global In-Memory Plan Repository (Outbound Port)

* **Domain Reaction to Faults (CRITICAL MUST):** `CacheFault` MUST NOT contain diagnostic payloads.
    1. **`Fault(Transient)`:** Degrade to a **Cache Miss**.
    2. **`Fault(CircuitOpen)`:** Bypass L2 for the remainder of the Session.
* **Linearizability, Exact-Instance & Safe Publication (CRITICAL MUST):** `intern` MUST guarantee `putIfAbsent`
  atomicity. `get` MUST return the exact canonical instance. Adapters MUST use JMM-compliant concurrent structures (
  `happens-before`). `PlanNode` and all backing collections MUST be final and structurally immutable.

### 4. Deterministic Commit Protocol & Persistent Pipeline (CRITICAL MUST)

1. **Pre-Commit Isolation:** `L2.intern` MUST ONLY be invoked during the bottom-up commit phase. A Hard Abort MAY occur
   during the commit phase; any partial L2 writes that occurred before the abort are semantically harmless.
2. **DAG Consistency Guarantee:** On `Fault(_)`, the Core MUST store `nodeToIntern` into the `CanonicalSubstitutionMap`.
3. **Persistent Canonicalization Rule:** Data retrieved from the Persistent Payload Store MUST be immediately routed
   through the `L2.intern` operation. The raw deserialized payload instance MUST be instantly discarded. If the L2 port
   transitions to `CircuitOpen`, the Persistent Payload Store MUST concurrently be bypassed.

### 5. Hexagonal Port Boundaries (CRITICAL MUST)

The Domain Core MUST NEVER interact directly with `KClass`, `java.lang.reflect.*`, or `KS*` types.

* **Outbound (Driven) Ports:**
    * `TypeFactsProvider`: Supplies normalized `TypeFactsDTO` (including precomputed `nodeIdentity64`).
    * `NodeIdIndexer`: Maps abstract keys to dense integers.
    * `CanonicalSignatureProvider`
    * `CanonicalEdgeKeyProvider`: Guarantees the `edgeOrderingVersion` and outputs `edgeRank: UInt64`.
    * `PlanInternRepository` (L2 Port)
    * `PlanPayloadStore` (Persistent Load Port)
    * `CacheTelemetrySink`

## Invariant Checklist for Property-Based Testing

* [ ] **Cache-Blind Semantic Determinism:** Executing identical requests with an empty vs. populated L2 cache MUST
  result in identical `MaxSemanticWorkUnits` Hard Abort outcomes, evaluated strictly on the final root node.
* [ ] **Cache-Sensitive Operational Reality:** `MaxFinalizeSteps` exhaustion MAY differ between hot vs. cold caches, but
  MUST NOT affect semantic Hard Abort outcomes governed solely by `root.treeSemanticCostUpperBound`.
* [ ] **Worker Pooling Purity:** An aborted session followed by a new request on the same worker MUST NOT leak
  `GreyMap`, `NodeIdIndexer`, or RMQ state.
* [ ] **Persistent Pipeline Integrity:** Deserialized payloads MUST NEVER bypass L2 interning, and the Persistent store
  MUST NOT be queried if the L2 Cache is `CircuitOpen`.