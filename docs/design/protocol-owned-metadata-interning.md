# Design Note: Protocol-Owned Metadata Interning

- Status: Draft
- Date: 2026-05-27
- Owner: Kontrakt Compiler Core / Metadata Interning
- Source Decision: ADR-0041
- Extraction Role: Post-acceptance extraction from ADR-0041
- Scope: Metadata interning semantics, collision verification, stable id assignment, and publication
- Non-Goal: Physical table layout, owner-lane transport, backend allocation strategy, contract graph ontology, or L2
  cache governance

---

## 1. Purpose

This design note extracts and maintains the protocol-owned metadata interning law accepted in ADR-0041.

It defines how Kontrakt converts already-ratified metadata identity material into scoped stable intern references
without
allowing runtime object identity, backend handles, digest-only equality, or physical table placement to become semantic
authority.

The interning pipeline is:

``````text
canonical identity bytes
+ metadata identity domain
+ active digest / HID descriptor
+ version bundle
+ verification payload
+ resolved interning scope policy
-> candidate admission
-> collision verification
-> deterministic stable intern id assignment
-> sealed publication
-> scoped stable intern reference
``````

This design note is downstream of:

``````text
docs/design/stable-metadata-identity-protocol.md
``````

and upstream of consumers such as:

- frozen image publication;
- planning provider lookup;
- TypeReference identity lookup;
- contract graph identity once graph units are ratified;
- L2 PlanCacheKey construction;
- reporting/replay identity surfaces where explicitly ratified.

ADR-0041 remains the accepted parent decision.

This design note is the narrower maintenance surface for metadata interning semantics after extraction.

---

## 2. Authority Boundary

### 2.1. This document owns

This document owns the extracted maintenance surface for:

- metadata intern scope law;
- interning candidate law;
- candidate admission law;
- staged-byte admission and failure policy;
- provisional handle law;
- collision verification law;
- bounded cold collision containment law;
- deterministic stable intern id assignment law;
- publication law;
- no partial planning-visible table law;
- stable id independence from physical placement;
- semantic probe/admission budget law;
- quarantine / fail-closed containment semantics;
- interner consumer obligations;
- interning golden-vector obligations.

### 2.2. This document does not own

This document does not own:

- canonical byte encoding;
- domain separation payload layout;
- digest-suite algorithm details;
- HID descriptor byte derivation;
- TypeReference canonical byte law;
- owner-lane routing transport;
- routed candidate batch buffers;
- inbox backpressure;
- self-drain mechanics;
- cache-line grouping;
- small-inline physical layout;
- preclassification chunking;
- slab / arena / epoch reclamation mechanics;
- off-heap / native / `MemorySegment` substrate choice;
- contract graph structural/contextual identity;
- incremental affected-set derivation;
- L2 cache eviction/governance;
- or persistent artifact file format.

Ownership split:

| Surface                                            | Owner                                              |
|----------------------------------------------------|----------------------------------------------------|
| Accepted parent decision                           | ADR-0041                                           |
| Canonical metadata bytes / HID protocol            | `docs/design/stable-metadata-identity-protocol.md` |
| Metadata interning semantics                       | This design note                                   |
| Primitive substrate / routing / physical lifecycle | ADR-0042                                           |
| Contract graph identity / incremental derivation   | ADR-0043                                           |
| Unified runtime memory envelope                    | ADR-0044                                           |
| L2 plan cache governance                           | L2 design notes and ADR-0031/ADR-0032 family       |
| Enforcement hooks                                  | `docs/constitution/compiler-core-protocols.md`     |

### 2.3. Conflict rule

If this design note conflicts with ADR-0041 before extraction is explicitly accepted, ADR-0041 wins.

After this design note is accepted as the narrower maintenance authority, conflict resolution follows the authority
boundary above.

No later document may reinterpret metadata interning to make interned equality depend on:

- runtime object identity;
- backend handles;
- physical address;
- table slot index alone;
- heap allocation order;
- GC behavior;
- thread scheduling;
- owner-lane timing;
- queue order;
- cache hit/miss state;
- or adaptive profiling.

---

## 3. Design Goals

### 3.1. Protocol-owned identity, not object interning

Metadata interning interns canonical identity material.

It does not intern runtime objects.

The interner may return compact stable references.

Those references stand for already-verified canonical identity only within a compatible intern scope.

### 3.2. Deterministic stable id assignment

Stable intern ids must be assigned deterministically from canonical material and scope policy.

They must not depend on:

- physical table slot;
- insertion race winner;
- worker/lane assignment;
- backend traversal order;
- object allocation order;
- hash table iteration order;
- callback completion order;
- or cache warmness.

### 3.3. Digest/HID acceleration without digest-only equality

HID descriptors may accelerate lookup and grouping.

They must never replace exact collision verification.

### 3.4. Publication-before-visibility

No consumer may observe a partial intern table.

No stable intern id may become planning-visible before:

- candidate admission succeeds;
- version compatibility is checked;
- collision verification succeeds;
- stable id assignment completes;
- table coverage is complete;
- integrity validation succeeds;
- and publication boundary safely publishes the table or slice.

### 3.5. Bounded failure and containment

Interner failure must be classified through resolved policy.

It must not degrade into unbounded collision chains, JVM OOM discovery, unbounded diagnostics, or process-wide panic
where a smaller containment boundary is lawful.

---

## 4. Vocabulary

### 4.1. Metadata intern scope

A metadata intern scope is the explicit boundary within which stable metadata intern ids are assigned and comparable.

Examples may include:

``````text
FROZEN_IMAGE_LOCAL
MODULE_METADATA_LOCAL
PLANNING_RUN_LOCAL
CONTRACT_GRAPH_LOCAL where ratified by ADR-0043
PERSISTED_ARTIFACT_LOCAL where a future artifact format ratifies it
``````

A stable id from one scope is not automatically comparable to a stable id from another scope.

### 4.2. Intern candidate

An intern candidate is the pre-publication representation of one canonical identity unit submitted to a metadata
interner.

A candidate includes:

- identity domain id;
- domain schema version;
- canonical encoding version;
- active digest suite id/version;
- HID / descriptor words;
- requested descriptor width;
- version-bundle fingerprint;
- canonical byte length;
- canonical verification payload handle;
- resolved scope id;
- candidate lifecycle state;
- and bounded diagnostics where required.

A candidate is not yet a stable interned identity.

### 4.3. Provisional handle

A provisional handle is a pre-publication local handle used to connect references while a scope is being built.

It is not stable identity.

It is not cross-scope identity.

It is not report/replay identity.

It may be invalidated by rollback before publication.

### 4.4. Stable intern id

A stable intern id is a deterministic scoped id assigned after verification and before sealed publication.

It is only meaningful with its scope, identity domain, interning protocol version, and compatibility proof.

### 4.5. Collision group

A collision group is the set of candidates that share enough compact descriptor material to require exact verification.

A collision group is bounded.

It must not grow without resolved policy caps.

### 4.6. Cold collision structure

A cold collision structure is a bounded overflow structure for rare collision groups that exceed hot collision capacity.

It is the final bounded escalation path.

It is not an unbounded chain.

### 4.7. Publication boundary

A publication boundary is the point at which a complete, verified, immutable intern table or slice becomes visible to
consumers.

Publication is not the same as candidate staging.

### 4.8. Containment boundary

A containment boundary is the smallest resolved scope that can fail closed or be quarantined without corrupting other
scopes.

Examples:

- current candidate;
- speculative planning branch;
- identity domain slice;
- acquisition source boundary;
- metadata intern scope;
- frozen image publication;
- worker lane inside a scope where physically necessary;
- or caller-owned continuation boundary.

---

## 5. Interner Input Law

A metadata interner consumes canonical identity material.

Required input shape:

``````text
canonical identity bytes or sealed exact verification payload
+ identity domain id
+ domain schema version
+ canonical encoding version
+ digest suite id/version
+ HID descriptor
+ requested descriptor width
+ version-bundle fingerprint
+ canonical byte length
+ resolved intern scope
+ resolved interning policy
``````

The interner MUST NOT consume:

- backend handles;
- reflection objects;
- KSP/PSI/compiler descriptor objects;
- annotation descriptor objects;
- DTO runtime objects;
- source AST objects;
- JVM object identity;
- object `hashCode`;
- `toString`;
- display rendering;
- backend serialization;
- mutable object graph references;
- worker/lane id;
- callback completion order;
- or platform collection iteration order.

The interner may consume a sealed reference to canonical bytes or canonical material if the reference is itself
protocol-owned, immutable, and exact-verification capable.

---

## 6. Intern Scope Law

A metadata intern scope MUST be explicit.

A scope MUST define:

- scope id;
- scope kind;
- identity domains admitted;
- interning protocol version;
- stable id assignment policy;
- candidate capacity policy;
- byte capacity policy;
- collision policy;
- publication boundary;
- rollback boundary;
- quarantine boundary;
- diagnostic budget;
- and compatibility with other scopes.

The implementation MUST NOT create an implicit global scope merely because a cache, map, singleton, or service object
exists.

### 6.1. Scope comparability

Stable ids are comparable only when:

- scope kind is compatible;
- scope id or artifact epoch is compatible;
- identity domain id matches;
- interning protocol version matches or has a ratified compatibility bridge;
- and the consumer has validated the scope proof.

A local stable id without scope proof is not global identity.

### 6.2. Scope creation

Scope creation MUST be caller-owned or explicitly admitted by the owning orchestration boundary.

The interner MUST NOT silently open a new semantic scope because a current scope ran out of memory, table capacity,
collision budget, or staging budget.

If a continuation scope is needed, the interner returns a bounded continuation request to the caller-owned boundary.

The caller-owned boundary may open a separately admitted scope only after resolving a new memory envelope, interning
policy, probe budget, staging budget, and publication boundary.

---

## 7. Candidate Lifecycle Law

A candidate must progress through explicit lifecycle states.

Minimum candidate lifecycle:

``````text
DISCOVERED
-> ADMITTED
-> STAGED
-> VERIFIED
-> ASSIGNED
-> PUBLISHED

Failure states:
REJECTED
ROLLED_BACK
FAILED_CLOSED
QUARANTINED
``````

A candidate MUST NOT skip from `DISCOVERED` to `PUBLISHED`.

A candidate MUST NOT receive a stable intern id before verification succeeds.

A candidate MUST NOT become consumer-visible before publication.

### 7.1. Provisional handle boundary

A provisional handle may be issued only after candidate admission has established that the candidate can be tracked
within
resolved scope budgets.

A provisional handle MUST NOT be issued if staged-byte admission, candidate-count admission, domain admission, or
diagnostic-budget admission has already failed.

A provisional handle MUST carry enough local metadata to be invalidated or rolled back without becoming stable identity.

### 7.2. Provisional handle use

A provisional handle MAY be used for:

- local graph/reference construction inside the same admitted scope;
- SCC staging;
- candidate dependency tracking;
- rollback bookkeeping;
- and deterministic diagnostics.

It MUST NOT be used as:

- stable intern id;
- semantic equality authority;
- PlanCacheKey material;
- frozen-image material;
- report/replay identity;
- persistent artifact identity;
- cross-scope identity;
- or query reuse key.

### 7.3. Provisional handle rollback

If candidate admission, collision verification, staging, or scope publication fails before publication, all provisional
handles inside the affected rollback boundary MUST become unreachable from consumer-visible state.

Rollback does not require physical zeroing unless ADR-0042 requires it for a specific substrate.

Rollback does require semantic zero-residue:

``````text
no consumer-visible path may retain the provisional handle as valid identity
``````

---

## 8. Candidate Admission Law

Interning is not free.

Candidate admission must prove that the candidate can be processed within resolved policy before it receives a
provisional handle or stable id.

Admission MUST check at least:

- identity domain is admitted by the scope;
- schema/digest/interner versions are compatible;
- candidate count capacity is feasible;
- staged-byte capacity is feasible;
- canonical byte length is within domain cap;
- verification payload is available;
- collision group budget is feasible;
- diagnostics budget is feasible where failure reporting is required;
- stable id range is feasible;
- publication boundary is open;
- caller-owned scope transition is lawful.

If any required admission check fails, the candidate MUST NOT be issued a provisional handle unless the failure policy
explicitly classifies a pre-screen-only path that does not create identity-visible handles.

### 8.1. Candidate count admission

A candidate insert MUST be rejected before publication if it would exceed the resolved count cap for its identity domain
or scope.

For load-factor feasibility, implementations MUST use checked integer arithmetic and division-free cross-multiplication
where the relationship is tested repeatedly.

Required shape:

``````text
prospectiveCandidateCount * maxLoadFactorDenominator
    <= logicalTableCapacitySlots * maxLoadFactorNumerator
``````

Forbidden hot-path shape:

``````text
prospectiveCandidateCount
    <= floor(logicalTableCapacitySlots * maxLoadFactorNumerator / maxLoadFactorDenominator)
``````

The forbidden form is not acceptable because it invites division, floor semantics, and overflow-prone multiplication
inside repeated admission logic.

All products MUST be computed with checked arithmetic in a width that can represent the maximum configured values.

### 8.2. Candidate count discovery timing

A profile MUST declare how candidate count is known.

Allowed candidate-count modes:

``````text
AOT_EXACT
    the candidate count is known before scope admission.

PREPASS_EXACT
    a deterministic prepass counts candidates before table construction.

BOUNDED_STREAMING
    candidates are discovered during construction under strict count/staging/probe budgets.

INCREMENTAL_AFFECTED_SET
    only affected/dirty candidates are admitted under an incremental profile.
``````

A mode MUST be selected before scope admission.

It MUST NOT change after the implementation observes that the current scope is larger than expected.

### 8.3. BOUNDED_STREAMING admission

In `BOUNDED_STREAMING`, candidate count is not known upfront.

Therefore the resolved profile MUST define:

- candidate count cap;
- staged canonical byte cap;
- staged metadata byte cap;
- provisional handle cap;
- domain-slice cap;
- route/staging interaction boundary if ADR-0042 owner-lane routing is used;
- and failure outcome when the cap is reached.

Streaming admission MUST fail before provisional handle issuance when the next candidate cannot fit.

The implementation MUST NOT discover streaming overrun through OOM.

### 8.4. INCREMENTAL_AFFECTED_SET admission

In `INCREMENTAL_AFFECTED_SET`, the profile must separately budget:

- dirty candidate count;
- reused sealed reference count;
- invalidation traversal edges;
- visited units;
- frontier units;
- scratch bytes;
- and full-rebuild fallback preflight where applicable.

A small dirty set does not imply small traversal cost.

If traversal exceeds the resolved budget, the implementation MUST follow the pre-admitted invalidation failure policy.

It MUST NOT silently reuse stale identity material.

---

## 9. Staged Byte Admission and Failure Policy

Staged bytes include any pre-publication material required to verify, assign, or publish candidates.

Examples:

- canonical byte slices;
- canonical verification payload references;
- staged metadata rows;
- provisional handle tables;
- collision verification records;
- diagnostics;
- scope-local mapping material;
- SCC staging material where applicable;
- and pre-publication index material.

Staged byte admission MUST occur before identity-visible provisional handle issuance.

The implementation MUST NOT discover staged-memory exhaustion through `OutOfMemoryError` after admitting the provisional
handle.

### 9.1. Resolved staged-byte-failure policy

If staged byte admission fails, the implementation MUST NOT choose an outcome opportunistically.

The outcome MUST be selected by a resolved staged-byte-failure policy declared before scope admission.

The policy MUST classify the failure context as one of:

``````text
DUPLICATE_PRESCREEN_ELIGIBLE
SPECULATIVE_BRANCH_LOCAL
REQUIRED_PUBLICATION_MATERIAL
SCOPE_RESOURCE_PRESSURE
REPEATED_OR_PATHOLOGICAL_PRESSURE
CALLER_OWNED_CONTINUATION_REQUIRED
``````

The default v1 mapping is:

``````text
DUPLICATE_PRESCREEN_ELIGIBLE
    -> do not enter full canonical staging;
       enter the bounded duplicate pre-screen path before provisional handle issuance.

SPECULATIVE_BRANCH_LOCAL
    -> reject the current candidate or roll back the current planning branch
       before provisional handle publication.

REQUIRED_PUBLICATION_MATERIAL
    -> fail the current identity scope closed.

SCOPE_RESOURCE_PRESSURE
    -> fail the current identity scope closed.

REPEATED_OR_PATHOLOGICAL_PRESSURE
    -> quarantine the current acquisition, planning, source, or incremental scope
       with bounded diagnostics.

CALLER_OWNED_CONTINUATION_REQUIRED
    -> return a bounded continuation request to the caller-owned orchestration boundary.
       The interner MUST NOT open a new scope by itself.
``````

A separately admitted scope may be opened only by the caller-owned orchestration boundary.

It requires a new explicit scope admission, new memory envelope, new probe budget, new staging budget, and new
publication boundary.

The implementation MUST NOT silently expand the current semantic scope.

### 9.2. Duplicate pre-screen boundary

A duplicate pre-screen may reduce staging pressure.

It is not equality authority.

It may use compact descriptors, local fingerprints, or bounded recent-seen structures only if exact verification remains
available before publication.

If a pre-screen rejects a candidate as duplicate, the rejection must be validated by the owner scope before any stable
id
is published.

Producer-local or backend-local suppression structures are physical accelerators owned by ADR-0042 and must be budgeted
there.

---

## 10. Collision Verification Law

Digest descriptors can collide.

Every equality-sensitive intern operation MUST include collision verification.

Required verification order at the semantic level:

``````text
identity domain check
-> schema/version compatibility check
-> digest suite / width compatibility check
-> HID / descriptor comparison
-> version-bundle fingerprint comparison
-> canonical byte length comparison
-> exact verification payload comparison
-> equality accepted or distinct candidate retained
``````

Physical verification ladder ordering and hot metadata layout belong to ADR-0042.

This document owns the semantic requirement that exact verification is mandatory.

### 10.1. Exact verification payload

The exact verification payload may be:

- canonical bytes;
- immutable canonical material reference;
- sealed canonical byte slice;
- collision verification record;
- stable intern reference with compatible scope proof;
- or another ratified exact payload.

It MUST NOT be:

- display rendering;
- diagnostic string;
- `toString`;
- object identity;
- backend handle;
- reflection/KSP/PSI object;
- mutable adapter object;
- source AST object;
- or backend serialized form.

### 10.2. Collision result

If two candidates have matching compact descriptors but different exact verification payloads, they are distinct
candidates.

The interner MUST NOT merge them.

The interner MUST retain both only if the collision group remains within resolved bounds.

If the resolved bounds are exceeded, the collision overflow policy applies.

### 10.3. Version mismatch

A version mismatch is not a successful collision verification.

It must be classified as:

- incompatible candidate;
- cache miss / non-match;
- protocol violation;
- scope rejection;
- or fail-closed publication error

according to resolved policy.

Version mismatch handling MUST be zero-allocation or bounded-allocation on hot paths where ADR-0042 requires it.

It MUST NOT create unbounded error logs or exception storms.

---

## 11. Bounded Cold Collision Containment Law

Hot collision structures are bounded.

If a hot collision group exceeds its resolved hot capacity, the implementation MAY enter a bounded cold collision
structure only if the resolved policy admits it.

A cold collision structure MUST define:

- maximum cold group member count;
- maximum cold canonical byte total;
- maximum cold verification payload bytes;
- maximum cold probe/comparison steps;
- maximum cold diagnostic bytes;
- containment boundary;
- failure outcome;
- and publication behavior.

Cold collision structure is the last bounded escalation.

If the bounded cold collision structure exceeds its resolved member, byte, probe, comparison, or diagnostic budget, the
implementation MUST terminate collision escalation for the current containment boundary.

It MUST fail closed or quarantine according to the resolved collision-overflow policy.

It MUST NOT:

- allocate another colder collision structure;
- extend the cold structure unboundedly;
- accept digest-only equality;
- select a new digest suite/seed/width after visibility;
- change stable intern id ordering;
- or keep scanning indefinitely.

### 11.1. Collision overflow containment

Allowed containment outcomes:

``````text
FAIL_CURRENT_CANDIDATE_BEFORE_HANDLE
FAIL_CURRENT_IDENTITY_UNIT_CLOSED
FAIL_CURRENT_IDENTITY_SCOPE_CLOSED
FAIL_FROZEN_IMAGE_PUBLICATION_CLOSED
QUARANTINE_SOURCE_OR_ACQUISITION_BOUNDARY
QUARANTINE_DOMAIN_SLICE
QUARANTINE_SCOPE
RETURN_CALLER_OWNED_CONTINUATION_REQUEST
``````

The containment boundary must be resolved before scope publication.

The implementation SHOULD prefer source/domain/scope containment before physical worker-lane quarantine when collision
pressure is attributable to logical input material.

Physical lane quarantine is a last resort for lane-local state contamination or physical lifecycle failure.

### 11.2. Runtime rekeying prohibition

Collision overflow MUST NOT trigger runtime rekeying after scope visibility.

The implementation MUST NOT change:

- hash seed;
- digest suite;
- HID width;
- route map;
- stable id order;
- version bundle fingerprint;
- or canonical encoding policy

as a reaction to observed collision pressure inside a visible scope.

A stronger-width path is lawful only if selected before scope admission by resolved policy and golden-vector covered.

---

## 12. Stable Intern Id Assignment Law

Stable intern ids are assigned deterministically after verification.

A stable intern id assignment function MUST depend only on ratified canonical ordering material and resolved scope
policy.

Allowed assignment inputs:

- identity domain id;
- domain schema version;
- canonical bytes or exact canonical sort key;
- version-bundle fingerprint;
- digest suite descriptor where ratified;
- verified collision group ordering;
- intern scope id;
- stable id assignment policy version.

Forbidden assignment inputs:

- physical table slot;
- hash bucket index;
- probe sequence outcome;
- insertion order;
- worker/lane id;
- CAS winner order;
- callback order;
- backend traversal order;
- object address;
- heap allocation order;
- map iteration order;
- source traversal order unless ratified;
- route queue order;
- cache hit/miss state;
- or collision overflow timing.

### 12.1. Stable id ordering

Within one scope, assignment must be stable across repeated runs with the same canonical input and resolved policy.

If the implementation assigns dense ids, the dense ordering must be derived from deterministic canonical order.

If the implementation assigns sparse ids, the sparse mapping must still be deterministic and stable within the scope.

### 12.2. Physical table independence

Stable id assignment MUST NOT change when the physical table:

- resizes lawfully;
- rebuilds lawfully;
- reindexes lawfully;
- changes probe strategy lawfully;
- changes segmented/page layout;
- changes owner-lane routing profile;
- changes slab placement;
- moves from heap arrays to off-heap substrate;
- or changes backend allocation strategy.

Physical placement belongs to ADR-0042.

Stable intern id assignment belongs to this protocol.

### 12.3. Stable id range and sentinel law

A scope MUST define stable id width, sentinel values, and maximum id count.

Reserved sentinel values MUST NOT collide with valid ids.

If the valid id range is exhausted, assignment MUST fail closed before publication.

The implementation MUST NOT silently wrap, truncate, or reuse ids.

---

## 13. Publication Law

An intern table, domain slice, or scope-local intern image becomes visible only after all required publication
conditions
are satisfied.

At minimum:

- all candidates are admitted;
- all candidates are encoded or exact verification payloads are sealed;
- all candidates are version-compatible or rejected;
- all collisions are verified or contained;
- all stable ids are assigned deterministically;
- all provisional handles are resolved or rolled back;
- table/slice coverage is complete;
- integrity validation succeeds;
- backend-native handles are unreachable from consumer-visible identity;
- publication uses a safe publication boundary.

Partial intern tables MUST NOT become planning-visible.

Partial domain slices MUST NOT become frozen-visible unless the owning scope explicitly defines independent publication
slices and all cross-slice references are sealed and verified.

### 13.1. Publication-before-completion

If interning participates in asynchronous or joined pipelines, completion signals may be delivered only after the
publication state they report has become authoritative.

A waiter, callback, continuation, or L2 consumer MUST NOT observe a stable intern id before the intern table/slice
publication boundary has linearized.

Physical delivery mechanics belong to ADR-0042 and L2 design notes.

### 13.2. Safe publication material

A safe publication boundary MUST ensure that consumers observe:

- immutable canonical verification material;
- final stable id mapping;
- final scope/domain/protocol metadata;
- final collision verification state;
- final compatibility classification;
- final table/slice integrity metadata;
- no mutable staging handles;
- and no backend-native handles as authority.

### 13.3. Publication failure

If publication fails after candidates have been staged but before visibility, the scope must fail closed or roll back
according to resolved policy.

No consumer-visible stable id may escape.

Diagnostics must remain bounded.

---

## 14. No Partial Table Visibility Law

No consumer may observe an intern table in a partially built state.

Forbidden:

``````text
candidate A published
candidate B still verifying
candidate C still has provisional handle
consumer reads table as complete
``````

Required:

``````text
all visible entries belong to a sealed publication slice
or no entries are visible
``````

If a profile supports independently published slices, each slice must be a complete publication unit with its own:

- coverage proof;
- compatibility proof;
- collision verification state;
- stable id range;
- cross-slice reference policy;
- and integrity check.

---

## 15. Interner Probe and Admission Budget Law

Probe/admission budgets are semantic safety surfaces even when physical probing is implemented by ADR-0042.

A metadata interner policy MUST define bounded probe/admission behavior.

At minimum:

- maximum admitted candidate count;
- maximum logical table capacity;
- maximum candidate count by identity domain;
- maximum collision group size;
- maximum exact verification bytes per collision group;
- maximum staged canonical bytes;
- maximum staged metadata bytes;
- maximum provisional handles;
- maximum diagnostics bytes;
- maximum stable id count;
- maximum rebuild/reindex events if any;
- and failure outcomes.

### 15.1. Feasibility before publication

Candidate and table feasibility MUST be proven before publication.

If feasibility cannot be proven, publication fails closed.

The implementation MUST NOT depend on physical table success as the first proof of feasibility.

### 15.2. Load factor law

Load factor feasibility MUST be checked using resolved integer policy.

Required relationship:

``````text
candidateCountForDomain * maxLoadFactorDenominator
    <= logicalTableCapacitySlotsForDomain * maxLoadFactorNumerator
``````

All terms are non-negative resolved integers.

All arithmetic is checked.

`maxLoadFactorNumerator` and `maxLoadFactorDenominator` MUST satisfy:

``````text
0 < maxLoadFactorNumerator
0 < maxLoadFactorDenominator
maxLoadFactorNumerator <= maxLoadFactorDenominator
``````

The relationship MUST be validated before any repeated admission logic that depends on it.

### 15.3. Probe exhaustion

If probe/admission limits are exhausted, the implementation must classify the outcome through resolved policy.

Allowed high-level outcomes:

- reject the candidate before provisional handle issuance;
- fail the identity domain slice closed;
- fail the scope closed;
- quarantine source/acquisition boundary;
- use a caller-owned continuation scope;
- or use an ADR-0042 physical fallback that preserves stable id and equality semantics.

Probe exhaustion MUST NOT produce partial publication.

---

## 16. Streaming and Lane Interaction Boundary

This document owns the semantic admission requirements for streaming interning.

ADR-0042 owns physical owner-lane routing, route buffers, inbox headroom, self-drain, cooperative drain limits, and
producer-local suppression memory.

The boundary is:

``````text
This document:
    what must be admitted, verified, assigned, and published

ADR-0042:
    how physical lanes, queues, slabs, buffers, and backend substrates move that material
``````

If ADR-0042 physical transport fails, this document's publication and containment laws still apply.

A transport failure must not become semantic equality.

A routing failure must not publish partial identity.

---

## 17. TypeReference Interning Boundary

TypeReference identity is defined by `docs/design/stable-metadata-identity-protocol.md`.

TypeReference stable intern ids are assigned by this interning protocol only after:

- final TypeReference canonical material exists;
- TypeCycleKey has been resolved after SCC seal;
- HID/domain/version/suite checks pass;
- exact TypeReference verification payload exists;
- collision verification succeeds;
- stable id assignment order is determined;
- and publication boundary succeeds.

Pre-SCC TypeReference pre-identity MUST NOT be interned as final stable identity.

Unresolved-cycle sentinel material MUST NOT become stable intern identity.

TypeReference hot carrier packing belongs to ADR-0042.

The semantic rule preserved here is:

``````text
a TypeReference stable intern id is valid only inside a compatible intern scope and only after final TypeReference
canonical verification.
``````

---

## 18. Frozen Image Consumer Boundary

A frozen image may consume metadata stable intern ids only after the intern scope or slice is published.

Frozen image material MUST NOT include:

- provisional handles;
- pre-SCC TypeReference identities;
- unverified HID descriptors;
- backend handles;
- mutable staging pointers;
- or local route/probe state.

If frozen image publication depends on metadata interning, frozen image publication must fail closed when required
interning publication fails.

---

## 19. Planning Consumer Boundary

Planning may consume stable intern references only after publication.

Planning MUST NOT use:

- provisional handles;
- pre-publication candidate indexes;
- HID-only equality;
- route-only descriptors;
- backend handles;
- physical table slots;
- or mutable staging material

as semantic planning identity.

Planning may use stable intern ids as compact references only with compatible scope/domain/protocol proof.

Planning cache keys must still include or verify their full semantic key axes.

---

## 20. L2 Consumer Boundary

L2 may use metadata intern ids, HIDs, and route descriptors as acceleration material.

L2 MUST NOT treat them as complete PlanCacheKey equality unless full PlanCacheKey verification succeeds.

L2 cache hit/miss, join timing, shard routing, dispatch lane routing, partition drop, or circuit-open state MUST NOT
change metadata identity or stable intern id assignment.

L2 governance belongs to L2 design notes and ADR-0031/ADR-0032 family.

This document only guarantees that stable metadata references consumed by L2 are scoped, verified, and published.

---

## 21. Persistent Artifact Boundary

A stable metadata intern id is not persistent artifact identity by itself.

A future persistent artifact format may persist intern tables only if it defines:

- artifact format version;
- interning protocol version;
- scope encoding;
- stable id encoding;
- canonical verification payload encoding;
- collision verification state encoding;
- compatibility law;
- migration law;
- compression policy if any;
- and golden vectors.

Until then, stable intern ids are scoped runtime/frozen/protocol references, not public wire-format ids.

---

## 22. Security and DoS Boundary

The metadata interner must defend against:

- collision amplification;
- oversized collision groups;
- staged byte exhaustion;
- provisional handle leaks;
- scope over-admission;
- stable id range exhaustion;
- pathological domain skew;
- repeated source-local collision pressure;
- unbounded diagnostics;
- partial publication;
- stale pre-SCC TypeReference identity;
- and runtime rekeying.

Required responses include:

- bounded admission;
- exact collision verification;
- resolved failure policy;
- source/domain/scope containment;
- no digest-only equality;
- no unbounded cold collision chain;
- no stable id assignment before verification;
- no partial publication;
- and no post-visibility suite/width/seed changes.

---

## 23. Golden Vectors

A released implementation of this design MUST provide golden vectors or deterministic fixtures for at least:

- candidate admission success;
- candidate count cap rejection;
- staged byte cap rejection before provisional handle;
- duplicate pre-screen eligible path;
- speculative branch rollback before publication;
- required publication material fail-closed;
- caller-owned continuation request;
- provisional handle issued only after admission;
- provisional handle rollback;
- pre-SCC TypeReference rejected as final stable identity;
- final TypeReference accepted after TypeCycleKey seal;
- HID match plus exact canonical match;
- HID match plus canonical mismatch retained as distinct collision;
- hot collision group within cap;
- hot collision group escalates to bounded cold collision structure;
- cold collision bound exceeded fail-closed;
- runtime rekeying rejected after visibility;
- stable id assignment independent of candidate discovery order;
- stable id assignment independent of physical table slot;
- stable id assignment independent of backend traversal order;
- load factor cross-multiplication boundary;
- stable id range exhaustion fail-closed;
- publication succeeds only after all candidates verified;
- partial table visibility rejected;
- independently published slice coverage proof where supported;
- frozen image rejects provisional handles;
- planning rejects HID-only equality;
- L2 PlanCacheKey exact verification required;
- bounded diagnostic output for collision overflow.

Golden vectors MUST include enough canonical identity and scope material to reproduce the expected stable id assignment.

---

## 24. Architecture Tests

A compliant implementation SHOULD provide architecture tests that assert:

1. no backend handles are accepted as interning candidates;
2. no runtime object identity participates in stable id assignment;
3. HID-only equality is impossible;
4. stable id assignment is independent from physical table slot;
5. stable id assignment is independent from worker/lane scheduling;
6. stable id assignment is independent from backend traversal order;
7. provisional handles cannot cross publication boundaries;
8. partial intern tables cannot become planning-visible;
9. staged byte failure cannot be discovered through OOM after handle issuance;
10. collision overflow cannot allocate unbounded chains;
11. runtime rekeying after scope visibility is impossible;
12. frozen image cannot consume unresolved pre-SCC TypeReference identity;
13. L2 cannot use stable intern id alone as full PlanCacheKey equality;
14. all failure paths produce bounded diagnostics;
15. caller-owned continuation scope cannot be opened by the interner itself.

---

## 25. Compliance Rules

1. Metadata interning consumes canonical identity material, not runtime objects.
2. Intern scopes must be explicit.
3. Stable ids are scoped and not globally comparable by default.
4. Candidate admission must precede provisional handle issuance.
5. Provisional handles are not stable identity.
6. Staged byte failure outcomes are resolved before scope admission.
7. Interner must not open new semantic scopes by itself.
8. HID match never implies equality without exact verification.
9. Collision groups are bounded.
10. Cold collision structure is the final bounded escalation path.
11. Cold overflow fails closed or quarantines under resolved policy.
12. Runtime rekeying after visibility is forbidden.
13. Stable id assignment must be deterministic.
14. Stable id assignment must not depend on physical table placement.
15. Stable id range exhaustion fails closed.
16. Publication requires all admitted candidates to be verified or rejected.
17. Partial intern tables must not become planning-visible.
18. Frozen image must not consume provisional handles.
19. Planning must not consume HID-only identity as semantic identity.
20. L2 must perform full PlanCacheKey verification.
21. Physical transport belongs to ADR-0042.
22. Canonical byte protocol belongs to stable metadata identity protocol design note.
23. Contract graph identity belongs to ADR-0043.
24. Golden vectors are mandatory for released behavior.

---

## 26. Alternatives Considered

### 26.1. Keep interning detail only in ADR-0041

Rejected.

ADR-0041 remains accepted, but metadata interning requires a narrower maintenance surface than the full stable metadata
identity monolith.

### 26.2. Treat interning as ordinary object interning

Rejected.

Object interning depends on object lifetime, allocation behavior, backend handles, and JVM identity.

Kontrakt interns protocol-owned canonical identity material.

### 26.3. Treat HID as the intern key authority

Rejected.

A HID is a compact descriptor and can collide.

Exact verification is required.

### 26.4. Allow stable ids to follow table insertion order

Rejected.

Insertion order depends on worker scheduling, backend traversal, queue timing, and physical table behavior.

Stable ids must follow deterministic canonical order.

### 26.5. Let the interner open a new scope when memory is exhausted

Rejected.

Scope transitions are semantic/resource-boundary transitions and must be caller-owned.

The interner may return a continuation request but must not silently expand scope.

### 26.6. Allow unbounded cold collision chains

Rejected.

Cold collision structure is the final bounded escalation path.

Cold overflow must fail closed or quarantine under resolved policy.

### 26.7. Let physical owner-lane routing define interning semantics

Rejected.

Owner-lane routing is a physical substrate/transport strategy.

It may accelerate interning but cannot define equality, stable id assignment, or publication authority.

---

## 27. Consequences

Positive:

- metadata interning has a narrower maintenance surface;
- stable id assignment is separated from physical table mechanics;
- digest/HID acceleration is retained without equality misuse;
- collision containment is explicit;
- staged-byte failure behavior is deterministic;
- provisional handles are contained;
- publication-before-visibility is explicit;
- consumers have clear boundaries.

Negative:

- stable id assignment requires deterministic canonical ordering;
- exact verification payloads must remain available until publication;
- large scopes require careful admission/preflight;
- collision overflow behavior needs fixtures;
- physical implementation must coordinate with ADR-0042 without leaking physical placement into semantics.

---

## 28. Final Rule

Protocol-owned metadata interning interns canonical identity material.

It does not intern runtime objects.

A stable intern id is valid only after candidate admission, exact collision verification, deterministic assignment, and
safe publication inside an explicit compatible scope.

HID descriptors, provisional handles, routing keys, physical table slots, and backend handles are not semantic equality
authority.