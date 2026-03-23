# 30. Edge-Aware Deterministic Cycle Truncation Strategy

Date: 2026-02-19

Status: Accepted

Supersedes: [ADR-0010](0010-strict-circular-reference-detection-strategy.md), [ADR-0027](0027-deterministic-cycle-truncation-policy.md)

Normative References: [ADR-0029](0029-runtime-link-handle-protocol-and-integrity.md)

Related Consistency References (
AMENDED): [ADR-0032](0032-capacity-law-resource-policy-resolution-identity-hierarchy-and-zero-residue-semantics.md)

## Context

Our framework's approach to handling recursive object graphs has evolved to address the conflict between **safety** and
**usability**.

We identified that relying on JVM recursion for graph traversal is **inherently unstable** and **expensive**:

1. **Stack Overflow Risk:** Deep graphs cause crashes dependent on JVM `-Xss` settings.
2. **Indeterminate State:** Managing rollback of state across recursive stack frames is error-prone.
3. **Lack of Control:** "Pausing" or "limiting" execution steps is difficult with native recursion.

To achieve commercial-grade reliability, we require a strategy that **bounds structural growth** (Space & Time) and
**deterministically handles cycles** using a controlled execution model. This execution model must adhere strictly to
Hexagonal Architecture principles, keeping the Domain (Planner) pure from infrastructure details.

## Decision

We adopt the **Edge-Aware Hybrid Truncation Strategy** powered by an **Iterative Explicit Stack Architecture** and
**Stateless Determinism**.

### 1. Execution Model & Domain Purity (MUST)

To guarantee safety, determinism, and domain purity, the Planner **MUST** adhere to the following rules:

* **Recursion Ban:** The traversal logic **MUST NOT** use native method recursion. It **MUST** be implemented using an
  **Iterative Depth-First Search (DFS)** loop.
* **Explicit Stack Frame:** The Planner MUST maintain its own heap-based stack.
    * **Frame Types:** `PLAN_NODE`, `ITERATE_MEMBERS`, `EXPAND_EDGE`, `ALLOCATE`.
* **Inbound Port Contracts & Value Objects (DDD):**
    * The Inbound Adapter (CLI, API, JUnit Extension) MUST parse raw external inputs and attempt to instantiate pure
      Domain Value Objects (e.g., `RootSeed.parse()`).
    * The Value Objects themselves MUST strictly enforce their own structural invariants (e.g., bounds checking,
      canonical string formatting). Instantiation failures MUST be caught by the Adapter and translated into
      human-readable UX.
* **Outbound Port Contracts (Hexagonal Purity):**
    * The Planner acts as the pure Domain Core. It MUST NOT perform raw reflection or bytecode parsing.
    * Outbound Ports (e.g., `TypeResolver`) MUST return pure schema components (`OwnerTypeFQCN`, `Name`,
      `TypeSignature`, `DeclarationIndex`) and raw structural facts (`isNullable`, `visibility`, `memberOrigin`,
      `typeSignatureNormalizationVersion`).
    * Ports MUST NOT pre-assemble serialization strings (Keys). String assembly is a pure Domain function.
* **Reality Defense (Domain Invariant Assertions):**
    * The Planner Core MUST defend its invariants against faulty inputs. Before assembling any Canonical Keys, the Core
      MUST execute a strict 1:1 mapping of validation checks:
        1. **Injectivity Check (`|` Delimiter):** No component may contain the delimiter `|`. If found, the Core MUST
           throw `PlanningException.InvalidCanonicalKeyComponent(faultKind = USER_MODEL_INVALID)`. *(Reason: `|`
           legitimately stems from user schema definitions via backticks or user-configured build pipelines like
           obfuscators).*
        2. **Normalization Check (NFC):** Each string component MUST strictly equal its NFC-normalized form. If
           violated, the Core MUST throw
           `PlanningException.PortContractViolation(faultKind = FRAMEWORK_INVARIANT_BROKEN)`. *(Reason: Normalization is
           strictly an Adapter contract).*
* **Transactional Backtracking & CPU-DoS Defense:**
    * Every stack frame **MUST** carry a **State Snapshot** (`CurrentSoftBudget`, `PlaceholderId Counter`,
      `Builder Log Position`, `Cache Log Position`).
    * **Rollback Rule:** When a branch is discarded, the Planner **MUST** restore the snapshot.
    * **Monotonic Step Counter (MUST):** To prevent CPU-DoS attacks, the `CumulativeStepCount` is **strictly excluded**
      from the snapshot. It MUST strictly and monotonically increase across all branch explorations and rollbacks.

#### 1.1. Rollback-Scoped Checkpoints vs. Monotonic Counters (AMENDED)

To remove ambiguity between rollback-local planner state and cumulative runtime metering, the following clarification is
normative:

* The frame snapshot fields (`CurrentSoftBudget`, `PlaceholderId Counter`, `Builder Log Position`, `Cache Log Position`)
  are **rollback-scoped checkpoints**, not cumulative metering state.
* `CurrentSoftBudget` is a planner-local checkpoint value and **MUST NOT** be interpreted as the global semantic budget
  counter.
* The planner runtime **MUST** maintain monotonic cumulative counters outside the snapshot boundary:
    * `CumulativePhysicalStepCount`
    * `CumulativeSemanticWorkCount`
* **Rollback Rule (AMENDED):** rollback may restore rollback-scoped checkpoints, stack-local planner state, and local
  builder/cache log positions, but it **MUST NOT** reduce already-consumed cumulative physical or semantic work.
* **Reason:** rollback is a control-flow recovery mechanism, not a physical time reversal mechanism. Already-consumed
  CPU
  work and semantic budget consumption remain spent.

### 2. Structural Cycle Detection & Identity

* **Node Identity (MUST):** For cycle detection, a node is identified strictly by its **Stripped Identity Signature**.
    * **Nullability:** **RECURSIVELY STRIPPED**.
    * **Generics:** **REIFIED**.
    * *Rule:* If the Active Stack contains a frame with the same Identity, it is a **Cycle**.

#### 2.1. Identity Representation Boundary (AMENDED)

To remove ambiguity between semantic identity rules and hot-path runtime representation:

* The stripped identity used for cycle detection **MUST** be bound to the active normalization / type-signature version.
* The planner/session hot-path representation of that identity **MUST** use a stable primitive 64-bit representation
  (for example, `nodeIdentity64` as a raw bit pattern), not boxed generic map keys.
* Richer descriptors MAY still exist for diagnostics, but active-path membership and cycle detection **MUST** be driven
  by the stable primitive identity representation.
* **Reason:** cycle detection is a hot-path planner operation and must remain stable across boxing differences, adapter
  representation details, and non-semantic runtime variation.

#### 2.2. Immediate Active-Path Re-entry Rule (AMENDED)

To make the cycle trigger threshold explicit:

* Re-observing the same stripped identity on the **current Active Stack** MUST be treated as an **immediate cycle**.
* The Planner MUST NOT allow:
    * bounded "grace" re-entry counts,
    * "N-times allowed" recursion policies,
    * cache/governance-dependent delayed cycle recognition,
    * retry-based postponement of cycle classification.
* Once active-path re-entry is observed, the Planner MUST immediately proceed to the deterministic breakpoint strategy
  defined by this ADR.
* **Reason:** delayed recognition would undermine bounded growth, complicate rollback semantics, and make semantic
  behavior sensitive to non-semantic execution conditions.

### 3. Edge Strength Classification & Ordering

#### 3.1. Capability Profile vs. Fingerprint (MUST)

* **`CapabilityProfile`:** The Core MUST receive the `CapabilityProfile` (a semantic domain object) to execute
  capability-based demotion logic.
* **`capabilityFingerprint` (Core-Derived):** The Core MUST compute
  `capabilityFingerprint = SHA-256(CanonicalForm(CapabilityProfile))`.
    * *Canonicalization Rules (MUST):* To prevent cache/entropy poisoning, the `CanonicalForm` MUST:
        1. Explicitly prefix the semantic version: `capabilityProfileVersion + "|" + canonicalPayload`.
        2. Ensure all payload tokens are pure ASCII (or NFC normalized UTF-8) and **MUST NOT** contain the delimiter
           `|`.
        3. Guarantee tokens are sorted deterministically (e.g., lexicographically by enum name).

        * *Violation:* If the Profile VO provides malformed tokens, the Core MUST throw `PortContractViolation`.

#### 3.2. Canonical Edge Key (Ordering & Tie-Breaking)

The Domain Core MUST assemble this key from Port-provided components to ensure deterministic selection.

* **Serialization Format:** `OwnerTypeFQCN|MemberKind|Name|TypeSignature|DeclarationIndex`.
* **Ordering Priority:** Ascending String Order -> `CTOR_PARAM` < `PROPERTY` -> Name -> Full Normalized Signature ->
  DeclarationIndex (default `-1` if unavailable).

#### 3.3. Entropy Target Key (Index Shift Immunity)

Entropy derivation MUST use the **Entropy Target Key**, which deliberately omits `DeclarationIndex` to maintain
stability during schema evolution.

* **Format:** `OwnerTypeFQCN|MemberKind|Name|TypeSignature` (Components validated by Reality Defense).

#### 3.4. Uniqueness Scope: The Active Member Set (MUST)

Collision detection MUST NOT be performed globally across all possible constructors.

* **Definition:** The **Active Member Set** consists of the parameters of the *single deterministically selected
  constructor* plus all eligible properties.
* **Fail-Fast Anti-Collision:** Upon resolving the Active Member Set, the Planner Core MUST verify uniqueness.
    * Duplicate `CanonicalEdgeKey` -> `PlanningException.AmbiguousEdgeKey`.
    * Duplicate `EntropyTargetKey` -> `PlanningException.AmbiguousEntropyTargetKey`.
    * *(Note: `faultKind` attribution for these collisions is dynamic and strictly evidence-based. See Section 7.2).*

#### 3.5. Edge Classification & Demotion (Domain Core Responsibility)

*Constraint:* The Planner Core MUST evaluate raw facts against the `CapabilityProfile`.

* **Strong Edge:** Non-nullable constructor parameters without default values. (Interop: Unknown nullability = STRONG).
* **Weak Edge (Breakable):**
    * **Subtype A: Substitutable:** Nullable parameters, Default Values, and Nullable Framework-Writable Properties (
      `var`, non-delegated, accessible under `CapabilityProfile`).
    * **Subtype B: Deferred:** `lateinit var` or provably uninitialized non-null fields.
* **Ignored Edge:** Read-only `val`, delegated properties, computed properties.
    * **Capability Demotion (MUST RECORD):** If restricted by the `CapabilityProfile`, the Core demotes it to **Ignored
      **
      and MUST record a `DemotionRecord` for diagnostics.

#### 3.6. Cycle Breakpoint Strategy (Tri-Stage)

1. **Stage 1:** Min Canonical Key among **Deferred Edges** -> Insert `UnlinkedDeferredNode`.
2. **Stage 2:** Min Canonical Key among **Substitutable Edges** -> Insert `SubstitutionNode`.
3. **Stage 3:** Fail-Fast (`PlanningException.CycleDetected`).

#### 3.6.1. Cycle Segment Candidate Scope (AMENDED)

The tri-stage breakpoint selection MUST be evaluated over the **current active cycle segment**, not over arbitrary
previously materialized child outputs.

* The candidate set MUST be derived from the **active edges that participate in the currently detected cycle segment**.
* The Planner MUST evaluate those active edges using the comparator rules of this ADR.
* The Planner MUST NOT shortcut breakpoint selection by consulting:
    * cache materialization timing,
    * previously published child results,
    * Tier-2 reuse timing,
    * builder/join completion order.

*Reason:* the truncation choice is a semantic planner decision over the active traversal structure, not a byproduct of
materialization history.

#### 3.6.2. Governance / Cache Blindness of Truncation Choice (AMENDED)

The selected cycle breakpoint is a **semantic planner decision** and therefore MUST be independent of infrastructure or
governance conditions.

* The chosen breakpoint **MUST** depend only on:
    * the Active Stack,
    * domain-visible facts,
    * the deterministic comparator rules defined by this ADR.
* The chosen breakpoint **MUST NOT** depend on:
    * cache hit/miss state,
    * Tier-2 join wait outcomes,
    * speculative builder admission,
    * timeout behavior,
    * thread scheduling,
    * GC pauses,
    * hot/cold cache state,
    * circuit-open transitions,
    * partition close/drop timing,
    * worker resumption order.
* Therefore, any governance or infrastructure change may alter reuse, retention, waiting, or throughput behavior, but
  **MUST NOT** alter the protocol-comparator-driven truncation choice.

#### 3.7. Constructor Selection Strategy

* **Candidate Eligibility:** Visible under `CapabilityProfile` and **Non-Synthetic**.
* **Selection Tuple:** `#StrongSatisfiable` -> `#DefaultAvailable` -> `#NullableAvailable` -> `#TotalParams` ->
  `SignatureStability`.

### 4. Explicit Substitution & Mapping

* `SubstitutionNode` **MUST carry** `reason` and `structuralPath`. `Nullable Property` = `NULL` (Skip Assignment).

#### 4.1. Planner Boundary vs. Downstream Materialization (AMENDED)

To prevent confusion between planner-time semantic decisions and later runtime realization:

* The Planner's responsibility ends at selecting the deterministic breakpoint and inserting either:
    * `UnlinkedDeferredNode`, or
    * `SubstitutionNode`.
* The Planner MUST NOT treat concrete runtime realization forms (e.g. assigning `null`, returning an empty collection,
  skipping assignment, or constructing a throwing diagnostic stub) as part of the breakpoint-selection algorithm.
* Those concrete realization forms belong to downstream linking/materialization rules and MAY vary by protocol, but the
  planner-time breakpoint choice itself MUST remain unchanged.

*Reason:* breakpoint selection is semantic and protocol-comparator-driven; concrete substitute realization is a later
phase concern.

### 5. Budgeting & Capacity Control

* **Max Live Node Cap / Planning Steps:** Hard caps resulting in `CapacityExceededException` (FATAL).

#### 5.1. Capacity Law Terminology Alignment (AMENDED)

To align this ADR with the later capacity-law documents without changing the original meaning:

* The hard-cap concept in this ADR corresponds to the resolved runtime caps later formalized as
  **`ResolvedPlannerSessionCaps`**.
* Representative cap categories include, but are not limited to:
    * `maxNodeIdCap`
    * `maxDepthCap`
    * `indexerTableCap`
    * `undoLogCap`
    * `maxSignatureBytes`
* A cap violation is a **hard failure boundary**, not a soft degradation signal.
* The planner **MUST** fail closed on hard cap exhaustion.

#### 5.2. Zero-Residue Clarification for Capacity Failures (AMENDED)

Following a fatal capacity violation:

* the current planning run **MUST** be aborted,
* rollback-scoped state **MUST** be unwound as applicable,
* worker-local session state **MUST** be reset before reuse.

However:

* zero-residue **does not require** byte-for-byte zero-filling of every primitive array or slab.
* zero-residue **does require** that discarded state becomes **semantically unreachable** from any subsequent planning
  run.

This means stale bytes may remain in memory physically, but they MUST NOT remain reachable via active stack pointers,
node counts, indexer heads/epochs, builder log positions, or equivalent runtime access paths.

### 6. Polymorphism & Determinism Contracts

#### 6.1. Type-Derived Entropy Contract (MUST)

Global mutable RNG streams and Path-based entropy are **STRICTLY FORBIDDEN**.

* **Algorithm:** **SHA-256**.
* **Strict Encoding Rules (MUST):**
    * `RootSeed`: Domain Value Object guaranteeing a canonical decimal string `[0, 18446744073709551615]`.
    * `capabilityFingerprint`: Computed by the Core (Section 3.1).
    * `FullCanonicalTypeSignature` & `TargetKey`: MUST be strictly UTF-8 encoded and NFC normalized.
    * `label` & `"NODE"` & `|`: MUST be pure ASCII. `label` MUST be from a fixed enum (e.g., `POLY_SELECT`,
      `COLLECTION_SIZE`, `TIME_OFFSET`) and **MUST NOT** contain the delimiter `|`.
* **Entropy Expansion / Domain Separation:**
    * Base string: `<RootSeed_ASCII> + "|" + <FullCanonicalTypeSignature_UTF8> + "|" + <capabilityFingerprint_ASCII>`
    * `Entropy(label, TargetKey) = SHA-256(BaseString + "|" + label_ASCII + "|" + TargetKey_UTF8)`
* **Choice Mapping & Bitwise Safety:**
    1. Ensure `n > 0` and `n <= Int.MAX_VALUE`.
    2. Extract `u` (unsigned big-endian 64-bit integer) masking with `0xFFL`.
    3. Modulo unsigned (`java.lang.Long.remainderUnsigned(u, n)`). Safely cast to `int` (for arrays/collections) or
       retain as `long` (for Time Offsets).
* **Entropy Versioning:** Bump `entropyVersion` upon any formulaic/normalization change.

#### 6.2. Cache Discipline & Immutability

* **Cache Key Contract:** `[FullCanonicalTypeSignature, RootSeed, capabilityFingerprint]`.
* **Site-Independent Resolution:** `LinkerContext` bindings **MUST be context-free**.
* **Immutability:** Cached `PlanNode` instances MUST be **Deeply Immutable**.

#### 6.2.1. Cache-Blind Determinism Boundary (AMENDED)

The cache layer exists to improve reuse and throughput, not to alter semantic planner meaning.

Therefore, changes in cache state or cache governance **MUST NOT** alter:

* final IR topology,
* canonical signatures,
* protocol-comparator-driven truncation choice,
* semantic outputs explicitly defined as semantic by the protocol,
* `treeSemanticCostUpperBound` (or any later equivalent semantic cost contract).

Cache/governance changes may alter only non-semantic dimensions such as:

* instance sharing,
* retention,
* latency,
* throughput,
* memory survival behavior.

#### 6.3. Time & Clock Determinism

* **Temporal Variance:** Offset `[0, MaxOffsetMillis)` generated via TDE (label: `TIME_OFFSET`). Computed as unsigned
  64-bit `long`. `TargetTime = RootTime.plusMillis(OffsetMillis)`.

### 7. Exception & Fault Taxonomy (MUST)

#### 7.1. Fault Kinds Namespaces

* **`PlanningFaultKind` (Domain Core Errors):** `USER_MODEL_INVALID`, `CAPABILITY_RESTRICTED`,
  `FRAMEWORK_INVARIANT_BROKEN`, `RESOURCE_EXHAUSTED`.
* **`RuntimeFaultKind` (Execution Errors):** `USER_CODE_VIOLATION`, `PROVIDER_BUG`.

#### 7.2. Exception Hierarchy & Dynamic Fault Attribution

To ensure fault attribution is strictly evidence-based and never misallocates responsibility between User, Framework,
and Third-party plugins (e.g., obfuscators), the Planner Core MUST use the following **Dynamic Fault Attribution Rule**
exclusively for schema collisions (`AmbiguousEdgeKey`, `AmbiguousEntropyTargetKey`):

* **Dynamic Fault Rule (Collisions Only):** The Planner MUST inspect the `memberOrigin` (`DECLARED`, `SYNTHETIC`,
  `ADAPTER_INFERRED`) and `typeSignatureNormalizationVersion` of the conflicting members.
    * IF AND ONLY IF all offending members strictly have `memberOrigin == DECLARED` AND the adapter's version exactly
      matches the Core's expected version, set `faultKind = USER_MODEL_INVALID`.
    * IF ANY member has `SYNTHETIC` or `ADAPTER_INFERRED`, OR the version mismatches, OR origin/version data is
      missing/unknown (null/empty), default to `faultKind = FRAMEWORK_INVARIANT_BROKEN`.

* **`PlanningException`** (Sealed Domain Exception):
    *
  `CycleDetected(path, cycleSegment, capabilityDemotions: List<DemotionRecord>, truncated: Boolean, faultKind: PlanningFaultKind)`
    * *Demotion Evidence:* `DemotionRecord` MUST contain `ownerType, memberKind, name, typeSignature, reason` and an
      optional `requiredCapabilityHint`.
    * *FaultKind Rule:* If `capabilityDemotions` is not empty, set to `CAPABILITY_RESTRICTED`. Otherwise,
      `USER_MODEL_INVALID`.
    * *Safety Rule:* `MAX_DIAGNOSTIC_ENTRIES` MUST be exactly `50`. Collections MUST be tail-truncated.
      `capabilityDemotions` MUST be sorted by **`EntropyTargetKey`** ascending before truncation to ensure
      index-independent deterministic reporting. If truncated, the `truncated` flag MUST be `true`.
        * `AmbiguousEdgeKey(ownerType, serializedKey, conflictingMembers, faultKind: PlanningFaultKind)` (Uses Dynamic
          Fault Rule).
        * `AmbiguousEntropyTargetKey(ownerType, targetKey, conflictingMembers, faultKind: PlanningFaultKind)` (Uses
          Dynamic
          Fault Rule).
        * `InvalidCanonicalKeyComponent(invalidComponent, reason, faultKind = USER_MODEL_INVALID)`
            * *Rule:* Thrown strictly when Reality Defense detects `|`. Always attributed to the user's model or their
              build pipeline (e.g., obfuscator/plugin).
        * `PortContractViolation(detail, faultKind = FRAMEWORK_INVARIANT_BROKEN)`
            * *Rule:* Thrown strictly when Reality Defense detects non-NFC normalized strings or malformed Capability
              Profiles, definitively indicating an Adapter bug.
* **`CapacityExceededException`** (Domain Exception): Payload: `limitType`, `currentValue`,
  `faultKind = RESOURCE_EXHAUSTED`.
* **`PlanViolationException`** (VM/Runtime Exception): Payload includes `runtimeFaultKind`.

*Adapter Translation Rule:* The Inbound Port adapters MUST translate these Domain Exceptions into human-readable
warnings based strictly on the `faultKind` and provided Domain descriptors.

* Examples: [cycle-truncation-examples](../design/cycle-truncation-examples.md)

## Consequences

### Positive

* Eliminates JVM stack-overflow risk from deep or cyclic graphs.
* Makes rollback behavior explicit, inspectable, and testable.
* Preserves deterministic cycle handling through comparator-driven breakpoint selection.
* Keeps Domain/Core responsibilities pure under Hexagonal Architecture.
* Aligns cycle truncation with later capacity-law and cache-blind determinism rules without changing the core decision
  logic of this ADR.

### Negative / Trade-offs

* The explicit frame machine is more verbose than native recursion.
* Rollback/checkpoint management requires disciplined implementation and compliance testing.
* Determinism now depends on stricter Port contract enforcement (normalization, origin/version reporting,
  canonical component hygiene).
* Additional governance/capacity documents must remain consistent with this ADR to avoid semantic drift.