# ADR-0038: Interface Contract Polymorphic Expansion and Non-Composite Type Expansion Completion

## Status

Migration Pending

## Date

2026-04-23

## Related

- ADR-0007: Adoption of "Real Object First" Dependency Injection Strategy
- ADR-0025: Interface-Driven Contract Verification and Test Interface Pattern
- ADR-0028: Polymorphic Test Subject Injection via Constructor Types
- ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- `../../design/deterministic-active-member-projection-and-ordering-protocol.md`
- `../../design/l1-planner-session-primitive-data-structures.md`

## Context

Kontrakt's central product direction is to make interfaces first-class executable contracts.

An interface is not merely a JVM type shape. An interface is the primary behavioral contract surface.

Annotations are currently the transitional syntax used to express contract constraints, but the architectural direction
is larger:

``````text
interface
-> contract SSOT
-> implementation discovery
-> deterministic implementation execution
-> generated / expanded contract verification
``````

ADR-0025 and ADR-0028 already establish the core contract-testing law:

- behavioral contracts belong to `interface` / `abstract class`;
- concrete classes must not own behavioral contract identity;
- one contract subject may execute against many implementations;
- implementation order must be deterministic.

ADR-0007 establishes a separate law for dependency injection:

- real object first;
- interface / abstract dependency fallback belongs to dependency materialization;
- dependency-cycle breaking may substitute a mock.

These are not the same problem.

Kontrakt must distinguish:

1. **Contract Subject**
    - the interface/abstract type under test as the behavioral contract SSOT.

2. **Dependency Site**
    - an interface/abstract dependency required to instantiate or execute some subject.

3. **Structural Member**
    - an interface/abstract type appearing in object-graph expansion.

ADR-0037 then ratifies the new type expansion ordering:

``````text
run ratification
-> discovery
-> metadata
-> planning
    -> shape
    -> cycle identity
    -> active-cycle detection
    -> materialization after cycle miss
-> linking
-> execute
-> reporting
``````

After ADR-0037, the pipeline still has open execution paths:

- `TypeKind.INTERFACE`
- `TypeKind.ATOMIC`
- `TypeKind.COLLECTION`
- `TypeKind.ARRAY`
- `TypeKind.MAP`

Leaving those paths as unsupported would turn core type expansion into a composite-only engine and would directly
conflict with Kontrakt's interface-first mission.

This ADR closes those paths.

## Problem

The type expansion pipeline cannot remain composite-only.

A composite-only implementation causes four architectural failures:

1. **Interface Failure**
    - `TypeKind.INTERFACE` becomes fail-closed.
    - This conflicts with interface-first contract verification.
    - Interfaces become unexecutable even though they are supposed to be Kontrakt's strongest contract surface.

2. **Non-Composite Failure**
    - Atomic/container/map types remain special cases.
    - The core either throws or later grows ad hoc leaf/container logic.
    - That would fragment signature, equality-key, semantic-cost, and payload laws.

3. **Frame Model Incompleteness**
    - `TypeExpansionDecision` contains more than `CompositeExpansion`, but `StructuralPlannerCore` cannot execute the
      full vocabulary.
    - A closed decision vocabulary without corresponding frame semantics is incomplete.

4. **Replayability Drift**
    - TEMPORAL / UUID / container cardinality / synthetic edge choices would drift toward hidden wall-clock or RNG usage
      unless deterministic seed law is closed here.

Therefore, this ADR ratifies full execution semantics for:

- interface / abstract contract expansion;
- atomic leaf expansion;
- collection expansion;
- array expansion;
- map expansion.

## Decision

Kontrakt will treat all `TypeExpansionDecision` variants as executable planning decisions.

No decision variant may remain permanently routed to an unsupported guard.

The full decision vocabulary is:

``````kotlin
sealed interface TypeExpansionDecision {
    val subject: TypeReference

    class AtomicExpansion() : TypeExpansionDecision
    class CompositeExpansion() : TypeExpansionDecision
    class CollectionExpansion() : TypeExpansionDecision
    class ArrayExpansion() : TypeExpansionDecision
    class MapExpansion() : TypeExpansionDecision
    class PolymorphicExpansion() : TypeExpansionDecision
}
``````

The Planning Core must support a deterministic execution path for each decision.

## Core Rule

Interfaces are not unsupported shapes.

Interfaces are executable contract surfaces and must lower to deterministic polymorphic implementation resolution.

Atomic, collection, array, and map types are not ad hoc exceptions. They must be represented by explicit expansion
plans, deterministic synthetic edges where applicable, and explicit assembly/signature/equality/semantic-cost laws.

## Run Ratification and Deterministic Seed Surface

Deterministic execution begins before discovery.

The run must first pass through a ratification boundary:

``````text
invocation
-> run ratification
-> discovery
-> metadata
-> planning
-> linking
-> execute
-> reporting
``````

At run ratification, the framework MUST freeze an immutable deterministic snapshot that is propagated downward through
all later stages.

Illustrative shape:

``````kotlin
class DeterministicSeedSurface private constructor(
    val rootSeed: String,
    val entropyVersion: String,
    val rootTime: String,
    val seedSnapshotVersion: String,
)
``````

This ADR does not define the persistence format of this surface. It does define that all semantic choices introduced by
this ADR MUST be replay-derivable from:

- `DeterministicSeedSurface`,
- canonical type identity,
- capability fingerprint,
- explicit version tuples,
- deterministic local selector tuples.

Forbidden semantic inputs include:

- wall-clock time,
- `UUID.randomUUID()`,
- mutable global RNG streams,
- object identity,
- backend iteration order,
- any hidden environment-coupled entropy source.

### Stage Discipline

The deterministic seed surface is fixed early, but it is not allowed to influence every stage.

Rules:

- discovery order MUST remain seed-independent;
- metadata normalization MUST remain seed-independent;
- cycle identity MUST remain seed-independent;
- active-member ordering MUST remain seed-independent;
- synthetic edge ordering MUST remain seed-independent.

Seed-governed choices are limited to explicit entropy points such as:

- atomic symbolic materialization,
- temporal materialization,
- UUID materialization,
- optional exploratory cardinality choice if such a mode is later enabled.

This preserves compiler-style determinism:

- structure is fixed by protocol;
- entropy is allowed only where the protocol explicitly says so.

## BLAKE3 Unification Law

Within the planning layer, Kontrakt standardizes on the BLAKE3 family for cryptographic derivation surfaces.

BLAKE3 is the default family for:

- canonical digest material,
- keyed derivation,
- entropy derivation,
- deterministic UUID payload derivation,
- future replay / manifest summary roots within planning scope.

This does not mean every adapter must expose BLAKE3 directly. It means the planning protocol standardizes on one
hash/PRF/KDF/XOF family so that:

- semantic law stays uniform;
- keyed derivation stays uniform;
- adapter-local optimization remains swappable.

Optional hot-path entropy backends may exist behind adapters or internal engines, but they must be semantically
equivalent to the ratified derivation law.

BLAKE3 unification belongs to canonical digest / keyed derivation / entropy derivation law.

It does not define canonical string ordering. Canonical string ordering remains a higher-level Unicode semantic law.

## HID: Hierarchical Identity Derivation

Kontrakt adopts **Hierarchical Identity Derivation (HID)** as the planning-layer entropy law.

Node-path-only derivation is deprecated for semantic materialization.

A semantic choice must be derived from:

- parent deterministic entropy or parent semantic identity,
- a local selector tuple,
- explicit version tuple,
- deterministic keyed derivation.

Illustrative shape:

``````kotlin
class LocalSelectorTuple private constructor(
    val label: String,
    val semanticMemberIdentity: String,
    val localRank: Int,
    val slotPhase: String,
    val typeSignature: String,
)
``````

Illustrative derivation:

``````text
derived_entropy =
    KEYED_DERIVATION(
        key   = parent_entropy_or_parent_identity,
        input = canonical_encode(local_selector_tuple, version_tuple)
    )
``````

### HID Rules

- mutable session-global entropy counters are forbidden;
- node-path-only derivation is forbidden as the final semantic law;
- source-order-only derivation is forbidden as the final semantic law;
- semantic member identity has priority over source-order rank;
- local selector tuples must be canonical and locale-independent.

Reason:

- nodePath is path-fragile;
- source order is refactor-fragile;
- semantic member identity preserves structural locality when the surrounding graph shifts but the local meaning remains
  the same.

## Contract Subject vs Dependency Site vs Structural Member

### Contract Subject

A contract subject interface / abstract class expands to all eligible concrete implementations.

Rules:

- mock fallback is forbidden;
- Kontrakt does not provide a fake-as-implementation feature for contract-subject execution;
- implementation candidates must be concrete;
- implementation candidates must be non-synthetic;
- implementation candidates must be inside discovery scope;
- candidates must be sorted by FQCN ascending;
- duplicate implementation FQCN fails.

Zero candidates are not a hard failure by default.

Instead, the framework must produce a deferred contract-vacancy result.

Illustrative policy:

``````kotlin
enum class ContractVacancyPolicy {
    WARN_AND_DEFER,
    FAIL_STRICT,
}
``````

Default behavior:

- emit a strong warning / diagnostic;
- mark the contract as `DEFERRED_NO_IMPLEMENTATION`;
- keep the contract visible in reports and history.

Strict CI or release-gating environments may elevate this to failure.

Reason:

Kontrakt's intended development flow allows teams to define rich interface contracts before all implementations exist.
The framework must support that workflow rather than block it.

### Dependency Site

An interface / abstract type appearing as a constructor dependency is resolved through host-project runtime binding
information first.

Kontrakt must not require a parallel framework-owned binding configuration merely to decide what the host project has
already decided.

### Structural Member

An interface / abstract type appearing as a structural member follows the same host-runtime-first binding law, but it is
not the same semantic role as a dependency constructor parameter.

`DEPENDENCY_SITE` and `STRUCTURAL_MEMBER` are distinct modes because they originate from different expansion contexts,
even if they share binding-precedence rules.

## Polymorphic Resolution Context Propagation

`PolymorphicResolutionMode` MUST NOT be inferred from `TypeReference` alone.

The mode must be propagated explicitly through request/frame/linking context.

Illustrative shape:

``````kotlin
class TypeExpansionContext private constructor(
    val mode: PolymorphicResolutionMode,
    val contractVacancyPolicy: ContractVacancyPolicy,
    val runtimeBindingSnapshot: RuntimeBindingSnapshot,
)
``````

Propagation rules:

- `CONTRACT_SUBJECT`
    - originates from contract discovery / linking before root planning.

- `DEPENDENCY_SITE`
    - originates from constructor dependency expansion context.

- `STRUCTURAL_MEMBER`
    - originates from projected/synthetic member expansion context.

The Planning Core, `TypeExpansionPipeline`, or equivalent frame boundary MUST carry this context explicitly. Using only
`TypeReference` is non-compliant because the same interface type may appear in more than one semantic role.

## Runtime Binding Snapshot

Introduce a driven port:

``````kotlin
interface RuntimeBindingSnapshotProvider {
    fun resolveBindingSnapshot(): RuntimeBindingSnapshot
}
``````

Illustrative snapshot:

``````kotlin
class RuntimeBindingSnapshot private constructor(
    val bindings: DeterministicTypeExpansionList<ResolvedBinding>,
    val sourceId: String,
    val sourceVersion: String,
    val bindingSnapshotEpoch: String,
)
``````

### Snapshot Freeze Rule

`RuntimeBindingSnapshotProvider` is a ratification-boundary port.

It MUST be invoked only at run ratification.

The resulting `RuntimeBindingSnapshot` MUST then be frozen and pinned for the lifetime of the logical run.

Planning, linking, and execution MUST consume the pinned snapshot. They MUST NOT re-query the provider mid-run.

## Polymorphic Implementation Resolution — AMENDED

Introduce a driven port:

``````kotlin
interface PolymorphicImplementationProvider {
    fun resolveImplementations(
        contractType: TypeReference,
        mode: PolymorphicResolutionMode,
        runtimeBindingSnapshot: RuntimeBindingSnapshot,
    ): PolymorphicImplementationCandidates
}
``````

Mode vocabulary:

``````kotlin
enum class PolymorphicResolutionMode {
    CONTRACT_SUBJECT,
    DEPENDENCY_SITE,
    STRUCTURAL_MEMBER,
}
``````

Candidate value object:

``````kotlin
class PolymorphicImplementationCandidate private constructor(
    val contractType: TypeReference,
    val implementation: ConcreteImplementationReference,
)
``````

Implementation identity is centralized in `ConcreteImplementationReference`.

Illustrative shape:

``````kotlin
class ConcreteImplementationReference private constructor(
    val type: TypeReference,
    val canonicalIdentifier: String,
    val materializationKind: ImplementationMaterializationKind,
)
``````

Candidate collection:

``````kotlin
class PolymorphicImplementationCandidates private constructor(
    val contractType: TypeReference,
    val candidates: ExpansionSequence<PolymorphicImplementationCandidate>,
)
``````

`PolymorphicImplementationCandidates` is not a `Set` data structure.

It is an ordered, duplicate-rejecting candidate sequence for one contract type.

Hexagonal rule:

* Reflection, KSP, bytecode, and static-source adapters may implement this port.
* Planning Core must not use reflection / KSP APIs directly.
* The result must be deterministic and adapter-independent under the same discovery scope and pinned runtime binding
  snapshot.
* The provider must issue `ConcreteImplementationReference` only after type-shape checks prove that the implementation
  is concrete/materializable.

Candidate ordering rules:

* ordering is ascending by `implementation.canonicalIdentifier` under Kontrakt canonical identifier order;
* locale-dependent collation is forbidden;
* adapter enumeration order is non-authoritative;
* duplicate `implementation.canonicalIdentifier` within one contract candidate collection fails closed.

The candidate itself must not duplicate FQCN-vs-TypeReference identity surfaces.

Reason:

Keeping `implementationFqcn` and `implementationType` as separate fields inside the candidate allows identity drift.
`ConcreteImplementationReference` is the single concreteness and implementation-identity boundary.

### Kontrakt Canonical String Order

Kontrakt canonical string order is defined at the Unicode semantic layer, not at the byte-encoding layer.

Rules:

- input strings MUST be valid canonical string values;
- input strings MUST already be NFC-normalized;
- ordering is lexicographic ascending over canonical Unicode scalar sequences;
- locale-dependent collation is forbidden.

This ADR intentionally does not define canonical ordering in terms of a platform library method.

### Kontrakt Canonical Identifier Order

Kontrakt canonical identifier order specializes Kontrakt canonical string order for identifier-shaped values such as
`implementationFqcn`.

Rules:

- identifiers MUST satisfy Kontrakt canonical string order preconditions;
- identifiers MUST satisfy the ratified identifier grammar for the relevant protocol surface;
- ordering follows Kontrakt canonical string order.

Canonical byte lowering is a separate concern from canonical ordering. UTF-8 belongs to canonical encoding / hashing /
serialization law, not to the top-level ordering law.

Contract-subject zero-candidate handling follows `ContractVacancyPolicy`. Dependency/member ambiguity follows
runtime-binding-first law.

### Sealed Hierarchy Fast Path

A provider MAY use sealed-hierarchy metadata as an optimization fast path.

However:

- this does not change semantic behavior;
- this does not introduce a new mode;
- the resulting candidate set must remain identical to the ordinary deterministic discovery law.

Sealed-hierarchy optimization is therefore an implementation choice, not a semantic exception.

### Interface / Abstract Type Expansion Shape — AMENDED

`TypeKind.INTERFACE` and equivalent abstract contract types lower to:

``````kotlin
TypeExpansionPreflightDecision.PolymorphicPreflight
``````

Then, after active-cycle detection reports cycle miss:

``````kotlin
TypeExpansionDecision.PolymorphicExpansion
``````

Illustrative shape:

``````kotlin
class PolymorphicExpansion private constructor(
    override val subject: TypeReference,
    val mode: PolymorphicResolutionMode,
    val candidates: PolymorphicImplementationCandidates,
) : TypeExpansionDecision
``````

`candidates` may be empty only through an explicit vacancy path for contract-subject expansion.

Accidental empty collections are non-compliant.

Vacancy handling belongs to `ContractVacancyPolicy`.

The candidate sequence must be deterministic before any downstream execution/specification explosion occurs.

### Contract Subject Execution

For `CONTRACT_SUBJECT`, polymorphic expansion MUST happen before root planning.

``````text
Contract<User>
-> resolve implementations [AdminUser, GuestUser, ...]
-> deterministic order by FQCN
-> create one concrete execution per implementation
``````

### Dependency / Structural Execution

For `DEPENDENCY_SITE` and `STRUCTURAL_MEMBER`, polymorphic expansion may occur inside graph planning after cycle miss.

The Core must lower the selected implementation candidate into concrete child plan frames according to the mode.

## Atomic Expansion

Atomic types are executable leaf decisions.

Atomic expansion must not be an ad hoc generator hidden inside the core.

Atomic expansion requires an explicit plan:

``````kotlin
class AtomicExpansionPlan private constructor(
    val subject: TypeReference,
    val cycleIdentity: TypeCycleIdentity,
    val atomicKind: AtomicKind,
    val valueStrategy: AtomicValueStrategy,
    val constraints: AtomicConstraintSet,
    val equalityMaterial: AtomicEqualityMaterial,
    val deterministicSeedSurface: DeterministicSeedSurface,
    val semanticCostUpperBound: Long,
)
``````

Atomic kind vocabulary:

``````kotlin
enum class AtomicKind {
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    CHAR,
    STRING,
    ENUM,
    BIG_INTEGER,
    BIG_DECIMAL,
    TEMPORAL,
    DURATION,
    UUID,
    OTHER_VALUE_OBJECT,
}
``````

Value strategy vocabulary:

``````kotlin
enum class AtomicValueStrategy {
    CONSTRAINT_GENERATED,
    DETERMINISTIC_SYMBOLIC,
    VALUE_OBJECT_CONSTRUCTOR,
    ENUM_FIRST_DECLARATION,
}
``````

Constraint set:

``````kotlin
class AtomicConstraintSet private constructor(
    val facets: DeterministicTypeExpansionList<AtomicConstraintFacet>,
)
``````

Constraint facet:

``````kotlin
class AtomicConstraintFacet private constructor(
    val kind: AtomicConstraintFacetKind,
    val canonicalPayload: String,
)
``````

Constraint facet kinds:

``````kotlin
enum class AtomicConstraintFacetKind {
    RANGE,
    LENGTH,
    PATTERN,
    ENUM_SUBSET,
    TEMPORAL_WINDOW,
    VALUE_OBJECT_RULE,
    CUSTOM_CANONICAL,
}
``````

### Constraint Source Law

`AtomicValueStrategy.CONSTRAINT_GENERATED` MUST derive only from normalized constraint facets carried in
`AtomicConstraintSet`.

Those facets originate from the metadata/contract boundary:

- interface contract annotations,
- normalized metadata constraints,
- canonical value-object rules already ratified by protocol.

Capability profile may gate whether a strategy is lawful. Capability profile MUST NOT invent new constraint data.

### Atomic Equality Material

`AtomicEqualityMaterial` is the canonical equality surface for atomic plans.

Illustrative shape:

``````kotlin
class AtomicEqualityMaterial private constructor(
    val equalityKind: String,
    val canonicalValue: String,
)
``````

Rules:

- equality material must be deterministic;
- equality material must not use object identity;
- equality material must not use wall-clock or random UUID;
-
    - canonical value rendering must follow Kontrakt canonical string order / canonical encoding law as applicable and
      must be locale-independent.

### Temporal / Duration / UUID Law

`TEMPORAL`, `DURATION`, and `UUID` are allowed atomic kinds.

However:

- wall-clock time is forbidden;
- random UUID generation is forbidden;
- mutable global RNG is forbidden.

Required law:

- TEMPORAL materialization must derive from `DeterministicSeedSurface.rootTime` plus HID-governed deterministic entropy;
- UUID materialization must derive from HID-governed deterministic entropy and MUST be encoded as a UUIDv8-compatible
  custom deterministic identifier;
- DURATION materialization must derive from deterministic seed/constraint law, not from ad hoc magic constants;
- time-zone / offset normalization must be explicit and canonical.

### Enum Law

Enum default selection must be deterministic.

Allowed:

- first canonical declaration in stable declaration law;
- explicit constraint-selected value.

Forbidden:

- runtime iteration order if unstable;
- locale-sensitive name ordering;
- process-random selection.

## Collection Expansion

Collection expansion is synthetic-edge expansion.

A collection is not a composite object with properties. It is a container with deterministic synthetic element edges.

Plan shape:

``````kotlin
class CollectionExpansionPlan private constructor(
    val subject: TypeReference,
    val elementType: TypeReference,
    val cardinalityPolicy: ContainerCardinalityPolicy,
    val syntheticEdges: OrderedSyntheticEdges,
)
``````

Cardinality policy:

``````kotlin
enum class ContainerCardinalityPolicy {
    EMPTY,
    SINGLE_ELEMENT,
    BOUNDED_EXPLORATION,
}
``````

Default structural policy:

``````text
SINGLE_ELEMENT
``````

If a future exploratory mode widens cardinality choice, that choice must be HID-governed and explicitly labeled.

## Array Expansion

Array expansion is synthetic component-edge expansion.

Plan shape:

``````kotlin
class ArrayExpansionPlan private constructor(
    val subject: TypeReference,
    val componentType: TypeReference,
    val cardinalityPolicy: ContainerCardinalityPolicy,
    val syntheticEdges: OrderedSyntheticEdges,
)
``````

Default policy:

``````text
SINGLE_ELEMENT
``````

## Map Expansion

Map expansion is entry-edge expansion, not independent key/value child expansion.

A map entry is a pair.

Plan shape:

``````kotlin
class MapExpansionPlan private constructor(
    val subject: TypeReference,
    val keyType: TypeReference,
    val valueType: TypeReference,
    val cardinalityPolicy: ContainerCardinalityPolicy,
    val canonicalEntries: DeterministicCanonicalMapEntries,
)
``````

Default map policy:

``````text
SINGLE_ELEMENT
``````

### Deterministic Canonical Entry Law

The map law is expressed in terms of **canonical entries**, not in terms of a particular container implementation.

Each canonical entry must carry:

- canonical key equality material,
- canonical key digest,
- canonical value descriptor,
- deterministic entry-local selector tuple.

A compliant implementation must ensure that map entries are normalized into a canonical deterministic representation.

This ADR does not require CHAMP specifically. It requires a result that is compatible with canonical hash-bucket or
trie-based representations.

### Minimum Compliant Duplicate Detection

A minimum compliant implementation may use:

1. canonical digest computation;
2. deterministic sort by digest;
3. adjacent exact comparison on equal digests.

### Stronger Compatible Implementations

Stronger implementations may use:

- deterministic canonical bucket structures,
- deterministic trie-like canonical maps,
- CHAMP-compatible canonical entry representations.

Any stronger implementation must preserve the same semantic result.

### Map Key Uniqueness Law

For `SINGLE_ELEMENT`, uniqueness is trivially satisfied.

For `BOUNDED_EXPLORATION`:

- each entry's key materialization must include entry-local HID selector input;
- map assembly MUST verify uniqueness of canonical key equality material;
- if duplicate keys remain after canonical equality comparison, map assembly MUST fail closed.

The framework must not silently overwrite one deterministic key with another.

## Synthetic Edge Model

Introduce a common synthetic edge identity model for collection / array / map.

Semantic synthetic edge identity is not a display string.

Illustrative shape:

``````kotlin
class SyntheticEdgeIdentity private constructor(
    val ownerType: TypeReference,
    val kind: SyntheticEdgeKind,
    val entryIndex: Int,
    val slotPhase: SyntheticSlotPhase,
)
``````

Kinds:

``````kotlin
enum class SyntheticEdgeKind {
    COLLECTION_ELEMENT,
    ARRAY_COMPONENT,
    MAP_ENTRY,
}
``````

Slot phase:

``````kotlin
enum class SyntheticSlotPhase {
    ELEMENT,
    COMPONENT,
    KEY,
    VALUE,
}
``````

Display labels such as:

- `element[0]`
- `component[0]`
- `entry[0].key`
- `entry[0].value`

are reporting labels only.

They are not the semantic edge key.

### Synthetic Edge Key Law

The semantic synthetic edge key must be derived from a structured tuple, not from a display string.

Synthetic edge key ordering must follow Kontrakt canonical string / identifier ordering law where textual components are
compared.

Byte lowering for hashing those keys belongs to canonical encoding law and may use UTF-8.

Canonical tuple:

``````text
ownerTypeSignature
-> syntheticEdgeKindRank
-> entryIndex
-> slotPhaseRank
``````

## Frame Model

StructuralPlannerCore must support the following frame families:

``````text
PlanNodeFrame
IterateCompositeMembersFrame
ExpandCompositeEdgeFrame
AllocateCompositeFrame

AtomicAllocateFrame

IterateSyntheticEdgesFrame
ExpandSyntheticEdgeFrame
AllocateCollectionFrame
AllocateArrayFrame
AllocateMapFrame

PolymorphicDispatchFrame
``````

## Signature and Equality Law

Each expansion kind must define canonical signature and equality material.

### Atomic

Atomic signature includes:

- subject type identity;
- atomic kind;
- value strategy;
- normalized constraint set where applicable;
- deterministic equality material;
- deterministic seed-surface identity / seed version where required by the strategy.

### Composite

Composite signature includes:

- subject type identity;
- selected constructor;
- ordered active members;
- child descriptors.

### Collection / Array

Container signature includes:

- container type identity;
- cardinality policy;
- ordered synthetic edge descriptors;
- child descriptors.

### Map

Map signature includes:

- map type identity;
- cardinality policy;
- canonical entry descriptors;
- key child descriptors;
- value child descriptors.

### Polymorphic

Polymorphic signature includes:

- contract type identity;
- polymorphic resolution mode;
- ordered implementation candidate identities;
- selected implementation identity if collapsed to one implementation.

## Discovery Scope Note

Discovery remains seed-independent.

This ADR requires deterministic discovery output but does not yet require partitioned Merkle-manifest topology. That
stronger discovery invalidation law is deferred to a follow-up discovery-focused ADR/design note.

A deterministic discovery manifest and summary root are allowed and encouraged, but they are not the central planning
law ratified by this document.

## Metering

ADR-0032 Type Expansion metering must be extended with the following cost centers or equivalent ratified entries:

- `POLYMORPHIC_IMPLEMENTATION_RESOLUTION`
    - physical-only;
    - emitted after implementation candidate resolution succeeds.

- `POLYMORPHIC_IMPLEMENTATION_ORDERING`
    - physical-only;
    - emitted after deterministic ordering / duplicate validation succeeds.

- `ATOMIC_EXPANSION_DECISION`
    - physical-only.

- `CONTAINER_EXPANSION_DECISION`
    - physical-only.

- `SYNTHETIC_EDGE_MATERIALIZATION`
    - semantic-also if synthetic edges create child traversal obligations.

- `SYNTHETIC_EDGE_ORDERING`
    - physical-only if ordering only validates already-materialized deterministic edges;
    - semantic-also if ordering itself closes child traversal obligations.

## Interaction with ADR-0037

This ADR does not weaken ADR-0037.

All expansion kinds still follow:

``````text
shape
-> cycle identity
-> active-cycle detection
-> materialization after cycle miss
``````

For cycle-hit paths:

- no raw facts for the current type;
- no active-member projection for the current type;
- no synthetic edge materialization for the current type;
- no polymorphic implementation expansion for the current type unless the implementation set was already resolved before
  this planning episode by discovery/linking.

## Adoption Rule

This ADR is proposed target-state architecture.

Its compliance rules describe the state that the codebase must satisfy after implementation lands.

While the ADR remains `Proposed`, transitional code may temporarily remain non-compliant, but such non-compliance must
be treated as migration debt rather than as a competing architecture.

## Canonical Ordering vs Canonical Encoding

### LocalSelectorTuple Shape — AMENDED

Illustrative shape:

``````kotlin
class LocalSelectorTuple private constructor(
    val label: LocalSelectorLabel,
    val semanticMemberIdentity: String,
    val localOrdinal: CanonicalLocalOrdinal,
    val slotPhase: LocalSelectorSlotPhase,
    val subjectType: TypeReference,
)
``````

`LocalSelectorTuple` is typed local selector material.

It is created at the Local IR assembly boundary when projected semantic material becomes a concrete expansion
obligation.

Examples:

``````text
Projected active member
-> Local expansion edge
-> LocalSelectorTuple
-> HID-governed materialization
``````

``````text
Polymorphic selected implementation
-> Local implementation edge
-> LocalSelectorTuple
-> HID-governed materialization
``````

Rules:

* `LocalSelectorTuple` must not expose delimiter-joined string rendering as canonical HID input.
* `LocalSelectorTuple` must not be stored inside Canonical IR as path/session-local material.
* HID encoding must be performed later by the canonical/HID encoder using tagged, length-prefixed, version-bound
  encoding.
* `subjectType` is a domain-issued `TypeReference`.
* The tuple must not revalidate raw type text.
* The tuple must not import reflection/KSP/backend handles.

Reason:

The tuple is local selector input for HID. It is not canonical byte encoding and not Canonical IR.

This ADR separates:

- **canonical ordering law**
    - Unicode / string / identifier comparison semantics;

from:

- **canonical encoding law**
    - byte lowering used for hashing, digest material, serialization, and golden vectors.

Canonical ordering must not be defined in terms of UTF-8 byte comparison. Canonical encoding may still use UTF-8.

## Non-Goals

This ADR does not define:

- the persistence format of replay artifacts;
- automatic replay artifact refresh workflow;
- partitioned Merkle-manifest discovery topology;
- exhaustive arbitrary-size collection generation;
- property-based random collection generation;
- runtime realization details outside planner IR assembly.

## Compliance Rules

A compliant implementation must satisfy:

1. `TypeKind.INTERFACE` is not permanently unsupported.
2. Contract subject interfaces expand to real concrete implementations.
3. Contract subject interfaces do not fallback to mock.
4. Kontrakt does not provide fake-as-implementation as a contract-subject feature.
5. Contract subject zero implementations default to deferred vacancy, not immediate hard failure.
6. Strict mode may elevate deferred vacancy to failure.
7. `PolymorphicResolutionMode` is carried by explicit expansion context, not inferred from `TypeReference`.
8. Dependency-site and structural-member ambiguity consult pinned host runtime binding snapshots before failing.
9. Runtime binding snapshot is resolved once at run ratification and remains immutable for the logical run.
10. Implementation candidates are deterministic and FQCN ordered under Kontrakt canonical identifier order.
11. Planning-layer digest / derivation family is standardized on BLAKE3.
12. HID is the entropy law for planning-layer semantic materialization.
13. Mutable session-global entropy counters are forbidden.
14. Atomic expansion is leaf allocation with explicit equality material.
15. `CONSTRAINT_GENERATED` derives only from normalized `AtomicConstraintSet`.
16. TEMPORAL / UUID / DURATION materialization are deterministic and replay-derivable.
17. UUID materialization uses UUIDv8-compatible deterministic encoding.
18. Collection expansion uses deterministic synthetic element edges.
19. Array expansion uses deterministic synthetic component edges.
20. Map expansion uses deterministic canonical entries.
21. Map key collisions in bounded exploration fail closed after canonical key equality verification.
22. Synthetic semantic edge keys are structured tuples, not display strings.
23. Non-composite expansion decisions have executable frame paths.
24. No decision variant is permanently routed to an unsupported guard.
25. Cycle-hit paths still obey ADR-0037 fact-lazy rules.
26. All semantic choices introduced by this ADR are replay-derivable from deterministic seed surface and explicit
    version tuples.
27. Seed-governed materialization is separated from seed-independent cycle identity.

## Required Tests

Add tests for:

- contract subject interface expands to all concrete implementations;
- implementation candidates are sorted by FQCN ascending under Kontrakt canonical identifier order;
- contract subject interface does not fallback to mock;
- contract subject zero implementations produce deferred vacancy by default;
- strict vacancy policy upgrades deferred vacancy to failure;
- dependency-site interface honors pinned host runtime binding snapshot;
- unresolved dependency-site ambiguity fails after pinned host runtime binding snapshot is exhausted;
- atomic expansion produces deterministic equality material;
- `CONSTRAINT_GENERATED` reads only normalized atomic constraint facets;
- temporal materialization is rootTime/seed-derived and wall-clock-free;
- UUID materialization is deterministic and non-random;
- cycle identity remains identical across different deterministic seed surfaces;
- HID output remains stable under parent-preserving local refactor;
- collection expansion creates deterministic synthetic element identity for index 0;
- array expansion creates deterministic synthetic component identity for index 0;
- map expansion creates deterministic key/value pairing for entry 0;
- minimum compliant map duplicate detection catches equal canonical keys;
- semantic synthetic edge key is independent from display label;
- mode propagation from contract/dependency/member context is explicit and correct;
- no non-composite decision is routed to permanent unsupported guard.

## Final Rule

Interfaces are not unsupported shapes.

Interfaces are executable contract surfaces.

Atomic, collection, array, and map types are not ad hoc exceptions.

They are first-class expansion decisions with explicit plans, deterministic signatures, deterministic equality material,
deterministic replay law, and executable frame paths.