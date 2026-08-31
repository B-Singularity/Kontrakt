# 31. Two-Tier Transactional Memoization and Structural Interning

Date: 2026-02-22

Status: Accepted (Amended: 2026-03-01)
<!-- AMENDED(2026-03-02): Added ULong boxing avoidance + NodeIdIndexer primitive-map mandate -->
<!-- AMENDED(2026-03-21): Clarified in-flight attach terminal-signal guarantees, commit/abort terminalization discipline, speculative-builder reservation release, drop-sweep linearizability, and callback execution-path safety without changing prior semantic policy. -->
<!-- AMENDED(2026-03-21): Clarified slot-owned speculative leases and attach-rejection vs quota-exhaustion distinction without introducing new domain fault surfaces. -->
<!-- AMENDED(2026-04-16): Clarified raw-fact DTO boundary, source-artifact reconciliation, explicit unknown/unavailable sentinel requirements, DTO-level normalization/version binding, and the separation between raw structural facts and Core-owned active-member semantic choice without changing prior cache-blind determinism or interning semantics. -->

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

#### 1.1. Raw Structural Fact Boundary Law (AMENDED)

The `TypeFactsDTO` family belongs to the outbound fact boundary and MUST represent **raw normalized structural facts**,
not planner-policy results.

Normative rule:

* `TypeFactsProvider` MUST supply normalized raw structural facts and precomputed routing/identity material;
* `TypeFactsDTO` MUST NOT smuggle planner-owned semantic choices such as:
    * the already-selected Active Member Set,
    * finalized capability-based demotion outcomes,
    * finalized cycle-break choices,
    * or equivalent Core-owned semantic policy results;
* the Core remains responsible for semantic choice over those facts under ADR-0030.

Reason: the DTO boundary is a fact boundary, not a semantic-planning authority surface.

#### 1.2. Source Artifact Reconciliation Law (AMENDED)

If different adapters (for example reflection, source analysis, bytecode analysis, or equivalent future backends)
describe the same semantic type under the same version tuple, they MUST reconcile to the same planning-relevant fact
meaning.

Normative rule:

* if a source backend cannot natively expose one required fact,
  the adapter MUST either reconstruct it deterministically or emit an explicit unavailable sentinel;
* backend capability differences MUST NOT silently rewrite:
    * node-identity meaning,
    * canonical ordering inputs,
    * collision behavior,
    * or planner-visible diagnostic evidence ordering.

Reason: adapter diversity must not become semantic nondeterminism.

#### 1.3. Unknown / Unavailable Sentinel Requirement (AMENDED)

Planning-relevant DTOs MUST represent unknown or unavailable metadata explicitly.

Examples include, where applicable:

* declaration ordinal unavailable,
* nullability unknown,
* default-value presence unknown,
* accessibility / writability unknown,
* origin or version metadata unavailable.

Forbidden:

* silently collapsing unknown into ordinary `false`,
* silently collapsing unavailable ordinals into `0`,
* or letting backend omission mutate semantic interpretation implicitly.

Reason: deterministic planning requires deterministic treatment of incomplete fact surfaces.

2. **`PlanCacheKey` (Memoization / Interning):**  
   `PlanCacheKey` consists of two explicitly separated identity surfaces carried together:

   **A. Authoritative semantic key material**
   `[workAccountingVersion, normalizationVersion, edgeOrderingVersion, capabilityProfileVersion, entropyVersion, partitionKey, equalityKey]`

   **B. Non-authoritative routing identity**
   `route64`

   Normative rule:

    * the authoritative semantic meaning of a plan-cache entry is the full semantic tuple only;
    * `route64` is a fast deterministic routing identity carried alongside that tuple;
    * `route64` MUST NOT replace exact semantic verification;
    * reuse / publication correctness MUST depend on authoritative full-key verification, not on `route64` uniqueness;
    * `partitionKey` lowering into canonical identifier material MUST remain deterministic and version-stable;
    * `equalityKey` MUST remain immutable authoritative semantic identity material.

   This separation is constitutional:

    * full semantic tuple = authoritative identity
    * `route64` = routing / shard / bucket / probing identity only

#### 1.2.1. `route64` Derivation Law (AMENDED)

`route64` MUST be deterministically derived from semantic key material and version-bound lowering inputs.

Normative rule:

* `route64` MUST be derived from:
    * `workAccountingVersion`,
    * `normalizationVersion`,
    * `edgeOrderingVersion`,
    * `capabilityProfileVersion`,
    * `entropyVersion`,
    * lowered `partitionKey`,
    * and authoritative `equalityKey` material;
* `route64` MUST NOT use process-random seeds, JVM identity hash, wall-clock entropy, or backend-incidental ordering;
* if reserved routing sentinels exist, `route64` MUST be deterministically remapped away from them;
* `route64` remains collision-tolerant and non-authoritative even when deterministically derived from the full semantic
  tuple.

Reason: routing identity must be reproducible and stable without becoming a substitute for semantic identity.

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

#### 1.3.1. Ordering Identity vs. Routing Identity Law (AMENDED)

`CanonicalEdgeKey` lowering and `route64` derivation are related only in that both must be deterministic and
version-bound.

They are not the same identity surface.

Normative rule:

* `CanonicalEdgeKey` governs deterministic choice/order inside planner semantics;
* `route64` governs fast routing for memoization/interning infrastructure;
* neither may silently substitute for the other;
* collisions on routing identity remain ordinary routing collisions;
* collisions or ambiguity in canonical ordering space remain planner-semantic concerns governed by ADR-0030.

Reason: routing identity, ordering identity, and full semantic identity must remain explicitly separated.

#### 1.4. Ordering-Version Boundary Law (AMENDED)

`edgeOrderingVersion` governs the deterministic lowering boundary for canonical ordering inputs.

Normative rule:

* lowering into `edgeRank` MUST depend only on:
    * normalized structural fact components,
    * the ratified canonical ordering tuple,
    * and the deterministic version-bound lowering law;
* lowering MUST NOT depend on:
    * raw adapter iteration order,
    * backend-specific incidental enumeration order,
    * process-local randomness,
    * or concurrency timing.

This ADR does not redefine the semantic ordering tuple itself.
That remains governed by ADR-0030.

This ADR defines the DTO/cache-side consequence:
the DTO and lowering boundary MUST provide all normalized inputs required to keep that ordering law version-bound and
backend-independent.

Reason: memoization and interning correctness rely on ordering-version stability, not merely on cache-key stability.

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
    * `TypeFactsProvider`: Supplies normalized raw `TypeFactsDTO` families (including precomputed `nodeIdentity64`)
      under the fact-boundary law of this ADR.
    * **CRITICAL MUST:** The provider supplies raw normalized structural facts, not planner-policy outputs and not
      memoization/interner key material.
    * **CRITICAL MUST:** The provider MUST NOT precompute or smuggle:
        * finalized Active Member Set choice,
        * finalized capability demotion result,
        * `PlanCacheKey`,
        * `route64`,
        * or equivalent Core/service-owned semantic or routing decisions.
    * **CRITICAL MUST:** If a backend cannot provide a planning-relevant fact directly, it MUST either:
        * reconstruct it deterministically, or
        * emit an explicit unavailable sentinel.
    * **CRITICAL MUST:** The provider MUST NOT let unstable backend enumeration order cross the boundary as semantic
      ordering truth.

    * `TypeFactsDTO` Family Richness Requirement (AMENDED):
        * **CRITICAL MUST:** The DTO surface MUST be rich enough to support the deterministic planning laws governed by
          ADR-0030 without consulting raw reflection/bytecode APIs from the Core.
        * **CRITICAL MUST:** DTO evolution MUST make it possible to represent, deterministically and explicitly, facts
          required for:
            * constructor-candidate identity,
            * property structural facts,
            * nullability certainty,
            * default-value presence where relevant,
            * mutability / storage classification where relevant,
            * declaration-ordinal availability,
            * origin provenance,
            * and version-bound signature provenance.
        * **CRITICAL MUST:** A DTO that hides required fact distinctions behind accidental defaults is non-compliant.
    * `NodeIdIndexer`: Maps abstract keys to dense integers.
      **CRITICAL MUST (Primitive Map):** `NodeIdIndexer` MUST be implemented using primitive storage
      (`LongArray`/`IntArray` + open addressing or equivalent). It MUST NOT use boxed-key generic maps
      (e.g., `Map<ULong, *>`, `Map<Long, *>` with boxing on Kotlin generics), and its hot-path APIs MUST be monomorphic
      and primitive (e.g., accept `identityBits: Long` and compare `CanonicalSignature` bytes in Phase 2).
    * `CanonicalSignatureProvider`
    * `CanonicalEdgeKeyProvider`: Guarantees the `edgeOrderingVersion` and outputs `edgeRank: UInt64`.
    * **Plan-cache key issuance boundary (AMENDED):**
        * **CRITICAL MUST:** Lowering from domain partition identity into `partitionKey` canonical material and
          derivation of
          `route64` belong to deterministic key-issuance/service logic, not to `TypeFactsProvider`.
        * **CRITICAL MUST:** The boundary that issues `PlanCacheKey` MUST preserve the explicit separation between:
            * authoritative semantic tuple,
            * and non-authoritative routing identity.
        * **CRITICAL MUST:** Exact-match reuse/publication MUST always verify the full semantic key, never `route64`
          alone.
    * `PlanInternRepository` (L2 Port)
    * `PlanPayloadStore` (Persistent Load Port)
    * `CacheTelemetrySink`

#### 5.1. Fact Boundary vs. Semantic Choice vs. Key-Issuance Boundary (AMENDED)

The following boundaries are distinct and MUST NOT collapse into one another:

1. **Fact Boundary**
    * normalized raw structural facts (`TypeFactsDTO`, `nodeIdentity64`)

2. **Semantic Choice Boundary**
    * deterministic active-member selection,
    * capability-based demotion,
    * projection,
    * uniqueness verification,
    * canonical ordering ratification

3. **Key-Issuance Boundary**
    * authoritative `PlanCacheKey` semantic tuple issuance,
    * deterministic `partitionKey` lowering,
    * deterministic `route64` derivation

Normative rule:

* outbound fact adapters transport facts only;
* the Core owns semantic choice over those facts;
* deterministic key issuance owns semantic-key material packaging plus routing-identity derivation;
* none of these boundaries may silently absorb the others.

Reason: fact transport, semantic choice, and routing/key issuance are distinct architectural authorities.

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

### Additional DTO / Identity-Boundary Compliance Checks (AMENDED)

* [ ] **Source Artifact Reconciliation:** Equivalent source backends produce the same planning-relevant fact meaning
  under the same normalization/version tuple.
* [ ] **Unknown / Unavailable Sentinel Stability:** Backend omission is surfaced through explicit sentinels rather than
  accidental defaults.
* [ ] **Ordering-Version Stability:** Equivalent normalized fact inputs produce identical canonical ordering-lowering
  inputs under the same `edgeOrderingVersion`.
* [ ] **Raw Fact Boundary Purity:** `TypeFactsProvider` does not leak planner-policy outputs or key-issuance outputs as
  if they were raw facts.
* [ ] **Backend Iteration Neutrality:** Unstable adapter iteration order does not cross the boundary as semantic
  ordering truth.
* [ ] **Semantic Tuple vs. Routing Identity Separation:** `route64` may vary only according to its ratified derivation
  law, but correctness always remains sealed by full semantic-key verification.
* [ ] **Reserved Routing Sentinel Stability:** `route64` derivation deterministically avoids reserved routing sentinels.