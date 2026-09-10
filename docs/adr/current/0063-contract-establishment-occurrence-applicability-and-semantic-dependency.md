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

At minimum, the occurrence must preserve the exact semantic coordinates needed to interpret that established result.

```text
Established Occurrence O

Occurrence Reference
    → exact semantic application

Definition Reference
    → exact applied Definition

Applicable Basis
    → exact Required Basis
    → exact Basis Binding
    → exact Established Material

Applicable Context
    → exact meaning-relevant established coordinates only

Applicability Law
    → exact law coordinate
      only when the other preserved coordinates
      do not identify that law uniquely

Established Result
    → exact occurrence-owned meaning
```

The occurrence does not need to copy complete source material when an exact semantic reference already preserves the
required relation.

It must not replace exact attribution with a lookup of `current`, `latest`, nearest, or physically reachable material.

A successful Applicable Basis relation already means that the bound material was applicable to that occurrence.

A second independent `applicable = true` field is not required by Contract law.

Candidate search order, rejected alternatives, solver trace, query dependencies, cache state, worker state, and storage
position are not occurrence meaning unless the owning Contract explicitly makes one of them semantic basis.

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

## 7.3. Applicability Result and Applicable Basis

The result of an Applicability judgment is:

```text
Applicable
```

or:

```text
Inapplicable
```

The source material does not carry a permanent `applicable` flag.

Applicability belongs to the dependent use.

```text
Established Material M

Use U1
    → Applicable

Use U2
    → Inapplicable
```

When the result is `Applicable`, the Basis Binding may become **Applicable Basis** for that exact semantic use.

```text
Applicable Basis

Required Basis R
    → Basis Binding B
        → Established Material M
    → applicable to Use U
```

The Applicable Basis relation itself records successful applicability.

A separate independent Boolean is not required.

The following do not establish Applicable Basis:

```text
same Contract kind
material exists
material is published
material is reachable
Basis Binding exists
compiler lookup succeeds
cache entry exists
```

A bound but inapplicable material does not satisfy the Required Basis.

This ADR does not create one permanent Established Applicability object for every applicability check.

If a later Established result depends on the judgment, the exact determining relation must remain attributable according
to Section 7.6.

### Why This Choice

A permanent flag would incorrectly move use-specific meaning onto the source material.

A universal Applicability object for every check would turn semantic evaluation into an occurrence or query-result
store.

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
Governance Arbitration

Determining Decisions
    → D1
    → D2
    → D3

Arbitration Law
    ↓
Resolved Selection
```

Here the complete competing set is semantic Basis because the Governance law says so.

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

Later establishment may change what is applicable to later semantic use.

Earlier Established Occurrence Material remains unchanged.

```text
Occurrence O17
    → Basis Binding B17
    → State Ready

later

State Ready
    → State Closed

Occurrence O17
    → unchanged
```

The new State may affect a later occurrence.

It does not retroactively rewrite the earlier Applicable Basis or Applicable Context.

The authority that owns succession decides which material applies to the later semantic application.

No universal mutable `current` result is created here.

---

## 7.8. Version and Governance

Version may participate in Applicable Context when the owning applicability law makes Version relevant.

Governance Binding may participate when the dependent law requires Governance material.

```text
Applicable Context

Version
    → V7

Governance Binding
    → G22
```

Neither coordinate is universal.

An Applicability law that does not use Version must not acquire Version dependency merely because Version exists.

An Applicability law that does not use Governance must not acquire Governance dependency merely because a Governance
Binding is available.

The original source authority remains unchanged when its material becomes applicable to another semantic use.

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

Occurrence-specific Established Material keeps the exact semantic attribution that determined its establishment.

```text
Established Occurrence O

Definition
    → exact Definition Reference

Applicable Basis
    → exact bound source material

Applicable Context
    → exact meaning-relevant context at O
```

Later Version, Policy, Governance, State, Basis Binding, or other material may support a new judgment.

It may not be presented as though it belonged to the earlier occurrence.

The earlier occurrence must not be reinterpreted through `current` or `latest` semantic material.

This is the common law that lets later consumers and Diagnostic distinguish source-time meaning from later
reconstruction.
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
resolved Basis bindings where already established as definition meaning
definition-owned Applicability relations where already established
other authority-owned relations required by the definition meaning
```

Source provenance may be exposed through a separate exact relation.

The world must distinguish a relation that is not owned by an authority from a relation that is required but unresolved.

It must not manufacture missing semantic relations for downstream convenience.

Occurrence-specific Applicable Basis, Applicable Context, and occurrence attribution do not become Definition-world
material merely because they reference definitions in the Canonical Contract World.

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

Where the owning law requires Complete Basis, partial authority is invalid.

An Applicability judgment is invalid when its Basis Binding, dependent application, meaning-determining context, or
required applicability-law coordinate cannot be identified exactly.

Multiple applicable candidates do not authorize implicit first-match or compiler-selected resolution.

Selection requires the owning Singularity, completeness, or Arbitration law.

Composition must preserve the exact source references used by the composing law.

Linking must preserve unchanged Definition References.

Established Definition Material is incomplete when a semantic consumer would need to reconstruct source-owned meaning,
Owning Authority, or an already-established required relation from source syntax, realization topology, compiler
heuristics, or another compiler product.

Established Occurrence Material is incomplete when its owning meaning depends on Applicability but the exact Applicable
Basis or required Applicable Context cannot be recovered.

An earlier occurrence is invalidly represented when later `current` or `latest` material can silently replace the exact
semantic attribution fixed at that occurrence.

Candidate search order, solver trace, query state, cache state, compiler generation, and storage layout must not be
required to recover Contract Applicability meaning.

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

Established Definition Material has an explicit semantic binding model.

Definition Meaning, Authority-Owned Definition Identity, and Owning Authority Binding remain distinct.

Every Established Definition has exactly one Owning Authority Binding.

An exact Definition Reference combines the authority reference with the identity defined under that authority.

Basis is explicit as Required Basis, Basis Binding, Applicability, and Complete Basis.

Applicability is now explicit as a judgment over one exact Basis Binding, one exact dependent application, and only the
Contract context used by the owning applicability law.

Applicable Basis records successful applicability for that semantic use.

The source material does not acquire a permanent applicability flag.

Multiple applicable candidates do not create implicit selection authority.

An Established Occurrence that depends on Applicability preserves its exact Definition Reference, determining Applicable
Basis, meaning-relevant Applicable Context, and an exact applicability-law coordinate only when that law is not already
uniquely determined.

The occurrence preserves direct determining references rather than copying the transitive semantic world.

Later Version, Policy, Governance, State, or other semantic material cannot rewrite an earlier occurrence.

This prevents source layout, compiler containment, solver behavior, local handles, fingerprints, cache state, and
publication generation from silently becoming Contract meaning.

The Canonical Contract World preserves exact Established Definition bindings without absorbing occurrence-specific
attribution into one aggregate definition world.

The compiler may choose different physical representations and future incremental mechanisms as long as these exact
semantic distinctions and bindings remain unchanged.

The cost is explicit binding and attribution bookkeeping.

The benefit is precise establishment, diagnostics, reuse, and later incremental invalidation without making one compiler
implementation authoritative.

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

## 2026-09-11 — Explicit Basis Binding Contract

Basis is now expressed as an explicit Contract model rather than an abstract dependency description.

The amendment separates Required Basis, Basis Resolution, Basis Binding, Applicability, and Complete Basis.

Required Basis is owned by the semantic judgment that needs it and names required meaning rather than producer topology.

Distinct requirements remain distinguishable even when they require the same Contract kind.

Basis Resolution produces an exact Basis Binding from one Required Basis to exact Established Material under the owning
composition law.

Binding does not imply Applicability.

The owning Contract defines cardinality and completeness.

`Not Owned`, `Permitted Absence`, `Required, Unresolved`, `Bound, Inapplicable`, and `Bound, Applicable` remain distinct
semantic states.

A resolved source becomes Definition-identity-significant only when the owning Contract makes that exact source relation
part of Definition Meaning.

Basis prerequisite order is semantic partial order rather than compiler scheduling order. Circular semantic
establishment remains invalid.

Compiler dependency remains derived from Contract Basis Binding and cannot create it.

## 2026-09-11 — Explicit Applicability and Occurrence Attribution Contract

Applicability is now defined over an exact Basis Binding, an exact dependent semantic application, and only the
meaning-determining context used by the owning applicability law.

Applicable Context is sparse Contract context rather than a universal nullable context object.

A successful Applicability judgment produces an Applicable Basis relation for that semantic use.

The source material does not receive a permanent `applicable` flag, and this ADR does not create one permanent
Established Applicability object for every check.

Multiple applicable candidates do not authorize implicit selection. Singularity, completeness, or explicit Arbitration
remains responsible for selection.

When Applicability contributes to Established Occurrence Material, the occurrence preserves its exact Definition
Reference, determining Applicable Basis, exact meaning-relevant context, and the applicability-law coordinate only when
the other preserved coordinates do not already identify that law uniquely.

The occurrence preserves direct determining semantic references rather than the full transitive semantic world.

Existing Established Material may therefore be referenced without copying its complete meaning.

Later Version, Policy, Governance, State, Basis Binding, or other material does not rewrite the attribution of an
earlier
Established Occurrence.

Candidate search order, rejected alternatives, solver trace, query dependency, cache state, compiler generation, and
storage layout remain outside Contract attribution unless an owning Contract law explicitly gives one of them semantic
meaning.