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

A later authority must be able to trust such material without taking ownership of its meaning.

The common question is:

> When does Contract material become authoritative, how is that exact meaning identified, and when may another
> responsibility rely on it?

Material before Establishment is not authoritative merely because syntax is valid, resolution succeeded, a compiler
representation exists, or no current analysis rejects it.

The Contract boundary owned by this ADR begins with resolved semantic material presented to Establishment.

```text
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Established Definition / Occurrence Material
```

For definition meaning, Established Definition Material is made available through the Canonical Contract World.

Occurrence material remains separate and exists only where an owning authority defines occurrence meaning.

Source parsing, compiler analysis, optimization, query orchestration, caching, storage, and backend realization are not
Establishment law.

This ADR defines the common Establishment, identity, reference, applicability, and composition rules required across
those authorities.

---

## 2. Problem

Material can exist before it is authoritative.

Parsed source is not yet a resolved Contract definition. Resolved Contract HIR is not yet Established Definition
Material.
A computed candidate is not yet the result it seeks to become. An old result may still be stored even after it is no
longer valid basis for a later judgment.

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

### 4.2.1. Common Established Material Contract

Every Established Material must preserve the complete meaning actually established by its owning authority.

The owning authority decides which semantic distinctions and direct relations are required for that meaning.

The common Contract does not add coordinates merely because another Contract or compiler consumer finds them useful.

When exact later use depends on identity or another established relation, that relation must remain exactly recoverable.

### 4.2.2. No Universal Material Schema

Established Material is a semantic category, not one common payload.

Fact remains Fact. Failure remains Failure. Governance retains ownership of the Binding it establishes.

Different authorities may therefore establish different material shapes and different relation sets.

The common law requires exact, complete source-owned meaning. It does not require a universal `EstablishedMaterial`
object or field set.


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

### 4.3.1. Established Definition Material Contract

Established Definition Material preserves three distinct semantic coordinates:

```text
Definition Meaning

Authority-Owned Definition Identity

Owning Authority Binding
```

**Definition Meaning** is the semantic content established by the owning Contract law.

**Authority-Owned Definition Identity** identifies that definition within the identity domain defined by the owning
authority.

**Owning Authority Binding** is the exact semantic relation from the Established Definition to the authority that owns
that meaning and identity.

Every Established Definition has exactly one Owning Authority Binding.

The three coordinates are distinct, but they are not freely interchangeable. The owning Contract law decides how its
definition meaning determines its authority-owned identity.

Changing the owning authority cannot preserve the same authoritative Definition Reference, even when another authority
could establish equal-looking definition content.

Established Definition Material must also preserve every direct established relation required to interpret that
definition.

Where Basis, Applicability, Version, Policy, Governance, State, or another coordinate belongs to the owning definition
meaning, the exact established relation must remain recoverable.

A coordinate does not become part of every definition merely because another authority owns a relation with that name.

Source provenance remains separate from Definition Meaning, Definition Identity, and Owning Authority Binding.


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

### 4.4.1. Established Occurrence Material Contract

Occurrence material exists only when the owning authority gives one semantic application result meaning of its own.

It must preserve:

```text
the owning authority
the Established Definition being applied
the established result of that application
every occurrence coordinate required by the owning law
```

An Occurrence Reference is required when later meaning depends on which application established the result.

Applicable context belongs to the occurrence only where the owning meaning requires that context to interpret the
result.

Compiler execution, query evaluation, diagnostic retention, or runtime invocation does not create an occurrence by
itself.

Equal occurrence results do not collapse distinct occurrences when the owning law preserves their distinction.

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

### 4.7.1. Establishment Input Contract

Material presented to Establishment must already identify the semantic subject required by the owning law.

Every reference required to interpret that candidate must denote an exact resolved semantic target.

Unresolved lexical choice, parser recovery, host-object containment, realization topology, and compiler heuristics
cannot
complete Establishment input.

The owning law still decides whether the candidate is sufficient.

```text
resolved candidate
+
exact required semantic references
+
required basis meaning
    ↓
owning Establishment law
```

This contract does not require one HIR schema. It defines only the semantic boundary that any frontend representation
must
satisfy before Establishment may rely on it.

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

### 4.8.1. Establishment Output Contract

Successful Establishment produces only the meaning owned by the establishing authority.

It does not implicitly establish:

```text
compiler reachability
optimization facts
diagnostic explanation
realization closure
consumer-specific projection
transitive semantic conclusions
```

If a larger meaning is required, another owning law must establish that larger meaning.

No Establishment result is partial unless the owning law explicitly defines that partial result as complete meaning of
its own.

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

## 6.1. Definition Meaning, Identity, and Authority

Definition Meaning, Authority-Owned Definition Identity, and Owning Authority Binding are separate semantic
coordinates.

```text
Definition Meaning
        │
        │ interpreted under
        ▼
Owning Authority
        │
        │ owns
        ▼
Authority-Owned Definition Identity
```

This separation does not permit compiler realization to assign Contract identity independently of the owning law.

Source path, declaration order, container nesting, host-object ownership, query ownership, storage position, and
compiler
generation do not establish Owning Authority or Definition Identity unless an owning Contract law explicitly gives such
a coordinate semantic meaning.

---

## 6.2. Owning Authority Binding

Every Established Definition has exactly one exact Owning Authority Binding.

```text
Established Definition
    ↓ owned by
exact Contract Authority
```

The binding is semantic. It is not compiler memory ownership or source containment.

The binding must be directly recoverable by a semantic consumer. A consumer must not need to infer authority from a
parent object, table position, generated host type, source nesting, or another compiler product.

The physical identity representation may encode authority together with other identity material. That implementation
choice does not remove the semantic distinction between Definition Identity and Owning Authority Binding.

---

## 6.3. Definition Reference

A **Definition Reference** identifies one exact authoritative definition.

Its semantic form is:

```text
Owning Authority Reference
    +
Authority-Owned Definition Identity
    ↓
exact authoritative Definition
```

An Authority-Owned Definition Identity is therefore not required to be globally unique outside its owning authority.

Equal authority-local identity material under different authorities does not denote the same authoritative Definition.

A Definition Reference must resolve to the same source-owned definition meaning in an equivalent semantic world.

Its physical encoding is compiler realization.

---

## 6.4. Occurrence Reference

An **Occurrence Reference** identifies the exact semantic application to which occurrence-specific Established Material
belongs.

This relation is required when later meaning depends on which application established the source result.

Occurrence identity remains distinct from Definition identity.

A universal runtime occurrence object is not required.

---

## 6.5. Identity Boundaries

Several coordinates may describe material without becoming the same identity.

| Coordinate                          | Meaning                                                              |
|-------------------------------------|----------------------------------------------------------------------|
| Definition meaning                  | What source-owned semantic content was established?                  |
| Authority-owned definition identity | Which definition is this within the owning authority's identity law? |
| Owning Authority Binding            | Which exact authority owns that meaning and identity?                |
| Definition Reference                | Which exact authoritative definition is denoted?                     |
| Occurrence relation                 | Which semantic application does occurrence material belong to?       |
| Source provenance                   | Where did authored material come from?                               |
| Fingerprint                         | Compiler evidence for comparing represented material for a use       |
| Compiler generation                 | Which compiler publication contains a representation?                |
| Local address                       | Where is that representation currently stored?                       |

These coordinates must not be collapsed.

A source-only change may change provenance while preserving the same Definition Reference and Definition Meaning.

A later compiler generation may assign a different local address to the same authoritative definition.

A fingerprint, local address, generation, or compiler-local handle cannot become Definition Identity merely because an
implementation uses it for lookup.

---

## 6.6. Deterministic Reference Resolution

The same Definition Reference in the same semantic world must resolve to the same source-owned definition meaning and
Owning Authority Binding.

Physical ordering, allocation, scheduling, and publication layout cannot change that result.

A new compiler generation may use a different representation while preserving the same semantic reference.

---

## 6.7. Identity Across Linking

Linking preserves the Definition Reference of unchanged source-owned meaning.

Physical relocation or aggregation into a larger compiler world does not mint a new authoritative Definition.

A new authoritative identity appears only when an owning Contract law establishes different meaning or a different
Owning Authority relation.

A higher-scope authority may establish a new higher-scope result without rewriting the identities of unchanged source
definitions it consumes.

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

Basis Resolution establishes the exact semantic source connection owned by the applicable composition law.

A compiler may derive its own computation dependencies from that relation.

The derived dependency is not Contract dependency and cannot replace the established source connection.


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

The **Canonical Contract World** is the compiler semantic substrate through which Established Definition Material is
available for exact later use.

It does not establish a second Contract meaning and is not one aggregate Contract identity.

```text
Contract Authority
    ↓
Established Definition Material
    ↓
Canonical Contract World
```

Occurrence-specific Established Material remains separate unless an owning Contract law defines an explicit relation to
definition-world material.

---

## 10.2. Resolved Contract HIR Boundary

Resolved Contract HIR is non-authoritative compiler semantic material presented to Establishment.

The Canonical Contract World represents definition meaning only after the owning Establishment law succeeds.

A downstream semantic consumer must not need to reopen source syntax or repeat lexical name resolution to recover
authoritative definition meaning.

---

## 10.3. Definition Preservation

The Canonical Contract World must preserve each Established Definition independently of the physical or aggregate
identity of the world representation.

A change to one independently established definition does not by itself rewrite the Definition Meaning, Definition
Identity, or Owning Authority Binding of an unchanged sibling definition.

Physical linking or republishing may change compiler representation without changing unchanged Definition References.

---

## 10.4. Exact Binding Surface

For each Established Definition made available through the world, the exact semantic surface must preserve:

```text
Definition Meaning
Authority-Owned Definition Identity
Owning Authority Binding
Definition Reference
direct established semantic relations
resolved Basis bindings where already established
owned Applicability relations where already established
other authority-owned relations required by the definition meaning
```

Source provenance may be exposed through a separate exact relation.

The world must distinguish a relation that is not owned by an authority from a relation that is required but unresolved.

It must not manufacture missing semantic relations for downstream convenience.

---

## 10.5. World Boundary

The Canonical Contract World does not turn:

```text
compiler analysis
verification
optimization
query dependency
build dependency
storage relation
generated artifact
```

into Contract authority.

The exact representation, indexing, storage, publication, reuse, and incremental mechanisms remain compiler
realization.

# 11. Contract and Representation Boundary

Compiler realization must preserve the semantic distinctions and exact bindings defined by this ADR.

It may change storage, layout, indexing, local handles, materialization, publication, or reuse mechanisms without
changing:

```text
Definition Meaning
Authority-Owned Definition Identity
Owning Authority Binding
Definition Reference
Definition / Occurrence distinction
owned Basis / Applicability / Composition relations
```

No physical representation becomes the source of those semantics merely because the compiler uses it to implement them.

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

The compiler must reject authority that appears without the Establishment law owned by its source.

Every Established Definition must have one exact Owning Authority Binding and one authority-owned Definition identity
sufficient to form an exact Definition Reference.

A Definition Reference must resolve deterministically to the same source-owned meaning and Owning Authority Binding in
an
equivalent semantic world.

A semantic application is invalid when required Basis cannot be resolved or when supplied material is not applicable to
that use.

Where the owning law requires complete Basis, partial authority is invalid.

Composition must preserve the exact source references used by the composing law.

Linking must preserve unchanged Definition References.

Established Definition Material is incomplete when a semantic consumer would need to reconstruct source-owned meaning,
Owning Authority, or an already-established required relation from source syntax, realization topology, compiler
heuristics, or another compiler product.

Established Occurrence Material is invalid when a distinction required by its owning occurrence law is lost.

# 16. Compiler Realization Obligation

Compiler realization must provide exact access to every semantic distinction and binding required by this ADR.

The representation may combine coordinates physically or store them separately.

It must not require a consumer to infer Contract meaning from implementation topology.

Reuse, caching, fingerprinting, generation management, and incremental evaluation may avoid compiler work. They cannot
establish, revoke, merge, or rewrite Contract authority.

---

# 17. Evolution Boundary

Future compiler generations may replace identity encoding, storage, publication, dependency, reuse, or incremental
mechanisms.

Such changes remain compatible only when equivalent semantic worlds preserve the same Definition Meaning, exact
Definition References, Owning Authority Bindings, and authority-owned relations.

Compiler generation, storage identity, and provenance changes therefore remain independently changeable where the
owning Contract meaning is unchanged.

---

# 18. Rejected Directions

## 18.1. Universal Established-Material Object

Rejected because Established Material is a semantic category rather than one physical shape.

A common wrapper would couple unrelated source authorities.

---

## 18.1.1. Universal Established-Material Coordinates

Rejected because not every authority owns the same semantic coordinates.

Basis, Applicability, Version, occurrence context, judgment attribution, and other relations belong to common
Establishment law only where the owning Contract makes them part of its meaning.

Downstream convenience cannot make one universal field set mandatory for every authority.

---

## 18.2. Host-Object Identity

Rejected because object allocation belongs to realization.

Changing the JVM representation must not create new Contract identity.

---

## 18.3. Universal Reference Object

Rejected because Definition Reference and Occurrence Reference are semantic relations.

Their physical representation may differ by compiler layer.

---

## 18.3.1. Structural Ownership as Contract Authority

Rejected because source nesting, parent objects, symbol-table containment, generated host types, and compiler object
ownership belong to representation or source organization.

They do not establish Owning Authority Binding.

---

## 18.3.2. Compiler-Local Handle as Definition Identity

Rejected because local ordinals, pointers, intern identifiers, query keys, and storage handles may change across
compiler
generations or representations.

They may accelerate exact lookup but do not define Contract identity.

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

## 18.11. Verification as Establishment

Rejected because verification judges compiler or realization properties against already-established Contract meaning.

A verified result may become reusable compiler knowledge.

It does not create Contract authority.

---

## 18.12. Optimization as Authority

Rejected because optimization consumes established meaning and preserves required semantics.

A specialization, elimination, or transformed execution form does not establish new Contract meaning merely because the
compiler proved it safe.

---

## 18.13. Generated Artifact Reconstruction

Rejected because generated APIs and other host artifacts are downstream compiler products.

Reconstructing Contract authority from reflection, KSP, host type shape, or another generated artifact would reverse the
authority direction.

---

# 19. Consequences

Established Definition Material now has an explicit semantic binding model.

Definition Meaning, Authority-Owned Definition Identity, and Owning Authority Binding remain distinct.

Every Established Definition has exactly one Owning Authority Binding.

An exact Definition Reference combines the authority reference with the identity defined under that authority.

This prevents source layout, compiler containment, local handles, fingerprints, cache state, and publication generation
from silently becoming Contract identity.

The Canonical Contract World preserves exact Established Definition bindings without turning the whole world into one
aggregate semantic identity.

The compiler may choose different physical representations and future incremental mechanisms as long as those exact
semantic distinctions and bindings remain unchanged.

The cost is explicit binding bookkeeping. The benefit is that later 1D material design and compiler implementation can
derive exact representation obligations from Contract semantics without making one implementation authoritative.

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

## 2026-09-10 — Contract-Aware Compiler Consumption Alignment

The ADR now makes Contract-Aware Analysis an explicit consumer of the Canonical Contract World.

Established applicability may support specialization and pruning, but compiler-derived knowledge, verification, and
optimization remain downstream realization and do not establish Contract meaning.

The amendment also separates Contract Basis and Composition relations from realization call relations, compiler product
or query dependencies, and build or artifact dependencies.

Reference Judgment is clarified as requiring actual candidate or occurrence material and applicable context when it
judges one application. Generated API products remain downstream products and cannot be used to reconstruct Contract
authority.

No Establishment, identity, applicability, Required Basis, Basis Resolution, Composition, or occurrence-integrity law is
changed by this amendment.

## 2026-09-10 — Established Material Contract Closure

The ADR now defines the Contract boundary from Resolved Contract HIR admission through Established Material and the
Canonical Contract World.

An Establishment Input Contract requires exact resolved semantic subjects and references without prescribing one HIR
schema.

A Common Established Material Contract now requires source authority, established meaning, and every semantic
distinction
owned by the source law while rejecting one universal material payload.

Definition Material and Occurrence Material now have separate minimum semantic contracts.

The Canonical Contract World now has an explicit semantic access contract for Established Definition Material.

Compiler analysis, verification, optimization, query, cache, publication, storage, and backend mechanisms remain outside
this Contract boundary.

No physical representation, query architecture, fingerprint, cache, IR layout, or backend mechanism is established by
this amendment.

## 2026-09-10 — Definition Meaning, Identity, and Authority Binding

Established Definition Material now separates Definition Meaning, Authority-Owned Definition Identity, and Owning
Authority Binding.

Every Established Definition has exactly one explicit Owning Authority Binding.

An exact Definition Reference is defined semantically by the Owning Authority Reference together with the
Authority-Owned Definition Identity.

The amendment also removes compiler-consumer, optimization, and query detail that is already owned by the current
compiler architecture and Established Contract World design documents.

Canonical Contract World wording is reduced to the exact semantic binding surface required by Establishment.

Structural containment, compiler-local handles, fingerprints, storage identity, and publication generation remain
compiler realization rather than Contract identity.