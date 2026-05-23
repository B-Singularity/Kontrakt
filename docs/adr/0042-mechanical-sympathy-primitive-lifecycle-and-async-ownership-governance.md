# ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance

## Status

Proposed

## Date

2026-05-22

## Related

- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline
- ADR-0034: Explicit L2 Join Lifecycle State Machine
- ADR-0035: Deterministic Balanced Lanes for Tier-2 Join Completion Delivery

## 1. Context

Kontrakt intentionally reduces hot-path JVM object allocation by lowering committed metadata, planning, interning,
frozen-image, and execution structures into primitive substrates.

That decision improves locality, predictability, and allocation pressure, but it also transfers lifecycle responsibility
from ordinary JVM object boundaries to explicit Kontrakt lifecycle boundaries.

ADR-0042 governs that responsibility.

It is not a generic performance note.

It is the governance law for where mechanical sympathy is admitted, which domain owns the physical state, how primitive
substrates are published or reclaimed, and how asynchronous ownership boundaries are kept deterministic.

## 2. Problem

Primitive slabs, arenas, packed words, and tables do not carry semantic lifetime by themselves.

The JVM can reclaim unreachable heap arrays, but it does not know:

- which bytes inside a slab are staging-only;
- which offsets are published identity material;
- which reader still observes an old image epoch;
- which lane owns a scratch arena;
- which worker only has temporary execution authority;
- which asynchronous callback is merely an ingestion boundary;
- whether a small slice accidentally pins a huge staging slab.

Without a central law, mechanical sympathy becomes a source of nondeterminism, memory pinning, false sharing, delayed
reclamation, and hidden ownership.

## 3. Decision

Kontrakt SHALL manage mechanical sympathy through explicit domain lifecycle tables, ownership boundaries, and
publication rules.

The following principles are ratified:

- semantic meaning remains separate from physical representation;
- mechanical-sympathy laws are core governance;
- physical substrate implementations are adapter / infrastructure backends;
- committed hot-path state uses primitive-substrate-compatible representations directly;
- transient arenas are reclaimed by deterministic reset / teardown;
- published immutable slabs are reclaimed by epoch retirement;
- reader epoch ownership is engine-lane-owned;
- workers do not own lanes;
- M:N worker/lane topology is dispatch topology, not ownership;
- asynchronous callbacks are event-ingestion boundaries;
- callbacks do not directly mutate canonical identity or lane-owned state;
- Kotlin `value class`, `ThreadLocal`, coroutine-local, virtual-thread-local, scheduler-owned, and worker-owned hidden
  state are not physical authority for committed core state.

### 3.1. Core Law vs Physical Backend Law

ADR-0042 separates mechanical sympathy into two authority layers.

Core-owned law:

- semantic meaning and physical representation are separate;
- canonical bytes, HID derivation, stable intern id assignment, collision verification, and publication laws are
  deterministic;
- hot-path committed state must be compatible with primitive substrates and explicit lifecycle ownership;
- object-graph-heavy committed state is rejected where it becomes identity, planning, interner, frozen-image, or
  execution-lane authority;
- staging, scratch, published, retired, diagnostic, and adapter material have distinct lifecycle classes;
- publication and reclamation require explicit ownership boundaries;
- asynchronous callbacks enter through event ingestion and do not directly mutate lane-owned or canonical state;
- physical acceleration must be observationally equivalent to the deterministic reference pipeline.

Adapter / infrastructure-owned backend:

- JVM heap primitive arrays;
- packed-word table implementations;
- direct byte buffers;
- Java `MemorySegment` layouts;
- off-heap allocators;
- native aligned allocators;
- memory-mapped image stores;
- SIMD / Vector API implementations;
- native digest bindings;
- generated physical layouts;
- cache-line padding mechanisms;
- allocator-specific alignment proofs;
- runtime-profile-specific benchmark evidence.

The core may require a substrate capability.

The core MUST NOT require a specific physical substrate implementation.

Forbidden core dependency shape:

``````text
metadata identity core
-> ByteArray-specific slab implementation
-> exact JVM heap array layout assumption
``````

Forbidden core dependency shape:

``````text
planning core
-> MemorySegment allocator
-> native address arithmetic
``````

Lawful shape:

``````text
core law
-> CanonicalByteSubstrate / PrimitiveTableBackend / DigestSuite port
-> adapter-owned HeapPrimitiveBackend or MemorySegmentBackend or NativeAlignedBackend
-> golden-vector and benchmark evidence
``````

Backend selection may affect:

- latency;
- throughput;
- allocation profile;
- memory bandwidth;
- cache locality;
- physical alignment evidence;
- reclamation implementation details.

Backend selection MUST NOT affect:

- canonical bytes;
- HID derivation;
- collision verification;
- stable intern id assignment;
- canonical ordering;
- semantic equality;
- frozen image identity;
- `PlanCacheKey` equality;
- report manifest identity;
- or public protocol compatibility.

### 3.2. Substrate Port and Backend Vocabulary

ADR-0042 uses the following vocabulary.

`Substrate port` means the core-facing contract for reading, writing, publishing, and reclaiming primitive-compatible
state.

Examples:

- `CanonicalByteSubstrate`;
- `PrimitiveTableBackend`;
- `PublishedImageStore`;
- `ReaderEpochRegistry`;
- `MetadataIdentityDigestSuite`;
- `CanonicalTextValidator`;
- `InternProbeTableBackend`.

`Substrate backend` means the physical implementation selected by infrastructure.

Examples:

- `HeapPrimitiveBackend`;
- `MemorySegmentBackend`;
- `DirectBufferBackend`;
- `NativeAlignedBackend`;
- `MappedImageBackend`;
- `GeneratedLayoutBackend`;
- `VectorizedValidatorBackend`;
- `Blake3DigestSuiteAdapter` or another ratified digest-suite adapter.

A port name may appear in domain or application code.

A backend name must remain in adapter, infrastructure, benchmark, test-fixture, or runtime-profile code unless a later
ADR
explicitly promotes it to protocol material.

The ordinary v1 backend may be JVM heap primitive arrays.

That is an implementation baseline, not a core semantic dependency.

The v2 physical acceleration track may introduce `MemorySegment`, off-heap, native aligned, vectorized, or mapped
backends.

Such backends must be substitutable behind the same core law and must pass cross-backend golden-vector equivalence.

## 4. Domain Mechanical Sympathy and Lifecycle Matrix

| Domain                          | Mechanical sympathy target                                           | Reason                                                          | Substrate port / backend family                           | Lifecycle                                                 | Ownership authority                                 | Reclamation strategy                                            | Async boundary                                |
|---------------------------------|----------------------------------------------------------------------|-----------------------------------------------------------------|-----------------------------------------------------------|-----------------------------------------------------------|-----------------------------------------------------|-----------------------------------------------------------------|-----------------------------------------------|
| Metadata identity               | canonical bytes, HID projections, interner probe tables              | remove object churn, pointer chasing, unstable backend identity | `CanonicalByteSubstrate` / `PrimitiveTableBackend` family | staging -> seal -> publish -> retire                      | identity seal owner / protocol-owned interner scope | scope teardown or published epoch reclamation                   | yes, only for retired published epochs        |
| Frozen metamodel image          | frozen tables, ordinal maps, coverage bitsets, image-local slabs     | avoid object graph traversal at read time                       | `PublishedImageStore` / image-owned substrate backend     | acquisition pass -> image publication -> epoch retirement | frozen image publisher                              | whole-image epoch retirement                                    | yes for old published images                  |
| Planning L1 transient execution | frame stack, work queue, budget ledger, undo log, traversal buffers  | avoid recursive object graph and allocation churn               | run-local / lane-local arena backend                      | run/lane/scope-local                                      | planning run and engine lane                        | deterministic reset / teardown                                  | ordinarily no                                 |
| Planning published artifacts    | sealed plan graph, shared planning snapshot, cacheable plan material | share immutable planning material without copying per reader    | published artifact substrate backend                      | build -> publish -> retire                                | planning artifact publisher                         | reader-epoch reclamation or equivalent                          | yes                                           |
| L2 interner/cache               | slot state, waiter state, builder state, route/probe tables          | deterministic lifecycle terminalization and cache-local probing | packed-state / probe-table backend                        | slot/shard lifecycle                                      | slot terminalization authority / owning lane        | shard rebuild, scope teardown, or epoch retire where applicable | yes: callback -> event -> owner transition    |
| VM / execution                  | generated verification cases, fixture buffers, lane-local state      | keep verification loop allocation-free                          | lane-local execution substrate backend                    | verification-run / lane-local                             | execution lane                                      | teardown / cursor reset                                         | ordinarily no                                 |
| Reporting / diagnostics         | bounded evidence bytes, sanitized failure records                    | avoid OOM during failure reporting                              | bounded report buffer backend                             | failure/report scope                                      | reporting sink                                      | report lifecycle                                                | async sink possible, never identity authority |
| Adapters                        | public DTOs, framework handles, external callbacks                   | isolate framework nondeterminism                                | ordinary JVM objects allowed on cold boundary             | adapter request                                           | adapter boundary                                    | JVM GC / adapter lifecycle                                      | callback ingestion only                       |

Matrix terminology note:

`Substrate port / backend family` names the lawful substrate capability and ownership family.

It does not require that domain or application core code import a concrete heap-array, `MemorySegment`, off-heap,
native,
or mapped implementation.

Concrete physical backends are selected by infrastructure/runtime profile and must remain observationally equivalent to
the core law.

## 5. Primitive Substrate Lifecycle Taxonomy

ADR-0042 distinguishes the following substrate lifecycles.

| Substrate class          | Examples                                                              |   May be mutable? |     May be published? | Reset/reclaim rule                                               |
|--------------------------|-----------------------------------------------------------------------|------------------:|----------------------:|------------------------------------------------------------------|
| Staging slab             | temporary canonical bytes, decoder workspace                          |  yes, before seal |                    no | discard, copy/compact, migrate, or zero-copy promote under proof |
| Scratch slab             | sort keys, tie-group ranges, preflight buffers                        |               yes |                    no | scope/lane/run teardown or cursor reset                          |
| Run-local planning arena | frames, queues, ledgers, undo logs                                    |               yes |                    no | planning run teardown / reset                                    |
| Lane-local arena         | repeated lane execution buffers                                       |               yes |                    no | lane epoch/run reset, never semantic authority                   |
| Published sealed slab    | frozen image bytes, metadata image bytes, published planning snapshot |                no |                   yes | immutable until epoch retirement                                 |
| Retired epoch slab       | old published image still visible to readers                          |                no |       already retired | reclaimed only after reader epoch safety                         |
| Diagnostic buffer        | failure evidence, report snippets                                     | yes within budget |     report-owned only | report lifecycle and diagnostic byte budget                      |
| Adapter DTO              | external API/callback material                                        |               yes | no identity authority | JVM/adapter lifecycle                                            |

Lifecycle taxonomy note:

The lifecycle class is core law.

The concrete storage vehicle is backend law.

For example, a `Published sealed slab` may be backed by a JVM heap primitive array in v1, a `MemorySegment` in v2, a
mapped image in a persistent profile, or a native aligned allocation in a platform-specific profile.

All of those backends must expose the same publication, immutability, bounds, and reclamation semantics to the core.

## 6. Ownership and Authority Vocabulary

| Term                           | Meaning                                                       | Authority boundary                                |
|--------------------------------|---------------------------------------------------------------|---------------------------------------------------|
| `EngineLaneId`                 | Kontrakt-owned deterministic logical execution lane           | owns lane-local state and lane epoch slot         |
| `LaneExecutionLease`           | temporary authority for a worker to execute one lane          | execution authority, not memory ownership         |
| `ReaderEpochGuard`             | read-side guard that pins observed published epoch            | memory reclamation guard, not execution authority |
| `LaneEpochSlot`                | lane-owned epoch record                                       | written only by current lane execution authority  |
| `WorkerLaneTopology`           | resolved physical dispatch topology between workers and lanes | dispatch topology, not ownership                  |
| `AsyncReclaimer`               | physical owner that discovers reclaimable retired epochs      | cannot mutate canonical identity                  |
| `ArtifactPublisher`            | owner of immutable artifact publication                       | publishes new epoch, retires old epoch            |
| `SlotTerminalizationAuthority` | owner of L2 lifecycle terminalization                         | uses CAS/state machine where needed               |

## 7. Governance Rules

### 7.1. Semantic Wrapper, Substrate Port, and Backend Separation Law

Semantic wrappers MAY exist only at cold API, diagnostic, test, and documentation boundaries.

Kotlin `value class` MUST NOT be used as the physical representation of committed identity, planning, metadata,
interner, frozen image, sort, SCC, or execution-lane state.

Reason:

Kotlin value classes do not provide a stable physical representation guarantee across nullable use, generics, arrays,
interface dispatch, reflection, boxing, or JVM lowering boundaries.

Committed hot-path state MUST use primitive-substrate-compatible material directly:

- bytes;
- integer words;
- packed primitive words;
- offsets;
- lengths;
- stable integer ordinals;
- sealed slab ids;
- published image ids;
- explicit handles whose backing substrate is owned by a substrate backend.

Concrete storage examples include, but are not limited to:

- JVM heap `ByteArray`;
- JVM heap `IntArray`;
- JVM heap `LongArray`;
- direct buffer storage;
- Java `MemorySegment`;
- off-heap storage;
- native aligned storage;
- mapped image storage;
- generated physical layouts.

These concrete storage examples are backend choices.

They are not semantic authority.

They MUST NOT be imported as required physical types by metadata identity, planning, interner, frozen-image, or
execution
domain law unless a later ADR explicitly ratifies a backend-specific runtime profile.

Core-facing code SHOULD depend on substrate ports or explicit primitive-compatible handles.

Allowed core-facing vocabulary includes:

- `CanonicalByteSubstrate`;
- `CanonicalBytesView`;
- `CanonicalByteWriter`;
- `PrimitiveTableBackend`;
- `PublishedImageStore`;
- `ReaderEpochRegistry`;
- `InternProbeTableBackend`;
- `MetadataIdentityDigestSuite`.

Forbidden ordinary core shape:

``````kotlin
class MetadataIdentitySealService(
    private val byteArraySlab: ByteArray,
)
``````

Forbidden ordinary core shape:

``````kotlin
class PlanningFrameStore(
    private val segment: java.lang.foreign.MemorySegment,
)
``````

Lawful shape:

``````kotlin
class MetadataIdentitySealService(
    private val canonicalBytes: CanonicalByteSubstrate,
    private val digestSuite: MetadataIdentityDigestSuite,
)
``````

Lawful shape:

``````text
core service
-> substrate port
-> runtime-profile-selected backend
-> golden-vector equivalence
``````

The v1 heap primitive implementation is allowed.

The v2 `MemorySegment`, off-heap, native-aligned, vectorized, or mapped implementation is allowed.

Neither implementation family may change canonical bytes, HID derivation, collision verification, stable intern id
assignment, canonical ordering, semantic equality, or publication law.

### 7.2. Transient Arena Reclamation Law

Run-local, lane-local, worker-local, scope-local, and scratch arenas are not published artifacts.

They MUST be reclaimed by deterministic reset, cursor reset, scope teardown, run teardown, lane teardown, or
adapter-owned
lifecycle cleanup.

They MUST NOT require reader-epoch reclamation unless they become published immutable artifacts.

### 7.3. Published Immutable Slab Epoch Law

Published slabs grow by rebuild-and-republish, not by in-place mutation.

A larger successor slab is built as a new epoch, verified, atomically published, and the previous epoch is retired.

The retired epoch is reclaimed only after no explicit lane reader epoch slot or adapter-owned reader slot can observe
it.

### 7.4. Engine-Owned Lane EBR Law

Reader epoch reclamation uses lane-local EBR.

It is not `ThreadLocal` EBR.

Lane epoch slots MUST be:

- engine-owned;
- lane-owned;
- single-writer under `LaneExecutionLease`;
- scanned asynchronously by the reclaimer;
- padded or otherwise isolated to reduce false sharing where implementation evidence supports the claim.

The ordinary read hot path MUST NOT mutate a global reader list, acquire a global lock, or increment/decrement a
globally
contended reader counter.

### 7.5. Async Event Ingestion Law

Asynchronous callback is not an execution authority.

Asynchronous callback is an event ingestion boundary.

The lawful shape is:

``````text
async source
-> explicit event record
-> bounded engine-owned queue
-> deterministic lane / maintenance owner
-> explicit transition
``````

Callbacks MUST NOT directly mutate lane-owned state, planning state, interner state, reader epoch slots, or canonical
identity material.

### 7.6. Mechanical Optimization Evidence Law

A release MAY claim a physical optimization only with evidence appropriate to the JVM/runtime target.

Examples:

- JVM heap primitive arrays may claim logical grouping, not exact cache-line alignment;
- exact alignment requires off-heap, `MemorySegment`, generated layout, native aligned allocation, or runtime-specific
  evidence;
- small-inline or branch-bounded hot paths require benchmark gates;
- daemon-safe slab reuse requires leak / repeated-run tests;
- async epoch reclamation requires slow-reader / pinned-epoch tests;
- a new physical substrate backend requires cross-backend golden-vector equivalence against the reference backend.

Evidence must distinguish:

``````text
semantic equivalence:
    same canonical bytes / HID / equality / stable ids

physical effect:
    lower allocation, better locality, lower cache misses, lower latency, or better throughput
``````

A backend that improves the physical effect but fails semantic equivalence is non-compliant.

### 7.7. Core Dependency Rejection Law

Core, domain, and application services governed by ADR-0042 MUST NOT depend on concrete physical backend classes as
semantic collaborators.

Forbidden imports in core/domain/application layers include, unless explicitly isolated behind a port adapter or test
fixture:

- `java.lang.foreign.MemorySegment`;
- `java.nio.ByteBuffer` / `DirectByteBuffer`;
- `sun.misc.Unsafe`;
- native allocator bindings;
- JNI / FFI allocator handles;
- concrete digest-library classes;
- concrete SIMD / Vector API validator classes;
- mapped-file implementation classes;
- backend-specific aligned allocation utilities.

Primitive array types may appear inside v1 heap substrate adapters and low-level implementation packages.

They SHOULD NOT become constructor parameters, public domain fields, or semantic authority in domain services.

Allowed core/domain/application dependencies:

- protocol ids;
- canonical offsets and lengths;
- stable ordinals;
- sealed handle ids;
- substrate ports;
- immutable policy snapshots;
- explicit lifecycle state;
- explicit ownership guards.

Architecture tests SHOULD enforce:

- domain packages do not import backend-native allocator classes;
- metadata identity domain services do not import concrete digest libraries;
- planning domain services do not import `MemorySegment`, direct buffers, native allocators, or concrete heap-slab
  implementations;
- physical backend implementations live under infrastructure, adapter, runtime-profile, benchmark, or test-fixture
  packages;
- cross-backend implementations pass the same golden vectors.

This law does not forbid efficient implementation.

It prevents efficient implementation from becoming semantic architecture.

## 8. Extracted Generic Lifecycle Laws from ADR-0041

The following laws were moved out of ADR-0041 because they govern primitive lifecycle and asynchronous ownership across
multiple domains, not only metadata identity.

### 8.10.3. Zero-Copy Slice Lifetime and Slab Pinning Law

Zero-copy slice views are decode-stage artifacts.

A zero-copy slice derived from a staging slab MUST NOT cross into:

- `FrozenMetamodelImage`;
- planning-facing providers;
- `PlanCacheKey`;
- `CanonicalPlanNode`;
- protocol-owned interner published tables;
- persistent artifacts;
- public DTOs;
- report manifests.

Zero-copy is a hot decode optimization.

It is not a lifetime ownership model.

If decoded payload material must survive the seal boundary, it MUST be copied, compacted, migrated, or promoted under
one
of the ratified publication paths in this section.

The ordinary lawful shape is:

``````text
staging slab
-> bounded zero-copy decode slice
-> verification / seal
-> compact published slab or verified handle
-> staging slab becomes unreachable
``````

The forbidden shape is:

``````text
staging slab
-> tiny slice view
-> stored in frozen image / planning / report / interner
-> entire staging slab pinned
``````

Architecture tests alone are not sufficient to prove runtime slab ownership.

ADR-0041 therefore requires type-state separation between staging and published slices.

A staging-slab slice MUST be represented by a staging-only type such as:

``````text
StagingSlice
``````

A published byte slice MUST be represented by a published-only type such as:

``````text
PublishedSlice
``````

or by a verified canonical byte handle with equivalent type-state guarantees.

Publication APIs MUST accept only published-slab slices, sealed canonical byte handles, stable intern ids, or
frozen-image
owned handles.

They MUST NOT accept staging-slab slice types.

A `StagingSlice` MUST NOT implement or alias the same publication interface as `PublishedSlice` unless the type system
also carries an unforgeable ownership/provenance state that distinguishes staging from published memory.

If a slice crosses a publication boundary, it MUST carry proof that its base slab is owned by the published artifact and
not by a transient staging phase.

### 8.10.4. Zero-Copy Promotion and Seal Ownership Transfer Law

Copy-on-seal is the default safe publication path.

However, if a staging slab is already a fully packed canonical byte artifact, ADR-0041 permits zero-copy promotion.

Zero-copy promotion means:

``````text
staging slab ownership
-> write closed
-> fully validated
-> transferred as published sealed slab
``````

It does not mean that arbitrary staging memory may be retained as published identity material.

A staging slab MAY be promoted to a published sealed slab only when all of the following hold:

- the staging slab has exactly one owner;
- no writer can mutate the slab after the seal point;
- no `StagingSlice` can outlive the promotion boundary;
- all live slices covering promoted bytes are converted to `PublishedSlice` or verified canonical byte handles;
- the promoted byte region is fully initialized;
- the promoted byte region contains only canonical bytes for the published artifact;
- unused capacity is either zero, absent, or bounded and charged to the published artifact under resolved policy;
- the promoted region satisfies payload-offset, zero-displacement, and bounds-validation laws;
- the promoted region satisfies the owning schema/version compatibility laws;
- the promotion assigns a sealed slab id, image id, or equivalent immutable owner;
- and promotion is atomic with respect to publication.

A staging slab MUST NOT be promoted if it contains:

- unrelated transient decode material;
- rejected candidates;
- abandoned scratch ranges;
- mutable parser workspace;
- worker-local garbage;
- diagnostic-only temporary bytes;
- or unused capacity that would pin excessive memory outside the resolved published budget.

If the promotion proof fails, the implementation MUST fall back to copy/compact/migrate or fail the current publication
scope closed.

Zero-copy promotion is a physical optimization.

It MUST NOT change canonical bytes, HID derivation, collision verification, stable intern id assignment, or semantic
equality.

A released implementation claiming zero-copy promotion MUST publish tests or benchmark evidence for:

- single-owner proof;
- write-closed proof;
- no staging-slice escape;
- published-slice conversion;
- unused-capacity budget accounting;
- and fallback to copy/compact when promotion is unsafe.

### 8.10.5. Sealed Slab Fragmentation and Epoch Reclamation Law

Published canonical byte slabs are immutable.

A sealed slab MUST NOT be compacted in place.

A sealed slab MUST NOT be modified by a background defragmentation thread.

A sealed slab MUST NOT have live offsets rewritten after publication.

Reason:

``````text
background defrag
-> moving bytes
-> rewriting offsets / handles
-> reader-visible race or identity drift
``````

Fragmentation control is handled by ownership and epoch boundaries, not by in-place moving compaction.

Lawful shapes:

``````text
run-local staging slab
-> seal / compaction / promotion
-> image-owned sealed slab
-> image epoch retired as a whole
``````

``````text
old sealed image
-> build new sealed image with compacted layout
-> exact verification
-> atomic publication of new image epoch
-> old image epoch reclaimed only after no valid reader lease can observe it
``````

Forbidden shapes:

``````text
published sealed slab
-> background defrag moves bytes
-> offsets patched in place
``````

``````text
stable intern id
-> raw mutable pointer into moving slab
``````

A published reference to canonical bytes MUST be one of:

- image id + stable offset + length;
- sealed slab id + stable offset + length;
- verified canonical byte handle;
- stable intern id that resolves through immutable published tables.

It MUST NOT be a raw mutable pointer that can be rewritten by defragmentation.

If long-running processes need memory reclamation, Kontrakt MUST use:

- scope-local slab teardown;
- image-epoch retirement;
- whole-slab reclamation;
- bounded rebuild / republish;
- adapter-owned lifecycle cleanup;
- or reader-lease enforcement as defined below.

It MUST NOT use unsynchronized in-place defragmentation of identity-bearing slabs.

Fragmentation evidence belongs in release-readiness benchmarking and daemon-hygiene tests.

### 8.10.6. Reader Lease and Epoch Pin Budget Law

Epoch reclamation is an availability boundary.

An old sealed slab epoch cannot be reclaimed while a valid reader may still observe it.

However, readers also cannot pin old epochs indefinitely without bounded accounting.

A compliant implementation MUST represent published-slab reads through an explicit reader lease, lane epoch guard, or
equivalent engine-owned lane read handle.

A reader lease MUST record, or be derivable from:

- reader epoch id;
- image epoch id;
- sealed slab id or image id;
- owning `EngineLaneId`;
- active `LaneExecutionLease` where a worker is currently executing the lane;
- acquisition sequence;
- optional acquisition time where an explicit wall-clock runtime policy is admitted;
- and closed / released state.

A reader lease MUST NOT be represented by:

- `ThreadLocal`;
- untracked raw byte-array reference;
- raw memory pointer;
- unbounded `PublishedSlice` escape;
- coroutine-local state;
- fiber-local state;
- virtual-thread-local state;
- OS scheduler state;
- JVM executor state;
- work-stealing queue state;
- callback-local state;
- or process-global parser/reader state.

A resolved runtime or adapter policy MUST define bounded epoch pinning using one or more of:

- maximum pinned epoch count;
- maximum pinned sealed bytes;
- maximum active reader lease count;
- maximum reader lease duration where wall-clock policy is explicitly admitted;
- maximum generation gap between current image epoch and oldest pinned image epoch;
- maximum lane-local pinned epoch count;
- maximum lane-local pinned bytes;
- maximum active `LaneExecutionLease` count;
- maximum retired epoch queue length;
- or a deterministic lane safe-point protocol.

#### 8.10.6.1. Engine-Owned Lane Epoch Table Law

The protocol owner of a reader epoch is the engine lane.

A lane is a Kontrakt-owned logical execution unit selected by deterministic lane assignment and resolved runtime policy.

A lane is not:

- an OS thread;
- a JVM executor task;
- a coroutine;
- a virtual thread;
- a work-stealing task;
- a callback;
- or a scheduler-owned resource.

The term "scheduler" MUST NOT be used as an ownership authority in this law.

The owner is the Kontrakt engine.

The state owner is the lane.

A worker is a physical execution agent.

A worker may execute work for one or more lanes only through a resolved `WorkerLaneTopology`.

The worker/lane relationship is a physical execution topology.

It is not semantic identity.

It MUST NOT affect:

- canonical bytes;
- HID derivation;
- canonical ordering;
- stable intern id assignment;
- collision verification;
- diagnostics category;
- image digest;
- or report identity.

`WorkerLaneTopology` MAY be M:N.

This means:

``````text
many workers may service many lanes through deterministic assignment windows
``````

It does not mean:

``````text
workers own lanes
``````

The lane remains the protocol owner of lane-local epoch state.

A worker receives only temporary execution authority through `LaneExecutionLease`.

The reader epoch slot is lane-owned engine state.

A worker may operate on that slot only while holding an explicit `LaneExecutionLease` for that lane.

The preferred lawful shape is:

``````text
EngineLaneId
-> engine-owned LaneEpochTable
-> lane-owned LaneEpochSlot
-> worker obtains LaneExecutionLease for that lane
-> release-store observed image epoch id / sealed slab epoch id
-> read immutable sealed slab
-> release-store EMPTY / released state
-> release LaneExecutionLease
``````

For M:N worker/lane execution topology, the lawful shape is:

``````text
engine-owned WorkerLaneTopology
-> WorkerId
-> deterministic assignment window
-> selected EngineLaneId
-> LaneExecutionLease
-> lane-owned LaneEpochSlot
-> immutable sealed slab read
-> lane-owned release
-> release LaneExecutionLease
``````

The topology may be:

- one worker to one lane;
- one worker to many lanes through a deterministic lane set;
- many workers to many lanes through deterministic assignment windows;
- or another ratified topology with the same ownership law.

Regardless of topology, at most one execution authority may mutate one lane-owned epoch slot at a time.

Concurrent readers of the same sealed slab through different lanes use different lane-owned epoch slots.

Reader lease acquisition MUST be hot-path bounded.

A compliant implementation MUST NOT use `ThreadLocal` as the ownership authority for reader epochs.

Reason:

``````text
ThreadLocal
-> hidden ambient state
-> thread reuse / worker migration / test-runner reuse hazard
-> ownership not visible in protocol or engine lane state
``````

The epoch reclaimer MAY asynchronously scan the engine-owned `LaneEpochTable`, topology-indexed lane epoch tables, or
adapter-owned explicit reader-slot tables to decide which retired epochs are reclaimable.

This asynchronous reclaimer is a physical memory-management component.

It is not a semantic participant in canonical identity.

It MUST NOT change:

- canonical bytes;
- HID derivation;
- collision verification;
- stable intern id assignment;
- canonical ordering;
- semantic equality;
- or report identity.

Forbidden ordinary hot-read shape:

``````text
every read
-> acquire global lock
-> mutate global reader list
-> increment contended global counter
-> read
-> decrement contended global counter
``````

Also forbidden:

``````text
reader epoch ownership
-> hidden ThreadLocal
-> implicit thread-affinity assumption
``````

``````text
reader epoch ownership
-> JVM executor scheduling
-> callback mutates lane slot directly
``````

``````text
worker owns lane
-> worker-local slot becomes reader epoch authority
``````

A centralized lease manager MAY exist only for:

- cold diagnostics;
- administrative draining;
- shutdown;
- test instrumentation;
- adapter fallback;
- or non-hot public API compatibility.

It MUST NOT be required for the ordinary published-slab read hot path unless benchmark evidence proves that the selected
runtime profile tolerates the contention.

Reader epoch records are runtime ownership metadata.

They are not canonical identity material.

They MUST NOT be encoded into canonical bytes, `PlanCacheKey`, `CanonicalPlanNode`, frozen image identity, report
manifest identity, or persistent artifacts.

#### 8.10.6.2. Event-Ingestion and Async Owner Boundary Law

Kontrakt may use asynchronous physical components.

Asynchronous components are not semantic authorities.

External callbacks, adapter completions, retired-epoch notifications, timeout signals, and reclaimer notifications MUST
NOT directly mutate lane-owned execution state or canonical identity state.

If an asynchronous source must interact with the engine, it MUST be converted into an explicit event record and
delivered
to one of:

- the owning deterministic engine lane;
- a bounded engine-owned maintenance queue;
- a bounded epoch-reclaimer queue;
- or an adapter-owned ingress queue that later hands off to an engine-owned queue.

The callback is an ingestion boundary.

The lane, maintenance owner, or reclaimer owner is the transition authority.

Lawful shape:

``````text
external callback / async completion
-> explicit event record
-> bounded engine-owned queue
-> owning lane or maintenance owner consumes event
-> legal transition
``````

Forbidden shape:

``````text
external callback / async completion
-> directly mutate LaneEpochSlot
-> directly publish identity
-> directly resume planning state
``````

This law matches the broader Kontrakt asynchronous discipline:

``````text
async source
-> event record
-> deterministic lane / owner
-> explicit transition
``````

It is the same ownership pattern used by L2 completion delivery, planning-run resumption, acquisition readiness, and
bounded reporting handoff.

M:N delivery, where used, is a dispatch topology.

It is not state ownership.

Dispatch may choose the lane or queue according to a deterministic mapping.

Only the owning lane or owning maintenance component may perform the state transition.

#### 8.10.6.3. Non-Suspending Lane Lease Boundary Law

ADR-0041 reader-lease hot paths are non-suspending.

A lane holding a published-slab epoch lease MUST NOT suspend, park, yield into an unbounded scheduler, perform external
blocking work, or cross into an adapter callback before releasing the lease.

Kontrakt core MUST NOT rely on Kotlin coroutine suspension for reader-lease lifetime management.

Coroutine-based lease ownership is forbidden.

A reader lease MUST be released before:

- network I/O;
- file I/O not owned by the current bounded verification phase;
- database calls;
- subprocess execution;
- user callbacks;
- framework callbacks;
- logging sinks that may block;
- debugger suspension points where the runtime policy treats them as blocking;
- monitor waits;
- virtual-thread parking;
- work-stealing migration;
- adapter scheduler handoff;
- event-loop handoff;
- or any adapter callout that is not under the current Kontrakt bounded execution policy.

Lawful shape:

``````text
enter explicit engine lane
-> acquire lane reader lease
-> read immutable sealed slab
-> copy, decode, or retain only non-pinning semantic result
-> release lane reader lease
-> leave lane / enter external boundary
-> reacquire through explicit lane if the sealed slab must be read again
``````

Forbidden shape:

``````text
acquire reader lease
-> hold PublishedSlice / raw byte reference
-> suspend / park / block / call out
-> old epoch remains pinned indefinitely
``````

Lease suspension as an implicit coroutine or fiber operation is forbidden.

A lease may be explicitly released and later reacquired.

A lease may be explicitly detached only if the detached state does not retain raw published-slab reachability and is
bounded by resolved adapter policy.

A detached adapter record MUST NOT contain `PublishedSlice`, raw byte references, mutable slab pointers, hidden reader
epoch ownership, or lane-owned epoch state.

A reader that cannot release before an external boundary MUST fail closed, cancel the owning operation, or move the
external operation outside the leased region.

#### 8.10.6.4. Asynchronous Reclaimer Isolation Law

The epoch reclaimer MAY run asynchronously.

Its job is to discover reclaimable retired epochs by scanning explicit engine-owned lane epoch records and adapter-owned
reader-slot records.

It MUST NOT rewrite published slab offsets.

It MUST NOT move bytes inside a published sealed slab.

It MUST NOT invalidate an active reader lease without a safe cancellation boundary.

It MUST NOT participate in canonical ordering, HID derivation, stable intern id assignment, collision verification, or
planning key construction.

A lawful asynchronous reclamation shape is:

``````text
publisher retires old image epoch
-> reclaimer scans engine-owned LaneEpochTable
-> if no lane-owned slot or adapter-owned reader slot can observe old epoch, old sealed slabs become reclaimable
-> reclamation releases whole-slab / whole-image ownership
``````

Forbidden asynchronous reclamation shape:

``````text
reclaimer observes slow reader
-> frees slab anyway
-> reader still holds raw bytes
``````

``````text
reclaimer compacts published slab
-> rewrites offsets
-> readers observe mixed epoch state
``````

The reclaimer may throttle new epoch publication when pinned-epoch budgets are exhausted.

Such throttling is availability policy.

It MUST NOT change canonical identity for already admitted material.

#### 8.10.6.5. Slow Reader Containment Law

If a reader exceeds the resolved lease policy, the implementation MUST apply one of the following deterministic
containment outcomes:

- fail fast at a lane safe point;
- cancel the owning lane operation at a cancellation boundary;
- reject further access through that reader lease;
- quarantine the owning adapter request before it enters a new canonical identity scope;
- throttle new epoch publication under bounded policy;
- or fail the current publication attempt closed.

The chosen outcome MUST be selected by resolved runtime or adapter policy.

It MUST NOT depend on:

- live heap pressure;
- thread scheduling;
- worker completion order;
- random fallback;
- coroutine scheduler behavior;
- virtual-thread scheduling behavior;
- JVM executor scheduling behavior;
- work-stealing queue behavior;
- or unratified runtime profiling.

Lawful slow-reader handling:

``````text
reader lease exceeds policy
-> lane fails at safe point / owning operation is cancelled
-> lease released
-> epoch becomes reclaimable
``````

Lawful publication throttling:

``````text
pinned epoch budget exhausted
-> new epoch publication is deferred or rejected
-> no published slab is reclaimed while unsafe readers can observe it
``````

Forbidden slow-reader handling:

``````text
reader still has raw pointer
-> epoch bytes freed or repurposed
-> reader observes moved / reused identity bytes
``````

New epoch publication MAY be throttled, deferred, or failed closed if the pinned-epoch budget is exhausted.

Such throttling is an availability decision.

It MUST NOT change canonical bytes, HID derivation, collision verification, stable intern id assignment, or semantic
equality for admitted material.

A released implementation claiming daemon-safe long-running behavior MUST provide tests or profiling evidence for:

- reader lease release on normal completion;
- reader lease release on failure;
- reader lease release before external I/O and external callouts;
- cancellation / safe-point handling for slow readers;
- maximum pinned epoch count;
- maximum pinned sealed bytes;
- maximum active reader lease count;
- maximum lane-local pinned bytes;
- maximum generation gap;
- repeated rebuild / republish without unbounded epoch accumulation;
- absence of staging-slab pinning through published slices;
- asynchronous reclaimer behavior under slow-reader pressure;
- M:N worker/lane topology where ratified;
- rejection of worker-owned reader epoch authority;
- rejection of `ThreadLocal` reader epoch authority;
- rejection of coroutine / virtual-thread reader lease ownership;
- and rejection of scheduler-owned reader epoch authority.

## 9. Related Mechanical Laws Still Referenced by ADR-0041

ADR-0041 keeps metadata-identity-specific laws.

ADR-0042 references the following ADR-0041 law families because they express domain-specific mechanical requirements for
metadata identity and interning.

### 3.2. Physical Optimization Rejection Rule

Any optimization MUST be rejected if it makes identity depend on:

- CPU topology;
- NUMA topology;
- SIMD width;
- hardware prefetch behavior;
- branch predictor behavior;
- thread scheduling;
- worker assignment;
- callback completion timing;
- memory address;
- object allocation order;
- cache state;
- backend enumeration order;
- acquisition order;
- hash table probe order;
- local arena insertion order;
- queue arrival order;
- physical table rebuild timing;
- JIT compilation timing;
- GC timing.

Performance is permitted to vary.

Identity is not.

### 3.3. Physical Acceleration Admission Rule

Physical acceleration may optimize candidate discovery, data movement, cache locality, and byte comparison.

It MUST NOT move semantic publication ahead of verification.

Allowed physical acceleration includes, when observational equivalence is proven:

- SIMD-compatible group probing;
- branch-minimal tag decoding;
- table-driven encoder / decoder dispatch;
- vectorized exact byte comparison;
- prefetch-aware slab layout;
- NUMA-local or lane-local staging;
- streaming candidate accumulation;
- query-key / incremental fingerprint preparation;
- provisional non-semantic handles.

Forbidden acceleration includes:

- digest-only equality;
- HID-only equality;
- stable intern id publication before verification and seal;
- planning-visible publication before exact identity verification;
- L2 exact-match publication before exact key verification;
- background verification that retroactively repairs already-published semantic identity;
- adaptive physical behavior that changes identity output inside an already-admitted scope.

The rule is:

``````text
Speculation may prepare.
Speculation may not publish semantic identity.
``````

---

### 8.10.2. Zero-Copy Canonical Byte Slice Law

Variable payload extraction on hot identity decode paths MUST return a bounded view over the immutable canonical byte
slab.

Hot decoders MUST NOT allocate new byte arrays for ordinary field extraction.

Allowed hot-path representation:

- immutable base slab reference plus offset and length;
- primitive offset / length pair carried with an already-known base;
- verified canonical byte slice handle;
- small-inline word payload when the full field fits inside the hot metadata plane.

Forbidden on hot identity paths:

- `Arrays.copyOfRange`;
- `ByteArrayOutputStream` materialization;
- per-field `ByteArray` allocation;
- per-field heap wrapper allocation merely to carry offset/length;
- `String` reconstruction unless the field is explicitly decoded as a string value at a ratified boundary.

Cold diagnostic paths MAY copy bounded payload slices when the diagnostic policy permits it.

Such copies are not canonical identity material and remain subject to diagnostic evidence budgets.

### 13.8. Intern Table Physical Layout Law

Protocol-owned intern tables are physical identity infrastructure.

A compliant high-performance implementation SHOULD group first-probe metadata into 64-byte logical bucket groups.

The first probe of an intern table entry SHOULD load enough metadata to reject almost all non-equal candidates without
chasing pointers into canonical byte payload slabs.

At minimum, the first-probe group SHOULD contain:

- HID bits;
- stable intern id or candidate id;
- identity domain / schema / version metadata;
- canonical byte length;
- canonical byte slab offset or compact verifier handle;
- inline verifier prefix;
- state / generation / occupancy metadata.

The implementation MUST NOT require a full canonical byte comparison immediately after every HID match.

Required verification order:

``````text
HID / route pre-screen
-> domain and version check
-> canonical length check
-> inline verifier prefix check
-> full canonical byte comparison only if all inline checks pass
``````

Full canonical byte comparison remains the final equality authority, but it must be a rare-path verification step rather
than the ordinary first response to a compact identity match.

### 13.9. Cache-Line Grouping Law

The target physical shape for hot intern-table probing is a cache-line-oriented entry group.

A compliant JVM implementation may not be able to guarantee exact physical 64-byte alignment for ordinary heap objects.
Therefore this ADR defines a logical cache-line grouping law rather than a strict object-address law.

Heap primitive arrays are the portable v1 baseline.

Off-heap, direct-memory, generated, or otherwise explicitly aligned probe groups are optional advanced physical
backends,
not mandatory v1 compliance requirements.

A release claiming exact 64-byte physical alignment MUST prove that claim with layout documentation and benchmark
evidence for the target runtime.

Required law:

- first-probe metadata MUST be stored contiguously;
- first-probe metadata MUST be primitive-friendly;
- first-probe metadata MUST avoid object graphs where practical;
- first-probe metadata MUST avoid pointer chasing before inline rejection fails;
- entry groups SHOULD be sized and padded so that a small number of cache lines covers the ordinary probe decision.

Preferred layouts:

- struct-of-arrays with parallel primitive arrays;
- array-of-struct-of-arrays groups;
- off-heap / direct-memory fixed-size bucket groups where the runtime policy allows it;
- generated primitive table layouts for persistent image indexes;
- padded heap objects only when validated by allocation and cache-miss benchmarks.

JVM heap primitive arrays do not by themselves prove physical cache-line alignment.

A `LongArray`, `IntArray`, or `ByteArray` may have an object header, runtime-specific base offset, compressed-oops
layout,
alignment padding, and GC relocation behavior.

Therefore, a JVM heap-array implementation MUST treat 64-byte grouping as a logical probe grouping objective, not as a
guaranteed physical cache-line alignment claim.

A release claiming exact physical cache-line grouping MUST provide one of:

- an off-heap / direct-memory layout with explicit alignment proof;
- a Java `MemorySegment` / native memory layout with explicit base-address alignment proof;
- generated persistent image layout with documented alignment;
- or runtime-specific layout evidence and benchmarks.

Physical alignment evidence MUST include:

- base-address alignment;
- entry stride;
- padding rule;
- cache-line split measurement or equivalent benchmark evidence;
- false-sharing analysis for concurrent lanes;
- and target JVM / OS / architecture assumptions.

If such evidence is absent, documentation MUST say "logical grouping" rather than "cache-line aligned".

Forbidden hot-path shape:

``````text
HID match
-> object reference
-> wrapper object
-> byte array object
-> offset object
-> metadata object
-> finally compare bytes
``````

The interner probe path must be physically boring: a few primitive loads, predictable comparisons, and rare full
verification.

### 13.11. Pointer-Chasing Deferral Law

Intern-table lookup MUST defer pointer chasing into cold canonical byte slabs until all inline rejection checks have
passed.

Required ordinary probe order:

1. read occupancy / state;
2. compare HID or route bits;
3. compare identity domain;
4. compare schema / encoding / derivation version material;
5. compare canonical byte length;
6. compare inline verifier prefix;
7. only then load canonical byte slab payload for exact verification.

This law exists because mandatory collision verification must not turn ordinary HID matches into systematic
memory-latency amplification.

### 13.17. Verification Ladder Law

Intern-table candidate verification MUST be staged from cache-local metadata to cold payload verification.

Required ordinary verification order:

``````text
1. occupancy / state check
2. HID word comparison
3. identity domain check
4. version bundle fingerprint check
5. canonical byte length check
6. inline verifier prefix check
7. inline verifier suffix or secondary verifier check
8. small-inline verification only when the active physical layout selects a branch-bounded inline mode
9. full canonical byte comparison
``````

The implementation MUST NOT read the canonical byte slab before the candidate survives the cache-local verification
stages, except for explicitly measured and justified small-table implementations.

A failure at any verification stage rejects the candidate only for the current equality check.

It MUST NOT mutate semantic identity material.

The verification ladder is a physical acceleration path.

It MUST NOT become semantic equality authority.

Exact canonical verification remains the final equality authority whenever compact metadata is not sufficient.

### 13.18. Small Canonical Bytes Inline Law

Small-inline canonical bytes are an optional physical optimization.

They are not a mandatory ADR-0041 compliance requirement.

A compliant implementation MAY inline small canonical byte payloads, or their word-equivalent representation, inside the
intern metadata plane only when the active resolved physical policy selects a lawful small-inline mode.

Allowed small-inline modes:

- `DISABLED`: every candidate uses sealed slab / canonical byte handle verification;
- `SEGREGATED_INLINE_TABLE`: inline candidates and external-slab candidates are stored or scanned through separate
  primitive tables / lanes;
- `PRECLASSIFIED_TWO_PASS`: candidates are preclassified into inline and external-slab ranges before the hot
  verification
  loop;
- `MEASURED_MIXED`: a mixed inline/external layout is allowed only with benchmark evidence proving that the branch is
  not
  a throughput regression for the target workload and runtime.

A hot verification loop SHOULD NOT contain an unpredictable per-candidate branch of the form:

``````text
if (isSmallInline) {
    compare inline bytes
} else {
    chase slab pointer
}
``````

unless `MEASURED_MIXED` is selected by resolved physical policy and justified by benchmark evidence.

The preferred shapes are:

``````text
inline table
-> inline verifier loop
``````

and:

``````text
external table
-> sealed slab / canonical byte handle verifier loop
``````

or:

``````text
preclassification
-> inline range verifier
-> external range verifier
``````

If the complete canonical byte payload fits within the implementation's small-inline threshold, equality MAY be verified
without chasing the canonical byte slab only inside a lawful small-inline mode.

The small-inline threshold is a physical implementation policy.

It MUST be fixed before scope admission.

It MUST be benchmarked.

It MUST NOT be selected by live profiling, GC behavior, branch-misprediction feedback, worker timing, or runtime data
frequency inside an admitted scope.

The small-inline representation MUST be byte-exact and MUST NOT use:

- display strings;
- object identity;
- backend-native handles;
- source text that bypassed canonical ratification;
- delimiter-joined material.

Changing the small-inline mode, threshold, or table shape MUST NOT change canonical bytes, HID derivation, collision
verification, stable intern id assignment, or semantic equality.

A release claiming small-inline acceleration MUST publish:

- selected small-inline mode;
- threshold;
- expected payload size distribution;
- branch-miss / throughput benchmark evidence;
- cache-miss benchmark evidence;
- and fallback behavior when the benchmark gate is not met.

### 13.25. NUMA-Local Staging and Deterministic Merge Law

NUMA-local, CPU-local, worker-local, or lane-local arenas MAY be used as physical staging areas.

They MUST NOT define semantic identity or stable intern id assignment.

The following MUST NOT influence final identity output:

- CPU core id;
- NUMA node id;
- worker id;
- lane id except as a non-semantic physical routing choice;
- local arena append order;
- thread scheduling;
- local completion order.

All locally staged candidates MUST pass through deterministic merge and seal before stable ids are published.

Required shape:

``````text
local physical staging
-> canonical bytes / HID preparation
-> deterministic global or scope-local merge
-> collision verification
-> sealed intern table
-> stable id publication
``````

### 13.26. Prefetch-Aware Slab Layout Law

Identity-adjacent material SHOULD be physically linearized by deterministic access order.

A compliant implementation SHOULD distinguish hot and cold identity material.

Hot material includes:

- HID words;
- stable intern id;
- version fingerprint;
- canonical byte length;
- inline verifier prefix / suffix;
- table ordinal;
- compact shape summary fields where applicable.

Cold material includes:

- large canonical byte payloads;
- large future-ratified lowered-contract payloads;
- diagnostic material;
- source spans;
- rare verification payloads.

Physical order may follow deterministic intern id order.

Physical order MUST NOT create deterministic intern id order.

## 10. Required Domain Registry

Every domain that uses primitive substrates MUST register:

| Field                 | Required meaning                                                                                                                |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------|
| Domain                | metadata identity, frozen image, planning transient, planning published snapshot, L2 interner, VM execution, reporting, adapter |
| Substrate port        | `CanonicalByteSubstrate`, `PrimitiveTableBackend`, `PublishedImageStore`, or equivalent core-facing contract                    |
| Physical backend      | heap primitive arrays, packed state word backend, off-heap segment, `MemorySegment`, native aligned, mapped, generated layout   |
| Reason                | allocation reduction, locality, branch discipline, deterministic ordering, DoS boundary, etc.                                   |
| Owner                 | lane, run, artifact publisher, interner scope, slot authority, adapter boundary                                                 |
| Mutability            | staging mutable, scratch mutable, published immutable, retired immutable                                                        |
| Publication rule      | none, copy/compact, zero-copy promotion, rebuild/republish, CAS terminalization, report lifecycle                               |
| Reclamation rule      | reset, teardown, epoch retirement, whole-slab reclaim, diagnostic lifecycle, JVM GC                                             |
| Async boundary        | none, event ingestion, async reclaimer, report sink, L2 completion lane                                                         |
| Equivalence authority | golden vectors, reference implementation, canonical byte oracle, or exact structural oracle                                     |
| Required tests        | golden vectors, architecture tests, leak tests, benchmarks, repeated-daemon tests, cross-backend equivalence tests              |

Registry rule:

A domain registry entry MUST name both the core-facing substrate port and the physical backend family.

It MUST NOT collapse them into one concept.

A backend may be replaced only if the replacement satisfies the same lifecycle law and cross-backend equivalence tests.

## 11. Consequences

### 11.1. Positive Consequences

- physical backends can evolve from heap primitive arrays to `MemorySegment`, off-heap, native-aligned, mapped, or
  generated layouts without changing core identity law;
- domain/application core remains insulated from JVM heap layout, native allocator, digest-library, and SIMD
  implementation details;
- v2 physical acceleration can be admitted behind ports with golden-vector equivalence rather than by rewriting
  canonical identity logic;
- Mechanical sympathy becomes auditable rather than scattered across ADR-0041.
- Primitive substrates can be introduced without losing lifecycle authority.
- Metadata, frozen, planning, L2, VM, and reporting can reuse the same lifecycle vocabulary.
- Thread-local, coroutine-local, scheduler-owned, and worker-owned hidden state is excluded from core authority.
- Published immutable slabs have a uniform reclamation model.

### 11.2. Negative Consequences

- substrate ports and physical backends add one explicit architectural seam;
- low-level implementations must maintain cross-backend equivalence tests;
- benchmark fixtures and runtime-profile evidence become mandatory for stronger physical claims;
- More lifecycle states must be modeled explicitly.
- Primitive performance work requires ownership and reclamation evidence.
- Some simple JVM object patterns become unacceptable in committed hot paths.
- Benchmark and leak-test burden increases.

### 11.3. Accepted Trade-off

Kontrakt accepts explicit primitive lifecycle governance because object-allocation reduction without ownership law would
merely move nondeterminism from GC behavior into hidden slab, lane, and callback behavior.

## 12. Final Rule

Mechanical sympathy is not an implementation preference.

Mechanical-sympathy laws are core governance.

Physical substrate implementations are adapter / infrastructure backends.

In Kontrakt, every primitive substrate has an owner, a lifecycle, a publication rule, a reclamation rule, a
deterministic authority boundary, a core-facing substrate port, and a replaceable physical backend.

A physical backend may improve locality, allocation profile, alignment control, throughput, or latency.

It MUST NOT change canonical bytes, HID derivation, collision verification, stable intern id assignment, canonical
ordering, semantic equality, publication law, or report identity.