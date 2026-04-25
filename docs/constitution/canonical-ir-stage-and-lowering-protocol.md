# Design Note: Canonical IR Stage and Lowering Protocol

## Status

Proposed

## Date

2026-04-23

## Related

- `docs/constitution/compiler-core-protocols.md`
- ADR-0030: Edge-Aware Deterministic Cycle Truncation Strategy
- ADR-0031: Two-Tier Transactional Memoization and Structural Interning
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- ADR-0038: Interface Contract Polymorphic Expansion and Non-Composite Type Expansion Completion
- `docs/design/deterministic-active-member-projection-and-ordering-protocol.md`
- `docs/design/type-expansion-decision-and-synthetic-edge-protocol.md`

## 1. Purpose

This document defines the meaning of **Canonical**, **IR**, and **Lowering** inside Kontrakt.

Kontrakt is not merely a library that creates test objects.
It is a deterministic compiler-like engine that lowers user contracts, metadata, runtime binding snapshots, and policy
snapshots into replayable planning IR.

Therefore, the words `Canonical`, `IR`, `Local`, `Raw`, `Projected`, `Payload`, `Signature`, `EdgeKey`, and `Committed`
must not be used casually.

This document prevents four classes of drift:

1. **Semantic drift**
    - the same model receives different identity / signature / ordering depending on adapter or runtime path.

2. **Naming drift**
    - `Canonical` is attached to values that are merely sorted, immutable, or diagnostic.

3. **Pipeline drift**
    - raw facts, projected facts, local planner state, canonical IR, and serialized payloads leak across phase
      boundaries.

4. **Version drift**
    - canonical bytes, signatures, digests, and cache keys silently change after algorithm or schema upgrades.

## 2. Kontrakt Definition of IR

In Kontrakt, **IR** means:

> A framework-owned intermediate representation used by the compiler-like planning engine to carry semantic structure
> across deterministic lowering stages.

IR is not synonymous with:

- DTO,
- AST,
- runtime object,
- serialized payload,
- adapter fact,
- diagnostic report.

An IR value must have an explicit stage.

Kontrakt recognizes the following IR-related stages:

``````text
Raw Adapter Material
-> Normalized Fact
-> Projected Semantic Model
-> Local IR
-> Canonical IR
-> Committed Result
-> Payload / Report Artifact
``````

Each stage has different ownership, mutability, visibility, and authority.

## 3. Kontrakt Definition of Canonical

In Kontrakt, **Canonical** means:

> Adapter-independent, normalized, protocol-ratified, deterministic material that participates in equality, ordering,
> signature, digest, identity, interning, replay, or cache-correctness law.

A value may use the `Canonical` prefix only if all of the following are true:

1. It is independent from reflection / KSP / bytecode enumeration order.
2. It is independent from JVM object identity.
3. It is independent from mutable runtime state.
4. It is normalized according to the ratified protocol for its surface.
5. It participates in deterministic equality, ordering, signature, digest, replay, or interning law.
6. It has a version-pinned lowering rule where byte material is produced.
7. It is safe to use as part of cache-blind semantic identity.

## 4. Canonical Is Not

The following are not canonical merely by being immutable or sorted:

- raw adapter DTOs;
- reflection/KSP/bytecode-native facts;
- JVM `Class`, `Method`, `Constructor`, or `KClass` objects;
- local planner frames;
- temporary expansion buffers;
- diagnostic renderings;
- display labels;
- arbitrary immutable collections;
- source-order snapshots;
- data copied from user code without protocol validation.

A sorted value is not automatically canonical.

A value becomes canonical only when it is:

1. normalized,
2. validated,
3. adapter-independent,
4. version-bound,
5. equality/signature/digest relevant,
6. and accepted by a ratified protocol boundary.

## 5. Why Kontrakt Does Not Expose a Thick AST

Most general-purpose compilers have an AST because source syntax is their primary input.

Kontrakt's primary semantic inputs are different:

- interface-level behavioral contracts;
- normalized type metadata;
- runtime binding snapshots;
- policy snapshots;
- deterministic seed/replay surfaces;
- planning expansion decisions.

Therefore, Kontrakt does not require a large public AST as its main user-facing model.

Kontrakt may internally use AST-like structures for annotations, contract clauses, or future static-analysis plugins,
but
the planning engine's main pipeline is not:

``````text
Source Text -> AST -> IR
``````

The planning engine's main pipeline is:

``````text
Contract / Metadata / Policy / Binding Snapshot
-> Normalized Facts
-> Projected Semantic Model
-> Local IR
-> Canonical IR
``````

### 5.1 Thin AST Rule

If AST-like structures exist in Kontrakt, they are thin front-end material.

They must not become Canonical IR.

They must be lowered into normalized facts or projected semantic models before planning.

### 5.2 Why This Is Intentional

This keeps the planner:

- independent from source parser details;
- independent from reflection/KSP/bytecode backend shape;
- independent from annotation syntax evolution;
- compatible with future external JSON / declarative contract surfaces;
- focused on semantic lowering rather than syntax retention.

## 6. Compiler Domain vs Kontrakt Domain Mapping

| Compiler Concept       | Kontrakt Equivalent                                          | Notes                                                                    |
|------------------------|--------------------------------------------------------------|--------------------------------------------------------------------------|
| Source file            | Interface contract / annotation / metadata input             | Kontrakt input may not be source text.                                   |
| AST                    | Thin contract syntax tree, if needed                         | Not the primary planning representation.                                 |
| Symbol table           | TypeReference / binding snapshot / implementation set        | Must be adapter-independent after normalization.                         |
| Semantic analysis      | Projection / active member selection / constructor selection | Produces projected semantic model.                                       |
| IR                     | Local IR / Canonical IR                                      | Planning-owned representation.                                           |
| Green tree             | Canonical IR                                                 | Parent-free, path-free, immutable, internable.                           |
| Red tree               | Local IR / contextual frame material                         | Parent/path/session-local material allowed only before canonicalization. |
| Lowering pass          | TypeExpansionPipeline / assemblers / commit path             | Deterministic phase conversion.                                          |
| Verifier pass          | CanonicalizationVerifier                                     | Fails closed on IR integrity violation.                                  |
| Codegen                | Payload / execution frame / report materialization           | Not canonical IR.                                                        |
| Object file / artifact | Replay payload / report artifact / persistent cache entry    | Post-mortem, not live IR.                                                |

## 7. Stage Taxonomy

### 7.1 Raw Adapter Material

Raw adapter material is backend-originated.

Examples:

- reflection-discovered constructor facts;
- KSP symbol facts;
- bytecode-scanned member facts;
- static-source index facts;
- runtime DI binding records;
- deserialized payload records.

Rules:

- raw material is adapter-owned;
- raw material may contain backend-native concepts;
- raw material must not be consumed directly by canonical IR;
- raw material must not carry planning authority;
- raw material must be lowered into normalized facts before projection.

### 7.2 Normalized Fact

Normalized facts are adapter-independent facts after canonical validation and normalization.

Examples:

- canonical FQCN;
- canonical type reference;
- normalized nullability kind;
- normalized visibility kind;
- stable declaration ordinal or explicit unavailable marker;
- normalized constructor candidate fact;
- normalized property candidate fact.

Rules:

- normalized facts may be consumed by domain projection;
- normalized facts are not yet canonical IR;
- normalized facts may still be rejected during projection;
- normalized facts must not own backend runtime objects.

### 7.3 Runtime Object Ban After Normalization

After the normalized fact boundary, domain/planning values must not own:

- `java.lang.Class`;
- `java.lang.reflect.Method`;
- `java.lang.reflect.Constructor`;
- `java.lang.reflect.Field`;
- `kotlin.reflect.KClass`;
- `kotlin.reflect.KType`;
- KSP symbols;
- bytecode library handles;
- DI container handles.

Allowed representations after normalization:

- `TypeReference`;
- canonical string identifiers;
- stable enum/value-object facts;
- primitive identity bits;
- immutable snapshots;
- protocol-ratified DTO/value objects.

Reason:

Backend runtime objects carry environment identity, classloader identity, reflection enumeration behavior, and runtime
state that are not canonical planning material.

### 7.4 Projected Semantic Model

Projected semantic model is the domain-owned interpretation of normalized facts.

Examples:

- selected constructor;
- projected active members;
- ordered active members;
- polymorphic implementation candidate set;
- synthetic edge descriptors;
- atomic equality material;
- map canonical entry descriptors.

Rules:

- projection must be deterministic;
- projection must reject ambiguity;
- projection must not depend on adapter enumeration order;
- projection may produce canonical material;
- projection is not automatically Local IR;
- projection is not automatically Canonical IR.

### 7.5 Local IR

Local IR is session-local construction material.

Examples:

- partially assembled plan nodes;
- pending child slots;
- explicit stack frames;
- local allocation frames;
- cycle-break assembly state;
- local selector tuples produced for HID materialization;
- local payload assembly buffers.

Rules:

- Local IR may reference current planner session state.
- Local IR may contain temporary construction context.
- Local IR may use stack-local diagnostics.
- Local IR must be unreachable after session reset.
- Local IR must not be published.
- Local IR must not be stored in L2.
- Local IR must not be returned to the user.

### 7.6 Canonical IR

Canonical IR is committed, deeply immutable, context-free semantic structure.

Examples:

- `CanonicalPlanNode`;
- canonical structural node body;
- canonical atomic node body;
- canonical container node body;
- canonical cycle-break result body, if ratified by semantic assembly law.

Rules:

- Canonical IR has no parent pointer.
- Canonical IR has no absolute path.
- Canonical IR has no stack index.
- Canonical IR has no session reference.
- Canonical IR has no adapter reference.
- Canonical IR has no mutable collection.
- Canonical IR has no hidden lazy computation.
- Canonical IR is safe for cross-thread publication only through the approved publication boundary.

### 7.7 Committed Result

Committed result is the externally consumable planning result.

A committed result may carry:

- canonical node reference;
- semantic cost bound;
- deferred/cycle/substitution state;
- reporting identity.

Committed wrapper types are carriers.

They must not hide semantic-cost defaults or canonicalization policy inside local constants.

### 7.8 Payload / Report Artifact

Payload and report artifacts are serialized or diagnostic representations.

They are **post-mortem artifacts**.

They are not live IR.

They may contain:

- canonical signatures;
- digests;
- seed surfaces;
- policy snapshot IDs;
- replay handles;
- report labels;
- persistent cache keys.

They must not be treated as canonical node objects.

## 8. Stage Directionality

The canonical planning pipeline is one-way.

Allowed direction:

``````text
Raw
-> Normalized
-> Projected
-> Local
-> Canonical
-> Committed
-> Payload
``````

Forbidden direction:

``````text
Payload
-> Committed
-> Canonical
-> Local
-> Projected
``````

### 8.1 Payload Is Post-Mortem

A payload is a post-mortem representation of a prior planning result.

It must never re-enter planning as live IR.

If a payload is used as replay input, it must be treated as raw payload material and must pass through the full
promotion
pipeline again:

``````text
Payload
-> RawPayloadNode
-> validation
-> normalized material
-> Local IR
-> commit()
-> L2 intern()
-> CanonicalPlanNode
``````

The old payload object itself never becomes canonical.

### 8.2 Unidirectionality Rule

No stage may directly construct an earlier-stage value as a way to bypass validation.

Examples of forbidden shortcuts:

- payload directly implements `CanonicalPlanNode`;
- normalized fact embeds `KType` and defers interpretation to planning;
- projected member stores reflection `Method`;
- committed result reconstructs Local IR;
- report artifact is reused as canonical node.

## 9. Red-Green Discipline

Kontrakt adopts a Red-Green style discipline for planner IR.

This document uses the terms **Green** and **Red** only as architecture analogies.
The normative Kontrakt terms are **Canonical IR** and **Local IR**.

### 9.1 Green-Like Canonical IR

Canonical IR is green-like.

It is:

- parent-free;
- path-free;
- context-free;
- deeply immutable;
- internable;
- structurally shareable;
- replay-stable.

A Canonical IR node must not know where it is used.

Therefore, the following are forbidden inside Canonical IR:

- parent pointer;
- root pointer;
- stack index;
- node path;
- traversal depth;
- current edge label;
- source order index unless part of ratified semantic identity;
- session ID;
- worker ID;
- telemetry handle;
- adapter handle;
- runtime binding snapshot object;
- wall-clock timestamp;
- random UUID.

### 9.2 Red-Like Local IR

Local IR is red-like.

It may carry local context while the planner is building or traversing.

Examples:

- parent frame;
- child index;
- pending edge;
- stack-local path for diagnostics;
- construction slot;
- local assembler state;
- local HID selector input.

Local IR exists only inside a logical planning session.

Local IR is erased or promoted through canonicalization before publication.

### 9.3 Context-Free Canonical Rule

Canonical IR must be context-free.

If a value's meaning changes when moved to another parent, another path, another stack index, or another traversal
episode, it is not canonical IR.

If path-sensitive material is needed for diagnostics or HID entropy derivation, it must live outside Canonical IR.

## 10. Projected-to-Local Promotion

### 10.1 Projection Does Not Allocate Canonical IR

Projected semantic model selects and orders meaning.

It does not publish canonical structure.

Examples:

- `SelectedConstructor` selects constructor semantics.
- `OrderedActiveMembers` freezes member traversal order.
- `PolymorphicImplementationSet` freezes implementation candidate order.
- `AtomicEqualityMaterial` freezes equality material.
- `CanonicalMapEntry` freezes map entry semantics.

These are semantic materials.

They are inputs to Local IR assembly.

### 10.2 LocalSelectorTuple Creation Point

`LocalSelectorTuple` belongs at the Local IR assembly boundary.

It is created when a projected semantic element becomes a concrete expansion obligation.

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

- projection may define semantic member identity;
- Local IR assembly creates local selector tuples;
- HID derivation uses parent deterministic entropy / parent semantic identity plus local selector tuple;
- LocalSelectorTuple must not be stored inside Canonical IR unless explicitly part of canonical signature material.

### 10.3 Signature Finalization Point

Canonical signature is finalized at the Local-to-Canonical boundary.

The sequence is:

``````text
Projected Semantic Model
-> Local IR assembly
-> LocalSelectorTuple creation where needed
-> child canonicalization
-> canonical signature generation
-> canonicalization verification
-> L2 interning
-> Canonical IR
``````

Projected semantic model may provide inputs to signature generation.
It does not by itself finalize the canonical signature.

Local IR may accumulate signature-relevant material.
It does not become canonical until the verifier and interner accept it.

## 11. Canonical Naming Discipline

### 11.1 Allowed `Canonical*` Names

Use `Canonical` only for protocol-ratified canonical material.

Allowed examples:

- `CanonicalPlanNode`
- `CanonicalSignature`
- `CanonicalIdentifier`
- `CanonicalEdgeKey`
- `CanonicalMapEntry`
- `CanonicalMapEntries`
- `CanonicalStringOrder`
- `CanonicalIdentifierOrder`

### 11.2 Disallowed `Canonical*` Names

Do not use `Canonical` for:

- generic immutable collections;
- temporary buffers;
- local frame state;
- raw DTOs;
- diagnostic labels;
- display strings;
- adapter-native values;
- merely sorted sequences.

### 11.3 Law / Protocol / Surface Naming

Use `Law` or `Protocol` for rules.

Examples:

- `CanonicalTextLaw`
- `OrderingLaw`
- `EncodingLaw`
- `SyntheticEdgeProtocol`

Use `Surface` for externally supplied but protocol-ratified input surfaces.

Examples:

- `DeterministicSeedSurface`
- `CapabilityProfile`

Use `Snapshot` for run-ratified, frozen environment/runtime captures.

Examples:

- `RuntimeBindingSnapshot`
- `PolicySnapshot`
- `VersionBundle`

Use `Sequence`, `Ordered`, or domain-specific names for deterministic containers that do not themselves define global
canonical identity.

Examples:

- `ExpansionSequence`
- `OrderedSyntheticEdges`
- `ProjectedActiveMembers`

### 11.4 Canonical Does Not Mean Sorted

A sorted value is not automatically canonical.

A value becomes canonical only when it is:

1. normalized,
2. validated,
3. adapter-independent,
4. version-bound,
5. equality/signature/digest relevant,
6. and accepted by a ratified protocol boundary.

## 12. Local-to-Canonical Transition Law

### 12.1 Interning as the Canonicalizer

Structural interning is not merely caching.

In Kontrakt, interning is the canonicalization bridge.

A Local IR node may become Canonical IR only through the authorized commit and interning path:

``````text
LocalPlanNode
-> commit()
-> canonical signature generation
-> version-aware binary header attachment
-> canonicalization verification
-> L2 exact-match / interning
-> CanonicalPlanNode
``````

Before this boundary, the object is local construction material.

After this boundary, the result is canonical, deeply immutable, safely publishable, and eligible for structural sharing.

### 12.2 Single Canonicalization Authority

There must be exactly one authority for Local-to-Canonical promotion per planning tier.

For the current planning engine, that authority is the commit path delegating to L2 interning.

Forbidden:

- direct public constructor of canonical node;
- adapter-created canonical node;
- local frame returning canonical node without commit;
- deserialized payload implementing canonical node;
- bypassing exact canonical signature verification;
- comparing canonical signatures without version compatibility check.

### 12.3 Commit Is a Semantic Barrier

`commit()` is not a convenience factory.

It is a semantic barrier.

It must verify:

- all children are already canonical or explicitly deferred by ratified cycle/deferred law;
- canonical signature has been generated by SSOT function;
- signature bytes carry a version-aware binary header;
- semantic cost bound has been computed by the correct assembly boundary;
- local mutable buffers are not retained;
- canonical node contains no parent/path/session-local material;
- publication occurs through a safe publication path.

Illustrative shape:

``````kotlin
interface LocalPlanNode {
    fun commit(
        interner: PlanInterner,
        verifier: CanonicalizationVerifier,
    ): CanonicalPlanNode
}
``````

The exact API may differ.
The invariant is normative.

## 13. Canonicalization Verifier

### 13.1 Purpose

The canonicalization verifier is the integrity gate at the Local-to-Canonical boundary.

It is inspired by compiler verifier passes:
every lowering stage that claims to produce canonical IR must be verified before the result can be published or
interned.

### 13.2 Structural Integrity Checks

A verifier must check:

- no Local IR child remains;
- no Raw Payload child remains;
- no mutable collection is reachable;
- no parent reference is reachable;
- no path reference is reachable;
- no session/worker reference is reachable;
- no adapter-native handle is reachable;
- no JVM runtime object is reachable from normalized/projected/canonical material;
- no telemetry payload object graph is retained;
- all child nodes are Canonical IR or approved deferred/cycle placeholder;
- canonical signature matches the node's semantic body;
- canonical signature version header matches the active version bundle;
- semantic cost bound is explicitly supplied from assembly boundary;
- all deterministic collections obey their ordering law;
- all canonical values are version-pinned.

### 13.3 Hierarchical Integrity

Canonical IR is hierarchical.

Therefore, a parent cannot become canonical while any child is non-canonical.

Allowed child states:

1. `CanonicalPlanNode`
2. Ratified deferred placeholder
3. Ratified cycle-break placeholder
4. Ratified substitution placeholder

Forbidden child states:

1. `LocalPlanNode`
2. `RawPayloadNode`
3. Adapter DTO
4. Mutable builder object
5. Lazy resolver
6. Runtime DI handle
7. JVM reflection object
8. KSP symbol object

### 13.4 Verification Failure

Verification failure is a protocol violation.

It must fail closed.

It must not degrade to cache miss.
It must not silently rebuild.
It must not publish partially verified structure.

## 14. Canonical Signature Law

### 14.1 Definition

`CanonicalSignature` is the version-pinned byte-level semantic identity material used for exact equality verification.

It is not a hash.

A hash may route to a bucket.
A digest may summarize canonical bytes.
A primitive identity may accelerate lookup.
A canonical signature proves semantic equality.

### 14.2 Relationship with HID, BLAKE3, Digest, and Identity64

Canonical signature generation may consume HID-derived semantic material.

Examples:

- deterministic UUID payload material;
- temporal materialization equality material;
- local selector tuple material;
- synthetic edge local selector material.

BLAKE3 may be used to derive:

- canonical digest material;
- keyed entropy material;
- deterministic UUID payload material;
- routing digests;
- replay summary roots.

However:

- BLAKE3 digest is not the canonical signature itself;
- `Identity64` is not the canonical signature itself;
- HID entropy is not the canonical signature itself;
- canonical signature comparison remains exact byte comparison under the active version tuple.

The relationship is:

``````text
Semantic Material
-> Canonical Encoding
-> CanonicalSignature bytes
-> Digest / Identity64 derived for routing or summary
-> Exact CanonicalSignature comparison remains authoritative
``````

### 14.3 SSOT Authority

There must be one SSOT signature generation function for each canonical IR family.

The function must be bound to:

- canonical encoding version;
- normalization version;
- signature schema version;
- type identity algorithm version;
- edge ordering version;
- hash derivation version.

### 14.4 Signature Contents

A canonical signature must include all semantic material required to distinguish canonical identity.

It must not include:

- object identity;
- memory address;
- allocation order;
- adapter enumeration order;
- parent path;
- runtime session ID;
- telemetry state;
- wall-clock time;
- random UUID;
- mutable RNG output;
- JVM runtime object handles.

### 14.5 Signature vs Digest

A digest is derived from canonical material.

A digest cannot replace canonical material unless collision resistance has been explicitly ratified as sufficient for
that
specific non-semantic surface.

For semantic identity, exact canonical signature comparison remains authoritative.

## 15. Version-Aware Canonical Signature Header

### 15.1 Purpose

Canonical signatures must be version-aware.

Two signatures generated under incompatible canonical rules must not be compared as if they were generated under the
same
protocol.

### 15.2 Required Version Tuple

The active canonical version tuple is:

``````text
canonicalEncodingVersion
normalizationVersion
signatureSchemaVersion
typeIdentityAlgorithmVersion
edgeOrderingVersion
hashDerivationVersion
``````

This tuple may be represented directly or through a compact hash, but the original components must remain inspectable in
debug/replay tooling.

### 15.3 Binary Header Rule

Every canonical signature byte stream must begin with a version-aware binary header.

Illustrative layout:

``````text
magic
canonicalEncodingVersion
normalizationVersion
signatureSchemaVersion
typeIdentityAlgorithmVersion
edgeOrderingVersion
hashDerivationVersion
payloadLength
payloadBytes
``````

The exact binary encoding is defined by the canonical encoding spec.

The invariant is normative:

> A canonical signature must carry enough version material to prevent accidental comparison across incompatible
> canonicalization protocols.

### 15.4 Version Mismatch Rule

When an L2 cache, persistent cache, replay artifact, or payload loader encounters canonical material generated under a
different version tuple, it must not silently compare it with current material.

Allowed outcomes:

1. reject with `CanonicalVersionMismatchException`;
2. force re-planning / re-canonicalization;
3. route to an explicitly compatible legacy reader if compatibility was ratified.

Forbidden outcomes:

1. exact-match comparison across incompatible version tuple;
2. silent cache hit;
3. silent downgrade;
4. ignoring header fields.

### 15.5 Version-Mismatch Interceptor

The interning boundary must include a version-mismatch interceptor.

This interceptor checks stored canonical material before exact signature comparison.

Illustrative shape:

``````kotlin
interface CanonicalVersionMismatchInterceptor {
    fun verifyCompatible(
        stored: CanonicalVersionTuple,
        current: CanonicalVersionTuple,
    )
}
``````

The exact API may differ.
The invariant is normative.

## 16. Canonical Ordering vs Canonical Encoding

### 16.1 Canonical Ordering

Canonical ordering is semantic ordering.

It is defined above byte encoding.

Kontrakt canonical string order:

- input strings are valid Unicode scalar sequences;
- input strings are NFC-normalized;
- comparison is lexicographic ascending over canonical Unicode scalar sequences;
- locale-dependent collation is forbidden;
- platform library collation is not the authority.

Kontrakt canonical identifier order specializes canonical string order for identifier-shaped values.

### 16.2 Canonical Encoding

Canonical encoding is byte lowering.

It is used for:

- canonical signature bytes;
- digest input;
- replay artifacts;
- golden vectors;
- persistent cache keys.

Canonical encoding may use UTF-8.

Canonical ordering must not be defined as UTF-8 byte ordering.

### 16.3 Length-Prefix Encoding Rule

Canonical encoding must be unambiguous.

Delimiter-joined string encoding is forbidden for canonical signatures.

Forbidden example:

``````text
field1|field2|field3
``````

Reason:

If any field later permits the delimiter or escaping rules evolve, old and new encodings can become ambiguous.

Required direction:

- length-prefix fields;
- explicit field tags or schema positions;
- explicit null/absent markers;
- explicit collection length;
- explicit version header;
- explicit byte length for variable-width material.

Illustrative shape:

``````text
fieldTag
fieldLength
fieldBytes
``````

The exact layout belongs to the canonical encoding spec.
The invariant is that canonical byte material must be self-delimiting and unambiguous.

### 16.4 Version-Pinned Canonical Encoding

Canonical encoding must be version-pinned.

Any change to byte layout, field order, delimiter scheme, escaping rule, normalization version, type identity version,
or
hash family version must be represented as an explicit version change.

A canonical byte stream generated under one version must remain reproducible under that version even if a later Kontrakt
version introduces a newer encoding.

### 16.5 Stable Binary Layout

Canonical encoding must define a stable binary layout.

A stable binary layout means:

- field order is fixed;
- field tags are fixed;
- scalar encoding is fixed;
- string lowering is fixed;
- collection ordering is fixed;
- omitted/default fields are handled explicitly;
- version headers are explicit;
- old layout readers are preserved where compatibility is promised.

Canonical encoding must not rely on:

- reflection field order;
- Kotlin data class component order;
- JSON object iteration order;
- JVM serialization;
- Map iteration order;
- platform locale;
- default charset.

## 17. Canonical Edge Key and Edge Rank Lowering

### 17.1 Semantic Edge Identity

Synthetic and active edges first exist as semantic tuples.

Examples:

``````text
owner type
member identity
member kind
synthetic kind
entry index
slot phase
target type
``````

### 17.2 Canonical Edge Key

`CanonicalEdgeKey` is the deterministic lowered edge identity used by traversal / ordering.

It is not the same as a display label.

Display labels such as:

``````text
element[0]
entry[0].key
field:userName
``````

are diagnostics only.

### 17.3 Edge Rank

`edgeRank` is a primitive lowering for hot-path ordering.

`edgeRank` may collide.

If it collides, the exact semantic edge comparison law must break ties deterministically.

The collision resolution must be version-bound.

## 18. Payload Discard Rule

### 18.1 Raw Payload Cannot Be Canonical

A deserialized payload object cannot be canonical merely because it contains canonical bytes.

It must be mapped to raw payload form first.

### 18.2 Promotion Path

The only legal promotion path is:

``````text
RawPayloadNode
-> payload validation
-> version header verification
-> normalized material
-> LocalPlanNode
-> commit()
-> L2 intern()
-> CanonicalPlanNode
``````

### 18.3 Immediate Discard

After promotion, raw payload instances must fall out of scope.

The canonical node returned by interning is the only object permitted to survive.

## 19. Cache-Blind Determinism

Canonical IR must be cache-blind.

Identical semantic request must produce the same canonical semantic structure whether:

- L2 is cold;
- L2 is hot;
- L2 is bypassed;
- L2 is circuit-open;
- partition bulk-drop happened before this run.

Cache may change object reference sharing.
Cache must not change canonical semantic structure.

## 20. Canonical IR and ADR-0038 Type Expansion

ADR-0038 defines executable expansion decisions for:

- interface / abstract polymorphic expansion;
- atomic expansion;
- collection expansion;
- array expansion;
- map expansion.

This document defines how the output of those expansion decisions becomes IR.

### 20.1 Atomic

Atomic expansion may produce terminal canonical IR only after:

- atomic value strategy is resolved;
- atomic equality material is canonical;
- temporal / UUID materialization follows deterministic replay law;
- semantic cost bound is explicit;
- canonical signature carries the active version tuple;
- verifier approves terminal leaf form.

### 20.2 Collection / Array

Container expansion may produce canonical IR only after:

- synthetic edges are ordered;
- synthetic edge identities are structured tuples, not display labels;
- child nodes are canonical or ratified placeholders;
- container cardinality is explicit;
- canonical signature includes ordered child descriptors.

### 20.3 Map

Map expansion may produce canonical IR only after:

- map entries are canonical entries;
- key/value pairing is preserved;
- key uniqueness is verified by canonical key equality material;
- duplicate canonical keys fail closed;
- map signature includes canonical entry descriptors.

### 20.4 Polymorphic Interface

Polymorphic expansion may produce canonical IR only after:

- polymorphic resolution mode is explicit;
- contract subject expansion is handled before root planning;
- dependency/member resolution consumes pinned runtime binding snapshot;
- implementation candidates are ordered under canonical identifier order;
- zero-candidate contract subject follows contract vacancy policy.

## 21. Enforcement Rules

### 21.1 Compile-Time

The following must be enforced through types and visibility:

- `LocalPlanNode` is not externally constructible.
- `CanonicalPlanNode` is sealed.
- Canonical implementations are final.
- Canonical constructors are non-public.
- Canonical nodes cannot be `data class`.
- Parent constructors accept only canonical children or ratified placeholders.
- Raw payload types cannot implement canonical interfaces.
- Canonical signatures are not constructible without a version tuple.

### 21.2 Static Analysis

Architecture tests must verify:

- no reflection/KSP imports in core;
- no JVM runtime object fields after normalized fact boundary;
- no raw mutable collection fields in canonical nodes;
- no `var` fields in canonical nodes;
- no `lazy` delegates in canonical nodes;
- no parent/path/session fields in canonical nodes;
- no `this` escape during canonical node construction;
- no Local IR stored inside Canonical IR;
- no Raw Payload stored inside Canonical IR;
- no direct construction of canonical nodes outside authorized factories;
- no JVM serialization dependency for canonical encoding;
- no delimiter-joined encoding in canonical signature generation.

### 21.3 Runtime / Property Tests

Tests must verify:

- cold cache and hot cache produce identical semantic structure;
- L2 bypass and L2 hit produce identical canonical signatures;
- raw payload promotion returns interned canonical node;
- verification rejects local child leakage;
- verification rejects parent/path leakage;
- verification rejects JVM runtime object leakage;
- canonical signature is stable across replay with same version tuple;
- canonical signature changes only under ratified version changes;
- version mismatch prevents exact-match cache hit;
- canonical ordering is locale-independent;
- canonical encoding golden vectors remain stable.

## 22. Required Golden Vectors

Golden vectors must exist for:

- canonical string order;
- canonical identifier order;
- canonical encoding header;
- canonical length-prefix field encoding;
- canonical encoding of a primitive atomic node;
- canonical encoding of a composite node;
- canonical encoding of a collection node;
- canonical encoding of a map node;
- canonical edge key lowering;
- canonical signature generation;
- digest derivation from canonical signature;
- raw payload promotion into canonical node;
- version-mismatch rejection;
- version-pinned compatibility case.

## 23. Naming Migration Guidance

Current and future naming should follow this discipline:

| Existing / Candidate Name               | Recommendation                  | Reason                                                               |
|-----------------------------------------|---------------------------------|----------------------------------------------------------------------|
| `CanonicalExpansionText`                | Rename to `CanonicalTextLaw`    | It is a law/helper, not canonical material.                          |
| `DeterministicCanonicalMapEntries`      | Rename to `CanonicalMapEntries` | Canonical entry law already includes deterministic uniqueness/order. |
| `CanonicalMapEntry`                     | Keep                            | ADR-0038 defines canonical map entries.                              |
| `ExpansionSequence`                     | Keep                            | It is a deterministic container, not canonical identity material.    |
| `OrderedSyntheticEdges`                 | Keep                            | It is ordered expansion material, not canonical edge key material.   |
| `SyntheticEdgeIdentity`                 | Keep                            | It is semantic identity tuple, not lowered canonical key.            |
| `AtomicEqualityMaterial.canonicalValue` | Keep                            | It is canonical equality surface.                                    |

## 24. Non-Goals

This document does not define:

- a complete binary serialization format;
- a persistent cache storage format;
- discovery manifest partitioning;
- UI/report rendering format;
- external JSON contract schema;
- policy contract DSL;
- implementation of L2 intern repository.

Those belong to separate design notes or ADRs.

## 25. Final Rule

Canonical is not a decoration.

Canonical means protocol-ratified semantic identity material.

Canonical IR is context-free, parent-free, path-free, deeply immutable, version-pinned, and interned through the
authorized
canonicalization boundary.

Payload is post-mortem.

Raw runtime objects are not normalized facts.

A hash is not a signature.

Anything else must use a different name.