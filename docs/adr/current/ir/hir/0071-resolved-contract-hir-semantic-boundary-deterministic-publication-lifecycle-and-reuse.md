# ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse

## Status

Accepted

## Date

2026-09-12

## Related

- `/what-contract-is.md`
- `/0046-idl-first-interface-contract-frontend-1d-catalog-backend-discipline.md`
-

`/0047-one-dimensional-contract-presentations-pipeline-slot-selection-and-backend-realization-boundary.md`

- `/0053-version-contract-sovereign-meaning-identity-version-claims-and-authority-boundary.md`
- `/0063-contract-establishment-occurrence-applicability-and-semantic-dependency.md`
- `/0064-input-contract-explicit-boundary-presentation.md`
- `/0065-admission-contract-continuation-judgment.md`
- `/0066-canonicalization-contract-stable-representative-and-canonical-bytes.md`
-

`/0067-lowering-contract-explicit-relation-compiler-derived-realization-and-core-entry.md`

- `/0068-fact-contract-explicit-immutable-core-information-sameness-and-uniqueness.md`
- `/0069-invariant-contract-fact-local-standing-integrity-law.md`
- `/0074-compiler-result-explicit-unsuccessful-result-recovery-and-observation-boundary.md`
- `/kontrakt-compiler-total-architecture-map-design-draft.md`
- `/kontrakt-compiler-material-and-ir-architecture-review-checklist.md`
- `/kontrakt-established-contract-world-architecture-todo.md`
- `/kontrakt_IR_subsystem_contract_implementation_separation_discussion.md`
- `/Kontrakt_Query_Oriented_Compiler_and_Object_Free_Core_Design.md`
- `/kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- `/kontrakt-v2-incremental-architecture-research-todo.md`
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

This ADR defines the semantic, visibility, and lifecycle contract of **Resolved Contract HIR**.

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
while field-level dependency tracking would make physical storage part of incremental architecture. Visible HIR
therefore needs stable semantic units and coherent generations without committing Kontrakt to one query or database
system.

Finally, incremental and parallel execution cannot weaken determinism. A warm cache, a different worker schedule, or an
incremental repair path must not change the resolved meaning presented to Establishment.

---

## 3. Decision Drivers

Contract meaning remains prior to compiler realization.

Resolved HIR must preserve every resolved candidate-semantic distinction that Establishment still needs. It may erase
authored differences once those differences no longer affect resolved candidate meaning.

A downstream consumer must not reopen source or inspect host-carrier topology to recover meaning that the frontend has
already resolved.

Visible HIR must be safe for independent consumers. Construction may be mutable, but a consumer must not observe a
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

Resolved Contract HIR is the high-level compiler-semantic representation produced after Contract frontend resolution
and before Contract Establishment.

```text
Contract Authoring Material
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

`High-Level` denotes semantic altitude in the Kontrakt compiler. Contract meaning is the highest semantic premise that
constrains later realization compilation, machine formation, optimization, and target lowering. HIR is not named by
proximity to user realization source.

Resolved Contract HIR is therefore the Contract-side HIR. User realization lexing, parsing, semantic analysis,
verification, optimization, and target lowering belong to later compiler domains and products. They may consume
established Contract meaning, but they do not extend Resolved Contract HIR.

Resolution success means that the compiler can state the candidate exactly in compiler-semantic form. It does not mean
that the owning Contract law has accepted that candidate.

HIR is a real IR level because its vocabulary, invariant, and information-loss boundary differ from source and syntax
material. It is not a new level merely because the compiler freezes or caches it.

---

## 4.2. Determinism-First Law

For the same explicit valid frontend inputs, Kontrakt must produce the same observable Resolved HIR meaning.

Worker order cannot select a meaning. Cache state cannot select a meaning. Filesystem enumeration order cannot select a
meaning. Allocation order, hash-table iteration, query scheduling, and incremental repair order cannot select a meaning.

If an ordering is semantically observable, the owning law must provide the ordering or the compiler must use an
explicit deterministic observation rule that does not invent Contract meaning.

Physical layout may differ when that difference is outside the HIR observable surface.

The same rule applies to optimization of the compiler itself. Kontrakt may prefer lower compile time, lower memory use,
more cache hits, more parallelism, or more incremental reuse only after deterministic equivalence is preserved.

A clean computation with reuse disabled remains the reference path for HIR semantic correctness.

---

## 4.3. HIR Resolution Invariant

Material is visible as Resolved Contract HIR only after the frontend can interpret its semantic references exactly.

The required resolution boundary includes the source-language ambiguity that would otherwise force later semantic work
to search or guess. Names required by the candidate are no longer unresolved spellings. The Contract role of the
candidate is known. Module or import qualification needed for exact interpretation has been resolved. A reference that
remains ambiguous prevents that semantic unit from satisfying the HIR invariant.

The exact reference form is a compiler concern. The invariant is semantic: later consumers receive one exact resolved
target rather than a lexical search problem.

A 1D role is not inferred from the shape of a user API declaration. The explicit IDL binding selects the declared
material for one exact 1D role. The HIR resolution invariant is satisfied only after that role-qualified material can be
interpreted under the
owning 1D frontend law.

Satisfying the HIR resolution invariant does not require Establishment success.

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

Operation references follow the same current-world rule. An `OperationRef` denotes one exact nominal Operation handle
inside one exact Contract Interface semantic scope. That scope is Contract meaning, not source containment. File path,
package, host class, generated API symbol, JVM descriptor, declaration ordinal, dense handle, HID, and
previous-generation
identity do not establish the Operation reference.

The Operation owns an independent callable coordinate surface. An `OperationParameterRef` denotes one exact declared
Operation-local nominal semantic coordinate. A parameter rename changes that current semantic coordinate. Declaration
order or JVM parameter index does not establish coordinate identity unless an owning Contract law explicitly makes order
semantic. Lowering may target an Operation parameter coordinate, but it does not create that coordinate.

The Operation result position is likewise resolved independently of generated host signature or JVM descriptor. The
exact payload and type relations carried by Operation parameter and result coordinates remain owned by the applicable
frontend and Contract laws.

---

## 4.6. Version and Basis Boundary

A Version Claim that participates in candidate meaning must be resolved far enough that Establishment is not left with
lexical ambiguity.

Visible HIR therefore preserves the exact authority-scoped **resolved Version candidate coordinate** required to judge
the candidate. The authored spelling is frontend evidence and may remain in provenance. The resolved coordinate is exact
compiler-semantic meaning, but it is not yet the authoritative `Version Binding` owned by Establishment.

Contract Version remains separate from compiler generation, artifact revision, persistent schema version, and encoded
product version. History or persistent storage may validate a Version candidate during Establishment, but storage record
identity does not create the semantic Version coordinate.

Required Basis follows a related but distinct separation.

A Definition Candidate preserves any **Basis Requirement Law** that belongs to its Definition meaning. That law may
state
what semantic kind, coordinate shape, cardinality, completeness rule, or permitted absence a later judgment requires. It
does not select the Established material that will satisfy the requirement.

An occurrence-owned or higher-scope `Required Basis` instance is not Definition HIR material. The Definition HIR still
preserves any occurrence-basis law that belongs to the Definition meaning. When a Definition-time judgment itself owns
the Required Basis, that exact requirement may be formed from the resolved candidate as Establishment input.

`Basis Resolution`, `Basis Binding`, actual source selection, and Complete Basis remain owned by the applicable
Establishment or composition law over Established Material. A compiler dependency edge or HIR reference does not become
Basis Binding.

---

## 4.7. Applicability Boundary

HIR preserves an Applicability Law when that law is part of resolved candidate meaning. It may preserve the exact
semantic coordinate kinds or other declared inputs that the owning law says participate in Applicability.

HIR does not contain an `Applicable` result for a future semantic use and does not contain the future Established
context
instance merely because the Definition declares that such context will be required.

Applicability in ADR-0063 is a later judgment over the exact binding, dependent application, relevant Established
context, and owning Applicability Law. Applicability is legality, not implicit producer selection or arbitration.

The compiler must therefore keep Applicability Law, applicable context instance, and Applicability result as different
semantic categories.

## 4.8. HIR Seal Verification Boundary

Every Resolved HIR product must satisfy the Resolved Contract HIR boundary before it is sealed and becomes
visible to an independent consumer.

Seal verification may reject material that is incomplete, unresolved, recovery-tainted, structurally inconsistent,
or otherwise incapable of satisfying the Resolved Contract HIR boundary. It does not run an owning Contract judgment and
it does not create Established meaning.

Private construction may temporarily contain incomplete structures while frontend computation is in progress. That
freedom ends when the product is sealed for visibility.

When the HIR seal judgment is actually entered and can state that its owned seal requirement is not satisfied, it
establishes an explicit HIR-owned unsuccessful compiler result under ADR-0074. That result is compiler correctness or
frontend validity material. It is not a Contract refusal and not Contract Failure meaning. Its exact rejection
vocabulary
belongs to the HIR seal-verifier law rather than to this ADR.

A seal judgment that is never entered because an earlier required compiler result was unsuccessful does not acquire a
synthetic HIR Seal Rejection. The earlier result remains owned by its producer, while later availability and completion
consequences are governed by ADR-0074.

The detailed verifier architecture, authority-specific checks, diagnostic mapping, and realization-verifier behavior are
owned by separate verifier ADRs and designs. This ADR fixes only the HIR seal, visibility, and HIR-specific result
boundary
that those verifiers must respect.

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
two authoring forms resolve to the same exact candidate reference and the same candidate meaning, the authoring route
does not create a different HIR meaning. Equal payload alone does not merge distinct candidate references or
Definitions.

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
property model. The shared HIR substrate may provide references, visibility, provenance relations, Binding
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

Definition Candidate meaning is complete independently of one particular consumer observation. A later consumer may
request
a narrower legal projection, but that request does not weaken or redefine the Candidate itself.

A Binding Candidate owns the selection relation, not the surrounding IDL context as a whole. It preserves only the
semantic context required to identify that exact selection occurrence, the selected slot or role, the exact target
Definition Candidate Reference, and any additional qualifier that the binding law independently makes part of the
selection relation. Other Interface, Interaction, Operation, Version, Policy, Governance, or source context does not
become
Binding meaning merely because it is nearby or reachable.

A semantic coordinate may participate in both Definition Candidate meaning and Binding Candidate meaning only when it
independently serves both meanings. Physical duplication or implementation convenience does not make one coordinate part
of both semantic relations.

## 4.14. 1D-Owned Definition Determinants

HIR does not impose one universal coordinate tuple on every 1D Definition Candidate.

The owning 1D law decides which declared material and which contextual material determine Definition meaning. Context
that does not determine Definition meaning remains Binding, Required Basis, Applicability, attribution, provenance, or
other separately owned material.

If one 1D law makes an Operation, Machine, Interface, resolved Version candidate coordinate, or another context part of
Definition meaning, that context participates in the Definition Candidate for that 1D. If the law does not make it
Definition-determining, HIR must not add it merely to obtain a convenient global key.

The shared HIR reference protocol therefore does not require every candidate to contain the same stored fields. Typed
reference domains may carry authority kind structurally. Version material and authority-local coordinates appear only
where the owning semantic law requires them.

Any context that determines Definition meaning must already be projected into the Definition Candidate before that
Candidate becomes Visible HIR. Definition Establishment must not recover Definition-determining meaning from an IDL
Binding Candidate, surrounding source, host structure, or implementation topology. Context that owns only the selection
relation remains Binding meaning and does not enter the Definition Candidate.

A Definition Candidate may contain exact direct semantic references to separately owned Primary HIR subjects when those
references are part of the Candidate meaning. Such a reference preserves the other subject's ownership. It must not be a
placeholder for unresolved Candidate meaning that a later consumer has to reconstruct through arbitrary reference
chasing.

ADR-0071 owns the common completeness obligation: every semantic component that the owning 1D law makes part of
Definition Candidate meaning must be legally observable through that authority's Definition Candidate Projection. The
exact candidate vocabulary, determinant set, and exact component set for each 1D Definition Candidate Reference remain
with the owning 1D ADR.

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
        ↓
Exact Source / Symbol Resolution
        ↓
Resolved Contract Surface Formation
    Contract Interface Subject
    Interaction Subject
    Operation Subject
    exact direct relations
        ↓
Explicit IDL Slot Selection
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
HIR Seal Verification
        ↓
Visible Resolved Contract HIR
```

Contract Interface, Interaction, and Operation are resolved semantic subjects, not generated host API artifacts. Their
formation does not create a source-containment identity hierarchy. Exact semantic relations connect them; nesting,
parent pointers, declaration order, and physical adjacency do not define their identity.

Operation formation includes its independent nominal callable coordinate surface. Operation parameter coordinates exist
before a Lowering Definition can target them. Lowering resolves an exact relation to those coordinates; it does not form
or own their semantic identity.

Role selection precedes 1D interpretation. The frontend does not infer a role from carrier shape and then treat the
inference as IDL meaning.

Definition determinant projection follows role selection because only the owning 1D law can decide which surrounding
context changes Definition meaning. Context that is not Definition-determining remains outside the Definition Candidate.

A complete Binding Candidate is formed only after its exact Definition Candidate target exists. Before that point the
frontend may hold a resolved slot selection and the minimum role-qualified formation input, but that working relation is
not a partially visible Binding Candidate.

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

A distinction required later for Establishment cannot be reconstructed from source after HIR becomes visible. If later
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
resolution judgment
       / \
      /   \
resolved  rejected
   ↓         ↓
 HIR     explicit producer-owned compiler result
                 ↓
          Compiler Result Protocol
                 ↓
      diagnostic / recovery / availability
```

A rejected resolution result is established by the exact frontend judgment that owns the failed compiler requirement. A
diagnostic does not create that rejection. Recovery does not rewrite it. A later HIR stage that was never entered does
not
receive a synthetic unsuccessful result merely because the earlier resolution result prevented entry.

A missing token is not explicit Contract absence. An unresolved symbol is not an empty semantic reference. A recovery
node is not a partially authoritative Contract definition.

Tooling may expose a separate incomplete frontend view when necessary. Such a view must not be accepted as Resolved
Contract HIR by Establishment.

Invalid source must not poison unrelated resolved semantic units.

If one candidate cannot satisfy the HIR resolution invariant, that candidate and any semantic unit whose exact meaning
depends on it are not made visible as valid HIR. An independent candidate may still be formed and verified when its own
determinant closure is complete. The producer preserves the exact unsuccessful compiler result under ADR-0074, while
diagnostics, tooling, and recovery consume legal projections of that result. The compiler does not insert a fabricated
HIR
placeholder merely to keep later HIR processing alive.

Compilation success is separate from this failure-isolation rule. A batch build may still fail because any required
source unit is invalid, while diagnostics or tooling continue to consume already-valid HIR units. The exact
availability,
boundary-completion, and orchestration representation remains owned by the common compiler-result architecture rather
than
by HIR semantic meaning.

---

# 7. Establishment Handoff

Resolved HIR must be sufficient for the owning Definition Establishment judgment without reopening authored source.

Authority-Owned Establishment observes Resolved HIR candidate meaning through the **Resolved HIR Candidate Protocol**
realized by the HIR Semantic Access Boundary in Section 8.9. The Protocol is a legal observation contract over Visible
Resolved HIR. It is not a new IR, compiler stage, Contract authority, or mandatory physical material layer.

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

Basis Requirement Law and Applicability Law remain in HIR when they belong to Definition Candidate meaning. Actual
Required Basis instances, Basis Binding, Applicable Context, Applicability results, Complete Basis, and
occurrence-specific
judgments remain at the later boundaries that own those meanings. A Binding Candidate does not become an Established
Occurrence merely because its target Definition is established.

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

HIR visibility does not establish authority. Establishment does not become a compiler refinement or representation
pass. Canonical Contract World remains a later compiler representation of already-established Definition meaning.

# 8. Visible HIR Surfaces

## 8.1. Consumer Contract

HIR is producer-independent and consumer-aware.

The frontend does not encode the topology of future consumers. It does, however, expose enough resolved meaning that a
valid consumer does not need a private semantic frontend.

Authority-Owned Establishment is the primary semantic consumer. Frontend diagnostics, tooling, query and reuse
infrastructure may also consume HIR directly. Later verifier, optimizer, execution, and backend work normally consume
established or lower material instead. Their needs still matter when deciding whether HIR is discarding information too
early.

Generated API semantics must not bypass Establishment by treating HIR as authoritative Contract definition material.
Generated Interaction and Operation APIs are downstream compiler products derived from established Contract meaning.
HIR preserves the Contract-semantic subjects and relations from which those products are derived; host API shape is not
HIR meaning.

---

## 8.2. Primary HIR Semantic Surface

Visible Resolved HIR has one primary semantic surface. It contains the resolved compiler-semantic material that later
valid consumers would otherwise have to recover by reopening authored source or re-running 1D frontend interpretation.

The primary surface contains the semantic subjects and direct relations that HIR itself owns. Its primary subject
categories include:

- Contract Interface Subject,
- Interaction Subject,
- Operation Subject,
- typed Definition Candidate and IDL Binding Candidate membership,
- the complete 1D-owned Definition Candidate meaning,
- the complete IDL Binding Candidate meaning,
- exact direct semantic references, and
- presence, absence, multiplicity, ordering, cardinality, or another distinction only when the owning law makes that
  distinction semantic.

Contract Interface, Interaction, and Operation are Contract-semantic subjects resolved from IDL meaning. They are not
the
generated Interaction API, generated Operation API, host-language declaration, JVM symbol, or another realization
artifact.

Operation owns an independent nominal callable coordinate surface. Its exact Operation reference, parameter coordinates,
and result position are Primary HIR meaning. Lowering and later generated APIs consume those coordinates; they do not
create their semantic identity. Host parameter order, JVM slot number, generated method descriptor, and physical handle
remain outside that identity law.

This is not one universal record schema. Each 1D authority owns its candidate vocabulary. A typed reference domain or an
authority-specific slab may carry role structurally; HIR does not require a redundant per-row authority tag when the
semantic type already provides that information.

Definition determinants are formation law, not generic visible HIR metadata. Material that participates in a 1D
Definition meaning remains in that meaning under its semantic name. The fact that a value was a formation determinant is
not itself a new HIR semantic field.

Explicit absence is observable only where an owning law makes absence meaningful. HIR does not add one universal
`absent` field to every semantic subject.

---

## 8.3. Stable Semantic Units

HIR must expose semantic units that can be addressed independently of physical layout.

A whole frontend generation is a coherent visibility unit. It is not required to be the only dependency or reuse unit.
A Contract Interface Subject, Interaction Subject, Operation Subject, Definition Candidate, Binding Candidate, or
another
meaningfully independent semantic unit may be consumed separately when the HIR semantics support that separation.

This ADR fixes Contract Interface, Interaction, Operation, Definition Candidate, and IDL Binding Candidate as primary
HIR
semantic subject categories for the current frontend model. It does not freeze the exact payload, reference coordinate,
projection set, or physical representation of those subjects.

---

## 8.4. HIR Semantic Projections and Derived Knowledge

A semantic projection is a deterministic view derived from explicit Primary HIR Semantic Surface inputs. It does not
become a second source of HIR meaning.

A projection must not reopen authored source, repeat 1D authoring refinement, or read hidden mutable compiler state to
recover meaning missing from HIR. It may be computed on demand, memoized, materialized as a side table, or persisted in
a
later compiler product when the same semantic result is preserved.

Examples include reverse-use lookup, Definition subsets, or consumer-specific views over exact direct HIR relations.
Analysis summaries, transitive closure, reachability, optimization hints, diagnostic explanations, fingerprints, cost
models, and similar inferred material are derived compiler knowledge rather than Primary HIR semantic material.

Logical derivation and physical materialization are separate decisions. A hot derived projection may be stored for
performance without becoming HIR authority.

The exact projection catalog remains a compiler design decision. A projection is not a new Contract authority and is not
automatically a new IR level.

---

## 8.5. Reference Direction and Ownership

A resolved HIR reference denotes another semantic subject. It does not physically own that subject merely because the
source syntax was nested or one declaration mentioned another.

The compiler may store related subjects next to one another, but consumers must observe an exact reference relation
rather than infer semantic ownership from containment, parent pointers, object nesting, or table adjacency.

This keeps source hierarchy, semantic reference, and physical storage as separate concerns. It also permits one
definition-level product to be reused without copying every referenced definition into the same physical object graph.

## 8.6. Contract Structure and Compiler Partition

HIR does not invent a new semantic ownership hierarchy when the Contract structure already provides the relevant
Definition and Binding relations.

Reusable Definition Candidate meaning remains defined by its 1D law. An IDL use is represented by a distinct Binding
Candidate. Source nesting and compiler storage grouping do not change either relation.

The compiler may still partition HIR for formation, visibility, reuse, lifetime, or locality. A partition is compiler
organization. It may group several semantic units or isolate one unit when that is profitable. It does not create
Contract ownership, Definition identity, or Binding meaning.

This separation lets physical partitions evolve with measured compiler needs without forcing semantic consumers to
adopt the same topology.

The exact physical partition catalog remains implementation and product-design work.

## 8.7. Exact Semantic Reference and Generation-Local Dense Handle

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

## 8.8. Consumer Access Discipline

Ordinary consumers read HIR through typed semantic subjects, exact references, and semantic projections. They do not
gain
semantic authority by traversing backing objects, parent pointers, table adjacency, or mutable producer state.

The implementation may expose specialized internal access for the HIR producer, verifier, serializer, or representation
preparation. That access is not the stable consumer contract.

Derived reverse indexes, lookup accelerators, and cached navigation tables may be built when useful. They remain
recomputable compiler products unless an owning semantic law explicitly says otherwise.

This access boundary allows a later implementation to introduce lazy materialization or dependency observation without
forcing consumers to learn how HIR is physically stored.

## 8.9. HIR Semantic Access Boundary and Resolved HIR Candidate Protocol

Visible Resolved HIR must expose a stable semantic access boundary between HIR meaning and its physical realization.

```text
HIR Semantic Contract
    ↓ observed through
HIR Semantic Access Boundary
    ↓ realized by
Physical HIR Representation
```

For Authority-Owned Establishment, this boundary realizes the **Resolved HIR Candidate Protocol**. The Protocol defines
the legal observation of resolved candidate meaning. It does not create another IR, another semantic stage, another
Contract authority, or a mandatory physical copy of HIR material.

```text
Resolved HIR Candidate Protocol
    ├── Typed HIR Semantic Reference Domain
    ├── Definition Candidate Projection
    ├── IDL Binding Candidate Projection
    └── Fine-Grained Semantic Projection
```

The Typed HIR Semantic Reference Domain exposes exact current references for the Primary HIR subjects that the legal
consumer must identify. For the current frontend model, those subjects include Contract Interface, Interaction,
Operation, Definition Candidate, and IDL Binding Candidate. Operation-local parameter coordinates and the
Operation-scoped result coordinate remain part of the Operation semantic surface. The Protocol does not infer these
references from source containment, declaration traversal position, backing layout, or generated host signatures.

A Definition Candidate Projection exposes one exact Definition Candidate Reference and the complete producer-owned
Definition Candidate meaning defined by the owning 1D law. It is not weakened according to the needs of one consumer. A
consumer that needs only part of that meaning uses a legal Fine-Grained Semantic Projection rather than redefining the
Definition Candidate Projection.

The owning 1D law decides the candidate vocabulary. Exact authority or semantic role, an authority-scoped resolved
Version candidate coordinate, direct semantic references, Basis Requirement Law, Applicability Law, declared semantic
inputs, or another retained semantic obligation appear in the Definition Candidate Projection only when the owning 1D
law
makes them part of Definition Candidate meaning. A typed projection may carry a distinction structurally. This ADR does
not require one redundant scalar field for every distinction, one generic determinant list, or one universal candidate
record.

A Definition Candidate Projection may expose exact direct semantic references to separately owned Primary HIR subjects.
Those references preserve the referenced subject's ownership and must already be exact. They must not require a consumer
to reopen source, traverse implementation containment, or follow an arbitrary internal chain to discover Candidate
meaning that the frontend failed to resolve.

An IDL Binding Candidate Projection exposes one exact Binding Candidate Reference, the exact selecting HIR semantic
subject or context required to identify that selection occurrence, the exact selected slot or role coordinate, the exact
target Definition Candidate Reference, and only additional qualifiers that the binding law independently owns. It does
not expose the surrounding IDL semantic context merely because that context is reachable from the selection.

The Binding Candidate Projection does not copy the Definition Candidate payload. If the same semantic coordinate appears
in both Candidate and Binding projections, it must independently participate in both meanings. Physical duplication or
lookup convenience is not sufficient. A legal visible Binding Candidate Projection exists only when its target
Definition
Candidate relation is complete and legally referenceable under the HIR seal and visibility laws. An IDL Binding
Candidate
remains pre-authority frontend material. It is not `Version Binding`, `Basis Binding`, Governance Binding, or another
authority-bearing relation established later.

A Fine-Grained Semantic Projection is a deterministic legal view derived from explicit Primary HIR inputs. It must be
semantically complete for the observation it declares. It does not reopen authored source, repeat 1D authoring
refinement, or read hidden mutable state to recover missing candidate meaning. Its semantic equality remains
producer-owned under Sections 8.4 and 9. A fine-grained projection is not a second source of HIR meaning and does not
require one compiler-wide query architecture.

A legal Establishment-facing Protocol view is available only over material that already satisfies the HIR seal,
recovery, visibility, and snapshot-coherence laws in Sections 4.8, 6, 11, and 13. Ordinary Establishment observation is
read-only. Establishment must not bypass the Protocol to recover missing semantic meaning from authored source, host
objects, mutable producer state, backing containment, or physical indexes. Protocol observation itself creates no
Contract authority.

Source provenance is not a default field of a Definition Candidate Projection or IDL Binding Candidate Projection. It
remains a separate relation surface under Sections 8.10 and 10. If an owning Contract law makes an exact source
coordinate part of candidate meaning, that coordinate appears in the candidate meaning under that law rather than
entering through generic provenance.

The Protocol does not expose a slab address, page identity, backing-array position, allocator choice, object topology,
storage-engine layout, cache state, query graph, worker identity, construction schedule, optimizer hint, or backend
layout as candidate meaning. HID, fingerprint, dense handle, table row, and storage address may support physical lookup
or validation but do not become semantic references or semantic equality authority. Recovery, poison, and unresolved
semantic placeholders are not legal Resolved HIR Candidate Protocol material.

The Protocol also does not pre-establish later authority-owned material. An authoritative `Version Binding`, actual
`Basis Binding`, Applicable Basis, Complete Basis, Applicability result, Established Definition Reference, or
Established
Occurrence result appears only at the later semantic boundary that owns it.

This boundary is logical. It does not require an object-oriented interface, virtual dispatch, wrapper allocation, one
accessor call per field, or a physical materialization step. A physical realization may provide inlined primitive
access,
typed bulk reads, contiguous ranges, or another compiler-native access path when those paths preserve the same legal
observation. The same immutable backing may support both candidate observation and later compiler products without
merging their semantic kinds or creating authority.

The first implementation may use primitive slabs. A later implementation may use segmented persistent slabs,
memory-mapped pages, content-addressed chunks, compressed columns, or another representation without changing downstream
HIR meaning. A consumer that requires such a change to rewrite its semantic logic is depending on physical
representation rather than Visible Resolved HIR.

This ADR fixes the logical Establishment-facing Protocol families above and the Primary HIR subject categories in
Section 8.2. It does not fix a concrete object or method API, one class hierarchy, per-1D payload schema, the exact
component set of each 1D Candidate Reference, the complete concrete projection catalog, bulk-access form, handle
encoding, or storage-specific fast path. Those remain owning-1D or compiler design work within the semantic boundary
fixed here.

### 8.9.1. Protocol Evolution and Compatibility

HIR Protocol evolution has independent stability domains. Contract Version, authority-specific HIR semantic-surface
evolution, common Resolved HIR Candidate Protocol evolution, persistent-product format evolution, and compiler
generation are distinct. No one revision coordinate owns or implicitly advances the others.

```text
Contract Version
    !=
authority-specific HIR semantic-surface evolution
    !=
common HIR Candidate Protocol evolution
    !=
persistent-product format evolution
    !=
compiler generation
```

A change in compiler representation, access API shape, persistent encoding, projection implementation, or storage
layout does not create a new Contract Version. A new Contract Version does not require a common HIR Protocol revision
when the current Protocol can already represent every required observation. An authority-specific HIR surface may evolve
without forcing unrelated authority surfaces to evolve.

Protocol compatibility is determined by legal semantic observation, not by revision-number equality. A producer and a
consumer are compatible only when every semantic observation required by that consumer can be supplied with the meaning
required by the current Protocol law, either directly or through an explicit validated migration or projection.

```text
revision equality
format equality
compiler-version equality
decode success
    !=
Protocol compatibility by themselves
```

Revision, capability, schema, or similar metadata may route compatibility checks cheaply. A revision mismatch may
require narrower compatibility validation, migration, or rejection. A revision match does not by itself prove that the
required semantic observation is supported. Compatibility evidence remains compiler evidence and does not become HIR
semantic identity or Contract authority.

Migration is a compiler compatibility operation. It may establish that older material can provide the current required
observation. It does not establish Contract identity, Contract Version equality, Definition equality, or HIR semantic
equality merely because the migration succeeded. A migration must preserve every semantic observation required by its
target Protocol surface and must not invent meaning that the source material did not contain.

This ADR fixes these logical evolution domains and compatibility laws. It does not require one integer Protocol version,
one semantic-versioning scheme, one capability bitmap, one extension registry, or one negotiation encoding. Such
mechanisms are compiler design choices when a physical boundary needs explicit evolution metadata.

### 8.9.2. Unknown Extensions, Unsupported Surfaces, and Downgrade

Protocol handling distinguishes unknown material from unsupported material. Unknown material has an interpretation the
consumer does not understand. Unsupported material has a recognized requirement that the consumer cannot completely
provide under its current supported surface. Neither case may be converted silently into older or simpler semantic
meaning.

An extension is handled according to a Protocol rule already understood by the consumer. The unknown payload does not
get to declare its own safe handling semantics to a consumer that cannot interpret those semantics.

```text
required semantic extension
    unknown or unsupported
        -> fail closed, migrate, or recompute under a compatible path

semantic material outside this consumer's legal observation
        -> RETAIN-HIDE or SEPARATE

explicitly ignorable non-semantic extension
        -> skip permitted

preservation-required unknown material
        -> retain opaquely without interpretation
```

Unknown material may be skipped only when a Protocol rule already understood by the consumer establishes that the
material is not required for that consumer's legal semantic observation. Material may therefore be semantic in HIR while
remaining hidden from one consumer. Hidden does not mean non-semantic, and ignored does not mean erased.

A preservation-required unknown extension may survive a boundary as opaque retained material when its interpretation is
not needed by the current consumer but a later legal consumer or persistent round trip may require it. The current
consumer must not expose that opaque payload as interpreted candidate meaning. Retaining such material does not
automatically make it part of the producer-owned semantic equality law; the owning semantic surface decides whether the
extension participates in equality.

An unsupported Protocol revision or authority-specific surface does not authorize automatic downgrade. Kontrakt may use
an explicit compatible projection or a validated migration only when every semantic observation required by the target
consumer is preserved. A smaller legal observation set is a projection, not a semantic downgrade. Dropping a required
new distinction and pretending that older meaning applies is a semantic downgrade and is forbidden.

```text
complete legal projection
    -> allowed

lossless representation conversion
    -> allowed

validated meaning-preserving migration
    -> allowed

silent semantic downgrade
lossy fallback
unknown-field loss that changes required meaning
fabricated default for missing meaning
    -> forbidden
```

Compatibility processing must not manufacture semantic meaning from absence. A missing value may receive a default only
when the owning semantic law explicitly defines that absence or default as the required meaning.

A stable extension coordinate, tag, field number, or equivalent protocol coordinate must not later be repurposed for an
incompatible semantic interpretation. The exact coordinate representation is design work, but retired coordinates cannot
silently acquire unrelated meaning in material that older producers or consumers may still encounter.

Failure caused by one unknown or unsupported authority-specific extension may remain local to the smallest sound
semantic
dependency boundary. It does not require whole-compiler invalidation when unaffected products remain independently
valid.
When compatibility cannot be established for the affected material, Kontrakt rejects that observation, performs the
required re-resolution or migration, or follows the deterministic clean formation path.

### 8.9.3. Consumer Scope, Reference Lifetime, and Post-Establishment Retention

Visible Resolved HIR has one producer-owned semantic meaning. Different legal consumers may observe different declared
projections of that meaning, but a consumer-specific projection does not create consumer-specific HIR semantics.

Authority-Owned Establishment observes candidate meaning through the Resolved HIR Candidate Protocol defined above.
Frontend diagnostics may observe exact sealed HIR semantic subjects or projections together with the separate provenance
relation and, for an unsuccessful compiler judgment, the Compiler Result Protocol defined by ADR-0074. Tooling and IDE
features may observe sealed HIR semantic projections and provenance when valid resolved meaning exists. Source recovery,
poison, incomplete syntax, and unresolved placeholders remain on separate source or recovery surfaces rather than being
smuggled into ordinary Visible Resolved HIR.

Reuse infrastructure may observe a producer-defined semantic projection, that projection's producer-owned equality law,
and compiler-owned current-validity evidence. HID, fingerprints, Merkle roots, epochs, cache keys, or dependency records
may accelerate that validation. They do not become HIR meaning, reference identity, or equality authority.

After Contract meaning has been established, an authoritative downstream compiler consumer uses the Canonical Contract
World or another later established surface that owns the required meaning. It must not return to raw HIR and treat
pre-authority candidate meaning as an authoritative substitute merely because the backing remains reachable. HIR may
remain available to frontend diagnostics, tooling, provenance projection, or reuse infrastructure under their own legal
observation rules.

A Protocol view and any generation-local observation handle obtained from it are valid only while the owning HIR
generation or independently visible projection remains visible and valid for that observation. A stale handle or stale
Protocol view must not be silently reinterpreted against another generation. Cross-generation use requires an explicit
current-reference resolution, remapping, or validation step that establishes the current target or projection. A stable
semantic coordinate may help perform that remapping when the owning semantic law provides one; it does not transfer
current-generation membership by inheritance.

Creating the Canonical Contract World neither requires HIR to be retained forever nor authorizes immediate destruction
of
HIR that a legal compiler consumer still needs. A HIR generation or projection may retire and its physical storage may
be
reclaimed only after no legal consumer requires that HIR observation and any independently required provenance,
diagnostic, persistence, or reuse material is available through the product that owns that later obligation. Retention
or physical reachability does not extend HIR semantic authority.

The common HIR Protocol therefore has an evolution domain even when no numeric revision is stored. An implementation may
represent compatibility through revision numbers, capability sets, schemas, direct producer-consumer knowledge, or
another compiler-owned mechanism. The normative requirement is preservation of the declared legal observation, not the
presence of one version field.

## 8.10. Visible HIR Representation Preservation

The HIR Semantic Access Boundary defines semantic observation, but semantic accessor equality alone is not
the complete visibility-preservation law.

A physical HIR realization is a valid representation of one Visible Resolved HIR product only when it preserves all
applicable visible-product obligations:

```text
Primary HIR Semantic Surface
+ Provenance Relation Surface
+ Visibility / Generation Protocol
+ Persistence Protocol, when persistence applies
```

Two physical representations may differ in storage, encoding, layout, segmentation, compression, interning, physical
sharing, addressing, or private physical order. Those differences are legal when the visible-product obligations above
remain
unchanged.

Representation preservation does not authorize Contract Canonicalization. A compiler-owned representation change must
not remove, merge, introduce, reinterpret, or establish a Contract-visible distinction.

Semantic order, deterministic observation order, and physical storage order are separate. An owning Contract law
controls
semantic order. The compiler may define a deterministic observation order where semantic order is absent. Physical
layout
may use another order when ordinary consumers cannot observe that layout as meaning.

Private ephemeral state may be nondeterministic when that nondeterminism cannot cross semantic observation, provenance,
visibility, persistence, diagnostics evidence, or another declared deterministic boundary. Deterministic private handle
assignment is still a valid implementation choice, but it is not HIR meaning.

Resource use, memory layout cost, and physical failure strategy are implementation admissibility concerns rather than
HIR semantic equality. An implementation remains subject to compiler-wide determinism, capacity, corruption, and failure
requirements even when it preserves the same HIR semantic result.

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

Equality is defined by the semantic product or projection being consumed. The producer-side HIR or projection law owns
that equality definition; a consumer, cache, query engine, or serialized format does not invent a weaker equality for
its
own convenience.

Whole-generation inequality does not imply that every Contract Interface, Interaction, Operation, Definition Candidate,
Binding Candidate, or projection is unequal. A projection family must therefore define equality strong enough to decide
whether its own consumer-visible meaning changed. Two distinct semantic subjects may expose equal payload projections
without becoming one subject or one Definition.

Generic object equality or serialized-byte equality is not the semantic law. A persisted compiler format may define a
canonical encoding for reproducibility or storage, but that encoding remains separate from HIR semantic equality.

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

# 11. Visibility

## 11.1. Frontend Working State

Before Resolved HIR becomes visible, the frontend may keep private working state while it resolves and refines authored
material. A resolver may use temporary symbol maps, worklists, intern tables, arenas, or another bounded representation
to perform that work.

This working state is compiler implementation material. It is not Resolved HIR. Establishment, query consumers, tooling,
and other independent downstream work must not depend on a half-resolved builder or on the shape of the temporary
structures used to produce HIR.

The frontend may retain source and working material for diagnostics, tooling, or later frontend work. Retention does not
make that material part of visible HIR meaning.

---

## 11.2. Visible Generation

A HIR generation becomes visible only after the producer has completed resolution, satisfied the HIR resolution
invariant, and sealed the generation.

```text
private construction
    ↓
resolution complete
    ↓
HIR invariant validation
    ↓
seal generation G
    ↓
make generation G visible
    ↓
read-only consumers
```

Visibility is a compiler lifecycle boundary. It adds no Contract authority.

A visible generation is immutable to ordinary consumers. The physical realization may use immutable structures,
sealed tables, overlays, snapshots, persistent structures, or another mechanism that preserves this rule.

## 11.3. Visibility Granularity and Materialization

A coherent HIR generation does not require one monolithic physical visibility barrier.

The compiler may make a generation visible as a stable manifest over independently sealed semantic units or projections.
A unit becomes observable only after that unit satisfies the HIR invariant and is associated with the same coherent
generation inputs.

Semantic formation and physical materialization are different. A visible semantic scope must already have closed
current membership and validated HIR candidate meaning for the scope it claims to expose. Physical backing for that
meaning may still be materialized eagerly or on demand.

Demand-driven semantic formation is permitted only when the compiler exposes the smaller semantic scope that has
actually been formed and validated. It must not advertise an unresolved larger generation as complete. When a consumer
receives a visible unit, the same HIR resolution and visibility laws apply as they do to eagerly materialized HIR.

This allows V1 to use a simple eager implementation while preserving a path to fine-grained queries, IDE demand, and
V2 incremental materialization without changing HIR meaning.

The exact visibility and materialization granularity remains a compiler design decision.

---

# 12. HIR Lifecycle and Transition Model

HIR lifecycle is compiler operational meaning. It is not the Contract State / Transition authority defined elsewhere.

The logical lifecycle is:

```text
construction candidate
    ↓ successful validation
validated candidate
    ↓ sealing
sealed generation
    ↓ visibility transition
visible generation
    ↓ newer generation made visible
superseded generation
    ↓ no legal consumer requires it
retired generation
    ↓ physical lifetime ends
reclaimed storage
```

A failed or cancelled construction candidate is discarded. It does not become a partially visible HIR. If an older
generation is already visible, that failed transition does not invalidate the older generation merely because a
replacement attempt began.

Only a successfully completed visibility transition may supersede the previous current generation. Making the newer
generation visible does not mutate the semantic meaning of the older generation. It creates a new visible result for a
new
explicit input set.

Supersession and reclamation are separate transitions. An older generation may remain readable while a newer generation
is already current.

The compiler must not require one mutable lifecycle enum on every HIR node. Lifecycle may be represented by generation
ownership, product metadata, generation handles, or another mechanism.

HIR architecture must permit lifecycle management at a finer semantic granularity than the whole frontend world when
that is useful. Fine-grained lifecycle units are stable semantic products or projections, not arbitrary physical fields.

A visible semantic product may outlive its construction scratch state while cold derived indexes or source-oriented
material are discarded earlier. The HIR contract does not require every projection, index, provenance expansion, or
decoded body to
remain simultaneously materialized. Selective retention must not make a consumer-visible semantic unit unavailable
while that unit is still visible, valid, and required.

The exact reclamation mechanism remains open.

---

# 13. Snapshot Coherence

A semantic computation that requires one coherent HIR world observes one visible generation.

It must not combine a new definition, an old resolution index, and a half-built relation merely because those pieces are
physically reachable at the same time.

Cross-generation reuse is allowed when an equivalent semantic projection has been validated for the new generation. The
logical consumer still observes that projection as valid input to its current generation. The reuse relation does not
create identity continuity between generations.

Current-generation membership is established from current explicit semantic inputs. Reused physical backing, a live old
page, a matching fingerprint, or a retained previous product does not by itself make that subject a member of the new
generation. Reuse may preserve storage or computation; it does not preserve membership by inheritance.

This rule preserves snapshot coherence without requiring the compiler to copy every unchanged definition into every
new physical generation.

---

# 14. Query, Product, and Manager Compatibility

Resolved HIR is a compiler product. It is not a query object and it is not owned by a Manager.

A query-oriented compiler may request whole HIR generations or smaller HIR projections. An Analysis Manager may attach
derived results to an exact HIR subject and validity context. A local Pass Manager may orchestrate HIR-preserving
representation preparation before sealing or over a private generation.

None of those infrastructures defines HIR meaning.

HIR must therefore provide stable semantic subjects, clear generation validity, and an observable read surface without
requiring one query scheduler, one Analysis Manager API, or one pass pipeline.

Derived knowledge remains outside HIR. If several consumers need the same expensive calculation, Kontrakt may expose a
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

Current reuse validity does not require re-executing every computation that originally produced or verified the previous
product. It requires sufficient current-validity evidence. Kontrakt may establish that evidence through retained
dependency evidence, producer-owned semantic equality, validated summaries, explicit compatibility judgments, selective
revalidation, or another sound mechanism. The exact routing and validation mechanism is compiler design.

Validation may proceed from cheaper evidence toward narrower semantic work. When coarse evidence is sufficient to prove
that the relevant semantic projection remains valid, deeper validation is unnecessary. When coarse evidence changes or
is insufficient, Kontrakt may descend only into the affected dependency region or semantic projection. A mismatch in a
fingerprint, HID, Merkle root, epoch, or other routing summary means that deeper validation may be required; it does not
by itself prove that Contract or HIR meaning changed.

If the current semantic projection is equivalent to the previous projection under that projection's producer-owned
semantic equality law, downstream products whose exact semantic inputs are limited to that projection need not be
invalidated further.

```text
current explicit inputs
    ↓
cheap current-validity evidence
    ↓
sufficient?
    ├── yes → validate previous product compatibility
    └── no  → narrow affected dependency / projection
                  ↓
              selective revalidation
                  ↓
              validate previous product compatibility
    ↓
compare consumer-visible HIR result where required
    ↓
meaning unchanged
    ↓
semantic propagation may stop
```

Early cutoff is based on consumer-visible semantic equality, not on the fact that a cache entry exists, a fingerprint
matches, or the aggregate HIR generation changed. A fingerprint or another compact summary may route validation or
reject
a mismatch quickly. It does not prove HIR or Contract equality by itself.

Selecting a previous product for comparison is compiler reuse work and does not create a cross-generation identity
relation.

A provenance-only change may therefore refresh source projection and diagnostics while allowing semantic downstream
products to remain reusable.

Early cutoff is an optimization. Failing to take the cutoff may cost time. Taking an unsound cutoff is a correctness
failure. When reuse validity cannot be established with sufficient current evidence, Kontrakt performs the required
revalidation or follows the deterministic clean recomputation path.

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
completes the IDL Binding Candidate, verifies the result, and makes Resolved HIR visible.

The physical realization may fuse those logical stages. Fusion does not remove their invariants.

## 20.1. Authority-Specific Authoring Refinement

Frontend refinement may erase syntax-only or host-only structure after its meaning is known. It may resolve aliases,
replace lexical references with exact candidate references, remove authoring sugar, and form the explicit absence or
relation material already required by the owning 1D law.

Frontend refinement must not invent a new semantic equivalence. It must not collapse a distinction merely because the
compiler can store the result more compactly. The `Canonicalization Contract`, Admission judgment, Fact Establishment,
Applicability judgment, Policy selection, Governance judgment, and other Contract authorities remain at their owning
boundaries.

## 20.2. Compiler Representation Preparation

Compiler-owned representation preparation may change how resolved HIR material is encoded or stored only when the
visible HIR obligations in Section 8.10 remain preserved.

Legal representation work may include columnar layout, primitive-width packing with exact value preservation, range
compaction, hot/cold column separation, segmentation, paging, lossless compression, out-of-line storage, deterministic
encoding, dictionary encoding with exact recovery, immutable payload interning, and physical payload deduplication.

Physical sharing does not merge semantic entities. Two Definition Candidates may share immutable payload bytes, interned
material, or a compressed backing block while remaining distinct Definition Candidates with distinct references and
provenance relations.

A compiler representation rule must not perform value case folding, Unicode normalization, floating-point distinction
collapse, implicit default insertion, semantic relation inference, semantic declaration elimination, duplicate semantic
edge elimination, Definition Candidate merging, or another transformation that requires an owning Contract law to decide
that a distinction is irrelevant.

The term `Canonicalization` remains reserved for the Contract authority when semantic equivalence is involved. Compiler
storage may use canonical encoding or deterministic observation rules without claiming Contract canonical meaning.

## 20.3. Physical Formation Optimization

Compiler-owned formation optimization may reduce work or improve locality without changing HIR meaning.

Legal techniques include direct-to-slab formation, role-partitioned work ranges, dense current-generation handles,
primitive columnar storage, compact relation ranges, collision-safe interning, parallel fill behind deterministic
visibility, and early release of producer scratch that has no remaining semantic, provenance, diagnostic-evidence, or
reader obligation.

Exact pre-count and exact sizing are permitted when profitable. They are not HIR semantic requirements. A realization
may instead use bounded sizing, deterministic segmented slabs, deterministic chunked formation, or another
representation
that preserves the same visible result.

The compiler may share formation computation or physical payload storage across uses when the relevant current inputs
are equivalent. That sharing does not merge two distinct Definition Candidates. Semantic entity identity remains owned
by the applicable 1D law.

## 20.4. Formation Reuse

Formation reuse is work avoidance. It does not change the logical formation law.

The compiler may reuse a previous Definition or projection only after current semantic determinants, exact references,
current semantic membership, and compiler-owned validity inputs establish that the previous product is a valid
comparison
candidate. Reuse never supplies a missing current determinant, establishes current identity, or carries membership
forward
from the previous generation.

When those conditions cannot be proven, Kontrakt follows the valid clean formation path.

## 20.5. Visible HIR Boundary

Visible Resolved HIR is read-only to ordinary consumers. A compiler optimization does not mutate visible candidate
meaning in place.

Authoring refinement, representation preparation, compaction, interning, verification, or other work used to produce one
visible HIR product occurs before sealing. Later compiler work that needs a different optimized semantic product
exposes a derived compiler product or a later IR instead of silently changing already-visible HIR meaning.

Visible HIR is not required to have one semantic normal form that collapses equivalent Contract material. It is a
verified deterministic semantic baseline that preserves every distinction owned by the applicable Contract laws.

When persistence or reproducible persistence requires canonical compiler bytes, that encoding is a separate compiler
persistence law. Optional compression or storage packaging occurs outside the semantic identity and equality law.

The legal optimization relation is therefore:

```text
same explicit semantic inputs
    ↓
clean / cached / incremental / parallel formation
    ↓
same Visible Resolved HIR obligations
```

A physical optimization that cannot preserve that relation is not a legal HIR optimization.

# 21. Concurrency

Parallel frontend work is allowed only behind deterministic visibility.

Workers may build independent private candidate material. Their completion order must not determine semantic identity,
exact candidate references, visible semantic relations, provenance assignment, deterministic observation order,
persistent product content, or another declared observable result.

Private dense handles, scratch-table positions, worker-local interner IDs, and other ephemeral physical coordinates may
differ across legal executions when those differences remain private. They must not leak into semantic references,
persistent keys, fingerprints that claim stable meaning, diagnostic evidence ordering, serialized products, or another
deterministic boundary.

A deterministic partition, merge, or equivalent visibility law resolves concurrent candidate work into one coherent HIR
generation. Concurrent interning or first-writer races must not decide semantic equality, observable ordering, or stable
persistent output.

Cancellation must not expose a partial generation. A stale worker result for an older input or generation must not
replace a newer valid visible generation.

The exact lock, epoch, persistent-structure, actor, work-stealing, transaction, or parallel-fill mechanism remains
implementation.

# 22. Persistence and Cross-Session Products

A future implementation may persist HIR products across compiler sessions.

Persistence has its own protocol. Runtime HIR representation equivalence does not by itself establish persistent
artifact compatibility. A persisted product must define the compiler schema or product version, integrity rules,
reference remapping law, and any canonical compiler encoding required for reproducibility. Contract Version remains a
separate semantic concern.

An incompatible persistent product is discarded or migrated by an explicit compiler product rule. It is not silently
reinterpreted as current HIR meaning. Persistent-format compatibility and HIR Protocol compatibility are separate. A
format migration may make bytes readable without proving that the restored material can provide every semantic
observation required by the current Protocol. Conversely, a Protocol-compatible semantic product may be stored in a new
persistent format without changing Contract Version or HIR meaning.

Content-addressed storage may be used to deduplicate immutable products. A CAS address remains a storage identity, not a
Contract identity or HIR semantic law. Compression or packaging bytes also remain storage realization. If a canonical
persistent encoding is used as content-addressed input, optional physical compression should not redefine semantic
identity or HIR equality.

A restored persistent HIR product must pass the same semantic resolution boundary as a newly computed product.
Persistent-product integrity, persistent-format compatibility, and current reuse validity are separate questions.
Readable bytes, valid checksums, schema compatibility, successful deserialization, a content-address match, or the
existence of prior verification do not independently establish that a restored product is valid for the current
compilation.

Prior verification is reusable compiler evidence. It may replace re-execution of an expensive check only when Kontrakt
can establish that the semantic determinants, exact semantic references, and compiler-owned reuse-validity inputs on
which that verification depended remain valid for the current product. Current-validity evidence may come from retained
dependency evidence, producer-owned semantic equality, validated summaries, explicit compatibility judgments, selective
revalidation, or another sound mechanism. Reuse does not require re-running every original resolution or verification
step when sufficient current-validity evidence is available.

Validation should descend only as far as required to establish current validity. A cheap higher-level summary may permit
direct reuse when it soundly covers the relevant dependency set. If that evidence changes or is insufficient, Kontrakt
may narrow the affected dependency region and revalidate only the necessary semantic projections. HID, fingerprints,
Merkle roots, epochs, or similar summaries may accelerate routing and validation. They remain compiler evidence and do
not become HIR semantic equality, Contract identity, or authority.

The current validity set must be complete for the product but need not contain unrelated ambient environment. A
compiler,
platform, schema, provider, or environment input participates only when it can affect that product's semantic result,
reference interpretation, verification validity, or other declared reuse condition. A broad version label does not
substitute for a more specific meaning-affecting input when that distinction matters.

When current reuse validity cannot be established, Kontrakt performs the required revalidation or follows the
deterministic clean formation path. Persistent state may make that path unnecessary in the common case. It may not make
old persistent state the only source of correct HIR meaning.

A persistent product must remap or validate current exact HIR references before it exposes generation-local dense
handles. Raw dense handles, page locations, or physical offsets are not persistent semantic references.

---

# 23. Diagnostics and Tooling

Frontend diagnostics may refer directly to HIR semantic subjects and join them with provenance. When a diagnostic
explains
an unsuccessful HIR-related compiler judgment, it observes that result through the Compiler Result Protocol defined by
ADR-0074 rather than reconstructing the result from HIR internals, exception state, stack shape, or diagnostic text.

A diagnostic may combine an explicit compiler result with HIR semantic subjects, source provenance, and separate
compiler
evidence when those relations are valid for the occurrence. HIR preserves the semantic and provenance relations that a
valid diagnostic projection may need, but formatted messages, warning policy, conflict reduction, root-cause analysis,
and diagnostic wording remain Diagnostic subsystem products.

A diagnostic renderer does not own HIR meaning or HIR rejection meaning. A source location does not own HIR identity. A
diagnostic occurrence may fail to render or may require provenance refresh without changing the underlying compiler
result.

A pre-HIR rejection does not require fabricated HIR merely to support diagnostics. Its owning compiler result and source
or recovery provenance remain the diagnostic basis until valid HIR exists.

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

Contract Interface Subject columns
    interfaceOperationRefBase[]
    interfaceOperationRefCount[]

Contract Interface -> Operation relation columns
    interfaceOperationRef[]

Interaction Subject columns
    interactionOperationRef[]
    interactionInputBindingRef[]
    interactionAdmissionBindingRef[]
    interactionCanonicalizationBindingRef[]
    interactionLoweringBindingRef[]

Operation Subject columns
    operationParameterRefBase[]
    operationParameterRefCount[]
    operationResultRef[]
    operation-owned resolved semantic material

Operation Parameter Coordinate columns
    operationParameterRef[]
    operationParameterNominalCoordinate[]

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
    loweringTargetParameterRef[]
    loweringTargetFactCoordinateRef[]

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

The current-reference index is separate from the hot dense-handle path. Operation references are nominal semantic
references scoped by the exact Contract Interface, and Operation parameter references are nominal coordinates scoped by
the exact Operation. Their physical encoding remains open. Other candidate-reference coordinates remain owned by the
relevant 1D laws. HIR does not require one universal tuple or one fixed-width key. HID or fingerprint may accelerate an
index without becoming the reference meaning.

Each 1D authority keeps its own semantic payload family. Kontrakt must not replace those authority-specific meanings
with
one generic property map or universal edge record.

The important shape is:

```text
shared compact HIR infrastructure
+
Contract Interface / Interaction / Operation subject material
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

This example follows one selected Interaction and its Operation from source acquisition to a visible HIR generation.
It shows how the Interaction Manifest binds flat 1D selections for that interaction, how one IDL slot grants a 1D role,
how Definition Candidate and Binding Candidate material stay separate, and how the compiler can still use direct
primitive formation, verification, visibility, early cutoff, and reclamation.

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
selected source form is evidence for frontend refinement. The visible HIR surface is the resolved Admission candidate
meaning and the exact binding from the selected Interaction context to that candidate. The Operation remains the exact
selectable realization handle related to that Interaction; it does not own the flat 1D Contract pipeline.

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
or intern tables. Those structures belong to the producer episode. They are not visible HIR.

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

For the selected `deposit` Interaction, the final hot relation is conceptually two-step.

```text
ix = interactionHandle(DepositContract.deposit.interaction)

ab = admissionBindingHandle(deposit.admission)
ad = admissionDefinitionHandle(DepositAdmission)

interactionAdmissionBindingRef[ix] = ab
admissionBindingTargetRef[ab]      = ad
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

loweringSourceCoordinateRef[base + 0]      = coordinateHandle(DepositInput.accountIdText)
loweringTargetParameterRef[base + 0]        = operationParameterHandle(deposit.command)
loweringTargetFactCoordinateRef[base + 0]   = factCoordinateHandle(DepositCommand.accountId)

loweringSourceCoordinateRef[base + 1]      = coordinateHandle(DepositInput.amountText)
loweringTargetParameterRef[base + 1]        = operationParameterHandle(deposit.command)
loweringTargetFactCoordinateRef[base + 1]   = factCoordinateHandle(DepositCommand.amountMinor)

loweringEdgeCount[lower] = 2
```

These handles are current-generation access coordinates. The semantic relations they realize are exact HIR references.
The source spellings are no longer consulted by ordinary downstream semantic consumers.

Original spelling and source ranges remain reachable through provenance.

The candidate remains non-authoritative. Direct formation does not perform Contract Establishment.

## 25.3. HIR Seal Verification Boundary

Before visibility, the candidate product is checked against the Resolved Contract HIR boundary. This reference model
does not prescribe the verifier subsystem, scan structure, diagnostic code layout, or authority-specific check catalog.

The only requirement fixed here is the boundary:

```text
private candidate material
    ↓
HIR seal verification
    ↓ success
Visible Resolved Contract HIR
```

Seal verification does not establish `DepositAdmission`, `DepositLowering`, or another Contract authority. The
detailed verifier architecture remains owned by separate verifier ADRs and designs.

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
    Contract Interface / Interaction / Operation subject material
    Interaction -> Binding Candidate handles
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
seal generation
    ↓
make generation visible
```

`layout planning` may mean exact pre-sizing, bounded capacity, or deterministic segmented/chunked storage. There is no
required intermediate object graph between resolved frontend facts and visible HIR storage.

Vertical partitioning remains important. Establishment for Admission should not have to touch provenance bytes,
diagnostic strings, Lowering edge slabs, or unrelated authority payloads merely because they share one HIR generation.
The physical partition does not become semantic ownership.

## 25.5. Visibility and Consumer Access

After verification succeeds, Kontrakt makes the sealed slab set visible as one coherent HIR generation.

The visibility transition changes reader access. It does not rewrite semantic rows in place.

```text
working slabs W42
    ↓
complete direct formation
    ↓
verify W42
    ↓
seal slabs
    ↓
make manifest G42 visible
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
DepositContract.deposit.interaction.admission
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

A new generation is produced beside the old one. The old visible slabs are not mutated into the new meaning.

```text
G42 visible and readable
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
make G43 visible
        ↓
G42 superseded for new requests
        ↓
last legal G42 reader releases its pin
        ↓
G42 retired
        ↓
G42 slabs reclaimed
```

A failed W43 formation or verification leaves G42 intact. Visibility and reclamation are separate lifecycle operations.

The exact pinning or reclamation mechanism remains open. Reference counting, epochs, arenas, RCU-like retirement, or
another deterministic-safe strategy may realize the same lifecycle law.

## 25.8. Partition-Local Formation and Failure Isolation

The compiler does not need one mutable HIR tree for the whole project.

A physical HIR partition may contain one semantic product or a deterministic group of related products. Its purpose is
formation locality, visibility, reuse, and lifetime management. The partition does not become Definition ownership or
Binding meaning.

A query that needs `DepositLowering` resolves the exact current candidate reference to its current dense handle and
reads
only the required Lowering ranges.

If one Definition Candidate fails resolution, no poison object is inserted into unrelated valid definition slabs. A
Binding Candidate that depends on the failed target cannot satisfy the HIR invariant. Independent semantic products may
remain valid when their own determinant closure is complete.

V1 may form all partitions eagerly. V2 may materialize or repair selected products on demand. Both paths must expose the
same HIR semantic meaning for the same valid inputs.

---

# 26. Non-Normative Implementation Reading Guide

The reference realization should be read as a compiler storage model, not as a proposed Kotlin domain model.

The parser owns source-oriented syntax arenas. Resolution owns conversion from lexical references to exact HIR targets.
HIR formation writes resolved candidate meaning into typed slabs. HIR verification checks compiler-semantic
well-formedness. Visibility exposes sealed generations. Provenance owns source origin. Query and analysis infrastructure
own dependency, validity, and reuse state. Establishment owns Contract authority.

The production baseline should avoid a per-node heap object graph for high-cardinality HIR material. The preferred
physical direction is deterministic dense ordinals, primitive columns, direct ranges, compact indexes, and FFM-backed or
heap-primitive slabs selected by measured cost.

Temporary frontend objects are permissible only when they are bounded construction aids and do not become the visible
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

It should be possible to compare clean and reused compilation at the Primary HIR semantic boundary. A comment-only edit
should be able to demonstrate semantic equality with changed provenance. A semantic edit should identify the projections
that changed. Randomized worker completion order should not change visible semantic observations, provenance relations,
or deterministic observation order. Private dense-handle values may differ when they remain outside those boundaries.

HIR verification should also be runnable in compiler tests after HIR formation and after any private representation
preparation that claims to preserve the HIR contract. Differential tests should compare Primary HIR semantic
observation,
provenance relation, visibility/generation behavior, and persisted encoding when the product claims a canonical
persistent form. Valid HIR remains a precondition for visibility to later consumers.

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

The cost is explicit architecture work. HIR needs stable semantic references, generation visibility, projection
boundaries, equality rules, and lifecycle ownership. Those costs are accepted because leaving them implicit would move
the same complexity into every downstream subsystem and make V2 invalidation depend on accidental representation.

This ADR does not require HIR to be physically object-heavy. The stronger semantic boundary allows the implementation to
use more aggressive tables, slabs, interning, persistent structures, or other compact representations behind the
visible-product contract. It also allows formation stages to be physically fused, skipped through validated reuse, or
realized
with different layout strategies as long as their logical invariants and visible result remain unchanged.

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

## 29.3. Recovery Nodes Inside Visible Resolved HIR

This weakens the meaning of `Resolved` and forces every consumer to distinguish complete meaning from parser poison.
Recovery remains a sibling frontend concern.

Rejected for the visible HIR semantic surface.

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

HIR keeps its own visibility lifecycle without becoming the global compiler state machine.

Rejected.

## 29.9. Source Containment as HIR Ownership

Using parser nesting, IDL containment, or host object containment as the permanent HIR semantic ownership relation would
make authoring shape control Definition meaning, visibility, reuse, and lifetime. The same reusable 1D declaration may
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

## 29.19. One Semantic Normal Form for Visible HIR

Forcing all semantically equivalent Contract material into one compiler-chosen HIR normal form would let frontend
optimization collapse distinctions owned by Canonicalization or another Contract authority.

Rejected. Visible HIR is a deterministic semantic baseline, not a compiler-owned Contract normal form.

## 29.20. Semantic Access Equality as the Only Representation-Preservation Law

Two backing representations may return the same semantic values while differing in provenance correctness, generation
coherence, stale-reference behavior, or persistent compatibility. Semantic access is central but is not the whole
visible-product contract.

Rejected. Representation preservation also respects provenance, visibility/generation, and applicable persistence laws.

## 29.21. Generic Determinant Metadata as Primary HIR Meaning

Copying formation dependency metadata into every HIR semantic unit would duplicate 1D meaning and make compiler reuse
structure part of HIR semantics.

Rejected. Definition determinants remain an owning 1D formation law, while compiler dependency records remain derived
reuse infrastructure.

## 29.22. Derivable Consumer Views as Primary HIR Fields

Adding reverse indexes, transitive relations, summaries, or consumer-specific convenience fields to HIR because a later
consumer may need them would turn derived knowledge into producer meaning.

Rejected. Such material may be materialized as projections or derived compiler products without becoming Primary HIR
semantic material.

---

# 30. V1 Requirements

V1 must provide a real Resolved Contract HIR boundary between frontend resolution and Establishment.

Visible HIR must satisfy the resolution invariant and remain read-only to ordinary consumers. The compiler must keep
source provenance separate from semantic equality. Contract Interface, Interaction, Operation, Definition Candidate,
and Binding Candidate semantic units must remain independently addressable where their semantic relations require it,
even if the first physical implementation groups their storage.

V1 must expose a Primary HIR Semantic Surface that contains Contract Interface, Interaction, and Operation subjects,
complete 1D-owned Definition Candidate meaning, complete IDL Binding Candidate meaning, exact direct semantic
references,
and owning-law distinctions required by valid consumers. Generated host APIs are downstream products and do not become
Primary HIR meaning. Reverse indexes, summaries, query edges, fingerprints, and other derived material do not become
Primary HIR meaning merely because V1 chooses to materialize them.

V1 must preserve provenance relation, visibility/generation behavior, and any V1 persistence obligations independently
from semantic equality. HIR semantic access is the semantic observation boundary, not the only visibility-preservation
obligation.

V1 frontend formation must obtain a 1D role from explicit IDL binding rather than from user API shape alone. It must
project Definition-determining context through the owning 1D law before forming Definition Candidate meaning. A complete
Binding Candidate is formed only after its exact Definition Candidate target exists.

V1 must resolve a Contract Version Claim to the exact authority-scoped Version candidate coordinate required by HIR
while
leaving authoritative `Version Binding` to Establishment. Basis Requirement Law and Applicability Law remain HIR meaning
when owned by a Definition Candidate; actual Required Basis instances, Basis Binding, Applicability results, and
Complete
Basis remain later authority-owned material.

The same role-qualified declaration may be reused by several IDL bindings when the owning 1D determinant and identity
laws permit one reusable Definition Candidate. Equal payload alone does not merge distinct Definition Candidates.

V1 must preserve exact current Contract Interface, Interaction, Operation, Operation-parameter, Operation-result,
Definition Candidate, and Binding Candidate references separately from generation-local dense handles. Operation and
parameter coordinates are nominal semantic coordinates, and the result reference is an Operation-scoped semantic result
position. None is established by source path, declaration ordinal, generated host symbol, JVM parameter slot, or JVM
descriptor. A dense handle may realize a hot relation inside one generation but cannot become semantic identity,
persistent reference, or history.

V1 may physically fuse role resolution, determinant projection, authority-specific refinement, Definition Formation,
and Binding completion. Fusion must preserve the logical formation order and each stage invariant.

V1 query orchestration must be able to identify explicit HIR inputs, observe product dependencies at stable semantic
boundaries, and reuse a visible result only under a valid generation or equivalent validity rule.

V1 must support deterministic semantic early cutoff where current determinants, exact references, current membership,
producer-owned semantic equality, and reuse validity are already available and the cutoff is profitable. It need not
incrementalize every frontend computation.

V1 may use exact pre-count and pre-sized primitive slabs. It may also use deterministic bounded, segmented, or chunked
formation when measurement favors them. No one layout-planning strategy is part of HIR meaning.

V1 may choose physical partitions for locality, formation, visibility, and lifetime. Those partitions must not create
Definition ownership or Binding meaning, and ordinary consumers must not depend on their topology.

Invalid source must be isolated at the smallest sound semantic dependency boundary. Unaffected semantic products may
remain available to frontend diagnostics and tooling even when the overall compilation cannot succeed. An entered HIR or
frontend judgment that rejects its owned requirement preserves an explicit producer-owned unsuccessful compiler result;
an unentered later HIR judgment receives no synthetic result.

V1 HIR-related compiler results must follow ADR-0074. The exact unsuccessful meaning and bounded Direct Basis remain
producer-owned, diagnostics remain downstream consumers, recovery remains separate, and a legal consumer must not
reconstruct rejection meaning through recursive reference chasing, stack inspection, query topology, or producer-private
storage.

V1 must expose Visible Resolved HIR through the Resolved HIR Candidate Protocol realized by the HIR Semantic Access
Boundary. Establishment-facing observation must preserve the typed HIR semantic reference domain, Definition Candidate
Projection, IDL Binding Candidate Projection, and valid fine-grained semantic projections defined in Section 8.9 without
requiring ordinary consumers to depend on the physical HIR layout. The first backing representation may be primitive
slabs without making that choice part of HIR meaning.

V1 must obey the Protocol evolution and extension laws in Sections 8.9.1 and 8.9.2. Required semantic material cannot be
silently ignored or downgraded. V1 need not persist one numeric Protocol revision or extension registry when its
internal
producer and consumer boundary can establish compatibility by other compiler-owned means.

V1 may use interning, physical payload sharing, deterministic encoding, compression, hot/cold partitioning, or another
representation optimization only under the HIR representation-preservation law. Equal payload does not merge semantic
Definitions, and compiler representation preparation does not perform Contract Canonicalization.

Cache-off and clean-recompute execution must remain valid and must agree with reused execution. Worker count, legal
scheduling order, cache state, and layout strategy must not change observable Visible HIR meaning.

# 31. V2 Evolution Seam

V2 may add persistent HIR products and finer frontend repair without changing this ADR.

Possible work includes persistent semantic projections, demand-driven materialization, incremental lexing and parsing,
incremental resolution, determinant-local Definition Formation, provenance-only refresh, cross-session early cutoff,
dynamic dependency repair, domain-local delta maintenance, and adaptive switching between repair and rebuild.

Previous-product metadata may help locate comparison products or avoid work. It remains compiler reuse infrastructure.
It does not create predecessor identity, successor identity, or semantic lineage for current HIR.

V2 must not constitutionalize HID-, fingerprint-, or Merkle-equality as HIR semantic equality or as the only legal early
cutoff mechanism. Compact identities and hierarchical summaries may route validation, localize likely change, or provide
cheap evidence under a producer-owned validity regime. A mismatch means only that the cheap evidence cannot justify the
old cutoff without further validation. A match permits cutoff only when the producer-owned equality and current-validity
law soundly establishes that the consumer-visible semantic projection is unchanged. Otherwise Kontrakt narrows the
affected region, revalidates, or uses the deterministic clean path.

A V2 formation engine may avoid rebuilding one Definition Candidate only after current role, current 1D semantic
determinants, exact current references, and compiler-owned reuse-validity inputs are sufficient to validate the reused
result. Missing history or persistent state falls back to clean formation.

V2 may choose a different physical formation architecture from V1. Persistent segmented slabs, mapped immutable pages,
content-addressed chunks, database-like product storage, or another representation remain legal behind the same Primary
HIR semantic, provenance, visibility/generation, and persistence laws.

V2 may also carry explicit Protocol revision, capability, extension, or migration metadata when persistent or
distributed
products require it. Such metadata is compatibility and routing evidence. It does not merge Contract Version,
authority-specific HIR surface evolution, common Protocol evolution, persistent-format evolution, or compiler generation
into one revision domain. Unknown required semantic extensions and unsupported surfaces remain fail-closed unless an
explicit compatible projection or validated migration preserves the required observation.

Those techniques remain compiler realization.

V2 must preserve the same resolution invariant, current-reference law, semantic equality, authority boundary, logical
formation law, visibility law, Protocol compatibility law, extension-handling law, and determinism-first rule.

# 32. Intentionally Open

This ADR does not freeze the HIR physical schema.

It does not decide whether V1 uses objects, tables, primitive arrays, slabs, persistent structures, or mixed storage. It
does not require exact pre-count, one global allocation pass, one segmented layout, or one chunking policy. Those are
physical formation choices constrained by the logical formation and determinism laws.

It fixes Contract Interface, Interaction, Operation, Definition Candidate, and IDL Binding Candidate as primary HIR
semantic subject categories for the current frontend model. It also fixes Operation reference as an Interface-scoped
nominal semantic reference, Operation parameter reference as an Operation-scoped nominal semantic coordinate, and the
Operation result reference as an Operation-scoped semantic result position. Section 8.9 additionally fixes the logical
Establishment-facing Protocol families: the typed HIR semantic reference domain, Definition Candidate Projection, IDL
Binding Candidate Projection, and valid fine-grained semantic projections.

It does not fix the remaining complete per-1D payload, 1D candidate reference coordinates, the complete concrete
projection catalog, concrete object or method API shape of the HIR Semantic Access Boundary, bulk-access forms, or
physical handle encoding. It also does not require one numeric HIR Protocol version, semantic-versioning scheme,
capability bitmap, extension registry, extension-coordinate encoding, or compatibility-negotiation wire format. It does
not select a query scheduler, Analysis Manager API, Pass Manager API, fingerprint algorithm, CAS implementation,
reclamation algorithm, serialization format, compression strategy, canonical persistent encoding, or V2 repair
algorithm.

It also does not define the complete semantic payload of each 1D Contract. The owning 1D ADR must still state the
candidate meaning, Definition determinants, and semantic distinctions that HIR has to preserve.

It does not require every deterministic projection to be stored. A projection may be recomputed, memoized, materialized,
or persisted according to compiler product policy without becoming Primary HIR meaning. Each reusable semantic product
or projection still has one producer-owned semantic equality law.

The detailed HIR seal-verifier architecture and authority-specific verification catalog remain outside this ADR.
Separate verifier ADRs and designs may refine those checks without moving owning Contract judgment into HIR seal
verification.

This ADR also does not define the common physical or logical representation of compiler availability effects, boundary
completion, multiple unsuccessful-result membership, recovery decisions, trust-domain invalidation, or deep compiler
evidence. Those are common compiler-result concerns owned by ADR-0074. HIR fixes only the HIR-specific producer
boundary:
valid Visible HIR is published on success, while an entered HIR-owned rejecting judgment preserves its exact
unsuccessful
compiler result without fabricating later HIR meaning.

The exact component set of each 1D Definition Candidate Reference remains open until the owning 1D identity and
reference laws are audited. This ADR does not require one universal `Authority + Contract Id + Version + Local
Coordinate` tuple. It also does not decide which Binding context is Definition-determining for a 1D unless the owning
ADR
already says so.

The exact reclamation algorithm, lease or epoch mechanism, memory policy, and retention cost policy remain design work.
The semantic lifetime law itself is fixed by Sections 8.9.3 and 12: a legal observation cannot outlive the validity of
the
HIR generation or projection that owns it, stale observations cannot be reinterpreted, and physical storage cannot be
reclaimed while a legal HIR consumer still requires that observation.

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

Database MVCC and Linux RCU show a more general visibility lesson. Readers need a coherent view, while making a
new version visible and reclaiming an old version are separate problems. Kontrakt uses that principle for HIR
generations
without adopting database transactions or RCU as compiler semantics.

DBSP and recent incremental data-flow research show that delta maintenance can be powerful when a domain has a suitable
update algebra. They do not justify one compiler-wide incremental algorithm. Recent empirical work on incremental
program analysis also reports meaningful memory and consistency costs, reinforcing the need for a clean deterministic
fallback and domain-specific repair choices.

The *Modern Compiler Architecture 01-15* material provides the same general constraints: stage boundaries are defined by
new invariants, logical stages are separate from physical materialization, semantic identity should be separated from
volatile provenance, visible material should not expose partial construction, and incremental architecture begins with
stable result and dependency boundaries rather than a cache implementation.

---

# 34. Final Law

Resolved Contract HIR is the deterministic visible compiler-semantic form of fully resolved Contract candidates before
Contract authority. It is Kontrakt's highest Contract-semantic IR because established Contract meaning constrains later
realization compilation, machine formation, optimization, and target lowering. Its level is defined by semantic
altitude,
not by proximity to user realization source.

Contract Interface, Interaction, and Operation are primary Contract-semantic subjects in HIR. They are distinct from the
generated Interaction and Operation host APIs that later project established Contract meaning into realization-facing
compiler products.

Explicit IDL binding grants the 1D role. HIR keeps reusable Definition Candidate meaning separate from contextual
Binding
Candidate meaning. Operation and Operation-parameter references are nominal current semantic coordinates scoped by their
Contract semantic subjects, and the Operation result is an Operation-scoped semantic result position. Exact current
references remain separate from dense handles, compiler history, and physical storage.

Contract Version claims are resolved in HIR to exact authority-scoped Version candidate coordinates without creating
`Version Binding`. Basis Requirement Law and Applicability Law remain in HIR when they belong to Definition meaning;
actual Required Basis instances, Basis Binding, Applicability results, and Complete Basis remain at the later authority
boundaries that own them.

It preserves all candidate meaning required by the owning Contract laws and removes frontend ambiguity that later
semantic work must not repeat.

Its Primary HIR Semantic Surface is independent of physical layout, query topology, manager topology, cache state,
generation-local addressing, and incremental algorithm. Authority-Owned Establishment observes that surface through the
Resolved HIR Candidate Protocol realized by the HIR Semantic Access Boundary. The Protocol exposes a typed semantic
reference domain, Definition Candidate Projection, IDL Binding Candidate Projection, and valid fine-grained semantic
projections without imposing one universal candidate record or one physical access mechanism. Provenance remains a
separate visible relation rather than part of candidate payload or HIR semantic equality. Visibility/generation behavior
and applicable persistence rules remain separate obligations of the same Visible HIR product.

HIR generations may expose deterministic semantic projections so compiler products can be reused without turning
consumer views or physical storage into producer meaning. Each projection uses its producer-owned semantic equality law;
equal projection payload does not merge distinct semantic subjects. Derived analysis and query knowledge remains outside
Primary HIR semantics even when it is materialized for performance.

Lifecycle and transition belong to compiler visibility and validity. They do not become Contract State or Transition.
A new generation is made visible instead of mutating already-visible meaning in place, and old-generation reclamation is
separate from visibility.

HIR formation follows a logical semantic order from explicit IDL role through 1D determinant projection and
authority-specific refinement to complete Definition and Binding candidates. A physical implementation may fuse those
stages or avoid work through validated reuse, but it cannot make layout, scheduling, cache state, or optimization policy
part of HIR meaning.

Compiler representation preparation may change encoding, layout, compression, interning, or physical sharing only when
Primary semantic observation, provenance relation, visibility/generation behavior, and applicable persistence behavior
are preserved. It may not create a compiler-owned semantic normal form that preempts Contract Canonicalization or
another
owning authority.

Protocol evolution keeps Contract Version, authority-specific HIR semantic-surface evolution, common HIR Candidate
Protocol evolution, persistent-product format evolution, and compiler generation separate. Compatibility is established
by preservation of the legal semantic observation required by the consumer, not by revision equality alone. Unknown or
unsupported required semantic material is fail-closed unless an explicit compatible projection or validated migration
preserves the required observation. Only material already classified by an understood Protocol rule as ignorable may be
skipped, and semantic meaning may not be silently downgraded, fabricated from absence, or reassigned through a reused
extension coordinate.

HIR-related unsuccessful compiler results follow ADR-0074. The first exact owning compiler judgment establishes the
result. Result meaning remains owned by that producer and is observed through the Compiler Result Protocol. Diagnostics,
recovery, and orchestration do not create or rewrite that meaning. A later HIR or Establishment stage that was never
entered has no synthetic unsuccessful result. Recovery, poison, unresolved placeholders, and invalid partial material do
not become Visible Resolved HIR merely because later tooling or diagnostics need to continue.

One Visible HIR meaning may have consumer-scoped legal projections. Establishment consumes the Resolved HIR Candidate
Protocol; frontend diagnostics and tooling consume sealed semantic projections plus separate provenance and compiler
result surfaces; reuse infrastructure consumes producer-defined semantic projections plus compiler-owned validity
evidence. After Establishment, authoritative downstream consumers use established or canonical surfaces rather than
reinterpreting HIR candidate material as authority. A HIR Protocol view is valid only within its owning visible
generation
or projection validity domain, and cross-generation use requires explicit current-reference remapping or validation.
Canonical Contract World creation alone neither forces HIR retention nor makes stale HIR observation legal.

Caching, persistence, and incremental repair may avoid work. HID, fingerprints, Merkle summaries, epochs, and similar
compact evidence may accelerate validation and change localization, but they do not define HIR semantic equality and do
not prove semantic change merely because they differ. Early cutoff may stop propagation when a consumer-visible
HIR result is unchanged under its semantic equality law. Reused backing does not inherit semantic identity or current
generation membership. Lazy physical materialization is permitted, but a visible semantic scope cannot claim unresolved
or unvalidated membership. None of those mechanisms may change the result that clean deterministic computation would
produce.

The authority boundary remains:

```text
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
```