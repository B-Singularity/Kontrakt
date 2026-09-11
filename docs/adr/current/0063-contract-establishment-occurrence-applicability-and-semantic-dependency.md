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