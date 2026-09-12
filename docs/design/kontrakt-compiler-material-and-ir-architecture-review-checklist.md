# Design Note: Kontrakt Compiler Material and IR Architecture Review Checklist

- Status: Draft
- Date: 2026-09-12
- Owner: Kontrakt Compiler Architecture
- Scope: Shared review criteria for compiler-semantic material, IRs, published compiler products, and derived compiler
  products
- Non-Goal: Defining Contract semantics, fixing physical schemas, fixing one query engine, or fixing one incremental
  algorithm

---

## 1. Purpose

This document is a reusable architecture checklist for creating or materially changing compiler material in Kontrakt.

It applies to material such as:

```text
Resolved Contract HIR
Realization Body IR
Contract-Aware Execution IR
JVM Plan / IR
Canonical Contract World projections
analysis products
verification overlays
summaries
indexes
query / product results
persistent compiler products
```

The checklist exists to prevent two opposite failures.

```text
Under-specified material
    → downstream subsystems re-infer meaning
    → duplicated work
    → accidental authority
    → weak reuse boundaries

Over-coupled material
    → consumer-specific fields enter the producer
    → query / cache / manager state enters semantics
    → representation becomes architecture
    → replacement becomes expensive
```

The target is:

```text
complete semantic information
+
replaceable compiler realization
```

A compiler material should expose enough meaning for valid consumers.

It should not absorb the implementation topology of those consumers.

---

## 2. Authority Boundary

This document does not establish new Contract law.

Authority order remains:

```text
What Contract Is
    ↓
Accepted / current ADR
    ↓
compiler architecture decisions
    ↓
design / TODO material
    ↓
implementation
```

If a compiler consumer needs semantic information that does not exist, first ask:

```text
Does the missing information belong to Contract meaning?
```

If yes, return to the owning Contract authority or ADR.

Do not invent the meaning in an IR, verifier, optimizer, query engine, cache, summary, or backend.

If the information is compiler-derived, keep it outside Contract authority.

---

## 3. Core Review Rule

Every compiler material should be reviewed in three layers.

```text
Semantic obligation
    ↓
Observable compiler surface
    ↓
Replaceable realization
```

The first two layers define what the compiler material means and what consumers may rely on.

The last layer defines how the material is stored, scheduled, cached, published, or repaired.

Do not reverse this order.

Bad direction:

```text
primitive slab exists
    ↓
therefore this field is semantic
```

Correct direction:

```text
semantic distinction exists
    ↓
compiler may represent it with a slab, table, object, overlay, or another mechanism
```

---

## 4. When to Use This Checklist

Use this checklist when:

```text
a new IR level is introduced
an existing IR changes semantic vocabulary
an existing IR changes equality or identity rules
a published compiler product becomes shared by new consumers
a summary or overlay becomes a stable cross-subsystem boundary
a query product becomes persistent or reusable
a material gains an incremental / V2 role
a physical rewrite risks changing observable compiler meaning
```

Do not use the checklist mechanically as a required section list in every document.

Not every item applies to every material.

A review should mark an item as one of:

```text
Applicable
Not owned here
Derived elsewhere
Implementation only
Open by design
```

---

# Part I. Generic Compiler Material Review

## 5. Purpose and Stage Invariant

State why the material exists.

State what becomes true when the material is valid.

A stage should exist because a new invariant becomes true.

A new class name, cache entry, generation, freeze operation, or pass completion is not enough.

Review questions:

```text
What is the input material?
What is the output material?
What new invariant does the output guarantee?
Can the next consumer rely on that invariant without reopening an earlier stage?
```

If no persistent invariant changes, a new logical material level may not be justified.

---

## 6. Semantic Vocabulary

List the semantic concepts represented by the material.

Do not begin with fields or classes.

Review questions:

```text
What meaning can this material express?
What meaning can it no longer express?
Which distinctions are semantically observable?
Which distinctions are only representation choices?
```

A material must not silently collapse distinctions required later.

---

## 7. Admission Invariant

Define what may enter the material.

Examples:

```text
Resolved Contract HIR
    → required frontend references are exact

JVM Plan / IR
    → target legality information is available
```

Invalid or incomplete input must not become valid material merely because storage was allocated.

---

## 8. Producer Obligation

Define what the producer must establish before publication.

The producer obligation should be independent of one physical implementation.

Review questions:

```text
What must the producer prove or validate?
What may remain unknown?
What failure prevents publication?
```

Producer success is a semantic or compiler invariant.

It is not a cache hit, builder completion, or table insertion event.

---

## 9. Consumer Sufficiency

Review downstream consumers before freezing the material contract.

For each consumer, ask:

```text
What exact information does the consumer need?
Does that information belong to this material's meaning?
Can the consumer derive it without reopening source or an earlier semantic stage?
```

If the information belongs here, expose it.

If it is derived knowledge, keep it as a derived product.

If it is consumer-specific convenience, do not add it to the producer merely to avoid one lookup.

The goal is:

```text
producer-independent
+
consumer-aware
```

---

## 10. Observable Surface

Define what consumers are allowed to depend on.

The observable surface is narrower than the full implementation.

Consumers should not depend on:

```text
backing-table order
object identity
memory address
builder topology
hash-map iteration order
worker completion order
private indexing structures
```

A private physical change should remain possible when the observable meaning is unchanged.

---

## 11. Semantic Identity and Equality

Define semantic equality separately from physical identity.

Keep at least these concepts distinct where applicable:

```text
source identity
source provenance identity
compiler semantic identity
Contract semantic identity
IR semantic equality
product identity
fingerprint / HID
generation identity
dense ordinal
table row
memory address
```

Review questions:

```text
What changes semantic meaning?
What changes only provenance?
What changes only physical representation?
What equality relation is valid for downstream early cutoff?
```

A fingerprint may provide equality evidence.

It is not semantic authority by itself.

---

## 12. Stable Reference Surface

Define how a consumer can refer to a semantic unit without depending on physical layout.

A stable reference surface may be needed for:

```text
analysis targets
diagnostics
query products
summaries
cross-product relations
persistent reuse
```

The semantic reference and its physical encoding remain separate.

---

## 13. Scope and Granularity

Define the logical units that may be independently consumed.

Possible examples include:

```text
definition
interaction
operation realization
method
core
whole-machine
module
published generation
```

Do not assume that physical aggregation requires one dependency unit.

A large material may expose smaller stable projections.

---

## 14. Projection Surface

Define whether useful stable projections can be derived from the material.

Example:

```text
HIR Generation G
    ├── Definition A projection
    ├── Definition B projection
    └── Provenance projection
```

A projection may act as a change-propagation firewall.

Projection does not create new Contract meaning.

Projection does not automatically create a new IR level.

The exact query API remains implementation-specific.

---

## 15. Determinant Set

Define the semantic inputs that determine the material or one of its stable projections.

This is not a cache-key definition.

It is a semantic truth used later to design correct query and reuse keys.

Review questions:

```text
Which inputs can change the result?
Which inputs cannot change the result?
Which context is actually relevant?
Which globally available context must not become an accidental determinant?
```

Worker order, cache warmth, memory address, and compiler scheduling are not semantic determinants.

---

## 16. Reuse Equivalence and Early Cutoff

Define when a recomputed result is semantically unchanged for downstream consumers.

This creates a seam for:

```text
V1 in-memory reuse
V2 persistent reuse
red-green style cutoff
projection cutoff
content-addressed storage
domain-local incremental repair
```

Do not fix one mechanism in the material contract.

The material should only expose enough semantic equality to make correct mechanisms possible.

---

## 17. Dependency Observation Surface

A query or incremental engine may need to observe which compiler products or projections were consumed.

Design the material so dependency observation can occur at semantic units.

Avoid making physical storage reads the dependency model.

Bad dependency unit:

```text
table row 182
object field read
slab offset 4096
```

Better dependency unit:

```text
Resolved Definition A
Verification Summary for Core B
JVM Method Plan for Method C
```

Always preserve:

```text
compiler query dependency
    ≠ Contract semantic dependency
```

---

## 18. Derived Knowledge Separation

List knowledge that can be recomputed from the material.

Examples:

```text
CFG properties
call graph
SCC summary
effect summary
verification result
optimization score
whole-machine summary
query dependency graph
diagnostic presentation
```

Derived knowledge should not become part of the source material merely because several consumers use it.

Shared derived knowledge may have its own published product contract.

---

## 19. Analysis Compatibility

The material should support analysis without making one Analysis Manager architecture mandatory.

Review questions:

```text
Can an analysis target be identified exactly?
Can analysis scope be stated explicitly?
Can an analysis result name its determining input generation or projection?
Can validity be checked after transformation?
Can expensive analysis be shared while valid?
```

The exact Analysis Manager API remains open.

---

## 20. Query and Product Compatibility

The material should support query-oriented orchestration without becoming a query object.

A stable compiler product should be able to describe:

```text
product identity
explicit semantic inputs
published result
dependency observation boundary
generation / validity context
```

The query graph is compiler infrastructure.

It is not the material's semantic graph.

---

## 21. Pass and Transform Compatibility

Define what kinds of transformation are allowed at this material level.

Every legal transform needs:

```text
precondition
meaning-preservation relation
result invariant
analysis preservation / invalidation consequence
```

The exact pass order remains open unless order itself is required for correctness.

A Pass Manager is an orchestration mechanism.

It does not own the semantic meaning of the material.

---

## 22. Validation and Independent Checking

Define which invariants can be checked independently.

Possible mechanisms include:

```text
IR verifier
local validator
translation validation
reference comparison
differential execution
metamorphic testing
```

A validator should not merely repeat the transform's own internal assumption.

Heavy checking may be configurable.

The invariant is not optional.

---

## 23. Publication Boundary

Define when construction becomes observable to independent consumers.

Preferred logical pattern:

```text
private construction
    ↓
producer validation
    ↓
publish
    ↓
read-only consumption
```

Publication does not require a full physical copy.

Publication does not create Contract authority.

The exact mechanism may be:

```text
immutable backing
sealed table
snapshot
copy-on-write generation
phase-qualified material
overlay
another equivalent realization
```

---

## 24. Generation and Snapshot Coherence

Define whether consumers require one coherent generation.

A consumer must not accidentally observe an invalid mixture such as:

```text
Definition A from generation G+1
Definition B from generation G
Index from a half-built generation
```

Generation identity is a compiler validity boundary.

It is not Contract Version.

---

## 25. Concurrency and Mutation Ownership

Define who may mutate material and when.

Prefer:

```text
worker-local mutation
immutable published material
explicit deterministic merge
```

Parallel scheduling must not determine semantic output.

A future parallel compiler should not require retrofitting ownership into globally mutable shared material.

---

## 26. Cancellation and Partial Work

Define what happens when computation is cancelled or fails.

A cancelled computation must not expose partially built material as a successful product.

Temporary work may be abandoned.

Published validity must remain explicit.

---

## 27. Provenance Separation

Separate semantic meaning from source or operational provenance.

Possible provenance includes:

```text
source file
source span
authored spelling
origin chain
generated-source origin
implementation bytecode location
```

A provenance-only change should be able to leave semantic material unchanged when the producer can establish that
equality.

Diagnostic presentation is not semantic identity.

---

## 28. Diagnostic and Evidence Surface

Define what semantic subject diagnostics can refer to.

Do not store rendered diagnostic strings in semantic hot material merely because diagnostics are a consumer.

A diagnostic product may join:

```text
semantic reference
provenance reference
structured arguments
related evidence
```

The message renderer remains separate.

---

## 29. Summary Derivation Surface

Determine whether wider consumers can use summaries instead of opening full material.

A summary should be designed from a consumer question.

```text
consumer question
    ↓
minimum sufficient summary
```

Do not create a summary that is merely a second full IR.

A summary is derived knowledge unless an owning Contract explicitly says otherwise.

---

## 30. Persistence and Serialization Seam

Determine whether the material may become a persistent compiler product.

Do not fix a binary format in the semantic contract.

Keep these separate:

```text
semantic meaning
compiler product schema
serialization format
fingerprint algorithm
Contract Version
frontend language version
```

Persistent state must remain deletable without changing Contract meaning.

---

## 31. Cross-Session Reuse Seam

If cross-session reuse may matter, verify that result identity is not tied to:

```text
object address
worker id
parse order
session-local ordinal
source line number
mutable global state
```

A persistent reuse mechanism should be able to validate compatibility explicitly.

Cold recomputation remains the correctness fallback.

---

## 32. Incremental / V2 Granularity

Define which semantic units may change independently.

Do not define the final incremental algorithm here.

Useful separations may include:

```text
semantic meaning
provenance
binding
context
body
summary
version
source presentation
```

The architecture should permit different repair strategies in different domains.

Possible V2 mechanisms include:

```text
pull validation
push invalidation
change-frontier propagation
delta maintenance
specialized dynamic graph repair
lazy rebuilding
full recomputation
hybrid scheduling
```

No one mechanism becomes compiler-wide semantic law.

---

## 33. Full-Recompute Fallback

Incremental state must not become the only way to obtain a correct result.

A clean recomputation path should remain conceptually valid.

This provides:

```text
correctness fallback
differential validation
corruption recovery
incremental-equivalence testing
```

Incremental execution is work avoidance.

It is not semantic authority.

---

## 34. Information-Loss Boundary

State what information may be discarded at this material boundary.

Review the next consumers before discarding information.

Ask:

```text
What is no longer needed?
What must survive to a later stage?
What would be expensive or impossible to reconstruct?
```

Progressive lowering should remove abstraction only after the last consumer that needs it.

---

## 35. Target Leakage Boundary

For non-target IRs, identify target-specific information that must not leak upward.

For target IRs, identify which higher-level semantic information has already been intentionally consumed.

Do not lower to JVM or machine vocabulary merely because the backend will eventually need it.

---

## 36. Resource and Lifetime Boundary

Determine the logical lifetime of the material.

Possible lifetimes include:

```text
construction-local
query-local
worker-local
compilation generation
cross-product shared
cross-session persistent
```

Do not keep large source or analysis material alive only because one unrelated consumer needs a small relation.

Lifetime policy remains implementation unless it changes observable validity.

---

## 37. Old-Generation Reclamation

If multiple published generations may coexist, publication and reclamation must remain separate concerns.

A new generation becoming visible does not imply that an older generation is immediately reclaimable.

The exact mechanism is open.

Possible realizations include epoch-based reclamation, reference tracking, generation pinning, or another safe scheme.

---

## 38. Determinism

The same explicit valid inputs must produce the same semantic result regardless of:

```text
worker scheduling
cache state
allocation order
filesystem enumeration order
hash-table iteration order
parallel completion order
incremental vs clean recomputation
```

Where output ordering is observable, define a deterministic ordering rule.

---

## 39. Representation Freedom

End every material review by restating the representation boundary.

Logical distinctions do not require one object, table, product, or allocation each.

The compiler may:

```text
flatten
fuse
split
intern
deduplicate
co-locate
compact
use overlays
use primitive slabs
use tables
use persistent structures
use lazy materialization
```

when the material contract remains satisfied.

---

# Part II. IR-Specific Review

## 40. IR Level Justification

For an IR, explicitly justify why it is a distinct IR level.

A new IR level is usually justified when one of these changes:

```text
semantic vocabulary
equivalence relation
persistent invariant
information that may now be discarded
target vocabulary
```

The following alone do not justify another IR level:

```text
verification completed
optimization ran
material was frozen
a cache entry was created
a generation changed
storage changed
```

---

## 41. IR Equivalence Relation

State what it means for two representations at the same IR level to have the same meaning.

Transforms may change representation while preserving IR meaning.

A new generation does not imply a new IR level when the vocabulary and equivalence relation remain unchanged.

---

## 42. IR Input and Output Contract

Define the input obligation and output guarantee of the IR boundary.

Example shape:

```text
Input material
    ↓
formation / lowering / normalization
    ↓
IR satisfying invariant I
```

Do not describe the boundary only through implementation classes.

---

## 43. IR Consumer Map

List direct consumers.

Then list indirect future requirements separately.

A direct consumer may require an observable IR surface.

An indirect consumer should usually influence information-retention review, not add consumer-specific fields.

---

## 44. IR Transformation Surface

Classify transformations as:

```text
formation-time normalization
canonicalization
meaning-preserving optimization
target lowering
publication-only change
```

Do not let a physical compaction operation masquerade as semantic lowering.

Do not let Contract authority establishment masquerade as an ordinary IR transform.

---

## 45. IR Analysis Surface

Define which facts are primary IR material and which facts are derived analysis.

If multiple consumers need the same expensive result, publish a shared analysis product rather than copying the result
into unrelated IR nodes.

---

## 46. IR Verification Surface

Define structural and semantic invariants that an IR verifier may check.

The verifier checks the IR contract.

It does not define the contract.

---

## 47. IR Lowering Loss Audit

Before lowering to the next level, list every high-level distinction being discarded.

For each discarded distinction, confirm one of:

```text
no later consumer needs it
it has already been converted into a lower-level equivalent
it has already been consumed for all required legality / specialization work
```

If none applies, lowering is premature.

---

# Part III. Resolved Contract HIR Specialization

## 48. HIR Purpose

Resolved Contract HIR is the compiler-semantic baseline after frontend resolution and before Contract Establishment.

It is not Contract authority.

It must allow later semantic work to proceed without reopening source ambiguity.

---

## 49. HIR Resolution Completeness

Review at least:

```text
lexical ambiguity resolved
name ambiguity resolved
module / import resolution complete where required
authority kind resolved
semantic role resolved
required semantic references exact
unresolved lexical choice removed
```

If Establishment must perform source-name lookup again, the HIR boundary is incomplete.

---

## 50. HIR 1D Vocabulary Preservation

HIR must preserve every 1D distinction required to interpret the candidate exactly.

HIR does not redefine those distinctions.

The owning 1D Contract defines them.

HIR only represents the resolved candidate meaning.

---

## 51. HIR Candidate Sufficiency

Ask:

```text
Can Establishment decide using only the HIR candidate plus its explicitly allowed basis?
```

If not, determine whether:

```text
the 1D Contract is under-specified
or
HIR discarded required resolved meaning
```

Do not solve the gap with source re-reading inside Establishment.

---

## 52. HIR Recovery Boundary

Parser recovery and poison material must not silently become valid resolved semantic material.

The exact physical representation remains open.

The review must still identify a clear boundary between:

```text
recoverable source / syntax state
and
material satisfying the Resolved HIR invariant
```

Explicit Contract absence is not parser absence.

---

## 53. HIR Provenance Relation

Source provenance remains reachable from HIR semantic subjects.

Provenance is not part of HIR semantic equality unless an owning semantic rule explicitly requires it.

A source move may therefore change provenance while leaving a definition-level HIR projection unchanged.

---

## 54. HIR Fine-Grained Projection

HIR should permit stable projections smaller than one whole frontend generation when useful.

Candidates may include:

```text
definition projection
interface / interaction projection
resolved basis-declaration projection
semantic relation projection
provenance projection
```

The exact projection set remains a design decision.

Do not fix query keys in the HIR contract.

---

## 55. HIR Establishment Handoff

HIR must not contain established authority merely to simplify Establishment.

Establishment consumes resolved candidate meaning.

The boundary remains:

```text
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Established Definition Material
```

Validation success, HIR publication, and Establishment are different events.

---

## 56. HIR Pre-Establishment Transform Limit

Compiler transformations before Establishment may change representation.

They must preserve resolved candidate meaning.

Allowed categories may include:

```text
desugaring
frontend normalization
exact-reference formation
interning
deduplication
compaction
pre-resolution
```

They must not:

```text
create Contract authority
perform authority-owned judgment
replace established applicability
invent policy / governance decisions
```

---

# Part IV. Non-IR Compiler Product Review

## 57. Derived Product Rule

Not every shared compiler product is an IR.

Examples:

```text
analysis result
verification overlay
summary
index
query result
coverage product
diagnostic product
persistent cache artifact
```

For these products, use the generic checklist but skip IR-level questions that do not apply.

---

## 58. Summary and Overlay Rule

A summary or overlay must identify:

```text
source material
semantic subject
validity boundary
derivation rule
consumers
```

It must not become a second semantic authority merely because it is compact or widely reused.

---

## 59. Published Product Rule

A published product should have enough lifecycle information to reject incompatible reuse.

Possible information includes:

```text
producer / schema version
target identity
input product identity
publication generation
compatibility checks
corruption checks
```

These are compiler product concerns.

They are not automatically part of the semantic meaning represented by the product.

---

# Part V. Architecture-Wide Consumer Audit

## 60. Consumer Inventory

When a new major material is introduced, review it against the current compiler landscape.

The current inventory is:

```text
1. Compiler Driver / Session
2. Contract Frontend
3. Source Manager / Provenance
4. Parser / Syntax / Recovery
5. Name / Symbol / Contract Resolution
6. Resolved Contract HIR
7. Authority-Owned Establishment
8. Canonical Contract World
9. Generated API Product
10. Realization Acquisition
11. Realization Body IR
12. Realization Admission / Airlock
13. Admitted Realization Binding
14. Local Structural Analysis
15. CFG / Data-Flow Analysis
16. Call Graph / Call-Target Analysis
17. SCC / Interprocedural Analysis
18. Effect / Origin / Escape / Alias Analysis
19. Contract-Aware Analysis
20. Contract-Aware Summary
21. Realization / Core Closure Verifier
22. Verification Overlay / Verification Summary
23. Core Summary / Whole-Machine Summary
24. Execution Formation
25. Contract-Aware Execution IR
26. Execution IR Analysis / Transform / Optimization
27. JVM Legalization / JVM Plan / IR
28. Direct Classfile Backend / Artifact Publication
29. Query / Product / Identity / Generation / Cache / Reuse Infrastructure
30. Reference Judgment / PBT / Coverage / Diagnostics / Failure Evidence / Compiler QA / Observability
31. V2 Incremental Architecture / Persistent Dependency State / Incremental Repair / Cross-Session Reuse
```

Do not force every subsystem to consume every material.

For each subsystem, classify the relation as:

```text
direct consumer
indirect future requirement
no relation
```

For a direct consumer, review the observable surface.

For an indirect future requirement, review information retention only.

---

# Part VI. SOTA Failure-Mode Audit

## 61. Mega Representation

Failure:

```text
one node / product contains
source
semantic meaning
analysis state
diagnostics
cache state
backend state
incremental metadata
```

Consequence:

```text
wide invalidation
poor locality
unclear ownership
hard replacement
```

Prefer separate material families and explicit relations.

---

## 62. Re-Interpretation by Consumers

Failure:

```text
Verifier re-parses source
Optimizer re-resolves names
Backend reconstructs Contract meaning
Diagnostics infer semantic ownership from host shape
```

Upstream semantic work should be published once and reused.

Independent reference checking is an explicit exception, not an accidental duplicate frontend.

---

## 63. Query Graph Becomes Semantic Graph

Failure:

```text
query dependency
    → treated as Contract dependency
```

Compiler computation dependencies change with implementation.

Contract meaning must not.

---

## 64. Physical Reads Become Incremental Dependencies

Failure:

```text
field read / table row / memory cell
    → permanent dependency unit
```

This makes storage layout part of incremental architecture.

Prefer semantic product or projection boundaries.

---

## 65. Monolithic Invalidation

Failure:

```text
large aggregate changed
    → every downstream consumer invalidated
```

Provide finer semantic projections when they are stable and useful.

Do not require one query per primitive field.

---

## 66. Provenance Pollutes Semantic Equality

Failure:

```text
line move
comment edit
formatting edit
    → semantic product changed
```

Keep volatile provenance separate where Contract meaning allows it.

---

## 67. Cache or CAS Becomes Authority

Failure:

```text
stored product exists
    → therefore meaning is valid
```

Reuse always requires compatibility and validity rules.

Deletion of persistent cache must not change semantic output.

---

## 68. Hidden Inputs

Failure:

A supposedly deterministic compiler product secretly reads:

```text
wall clock
filesystem state
environment variable
mutable global
random state
undeclared network / service state
```

Hidden inputs make query and build dependencies unsound.

Make relevant inputs explicit or fail closed.

---

## 69. Stale Analysis

Failure:

```text
transform changes primary material
    ↓
old analysis reused without preservation proof
```

Analysis validity is part of compiler correctness.

Support preserve, maintain, or invalidate.

---

## 70. Over-Incrementalization

Failure:

Every computation is forced into fine-grained incremental maintenance even when tracking and repair cost exceed
recomputation.

Keep full recomputation valid.

Measure tracking cost, memory cost, change shape, and stability.

Allow domain-local and elastic strategies.

---

## 71. No Reclamation Model

Failure:

Multiple immutable generations are introduced without a plan for safe old-generation reclamation.

Publication and reclamation are separate.

The exact mechanism remains open.

---

## 72. Summary Becomes Authority

Failure:

A compact summary starts replacing its authoritative or primary source material.

A summary should answer declared consumer questions.

It should remain derived and recomputable unless an explicit authority says otherwise.

---

## 73. Manager Becomes Semantic Owner

Failure:

```text
HIRManager
AnalysisManager
PassManager
WorldManager
```

starts deciding semantic meaning because it controls lifecycle or scheduling.

Managers may own orchestration, reuse, invalidation, and resource policy.

They do not own Contract meaning or IR semantic law.

---

## 74. Early Target Lowering

Failure:

High-level semantic information is converted into generic JVM structure before the last Contract-aware consumer uses it.

Use high-level knowledge before it is discarded.

---

## 75. Full Material Everywhere

Failure:

Every wider-scope analysis opens every full body or full definition.

Use summaries, indexes, and selective body opening where sufficient.

---

## 76. Scheduling Changes Meaning

Failure:

```text
worker completion order
cache hit order
parallel merge order
```

changes semantic identity or result ordering.

Scheduling may change latency.

It must not change meaning.

---

# Part VII. Review Procedure

## 77. Review Order

Use this order for a new major material.

```text
1. Read the owning Contract / ADR material.

2. State the material purpose and stage invariant.

3. Define semantic vocabulary and admission invariant.

4. Identify direct and indirect consumers.

5. Run the consumer-sufficiency audit.

6. Define observable surface, identity, equality, reference, and projection boundaries.

7. Separate derived analysis, provenance, diagnostics, query state, cache state, and manager state.

8. Define publication, generation, determinism, and cancellation boundaries.

9. Review transformation and information-loss boundaries.

10. Review V1 reuse and V2 incremental seams.

11. Run the SOTA failure-mode audit.

12. Only then design physical representation and APIs.
```

---

## 78. Review Output

A review should produce a short decision report before implementation.

Use these classifications:

```text
Already decided
Needs semantic decision
Needs compiler-material decision
Owned by another Contract / ADR
Derived product
Implementation choice
V1 requirement
V2 seam
Intentionally open
Stale / conflicting design
```

The report should identify the canonical owner of each decision.

Do not duplicate the same law across multiple documents.

---

## 79. Completion Test

A major compiler material is not ready merely because a schema can be implemented.

It is ready for implementation when the following can be answered clearly:

```text
Why does this material exist?
What invariant does it guarantee?
What semantic vocabulary does it preserve?
What may be discarded?
Who produces it?
Who consumes it?
What may consumers rely on?
What is derived instead of primary?
What is semantic equality?
What are the useful stable projections?
How is publication made coherent?
How can analysis validity be expressed?
How can reuse be correct?
How can V2 observe dependencies without making storage topology semantic?
How can the physical representation be replaced?
```

If these answers require naming one Kotlin class, one cache implementation, or one query algorithm, the design boundary
is probably still too physical.

---

# Part VIII. Current Kontrakt Application

## 80. Major IR Families

Apply the generic checklist to at least:

```text
Resolved Contract HIR
Realization Body IR
Contract-Aware Execution IR
JVM Plan / IR
```

Each IR then adds its own specialized review questions.

---

## 81. Other Compiler Materials

Use the generic subset for:

```text
Canonical Contract World read surfaces
Shared Contract-Derived Knowledge
Local / Contract-Aware Analysis
Verification Overlay / Verification Summary
Core / Whole-Machine Summary
query / product results
persistent product state
diagnostic products
artifact publication metadata
```

Do not call all of these IRs merely because they are shared compiler material.

---

## 82. HIR Immediate Use

The next use of this checklist should be the Resolved Contract HIR architecture review.

The HIR review should focus first on:

```text
stage invariant
resolution completeness
1D candidate vocabulary preservation
consumer sufficiency
Establishment handoff
semantic / provenance separation
semantic equality
fine-grained projections
query / analysis compatibility
publication / generation
V1 reuse seam
V2 incremental seam
information-loss boundary
representation freedom
```

The physical HIR schema should be designed after these questions are closed.

---

# 83. Basis of This Draft

This checklist is aligned with the current Kontrakt architecture direction from:

```text
What Contract Is
ADR-0063
Kontrakt Compiler Architecture Map
Kontrakt Established Contract World Architecture TODO
Kontrakt IR Subsystem Contract / Implementation Separation Discussion
Kontrakt Query-Oriented Compiler Engine and Object-Free Core Representation
Kontrakt Compiler Reuse and Incremental Architecture TODO
Kontrakt V1 Commercial Compiler Foundation Candidate Architecture
Kontrakt V2 Reference Architecture and V1 Foundations
Kontrakt V2 Incremental Architecture Research TODO
Modern Compiler Architecture 01–15
```

The external engineering patterns referenced by those documents are used as architecture evidence only.

They do not override Kontrakt Contract semantics.

Relevant recurring principles include:

```text
explicit stage invariants
logical / physical separation
multi-level IR
immutable or coherent publication
analysis / transform separation
analysis preservation / invalidation
query-oriented product orchestration
fine-grained projection
semantic / provenance separation
summary-driven wide-scope work
content-addressed reuse
incremental early cutoff
domain-local repair
clean recomputation fallback
snapshot coherence
safe old-generation reclamation
hidden-input avoidance
deterministic publication
```

The central rule remains:

```text
Meaning first.

Compiler material must be sufficient for valid consumers.

Consumer topology must not become producer meaning.

Derived knowledge must not become authority.

Physical realization remains replaceable.
```