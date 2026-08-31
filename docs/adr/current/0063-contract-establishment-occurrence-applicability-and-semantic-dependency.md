# ADR-0063: Contract Establishment, Identity, Applicability, and Composition

## Status

Accepted

## Date

2026-08-30

## Related

- `../../the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-v2-reference-architecture-and-v1-foundations-en.md`
- ADR-0046: Interface Contract Frontend
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

Kontrakt has several authorities whose results become material for later judgment.

A later authority must be able to trust such material without taking ownership of its meaning. The compiler must also
let several products consume the same source without rebuilding that source independently.

The missing common law is therefore broader than one runtime judgment.

> When does Contract material become authoritative, how is that material identified, and when may another responsibility
> rely on it?

The existing design already depends on this distinction. A lowered candidate is not Fact until the required judgments
succeed. The same principle appears when Governance consumes source-owned material or when Diagnostic Evidence refers to
a result established elsewhere.

V2 raises the engineering requirement. One Canonical Contract World must support several independent compiler products
while preserving the same Contract meaning.

```text
Canonical Contract World
    ├── Verification
    ├── Test Synthesis
    ├── Diagnostics
    ├── Optimization
    ├── Linking
    └── Realization
```

None of those products becomes the source authority merely because it consumes the shared semantic substrate.

This ADR defines the common establishment model that makes that architecture possible.

---

## 2. Problem

Material can exist before it is authoritative.

Parsed source is not yet a resolved Contract definition. A computed candidate is not yet the result it seeks to become.
An old result may still be stored even after it is no longer valid basis for a later judgment.

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

A JVM object may be reachable while its meaning is inapplicable. A cache entry may exist while its result is stale for
the current compiler computation. A local ordinal may change even though the semantic identity remains the same.

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

The design must leave room for compact low-level representation without prescribing one storage format.

The individual Contracts must keep ownership of their own result meaning.

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
definition and has accepted the complete semantic definition under the owning Contract law.

```text
authored source
    ↓
semantic resolution
    ↓
Established Definition Material
```

This is the authoritative definition meaning represented by the Canonical Contract World.

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

A cache hit must agree with recomputation.

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
| Fingerprint         | Has relevant compiler material changed?                    |
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

The same rule applies when the compiler builds a summary or republishes a semantic world.

```text
source meaning
    ↓ linking or summary
same source identity
```

A new identity appears only when an owning semantic law establishes different meaning.

This allows Whole-Machine analysis to preserve unit authority while still establishing new Whole-Machine meaning where a
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

A cache entry cannot make material applicable.

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

The compiler may derive a dependency from it for analysis or incremental work.

That dependency is compiler knowledge.

It is not a declaration made by the consuming Contract.

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

A compiler summary may help resolve the connection without acquiring Contract authority.

---

## 8.8. Shared Consumption

One source meaning may serve several compiler products.

Each consumer can retain the exact source reference while using the material for its own purpose.

Shared analysis may also be reused.

Such reuse does not transfer Contract authority.

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

The Canonical Contract World is the compiler-owned semantic substrate that represents Established Definition Material.

It also preserves the authority relations needed by later compiler products.

```text
Contract authority
    ↓
Canonical Contract World
    ↓
compiler consumers
```

Storage alone does not create authority.

---

## 10.2. Cross-Unit Preservation

Material from several compilation units may meet in one Canonical Contract World.

Linking must preserve the source identity of unchanged definitions.

A unit-local physical address may disappear during linking.

The source meaning must not.

---

## 10.3. Semantic Relations

A compiler consumer must be able to recover the source meaning carried by a reference.

It must also be able to test whether supplied material is applicable to its use.

Before composition, an authority knows only the meaning required at its own boundary.

After Basis Resolution, the linked world can also identify which source material satisfied that requirement.

Sections 6 through 8 define these relations.

The Canonical Contract World represents them without redefining their meaning.

---

## 10.4. Derived Compiler Knowledge

Shared analysis may derive compiler knowledge from the Canonical Contract World.

That knowledge remains compiler-owned unless a Contract authority separately establishes its meaning.

A reachability result can support later compiler work without becoming State authority.

---

## 10.5. Independent Products

Compiler products consume the same semantic substrate.

```text
Canonical Contract World
    ├── Verification
    ├── Test Synthesis
    ├── Diagnostics
    ├── Optimization
    └── Realization
```

No product reconstructs Contract authority from another product's private representation.

Shared analysis remains an implementation facility.

# 11. Core Representation Requirements

## 11.1. Value-Based Semantic Core

The semantic core must be representable without host-object identity.

A Kotlin or JVM object may provide a temporary view.

Its allocation identity does not become semantic identity.

This keeps semantic storage free to use a lower-level physical form.

---

## 11.2. Exact Low-Level Reference

A compiler consumer must be able to reach source meaning through an exact compact reference.

Resolving the reference must not require traversal of a compiler-wide object graph.

The same rule applies when one authority refers to material owned by another authority.

---

## 11.3. Local Physical Address

A published compiler generation may assign a local address for efficient access.

That address is not semantic identity.

```text
semantic identity
    !=
local physical address
```

Rebuilding the same semantic world may assign another address without changing Contract meaning.

---

## 11.4. Source-Specific Physical Shape

Each authority may use a physical shape suited to its own semantic material.

The common establishment model does not require one universal payload record.

Material crossing an authority boundary keeps an exact semantic reference rather than entering a shared wrapper
hierarchy.

---

## 11.5. Deterministic Materialization

Physical references visible to later compiler work must be produced deterministically from the semantic world being
published.

Worker completion order cannot choose meaning-bearing addresses.

A different layout is allowed in another generation when semantic identity and reference resolution remain unchanged.

---

## 11.6. Representation Independence

The compiler may change its low-level storage strategy without changing the laws in this ADR.

The semantic requirement is limited to exact identity and deterministic reference.

The physical encoding remains realization.

---

# 12. Relation to Existing Authorities

The existing authorities keep their local semantics.

| Authority           | Common relation defined here                                                            |
|---------------------|-----------------------------------------------------------------------------------------|
| Input               | Input establishes the boundary meaning owned by Input.                                  |
| Canonicalization    | Canonicalization establishes its representative meaning.                                |
| Lowering            | Lowering can produce candidate core material without granting Fact authority.           |
| Fact                | Fact receives factual authority only after its required basis succeeds.                 |
| Invariant           | Invariant owns its integrity judgment without becoming Fact authority.                  |
| State Machine       | State and Transition keep movement authority separate from Contract authority.          |
| Budget              | A Budget result can become source-owned basis for a dependent responsibility.           |
| Capacity            | A Capacity result can become source-owned basis for a dependent responsibility.         |
| Policy              | Policy keeps ownership of its Contract World meaning.                                   |
| Governance          | Governance may require source-owned basis while owning only the Binding it establishes. |
| Failure             | Failure keeps the meaning fixed at the occurrence where Failure law establishes it.     |
| Version             | Version may affect later applicability without reinterpreting earlier material.         |
| Publication         | Publication decides outward authorization without taking source authority.              |
| Output              | Output owns the outward shape after Publication authorization.                          |
| Diagnostic Evidence | Diagnostic may bind to an exact source occurrence while owning only Diagnostic meaning. |

The owning ADR defines each local result.

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

# 14. Whole-Machine Consequences

Whole-Machine linking preserves the source identity of unchanged unit material.

Composition may connect material from one unit to a required basis in another unit.

Neither local Contract needs to name the other authority before that connection is resolved.

A summary may stand in for full material during compiler analysis when it preserves the meaning needed to resolve the
connection.

The summary remains compiler knowledge.

If Whole-Machine semantics require a new result, an owning Whole-Machine law must establish that result.

This keeps semantic composition separate from physical linking.

# 15. Verification Requirements

The verifier must reject authority that appears without the establishment law owned by its source.

A semantic application is invalid when its required basis cannot be resolved.

Resolved material must also be applicable to that application.

Where the owning law requires complete basis, partial authority is invalid.

Composition must preserve the source references used to establish a new result.

Reference resolution must remain deterministic across equivalent semantic worlds.

Linking must not mint new identity for unchanged source meaning.

Backend shape cannot be the only way to recover these relations.

# 16. V1 Foundation Requirements

V1 must implement this model so that V2 can extend the compiler without replacing the semantic foundation.

Resolved definitions need stable compiler-owned semantic identity.

Definition Reference needs an explicit representation seam.

Occurrence-sensitive source relation needs its own seam where the Contract meaning requires it.

Source provenance must remain separate from semantic identity.

Required basis must be represented independently from the source that later satisfies it.

Basis Resolution must preserve the exact source relation after composition.

Applicability must survive lowering as semantic meaning.

Major semantic computations need explicit inputs and deterministic results.

Candidate compiler work must stay private until a complete semantic view is published.

The canonical core must support exact low-level references without requiring object identity.

Whole-Machine linking must preserve source identity across unit boundaries.

Shared analyses may serve several compiler products while remaining compiler-owned.

V1 QA must compare clean recomputation with each alternate execution mode that V1 supports.

# 17. V2 Consequences

The V2 query system may use semantic identity when forming stable compiler keys.

A Definition Reference can survive physical relocation across compiler generations.

Basis Resolution gives the compiler a semantic connection from which query dependency may be derived.

That derived dependency remains compiler infrastructure.

Applicability can be analyzed separately from cache validity.

A fingerprint may decide whether compiler work can be reused.

It does not replace semantic identity.

Incremental compilation may stop propagation when recomputation produces unchanged semantic meaning.

A clean build and an incremental build must converge on the same Contract result.

Parallel execution may change completion order while preserving the same references.

Persistent cache loss may increase work without removing semantic truth.

Summary-driven Whole-Machine analysis may avoid eager materialization when a summary preserves the meaning needed for
Basis Resolution.

The exact query engine remains outside this ADR.

The physical storage layout also remains outside this ADR.

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

Rejected because query state and cache state describe compiler work.

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

# 19. Consequences

Kontrakt gains one common law for material that has acquired source authority.

The same law now explains how 1D results become basis for Governance without transferring authority.

Diagnostic can refer to an exact source occurrence without reconstructing the source judgment.

Whole-Machine linking can preserve unit identity while establishing new higher-scope meaning only where an owning law
requires it.

Independent compiler products can now share one stable semantic substrate.

Determinism becomes part of semantic correctness rather than a property added later by the build system.

The compiler can pursue compact low-level representation because semantic identity no longer depends on wrappers or
object graphs.

V2 can add incremental reuse and parallel evaluation without redefining Contract meaning.

The cost is explicit semantic bookkeeping. The compiler must preserve source identity and applicability instead of
recovering them later from execution order or backend shape.