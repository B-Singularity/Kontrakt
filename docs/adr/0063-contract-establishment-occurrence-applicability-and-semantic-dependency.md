# ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency

## Status

Proposed

## Date

2026-08-29

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v2-reference-architecture-and-v1-foundations-en.md`
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0049: Flow Contract Processing — Fact, Invariant, and Publication
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0055: Policy Contract
- ADR-0056: Governance Contract
- ADR-0057: Failure Contract
- ADR-0058: Publication Contract
- ADR-0059: Output Presentation Contract
- ADR-0060: Diagnostic Evidence and Retention Contract

---

## 1. Context

Kontrakt already uses establishment throughout the Contract Machine.

Input can establish judgeable boundary presentation. Canonicalization can establish a stable representative. Lowering
can form material that is still only a candidate for later authority. Fact authority appears only after the obligations
required for factual establishment have succeeded. State and Transition establish machine condition and legal movement
under their own authority. Failure, Governance, Publication, and Output also establish results that later machine
responsibilities may need to use.

These cases share one problem.

A result may be correct for the authority that produced it without being usable everywhere else. Another Contract may
need that result as basis, but the later Contract must not take ownership of the earlier meaning. A later Version or
Governance decision may change what is applicable next without rewriting what happened before. Diagnostic Evidence may
explain an earlier result without reconstructing that result from logs or later observation.

The existing ADRs already contain local versions of these laws.

A lowered candidate is not automatically Fact. A refused Change Proposal establishes no partial Fact or movement. A
Failure is not rewritten by retry. Old material does not silently gain new Version meaning. An Output result is not
rewritten by later transport. Diagnostic Evidence does not become the authority that produced the source result.

These are not separate accidents. They are consequences of one missing common law.

Kontrakt therefore needs a shared meaning for:

```text
Definition
Occurrence
Judgment
Establishment
Established Material
Applicability
Semantic Dependency
Non-Retroactivity
```

This ADR defines that common law.

It does not replace the Contracts that own the actual results.

---

## 2. Problem

A Contract Definition declares a responsibility. It does not represent one application of that responsibility.

A value may exist before the machine is allowed to rely on it. A condition may be observed before any Contract has
judged what that condition means. A candidate may be complete in memory while still lacking the authority needed by the
next Contract. A previous result may remain stored after it stops being applicable to a later occurrence.

If these states are collapsed, implementation starts deciding meaning.

The following must not become equivalent:

```text
defined
observed
computed
available
established
applicable
retained
published
```

The same separation is needed between Contract authorities.

Suppose one authority establishes material `M` and a second authority uses `M` to establish `N`.

The correct relation is:

```text
Authority A
    establishes M

Authority B
    uses M as declared basis
    establishes N
```

It is not:

```text
Authority B reads M
    therefore B owns M
```

The machine also needs a precise rule for incomplete basis. If one result requires several established inputs, a subset
must not silently create a weaker authoritative result. The owning Contract may define another complete case, but
absence of required material cannot invent one.

The same issue appears across scope. Several local results do not automatically establish a Whole-Machine result. A
higher-scope authority must own the higher-scope judgment.

This ADR must solve these problems without introducing runtime lifecycle, object lifetime, storage lifetime, compiler
cache validity, or distributed commit mechanisms into Contract semantics.

---

## 3. Decision Drivers

Establishment must express semantic authority rather than implementation completion.

The authority that owns a meaning must remain identifiable after other Contracts consume its result.

Definition and occurrence must remain distinct. Equal material from separate occurrences must not merge those
occurrences.

The basis used by a judgment must remain separate from the result established by that judgment.

An established result may be usable by several later responsibilities without transferring its source authority.

Applicability must be exact enough to prevent old or unrelated material from being reused under the wrong Contract
meaning.

The machine must not require one universal context record containing every possible Contract coordinate.

Incomplete required basis must not create partial authority.

Observed or inferred material must not become authoritative merely because nothing currently contradicts it.

Later machine changes must not rewrite earlier establishments.

Physical scheduling must not decide semantic order.

Retention and persistence must remain separate from establishment.

Publication must remain the only authority for outward exposure.

The common law must support a future incremental compiler without defining Contract semantics in terms of queries,
caches, dependency graphs, epochs, or snapshots.

---

## 4. Decision

### 4.1. Definition and Occurrence Are Different

A **Contract Definition** declares one Contract responsibility.

A **Contract Occurrence** is one exact application of that responsibility to a semantic situation governed by that
definition.

```text
Contract Definition
        ↓
zero or more Contract Occurrences
```

One Definition may be used many times.

Two occurrences may produce equal material and still remain different occurrences.

```text
Occurrence A
    -> M

Occurrence B
    -> M
```

This does not make `A` and `B` the same occurrence.

Occurrence is semantic. It is not defined by a method call, thread, object, callback, timestamp, UUID, or stack frame.

This ADR does not require one universal occurrence identifier in the user API or canonical representation. A realization
only needs to preserve occurrence distinction where later Contract meaning depends on it.

---

### 4.2. Judgment and Establishment Are Different

A **Judgment** is an authoritative evaluation owned by a Contract or State-Machine responsibility.

An **Establishment** is the boundary at which source-specific meaning becomes authoritative for one occurrence.

```text
basis
    ↓
owning judgment where required
    ↓
establishment
    ↓
authoritative result
```

Not every establishment needs another local predicate.

Fact establishment is the main example. Invariant and State-Machine authorities may perform the judgments required for
one proposal, while Fact remains the authority for the factual meaning established when those requirements succeed.

The existence of required judgments does not make those judgments the owner of the result they permit.

---

### 4.3. Establishment Grants Authority

Establishment occurs only when the owning responsibility's declared requirements for that result are complete.

Material does not gain authority merely because an implementation has produced it.

```text
candidate exists
    !=
result established
```

The following may realize or carry established meaning, but none defines establishment:

```text
object construction
method return
callback completion
field mutation
message arrival
storage
serialization
cache insertion
```

The backend may change any of these mechanisms while preserving the same Contract meaning.

---

### 4.4. Established Material Keeps Its Source Authority

**Established Material** is the common term in this ADR for source-specific meaning that has received authority.

It is not a new universal Contract type.

A Fact remains Fact. A State remains State. A Failure remains Failure. A Governance Binding remains Governance material.
Publication and Output keep their own result kinds.

The authority that owns that meaning remains its source.

A later Contract may use the material as basis and establish a new result of its own.

```text
Authority A
    establishes M

Authority B
    uses M
    applies B's law
    establishes N
```

Authority B does not acquire authority over `M`.

It must not rewrite what A established or claim that A established a meaning that belonged to B.

---

### 4.5. Basis and Result Remain Separate

Material used by a judgment is not the judgment's result.

```text
basis
    !=
judgment result
```

For example, an observed quantity does not decide whether a Budget was exceeded. A State value does not decide whether
Governance should select one Policy World. A Failure does not decide whether its material may be published.

The authority that owns the later question applies its own law.

This preserves the difference between source material and the conclusion drawn from it.

A later authority may establish a stronger conclusion when its own declared law permits that conclusion. It must still
attribute the source material honestly.

---

### 4.6. Observation Does Not Establish Contract Meaning

Observation alone does not grant Contract authority.

The machine may observe:

```text
filesystem state
OS metrics
hardware signals
runtime values
trace events
external messages
```

Those observations may become useful basis.

They do not become Contract meaning until an authority that is allowed to establish the required meaning judges or
qualifies them under declared law.

Conceptually:

```text
observation
    ↓
declared qualification or judgment
    ↓
established semantic material
```

This ADR does not create one universal Observation Contract and does not decide which future Contract owns every
possible realization-originated qualification.

It only forbids raw observation from acquiring authority by itself.

---

### 4.7. Candidate Meaning Is Not Established Meaning

A candidate may be well-formed and still lack authority.

Lowering may form candidate material. A Change Proposal may contain complete proposed Fact changes. Governance
realization may compute a possible selection. An optimizer may produce a transformed representation.

None becomes authoritative merely because it exists.

The following are also insufficient:

```text
not yet disproved
likely valid
consistent with current observations
accepted by one implementation path
```

Establishment requires the owning semantic boundary to be satisfied.

Absence of contradiction is not establishment.

---

### 4.8. Applicable Context Belongs to the Occurrence

Every occurrence is interpreted under the Contract material actually applicable to that occurrence.

**Applicable Context** means only the Contract coordinates needed to interpret that occurrence honestly.

It is source-specific.

One occurrence may need Version and Governance Binding. Another may also depend on State. A third may not need either.

This ADR does not create a universal nullable context structure.

All coordinates required to interpret an occurrence must be fixed for that occurrence before its result is established.

`Fixed` is a semantic rule. It does not require a memory copy, database snapshot, lock, epoch object, or transaction.

---

### 4.9. Establishment and Applicability Are Separate

An established result is authoritative for its source occurrence.

That does not make it valid basis for every later occurrence.

```text
established
    !=
universally applicable
```

Applicability is a relation between established material and the dependent occurrence that wants to use it.

```text
Established M
        +
Dependent Occurrence O
        +
Applicable Contract meaning
        ↓
M may be used as basis for O
```

The same established material may therefore be applicable to one later occurrence and not another.

Applicability must not be reduced to a permanent Boolean stored on the source material.

---

### 4.10. Semantic Dependency Is Explicit

A **Semantic Dependency** exists when one occurrence requires meaning established by another authority as declared
basis.

```text
Occurrence A
    establishes M

Occurrence B
    requires M
```

The dependent occurrence may use `M` only when the required source meaning has been established and is applicable to B.

Physical availability is not enough.

```text
M exists in memory
    !=
B may rely on M
```

The dependency does not transfer ownership. B owns only the result that B establishes.

---

### 4.11. Required Basis Must Be Complete

The owning Contract decides what basis is required for one occurrence.

If that law requires a complete set, a subset cannot silently establish a weaker result.

```text
required:
    A + B + C

present:
    A + B

result:
    no partial authority
```

The owning Contract may declare several complete alternatives. That is different from treating an incomplete case as
partially valid.

Missing basis does not automatically establish `false`, `Unknown`, `Failure`, or another synthetic result.

Such a result exists only when an authority that owns that meaning establishes it.

---

### 4.12. Coordinated Establishment Is Indivisible

One declared result may depend on several independently judged parts.

The machine may process those parts separately, but authority belongs only to the complete result defined by the owning
law.

```text
all required parts satisfied
    -> establishment

required part missing or refused
    -> no partial establishment
```

This generalizes the existing Change Proposal rule. If a required Invariant or State-Machine judgment refuses the
proposal, no proposed Fact change or movement receives partial authority.

The same law can support a Governance Binding that must be complete across several selected parts.

The physical mechanism used to coordinate that work remains realization.

---

### 4.13. Higher-Scope Meaning Needs a Higher-Scope Authority

Several established local results do not automatically establish an enclosing result.

```text
A establishes MA
B establishes MB

MA + MB
    !=
higher-scope meaning
```

If a higher-scope meaning depends on both, an authority that owns that meaning must apply its own composition law.

```text
MA + MB
    ↓
higher-scope owning judgment
    ↓
MC established
```

This keeps local authority intact and prevents Whole-Machine meaning from appearing through implicit aggregation.

It also preserves Governance scope. Several selected local materials may become basis for one encompassing Governance
decision, but they do not create that decision by themselves.

---

### 4.14. One Established Result May Have Many Consumers

Established material may be used by several later responsibilities.

```text
Established M
    ├── Governance
    ├── Verification
    ├── Test Synthesis
    ├── Diagnostic
    └── Optimization
```

The number of consumers does not create more source authority.

Each consumer may establish only the product owned by its own responsibility.

This allows shared semantic analysis without turning one subsystem into the authority for another.

---

### 4.15. Non-Establishment Has No Implicit Meaning

If an occurrence does not establish a result, the machine must not invent one simply to fill the pipeline.

A later position that is never reached does not establish:

```text
Skipped
Blocked
NotEvaluated
Deferred
```

It was not reached.

A required result that was not established does not automatically mean `false`.

A Failure exists only when Failure law causes the applicable authority to establish one.

Diagnostic Evidence may later explain why a position was not reached. That explanation is not a synthetic source result.

---

### 4.16. Later Occurrences Do Not Rewrite Earlier Establishments

Once an occurrence establishes meaning, later occurrences cannot change what that earlier occurrence meant.

```text
Occurrence O1
    under Context C1
    establishes M1

later

Occurrence O2
    under Context C2
    establishes M2
```

`M2` does not rewrite `M1`.

This applies across the existing machine:

- a retry does not rewrite an earlier Failure;
- a later Governance Binding does not rewrite an earlier governed occurrence;
- a later Version does not silently give old material new meaning;
- later transport behavior does not rewrite an established Output result.

The old result may stop being applicable to later occurrences. It does not stop being the result that was established
for its own occurrence.

---

### 4.17. Succession Is Not Mutation

Some Contracts define replacement, withdrawal, compatibility, or later selection.

Those meanings remain owned by their own ADRs.

This ADR defines only the common rule:

> A later establishment may become applicable to later occurrences under an owning law, but it does not mutate an
> earlier establishment.

There is no universal `current`, `latest`, or `superseded` result in this ADR.

Such a relation needs an exact subject and an authority that owns the succession meaning.

---

### 4.18. Equal Results Do Not Merge Occurrences

Two occurrences may establish equal semantic material.

They still remain separate occurrences where the machine needs to distinguish them.

This matters for retry, Governance rebinding, State movement, diagnostic correlation, and test reproduction.

It does not mean every result identity must include occurrence identity.

Fact identity remains governed by the Fact Contract. Repeated establishment of identical canonical Fact material does
not create artificial Fact multiplicity merely because processing occurred more than once.

Result equality and occurrence distinction are separate relations.

---

### 4.19. Earlier Context Must Remain Honest

Material used to explain an occurrence later must not change the context under which the source result was established.

A later observation may help diagnosis. A later reconstruction may help investigation.

Neither may be presented as material that belonged to the earlier occurrence unless the source authority established
that relation.

```text
Occurrence O
    Basis B1
    Context C1
    -> M

later
    Basis B2
    Context C2
```

The machine must not describe `M` as though `B2` or `C2` had been the basis of O.

This preserves the same temporal honesty required by Failure context and Version meaning.

---

### 4.20. Source Relations Must Remain Intact

A consumer must preserve enough source relation to interpret established material correctly.

The machine must not combine unrelated material and present the combination as one coherent source occurrence.

For example:

```text
result from occurrence A
context from occurrence B
Version from occurrence C
```

must not be presented as one establishment unless an authority explicitly establishes a new meaning from those
materials.

Physical deduplication is allowed.

Shared bytes or shared storage do not imply shared occurrence or shared applicability.

---

### 4.21. Establishment Is Separate from Availability and Retention

Establishment answers whether meaning became authoritative.

Availability answers whether a consumer can currently obtain the material.

Retention answers whether the material remains preserved after a boundary that could otherwise discard it.

These are different responsibilities.

```text
established
    !=
available now

established
    !=
retained

established
    !=
persisted
```

Removing stored material does not rewrite the earlier establishment.

Keeping material for a long time does not increase its authority.

Retention duration and physical persistence remain outside this ADR.

---

### 4.22. Establishment Does Not Grant Outward Authority

Internal authority is not publication authority.

```text
established
    !=
public
```

An established Fact, Failure, Governance Binding, or Diagnostic result may remain internal.

Publication decides what may receive outward authority. Output decides the outward shape where an Output Presentation
applies.

Storage, inspection, debugging, or internal reuse does not bypass those Contracts.

---

## 5. Contract and State-Machine Axes

The Contract Pipeline and State-Machine axis remain separate authorities.

A Contract judgment does not establish State movement merely because the same interaction depends on both.

A legal State movement does not establish another Contract's result.

Where one change requires both axes, their judgments become basis for the complete establishment owned by that change.

This preserves the current Change Proposal model:

```text
proposed Fact change
+
proposed State movement
        ↓
independent Invariant judgment
+
independent State / Transition judgment
        ↓
complete accepted proposal
        ↓
Fact authority and legal movement established together
```

The separate judgments remain attributable to their own authorities.

---

## 6. Relation to the One-Dimensional Pipeline

This ADR does not redefine the 1D Contracts. It provides their shared establishment law.

### 6.1. Input

Outside material does not have Input authority merely because it reached an adapter.

Input owns the boundary presentation established under the selected Input Contract.

### 6.2. Admission

Admission owns the decision that allows or refuses continuation.

A successful Admission result may become basis for later processing. It does not establish Fact authority.

### 6.3. Canonicalization

Canonicalization owns the stable representative required by its equivalence law.

A normalized host value is not authoritative merely because normalization code returned.

### 6.4. Lowering

Lowering owns the declared relation from canonical boundary material toward core-readable candidate material.

A completed candidate is still not Fact unless the required factual and movement obligations succeed.

### 6.5. Fact and Invariant

Invariant owns Fact Integrity judgment.

Fact owns factual authority.

Invariant success may be required for Fact establishment, but Invariant does not become the source of Fact meaning.

The same rule applies when State and Transition judgments are required for one Change Proposal.

### 6.6. Budget and Capacity

Budget and Capacity keep the resource meaning each Contract owns.

Another Contract may use their established results only through declared dependency and applicable context.

Physical resource observations do not transfer that authority.

### 6.7. Policy and Governance

Policy defines its Contract World. Governance decides which declared arrangement governs under Governance law.

Governance may use applicable established material from other authorities as Decision Basis. It does not take ownership
of those source results.

Where one Binding requires several basis elements, Governance cannot gain partial authority from an incomplete set.

The exact rules for Decision Basis, Selection, Scope, Binding, replacement, withdrawal, and Governance Failure remain in
ADR-0056.

### 6.8. Failure

ADR-0057 continues to own Failure meaning.

This ADR generalizes only the shared laws around establishment.

A Failure does not arise merely because later processing could not continue. It is established only where the applicable
authority determines the failed meaning defined by Failure law.

Once established, its source meaning and applicable context are not rewritten later.

### 6.9. Publication and Output

Publication consumes already-authoritative source material and establishes outward authorization under Publication law.

It does not become the source authority for Fact or Failure meaning.

Output consumes Publication-authorized material and establishes the outward result owned by Output Presentation.

Later transport or consumer behavior does not rewrite that result.

---

## 7. Diagnostic Relation

Diagnostic design depends on this ADR rather than redefining its common terms.

A Diagnostic Evidence occurrence may refer to an exact source occurrence and may use established source material. It can
also include qualified observed material where the Diagnostic Contract permits it.

Diagnostic then establishes only its own explanatory meaning.

```text
Source authority
    establishes M

Diagnostic
    refers to M
    establishes E
```

`E` does not replace `M`.

A later observation must remain distinguishable from occurrence-time source material.

Retention of Diagnostic Evidence does not determine whether the source result was established.

ADR-0060 continues to own what Diagnostic Evidence may establish and what Retention requires.

---

## 8. Determinism and Concurrency

Establishment follows semantic prerequisites rather than incidental scheduling.

Changing the following must not change the required Contract meaning:

```text
worker count
thread schedule
completion order
object allocation
cache state
hash iteration order
```

A worker finishing first does not gain semantic priority merely because it finished first.

```text
physical completion order
    !=
semantic establishment order
```

If ordering itself is part of Contract meaning, the authority that owns that order must declare it.

This ADR does not prescribe locks, transactions, CAS, MVCC, RCU, consensus, or scheduler behavior.

Those mechanisms may realize the same semantic law.

---

## 9. Version Relation

Version remains owned by ADR-0053.

This ADR requires only one common rule:

> Established material remains the material established under the Contract meaning applicable to its occurrence.

A later Version does not silently reinterpret old material.

Reuse under another Version requires the compatibility or new judgment required by the owning Contracts.

Stable representation does not imply stable applicability.

```text
same bytes
    !=
same Contract meaning
```

---

## 10. No Universal Semantic Lifetime

This ADR does not introduce a mandatory `SemanticLifetime` object.

The required meaning is already expressed by:

```text
establishment
+
applicability
+
source-specific succession law
```

Material may remain historically established while no longer being applicable to a later occurrence.

That relation is different from memory lifetime, cache lifetime, session lifetime, retention duration, or storage
persistence.

---

## 11. Realization Boundary

The implementation may represent these laws through any structure that preserves them.

Possible realization mechanisms include immutable records, tables, compact IDs, snapshots, query results, caches, or
generated code.

None becomes Contract authority.

In particular, this ADR does not define Contract occurrence or establishment through:

```text
runtime object identity
thread identity
compiler query invocation
cache generation
database transaction
filesystem record
epoch
snapshot object
```

Those mechanisms remain replaceable.

---

## 12. Canonical Representation Requirements

This ADR does not decide the final Semantic IR or Canonical IR layout.

The compiler must nevertheless preserve enough semantic information to represent, where required:

```text
Definition identity
source authority
Occurrence distinction
source-specific established result
Applicable Context relation
Semantic Dependency
non-retroactive succession
```

These meanings do not have to live in one record.

The compiler must not create one universal nullable context object merely because several Contracts use context.

It must not create one universal history or provenance graph merely because several relations can be drawn as edges.

Representation should stay specific to the meaning being represented.

---

## 13. V1 Compiler Requirements

V1 does not need the full incremental architecture planned for V2.

It must establish boundaries that V2 can extend without replacing the semantic model.

### 13.1. Stable Definition Identity

Resolved Contract Definitions need compiler-owned semantic identity.

Host object identity, source traversal order, and backend handles must not own that identity.

### 13.2. Source Authority Must Be Preserved

Compiler products derived from established meaning must keep the exact source authority relation.

Verifier, PBT, Diagnostic, Governance analysis, and Optimization must not reconstruct ownership from implementation
shape.

### 13.3. Semantic Dependency Must Have an Explicit Seam

When one judgment depends on another authority's established material, that relation must survive lowering into
compiler-owned material.

V1 may use a simple representation.

The dependency must not exist only as callback order, global lookup, mutable singleton state, or execution order.

### 13.4. Candidate and Published Compiler Products Stay Separate

Compiler construction may use mutable builders or private workspaces.

Published semantic products must be complete and immutable.

This compiler publication boundary realizes the semantic model but is not itself Contract establishment.

### 13.5. Shared Analysis Must Be Possible

Verifier, PBT, Diagnostic, and Optimization should be able to reuse one derived analysis result when they rely on the
same semantic source.

Each subsystem still owns its own product.

### 13.6. Provenance Must Not Be Semantic Identity

Source position, semantic identity, and occurrence relation must remain separable.

Moving source without changing Contract meaning must not create a different semantic result merely because the line
number changed.

---

## 14. V2 Compiler Consequences

The laws in this ADR should remain usable by the future V2 compiler without making compiler mechanisms part of Contract
semantics.

### 14.1. Contract Occurrence Is Not a Compiler Query

V2 may use demand-driven semantic queries.

A query invocation is a compiler computation.

A Contract Occurrence is semantic.

```text
Contract Occurrence
    !=
Compiler Query Invocation
```

Several queries may analyze one occurrence, while one query over a Definition may describe material that has many later
occurrences.

### 14.2. Semantic Dependency Is Not Query Dependency

A Contract may declare that one occurrence depends on established material from another authority.

The compiler may need several internal queries to compute that relation.

```text
Semantic Dependency
    !=
Compiler Query Dependency
```

The semantic relation may guide the compiler graph. The compiler graph does not define the semantic relation.

### 14.3. Establishment Is Not Cache Validity

A cached compiler result may have been valid for an earlier compiler revision and still be unusable now.

That is a compiler reuse decision.

It does not change what a Contract occurrence established.

Likewise, a cache hit does not prove Contract applicability.

### 14.4. Applicability Enables Precise Reuse

V2 may separately detect that semantic meaning stayed unchanged while source provenance or a dependent context changed.

This allows fine-grained invalidation without rewriting the earlier establishment.

The compiler may stop reuse where applicability changed while retaining the stable source meaning that did not change.

### 14.5. Shared Source Meaning Supports Several Product Subsystems

The same semantic source may feed:

```text
Verifier
PBT and Fixture generation
Diagnostic
Governance analysis
Optimization
Backend planning
```

without being recomputed as several different truths.

V2 should therefore support reusable immutable analysis products with exact source ownership.

### 14.6. Higher-Scope Establishment Supports Summary-Driven Compilation

A compiler summary may preserve selected established facts about one unit.

Whole-Machine analysis may use that summary as basis.

The summary does not establish Whole-Machine meaning by itself.

A Whole-Machine-owned analysis or judgment must still establish the higher-scope result.

This leaves room for Thin-style summary indexes and lazy materialization.

### 14.7. Candidate Separation Leaves Room for Independent Validation

V2 may use a pipeline such as:

```text
transform
    ↓
candidate target representation
    ↓
preservation check
    ↓
published compiler product
```

The preservation check may use IR verification, differential execution, translation validation, or another method.

These are compiler realization choices.

They do not redefine Contract establishment.

### 14.8. Non-Retroactivity Supports Immutable Compiler Generations

A long-lived compiler may hold several immutable generations.

A newer generation can replace the view used by later compiler work without mutating the earlier generation.

MVCC, RCU, generation pinning, and reclamation remain implementation options.

---

## 15. Product-Subsystem Boundary

Verifier, PBT, Diagnostic, and Optimization all consume semantic material but own different products.

### Verifier

The Verifier checks Contract validity or realization preservation.

Its proof or verification product is not the Contract result being checked.

### PBT, Fixture, and Unit-Test Synthesis

Test synthesis derives test objectives from Contract meaning.

A generated test does not establish the Contract it tests.

A passing test does not become source Contract authority.

### Diagnostic

Diagnostic explains a source occurrence.

It does not reconstruct source authority from logs or observations.

### Optimization

Optimization transforms realization while preserving Contract meaning.

The optimized representation does not gain independent Contract authority.

This separation allows all four subsystems to reuse shared analysis without creating an authority chain.

---

## 16. Frontend Boundary

This ADR does not introduce a required user-facing `establish` statement.

It also does not require users to author universal occurrence objects, history objects, or context wrappers.

Users continue to declare the Contracts that own actual meaning.

The compiler derives the common establishment relations after resolution from the selected Contract definitions and
their explicit bindings.

Compiler query keys, epochs, cache keys, runtime IDs, and backend representations must not appear as user-facing
establishment authority.

---

## 17. Consequences

### 17.1. Common Meaning Replaces Repeated Local Definitions

Failure, Governance, Diagnostic, Publication, and later Contracts can rely on one shared meaning of establishment and
applicability.

They no longer need to redefine what it means for source material to have authority.

Their ADRs still own the meaning of their own results.

### 17.2. Failure Keeps Its Local Meaning

ADR-0057 still decides when a Failure exists and what it means.

This ADR only explains why an established Failure is not rewritten later and why non-establishment does not
automatically create Failure.

### 17.3. Governance Can Use Source-Owned Material Directly

Governance may define its Decision Basis in terms of applicable material established by other authorities.

It does not need Diagnostic to become an intermediate authority.

Governance still owns the rules that decide which basis is admissible and which Binding is established.

### 17.4. Diagnostic Becomes Simpler

ADR-0060 can treat source occurrence, source authority, establishment, applicability, and non-retroactivity as existing
common law.

Diagnostic can focus on the explanation it establishes and the Retention obligations that apply to it.

### 17.5. V2 Gains a Stable Semantic Seam

The compiler can assign stable representation to Definitions, dependency relations, and derived results without
confusing them with runtime occurrence objects.

This supports incremental reuse, shared analysis, immutable snapshots, and parallel compilation while preserving
semantic boundaries.

---

## 18. Rejected Directions

### 18.1. Establishment as Runtime Object Creation

Rejected because object construction is replaceable implementation.

The same Contract meaning may be realized without creating the same object shape.

### 18.2. Establishment as Successful Method Return

Rejected because a method return says only that an implementation action completed.

The owning Contract may still require another judgment before authority exists.

### 18.3. Establishment as Observation

Rejected because observed behavior does not become Contract authority by being visible.

Observation must remain qualified until an owning authority establishes the meaning required by a later Contract.

### 18.4. One Universal Context Record

Rejected because different Contracts require different context.

A large nullable record would hide absence and couple unrelated authorities.

### 18.5. One Universal Occurrence Identifier

Rejected because occurrence distinction is semantic while UUIDs, counters, timestamps, and runtime handles are
realization choices.

### 18.6. Automatic Higher-Scope Aggregation

Rejected because several valid local results do not define the meaning of their combination.

A higher-scope authority must own that judgment.

### 18.7. Missing Material as Automatic `Unknown` or Failure

Rejected because non-establishment does not own another result kind.

The authority for the later result must decide what, if anything, is established.

### 18.8. Compiler Cache Validity as Contract Applicability

Rejected because cache reuse is a compiler optimization.

Contract applicability is semantic.

### 18.9. Universal Semantic Lifetime

Rejected because establishment and applicability already express the required semantic relation.

Object lifetime, cache lifetime, persistence, and Retention remain separate concerns.

### 18.10. Universal Establishment Graph

Rejected because Definition, dependency, occurrence, source relation, and control flow are different meanings.

The compiler may use specialized representations for each.

---

## 19. Verification Requirements

The verifier must reject a Contract world or lowering that permits a dependent judgment to claim source-established
basis when the required source authority or applicability relation cannot be resolved.

It must reject a complete-result claim when the owning law requires basis that is not complete.

It must preserve source authority across composition.

Where one result is required to be established indivisibly, no valid lowering may expose a partial authoritative state.

Version or Governance change must not make a previous occurrence appear to have been established under later meaning.

Diagnostic projection must not substitute later observation for occurrence-time source material without preserving the
distinction.

Backend-specific representation must not become the only place from which these semantic relations can be reconstructed.

---

## 20. Implementation Note

This ADR intentionally leaves physical realization open.

V1 may use direct immutable structures and explicit tables.

V2 may add query engines, dependency tracking, persistent caches, summary indexes, parallel evaluation, or immutable
compiler generations.

Those mechanisms are acceptable only when removing or replacing them leaves the Contract meaning defined here unchanged.

The governing test remains:

```text
If the mechanism changes,
does the declared Contract meaning change?
```

If the answer is no, the mechanism belongs to realization.

If the answer is yes, the Contract law must be explicit before the compiler is allowed to rely on it.