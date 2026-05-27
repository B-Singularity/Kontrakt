# Design Note: Stable Metadata Identity Protocol

- Status: Draft
- Date: 2026-05-27
- Owner: Kontrakt Compiler Core / Metadata Identity
- Source Decision: ADR-0041
- Extraction Role: Post-acceptance extraction from ADR-0041
- Scope: Byte-level stable metadata identity protocol
- Non-Goal: Interner table mechanics, physical substrate implementation, contract graph ontology, or persistent artifact
  format

---

## 1. Purpose

This design note extracts and maintains the byte-level stable metadata identity protocol accepted in ADR-0041.

It defines how Kontrakt converts metadata identity material into deterministic, cross-backend, collision-verifiable
identity bytes and compact identity descriptors.

The protocol pipeline is:

``````text
ratified metadata material
-> canonical material
-> canonical protocol bytes
-> canonical domain separation payload
-> active digest-suite invocation
-> HID / digest descriptor bytes
-> collision verification payload
-> downstream protocol-owned interning
``````

This design note exists to keep ADR-0041 from remaining the permanent maintenance location for every detail of canonical
metadata identity.

ADR-0041 remains the accepted parent decision.

This design note is the narrower maintenance surface for the byte-level metadata identity protocol after extraction.

---

## 2. Authority Boundary

### 2.1. This document owns

This document owns the extracted maintenance surface for:

- canonical metadata material;
- canonical protocol bytes;
- canonical envelope/header shape;
- object/record/message encoding law;
- field and tag ordering law;
- string and byte encoding law;
- canonical sorting input law where it is protocol-semantic;
- unknown tag and decoder bound law;
- domain separation payload;
- digest suite abstraction;
- HID descriptor shape;
- HID width/suite/version semantics;
- HID-not-equality-authority law;
- digest/domain golden vectors;
- TypeReference canonical material;
- TypeReference HID law;
- TypeReference pre-SCC/final identity phasing;
- TypeCycleKey derivation boundary.

### 2.2. This document does not own

This document does not own:

- protocol-owned metadata interning mechanics;
- stable intern id assignment;
- interner probe table layout;
- collision group storage;
- staged-byte failure policy after interner admission;
- owner-lane routing;
- routed batching;
- inbox backpressure;
- self-drain mechanics;
- primitive slab lifecycle;
- off-heap / `MemorySegment` / native allocation policy;
- contract graph structural/contextual identity;
- incremental affected-set derivation;
- L2 cache governance;
- or persistent artifact binary format.

Ownership split:

| Surface                                                        | Owner                                              |
|----------------------------------------------------------------|----------------------------------------------------|
| Accepted parent decision                                       | ADR-0041                                           |
| Stable metadata identity bytes / HID protocol                  | This design note                                   |
| Protocol-owned metadata interning                              | `docs/design/protocol-owned-metadata-interning.md` |
| Primitive lifecycle / physical substrate / mechanical sympathy | ADR-0042                                           |
| Contract graph identity / incremental derivation               | ADR-0043                                           |
| Unified runtime memory envelope                                | ADR-0044                                           |
| Enforcement hooks                                              | `docs/constitution/compiler-core-protocols.md`     |

### 2.3. Conflict rule

If this design note conflicts with ADR-0041 before extraction is explicitly accepted, ADR-0041 wins.

After this design note is accepted as the narrower maintenance authority, conflict resolution follows the authority
boundary above.

No later document may reinterpret this protocol to make identity depend on:

- runtime cache warmth;
- thread scheduling;
- backend enumeration order;
- object identity;
- physical address;
- JVM allocation behavior;
- GC behavior;
- wall-clock timing;
- or adaptive profiling.

---

## 3. Design Goals

The stable metadata identity protocol must satisfy all of the following simultaneously.

### 3.1. Cross-backend determinism

The same ratified metadata material must produce the same canonical bytes and descriptor bytes across supported
backends.

Backend handles, backend traversal order, reflection rendering, KSP descriptors, PSI nodes, classloader identity, or JVM
object identity must not enter the protocol.

### 3.2. Collision-verifiable compact identity

Compact digest/HID descriptors are useful for routing, pre-screening, storage compaction, and interner acceleration.

They are not semantic equality authority.

Equality authority remains the canonical material and the exact collision verification path.

### 3.3. Algorithm agility without semantic drift

A specific hash algorithm is not the semantic contract.

The semantic contract is:

``````text
canonical bytes
+ canonical domain separation payload
+ active digest-suite profile
+ requested output width
-> deterministic descriptor bytes
``````

Digest algorithms, output widths, keyed derivation modes, or XOF expansion rules may evolve only through ratified suite
profiles, versions, golden vectors, and compatibility law.

### 3.4. Mechanical sympathy without physical leakage

The protocol may be CPU-facing binary material.

It may use fixed-width fields, little-endian encoding, numeric tags, and compact canonical bytes.

However, physical layout choices such as cache-line alignment, slab offset, MemorySegment address, or off-heap alignment
are not metadata identity authority.

Those belong to ADR-0042.

### 3.5. Fail-closed protocol safety

Malformed, unknown, incompatible, overflowing, ambiguous, or unsupported protocol material must fail closed before it
can become published identity.

The implementation must not discover protocol impossibility through JVM `OutOfMemoryError`, `StackOverflowError`,
backend exceptions, platform string conversion failures, or decoder runaway.

---

## 4. Vocabulary

### 4.1. Metadata identity material

Metadata identity material is ratified semantic input to metadata identity.

Examples include:

- type reference identity material;
- member identity material;
- lowered contract fact identity material where metadata-local;
- canonical edge key material;
- active member key material;
- version bundle material;
- domain schema material;
- canonical ordering policy material.

Metadata identity material is not:

- backend object identity;
- runtime graph object identity;
- annotation descriptor object identity;
- DTO object identity;
- source AST object identity;
- reflection handle identity;
- display text;
- or diagnostic rendering.

### 4.2. Canonical material

Canonical material is metadata identity material after backend erasure, normalization, schema classification, and
ratified compatibility processing.

Canonical material is still a logical model.

It is not yet canonical bytes.

### 4.3. Canonical bytes

Canonical bytes are the byte-level protocol representation of canonical material.

Canonical bytes are the primary input to digest derivation and collision verification.

Canonical bytes are not backend serialization.

Canonical bytes are not JVM object graph serialization.

Canonical bytes are not display strings.

### 4.4. Domain separation payload

The domain separation payload is fixed-layout protocol material included in every digest invocation used for metadata
identity.

It prevents digest reuse across incompatible domains, schema versions, encoding versions, suite profiles, and semantic
version bundles.

### 4.5. HID

A HID is a compact digest descriptor derived from canonical protocol bytes under an active digest suite.

A HID may accelerate lookup.

A HID may participate in descriptor comparison.

A HID is not semantic equality authority without exact verification.

### 4.6. Digest suite

A digest suite is a versioned profile defining:

- algorithm id;
- algorithm implementation version;
- derivation mode;
- requested output width;
- domain separation semantics;
- XOF/output rules where applicable;
- golden vectors;
- compatibility classification.

The digest suite is an adapter/protocol profile.

It is not the semantic metadata identity itself.

### 4.7. TypeReference pre-identity

A TypeReference pre-identity is non-final pre-SCC material used to build the metadata identity dependency graph and
discover cycles.

It is not a stable identity.

It is not a cache key.

It is not frozen-image identity.

### 4.8. TypeCycleKey

A TypeCycleKey is final sealed TypeReference identity material derived after SCC discovery/seal.

It is not required to construct the pre-SCC graph.

---

## 5. Identity Authority Lattice

Kontrakt must preserve the distinction between identity authorities.

Ordered from strongest to weakest for equality:

``````text
exact canonical material / canonical bytes
-> collision-verified compact identity
-> stable intern id inside compatible scope
-> routing material
-> diagnostic material
``````

### 5.1. Semantic equality authority

Semantic equality authority belongs to the exact canonical material and exact canonical bytes accepted by the relevant
identity domain.

A digest descriptor may become part of a verified equality result only after the collision verification path proves that
the canonical material matches.

### 5.2. Collision-verified compact identity

A compact descriptor is collision-verified only after:

- identity domain matches;
- domain schema/version material matches;
- digest suite/version/width matches;
- canonical encoding version matches;
- version bundle fingerprint matches;
- canonical byte length matches;
- and exact verification payload matches.

### 5.3. Stable intern id

A stable intern id is scoped identity produced by a protocol-owned interner.

It is not cross-scope identity by itself.

It may stand for already-verified canonical identity only inside its explicit compatible scope.

### 5.4. Routing material

Routing material chooses a lane, shard, partition, bucket, or pre-screen path.

Routing material must not become equality authority.

### 5.5. Diagnostic material

Diagnostic material explains failures.

It must not be used to accept equality.

---

## 6. Canonical Material Law

Metadata identity starts from ratified canonical material.

A metadata identity domain MUST define:

- identity domain id;
- domain schema version;
- canonical material fields;
- field presence law;
- field defaulting law;
- field ordering law;
- unknown-field law;
- duplicate-field law;
- collection ordering law;
- canonical string/bytes law;
- version-bundle axes;
- compatibility classification;
- canonical byte encoding;
- digest-suite usage;
- collision verification payload;
- and golden vectors.

Canonical material MUST NOT include:

- JVM object address;
- `System.identityHashCode`;
- runtime `hashCode`;
- backend handle identity;
- reflection handle identity;
- KSP / PSI / compiler descriptor identity;
- classloader identity;
- source traversal order unless ratified as semantic;
- classpath order;
- service-loader order;
- map iteration order;
- thread id;
- coroutine id;
- worker/lane id;
- callback completion order;
- cache hit/miss state;
- telemetry values;
- GC state;
- or wall-clock time.

The lawful shape is:

``````text
syntax / annotation / DSL / backend metadata / generated index
-> backend-erased lowering
-> ratified metadata material
-> canonical material
-> canonical bytes
``````

The forbidden shape is:

``````text
backend object / reflection object / annotation descriptor
-> direct identity
``````

---

## 7. Canonical Byte Encoding Law

Canonical bytes are protocol-owned bytes.

They MUST be produced by Kontrakt-owned deterministic encoders.

They MUST NOT be produced by:

- Java serialization;
- Kotlin serialization defaults;
- protobuf defaults without a Kontrakt-ratified canonical profile;
- JSON object iteration;
- display rendering;
- `toString`;
- platform charset default;
- locale-sensitive formatting;
- classloader-dependent lookup;
- map iteration order;
- or backend-provided serializer order.

### 7.1. Canonical envelope

Every canonical metadata identity byte payload MUST begin with a canonical envelope header.

The header establishes:

- protocol marker;
- canonical encoding version;
- identity domain id;
- domain schema version;
- payload kind;
- payload length or bounded segment length;
- and compatibility-relevant version material where required.

The exact header layout is versioned by `canonicalEncodingVersion32`.

A layout selected by one canonical encoding version MUST NOT silently change.

### 7.2. Endian rule

Fixed-width numeric protocol fields use little-endian encoding unless a later canonical encoding version explicitly
ratifies a different rule.

The endian rule is protocol material.

It is not selected from host CPU endianness.

### 7.3. Integer encoding

Fixed-width integers MUST define:

- signedness;
- width;
- endian;
- overflow behavior;
- sentinel mapping;
- and canonical range.

Variable-width integers, including varint-like encodings, are lawful only when the domain ratifies:

- exact value range;
- exact width rule;
- continuation-bit rule;
- canonical shortest-form rule;
- overflow bound;
- decoder progress rule;
- and golden vectors.

Variable-width integer material MUST NOT be re-derived from mutable runtime state after size measurement has accepted
the value.

### 7.4. String encoding

Canonical strings MUST use pinned Unicode normalization and UTF-8 byte encoding.

A string domain MUST declare:

- normalization form;
- Unicode/ICU version or equivalent pinned normalization profile;
- invalid sequence policy;
- length bound;
- byte-count rule;
- comparison rule;
- and golden vectors.

Platform default charset is forbidden.

Locale-sensitive case mapping is forbidden unless the domain ratifies a pinned locale and versioned rule.

### 7.5. Bytes encoding

Canonical byte fields MUST define:

- length bound;
- length prefix rule;
- raw byte preservation rule;
- zero-length representation;
- unknown/trailing byte policy;
- and decoder progress bound.

### 7.6. Collection encoding

Collections MUST define whether they are:

- ordered;
- unordered but canonical-sorted;
- duplicate-preserving;
- duplicate-rejecting;
- multiset-like;
- map-like;
- or set-like.

Unordered collections MUST be canonicalized by deterministic sort keys.

The sort key MUST be canonical protocol material.

It MUST NOT be platform comparator output, object identity, insertion order, hash bucket order, or backend traversal
order.

### 7.7. Object/record/message encoding

Object-like material is encoded as protocol records.

A record schema MUST define:

- field ids;
- field tags;
- field order;
- field type;
- field presence;
- field defaulting;
- required/optional semantics;
- unknown tag policy;
- duplicate tag policy;
- nested object policy;
- recursion/cycle policy;
- and compatibility behavior.

Object/record encoding is not JVM object graph serialization.

### 7.8. Unknown tag law

Unknown tags MUST fail closed unless the domain explicitly ratifies a bounded skip rule.

A bounded skip rule MUST define:

- max skipped field count;
- max skipped byte count;
- max nesting depth;
- compatibility classification;
- diagnostic policy;
- and golden vectors.

Unknown tag skipping MUST NOT cause decoder desynchronization.

### 7.9. Decoder progress law

A decoder MUST make strict progress.

Every decode step must either:

- consume a positive number of bytes;
- terminate successfully at an exact boundary;
- or fail closed.

A decoder MUST NOT loop indefinitely over zero-length, unknown, malformed, or overlong input.

### 7.10. Capacity arithmetic law

All capacity and byte-length arithmetic used by canonical encoding MUST be checked.

Overflow MUST fail closed before allocation or publication.

Canonical encoding MUST NOT discover size impossibility through `OutOfMemoryError`.

---

## 8. Canonical Sorting Law

Canonical sort is semantic protocol work when it determines canonical byte order.

A canonical sorting domain MUST define:

- sort key material;
- sort key encoding;
- comparator law;
- tie handling;
- duplicate handling;
- maximum member count;
- maximum sort scratch bytes;
- failure behavior;
- and golden vectors.

Canonical sort MUST be deterministic across:

- backend acquisition order;
- source traversal order;
- platform collection iteration;
- worker/lane scheduling;
- thread interleaving;
- callback completion;
- and physical storage placement.

If exact deterministic ordering cannot be established within the resolved budget, the sort MUST fail closed before
publication.

Tie groups MUST be resolved by ratified canonical material.

Unresolved ties MUST NOT be broken by:

- first seen;
- source file order unless ratified;
- backend index;
- object address;
- identity hash;
- thread/lane id;
- CAS winner order;
- or random seed.

---

## 9. Canonical Compactness Boundary

Canonical identity bytes may be compact.

They may not be stateful.

Approved compactness direction:

``````text
semantic redundancy elimination
-> schema-aware stripping
-> protocol numeric tags
-> bit-packed hot fields
-> sealed child intern references where ratified
-> optional immutable canonical-base delta representation
``````

Rejected compactness direction:

``````text
run-local compression
previous-item delta
backend-order dictionary
frequency-learned dictionary
planning-truncation identity shortcut
runtime cache-state shortcut
``````

A compact representation is lawful only if it is canonical, deterministic, bounded, and golden-vector covered.

---

## 10. General Compression Boundary

General-purpose compression such as Zstd, LZ4, gzip, runtime-learned dictionaries, adaptive dictionaries, or
previous-run
dictionary reuse MUST NOT be part of hot semantic identity equality.

Compression MAY be used for cold external artifacts only if a future artifact format defines:

- compression algorithm id;
- compression version;
- deterministic compressor settings;
- decompression safety bounds;
- canonical pre-compression bytes;
- compatibility law;
- and golden vectors.

Compressed bytes are not canonical identity bytes.

Canonical identity bytes are the uncompressed canonical protocol bytes or a domain-ratified canonical-base delta
representation.

A compression profile MUST NOT be selected by runtime frequency, previous item order, cache warmth, backend traversal
order, or adaptive profiling.

---

## 11. Domain Separation Payload Law

Every digest invocation used for metadata identity MUST include domain separation.

Domain separation is byte-level protocol material.

It MUST NOT be assembled from:

- display strings;
- enum ordinals;
- declaration order;
- service-loader order;
- map iteration order;
- classpath order;
- source traversal order;
- delimiter-joined text;
- or backend descriptor rendering.

### 11.1. DigestDomainSeparationPayloadV1

The canonical domain separation payload for HID derivation is a fixed-width 56-byte protocol payload:

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

The payload size is protocol material.

It is not a cache-line alignment claim.

Physical cache-line placement belongs to ADR-0042.

The implementation MUST NOT add padding bytes to this protocol payload merely to match cache-line size.

### 11.2. Reserved fields

Reserved fields MUST be encoded.

They MUST be zero.

They MUST NOT contain uninitialized memory.

They MUST NOT be omitted when not applicable.

A reserved field becoming non-zero is a new protocol version event, not a backward-compatible accidental extension.

### 11.3. Version bundle fingerprint

Version-bundle fingerprints are derived from tagged canonical axis bytes.

They are not delimiter-free string concatenations.

The fingerprint input MUST include:

- identity domain id;
- canonical encoding version;
- domain schema version;
- semantic version axes relevant to the domain;
- compatibility policy version;
- ordering policy version where applicable;
- type identity algorithm version where applicable;
- and width-suffixed version axis tags.

The fingerprint derivation must be golden-vector covered.

---

## 12. Digest Suite Law

The digest algorithm is not itself the semantic metadata identity contract.

A digest suite is a ratified protocol profile.

A suite profile MUST define:

- suite id;
- algorithm id;
- algorithm implementation version;
- derivation version;
- requested output widths;
- keyed/unkeyed derivation modes;
- XOF/output expansion rule where applicable;
- domain separation payload version;
- canonical input concatenation order;
- compatibility classification;
- golden vectors;
- and migration policy.

### 12.1. Active suite invocation shape

The logical invocation shape is:

``````text
canonical domain separation payload
+ canonical identity bytes
+ requested descriptor width
+ digest suite profile
-> deterministic descriptor bytes
``````

A suite implementation MUST NOT read:

- mutable runtime state;
- random seed;
- clock;
- backend enumeration state;
- adaptive profiling state;
- GC state;
- thread/lane id;
- or object address.

### 12.2. Algorithm agility

A later suite may replace the current digest algorithm.

Such a change is a suite/profile/version change.

It MUST be reflected in domain separation material and compatibility classification.

A digest algorithm change MUST NOT be hidden behind the same suite id/version.

### 12.3. XOF/output expansion

XOF or variable-output expansion is allowed only if the suite profile explicitly ratifies:

- output derivation rule;
- requested width encoding;
- maximum width;
- truncation rule;
- domain separation for width;
- compatibility behavior;
- and golden vectors.

The implementation MUST NOT request arbitrary output width at runtime.

### 12.4. Runtime rekeying prohibition

Collision pressure, cold collision overflow, queue pressure, runtime data distribution, or adversarial input MUST NOT
cause the implementation to select a new digest suite, seed, output width, route map, or derivation rule after a scope
has become visible.

Stronger-width derivation is lawful only when selected before scope admission by resolved policy and covered by golden
vectors.

---

## 13. HID Law

A HID is a compact digest descriptor.

It is a candidate descriptor and accelerator.

It is not semantic equality authority.

### 13.1. HID descriptor material

A HID descriptor MUST be interpreted together with:

- identity domain id;
- domain schema version;
- canonical encoding version;
- digest suite id;
- digest suite version;
- requested output width;
- HID derivation version;
- version-bundle fingerprint;
- canonical byte length;
- and verification payload availability.

A bare HID word sequence is insufficient.

### 13.2. HID width law

Supported widths must be ratified by the digest suite profile.

A width profile MUST define:

- width id;
- byte length;
- word layout;
- endian rule;
- comparison rule;
- collision verification requirement;
- compatibility behavior;
- and golden vectors.

A wider HID may reduce collision probability.

It does not eliminate verification.

### 13.3. HID equality prohibition

The following is forbidden:

``````text
HID match
-> semantic equality accepted
``````

The lawful shape is:

``````text
HID match
-> domain/version/suite/width/length check
-> collision verification payload check
-> exact canonical byte/material verification
-> equality accepted
``````

### 13.4. HID usage boundary

A HID MAY be used for:

- routing;
- pre-screening;
- compact descriptor storage;
- interner candidate lookup;
- diagnostic grouping;
- collision group admission;
- L2 key pre-screening where exact key verification follows.

A HID MUST NOT become:

- stable intern id;
- semantic equality authority;
- PlanCacheKey equality authority;
- frozen-image identity by itself;
- persistent artifact identity by itself;
- cross-scope identity;
- query reuse authority;
- or report/replay identity by itself.

### 13.5. HID64 and HID128 collision handling

Narrow descriptors require explicit collision verification.

HID64 is never enough for equality.

HID128 is also not equality authority.

If a HID collision occurs, the system must either:

- verify exact canonical material and treat the candidates as distinct if material differs;
- contain the collision group under bounded policy;
- fail the current identity scope closed;
- quarantine the owning source/acquisition/identity boundary;
- or use a pre-admitted stronger-width path selected before visibility.

It MUST NOT accept digest-only equality.

---

## 14. Collision Verification Payload Law

Every compact identity descriptor used for equality-sensitive work MUST have an exact verification payload path.

The payload may include:

- canonical bytes;
- canonical string table slice;
- sealed canonical material reference;
- collision verification record;
- stable intern reference with scope proof;
- or another ratified exact verification surface.

The payload MUST NOT be:

- display text;
- `toString`;
- object identity;
- backend handle;
- non-canonical serialized bytes;
- mutable adapter object;
- or diagnostic string.

If exact verification material is unavailable, equality-sensitive publication MUST fail closed.

---

## 15. Hierarchical Identity Derivation

Hierarchical identity derivation must preserve the distinction between local structural identity and parent/contextual
identity.

The safe generic shape is:

``````text
child local canonical material
-> child structural descriptor / sealed reference
-> parent/context derivation consumes sealed child reference
``````

The unsafe shape is:

``````text
parent/context key
+ raw child object graph
+ recursive child bytes
-> identity
``````

Parent derivation MAY use sealed child references to avoid re-reading full child canonical bytes.

This reduces cost with respect to child byte length.

It does not make arbitrary graph updates constant-time.

Contract graph-specific structural/contextual identity is maintained by ADR-0043.

---

## 16. TypeReference Canonical Material

TypeReference identity is a metadata identity domain.

The final sealed TypeReference identity material MUST include:

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
use-site annotations or equivalent contract surfaces.

It MUST NOT include annotation descriptors or annotation backend objects directly.

It MUST NOT include:

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

### 16.1. TypeReference pre-SCC material

The pre-SCC TypeReference pre-identity material MAY include only the subset required to construct the metadata identity
dependency graph and discover SCCs.

Pre-SCC material MUST NOT pretend to be the final sealed TypeReference identity.

If `TypeCycleKey` is not yet available, the pre-SCC phase MUST use a fixed unresolved-cycle sentinel or a ratified
node-local pre-identity surface that is explicitly marked as non-final.

### 16.2. Type nesting and generics depth fuse

TypeReference canonicalization MUST bound nested and recursive type structure before expansion, flattening, signature
materialization, or dependency-graph construction can recurse unboundedly.

A resolved TypeReference identity policy MUST define at least:

``````text
maxTypeNestingDepth
maxTypeArgumentCountPerType
maxTypeProjectionCount
maxTypeBoundCount
maxFBoundExpansionDepth
maxRecursiveTypeReferenceEdges
maxTypeSignatureBytes
maxTypeCanonicalizationFrames
maxTypeDiagnosticBytes
``````

The implementation MUST meter these limits while traversing generic signatures, nullable wrappers, arrays, function
types, type aliases where ratified, captured types, variance projections, upper/lower bounds, and F-bound recursive
patterns.

If any resolved depth/count/byte/frame fuse is exceeded, the TypeReference candidate MUST fail closed before final
TypeReference canonical byte publication, HID derivation, stable intern id assignment, frozen image publication,
planning visibility, or report/replay identity publication.

A depth fuse failure is not semantic equality.

It is a bounded identity-canonicalization rejection.

The implementation MUST NOT rely on JVM call-stack overflow, recursive function failure, backend exception behavior, or
`StackOverflowError` as the boundary.

---

## 17. TypeReference HID

A TypeReference HID is a metadata identity descriptor derived under the active digest suite from final TypeReference
canonical bytes and canonical domain separation payload.

It is not the TypeReference semantic equality authority by itself.

Required equality-sensitive TypeReference comparison order:

``````text
identity domain
-> TypeReference schema/version material
-> digest suite / HID width
-> HID words
-> canonical byte length
-> TypeCycleKey compatibility
-> exact canonical TypeReference material verification
``````

A TypeReference HID MUST NOT be compared across incompatible:

- identity domains;
- schema versions;
- canonical encoding versions;
- type identity algorithm versions;
- normalization versions;
- TypeCycleKey derivation versions;
- or digest suite profiles.

---

## 18. TypeReference Cycle-Key Phasing

Final TypeReference identity MUST NOT require a final `TypeCycleKey` before the metadata identity dependency graph has
been constructed.

The lawful v1 shape is:

``````text
Phase A: Pre-SCC TypeReference Pre-Identity
    canonical node-local type material
    canonical type signature prelude
    bounded generic/depth metering
    fixed unresolved-cycle sentinel where TypeCycleKey would appear
    dependency edge extraction
    no stable intern id publication

Phase B: SCC Seal and Final TypeReference Identity
    deterministic SCC discovery
    SCC-local ordinal / reference encoding
    TypeCycleKey derivation
    final TypeReference canonical material
    HID derivation
    collision verification
    stable intern id assignment through protocol-owned interner
    safe publication
``````

The pre-SCC identity is not stable identity.

It may be used only for:

- dependency graph construction;
- SCC discovery;
- cycle preflight metering;
- deterministic diagnostic evidence;
- and bounded staging before final seal.

It MUST NOT be used as:

- stable intern id;
- semantic equality authority;
- PlanCacheKey material;
- frozen-image material;
- report/replay identity;
- persistent artifact identity;
- cross-scope identity;
- or query reuse key.

### 18.1. Unresolved-cycle sentinel

The unresolved-cycle sentinel MUST be fixed, versioned, domain-separated, and explicitly encoded.

It MUST NOT be:

- null omission;
- absent field;
- backend default value;
- display text;
- source traversal marker;
- or object identity.

After SCC seal, the final `TypeCycleKey` MUST replace the unresolved-cycle sentinel in the final canonical material.

The implementation MUST verify that all provisional pre-SCC references are either:

- resolved to final sealed TypeReference identity material;
- represented by lawful SCC-local references during SCC seal;
- or failed closed before publication.

### 18.2. TypeCycleKey derivation boundary

`TypeCycleKey` is final sealed TypeReference identity material.

It is not required as an input to construct the pre-SCC dependency graph.

A `TypeCycleKey` MUST be derived only from ratified SCC / cycle material such as:

- SCC profile id;
- deterministic SCC member ordering;
- SCC-local ordinal encoding;
- canonical intra-SCC reference encoding;
- canonical external dependency identity material;
- version/schema compatibility material;
- and canonical TypeReference member material accepted by the SCC seal.

It MUST NOT be derived from:

- planning cycle truncation;
- active planning stack;
- backend traversal order;
- discovery order;
- object address;
- worker/lane id;
- callback completion order;
- or provisional handle allocation order.

If TypeCycleKey derivation fails, the owning SCC seal MUST fail closed before final TypeReference stable intern id
publication.

The implementation MUST NOT publish a final TypeReference identity with a placeholder TypeCycleKey.

---

## 19. TypeReference Carrier Boundary

This protocol document defines TypeReference identity material.

It does not own hot-path physical carrier packing.

The following are maintained by ADR-0042:

- `TABLE_PROVEN_LOCAL_ID32`;
- `PACKED_LOCAL_REF64`;
- `PACKED_REF128`;
- `SOA_PROOF_COLUMNS`;
- hot carrier bit packing;
- physical carrier allocation prohibition;
- hot wrapper allocation rules.

The semantic rule preserved here is:

``````text
primitive carrier equality is not TypeReference semantic equality without compatible table/scope/protocol proof and
collision verification.
``````

---

## 20. L2 / PlanCacheKey Boundary

L2 may consume stable metadata identity material.

L2 may use HID, intern ids, route descriptors, and canonical identity fingerprints as acceleration material.

L2 MUST NOT treat these as exact PlanCacheKey equality unless the full semantic PlanCacheKey tuple is verified.

This protocol provides identity substrate.

It does not define L2 cache governance.

L2 cache-blindness remains required:

``````text
cache hit or miss
-> must not alter canonical topology, canonical signatures, stable metadata identity, or semantic result
``````

---

## 21. Persistent Artifact Boundary

This design note does not define a persistent artifact binary format.

A future artifact format may persist canonical identity material only if it defines:

- artifact format id;
- artifact format version;
- canonical encoding version;
- digest suite version;
- compatibility matrix;
- compression policy if any;
- decoding bounds;
- golden vectors;
- and migration law.

Until such a format exists, canonical metadata identity bytes are protocol bytes for identity derivation and
verification,
not a public persistent wire format.

---

## 22. Security and DoS Boundary

The protocol must defend against:

- hash collision amplification;
- malformed canonical bytes;
- unknown tag flooding;
- decoder progress attacks;
- deeply nested type signatures;
- recursive generic signatures;
- oversized strings/byte arrays;
- unbounded collection sorting;
- delimiter collision;
- compression bombs;
- low-entropy verifier misuse;
- runtime rekeying;
- and backend object identity leaks.

Required responses include:

- checked arithmetic;
- explicit depth/count/byte fuses;
- bounded diagnostics;
- fail-closed decoding;
- exact collision verification;
- source/acquisition boundary quarantine where applicable;
- no unbounded retry;
- no digest-only equality;
- and no post-visibility suite/width/seed changes.

---

## 23. Golden Vectors

A released implementation of this protocol MUST provide golden vectors for at least:

- canonical envelope header v1;
- each primitive integer width and endian rule;
- signed and unsigned boundary values;
- fixed-width and variable-width integer encoding where ratified;
- UTF-8 string normalization;
- invalid string input fail-closed;
- zero-length bytes;
- maximum-length bounded bytes;
- ordered collection encoding;
- unordered canonical sort encoding;
- duplicate collection policy;
- object/record field ordering;
- required field missing fail-closed;
- unknown tag fail-closed;
- unknown tag bounded skip where ratified;
- decoder progress on malformed zero-length inputs;
- canonical domain separation payload v1;
- reserved domain separation fields are zero;
- version-bundle fingerprint derivation;
- active digest suite invocation;
- each supported HID width;
- HID same input / same output;
- different domain / different descriptor;
- different schema / different descriptor;
- different canonical encoding version / different descriptor;
- HID collision verification fixture;
- HID match but canonical bytes differ fixture;
- TypeReference simple type identity;
- TypeReference generic type identity;
- TypeReference variance/projection identity;
- TypeReference F-bound depth fuse;
- TypeReference recursive edge fuse;
- TypeReference pre-SCC unresolved sentinel;
- TypeReference final TypeCycleKey replacement;
- pre-SCC TypeReference identity rejected as stable identity;
- final TypeReference identity rejects placeholder TypeCycleKey;
- compression boundary fixture;
- digest-suite migration fixture;
- runtime rekeying rejection fixture.

Golden vectors MUST include raw byte hex for canonical bytes and descriptor bytes.

They MUST NOT rely on display rendering.

---

## 24. Architecture Tests

A compliant implementation SHOULD provide architecture tests that assert:

1. backend handles do not enter canonical identity material;
2. reflection/KSP/PSI objects do not enter canonical bytes;
3. platform serialization is not used for canonical bytes;
4. display rendering is not used for equality;
5. canonical bytes are independent from backend traversal order;
6. canonical bytes are independent from worker/lane scheduling;
7. map/set iteration order does not affect canonical bytes;
8. unknown tags fail closed unless a bounded skip rule is ratified;
9. decoder progress is strict;
10. string encoding uses pinned UTF-8/normalization profile;
11. digest suite id/version affects descriptor bytes;
12. HID is never accepted as equality without exact verification;
13. TypeReference pre-SCC identity cannot be used as stable identity;
14. TypeReference generic expansion is explicitly bounded;
15. TypeCycleKey is final sealed material;
16. hot carrier packing is not treated as semantic equality;
17. compressed bytes are not canonical identity bytes;
18. runtime rekeying after visibility is impossible.

---

## 25. Compliance Rules

1. Metadata identity begins from ratified canonical material.
2. Canonical bytes are protocol-owned bytes.
3. Backend objects are not identity material.
4. Display strings are not identity material.
5. Platform serialization is not canonical encoding.
6. Canonical envelope/header layout is versioned protocol material.
7. Fixed-width numeric fields use ratified endian/width/signedness.
8. Variable-width integers require ratified width and decoder-progress rules.
9. Strings require pinned normalization and UTF-8 byte encoding.
10. Unknown tags fail closed unless bounded skip is ratified.
11. Decoders must make strict progress.
12. Canonical sorting must be deterministic and exact before publication.
13. General-purpose compression is not hot identity equality.
14. Domain separation is mandatory for every identity digest invocation.
15. `DigestDomainSeparationPayloadV1` is 56 bytes and not a cache-line claim.
16. Reserved bytes must be encoded as zero.
17. Digest algorithms are suite-profile material, not semantic identity authority.
18. HID descriptors are not equality authority.
19. HID equality requires exact verification before semantic equality is accepted.
20. Runtime collision pressure must not trigger post-visibility rekeying.
21. TypeReference identity material must not include backend type objects.
22. TypeReference generic expansion must be bounded.
23. Pre-SCC TypeReference identity is not final identity.
24. TypeCycleKey is integrated after SCC seal.
25. Physical carrier packing belongs to ADR-0042.
26. L2 exact equality belongs to full PlanCacheKey verification, not HID alone.
27. Persistent artifact format is out of scope until separately ratified.
28. Golden vectors are mandatory for released protocol behavior.

---

## 26. Alternatives Considered

### 26.1. Keep all protocol detail only in ADR-0041

Rejected.

ADR-0041 remains accepted, but it is too broad as the long-term maintenance surface for byte-level identity protocol,
interner mechanics, physical lifecycle, graph identity, and future memory-envelope policy.

### 26.2. Create `docs/specs/` immediately

Rejected for this extraction.

This protocol is currently an internal compiler/runtime design protocol.

A `docs/specs/` surface should be introduced only when Kontrakt ratifies an external, cross-language, persistent
binary, wire-format, or public compatibility specification.

### 26.3. Treat BLAKE3 or any specific algorithm as the semantic contract

Rejected.

The semantic contract is canonical bytes plus a versioned active digest suite profile.

A particular algorithm is replaceable through suite migration.

### 26.4. Treat HID as equality authority

Rejected.

Digest descriptors can collide.

HID is a pre-screen/descriptor and requires exact verification before equality-sensitive publication.

### 26.5. Use backend serialization as canonical encoding

Rejected.

Backend serializers may depend on field order, reflection order, defaults, platform versions, unknown field behavior, or
collection iteration.

Kontrakt-owned canonical encoding is required.

### 26.6. Add cache-line padding to domain separation payload

Rejected.

Domain separation payload size is protocol material.

Cache-line alignment is a physical substrate concern owned by ADR-0042.

### 26.7. Use general-purpose compression as canonical identity

Rejected.

Compression introduces algorithm settings, dictionary state, training data, and decompression safety concerns.

Compressed bytes are cold artifact concerns, not hot canonical identity bytes.

### 26.8. Require final TypeCycleKey before SCC discovery

Rejected.

That creates a chicken-and-egg dependency.

The lawful model is pre-SCC non-final TypeReference pre-identity followed by SCC seal and final TypeCycleKey
integration.

---

## 27. Consequences

Positive:

- stable metadata identity has a narrower maintenance surface;
- ADR-0041 can remain accepted without continuing to grow;
- digest algorithm agility is preserved;
- HID equality misuse is explicitly blocked;
- TypeReference cycle-key phasing is explicit;
- canonical bytes are separated from backend serialization;
- physical substrate concerns are cleanly delegated to ADR-0042;
- graph identity concerns are cleanly delegated to ADR-0043.

Negative:

- implementers must maintain golden vectors for canonical bytes and descriptor bytes;
- digest suite migrations require explicit versioning and compatibility law;
- canonical encoders require more implementation discipline than backend serialization;
- TypeReference identity requires explicit graph/SCC preflight;
- this document must remain synchronized with protocol-owned interning and ADR-0042/0043 extraction documents.

---

## 28. Final Rule

Stable metadata identity is protocol material.

It is not backend material.

It is not cache material.

It is not object identity.

A metadata identity must become canonical material, canonical bytes, domain-separated digest input, and
collision-verifiable
descriptor material before it may feed protocol-owned interning, frozen identity, planning identity, L2 key
construction,
or persistent artifact identity.

Compact descriptors accelerate identity processing.

They do not replace exact canonical verification.