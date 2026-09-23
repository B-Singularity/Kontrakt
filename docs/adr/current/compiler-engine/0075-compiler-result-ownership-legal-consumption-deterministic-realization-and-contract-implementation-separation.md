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

Kontrakt is a Contract machine realized through a compiler, but the compiler is not a second source of Contract meaning.
Its job is to realize meaning that has already been declared under the owning Contract law.

ADR-0071 defines the pre-authority HIR boundary, and ADR-0063 defines how Establishment creates and exposes Established
meaning. ADR-0074 separately owns compiler-side unsuccessful results and recovery. Those ADRs close the producer-side
boundaries that this document relies on.

What remains is the downstream consumption problem. Later compiler responsibilities need earlier results, but they must
be able to use them without reopening source meaning or depending on the producer's private implementation state. This
ADR defines that compiler-wide consumption boundary and keeps compiler-derived knowledge separate from Contract
authority.

Caching and incremental compilation rely on this boundary once it exists. They do not define the boundary themselves.

---

## 2. Decision

Kontrakt will use producer-owned compiler result boundaries for results that are consumed outside the responsibility
that produced them. A result boundary exists when a producer has completed a result that another compiler
responsibility may rely on. The producer exposes that result through a declared observation surface.

The consumer must be able to interpret the result without access to the producer's private mutable state. That boundary
is not a universal `CompilerProduct` semantic type, nor does it turn local temporary values into published compiler
products.

The direction is:

```text
Declared Contract law
    ↓
legal semantic observation
    ↓
compiler-owned derivation
    ↓
complete compiler result
    ↓
legal compiler consumption
    ↓
replaceable physical realization
```

Contract authority stays with the Contract law that declares the meaning, so compiler consumption cannot transfer that
authority. A consumer may derive new compiler knowledge from what it observes, but the derived result belongs to the
compiler responsibility that formed it rather than to the upstream Contract authority.

A downstream compiler responsibility must not reconstruct missing Contract meaning from implementation state. A
deterministic result must not change because the compiler chose another legal physical execution path. Compiler
inability must remain a compiler result unless an owning Contract law gives that condition Contract meaning. Physical
implementation choices remain replaceable when the required legal observations stay the same.

---

## 3. Architecture Layers

The architecture keeps four layers distinct. Declared Contract law is the first and is the only one of these layers that
owns Contract meaning and authority.

Semantic material forms the second layer. Resolved Contract HIR represents meaning before authority, while Established
Semantic Protocol observations expose meaning after the owning Establishment law has succeeded.

Compiler reasoning forms the third layer. Analysis and summary results live here, as do verification results and
execution plans. A backend plan also remains compiler-derived unless an owning Contract law gives some part of it
Contract meaning.

Physical realization is the fourth layer. Storage layout and query scheduling are implementation choices within that
layer, as are cache structure and persistence. Backend data structures also remain part of physical realization rather
than semantic meaning.

```text
Contract law
    ↓
semantic material and legal observation
    ↓
compiler-derived result
    ↓
physical realization
```

A physical implementation may fuse work across these layers without merging their ownership. Section 6 defines that
separation once for the rest of this ADR, so later sections refer back to it instead of restating the same rule.

---

## 4. Determinism First

### 4.1. Semantic Determinism

ADR-0063 already establishes the semantic law:

```text
same semantic basis
    ↓
same established meaning
```

This ADR does not redefine that law. Compiler state outside the owning semantic basis cannot change Established meaning,
so a different worker schedule or cache state cannot produce a different Contract result. The same applies when the
compiler changes its storage or evaluation strategy. If changing one of those mechanisms changes Contract meaning, the
mechanism has leaked into the Contract.

### 4.2. Deterministic Compiler Results

A compiler-owned result can depend on explicit compiler inputs without making those inputs Contract determinants. A
backend target is one such input. When the result is declared deterministic, the same declared compiler inputs must
produce the same consumer-visible result.

```text
Contract determinism
    → owned by Contract semantic determinants

compiler-result determinism
    → owned by declared compiler-result inputs
```

The compiler-result rule cannot redefine Contract equality or Contract identity.

### 4.3. Physical Schedule Freedom

Determinism constrains the result rather than forcing one physical schedule. The compiler may reorder or parallelize
independent work, provided that every legal schedule exposes the same deterministic result to the consumer.

### 4.4. Cache-Blind Correctness

Cache state can change how much work the compiler performs, but it cannot decide meaning. A cold and a warm cache must
therefore expose the same legal result for the same current inputs. Section 21 owns the storage and cache consequences
of this law.

---

## 5. Explicit Meaning Before Compiler Convenience

Contract meaning must already be complete at the boundary owned by its Contract law. If a later compiler responsibility
needs a distinction that belongs to Input, that distinction must therefore be present in legal Input meaning rather
than recovered from a host object or inferred from backend representation.

The same rule applies to every other Contract authority. A later consumer cannot enlarge an earlier semantic surface
for convenience, and useful compiler metadata does not become Contract meaning merely because downstream work depends
on it. A cache key remains compiler metadata, while a storage address remains physical state. A fingerprint is compiler
evidence unless the owning semantic law gives the represented distinction Contract meaning.

The owning Contract ADRs define the actual semantic detail. This ADR only prevents downstream compiler needs from
changing that detail.

---

## 6. Contract and Implementation Separation

### 6.1. No Consumer-Driven Semantic Growth

An upstream semantic producer owns its meaning independently of later compiler needs. If a downstream subsystem needs
semantic information that the producer does not legally expose, the subsystem must not invent the missing meaning.
The design returns to the semantic owner, which decides whether that distinction belongs in its semantic surface.

The rule applies uniformly downstream. Verification and optimization remain consumers rather than semantic owners, and
the same boundary applies to diagnostics, generated artifacts, and backend work.

### 6.2. Compiler Derivation Owns Its Result

A compiler responsibility may derive a new result from legal upstream observations, and that result is owned by the
responsibility that formed it. Other compiler work may reuse the result, but reuse does not retroactively turn it into
Established Contract meaning.

```text
Established meaning
    ↓ legal observation
compiler analysis
    ↓
derived compiler result
```

### 6.3. User Realization and Compiler Realization Are Both Non-Authority

User code and the Kontrakt compiler both realize the declared machine, but neither realization defines Contract meaning.
Because Kontrakt itself owns the compiler implementation, its internal conveniences need the same authority boundary
that applies to external user realization.

The distinction matters because compiler code is maintained by Kontrakt itself. Internal implementation convenience
therefore needs the same authority boundary as external user realization.

### 6.4. Mechanism Names Stay Below the Contract

Contract ADRs state obligations rather than prescribing compiler machinery, unless a mechanism is itself the subject of
a compiler architecture ADR. Choosing a query engine therefore does not create Contract meaning. A pass manager is
subject to the same rule, and incremental behavior remains a replaceable compiler strategy. Sections 20 through 22
define the compiler consequences without moving those mechanisms into Contract law.

---

## 7. Reality and Availability

A real compiler may be unable to provide material even when the semantic category itself is well-defined. Retained
material can become unavailable, a persisted representation can be corrupt, and a compiler may not support the
requested feature. None of those conditions is semantic absence by itself, and none is automatically Contract refusal
or declared Failure.

The compiler must preserve the owner of each condition instead of turning inability to continue into a Contract
judgment. A stale retained result is a compiler-validity problem, while resource exhaustion is handled under Section 26
and an internal compiler defect remains an ADR-0074 compiler result.

ADR-0074 owns the common representation of compiler unsuccessful results and recovery. This ADR requires only that those
results remain separate from Contract meaning. The same separation applies to materialization: material that is not
currently materialized, or was never retained, is not thereby semantically absent.

---

## 8. HIR Producer Boundary

ADR-0071 remains the owner of Resolved Contract HIR and of the HIR subjects and HIR projection families defined at that
boundary. Establishment must consume HIR through the legal observation boundary defined by ADR-0071 rather than
reopening source syntax or inferring Candidate meaning from physical storage.

HIR remains pre-authority material, so observing it creates no Contract authority. The physical storage used to expose
that observation remains an implementation detail under ADR-0071 and Section 28 of this ADR.

---

## 9. HIR-to-Establishment Handoff

The HIR handoff must contain enough resolved semantic information for the owning Establishment law to run without a
hidden frontend. Every determinant used by Establishment must come from a legal HIR observation or from another exact
authoritative prerequisite that the owning law permits. An explicit authority-owned judgment input is also legal when
the owning law defines it.

HIR must not pre-establish a later result merely to simplify compiler implementation. Establishment still creates only
the meaning owned by the establishing authority, and compiler reachability cannot appear as an implicit side effect of
that judgment.

Optimization knowledge and diagnostic explanation remain downstream compiler concerns rather than Establishment output.
The same rule applies to any other conclusion that belongs to a later consumer.

These laws are already owned by ADR-0071 and ADR-0063. This section fixes only their handoff discipline for later
compiler products.

---

## 10. Establishment and Established Semantic Observation

ADR-0063 remains the owner of Established Material and the Established Semantic Protocol. It already distinguishes
Established Definition meaning from occurrence-specific meaning and leaves room for authority-specific Established
Material when an owning law defines it.

This ADR does not change the semantic placement of any of those categories. Downstream compiler work must consume
Established meaning through the legal observation surfaces defined by ADR-0063 and the owning authority-specific ADR.
Observation does not perform another Establishment or transfer authority to the consumer, and this ADR does not add a
universal `EstablishedMaterial` schema.

---

## 11. IDL Binding Discipline

ADR-0071 already separates Definition Candidate meaning from IDL Binding Candidate selection meaning, and this ADR
preserves that distinction. The architecture may still require an authoritative relation from a declared machine
position to an exact Established Definition, but this ADR does not create that relation. Before compiler products can
consume it, the owning Contract law must define both what determines the relation and when it becomes established.

If Policy affects the relation, that effect must have an explicit owner; Governance is subject to the same rule. Neither
a compiler dependency edge nor a generated host artifact can supply authority that the owning Contract law has not
established. Where the current Contract ADRs have not closed the exact post-HIR owner, that question remains outside
this ADR.

---

## 12. Compiler Result Boundary

A compiler result boundary belongs to compiler architecture. For a major result, the boundary must identify the logical
subject, the producer that owns it, and the guarantee on which a legal consumer may rely.

When the result has compiler-specific inputs, those inputs must be explicit enough to support deterministic
recomputation. The producer must also expose a legal read surface for the completed result. When the result can be
unavailable, its unsuccessful-result boundary must be explicit.

When reuse compares an earlier result with a current one, the producer owns the comparison rule for that result family.
Information loss must likewise remain visible whenever later consumers depend on the lost distinction. These
requirements
apply to independently consumed results rather than to private scratch state.

ADR-0074 owns the common unsuccessful-result protocol. Information loss is governed by Section 15, explicit compiler
inputs by Section 17, and reuse distinctions by Section 19.

---

## 13. Legal Consumption

A consumer may use only the observation surface exposed by the producer level it consumes. Resolved HIR therefore
follows
ADR-0071, while Established semantic consumption follows ADR-0063.

Compiler-derived results use a producer-owned read surface appropriate to that result family. This ADR does not
require every such surface to be named `Protocol`. The physical access method is not part of the logical rule. A
producer may expose the same legal observation through a compact table or through another representation.

A consumer must not depend on private construction state or reconstruct upstream semantic meaning from physical
topology.
Using a result transfers neither Contract authority nor producer ownership.

---

## 14. Projection and Summary

A projection exposes part of meaning that the producer already owns. A summary is a new compiler result formed by
analysis of earlier material.

```text
producer meaning
    ↓
producer-owned projection

producer meaning
    ↓ analysis
summary result
```

The distinction matters because a summary may answer a wider-scope compiler question without becoming a second
semantic authority. If a consumer only needs the summary, it may avoid materializing the full producer body. Section
25 applies this rule to Whole-Machine work.

The exact summary representation remains Design unless another compiler ADR gives it an explicit architecture boundary.
Its persistence policy, update strategy, and query key remain Design for the same reason.

---

## 15. Information Preservation and Loss

A compiler stage may erase information only when the erased distinction is no longer required by any legal consumer of
that stage. A later consumer must not reconstruct erased Contract meaning from source shape or backend representation.
If later work still needs the distinction, the upstream legal observation must retain it or a separately owned
compiler result must preserve it before the loss occurs.

This law explains why target lowering cannot discard high-level Contract knowledge before Kontrakt has finished using
that knowledge. It does not require lower IR to carry every earlier distinction forever.

---

## 16. Derived Analysis and Transformation

Analysis and transformation belong to compiler realization, but a transformation may invalidate assumptions used by an
earlier analysis. Once that happens, the compiler must not keep consuming the stale result as though it were still
valid.

This ADR does not require the LLVM or MLIR invalidation API. The producer may recompute or update the result, invalidate
it for later rebuilding, or prove that the relevant result did not change.

Incremental repair is another legal strategy. Whichever mechanism the producer chooses, stale knowledge must remain
inside the producer's validity boundary. The Contract requirement is unchanged: a legal compiler transformation must
preserve the meaning it is obligated to preserve. Section 33 records the external engineering references for this rule.

---

## 17. Explicit Compiler Inputs Without Semantic Leakage

A deterministic compiler result must not depend on hidden implementation state. Any compiler-specific condition that may
legally affect the result must therefore be owned by the producer as an explicit compiler input. A backend target is one
such input when the result depends on the selected target.

The same rule applies to a selected compiler feature mode or toolchain capability when the result depends on it. An
optimization mode must be explicit when it changes the legal output form.

A persistent format revision is also an input when it affects whether retained material can be reused. None of these
compiler inputs becomes a Contract determinant, and uncontrolled environment state must not silently substitute for one.

Wall-clock time can affect the result only when the producer declares it. Filesystem traversal order and process locale
must not silently change the outcome.

Randomness likewise needs an explicit owner when it is legal. Mutable global state and cache presence cannot become
undeclared determinants.

Section 4 owns the determinism law. This section only states how compiler-specific determinants enter that law.

---

## 18. Visibility and Coherent Observation

An independently consumed result must become visible only after the producer has completed the guarantee exposed to
ordinary consumers. A consumer must observe a compatible set of inputs for one computation. Compatibility is a
semantic and compiler-result requirement.

A matching numeric generation identifier is not enough by itself to prove that observation is coherent. Physical
visibility remains implementation work, and the compiler may realize it with immutable generations or another safe
publication mechanism.

Reclamation is separate from semantic validity. Keeping old storage alive does not make it current, while reclaiming
that
storage does not undo a semantic occurrence that was previously established. Section 28 keeps the physical mechanism
replaceable.

---

## 19. Equality, Validity, and Reuse

Semantic equality answers whether semantic meaning is the same and is controlled by the owning semantic producer.
Compiler-result equality asks a different question: whether two results in one compiler result family are equivalent
under the comparison declared by that producer.

Reuse validity asks whether retained material may satisfy the current request, which is different from either equality
judgment. A fingerprint may support a physical comparison, but it does not become semantic equality authority.

Byte equality is also not semantic equality by itself. Object identity is not semantic equality either. A retained
result can be semantically equal to a clean result and still be unusable because its compiler compatibility has
expired.

The converse also holds: physical identity does not prove semantic equality. Sections 21 through 23 use this distinction
when defining cache and incremental behavior.

---

## 20. Dependency Recording Is Compiler Machinery

Compiler dependency tracking records the work needed for recomputation and reuse; it does not define Contract
dependency.
A Contract Basis relation therefore remains distinct from a compiler computation edge.

HIR semantic relations are separate for the same reason. Build dependency, provenance, and physical reachability also
remain independent relations rather than alternate forms of semantic dependency.

A dynamic dependency trace describes one previous computation rather than the complete semantic law for every future
run.
If control flow changes, later reads may change as well, so an incremental engine must validate or rediscover its
dependencies soundly. The representation of the dependency graph remains Design.

---

## 21. Cache, Persistence, and Content Addressing

Cache, persistence, and content addressing support reuse in different ways. Cache avoids repeated work, persistence
keeps
representation available, and content addressing helps locate or validate retained material.

None of those mechanisms creates Contract authority. Retained material is usable only after the producer's current
validity rule has succeeded; finding the bytes is not enough.

A fingerprint or HID may accelerate lookup and validation without becoming semantic identity authority. BLAKE3 can
compute that evidence, while Merkle summaries or CAS may support validation and retention at wider scopes. Those choices
remain compiler mechanisms and do not change the meaning owned by the semantic producer.

If retained state is missing or unusable, the compiler needs another legal path whenever the requested result is
otherwise
computable. Section 4.4 owns cache-blind correctness, while Section 19 owns the distinction between equality and reuse.

---

## 22. Incremental Compilation Follows the Architecture

V1 must leave a durable seam for future incremental work without making one incremental algorithm permanent
architecture.
A reusable major result therefore needs a stable logical subject, knowable legal inputs, and a producer-owned comparison
rule for reuse.

A clean recomputation path remains the semantic reference when retained state is absent. V2 may choose different
repair strategies for different result families. A query result and a data-flow result do not need the same
incremental algorithm.

The incremental engine remains below the semantic boundary defined by the earlier ADRs. Section 6 preserves the
authority
boundary, and Section 20 preserves the dependency boundary.

---

## 23. Early Cutoff Is a Derived Optimization

An incremental engine may stop propagation when the producer has re-established that the result observed by downstream
consumers is unchanged. An upstream source change alone does not prove that cutoff is legal. The owning result must
first be validated, recomputed, or repaired.

A fingerprint may accelerate the check only under a sound producer-owned comparison rule, and a hash collision cannot
redefine that equality. This section leaves Contract equality unchanged; it permits only the compiler to avoid
downstream
work once the relevant owned result is known to be unchanged.

---

## 24. Generated APIs and Backend Products

Generated host interfaces are downstream compiler artifacts rather than Contract authority. The compiler may derive an
intermediate plan before generation or fuse that planning into generation itself.

In either realization, the generated surface must preserve the Contract meaning it is required to represent. The backend
follows the same direction: it consumes legal upstream material without turning JVM object shape into new Contract
meaning.

Section 6 owns this authority separation. Section 15 owns information preservation before target lowering.

---

## 25. Whole-Machine and Summary-Driven Work

Whole-Machine compiler work may consume a sufficient summary instead of forcing every consumer to materialize complete
local bodies. The summary remains a compiler result owned by its producer. If a local change leaves that summary
unchanged, a consumer that depends only on the summary may avoid recomputation under Section 23.

A Whole-Machine Contract authority is different from a compiler summary. If Contract law establishes new Whole-Machine
meaning, the relevant Contract ADR must own it; summary infrastructure cannot acquire that authority.

---

## 26. Resource Reality

Compiler work is finite, so the implementation needs explicit limits that prevent uncontrolled resource consumption.
Those limits belong to compiler realization rather than automatically becoming Contract Budget or Contract Capacity.

They enter the user machine only when an owning Contract law explicitly gives them that meaning. Resource exhaustion
must
not silently alter Contract meaning: the compiler uses a legal fallback when one exists, and otherwise the owning
subsystem produces its ADR-0074 unsuccessful result.

An optimization may be abandoned when it exceeds its compiler limit. Required semantic work cannot be silently omitted
for that reason.

---

## 27. Query and Pass Orchestration Are Replaceable

V1 may use query-oriented orchestration for major reusable results while still using passes inside a producer when an
ordered local transformation is the suitable implementation. Neither query topology nor pass order defines Contract
authority.

A later compiler may change its orchestration strategy as long as the same legal result boundaries remain observable.
Section 20 owns dependency recording, and Section 22 owns the incremental seam.

---

## 28. Physical Representation Freedom

A logical result boundary does not imply one JVM object or one table. The compiler may choose a representation that fits
measured access patterns and replace that representation later.

Physical fusion does not merge semantic ownership, just as physical separation does not create new semantic entities.
The
first implementation therefore does not become permanent architecture merely because it starts from one particular
layout.

ADR-0071 already applies this principle to HIR, and ADR-0063 applies it to Established meaning. This section extends the
same implementation freedom to compiler-derived results.

---

## 29. V1 Requirements

V1 must preserve Visible HIR as the deterministic pre-authority boundary defined by ADR-0071. Establishment must
consume only legal HIR observations and exact authoritative prerequisites permitted by the owning law. Established
semantic material must be consumed through ADR-0063-owned observation surfaces.

Every major compiler result that is independently consumed must have an explicit producer and a complete
consumer-visible guarantee. Derived compiler knowledge must remain non-authoritative under Section 6. Deterministic
results must remain independent of hidden compiler state under Section 4.

Ordinary consumers must not observe incomplete successful results under Section 18. Compiler unsuccessful results must
remain distinct from Contract negative meaning under Section 7 and ADR-0074. Clean recomputation must remain correct
without cache or persistence under Section 21.

Query choices remain replaceable under Section 27, while storage choices remain replaceable under Section 28.
Verification
must retain an independent or clean comparison path wherever the product family requires one.

Compiler architecture must not invent the unresolved authoritative IDL binding owner; Section 11 leaves that question
with
the semantic owner. V1 is not required to persist every result or incrementalize every computation.

---

## 30. V2 Evolution

V2 may add persistence and incremental repair behind the V1 boundaries. Persistent HIR projections remain subject to
ADR-0071, while persistent compiler products remain governed by the reuse law owned by their producer.

The stronger engine may add selective dependency validation where that reduces work and incremental analysis where the
result family supports it. Whole-Machine summaries may also gain incremental repair.

Backend artifacts may gain reuse as well, while reader lifetime and reclamation remain independent of semantic identity.
HIR observations may be persisted when ADR-0071 compatibility rules permit it, and compiler-derived results may be
persisted when their producer defines a safe reuse rule.

Different result families may choose different dependency and repair strategies. The compiler may also add stronger
summary reuse and backend artifact reuse where the owning result boundaries support them. These implementation changes
must not require Contract ADRs to change merely because the compiler has gained a stronger reuse engine. Section 22
remains the governing architecture seam.

---

## 31. Adversarial Review

The following cases test whether the earlier laws are being violated.

### 31.1. Implementation Becomes Authority

A cache hit is treated as proof of Contract validity. Rejected by Sections 6 and 21.

### 31.2. Consumer Creates Missing Contract Meaning

A backend adds a semantic field because backend generation needs it. Rejected by Sections 5 and 6. The question
returns to the owning semantic ADR.

### 31.3. HIR Pre-Establishes Authority

HIR computes a later authoritative result only to simplify Establishment. Rejected by Sections 8 and 9.

### 31.4. Compiler Failure Masquerades as Contract Failure

Corrupt retained compiler data is reported as Contract Failure. Rejected by Section 7 and ADR-0074.

### 31.5. Hidden Environment Changes Result

The same declared inputs produce another deterministic result because an undeclared environment condition changed.
Rejected by Sections 4 and 17.

### 31.6. Generated Artifact Becomes Source of Truth

A generated JVM signature is used to reconstruct Contract identity. Rejected by Sections 6 and 24.

### 31.7. Old Dependency Trace Becomes Semantic Law

A previous dynamic read-set is treated as the complete future semantic dependency relation. Rejected by Section 20.

### 31.8. Stale Derived Knowledge Survives Transformation

A transformation invalidates an analysis assumption and the old result is still consumed. Rejected by Section 16.

### 31.9. Summary Becomes Authority

A Whole-Machine summary is treated as canonical Contract meaning. Rejected by Sections 14 and 25.

### 31.10. Resource Exhaustion Changes Meaning

Compiler resource exhaustion causes required semantic material to be omitted. Rejected by Section 26.

### 31.11. Physical Co-Location Creates Identity

Two semantic entities are treated as identical because they share physical backing. Rejected by Sections 19 and 28.

### 31.12. Consumer-Specific Equality Rewrites Producer Meaning

A consumer ignores a producer-owned distinction and declares the complete producer result equal. Rejected by Section

19. A legal projection or a separately owned summary is required instead.

---

## 32. Verification and QA Requirements

Verification must exercise each law at the boundary that owns it. Where reuse exists, a reused computation should be
checked against clean computation while cache state is varied.

For deterministic products, parallel execution and alternative legal schedules should be checked against the reference
path so that scheduling changes cannot alter the declared result. Persistent reload must likewise agree with clean
recomputation.

The owning validity law must reject stale retained material and unsupported retained revisions. Corrupt retained
material
follows ADR-0074 instead of changing semantic meaning.

Fingerprint and hash validation should include adversarial checks when those mechanisms participate in reuse evidence.
Where the compiler supports more than one physical representation, tests should confirm that the legal observation
remains the same. This check covers changes in physical split and fusion as well as changes in storage form.

Optimized paths should be compared with an independent or reference path where the subsystem requires that assurance.
Hidden environment perturbation should be part of determinism testing when such state could accidentally enter the
result. If a final artifact requires byte reproducibility, the backend must control every input that can legally
affect those bytes.

That requirement is stronger than semantic determinism. The artifact producer owns it.

---

## 33. Non-Normative Engineering Basis

The following systems support individual engineering principles. None defines Kontrakt Contract semantics.

### 33.1. LLVM New Pass Manager

LLVM keeps reusable analysis results separate from transforms. A transform must not leave stale analysis visible as if
it were still valid. This supports Section 16. It does not require Kontrakt to copy LLVM's pass-manager API.

Source: <https://llvm.org/docs/NewPassManager.html>

### 33.2. MLIR Pass Infrastructure

MLIR also separates analysis lifetime from transformation. Its preservation rules provide another implementation
example of the stale-knowledge problem described in Section 16.

Source: <https://mlir.llvm.org/docs/PassManagement/>

### 33.3. rustc Query and Incremental Compilation

rustc incremental compilation assumes deterministic query results before reuse is considered. That supports the
ordering used by Sections 4 and 22. rustc also tracks dynamic query dependencies.

A changed earlier read can change which later reads occur. That supports the warning in Section 20 that an old
dependency trace is not semantic law.

Sources:

- <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation.html>
- <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation-in-detail.html>

### 33.4. LLVM ThinLTO

ThinLTO demonstrates that global compiler work can use a compact derived summary instead of requiring every consumer
to load complete local IR. This supports Sections 14 and 25.

Source: <https://clang.llvm.org/docs/ThinLTO.html>

### 33.5. Reproducible Builds and Nix

Reproducible-build work demonstrates the danger of undeclared build inputs. Nix derivations provide a related example
of making build inputs explicit. These systems support Section 17 and the cache-blind rule in Section 4. They do not
define Contract semantics.

Sources:

- <https://reproducible-builds.org/docs/deterministic-build-systems/>
- <https://reproducible-builds.org/docs/timestamps/>
- <https://wiki.nixos.org/wiki/Derivations>

### 33.6. Linux RCU and RocksDB Snapshots

RCU demonstrates that replacement and reclamation can be separate physical concerns. RocksDB snapshots demonstrate
explicit point-in-time read views. These systems support the implementation freedom in Section 18. Their epoch or
sequence identifiers are not semantic identity in Kontrakt.

Sources:

- <https://docs.kernel.org/RCU/whatisRCU.html>
- <https://github.com/facebook/rocksdb/wiki/Snapshot>

### 33.7. DBSP and Enzyme

DBSP demonstrates principled incremental maintenance for computations that fit its model. Enzyme shows that a
production system may choose among refresh strategies. These examples support product-specific incremental design
rather than one universal algorithm.

These systems support Section 22. They do not define the Kontrakt incremental architecture.

Sources:

- DBSP: <https://www.vldb.org/pvldb/vol16/p1601-budiu.pdf>
- Enzyme: <https://arxiv.org/abs/2603.27775>

### 33.8. Verified Compilation and Reproducibility Research

Recent verified-compiler work provides evidence for checking implementation paths against independently specified
semantics. Reproducibility research provides related evidence for checking the integrity of artifact production. These
references support Section 32. They are not architecture templates.

Examples:

- *Verified VCG and Verified Compiler for Dafny* (2025): <https://arxiv.org/abs/2512.05262>
- *Verifiable Provenance of Software Artifacts with Zero-Knowledge Compilation*
  (2026): <https://arxiv.org/abs/2602.11887>

---

## 34. Rejected Designs

### 34.1. Compiler Product Graph as Contract Graph

Rejected. Section 20 keeps compiler dependency recording separate from Contract semantic dependency.

### 34.2. One Universal Product Schema

Rejected. Sections 3 and 12 preserve producer-owned result families instead of one semantic super-schema.

### 34.3. Query as Authority

Rejected. Section 27 keeps query orchestration below Contract authority.

### 34.4. Cache as Validity

Rejected. Sections 19 and 21 require current reuse validity independently of cache presence.

### 34.5. Fingerprint as Semantic Equality

Rejected. Section 19 keeps fingerprints below producer-owned equality.

### 34.6. Compiler Unavailability as Semantic Absence

Rejected. Section 7 keeps compiler availability separate from semantic absence.

### 34.7. Analysis API as Contract Law

Rejected. Section 16 fixes the correctness requirement without fixing one analysis API.

### 34.8. Generated API as Authority

Rejected. Section 24 treats generated APIs as downstream artifacts.

### 34.9. Full Source Reopening Downstream

Rejected. Sections 8, 9, and 13 require legal upstream observations instead.

### 34.10. First Physical Layout as Permanent Architecture

Rejected. Section 28 keeps physical representation replaceable.

---

## 35. Consequences

### 35.1. Positive

The resulting architecture keeps Contract authority above compiler machinery and establishes determinism before reuse or
incremental optimization. HIR and the Established Semantic Protocol remain semantic observation boundaries rather than
universal compiler APIs.

Compiler-derived knowledge gains an explicit owner without becoming a second Contract model. Compiler failure can be
represented without fabricating Contract Failure. V1 can use practical in-memory compiler techniques while V2 remains
free to adopt stronger persistence and incremental repair.

Whole-Machine work can use summaries without moving authority into those summaries. Backend replacement remains
possible because target representation does not define Contract meaning.

### 35.2. Costs

The cost is that compiler subsystems must state ownership and consumer-visible guarantees more precisely. Some direct
reads from source or global compiler context become illegal shortcuts, and reuse requires a real validity rule rather
than cache presence.

Verification must compare alternative execution paths wherever a product family requires that assurance. The unresolved
authoritative IDL binding owner also cannot be hidden inside compiler plumbing and must instead be closed by the
semantic
owner.

---

## 36. Required Follow-Up

The next work remains separated by ownership.

### 36.1. Contract / Establishment Follow-Up

The exact authoritative IDL or Interface slot-binding owner must be closed by the owning Contract law. The 1D
HIR-to-Establishment review must continue through the master checklist without adding compiler reuse metadata to 1D
meaning.

### 36.2. Compiler Architecture Follow-Up

The major result producers from HIR through the JVM backend must be mapped. Each independently consumed result must
state its consumer-visible guarantee and its legal upstream inputs. ADR-0074 integration must close the common
compiler-result availability boundary.

### 36.3. Design Follow-Up

V1 must choose concrete query boundaries and storage representations. The storage design must also decide where
physical split or fusion is useful from measured access patterns. The implementation must also choose its
analysis-validity mechanism and its visibility mechanism.

Reuse evidence remains Design under the laws in this ADR. The fingerprint or HID strategy belongs at that level, and so
does summary granularity.

### 36.4. V2 Research Follow-Up

V2 research should compare persistence strategies only for result families that benefit from them. That work includes
persistent product loading and selective validation, while dependency-repair strategies should be compared separately.

Incremental analysis and summary repair therefore need their own evaluations. Delta maintenance should be considered
only
where the result family fits that model.

The choice between adaptive repair and rebuild remains open. Backend reuse must follow the same producer-owned validity
model, and persistent backend artifacts must not acquire semantic authority.

---

## 37. Summary

Kontrakt uses compiler result boundaries so later compiler responsibilities can consume already-formed meaning without
recreating semantic authority. The compiler may derive new knowledge from legal observations, but that knowledge remains
compiler-owned.

Section 4 keeps deterministic results independent of replaceable physical machinery, Section 6 preserves the boundary
between Contract and implementation, and Section 7 keeps compiler failure separate from Contract meaning.

Reuse and incremental compilation build on those laws under Sections 19 through 23. This separation allows the
compiler to become more aggressive without making its current implementation part of the Contract.