# ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Publication, Lifecycle, and Reuse

## Status

Proposed

## Date

2026-09-12

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/adr/current/0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md`
-
`docs/adr/current/0047-one-dimensional-contract-presentations-pipeline-slot-selection-and-backend-realization-boundary.md`
- `docs/adr/current/0053-version-contract-sovereign-meaning-identity-version-claims-and-authority-boundary.md`
- `docs/adr/current/0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md`
- `docs/adr/current/0064-input-contract-explicit-boundary-presentation.md`
- `docs/adr/current/0065-admission-contract-continuation-judgment.md`
- `docs/adr/current/0066-canonicalization-contract-stable-representative-and-canonical-bytes.md`
- `docs/adr/current/0067-lowering-contract-explicit-relation-compiler-derived-realization-and-core-entry.md`
- `docs/adr/current/0068-fact-contract-explicit-immutable-core-information-sameness-and-uniqueness.md`
- `docs/adr/current/0069-invariant-contract-fact-local-standing-integrity-law.md`
- `docs/design/kontrakt-compiler-total-architecture-map-design-draft.md`
- `docs/design/kontrakt-compiler-material-and-ir-architecture-review-checklist.md`
- `docs/todo/kontrakt-established-contract-world-architecture-todo.md`
- `docs/todo/kontrakt_IR_subsystem_contract_implementation_separation_discussion.md`
- `docs/todo/Kontrakt_Query_Oriented_Compiler_and_Object_Free_Core_Design.md`
- `docs/todo/kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/v2/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- `docs/todo/v2/kontrakt-v2-incremental-architecture-research-todo.md`
- *Modern Compiler Architecture 01-15*

---

## 1. Context

Kontrakt has more than one authoring form and more than one later compiler consumer.

`.kontrakt` source and selected immutable 1D carrier source begin as frontend material. They still contain source
spelling,
syntax structure, host-carrier structure, and unresolved references that later compiler work must not reinterpret for
itself.

At the other side of the frontend, ADR-0063 already defines a different boundary.

```text
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Established Definition Material
    ↓
Canonical Contract World
```

The missing architecture law is the left side of that boundary.

Kontrakt needs one resolved compiler-semantic representation that removes frontend ambiguity without claiming Contract
authority. Establishment must receive enough exact candidate meaning to apply the owning Contract law without reopening
source syntax, rediscovering host structure, or repeating lexical lookup.

That representation also becomes an important compiler product. Query orchestration, diagnostics, tooling, reuse, and
future incremental work may consume its resolved meaning. Those consumers must not force query state, cache state,
manager topology, or incremental metadata into HIR semantics.

This ADR defines the semantic and publication contract of **Resolved Contract HIR**.

It does not define a Kotlin class hierarchy, table schema, query engine, persistent format, or incremental algorithm.

---

## 2. Problem

A frontend can fail in two opposite directions.

The first failure is to stop too early. Parsed or partially resolved material is handed downstream and every consumer
finishes the interpretation for itself. Establishment repeats name lookup. Diagnostics infer ownership from syntax.
Tooling and generated products learn different meanings from the same source. Incremental reuse then tracks whichever
physical path happened to perform the missing work.

The second failure is to make HIR absorb the whole compiler. Analysis results, query edges, cache fingerprints,
diagnostics, lifecycle flags, backend coordinates, and optimizer knowledge are attached to one representation because a
consumer may need them later. HIR then becomes a mutable universal object whose physical layout controls invalidation,
lifetime, and authority.

Both designs destroy the boundary that Kontrakt needs.

Resolved Contract HIR must be semantically complete for its role, but that role ends before Contract authority.

It must also remain usable by a production compiler. A whole-generation product alone is too coarse for precise reuse,
while field-level dependency tracking would make physical storage part of incremental architecture. Published HIR
therefore needs stable semantic units and coherent generations without committing Kontrakt to one query or database
system.

Finally, incremental and parallel execution cannot weaken determinism. A warm cache, a different worker schedule, or an
incremental repair path must not change the resolved meaning presented to Establishment.

---

## 3. Decision Drivers

Contract meaning remains prior to compiler realization.

Resolved HIR must preserve every source-owned distinction that Establishment still needs. It must erase source-only
differences once those differences no longer affect resolved candidate meaning.

A downstream consumer must not reopen source or inspect host-carrier topology to recover meaning that the frontend has
already resolved.

Published HIR must be safe for independent consumers. Construction may be mutable, but a consumer must not observe a
half-resolved generation.

The architecture must support fine-grained products without turning table rows, object fields, or query nodes into
semantic identity.

V1 must support query-oriented reuse. V2 must remain free to add persistent dependency state, cross-session reuse, and
domain-specific incremental repair without rewriting HIR semantics.

Correctness and accepted Contract law are constraints, not engineering trade-offs. After those constraints are
satisfied, determinism is the first trade-off criterion. A faster or more incremental realization is rejected when it
cannot preserve the same observable HIR result as clean deterministic computation.

---

# 4. Decision

## 4.1. Resolved Contract HIR Role

Resolved Contract HIR is the high-level compiler-semantic representation produced after frontend resolution and before
Contract Establishment.

```text
Authoring Material
    ↓
parse / acquire
    ↓
resolution / semantic formation
    ↓
Resolved Contract HIR
    ↓
Authority-Owned Establishment
```

HIR owns **resolved candidate meaning**.

It does not own Contract authority.

Resolution success means that the compiler can state the candidate exactly in compiler-semantic form. It does not mean
that the owning Contract law has accepted that candidate.

HIR is a real IR level because its vocabulary, invariant, and information-loss boundary differ from source and syntax
material. It is not a new level merely because the compiler freezes or caches it.

---

## 4.2. Determinism-First Law

For the same explicit valid frontend inputs, Kontrakt must produce the same observable Resolved HIR meaning.

Worker order cannot select a meaning. Cache state cannot select a meaning. Filesystem enumeration order cannot select a
meaning. Allocation order, hash-table iteration, query scheduling, and incremental repair order cannot select a meaning.

If an ordering is semantically observable, the owning law must provide the ordering or the compiler must use a
canonical deterministic rule that does not invent Contract meaning.

Physical layout may differ when that difference is outside the HIR observable surface.

The same rule applies to optimization of the compiler itself. Kontrakt may prefer lower compile time, lower memory use,
more cache hits, more parallelism, or more incremental reuse only after deterministic equivalence is preserved.

A clean computation with reuse disabled remains the reference path for HIR semantic correctness.

---

## 4.3. HIR Admission Invariant

Material is visible as Resolved Contract HIR only after the frontend can interpret its semantic references exactly.

The required resolution boundary includes the source-language ambiguity that would otherwise force later semantic work
to search or guess. Names required by the candidate are no longer unresolved spellings. The Contract role of the
candidate is known. Module or import qualification needed for exact interpretation has been resolved. A reference that
remains ambiguous prevents that semantic unit from satisfying the HIR invariant.

The exact reference form is a compiler concern. The invariant is semantic: later consumers receive one exact resolved
target rather than a lexical search problem.

HIR admission does not require Establishment success.

A candidate may be fully resolved and still be rejected by its owning Contract law.

---

## 4.4. Resolved Semantic Surface

HIR preserves the complete resolved candidate meaning owned by the relevant frontend and 1D Contract vocabulary.

The owning 1D ADR decides which distinctions are Contract distinctions. HIR does not redefine Input, Admission,
Canonicalization, Lowering, Fact, Invariant, State, Version, Policy, Governance, Failure, Publication, Output, or
another
Contract authority into one generic node schema.

HIR instead preserves those distinctions in a resolved compiler form.

When one 1D Definition declares semantic coordinates, explicit alternatives, required absence, Required Basis meaning,
or another distinction that Establishment must inspect, HIR must retain it exactly enough for Establishment to decide
without returning to authored syntax.

A downstream convenience field is not added merely because a later subsystem could use it.

---

## 4.5. Exact References Before Authority

HIR references are exact compiler-semantic references to resolved candidate targets.

They are not automatically the authoritative `Definition Reference` defined by ADR-0063.

```text
authored symbol
    ↓ resolution
exact HIR semantic target
    ↓ owning Establishment / Composition law
possible authoritative relation
```

This distinction matters when two definitions in the same frontend generation refer to one another before either has
received authority.

The compiler may know exactly which candidate is referenced. That exactness does not establish the target or the
relation.

If later Contract law establishes an exact `Definition Reference`, `Version Binding`, `Basis Binding`, or another
authority-bearing relation, that meaning is created at the boundary that owns it. HIR must not pre-establish it for
convenience.

---

## 4.6. Version and Basis Boundary

A Version Claim that participates in candidate meaning must be resolved far enough that Establishment is not left with
lexical ambiguity.

HIR therefore preserves the exact authority-scoped version target required to judge the candidate. This is resolved
compiler meaning. It does not make compiler generation, artifact revision, or product schema version into Contract
Version.

ADR-0063 remains the owner of authoritative `Version Binding` meaning.

Required Basis follows the same separation.

When a Definition judgment owns a Required Basis, HIR preserves the requirement as part of the resolved candidate. The
actual source that satisfies that requirement is not guessed from HIR topology. `Basis Resolution` and `Basis Binding`
remain owned by the applicable composition law over Established Material.

Occurrence-only or higher-scope Required Basis does not become Definition HIR meaning merely because later execution
will need it.

---

## 4.7. Applicability Boundary

HIR may preserve a resolved applicability declaration when that declaration is part of the candidate Contract meaning.
It may also preserve the exact semantic coordinates needed to identify the declared applicability law.

HIR does not contain an `Applicable` result for a future semantic use.

Applicability in ADR-0063 is a judgment over an exact binding, an exact dependent application, and the relevant semantic
context. Those inputs do not exist merely because a definition candidate has been resolved.

The compiler must therefore keep applicability declaration from applicability result.

## 4.8. HIR Verification Contract

Every published Resolved HIR generation must satisfy the HIR invariant before an independent consumer can observe it.

HIR verification checks compiler-semantic well-formedness. It does not run the owning Contract Establishment judgment.
A verifier may reject an unresolved reference, a role-incompatible target, recovery material, a malformed resolved
relation, or another violation of the HIR contract. It must not reject a fully resolved candidate merely because the
owning Contract law will later refuse to establish it.

Private construction may temporarily contain incomplete structures while one frontend computation is in progress. That
freedom ends at publication. A representation-preserving transform over private HIR material must also leave a valid
HIR result before publication.

HIR verification is compiler correctness machinery. A verifier failure is not a Contract refusal and does not create
Contract Failure meaning.

## 4.9. Normalized Semantic Form

Resolved HIR is source-independent enough that semantically equivalent authoring forms can converge on the same
consumer-visible candidate meaning.

Normalization may remove alias spelling, import spelling, source-only nesting, authoring sugar, or another distinction
whose meaning has already been resolved. The original form may remain available through provenance or source material.

Normalization must not erase a distinction owned by a Contract law. If two authored forms lead to different candidate
meaning under an owning 1D ADR, HIR must preserve that difference.

This normalization is compiler frontend work. It is not the `Canonicalization Contract`. It also does not require one
byte-canonical HIR storage format. The required property is semantic convergence at the HIR observable surface.

If order is not semantic, physical construction order must not become observable meaning. If order is semantic, its
source must be explicit in the owning law or frontend language.

## 4.10. HIR Determinant Set and Frontend Confluence

The observable meaning of one HIR semantic unit must be determined by explicit frontend inputs.

Those inputs include the authored material that owns the candidate, the frontend language version, and the exact
resolution environment required to interpret the candidate. A compiler option belongs to this determinant set only when
it is explicitly allowed to change frontend semantic meaning.

Worker scheduling, cache state, allocation order, current memory layout, and the route by which a query happened to be
computed are not determinants.

Different supported authoring forms may converge on the same HIR meaning. When `.kontrakt` source and a selected 1D
carrier express the same resolved candidate meaning under the same applicable frontend law, the authoring route does not
create a second HIR meaning. Their source origin may remain different provenance.

This confluence rule prevents frontend implementation choice from leaking into Establishment, equality, or reuse.

## 4.11. Typed Extension Boundary

HIR has shared compiler infrastructure, but each Contract authority keeps its own candidate vocabulary.

Adding a new 1D Contract family must not require existing candidate meanings to be rewritten into a weaker universal
property model. The shared HIR substrate may provide references, publication, provenance relations, ownership, and
projection access. The new authority provides the semantic payload that its own ADR defines.

A generic tag or property bag may exist as physical encoding. It is not the semantic contract of HIR.

---

# 5. Information Retention and Loss

## 5.1. What HIR May Erase

HIR may erase authored distinctions that no longer affect resolved candidate meaning.

Whitespace and comments are not HIR semantics. Import spelling and alias spelling need not survive after they have been
resolved to the same semantic target. Parentheses or authoring sugar may disappear when their meaning is already
represented explicitly. Host object identity, constructor path, getter behavior, carrier nesting, builder shape, and
runtime allocation are not preserved merely because the frontend used them to acquire source evidence.

Source nesting also does not become implicit Contract ancestry.

Erasing an authored distinction is legal only when the owning Contract law does not make that distinction meaningful.

---

## 5.2. What HIR Must Retain

HIR retains every distinction needed to interpret the candidate under the owning Contract law.

It also retains enough exact semantic anchoring to connect later diagnostics and compiler products to the candidate
without recovering meaning from storage topology.

A distinction required later for Establishment cannot be reconstructed from source after HIR publication. If later
Establishment needs to reread a symbol name, rediscover a carrier member, or infer a role from source position, the HIR
boundary is incomplete.

The same principle applies to later lowering. High-level Contract distinctions that remain necessary for a later
semantic boundary must not be discarded simply because a lower representation would be easier to store.

---

# 6. Recovery and Invalid Source

Parser recovery and poisoned source material are not Resolved Contract HIR.

The frontend may keep recovery material so that it can report more than one error, support an editor, or continue
parsing nearby definitions. That material remains in the source or recovery domain.

```text
Source / Recovery Material
        ↓
resolution attempt
       / \
      /   \
resolved  rejected
   ↓         ↓
 HIR     diagnostic material
```

A missing token is not explicit Contract absence. An unresolved symbol is not an empty semantic reference. A recovery
node is not a partially authoritative Contract definition.

Tooling may expose a separate incomplete frontend view when necessary. Such a view must not be accepted as Resolved
Contract HIR by Establishment.

Invalid source must not poison unrelated resolved semantic units.

If one candidate cannot satisfy the HIR admission invariant, that candidate and any semantic unit whose exact meaning
depends on it are not published as valid HIR. An independent candidate may still be formed and verified when its own
determinant closure is complete. The compiler records the rejected source through diagnostic or recovery products rather
than inserting a fabricated HIR placeholder.

Compilation success is separate from this failure-isolation rule. A batch build may still fail because any required
source unit is invalid, while diagnostics or tooling continue to consume already-valid HIR units.

---

# 7. Establishment Handoff

Resolved HIR must be sufficient for the owning Definition Establishment judgment.

The handoff is complete when Establishment can consume the candidate meaning and its explicitly permitted semantic
basis without reopening authored source.

```text
Resolved HIR Candidate
    +
Definition-time semantic basis allowed by the owning law
    ↓
Authority-Owned Establishment
```

Establishment does not repair missing frontend resolution.

HIR publication does not establish authority. Establishment does not become a compiler normalization pass. Canonical
Contract World publication remains a later compiler representation of already-established Definition meaning.

---

# 8. Observable HIR Surface

## 8.1. Consumer Contract

HIR is producer-independent and consumer-aware.

The frontend does not encode the topology of future consumers. It does, however, publish enough resolved meaning that a
valid consumer does not need a private semantic frontend.

Authority-Owned Establishment is the primary semantic consumer. Frontend diagnostics, tooling, query and reuse
infrastructure may also consume HIR directly. Later verifier, optimizer, execution, and backend work normally consume
established or lower material instead. Their needs still matter when deciding whether HIR is discarding information too
early.

Generated API semantics must not bypass Establishment by treating HIR as authoritative Contract definition material.

---

## 8.2. Stable Semantic Units

HIR must expose semantic units that can be addressed independently of physical layout.

A whole frontend generation is a coherent publication unit. It is not required to be the only dependency or reuse unit.
A definition, interface-level surface, interaction-level surface, or another meaningfully independent projection may be
consumed separately when the HIR semantics support that separation.

The exact projection catalog remains a compiler design decision.

A projection is not a new Contract authority and is not automatically a new IR level.

## 8.3. Reference Direction and Ownership

A resolved HIR reference denotes another semantic subject. It does not physically own that subject merely because the
source syntax was nested or one declaration mentioned another.

The compiler may store related subjects next to one another, but consumers must observe an exact reference relation
rather than infer semantic ownership from containment, parent pointers, object nesting, or table adjacency.

This keeps source hierarchy, semantic reference, and physical storage as separate concerns. It also permits one
definition-level product to be reused without copying every referenced definition into the same physical object graph.

## 8.4. Semantic Ownership and Partition

HIR needs an explicit logical ownership boundary for independently addressable compiler-semantic material.

An owner is a compiler publication and reuse boundary. It is not Contract authority, source nesting, or object
containment. An independently published HIR subject belongs to one logical owner, and a relation to material owned
elsewhere is expressed as an exact semantic reference.

This prevents one large mutable graph from becoming the only unit of lowering, verification, reuse, or reclamation. It
also gives query and incremental infrastructure a stable unit larger than individual fields and smaller than the whole
frontend world.

The exact owner catalog remains design work. Interface, Interaction, Operation, and 1D Definition boundaries are
candidates only where their semantic and reuse laws justify independent ownership.

## 8.5. Stable Semantic Locator and Generation-Local Handle

Cross-generation identity and fast in-generation addressing are different requirements.

A semantic unit that may be compared or reused across generations needs a deterministic semantic locator that survives
unrelated physical relocation. The compiler may also assign a dense generation-local handle for fast access inside one
published generation.

```text
stable HIR semantic locator
    → cross-generation lookup / comparison

generation-local handle
    → fast access inside one generation
```

Neither value is Contract Definition identity merely because it is stable or efficient.

A generation-local handle cannot be interpreted in another generation without an explicit remap or validation. A
persistent product must not serialize a raw local handle as though it were a cross-session semantic identity.

## 8.6. Consumer Access Discipline

Ordinary consumers read HIR through typed semantic subjects, references, and projections. They do not gain semantic
authority by traversing backing objects, parent pointers, table adjacency, or mutable producer state.

The implementation may expose specialized internal access for the HIR producer, verifier, serializer, or representation
transform. That access is not the stable consumer contract.

Derived reverse indexes, lookup accelerators, and cached navigation tables may be built when useful. They remain
recomputable compiler products unless an owning semantic law explicitly says otherwise.

This access boundary allows a later implementation to introduce lazy materialization or dependency observation without
forcing consumers to learn how HIR is physically stored.

## 8.7. HIR Semantic Access Boundary

Published Resolved HIR must expose a stable semantic access boundary between HIR meaning and its physical realization.

```text
HIR Semantic Contract
    ↓ observed through
HIR Semantic Access Boundary
    ↓ realized by
Physical HIR Representation
```

The access boundary is the compiler-facing seam through which ordinary consumers observe published HIR. It preserves
the meaning of typed subjects, exact references, semantic projections, generation context, and other HIR material that
this ADR makes observable.

The boundary does not expose a slab address, page identity, backing-array position, allocator choice, object topology,
or storage-engine layout as semantic meaning. A compact generation-local handle may cross the boundary when the HIR
model defines that handle as an opaque typed reference. Its numeric value does not become observable layout.

This boundary is logical. It does not require an object-oriented interface, virtual dispatch, wrapper allocation, or one
accessor call per field. A physical realization may provide inlined primitive access, typed bulk reads, contiguous
ranges,
or another compiler-native access path when those paths preserve the same semantic surface.

The first implementation may use primitive slabs. A later implementation may use segmented persistent slabs,
memory-mapped pages, content-addressed chunks, or another representation without changing downstream HIR meaning. A
consumer that requires such a change to rewrite its semantic logic is depending on physical representation rather than
Published Resolved HIR.

The exact protocol shape is not fixed here. Owner granularity, projection catalog, bulk-access forms, handle encoding,
and storage-specific fast paths remain later design work.

---

# 9. HIR Identity and Equality

HIR semantic equality is equality of the resolved candidate meaning exposed by the relevant HIR surface.

It is not Contract Definition identity.

It is also not compiler generation identity, source location, source revision, table position, dense ordinal, JVM object
identity, HID, or fingerprint.

A new HIR generation may carry semantic projections equal to the previous generation. A source move may change
provenance while leaving the HIR semantic projection equal. A different physical layout may carry the same HIR meaning.

Fingerprints and HID may provide efficient equality evidence. They remain implementation mechanisms. A collision or
cache lookup must never be allowed to establish a false semantic equality.

The exact collision-safe comparison strategy remains outside this ADR.

Equality is defined at the semantic surface being consumed. Whole-generation inequality does not imply that every owner
or projection is unequal. A projection family must therefore have an equality law strong enough to decide whether its
own consumer-visible meaning changed. Generic object equality or serialized-byte equality is not the semantic law unless
the owning HIR product explicitly makes that representation canonical.

---

# 10. Provenance

Source provenance is related to HIR meaning but is not part of HIR semantic equality unless an owning semantic law
explicitly requires that origin as meaning.

A semantic HIR subject must still be connectable to its authored origin for diagnostics and tooling.

```text
HIR semantic subject
    ── provenance relation ──> source material
```

This relation allows semantic and provenance validity to change independently.

A comment edit or line movement may require new source projection while leaving semantic HIR unchanged. A semantic edit
may invalidate the HIR projection even when a source span happens to remain identical.

The compiler must not store formatted diagnostic text as HIR meaning.

One semantic subject may have more than one relevant authored origin. A normalized or synthesized HIR subject may also
need an origin chain that points to the source material from which the compiler formed it. HIR therefore does not assume
a mandatory one-subject-to-one-span provenance model.

Synthetic compiler material that has no direct authored token must still be distinguishable from missing provenance.
Its provenance may identify the semantic source that caused synthesis without making that source location part of HIR
equality.

---

# 11. Publication

## 11.1. Frontend Working State

Before Resolved HIR is published, the frontend may keep private working state while it resolves and normalizes authored
material. A resolver may use temporary symbol maps, worklists, intern tables, arenas, or another bounded representation
to perform that work.

This working state is compiler implementation material. It is not Resolved HIR. Establishment, query consumers, tooling,
and other independent downstream work must not depend on a half-resolved builder or on the shape of the temporary
structures used to produce HIR.

The frontend may retain source and working material for diagnostics, tooling, or later frontend work. Retention does not
make that material part of published HIR meaning.

---

## 11.2. Published Generation

A HIR generation becomes visible only after the producer has completed resolution and validated the HIR admission
invariant.

```text
private construction
    ↓
resolution complete
    ↓
HIR invariant validation
    ↓
publish generation G
    ↓
read-only consumers
```

Publication is a compiler lifecycle boundary. It adds no Contract authority.

A published generation is immutable to ordinary consumers. The physical realization may use immutable structures,
sealed tables, overlays, snapshots, persistent structures, or another mechanism that preserves this rule.

## 11.3. Publication Granularity and Materialization

A coherent HIR generation does not require one monolithic physical publication barrier.

The compiler may publish a generation as a stable manifest over independently sealed owner units or semantic
projections. A unit becomes observable only after that unit satisfies the HIR invariant and is associated with the same
coherent generation inputs.

Physical materialization may be eager or demand-driven. A lazy unit is not permission to expose partially resolved
state. When a consumer receives the unit, the same HIR admission and verification laws apply as they do to eagerly
materialized HIR.

This allows V1 to use a simple eager implementation while preserving a path to owner-local queries, IDE demand, and V2
incremental materialization without changing HIR meaning.

The exact publication and materialization granularity remains a compiler design decision.

---

# 12. HIR Lifecycle and Transition Model

HIR lifecycle is compiler operational meaning. It is not the Contract State / Transition authority defined elsewhere.

The logical lifecycle is:

```text
construction candidate
    ↓ successful validation
published generation
    ↓ newer generation published
superseded generation
    ↓ no legal consumer requires it
retired generation
    ↓ physical lifetime ends
reclaimed storage
```

A failed or cancelled construction candidate is discarded. It does not become a partial published HIR. If an older
generation is already published, that failed transition does not invalidate the older generation merely because a
replacement attempt began.

Only a successfully completed publication may supersede the previous current generation. Publication of the newer
generation does not mutate the semantic meaning of the older generation. It creates a new published result for a new
explicit input set.

Supersession and reclamation are separate transitions. An older generation may remain readable while a newer generation
is already current.

The compiler must not require one mutable lifecycle enum on every HIR node. Lifecycle may be represented by generation
ownership, product metadata, publication handles, or another mechanism.

HIR architecture must permit lifecycle management at a finer semantic granularity than the whole frontend world when
that is useful. Fine-grained lifecycle units are stable semantic products or projections, not arbitrary physical fields.

A semantic owner may outlive its construction scratch state while cold derived indexes or source-oriented material are
discarded earlier. The HIR contract does not require every projection, index, provenance expansion, or decoded body to
remain simultaneously materialized. Selective retention must not make a consumer-visible semantic unit unavailable
while that unit is still validly published and required.

The exact reclamation mechanism remains open.

---

# 13. Snapshot Coherence

A semantic computation that requires one coherent HIR world observes one published generation.

It must not combine a new definition, an old resolution index, and a half-built relation merely because those pieces are
physically reachable at the same time.

Cross-generation reuse is allowed when an equivalent semantic projection has been validated for the new generation. The
logical consumer still observes that projection as valid input to its current generation.

This rule preserves snapshot coherence without requiring the compiler to copy every unchanged definition into every
new physical generation.

---

# 14. Query, Product, and Manager Compatibility

Resolved HIR is a compiler product. It is not a query object and it is not owned by a Manager.

A query-oriented compiler may request whole HIR generations or smaller HIR projections. An Analysis Manager may attach
derived results to an exact HIR subject and validity context. A local Pass Manager may orchestrate HIR-preserving
normalization before publication or over a private generation.

None of those infrastructures defines HIR meaning.

HIR must therefore provide stable semantic subjects, clear generation validity, and an observable read surface without
requiring one query scheduler, one Analysis Manager API, or one pass pipeline.

Derived knowledge remains outside HIR. If several consumers need the same expensive calculation, Kontrakt may publish a
shared derived product rather than storing the result inside unrelated HIR definitions.

Query and analysis scope should follow semantic ownership when that gives a smaller valid dependency boundary. A
consumer that needs one Definition projection should not be forced to depend on the full frontend generation merely
because the first physical implementation stores both in one allocation domain.

---

# 15. Dependency Observation

Query and incremental infrastructure may record which HIR semantic products or projections a computation consumed.

Dependency observation must occur at a semantic product boundary rather than at an accidental storage boundary.

A read of slab offset `N`, object field `x`, or table row `R` is not by itself a permanent compiler dependency law.
Physical representation must remain replaceable.

The following graphs remain different:

```text
HIR semantic relations
compiler query dependencies
build / artifact dependencies
source provenance relations
```

A query edge does not create Required Basis, Basis Binding, Applicability, or another Contract relation.

Relevant external or ambient inputs to a HIR-producing computation must be explicit. Hidden clock, locale, random state,
mutable global state, or undeclared environment state cannot silently change the HIR result.

---

# 16. Cache and Reuse Law

Cache is work avoidance.

It is not HIR authority and it is not Contract authority.

A cached HIR product may be reused only after the compiler has established compatibility with the requested semantic
product. A stale, corrupt, missing, or incompatible entry falls back to valid computation or produces an explicit
compiler failure when recomputation itself is impossible.

Deleting the cache must not change HIR meaning.

Warm-cache and cold-cache compilation must produce the same observable HIR result for the same explicit inputs.

The same law applies to persistent CAS-like storage, in-memory memoization, generation-local caches, and future remote
reuse. Their retention policy does not become HIR semantics.

---

# 17. Early Cutoff

HIR supports semantic early cutoff.

If a recomputed semantic projection is equivalent to the previous projection, downstream products whose exact semantic
inputs are limited to that projection need not be invalidated further.

```text
input revision changed
    ↓
HIR projection recomputed
    ↓
consumer-visible HIR meaning unchanged
    ↓
semantic propagation may stop
```

Early cutoff is based on consumer-visible semantic equality, not on the fact that a cache entry exists or the aggregate
HIR generation changed.

A provenance-only change may therefore refresh source projection and diagnostics while allowing semantic downstream
products to remain reusable.

Early cutoff is an optimization. Failing to take the cutoff may cost time. Taking an unsound cutoff is a correctness
failure.

---

# 18. Incremental Architecture Boundary

V1 uses query-oriented compiler orchestration and may use generation-bound in-memory reuse, explicit dependency
recording, and local early cutoff.

V1 does not need to implement the final V2 incremental engine.

V2 may add persistent dependency state, cross-session HIR reuse, incremental parsing and resolution, change-frontier
propagation, delta maintenance for suitable domains, lazy repair, or other algorithms.

No one repair strategy is required across the whole frontend.

A small semantic projection may use memoized validation while another domain recomputes from scratch. A relational
analysis may later justify delta maintenance. A cheap frontend product may simply rerun because dependency bookkeeping
would cost more than recomputation.

Every strategy is constrained by the same HIR result law.

```text
incremental result
    == HIR semantic result required by clean computation
```

If that equivalence cannot be established, Kontrakt uses the deterministic clean path.

---

# 19. Full-Recompute Reference Path

HIR correctness must not depend on persistent incremental state.

Kontrakt must retain a conceptually complete path that can rebuild HIR from the explicit frontend inputs without using
previous HIR products.

This path is required for corruption recovery, differential validation, cache-off testing, and incremental-equivalence
testing.

A future persistent compiler may optimize how often this path runs. It may not make old incremental state the only
source from which correct HIR meaning can be recovered.

---

# 20. Pre-Establishment Transformation Law

Compiler transformations may improve HIR representation before Establishment.

They may remove syntax-only structure, normalize an already-defined frontend meaning, intern exact references,
deduplicate equivalent compiler representation, compact storage, or precompute a resolved projection.

The legal relation is:

```text
HIR meaning before transform
    ==
HIR meaning after transform
```

A transform does not gain permission to discharge an authority-owned Contract judgment merely because the answer seems
static.

Policy selection, Governance judgment, Applicability result, Fact establishment, or another Contract authority remains
at its owning boundary.

A representation-only transform does not justify a new HIR level. A semantic transformation that changes the HIR
observable meaning produces a different HIR result and must be published under the appropriate new generation.

---

# 21. Concurrency

Parallel frontend work is allowed only behind deterministic publication.

Workers may build independent private candidate material. Their completion order must not determine semantic identity,
reference assignment, definition ordering, or user-visible deterministic output.

A deterministic merge or equivalent publication law resolves concurrent candidate work into one coherent HIR
generation.

Cancellation must not expose a partial generation. A stale worker result for an older input or generation must not
replace a newer valid publication.

The exact lock, epoch, persistent-structure, actor, work-stealing, or transaction mechanism remains implementation.

---

# 22. Persistence and Cross-Session Products

A future implementation may persist HIR products across compiler sessions.

Persistence requires an explicit product compatibility boundary. The persisted product format, producer schema version,
frontend language version, target-independent semantic identity, and Contract Version are separate concerns.

An incompatible persistent product is discarded or migrated by an explicit compiler product rule. It is not silently
reinterpreted as current HIR meaning.

Content-addressed storage may be used to deduplicate immutable products. A CAS address remains a storage identity, not a
Contract identity or HIR semantic law.

A restored persistent HIR product must pass the same semantic admission boundary as a newly computed product. The
implementation may avoid re-running every expensive check when schema compatibility, integrity validation, and prior
verification provide equivalent evidence. Deserialization or cache presence alone is not publication authority.

---

# 23. Diagnostics and Tooling

Frontend diagnostics may refer directly to HIR semantic subjects and join them with provenance.

A diagnostic renderer does not own HIR meaning. A source location does not own HIR identity.

Tooling may request stable HIR projections when it needs resolved semantic information. IDE recovery needs may also keep
source or partial semantic products that are not valid Resolved HIR.

The batch compiler and an IDE must not become two different Contract semantic engines. Both ultimately consume or form
the same HIR semantic contract when material is fully resolved.

---

# 24. Non-Normative Reference Semantic Model

The normative sections above define HIR meaning. This section gives one concrete compiler-oriented realization of that
meaning. It is intentionally close to the physical direction already used elsewhere in Kontrakt so that the semantic law
can be implemented without first translating it into a heap object model.

The example does not define the required storage schema. It demonstrates the required separation.

```text
resolved semantic subjects and relations
    ≠
source provenance
    ≠
query / cache state
    ≠
physical storage identity
```

The reference model uses typed columnar families and generation-local dense ordinals.

```text
HIR Generation G

Owner columns
    ownerStableKeyRef[]
    ownerFirstSubject[]
    ownerSubjectCount[]

Subject columns
    subjectKind[]
    subjectOwner[]
    subjectStableKeyRef[]

Operation columns
    operationCommandTypeRef[]
    operationResultTypeRef[]
    operationInputRef[]
    operationAdmissionRef[]
    operationCanonicalizationRef[]
    operationLoweringRef[]

Lowering columns
    loweringEdgeBase[]
    loweringEdgeCount[]

Lowering-edge columns
    loweringSourceCoordinateRef[]
    loweringTargetCoordinateRef[]

Stable-key storage
    semanticKeyBytes[]
    semanticKeyOffset[]
    semanticKeyLength[]

Provenance sidecar
    provenanceSubjectRef[]
    provenanceSourceRef[]
    provenanceStart[]
    provenanceEnd[]
```

Each array name denotes a logical column. A JVM realization may back high-cardinality columns with primitive arrays or
FFM `MemorySegment` storage. Small metadata may use another compact representation when that is cheaper. The semantic
contract does not depend on the backing choice.

A local reference such as `operationInputRef[operationOrdinal]` is a dense reference inside one published generation. It
is not a Contract `Definition Reference` and it is not stable across generations.

A stable semantic locator is represented separately. The example stores its canonical bytes in `semanticKeyBytes` and
uses offset/length columns to address them. This avoids making a table row, object address, current ordinal, or one
fixed
hash width the cross-generation identity law.

The stable-key encoding shown here is only a compiler representation example. The final HID or fingerprint encoding
remains outside this ADR.

Each 1D authority keeps its own semantic payload family. For example, an Input candidate may have an
`inputPayloadRef[]` column into Input-owned semantic slabs. A Lowering candidate may have the edge ranges shown above.
Kontrakt must not replace those authority-specific meanings with one generic property map or universal edge record.

The important shape is:

```text
shared compact HIR substrate
+
typed authority-specific semantic slabs
+
exact generation-local references
+
separate stable semantic locators
+
separate provenance
```

This gives consumers a typed semantic surface without requiring a pointer-heavy object graph.

---

# 25. Non-Normative End-to-End Reference Realization

This example follows one authored Operation from source acquisition to a published HIR generation. It also shows
verification, publication, query consumption, semantic early cutoff, and generation retirement using compiler-oriented
storage.

The source shape follows the existing IDL decisions.

```text
interface DepositContract {
    policy        DepositPolicy
    governance    DepositGovernance
    budget        DepositBudget
    capacity      DepositCapacity
    facts         DepositFacts
    invariants    DepositInvariants

    operation deposit(command: DepositCommand): DepositRecorded {
        manifest {
            flow:
                input             DepositInput
                admission         DepositAdmission
                canonicalization  DepositCanonicalization
                lowering          DepositLowering
        }

        lowering DepositLowering {
            accountIdText -> command.accountId
            amountText    -> command.amountMinor
        }
    }
}
```

## 25.1. Source Acquisition and Frontend Working Storage

The parser may retain source-oriented syntax in a compact arena. The exact parser representation is not HIR.

One possible working layout is:

```text
byte[]  declKind
int[]   declNameTokenRef
int[]   declFirstSlot
int[]   declSlotCount

byte[]  slotKind
int[]   slotTargetTokenRef

int[]   loweringFirstEdge
int[]   loweringEdgeCount
int[]   loweringSourceTokenRef
int[]   loweringTargetTokenRef
```

The source buffer and token index remain separate. `slotTargetTokenRef` still points to lexical material. It is
therefore
not a valid HIR reference.

Resolution may use bounded transient structures such as an open-addressed symbol index, work queues, scratch ordinals,
or intern tables. Those structures belong to the producer episode. They are not published HIR.

The frontend may release scratch structures as soon as the information has been lowered into the resolved HIR candidate.

## 25.2. Deterministic Pre-Count and Direct HIR Formation

The reference path does not build a graph of `ResolvedOperationCandidate` objects and later copy it into tables.

It first determines the required family sizes.

```text
count owners
count subjects by authority kind
count operations
count Lowering relations
count provenance ranges
```

It then allocates exact or bounded-capacity column storage once.

```text
allocate owner columns
allocate subject columns
allocate operation columns
allocate authority-specific payload slabs
allocate provenance sidecar
```

Local ordinals are assigned by a deterministic compiler ordering that does not depend on worker completion order, hash
iteration order, object allocation order, or cache state.

Resolution then writes directly into the candidate HIR slabs.

For the `deposit` Operation, the resulting local relations are conceptually:

```text
op = operationOrdinal(DepositContract.deposit)

operationInputRef[op]            = inputOrdinal(DepositInput)
operationAdmissionRef[op]        = admissionOrdinal(DepositAdmission)
operationCanonicalizationRef[op] = canonicalizationOrdinal(DepositCanonicalization)
operationLoweringRef[op]         = loweringOrdinal(DepositLowering)
```

The Lowering payload is written as a contiguous range.

```text
lower = loweringOrdinal(DepositLowering)
base  = loweringEdgeBase[lower]

loweringSourceCoordinateRef[base + 0] = coordinateOrdinal(DepositInput.accountIdText)
loweringTargetCoordinateRef[base + 0] = coordinateOrdinal(deposit.command.accountId)

loweringSourceCoordinateRef[base + 1] = coordinateOrdinal(DepositInput.amountText)
loweringTargetCoordinateRef[base + 1] = coordinateOrdinal(deposit.command.amountMinor)

loweringEdgeCount[lower] = 2
```

The ordinals are generation-local fast references. The source spelling `"DepositInput"` is no longer consulted by
ordinary downstream semantic consumers.

Original spelling and source ranges remain reachable through the provenance sidecar.

The candidate is still non-authoritative. Direct-to-slab formation does not perform Contract Establishment.

## 25.3. HIR Verification as a Deterministic Table Scan

HIR verification validates the candidate generation before publication. The reference path does not use assertion-style
`require(...)` calls as the verification model.

Verification scans typed slabs in deterministic ordinal order and writes structured compiler violations into a bounded
violation buffer.

```text
short[] violationCode
int[]   violationSubject
int[]   violationRelation
int     violationCount
```

A simplified scan is:

```text
for op in 0 ..< operationCount:
    input = operationInputRef[op]
    if input < 0 or input >= inputCount:
        emit(HIR_INVALID_INPUT_REF, operationSubject(op), input)

    admission = operationAdmissionRef[op]
    if admission < 0 or admission >= admissionCount:
        emit(HIR_INVALID_ADMISSION_REF, operationSubject(op), admission)

for lower in 0 ..< loweringCount:
    base  = loweringEdgeBase[lower]
    count = loweringEdgeCount[lower]

    if base < 0 or count < 0 or base + count > loweringEdgeTotal:
        emit(HIR_INVALID_LOWERING_RANGE, loweringSubject(lower), base)
        continue

    for edge in base ..< base + count:
        src = loweringSourceCoordinateRef[edge]
        dst = loweringTargetCoordinateRef[edge]

        if not exactInputCoordinate(src):
            emit(HIR_INVALID_LOWERING_SOURCE, loweringSubject(lower), edge)

        if not exactOperationCoordinate(dst):
            emit(HIR_INVALID_LOWERING_TARGET, loweringSubject(lower), edge)
```

Additional scans reject unresolved lexical references, recovery material, invalid owner ranges, incompatible slot target
kinds, malformed stable-key slices, and non-deterministic observable ordering.

The scan produces compiler diagnostic material when violations exist. The candidate generation is not published as valid
Resolved HIR.

The verifier does not decide whether `DepositInput` or `DepositLowering` receives Contract authority. That remains the
owning Establishment law.

The physical validator may use bitsets, vectorized range checks, partitioned validation, or parallel local scans when
the
result remains deterministic. Those are implementation choices.

## 25.4. Reference V1 Physical Layout

A production-oriented V1 baseline should not require one heap object per HIR subject or relation.

A representative JVM layout is:

```text
Generation header
    primitive counts
    product/schema version
    stable-key index reference
    provenance index reference

Hot semantic slabs
    operation columns          -> primitive slab / FFM segment
    subject columns            -> primitive slab / FFM segment
    owner columns              -> primitive slab / FFM segment
    authority-specific refs    -> primitive slab / FFM segment
    relation ranges            -> primitive slab / FFM segment

Variable-width canonical material
    semantic-key byte slab
    optional canonical payload byte slabs where justified

Cold side material
    provenance ranges
    authored spelling references
    diagnostic-only source relations

Derived infrastructure outside HIR meaning
    query dependency records
    cached projection fingerprints
    persistent-product metadata
```

High-cardinality material should prefer primitive columnar storage. FFM-backed slabs are the default physical direction
when off-heap storage, explicit lifetime, or larger contiguous regions make them profitable. Heap primitive arrays
remain
a replaceable option where measurement favors them.

The formation path follows the same memory discipline already used elsewhere in Kontrakt:

```text
pre-count
    ↓
pre-size
    ↓
deterministic local ordinal assignment
    ↓
direct-to-slab formation
    ↓
release frontend scratch that is no longer needed
    ↓
batch HIR verification
    ↓
seal
    ↓
publish generation
```

There is no required intermediate object graph between resolved frontend facts and published HIR slabs.

Vertical partitioning remains important. Establishment for Input should not have to touch provenance bytes, diagnostic
strings, Lowering edge slabs, or unrelated authority payloads merely because they share one HIR generation.

## 25.5. Publication and Consumer Access

After verification succeeds, Kontrakt publishes the sealed slab set as one coherent HIR generation.

Publication changes visibility. It does not rewrite semantic rows in place.

```text
working slabs W42
    ↓
complete direct formation
    ↓
verify W42
    ↓
seal slabs
    ↓
publish manifest G42
```

A consumer obtains a typed semantic projection. The projection may resolve a stable semantic key through the generation
index and then use one dense ordinal for hot reads.

```text
DepositInput stable key
    ↓ generation index
input ordinal 17
    ↓
input-owned semantic columns
```

The ordinal `17` is not recorded as cross-generation semantic identity.

Conceptually, compiler products consume boundaries such as:

```text
Establishment
    consumes InputProjection(DepositInput)

Generated API product
    consumes OperationProjection(DepositContract.deposit)

Frontend diagnostic
    consumes LoweringProjection(DepositLowering)
    consumes ProvenanceProjection(DepositLowering)
```

Query infrastructure records those semantic product reads. It does not make `inputPayloadRef[17]` or a raw FFM address
the permanent dependency law.

## 25.6. Semantic Early Cutoff

Assume a comment moves `DepositInput` to another source range without changing its resolved candidate meaning.

`G43` may contain different provenance while the Input semantic projection remains equivalent to `G42`.

```text
G42
    InputProjection(DepositInput) = S
    Provenance(DepositInput)      = P42

G43
    InputProjection(DepositInput) = S
    Provenance(DepositInput)      = P43
```

A fast fingerprint may reject equality quickly when the values differ. A matching fingerprint is not by itself Contract
or HIR semantic authority. Reuse must follow the HIR projection equality law or another collision-safe validation rule.

When equality is established, propagation may stop at the Input projection boundary. Products that consume provenance
still observe `P43`.

If the Lowering target changes, the Lowering semantic projection changes. Its dependents cannot reuse the previous
result
through the same cutoff. Unrelated Input projections remain independently reusable.

This is why the HIR product boundary is finer than one serialized generation blob even when physical storage is grouped
into large slabs.

## 25.7. Generation Transition and Reclamation

A new generation is produced beside the old one. The old published slabs are not mutated into the new meaning.

```text
G42 published and readable
        │
        ├──────── readers may still pin G42
        │
source revision 43
        ↓
construct W43
        ↓
verify W43
        ↓
seal W43
        ↓
publish G43
        ↓
G42 superseded for new requests
        ↓
last legal G42 reader releases its pin
        ↓
G42 retired
        ↓
G42 slabs reclaimed
```

A failed W43 formation or verification leaves G42 intact. Publication and reclamation are separate lifecycle operations.

The exact pinning or reclamation mechanism remains open. Reference counting, epochs, arenas, RCU-like retirement, or
another deterministic-safe strategy may realize the same lifecycle law.

## 25.8. Owner-Local Formation and Failure Isolation

The compiler does not need one mutable HIR tree for the whole project.

A logical HIR owner may correspond to one independently addressable semantic unit or one deterministic group of closely
related units. Its hot material occupies ranges inside typed slabs.

```text
ownerOrdinal
    ↓
ownerFirstSubject[ownerOrdinal]
ownerSubjectCount[ownerOrdinal]
```

A query that needs `DepositLowering` can resolve its stable semantic locator to the current owner-local ordinal and read
only the required Lowering range.

If another independent definition fails resolution, no poison object is inserted into the valid `DepositLowering` rows.
The failed owner is rejected from valid Resolved HIR publication. Independent owners remain valid when their determinant
closure does not include the failure.

V1 may form all owner ranges eagerly. V2 may materialize or repair selected owner products on demand. Both paths must
publish the same HIR semantic meaning for the same valid inputs.

---

# 26. Non-Normative Implementation Reading Guide

The reference realization should be read as a compiler storage model, not as a proposed Kotlin domain model.

The parser owns source-oriented syntax arenas. Resolution owns conversion from lexical references to exact HIR targets.
HIR formation writes resolved candidate meaning into typed slabs. HIR verification checks compiler-semantic
well-formedness. Publication exposes sealed generations. Provenance owns source origin. Query and analysis
infrastructure
own dependency, validity, and reuse state. Establishment owns Contract authority.

The production baseline should avoid a per-node heap object graph for high-cardinality HIR material. The preferred
physical direction is deterministic dense ordinals, primitive columns, direct ranges, compact indexes, and FFM-backed or
heap-primitive slabs selected by measured cost.

Temporary frontend objects are permissible only when they are bounded construction aids and do not become the published
HIR model. The normal hot path should be able to lower resolved working material directly into pre-sized HIR storage
without allocating one wrapper object per semantic row or relation.

Consumers should see typed semantic projections rather than raw backing storage. A projection implementation may reduce
to a few ordinal-indexed slab reads. The consumer contract must survive a later replacement of heap primitive arrays by
FFM segments, a different local ordinal assignment, or a different stable-key index.

A good implementation test is therefore stronger than class-shape compatibility. Replacing the physical HIR backing
must not require Establishment, diagnostics, query consumers, or generated-product consumers to reinterpret source
syntax
or change the semantic dependency they declare.

---

# 27. Non-Normative Engineering Validation

The HIR implementation should have a deterministic inspection path for compiler QA. The inspection form is a derived
debug product, not HIR authority.

It should be possible to compare clean and reused compilation at the HIR semantic boundary. A comment-only edit should
be able to demonstrate semantic equality with changed provenance. A semantic edit should identify the projections that
changed. Randomized worker completion order should not change the semantic inspection result.

HIR verification should also be runnable in compiler tests after HIR formation and after any transformation that claims
to preserve the HIR contract. This follows the same general engineering discipline used by production IR systems: valid
IR is a precondition for later work, and transformations must return material that satisfies the IR invariants before it
is published to the next consumer.

Compiler QA should also move unrelated declarations and reorder independent construction work while checking that stable
semantic keys and HIR projections remain equivalent. Tests should reject cross-generation use of a generation-local
handle unless an explicit remap occurs. An invalid definition should not create poison HIR for an independent owner.
Eager and demand-driven materialization, when both are implemented, must produce the same semantic inspection result.

---

# 28. Consequences

The frontend gains a strict semantic checkpoint before Contract authority.

Establishment can remain authority-focused because it receives exact candidate meaning rather than frontend search
problems. Diagnostics and tooling can share the same resolved meaning without becoming semantic authorities of their
own.

The compiler also gains a stable product boundary for query orchestration and future persistence. Whole-generation
snapshot coherence can coexist with definition-level reuse. Provenance can refresh independently from semantic meaning.

The cost is explicit architecture work. HIR needs stable semantic references, generation publication, projection
boundaries, equality rules, and lifecycle ownership. Those costs are accepted because leaving them implicit would move
the same complexity into every downstream subsystem and make V2 invalidation depend on accidental representation.

This ADR does not require HIR to be physically object-heavy. The stronger semantic boundary allows the implementation to
use more aggressive tables, slabs, interning, persistent structures, or other compact representations behind the
published contract.

---

# 29. Rejected Alternatives

## 29.1. Parsed Source Directly to Establishment

This would keep source lookup and syntax interpretation inside authority-owned judgment. It would also make multiple
authoring frontends harder to converge on one semantic boundary.

Rejected.

## 29.2. One Mutable Mega-HIR

A representation that stores syntax, resolved meaning, diagnostics, analysis results, cache state, backend state, and
incremental metadata in one mutable graph would couple unrelated lifetimes and invalidation rules.

Rejected.

## 29.3. Recovery Nodes Inside Published Resolved HIR

This weakens the meaning of `Resolved` and forces every consumer to distinguish complete meaning from parser poison.
Recovery remains a sibling frontend concern.

Rejected for the published HIR semantic surface.

## 29.4. One Whole-HIR Dependency Unit

This makes small semantic changes invalidate unrelated consumers and blocks definition-level early cutoff.

Rejected as the only dependency model.

## 29.5. Physical Field Reads as Incremental Dependencies

This would turn layout into incremental architecture and make later storage replacement expensive.

Rejected.

## 29.6. Fingerprint or CAS Identity as Semantic Identity

A hash or storage address can accelerate equality and lookup. It cannot replace the semantic equality law.

Rejected.

## 29.7. Incremental State as Correctness Authority

A compiler that can only produce correct HIR when previous cache or dependency state is present has lost the semantic
source of truth.

Rejected.

## 29.8. Global Compiler Lifecycle State Machine

The compiler does not move linearly from `parsed` to `HIR` to `established` to `optimized` as one mutable global state.
Different products may exist at different generations and lifetimes.

HIR keeps its own publication lifecycle without becoming the global compiler state machine.

Rejected.

## 29.9. Source Containment as HIR Ownership

Using parser nesting or host object containment as the permanent HIR owner relation would make source shape control
publication, reuse, and lifetime.

Rejected.

## 29.10. Generation-Local Handles as Persistent Identity

A dense ordinal, arena index, row number, or local interner ID may be efficient inside one generation. Reusing that
value
as a cross-generation or cross-session semantic locator would couple persistence to physical construction order.

Rejected.

---

# 30. V1 Requirements

V1 must provide a real Resolved Contract HIR boundary between frontend resolution and Establishment.

Published HIR must satisfy the admission invariant and remain read-only to ordinary consumers. The compiler must keep
source provenance separate from semantic equality. Stable definition-level or equivalent projections must be possible
even if the first physical implementation builds one larger frontend product.

V1 query orchestration must be able to identify explicit HIR inputs, observe product dependencies at stable semantic
boundaries, and reuse a published result only under a valid generation or equivalent validity rule.

V1 must support deterministic semantic early cutoff where equality is already available and profitable. It need not
incrementalize every frontend computation.

V1 must define a logical HIR ownership or partition boundary, stable semantic locators for independently reusable units,
and a separate generation-local addressing mechanism when dense local handles are used. The first implementation may
materialize all units eagerly, but consumers must not depend on that choice.

Invalid source must be isolated at the smallest sound semantic owner boundary. Unaffected owners may remain available to
frontend diagnostics and tooling even when the overall compilation cannot succeed.

V1 must expose Published Resolved HIR through a semantic access boundary that does not require ordinary consumers to
depend on the physical HIR layout. The first backing representation may be primitive slabs without making that choice
part of HIR meaning.

Cache-off and clean-recompute execution must remain valid and must agree with reused execution.

---

# 31. V2 Evolution Seam

V2 may add persistent HIR generations and finer frontend repair without changing this ADR.

Possible work includes persistent semantic projections, owner-local or demand-driven materialization, incremental lexing
and parsing, incremental resolution, provenance-only refresh, cross-session early cutoff, Merkle-style localization,
dynamic dependency repair, domain-local delta maintenance, and adaptive switching between repair and rebuild.

Those techniques remain compiler realization.

V2 must preserve the same admission invariant, semantic equality, authority boundary, publication law, and
determinism-first rule.

---

# 32. Intentionally Open

This ADR does not freeze the HIR physical schema.

It does not decide whether V1 uses objects, tables, primitive arrays, slabs, persistent structures, or mixed storage. It
does not choose the final HIR projection catalog. It does not fix the concrete API shape of the HIR Semantic Access
Boundary, its bulk-access forms, or its handle encoding. It does not select a query scheduler, Analysis Manager API,
Pass
Manager API, fingerprint algorithm, CAS implementation, reclamation algorithm, serialization format, or V2 repair
algorithm.

It also does not define the semantic payload of each 1D Contract. The owning 1D ADR must still state the candidate
meaning that HIR has to preserve.

The exact compiler product lifetime and memory policy remain design work as long as the publication and validity laws in
this ADR are preserved.

---

# 33. Non-Normative Engineering Basis

This decision is consistent with several production systems, but none of them is a template for Kontrakt.

`rustc` lowers AST into HIR after removing syntax structure that later analysis does not need. HIR lowering is organized
around semantic owners rather than one undifferentiated tree, and stable identity work is separated from
compilation-local
IDs for incremental reuse. Its query system also shows why larger products need smaller semantic projections and why
query dependency must not be confused with the semantic relation represented by HIR. Owner-local identity is especially
important because unrelated movement should not renumber every semantic unit.

Kotlin K2 FIR shows that logical resolution phases can strengthen one frontend semantic representation without requiring
one physically separate full IR for every phase. The relevant architectural lesson is the phase invariant, not FIR's
object model. FIR symbol access also shows the value of giving consumers a stable semantic access surface instead of
requiring them to reach through a symbol into mutable backing declarations.

LLVM and MLIR make IR validity explicit. Their verifier boundaries let later passes assume well-formed input and require
transformations to return valid IR. MLIR also separates a common IR substrate from dialect-owned semantics, which is a
useful comparison for preserving distinct 1D Contract vocabularies over shared compiler infrastructure. Its pass
isolation rules also demonstrate why independently processed units need explicit mutation and ownership boundaries for
parallel work. Kontrakt adopts these invariant and isolation lessons without adopting LLVM or MLIR's universal operation
model.

Swift's Request Evaluator separates derived computations, dependency tracking, and cached results from the underlying
semantic representation. This supports keeping query and analysis state outside HIR meaning while still allowing
fine-grained demand-driven compiler work.

Bazel Skyframe uses immutable computed values and change pruning. Its documentation also records a deliberate preference
for repeatable clean-build equivalence over incremental mutation that is hard to validate. Kontrakt adopts the same
priority at the HIR boundary: incrementality is useful only inside deterministic equivalence.

LLVM CAS demonstrates immutable content-addressed compiler products and deduplication. Kontrakt may use the same kind of
storage technique without turning a content address into semantic authority.

Database MVCC and Linux RCU show a more general publication lesson. Readers need a coherent view, while publication of a
new version and reclamation of an old version are separate problems. Kontrakt uses that principle for HIR generations
without adopting database transactions or RCU as compiler semantics.

DBSP and recent incremental data-flow research show that delta maintenance can be powerful when a domain has a suitable
update algebra. They do not justify one compiler-wide incremental algorithm. Recent empirical work on incremental
program analysis also reports meaningful memory and consistency costs, reinforcing the need for a clean deterministic
fallback and domain-specific repair choices.

The *Modern Compiler Architecture 01-15* material provides the same general constraints: stage boundaries are defined by
new invariants, logical stages are separate from physical materialization, semantic identity should be separated from
volatile provenance, published material should not expose partial construction, and incremental architecture begins with
stable result and dependency boundaries rather than a cache implementation.

---

# 34. Final Law

Resolved Contract HIR is the deterministic published compiler-semantic form of fully resolved Contract candidates before
Contract authority.

It preserves all candidate meaning required by the owning Contract laws and removes frontend ambiguity that later
semantic work must not repeat.

Its published semantic surface is independent of source provenance, physical layout, query topology, manager topology,
cache state, generation identity, and incremental algorithm.

HIR generations may expose finer semantic projections so compiler products can be reused without turning physical
storage into dependency law.

Lifecycle and transition belong to compiler publication and validity. They do not become Contract State or Transition.
A new generation is published instead of mutating already-published meaning in place, and old-generation reclamation is
separate from publication.

Caching, persistence, and incremental repair may avoid work. Early cutoff may stop propagation when a consumer-visible
HIR result is unchanged. None of those mechanisms may change the result that clean deterministic computation would
produce.

The authority boundary remains:

```text
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
```