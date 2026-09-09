# ADR-0063: Contract Establishment, Identity, Applicability, and Composition

## Status

Accepted

## Date

2026-08-30

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-established-contract-world-architecture-todo.md`
- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/v2/kontrakt-v2-reference-architecture-and-v1-foundations.md`
- ADR-0046: Interface Contract Frontend
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0054: Policy Contract
- ADR-0055: Whole-Machine Pipeline Composition and Contract Concurrency
- ADR-0056: Governance Contract
- ADR-0057: Failure Contract
- ADR-0058: Publication Contract
- ADR-0059: Output Presentation Contract
- ADR-0060: Diagnostic Evidence and Retention Contract
- ADR-0064: Input Contract
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0069: Invariant Contract
- ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization

---

## 1. Context

Kontrakt has several authorities whose results become material for later judgment.

A later authority must be able to trust such material without taking ownership of its meaning. The compiler must also
let several products consume the same source without rebuilding that source independently.

The missing common law is therefore broader than one runtime judgment.

> When does Contract material become authoritative, how is that material identified, and when may another responsibility
> rely on it?

The existing design already depends on this distinction. A lowered candidate is not Fact until the required judgments
succeed. The same principle appears when Governance consumes source-owned material or when Diagnostic Evidence refers to
a result established elsewhere.

The V1 compiler architecture makes this boundary explicit. Source syntax and resolved frontend material must remain
separate from material that has received Contract authority. Once definition meaning is established, independent
compiler products must be able to consume the same authoritative semantic substrate without reconstructing that
authority.

```text
Source / Syntax
    ↓
Resolution
    ↓
Resolved Contract HIR
    ↓
Authority-owned Establishment
    ↓
Canonical Contract World
```

The Canonical Contract World can then serve independent compiler products.

```text
Canonical Contract World
    ├── Verification
    ├── Reference Judgment
    ├── Test Synthesis
    ├── Diagnostics
    ├── Generated API Projection
    └── Execution Formation
```

None of those products becomes the source authority merely because it consumes the shared semantic substrate.

This ADR defines the common establishment model that makes that architecture possible.

---

## 2. Problem

Material can exist before it is authoritative.

Parsed source is not yet a resolved Contract definition. Resolved Contract HIR is not yet Established Definition
Material. A computed candidate is not yet the result it seeks to become. An old result may still be stored even after it
is no longer valid basis for a later judgment.

The problem becomes harder when meaning crosses an authority boundary.

```text
Authority A
    establishes M

Authority B
    uses M
    establishes N
```

The machine needs to know exactly what `M` means. It must also preserve who owns that meaning. B may use `M` only when
the relation defined by B permits that use.

Physical availability cannot answer these questions.

A JVM object may be reachable while its meaning is inapplicable. A compiler result may exist while it is not valid basis
for the current semantic application. A local ordinal may change even though the semantic identity remains the same.

Determinism creates another requirement. Equal authoritative input must not produce different Contract meaning because
the compiler used a different worker schedule or a different physical layout.

Kontrakt therefore needs one common model for authority that remains valid across Contract judgment and compiler
realization.

---

## 3. Decision Drivers

The model must make source authority explicit before another responsibility can rely on the material.

Equal semantic basis must establish equal meaning.

Semantic identity must survive changes in physical representation.

A later use must depend on declared applicability rather than compiler reachability.

A Contract must describe the meaning it needs at its own boundary without naming the authority that must provide it.

The compiler core must not require host-object identity to preserve these relations.

The design must leave room for replaceable compiler representation without prescribing one storage format.

The individual Contracts must keep ownership of their own result meaning.

Compiler intermediate representations, derived analysis, scheduling, caching, and publication mechanisms must not create
Contract authority.

---

# 4. Established Material

## 4.1. Establishment

**Establishment** is the semantic boundary at which material receives the authority owned by its source responsibility.

```text
required basis
    ↓
owning law
    ↓
Establishment
    ↓
Established Material
```

The owning law decides what must be complete before authority appears.

A physical event may realize that boundary. The event does not define the authority.

---

## 4.2. Established Material

**Established Material** is semantic material that has received the authority owned by its source.

The term describes a semantic property. It does not define a shared compiler container.

Fact remains Fact after establishment. Failure remains Failure. Governance keeps ownership of the Binding it
establishes.

A later responsibility may require Established Material because the source judgment has already been completed under the
source law.

---

## 4.3. Established Definition Material

Authored source does not carry final Contract authority.

A declaration becomes **Established Definition Material** after Kontrakt has resolved the meaning required by that
definition and the owning Contract law has accepted the complete semantic definition.

The compiler may represent the resolved definition in a high-level intermediate form before establishment.

```text
authored source
    ↓
syntax material
    ↓
resolution
    ↓
Resolved Contract HIR
    ↓
authority-owned Establishment
    ↓
Established Definition Material
```

Resolved Contract HIR is compiler semantic material on the way to authority. Resolution success alone does not establish
Contract authority.

Established Definition Material is the authoritative definition meaning represented by the Canonical Contract World.
Compiler publication does not create this authority. Publication makes a completed compiler representation visible.

---

## 4.4. Established Occurrence Material

An Established Definition may later apply to one semantic situation.

Material produced by that application becomes **Established Occurrence Material** when the owning authority completes
the establishment required for that result.

```text
Established Definition
    ↓
semantic application
    ↓
Established Occurrence Material
```

Definition material states authoritative Contract meaning.

Occurrence material records the result established by one application of that meaning.

Occurrence-specific material is not automatically part of the Canonical Contract World merely because a compiler
consumer may later need it.

---

## 4.5. Contract Occurrence

A **Contract Occurrence** is a distinct semantic application of an Established Definition.

The owning authority decides which semantic coordinates distinguish its applications.

A compiler query is not an occurrence. A runtime call does not define occurrence identity by itself.

Two occurrences may establish equal result material while remaining distinct when later meaning needs that distinction.

---

## 4.6. Judgment

A **Judgment** is an authoritative evaluation owned by a Contract or by the State-Machine axis.

Judgment and Establishment are different relations.

A result may require several judgments before its own authority appears. Fact is the representative case. Invariant may
judge integrity while the State-Machine judges movement. Fact still owns factual authority.

---

## 4.7. Candidate Material

Candidate material can be inspected before it is authoritative.

```text
candidate
    !=
Established Material
```

The target authority decides whether the candidate crosses its establishment boundary.

A candidate also does not gain authority merely because no current analysis rejects it.

---

## 4.8. Source Authority

Established Material keeps the authority that established its meaning.

```text
Authority A
    establishes M

Authority B
    uses M
    establishes N
```

B owns `N`.

A continues to own `M`.

A consumer may establish a new conclusion under its own law. It may not rewrite the source meaning.

---

## 4.9. Minimum Contract Detail and Replaceable Realization

Established Material fixes authoritative semantic meaning. It does not prescribe one physical representation of that
meaning.

The owning Contract must declare each distinction required to preserve its obligation. Identity and applicability belong
to Contract law only when the owning meaning requires them. The same rule applies to judgment, attribution, and direct
semantic relations.

A distinction must not become Contract law merely because the current frontend, compiler, runtime, storage model, or
backend represents material that way.

Contract precision does not mean maximum specification. A detail can over-couple the Contract even when it is described
with semantic vocabulary. Detail without an independent semantic responsibility creates coupling without adding Contract
meaning.

Use this test when deciding whether a detail belongs to the Contract:

```text
detail changes

declared obligation
required semantic distinctions
    remain unchanged

then:
    Contract meaning does not change
    -> detail remains realization
```

The owning authority decides which semantic distinctions are required. A later compiler product, Diagnostic consumer,
Governance consumer, or backend convenience does not create that requirement for the source Contract.

A canonical compiler representation is not automatically canonical Contract meaning. A realization may retain additional
fields, indexes, summaries, or technical identities. Their presence does not enlarge Established Material meaning.

Established Material is semantically immutable once established. Later realization activity may not rewrite the
established meaning.

Semantic immutability does not require one physical immutability mechanism. Storage, layout, sharing, and
materialization may change while the same established meaning is preserved.

Definition Reference, Occurrence Reference, and explicit semantic relations require exact semantic targets and exact
relations. Section 6 defines the common reference requirements. They do not require one pointer, table, graph, or object
representation.

Ordering belongs to Contract law only when the owning law declares the order itself meaningful. Discovery order,
evaluation order, storage order, and physical publication order do not acquire authority from execution.

A representation itself belongs to Contract law only when the owning Contract explicitly makes that representation part
of the declared meaning. Exact bytes may therefore be Contract material when the exact byte sequence participates in a
declared protocol, identity law, or outward obligation. Bytes used only for compiler implementation remain realization.

A realization may be replaced when every Contract-visible meaning and required distinction is preserved. If a backend
cannot preserve an established law, that backend is not a valid realization of the law. The Contract must not be
silently weakened to fit the backend.

The first implementation does not become Contract law merely because it is the first implementation.

Contract Version and compiler representation version are separate concerns. A representation format may change without a
Contract Version change when established meaning is unchanged. A semantic change remains a Contract change even when the
compiler can migrate the physical representation.

---

# 5. Deterministic Establishment

## 5.1. Semantic Determinism

Establishment depends only on semantic material that the owning law treats as relevant.

Equal semantic basis must therefore establish equal meaning.

```text
same semantic basis
    ↓
same established meaning
```

This law defines what may influence the Contract result.

---

## 5.2. Hidden Compiler State

Compiler state cannot change established meaning unless that state has first become applicable semantic material.

A reused compiler result must agree with recomputation.

A different worker schedule must also preserve the result.

This keeps implementation state outside Contract authority.

---

## 5.3. Physical Order

Physical completion order has no semantic authority.

```text
physical completion order
    !=
semantic establishment order
```

If order changes Contract meaning, an authority must declare that ordering relation.

---

## 5.4. Deterministic Composition

Composition obeys the same rule.

When the same source meanings are applicable under the same composition law, the composed meaning must be the same.

The compiler may discover the source material in a different physical order without changing the result.

Rebuilding or republishing the same semantic world under the same semantic basis must not change the established
meaning.

---

# 6. Identity and Reference

## 6.1. Authority-Owned Identity

Semantic identity belongs to the authority that owns the meaning.

A reference to Established Material must therefore preserve the source authority together with the identity defined by
that authority.

```text
source authority
    +
source semantic identity
    ↓
exact source meaning
```

The common compiler infrastructure may carry that relation. It does not redefine the identity law.

---

## 6.2. Definition Reference

A **Definition Reference** identifies authoritative definition meaning.

Compiler products use this relation when they need the Contract law itself rather than one result produced by applying
that law.

The reference remains tied to its source authority.

Its physical representation is compiler realization.

---

## 6.3. Occurrence Reference

An **Occurrence Reference** identifies the exact semantic application to which occurrence-specific Established Material
belongs.

This relation is required when later meaning depends on which application produced the source result.

Diagnostic Evidence is one such consumer because its explanation may need the exact source occurrence.

A universal runtime occurrence object is not required.

---

## 6.4. Identity Boundaries

Several compiler coordinates describe different facts about the same material.

| Coordinate          | Meaning                                                    |
|---------------------|------------------------------------------------------------|
| Semantic identity   | Which source-owned meaning is this?                        |
| Occurrence relation | Which semantic application does this result belong to?     |
| Source provenance   | Where did the authored material come from?                 |
| Fingerprint         | Does compiler material compare as the same for a use?      |
| Compiler generation | Which published compiler view contains the representation? |
| Local address       | Where is that representation stored?                       |

These coordinates must not be collapsed.

A source-only edit may change provenance while preserving semantic identity.

A later compiler generation may assign a different local address to the same meaning.

---

## 6.5. Deterministic Reference Resolution

The same semantic reference in the same semantic world must resolve to the same source meaning.

Physical ordering cannot change that result.

A new compiler generation may use a different local address. The reference still denotes the same semantic identity when
the Contract meaning is unchanged.

---

## 6.6. Identity Across Linking

Linking does not create new semantic identity for unchanged source meaning.

```text
source meaning
    ↓ linking
same source identity
```

A new identity appears only when an owning semantic law establishes different meaning.

This allows Whole-Machine work to preserve unit authority while still establishing new Whole-Machine meaning where a
separate law owns that composition.

---

# 7. Applicability

## 7.1. Applicable Context

Established Material is authoritative under its source meaning.

That does not make it valid basis for every later responsibility.

The **Applicable Context** of an occurrence is the Contract material required to interpret that occurrence under its
owning law.

Each authority defines the context it needs.

No universal context object is introduced.

---

## 7.2. Applicability Relation

**Applicability** answers whether exact Established Material may participate in a dependent semantic application.

The relation starts from the source reference established in Section 6.

```text
exact source meaning
    +
dependent responsibility
    +
required context
    ↓
applicable basis
```

The source material does not carry a permanent `applicable` flag.

Applicability is decided for the dependent use.

---

## 7.3. Deterministic Applicability

Applicability depends on semantic meaning alone.

The same source meaning under the same relevant context must produce the same applicability result.

A compiler cache cannot make material applicable.

Physical reachability cannot do so either.

---

## 7.4. Applicability After Change

Later establishment may change what is applicable to later use.

The earlier establishment remains unchanged.

```text
earlier meaning stays fixed

later meaning may govern later use
```

The authority that owns succession decides when later material replaces what was previously applicable.

No universal mutable `current` result is created here.

---

## 7.5. Version and Governance

Version may participate in Applicable Context when the owning Contract makes Version relevant.

Governance Binding may participate when the dependent law requires a governed arrangement.

Neither coordinate receives universal meaning from this ADR.

The source authority remains responsible for deciding whether the coordinate matters to its own result.

---

# 8. Basis and Composition

## 8.1. Required Basis

An authority defines the meaning required for its own judgment.

It does not name the authority that must produce that material.

```text
required basis meaning
    ↓
own judgment
```

This keeps each Contract independent from the topology that supplies its input.

---

## 8.2. Applicable Basis

Material supplied to a judgment can satisfy the required basis only when it is applicable under Section 7.

The consuming authority judges the supplied meaning.

It does not reconstruct how that material was produced.

```text
required basis
    +
applicable supplied material
    ↓
usable basis
```

---

## 8.3. Basis Resolution

Basis Resolution interprets a source connection owned by the composition that supplies the required basis.

It does not choose a source on behalf of the consuming Contract.

The resolved connection preserves the source reference from Section 6.

If the composition law does not determine the required connection, the basis remains unresolved.

The consuming Contract stays independent from producer topology, while the composed application remains deterministic.

---

## 8.4. Complete Basis

The owning law decides when its basis is complete.

If required material is missing, the result receives no partial authority.

```text
required:
    A + B

available:
    A

result:
    not established
```

Another complete alternative is valid only when the owning law declares it.

Missing basis does not create another result.

---

## 8.5. Composition Authority

Resolving several inputs does not establish a larger meaning by itself.

A separate law must own any meaning created from their combination.

```text
MA + MB
    ↓
composition law
    ↓
MC
```

`MC` belongs to the composing authority.

The source meanings keep their original authority.

---

## 8.6. Derived Dependency

After Basis Resolution connects source material to a required input, the linked semantic world knows that connection.

The compiler may derive analysis or computation dependencies from that established relation.

Those dependencies remain compiler knowledge. They are not declarations made by the consuming Contract.

```text
required basis
    ↓
resolved source connection
    ↓
derived compiler dependency
```

This keeps compiler dependency tracking useful without making one Contract know another Contract's topology.

---

## 8.7. Whole-Machine Composition

Whole-Machine composition may resolve basis connections across unit boundaries.

The local Contract on either side remains independent of that connection.

If Whole-Machine semantics establish a new result from the connected material, a Whole-Machine-owned law must own that
result.

Physical linking does not establish that meaning by itself.

---

## 8.8. Shared Consumption

One source meaning may serve several compiler products.

Each consumer can retain the exact source reference while using the material for its own purpose.

Shared derived analysis may also be reused.

Such reuse does not transfer Contract authority.

---

# 9. Integrity Boundaries

## 9.1. Observation

Observation does not establish Contract meaning.

When observed realization state matters to a Contract judgment, an authority must first establish the semantic meaning
required by that judgment.

```text
observation
    ↓
owned semantic qualification
    ↓
Established Material
```

No general Observation Contract is created.

---

## 9.2. Non-Establishment

If a result is not established, another result does not appear automatically.

An unreached stage has no synthetic result unless an authority defines one.

Failure remains governed by Failure law.

---

## 9.3. Occurrence-Time Integrity

Occurrence-specific Established Material keeps the source relation that belonged to its establishment.

Later material may support a new judgment.

It may not be presented as though it belonged to the earlier occurrence.

This is the common law that lets Diagnostic distinguish source-time material from later reconstruction.

---

## 9.4. Retention

Retention decides whether material remains available after its original processing boundary.

It does not decide whether Establishment occurred.

Removing retained data therefore does not undo earlier authority.

---

## 9.5. Outward Authority

Internal Establishment does not grant outward permission.

Publication owns outward authorization.

Output owns the external result shape.

Internal consumers may use Established Material without bypassing either authority.

---

# 10. Canonical Contract World

## 10.1. Role

The **Canonical Contract World** is the compiler-owned semantic substrate that represents Established Definition
Material and preserves the authority relations required by later compiler products.

```text
Contract authority
    ↓
Canonical Contract World
    ↓
compiler consumers
```

The world provides one authoritative definition view to its consumers. Storage, publication, or compiler traversal does
not create the authority represented there.

---

## 10.2. Resolved Contract HIR Boundary

The Canonical Contract World is not an ordinary IR level.

Resolved Contract HIR is an intermediate compiler representation. It exists so source syntax can become exact,
compiler-usable semantic material before establishment.

```text
Resolved Contract HIR
    = resolved intermediate compiler representation

Canonical Contract World
    = substrate representing already-established definition meaning
```

By the time material reaches Resolved Contract HIR, references required by later semantic work should denote exact
resolved targets rather than require repeated source-name lookup.

HIR may preserve rich Contract vocabulary such as Input, Fact, Invariant, Failure, Governance, Publication, and State.
That vocabulary does not make HIR authoritative before the owning establishment law succeeds.

---

## 10.3. Definition Scope and Occurrence Boundary

The Canonical Contract World represents Established Definition Material.

Occurrence-specific Established Material remains distinct. It exists only where an owning authority defines occurrence
meaning and completes the required occurrence establishment.

Diagnostic, runtime, or compiler convenience does not create a universal occurrence model.

Definition References may therefore enter the Canonical Contract World without requiring every authority to publish one
common occurrence record.

---

## 10.4. Cross-Unit Preservation

Material from several compilation units may meet in one Canonical Contract World.

Linking must preserve the source identity of unchanged definitions.

A unit-local physical address may disappear during linking.

The source meaning must not.

---

## 10.5. Semantic Relations

A compiler consumer must be able to recover the source meaning carried by a reference.

It must also be able to use the semantic relations already established for that material.

A compiler consumer must also be able to determine whether exact Established Material is applicable to the dependent
semantic use under Section 7.

Before composition, an authority knows only the meaning required at its own boundary.

After Basis Resolution, the linked world can also identify which source material satisfied that requirement.

Sections 6 through 8 define these relations.

The Canonical Contract World represents them without redefining their meaning.

---

## 10.6. Derived Compiler Knowledge

Shared analysis may derive compiler knowledge from the Canonical Contract World.

That knowledge remains compiler-owned unless a Contract authority separately establishes its meaning.

A reachability result can support later compiler work without becoming State authority. A computation dependency can
support compiler orchestration without becoming Contract dependency.

---

## 10.7. Independent Products

Compiler products consume the same authoritative semantic substrate as sibling consumers.

```text
Canonical Contract World
    ├── Verification
    ├── Reference Judgment
    ├── PBT / Fixture / Unit-Test Synthesis
    ├── Contract Coverage
    ├── Diagnostics
    ├── Generated API Projection
    └── Execution Formation
```

No product reconstructs Contract authority from another product's private representation.

Shared analysis may serve several products. The shared analysis remains compiler-owned.

Producer-consumer relations do not create an authority chain between the products.

---

# 11. Representation Constraints

## 11.1. Identity Independence

Semantic identity must be representable without host-object identity or physical address.

A Kotlin or JVM object may provide a temporary view. Its allocation identity does not become Contract identity.

---

## 11.2. Exact Semantic Reference

Compiler representations must preserve exact references to authority-owned material where later meaning requires those
relations.

The physical encoding of a Definition Reference or Occurrence Reference remains compiler realization.

A shared wrapper hierarchy is not required.

---

## 11.3. Representation Replaceability

Storage, layout, materialization, and publication mechanisms may change while established meaning and exact semantic
relations remain unchanged.

A physical representation may therefore use different layouts across compiler versions or generations without creating
new Contract meaning.

---

# 12. Relation to Existing Authorities

The existing authorities keep their local semantics.

| Authority           | Common relation defined here                                                               |
|---------------------|--------------------------------------------------------------------------------------------|
| Input               | Input establishes the boundary meaning owned by Input.                                     |
| Admission           | Admission owns its continuation judgment and establishes only the meaning its law defines. |
| Canonicalization    | Canonicalization establishes its representative meaning.                                   |
| Lowering            | Lowering can produce candidate core material without granting Fact authority.              |
| Fact                | Fact receives factual authority only after its required basis succeeds.                    |
| Invariant           | Invariant owns its integrity judgment without becoming Fact authority.                     |
| State Machine       | State and Transition keep movement authority separate from Contract authority.             |
| Budget              | A Budget result can become source-owned basis for a dependent responsibility.              |
| Capacity            | A Capacity result can become source-owned basis for a dependent responsibility.            |
| Version             | Version may affect later applicability without reinterpreting earlier material.            |
| Policy              | Policy keeps ownership of its Contract World meaning.                                      |
| Governance          | Governance may require source-owned basis while owning only the Binding it establishes.    |
| Failure             | Failure keeps the meaning fixed at the occurrence where Failure law establishes it.        |
| Publication         | Publication decides outward authorization without taking source authority.                 |
| Output              | Output owns the outward shape after Publication authorization.                             |
| Diagnostic Evidence | Diagnostic may bind to an exact source occurrence while owning only Diagnostic meaning.    |

The owning ADR defines each local result.

This table does not define the final Established Material schema for any 1D authority. Those meanings remain owned by
their respective ADRs.

---

# 13. Governance and Diagnostic Consequences

## 13.1. Governance

Governance defines the meaning required in its Decision Basis.

It does not declare which Contract must provide that basis.

Composition supplies Established Material to one Governance application.

Section 6 preserves the exact source meaning carried by that material.

Section 7 decides whether the supplied material is applicable.

Governance then judges only its own responsibility and establishes its own Binding.

Raw realization observation cannot bypass this path.

---

## 13.2. Diagnostic

Diagnostic can refer to source meaning without recreating source authority.

A Definition Reference is sufficient when the governing definition is the subject.

An Occurrence Reference is needed when the explanation concerns one exact source application.

Occurrence-time integrity prevents later reconstruction from being presented as original source material.

Retention controls later availability.

Diagnostic remains a consumer of authoritative material, provenance, and compiler-derived evidence. It does not become
the authority source for Governance or another compiler product.

---

# 14. Whole-Machine Consequences

Whole-Machine linking preserves the source identity of unchanged unit material.

Composition may connect material from one unit to a required basis in another unit.

Neither local Contract needs to name the other authority before that connection is resolved.

If Whole-Machine semantics require a new result, an owning Whole-Machine law must establish that result.

Physical linking does not establish the result and does not replace the source authority of unchanged material.

---

# 15. Semantic Validity Requirements

The compiler must reject authority that appears without the establishment law owned by its source.

A semantic application is invalid when its required basis cannot be resolved.

Resolved material must also be applicable to that application.

Where the owning law requires complete basis, partial authority is invalid.

Composition must preserve the source references used to establish a new result.

Reference resolution must remain deterministic across equivalent semantic worlds.

Linking must not mint new identity for unchanged source meaning.

Compiler representation cannot be the only source from which these semantic relations can be inferred.

---

# 16. V1 Foundation Requirements

V1 must implement the establishment boundary without making the current compiler representation authoritative.

The required logical direction is:

```text
Source / Syntax
    ↓
Resolution
    ↓
Resolved Contract HIR
    ↓
Authority-owned Establishment
    ↓
Canonical Contract World
```

Resolved definitions need stable semantic identity.

Resolved references must no longer depend on repeated source-name lookup.

Resolved Contract HIR must remain non-authoritative until the owning Establishment law succeeds.

The Canonical Contract World must represent Established Definition Material without introducing one universal
Established-Material schema.

Definition meaning and occurrence meaning must remain distinct.

Source provenance must remain separate from semantic identity.

Required Basis must remain independent from the source that later satisfies it.

Basis Resolution must preserve the exact source relation after composition.

Applicability must remain semantic meaning across later compiler stages.

Downstream compiler products must consume the Canonical Contract World without reconstructing Contract authority from
source syntax, generated artifacts, another product, or backend shape.

Shared derived analysis may be reused while remaining compiler-owned.

Compiler publication must expose only complete material for the logical stage being consumed. Partial compiler state
must not acquire Contract authority through visibility.

V1 must preserve deterministic semantic results across the execution modes it supports.

---

# 17. Future Compiler Evolution

Future compiler versions may change dependency representation, evaluation strategy, incremental repair, scheduling,
storage, materialization, and publication mechanisms without redefining Establishment.

Such changes must preserve semantic identity, source authority, exact semantic relations, applicability, determinism,
and the distinction between Contract meaning and compiler-derived knowledge.

Compiler reuse may reduce work. It may not create semantic truth.

A clean computation and a reused computation must establish the same Contract meaning from the same semantic basis.

A future incremental architecture may derive its own computation dependencies from established semantic relations. Those
dependencies remain compiler realization and do not become Contract dependencies.

This ADR does not prescribe a query engine, graph traversal policy, invalidation algorithm, repair strategy, cache
model, or physical storage layout.

---

# 18. Rejected Directions

## 18.1. Universal Established-Material Object

Rejected because Established Material is a semantic category rather than one physical shape.

A common wrapper would couple unrelated source authorities.

---

## 18.2. Host-Object Identity

Rejected because object allocation belongs to realization.

Changing the JVM representation must not create new Contract identity.

---

## 18.3. Universal Reference Object

Rejected because Definition Reference and Occurrence Reference are semantic relations.

Their physical representation may differ by compiler layer.

---

## 18.4. Compiler State as Authority

Rejected because compiler orchestration, cache state, and derived analysis describe compiler work.

They do not establish Contract meaning.

---

## 18.5. Observation as Authority

Rejected because observed realization state requires an owning semantic interpretation before a Contract can rely on it.

---

## 18.6. Automatic Composition

Rejected because source results do not establish their own combination.

The larger meaning requires an owning law.

---

## 18.7. Universal Context or Lifetime

Rejected because each authority defines the context required by its own meaning.

Applicability already explains whether established material may participate later.

---

## 18.8. Physical Order as Semantic Order

Rejected because scheduling belongs to realization.

Semantic order exists only where an authority declares it.

---

## 18.9. Canonical Contract World as Universal IR

Rejected because the Canonical Contract World represents already-established Contract definition meaning, while IRs
represent compiler states used to analyze, transform, realize, or lower that meaning.

Collapsing them would mix Contract authority, derived analysis, optimization state, and backend realization into one
representation.

---

## 18.10. Compiler Dependency as Contract Dependency

Rejected because compiler dependencies describe what compiler work determines another compiler result.

Contract dependencies exist only where Contract law establishes the corresponding semantic relation.

A compiler may derive the former from the latter. The derivation does not reverse the authority direction.

---

# 19. Consequences

Kontrakt gains one common law for material that has acquired source authority.

The same law explains how 1D results become basis for Governance without transferring authority.

Diagnostic can refer to an exact source occurrence without reconstructing the source judgment.

Whole-Machine linking can preserve unit identity while establishing new higher-scope meaning only where an owning law
requires it.

The compiler gains an explicit semantic boundary between Resolved Contract HIR and the Canonical Contract World.

The Canonical Contract World can serve independent compiler products without becoming a universal IR or a universal
Established-Material object.

Derived analysis and compiler dependency tracking can reuse established relations without becoming Contract authority.

Determinism remains part of semantic correctness rather than a property added later by compiler scheduling.

Compiler representation can evolve because semantic identity no longer depends on wrappers, object graphs, local
addresses, or one incremental strategy.

The cost is explicit semantic bookkeeping. The compiler must preserve source identity, exact semantic relations, and
applicability instead of recovering them later from execution order or backend shape.

---

# 20. Amendment History

## 2026-09-02 — Contract Detail and Replaceable Realization

Section 4.9 clarifies that Established Material fixes semantic authority rather than one physical representation.

The amendment also makes the Contract-detail boundary explicit. A detail belongs to Contract law only when the owning
authority requires that distinction to preserve its declared meaning.

This amendment does not define new 1D-specific Established Material. The existing establishment, identity,
applicability, composition, and integrity laws remain unchanged.

## 2026-09-09 — Compiler Semantic Boundary Alignment

The ADR now makes the V1 compiler boundary explicit as `Source / Syntax → Resolution → Resolved Contract HIR →
Authority-owned Establishment → Canonical Contract World`.

Resolved Contract HIR is clarified as non-authoritative intermediate compiler material. The Canonical Contract World is
clarified as the semantic substrate representing Established Definition Material rather than an ordinary IR level.

Compiler products are clarified as sibling consumers of the authoritative substrate. Query, incremental, storage,
scheduling, and physical materialization mechanisms remain replaceable compiler realization rather than Establishment
law.

The amendment also removes V2-specific assumptions from the Establishment law. Future incremental architecture may
change without redefining the Contract semantics established by this ADR.