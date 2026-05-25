# ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning

## Status

Proposed

## Date

2026-05-14

## Related

- ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
- ADR-0031: Two-Tier Transactional Memoization and Structural Interning
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0033: Bootstrap Runtime Policy Ratification, Storage Governance, and Deferred Platform-Aware Autotuning
- ADR-0034: Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority
- ADR-0035: Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery
- ADR-0036: Joined-Wait Planning-Run Suspension Bridge and Fresh-Session Restart Authority
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- ADR-0038: Interface Contract Polymorphic Expansion and Non-Composite Type Expansion Completion
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- `docs/constitution/compiler-core-protocols.md`
- `docs/design/canonical-ir-stage-and-lowering-protocol.md`
- `docs/design/deterministic-active-member-projection-and-ordering-protocol.md`
- `docs/design/l1-planner-session-primitive-data-structures.md`
- `docs/design/l2-plan-interner-partitioned-tier2-with-governance.md`
- `docs/design/planner-budget-resolution-and-worker-lifecycle.md`

---

- ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation

## 1. Context

Kontrakt treats an interface as a real executable and analyzable contract, not merely as a JVM type surface.

A JVM interface normally exposes method signatures and type shape, but it does not fully standardize the behavioral,
structural, construction, dependency, polymorphic, and verification obligations required to treat an implementation as a
contract-bearing subject.

Kontrakt restores that missing contract role.

Runtime test-object generation is one current verification strategy. It is not the definition of the system.

The deeper goal is to recover the contract meaning that interfaces should have carried: a standard semantic surface that
can be analyzed, lowered, verified, replayed, queried incrementally, and, when necessary, materialized.

Therefore, planning does not merely produce a test plan.

Planning lowers interface contract meaning into a canonical verification structure.

In the current runtime-oriented implementation, that verification structure may drive fixture generation, object
materialization, execution, and observation. In future static or hybrid implementations, the same canonical contract
meaning may drive static analysis, proof-like validation, incremental query evaluation, or compile-time contract
checking without requiring runtime test-object generation.

ADR-0039 erased backend-native handles from planning-facing metamodel consumption.

The accepted long-term shape is:

``````text
backend-native handle
-> acquisition / lowering
-> frozen adapter-neutral metamodel image
-> planning-facing providers
``````

ADR-0040 then promoted frozen metamodel acquisition from a reflection-specific collector into a backend-model-neutral
deterministic acquisition pipeline.

The accepted ADR-0040 shape is:

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

ADR-0040 deliberately does **not** define:

- canonical byte encoding;
- BLAKE3 derivation;
- HID format;
- stable TypeReference fingerprinting;
- protocol-owned interning;
- stable intern id assignment;
- final primitive membership substrate;
- final route64 derivation;
- final PlanCacheKey integration;
- persistent frozen image identity.

This ADR closes that identity substrate.

The immediate motivation is:

``````text
ADR-0040 makes acquisition deterministic and backend-erased.
ADR-0041 makes metadata identity deterministic, compact, collision-verified,
version-bound, and reusable across frozen acquisition, planning, and L2.
``````

Without ADR-0041, the system remains dependent on:

- expensive structural comparison;
- transitional object-array membership;
- comparator-heavy lookup;
- repeated canonical text comparison;
- fragile hand-written hashing;
- ambiguous use of route keys as equality keys;
- image-local ordinals being mistaken for stable identity;
- and backend/cache/runtime order leaking into ids.

With ADR-0041, Kontrakt standardizes the following substrate:

``````text
canonical semantic material
-> canonical byte encoding
-> BLAKE3 digest / keyed derivation
-> domain-separated HID
-> collision verification
-> protocol-owned interning
-> deterministic stable intern id assignment
-> primitive membership / sorting / table addressing
``````

---

## 2. Problem

Kontrakt already has many identity-like surfaces.

Examples include:

- `TypeReference`;
- `CanonicalTypeId`;
- `TypeCycleKey`;
- `TypeCycleIdentity.identityBits64`;
- `CanonicalSignature`;
- `CanonicalActiveMemberKey`;
- `LocalSelectorTuple`;
- `FrozenTypeReferenceIndex` ordinal;
- `FrozenMetamodelImageId`;
- `FrozenMetamodelImageSchemaVersion`;
- `PlanCacheKey`;
- `PlanCacheKey.route64`;
- `CanonicalEdgeKey`;
- `HID`;
- `hashCode()`;
- backend-native symbol identity;
- backend-native source location;
- runtime binding snapshot identity;
- future persistent frozen image digest.

These values are not equivalent.

Some are authoritative semantic equality material.
Some are normalized facts.
Some are routing hints.
Some are image-local addresses.
Some are diagnostic ids.
Some are operational generation markers.
Some are cache keys.
Some are entropy derivation inputs.
Some are merely implementation conveniences.

If these axes are collapsed, Kontrakt loses compiler-grade determinism.

### 2.1. `hashCode()` is not protocol identity

JVM / Kotlin `hashCode()` may exist as an in-memory equality companion.

It MUST NOT define:

- protocol identity;
- canonical digest;
- route64;
- HID;
- stable intern id;
- persistent frozen image key;
- PlanCacheKey equality;
- canonical ordering;
- frozen ordinal assignment.

Reason:

`hashCode()` is not a versioned protocol surface.
It is also not a collision-verified equality authority.

### 2.2. Backend identity is not Kontrakt identity

Reflection, KSP, KSP2, bytecode, compiler-static, source-analysis, and precomputed-index backends may expose
identity-like material.

Examples:

- `KType` object identity;
- `KClass` identity;
- `KSType` identity;
- `KSDeclaration` identity;
- `Resolver` identity;
- compiler IR symbol id;
- bytecode parser node id;
- PSI / AST node id;
- source file path;
- source line number;
- classloader identity;
- backend-local slot id;
- backend manifest ordinal.

These may be useful for diagnostics, scheduling, compatibility checks, or backend-local acceleration.

They are not Kontrakt semantic identity.

A backend-native identity may become relevant to Kontrakt identity only after it is:

1. lowered into backend-neutral canonical material;
2. encoded under this ADR's canonical byte encoding law;
3. version-bound;
4. domain-separated;
5. collision-verified;
6. and accepted by the relevant protocol boundary.

### 2.3. Frozen ordinal is not stable identity

`FrozenTypeReferenceIndex` ordinals are image-local dense addresses.

They are allowed and desirable for:

- table addressing;
- provider lookup;
- frozen shape / cycle / raw-fact table reads;
- planning-facing cheap ordinal paths.

They MUST NOT be used as:

- persistent metadata identity;
- cross-image equality material;
- route64 material;
- PlanCacheKey semantic equality material;
- HID replacement;
- stable TypeReference intern id unless explicitly derived by this ADR's intern-id law.

Reason:

The same semantic type may receive a different image-local ordinal in a different frozen image, backend snapshot, module
slice, or acquisition run.

### 2.4. Hash equality is not semantic equality

A digest or HID match is a fast-path candidate.

It is not semantic equality authority by itself.

Kontrakt MUST treat collision as a protocol case, not as an impossibility.

Required implication:

``````text
compact identity match
-> exact canonical byte verification
   or exact canonical structural verification
-> semantic equality accepted
``````

Forbidden implication:

``````text
compact identity match
-> semantic equality accepted
``````

### 2.5. Acquisition order must not assign stable identity

Stable intern ids, route keys, persistent ids, and canonical identity material MUST NOT depend on:

- acquisition discovery order;
- backend enumeration order;
- backend callback completion order;
- thread scheduling;
- map iteration order;
- set iteration order;
- queue arrival order;
- parallel lane completion order;
- object allocation order;
- source declaration order unless declaration order is an explicit ratified semantic fact;
- frozen ordinal assignment unless the ordinal was derived from this ADR's canonical identity law.

### 2.6. Canonical ordering and canonical byte encoding are distinct

Canonical ordering determines deterministic comparison.

Canonical byte encoding determines protocol bytes for digesting, HID, stable interning, signatures, and golden vectors.

They are related but not interchangeable.

Forbidden:

- defining canonical byte encoding as "whatever comparator sorted";
- defining canonical ordering as UTF-8 byte comparison;
- delimiter-joining fields into one string and hashing it;
- using display labels as canonical encoding;
- using JSON serialization as canonical encoding unless a later ADR explicitly ratifies JSON as protocol encoding.

### 2.7. UTF-8 protocol bytes must be explicit

Kotlin/JVM `String` is UTF-16 code-unit based internally.

Kontrakt protocol identity is byte-level.

ADR-0041 therefore must define:

- UTF-8 protocol encoding;
- byte-length accounting;
- tagged fields;
- explicit length prefixes;
- no platform default charset;
- no locale-sensitive collation;
- no delimiter-joined identity payload;
- no implicit Unicode normalization during byte encoding;
- unpaired surrogate rejection before canonical encoding.

---

## 3. Decision

ADR-0041 owns metadata identity authority.

Generic primitive substrate lifecycle, slab ownership, reader epoch reclamation, and asynchronous ownership boundaries
are
governed by ADR-0042 and are referenced by ADR-0041 only where they affect metadata identity publication.

Kontrakt will introduce a **Stable Metadata Identity Substrate**.

This substrate is the only accepted path for stable metadata identity derivation.

The normative identity pipeline is:

``````text
ratified contract-meaning material
-> canonical material object
-> canonical byte encoding
-> BLAKE3 digest / keyed derivation
-> domain-separated HID
-> collision verification
-> protocol-owned interning
-> deterministic stable intern id assignment
-> primitive membership / sorting / table addressing
``````

This ADR defines:

- identity authority levels;
- canonical material rules;
- canonical byte encoding rules;
- BLAKE3 suite usage;
- HID domain separation and width law;
- hierarchical derivation law;
- collision verification law;
- protocol-owned interning law;
- stable intern id assignment law;
- frozen consumer law;
- planning consumer law;
- L2 consumer law;
- golden vector obligations;
- integration boundary with ADR-0043 for structural/contextual contract graph identity.

This ADR does not define the full contract graph ontology, contract fact taxonomy, software-contract state machine
model,
DTO/boundary contract model, governance contract vocabulary, or incremental contract-query dependency model.

Those surfaces are intentionally left to the forthcoming top-level contract definition document and ADR-0043.

ADR-0041 owns the digest/HID/interner substrate used by those surfaces after they are ratified.

This ADR does not require every physical optimization to land in the first implementation patch.

It does require that all newly introduced identity material follows this law from the first implementation.

### 3.0.1. Ratified by ADR-0041 vs Deferred Elsewhere

ADR-0041 owns the common stable metadata identity substrate.

The following surfaces are ratified by ADR-0041:

| Surface                                   | Status                                                                                         |
|-------------------------------------------|------------------------------------------------------------------------------------------------|
| `CanonicalEnvelopeHeader`                 | ratified as the mandatory common canonical identity envelope header                            |
| common field-table structure              | ratified at the protocol level                                                                 |
| common wire type id space                 | ratified at the protocol level                                                                 |
| endian rule                               | ratified as little-endian unsigned bit patterns unless a field explicitly states otherwise     |
| offset / length arithmetic law            | ratified as checked arithmetic with fail-closed bounds validation                              |
| unknown tag behavior                      | ratified as default fail-closed, with compatibility-matrix skip only when explicitly allowed   |
| padding / reserved-bit law                | ratified as zero-on-encode and fail-closed-on-non-zero unless compatibility ratifies otherwise |
| BLAKE3 / HID derivation law               | ratified                                                                                       |
| version-bundle fingerprint derivation law | ratified                                                                                       |
| collision verification law                | ratified                                                                                       |
| protocol-owned interning law              | ratified                                                                                       |

The following surfaces are deferred to the owning contract or domain document:

| Surface                                                 | Owner                                         |
|---------------------------------------------------------|-----------------------------------------------|
| `LOWERED_CONTRACT_FACT` vocabulary                      | future top-level contract definition document |
| annotation / DSL / compiler metadata lowering semantics | future top-level contract definition document |
| domain-specific contract fact field tags                | owning contract fact domain                   |
| domain-specific verification payloads                   | owning identity domain                        |
| future contract frontend parity law                     | future top-level contract definition document |
| domain-specific payload schema evolution                | owning identity domain compatibility matrix   |

The boundary is:

``````text
ADR-0041 fixes the common binary envelope and identity substrate.
Domain documents define their payload fields inside that envelope.
``````

### 3.1. Determinism Supremacy Law

Determinism is the supreme constraint of the stable metadata identity protocol.

All physical acceleration is subordinate to deterministic identity.

If performance and determinism conflict, determinism wins.

No physical optimization may change:

- canonical bytes;
- canonical ordering;
- HID derivation result;
- stable intern id assignment;
- query-key equality;
- collision verification outcome;
- frozen ordinal assignment;
- `PlanCacheKey` equality;
- route64 derivation;
- planning-visible identity;
- cache-visible identity;
- persistent artifact identity.

Physical acceleration may change only:

- latency;
- throughput;
- memory bandwidth usage;
- cache locality;
- allocation profile;
- probe count;
- CPU instruction mix;
- branch prediction behavior;
- prefetch success rate.

A compliant optimization must be observationally equivalent to the reference deterministic identity pipeline:

``````text
ratified contract-meaning material
-> canonical material object
-> canonical byte encoding
-> domain-separated BLAKE3 / HID derivation
-> collision verification
-> protocol-owned deterministic interning
-> stable scoped intern id
``````

If an optimization cannot prove observational equivalence with this reference pipeline, it is non-compliant.

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

## 4. Authority and Precedence

This ADR is authoritative for:

- stable metadata identity;
- canonical metadata byte encoding;
- BLAKE3 usage for metadata identity / digest / derivation surfaces;
- HID domain separation and width semantics;
- protocol-owned metadata interning;
- stable metadata intern id assignment;
- collision verification for compact metadata identity;
- frozen / planning / L2 consumer rules for metadata identity material.

This ADR is not authoritative for:

- full planning canonical IR signature schema;
- L2 storage eviction policy;
- S3-FIFO / SIEVE retention policy;
- frozen acquisition state-machine lifecycle;
- backend readiness bridge semantics;
- runtime worker lifecycle;
- full persistent frozen image binary format;
- source-level incremental watch protocol;
- database-style out-of-core storage.

If this ADR conflicts with ADR-0040 on frozen acquisition lifecycle, ADR-0040 wins.

If this ADR conflicts with Canonical IR documents on Local-to-Canonical promotion or canonical plan-node signature
authority, the Canonical IR law wins.

If this ADR conflicts with ADR-0032 on routing identity vs semantic identity hierarchy, ADR-0032 wins.

This ADR defines the metadata identity substrate that those layers may consume.

### 4.1. ADR-0040 / ADR-0041 Temporal Ordering

ADR-0041 does not own the frozen acquisition lifecycle.

ADR-0040 owns acquisition run lifecycle, backend readiness, restart, acquisition node progress, and publication
lifecycle.

ADR-0041 owns the **identity seal phase** that must complete before a `FrozenMetamodelImage` becomes planning-visible.

The required temporal ordering is:

``````text
1. ADR-0040 acquisition lifecycle admits and runs.
2. Backend-erased candidate material is staged.
3. ADR-0041 identity seal runs:
       canonical bytes
       HID derivation
       version-bundle compatibility validation
       collision verification
       stable intern id assignment
       intern table integrity validation
4. Frozen image assembly validates table coverage.
5. FrozenMetamodelImage is safely published.
``````

Therefore, stable intern ids may be included in a frozen image only after acquisition material is complete enough for
the identity seal to run.

ADR-0041 MUST NOT be interpreted as allowing stable intern ids to be published before ADR-0040 acquisition has reached
the appropriate seal boundary.

---

## 5. Core Vocabulary

### 5.0. Contract Meaning and Canonicalization

In ADR-0041, contract meaning means the obligations, choices, relationships, constraints, and verification-relevant
facts
that Kontrakt must preserve when it lowers an interface contract into a canonical verification structure.

This is not raw metadata shape.

It is not every fact a backend can observe.

It is not the incidental form in which reflection, KSP, bytecode, source indexes, frozen images, runtime bindings, or
persistent artifacts happen to expose a type.

Contract meaning is the part of an interface contract that can change the canonical contract graph or its verification
semantics.

Examples of contract meaning include:

- which subject type the contract is about;
- which implementation candidates may satisfy the contract;
- which constructor, property, parameter, dependency, synthetic edge, map entry edge, or polymorphic edge is relevant to
  the contract;
- which members are active under the current capability profile;
- which edges are strong, deferred, substitutable, ignored, or invalid;
- which cycles are contractually breakable and which must fail;
- which local materialization choices are allowed;
- which deterministic selector material governs generated or materialized values;
- which runtime binding snapshot facts may affect implementation or polymorphic selection;
- which canonical node body must be produced;
- and which signature, digest, HID, interning, replay, query-key, and cache-correctness surfaces may rely on that
  meaning.

Examples of non-contract meaning include:

- whether the fact came from reflection, KSP, KSP2, bytecode, source analysis, or a precomputed index;
- backend-native object identity;
- backend enumeration order;
- callback completion order;
- source location used only for diagnostics;
- display strings;
- frozen ordinal by itself;
- route64 by itself;
- cache hit/miss state;
- worker, lane, thread, CPU, NUMA, or allocation identity;
- wall-clock time;
- and JVM `hashCode()`.

The core canonicalization rule is:

``````text
same interface contract meaning
-> same canonical contract representation
-> same reproducible verification structure
``````

Reflection, KSP, bytecode, source indexes, frozen images, cache state, worker assignment, thread timing, and runtime
object identity may differ.

The canonical contract meaning must not.

Canonicalization is the process of lowering many possible representations of the same interface contract meaning into
one Kontrakt-standard representative.

### 5.1. Canonical Material

Canonical material is Kontrakt's standard representative of protocol-defined contract meaning.

It is the form Kontrakt uses after it has decided which facts are contract-relevant and which facts are merely backend,
runtime, diagnostic, physical, or ordering noise.

The word `canonical` does not merely mean normalized, immutable, sorted, hashed, interned, or backend-erased.

It means that Kontrakt has selected one representation as the standard representative for a specific contract meaning
inside a specific identity domain.

The relationship is:

``````text
many possible backend/runtime representations
-> one protocol-defined contract meaning
-> one canonical material representation
-> canonical bytes
-> digest / HID / interning / replay / query key / cache correctness
``````

Backend erasure is required because backend facts are not the contract.

Deterministic encoding is required because the same contract must replay the same way.

Collision verification is required because compact identity must not become probabilistic contract identity.

Version binding is required because changing the standard contract meaning must be explicit.

But the reason all of these exist is the same:

Kontrakt must make the same interface contract lower to the same canonical contract graph and the same reproducible
verification structure.

Canonical material is not necessarily Canonical IR.

Canonical IR is the standard representation of committed planning structure.

Canonical metadata material is the standard representation of metadata and contract-identity material used before or
beside Canonical IR by frozen metamodel, planning, query-key, interning, replay, and cache protocols.

Examples:

- canonical TypeReference material;
- canonical TypeCycleKey material;
- canonical TypeShapeSummary identity material;
- canonical lowered contract-fact material derived from annotations, DSL, compiler metadata, generated indexes, or
  future contract surfaces;
- canonical raw-fact record identity material;
- canonical active-member key material;
- canonical runtime binding identity material;
- canonical local selector tuple material;
- canonical frozen table schema identity material.

Canonical material MUST be:

- the standard representative for its contract meaning domain;
- protocol-ratified;
- identity-domain explicit;
- adapter-neutral;
- backend-handle free;
- version-bound;
- normalized before encoding;
- deterministic under the active version bundle;
- independent from JVM object identity;
- independent from backend-native handles;
- independent from acquisition order;
- independent from callback completion order;
- independent from thread scheduling;
- independent from mutable runtime state;
- explicit about unavailable / unknown metadata;
- suitable for canonical byte encoding;
- and protected by collision verification when compact identity is derived.

Canonical material MUST NOT include:

- reflection / KSP / compiler / bytecode handles;
- source location as semantic identity;
- JVM object identity;
- JVM `hashCode()`;
- allocation order;
- backend enumeration order;
- frozen ordinal unless the domain explicitly treats it as local address material;
- route64;
- diagnostic labels;
- display strings;
- wall-clock time;
- random UUIDs;
- mutable runtime state;
- or process-global mutable interner state.

### 5.1.1. Canonicality Means Standard Contract Representation

Canonicality is Kontrakt's standardization status for contract meaning.

A value is canonical because Kontrakt has selected it as the one standard representative for the contract meaning owned
by
an identity domain.

It is not canonical merely because it is:

- immutable;
- sorted;
- byte-encoded;
- deduplicated;
- interned;
- hashed;
- backend-erased;
- or stored in a frozen image.

Those properties may support canonicality, but none of them independently grants canonical authority.

A canonical protocol must answer:

``````text
many possible representations of the same interface contract meaning
-> one standard contract representation
-> one ordering law
-> one byte encoding law
-> one digest / HID derivation law
-> one collision verification law
-> one interning law
``````

If two inputs carry the same interface contract meaning under the active Kontrakt protocol, they MUST lower to the same
canonical material.

If two inputs lower to different canonical material, then either:

- they are contract-meaningfully different under the active protocol;
- the version bundle intentionally changed the standard representation;
- or an implementation violated the canonicalization law.

Canonicality is therefore the boundary where Kontrakt stops preserving incidental representation differences and starts
enforcing its own standard contract representation.

### 5.1.2. Examples of Contract Meaning by Identity Domain

This section gives identity-domain examples only to clarify the boundary of ADR-0041.

It does not define the full Kontrakt contract ontology, contract fact taxonomy, or frontend lowering semantics.

The top-level contract document owns that work:

``````text
`docs/the-most-important-thing/interface-as-contract.md`
``````

`TypeReference` canonical material represents the standard meaning of a type reference as consumed by frozen metamodel,
planning, cycle identity, projection, query-key, and interning protocols.

Meaning-bearing facts may include:

- canonical type id;
- type cycle key;
- canonical type signature;
- type shape summary;
- generic argument identities;
- nullability semantics where already ratified by the relevant type protocol;
- type nesting depth;
- type identity coherence proof;
- normalization and identity algorithm versions.

Non-meaning-bearing facts include:

- `KType` object identity;
- `KClass` identity;
- KSP symbol identity;
- reflection enumeration order;
- backend registry slot;
- source spelling not accepted by the canonical type-text protocol;
- diagnostic rendering.

`TypeCycleKey` canonical material represents the standard meaning of the type identity used for active-cycle preflight.
It is not the full planning node identity and does not include child traversal results.

`TypeShapeSummary` canonical material represents the standard meaning of the structural expansion category of a type.
Meaning-bearing facts include whether the type is atomic, composite, container-like, array-like, map-like, polymorphic,
or otherwise governed by a ratified expansion family.

`LOWERED_CONTRACT_FACT` is a reserved identity domain.

It represents future contract material only after the top-level contract document ratifies the contract fact vocabulary,
frontend syntax boundary, lowering law, version bundle, canonical fields, and verification payload.

Until that ratification exists, ADR-0041 does not define which contract facts exist, how annotations lower into them,
how
DSL clauses lower into them, how compiler metadata lowers into them, or how default/effective value semantics work.

`AnnotationDescriptor` is not canonical material by itself.

Annotation syntax is a front-end / adapter-facing contract surface. It may carry contract evidence, but it is not the
standard contract meaning itself.

Equivalent future contract meaning may be expressed through annotations, DSLs, compiler metadata, generated indexes,
source analysis, or other surfaces. The identity substrate in this ADR is prepared to host that material after it is
ratified elsewhere.

`RawFactRecord` canonical material represents the standard meaning of a frozen raw metamodel fact row. It may reference
TypeReference identity material, but it must not recursively inline arbitrary reachable raw facts.

`ActiveMemberKey` canonical material represents the standard meaning of a projected member as a contract-relevant
traversal and ordering candidate. Meaning-bearing facts include owner identity, member kind, canonical member name,
source role, declaration ordinal availability, and ratified type identity material.

`LocalSelectorTuple` canonical material represents the standard meaning of a local expansion obligation before Canonical
IR publication. It is local and path-sensitive. It may participate in HID derivation, but it is not automatically
Canonical IR.

`RuntimeBindingSnapshot` canonical material represents the standard meaning of runtime binding inputs that may affect
polymorphic or implementation selection. It excludes runtime object identity and mutable container state.

`FrozenTableSchema` canonical material represents the standard meaning of the frozen table layout and schema contract.
It excludes object-array storage details, allocation addresses, and physical slab placement.

### 5.1.3. Annotation Surface Boundary

Annotation syntax is not the contract.

Annotation syntax is one possible front-end surface for expressing future Kontrakt contract meaning.

Kontrakt MUST NOT make annotation descriptors, annotation backend handles, annotation DTO shapes, or annotation source
spellings canonical identity material.

This ADR intentionally does not ratify annotation semantics, annotation argument semantics, DSL semantics, compiler
metadata semantics, generated-index semantics, or frontend lowering semantics.

The reserved future lowering path is:

``````text
annotation / DSL / compiler metadata / generated index / future contract surface
-> front-end contract syntax material
-> future ratified lowered contract material
-> canonical identity material governed by ADR-0041
``````

The top-level contract document must define when a frontend surface carries contract meaning and how that meaning
lowers.

Until that document is ratified, annotation material remains adapter/front-end syntax and diagnostic evidence only.

### 5.1.4. Explicit Effective Value Law

Reserved.

This law is intentionally not ratified by ADR-0041.

The distinction between source syntax presence, defaulted value, effective value, absent value, unavailable value, and
rejected value belongs to the top-level contract definition document:

``````text
`docs/the-most-important-thing/interface-as-contract.md`
``````

ADR-0041 reserves this section because future lowered contract material may require effective-value semantics before it
can become canonical identity material.

Until that document ratifies the effective-value model, no annotation, DSL, compiler metadata, generated contract index,
or future contract surface may define canonical identity by relying on implicit default interpretation.

Backend-provided defaults are input evidence only.

They are not canonical truth in ADR-0041.

### 5.1.5. Contract Syntax Backend Parity Law

Reserved.

ADR-0041 does not yet define contract frontend syntax parity.

The future top-level contract definition document must define when Reflection annotations, KSP annotations, compiler
metadata, generated indexes, DSL clauses, or other contract frontends express the same contract meaning.

Once that law is ratified, ADR-0041 requires equivalent ratified frontend meanings to enter the same canonical identity
material under the active version bundle.

Until then, this section is a reserved integration point.

Silent backend divergence remains forbidden for identity material that is already ratified by an active identity domain.

### 5.1.6. Lowered Contract Fact Reference Boundary

Reserved.

ADR-0041 already defines the general layered recursive interning and parent-references-child identity laws.

Whether future lowered contract material may reference types, members, selectors, policies, states, protocols, or other
contract facts belongs to the top-level contract definition document.

Once ratified, any such references MUST obey ADR-0041's identity substrate rules:

- no backend handles;
- no annotation object graphs;
- no source AST / PSI object graphs;
- no recursive raw-fact closure inlining;
- no unbounded frontend syntax traversal;
- references must use ratified identity material, sealed stable intern ids, or SCC-governed temporary references.

Until the contract model is ratified, this section is a reserved boundary and does not define a concrete
lowered-contract
reference schema.

### 5.1.7. Contract Syntax Dependency SCC Law

Reserved.

ADR-0041 already defines deterministic SCC sealing for metadata identity graphs.

Whether future frontend contract syntax or lowered contract material forms its own dependency graph is deferred to the
top-level contract definition document.

Once ratified, any cyclic lowered-contract identity graph MUST use ADR-0041's deterministic SCC seal rules.

Planning cycle truncation MUST NOT be used as a contract-fact identity cycle breaker.

Unrecognized annotations, incidental meta-annotations, diagnostic source metadata, and backend-only annotation graphs
MUST
NOT be recursively followed merely because they exist.

Until the contract model is ratified, this section is a reserved integration point and does not define contract syntax
SCC semantics.

### 5.1.8. Lowered Contract Fact Hot Layout Reservation

Reserved.

ADR-0041 does not ratify a bit-packed, fixed-offset, fixed-width, or hot-projected layout for lowered contract facts.

A hot lowered-contract layout may be defined only after the top-level contract document ratifies:

- the contract fact taxonomy;
- the target model;
- the value model;
- the effective/default value model, if any;
- the compatibility matrix;
- the canonical byte fields;
- and the collision verification payload.

Until then, lowered contract material may not claim a protocol-owned bit layout, field packing scheme, or hot-path fixed
record shape.

The reserved optimization direction is:

``````text
contract meaning model
-> lowered contract fact taxonomy
-> canonical encoding and compatibility matrix
-> optional bit-packed hot projection
``````

Not:

``````text
bit layout
-> force contract semantics to fit it
``````

### 5.2. Canonical Bytes

Canonical bytes are the tagged, length-prefixed, version-bound byte encoding of canonical material.

Canonical bytes are protocol material.

They are not:

- debug strings;
- display strings;
- JSON by default;
- Kotlin serialization output;
- Java serialization output;
- delimiter-joined text;
- backend-native descriptor dumps;
- source text dumps;
- reflection `toString()` output;
- KSP symbol spelling.

Canonical bytes are the exact input to digest and HID derivation.

### 5.3. Digest

Digest means the cryptographic hash output over canonical bytes under an explicit algorithm suite and domain.

For this ADR, the default suite is BLAKE3.

A digest may be:

- a full 256-bit BLAKE3 digest;
- a BLAKE3 XOF output of a ratified width;
- a keyed BLAKE3 output;
- a derived compact identity input.

A digest is not semantic equality authority by itself.

### 5.4. HID

HID means **Hashed Identity Descriptor**.

A HID is a domain-separated compact identity descriptor derived from canonical bytes, parent identity, local selector
material, or equivalent canonical material under this ADR.

HID may be used for:

- primitive membership;
- routing;
- table addressing;
- deterministic ordering acceleration;
- grouping collision candidates;
- entropy derivation;
- deterministic materialization;
- stable intern preclassification.

HID MUST NOT be used as final semantic equality authority without collision verification.

### 5.5. Stable Intern Id

A stable intern id is a dense protocol-owned id assigned under an explicit intern scope and deterministic assignment
law.

A stable intern id is not:

- a backend ordinal;
- a frozen ordinal;
- a discovery ordinal;
- a callback order;
- a JVM identity hash;
- a global mutable sequence;
- an allocation-order id.

Stable intern ids may be image-local, run-local, module-local, or artifact-local, but the scope MUST be explicit.

### 5.6. Protocol-Owned Interner

A protocol-owned interner is an identity authority that:

- consumes canonical bytes and verified identity material;
- groups by compact identity only as a fast path;
- verifies collision candidates exactly;
- assigns stable ids deterministically;
- exposes primitive-friendly ids only after verification;
- does not depend on backend-native order;
- does not depend on thread races;
- does not depend on global mutable process state.

### 5.7. Identity Domain

An identity domain is a typed namespace for identity derivation.

Examples:

- `TYPE_REFERENCE`;
- `TYPE_CYCLE_KEY`;
- `TYPE_CYCLE_IDENTITY`;
- `TYPE_SHAPE_SUMMARY`;
- `RAW_FACT_RECORD`;
- `LOWERED_CONTRACT_FACT` (reserved until top-level contract model ratification);
- `ACTIVE_MEMBER_KEY`;
- `LOCAL_SELECTOR_TUPLE`;
- `RUNTIME_BINDING`;
- `FROZEN_IMAGE_CONTENT`;
- `PLAN_CACHE_KEY`;
- `CANONICAL_PLAN_NODE`;
- `REPLAY_MANIFEST`.

Different identity domains MUST NOT share raw digest namespaces.

The same canonical byte payload in two different domains MUST produce different HIDs.

---

## 6. Identity Authority Lattice

Kontrakt adopts the following authority lattice.

### 6.1. Semantic Equality Authority

Semantic equality authority is exact identity material.

Examples:

- canonical signature bytes under active version tuple;
- canonical bytes plus schema/domain/version header;
- exact canonical structural payload;
- full `PlanCacheKey` semantic tuple;
- exact `TypeReference` canonical material where applicable.

Rules:

- semantic equality authority may decide equality;
- it must be version-compatible;
- it must be adapter-neutral;
- it must be collision-resistant by exact verification, not by hash assumption.

### 6.2. Collision-Verified Compact Identity

Collision-verified compact identity is a HID/digest plus verification payload.

Examples:

- `Hid128` + canonical bytes;
- `Hid256` + canonical structural verifier;
- `identityBits64` + canonical signature bytes.

Rules:

- compact identity may group candidates;
- exact verification must follow on match;
- mismatch may reject equality fast;
- match alone is insufficient.

### 6.3. Routing Identity

Routing identity places work into buckets, shards, partitions, or primitive tables.

Examples:

- `route64`;
- shard index;
- primitive hash table bucket;
- L2 pre-screen key;
- NodeIdIndexer phase-1 routing bits.

Rules:

- routing identity is deterministic;
- routing identity is collision-tolerant;
- routing identity is never semantic equality authority;
- exact verification is mandatory after routing match.

### 6.4. Local Address Identity

Local address identity addresses data inside one published object or episode.

Examples:

- frozen ordinal;
- table row index;
- slab offset;
- arena slot;
- dense node id inside one `PlannerSession`.

Rules:

- local address identity is scoped;
- scope must be explicit;
- it must not escape as persistent semantic identity;
- it may be used for performance after the owning artifact is validated.

### 6.5. Diagnostic Identity

Diagnostic identity helps explain provenance.

Examples:

- source file / line;
- backend adapter kind;
- backend version;
- frozen image diagnostic header;
- acquisition scope id;
- image build ordinal;
- backend capability declaration.

Rules:

- diagnostic identity must not influence planning semantics;
- diagnostic identity must not become route64 material;
- diagnostic identity must not become PlanCacheKey semantic material;
- diagnostic identity must not participate in HID unless the domain is explicitly diagnostic-only.

---

## 7. Canonical Material Law

### 7.1. Required Material Properties

Any value used as input to canonical metadata identity MUST satisfy:

1. backend-erased representation;
2. explicit schema version;
3. explicit identity domain;
4. explicit algorithm suite id where hashing is performed;
5. stable field order;
6. explicit unknown/unavailable sentinels;
7. no hidden default values;
8. no mutable collections;
9. no backend-native handles;
10. no object identity;
11. no wall-clock time;
12. no random UUID;
13. no mutable RNG stream;
14. no locale-sensitive rendering;
15. no delimiter-joined string material.

### 7.2. Required Version Tuple

Canonical material MUST carry or be encoded with the relevant version tuple.

At minimum, versioned metadata identity surfaces must include, where applicable:

- `identityDomainVersion`;
- `canonicalEncodingVersion`;
- `normalizationVersion`;
- `typeIdentityAlgorithmVersion`;
- `shapeSummaryVersion`;
- `rawFactSchemaVersion`;
- `contractFactEncodingVersion`;
- `capabilityProfileVersion` when policy changes identity material;
- `runtimeBindingSnapshotVersion` when binding material participates;
- `entropyVersion` when derivation participates;
- `hashAlgorithmVersion`;
- `hidDerivationVersion`;
- `interningProtocolVersion`.

The exact tuple may differ by domain.

It must be explicit and golden-vector covered.

### 7.2.1. Canonical Identity Version Bundle

Individual version values MUST NOT be interpreted independently on hot identity paths.

Each identity domain MUST resolve its relevant version axes into a domain-specific `CanonicalIdentityVersionBundle`.

Illustrative shape:

``````kotlin
class CanonicalIdentityVersionBundle private constructor(
    val identityDomain: IdentityDomain,
    val bundleVersion: Long,
    val canonicalEncodingVersion: Long,
    val domainSchemaVersion: Long,
    val semanticVersionFingerprint128High: Long,
    val semanticVersionFingerprint128Low: Long,
    val compatibilityClass: IdentityCompatibilityClass,
)
``````

The exact API is not frozen.

The invariant is normative:

``````text
many explicit version axes
-> domain-specific resolved bundle
-> compact hot-path fingerprint
-> compatibility-matrix decision
``````

### 7.2.2. Version Dependency Graph

Each identity domain MUST define a version dependency graph.

The dependency graph must answer questions such as:

- whether a `canonicalEncodingVersion` bump invalidates HID output;
- whether a `hidDerivationVersion` bump invalidates canonical bytes;
- whether a `normalizationVersion` bump invalidates TypeReference canonical material;
- whether a schema bump is byte-equivalent, forward-compatible, or incompatible;
- whether an old intern table may be reused under a newer runtime-policy epoch.

Required baseline rules:

``````text
canonicalEncodingVersion bump:
    invalidates canonical bytes;
    invalidates HID unless the compatibility matrix declares byte-equivalent encoding.

hidDerivationVersion bump:
    invalidates HID comparison;
    does not necessarily invalidate canonical bytes.

normalizationVersion bump:
    invalidates TypeReference canonical material unless domain equivalence is explicitly declared.

domainSchemaVersion bump:
    invalidates field interpretation unless compatibility is explicitly declared.

interningProtocolVersion bump:
    invalidates stable intern id comparison across scopes unless a conversion law exists.
``````

### 7.2.3. Compatibility Matrix

Every released identity domain MUST provide an explicit compatibility matrix.

A compatibility matrix may classify two version bundles as:

- `IDENTICAL`;
- `BYTE_EQUIVALENT`;
- `REHASH_REQUIRED`;
- `REENCODE_REQUIRED`;
- `REINTERN_REQUIRED`;
- `INCOMPATIBLE_FAIL_CLOSED`.

A hot path MAY compare compact version fingerprints, but the compatibility meaning of those fingerprints belongs to the
domain compatibility matrix, not to ad hoc integer comparison.

Silent version drift is a protocol violation.

### 7.3. No Hidden Normalization

Canonical material consumes already-ratified values.

Canonical byte encoding MUST NOT perform:

- Unicode normalization;
- Unicode repair;
- trimming;
- lowercasing;
- case folding;
- locale collation;
- source-token interpretation;
- nullable-marker policy;
- star-projection policy;
- generic syntax parsing;
- type alias normalization.

Those belong to earlier metamodel ratification boundaries.

### 7.4. Explicit Unknown / Unavailable Semantics

Unknown and unavailable metadata MUST be encoded explicitly.

Forbidden:

- encoding unavailable ordinal as ordinary `0`;
- encoding unknown nullability as non-null;
- encoding unavailable default-value presence as absent;
- omitting unknown fields as if they were semantically absent;
- collapsing backend limitation into ordinary false.

Required:

``````text
UNKNOWN != ABSENT
UNAVAILABLE != PRESENT(0)
UNSUPPORTED != EMPTY
REJECTED != UNAVAILABLE
FAILED != DEFERRED
``````

### 7.5. Resolved Metadata Identity Policy and Bootstrap Caps

Canonical identity is a bounded resource surface.

A compliant runtime MUST resolve metadata identity caps before identity materialization.

Illustrative shape:

``````kotlin
class ResolvedMetadataIdentityPolicy private constructor(
    /*
     * Per-unit identity fuses.
     *
     * These caps bound one canonical unit so that a single TypeReference, TypeCycleKey,
     * TypeCycleIdentityPrecheck, TypeShapeSummary, raw fact record, active member key,
     * local selector tuple, runtime binding snapshot, intern candidate, or future
     * lowered contract fact cannot accidentally absorb an entire graph closure.
     * Ordinary resource profiles should not primarily scale these values.
     */
    val maxCanonicalBytesPerTypeReference: Int,
    val maxCanonicalBytesPerTypeCycleKey: Int,
    val maxCanonicalBytesPerTypeCycleIdentityPrecheck: Int,
    val maxCanonicalBytesPerTypeShapeSummary: Int,
    val maxCanonicalBytesPerRawFactRecord: Int,
    val maxCanonicalBytesPerActiveMemberKey: Int,
    val maxCanonicalBytesPerLocalSelectorTuple: Int,
    val maxCanonicalBytesPerRuntimeBindingSnapshot: Int,
    val reservedMaxCanonicalBytesPerLoweredContractFact: Int?,
    val maxCanonicalBytesPerInternCandidate: Int,

    /*
     * Aggregate identity budgets for active ADR-0041 v1 metadata domains.
     *
     * Profiles should primarily scale how many canonical units may be admitted and how
     * many total bytes the image/scope may contain, not how large one canonical unit may
     * become.
     */
    val maxTypeReferenceCount: Int,
    val maxTotalTypeReferenceCanonicalBytes: Long,
    val maxTypeCycleKeyCount: Int,
    val maxTotalTypeCycleKeyCanonicalBytes: Long,
    val maxTypeCycleIdentityPrecheckCount: Int,
    val maxTotalTypeCycleIdentityPrecheckCanonicalBytes: Long,
    val maxTypeShapeSummaryCount: Int,
    val maxTotalTypeShapeSummaryCanonicalBytes: Long,
    val maxRawFactRecordCount: Int,
    val maxTotalRawFactRecordCanonicalBytes: Long,
    val maxActiveMemberKeyCount: Int,
    val maxTotalActiveMemberKeyCanonicalBytes: Long,
    val maxLocalSelectorTupleCount: Int,
    val maxTotalLocalSelectorTupleCanonicalBytes: Long,
    val maxRuntimeBindingSnapshotCount: Int,
    val maxTotalRuntimeBindingSnapshotCanonicalBytes: Long,
    val reservedMaxLoweredContractFactCount: Int?,
    val reservedMaxTotalLoweredContractFactCanonicalBytes: Long?,
    val maxInternCandidateCount: Int,
    val maxTotalInternCandidateCanonicalBytes: Long,
    val maxTotalFrozenImageIdentityBytes: Long,
    val maxDiagnosticEvidenceBytes: Long,

    val maxCanonicalBytesPerFrozenImageIdentitySummary: Int,
    val reservedMaxLoweredContractFactReferenceCount: Int?,
    val reservedMaxContractSyntaxDependencyEdges: Int?,
    val reservedMaxContractSyntaxSccMemberCount: Int?,
    val maxCollisionGroupSize: Int,
    val maxSccMemberCount: Int,
    val maxSccCanonicalBytes: Int,
    val maxSccIntraReferenceCount: Int,
    val maxSccSealIterations: Int,
    val maxSccPreflightBytes: Int,
    val maxSccSizeOnlyPassBytes: Int,
    val maxSccBudgetReservationBytes: Int,
    val inlineVerifierPrefixBits: Int,
    val defaultInternHidWidthBits: Int,
    val routeHidWidthBits: Int,

    /*
     * Decoder and sorting DoS guards.
     */
    val maxCanonicalMessageNestingDepth: Int,
    val maxCanonicalDecoderFrameCount: Int,
    val maxRatifiedTag32: Int,
    val maxGeneratedDecoderTableEntries: Int,
    val maxGeneratedDecoderTableBytes: Int,
    val maxCanonicalObjectNestingDepth: Int,
    val maxCanonicalObjectFieldCount: Int,
    val maxCanonicalObjectReferenceCount: Int,
    val maxCanonicalObjectEncodedBytes: Int,
    val maxCanonicalEncoderFrameCount: Int,
    val maxInlineFieldPayloadBytes: Int,
    val maxInlineFieldPayloadCountPerRecord: Int,
    val maxTotalInlineFieldPayloadBytesPerRecord: Int,
    val maxCanonicalSortKeyBytes: Int,
    val maxCanonicalSortTieBreakComparisons: Int,
    val maxCanonicalSortTieBreakBytes: Long,
    val maxCanonicalSortScratchBytesPerScope: Long,
    val maxCanonicalSortScratchBytesPerLane: Long,
    val maxCanonicalSortElementCountPerCollection: Int,
    val maxCanonicalTieGroupCountPerCollection: Int,
    val maxCanonicalTieGroupDescriptorBytes: Long,
    val maxCanonicalSortProjectionLevelCount: Int,
    val maxCanonicalExactCloneScanBytes: Long,
    val maxCanonicalColdSortGroupSize: Int,
    val maxCanonicalColdSortComparisons: Int,
    val maxCanonicalColdSortBytes: Long,
)
``````

The exact API may change, but the existence of an explicit resolved policy is normative.

For ADR-0041 v1, the shared metadata identity interner admits exactly the following active intern-candidate domains:

``````text
TypeReference
TypeCycleKey
TypeCycleIdentityPrecheck
TypeShapeSummary
RawFactRecord
ActiveMemberKey
LocalSelectorTuple
RuntimeBindingSnapshot
``````

`LOWERED_CONTRACT_FACT` is not part of the active v1 shared-interner formula.

It may be added only after the top-level contract model ratifies the lowered contract fact vocabulary, lowering law,
canonical fields, version bundle, and verification payload.

Identity domains used for route keys, planning cache keys, canonical plan-node summaries, frozen image content
summaries, replay manifests, or local frozen table membership are not part of the ADR-0041 v1 shared metadata interner
candidate count.

The lowered-contract-fact fields are reserved capacity slots.

They are inactive until the top-level contract document ratifies the contract fact vocabulary and lowering law.

They do not make annotation descriptors, DSL clauses, compiler metadata, generated indexes, or frontend syntax material
canonical identity material.

#### Approved v1 bootstrap identity caps

The v1 bootstrap caps for active ADR-0041 identity surfaces are:

``````text
maxCanonicalBytesPerTypeReference              = 16 KiB
maxCanonicalBytesPerRawFactRecord              = 64 KiB
maxCanonicalBytesPerInternCandidate            = 64 KiB
maxCanonicalBytesPerFrozenImageIdentitySummary = 256 KiB
maxCollisionGroupSize                          = 8
maxSccMemberCount                              = 256
maxSccCanonicalBytes                           = 1 MiB
maxSccIntraReferenceCount                      = 4096
maxSccSealIterations                           = 2
maxSccPreflightBytes                           = 256 KiB
maxSccSizeOnlyPassBytes                        = 1 MiB
maxSccBudgetReservationBytes                   = 1 MiB
inlineVerifierPrefixBits                       = 128
defaultInternHidWidthBits                      = 128
routeHidWidthBits                              = 64
maxCanonicalMessageNestingDepth                = 64
maxCanonicalDecoderFrameCount                  = 1024
maxRatifiedTag32                               = 65535
maxGeneratedDecoderTableEntries                = 65536
maxGeneratedDecoderTableBytes                  = 512 KiB
maxCanonicalObjectNestingDepth                 = 64
maxCanonicalObjectFieldCount                   = 1024
maxCanonicalObjectReferenceCount               = 4096
maxCanonicalObjectEncodedBytes                 = 64 KiB
maxCanonicalEncoderFrameCount                  = 1024
maxInlineFieldPayloadBytes                     = 16
maxInlineFieldPayloadCountPerRecord            = 64
maxTotalInlineFieldPayloadBytesPerRecord       = 512
maxCanonicalSortKeyBytes                       = 256
maxCanonicalSortTieBreakComparisons            = 4096
maxCanonicalSortTieBreakBytes                  = 1 MiB
maxCanonicalSortScratchBytesPerScope           = 16 MiB
maxCanonicalSortScratchBytesPerLane            = 2 MiB
maxCanonicalSortElementCountPerCollection      = 65_535
maxCanonicalTieGroupCountPerCollection         = 65_535
maxCanonicalTieGroupDescriptorBytes            = 1 MiB
maxCanonicalSortProjectionLevelCount           = 3
maxCanonicalExactCloneScanBytes                = 16 MiB
maxCanonicalColdSortGroupSize                  = 4096
maxCanonicalColdSortComparisons                = 65_536
maxCanonicalColdSortBytes                      = 16 MiB
``````

Reserved lowered-contract caps are intentionally not active v1 constants in this ADR.

They must be supplied only after the contract model is ratified.

These values are v1 bootstrap policy constants.

They are not universal hardware laws.

They do, however, make the active identity protocol bounded.

A future policy may raise or lower these values only through explicit policy ratification, golden-vector updates, and
benchmark evidence.

However, ordinary resource profiles SHOULD NOT primarily scale per-unit identity fuses such as
`maxCanonicalBytesPerTypeReference`.

Numeric caps are required because an unbounded canonical-byte equality check is not a real bound.

---

### 7.5.1. Unit-Cap and Aggregate-Budget Law

Metadata identity caps are resolved policy values.

They are not semantic identity algorithms.

Changing a cap may change whether an oversized model is accepted or rejected, but it MUST NOT change the canonical
bytes, HID derivation, canonical ordering, collision verification outcome, stable intern id assignment law, or equality
result for material that remains within the accepted bound.

ADR-0041 distinguishes **per-unit identity fuses** from **aggregate profile budgets**.

A per-unit identity fuse bounds one canonical unit.

Examples:

- `maxCanonicalBytesPerTypeReference` bounds one TypeReference-local canonical identity payload;
- `maxCanonicalBytesPerTypeCycleKey` bounds one TypeCycleKey canonical identity payload;
- `maxCanonicalBytesPerTypeCycleIdentityPrecheck` bounds one TypeCycleIdentityPrecheck canonical identity payload;
- `maxCanonicalBytesPerTypeShapeSummary` bounds one TypeShapeSummary canonical identity payload;
- `maxCanonicalBytesPerRawFactRecord` bounds one raw-fact-record canonical identity payload;
- `maxCanonicalBytesPerActiveMemberKey` bounds one active-member-key canonical identity payload;
- `maxCanonicalBytesPerLocalSelectorTuple` bounds one local-selector-tuple canonical identity payload;
- `maxCanonicalBytesPerRuntimeBindingSnapshot` bounds one runtime-binding-snapshot canonical identity payload;
- `maxCanonicalBytesPerInternCandidate` bounds one intern candidate canonical identity payload;
- future `maxCanonicalBytesPerLoweredContractFact`, once ratified, would bound one lowered-contract-fact canonical
  identity payload.

An aggregate profile budget bounds how many canonical units, table rows, graph edges, total canonical bytes, or physical
slab/table bytes a scope may admit.

Examples:

- `maxTypeReferenceCount`;
- `maxTotalTypeReferenceCanonicalBytes`;
- `maxTypeCycleKeyCount`;
- `maxTotalTypeCycleKeyCanonicalBytes`;
- `maxTypeCycleIdentityPrecheckCount`;
- `maxTotalTypeCycleIdentityPrecheckCanonicalBytes`;
- `maxTypeShapeSummaryCount`;
- `maxTotalTypeShapeSummaryCanonicalBytes`;
- `maxRawFactRecordCount`;
- `maxTotalRawFactRecordCanonicalBytes`;
- `maxActiveMemberKeyCount`;
- `maxTotalActiveMemberKeyCanonicalBytes`;
- `maxLocalSelectorTupleCount`;
- `maxTotalLocalSelectorTupleCanonicalBytes`;
- `maxRuntimeBindingSnapshotCount`;
- `maxTotalRuntimeBindingSnapshotCanonicalBytes`;
- `maxInternCandidateCount`;
- `maxTotalInternCandidateCanonicalBytes`;
- future `maxLoweredContractFactCount`, once ratified;
- future `maxTotalLoweredContractFactCanonicalBytes`, once ratified;
- `maxTotalFrozenImageIdentityBytes`;
- `maxTraversalEdges`;
- `maxFrozenTableBytes`;
- `maxDiagnosticEvidenceBytes`.

Resource profiles MUST align with the planning/resource-policy surface.

The accepted deployment-facing resource intent vocabulary is:

``````text
AUTO
SMALL
STANDARD
LARGE
``````

These names are resource intent inputs.

They are not direct cap-editing surfaces.

`DIAGNOSTIC` and `RESEARCH` are not resource profile values in ADR-0041.

Diagnostic collection may have its own diagnostic evidence budget, but diagnostic evidence budget is not a semantic
identity profile and does not authorize larger identity payloads by itself.

Resource profiles scale aggregate budgets first.

They do not normally scale how large one TypeReference, raw fact record, intern candidate, or future lowered contract
fact is allowed to become.

Reason:

``````text
large input program / large frozen image
-> more canonical units
-> more table rows
-> more graph edges
-> more total canonical bytes
-> larger aggregate slabs and indexes

not:

large profile
-> one canonical unit may absorb an unbounded graph closure
``````

`maxCanonicalBytesPerTypeReference` is a per-TypeReference fuse.

It protects the invariant that TypeReference identity remains TypeReference-local identity and does not recursively
absorb raw facts, lowered contract facts, diagnostics, annotation graphs, or the whole canonical contract graph.

`AUTO`, `SMALL`, `STANDARD`, `LARGE`, and every concrete policy set selected by `AUTO` SHOULD use the same
`maxCanonicalBytesPerTypeReference` value unless a domain-specific policy ratifies a different value with justification.

For v1, the ordinary resource-profile-invariant value is:

``````text
maxCanonicalBytesPerTypeReference = 16 KiB
``````

Per-unit identity fuses are protocol safety limits.

They are not user-authored configuration values.

Ordinary users, project configuration files, deployment profiles, and `AUTO` resolution MUST NOT raise per-unit identity
fuses.

A per-unit fuse may differ from the default only as a versioned, protocol-ratified domain exception owned by Kontrakt's
identity policy schema.

Such an exception MUST document:

- why the unit legitimately needs a larger identity payload;
- why the material cannot be split into more canonical units;
- why layered intern references are insufficient;
- which golden vectors cover the larger payload;
- which diagnostics distinguish legitimate large material from recursive closure leakage;
- and which benchmark threshold table covers the larger verification path.

The same principle applies to other per-unit caps.

If a raw fact record, intern candidate, or future lowered contract fact routinely requires profile-scaled per-unit caps,
the owning domain MUST re-check whether the payload should be split, layered, referenced by stable intern id, or moved
into a cold aggregate/table identity surface.

Image-level summary caps are different.

`maxCanonicalBytesPerFrozenImageIdentitySummary` is an aggregate image-summary surface and MAY scale with image/profile
size through resolved policy because it summarizes many already-validated units.

### 7.5.2. Resolved Cap Surface and Non-User-Knob Law

ADR-0041 does not define a user-editable arbitrary cap file.

Concrete numeric caps are resolved policy outputs, not user-authored protocol values.

A user or operator MAY select a coarse resource intent such as:

``````text
AUTO
SMALL
STANDARD
LARGE
``````

A user or operator MUST NOT be required to author internal cap numbers such as:

``````text
maxCanonicalBytesPerTypeReference
maxTotalTypeReferenceCanonicalBytes
maxRawFactRecordCount
maxInternCandidateCount
maxTraversalEdges
maxFrozenTableBytes
``````

Those numbers belong to the metadata identity capacity solver and the resolved policy snapshot.

The lawful shape is:

``````text
resource intent
+ policy epoch
+ backend capability snapshot
+ environment resource snapshot
+ graph-size estimate where available
+ solver id/version
-> MetadataIdentityCapacitySolver
-> ResolvedMetadataIdentityPolicy
``````

The admitted identity scope observes only `ResolvedMetadataIdentityPolicy`.

It does not observe the user's resource intent directly.

It does not observe a mutable configuration map.

It does not observe live runtime feedback.

This keeps Kontrakt's user surface focused on contracts, boundaries, state, policy, and protocol meaning rather than on
internal table and byte-ledger tuning.

### 7.5.3. Explicit Cap Dependency and Feasibility Law

Count caps, aggregate byte budgets, table budgets, and per-unit fuses are not independent knobs.

A resolved metadata identity policy MUST satisfy explicit feasibility relationships.

ADR-0041 intentionally writes these relationships with full policy field names instead of symbolic abbreviations.

#### TypeReference identity capacity relationship

For every admitted TypeReference canonical identity payload:

``````text
actualCanonicalBytesPerTypeReference <= maxCanonicalBytesPerTypeReference
``````

For the admitted TypeReference domain inside one identity scope:

``````text
actualTypeReferenceCount <= maxTypeReferenceCount
actualTotalTypeReferenceCanonicalBytes <= maxTotalTypeReferenceCanonicalBytes
``````

The resolved policy MUST also satisfy:

``````text
maxTypeReferenceCount * minimumEncodedBytesPerTypeReference
<= maxTotalTypeReferenceCanonicalBytes
``````

Reason:

``````text
If maxTotalTypeReferenceCanonicalBytes is smaller than the minimum bytes required to hold
maxTypeReferenceCount TypeReference identities, then maxTypeReferenceCount is an impossible decorative cap.
``````

The resolved policy SHOULD also satisfy:

``````text
maxTotalTypeReferenceCanonicalBytes
<= maxTypeReferenceCount * maxCanonicalBytesPerTypeReference
``````

Reason:

``````text
If the aggregate TypeReference byte budget is larger than the maximum bytes that can ever be consumed by the admitted
TypeReference count and per-unit fuse, then the aggregate byte budget cannot bind before the other two caps bind.
``````

A policy MAY intentionally choose a lower aggregate budget than the worst-case product:

``````text
maxTotalTypeReferenceCanonicalBytes
< maxTypeReferenceCount * maxCanonicalBytesPerTypeReference
``````

This is expected.

The ordinary case should assume that most TypeReference identities are much smaller than the per-unit fuse.

The deterministic solver may therefore use versioned estimated TypeReference byte sizes and safety factors, provided the
estimator is deterministic and benchmarked.

#### Target-average budget tightening relationship

The feasibility window above is a safety envelope, not a sizing formula.

A released solver MUST NOT treat the worst-case product as the ordinary aggregate budget merely because it is legal.

For TypeReference identity, the ordinary aggregate budget SHOULD be derived from a tighter deterministic relationship
such as:

``````text
maxTotalTypeReferenceCanonicalBytes
<= maxTypeReferenceCount
 * targetAverageCanonicalBytesPerTypeReference
 * typeReferenceCanonicalBytesSafetyMultiplier
``````

The fields `targetAverageCanonicalBytesPerTypeReference` and `typeReferenceCanonicalBytesSafetyMultiplier` are
solver-owned policy constants or solver outputs.

They are not user-authored knobs.

A released solver that does not use these exact names MUST document the semantically equivalent target-average sizing
relationship.

Equivalent target-average relationships SHOULD exist for other large aggregate identity domains, for example:

``````text
maxTotalRawFactRecordCanonicalBytes
<= maxRawFactRecordCount
 * targetAverageCanonicalBytesPerRawFactRecord
 * rawFactRecordCanonicalBytesSafetyMultiplier

maxTotalInternCandidateCanonicalBytes
<= maxInternCandidateCount
 * targetAverageCanonicalBytesPerInternCandidate
 * internCandidateCanonicalBytesSafetyMultiplier
``````

The target-average relationship MUST still remain inside the hard feasibility envelope:

``````text
maxTypeReferenceCount * minimumEncodedBytesPerTypeReference
<= maxTotalTypeReferenceCanonicalBytes
<= maxTypeReferenceCount * maxCanonicalBytesPerTypeReference
``````

This prevents both impossible count caps and excessive over-allocation caused by treating every admitted unit as if it
were near the per-unit fuse.

#### Deterministic Fixed-Point Capacity Arithmetic Law

Capacity solver arithmetic is policy material.

It MUST be deterministic across supported platforms.

A released solver MUST NOT use floating-point arithmetic for identity capacity budgets, target-average sizing, safety
multipliers, scratch budgets, or admitted/rejected boundary decisions.

Forbidden in capacity calculation:

- `Float`;
- `Double`;
- platform floating-point rounding mode;
- FMA-dependent results;
- extended-precision register behavior;
- locale-dependent decimal parsing;
- or host math-library behavior.

Allowed forms:

- checked integer arithmetic;
- fixed-point rational arithmetic;
- power-of-two shift arithmetic;
- saturating checked arithmetic only where the saturation law is explicit;
- deterministic ceiling division.

A target-average relationship written mathematically as:

``````text
maxTotalTypeReferenceCanonicalBytes
<= maxTypeReferenceCount
 * targetAverageCanonicalBytesPerTypeReference
 * typeReferenceCanonicalBytesSafetyMultiplier
``````

MUST be implemented with explicit integer fields such as:

``````text
targetAverageCanonicalBytesPerTypeReference
typeReferenceCanonicalBytesSafetyMultiplierNumerator
typeReferenceCanonicalBytesSafetyMultiplierDenominator
``````

The required deterministic calculation shape is:

``````text
ceilDivChecked(
    checkedMultiply(
        checkedMultiply(maxTypeReferenceCount, targetAverageCanonicalBytesPerTypeReference),
        typeReferenceCanonicalBytesSafetyMultiplierNumerator
    ),
    typeReferenceCanonicalBytesSafetyMultiplierDenominator
)
``````

Rules:

- numerator and denominator are unsigned integer policy values;
- denominator MUST be non-zero;
- every multiplication MUST be checked before division;
- overflow MUST fail closed during policy resolution;
- rounding MUST use the ratified ceiling/floor law for that relationship;
- the same resolved policy snapshot MUST produce the same concrete byte budget on every supported platform.

The same fixed-point law applies to every equivalent target-average relationship for raw facts, intern candidates,
active metadata domains, canonical sort scratchpads, cold exact sort budgets, diagnostic evidence budgets, and future
lowered contract fact budgets.

#### Raw-fact-record identity capacity relationship

For every admitted raw fact record canonical identity payload:

``````text
actualCanonicalBytesPerRawFactRecord <= maxCanonicalBytesPerRawFactRecord
``````

For the admitted raw fact record domain inside one identity scope:

``````text
actualRawFactRecordCount <= maxRawFactRecordCount
actualTotalRawFactRecordCanonicalBytes <= maxTotalRawFactRecordCanonicalBytes
``````

The resolved policy MUST satisfy:

``````text
maxRawFactRecordCount * minimumEncodedBytesPerRawFactRecord
<= maxTotalRawFactRecordCanonicalBytes
``````

The resolved policy SHOULD satisfy:

``````text
maxTotalRawFactRecordCanonicalBytes
<= maxRawFactRecordCount * maxCanonicalBytesPerRawFactRecord
``````

If raw fact records routinely hit the per-record fuse, the owning domain MUST investigate whether record material should
be split into more canonical records, referenced through sealed TypeReference identity, or moved into a bounded table
summary surface.

#### Additional active metadata-domain capacity relationships

The following active ADR-0041 v1 metadata domains MUST have the same count / bytes / per-unit fuse relationship shape as
TypeReference and RawFactRecord:

- TypeCycleKey;
- TypeCycleIdentityPrecheck;
- TypeShapeSummary;
- ActiveMemberKey;
- LocalSelectorTuple;
- RuntimeBindingSnapshot.

For every admitted TypeCycleKey canonical identity payload:

``````text
actualCanonicalBytesPerTypeCycleKey <= maxCanonicalBytesPerTypeCycleKey
actualTypeCycleKeyCount <= maxTypeCycleKeyCount
actualTotalTypeCycleKeyCanonicalBytes <= maxTotalTypeCycleKeyCanonicalBytes
maxTypeCycleKeyCount * minimumEncodedBytesPerTypeCycleKey
<= maxTotalTypeCycleKeyCanonicalBytes
maxTotalTypeCycleKeyCanonicalBytes
<= maxTypeCycleKeyCount * maxCanonicalBytesPerTypeCycleKey
``````

For every admitted TypeCycleIdentityPrecheck canonical identity payload:

``````text
actualCanonicalBytesPerTypeCycleIdentityPrecheck <= maxCanonicalBytesPerTypeCycleIdentityPrecheck
actualTypeCycleIdentityPrecheckCount <= maxTypeCycleIdentityPrecheckCount
actualTotalTypeCycleIdentityPrecheckCanonicalBytes <= maxTotalTypeCycleIdentityPrecheckCanonicalBytes
maxTypeCycleIdentityPrecheckCount * minimumEncodedBytesPerTypeCycleIdentityPrecheck
<= maxTotalTypeCycleIdentityPrecheckCanonicalBytes
maxTotalTypeCycleIdentityPrecheckCanonicalBytes
<= maxTypeCycleIdentityPrecheckCount * maxCanonicalBytesPerTypeCycleIdentityPrecheck
``````

For every admitted TypeShapeSummary canonical identity payload:

``````text
actualCanonicalBytesPerTypeShapeSummary <= maxCanonicalBytesPerTypeShapeSummary
actualTypeShapeSummaryCount <= maxTypeShapeSummaryCount
actualTotalTypeShapeSummaryCanonicalBytes <= maxTotalTypeShapeSummaryCanonicalBytes
maxTypeShapeSummaryCount * minimumEncodedBytesPerTypeShapeSummary
<= maxTotalTypeShapeSummaryCanonicalBytes
maxTotalTypeShapeSummaryCanonicalBytes
<= maxTypeShapeSummaryCount * maxCanonicalBytesPerTypeShapeSummary
``````

For every admitted ActiveMemberKey canonical identity payload:

``````text
actualCanonicalBytesPerActiveMemberKey <= maxCanonicalBytesPerActiveMemberKey
actualActiveMemberKeyCount <= maxActiveMemberKeyCount
actualTotalActiveMemberKeyCanonicalBytes <= maxTotalActiveMemberKeyCanonicalBytes
maxActiveMemberKeyCount * minimumEncodedBytesPerActiveMemberKey
<= maxTotalActiveMemberKeyCanonicalBytes
maxTotalActiveMemberKeyCanonicalBytes
<= maxActiveMemberKeyCount * maxCanonicalBytesPerActiveMemberKey
``````

For every admitted LocalSelectorTuple canonical identity payload:

``````text
actualCanonicalBytesPerLocalSelectorTuple <= maxCanonicalBytesPerLocalSelectorTuple
actualLocalSelectorTupleCount <= maxLocalSelectorTupleCount
actualTotalLocalSelectorTupleCanonicalBytes <= maxTotalLocalSelectorTupleCanonicalBytes
maxLocalSelectorTupleCount * minimumEncodedBytesPerLocalSelectorTuple
<= maxTotalLocalSelectorTupleCanonicalBytes
maxTotalLocalSelectorTupleCanonicalBytes
<= maxLocalSelectorTupleCount * maxCanonicalBytesPerLocalSelectorTuple
``````

For every admitted RuntimeBindingSnapshot canonical identity payload:

``````text
actualCanonicalBytesPerRuntimeBindingSnapshot <= maxCanonicalBytesPerRuntimeBindingSnapshot
actualRuntimeBindingSnapshotCount <= maxRuntimeBindingSnapshotCount
actualTotalRuntimeBindingSnapshotCanonicalBytes <= maxTotalRuntimeBindingSnapshotCanonicalBytes
maxRuntimeBindingSnapshotCount * minimumEncodedBytesPerRuntimeBindingSnapshot
<= maxTotalRuntimeBindingSnapshotCanonicalBytes
maxTotalRuntimeBindingSnapshotCanonicalBytes
<= maxRuntimeBindingSnapshotCount * maxCanonicalBytesPerRuntimeBindingSnapshot
``````

A released solver MAY use target-average sizing relationships for these domains, but those relationships MUST remain
inside the explicit feasibility envelope above.

#### Intern-candidate identity capacity relationship

For every admitted intern candidate canonical identity payload:

``````text
actualCanonicalBytesPerInternCandidate <= maxCanonicalBytesPerInternCandidate
``````

For the admitted intern candidate domain inside one identity scope:

``````text
actualInternCandidateCount <= maxInternCandidateCount
actualTotalInternCandidateCanonicalBytes <= maxTotalInternCandidateCanonicalBytes
``````

The resolved policy MUST satisfy:

``````text
maxInternCandidateCount * minimumEncodedBytesPerInternCandidate
<= maxTotalInternCandidateCanonicalBytes
``````

The resolved policy SHOULD satisfy:

``````text
maxTotalInternCandidateCanonicalBytes
<= maxInternCandidateCount * maxCanonicalBytesPerInternCandidate
``````

For ADR-0041 v1, the shared metadata identity interner admits exactly these active intern-candidate domains:

- TypeReference;
- TypeCycleKey;
- TypeCycleIdentityPrecheck;
- TypeShapeSummary;
- RawFactRecord;
- ActiveMemberKey;
- LocalSelectorTuple;
- RuntimeBindingSnapshot.

Therefore, the resolved policy MUST satisfy:

``````text
maxInternCandidateCount
>= maxTypeReferenceCount
 + maxTypeCycleKeyCount
 + maxTypeCycleIdentityPrecheckCount
 + maxTypeShapeSummaryCount
 + maxRawFactRecordCount
 + maxActiveMemberKeyCount
 + maxLocalSelectorTupleCount
 + maxRuntimeBindingSnapshotCount
``````

`LOWERED_CONTRACT_FACT` is not part of the active v1 formula.

It may be added only after the top-level contract model ratifies the lowered contract fact vocabulary, lowering law,
canonical fields, version bundle, and verification payload.

The resolved policy MUST also satisfy the equivalent byte-budget relationship:

``````text
maxTotalInternCandidateCanonicalBytes
>= maxTotalTypeReferenceCanonicalBytes
 + maxTotalTypeCycleKeyCanonicalBytes
 + maxTotalTypeCycleIdentityPrecheckCanonicalBytes
 + maxTotalTypeShapeSummaryCanonicalBytes
 + maxTotalRawFactRecordCanonicalBytes
 + maxTotalActiveMemberKeyCanonicalBytes
 + maxTotalLocalSelectorTupleCanonicalBytes
 + maxTotalRuntimeBindingSnapshotCanonicalBytes
``````

Identity domains used for route keys, planning cache keys, canonical plan-node summaries, frozen image content
summaries,
replay manifests, or local frozen table membership MUST NOT be included in the shared metadata interner formula merely
because they have HID domains elsewhere in ADR-0041.

If a future ADR moves any of those domains into the shared metadata interner scope, that ADR MUST amend this formula,
add count and byte budget fields, and add cap-boundary golden vectors.

If interners are domain-local instead of shared, then each domain-local interner must define the corresponding
relationship explicitly.

#### Traversal-edge and table-budget relationship

`maxTraversalEdges` is not independent from the admitted unit counts.

The resolved policy MUST define how traversal edges are derived from admitted graph dimensions.

At minimum, it must bound:

``````text
maxTraversalEdges
>= maxShapeReferenceEdges
 + maxRawFactReferenceEdges
 + future maxContractFactReferenceEdges, once ratified
``````

The resolved policy MUST also provide an upper derivation such as:

``````text
maxTraversalEdges
<= maxTypeReferenceCount * maxTraversalEdgesPerTypeReference
``````

or an equivalent domain-split derivation such as:

``````text
maxTraversalEdges
<= maxShapeReferenceEdges
 + maxRawFactReferenceEdges
 + future maxContractFactReferenceEdges, once ratified
``````

The constants `maxTraversalEdgesPerTypeReference`, `maxShapeReferenceEdges`, `maxRawFactReferenceEdges`, and future
`maxContractFactReferenceEdges` are solver-owned policy constants or solver outputs.

They are not user-authored knobs.

`maxFrozenTableBytes` must be derived from admitted table row counts, table layouts, load factors, coverage bitsets,
slab offset tables, and implementation-required headroom.

It MUST NOT be an unrelated arbitrary number.

#### Scope-level budget relationship

A resolved metadata identity scope MUST satisfy:

``````text
maxTotalTypeReferenceCanonicalBytes
+ maxTotalTypeCycleKeyCanonicalBytes
+ maxTotalTypeCycleIdentityPrecheckCanonicalBytes
+ maxTotalTypeShapeSummaryCanonicalBytes
+ maxTotalRawFactRecordCanonicalBytes
+ maxTotalActiveMemberKeyCanonicalBytes
+ maxTotalLocalSelectorTupleCanonicalBytes
+ maxTotalRuntimeBindingSnapshotCanonicalBytes
+ maxTotalInternCandidateCanonicalBytes
+ future maxTotalLoweredContractFactCanonicalBytes, once ratified
+ maxFrozenTableBytes
+ maxIdentityFrontierBytes
+ maxIdentityDependencyGraphBytes
+ maxInternTableBytes
+ maxDiagnosticEvidenceBytes
+ fixedMetadataIdentityOverheadBytes
<= maxMetadataIdentityScopeBytes
``````

All fields in this relationship are resolved policy outputs.

If a released implementation does not expose a field with exactly one of these names, it MUST still document the
semantically equivalent resolved field.

The important rule is that total scope capacity is the result of a deterministic byte-ledger equation, not a pile of
independent constants.

#### Solver evidence

Every released metadata identity capacity solver MUST publish:

- the solver id/version;
- the supported resource intent vocabulary;
- the resolved field list;
- the dependency relationships among counts, byte totals, table bytes, and per-unit fuses;
- the deterministic estimator used for ordinary unit sizes, if any;
- the benchmark corpus used to calibrate the estimator;
- the cap-boundary golden vectors;
- and the failure diagnostics emitted when a feasibility relationship is violated.

A resolved policy that violates its own feasibility relationships is invalid and MUST fail before scope admission.

### 7.5.4. AUTO Deterministic Aggregate Budget Solver Law

`AUTO` is the default resource policy mode shared with planning.

`AUTO` is not a separate semantic identity profile.

`AUTO` is a deterministic policy-resolution mode.

In v1, `AUTO` is intentionally conservative and MUST resolve to the deterministic `STANDARD` bootstrap cap set.

In v2, `AUTO` MAY become a deterministic aggregate-budget solver.

The solver exists to choose an appropriate total capacity envelope for local development, large cloud runners, MSA-scale
module graphs, deep-learning-assisted environments, and other large software settings without compromising
determinism.

The solver may scale aggregate budgets such as:

- maximum admitted TypeReference count;
- maximum raw-fact record count;
- maximum intern-candidate count;
- maximum total canonical bytes per identity scope;
- maximum frozen table / slab bytes;
- maximum traversal edges;
- maximum diagnostic evidence bytes;
- maximum worker/session aggregate memory envelope.

The solver MUST NOT change canonical meaning, canonical byte encoding, canonical ordering, HID derivation, collision
verification, stable intern id assignment law, query-key equality, or PlanCacheKey equality.

The solver MUST NOT normally scale per-unit identity fuses such as `maxCanonicalBytesPerTypeReference`.

The solver formula and every solver input class MUST be versioned.

For the same admitted input graph, policy snapshot, backend capability snapshot, environment resource snapshot, solver
version, and ratified estimation inputs, the solver MUST produce the same concrete resolved cap set.

The solver MUST run before scope admission.

After scope admission, the resolved cap set is immutable for that scope.

Forbidden `AUTO` inputs include:

- GC timing;
- current free-memory fluctuations;
- current CPU load;
- cache warmth;
- branch predictor behavior;
- thread scheduling;
- runtime throughput observed after scope admission;
- adaptive feedback that changes caps inside an admitted scope.

Allowed hardware or environment awareness is limited to deterministic pre-admission snapshots.

The rule is:

``````text
AUTO adjusts how much work the scope may admit.
AUTO does not change what the admitted work means.
``````

The exact aggregate profile table is policy material, not protocol identity material.

However, every released profile table MUST use the same public resource-profile vocabulary as planning:

``````text
AUTO
SMALL
STANDARD
LARGE
``````

`AUTO` resolves to a concrete cap set before scope admission.

In v1 this concrete cap set is the deterministic `STANDARD` bootstrap set.

In v2 it may be produced by a deterministic aggregate-budget solver, but the solver output is still a fixed resolved
policy snapshot for the admitted scope.

It is not allowed to mutate caps inside an admitted identity scope.

The solver MUST NOT use GC timing, current free-memory fluctuations, current CPU load, cache warmth, thread scheduling,
observed runtime throughput, or any other live feedback channel to change caps after admission.

Every released profile table MUST be:

- versioned;
- documented;
- benchmarked;
- covered by cap-boundary tests;
- and tied to a `ResolvedMetadataIdentityPolicy`.

If canonical material exceeds a resolved cap, the implementation MUST fail closed with structured diagnostics.

Diagnostics SHOULD include:

- identity domain;
- whether the violated cap is a per-unit fuse, aggregate budget, table budget, or scope budget;
- configured cap;
- required encoded byte count;
- largest contributing fields;
- generic nesting depth where applicable;
- child reference count where applicable;
- future-ratified lowered-contract byte contribution where applicable;
- frontend syntax clause count where diagnostic-safe after contract-model ratification;
- selected resource intent;
- resolved concrete profile set;
- suggested higher resource intent when the violation is aggregate;
- suggested payload split / layered reference boundary when the violation is per-unit;
- violated feasibility relationship where applicable;
- backend evidence only when diagnostic-safe.

The diagnostic payload itself remains budgeted and sanitized.

A cap violation MUST NOT degrade into digest-only equality, partial interning, recursive closure inlining, or unstable
fallback identity.

### 7.5.5. Contract Syntax Lowering Caps

Reserved.

ADR-0041 does not ratify concrete contract syntax lowering caps.

This section is kept as an integration point because future lowered contract material will need bounded identity
behavior.

The future top-level contract document must define, before activation:

- whether lowered contract facts exist as a ratified identity domain;
- canonical bytes per lowered contract fact;
- reference count per lowered contract fact;
- contract syntax dependency edge caps, if contract syntax dependency graphs exist;
- contract syntax SCC member caps, if contract syntax SCCs exist;
- diagnostic payload caps for lowering failures.

Until then:

- no annotation syntax cap is an identity cap;
- no DSL syntax cap is an identity cap;
- no lowered-contract-fact byte cap is active in ADR-0041;
- no contract syntax SCC cap is active in ADR-0041.

Once ratified, any such caps MUST obey ADR-0041's fail-closed, diagnostic-budget, and deterministic identity rules.

## 8. Canonical Byte Encoding Law

### 8.1. Encoding Form

Kontrakt adopts a binary tagged, length-prefixed canonical encoding.

ADR-0041 ratifies `CanonicalEnvelopeHeader` as the mandatory common envelope header for canonical identity bytes.

The current fixed 64-byte layout is selected by `canonicalEncodingVersion32 = 1`.

The encoding format for `canonicalEncodingVersion32 = 1` is:

``````text
CanonicalEnvelopeHeader
FieldTable
PayloadBytes
``````

`CanonicalEnvelopeHeader` is always present.

`payloadLength32` is mandatory.

Domain-specific variation MUST be represented through:

- identity domain id;
- domain schema version;
- version-bundle fingerprint;
- field tags;
- field table entries;
- payload fields;
- and the owning domain compatibility matrix.

Domain-specific variation MUST NOT be represented by changing the common envelope header layout.

Each field table entry describes:

- field tag;
- wire type;
- criticality / compatibility behavior where encoded by the domain;
- byte offset into `PayloadBytes`;
- byte length;
- and repeated-field or SCC-local reference metadata where applicable.

Field payload bytes are interpreted only after:

- header validation;
- field-table bounds validation;
- domain / schema / version compatibility classification;
- and unknown-tag policy validation.

### 8.2. Field Tags

Every encoded field MUST have a stable numeric field tag.

Rules:

- field tags are protocol ids;
- released field tags MUST NOT be reused;
- removed fields become reserved;
- field order in the byte stream MUST be ascending by field tag unless the domain explicitly defines repeated ordered
  fields;
- repeated ordered fields must carry explicit order semantics;
- repeated unordered fields must be sorted under the ratified canonical ordering law before encoding.

### 8.3. Primitive Encoding

Primitive encoding rules:

- signed integers: fixed-width two's-complement little-endian unless a domain explicitly ratifies varint;
- unsigned integer bit patterns: fixed-width little-endian raw bit pattern;
- booleans: one byte `0x00` or `0x01`;
- enum values: explicit protocol integer id, never enum ordinal;
- floating-point values: forbidden in metadata identity unless a domain explicitly ratifies canonical IEEE
  representation and NaN policy;
- byte arrays: length-prefixed raw bytes;
- strings: length-prefixed UTF-8 bytes from already-ratified immutable `String`.

### 8.3.1. Wire Type Registry and SCC-Local Reference Boundary

Every canonical wire type is protocol material.

The v1 common wire type id space is:

``````text
WIRE_TYPE_FIXED_U64        = 0
WIRE_TYPE_FIXED_I64        = 1
WIRE_TYPE_BYTES            = 2
WIRE_TYPE_STRING_UTF8      = 3
WIRE_TYPE_MESSAGE          = 4
WIRE_TYPE_REPEATED         = 5
WIRE_TYPE_SCC_LOCAL_REF    = 6
WIRE_TYPE_RESERVED         = 7
``````

A wire type MUST define:

- numeric protocol id;
- payload length rule;
- alignment rule where applicable;
- whether the payload may appear in hot headers, cold payloads, repeated fields, or SCC seal payloads;
- unknown-field behavior;
- decoder bounds checks;
- and golden vectors.

`WIRE_TYPE_RESERVED` MUST NOT appear in a released identity payload.

A decoder MUST fail closed if it observes `WIRE_TYPE_RESERVED` unless a later compatibility matrix ratifies a concrete
meaning for the active domain/schema/version.

ADR-0041 permits `WIRE_TYPE_SCC_LOCAL_REF` only for metadata identity SCC sealing.

It means:

``````text
deterministic temporary ordinal valid only inside the canonical SCC seal payload
``````

Rules:

- it is valid only inside an ADR-0041 metadata identity SCC seal encoding;
- it encodes a deterministic SCC-local temporary ordinal as a bounded unsigned integer payload;
- it is not a stable intern id;
- it is not a provisional handle;
- it is not a frozen ordinal;
- it is not a planning node id;
- it cannot appear in published canonical material outside the SCC seal procedure;
- it cannot cross into `FrozenMetamodelImage`, planning-facing providers, `PlanCacheKey`, `CanonicalPlanNode`,
  persistent artifacts, public DTOs, or query-key surfaces;
- it MUST be resolved to sealed stable identity material before publication outside the SCC seal boundary.

`WIRE_TYPE_SCC_LOCAL_REF` is lawful only when the enclosing canonical envelope sets:

``````text
HEADER_FLAG_SCC_SEAL_PAYLOAD = 0x0001
``````

The SCC seal boundary is self-describing canonical byte material.

It MUST NOT be supplied by:

- `ThreadLocal`;
- decoder-global mutable state;
- caller-local ad hoc parameters;
- backend object context;
- recursion stack identity;
- or process-global parser mode.

If `WIRE_TYPE_SCC_LOCAL_REF` appears when `HEADER_FLAG_SCC_SEAL_PAYLOAD` is not set, decoding MUST fail closed.

If `HEADER_FLAG_SCC_SEAL_PAYLOAD` is set for an identity domain or schema that does not ratify SCC sealing, decoding
MUST
fail closed.

### 8.4. String Encoding

String encoding law:

- input must be an immutable already-ratified `String`;
- encoding is UTF-8;
- length prefix is UTF-8 byte length;
- platform default charset is forbidden;
- `Charset.defaultCharset()` is forbidden;
- delimiter-joined string encoding is forbidden;
- unpaired surrogate must have been rejected before this boundary;
- encoder must fail closed if an unpaired surrogate is observed defensively;
- no Unicode normalization is performed by the encoder;
- no locale transformation is performed by the encoder.

### 8.4.1. Early Text Ratification and Surrogate Preflight Law

Unpaired surrogate rejection SHOULD occur at the earliest backend-erased text ratification boundary.

The canonical encoder MUST keep its defensive fail-closed check, but the ordinary path MUST NOT rely on late encoder
rejection after expensive canonicalization, sorting, interning, or graph construction work.

Required direction:

``````text
backend text evidence
-> backend-erased text ratification
-> surrogate / malformed-text preflight
-> canonical material
-> canonical bytes
``````

Forbidden ordinary path:

``````text
backend text evidence
-> expensive graph canonicalization
-> late string encoder discovers malformed text
-> repeated expensive fail-closed rejection
``````

A late encoder rejection remains lawful as a defense-in-depth guard.

It is not the expected first line of defense.

### 8.5. Collection Encoding

Collection encoding rules:

- ordered collections encode elements in ratified order;
- unordered semantic sets must be sorted under canonical ordering before encoding;
- maps encode entries ordered by canonical key material;
- duplicate canonical map keys fail closed;
- unordered semantic sets MUST define an explicit duplicate canonical element policy;
- collection count is encoded before elements;
- element count must be bounded by the owning capacity policy;
- null element policy must be explicit per domain.

An unordered collection encoder MUST NOT rely on:

- backend iteration order;
- platform collection order;
- platform hash-table order;
- worker completion order;
- object identity;
- JVM `hashCode()`;
- or live profiling feedback.

### 8.5.1. Canonical Collection Sorting Cost and Sort-Key Law

Canonical sorting is a DoS boundary.

Unordered collection canonicalization MUST be deterministic, but it MUST NOT become an unbounded recursive-comparator
path.

A compliant encoder MUST bound:

- collection element count;
- canonical sort-key byte length;
- comparator fallback depth;
- total canonical bytes read during tie-breaking;
- total sort scratch memory;
- and total sort work admitted for one canonical unit or SCC seal payload.

Unordered collection canonicalization MUST precompute bounded canonical sort keys before sorting.

The ordinary sorting comparator MUST NOT recursively traverse full child metadata, raw facts, nested messages, payload
trees, staging slabs, or canonical byte trees during pairwise comparison.

A canonical sort key MUST be bounded by resolved metadata identity policy.

Accepted sort-key material includes fixed-width or otherwise bounded material such as:

- identity domain id;
- identity domain schema version;
- version-bundle fingerprint;
- canonical byte length;
- HID or digest projection where already available at that boundary;
- inline verifier prefix;
- field-count summary;
- fixed-width canonical key prelude.

A bounded canonical byte slice MAY be used only as a metered final tie-breaker.

Full canonical byte comparison remains the final deterministic tie-break authority.

It MUST be metered and cap-bounded.

If the tie-breaker budget is exceeded, canonicalization MUST NOT fall back to unbounded recursive comparison.

The ordinary forbidden path is:

``````text
sort comparator
-> recursively walk full metadata graph
-> compare deep child structures repeatedly
-> repeat for O(n log n) or worse comparator calls
``````

The required ordinary path is:

``````text
element canonical material
-> bounded canonical sort key
-> deterministic sort by sort key
-> adjacent tie-group extraction
-> projection escalation for tie groups when available
-> metered full canonical byte tie-break only when needed
``````

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxCanonicalSortKeyBytes
maxCanonicalSortTieBreakComparisons
maxCanonicalSortTieBreakBytes
``````

The following are forbidden in ordinary unordered-collection sorting:

- comparator-driven recursive metadata traversal;
- comparator-driven decoding of nested messages;
- comparator-driven raw-fact expansion;
- comparator-driven backend handle traversal;
- comparator-driven staging-slab traversal;
- fallback to platform collection iteration order;
- fallback to object identity or JVM `hashCode()`.

A released implementation MUST provide golden vectors or tests proving that shuffled unordered inputs produce the same
encoded bytes without relying on backend iteration order, platform collection order, object identity, hash table order,
or
recursive graph traversal timing.

### 8.5.2. Tie-Group Primitive Clustering and Projection Escalation Law

Only equal bounded-sort-key ranges may enter tie-break or bounded cold exact sorting.

A compliant implementation MUST extract tie groups as adjacent primitive ranges after bounded sort-key ordering.

Tie-group extraction MUST NOT allocate object graphs such as:

- `List<List<Node>>`;
- per-group heap lists;
- per-element wrapper objects;
- object bucket maps;
- platform hash sets;
- or backend handle collections.

Tie groups MUST be represented by primitive scratchpad descriptors such as:

``````text
startIndex
endExclusiveIndex
sortKeyOffset
sortKeyLength
``````

or an equivalent primitive range representation.

A compliant implementation SHOULD use in-place clustering, two-pointer range scanning, or index-array partitioning
inside
the already admitted primitive scratchpad.

It MUST NOT create unbounded heap allocation churn while extracting tie groups.

Projection escalation is permitted only inside tie groups.

A lawful escalation sequence is:

``````text
SortKeyLevel0:
  domain + schema + length + bounded digest projection

SortKeyLevel1:
  full HID / verifier prefix where already available

SortKeyLevel2:
  bounded canonical byte prelude

Final:
  exact canonical byte lexicographic comparison
``````

The number of projection levels MUST be bounded by resolved policy.

Projection escalation MUST be selected by domain/schema/version/resolved policy before canonicalization begins.

It MUST NOT be selected by runtime profiling, GC behavior, worker timing, input iteration order, or platform sort
behavior.

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxCanonicalTieGroupCountPerCollection
maxCanonicalTieGroupDescriptorBytes
maxCanonicalSortProjectionLevelCount
``````

If tie-group descriptor budget is insufficient, canonicalization MUST fail closed before publishing canonical bytes, HID
material, interner candidates, frozen image tables, planning-visible providers, report manifests, or persistent
artifacts.

### 8.5.3. Bounded Cold Exact Sort Path Law

Tie-breaker exhaustion MUST NOT publish partially ordered material.

If bounded canonical sort keys and ratified projection escalation do not produce a total order within the ordinary
tie-break budget, the encoder has only two lawful outcomes:

1. enter a ratified bounded cold exact sort path before publication; or
2. fail the current identity scope closed.

A bounded cold exact sort path is not a semantic relaxation path.

It is a deterministic pre-publication fallback.

A bounded cold exact sort path, if ratified, MUST:

- remain inside canonicalization;
- operate only on tie groups, not the whole collection, unless the entire collection is one tie group;
- complete exact deterministic ordering before publication;
- obey its own comparison-count and byte-read budgets;
- use non-recursive canonical byte-slice comparison;
- never decode backend handles;
- never traverse metadata graphs through object pointers;
- never expose quarantined or partially ordered material to planning;
- never expose quarantined or partially ordered material to planning-facing providers;
- never expose quarantined or partially ordered material to `PlanCacheKey`;
- never expose quarantined or partially ordered material to `CanonicalPlanNode`;
- never expose quarantined or partially ordered material to interner publication;
- never expose quarantined or partially ordered material to frozen image publication;
- never expose quarantined or partially ordered material to report manifests;
- never expose quarantined or partially ordered material to persistent artifacts;
- never change canonical equality semantics;
- never accept digest-only ordering when exact canonical bytes are required;
- emit structured diagnostics if the cold bound is exceeded.

If no ratified bounded cold exact sort path exists, ordinary tie-breaker exhaustion MUST fail the current identity scope
closed.

Planning, providers, `PlanCacheKey`, `CanonicalPlanNode`, interner publication, frozen image publication, persistent
artifacts, and report manifests MUST NOT observe unordered, quarantined, or partially sorted material.

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxCanonicalColdSortGroupSize
maxCanonicalColdSortComparisons
maxCanonicalColdSortBytes
``````

Cold exact sort budgets are not semantic meaning.

They are deterministic safety fuses.

Changing them may change whether a pathological scope is accepted or rejected, but it MUST NOT change canonical order,
canonical bytes, HID derivation, collision verification, stable intern id assignment, or equality for material that
remains within the accepted bounds.

### 8.5.4. Exact Clone Group Law

An exact clone group is a tie group whose members have byte-identical canonical element material.

An exact clone group MUST NOT trigger all-to-all pairwise canonical byte comparison.

A compliant implementation MUST classify exact clone groups by a bounded adjacent exact scan after deterministic
ordering
or within a bounded cold exact sort path.

For exact byte-identical elements:

- no additional ordering distinction exists;
- object identity MUST NOT be used to break the tie;
- backend order MUST NOT be used to break the tie;
- worker completion order MUST NOT be used to break the tie;
- JVM `hashCode()` MUST NOT be used to break the tie.

Collection semantics decide how exact clone groups are handled:

- ordered collections preserve multiplicity under the ratified order;
- repeated unordered collections preserve multiplicity after canonical ordering;
- maps reject duplicate canonical keys;
- unordered semantic sets follow the owning domain's explicit duplicate canonical element policy.

If the owning domain chooses deterministic de-duplication for unordered semantic sets, the de-duplication MUST happen
before publication and MUST be golden-vector covered.

If the owning domain chooses fail-closed duplicate rejection, duplicate canonical elements MUST fail closed before
publication.

A clone bomb is therefore bounded by:

- collection element count caps;
- canonical sort scratchpad caps;
- exact clone scan byte caps;
- duplicate-key / duplicate-element policy;
- and bounded cold exact sort caps where ratified.

The resolved metadata identity policy MUST define, or map to a semantically equivalent field:

``````text
maxCanonicalExactCloneScanBytes
``````

If exact clone scan budget is exceeded, canonicalization MUST fail closed unless a stronger ratified domain duplicate
policy decides the outcome before the scan requires more work.

### 8.5.5. Canonical Sort Scratchpad Budget Law

Canonical sort key precomputation MUST consume an explicit transient scratchpad budget.

The encoder MUST NOT allocate unbounded per-element sort-key objects.

For an unordered collection with `elementCount`, the encoder MUST prove before sorting that:

``````text
elementCount <= maxCanonicalSortElementCountPerCollection
``````

and:

``````text
elementCount * actualSortKeyWidthBytes <= remainingCanonicalSortScratchBytes
``````

and:

``````text
actualSortKeyWidthBytes <= maxCanonicalSortKeyBytes
``````

If the scratchpad budget is insufficient, canonicalization MUST fail closed before allocating, filling, publishing, or
partially exposing the sort-key arena.

Sort scratch memory is transient.

It MUST NOT cross into:

- canonical identity publication;
- frozen image tables;
- planning-facing providers;
- protocol-owned interner published tables;
- report manifests;
- public DTOs;
- persistent artifacts.

Concurrent lanes MUST consume from resolved aggregate and per-lane scratch budgets.

They MUST NOT use live heap availability, GC timing, worker timing, or observed memory pressure as dynamic admission
inputs after the scope has been admitted.

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxCanonicalSortScratchBytesPerScope
maxCanonicalSortScratchBytesPerLane
maxCanonicalSortElementCountPerCollection
``````

The lawful shape is:

``````text
resolved scratch budget
-> bounded transient sort-key arena
-> primitive tie-group range descriptors
-> deterministic sort
-> ordered canonical payload
-> scratch arena becomes unreachable
``````

The forbidden shape is:

``````text
live heap availability
-> opportunistic sort-key allocation
-> heap object tie-group buckets
-> partial sort-key arena escapes
-> frozen / planning / report / interner surface retains scratch memory
``````

### 8.5.6. Map Duplicate Key Deterministic Phase Law

Duplicate canonical map keys MUST be detected in a dedicated deterministic phase.

Duplicate detection MUST NOT be performed as an incidental side effect of:

- a sorting comparator;
- a pivot comparison;
- a platform sort callback;
- a platform hash-table collision path;
- a worker completion race;
- a lane merge race;
- or a diagnostic formatting path.

A compliant encoder MUST use one of the following phase shapes.

Shape A:

``````text
key canonicalization
-> exact key id / exact key bytes available
-> deterministic duplicate scan
-> bounded canonical sort
-> encode
``````

Shape B:

``````text
key canonicalization
-> bounded sort-key precomputation
-> deterministic sort
-> adjacent exact-key duplicate scan
-> encode
``````

If duplicate keys are found, encoding MUST fail closed before publishing:

- canonical bytes;
- HID material;
- interner candidates;
- stable intern ids;
- frozen image tables;
- planning-visible providers;
- `PlanCacheKey`;
- `CanonicalPlanNode`;
- persistent artifacts;
- or report manifests.

The duplicate diagnostic SHOULD report canonical key evidence under `maxDiagnosticEvidenceBytes`.

The amount of work consumed before duplicate detection MUST be bounded and deterministic under the resolved policy.

A duplicate-key failure may have different physical work cost depending on the selected lawful phase shape, but that
phase shape MUST be selected by domain/schema/version/resolved policy before canonicalization begins.

It MUST NOT be selected by platform sort behavior, worker timing, input iteration order, or live runtime profiling.

### 8.5.7. Deterministic Sort Projection and Randomized Seed Rejection Law

Canonical sort keys are semantic-ordering material.

They MUST NOT include:

- per-process random seeds;
- per-scope random seeds;
- SipHash keys generated from runtime entropy;
- system time;
- thread id;
- worker id;
- GC state;
- heap address;
- ASLR-dependent values;
- non-ratified entropy;
- or any value that is not part of canonical identity material.

Randomized sort-key perturbation is forbidden for canonical ordering.

Reason:

``````text
same admitted semantic material
+ different runtime seed
-> different sort order
-> different canonical bytes
-> different HID / interner behavior
``````

A released implementation MAY use keyed, randomized, or salted hashing only for non-authoritative route/probe structures
when all of the following hold:

- the randomized value is not encoded into canonical bytes;
- it does not affect canonical ordering;
- it does not affect stable intern id assignment;
- exact canonical verification remains the equality authority;
- and changing the seed cannot change accepted semantic meaning.

HashDoS defense for canonical sorting MUST instead use deterministic mechanisms:

- stronger bounded sort projections;
- tie-group-local projection escalation;
- exact canonical byte tie-break inside bounded budgets;
- bounded cold exact sort path before publication where ratified;
- exact clone group handling;
- duplicate-key / duplicate-element policy;
- and scope fail-closed when resolved deterministic budgets are exceeded.

Deterministic projection strength is the canonical HashDoS defense for ordering.

A released domain sort policy SHOULD use the strongest deterministic projection already available at that boundary.

Recommended projection priority:

``````text
identityDomainId32
domainSchemaVersion32
versionBundleFingerprint128
canonicalByteLength
full HID128 where already available
inlineVerifierPrefix128
fieldCount / shape summary where ratified
bounded canonical byte prelude
``````

If full `HID128` is already available for an element before collection ordering, the ordinary bounded sort key SHOULD
include
it unless the owning domain documents a stronger deterministic alternative.

If full `HID128` is not yet available, the domain MUST define which deterministic projection levels are available and
which budgeted escalation path is used.

A sort policy MUST NOT intentionally use a weak projection merely to avoid computing available deterministic identity
material.

Adapter-level abuse throttling may reject or defer external requests before ADR-0041 admission, but admitted canonical
material MUST be ordered by deterministic protocol material only.

If a domain wants a keyed deterministic projection, the key MUST be protocol-ratified, versioned, included in the
relevant
version-axis material, and stable for the same admitted semantic material.

It MUST NOT be live randomness.

### 8.5.8. Nested Collection Bottom-Up Canonicalization Law

Nested unordered collections create a dependency order.

For example:

``````text
Set<Set<TypeReference>>
``````

The outer set cannot be canonically sorted until each inner set has produced stable canonical sort material.

A compliant encoder MUST NOT sort an outer unordered collection by recursively comparing unresolved child collections.

Instead, nested collection canonicalization MUST use one of the following lawful shapes.

Shape A:

``````text
inner collection canonicalization
-> sealed child canonical bytes / verified child identity handle
-> outer collection bounded sort key
-> outer deterministic sort
``````

Shape B:

``````text
bottom-up dependency plan
-> child collection sort-key material
-> child collection seal
-> parent collection sort-key material
-> parent collection seal
``````

Shape C:

``````text
demand-driven child seal
-> memoized sealed child material
-> parent sort consumes sealed child handle
``````

Shape C is lawful only if the demand-driven seal is deterministic, memoized inside the admitted scope, and consumes
resolved scratch / frame / byte budgets.

Forbidden shape:

``````text
outer sort comparator
-> recursively enter inner set comparator
-> recursively traverse TypeReference graph
-> repeat during pairwise comparison
``````

Nested collection canonicalization MUST be bounded by:

- object / message nesting depth caps;
- canonical sort scratchpad caps;
- canonical encoder frame caps;
- child seal byte caps;
- tie-group caps;
- and SCC seal caps where cycles exist.

If child canonical material cannot be sealed within the resolved policy, the parent collection MUST fail closed before
publication.

This law does not require eager whole-world encoding.

It requires that material used as an outer sort key is already sealed, verified, or memoized under deterministic budget
control.

### 8.6. Object / Record Encoding

Object encoding in ADR-0041 means encoding a domain-ratified canonical record or message payload.

It does not mean serializing a JVM object graph.

Object encoding rules:

- object type is encoded by identity domain id and domain schema version, not by JVM class name;
- implementation class names are forbidden unless they are semantic material in that domain;
- field absence is meaningful only when the domain declares it meaningful;
- default values must be encoded explicitly or prohibited;
- unknown fields in persisted payloads follow the unknown-tag law and are not accepted silently by this ADR;
- object fields are encoded only from the domain-ratified schema field set;
- object encoding MUST NOT discover fields through reflection, Kotlin data-class component order, Java serialization,
  Jackson, kotlinx serialization, JVM declaration order, or backend descriptor traversal.

### 8.6.1. Object Is Not JVM Object Law

A canonical object is a protocol record.

It is not:

- a JVM heap object;
- a Kotlin data class instance;
- a Java bean;
- a reflection object;
- a KSP symbol object;
- a compiler AST / PSI node;
- a Spring bean;
- a framework DTO;
- or a general serialization target.

A compliant encoder MUST receive already-ratified canonical material or domain-owned field material.

It MUST NOT recursively inspect arbitrary JVM object fields to discover identity material.

It MUST NOT use:

- JVM object identity;
- implementation class name;
- reflection field order;
- Kotlin declaration order;
- backend descriptor order;
- framework serializer order;
- map iteration order;
- or object `hashCode()`

as object identity material unless the owning domain explicitly ratifies that material as semantic.

### 8.6.2. Schema-Owned Field Set Law

Every object field that enters canonical bytes MUST be owned by the active identity domain schema.

A field MUST have:

- stable numeric field tag;
- wire type;
- presence law;
- default law;
- repetition law;
- ordering law;
- compatibility behavior;
- and byte-budget relationship.

Fields not declared by the active domain schema are unknown fields.

Unknown fields are rejected by default unless the active compatibility matrix explicitly ratifies them as skippable and
non-critical.

A decoder MUST NOT infer unknown field meaning from:

- field name text;
- JVM property name;
- reflection metadata;
- framework annotations;
- serializer descriptors;
- source order;
- or backend handle identity.

### 8.6.3. Non-Recursive Object Encoding Law

Object encoding MUST be bounded by deterministic structural limits.

A compliant encoder MUST NOT rely on JVM call-stack depth as the object nesting bound.

The encoder MUST use one of:

- explicit bounded encode frames;
- explicit bounded decode frames;
- SCC seal processing;
- stable intern id references;
- or ratified non-recursive traversal.

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxCanonicalObjectNestingDepth
maxCanonicalObjectFieldCount
maxCanonicalObjectReferenceCount
maxCanonicalObjectEncodedBytes
maxCanonicalEncoderFrameCount
``````

`maxCanonicalDecoderFrameCount` may be shared with the message decoder frame budget when the implementation uses one
unified canonical decode-frame stack.

A payload may remain within byte caps and still be rejected for excessive object/message nesting, field count, reference
count, encoder-frame count, or decoder-frame count.

Object nesting depth and `WIRE_TYPE_MESSAGE` nesting depth may share the same resolved policy fields if the
implementation uses one unified canonical frame budget.

### 8.6.4. Object Reference and Cycle Boundary Law

Canonical object encoding MUST NOT recursively inline arbitrary reachable object graphs.

If an object field references another identity-bearing object, the field MUST encode one of the following ratified
forms:

- sealed stable intern id;
- verified canonical identity handle;
- domain-ratified local selector material;
- SCC-local reference valid only inside an SCC seal payload;
- or an explicitly embedded nested message whose depth and byte budget are bounded.

A parent object MUST NOT reference:

- provisional handles outside SCC seal;
- backend object handles;
- reflection objects;
- KSP symbols;
- JVM object identity;
- frozen ordinals as persistent identity;
- planning node ids;
- runtime object addresses;
- Spring `ApplicationContext`;
- framework bean handles;
- or serializer descriptor objects.

Cycles MUST be handled by SCC sealing or by stable reference material.

They MUST NOT be handled by recursive object graph traversal.

### 8.6.5. Field Presence, Default, and Duplicate Field Law

Field presence is protocol material only when the owning domain declares it meaningful.

If a field has a default value, the domain MUST choose exactly one policy:

- encode the effective default explicitly;
- prohibit omission;
- treat omission as a distinct canonical state;
- or reject the field configuration.

A decoder MUST fail closed on duplicate non-repeated field tags.

Repeated fields MUST declare whether they are:

- ordered and order-bearing;
- unordered and canonically sorted;
- duplicate-preserving;
- duplicate-rejecting;
- or duplicate-collapsing under a domain-ratified law.

A decoder MUST fail closed if a repeated field appears in a form that violates the active repetition law.

Default handling MUST NOT depend on:

- JVM default constructor behavior;
- Kotlin default parameter masks;
- reflection default values;
- annotation proxy default material;
- framework binder defaults;
- or serializer library defaults

unless those values have already been lowered into Kontrakt-owned canonical material by a ratified frontend/lowering
law.

### 8.6.6. Object Decoding Publication Law

Decoded object material is not semantic identity merely because it was parsed successfully.

Before publication, a decoded object MUST pass:

- envelope validation;
- field table validation;
- schema/version compatibility validation;
- unknown-tag policy validation;
- field presence/default validation;
- duplicate field validation;
- repeated field ordering / duplicate policy validation;
- object depth/frame budget validation;
- reference boundary validation;
- cycle/SCC validation where applicable;
- and exact canonical identity verification where compact identity is used.

A decoder MUST NOT publish partially decoded object material to:

- frozen image tables;
- protocol-owned interner tables;
- planning-facing providers;
- `PlanCacheKey`;
- `CanonicalPlanNode`;
- report manifests;
- public DTOs;
- or persistent artifacts.

Publication requires a single validated ownership transition into:

- sealed canonical bytes;
- a verified canonical byte handle;
- a sealed interned identity entry;
- a frozen-image-owned slab;
- or another artifact-owned immutable surface approved by the owning protocol.

### 8.6.7. Object Encoding Failure Law

Object encoding failures are scope-local fail-closed events.

They MUST NOT become:

- best-effort partial records;
- warning-only canonical material;
- planning-visible placeholder objects;
- backend object fallbacks;
- framework serializer fallbacks;
- or diagnostic-only substitutes for canonical identity.

A failed object encoding MUST leave no published canonical bytes, HID material, interner candidate, stable intern id,
frozen table row, planning provider entry, report manifest entry, or persistent artifact entry for the failed object.

Diagnostic evidence for object encoding failure MUST be bounded by `maxDiagnosticEvidenceBytes`.

### 8.7. No General Serialization Dependency

Canonical byte encoding MUST NOT depend on:

- Java serialization;
- Kotlin serialization default output;
- Jackson;
- kotlinx JSON order;
- reflection field order;
- data class generated component order;
- JVM class declaration order;
- compiler plugin incidental order.

Canonical encoding is a hand-ratified protocol.

### 8.8. Canonical Encoding Physical Layout Law

Canonical byte encoding is a physical protocol surface, not merely a serialization format.

A compliant encoding MUST be designed for:

- deterministic byte equality;
- bounded decoding;
- branch-predictable hot-path dispatch;
- fixed-width hot metadata reads;
- prefetch-friendly field layout;
- delayed pointer chasing into cold variable payloads;
- and mechanical sympathy with primitive table lookup and interner probe paths.

The encoder MUST place hot fixed-width metadata before cold variable-length material.

Every v1 identity envelope MUST begin with `CanonicalEnvelopeHeader`.

The header contains:

- protocol magic;
- header size;
- header flags;
- canonical encoding version;
- identity domain id;
- domain schema version;
- hash / HID suite id;
- HID derivation version;
- version-bundle fingerprint;
- field table offset and length;
- field count;
- payload offset;
- payload byte length;
- reserved zero fields.

Variable-length fields MUST be reached through explicit offset / length material.

A decoder MUST NOT discover hot identity structure through:

- reflection;
- string lookup;
- map lookup;
- JSON parsing;
- delimiter scanning;
- backend descriptor parsing;
- or generic object traversal.

Canonical bytes must be deterministic enough for equality and physical enough for repeated machine consumption.

### 8.9. Fixed-Width Hot Header Law

Identity-bearing canonical encodings MUST separate hot metadata from cold payload bytes.

ADR-0041 ratifies `CanonicalEnvelopeHeader` as the mandatory common envelope header for canonical identity bytes.

The current common layout is selected by:

``````text
canonicalEncodingVersion32 = 1
``````

For `canonicalEncodingVersion32 = 1`, `CanonicalEnvelopeHeader` is a fixed 64-byte little-endian header.

The exact layout is:

``````text
CanonicalEnvelopeHeader, canonicalEncodingVersion32 = 1
64 bytes, little-endian

offset  size  field
0       4     magic32
4       2     headerSize16
6       2     headerFlags16
8       4     canonicalEncodingVersion32
12      4     identityDomain32
16      4     domainSchemaVersion32
20      2     hashSuite16
22      2     hidDerivationVersion16
24      8     versionBundleFingerprintHigh64
32      8     versionBundleFingerprintLow64
40      4     fieldTableOffset32
44      4     fieldTableLength32
48      2     fieldCount16
50      2     reserved16
52      4     payloadOffset32
56      4     payloadLength32
60      4     reserved32
``````

Mandatory header constants and validation rules for `canonicalEncodingVersion32 = 1`:

- `magic32` MUST be `0x4B4E5443`;
- `magic32` represents ASCII `KNTC`;
- `headerSize16` MUST be `64`;
- `canonicalEncodingVersion32` MUST be `1`;
- `canonicalEncodingVersion32` is the common envelope, field-table, wire-type, offset/length, unknown-tag, and canonical
  framing protocol version;
- a future common envelope layout change MUST bump `canonicalEncodingVersion32`;
- a decoder MUST use `magic32`, `headerSize16`, and `canonicalEncodingVersion32` together to select the canonical
  envelope decoder;
- `headerFlags16` MUST use only flags ratified by ADR-0041 or by the active compatibility matrix;
- for `canonicalEncodingVersion32 = 1`, ADR-0041 ratifies `HEADER_FLAG_SCC_SEAL_PAYLOAD = 0x0001`;
- `HEADER_FLAG_SCC_SEAL_PAYLOAD` may be set only for ADR-0041 metadata identity SCC seal payloads;
- every other `headerFlags16` bit MUST be zero unless the active compatibility matrix ratifies a specific flag;
- `reserved16` MUST be zero;
- `reserved32` MUST be zero;
- `payloadLength32` is mandatory;
- all integer fields are little-endian unsigned bit patterns unless the field explicitly states otherwise;
- a decoder MUST fail closed if `magic32`, `headerSize16`, `canonicalEncodingVersion32`, `reserved16`, or `reserved32`
  is invalid;
- unknown non-zero `headerFlags16` bits MUST fail closed unless ratified by ADR-0041 or by the active compatibility
  matrix;
- `fieldCount16` MUST be less than or equal to the resolved field-count cap for the identity domain.

Offset constraints:

- `fieldTableOffset32` MUST be greater than or equal to `64`;
- `fieldTableLength32` MUST be large enough to contain exactly `fieldCount16` field table entries under the active
  field table layout;
- `payloadOffset32` MUST be greater than or equal to `fieldTableOffset32 + fieldTableLength32`;
- `payloadOffset32 + payloadLength32` MUST NOT overflow;
- `payloadOffset32 + payloadLength32` MUST be less than or equal to the envelope byte length;
- no field payload slice may point into the header, field table, reserved padding, or outside the envelope.

The hot header is protocol material.

It is not an implementation object layout.

It is not an intern-table probe group.

It is not allowed to contain backend provenance as semantic identity material.

Domain-specific payload needs MUST be handled by field tables, field tags, domain schema versions, compatibility
matrices,
or payload fields.

They MUST NOT change the common header layout selected by `canonicalEncodingVersion32 = 1`.

### 8.9.1. Canonical Envelope Header vs Intern Probe Projection

The canonical encoding envelope header and the intern-table first-probe group are different physical surfaces.

The canonical envelope header is the self-describing canonical byte protocol.

The intern-table first-probe group is a compact projected lookup surface derived from canonical identity material.

They MUST NOT be forced into the same physical layout.

Priority order:

``````text
1. canonical bytes remain correctness authority;
2. intern probe metadata is a compact cache-local projection;
3. full canonical envelope material is loaded only after inline rejection checks pass.
``````

Therefore, the cache-line grouping law in the interner applies to the projected probe group, not to the full canonical
envelope header.

This resolves the physical tension between self-describing canonical bytes and cache-local intern lookup.

### 8.9.2. Header Reserved Bits and Padding Law

`CanonicalEnvelopeHeader` is fixed by ADR-0041 for each `canonicalEncodingVersion32` layout. The current layout is
`canonicalEncodingVersion32 = 1`.

The following header fields are reserved for `canonicalEncodingVersion32 = 1`:

- `reserved16`;
- `reserved32`;
- every `headerFlags16` bit except `HEADER_FLAG_SCC_SEAL_PAYLOAD = 0x0001` unless a later compatibility matrix ratifies
  that bit.

Reserved bits and bytes are protocol bytes.

They MUST encode as zero.

A decoder MUST fail closed if reserved fields are non-zero unless the active compatibility matrix explicitly ratifies
those bits for the active domain/schema/version.

This rule is part of canonical identity.

Reserved data is not a runtime hint.

It is not an implementation scratch field.

It is not diagnostic storage.

### 8.9.3. Version Bundle Fingerprint Derivation Law

`versionBundleFingerprintHigh64` and `versionBundleFingerprintLow64` are the two 64-bit words of
`VersionBundleFingerprint128`.

`VersionBundleFingerprint128` MUST be derived deterministically from the active `CanonicalIdentityVersionBundle`.

The derivation shape is:

``````text
CanonicalIdentityVersionBundle
-> CanonicalVersionBundlePayload
-> domain-separated BLAKE3 keyed derivation / XOF
-> first 128 bits
-> high64 / low64 encoded as little-endian unsigned bit patterns
``````

`VersionBundleFingerprint128` is a compact compatibility precheck and envelope identity component.

It compresses the active version-bundle tuple into two fixed-width words for branch-bounded hot validation.

It does not authorize ignoring domain/schema compatibility laws.

#### 8.9.3.1. Canonical Version-Bundle Payload Law

`VersionBundleFingerprint128` is derived from canonical version-bundle bytes.

Those bytes MUST have one deterministic encoding.

The canonical version-bundle payload for `canonicalEncodingVersion32 = 1` is:

``````text
CanonicalVersionBundlePayload

versionBundleEncodingVersion32 : u32 little-endian
axisCount16                    : u16 little-endian
reserved16                     : u16, MUST be zero
AxisEntry[axisCount16]
``````

Each `AxisEntry` is:

``````text
axisId32           : u32 little-endian
axisValueWidth16   : u16 little-endian
reserved16         : u16, MUST be zero
axisValueBytes     : byte[axisValueWidth16]
``````

This variable-width tagged-axis structure is selected deliberately.

It allows global and domain-specific version axes to use different ratified widths without introducing delimiter
ambiguity.

It still keeps every axis explicitly tagged, length-bounded, ordered, and byte-exact.

Rules:

- `versionBundleEncodingVersion32` MUST be `1`;
- both `reserved16` fields MUST be zero;
- every `axisId32` MUST be a ratified protocol axis id;
- `axisValueWidth16` MUST be non-zero;
- `axisValueWidth16` MUST match the ratified width for that `axisId32`;
- `axisValueBytes` MUST use the ratified canonical encoding for that `axisId32`;
- integer axis payloads MUST be fixed-width little-endian unsigned bit patterns unless the axis explicitly states
  otherwise;
- varint axis encoding is forbidden unless a future ADR explicitly ratifies it for a specific axis id;
- delimiter-free concatenation is forbidden;
- text axis values are forbidden unless a future ADR ratifies a canonical text axis encoding;
- axis entries MUST be sorted by `axisId32` ascending;
- duplicate `axisId32` values MUST fail closed;
- unknown required axis ids MUST fail closed;
- unknown optional axis ids may be skipped only if the active compatibility matrix explicitly classifies the axis as
  skippable and non-critical;
- skipped axes MUST still pass `axisValueWidth16` and bounds validation;
- skipped axes MUST NOT change canonical identity meaning.

The payload MUST NOT be constructed by:

``````text
axisValue1 || axisValue2 || axisValue3
``````

without tags and widths.

The lawful shape is:

``````text
axisId32
axisValueWidth16
reserved16
axisValueBytes
``````

for every axis.

All offset, count, and width arithmetic used while reading the version-bundle payload MUST use checked arithmetic before
narrowing or indexing.

#### 8.9.3.2. Required Version-Bundle Axes

The canonical version-bundle payload MUST include every global axis that can affect canonical identity behavior.

`identityDomainId32` is mandatory.

It MUST be present even though the same value also appears in `CanonicalEnvelopeHeader`.

Reason:

- the envelope header identifies the payload domain for decoding;
- the version-bundle payload identifies the domain inside the fingerprint input;
- omitting `identityDomainId32` from the fingerprint input would allow two domains with identical version tuples to
  produce the same `VersionBundleFingerprint128`.

For ADR-0041 v1, `identityDomainId32` is assigned the lowest global version-axis id and therefore appears first after
ascending `axisId32` ordering.

All global version axes MUST use physical-width suffixes in their protocol names.

A released global axis name MUST NOT omit its bit width.

Examples of forbidden ambiguous names:

- `compatibilityMatrixVersion`;
- `capabilityProfileVersion`;
- `entropyVersion`;
- `resourcePolicySchemaVersion`;
- `canonicalOrderingAlgorithmVersion`;
- `typeIdentityAlgorithmVersion`.

Examples of valid physical names:

- `compatibilityMatrixVersion32`;
- `capabilityProfileVersion32`;
- `entropyVersion32`;
- `resourcePolicySchemaVersion32`;
- `canonicalOrderingAlgorithmVersion32`;
- `typeIdentityAlgorithmVersion32`.

Required global axes include:

| Axis                                       |   Width | Required when                                                                  |
|--------------------------------------------|--------:|--------------------------------------------------------------------------------|
| `identityDomainId32`                       | 4 bytes | always                                                                         |
| `canonicalEncodingVersion32`               | 4 bytes | always                                                                         |
| `identityDomainVersion32`                  | 4 bytes | always                                                                         |
| `domainSchemaVersion32`                    | 4 bytes | always                                                                         |
| `hashSuite16`                              | 2 bytes | always                                                                         |
| `hidDerivationVersion16`                   | 2 bytes | always                                                                         |
| `compatibilityMatrixVersion32`             | 4 bytes | always                                                                         |
| `capabilityProfileVersion32`               | 4 bytes | always                                                                         |
| `entropyVersion32`                         | 4 bytes | always                                                                         |
| `resourcePolicySchemaVersion32`            | 4 bytes | always                                                                         |
| `metadataIdentityPolicySchemaVersion32`    | 4 bytes | always                                                                         |
| `canonicalEncodingPolicyVersion32`         | 4 bytes | always                                                                         |
| `canonicalOrderingAlgorithmVersion32`      | 4 bytes | always when unordered collection encoding can affect the domain                |
| `typeIdentityAlgorithmVersion32`           | 4 bytes | always when TypeReference identity or type normalization can affect the domain |
| `normalizationVersion32`                   | 4 bytes | when text/type normalization can affect the domain                             |
| `interningProtocolVersion32`               | 4 bytes | when protocol-owned interning can affect the domain                            |
| `sccSealAlgorithmVersion32`                | 4 bytes | when SCC sealing can affect the domain                                         |
| `collisionVerificationPolicyVersion32`     | 4 bytes | when collision verification policy can affect publication                      |
| `stableInternIdAssignmentVersion32`        | 4 bytes | when stable intern id assignment can affect published identity material        |
| `runtimeBindingIdentityAlgorithmVersion32` | 4 bytes | when runtime binding snapshots can affect the domain                           |

The payload MUST also include every domain-specific version axis that can affect:

- canonical bytes;
- field interpretation;
- compatibility classification;
- HID derivation;
- collision verification;
- stable intern id assignment;
- SCC seal behavior;
- sort-key / ordering behavior;
- decoder behavior;
- type identity;
- runtime binding identity;
- or semantic equality.

The payload MUST NOT include axes that affect only:

- display strings;
- diagnostic wording;
- source locations;
- backend names;
- local logging format;
- report styling;
- progress reporting;
- or non-semantic debug labels.

If a diagnostic or reporting version changes semantic diagnostic evidence bytes that are part of identity material, it
is
no longer diagnostic-only and MUST be represented by a ratified version axis with an explicit physical-width suffix.

#### 8.9.3.3. Axis Encoding and Registry Law

Every released version axis MUST define:

- `axisId32`;
- axis name with explicit physical-width suffix where the axis carries an integer value;
- owner;
- semantic meaning;
- fixed or variable width rule;
- exact `axisValueWidth16`;
- canonical byte encoding rule;
- required / optional classification;
- compatibility behavior when missing;
- compatibility behavior when unknown;
- and golden vectors.

Physical-width suffixes are part of the protocol name.

An integer version axis name without an explicit width suffix is not releaseable.

For an integer axis, `axisValueWidth16` MUST match the suffix and registry definition.

Examples:

- a `...Version16` axis MUST use `axisValueWidth16 = 2`;
- a `...Version32` axis MUST use `axisValueWidth16 = 4`;
- a `...Version64` axis MUST use `axisValueWidth16 = 8`.

A mismatch between axis name suffix, registry width, `axisValueWidth16`, or actual `axisValueBytes` length MUST fail
closed.

The axis registry is protocol material.

It MUST NOT be assembled from:

- enum ordinal;
- declaration order;
- map iteration order;
- source order;
- service-loader order;
- annotation order;
- classpath order;
- or process-global registration order.

The global axis registry for ADR-0041 v1 MUST include the required global axes listed in Section 8.9.3.2.

The registry MUST assign stable `axisId32` values to those axes.

`identityDomainId32` MUST be assigned the lowest global axis id.

The registry MAY reserve gaps for future axes, but reserved axis ids MUST NOT be emitted.

`axisId32` values are stable protocol ids.

They MUST NOT be reused for different meanings.

They MUST NOT be reassigned after release.

#### 8.9.3.4. BLAKE3 Derivation and Output Split Law

`VersionBundleFingerprint128` MUST be the first 128 bits of the domain-separated BLAKE3 XOF output over the canonical
version-bundle payload.

The derivation context MUST be domain-separated from ordinary HID derivation and from all other ADR-0041 fingerprints.

The derivation context for `versionBundleEncodingVersion32 = 1` is:

``````text
KONTRAKT_BLAKE3_VERSION_BUNDLE_FINGERPRINT_V1
``````

The first 16 output bytes are split as:

``````text
versionBundleFingerprintHigh64 = bytes[0..8]  interpreted as u64 little-endian
versionBundleFingerprintLow64  = bytes[8..16] interpreted as u64 little-endian
``````

The 128-bit fingerprint is sufficient for compact compatibility precheck under the expected version-bundle cardinality.

It is not a substitute for exact compatibility-matrix law where exact version-bundle material is required.

Two implementations that receive the same active version bundle and the same axis registry MUST compute the same
`VersionBundleFingerprint128`.

A released implementation MUST publish golden vectors for:

- identical bundle -> identical fingerprint;
- canonical encoding version bump;
- domain schema version bump;
- identity domain id change;
- HID derivation version bump;
- compatibility matrix version bump;
- capability profile version bump;
- entropy version bump;
- resource policy schema version bump;
- metadata identity policy schema version bump;
- canonical ordering algorithm version bump;
- type identity algorithm version bump;
- SCC seal algorithm version bump;
- collision verification policy version bump;
- stable intern id assignment version bump;
- runtime binding identity algorithm version bump;
- canonical encoding policy version bump;
- domain-specific version-axis change;
- axis entry order shuffle preserving the same sorted canonical payload;
- duplicate axis id fail-closed;
- unknown required axis id fail-closed;
- unknown optional skippable axis id where ratified;
- axis width mismatch fail-closed;
- axis name physical-width suffix mismatch fail-closed;
- non-zero reserved field fail-closed;
- delimiter-free concatenation rejection.

### 8.10. Variable Payload Offset Table Law

Variable-length payloads MUST NOT force sequential delimiter scanning on hot identity paths.

A compliant encoding MUST provide either:

- an explicit field offset / length table;
- or an equivalent bounded layout that allows direct field location after tag dispatch.

Forbidden hot-path mechanisms:

- scan until delimiter;
- parse JSON object keys;
- search a map for field names;
- invoke reflection to find a field;
- compute field order dynamically;
- depend on backend declaration order.

For repeated fields:

- the element count MUST be encoded explicitly;
- each variable-length element MUST be addressable by length-prefix or by table-derived offset;
- unordered semantic collections MUST be canonicalized before encoding;
- duplicate canonical keys MUST fail closed.

### 8.10.1. Checked Offset, Length, Base, and Linear Slice Validation Law

Offset tables are a security boundary.

All offset and length arithmetic MUST use checked `Long` arithmetic before narrowing to any encoded field width.

A canonical encoder MUST fail closed before emission if any offset, length, table size, or total payload size cannot be
represented by the ratified encoded width.

ADR-0041 distinguishes two offset bases.

Envelope-absolute offsets:

- `fieldTableOffset32` is relative to the first byte of `CanonicalEnvelopeHeader`;
- `payloadOffset32` is relative to the first byte of `CanonicalEnvelopeHeader`;
- `fieldTableOffset32 = 0` is invalid because the field table cannot overlap the header;
- `payloadOffset32 = 0` is invalid because payload bytes cannot overlap the header.

Payload-relative offsets:

- every external field payload slice offset inside the field table is relative to the first byte of the declared payload
  region;
- payload-relative offset `0` means `envelope[payloadOffset32]`;
- payload-relative offset `N` means `envelope[payloadOffset32 + N]` after checked arithmetic;
- field-table slice offsets MUST NOT be interpreted as envelope-absolute offsets.

A canonical decoder MUST validate, before exposing any slice:

- payload-relative offset is non-negative;
- length is non-negative;
- `payloadRelativeOffset + length` does not overflow;
- `payloadRelativeOffset + length <= payloadLength32`;
- `fieldTableOffset32 + fieldTableByteLength` does not overflow;
- `fieldTableOffset32 + fieldTableByteLength <= envelopeLength`;
- `payloadOffset32 + payloadLength32` does not overflow;
- `payloadOffset32 + payloadLength32 <= envelopeLength`;
- every external slice points into the declared payload region after adding `payloadOffset32`;
- no external slice points into the hot header, field table, reserved padding, or outside the envelope.

External payload slice overlap MUST be validated in linear time.

A canonical encoding MUST NOT require the decoder to sort arbitrary slice intervals to prove non-overlap.

For fields whose external payload slices must not overlap, field table entries or their external-payload slice
descriptors
MUST be encoded in physical payload order.

The required linear validation is:

``````text
previousEnd = 0
for each external slice descriptor in physical payload order:
    start = payloadRelativeOffset
    end = checkedAdd(start, length)
    require start >= previousEnd
    require end <= payloadLength32
    previousEnd = end
``````

If the owning domain requires strict physical separation between consecutive external slices, it MUST require:

``````text
start > previousEnd
``````

except when the previous slice is a domain-ratified zero-length semantic value whose descriptor still advances the table
or element index.

If a descriptor sequence is not encoded in physical payload order, the decoder MUST fail closed unless the owning domain
has ratified an alternative linear-time non-overlap proof.

The ordinary decoder MUST NOT perform `O(N log N)` interval sorting as the required overlap defense for adversarial
input.

Overlapping immutable views are forbidden by default.

A domain MAY ratify overlapping immutable views only when all of the following hold:

- the overlap is semantic, not accidental;
- the overlap is representable by a linear proof;
- the overlap cannot expose header, field table, padding, or out-of-envelope bytes;
- exact golden vectors cover the overlap;
- and the overlap cannot change canonical identity meaning.

Forbidden:

``````text
Int offset = previousOffset + fieldLength
// silent wraparound
``````

Forbidden:

``````text
collect all slices
-> sort intervals by offset
-> prove non-overlap with unbounded adversarial interval count
``````

Required:

``````text
checked Long arithmetic
-> base classification
-> range validation
-> physical-order linear slice validation
-> safe narrowing only after validation
-> bounded slice exposure
``````

Integer overflow, negative payload-relative offset, negative length, out-of-payload range, header overlap, field-table
overlap, malformed field-table length, ambiguous offset base, non-linear overlap requirement, or malformed physical
slice
order MUST fail closed.

### 8.10.1.1. Decoder Cursor Progress and Zero-Displacement Law

Checked offset arithmetic is not sufficient by itself.

Every decoder loop that advances through a table, repeated field, TLV-like sequence, nested message, or variable payload
MUST prove strict cursor progress.

A decoder loop MUST maintain a monotonic cursor or entry index such that every successful iteration consumes one of:

- a fixed-width field-table entry;
- a declared repeated-element entry;
- a non-zero encoded prefix;
- a nested envelope with validated total encoded width;
- or a domain-ratified zero-payload value whose containing record still advances by a non-zero encoded record width.

Zero-length semantic values are allowed only when the enclosing encoding still advances.

Allowed examples:

- empty string with a length prefix and a consumed field-table entry;
- empty byte array with a length prefix and a consumed field-table entry;
- empty collection with a consumed collection-count record;
- empty payload in an envelope whose header/table width has already advanced.

Forbidden examples:

``````text
cursor = cursor + elementLength
elementLength = 0
// cursor does not advance
``````

``````text
while (cursor < end) {
    read length = 0
    cursor += length
}
``````

A decoder MUST fail closed if an iteration would leave the cursor, entry index, or remaining-byte accounting unchanged.

This law applies even if all offset and length arithmetic is non-overflowing.

Zero displacement is a parser progress failure, not a valid compact encoding.

### 8.10.1.1A. Branch-Bounded Bounds Validation Implementation Law

Bounds validation is correctness material first and physical optimization second.

A compliant decoder MAY implement offset, length, cursor-progress, and physical-order checks using branch-hoisting,
bitwise aggregation, status-word accumulation, table dispatch, or platform intrinsics.

A compliant decoder MUST NOT remove a required validation check to reduce branches.

A hot validation loop SHOULD avoid unpredictable per-field branch chains when a branch-bounded equivalent is available.

Preferred implementation shape:

``````text
accumulatedInvalidBits = 0
for each descriptor:
    accumulatedInvalidBits |= invalidOffsetBit
    accumulatedInvalidBits |= invalidLengthBit
    accumulatedInvalidBits |= invalidBaseBit
    accumulatedInvalidBits |= invalidProgressBit
    accumulatedInvalidBits |= invalidPhysicalOrderBit

if accumulatedInvalidBits != 0:
    fail closed
``````

This is implementation guidance, not a different semantic rule.

The branch-bounded implementation MUST produce the same accept/reject result as the direct validation law.

Benchmark evidence MAY choose a clearer branched implementation for cold paths.

The following are forbidden:

- skipping checks because they are expensive;
- relying on CPU exception behavior for bounds validation;
- using platform-specific overflow wraparound as validation;
- accepting data after a failed aggregated validation bit;
- changing accepted/rejected payloads based on branch predictor behavior, JIT behavior, or profiling feedback.

### 8.10.1.2. Short Inline Field Payload Encoding Law

Very small variable payloads may suffer unnecessary pointer chasing when every access requires:

``````text
field table entry
-> payload offset
-> external payload slab load
``````

ADR-0041 therefore allows a versioned canonical short-inline field payload mode.

This is canonical encoding material.

It is not an implementation-local optimization.

A field payload may be encoded inline only when all of the following are true:

- the active `canonicalEncodingPolicyVersion32` ratifies short-inline field payload encoding;
- the owning identity domain schema marks the field as inline-eligible;
- the field wire type permits inline representation;
- the payload byte length is less than or equal to `maxInlineFieldPayloadBytes`;
- the field-table / inline-sidecar layout is defined by the active canonical encoding policy;
- the same semantic field value always selects the same inline/external form under the same schema and policy.

A compliant implementation MUST NOT choose inline vs external payload layout using:

- runtime profiling;
- observed field frequency;
- branch predictor feedback;
- GC behavior;
- heap pressure;
- CPU cache miss counters;
- worker timing;
- adapter preference;
- or implementation-local threshold tuning inside an admitted scope.

Lawful shape:

``````text
domain schema + canonicalEncodingPolicyVersion32
-> field inline eligibility
-> deterministic payload length check
-> INLINE_SMALL_PAYLOAD or EXTERNAL_PAYLOAD
-> canonical bytes
``````

Forbidden shape:

``````text
runtime profiling says this field is frequent
-> inline this run
-> external next run
-> different canonical bytes
``````

Short-inline field payload encoding MUST preserve:

- field tag identity;
- wire type identity;
- payload byte exactness;
- unknown-tag behavior;
- offset/length validation;
- zero-displacement protection;
- and canonical ordering.

If short-inline encoding is not ratified by the active `canonicalEncodingPolicyVersion32`, variable payloads MUST use
the
ordinary external payload layout or another ratified bounded layout.

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxInlineFieldPayloadBytes
maxInlineFieldPayloadCountPerRecord
maxTotalInlineFieldPayloadBytesPerRecord
``````

Changing the short-inline threshold or layout requires a `canonicalEncodingPolicyVersion32` change and golden vectors.

It MUST NOT change semantic equality for material that remains representable under both layouts.

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

### 8.10.3. Metadata Identity Binding to ADR-0042 Primitive Lifecycle Governance

ADR-0041 no longer owns the full generic primitive substrate lifecycle law.

The generic laws for:

- zero-copy slice lifetime;
- staging-slice / published-slice type-state separation;
- zero-copy promotion;
- sealed slab fragmentation;
- epoch-based reclamation;
- reader epoch guards;
- engine-owned lane epoch tables;
- M:N worker/lane execution topology;
- asynchronous reclaimer isolation;
- event-ingestion boundaries;
- non-suspending lease boundaries;
- and slow-reader containment

are governed by ADR-0042.

ADR-0041 retains the metadata-identity binding:

- canonical metadata identity bytes MAY use zero-copy decode views only inside bounded decode / verification phases;
- staging-slab slices MUST NOT become metadata identity authority;
- metadata identity publication MUST use sealed canonical bytes, verified canonical byte handles, stable intern ids, or
  frozen-image-owned handles;
- published metadata identity slabs are immutable;
- old metadata identity image epochs are reclaimed only through ADR-0042-compatible epoch reclamation;
- reader epoch ownership is engine-lane-owned, not `ThreadLocal`, worker-owned, coroutine-owned, scheduler-owned, or
  callback-owned;
- asynchronous callbacks and reclaimer notifications are event-ingestion boundaries, not canonical identity mutation
  authority.

Any metadata identity implementation that uses primitive slabs, published images, or zero-copy views MUST conform to
ADR-0042.

### 8.11. Decoder Dispatch and Branch Discipline

Canonical tag decoding MUST be table-driven or switch-table-friendly.

Field tags MAY be dense enough for direct table indexing or jump-table-like lowering where the target runtime can
support it.

However, dense dispatch is lawful only after tag bounds validation.

The hot successful decode path SHOULD avoid unpredictable chained semantic `if/else` dispatch.

Forbidden on the hot decode path:

- reflection-based field dispatch;
- string tag lookup;
- `Map` lookup by tag;
- delimiter scanning;
- backend-specific descriptor parsing;
- generic JSON object traversal.

Allowed:

- bounded dense numeric tag indexing;
- generated decoder tables with explicit tag upper bounds;
- `tableswitch` / `lookupswitch`-friendly dispatch;
- bit-mask extraction of wire type, field family, criticality, and fast flags;
- fail-closed validation branches for unknown tags, invalid wire types, bounds violations, or version mismatch.

The goal is branch-minimal deterministic decoding, not the removal of all validation branches.

A validation branch is lawful when it protects:

- bounds safety;
- unknown field rejection;
- sparse tag upper-bound rejection fixture;
- dense decoder table out-of-range tag rejection fixture;
- `tag32 = Int.MAX_VALUE` fail-closed fixture;
- generated decoder table maximum-size fixture;
- critical field presence;
- schema compatibility;
- canonical ordering validation;
- duplicate field rejection;
- integer overflow rejection;
- sparse-tag / out-of-range tag rejection;
- or malformed UTF-8 / surrogate defense.

### 8.11.1. Unknown Tag Default-Reject and Ratified Skip Law

Unknown tags are rejected by default.

Canonical identity decoding MUST NOT silently skip unknown fields merely because their wire type appears mechanically
skippable.

Reason:

``````text
unknown identity field
-> may carry contract meaning under a newer schema
-> silent skip could make different contract meanings compare as equal
``````

A compatibility matrix MAY ratify deterministic skip behavior only for a specific domain/schema/version combination.

Such a ratified skippable field MUST satisfy all of the following:

- the field is explicitly non-critical;
- the field is declared non-identity-affecting for the decoding compatibility class;
- the wire type has a bounded length rule;
- offset and length validation passes under Section 8.10.1;
- skipping is golden-vector covered;
- skipping cannot change canonical bytes for the active identity version;
- and skipping cannot change HID derivation, collision verification, canonical ordering, or stable intern id assignment.

Required default:

``````text
unknown tag
-> fail closed
``````

Allowed only after compatibility ratification:

``````text
known-skippable future field
-> validate wire type and length
-> deterministic bounded skip
-> continue decoding under declared compatibility class
``````

### 8.11.2. Sparse Tag Bounds and Dense Table Admission Law

Dense numeric tag indexing is an optimization surface.

It is not permission to allocate or index a table from an unbounded attacker-controlled tag value.

Every decoder schema MUST define:

- `maxRatifiedTag32`;
- ratified tag count;
- dense table length where dense dispatch is used;
- unknown-tag policy;
- and maximum generated decoder table size.

Before any direct table indexing, generated-table lookup, jump-table dispatch, or field-family extraction that assumes a
bounded tag domain, the decoder MUST prove:

``````text
tag32 <= maxRatifiedTag32
``````

and, when dense table indexing is used:

``````text
tag32 < denseDecoderTableLength
``````

If either check fails, decoding MUST fail closed before table indexing.

Forbidden shape:

``````text
tag32 = attacker input
-> decoderTable[tag32]
``````

Forbidden shape:

``````text
tag32 = 2_147_483_647
-> allocate table with length tag32 + 1
``````

Lawful shape:

``````text
tag32 = read fixed-width tag
-> checked tag upper bound
-> checked dense table bound where applicable
-> table dispatch or fail closed
``````

Generated decoder tables MUST be produced from the ratified schema.

They MUST NOT be dynamically resized to admit an unknown tag.

They MUST NOT allocate proportional to the largest tag observed in the payload.

If a schema uses sparse tag ids, the decoder MUST use a bounded generated lookup shape such as:

- ratified sorted tag table with bounded binary/search strategy;
- generated perfect-hash-like table with bounded size;
- `lookupswitch`-friendly dispatch;
- or another ratified bounded dispatch strategy.

A released implementation MUST provide golden vectors for:

- `tag32 = maxRatifiedTag32`;
- `tag32 = maxRatifiedTag32 + 1`;
- `tag32 = denseDecoderTableLength`;
- `tag32 = Int.MAX_VALUE`;
- unknown sparse tag fail-closed;
- unknown skippable future tag where explicitly ratified.

### 8.11.3. Message Nesting Depth Law

`WIRE_TYPE_MESSAGE` decoding MUST be bounded by a deterministic structural depth counter.

A decoder MUST NOT rely on JVM call-stack depth as the message nesting bound.

A compliant decoder MUST either:

- use an explicit bounded decode-frame stack; or
- prove that recursive decoding is bounded by `maxCanonicalMessageNestingDepth`.

The resolved metadata identity policy MUST define, or map to semantically equivalent fields:

``````text
maxCanonicalMessageNestingDepth
maxCanonicalDecoderFrameCount
``````

If nested message depth exceeds the resolved bound, decoding MUST fail closed before entering the nested payload.

The bound is independent from byte-size caps.

A payload may be within `maxCanonicalBytesPerInternCandidate` and still be rejected for excessive structural nesting.

A decoder MUST also fail closed if the explicit decode-frame stack would exceed `maxCanonicalDecoderFrameCount`.

Malformed nested messages MUST NOT surface as ordinary JVM `StackOverflowError`.

The ordinary policy outcome is structured fail-closed rejection.

### 8.11.4. Branch-Bounded Text Validation Law

Malformed UTF-8, invalid surrogate material, overlong encodings, and invalid continuation-byte sequences are identity
decoder safety boundaries.

They MUST be rejected before a string becomes canonical identity material.

However, text validation MUST NOT introduce an avoidable unpredictable branch per byte on the ordinary hot path.

A compliant implementation SHOULD use a branch-bounded validation shape, such as:

- ASCII fast-path block validation;
- bitwise invalid-state accumulation;
- table-driven byte-class validation;
- chunked validation;
- platform vector / SIMD validation where available and evidence-backed;
- or another ratified branch-bounded validator.

The constitutional requirement is deterministic accept/reject equivalence.

The implementation MAY choose scalar, table-driven, or vectorized validation as long as:

- the same byte sequence produces the same accept/reject result on every supported platform;
- malformed input fails closed;
- validation does not depend on locale, JVM default charset, platform decoder replacement behavior, or normalization
  side
  effects;
- validation remains bounded by resolved byte caps;
- and benchmark evidence exists for any hot-path acceleration claim.

Forbidden ordinary hot-path shape:

``````text
for each byte:
    unpredictable semantic branch
    update decoder state through backend charset replacement behavior
``````

Forbidden correctness shortcut:

``````text
platform string decoder accepts/replaces malformed bytes
-> use produced String as canonical material
``````

Allowed direction:

``````text
canonical UTF-8 bytes
-> branch-bounded validation
-> explicit fail-closed invalid state
-> ratified immutable String boundary only after validation
``````

Branchless or vectorized validation is an allowed implementation strategy.

It is not a license to remove validation.

A released implementation MUST provide golden vectors for:

- ASCII-only text;
- mixed ASCII and multibyte UTF-8;
- invalid leading byte;
- invalid continuation byte;
- overlong encoding;
- surrogate material where prohibited;
- truncated multibyte sequence;
- maximal accepted valid scalar sequence where ratified;
- and branch-bounded validator equivalence with the canonical scalar validation oracle.

### 8.12. Tag Bit Partitioning Law

Canonical field tags SHOULD reserve bit ranges for fast mechanical dispatch.

A compliant tag vocabulary SHOULD be able to expose, without table lookup:

- field family;
- wire type;
- critical vs optional status;
- repeated-field marker;
- hot vs cold field class;
- and semantic domain subfamily where applicable.

Illustrative shape:

``````text
bits  0..9   : field number within domain
bits 10..12  : wire type
bit      13  : repeated flag
bit      14  : critical field flag
bit      15  : hot field flag
``````

The exact layout is not constitutional.
The constitutional rule is that tag decoding must be mechanically predictable and must not require semantic string
dispatch on the hot path.

### 8.12.1. Tag Decode Benchmark Evidence Law

Tag bit partitioning is an optimization surface, not semantic authority.

A released hot decoder SHOULD publish benchmark evidence for its tag decode strategy.

The evidence SHOULD cover:

- mask / shift extraction cost;
- table dispatch cost;
- successful hot-path branch rate;
- validation branch rate;
- unknown tag rejection path;
- wire-type mismatch rejection path;
- and malformed length rejection path.

The benchmark does not define correctness.

Correctness remains defined by canonical bytes, schema/version compatibility, bounds validation, and fail-closed
decoding.

### 8.13. Varint Restriction for Identity Hot Headers

Variable-length integer encodings are not forbidden globally.

However, varint encoding MUST NOT be used for the fixed-width identity hot header unless a later amendment proves that:

- decode cost is bounded;
- branch predictability is preserved;
- canonical byte equality remains unambiguous;
- malformed overlong encodings fail closed;
- and golden vectors cover every boundary case.

Default rule:

``````text
identity hot header -> fixed-width integers
cold payload counts -> fixed-width unless separately ratified
external artifact compression -> future ADR / artifact format concern
``````

Reason:

Canonical metadata identity is read repeatedly by interners, frozen indexes, and planning/L2 routing surfaces.
The hot path should prefer predictable fixed-width reads over compact but branch-heavy decoding.

### 8.14. Schema-Aware Stripping Law

Canonical identity encoding MUST NOT encode semantic field names as string payload.

Field names are diagnostic material.

Canonical bytes MUST encode identity-bearing structure with:

- protocol-owned numeric field tags;
- wire types;
- schema versions;
- explicit value payloads;
- explicit unknown / unavailable markers where required.

A canonical identity encoder MUST NOT emit strings such as `"name"`, `"type"`, `"owner"`, or `"annotations"` as identity
field names.

A fixed positional physical layout MAY be used only after all of the following have already been validated:

- identity domain;
- canonical encoding version;
- domain schema version;
- version bundle fingerprint;
- field-table compatibility.

Protocol canonical bytes remain tagged unless a later ADR ratifies a fully positional encoding for one closed domain.

Reason:

``````text
schema-aware stripping removes redundant descriptive metadata without relying on run-local compression state
``````

The optimization target is semantic redundancy removal, not generic compression.

### 8.15. Deterministic Canonical Encoding and Observation-Independence Law

Canonical identity encoding is not observation-independent.

It is explicitly stateful where the state is protocol-owned, metered, deterministic, and visible to the encoding law.

A compliant encoder MAY use explicit deterministic protocol state such as:

- encoder cursor;
- bounded encoder frame stack;
- field presence bitmap;
- canonical byte offset;
- arena write offset;
- SCC-local deterministic ordinal state;
- budget ledger;
- diagnostic meter;
- immutable protocol tables;
- sealed intern tables;
- precomputed canonical-base registries;
- fixed schema descriptors;
- resolved metadata identity policy;
- deterministic version-bundle compatibility data.

However, canonical identity output MUST be independent from non-authoritative observation state.

Canonical identity encoding MUST NOT depend on:

- acquisition order;
- backend traversal order;
- thread scheduling;
- previous encoded item;
- recently seen values;
- cache state;
- local arena insertion order;
- callback completion order;
- current run frequency distribution.

An encoder MAY use:

- immutable protocol tables;
- sealed intern tables;
- precomputed canonical-base registries;
- fixed schema descriptors;
- resolved metadata identity policy;
- deterministic version-bundle compatibility data;
- explicit bounded encoder state ratified by this ADR or by a domain-specific canonical encoding policy.

An encoder MUST NOT use:

- mutable run-local observation order;
- previous item state;
- recently seen item state;
- adaptive dictionaries learned from the current run;
- thread-local previous values;
- backend enumeration neighbors;
- acquisition-batch neighbors;
- compression bases chosen by observed frequency;
- hidden environment state;
- ambient scheduler state;
- callback completion state;
- or cache hit/miss history.

Allowed shape:

``````text
canonical material
-> explicit protocol-owned encoder state
-> domain/schema/version-ratified encoding rule
-> canonical bytes
``````

Forbidden shape:

``````text
canonical material
-> compare against previous encoded item
-> emit shorter run-order-dependent delta
``````

Forbidden shape:

``````text
canonical material
-> observe current run frequency distribution
-> choose compression base
-> canonical bytes depend on current run history
``````

The rule is not:

``````text
no state
``````

The rule is:

``````text
explicit deterministic protocol state is allowed
ambient observation state is forbidden
``````

Reason:

Order-sensitive compression, hidden observation state, and current-run adaptation are poison for deterministic identity.

Changing hidden observation state MUST NOT change canonical bytes, HID derivation, collision verification, stable intern
id
assignment, canonical ordering, or semantic equality.

### 8.16. Bit-Packed Field Law

Bit packing is allowed for canonical identity hot material only when the bit layout is protocol-owned and golden-vector
covered.

Every packed field MUST define:

- bit offset;
- bit width;
- signedness;
- endian rule;
- allowed values;
- reserved values;
- invalid values;
- version bump condition;
- golden vectors.

Reserved bits MUST be encoded as zero.

A decoder MUST fail closed if reserved bits are non-zero unless a later compatibility matrix explicitly ratifies those
bits for the active schema version.

Illustrative shape:

``````text
bits  0..11  : identityDomain
bits 12..15  : wireType
bits 16..31  : fieldTag
bits 32..63  : flags / version-local material
``````

The exact shape is domain-specific.

The invariant is normative:

``````text
packed bits are protocol bytes, not implementation hints
``````

### 8.17. Canonical-Base Delta Encoding Law

Delta encoding is allowed only against immutable canonical bases.

The selected base MUST be determined solely by:

- identity domain;
- domain schema version;
- canonical encoding version;
- protocol-owned base id;
- relevant version-bundle compatibility matrix.

Forbidden delta bases:

- previous encoded item;
- recently seen item;
- acquisition-order neighbor;
- backend traversal neighbor;
- thread-local previous value;
- local arena previous value;
- frequency-learned dictionary from the current run;
- adaptive dictionary selected after scope admission.

A delta-encoded identity payload MUST have golden vectors proving byte-equivalent reconstruction of the full canonical
material.

Canonical-base delta is optional.

It is a v2+ compactness mechanism, not a requirement for the initial ADR-0041 implementation.

A domain that does not define a canonical base MUST emit ordinary canonical bytes.

### 8.18. General Compression Boundary

General-purpose compression such as Zstd, LZ4, gzip, or runtime-learned dictionaries MUST NOT be part of hot semantic
identity equality.

Such compression MAY be used for cold external artifacts only if a future artifact format defines:

- compression algorithm id;
- compression version;
- deterministic compressor settings;
- decompression safety bounds;
- canonical pre-compression bytes;
- golden vectors.

Compressed bytes are not canonical identity bytes.

The canonical identity bytes are the uncompressed canonical protocol bytes or a domain-ratified canonical-base delta
representation.

---

## 9. Metadata Identity Digest Suite Law

### 9.1. Digest Suite Contract and Initial Ratified Suite

Kontrakt core does not make BLAKE3 a semantic domain contract.

Kontrakt core defines a deterministic metadata identity digest suite contract.

A digest suite is an adapter-implemented, protocol-ratified mechanism that maps:

``````text
canonical identity bytes
+ canonical domain separation payload
+ suite id / version
+ requested output width
-> deterministic digest / HID descriptor bytes
``````

The initial ratified suite is:

``````text
algorithmFamily = BLAKE3
algorithmId = KONTRAKT_BLAKE3_METADATA_IDENTITY
algorithmVersion = 1
hashSuite16 = 1
hidDerivationVersion16 = 1
``````

BLAKE3 is the v1 ratified suite implementation.

BLAKE3 is not the semantic identity contract itself.

The contract is the digest-suite law:

``````text
same canonical identity bytes
+ same canonical domain separation payload
+ same suite id / version
+ same requested output width
-> same deterministic digest / HID descriptor bytes
``````

Digest algorithms are replaceable only through versioned suite ratification.

Changing the digest suite changes protocol material.

A later ADR may ratify another suite if it defines:

- suite id;
- algorithm id;
- algorithm version;
- derivation version;
- canonical domain separation payload compatibility;
- output width and split rules;
- migration behavior;
- coexistence behavior;
- downgrade rejection;
- cache invalidation;
- stable intern id treatment;
- and golden vectors.

Forbidden shape:

``````text
same hashSuite16
-> different digest algorithm
-> different HID output
``````

Lawful shape:

``````text
hashSuite16 = 1
-> BLAKE3 v1

hashSuite16 = 2
-> future ratified digest suite
``````

Suite mismatch is not ordinary inequality.

Suite mismatch MUST be classified as one of:

- cache miss with suite reason;
- compatibility rejection;
- protocol migration boundary;
- or protocol violation depending on the boundary.

This suite contract governs:

- canonical metadata digest;
- HID derivation;
- keyed derivation;
- deterministic entropy derivation;
- deterministic UUID payload derivation where metadata participates;
- future frozen image content summary roots;
- replay manifest summary roots.

### 9.2. Adapter Boundary

Adapters do not become digest-suite authorities.

Backends may optimize locally, but planning-visible or frozen-visible digest material must be semantically equivalent to
the active ADR-ratified digest suite.

The adapter implements the selected suite.

The protocol selects and versions the suite.

Forbidden:

- backend-specific hashing as semantic identity;
- JVM `hashCode()` as digest input;
- backend manifest digest as Kontrakt digest unless re-verified through the active digest suite contract;
- classloader-stable hash as Kontrakt identity;
- swapping the digest algorithm without changing suite id/version;
- treating a local hardware-accelerated digest path as protocol authority without golden-vector equivalence.

### 9.3. Canonical Domain Separation Payload Law

Every digest invocation used for identity MUST include domain separation.

Domain separation is byte-level protocol material.

It MUST NOT be assembled from:

- display strings;
- enum ordinals;
- declaration order;
- service-loader order;
- map iteration order;
- classpath order;
- source traversal order;
- or delimiter-joined text.

The canonical domain separation payload for HID derivation is a fixed-width 56-byte canonical protocol payload:

``````text
DigestDomainSeparationPayloadV1

offset  size  field
0       4     protocolMarker32
4       4     identityDomainId32
8       4     identityDomainVersion32
12      4     canonicalEncodingVersion32
16      2     hashSuite16
18      2     hidDerivationVersion16
20      4     domainSchemaVersion32
24      8     versionBundleFingerprintHigh64
32      8     versionBundleFingerprintLow64
40      4     canonicalEncodingPolicyVersion32
44      4     canonicalOrderingAlgorithmVersion32
48      4     typeIdentityAlgorithmVersion32
52      4     reserved32
``````

Physical encoding:

``````text
protocolMarker32                       : u32 little-endian
identityDomainId32                     : u32 little-endian
identityDomainVersion32                : u32 little-endian
canonicalEncodingVersion32             : u32 little-endian
hashSuite16                            : u16 little-endian
hidDerivationVersion16                 : u16 little-endian
domainSchemaVersion32                  : u32 little-endian
versionBundleFingerprintHigh64         : u64 little-endian
versionBundleFingerprintLow64          : u64 little-endian
canonicalEncodingPolicyVersion32       : u32 little-endian
canonicalOrderingAlgorithmVersion32    : u32 little-endian, zero when not applicable
typeIdentityAlgorithmVersion32         : u32 little-endian, zero when not applicable
reserved32                             : u32 little-endian, MUST be zero
``````

The byte length of `DigestDomainSeparationPayloadV1` is exactly:

``````text
56 bytes
``````

All 56 bytes are digest-suite input.

No field in `DigestDomainSeparationPayloadV1` may be omitted.

The phrase `when not applicable` means:

``````text
write the fixed-width field with value zero
``````

It does not mean:

``````text
omit the field
``````

Length-shifting the payload by omitting non-applicable axes is forbidden.

For `hashSuite16 = 1`, this payload is the input domain-separation payload for the BLAKE3 v1 suite.

For a future digest suite, the same payload remains the default unless the suite migration ADR ratifies a different
payload version.

Rules:

- `protocolMarker32` MUST be the ADR-ratified Kontrakt protocol marker;
- `identityDomainId32` MUST be present even if the same value appears in `CanonicalEnvelopeHeader`;
- `versionBundleFingerprintHigh64` and `versionBundleFingerprintLow64` MUST be derived by Section 8.9.3;
- every integer field MUST use fixed-width little-endian encoding;
- `canonicalOrderingAlgorithmVersion32` MUST be physically present and MUST be zero when not applicable;
- `typeIdentityAlgorithmVersion32` MUST be physically present and MUST be zero when not applicable;
- `reserved32` MUST be physically present and MUST be zero;
- every byte of the 56-byte payload MUST be initialized before hashing;
- a builder MAY zero-fill the whole 56-byte payload before writing fields, or explicitly write every field including
  zero-valued fields;
- hashing a reused scratch range that contains uninitialized or stale bytes is forbidden;
- hashing more or fewer than 56 bytes for `DigestDomainSeparationPayloadV1` is forbidden;
- delimiter-free concatenation is forbidden;
- text domain labels are forbidden as digest input unless a future ADR ratifies a canonical text-domain encoding.

The 56-byte length is the canonical digest input length for this payload version.

It is not derived from JVM object headers, JVM array base offsets, compressed-oops layout, GC layout, cache-line
subtraction, or any other runtime object-layout assumption.

The 64-bit fingerprint words are placed at 8-byte-relative offsets within the canonical payload.

This is a protocol-relative offset property.

It does not, by itself, prove physical memory alignment on the JVM heap.

A physical implementation MAY store this payload inside a wider cache-line-oriented slot, such as a 64-byte or 128-byte
stride slot, when a substrate backend can prove the corresponding alignment and lifecycle properties.

Unless a future digest-suite version ratifies a different payload length, only the canonical 56 bytes are digest-suite
input.

Physical padding in a wider slot is physical storage material.

It is not canonical identity material.

A portable JVM heap implementation MUST NOT claim exact cache-line placement or physical alignment merely because this
payload's canonical length is 56 bytes or because an implementation stores it in a wider physical slot.

Exact physical cache-line alignment claims remain governed by ADR-0042 and require implementation evidence.

Different domains MUST produce different outputs even for identical payload bytes.

The terms `relevant schema version` and `relevant semantic version tuple` are not open-ended phrases.

For ADR-0041, their material is the ratified `CanonicalIdentityVersionBundle` and the
`VersionBundleFingerprint128` defined in Section 8.9.3.

A domain-specific extension may add a version axis only through the axis registry and golden vectors.

### 9.4. Derivation Modes, Plain Digest, and Fixed-Width Output Law

Kontrakt distinguishes:

- plain digest over canonical bytes;
- keyed or context-derived derivation for HID/entropy surfaces;
- parent-child hierarchical derivation;
- fixed-width extensible output where the active suite ratifies it.

Plain digest is suitable for:

- content summary;
- collision candidate grouping;
- manifest evidence.

Suite-ratified derivation is required for:

- HID;
- parent-child entropy derivation;
- deterministic UUID materialization;
- local selector derivation;
- domain-separated primitive routing identity where a short width is emitted.

For `hashSuite16 = 1`, the initial ratified suite uses BLAKE3 digest / derive / XOF semantics as specified by this ADR.

For future suites, equivalent derivation surfaces MUST be defined by suite id/version and golden vectors.

Extensible output is allowed only as fixed-width output material.

A lawful extensible-output use MUST define:

- purpose;
- derivation context string or context id;
- input payload law;
- output offset, normally zero;
- output length in bits and bytes;
- width-specific split rule;
- truncation rule;
- suite id/version;
- and golden vectors.

Open-ended extensible output is forbidden.

Forbidden shape:

``````text
derive digest output stream
-> take arbitrary later bytes depending on runtime need
``````

Lawful shape:

``````text
domain-separated input
-> suite-ratified digest/derive operation
-> first N ratified bytes
-> fixed-width primitive words
``````

### 9.5. Width Law and Candidate-Descriptor Authority Boundary

Digest output width must be explicit.

Ratified HID descriptor widths:

``````text
HID64   = 64-bit compact routing / table acceleration only
HID128  = 128-bit ordinary compact candidate descriptor
HID256  = 256-bit strong compact candidate descriptor / digest-equivalent descriptor
``````

Rules:

- `HID64` is never semantic equality authority;
- `HID128` is never semantic equality authority by itself;
- `HID256` is still not semantic equality authority by itself unless a later ADR explicitly ratifies digest-only
  equality
  for a non-semantic surface;
- route64 may be derived from HID material but remains routing-only;
- width truncation must be deterministic and version-bound;
- width split into primitive words MUST be defined by the active suite law.

`HID128` being the default intern-table compact descriptor does not make it equality authority.

It means:

``````text
HID128 match
-> candidate lookup hit
-> verification ladder required
``````

It does not mean:

``````text
HID128 match
-> semantic equality accepted
``````

An implementation MUST NOT publish, intern, deduplicate, merge, resume, or report semantic equality solely from HID
equality.

### 9.6. HID Width Selection and Collision Probability Boundary Law

Protocol-owned intern membership MUST use `HID128` or wider by default.

`HID64` is routing-only unless a domain-specific cardinality proof allows a narrower compact descriptor for a
non-semantic surface.

Approximate collision probability MAY be reasoned from the birthday-bound approximation:

``````text
p ≈ 1 - exp(-n(n - 1) / 2^(b + 1))

For small p:

p ≈ n(n - 1) / 2^(b + 1)
  ≈ n^2 / 2^(b + 1)

where:
    n = expected candidate count inside the identity scope
    b = compact descriptor width in bits
    p = approximate birthday-bound collision probability
``````

This probability model assumes uniform cryptographic output.

It is a sizing aid, not an equality proof.

It MUST NOT be used to remove exact verification.

It MUST NOT be used to justify `HID64` semantic equality.

It MUST NOT be used to ignore adversarial input, schema-correlated clusters, exact clone groups, or collision budget
exhaustion.

Required sizing condition:

``````text
Choose b such that:

    n(n - 1) / 2^(b + 1) <= p_target
``````

Equivalent explicit bound:

``````text
b + 1 >= ceil(log2(n(n - 1) / p_target))
b >= ceil(log2(n(n - 1) / p_target)) - 1
``````

A released implementation MAY choose a wider ratified width than the minimum bound.

The conservative default remains `HID128` for ordinary intern-table compact candidate descriptors, even when the
calculated minimum would be smaller.

Default interpretation:

``````text
HID64:
    route / shard / bucket index only

HID128:
    default intern-table compact candidate descriptor

HID256:
    strong artifact digest, persistent summary, or cold verification surface
``````

A domain that uses `HID64` for anything beyond routing must document:

- expected maximum cardinality;
- target collision probability;
- collision handling path;
- why `HID128` is unnecessary for that surface;
- and why the surface is non-semantic.

Every HID width, including `HID128` and `HID256`, MUST define a verification path.

### 9.7. Seal / Materialization Boundary and Hot Projection Law

The active metadata identity digest suite remains the protocol-owned metadata identity derivation root.

ADR-0041 does not replace the active suite with non-cryptographic hashes for canonical identity, HID derivation,
persistent summaries, replay manifests, or future query fingerprints.

However, hot lookup paths MUST NOT repeatedly re-encode full canonical material and re-run full digest derivation for
every shard, lane, bucket, or probe decision.

A seal boundary is the deterministic point at which canonical material has been fully validated and canonical bytes are
closed for the relevant identity unit.

A materialization boundary is the deterministic point at which a sealed identity unit is converted into published or
probe-visible primitive metadata.

For ADR-0041, a seal or materialization boundary MUST occur only after:

- canonical material is complete;
- observation-independent canonical encoding has selected the byte representation;
- version-bundle material is resolved;
- domain separation payload is fixed;
- canonical bytes are immutable for that identity unit;
- resolved policy budgets have admitted the unit;
- and no future field, child, SCC member, or unordered collection element can change the canonical bytes without
  creating a
  new identity unit or a new publication epoch.

The lawful shape is:

``````text
canonical material
-> canonical bytes
-> active-suite digest / HID derivation at seal or materialization boundary
-> deterministic primitive projections
-> route64 / shard bits / inline verifier prefix / probe metadata
-> hot lookup reads the projections
``````

The unlawful shape is:

``````text
every hot route/probe operation
-> re-create canonical bytes
-> re-run full digest over the full payload
-> truncate again for route/probe
``````

Projection invalidation law:

- projections are immutable products of a sealed canonical byte unit;
- a canonical material change MUST create a new sealed unit, new projection tuple, or new publication epoch;
- stale projections MUST NOT remain reachable from newly published identity material;
- mutable in-place canonical material changes are forbidden after seal;
- route/probe tables MUST carry enough version, epoch, or handle material to reject stale projections;
- rebuilding a table from sealed identity material MUST reproduce the same projection tuple.

Non-cryptographic hashes such as Murmur3 or xxHash MAY be used only as explicitly non-semantic physical hints over
already-ratified or already-verified material.

`already-verified material` means material that has passed the owning domain's equality verification path for the
current
operation and cannot change semantic equality if the hint collides.

Non-cryptographic hashes MUST NOT become:

- HID replacement;
- persistent identity;
- collision verification authority;
- query fingerprint authority;
- stable intern id assignment authority;
- PlanCacheKey equality authority;
- canonical material equality authority;
- stale-projection freshness authority;
- or publication authority.

A future domain may migrate a physical route projection from a non-cryptographic route hash to an active-suite-derived
route projection only through a versioned route-derivation amendment, golden vectors, and a migration boundary.

### 9.8. Required Digest Suite Golden Vector Law

Digest/HID law is not releaseable without golden vectors.

A released implementation MUST provide a golden vector suite under the protocol golden-vector test surface.

Recommended path:

``````text
src/test/resources/kontrakt/golden/adr-0041/digest-suite/
``````

The suite MUST include known inputs and expected outputs for the initial BLAKE3 v1 suite and for every future ratified
suite:

- empty canonical byte payload;
- one minimal TypeReference canonical payload;
- one RawFactRecord canonical payload;
- one ActiveMemberKey canonical payload;
- one RuntimeBinding identity payload;
- one nested-message payload;
- one unordered collection payload with shuffled input order;
- one version-bundle fingerprint input;
- one domain-separation payload;
- `HID64` derivation;
- `HID128` derivation;
- `HID256` derivation;
- route64 derivation from HID material;
- inline verifier prefix derivation;
- unknown domain id fail-closed;
- domain id changed with identical canonical bytes;
- schema version changed with identical canonical bytes;
- canonical encoding version changed with identical canonical material;
- hash suite version changed;
- HID derivation version changed;
- fixed-width extensible output fixture for every ratified width.

Each vector entry MUST record:

- vector id;
- ADR version;
- identity domain id;
- domain schema version;
- canonical encoding version;
- hash suite id/version;
- algorithm id/version;
- HID derivation version;
- version-bundle fingerprint high/low;
- exact canonical input bytes as hex;
- domain separation payload bytes as hex;
- derivation context id/string;
- requested output width;
- expected output bytes as hex;
- expected split into primitive words where applicable.

Golden vectors are protocol tests.

A reference implementation may generate them, but released implementations MUST treat the checked-in vector bytes as the
compatibility authority.

### 9.9. Digest Suite Migration and Algorithm Replacement Law

Digest suite migration is a compatibility boundary.

A future algorithm, suite id, context string, derivation version, or width split change MUST define:

- old suite id/version;
- new suite id/version;
- old algorithm id/version;
- new algorithm id/version;
- migration trigger;
- coexistence period if any;
- cache invalidation rule;
- stable intern id treatment;
- published image compatibility rule;
- golden vectors for old and new suite;
- downgrade rejection rule;
- and replay / manifest compatibility behavior.

HID material from two different digest suites MUST NOT be compared as ordinary equality candidates without suite
classification.

Suite mismatch is:

- cache miss with suite reason;
- compatibility rejection;
- or protocol violation depending on boundary.

It is not silent inequality.

BLAKE3 v1 may be replaced only through this law.

The ability to replace the algorithm is part of the contract.

The replacement itself is not an adapter-local decision.

## 10. HID Law

### 10.1. Definition

HID is a domain-separated compact descriptor derived from canonical material.

HID has two distinct representations:

| Representation           | Purpose                                                                | Allowed surface          |
|--------------------------|------------------------------------------------------------------------|--------------------------|
| cold descriptor facade   | diagnostics, public API, golden-vector fixtures, tests, boundary views | ordinary objects allowed |
| hot primitive projection | intern probes, route probes, equality pre-screening, table rows        | primitive substrate only |

No Kotlin interface, class hierarchy, wrapper object, enum object, or descriptor allocation shape is normative for hot
HID storage.

The normative hot representation is fixed-width primitive words plus table-level proof material.

For ordinary `HID128` hot membership, the primitive projection is:

``````text
hidHigh64
hidLow64
``````

For ordinary `HID64` routing, the primitive projection is:

``````text
routeOrHid64
``````

For ordinary `HID256` cold or strong-summary membership, the primitive projection is:

``````text
word0_64
word1_64
word2_64
word3_64
``````

Cold descriptor facades MAY be materialized after hot probing or for diagnostic/export boundaries.

They MUST NOT be the physical representation of committed intern-table rows, route-table rows, probe groups, or
planning-visible hot identity state.

The invariant is normative.

The illustrative `HashedIdentityDescriptor` interface shape is a cold API, diagnostic, test, or boundary representation.

It is not the required physical representation for intern-table, route-table, or probe hot paths.

Hot-path HID material MUST be represented by fixed-width primitive words, not by `ByteArray` object references and not
by
interface-dispatched descriptor objects.

A hot intern-table or route-table implementation MUST NOT require ordinary probes to call:

``````text
HashedIdentityDescriptor.equals(...)
HashedIdentityDescriptor.hashCode()
virtual/interface property dispatch
sealed-interface polymorphic dispatch
per-candidate descriptor object allocation
``````

The ordinary hot-path shape for HID128 is primitive projection material such as:

``````text
hidHigh64[]
hidLow64[]
identityDomainId32[]
versionBundleFingerprintHigh64[]
versionBundleFingerprintLow64[]
canonicalLength32[]
inlineVerifierPrefix64/128[] where ratified
offset/length material for rare exact verification
``````

or an equivalent primitive substrate representation.

For HID64 and HID256, the same law applies with the ratified primitive word width for that surface.

Cold wrapper objects MAY be materialized after the hot probe path for diagnostics, public reporting, tests, or adapter
boundary use.

They MUST NOT be the ordinary authority representation used by committed intern/probe structures.

`ByteArray` digest material is allowed only for cold artifact, persistence, or diagnostic surfaces where pointer chasing
is not part of the ordinary probe path.

### 10.2. HID Is Not Equality Authority

A HID match means:

``````text
candidate match
``````

It does not mean:

``````text
semantic equality
``````

Required equality acceptance path:

``````text
HID match
-> domain/version compatibility check
-> canonical bytes exact comparison
   or canonical structural verification
-> equality accepted
``````

### 10.3. HID Mismatch

A HID mismatch under the same domain and version tuple may reject equality fast.

However, version mismatch must not be treated as ordinary inequality without classification.

Version mismatch is:

- incompatible identity material;
- cache miss with version reason;
- or protocol violation depending on boundary.

### 10.3.1. Version Mismatch Zero-Allocation Classification Law

A version mismatch is a deterministic classification boundary.

It is not an invitation to allocate exceptions, diagnostic strings, cache records, log events, or cold report material
on
the ordinary hot path.

The ordinary HID comparison path MUST classify version mismatch using fixed-width primitive fields and return a bounded
classification value such as:

``````text
HID_MISMATCH
HID_MATCH_REQUIRES_VERIFICATION
VERSION_MISMATCH_CACHE_MISS
VERSION_MISMATCH_REHASH_REQUIRED
VERSION_MISMATCH_REENCODE_REQUIRED
VERSION_MISMATCH_REINTERN_REQUIRED
VERSION_MISMATCH_INCOMPATIBLE_FAIL_CLOSED
VERSION_MISMATCH_PROTOCOL_VIOLATION
``````

The exact names may differ.

The invariant is normative:

``````text
hot version mismatch
-> primitive classification
-> no heap allocation required
-> no exception object required
-> no string concatenation required
-> no synchronous log I/O required
-> no cache structure allocation required
``````

Structured diagnostics MAY be emitted later through a cold, budgeted diagnostic path.

That diagnostic path MUST be sanitized, metered by `maxDiagnosticEvidenceBytes`, and separated from ordinary
intern-table
probe execution.

Repeated version mismatch from a stale or malicious adapter MUST be contained by bounded counters, admission policy,
compatibility classification, or adapter quarantine.

It MUST NOT produce unbounded log spam, exception churn, cache-entry churn, retry storms, or report material allocation.

A cache miss with version reason is allowed only when the miss path is allocation-bounded and governed by resolved
policy.

A protocol violation may fail the current identity scope closed, but the hot classification step itself remains
zero-allocation and non-blocking.

### 10.4. HID Domain Examples

Initial identity domains:

``````text
TYPE_REFERENCE_IDENTITY
TYPE_CYCLE_KEY_IDENTITY
TYPE_CYCLE_IDENTITY_PRECHECK
TYPE_SHAPE_SUMMARY_IDENTITY
RAW_FACT_RECORD_IDENTITY
LOWERED_CONTRACT_FACT_IDENTITY (reserved)
ACTIVE_MEMBER_KEY_IDENTITY
LOCAL_SELECTOR_IDENTITY
RUNTIME_BINDING_IDENTITY
FROZEN_TYPE_INDEX_MEMBERSHIP
FROZEN_IMAGE_CONTENT_SUMMARY
PLAN_CACHE_ROUTE
PLAN_CACHE_EQUALITY
CANONICAL_PLAN_NODE_SIGNATURE_SUMMARY
REPLAY_MANIFEST_SUMMARY
``````

Each domain must define:

- canonical material;
- canonical byte encoding;
- digest/HID width;
- collision verification payload;
- version tuple;
- golden vectors.

---

## 11. Hierarchical Identity Derivation

### 11.1. Purpose

HID is also the basis for hierarchical derivation.

Kontrakt must avoid:

- mutable global entropy counters;
- node-path-only derivation;
- source-order-only derivation;
- per-run RNG streams;
- wall-clock entropy;
- object allocation order.

Hierarchical derivation derives child identity / entropy from:

- parent deterministic entropy or parent semantic identity;
- local selector tuple;
- explicit version tuple;
- identity domain;
- deterministic keyed derivation.

### 11.2. Derivation Shape

Normative shape:

``````text
child_material =
    canonical_encode(
        domain,
        parent_identity_or_entropy,
        local_selector_tuple,
        version_tuple
    )

child_hid =
    ACTIVE_DIGEST_SUITE_DERIVE(
        suite = hashSuite16 / hidDerivationVersion16,
        key   = domain_separated_parent_key,
        input = child_material,
        width = ratified_width
    )
``````

For `hashSuite16 = 1`, the active suite uses the BLAKE3 v1 derivation semantics ratified by Section 9.

A future digest suite may replace the derivation algorithm only through versioned suite ratification, golden vectors,
and migration law.

### 11.3. Local Selector Tuple

`LocalSelectorTuple` is local selector material.

It is created when projected semantic material becomes a concrete local expansion obligation.

Examples:

``````text
Projected active member
-> Local expansion edge
-> LocalSelectorTuple
-> HID-governed materialization
``````

``````text
Synthetic map entry
-> Local key/value edge
-> LocalSelectorTuple
-> HID-governed key/value materialization
``````

Rules:

- `LocalSelectorTuple` must not expose delimiter-joined rendering as HID input;
- `LocalSelectorTuple` must be encoded by tagged, length-prefixed canonical encoding;
- `LocalSelectorTuple` must not retain backend handles;
- `LocalSelectorTuple` must not retain raw type text;
- `LocalSelectorTuple` must not be stored inside Canonical IR unless a later canonical signature law explicitly includes
  it as canonical material;
- local selector material is path-sensitive and therefore belongs before Canonical IR publication.

### 11.4. Parent-Preserving Refactor Stability

A HID derivation should be stable under local refactors that preserve semantic member identity.

Example:

- adding a sibling member with a higher canonical rank must not perturb the HID of an existing member if that member's
  own semantic identity and parent identity are unchanged;
- reordering source declarations must not perturb HID if declaration order is not semantic material;
- renaming a semantic member must perturb HID if the member name is part of semantic identity.

### 11.5. Parent Identity Change

If parent semantic identity changes, child HID may change.

This is expected.

The hierarchy is semantic, not path-only.

### 11.6. Hierarchical Re-Keying and Incremental Derivation Boundary Law

Parent-dependent HID derivation intentionally creates an invalidation boundary.

If parent semantic identity participates in a child HID, then a parent identity change MAY change the child HID.

A compliant implementation MUST NOT keep using an old child HID after a parent identity change merely because the
child's
own canonical bytes are unchanged.

However, Kontrakt may avoid unnecessary re-encoding of parent-independent child material.

Lawful incremental shape:

``````text
child canonical material unchanged
-> child canonical bytes / local selector bytes already sealed and verified
-> parent identity changes
-> derive new parent domain-separated key
-> re-run active-suite child HID derivation from sealed child material and new parent key
-> verify version/domain/scope compatibility
-> publish new child projection only after the identity seal accepts it
``````

Forbidden incremental shape:

``````text
parent identity changes
-> reuse old parent-dependent child HID
-> treat old HID as still valid
-> publish without version/scope/parent verification
``````

Also forbidden:

``````text
parent identity changes
-> mutate child HID in place inside a published table
-> readers observe mixed parent/child epochs
``````

A derived child HID is a projection over:

- the active digest suite;
- the parent identity or parent deterministic entropy;
- the local selector tuple;
- the child canonical material selected by the owning domain;
- and the active version bundle.

Therefore, an implementation MAY cache parent-independent child canonical bytes, child local-selector bytes, and sealed
child-local digest projections when those cached values are version-bound and exact-verification safe.

Such cached material is not semantic equality authority by itself.

It is reusable derivation input only.

The implementation MUST still derive the parent-dependent HID under the current parent key and active version bundle
before publication.

This optimization is a re-derivation optimization, not a semantic shortcut.

It may reduce work from:

``````text
re-lower child material
-> re-encode child canonical bytes
-> rehash child local material
-> derive parent-dependent HID
``````

to:

``````text
reuse sealed child canonical bytes / child-local projection
-> derive parent-dependent HID with new parent key
``````

but it MUST NOT remove:

- parent-version compatibility classification;
- parent/child domain separation;
- active suite id/version classification;
- exact verification where a compact identity match is used;
- publication epoch integrity;
- dependency invalidation tracking;
- or golden-vector equivalence.

A future L2 or incremental-query layer MAY memoize parent-independent child material, but it MUST key that memoization
by
all version axes that can change the child canonical bytes or child-local derivation input.

A parent identity change may still require O(number of affected parent-dependent children) projection updates.

ADR-0041 does not promise O(1) invalidation for an entire subtree.

It permits O(1)-per-child re-derivation when sealed parent-independent child material is already available and verified.

Any broader incremental cutoff belongs to the future query/incremental work and must preserve this ADR's identity law.

### 11.7. ADR-0043 Structural/Contextual Graph Identity Bridge

ADR-0041 permits hierarchical derivation to consume sealed parent-independent child identity references.

ADR-0041 does not define the full canonical contract graph.

ADR-0043 owns:

- contract graph unit boundaries;
- structural identity versus contextual identity semantics;
- sealed structural identity reference shape;
- graph interning rules;
- Merkle-like child-reference encoding;
- SCC graph identity;
- incremental dependency invalidation;
- and future integration with lowered contract facts for state, protocol, data, governance, DTO, boundary, and explicit
  state-machine contract material.

ADR-0041 owns only the digest/HID/interner laws used by those identities after graph material has been ratified.

A parent or context derivation may consume a child reference only when that child reference represents sealed structural
identity material.

A sealed structural identity reference means that the child-local material has completed:

``````text
child-local canonical material
-> canonical byte encoding
-> active digest-suite structural descriptor derivation
-> collision verification
-> seal
-> stable reference publication inside an explicit scope
``````

The lawful ADR-0041 bridge shape is:

``````text
sealed child structural identity reference
+ parent/context key
+ local selector / edge role / position semantics where ratified
+ version bundle
-> active-suite contextual descriptor derivation
-> publication only after compatibility and identity-seal checks
``````

The forbidden bridge shape is:

``````text
bare child HID
-> assume child equality
-> parent re-keying
-> publish contextual identity without child seal or compatibility validation
``````

ADR-0041 therefore allows a future ADR-0043 implementation to avoid re-reading unchanged child canonical bytes when the
child-local material is already sealed.

This optimization is bounded as:

``````text
O(1) with respect to the sealed child canonical byte length per child reference
``````

It is not:

``````text
O(1) for an entire subtree regardless of affected reference count
``````

If a parent owns `N` child references, parent/context derivation remains at least proportional to the number of
parent-visible child references unless ADR-0043 ratifies a stronger dependency summary law.

Any stronger incremental cutoff MUST preserve:

- canonical byte equivalence;
- digest-suite equivalence;
- collision verification;
- version compatibility;
- publication epoch integrity;
- dependency invalidation correctness;
- and golden-vector reproducibility.

Until ADR-0043 and the top-level contract definition document are ratified, ADR-0041 treats lowered contract graph
material as reserved integration material.


---

## 12. Collision Verification Law

### 12.1. Required Collision Verification Payload

Every compact identity surface must define its verification payload.

Acceptable verification payloads:

- canonical bytes;
- canonical signature bytes;
- exact canonical structural payload;
- exact frozen record material;
- exact full semantic key tuple;
- canonical string table slice plus schema/version proof.

Unacceptable verification payloads:

- `toString()`;
- display label;
- source path alone;
- backend symbol pointer;
- backend ordinal;
- object identity;
- `hashCode()`;
- digest-only match.

### 12.2. Verification Procedure

Normative procedure:

``````text
1. Compare identity domain.
2. Compare algorithm id/version.
3. Compare canonical encoding version.
4. Compare domain schema version.
5. Compare compact HID/digest bits.
6. If compact identity differs:
       reject equality.
7. If compact identity matches:
       perform exact verification against the verification payload.
8. If exact verification succeeds:
       accept equality.
9. If exact verification fails:
       record collision event and treat as distinct material.
``````

### 12.3. Collision Is Not Panic by Default

A hash collision is not automatically a panic.

It is a valid protocol case if exact verification distinguishes candidates.

However, collision may become panic or fail-closed if:

- the storage structure cannot represent collision chains;
- the compact identity was used without verification;
- an invariant claimed collision impossibility;
- a persisted artifact lacks verification material;
- a supposedly exact identity surface collides.

### 12.3.1. Fail-Closed Availability Boundary

Fail-closed means semantic non-publication.

It does not mean that the ordinary policy path must terminate the entire process.

Allowed fail-closed outcomes include:

- reject the current identity candidate;
- reject the current identity scope;
- fail the current frozen image or artifact publication;
- quarantine the current acquisition scope;
- quarantine the current worker lane for the current scope;
- move the collision group into a bounded cold collision structure when such a structure is ratified by policy.

Forbidden outcomes:

- accept HID-only equality;
- silently drop colliding candidates;
- retroactively repair already-published stable ids;
- process-wide hard crash as the ordinary response to a bounded, representable collision case.

A process-wide fatal error is reserved for implementation corruption, memory corruption, invariant contradiction, or an
unrepresentable state that cannot be safely diagnosed within the current process.

### 12.4. Collision Telemetry

Collision events must be observable.

Telemetry should include:

- identity domain;
- algorithm id/version;
- encoding version;
- HID width;
- collision group size;
- verification path;
- boundary where collision was detected.

Telemetry must not include unbounded raw payloads.

Diagnostic material must be sanitized and budgeted.

### 12.5. Verification Cost Bound

Exact canonical byte comparison is the final equality authority, but it MUST be bounded by resolved metadata identity
policy.

A compact identity lookup MUST NOT proceed directly from HID match to unbounded canonical-byte comparison.

Required ordinary verification order:

``````text
HID / route pre-screen
-> domain and version-bundle check
-> canonical byte length check
-> inline verifier prefix check
-> full canonical byte comparison only if all inline checks pass
``````

The full canonical byte comparison is lawful only when:

- the candidate's canonical byte length is within the domain cap;
- the collision group size is within the resolved policy cap;
- the candidate survived inline verifier-prefix rejection;
- and the verification path is metered or benchmark-covered for that identity surface.

If canonical bytes exceed the resolved cap, the material must fail closed or be represented by a separately ratified
bounded summary surface.

It must not silently enter ordinary interning.

---

## 13. Protocol-Owned Interning Law

### 13.1. Purpose

Protocol-owned interning provides stable ids and primitive membership without sacrificing determinism.

It is not a generic process-global cache.

It is not a replacement for semantic equality.

It is not an L2 retention policy.

It is a deterministic identity compaction mechanism.

### 13.2. Intern Scope

Every protocol-owned interner must declare its scope.

Allowed scopes:

``````text
FROZEN_IMAGE_LOCAL
FROZEN_ACQUISITION_RUN_LOCAL
PLANNING_RUN_LOCAL
RUNTIME_POLICY_EPOCH_LOCAL
MODULE_SNAPSHOT_LOCAL
PERSISTED_ARTIFACT_LOCAL
GLOBAL_PROTOCOL_TABLE
``````

`GLOBAL_PROTOCOL_TABLE` is allowed only for immutable, pre-ratified protocol constants.

It is not allowed for dynamically discovered user metadata.

Examples of allowed global protocol tables:

- identity domain registry;
- canonical wire type registry;
- protocol field tag registry;
- BLAKE3 suite registry;
- golden vector registry.

Examples of forbidden global protocol tables:

- global mutable TypeReference interner;
- global mutable front-end annotation descriptor interner;
- process-wide first-seen metadata id allocator;
- global table that mutates as user code is discovered.

Rules:

- scope is part of intern-id meaning;
- ids from different scopes are not comparable unless a conversion law exists;
- scope must be encoded in diagnostics;
- scope must be encoded in persisted artifacts where ids survive process boundaries.

### 13.3. Intern Input

Intern input is:

``````text
canonical bytes
+ identity domain
+ version tuple
+ compact HID
+ collision verification payload
``````

Forbidden intern input:

- backend handles;
- backend ordinals;
- frozen ordinals;
- acquisition order;
- callback order;
- thread id;
- object identity;
- JVM identity hash;
- mutable collection iteration order;
- non-ratified strings.

### 13.4. Intern Id Assignment

Stable intern id assignment MUST be acquisition-order independent.

The default assignment law is:

``````text
1. collect candidate canonical material;
2. encode canonical bytes;
3. derive domain-separated HID;
4. group by HID;
5. collision-verify within each group;
6. sort unique verified materials by canonical byte order under this ADR;
7. assign dense ids in sorted order;
8. publish immutable intern table.
``````

This law is intentionally batch-oriented for correctness.

A streaming implementation may accumulate candidates, encode canonical bytes, compute HIDs, and assign provisional
non-semantic handles.

It MUST NOT publish final stable dense intern ids before the interning scope is sealed.

Final stable intern ids are published only after deterministic ordering, collision verification, and table integrity
validation.

Streaming is therefore an implementation technique for candidate accumulation and memory management, not an authority to
issue final stable ids before the seal boundary.

### 13.5. Dense Id Shape

A stable intern id has two representation layers.

| Layer                   | Purpose                                                                | Representation rule                   |
|-------------------------|------------------------------------------------------------------------|---------------------------------------|
| semantic/cold facade    | API, diagnostics, tests, report views, golden-vector fixtures          | ordinary wrapper object MAY exist     |
| committed/hot substrate | intern table rows, probe groups, frozen indexes, planning acceleration | primitive id / table-level proof only |

The normative dense-id material is:

``````text
scopeId32
localStableInternId32
identityDomainId32
interningProtocolVersion64
``````

A table MAY move `scopeId32`, `identityDomainId32`, and `interningProtocolVersion64` into a validated table header when
the whole table is proven to share those values.

In that case, the ordinary hot row MAY store only:

``````text
localStableInternId32
``````

or `localStableInternId32` plus the additional primitive projection fields required by the active probe layout.

No class named `TypeReferenceInternId`, `StableInternId`, or similar facade is normative for committed interner storage.

If such a facade exists, it is a cold boundary view over primitive material.

Rules:

- `id >= 0`;
- `id` is dense inside its declared scope;
- `id` is not semantic equality authority alone;
- `id` may index arrays only after table integrity validation;
- `id` must not be persisted without scope/version metadata.

### 13.5.1. Intern Id Facade and Primitive Substrate Split Law

Intern id wrapper objects are not committed interning representation.

They are optional cold facades over primitive intern material.

A compliant implementation MUST NOT store committed intern membership as:

``````text
Array<TypeReferenceInternId>
Array<StableInternId>
Array<InternHandle>
List<InternHandle>
Map<WrapperObject, WrapperObject>
``````

for the ordinary hot interner path.

The lawful hot shape is:

``````text
table header:
    scopeId32
    identityDomainId32
    interningProtocolVersion64
    versionBundleFingerprint128 where required
    tableEpoch64 where required

primitive rows:
    localStableInternId32
    hidHigh64 / hidLow64 or ratified HID width words
    canonicalBytesOffset32
    canonicalBytesLength32
    inlineVerifierPrefix words where ratified
    state / generation / occupancy bits
``````

or an equivalent primitive substrate backend admitted by ADR-0042.

A cold facade may be constructed only after the row's table-level proof has been validated.

The facade MUST NOT become:

- equality authority;
- interner row storage;
- probe key storage;
- route-table storage;
- frozen image row storage;
- planning hot-path state;
- stable id assignment state;
- or L2 exact-match key material.

Architecture tests MUST reject hot-path arrays, maps, or published tables that store intern id wrapper objects as their
ordinary committed representation.

### 13.5.2. Cross-Scope Intern Id Translation Law

Stable intern ids are scoped dense references.

A local stable intern id from one scope is not comparable with a local stable intern id from another scope by numeric
value.

The following is forbidden:

``````text
sourceScope.localStableInternId32 == targetScope.localStableInternId32
-> semantic equality accepted
``````

Cross-scope reuse is lawful only through one of the following validated paths:

1. exact canonical material is verified again in the target scope;
2. a sealed structural identity reference accepted by ADR-0043 is imported and verified against the target scope's
   domain/version/suite policy;
3. a ratified cross-scope translation table maps source scope id to target scope id after canonical verification; or
4. the scopes share the same immutable published table instance and the same table-level proof.

A cross-scope translation table MUST include at least:

- source scope id;
- target scope id;
- identity domain id;
- interning protocol version;
- version bundle fingerprint;
- digest suite id/version;
- source local stable intern id;
- target local stable intern id;
- and verification epoch / publication epoch material.

The translation table MUST NOT be derived from:

- numeric id equality alone;
- table row position;
- frozen ordinal equality;
- backend handle equality;
- insertion order;
- cache hit state;
- or physical table layout.

If no translation law is available, consumers MUST treat ids from different scopes as incomparable compact references.

### 13.5.3. Intern Scope Retirement and Stale Reference Rejection Law

Stable intern ids are valid only while their owning scope and table-level proof are valid.

ADR-0041 does not define the full lifecycle reclamation mechanism for published intern tables.

ADR-0042 owns physical retirement, reader leases, epoch reclamation, and substrate teardown.

ADR-0043 owns graph/query invalidation for contract graph identities.

ADR-0041 nevertheless requires a minimal stale-reference rejection contract:

- every published intern table MUST expose scope id, identity domain id, interning protocol version, version bundle
  fingerprint, and publication epoch material;
- every consumer that accepts an intern id across a boundary MUST validate the table-level proof required by that
  boundary;
- a retired, superseded, or protocol-incompatible scope MUST NOT accept new lookups or publish new stable ids;
- stale intern ids MUST fail closed or be translated through a ratified cross-scope translation law;
- retirement MUST NOT make a stale numeric id silently refer to new canonical material.

The forbidden shape is:

``````text
old scope numeric id
-> new table row with same integer value
-> accepted without scope/version proof
``````

The lawful shape is:

``````text
localStableInternId32
+ scope id
+ identity domain id
+ interning protocol version
+ version bundle fingerprint
+ publication epoch proof
-> validate or reject / translate
``````

### 13.6. No Global Mutable Interner

A mutable process-global interner is forbidden for semantic identity.

Forbidden:

``````text
GlobalTypeReferenceInterner.nextId++
ConcurrentHashMap<TypeReference, Int> shared across runs
static mutable string interner used as semantic id authority
intern id assigned by first observation order
``````

Allowed:

- immutable global protocol constants;
- immutable golden vector registries;
- immutable algorithm suite registries;
- immutable epoch-published protocol tables;
- per-scope interner built from canonical sorted material;
- runtime caches that memoize verified intern results without becoming semantic authority.

If a global-like table is required for protocol material, it must be:

- built from ratified constants or sealed canonical material;
- safely published as immutable;
- versioned;
- scope-declared;
- and never mutated after publication.

### 13.7. Interner Publication

An intern table becomes visible only after:

1. all candidates are encoded;
2. all candidates are version-compatible;
3. all collisions are verified;
4. all ids are assigned deterministically;
5. table coverage is complete;
6. table integrity validation succeeds;
7. backend-native handles are unreachable;
8. publication uses a safe publication boundary.

Partial intern tables must not become planning-visible.

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

### 13.9.1. Intern-Table Substrate Backend Boundary Law

Intern-table physical layout is a substrate backend concern.

ADR-0041 defines the semantic and protocol requirements for intern-table membership, candidate lookup, collision
verification, stable id assignment, and publication.

ADR-0041 does not make any concrete storage backend semantic authority.

A compliant implementation MUST keep the following boundary:

``````text
core / domain / protocol:
    canonical bytes
    HID descriptor semantics
    verification ladder
    collision escalation law
    stable intern id assignment law
    publication law

substrate adapter / infrastructure backend:
    heap primitive arrays
    off-heap storage
    MemorySegment storage
    native aligned allocation
    generated physical layout
    memory-mapped layout
    SIMD / vectorized probe implementation
``````

The core MUST NOT depend on:

- `ByteArray` object layout;
- `IntArray` object layout;
- `LongArray` object layout;
- Java heap object headers;
- compressed-oops layout;
- GC compaction behavior;
- `MemorySegment` implementation details;
- native allocator identity;
- off-heap base address;
- SIMD width;
- cache-line size;
- or a particular digest library implementation.

The core MAY require:

- primitive-substrate-compatible access;
- explicit offset / length / width material;
- sealed immutable publication;
- deterministic lifecycle boundaries;
- no per-candidate hot-path wrapper allocation;
- no object-graph authority in committed intern tables;
- and cross-backend equivalence with golden vectors.

Changing the substrate backend MUST NOT change:

- canonical bytes;
- HID derivation;
- collision verification result;
- semantic equality;
- stable intern id assignment;
- publication order;
- or report/replay identity.

### 13.9.2. V1 Mechanical Profile and Aligned Backend Admission Law

The portable v1 baseline may use heap primitive arrays.

However, a release claiming the ADR-0041 high-performance mechanical profile for protocol-owned interning SHOULD provide
at least one explicitly aligned physical backend for hot probe groups.

Acceptable aligned backend candidates include:

- Java `MemorySegment` native allocation with explicit alignment proof;
- off-heap direct-memory allocation with explicit alignment proof;
- native aligned allocation through a contained infrastructure adapter;
- generated persistent image layout with documented alignment;
- or another substrate backend that proves base-address alignment, stride, and lifecycle safety.

This requirement is a performance-profile requirement.

It is not a semantic correctness requirement.

A heap-only implementation MAY be compliant with ADR-0041 semantics, but it MUST document itself as a portable baseline
backend and MUST NOT claim exact physical cache-line alignment.

A high-performance mechanical profile MUST publish:

- selected substrate backend;
- base-address alignment proof;
- entry stride;
- slot padding rule;
- hot/cold field split;
- false-sharing analysis;
- leak / teardown tests;
- fallback behavior when aligned allocation is unavailable;
- and cross-backend golden-vector equivalence against the reference heap backend.

A substrate backend failure MUST NOT silently change identity semantics.

If the aligned backend is unavailable, the implementation MUST choose one of:

- fall back to the documented portable heap baseline;
- disable the high-performance mechanical profile for that run;
- or fail profile admission closed before identity scope admission.

It MUST NOT partially mix backend semantics inside an admitted identity scope.

### 13.10. Inline Verification Prefix Law

Every compact intern-table entry SHOULD store an inline verification prefix derived from canonical bytes.

The prefix is not semantic equality authority.
It is a cache-local rejection accelerator.

A compliant implementation MAY use:

- the first 64 bits of canonical bytes where safe;
- a BLAKE3-derived verifier prefix;
- a domain-separated secondary fingerprint;
- a fixed-width prefix over the canonical byte envelope header and selected hot fields;
- or an equivalent fixed-width verifier prefix.

The prefix MUST be version-bound and domain-separated if derived by hashing.

The prefix MUST NOT replace full canonical byte verification.

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

### 13.12. Scope-Layered Interning Law

Interning scope selection is a memory-locality decision as well as a semantic boundary decision.

Too narrow a scope duplicates identity material.
Too broad a scope produces oversized intern tables, poor locality, and harder lifecycle control.

Therefore each interner scope MUST declare:

- semantic reuse boundary;
- expected cardinality envelope;
- table lifecycle;
- ownership boundary;
- publication boundary;
- memory reclamation boundary;
- and whether ids may cross into persistent artifacts.

A scope is non-compliant if it is chosen only for convenience and cannot explain its locality and lifecycle
consequences.

### 13.13. Interner Probe Budget Law

Intern-table probing is not free.

Probe behavior is an ADR-0041 admission-budget concern.

The physical probing algorithm is an ADR-0042 substrate-backend concern.

ADR-0041 owns:

- resolved probe-budget vocabulary;
- domain-partitioned admission relationships;
- feasibility relationships;
- bootstrap cap values and bootstrap derivation formulas;
- fail-closed probe exhaustion behavior;
- stable-id independence from physical probing;
- hot-loop enforcement boundaries;
- and ledger/accounting requirements.

ADR-0042 owns:

- concrete probing algorithm;
- heap/off-heap/`MemorySegment`/native layout;
- control-byte layout;
- physical stride;
- physical padding;
- internal fragmentation evidence;
- transient rebuild/resize memory evidence;
- read-path locality profile;
- physical cache-line / cache-miss evidence;
- branch-miss evidence;
- and backend-specific benchmark gates.

A compliant backend MAY use open addressing, grouped probing, Robin-Hood-style displacement, generated frozen indexes,
or
another ratified physical strategy.

Regardless of strategy, the admitted scope MUST satisfy the resolved ADR-0041 probe budget before stable intern ids
become planning-visible.

Stable intern id assignment remains deterministic even if the physical table rebuilds, resizes, relocates, compacts, or
changes probing strategy.

#### 13.13.1. Resolved Interner Probe Budget Vector Law

An interning scope MUST resolve a probe-budget vector before scope admission.

The resolved probe-budget vector is policy/admission material.

It is not a table-layout implementation.

Required scalar vocabulary:

``````text
admittedCandidateCount
    number of intern candidates admitted into this interning scope.

admittedCandidateCountByIdentityDomain[identityDomainId]
    number of candidates admitted for one identity domain.

logicalTableCapacitySlots
    physical table capacity measured in logical candidate slots.

logicalTableCapacitySlotsByIdentityDomain[identityDomainId]
    logical candidate slot capacity reserved for one identity domain when the scope uses domain-partitioned admission
    or a shared table with domain quotas.

maxLoadFactorNumerator / maxLoadFactorDenominator
    maximum admitted load factor as a rational number:
        maxLoadFactor = maxLoadFactorNumerator / maxLoadFactorDenominator

probeGroupWidthSlots
    number of logical candidate slots in one hot probe group.

maxHotProbeGroups
    maximum hot probe groups that may be scanned by one ordinary lookup or insert attempt.

maxProbeDisplacementSlots
    maximum logical displacement, measured in slots, admitted for ordinary hot probing.

logicalHotSlotBytes
    deterministic logical hot-probe projection byte cost per candidate slot.

maxHotProbeProjectionBytesPerOperation
    maximum logical hot-probe projection bytes read by one ordinary lookup or insert attempt.

maxHotCollisionCandidates
    maximum candidates in one ordinary hot collision group before escalation.

maxExactVerificationBytesPerProbe
    maximum canonical exact-verification bytes admitted for one lookup or insert attempt.

maxResizeCountPerScope
    maximum table resize count admitted for the scope before stable id publication.

maxRebuildCountPerScope
    maximum table rebuild / reindex count admitted for the scope before stable id publication.

maxRebuildScratchBytes
    maximum deterministic scratch bytes reserved for rebuild / reindex work.

maxMigrationMetadataBytes
    maximum deterministic metadata bytes required to describe migration from one table layout/capacity to another.

maxProbeDiagnosticsBytes
    maximum bounded diagnostic evidence bytes reserved for probe-budget failures.

backendPhysicalOverheadBytes
    deterministic physical backend overhead reserved for object headers, array headers, segment headers, alignment
    padding, allocator metadata, table headers, and internal fragmentation.

maxTransientRebuildBytes
    maximum additional bytes that may be simultaneously live during resize, rebuild, reindex, or backend migration.

maxColdCollisionStructureBytes
    maximum bytes reserved for a bounded cold collision structure when that escalation path is ratified.

maxColdCollisionProbeGroups
    maximum cold-structure probe groups that may be scanned after hot collision escalation.

identityTransientWorkBudgetBytes
    shared transient bytes reserved for exact verification, canonical sort scratch, SCC collision handling, and
    collision escalation work when those paths may overlap.

candidateCountingMode
    ratified counting mode for this interning scope.

laneQuotaChunkSize
    number of candidate-count units reserved at once for one engine lane in bounded streaming mode.

maxLaneQuotaRefillsPerScope
    maximum deterministic quota-refill events admitted for one scope.

laneCandidateQuotaByEngineLane[engineLaneId]
    candidate-count quota reserved for a specific engine lane.

laneStagedScratchBytesCapByEngineLane[engineLaneId]
    staged scratch byte cap reserved for a specific engine lane.

stagedCanonicalBytesCap
    maximum canonical byte payload bytes that may remain staged before seal / publication.

stagedScratchBytesCap
    maximum mutable scratch bytes that may remain staged before seal / publication.

stagedHandleBytesCap
    maximum provisional handle / candidate descriptor bytes that may remain staged before seal / publication.

stagedCandidateMetadataBytesCap
    maximum metadata bytes for staged candidate descriptors, ownership records, and seal bookkeeping.

maxInvalidationTraversalEdges
    maximum dependency edges that may be traversed to discover an incremental affected candidate set.

maxInvalidationTraversalNodes
    maximum dependency nodes that may be traversed to discover an incremental affected candidate set.

maxAffectedSetExpansionSteps
    maximum deterministic expansion steps admitted while building an incremental affected set.

maxReusedSealedReferenceReads
    maximum reads of already-sealed references admitted while proving that unchanged material may be reused.
``````

All values MUST be integers.

All ratio arithmetic MUST use integer arithmetic.

Floating-point arithmetic is forbidden in probe-budget admission.

Every multiplication, addition, shift, and `ceilDiv(...)` used by probe-budget resolution MUST be overflow-checked.

A resolved probe-budget vector with a non-positive count, non-positive capacity, negative byte budget, or overflowed
intermediate value is invalid and MUST fail before scope admission.

The validation order is normative:

``````text
1. validate denominator / numerator positivity and ordering
2. validate candidate count and capacity positivity
3. validate per-domain candidate counts and per-domain capacities
4. validate all byte-cap inputs are non-negative
5. validate all multiplication inputs before multiplication
6. validate every multiplication / addition for overflow
7. compute derived capacities
8. validate feasibility inequalities
9. admit or fail closed
``````

The implementation MUST NOT evaluate `ceilDiv(...)`, division, multiplication by ratio material, or derived byte
formulas before the corresponding denominator and numerator constraints have been validated.

Invalid profile material is a profile-admission failure.

It MUST NOT be represented by a hot-path arithmetic exception.

Admission-time checked arithmetic MAY use implementation exceptions internally only if they are caught at the admission
boundary and converted into a bounded fail-closed profile-admission result.

Hot-path probing arithmetic MUST NOT rely on exception-throwing overflow detection.

#### 13.13.1.1. Candidate Counting Phase Law

Candidate count is not discovered at the same phase for every interning scope.

A compliant interning scope MUST declare its counting mode before scope admission.

Allowed counting modes:

``````text
PRECOUNTED_BATCH
BOUNDED_STREAMING
PUBLISHED_TABLE
INCREMENTAL_AFFECTED_SET
RATIFIED_STATIC_REGISTRY
``````

Meanings:

``````text
PRECOUNTED_BATCH:
    candidateCountCap is resolved before execution.
    observedCandidateCount is accumulated during staging.
    sealedCandidateCount is known after the candidate set is closed and before identity seal.

BOUNDED_STREAMING:
    candidateCountCap is resolved before execution.
    observedCandidateCount is incremented as candidates are discovered.
    stable intern id publication is forbidden until deterministic seal.

PUBLISHED_TABLE:
    publishedTableCandidateCount is read from an immutable table header.
    no new candidate is admitted.

INCREMENTAL_AFFECTED_SET:
    affectedCandidateCount is produced by a deterministic invalidation/query boundary.
    reused sealed references are not counted as new candidates.
    traversal work required to discover the affected set is separately budgeted.

RATIFIED_STATIC_REGISTRY:
    candidate count is defined by a ratified protocol registry version and golden vectors.
``````

Admission-time feasibility uses the resolved cap:

``````text
candidateCountCap * maxLoadFactorDenominator
    <= plannedLogicalTableCapacitySlots * maxLoadFactorNumerator
``````

Streaming insertion uses the next observed value:

``````text
nextObservedCandidateCount * maxLoadFactorDenominator
    <= currentLogicalTableCapacitySlots * maxLoadFactorNumerator
``````

Batch seal may use the closed set count:

``````text
sealedCandidateCount * maxLoadFactorDenominator
    <= finalLogicalTableCapacitySlots * maxLoadFactorNumerator
``````

Published table read paths use the immutable table header:

``````text
publishedTableCandidateCount * maxLoadFactorDenominator
    <= publishedLogicalTableCapacitySlots * maxLoadFactorNumerator
``````

The chosen counting mode MUST NOT affect canonical bytes, HID derivation, collision verification, stable intern id
assignment, or semantic equality.

A streaming scope MUST NOT assign stable intern ids from discovery order.

The lawful streaming shape is:

``````text
candidate discovery
-> cap / quota / staged-byte admission
-> provisional candidate handle
-> deterministic seal
-> canonical ordering
-> stable intern id assignment
-> immutable publication
``````

The forbidden streaming shape is:

``````text
candidate discovery order
-> stable intern id assignment
-> later repair / reorder / rewrite
``````

#### 13.13.2. Domain-Partitioned Probe Admission Law

Scope-level probe feasibility is necessary but not sufficient.

A hot identity domain must not consume all table capacity and starve a cold identity domain that has already been
admitted into the same interning scope.

For every admitted identity domain, the solver MUST prove:

``````text
admittedCandidateCountByIdentityDomain[identityDomainId] > 0

logicalTableCapacitySlotsByIdentityDomain[identityDomainId] > 0

admittedCandidateCountByIdentityDomain[identityDomainId] * maxLoadFactorDenominator
    <= logicalTableCapacitySlotsByIdentityDomain[identityDomainId] * maxLoadFactorNumerator
``````

The domain partition may be implemented as:

- physically separate tables per identity domain;
- one table with deterministic domain slices;
- one table with deterministic domain quotas and routing prefixes;
- or another ratified layout that proves equivalent isolation.

For a shared physical table, the solver MUST additionally prove:

``````text
sum(logicalTableCapacitySlotsByIdentityDomain[*])
    <= logicalTableCapacitySlots
``````

A candidate insert MUST be rejected before publication if accepting it would violate the same integer
cross-multiplication
law used for admission-time feasibility:

``````text
nextCandidateCountByIdentityDomain =
    currentCandidateCountByIdentityDomain[identityDomainId] + pendingInsertCount

nextCandidateCountByIdentityDomain * maxLoadFactorDenominator
    <= logicalTableCapacitySlotsByIdentityDomain[identityDomainId] * maxLoadFactorNumerator
``````

The implementation MUST reject the insert before publication if the inequality is false.

This check MUST be evaluated with overflow-checked integer arithmetic.

It MUST NOT be rewritten as a threshold-division form such as:

``````text
nextCandidateCountByIdentityDomain
    <= integerThreshold(capacity * numerator / denominator)
``````

for hot insert validation.

The threshold-division form is forbidden for this law because it reintroduces division, invites floating-point threshold
interpretations, and weakens the existing cross-multiplication invariant.

The load-factor numerator and denominator MUST have already passed Section 13.13.1 validation before this check can be
executed.

If either product overflows the selected integer width, the operation MUST fail closed through a bounded probe-budget
classification before candidate visibility.

The domain quota is admission material.

It is not semantic equality material.

If an implementation chooses one physical table for multiple identity domains, it still MUST retain domain-specific
candidate counts, collision counts, and probe exhaustion accounting.

#### 13.13.3. Probe Capacity Feasibility Law

The probe capacity solver MUST prove load-factor feasibility before scope admission and after any resize or rebuild that
changes logical table capacity.

Required relationships:

``````text
0 < maxLoadFactorNumerator < maxLoadFactorDenominator

admittedCandidateCount > 0
logicalTableCapacitySlots > 0

admittedCandidateCount * maxLoadFactorDenominator
    <= logicalTableCapacitySlots * maxLoadFactorNumerator
``````

Equivalently:

``````text
logicalTableCapacitySlots
    >= ceilDiv(
           admittedCandidateCount * maxLoadFactorDenominator,
           maxLoadFactorNumerator
       )
``````

The implementation MAY use a backend-ratified capacity schedule.

For a power-of-two table backend, the accepted shape is:

``````text
minimumCapacityByLoad =
    ceilDiv(
        admittedCandidateCount * maxLoadFactorDenominator,
        maxLoadFactorNumerator
    )

minimumCapacity =
    max(
        minimumCapacityByLoad,
        minInternTableCapacitySlots
    )

logicalTableCapacitySlots =
    nextPowerOfTwo(minimumCapacity)

if logicalTableCapacitySlots > maxInternTableCapacitySlots:
    fail closed before scope admission
``````

For a non-power-of-two backend, `nextPowerOfTwo(...)` is replaced by a ratified deterministic capacity schedule:

``````text
logicalTableCapacitySlots = nextRatifiedCapacity(minimumCapacity)
``````

The capacity schedule MUST be deterministic and versioned.

It MUST NOT depend on:

- live heap availability;
- CPU count;
- NUMA topology;
- GC timing;
- current collision observations;
- previous run outcomes;
- table construction timing;
- JIT compilation state;
- or runtime profiling.

A scope that cannot satisfy load-factor feasibility MUST fail before stable id publication.

It MUST NOT silently lower `admittedCandidateCount`, drop candidates, or publish a partial intern table.

#### 13.13.4. Insert-Time and Resize-Time Revalidation Law

Initial scope admission does not replace runtime table-state validation.

A streaming candidate accumulation implementation MUST track:

``````text
currentCandidateCount
currentCandidateCountByIdentityDomain[identityDomainId]
currentLogicalTableCapacitySlots
currentLogicalTableCapacitySlotsByIdentityDomain[identityDomainId]
``````

Before an insert becomes visible to the table builder, the implementation MUST prove:

``````text
(currentCandidateCount + pendingInsertCount) * maxLoadFactorDenominator
    <= currentLogicalTableCapacitySlots * maxLoadFactorNumerator
``````

and, for the candidate's identity domain:

``````text
(currentCandidateCountByIdentityDomain[identityDomainId] + pendingInsertCount) * maxLoadFactorDenominator
    <= currentLogicalTableCapacitySlotsByIdentityDomain[identityDomainId] * maxLoadFactorNumerator
``````

After any resize, rebuild, reindex, migration, or backend table replacement, the implementation MUST revalidate the same
relationships against the new logical capacities before continuing insertion or publication.

A resize may increase capacity.

It MUST NOT retroactively admit more candidates than the resolved scope candidate cap allows.

If the resolved `admittedCandidateCount` is exceeded, the scope fails closed or must open a new explicitly admitted
scope.

It MUST NOT silently expand the semantic scope.

#### 13.13.4.1. BOUNDED_STREAMING Lane-Quota Reservation Law

A `BOUNDED_STREAMING` interning scope MUST NOT debit a single global candidate counter for every candidate on the
ordinary hot discovery path.

The preferred lawful shape is lane-owned quota reservation with a deterministic reserve pool:

``````text
scope admission
-> candidate caps / staged-byte caps resolved
-> engine lane set resolved
-> initial lane quota slices assigned deterministically
-> deterministic reserve pool retained by the scope
-> lane-local primitive quota debit during candidate discovery
-> deterministic reserve-pool refill at explicit safe points
-> deterministic quota reclamation barrier only when reserve-pool refill cannot satisfy a request
-> deterministic seal reconciliation
``````

The quota owner is the engine lane.

It is not the worker thread.

It is not a `ThreadLocal`.

It is not coroutine-local, virtual-thread-local, scheduler-owned hidden state, callback-local state, or adapter-local
state.

A lane quota slice MUST be represented by primitive lane-owned state selected by deterministic policy.

A refill-capable profile MUST split the total candidate cap into at least:

``````text
initialLaneQuotaTotal
scopeReserveQuotaPool
reclaimableLaneQuota
``````

Required relationships:

``````text
initialLaneQuotaTotal + scopeReserveQuotaPool <= candidateCountCap

sum(initialLaneQuotaByEngineLane[engineLaneId]) <= initialLaneQuotaTotal
``````

A profile MUST NOT preallocate the entire `candidateCountCap` into lane-local quota unless it selects a strict no-refill
profile and accepts deterministic early exhaustion as part of that profile.

A refill-capable profile SHOULD retain a deterministic reserve pool so that skewed lane demand is absorbed without
running the reclamation barrier on the ordinary path.

Allowed allocation strategies:

- deterministic preallocation by `engineLaneId`, identity domain, and resolved scope policy;
- deterministic reserve-pool refill processed by a maintenance owner at explicit safe points;
- deterministic chunked refill processed by a maintenance owner at explicit safe points;
- deterministic quota reclamation barrier before bounded refill failure where the profile permits refill;
- or a stricter no-refill policy that fails closed when lane quota is exhausted.

A chunked refill is lawful only if:

- refill requests are explicit events;
- grant order is deterministic by `engineLaneId`, identity domain id, and request sequence;
- `laneQuotaChunkSize` is resolved before scope admission;
- `maxLaneQuotaRefillsPerScope` is resolved before scope admission;
- all granted chunks are charged to the same scope candidate cap;
- reserve-pool grants are charged before reclamation is attempted;
- unused quota is reconciled at seal / scope close;
- refill failure reaches a bounded classification;
- and refill timing cannot change stable intern id assignment.

A deterministic quota reclamation barrier is required before fail-closed refill exhaustion when all of the following
hold:

- the scope uses refill-capable `BOUNDED_STREAMING`;
- a lane requests additional quota;
- the deterministic reserve pool cannot satisfy the request;
- other lanes may still hold unused reserved quota;
- and the scope has not selected the stricter no-refill profile.

The reclamation barrier MUST:

- stop new candidate admission for the affected scope or identity-domain slice at an explicit safe point;
- read each lane's primitive used / reserved / remaining quota state in deterministic `engineLaneId` order;
- reclaim only unused quota that has not been consumed by an already-issued provisional candidate;
- keep already-issued provisional candidates valid;
- redistribute reclaimed quota by deterministic `engineLaneId`, identity domain id, and request sequence;
- record reserve-pool grants, reclaimed quota, redistributed quota, and still-stranded quota in the probe ledger;
- resume admission only after the maintenance owner publishes the new quota state;
- and fail closed only if the deterministic reserve-pool refill and the deterministic reclamation pass still cannot
  satisfy the bounded refill request.

The reclamation barrier MUST NOT be implemented as opportunistic quota stealing.

Forbidden shapes:

``````text
lane A runs faster
-> lane A steals quota from lane B without a barrier
``````

``````text
first CAS winner
-> obtains remaining global quota
-> other lanes fail due to timing
``````

``````text
worker-local or ThreadLocal remaining quota
-> hidden ownership
-> non-deterministic reuse across worker migration
``````

The implementation MUST NOT decide quota ownership by:

- first CAS winner;
- worker scheduling order;
- callback completion order;
- thread id;
- wall-clock timing;
- live queue depth;
- current throughput;
- cache warmth;
- or GC behavior.

The ordinary hot path may decrement lane-local primitive counters.

It MUST NOT require a global atomic increment / decrement per discovered candidate.

Global or scope-level counters may be updated at bounded reconciliation points, not per candidate, unless a released
profile proves that the path is outside the hot discovery loop.

Quota stranding is a budget state, not semantic inequality.

A scope MUST NOT fail closed for quota exhaustion until the required deterministic reserve-pool refill and deterministic
quota reclamation barrier have either:

- recovered sufficient unused quota; or
- proven that no reserve-pool quota or reclaimable unused lane quota remains under the resolved policy.

#### 13.13.4.1.1. Deterministic Owner-Lane Routing Law

A refill-capable `BOUNDED_STREAMING` interner SHOULD prefer deterministic owner-lane routing over moving quota between
lanes.

The preferred lawful shape is:

``````text
candidate discovered on any execution lane
-> derive deterministic routing material
-> resolve ownerEngineLaneId from the route map
-> append to producer-local routed batch for that owner lane
-> flush batch through bounded routing transport
-> owner lane performs duplicate pre-screen, staging admission, provisional handle issuance, and seal participation
``````

The discovery lane is not the owner unless the resolved route map says so.

The owner lane is derived from versioned, substrate-neutral routing material such as:

- identity domain id;
- interning scope id;
- route-map version;
- routing epoch id;
- version-bundle fingerprint where required;
- domain-separated route digest / route prefix;
- canonical byte length where already known;
- or another ratified fixed-width route projection.

The owner lane MUST NOT be derived from:

- worker id;
- thread id;
- callback completion order;
- queue arrival order;
- first discovery order;
- wall-clock timing;
- current queue depth;
- current CPU utilization;
- current throughput;
- GC behavior;
- or runtime profiling.

Routing material is not equality authority.

`routePrefix`, `route64`, HID prefix material, verifier-prefix material, and local shape summaries MAY route candidates.

They MUST NOT replace canonical byte encoding, collision verification, deterministic stable id assignment, table
coverage
validation, or publication integrity validation.

The route map MUST be resolved before the route epoch admits candidates.

Changing the route map inside an open route epoch is forbidden.

A route-map change MAY occur only at an explicit route-epoch boundary, quota-epoch boundary, or separately admitted
scope boundary.

A route-map change MUST NOT change:

- canonical bytes;
- HID derivation;
- collision verification result;
- semantic equality;
- stable intern id assignment;
- stable id publication order;
- already-issued provisional handle ownership;
- or report/replay identity.

#### 13.13.4.1.2. Routed Candidate Batch Law

Routed candidate transport MUST be bounded.

A producer lane MAY batch outbound candidates by owner lane, identity domain, route epoch, and interning scope.

Batch routing is the preferred transport shape for owner-lane routing.

The resolved routing budget MUST define at least:

``````text
maxRoutedCandidateBatchSize
maxRoutedCandidateBatchBytes
maxRoutedBatchBufferBytesByEngineLane[engineLaneId]
maxPendingRoutedBatchesPerOwnerLane[ownerEngineLaneId]
maxOwnerInboxBytes[ownerEngineLaneId]
maxOwnerInboxBatches[ownerEngineLaneId]
maxRouteFlushRetries
maxRouteDrainStepsPerBackpressure
maxRouteBackpressureEventsPerScope
``````

A routed batch MUST flush when any of the following deterministic conditions holds:

- `maxRoutedCandidateBatchSize` is reached;
- `maxRoutedCandidateBatchBytes` is reached;
- owner lane changes;
- identity domain changes where the profile requires domain-segregated batches;
- branch frame closes;
- planning frame closes;
- semantic validation boundary is reached;
- scope safe point is reached;
- route epoch boundary is reached;
- quota epoch boundary is reached;
- seal preflight begins;
- publication preflight begins;
- rollback / failure boundary is reached;
- or the producer lane exits the owning pipeline phase.

A non-empty routed batch MUST NOT remain buffered across a boundary that could make its candidates unreachable,
unsealed, unaccounted, or owned by a closed branch / frame / scope.

The implementation MUST NOT flush routed batches based on:

- wall-clock timers;
- elapsed time;
- queue depth feedback;
- consumer speed;
- CPU utilization;
- throughput feedback;
- GC events;
- thread scheduling delay;
- or adaptive runtime profiling.

Partial-fill batches are legal only while their owning branch/frame/scope/route epoch remains open and reachable.

A partial-fill batch MUST flush at close, rollback, seal preflight, or publication preflight even if it contains fewer
candidates than `maxRoutedCandidateBatchSize`.

#### 13.13.4.1.3. Owner Inbox Backpressure and Deterministic Route-Drain Law

Owner-lane routing MUST NOT rely on unbounded inbox growth.

If a producer attempts to flush a routed batch and the owner inbox cannot admit it under the resolved inbox budget, the
implementation MUST enter a bounded deterministic route-drain path before terminal failure, unless the selected profile
explicitly uses a stricter no-drain fail-closed policy.

The lawful backpressure path is:

``````text
producer flush attempt
-> owner inbox admission fails under resolved inbox budget
-> route-backpressure event recorded
-> producer stops new admission for the affected route / domain / scope slice
-> deterministic route-drain safe point entered
-> owner inboxes drain in fixed ownerEngineLaneId order or by another ratified deterministic order
-> producer retries flush under maxRouteFlushRetries
-> success, or bounded route-backpressure classification
``````

Route-drain is step-bounded.

It is not time-bounded.

The implementation MUST NOT wait until an inbox becomes available based on wall-clock time.

The implementation MUST NOT spin until an inbox has space.

The implementation MUST NOT block a worker indefinitely.

Allowed terminal classifications include:

``````text
ROUTE_BACKPRESSURE_DRAINED
ROUTE_BACKPRESSURE_RETRY_EXHAUSTED
ROUTE_INBOX_CAP_EXHAUSTED
ROUTE_SCOPE_QUARANTINED
ROUTE_PUBLICATION_REJECTED_BEFORE_VISIBILITY
``````

Cooperative route-drain MAY help routing infrastructure make progress.

It MUST NOT transfer ownership of candidate staging, duplicate suppression, provisional handle issuance, collision
verification, stable id assignment, or publication away from the deterministic owner lane.

A producer lane MAY participate only in protocol-assigned drain work whose state transition remains attributed to the
owner lane or routing infrastructure owner.

It MUST NOT directly mutate another owner lane's candidate staging table, duplicate suppression table, provisional
handle table, or stable-id assignment state.

#### 13.13.4.1.4. Producer-Local Suppression Budget and Authority Law

Producer-local route suppression is an optional traffic-reduction mechanism.

It is not equality authority.

It MUST NOT replace owner-lane duplicate suppression, canonical byte encoding, collision verification, deterministic
stable id assignment, or publication integrity validation.

A lawful producer-local suppression structure MUST be:

- lane-owned;
- primitive-substrate compatible;
- bounded by resolved entry and byte caps;
- cleared or reconciled at branch/frame/scope/route-epoch boundaries;
- deterministic in eviction / overwrite behavior;
- and charged to the owning staging / routing memory envelope.

The resolved budget MUST define, when producer-local suppression is enabled:

``````text
localRouteSuppressionEntriesCapByEngineLane[engineLaneId]
localRouteSuppressionBytesCapByEngineLane[engineLaneId]
localRouteSuppressionBytesCapByIdentityDomain[identityDomainId]
localRouteSuppressionEvictionCountCap
``````

The implementation MUST NOT use:

- unbounded producer-local maps;
- `HashMap` / `HashSet` as committed hot-path suppression state;
- `ThreadLocal` suppression maps;
- worker-local suppression maps;
- runtime-adaptive suppression capacity;
- object identity;
- backend handle identity;
- or recent-seen state that crosses a branch/frame/scope boundary without explicit lifecycle ownership.

A suppression hit may suppress or merge routing traffic only under resolved policy.

It MUST NOT publish a stable id, reject semantic equality, or construct a cache/reuse key.

#### 13.13.4.1.5. Deterministic Route-Skew Handling Law

Route skew must be handled by deterministic ownership policy, not runtime timing.

The implementation MAY classify route pressure using deterministic ledger counters such as:

- routed candidate count by route bucket;
- routed batch count by route bucket;
- producer-local suppression hits by route bucket;
- owner-lane duplicate pre-screen hits by route bucket;
- owner inbox admission rejections by route bucket;
- staged bytes by route bucket;
- and exact unique candidate count by route bucket where already sealed or verified.

The implementation MUST NOT classify skew using:

- queue processing speed;
- wall-clock latency;
- CPU utilization;
- GC pause duration;
- allocation speed;
- thread scheduling delay;
- consumer lag measured by time;
- or runtime throughput.

A route-range split is lawful only if:

- it occurs at an explicit route-epoch or scope boundary;
- split inputs are deterministic ledger counters;
- the split formula is resolved before the new route epoch admits candidates;
- already-issued provisional handles keep their original owner;
- newly discovered candidates follow the new route map;
- route map version changes are recorded in bounded diagnostics;
- and stable intern id assignment remains based on canonical ordering, not owner lane.

Exact hot-key pressure MUST NOT be solved by splitting the same canonical candidate across multiple owner lanes.

For exact hot keys, the lawful mitigations are bounded duplicate suppression, cached resolved owner-lane material,
sealed-reference reuse where already published, or fail-closed / quarantine under resolved policy.

Range skew and exact hot-key pressure are distinct classifications.

#### 13.13.4.2. BOUNDED_STREAMING Staged Candidate Memory Budget Law

Candidate count safety does not imply staged memory safety.

A `BOUNDED_STREAMING` scope MUST meter staged physical material before seal.

The resolved budget MUST define at least:

``````text
preScreenStagingBytesCap
duplicateSuppressionTableBytesCap
localRouteSuppressionBytesCapByEngineLane[engineLaneId] where producer-local suppression is enabled
localRouteSuppressionBytesCapByIdentityDomain[identityDomainId] where producer-local suppression is enabled
routedBatchBufferBytesCapByEngineLane[engineLaneId] where owner-lane routing is enabled
ownerInboxBytesCapByOwnerLane[ownerEngineLaneId] where owner-lane routing is enabled
stagedCanonicalBytesCap
stagedScratchBytesCap
stagedHandleBytesCap
stagedCandidateMetadataBytesCap
laneStagedScratchBytesCapByEngineLane[engineLaneId]
stagedBytesCapByIdentityDomain[identityDomainId] where domain partitioning is enabled
``````

A `BOUNDED_STREAMING` implementation MAY use a bounded duplicate pre-screen stage before full canonical byte staging.

The lawful shape is:

``````text
candidate discovered
-> fixed-width pre-screen material staged
-> bounded duplicate suppression lookup
-> probable duplicate: delay or avoid full canonical staging until exact verification is required
-> probable unique: admit full canonical staging under staged byte caps
-> seal: exact canonical verification and deterministic deduplication
``````

Pre-screen material MAY include only ratified, bounded, domain-separated, version-bound material such as:

- identity domain id;
- version-bundle fingerprint;
- canonical byte length where already known;
- fixed-width local shape summary;
- bounded sort-key summary;
- HID / verifier prefix material derived under the active suite;
- and a lane-local staging ticket.

Pre-screen material is not equality authority.

Pre-screen duplicate suppression MUST NOT replace:

- canonical byte encoding;
- collision verification;
- deterministic stable id assignment;
- table coverage validation;
- or publication integrity validation.

Before a pre-screen entry is issued, the implementation MUST prove:

``````text
nextPreScreenStagingBytes <= preScreenStagingBytesCap
nextDuplicateSuppressionTableBytes <= duplicateSuppressionTableBytesCap
``````

Before a full provisional candidate handle is issued, the implementation MUST prove:

``````text
nextStagedCanonicalBytes <= stagedCanonicalBytesCap
nextStagedScratchBytes   <= stagedScratchBytesCap
nextStagedHandleBytes    <= stagedHandleBytesCap
nextStagedMetadataBytes  <= stagedCandidateMetadataBytesCap
``````

For the owning lane:

``````text
nextLaneStagedScratchBytes[engineLaneId]
    <= laneStagedScratchBytesCapByEngineLane[engineLaneId]
``````

For the candidate's identity domain, when domain partitioning is enabled:

``````text
nextDomainStagedBytes[identityDomainId]
    <= stagedBytesCapByIdentityDomain[identityDomainId]
``````

Staging material includes at least:

- pre-screen staging tickets;
- duplicate suppression table entries;
- producer-local route suppression entries;
- routed candidate batch buffers;
- owner inbox/routing queue entries;
- provisional candidate descriptors;
- candidate canonical bytes not yet sealed into an immutable artifact;
- candidate sort keys;
- inline verifier prefix/suffix staging;
- collision verification scratch;
- SCC-local temporary material where applicable;
- lane-local staging cursors;
- and bounded diagnostics attached to staged candidates.

A provisional handle MUST NOT pin unbounded staging slabs.

A duplicate pre-screen ticket MUST either be:

- promoted into a full provisional candidate under full staging caps;
- merged with an existing staged candidate after exact verification;
- rejected before provisional handle issuance;
- or discarded at rollback / scope close.

It MUST NOT become a stable intern id, semantic equality authority, PlanCacheKey material, frozen-image material,
report/replay identity, persistent artifact identity, cross-scope identity, or query reuse key.

If staged byte admission fails, the implementation MUST choose one of:

- fail the current identity scope closed;
- quarantine the current acquisition/planning scope;
- reject the current candidate before provisional handle issuance;
- delay full canonical staging behind a bounded duplicate pre-screen where lawful;
- or open a separately admitted scope if the caller explicitly owns that transition.

It MUST NOT discover staged-memory exhaustion through `OutOfMemoryError` after admitting the provisional handle.

#### 13.13.4.3. INCREMENTAL_AFFECTED_SET Traversal Budget Bridge Law

`INCREMENTAL_AFFECTED_SET` mode counts changed or newly staged identity candidates.

Reused sealed references are not new candidates.

However, discovering the affected set is not free.

Before an incremental affected-set interning scope is admitted, the resolved policy MUST define traversal budgets.

Required bridge vocabulary:

``````text
maxInvalidationTraversalEdges
maxInvalidationTraversalNodes
maxAffectedSetExpansionSteps
maxReusedSealedReferenceReads
maxInvalidationTraversalDiagnosticsBytes
fullRebuildMinimumReserveBytes
fullRebuildPreflightDiagnosticsBytes
``````

The implementation MUST meter the affected-set discovery phase against these budgets before candidate seal /
publication.

If affected-set discovery exceeds the resolved traversal budget, the implementation MUST enter a bounded diagnostic
classification before choosing an outcome.

Required classification vocabulary:

``````text
INCREMENTAL_TRAVERSAL_BUDGET_EXHAUSTED
INCREMENTAL_FULL_REBUILD_PREFLIGHT_PASSED
INCREMENTAL_FULL_REBUILD_PREFLIGHT_FAILED
INCREMENTAL_FULL_REBUILD_FALLBACK_ADMITTED
INCREMENTAL_FULL_REBUILD_FALLBACK_REJECTED
INCREMENTAL_DEPENDENCY_SHAPE_PATHOLOGICAL
INCREMENTAL_INVALIDATION_SCOPE_QUARANTINED
``````

Traversal budget exhaustion is not semantic inequality.

It is not stable-id evidence.

It is not allowed to silently publish a partial affected set.

Allowed deterministic outcomes are:

- fall back to a separately admitted full-rebuild scope;
- fail the incremental update closed;
- quarantine the incremental invalidation scope;
- reject the current artifact publication before planning visibility;
- or surface a bounded diagnostic that classifies the dependency shape as pathological for incremental mode.

A full-rebuild fallback is lawful only if the full-rebuild scope has its own resolved memory, traversal, staging,
interner, and publication budgets.

Before choosing `INCREMENTAL_FULL_REBUILD_FALLBACK_ADMITTED`, the implementation MUST run a full-rebuild preflight.

The preflight MUST prove at least:

``````text
fullRebuildMinimumReserveBytes <= availableFullRebuildReserveBytes
``````

and MUST include, where applicable:

- full graph traversal budget;
- full contract graph / metadata candidate count caps;
- full interner probe-table budget;
- full staged canonical bytes budget;
- full sort scratch budget;
- full SCC seal budget;
- full transient resize / rebuild high-water reserve;
- full diagnostics budget;
- and published artifact / replacement-image publication budget.

A failed full-rebuild preflight MUST be classified as `INCREMENTAL_FULL_REBUILD_PREFLIGHT_FAILED` or
`INCREMENTAL_FULL_REBUILD_FALLBACK_REJECTED`.

It MUST NOT allocate the full rebuild first and discover the failure through heap exhaustion, probe exhaustion, or
partial publication.

The implementation MUST NOT use the incremental traversal budget as implicit permission to run a full rebuild.

If repeated updates for the same contract graph, module snapshot, identity domain, or dependency slice exceed traversal
budgets, the implementation SHOULD classify the condition as an incremental-shape / dependency-graph pressure event
rather than treating every occurrence as an ordinary fallback.

The diagnostic payload MUST be bounded by `maxInvalidationTraversalDiagnosticsBytes` and, for full-rebuild preflight,
`fullRebuildPreflightDiagnosticsBytes`.

It SHOULD include only deterministic summary evidence such as:

- identity domain id;
- dependency slice id where ratified;
- traversal budget consumed;
- first boundary where the limit was exceeded;
- number of dirty candidates found before exhaustion;
- number of reused sealed references read before exhaustion;
- full-rebuild minimum reserve requested;
- full-rebuild reserve available;
- and the selected deterministic outcome.

It MUST NOT include unbounded edge lists, object graph dumps, backend handles, source traversal order, callback order,
or
worker scheduling traces.

ADR-0043 owns the full contract-graph invalidation law, dependency-edge semantics, repeated traversal pressure policy,
and structural/contextual graph identity model.

ADR-0041 requires only that any `INCREMENTAL_AFFECTED_SET` interning scope provide bounded traversal admission, bounded
traversal-exhaustion classification, and full-rebuild preflight before it may publish stable identity material through a
fallback path.

#### 13.13.5. Hot Probe Work Feasibility Law

The hot probe work bound is derived from group count, group width, and logical hot-slot byte cost.

Required formulas:

``````text
maxVisitedSlots =
    maxHotProbeGroups * probeGroupWidthSlots

maxProbeDisplacementSlots + 1 <= maxVisitedSlots

maxHotProbeProjectionBytesPerOperation =
    maxVisitedSlots * logicalHotSlotBytes
``````

The solver MUST fail closed before scope admission if:

- `probeGroupWidthSlots <= 0`;
- `maxHotProbeGroups <= 0`;
- `logicalHotSlotBytes <= 0`;
- `maxProbeDisplacementSlots < 0`;
- `maxVisitedSlots` overflows;
- `maxHotProbeProjectionBytesPerOperation` overflows;
- `maxProbeDisplacementSlots + 1 > maxVisitedSlots`;
- `maxHotProbeProjectionBytesPerOperation > configuredMaxHotProbeProjectionBytesPerOperation`;
- or the selected backend cannot prove that one ordinary probe attempt terminates within `maxVisitedSlots`.

The overflow checks in this section are explicit obligations.

They are not merely inherited from Section 13.13.1.

`logicalHotSlotBytes` is a deterministic logical projection cost.

It is not a JVM object-size claim.

It is not a physical cache-line alignment claim.

It is not a physical cache-miss predictor.

It may represent:

- HID words;
- domain/version projection;
- canonical byte length;
- inline verifier prefix words;
- stable id / candidate id projection;
- state/generation/control bits;
- or table-level proof material amortized through a deterministic layout calibration.

Backend-specific storage offsets, base addresses, object headers, pointer widths, cache-line alignment, stride,
cache-line
touch count, and cache-miss behavior belong to ADR-0042 substrate profiles.

They are not ADR-0041 semantic identity material.

#### 13.13.6. Probe Displacement Definition Law

`maxProbeDisplacementSlots` is measured in logical slot distance from the candidate's deterministic home slot to the
candidate's observed slot under the selected probing sequence.

For grouped probing, the relationship to probe groups is:

``````text
visitedProbeGroupsForDisplacement =
    ceilDiv(maxProbeDisplacementSlots + 1, probeGroupWidthSlots)

visitedProbeGroupsForDisplacement <= maxHotProbeGroups
``````

A backend that cannot define logical displacement for its selected probing strategy MUST define an equivalent bounded
progress metric before profile admission.

The equivalent metric MUST prove ordinary lookup/insert termination within `maxVisitedSlots`.

#### 13.13.7. Bootstrap Probe Profiles

ADR-0041 v1 defines deterministic bootstrap probe profiles.

The exact values may later move into a resolved policy table, but a released v1 implementation MUST produce a concrete
resolved probe-budget vector equivalent to one of these profiles or a stricter profile.

``````text
SMALL:
    maxLoadFactorNumerator                     = 1
    maxLoadFactorDenominator                   = 2
    probeGroupWidthSlots                       = 4
    maxHotProbeGroups                          = 4
    maxProbeDisplacementSlots                  = 15
    maxHotCollisionCandidates                  = 4
    maxResizeCountPerScope                     = 1
    maxRebuildCountPerScope                    = 1
    logicalHotSlotBytesTarget                  = 64
    configuredMaxHotProbeProjectionBytesPerOp  = 1024
    configuredMaxExactVerificationBytesPerProbe =
        4 * maxCanonicalBytesPerInternCandidate
    maxProbeDiagnosticsBytesFloor              = 4096
    rebuildScratchBytesPerLogicalSlot          = 16
    migrationMetadataBytesPerLogicalSlot       = 8
    backendPhysicalOverheadBytes               = backendProfileDeclaredPhysicalOverheadBytes
    maxTransientRebuildBytes                   = derived by Section 13.13.8.2

STANDARD:
    maxLoadFactorNumerator                     = 2
    maxLoadFactorDenominator                   = 3
    probeGroupWidthSlots                       = 8
    maxHotProbeGroups                          = 4
    maxProbeDisplacementSlots                  = 31
    maxHotCollisionCandidates                  = 8
    maxResizeCountPerScope                     = 2
    maxRebuildCountPerScope                    = 2
    logicalHotSlotBytesTarget                  = 64
    configuredMaxHotProbeProjectionBytesPerOp  = 2048
    configuredMaxExactVerificationBytesPerProbe =
        8 * maxCanonicalBytesPerInternCandidate
    maxProbeDiagnosticsBytesFloor              = 8192
    rebuildScratchBytesPerLogicalSlot          = 16
    migrationMetadataBytesPerLogicalSlot       = 8
    backendPhysicalOverheadBytes               = backendProfileDeclaredPhysicalOverheadBytes
    maxTransientRebuildBytes                   = derived by Section 13.13.8.2

LARGE:
    maxLoadFactorNumerator                     = 3
    maxLoadFactorDenominator                   = 4
    probeGroupWidthSlots                       = 8
    maxHotProbeGroups                          = 6
    maxProbeDisplacementSlots                  = 47
    maxHotCollisionCandidates                  = 12
    maxResizeCountPerScope                     = 2
    maxRebuildCountPerScope                    = 2
    logicalHotSlotBytesTarget                  = 64
    configuredMaxHotProbeProjectionBytesPerOp  = 3072
    configuredMaxExactVerificationBytesPerProbe =
        12 * maxCanonicalBytesPerInternCandidate
    maxProbeDiagnosticsBytesFloor              = 16384
    rebuildScratchBytesPerLogicalSlot          = 16
    migrationMetadataBytesPerLogicalSlot       = 8
    backendPhysicalOverheadBytes               = backendProfileDeclaredPhysicalOverheadBytes
    maxTransientRebuildBytes                   = derived by Section 13.13.8.2
``````

For v1:

``````text
AUTO = STANDARD
``````

A stricter profile may reduce load factor, probe groups, displacement, collision-group size, resize count, rebuild
count, or hot probe bytes.

A stricter profile may not weaken determinism, collision verification, or publication rules.

A looser profile requires explicit ratification, benchmark evidence, capacity feasibility tests, and golden vectors.

`logicalHotSlotBytesTarget = 64` is a logical hot-probe projection target.

It is not a claim that JVM heap arrays are physically 64-byte aligned.

It is not a promise that one lookup touches exactly one cache line.

A high-performance substrate backend may use 64-byte or wider aligned physical probe groups only under ADR-0042
evidence.

The gap between average expected probe behavior and worst-case probe bound is intentional.

ADR-0041 defines deterministic tail-bound admission.

ADR-0042 may ratify stricter backend-specific profiles when benchmark and layout evidence support them.

#### 13.13.8. Intern Table Byte Feasibility Law

Probe admission must account for intern-table structural bytes before scope admission.

The solver MUST validate:

``````text
totalInternTableBytes(resolvedProbeCaps) <= internTableBudgetBytes
``````

`totalInternTableBytes(...)` MUST include at least:

- logical hot-slot projection bytes:
  `logicalTableCapacitySlots * logicalHotSlotBytes`;
- control/state bytes;
- occupancy metadata bytes;
- generation/state metadata bytes;
- stable-id projection bytes;
- candidate-id projection bytes where required;
- collision-group metadata bytes;
- overflow/cold-collision structure header bytes where ratified;
- cold collision structure bytes where that escalation path is ratified:
  `maxColdCollisionStructureBytes`;
- table-level proof bytes;
- version-bundle proof bytes where required;
- rebuild/reindex scratch bytes:
  `maxRebuildScratchBytes`;
- migration metadata bytes:
  `maxMigrationMetadataBytes`;
- bounded probe diagnostic reserve:
  `maxProbeDiagnosticsBytes`;
- backend physical overhead bytes:
  `backendPhysicalOverheadBytes`;
- transient rebuild/resize/migration bytes:
  `maxTransientRebuildBytes`;
- deterministic alignment and padding bytes from the selected layout calibration;
- and fixed table headroom bytes.

The exact physical values are substrate-backend and calibration material.

The accounting obligation is ADR-0041 material.

A backend MAY store the same logical material using heap primitive arrays, off-heap memory, `MemorySegment`, native
aligned storage, mapped image indexes, or generated layouts.

The backend MUST provide a deterministic physical-layout byte model before scope admission.

The model MUST account for, where applicable:

- JVM array/object headers in heap primitive backends;
- SoA array splitting overhead;
- direct buffer / segment header overhead;
- native allocator metadata;
- memory-mapped page/header overhead;
- table header/footer material;
- control-byte region padding;
- probe-group alignment padding;
- internal fragmentation;
- allocator granularity;
- and backend-specific fixed headroom.

This model is backend evidence.

It is not semantic identity material.

The backend MUST prove that its physical layout does not exceed the admitted byte budget for the scope.

If `totalInternTableBytes(resolvedProbeCaps)` exceeds `internTableBudgetBytes`, the scope MUST fail before stable id
publication.

#### 13.13.8.1. Physical Overhead and Internal Fragmentation Law

Logical hot-slot bytes are not sufficient proof of physical memory feasibility.

A backend that splits logical probe material across multiple physical arrays, buffers, segments, native regions, mapped
pages, or generated layout regions MUST include the resulting physical overhead in `backendPhysicalOverheadBytes`.

`backendPhysicalOverheadBytes` MUST be deterministic for the selected backend profile, runtime profile, and resolved
layout calibration.

It MUST NOT be computed from:

- current heap free bytes;
- current GC region state;
- current allocator success/failure;
- observed object addresses;
- live telemetry;
- or previous run allocation outcomes.

If a backend cannot provide a deterministic physical overhead model, it may still be a portable semantic backend only if
it admits no exact physical memory claim and remains inside a conservative budget envelope.

A backend claiming the high-performance mechanical profile MUST provide physical overhead evidence.

#### 13.13.8.2. Transient Resize / Rebuild Memory Spike Law

Resize, rebuild, reindex, migration, and compaction may require old and new tables to be simultaneously live.

This transient two-table interval must be budgeted before scope admission.

`maxTransientRebuildBytes` is a high-water reserve.

It is not the sum of every possible resize / rebuild spike across the scope.

Required relationship:

``````text
maxTransientRebuildBytes
    >= maxOverAllowedResizeOrRebuildEvents(
           maxSimultaneouslyLiveOldTableBytes(event)
         + maxSimultaneouslyLiveNewTableBytes(event)
         + maxRebuildScratchBytes(event)
         + maxMigrationMetadataBytes(event)
       )
``````

For a single event, the required event spike is:

``````text
transientRebuildBytesForEvent(event)
    =
        maxSimultaneouslyLiveOldTableBytes(event)
      + maxSimultaneouslyLiveNewTableBytes(event)
      + maxRebuildScratchBytes(event)
      + maxMigrationMetadataBytes(event)
``````

Then:

``````text
maxTransientRebuildBytes
    >= max(transientRebuildBytesForEvent(event))
       over every resize / rebuild / reindex / migration event admitted by the profile
``````

`maxMigrationMetadataBytes` is the deterministic metadata required to track a migration operation.

It includes, where applicable:

- old-to-new slot mapping material;
- domain-slice remap material;
- displacement recomputation scratch;
- generation/state transition metadata;
- rebuild diagnostics reserved for the migration;
- and backend-specific migration headers.

If the backend uses grow-by-copy resizing, the conservative v1 bound for one event is:

``````text
maxSimultaneouslyLiveOldTableBytes(event) =
    totalInternTableBytes(currentTableCapsForEvent)

maxSimultaneouslyLiveNewTableBytes(event) =
    totalInternTableBytes(nextTableCapsForEvent)
``````

For example, if a backend admits two grow-by-copy resize events:

``````text
table X  -> table 2X
table 2X -> table 4X
``````

then the transient reserve is based on the larger event high-water mark:

``````text
max(
    totalBytes(X)  + totalBytes(2X) + eventScratchAndMetadata(X -> 2X),
    totalBytes(2X) + totalBytes(4X) + eventScratchAndMetadata(2X -> 4X)
)
``````

not the sum of both event spikes.

A backend may use a lower bound only if it proves a deterministic in-place, segmented, paged, or incremental migration
strategy that never has both complete tables live.

If `maxResizeCountPerScope > 0` or `maxRebuildCountPerScope > 0`, then `maxTransientRebuildBytes` MUST be positive and
MUST fit inside the scope's resolved transient memory reserve.

A profile that cannot reserve transient rebuild bytes MUST set:

``````text
maxResizeCountPerScope  = 0
maxRebuildCountPerScope = 0
``````

or fail admission before stable id publication.

The implementation MUST NOT discover the transient spike by actually allocating the new table first.

Admission must prove the transient high-water reserve before allocation.

The transient high-water reserve may be reused across sequential resize / rebuild events only after the previous event
has released its old table, scratch, and migration metadata according to ADR-0042 lifecycle rules.

Overlapping resize / rebuild events in the same scope MUST either:

- be forbidden by policy; or
- be included in the high-water calculation as simultaneously live events.

#### 13.13.9. Collision Amplification Budget Law

Collision verification is mandatory, but collision amplification is budgeted.

The solver MUST reserve hot collision and cold exact-verification budget before publication.

Required relationships:

``````text
maxHotCollisionCandidates > 0

maxHotCollisionCandidates <= configuredMaxHotCollisionCandidates

maxExactVerificationBytesPerProbe =
    maxHotCollisionCandidates * maxCanonicalBytesPerInternCandidate

maxExactVerificationBytesPerProbe <= configuredMaxExactVerificationBytesPerProbe
``````

A domain MAY ratify a smaller `maxExactVerificationBytesPerCandidate` when its canonical material has a smaller local
fuse.

In that case:

``````text
maxExactVerificationBytesPerProbe =
    maxHotCollisionCandidates * min(
        maxCanonicalBytesPerInternCandidate,
        maxExactVerificationBytesPerCandidate
    )
``````

If a HID, route, or compact descriptor group exceeds `maxHotCollisionCandidates`, the implementation MUST NOT continue
ordinary hot verification.

It MUST enter the collision escalation law.

Allowed escalation outcomes are those defined by Section 13.21.

The implementation MUST NOT:

- continue an unbounded collision chain;
- allocate exception objects for each candidate;
- log per-candidate unbounded diagnostics;
- retry the same group without a deterministic state transition;
- or promote a digest-only match to equality.

#### 13.13.10. SCC Collision Budget Coupling Law

SCC sealing does not bypass interner collision budgets.

If SCC members, SCC-local references, or SCC seal payload candidates enter an intern table and share the same HID,
route,
or compact descriptor group, they count against `maxHotCollisionCandidates` and `maxExactVerificationBytesPerProbe`.

SCC-local temporary references MAY be used before final seal only under the SCC laws in Section 13.30.

They MUST NOT become a side channel for unbounded collision verification.

The SCC seal process MUST reserve one of the following before publication:

1. sufficient ordinary collision budget for the SCC member group;
2. a bounded cold collision structure budget linked to Section 13.21; or
3. a fail-closed SCC seal outcome when the collision budget is exceeded.

If an SCC collision group exceeds the resolved collision budget, the implementation MUST fail the SCC seal closed
before:

- stable intern id assignment;
- parent table publication;
- frozen image publication;
- planning visibility;
- or persistent artifact publication.

#### 13.13.11. Sort Scratchpad and Probe Budget Non-Double-Spend Law

Exact verification may require canonical byte comparison only.

Some domains may additionally require canonical sort scratch during collision verification if their exact-verification
path invokes collection canonicalization or cold exact sort.

Those bytes are not free.

If collision verification can invoke canonical sort or cold sort machinery, the solver MUST prove one of:

``````text
identityTransientWorkBudgetBytes
    >= maxExactVerificationBytesPerProbe
     + maxCanonicalSortScratchBytesPerScope
     + maxColdCollisionStructureBytes
``````

or:

``````text
exact verification scratch
canonical sort scratch
cold collision structure scratch
are disjoint by construction and separately budgeted
``````

The implementation MUST NOT charge the same transient scratch bytes simultaneously to:

- collision exact verification;
- canonical collection sorting;
- cold exact sorting;
- SCC seal collision handling;
- and cold collision structure escalation.

If the implementation cannot prove non-overlap or sufficient shared reserve, the identity scope fails closed before
stable id publication.

#### 13.13.12. Cold Collision Structure Budget Coupling Law

A bounded cold collision structure is lawful only if its budget is resolved before escalation.

The resolved probe budget MUST define, or explicitly disable, the cold collision path.

If enabled, the resolved budget MUST define at least:

``````text
maxColdCollisionStructureBytes
maxColdCollisionProbeGroups
maxColdCollisionCandidates
maxColdExactVerificationBytes
``````

The cold collision structure may consume either:

1. a sub-budget reserved inside the same interner probe budget vector; or
2. a separately resolved cold-collision budget that is linked by scope id, identity domain id, and publication epoch.

The linkage MUST be deterministic.

It MUST NOT be acquired lazily from live heap availability after escalation.

If no cold collision budget is resolved, the only lawful outcome after hot collision escalation is fail-closed or
quarantine under Section 13.21.

#### 13.13.13. Resize, Rebuild, and Reindex Budget Law

Resize, rebuild, and reindex work are deterministic physical work.

They are not semantic identity.

A resolved probe-budget vector MUST declare:

``````text
maxResizeCountPerScope
maxRebuildCountPerScope
maxRebuildScratchBytes
maxMigrationMetadataBytes
maxTransientRebuildBytes
``````

Required relationships:

``````text
0 <= observedResizeCount <= maxResizeCountPerScope
0 <= observedRebuildCount <= maxRebuildCountPerScope
maxRebuildScratchBytes <= rebuildScratchBytesCap
maxMigrationMetadataBytes <= migrationMetadataBytesCap
maxTransientRebuildBytes <= transientRebuildBytesCap
``````

If `maxResizeCountPerScope` or `maxRebuildCountPerScope` is exhausted before the scope can satisfy table feasibility,
the
implementation MUST fail the scope closed before stable id publication.

Resize / rebuild MAY change:

- physical bucket index;
- group placement;
- displacement distance;
- physical slot address;
- table capacity;
- control-byte layout;
- and backend-local probe traces.

Resize / rebuild MUST NOT change:

- canonical bytes;
- HID derivation;
- collision verification result;
- stable intern id assignment;
- stable id publication order;
- semantic equality;
- report/replay identity;
- or planning-visible facts.

Stable intern ids are assigned from collision-verified canonical material and deterministic canonical ordering.

They MUST NOT be assigned from:

- bucket index;
- probe order;
- displacement distance;
- resize timing;
- rebuild timing;
- insertion order;
- table construction timing;
- physical memory address;
- or backend-specific table layout.

#### 13.13.14. Saturated Segment and Probe Exhaustion Law

Probe exhaustion is a bounded terminal classification for the current operation or scope.

If lookup or insertion exceeds any of the following:

- `maxHotProbeGroups`;
- `maxVisitedSlots`;
- `maxProbeDisplacementSlots`;
- `maxHotProbeProjectionBytesPerOperation`;
- `maxHotCollisionCandidates`;
- `maxExactVerificationBytesPerProbe`;
- `maxResizeCountPerScope`;
- `maxRebuildCountPerScope`;
- `maxRebuildScratchBytes`;
- `maxMigrationMetadataBytes`;
- `maxTransientRebuildBytes`;
- `maxColdCollisionStructureBytes` where enabled;
- `identityTransientWorkBudgetBytes` where shared transient work is enabled;
- or any per-domain capacity slice;

the implementation MUST NOT continue unbounded probing.

Allowed outcomes:

- fail the current identity scope closed;
- quarantine the current acquisition scope;
- fail the current artifact publication closed;
- move the candidate group into a bounded cold collision structure where ratified and pre-budgeted;
- trigger a deterministic rebuild if rebuild budget remains;
- trigger a deterministic resize if resize budget remains and publication has not occurred;
- or reject the current publication attempt before planning visibility.

Probe exhaustion MUST be represented by a primitive fault classification or bounded diagnostic record.

It MUST NOT be represented by:

- unbounded exception allocation;
- unbounded log emission;
- retry spinning;
- hidden worker-local state;
- leaked thread-local state;
- or process-wide hard termination as the ordinary path.

#### 13.13.15. Probe Ledger Accounting Law

A compliant implementation MUST account for probe work.

The interner ledger or scope-local accounting record MUST meter at least:

- hot probe attempts;
- hot probe groups scanned;
- logical candidate slots visited;
- maximum observed displacement;
- probe byte budget consumed;
- per-domain candidate counts;
- per-domain capacity-slice consumption;
- lane quota grants;
- initial lane quota total;
- deterministic reserve-pool quota;
- reserve-pool grants;
- lane quota refills;
- lane-local quota consumed;
- unused lane quota reconciled at seal / scope close;
- quota reclamation barriers;
- quota reclaimed from inactive or underused lanes;
- quota redistribution grants after reclamation;
- quota still stranded after reclamation;
- reserve-pool exhaustion outcomes;
- route-map version and routing epoch id;
- routed candidate batches created;
- routed candidate batch flushes;
- partial-fill flush-on-boundary events;
- owner inbox admission attempts;
- owner inbox backpressure events;
- deterministic route-drain steps;
- route backpressure terminal classifications;
- producer-local suppression entries;
- producer-local suppression bytes;
- producer-local suppression evictions;
- route-range split classifications;
- exact hot-key pressure classifications;
- pre-screen staging bytes;
- duplicate suppression table bytes;
- delayed full staging outcomes;
- staged canonical bytes;
- staged scratch bytes;
- staged provisional handle bytes;
- staged candidate metadata bytes;
- invalidation traversal edges;
- incremental traversal budget-exhaustion classifications;
- full-rebuild preflight pass / fail outcomes;
- full-rebuild minimum reserve requested;
- full-rebuild reserve available;
- full-rebuild fallback admission / rejection outcomes;
- invalidation traversal nodes;
- affected-set expansion steps;
- reused sealed reference reads;
- collision candidates examined;
- inline verifier prefix comparisons;
- cold exact-verification attempts;
- exact canonical bytes compared;
- resize count;
- rebuild count;
- rebuild scratch bytes;
- migration metadata bytes;
- backend physical overhead bytes;
- transient high-water rebuild / resize bytes;
- cold collision structure bytes where enabled;
- shared transient identity work bytes where enabled;
- saturated segment outcomes;
- collision escalation outcomes;
- and bounded diagnostic evidence bytes.

Accounting uses deterministic counters.

Budget enforcement in the hot probe loop SHOULD use loop-local primitive counters and compare-against constants that
were resolved before scope admission.

The ordinary hot loop MUST NOT require global atomic counter mutation per visited slot.

The ordinary bounded-streaming discovery loop MUST NOT require global atomic counter mutation per discovered candidate.

Lane-local quota debit is the preferred hot-path enforcement shape.

Detailed diagnostics MAY be aggregated through lane-local counters, scope-local snapshots, or cold-path summaries after
an operation reaches a bounded classification.

The implementation MUST NOT use elapsed wall-clock time as semantic budget.

Runtime watchdogs may abort at an outer orchestration boundary.

They MUST NOT reinterpret probe exhaustion as semantic inequality or stable id assignment evidence.

#### 13.13.16. Published Read-Path Locality Handoff Law

ADR-0041 probe budgets bound semantic admission, candidate publication, and worst-case logical probe work.

They do not prove published read-path cache residency.

A high-performance claim for repeated published lookups MUST be owned by ADR-0042 substrate profile evidence.

That evidence SHOULD define:

- hot/cold table split;
- expected cache-line touch model;
- maximum physical cache lines touched by the ordinary probe path where measurable;
- branch policy;
- prefetch policy if any;
- lane-local read ownership;
- table epoch and reader lease interaction;
- and benchmark gates for representative TypeReference-scale workloads.

ADR-0041 requires the handoff.

ADR-0041 does not make cache residency semantic identity.

#### 13.13.16.1. Owning Memory Envelope Integration Law

Interner memory is not free add-on memory.

An interner scope MUST charge its resolved memory to the owning bounded-context memory envelope.

Allowed owning envelopes include, where applicable:

- frozen acquisition memory envelope;
- frozen image publication envelope;
- metadata identity seal envelope;
- planning run memory envelope;
- L2 interner/cache partition envelope;
- contract-graph lowering envelope once ADR-0043 ratifies it;
- VM execution envelope once the VM execution pipeline ratifies primitive identity consumption;
- reporting/diagnostic envelope for bounded report material only.

The owning envelope MUST include, or explicitly reject, at least:

- interner probe-table bytes;
- candidate staging bytes;
- canonical byte staging bytes;
- provisional handle bytes;
- sort scratch bytes;
- exact-verification scratch bytes;
- SCC-local temporary identity bytes;
- collision escalation bytes;
- cold collision structure bytes where enabled;
- transient resize/rebuild/migration bytes;
- published-retention bytes where the table survives the producer scope;
- and bounded diagnostics.

A planning run that uses interning charges the interner to the planning run memory envelope.

A frozen acquisition pass that uses interning charges the interner to the frozen acquisition memory envelope.

An L2 cache/interner charges the interner to the L2 storage / partition envelope.

A published metadata identity table that survives its producer scope charges retained bytes to the published artifact or
metadata identity retention envelope.

The implementation MUST NOT admit identity interning by adding unbounded extra memory on top of the already resolved
planning, frozen, L2, VM, or reporting budgets.

If the owning envelope has insufficient remaining capacity, the interner scope MUST fail admission, choose a stricter
profile, or require an explicitly resolved larger envelope before work begins.

It MUST NOT discover envelope exhaustion through late OOM during staging, resize, seal, publication, or reporting.

#### 13.13.17. ADR-0042 Backend Profile Handoff Law

ADR-0041 probe budgets are backend-neutral admission contracts.

ADR-0042 substrate profiles must prove that their chosen physical layout and probing strategy satisfy these budgets.

A backend profile must document at least:

- capacity schedule;
- load-factor support;
- per-domain capacity partitioning or equivalent isolation;
- group width;
- displacement implementation or equivalent bounded progress metric;
- collision-group representation;
- hot-slot projection byte cost;
- physical overhead model;
- internal fragmentation model;
- transient rebuild/resize memory model;
- cold collision structure budget where enabled;
- read-path locality profile where high-performance lookup is claimed;
- lane-quota reservation profile for bounded streaming scopes;
- staged candidate memory model;
- incremental affected-set traversal budget bridge where applicable;
- owning memory-envelope integration proof;
- rebuild/resize strategy;
- scratch reservation;
- physical alignment claim if any;
- fallback behavior;
- and benchmark / golden-vector evidence.

A backend that cannot prove compliance with the resolved ADR-0041 probe budget MUST fail backend profile admission
before
identity scope admission or fall back to a compliant profile.

It MUST NOT weaken the ADR-0041 budget after candidates have been admitted.

### 13.14. Intern Probe Group Projection Law

The intern probe group is a hot projection of canonical identity material.

It is not the canonical identity envelope itself.

A compliant intern probe group SHOULD be able to fit ordinary first-probe rejection material in one logical 64-byte
group.

Illustrative projected group:

``````text
HID128                         16 bytes
inlineVerifierPrefix128        16 bytes
versionBundleFingerprint64      8 bytes
canonicalBytesOffset32          4 bytes
canonicalBytesLength32          4 bytes
stableInternId32                4 bytes
domain16 + schemaClass16        4 bytes
state / generation / probe       8 bytes
reserved / padding              remaining
``````

The exact layout is implementation-specific.

The law is:

``````text
first probe loads compact projected metadata;
full canonical envelope bytes are loaded only after projected checks pass.
``````

### 13.14.1. HID256 Hot / Cold Probe Boundary

`HID256` is the default width for strong artifact summaries, persistent content identity, replay manifest roots, and
cold integrity surfaces.

Hot intern-table membership SHOULD use `HID128` plus inline verifier metadata by default.

A 64-byte logical first-probe group is sized for ordinary hot membership metadata, not for carrying every possible
strong digest word.

If a domain requires `HID256` participation in hot lookup, it MUST define a domain-specific probe layout.

A `HID256` hot lookup implementation MUST NOT claim compliance with the 64-byte first-probe group law unless its
first-probe projection actually fits within that group.

Allowed strategies include:

- `HID128` hot projection with cold `HID256` verification;
- 128-byte logical probe groups;
- two-stage probe groups;
- or a domain-ratified equivalent that preserves the same verification ladder.

`FROZEN_IMAGE_CONTENT_SUMMARY` is a publication-time or artifact-level identity by default.

It is not a per-lookup hot membership key unless a later implementation note explicitly proves that use case.

### 13.15. JVM Layout Baseline and Future Value-Type Adoption Gate

The baseline JVM implementation MUST assume ordinary heap objects are pointer-rich and alignment-opaque.

Therefore hot identity tables SHOULD use:

- primitive arrays;
- fixed-width primitive fields;
- struct-of-arrays or array-of-struct-of-arrays layouts;
- explicit slab offsets;
- no boxed `Long` / `ULong` keys in generic collections;
- no hot-path `ByteArray` wrapper dereference for HID comparison.

Future JVM value-type or inline-object features may be adopted only after they prove:

- no additional pointer chasing;
- no unexpected boxing;
- no layout regression;
- no JIT deoptimization regression;
- equal or better microbenchmark results against the primitive-array baseline.

A preview runtime feature is not semantic authority.

### 13.15.1. JVM Value-Type Adoption Proof

The primitive-array / struct-of-arrays implementation is the baseline JVM hot-path layout.

Future JVM value-type, inline-object, flattened-object, or Valhalla-style implementations MAY replace the baseline only
after a release-specific adoption proof is published.

The proof MUST compare against the primitive-array baseline under the same:

- candidate cardinality;
- load factor;
- identity domain distribution;
- canonical byte length distribution;
- collision distribution;
- probe policy;
- verification ladder;
- HID width;
- small-inline threshold;
- thread count;
- and target runtime profile.

The adoption proof MUST show no regression under the release threshold table for:

- p50 / p95 / p99 probe latency;
- average probe count;
- allocation bytes per lookup;
- GC allocation rate;
- full-verification rate;
- JIT deoptimization / uncommon-trap regression where measurable;
- throughput under fixed thread count;
- and throughput under fixed NUMA / lane topology where applicable.

If the proof is absent, ambiguous, or workload-narrow, the implementation MUST keep the primitive-array baseline for the
hot path.

### 13.16. Optimistic Probe and Pessimistic Equality Law

Protocol-owned interning MAY use optimistic probing to minimize hot-path canonical byte loading.

Optimistic probing means that the intern table rejects non-equal candidates using cache-local metadata before reading
canonical byte payload.

It does not mean probabilistic equality.

A compliant implementation MUST preserve the following distinction:

``````text
optimistic probe:
    may reject candidates using HID, domain, version, length, inline verifier metadata,
    small-inline bytes, and candidate masks

pessimistic equality:
    may accept semantic equality only after exact canonical byte verification
    or after observing a scope-local verified canonical byte handle
``````

Digest probability, including BLAKE3-256 collision resistance, MUST NOT be used as a reason to skip exact verification
for semantic equality.

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

`MEASURED_MIXED` is never the default high-performance mode.

A high-performance mechanical profile SHOULD prefer `SEGREGATED_INLINE_TABLE` or `PRECLASSIFIED_TWO_PASS` unless the
target benchmark corpus proves that mixed branching is stable for the selected runtime profile.

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

### 13.18.1. Small-Inline Branch Entropy Containment Law

Small-inline acceleration MUST NOT introduce an unpredictable hot-loop branch as the ordinary path.

A mixed inline/external verifier is lawful only when the resolved physical policy selects `MEASURED_MIXED` before scope
admission and the release evidence proves that the selected workload does not create branch-thrashing.

The branch policy MUST be fixed before identity scope admission.

It MUST NOT be selected by:

- live branch-miss counters;
- current input frequency distribution;
- GC behavior;
- worker timing;
- lane timing;
- adaptive runtime profiling;
- or cache warmth observed inside the admitted scope.

If benchmark evidence later shows that mixed branching regresses the target profile, the compliant fallback is:

``````text
MEASURED_MIXED
-> disabled for that runtime profile
-> SEGREGATED_INLINE_TABLE or PRECLASSIFIED_TWO_PASS
``````

not:

``````text
hot loop
-> adapt per candidate
-> change verification shape based on observed frequency
``````

Changing the small-inline branch policy may change latency or throughput.

It MUST NOT change canonical bytes, HID derivation, collision verification, stable intern id assignment, or semantic
equality.

### 13.19. Verified Canonical Bytes Handle Law

After exact canonical byte verification succeeds inside a sealed interning scope, the implementation MAY issue a
verified canonical byte handle.

A verified canonical byte handle is a scope-local proof that the referenced canonical byte slice has already passed
exact verification.

Allowed fast path:

``````text
same sealed intern scope
same verified canonical byte handle
-> equality already proven inside that scope
``````

Forbidden:

``````text
raw ByteArray reference equality
staging buffer reference equality
backend object identity
cross-scope handle equality
``````

The handle is a proof artifact, not semantic material by itself.

It MUST NOT be persisted or compared outside its declared scope unless a future ADR defines a persistent proof format.

### 13.20. Vectorized Exact Comparison Law

Full canonical byte comparison MAY be implemented with vectorized, word-at-a-time, intrinsic, off-heap, or
runtime-specific comparison techniques.

Such techniques are physical optimizations only.

They MUST produce exactly the same result as byte-for-byte canonical comparison.

They MUST NOT introduce platform-dependent equality semantics.

The same canonical bytes must compare equal or unequal identically across:

- scalar comparison;
- vectorized comparison;
- word-at-a-time comparison;
- off-heap comparison;
- intrinsic comparison.

### 13.21. Collision Group Escalation Law

Intern tables MUST define a maximum hot collision group size.

If a HID collision group exceeds the configured hot bound, the implementation MUST NOT allow unbounded hot-path
verification.

A compliant implementation MUST choose one of:

- fail the current identity scope closed;
- fail the current frozen image or artifact publication closed;
- quarantine the current acquisition scope;
- quarantine the current worker lane for the current admitted scope;
- move the group into a bounded cold collision structure;
- apply an explicitly ratified stronger-width rekeying strategy selected before the scope becomes planning-visible.

Silent unbounded collision chains are forbidden.

Process-wide hard termination is not the ordinary escalation path for a bounded, diagnosable collision group.

The ordinary policy outcome is semantic non-publication, bounded quarantine, or a ratified cold path.

A collision group escalation path MUST NOT change stable intern id assignment for already verified material in the same
sealed scope.

### 13.21.1. Persistent Collision Pressure Containment Law

Collision escalation is not allowed to become an unbounded retry loop.

If the same identity scope, adapter source, domain/schema/version tuple, or acquisition boundary repeatedly triggers
collision escalation, the implementation MUST apply deterministic containment selected by resolved policy.

Allowed containment outcomes include:

- fail the current identity scope closed;
- quarantine the current acquisition scope;
- reject further candidates for the offending domain/schema/version tuple inside the admitted scope;
- mark the adapter source as protocol-incompatible for that scope;
- throttle or reject new publication attempts under bounded policy;
- move to a bounded cold collision structure where ratified;
- or fail the current artifact publication closed.

The implementation MUST NOT:

- repeatedly allocate exception objects on the hot path;
- emit unbounded logs for each colliding candidate;
- keep a worker lane permanently pinned in a failed state;
- retry the same collision group without a deterministic state transition;
- or allow collision diagnostics to exceed `maxDiagnosticEvidenceBytes`.

Collision escalation events SHOULD be represented as primitive fault classifications or bounded diagnostic records.

Repeated collision pressure is an availability fault.

It is not semantic inequality.

It MUST NOT change canonical bytes, HID derivation, collision verification semantics, stable intern id assignment, or
semantic equality for material that remains accepted.

### 13.21.2. Collision Escalation Lane Release Law

Quarantine of a worker lane or acquisition boundary is a scoped containment outcome.

It MUST release lane-local scratch, reader leases, staging slabs, and temporary collision descriptors according to
ADR-0042 lifecycle rules.

It MUST NOT permanently remove the physical worker or engine lane from future unrelated scopes unless a resolved runtime
policy explicitly opens a broader circuit for the remainder of the run.

A lane quarantine result MUST be represented as an explicit state.

It MUST NOT be represented by:

- leaked thread-local state;
- hidden worker flags;
- unbounded exception state;
- callback-local state;
- or a permanently poisoned primitive slot without an owner transition.

The lawful shape is:

``````text
collision hot bound exceeded
-> explicit escalation classification
-> scoped quarantine / fail-closed outcome
-> release transient resources
-> prevent publication of the offending identity material
``````

The forbidden shape is:

``````text
collision hot bound exceeded
-> lane spins / retries / logs indefinitely
-> temporary slabs remain pinned
-> publication never reaches a terminal state
``````

### 13.21.1. Bounded Cold Collision Structure Law

A bounded cold collision structure is optional.

If no ratified cold collision structure exists for the active domain/policy, collision group overflow MUST fail the
current identity scope, frozen image publication, or artifact publication closed.

If a bounded cold collision structure is ratified, it MUST satisfy:

- it remains non-semantic until exact verification succeeds;
- it stores only canonical byte offsets / lengths, verified canonical byte handles, or equivalent bounded verification
  payload references;
- it has a maximum cold group member count;
- it has a maximum cold canonical-byte total;
- it has a maximum probe / comparison budget;
- it preserves deterministic stable intern id assignment;
- it never accepts digest-only or HID-only equality;
- it emits structured diagnostics when its cold bound is exceeded;
- it cannot retroactively repair already-published semantic identity;
- it cannot change already assigned stable ids inside a sealed scope.

A stronger-width rekeying path, if ratified, MUST be selected before the scope becomes planning-visible and MUST
preserve
the same deterministic publication law.

### 13.21.2. Collision Escalation Availability Containment Law

Collision escalation is an availability boundary.

An attacker or pathological input may force a scope to spend its collision budget.

ADR-0041 therefore requires bounded containment.

A collision overflow MUST NOT ordinarily terminate the process.

A collision overflow MUST NOT poison unrelated identity domains, unrelated admitted scopes, unrelated worker lanes, or
already published immutable images.

A compliant implementation MUST contain collision overflow to the smallest lawful boundary selected by resolved policy:

- current identity unit;
- current identity scope;
- current frozen image publication;
- current artifact publication;
- current worker lane inside the admitted scope;
- or current acquisition scope.

The selected containment boundary MUST be deterministic under the resolved policy.

It MUST NOT depend on:

- current heap pressure;
- thread scheduling;
- worker completion order;
- live profiling;
- or random fallback.

Repeated collision overflow from the same external input source is outside ADR-0041 semantic identity, but adapters MAY
apply admission throttling, source quarantine, or request-level rejection before entering ADR-0041 identity sealing.

Such adapter-level throttling MUST NOT change canonical identity material for admitted scopes.

BLAKE3/HID collision resistance reduces ordinary collision probability, but ADR-0041 still treats collision overflow as
a
bounded diagnosable condition rather than as an impossible event.

### 13.22. Physical Acceleration Equivalence Law

Physical acceleration may change latency, throughput, cache locality, memory bandwidth behavior, and instruction mix.

It MUST NOT change semantic identity, canonical ordering, stable intern id assignment, query-key equality, collision
verification outcome, or publication legality.

Any accelerated implementation MUST be observationally equivalent to the reference deterministic identity pipeline:

``````text
canonical material
-> canonical bytes
-> HID / digest
-> collision verification
-> deterministic interning
-> stable scoped id
``````

SIMD probing, NUMA-local staging, prefetch-aware slab layout, branch-minimal decoding, vectorized byte comparison, and
query-key precomputation are physical implementations only.

### 13.23. SIMD-Compatible Group Probing Law

Protocol-owned intern tables SHOULD use SIMD-compatible group probing.

The table layout SHOULD separate compact control metadata from cold canonical payload material so that multiple
candidate slots can be rejected before pointer chasing.

A compliant implementation MAY use:

- Swiss-table-style control bytes;
- H2 fingerprints;
- SIMD masks;
- vectorized equality over HID words;
- grouped occupancy metadata;
- branch-minimal candidate mask extraction;
- array-of-struct-of-arrays group layout.

SIMD candidate matching is not semantic equality.

A candidate selected by SIMD probing MUST still pass the required verification ladder before equality is accepted.

SIMD width MUST NOT affect stable identity output.

### 13.24. Provisional Handle and Verified Publication Law

The implementation MAY issue provisional handles during speculative, streaming, NUMA-local, or incremental physical
work.

A provisional handle is not semantic identity.

A provisional handle MUST NOT be used as:

- stable intern id;
- planning-visible equality proof;
- L2 exact-match proof;
- persistent artifact identity;
- canonical IR publication authority;
- query result reuse authority.

Only verified handles issued after exact verification, or stable ids issued after sealed deterministic interning, may
cross into planning-visible or cache-visible state.

Background verification MAY prepare publication.

Background verification MUST NOT retroactively repair already-published semantic identity.

### 13.24.1. Provisional Handle Type-State Barrier

Provisional intern handles are physical seal-stage artifacts.

They are not semantic identity.

They MUST NOT cross into:

- `FrozenMetamodelImage`;
- planning-facing providers;
- `PlanCacheKey`;
- `CanonicalPlanNode`;
- persistent artifacts;
- public DTOs;
- query-result reuse keys.

The type-state split is normative.

The object shape is not normative.

A compliant implementation may expose cold type-state facades for testing, diagnostics, or narrow package-private seal
APIs, but the committed physical state MUST be primitive.

Required logical states:

| State       | Meaning                                                                         |    May cross publication boundary? |
|-------------|---------------------------------------------------------------------------------|-----------------------------------:|
| provisional | candidate exists before exact verification and deterministic seal               |                                 no |
| verified    | exact verification succeeded inside the current seal process                    |     only through a lawful seal API |
| stable      | deterministic dense id assigned after scope seal and table integrity validation | yes, only with scope/version proof |

Preferred hot physical state material:

``````text
handleStateBits
scopeId32
localCandidateId32 or localStableInternId32
identityDomainId32
verificationEpoch64
internerGeneration64
``````

The same material MAY be packed into primitive words or split across primitive arrays by the selected substrate backend.

Forbidden ordinary representation for committed hot state:

``````text
sealed interface InternHandle
-> ProvisionalInternHandle object
-> VerifiedInternHandle object
-> StableInternId object
``````

The forbidden shape may exist only as a cold facade and only if architecture tests prove that it cannot become committed
hot-path storage.

Provisional handle implementations SHOULD have:

- internal constructors;
- seal-package-local visibility;
- no serialization surface;
- no frozen-image DTO representation;
- no planning-domain API exposure;
- no L2 key participation.

Architecture tests MUST reject any planning-visible, cache-visible, frozen-visible, or persistent type that stores a
provisional handle.

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

### 13.25.1. Physical Locality Backend Isolation Law

NUMA-local, CPU-local, worker-local, lane-local, off-heap-local, or native-local placement is physical implementation
material.

It is not metadata identity material.

A physical backend MAY choose local arenas to reduce cache snooping, memory bandwidth contention, or allocator pressure.

The backend MUST expose only deterministic sealed identity material to ADR-0041 publication.

The core MUST NOT observe:

- physical memory address;
- NUMA node id;
- native allocation id;
- off-heap base pointer;
- worker-local buffer id;
- arena allocation order;
- or placement-dependent timing.

A v1 high-performance backend MAY use off-heap or `MemorySegment` arenas for local staging if it proves:

- explicit ownership;
- bounded lifetime;
- deterministic merge input;
- no staging-slab escape;
- no publication before seal;
- and cross-backend equivalence.

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

### 13.27. Query-Key and Incremental Engine Compatibility Law

Stable metadata identity MUST be suitable as a future query-key substrate.

A query key derived from ADR-0041 material MUST include:

- identity domain;
- query definition version;
- canonical input identities;
- dependency identity set fingerprint;
- canonical encoding version;
- relevant version bundle fingerprint.

Dependency sets MUST be canonicalized before encoding.

Dependency-set canonicalization MUST be independent from:

- backend order;
- acquisition order;
- worker assignment;
- callback completion order;
- NUMA arena id;
- frozen ordinal alone;
- object identity;
- wall-clock;
- random entropy.

Incremental invalidation may use compact fingerprints.

Query result reuse must remain collision-verified where semantic equality is required.

### 13.28. Resolved Physical Policy Fixity Law

Physical acceleration policies that can affect table layout, HID width, rekey strategy, small-inline threshold, probe
group shape, or NUMA staging topology MUST be resolved before the relevant identity scope is admitted.

They MUST remain fixed for that scope.

Forbidden:

``````text
runtime collision count
-> mutate HID width for already-admitted scope
-> change stable id assignment
``````

Allowed:

``````text
cardinality estimate / resource profile / backend capability
-> resolved metadata identity policy
-> fixed HID width and layout policy for that scope
``````

Adaptive optimization is allowed only for future scopes unless a later ADR proves an in-scope adaptation preserves exact
identity output and publication legality.

### 13.29. Layered Recursive Interning Law

Recursive interning is allowed only as layered reference interning.

A parent canonical identity payload MAY reference a child identity by a verified scoped stable intern id.

A parent MUST NOT recursively inline an unbounded child metadata closure.

A child intern id may be used in parent canonical bytes only after one of the following is true:

1. the child identity is already sealed; or
2. the child belongs to a deterministic SCC seal group governed by this ADR.

Allowed shape:

``````text
child canonical material
-> child canonical bytes
-> child HID
-> child collision verification
-> child stable intern id
-> parent canonical bytes reference child stable intern id
``````

Forbidden shape:

``````text
parent canonical bytes
-> recursively inline full child raw facts
-> recursively inline referenced child raw facts
-> traversal-order-dependent closure
``````

Reason:

Layered reference interning removes repeated structural material while preserving deterministic identity boundaries.

### 13.30. Metadata Dependency Graph and SCC Seal Law

Canonical metadata interning MUST treat recursive identity dependencies as a graph problem.

If the identity dependency graph is acyclic, candidates MAY be processed in canonical topological order.

If the graph contains cycles, the implementation MUST form deterministic strongly connected components.

A cyclic SCC MUST be sealed as one identity group using:

- deterministic member ordering;
- SCC-local temporary ordinals;
- canonical intra-SCC reference encoding;
- collision-verified SCC content identity;
- deterministic final stable id assignment.

Planning cycle truncation MUST NOT be used as the canonical metadata identity cycle breaker.

Boundary rule:

``````text
ADR-0030:
    planning traversal cycle handling

ADR-0041:
    metadata identity dependency graph SCC sealing
``````

These two mechanisms are both deterministic, but they belong to different semantic layers.

### 13.30.1. SCC Seal Atomicity and Failure Law

SCC sealing is atomic.

A strongly connected identity component MUST either seal completely or fail completely.

If any member of an SCC fails canonical encoding, exceeds a metadata identity cap, violates schema law, lacks required
verification material, or produces incompatible version material, the entire SCC seal MUST fail.

No stable intern id from a failed SCC may be published.

No parent component may observe a partially sealed child id.

No previously issued provisional handle from a failed SCC may cross the seal boundary.

A failed SCC may be represented only through an explicitly ratified diagnostic, unavailable, rejected, or failed
sentinel boundary.

Such sentinel material MUST NOT masquerade as the original semantic identity.

Forbidden:

``````text
SCC member A seals successfully
SCC member B exceeds cap
-> publish A stable id and reject only B
``````

Required:

``````text
SCC member A seals successfully as provisional material
SCC member B exceeds cap
-> fail the whole SCC seal
-> publish no stable intern id from that SCC
``````

### 13.30.2. SCC Capacity Law

Metadata identity sealing MUST bound cyclic identity groups.

A resolved metadata identity policy MUST define:

- maximum SCC member count;
- maximum SCC canonical byte total;
- maximum SCC intra-reference count;
- maximum SCC seal iteration count.

The v1 bootstrap defaults are:

``````text
maxSccMemberCount       = 256
maxSccCanonicalBytes    = 1 MiB
maxSccIntraReferences   = 4096
maxSccSealIterations    = 2
``````

If any SCC cap is exceeded, the SCC MUST fail closed before stable ids are published.

Silent partial publication is forbidden.

A fixed-point SCC identity algorithm, if later introduced, MUST consume `maxSccSealIterations` as a hard deterministic
bound.

For v1, deterministic SCC-local ordinal encoding is preferred over unbounded fixed-point iteration.

### 13.30.3. SCC Early Metering and Preflight Law

SCC seal atomicity does not require wasting the entire SCC budget before discovering a deterministic failure.

An implementation MUST fail an SCC as early as failure is deterministically provable.

Required safeguards:

- member-count metering while constructing the SCC candidate set;
- intra-reference-count metering while recording SCC-local references;
- per-member canonical byte fuse checks before SCC content identity sealing;
- aggregate SCC byte metering before stable id publication;
- version/schema compatibility checks before expensive full canonical-byte comparison;
- deterministic early-abort diagnostics when a preflight cap is exceeded.

Forbidden:

``````text
construct unbounded SCC
-> allocate all member payloads
-> derive all member HIDs
-> discover the first cap violation only at final publication
``````

Required direction:

``````text
construct SCC candidate under metered preflight
-> abort deterministically as soon as a member-count, edge-count, byte-count, or schema violation is provable
-> publish no stable id from the failed SCC
``````

Early abort MUST NOT change which valid SCCs seal successfully.

It only changes how quickly invalid SCCs are rejected.

### 13.30.4. SCC Two-Phase Sizing and Budget Reservation Law

A cyclic SCC SHOULD NOT perform expensive final materialization before deterministic sizing evidence exists.

For SCCs whose member count, reference count, projected canonical bytes, sort scratch demand, or encoded payload shape
can
approach resolved caps, a compliant implementation MUST use a two-phase seal plan or a documented equivalent.

Required two-phase shape:

``````text
Phase 1:
    deterministic member ordering
    field/schema compatibility preflight
    object/message nesting preflight
    reference-count preflight
    size-only canonicalization where applicable
    sort scratch estimate / reservation
    SCC byte budget reservation
    diagnostic budget reservation where required

Phase 2:
    canonical byte materialization
    HID / version-bundle fingerprint use
    collision verification
    stable intern id assignment
    atomic publication
``````

A size-only canonicalization pass computes deterministic encoded sizes and budget requirements without publishing:

- final canonical bytes;
- HIDs;
- interner candidates;
- stable intern ids;
- frozen table rows;
- planning-visible providers;
- or report manifest entries.

If exact size cannot be known without visiting a member payload, the implementation MUST meter that visit under the
preflight budget and MUST NOT publish member identity material before aggregate SCC reservation succeeds.

Budget reservation MUST cover:

- per-member canonical byte fuses;
- aggregate SCC canonical bytes;
- SCC intra-reference count;
- encoder / decoder frame count;
- canonical sort scratchpad demand;
- bounded cold exact sort demand where applicable;
- diagnostic evidence budget where failure reporting is required.

If reservation fails, the SCC MUST fail closed before expensive member publication, interner candidate publication,
stable id assignment, frozen image publication, planning visibility, or report manifest publication.

The reservation result is deterministic policy material.

It MUST NOT depend on:

- live heap availability;
- GC timing;
- thread scheduling;
- worker completion order;
- runtime profiling;
- or adaptive retry behavior.

A valid SCC that passes reservation and materialization MUST produce the same sealed identity as a single-pass exact
implementation.

The two-phase plan changes failure timing and resource usage only.

It MUST NOT change canonical bytes, HID derivation, collision verification, stable intern id assignment, or semantic
equality for accepted SCCs.

### 13.30.4.1. SCC Measure/Write Equivalence Law

A two-phase SCC seal plan is lawful only if its size-only pass and materialization pass are equivalent over the same
canonical encoder law.

The size-only pass MUST use the same:

- field order;
- tag order;
- wire type registry;
- endian rule;
- length-prefix rule;
- varint rule where ratified;
- string UTF-8 byte-count rule;
- object/message nesting rule;
- collection ordering law;
- SCC-local reference encoding law;
- and version-bundle compatibility decision

as the materialization pass.

The size-only pass MUST NOT estimate by:

- platform string length;
- UTF-16 code-unit count when UTF-8 byte count is required;
- backend text encoding;
- adapter serialization;
- object `toString()`;
- runtime display rendering;
- approximate varint length;
- cached stale length;
- or implementation-specific object size.

The materialization pass MUST verify that the final write cursor equals the preflight reserved byte count.

Required shape:

``````text
size-only canonicalization
-> exact byte count
-> reservation
-> materialization
-> final cursor == reserved end
-> publication may proceed
``````

Forbidden shape:

``````text
size estimate
-> reservation
-> materialization writes more or fewer bytes
-> patch offsets / resize / continue publication
``````

If the materialization pass writes a different number of bytes than the size-only pass predicted, the implementation
MUST treat this as a protocol implementation fault for the current SCC seal.

It MUST fail the SCC seal closed before:

- HID publication;
- interner candidate publication;
- stable intern id assignment;
- frozen image publication;
- planning visibility;
- report manifest publication;
- or persistent artifact publication.

The implementation MUST NOT repair the mismatch by:

- widening the slab in place;
- shifting later offsets;
- truncating bytes;
- padding unratified bytes;
- retrying with a different encoder;
- or accepting a backend-specific serialized form.

A released implementation using a two-phase SCC plan MUST provide golden vectors or tests for:

- exact measure/write equality;
- UTF-8 byte count parity;
- varint length parity where varint is ratified;
- nested message length parity;
- collection sort-key parity;
- SCC-local reference length parity;
- reservation overflow fail-closed;
- and cursor mismatch fail-closed.

### 13.31. Parent-References-Child-InternId Law

When a parent identity payload references a child by intern id, the reference MUST include enough scope and version
material to prevent accidental cross-scope aliasing.

At minimum, a child reference payload MUST be protected by:

- child identity domain;
- child intern scope;
- child interning protocol version;
- child stable intern id;
- child version-bundle fingerprint or a table-level proof that the parent and child share the same version bundle.

A parent MUST NOT reference:

- a provisional child handle;
- an unsealed child id;
- a backend-local child ordinal;
- a frozen ordinal from another image;
- an acquisition arena slot;
- a local arena insertion index;
- an SCC-local temporary ordinal after the SCC has been sealed.

SCC-local temporary ordinals are allowed only inside the canonical SCC seal procedure.

They MUST NOT escape as stable intern ids.

### 13.32. Canonical Topological Order Law

For acyclic metadata identity dependency graphs, processing order MUST be a canonical topological order.

When multiple nodes are simultaneously eligible, ties MUST be resolved by a ratified deterministic key such as:

- identity domain id;
- pre-identity local key;
- canonical type id;
- canonical byte prelude;
- or another domain-ratified canonical ordering input.

The following MUST NOT break ties:

- backend enumeration order;
- discovery order;
- callback completion order;
- thread id;
- object allocation order;
- hash table probe order;
- local arena append order.

A topological order is not accepted unless its tie-breaking rule is deterministic and golden-vector covered.

### 13.33. Canonical Compactness Boundary Law

Canonical byte stream compactness MUST be achieved by semantic redundancy elimination, not by run-local compression.

Accepted compactness mechanisms:

- schema-aware stripping;
- numeric field tags;
- fixed protocol ids;
- bit-packed hot metadata;
- sealed child intern id references;
- small canonical byte inlining;
- hot/cold slab separation;
- immutable canonical-base delta where ratified.

Rejected compactness mechanisms for hot identity:

- previous-item delta;
- adaptive dictionary compression;
- backend-order dictionary compression;
- frequency-learned run-local compression;
- thread-local compression state;
- compressor settings not fixed by protocol.

The encoder may reduce bytes.

It may not introduce hidden state.

---

## 14. Stable TypeReference Identity

### 14.1. TypeReference Role

`TypeReference` is a normalized fact, not Canonical IR.

However, it participates heavily in:

- frozen acquisition frontier membership;
- frozen type index lookup;
- cycle identity;
- raw fact references;
- active-member projection;
- polymorphic expansion;
- planning signatures;
- L2 key material.

Therefore TypeReference requires a stable metadata identity surface.

### 14.2. TypeReference Canonical Material

TypeReference canonical identity material must include:

- `CanonicalTypeId`;
- `TypeCycleKey`;
- `CanonicalTypeSignature`;
- type shape summary identity where required;
- type nesting depth;
- ratified type-use contract qualifier material derived from use-site annotations or equivalent contract surfaces;
- type identity coherence proof fingerprint;
- normalization version;
- type identity algorithm id/version;
- canonical text policy fingerprint where relevant.

The TypeReference identity surface MAY include only ratified type-use qualifier / contract fact material derived from
use-site annotations or equivalent contract surfaces. It MUST NOT include annotation descriptors or annotation backend
objects directly.

It must not include:

- `KType`;
- `KClass`;
- `KSType`;
- `KSDeclaration`;
- reflection string rendering;
- source location alone;
- classloader identity;
- backend registry id;
- acquisition slot id;
- frozen ordinal;
- backend enumeration order.

### 14.3. TypeReference HID

TypeReference HID is a compact descriptor over TypeReference canonical bytes.

Required surfaces:

``````text
TypeReferenceHid128
TypeReferenceHid256
TypeReferenceRoute64
``````

Rules:

- `TypeReferenceHid128` may be used for ordinary primitive membership grouping;
- `TypeReferenceHid256` may be used for stronger artifact summaries;
- `TypeReferenceRoute64` is routing-only;
- exact TypeReference canonical bytes or structural material must verify equality after HID match.

### 14.4. TypeReference Intern Id

A TypeReference stable intern id is assigned only by a protocol-owned interner.

The committed representation is primitive intern material, not a wrapper object.

A cold facade named `TypeReferenceInternId` MAY exist only as a boundary view over validated primitive material.

It may be used for:

- frozen frontier primitive membership;
- frozen table addressing after validated mapping;
- planning provider lookup acceleration;
- deterministic sort acceleration;
- direct-to-slab future lowering.

Hot consumers SHOULD use one of:

``````text
localStableInternId32
``````

when table-level proof supplies scope/domain/protocol material, or:

``````text
scopeId32
localStableInternId32
identityDomainId32
interningProtocolVersion64
``````

when the proof must be carried with the reference.

The wrapper/facade MUST NOT replace TypeReference semantic equality without table validation.

The wrapper/facade MUST NOT be stored in ordinary hot-path intern tables, frozen row arrays, planning hot loops, or L2
exact-match keys.

### 14.5. Cycle-Safe TypeReference Identity Stratification

Canonical metadata identity MUST be stratified to avoid recursive identity cycles.

A node-local identity MUST NOT recursively inline the full reachable metadata closure.

Required rule:

- `TypeReference` identity encodes TypeReference-local canonical material.
- raw fact record identity encodes referenced TypeReference identity material, not the target type's full raw facts.
- frozen table identity summarizes already-established row identities.
- frozen image content identity summarizes validated table identities after table coverage is complete.

This prevents cycle-containing metadata graphs from making identity depend on traversal order, backend completion order,
or recursive expansion timing.

Forbidden shape:

``````text
TypeReference(A)
-> inline all raw facts of A
-> inline TypeReference(B)
-> inline all raw facts of B
-> inline TypeReference(A)
-> recursion / truncation-order identity leak
``````

Accepted shape:

``````text
TypeReference(A)
-> local canonical TypeReference material only

RawFactRecord(A.memberB)
-> member-local material
-> referenced TypeReference(B) verified identity material

FrozenRawFactTable
-> sorted verified row identities

FrozenMetamodelImage
-> validated table identity summaries
``````

### 14.6. Cycle Boundary and Truncation Independence

Cycle truncation decisions belong to planning semantics.

TypeReference metadata identity MUST NOT depend on:

- planning active stack position;
- cycle breakpoint selection;
- deferred/substitutable edge choice;
- previously materialized child result;
- L2 cache state;
- joined-wait timing;
- backend traversal completion order.

A recursive metadata graph must be encoded by reference identity and table-level summaries, not by planning traversal
topology.

TypeReference identity is computed before planning traversal and before ADR-0030 cycle truncation.

It is metamodel-local identity, not planning-result identity.

Required layer order:

``````text
Layer 1: TypeReference-local identity
Layer 2: reference edge identity
Layer 3: raw fact record identity
Layer 4: frozen table identity
Layer 5: frozen image content identity
``````

No layer may recursively inline a later layer as part of its own identity.

### 14.7. TypeReference Dependency Encoding Boundary

`TypeReference` identity is metamodel-local identity.

It is computed before planning traversal and before ADR-0030 cycle truncation.

It MUST NOT encode:

- planning traversal topology;
- active stack state;
- cycle-truncated planning result;
- materialized child planning result;
- cache winner timing;
- backend completion timing.

Nested or referenced type material MUST be represented by ratified child TypeReference identity material, sealed child
intern ids, or SCC-governed identity references.

`TypeReference` identity MUST NOT recursively encode the complete reachable raw-fact closure.

This preserves a strict boundary:

``````text
metamodel identity graph
-> ADR-0041 SCC / intern seal
-> frozen image publication
-> planning traversal
-> ADR-0030 planning cycle handling
``````

---

## 15. Frozen Image Consumer Law

### 15.0. Identity Seal Before Frozen Publication

A frozen image may expose stable intern ids only after the ADR-0041 identity seal has completed.

Required order:

``````text
acquisition candidate staging
-> canonical identity encoding
-> HID derivation
-> version-bundle compatibility validation
-> collision verification
-> stable intern id assignment
-> frozen table coverage validation
-> FrozenMetamodelImage publication
``````

The frozen acquisition lifecycle remains governed by ADR-0040.

This ADR governs only the identity seal that must complete before planning-visible publication.

### 15.1. Frozen Ordinal vs Stable Intern Id

Frozen image may contain both:

``````text
frozenOrdinal
stableInternId
``````

They have different meanings.

`frozenOrdinal`:

- image-local table address;
- dense row index;
- used by providers.

`stableInternId`:

- protocol-owned identity compaction id;
- assigned under declared intern scope;
- may help primitive membership and sorting;
- may survive into persisted artifact only with scope/version metadata.

Rules:

- `frozenOrdinal` must not be treated as persistent identity;
- `stableInternId` must not be treated as table address unless the image declares a direct mapping;
- both must be validated before provider publication.

### 15.2. Frozen Type Index Evolution

Target evolution:

``````text
Before:
  TypeReference structural lookup
  comparator-heavy membership
  object-array transitional table

After:
  TypeReference HID / intern id pre-screen
  collision-verified canonical bytes
  dense frozen ordinal
  primitive membership
  ordinal-addressed frozen tables
``````

### 15.3. Frozen Image Content Identity

Frozen image content identity is not `FrozenMetamodelImageId`.

`FrozenMetamodelImageId` may remain diagnostic / compatibility material.

A future persistent frozen image content identity must be derived from:

- image schema version;
- sorted TypeReference canonical identities;
- frozen shape table canonical material;
- frozen cycle identity table canonical material;
- frozen raw fact table canonical material;
- explicit unavailable/rejected/failed sentinel material;
- table coverage proof;
- canonical encoding version;
- BLAKE3 suite id/version.

It must not include:

- backend source adapter provenance as semantic material;
- acquisition timing;
- backend session generation;
- callback completion order;
- memory addresses;
- object identity;
- runtime watchdog values.

### 15.4. Direct-to-Slab Compatibility

If a future backend supports direct ordinal or direct slab materialization, it still must satisfy this ADR.

Direct materialization cannot use:

- backend ordinal;
- manifest order;
- callback order;
- file order;
- object allocation order;

as final frozen ordinal authority unless that order has first been canonicalized and validated under this ADR.

---

## 16. Planning Consumer Law

Planning may consume stable metadata identity only through approved boundaries.

Allowed:

- `TypeReference`;
- verified TypeReference stable intern material, represented as primitive id plus table-level proof on hot paths;
- frozen ordinal from validated `FrozenTypeReferenceIndex`;
- `TypeCycleIdentity` with exact canonical signature verification;
- canonical active-member keys;
- local selector tuple canonical material;
- HID-derived deterministic entropy where explicit;
- PlanCacheKey full semantic tuple;
- route64 as routing-only material.

Forbidden:

- backend handles;
- backend-native identity;
- unverified HID equality;
- frozen acquisition frontier internals;
- runtime watchdog material;
- wall-clock time;
- source location as semantic identity;
- global mutable interner ids;
- acquisition-order ids.

Planning must preserve ADR-0037 fact-lazy ordering:

``````text
shape
-> cycle identity
-> active-cycle detection
-> raw facts only after cycle miss
``````

ADR-0041 does not allow TypeReference identity acceleration to pull raw facts earlier.

---

## 17. L2 Consumer Law

### 17.1. PlanCacheKey Domain Separation

`PlanCacheKey` belongs to the planning/L2 domain.

It is not the same domain as TypeReference identity.

Required separation:

``````text
TypeReference identity domain != PlanCacheKey equality domain
PlanCacheKey equality domain != route64 domain
route64 domain != CanonicalEdgeKey domain
CanonicalPlanNode signature domain != metadata identity domain
``````

### 17.2. route64

`route64` is deterministic routing material.

It may be derived from canonical semantic key material.

It remains non-authoritative.

Rules:

- route64 may select partition / shard / bucket / lane;
- route64 mismatch may reject a route candidate;
- route64 match must perform exact full-key verification;
- route64 must be version-bound;
- route64 must be deterministically remapped away from reserved sentinel values;
- route64 must not use process-random seeds;
- route64 must not use object identity;
- route64 must not use backend ordinal.

### 17.2.1. Planning L2 Route64 Migration Boundary

ADR-0041 does not silently replace the existing Planning L2 `route64` derivation.

Planning L2 `route64` is a PlanCacheKey-domain physical routing artifact.

It is separate from metadata HID.

A transitional implementation MAY keep a Murmur3-derived or otherwise non-cryptographic `route64` for Planning L2 if all
of the following remain true:

- the derivation is deterministic;
- the derivation is version-bound;
- reserved sentinel values are remapped deterministically;
- `route64` remains routing-only;
- exact PlanCacheKey verification follows every route candidate;
- and the route derivation is not reused as metadata identity, query fingerprint, persistent identity, or stable intern
  id
  assignment authority.

A future BLAKE3-derived PlanCacheKey route projection requires a separate PlanCacheKey canonical-material amendment,
route-derivation version, golden vectors, and migration boundary.

BLAKE3-derived `route64` would still be routing-only.

It would not become PlanCacheKey equality.

### 17.3. L2 Exact Match

L2 exact match remains governed by full semantic key verification.

Compact identity may accelerate.

It must not replace full-key comparison.

### 17.4. Cache-Blind Semantics

L2 cache warmness, hit/miss, join timing, partition drop, circuit-open, or dispatch timing must not alter:

- canonical topology;
- canonical signatures;
- semantic cost bound;
- cycle breakpoint choice;
- TypeReference identity;
- stable intern id assignment inside a published scope.

---

## 18. Persistent Artifact Boundary

This ADR defines identity material that may later support persistent frozen images and replay manifests.

It does not define the complete persistent binary format.

However, any future persistent artifact using this ADR must store:

- identity domain;
- canonical encoding version;
- domain schema version;
- BLAKE3 suite id/version;
- HID derivation version;
- intern scope;
- intern protocol version;
- collision verification material or a ratified equivalent;
- relevant semantic version tuple;
- golden-vector compatibility id where applicable.

A persistent artifact must not restore identity by trusting:

- stale process-global intern ids;
- backend ordinals;
- classloader identity;
- source location alone;
- object identity;
- JVM hashCode;
- frozen ordinal without image/schema proof.

---

## 19. Security and DoS Boundary

Stable metadata identity is a DoS boundary.

Attack surfaces include:

- enormous canonical strings;
- accidental recursive closure inside a single canonical unit;
- large future-ratified lowered-contract payloads;
- backend-divergent contract syntax lowering;
- recursive contract syntax / meta-syntax dependency graphs;
- effective-value defaulting drift between Reflection, KSP, compiler metadata, and DSL surfaces;
- deeply nested generic signatures;
- many colliding compact identities;
- oversized repeated fields;
- malicious backend producing repeated material;
- backend bug generating inconsistent canonical material;
- persisted artifact with malformed lengths;
- collision group amplification;
- comparator-amplification attacks during unordered collection canonicalization;
- offset-table integer overflow / wraparound;
- malformed payload slices that point into hot headers or field tables;
- and repeated expensive late text rejection after avoidable malformed-text evidence.

Required defenses:

- byte-length caps before allocation;
- checked offset and length arithmetic before slice exposure;
- zero-copy bounded slice views on hot identity paths;
- canonical collection sort-key budgeting;
- early backend-erased text ratification and surrogate preflight;
- per-unit identity fuses;
- aggregate canonical byte budgets per acquisition/image scope;
- field count caps;
- repeated field caps;
- collision group size caps;
- fail-closed overflow checks;
- fail-closed as semantic non-publication rather than ordinary process-wide termination;
- bounded collision quarantine or bounded cold collision paths where ratified;
- SCC preflight metering and deterministic early abort;
- `Long` arithmetic for byte totals;
- no silent integer wraparound;
- diagnostic budget;
- bounded sanitizer for diagnostic payloads;
- golden vector tests for size boundaries.

The bootstrap caps in Section 7.5 are part of this defense.

A collision group exceeding policy cap must fail closed or degrade through an explicitly ratified non-semantic path.

It must not silently accept digest equality.

A canonical-byte payload exceeding its resolved per-unit domain cap must fail closed before allocation or before
publication into a hot identity table.

An acquisition/image scope exceeding its aggregate canonical-byte budget must fail closed before publication, but it
MUST
NOT justify inflating per-unit identity fuses unless a separate policy ratifies that override.

---

## 20. Implementation Target Types

The following names are illustrative target surfaces.

Exact package placement may change.

### 20.1. Encoding

``````text
metamodel.domain.identity.CanonicalEnvelopeHeader
metamodel.domain.identity.EngineLaneEpochTable
metamodel.domain.identity.WorkerLaneTopology
metamodel.domain.identity.LaneExecutionLease
metamodel.domain.identity.AsyncOwnerEvent
metamodel.domain.identity.EngineMaintenanceQueue
metamodel.domain.identity.CanonicalEnvelopeHeaderValidator
metamodel.domain.identity.CanonicalByteEncoder
metamodel.domain.identity.CanonicalByteSink
metamodel.domain.identity.CanonicalEncodingVersion
metamodel.domain.identity.CanonicalEncodingDomain
metamodel.domain.identity.CanonicalFieldTag
metamodel.domain.identity.CanonicalWireType
metamodel.domain.identity.CanonicalFieldTableEntry
metamodel.domain.identity.CanonicalObjectRecordEncoder
metamodel.domain.identity.CanonicalObjectRecordDecoder
metamodel.domain.identity.CanonicalObjectSchema
metamodel.domain.identity.CanonicalObjectFieldSpec
metamodel.domain.identity.CanonicalObjectReference
metamodel.domain.identity.CanonicalObjectFrameStack
metamodel.domain.identity.CanonicalEncodedBytes
metamodel.domain.identity.VersionBundleFingerprintDeriver
metamodel.domain.identity.DigestDomainSeparationPayload
metamodel.domain.identity.MetadataIdentityDigestSuite
metamodel.domain.identity.DigestSuiteId
metamodel.domain.identity.DigestSuiteGoldenVectorSuite
metamodel.domain.identity.HidVerificationPath
metamodel.domain.identity.SealedProjectionTuple
metamodel.domain.identity.DigestSuiteMigrationPolicy
metamodel.domain.identity.CanonicalDecodeFrame
metamodel.domain.identity.CanonicalDecoderFrameStack
metamodel.domain.identity.CanonicalSortKey
metamodel.domain.identity.CanonicalSortKeyBudget
metamodel.domain.identity.CanonicalSortScratchpad
metamodel.domain.identity.CanonicalSortScratchpadBudget
metamodel.domain.identity.CanonicalTieGroupRange
metamodel.domain.identity.CanonicalTieGroupScratchpad
metamodel.domain.identity.CanonicalSortProjectionLevel
metamodel.domain.identity.ExactCloneGroupDetector
metamodel.domain.identity.BoundedColdExactSortPath
metamodel.domain.identity.BoundedColdExactSortBudget
metamodel.domain.identity.MapDuplicateKeyDetector
metamodel.domain.identity.CanonicalByteSliceView
metamodel.domain.identity.DecoderCursorProgressValidator
metamodel.domain.identity.DecoderTagBoundsValidator
metamodel.domain.identity.DenseDecoderTableSpec
metamodel.domain.identity.BranchBoundedUtf8Validator
metamodel.domain.identity.CanonicalTextValidationOracle
metamodel.domain.identity.CanonicalSliceLinearOrderValidator
metamodel.domain.identity.PayloadRelativeOffset
metamodel.domain.identity.BranchBoundedBoundsValidator
metamodel.domain.identity.CanonicalHeaderFlags
metamodel.domain.identity.FixedPointCapacityArithmetic
metamodel.domain.identity.ShortInlineFieldPayloadPolicy
metamodel.domain.identity.ShortInlineFieldPayloadMode
metamodel.domain.identity.SccSealPreflightPlanner
metamodel.domain.identity.SccSizeOnlyCanonicalizer
metamodel.domain.identity.SccBudgetReservation
metamodel.domain.identity.SmallInlineVerificationMode
metamodel.domain.identity.SmallInlineBenchmarkGate
metamodel.domain.identity.CacheLineAlignmentEvidence
metamodel.domain.identity.SealedSlabEpoch
metamodel.domain.identity.SealedSlabHandle
metamodel.domain.identity.CollisionContainmentBoundary
metamodel.domain.identity.SealedCanonicalByteSlab
metamodel.domain.identity.CanonicalByteSlice
metamodel.domain.identity.CanonicalPayloadSlice
metamodel.domain.identity.CanonicalOffsetTableValidator
metamodel.domain.identity.CanonicalSortKey
metamodel.domain.identity.CanonicalCollectionCanonicalizer
metamodel.domain.identity.SccLocalReferenceWireType
``````

### 20.1A. Compact Encoding and Dependency Graph Surfaces

``````text
metamodel.domain.identity.StatelessCanonicalEncoder
metamodel.domain.identity.CanonicalBaseRegistry
metamodel.domain.identity.CanonicalBaseDeltaEncoder
metamodel.domain.identity.BitPackedIdentityFieldSpec
metamodel.domain.identity.MetadataIdentityDependencyGraph
metamodel.domain.identity.MetadataIdentityScc
metamodel.domain.identity.MetadataIdentitySccSealer
metamodel.domain.identity.LayeredInternReference
``````

### 20.1B. Contract Syntax Lowering Surfaces

Reserved integration point.

ADR-0041 does not ratify concrete contract syntax lowering APIs.

Future surfaces may include contract syntax lowerers, lowered contract material, backend parity vectors, effective-value
models, dependency graphs, and diagnostics, but their concrete names and fields belong to the top-level contract
document
first.

This ADR only reserves the identity-substrate integration point for future ratified contract material.

Reserved placeholders:

``````text
contract.frontend.syntax surface
contract.semantic lowered material
contract.semantic version bundle
contract.semantic collision verification payload
contract.semantic identity domain activation gate
``````

Once ratified elsewhere, those surfaces must obey ADR-0041's canonical byte encoding, BLAKE3/HID, collision
verification,
protocol-owned interning, deterministic publication, and physical optimization laws.

### 20.2. BLAKE3 / HID

``````text
metamodel.domain.identity.MetadataHashAlgorithm
metamodel.domain.identity.Blake3MetadataHasher
metamodel.domain.identity.Hid64
metamodel.domain.identity.Hid128
metamodel.domain.identity.Hid256
metamodel.domain.identity.HidDomain
metamodel.domain.identity.HidDeriver
metamodel.domain.identity.HidDerivationVersion
metamodel.domain.identity.SealTimeIdentityProjection
metamodel.domain.identity.Route64Projection
metamodel.domain.identity.NonSemanticPhysicalHashHint
``````

### 20.3. TypeReference Identity

``````text
metamodel.domain.identity.TypeReferenceCanonicalMaterial
metamodel.domain.identity.TypeReferenceCanonicalEncoder
metamodel.domain.identity.TypeReferenceIdentityDomain
metamodel.domain.identity.TypeReferenceHid
metamodel.domain.identity.TypeReferenceInternFacade
metamodel.domain.identity.TypeReferenceInternPrimitiveProjection
``````

### 20.4. Collision Verification

``````text
metamodel.domain.identity.CollisionVerificationPayload
metamodel.domain.identity.CanonicalBytesVerifier
metamodel.domain.identity.CanonicalStructuralVerifier
metamodel.domain.identity.IdentityCollisionRecord
``````

### 20.5. Protocol-Owned Interning

``````text
metamodel.domain.identity.ProtocolOwnedInterner
metamodel.domain.identity.InternScope
metamodel.domain.identity.ProvisionalInternFacade
metamodel.domain.identity.VerifiedInternFacade
metamodel.domain.identity.StableInternFacade
metamodel.domain.identity.InternHandlePrimitiveProjection
metamodel.domain.identity.DeterministicInternIdAssigner
metamodel.domain.identity.InternedIdentityTable
metamodel.domain.identity.InternVerificationEpoch
metamodel.domain.identity.SccSealGroup
metamodel.domain.identity.SccSealFailure
metamodel.domain.identity.SccSealPreflightMeter
metamodel.domain.identity.SccEarlyAbortDiagnostic
``````

### 20.6. Frozen Integration

``````text
metamodel.domain.frozen.FrozenTypeReferenceIdentityTable
metamodel.domain.frozen.FrozenInternedTypeReferenceIndex
metamodel.domain.frozen.FrozenIdentityIntegrityValidator
``````

### 20.7. Mechanical Sympathy Surfaces

``````text
metamodel.domain.identity.CanonicalHotHeader
metamodel.domain.identity.CanonicalFieldOffsetTable
metamodel.domain.identity.CanonicalDecoderTable
metamodel.domain.identity.CanonicalTagDispatchTable
metamodel.domain.identity.IdentityEnvelopeLayout
metamodel.domain.identity.IdentityHeaderLayoutSpec
metamodel.domain.identity.ReservedPaddingSpec
metamodel.domain.identity.InlineVerifierPrefix
metamodel.domain.identity.InternProbeGroup
metamodel.domain.identity.InternTableLayout
metamodel.domain.identity.InternProbeBudget
metamodel.domain.identity.BoundedColdCollisionStructure
metamodel.domain.identity.ColdCollisionBudget
``````

These names are illustrative.
The architectural requirement is that decoder and interner physical layout become explicit design surfaces rather than
incidental implementation details.

### 20.8. Version and Policy Surfaces

``````text
metamodel.domain.identity.CanonicalIdentityVersionBundle
metamodel.domain.identity.IdentityVersionDependencyGraph
metamodel.domain.identity.IdentityCompatibilityMatrix
metamodel.domain.identity.IdentityCompatibilityClass
metamodel.domain.identity.ResolvedMetadataIdentityPolicy
metamodel.domain.identity.MetadataIdentityCapacitySolver
metamodel.domain.identity.MetadataIdentityCapacityFeasibilityValidator
metamodel.domain.identity.MetadataIdentityCapacityRelationshipSpec
metamodel.domain.identity.MetadataIdentityBootstrapCaps
metamodel.domain.identity.MetadataIdentityTargetAverageSizer
metamodel.domain.identity.MetadataIdentityScopeBudgetEquation
``````

These surfaces are required to prevent version-axis drift, unbounded identity verification cost, arbitrary cap editing,
and impossible count/byte/table budget combinations.

---

## 21. Migration Strategy

### Phase 0 — Version Bundle and Metadata Identity Policy

Introduce the version and budget surfaces before identity material is used by hot consumers.

Deliverables:

- `CanonicalIdentityVersionBundle`;
- identity version dependency graph;
- identity compatibility matrix;
- `ResolvedMetadataIdentityPolicy`;
- `MetadataIdentityCapacitySolver`;
- metadata identity capacity feasibility relationships;
- v1 bootstrap cap table;
- golden vectors for version-fingerprint changes.

### Phase 1 — Canonical Encoding Foundation

Introduce canonical byte encoding infrastructure.

Deliverables:

- canonical byte writer;
- tagged field encoding;
- UTF-8 length-prefix string encoding;
- enum protocol id encoding;
- integer encoding;
- collection encoding;
- golden vectors.

No consumer may use this path for semantic equality until golden vectors exist.

### Phase 2 — BLAKE3 / HID Foundation

Introduce BLAKE3 metadata hasher and HID descriptors.

Deliverables:

- BLAKE3 suite id/version;
- HID64/HID128/HID256;
- domain separation;
- keyed derivation;
- width tests;
- golden vectors.

### Phase 3 — TypeReference Canonical Bytes

Introduce TypeReference canonical material and encoder.

Deliverables:

- TypeReference canonical material extraction;
- TypeReference canonical bytes;
- TypeReference HID;
- TypeReference collision verification payload;
- TypeReference identity golden vectors.

### Phase 3A — Contract Syntax Lowering Foundation

Reserved integration phase.

ADR-0041 does not introduce concrete contract syntax lowering in this phase.

This phase remains in the migration plan as the future place where ratified contract material will be connected to the
stable metadata identity substrate.

Before this phase may become active, `docs/the-most-important-thing/interface-as-contract.md` must ratify:

- the contract fact vocabulary;
- the frontend syntax boundary;
- the lowering law;
- the version bundle;
- the canonical byte fields;
- the collision verification payload;
- the allowed consumers;
- and any effective/default value model.

Until then, the only active rule is:

``````text
frontend syntax is not canonical identity material
``````

A future implementation may fill this phase after the contract model is closed.

### Phase 4 — Protocol-Owned Interner

Introduce deterministic intern id assignment.

Initial implementation may be batch-based.

Required algorithm:

``````text
canonical material
-> canonical bytes
-> HID
-> collision group
-> canonical bytes sort
-> dense id assignment
``````

### Phase 4A — Identity Seal Boundary

Introduce the frozen-publication identity seal.

Deliverables:

- acquisition candidate staging boundary;
- canonical identity encoding before frozen publication;
- HID derivation before frozen publication;
- version-bundle validation before frozen publication;
- collision verification before frozen publication;
- stable intern id assignment before frozen publication;
- table integrity validation before frozen publication.

### Phase 4B — Mechanical Encoding and Decoder Hardening

Introduce the physical encoding discipline required by this ADR.

Deliverables:

- fixed-width identity hot header;
- explicit variable-payload offset table;
- dense numeric field tags;
- generated or table-driven decoder dispatch;
- branch-minimal successful decode path;
- fail-closed decoder validation paths;
- no map/string/reflection dispatch on the hot path;
- explicit decode-frame stack or bounded recursion proof for `WIRE_TYPE_MESSAGE`;
- decoder microbenchmarks and allocation tests.

### Phase 4C — Cache-Line-Aware Intern Table Layout

Introduce an intern-table physical layout that defers pointer chasing.

Deliverables:

- first-probe metadata group;
- inline verifier prefix;
- domain/version/length inline checks;
- primitive-friendly probe arrays;
- bounded probe budget;
- full canonical byte verification only after inline checks pass;
- microbenchmarks for HID hit/miss, prefix rejection, and full-verify rare path.

### Phase 4D — Physical Acceleration Compatibility

Introduce physical acceleration only after the reference deterministic path is golden-vector stable.

Deliverables:

- SIMD-compatible group probing layout;
- provisional handle boundary;
- verified publication gate;
- NUMA-local staging proof;
- deterministic merge proof;
- prefetch-aware hot / cold slab split;
- query-key compatibility tests;
- physical acceleration equivalence tests against the reference identity pipeline.

No physical acceleration introduced in this phase may publish semantic identity before verification.

### Phase 4E — Compact Canonical Encoding and Layered Interning Compatibility

Introduce compactness laws without changing identity semantics.

Deliverables:

- schema-aware stripping checks;
- observation-independent encoder checks;
- bit-packed field golden vectors where bit packing is used;
- layered child intern reference support;
- metadata dependency graph extraction;
- deterministic topological order for acyclic identity graphs;
- deterministic SCC seal path for cyclic identity graphs;
- explicit ban on planning cycle truncation as metadata identity cycle breaker.

Canonical-base delta remains optional and may be deferred to a later implementation phase.

### Phase 5 — Frozen Consumer Integration

Evolve frozen frontier and type index toward:

``````text
HID pre-screen
-> collision verification
-> stable intern id
-> frozen ordinal
-> primitive lookup
``````

### Phase 6 — Planning Consumer Integration

Planning consumes verified ids and ordinals only through frozen/provider boundaries.

No planning code may branch on unverified HID equality.

### Phase 7 — L2 Consumer Integration

PlanCacheKey derivation may consume ADR-0041 canonical material, but must keep:

``````text
full semantic equality key
route64
digest/HID
``````

as separate surfaces.

### Phase 8 — Persistent Artifact Preparation

Introduce frozen image content summary roots only after canonical encoding and collision verification are golden-vector
stable.

---

## 22. Compliance Rules

A compliant implementation MUST satisfy:

1. No JVM `hashCode()` is used as protocol identity.
2. No backend-native identity is used as semantic identity.
3. No backend ordinal is used as stable intern id.
4. No frozen ordinal is used as persistent identity.
5. No acquisition order assigns stable identity.
6. No callback completion order assigns stable identity.
7. No thread race assigns stable identity.
8. No delimiter-joined string is used as canonical bytes.
9. No JSON serialization is used as canonical bytes unless separately ratified.
10. No platform default charset is used.
11. Canonical strings are encoded as UTF-8 bytes with explicit byte length.
12. Canonical byte encoder performs no Unicode normalization or repair.
13. Unknown/unavailable metadata is encoded explicitly.
14. Enum identity uses protocol ids, not enum ordinal.
15. Field tags are stable protocol ids.
16. BLAKE3 invocations are domain-separated.
17. HID width is explicit.
18. HID match is never semantic equality by itself.
19. Collision verification is mandatory after compact identity match.
20. route64 remains routing-only.
21. PlanCacheKey full semantic tuple remains equality authority.
22. Protocol-owned interner declares scope.
23. Stable intern id assignment is deterministic and order-independent.
24. Partial intern tables are not published.
25. Frozen providers consume validated frozen image/index structures only.
26. Planning consumes verified identity surfaces only through approved boundaries.
27. L2 exact match does not depend on compact identity alone.
28. Persistent artifacts store version/domain/scope metadata with ids.
29. Golden vectors exist for every released identity domain.
30. Architecture tests block backend handle leakage into identity material.

31. Canonical identity envelopes use `CanonicalEnvelopeHeader` as the mandatory 64-byte little-endian common header.
32. `magic32` is `0x4B4E5443`, `headerSize16` is `64`, and `payloadLength32` is mandatory.
33. `headerFlags16` is zero in v1 unless ratified by compatibility matrix, and `reserved16` / `reserved32` are zero.
34. `VersionBundleFingerprint128` is derived from canonical version-bundle bytes by domain-separated BLAKE3 derivation.
35. Hot decoder dispatch is table-driven or switch-table-friendly.
36. Hot decoder dispatch does not use reflection, string lookup, or map lookup.
37. Variable payloads are reached through offset / length material.
38. Identity hot headers avoid varint decoding unless separately ratified.
39. Intern-table first-probe metadata is physically grouped and primitive-friendly.
40. Intern-table lookup performs domain/version/length/prefix rejection before full byte comparison.
41. Full canonical byte comparison is a rare-path verification step, not the first response to every HID match.
42. Inline verifier prefixes are not equality authority.
43. TypeReference identity does not recursively inline the full reachable metadata closure.
44. Stable intern id assignment is invariant under physical table rebuild, resize, and probe-strategy change.
45. Every identity domain has a resolved version bundle.
46. Every identity domain has a compatibility matrix.
47. Canonical-byte payloads are capped by resolved metadata identity policy.
48. Protocol-owned intern membership uses `HID128` or wider by default.
49. `HID64` remains routing-only unless a domain-specific cardinality proof exists.
50. Hot-path `HID256` is represented as primitive words, not `ByteArray`.
51. Stable dense intern ids are not published before the interning scope is sealed.
52. Streaming implementations may publish provisional handles only, not final stable intern ids.
53. `GLOBAL_PROTOCOL_TABLE` is immutable protocol material only, not dynamic user metadata interning.
54. Frozen image publication waits for the ADR-0041 identity seal.
55. TypeReference identity is computed before planning traversal and before ADR-0030 cycle truncation.
56. Future JVM value-type usage must beat or match the primitive-array baseline before replacing it.
57. Determinism has priority over every physical optimization.
58. SIMD width does not affect stable identity output.
59. NUMA topology does not affect stable identity output.
60. Provisional handles never cross into planning-visible, cache-visible, or persistent identity state.
61. Speculative physical work does not publish semantic identity before verification.
62. Background verification does not retroactively repair already-published semantic identity.
63. Physical table probe order does not affect stable intern id assignment.
64. Local arena insertion order does not affect canonical ordering or stable intern id assignment.
65. Query-key derivation canonicalizes dependency sets before encoding.
66. Query keys do not depend on backend order, acquisition order, worker assignment, NUMA arena id, frozen ordinal
    alone, wall-clock, or object identity.
67. Physical acceleration policies are resolved before scope admission and remain fixed for the scope.
68. Prefetch-aware physical layout follows deterministic order; it does not create deterministic order.
69. Optimistic probing never becomes probabilistic equality.
70. Canonical identity encoding does not encode semantic field names as string payload.
71. Fixed positional physical layouts are used only after schema/version/domain validation.
72. Canonical identity encoding is observation-independent with respect to previous item, recently seen item, and
    acquisition order.
73. Run-local adaptive compression dictionaries are not used for hot identity bytes.
74. Bit-packed identity fields define offset, width, signedness, endian rule, reserved values, invalid values, and
    golden vectors.
75. Reserved bits are zero on encode and fail closed on decode unless compatibility explicitly ratifies them.
76. Canonical-base delta uses only immutable protocol-owned bases.
77. Previous-item delta is forbidden for hot identity bytes.
78. General-purpose compression is not part of hot semantic identity equality.
79. Recursive interning is layered reference interning, not unbounded recursive closure inlining.
80. Parent canonical bytes reference only sealed child intern ids or SCC-governed temporary references.
81. Metadata identity cycles are handled by deterministic SCC sealing, not ADR-0030 planning truncation.
82. SCC-local temporary ordinals do not escape as stable intern ids.
83. Acyclic metadata dependency graphs use canonical topological order with deterministic tie-breaking.
84. Child intern id references carry scope/version protection or table-level proof.
85. TypeReference identity does not encode planning traversal topology, active stack state, or cycle-truncated planning
    results.
86. SCC sealing is atomic: no stable intern id from a failed SCC may be published.
87. SCC member-count, byte-total, intra-reference, and seal-iteration caps are resolved before SCC sealing.
88. SCC cap violations fail closed before stable id publication.
89. Failed SCC sentinel material does not masquerade as the original semantic identity.
90. `HID256` hot lookup requires a domain-specific probe layout or a `HID128` hot projection.
91. `FROZEN_IMAGE_CONTENT_SUMMARY` is cold / publication-time identity by default, not ordinary hot membership identity.
92. Provisional handle types are distinct from verified handles and stable intern ids.
93. Provisional handle implementations are not exposed through frozen image, planning, L2, persistent, public DTO, or
    query-key surfaces.
94. Future JVM value-type / inline-object layouts must pass release-specific adoption proof against the primitive-array
    baseline.
95. Metadata identity cap violations emit structured, budgeted diagnostics.
96. `AUTO` resolves to the deterministic `STANDARD` bootstrap cap set in v1.
97. Future `AUTO` solvers may scale aggregate budgets only through deterministic pre-admission policy resolution.
98. `AUTO` does not live-adapt caps inside an admitted identity scope.
99. `AUTO` does not change canonical meaning, canonical bytes, HID derivation, collision verification, stable intern id
    assignment, query-key equality, or PlanCacheKey equality.
100. Ordinary resource profiles scale aggregate budgets and counts before they scale per-unit identity fuses.
101. `maxCanonicalBytesPerTypeReference` remains a TypeReference-local identity fuse and must not be used to admit
     raw-fact,
     lowered-contract, diagnostic, or graph-closure payloads.
102. Per-unit fuse changes are not user configuration knobs and require a versioned protocol-ratified domain exception,
     golden vectors, diagnostics, and benchmark evidence.
103. Annotation descriptors, annotation backend handles, annotation DTO shapes, and annotation source spellings are not
     canonical identity material.
104. `LOWERED_CONTRACT_FACT_IDENTITY` is a reserved identity domain until the top-level contract document ratifies the
     contract fact vocabulary and lowering law.
105. ADR-0041 does not define annotation semantics, DSL semantics, compiler-metadata semantics, generated-index
     semantics,
     default/effective value semantics, or lowered-contract-fact bit layout.
106. Future ratified lowered contract material must obey ADR-0041 canonical byte encoding, HID derivation, collision
     verification, protocol-owned interning, deterministic publication, and physical optimization laws.
107. No frontend syntax object may become canonical identity material merely because a future section title is reserved
     for it.
108. Contract syntax SCC sealing remains reserved until the top-level contract document ratifies the contract fact
     dependency
     model; once ratified, it must be deterministic, bounded, and atomic.
109. Contract syntax cap violations fail closed before stable lowered-contract-fact identity publication once the
     lowered
     contract-fact identity domain becomes active.
110. Bit-packed lowered-contract-fact hot layouts are reserved until the contract fact taxonomy, value model, target
     model,
     defaulting law, and compatibility matrix are ratified.
111. ADR-0041 does not expose arbitrary user-authored numeric cap editing as an ordinary configuration surface.
112. Concrete numeric metadata identity caps are resolved policy outputs produced by a deterministic capacity solver or
     an
     equivalent released bootstrap table.
113. Count caps, aggregate byte budgets, table budgets, and per-unit identity fuses must satisfy explicit feasibility
     relationships before scope admission.
114. `maxTotalTypeReferenceCanonicalBytes` must be feasible with `maxTypeReferenceCount`,
     `minimumEncodedBytesPerTypeReference`, and `maxCanonicalBytesPerTypeReference`.
115. `maxTotalRawFactRecordCanonicalBytes` must be feasible with `maxRawFactRecordCount`,
     `minimumEncodedBytesPerRawFactRecord`, and `maxCanonicalBytesPerRawFactRecord`.
116. `maxTotalInternCandidateCanonicalBytes` must be feasible with `maxInternCandidateCount`,
     `minimumEncodedBytesPerInternCandidate`, and `maxCanonicalBytesPerInternCandidate`.
117. The ADR-0041 v1 shared metadata interner candidate count must cover exactly the active v1 metadata domains:
     TypeReference, TypeCycleKey, TypeCycleIdentityPrecheck, TypeShapeSummary, RawFactRecord, ActiveMemberKey,
     LocalSelectorTuple, and RuntimeBindingSnapshot.
118. `maxTraversalEdges` must be derived from admitted graph dimensions or explicit domain-split edge budgets.
119. `maxFrozenTableBytes` must be derived from row counts, table layouts, load factors, coverage bitsets, slab offset
     tables, and headroom.
120. A resolved metadata identity scope budget must account for domain canonical-byte budgets, table budgets, frontier
     budget, dependency-graph budget, intern-table budget, diagnostic evidence budget, and fixed overhead.
121. A resolved policy that violates its own feasibility relationships is invalid and must fail before scope admission.
122. HID width selection must satisfy `n(n - 1) / 2^(b + 1) <= p_target` or choose a wider ratified width.
123. BLAKE3 remains the protocol identity derivation root for metadata identity surfaces.
124. Hot route/probe paths must use seal-time deterministic projections rather than repeatedly rehashing full canonical
     material.
125. Non-cryptographic hashes are permitted only as non-semantic physical hints over already-ratified or
     already-verified
     material.
126. Existing Planning L2 `route64` derivation is not silently replaced by ADR-0041; any migration to BLAKE3-derived
     route projection requires versioning, golden vectors, and exact PlanCacheKey verification.
127. Fail-closed collision handling means semantic non-publication, scope rejection, quarantine, or a ratified bounded
     cold path; it does not require ordinary process-wide hard termination.
128. SCC seal implementations must meter member count, intra-reference count, byte totals, and schema/version
     compatibility
     early enough to abort deterministically before stable id publication.
129. JVM heap primitive arrays are the portable v1 layout baseline; exact 64-byte physical alignment claims require
     separate evidence and are not assumed for ordinary heap objects.
130. Aggregate byte budgets should be tightened by deterministic target-average sizing relationships, not merely by the
     worst-case product of count and per-unit fuse.
131. Unordered collection canonicalization uses bounded canonical sort keys and metered full-byte tie-breaks rather than
     unbounded recursive comparator traversal.
132. Offset and length arithmetic for canonical envelope field tables uses checked `Long` arithmetic before narrowing.
133. Decoders reject negative offsets, negative lengths, overflowed `offset + length`, out-of-payload slices, and slices
     that point into hot headers, field tables, or reserved padding.
134. Hot identity decoders expose variable payloads as bounded canonical byte slices and do not allocate per-field
     `ByteArray` copies.
135. Every released identity envelope layout declares header size, alignment, reserved padding, field-table start, and
     payload start rules.
136. Reserved padding bytes encode as zero and fail closed when non-zero unless compatibility explicitly ratifies them.
137. Malformed text and unpaired surrogates are rejected at the earliest backend-erased text ratification boundary where
     practical; encoder rejection remains a defensive guard.
138. `WIRE_TYPE_SCC_LOCAL_REF` is valid only inside SCC seal payloads and cannot escape as a stable intern id, frozen
     ordinal, planning id, provisional handle, or public identity surface.
139. Unknown canonical identity tags fail closed by default.
140. Unknown-tag skip is allowed only when a domain compatibility matrix ratifies a bounded, non-critical,
     non-identity-affecting field and golden vectors cover the skip behavior.
141. Tag bit partitioning remains allowed, but released hot decoders should publish benchmark evidence for mask/shift,
     dispatch, and validation-branch behavior.

---

142. `CanonicalEnvelopeHeader` is the protocol-facing header name; the active layout is selected by
     `canonicalEncodingVersion32`.
143. For the current layout, `canonicalEncodingVersion32` is `1`.
144. Message decoding is bounded by explicit structural depth and decoder-frame caps.
145. `WIRE_TYPE_MESSAGE` decoding does not rely on JVM call-stack depth as the nesting bound.
146. Excessive message nesting fails closed even if the total canonical byte payload remains within the per-unit byte
     fuse.
147. Unordered collection canonicalization precomputes bounded canonical sort keys before sorting.
148. Ordinary sorting comparators do not recursively traverse full child metadata, raw facts, nested messages, staging
     slabs, or canonical byte trees.
149. Full canonical byte comparison during sorting is a metered final tie-breaker only.
150. Zero-copy slice views derived from staging slabs do not cross frozen, planning, interner, report, public DTO, or
     persistent publication boundaries.
151. Payload material that survives the seal boundary is copied, compacted, or migrated into a sealed artifact-owned
     slab
     or verified canonical byte handle.

152. Tie-breaker exhaustion does not publish partially ordered material.
153. If ordinary sort tie-break budgets are exhausted, the encoder either enters a ratified bounded cold exact sort path
     before
     publication or fails the current identity scope closed.
154. Bounded cold exact sort paths remain inside canonicalization and complete exact deterministic ordering before
     publication.
155. Quarantined or partially sorted material is never visible to planning, providers, `PlanCacheKey`,
     `CanonicalPlanNode`, report manifests, frozen image publication, interner publication, or persistent artifacts.
156. Canonical sort key precomputation consumes explicit transient scratchpad budgets.
157. Canonical sort scratch arenas do not cross publication boundaries.
158. Concurrent canonical sorts consume resolved aggregate and per-lane scratch budgets, not live heap availability.
159. Duplicate canonical map keys are detected in a dedicated deterministic phase.
160. Duplicate key detection is not an incidental side effect of sorting comparators, platform hash tables, worker
     races,
     lane merge races, or diagnostic formatting.

161. Tie groups are represented by primitive scratchpad ranges, not heap object bucket graphs.
162. Tie-group extraction uses in-place clustering, two-pointer range scanning, or index-array partitioning inside
     admitted
     primitive scratchpads.
163. Projection escalation is permitted only inside tie groups and is selected by domain/schema/version/resolved policy
     before canonicalization begins.
164. Bounded cold exact sort paths operate only on tie groups unless the entire collection is one tie group.
165. Bounded cold exact sort paths use non-recursive canonical byte-slice comparison.
166. Exact clone groups do not trigger all-to-all pairwise canonical byte comparison.
167. Exact clone groups are handled by the owning collection duplicate policy before publication.
168. Unordered semantic sets define an explicit duplicate canonical element policy.

169. Canonical object encoding means protocol-record encoding, not JVM object graph serialization.
170. Object fields that enter canonical bytes are owned by the active identity domain schema.
171. Object encoding does not discover fields through reflection, JVM declaration order, Kotlin data-class component
     order,
     Java serialization, Jackson, kotlinx serialization, backend descriptor traversal, or framework serializer order.
172. Object encoding is bounded by deterministic object nesting, field-count, reference-count, encoded-byte,
     encoder-frame,
     and decoder-frame limits.
173. Canonical object references use sealed stable intern ids, verified canonical identity handles, domain-ratified
     local
     selector material, SCC-local references inside SCC seal payloads, or bounded embedded messages.
174. Canonical object encoding never recursively inlines arbitrary reachable JVM object graphs.
175. Duplicate non-repeated field tags fail closed.
176. Repeated fields declare ordered/unordered and duplicate-preserving/duplicate-rejecting/duplicate-collapsing policy.
177. Default values are encoded explicitly, prohibited, treated as distinct canonical state, or rejected by the owning
     domain.
178. Partially decoded object material is not published to frozen tables, interners, planning providers, `PlanCacheKey`,
     `CanonicalPlanNode`, report manifests, public DTOs, or persistent artifacts.

179. Canonical version-bundle payloads are tagged fixed-width/variable-width axis sequences.
180. Version-bundle axes are encoded as `axisId32`, `axisValueWidth16`, `reserved16`, and `axisValueBytes`.
181. Version-bundle integer axis payloads use fixed-width little-endian unsigned bit patterns unless explicitly ratified
     otherwise.
182. Delimiter-free concatenation of version-axis values is forbidden.
183. Varint axis encoding is forbidden unless a future ADR ratifies it for a specific axis id.
184. Version-bundle axis entries are sorted by `axisId32` ascending.
185. Duplicate version-bundle axis ids fail closed.
186. Unknown required version-bundle axis ids fail closed.
187. Unknown optional version-bundle axis ids are skippable only when the active compatibility matrix ratifies the axis
     as
     skippable and non-critical.
188. `capabilityProfileVersion`, `entropyVersion`, `resourcePolicySchemaVersion`, `compatibilityMatrixVersion`, and
     `canonicalEncodingPolicyVersion` are explicit global version axes when they affect canonical identity behavior.
189. Version-bundle fingerprint derivation uses a domain-separated BLAKE3 context distinct from HID derivation.
190. Version-bundle fingerprint output split is `bytes[0..8]` -> high64 and `bytes[8..16]` -> low64, both little-endian.

191. `identityDomainId32` is part of every canonical version-bundle payload.
192. `identityDomainId32` appears in the fingerprint input even though it also appears in `CanonicalEnvelopeHeader`.
193. All integer global version-axis names include explicit physical-width suffixes.
194. Ambiguous global version-axis names without width suffixes are not releaseable.
195. `axisValueWidth16` matches the physical-width suffix, registry width, and actual `axisValueBytes` length.
196. `canonicalOrderingAlgorithmVersion32` is present when unordered collection ordering can affect the domain.
197. `typeIdentityAlgorithmVersion32` is present when TypeReference identity or type normalization can affect the
     domain.
198. `sccSealAlgorithmVersion32`, `collisionVerificationPolicyVersion32`, and `stableInternIdAssignmentVersion32` are
     present when those algorithms can affect publication or identity material.
199. Version-bundle axes that affect only non-semantic display, logging, progress, or report styling are excluded.

200. Decoder loops prove strict cursor progress.
201. Zero-length semantic values are allowed only when their enclosing encoded record still advances by non-zero encoded
     width.
202. Zero-displacement parser loops fail closed.
203. `HEADER_FLAG_SCC_SEAL_PAYLOAD = 0x0001` is the self-describing header flag for SCC seal payloads.
204. `WIRE_TYPE_SCC_LOCAL_REF` is legal only when `HEADER_FLAG_SCC_SEAL_PAYLOAD` is set and the domain/schema ratifies
     SCC sealing.
205. SCC seal payload status is not supplied by `ThreadLocal`, mutable decoder-global state, caller-local ad hoc
     parameters, backend context, or process-global parser mode.
206. Canonical sort keys do not include per-process random seeds, per-scope random seeds, runtime entropy, time, thread
     ids, worker ids, heap addresses, or ASLR-dependent values.
207. Randomized hashing may be used only for non-authoritative route/probe structures and never for canonical ordering.
208. HashDoS defense for canonical sorting uses deterministic projection escalation, bounded exact tie-break,
     duplicate policies, bounded cold exact sort, and fail-closed budgets.
209. Capacity solver arithmetic for identity budgets is fixed-point or checked integer arithmetic, not floating-point.
210. Capacity solver safety multipliers are explicit numerator/denominator policy values when fractional scaling is
     used.

211. Small-inline canonical bytes are optional physical optimization, not semantic identity authority.
212. Small-inline verification uses a resolved branch-bounded mode such as disabled, segregated inline table,
     preclassified two-pass, or measured mixed.
213. Unpredictable per-candidate inline/external branches in hot verification loops require benchmark evidence.
214. JVM heap primitive arrays are logical grouping baselines and do not prove physical 64-byte alignment.
215. Exact cache-line alignment claims require off-heap, MemorySegment, generated layout, or runtime-specific evidence.
216. Nested unordered collection sorting consumes sealed child canonical material or verified child handles, not
     recursive
     comparator traversal.
217. Published sealed slabs are immutable and are not compacted in place.
218. Background defragmentation does not rewrite published canonical byte offsets or stable handles.
219. Long-running memory reclamation uses scope teardown, image-epoch retirement, whole-slab reclamation, or rebuild /
     republish.
220. Collision escalation is contained to the smallest deterministic lawful boundary selected by resolved policy.
221. Collision overflow does not ordinarily terminate the process or poison unrelated scopes, lanes, domains, or already
     published images.

222. Short-inline field payload encoding is selected by schema and `canonicalEncodingPolicyVersion32`, not by runtime
     profiling.
223. Short-inline field payload layout is canonical encoding material and therefore versioned.
224. Inline/external field payload choice is deterministic for the same semantic value, schema, and encoding policy.
225. SCC sealing uses deterministic preflight and budget reservation when SCC size, references, projected bytes, sort
     scratch, or payload shape can approach resolved caps.
226. Size-only SCC canonicalization does not publish canonical bytes, HIDs, interner candidates, stable ids, frozen
     rows,
     planning providers, or report manifests.
227. SCC budget reservation succeeds before member publication, interner candidate publication, stable id assignment,
     frozen image publication, planning visibility, or report manifest publication.
228. SCC preflight and reservation do not change canonical bytes, HID derivation, collision verification, stable intern
     id
     assignment, or semantic equality for accepted SCCs.

229. External field payload slice offsets are payload-relative, not envelope-absolute.
230. `fieldTableOffset32` and `payloadOffset32` are envelope-absolute offsets from the first byte of
     `CanonicalEnvelopeHeader`.
231. Payload-relative offset `0` means the first byte of the declared payload region.
232. External payload slices that require non-overlap are encoded in physical payload order or use a ratified
     linear-time
     non-overlap proof.
233. Ordinary decoders do not sort arbitrary slice intervals to prove non-overlap under adversarial input.
234. Repeated-field and variable-payload decoder loops prove strict cursor, entry-index, or record-width progress.
235. Zero-length semantic values are valid only when the enclosing descriptor or record still advances.
236. Branch-bounded bounds validation may aggregate invalid bits, but it preserves every required fail-closed check.
237. Branch reduction does not change accepted/rejected payloads.

238. Staging slices and published slices are separated by type-state.
239. Publication APIs accept only published-slab slices, verified canonical byte handles, stable intern ids, or
     frozen-image
     owned handles.
240. Staging-slab slice types cannot cross publication boundaries.
241. Zero-copy promotion is allowed only with single-owner, write-closed, fully validated, budget-accounted ownership
     transfer.
242. Unsafe zero-copy promotion falls back to copy/compact/migrate or fails the current publication scope closed.
243. Published sealed slabs are immutable and never defragmented in place.
244. Reader leases or equivalent epoch guards bound old epoch pinning.
245. Slow readers fail at cooperative safe points, are cancelled, or block new publication according to resolved runtime
     policy; sealed slabs are not reclaimed while unsafe raw readers can observe them.
246. Pinned epoch count, pinned sealed bytes, active reader leases, lease duration, or generation gap are bounded by
     resolved runtime/adapter policy.

247. Ordinary published-slab read hot paths use engine-owned lane epoch records, not globally contended
     lock/counter lease acquisition.
248. Asynchronous epoch reclaimer scanning is physical memory management and not canonical identity logic.
249. Reader leases are released or suspended before external I/O, user callbacks, framework callbacks, coroutine
     suspension, virtual-thread parking, or work-stealing migration boundaries.
250. Reader-lease hot paths are non-suspending.
251. `ThreadLocal`, coroutine-local, fiber-local, virtual-thread-local, JVM executor, OS scheduler, and work-stealing
     queue state are forbidden as reader epoch ownership authority.
252. Slow-reader containment is selected by resolved runtime/adapter policy and does not depend on heap pressure,
     scheduling, random fallback, or unratified profiling.
253. Asynchronous reclaimers reclaim only whole retired epochs/slabs that no valid reader lease can observe.
254. Asynchronous reclaimers do not move published bytes, rewrite offsets, or invalidate active leases without a safe
     cancellation boundary.

255. Reader epoch ownership is engine-owned lane state, not hidden ambient thread state, worker ownership, or scheduler
     state.
256. Coroutine-based reader lease lifetime management is forbidden.
257. Lease suspension as an implicit coroutine/fiber operation is forbidden; a lease must be released before external
     scheduler or adapter boundaries.
258. Asynchronous reclaimers scan explicit lane/topology/adapter epoch records, not hidden thread-local state.

259. M:N worker/lane topology is dispatch topology, not reader epoch ownership.
260. Workers acquire temporary `LaneExecutionLease` authority and do not own lane epoch slots.
261. The engine-owned `LaneEpochTable` is the reader epoch authority.
262. External callbacks, adapter completions, retired-epoch notifications, timeout signals, and reclaimer notifications
     are event-ingestion boundaries and cannot directly mutate lane-owned state.
263. Async sources interact with reader-lease state only through explicit event records delivered to an owning lane,
     engine maintenance queue, epoch-reclaimer queue, or adapter ingress queue.
264. `scheduler-owned` state is not a lawful reader epoch ownership model.

265. Dense decoder table indexing is performed only after tag upper-bound validation.
266. Decoder schemas define `maxRatifiedTag32`, ratified tag count, dense table length, and maximum generated table
     size.
267. Decoders fail closed before indexing if `tag32 > maxRatifiedTag32` or `tag32 >= denseDecoderTableLength` for dense
     dispatch.
268. Generated decoder tables are produced from ratified schema and are not dynamically resized from payload-observed
     tags.
269. Sparse tag schemas use bounded generated lookup strategies rather than payload-sized arrays.
270. Text validation rejects malformed UTF-8 and invalid surrogate material before string material becomes identity
     material.
271. Text validation uses branch-bounded, table-driven, chunked, vectorized, or equivalent bounded validation where it
     is
     in the hot path.
272. Platform charset replacement behavior, locale, JVM default charset, and backend decoder side effects are not
     canonical text validation authority.

273. Canonical identity encoding permits explicit deterministic protocol state.
274. Canonical identity output is independent from non-authoritative observation state.
275. Encoders do not use previous item state, recently seen values, cache hit/miss history, callback completion order,
     thread-local previous values, acquisition-batch neighbors, or current-run frequency distribution as canonical
     encoding authority.
276. Explicit encoder cursors, bounded frame stacks, field presence bitmaps, arena offsets, SCC-local deterministic
     ordinal state, budget ledgers, and diagnostic meters are lawful only when protocol-owned, metered, deterministic,
     and visible to the encoding law.

277. HID128 is a compact candidate descriptor, not semantic equality authority.
278. Intern-table HID matches always enter a verification ladder before equality is accepted.
279. Digest suite domain separation input is a canonical fixed-width payload, not text concatenation or enum/declaration
     order.
280. `VersionBundleFingerprint128` is part of HID domain separation input.
281. Digest suite extensible output is fixed-width and ratified; arbitrary runtime XOF expansion is forbidden.
282. HID width collision probability is a sizing aid under uniform-output assumptions and never removes exact
     verification.
283. Seal and materialization boundaries are defined before hot projections are generated.
284. Sealed projection tuples are immutable and stale projections are rejected by version, epoch, or handle material.
285. Non-cryptographic hashes are non-semantic hints only after the owning verification path has succeeded for the
     current
     operation.
286. Digest suite/HID golden vectors are required release artifacts.
287. Digest suite migration is versioned and cannot silently compare HID material across suites.

288. BLAKE3 is the v1 ratified digest suite implementation, not the semantic identity contract.
289. Digest algorithms are replaceable only through versioned suite ratification.
290. The digest suite contract fixes suite id/version, domain separation payload, output width, split rule, migration
     law,
     and golden vectors.
291. An adapter implements the selected digest suite but does not select or mutate the protocol suite.
292. Swapping the digest algorithm without changing suite id/version is forbidden.

293. `DigestDomainSeparationPayloadV1` is exactly 56 bytes.
294. Non-applicable domain-separation axes are encoded as fixed-width zero fields, not omitted fields.
295. `reserved32` is physically present and zero-initialized before hashing.
296. Domain-separation payload builders initialize every byte before hashing.
297. Reused scratch memory containing stale bytes cannot be hashed as a domain-separation payload.
298. Exact cache-line alignment claims for domain-separation payloads require ADR-0042 evidence.

299. All 56 bytes of `DigestDomainSeparationPayloadV1` are digest-suite input.
300. The 56-byte payload is canonical digest input; physical cache-line-oriented slots are substrate-backend material
     and do not prove JVM heap alignment.

301. Physical padding around `DigestDomainSeparationPayloadV1` is not digest-suite input unless a future suite ratifies
     a new payload length.
302. Cache-line-oriented physical slots for domain-separation payloads are substrate-backend material governed by
     ADR-0042.
303. `DigestDomainSeparationPayloadV1` length is not derived from JVM object layout, array base offsets, or cache-line
     subtraction.

304. Hot intern/probe paths do not use `HashedIdentityDescriptor` interface dispatch, descriptor object equality, or
     per-candidate descriptor object allocation as ordinary probe authority.
305. HID hot-path material is stored as fixed-width primitive words or equivalent primitive substrate projections.
306. Version mismatch classification on the HID hot path is zero-allocation, non-blocking, and does not require
     exception
     creation, string concatenation, synchronous log I/O, or cache-entry allocation.
307. Repeated stale-version or malicious-version input is contained by primitive classification, resolved policy,
     adapter
     quarantine, or fail-closed scope handling rather than unbounded log/exception/cache churn.
308. Parent-dependent child HID material is not reused after parent identity changes without active-suite re-derivation
     under the new parent key and version bundle.
309. Parent-independent child canonical bytes may be reused only as sealed, version-bound derivation input; they are not
     equality authority and do not bypass verification.
310. Hierarchical re-keying is O(1) per affected child only when sealed child-local material is already available;
     ADR-0041
     does not promise O(1) invalidation for an entire subtree.

311. ADR-0043 owns full canonical contract graph identity semantics.
312. ADR-0041 may consume sealed structural identity references but MUST NOT treat bare child HID as equality proof.
313. Parent/context derivation MAY avoid re-reading unchanged child canonical bytes only after child-local material is
     sealed and verification-safe.
314. Structural/contextual graph identity optimization is O(1) with respect to sealed child byte length per child
     reference, not O(1) for an entire affected subtree.
315. Future lowered contract graph material for state, protocol, data, governance, DTO, boundaries, and explicit state
     machines remains reserved until ratified by the top-level contract definition document and ADR-0043.

316. Intern-table physical storage backends are adapters, not semantic identity authorities.
317. A high-performance mechanical profile SHOULD provide an explicitly aligned intern-table backend or document itself
     as a portable heap baseline.
318. Exact cache-line alignment claims require substrate-backend evidence and cross-backend golden-vector equivalence.
319. `MEASURED_MIXED` small-inline mode is never the default high-performance mode.
320. Small-inline branch policy is fixed before scope admission and cannot adapt per candidate.
321. Persistent collision pressure must reach bounded deterministic containment, not unbounded retry, logging, or lane
     pinning.
322. Collision quarantine releases transient resources and reaches an explicit terminal state.
323. NUMA/off-heap/native locality is physical backend material and cannot enter identity material.
324. SCC two-phase sizing requires measure/write equivalence over the same canonical encoder law.
325. SCC materialization cursor mismatch fails the SCC seal closed before publication.

326. Intern id wrapper objects are cold facades only.
327. Committed intern-table rows use primitive id material and table-level proof, not wrapper objects.
328. `TypeReferenceInternId`, `StableInternId`, and `InternHandle` class/interface shapes are not normative hot-path
     representations.
329. Hot intern tables MUST NOT store `Array<TypeReferenceInternId>`, `Array<StableInternId>`, `Array<InternHandle>`,
     or equivalent wrapper-object arrays as their ordinary committed representation.
330. Type-state for provisional/verified/stable intern handles is normative; object hierarchy is not.
331. Cold facades may be materialized only after primitive row/table integrity proof has been validated.
332. Architecture tests reject wrapper-object intern ids in committed hot-path storage, frozen row arrays, planning hot
     loops, and L2 exact-match keys.

333. Intern-table probe budgets are resolved before identity scope admission.
334. Probe-budget admission uses integer arithmetic only; floating-point arithmetic is forbidden.
335. Probe capacity must satisfy
     `admittedCandidateCount * maxLoadFactorDenominator <= logicalTableCapacitySlots * maxLoadFactorNumerator`.
336. Denominator and numerator constraints are verified before any `ceilDiv(...)`, division, or ratio-derived
     arithmetic.
337. Power-of-two or backend-ratified capacity schedules must be deterministic and versioned.
338. Hot probe work must satisfy `maxProbeDisplacementSlots + 1 <= maxHotProbeGroups * probeGroupWidthSlots`.
339. Hot probe bytes must satisfy
     `maxHotProbeProjectionBytesPerOperation = maxHotProbeGroups * probeGroupWidthSlots * logicalHotSlotBytes`.
340. `totalInternTableBytes(resolvedProbeCaps) <= internTableBudgetBytes` must be proven before stable id publication.
341. Backend physical overhead and internal fragmentation must be included in intern-table byte feasibility.
342. Transient resize/rebuild/migration memory must be reserved before allocation.
343. Collision amplification must satisfy the resolved `maxHotCollisionCandidates` and
     `maxExactVerificationBytesPerProbe` bounds.
344. Resize and rebuild counts are deterministic physical work and must be bounded by resolved policy.
345. Resize, rebuild, relocation, or reindex must not change stable intern id assignment.
346. Probe exhaustion must reach a bounded classification; unbounded probing, retry spinning, and per-candidate
     exception/log allocation are forbidden.
347. Probe work is ledger-accounted through deterministic counters, not elapsed wall-clock time.
348. ADR-0042 backend profiles must prove compliance with ADR-0041 probe budgets before identity scope admission.
349. Per-domain interner capacity feasibility must be proven before scope admission.
350. Shared interner tables must prove deterministic domain capacity slices or an equivalent isolation mechanism.
351. Insert-time and resize-time load-factor revalidation is mandatory.
352. Invalid load-factor numerator/denominator material fails closed before any division or `ceilDiv(...)` execution.
353. Hot-path probing must not rely on exception-throwing arithmetic for overflow detection.
354. Physical cache-line residency must not be inferred from `logicalHotSlotBytes` or logical probe-byte budgets.
355. Backend physical overhead, internal fragmentation, migration metadata, and transient rebuild memory must be
     included in intern-table byte feasibility.
356. Cold collision structures must have a resolved budget before they can be used as escalation outcomes.
357. SCC sealing must not bypass interner collision budgets.
358. Collision exact-verification scratch and canonical sort scratch must be proven disjoint or charged to a shared
     transient identity work budget.
359. Probe ledger enforcement must use bounded deterministic counters; ordinary hot loops must not require global atomic
     counter mutation per visited slot.
360. Published read-path locality claims are ADR-0042 backend-profile evidence, not ADR-0041 semantic identity material.
361. Cross-scope intern id equality by numeric id alone is forbidden.
362. Retired or superseded intern scopes must reject stale ids or translate through a ratified cross-scope translation
     law.

363. Interner scopes declare their candidate counting mode before scope admission.
364. `BOUNDED_STREAMING` scopes do not debit a global atomic candidate counter per discovered candidate.
365. Bounded streaming quota is engine-lane-owned, not worker-owned, `ThreadLocal`, coroutine-local, scheduler-owned,
     callback-local, or adapter-local hidden state.
366. Chunked quota refill, when enabled, is processed through deterministic events and bounded safe points.
367. Provisional candidate handles are admitted only after staged canonical bytes, scratch bytes, handle bytes, metadata
     bytes, lane-local staging bytes, and domain staging bytes pass resolved caps.
368. Staged-memory exhaustion is detected by admission checks, not by late `OutOfMemoryError` after provisional handle
     issuance.
369. `INCREMENTAL_AFFECTED_SET` mode requires bounded invalidation traversal budgets before stable identity publication.
370. Reused sealed references are not counted as new candidates, but discovering reuse consumes traversal/read budgets.
371. Interner memory is charged to the owning bounded-context memory envelope; it is not unbounded add-on memory.
372. Planning interning charges the planning run memory envelope; frozen interning charges the frozen acquisition/image
     envelope; L2 interning charges the L2 storage/partition envelope.

373. Refill-capable bounded streaming must run a deterministic quota reclamation barrier before fail-closed refill
     exhaustion when unused lane quota may still exist.
374. Quota reclamation is a deterministic safe-point barrier, not opportunistic quota stealing.
375. Quota stranding is budget state and must not be treated as semantic inequality.
376. `maxTransientRebuildBytes` is a high-water reserve over admitted resize/rebuild events, not a sum of all sequential
     event spikes.
377. Reuse of transient rebuild reserve across sequential events is lawful only after prior event resources are released
     under ADR-0042 lifecycle rules.
378. Incremental affected-set traversal exhaustion must reach a bounded diagnostic classification before fallback,
     quarantine, or fail-closed outcome.
379. Full-rebuild fallback from incremental traversal exhaustion requires separately admitted full-rebuild budgets.
380. Repeated incremental traversal exhaustion should be classified as dependency-graph pressure or pathological shape
     rather than silently treated as ordinary fallback.
381. Incremental traversal diagnostics are bounded by `maxInvalidationTraversalDiagnosticsBytes`.
382. Insert-time load-factor checks must use overflow-checked integer cross multiplication; threshold-division forms are
     forbidden.
383. A load-factor insert check must reject before publication if
     `nextCandidateCountByIdentityDomain * maxLoadFactorDenominator <= logicalTableCapacitySlotsByIdentityDomain[identityDomainId] * maxLoadFactorNumerator`
     is false.
384. Overflow in either product of a load-factor insert check fails closed through a bounded probe-budget classification
     before candidate visibility.

385. Refill-capable bounded streaming must retain a deterministic reserve pool or explicitly select a strict no-refill
     profile.
386. A refill-capable profile must not preallocate the entire candidate cap into lane-local quota unless it accepts
     strict no-refill exhaustion semantics.
387. Reserve-pool refill must be attempted before deterministic quota reclamation when the reserve pool can satisfy the
     request.
388. Duplicate pre-screen staging is allowed only as bounded, domain-separated, version-bound acceleration and is not
     equality authority.
389. Full canonical staging may be delayed behind duplicate pre-screening, but exact canonical verification and
     deterministic deduplication remain required before seal.
390. Pre-screen tickets must not become stable intern ids, PlanCacheKey material, frozen-image material, report
     identity, persistent artifact identity, or query reuse keys.
391. Incremental full-rebuild fallback requires full-rebuild preflight before fallback admission.
392. Failed full-rebuild preflight must not allocate or partially publish a full rebuild before rejecting fallback.
393. Full-rebuild fallback diagnostics must be bounded by `fullRebuildPreflightDiagnosticsBytes`.

394. Owner-lane routing derives ownership from a resolved deterministic route map, not discovery lane or runtime timing.
395. Routing material is not equality authority and must not replace canonical byte verification or collision
     verification.
396. Routed candidate batches must flush on deterministic count, byte, ownership, branch/frame/scope, route/quota epoch,
     seal, publication, rollback, or phase-exit boundaries.
397. Partial-fill routed batches must not remain buffered across a boundary that can make candidates unreachable,
     unsealed, or unaccounted.
398. Routed batch flushing must not depend on wall-clock timers, queue depth, CPU utilization, throughput, GC behavior,
     consumer speed, or adaptive runtime profiling.
399. Owner inbox backpressure must use bounded deterministic route-drain or a stricter fail-closed profile; unbounded
     blocking, spinning, and time-based waiting are forbidden.
400. Cooperative route-drain must not transfer candidate staging, duplicate suppression, provisional handle issuance,
     collision verification, stable id assignment, or publication ownership away from the deterministic owner lane.
401. Producer-local suppression structures are optional traffic-reduction structures, not equality authority.
402. Producer-local suppression entries, routed batch buffers, and owner inbox entries are charged to resolved staging /
     routing memory budgets.
403. Route-range split is lawful only at a route-epoch or scope boundary using deterministic ledger counters.
404. Exact hot-key pressure must not be solved by splitting the same canonical candidate across multiple owner lanes.

## 23. Required Golden Vectors

Golden vectors MUST exist for:

### 23.1. Canonical Encoding

- empty object envelope;
- single string field;
- UTF-8 byte length vs UTF-16 length case;
- enum protocol id;
- signed integer;
- unsigned 64-bit raw bit pattern;
- repeated ordered field;
- repeated unordered field sorted before encoding;
- explicit `UNKNOWN`;
- explicit `UNAVAILABLE`;
- rejected malformed surrogate case;
- early malformed-text preflight case;
- `CanonicalEnvelopeHeader` 64-byte layout fixture;
- `magic32 = 0x4B4E5443` fixture;
- `headerSize16 = 64` fixture;
- `headerFlags16 = 0x0000` fixture;
- mandatory `payloadLength32` fixture;
- version header;
- `fieldCount16` cap boundary fixture;
- declared header size / alignment / reserved padding case;
- reserved padding zero case;
- reserved padding non-zero fail-closed case;
- checked offset / length valid slice case;
- offset overflow fail-closed case;
- negative offset / negative length fail-closed case;
- payload slice pointing into hot header fail-closed case;
- zero-copy canonical byte slice fixture;
- unordered collection sorted by bounded canonical sort keys;
- full-byte tie-break metering fixture;
- unknown tag default rejection;
- ratified skippable unknown field fixture where a compatibility matrix allows it;
- `WIRE_TYPE_SCC_LOCAL_REF` valid only inside SCC seal payload.

### 23.2. BLAKE3 / HID

- BLAKE3 digest over canonical bytes;
- BLAKE3 keyed derivation;
- domain separation: same bytes, different domains, different outputs;
- HID64 truncation;
- HID128 output;
- HID256 output;
- version tuple change changes output;
- version-bundle fingerprint derivation fixture;
- version-bundle axis id ordering fixture;
- version-bundle duplicate axis id fail-closed fixture;
- version-bundle axis width mismatch fail-closed fixture;
- version-bundle non-zero reserved field fail-closed fixture;
- version-bundle delimiter-free concatenation rejection fixture;
- version-bundle unknown required axis fail-closed fixture;
- version-bundle unknown optional skippable axis fixture where ratified;
- version-bundle capability profile version bump fixture;
- version-bundle entropy version bump fixture;
- version-bundle resource policy schema version bump fixture;
- version-bundle identity domain id change fixture;
- version-bundle metadata identity policy schema version bump fixture;
- version-bundle canonical ordering algorithm version bump fixture;
- version-bundle type identity algorithm version bump fixture;
- version-bundle SCC seal algorithm version bump fixture;
- version-bundle collision verification policy version bump fixture;
- version-bundle stable intern id assignment version bump fixture;
- version-bundle physical-width suffix mismatch fail-closed fixture;
- encoding version change changes output;
- HID width bound fixture satisfying `n(n - 1) / 2^(b + 1) <= p_target`;
- seal-time BLAKE3 derivation with deterministic route64 projection;
- hot route/probe path proving no full canonical-material rehash is required after seal-time projection.
- hot HID128 primitive projection fixture proving interface-dispatched descriptor equality is not required;
- version mismatch zero-allocation classification fixture;
- repeated stale-version input containment fixture;
- parent identity change re-derives child HID fixture;
- unchanged child canonical bytes reused as sealed derivation input fixture;
- old parent-dependent child HID rejection after parent identity change fixture.
- ADR-0043 sealed structural reference bridge fixture;
- bare child HID rejected as parent derivation proof fixture;
- parent/context derivation over sealed structural child reference fixture;
- structural/contextual identity split remains byte-equivalent after child-local material reuse fixture;
- affected parent with multiple sealed child references proves fixed-reference-width derivation cost fixture.

### 23.3. TypeReference Identity

- simple type;
- nullable and non-nullable cycle identity behavior where applicable;
- generic type;
- nested type;
- ratified type-use contract qualifier material derived from use-site annotations or equivalent contract surfaces;
- explicit unavailable metadata;
- backend-neutral equivalence case;
- reflection/KSP equivalent lowering case;
- backend disagreement fail-closed case.

### 23.4. Interning

- order-independent id assignment;
- shuffled acquisition inputs produce identical ids;
- parallel completion order simulation produces identical ids;
- collision group verification distinguishes two materials;
- frozen ordinal differs from stable intern id;
- scope mismatch prevents id comparison.

### 23.5. L2 Integration

- route64 deterministic derivation;
- route64 sentinel remap;
- route64 collision exact-key verification;
- PlanCacheKey version mismatch rejection;
- hot/cold cache equality.

### 23.6. Version Bundle and Policy Vectors

Golden vectors MUST exist for:

- canonical encoding version bump;
- HID derivation version bump;
- schema version bump;
- normalization version bump;
- byte-equivalent compatibility case;
- rehash-required compatibility case;
- reintern-required compatibility case;
- incompatible fail-closed case;
- canonical-byte per-unit cap exceeded;
- aggregate canonical-byte budget exceeded;
- profile scaling preserves per-TypeReference unit cap unless a versioned protocol-ratified domain exception exists;
- per-unit fuse change rejection when protocol-ratified exception evidence / golden-vector coverage is missing;
- collision group cap exceeded;
- collision overflow contained to current identity scope fixture;
- collision overflow does not poison unrelated scope fixture;
- collision overflow does not terminate process as ordinary path fixture;
- bounded cold collision structure accepted fixture where ratified;
- bounded cold collision structure overflow fail-closed fixture;
- HID64 cardinality proof rejection;
- HID128 default intern-membership acceptance;
- v1 `AUTO` resolving to the deterministic `STANDARD` bootstrap cap set;
- v2 `AUTO` solver fixture producing identical aggregate budgets from identical pre-admission snapshots;
- `AUTO` live-adaptation rejection inside an admitted scope;
- arbitrary user-authored numeric cap editing rejection on ordinary configuration surfaces;
- `maxTypeReferenceCount * minimumEncodedBytesPerTypeReference <= maxTotalTypeReferenceCanonicalBytes` boundary;
- `maxTotalTypeReferenceCanonicalBytes <= maxTypeReferenceCount * maxCanonicalBytesPerTypeReference` feasibility check;
- raw-fact-record count/byte/fuse feasibility boundary;
- intern-candidate count/byte/fuse feasibility boundary;
- active v1 shared metadata interner candidate-count sum feasibility;
- active v1 shared metadata interner byte-budget sum feasibility;
- traversal-edge derivation feasibility;
- frozen-table-byte derivation feasibility;
- scope-level budget equation boundary;
- target-average TypeReference canonical-byte budget tightening fixture;
- fixed-point TypeReference target-average budget fixture;
- floating-point capacity calculation rejection fixture;
- fixed-point safety multiplier overflow fail-closed fixture;
- target-average raw-fact-record canonical-byte budget tightening fixture;
- target-average intern-candidate canonical-byte budget tightening fixture;
- rejection of a solver that uses only the worst-case count-times-fuse product without published sizing evidence.

---

### 23.7. Mechanical Sympathy Golden Vectors and Microbench Gates

Golden vectors and benchmark gates MUST exist for:

- fixed-width hot header encoding;
- field offset table decoding;
- dense tag dispatch;
- unknown tag fail-closed behavior;
- duplicate tag fail-closed behavior;
- wire-type mismatch fail-closed behavior;
- hot-header version mismatch rejection;
- HID match with length mismatch rejection;
- HID match with inline verifier prefix mismatch rejection;
- HID match with prefix match and full canonical byte mismatch collision handling;
- shuffled acquisition order preserving intern ids;
- parallel completion order preserving intern ids;
- intern table probe budget saturation behavior;
- SIMD candidate mask equivalence against scalar probing;
- SIMD width variation preserving identical candidate sets;
- provisional handle rejection at planning-visible boundary;
- NUMA-local staging order preserving stable ids after deterministic merge;
- prefetch-aware slab layout preserving canonical identity;
- query dependency set shuffled order preserving query key;
- physical acceleration equivalence against the reference deterministic pipeline;
- canonical collection sorting with bounded sort-key precomputation;
- recursive-comparator amplification rejection or metering;
- checked offset arithmetic overflow rejection;
- zero-copy slice exposure without per-field `ByteArray` allocation;
- declared header padding validation;
- unknown tag default rejection;
- ratified unknown-tag skip where compatibility permits it;
- SCC-local reference wire type rejection outside SCC seal payload.

Microbenchmarks SHOULD measure:

- allocation count per hot decode;
- branch-miss-sensitive decode throughput where available;
- HID miss probe throughput;
- HID primitive projection probe throughput without descriptor polymorphism;
- version mismatch classification throughput without exception/log allocation;
- HID hit / prefix mismatch probe throughput;
- HID hit / full verification path throughput;
- canonical byte slab pointer-chasing frequency;
- cache-line group density.

A benchmark is not semantic authority, but it is required evidence that the physical protocol did not collapse into
pointer-heavy object traversal.

A release claiming compliance with the physical layout law MUST publish a versioned benchmark threshold table for its
target runtime profile.

The threshold table SHOULD cover at least:

- canonical encoder allocations per identity;
- canonical decoder allocations per identity;
- HID derivation throughput;
- intern table successful miss probe latency;
- intern table successful hit probe latency;
- full verification rare-path rate;
- average probe count;
- p99 probe count;
- collision group size;
- collision group escalation into fail-closed scope rejection / bounded cold path / quarantine where ratified;
- SCC preflight early-abort rate for invalid SCC fixtures;
- canonical bytes p50 / p95 / p99 length;
- allocation rate;
- GC pressure;
- JIT deoptimization or uncommon-trap regression where measurable;
- SIMD group probe scalar-equivalence overhead;
- NUMA staging merge overhead;
- query-key derivation throughput;
- prefetch slab hot/cold access ratio;
- canonical collection sort-key generation cost;
- comparator fallback rate;
- zero-copy slice extraction allocation rate;
- offset table validation throughput;
- tag mask/shift and dispatch cost.

Hardware-specific absolute nanosecond numbers belong to the release threshold table, not to this ADR.

### 23.8. Compact Encoding and Recursive Interning Vectors

Golden vectors MUST exist for:

- schema-aware stripping with numeric tags and no field-name strings;
- positional physical layout rejection before schema validation;
- observation-independent encoding under shuffled acquisition order;
- previous-item delta rejection;
- immutable canonical-base delta reconstruction where a domain ratifies canonical-base delta;
- bit-packed field layout boundaries;
- reserved-bit zero encoding;
- reserved-bit non-zero fail-closed decoding;
- parent referencing sealed child intern id;
- parent attempting to reference provisional child handle rejection;
- acyclic dependency graph canonical topological order;
- topological tie-break independence from backend order;
- cyclic dependency graph SCC detection;
- cyclic SCC-local temporary ordinal encoding;
- SCC-local reference wire type encoding inside SCC seal payload;
- SCC-local reference wire type rejection outside SCC seal payload;
- cyclic SCC final stable id assignment;
- SCC-local temporary ordinal escape rejection;
- TypeReference cycle identity independent from ADR-0030 planning truncation;
- general compression exclusion from hot identity equality;
- SCC seal atomic failure when one member exceeds canonical byte cap;
- SCC preflight early abort when member count, intra-reference count, or byte total exceeds cap before final
  publication;
- SCC seal atomic failure when one member lacks required verification material;
- SCC member-count cap exceeded;
- SCC intra-reference cap exceeded;
- SCC-local provisional ids not published after seal failure;
- `HID256` cold summary with `HID128` hot projection equivalence;
- `HID256` two-stage probe vector where a domain ratifies hot use;
- metadata identity cap diagnostic payload shape;
- provisional handle type-state rejection at frozen/planning/L2/public DTO boundaries;
- primitive-array baseline vs value-type candidate adoption benchmark fixture.

### 23.9. Contract Syntax Lowering Vectors

Reserved.

ADR-0041 does not yet require concrete contract syntax lowering golden vectors because the contract fact taxonomy and
frontend lowering law are not ratified in this ADR.

This section is preserved to record the future vector category.

Once the top-level contract document ratifies contract material, golden vectors MUST be added for the ratified surfaces,
including whichever of the following categories become applicable:

- frontend equivalence across annotation / DSL / compiler metadata / generated index surfaces;
- default / effective value behavior, if the contract model defines such behavior;
- reference-boundary behavior;
- backend parity;
- dependency graph behavior;
- SCC behavior;
- canonical byte fields;
- collision verification payloads;
- rejected / unavailable / diagnostic behavior.

Until ratification, no concrete annotation, DSL, or compiler-metadata lowering vector is required by ADR-0041.

- aligned intern-table backend equivalence fixture;
- heap baseline versus off-heap / MemorySegment backend identity equivalence fixture;
- small-inline segregated table fixture;
- small-inline measured-mixed rejection fixture where benchmark gate is not met;
- persistent collision pressure containment fixture;
- collision quarantine resource-release fixture;
- SCC two-phase measure/write equality fixture;
- SCC two-phase cursor mismatch fail-closed fixture;


- primitive stable intern id projection fixture;
- table-level proof for local stable intern id fixture;
- cold facade materialization after table validation fixture;
- wrapper facade excluded from hot intern storage fixture;
- provisional/verified/stable handle primitive state transition fixture;

### 23.x. Interner Probe Budget

- probe capacity feasibility fixture:
  `admittedCandidateCount * maxLoadFactorDenominator <= logicalTableCapacitySlots * maxLoadFactorNumerator`;
- divide-by-zero / invalid load-factor fail-closed fixture;
- numerator/denominator validation-before-division fixture;
- power-of-two capacity schedule fixture;
- backend-ratified non-power-of-two capacity schedule fixture where applicable;
- hot probe work fixture:
  `maxProbeDisplacementSlots + 1 <= maxHotProbeGroups * probeGroupWidthSlots`;
- hot probe byte fixture:
  `maxHotProbeProjectionBytesPerOperation = maxHotProbeGroups * probeGroupWidthSlots * logicalHotSlotBytes`;
- `totalInternTableBytes(resolvedProbeCaps)` feasibility fixture;
- backend physical overhead and internal fragmentation fixture;
- transient resize/rebuild memory reserve fixture;
- collision amplification budget fixture;
- collision group exceeds `maxHotCollisionCandidates` fail-closed / escalation fixture;
- resize budget exhaustion fixture;
- rebuild budget exhaustion fixture;
- rebuild does not change stable intern id assignment fixture;
- physical bucket order does not affect stable id publication fixture;
- probe exhaustion bounded diagnostic fixture;
- probe ledger accounting fixture;
- heap backend and aligned backend produce identical canonical bytes, HID, collision verification result, and stable
  intern ids.

### 23.x. Interner Probe Budget Extended Fixtures

- per-domain probe capacity feasibility fixture;
- shared table domain-slice isolation fixture;
- hot-domain cannot starve cold-domain capacity fixture;
- insert-time load-factor revalidation fixture;
- resize-time load-factor revalidation fixture;
- invalid numerator/denominator fail-closed-before-division fixture;
- checked arithmetic admission failure converted to bounded fail-closed result fixture;
- logicalHotSlotBytes does not claim cache-line residency fixture;
- physical overhead model fixture for heap primitive SoA backend;
- physical overhead model fixture for aligned off-heap or `MemorySegment` backend where enabled;
- migration metadata byte accounting fixture;
- transient resize/rebuild memory spike fixture;
- cold collision structure budget linkage fixture;
- SCC collision budget coupling fixture;
- sort scratch and exact-verification scratch non-double-spend fixture;
- hot-loop counter does not require global atomic mutation fixture;
- read-path locality profile handoff fixture;
- cross-scope intern id translation fixture;
- stale intern id rejection after scope retirement fixture.

### 23.x. Bounded Streaming and Incremental Counting

- candidate counting mode fixture for `PRECOUNTED_BATCH`, `BOUNDED_STREAMING`, `PUBLISHED_TABLE`,
  `INCREMENTAL_AFFECTED_SET`, and `RATIFIED_STATIC_REGISTRY`;
- bounded-streaming lane quota fixture;
- deterministic lane quota refill order fixture;
- unused lane quota reconciliation fixture;
- global atomic per-candidate counter rejection architecture fixture;
- staged canonical bytes cap fixture;
- staged scratch bytes cap fixture;
- staged provisional handle bytes cap fixture;
- lane-local staged bytes cap fixture;
- domain staged bytes cap fixture;
- provisional handle rejection on staged-memory cap exhaustion fixture;
- incremental affected-set traversal edge cap fixture;
- incremental affected-set reused sealed reference read cap fixture;
- owning memory-envelope admission fixture for planning interning;
- owning memory-envelope admission fixture for frozen acquisition interning;
- owning memory-envelope admission fixture for L2 interning.

### 23.x. Quota Reclamation, Transient High-Water, and Incremental Traversal

- lane quota stranding reclamation fixture;
- refill failure before reclamation is rejected fixture;
- deterministic quota redistribution by `engineLaneId`, identity domain id, and request sequence fixture;
- quota reclamation does not change stable intern id assignment fixture;
- transient resize high-water reserve fixture:
  sequential `X -> 2X -> 4X` uses the maximum single-event spike, not the sum of all event spikes;
- overlapping resize/rebuild events require combined high-water fixture;
- incremental traversal budget exhaustion classification fixture;
- incremental full-rebuild fallback requires separately admitted full-rebuild budget fixture;
- repeated traversal exhaustion dependency-pressure diagnostic fixture;
- bounded invalidation traversal diagnostic payload fixture.

### 23.x. Insert-Time Load-Factor Arithmetic

- insert-time load-factor cross-multiplication fixture;
- threshold-division form rejection fixture;
- insert-time load-factor product overflow fail-closed fixture;
- numerator/denominator validation-before-insert-check fixture;
- domain-slice insert rejection before publication fixture.

### 23.x. Reserve Pool, Duplicate Pre-Screen, and Full-Rebuild Preflight

- deterministic reserve-pool refill before reclamation fixture;
- strict no-refill profile early exhaustion fixture;
- initial lane quota total plus reserve pool does not exceed candidate cap fixture;
- reserve-pool refill does not change stable intern id assignment fixture;
- duplicate pre-screen suppresses duplicate full staging without replacing exact verification fixture;
- pre-screen ticket cannot become stable intern id / PlanCacheKey / frozen-image material fixture;
- delayed full canonical staging admission fixture;
- duplicate pre-screen staging budget exhaustion fixture;
- full-rebuild preflight pass fixture;
- full-rebuild preflight fail fixture;
- full-rebuild fallback cannot allocate before preflight fixture;
- bounded full-rebuild preflight diagnostic payload fixture.

### 23.x. Deterministic Owner-Lane Routing and Routed Batching

- deterministic owner-lane routing fixture;
- discovery lane differs from owner lane fixture;
- route material is not equality authority fixture;
- routed candidate batch flush by count fixture;
- routed candidate batch flush by byte budget fixture;
- partial-fill flush-on-branch-close fixture;
- partial-fill flush-on-scope-safe-point fixture;
- partial-fill flush-on-rollback fixture;
- no timer-based batch flush fixture;
- owner inbox backpressure bounded route-drain fixture;
- owner inbox retry exhaustion classification fixture;
- cooperative route-drain does not mutate owner candidate state fixture;
- producer-local suppression memory accounting fixture;
- producer-local suppression is not equality authority fixture;
- route-range split at route epoch boundary fixture;
- exact hot-key pressure does not split one canonical candidate across owner lanes fixture.

## 24. Required Architecture Tests

Architecture tests MUST verify:

- metamodel identity code does not import reflection/KSP/backend handles;
- canonical byte encoder does not call Unicode normalization APIs;
- canonical byte encoder does not call platform default charset;
- canonical byte encoder does not use delimiter-joined strings;
- identity material classes do not expose mutable collections;
- stable intern id assignment does not depend on insertion order;
- frozen ordinal is not used in persistent identity classes;
- route64 is not used as equality authority;
- HID equality is followed by verification in lookup paths;
- hot intern/probe lookup paths do not call `HashedIdentityDescriptor.equals`, descriptor object `hashCode`, or
  interface-dispatched descriptor property access as ordinary probe authority;
- hot HID128 lookup paths use primitive high/low words or equivalent primitive projections;
- version mismatch hot paths return primitive classification and do not allocate exceptions, diagnostic strings, log
  records, or cache entries before cold diagnostic admission;
- parent-dependent child HID projections are invalidated or re-derived when parent identity changes;
- parent-independent child canonical bytes reused by incremental derivation are sealed, version-bound, and
  exact-verification safe;
- protocol ids are not enum ordinals;
- no process-global mutable interner exists for semantic identity.

- canonical decoder hot paths do not use reflection dispatch;
- canonical decoder hot paths do not use string tag lookup;
- canonical decoder hot paths do not use `Map` lookup for field dispatch;
- canonical decoder message nesting is bounded by explicit depth/frame counters and not by JVM stack depth;
- canonical text validators reject malformed UTF-8 before String material becomes identity material;
- canonical text validation does not use platform charset replacement behavior as authority;
- hot-path text validation is branch-bounded or benchmark-gated when acceleration is claimed;
- decoder cursor progress validators reject zero-displacement loops;
- offset-base tests prove that external slice offsets are payload-relative and header offsets are envelope-absolute;
- slice non-overlap tests use physical-order linear validation or a ratified linear proof, not adversarial interval
  sorting;
- zero-length semantic payload tests prove that enclosing encoded records still advance;
- branch-bounded bounds validators are equivalence-tested against direct validation laws;
- identity envelope decoding uses `CanonicalEnvelopeHeader` and rejects non-common header layouts;
- header validation rejects invalid `magic32`, non-64 `headerSize16`, unratified `headerFlags16`, non-zero
  `reserved16`, and non-zero `reserved32`;
- SCC-local reference decoding is controlled by `HEADER_FLAG_SCC_SEAL_PAYLOAD`, not by thread-local or mutable parser
  state;
- SCC seal implementation performs deterministic preflight and reservation before expensive publication when caps are
  near;
- SCC size-only canonicalization cannot publish HIDs, interner candidates, stable ids, frozen rows, planning providers,
  or report entries;
- version-bundle fingerprint derivation is covered by golden vectors and does not depend on map/set iteration order;
- version-bundle payload construction uses tagged axis entries, not delimiter-free concatenation;
- version-bundle axis registry is not derived from enum ordinals, declaration order, source order, classpath order,
  service
  loader order, or process-global registration order;
- version-bundle integer axis payloads are fixed-width little-endian unless explicitly ratified otherwise;
- version-bundle axis ids are sorted before hashing;
- version-bundle payload tests verify that `identityDomainId32` is present even when the envelope also contains
  `identityDomain32`;
- version-bundle axis-name tests reject integer axes without physical-width suffixes;
- version-bundle width tests reject mismatches between axis name suffix, registry width, `axisValueWidth16`, and actual
  `axisValueBytes` length;
- version-bundle tests cover canonical ordering, type identity, SCC seal, collision verification, stable intern id
  assignment, and runtime binding identity algorithm-version bumps;
- identity envelope decoding uses fixed-width hot header material;
- variable payload access uses offset / length material rather than delimiter scanning;
- offset / length validation uses checked arithmetic and rejects overflow before slice exposure;
- hot identity decoding does not allocate per-field `ByteArray` copies for ordinary field extraction;
- canonical byte slices are represented as base slab plus primitive offset / length or equivalent verified slice
  handles;
- unknown canonical identity tags fail closed unless a compatibility matrix ratifies deterministic skip behavior;
- ratified unknown-tag skip validates wire type, length, and bounds before continuing;
- intern-table probe path performs inline domain/version/length/prefix rejection before full byte comparison;
- full canonical byte comparison is not the first action after HID match;
- intern-table hot metadata is stored in primitive-friendly contiguous structures;
- stable intern id assignment remains unchanged after table resize or rebuild;
- TypeReference identity does not recursively inline full raw-fact closure.
- unordered collection canonicalization uses bounded sort keys and does not repeatedly recurse through full metadata
  graphs in comparator loops.
- released identity envelope layouts declare hot header size, alignment, field-table start, payload start, and reserved
  padding rules;
- reserved padding is encoded as zero and non-zero reserved padding fails closed unless ratified by compatibility;
- physical acceleration implementation produces the same canonical bytes, HID values, stable intern ids, and query keys
  as the reference path;
- SIMD probing produces the same candidate set as scalar probing;
- provisional handles cannot be passed to planning-visible, L2-visible, or persistent identity APIs;
- NUMA-local staging output is deterministically merged before stable id publication;
- physical layout policy is immutable for an admitted scope;
- query dependency sets are sorted/canonicalized before query-key encoding;
- prefetch-aware slab order follows already-determined stable order and does not define it.

- canonical identity encoding does not emit semantic field-name strings as identity payload;
- observation-independent encoder tests reject previous-item, recently-seen, or acquisition-order dependent encoding;
- unordered collection encoders precompute bounded canonical sort keys before sorting;
- canonical sort key generation rejects runtime random seeds, per-scope entropy, thread ids, worker ids, time, heap
  addresses, and ASLR-dependent values;
- canonical sort key generation uses the strongest deterministic projection available at the domain boundary;
- adapter-level abuse throttling is outside canonical identity and cannot change admitted canonical order or equality;
- nested unordered collection sorting consumes sealed child material or verified child handles and rejects recursive
  comparator traversal;
- randomized route/probe hashes, where used, are non-authoritative and cannot change canonical ordering or equality;
- canonical sort key precomputation consumes bounded scratchpad budgets before allocating or filling sort-key arenas;
- sort tie-break exhaustion cannot publish partially ordered material;
- bounded cold exact sort paths, where ratified, complete exact deterministic ordering before publication;
- bounded cold exact sort paths operate only on tie groups unless the entire collection is one tie group;
- tie groups are primitive range descriptors and not heap object bucket graphs;
- projection escalation is selected before canonicalization and not by runtime profiling;
- sorting comparators do not recursively decode nested messages or traverse staging slabs;
- sorting comparators do not perform duplicate map key detection as an incidental side effect;
- exact clone groups do not cause all-to-all pairwise canonical byte comparison;
- unordered semantic sets define duplicate canonical element policy;
- bit-packed fields have generated or hand-verified mask/shift tests;
- reserved bits fail closed on decode when non-zero;
- canonical-base delta cannot select a base from run-local frequency or previous item state;
- recursive interning does not inline unbounded child metadata closures;
- parent identity payload cannot reference provisional child handles;
- metadata dependency graph cycles use SCC sealing rather than planning cycle truncation;
- SCC-local temporary ordinals cannot escape sealed SCC encoding;
- `WIRE_TYPE_SCC_LOCAL_REF` cannot appear outside SCC seal encoding;
- TypeReference identity tests prove independence from ADR-0030 planning traversal state.

- no type outside the identity seal package may reference `ProvisionalInternHandle` unless explicitly allowed by the
  seal API;
- `FrozenMetamodelImage` does not contain provisional handles;
- planning-facing providers do not expose provisional handles;
- `PlanCacheKey` does not contain provisional handles;
- `CanonicalPlanNode` does not contain provisional handles;
- public DTOs do not expose provisional handles;
- SCC seal implementation publishes no stable ids on injected member failure;
- SCC-local temporary ordinals do not appear in published stable-id tables;
- SCC cap checks execute before stable-id publication;
- `HID256` hot membership domains declare an explicit probe layout;
- `FROZEN_IMAGE_CONTENT_SUMMARY` is not used as an ordinary hot membership key unless a domain-specific proof exists;
- value-type / inline-object hot-path implementation remains behind a release adoption proof gate;
- zero-copy slice views from staging slabs cannot be stored in frozen image, planning, interner, report, public DTO, or
  persistent artifact surfaces;
- staging and published byte slices are distinct type-state surfaces;
- publication APIs reject staging-slice types and accept only published slices, verified handles, stable intern ids, or
  frozen-image-owned handles;
- zero-copy promotion requires single-owner, write-closed, fully initialized, budget-accounted ownership transfer;
- published sealed slabs are immutable and not compacted in place;
- published sealed slab epochs are reclaimed through reader-lease / epoch-guard accounting, not untracked raw
  references;
- ordinary reader lease acquisition uses explicit lane-owned or engine-owned lane epoch slots and avoids globally
  contended hot-path counters;
- asynchronous epoch reclaimer scans explicit lane/topology/adapter epoch records and never participates in canonical
  identity;
- long-running daemon tests cover slow-reader lease cancellation, pinned epoch limits, and repeated rebuild / republish
  without unbounded old-slab accumulation;
- external I/O, user callback, framework callback, virtual-thread parking, work-stealing migration, adapter scheduler
  handoff, and event-loop handoff tests verify lease release before crossing the boundary;
- reader-lease hot-path tests reject coroutine/fiber/ThreadLocal/scheduler/worker-owned reader epoch ownership and
  reject suspension while a published-slab lease is held;
- sealed slab reclamation uses image-epoch retirement or rebuild/republish rather than background offset rewriting;
- canonical sort scratch arenas cannot be stored in frozen image, planning, interner, report, public DTO, or persistent
  artifact surfaces;
- v1 `AUTO` resolves to the deterministic `STANDARD` bootstrap cap set;
- any future `AUTO` solver runs before scope admission and publishes an immutable resolved policy snapshot;
- no identity scope reads live hardware, GC, load, throughput, cache, or scheduling feedback to mutate caps after
  admission;
- ordinary user/operator configuration surfaces do not expose arbitrary metadata identity cap numbers;
- metadata identity policy resolution goes through a deterministic capacity solver or released bootstrap table;
- resolved metadata identity policies run feasibility validation before scope admission;
- count caps are not accepted when their minimum encoded bytes cannot fit into their aggregate byte budget;
- aggregate byte budgets are not accepted when they exceed their count multiplied by the per-unit fuse unless the domain
  explicitly documents why the extra budget is semantically reachable;
- ADR-0041 v1 shared metadata interner candidate budgets cover TypeReference, TypeCycleKey,
  TypeCycleIdentityPrecheck, TypeShapeSummary, RawFactRecord, ActiveMemberKey, LocalSelectorTuple, and
  RuntimeBindingSnapshot candidates;
- traversal-edge budgets are derived from admitted graph dimensions or explicit domain-split edge budgets;
- frozen table byte budgets are derived from table layout, row counts, load factor, bitsets, offset tables, and
  headroom.

- canonical identity material types do not store `AnnotationDescriptor` as identity material;
- canonical identity material types do not store reflection annotation objects or KSP annotation handles;
- contract syntax lowering architecture tests are reserved until the top-level contract model is ratified;
- future lowered contract material must not expose backend annotation handles, source AST objects, compiler symbols, or
  frontend DTOs as identity material.

---

- BLAKE3 canonical identity derivation is not replaced by Murmur3, xxHash, JVM hashCode, or any other
  non-cryptographic route hint.
- hot lookup code does not repeatedly re-run full BLAKE3 over full canonical material after seal-time projection exists.
- non-cryptographic physical route hints cannot cross into HID, persistent identity, query fingerprint, stable intern
  id, or
  equality-authority surfaces.
- collision cap overflow does not publish HID-only equality and does not retroactively repair stable ids.
- SCC implementations expose metered preflight or equivalent deterministic early-abort evidence.
- direct/off-heap aligned probe implementations are optional physical backends and cannot be required for v1 heap-array
  compliance.
- capacity solver arithmetic tests reject `Float` / `Double` usage for identity budget decisions;
- capacity solver fixed-point arithmetic tests cover numerator/denominator safety multipliers and overflow fail-closed;
- capacity solver outputs include target-average sizing evidence or an explicitly documented equivalent tightening
  relationship.


- bounded streaming discovery loop does not mutate a global atomic candidate counter per candidate;
- bounded streaming quota authority is engine-lane-owned, not worker/thread/ThreadLocal/coroutine/callback-owned;
- provisional handles cannot be issued before staged byte budgets are admitted;
- incremental affected-set traversal is metered separately from dirty candidate count;
- interner scopes charge memory to an owning bounded-context envelope before work begins;
- planning, frozen, L2, VM, and reporting envelopes cannot receive unbounded interner add-on memory.

## 25. Consequences

### 25.1. Positive Consequences

- Metadata identity becomes backend-neutral.
- Frozen acquisition can move from structural comparison to primitive membership.
- Planning can consume dense verified ids without re-opening backend handles.
- L2 can derive routing material without weakening exact equality.
- BLAKE3/HID usage becomes uniform and version-bound.
- Persistent frozen image identity has a safe future foundation.
- Collision handling is explicit rather than assumed impossible.
- Golden vectors make identity drift visible.
- Protocol-owned interning prevents acquisition-order nondeterminism.
- Canonical byte encoding becomes suitable for repeated machine consumption, not merely archival correctness.
- Intern-table probing can reject most non-equal candidates without pointer chasing.
- Mandatory collision verification remains correct without dominating the ordinary hot path.
- Future SIMD, NUMA, prefetch, branch-minimal, and query-based engines can be added without changing semantic identity
  law.
- Physical acceleration becomes an equivalence-preserving implementation detail rather than a semantic authority.
- Canonical byte streams can become smaller without introducing run-order-dependent compression state.
- Layered recursive interning can reduce repeated TypeReference and lowered contract-fact material while preserving
  deterministic sealing.
- Future query/incremental engines can reuse compact identity references without recursively expanding entire metadata
  closures.

### 25.2. Negative Consequences

- More protocol surface must be maintained.
- Golden vectors become mandatory.
- Identity schema versioning becomes stricter.
- Simple "first seen id" interning is prohibited.
- Streaming direct-to-slab implementations must prove equivalence to deterministic batch assignment.
- Debugging identity mismatch requires richer diagnostics.
- Initial implementation will be heavier than a naive hash map.
- Physical layout rules introduce benchmark obligations, not just functional tests.
- JVM implementations may need primitive arrays, generated dispatch tables, off-heap layouts, or carefully padded
  structures to approach the target mechanical profile.
- Physical acceleration adds equivalence tests in addition to functional tests.
- Speculative physical work cannot publish early, so some theoretically possible throughput optimizations remain
  forbidden.
- NUMA-local staging and query-key acceleration require deterministic merge and dependency canonicalization
  infrastructure.
- SCC sealing adds another identity-graph phase before stable intern ids can be published.
- Canonical-base delta requires careful compatibility matrices and reconstruction golden vectors.
- Compactness cannot rely on generic compression or previous-item deltas in the hot identity path.

### 25.3. Accepted Trade-off

Kontrakt accepts this complexity because metadata identity is foundational.

A compiler-grade contract engine cannot allow backend identity, callback timing, cache state, or runtime order to
influence canonical contract identity.

---

## 26. Alternatives Considered

### 26.47. Flush Routed Batches by Wall-Clock Timer

Rejected.

Timer-based flushing may reduce partial-fill latency, but it makes routing behavior depend on wall-clock timing,
scheduler delay, GC pauses, and runtime speed.

Routed batches flush by deterministic count, byte, ownership, branch/frame/scope, epoch, seal, publication, rollback, or
phase-exit boundaries instead.

### 26.48. Block or Spin Until Owner Inbox Space Appears

Rejected.

Unbounded blocking and spinning convert transient inbox pressure into scheduler-dependent behavior and may deadlock the
pipeline.

Owner inbox pressure is handled through bounded deterministic route-drain or a stricter fail-closed profile.

### 26.49. Let Producers Process Owner-Lane Candidate State Directly

Rejected.

Cooperative route-drain may help routing infrastructure, but candidate staging, duplicate suppression, provisional
handle
issuance, collision verification, stable id assignment, and publication remain owned by the deterministic owner lane.

### 26.50. Use Unbounded Producer-Local Duplicate Maps

Rejected.

Producer-local suppression is a bounded traffic-reduction structure.

It is charged to staging/routing memory budgets and is never equality authority.

### 26.51. Split Exact Hot Keys Across Multiple Owner Lanes

Rejected.

Splitting one canonical candidate across multiple owners breaks duplicate suppression authority and risks inconsistent
provisional ownership.

Exact hot-key pressure is handled through bounded suppression, sealed-reference reuse, owner-lane cached material, or
bounded fail-closed / quarantine outcomes.

### 26.47. Preallocate the Entire Candidate Cap into Lane Quotas

Rejected for refill-capable profiles.

Preallocating the whole cap to lanes maximizes quota stranding under skewed workloads and forces reclamation barriers to
become the ordinary path.

A refill-capable profile must retain a deterministic reserve pool or explicitly select strict no-refill semantics.

### 26.48. Treat Duplicate Pre-Screen Matches as Equality

Rejected.

Pre-screen material is an allocation and staging accelerator only.

It is not semantic equality authority and cannot replace canonical byte encoding, collision verification, deterministic
stable id assignment, table coverage validation, or publication integrity validation.

### 26.49. Enter Full Rebuild Immediately After Incremental Traversal Exhaustion

Rejected.

Full rebuild is a separate expensive scope.

It requires its own resolved memory, traversal, staging, interner, SCC, diagnostic, transient rebuild, and publication
budgets.

A fallback may be admitted only after full-rebuild preflight succeeds.

### 26.47. Use Threshold Division for Insert-Time Load-Factor Checks

Rejected.

A threshold form such as:

``````text
nextCandidateCount <= integerThreshold(capacity * numerator / denominator)
``````

reintroduces division into a path that ADR-0041 intentionally defines through integer cross multiplication.

It also invites floating-point threshold interpretation and creates an additional overflow surface in
`capacity * numerator`
before the division.

The accepted form is:

``````text
nextCandidateCount * denominator <= capacity * numerator
``````

with checked integer arithmetic and fail-closed overflow handling.

### 26.44. Let Faster Lanes Steal Quota Opportunistically

Rejected.

Opportunistic quota stealing would make budget outcomes depend on worker scheduling, callback timing, cache warmth, or
first-CAS timing.

Unused quota may be reclaimed only through a deterministic safe-point reclamation barrier owned by the explicit engine
lane protocol.

### 26.45. Sum Every Sequential Resize Spike into the Transient Reserve

Rejected.

Sequential resize/rebuild events do not require all event spikes to be live at the same time.

The reserve is a high-water mark over admitted events, unless the backend permits overlapping resize/rebuild events.

Overlapping events must be explicitly accounted as simultaneously live.

### 26.46. Treat Incremental Traversal Exhaustion as Ordinary Silent Full Rebuild

Rejected.

Traversal exhaustion may be a normal fallback, a pressure signal, or evidence of a pathological dependency shape.

The implementation must classify the exhaustion with bounded diagnostics and may enter full rebuild only through a
separately admitted full-rebuild scope.

### 26.44. Use a Global Atomic Candidate Counter for Bounded Streaming

Rejected.

A single global atomic candidate counter is simple, but it creates cache-line contention and makes candidate discovery
pay synchronization cost per candidate.

The accepted shape is engine-lane-owned quota reservation with deterministic reconciliation.

### 26.45. Use ThreadLocal or Worker-Local Candidate Quotas

Rejected.

Thread-local and worker-local quotas hide ownership in runtime scheduling state.

Kontrakt's ownership authority is the engine lane.

M:N worker/lane topology is dispatch topology, not identity or quota ownership.

### 26.46. Budget Candidate Count but Not Staged Candidate Bytes

Rejected.

Candidate count does not bound provisional handles, canonical byte staging, scratch arenas, SCC-local material, sort
keys,
or bounded diagnostics.

A bounded streaming scope must meter staged bytes before issuing provisional handles.

### 26.47. Let Incremental Affected-Set Traversal Run Until Dirty Candidates Are Found

Rejected.

Dirty candidate count is not a traversal budget.

A tiny dirty set can require a large dependency-graph traversal.

Incremental affected-set mode requires explicit traversal budgets and ADR-0043 graph invalidation law.

### 26.48. Treat Interner Memory as Extra Memory Outside the Owning Pipeline Envelope

Rejected.

Interning reduces comparison and lookup cost, but it still consumes probe-table, staging, scratch, collision, transient,
and retained memory.

The accepted model charges interning memory to the owning planning, frozen, L2, contract-graph, VM, or reporting
envelope.

### 26.44. Use One Scope-Level Probe Budget Without Domain Slices

Rejected.

Interning scopes may contain identity domains with highly skewed distributions.

A single scope-level candidate count allows a hot domain to consume table capacity intended for colder domains.

ADR-0041 therefore requires per-domain candidate counts and either physical partitioning, deterministic domain slices,
or
an equivalent isolation proof.

### 26.45. Treat Logical Probe Bytes as Cache-Line Proof

Rejected.

`logicalHotSlotBytes` is budget accounting material.

It does not prove physical cache-line alignment, physical cache-line touch count, cache residency, or cache-miss
behavior.

Those claims belong to ADR-0042 substrate backend evidence.

### 26.46. Validate Load Factor Only at Initial Scope Admission

Rejected.

Streaming candidate accumulation, resize, rebuild, and migration can change the relationship between current candidate
count and current table capacity.

ADR-0041 requires insert-time and resize-time revalidation.

### 26.47. Let Cold Collision Structures Allocate Their Own Budget Lazily

Rejected.

Cold collision structures are escalation paths for adversarial or pathological identity groups.

Allowing them to allocate budget lazily from live heap state reopens resource-exhaustion behavior that this ADR is meant
to close.

### 26.48. Update Global Probe Counters on Every Hot-Loop Slot Visit

Rejected.

That would turn budget accounting into the hot-path bottleneck.

ADR-0041 requires deterministic budget enforcement, but ordinary hot loops should use loop-local primitive counters and
compare-against constants.

Detailed diagnostics may be aggregated after a bounded classification or through lane-local snapshots.

### 26.40. Leave Probe Budget as a Backend-Local Declaration

Rejected.

A backend-local declaration such as "maximum probe length must be declared" is too weak for ADR-0041.

Protocol-owned interning is a semantic publication boundary.

Therefore probe behavior requires resolved caps, integer feasibility relationships, deterministic byte accounting,
collision-amplification bounds, physical-overhead accounting, transient rebuild reserve, and stable-id independence from
physical probing.

### 26.41. Put Interner Probe Budget Only in ADR-0042

Rejected.

ADR-0042 owns physical substrate profiles.

ADR-0041 owns protocol-owned interning admission and publication.

The mathematical probe-budget contract belongs to ADR-0041 because it determines when an identity scope may publish
stable intern ids.

ADR-0042 may choose a physical probing algorithm only after proving that it satisfies the ADR-0041 budget.

### 26.42. Make Probe Displacement or Bucket Index Part of Stable Intern Id Assignment

Rejected.

Stable intern ids are assigned from collision-verified canonical material and deterministic ordering.

Bucket index, probe order, displacement, resize timing, rebuild timing, physical memory address, and backend-specific
table layout are physical placement material only.

### 26.43. Count Only Logical Slot Bytes and Ignore Physical Backend Overhead

Rejected.

Logical slot bytes are necessary but insufficient.

A backend may split one logical slot across multiple primitive arrays, off-heap regions, mapped pages, segment regions,
or generated layout regions.

Backend physical overhead, internal fragmentation, padding, allocator granularity, and transient rebuild memory must be
accounted before scope admission.

### 26.38. Use Wrapper Objects as Intern Id Storage

Rejected.

Typed wrapper objects are useful as cold semantic facades, but they are not lawful committed interner storage.

Using wrapper objects in intern tables reintroduces object headers, reference arrays, pointer chasing, interface
dispatch,
and GC pressure on the path ADR-0041 is explicitly trying to make primitive and deterministic.

The accepted shape is:

``````text
cold facade:
    optional typed view for API / diagnostics / tests

hot substrate:
    primitive id words
    primitive HID words
    primitive offsets / lengths
    table-level scope/domain/version proof
``````

### 26.39. Make the Intern Handle Interface Hierarchy the Normative Representation

Rejected.

The provisional / verified / stable state split is normative.

The object hierarchy is not.

A compliant implementation may enforce the split through primitive state bits, package-private seal APIs, generated
tables, or other architecture-tested mechanisms.

### 26.34. Make Off-Heap Storage Semantic Authority

Rejected.

Off-heap, `MemorySegment`, native allocation, and generated physical layouts are substrate backend implementations.

They may improve locality, alignment, and allocation behavior.

They do not define semantic identity.

The same canonical material must produce the same canonical bytes, HID, collision verification result, and stable intern
id under every compliant backend.

### 26.35. Require Heap Arrays for V1 Intern Tables

Rejected.

Heap primitive arrays are a portable baseline, but they are not the only lawful v1 substrate.

A v1 high-performance mechanical profile may use an aligned off-heap or `MemorySegment` backend if it proves alignment,
lifecycle safety, and cross-backend equivalence.

### 26.36. Treat Small-Inline Mixed Branching as the Default

Rejected.

Mixed inline/external branching can cause branch-thrashing on some workloads.

The default high-performance direction is segregated tables or preclassified ranges.

Mixed branching is lawful only when selected by resolved physical policy and benchmark evidence.

### 26.37. Repair SCC Two-Phase Size Mismatch by Resizing or Patching Offsets

Rejected.

A measure/write mismatch means the size-only pass and materialization pass are not equivalent under the canonical
encoder
law.

The SCC seal must fail closed before publication.

### 26.1. Use JVM `hashCode()`

Rejected.

It is not a protocol identity surface, not version-bound, not collision-verified, and not suitable for persistence or
cross-backend determinism.

### 26.2. Use backend-native ids

Rejected.

Backend-native ids violate backend erasure and adapter neutrality.

### 26.3. Use frozen ordinal as identity

Rejected.

Frozen ordinal is image-local addressing material, not stable identity.

### 26.4. Use first-seen global interning

Rejected.

First-seen interning depends on acquisition order, backend order, thread scheduling, and callback timing.

### 26.5. Use JSON canonicalization

Rejected for this ADR.

A JSON canonicalization format may be considered later for diagnostic or external artifacts, but Kontrakt's internal
protocol identity needs compact tagged binary encoding, explicit field ids, and direct byte-budget control.

### 26.6. Treat BLAKE3 digest as equality

Rejected.

Digest equality without verification would make hash collision a semantic corruption path.

### 26.7. Keep object-array structural comparison forever

Rejected.

It is acceptable as transitional migration debt, but not as SOTA target architecture.

### 26.8. Verify Full Canonical Bytes Immediately After Every HID Match

Rejected as the ordinary hot-path strategy.

Full canonical byte verification remains the final equality authority, but performing it immediately after every HID
match creates pointer chasing and cache-miss amplification.

The accepted strategy is:

``````text
HID match
-> inline domain/version/length/prefix checks
-> full canonical byte verification only on surviving candidates
``````

### 26.9. Require Exact Physical 64-Byte Alignment on JVM Heap Objects

Rejected as a protocol-level requirement.

The JVM does not provide a portable ordinary-object guarantee for exact cache-line alignment.

The accepted rule is logical cache-line grouping:

- contiguous first-probe metadata;
- primitive-friendly layout;
- no pointer chasing before inline rejection;
- optional off-heap or direct-memory implementation where policy allows.

### 26.10. Publish on BLAKE3/HID match and verify later

Rejected.

This would make semantic publication depend on probabilistic equality.

Background verification cannot retroactively repair already-published semantic identity without violating deterministic
publication law.

Kontrakt permits speculative physical preparation, but not speculative semantic publication.

### 26.11. Use CPU / NUMA topology as identity partition authority

Rejected.

CPU topology and NUMA placement are physical scheduling facts.

They may affect staging locality and throughput.

They MUST NOT affect stable identity, canonical ordering, intern id assignment, or query-key equality.

### 26.12. Let prefetch-optimized physical order define stable order

Rejected.

Physical order may follow deterministic stable order.

It must never create it.

### 26.13. Use previous-item delta encoding for identity bytes

Rejected.

Previous-item delta encoding depends on processing order.

Processing order can vary with backend traversal, parallel acquisition, local arena staging, callback completion, or
physical batching.

Kontrakt permits delta encoding only against immutable canonical bases selected by protocol-owned domain/schema/version
material.

### 26.14. Use general compression for hot identity bytes

Rejected for hot semantic identity.

General compression may be useful for cold artifacts, but the identity hot path requires bounded decoding,
branch-predictable access, and direct equality material.

A compressed artifact may store canonical bytes, but compressed bytes are not canonical identity bytes unless a future
artifact ADR ratifies a dedicated canonical compressed format.

### 26.15. Use ADR-0030 planning cycle truncation as metadata identity cycle breaker

Rejected.

ADR-0030 governs planning traversal cycle handling.

ADR-0041 governs metadata identity dependency sealing.

A metadata identity cycle must be handled by deterministic SCC sealing, not by planning breakpoint selection.

### 26.16. Fully positional canonical encoding for all domains

Rejected as the default protocol encoding.

Fully positional layout is compact, but it is brittle under schema evolution and compatibility handling.

ADR-0041 allows positional physical layout only after domain/schema/version validation, or where a later ADR ratifies a
fully closed positional domain.

---

### 26.17. Partially Publish a Failed SCC

Rejected.

Partial SCC publication would allow parent components to observe identity material from only part of a recursive
identity group.

That would make stable intern id assignment depend on failure timing, diagnostic policy, or implementation-specific seal
order.

A cyclic identity component must seal completely or fail completely.

### 26.18. Use `HID256` as the Ordinary Hot Probe Key for Every Domain

Rejected as the default.

`HID256` remains valuable for artifact summaries and cold integrity surfaces, but carrying all 256 bits in the ordinary
first-probe group can exceed the intended 64-byte logical probe budget.

Hot membership uses `HID128` plus inline verifier metadata by default.

Domains that require `HID256` in hot lookup must define a separate probe layout.

### 26.19. Scale Per-TypeReference Identity Size with Resource Profile

Rejected as the ordinary profile model.

A larger project, module, frozen image, or contract graph should normally produce more canonical units, more table rows,
more edges, and larger aggregate canonical-byte totals.

It should not normally make one `TypeReference` identity payload absorb more material.

`maxCanonicalBytesPerTypeReference` is a per-unit fuse that protects TypeReference-local identity from accidentally
including raw facts, lowered contract facts, diagnostics, annotation graphs, or the whole contract graph.

If a domain needs a different per-unit fuse, that is a versioned protocol-ratified domain exception, not ordinary
`AUTO` / `SMALL` / `STANDARD` / `LARGE` scaling and not a user configuration knob.

`AUTO` follows the same rule.

In v1, it resolves to the deterministic `STANDARD` bootstrap cap set.

In v2, it may select aggregate budgets at a stable pre-admission policy-resolution boundary using the deterministic
resource solver, but it MUST NOT silently inflate per-unit identity fuses as a substitute for splitting, layering, or
referencing canonical material.

The accepted scaling direction is:

``````text
resource profile scaling
-> more admitted units
-> more aggregate bytes
-> larger tables / slabs / diagnostic evidence budgets

not:

resource profile scaling
-> larger single TypeReference identity payload
``````

### 26.20. Add `DIAGNOSTIC` / `RESEARCH` as Resource Profiles

Rejected.

ADR-0041 uses the same public resource-profile vocabulary as planning:

``````text
AUTO
SMALL
STANDARD
LARGE
``````

Diagnostic behavior is a diagnostic evidence and reporting concern, not a separate resource-profile name.

Research-only experiments may exist as implementation-local or lab-local policy overrides, but they MUST NOT become the
public resource-profile vocabulary of this ADR.

This keeps metadata identity policy aligned with planning policy resolution and prevents documentation drift between
planning, frozen acquisition, and identity seal layers.

### 26.21. Treat `AUTO` as Live Adaptive Runtime Tuning

Rejected.

`AUTO` is a deterministic policy-resolution mode, not a live runtime tuner.

In v1, `AUTO` resolves to the deterministic `STANDARD` bootstrap cap set.

In v2, `AUTO` may use a deterministic pre-admission aggregate-budget solver, but the solver output becomes an immutable
resolved policy snapshot for the admitted scope.

The implementation MUST NOT mutate admitted caps based on GC timing, current free-memory fluctuations, current CPU load,
cache warmth, runtime throughput, thread scheduling, or other live feedback.

This keeps `AUTO` useful across local, cloud, MSA-scale, and future deep-learning-assisted environments without making
identity, publication legality, or query compatibility depend on runtime timing.

### 26.22. Treat `AnnotationDescriptor` as Canonical Contract Identity

Rejected.

Annotation descriptors are front-end / adapter-facing contract syntax surfaces.

They may carry contract meaning, but they are not the standard contract meaning.

Equivalent contract meaning may later be expressed through Reflection annotations, KSP annotations, compiler-static
metadata, bytecode metadata, generated indexes, external DSLs, or non-annotation language features.

The canonical identity surface is the lowered Kontrakt-owned contract fact, not the annotation descriptor.

### 26.23. Ratify Default / Effective Value Semantics Inside ADR-0041

Rejected.

Default handling, source presence, effective value, and frontend-specific argument semantics belong to the top-level
contract definition document, not to ADR-0041.

ADR-0041 only reserves the integration point.

Once a future contract model ratifies those semantics, the resulting material must enter ADR-0041 through canonical
bytes, HID derivation, collision verification, and protocol-owned interning.

### 26.24. Recursively Lower the Entire Annotation / Meta-Annotation Graph

Rejected.

Annotation and meta-annotation graphs are frontend syntax, diagnostics, framework metadata, or backend-specific evidence
until a contract model explicitly recognizes them.

ADR-0041 does not ratify recursive traversal of annotation object graphs as identity material.

If a future contract model defines contract syntax dependency graphs, those graphs must be explicit, bounded,
deterministic, and governed by ADR-0041's SCC seal rules.

### 26.25. Ratify a Bit-Packed Lowered-Contract-Fact Layout Before the Contract Fact Model Is Closed

Rejected.

Bit packing is a physical optimization.

It must follow the contract meaning model.

A hot lowered-contract-fact projection may be ratified only after the fact taxonomy, target model, value model,
compatibility matrix, canonical fields, and golden vectors are stable.

ADR-0041 reserves the section title and integration point, but does not freeze a lowered-contract-fact bit layout.

### 26.26. Expose Arbitrary Numeric Cap Editing to Ordinary Users

Rejected.

ADR-0041 does not make users assemble internal byte-ledger constants.

Users and operators may express coarse resource intent such as `AUTO`, `SMALL`, `STANDARD`, or `LARGE`.

Concrete numeric caps are resolved policy outputs.

They belong to a deterministic solver or released bootstrap table, not to an arbitrary user-authored cap file.

This preserves the product principle:

``````text
users own contract meaning;
the engine owns lowering, identity, state transitions, and capacity resolution discipline.
``````

### 26.27. Treat Count Caps, Byte Budgets, Table Budgets, and Unit Fuses as Independent Constants

Rejected.

Independent constants can produce impossible policies, unreachable caps, or memory envelopes that do not match the
admitted graph.

ADR-0041 requires explicit feasibility relationships among count caps, aggregate byte budgets, table/slab budgets,
per-unit fuses, and total scope budget.

This follows the planning capacity law: profile names are resource intent, while concrete caps are deterministic solver
outputs.

### 26.28. Replace BLAKE3 Identity Derivation with a Non-Cryptographic Hash

Rejected.

Non-cryptographic hashes may be fast physical hints, but they are not the ADR-0041 identity derivation root.

BLAKE3 remains the protocol-owned family for metadata digest, HID derivation, domain-separated derivation, persistent
summary roots, replay manifest roots, and future query-compatible fingerprints.

The accepted optimization is seal-time derivation plus deterministic primitive projections, not replacing the identity
root with Murmur3, xxHash, JVM hashCode, or backend-native hashes.

### 26.29. Recompute Full BLAKE3 on Every Hot Route / Probe

Rejected.

BLAKE3 remains the identity root, but the hot path should consume already-derived projections.

Repeated full-payload BLAKE3 derivation on every shard, lane, bucket, or probe decision would turn identity safety into
unnecessary instruction-pipeline cost.

### 26.30. Treat Collision Cap Overflow as Ordinary Process-Wide Panic

Rejected.

Collision cap overflow is a semantic non-publication event unless the process is corrupted or the implementation reached
an unrepresentable state.

Ordinary compliant responses include scope rejection, artifact publication failure, bounded quarantine, or a ratified
cold
collision path.

### 26.31. Use Only Worst-Case Count-Times-Fuse Products for Aggregate Budgets

Rejected.

Worst-case products are hard feasibility ceilings, not ordinary sizing formulas.

A deterministic capacity solver should publish target-average sizing relationships or an equivalent benchmark-ratified
tightening rule to avoid over-allocating aggregate budgets for every canonical unit as if each unit were near its
per-unit fuse.

### 26.32. Silently Skip Unknown Canonical Identity Tags

Rejected as the default.

Unknown canonical identity fields may carry contract meaning under a newer schema.

Silently skipping them could make different contract meanings compare as equal.

Unknown tags fail closed unless a domain compatibility matrix explicitly ratifies bounded skip behavior for a
non-critical and non-identity-affecting field.

### 26.33. Encode Provisional Handles as Canonical Wire Material

Rejected.

Provisional handles are physical seal-stage artifacts.

They are not canonical identity material.

ADR-0041 permits only `WIRE_TYPE_SCC_LOCAL_REF`, a deterministic temporary ordinal valid inside SCC seal payloads.

It cannot escape publication and cannot become a planning-visible identity.

### 26.34. Leave the Canonical Envelope Header Illustrative

Rejected.

ADR-0041 owns the common canonical byte protocol.

Therefore the v1 canonical identity envelope header cannot remain illustrative, optional, or domain-selected.

`CanonicalEnvelopeHeader` is ratified as the mandatory 64-byte little-endian common envelope header for v1 canonical
identity bytes.

Domain-specific variation belongs in:

- identity domain id;
- domain schema version;
- version-bundle fingerprint;
- field tags;
- field table entries;
- payload bytes;
- and compatibility matrices.

It does not belong in changing the common envelope header layout.

Canonical envelope headers and intern probe projections remain distinct physical surfaces.

The fixed common header does not require the intern-table probe group to share the same layout.

### 26.33Q. Use HID Descriptor Interface Objects as Hot Probe Representation

Rejected.

A sealed interface descriptor is useful as a cold boundary, test, diagnostic, or adapter representation.

It is not the ordinary hot representation for intern-table or route-table probing.

Hot probe structures use fixed-width primitive words or equivalent primitive substrate projections to avoid descriptor
allocation, virtual/interface dispatch, megamorphic equality, and avoidable branch-prediction noise.

### 26.33R. Allocate Exceptions or Logs for Every HID Version Mismatch

Rejected.

Version mismatch is a deterministic classification boundary.

The hot path returns a primitive classification without exception allocation, string concatenation, synchronous log I/O,
or cache-entry churn.

Cold diagnostics may report the mismatch later under diagnostic budget.

### 26.33S. Reuse Parent-Dependent Child HID After Parent Identity Changes

Rejected.

If parent identity participates in child HID derivation, a parent identity change invalidates the parent-dependent child
projection.

Kontrakt may reuse sealed parent-independent child canonical bytes as derivation input, but it must derive a new
parent-dependent HID under the new parent key and active version bundle before publication.

### 26.33T. Put Full Contract Graph Canonicalization into ADR-0041

Rejected.

ADR-0041 owns the stable metadata identity substrate.

Full contract graph canonicalization, structural/contextual identity semantics, sealed structural references, graph
interning, and incremental derivation are large enough to require ADR-0043.

ADR-0041 keeps only the bridge law needed to preserve identity substrate correctness.

### 26.33U. Treat Bare Child HID as a Sealed Structural Reference

Rejected.

A bare HID is a compact candidate descriptor.

A sealed structural reference must represent child-local material that has passed canonical encoding, digest-suite
derivation, collision verification, and publication under an explicit scope.

### 26.33V. Promise O(1) Subtree Invalidation

Rejected.

ADR-0041 permits fixed-width derivation per already-sealed child reference.

It does not promise constant-time invalidation for an entire affected subtree.

Any stronger incremental cutoff belongs to ADR-0043 or a future query/incremental ADR and must preserve deterministic
identity.

## 27. Final Rule

Hot HID probe representations are primitive projections, version mismatch classification is zero-allocation on the hot
path, and parent-dependent child HID projections are re-derived when parent identity changes.

Generic primitive lifecycle and asynchronous ownership governance are delegated to ADR-0042.

Canonical contract graph identity, sealed structural references, and incremental derivation semantics are delegated to
ADR-0043 while ADR-0041 keeps the digest/HID/interner bridge law.

Decoder dispatch is bounded by ratified tag space, and text validation is fail-closed and branch-bounded where hot.

Canonical encoding is deterministic and observation-independent, not stateless.

HID values are compact candidate descriptors; equality requires verification, and digest-suite domain separation is
canonical byte protocol material. BLAKE3 is the initial ratified suite implementation, not the semantic identity
contract.

Physical substrate backends may improve locality and alignment, but they are not semantic identity authority; SCC
two-phase sizing must prove exact measure/write equivalence before publication.

Intern id and handle wrappers are cold facades only; committed interning state is primitive substrate plus table-level
proof.

Interner probe behavior is admitted by ADR-0041 integer budget feasibility; ADR-0042 may change the physical probing
backend only after proving the same budget, including physical overhead and transient rebuild memory, while preserving
stable id results.

Bounded streaming owner-lane routing uses deterministic route maps, boundary-flushed routed batches, bounded route-drain
backpressure handling, and budgeted producer-local suppression; routing material never becomes equality authority.

Bounded streaming quota refill may fail only after deterministic quota reclamation proves no reclaimable unused quota
remains; transient rebuild reserve is a high-water mark, and incremental traversal exhaustion requires a bounded
diagnostic classification.

Refill-capable streaming uses deterministic reserve-pool refill before reclamation; duplicate pre-screen may reduce
staging pressure but never becomes equality authority; full-rebuild fallback requires a separately admitted preflight
before allocation or publication.

Load-factor feasibility is expressed by checked integer cross multiplication; insert-time threshold-division forms are
forbidden.

Bounded streaming interning uses engine-lane-owned quota reservation, staged-byte admission, and owning-envelope memory
accounting; it never relies on per-candidate global counters or unbounded add-on memory.

Probe budgets include per-domain admission, insert/resize revalidation, physical overhead, transient rebuild reserve,
cold-collision linkage, and hot-loop accounting boundaries; physical read-path locality remains ADR-0042 evidence.

`DigestDomainSeparationPayloadV1` is a fixed 56-byte initialized canonical protocol payload; non-applicable axes and
reserved bytes are zero-filled, never omitted or left stale.

Stable metadata identity is protocol material.

It is not backend material.

It is not cache material.

It is not diagnostic material.

It is not object identity.

It is not frozen ordinal.

It is not route64.

It is not hashCode.

The only compliant identity path is:

``````text
ratified contract-meaning material
-> canonical byte encoding
-> domain-separated BLAKE3 / HID derivation
-> collision verification
-> protocol-owned deterministic interning
-> scoped stable intern id
``````

All consumers must preserve the distinction between:

``````text
semantic equality authority
collision-verified compact identity
routing identity
local address identity
diagnostic identity
``````

Version compatibility, canonical-byte size bounds, HID width, interner scope, and metadata identity capacity are
resolved
protocol surfaces.

Resource intent is not an arbitrary cap file.

Concrete caps are deterministic resolved policy outputs, and their count/byte/table relationships must be feasible
before
scope admission.

Canonical identity bytes use `CanonicalEnvelopeHeader` as the mandatory common envelope header.

Domain payloads may vary by identity domain and schema.

The common header layout selected by `canonicalEncodingVersion32 = 1` may not.

Message nesting, object/record encoding, canonical sorting, and zero-copy slice lifetimes are also protocol safety
surfaces.

Object encoding is protocol-record encoding, not JVM object graph serialization.

Version-bundle fingerprints are derived from tagged canonical axis bytes, not delimiter-free concatenation.

Version-bundle fingerprints include identityDomainId32 and width-suffixed version axes.

Decoder progress, SCC seal payload status, canonical sort defense, and capacity arithmetic are deterministic protocol
surfaces.

Published identity slabs are immutable; fragmentation is handled by epoch ownership, not background pointer rewriting.

Staging slices and published slices are type-state separated, and epoch reclamation is reader-lease bounded.

Reader epoch reclamation is asynchronous and engine-lane-owned in the hot path; M:N worker/lane topology is dispatch
topology, not ownership, ThreadLocal/coroutine/scheduler-owned lease authority is forbidden, and leases do not cross
external blocking or scheduler handoff boundaries.

Small-inline and exact cache-line grouping are benchmark-gated physical optimizations, not semantic obligations.

Short-inline field payload layout and SCC preflight are deterministic protocol decisions.

Offset validation uses explicit bases, linear slice proofs, strict progress, and branch-bounded implementations only
when equivalent to direct fail-closed validation.

Canonical sorting must either finish exact deterministic ordering before publication or fail closed.

Tie groups are handled inside primitive scratch budgets, and bounded cold exact sorting is a pre-publication exact-order
fallback, not a planning-visible quarantine path.

Canonical identity bytes may be compact.

They may not be stateful.

The approved compactness direction is:

``````text
semantic redundancy elimination
-> schema-aware stripping
-> protocol numeric tags
-> bit-packed hot fields
-> sealed child intern references
-> optional immutable canonical-base delta
``````

The rejected compactness direction is:

``````text
run-local compression
previous-item delta
backend-order dictionary
frequency-learned dictionary
planning-truncation identity shortcut
``````

Annotation syntax follows the same law:

``````text
annotation / DSL / compiler / generated syntax surface
-> lowered Kontrakt-owned contract fact
-> canonical contract material
-> canonical bytes / HID / interning
``````

Annotation descriptors are syntax.

Lowered contract facts are identity material.

No optimization may collapse those layers.

Canonical decoding follows the same hierarchy.

Unknown tags fail closed unless compatibility explicitly ratifies a bounded skip.

Variable payloads are exposed through checked zero-copy slices on hot identity paths.

SCC-local references are local to SCC seal payloads and never become published identity.

All physical acceleration is subordinate to deterministic identity.

If performance and determinism conflict, determinism wins.

Mechanical sympathy is part of this identity law.
A compliant implementation must treat canonical encoding and protocol-owned interning as CPU-facing binary
infrastructure, not as ordinary object serialization.