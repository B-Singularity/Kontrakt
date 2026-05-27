# ADR-0041: Stable Metadata Identity, Digest/HID, and Protocol-Owned Interning — Extracted Maintenance Edition

## Status

Accepted

## Date

2026-05-27

## Maintenance Edition

This document is the extracted maintenance edition of ADR-0041.

The accepted monolithic ADR-0041 remains the archival source decision.

This maintenance edition is intentionally slim. It does not delete or weaken the accepted ADR-0041 logic. Instead, it
preserves ADR-0041's core decision, invariants, authority boundaries, non-regression rules, and extraction map while
delegating detailed maintenance to the narrower documents created after acceptance.

## Related

- ADR-0041 archival monolith: Stable Metadata Identity, BLAKE3/HID, and Protocol-Owned Interning
- `docs/design/stable-metadata-identity-protocol.md`
- `docs/design/protocol-owned-metadata-interning.md`
- ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0044: Unified Runtime Memory Envelope and Pipeline Lifecycle Governance
- `docs/constitution/compiler-core-protocols.md`

---

## 1. Purpose

ADR-0041 defines the accepted architecture for stable metadata identity in Kontrakt.

Its core pipeline remains:

``````text
ratified metadata material
-> canonical material
-> canonical bytes
-> canonical domain separation payload
-> active digest suite
-> HID / digest descriptor
-> exact collision verification
-> protocol-owned interning
-> deterministic stable intern id assignment
-> safe publication
``````

This maintenance edition exists because the accepted ADR-0041 monolith became too broad for long-term maintenance.

The extraction does not change what the system means.

It changes only where narrower maintenance happens.

---

## 2. Non-Replacement Rule

This maintenance edition MUST NOT be read as deleting the accepted ADR-0041 monolith.

The archival monolith remains the historical authority for the accepted decision.

This slim edition is a maintenance front door.

When this edition summarizes a topic, the summary preserves the original ADR-0041 intent and points to the extracted
maintenance document that owns the detailed law.

If a conflict exists before an extraction document is accepted, the archival ADR-0041 text wins.

After an extraction document is explicitly accepted as the narrower maintenance authority, conflict resolution follows
the authority boundary declared by that document.

---

## 3. Core Decision

Kontrakt metadata identity is protocol material.

It is not backend material.

It is not runtime object identity.

It is not cache identity.

It is not physical table identity.

Metadata identity MUST be derived from ratified canonical material and canonical protocol bytes.

Compact descriptors, HIDs, route keys, stable intern ids, and physical table positions may accelerate the pipeline.

They MUST NOT replace exact canonical verification where semantic equality is at stake.

---

## 4. Preserved Invariants

The following invariants are preserved from the accepted ADR-0041 monolith and remain normative.

### 4.1. Canonical material invariant

Metadata identity begins from ratified canonical material.

Backend handles, reflection objects, KSP/PSI descriptors, annotation descriptor objects, DTO runtime objects, display
strings, object addresses, JVM identity, source traversal accidents, classpath order, service-loader order, and runtime
cache state MUST NOT become metadata identity authority.

### 4.2. Canonical byte invariant

Canonical bytes are Kontrakt-owned deterministic protocol bytes.

They are not JVM object serialization.

They are not backend serializer output.

They are not display rendering.

They are not compressed runtime bytes.

### 4.3. Domain separation invariant

Every digest invocation used for metadata identity MUST include canonical domain separation material.

Domain separation must be byte-level, versioned, deterministic, and golden-vector covered.

### 4.4. Digest suite invariant

The digest algorithm is not the semantic contract.

The semantic contract is:

``````text
canonical identity bytes
+ canonical domain separation payload
+ active digest-suite profile
+ requested output width
-> deterministic descriptor bytes
``````

Digest algorithms, requested widths, suite ids, derivation modes, and XOF/output rules are versioned protocol profiles.

### 4.5. HID-not-equality invariant

A HID is a compact candidate descriptor.

It is not semantic equality authority.

The forbidden shape is:

``````text
HID match
-> equality accepted
``````

The required shape is:

``````text
HID match
-> domain/version/suite/width/length checks
-> exact collision verification
-> equality accepted only if canonical material matches
``````

### 4.6. Collision verification invariant

Every equality-sensitive compact identity path MUST have exact verification.

Collision groups are bounded.

Cold collision containment is the final bounded escalation.

Cold overflow fails closed or quarantines under resolved policy.

The implementation MUST NOT accept digest-only equality.

### 4.7. Runtime rekeying prohibition

Observed collision pressure, queue pressure, cold overflow, or adversarial input MUST NOT cause post-visibility changes
to
digest suite, seed, HID width, route map, version bundle fingerprint, canonical encoding policy, or stable id order.

A stronger-width path is lawful only if selected before scope admission by resolved policy and golden vectors.

### 4.8. Protocol-owned interning invariant

Kontrakt interns canonical identity material.

It does not intern runtime objects.

A stable intern id is valid only after candidate admission, exact collision verification, deterministic assignment, and
safe publication inside an explicit compatible scope.

### 4.9. Stable id determinism invariant

Stable intern id assignment MUST NOT depend on physical table slot, insertion race, worker/lane id, CAS winner order,
backend traversal order, queue timing, object allocation order, or cache warmness.

### 4.10. Publication-before-visibility invariant

Partial intern tables MUST NOT become planning-visible.

No consumer may observe a stable intern id before the owning table/slice/scope publication boundary has linearized.

### 4.11. TypeReference phasing invariant

Final TypeReference identity includes TypeCycleKey material.

However, final TypeCycleKey is derived after pre-SCC graph construction and SCC seal.

Pre-SCC TypeReference pre-identity is not stable identity and MUST NOT become PlanCacheKey, frozen-image, report/replay,
persistent artifact, cross-scope, or query reuse identity.

### 4.12. Mechanical substrate separation invariant

Primitive arrays, slabs, MemorySegment storage, off-heap addresses, cache-line grouping, owner-lane queues, dispatch
lanes, route buffers, and physical carrier packing are implementation mechanics.

They may accelerate identity processing.

They do not define metadata equality.

### 4.13. Graph identity separation invariant

Contract graph identity is layered above the metadata identity substrate.

Graph structural/contextual identity, sealed structural references, SCC graph sealing, and incremental derivation are
maintained by ADR-0043.

They must consume ADR-0041 identity guarantees.

They must not redefine canonical bytes or HID equality.

### 4.14. Cache-blind consumer invariant

L2 cache hit/miss, join timing, partition drop, dispatch timing, circuit-open state, or cache warmth MUST NOT alter
canonical metadata identity, stable intern id assignment, TypeReference identity, canonical topology, or semantic
planning result.

### 4.15. Fail-closed invariant

Malformed, unsupported, overflowing, ambiguous, unbounded, or incompatible identity material must fail closed before
publication.

The implementation MUST NOT discover protocol impossibility through JVM `OutOfMemoryError`, `StackOverflowError`,
unbounded exception storms, or silent partial publication.

---

## 5. Extraction Authority Map

ADR-0041 delegates detailed post-acceptance maintenance to the following documents.

### 5.1. Stable metadata identity protocol

`docs/design/stable-metadata-identity-protocol.md` owns:

- canonical material law;
- canonical byte encoding law;
- canonical envelope/header law;
- object/record/message encoding;
- unknown tag and decoder bounds;
- canonical sorting where protocol-semantic;
- domain separation payload;
- digest suite abstraction;
- HID descriptor law;
- HID-not-equality law;
- TypeReference canonical material;
- TypeReference HID law;
- TypeReference pre-SCC/final identity phasing;
- protocol golden vectors.

It MUST NOT own interner publication mechanics, physical substrate mechanics, graph identity, or L2 cache governance.

### 5.2. Protocol-owned metadata interning

`docs/design/protocol-owned-metadata-interning.md` owns:

- intern scope law;
- candidate admission;
- staged-byte admission and failure policy;
- provisional handle law;
- collision verification;
- bounded cold collision containment;
- deterministic stable intern id assignment;
- no partial table visibility;
- publication law;
- consumer boundaries for frozen image, planning, and L2;
- interning golden vectors.

It MUST NOT own canonical byte derivation, physical lane transport, slab/arena lifecycle, graph structural identity, or
L2 eviction/governance.

### 5.3. Mechanical substrate and physical lifecycle

ADR-0042 owns:

- primitive substrate lifecycle;
- staging/scratch/published/retired state;
- zero-copy slice lifetime;
- slab/arena/epoch reclamation;
- reader leases;
- async reclamation;
- owner-lane transport;
- routed candidate batching;
- boundary flush storm headroom;
- inbox backpressure;
- self-drain and cooperative drain limits;
- producer-local suppression memory;
- verification ladder physical ordering;
- inline verifier entropy and selector evidence gates;
- small-inline and preclassification profiles;
- segmented/paged extension;
- whole-table grow-by-copy prohibition;
- physical reclamation lag;
- hot primitive carrier packing.

ADR-0042 MUST NOT redefine canonical equality, HID meaning, or stable intern id assignment.

### 5.4. Contract graph identity

ADR-0043 owns:

- contract graph units;
- structural identity;
- contextual identity;
- sealed structural references;
- parent/child graph reference law;
- graph interning;
- graph SCC sealing;
- graph symmetry handling;
- graph snapshot/materialization;
- incremental affected-set derivation;
- full rebuild preflight;
- graph/L2 query integration;
- graph/physical layer separation.

ADR-0043 consumes ADR-0041 identity protocol and ADR-0042 substrate guarantees.

### 5.5. Unified runtime memory envelope

ADR-0044 owns:

- cross-pipeline memory envelope governance;
- frozen acquisition memory slice;
- metadata identity/interner memory slice;
- planning L1 memory slice;
- contract graph memory slice;
- L2 memory slice;
- VM execution memory slice;
- reporting/diagnostic memory slice;
- emergency diagnostic reserve;
- retired epoch reserve;
- continuation/restart reserve;
- quarantine reserve;
- lifecycle closure and reclamation priority.

ADR-0044 MUST NOT redefine identity equality or physical substrate mechanics.

### 5.6. Compiler constitution enforcement

`docs/constitution/compiler-core-protocols.md` owns enforcement hooks such as:

- no backend handles in core identity;
- no runtime object authority after normalization;
- no boxed hot-path identity carriers;
- no per-candidate wrapper allocation in primitive identity paths;
- deterministic collection enforcement;
- safe publication enforcement;
- Local IR / Canonical IR boundary enforcement;
- no adapter material crossing canonical identity boundaries.

It is an enforcement index, not the maintenance location for identity protocol semantics.

---

## 6. Maintenance Procedure

Future changes to stable metadata identity must follow this procedure.

1. Identify the owning extraction document.
2. Make the detailed amendment there.
3. Update ADR-0041 maintenance edition only if the change affects a preserved invariant or extraction map.
4. Add or update golden vectors.
5. Add or update architecture tests.
6. Verify the non-regression checklist.
7. Avoid re-expanding ADR-0041 into a catch-all design document.

---

## 7. Non-Regression Checklist

Every extraction or amendment must preserve the following.

``````text
Does canonical material remain backend-erased?
Does canonical byte encoding remain deterministic and Kontrakt-owned?
Does every identity digest remain domain-separated?
Does HID remain non-authoritative for semantic equality?
Does every equality-sensitive compact descriptor have exact verification?
Does collision overflow fail closed or quarantine under resolved policy?
Is runtime rekeying after visibility still impossible?
Are stable ids still deterministic and independent from physical placement?
Are provisional handles still non-stable and non-public?
Is publication-before-visibility still enforced?
Are TypeReference pre-SCC identities still non-final?
Is TypeCycleKey still final sealed material?
Are physical substrate mechanics still outside semantic equality authority?
Are graph identity laws still owned by ADR-0043?
Are L2/cache hits still cache-blind?
Are golden vectors updated?
Are architecture tests updated?
``````

A failed checklist item means the extraction or amendment is incomplete.

---

## 8. Golden Vector Continuity

This slim edition does not repeat all golden vectors.

The required golden-vector surfaces are maintained by the extracted documents.

At minimum, the combined extraction set must cover:

- canonical byte encoding;
- domain separation payload;
- digest suite invocation;
- HID descriptor widths;
- HID collision verification;
- TypeReference identity;
- TypeCycleKey phasing;
- staged byte failure policy;
- stable intern id assignment;
- cold collision containment;
- publication law;
- mechanical substrate equivalence;
- graph structural/contextual identity;
- incremental derivation equivalence;
- runtime memory envelope admission.

---

## 9. Architecture Test Continuity

The architecture-test obligation remains.

The extracted documents and compiler constitution must ensure that implementation cannot regress into:

- backend-object identity;
- reflection/KSP object identity;
- object graph interning;
- HID-only equality;
- table-slot identity;
- wrapper allocation on hot identity paths;
- partial publication;
- unbounded collision chains;
- runtime rekeying;
- cache-dependent semantics;
- source traversal order dependency;
- physical address dependency;
- JVM stack/OOM boundary discovery.

---

## 10. Alternatives Considered

### 10.1. Keep ADR-0041 as the only maintenance document

Rejected.

The accepted monolith is useful as a historical decision record, but it is too broad to remain the only maintenance
surface.

### 10.2. Delete the monolith and keep only extracted documents

Rejected.

That would lose the accepted decision context and make it harder to audit whether extraction changed the original
invariants.

### 10.3. Move all extracted content into ADR-0042 and ADR-0043

Rejected.

ADR-0042 is physical/mechanical substrate.

ADR-0043 is contract graph identity.

The byte-level metadata identity protocol and metadata interning semantics need their own design notes.

### 10.4. Create `docs/specs/` immediately

Rejected for this stage.

The current extraction is internal compiler/runtime protocol maintenance, not yet an external persistent artifact or
cross-language wire-format specification.

---

## 11. Consequences

Positive:

- ADR-0041 stops growing as a monolith;
- core identity invariants remain visible;
- each detailed law has a narrower maintenance surface;
- physical backend mechanics are separated from semantic identity;
- graph identity is separated from metadata identity;
- interning semantics are separated from canonical byte protocol;
- future ADR-0044 can handle cross-pipeline memory envelopes without bloating ADR-0041.

Negative:

- maintainers must keep extraction documents synchronized;
- changes require cross-document non-regression review;
- links and authority boundaries must be kept precise;
- some duplication remains intentional for auditability.

---

## 12. Final Rule

ADR-0041 remains the accepted parent decision for stable metadata identity.

The slim maintenance edition preserves its invariants and routes detailed maintenance to narrower documents.

Extraction may reduce duplication.

Extraction may not change identity semantics.

The same metadata identity still means the same canonical material, the same canonical bytes, the same domain-separated
digest input, the same verified collision result, the same scoped stable intern id, and the same cache-blind semantic
result.