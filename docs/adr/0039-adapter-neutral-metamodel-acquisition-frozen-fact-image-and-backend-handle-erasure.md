# ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure

## Status

Accepted

## Date

2026-05-06

## Related

- ADR-0001: Adoption of Hexagonal Architecture
- ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
- ADR-0031: Cache-Blind Determinism and Plan Interning Governance
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0035: Deterministic Balanced Lanes for Tier-2 Join Completion Delivery
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- ADR-0038: Interface Contract Polymorphic Expansion and Non-Composite Type Expansion Completion
- `docs/design/deterministic-active-member-projection-and-ordering-protocol.md`
- `docs/design/l1-planner-session-primitive-data-structures.md`
- `docs/design/adr-0034-l2-join-lifecycle-design-note.md`
- `docs/design/adr-0035-primitive-lane-owned-delivery-plane.md`

## 1. Context

Kontrakt currently has a V1 reflection-backed metamodel path.

The transitional reflection path can be described as:

``````text
KType
-> ReflectionTypeReferenceBridge
-> CanonicalTypeReferenceIssuer
-> TypeReference
-> ReflectionTypeHandleRegistry.bind(TypeReference, KType)

Later:
TypeShapeProvider.resolveTypeShape(TypeReference)
RawTypeFactsProvider.resolveRawFacts(TypeReference)
    -> ReflectionTypeHandleRegistry.requireKType(TypeReference)
    -> KType
    -> reflection-backed shape/raw fact resolution
``````

This works as a transitional implementation, but it exposes a deeper architectural problem.

`TypeReference` is intended to be a domain-issued, adapter-neutral metamodel identity.

It must remain valid regardless of whether the source adapter is:

- reflection;
- KSP;
- bytecode analysis;
- source analysis;
- generated metadata;
- a future static metamodel manifest.

The project already adopts Hexagonal Architecture specifically to keep the core independent from volatile technology
choices such as reflection, KSP, scanner implementations, and future adapters. ADR-0001 states that the core must not
know implementation technologies and that such technologies must live behind ports/adapters.

However, if planning-time providers need to return from `TypeReference` to a backend-native handle, every backend must
carry its own sidecar registry:

``````text
Reflection:
TypeReference -> KType

KSP:
TypeReference -> KSType / KSDeclaration

Bytecode:
TypeReference -> ClassSymbol / MethodSymbol

Source:
TypeReference -> SourceSymbol / AST/PSI handle
``````

This creates a structural leak.

The leak is not that `TypeReference` literally contains a backend handle.

The leak is that planning-time provider execution still depends on backend-native handles through adapter-local mutable
state.

That sidecar state then creates secondary problems:

- mutable sidecar lifetime;
- ownership ambiguity;
- lane/thread/concurrency policy;
- accidental `ConcurrentHashMap` or lock introduction;
- adapter-specific cache behavior;
- classloader / symbol lifetime concerns;
- KSP symbol lifetime concerns;
- inability to promote facts safely into L2;
- difficulty proving cache-blind semantic determinism;
- hidden backend-handle reachability through supposedly frozen records.

Kontrakt's architecture must instead follow a compiler-style acquisition/freeze boundary.

Backend-native handles are acquisition inputs.

They are not planning facts.

## 2. External Architectural Pattern

This ADR follows a recurring pattern found in high-quality compiler, build, and database systems.

The exact vocabulary differs, but the underlying strategy is consistent:

1. external or mutable handles are kept inside a bounded context;
2. domain/query keys are stable and adapter-neutral;
3. mutable builder/memo state has a short lifetime;
4. after freeze/register/seal, consumers read immutable facts, values, or query outputs;
5. identity keys and mutable implementation details are not mixed;
6. incremental/lazy computation happens over stable facts or query keys, not over arbitrary backend handles.

Reference patterns:

- query-key / query-value incremental compilation;
- context-owned uniqued type storage;
- immutable build graph key/value evaluation;
- optimizer memo registration;
- dataflow fact/change maintenance.

Kontrakt adopts the same structural lesson:

``````text
backend handle
-> acquisition/lowering
-> frozen adapter-neutral metamodel image
-> planning-facing providers
``````

Planning must not depend on reflection, KSP, bytecode, or source-analysis object lifetimes.

## 3. Problem

The current V1 reflection path tempts the framework into treating a backend handle registry as a planning-time
dependency.

That is a structural smell.

The problem is not solved by changing the registry implementation from:

- `ConcurrentHashMap`,
- to `synchronized`,
- to lock-free CAS,
- to open-addressed object arrays,
- to primitive slabs.

Those are storage strategies.

The deeper problem is that planning is asking the adapter to re-open backend-native material after canonical identity
issuance.

A final architecture with:

``````text
TypeReference -> backend handle -> facts
``````

is non-compliant with Kontrakt's long-term architecture because it makes backend handles part of the effective planning
data path.

That violates six core principles.

### 3.1. Adapter Neutrality

`TypeReference` must not imply reflection, KSP, bytecode, or source origin.

If a planning provider requires a backend-native handle, the adapter-neutral boundary is incomplete.

### 3.2. Determinism

Backend-native handles are often tied to implementation details:

- reflection object identity;
- KSP processing lifetime;
- compiler symbol lifetime;
- classloader state;
- source-indexing structures;
- backend enumeration order.

These are not stable planning facts.

### 3.3. Hexagonal Architecture

Ports should expose domain-facing facts, not backend-native recovery paths.

Reflection, KSP, bytecode, and source adapters are interchangeable only if they lower into the same metamodel contract.

### 3.4. Cache-Blind Semantics

L2 cache/interner/governance must operate on adapter-neutral material.

Backend handles must never become cache keys, route keys, equality material, or cache-resident payload.

### 3.5. Backend-Handle Reachability

A frozen record that does not directly store `KType` or `KSDeclaration` can still violate this ADR if it stores an
indirect recovery key that can reopen the backend handle.

Examples of forbidden indirect recovery material:

- classloader-local indexes;
- reflection registry keys;
- KSP resolver-local ids;
- PSI/AST node ids;
- object identity handles;
- backend table offsets;
- generated handles that can reconstruct backend-native state.

Frozen records must erase backend-native reachability, not merely hide it behind another name.

### 3.6. Cross-Adapter Identity Drift

Reflection and KSP may observe the same semantic model through different backend-specific surfaces.

Examples:

- nested class spelling differences;
- `$` vs `.` lowering differences;
- platform nullability differences;
- wildcard/star-projection differences;
- declaration-order availability differences;
- synthetic/generated member visibility differences.

If adapter-specific spelling leaks into canonical identity, the same semantic type can receive different `TypeReference`
values depending on the backend.

That would break cache reuse, equality, reproducibility, and adapter interchangeability.

## 4. Decision

Kontrakt will introduce an adapter-neutral metamodel acquisition architecture.

The long-term shape is:

``````text
Backend handle
    Reflection: KType / KClass
    KSP:        KSType / KSDeclaration
    Bytecode:   ClassSymbol / MethodSymbol
    Source:     SourceSymbol / AST handle

-> MetamodelAcquisitionLane<THandle>
-> MutableMetamodelAcquisitionArena<THandle>
-> freeze()
-> FrozenMetamodelImage
-> Planning-facing providers
``````

Planning-facing providers must eventually be backed by `FrozenMetamodelImage`, not by backend-native handle registries.

The target provider family is:

``````text
FrozenMetamodelTypeShapeProvider
FrozenMetamodelTypeCycleIdentityProvider
FrozenMetamodelRawTypeFactsProvider
``````

These providers consume:

``````text
TypeReference -> FrozenMetamodelImage -> adapter-neutral facts
``````

They must not consume:

``````text
TypeReference -> KType
TypeReference -> KSType
TypeReference -> KSDeclaration
TypeReference -> bytecode symbol
TypeReference -> source AST/PSI handle
``````

## 5. Core Rule

Adapter-native handles are acquisition inputs, not planning facts.

`TypeReference` is adapter-neutral.

Planning consumes frozen adapter-neutral metamodel material.

Frozen records must not contain direct or indirect backend-handle reachability.

## 6. TypeReference Adapter-Neutrality Law

`TypeReference` MUST NOT contain, expose, or semantically depend on:

- `KType`;
- `KClass`;
- `KFunction`;
- `KProperty`;
- `KSType`;
- `KSDeclaration`;
- bytecode parser handles;
- source AST / PSI handles;
- classloader identity;
- JVM object identity;
- adapter lane id;
- registry ordinal;
- acquisition slot id;
- discovery append ordinal;
- backend-specific symbol id;
- backend-specific descriptor unless already ratified as adapter-neutral canonical text.

Forbidden illustrative shape:

``````kotlin
class TypeReference private constructor(
    val id: CanonicalTypeId,
    val adapterSlot: Int,
    val reflectionHandleId: Long,
)
``````

Allowed illustrative shape:

``````kotlin
class TypeReference private constructor(
    val id: CanonicalTypeId,
    val cycleKey: TypeCycleKey,
    val signature: CanonicalTypeSignature,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val typeNestingDepth: Int,
)
``````

Primitive ids, ordinals, and slots may exist inside acquisition or frozen-image storage.

They must not become semantic fields of `TypeReference`.

## 7. Canonical Issuance Contract

All adapters must converge through the same domain issuance authority.

Adapter-specific observation must be lowered into a backend-neutral issuance material shape before `TypeReference` is
issued.

Illustrative shape:

``````kotlin
class CanonicalTypeReferenceMaterial private constructor(
    val idText: String,
    val cycleText: String,
    val signatureText: String,
    val shapeSummary: TypeShapeSummary,
    val classifierId: String,
    val classifierVersion: String,
    val ratificationFingerprint: TypeShapeRatificationFingerprint,
    val useSiteAnnotations: OrderedUseSiteAnnotations,
    val typeNestingDepth: Int,
)
``````

Required flow:

``````text
Reflection/KSP/Bytecode/Source observation
-> adapter-specific lowering
-> CanonicalTypeReferenceMaterial
-> CanonicalTypeReferenceIssuer
-> TypeReference
``````

Forbidden flow:

``````text
Reflection/KSP/Bytecode/Source observation
-> adapter directly calls TypeReference.issue(...)
``````

The same semantic model must produce the same `CanonicalTypeReferenceMaterial` fields after adapter-specific lowering,
or must fail closed with explicit unavailable/unknown metadata.

Backend differences must be represented explicitly, not silently normalized into ordinary false/default values.

## 8. Cross-Adapter Equivalence Gate

Reflection and KSP equivalence is not optional.

The architecture requires a compatibility gate:

``````text
same semantic model
-> reflection acquisition
-> FrozenMetamodelImage

same semantic model
-> KSP acquisition
-> FrozenMetamodelImage

compare:
- TypeReference
- TypeShapeSummary
- TypeCycleIdentity material
- Raw fact records
- nullability certainty
- declaration ordinal availability
- constructor/property identity
- annotation material
``````

If reflection and KSP disagree on a required semantic axis, the framework must choose one of the following:

1. normalize through a ratified backend-neutral law;
2. represent the axis as `UNKNOWN` / `UNAVAILABLE`;
3. fail closed.

It must not silently choose one backend's interpretation as semantic truth.

## 9. New Vocabulary

### 9.1. `MetamodelAcquisitionLane<THandle>`

A backend-specific lane-scoped acquisition boundary.

The lane does not acquire just one root.

It acquires an acquisition request and owns the transitive closure needed to produce a frozen metamodel image.

Illustrative shape:

``````kotlin
interface MetamodelAcquisitionLane<THandle> : AutoCloseable {
    fun acquire(
        request: MetamodelAcquisitionRequest<THandle>,
    ): MetamodelAcquisitionResult

    fun freeze(): FrozenMetamodelImage

    override fun close()
}
``````

Request shape:

``````kotlin
class MetamodelAcquisitionRequest<THandle> private constructor(
    val roots: AcquisitionSequence<THandle>,
    val scopeId: String,
    val scopeVersion: String,
)
``````

Result shape:

``````kotlin
class MetamodelAcquisitionResult private constructor(
    val rootReferences: AcquisitionSequence<TypeReference>,
    val acquiredTypeCount: Int,
)
``````

Examples:

``````kotlin
class ReflectionMetamodelAcquisitionLane :
    MetamodelAcquisitionLane<KType>

class KspMetamodelAcquisitionLane :
    MetamodelAcquisitionLane<KSType>
``````

Responsibilities:

- own backend-native mutable acquisition state;
- traverse or request the transitive closure required by the acquisition scope;
- lower backend-native observations into adapter-neutral material;
- issue `TypeReference` only through canonical metamodel-domain services;
- produce a `FrozenMetamodelImage`;
- drop backend-native handle reachability at freeze;
- prevent planning from seeing backend-native handles.

Non-responsibilities:

- planning traversal;
- active-member projection policy;
- HID;
- L2 interner governance;
- eviction;
- runtime execution.

### 9.2. Lane Ownership Law

`MetamodelAcquisitionLane<THandle>` is lane-owned single-writer state.

This imports the broad ownership law from ADR-0035, but not the L2 dispatch mechanics.

ADR-0035 prefers primitive-friendly, lane-local structures and disallows global dynamic contention surfaces; it also
requires explicit operational state machines and lane-owned final clear for mutable lane state.

Applied here:

- no global `ConcurrentHashMap` as semantic storage;
- no global mutable acquisition registry;
- no external direct mutation of lane-owned arena state;
- one lane owns mutation;
- freeze/close are explicit lifecycle operations;
- final clearing of backend handles is lane-owned;
- administrative close may publish intent but must not corrupt lane-owned state.

This ADR does not import ADR-0035's L2 waiter delivery mechanics.

Not imported:

- waiter stores;
- deadline heaps;
- ready queues;
- route64 delivery;
- joined-wait callback delivery;
- dirty-shard replay.

Adopted principle:

``````text
mutable operational state must have an explicit owner and a bounded lifetime
``````

### 9.3. `MutableMetamodelAcquisitionArena<THandle>`

A lane-owned mutable acquisition builder.

The arena is not a free-form registry.

It is an explicit state machine.

Illustrative shape:

``````kotlin
interface MutableMetamodelAcquisitionArena<THandle> {
    fun beginType(
        handle: THandle,
    ): AcquisitionTypeSlot

    fun issueIdentity(
        slot: AcquisitionTypeSlot,
        material: CanonicalTypeReferenceMaterial,
    ): TypeReference

    fun recordShape(
        slot: AcquisitionTypeSlot,
        shapeRecord: FrozenTypeShapeRecordCandidate,
    )

    fun recordCycleIdentity(
        slot: AcquisitionTypeSlot,
        cycleRecord: FrozenTypeCycleIdentityRecordCandidate,
    )

    fun recordRawFactMaterial(
        slot: AcquisitionTypeSlot,
        rawRecord: FrozenRawFactRecordCandidate,
    )

    fun freeze(): FrozenMetamodelImage
}
``````

Slot state vocabulary:

``````kotlin
enum class AcquisitionTypeSlotState {
    OPEN,
    IDENTITY_ISSUED,
    SHAPE_RECORDED,
    CYCLE_RECORDED,
    RAW_RECORD_RECORDED,
    SEALED,
}
``````

Required transition law:

``````text
OPEN
-> IDENTITY_ISSUED
-> SHAPE_RECORDED
-> CYCLE_RECORDED
-> RAW_RECORD_RECORDED
-> SEALED
``````

Allowed shortcut:

``````text
CYCLE_RECORDED
-> SEALED
``````

The shortcut is allowed when raw fact material is represented by an explicit frozen lazy raw record descriptor or when
the type is known not to require raw facts.

Forbidden:

``````text
OPEN -> SHAPE_RECORDED
OPEN -> RAW_RECORD_RECORDED
IDENTITY_ISSUED -> RAW_RECORD_RECORDED without shape/cycle record
SEALED -> any write
FROZEN arena -> any write
CLOSED arena -> any write
``````

Reason:

The acquisition arena is mutable, but its mutation law must be visible and closed.

Ordering cannot remain implicit in scattered branch logic.

### 9.4. `FrozenMetamodelImage`

Illustrative shape:

``````kotlin
class FrozenMetamodelImage private constructor(
    val imageId: FrozenMetamodelImageId,
    val schemaVersion: FrozenMetamodelImageSchemaVersion,
    val typeIndex: FrozenTypeReferenceIndex,
    val shapeTable: FrozenTypeShapeTable,
    val cycleIdentityTable: FrozenTypeCycleIdentityTable,
    val rawFactTable: FrozenRawFactTable,
)
``````

`FrozenMetamodelImage` is planning-visible semantic material.

It deliberately does not expose source-adapter provenance as an ordinary field.

Reason:

``````text
sourceAdapterProvenance
-> diagnostic / compatibility material
-> not semantic planning input
``````

Planning-facing providers must not be able to branch on whether the image came from reflection, KSP, bytecode, source
analysis, or generated metadata.

Forbidden:

``````kotlin
if (image.sourceAdapterProvenance.sourceAdapterKind == MetamodelSourceAdapterKind.KSP) {
    // semantic behavior branch
}
``````

Allowed:

``````text
FrozenMetamodelImageEnvelope
-> image
-> diagnosticHeader
``````

Planning-facing providers receive only `FrozenMetamodelImage`.

Diagnostic tooling, bootstrap code, or compatibility-reporting code may receive `FrozenMetamodelImageEnvelope`.

`FrozenMetamodelImage.issue(...)` must perform freeze-final integrity validation before publishing the image.

The type index is the coverage authority.

For every `TypeReference` in the frozen type index:

- the shape table must contain explicit coverage;
- the cycle identity table must contain explicit coverage;
- the raw fact table must contain explicit coverage.

Raw fact coverage does not necessarily mean eager `RawTypeFactsDTO` materialization.

Allowed raw fact coverage:

``````text
materialized RawTypeFactsDTO
frozen raw fact record
TRUNCATED sentinel record
FILTERED_BY_POLICY sentinel record
UNAVAILABLE_FROM_BACKEND sentinel record
ACQUISITION_FAILED diagnostic record
``````

Missing coverage is a frozen image integrity failure.

It must fail before the image is published.

### 9.5. `FrozenMetamodelImageEnvelope`

`FrozenMetamodelImageEnvelope` is an adapter/bootstrap return object.

It separates planning-visible semantic material from diagnostic provenance.

Illustrative shape:

``````kotlin
class FrozenMetamodelImageEnvelope private constructor(
    val image: FrozenMetamodelImage,
    val diagnosticHeader: FrozenMetamodelImageDiagnosticHeader,
)
``````

`FrozenMetamodelImageDiagnosticHeader` may contain source adapter provenance.

Illustrative shape:

``````kotlin
class FrozenMetamodelImageDiagnosticHeader private constructor(
    val sourceAdapterProvenance: MetamodelSourceAdapterProvenance,
)
``````

Rules:

- planning-facing providers must receive only `FrozenMetamodelImage`;
- diagnostic tooling may receive `FrozenMetamodelImageEnvelope`;
- source adapter provenance must not participate in semantic equality;
- source adapter provenance must not influence planning traversal;
- source adapter provenance must not influence type expansion decisions;
- source adapter provenance must not influence L2 key material;
- source adapter provenance must not become route64 or PlanCacheKey material.

Reason:

Diagnostic provenance is useful for debugging and compatibility reports.

It is dangerous as ordinary planning input because it can create backend-dependent semantic branches.

`MetamodelSourceAdapterProvenance` is diagnostic material.

It may implement structural equality for diagnostic aggregation and test assertions, but that equality is not
frozen-image semantic equality.

`sourceAdapterVersion` is an exact diagnostic token. It does not define semantic-version compatibility, stale-image
rejection, or migration behavior.

Any adapter-version compatibility policy must live at a bootstrap, loader, diagnostic, or compatibility-reporting
boundary. Planning-facing providers must not consume source adapter provenance and must not branch on adapter kind or
adapter version.

### 9.6. `FrozenMetamodelImageId`

`FrozenMetamodelImageId` is a diagnostic and compatibility identity for one frozen image.

It is not:

- a `TypeReference`;
- an L2 plan cache key;
- a route64;
- a canonical plan identity;
- a hidden adapter handle key.

Illustrative shape:

``````kotlin
class FrozenMetamodelImageId private constructor(
    val acquisitionScopeId: String,
    val imageBuildOrdinal: Long,
)
``````

Rules:

- it may identify the frozen image for diagnostics;
- it may participate in image compatibility messages;
- it must not be used as semantic type equality material;
- it must not be used as L2 cache key authority;
- it must not encode backend handle identity;
- it must not encode classloader identity;
- it must not encode object identity;
- it must not duplicate FrozenMetamodelImage.schemaVersion.
- it must not contain source adapter provenance.

The future persistent frozen-image identity requires a separate canonical encoding / digest ADR.

### 9.7. `FrozenMetamodel*Provider`

Planning-facing frozen providers must be constructed from `FrozenMetamodelImage`, not from
`FrozenMetamodelImageEnvelope`.

They must not receive `FrozenMetamodelImageDiagnosticHeader`.

They must not branch on source adapter provenance.

Forbidden:

```kotlin
class FrozenMetamodelTypeShapeProvider private constructor(
    private val envelope: FrozenMetamodelImageEnvelope,
)
```

Allowed:

```kotlin
class FrozenMetamodelTypeShapeProvider private constructor(
    private val image: FrozenMetamodelImage,
)
```

#### Provider lookup law

Frozen providers must use the image-local ordinal lookup path:

```text
TypeReference
-> FrozenTypeReferenceIndex.ordinalOf(reference)
-> frozen table read by frozen ordinal
```

Allowed provider lookup shape:

```kotlin
val frozenOrdinal =
    image.typeIndex.ordinalOf(reference)

if (frozenOrdinal == FrozenTypeReferenceIndex.MISSING_ORDINAL) {
    throw FrozenMetamodelUnknownTypeReferenceException()
}

val shape =
    image.shapeTable.findShapeAt(frozenOrdinal)
        ?: throw FrozenMetamodelIncompleteTableException()
```

Forbidden provider lookup shape:

```kotlin
if (!image.typeIndex.contains(reference)) {
    throw FrozenMetamodelUnknownTypeReferenceException()
}

val shape =
    image.shapeTable.findShape(reference)
```

Reason:

The frozen type index is the only authority for:

```text
TypeReference -> image-local frozen type ordinal
```

Frozen tables are ordinal-addressed payload tables.

They must not independently repeat TypeReference lookup. Repeating lookup work in each table reintroduces double lookup
pressure and weakens the table/index authority split.

#### Missing-reference vs incomplete-table law

Providers must distinguish these cases:

```text
reference absent from FrozenTypeReferenceIndex
-> unknown reference for this image

reference present in FrozenTypeReferenceIndex
but payload table has no slot coverage
-> incomplete frozen image
```

Therefore:

- absent from type index must fail as `FrozenMetamodelUnknownTypeReferenceException`;
- present in type index but missing table payload must fail as `FrozenMetamodelIncompleteTableException`.

#### Cycle identity algorithm authority law

`FrozenMetamodelTypeCycleIdentityProvider` must not accept caller-supplied algorithm metadata.

The cycle identity algorithm metadata authority belongs to `FrozenTypeCycleIdentityTable`.

Provider metadata must be derived from the table:

```text
provider.identityAlgorithmId == image.cycleIdentityTable.identityAlgorithmId
provider.identityAlgorithmVersion == image.cycleIdentityTable.identityAlgorithmVersion
```

Forbidden:

```kotlin
class FrozenMetamodelTypeCycleIdentityProvider private constructor(
    private val image: FrozenMetamodelImage,
    override val identityAlgorithmId: String,
    override val identityAlgorithmVersion: Long,
)
```

Allowed:

```kotlin
class FrozenMetamodelTypeCycleIdentityProvider private constructor(
    private val image: FrozenMetamodelImage,
) : TypeCycleIdentityProvider {
    override val identityAlgorithmId: String
        get() = image.cycleIdentityTable.identityAlgorithmId

    override val identityAlgorithmVersion: Long
        get() = image.cycleIdentityTable.identityAlgorithmVersion
}
```

Reason:

If provider metadata and table payload metadata have separate authorities, the provider can claim one cycle identity
algorithm while the table contains identities produced by another. That drift is a frozen image integrity violation.

#### Raw fact resolution accounting law

A frozen raw fact provider must return:

```kotlin
RawTypeFactsResolution.cacheHit()
```

It must not return backend actual resolution.

Reason:

All backend-native acquisition already happened before freeze publication. A frozen provider may materialize DTOs from
adapter-neutral frozen records, but it must not perform backend discovery or reopen backend handles.

These providers must not access backend-native handles.

## 10. Freeze Rule

Freeze is the boundary that turns backend-specific acquisition state into adapter-neutral immutable metamodel material.

Freeze does not necessarily mean every expensive DTO must be eagerly materialized.

Freeze means:

``````text
after freeze, all future planning-visible facts must be derivable without
backend-native handles.
``````

Accepted:

``````text
TypeReference
-> FrozenRawFactRecord
-> RawTypeFactsDTO
``````

Rejected:

``````text
TypeReference
-> KType
-> RawTypeFactsDTO
``````

Rejected:

``````text
TypeReference
-> KSDeclaration
-> RawTypeFactsDTO
``````

### 10.1. Freeze Memory Discipline

`freeze()` is not a planning hot path.

It is a heavy transition path.

It is the boundary where backend-native mutable acquisition state is lowered into adapter-neutral frozen material.

A compliant implementation must treat `freeze()` as both:

1. a semantic erasure boundary; and
2. a memory ownership transition boundary.

#### Two-World Overlap Risk

During freeze, two worlds may temporarily coexist:

``````text
old world:
    backend handles
    mutable acquisition arena
    reflection/KSP/source object graphs

new world:
    frozen adapter-neutral metamodel image
    frozen indexes
    frozen tables
    frozen records or slabs
``````

This overlap can create:

- memory peaks;
- object promotion;
- premature old-generation pressure;
- stop-the-world GC pressure;
- extended lifetime of backend-native object graphs.

Freeze implementation must minimize the overlap window.

#### Required Freeze Memory Rules

A compliant freeze implementation must follow these rules.

##### 1. Pre-count before allocation

The acquisition arena must expose enough counts to pre-size frozen structures.

Forbidden default:

``````text
repeatedly grow ArrayList/HashMap during freeze
``````

Preferred:

``````text
count records
-> allocate exact or bounded-capacity frozen storage
-> fill once
``````

##### 2. Direct-to-slab lowering

Freeze should lower acquisition slots directly into frozen table storage.

Forbidden default:

``````text
slot
-> temporary record object
-> list
-> copied table
``````

Preferred:

``````text
slot
-> frozen table offset
``````

##### 3. Early source-slot nullification

Once a backend handle slot has been fully lowered, the slot must be cleared as soon as legal.

Required intent:

``````text
lower slot i
-> write frozen material
-> clear backend handle slot i
-> continue
``````

This reduces backend-handle reachability before the whole image is published.

##### 4. No closure-backed frozen material

Frozen tables and records must not store:

- lambdas;
- suppliers;
- lazy delegates;
- service locators;
- callbacks;
- closures capturing backend handles;
- registry keys that can recover backend handles.

Reason:

An immutable table can still retain backend-native handles through closure capture.

That violates backend-handle erasure and can extend the lifetime of heavy backend object graphs.

##### 5. Vertical partitioning

Frozen image tables must remain vertically partitioned:

``````text
type index
shape table
cycle identity table
raw fact table
``````

Planning should be able to read shape and cycle material without touching raw fact material.

##### 6. Ordinal-friendly table layout

The type index must support deterministic local frozen ordinals.

The ordinal is local to the image and must not enter `TypeReference`.

Target shape:

``````text
TypeReference -> FrozenTypeOrdinal
FrozenTypeOrdinal -> shape/cycle/raw tables
``````

##### 7. Chunked freeze is allowed

If acquisition scope is large, freeze may process records in deterministic chunks.

Chunking must not change semantic order.

Required:

``````text
deterministic chunk boundaries
deterministic final ordering
same output independent of chunk size
``````

##### 8. Freeze-final validation before publication

The image must not be visible until table coverage and sequence laws pass.

Publication happens after validation.

#### Allowed Implementation Levels

Level 0: Transitional object image

``````text
immutable object tables
minimal migration
not final SOTA
``````

Level 1: Object-array frozen tables

``````text
Array<TypeReference?>
Array<ResolvedTypeShape?>
Array<TypeCycleIdentity?>
Array<FrozenRawFactRecord?>
``````

Level 2: Ordinal-indexed slab tables

``````text
FrozenTypeOrdinal
-> parallel arrays
-> compact local indexes
``````

Level 3: Primitive slab tables

``````text
IntArray / LongArray / ByteArray metadata
Object arrays only for unavoidable domain references
``````

Level 4: Canonical encoded slabs

``````text
versioned canonical byte layout
BLAKE3/HID-ready material
``````

ADR-0039 accepts the direction up to Level 2 as immediate design pressure.

Level 3 and Level 4 require separate canonical encoding, primitive table, and golden-vector work.

#### Final Freeze Memory Rule

`freeze()` may allocate, but it must not allocate casually.

The accepted target is:

``````text
pre-count
-> pre-size
-> deterministic order
-> direct-to-slab lowering
-> early backend-handle nullification
-> validation
-> publish frozen image
``````

### 10.2. Freeze Lifecycle State Machine

Acquisition lane lifecycle:

``````kotlin
enum class MetamodelAcquisitionLaneState {
    OPEN,
    ACQUIRING,
    FREEZING,
    FROZEN,
    CLOSED,
}
``````

Legal transitions:

``````text
OPEN -> ACQUIRING
ACQUIRING -> FREEZING
FREEZING -> FROZEN
OPEN -> CLOSED
ACQUIRING -> CLOSED
FROZEN -> CLOSED
``````

Forbidden:

``````text
FROZEN -> ACQUIRING
CLOSED -> ACQUIRING
CLOSED -> FREEZING
CLOSED -> FROZEN
``````

### 10.3. Freeze Semantics

`freeze()` is one-shot.

After successful `freeze()`:

- backend handle slabs are cleared;
- backend handle reachability is dropped;
- mutable acquisition records are sealed;
- the frozen image remains readable;
- any further arena write fails closed;
- any second `freeze()` fails closed unless an implementation explicitly documents idempotent return of the same image.
- backend handle slots are cleared as soon as their lowered frozen material has been written;
- freeze-final table coverage validation has passed;
- deterministic sequence validation has passed;
- frozen image publication happens only after validation;
- frozen material does not retain closure-backed backend-handle reachability.

Default policy:

``````text
second freeze call fails closed
Freeze must not be implemented as a casual copy from mutable objects into immutable wrappers.
A compliant implementation must minimize the time during which backend-native handles and frozen adapter-neutral material are both strongly reachable.
``````

Reason:

A second freeze can hide lifecycle bugs and create ambiguity about whether the same immutable image or a newly built
image is being observed.

### 10.4. Close Semantics

`close()` before freeze:

- aborts acquisition;
- clears backend handles;
- clears mutable arena state;
- does not produce a `FrozenMetamodelImage`;
- makes further acquisition/freeze calls fail closed.

`close()` after freeze:

- clears any remaining acquisition-only state;
- does not invalidate the already returned `FrozenMetamodelImage`;
- does not mutate planning-visible frozen material.

`close()` is idempotent only for cleanup.

It must not reopen or reinitialize acquisition.

## 11. Frozen Record Reachability Erasure Law

A frozen record is compliant only if it cannot directly or indirectly recover backend-native handles.

Forbidden direct fields:

- `KType`;
- `KClass`;
- `KSType`;
- `KSDeclaration`;
- bytecode parser handles;
- source AST/PSI handles.

Forbidden indirect fields:

- reflection registry keys;
- KSP resolver-local ids;
- classloader-local indexes;
- source parser node ids;
- object identity handles;
- backend table offsets;
- lambda callbacks that capture backend handles;
- lazy suppliers that close over backend handles;
- service locators that can recover backend handles.
- lazy delegates that can recover backend handles;
- suppliers that can recover backend handles;
- callbacks that capture acquisition arena slots;
- service locators that can reopen adapter registries;
- closure-backed table cells;
- memoized functions that close over backend-native objects.

Forbidden illustrative shape:

``````kotlin
class FrozenRawFactRecord private constructor(
    val reference: TypeReference,
    val hiddenReflectionKey: Int,
    val resolver: () -> KType,
)
``````

Allowed illustrative shape:

``````kotlin
class FrozenRawFactRecord private constructor(
    val reference: TypeReference,
    val constructorRecords: FrozenConstructorRecordSequence,
    val propertyRecords: FrozenPropertyRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val sourceAvailability: FrozenMetadataAvailability,
)
``````

A compliant implementation must provide a reachability audit.

Minimum audit requirement:

- frozen image package must not import reflection/KSP backend types;
- frozen record constructors must not accept backend handles;
- frozen records must not store function objects that capture backend handles;
- frozen records must not store registry ordinals that can recover backend handles;
- tests must assert backend handle absence at public frozen-image boundaries.
- frozen tables must not store lambdas, suppliers, lazy delegates, service locators, or callbacks;
- frozen table implementations must be plain-data, object-array-backed, ordinal-indexed, slab-backed, or
  primitive-array-backed;
- frozen records must not retain acquisition arena slots after freeze;
- freeze tests must assert that source acquisition slots are nulled or otherwise unreachable after successful lowering.

## 12. Frozen Metamodel Image Exception Taxonomy

Frozen image failures are protocol/integrity failures.

They must not be collapsed into one generic lookup exception.

A frozen image is the planning-visible metamodel input. Therefore failures must identify whether the problem is:

- an unknown reference;
- an incomplete frozen table;
- an incompatible image/schema;
- illegal lifecycle state;
- backend-handle reachability leakage;
- deterministic sequence violation;
- frozen-record materialization failure.

### 12.1. Base Exception

All frozen-image exceptions inherit from a dedicated metamodel exception family.

Illustrative shape:

``````kotlin
abstract class FrozenMetamodelImageException(
    message: String,
) : MetamodelException(message)
``````

This family is not:

- an adapter assembly exception;
- an adapter state exception;
- a strict-mode modeling exception;
- an ordinary cache miss;
- an L2 cache/governance failure.

It represents frozen metamodel image integrity and compatibility failures.

### 12.2. Table Identifier

Frozen image table references must be typed.

Do not pass arbitrary table-name strings.

Illustrative shape:

``````kotlin
enum class FrozenMetamodelImageTableId {
    TYPE_INDEX,
    SHAPE_TABLE,
    CYCLE_IDENTITY_TABLE,
    RAW_FACT_TABLE,
    CONSTRUCTOR_RECORD_SEQUENCE,
    CONSTRUCTOR_PARAMETER_SEQUENCE,
    PROPERTY_RECORD_SEQUENCE,
    ANNOTATION_RECORD_SEQUENCE,
}
``````

Reason:

A raw `tableName: String` weakens diagnostics and allows spelling drift.

### 12.3. Unknown TypeReference

Thrown when a provider is asked for a `TypeReference` that is not part of the frozen image type index.

Illustrative shape:

``````kotlin
class FrozenMetamodelUnknownTypeReferenceException(
    val imageId: FrozenMetamodelImageId,
    val reference: TypeReference,
    val requestedTable: FrozenMetamodelImageTableId,
) : FrozenMetamodelImageException(
    "Frozen metamodel image does not contain requested TypeReference: " +
            "image=$imageId, requestedTable=$requestedTable, " +
            "reference=${reference.renderSummary()}",
)
``````

Meaning:

``````text
The caller is likely mixing a TypeReference from another image/scope,
or acquisition failed to include the transitive type scope.
``````

This is not a cache miss.

### 12.4. Incomplete Frozen Table

Thrown when the image type index contains the `TypeReference`, but the required table does not contain the corresponding
record.

Illustrative shape:

``````kotlin
class FrozenMetamodelIncompleteTableException(
    val imageId: FrozenMetamodelImageId,
    val reference: TypeReference,
    val missingTable: FrozenMetamodelImageTableId,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image table is incomplete: " +
            "image=$imageId, missingTable=$missingTable, " +
            "reference=${reference.renderSummary()}, reason=$reason",
)
``````

Meaning:

``````text
The reference belongs to this image, but freeze failed to publish a required
shape/cycle/raw-fact/sequence record.
``````

Provider rule:

``````text
reference absent from typeIndex
-> FrozenMetamodelUnknownTypeReferenceException

reference present in typeIndex but absent from requested table
-> FrozenMetamodelIncompleteTableException
``````

### 12.5. Image Compatibility Violation

Thrown when an image cannot be consumed by a provider because schema, algorithm, source compatibility, or frozen-record
law does not match.

Illustrative shape:

``````kotlin
class FrozenMetamodelImageCompatibilityException(
    val imageId: FrozenMetamodelImageId,
    val expectedSchemaVersion: FrozenMetamodelImageSchemaVersion,
    val actualSchemaVersion: FrozenMetamodelImageSchemaVersion,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image compatibility violation: " +
            "image=$imageId, expectedSchema=$expectedSchemaVersion, " +
            "actualSchema=$actualSchemaVersion, reason=$reason",
)
``````

Examples:

- unsupported frozen image schema version;
- incompatible type-shape schema version;
- incompatible identity algorithm version;
- incompatible source adapter version for the requested compatibility mode.

### 12.6. Image Lifecycle Violation

Thrown when an acquisition lane, arena, or frozen image is used in an illegal lifecycle state.

Illustrative shape:

``````kotlin
class FrozenMetamodelImageLifecycleException(
    val imageId: FrozenMetamodelImageId?,
    val operation: String,
    val state: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image lifecycle violation: " +
            "image=$imageId, operation=$operation, state=$state, reason=$reason",
)
``````

Examples:

- write after freeze;
- second freeze call under non-idempotent freeze policy;
- acquire after close;
- freeze after close;
- provider reads from an invalidated acquisition-only structure.

### 12.7. Backend-Handle Reachability Violation

Thrown when frozen material directly or indirectly retains backend-native handles.

Illustrative shape:

``````kotlin
class FrozenMetamodelBackendReachabilityException(
    val imageId: FrozenMetamodelImageId,
    val recordKind: String,
    val forbiddenMaterialKind: String,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel image retains forbidden backend-handle reachability: " +
            "image=$imageId, recordKind=$recordKind, " +
            "forbiddenMaterial=$forbiddenMaterialKind, reason=$reason",
)
``````

Examples:

- `KType` field in a frozen record;
- `KSDeclaration` field in a frozen record;
- lambda/supplier capturing `KType`;
- registry ordinal that can recover `KType`;
- classloader-local lookup key;
- KSP resolver-local id.

This exception exists because “not storing `KType` directly” is insufficient.
Frozen material must not be able to recover backend handles indirectly.

### 12.8. Deterministic Sequence Violation

Thrown when a frozen record sequence violates ordering, duplicate, strict-total-order, or compact ordinal law.

Illustrative shape:

``````kotlin
class FrozenMetamodelSequenceViolationException(
    val imageId: FrozenMetamodelImageId,
    val sequenceTable: FrozenMetamodelImageTableId,
    val reference: TypeReference?,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel deterministic sequence violation: " +
            "image=$imageId, sequence=$sequenceTable, " +
            "reference=${reference?.renderSummary()}, reason=$reason",
)
``````

Examples:

- duplicate constructor key;
- duplicate property key;
- duplicate non-repeatable annotation key;
- comparator equality between distinct records;
- non-compact parameter index;
- local ordinal assigned before deterministic ordering;
- backend enumeration order used as semantic order.

### 12.9. Frozen Record Materialization Failure

Thrown when a frozen adapter-neutral record cannot materialize its planning-facing DTO.

Illustrative shape:

``````kotlin
class FrozenMetamodelRecordMaterializationException(
    val imageId: FrozenMetamodelImageId,
    val reference: TypeReference,
    val recordTable: FrozenMetamodelImageTableId,
    val reason: String,
) : FrozenMetamodelImageException(
    "Frozen metamodel records materialization failed: " +
            "image=$imageId, table=$recordTable, " +
            "reference=${reference.renderSummary()}, reason=$reason",
)
``````

Examples:

- frozen raw fact record is internally incomplete;
- frozen annotation payload cannot lower into DTO payload;
- frozen constructor record references a missing parameter sequence;
- frozen availability state is inconsistent with the requested DTO.

This is different from `FrozenMetamodelIncompleteTableException`.

``````text
IncompleteTable:
the table entry is missing.

RecordMaterialization:
the table entry exists but cannot produce a valid DTO.
``````

### 12.10. Provider Lookup Algorithm

Frozen providers must use a two-step lookup.

Illustrative shape:

``````kotlin
class FrozenMetamodelTypeShapeProvider private constructor(
    private val image: FrozenMetamodelImage,
) : TypeShapeProvider {
    override fun resolveTypeShape(
        reference: TypeReference,
    ): ResolvedTypeShape {
        if (!image.typeIndex.contains(reference)) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                reference = reference,
                requestedTable = FrozenMetamodelImageTableId.SHAPE_TABLE,
            )
        }

        return image.shapeTable.findShape(reference)
            ?: throw FrozenMetamodelIncompleteTableException(
                imageId = image.imageId,
                reference = reference,
                missingTable = FrozenMetamodelImageTableId.SHAPE_TABLE,
                reason = "TypeReference exists in type index but has no shape records.",
            )
    }
}
``````

Raw facts provider example:

``````kotlin
class FrozenMetamodelRawTypeFactsProvider private constructor(
    private val image: FrozenMetamodelImage,
) : RawTypeFactsProvider {
    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        if (!image.typeIndex.contains(reference)) {
            throw FrozenMetamodelUnknownTypeReferenceException(
                imageId = image.imageId,
                reference = reference,
                requestedTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
            )
        }

        val facts =
            image.rawFactTable.findFacts(reference)
                ?: throw FrozenMetamodelIncompleteTableException(
                    imageId = image.imageId,
                    reference = reference,
                    missingTable = FrozenMetamodelImageTableId.RAW_FACT_TABLE,
                    reason = "TypeReference exists in type index but has no raw fact records.",
                )

        return RawTypeFactsResolution.cacheHit(facts)
    }
}
``````

### 12.11. Final Exception Rule

Frozen image failures must be precise.

Use:

``````text
UnknownTypeReference
-> reference is not part of image type index

IncompleteTable
-> reference is indexed but required table entry is missing

Compatibility
-> schema/algorithm/source compatibility mismatch

Lifecycle
-> illegal acquire/freeze/close/read state

BackendReachability
-> frozen material retains direct/indirect backend handle reachability

SequenceViolation
-> frozen deterministic sequence law violation

RecordMaterialization
-> frozen record exists but cannot produce DTO
``````

Do not reintroduce a single generic `FrozenMetamodelImageLookupException`.

## 13. Raw Facts Resolution Accounting

`RawTypeFactsProvider` returns `RawTypeFactsResolution` because metering must distinguish actual backend fact
discovery/reconciliation from already-ratified cached facts.

Frozen image providers do not perform backend discovery.

Therefore:

``````text
FrozenMetamodelRawTypeFactsProvider
-> RawTypeFactsResolution.cacheHit(...)
``````

This is true even when the provider lazily materializes a `RawTypeFactsDTO` from a frozen raw fact record for the first
time.

Reason:

The expensive backend discovery/reconciliation has already happened before freeze.

The first materialization from a frozen record is not backend actual resolution.

If DTO assembly from frozen records needs metering, introduce a separate frozen-materialization cost center.

Illustrative shape:

``````kotlin
class FrozenMetamodelRawTypeFactsProvider private constructor(
    private val image: FrozenMetamodelImage,
) : RawTypeFactsProvider {
    override fun resolveRawFacts(
        reference: TypeReference,
    ): RawTypeFactsResolution {
        val facts =
            image.rawFactTable.requireFacts(reference)

        return RawTypeFactsResolution.cacheHit(facts)
    }
}
``````

Forbidden:

``````text
Frozen image provider returning ACTUAL_RESOLUTION
because it would falsely meter frozen-record materialization as backend discovery.
``````

## 14. Lazy Policy

Kontrakt distinguishes three different forms of laziness.

### 14.1. Backend-Handle Lazy Loading

Rejected as target architecture.

Examples:

``````text
TypeReference -> KType -> facts
TypeReference -> KSType -> facts
TypeReference -> KSDeclaration -> facts
``````

This may remain temporarily in V1 reflection code, but it is migration debt.

Reason:

- backend handle lifecycle leaks into planning;
- adapter-specific registry state survives too long;
- KSP migration repeats the same problem with different handles;
- L2 promotion becomes unsafe;
- deterministic ownership becomes harder to prove.

### 14.2. Planning Fact-Lazy Expansion

Accepted.

This preserves ADR-0037.

Required order:

``````text
shape
-> cycle identity
-> active-cycle detection
-> raw facts only after cycle miss
-> projection
-> ordering
-> traversal
``````

Cycle-hit path must not require raw facts for the current type.

### 14.3. Frozen-Image Lazy Materialization

Accepted with restrictions.

Allowed:

``````text
TypeReference -> FrozenRawFactRecord -> RawTypeFactsDTO
``````

Allowed:

``````text
TypeReference -> FrozenShapeRecord -> ResolvedTypeShape
``````

Allowed:

``````text
TypeReference -> FrozenCycleIdentityRecord -> TypeCycleIdentity
``````

Forbidden:

``````text
TypeReference -> backend handle -> DTO
``````

The input to lazy materialization must be adapter-neutral immutable material.

## 15. Option D Clarification: Why Not Full Eager?

Option D is accepted target architecture.

It is not the same as full eager freeze.

### 15.1. Full Eager Freeze

Full eager freeze computes every DTO before planning.

Shape:

``````text
acquisition
-> compute all TypeReferences
-> compute all shapes
-> compute all cycle identities
-> compute all RawTypeFactsDTO
-> freeze
-> planning reads only DTOs
``````

Benefit:

- simplest read path;
- no backend handles after freeze;
- easiest correctness proof.

Cost:

- computes raw facts for cycle-hit types;
- weakens ADR-0037 fact-lazy benefit;
- can allocate large raw DTO graphs early.

### 15.2. Accepted Option D

Option D eagerly freezes identity, shape, and cycle identity material required for ADR-0037 preflight.

Raw facts are represented by frozen adapter-neutral records.

Shape:

``````text
acquisition
-> compute TypeReference
-> compute frozen shape record
-> compute frozen cycle identity record
-> lower raw fact candidate material into frozen raw fact records
-> drop backend handles
-> freeze

planning
-> shape from frozen image
-> cycle identity from frozen image
-> active-cycle detection
-> raw facts only after cycle miss
``````

This preserves:

- backend-handle erasure;
- fact-lazy planning;
- KSP lifetime safety;
- deterministic frozen image input.

### 15.3. Frozen Raw Fact Record vs RawTypeFactsDTO

`FrozenRawFactRecord` is immutable adapter-neutral material that can produce `RawTypeFactsDTO` without backend handles.

It is not a backend handle.

It is not a registry key.

It is not necessarily the final DTO.

Example:

``````text
FrozenRawFactRecord
- owner type reference
- constructor candidate records
- parameter records
- property records
- annotation records
- availability/sentinel records
- normalization/version provenance

RawTypeFactsDTO
- DTO shape required by planning raw fact port
``````

Reason:

This lets Kontrakt defer DTO assembly while still erasing backend handles before planning.

## 16. Frozen Record Deterministic Sequence Law

Frozen records may contain nested sequences.

Examples:

``````kotlin
class FrozenRawFactRecord private constructor(
    val reference: TypeReference,
    val constructorRecords: FrozenConstructorRecordSequence,
    val propertyRecords: FrozenPropertyRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val sourceAvailability: FrozenMetadataAvailability,
)
``````

These sequence types are not ordinary `List`, `Set`, or `Map` wrappers.

They are frozen deterministic sequences.

Every sequence stored inside a `FrozenMetamodelImage` must satisfy the same sequence law:

1. the ordering authority is explicit;
2. adapter enumeration order is non-authoritative;
3. ordering is a strict total order;
4. duplicate semantic keys fail closed;
5. equal adjacent comparator results fail closed;
6. local ordinals are compact and assigned after deterministic ordering;
7. no platform collection iteration order may become semantic output;
8. no backend-native declaration object may participate in equality or ordering;
9. sequence construction is completed before freeze;
10. sequence contents are immutable after freeze.

### 16.1. Identity Key vs Availability State

Frozen sequence keys must contain semantic identity material only.

Availability values are record state, diagnostic material, or metadata-certainty material. They must not participate in
the primary identity key unless a future ADR explicitly ratifies an availability-sensitive identity surface.

Forbidden illustrative shape:

``````kotlin
class FrozenConstructorRecordKey private constructor(
    val ownerType: TypeReference,
    val constructorSignature: CanonicalConstructorSignature,
    val parameterShapeSignature: CanonicalParameterShapeSignature,
    val declarationAvailability: FrozenMetadataAvailability,
)
``````

Allowed illustrative shape:

``````kotlin
class FrozenConstructorRecordKey private constructor(
    val ownerType: TypeReference,
    val constructorSignature: CanonicalConstructorSignature,
    val parameterShapeSignature: CanonicalParameterShapeSignature,
)

class FrozenConstructorRecord private constructor(
    val key: FrozenConstructorRecordKey,
    val parameterRecords: FrozenConstructorParameterRecordSequence,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val declarationAvailability: FrozenMetadataAvailability,
    val sourceAvailability: FrozenMetadataAvailability,
)
``````

Rule:

``````text
same semantic key + different availability payload
-> fail closed unless a ratified availability merge law exists
``````

Reason:

Availability drift is not identity drift.

If availability participates in the key, the same semantic constructor/property can be split into multiple records and
duplicate detection becomes weaker.

Records that do not have enough backend-neutral material to form a semantic key must not be inserted into ordinary
frozen deterministic sequences. They must either fail closed or be emitted through an explicit availability surface.

### 16.2. Constructor Record Sequence

`FrozenConstructorRecordSequence` is ordered by a structured constructor key.

Illustrative key:

``````kotlin
class FrozenConstructorRecordKey private constructor(
    val ownerType: TypeReference,
    val constructorSignature: CanonicalConstructorSignature,
    val parameterShapeSignature: CanonicalParameterShapeSignature,
)
``````

Ordering law:

``````text
ownerType canonical identity
-> constructorSignature canonical order
-> parameterShapeSignature canonical order
``````

Rules:

- reflection constructor enumeration order is forbidden;
- KSP constructor enumeration order is forbidden unless normalized;
- JVM descriptor order is not semantic authority;
- duplicate constructor keys fail closed;
- declaration availability is record state, not constructor key material;
- same constructor key with conflicting availability payload fails closed unless a ratified availability merge law
  exists;
- if two constructors cannot be distinguished by backend-neutral material, acquisition must fail closed or mark the
  overload surface unavailable according to a ratified availability law.

### 16.3. Constructor Parameter Sequence

Constructor parameters must be ordered by a compact deterministic parameter index.

Illustrative key:

``````kotlin
class FrozenConstructorParameterRecordKey private constructor(
    val ownerConstructorKey: FrozenConstructorRecordKey,
    val parameterIndex: Int,
    val parameterName: CanonicalParameterName,
)
``````

Rules:

- parameter indexes must be compact: `0..N-1`;
- duplicate parameter indexes fail closed;
- missing parameter names fail closed when names participate in canonical active-member identity;
- reflection `KParameter.index` is raw observation only;
- KSP parameter order is raw observation only;
- both must be lowered into the same compact parameter sequence law before freeze.

### 16.4. Property Record Sequence

`FrozenPropertyRecordSequence` is ordered by canonical property identity, not by reflection/KSP enumeration.

Illustrative key:

``````kotlin
class FrozenPropertyRecordKey private constructor(
    val ownerType: TypeReference,
    val propertyName: CanonicalPropertyName,
    val propertyType: TypeReference,
    val visibilityRank: Int,
)

class FrozenPropertyRecord private constructor(
    val key: FrozenPropertyRecordKey,
    val annotationRecords: FrozenAnnotationRecordSequence,
    val declarationAvailability: FrozenMetadataAvailability,
    val sourceAvailability: FrozenMetadataAvailability,
)
``````

Ordering law:

``````text
ownerType canonical identity
-> propertyName canonical identifier order
-> propertyType canonical identity
-> visibilityRank
``````

Rules:

- duplicate canonical property keys fail closed;
- declaration availability is record state, not property key material;
- same property key with conflicting availability payload fails closed unless a ratified availability merge law exists;
- synthetic/backend-only properties must be excluded or represented through an explicit availability/demotion law;
- backend enumeration order is forbidden;
- locale-dependent collation is forbidden.

### 16.5. Annotation Record Sequence

`FrozenAnnotationRecordSequence` is ordered by canonical annotation identity.

Illustrative key:

``````kotlin
class FrozenAnnotationRecordKey private constructor(
    val annotationType: TypeReference,
    val annotationQualifiedName: AnnotationQualifiedName,
    val useSiteTarget: FrozenAnnotationUseSiteTarget,
    val canonicalPayload: FrozenAnnotationPayload,
)
``````

Ordering law:

``````text
annotationQualifiedName canonical identifier order
-> useSiteTarget rank
-> canonicalPayload order
``````

Rules:

- backend annotation enumeration order is forbidden;
- duplicate annotation keys fail closed unless repeatable annotation semantics are explicitly ratified;
- repeatable annotations must carry a deterministic repeatable ordinal derived from canonical payload ordering, not
  backend enumeration order;
- annotation payload canonicalization must be backend-neutral.

### 16.6. Sequence Construction Protocol

All frozen sequences must be built through a closed construction protocol.

Illustrative shape:

``````kotlin
interface FrozenDeterministicSequenceBuilder<TRecord, TKey> {
    fun add(
        record: TRecord,
        key: TKey,
    )

    fun freeze(): FrozenDeterministicSequence<TRecord>
}
``````

The builder must:

1. collect records during acquisition;
2. lower each record to a backend-neutral key;
3. sort by the ratified key comparator;
4. scan adjacent entries;
5. reject comparator equality as duplicate/ambiguous material;
6. assign compact local ordinals after ordering;
7. publish an immutable sequence.

Forbidden:

``````kotlin
records.sortedBy { backendDeclarationIndex }
records.toSet()
records.associateBy { it.name }
records.sortWith(platformComparator)
``````

Minimum compliant algorithm:

``````text
collect records
-> compute backend-neutral keys
-> deterministic strict sort
-> adjacent duplicate check
-> compact ordinal assignment
-> immutable frozen sequence
``````

### 16.7. Comparator Strictness

Every frozen sequence comparator must be a strict total order.

If two distinct records compare as equal, the sequence builder must fail closed.

It must not rely on stable sort behavior to preserve backend order.

Reason:

Stable sorting only preserves input order for equal comparator keys.
If input order comes from reflection, KSP, bytecode, or source parser enumeration, then preserving that order imports
backend-specific nondeterminism.

Comparator equality therefore means:

``````text
the frozen key is insufficiently precise
``````

and must be treated as a protocol violation.

### 16.8. Local Ordinals

Local ordinals inside frozen sequences are assigned only after deterministic ordering.

Allowed:

``````text
deterministic sorted sequence index -> local ordinal
``````

Forbidden:

``````text
reflection declaration index -> local ordinal
KSP declaration index -> local ordinal
source parser iteration index -> local ordinal
discovery append index -> local ordinal
``````

Local ordinals are sequence-local storage/selection material.

They are not semantic fields of `TypeReference`.

### 16.9. Cross-Adapter Equivalence

For the same semantic model, reflection and KSP acquisition must produce equivalent frozen sequences.

Required comparison axes:

- sequence size;
- ordered sequence keys;
- compact local ordinals;
- record semantic payloads;
- availability categories;
- duplicate rejection behavior.

If one adapter can observe an axis that another adapter cannot observe, the axis must be represented through a
backend-neutral availability value.

It must not be silently omitted or replaced by backend defaults.

Illustrative availability vocabulary:

``````kotlin
enum class FrozenMetadataAvailability {
    PRESENT,
    UNAVAILABLE_FROM_BACKEND,
    UNKNOWN,
    REJECTED_UNSAFE,
    TRUNCATED,
    FILTERED_BY_POLICY,
    ACQUISITION_FAILED,
}
``````

Meaning:

- `PRESENT`: metadata was observed, lowered, and frozen successfully.
- `UNAVAILABLE_FROM_BACKEND`: the backend does not expose this metadata surface.
- `UNKNOWN`: the framework cannot determine whether the metadata is present.
- `REJECTED_UNSAFE`: metadata was rejected because it violates safety, protocol, or canonicalization law.
- `TRUNCATED`: metadata was intentionally cut by deterministic truncation policy such as cycle, depth, or budget cutoff.
- `FILTERED_BY_POLICY`: metadata was intentionally excluded by user/framework policy, such as visibility or member-scope
  policy.
- `ACQUISITION_FAILED`: metadata acquisition was attempted but failed due to a technical backend or acquisition error.

Availability drift is not identity drift.

If two records have the same semantic key but conflicting availability payloads, the sequence builder must fail closed
unless a ratified availability merge law exists.

`TRUNCATED` is not a planning command.

It means:

``````text
this metadata surface was intentionally cut by deterministic framework policy
``````

It does not mean:

``````text
the planner must directly stop expansion here
``````

Planning behavior must still be expressed through the normal expansion decision layer, such as traversal disposition,
type expansion decision, or cycle policy.

### 16.10. Final Sequence Rule

Frozen sequence determinism is part of frozen image determinism.

A `FrozenMetamodelImage` is not compliant unless all nested records and all nested record sequences satisfy this law.

## 17. Tradeoff Analysis

### 17.1. Option A: Keep Backend-Handle Registry as Planning Dependency

Shape:

``````text
TypeReference -> adapter registry -> backend handle -> facts
``````

Benefits:

- easiest V1 reflection implementation;
- minimal up-front refactoring;
- raw facts can remain truly backend-lazy;
- fewer frozen record types initially.

Costs:

- violates adapter neutrality in practice;
- requires one registry design per backend family;
- creates ownership and lifecycle problems;
- encourages `ConcurrentHashMap`, locks, or unsafe mutable tables;
- KSP transition repeats the same structural problem;
- planning path remains indirectly backend-dependent;
- L2 promotion cannot safely consume backend handle state;
- cache-blind determinism becomes harder to reason about;
- frozen-record reachability erasure cannot be proven.

Decision:

Rejected as final architecture.

Allowed only as transitional V1 compatibility debt.

### 17.2. Option B: Put Adapter Slot / Primitive Id into TypeReference

Shape:

``````text
TypeReference(adapterSlot = 17)
-> adapterSlab[17]
``````

Benefits:

- fast lookup;
- simple slab indexing;
- avoids hash lookup;
- low memory overhead.

Costs:

- contaminates `TypeReference` with adapter-local state;
- breaks reflection/KSP/bytecode/source interchangeability;
- makes equality and serialization ambiguous;
- risks making acquisition order semantic;
- makes TypeReference lifetime depend on adapter arena lifetime;
- conflicts with TypeReference's role as canonical domain identity.

Decision:

Rejected.

Primitive indexes may exist inside acquisition arenas or frozen images.

They must not be fields of `TypeReference`.

### 17.3. Option C: Full Eager Freeze

Shape:

``````text
acquisition
-> compute all shape/cycle/raw facts
-> freeze complete DTO image
-> planning reads only immutable facts
``````

Benefits:

- simplest frozen provider model;
- no backend handles after freeze;
- easiest to audit;
- KSP lifetime problem disappears;
- planning is fully read-only;
- strong DDD/Hexagonal separation.

Costs:

- may compute raw facts for types that cycle-hit and never need raw facts;
- weakens ADR-0037's fact-lazy performance benefit;
- can increase acquisition-time cost;
- can allocate large DTO graphs eagerly.

Decision:

Not the default target.

Allowed for small adapters, tests, or strict static modes.

### 17.4. Option D: Identity/Shape/Cycle Eager + Raw Fact Record Lazy

Shape:

``````text
acquisition:
    lower identity, shape, cycle identity eagerly
    lower raw-fact candidate material into immutable frozen records

freeze:
    drop backend handles

planning:
    resolve shape and cycle from frozen image
    materialize raw facts only after cycle miss
``````

Benefits:

- preserves ADR-0037 fact-lazy behavior;
- drops backend handles before planning;
- keeps planning adapter-neutral;
- supports KSP transition;
- allows deterministic frozen indexes;
- allows future L2 promotion of adapter-neutral facts;
- bounds mutable acquisition lifetime;
- supports compiler-style phase discipline.

Costs:

- requires new frozen raw fact record design;
- requires freeze validation and duplicate checks;
- requires transitional migration from V1 reflection registry;
- more complex than full eager freeze.

Decision:

Accepted target architecture.

### 17.5. Option E: Query System Over Frozen Image

Shape:

``````text
TypeReference query key
-> frozen image query engine
-> cached immutable query result
``````

Benefits:

- aligns with compiler query systems;
- supports projection/firewall patterns;
- can incrementally recompute only changed facts;
- can later support persistent fingerprints and red/green-like invalidation.

Costs:

- overkill for immediate reflection/KSP boundary;
- requires query dependency tracking;
- requires stable fingerprint/golden-vector protocol;
- not necessary before frozen image exists.

Decision:

Deferred.

The frozen image must not preclude a future query engine.

## 18. Accepted Architecture

Kontrakt adopts Option D as target architecture:

``````text
adapter-specific acquisition
-> adapter-neutral frozen metamodel image
-> planning fact-lazy expansion
``````

This preserves the framework's primary tradeoff priorities:

1. determinism;
2. adapter neutrality;
3. immutability after phase boundary;
4. reproducibility;
5. cache-blind semantics;
6. explicit lifecycle ownership;
7. backend-handle erasure;
8. performance through primitive/frozen indexes only after identity law is ratified.

## 19. Interaction with ADR-0037

This ADR does not weaken ADR-0037.

ADR-0037's fact-lazy expansion remains mandatory:

``````text
shape
-> cycle identity
-> active-cycle detection
-> raw facts only after cycle miss
``````

What changes is the source of facts.

Before target migration:

``````text
RawTypeFactsProvider
-> backend handle registry
-> KType / KSP symbol
-> facts
``````

After target migration:

``````text
RawTypeFactsProvider
-> FrozenMetamodelImage
-> FrozenRawFactRecord
-> facts
``````

Cycle-hit path still avoids raw fact materialization for the current type.

## 20. Interaction with ADR-0038

ADR-0038 requires all expansion decisions to become executable planning decisions.

ADR-0039 defines the metamodel acquisition boundary that feeds those expansion decisions.

ADR-0038 decides:

``````text
what expansion decisions planning must execute
``````

ADR-0039 decides:

``````text
where adapter-native type information is erased before planning consumes facts
``````

Together:

``````text
backend adapter
-> frozen metamodel image
-> TypeExpansionPipeline
-> executable TypeExpansionDecision
-> StructuralPlannerCore frames
``````

ADR-0038 already treats reflection, KSP, bytecode, and static-source adapters as backend implementations behind ports
and explicitly forbids Planning Core from using reflection/KSP APIs directly. the ADR-0038 hexagonal rule.

## 21. Interaction with ADR-0032

ADR-0032's primitive/slab direction remains valid.

However, primitive indexes are allowed only in the correct layer.

Allowed:

``````text
MutableMetamodelAcquisitionArena local slot
FrozenMetamodelImage frozen ordinal
LongArray lowered key table inside frozen image
ObjectArray / parallel primitive arrays for frozen records
``````

Forbidden:

``````text
TypeReference.adapterSlot
TypeReference.discoveryOrdinal
TypeReference.laneId
``````

Primitive storage is an implementation strategy for arenas/images.

It is not a semantic field of domain identity.

## 22. Interaction with ADR-0035

ADR-0035's lane-owned mutable state principle is adopted.

The acquisition layer should follow the same broad law:

- no global `ConcurrentHashMap` as semantic state;
- no global mutable registry;
- no caller-thread direct mutation of shared global state;
- lane-owned mutable acquisition;
- explicit lifecycle;
- freeze/seal boundary;
- owner-owned final clear.

ADR-0035 requires lane-owned final clear and forbids external threads from directly clearing lane-owned mutable state. .

However, ADR-0035's L2 dispatch mechanics are not imported wholesale.

Not imported:

- L2 join lane dispatch;
- waiter stores;
- deadline heaps;
- ready queues;
- dirty-shard replay;
- route64 dispatch;
- join terminalization rules.

Adopted principle:

``````text
mutable operational state must have an explicit owner and a bounded lifetime
``````

## 23. Determinism Requirements

A compliant frozen metamodel image must be deterministic with respect to:

- canonical type identity;
- canonical type text normalization version;
- type shape schema version;
- type identity algorithm id/version;
- explicit adapter source version;
- deterministic ordering laws;
- duplicate fail-closed checks;
- frozen index assignment law;
- backend-neutral lowering law.

It must not depend on:

- reflection enumeration order;
- KSP enumeration order unless normalized;
- platform collection iteration order;
- `ConcurrentHashMap` resize or traversal behavior;
- thread scheduling;
- wall-clock time;
- object identity;
- classloader identity;
- random salt;
- platform default charset;
- locale-sensitive collation.

## 24. Canonical Ordering vs Canonical Encoding

This ADR does not define canonical byte encoding.

Canonical byte encoding belongs to a later encoding/HID/golden-vector phase.

This ADR defines only the acquisition/freeze ownership boundary.

Ordering law and encoding law remain separate.

Rules:

- frozen image ordering must use Kontrakt canonical ordering laws;
- byte lowering for hashing, digest, and persistence is deferred;
- temporary adapter byte payloads must not become canonical identity authority;
- V1 reflection compatibility payloads are transitional and must not enter L2 as canonical material.

## 25. L2 Promotion Rule

Only adapter-neutral frozen material may be promoted into global L2.

Forbidden L2 content:

- `KType`;
- `KClass`;
- `KSDeclaration`;
- `KSType`;
- bytecode parser handles;
- source AST/PSI handles;
- backend object identity;
- classloader references;
- adapter registry cells;
- frozen records with indirect backend-handle reachability.
- diagnostic source adapter provenance;
- `FrozenMetamodelImageEnvelope`;
- `FrozenMetamodelImageDiagnosticHeader`;
- closure-backed frozen tables;
- frozen tables whose coverage has not been validated;
- frozen material produced before freeze-final validation.

Allowed L2 candidates after later ratification:

- frozen type identity records;
- frozen shape records;
- frozen raw fact records;
- frozen projection records;
- canonical IR;
- canonical plan keys.

Even adapter-neutral frozen material may be promoted to L2 only after:

- backend-handle reachability erasure;
- table coverage validation;
- deterministic sequence validation;
- canonical encoding law ratification;
- golden-vector coverage.

L2 promotion requires separate cache/interner/governance ADRs and golden vectors.

## 26. Transitional V1 Reflection Rule

The existing reflection registry path is classified as transitional debt.

Allowed temporarily:

``````text
TypeReference -> ReflectionTypeHandleRegistry -> KType
``````

Restrictions:

- must not enter domain objects;
- must not enter L2;
- must not become permanent planning architecture;
- must be documented as V1 compatibility;
- must be replaced by frozen image providers.

The registry may remain only until the frozen reflection metamodel image path is implemented.

## 27. Migration Plan

### 27.1. Phase 1: Document Transitional Debt

- Mark `ReflectionTypeHandleRegistry` as V1 transitional sidecar.
- State clearly that it is not the target planning provider dependency.
- Keep `TypeReference` adapter-neutral.
- Remove language implying the registry is final architecture.

### 27.2. Phase 2: Add Acquisition State Vocabulary

Introduce:

``````text
MetamodelAcquisitionLane<THandle>
MetamodelAcquisitionRequest<THandle>
MetamodelAcquisitionResult
MutableMetamodelAcquisitionArena<THandle>
AcquisitionTypeSlot
AcquisitionTypeSlotState
``````

Do not define frozen table schemas yet.

### 27.3. Phase 3: Define Candidate Records

Define adapter-neutral candidate records produced by acquisition:

``````text
FrozenTypeShapeRecordCandidate
FrozenTypeCycleIdentityRecordCandidate
FrozenRawFactRecordCandidate
FrozenMetadataAvailability
``````

This phase determines what the frozen tables need to store.

### 27.4. Phase 4: Add Frozen Image Skeleton

Add immutable table interfaces based on candidate records:

``````text
FrozenMetamodelImage
FrozenTypeReferenceIndex
FrozenTypeShapeTable
FrozenTypeCycleIdentityTable
FrozenRawFactTable
``````

Do not optimize into primitive `LongArray` tables until the lowered key law is ratified.

This phase must also add the freeze memory discipline surface:

``````text
FrozenTypeOrdinal
ordinal-friendly FrozenTypeReferenceIndex
coverage-aware FrozenTypeShapeTable
coverage-aware FrozenTypeCycleIdentityTable
coverage-aware FrozenRawFactTable
FrozenMetamodelImageIntegrityValidator
FrozenMetamodelImageEnvelope
FrozenMetamodelImageDiagnosticHeader
``````

This phase does not implement primitive `LongArray` tables yet.

It only makes the frozen image contract ordinal-friendly, closure-free, and validation-ready.

### 27.5. Phase 5: Reflection Vertical Slice

Implement a minimal vertical slice:

``````text
ReflectionMetamodelAcquisitionLane
MutableReflectionMetamodelArena
FrozenReflectionMetamodelImage
FrozenMetamodelTypeShapeProvider
FrozenMetamodelTypeCycleIdentityProvider
FrozenMetamodelRawTypeFactsProvider
``````

The frozen image must not contain `KType`.

### 27.6. Phase 6: Rewire Planning Providers

Move planning-facing provider injection from:

``````text
ReflectionTypeShapeProvider
ReflectionRawTypeFactsProvider
ReflectionTypeCycleIdentityProvider
``````

to:

``````text
FrozenMetamodelTypeShapeProvider
FrozenMetamodelRawTypeFactsProvider
FrozenMetamodelTypeCycleIdentityProvider
``````

### 27.7. Phase 7: KSP Vertical Slice

Implement:

``````text
KspMetamodelAcquisitionLane
MutableKspMetamodelArena
FrozenKspMetamodelImage
``````

The frozen image must not contain `KSType` or `KSDeclaration`.

### 27.8. Phase 8: Cross-Adapter Equivalence Gate

Add reflection/KSP golden-vector tests for the same semantic model.

Required comparison axes:

- `TypeReference`;
- type shape;
- cycle identity material;
- constructor candidate identity;
- property candidate identity;
- nullability certainty;
- default-value presence;
- declaration ordinal availability;
- annotation identity;
- raw fact availability categories.

### 27.9. Phase 9: L2 Readiness

Before L2 promotion:

- define frozen image schema version;
- define lowered key law;
- define duplicate detection law;
- define canonical encoding law;
- write golden vectors;
- test reflection/KSP equivalence on the same semantic model.

## 28. Non-Goals

This ADR does not define:

- canonical byte encoding;
- BLAKE3 derivation;
- HID;
- PlanCacheKey;
- route64;
- L2 interner join lifecycle;
- eviction;
- S3-FIFO / SIEVE selection;
- persistent frozen-image storage format;
- distributed discovery topology;
- full query engine;
- red/green incremental invalidation;
- source-level incremental watch protocol.
- off-heap frozen table storage;
- DirectBuffer-backed metamodel slabs;
- final primitive LongArray/IntArray frozen table implementation;
- bump-allocator emulation on the JVM;
- persistent frozen image binary format;

## 29. Compliance Rules

A compliant implementation must satisfy:

1. `TypeReference` contains no backend-native handle.
2. `TypeReference` contains no adapter slot, registry ordinal, lane id, or acquisition ordinal.
3. Backend-native handles are allowed only in acquisition state.
4. Freeze drops backend-native handle reachability from planning-visible material.
5. Frozen records must not contain indirect backend-handle recovery keys.
6. Planning-facing providers must eventually read from `FrozenMetamodelImage`.
7. Backend-handle lazy loading is migration debt, not target architecture.
8. Planning fact-lazy expansion remains mandatory.
9. Cycle-hit paths do not materialize raw facts for the current type.
10. Frozen-image lazy materialization may depend only on adapter-neutral immutable material.
11. Frozen raw fact provider returns `RawTypeFactsResolution.cacheHit(...)`, not `actualResolution(...)`.
12. `KType`, `KClass`, `KSType`, and `KSDeclaration` must not enter L2.
13. Frozen image ordering must not depend on adapter enumeration order.
14. Frozen image indexes may use primitive storage, but `TypeReference` must not expose those indexes.
15. Mutable acquisition state must have an explicit owner and bounded lifetime.
16. Global `ConcurrentHashMap` must not become semantic storage.
17. Any transitional reflection registry must be documented as V1 compatibility debt.
18. KSP must lower into the same frozen metamodel contract as reflection.
19. Equivalent reflection and KSP inputs must produce equivalent `TypeReference` / frozen facts under the same semantic
    model.
20. Adapter provenance may be diagnostic but must not affect semantic equality.
21. `FrozenMetamodelImage` must not expose source adapter provenance directly.
22. Source adapter provenance must be carried through diagnostic envelope/header or diagnostic-only ports.
23. Planning-facing providers must receive `FrozenMetamodelImage`, not `FrozenMetamodelImageEnvelope`.
24. Planning-facing providers must not receive `FrozenMetamodelImageDiagnosticHeader`.
25. Planning-facing providers must not branch on reflection/KSP/bytecode/source/generated provenance.
26. Diagnostic provenance must not influence planning traversal, type expansion decisions, active-cycle identity, L2 key
    material, route64, or `PlanCacheKey`.
27. L2 promotion is allowed only for adapter-neutral frozen material.
28. L2 promotion must reject backend-native handles, adapter provenance, `FrozenMetamodelImageEnvelope`, and
    `FrozenMetamodelImageDiagnosticHeader`.
29. L2 promotion must reject closure-backed frozen tables.
30. L2 promotion must reject frozen material produced before freeze-final validation.
31. Canonical byte encoding remains a separate ADR/design concern.
32. Freeze is one-shot by default.
33. Writes after freeze fail closed.
34. Close before freeze aborts acquisition and produces no image.
35. Close after freeze releases acquisition-only state without invalidating the frozen image.
36. Freeze is both a semantic erasure boundary and a memory ownership transition boundary.
37. Freeze must minimize the two-world overlap between backend-native acquisition state and frozen adapter-neutral
    material.
38. Freeze should pre-count before allocation.
39. Freeze should pre-size frozen structures instead of repeatedly growing `ArrayList`, `HashMap`, or equivalent dynamic
    structures during freeze.
40. Freeze should lower acquisition slots directly into frozen table storage whenever possible.
41. Freeze should clear backend handle slots as soon as their lowered frozen material has been written and no longer
    needs the backend handle.
42. Freeze publication must happen only after table coverage validation.
43. Freeze publication must happen only after deterministic sequence validation.
44. Freeze publication must happen only after backend-handle reachability erasure.
45. Chunked freeze is allowed only if chunk boundaries are deterministic and output is independent of chunk size.
46. The type index is the frozen image coverage authority.
47. Every indexed `TypeReference` must have explicit shape table coverage.
48. Every indexed `TypeReference` must have explicit cycle identity table coverage.
49. Every indexed `TypeReference` must have explicit raw fact table coverage.
50. Raw fact coverage may be a materialized DTO, frozen raw fact record, deterministic sentinel record, or
    acquisition-failure diagnostic record.
51. Missing table coverage is a frozen image integrity failure and must fail before image publication.
52. Frozen providers must distinguish unknown `TypeReference`, incomplete table entry, materialization failure,
    lifecycle violation, compatibility violation, backend reachability violation, and deterministic sequence violation
    through the dedicated `FrozenMetamodelImageException` taxonomy.
53. Frozen tables must be closure-free.
54. Frozen tables must not store lambdas, suppliers, lazy delegates, service locators, callbacks, closure-backed cells,
    or memoized functions that capture backend-native objects.
55. Frozen tables must not store registry keys, resolver-local ids, classloader-local indexes, or service locators that
    can recover backend handles.
56. Frozen table implementations must be plain-data, object-array-backed, ordinal-indexed, slab-backed, or
    primitive-array-backed.
57. Frozen type indexes must be ordinal-friendly.
58. Frozen ordinals must be image-local.
59. Frozen ordinals must be assigned only after deterministic ordering.
60. Frozen ordinals must not enter `TypeReference`.
61. Frozen ordinals must not encode adapter acquisition order.
62. Primitive slab implementation is deferred, but the frozen image contract must not block it.
63. `MetamodelAcquisitionLane<THandle>` owns transitive acquisition for its request scope.
64. Arena slot state transitions must be explicit and enforced.
65. Reflection/KSP equivalence requires golden-vector coverage.
66. Frozen image id is diagnostic/compatibility material, not L2 key authority.
67. All nested frozen record sequences must be deterministic, duplicate-rejecting, and strictly ordered.
68. Frozen sequence local ordinals must be assigned only after deterministic ordering.
69. Frozen sequences must not rely on backend enumeration order.
70. Comparator equality between distinct frozen sequence records fails closed.
71. Reflection and KSP acquisition must produce equivalent frozen sequence keys for the same semantic model, or must
    represent unavailable axes explicitly.
72. Frozen sequence keys must contain semantic identity material only.
73. Metadata availability must be record state, not primary identity key material.
74. Same semantic key with conflicting availability payload fails closed unless a ratified availability merge law
    exists.
75. Records without enough backend-neutral material to form a semantic key must not be inserted into ordinary frozen
    deterministic sequences.
76. Frozen constructor record keys and frozen property record keys must exclude declaration availability.
77. Frozen metadata availability must distinguish backend unavailability, unknown state, unsafe rejection, deterministic
    truncation, policy filtering, and acquisition failure.
78. `TRUNCATED` must represent deterministic framework truncation, not backend absence.
79. `FILTERED_BY_POLICY` must represent policy/scope exclusion, not unsafe material.
80. `ACQUISITION_FAILED` must represent attempted acquisition failure, not normal backend capability absence.
81. Availability values must remain record state and must not participate in primary identity keys.

## 30. Required Tests

Add tests for:

- `TypeReference` does not expose adapter handle fields.
- `TypeReference` does not expose adapter slot or registry ordinal.
- Reflection frozen image contains no `KType`.
- KSP frozen image contains no `KSType`.
- KSP frozen image contains no `KSDeclaration`.
- Frozen records contain no indirect backend recovery keys.
- Frozen records contain no lambda/supplier capturing backend handles.
- Frozen tables reject closure-backed cells in implementation tests.
- Frozen table implementation tests reject lambda-backed records.
- Frozen table implementation tests reject supplier-backed records.
- Frozen table implementation tests reject lazy-delegate-backed records.
- Frozen table implementation tests reject service-locator-backed records.
- Frozen table implementation tests reject callback-backed records.
- Frozen table implementation tests reject registry-key-backed handle recovery.
- Frozen providers do not access backend handles.
- Frozen providers do not receive `FrozenMetamodelImageEnvelope`.
- Frozen providers do not receive `FrozenMetamodelImageDiagnosticHeader`.
- Frozen providers cannot access source adapter provenance.
- FrozenMetamodelImage does not expose source adapter provenance.
- Adapter provenance is diagnostic-only and not semantic equality material.
- Adapter provenance does not influence planning traversal.
- Adapter provenance does not influence type expansion decisions.
- Adapter provenance does not influence L2 key material.
- L2 promotion rejects diagnostic source adapter provenance.
- L2 promotion rejects `FrozenMetamodelImageEnvelope`.
- L2 promotion rejects `FrozenMetamodelImageDiagnosticHeader`.
- L2 promotion rejects closure-backed frozen tables.
- L2 promotion rejects backend handle payloads.
- L2 promotion rejects frozen material produced before freeze-final validation.
- Cycle-hit path does not materialize raw facts.
- Frozen raw fact provider returns `RawTypeFactsResolution.cacheHit(...)`.
- Frozen raw fact record materializes deterministically.
- Frozen provider throws `FrozenMetamodelUnknownTypeReferenceException` when reference is absent from type index.
- Frozen provider throws `FrozenMetamodelIncompleteTableException` when reference exists but table entry is missing.
- Frozen raw fact provider throws `FrozenMetamodelRecordMaterializationException` when a frozen record exists but cannot
  produce DTO.
- Frozen image rejects backend-handle reachability with `FrozenMetamodelBackendReachabilityException`.
- Frozen deterministic sequence duplicate throws `FrozenMetamodelSequenceViolationException`.
- Frozen image schema mismatch throws `FrozenMetamodelImageCompatibilityException`.
- Frozen acquisition write after freeze throws `FrozenMetamodelImageLifecycleException`.
- Freeze is one-shot.
- Writes after freeze fail closed.
- Close before freeze aborts acquisition and produces no image.
- Close after freeze does not invalidate returned image.
- Freeze clears backend handle slots after successful lowering.
- Freeze clears each backend handle slot as soon as the slot is safely lowered.
- Freeze publication occurs only after table coverage validation.
- Freeze publication occurs only after deterministic sequence validation.
- Freeze publication occurs only after backend-handle reachability erasure.
- Freeze pre-counts record counts before frozen table allocation.
- Freeze pre-sizes frozen structures instead of repeatedly growing dynamic collections.
- Freeze lowers acquisition slots directly into frozen table storage in the direct-to-slab path.
- Chunked freeze produces the same frozen image as unchunked freeze for the same semantic input.
- Chunked freeze output is independent of chunk size.
- FrozenMetamodelImage.issue rejects missing shape table coverage.
- FrozenMetamodelImage.issue rejects missing cycle identity table coverage.
- FrozenMetamodelImage.issue rejects missing raw fact table coverage.
- Raw fact sentinel coverage satisfies image coverage validation.
- Shape/cycle table reads do not touch raw fact table material.
- FrozenTypeReferenceIndex exposes deterministic `referenceAt` order.
- FrozenTypeOrdinal is image-local.
- FrozenTypeOrdinal is assigned only after deterministic ordering.
- FrozenTypeOrdinal is not stored in `TypeReference`.
- FrozenTypeOrdinal does not encode adapter acquisition order.
- Reflection and KSP same semantic model produce same `TypeReference`.
- Reflection and KSP same semantic model produce equivalent shape facts.
- Reflection and KSP same semantic model produce equivalent raw facts.
- Reflection and KSP same semantic model produce equivalent metadata availability categories.
- Nested class spelling differences do not silently create divergent identity.
- Generic wildcard/star-projection differences are normalized, represented as unavailable/unknown, or fail closed.
- Frozen image ordering rejects duplicate `TypeReference` entries.
- Frozen image ordering is independent from reflection enumeration order.
- Frozen image ordering is independent from KSP enumeration order.
- Backend handle registry is not used after freeze.
- Transitional reflection registry is not visible to planning domain APIs.
- Frozen image schema version participates in compatibility checks.
- Acquisition slot state machine rejects illegal transitions.
- `MetamodelAcquisitionLane.acquire(...)` owns transitive acquisition for the request.
- Frozen constructor record sequence rejects duplicate constructor keys.
- Frozen constructor parameter sequence rejects duplicate indexes.
- Frozen constructor parameter sequence rejects non-compact indexes.
- Frozen property sequence ordering is independent from reflection enumeration order.
- Frozen property sequence ordering is independent from KSP enumeration order.
- Frozen annotation sequence rejects duplicate non-repeatable annotations.
- Frozen repeatable annotation sequence orders by canonical payload, not backend order.
- Frozen sequence comparator equality fails closed.
- Frozen sequence local ordinals are compact after deterministic ordering.
- Reflection and KSP same semantic model produce equivalent frozen constructor sequences.
- Reflection and KSP same semantic model produce equivalent frozen property sequences.
- Reflection and KSP same semantic model produce equivalent frozen annotation sequences.
- Frozen constructor record key excludes declaration availability.
- Frozen property record key excludes declaration availability.
- Same constructor key with conflicting availability fails closed.
- Same property key with conflicting availability fails closed.
- Constructor record without enough material to form a semantic key is rejected or emitted through an explicit
  availability surface.
- Property record without enough material to form a semantic key is rejected or emitted through an explicit availability
  surface.
- FrozenMetadataAvailability distinguishes `UNAVAILABLE_FROM_BACKEND` from `ACQUISITION_FAILED`.
- FrozenMetadataAvailability distinguishes `REJECTED_UNSAFE` from `FILTERED_BY_POLICY`.
- FrozenMetadataAvailability distinguishes `TRUNCATED` from `UNAVAILABLE_FROM_BACKEND`.
- Same semantic record key with different availability payload fails closed unless an availability merge law is
  ratified.
- `TRUNCATED` availability does not become `TypeReference` identity material.
- `FILTERED_BY_POLICY` availability does not become frozen sequence ordering key material.
- `MetamodelSourceAdapterProvenance` uses structural equality over adapter kind and adapter version.
- `MetamodelSourceAdapterProvenance` equality is used only for diagnostics, compatibility reports, or tests.
- Planning-facing providers do not depend on `MetamodelSourceAdapterProvenance`.
- Planning-facing providers do not branch on `MetamodelSourceAdapterKind`.
- Planning-facing providers do not branch on `sourceAdapterVersion`.
- Adapter-version compatibility is not interpreted by `MetamodelSourceAdapterProvenance` itself.

## 31. Adoption Rule

This ADR is proposed target-state architecture.

While ADR-0039 remains `Proposed`, V1 reflection code may temporarily remain non-compliant.

Such non-compliance must be classified as migration debt.

No new architecture may deepen backend-handle lazy loading.

All new metamodel acquisition work must move toward:

``````text
adapter-specific acquisition
-> adapter-neutral frozen image
-> planning fact-lazy expansion
``````

## 32. Appendix: Reference Pattern Notes

This appendix is non-normative.

Kontrakt's decision aligns with recurring SOTA patterns:

- compiler query systems separate stable keys from computed values;
- context-owned type systems keep unique storage behind stable wrappers;
- build graph systems evaluate immutable key/value nodes through dependency recording;
- optimizer memo systems allow mutable exploration internally but treat registered expressions as planner facts;
- dataflow systems maintain stable facts/changes rather than repeatedly consulting source handles.

The exact implementation details are not imported wholesale.

The architectural lesson is imported:

``````text
external handle lifetime must not leak into core semantic identity
``````

## Final Rule

Adapter-native handles are acquisition inputs.

They are not planning facts.

`TypeReference` is adapter-neutral.

Planning consumes frozen adapter-neutral metamodel material.

Backend-handle lazy loading is transitional debt.

Fact-lazy planning over a frozen metamodel image is the target architecture.