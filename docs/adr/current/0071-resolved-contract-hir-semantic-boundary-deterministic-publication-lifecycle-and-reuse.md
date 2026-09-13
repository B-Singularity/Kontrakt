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

A 1D role is not inferred from the shape of a user API declaration. The explicit IDL binding selects the declared
material for one exact 1D role. HIR admission occurs only after that role-qualified material can be interpreted under
the
owning 1D frontend law.

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

## 4.5. Exact Candidate References Before Authority

HIR references are exact compiler-semantic references to current resolved candidate targets.

They are not automatically the authoritative `Definition Reference` defined by ADR-0063.

```text
authored Contract material
    ↓ IDL role binding / resolution
exact HIR candidate target
    ↓ owning Establishment / Composition law
possible authoritative relation
```

An HIR candidate reference denotes the resolved semantic target. It is not the source AST node, file position, host
class object, content hash, generation-local ordinal, or physical address used while producing that target. Source
material remains available through provenance when required.

The reference is current-world material. It does not inherit identity from a previous HIR generation and it does not
carry a lineage relation. A later generation resolves its own exact candidate references from its own explicit inputs.

This distinction matters when two candidates in the same frontend generation refer to one another before either has
received Contract authority. The compiler may know exactly which candidate is referenced. That exactness does not
establish the target or the relation.

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

## 4.9. Authoring Refinement and Semantic Convergence

Resolved HIR is source-independent enough that different authoring forms may converge on the same consumer-visible
candidate meaning when the owning frontend and 1D laws say that their resolved meaning is the same.

Frontend refinement may remove alias spelling, import spelling, source-only nesting, authoring sugar, host-carrier
structure, or another distinction whose semantic interpretation has already been resolved. The original form may remain
available through provenance or source material.

Frontend refinement must not collapse a distinction owned by a Contract law. It must not perform value
Canonicalization, Admission judgment, Fact Establishment, Applicability judgment, or another authority-owned decision in
order to obtain a convenient HIR form.

If two authored forms lead to different candidate meaning under an owning 1D ADR, HIR preserves that difference. If
they lead to the same resolved candidate meaning, their authoring route does not create a second HIR meaning.

This is compiler frontend refinement. It is not the `Canonicalization Contract`, and it does not require one byte-level
canonical HIR storage format.

If order is not semantic, physical construction order must not become observable meaning. If order is semantic, its
source must be explicit in the owning law or frontend language.

## 4.10. HIR Semantic Determinants and Frontend Confluence

The observable meaning of one HIR semantic unit must be determined by explicit semantic inputs.

Those inputs include the role-qualified declared material and the exact resolution environment required to interpret
that material. Binding context contributes to Definition Candidate meaning only when the owning 1D law declares that
context to be Definition-determining. A frontend language or semantic profile version participates only where its owning
law makes it relevant to candidate meaning.

Worker scheduling, cache state, allocation order, current memory layout, compiler traversal order, and the route by
which a query happened to be computed are not semantic determinants.

Semantic determinants are not the same thing as compiler reuse-validity inputs. A compiler schema version, frontend
implementation revision, persistent-product encoding, or another compiler-owned input may invalidate a cached physical
product without becoming part of Contract candidate meaning.

Different supported authoring forms may converge on the same HIR meaning. When `.kontrakt` source and a selected 1D
carrier express the same role-qualified resolved candidate meaning under the same applicable frontend law, the authoring
route does not create a second HIR meaning. Their source origin may remain different provenance.

This confluence rule prevents frontend implementation choice from leaking into Establishment, equality, or reuse.

## 4.11. Typed Extension Boundary

HIR has shared compiler infrastructure, but each Contract authority keeps its own candidate vocabulary.

Adding a new 1D Contract family must not require existing candidate meanings to be rewritten into a weaker universal
property model. The shared HIR substrate may provide references, publication, provenance relations, Binding
representation, physical partitioning, and projection access. The new authority provides the semantic payload that its
own ADR defines.

A generic tag or property bag may exist as physical encoding. It is not the semantic contract of HIR.

## 4.12. Authored Contract Material and 1D Role Grant

A user API declaration is an authoring carrier. Its class shape, host type, or declaration object does not by itself
make
that material a 1D Contract.

The explicit IDL binding grants the 1D role used for frontend refinement. The frontend then interprets the selected
material under that 1D law and forms resolved candidate meaning.

```text
user-authored carrier
    ↓ explicit IDL binding
role-qualified declared material
    ↓ 1D frontend refinement
Resolved HIR candidate material
```

An unselected carrier does not become a 1D HIR candidate merely because it has a supported user API shape. It may remain
ordinary source material.

This rule keeps the authoring representation replaceable. A future frontend may express the same role-qualified meaning
without using the current host-language carrier shape.

## 4.13. Definition Candidate and Binding Candidate

HIR separates reusable Definition Candidate meaning from the exact IDL use that selects it.

A **Definition Candidate** is the resolved 1D definition meaning determined by the owning 1D law.

A **Binding Candidate** is the exact current frontend relation by which one IDL context selects a Definition Candidate
for one 1D slot or role. In this ADR, `Binding Candidate` names that IDL-use relation. It is not `Basis Binding`,
`Version Binding`, Governance Binding, or another authority-bearing Binding established later.

```text
IDL A ── Binding Candidate A ──┐
                               ├──> Definition Candidate D
IDL B ── Binding Candidate B ──┘
```

Several IDL contexts may select the same authored declaration. When the owning 1D law says that the differing context
does not change Definition meaning, those uses may resolve to one reusable Definition Candidate while retaining distinct
Binding Candidates.

The binding does not copy or inherit Definition identity. It preserves an exact relation to its target candidate.

## 4.14. 1D-Owned Definition Determinants

HIR does not impose one universal coordinate tuple on every 1D Definition Candidate.

The owning 1D law decides which declared material and which contextual material determine Definition meaning. Context
that does not determine Definition meaning remains Binding, Required Basis, Applicability, attribution, provenance, or
other separately owned material.

If one 1D law makes an Operation, Machine, Interface, Version Claim, or another context part of Definition meaning, that
context participates in the Definition Candidate for that 1D. If the law does not make it Definition-determining, HIR
must not add it merely to obtain a convenient global key.

The shared HIR reference protocol therefore does not require every candidate to contain the same stored fields. Typed
reference domains may carry authority kind structurally. Version material and authority-local coordinates appear only
where the owning semantic law requires them.

The exact component set for each 1D Definition Candidate Reference remains to be completed with the owning 1D ADRs.

## 4.15. Current Identity and History Boundary

HIR current meaning does not depend on predecessor identity.

Identity is not inherited, transferred, or continued from an earlier HIR generation. A current candidate reference is
resolved from current explicit inputs. The previous compiler generation is not an identity source.

Compiler infrastructure may compare a previous product with a current product for reuse, diagnostics, or change
analysis. That comparison does not create semantic lineage and it does not establish current identity.

When historical, replacement, succession, or transition meaning is itself part of Contract semantics, the Contract that
owns that meaning establishes an explicit relation between independently identified material. Governance Replacement,
State Transition, or Version-owned history does not create HIR identity continuity.

A missing previous product may reduce reuse. It must not reduce the semantic completeness of current HIR.

## 4.16. Logical HIR Formation Order

HIR formation has an ordered logical dependency even when one physical implementation fuses several steps into one
tight loop.

```text
Authored Contract Material
    +
Explicit IDL Slot Selection
        ↓
Exact Source / Symbol Resolution
        ↓
Resolved 1D Role Selection
        ↓
1D-Owned Definition Determinant Projection
        ↓
Authority-Specific Authoring Refinement
        ↓
Definition Candidate Reference and Meaning Formation
        ↓
IDL Binding Candidate Completion
        ↓
HIR Verification
        ↓
Published Resolved Contract HIR
```

Role selection precedes 1D interpretation. The frontend does not infer a role from carrier shape and then treat the
inference as IDL meaning.

Definition determinant projection follows role selection because only the owning 1D law can decide which surrounding
context changes Definition meaning. Context that is not Definition-determining remains outside the Definition Candidate.

A complete Binding Candidate is formed only after its exact Definition Candidate target exists. Before that point the
frontend may hold a resolved slot selection and the minimum role-qualified formation input, but that working relation is
not a partially published Binding Candidate.

The logical stages do not require one heap object, one IR level, or one materialized table per stage. They state the
invariants that a fused, lazy, direct-to-slab, or future incremental realization must preserve.

Definition, Binding, and Occurrence remain different semantic categories. Definition Formation does not establish an
Occurrence. Occurrence meaning, when an owning Contract has one, is created only after the later basis, applicability,
and occurrence judgment required by that law.
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

Resolved HIR must be sufficient for the owning Definition Establishment judgment without reopening authored source.

The Definition Candidate carries the resolved 1D meaning that the owning law must judge. The Binding Candidate carries
the exact current IDL use that selected that candidate. These materials are related but they are not the same semantic
product.

```text
Resolved Definition Candidate
    +
Definition-time semantic basis allowed by the owning law
    ↓
Authority-Owned Definition Establishment
    ↓
Established Definition
```

Binding, Required Basis, Applicability, and occurrence-specific work remain at the boundaries that own those meanings.
A Binding Candidate does not become an Established Occurrence merely because its target Definition is established.

When an owning 1D law gives a concrete application separate semantic meaning, the later path is conceptually:

```text
Established Definition
    +
exact application context
    +
Required Basis resolution / Basis Binding where owned
    +
Applicability judgment where owned
        ↓
Occurrence judgment
        ↓
Established Occurrence
```

Not every 1D requires an Established Occurrence. The owning Contract law decides whether application meaning exists as a
separate semantic product.

If a 1D law makes some IDL binding context Definition-determining, that context has already participated in Definition
Candidate formation under Section 4.14. Establishment does not rediscover that rule from IDL topology.

Establishment does not repair missing frontend resolution.

HIR publication does not establish authority. Establishment does not become a compiler refinement or representation
pass. Canonical Contract World publication remains a later compiler representation of already-established Definition
meaning.

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
A Definition Candidate, Binding Candidate, interface-level surface, interaction-level surface, or another meaningfully
independent projection may be consumed separately when the HIR semantics support that separation.

The exact projection catalog remains a compiler design decision.

A projection is not a new Contract authority and is not automatically a new IR level.

## 8.3. Reference Direction and Ownership

A resolved HIR reference denotes another semantic subject. It does not physically own that subject merely because the
source syntax was nested or one declaration mentioned another.

The compiler may store related subjects next to one another, but consumers must observe an exact reference relation
rather than infer semantic ownership from containment, parent pointers, object nesting, or table adjacency.

This keeps source hierarchy, semantic reference, and physical storage as separate concerns. It also permits one
definition-level product to be reused without copying every referenced definition into the same physical object graph.

## 8.4. Contract Structure and Compiler Partition

HIR does not invent a new semantic ownership hierarchy when the Contract structure already provides the relevant
Definition and Binding relations.

Reusable Definition Candidate meaning remains defined by its 1D law. An IDL use is represented by a distinct Binding
Candidate. Source nesting and compiler storage grouping do not change either relation.

The compiler may still partition HIR for formation, publication, reuse, lifetime, or locality. A partition is compiler
organization. It may group several semantic units or isolate one unit when that is profitable. It does not create
Contract ownership, Definition identity, or Binding meaning.

This separation lets physical partitions evolve with measured compiler needs without forcing semantic consumers to
adopt the same topology.

The exact physical partition catalog remains implementation and product-design work.

## 8.5. Exact Semantic Reference and Generation-Local Dense Handle

Exact HIR reference meaning and fast in-generation addressing are different requirements.

A Definition Candidate Reference or Binding Candidate Reference denotes one exact current semantic target. A physical
realization may map that reference to a compact generation-local dense handle for hot access.

```text
exact current HIR reference
    ↓ current-generation mapping
dense primitive handle
    ↓
typed slab / table / segment
```

The dense handle is not semantic identity. Its numeric value is not observable HIR meaning and it is valid only in the
generation that assigned it. A later generation may assign another handle to a reference that resolves to equivalent
current meaning.

HID, fingerprint, hash, or persistent product keys may accelerate lookup, comparison, or remapping. They are neither the
semantic reference nor the dense handle. A slab offset, page number, segment address, or memory address is a still lower
physical coordinate.

A persistent product must not serialize a raw dense handle as though it were a semantic reference. Restoration resolves
or validates current semantic reference material and then obtains a current-generation handle.

No cross-generation lineage identity is introduced by this mapping.

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

The exact protocol shape is not fixed here. Projection catalog, physical partition granularity, bulk-access forms,
handle encoding, and storage-specific fast paths remain later design work.

---

# 9. HIR Identity and Equality

HIR keeps exact current reference, candidate meaning, and semantic equality separate.

A Definition Candidate Reference or Binding Candidate Reference tells the compiler which current semantic target is
being addressed. Reference equality does not by itself prove equality of the complete candidate meaning. Semantic
equality is defined by the resolved candidate meaning exposed by the relevant HIR surface.

HIR candidate reference is not the authoritative Contract `Definition Reference` or `Occurrence Reference`. Those
meanings remain owned by Establishment and the relevant Contract law.

HIR semantic equality is also not compiler generation identity, source location, source revision, table position, dense
ordinal, JVM object identity, HID, or fingerprint.

A new HIR generation may carry semantic projections equal to a previous projection. That fact is established by current
resolution and equality validation, not by inherited identity. A source move may change provenance while leaving a HIR
semantic projection equal. A different physical layout may carry the same HIR meaning.

Fingerprints and HID may provide efficient equality evidence. They remain implementation mechanisms. A collision or
cache lookup must never be allowed to establish a false semantic equality.

The exact collision-safe comparison strategy remains outside this ADR.

Equality is defined at the semantic surface being consumed. Whole-generation inequality does not imply that every
Definition Candidate, Binding Candidate, or projection is unequal. A projection family must therefore have an equality
law strong enough to decide whether its own consumer-visible meaning changed. Generic object equality or serialized-byte
equality is not the semantic law unless the owning HIR product explicitly makes that representation canonical.

HIR carries no lineage identity. Previous-product comparison is optional compiler reuse material and does not
participate
in current semantic identity.

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

One semantic subject may have more than one relevant authored origin. A refined or synthesized HIR subject may also
need an origin chain that points to the source material from which the compiler formed it. HIR therefore does not assume
a mandatory one-subject-to-one-span provenance model.

Synthetic compiler material that has no direct authored token must still be distinguishable from missing provenance.
Its provenance may identify the semantic source that caused synthesis without making that source location part of HIR
equality.

---

# 11. Publication

## 11.1. Frontend Working State

Before Resolved HIR is published, the frontend may keep private working state while it resolves and refines authored
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

The compiler may publish a generation as a stable manifest over independently sealed semantic units or projections.
A unit becomes observable only after that unit satisfies the HIR invariant and is associated with the same coherent
generation inputs.

Physical materialization may be eager or demand-driven. A lazy unit is not permission to expose partially resolved
state. When a consumer receives the unit, the same HIR admission and verification laws apply as they do to eagerly
materialized HIR.

This allows V1 to use a simple eager implementation while preserving a path to fine-grained queries, IDE demand, and
V2 incremental materialization without changing HIR meaning.

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

A published semantic product may outlive its construction scratch state while cold derived indexes or source-oriented
material are discarded earlier. The HIR contract does not require every projection, index, provenance expansion, or
decoded body to
remain simultaneously materialized. Selective retention must not make a consumer-visible semantic unit unavailable
while that unit is still validly published and required.

The exact reclamation mechanism remains open.

---

# 13. Snapshot Coherence

A semantic computation that requires one coherent HIR world observes one published generation.

It must not combine a new definition, an old resolution index, and a half-built relation merely because those pieces are
physically reachable at the same time.

Cross-generation reuse is allowed when an equivalent semantic projection has been validated for the new generation. The
logical consumer still observes that projection as valid input to its current generation. The reuse relation does not
create identity continuity between generations.

This rule preserves snapshot coherence without requiring the compiler to copy every unchanged definition into every
new physical generation.

---

# 14. Query, Product, and Manager Compatibility

Resolved HIR is a compiler product. It is not a query object and it is not owned by a Manager.

A query-oriented compiler may request whole HIR generations or smaller HIR projections. An Analysis Manager may attach
derived results to an exact HIR subject and validity context. A local Pass Manager may orchestrate HIR-preserving
representation preparation before publication or over a private generation.

None of those infrastructures defines HIR meaning.

HIR must therefore provide stable semantic subjects, clear generation validity, and an observable read surface without
requiring one query scheduler, one Analysis Manager API, or one pass pipeline.

Derived knowledge remains outside HIR. If several consumers need the same expensive calculation, Kontrakt may publish a
shared derived product rather than storing the result inside unrelated HIR definitions.

Query and analysis scope should follow explicit Definition, Binding, and projection boundaries when that gives a
smaller valid dependency boundary. A consumer that needs one Definition projection should not be forced to depend on the
full frontend generation merely because the first physical implementation stores both in one allocation domain.

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

A previous product may be considered for reuse only after the current computation has resolved the current 1D role, the
complete current semantic determinant set required for the product, and the exact current semantic references on which
that product depends. Compiler-owned reuse-validity inputs must also permit comparison with the previous product.

If the current semantic projection is equivalent to the previous projection, downstream products whose exact semantic
inputs are limited to that projection need not be invalidated further.

```text
current explicit inputs
    ↓
resolve role / determinants / exact references
    ↓
validate previous product compatibility
    ↓
compare consumer-visible HIR result
    ↓
meaning unchanged
    ↓
semantic propagation may stop
```

Early cutoff is based on consumer-visible semantic equality, not on the fact that a cache entry exists, a fingerprint
matches, or the aggregate HIR generation changed. A fingerprint may reject a mismatch quickly. It does not prove HIR or
Contract equality by itself.

Selecting a previous product for comparison is compiler reuse work and does not create a cross-generation identity
relation.

A provenance-only change may therefore refresh source projection and diagnostics while allowing semantic downstream
products to remain reusable.

Early cutoff is an optimization. Failing to take the cutoff may cost time. Taking an unsound cutoff is a correctness
failure. When reuse validity is uncertain, Kontrakt recomputes.

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

# 20. HIR Formation, Transformation, and Optimization Law

HIR formation has a semantic plane and a physical realization plane.

The semantic plane is fixed by Sections 4.12 through 4.16. It resolves the explicit IDL role, projects the owning 1D
Definition determinants, refines authored material under that 1D law, forms exact candidate references and meaning,
completes the IDL Binding Candidate, verifies the result, and publishes Resolved HIR.

The physical realization may fuse those logical stages. Fusion does not remove their invariants.

## 20.1. Authority-Specific Authoring Refinement

Frontend refinement may erase syntax-only or host-only structure after its meaning is known. It may resolve aliases,
replace lexical references with exact candidate references, remove authoring sugar, and form the explicit absence or
relation material already required by the owning 1D law.

Frontend refinement must not invent a new semantic equivalence. It must not collapse a distinction merely because the
compiler can store the result more compactly. The `Canonicalization Contract`, Admission judgment, Fact Establishment,
Applicability judgment, Policy selection, Governance judgment, and other Contract authorities remain at their owning
boundaries.

## 20.2. Physical Formation Optimization

Compiler-owned formation optimization may reduce work or improve locality without changing HIR meaning.

Legal techniques include direct-to-slab formation, role-partitioned work ranges, dense current-generation handles,
primitive columnar storage, compact relation ranges, collision-safe interning, deterministic parallel fill, and early
release of producer scratch that has no remaining semantic, provenance, diagnostic, or reader obligation.

Exact pre-count and exact sizing are permitted when profitable. They are not HIR semantic requirements. A realization
may instead use bounded sizing, deterministic segmented slabs, deterministic chunked formation, or another
representation
that preserves the same published result.

The compiler may share formation computation or physical payload storage across uses when the relevant current inputs
are equivalent. That sharing does not merge two distinct Definition Candidates. Semantic entity identity remains owned
by the applicable 1D law.

## 20.3. Formation Reuse

Formation reuse is work avoidance. It does not change the logical formation law.

The compiler may reuse a previous Definition or projection only after current semantic determinants, exact references,
and compiler-owned validity inputs establish that the previous product is a valid comparison candidate. Reuse never
supplies a missing current determinant and never establishes current identity.

When those conditions cannot be proven, Kontrakt follows the valid clean formation path.

## 20.4. Published HIR Boundary

Published Resolved HIR is read-only to ordinary consumers. A compiler optimization does not mutate published candidate
meaning in place.

Representation preparation, compaction, interning, or validation used to create one publication occurs before that
publication. Later compiler work that needs a different optimized representation publishes a derived compiler product or
a later IR instead of silently changing the already-published HIR meaning.

The legal optimization relation is therefore:

```text
same explicit semantic inputs
    ↓
clean / cached / incremental / parallel formation
    ↓
same observable Published Resolved HIR meaning
```

A physical optimization that cannot preserve that relation is not a legal HIR optimization.

# 21. Concurrency

Parallel frontend work is allowed only behind deterministic publication.

Workers may build independent private candidate material. Their completion order must not determine semantic identity,
exact candidate references, dense-handle assignment, semantic relation ordering, or user-visible deterministic output.
When a persistent or serialized HIR format claims canonical reproducibility, worker count and scheduling must not change
its canonical bytes.

A deterministic partition, assignment, merge, or equivalent publication law resolves concurrent candidate work into one
coherent HIR generation. Concurrent interning or first-writer races must not decide semantic equality or stable output.

Cancellation must not expose a partial generation. A stale worker result for an older input or generation must not
replace a newer valid publication.

The exact lock, epoch, persistent-structure, actor, work-stealing, transaction, or parallel-fill mechanism remains
implementation.

# 22. Persistence and Cross-Session Products

A future implementation may persist HIR products across compiler sessions.

Persistence requires an explicit product compatibility boundary. The persisted product format, producer schema
version, frontend language version, current semantic reference/equality material, and Contract Version are separate
concerns.

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
resolved Definition Candidate meaning
    ≠
IDL Binding Candidate meaning
    ≠
source provenance
    ≠
query / cache state
    ≠
physical storage identity
```

The reference model uses typed columnar families and generation-local dense handles.

```text
HIR Generation G

Operation slot columns
    operationInputBindingRef[]
    operationAdmissionBindingRef[]
    operationCanonicalizationBindingRef[]
    operationLoweringBindingRef[]

Input Binding Candidate columns
    inputBindingTargetRef[]

Admission Binding Candidate columns
    admissionBindingTargetRef[]

Canonicalization Binding Candidate columns
    canonicalizationBindingTargetRef[]

Lowering Binding Candidate columns
    loweringBindingTargetRef[]

Authority-specific Definition Candidate columns
    Input definition slabs
    Admission definition slabs
    Canonicalization definition slabs
    Lowering definition slabs

Lowering relation columns
    loweringEdgeBase[]
    loweringEdgeCount[]

Lowering-edge columns
    loweringSourceCoordinateRef[]
    loweringTargetCoordinateRef[]

Current-reference indexes
    authority-specific candidate-reference material
    current reference -> generation-local dense handle

Provenance sidecar
    provenanceSubjectRef[]
    provenanceSourceRef[]
    provenanceStart[]
    provenanceEnd[]
```

Each array name denotes a logical column. A JVM realization may back high-cardinality columns with primitive arrays or
FFM `MemorySegment` storage. Small metadata may use another compact representation when that is cheaper. The semantic
contract does not depend on the backing choice.

A hot relation such as `admissionBindingTargetRef[bindingHandle]` may physically contain one dense Admission Definition
handle. Semantically it still means that one exact Binding Candidate targets one exact Definition Candidate. The handle
is valid only in the current generation.

The current-reference index is separate from the hot dense-handle path. Its exact coordinate representation remains
owned by the relevant 1D laws and later HIR design. It is not required to be one universal tuple or one fixed-width key.
HID or fingerprint may accelerate that index without becoming the reference meaning.

Each 1D authority keeps its own semantic payload family. Kontrakt must not replace those authority-specific meanings
with
one generic property map or universal edge record.

The important shape is:

```text
shared compact HIR infrastructure
+
typed Definition Candidate slabs
+
exact IDL Binding Candidate relations
+
current-generation dense handles
+
separate current-reference material
+
separate provenance
```

This gives consumers a typed semantic surface without requiring a pointer-heavy object graph or making physical storage
into semantic ownership.

---

# 25. Non-Normative End-to-End Reference Realization

This example follows one authored Operation from source acquisition to a published HIR generation. It shows how an IDL
slot grants a 1D role, how Definition Candidate and Binding Candidate material stay separate, and how the compiler can
still use direct primitive formation, verification, publication, early cutoff, and reclamation.

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

`DepositAdmission` is authoring material until the explicit `admission` slot selects it for the Admission role. The
selected source form is evidence for frontend refinement. The published HIR surface is the resolved Admission candidate
meaning and the exact binding from this Operation context to that candidate.

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
not a valid HIR reference and the source declaration object is not the HIR Definition Candidate reference.

Resolution may use bounded transient structures such as an open-addressed symbol index, work queues, scratch ordinals,
or intern tables. Those structures belong to the producer episode. They are not published HIR.

The frontend may release scratch structures as soon as the information has been formed into resolved HIR candidate
material.

## 25.2. Deterministic Layout Planning and Direct HIR Formation

The reference path does not require a graph of candidate objects that is later copied into production tables.

The frontend first resolves exact IDL slot selections and partitions work by 1D role. Each authority-specific formation
kernel then projects only the Definition-determining context owned by that 1D law.

```text
IDL slot selections
    ↓
role-partitioned work ranges
    ↓
1D Definition determinant projection
    ↓
authority-specific direct HIR formation
```

A V1 implementation may pre-count Definitions, Binding Candidates, relation rows, and provenance ranges when that avoids
reallocation at acceptable scan cost. It may then allocate exact or bounded-capacity column storage once.

```text
optional pre-count / capacity planning
    ↓
allocate or reserve typed columns
    ↓
deterministic current-generation handle assignment
    ↓
direct-to-slab formation
```

Exact pre-count is not required by the HIR contract. A deterministic segmented or chunked layout is equally valid when
it produces the same observable HIR result.

For the `deposit` Operation, the final hot relation is conceptually two-step.

```text
op = operationHandle(DepositContract.deposit)

ab = admissionBindingHandle(deposit.admission)
ad = admissionDefinitionHandle(DepositAdmission)

operationAdmissionBindingRef[op] = ab
admissionBindingTargetRef[ab]    = ad
```

The Binding Candidate is completed only after `ad` denotes a complete resolved Admission Definition Candidate. Before
that point the frontend may retain the role-selected source handle and the Admission-owned determinant input as private
working state.

If another IDL explicitly selects the same Admission declaration and the Admission law says that the differing context
does not change Definition meaning, the compiler may share the Definition formation result or its physical payload while
retaining each exact Binding Candidate. If the two uses denote distinct Definition Candidates under the owning identity
law, equal payload does not merge those semantic entities.

If an owning 1D law makes the context Definition-determining, its determinant projection forms the distinct Definition
Candidate required by that law.

The Lowering payload remains a contiguous relation range in this reference realization.

```text
lower = loweringDefinitionHandle(DepositLowering)
base  = loweringEdgeBase[lower]

loweringSourceCoordinateRef[base + 0] = coordinateHandle(DepositInput.accountIdText)
loweringTargetCoordinateRef[base + 0] = coordinateHandle(deposit.command.accountId)

loweringSourceCoordinateRef[base + 1] = coordinateHandle(DepositInput.amountText)
loweringTargetCoordinateRef[base + 1] = coordinateHandle(deposit.command.amountMinor)

loweringEdgeCount[lower] = 2
```

These handles are current-generation access coordinates. The semantic relations they realize are exact HIR references.
The source spellings are no longer consulted by ordinary downstream semantic consumers.

Original spelling and source ranges remain reachable through provenance.

The candidate remains non-authoritative. Direct formation does not perform Contract Establishment.

## 25.3. HIR Verification as a Deterministic Table Scan

HIR verification validates the candidate generation before publication. The reference path does not use assertion-style
`require(...)` calls as the verification model.

Verification scans typed slabs in deterministic handle order and writes structured compiler violations into a bounded
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
    binding = operationAdmissionBindingRef[op]
    if binding < 0 or binding >= admissionBindingCount:
        emit(HIR_INVALID_ADMISSION_BINDING_REF, operationSubject(op), binding)
        continue

    target = admissionBindingTargetRef[binding]
    if target < 0 or target >= admissionDefinitionCount:
        emit(HIR_INVALID_ADMISSION_TARGET_REF, bindingSubject(binding), target)

for lower in 0 ..< loweringDefinitionCount:
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

Additional scans reject unresolved lexical references, recovery material, role-incompatible Binding targets, malformed
candidate-coordinate material, invalid relation ranges, and non-deterministic observable ordering.

The scan produces compiler diagnostic material when violations exist. The candidate generation is not published as
valid Resolved HIR.

The verifier does not decide whether `DepositAdmission` or `DepositLowering` receives Contract authority. That remains
the owning Establishment law.

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
    current-reference index metadata
    provenance index metadata

Hot semantic slabs
    Operation slot -> Binding Candidate handles
    Binding Candidate -> Definition Candidate handles
    authority-specific Definition Candidate columns
    authority-specific relation ranges

Current-reference material
    authority-specific resolved candidate coordinates
    indexes from exact current references to dense handles

Cold side material
    provenance ranges
    authored spelling references
    diagnostic-only source relations

Derived infrastructure outside HIR meaning
    query dependency records
    cached projection fingerprints
    previous-product comparison metadata
    persistent-product metadata
```

High-cardinality material should prefer primitive columnar storage. FFM-backed slabs are the default physical direction
when off-heap storage, explicit lifetime, or larger contiguous regions make them profitable. Heap primitive arrays
remain
a replaceable option where measurement favors them.

One efficient V1 path may use exact pre-count and pre-sizing, but the architecture does not require that strategy:

```text
role / determinant resolution
    ↓
layout planning
    ↓
deterministic current-generation handle assignment
    ↓
direct typed formation
    ↓
release only scratch whose semantic / provenance / diagnostic obligations are complete
    ↓
batch HIR verification
    ↓
seal
    ↓
publish generation
```

`layout planning` may mean exact pre-sizing, bounded capacity, or deterministic segmented/chunked storage. There is no
required intermediate object graph between resolved frontend facts and published HIR storage.

Vertical partitioning remains important. Establishment for Admission should not have to touch provenance bytes,
diagnostic strings, Lowering edge slabs, or unrelated authority payloads merely because they share one HIR generation.
The physical partition does not become semantic ownership.

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

A consumer obtains a typed semantic projection. When hot access is needed, the implementation resolves or validates the
current exact candidate reference and maps it to a current-generation dense handle.

```text
Admission Definition Candidate Reference R
    ↓ current-generation reference index
Admission handle 17
    ↓
Admission-owned semantic columns
```

The number `17` is not the reference meaning and it is not recorded as cross-generation identity.

The IDL use remains separately observable as a Binding Candidate.

```text
DepositContract.deposit.admission
    ↓ Binding Candidate B
B
    ↓ exact target relation
Admission Definition Candidate R
```

Conceptually, compiler products consume boundaries such as:

```text
Admission Definition Establishment
    consumes AdmissionDefinitionProjection(R)

Binding / composition work
    consumes AdmissionBindingProjection(B)

Frontend diagnostic
    consumes AdmissionDefinitionProjection(R)
    consumes ProvenanceProjection(R)
```

Query infrastructure records those semantic product reads. It does not make a raw handle, slab offset, or FFM address
the
permanent dependency law.

## 25.6. Semantic Early Cutoff

Assume a comment moves the authored `DepositAdmission` declaration to another source range without changing its current
Admission role, Definition-determining inputs, exact semantic references, Definition Candidate meaning, or exact IDL
binding.

The new generation may contain different provenance while the semantic projections remain equivalent.

```text
G42
    AdmissionDefinitionProjection(R42) = S
    AdmissionBindingProjection(B42)    = B
    Provenance(R42)                     = P42

G43
    AdmissionDefinitionProjection(R43) = S
    AdmissionBindingProjection(B43)    = B
    Provenance(R43)                     = P43
```

`R42` and `R43` are current-generation references. The compiler does not claim that one inherited the identity of the
other.

For G43, the frontend first resolves the current role, current Admission Definition determinants, and current exact
references. Previous-product infrastructure may then select the G42 projections for comparison if compiler-owned
validity inputs also permit that comparison.

A fast fingerprint may reject equality quickly when values differ. A matching fingerprint is not by itself Contract or
HIR semantic authority. Reuse must follow the HIR projection equality law or another collision-safe validation rule.

When semantic equality and reuse validity are established, propagation may stop at the Definition and Binding projection
boundaries. Products that consume provenance still observe `P43`.

If the binding target changes, the Binding projection changes even when both target definitions happen to have equal
payload meaning. If the Definition meaning changes while the binding remains structurally the same, Definition-dependent
products recompute. These are separate change dimensions.

If any required determinant or reuse-validity input is uncertain, the compiler recomputes the affected product.

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

## 25.8. Partition-Local Formation and Failure Isolation

The compiler does not need one mutable HIR tree for the whole project.

A physical HIR partition may contain one semantic product or a deterministic group of related products. Its purpose is
formation locality, publication, reuse, and lifetime management. The partition does not become Definition ownership or
Binding meaning.

A query that needs `DepositLowering` resolves the exact current candidate reference to its current dense handle and
reads
only the required Lowering ranges.

If one Definition Candidate fails resolution, no poison object is inserted into unrelated valid definition slabs. A
Binding Candidate that depends on the failed target cannot satisfy the HIR invariant. Independent semantic products may
remain valid when their own determinant closure is complete.

V1 may form all partitions eagerly. V2 may materialize or repair selected products on demand. Both paths must publish
the
same HIR semantic meaning for the same valid inputs.

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
HIR model. The normal hot path should be able to form resolved working material directly into typed HIR storage without
allocating one wrapper object per semantic row or relation. Exact pre-sizing is one optimization, not a semantic
requirement.

Consumers should see typed semantic projections rather than raw backing storage. A projection implementation may
reduce to a few handle-indexed slab reads. The consumer contract must survive a later replacement of heap primitive
arrays by FFM segments, a different local handle assignment, or a different current-reference index.

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

Compiler QA should also move unrelated declarations and reorder independent construction work while checking that
current semantic references and HIR projections remain correct. Tests should reject use of a generation-local handle in
another generation unless current reference material is resolved or validated into a new handle. An invalid Definition
Candidate should not create poison HIR for an independent semantic product. Eager and demand-driven materialization,
exact pre-count and segmented formation, clean and reused formation, and different legal worker schedules must produce
the same semantic inspection result. When a persisted format claims canonical encoding, those tests should also compare
its canonical bytes.

---

# 28. Consequences

The frontend gains a strict semantic checkpoint before Contract authority.

Establishment can remain authority-focused because it receives exact candidate meaning rather than frontend search
problems. Diagnostics and tooling can share the same resolved meaning without becoming semantic authorities of their
own.

The compiler also gains a stable product boundary for query orchestration and future persistence. Whole-generation
snapshot coherence can coexist with Definition- and Binding-level reuse. Provenance can refresh independently from
semantic meaning.

The cost is explicit architecture work. HIR needs stable semantic references, generation publication, projection
boundaries, equality rules, and lifecycle ownership. Those costs are accepted because leaving them implicit would move
the same complexity into every downstream subsystem and make V2 invalidation depend on accidental representation.

This ADR does not require HIR to be physically object-heavy. The stronger semantic boundary allows the implementation to
use more aggressive tables, slabs, interning, persistent structures, or other compact representations behind the
published contract. It also allows formation stages to be physically fused, skipped through validated reuse, or realized
with different layout strategies as long as their logical invariants and published result remain unchanged.

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

Using parser nesting, IDL containment, or host object containment as the permanent HIR semantic ownership relation would
make authoring shape control Definition meaning, publication, reuse, and lifetime. The same reusable 1D declaration may
be selected by more than one IDL context.

Rejected.

## 29.10. Generation-Local Handles as Persistent Identity

A dense ordinal, arena index, row number, or local interner ID may be efficient inside one generation. Reusing that
value
as a cross-generation or cross-session semantic reference would couple persistence to physical construction order.

Rejected.

## 29.11. User API Shape as 1D Authority

Treating a supported class or carrier shape as a 1D Contract before explicit IDL binding would make the current
authoring
API define Contract role. It would also make future authoring frontends harder to converge on the same HIR meaning.

Rejected.

## 29.12. Binding Context Always Creates a New Definition Candidate

Cloning Definition Candidate meaning for every IDL use would fold Binding context into Definition meaning even when the
owning 1D law does not make that context semantic. It would duplicate reusable definitions and enlarge invalidation.

Rejected.

## 29.13. Cross-Generation Lineage Identity

Carrying predecessor, successor, inherited identity, or transferred identity from one HIR generation into another would
make current meaning depend on compiler history. Previous products may be compared for reuse, but they do not establish
current HIR identity.

Rejected.

## 29.14. One Universal Definition Candidate Coordinate Tuple

Forcing every 1D candidate into one tuple such as authority kind, Contract ID, Version, and local coordinate would move
identity law out of the owning 1D semantics. Some authorities are Interface-local, Operation-local, Machine-local, or
use
other version and coordinate rules.

Rejected.

## 29.15. Compiler Normalization as Hidden Contract Canonicalization

Allowing frontend optimization to collapse distinctions merely because two forms are convenient to store would let
compiler representation decide Contract equivalence. Contract-owned Canonicalization and other authority judgments must
remain explicit.

Rejected.

## 29.16. Merging Definition Candidates by Equal Payload

Two Definition Candidates may have equal current payload without being one semantic entity. Formation computation or
physical payload may be shared when valid, but semantic Definition identity remains owned by the applicable 1D law.

Rejected.

## 29.17. Mandatory Exact Pre-Count Before HIR Formation

Exact pre-count can reduce allocation and copying, but requiring it as architecture would force additional scans and
constrain lazy, segmented, or incremental formation even when another deterministic layout is better.

Rejected as a semantic or universal physical requirement.

## 29.18. Reuse Before Current Determinants Are Resolved

Using a previous product because its key or fingerprint appears unchanged before current role, semantic determinants,
and exact references are known can hide changed inputs and produce stale HIR.

Rejected.
---

# 30. V1 Requirements

V1 must provide a real Resolved Contract HIR boundary between frontend resolution and Establishment.

Published HIR must satisfy the admission invariant and remain read-only to ordinary consumers. The compiler must keep
source provenance separate from semantic equality. Definition Candidate and Binding Candidate projections must be
separately addressable even if the first physical implementation groups their storage.

V1 frontend formation must obtain a 1D role from explicit IDL binding rather than from user API shape alone. It must
project Definition-determining context through the owning 1D law before forming Definition Candidate meaning. A complete
Binding Candidate is formed only after its exact Definition Candidate target exists.

The same role-qualified declaration may be reused by several IDL bindings when the owning 1D determinant and identity
laws permit one reusable Definition Candidate. Equal payload alone does not merge distinct Definition Candidates.

V1 must preserve exact current Definition Candidate and Binding Candidate references separately from generation-local
dense handles. A dense handle may realize a hot relation inside one generation but cannot become semantic identity,
persistent reference, or history.

V1 may physically fuse role resolution, determinant projection, authority-specific refinement, Definition Formation,
and Binding completion. Fusion must preserve the logical formation order and each stage invariant.

V1 query orchestration must be able to identify explicit HIR inputs, observe product dependencies at stable semantic
boundaries, and reuse a published result only under a valid generation or equivalent validity rule.

V1 must support deterministic semantic early cutoff where current determinants, exact references, semantic equality, and
reuse validity are already available and the cutoff is profitable. It need not incrementalize every frontend
computation.

V1 may use exact pre-count and pre-sized primitive slabs. It may also use deterministic bounded, segmented, or chunked
formation when measurement favors them. No one layout-planning strategy is part of HIR meaning.

V1 may choose physical partitions for locality, formation, publication, and lifetime. Those partitions must not create
Definition ownership or Binding meaning, and ordinary consumers must not depend on their topology.

Invalid source must be isolated at the smallest sound semantic dependency boundary. Unaffected semantic products may
remain available to frontend diagnostics and tooling even when the overall compilation cannot succeed.

V1 must expose Published Resolved HIR through a semantic access boundary that does not require ordinary consumers to
depend on the physical HIR layout. The first backing representation may be primitive slabs without making that choice
part of HIR meaning.

Cache-off and clean-recompute execution must remain valid and must agree with reused execution. Worker count, legal
scheduling order, cache state, and layout strategy must not change observable Published HIR meaning.

# 31. V2 Evolution Seam

V2 may add persistent HIR products and finer frontend repair without changing this ADR.

Possible work includes persistent semantic projections, demand-driven materialization, incremental lexing and parsing,
incremental resolution, determinant-local Definition Formation, provenance-only refresh, cross-session early cutoff,
dynamic dependency repair, domain-local delta maintenance, and adaptive switching between repair and rebuild.

Previous-product metadata may help locate comparison products or avoid work. It remains compiler reuse infrastructure.
It does not create predecessor identity, successor identity, or semantic lineage for current HIR.

A V2 formation engine may avoid rebuilding one Definition Candidate only after current role, current 1D semantic
determinants, exact current references, and compiler-owned reuse-validity inputs are sufficient to validate the reused
result. Missing history or persistent state falls back to clean formation.

V2 may choose a different physical formation architecture from V1. Persistent segmented slabs, mapped immutable pages,
content-addressed chunks, database-like product storage, or another representation remain legal behind the same semantic
access and publication laws.

Those techniques remain compiler realization.

V2 must preserve the same admission invariant, current-reference law, semantic equality, authority boundary, logical
formation law, publication law, and determinism-first rule.

# 32. Intentionally Open

This ADR does not freeze the HIR physical schema.

It does not decide whether V1 uses objects, tables, primitive arrays, slabs, persistent structures, or mixed storage. It
does not require exact pre-count, one global allocation pass, one segmented layout, or one chunking policy. Those are
physical formation choices constrained by the logical formation and determinism laws.

It does not choose the final HIR projection catalog. It does not fix the concrete API shape of the HIR Semantic Access
Boundary, its bulk-access forms, or its handle encoding. It does not select a query scheduler, Analysis Manager API,
Pass
Manager API, fingerprint algorithm, CAS implementation, reclamation algorithm, serialization format, or V2 repair
algorithm.

It also does not define the complete semantic payload of each 1D Contract. The owning 1D ADR must still state the
candidate meaning, Definition determinants, and semantic distinctions that HIR has to preserve.

The exact component set of each 1D Definition Candidate Reference remains open until the owning 1D identity and
reference laws are audited. This ADR does not require one universal `Authority + Contract Id + Version + Local
Coordinate` tuple. It also does not decide which Binding context is Definition-determining for a 1D unless the owning
ADR
already says so.

The exact compiler product lifetime and memory policy remain design work as long as the publication and validity laws in
this ADR are preserved.

# 33. Non-Normative Engineering Basis

This decision is consistent with several production systems, but none of them is a template for Kontrakt.

`rustc` lowers AST into HIR after removing syntax structure that later analysis does not need. Its HIR uses local
compiler addressing and separate stable forms for incremental work rather than treating one physical ID as every kind of
identity. Its query system also shows why larger products need smaller semantic projections and why query dependency
must
not be confused with the semantic relation represented by HIR. Kontrakt adopts the separation lesson without adopting
cross-generation lineage identity or Rust source-containment rules as Contract semantics.

Kotlin K2 FIR shows that logical resolution phases can strengthen one frontend semantic representation without requiring
one physically separate full IR for every phase. The relevant architectural lesson is the phase invariant, not FIR's
object model. FIR symbol access also shows the value of giving consumers a stable semantic access surface instead of
requiring them to reach through a symbol into mutable backing declarations.

LLVM and MLIR make IR validity explicit. Their verifier boundaries let later passes assume well-formed input and require
transformations to return valid IR. MLIR also separates a common IR substrate from dialect-owned semantics, which is a
useful comparison for preserving distinct 1D Contract vocabularies over shared compiler infrastructure. Its pass
isolation rules also demonstrate why independently processed units need explicit mutation and partition boundaries for
parallel work. Kontrakt adopts these invariant and isolation lessons without adopting LLVM or MLIR's universal operation
model or symbol-table containment as HIR Definition identity.

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

Explicit IDL binding grants the 1D role. HIR keeps reusable Definition Candidate meaning separate from contextual
Binding
Candidate meaning. Exact current references remain separate from dense handles, compiler history, and physical storage.

It preserves all candidate meaning required by the owning Contract laws and removes frontend ambiguity that later
semantic work must not repeat.

Its published semantic surface is independent of source provenance, physical layout, query topology, manager topology,
cache state, generation identity, and incremental algorithm.

HIR generations may expose finer semantic projections so compiler products can be reused without turning physical
storage into dependency law.

Lifecycle and transition belong to compiler publication and validity. They do not become Contract State or Transition.
A new generation is published instead of mutating already-published meaning in place, and old-generation reclamation is
separate from publication.

HIR formation follows a logical semantic order from explicit IDL role through 1D determinant projection and
authority-specific refinement to complete Definition and Binding candidates. A physical implementation may fuse those
stages or avoid work through validated reuse, but it cannot make layout, scheduling, cache state, or optimization policy
part of HIR meaning.

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