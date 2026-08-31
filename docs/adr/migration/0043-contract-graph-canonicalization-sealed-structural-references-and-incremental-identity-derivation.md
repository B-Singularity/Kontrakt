# ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation

## Status

Migration Pending

## Date

2026-05-23

## Related

- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0037: Cycle Identity Preflight and Deferred Raw Fact Resolution
- ADR-0035: Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery
- ADR-0034: Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- `../../constitution/compiler-core-protocols.md`
- `docs/the-most-important-thing/interface-as-contract.md` (forthcoming top-level contract definition)
- `docs/design/canonical-ir-stage-and-lowering-protocol.md`
- `../../design/l1-planner-session-primitive-data-structures.md`
- `../../design/l2-plan-interner-partitioned-tier2-with-governance.md`
- `../../design/stable-metadata-identity-protocol.md` (post-ADR-0041 extraction target)
- `../../design/protocol-owned-metadata-interning.md` (post-ADR-0041 extraction target)
- ADR-0044: Unified Runtime Memory Envelope and Pipeline Lifecycle Governance (planned)

---

## 1. Context

ADR-0041 defines the stable metadata identity substrate:

``````text
canonical material
-> canonical bytes
-> active digest suite
-> HID / digest descriptor
-> collision verification
-> protocol-owned interning
-> deterministic stable intern id assignment
``````

ADR-0042 defines the mechanical substrate and ownership laws needed to store, publish, read, and reclaim primitive state
without allowing physical layout to become semantic identity.

However, Kontrakt's contract model is broader than metadata identity.

The target contract-native software model includes:

- interface contract meaning;
- state transitions;
- protocols;
- data schema and data movement;
- DTO and boundary semantics;
- governance and policy;
- explicit state machines;
- lifecycle rules;
- verification obligations;
- reporting and diagnostic contracts;
- and future static / dynamic / hybrid verification surfaces.

These contract elements will eventually lower into canonical contract graph material.

ADR-0041 deliberately reserves many of these surfaces because the top-level contract definition is not yet ratified.

ADR-0043 is therefore a draft bridge between:

``````text
future contract definition
-> canonical contract graph units
-> structural identity
-> contextual identity
-> graph interning
-> incremental derivation
-> ADR-0041 digest/HID/interner substrate
-> ADR-0042 physical lifecycle substrate
``````

This ADR does not finalize the contract ontology.

It defines the graph-identity architecture that the forthcoming contract definition can use without overloading
ADR-0041.

## 2. Problem

If contract graph identity is left implicit, Kontrakt risks falling back into object-graph behavior.

The main failure modes are:

1. **recursive child byte inlining**

   Parent identity recursively embeds child canonical bytes, grandchildren, raw facts, and contract facts.

   This causes repeated byte expansion and makes diamond-shaped graphs expensive.

2. **hierarchical avalanche**

   A parent identity change forces every child to re-encode and rehash full child-local canonical bytes even when the
   child-local material did not change.

3. **bare HID as proof**

   A compact child HID is treated as if it proves child equality.

   This violates ADR-0041, where HID is a candidate descriptor, not equality authority.

4. **graph object interning**

   Runtime graph nodes, backend handles, DTO objects, annotation descriptors, or source AST nodes are interned directly.

   This leaks backend or physical identity into contract identity.

5. **cycle ambiguity**

   Cyclic contract material cannot be represented as a naive Merkle tree.

   It requires deterministic SCC sealing.

6. **incremental cache unsafety**

   A cache hit on a previous graph unit may be used after version, parent context, edge role, or contract fact semantics
   changed.

7. **semantic and physical layer collapse**

   Primitive tables, slabs, MemorySegment storage, or off-heap handles are mistaken for contract graph authority.

ADR-0043 exists to prevent these failures.

## 3. Decision

Kontrakt will represent contract graph identity through canonical graph units and sealed references.

The central decision is:

``````text
Do not recursively inline arbitrary child graph bytes into parent identity.

Seal child-local material first.

Parents refer to sealed child structural references.

Contextual identity is derived from parent/context material plus sealed structural references.
``````

ADR-0043 introduces:

- canonical contract graph units;
- structural identity;
- contextual identity;
- sealed structural identity references;
- Merkle-like reference encoding;
- graph interning;
- SCC graph sealing;
- incremental derivation boundaries;
- and query/cache integration rules.

This ADR is subordinate to the forthcoming top-level contract definition for concrete contract fact vocabulary.

It is authoritative for the graph identity shape once contract facts are ratified.

## 4. Authority and Boundaries

ADR-0043 owns:

- contract graph unit partitioning law;
- structural versus contextual identity law;
- sealed structural reference law;
- parent/child reference encoding law;
- graph interning law;
- contract graph SCC sealing law;
- incremental graph identity derivation law;
- dependency invalidation law for graph identity;
- and L2/query integration rules for graph units.

ADR-0043 does not own:

- final contract fact taxonomy;
- final contract annotation / DSL / compiler metadata syntax;
- frontend parity law;
- default/effective value semantics;
- public user-facing contract language;
- physical slab implementation;
- exact digest algorithm implementation;
- or execution scheduling mechanics.

Ownership split:

| Surface                                                  | Owner                                  |
|----------------------------------------------------------|----------------------------------------|
| digest/HID/interner substrate                            | ADR-0041                               |
| primitive lifecycle and physical backend                 | ADR-0042                               |
| contract graph identity and incremental graph derivation | ADR-0043                               |
| concrete contract fact ontology                          | top-level contract definition document |
| execution state-machine runtime                          | future execution/runtime ADRs          |
| public contract syntax                                   | future frontend contract documents     |
| unified runtime memory envelope                          | ADR-0044 (planned)                     |

## 5. Vocabulary

### 5.1. Contract Graph Unit

A contract graph unit is the smallest ratified contract-meaning unit that may be independently canonicalized, sealed,
interned, referenced, invalidated, or reused.

Examples after contract ratification may include:

- type reference contract unit;
- lowered contract fact unit;
- protocol transition unit;
- state-machine state unit;
- state-machine transition unit;
- DTO boundary unit;
- governance policy unit;
- data schema unit;
- verification obligation unit;
- diagnostic rule unit;
- capability-profile unit;
- adapter boundary unit.

This draft does not activate those concrete units.

It defines the identity law they must follow once activated.

### 5.2. Structural Identity

Structural identity is parent-independent identity for a graph unit's local canonical meaning.

Structural identity answers:

``````text
What is this unit, independent of where a parent uses it?
``````

Structural identity is derived from:

- the unit identity domain;
- local canonical material;
- local schema/version material;
- local dependency references that are structural under the owning domain;
- active digest suite;
- and the ADR-0041 verification path.

Structural identity MUST NOT depend on:

- parent identity;
- acquisition order;
- backend traversal order;
- callback timing;
- thread or lane assignment;
- object allocation order;
- diagnostic rendering;
- or physical storage address.

### 5.3. Contextual Identity

Contextual identity is parent/context-dependent identity for a graph unit under a specific use site, edge role,
selector, state, protocol position, boundary, or governance context.

Contextual identity answers:

``````text
What does this sealed unit mean in this parent/context?
``````

Contextual identity MAY depend on:

- parent structural or contextual identity;
- edge role;
- local selector tuple;
- position semantics where the domain ratifies position as semantic;
- protocol transition role;
- state-machine region;
- DTO boundary direction;
- governance scope;
- capability profile;
- and version-bundle material.

Contextual identity MUST NOT skip child structural sealing.

### 5.4. Sealed Structural Identity Reference

A sealed structural identity reference is a fixed-width or bounded-width canonical reference to a child unit whose
structural identity has passed the owning seal boundary.

It is not a bare HID.

It is not a raw pointer.

It is not a backend handle.

It is not a JVM object reference.

It is a protocol reference with enough material to verify that the referred child identity is compatible with the
current derivation boundary.

Illustrative shape:

``````text
SealedStructuralIdentityRef

identityDomainId32
domainSchemaVersion32
canonicalEncodingVersion32
hashSuite16
hidDerivationVersion16
versionBundleFingerprintHigh64
versionBundleFingerprintLow64
structuralDescriptorWidth16
structuralDescriptorWords
canonicalByteLength32
stableInternScopeId32 where applicable
stableInternId64 where applicable
sealEpoch32 or artifact epoch where applicable
``````

The exact physical shape is not frozen by this draft.

The invariant is normative:

``````text
bare HID alone is insufficient
sealed structural reference includes domain, version, width, and seal/provenance material
``````

### 5.5. Graph Intern Scope

A graph intern scope is the explicit scope within which sealed graph units receive stable dense references.

Allowed future scopes may include:

``````text
CONTRACT_GRAPH_LOCAL
FROZEN_IMAGE_LOCAL
MODULE_CONTRACT_GRAPH_LOCAL
PLANNING_RUN_LOCAL
PERSISTED_CONTRACT_ARTIFACT_LOCAL
``````

The scope is part of the reference meaning.

Stable ids from different scopes are not comparable unless a conversion law is ratified.

### 5.6. Graph Dependency Edge

A graph dependency edge is a ratified relationship from one contract graph unit to another.

Examples after contract ratification may include:

- state transition references state;
- protocol step references message schema;
- DTO boundary references data schema;
- governance policy references capability profile;
- verification obligation references subject type;
- contract fact references another contract fact.

Edges may be:

- structural;
- contextual;
- ordered;
- unordered;
- optional;
- required;
- SCC-local;
- diagnostic-only;
- or non-identity-bearing.

The owning contract domain must classify the edge.

## 6. Contract Graph Unit Boundary Law

A contract graph unit MUST be explicitly ratified by an owning contract domain.

A graph unit MUST define:

- identity domain id;
- schema version;
- local canonical material;
- structural dependency edges;
- contextual dependency edges, if any;
- canonical encoding law;
- duplicate edge policy;
- cycle policy;
- version-bundle axes;
- capacity caps;
- verification payload;
- and golden vectors.

A graph unit MUST NOT be inferred merely from:

- Kotlin class shape;
- annotation object shape;
- DTO class name;
- backend AST node;
- reflection handle;
- source declaration order;
- or incidental object graph topology.

The lawful shape is:

``````text
contract surface
-> backend-erased lowering evidence
-> ratified lowered contract fact / graph unit
-> canonical material
-> structural identity
-> sealed reference
``````

The forbidden shape is:

``````text
annotation object graph
-> direct graph node identity
``````

or:

``````text
reflection / KSP / PSI handle
-> direct graph edge identity
``````

## 7. Structural Identity Law

Every reusable contract graph unit SHOULD define structural identity unless the owning domain explicitly proves that the
unit is inherently contextual.

Structural identity derivation shape:

``````text
unit-local canonical material
+ structural child references where ratified
+ local version bundle
+ identity domain
-> ADR-0041 canonical bytes
-> active digest-suite structural descriptor
-> collision verification
-> sealed structural identity reference
``````

Structural identity MUST NOT include:

- parent identity;
- parent path;
- edge role that belongs only to a parent;
- source traversal order unless the domain ratifies declaration order as semantic;
- acquisition order;
- callback completion order;
- physical storage address;
- or live cache state.

Structural identity MAY include child references only when those child references are themselves sealed structural
identity references or SCC-governed temporary references.

## 8. Contextual Identity Law

A contextual identity is derived only after the structural material it references is sealed.

Contextual derivation shape:

``````text
parent/context key
+ sealed structural identity reference
+ local selector / edge role / position semantics where ratified
+ active version bundle
-> active digest-suite contextual descriptor
-> compatibility classification
-> publication gate
``````

A contextual descriptor is also a compact candidate descriptor.

It is not semantic equality authority by itself.

A contextual identity MUST NOT be published unless:

- parent context is version-compatible;
- child sealed reference is version-compatible;
- local selector / edge role material is ratified;
- active digest suite is classified;
- collision verification payload exists;
- and publication epoch rules are satisfied.

Forbidden:

``````text
bare child HID
-> parent key
-> contextual HID
-> publish
``````

Required:

``````text
sealed child structural reference
-> parent/context derivation
-> verification / compatibility / publication gate
``````

## 9. Sealed Structural Reference Encoding Law

Parent canonical material MUST NOT recursively inline arbitrary child canonical bytes.

Instead, when a child unit has an independent structural identity, parent canonical material SHOULD encode the child's
sealed structural reference.

Lawful parent encoding:

``````text
parent-local canonical fields
+ edge role / selector where ratified
+ sealed child structural reference
``````

Forbidden parent encoding:

``````text
parent-local canonical fields
+ recursively expanded child canonical bytes
+ recursively expanded grandchild canonical bytes
+ backend object graph closure
``````

A domain may inline a small child payload only if:

- the child has no independent identity boundary;
- the payload is bounded by policy;
- inlining is part of the canonical schema;
- the same material cannot also appear as a separate sealed identity reference in the same equality surface;
- and golden vectors cover the inlining behavior.

## 10. Graph Interning Law

Graph interning is protocol-owned.

It is not object interning.

A graph interner consumes:

``````text
contract graph unit canonical bytes
+ identity domain
+ version bundle
+ structural descriptor
+ verification payload
``````

It produces:

``````text
sealed structural identity reference
stable graph intern id where ratified
``````

It MUST NOT consume:

- backend handles;
- annotation object identity;
- DTO object identity;
- JVM `hashCode()`;
- source AST object identity;
- mutable global insertion order;
- worker completion order;
- or platform hash table iteration order.

Graph interning is allowed to share repeated graph units across:

- repeated TypeReferences;
- repeated lowered contract facts;
- repeated protocol steps;
- repeated data schemas;
- repeated DTO boundary schemas;
- repeated governance policies;
- repeated verification obligations;
- repeated state-machine fragments;
- and repeated diagnostic rule units once those domains are ratified.

A graph interner MUST remain collision-verified under ADR-0041.

## 11. Merkle-Like DAG Reference Law

Acyclic contract graph regions SHOULD use Merkle-like sealed references.

This means:

``````text
child structural identity first
-> parent references sealed child identity
-> parent structural/contextual identity
``````

It does not mean:

``````text
HID-only equality
``````

or:

``````text
recursive tree expansion of a DAG
``````

Diamond-shaped dependency graphs MUST NOT cause repeated expansion of the same sealed child material in parent identity
unless the owning domain explicitly ratifies duplicate expansion as semantic.

Preferred shape for a diamond graph:

``````text
A
-> B
-> D

A
-> C
-> D

D seals once inside the graph intern scope.
B and C reference D's sealed structural identity reference.
A references B and C.
``````

This avoids accidental tree expansion.

## 12. SCC Graph Identity Law

Cyclic contract graph material MUST use deterministic SCC sealing.

A graph cycle MUST NOT be broken by:

- object identity;
- allocation order;
- backend traversal order;
- worker completion order;
- random temporary ids;
- or planning cycle truncation.

SCC sealing shape:

``````text
detect graph SCC
-> assign deterministic SCC-local temporary references
-> encode SCC seal payload
-> derive SCC structural descriptor
-> collision verification
-> publish sealed SCC identity group
-> resolve external references to sealed identities
``````

SCC-local references are valid only inside the SCC seal payload.

After sealing, external graph material must reference sealed structural identity references or stable graph intern ids.

## 13. Incremental Derivation Law

Incremental derivation is lawful only when it preserves from-scratch identity equivalence.

The from-scratch reference pipeline is:

``````text
contract graph material
-> canonical graph units
-> structural identities
-> contextual identities where required
-> graph interning
-> publication
``````

An incremental implementation must produce the same result as that pipeline for all admitted material.

Allowed reuse:

- sealed child structural identity reference;
- sealed child-local canonical bytes;
- stable graph intern id inside compatible scope;
- fixed-width structural descriptor;
- domain/version compatibility classification;
- dependency graph edge set;
- previous query result only when all dependencies are unchanged and version-compatible.

Forbidden reuse:

- old contextual identity after parent context changed;
- old graph intern id from incompatible scope;
- child HID without seal/provenance material;
- cached equality without collision verification;
- dependency result after an unclassified contract fact version change;
- cache hit selected by object identity or backend traversal order.

### 13.1. Bounded Cost Claim

When child-local material is unchanged and sealed, parent/context derivation MAY use the sealed structural reference
rather than re-reading child canonical bytes.

The allowed cost claim is:

``````text
fixed-width-per-child-reference with respect to child canonical byte length
``````

The forbidden cost claim is:

``````text
constant time for an entire affected subtree regardless of affected reference count
``````

If a parent has `N` child references, the parent still has at least `O(N)` reference processing unless a future ADR
ratifies a stronger dependency summary law.

### 13.2. Dependency Invalidation

Every incremental graph cache entry MUST declare its dependencies.

Dependencies may include:

- structural child references;
- contextual parent keys;
- edge roles;
- local selector tuples;
- version bundles;
- capability profiles;
- governance policies;
- state-machine protocol versions;
- data-schema versions;
- boundary/DTO schema versions;
- digest suite ids;
- interning protocol versions;
- and contract frontend lowering versions.

If any dependency changes in a way classified as non-equivalent by the owning compatibility matrix, the cache entry is
invalid.

Invalid means:

``````text
must not publish from cached value
``````

It does not necessarily mean:

``````text
process panic
``````

## 14. L2 / Query / Cache Integration Law

L2 and future query layers may cache graph units.

They may cache:

- sealed structural identity references;
- contextual descriptors;
- graph dependency edges;
- compatibility classifications;
- canonical graph unit bytes where budgeted;
- stable graph intern ids;
- query results over sealed graph units.

They MUST NOT treat a cache hit as semantic authority without ADR-0041 verification.

A graph cache key MUST include, or be derivable from:

- identity domain;
- graph unit schema version;
- structural/contextual identity kind;
- active digest suite id/version;
- version-bundle fingerprint;
- graph intern scope;
- and all dependency axes required by the owning domain.

L2 callback or asynchronous completion MUST enter through the ADR-0042 event-ingestion boundary.

A callback MUST NOT directly mutate graph identity state.

## 15. Mechanical Substrate Integration

ADR-0043 does not require a particular physical substrate.

A compliant implementation may store graph identity material in:

- heap primitive arrays;
- packed primitive tables;
- published sealed slabs;
- MemorySegment-backed substrates;
- off-heap aligned substrates;
- mapped artifact substrates;
- or future native substrates.

Physical backend choice MUST NOT change:

- canonical bytes;
- structural identity;
- contextual identity;
- sealed structural references;
- graph intern ids;
- dependency invalidation results;
- equality results;
- or golden vectors.

Physical storage, reader epochs, lane ownership, async reclaimer behavior, and primitive lifecycle are governed by
ADR-0042.

## 16. Contract Definition Reservation

This ADR is intentionally incomplete until the top-level contract definition is ratified.

The forthcoming contract definition must define:

- what contract facts exist;
- how interface, annotation, DSL, compiler metadata, generated indexes, DTOs, boundary declarations, governance
  policies, protocol rules, and state-machine declarations lower into those facts;
- which facts are graph units;
- which edges are structural or contextual;
- which edges are ordered or unordered;
- which facts may form SCCs;
- which facts are verification obligations;
- which facts are diagnostic-only;
- and how frontend parity is decided.

Until then, ADR-0043 is a graph identity skeleton.

It does not activate a concrete user-facing contract language.

## 17. ADR-0041 Contract Graph Extraction Addendum

This addendum extracts the graph-facing material from ADR-0041 into ADR-0043 without changing the accepted ADR-0041
metadata identity substrate.

ADR-0043 remains a graph-identity layer.

It consumes:

- ADR-0041 canonical identity protocol;
- ADR-0041 digest/HID/interner substrate;
- post-ADR-0041 `../../design/stable-metadata-identity-protocol.md`;
- post-ADR-0041 `../../design/protocol-owned-metadata-interning.md`;
- ADR-0042 primitive substrate lifecycle;
- and the forthcoming top-level contract definition.

It MUST NOT redefine canonical byte encoding, HID equality meaning, metadata interning, physical substrate ownership, or
final contract ontology.

### 17.1. Graph-Lowered Contract Fact Boundary

Future contract facts for state, protocol, data, governance, DTO boundaries, verification obligations, diagnostics, and
explicit state machines MUST lower into ratified contract graph units before they become graph identity material.

The lawful graph lowering shape is:

``````text
contract syntax / annotation / DSL / compiler metadata / generated index
-> adapter-erased lowering
-> lowered contract fact
-> ratified graph unit
-> canonical graph material
-> structural identity
-> sealed structural reference
-> contextual identity where required
``````

The forbidden graph lowering shape is:

``````text
annotation object / DTO object / backend handle / source AST node
-> direct graph identity
``````

A graph unit MUST NOT treat frontend syntax, backend descriptors, annotation descriptors, DTO runtime objects, or source
spelling as graph identity authority merely because those surfaces are convenient to enumerate.

### 17.2. Structural and Contextual Identity Stratification

ADR-0043 adopts the structural/contextual split for contract graph identity.

Structural identity is the graph-unit-local, parent-independent identity.

Contextual identity is parent/use-site/edge-role/boundary/protocol-position identity.

The following law is normative:

``````text
structural identity first
-> sealed structural reference
-> contextual derivation
``````

The reverse order is forbidden:

``````text
parent context first
-> recursively derive child local identity through parent
-> publish child identity as if parent-independent
``````

A contextual descriptor MUST NOT be accepted as proof that the structural child is equal.

It is a candidate descriptor for the contextual surface and must preserve collision verification and compatibility
boundaries.

### 17.3. Sealed Child Reference Derivation

A parent graph unit may reference a child only through:

- sealed structural identity reference;
- stable graph intern id inside a compatible graph intern scope;
- SCC-local temporary reference inside the same SCC seal payload;
- or bounded inline child material ratified by the owning graph unit schema.

A parent graph unit MUST NOT reference:

- bare HID;
- provisional handle;
- backend handle;
- source object;
- DTO object;
- annotation descriptor object;
- runtime graph node object;
- or child material that has not crossed a seal boundary.

A sealed child reference MUST include enough domain/version/width/provenance material to prevent accidental equality
across incompatible graph domains or schema versions.

### 17.4. Graph Interning over ADR-0041 Interner Substrate

Graph interning is layered over the ADR-0041 metadata/interner substrate.

It is not separate object interning.

A graph interner MAY use ADR-0041 stable metadata/interner primitives such as:

- canonical bytes;
- active digest suite descriptors;
- HID descriptors as candidate descriptors;
- collision verification payloads;
- scoped stable intern ids;
- publication law;
- and bounded diagnostics.

However, graph interning MUST define its own graph-unit identity domains, graph intern scopes, and graph-unit
compatibility axes.

A graph stable intern id is not automatically comparable to a metadata stable intern id.

Cross-scope conversion requires an explicit conversion law.

### 17.5. Graph SCC Sealing and Symmetry

Contract graph SCC sealing follows the same high-level safety law as ADR-0041 metadata SCC sealing but is owned here for
graph units.

A contract graph SCC MUST define:

- SCC member capacity profile;
- SCC canonical byte budget;
- SCC intra-edge budget;
- SCC-local temporary reference encoding;
- deterministic member ordering;
- deterministic edge ordering;
- symmetry-breaking law;
- snapshot-backed measure/write materialization where needed;
- bounded diagnostic policy;
- and fail-closed behavior before publication.

Graph SCC-local temporary references MUST NOT escape the SCC seal payload.

If SCC member ordering cannot be resolved by ratified canonical graph material, the implementation MUST either:

- collapse members only under exact graph canonical equivalence proof; or
- reject the SCC seal before publication.

It MUST NOT break symmetry through discovery order, backend index, object identity, worker/lane id, callback order, or
source enumeration accident.

### 17.6. Graph Snapshot and Materialization

Graph SCC materialization and large graph-unit materialization MAY use two-phase sizing, chunked measure/write
interleaving, and snapshot-backed canonical IR materialization.

If used, the graph materialization plan MUST preserve:

- same canonical graph bytes as the single-pass reference;
- same structural identity;
- same contextual identity;
- same graph intern id assignment;
- same dependency invalidation result;
- and same L2/query key behavior.

The materialization pass MUST NOT reread mutable backend/source/adapter objects after the measure pass has accepted byte
counts or edge counts.

Snapshot or memoized graph IR used for materialization is staging material.

It is not persistent artifact identity unless a future artifact format ratifies it.

### 17.7. Incremental Affected-Set Derivation

Incremental graph identity derivation MUST classify affected sets explicitly.

A graph incremental implementation MUST define:

- direct changed graph units;
- structurally affected parent units;
- contextually affected use sites;
- dependency edge changes;
- version-bundle changes;
- graph intern scope changes;
- compatibility-equivalent changes;
- and compatibility-breaking changes.

Incremental affected-set discovery MUST be budgeted.

At minimum, a compliant graph profile SHOULD define:

``````text
maxGraphInvalidationTraversalEdges
maxGraphInvalidationVisitedUnits
maxGraphInvalidationFrontierUnits
maxGraphInvalidationScratchBytes
maxGraphInvalidationDiagnosticsBytes
``````

If affected-set discovery exceeds the resolved budget, the implementation MUST choose one of the pre-admitted outcomes:

- fail the current graph identity scope closed;
- quarantine the current graph acquisition/source boundary;
- request caller-owned full rebuild preflight;
- or use a separately admitted continuation scope.

It MUST NOT silently reuse stale graph identity.

It MUST NOT fall back to full rebuild unless the full rebuild memory/time envelope has already passed preflight.

### 17.8. Structural Reference Reuse and Avalanche Containment

Sealed structural references may reduce re-derivation cost by allowing parent/context identity to consume fixed-width
child references instead of full child-local canonical bytes.

This is an avalanche-containment mechanism, not a universal constant-time update guarantee.

Allowed statement:

``````text
parent derivation can be independent of child canonical byte length once the child structural reference is sealed
``````

Forbidden statement:

``````text
arbitrary graph update is O(1)
``````

If a parent has many child references, parent derivation remains proportional to the number of relevant references or to
a separately ratified dependency summary.

A future dependency-summary law may strengthen this bound, but it must be ratified separately.

### 17.9. Graph Dependency Summary Reservation

A graph dependency summary is reserved for future work.

Until ratified, a graph unit MUST NOT use a compact dependency summary as equality authority, invalidation authority, or
complete affected-set proof.

A graph dependency summary MAY be used as a routing or pre-screen hint only if exact dependency verification remains
available.

### 17.10. L2 / Query Integration for Graph Units

L2 may cache graph-derived planning or query results.

L2 MUST NOT decide graph equality.

Graph cache keys MUST include or derive from:

- graph unit identity domain;
- graph unit schema version;
- structural/contextual identity kind;
- active digest suite id/version;
- graph intern scope;
- version-bundle fingerprint;
- dependency identity set fingerprint;
- contextual parent/use-site key where applicable;
- and compatibility classification.

A graph cache hit is valid only if all dependency axes remain unchanged or compatibility-equivalent under the owning
graph unit compatibility matrix.

A graph cache hit MUST NOT be used after:

- parent context changed incompatibly;
- edge role changed incompatibly;
- dependency graph changed incompatibly;
- graph intern scope changed;
- digest/interner protocol changed;
- contract lowering version changed incompatibly;
- or the top-level contract definition changed incompatibly.

### 17.11. Graph and Physical Layer Separation

Graph identity authority belongs to canonical graph material and sealed references.

It does not belong to:

- primitive table slots;
- slab offsets;
- MemorySegment addresses;
- off-heap addresses;
- lane queues;
- owner routing tables;
- cache-line grouping;
- backend object references;
- or runtime graph node objects.

ADR-0042 may optimize graph material storage and transport.

ADR-0042 MUST NOT redefine graph equality.

ADR-0043 may require physical substrate capabilities.

ADR-0043 MUST NOT mandate a specific physical backend such as heap arrays, off-heap memory, native allocation, or
MemorySegment as semantic graph authority.

### 17.12. Graph Extraction Non-Regression Rule

The extracted graph-facing laws from ADR-0041 preserve these invariants:

- parents consume sealed structural references, not arbitrary child object graphs;
- bare HID is not child proof;
- graph interning is protocol-owned and collision-verified;
- structural identity is parent-independent;
- contextual identity is parent/context-dependent;
- graph SCCs seal deterministically;
- SCC-local temporary references do not escape;
- incremental derivation equals from-scratch derivation;
- physical substrate changes do not change graph identity;
- L2 cache hits are not graph equality authority;
- concrete contract fact vocabulary remains reserved until ratified.

## 18. Golden Vectors

A released ADR-0043 implementation MUST provide golden vectors for:

- single graph unit structural identity;
- same unit from different backend acquisition orders;
- repeated child graph unit shared by multiple parents;
- diamond graph with shared sealed child reference;
- parent context changed, child local material unchanged;
- child local material changed;
- contextual identity with different edge role;
- ordered edge list;
- unordered edge list;
- duplicate edge policy;
- bare child HID rejected as sealed reference;
- incompatible version bundle rejection;
- SCC graph seal;
- SCC-local temporary reference rejection outside SCC seal payload;
- graph intern scope mismatch;
- stable graph intern id assignment independent from acquisition order;
- incremental cache hit equivalent to from-scratch result;
- incremental cache invalidation after dependency change;
- DTO boundary graph unit fixture once ratified;
- governance policy graph unit fixture once ratified;
- state-machine transition graph unit fixture once ratified;
- protocol step graph unit fixture once ratified;
- structural/contextual identity stratification fixture;
- graph SCC symmetry rejection fixture;
- graph SCC exact equivalence collapse fixture where ratified;
- graph snapshot-backed materialization fixture;
- graph incremental affected-set budget exhaustion fixture;
- full rebuild preflight required before fallback fixture;
- graph cache hit rejected after incompatible parent/context change fixture.

## 19. Compliance Rules

1. Contract graph units must be ratified before they become identity material.
2. Structural identity is parent-independent.
3. Contextual identity is parent/context-dependent.
4. Parent canonical material should reference sealed child structural references instead of recursively inlining
   arbitrary child bytes.
5. Bare HID is not a sealed structural reference.
6. Sealed structural references require canonical encoding, digest-suite derivation, collision verification, and seal.
7. Graph interning is protocol-owned and collision-verified.
8. Runtime object graphs are not graph identity authority.
9. Backend handles are not graph identity authority.
10. DTO object identity is not graph identity authority.
11. Acyclic graph sharing uses sealed references.
12. Cyclic graph material uses deterministic SCC sealing.
13. Incremental derivation must be equivalent to from-scratch derivation.
14. Incremental reuse requires dependency classification.
15. O (1) claims apply only with respect to sealed child byte length per child reference, not entire subtree size.
16. L2 cache hits are not semantic equality authority.
17. Physical substrate backends do not change graph identity.
18. ADR-0042 owns physical lifecycle and async ownership.
19. Concrete contract fact vocabulary remains reserved until the top-level contract definition is ratified.
20. Lowered contract facts must become ratified graph units before becoming graph identity material.
21. A graph stable intern id is not automatically comparable to a metadata stable intern id.
22. Graph SCC symmetry must not be broken by discovery order, object identity, backend traversal, worker/lane id, or
    callback order.
23. Graph SCC-local temporary references must not escape the SCC seal payload.
24. Graph materialization must not reread mutable backend/source/adapter objects after snapshot-backed measurement has
    accepted graph byte or edge counts.
25. Incremental affected-set discovery must be budgeted.
26. Full rebuild fallback requires preflight before use.
27. Graph dependency summaries are reserved and must not become equality or invalidation authority before ratification.
28. Graph cache keys must include all dependency axes required by the owning graph unit compatibility matrix.
29. Physical substrate mechanics must remain outside graph equality authority.

## 20. Alternatives Considered

### 20.1. Put Full Contract Graph Identity into ADR-0041

Rejected.

ADR-0041 is already responsible for the metadata identity substrate.

Contract graph canonicalization is a separate semantic layer.

### 20.2. Use Recursive Canonical Byte Inlining

Rejected as the ordinary graph identity strategy.

It causes repeated expansion, poor incremental behavior, and diamond graph amplification.

### 20.3. Use Bare HID as Child Proof

Rejected.

A HID is a compact candidate descriptor.

It is not equality authority and not sufficient provenance for graph parent derivation.

### 20.4. Depend on Runtime Graph Object Identity

Rejected.

Runtime graph object identity depends on allocation, backend, object lifetime, and physical execution.

### 20.5. Promise Constant-Time Subtree Updates

Rejected.

Kontrakt may reduce re-derivation from child byte length to fixed-width sealed references.

It cannot promise constant-time updates for arbitrary affected subtrees without a separately ratified dependency summary
law.

### 20.6. Let L2 Cache Decide Graph Equality

Rejected.

L2 may accelerate candidate lookup and reuse.

It cannot become semantic equality authority.

### 20.7. Treat Graph Dependency Summary as Equality Authority

Rejected.

Dependency summaries are useful pre-screen and invalidation accelerators only after ratification.

They cannot replace exact dependency classification and sealed reference verification without a separate law and golden
vectors.

### 20.8. Fall Back to Full Rebuild Without Preflight

Rejected.

Full rebuild is not free.

It may consume more memory and execution budget than the incremental path that failed.

A full rebuild fallback is lawful only after the caller-owned boundary proves that the full rebuild envelope is
available or admits a separate continuation scope.

### 20.9. Let Physical Substrate Define Graph Identity

Rejected.

Primitive tables, slabs, off-heap memory, MemorySegment addresses, lane queues, and cache-line grouping are physical
mechanics.

They may accelerate graph identity processing.

They cannot become graph equality authority.

## 21. Consequences

Positive:

- repeated graph units can be shared through sealed references;
- parent identity no longer needs to recursively inline child bytes;
- diamond graphs avoid accidental tree expansion;
- child-local canonical bytes can be reused safely after seal;
- incremental derivation becomes possible without breaking ADR-0041;
- future contract facts for state, protocol, data, governance, DTO, boundary, and explicit state machines have a graph
  identity substrate;
- ADR-0041 graph-facing laws now have a narrower ADR-0043 maintenance surface;
- graph SCC and incremental affected-set behavior are explicit rather than implicit;
- future top-level contract definition can plug into a prepared graph identity architecture.

Negative:

- implementation must track graph dependency edges explicitly;
- sealed structural references add protocol complexity;
- SCC graph sealing must be implemented carefully;
- from-scratch equivalence tests and golden vectors are mandatory;
- the contract definition must classify graph units and edges precisely before activation;
- graph extraction increases ADR-0043 surface area;
- graph-specific capacity, SCC, and invalidation policies now need explicit tests and golden vectors.

## 22. Final Rule

Kontrakt contract graph identity is not runtime object identity.

A contract graph unit must become canonical material, derive structural identity, pass collision verification, and seal
before it may be referenced by parent or contextual identity.

Parents consume sealed structural references, not arbitrary child object graphs or bare child HIDs.

Incremental derivation may reuse sealed child-local identity material, but it must produce the same result as the
from-scratch deterministic pipeline.

Graph SCCs must seal deterministically, graph affected-set reuse must be budgeted, and L2/query cache reuse must never
become graph equality authority.