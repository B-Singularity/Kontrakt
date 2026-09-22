# ADR-0075: Compiler Result Ownership, Legal Consumption, Deterministic Realization, and Contract/Implementation Separation

## Status

Proposed

## Date

2026-09-23

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Boundary
- ADR-0047: One-Dimensional Contract Presentations and Pipeline-Slot Selection
- ADR-0053: Version Contract
- ADR-0056: Governance Contract
- ADR-0057: Failure Contract
- ADR-0058: Publication Contract
- ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency
- ADR-0064: Input Contract
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0069: Invariant Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform, External Technology, and Realization Boundary
- ADR-0074: Compiler Result, Explicit Unsuccessful Result, Recovery, and Observation Boundary
- `kontrakt-1d-hir-establishment-master-checklist.md`
- `kontrakt-compiler-total-architecture-map-design-draft.md`
- `kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `kontrakt-v2-incremental-architecture-research-todo.md`
- *Modern Compiler Architecture 01-15*

---

## 1. Context

Kontrakt is not a compiler architecture that happens to process contracts.

Its compiler architecture exists to realize a Contract machine whose authority comes from declared obligations.

The governing direction is:

```text
Contract meaning
    ↓ constrains
compiler realization
    ↓ produces
physical realization
```

The reverse direction is not allowed.

```text
query topology
cache state
analysis layout
storage topology
backend shape
worker schedule
physical reachability

    ✕ must not define

Contract meaning
```

`What Contract Is` requires a machine that is explicit, rich enough to state its real obligations, honest about finite
resources and failure, and independent from the mechanism that happens to realize those obligations.

This gives four requirements that precede the usual compiler-product questions.

First, **determinism is primary**. The same exact semantic determinants in the same applicable semantic world must not
gain
different Contract meaning because work was scheduled differently, cached differently, persisted differently, fused,
split, parallelized, repaired, or lowered through another physical path.

Second, **Contract meaning must be explicit and sufficiently rich**. If Input owns order, absence, sameness, topology,
or
another distinction, that distinction must exist in the owning semantic surface. It must not be reconstructed from a
backend object because a later compiler consumer happens to need it.

Third, **the machine must admit reality rather than idealize it away**. Semantic absence, unavailable compiler backing,
unsupported observation, stale retained material, corrupt storage, resource exhaustion, compiler inability, Contract
refusal, and declared Failure are not one state. The compiler must represent the difference at the boundary that owns
the
problem instead of fabricating Contract meaning.

Fourth, **Contract and implementation remain different axes**. Compiler architecture may be explicit, verified,
incremental, cached, persistent, parallel, table-driven, query-oriented, pass-oriented, or replaced entirely. Those
choices must not become the Contract merely because the first implementation needs them.

ADR-0071 and ADR-0063 already close two central semantic boundaries.

ADR-0071 gives complete pre-authority Resolved Contract HIR a legal producer-owned observation surface for
Establishment.
ADR-0063 gives already-established Contract meaning a legal observation boundary without transferring source authority.

The unresolved problem is downstream compiler organization.

Many independent consumers need already-produced semantic or compiler results:

```text
Visible Resolved HIR
    → Establishment

Established Contract meaning
    → diagnostics
    → verification
    → PBT / coverage
    → generated artifacts
    → realization analysis
    → execution formation

Realization material
    → analysis
    → verification
    → optimization

IR
    → analysis
    → transformation
    → lower IR

local products / summaries
    → wider-scope products

compiler results
    → retained / cached / persistent / incremental reuse
```

A common compiler discipline is useful here, but that discipline must not become a second Contract theory.

The purpose of this ADR is therefore narrower than the first draft.

It defines how compiler-owned results are produced and legally consumed **while preserving the prior Contract meaning,
determinism, reality boundaries, and realization replaceability**.

Caching and incrementality are consequences of that architecture. They are not its reason for existence.

---

## 2. Decision

Kontrakt will use **producer-owned compiler result boundaries** for independently consumable compiler results.

A compiler result boundary exists when one compiler responsibility has completed a result sufficiently for another
responsibility to rely on its declared guarantee without reopening or reinterpreting the producer's hidden state.

This ADR does not create one universal `CompilerProduct` semantic object.

It does not make every temporary calculation a published product.

It does not create a new Contract authority.

It establishes the following order of responsibility:

```text
Declared Contract law
    ↓
legal semantic representation / observation
    ↓
compiler-owned derivation or transformation
    ↓
complete compiler result
    ↓
legal compiler consumption
    ↓
replaceable physical realization
```

The common laws are:

1. Contract authority is determined only by the owning Contract law.
2. Compiler consumption cannot create, widen, transfer, revoke, or merge Contract authority.
3. An upstream semantic producer must not change its meaning to accommodate a later compiler consumer.
4. A downstream compiler consumer must not reconstruct missing Contract meaning from source, host objects, cache state,
   physical indexes, or another implementation topology.
5. The same legal determinants must produce the same legal result at every deterministic boundary owned by that result.
6. A compiler result may derive new compiler knowledge, but that new knowledge belongs to the deriving compiler
   responsibility and does not retroactively become source Contract meaning.
7. Compiler inability, unavailable backing, unsupported product format, stale retained state, and corruption remain
   compiler reality. They do not fabricate Contract absence, refusal, Failure, or authority.
8. Physical split, fusion, scheduling, caching, persistence, repair, and representation remain replaceable when the same
   legal observations and required results are preserved.

---

## 3. Four Architecture Layers

The architecture must keep four layers conceptually distinct even when one implementation physically fuses them.

```text
┌──────────────────────────────────────────────┐
│ 1. DECLARED CONTRACT LAW                     │
│                                              │
│ Input / Admission / Canonicalization         │
│ Lowering / Fact / Invariant / State          │
│ Failure / Publication / Output               │
│ Version / Policy / Budget / Capacity         │
│ Governance / Diagnostic Evidence             │
│                                              │
│ Definition / Occurrence / Basis /            │
│ Applicability / authority-owned relations    │
└──────────────────────────────────────────────┘
                      │
                      │ represented without authority transfer
                      ▼
┌──────────────────────────────────────────────┐
│ 2. SEMANTIC MATERIAL AND OBSERVATION         │
│                                              │
│ Resolved Contract HIR                        │
│ HIR Semantic Protocol                        │
│ Established Material                         │
│ Established Semantic Protocol                │
│                                              │
│ complete / typed / explicit / coherent       │
│ observation surfaces                         │
└──────────────────────────────────────────────┘
                      │
                      │ consumed legally
                      ▼
┌──────────────────────────────────────────────┐
│ 3. COMPILER-DERIVED KNOWLEDGE AND RESULTS    │
│                                              │
│ realization knowledge                        │
│ CFG / SSA / analyses                         │
│ summaries                                    │
│ verification results                         │
│ generated-artifact plans                     │
│ execution formation                          │
│ optimization knowledge                       │
│ JVM planning                                 │
└──────────────────────────────────────────────┘
                      │
                      ▼
┌──────────────────────────────────────────────┐
│ 4. PHYSICAL REALIZATION                      │
│                                              │
│ queries / passes / caches                     │
│ slabs / tables / arenas / mmap               │
│ HID / fingerprints / Merkle / CAS            │
│ threads / epochs / RCU-like reclamation      │
│ persistence / red-green / delta repair       │
│ JVM backend structures                       │
└──────────────────────────────────────────────┘
```

Layer 2 is not Contract authority merely because it represents Contract meaning.

Layer 3 may derive useful facts about realization or compilation. Those facts do not become Contract obligations unless
a
separate owning Contract law declares them.

Layer 4 may replace the implementation of Layers 2 and 3. It cannot change their legal meaning.

A design statement that crosses these layers must say which layer owns each part.

---

## 4. Determinism First

### 4.1. Semantic Determinism

ADR-0063 already establishes the Contract law:

```text
same semantic basis
    ↓
same established meaning
```

This ADR preserves that law and adds a compiler-side consequence.

Compiler machinery cannot become an undeclared determinant of semantic meaning.

The following do not alter Contract meaning merely because they alter execution:

```text
worker count
worker completion order
query evaluation order
legal pass scheduling order
cache hit / miss
persistent product presence
physical row assignment
arena address
hash-table iteration order
slab partition
mmap placement
backend object identity
```

If one of these changes Contract meaning, the compiler implementation has leaked into the Contract.

### 4.2. Deterministic Compiler Results

A compiler-owned result may have its own explicit result-affecting inputs that are not Contract determinants.

Examples include an exact target architecture or an explicitly selected backend mode.

When a compiler result declares such inputs, the same declared inputs must produce the same consumer-visible result
where
that result is specified as deterministic.

This is separate from Contract semantic determinism.

```text
Contract determinism
    → same semantic determinants produce same Contract meaning

compiler-result determinism
    → same declared compiler inputs produce same declared compiler result
```

The second must never be used to redefine the first.

### 4.3. Physical Schedule Freedom

Determinism does not require one execution schedule.

```text
physical schedule may vary
consumer-visible deterministic result may not
```

Parallelism is permitted only when merge, ordering, identity formation, and visibility preserve the declared result.

### 4.4. Cache-Blind Correctness

Cold cache and warm cache must not define different legal meaning.

Deleting all reusable compiler state may increase work. It must not change what the current exact inputs mean.

A cache that decides whether a result is legal is authority leakage.

---

## 5. Explicit and Rich Meaning Before Compiler Convenience

Kontrakt does not obtain semantic richness by attaching more compiler metadata to products.

Rich Contract meaning means that the owning Contract law declares the real distinctions required by the machine.

For example, Input may own distinctions such as:

```text
presence / absence
presentation family
coordinate topology
form-defining extent
Presentation Sameness
observable order
uniqueness
exact semantic references
```

If such a distinction matters, it must be present in the owning Contract/HIR/Established surface according to that law.

A later optimizer, diagnostic subsystem, verifier, or backend cannot add the distinction merely because it would be
useful.

The converse is also important.

Compiler-only facts such as:

```text
query identity
cache key
slab address
fingerprint
reader epoch
analysis preservation state
cost estimate
codegen unit
```

are not Contract richness.

They remain compiler realization or compiler-derived knowledge unless an owning Contract independently declares the same
meaning for a different reason.

---

## 6. Contract and Implementation Separation

### 6.1. No Consumer-Driven Semantic Growth

An upstream semantic producer owns its meaning independently of downstream compiler consumers.

```text
Producer meaning
    ≠ verifier-specific meaning
    ≠ optimizer-specific meaning
    ≠ diagnostic-specific meaning
    ≠ backend-specific meaning
```

If a downstream subsystem requires semantic material that does not exist, the compiler must not invent that meaning in a
product layer.

The question returns to the owning Contract or semantic ADR.

### 6.2. Compiler Derivation Owns Only Its Derived Knowledge

A downstream compiler subsystem may derive a new result from legal semantic observations.

That result belongs to the compiler subsystem that derived it.

```text
Established Contract meaning
    ↓ legal observation
Analysis
    ↓
Derived Analysis Result
```

The Analysis Result may influence optimization, verification, or planning.

It does not become Established Contract meaning.

### 6.3. User Realization and Compiler Realization Are Both Non-Authority

Kontrakt has at least two realization axes.

```text
Declared Contract
├── User-System Realization
│      Operation implementation
│      explicit adapters / realization ports
│
└── Kontrakt Compiler Realization
       HIR storage
       Establishment executor
       query engine
       verifier
       optimizer
       cache
       incremental engine
       backend
```

Neither realization axis may define Contract meaning.

Compiler realization requires special discipline because it is maintained by Kontrakt itself and can therefore be
mistaken for authority.

### 6.4. Mechanism Names Must Stay Below the Contract

The following may be selected by compiler design:

```text
query
pass
analysis manager
red-green
Merkle validation
CAS
RCU-like reclamation
persistent slab
DB-style incremental maintenance
```

No owning Contract ADR should require these mechanisms merely to state its semantic law.

---

## 7. Reality Is Part of the Machine, Not an Exception to the Model

A real compiler and a real Contract machine operate under incomplete source, finite memory, unsupported features,
corrupted persistence, version mismatch, stale products, and resource limits.

Kontrakt must not hide those conditions by coercing them into semantic states they are not.

The following categories remain distinguishable where applicable:

```text
Contract-level
    valid semantic absence
    Contract refusal
    declared Failure
    established negative judgment

compiler-level
    not retained
    not yet physically materialized
    backing unavailable
    unsupported protocol or product revision
    stale retained result
    corrupt persisted bytes
    compiler unable to prove a required condition
    compiler resource exhaustion
    internal compiler defect
```

These categories may share diagnostics or recovery infrastructure. They do not share authority.

A required semantic observation that cannot currently be supplied does not become semantic absence.

A compiler failure to prove legality does not establish Contract rejection unless an owning Contract law explicitly
gives
that inability Contract meaning.

A cache miss is not Contract Failure.

Corrupt persisted bytes do not revoke a previously established semantic occurrence merely because its old backing cannot
be loaded.

ADR-0074 owns the common compiler unsuccessful-result and recovery representation. This ADR fixes only the separation
law
that prevents those results from being reinterpreted as Contract meaning.

---

## 8. HIR Producer Boundary

ADR-0071 remains the owner of the pre-authority semantic boundary.

The primary HIR subjects remain distinct:

```text
Contract Interface
Interaction
Operation
Definition Candidate
IDL Binding Candidate
```

The logical Establishment-facing HIR Protocol remains:

```text
Typed HIR Semantic Reference Domain
Definition Candidate Projection
IDL Binding Candidate Projection
Fine-Grained Semantic Projection
```

These projections expose already-resolved HIR meaning.

They do not establish Contract authority.

They do not expose physical storage as meaning.

They do not permit Establishment to reopen source, host objects, mutable producer state, query topology, or backing
containment to recover missing Candidate meaning.

This ADR does not add a second generic protocol above ADR-0071.

---

## 9. HIR-to-Establishment Handoff

The handoff must remain complete enough that Establishment can execute the owning law without a hidden frontend.

Every semantic determinant of an Established result must be traceable to one of the following:

```text
legal HIR Protocol observation
exact separately Established prerequisite
explicit authority-owned judgment input
```

The handoff must not contain later authority merely for compiler convenience.

```text
HIR Candidate
    ✕ must not pre-establish
Version Binding
Basis Binding
Applicability result
Established DefinitionRef
Established Occurrence result
```

Establishment creates only the meaning owned by the establishing authority.

It does not implicitly create:

```text
compiler reachability
optimization facts
diagnostic explanations
realization closure
consumer-specific semantic meaning
transitive semantic conclusions
```

This preserves ADR-0063 rather than replacing it.

---

## 10. Establishment and Established Semantic Observation

Successful Establishment produces authority-owned material according to the owning law.

The common categories remain:

```text
Established Definition Material
Established Occurrence Material, when the authority owns occurrence meaning
other authority-specific Established Material, only when an owning law requires it
```

Established Definition Material belongs to the Canonical Contract World according to ADR-0063.

Occurrence-specific material does not automatically become part of that world merely because a compiler consumer wants a
single aggregate store.

Downstream consumers observe Established meaning through the Established Semantic Protocol and its authority-owned
specializations.

Observation:

```text
≠ Establishment
≠ authority transfer
≠ outward Publication
```

This ADR does not introduce a universal `EstablishedMaterial` schema.

---

## 11. IDL Binding Discipline

HIR already distinguishes Definition Candidate meaning from IDL Binding Candidate selection meaning.

```text
Definition Candidate
    → what this resolved 1D Definition means

IDL Binding Candidate
    → which declared machine position selected which Candidate
```

This separation remains.

The current compiler architecture may need an authoritative established relation that connects an Interface,
Interaction,
Operation, role, or Policy World to an exact Established Definition.

This ADR does **not** invent that relation.

The owning Contract law must state:

```text
whether the relation is authoritative
who owns it
what determines it
when it becomes established
how Policy / Governance affect it
```

Only after that semantic ownership exists may compiler products consume the relation.

A query edge from an IDL Binding Candidate to a Definition Candidate cannot create the authoritative relation.

A generated host interface cannot create it either.

This is an explicit open dependency on the owning Interface / Governance / Policy binding law where current Accepted
ADRs
have not yet closed the exact post-HIR owner.

---

## 12. Compiler Result Boundary

A compiler result boundary is an architecture boundary, not a Contract category.

It is useful when an independently owned compiler responsibility produces a result that another responsibility may
consume
without depending on the producer's private mutable state.

A result boundary should state, where applicable:

```text
logical result subject
producer ownership
explicit result-affecting compiler inputs
legal upstream semantic observations
consumer-visible guarantee
legal read surface
result comparison law when reuse needs one
availability / unsuccessful-result boundary
information-retention or information-loss guarantee
```

These are **compiler architecture obligations**.

They do not become user-facing Contract obligations merely because this ADR requires the compiler to be explicit about
them.

Not every local value is a compiler result boundary.

Temporary calculations, scratch buffers, local worklists, local SSA construction state, or loop-local facts may remain
private when no independent consumer or reuse boundary requires exposure.

---

## 13. Legal Consumption

A compiler consumer may use only observations legally exposed by the producer level it consumes.

For Contract semantic material:

```text
Resolved HIR
    → ADR-0071 HIR Semantic Protocol

Established Contract Material
    → ADR-0063 Established Semantic Protocol
```

For compiler-derived results, the producer exposes an appropriate read-only result surface.

This ADR does not require every compiler result to define a named `Protocol` type.

The physical realization may be:

```text
primitive-array reads
typed slab ranges
columnar tables
immutable objects
bulk views
mmap regions
shared immutable backing
```

The logical rule is only that a consumer must not depend on private construction state or reconstruct upstream semantic
meaning from physical topology.

Consumption transfers neither Contract authority nor producer ownership.

---

## 14. Projection and Summary

A **projection** narrows already-existing producer meaning without adding a new judgment.

A **summary** is newly derived compiler knowledge.

```text
producer meaning
    ↓ select / restrict / re-index
producer-owned projection

producer meaning
    ↓ analysis / aggregation / closure / compression
summary producer
    ↓
summary result
```

This distinction prevents Whole-Machine optimization pressure from expanding HIR or Established Material into universal
consumer schemas.

If a global consumer needs only a compact result, it may consume a summary rather than full producer material.

The summary remains derived compiler knowledge.

It cannot become source Contract authority because many consumers use it.

The exact summary family, persistence policy, update strategy, and query key remain Design unless another ADR gives them
a
compiler architecture boundary of their own.

---

## 15. Information Preservation and Loss

Every semantic lowering or compiler transformation that intentionally loses information must respect ownership.

A producer may erase a distinction only after that distinction is no longer required by the legal consumers of that
producer level.

A later consumer must not reconstruct intentionally erased Contract meaning from:

```text
source spelling
source containment
host type shape
object adjacency
backend descriptor
storage layout
heuristic inference
```

If later work needs the distinction, one of the following must be true:

1. the distinction was required to remain in the upstream legal semantic observation;
2. a separately owned derived result preserved it before lowering; or
3. the later work does not actually require that Contract distinction.

Target lowering therefore remains delayed until Kontrakt has used the high-level Contract knowledge that only Kontrakt
possesses.

This does not require the JVM IR to preserve every upstream semantic distinction forever.

---

## 16. Derived Analysis and Transformation

Analysis and transformation belong to compiler realization.

An analysis result is compiler-derived knowledge over a declared subject.

A transform may invalidate knowledge that was true of the earlier representation.

Compiler architecture must therefore prevent stale derived knowledge from being silently consumed after its assumptions
no longer hold.

This ADR does not constitutionalize LLVM `PreservedAnalyses`, MLIR `AnalysisManager`, or any equivalent API.

A compiler realization may satisfy the requirement by:

```text
recomputing derived knowledge
updating it together with the transformation
proving a relevant result unchanged
invalidating and lazily rebuilding it
using a product-specific incremental repair
```

The Contract-level requirement remains smaller:

```text
compiler transformation
    must preserve the meaning it is obligated to preserve
```

Analysis invalidation is a compiler correctness mechanism used to achieve that requirement. It is not Contract law.

---

## 17. Explicit Compiler Inputs Without Semantic Leakage

Compiler results must not depend on ambient implementation state that silently changes their declared result.

For a compiler-owned result, legitimate result-affecting implementation inputs should be explicit at the compiler
boundary
that owns them.

Examples may include:

```text
exact backend target
explicit compiler feature mode
explicit toolchain capability
explicitly selected optimization mode, where output form may differ legally
persistent format revision
```

This is not a statement that these become Contract determinants.

They are compiler inputs to a compiler result.

Ambient state such as current wall clock, filesystem enumeration order, process-global locale, random seed, mutable
singleton state, or cache presence must not silently change deterministic compiler output unless a higher architecture
explicitly declares and owns that variability.

This follows the same engineering lesson as reproducible build systems: hidden environmental inputs create accidental
non-determinism.

---

## 18. Visibility and Coherent Observation

An independently consumed result must not expose half-formed state as a successful current result.

Conceptually:

```text
private formation
    ↓
producer checks
    ↓
complete visible result
    ↓
ordinary consumers
```

This is compiler visibility, not ADR-0058 Publication authority.

One consumer computation must observe mutually compatible inputs.

Compatibility does not require every input to have the same numeric generation identifier.

It requires that the combination be legal for the current computation.

```text
same generation number
    ≠ sufficient compatibility proof

old physical backing
    ≠ automatically stale semantic meaning
```

Physical visibility and memory reclamation remain implementation questions.

RCU-like reader pinning, immutable generations, copy-on-write, arena generations, versioned tables, snapshots, reference
counting, or another mechanism may implement the invariant.

The mechanism does not become semantic lifetime.

---

## 19. Equality, Validity, and Reuse Must Not Collapse

Three questions must remain distinct.

```text
What does this result mean?
Is the retained old result usable for the current request?
Can a cheap physical comparison avoid more work?
```

For semantic products, equality remains owned by the semantic producer and the owning Contract law.

For compiler-derived products, the compiler producer may define a result comparison useful for its own result family.

That compiler comparison cannot redefine upstream Contract equality.

A retained representation may be unusable because its codec, backend, target, or compiler compatibility no longer holds
even when clean recomputation would produce equal semantic meaning.

Conversely, a physical hash match is only evidence. It is not semantic authority by itself.

```text
semantic equality
    ≠ compiler-result equality
    ≠ current reuse eligibility
    ≠ byte equality
    ≠ fingerprint equality
    ≠ object identity
```

---

## 20. Dependency Recording Is Compiler Machinery

Compiler dependency tracking exists to avoid unnecessary work and to preserve correctness under reuse.

It does not define Contract dependency.

The following remain distinct:

```text
Contract semantic prerequisite / Basis relation
pre-authority HIR semantic relation
compiler derivation input
observed compiler computation dependency
build / artifact dependency
provenance relation
physical reachability
```

A previous dynamic dependency trace records what one previous computation observed.

It is not universal semantic law.

In branch-dependent computation, a changed earlier dependency may alter which later dependencies are discovered.

Therefore an incremental engine must validate dependency structure soundly rather than assuming that the old trace
defines
all future legal reads.

The exact representation of dependency edges, dependency ordering, query nodes, invalidation marks, red/green state, or
change frontiers is Design.

---

## 21. Cache, Persistence, and Content Addressing

Cache is work avoidance.

Persistence is retained representation.

Content addressing is lookup and integrity machinery.

None creates Contract authority.

None creates semantic identity by existence alone.

None makes stale material current merely because bytes can be found.

The compiler may use:

```text
HID
BLAKE3
fingerprints
Merkle summaries
CAS
persistent tables
serialized products
```

to accelerate lookup, comparison, corruption detection, deduplication, and validation routing.

The owning semantic or compiler-result law remains responsible for deciding what counts as current legal meaning.

A cache miss or unreadable persisted result returns the compiler to another legal path. It does not change the Contract.

---

## 22. Incremental Compilation Follows the Architecture; It Does Not Define It

V1 must not be architected as a non-incremental compiler whose only future seam is `cache key -> value`.

It also must not constitutionalize one incremental engine.

The V1 requirement is smaller and more durable.

Reusable major results should expose enough stable architecture that a future engine can determine:

```text
what result is being discussed
what legal semantic observations it depends on
what explicit compiler inputs affect it
what result comparison is legal for that family
what unsuccessful / unavailable state prevents reuse
how clean recomputation remains possible
```

V2 may implement persistence and incremental repair with different strategies for different product families.

Possible mechanisms include:

```text
red-green query reevaluation
dynamic dependency repair
summary-level change cutoff
Merkle-assisted validation
delta maintenance
incremental data-flow analysis
persistent segmented products
adaptive repair versus rebuild
```

No one strategy becomes Contract law.

The clean path remains semantically sufficient when retained state is absent.

---

## 23. Early Cutoff Is a Derived Optimization

A compiler may stop change propagation when the exact result observed by downstream consumers has been re-established as
unchanged under the result owner’s legal comparison.

This is an optimization of compiler work.

It is not a semantic rule that an upstream source change is irrelevant.

The legal reasoning is:

```text
upstream changed
    ↓
old downstream assumption cannot be trusted automatically
    ↓
validate / recompute / repair the owning result
    ↓
consumer-visible owned result unchanged
    ↓
consumers that depend only on that owned result need not be recomputed
```

A fingerprint may accelerate this proof only under a sound producer-owned validation scheme.

A hash collision must not be allowed to redefine Contract equality.

---

## 24. Generated APIs and Backend Products

Generated external interfaces and Operation realization interfaces are downstream artifacts.

They are not sources of Contract authority.

The Contract requirement is that they preserve the declared surface they are required to represent.

This ADR does not require one fixed direct dependency path such as:

```text
Established Semantic Protocol
    → Generated API generator
```

The compiler may introduce a derived API plan, fuse planning with generation, or reuse a validated artifact plan.

Those are compiler realization choices.

What remains forbidden is reconstructing Contract authority from the generated artifact after the authoritative semantic
material already exists.

The same rule applies to the backend.

Backend structures may consume legally derived execution material. They must not reinterpret generated JVM object shape
as
new Contract meaning.

---

## 25. Whole-Machine and Summary-Driven Work

Whole-Machine work must not force every global consumer to materialize every full local body when a sufficient owned
summary can answer the question.

This is a scale principle, not a new Contract relation.

A summary producer owns its derived compiler result.

The global consumer may depend on the summary rather than the full body.

If the summary remains unchanged after a local recomputation, propagation may stop for consumers whose only legal input
is
that summary.

The compiler must not make the summary authoritative merely because it becomes a convenient global index.

If a Whole-Machine Contract authority establishes new semantic meaning, that authority remains separate and must be
owned
by the relevant Contract law.

---

## 26. Resource Reality

Compiler work is finite.

A correct semantic design does not justify unbounded compiler memory, stack use, serialization size, graph traversal, or
analysis work.

Compiler resource governance is implementation/runtime reality and must be explicit enough to avoid accidental denial of
service or hidden failure modes.

This ADR does not turn compiler memory limits into Contract Budget or Capacity unless an owning Contract explicitly
makes
them part of the user machine.

The distinction is:

```text
Contract Budget / Capacity
    → declared machine obligation

compiler resource limit
    → realization safety / operability constraint
```

Both should be explicit in their own layer.

Exhausting a compiler resource limit must not silently change Contract meaning.

The compiler may fail, degrade tooling, abandon an optimization, switch to a clean or conservative path, or return an
explicit unsuccessful compiler result according to ADR-0074 and subsystem law.

---

## 27. Query and Pass Orchestration Are Replaceable

V1 may use query-oriented orchestration for major products and expensive reusable derived results.

Passes may remain useful inside a producer computation for local ordered transformation.

Neither `query` nor `pass` is a Contract concept.

```text
query graph
    → compiler computation topology

pass order
    → compiler realization schedule

neither
    → Contract authority graph
```

A later architecture may replace query orchestration, change query granularity, or move work into a persistent
relational
engine if it preserves the same semantic and compiler-result laws.

---

## 28. Physical Representation Freedom

A logical semantic or compiler result boundary does not require one JVM object.

Legal implementations include:

```text
primitive arrays
typed slabs
columnar tables
immutable object graphs
arena-backed ranges
side tables
compressed columns
segmented persistent slabs
mmap pages
content-addressed chunks
mixed representations
```

Physical split and fusion follow measured access patterns and lifetime needs.

They must preserve logical ownership and legal observation.

The first layout does not become architecture law merely because implementation started there.

---

## 29. V1 Requirements

V1 must close the following compiler architecture seams.

```text
1. Visible HIR remains a complete deterministic pre-authority semantic boundary.
2. Establishment consumes legal HIR observations and exact authoritative prerequisites only.
3. Established semantic material is consumed through ADR-0063-owned observation surfaces.
4. Major independently consumed compiler results have explicit ownership and complete consumer-visible guarantees.
5. Derived compiler knowledge cannot become Contract authority by reuse or convenience.
6. Hidden compiler state cannot change deterministic semantic or declared compiler results.
7. Successful visible results are coherent and ordinary consumers do not observe incomplete construction state.
8. Compiler unsuccessful / unavailable states remain distinct from Contract negative meaning.
9. Clean recomputation remains correct without cache or persistence.
10. Query / storage / representation choices remain replaceable.
11. Verification can compare optimized or reused paths against an independent or clean reference where required.
12. The exact authoritative IDL / Interface binding owner is not invented by compiler architecture and remains an explicit
    semantic closure item where still open.
```

V1 does not need to persist every product or incrementalize every computation.

---

## 30. V2 Evolution

V2 may add persistent and incremental realization behind the same boundaries.

Candidate work includes:

```text
persistent HIR projections
persistent Established-derived compiler products
cross-session result lookup
selective dependency validation
red-green-like early cutoff
persistent summaries
incremental analysis
incremental Whole-Machine repair
adaptive repair versus rebuild
incremental backend artifact reuse
reader pinning / generation reclamation
```

V2 must not require Contract ADRs to change merely because the compiler chooses a stronger reuse engine.

A V2 engine may change its storage, dependency model, scheduling policy, and repair strategy while preserving the same
legal semantic observations.

---

## 31. Adversarial Review

The architecture must be tested against at least the following failures.

### 31.1. Implementation Becomes Authority

```text
cache hit
    → treated as proof of Contract validity
```

Rejected.

### 31.2. Consumer Creates Missing Contract Meaning

```text
backend needs field X
    → adds X to Established Material semantics
```

Rejected.

Return to the owning Contract law if X is truly semantic.

### 31.3. HIR Pre-Establishes Authority

```text
HIR formation computes Applicability result
because Establishment would be easier
```

Rejected unless HIR is only representing meaning already owned as pre-authority material and the actual authority
remains
later.

### 31.4. Compiler Failure Masquerades as Contract Failure

```text
persistent bytes corrupt
    → Contract Failure
```

Rejected.

### 31.5. Hidden Environment Changes Result

```text
same declared inputs
+ different filesystem iteration order
    → different semantic result
```

Rejected.

### 31.6. Generated Artifact Becomes Source of Truth

```text
generated Kotlin signature
    → reconstruct Contract identity
```

Rejected.

### 31.7. Old Dependency Trace Becomes Semantic Law

```text
previous query read-set
    → assumed complete future semantic dependency
```

Rejected.

### 31.8. Stale Derived Knowledge Survives Transformation

```text
IR changed
analysis assumption invalid
analysis reused anyway
```

Rejected as compiler correctness failure.

### 31.9. Summary Becomes Authority

```text
Whole-Machine Summary
    → canonical Contract meaning
```

Rejected.

### 31.10. Resource Exhaustion Changes Meaning

```text
compiler budget exhausted
    → silently omit semantic member
```

Rejected.

Return an explicit compiler unsuccessful result or use a legal fallback.

### 31.11. Physical Co-Location Creates Identity

```text
same slab row / same CAS object
    → same semantic entity
```

Rejected.

### 31.12. Consumer-Specific Equality Rewrites Producer Meaning

```text
optimizer only cares about field A
    → declares full Established Definition equal when field B changed
```

Rejected.

Use a producer-owned legal projection or a separately owned derived summary.

---

## 32. Verification and QA Requirements

This ADR requires verification at the boundaries it protects, without making the verification machinery Contract
authority.

The compiler test strategy should include:

```text
clean path vs reused path
cold cache vs warm cache
single-thread vs parallel formation
legal scheduling variations
persistent reload vs clean recomputation
representation A vs representation B
optimized path vs reference path
valid current product vs stale product
corrupt persisted product rejection
unsupported product revision rejection
hidden-environment perturbation
hash / fingerprint adversarial checks where used
```

The invariant is not that every physical byte is identical unless a product explicitly requires byte reproducibility.

The invariant is that the relevant legal observation or declared deterministic compiler result is identical.

For final artifacts where reproducibility is itself required, the backend must additionally remove or explicitly own
nondeterministic inputs such as timestamps, unstable traversal order, or build-path leakage.

---

## 33. Non-Normative Engineering Basis

The following systems support individual engineering principles. None defines Kontrakt Contract semantics.

### 33.1. LLVM New Pass Manager

LLVM caches analysis results and invalidates them when transforms no longer preserve their assumptions. A transform may
update an analysis or report the analyses it preserves. This supports the compiler-side law that stale derived knowledge
must not be silently consumed.

Source: <https://llvm.org/docs/NewPassManager.html>

### 33.2. MLIR Pass Infrastructure

MLIR analyses are separate from transformations, are cached, and are assumed invalidated unless preserved. Pass failure
is
also an explicit compiler state. This is useful evidence for keeping derived knowledge ownership and compiler failure
separate from domain semantics.

Source: <https://mlir.llvm.org/docs/PassManagement/>

### 33.3. rustc Query and Incremental Compilation

rustc incremental compilation relies on deterministic query results. If all recorded inputs are unchanged, a query
result
must be unchanged or the compiler is not deterministic. Its red-green algorithm is therefore an optimization built on a
prior determinism assumption, not a replacement for it.

rustc also records dependency order because an earlier changed dependency can alter which later dependencies are read.
This is evidence against treating a previous dynamic read-set as universal future semantic law.

Sources:

- <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation.html>
- <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation-in-detail.html>

### 33.4. LLVM ThinLTO

ThinLTO demonstrates that wider-scope work can consume compact summaries instead of forcing every consumer to
materialize
full local IR. The summary is derived optimization material, not source semantic authority.

Source: <https://clang.llvm.org/docs/ThinLTO.html>

### 33.5. Reproducible Builds and Nix

Reproducible-build systems show why ambient time, filesystem ordering, and uncontrolled environment state must not
silently
enter deterministic build results. Nix derivations similarly model reproducible builds from explicit build inputs.

These systems do not define Contract semantics. They are evidence for explicit compiler-input boundaries and cache-blind
correctness.

Sources:

- <https://reproducible-builds.org/docs/deterministic-build-systems/>
- <https://reproducible-builds.org/docs/timestamps/>
- <https://wiki.nixos.org/wiki/Derivations>

### 33.6. Linux RCU and RocksDB Snapshots

RCU separates replacement from reclamation so readers do not observe partially updated references and old storage is not
reclaimed before readers stop using it. RocksDB snapshots provide explicit point-in-time read views.

These are physical-coherence references only. Kontrakt does not make RCU epochs or RocksDB sequence numbers semantic
identity.

Sources:

- <https://docs.kernel.org/RCU/whatisRCU.html>
- <https://github.com/facebook/rocksdb/wiki/Snapshot>

### 33.7. DBSP and Enzyme

Database incremental-view-maintenance work shows that some derived computations can support principled delta
maintenance.
DBSP gives a formal incrementalization model for suitable computations. Enzyme shows production systems may choose among
refresh strategies according to cost.

Kontrakt therefore keeps the incremental algorithm below the semantic boundary and permits product-specific strategies.

Sources:

- DBSP: <https://www.vldb.org/pvldb/vol16/p1601-budiu.pdf>
- Enzyme: <https://arxiv.org/abs/2603.27775>

### 33.8. Verified Compilation and Reproducibility Research

Recent verified-compiler work remains useful as evidence that implementation paths can be checked against independently
specified semantics rather than treated as semantics themselves. Reproducible-build research likewise treats build
repetition as evidence about the integrity of the implementation pipeline rather than as the definition of source
meaning.

Examples:

- *Verified VCG and Verified Compiler for Dafny* (2025): <https://arxiv.org/abs/2512.05262>
- *Verifiable Provenance of Software Artifacts with Zero-Knowledge Compilation* (2026):
  <https://arxiv.org/abs/2602.11887>

These are research references, not architecture templates.

---

## 34. Rejected Designs

### 34.1. Compiler Product Graph as Contract Graph

Rejected.

Compiler computation dependencies may be derived from Contract relations but cannot establish them.

### 34.2. One Universal Product Schema

Rejected.

HIR Candidate meaning, Established Contract meaning, analysis result, summary, verification result, and backend artifact
do
not gain one common semantic field set merely because a compiler stores them.

### 34.3. Query as Authority

Rejected.

Query evaluates work. The owning law determines meaning.

### 34.4. Cache as Validity

Rejected.

Cache presence avoids work only after current-validity requirements are satisfied.

### 34.5. Fingerprint as Semantic Equality

Rejected.

Fingerprint may provide evidence under a legal comparison law. It does not own semantic equality.

### 34.6. Compiler Unavailability as Semantic Absence

Rejected.

Unsupported, unavailable, stale, corrupt, and absent are different states.

### 34.7. Analysis API as Contract Law

Rejected.

LLVM-style or MLIR-style invalidation APIs are useful realization references but remain replaceable compiler machinery.

### 34.8. Generated API as Authority

Rejected.

Generated artifacts represent and realize already-declared meaning.

### 34.9. Full Source Reopening Downstream

Rejected.

If a legal downstream semantic consumer must reopen source to recover required meaning, the upstream semantic boundary
is
incomplete or the ownership decision is wrong.

### 34.10. First Physical Layout as Permanent Architecture

Rejected.

Primitive slabs, objects, mmap, or CAS may be replaced without Contract evolution when legal observations remain the
same.

---

## 35. Consequences

### 35.1. Positive

The architecture keeps Contract authority above compiler machinery.

Determinism becomes a prerequisite for reuse rather than a side effect of a cache design.

HIR and Established Semantic Protocols remain narrow semantic observation boundaries instead of becoming universal
compiler APIs.

Compiler-derived knowledge gains explicit ownership without becoming a second semantic world.

Compiler failure and resource reality can be represented honestly without fabricating Contract Failure or absence.

V1 can use practical query and in-memory techniques while leaving V2 free to adopt stronger persistent and incremental
algorithms.

Whole-Machine scaling can use summaries without moving authority into summary indexes.

Backend replacement remains possible because target representation does not define Contract meaning.

### 35.2. Costs

Compiler subsystems must state ownership and result guarantees more explicitly.

Some apparently convenient direct reads from source or global compiler context become illegal architectural shortcuts.

Incremental engines need sound dependency and validity design rather than treating cache presence as correctness.

Verification must test alternate physical paths against the same legal observations.

The open authoritative IDL binding ownership question cannot be hidden inside compiler plumbing and must be closed by
the
semantic owner.

---

## 36. Required Follow-Up

After this ADR is reviewed, the next work should be separated by layer.

### 36.1. Contract / Establishment Follow-Up

```text
Close the exact authoritative IDL / Interface slot-binding owner.
Continue 1D-specific HIR → Establishment closure using the master checklist.
Do not move compiler dependency or cache fields into 1D semantic meaning.
```

### 36.2. Compiler Architecture Follow-Up

```text
Map major producer/result owners from HIR through JVM backend.
For each major result, state consumer-visible guarantee and legal upstream inputs.
Mark derived knowledge explicitly as non-Contract.
Define common compiler-result availability / unsuccessful-result integration with ADR-0074.
```

### 36.3. Design Follow-Up

```text
Choose V1 query boundaries.
Choose primitive/table/slab storage shapes.
Choose analysis preservation / invalidation implementation.
Choose generation / reader lifetime mechanism.
Choose cache / fingerprint / HID evidence strategy.
Measure summary granularity and split / fuse decisions.
```

### 36.4. V2 Research Follow-Up

```text
persistent products
dependency recording
selective invalidation
early cutoff
incremental analysis
summary repair
delta maintenance
adaptive repair versus rebuild
persistent backend reuse
```

---

## 37. Summary

Kontrakt does not introduce compiler result boundaries in order to make caching convenient.

It introduces them so that a large compiler can consume already-resolved meaning without recreating authority, hidden
semantics, or implementation coupling.

The governing order is:

```text
explicit declared Contract
    ↓
complete legal semantic observation
    ↓
consumer-owned compiler derivation
    ↓
deterministic result
    ↓
replaceable realization
```

Caching, persistence, fingerprints, dependency graphs, incremental repair, summaries, and backend specialization may
avoid
work beneath that structure.

They do not define the structure.

The machine remains explicit about what it promises, explicit about what it knows, explicit about what failed, explicit
about what it cannot currently provide, and explicit about which layer owns each fact.

That separation is the precondition for aggressive optimization rather than an obstacle to it.