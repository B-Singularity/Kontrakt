# ADR-0075: Compiler Product Protocol, Legal Consumption, Deterministic Realization, and Contract/Implementation Separation

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

The compiler nevertheless contains responsibilities that produce material or results for other responsibilities to use.
ADR-0071 already defines one such boundary for Resolved Contract HIR, while ADR-0063 defines the observation of
Established meaning. ADR-0074 separately owns the common boundary for compiler-side unsuccessful results and recovery.
These are important applications of a broader compiler problem rather than the complete scope of this ADR.

The same producer-consumer problem can appear elsewhere as the compiler develops. A realization analysis may publish a
result that verification consumes, and a later IR producer may expose material that lowering consumes. Future persistent
products may cross compilation sessions while preserving the same logical ownership boundary. The common requirement is
that a consumer can rely on a completed producer-owned result without reopening upstream meaning or depending on the
producer's private implementation state.

This ADR defines that compiler-wide product protocol. It fixes the ownership and legal observation relation that must
survive changes in compiler structure. Caching and incremental compilation may exploit this protocol, but neither
defines it.

---

## 2. Decision

Kontrakt will use producer-owned compiler product boundaries when completed material or a completed result crosses from
one compiler responsibility to another. The producing responsibility owns what it publishes and exposes the legal
observation surface on which another responsibility may rely.

A compiler product in this ADR is therefore not every intermediate value produced during compilation. Private scratch
state stays private to the responsibility that owns the computation. Material enters this protocol only when it is made
available across that responsibility boundary for independent observation or consumption.

A consumer may use the published observation and derive another compiler-owned result for its own responsibility. It may
not gain the producer's authority by consuming the result, and it may not recover missing Contract meaning from
producer-private state. When the consumed material carries Contract meaning, the owning Contract ADR continues to define
that meaning.

The protocol is compiler-wide without imposing one universal `CompilerProduct` semantic type. Different product families
may expose different legal observations because their producers own different guarantees. The physical access method
remains replaceable as long as the same legal observation is preserved.

The local relation is:

```text
producer responsibility
    ↓ publishes a completed legal observation
consumer responsibility
    ↓ may derive
consumer-owned compiler result
```

This relation does not imply a global compiler pipeline. It states only the direction of ownership across one product
boundary.

---

## 3. Protocol Relations and Topology Freedom

ADR-0075 does not define a universal compiler layer stack. It does not fix the number of IR levels or require every
legal product to be reached through one mandatory traversal path. Those choices belong to compiler architecture and
Design rather than to this protocol.

The absence of a fixed global topology does not remove local prerequisite law. Resolved Contract HIR still precedes the
Establishment judgment that legally consumes it because ADR-0071 and ADR-0063 define that relation. Other product
families may define their own prerequisites through their owning architecture or semantic law. ADR-0075 preserves those
relations without combining them into one compiler-wide linear order.

Compiler Design may later revise an IR family or introduce another internal level. A subsystem boundary may also change
as the compiler evolves. None of those changes is legal if it breaks the producer-consumer protocol or an applicable
prerequisite owned elsewhere.

A logical product boundary also does not require a separate physical copy. Its physical realization may change when the
required observation remains unchanged. Section 6 defines the Contract and implementation separation that constrains
that freedom, while later sections define the validity and reuse laws that apply when an earlier product is retained or
restored.

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

The stronger common question is not yet closed in this Proposed ADR: whether every protocol-visible Compiler Product
must
provide deterministic legal observation for the same complete determining inputs, rather than determinism being an
optional property of a product family. The answer must remain compatible with HIR determinism, clean recomputation,
legal
parallel schedules, and the separation between Contract determinants and compiler-owned inputs.

### 4.3. Physical Schedule Freedom

Determinism constrains the result rather than forcing one physical schedule. The compiler may reorder or parallelize
independent work, provided that every legal schedule exposes the same deterministic result to the consumer where the
product's determinism law applies.

### 4.4. Cache-Blind Correctness

Cache state can change how much work the compiler performs, but it cannot decide meaning. A cold and a warm cache must
therefore expose the same legal result for the same current inputs whenever reuse is legal. Section 21 owns the storage
and cache consequences of this law.

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
judgment. A stale retained result is a compiler-validity problem, while an internal compiler defect remains an ADR-0074
compiler result.

Compiler resource limits are also realization concerns unless an owning Contract law explicitly gives them Contract
Budget or Capacity meaning. Optional optimization work may be abandoned when its compiler limit is reached. Required
semantic work must not be silently omitted for that reason. When the compiler cannot continue and no legal fallback
exists, the unsuccessful compiler result remains owned by ADR-0074 rather than being fabricated as Contract meaning.

ADR-0074 owns the common representation of compiler unsuccessful results and recovery. This ADR requires only that those
results remain separate from Contract meaning. The same separation applies to materialization: material that is not
currently materialized, or was never retained, is not thereby semantically absent.

Because ADR-0074 is still Proposed in the current project material, the unsuccessful-result, recovery, trust-loss, and
boundary-availability portions of this section remain dependent on that ADR until its ownership is accepted or moved.

---

## 8. HIR Producer Boundary

ADR-0071 remains the owner of Resolved Contract HIR and of the HIR subjects and HIR projection families defined at that
boundary. Establishment must consume HIR through the legal observation boundary defined by ADR-0071 rather than
reopening source syntax or inferring Candidate meaning from physical storage.

HIR remains pre-authority material, so observing it creates no Contract authority. The physical storage used to expose
that observation remains an implementation detail under ADR-0071 and the physical-freedom law in Section 3 of this ADR.

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

## 12. Compiler Product Boundary

A compiler product boundary belongs to compiler architecture. For a major product, the boundary must identify the
logical
subject, the producer that owns it, and the guarantee on which a legal consumer may rely.

When the product has compiler-specific inputs, those inputs must be explicit enough to support deterministic
recomputation where determinism is required. The producer must also expose a legal read surface for the completed
result.
When the product can be unavailable, its unsuccessful-result boundary must remain explicit under ADR-0074.

When reuse compares an earlier result with a current one, the producer owns the comparison rule for that result family.
Information loss must likewise remain visible whenever later consumers depend on the lost distinction. These
requirements
apply to independently consumed products rather than to private scratch state.

This boundary does not require one query node, cache entry, persisted record, physical allocation, or object per
Compiler
Product. Those are realization choices. ADR-0074 owns the common unsuccessful-result protocol. Information loss is
governed by Section 15, compiler inputs by Section 17, and reuse distinctions by Section 19.

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
semantic authority. If a consumer only needs the summary, it may avoid materializing the full producer body. A
Whole-Machine compiler summary remains compiler-owned derived material; it does not acquire Whole-Machine Contract
authority merely because wider compiler work depends on it.

The exact summary representation remains Design unless another compiler ADR gives it an explicit architecture boundary.
Its persistence policy, update strategy, query key, and physical granularity remain Design for the same reason.

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
preserve the meaning it is obligated to preserve. This ADR does not prescribe one invalidation or repair API.

---

## 17. Explicit Compiler Inputs Without Semantic Leakage

A deterministic compiler result must not depend on hidden implementation state. Any compiler-specific condition that may
legally affect the result must therefore be owned by the producer as an explicit compiler input. A backend target is one
such input when the result depends on the selected target.

The same rule applies to a selected compiler feature mode or toolchain capability when the result depends on it. An
optimization mode must be explicit when it changes the legal output form.

A persistent format revision can affect whether retained material is reusable without necessarily changing the result
that clean computation would produce. The common protocol must therefore keep such reuse compatibility from silently
becoming Contract meaning or an undeclared result determinant. This Proposed ADR has not yet closed whether
result-determining inputs and reuse-only compatibility inputs should be modeled as two explicit protocol classes or as
one input family with distinct producer-owned roles.

Wall-clock time can affect the result only when the producer declares it. Filesystem traversal order and process locale
must not silently change the outcome. Randomness likewise needs an explicit owner when it is legal. Mutable global state
and cache presence cannot become undeclared determinants.

Section 4 owns the determinism law. Section 19 owns the distinction between equality and reuse validity. This section
only constrains how compiler-specific conditions may legally participate in those laws.

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
storage does not undo a semantic occurrence that was previously established. Section 3 keeps the physical mechanism
replaceable.

---

## 19. Equality, Validity, and Reuse

Semantic equality answers whether semantic meaning is the same and is controlled by the owning semantic producer.
Compiler-product equality asks a different question: whether two results in one compiler product family are equivalent
under the comparison declared by that producer.

Reuse validity asks whether retained material may satisfy the current request, which is different from either equality
judgment. A fingerprint may support a physical comparison, but it does not become semantic equality authority.

Byte equality is also not semantic equality by itself. Object identity is not semantic equality either. A retained
result can be semantically equal to a clean result and still be unusable because its compiler compatibility has expired.
The converse also holds: physical identity does not prove semantic equality.

ADR-0071 already permits one HIR product to expose producer-defined projections with their own producer-owned equality
relations. The compiler-wide generalization remains open in this Proposed ADR: whether every product family may expose
legal observation or projection equality distinct from complete-product equality, and how that distinction controls
downstream reuse, must be closed without allowing consumers to redefine producer meaning.

Sections 21 through 23 use the established distinction between equality and reuse validity. They do not settle that open
granularity question by themselves.

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

ADR-0071 already prevents HIR dependency observation from becoming permanently tied to slab offsets, object fields,
table rows, or other incidental storage boundaries. The compiler-wide boundary is not yet fully closed here: this
Proposed ADR must still decide whether cross-responsibility dependencies are required to name producer-owned legal
Product or Projection observations while allowing each producer to keep finer-grained dependency machinery private.
That decision must preserve future incremental freedom without turning physical layout into protocol law.

---

## 21. Cache, Persistence, and Retained Products

Cache and persistence support reuse in different ways. Cache avoids repeated work, while persistence keeps a
representation available across a wider lifetime. A retained representation may also carry compiler-owned evidence that
helps locate or validate the product.

None of those mechanisms creates Contract authority. Retained material is usable only after the producer's current
validity law has succeeded; finding stored material is not enough. A fingerprint, content key, or other reuse evidence
may accelerate lookup or validation without becoming semantic identity or equality authority.

If retained state is missing or unusable, the compiler needs another legal path whenever the requested result is
otherwise
computable. HIR retention and persistence remain subject to ADR-0071's HIR-specific compatibility law rather than being
redefined here. Section 4.4 owns cache-blind correctness, while Section 19 owns the distinction between equality and
reuse validity. Concrete hash algorithms, content-addressing structures, cache tiers, persistence stores, and eviction
policies remain Design.

---

## 22. Incremental Compilation Follows the Architecture

V1 must leave a durable seam for future incremental work without making one incremental algorithm permanent
architecture.
A reusable major result therefore needs a stable logical subject, knowable legal inputs, and a producer-owned comparison
rule sufficient for the reuse law that applies to that result family.

A clean recomputation path remains the semantic reference when retained state is absent. V1 is not required to persist
every result or incrementalize every computation. V2 may choose different repair strategies for different result
families, and a query result and a data-flow result do not need the same incremental algorithm.

A stronger reuse engine must remain below the semantic boundaries defined by the earlier ADRs. Adding persistence,
selective validation, delta maintenance, or another repair strategy must not require a Contract ADR to change merely
because the compiler implementation became more capable. HIR persistence remains governed by ADR-0071, while other
compiler products remain governed by their producer-owned product and reuse laws.

Section 6 preserves the authority boundary, and Section 20 preserves the dependency boundary. Concrete scheduling,
repair/rebuild thresholds, persistent state layout, and incremental algorithms remain Design.

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

---

## 24. Consequences

The protocol keeps Contract authority with the owning Contract law while allowing compiler responsibilities to publish
completed products that other compiler work may consume. HIR and Established semantic observation retain their own
owners instead of becoming instances of one universal compiler schema. Derived analysis, summaries, backend products,
and future retained products can therefore evolve without creating a second Contract model.

The cost is that compiler product families must state ownership and consumer-visible guarantees explicitly. Reuse cannot
be justified by cache presence, physical identity, or a query edge alone, and stale derived knowledge cannot remain
visible merely because its storage still exists. Some direct reads from source, ambient compiler state, or
producer-private
construction state become illegal shortcuts.

This Proposed ADR is not ready for acceptance until the remaining common protocol questions inside Sections 4, 17, 19,
and 20 are closed and the ADR-0074 unsuccessful-result ownership on which Sections 7 and 12 depend is accepted or
reassigned. Concrete cache, storage, dependency-graph, scheduling, verification, and incremental algorithms remain in
Design, research, or QA material rather than becoming requirements of this ADR.