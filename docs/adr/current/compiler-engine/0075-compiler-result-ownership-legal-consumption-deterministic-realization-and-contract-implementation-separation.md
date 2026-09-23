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

## 2. Decision and Scope

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
required legal observation remains unchanged. Later sections define the product-specific validity and reuse laws without
turning one current representation into permanent architecture.

---

## 4. Existing Project Constraints

ADR-0075 assumes the project-wide Contract and realization boundaries already established by *What Contract Is* and the
owning Contract ADRs. Compiler machinery does not acquire Contract authority, downstream compiler convenience does not
enlarge upstream Contract meaning, and replaceable query, cache, storage, scheduling, or backend machinery cannot become
a hidden source of Contract meaning.

A compiler responsibility may derive compiler-owned knowledge from legal upstream observations, but that derivation does
not transfer or recreate the upstream authority. ADR-0075 does not restate the semantic details of those laws. It uses
them only as constraints on the Compiler Product Protocol defined below.

The protocol-specific questions of compiler-product determinism, product inputs, equality, validity, and dependency
observation are not closed by this project-level constraint. They remain owned by the corresponding sections of this
ADR.

---

## 5. Existing Protocol Boundaries

ADR-0075 does not redefine the protocols already owned elsewhere. Resolved Contract HIR is observed under ADR-0071.
Established semantic material is observed under ADR-0063 and the owning authority-specific ADR. Compiler-side
unsuccessful results, recovery, trust loss, and non-entry remain owned by ADR-0074. Any authoritative Interface, Policy,
Governance, Version, or other Contract relation remains with its owning Contract law.

A Compiler Product may consume those legal observations, but it cannot fill a missing semantic relation, reopen an
upstream representation to reconstruct meaning, or reinterpret an unsuccessful compiler result as Contract meaning.
Where an upstream authority is still unresolved, ADR-0075 leaves that question unresolved rather than absorbing it into
compiler plumbing.

ADR-0074 is still Proposed in the current project material. The unsuccessful-result and availability portions on which
this ADR depends therefore remain provisional until that ownership is accepted or reassigned.

---

## 6. Compiler Product Subject, Identity, and Boundary

A compiler product boundary belongs to compiler architecture. For a major product, the boundary must identify the
logical subject, the producer that owns it, and the guarantee on which a legal consumer may rely.

The producer must expose a legal observation surface for the completed product. When the product can be unavailable, its
unsuccessful-result boundary must remain explicit under ADR-0074. Product inputs and the common determinism law are
owned by Section 11 rather than being defined indirectly by this boundary.

When reuse compares an earlier result with a current one, the producer owns the comparison rule for that result family.
Information loss must likewise remain visible whenever later consumers depend on the lost distinction. These
requirements
apply to independently consumed products rather than to private scratch state.

This boundary does not require one query node, cache entry, persisted record, physical allocation, or object per
Compiler Product. Those are realization choices. ADR-0074 owns the common unsuccessful-result protocol. Information loss
is governed by Section 9, compiler inputs by Section 11, and reuse distinctions by Section 13.

This Proposed ADR has not yet closed the exact product identity protocol. In particular, the roles of product family,
logical subject, product identity, product generation or occurrence, and their separation from HID, query keys, cache
keys, persistence keys, and physical addresses remain to be decided here rather than inferred from the current
implementation.

---

## 7. Legal Consumption

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

## 8. Projection, Derivation, and Summary

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

## 9. Information Preservation and Loss

A compiler stage may erase information only when the erased distinction is no longer required by any legal consumer of
that stage. A later consumer must not reconstruct erased Contract meaning from source shape or backend representation.
If later work still needs the distinction, the upstream legal observation must retain it or a separately owned
compiler result must preserve it before the loss occurs.

This law explains why target lowering cannot discard high-level Contract knowledge before Kontrakt has finished using
that knowledge. It does not require lower IR to carry every earlier distinction forever.

---

## 10. Derived Analysis and Transformation Validity

Analysis and transformation belong to compiler realization, but a transformation may invalidate assumptions used by an
earlier analysis. Once that happens, the compiler must not keep consuming the stale result as though it were still
valid.

This ADR does not require the LLVM or MLIR invalidation API. The producer may recompute or update the result, invalidate
it for later rebuilding, or prove that the relevant result did not change.

Incremental repair is another legal strategy. Whichever mechanism the producer chooses, stale knowledge must remain
inside the producer's validity boundary. The Contract requirement is unchanged: a legal compiler transformation must
preserve the meaning it is obligated to preserve. This ADR does not prescribe one invalidation or repair API.

---

## 11. Compiler Product Inputs and Determinism

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

The common product determinism law is not yet closed. This ADR still has to decide whether every protocol-visible
Compiler Product must provide the same legal observation for the same complete determining inputs, how legal
nondeterministic sources such as time or randomness are represented when they are intentionally admitted, and which
conditions affect clean product formation rather than only retained-product compatibility.

Section 13 owns the distinction between equality and reuse validity. This section must close the input and determinism
side of that relation without turning compiler configuration or compatibility metadata into Contract determinants.

---

## 12. Completion, Visibility, and Coherent Observation

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

## 13. Equality, Validity, and Reuse

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

Sections 15 through 17 use the established distinction between equality and reuse validity. They do not settle that open
granularity question by themselves.

---

## 14. Dependency Observation

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

## 15. Retention and Persistence

Cache and persistence support reuse in different ways. Cache avoids repeated work, while persistence keeps a
representation available across a wider lifetime. A retained representation may also carry compiler-owned evidence that
helps locate or validate the product.

None of those mechanisms creates Contract authority. Retained material is usable only after the producer's current
validity law has succeeded; finding stored material is not enough. A fingerprint, content key, or other reuse evidence
may accelerate lookup or validation without becoming semantic identity or equality authority.

If retained state is missing or unusable, the compiler needs another legal path whenever the requested result is
otherwise
computable. HIR retention and persistence remain subject to ADR-0071's HIR-specific compatibility law rather than being
redefined here. Cache state must not alter the legal current result, while Section 13 owns the distinction between
equality and reuse validity. Concrete hash algorithms, content-addressing structures, cache tiers, persistence stores,
and eviction policies remain Design.

---

## 16. Incremental Evolution

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

Section 4 preserves the inherited authority constraint, and Section 14 owns the compiler dependency boundary. Concrete
scheduling,
repair/rebuild thresholds, persistent state layout, and incremental algorithms remain Design.

---

## 17. Early Cutoff

An incremental engine may stop propagation when the producer has re-established that the result observed by downstream
consumers is unchanged. An upstream source change alone does not prove that cutoff is legal. The owning result must
first be validated, recomputed, or repaired.

A fingerprint may accelerate the check only under a sound producer-owned comparison rule, and a hash collision cannot
redefine that equality. This section leaves Contract equality unchanged; it permits only the compiler to avoid
downstream
work once the relevant owned result is known to be unchanged.

---

## 18. Consequences

The protocol keeps Contract authority with the owning Contract law while allowing compiler responsibilities to publish
completed products that other compiler work may consume. HIR and Established semantic observation retain their own
owners instead of becoming instances of one universal compiler schema. Derived analysis, summaries, backend products,
and future retained products can therefore evolve without creating a second Contract model.

The cost is that compiler product families must state ownership and consumer-visible guarantees explicitly. Reuse cannot
be justified by cache presence, physical identity, or a query edge alone, and stale derived knowledge cannot remain
visible merely because its storage still exists. Some direct reads from source, ambient compiler state, or
producer-private
construction state become illegal shortcuts.

This Proposed ADR is not ready for acceptance. The protocol still has to close product subject and identity, the common
input and determinism model, projection and product equality, and the cross-responsibility dependency observation
boundary. The ADR-0074 unsuccessful-result ownership on which this protocol depends must also be accepted or reassigned.
Concrete cache, storage, dependency-graph, scheduling, verification, and incremental algorithms remain Design, research,
or QA work.