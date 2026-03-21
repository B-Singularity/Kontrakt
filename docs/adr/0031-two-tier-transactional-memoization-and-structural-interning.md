# 31. Two-Tier Transactional Memoization and Structural Interning

Date: 2026-02-22

Status: Accepted (Amended: 2026-03-01)
<!-- AMENDED(2026-03-02): Added ULong boxing avoidance + NodeIdIndexer primitive-map mandate -->
<!-- AMENDED(2026-03-21): Clarified in-flight attach terminal-signal guarantees, commit/abort terminalization discipline, speculative-builder reservation release, drop-sweep linearizability, and callback execution-path safety without changing prior semantic policy. -->
<!-- AMENDED(2026-03-21): Clarified slot-owned speculative leases and attach-rejection vs quota-exhaustion distinction without introducing new domain fault surfaces. -->

Normative

References: ADR-0017, ADR-0030

## Context

Following the adoption of the Iterative Explicit Stack architecture (ADR-0030), the `StructuralPlanner` requires a
highly robust memoization (caching) strategy. The caching mechanism is not merely a performance optimization; it is a
**Domain Core constraint** that guarantees Type-Stable Determinism (TDE), prevents infinite loops, and mitigates DoS
attacks.

We must establish explicit Hexagonal boundaries via DTOs for KSP readiness, enforce strict operational DoS limits
through resource accounting (which is cache-sensitive by nature), while ensuring that semantic determinism remains
strictly cache-blind.

Kotlin/JVM note: ULong is a value class and can BOX when it crosses generic/Any boundaries.
Therefore, all hot-path indexing (NodeIdIndexer, GreyMap membership, etc.) MUST be implemented using primitive storage
(Long/Int arrays + primitive hashing), and MUST NOT use `Map<ULong, *>` or other boxed-key generic collections.
This amendment is performance-critical and constitutionally enforced as part of "Monomorphic Hot-Paths".

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
    * **CRITICAL MUST (Hot-Path Physical Constraint):** Although `nodeIdentity64` is modeled as `ULong` at the DTO
      surface, the Domain Core MUST immediately extract and route it as a **primitive 64-bit bitpattern**
      (e.g., `identityBits: Long`) for all indexing and membership operations. The core MUST NOT store `ULong` inside
      generic collections (boxing risk).

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
    * **NOTE (Performance):** Any hot-path storage of `edgeRank` sequences (e.g., RMQ inputs, stack-edge arrays) MUST
      use primitive arrays (`LongArray` / `ULongArray`) and MUST NOT route `edgeRank` through boxed generic collections.

### 2. Tier 1 (L1): Domain Session (Worker-Local Transaction)

* **Cycle Segment Definition:** The cycle edge set is strictly defined as
  `{incomingEdgeRankAtDepth[cycleStartIndex + 1] ... incomingEdgeRankAtDepth[currentDepth]} U {backEdge}`.
* **Worker-Local State Cleanup (CRITICAL MUST):** The worker MUST reset the `PlannerSession` to a clean state on ANY
  exit path (Success, Fault, or Hard Abort). This MUST be done in a `finally` block by unwinding to `depth = 0`,
  resetting the `NodeIdIndexer` (e.g., via epoch increment or safe per-request recreation), and clearing membership
  strictly along the active stack.
* **CRITICAL MUST (Primitive Reset):** `NodeIdIndexer` reset MUST be O(1) epoch-based or O(n_active) stack-based and
  MUST NOT depend on clearing boxed maps. Any membership/index tables used by GreyMap MUST be primitive structures.

* **Dual-Track Budgeting Model (CRITICAL MUST):**
    1. **Physical DoS Limits (Cache-Sensitive):** `MaxFinalizeSteps`, `MaxDepthCap`, and `MaxNodeIdCap`.
       `MaxFinalizeSteps` MUST increment on every hot-path cost center: frame dispatch, key materialization,
       RMQ updates, and every `L2.get/intern` attempt.
    2. **Semantic Work Budget (Cache-Blind Determinism):** The `MaxSemanticWorkUnits` evaluates the logical weight of
       the final graph. The Core MUST Hard Abort if and only if
       `root.treeSemanticCostUpperBound > MaxSemanticWorkUnits`. This definitive evaluation guarantees identical
       outcomes regardless of L2 cache states (Hot vs. Cold).

### 3. Tier 2 (L2): Global In-Memory Plan Repository (Outbound Port)

* **Domain Reaction to Faults (CRITICAL MUST):** `CacheFault` MUST NOT contain diagnostic payloads.
    1. **`Fault(Transient)`:** Degrade to a **Cache Miss**.
    2. **`Fault(CircuitOpen)`:** Bypass L2 for the remainder of the Session.

* **Linearizability, Exact-Instance & Safe Publication (CRITICAL MUST):**
  `intern` MUST guarantee atomic publication (`putIfAbsent` or equivalent).
  `get` MUST return the exact canonical instance. Adapters MUST use JMM-compliant concurrent structures
  (`happens-before`). `PlanNode` and all backing collections MUST be final and structurally immutable.

### 4. Deterministic Commit Protocol & Persistent Pipeline (CRITICAL MUST)

1. **Pre-Commit Isolation:** `L2.intern` MUST ONLY be invoked during the bottom-up commit phase. A Hard Abort MAY occur
   during the commit phase; any partial L2 writes that occurred before the abort are semantically harmless.

2. **DAG Consistency Guarantee:** On `Fault(_)`, the Core MUST store `nodeToIntern` into the `CanonicalSubstitutionMap`.

3. **Persistent Canonicalization Rule:** Data retrieved from the Persistent Payload Store MUST be immediately routed
   through the `L2.intern` operation. The raw deserialized payload instance MUST be instantly discarded. If the L2 port
   transitions to `CircuitOpen`, the Persistent Payload Store MUST concurrently be bypassed.

4. **Builder Handle Terminalization Discipline (AMENDED):**
   If the L2 Port returns a builder-owned handle (or equivalent builder-right token) on a miss path, the caller MUST
   guarantee eventual terminalization of that handle by invoking exactly one of:
    * `commit(...)`, or
    * `abort(cause)`.

   If local building or pre-publication preparation throws before `commit(...)`, the caller MUST invoke
   `abort(cause)`.

   Implementations SHOULD document or provide a usage pattern equivalent to `try/finally` so that builder-owned pending
   slots do not remain orphaned indefinitely.

### 5. Hexagonal Port Boundaries (CRITICAL MUST)

The Domain Core MUST NEVER interact directly with `KClass`, `java.lang.reflect.*`, or `KS*` types.

* **Outbound (Driven) Ports:**
    * `TypeFactsProvider`: Supplies normalized `TypeFactsDTO` (including precomputed `nodeIdentity64`).
    * `NodeIdIndexer`: Maps abstract keys to dense integers.
      **CRITICAL MUST (Primitive Map):** `NodeIdIndexer` MUST be implemented using primitive storage
      (`LongArray`/`IntArray` + open addressing or equivalent). It MUST NOT use boxed-key generic maps
      (e.g., `Map<ULong, *>`, `Map<Long, *>` with boxing on Kotlin generics), and its hot-path APIs MUST be monomorphic
      and primitive (e.g., accept `identityBits: Long` and compare `CanonicalSignature` bytes in Phase 2).
    * `CanonicalSignatureProvider`
    * `CanonicalEdgeKeyProvider`: Guarantees the `edgeOrderingVersion` and outputs `edgeRank: UInt64`.
    * `PlanInternRepository` (L2 Port)
    * `PlanPayloadStore` (Persistent Load Port)
    * `CacheTelemetrySink`

---

## Amendment : Partition-First Governance, Capacity Governor, In-Flight Gate, and Sharding

This amendment refines *how* Tier-2 is implemented to survive enterprise-scale workloads (many tests, many tenants),
without violating cache-blind determinism.

### A. PartitionKey is a Physical Boundary (Bulk Drop Unit)

* **CRITICAL MUST:** `partitionKey` is not a cosmetic identifier. It is a **physical scoping and reclamation boundary**.
* **CRITICAL MUST:** The L2 adapter MUST implement Tier-2 storage as:
  `regions: ConcurrentHashMap<PartitionKey, PartitionRegion>`.
* **CRITICAL MUST:** The system MUST be able to reclaim memory by bulk invalidation:
  `regions.remove(partitionKey)` (or equivalent), without scanning global maps.

### B. Capacity Governor (Survival over Speed)

* **CRITICAL MUST:** Tier-2 MUST enforce capacity limits to prevent OOM:
  per-partition and/or global caps (`maxEntries`, optional `maxApproxBytes`).
* **CRITICAL MUST:** Upon cap breach, the adapter MUST transition the affected scope to `CircuitOpen` and emit
  telemetry.
* **CRITICAL MUST:** When `CircuitOpen`, the Domain MUST bypass L2 for the remainder of the session (treat as miss),
  preserving semantic determinism (only sharing/throughput is degraded).

### C. In-Flight Gate (Hot-Key Duplicate Build Storm Defense)

* **CRITICAL MUST:** The adapter MUST support an **in-flight gate** so that when N threads miss the same hot key, only
  one thread performs the expensive build while others join (bounded).
* **CRITICAL MUST:** In-flight joining MUST NOT introduce global locks; contention must be per-key (or per-bucket).
* **CRITICAL MUST:** The gate MUST be compatible with Fuel governance:
  join operations MUST consume `MaxFinalizeSteps` and MUST have a bounded wait/degrade policy.

### D. Sharding (Mechanical Contention Reduction)

* **CRITICAL MUST:** `PartitionRegion` MUST be subdivided into shards (striping) to reduce CHM contention:
  `PartitionRegion(shards: Array<Shard>)`.
* **CRITICAL MUST:** Routing MUST be deterministic and based on stable hashes derived from the `PlanCacheKey` tuple.

### E. Determinism Requirements under Governance

* **CRITICAL MUST:** Enabling/disabling in-flight gate, different shard counts, and partition bulk-drop MUST NOT alter:
    1) graph topology, 2) `treeSemanticCostUpperBound`, 3) semantic hard-abort outcomes.
* **CRITICAL MUST:** Only cache-sensitive operational counters (`MaxFinalizeSteps`) may differ between hot vs cold
  caches.

### F. In-Flight Attach Terminalization Guarantee (AMENDED)

* **CRITICAL MUST:** A waiter attachment is considered successful only if the implementation guarantees that attachment
  will later receive exactly one terminal outcome:
    * normal resume,
    * exceptional completion caused by shared-slot terminalization,
    * waiter timeout,
    * or waiter cancellation.
* **CRITICAL MUST:** No successfully attached waiter may remain in a state where no terminal signal is any longer
  reachable.

### G. Post-Insertion Attach Re-Verification Rule (AMENDED)

* **CRITICAL MUST:** After waiter-list insertion, the implementation MUST re-verify shared-slot state.
* **CRITICAL MUST:** If the shared slot has already transitioned out of `PENDING` at that point, the implementation MUST
  either:
    * immediately deliver the terminal signal to that attachment, or
    * remove the attachment and reject the attach.
* **CRITICAL MUST:** An implementation MUST NOT leave a post-insertion attachment in a state where terminalization is no
  longer reachable.

### H. Completion Continuation Execution-Path Safety (AMENDED)

* **CRITICAL MUST:** Completion continuation execution on the builder publication path MUST NOT re-enter the L2 shard
  path.
* **CRITICAL MUST:** Implementations MAY dispatch continuations to a separate executor or equivalent completion queue to
  prevent lock inversion or publication-path contamination.
* **CRITICAL MUST:** This requirement does not change publication-before-completion ordering; it only constrains how
  waiter continuations may execute after terminalization becomes observable.

### I. Slot-Owned Speculative Lease Rule (AMENDED)

* **CRITICAL MUST:** If timeout/degrade handling promotes a waiter into a speculative builder under governance quota,
  the
  resulting speculative reservation MUST be modeled as a **slot-owned lease**, not as handle-owned or session-owned
  state.
* **CRITICAL MUST:** Lease issuance MUST originate from the shared in-flight slot.
* **CRITICAL MUST:** Shared-slot terminalization (`SUCCESS`, `FAILED`, or `DROPPED`) MUST force-release any still-live
  speculative leases owned by that slot.
* **CRITICAL MUST:** Lease-release correctness MUST therefore survive successful publish, builder failure, and
  partition-drop sweep.

### J. Attach Rejection vs Quota Exhaustion Distinction (AMENDED)

* **CRITICAL MUST:** Ordinary attach rejection and speculative-builder quota exhaustion are distinct events.
* **CRITICAL MUST:** Examples of ordinary attach rejection include:
    * region already closed,
    * shared slot already terminalized,
    * waiter cap reached.
* **CRITICAL MUST:** `QUOTA_EXHAUST` applies only to speculative-builder quota denial after timeout/degrade handling.
  It MUST NOT be used as a generic label for all attach rejection paths.

### K. Drop-Sweep Linearizability (AMENDED)

* **CRITICAL MUST:** A close-gate publication MUST occur before final partition reclamation.
* **CRITICAL MUST:** The implementation MUST ensure that any in-flight slot visible after close publication is
  terminalized before final region removal.
* **CRITICAL MUST:** This MAY be achieved by:
    * stable repeated sweep,
    * post-insert close check with immediate drop,
    * or an equivalent linearizable mechanism.
* **CRITICAL MUST:** This requirement also applies to any slot created after close publication but before final
  reclamation; such slots MUST be terminalized before region removal completes.

---

## Invariant Checklist for Property-Based Testing

* [ ] **Cache-Blind Semantic Determinism:** Executing identical requests with an empty vs. populated L2 cache MUST
  result in identical `MaxSemanticWorkUnits` Hard Abort outcomes, evaluated strictly on the final root node.
* [ ] **Cache-Sensitive Operational Reality:** `MaxFinalizeSteps` exhaustion MAY differ between hot vs. cold caches, but
  MUST NOT affect semantic Hard Abort outcomes governed solely by `root.treeSemanticCostUpperBound`.
* [ ] **Worker Pooling Purity:** An aborted session followed by a new request on the same worker MUST NOT leak
  `GreyMap`, `NodeIdIndexer`, or RMQ state.
* [ ] **Persistent Pipeline Integrity:** Deserialized payloads MUST NEVER bypass L2 interning, and the Persistent store
  MUST NOT be queried if the L2 Cache is `CircuitOpen`.
* [ ] **Partition Bulk Drop:** After `regions.remove(partitionKey)`, no subsequent request MUST observe stale canonical
  instances from that partition.
* [ ] **In-Flight Gate Correctness:** Joiners MUST never observe partially constructed nodes; completion MUST occur only
  after safe publication.

### Additional In-Flight / Governance Compliance Checks (AMENDED)

* [ ] **Attach Terminal Signal Completeness:** Every successfully attached waiter eventually receives exactly one
  terminal signal.
* [ ] **Attach Post-Insertion Reconciliation:** No attach that succeeds past insertion may remain orphaned after a
  shared slot has already terminalized.
* [ ] **Publication-Path Continuation Safety:** Waiter continuation dispatch MUST NOT re-enter the L2 shard path on the
  builder publication path.
* [ ] **Handle Terminalization Discipline:** Builder-owned handles returned on miss paths are always eventually
  terminalized by exactly one of `commit(...)` or `abort(...)`.
* [ ] **Slot-Owned Speculative Lease Release:** Any speculative-builder reservation acquired from a shared slot is
  released either by normal speculative-builder termination or by shared-slot terminalization, including `DROPPED`
  during partition close.
* [ ] **Attach Rejection / Quota Distinction:** Telemetry and governance handling can distinguish ordinary attach
  rejection from speculative quota exhaustion.
* [ ] **Drop-Sweep Linearizability:** Any slot visible after close publication is terminalized before final region
  reclamation, including slots that appear after close publication but before final removal.