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
- enforcement rules.

This ADR does not require every physical optimization to land in the first implementation patch.

It does require that all newly introduced identity material follows this law from the first implementation.

### 3.0.1. Ratified by ADR-0041 vs Deferred Elsewhere

ADR-0041 owns the common stable metadata identity substrate.

The following surfaces are ratified by ADR-0041:

| Surface                                   | Status                                                                                         |
|-------------------------------------------|------------------------------------------------------------------------------------------------|
| `CanonicalEnvelopeHeaderV1`               | ratified as the mandatory common v1 canonical identity envelope header                         |
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
    val inlineVerifierPrefixBits: Int,
    val defaultInternHidWidthBits: Int,
    val routeHidWidthBits: Int,
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
inlineVerifierPrefixBits                       = 128
defaultInternHidWidthBits                      = 128
routeHidWidthBits                              = 64
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

ADR-0041 ratifies `CanonicalEnvelopeHeaderV1` as the mandatory common envelope header for v1 canonical identity bytes.

The v1 encoding format is:

``````text
CanonicalEnvelopeHeaderV1
FieldTable
PayloadBytes
``````

`CanonicalEnvelopeHeaderV1` is always present.

`payloadLength32` is mandatory.

Domain-specific variation MUST be represented through:

- identity domain id;
- domain schema version;
- version-bundle fingerprint;
- field tags;
- field table entries;
- payload fields;
- and the owning domain compatibility matrix.

Domain-specific variation MUST NOT be represented by changing the common v1 envelope header layout.

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

A decoder MUST know whether it is currently decoding an SCC seal payload.

If `WIRE_TYPE_SCC_LOCAL_REF` appears outside an SCC seal boundary, decoding MUST fail closed.

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
- collection count is encoded before elements;
- element count must be bounded by the owning capacity policy;
- null element policy must be explicit per domain.

### 8.5.1. Canonical Collection Sorting Cost Law

Canonical sorting is a DoS boundary.

Unordered collection canonicalization MUST be deterministic, but it MUST NOT become an unbounded recursive-comparator
path.

A compliant encoder MUST bound:

- collection element count;
- canonical sort-key byte length;
- comparator fallback depth;
- total canonical bytes read during tie-breaking;
- and total sort work admitted for one canonical unit or SCC seal payload.

The ordinary sorting path SHOULD precompute bounded canonical sort keys before sorting.

Accepted sort-key material includes:

- identity domain id;
- domain schema / version bundle fingerprint;
- canonical byte length;
- HID or digest projection where already available at that boundary;
- inline verifier prefix;
- fixed-width canonical key prelude;
- and a bounded canonical byte slice only as a final tie-breaker.

Forbidden ordinary sorting path:

``````text
sort comparator
-> recursively walk full metadata graph
-> compare deep child structures repeatedly
-> repeat for O(n log n) or worse comparator calls
``````

Required ordinary direction:

``````text
element canonical material
-> bounded canonical sort key
-> deterministic sort by sort key
-> metered full canonical byte tie-break only when needed
``````

Full canonical byte comparison remains the final deterministic tie-break authority.

It must be metered and cap-bounded.

A released implementation MUST provide golden vectors or tests proving that shuffled unordered inputs produce the same
encoded bytes without relying on backend iteration order or recursive graph traversal timing.

### 8.6. Object Encoding

Object encoding rules:

- object type is encoded by domain tag / schema id, not by JVM class name;
- implementation class names are forbidden unless they are semantic material in that domain;
- field absence is meaningful only when the domain declares it meaningful;
- default values must be encoded explicitly or prohibited;
- unknown fields in persisted payloads require a future compatibility policy and are not accepted silently by this ADR.

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

Every v1 identity envelope MUST begin with `CanonicalEnvelopeHeaderV1`.

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

ADR-0041 ratifies `CanonicalEnvelopeHeaderV1` as a fixed 64-byte little-endian common header.

The exact v1 layout is:

``````text
CanonicalEnvelopeHeaderV1 = 64 bytes, little-endian

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

Mandatory v1 header constants and validation rules:

- `magic32` MUST be `0x4B4E5443`;
- `magic32` represents ASCII `KNTC`;
- `headerSize16` MUST be `64`;
- `headerFlags16` MUST be `0x0000` in v1 unless the active compatibility matrix ratifies a specific flag;
- `reserved16` MUST be zero;
- `reserved32` MUST be zero;
- `payloadLength32` is mandatory;
- all integer fields are little-endian unsigned bit patterns unless the field explicitly states otherwise;
- a decoder MUST fail closed if `magic32`, `headerSize16`, `reserved16`, or `reserved32` is invalid;
- unknown non-zero `headerFlags16` bits MUST fail closed unless ratified by the active compatibility matrix;
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

They MUST NOT change the common v1 envelope header layout.

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

`CanonicalEnvelopeHeaderV1` is fixed by ADR-0041.

The following header fields are reserved in v1:

- `reserved16`;
- `reserved32`;
- every `headerFlags16` bit not ratified by the active compatibility matrix.

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

Required derivation shape:

``````text
CanonicalIdentityVersionBundle
-> canonical version-bundle byte payload
-> domain-separated BLAKE3 keyed derivation / XOF
-> first 128 bits
-> high64 / low64 encoded as little-endian unsigned bit patterns
``````

The canonical version-bundle byte payload MUST include:

- identity domain id;
- identity domain version;
- canonical encoding version;
- domain schema version;
- normalization version where applicable;
- hash algorithm suite id/version;
- HID derivation version;
- interning protocol version where applicable;
- compatibility class id;
- and every domain-specific version axis in ascending protocol field-tag order.

The version-bundle byte payload MUST NOT include:

- display strings;
- backend names;
- source locations;
- runtime object identity;
- map iteration order;
- set iteration order;
- process-global registry order;
- or diagnostic labels.

The BLAKE3 derivation context MUST be domain-separated from ordinary HID derivation.

Illustrative derivation context:

``````text
KONTRAKT_BLAKE3_VERSION_BUNDLE_FINGERPRINT_V1
``````

A released implementation MUST publish golden vectors for:

- identical bundle -> identical fingerprint;
- canonical encoding version bump;
- domain schema version bump;
- HID derivation version bump;
- compatibility class change;
- domain-specific version-axis change;
- field-order shuffle preserving identical canonical version-bundle bytes.

Two implementations that receive the same active version bundle MUST compute the same
`VersionBundleFingerprint128`.

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

### 8.10.1. Checked Offset and Length Arithmetic Law

Offset tables are a security boundary.

All offset and length arithmetic MUST use checked `Long` arithmetic before narrowing to any encoded field width.

A canonical encoder MUST fail closed before emission if any offset, length, table size, or total payload size cannot be
represented by the ratified encoded width.

A canonical decoder MUST validate, before exposing any slice:

- offset is non-negative;
- length is non-negative;
- `offset + length` does not overflow;
- `offset + length <= payloadLength`;
- `fieldTableOffset + fieldTableByteLength` does not overflow;
- `fieldTableOffset + fieldTableByteLength <= envelopeLength`;
- every offset points into the declared payload region, not into the hot header, field table, or reserved padding;
- repeated-field element offsets are monotonic where the domain requires monotonic layout;
- and no slice may overlap another slice unless the domain explicitly ratifies overlapping immutable views.

Forbidden:

``````text
Int offset = previousOffset + fieldLength
// silent wraparound
``````

Required:

``````text
checked Long arithmetic
-> range validation
-> safe narrowing only after validation
-> bounded slice exposure
``````

Integer overflow, negative offset, negative length, out-of-payload range, header overlap, or malformed field-table
length
MUST fail closed.

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

### 8.11. Decoder Dispatch and Branch Discipline

Canonical tag decoding MUST be table-driven or switch-table-friendly.

Field tags SHOULD be dense enough for direct table indexing or jump-table-like lowering where the target runtime can
support it.

The hot successful decode path SHOULD avoid unpredictable chained semantic `if/else` dispatch.

Forbidden on the hot decode path:

- reflection-based field dispatch;
- string tag lookup;
- `Map` lookup by tag;
- delimiter scanning;
- backend-specific descriptor parsing;
- generic JSON object traversal.

Allowed:

- dense numeric tag indexing;
- generated decoder tables;
- `tableswitch` / `lookupswitch`-friendly dispatch;
- bit-mask extraction of wire type, field family, criticality, and fast flags;
- fail-closed validation branches for unknown tags, invalid wire types, bounds violations, or version mismatch.

The goal is branch-minimal deterministic decoding, not the removal of all validation branches.

A validation branch is lawful when it protects:

- bounds safety;
- unknown field rejection;
- critical field presence;
- schema compatibility;
- canonical ordering validation;
- duplicate field rejection;
- integer overflow rejection;
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

### 8.15. Stateless Canonical Encoding Law

Canonical identity encoding MUST be stateless with respect to:

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
- deterministic version-bundle compatibility data.

An encoder MUST NOT use:

- mutable run-local observation order;
- previous item state;
- recently seen item state;
- adaptive dictionaries learned from the current run;
- thread-local previous values;
- backend enumeration neighbors;
- acquisition-batch neighbors;
- compression bases chosen by observed frequency.

Allowed shape:

``````text
canonical material
-> domain/schema/version-ratified encoding rule
-> canonical bytes
``````

Forbidden shape:

``````text
canonical material
-> compare against previous encoded item
-> emit shorter run-order-dependent delta
``````

Reason:

Order-sensitive compression is poison for deterministic identity.

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

## 9. BLAKE3 Suite Law

### 9.1. Default Suite

Kontrakt standardizes on the BLAKE3 family for metadata identity digest and derivation surfaces.

The initial suite is:

``````text
algorithmFamily = BLAKE3
algorithmId = KONTRAKT_BLAKE3_METADATA_IDENTITY
algorithmVersion = 1
``````

This suite governs:

- canonical metadata digest;
- HID derivation;
- keyed derivation;
- deterministic entropy derivation;
- deterministic UUID payload derivation where metadata participates;
- future frozen image content summary roots;
- replay manifest summary roots.

### 9.2. Adapter Boundary

Adapters do not become BLAKE3 authorities.

Backends may optimize locally, but planning-visible or frozen-visible digest material must be semantically equivalent to
this ADR's BLAKE3 suite.

Forbidden:

- backend-specific hashing as semantic identity;
- JVM `hashCode()` as digest input;
- backend manifest digest as Kontrakt digest unless re-verified through this ADR;
- classloader-stable hash as Kontrakt identity.

### 9.3. Domain Separation

Every BLAKE3 invocation used for identity must include domain separation.

Domain separation MUST include:

- Kontrakt protocol marker;
- identity domain id;
- identity domain version;
- canonical encoding version;
- hash algorithm suite id/version;
- relevant schema version;
- relevant semantic version tuple.

Different domains MUST produce different outputs even for identical payload bytes.

### 9.4. Keyed Derivation vs Plain Digest

Kontrakt distinguishes:

- plain digest over canonical bytes;
- keyed derivation for HID/entropy surfaces;
- parent-child hierarchical derivation;
- XOF expansion where explicitly ratified.

Plain digest is suitable for:

- content summary;
- collision candidate grouping;
- manifest evidence.

Keyed derivation is required for:

- HID;
- parent-child entropy derivation;
- deterministic UUID materialization;
- local selector derivation;
- domain-separated primitive routing identity where a short width is emitted.

### 9.5. Width Law

BLAKE3 output width must be explicit.

Ratified widths:

``````text
HID64   = 64-bit compact routing / table acceleration only
HID128  = 128-bit ordinary compact identity descriptor
HID256  = 256-bit strong compact identity descriptor / digest-equivalent descriptor
``````

Rules:

- `HID64` is never semantic equality authority;
- `HID128` is not semantic equality authority without verification;
- `HID256` is still not semantic equality authority without verification unless a later ADR explicitly ratifies
  digest-only equality for a non-semantic surface;
- route64 may be derived from HID material but remains routing-only;
- width truncation must be deterministic and version-bound.

### 9.6. HID Width Selection Law

Protocol-owned intern membership MUST use `HID128` or wider by default.

`HID64` is routing-only unless a domain-specific cardinality proof allows a narrower compact identity for a non-semantic
surface.

Approximate collision probability must be reasoned from cardinality:

``````text
p ≈ n(n - 1) / 2^(b + 1)

where:
    n = expected candidate count inside the identity scope
    b = compact identity width in bits
    p = approximate birthday-bound collision probability
``````

Required safety condition:

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

The conservative default remains `HID128` for ordinary intern-table compact identity, even when the calculated minimum
would be smaller.

Default interpretation:

``````text
HID64:
    route / shard / bucket index only

HID128:
    default intern-table compact identity

HID256:
    strong artifact digest, persistent summary, or cold verification surface
``````

A domain that uses `HID64` for anything beyond routing must document:

- expected maximum cardinality;
- target collision probability;
- collision handling path;
- why `HID128` is unnecessary for that surface.

### 9.7. BLAKE3 Seal-Time Derivation and Hot Projection Law

BLAKE3 remains the protocol-owned metadata identity derivation root.

ADR-0041 does not replace BLAKE3 with non-cryptographic hashes for canonical identity, HID derivation, persistent
summaries, replay manifests, or future query fingerprints.

However, hot lookup paths MUST NOT repeatedly re-encode full canonical material and re-run full BLAKE3 derivation for
every shard, lane, bucket, or probe decision.

The lawful shape is:

``````text
canonical material
-> canonical bytes
-> BLAKE3 digest / HID derivation at seal or materialization boundary
-> deterministic primitive projections
-> route64 / shard bits / inline verifier prefix / probe metadata
-> hot lookup reads the projections
``````

The unlawful shape is:

``````text
every hot route/probe operation
-> re-create canonical bytes
-> re-run full BLAKE3 over the full payload
-> truncate again for route/probe
``````

Non-cryptographic hashes such as Murmur3 or xxHash MAY be used only as explicitly non-semantic physical hints over
already-ratified or already-verified material.

They MUST NOT become:

- HID replacement;
- persistent identity;
- collision verification authority;
- query fingerprint authority;
- stable intern id assignment authority;
- PlanCacheKey equality authority;
- or canonical material equality authority.

A future domain may migrate a physical route projection from a non-cryptographic route hash to a BLAKE3-derived route
projection only through a versioned route-derivation amendment, golden vectors, and a migration boundary.

---

## 10. HID Law

### 10.1. Definition

HID is a domain-separated compact descriptor derived from canonical material.

Illustrative shape:

``````kotlin
sealed interface HashedIdentityDescriptor {
    val domain: IdentityDomain
    val algorithmId: String
    val algorithmVersion: Long
    val encodingVersion: Long
    val derivationVersion: Long
    val widthBits: Int
}

class Hid64 private constructor(
    override val domain: IdentityDomain,
    override val algorithmId: String,
    override val algorithmVersion: Long,
    override val encodingVersion: Long,
    override val derivationVersion: Long,
    val bits: Long,
) : HashedIdentityDescriptor

class Hid128 private constructor(
    override val domain: IdentityDomain,
    override val algorithmId: String,
    override val algorithmVersion: Long,
    override val encodingVersion: Long,
    override val derivationVersion: Long,
    val highBits: Long,
    val lowBits: Long,
) : HashedIdentityDescriptor

class Hid256 private constructor(
    override val domain: IdentityDomain,
    override val algorithmId: String,
    override val algorithmVersion: Long,
    override val encodingVersion: Long,
    override val derivationVersion: Long,
    val word0: Long,
    val word1: Long,
    val word2: Long,
    val word3: Long,
) : HashedIdentityDescriptor
``````

The exact API may differ.

The invariant is normative.

Hot-path HID material MUST be represented by fixed-width primitive words, not by `ByteArray` object references.

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
    BLAKE3_KEYED_DERIVE(
        key   = domain_separated_parent_key,
        input = child_material,
        width = ratified_width
    )
``````

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

Illustrative shape:

``````kotlin
class TypeReferenceInternId private constructor(
    val scope: InternScope,
    val id: Int,
    val identityDomain: IdentityDomain,
    val interningProtocolVersion: Long,
)
``````

Rules:

- `id >= 0`;
- `id` is dense inside its declared scope;
- `id` is not semantic equality authority alone;
- `id` may index arrays only after table integrity validation;
- `id` must not be persisted without scope/version metadata.

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

A compliant implementation MUST define bounded probe behavior.

At minimum:

- maximum probe length or displacement policy must be declared;
- saturated segment behavior must fail closed or degrade through a non-semantic path;
- resize / rebuild behavior must not change stable id assignment;
- collision-chain amplification must be budgeted;
- pathological collision groups must be detected and surfaced.

Stable intern id assignment remains deterministic even if the physical table rebuilds, resizes, or changes probing
strategy.

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
8. small inline canonical byte comparison where applicable
9. full canonical byte comparison
``````

The implementation MUST NOT read the canonical byte slab before the candidate survives the cache-local verification
stages, except for explicitly measured and justified small-table implementations.

A failure at any verification stage rejects the candidate only for the current equality check.

It MUST NOT mutate semantic identity material.

### 13.18. Small Canonical Bytes Inline Law

A compliant high-performance implementation SHOULD inline small canonical byte payloads, or their word-equivalent
representation, inside the intern metadata plane.

If the complete canonical byte payload fits within the implementation's small-inline threshold, equality MAY be verified
without chasing the canonical byte slab.

The small-inline threshold is a physical implementation policy and MUST be benchmarked.

The small-inline representation MUST be byte-exact and MUST NOT use:

- display strings;
- object identity;
- backend-native handles;
- source text that bypassed canonical ratification;
- delimiter-joined material.

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

The implementation SHOULD enforce this with distinct type-state surfaces.

Illustrative shape:

``````kotlin
sealed interface InternHandle

sealed interface ProvisionalInternHandle : InternHandle {
    val scopeId: InternScopeId
}

sealed interface VerifiedInternHandle : InternHandle {
    val scopeId: InternScopeId
    val verificationEpoch: InternVerificationEpoch
}

sealed interface StableInternId : VerifiedInternHandle {
    val id: Int
    val domain: IdentityDomain
}
``````

The exact API may differ, but the type-state split is normative.

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

An implementation SHOULD fail an SCC as early as failure is deterministically provable.

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

`TypeReferenceInternId` is assigned only by a protocol-owned interner.

It may be used for:

- frozen frontier primitive membership;
- frozen table addressing after validated mapping;
- planning provider lookup acceleration;
- deterministic sort acceleration;
- direct-to-slab future lowering.

It must not replace TypeReference semantic equality without table validation.

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
- verified `TypeReferenceInternId`;
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
metamodel.domain.identity.CanonicalEnvelopeHeaderV1
metamodel.domain.identity.CanonicalEnvelopeHeaderValidator
metamodel.domain.identity.CanonicalByteEncoder
metamodel.domain.identity.CanonicalByteSink
metamodel.domain.identity.CanonicalEncodingVersion
metamodel.domain.identity.CanonicalEncodingDomain
metamodel.domain.identity.CanonicalFieldTag
metamodel.domain.identity.CanonicalWireType
metamodel.domain.identity.CanonicalFieldTableEntry
metamodel.domain.identity.CanonicalEncodedBytes
metamodel.domain.identity.VersionBundleFingerprintDeriver
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
metamodel.domain.identity.TypeReferenceInternId
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
metamodel.domain.identity.ProvisionalInternHandle
metamodel.domain.identity.VerifiedInternHandle
metamodel.domain.identity.StableInternId
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
- stateless encoder checks;
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

31. Canonical identity envelopes use `CanonicalEnvelopeHeaderV1` as the mandatory 64-byte little-endian v1 common
    header.
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
72. Canonical identity encoding is stateless with respect to previous item, recently seen item, and acquisition order.
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
- `CanonicalEnvelopeHeaderV1` 64-byte layout fixture;
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
- encoding version change changes output;
- HID width bound fixture satisfying `n(n - 1) / 2^(b + 1) <= p_target`;
- seal-time BLAKE3 derivation with deterministic route64 projection;
- hot route/probe path proving no full canonical-material rehash is required after seal-time projection.

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
- stateless encoding under shuffled acquisition order;
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
- protocol ids are not enum ordinals;
- no process-global mutable interner exists for semantic identity.

- canonical decoder hot paths do not use reflection dispatch;
- canonical decoder hot paths do not use string tag lookup;
- canonical decoder hot paths do not use `Map` lookup for field dispatch;
- identity envelope decoding uses `CanonicalEnvelopeHeaderV1` and rejects non-v1 common header layouts;
- header validation rejects invalid `magic32`, non-64 `headerSize16`, non-zero v1 `headerFlags16`, non-zero
  `reserved16`, and non-zero `reserved32`;
- version-bundle fingerprint derivation is covered by golden vectors and does not depend on map/set iteration order;
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
- stateless encoder tests reject previous-item, recently-seen, or acquisition-order dependent encoding;
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
- capacity solver outputs include target-average sizing evidence or an explicitly documented equivalent tightening
  relationship.

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

`CanonicalEnvelopeHeaderV1` is ratified as the mandatory 64-byte little-endian common envelope header for v1 canonical
identity bytes.

Domain-specific variation belongs in:

- identity domain id;
- domain schema version;
- version-bundle fingerprint;
- field tags;
- field table entries;
- payload bytes;
- and compatibility matrices.

It does not belong in changing the common v1 envelope header layout.

Canonical envelope headers and intern probe projections remain distinct physical surfaces.

The fixed common header does not require the intern-table probe group to share the same layout.

## 27. Final Rule

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

Canonical identity bytes use `CanonicalEnvelopeHeaderV1` as the mandatory common v1 envelope header.

Domain payloads may vary by identity domain and schema.

The common v1 header may not.

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