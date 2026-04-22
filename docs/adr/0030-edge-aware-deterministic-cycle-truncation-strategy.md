# 30. Edge-Aware Deterministic Cycle Truncation Strategy

Date: 2026-02-19

Status: Accepted

Supersedes: [ADR-0010](0010-strict-circular-reference-detection-strategy.md), [ADR-0027](0027-deterministic-cycle-truncation-policy.md)

Normative References: [ADR-0029](0029-runtime-link-handle-protocol-and-integrity.md)

Related Consistency References (
AMENDED): Related Consistency References (
AMENDED): [ADR-0032](0032-capacity-law-resource-policy-resolution-identity-hierarchy-and-zero-residue-semantics.md), [ADR-0037](0037-cycle-identity-preflight-and-deferred-raw-fact-resolution.md)


<!-- AMENDED(2026-04-16): Added deterministic planning preconditions for active-member selection timing, projection freeze, iteration stability, source-artifact reconciliation, explicit unknown/unavailable sentinel law, entropy exclusion, integer arithmetic discipline, mutable-global contamination prohibition, and deterministic diagnostic evidence ordering without changing prior semantic cycle-truncation policy. -->

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

### AMENDED — Cycle Detection Input Is Identity-Only

ADR-0037 refines the execution order for active-cycle detection.

Cycle detection MUST be performed from `TypeCycleIdentity`, not from raw type facts.

The following are forbidden as prerequisites for active-cycle detection:

- `RawTypeFactsDTO`,
- constructor enumeration,
- property enumeration,
- active-member projection,
- active-member ordering,
- declaration ordinal reconstruction,
- reflection enumeration order,
- KSP declaration order,
- capability-profile filtering.

Reason:

Cycle detection asks:

> Is this type identity already active on the current planning stack?

It does not ask:

> Which constructor/properties will be traversed under the current capability profile?

Therefore, the current node's raw facts, projected members, and ordered traversal view MUST be deferred until after
active-cycle detection reports a cycle miss.

This amendment is the ADR-0030-facing expression of ADR-0037.

ADR-0030 remains the authority for deterministic breakpoint selection.
ADR-0037 is the authority for the pre-cycle staging rule that makes breakpoint selection identity-first and fact-lazy.

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

#### 1.2. Mutable Global Contamination Prohibition Law (AMENDED)

Mutable global state MUST NOT participate in semantic planning.

Forbidden on the semantic planning path:

* mutable static scratch state,
* singleton mutable planning residue,
* previous-run frontier residue,
* mutable global clocks or RNG streams used as semantic inputs,
* mutable global counters used as semantic choice inputs,
* or equivalent hidden shared mutable state outside explicit session / frame ownership.

Allowed:

* immutable constants,
* immutable protocol tables,
* immutable version metadata,
* immutable snapshot registries.

Normative rule:

All mutable planning state MUST remain owned by explicit runtime/session/frame boundaries and MUST become semantically
unreachable after reset / rollback / terminal cleanup according to zero-residue law.

Reason: replayability and pooled-worker cleanliness are impossible under hidden mutable-global contamination.

### 2. Structural Cycle Detection & Identity

* **Cycle Identity (MUST):** For active-cycle detection, a planning node is identified by `TypeCycleIdentity`.
    * **Nullability:** usage-site nullability is **RECURSIVELY STRIPPED**.
    * **Generics:** generic arguments are **REIFIED** under the ratified canonical rendering law.
    * **Fast Path:** `identityBits64` may be used for primitive routing / index probing.
    * **Exact Check:** `canonicalSignature` remains the exact identity verification authority.
    * *Rule:* If the Active Stack contains a frame with the same cycle identity, it is a **Cycle**.

### AMENDED — Identity-First / Fact-Lazy Cycle Pipeline

For one node-expansion episode, the cycle-related part of the pipeline MUST follow this order:

1. resolve coarse type shape;
2. resolve `TypeCycleIdentity`;
3. verify cycle-identity subject continuity;
4. call `PlannerSession.enterOrDetectCycle(...)`;
5. if active cycle is detected:
    - run deterministic truncation / breakpoint selection;
    - do not resolve raw type facts for the current cycle-hit type;
    - do not project active members for the current cycle-hit type;
    - do not order active members for the current cycle-hit type;
6. if no active cycle is detected:
    - bind incoming edge metadata at the active depth;
    - then resolve raw facts;
    - then project/order active members;
    - then enter traversal.

Normative shape:

``````text
TypeReference
-> TypeShapeProvider.resolveTypeShape(reference)
-> TypeCycleIdentityProvider.resolveCycleIdentity(reference)
-> PlannerSession.enterOrDetectCycle(
       identityBits = cycleIdentity.identityBits64,
       signature = cycleIdentity.canonicalSignature
   )
-> if cycle hit:
       deterministic breakpoint selection
       no RawTypeFactsProvider call
   else:
       RawTypeFactsProvider.resolveRawFacts(reference)
       ActiveMemberProjector.project(...)
       ActiveMemberOrderer.order(...)
       IterateMembersFrame(OrderedActiveMembers)
``````

#### 2.0.1. TypeCycleIdentity as the Active-Cycle Detection Authority (AMENDED)

Active-cycle detection is driven by `TypeCycleIdentity`.

Illustrative shape:

``````kotlin
class TypeCycleIdentity private constructor(
    val subject: TypeReference,
    val identityBits64: Long,
    val canonicalSignature: CanonicalSignature,
    val identityAlgorithmId: String,
    val identityAlgorithmVersion: Long,
)
``````

`identityBits64` is a fast routing / indexing identity.

It is not authoritative by itself.

`canonicalSignature` is the exact identity verification material and MUST be checked after fast routing in the
`NodeIdIndexer` / active-cycle lookup path.

This preserves the two-phase identity law:

1. route / probe by primitive 64-bit identity;
2. verify by canonical signature byte equality.

`TypeCycleIdentity` replaces any ambiguous conceptual use of raw `nodeIdentity64` as the cycle-detection input.

Implementation may still store `identityBits64` in primitive `LongArray` form.
The semantic meaning, however, is cycle identity routing, not raw node payload identity.

#### 2.1. Identity Representation Boundary (AMENDED)

To remove ambiguity between semantic identity rules and hot-path runtime representation:

* The cycle identity used for active-cycle detection MUST be bound to the active normalization / type-signature version.
* The planner/session hot-path representation of that identity MAY use a stable primitive 64-bit representation
  (`identityBits64`) for routing, probing, and dense-node indexing.
* `identityBits64` MUST NOT be treated as the only equality authority.
* Exact identity equality MUST be verified by `canonicalSignature`.
* Richer descriptors MAY still exist for diagnostics, but active-path membership and cycle detection MUST be driven by
  the stable `TypeCycleIdentity` protocol.
* Ambiguous terminology such as raw `nodeIdentity64` SHOULD be retired from cycle-detection-facing APIs in favor of
  `cycleIdentityBits64`, `identityBits64`, or an equivalent name that makes the cycle-identity role explicit.

Reason:

Cycle detection is a hot-path planner operation and must remain stable across boxing differences, adapter representation
details, and non-semantic runtime variation while still protecting against primitive 64-bit collision.

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

#### 2.3. Cycle Identity Exclusion Rules (AMENDED)

The following MUST NOT participate in cycle identity:

* constructor candidates,
* constructor parameter lists,
* properties,
* declaration ordinal,
* active-member projection result,
* active-member ordering result,
* capability profile,
* adapter enumeration order,
* object identity,
* UUID,
* wall-clock time.

Cycle identity MUST include only adapter-independent canonical type identity material.

Required rules:

* usage-site nullability is stripped;
* generic arguments are represented deterministically;
* type aliases are either resolved to canonical targets or explicitly encoded by a ratified normalization rule;
* platform nullability uncertainty does not affect cycle identity;
* identity algorithm id/version are carried and checked.

Reason:

Cycle identity answers whether the type is already active on the stack.
It must not be contaminated by member traversal policy or adapter-specific fact surfaces.

#### 2.4. Nullability Strip and Generic Reification Law (AMENDED)

Cycle identity MUST strip usage-site nullability.

The following must map to the same cycle identity:

``````text
User
User?
``````

Reason:

Nullability changes absence semantics.
It does not create a different active-cycle type for structural recursion detection.

Generic arguments MUST be represented deterministically.

The following may map to different cycle identities:

``````text
Node<String>
Node<Int>
``````

Reason:

Generic arguments can change reachable child shape and structural recursion.

Forbidden:

* raw `KType.toString()` as identity authority;
* raw KSP symbol spelling as identity authority;
* adapter-native declaration order as identity authority;
* locale-sensitive rendering;
* object identity-based rendering.

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

* **Serialization Format:** `OwnerTypeFQCN|MemberKind|Name|TypeSignature|DeclarationOrdinal`.
* **Ordering Priority:** Ascending String Order -> `CTOR_PARAM` < `PROPERTY` -> Name -> Full Normalized Signature ->
  DeclarationOrdinal according to the explicit availability law below.

#### 3.2.1. Declaration Ordinal Availability Law (AMENDED)

`DeclarationOrdinal` is a semantic fact, not a magic integer.

It MUST be modeled conceptually as one of:

* `Present(n)`
* `Unavailable`

Normative rule:

* `Present(n)` means the adapter has deterministically reconstructed a declaration ordinal.
* `Unavailable` means the adapter could not deterministically reconstruct a declaration ordinal.
* `Unavailable` MUST NOT be treated as:
    * first declaration,
    * zero,
    * backend-native raw iteration order,
    * or any invented fallback ordinal.

For protocol semantics, `DeclarationOrdinal` remains an explicit fact with the two states above.

For primitive hot-path lowering only, a compliant implementation MAY encode:

* `Present(n)` -> `n`
* `Unavailable` -> `-1`

provided that this encoding remains purely mechanical and does not change the semantic meaning of the ordering law.

Reason:

The semantic protocol must expose **availability** explicitly.
A sentinel such as `-1` may be used for primitive lowering, but it must not replace the semantic fact model.

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

#### 3.4.1. Active Member Selection Point Law (AMENDED)

To guarantee deterministic planning order, Active Member Set construction MUST occur at one explicit point and only once
per logical node-expansion episode.

ADR-0037 refines the timing of that point.

Normative rule:

1. coarse type shape is resolved;
2. `TypeCycleIdentity` is resolved;
3. active-cycle detection is performed;
4. if the current type is an active-stack cycle hit:
    * raw facts MUST NOT be resolved for the current cycle-hit type;
    * constructor selection MUST NOT run for the current cycle-hit type;
    * active-member projection MUST NOT run for the current cycle-hit type;
    * active-member ordering MUST NOT run for the current cycle-hit type;
5. if active-cycle detection reports a cycle miss:
    * raw normalized structural facts are resolved from the outbound port;
    * the single deterministic constructor is selected;
    * eligible-property classification / demotion is evaluated;
    * the Active Member Set is projected;
    * uniqueness is verified;
    * canonical ordering is ratified;
    * and only then may traversal proceed.

Forbidden:

* deferring constructor selection into child-expansion control flow;
* recomputing the Active Member Set after partial child traversal;
* resolving raw facts before active-cycle detection merely to support cycle detection;
* allowing cache state, join/publication timing, or materialization history to affect member selection;
* allowing backend enumeration order to become the semantic traversal order.

Reason:

The active-member selection point is part of semantic determinism, but it is not a prerequisite for determining whether
the current type identity is already active on the stack.

#### 3.4.2. Projection Freeze Law (AMENDED)

Once the Active Member Set for one node-expansion episode is projected and canonically ordered, that view MUST be
treated as frozen traversal input.

Normative rule:

* traversal state MUST carry only the fully projected, uniqueness-verified, canonically ordered Active Member Set;
* raw structural member collections MUST NOT remain the semantic traversal input once projection is complete;
* rollback MAY restore traversal position, but MUST NOT mutate or recompute the already-frozen ordered member view for
  the same expansion episode.

Reason: deterministic traversal requires an immutable, already-ratified frontier.

#### 3.4.3. Comparator vs. Uniqueness Separation Law (AMENDED)

Deterministic ordering and semantic uniqueness are distinct protocol concerns and MUST NOT be conflated.

Normative rule:

* canonical ordering exists only to order already-distinct members inside the Active Member Set;
* ordering MUST NOT be used to conceal semantic ambiguity;
* if two active members collide on the canonical ordering identity domain, the planner MUST fail closed through the
  collision law rather than "pick one anyway" through extra tie-breaking.

Therefore:

* duplicate `CanonicalEdgeKey` remains a semantic ambiguity,
* duplicate `EntropyTargetKey` remains a semantic ambiguity,
* deterministic ordering does not override collision failure.

Reason: a deterministic order over an ambiguous model is still an ambiguous model.

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

#### 3.5.1. Unknown / Unavailable Sentinel Law (AMENDED)

Planning-relevant structural facts MUST represent unknown or unavailable metadata explicitly.

Forbidden:

* silently collapsing unknown into ordinary `false`,
* silently collapsing unavailable ordinals into `0`,
* silently treating missing backend facts as if they were confirmed semantic facts.

Required categories include, where applicable:

* nullability certainty,
* default-value presence,
* declaration-order availability,
* accessibility / writability availability,
* origin/version availability.

Normative rule:

* backend omission MUST be represented explicitly,
* deterministic conservative fallback rules MUST be documented,
* and those fallback rules MUST remain cache-blind, backend-blind, and concurrency-blind.

This amendment preserves the existing rule that unknown nullability MUST be treated conservatively rather than as an
ordinary nullable fact.

Reason: deterministic planning requires deterministic treatment of incomplete fact surfaces.

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

#### 3.6.3. Current Cycle-Hit Node Does Not Need Raw Facts (AMENDED)

A cycle-hit node MUST NOT require raw facts for the current type.

The current back-edge frame must carry enough incoming-edge metadata for ADR-0030 breakpoint comparison, including the
ratified fields required by the current edge-ranking protocol.

Examples of required incoming-edge metadata may include:

* incoming edge rank,
* incoming edge stage tag,
* incoming member index,
* incoming expansion execution index.

The current cycle-hit type's own constructors/properties are not required to detect the cycle or select the breakpoint.

Previously entered frames in the active segment may already own `OrderedActiveMembers`.
That is lawful because those frames passed cycle detection earlier.

The current cycle-hit frame MUST NOT force raw-fact resolution merely to participate in breakpoint selection.

#### 3.7. Constructor Selection Strategy

* **Candidate Eligibility:** Visible under `CapabilityProfile` and **Non-Synthetic**.
* **Selection Tuple:** `#StrongSatisfiable` -> `#DefaultAvailable` -> `#NullableAvailable` -> `#TotalParams` ->
  `SignatureStability`.

#### 3.7.1. Iteration Stability Law (AMENDED)

No semantic planning decision may depend on unstable adapter iteration order.

Normative rule:

* raw fact gathering may internally use any data structure,
  but unstable iteration order MUST NOT cross the domain boundary as semantic truth;
* insertion-order-preserving containers are insufficient if their insertion order is already unstable upstream;
* the Core MUST treat backend-provided raw member ordering as non-authoritative and rely only on the deterministic
  selection + projection + canonical ordering protocol of this ADR.

Reason: semantic determinism must not depend on incidental collection order.

#### 3.7.2. Source Artifact Drift Reconciliation Law (AMENDED)

Different structural fact sources (for example reflection, source analysis, bytecode analysis, or equivalent future
adapters) MUST reconcile to the same planning-relevant semantic fact surface.

Normative rule:

* if one source cannot natively provide a required planning fact,
  the adapter MUST either reconstruct it deterministically or emit an explicit unavailable sentinel handled by the Core
  under deterministic fallback law;
* source-specific capability differences MUST NOT silently rewrite constructor selection, property eligibility,
  canonical ordering, collision behavior, or diagnostic evidence order.

Reason: source backend diversity must not become semantic nondeterminism.

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

#### 4.2. Semantic Cost Ownership Boundary (AMENDED)

To prevent semantic-cost policy from leaking into planner-internal wrapper types:

* The planner's responsibility is:
    * detect active-path re-entry,
    * choose the deterministic breakpoint,
    * and emit semantic breakpoint intent.
* The planner MUST NOT encode the final `treeSemanticCostUpperBound` by relying on type-local wrapper defaults.
* The semantic cost bound for:
    * deferred break results,
    * substitution results,
    * and fully materialized results
      MUST be computed by the upstream semantic assembly / materialization boundary and then passed explicitly into the
      committed-result layer.
* Therefore, committed-result wrapper types MUST NOT be treated as the SSOT location for semantic-cost policy.
* This keeps:
    * breakpoint choice,
    * concrete realization form,
    * and semantic-cost ownership
      on distinct boundaries.

*Reason:* breakpoint selection is semantic and comparator-driven, while semantic-cost computation is a separate contract
that must remain explicit, cache-blind, and centrally owned.

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

#### 5.3. Integer Arithmetic Law (AMENDED)

Planning protocol arithmetic MUST remain integer-domain deterministic unless a separately ratified fixed-point law
states otherwise.

Normative rule:

The following categories MUST use deterministic integer arithmetic:

* budget accounting,
* semantic-work accounting,
* structural capacity accounting,
* deterministic counters,
* canonical ordering rank lowering,
* threshold / cap evaluation,
* and equivalent planner-control arithmetic.

Floating-point arithmetic MUST NOT become a hidden semantic choice surface for:

* active-member selection,
* ordering legality,
* collision classification,
* capacity-abort timing,
* or semantic output shape.

Reason: planner control arithmetic is protocol law, not approximation heuristics.

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

#### 6.3.1. Global Entropy Exclusion Law (AMENDED)

Planning semantics MUST exclude non-semantic global entropy.

Forbidden semantic inputs include:

* wall-clock time outside explicitly versioned deterministic entropy law,
* process-random UUIDs,
* mutable global RNG streams,
* object identity / memory-address effects,
* backend-specific incidental ordering,
* or any equivalent environment-coupled entropy source.

Normative rule:

* semantic identity,
* semantic ordering,
* active-member selection,
* canonical lowering,
* and semantic diagnostic ordering

MUST depend only on:

* domain-visible normalized structural facts,
* explicit deterministic version tuples,
* deterministic capability state,
* and explicitly injected deterministic seed surfaces already ratified by protocol.

This law does not require every identifier to be content-hash based.
It requires explicit separation among:

* routing identity,
* semantic identity,
* local dense ordinal,
* and runtime episode identifiers,

so that forbidden entropy does not leak into semantic planning meaning.

Reason: global entropy leakage destroys replayability and cache-blind semantic determinism.

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
* **Diagnostic Determinism Rule (AMENDED):**
  Any planner-produced diagnostic evidence set MUST be deterministically ordered before truncation, rendering, or
  adapter translation.

  This applies to:
    * capability demotion evidence,
    * collision evidence,
    * candidate lists,
    * active-member projection rejection evidence,
    * constructor-selection tie evidence where surfaced for diagnostics,
    * and equivalent future planner evidence collections.

  Forbidden:
    * preserving backend iteration order as diagnostic order,
    * preserving concurrency race order as diagnostic order,
    * unstable truncation caused by incidental collection order.

  Normative rule:
    * every planner-visible evidence family MUST define one deterministic ordering key,
    * truncation MUST occur after deterministic ordering,
    * and the `truncated` flag MUST reflect that deterministic post-order truncation result only.
* **`CapacityExceededException`** (Domain Exception): Payload: `limitType`, `currentValue`,
  `faultKind = RESOURCE_EXHAUSTED`.
* **`PlanViolationException`** (VM/Runtime Exception): Payload includes `runtimeFaultKind`.

*Adapter Translation Rule:* The Inbound Port adapters MUST translate these Domain Exceptions into human-readable
warnings based strictly on the `faultKind` and provided Domain descriptors.

* Examples: [cycle-truncation-examples](../design/cycle-truncation-examples.md)

## Additional Invariants from ADR-0037

1. Active-cycle detection uses `TypeCycleIdentity`.
2. Active-cycle detection does not use `RawTypeFactsDTO`.
3. Active-cycle detection does not use active-member projection.
4. Active-cycle detection does not use active-member ordering.
5. Cycle-hit path does not call `RawTypeFactsProvider` for the current cycle-hit type.
6. Cycle-hit path does not call `ActiveMemberProjector` for the current cycle-hit type.
7. Cycle-hit path does not call `ActiveMemberOrderer` for the current cycle-hit type.
8. `identityBits64` is a routing / indexing identity only.
9. `canonicalSignature` remains the exact identity verification authority.
10. Cycle identity strips usage-site nullability.
11. Cycle identity represents generic arguments deterministically.
12. Cycle identity excludes capability profile.
13. Cycle identity excludes declaration order.
14. Breakpoint selection remains governed by this ADR's edge-aware deterministic truncation rules.
15. ADR-0037 changes pre-cycle staging, not breakpoint ranking law.

## Consequences

### Positive

* Eliminates JVM stack-overflow risk from deep or cyclic graphs.
* Makes rollback behavior explicit, inspectable, and testable.
* Preserves deterministic cycle handling through comparator-driven breakpoint selection.
* Keeps Domain/Core responsibilities pure under Hexagonal Architecture.
* Aligns cycle truncation with later capacity-law and cache-blind determinism rules without changing the core decision
  logic of this ADR.
* Cycle-hit paths avoid raw-fact resolution for the current cycle-hit type.
* Cycle-hit paths avoid active-member projection/order for the current cycle-hit type.
* Cycle detection no longer depends on reflection/KSP member enumeration.
* Cycle detection no longer depends on declaration ordinal availability.
* Cycle detection no longer depends on capability-profile-specific active-member selection.
* Breakpoint selection remains edge-aware while the current cycle-hit type remains fact-lazy.

### Negative / Trade-offs

* The explicit frame machine is more verbose than native recursion.
* Rollback/checkpoint management requires disciplined implementation and compliance testing.
* Determinism now depends on stricter Port contract enforcement (normalization, origin/version reporting,
  canonical component hygiene).
* Additional governance/capacity documents must remain consistent with this ADR to avoid semantic drift.
* Introduces a separate `TypeCycleIdentityProvider` dependency before raw-fact resolution.
* Requires stricter subject-continuity checks between `TypeReference`, `ResolvedTypeShape`, and `TypeCycleIdentity`.
* Requires adapters to derive cycle identity without relying on constructor/property enumeration.
* Requires tests proving `RawTypeFactsProvider` is not called on cycle-hit paths.