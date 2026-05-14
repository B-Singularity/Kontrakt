# ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

## Status

Proposed

## Date

2026-05-11

## Related

- ADR-0001: Adoption of Hexagonal Architecture
- ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
- ADR-0031: Cache-Blind Determinism and Plan Interning Governance
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0033: Bootstrap Runtime Policy Ratification, Storage Governance, and Deferred Platform-Aware Autotuning
- ADR-0034: Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority
- ADR-0035: Deterministic Balanced Lanes for Tier-2 Join Completion Delivery
- ADR-0036: Joined-Wait Planning-Run Suspension Bridge and Fresh-Session Restart Authority
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- ADR-0038: Interface Contract Polymorphic Expansion and Non-Composite Type Expansion Completion
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-owned Interning
- Future ADR / Amendment: Shared Runtime Policy Snapshot, Policy Epoch Publication, and Bounded-Context Policy Slices
- `docs/design/cycle-truncation-examples.md`
- `docs/design/deterministic-active-member-projection-and-ordering-protocol.md`
- `docs/design/l1-planner-session-primitive-data-structures.md`
- `docs/design/l2-join-lifecycle-state-machine-mechanics-for-planning-tier-2-cache.md`
- `docs/design/l2-plan-interner-partitioned-tier2-with-governance.md`
- `docs/design/planner-budget-resolution-and-worker-lifecycle.md`
- `docs/design/primitive-lane-owned-delivery-for-tier-2-joined-wait-completion.md`

## 1. Context

ADR-0039 establishes the target direction for metamodel erasure:

``````text
backend-native handle
-> acquisition/lowering
-> frozen adapter-neutral metamodel image
-> planning-facing providers
``````

The planning core must not consume reflection, KSP, bytecode, source-parser, compiler-IR, or backend-local registry
state.

ADR-0039 therefore classifies backend-native handles as acquisition inputs, not planning facts.

The intended planning-facing path is:

``````text
TypeReference
-> FrozenMetamodelImage
-> adapter-neutral facts
``````

not:

``````text
TypeReference
-> KType
-> facts
``````

and not:

``````text
TypeReference
-> KSType / KSDeclaration
-> facts
``````

However, ADR-0039 intentionally does not fully define the acquisition engine that creates a `FrozenMetamodelImage`.

The first obvious implementation direction was a reflection-specific collector.

A transitional collector can be described as:

``````text
root KType[]
-> issueRootReference
-> ArrayDeque pending queue
-> ArrayList discovered
-> linear contains
-> resolve shape / cycle / raw facts
-> FrozenMetamodelImageAssemblyInput
``````

That shape is too weak for Kontrakt's target architecture.

It solves the immediate reflection problem, but it does not close the more important design problem:

``````text
frozen acquisition traversal itself must be deterministic,
backend-model-neutral,
memory-disciplined,
restart-aware,
and future-compatible with KSP, KSP2, bytecode, compiler-static analysis,
and precomputed metadata indexes.
``````

The acquisition pipeline must therefore be promoted from a reflection adapter utility into a backend-model-neutral
frozen acquisition core.

This ADR defines that core.

## 2. External Architectural Pattern

This ADR follows recurring patterns found in high-quality compiler, build-system, and incremental-computation engines.

The exact vocabulary differs, but the common structural lessons are:

1. external or mutable handles are kept inside bounded contexts;
2. domain keys are stable and adapter-neutral;
3. operational dependency discovery is explicit;
4. missing dependency or pending work must not retain worker ownership indefinitely;
5. restart eligibility must be explicit and not hidden in arbitrary call stacks;
6. mutable builder state has a short lifetime;
7. after freeze/register/seal, consumers read immutable facts, values, or query outputs;
8. completion order must not become semantic order;
9. memory ownership is part of the contract;
10. cache retention is not semantic identity.

Reference patterns considered:

- Skyframe StateMachine-style explicit state-machine restart on missing readiness;
- DICE-style dependency tracking and deterministic join;
- rustc/Salsa-style query fingerprinting and early cutoff;
- Shake/Pluto-style dynamic dependency traces;
- LLVM-style pass-scoped arena / bulk lifetime;
- Cranelift/Zig-style entity or struct-of-arrays physical lowering;
- KSP/KSP2-style typed backend deferral;
- S3-FIFO/SIEVE-style retention governance for later L2 storage decisions.

This ADR imports principles, not implementations.

Kontrakt's interpretation is:

``````text
backend-native input
-> explicit acquisition state machine
-> deterministic TypeReference closure frontier
-> budgeted, byte-budgeted, and lowering-complete materialization
-> frozen adapter-neutral image
-> planning-facing providers
``````

## 3. Problem

The current frozen work is at risk of becoming a reflection-specific acquisition mechanism.

That would be a structural error.

The problem is not only that reflection has `KType`.

The deeper problem is that each future backend has a different native lifetime and discovery model:

``````text
Reflection:
    KType / KClass / KCallable / classloader state

KSP:
    KSType / KSDeclaration / Resolver / round lifecycle

KSP2:
    K2 compiler API session / symbol processing lifecycle

Bytecode:
    classfile nodes / descriptors / signature parser state

Compiler-static:
    compiler IR / module graph / symbol table snapshot

Precomputed index:
    persisted metadata index / generated metamodel manifest
``````

If traversal mechanics live inside each adapter, Kontrakt will acquire backend-specific determinism.

That is unacceptable.

### 3.1. Reflection-Specific Traversal Drift

A `ReflectionFrozenMetamodelCollector` would likely own:

- root traversal;
- duplicate detection;
- pending queue;
- discovered set;
- shape reference-requirement discovery;
- raw fact reference-requirement discovery;
- budget accounting;
- error handling;
- assembly input creation.

KSP, bytecode, and compiler-static backends would then either:

1. duplicate the same logic; or
2. bypass it with backend-specific traversal.

Both choices create semantic drift.

### 3.2. Naive Linear Membership

A simple `ArrayList.contains` membership check is deterministic but too weak.

It creates:

- `O(N * E)` style behavior on large type graphs;
- repeated heavy `TypeReference` structural comparison;
- no migration path toward stable HID / interning;
- no primitive membership authority;
- no clear collision verification law.

### 3.3. Count-As-Primary Budgeting

A collector that only counts nodes and edges can still be attacked or destabilized by oversized text material.

Examples:

- one type with enormous canonical signature text;
- huge annotation payload keys;
- large constructor signature material;
- diagnostic strings built from large object summaries;
- adapter bug producing repeated or exploded canonical text.

However, the inverse is also dangerous.

Frozen acquisition must not make record counts, table counts, or discovered-node counts the primary public policy
surface.

Low-level values such as:

- maximum TypeReference closure size;
- maximum constructor record count;
- maximum constructor-parameter record count;
- maximum property record count;
- maximum annotation record count;
- maximum shape table rows;
- maximum cycle-identity table rows;
- maximum raw-fact table rows;

are concrete capacity results.

They must be derived from an already-resolved byte/resource envelope by a deterministic capacity solver.

The planning L1 policy model already follows this discipline:

``````text
ResourceProfile
-> ResolvedSessionBudget
-> ResolvedSizingCalibration
-> capacity resolver
-> ResolvedPlannerSessionCaps
``````

Frozen acquisition follows the same shape:

``````text
ResourceProfile
-> ResolvedFrozenAcquisitionBudget
-> ResolvedFrozenSizingCalibration
-> FrozenAcquisitionCapacityResolver
-> ResolvedFrozenAcquisitionCaps
``````

Public/operator-facing policy chooses a high-level resource profile.

The acquisition core consumes only resolved concrete caps.

### 3.4. Hidden Coroutine Suspension and Hidden Continuation Ownership

Kontrakt's planning/L2 async model does not use Kotlin coroutine suspension as semantic control.

It uses explicit lifecycle states, callback-mediated readiness publication, and fresh execution-boundary restart
authority.

Frozen acquisition must follow the same quality bar.

If the acquisition backend exposes `suspend fun` as the semantic boundary, then:

- restart control is hidden behind coroutine machinery;
- budget step accounting becomes less explicit;
- restart boundaries become less visible;
- completion timing can leak into traversal order;
- KSP round deferral can be confused with pending backend work;
- explicit restart descriptor and deterministic replay become weaker.

### 3.5. Pending Work vs Backend Deferral

Not all unavailable material is the same.

Some material is pending:

``````text
bytecode index load in progress
compiler-static snapshot query in progress
metadata index loading
``````

Some material is deferred:

``````text
KSP generated source not visible until a later round
module graph not available in the current backend episode
dependency index not ready in this backend progress boundary
``````

A readiness callback may resolve pending work.

Waiting inside the current backend episode does not resolve backend deferral.

These must be distinct states.

### 3.6. Completion-Order Contamination

Future frozen acquisition may use asynchronous backend work or deterministic parallelism.

If completion order affects:

- TypeReference closure order;
- discovered order;
- frozen ordinal assignment;
- table layout;
- diagnostic classification;
- image digest;
- or planning-visible facts;

then frozen acquisition is not deterministic.

### 3.7. Freeze-Time Memory Pressure

Freeze is not the main planning hot path, but it is a heavy bridge.

During freeze, two worlds may overlap:

``````text
old world:
    backend handles
    mutable acquisition arena
    reflection/KSP/source/compiler object graphs
    staging buffers

new world:
    frozen type index
    frozen shape table
    frozen cycle identity table
    frozen raw fact table
    frozen record sequences
    planning-facing providers
``````

If this transition is not disciplined, Kontrakt risks:

- memory peaks;
- young-generation GC churn;
- backend handle lifetime extension;
- accidental object promotion;
- closure-backed frozen records;
- repeated temporary copies;
- pointer-heavy object graph retention.

### 3.8. Two Related Passes Can Look Like Duplicate Traversal

Frozen acquisition and planning both walk `TypeReference`-related structures.

This is intentional but performance-sensitive.

A wrong optimization would merge frozen acquisition traversal with planning expansion traversal.
That would let backend availability, acquisition order, or lowering details contaminate planning semantics.

The accepted optimization direction is different:

``````text
Do not merge frozen acquisition and planning traversal.
Make planning traversal cheap by making frozen output dense, ordinal-addressed,
table-backed, and backend-erased.
``````

Frozen acquisition must pay the expensive backend/lowering/normalization cost once.

Planning must then operate on:

- image-local frozen ordinals;
- frozen shape tables;
- frozen cycle identity tables;
- frozen raw fact tables;
- pre-sorted frozen record sequences;
- future HID / intern-id material where ratified.

If planning still performs backend lookups, structural `TypeReference` comparison, or string-heavy canonicalization,
then frozen acquisition has not fulfilled its optimization role.

## 4. Decision

Kontrakt will introduce a backend-model-neutral deterministic frozen acquisition pipeline.

The accepted shape is:

``````text
backend root / backend snapshot / backend index
-> FrozenMetamodelAcquisitionBackend<ROOT>
-> FrozenAcquisitionEngine
-> deterministic acquisition strategy
-> FrozenMetamodelImageAssemblyInput
-> FrozenMetamodelAdapterAssembler
-> FrozenMetamodelImage
-> FrozenMetamodelProviderBundle
-> planning-facing providers
``````

The pipeline is not a reflection collector.

Reflection is the first backend implementation.

KSP, KSP2, bytecode, compiler-static, and precomputed-index backends must be able to implement the same acquisition
backend contract.

## 5. Core Rule

Frozen acquisition is a deterministic state-machine process.

Backend-native handles are acquisition inputs.

They are not frozen facts.

They are not planning facts.

Kotlin coroutine suspension is not the semantic control model.

The core engine observes backend progress only through explicit acquisition step results.

The core engine owns:

- run lifecycle;
- TypeReference closure membership;
- traversal phase;
- budget consumption;
- byte budget consumption;
- restart admission;
- publication gate;
- deterministic table alignment.

Backends own:

- native handle reading;
- native symbol/type lowering;
- native index/session lifecycle;
- adapter-specific availability detection;
- backend-specific safety rejection.

Completion timing is operational material.

It is not semantic material.

## 6. Backend Model Neutrality Law

Frozen acquisition core must not import or depend on backend-native APIs.

Forbidden in frozen acquisition core:

- `KType`;
- `KClass`;
- `KFunction`;
- `KProperty`;
- `KSType`;
- `KSTypeReference`;
- `KSDeclaration`;
- `Resolver`;
- compiler IR node;
- bytecode parser handle;
- source AST / PSI handle;
- adapter-local registry cell;
- classloader identity;
- backend enumeration ordinal.

Allowed:

- backend-neutral `TypeReference`;
- adapter-neutral frozen record candidates;
- adapter-neutral shape material;
- adapter-neutral cycle identity material;
- adapter-neutral raw fact record material;
- backend-neutral acquisition result taxonomy;
- backend-neutral diagnostic reason codes.

Backend-native material may exist inside backend implementations only.

## 6A. Shared Runtime Policy Epoch Law

Frozen acquisition uses the same runtime policy epoch axis as planning, L2, dispatch, storage governance, and worker
lifecycle governance.

However, sharing the epoch does not collapse those bounded contexts into one runtime lifecycle.

The shared runtime policy epoch represents:

``````text
which immutable policy snapshot this run/acquisition/lane/host is governed by
``````

It does not represent:

``````text
planning run continuity
frozen acquisition run continuity
worker backing freshness
backend session generation
L2 host generation
dispatch lane lifecycle
``````

Therefore, ADR-0040 adopts the shared runtime policy epoch direction:

``````text
RuntimePolicyEpoch
    shared governance snapshot epoch

PlanningRunEpoch
    planning logical run continuity

FrozenAcquisitionRunEpoch
    frozen acquisition logical run continuity

WorkerBackingEpoch
    worker-local backing freshness

FrozenAcquisitionBackingEpoch
    frozen acquisition scratch/arena backing freshness

FrozenBackendSessionGeneration
    backend session / resolver / index stale-signal defense

L2HostGeneration
    L2 host / dispatch infrastructure lifecycle generation
``````

`RuntimePolicyEpoch` must be promoted out of planning infrastructure before frozen acquisition domain code depends on
it.

The current planning-local shape is transitional:

``````kotlin
package planning.infrastructure.runtime.policy

class RuntimePolicyEpoch private constructor(
    val id: Long,
    val policy: ResolvedRuntimePolicy,
)
``````

The target direction is:

``````kotlin
package runtime.domain.policy

class RuntimePolicyEpoch private constructor(
    val id: Long,
    val snapshot: ResolvedRuntimePolicySnapshot,
)
``````

or, if governance terminology is preferred:

``````kotlin
package governance.domain.policy

class RuntimePolicyEpoch private constructor(
    val id: Long,
    val snapshot: ResolvedRuntimePolicySnapshot,
)
``````

ADR-0040 does not require the final package name to be selected here.

ADR-0040 does require that frozen acquisition domain code must not import:

``````kotlin
planning.infrastructure.runtime.policy.RuntimePolicyEpoch
``````

unless `RuntimePolicyEpoch` has first been promoted to a shared runtime/governance policy package.

### 6A.1. Runtime Policy Snapshot Slicing

The policy epoch payload must be split by bounded-context policy slices.

Illustrative target shape:

``````kotlin
class ResolvedRuntimePolicySnapshot private constructor(
    val planning: ResolvedPlanningRuntimePolicy,
    val frozenAcquisition: ResolvedFrozenAcquisitionPolicy,
    val l2Join: ResolvedL2JoinPolicy,
    val l2Storage: ResolvedL2StoragePolicy,
    val dispatch: ResolvedDispatchLanePolicy,
    val runtimeWatchdog: ResolvedRuntimeWatchdogPolicy?,
)
``````

Each bounded context consumes only its own policy slice.

Planning consumes:

``````text
planning policy
L2 join policy when planning interacts with L2
``````

Frozen acquisition consumes:

``````text
frozen acquisition policy
``````

No bounded context consumes runtime watchdog policy as a semantic policy slice.

Runtime watchdog material may be observed only by outer runtime orchestration when explicitly allowed by a later ADR.

L2 join infrastructure consumes:

``````text
L2 join policy
dispatch policy
storage policy where relevant
``````

L2 storage governance consumes:

``````text
L2 storage policy
retention/admission/eviction policy
``````

No bounded context may mutate or reinterpret the shared runtime policy epoch after admission.

### 6A.2. Runtime Policy Epoch Continuity

Frozen acquisition fresh restart must preserve:

``````text
same FrozenAcquisitionRunEpoch
same pinned RuntimePolicyEpoch
same runtime policy snapshot payload
same resolved frozen acquisition policy slice
same remaining acquisition budget law
``````

Planning fresh restart must preserve:

``````text
same PlanningRunEpoch
same pinned RuntimePolicyEpoch
same resolved planning policy slice
same remaining planning budget law
``````

A newly published runtime policy epoch may affect future runs and future acquisitions.

It must not retune an already admitted planning run or frozen acquisition run.

### 6A.3. Runtime Watchdog Separation Law

`ResolvedRuntimeWatchdogPolicy` is not a bounded-context semantic policy slice.

It is optional outer runtime-orchestration watchdog material.

It may be used only by orchestration boundaries responsible for:

``````text
worker/lane ownership leak prevention
acquisition episode elapsed-time watchdog
backend readiness wait watchdog
hard operational abort
``````

It must not be consumed by:

- `FrozenAcquisitionCapacityResolver`;
- `TypeReferenceClosureAcquisitionStrategy`;
- `DeterministicTypeReferenceFrontier`;
- frozen ordering or ordinal assignment;
- frozen image identity, digest, or cache key construction;
- closure membership decisions;
- restart boundedness authority;
- planning semantic expansion;
- L2 key, route, retention, or dispatch semantics.

Elapsed wall-clock may abort an acquisition episode at the outer runtime boundary.

It must not decide:

- TypeReference closure membership;
- reference requirement registration;
- table layout;
- diagnostic classification;
- image digest;
- planning-visible facts;
- fresh restart priority;
- or fresh restart boundedness.

Fresh frozen-acquisition restart boundedness must be enforced by carried-forward deterministic acquisition budget, not
by elapsed wall-clock thresholds.

## 6B. Frozen / Planning Boundary and No Shared-Primitive Extraction Law

Frozen acquisition and planning both inspect `TypeReference`-related structures.

This similarity is mechanical, not semantic.

Frozen acquisition answers this question:

``````text
Which backend-erased TypeReference closure and table coverage are required
for a complete frozen metamodel image?
``````

Planning answers this question:

``````text
Given a validated frozen metamodel image, the current planning request,
the active expansion stack, and the active semantic policy, which expansion
branches should actually be opened?
``````

Therefore, ADR-0040 does not merge frozen acquisition traversal with planning traversal.

It also does not introduce a shared graph engine, shared frontier, shared budget ledger,
shared readiness bridge, or shared traversal primitive between frozen acquisition and planning.

Mechanically similar ideas may appear in both bounded contexts, but they remain independently owned
until multiple implementations prove that a shared abstraction is safe.

### TypeReference closure, not planning tree

Frozen acquisition may scan backend metadata references to compute the `TypeReference` closure required
for a complete frozen image.

This is not planning tree construction.

Frozen acquisition produces:

- root `TypeReference` set;
- reachable `TypeReference` closure;
- shape table coverage;
- cycle identity table coverage;
- raw fact table coverage;
- unavailable / rejected / failure sentinel material where required;
- deterministic reference-requirement evidence for diagnostics and validation.

Frozen acquisition must not produce:

- planning traversal tree;
- active-member ordered children;
- selected-constructor traversal children;
- eligible-property traversal children;
- cycle-truncated expansion branches;
- planning expansion frames;
- planning traversal topology;
- request-specific expansion graph.

References discovered from shape or raw-fact material are frozen image coverage requirements.

They are not planning traversal children.

### Two traversals are intentional

Kontrakt intentionally keeps the freeze barrier before planning.

The architecture accepts two conceptually related passes:

``````text
Frozen acquisition:
    expensive backend / lowering / normalization / coverage pass

Planning expansion:
    cheap ordinal / table / semantic-decision pass over the frozen image
``````

The optimization target is not to merge these passes.

The optimization target is to make the planning pass dense, ordinal-based, table-backed,
and free of backend-native handles, canonicalization work, and string-heavy lowering.

Frozen acquisition may provide factual reference-requirement indexes.

Those indexes are not planning traversal topology.

Planning remains responsible for:

- active-cycle hit decisions;
- cycle truncation decisions;
- raw-fact lazy read policy;
- constructor selection;
- property eligibility;
- active-member projection;
- active-member ordering;
- expansion topology;
- planning IR assembly;
- L1 planner-session budget mutation;
- L2 plan interning/cache join semantics.

### L2 mechanics non-import rule

Frozen acquisition may reuse the architectural quality bar of planning/L2 readiness handling:

- explicit lifecycle state;
- callback bridge;
- no retained worker ownership;
- no coroutine suspension as semantic control;
- stale-signal defense;
- single authority;
- pinned policy epoch continuity.

Frozen acquisition must not import L2-specific mechanics:

- shared slot state;
- waiter state;
- builder-handle state;
- join handle semantics;
- L2 route/shard ownership;
- L2 delivery-key semantics;
- L2 cache-publication semantics;
- L2 retention / admission / eviction semantics.

`FrozenAcquisitionReadinessBridge` is frozen-acquisition-local.

It is not `PlanningRunJoinBridge`.

It is not a shared runtime bridge.

It is not an L2 dispatch plane.

### Conservative extraction rule

No shared graph primitive may be extracted from frozen acquisition and planning as part of ADR-0040.

A future extraction requires a separate ADR or design note proving:

- identical mechanical law;
- no semantic leakage;
- no dependency from frozen acquisition to planning;
- no dependency from planning to frozen acquisition internals;
- no L2 bounded-context leakage;
- no loss of domain-specific optimization freedom.

## 7. FrozenMetamodelAcquisitionBackend

The backend boundary returns explicit step results.

It is not coroutine-suspending semantic control.

Illustrative shape:

``````kotlin
internal interface FrozenMetamodelAcquisitionBackend<ROOT> {
    val backendId: String
    val capability: FrozenMetamodelAcquisitionCapability
    val identityAlgorithmId: String
    val identityAlgorithmVersion: Long

    fun beginSession(
        context: FrozenAcquisitionSessionContext,
    ): FrozenAcquisitionStepResult<FrozenAcquisitionSession>

    fun issueRootReference(
        session: FrozenAcquisitionSession,
        root: ROOT,
    ): FrozenAcquisitionStepResult<TypeReference>

    fun resolveTypeShape(
        session: FrozenAcquisitionSession,
        reference: TypeReference,
    ): FrozenAcquisitionStepResult<ResolvedTypeShape>

    fun resolveCycleIdentity(
        session: FrozenAcquisitionSession,
        reference: TypeReference,
    ): FrozenAcquisitionStepResult<TypeCycleIdentity>

    fun resolveRawFacts(
        session: FrozenAcquisitionSession,
        reference: TypeReference,
    ): FrozenAcquisitionStepResult<RawTypeFactsResolution>

    fun finishSession(
        session: FrozenAcquisitionSession,
    ): FrozenAcquisitionSessionCompletion
}
``````

Backend session meaning differs by backend:

``````text
Reflection:
    registry/lifecycle scope

KSP:
    resolver-bound round/session scope

KSP2:
    K2 compiler API session/snapshot scope

Compiler-static:
    immutable program snapshot / module graph scope

Bytecode:
    classpath index / parser cache scope

Precomputed index:
    persisted index read transaction
``````

The acquisition core must not inspect backend-native session internals.

## 8. FrozenAcquisitionStepResult

Frozen acquisition backend operations must return a closed result algebra.

Illustrative shape:

``````kotlin
internal sealed class FrozenAcquisitionStepResult<out T> {
    class Available<T>(
        val value: T,
    ) : FrozenAcquisitionStepResult<T>()

    class PendingBackendWork(
        val handle: FrozenAcquisitionReadinessHandle,
        val restart: FrozenAcquisitionRestartDescriptor,
    ) : FrozenAcquisitionStepResult<Nothing>()

    class DeferredToBackendProgress(
        val reason: FrozenAcquisitionDeferral,
    ) : FrozenAcquisitionStepResult<Nothing>()

    class Unavailable(
        val reasonCode: String,
    ) : FrozenAcquisitionStepResult<Nothing>()

    class RejectedUnsafe(
        val reasonCode: String,
    ) : FrozenAcquisitionStepResult<Nothing>()

    class Failed(
        val reasonCode: String,
    ) : FrozenAcquisitionStepResult<Nothing>()
}
``````

Meanings:

- `Available`: material is available now and may be consumed.
- `PendingBackendWork`: backend work is in flight and may later publish readiness through a callback bridge. It carries
  an operational readiness handle and an explicit restart descriptor.
- `DeferredToBackendProgress`: the current backend episode cannot provide the material. A backend progress boundary must
  change before the material can be requested again.
- `Unavailable`: backend cannot provide this material as an expected capability/availability condition.
- `RejectedUnsafe`: material was rejected because accepting it would violate safety, erasure, canonicalization, lowering
  completeness, or determinism law.
- `Failed`: operational or integrity-class acquisition failure.

### 8.1. Pending vs Deferred Law

Pending and deferred must not be collapsed.

``````text
PendingBackendWork:
    a backend operation is in flight or may become ready through an asynchronous backend event.
    readiness is delivered by a one-shot callback bridge.
    the acquisition engine does not retain worker ownership while readiness is outstanding.
    the acquisition engine restarts through an explicit descriptor.

DeferredToBackendProgress:
    the current backend episode cannot make progress by waiting.
    a backend progress boundary must change.
``````

Examples:

``````text
Bytecode index load:
    PendingBackendWork may be valid.

Compiler-static snapshot query:
    PendingBackendWork may be valid.

KSP generated source not visible until next round:
    DeferredToBackendProgress.

Reflection unsupported local anonymous class:
    RejectedUnsafe or Failed.

Reflection backend incapable of source-only metadata:
    Unavailable.
``````

### 8.2. Readiness Callback Law

`PendingBackendWork` is not worker waiting, not retained worker ownership, and not coroutine suspension.

It represents an operation whose readiness may be reported later.

The backend must expose readiness through `FrozenAcquisitionReadinessHandle` and must not mutate
`FrozenAcquisitionRunContext` directly.

The bridge is responsible for one-shot translation:

``````text
backend readiness callback
-> FrozenAcquisitionReadinessBridge
-> READY_TO_RESTART publication
-> FrozenAcquisitionRestartAdmission
-> fresh acquisition restart
``````

Already-ready and asynchronously-ready cases must converge to the same fresh-restart path.

Backend readiness timing may affect latency only.

It must not affect:

- TypeReference closure order;
- discovered order;
- frozen ordinal assignment;
- table layout;
- diagnostic classification;
- image identity;
- planning-visible facts.

Terms that imply retained worker ownership, retained node execution, or coroutine suspension must not be used as
normative frozen acquisition semantics.

## 9. FrozenMetamodelAcquisitionCapability

Backend capability is acquisition, diagnostic, scheduling, and policy material.

It is not planning-visible semantic material.

Capability describes what a backend can provide and under which physical invocation constraints the acquisition strategy
may safely call it.

Capability must not become semantic authority.

It may govern:

- acquisition strategy selection;
- compatibility checks;
- diagnostic evidence rendering;
- backend call scheduling;
- backend progress handling;
- batch request planning;
- future cache-eligibility heuristics;
- policy validation.

It must not govern:

- planning semantic branching;
- type expansion decisions;
- TypeReference equality;
- frozen key equality;
- canonical IR shape;
- L2 key equality;
- PlanCacheKey shape;
- route64 derivation;
- HID derivation;
- protocol-owned interning identity.

Illustrative shape:

``````kotlin
internal class FrozenMetamodelAcquisitionCapability private constructor(
    val rootModel: FrozenAcquisitionRootModel,
    val progressModel: FrozenAcquisitionProgressModel,
    val graphCompleteness: FrozenAcquisitionGraphCompleteness,
    val payloadModel: FrozenAcquisitionPayloadModel,

    val diagnosticEvidenceModel: FrozenDiagnosticEvidenceModel,
    val concurrencyModel: FrozenBackendConcurrencyModel,
    val backendIdentityStability: FrozenBackendIdentityStability,
    val batchingModel: FrozenBackendBatchingModel,

    val supportsSourceDeclarations: Boolean,
    val supportsBytecodeMetadata: Boolean,
    val supportsRuntimeReflection: Boolean,
    val supportsDeferral: Boolean,
    val supportsWholeProgramSnapshot: Boolean,
    val supportsIncrementalSnapshot: Boolean,
    val supportsPendingBackendWork: Boolean,
)
``````

`supportsDirectOrdinalMaterialization` must not remain a separate boolean capability in the final ADR-0040 vocabulary.

Direct ordinal or direct slab behavior is represented by:

``````text
payloadModel = DIRECT_SLAB_WRITER
``````

or:

``````text
batchingModel = DIRECT_ORDINAL_MATERIALIZATION
``````

A backend that claims direct ordinal materialization still must satisfy the same frozen semantic contract as ordinary
acquisition.

It does not gain authority over TypeReference identity, frozen ordinal law, table layout law, or planning semantics.

Associated enums:

``````kotlin
internal enum class FrozenAcquisitionRootModel {
    EXPLICIT_ROOTS,
    ANNOTATED_SYMBOLS,
    MODULE_GRAPH,
    WHOLE_PROGRAM,
    PRECOMPUTED_INDEX,
}

internal enum class FrozenAcquisitionProgressModel {
    SINGLE_SESSION,
    MULTI_ROUND,
    INCREMENTAL_SNAPSHOT,
    WHOLE_PROGRAM_SNAPSHOT,
}

internal enum class FrozenAcquisitionGraphCompleteness {
    REACHABLE_FROM_ROOTS,
    MODULE_COMPLETE,
    WHOLE_PROGRAM_COMPLETE,
    INDEX_COMPLETE,
}

internal enum class FrozenAcquisitionPayloadModel {
    LAZY_PROVIDER_RESOLUTION,
    EAGER_SYMBOL_LOWERING,
    PRECOMPUTED_PAYLOADS,
    DIRECT_SLAB_WRITER,
}
``````

Physical capability enums:

``````kotlin
internal enum class FrozenDiagnosticEvidenceModel {
    SYMBOL_NAME_ONLY,
    BINARY_DECLARATION,
    SOURCE_FILE_ONLY,
    SOURCE_FILE_AND_LINE,
    SOURCE_RANGE,
    PRECOMPUTED_DIAGNOSTIC_SPAN,
}
``````

``````kotlin
internal enum class FrozenBackendConcurrencyModel {
    SINGLE_THREAD_CONFINED,
    SESSION_THREAD_CONFINED,
    LANE_CONFINED,
    CONCURRENT_READ_ONLY,
    CONCURRENT_SESSION_SAFE,
}
``````

``````kotlin
internal enum class FrozenBackendIdentityStability {
    EPHEMERAL_HANDLE,
    SESSION_STABLE,
    SNAPSHOT_STABLE,
    IMAGE_PERSISTENT,
}
``````

``````kotlin
internal enum class FrozenBackendBatchingModel {
    NONE,
    ORDERED_BATCH,
    UNORDERED_BATCH_WITH_DETERMINISTIC_MERGE,
    SNAPSHOT_BULK_EXPORT,
    DIRECT_ORDINAL_MATERIALIZATION,
}
``````

### 9.1. Diagnostic Evidence Law

Diagnostic evidence capability governs diagnostic rendering only.

It must not influence:

- TypeReference identity;
- frozen key equality;
- frozen ordinal assignment;
- planning semantics;
- PlanCacheKey material;
- L2 key material;
- route64 derivation;
- HID derivation;
- protocol-owned intern ids.

If a backend cannot provide source locations, diagnostics must degrade to the strongest available backend-neutral
evidence model.

Examples:

``````text
Reflection:
    SYMBOL_NAME_ONLY or BINARY_DECLARATION

KSP / KSP2:
    SOURCE_FILE_AND_LINE or SOURCE_RANGE

Bytecode:
    BINARY_DECLARATION
    SOURCE_FILE_AND_LINE only if reliable line table material is available

Precomputed index:
    PRECOMPUTED_DIAGNOSTIC_SPAN when the manifest provides stable spans
``````

Source location evidence is diagnostic provenance.

It is not frozen semantic identity.

### 9.2. Backend Concurrency Model Law

Backend concurrency capability governs scheduling only.

It must not influence semantic output.

If a backend is:

``````text
SINGLE_THREAD_CONFINED
SESSION_THREAD_CONFINED
LANE_CONFINED
``````

then acquisition strategy must serialize backend calls through the required deterministic owner.

If a backend is concurrent-safe, parallel execution may change latency only.

It must not change:

- TypeReference closure membership;
- frozen ordinal assignment;
- table layout;
- diagnostic classification;
- image digest;
- planning-visible facts.

Concurrency capability must not be inferred from implementation convenience.

A backend must declare the most conservative model that is safe for its native handle/session lifecycle.

Examples:

``````text
Reflection:
    SESSION_THREAD_CONFINED or LANE_CONFINED unless the registry implementation proves stronger safety.

KSP / KSP2:
    SESSION_THREAD_CONFINED by default because resolver/session lifecycle is backend-owned.

Bytecode index:
    CONCURRENT_READ_ONLY if parser/index data is immutable and read-only.

Precomputed immutable index:
    CONCURRENT_READ_ONLY or CONCURRENT_SESSION_SAFE.
``````

### 9.3. Backend Identity Stability Law

Backend identity stability is not Kontrakt semantic identity.

It may guide:

- acquisition strategy;
- diagnostic explanation;
- future cache-eligibility heuristics;
- backend snapshot compatibility checks.

It must not be used as:

- TypeReference equality;
- frozen key equality;
- HID replacement;
- route64 material;
- PlanCacheKey material;
- protocol-owned intern id;
- frozen ordinal authority.

ADR-0041 owns stable semantic identity.

Examples:

``````text
EPHEMERAL_HANDLE:
    object identity or backend handle identity valid only for a short-lived invocation.

SESSION_STABLE:
    stable inside one backend session, invalid after session close.

SNAPSHOT_STABLE:
    stable inside a compiler/static/index snapshot.

IMAGE_PERSISTENT:
    stable across persisted image/index boundaries, but still not semantic authority
    until ADR-0041 canonical verification accepts it.
``````

Even `IMAGE_PERSISTENT` backend identity must be verified through canonical ADR-0041 identity machinery before becoming
semantic identity.

### 9.4. Batching Law

Batching capability governs how aggressively the acquisition strategy may request backend material.

Batch result order is not semantic order.

If a backend returns unordered batch results, the acquisition core must deterministically merge them before:

- TypeReference closure membership;
- frozen ordinal assignment;
- table layout;
- diagnostics classification;
- publication.

`DIRECT_ORDINAL_MATERIALIZATION` is allowed only when the backend can satisfy the same frozen semantic contract as
ordinary acquisition.

It must not use backend-local ordinal, discovery order, callback completion order, or manifest order as frozen ordinal
authority unless the order is first canonicalized and validated under Kontrakt's frozen ordering law.

Examples:

``````text
NONE:
    one request at a time.

ORDERED_BATCH:
    backend preserves requested order, but Kontrakt still validates and canonicalizes.

UNORDERED_BATCH_WITH_DETERMINISTIC_MERGE:
    backend may return arbitrary order; core must deterministically merge.

SNAPSHOT_BULK_EXPORT:
    backend exports a whole immutable snapshot for acquisition.

DIRECT_ORDINAL_MATERIALIZATION:
    backend may write directly into ordinal-oriented material only after
    frozen ordinal law has been established by Kontrakt.
``````

### 9.5. Capability Integrity Law

Capability is an agreement between backend and acquisition strategy.

If a backend overclaims capability, the resulting acquisition is unsafe.

If a backend underclaims capability, the result may be slower but must remain correct.

Therefore:

- capability overclaim is an integrity failure when detected;
- conservative capability declaration is allowed;
- strategy may use capability to choose slower safe paths;
- strategy must not use capability to bypass frozen semantic validation;
- capability is snapshot material and must be pinned for the backend session/acquisition episode it governs.

## 10. Frozen Acquisition State Model

Frozen acquisition must use explicit state axes.

It must not reuse L2 shared-slot, waiter, or builder-handle state names.

Planning L2 lifecycle principles are imported.

Planning L2 vocabulary is not imported.

### 10.1. FrozenAcquisitionRunState

Illustrative shape:

``````kotlin
internal enum class FrozenAcquisitionRunState {
    INITIALIZED,
    RUNNING,
    AWAITING_BACKEND_READY,
    READY_TO_RESTART,
    RESTARTING,
    COMPLETED,
    ABORTED,
    PANIC_ISOLATED,
}
``````

`AWAITING_BACKEND_READY` does not mean worker synchronous waiting.

It means the logical acquisition run is not executing and is awaiting a readiness publication through the readiness
bridge.

No worker thread, acquisition lane, open arena, backend-native handle, or coroutine continuation is retained as the
waiting owner.

Legal transitions:

``````text
INITIALIZED -> RUNNING

RUNNING -> AWAITING_BACKEND_READY
AWAITING_BACKEND_READY -> READY_TO_RESTART
READY_TO_RESTART -> RESTARTING
RESTARTING -> RUNNING

RUNNING -> COMPLETED

RUNNING -> ABORTED
AWAITING_BACKEND_READY -> ABORTED
READY_TO_RESTART -> ABORTED
RESTARTING -> ABORTED

RUNNING -> PANIC_ISOLATED
AWAITING_BACKEND_READY -> PANIC_ISOLATED
READY_TO_RESTART -> PANIC_ISOLATED
RESTARTING -> PANIC_ISOLATED
``````

Forbidden transitions:

``````text
AWAITING_BACKEND_READY -> RUNNING
READY_TO_RESTART -> COMPLETED
COMPLETED -> any
ABORTED -> any
PANIC_ISOLATED -> any
``````

Any transition absent from the legal matrix is illegal.

### 10.2. FrozenAcquisitionNodeState

Each acquired `TypeReference` node progresses through explicit states.

Illustrative shape:

``````kotlin
internal enum class FrozenAcquisitionNodeState {
    DISCOVERED,
    SHAPE_RESOLVED,
    SHAPE_EDGES_ENQUEUED,
    CYCLE_IDENTITY_RESOLVED,
    RAW_FACTS_RESOLVED,
    RAW_FACT_EDGES_ENQUEUED,
    REFERENCE_MATERIAL_COMMITTED,
    SEALED,
    BACKEND_READINESS_PENDING,
    DEFERRED_TO_BACKEND_PROGRESS,
    FAILED,
}
``````

`REFERENCE_MATERIAL_COMMITTED` means the node's backend-erased acquisition material has been staged.

It is not final image publication and it is not planning-visible state.

Rules:

- `SEALED` nodes must not be mutated.
- `BACKEND_READINESS_PENDING` nodes record backend readiness dependency only; they do not retain worker ownership, open
  arena ownership, or backend-native continuation state.
- `DEFERRED_TO_BACKEND_PROGRESS` nodes must not enter a final published image.
- `FAILED` nodes must not enter a final published image.
- backend completion order must not define node sealing order.
- node state is acquisition material, not planning-visible state.

### 10.3. FrozenAcquisitionPublicationState

Publication state is separate from acquisition run state.

Illustrative shape:

``````kotlin
internal enum class FrozenAcquisitionPublicationState {
    NOT_STARTED,
    ALIGNING,
    VALIDATING,
    PUBLISHED,
    REJECTED,
}
``````

Rules:

- `PUBLISHED` is terminal.
- `REJECTED` is terminal.
- `VALIDATING` must not consult backend-native handles.
- publication may begin only when no pending, deferred, or failed acquisition material remains.
- publication must validate before planning-facing providers are exposed.

### 10.4. FrozenAcquisitionArenaState

Illustrative shape:

``````kotlin
internal enum class FrozenAcquisitionArenaState {
    OPEN,
    SEALED,
    RELEASED,
    POISONED,
}
``````

Rules:

- `OPEN` arenas may be mutated only by their owning acquisition episode.
- `SEALED` arenas may be read only for lawful finalization or diagnostics.
- `RELEASED` arenas must no longer be accessed by acquisition logic.
- `POISONED` arenas are panic-isolated and must not be reused.
- a published image must not retain an `OPEN`, `SEALED`, or mutable arena.
- pending backend work must not keep an open mutable arena alive while external readiness is outstanding.

## 11. Version, Epoch, Generation, and Policy Axes

Frozen acquisition distinguishes policy governance, logical acquisition continuity, scratch backing freshness, and
backend session stale-signal defense.

The shared runtime policy epoch is common across planning, frozen acquisition, L2 governance, dispatch, storage
governance, and worker lifecycle.

The other axes remain bounded-context-specific.

Illustrative vocabulary:

``````text
RuntimePolicyEpoch
FrozenAcquisitionRunEpoch
FrozenAcquisitionBackingEpoch
FrozenBackendSessionGeneration
``````

Meanings:

``````text
RuntimePolicyEpoch:
    shared immutable runtime-policy snapshot epoch.
    It identifies the resolved policy snapshot that governs this acquisition.
    It is policy governance only.

FrozenAcquisitionRunEpoch:
    one logical frozen acquisition continuity across fresh restarts and deterministic replay episodes.
    It is acquisition run continuity only.

FrozenAcquisitionBackingEpoch:
    acquisition scratch/arena backing freshness marker.
    It is mutable backing freshness only.

FrozenBackendSessionGeneration:
    stale-signal defense for backend session/resolver/index episodes.
    It is backend-session lifecycle discrimination only.
``````

Rules:

- `RuntimePolicyEpoch` is shared policy governance.
- `RuntimePolicyEpoch` is not planning run continuity.
- `RuntimePolicyEpoch` is not frozen acquisition run continuity.
- `RuntimePolicyEpoch` is not worker backing freshness.
- `RuntimePolicyEpoch` is not backend session generation.
- `RuntimePolicyEpoch` is not L2 host generation.
- `RuntimePolicyEpoch` is not dispatch lane lifecycle.
- `FrozenAcquisitionRunEpoch` is frozen acquisition logical continuity only.
- `FrozenAcquisitionBackingEpoch` is scratch / arena backing freshness only.
- `FrozenBackendSessionGeneration` is stale backend signal defense only.
- no bare `Epoch` or `Generation` name is allowed in normative code or documentation.

### 11.1. Shared Policy Epoch Dependency Rule

Frozen acquisition domain code may depend on `RuntimePolicyEpoch` only after it has been promoted to a shared
runtime/governance package.

Forbidden dependency:

``````kotlin
import planning.infrastructure.runtime.policy.RuntimePolicyEpoch
``````

Allowed target dependency examples:

``````kotlin
import runtime.domain.policy.RuntimePolicyEpoch
``````

or:

``````kotlin
import governance.domain.policy.RuntimePolicyEpoch
``````

### 11.2. Policy Snapshot Slicing Rule

`RuntimePolicyEpoch` must point to a resolved policy snapshot that is sliced by bounded context.

Illustrative target shape:

``````kotlin
class RuntimePolicyEpoch private constructor(
    val id: Long,
    val snapshot: ResolvedRuntimePolicySnapshot,
)
``````

``````kotlin
class ResolvedRuntimePolicySnapshot private constructor(
    val planning: ResolvedPlanningRuntimePolicy,
    val frozenAcquisition: ResolvedFrozenAcquisitionPolicy,
    val l2Join: ResolvedL2JoinPolicy,
    val l2Storage: ResolvedL2StoragePolicy,
    val dispatch: ResolvedDispatchLanePolicy,
    val runtimeWatchdog: ResolvedRuntimeWatchdogPolicy?,
)
``````

Frozen acquisition must consume only:

``````text
snapshot.frozenAcquisition
``````

unless another policy slice is explicitly admitted by a later ADR or amendment.

Planning must not consume `snapshot.frozenAcquisition`.

Frozen acquisition must not consume planning-only policy fields.

Frozen acquisition must not consume L2-storage-only policy fields unless explicitly allowed by a later ADR.

This prevents a shared epoch from becoming a shared mutable runtime object.

## 12. FrozenAcquisitionEngine

The acquisition engine advances bounded acquisition episodes.

An episode may complete, enter external-readiness state, defer to backend progress, exhaust its deterministic work
quantum,
abort, or isolate panic.

Illustrative shape:

``````kotlin
internal interface FrozenAcquisitionEngine<ROOT> {
    fun advanceEpisode(
        context: FrozenAcquisitionRunContext<ROOT>,
    ): FrozenAcquisitionEngineOutcome<ROOT>

    fun restartFromAdmission(
        admission: FrozenAcquisitionRestartAdmission<ROOT>,
    ): FrozenAcquisitionEngineOutcome<ROOT>
}
``````

Outcome shape:

``````kotlin
internal sealed class FrozenAcquisitionEngineOutcome<ROOT> {
    class Completed<ROOT>(
        val assemblyInput: FrozenMetamodelImageAssemblyInput,
    ) : FrozenAcquisitionEngineOutcome<ROOT>()

    class AwaitingReadiness<ROOT>(
        val readiness: FrozenAcquisitionReadinessWait<ROOT>,
    ) : FrozenAcquisitionEngineOutcome<ROOT>()

    class Deferred<ROOT>(
        val deferredSet: FrozenAcquisitionDeferredSet,
    ) : FrozenAcquisitionEngineOutcome<ROOT>()

    class Aborted<ROOT>(
        val reasonCode: String,
    ) : FrozenAcquisitionEngineOutcome<ROOT>()

    class PanicIsolated<ROOT>(
        val reasonCode: String,
    ) : FrozenAcquisitionEngineOutcome<ROOT>()
}
``````

`FrozenAcquisitionRunContext` owns:

- `FrozenAcquisitionRunEpoch`;
- a pinned shared `RuntimePolicyEpoch`;
- the resolved frozen acquisition policy slice derived from that epoch;
- root input snapshot;
- schema version;
- backend identity algorithm snapshot;
- remaining acquisition budget ledger;
- run-scoped `FrozenAcquisitionRemainingBudget`;
- current run state;
- current publication state;
- current active readiness handle if any;
- fresh-restart admission authority.

The run context does not own the global runtime policy registry.

The run context does not own the full runtime policy lifecycle.

The run context must not mutate the pinned runtime policy snapshot.

The run context must not consume planning-only policy fields.

The run context must not consume L2-storage-only policy fields unless explicitly allowed by a later ADR.

The pinned runtime policy epoch is governance material.

The frozen acquisition run epoch is acquisition continuity material.

They are intentionally separate.

It does not own backend-native handles.

## 13. Readiness Callback and Fresh Restart Law

Frozen acquisition does not use coroutine continuation as semantic control.

Frozen acquisition does not retain worker threads, acquisition lanes, or execution resources while backend readiness is
outstanding.

Frozen acquisition does not continue from a half-executed acquisition episode.

Frozen acquisition does not retain a half-executed worker-local acquisition episode as the owner of future progress.

When `PendingBackendWork` occurs, the engine must:

1. create an immutable `FrozenAcquisitionRestartDescriptor` that describes a lawful fresh-restart boundary, not a
   continuation;
2. install one active `FrozenAcquisitionReadinessHandle` for Level 1.5;
3. transition `RUNNING -> AWAITING_BACKEND_READY`;
4. seal, release, or poison the current mutable acquisition arena as appropriate;
5. release worker-local execution resources;
6. register a one-shot readiness callback through `FrozenAcquisitionReadinessBridge`;
7. remain in a logical readiness state, not in worker execution;
8. transition `AWAITING_BACKEND_READY -> READY_TO_RESTART` when readiness is lawfully published;
9. admit a fresh restart through `FrozenAcquisitionRestartAdmission`;
10. transition `READY_TO_RESTART -> RESTARTING`;
11. create a fresh acquisition lease or episode;
12. restart acquisition from a clean boundary under the same `FrozenAcquisitionRunEpoch` and pinned
    `RuntimePolicyEpoch`.

Level 1.5 allows at most one active backend readiness wait per acquisition run.

Multiple concurrent backend readiness handles require a later design note or ADR amendment.

### 13.1. Already-Ready and Asynchronously-Ready Convergence

A backend operation may discover that the material is already ready after returning a readiness-capable handle.

A backend operation may also complete asynchronously later.

Both paths must converge to the same `READY_TO_RESTART` publication discipline.

The bridge owns the one-shot convergence.

Backend code must not directly mutate `FrozenAcquisitionRunContext`.

### 13.2. Restart Descriptor Law

A `FrozenAcquisitionRestartDescriptor` is not a continuation descriptor.

It must not point back into a half-executed acquisition episode.

It may retain only adapter-neutral, backend-erased, and restart-safe material.

Allowed descriptor material includes:

- root snapshot identity;
- schema version;
- pinned `RuntimePolicyEpoch` identity;
- `FrozenAcquisitionRunEpoch`;
- backend identity algorithm snapshot;
- readiness handle identity through the readiness bridge only;
- deterministic replay seed material;
- budget ledger carry-forward material where the ledger law permits it.

Forbidden descriptor material includes:

- `KType`;
- `KClass`;
- `KSType`;
- `KSDeclaration`;
- `Resolver`;
- compiler IR node;
- bytecode parser node;
- source AST handle;
- closure capturing backend-native material;
- open mutable arena;
- mutable frontier cursor;
- half-filled mutable slab builder;
- thread-local execution state;
- coroutine continuation state.

## 14. Fresh Restart, Deterministic Replay, and Future Immutable Checkpoint Law

Level 1.5 does not require a full query engine.

Level 1.5 also does not introduce continuation-style acquisition restart.

The normal readiness path is a fresh acquisition restart after the previous mutable acquisition episode has been
dropped, sealed, released, or poisoned.

The baseline Level 1.5 implementation may conservatively perform deterministic replay from the stable root input
snapshot.

This is accepted as a correctness-first foundation.

Replay cost is a performance concern, not a reason to reintroduce continuation-style state.

A compliant Level 1.5 implementation must distinguish:

``````text
normal path:
    readiness callback
    READY_TO_RESTART publication
    fresh acquisition episode
    deterministic replay from a clean boundary

future optimization path:
    validated immutable checkpoint
    backend-erased sealed prefix
    stable fingerprint / HID verification
    sealed-slot coverage proof

forbidden path:
    half-executed acquisition episode continuation
    mutable frontier cursor continuation
    open arena continuation
    backend-native handle continuation
    coroutine continuation
``````

Root replay may be optimized in future freezing stages, but only by using validated immutable material.

Such optimization is a fresh-restart optimization, not continuation-style state reuse.

It must not preserve arbitrary call stack state.

It must not preserve open arena state.

It must not preserve backend-native handles.

Future immutable checkpoint and replay-cutoff support requires:

- stable HID;
- canonical fingerprint;
- trace-compatible dependency recording;
- deterministic replay cutoff;
- sealed-slot coverage proof;
- collision verification law.

That belongs to ADR-0041 and later v2 query/incremental work.

## 15. FrozenAcquisitionReadinessBridge

The readiness bridge translates backend readiness callbacks into acquisition restart readiness.

It is not semantic authority.

It is not a backend operation executor.

It is not a worker-retention mechanism.

Responsibilities:

- wrap backend readiness handles;
- register one-shot readiness callbacks;
- filter stale backend-session generations;
- collapse duplicate completion signals;
- publish `READY_TO_RESTART` at most once;
- preserve `FrozenAcquisitionRunEpoch` continuity;
- preserve pinned `RuntimePolicyEpoch` continuity;
- prevent backend planes from mutating run state directly.

Invariants:

``````text
1. At most one active backend readiness wait exists per Level 1.5 acquisition run.
2. At most one successful READY_TO_RESTART publication exists per pending backend episode.
3. READY_TO_RESTART may be published only from AWAITING_BACKEND_READY.
4. Fresh Restart admission may be produced only from READY_TO_RESTART.
5. Fresh Restart continues the same FrozenAcquisitionRunEpoch.
6. Fresh Restart observes the same pinned RuntimePolicyEpoch.
7. Fresh Restart carries the same remaining acquisition budget ledger.
8. Backend work planes never mutate FrozenAcquisitionRunContext directly.
9. Readiness handles never leak as raw orchestration authority beyond the bridge boundary.
10. Stale FrozenBackendSessionGeneration signals do not reopen terminal acquisition runs.
``````

### 15.1. Backend Readiness Starvation Law

A backend that repeatedly reports pending readiness without producing usable material may starve acquisition.

ADR-0040 therefore requires a resolved backend readiness policy.

Illustrative policy shape:

``````kotlin
class ResolvedFrozenBackendReadinessPolicy private constructor(
    val maxReadinessWaitsPerRun: Int,
    val maxReadinessWaitsPerNode: Int,
    val maxRestartAdmissionsPerRun: Int,
    val maxConsecutiveReadinessWaitsOnSameNode: Int,
    val pendingDeadlineNanos: Long,
    val readyToRestartDeadlineNanos: Long,
    val deterministicBackoffPolicy: ResolvedDeterministicBackoffPolicy,
)
``````

Rules:

- random jitter is forbidden as semantic acquisition control;
- scheduler timing must not define fresh restart priority;
- backend completion order must not define acquisition order;
- starvation limits are resolved policy material;
- exceeding readiness limits fails closed;
- repeated pending on the same node may be promoted to `DeferredToBackendProgress` only if backend capability and reason
  code explicitly justify that classification;
- otherwise repeated pending becomes a backend starvation failure.

## 16. FrozenAcquisitionStrategy

Acquisition algorithm is strategy-owned.

Backend identity is backend-owned.

Frozen semantic law is core-owned.

Illustrative shape:

``````kotlin
internal interface FrozenAcquisitionStrategy<ROOT> {
    fun advanceEpisode(
        context: FrozenAcquisitionRunContext<ROOT>,
        backend: FrozenMetamodelAcquisitionBackend<ROOT>,
    ): FrozenAcquisitionEngineOutcome<ROOT>
}
``````

Level 1.5 adopts:

``````text
TypeReferenceClosureAcquisitionStrategy
``````

Future strategies may include:

``````text
WholeProgramIndexAcquisitionStrategy
TwoPassOrdinalAcquisitionStrategy
DirectToSlabAcquisitionStrategy
ParallelDeterministicAcquisitionStrategy
IncrementalQueryAcquisitionStrategy
``````

All strategies must satisfy the same frozen semantic contract.

## 17. TypeReferenceClosureAcquisitionStrategy

This strategy computes `TypeReference` closure and frozen table coverage.

It does not construct a planning traversal tree.

It does not select active members.

It does not decide expansion topology.

Baseline execution shape:

``````text
begin backend session
snapshot backend identity algorithm metadata
initialize budget ledger
initialize deterministic TypeReference closure frontier
issue root references
advance bounded acquisition episodes until one terminal or boundary outcome is reached
finish backend session only after closure completeness and backend readiness constraints are satisfied
build FrozenMetamodelImageAssemblyInput
``````

This strategy must not be specified as an unbounded synchronous loop.

The engine advances bounded acquisition episodes.

An episode must return control when:

``````text
backend readiness is required
the deterministic episode work quantum is exhausted
capacity is exhausted
publication becomes possible
backend deferral is observed
panic isolation is required
the acquisition completes
``````

Conceptual dispatcher shape:

``````text
advanceEpisode(context):
    1. validate run state is RUNNING
    2. acquire the next deterministic closure work item
    3. execute one bounded acquisition episode
    4. if backend material is pending:
           publish restart descriptor
           release worker-owned transient authority
           register readiness callback
           return AwaitingReadiness
    5. if deterministic work remains and episode quantum remains:
           continue within the bounded episode
    6. if deterministic work remains but quantum is exhausted:
           return RunnableProgress
    7. if no work remains:
           validate closure completeness
           return Completed or ReadyForPublication
``````

The episode quantum is deterministic capacity material.

It must not be derived from wall-clock time, scheduler timing, callback timing, or backend completion order.

Reference processing phases:

``````text
1. DEQUEUE_REFERENCE
2. RESOLVE_SHAPE
3. REGISTER_SHAPE_REFERENCE_REQUIREMENTS
4. RESOLVE_CYCLE_IDENTITY
5. RESOLVE_RAW_FACTS
6. REGISTER_RAW_FACT_REFERENCE_REQUIREMENTS
7. COMMIT_REFERENCE_MATERIAL
8. COMPLETE_REFERENCE
9. VALIDATE_CLOSURE_COMPLETENESS
``````

Phase enum:

``````kotlin
internal enum class FrozenTraversalPhase {
    BEGIN_SESSION,
    ENQUEUE_ROOT,
    DEQUEUE_REFERENCE,
    RESOLVE_SHAPE,
    REGISTER_SHAPE_REFERENCE_REQUIREMENTS,
    RESOLVE_CYCLE_IDENTITY,
    RESOLVE_RAW_FACTS,
    REGISTER_RAW_FACT_REFERENCE_REQUIREMENTS,
    COMMIT_REFERENCE_MATERIAL,
    COMPLETE_REFERENCE,
    VALIDATE_CLOSURE_COMPLETENESS,
    FINISH_SESSION,
}
``````

`COMMIT_REFERENCE_MATERIAL` commits backend-erased, adapter-neutral material for the current `TypeReference` into
acquisition-owned staged frozen material.

It does not publish a `FrozenMetamodelImage`.

Final publication remains owned by the publication gate and may occur only after closure completeness, table coverage,
lowering completeness, and image integrity validation succeed.

`VALIDATE_CLOSURE_COMPLETENESS` verifies that every reference requirement discovered from shape or raw-fact material was
either:

- accepted into TypeReference closure accounting;
- resolved as explicitly unavailable or unsupported;
- rejected fail-closed;
- classified as backend deferral that prevents publication;
- or classified as pending backend readiness that prevents publication.

No discovered reference requirement may disappear silently.

Preferred acquisition terminology is:

``````text
reference material
staged material
table row
record
slot
coverage material
``````

Rules:

- adapter discovery order is non-authoritative;
- backend completion order is non-authoritative;
- TypeReference closure membership authority is deterministic and explicit;
- every discovered reference requirement is budgeted;
- every accepted payload is budgeted;
- textual/canonical surfaces are byte-budgeted;
- duplicate semantic keys fail closed;
- comparator-equal but structurally unequal material fails closed;
- publication cannot start while pending or deferred material remains;
- registered reference requirements are closure requirements, not planning children;
- frozen acquisition must not turn reference requirements into expansion branches.

## 18. DeterministicTypeReferenceFrontier

Level 1.5 frontier authority must be deterministic and exact.

The initial smoke implementation may use an `ArrayList`-based sorted membership order.

The Level 1.5 target is stronger:

``````text
DeterministicPagedTypeReferenceIndex
``````

This avoids turning `ArrayList` insertion shift into the accepted Level 1.5 physical target.

### 18.1. Transitional Smoke Structure

Temporary bring-up may use:

``````text
discoveryOrder:
    ArrayList<TypeReference>
    traversal discovery order for diagnostics and restart evidence
    not frozen semantic order

membershipOrder:
    ArrayList<TypeReference>
    sorted by FrozenTypeReferenceOrder
    binary-search membership authority

pending:
    ArrayDeque<TypeReference>
    deterministic processing queue
``````

Known cost:

``````text
membership lookup:
    O(log N * TypeReference compare cost)

insertion:
    O(N) array shift
``````

This is allowed only as migration debt or smoke implementation.

### 18.2. Level 1.5 Target Structure

The Level 1.5 target frontier is a deterministic paged index.

Illustrative shape:

``````text
DeterministicPagedTypeReferenceIndex:
    pages: fixed-order page array
    pageMaxima: sorted maximum key per page
    per-page sorted TypeReference storage
    deterministic split rule
    deterministic compaction rule
    no HashMap
    no HashSet
    no hashCode authority
``````

Lookup:

``````text
1. binary-search pageMaxima
2. binary-search within selected page
3. verify comparator/equality consistency
``````

Insertion:

``````text
1. insert within page
2. shift only inside page
3. split page by deterministic rule if full
4. update pageMaxima
``````

Page split must not depend on timing, allocation identity, thread identity, or backend order.

### 18.3. Enqueue Algorithm

``````text
enqueueIfNew(reference):
    1. query deterministic membership index using FrozenTypeReferenceOrder
    2. if comparator-equal and equals true:
           already discovered
    3. if comparator-equal and equals false:
           fail closed
    4. otherwise:
           insert reference through deterministic membership index
           append reference to discoveryOrder
           append reference to pending runnable queue
           consume discovered and pending budget
``````

### 18.4. TypeReference Structural Verification Basis

Comparator equality is not equality authority.

A comparator-equal candidate is a membership hit only after exact frozen structural verification.

The verification basis must be backend-erased `TypeReference` canonical verification material available after lowering.

Allowed verification material may include:

- canonical classifier identity text;
- canonical package, module, and owner material;
- canonical type-argument material;
- nullability and material flags;
- variance, function, array, and suspend flags;
- canonical annotation-use-site identity where it participates in `TypeReference` identity;
- backend-erased disambiguation material required by the `TypeReference` protocol.

Forbidden equality authorities:

- backend object identity;
- `KType` identity;
- `KSType` identity;
- source location alone;
- bytecode parser node identity;
- classloader object identity;
- unverified fingerprint equality;
- callback completion order.

Backend-provided fingerprints may be retained as diagnostic or fast-reject material only.

They are not semantic equality authority unless ratified by ADR-0041 canonical HID/interner law.

If two backend handles lower to comparator-equal but structurally unequal `TypeReference` values, acquisition fails
closed.

If the backend cannot provide enough backend-erased verification material to distinguish them, lowering must be rejected
before publication.

### 18.5. Transitional Membership Equality and Final Interned Membership

Level 1.5 comparator/equality membership is transitional.

The final architecture moves membership authority to ADR-0041's stable metadata identity substrate:

``````text
TypeReference canonical material
-> canonical byte encoding
-> BLAKE3 / HID fingerprint
-> collision-verified protocol-owned interning
-> stable TypeReferenceInternId
-> primitive membership / sorting / table addressing
``````

Final frontier membership must use verified stable intern ids or frozen image ordinals after ADR-0041 ratification.

The final path must not perform repeated `TypeReference` structural comparison on the acquisition hot path.

HID or fingerprint equality alone is not semantic equality authority.

A compliant interner must verify canonical bytes or canonical structural payload on collision before returning an
existing intern id.

Intern id assignment must be acquisition-order independent and callback-completion-order independent.

ADR-0040 does not define the final HID, byte encoding, collision verification, or interning protocol.

ADR-0041 owns those details.

### 18.6. Forbidden Frontier Authorities

The following must not define TypeReference closure membership:

- `HashSet`;
- `HashMap`;
- `TypeReference.hashCode`;
- `System.identityHashCode`;
- `ReflectionTypeHandleRegistry`;
- KSP resolver identity;
- compiler IR node identity;
- bytecode parser node identity;
- classloader identity;
- adapter discovery ordinal;
- platform collection iteration order.

### 18.7. Future Frontier Physical Evolution

Future evolution:

``````text
Level 2:
    stable identity / protocol-owned interning

Level 2.5:
    DeterministicOpenAddressedTypeReferenceSet

Level 3:
    primitive membership frontier

Level 4:
    direct-to-slab / entity-like ordinal layout
``````

Future deterministic open-addressed table sketch:

``````text
keys64:            LongArray
internId:          IntArray
state:             ByteArray
verificationIndex: IntArray
``````

Probe laws:

- fixed seed;
- fixed probe sequence;
- deterministic capacity schedule;
- no JVM hashCode;
- no platform `HashMap`;
- no randomized probing;
- no hash64-only equality;
- canonical verification payload required on collision.

## 19. Frozen Acquisition Capacity Resolution

Frozen acquisition capacity is derived from the frozen acquisition slice of the pinned `RuntimePolicyEpoch`.

The acquisition engine does not invent local default limits.

The acquisition engine does not read planning policy fields to derive frozen acquisition limits.

The acquisition engine does not read L2 retention policy fields to derive frozen acquisition limits.

Capacity resolution happens before acquisition admission.

### 19.1. Capacity Resolution Discipline

Frozen acquisition follows the same capacity-resolution discipline as planning.

Planning does not treat concrete node/table/depth/undo counts as public policy input.

Planning resolves a byte/resource envelope first and then derives concrete caps through a deterministic capacity
resolver.

Frozen acquisition must do the same.

The frozen capacity flow is:

``````text
ResourceProfile
-> ResolvedFrozenAcquisitionBudget
-> ResolvedFrozenSizingCalibration
-> FrozenAcquisitionCapacityResolver
-> ResolvedFrozenAcquisitionCaps
``````

The frozen acquisition core consumes only `ResolvedFrozenAcquisitionCaps`.

It must not read heap state, CPU count, cgroup/container memory, live telemetry, previous policy, backend timing, or
previous acquisition outcomes directly.

### 19.2. Public Surface

The public/operator-facing surface remains high-level.

The accepted V1 resource profile surface is:

``````text
AUTO
SMALL
STANDARD
LARGE
``````

For V1:

``````text
AUTO = STANDARD
``````

Low-level values such as:

``````text
maxTypeReferenceClosureSize
constructorRecordCap
constructorParameterRecordCap
propertyRecordCap
annotationRecordCap
shapeTableCap
cycleIdentityTableCap
rawFactTableCap
sortBufferBytesCap
canonicalTextDedupTableCap
restartControlBytesCap
``````

are not public profile definitions.

They are deterministic capacity-solver outputs.

### 19.3. Resolved Frozen Acquisition Policy

The source policy slice is conceptually:

``````kotlin
class ResolvedFrozenAcquisitionPolicy private constructor(
    val budget: ResolvedFrozenAcquisitionBudget,
    val caps: ResolvedFrozenAcquisitionCaps,
    val backendPendingPolicy: ResolvedFrozenBackendPendingPolicy,
    val arenaPolicy: ResolvedFrozenAcquisitionArenaPolicy,
)
``````

ADR-0040 does not require the exact class shape above, but it requires the policy slicing rule:

``````text
frozen acquisition budget/caps
    come from the frozen acquisition policy slice

planning session budget/caps
    come from the planning policy slice

L2 storage retention budget/caps
    come from the L2 storage policy slice
``````

### 19.4. ResolvedFrozenAcquisitionBudget

`ResolvedFrozenAcquisitionBudget` is a policy snapshot value.

It is already resolved outside the frozen acquisition core.

It is immutable for the lifetime of one frozen acquisition run.

It is a byte/resource envelope, not a record-count list.

Illustrative shape:

``````kotlin
class ResolvedFrozenAcquisitionBudget private constructor(
    val maxFrozenBytesPerAcquisition: Long,
    val maxAcquisitionSteps: Int,
    val maxBackendWorkUnits: Int,
    val maxCoverageWorkUnits: Int,
    val maxLoweringWorkUnits: Int,
    val maxCanonicalTextBytes: Long,
    val maxRawFactMaterialBytes: Long,
    val fixedHeadroomBytes: Long,
)
``````

The following must not be fields of `ResolvedFrozenAcquisitionBudget`:

``````kotlin
val maxConstructorRecords: Int
val maxConstructorParameters: Int
val maxPropertyRecords: Int
val maxAnnotationRecords: Int

val maxShapeTableRows: Int
val maxCycleIdentityTableRows: Int
val maxRawFactTableRows: Int

val maxSortBufferBytes: Long
val canonicalTextInternerTableCap: Int
val maxReentryDescriptorBytes: Long
``````

Those values either do not belong to ADR-0040 terminology or may exist only as concrete capacity-solver outputs.

Specifically:

- record-count caps are concrete capacity outputs;
- sort-buffer capacity is a concrete memory-cap output;
- freeze-time canonical text dedup/scratch table capacity is a concrete table-cap output;
- re-entry descriptors are forbidden;
- fresh-restart control material may be bounded through `restartControlBytesCap`.

### 19.5. Frozen Coverage and Lowering Work Budgets

Frozen acquisition does not own planning semantic expansion budget.

It does own frozen image completeness work.

`maxCoverageWorkUnits` bounds deterministic work required to establish frozen image coverage, including:

- reference requirement registration;
- shape table coverage;
- cycle identity table coverage;
- raw fact table coverage;
- closure completeness validation;
- explicit unavailable / unsupported / rejected classification.

`maxLoweringWorkUnits` bounds deterministic work required to lower backend-native material into adapter-neutral frozen
material, including:

- backend-erased shape lowering;
- backend-erased cycle identity lowering;
- backend-erased raw fact lowering;
- canonical text issue;
- canonical byte-length measurement;
- lowering completeness validation.

These budgets are not planning semantic graph expansion budgets.

They must not authorize frozen acquisition to construct planning traversal trees, active-member children,
constructor-selection branches, or property-eligibility branches.

### 19.6. ResolvedFrozenSizingCalibration

Sizing calibration is internal.

It is not user-facing API.

It is not a semantic protocol constant.

It is deterministic, versioned, and consumed only by the capacity resolver.

Illustrative shape:

``````kotlin
internal class ResolvedFrozenSizingCalibration private constructor(
    val canonicalTextReserveRatio: Double,
    val rawFactMaterialReserveRatio: Double,
    val diagnosticReserveRatio: Double,
    val sortBufferReserveRatio: Double,
    val restartControlReserveRatio: Double,

    val referenceRequirementLoadFactor: Double,
    val runnableQueueReserveRatio: Double,
    val membershipTableLoadFactor: Double,
    val tableOverProvisionRatio: Double,

    val typeReferenceSlotBytes: Int,
    val referenceRequirementSlotBytes: Int,
    val shapeTableRowBytes: Int,
    val cycleIdentityTableRowBytes: Int,
    val rawFactTableRowHeaderBytes: Int,
    val constructorRecordHeaderBytes: Int,
    val constructorParameterRecordHeaderBytes: Int,
    val propertyRecordHeaderBytes: Int,
    val annotationRecordHeaderBytes: Int,
    val offsetSlotBytes: Int,
    val lengthSlotBytes: Int,
    val coverageBitsetBytesPerRecordNumerator: Int,
    val coverageBitsetBytesPerRecordDenominator: Int,

    val secureWipeOnRelease: Boolean,
)
``````

The exact field set may evolve with the physical layout.

The stable law is:

``````text
calibration is deterministic, internal, versioned, and consumed only by the capacity resolver.
``````

### 19.7. ResolvedFrozenAcquisitionCaps

Concrete count/table/queue/memory limits are derived capacity outputs.

They are not public policy inputs.

Illustrative aggregate shape:

``````kotlin
internal class ResolvedFrozenAcquisitionCaps private constructor(
    val closure: ResolvedFrozenClosureCaps,
    val table: ResolvedFrozenTableCaps,
    val record: ResolvedFrozenRecordCaps,
    val memory: ResolvedFrozenMemoryCaps,
    val readiness: ResolvedFrozenReadinessCaps,
)
``````

Closure caps:

``````kotlin
internal class ResolvedFrozenClosureCaps private constructor(
    val typeReferenceClosureSizeCap: Int,
    val referenceRequirementCap: Int,
    val runnableQueueCap: Int,
)
``````

Table caps:

``````kotlin
internal class ResolvedFrozenTableCaps private constructor(
    val typeReferenceMembershipTableCap: Int,
    val referenceRequirementTableCap: Int,
    val shapeTableCap: Int,
    val cycleIdentityTableCap: Int,
    val rawFactTableCap: Int,
    val canonicalTextDedupTableCap: Int,
)
``````

Record caps:

``````kotlin
internal class ResolvedFrozenRecordCaps private constructor(
    val constructorRecordCap: Int,
    val constructorParameterRecordCap: Int,
    val propertyRecordCap: Int,
    val annotationRecordCap: Int,
)
``````

Memory caps:

``````kotlin
internal class ResolvedFrozenMemoryCaps private constructor(
    val frozenBytesCap: Long,
    val structBudgetBytes: Long,
    val arenaBytesCap: Long,
    val stagingBytesCap: Long,
    val sortBufferBytesCap: Long,
    val canonicalTextBytesCap: Long,
    val rawFactMaterialBytesCap: Long,
    val diagnosticBytesCap: Long,
    val restartControlBytesCap: Long,
)
``````

Readiness caps:

``````kotlin
internal class ResolvedFrozenReadinessCaps private constructor(
    val pendingBackendOperationCap: Int,
    val readinessCallbackCap: Int,
    val restartAdmissionCap: Int,
)
``````

Concrete caps exist because the engine must size arrays, tables, queues, bitsets, slabs, sort buffers, restart-control
buffers, canonical-text scratch structures, and diagnostic buffers.

But they must be produced by the capacity resolver.

They must not be directly configured as primary public policy.

### 19.8. Count Caps Are Solver Outputs

The policy layer must not define record-count limits as primary policy.

The following are concrete capacity outputs:

``````text
constructorRecordCap
constructorParameterRecordCap
propertyRecordCap
annotationRecordCap
shapeTableCap
cycleIdentityTableCap
rawFactTableCap
``````

They are derived from the byte/resource envelope.

They are not configured directly.

Normative rule:

``````text
Do not configure record counts directly.
Derive record counts from the resolved byte/resource envelope.
``````

This mirrors the planner capacity model:

``````text
Planning:
    maxPlannerBytesPerWorker
    -> structBudget
    -> node/table/depth/undo caps

Frozen:
    maxFrozenBytesPerAcquisition
    -> structBudget
    -> closure/table/record/queue caps
``````

### 19.9. Sort-Buffer Capacity

Frozen acquisition requires deterministic ordering, duplicate detection, conflict scans, deterministic sequence sorting,
and table alignment.

The capacity solver must reserve or derive a bounded sort-buffer capacity:

``````kotlin
val sortBufferBytesCap: Long
``````

This capacity belongs to `ResolvedFrozenMemoryCaps`.

It is not an additional public policy knob.

Sort-buffer bytes are part of the same frozen acquisition byte envelope.

They must be included in the feasibility calculation before acquisition starts.

### 19.10. Canonical Text Dedup / Scratch Table Capacity

If frozen acquisition uses a freeze-time canonical text deduplication or scratch table, the solver must derive a table
cap:

``````kotlin
val canonicalTextDedupTableCap: Int
``````

This table is a freeze-time scratch/dedup structure.

It is not the ADR-0041 protocol-owned interner.

Its slot ids must not be used as:

- stable TypeReference identity;
- route identity;
- cache identity;
- protocol identity;
- frozen ordinal;
- or planning-visible identity.

ADR-0041 owns final protocol-owned interning.

### 19.11. Restart Control Memory

Re-entry descriptors are forbidden.

Fresh restart control material may still require bounded memory for:

- suspension handle wrappers;
- readiness callback registration;
- restart descriptor;
- restart admission metadata;
- stale generation filter material;
- callback coalescing state.

The solver must derive:

``````kotlin
val restartControlBytesCap: Long
``````

This is not `maxReentryDescriptorBytes`.

The ADR-0040 model remains:

``````text
no re-entry
yes readiness callback bridge
yes READY_TO_RESTART
yes fresh acquisition restart
yes clean drop of mutable acquisition episode
``````

### 19.12. Fixed Metadata Overhead

Raw fact material bytes are not sufficient to model physical memory.

The solver must account for fixed metadata overhead, including at least:

- record headers;
- offset tables;
- length tables;
- coverage bitsets;
- ordinal tables;
- membership tables;
- reference requirement tables;
- sort buffers;
- staging buffers;
- diagnostic buffers;
- restart-control buffers;
- canonical text offset/index slots;
- canonical text length slots;
- raw fact material offset/index slots;
- raw fact material length slots;
- material-index metadata;
- deterministic alignment and padding;
- fixed headroom.

This overhead may be represented through a versioned internal layout calibration.

It must not be omitted from the byte feasibility calculation.

### 19.13. FrozenAcquisitionCapacityResolver

Illustrative shape:

``````kotlin
internal interface FrozenAcquisitionCapacityResolver {
    fun resolve(
        budget: ResolvedFrozenAcquisitionBudget,
        calibration: ResolvedFrozenSizingCalibration,
    ): ResolvedFrozenAcquisitionCaps
}
``````

For identical:

``````text
resolved frozen acquisition budget
resolved frozen sizing calibration
capacity resolver version tuple
``````

the capacity resolver must produce identical `ResolvedFrozenAcquisitionCaps`.

The resolver must be deterministic.

It must run before acquisition execution.

It must not inspect live heap, CPU count, cgroup/container state, telemetry, backend timing, or previous acquisition
outcomes.

### 19.14. Text-First and Material-First Reserve Law

Frozen acquisition capacity resolution reserves text and material payload space before deriving structural caps.

The solver computes structural budget only after reserving non-structural byte regions.

This law prevents payload-heavy input from consuming the same bytes that must be used for frontier structures, table
rows,
record arrays, offset tables, coverage bitsets, and other deterministic acquisition structures.

Conceptual formula:

``````text
reservedTextBytes =
    max(
        configuredCanonicalTextBytesFloor,
        maxFrozenBytesPerAcquisition * canonicalTextReserveRatio
    )

reservedRawFactMaterialBytes =
    max(
        configuredRawFactMaterialBytesFloor,
        maxFrozenBytesPerAcquisition * rawFactMaterialReserveRatio
    )

reservedNonStructuralBytes =
    fixedHeadroomBytes
  + reservedTextBytes
  + reservedRawFactMaterialBytes
  + diagnosticReserveBytes
  + emergencyDiagnosticBytesCap
  + sortBufferReserveBytes
  + restartControlReserveBytes

if reservedNonStructuralBytes >= maxFrozenBytesPerAcquisition:
    fail closed

structBudgetBytes =
    maxFrozenBytesPerAcquisition - reservedNonStructuralBytes

if structBudgetBytes < minRequiredStructuralBytes:
    fail closed
``````

The capacity solver derives closure, table, queue, and record caps from `structBudgetBytes`.

It must fail closed if the minimal valid frozen acquisition layout does not fit inside `structBudgetBytes`.

The solver must not treat payload bytes as proof that the associated index structures will fit.

Small payloads may still require large structural indexes.

Example:

``````text
1,000,000 tiny canonical text fragments
    may consume a small canonical text payload region
    but require a large offset/length/index region
``````

Therefore, material index overhead belongs to the structural layout byte model, not to a vague payload-size estimate.

The solver must validate:

``````text
TotalFrozenStructBytes(targetCaps) <= structBudgetBytes
``````

`TotalFrozenStructBytes(...)` must include at least:

- TypeReference membership table bytes;
- reference requirement table bytes;
- frontier queue/index bytes;
- frozen table row bytes;
- frozen record array bytes;
- canonical text offset/index slot bytes;
- canonical text length slot bytes;
- raw fact material offset/index slot bytes;
- raw fact material length slot bytes;
- coverage bitset bytes;
- ordinal table bytes;
- material-index metadata bytes;
- alignment and padding bytes;
- fixed metadata overhead derived from layout calibration.

Material-index overhead must not be omitted merely because the payload region itself fits.

Alignment and padding must be included in the feasibility calculation.

The exact alignment values are implementation-specific and versioned through the sizing calibration, but the solver must
account for them deterministically.

### 19.15. Desired vs Feasible Rule

The solver must distinguish desired capacity from feasible capacity.

Example:

``````text
desiredClosureSize:
    policy/calibration preference

feasibleClosureSize:
    maximum closure size that fits inside maxFrozenBytesPerAcquisition
    after canonical text reserve, raw fact reserve, staging reserve,
    diagnostics reserve, emergency diagnostic reserve, sort-buffer reserve,
    restart-control reserve, material-index overhead, alignment/padding,
    fixed metadata overhead, and fixed headroom
``````

Normative rule:

``````text
targetClosureSize = min(desiredClosureSize, feasibleClosureSize)
``````

Equivalent desired-vs-feasible rules apply to:

- TypeReference closure size;
- reference requirement table capacity;
- runnable queue capacity;
- TypeReference membership table capacity;
- shape table capacity;
- cycle identity table capacity;
- raw fact table capacity;
- constructor record capacity;
- constructor-parameter record capacity;
- property record capacity;
- annotation record capacity;
- canonical text dedup table capacity;
- staging capacity;
- sort-buffer capacity;
- diagnostic rendering capacity;
- restart-control capacity;
- material index / offset / length table capacity;
- alignment and padding capacity;
- readiness callback capacity;
- restart admission capacity.

The protocol must not encode undocumented hard ceilings or floors as semantic law.

### 19.16. Naming Corrections

`maxDiscoveredTypeReferences` is forbidden as a policy input name.

If this capacity exists, it must be a derived concrete cap named:

``````kotlin
val typeReferenceClosureSizeCap: Int
``````

`maxPendingTypeReferences` is also forbidden because it conflates two different capacities.

It must be split into:

``````kotlin
val runnableQueueCap: Int
val pendingBackendOperationCap: Int
``````

The first belongs to closure acquisition capacity.

The second belongs to readiness/callback capacity.

`maxReentryDescriptorBytes` is forbidden because re-entry is not an ADR-0040 semantic model.

If fresh-restart control material needs capacity, it must be represented as:

``````kotlin
val restartControlBytesCap: Long
``````

### 19.17. Solver Safety Constraints

The solver must fail closed if any of the following hold:

- `maxFrozenBytesPerAcquisition <= 0`;
- `fixedHeadroomBytes < 0`;
- `maxCanonicalTextBytes < 0`;
- `maxRawFactMaterialBytes < 0`;
- any reserve or cap used in byte arithmetic is negative;
- any intermediate byte arithmetic overflows;
- reserved non-structural bytes are greater than or equal to `maxFrozenBytesPerAcquisition`;
- `structBudgetBytes <= 0`;
- `structBudgetBytes < minRequiredStructuralBytes`;
- the minimal valid frozen acquisition layout does not fit;
- `TotalFrozenStructBytes(targetCaps) > structBudgetBytes`;
- material-index overhead is omitted from `TotalFrozenStructBytes(...)`;
- canonical text offset/index slots are omitted from `TotalFrozenStructBytes(...)`;
- canonical text length slots are omitted from `TotalFrozenStructBytes(...)`;
- raw fact material offset/index slots are omitted from `TotalFrozenStructBytes(...)`;
- raw fact material length slots are omitted from `TotalFrozenStructBytes(...)`;
- alignment or padding bytes are omitted from the feasibility calculation;
- any derived table capacity becomes non-positive;
- any derived queue capacity becomes non-positive;
- any `nextPowerOfTwo(...)` calculation overflows;
- any derived cap exceeds `Int.MAX_VALUE`;
- canonical text reserve and raw fact reserve are double-counted;
- sort-buffer reserve is omitted from feasibility calculation;
- emergency diagnostic reserve is omitted from feasibility calculation;
- restart-control reserve is omitted from feasibility calculation;
- fixed metadata overhead is omitted from feasibility calculation;
- concrete caps are derived from live environment state rather than the resolved policy snapshot.

A capacity solver must reject an invalid capacity envelope before acquisition execution starts.

It must not allow a negative, zero, overflowed, or under-provisioned structural budget to reach acquisition runtime.

### 19.18. V2 Autotuning Boundary

V2 may replace the deterministic bootstrap resolver with a platform-aware policy resolver.

Potential resolver strategies include:

``````text
cost-based policy search
Pareto-frontier specialization
e-graph / equality-saturation style plan selection
``````

However, V2 must still produce one immutable resolved policy snapshot before acquisition admission.

Already-running acquisition must not be retuned in place.

The frozen acquisition core must remain policy-snapshot-driven, not environment-driven.

### 19.19. Capacity Exhaustion Law

Capacity exhaustion is not a soft warning.

It is a fail-closed acquisition outcome.

The failure must include:

- image or acquisition ID;
- backend ID when relevant;
- phase;
- capacity field;
- consumed value;
- limit;
- compact material summary.

Diagnostic summary construction itself must obey diagnostic byte capacity.

If ordinary diagnostic capacity is exhausted, capacity-exhaustion reporting may use `emergencyDiagnosticBytesCap`.

Emergency diagnostics must remain fixed-shape, bounded, deterministic, and non-recursive.

### 19.20. FrozenAcquisitionRemainingBudget

The resolved policy snapshot owns the budget/cap law.

The acquisition run context carries only run-scoped remaining execution and governance budget across fresh restarts.

Illustrative shape:

``````kotlin
internal class FrozenAcquisitionRemainingBudget private constructor(
    val remainingAcquisitionSteps: Int,
    val remainingBackendWorkUnits: Int,
    val remainingCoverageWorkUnits: Int,
    val remainingLoweringWorkUnits: Int,
    val remainingReadinessWaits: Int,
    val remainingRestartAdmissions: Int,
)
``````

Rules:

- the run context must not own or reinterpret the full runtime policy snapshot;
- byte caps, table caps, sizing calibration, and watchdog policy remain in the pinned resolved policy snapshot;
- remaining budget is carry-forward acquisition material;
- remaining budget is not semantic identity material;
- fresh restart must carry the remaining deterministic budget forward;
- elapsed wall-clock must not replenish, reduce, or reinterpret remaining deterministic budget.

## 20. FrozenAcquisitionCapacityLedger

The ledger records consumption against resolved acquisition caps.

It is both:

``````text
real-time fail-fast capacity enforcement authority
terminal diagnostic / post-mortem evidence material
``````

It is not:

- adaptive live policy;
- semantic identity material;
- frozen image identity material;
- cache key material;
- PlanCacheKey material;
- L2 route/key material;
- planning decision material.

The ledger records semantic acquisition work, frozen coverage work, lowering work, deterministic physical work, backend
work units, frontier work units,
readiness/restart governance consumption, fixed-width structural bytes, variable material bytes, normal diagnostic
bytes,
and emergency diagnostic bytes.

Illustrative operations:

``````kotlin
consumeAcquisitionStep(phase, reference)
consumeBackendWorkUnit(phase, reference)
consumeCoverageWorkUnit(label, units)
consumeLoweringWorkUnit(label, units)
consumeClosureCompletenessValidation(units)
consumeLoweringCompletenessValidation(units)

consumeTypeReferenceClosureEntry(reference)
consumeRunnableTypeReference(reference)
consumeReferenceRequirement(owner, fieldName, target)

consumeShapeResolution(reference)
consumeCycleIdentityResolution(reference)
consumeRawFactResolution(reference)

consumeConstructorRecord(owner, constructorIndex)
consumeConstructorParameter(owner, constructorIndex, parameterIndex)
consumePropertyRecord(owner, propertyIndex)
consumeAnnotationRecord(owner, annotationIndex)

consumeShapeTableRow(reference)
consumeCycleIdentityTableRow(reference)
consumeRawFactTableRow(reference)

consumeFrontierLookupWorkUnits(comparedKeys)
consumeFrontierMovementWorkUnits(shiftedSlots)
consumeFrontierPageSplitWorkUnits(movedSlots)
consumeTypeReferenceStructuralVerification(candidateCount)

consumeReadinessWait(reference, reasonCode)
consumeRestartAdmission(descriptorId)
consumeRestartControlBytes(label, byteCount)

consumeCanonicalTextBytes(label, byteCount)
consumeTypeSignatureBytes(reference, byteCount)
consumeConstructorSignatureBytes(reference, byteCount)
consumePropertyNameBytes(reference, byteCount)
consumeAnnotationPayloadBytes(reference, byteCount)
consumeRawFactMaterialBytes(label, byteCount)

consumeStagingBytes(label, byteCount)
consumeSortBufferBytes(label, byteCount)
consumeDiagnosticSummaryBytes(label, byteCount)
consumeEmergencyDiagnosticBytes(label, byteCount)
``````

`consumeAcquisitionStep` replaces traversal-shaped naming in normative API examples.

Frozen acquisition computes TypeReference closure and table coverage.

It does not construct a planning traversal tree.

Therefore, ledger vocabulary should prefer acquisition, reference material, table row, record, slot, and coverage terms.

Capacity failure must include:

- image or acquisition ID;
- backend ID when relevant;
- phase;
- budget field;
- consumed value;
- limit;
- compact material summary.

Normal diagnostic summary construction must obey diagnostic byte capacity.

Capacity-exhaustion diagnostic construction may use emergency diagnostic reserve.

Emergency diagnostics must be fixed-shape, bounded, deterministic, and non-recursive.

### 20.1. Byte-Length Accounting Law

Byte budget is mandatory.

However, byte-budget enforcement must not repeatedly re-encode the same strings in acquisition loops.

Byte length must be computed once at canonicalization or lowering boundaries.

Illustrative value object shape:

``````kotlin
class CanonicalProtocolText private constructor(
    val value: String,
    val utf8ByteLength: Int,
)
``````

Illustrative signature shape:

``````kotlin
class CanonicalConstructorSignature private constructor(
    val text: String,
    val utf8ByteLength: Int,
)
``````

Forbidden pattern:

``````kotlin
ledger.consumeCanonicalTextBytes(
    label = "typeSignature",
    byteCount = text.toByteArray(Charsets.UTF_8).size,
)
``````

Allowed pattern:

``````kotlin
val canonical = CanonicalProtocolText.issue(rawText)
ledger.consumeCanonicalTextBytes(
    label = "typeSignature",
    byteCount = canonical.utf8ByteLength.toLong(),
)
``````

Canonical value objects and frozen key material should carry their measured byte length when the value is issued.

The ledger accumulates already-computed byte lengths.

This preserves byte-budget safety without making metering more expensive than acquisition itself.

### 20.2. Physical Work Accounting Law

No deterministic acquisition work is free.

Frontier lookup, frontier movement, page splitting, and structural equality verification must be charged as
deterministic
physical work.

This closes the gap where an input graph can consume little memory but burn unbounded CPU through comparator-heavy or
movement-heavy frontier behavior.

The ledger must account for at least:

- membership comparison work;
- page-local shift work;
- smoke `ArrayList` insertion shift work if a smoke implementation remains;
- deterministic page split work;
- duplicate verification scans;
- TypeReference structural equality verification attempts.

This cost is deterministic work-unit consumption.

It must not be inferred from elapsed wall-clock time.

Runtime watchdog timeout may abort at an orchestration boundary.

It must not reclassify deterministic frontier movement cost as backend starvation.

### 20.3. Coverage and Lowering Work Accounting Law

Frozen acquisition must meter frozen image completeness work separately from planning semantic expansion work.

The ledger must account for:

- reference requirement registration work;
- closure completeness validation work;
- table coverage validation work;
- lowering completeness validation work;
- backend-erased shape lowering work;
- backend-erased cycle identity lowering work;
- backend-erased raw fact lowering work.

Illustrative operations:

``````kotlin
consumeCoverageWorkUnit(label, units)
consumeLoweringWorkUnit(label, units)
consumeClosureCompletenessValidation(units)
consumeLoweringCompletenessValidation(units)
``````

These counters are frozen acquisition boundedness material.

They are not planning semantic expansion counters.

### 20.4. Structural Slot Charging Law

Table-row consumption must charge fixed-width structural storage.

Material-byte consumption charges variable-width payload storage.

These are separate accounting dimensions.

A table row, offset slot, length slot, coverage bit, ordinal slot, or membership slot must not be treated as free merely
because the variable payload bytes were already charged.

Examples:

``````text
consumeShapeTableRow(reference):
    charges fixed-width shape table row storage.

consumeCycleIdentityTableRow(reference):
    charges fixed-width cycle identity table row storage.

consumeRawFactTableRow(reference):
    charges fixed-width raw fact table row storage, including table-row header responsibility where applicable.

consumeRawFactMaterialBytes(label, byteCount):
    charges variable raw fact payload bytes only.
``````

Offset, length, coverage, ordinal, and index slot costs may be charged through dedicated operations or through the
corresponding row/record operations, but they must be represented exactly once in the capacity model.

They must also be represented in `TotalFrozenStructBytes(...)` during capacity resolution.

### 20.5. Readiness and Restart Governance Accounting Law

Readiness and restart governance are capacity-governed operations.

The ledger must record:

- readiness waits;
- readiness waits per reference/work item where the active policy requires it;
- restart admissions;
- restart-control byte consumption;
- stale-signal filtering cost where the implementation models it as deterministic work.

Illustrative operations:

``````kotlin
consumeReadinessWait(reference, reasonCode)
consumeRestartAdmission(descriptorId)
consumeRestartControlBytes(label, byteCount)
``````

Readiness wait accounting supports policies such as:

- maximum readiness waits per run;
- maximum readiness waits per reference/work item;
- maximum restart admissions per run;
- maximum consecutive readiness waits on the same reference/work item.

These counters are acquisition governance material.

They are not planning semantic material.

### 20.6. Recursive Diagnostic Failure Law

A diagnostic failure must not recursively trigger unbounded diagnostic work.

If normal diagnostic rendering exceeds `diagnosticBytesCap`, the engine must switch to emergency diagnostic mode.

Emergency diagnostic mode:

- uses `emergencyDiagnosticBytesCap`;
- emits a fixed-shape message;
- truncates material labels deterministically;
- avoids recursive material rendering;
- never attempts to build a second detailed diagnostic about the first diagnostic failure.

If emergency diagnostic capacity is also unavailable, the engine emits a fixed static fallback reason code.

The fallback reason code is still a terminal diagnostic.

It must not be silent.

The static fallback path must not attempt unbounded rendering and must not recursively call normal diagnostic rendering.

### 20.7. Ledger Snapshot Law

The live ledger is the fail-fast enforcement authority for the active acquisition run.

At terminal state, the implementation may emit an immutable ledger snapshot for diagnostics, benchmarks, regression
analysis, and future policy evaluation.

Illustrative shape:

``````kotlin
internal class FrozenAcquisitionLedgerSnapshot private constructor(
    val acquisitionId: FrozenAcquisitionId,
    val backendId: String,
    val runEpoch: FrozenAcquisitionRunEpoch,
    val policyEpochId: Long,
    val terminalOutcome: FrozenAcquisitionTerminalOutcome,
    val consumed: FrozenAcquisitionConsumedCounters,
    val limits: ResolvedFrozenAcquisitionCaps,
    val compactFailureReason: String?,
)
``````

A ledger snapshot may be used for:

- failure diagnostics;
- post-mortem analysis;
- benchmark comparison;
- capacity solver tuning input for future policy epochs;
- V2 autotuning telemetry candidates;
- regression detection.

A ledger snapshot must not:

- retune the active acquisition;
- change already-admitted capacity;
- affect semantic output;
- become TypeReference identity material;
- become frozen image identity material;
- become PlanCacheKey material;
- become L2 key or route material;
- become planning decision material.

## 21. Freeze Memory Discipline

Freeze is both:

1. a semantic erasure boundary;
2. a memory ownership transition boundary.

A compliant implementation must minimize the two-world overlap between backend-native acquisition state and frozen
adapter-neutral material.

### 21.1. Pre-count Before Allocation

The acquisition engine should know enough counts to pre-size frozen structures.

Forbidden default:

``````text
repeatedly grow ArrayList / HashMap / equivalent dynamic structures during freeze
``````

Preferred:

``````text
count records
-> allocate exact or bounded-capacity frozen storage
-> fill once
``````

### 21.2. Single-Pass Copy Law

Repeated defensive copies inside traversal loops are forbidden.

Copies are allowed only at:

- input snapshot boundaries;
- ownership transfer boundaries;
- publication boundaries;
- failure-safe diagnostic isolation boundaries.

### 21.3. Closure-Free Frozen Material

Frozen tables and records must not store:

- lambdas;
- suppliers;
- lazy delegates;
- service locators;
- callbacks;
- closures capturing backend handles;
- registry keys that can recover backend handles.

### 21.4. Arena Lifetime Law

A published frozen image must not retain:

- mutable acquisition arena;
- mutable builder;
- source slot;
- backend handle;
- backend registry;
- backend resolver;
- backend session;
- closure-backed accessor.

### 21.5. Readiness Fresh Restart Memory Law

When acquisition returns `AwaitingReadiness`, the current mutable episode must not remain open.

The engine must:

``````text
seal/release/poison current arena as appropriate
retain only immutable restart descriptors
retain only bridge-owned readiness handle material
reject stale backend-session generation signals
``````

The following are forbidden as readiness-wait ownership:

- open mutable arena;
- worker-local mutable frontier owner;
- half-filled mutable slab builder;
- backend-native handle;
- coroutine continuation;
- callback closure with backend-native reachability.

### 21.6. Spilling Non-Goal

Level 1.5 does not spill partially frozen semantic state to disk.

Budget exhaustion fails closed.

Out-of-core spilling is not part of ADR-0040.

## 22. Publication Law

Frozen publication may begin only when:

- no `PendingBackendWork` remains;
- no `DeferredToBackendProgress` remains;
- no failed node remains;
- no unsafe rejected material is required for completeness;
- type index is deterministic;
- shape table coverage is explicit;
- cycle identity table coverage is explicit;
- raw fact table coverage is explicit;
- closure completeness has been validated;
- lowering completeness has been validated.

Publication order:

``````text
1. deterministic type index issue
2. closure completeness validation
3. deterministic table alignment
4. coverage validation
5. lowering completeness validation
6. cross-table semantic validation
7. backend-handle erasure verification
8. publication of FrozenMetamodelImage
9. construction of planning-facing providers from image only
``````

Planning-facing providers receive:

``````text
FrozenMetamodelImage
``````

They must not receive:

``````text
FrozenMetamodelImageEnvelope
FrozenMetamodelImageDiagnosticHeader
SourceAdapterProvenance
Reflection registry
KSP resolver
bytecode parser
compiler IR graph
``````

## 22A. Lowering Completeness Law

Backend erasure is not semantic compression.

Removing backend-native handles must not silently remove planning-relevant information.

Every backend fact that can affect planning, expansion, member selection, cycle identity, raw fact availability,
constructor/property/annotation visibility, or diagnostic integrity must be handled in one of three ways:

``````text
1. lowered into adapter-neutral frozen material;
2. represented as explicit unavailable / unsupported / capability material;
3. rejected fail-closed before publication.
``````

Silent semantic loss is forbidden.

Examples:

``````text
Backend-specific visibility nuance:
    normalize into Kontrakt canonical visibility protocol;
    or emit explicit unsupported capability material;
    or fail closed if planning correctness depends on it.

KSP-only source declaration information:
    lower into frozen source/declaration availability material;
    or mark explicitly unavailable for non-source backends;
    or fail closed if required by the active policy.

Reflection-only runtime handle detail:
    may be ignored only if it is proven non-planning-relevant;
    otherwise it must be lowered, represented, or rejected.
``````

The image validator or a dedicated lowering completeness validator must be able to verify:

- required fact coverage;
- backend capability vs lowered fact coverage;
- explicit unsupported feature markers;
- unsafe feature rejection;
- no silent truncation of planning-relevant material.

This law belongs to ADR-0040 and ADR-0039's erasure boundary.

It is not deferred to HID, BLAKE3, interning, or physical slab lowering.

## 22B. Planning Traversal Optimization Boundary

Frozen acquisition does not eliminate planning traversal.

It changes the cost profile of planning traversal.

Planning remains responsible for semantic expansion, but it should not repeat frozen acquisition's expensive work.

A compliant frozen image should allow planning to use:

``````text
TypeReference
-> FrozenTypeReferenceIndex.ordinalOf(reference)
-> frozen ordinal
-> ordinal-addressed table reads
``````

Future planning optimization should prefer:

``````text
visited set:
    BooleanArray / BitSet / epoch-marked IntArray by frozen ordinal

active stack:
    IntArray of frozen ordinals or stable intern ids

shape lookup:
    shapeTable.findAt(frozenOrdinal)

cycle identity lookup:
    cycleIdentityTable.findAt(frozenOrdinal)

raw fact lookup:
    rawFactTable.findAt(frozenOrdinal)
``````

This keeps the freeze barrier while avoiding backend-heavy duplicate work.

The target is:

``````text
first pass:
    expensive backend metadata closure acquisition

second pass:
    cheap semantic expansion over dense frozen tables
``````

This section does not mandate planning implementation changes.

It records the optimization contract that ADR-0041 and later planning ADR/design amendments should use.

## 23. Panic / Isolation Law

The following are integrity-class failures:

- comparator-equal but structurally unequal `TypeReference` in frontier;
- backend handle detected inside frozen payload;
- planning-facing provider receives diagnostic/provenance envelope;
- publication validation observes missing required coverage;
- `READY_TO_RESTART` signal attempts to reopen terminal acquisition run;
- stale backend session generation attempts to restart current run;
- backend completion order affects frozen ordinal assignment;
- published image retains mutable acquisition arena.

A compliant implementation must:

- abort the current acquisition run;
- prevent publication of partial image material;
- isolate contaminated acquisition arena;
- reject stale restart handles;
- propagate a panic-grade diagnostic.

## 24. Interaction with ADR-0039

ADR-0039 defines the frozen image and backend-handle erasure direction.

This ADR defines the acquisition engine that produces such images.

ADR-0039 says:

``````text
backend handle
-> acquisition/lowering
-> frozen adapter-neutral metamodel image
-> planning-facing providers
``````

ADR-0040 specifies how the acquisition/lowering stage is performed:

``````text
backend root / snapshot / index
-> backend-neutral acquisition result algebra
-> deterministic TypeReference closure frontier
-> explicit lifecycle state
-> restart-aware acquisition
-> budgeted, byte-budgeted, and lowering-complete materialization
-> memory-disciplined publication
``````

ADR-0040 does not weaken ADR-0039's erasure law.

Frozen acquisition core must remain backend-neutral.

## 25. Interaction with ADR-0034, ADR-0035, and ADR-0036

ADR-0040 imports the quality bar of planning/L2 async architecture:

- explicit state axes;
- single lifecycle authority;
- event source vs semantic authority separation;
- one-shot readiness publication;
- stale generation defense;
- no synchronous waiting worker ownership;
- bounded mutable state;
- fresh restart through an explicit authority.

ADR-0040 does not import L2-specific mechanics:

- shared slot state;
- waiter state;
- builder handle state;
- L2 route64 delivery;
- joined-wait callback delivery;
- deadline heap;
- dirty-shard replay.

Frozen acquisition needs its own state machine because its risk model is different.

Planning L2 async protects joined wait / cache miss coordination.

Frozen acquisition protects frozen image publication, backend erasure, acquisition memory, and table coverage.

ADR-0040 also follows the policy epoch distinction used by the planning/L2 runtime model.

The shared `RuntimePolicyEpoch` is reused as a policy-governance axis.

It is not reused as an acquisition lifecycle axis.

Therefore:

``````text
RuntimePolicyEpoch:
    what policy snapshot governs this acquisition

FrozenAcquisitionRunEpoch:
    which logical acquisition run is being restarted/replayed

FrozenAcquisitionBackingEpoch:
    which mutable scratch/arena backing episode is safe

FrozenBackendSessionGeneration:
    which backend session/resolver/index episode produced a signal
``````

A fresh restart may create a fresh acquisition episode or fresh mutable backing.

It must not pick a new runtime policy epoch.

## 26. Interaction with ADR-0037

ADR-0037's fact-lazy planning rule remains mandatory.

Planning order remains:

``````text
shape
-> cycle identity
-> active-cycle detection
-> raw facts only after cycle miss
``````

ADR-0040 affects where facts come from.

Before migration:

``````text
RawTypeFactsProvider
-> backend handle registry
-> KType / KSP symbol
-> facts
``````

Target:

``````text
RawTypeFactsProvider
-> FrozenMetamodelImage
-> FrozenRawFactRecord
-> facts
``````

Cycle-hit path still avoids raw fact materialization for the current type.

## 27. Interaction with ADR-0041

ADR-0041 will own:

- BLAKE3;
- HID;
- stable TypeReference fingerprint;
- protocol-owned interning;
- canonical byte encoding;
- collision verification;
- golden vectors;
- frozen consumer law;
- planning consumer law;
- L2 consumer law.

ADR-0040 only preserves insertion points for that work.

ADR-0040 does not define final digest bytes, final HID format, final route64 derivation, or final interning protocol.

## 28. Interaction with ADR-0042

ADR-0042 or a later storage-governance ADR will own:

- S3-FIFO;
- SIEVE;
- L2 retention;
- admission;
- eviction;
- approximate byte-weighted retention;
- cache-blind semantic preservation.

ADR-0040 does not choose a cache eviction algorithm.

Frozen acquisition identity and L2 retention must remain separate responsibilities.

## 28A. Interaction with Shared Runtime Policy Snapshot

ADR-0040 requires `RuntimePolicyEpoch` to become shared runtime/governance policy material.

The current planning-local package placement is transitional.

Target direction:

``````text
planning.infrastructure.runtime.policy.RuntimePolicyEpoch
    -> runtime.domain.policy.RuntimePolicyEpoch
``````

or:

``````text
planning.infrastructure.runtime.policy.RuntimePolicyEpoch
    -> governance.domain.policy.RuntimePolicyEpoch
``````

The final package name may be decided by a runtime/governance policy ADR or ADR-0033 amendment.

However, ADR-0040 requires the following architectural outcome:

``````text
RuntimePolicyEpoch is no longer planning infrastructure.
RuntimePolicyEpoch is shared policy governance.
RuntimePolicyEpoch points to a resolved runtime policy snapshot.
The snapshot is split into bounded-context policy slices.
``````

Frozen acquisition may pin the shared epoch.

Frozen acquisition may consume the frozen acquisition policy slice.

Frozen acquisition may not import planning infrastructure to access runtime policy.

Frozen acquisition may not depend on planning-only policy payloads.

Planning may continue to use the same shared epoch.

Planning may consume planning policy slices.

L2 may consume L2 join, dispatch, and storage policy slices.

This preserves one policy governance timeline without merging bounded-context lifecycles.

## 29. Tradeoff Analysis

### 29.1. Option A: Reflection-Specific Collector

Shape:

``````text
Reflection roots
-> reflection collector
-> frozen image
``````

Benefits:

- easiest near-term implementation;
- minimal abstraction;
- direct access to current reflection bundle;
- less code.

Costs:

- traversal law becomes reflection-specific;
- KSP/bytecode/compiler-static repeat the same problem;
- budget and memory discipline become adapter-local;
- backend-specific ordering can leak;
- future query/incremental model becomes harder.

Decision:

Rejected as target architecture.

Reflection may have a thin facade over backend-neutral acquisition.

### 29.2. Option B: Coroutine-Suspending Acquisition Port

Shape:

``````kotlin
suspend fun resolveTypeShape()
``````

Benefits:

- familiar Kotlin async surface;
- easy to integrate with coroutine-capable adapters;
- fewer custom lifecycle classes initially.

Costs:

- hides continuation ownership;
- weakens fresh restart law;
- confuses pending work with backend deferral;
- makes budget step accounting less explicit;
- allows dispatcher timing to leak into control flow;
- conflicts with Kontrakt's planning/L2 no-coroutine-semantic-control model.

Decision:

Rejected.

Adapter internals may use operational async mechanisms, but the acquisition core boundary must use explicit step results
and readiness callbacks.

### 29.3. Option C: Synchronous Waiting Backend Calls

Benefits:

- simplest control flow;
- no readiness handle;
- no fresh restart model.

Costs:

- monopolizes worker resources;
- risks pool starvation;
- conflicts with Kontrakt's non-synchronous waiting planning direction;
- prevents later deterministic parallel acquisition;
- does not handle KSP-like backend progress boundaries cleanly.

Decision:

Rejected.

### 29.4. Option D: Retained Node / Retained Worker Waiting Model

Benefits:

- superficially resembles scheduler-managed dependency waiting;
- can be easier to explain as runnable vs non-runnable nodes.

Costs:

- the terminology conflicts with Kontrakt's non-synchronous waiting architecture;
- it can imply worker, node, or mutable arena retention;
- it can hide callback ownership and restart authority;
- it can regress toward already-rejected retained-worker waiting models.

Decision:

Rejected as ADR vocabulary and semantic model.

ADR-0040 uses readiness callback bridge and explicit restart descriptor instead.

### 29.5. Option E: Callback-Driven Readiness Bridge and Fresh Restart

Benefits:

- matches Kontrakt's existing planning/L2 quality bar without copying L2 semantics;
- keeps execution control inside the engine;
- avoids hidden continuation ownership;
- supports stale signal defense;
- releases worker and arena ownership while backend work is pending;
- keeps completion order non-semantic;
- avoids root replay as the normal path;
- remains compatible with future query/incremental acquisition.

Costs:

- more state types;
- more lifecycle tests;
- explicit restart descriptors must be designed carefully;
- bridge/fresh restart machinery required.

Decision:

Accepted for Level 1.5.

### 29.6. Option F: Full Query / Incremental Engine Now

Benefits:

- aligns with future compiler-static and query plans;
- can support node-level early cutoff;
- can reduce fresh restart replay cost further.

Costs:

- too large for current frozen foundation;
- requires HID/fingerprint/golden vectors first;
- risks mixing ADR-0040 with ADR-0041 and v2 query design;
- expands scope beyond immediate freeze correctness.

Decision:

Deferred.

ADR-0040 remains query-compatible but does not implement a query engine.

## 30. Accepted Architecture

The accepted Level 1.5 architecture is:

``````text
backend-model-neutral
no coroutine semantic suspension
callback-driven readiness bridge
explicit state machine
logical pending / deferral algebra
explicit restart descriptor
fresh acquisition restart
deterministic TypeReference closure frontier
count-budgeted
byte-budgeted
coverage-work-budgeted
lowering-work-budgeted
byte-length-accounted at canonicalization boundaries
memory-disciplined
lowering-complete
publication-validated
backend-erased
``````

The accepted architecture also includes shared policy-governance pinning:

``````text
shared RuntimePolicyEpoch
  -> resolved runtime policy snapshot
  -> bounded-context policy slices
      -> planning policy
      -> frozen acquisition policy
      -> L2 join policy
      -> L2 storage policy
      -> dispatch policy
``````

This does not merge runtime lifecycles.

It only establishes one immutable policy snapshot line that all admitted runs and acquisition episodes can pin.

This is the foundation for future:

``````text
HID / BLAKE3 / interning
deterministic primitive membership
two-pass acquisition
direct-to-slab lowering
deterministic parallel acquisition
query/incremental compiler-static acquisition
``````

## 31. Non-Goals

This ADR does not define:

- canonical byte encoding;
- BLAKE3 derivation;
- HID;
- route64;
- PlanCacheKey;
- protocol-owned interning;
- L2 retention/admission/eviction;
- S3-FIFO;
- SIEVE;
- persistent frozen image binary format;
- full query engine;
- red/green incremental invalidation;
- dynamic incremental cycle detection;
- source-level incremental watch protocol;
- database-style out-of-core spilling;
- off-heap frozen table storage;
- final primitive `LongArray` / `IntArray` table implementation;
- full parallel acquisition lane implementation.

## 32. Compliance Rules

A compliant implementation must satisfy:

1. Frozen acquisition core is backend-model-neutral.
1. Reflection, KSP, compiler, bytecode, and source APIs do not appear in acquisition core.
1. Frozen acquisition core does not use Kotlin coroutine suspension as semantic control.
1. Backend operations return `FrozenAcquisitionStepResult`.
1. `PendingBackendWork` and `DeferredToBackendProgress` are distinct.
1. Backend completion order does not define semantic order.
1. Backend readiness is delivered through `FrozenAcquisitionReadinessBridge`.
1. Readiness callback publication is one-shot.
1. Fresh restart is admitted only through acquisition authority.
1. Fresh restart preserves `FrozenAcquisitionRunEpoch`.
1. Fresh restart preserves pinned `RuntimePolicyEpoch`.
1. Stale backend session generation cannot reopen a terminal run.
1. Frontier membership is deterministic and explicit.
1. Frontier authority does not use `hashCode`, `HashSet`, `HashMap`, or backend handle identity.
1. Level 1.5 target frontier is `DeterministicPagedTypeReferenceIndex` or a stricter deterministic exact structure.
1. `ArrayList` sorted membership is allowed only as smoke implementation or migration debt.
1. Frozen acquisition uses byte/resource envelope based capacity resolution.
1. Concrete count/table/queue caps are derived solver outputs, not primary policy inputs.
1. Record-count caps are not configured directly.
1. Byte length is computed once at canonicalization/lowering boundaries where possible.
1. Sort-buffer capacity is derived by the capacity solver and included in the frozen byte envelope.
1. Freeze-time canonical text dedup/scratch table capacity is derived by the capacity solver and must not be treated as
   ADR-0041 protocol interning.
1. Re-entry descriptor capacity is forbidden; fresh-restart control material is bounded by `restartControlBytesCap`.
1. Fixed record/header/offset/length/coverage overhead is included in the capacity feasibility calculation.
1. Text/material reserves are computed before structural caps are derived.
1. Capacity exhaustion fails closed.
1. Backend readiness starvation limits are enforced by resolved policy.
1. Frozen image publication requires complete table coverage.
1. Frozen image publication validates lowering completeness before provider exposure.
1. Published image contains no backend-native handles.
1. Published image contains no closure-backed backend reachability.
1. Planning-facing providers receive `FrozenMetamodelImage` only.
1. Diagnostic provenance stays outside planning-visible provider paths.
1. Mutable acquisition arena does not survive into published image.
1. Open mutable arena does not survive while external readiness is outstanding.
1. `FrozenAcquisitionArenaState` transitions are enforced.
1. `FrozenAcquisitionNodeState` transitions are enforced.
1. `FrozenAcquisitionPublicationState` transitions are enforced.
1. Backend erasure is not semantic compression.
1. Planning-relevant backend facts are lowered, explicitly unavailable/unsupported, or rejected fail-closed.
1. Future physical lowering must preserve this ADR's semantic laws.
1. `RuntimePolicyEpoch` must be promoted out of `planning.infrastructure` before frozen acquisition domain code imports
   it.
1. Frozen acquisition domain code must not import `planning.infrastructure.runtime.policy.RuntimePolicyEpoch`.
1. Frozen acquisition must pin a shared runtime policy epoch or an adapter-neutral epoch identity supplied by the
   orchestration boundary.
1. Frozen acquisition must release its runtime policy epoch pin at terminal state.
1. Frozen acquisition must consume only the frozen acquisition policy slice from the resolved runtime policy snapshot.
1. Planning policy, L2 storage policy, and dispatch policy must not be consumed by frozen acquisition unless explicitly
   allowed by a later ADR.
1. `RuntimePolicyEpoch` must not be used as `FrozenAcquisitionRunEpoch`.
1. `RuntimePolicyEpoch` must not be used as `FrozenAcquisitionBackingEpoch`.
1. `RuntimePolicyEpoch` must not be used as `FrozenBackendSessionGeneration`.
1. A newly published `RuntimePolicyEpoch` may affect future acquisition runs only, not an already admitted acquisition
   run.
1. Terms that imply retained worker ownership, retained node execution, or coroutine suspension must not be used as
   normative frozen acquisition semantics.
1. Frozen acquisition must not construct a planning traversal tree.
1. Frozen acquisition must not perform active-member projection or ordering.
1. Frozen acquisition must not decide cycle-truncated planning branches.
1. Frozen acquisition must not import planning traversal topology semantics.
1. Frozen acquisition readiness bridge must remain frozen-local.
1. ADR-0040 must not introduce a shared graph engine, shared frontier, shared budget ledger, or shared readiness bridge
   between frozen acquisition and planning.
1. Registered shape/raw-fact reference requirements are closure requirements, not planning children.
1. Planning traversal optimization must use frozen ordinal/table material rather than frozen acquisition traversal
   state.
1. Capability overclaim detected during acquisition is an integrity failure.
1. Diagnostic evidence capability must influence diagnostics only, not frozen identity or planning semantics.
1. Backend concurrency capability must influence scheduling only, not semantic output.
1. A `SINGLE_THREAD_CONFINED`, `SESSION_THREAD_CONFINED`, or `LANE_CONFINED` backend must be invoked through its
   required deterministic owner.
1. Backend identity stability must not be used as TypeReference equality, frozen key equality, HID replacement, route64
   material, PlanCacheKey material, or intern-id authority.
1. `IMAGE_PERSISTENT` backend identity still requires ADR-0041 canonical verification before it may participate in
   semantic identity.
1. Batch result order must not affect TypeReference closure membership, frozen ordinal assignment, table layout,
   diagnostics classification, publication, or image identity.
1. Unordered batch results must be deterministically merged before they affect frozen acquisition state.
1. `supportsDirectOrdinalMaterialization` must not remain an independent final capability boolean; direct behavior must
   be represented through payload or batching model.
1. Capability is pinned backend-session/acquisition material and must not drift during one acquisition episode.
1. Acquisition phase and node-state vocabulary must use reference material, table row, record, slot, or coverage
   material terminology instead of ambiguous publication-oriented material names.
1. Reserved non-structural bytes greater than or equal to `maxFrozenBytesPerAcquisition` must fail closed before
   acquisition execution.
1. `structBudgetBytes` must be positive and at least `minRequiredStructuralBytes` before acquisition execution.
1. The capacity solver must prove `TotalFrozenStructBytes(targetCaps) <= structBudgetBytes`.
1. Material-index overhead must be part of structural feasibility, not a vague payload-size estimate.
1. Canonical text offset/index slots and length slots must be counted in structural feasibility.
1. Raw fact material offset/index slots and length slots must be counted in structural feasibility.
1. Alignment and padding bytes must be included in deterministic capacity feasibility calculations.
1. Emergency diagnostic reserve must be included in the non-structural reserve calculation and must be separate from
   ordinary diagnostic capacity.

1. Frontier lookup, movement, page split, and structural verification work units must be charged by the ledger.
1. Smoke `ArrayList` insertion shifts must be charged if the smoke implementation remains.
1. Readiness waits and restart admissions must be ledger-accounted and policy-governed.
1. Restart-control bytes must be ledger-accounted separately from ordinary material bytes.
1. Table rows, offset slots, length slots, coverage bits, ordinal slots, and membership slots must not be treated as
   free
   when payload bytes are charged.
1. Emergency diagnostic bytes must be accounted separately from normal diagnostic summary bytes.
1. Normal diagnostic budget exhaustion must not recursively trigger unbounded diagnostic rendering.
1. Static fallback diagnostics must be fixed-shape, bounded, and terminal.
1. Ledger snapshots are post-mortem evidence and must not retune the active acquisition or become semantic identity,
   cache key, or planning decision material.
1. Runtime watchdog policy is outer orchestration material only, not a bounded-context semantic policy slice.
1. Runtime watchdog policy must not be consumed by capacity solver, frontier membership, frozen ordering, ordinal
   assignment, image identity, or restart boundedness.
1. Frozen acquisition must advance bounded episodes and must not be specified as an unbounded synchronous loop.
1. `advanceEpisode` is the normative engine/strategy method name for bounded episode progress.
1. Closure completeness must be validated before publication.
1. Coverage work units and lowering work units must be policy-governed and ledger-accounted.
1. Fresh restart must carry forward `FrozenAcquisitionRemainingBudget`.
1. TypeReference structural verification must use backend-erased canonical verification material, not backend handle
   identity.
1. Level 1.5 comparator/equality membership is transitional; final membership must use ADR-0041 verified intern ids or
   frozen ordinals after ratification.

## 33. Required Tests

Add tests for:

- acquisition core imports no reflection/KSP/compiler/bytecode APIs;
- reflection backend leaks no `KType` into frozen image;
- KSP backend leaks no `KSType` or `KSDeclaration` into frozen image;
- provider construction uses `FrozenMetamodelImage`, not diagnostic envelope;
- TypeReference closure order is independent from backend discovery order;
- duplicate `TypeReference` is detected deterministically;
- comparator-equal but structurally unequal references fail closed;
- frontier does not use `hashCode` as authority;
- `DeterministicPagedTypeReferenceIndex` lookup is deterministic;
- `DeterministicPagedTypeReferenceIndex` insertion does not depend on thread timing or backend order;
- `ArrayList` membership implementation is marked as smoke/migration debt if still present;
- type count budget exhaustion fails closed;
- reference requirement budget exhaustion fails closed;
- canonical text byte budget exhaustion fails closed;
- annotation payload byte budget exhaustion fails closed;
- diagnostic summary byte budget is honored;
- canonical byte lengths are measured once at canonicalization/lowering boundaries;
- ledger does not repeatedly re-encode canonical strings in traversal loops;
- sort-buffer reserve is included in frozen capacity resolution;
- canonical text dedup/scratch table cap is derived and not treated as protocol interner capacity;
- restart-control reserve is enforced without introducing re-entry descriptors;
- fixed record/header/offset/length/coverage overhead is included in capacity resolution;
- text/material reserves are subtracted before structural caps are derived;
- minimal frozen acquisition layout failure is detected before acquisition starts;
- `Available` progresses acquisition;
- `PendingBackendWork` transitions to `AWAITING_BACKEND_READY`;
- `DeferredToBackendProgress` does not enter readiness waiting;
- `Unavailable` is represented as explicit availability/diagnostic material;
- `RejectedUnsafe` prevents publication when required coverage depends on it;
- `Failed` aborts acquisition;
- backend readiness callback publishes `READY_TO_RESTART` exactly once;
- already-ready and asynchronously-ready paths converge to the same READY_TO_RESTART publication discipline;
- stale backend session generation cannot restart a terminal run;
- restart preserves `FrozenAcquisitionRunEpoch`;
- restart preserves pinned `RuntimePolicyEpoch`;
- fresh restart does not retain stale mutable arena;
- explicit restart descriptor contains no backend-native handles;
- root replay is not the normal readiness path;
- publication rejects pending material;
- publication rejects deferred material;
- publication rejects missing table coverage;
- publication rejects backend handle retention;
- publication rejects closure-backed frozen material;
- publication rejects silent lowering loss;
- published providers are derived from `FrozenMetamodelImage` only;
- failed acquisition releases or poisons mutable arena;
- repeated traversal-loop copies are absent;
- frozen image does not retain mutable builders.
- phase/node-state names use `COMMIT_REFERENCE_MATERIAL` and `REFERENCE_MATERIAL_COMMITTED` for staged material handoff.
- frozen acquisition does not construct planning traversal tree material.
- frozen acquisition does not expose active-member projected children.
- frozen acquisition does not expose selected-constructor traversal children.
- frozen acquisition does not expose eligible-property traversal children.
- registered shape reference requirements are distinguishable from planning children.
- registered raw-fact reference requirements are distinguishable from planning children.
- planning-facing providers can read frozen tables by ordinal without observing acquisition frontier state.
- frozen acquisition domain code does not depend on planning traversal implementation classes.
- frozen readiness bridge does not depend on L2 shared-slot, waiter, builder-handle, or join-handle types.

- capacity solver fails closed when reserve floors exceed or equal the frozen acquisition byte envelope;
- capacity solver fails closed when `structBudgetBytes` is below `minRequiredStructuralBytes`;
- capacity solver fails closed when `TotalFrozenStructBytes(targetCaps)` exceeds `structBudgetBytes`;
- canonical text offset/index slots are included in `TotalFrozenStructBytes(...)`;
- canonical text length slots are included in `TotalFrozenStructBytes(...)`;
- raw fact material offset/index slots are included in `TotalFrozenStructBytes(...)`;
- raw fact material length slots are included in `TotalFrozenStructBytes(...)`;
- material-index overhead is included even when payload bytes are small;
- alignment and padding are included in the deterministic feasibility calculation;
- emergency diagnostic reserve is included in the non-structural reserve calculation;
- capacity-exhaustion reporting uses fixed-shape emergency diagnostics when ordinary diagnostic capacity is exhausted;

- frontier lookup work units are charged.
- frontier movement / insertion shift work units are charged.
- frontier page split work units are charged.
- TypeReference structural verification attempts are charged.
- smoke `ArrayList` insertion shift cost is charged if the smoke implementation remains.
- readiness wait counters are charged per run and per reference/work item where policy requires it.
- restart admission counters are charged.
- restart-control bytes are charged.
- table-row consumption charges fixed-width structural storage.
- raw fact material byte consumption does not implicitly cover offset, length, coverage, ordinal, or membership slot
  bytes.
- offset, length, coverage, ordinal, and membership slot costs are represented exactly once.
- normal diagnostic budget exhaustion falls back to emergency diagnostic reserve.
- emergency diagnostic output is fixed-shape and non-recursive.
- emergency diagnostic fallback emits a terminal reason code instead of silently dying.
- ledger snapshot emission does not change active acquisition policy or semantic output.
- ledger snapshot material is not used in TypeReference identity, frozen image identity, PlanCacheKey, L2 route/key
  material,
  or planning decisions.
- runtime watchdog policy is not reachable from frozen acquisition capacity resolver APIs.
- runtime watchdog policy is not reachable from frontier membership, frozen ordering, or ordinal assignment APIs.
- elapsed wall-clock timeout does not change closure membership, diagnostics classification, or image identity.
- `advanceEpisode` returns at deterministic episode boundaries and does not retain worker ownership while readiness is
  outstanding.
- closure completeness validation rejects silently dropped reference requirements.
- closure completeness validation rejects pending, deferred, or unaccounted requirements at publication time.
- coverage work units are charged for reference requirement registration and table coverage validation.
- lowering work units are charged for backend-erased shape, cycle identity, and raw fact lowering.
- fresh restart carries forward `FrozenAcquisitionRemainingBudget`.
- remaining deterministic budget is not replenished or reinterpreted by elapsed wall-clock.
- comparator-equal TypeReferences are verified against backend-erased canonical verification material.
- backend handle identity, source location alone, classloader identity, and unverified fingerprint equality are rejected
  as TypeReference equality authorities.
- Level 1.5 comparator/equality membership is marked transitional.
- final membership bridge to ADR-0041 verified intern ids or frozen ordinals is preserved in documentation and package
  boundaries.

### Runtime policy epoch / policy slice tests

- `RuntimePolicyEpoch` is not located under `planning.infrastructure` after shared promotion.
- Frozen acquisition domain code does not import `planning.infrastructure.runtime.policy.RuntimePolicyEpoch`.
- Frozen acquisition run context preserves the same pinned `RuntimePolicyEpoch` across fresh restart.
- Frozen acquisition restart does not pick up a newly published runtime policy epoch.
- Frozen acquisition releases its runtime policy epoch pin at terminal state.
- Frozen acquisition consumes only the frozen acquisition policy slice.
- Planning-only policy fields are not reachable from frozen acquisition core APIs.
- L2-storage-only policy fields are not reachable from frozen acquisition core APIs.
- `RuntimePolicyEpoch` is not equal to `FrozenAcquisitionRunEpoch`.
- `RuntimePolicyEpoch` is not equal to `FrozenAcquisitionBackingEpoch`.
- `RuntimePolicyEpoch` is not equal to `FrozenBackendSessionGeneration`.
- Same epoch id with different policy snapshot payload fails policy integrity validation.
- Same epoch id with same policy snapshot payload is treated as benign duplicate delivery.

### Backend readiness starvation tests

- max readiness waits per run are enforced.
- max readiness waits per node are enforced.
- max restart admissions per run are enforced.
- max consecutive readiness waits on the same node are enforced.
- readiness deadline expiration fails closed.
- repeated pending is not silently converted to deferred unless backend capability and reason code justify it.
- random jitter is not used as semantic acquisition control.
- reflection capability does not claim source-range diagnostics unless a source-backed reflection extension exists.
- KSP capability may claim source-range diagnostics but must not expose `KSDeclaration` to frozen core.
- bytecode capability claims source line evidence only when reliable line table material is available.
- precomputed-index capability may claim precomputed diagnostic spans only when the manifest provides stable spans.
- a `SINGLE_THREAD_CONFINED` backend is invoked only through one deterministic owner.
- a `SESSION_THREAD_CONFINED` backend is invoked only through the owning backend session context.
- a `LANE_CONFINED` backend is invoked only through its assigned deterministic lane.
- a `CONCURRENT_READ_ONLY` backend produces the same frozen image under serial and parallel read scheduling.
- an unordered batch backend produces the same frozen image as ordered single-item acquisition.
- batch completion order does not affect TypeReference closure order.
- batch completion order does not affect frozen ordinal assignment.
- backend identity stability is not used as TypeReference equality.
- backend identity stability is not used as frozen key material.
- `IMAGE_PERSISTENT` backend identity still requires ADR-0041 canonical verification before becoming semantic identity.
- diagnostic evidence does not influence PlanCacheKey, route64, frozen key equality, or planning expansion decisions.
- a backend overclaiming capability is rejected or panic-isolated when detected.

## 34. Migration Plan

### 34.1. Phase 1: ADR Adoption

Adopt ADR-0040 and freeze the acquisition lifecycle vocabulary.

### 34.1A. Phase 1A: Shared Runtime Policy Epoch Promotion

Promote runtime policy epoch out of planning infrastructure.

Current transitional location:

``````text
planning.infrastructure.runtime.policy.RuntimePolicyEpoch
``````

Target location candidate:

``````text
runtime.domain.policy.RuntimePolicyEpoch
``````

or:

``````text
governance.domain.policy.RuntimePolicyEpoch
``````

Replace the planning-specific integrity exception:

``````kotlin
PlanningProtocolIntegrityException
``````

with a shared policy integrity exception, for example:

``````kotlin
PolicySnapshotIntegrityException
``````

or:

``````kotlin
RuntimePolicyIntegrityException
``````

Introduce or prepare a split resolved runtime policy snapshot:

``````kotlin
class ResolvedRuntimePolicySnapshot private constructor(
    val planning: ResolvedPlanningRuntimePolicy,
    val frozenAcquisition: ResolvedFrozenAcquisitionPolicy,
    val l2Join: ResolvedL2JoinPolicy,
    val l2Storage: ResolvedL2StoragePolicy,
    val dispatch: ResolvedDispatchLanePolicy,
    val runtimeWatchdog: ResolvedRuntimeWatchdogPolicy?,
)
``````

Existing `ResolvedRuntimePolicy` may remain as a transitional compatibility name only if it does not cause planning-only
policy payloads to become frozen acquisition dependencies.

### 34.1B. Phase 1B: Frozen / Planning Boundary Guard

Before implementing the acquisition strategy, add explicit architecture tests or package-boundary rules ensuring that
frozen acquisition cannot import planning traversal or L2 join mechanics.

The frozen acquisition implementation must remain under frozen/metamodel acquisition ownership.

Do not extract common graph primitives during ADR-0040 implementation.

Do not reuse planning frontier, planning budget ledger, PlanningRunJoinBridge, L2 waiter, L2 shared-slot, or L2
dispatch-plane types.

### 34.2. Phase 2: Acquisition Result Vocabulary

Introduce:

``````text
FrozenAcquisitionStepResult
FrozenAcquisitionDeferral
FrozenAcquisitionDeferralKind
FrozenMetamodelAcquisitionCapability
FrozenDiagnosticEvidenceModel
FrozenBackendConcurrencyModel
FrozenBackendIdentityStability
FrozenBackendBatchingModel
FrozenMetamodelAcquisitionBackend
``````

Capability migration must remove `supportsDirectOrdinalMaterialization` as an independent final boolean.

Direct behavior must be represented by `FrozenAcquisitionPayloadModel.DIRECT_SLAB_WRITER` or
`FrozenBackendBatchingModel.DIRECT_ORDINAL_MATERIALIZATION`.

### 34.3. Phase 3: Capacity Resolution and Frontier

Introduce:

``````text
ResolvedFrozenAcquisitionBudget
ResolvedFrozenSizingCalibration
FrozenAcquisitionCapacityResolver
ResolvedFrozenAcquisitionCaps
ResolvedFrozenClosureCaps
ResolvedFrozenTableCaps
ResolvedFrozenRecordCaps
ResolvedFrozenMemoryCaps
ResolvedFrozenReadinessCaps
FrozenAcquisitionCapacityLedger
FrozenTraversalPhase
DeterministicPagedTypeReferenceIndex
DeterministicTypeReferenceFrontier
``````

Record-count caps are capacity-solver outputs, not policy inputs.

Sort-buffer capacity, canonical text dedup/scratch table capacity, restart-control capacity, emergency diagnostic
reserve, fixed metadata overhead, material-index overhead, and deterministic alignment/padding must be represented in
the
capacity model.

The capacity resolver must expose or internally own a deterministic `TotalFrozenStructBytes(...)` layout calculation.

Keep `ArrayList` sorted membership only as smoke implementation or migration debt.

### 34.3A. Phase 3A: Ledger Physical Work and Snapshot Discipline

Extend `FrozenAcquisitionCapacityLedger` to account for:

``````text
frontier lookup work units
frontier movement / insertion shift work units
frontier page split work units
TypeReference structural verification attempts
readiness wait counts
restart admission counts
restart-control bytes
fixed-width table row / slot structural bytes
emergency diagnostic bytes
``````

Introduce immutable terminal ledger snapshots for post-mortem diagnostics and future policy evaluation.

Ledger snapshots must not retune the active acquisition and must not become semantic identity, cache key, L2 route/key,
or planning decision material.

### 34.3B. Phase 3B: Coverage and Lowering Work Budgets

Extend the frozen acquisition policy and ledger to include:

``````text
maxCoverageWorkUnits
maxLoweringWorkUnits
consumeCoverageWorkUnit
consumeLoweringWorkUnit
consumeClosureCompletenessValidation
consumeLoweringCompletenessValidation
FrozenAcquisitionRemainingBudget
``````

These counters govern frozen image completeness and backend-erased lowering work.

They must not be reused as planning semantic expansion counters.

### 34.3C. Phase 3C: Runtime Watchdog Isolation

Replace any transitional legacy wall-clock policy slice vocabulary with runtime-watchdog vocabulary.

The runtime watchdog must remain outer orchestration material.

It must not be consumed by frozen capacity resolution, frontier membership, ordering, ordinal assignment, image
identity, or restart boundedness.

### 34.4. Phase 4: Engine and Lifecycle

Introduce:

``````text
FrozenAcquisitionEngine
FrozenAcquisitionRunContext
FrozenAcquisitionRunState
FrozenAcquisitionNodeState
FrozenAcquisitionPublicationState
FrozenAcquisitionArenaState
``````

The normative engine and strategy operation is `advanceEpisode`, not an unbounded synchronous traversal loop.

### 34.5. Phase 5: Readiness Bridge and Fresh Restart

Introduce:

``````text
FrozenAcquisitionReadinessHandle
FrozenAcquisitionReadinessBridge
FrozenAcquisitionReadinessWait
FrozenAcquisitionRestartDescriptor
FrozenAcquisitionRestartAdmission
``````

Level 1.5 allows one active backend readiness wait per acquisition run.

### 34.6. Phase 6: Canonical Byte-Length Accounting

Introduce or retrofit canonical value objects so byte length is measured once at issue/lowering boundaries.

Candidate types:

``````text
CanonicalProtocolText
CanonicalConstructorSignature
CanonicalPropertyName
CanonicalAnnotationPayloadKey
``````

### 34.7. Phase 7: Backend Readiness Starvation Policy

Introduce:

``````text
ResolvedFrozenBackendReadinessPolicy
ResolvedDeterministicBackoffPolicy
``````

Wire the policy through the frozen acquisition slice of the pinned runtime policy epoch.

### 34.8. Phase 8: Lowering Completeness Validation

Introduce or extend:

``````text
FrozenMetamodelImageIntegrityValidator
FrozenLoweringCompletenessValidator
``````

to enforce:

``````text
planning-relevant backend facts are lowered, explicitly unavailable/unsupported, or rejected fail-closed.
``````

### 34.9. Phase 9: Reflection Backend

Implement:

``````text
ReflectionFrozenMetamodelAcquisitionBackend
ReflectionFrozenMetamodelFacade
``````

The reflection backend is the first backend implementation, not the architectural center.

### 34.10. Phase 10: Assembly Integration

Connect:

``````text
TypeReferenceClosureAcquisitionStrategy
-> FrozenMetamodelImageAssemblyInput
-> FrozenMetamodelAdapterAssembler
-> FrozenMetamodelImage
-> FrozenMetamodelProviderBundle
``````

### 34.11. Phase 11: Planning Provider Wiring

Move planning-facing provider injection toward:

``````text
FrozenMetamodelTypeShapeProvider
FrozenMetamodelTypeCycleIdentityProvider
FrozenMetamodelRawTypeFactsProvider
``````

### 34.12. Phase 12: KSP / Compiler-Static Compatibility

Document, but do not yet fully implement:

``````text
KspFrozenMetamodelAcquisitionBackend
BytecodeFrozenMetamodelAcquisitionBackend
CompilerStaticFrozenMetamodelAcquisitionBackend
PrecomputedIndexFrozenMetamodelAcquisitionBackend
``````

## 35. Adoption Rule

This ADR is proposed target-state architecture for frozen acquisition.

While it remains proposed, V1 reflection code may temporarily remain non-compliant.

Such non-compliance must be classified as migration debt.

No new architecture may deepen backend-handle lazy loading.

No new acquisition design may introduce coroutine suspension as frozen acquisition semantic control.

No new acquisition design may introduce retained worker ownership, retained node execution, or synchronous waiting as
normative frozen acquisition semantics.

No new backend may overclaim diagnostic, concurrency, identity-stability, batching, or direct-materialization
capability.

Capability overclaim is an acquisition integrity issue, not a performance hint.

All new metamodel acquisition work must move toward:

``````text
backend-model-neutral acquisition
-> explicit readiness callback bridge
-> explicit restart descriptor
-> deterministic frozen image
-> planning fact-lazy expansion
``````

Before frozen acquisition domain code depends on `RuntimePolicyEpoch`, `RuntimePolicyEpoch` must be moved out of
planning infrastructure.

Until then, frozen acquisition may accept only an adapter-neutral epoch id or orchestration-provided pinned policy
reference.

No new frozen acquisition code may add a direct dependency on:

``````kotlin
planning.infrastructure.runtime.policy.RuntimePolicyEpoch
``````

This migration rule prevents the shared policy epoch from becoming a planning-infrastructure dependency leak.

## 36. Appendix: Reference Pattern Notes

This appendix is non-normative.

Kontrakt's decision aligns with recurring SOTA patterns:

- restart-on-missing-dependency in build graph systems;
- explicit dependency tracking in incremental computation engines;
- immutable key/value query models in compiler systems;
- pass-scoped allocation in compiler infrastructures;
- typed deferral in source-generation processing;
- primitive/entity-oriented physical lowering in compiler IR storage.

The exact implementation details are not imported wholesale.

The architectural lesson is imported:

``````text
external handle lifetime and operational readiness must not leak into
core semantic identity or planning-visible facts.
``````

## Final Rule

Frozen acquisition is a backend-model-neutral deterministic state machine.

Backend-native handles are acquisition inputs.

They are not frozen facts.

They are not planning facts.

Kotlin coroutine suspension is not the semantic control model.

Retained worker ownership, retained node execution, and synchronous waiting are not frozen acquisition semantics.

Backend readiness is delivered through an explicit callback bridge.

Readiness fresh restart is admitted through explicit acquisition authority.

Planning consumes frozen adapter-neutral metamodel material.

Backend completion order is operational timing, not semantic order.

Fact-lazy planning over a frozen metamodel image remains the target architecture.

`RuntimePolicyEpoch` is shared policy governance.

`RuntimePolicyEpoch` is not planning run continuity.

`RuntimePolicyEpoch` is not frozen acquisition run continuity.

`RuntimePolicyEpoch` is not worker backing freshness.

`RuntimePolicyEpoch` is not backend session generation.

Frozen acquisition pins the shared policy epoch and consumes only the frozen acquisition policy slice.

Runtime watchdog material, if present, is outer orchestration material only and must not become frozen semantic,
capacity, identity, ordering, or restart-boundedness authority.

Frozen acquisition releases that pin at terminal state.

Planning pins the same kind of shared policy epoch and consumes only planning-relevant policy slices.

The shared policy epoch unifies governance.

It does not merge bounded-context lifecycles.

Frozen acquisition computes TypeReference closure and frozen table coverage.

Frozen acquisition meters coverage work and lowering work separately from planning semantic expansion work.

Planning computes request-specific semantic expansion over the frozen image.

The two passes are not merged.

ADR-0040 does not introduce shared graph primitives between frozen acquisition and planning.

The optimization target is to make planning traversal ordinal-based, table-backed, and backend-erased, not to reuse
frozen acquisition traversal state.

Backend erasure is not semantic compression.

Planning-relevant backend facts must be lowered, explicitly unavailable/unsupported, or rejected fail-closed.

ADR-0040 remains the foundation for later ADR-0041 HID/BLAKE3/interner work and later v2 query/incremental acquisition
work.