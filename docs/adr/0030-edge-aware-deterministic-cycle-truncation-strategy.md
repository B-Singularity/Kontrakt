# 30. Edge-Aware Deterministic Cycle Truncation Strategy

Date: 2026-02-19
Status: Accepted
Supersedes: [ADR-0010](0010-strict-circular-reference-detection-strategy.md), [ADR-0027](0027-deterministic-cycle-truncation-policy.md)
Normative References: [ADR-0029](0029-runtime-link-handle-protocol-and-integrity.md)

## Context

Our framework's approach to handling recursive object graphs has evolved to address the conflict between **safety** and
**usability**.

We identified that relying on JVM recursion for graph traversal is **inherently unstable** and **expensive**:

1. **Stack Overflow Risk:** Deep graphs cause crashes dependent on JVM `-Xss` settings.
2. **Indeterminate State:** Managing rollback of state across recursive stack frames is error-prone.
3. **Lack of Control:** "Pausing" or "limiting" execution steps is difficult with native recursion.

To achieve commercial-grade reliability, we require a strategy that **bounds structural growth** (Space & Time) and *
*deterministically handles cycles** using a controlled execution model. This execution model must adhere strictly to
Hexagonal Architecture principles, keeping the Domain (Planner) pure from infrastructure details.

## Decision

We adopt the **Edge-Aware Hybrid Truncation Strategy** powered by an **Iterative Explicit Stack Architecture** and *
*Stateless Determinism**.

### 1. Execution Model & Domain Purity (MUST)

To guarantee safety, determinism, and domain purity, the Planner **MUST** adhere to the following rules:

* **Recursion Ban:** The traversal logic **MUST NOT** use native method recursion. It **MUST** be implemented using an *
  *Iterative Depth-First Search (DFS)** loop.
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

### 2. Structural Cycle Detection & Identity

* **Node Identity (MUST):** For cycle detection, a node is identified strictly by its **Stripped Identity Signature**.
    * **Nullability:** **RECURSIVELY STRIPPED**.
    * **Generics:** **REIFIED**.
    * *Rule:* If the Active Stack contains a frame with the same Identity, it is a **Cycle**.

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
      ** and MUST record a `DemotionRecord` for diagnostics.

#### 3.6. Cycle Breakpoint Strategy (Tri-Stage)

1. **Stage 1:** Min Canonical Key among **Deferred Edges** -> Insert `UnlinkedDeferredNode`.
2. **Stage 2:** Min Canonical Key among **Substitutable Edges** -> Insert `SubstitutionNode`.
3. **Stage 3:** Fail-Fast (`PlanningException.CycleDetected`).

#### 3.7. Constructor Selection Strategy

* **Candidate Eligibility:** Visible under `CapabilityProfile` and **Non-Synthetic**.
* **Selection Tuple:** `#StrongSatisfiable` -> `#DefaultAvailable` -> `#NullableAvailable` -> `#TotalParams` ->
  `SignatureStability`.

### 4. Explicit Substitution & Mapping

* `SubstitutionNode` **MUST carry** `reason` and `structuralPath`. `Nullable Property` = `NULL` (Skip Assignment).

### 5. Budgeting & Capacity Control

* **Max Live Node Cap / Planning Steps:** Hard caps resulting in `CapacityExceededException` (FATAL).

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
          Fault
          Rule).
        * `AmbiguousEntropyTargetKey(ownerType, targetKey, conflictingMembers, faultKind: PlanningFaultKind)` (Uses
          Dynamic
          Fault Rule).
        * `InvalidCanonicalKeyComponent(invalidComponent, reason, faultKind = USER_MODEL_INVALID)`
            * *Rule:* Thrown strictly when Reality Defense detects `|`. Always attributed to the user's model or their
              build
              pipeline (e.g., obfuscator/plugin).
        * `PortContractViolation(detail, faultKind = FRAMEWORK_INVARIANT_BROKEN)`
            * *Rule:* Thrown strictly when Reality Defense detects non-NFC normalized strings or malformed Capability
              Profiles, definitively indicating an Adapter bug.
* **`CapacityExceededException`** (Domain Exception): Payload: `limitType`, `currentValue`,
  `faultKind = RESOURCE_EXHAUSTED`.
* **`PlanViolationException`** (VM/Runtime Exception): Payload includes `runtimeFaultKind`.

*Adapter Translation Rule:* The Inbound Port adapters MUST translate these Domain Exceptions into human-readable
warnings based strictly on the `faultKind` and provided Domain descriptors.

* Examples: [cycle-truncation-examples](../design/cycle-truncation-examples.md)
