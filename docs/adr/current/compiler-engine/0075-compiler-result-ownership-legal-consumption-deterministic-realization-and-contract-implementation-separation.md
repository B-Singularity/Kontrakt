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

Kontrakt is a Contract machine implemented by a compiler.

The compiler exists to realize declared Contract meaning.

Its internal organization must not become a second source of that meaning.

ADR-0071 already defines the pre-authority HIR boundary.

ADR-0063 already defines Establishment and the observation of Established meaning.

ADR-0074 defines compiler-owned unsuccessful results and recovery.

The remaining problem is how later compiler responsibilities consume those results.

Later compiler responsibilities need earlier results.

They must be able to consume those results without reopening source meaning or inheriting the producer's private
implementation state.

This ADR defines that compiler-wide consumption boundary. It also fixes the separation between compiler-derived
knowledge and Contract authority.

Caching and incremental compilation depend on this boundary. They do not define it.

---

## 2. Decision

Kontrakt will use producer-owned compiler result boundaries for results that are consumed outside the responsibility
that produced them.

A result boundary exists when a producer has completed a result that another compiler responsibility may rely on.

The producer exposes that result through a declared observation surface.

The consumer must not need the producer's private mutable state to interpret it.

This ADR does not create a universal `CompilerProduct` semantic type. It also does not turn local temporary values into
published compiler products.

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

Contract authority remains owned by the Contract law that declares it.

Compiler consumption does not transfer that authority.

A compiler consumer may derive new compiler knowledge. That new knowledge belongs to the deriving compiler
responsibility.

A downstream compiler responsibility must not reconstruct missing Contract meaning from implementation state.

A deterministic result must not change because the compiler chose another legal physical execution path.

Compiler inability must remain a compiler result unless an owning Contract law gives that condition Contract meaning.

Physical implementation choices remain replaceable when the required legal observations stay the same.

---

## 3. Architecture Layers

The architecture distinguishes four layers.

The first layer is declared Contract law. It owns Contract meaning and authority.

The second layer is semantic material that represents meaning at a compiler boundary.

Resolved Contract HIR belongs here before authority.

Established Semantic Protocol observations belong here after authority.

The third layer is compiler-derived knowledge.

Analysis results belong here when they come from compiler reasoning.

Summaries belong here for the same reason.

Verification results and execution plans do too.

Backend plans remain compiler-derived unless Contract law says otherwise.

The fourth layer is physical realization.

Storage layout belongs here.

Query scheduling does too.

Cache structure and persistence are also physical realization.

Backend data structures remain in this layer.

```text
Contract law
    ↓
semantic material and legal observation
    ↓
compiler-derived result
    ↓
physical realization
```

A physical implementation may fuse work across these layers. That optimization does not merge their ownership.

Section 6 owns the general separation law. Later sections refer back to it rather than restating it.

---

## 4. Determinism First

### 4.1. Semantic Determinism

ADR-0063 already establishes the semantic law:

```text
same semantic basis
    ↓
same established meaning
```

This ADR does not redefine that law.

Compiler state that is not part of the owning semantic basis cannot change Established meaning.

A different worker schedule therefore cannot change Contract meaning.

A different cache state cannot change it either.

Changing storage or evaluation strategy follows the same rule.

If changing a compiler mechanism changes Contract meaning, that mechanism has leaked into the Contract.

### 4.2. Deterministic Compiler Results

A compiler-owned result may have explicit inputs that are not Contract determinants. A backend target is one example.

When a compiler result is declared deterministic, the same declared inputs must produce the same consumer-visible
result.

```text
Contract determinism
    → owned by Contract semantic determinants

compiler-result determinism
    → owned by declared compiler-result inputs
```

The compiler-result rule cannot redefine Contract equality or Contract identity.

### 4.3. Physical Schedule Freedom

Determinism does not require one physical schedule.

The compiler may evaluate independent work in another order. It may also parallelize that work. The consumer-visible
result must still satisfy the same deterministic result law.

### 4.4. Cache-Blind Correctness

Cache state cannot decide meaning.

A cold cache may require more work than a warm cache. It must not change the legal result for the same current inputs.

Section 21 owns the storage and cache consequences of this law.

---

## 5. Explicit Meaning Before Compiler Convenience

Contract meaning must be complete at the boundary owned by its Contract law.

If a later compiler responsibility needs a distinction that belongs to Input, that distinction must already exist in
legal Input meaning.

The compiler may not recover it from a host object. It may not recover it from backend representation either.

The same rule applies to every other Contract authority. A later consumer cannot enlarge an earlier semantic surface for
convenience.

Compiler metadata does not become Contract meaning merely because it is useful.

A cache key remains compiler metadata.

A storage address remains physical state.

A fingerprint remains compiler evidence unless an owning semantic law gives the represented distinction Contract
meaning.

The owning Contract ADRs define the actual semantic detail. This ADR only prevents downstream compiler needs from
changing that detail.

---

## 6. Contract and Implementation Separation

### 6.1. No Consumer-Driven Semantic Growth

An upstream semantic producer owns its meaning independently of its consumers.

If a downstream subsystem needs semantic information that the producer does not legally expose, the subsystem must not
invent it.

The design must return to the semantic owner. The owner then decides whether the missing distinction belongs in that
semantic surface.

This rule applies to every downstream compiler consumer.

Verification does not get an exception. Optimization does not get one either. Diagnostics, generated artifacts, and
backend work remain consumers under the same rule.

### 6.2. Compiler Derivation Owns Its Result

A compiler responsibility may derive a new result from legal upstream observations.

That result belongs to the compiler responsibility that formed it. It may be reused by other compiler work. It does not
retroactively become Established Contract meaning.

```text
Established meaning
    ↓ legal observation
compiler analysis
    ↓
derived compiler result
```

### 6.3. User Realization and Compiler Realization Are Both Non-Authority

User code realizes the declared machine. The Kontrakt compiler also realizes that machine through its own
implementation.

Neither realization defines Contract meaning.

The distinction matters because compiler code is maintained by Kontrakt itself. Internal implementation convenience
therefore needs the same authority boundary as external user realization.

### 6.4. Mechanism Names Stay Below the Contract

Contract ADRs state obligations. They do not require a particular compiler mechanism unless that mechanism is itself the
subject of a compiler architecture ADR.

A query engine is therefore not a Contract concept. A pass manager is not a Contract concept. An incremental algorithm
is not a Contract concept.

Sections 20 through 22 define the compiler consequences without moving those mechanisms into Contract law.

---

## 7. Reality and Availability

A real compiler can fail to provide material even when the semantic category itself is well-defined.

Retained material can be unavailable.

A persisted representation can be corrupt.

A compiler feature can be unsupported.

None of those states is semantic absence by itself.

Contract refusal and declared Failure also remain separate from compiler inability.

The compiler must preserve the owning boundary of each condition.

A compiler problem does not become a Contract judgment merely because later work cannot continue.

A stale retained result remains a compiler-validity problem. A resource limit remains compiler reality under Section 26.
An internal compiler defect remains compiler-owned unsuccessful state under ADR-0074.

ADR-0074 owns the common representation of compiler unsuccessful results and recovery. This ADR only requires those
results to remain separate from Contract meaning.

The same principle applies to materialization.

Material that is not currently materialized is not automatically absent from the semantic model.

Material that was never retained is also distinct from semantic absence.

---

## 8. HIR Producer Boundary

ADR-0071 remains the owner of Resolved Contract HIR.

This ADR does not redefine HIR subjects or HIR projection families.

Establishment must consume HIR through the legal observation boundary defined by ADR-0071. It must not reopen source
syntax or infer Candidate meaning from physical storage.

HIR remains pre-authority material. Observation of HIR does not create Contract authority.

Physical storage used to expose HIR remains an implementation detail under ADR-0071 and Section 28 of this ADR.

---

## 9. HIR-to-Establishment Handoff

The HIR handoff must contain enough resolved semantic information for the owning Establishment law to run without a
hidden frontend.

Every determinant used by Establishment must come from a legal HIR observation or from another exact authoritative
prerequisite that the owning law permits.

An explicit authority-owned judgment input is also legal when the owning law defines it.

HIR must not pre-establish a later result to simplify compiler implementation.

Establishment still creates only the meaning owned by the establishing authority.

Compiler reachability does not appear as a side effect of Establishment.

Optimization knowledge does not appear there either.

Diagnostic explanation remains consumer-owned. Other downstream conclusions follow the same rule.

These laws are already owned by ADR-0071 and ADR-0063. This section fixes only their handoff discipline for later
compiler products.

---

## 10. Establishment and Established Semantic Observation

ADR-0063 remains the owner of Established Material and the Established Semantic Protocol.

Established Definition meaning remains distinct from occurrence-specific meaning.

Authority-specific Established Material also remains possible when an owning law defines it.

This ADR does not change the semantic placement of any of those categories.

Downstream compiler work must consume Established meaning through the legal observation surfaces defined by ADR-0063 and
the owning authority-specific ADR.

Observation does not perform another Establishment. It also does not transfer authority to the consumer.

This ADR does not add a universal `EstablishedMaterial` schema.

---

## 11. IDL Binding Discipline

ADR-0071 already separates Definition Candidate meaning from IDL Binding Candidate selection meaning. This ADR preserves
that distinction.

The current architecture may require an authoritative relation from a declared machine position to an exact Established
Definition. This ADR does not create that relation.

The owning Contract law must define the authoritative relation before compiler products can consume it.

That law must define the relation's determinants. It must define when the relation becomes established.

If Policy participates, its effect must be owned explicitly. Governance follows the same rule.

A compiler dependency edge cannot create the missing authority. A generated host artifact cannot create it either.

Where the current Contract ADRs have not closed the exact post-HIR owner, that question remains outside this ADR.

---

## 12. Compiler Result Boundary

A compiler result boundary belongs to compiler architecture.

A major result should state its logical subject and its producer. It should also state what a legal consumer is allowed
to rely on.

When the result has compiler-specific inputs, those inputs must be explicit enough to support deterministic
recomputation.

The producer must also expose a legal read surface for the completed result.

When the result can be unavailable, its unsuccessful-result boundary must be explicit.

If reuse depends on result comparison, the producer owns that comparison rule for the compiler result family.

Information loss must also be visible when later consumers rely on the result.

This requirement applies only to independently consumed results. Local scratch state may remain private.

ADR-0074 owns the common unsuccessful-result protocol.

Section 15 owns information-loss rules. Section 17 owns explicit compiler inputs. Section 19 owns reuse distinctions.

---

## 13. Legal Consumption

A consumer may use only the observation surface exposed by the producer level it consumes.

Resolved HIR consumption follows ADR-0071.

Established semantic consumption follows ADR-0063.

Compiler-derived results use a producer-owned read surface appropriate to that result family. This ADR does not require
every such surface to be named `Protocol`.

The physical access method is not part of the logical rule. A producer may expose the same legal observation through a
compact table or through another representation.

A consumer must not depend on private construction state. It also must not reconstruct upstream semantic meaning from
physical topology.

Consumption transfers neither Contract authority nor producer ownership.

---

## 14. Projection and Summary

A projection exposes part of meaning that the producer already owns.

A summary is a new compiler result formed by analysis of earlier material.

```text
producer meaning
    ↓
producer-owned projection

producer meaning
    ↓ analysis
summary result
```

The distinction matters because a summary may answer a wider-scope compiler question without becoming a second semantic
authority.

If a consumer only needs the summary, it may avoid materializing the full producer body. Section 25 applies this rule to
Whole-Machine work.

The exact summary representation remains Design unless another compiler ADR gives that summary an explicit architecture
boundary.

Its persistence policy remains Design as well. The same is true for its update strategy and query key.

---

## 15. Information Preservation and Loss

A compiler stage may erase information only when the erased distinction is no longer required by any legal consumer of
that stage.

A later consumer must not reconstruct erased Contract meaning from source shape or backend representation.

If later work still needs the distinction, the upstream legal observation must retain it or a separately owned compiler
result must preserve it before the loss occurs.

This law explains why target lowering cannot discard high-level Contract knowledge before Kontrakt has finished using
that knowledge.

It does not require lower IR to carry every earlier distinction forever.

---

## 16. Derived Analysis and Transformation

Analysis and transformation are compiler realization.

A transformation can make earlier analysis results stale. The compiler must not silently consume such a result after its
assumptions no longer hold.

This ADR does not require the LLVM or MLIR invalidation API.

The compiler may recompute a result.

It may update it.

It may invalidate it and rebuild later.

It may prove that the relevant result did not change.

It may also use an incremental repair strategy.

Any of those implementations is legal when stale knowledge cannot escape the producer's validity rule.

The Contract requirement remains that a legal compiler transformation preserve the meaning it is obligated to preserve.

Section 33 records the external engineering references for this rule.

---

## 17. Explicit Compiler Inputs Without Semantic Leakage

A deterministic compiler result must not depend on hidden implementation state.

When a compiler-specific condition can legally affect a result, the producer must own that condition as an explicit
compiler input.

A backend target is one example.

A selected compiler feature mode can be another.

Toolchain capability can also be explicit when the result depends on it.

An optimization mode may be explicit when legal output form changes.

A persistent format revision is an input when it affects whether retained material can be reused.

These compiler inputs do not become Contract determinants.

Uncontrolled environment state must not silently change a deterministic result.

Wall-clock time cannot enter the result unless the producer declares it.

Filesystem traversal order cannot alter the result either.

Process locale cannot silently change it.

Randomness must be owned explicitly when it is legal.

Mutable global state cannot become an undeclared determinant.

Cache presence cannot become one either.

Section 4 owns the determinism law. This section only states how compiler-specific determinants enter that law.

---

## 18. Visibility and Coherent Observation

An independently consumed result must become visible only after the producer has completed the guarantee exposed to
ordinary consumers.

A consumer must observe a compatible set of inputs for one computation.

Compatibility is a semantic and compiler-result requirement.

Matching one numeric generation identifier is not sufficient by itself.

Physical visibility remains implementation work. The compiler may use immutable generations or another safe publication
mechanism.

Reclamation is separate from semantic validity. Keeping old storage alive does not make it current. Reclaiming storage
does not undo a previously established semantic occurrence.

Section 28 keeps the physical mechanism replaceable.

---

## 19. Equality, Validity, and Reuse

Semantic equality answers what semantic meaning is the same. The owning semantic producer controls that law.

Compiler-result equality answers whether two results of one compiler result family are equivalent under that producer's
declared comparison.

Reuse validity answers whether retained material may satisfy the current request.

These are different questions.

A fingerprint may help answer a physical comparison question. It does not become semantic equality authority.

Byte equality is also not semantic equality by itself. Object identity is not semantic equality either.

A retained result can be semantically equal to a clean result and still be unusable because its compiler compatibility
has expired.

The reverse is also important. Physical identity never proves semantic equality by itself.

Sections 21 through 23 use this separation for cache and incremental work.

---

## 20. Dependency Recording Is Compiler Machinery

Compiler dependency tracking records work needed for recomputation and reuse. It does not define Contract dependency.

A Contract Basis relation remains distinct from a compiler computation edge.

HIR semantic relations remain distinct as well.

Build dependencies remain separate. Provenance remains separate. Physical reachability remains separate.

A previous dynamic dependency trace describes one previous computation. It cannot be treated as the complete semantic
law for future computations.

When control flow changes, later reads may also change. An incremental engine must therefore validate or rediscover
dependencies soundly.

The representation of the dependency graph remains Design.

---

## 21. Cache, Persistence, and Content Addressing

Cache avoids work. Persistence retains representation. Content addressing helps locate or validate retained
representation.

None of them creates Contract authority.

A retained item is usable only after the producer's current validity rule has been satisfied. Finding the bytes is not
enough.

A fingerprint or HID may accelerate lookup and validation. It does not become semantic identity authority.

BLAKE3 may be used to compute such evidence. Merkle summaries may support wider validation. CAS may retain reusable
representation. None of those mechanisms changes the owning meaning.

If retained state is missing or unusable, the compiler must have another legal path when the requested result is
otherwise computable.

Section 4.4 owns cache-blind correctness. Section 19 owns the equality and reuse distinction.

---

## 22. Incremental Compilation Follows the Architecture

V1 must leave a durable seam for future incremental work. It must not define one incremental algorithm as permanent
architecture.

A reusable major result therefore needs a stable logical subject. Its legal inputs must be knowable. Its producer must
also own the result comparison used by reuse.

A clean recomputation path remains the semantic reference when retained state is absent.

V2 may choose different repair strategies for different result families. A query result and a data-flow result do not
need the same incremental algorithm.

The incremental engine remains below the semantic boundary defined by the earlier ADRs.

Section 6 preserves the authority boundary. Section 20 preserves the dependency boundary.

---

## 23. Early Cutoff Is a Derived Optimization

An incremental engine may stop propagation when the producer has re-established that the result observed by downstream
consumers is unchanged.

An upstream source change alone does not prove that cutoff is legal. The owning result must first be validated,
recomputed, or repaired.

A fingerprint may accelerate that check only under a sound producer-owned comparison rule.

A hash collision must not redefine producer-owned equality.

This section does not change Contract equality. It only permits the compiler to avoid downstream work after the relevant
owned result is known to be unchanged.

---

## 24. Generated APIs and Backend Products

Generated host interfaces are downstream compiler artifacts. They do not create Contract authority.

The compiler may derive an intermediate plan before generation.

It may also fuse planning with generation.

The Contract-visible requirement is only that the generated surface preserve the meaning it is required to represent.

The backend follows the same direction. Backend structures consume legal upstream material. They do not reinterpret JVM
object shape as new Contract meaning.

Section 6 owns this authority separation. Section 15 owns information preservation before target lowering.

---

## 25. Whole-Machine and Summary-Driven Work

Whole-Machine compiler work may consume a sufficient summary instead of forcing every consumer to materialize complete
local bodies.

The summary remains a compiler result owned by its producer.

If a local change leaves that summary unchanged, a consumer that depends only on the summary may avoid recomputation
under Section 23.

A Whole-Machine Contract authority is different.

If Contract law establishes new Whole-Machine meaning, the relevant Contract ADR must own it. Summary infrastructure
cannot take that authority.

---

## 26. Resource Reality

Compiler work is finite. The compiler therefore needs explicit implementation limits that prevent uncontrolled resource
consumption.

Those limits are not automatically Contract Budget or Contract Capacity.

They belong to compiler realization unless an owning Contract law declares them as part of the user machine.

Resource exhaustion must not silently alter Contract meaning.

The compiler must use a legal fallback when one exists. Otherwise the relevant subsystem produces its compiler
unsuccessful result under ADR-0074.

An optimization may be abandoned when it exceeds its compiler limit. Required semantic work cannot be silently omitted
for that reason.

---

## 27. Query and Pass Orchestration Are Replaceable

V1 may use query-oriented orchestration for major reusable results.

Passes may still be used inside a producer when ordered local transformation is the suitable implementation.

Neither query topology nor pass order defines Contract authority.

A later compiler may change orchestration strategy without changing the semantic law, provided that the same legal
result boundaries remain observable.

Section 20 owns dependency recording. Section 22 owns the incremental seam.

---

## 28. Physical Representation Freedom

A logical result boundary does not imply one JVM object or one table.

The compiler may choose a representation that matches measured access patterns. It may also change that representation
later.

Physical fusion does not merge semantic ownership. Physical separation does not create new semantic entities.

The first implementation therefore does not become permanent architecture merely because it uses one particular layout.

ADR-0071 already applies the same principle to HIR.

ADR-0063 applies it to Established meaning.

This section extends that implementation freedom to compiler-derived results.

---

## 29. V1 Requirements

V1 must preserve Visible HIR as the deterministic pre-authority boundary defined by ADR-0071.

Establishment must consume only legal HIR observations and exact authoritative prerequisites permitted by the owning
law.

Established semantic material must be consumed through ADR-0063-owned observation surfaces.

Every major compiler result that is independently consumed must have an explicit producer and a complete
consumer-visible guarantee.

Derived compiler knowledge must remain non-authoritative under Section 6.

Deterministic results must remain independent of hidden compiler state under Section 4.

Ordinary consumers must not observe incomplete successful results under Section 18.

Compiler unsuccessful results must remain distinct from Contract negative meaning under Section 7 and ADR-0074.

Clean recomputation must remain correct without cache or persistence under Section 21.

Query choices must remain replaceable under Section 27.

Storage choices must remain replaceable under Section 28.

Verification must retain an independent or clean comparison path where such a path is required by the product family.

The unresolved authoritative IDL binding owner must not be invented by compiler architecture. Section 11 preserves that
open semantic ownership question.

V1 does not need to persist every result. It also does not need to incrementalize every computation.

---

## 30. V2 Evolution

V2 may add persistence and incremental repair behind the V1 boundaries.

Persistent HIR projections remain subject to ADR-0071. Persistent compiler products remain subject to their
producer-owned reuse laws.

Selective dependency validation may be added where it reduces work.

Incremental analysis may be added where the result family supports it.

Whole-Machine summaries may gain incremental repair.

Backend artifacts may also gain reuse.

Reader lifetime and reclamation may be optimized independently of semantic identity.

It may persist HIR observations when ADR-0071 compatibility rules permit that. It may also persist compiler-derived
results whose producer defines a safe reuse rule.

Different result families may choose different dependency and repair strategies.

The compiler may also add stronger summary reuse and backend artifact reuse where the owning result boundaries support
them.

These implementation changes must not require Contract ADRs to change merely because the compiler has gained a stronger
reuse engine.

Section 22 remains the governing architecture seam.

---

## 31. Adversarial Review

The following cases test whether the earlier laws are being violated.

### 31.1. Implementation Becomes Authority

A cache hit is treated as proof of Contract validity.

Rejected by Sections 6 and 21.

### 31.2. Consumer Creates Missing Contract Meaning

A backend adds a semantic field because backend generation needs it.

Rejected by Sections 5 and 6. The question returns to the owning semantic ADR.

### 31.3. HIR Pre-Establishes Authority

HIR computes a later authoritative result only to simplify Establishment.

Rejected by Sections 8 and 9.

### 31.4. Compiler Failure Masquerades as Contract Failure

Corrupt retained compiler data is reported as Contract Failure.

Rejected by Section 7 and ADR-0074.

### 31.5. Hidden Environment Changes Result

The same declared inputs produce another deterministic result because an undeclared environment condition changed.

Rejected by Sections 4 and 17.

### 31.6. Generated Artifact Becomes Source of Truth

A generated JVM signature is used to reconstruct Contract identity.

Rejected by Sections 6 and 24.

### 31.7. Old Dependency Trace Becomes Semantic Law

A previous dynamic read-set is treated as the complete future semantic dependency relation.

Rejected by Section 20.

### 31.8. Stale Derived Knowledge Survives Transformation

A transformation invalidates an analysis assumption and the old result is still consumed.

Rejected by Section 16.

### 31.9. Summary Becomes Authority

A Whole-Machine summary is treated as canonical Contract meaning.

Rejected by Sections 14 and 25.

### 31.10. Resource Exhaustion Changes Meaning

Compiler resource exhaustion causes required semantic material to be omitted.

Rejected by Section 26.

### 31.11. Physical Co-Location Creates Identity

Two semantic entities are treated as identical because they share physical backing.

Rejected by Sections 19 and 28.

### 31.12. Consumer-Specific Equality Rewrites Producer Meaning

A consumer ignores a producer-owned distinction and declares the complete producer result equal.

Rejected by Section 19. A legal projection or a separately owned summary is required instead.

---

## 32. Verification and QA Requirements

Verification must test the laws at the boundaries that own them.

A clean computation should be compared with a reused computation where reuse exists.

Cache state should be varied.

Parallel execution should be compared with the reference path when the result is deterministic.

Alternative legal schedules should also preserve the same declared result.

Persistent reload must be checked against clean recomputation.

Stale retained material must be rejected by the owning validity law.

Unsupported retained revisions must also be rejected by that boundary.

Corrupt retained material must follow ADR-0074 rather than changing semantic meaning.

Fingerprint and hash validation should include adversarial checks when those mechanisms participate in reuse evidence.

Where the compiler supports more than one physical representation, tests should confirm that the legal observation
remains the same.

This check covers changes in physical split and fusion as well as changes in storage form.

Optimized paths should be compared with an independent or reference path where the subsystem requires that assurance.

Hidden environment perturbation should be part of determinism testing when such state could accidentally enter the
result.

If a final artifact requires byte reproducibility, the backend must control every input that can legally affect those
bytes.

That requirement is stronger than semantic determinism. The artifact producer owns it.

---

## 33. Non-Normative Engineering Basis

The following systems support individual engineering principles. None defines Kontrakt Contract semantics.

### 33.1. LLVM New Pass Manager

LLVM keeps reusable analysis results separate from transforms. A transform must not leave stale analysis visible as if
it were still valid.

This supports Section 16. It does not require Kontrakt to copy LLVM's pass-manager API.

Source: <https://llvm.org/docs/NewPassManager.html>

### 33.2. MLIR Pass Infrastructure

MLIR also separates analysis lifetime from transformation. Its preservation rules provide another implementation example
of the stale-knowledge problem described in Section 16.

Source: <https://mlir.llvm.org/docs/PassManagement/>

### 33.3. rustc Query and Incremental Compilation

rustc incremental compilation assumes deterministic query results before reuse is considered. That supports the ordering
used by Sections 4 and 22.

rustc also tracks dynamic query dependencies.

A changed earlier read can change which later reads occur.

That supports the warning in Section 20 that an old dependency trace is not semantic law.

Sources:

- <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation.html>
- <https://rustc-dev-guide.rust-lang.org/queries/incremental-compilation-in-detail.html>

### 33.4. LLVM ThinLTO

ThinLTO demonstrates that global compiler work can use a compact derived summary instead of requiring every consumer to
load complete local IR.

This supports Sections 14 and 25.

Source: <https://clang.llvm.org/docs/ThinLTO.html>

### 33.5. Reproducible Builds and Nix

Reproducible-build work demonstrates the danger of undeclared build inputs. Nix derivations provide a related example of
making build inputs explicit.

These systems support Section 17 and the cache-blind rule in Section 4. They do not define Contract semantics.

Sources:

- <https://reproducible-builds.org/docs/deterministic-build-systems/>
- <https://reproducible-builds.org/docs/timestamps/>
- <https://wiki.nixos.org/wiki/Derivations>

### 33.6. Linux RCU and RocksDB Snapshots

RCU demonstrates that replacement and reclamation can be separate physical concerns. RocksDB snapshots demonstrate
explicit point-in-time read views.

These systems support the implementation freedom in Section 18. Their epoch or sequence identifiers are not semantic
identity in Kontrakt.

Sources:

- <https://docs.kernel.org/RCU/whatisRCU.html>
- <https://github.com/facebook/rocksdb/wiki/Snapshot>

### 33.7. DBSP and Enzyme

DBSP demonstrates principled incremental maintenance for computations that fit its model.

Enzyme shows that a production system may choose among refresh strategies.

These examples support product-specific incremental design rather than one universal algorithm.

These systems support Section 22. They do not define the Kontrakt incremental architecture.

Sources:

- DBSP: <https://www.vldb.org/pvldb/vol16/p1601-budiu.pdf>
- Enzyme: <https://arxiv.org/abs/2603.27775>

### 33.8. Verified Compilation and Reproducibility Research

Recent verified-compiler work provides evidence for checking implementation paths against independently specified
semantics. Reproducibility research provides related evidence for checking the integrity of artifact production.

These references support Section 32. They are not architecture templates.

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

Contract authority remains above compiler machinery.

Determinism is established before reuse and incremental optimization.

HIR and Established Semantic Protocols remain semantic observation boundaries rather than universal compiler APIs.

Compiler-derived knowledge gains an explicit owner without becoming a second Contract model.

Compiler failure can be represented without fabricating Contract Failure.

V1 can use practical in-memory compiler techniques while V2 remains free to adopt stronger persistence and incremental
repair.

Whole-Machine work can use summaries without moving authority into those summaries.

Backend replacement remains possible because target representation does not define Contract meaning.

### 35.2. Costs

Compiler subsystems must state ownership and consumer-visible guarantees more precisely.

Some direct reads from source or global compiler context become illegal shortcuts.

Reuse requires a real validity rule rather than cache presence.

Verification must compare alternative execution paths where a product family requires that assurance.

The unresolved authoritative IDL binding owner cannot be hidden inside compiler plumbing. It must be closed by the
semantic owner.

---

## 36. Required Follow-Up

The next work remains separated by ownership.

### 36.1. Contract / Establishment Follow-Up

The exact authoritative IDL or Interface slot-binding owner must be closed by the owning Contract law.

The 1D HIR-to-Establishment review must continue through the master checklist without adding compiler reuse metadata to
1D meaning.

### 36.2. Compiler Architecture Follow-Up

The major result producers from HIR through the JVM backend must be mapped.

Each independently consumed result must state its consumer-visible guarantee and its legal upstream inputs.

ADR-0074 integration must close the common compiler-result availability boundary.

### 36.3. Design Follow-Up

V1 must choose concrete query boundaries and storage representations.

The storage design must also decide where physical split or fusion is useful from measured access patterns.

The implementation must also choose its analysis-validity mechanism and its visibility mechanism.

Reuse evidence remains a Design decision under the laws in this ADR.

The fingerprint or HID strategy belongs there as well.

Summary granularity also remains Design.

### 36.4. V2 Research Follow-Up

V2 research should compare persistence strategies for the result families that benefit from them.

It should include persistent product loading and selective validation.

It should also compare dependency-repair strategies.

Incremental analysis needs its own evaluation. Summary repair needs another. Delta maintenance should be considered only
where the result family fits that model.

Adaptive repair versus rebuild should remain open.

Backend reuse should be evaluated under the same producer-owned validity model.

Persistent backend artifact reuse must not create a new semantic authority.

---

## 37. Summary

Kontrakt uses compiler result boundaries so later compiler responsibilities can consume already-formed meaning without
recreating semantic authority.

The compiler may derive new knowledge from legal observations. That knowledge remains compiler-owned.

Deterministic results remain independent of replaceable physical machinery under Section 4.

Contract and implementation remain separate under Section 6.

Compiler failure remains separate from Contract meaning under Section 7.

Reuse and incremental compilation build on those laws under Sections 19 through 23.

This separation allows the compiler to become more aggressive without making its current implementation part of the
Contract.