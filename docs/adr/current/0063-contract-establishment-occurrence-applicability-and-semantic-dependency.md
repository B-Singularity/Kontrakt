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

A semantic distinction must not require a separate object, allocation, table, compiler product, or reference hop merely
because the meanings are distinct.

The compiler must remain free to split or fuse physical material according to independent consumption, invalidation,
reuse, lifetime, publication, and access-pattern needs.

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

```text
source-owned judgment
    ↓
Establishment
    ↓
Established Material
```

Establishment does not replace the source kind.

```text
Fact
    → Established Fact

Failure
    → Established Failure

Governance Binding
    → Established Governance Binding
```

The examples do not define a common payload. Each authority keeps ownership of its own result meaning.

### 4.2.1. Common Established Material Contract

Every Established Material preserves:

```text
complete source-owned meaning
    +
semantic distinctions required by the owning law
    +
direct established relations required to interpret that meaning
```

A later consumer may rely on those exact relations. It may not add new source meaning for its own convenience.

### 4.2.2. No Universal Material Schema

Established Material is a semantic category.

```text
Established Material
    ↛ one universal object
    ↛ one universal field set
    ↛ one universal relation set
```

A semantic coordinate exists only where the owning law requires it.

A named semantic coordinate or relation does not become separate Established Material merely because it can be described
independently.

```text
semantic distinction
    ≠ separate Established Material
```

Separate Established Material exists only when an owning Contract law establishes complete meaning of its own.

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

Established Definition Material preserves the authoritative definition meaning and the exact reference required to
identify it.

```text
Established Definition D

Definition Meaning
    → exact source-owned meaning

Definition Reference
    → exact authoritative Definition
    → defined by Section 6

Direct Established Relations
    → only relations owned by D's definition meaning
```

Section 6 owns the common identity model, including Owning Authority Binding, Version Binding, and any
Authority-Local Definition Coordinate required by the owning identity law.

Source provenance remains separate from Definition Meaning and Definition Reference.

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

```text
Established Occurrence O

Occurrence Reference
    → exact semantic application

Definition Reference
    → exact applied Definition

Determining Semantic Basis
    → exact occurrence attribution
    → defined by Section 7.6

Established Result
    → exact occurrence-owned meaning
```

Equal result values do not collapse distinct occurrences when the owning law preserves their distinction.

The occurrence does not copy an entire transitive semantic world merely to preserve attribution.

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

A detail belongs to Contract law only when changing that detail can change a declared obligation or a semantic
distinction required by the owning law.

```text
detail changes

declared obligation
required semantic distinctions
    remain unchanged

then

Contract meaning
    → unchanged

detail
    → realization
```

Contract precision does not require maximum structural specification.

The owning authority declares the distinctions required to preserve its meaning.

It does not require one physical structure for each distinction.

```text
semantic coordinate
    ≠ field requirement

semantic relation
    ≠ pointer requirement

semantic boundary
    ≠ allocation boundary

semantic material
    ≠ compiler-product boundary
```

A compiler consumer, Diagnostic, backend, or first implementation cannot add Contract meaning merely because it needs
another field, index, summary, handle, table, or projection.

Established Material is semantically immutable after Establishment.

```text
same Established Material
    → same semantic meaning

physical representation
    → may split
    → may fuse
    → may move
    → may be projected differently
```

Exact bytes become Contract material only when the owning law explicitly makes those bytes part of a protocol, identity,
or outward obligation.

Section 11 owns the general logical-separation / physical-materialization boundary.
---

# 5. Deterministic Establishment

## 5.1. Semantic Determinism

Establishment depends only on semantic material that the owning law treats as relevant.

```text
same semantic basis
    ↓
same established meaning
```

This law defines the determinant set of the Contract result.

---

## 5.2. Hidden Compiler State

Compiler-only state cannot change established meaning.

A reused result must agree with recomputation under the same semantic basis.

---

## 5.3. Physical Order

Physical completion order has no semantic authority.

```text
physical completion order
    ≠
semantic prerequisite order
```

If ordering changes Contract meaning, the owning law must declare that ordering relation.

---

## 5.4. Deterministic Composition

Composition follows the same law.

```text
same applicable source meanings
    +
same owning composition law
        ↓
same composed meaning
```

Discovery, scheduling, rebuilding, or republication order cannot change the result.

---

# 6. Identity and Reference

## 6.1. Definition Meaning, Identity, and Authority

An Established Definition keeps these semantic concepts distinct.

```text
Established Definition

Definition Meaning
    → what was established

Owning Authority Binding
    → which exact authority owns that meaning

Authority-Owned Definition Identity
    → which Definition that authority identifies

Version Binding
    → exact Version under that authority
      when the authority is version-sensitive

Authority-Local Definition Coordinate
    → additional local coordinate
      only when the owning identity law needs one
```

These concepts may share one physical encoding. They do not become one semantic concept.

Section 11 forbids compiler representation identity from creating any of these coordinates.

---

## 6.2. Owning Authority Binding

Every Established Definition has exactly one exact Owning Authority Binding.

```text
Established Definition
    ↓ owned by
exact Contract Authority
```

The binding is semantic.

It is not compiler memory ownership or source containment.

The binding must be directly recoverable by a semantic consumer.

A consumer must not need to infer authority from a parent object, table position, generated host type, source nesting,
or
another compiler product.

The physical identity representation may encode authority together with other identity material.

That implementation choice does not remove the semantic distinction between Definition Identity and Owning Authority
Binding.

---

## 6.3. Version Binding

A **Version Binding** connects one version-sensitive Contract Authority to one exact Version Identity under that
authority.

```text
Version Binding

Contract Authority A
    → Version Identity V
```

Version Identity is authority-scoped Contract meaning.

A Version label or claim is not globally meaningful by itself.

```text
Authority A
    → Version Stable

Authority B
    → Version Stable
```

The two `Stable` values do not establish Version Agreement merely because their authored spelling is equal.

### Version Claim and Resolution

A **Version Claim** is the declared version coordinate presented for resolution.

It is not yet the resolved Version Binding.

```text
Version Claim
    → Stable

Required Authority
    → A

exact Authority-scoped resolution
    ↓

Version Binding
    → A / Stable
```

Unknown or ambiguous Version Claims remain unresolved.

They must not be mapped implicitly to:

```text
current
latest
preferred
nearest
first available
```

A Version Binding must identify the exact authority and the exact Version Identity selected by Contract law.

Compiler lookup behavior does not select Contract Version.

### Versioned Definition Coordinate

For an authority that owns one independently addressable Definition per Version:

```text
Exact Versioned Definition

Authority A
    +
Version V
        ↓
Definition
```

When one Authority / Version owns several independently addressable Definitions, the owning law must also preserve the
coordinate that distinguishes them.

```text
Exact Versioned Definition

Authority A
    +
Version V
    +
Definition Coordinate L
        ↓
Definition
```

`L` is not a mandatory universal field.

It exists only when the owning identity law needs an additional semantic coordinate.

Declaration order, array position, table position, hash order, source nesting, and compiler discovery order must not
substitute for `L`.

### Exactness

The following coordinates denote one exact Versioned Definition coordinate:

```text
same Authority A
    +
same Version V
    +
same Definition Coordinate L
        ↓
same exact Versioned Definition coordinate
```

A different Version Identity denotes a different exact Versioned Definition coordinate.

```text
same Authority A
    +
different Version
        ↓
different exact Versioned Definition
```

If the same exact `A / V / L` coordinate would establish different Definition Meaning, both meanings cannot be accepted
as
the same Versioned Definition.

ADR-0053 owns the history and merge rules used to resolve that conflict.

Version Binding itself does not invent a compatibility rule or silently merge meanings.

### Why This Choice

Version is Contract meaning, not an artifact revision number.

Keeping Authority, Version, and local Definition coordinates explicit prevents cache identity or serialized format
version from becoming semantic identity.

The compiler may still pack or hash these coordinates for efficient lookup.

---

## 6.4. Definition Reference

A **Definition Reference** identifies one exact authoritative Definition.

For a version-sensitive authority, its semantic form is:

```text
Definition Reference

Owning Authority Reference
    → A

Version Binding
    → V

Authority-Local Definition Coordinate
    → L
      when required by the owning identity law

        ↓

exact authoritative Versioned Definition
```

The earlier `Authority-Owned Definition Identity` remains the identity owned by `A`.

Version Binding and the local coordinate are explicit semantic components of that authority-owned identity law.

An Authority-Local Definition Coordinate is not required to be globally unique outside its Authority / Version.

Equal local coordinates under different authorities or different Versions do not denote the same authoritative
Definition.

A Definition Reference must resolve to the same source-owned Definition Meaning in an equivalent semantic world.

Its physical encoding is compiler realization.

---

## 6.5. Occurrence Reference

An **Occurrence Reference** identifies the exact semantic application to which occurrence-specific Established Material
belongs.

This relation is required when later meaning depends on which application established the source result.

Occurrence identity remains distinct from Definition identity and Version Binding.

When an occurrence references a Versioned Definition, its exact Definition Reference already fixes that Definition
Version.

A universal runtime occurrence object is not required.

---

## 6.6. Identity Boundaries

Contract identity and compiler representation identity are different coordinate families.

```text
Contract semantic coordinates

Definition Meaning
    → established semantic content

Owning Authority Binding
    → exact owning authority

Version Binding
    → exact authority-scoped Version

Authority-Local Definition Coordinate
    → exact local Definition coordinate when required

Definition Reference
    → exact authoritative Definition

Occurrence Reference
    → exact semantic application
```

```text
Adjacent or realization coordinates

Source Provenance
    → authored source location / origin

Fingerprint
    → compiler comparison evidence

Compiler Generation
    → compiler publication generation

Compiler Schema Version
    → serialized compiler representation version

Local Address / Handle
    → current physical lookup coordinate
```

The second group must not become the first group merely because an implementation uses it for lookup.

A provenance-only change may preserve Definition Meaning and Definition Reference.

A schema migration, compiler generation change, or local relocation may preserve the same Version Binding and exact
Definition Reference.

---

## 6.7. Deterministic Reference Resolution

The same Definition Reference in the same semantic world must resolve to the same source-owned Definition Meaning,
Version
Binding, and Owning Authority Binding.

Physical ordering, allocation, scheduling, cache state, compiler generation, and publication layout cannot change that
result.

A new compiler generation or representation schema may use a different physical representation while preserving the same
semantic reference.

---

## 6.8. Identity Across Linking

Linking preserves the exact Definition Reference of unchanged source-owned meaning.

For Versioned Definitions, linking also preserves the exact Version Binding already contained in that reference.

Physical relocation, aggregation into a larger compiler world, or artifact regeneration does not mint a new Contract
Version or authoritative Definition.

A higher-scope authority may establish a new higher-scope result without rewriting the Authority, Version, or Definition
identity of unchanged source definitions it consumes.

---

# 7. Applicability

## 7.1. Applicable Context

Established Material is authoritative under its source meaning.

That does not make it valid Basis for every later semantic use.

**Applicable Context** is the exact Contract context that the owning applicability law actually uses to decide one
dependent application.

```text
Applicable Context

Version
    → V4

State
    → Ready
```

Another applicability law may use different coordinates.

```text
Applicable Context

Governance Binding
    → G17

Policy World
    → Emergency
```

A law that needs no additional context has no additional context requirement.

```text
Applicable Context
    → none
```

`none` means that the owning law requires no additional context.

It does not mean that one universal empty context object exists.

The context must not contain coordinates merely because they are globally available.

A universal context such as the following is not Contract law.

```text
currentVersion
currentPolicy
currentState
currentGovernance
currentEverything
```

Only meaning-determining coordinates belong to the Applicability judgment.

Source provenance, compiler generation, cache state, storage identity, worker state, and query state are not Applicable
Context.

When Version, Policy, Governance, State, or another Contract coordinate is already Established Material, Applicability
may preserve an exact reference to that material rather than copy its complete meaning.

### Why This Choice

A sparse context states exactly which Contract coordinates can change the judgment.

A universal context makes unrelated changes appear semantically relevant and obscures the actual Contract dependency.

---

## 7.2. Applicability Judgment

**Applicability** decides whether one exact Basis Binding may participate in one exact dependent semantic application.

```text
Applicability Judgment

Owning Applicability Law
    ↓

Basis Binding
    Required Basis R
        → Exact Established Material M

Dependent Application
    → U

Applicable Context
    → only meaning-determining coordinates

Result
    → Applicable
    or
    → Inapplicable
```

The Basis Binding is one input.

Applicability does not independently accept another Required Basis or another source material that could disagree with
that binding.

The dependent application must identify the semantic use being judged.

Examples include:

```text
Definition-time application
    → exact Definition judgment

Occurrence-time application
    → exact Contract Occurrence

Higher-scope application
    → exact composition judgment
```

A runtime call, compiler query, source container, parent object, or physical scope does not define the dependent
application by itself.

The owning applicability law must be identifiable exactly.

If the already-preserved authority, Definition, Required Basis, and dependent application identify one law uniquely, no
additional applicability-law coordinate is required.

If more than one meaning-distinct applicability law remains possible, an exact law coordinate must remain recoverable.

```text
Invariant I / Version V3

inputFact
    → Applicability Law L1

stateBasis
    → Applicability Law L2
```

The compiler must not infer `L1` or `L2` from declaration order, function order, table position, or lookup path.

### Why This Choice

Applicability is a judgment over an exact semantic relation and an exact use.

Keeping the law, binding, use, and relevant context explicit prevents compiler lookup structure from becoming Contract
authority.

---

## 7.3. Applicability Result

An Applicability judgment has two semantic results.

```text
Applicability Judgment
    ↓
Applicable
or
Inapplicable
```

The result belongs to the exact dependent use.

```text
Established Material M

Use U1
    → Applicable

Use U2
    → Inapplicable
```

The source material does not acquire a permanent `applicable` flag.

`Applicable` permits the exact Basis Binding to participate as Applicable Basis. Section 8.3 owns the Applicable Basis
relation.

This ADR does not create one permanent Established Applicability object for every check.

---

## 7.4. Deterministic Applicability

Applicability depends only on the semantic inputs owned by the applicable law.

```text
same Owning Applicability Law
    +
same Basis Binding
    +
same Dependent Application
    +
same meaning-determining Applicable Context
    ↓
same Applicability Result
```

A change to an irrelevant coordinate must not change the result.

The following cannot change Applicability meaning:

```text
compiler generation
cache hit or miss
storage address
table position
worker or thread
query order
analysis traversal order
source formatting
provenance-only movement
```

A change to a Contract coordinate used by the owning applicability law may require a new judgment for a later semantic
application.

The compiler may use these exact semantic inputs for reuse or invalidation.

Reuse machinery does not become Applicability law.

### Why This Choice

The Contract exposes the complete semantic determinant set without fixing a cache key, fingerprint, or incremental
algorithm.

This permits precise reuse while keeping compiler mechanisms replaceable.

---

## 7.5. Candidate Set, Singularity, and Arbitration

Applicability judges whether each candidate relation is valid for the dependent use.

It does not choose one candidate merely because several are applicable.

```text
Candidate Basis Bindings

B1
    → Applicable

B2
    → Applicable
```

This result does not authorize Applicability to select `B1` or `B2`.

```text
B1 + B2 are applicable
    ↓
Required Basis completeness
or
Singularity law
or
explicit Arbitration law
```

Selection belongs to the authority that owns that decision.

Candidate discovery order, ranking heuristic, hash order, declaration order, and first match are not implicit
arbitration.

Rejected candidates are not automatically part of the later Established Occurrence.

They are preserved as Contract attribution only when the owning law makes the candidate set itself determining semantic
Basis.

For example:

```text
Owning Arbitration Judgment

Determining Candidates
    → C1
    → C2
    → C3

Arbitration Law
    ↓
Resolved Selection
```

The complete candidate set is semantic Basis only when the owning Contract law declares that set determining.

A compiler solver trace is still not Contract meaning.

### Why This Choice

Applicability answers legality, not preference.

Combining legality and selection would let compiler candidate machinery silently acquire Contract authority.

---

## 7.6. Applicability Attribution in Established Occurrence Material

When an Applicability judgment contributes to an Established Occurrence, the occurrence must preserve the exact semantic
attribution that made the successful use meaningful.

```text
Established Occurrence O

Occurrence Reference
    → O

Definition Reference
    → D

Applicable Basis

    Required Basis R1
        → Basis Binding B1
        → Established Material M1

    Required Basis R2
        → Basis Binding B2
        → Established Material M2

Applicable Context

    Context Coordinate C1
        → exact Established Material C1'

    Context Coordinate C2
        → exact Established Material C2'

Applicability Law
    → exact law coordinate
      only when not otherwise uniquely determined

Established Result
    → occurrence-owned meaning
```

The occurrence preserves **direct determining relations**.

It does not copy the entire transitive semantic world.

If `M1`, `C1'`, or another determinant is already Established Material with an exact reference, that exact reference is
sufficient unless the owning occurrence law requires additional local meaning.

```text
Occurrence O
    → Governance Binding G17
        → Governance owns its own complete meaning
```

`O` does not need to duplicate every field already established by `G17`.

The occurrence must not preserve a later lookup rule such as:

```text
use current Governance Binding
use latest State
use current Version
```

It preserves the exact material that determined that occurrence.

The Applicable Basis relation already records the successful Applicability result.

The occurrence must not depend on a second independently mutable `applicable = true` value.

Candidate search order, rejected alternatives, proof trace, query dependency, cache entry, compiler generation, source
line, and full provenance chain are not required attribution.

They may belong to diagnostics or compiler analysis.

If the owning Contract law makes a complete candidate set, exact source position, or another coordinate part of the
result meaning, that coordinate becomes Contract attribution for that law.

### Example

```text
Invariant Occurrence O17

Definition
    → PositiveBalance / V3

Required Basis

target
    → Fact

Applicable Basis

target
    → Basis Binding B42
        → BalanceFact F42

Applicable Context

Policy World
    → Normal

State
    → Active

Result
    → invariant satisfied
```

If later context changes:

```text
Policy World
    Normal → Emergency

State
    Active → Closed
```

`O17` is not rewritten.

A later semantic application may establish another occurrence under the new context.

```text
Invariant Occurrence O18

Policy World
    → Emergency

State
    → Closed
```

### Why This Choice

The established occurrence remains interpretable without reconstructing historical context from mutable world state.

Direct exact references avoid duplicating already-established meaning while preserving the complete semantic basis of
the
occurrence.

---

## 7.7. Applicability After Change

New semantic context may require a new Applicability judgment for a later use.

```text
earlier Occurrence O17
    → attribution fixed

later context change
    → may affect later application
    → must not rewrite O17
```

Section 9.3 owns the common occurrence-time integrity law.

---

## 7.8. Version and Other Established Context

A Definition Reference already fixes the Version of the Definition it denotes.

```text
Definition Reference
    → Invariant / V3

Applicable Context
    → State Ready
```

`V3` is not repeated merely because the Definition is versioned.

If an Applicability law independently consumes another Versioned or otherwise Established relation, that exact relation
belongs to Applicable Context.

```text
Definition Reference
    → RefundInvariant / V4

Applicable Context

Source Definition
    → PaymentFact / V2

Other Established Binding
    → B17
```

Only independently meaning-determining relations are included.

---

# 8. Basis and Composition

## 8.1. Required Basis

A **Required Basis** is semantic material that a Contract judgment requires before that judgment can establish its
result.

The requirement belongs to the judgment that needs the material.

```text
Definition judgment
    └── Definition-time Required Basis

Occurrence judgment
    └── Occurrence-time Required Basis

Higher-scope composition judgment
    └── Higher-scope Required Basis
```

A Required Basis states **what meaning is required**.

It does not state which authority, definition, compiler product, or runtime producer must supply that meaning.

```text
Required Basis

leftOperand
    → Fact

rightOperand
    → Fact
```

`leftOperand` and `rightOperand` are distinct requirements even though both require `Fact`.

If the owning law gives two requirements different meaning, the compiler must be able to distinguish them without using
declaration order, array position, storage order, or discovery order.

A Definition owns a Required Basis only when that Basis is required for the Definition judgment itself.

A Basis required only for one occurrence or one higher-scope judgment does not become Definition meaning.

### Why This Choice

The consuming Contract declares its need without naming its producer.

This keeps source authority independent from composition topology.

The trade-off is explicit requirement bookkeeping. That cost is required to avoid hidden producer and ordering
semantics.

---

## 8.2. Basis Resolution and Basis Binding

**Basis Resolution** determines which exact Established Material satisfies one exact Required Basis under the applicable
composition law.

The result is a **Basis Binding**.

```text
Required Basis

leftOperand
    → Fact

rightOperand
    → Fact


Basis Binding

leftOperand
    → Established Fact A

rightOperand
    → Established Fact B
```

Each Basis Binding must preserve both sides exactly:

```text
Required Basis
    → exact requirement being satisfied

Established Material
    → exact source material supplied for that requirement
```

The consuming Contract does not choose the producer.

The composition law that owns the semantic connection determines the binding.

A compiler lookup result, call edge, query dependency, storage relation, or discovery path does not establish a Basis
Binding.

If the applicable composition law cannot determine the required exact source, that Required Basis remains unresolved.

A Basis Binding is a semantic relation. Its physical representation is not defined here.

### Why This Choice

Requirement and source connection are different meanings.

Keeping them separate allows the same Contract requirement to participate in different valid compositions without
rewriting the requirement itself.

The compiler must retain an explicit exact binding instead of recovering it later from implementation topology.

---

## 8.3. Applicable Basis

A Basis Binding does not by itself satisfy a Required Basis.

The bound Established Material must also be applicable to the semantic judgment being performed.

```text
Required Basis
    ↓
Basis Binding
    ↓
Applicability
    ↓
Applicable Basis
```

An **Applicable Basis** is a Basis Binding whose bound Established Material is permitted by the applicable Contract law
to
satisfy that exact Required Basis for that exact semantic use.

```text
Basis Binding

leftOperand
    → Established Fact A
        → applicable

rightOperand
    → Established Fact B
        → applicable
```

The following are not enough:

```text
same Contract kind
material exists
material is published
material is reachable
Basis Binding exists
compiler lookup succeeded
```

Applicability remains a separate Contract judgment.

```text
exists
    ≠
bound
    ≠
applicable
```

A bound but inapplicable material does not satisfy the requirement.

### Why This Choice

Source connection and permission to use that source may change for different reasons.

Separating them prevents Version, Policy, State, or other applicability context from being hidden inside producer
topology.

---

## 8.4. Complete Basis

A judgment has **Complete Basis** only when every Required Basis owned by that judgment is satisfied according to the
completeness law of the owning Contract.

```text
Required Basis

leftOperand
    → applicable Established Fact A

rightOperand
    → unresolved

Result
    → Basis incomplete
    → judgment cannot establish
```

One satisfied requirement does not grant partial authority for another unsatisfied requirement.

The owning Contract defines the required shape and cardinality.

Examples include:

```text
exactly one

zero or one

one or more

exact finite set

A and B

A or B
```

These are examples of possible owning laws. This ADR does not define one universal Basis cardinality language.

For an `exactly one` requirement:

```text
zero applicable bindings
    → incomplete

one applicable binding
    → satisfied

more than one applicable binding
    → invalid when singularity is required
```

A different alternative is valid only when the owning Contract explicitly defines that alternative.

Missing Basis does not create another result.

### Why This Choice

Completeness belongs to the Contract that owns the judgment.

A universal compiler rule would either reject valid Contract shapes or silently accept incomplete authority.

---

## 8.5. Basis States

The following states are distinct.

```text
Not Owned

The judgment defines no such Required Basis.


Permitted Absence

The owning law permits absence for that requirement.


Required, Unresolved

The judgment requires the Basis.
No exact Basis Binding has been established.


Bound, Inapplicable

An exact Basis Binding exists.
The bound material cannot satisfy this semantic use.


Bound, Applicable

An exact Basis Binding exists.
The bound material may satisfy this semantic use.


Complete

Every requirement required by the owning law is satisfied.
```

`Not Owned`, `Permitted Absence`, and `Required, Unresolved` must not collapse into one semantic absence.

A required unresolved Basis prevents the owning judgment from establishing.

A representation may encode these states in any form. It must preserve their semantic distinction.

### Why This Choice

A single missing value cannot tell whether material was unnecessary, legally absent, or required but unresolved.

That distinction is necessary for correct establishment, diagnostics, and later reuse.

---

## 8.6. Basis and Definition Identity

A Required Basis belongs to the meaning of the judgment that declares it.

A resolved source does not automatically become part of the identity of the consuming Definition.

```text
Definition D
    Required Basis R


World A

R
    → Established Fact F1


World B

R
    → Established Fact F2
```

If the owning Contract defines `D` as requiring any material that satisfies `R`, then the two worlds may preserve the
same
Definition Meaning and Authority-Owned Definition Identity while holding different Basis Bindings.

```text
Definition Meaning
    → same

Authority-Owned Definition Identity
    → same

Basis Binding
    → different
```

If the owning Contract instead defines `D` specifically against exact Definition `F1`, that exact reference is part of
the Definition Meaning.

Changing `F1` to `F2` then changes the meaning according to that owning law.

```text
Exact source reference
    ∈ Definition Meaning
        ↓
identity-significant under the owning law
```

The compiler must not decide identity significance merely because one reference is convenient to hash, store, or cache.

### Why This Choice

Always placing resolved bindings inside Definition Identity would make composition changes rewrite otherwise unchanged
definitions.

Always excluding them would lose Contracts whose meaning explicitly names an exact source.

The owning Contract therefore decides whether the exact source relation is Definition meaning.

---

## 8.7. Semantic Prerequisite Order

Basis creates semantic prerequisite order.

```text
Required Basis
    ↓
Basis Binding
    ↓
Applicability
    ↓
Complete Basis
    ↓
Owning Judgment
    ↓
Establishment
```

The diagram states prerequisites, not compiler pass order.

A judgment that requires Basis must not establish before its required Basis is complete.

A Basis Binding may bind only to material whose owning Establishment has already succeeded.

Later Establishment may consume earlier Established Material.

No parser order, query order, worker order, declaration order, table order, or cache order gains Contract meaning from
this prerequisite relation.

### Cycles

An unresolved semantic prerequisite cycle cannot establish.

```text
A requires B
B requires C
C requires A

No member has Complete Basis.
    ↓
No member may establish from that cycle.
```

The Contract rule is the prohibition of circular semantic establishment.

The compiler algorithm used to detect the cycle is realization.

### Why This Choice

A partial order states only the prerequisites required by Contract meaning.

It leaves independent work free to be scheduled, parallelized, reused, or incrementally repaired by later compiler
implementations.

---

## 8.8. Composition Authority

Connecting several Established Materials does not establish a larger meaning by itself.

A Contract law must own the new meaning.

```text
Established Material A
    +
Established Material B
    ↓
owning composition law
    ↓
Established Material C
```

`C` belongs to the composing authority.

`A` and `B` keep their original authorities and Definition References.

Basis Bindings used by the composition do not transfer source authority to the consumer.

---

## 8.9. Whole-Machine Composition

Whole-Machine composition may establish Basis Bindings across unit boundaries.

```text
Core A material
    ↓
Whole-Machine Basis Binding
    ↓
Core B requirement
```

Neither local Contract needs to name the other as its producer.

The Whole-Machine law owns the cross-unit connection.

If that law establishes new Whole-Machine meaning from the connected material, that result belongs to the
Whole-Machine authority.

Physical linking does not establish the connection or the larger meaning.

---

## 8.10. Compiler-Derived Dependency

The compiler may derive computation dependencies from established Basis Bindings.

```text
Contract Basis Binding
    ↓
compiler-derived dependency
```

The direction does not reverse.

A query edge, analysis dependency, build edge, or cached dependency cannot establish a Contract Basis Binding.

The compiler may discard and rebuild its derived dependency representation without changing the established semantic
connection.

---

## 8.11. Shared Consumption

The same Established Material may satisfy several independently owned requirements when each use is valid.

```text
Established Fact A
    ├── Basis Binding → Judgment X
    └── Basis Binding → Judgment Y
```

Each Basis Binding remains exact to its own Required Basis.

Shared consumption does not transfer or merge source authority.

One consumer's use does not create applicability for another consumer.

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

Established occurrence meaning is fixed at Establishment.

```text
Occurrence O
    → exact attribution at Establishment

later semantic material
    → may support a later judgment
    ↛ rewrite O
```

A later consumer must use the exact attribution preserved by Section 7.6.

It must not reinterpret an earlier occurrence through `current`, `latest`, nearest, or newly reachable material.

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

For every Established Definition exposed by the Canonical Contract World:

```text
Established Definition D

Definition Reference
    → exact identity model from Section 6

Definition Meaning
    → exact established meaning

Direct Established Relations
    → only relations already established as D's meaning

Source Provenance
    → separate exact relation when retained
```

The world must distinguish:

```text
relation not owned
    ≠
owned and validly absent
    ≠
required but unresolved
```

It must not manufacture a missing semantic relation for downstream convenience.

Occurrence-specific Applicable Basis, Applicable Context, and occurrence attribution remain outside Definition-world
material unless an owning Contract explicitly establishes such a relation.

This exact binding surface defines semantic recoverability.

It does not require one field, row, object, table, or reference edge per relation.

```text
exact semantic relation
    → must remain recoverable

physical path used to recover it
    → replaceable
```

---

## 10.5. World Boundary

The Canonical Contract World is an authoritative semantic substrate, not a universal compiler IR.

```text
Canonical Contract World
    → exposes Established Definition meaning

compiler analysis / verification / optimization
    → consume that meaning

query / cache / storage / generated artifact
    → realize or consume compiler work

none of the latter
    → create Contract authority
```

Representation, indexing, publication, reuse, and incremental mechanisms remain compiler realization.

---

# 11. Contract and Representation Boundary

## 11.1. Semantic Separation

Compiler realization must preserve every semantic coordinate and exact relation defined by this ADR.

```text
Contract semantic material

Established Material meaning
Definition / Occurrence distinction
Judgment / Establishment distinction
Source Authority
Definition Meaning
Authority-Owned Definition Identity
Owning Authority Binding
Version Binding
Authority-Local Definition Coordinate
    when required by the owning identity law
Definition Reference
Occurrence Reference
Required Basis
Basis Binding
Applicable Basis
Applicability Judgment / Result
Applicable Context
Complete Basis
Composition relation
Semantic prerequisite order
Occurrence attribution
```

These distinctions are logical.

They do not prescribe physical topology.

```text
semantic distinction
    ≠ separate object
    ≠ separate allocation
    ≠ separate table
    ≠ separate compiler product
    ≠ pointer or reference hop
```

The realization must preserve meaning.

It does not have to preserve the semantic diagram as an object graph.

---

## 11.2. Logical Separation Does Not Require Physical Indirection

A semantic reference denotes an exact semantic coordinate or relation.

It does not require a host-language object reference.

```text
Definition Reference
    → exact Definition

Basis Binding
    → exact Required Basis / source relation

Occurrence Reference
    → exact semantic application
```

The Contract does not require:

```text
DefinitionObject
    → VersionObject
        → BasisBindingObject
            → ApplicabilityObject
                → OccurrenceObject
```

A realization may instead use packed identity, dense handles, flat relations, primitive slabs, direct offsets,
summaries,
or another representation that preserves the same exact semantics.

```text
semantic reference
    → may realize as dense handle

semantic relation
    → may realize as flat row / slab range

several semantic coordinates
    → may realize in one physical record
```

No encoding choice gains Contract authority.

---

## 11.3. Material, Product, and Storage Boundaries

The following boundaries are independent decisions.

```text
Semantic Coordinate
    ↓ may or may not require
Established Material
    ↓ may or may not require
Compiler Product
    ↓ may or may not require
Physical Storage Unit
```

A semantic distinction alone does not justify promotion to the next boundary.

Separate Established Material exists only when an owning Contract law establishes complete meaning of its own.

Separate compiler material or product is a realization decision.

A compiler may split a product when independent consumption, invalidation, reuse, lifetime, publication, or
materialization makes that split useful.

A compiler may fuse products when consumers repeatedly need the same semantic material together and no independent
boundary requires separate materialization.

```text
semantic split
    → preserved

physical split / fuse
    → replaceable
```

The Contract does not decide product granularity for the compiler.

---

## 11.4. Performance-Sensitive Projection

A hot consumer must not be forced to repeatedly reconstruct semantic relations that the compiler has already resolved.

```text
Canonical semantic relations

Definition
Version
Basis
Applicability
Composition

        ↓
consumer-specific projection

        ↓
hot consumer
```

A realization may:

```text
pre-resolve
flatten
fuse
co-locate
denormalize derived values
build summaries
replace semantic reference chains with dense lookup
```

when those changes preserve the canonical semantic source and exact Contract meaning.

This freedom applies to compiler analysis, verification, whole-machine processing, execution formation, and backend
realization.

A projection is derived realization material.

```text
canonical semantic source
    → authority

derived projection
    → performance / consumer representation
    ↛ authority
```

A derived copy may duplicate a semantic value for locality.

That duplication does not create a second semantic owner.

---

## 11.5. Temperature Is Not Contract Meaning

Hot, warm, and cold are compiler realization properties.

They are not Contract coordinates.

A backend or compiler generation may classify the same semantic material differently.

```text
Hot
    → may favor fusion
    → may favor co-location
    → may favor pre-resolution

Warm
    → may favor indexed flat relations
    → may favor summaries

Cold
    → may favor indirection
    → may favor separate provenance or diagnostic storage
```

These are realization options, not mandatory layouts.

The Contract does not permanently classify Definition, Version, Basis, Applicability, Occurrence, provenance, or any
other coordinate as hot or cold.

Physical temperature follows actual consumer behavior and cost.

---

## 11.6. Representation Authority Boundary

Compiler realization material remains distinct from Contract meaning.

```text
Compiler realization material

source container
host object identity
local handle
table position
query state
cache state
fingerprint
compiler generation
compiler schema version
worker schedule
backend artifact layout
consumer-specific summary
derived projection
```

The realization group may encode, index, compare, cache, duplicate, transport, split, or fuse Contract material.

It may not establish, revoke, merge, or rewrite Contract authority.

```text
Contract Version
    ≠ Compiler Schema Version
    ≠ Compiler Generation
    ≠ Cache Generation
    ≠ Backend Artifact Format Version
```

A representation may change while Contract meaning remains unchanged.

A semantic change remains a Contract change even when the compiler can migrate the physical representation.

A backend that cannot preserve an established law is not a valid realization of that law.

---

# 12. Relation to Existing Authorities

This ADR defines only the common Establishment relations.

```text
1D Authority
    → owns its local Established Material

ADR-0063
    → defines common Establishment / identity / Basis / Applicability / composition relations

Another Authority
    → may consume source-owned material
      only through an explicit applicable relation
```

Each 1D ADR defines its own material meaning, local judgment, and any authority-specific binding.

ADR-0063 does not duplicate or replace those local laws.

---

# 13. Governance and Diagnostic Consequences

## 13.1. Governance

Governance keeps ownership of its own semantics.

ADR-0063 does not define Governance selection, scope, validity, singularity, or local Binding shape.

When Governance consumes another authority's Established Material, the common rules remain:

```text
source Established Material
    → exact Basis Binding
    → Applicability
    → Governance-owned judgment
    → Governance-owned result
```

Source authority does not transfer to Governance.

---

## 13.2. Diagnostic

Diagnostic may explain authoritative meaning without recreating it.

```text
definition explanation
    → Definition Reference

occurrence explanation
    → Occurrence Reference
    → exact occurrence attribution when required
```

Diagnostic evidence, provenance, and compiler-derived evidence remain distinct from the source authority they explain.

Retention controls whether retained evidence remains available; it does not recreate Establishment.

---

# 14. Whole-Machine Consequences

Section 8.9 owns the common Whole-Machine composition rule.

```text
unit source material
    → keeps source identity and authority

Whole-Machine connection
    → owned by Whole-Machine composition law

new Whole-Machine meaning
    → requires Whole-Machine Establishment
```

Physical linking establishes none of these relations by itself.

---

# 15. Semantic Validity Requirements

The compiler must reject semantic states that cannot satisfy the exact Establishment contract.

```text
Invalid Establishment

Establishment Input
    → semantic subject unresolved
    → required semantic reference unresolved
    → parser recovery or realization topology used to complete missing meaning

Owning Authority
    → missing or non-exact

Definition Reference
    → cannot resolve exact authoritative meaning

Version Binding
    → required but unresolved
    → ambiguous claim
    → implicit current / latest fallback

Authority / Version / local Definition coordinate
    → same exact coordinate
    → conflicting Definition Meaning

Required Basis
    → required but unresolved
    → bound but inapplicable
    → incomplete under owning completeness law

Applicability
    → Basis Binding not exact
    → dependent application not exact
    → meaning-determining context not recoverable
    → owning law not identifiable when another coordinate is required

Applicable candidates
    → more than owning singularity law permits
    → no owning Arbitration law resolves them

Semantic prerequisite
    → unresolved cycle

Composition
    → source relation not exact
    → new meaning has no owning composition law

Established Occurrence
    → determining attribution not recoverable
    → historical attribution replaced by current / latest material
```

Compiler-only identity, lookup, ordering, reachability, cache state, or physical representation cannot repair any
invalid
state above.

Where an owning law permits absence or another cardinality, that law decides validity. The compiler must not substitute
a
universal default.

---

# 16. Compiler Realization Obligation

Compiler realization must provide exact access to every semantic distinction required by this ADR.

```text
semantic relation
    → exactly recoverable

physical encoding
    → replaceable
```

Exact recoverability does not require a fixed traversal path.

```text
semantic chain

A
    → B
    → C
    → D

may realize as

pre-resolved projection P
```

A compiler may pack, split, fuse, flatten, summarize, or duplicate derived projections when Contract meaning remains
unchanged.

A consumer must not need to reconstruct Contract meaning from implementation topology.

A hot consumer should not be forced to repeat semantic resolution that an earlier valid compiler stage has already made
available in a verified projection.

Reuse and incremental machinery may avoid compiler work.

Performance projections may avoid reference chasing.

Neither creates Contract authority.

The realization must preserve the ability to change physical split / fuse decisions without changing Contract semantics.

Appendix A gives non-normative V1 implementation examples for these semantic relations. Those examples are
implementation
reference only and do not constrain later representation.

---

# 17. Evolution Boundary

Future compiler generations may replace identity encoding, storage, publication, dependency, reuse, incremental,
serialization, product granularity, or split / fuse decisions.

Compatibility requires preservation of the same Contract-visible meaning and exact semantic relations.

```text
Contract semantics
    → stable under equivalent semantic world

compiler realization
    → may evolve independently
```

A future compiler may:

```text
split one physical product into several
fuse several products into one
move a relation between hot and cold storage
replace a reference chain with a summary
replace a summary with another validated projection
```

without changing this ADR when the same Contract semantics remain exactly recoverable.

Contract Version remains distinct from representation and compiler generation under Section 11.

Compatibility or migration between Contract Versions does not make their Version Identities equal.

This boundary leaves V2 incremental architecture and later performance work open without redefining Establishment
semantics.

---

# 18. Rejected Directions

## 18.1. Universal Semantic Containers

Rejected:

```text
one universal EstablishedMaterial object
one universal Established-Material field set
one universal Reference object
one universal Applicable Context object
one universal lifetime model
```

Different authorities own different semantic coordinates.

A common compiler representation may exist. It does not become one universal Contract schema.

---

## 18.2. Representation as Contract Authority

Rejected:

```text
host-object identity
source nesting
parent object
symbol-table containment
generated host type
compiler-local handle
pointer / ordinal / intern id
table position
physical completion order

    ↛

Owning Authority
Definition Identity
Version Identity
semantic order
```

These coordinates belong to source organization or realization unless an owning Contract explicitly gives one semantic
meaning.

---

## 18.3. Implicit Semantic Reconstruction

Rejected:

```text
observation
    → authority without owned semantic qualification

source results
    → automatic larger composition

generated artifact / reflection / KSP / host shape
    → reconstructed Contract authority

Version Claim unresolved
    → current / latest / preferred / nearest / first available
```

Authority must come from the owning Contract law and exact established relations.

---

## 18.4. Derived Compiler Work as Contract Authority

Rejected:

```text
compiler state
query dependency
build dependency
cache state
verification result
optimization proof
specialized execution form

    ↛

Contract dependency
Contract Establishment
Contract authority
```

The compiler may derive its own knowledge from Contract relations.

The derivation cannot reverse the authority direction.

The Canonical Contract World also remains an authoritative substrate rather than a universal IR that absorbs analysis,
optimization, or backend state.

---

## 18.5. Version Conflation

Rejected:

```text
Contract Version
    = Compiler Schema Version

Contract Version
    = Compiler Generation

Contract Version
    = Backend Artifact Format Version

Version V3 compatible with V4
    → V3 = V4
```

Representation compatibility, migration, or another compatibility law does not create Version equality.

ADR-0053 remains the owner of Contract Version semantics.

---

## 18.6. Semantic Decomposition as Mandatory Physical Topology

Rejected:

```text
semantic concept A
semantic concept B
semantic concept C

therefore

AObject
    → BObject
        → CObject
```

Distinct semantics do not require distinct heap objects, tables, allocations, compiler products, or repeated reference
traversal.

Also rejected:

```text
first implementation layout
    → permanent semantic architecture
```

The compiler may physically split or fuse material as access patterns and product boundaries evolve.

No first representation becomes Contract law merely because it was convenient to implement.

---

# 19. Consequences

The model requires explicit semantic binding and attribution.

```text
Cost

exact Definition / Occurrence references
explicit Version Binding
explicit Required Basis / Basis Binding
explicit Applicable Context
explicit occurrence attribution

    → more semantic bookkeeping
```

The model does not require equivalent physical fragmentation.

```text
Semantic precision
    → preserved

Physical realization
    → may split
    → may fuse
    → may flatten
    → may pre-resolve
    → may build consumer-specific summaries
```

```text
Benefit

source authority remains exact
semantic identity survives representation change
applicability remains use-specific
historical occurrence meaning remains recoverable
hot consumers can avoid unnecessary reference chasing
physical product granularity can evolve
diagnostics can reference exact semantic sources
reuse and incremental invalidation can be fine-grained

    → without compiler mechanism becoming Contract authority
```

The compiler therefore pays the cost of preserving exact semantic relations while retaining freedom to choose a
performance-oriented physical layout.

The physical representation remains replaceable as long as these semantic laws are preserved.

---

# 20. Amendment History

Amendment history is non-normative. The body of this ADR is the current authority.

```text
2026-09-02
    → clarified Contract detail vs replaceable realization

2026-09-09
    → aligned Resolved Contract HIR / Establishment / Canonical Contract World boundary

2026-09-10
    → aligned Contract-aware compiler consumption
    → closed common Established Material contract
    → separated Definition Meaning / identity / Owning Authority Binding

2026-09-11
    → made Required Basis / Basis Binding / Complete Basis explicit
    → made Applicability / Applicable Context / Occurrence attribution explicit
    → made Authority-scoped Version Binding explicit
    → removed semantic duplication and assigned one canonical owner section per common law
    → separated semantic decomposition from compiler product and physical split / fuse decisions
```

---

# Appendix A. Non-Normative Kontrakt V1 Implementation Reference

## A.1. Status

**This appendix is implementation reference only. It is not Contract law.**

Sections 1–20 define normative Contract meaning. Appendix A gives one plausible Kontrakt V1 realization so
implementation can start from concrete material shapes without turning those shapes into authority.

```text
Normative Contract
    → Sections 1–20

V1 implementation reference
    → Appendix A
    → non-normative
    → replaceable
```

Nothing here fixes Kotlin class shape, JVM object topology, table count, slab count, IR operation names, HID encoding,
dense-handle width, query API, serialization schema, or physical split / fuse choice.

---

## A.2. Reading Form

Each important internal material answers the same questions.

```text
Material
    → what Kontrakt calls or observes

Produced by
    → compiler boundary that creates it

Consumes
    → exact upstream material

Exposes
    → information downstream may read

Example dump
    → human-readable internal view

Possible V1 backing
    → concrete primitive / slab candidate

Consumed by
    → downstream compiler products
```

`Possible V1 backing` is concrete by design. It is still replaceable.

---

## A.3. End-to-End Material Map

```text
Contract Source
    ↓
Source / Syntax / Provenance
    ↓
Resolved Contract HIR
    ↓
Authority-Owned Establishment
    ↓
Canonical Contract World
    ├── Generated API Product
    ├── Reference Judgment
    ├── PBT / Fixture / Coverage
    ├── Diagnostics / Evidence
    └── Contract-Aware consumers

User JVM Classfiles
    ↓
Realization Acquisition
    ↓
Realization Body IR
    ↓
Admitted Realization Binding
    ↓
Contract-Aware Analysis
    ↓
Verification Overlay
    ↓
Whole-Machine / Specialization Knowledge
    ↓
Execution Formation
    ↓
Contract-Aware Execution IR
    ↓
Analysis / Transform
    ↓
Execution IR New Generation
    ↓
JVM Plan / IR
    ↓
Classfile Product
```

```text
Resolved Contract HIR
    → IR

Canonical Contract World
    → authoritative semantic substrate
    → not ordinary optimization IR

Realization Body IR
    → IR

Contract-Aware Analysis
Verification Overlay
Whole-Machine Summary
    → derived compiler material

Contract-Aware Execution IR
    → IR

JVM Plan / IR
    → target IR
```

---

## A.4. Running Example

```text
Invariant Definition
    → PositiveBalance

Version
    → V3

Required Basis
    target
        → Balance Fact
        → exactly one

Applicability uses
    → State

Occurrence Context
    State
        → Active

Occurrence Result
    → Satisfied
```

The appendix begins after authored syntax has been parsed. Exact `.kontrakt` grammar is outside this example.

---

## A.5. Source, Provenance, and Resolved Contract HIR

### Material

```text
Source / Provenance Material
Resolved Contract HIR
```

### Produced by

```text
.kontrakt
    ↓
Source Manager / Parser / Recovery
    ↓
Resolution
```

### Exposes

```text
exact source provenance
resolved authority references
resolved semantic names
resolved slot / relation references
poison / recovery state when required
```

### Example dump

```text
hir.definition %d17

kind
    → Invariant

authority
    → invariant:PositiveBalance

versionClaim
    → V3

localDefinition
    → PositiveBalance

requiredBasis
    target
        → Fact<Balance>
        → exactly one

applicabilityInputs
    → State

provenance
    → src#91
```

### Possible V1 backing

```text
ResolvedDefinitionHIR

kind[]
authorityRef[]
versionClaimRef[]
localDefinitionRef[]

requiredBasisBase[]
requiredBasisCount[]

applicabilityInputBase[]
applicabilityInputCount[]

provenanceHandle[]
poisonTag[]
```

Example:

```text
ResolvedDefinitionHIR[17]

kind                     = INVARIANT
authorityRef             = 8
versionClaimRef          = 3
localDefinitionRef       = 12
requiredBasisBase        = 40
requiredBasisCount       = 1
applicabilityInputBase   = 70
applicabilityInputCount  = 1
provenanceHandle         = 91
poisonTag                = CLEAN
```

### Consumed by

```text
Authority-Owned Establishment
compiler diagnostics
source-aware tooling
```

Resolved HIR is not authoritative.

---

## A.6. Establishment Input and Established Definition Output

### Material

```text
Establishment Input
Established Definition Material
```

### Produced by

```text
Resolved Contract HIR
    ↓
Basis / applicability prerequisites as required
    ↓
Owning Contract Law
    ↓
Establishment
```

### Example input

```text
establishment.input

candidate
    → hir.definition %d17

owningAuthority
    → invariant:PositiveBalance

resolvedVersion
    → V3

requiredBasisDeclaration
    target
        → Fact<Balance>
        → exactly one

provenance
    → src#91
```

### Example output

```text
established.definition @def:42

definitionReference
    → invariant:PositiveBalance / V3

owningAuthority
    → invariant:PositiveBalance

versionBinding
    → V3

authorityLocalDefinition
    → PositiveBalance

definitionMeaning
    → positive-balance-invariant#5

requiredBasis
    target
        → requiredBasis#80

directRelations
    → relationRange[120..122)

provenance
    → src#91
```

### Possible V1 backing

```text
DefinitionTable

authorityHandle[]
versionHandle[]
localDefinitionCoordinate[]
meaningKind[]
meaningPayloadHandle[]
requiredBasisBase[]
requiredBasisCount[]
relationBase[]
relationCount[]
provenanceHandle[]
```

Example:

```text
DefinitionTable[42]

authorityHandle           = 8
versionHandle             = 3
localDefinitionCoordinate = 12
meaningKind               = INVARIANT
meaningPayloadHandle      = 5
requiredBasisBase         = 80
requiredBasisCount        = 1
relationBase              = 120
relationCount             = 2
provenanceHandle          = 91
```

### Consumed by

```text
Canonical Contract World publication
Reference Judgment
PBT planning
diagnostics
Contract-Aware Analysis
Execution Formation
```

No universal `EstablishedMaterial` row is required.

---

## A.7. Canonical Contract World, Stable Keys, and Dense Handles

### Material

```text
Canonical Contract World Generation G12
```

### Produced by

```text
Established Definition Material
    ↓
semantic verification
    ↓
freeze / seal
    ↓
publication
```

### Example world view

```text
CanonicalContractWorld G12

authorities
    → AuthorityTable

versions
    → VersionTable

definitions
    → DefinitionTable

requiredBasis
    → RequiredBasisSlab

relations
    → DefinitionRelationSlab

stableIdentity
    → StableKeyIndex

provenance
    → ProvenanceStore
```

Occurrence-specific material remains separate unless an owning Contract defines otherwise.

### Identity use

```text
Stable Definition Key
    → hid:7f4a...19c2

Generation-Local Definition Handle
    → 42
```

```text
hid:7f4a...19c2
    ↓ resolve once in G12
definitionHandle = 42
    ↓ repeated reads
DefinitionTable[42]
```

### Possible V1 backing

```text
StableKeyIndex
    stableKey[]
    denseHandle[]

DefinitionRelationSlab
    relationKind[]
    leftHandle[]
    rightKind[]
    rightHandle[]

ProvenanceStore
    sourceId[]
    spanStart[]
    spanEnd[]
    originHandle[]
```

### Consumed by

```text
all sibling Contract products
Contract-Aware Analysis
Execution Formation
Whole-Machine work
```

Stable keys and dense handles are lookup machinery, not authority.

---

## A.8. Required Basis and Basis Binding Material

### Contract view

```text
Required Basis

target
    → Fact<Balance>
    → exactly one
```

Later:

```text
Basis Binding

target
    → Balance Fact F42
```

### Possible V1 backing

```text
RequiredBasisSlab

ownerKind[]
ownerHandle[]
requirementCoordinate[]
requiredMeaningKind[]
requiredMeaningPayload[]
cardinalityKind[]
cardinalityPayload[]
```

```text
RequiredBasisSlab[80]

ownerKind              = DEFINITION
ownerHandle            = 42
requirementCoordinate  = TARGET
requiredMeaningKind    = FACT
requiredMeaningPayload = Balance
cardinalityKind        = EXACTLY_ONE
cardinalityPayload     = 0
```

Resolved connection:

```text
BasisBindingSlab

requiredBasisHandle[]
sourceKind[]
sourceHandle[]
dependentApplicationHandle[]
```

```text
BasisBindingSlab[100]

requiredBasisHandle        = 80
sourceKind                 = FACT
sourceHandle               = 205
dependentApplicationHandle = 17
```

### Consumed by

```text
Applicability evaluation
Complete Basis check
Occurrence establishment
diagnostics
Reference Judgment
Execution Formation when runtime-relevant
```

The two slabs may later be fused.

---

## A.9. Applicability, Applicable Context, and Complete Basis

### Produced by

```text
Basis Binding
+
Dependent Application
+
Applicable Context
+
Owning Applicability Law
    ↓
Applicability Judgment
```

### Example input

```text
applicability.input

law
    → invariant-target-applicability#4

basisBinding
    → basisBinding#100

dependentApplication
    → occurrenceCandidate#17

context
    State
        → Active
```

### Example transient result

```text
applicability.result

basisBinding
    → basisBinding#100

application
    → occurrenceCandidate#17

result
    → Applicable
```

### Possible V1 backing for sparse context

```text
ContextSlab

ownerApplicationHandle[]
coordinateKind[]
materialKind[]
materialHandle[]
```

```text
ContextSlab[55]

ownerApplicationHandle = 17
coordinateKind         = STATE
materialKind           = STATE
materialHandle         = 301
```

### Complete Basis

```text
Complete Basis for occurrenceCandidate#17

target
    → basisBinding#100
    → Applicable

Completeness Law
    → exactly one target

result
    → Complete
```

A permanent `CompleteBasis` object is not required. V1 may derive it from requirement ranges, applicable binding ranges,
and the owning completeness law.

### Consumed by

```text
Occurrence establishment
Reference Judgment
verification / diagnostics when relevant
Execution Formation when runtime-relevant
```

---

## A.10. Established Occurrence Material

### Produced by

```text
exact Definition
+
Complete Basis
+
Applicable Context
+
Owning Occurrence Judgment
    ↓
Occurrence Establishment
```

### Example dump

```text
established.occurrence @occ:17

occurrenceReference
    → PositiveBalance occurrence O17

definitionReference
    → invariant:PositiveBalance / V3

applicableBasis
    target
        → requiredBasis#80
        → basisBinding#100
        → Balance Fact F42

applicableContext
    State
        → Active

result
    → Satisfied
```

### Possible V1 backing

```text
OccurrenceTable

definitionHandle[]
basisBase[]
basisCount[]
contextBase[]
contextCount[]
resultKind[]
resultPayloadHandle[]
```

```text
OccurrenceTable[17]

definitionHandle    = 42
basisBase           = 100
basisCount          = 1
contextBase         = 55
contextCount        = 1
resultKind          = SATISFIED
resultPayloadHandle = 0
```

### Consumed by

```text
later Contract judgments when explicitly used as Basis
diagnostics
Reference comparison
execution products when occurrence meaning remains runtime-relevant
```

Version need not be duplicated when the Definition Reference already fixes it.

---

## A.11. Composition and Whole-Machine Established Material

### Produced by

```text
already-established source material
+
Owning Composition Law
    ↓
Composition Judgment
    ↓
new Established meaning when the law establishes one
```

### Example

```text
composition @comp:7

law
    → whole-machine-composition#7

inputs
    coreAResult
        → Established Material A17

    coreBResult
        → Established Material B3

result
    → Whole-Machine Material W9

owner
    → Whole-Machine Authority W
```

### Possible V1 backing

```text
CompositionTable

lawHandle[]
inputBase[]
inputCount[]
resultKind[]
resultHandle[]
ownerAuthorityHandle[]
```

```text
CompositionInputSlab

compositionHandle[]
roleCoordinate[]
sourceKind[]
sourceHandle[]
```

Example:

```text
CompositionTable[7]

lawHandle            = 44
inputBase             = 300
inputCount            = 2
resultKind            = WHOLE_MACHINE
resultHandle          = 9
ownerAuthorityHandle  = 61
```

### Consumed by

```text
Canonical Contract World when the result is Definition meaning
Whole-Machine linking
Whole-Machine verification
later semantic judgments
```

Source materials keep their original authorities.

---

## A.12. Generated Operation / Interaction API Product

### Produced by

```text
Canonical Contract World
    ↓
Generated API projection
```

### Example

```text
generated.api @order-v3

contractInterface
    → Order / V3

hostType
    → io.example.OrderInteraction

operations
    placeOrder
        → operationDefinition#72
        → descriptor#18
```

### Possible V1 backing

```text
GeneratedApiProduct

contractInterfaceHandle[]
hostTypeNameHandle[]
operationBase[]
operationCount[]
artifactHandle[]
```

```text
GeneratedApiOperationSlab

productHandle[]
operationDefinitionHandle[]
hostMethodNameHandle[]
descriptorHandle[]
```

### Consumed by

```text
host compiler
user implementation compilation
IDE / source generation tools
```

Generated API material is artifact output, not authority.

---

## A.13. Reference Judgment Product

### Produced by

```text
Canonical Contract World
    ↓
simple deterministic reference path
```

### Example

```text
reference.request @r11

subject
    → invariant:PositiveBalance / V3

basis
    target
        → Balance Fact F42

context
    State
        → Active
```

```text
reference.result @r11

judgment
    → Satisfied

determiningBasis
    → basisBinding#100

determiningContext
    → contextRange[55..56)
```

### Possible V1 backing

```text
ReferenceResult

requestKey[]
subjectKind[]
subjectHandle[]
resultTag[]
resultPayloadHandle[]
basisBase[]
basisCount[]
contextBase[]
contextCount[]
```

### Consumed by

```text
PBT oracle
generated-gate differential checking
backend differential checking
optimization validation
compiler regression
```

Reference Judgment is intentionally simpler than the optimized path.

---

## A.14. PBT, Fixture, and Contract Coverage Products

### Produced by

```text
Canonical Contract World
    ↓
Obligation Extraction
    ↓
Test Objective Planning
    ↓
Case Generation
    ↓
Reference Judgment
```

### Example objective

```text
test.objective @t21

contractObligation
    → invariant:PositiveBalance / V3

witnessKind
    → violating

requiredContext
    State
        → Active

generationDomain
    → balance-domain#4
```

### Possible V1 backing

```text
TestObjectiveSlab

obligationKind[]
obligationHandle[]
witnessKind[]
contextBase[]
contextCount[]
domainHandle[]
```

```text
GeneratedTestCase

objectiveHandle[]
seed[]
inputFixtureHandle[]
expectedReferenceResultHandle[]
```

```text
ContractCoverage

obligationHandle[]
coverageKind[]
coveredTag[]
witnessCount[]
```

### Consumed by

```text
generated unit-test product
PBT runner
fixture adapter
coverage reporting
```

Randomness controls generation, not Contract truth.

---

## A.15. Diagnostics and Diagnostic Evidence Products

Two products may coexist.

```text
Contract Diagnostic Evidence
Compiler Diagnostic Record
```

The Contract evidence meaning is owned by its Diagnostic ADR. Compiler diagnostics are realization products.

### Example compiler diagnostic

```text
compiler.diagnostic @D-KON-0174

code
    → KON-0174

semanticSubject
    → occurrence @occ:17

primaryProvenance
    → src#91

arguments
    → argRange[12..15)

relatedNotes
    → noteRange[7..9)
```

### Possible V1 backing

```text
CompilerDiagnosticRecord

codeHandle[]
semanticSubjectKind[]
semanticSubjectHandle[]
primaryProvenanceHandle[]
argumentBase[]
argumentCount[]
noteBase[]
noteCount[]
```

A Contract evidence projection may preserve:

```text
ContractDiagnosticEvidence

semanticSubject
    → exact Established material

determiningBasis
    → exact Basis references

applicableContext
    → exact relevant context

failureReference
    → exact Failure when present
```

### Consumed by

```text
CLI rendering
IDE diagnostics
reports
test failure explanation
reproducer generation
```

Rendering is separate from semantic diagnostic identity.

---

## A.16. Realization Body IR

### Produced by

```text
User JVM classfile
    ↓
Realization Acquisition
    ↓
normalization / CFG formation
```

### Example IR

```text
realization.method @m31

signature
    → OrderOperation.place(PlaceOrderFact) -> OrderPlacedFact

entry bb0

bb0:
    %v0 = param 0
    %v1 = call @helper.calculateTotal(%v0)
    %v2 = new OrderPlacedFact(%v1)
    return %v2
```

### Possible V1 backing

```text
MethodTable

ownerTypeHandle[]
nameHandle[]
descriptorHandle[]
blockBase[]
blockCount[]
valueBase[]
valueCount[]
callSiteBase[]
callSiteCount[]
```

```text
BlockSlab

methodHandle[]
firstInstruction[]
instructionCount[]
successorBase[]
successorCount[]
```

```text
CallSiteSlab

callerMethod[]
blockHandle[]
targetKind[]
targetHandle[]
originHandle[]
```

### Consumed by

```text
local structural analysis
Contract-Aware Analysis
verification
Execution Formation
```

This IR describes realization, not Contract meaning.

---

## A.17. Admitted Realization Binding

### Produced by

```text
external composition / DI
    ↓
effective implementation selection
    ↓
realization admission boundary
```

### Example

```text
realization.binding @rb12

operationDefinition
    → OrderOperation.place / V2

realizationMethod
    → method @m31

admission
    → Admitted

origin
    → external-composition#4
```

### Possible V1 backing

```text
OperationRealizationBindingTable

operationDefinitionHandle[]
methodHandle[]
admissionTag[]
originHandle[]
```

### Consumed by

```text
Core closure verification
Contract-Aware Analysis
Execution Formation
```

This is realization-side binding material.

---

## A.18. Contract-Aware Analysis and Verification Overlay

### Produced by

```text
Canonical Contract World
+
Realization Body IR
+
Admitted Realization Binding
+
local structural analysis
    ↓
Contract-Aware Analysis
    ↓
Core Realization Verification
```

### Example analysis

```text
analysis.contractAware @ca9

subject
    → method @m31

contractDefinition
    → operationDefinition#72

fixedContext
    State
        → Active

callTargets
    @helper.calculateTotal
        → exact method @m88

effects
    → no undeclared external factual ingress

origin
    → result values derived from admitted input

specialization
    → state-is-fixed
```

### Possible V1 backing

```text
ContractAwareSummary

subjectHandle[]
contractDefinitionHandle[]
contextBase[]
contextCount[]
callTargetSummaryHandle[]
effectSummaryHandle[]
originSummaryHandle[]
specializationSummaryHandle[]
```

Verification:

```text
verification.overlay @v7

realizationGeneration
    → RG5

subject
    → method @m31

result
    → Verified

evidence
    → summaryRange[40..44)
```

```text
VerificationOverlayTable

subjectHandle[]
realizationGeneration[]
verificationTag[]
summaryBase[]
summaryCount[]
```

### Consumed by

```text
Execution Formation
optimization
diagnostics
Whole-Machine analysis
```

These products are derived and recomputable.

---

## A.19. Whole-Machine Summary

### Produced by

```text
per-Core semantic / realization summaries
    ↓
Whole-Machine analysis
```

### Example

```text
wholeMachine.summary @wm3

machine
    → OrderMachine / V3

cores
    → coreSummary#11
    → coreSummary#12

crossCoreBindings
    → bindingRange[500..504)

reachableOperations
    → operationSet#7

effects
    → effectSummary#31

origin
    → originSummary#18

specializationContext
    Policy World
        → Normal
```

### Possible V1 backing

```text
WholeMachineSummary

machineHandle[]
coreBase[]
coreCount[]
crossBindingBase[]
crossBindingCount[]
reachableOperationSetHandle[]
effectSummaryHandle[]
originSummaryHandle[]
contextBase[]
contextCount[]
```

### Consumed by

```text
Whole-Machine verification
Execution Formation
global pruning
selective full-body materialization
```

The summary is derived compiler knowledge, not Whole-Machine Contract authority.

---

## A.20. Execution Formation Input

### Consumes

```text
Canonical Contract World
verified realization
Admitted Realization Binding
Contract-Aware Summary
Whole-Machine / specialization knowledge
target capability
```

### Example

```text
execution.form @ef5

interactionDefinition
    → Order.place / V3

realizationBinding
    → realization.binding @rb12

verification
    → verification.overlay @v7

staticContext
    State
        → Active

derivedKnowledge
    → contractAwareSummary#9
    → wholeMachineSummary#3
```

Execution Formation may already resolve:

```text
Version V3
    → fixed

State Active
    → fixed

realization target
    → method @m31

unselected alternatives
    → not materialized
```

### Produces

```text
Contract-Aware Execution IR
```

---

## A.21. Contract-Aware Execution IR

### Exposes

```text
executable control flow
values
realization calls
runtime Contract judgments
Failure relation
State movement reference
Publication relation
Output relation
exact Contract references
```

### Example IR

```text
exec.function @interaction.placeOrder

contract
    → Order.place / V3

entry bb0

bb0:
    %in0 = input.slot 0

    %fact0 = establish.fact %in0
        definitionRef → factDefinition#18

    %r0 = realization.call @m31(%fact0)
        admittedBinding → rb12

    %j0 = contract.judgment PositiveBalance(%r0)
        definitionRef → @def:42

    branch %j0
        true  → bb_publish
        false → bb_fail

bb_publish:
    publication.emit %r0
        publicationRef → publicationDefinition#63
        outputRef      → outputDefinition#64

    return

bb_fail:
    failure.establish
        failureRef → failureDefinition#71

    return
```

### Possible V1 backing

```text
ExecutionFunctionTable
ExecutionBlockSlab
ExecutionValueSlab
ExecutionInstructionSlab
RuntimeJudgmentSlab
ContractReferenceSlab
```

```text
RuntimeJudgmentSlab[60]

definitionHandle = 42
valueSlot        = 4
successTarget    = 9
failureTarget    = 7
```

### Consumed by

```text
Execution IR analysis
Contract-specific optimization
generic cleanup
JVM lowering
```

State Transition is not a CFG edge. Failure is not automatically a JVM exception. Publication is not automatically a
return instruction.

---

## A.22. Failure, Publication, and Output Projections

The exact semantics remain owned by their 1D ADRs. These are compiler projections only.

### Failure projection

```text
FailureProjection

failureDefinition
    → failureDefinition#71

origin
    → invariant occurrence @occ:17

failedMeaning
    → PositiveBalance unsatisfied

applicableContext
    → contextRange[55..56)

stoppedScope
    → Core Order

executionTarget
    → block 7
```

```text
FailureProjectionTable

failureDefinitionHandle[]
originKind[]
originHandle[]
contextBase[]
contextCount[]
stoppedScopeHandle[]
executionTarget[]
```

### Publication projection

```text
PublicationProjection

publicationDefinition
    → publicationDefinition#63

source
    → result Fact F88

allowedProjection
    → publicationFields[10..14)

executionTarget
    → block 9
```

```text
PublicationProjectionTable

publicationDefinitionHandle[]
sourceKind[]
sourceHandle[]
fieldBase[]
fieldCount[]
executionTarget[]
```

### Output projection

```text
OutputProjection

outputDefinition
    → outputDefinition#64

publication
    → publicationDefinition#63

hostShape
    → OrderPlacedOutput

adapterBoundary
    → external-output-adapter#2
```

```text
OutputProjectionTable

outputDefinitionHandle[]
publicationDefinitionHandle[]
hostShapeHandle[]
adapterBoundaryHandle[]
```

### Consumed by

```text
Execution Formation
Execution IR
JVM lowering
diagnostics
```

---

## A.23. Execution Analysis, Optimization Generation, and Hot Projection

### Example analysis

```text
execution.analysis @ea4

function
    → interaction.placeOrder

reachability
    → blockSet#10

dominance
    → domTree#3

fixedContractContext
    State
        → Active

specializationOpportunity
    → publication-path-fixed
```

### Optimized generation

```text
Execution IR Generation 12
    ↓
static judgment discharge
formation-time pruning
CFG simplification
DCE
    ↓
Execution IR Generation 13
```

The IR vocabulary may remain the same.

### Hot projection

```text
ExecutionFunctionTable[5]

entryBlock           = 0
inputBase            = 20
inputCount           = 1
operationTarget      = 31
runtimeJudgmentBase  = 60
runtimeJudgmentCount = 1
failureTarget        = 7
publicationTarget    = 9
```

A hot consumer need not traverse:

```text
Definition
    → Version
    → Required Basis
    → Basis Binding
    → Applicability
    → Realization Binding
```

again after a verified projection exists.

### Consumed by

```text
JVM lowering
performance diagnostics
translation / differential validation
```

---

## A.24. JVM Plan / IR and Classfile Product

### Produced by

```text
optimized Contract-Aware Execution IR
    ↓
JVM capability / legalization
    ↓
JVM Plan / IR
    ↓
classfile emission
```

### Example JVM plan

```text
jvm.method-plan @placeOrder

descriptor
    → (LPlaceOrderFact;)LOrderPlacedFact;

locals
    slot0
        → input

    slot1
        → operation result

blocks
    B0
        → load input
        → invoke-static OrderService.place
        → store slot1
        → test lowered PositiveBalance predicate
        → if-false B2
        → goto B1

    B1
        → load slot1
        → return

    B2
        → materialize mapped failure
        → return / throw according to selected output realization
```

### Possible V1 backing

```text
JvmMethodPlanTable
JvmBlockSlab
JvmValueSlab
JvmInstructionSlab
JvmExceptionRegionSlab
ConstantPoolPlan
FramePlan
```

### Classfile product

```text
ClassfileProduct

generatedType
    → io.example.OrderInteractionMachine

bytes
    → artifact#55

sourceContract
    → Order / V3

backendTarget
    → JVM target#4
```

### Consumed by

```text
JVM verifier
HotSpot / Graal
artifact publication
```

The JVM backend does not re-establish Contract meaning.

---

## A.25. Primitive Storage Families and Split / Fuse Examples

A plausible V1 physical family is:

```text
Frontend
    Source / Provenance Store
    ResolvedDefinitionHIR
    ResolvedRelationSlab

Canonical Contract World
    AuthorityTable
    VersionTable
    DefinitionTable
    RequiredBasisSlab
    DefinitionRelationSlab
    StableKeyIndex

Occurrence / semantic-use material
    OccurrenceTable
    BasisBindingSlab
    ContextSlab
    CompositionTable
    CompositionInputSlab

Sibling compiler products
    GeneratedApiProduct
    ReferenceResult
    TestObjectiveSlab
    GeneratedTestCase
    ContractCoverage
    CompilerDiagnosticRecord

Realization
    MethodTable
    BlockSlab
    ValueSlab
    CallSiteSlab
    OperationRealizationBindingTable

Derived analysis
    ContractAwareSummary
    VerificationOverlayTable
    WholeMachineSummary

Execution
    ExecutionFunctionTable
    ExecutionBlockSlab
    ExecutionInstructionSlab
    RuntimeJudgmentSlab
    FailureProjectionTable
    PublicationProjectionTable
    OutputProjectionTable

Backend
    JvmMethodPlanTable
    JvmBlockSlab
    JvmInstructionSlab
    ConstantPoolPlan
    ClassfileProduct
```

This is a candidate map, not a required product list.

### Split example

```text
RequiredBasisSlab

requirementCoordinate[]
requiredMeaningKind[]
cardinalityKind[]
```

```text
BasisBindingSlab

requiredBasisHandle[]
sourceHandle[]
applicationHandle[]
```

### Fused example

```text
ApplicationBasisSlab

requirementCoordinate[]
requiredMeaningKind[]
cardinalityKind[]
sourceHandle[]
applicationHandle[]
applicabilityTag[]
```

Both realize the same semantics. The compiler may choose either from actual access patterns.

---

## A.26. Query / Product View and Frozen Publication

### Query / product view

```text
resolvedContractHir(sourceUnit)
    → Resolved Contract HIR

establishedDefinition(definitionKey)
    → published Definition handle

requiredBasis(definitionHandle)
    → Required Basis range

referenceJudgment(subject, basis, context)
    → Reference Result

verifyRealization(operationDefinition, realizationBinding)
    → Verification Overlay

wholeMachineSummary(machine)
    → Whole-Machine Summary

formExecution(interactionDefinition, admittedBinding, target)
    → Execution IR generation
```

```text
query dependency
    ≠ Contract Basis Binding
```

### Frozen publication

```text
worker-local / private builders
    ↓
semantic verification
reference closure
collision verification where required
deterministic merge
    ↓
seal / freeze
    ↓
Published Generation G12
    ↓
read-only consumers
```

A generation is a publication / validity boundary, not Contract Version.

---

## A.27. Example Consumer Reads

### Reference Judgment

```text
reads
    DefinitionTable[42]
    RequiredBasisSlab[80..81)
    BasisBindingSlab[100..101)
    ContextSlab[55..56)

produces
    ReferenceResult[r11]
```

### Diagnostics

```text
reads
    OccurrenceTable[17]
    DefinitionTable[42]
    BasisBindingSlab[100..101)
    ContextSlab[55..56)
    ProvenanceStore[91]

produces
    CompilerDiagnosticRecord[D-KON-0174]
```

### Verifier

```text
reads
    DefinitionTable[operationDefinition]
    Realization Body IR method[31]
    OperationRealizationBindingTable[12]
    ContractAwareSummary[9]

produces
    VerificationOverlay[7]
```

### Whole-Machine analysis

```text
reads
    CoreSummary[*]
    CompositionTable[*]
    ContractAwareSummary[*]

produces
    WholeMachineSummary[3]
```

### Execution Formation

```text
reads
    Canonical Contract World G12
    VerificationOverlay[7]
    ContractAwareSummary[9]
    WholeMachineSummary[3]
    Realization Body IR
    Admitted Realization Binding[12]

produces
    Contract-Aware Execution IR Generation 12
```

### JVM backend

```text
reads
    Optimized Execution IR Generation 13

produces
    JVM Plan / IR
    Classfile Product
```

These examples define producer / consumer direction, not one mandatory pass schedule.

---

## A.28. Coverage Audit of Major V1 Material Families

```text
Contract frontend

Source / Provenance
    → A.5

Resolved Contract HIR
    → A.5

Authority-Owned Establishment
    → A.6

Established Definition Material
    → A.6

Canonical Contract World
    → A.7

Frozen publication
    → A.26


ADR-0063 semantic relations

Required Basis
    → A.8

Basis Binding
    → A.8

Applicability
    → A.9

Applicable Context
    → A.9

Complete Basis
    → A.9

Established Occurrence
    → A.10

Composition
    → A.11


Canonical-world sibling products

Generated APIs
    → A.12

Reference Judgment
    → A.13

PBT / Fixture
    → A.14

Contract Coverage
    → A.14

Diagnostics / Evidence
    → A.15


Realization

Realization Body IR
    → A.16

Admitted Realization Binding
    → A.17

Contract-Aware Analysis
    → A.18

Verification Overlay
    → A.18

Whole-Machine Summary
    → A.19


Execution / optimization

Execution Formation
    → A.20

Contract-Aware Execution IR
    → A.21

Failure projection
    → A.22

Publication projection
    → A.22

Output projection
    → A.22

Execution analysis
    → A.23

optimized Execution IR generation
    → A.23

hot execution projection
    → A.23


Backend

JVM Plan / IR
    → A.24

Classfile Product
    → A.24


Compiler infrastructure

primitive / slab candidates
    → A.25

split / fuse alternatives
    → A.25

query / product boundaries
    → A.26

generation publication
    → A.26

consumer reads
    → A.27
```

The following remain intentionally open:

```text
exact HIR schema
exact Canonical Contract World schema
exact Whole-Machine summary schema
exact query names
exact Execution IR opcode set
SSA choice
CFG physical layout
exact JVM IR instruction set
exact slab layout
exact HID encoding
exact product granularity
```

---

## A.29. Implementation Reading Rule

```text
1. Read the normative section
    → identify exact Contract meaning

2. Find the matching Appendix A example
    → use it as a V1 starting shape

3. Check the current compiler architecture
    → identify the actual IR / product owner

4. Choose physical split / fuse from real access patterns
    → not from semantic naming alone

5. Keep stable semantic identity separate from dense local addressing

6. Publish only complete verified generations

7. Let Execution Formation pre-resolve hot semantic chains when legal

8. Never let the chosen representation become Contract authority
```

If a change affects only table layout, slab layout, dense handles, summary shape, query boundary, materialization
strategy, split / fuse choice, or hot / cold placement, ADR-0063 need not change.

If it changes the normative semantic relation, the change is not implementation-only.