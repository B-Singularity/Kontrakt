# ADR-0063: Contract Establishment, Identity, Applicability, and Composition

## Status

Accepted

## Date

2026-08-30

## Related

- `../../../../the-most-important-thing/what-contract-is.md`
- `../../../../todo/contract/kontrakt-established-contract-world-architecture-todo.md`
- `../../../../todo/roadmap/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `../../../../todo/roadmap/kontrakt-v2-reference-architecture-and-v1-foundations.md`
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
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0074: Compiler Result, Explicit Unsuccessful Result, Recovery, and Observation Boundary

---

## 1. Context

Kontrakt has several authorities whose results become material for later judgment.

A later authority must be able to trust such material without taking ownership of its meaning.

The common question is:

> When does Contract material become authoritative, how is that exact meaning identified, and when may another
> responsibility rely on it?

Material before Establishment is not authoritative merely because syntax is valid, resolution succeeded, a compiler
representation exists, or no current analysis rejects it.

The Contract boundary owned by this ADR begins when Authority-Owned Establishment observes resolved candidate meaning
through the Resolved HIR Candidate Protocol defined by ADR-0071.

```text
Resolved Contract HIR
    ↓
Resolved HIR Candidate Protocol
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

Establishment must preserve the exact authority and semantic distinctions owned by the source while remaining
deterministic under the same semantic basis. A later authority may rely on established meaning only through relations
that its own law admits; compiler reachability, execution order, cache state, or another implementation fact cannot
supply missing Contract meaning.

The semantic model must also survive replacement of compiler representation. Host-object identity, storage topology,
product granularity, and physical split or fusion may change without changing Contract identity. Distinct semantic
concepts therefore do not require distinct objects, allocations, tables, or reference hops.

Each Contract keeps ownership of its own result meaning. Compiler analysis, scheduling, reuse, publication machinery,
and backend realization may preserve or consume that meaning, but they cannot create or revise its authority.

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

Under the boundary established by Section 1, a declaration becomes **Established Definition Material** only after the
owning Contract law accepts its complete semantic definition. Resolved Contract HIR remains pre-authority material; the
legal HIR observation rules are owned by Section 4.7.1 and ADR-0071.

Established Definition Material is the authoritative definition meaning represented by the Canonical Contract World.
Compiler publication may expose a completed representation, but it does not create this authority.

---

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

Section 1's pre-authority rule applies to candidate material. The target authority alone decides whether an exact
candidate
crosses its Establishment boundary.

### 4.7.1. Establishment Input Contract

Section 1 fixes the Resolved HIR-to-Establishment boundary. This section defines how Establishment may consume that
boundary.

Material observed through the Resolved HIR Candidate Protocol must already identify the semantic subject required by the
owning law, and every reference required to interpret that candidate must denote an exact resolved semantic target.
Candidate meaning must be resolved before this boundary. Parser recovery cannot stand in for it, and Establishment
cannot
infer a missing part from host structure, realization topology, compiler heuristics, or physical HIR layout. The owning
law still decides whether the observed candidate meaning and separately owned authoritative prerequisites are
sufficient.

```text
Resolved HIR Candidate Protocol
    → exact Candidate Reference
    → legal candidate observation

separately owned authoritative prerequisites
    ↓
owning Establishment law
```

For a Definition judgment, the Definition Candidate Projection is the complete producer-owned observation of that
Definition Candidate meaning under the owning 1D law. A judgment may instead consume producer-defined Fine-Grained
Semantic Projections when those projections expose every observation required by the owning law. This permits lazy or
partial physical reads without creating consumer-specific HIR meaning. An observation omitted by a narrower projection
is not semantic absence; absence is usable only when the producer-owned HIR meaning represents it explicitly.

An IDL Binding Candidate Projection is consumed only when the owning judgment requires the pre-authority selection
relation represented by that Binding Candidate. It does not supply or reconstruct the target Definition Candidate
meaning. When the target meaning is required, Establishment follows the exact target Candidate Reference and consumes
the
corresponding Definition Candidate Projection or a legal fine-grained projection of it.

Frontend resolution does not turn an IDL Binding Candidate into Version Binding, Basis Binding, Governance Binding, or
another authority-bearing relation. Any later authoritative binding remains a separately established relation.

Establishment cannot recover missing candidate meaning from authored source, generic provenance, mutable producer state,
query topology, cache state, or implementation containment. This constrains the semantic observation boundary rather
than
the physical access mechanism: a realization may share immutable backing or materialize observations lazily when the
same
Protocol law remains intact and physical coordinates stay semantically unobservable.

Source provenance remains a separate relation. If an owning 1D law makes an exact source coordinate part of Definition
Candidate meaning, that coordinate is exposed as candidate meaning under that law rather than imported from generic
provenance.

The Establishment Input Contract does not require a second semantic input IR or DTO. Physical staging may combine legal
observations for execution, but it has no independent semantic completeness, identity, or authority. Already-Established
prerequisites likewise remain outside the HIR Protocol and keep the authority defined by their owning laws.

### 4.7.2. Establishment Entry and Result Boundary

ADR-0063 does not define a universal `EstablishmentOutcome` taxonomy. An Establishment judgment produces semantic
meaning
only after that exact judgment is legally entered and its owning law can establish an exact result.

A prerequisite result that prevents legal entry does not create a second unsuccessful result for the unentered
Establishment judgment. The prerequisite result remains owned by the judgment that established it. Work that does not
depend on the unavailable meaning may continue only while its own legal inputs remain available and the compiler domain
supplying them remains trustworthy; ADR-0057 and ADR-0074 own the corresponding Contract-machine and compiler-side
continuation laws.

When an entered Contract or State-Machine judgment establishes that required machine meaning is not satisfied, the exact
unsuccessful meaning remains owned by that authority and Failure is governed by ADR-0057. A result that merely looks
negative does not become Failure unless the owning law establishes unsuccessful required meaning.

When a compiler-owned judgment can establish that its own compiler requirement is not satisfied, its unsuccessful result
is governed by ADR-0074. That compiler result does not become Contract Failure and does not establish an unsuccessful
result for an authority judgment that was never entered.

If crash, trust-domain loss, or another machinery failure leaves the exact authority-owned result indeterminate,
Kontrakt
must not fabricate successful Establishment, Contract Failure, or an exact compiler rejection from the absence of a
known
result. Conversely, once an authority-owned result has been established, a later compiler-side unsuccessful result does
not rewrite or revoke that established meaning. Durable project-history publication remains a separate boundary under
Section 6.9 and ADR-0053.

---

## 4.8. Source Authority

Established Material keeps the authority that established its meaning. In the authority-crossing case introduced in
Section 2, the consumer owns only the new conclusion established under its own law; the source authority continues to
own
the source meaning. Consumption cannot rewrite that meaning.

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
authority. Version Identity is authority-scoped Contract meaning, so equal authored labels under different authorities
do
not establish Version Agreement.

### Version Claim and Resolution

A **Version Claim** is a declared coordinate presented for frontend resolution. Exact resolution produces a **Resolved
Version Candidate Coordinate** that identifies the required Authority / Version pair without lexical ambiguity. This is
exact pre-authority compiler-semantic meaning, not yet the authoritative Version Binding.

```text
Version Claim
    → Stable

Required Authority
    → A

exact Authority-scoped resolution
    ↓

Resolved Version Candidate Coordinate
    → A / Stable
    ↓
owning Establishment law
    ↓

Version Binding
    → A / Stable
```

Establishment does not reselect the Version. It may consume an already-established exact Version Binding, or the
applicable Version law may establish the required binding when none exists. Reusing an existing binding does not mint a
second semantic Version relation, and failure of one Definition judgment does not revoke an independently established
binding.

A rejected candidate establishes no substitute Version Binding. Unknown or ambiguous claims remain unresolved rather
than being mapped to `current`, `latest`, `preferred`, `nearest`, or another fallback. Compiler lookup behavior
therefore
cannot select Contract Version.

### Versioned Definition Coordinate

An authority that owns one independently addressable Definition per Version needs no additional Definition coordinate.
When one Authority / Version owns several independently addressable Definitions, the owning identity law must preserve
an
additional **Authority-Local Definition Coordinate**. That coordinate may be typed or structured; it is not a mandatory
universal scalar field and cannot be replaced by declaration order, table position, source nesting, hash order, or
compiler discovery order.

```text
Authority A
    +
Version V
    +
Authority-Local Definition Coordinate L
        ↓
exact Versioned Definition coordinate
```

The same exact Authority / Version / local coordinate cannot authoritatively denote conflicting Definition Meaning.
ADR-0053 owns the history and merge rules used to resolve such conflicts; Version Binding itself introduces no
compatibility or merge rule.

---

## 6.4. Definition Reference

A **Definition Reference** identifies one exact authoritative Definition. A HIR Candidate Reference identifies exact
pre-authority compiler-semantic material and is not converted, promoted, cast, or retyped into a Definition Reference.
The owning Establishment judgment establishes authoritative Definition identity from only the semantic coordinates
admitted by the owning Contract identity law.

For a version-sensitive authority, the common semantic shape is:

```text
Definition Reference

Owning Authority Reference
    → A

Version Binding
    → V

Authority-Local Definition Coordinate
    → L
      only when required by the owning identity law

        ↓

exact authoritative Versioned Definition
```

The local coordinate need not be globally unique outside its Authority / Version. Equal local coordinates under
different
authorities or Versions therefore do not denote the same Definition.

A Definition Reference becomes legally observable only when the complete authoritative identity required to interpret
its
target is coherent. Required relations may already have been established independently or may be established by the
current owning judgment. This is a semantic visibility rule, not a requirement for one physical write order, transaction
mechanism, table layout, or allocation sequence. The unsuccessful and indeterminate paths are governed by Section 4.7.2.

A Definition Reference must resolve to the same source-owned Definition Meaning in an equivalent semantic world.
Candidate material and Established Definition Material may share immutable bytes, rows, or other backing, but physical
sharing transfers neither reference kind nor authority.

This ADR does not impose a universal one-to-one cardinality between Candidate References and Definition References.
Distinct current candidates may resolve to the same authoritative Definition when the owning identity law establishes
the
same identity and meaning, while equal candidate payload alone does not merge independently identified Definitions.
Candidate-to-Definition correspondence may be retained for diagnostics, reuse, or other compiler work as a
compiler-owned
relation; Definition identity must remain interpretable without retaining HIR or that correspondence.

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

ADR-0063 distinguishes pre-authority semantic coordinates, authoritative Contract coordinates, and compiler realization
coordinates.

```text
Pre-authority compiler-semantic coordinates
    HIR Candidate Reference
        → exact current candidate
    Resolved Version Candidate Coordinate
        → exact Authority-scoped Version candidate

Authoritative Contract coordinates
    Owning Authority Binding
    Version Binding
    Authority-Local Definition Coordinate when required
    Definition Reference
    Occurrence Reference

Compiler realization coordinates
    provenance location
    fingerprint / HID
    compiler generation / schema version
    dense handle / row / address
```

A value or physical encoding may be reused across these families, but reuse does not transfer its semantic role. The
transition from pre-authority meaning to authoritative meaning occurs only under the owning Establishment law. Compiler
coordinates may accelerate lookup, equality checking, persistence, or remapping; they do not create either pre-authority
HIR meaning or Contract identity.

A provenance-only movement, schema migration, compiler-generation change, or local relocation may therefore preserve the
same authoritative Definition Reference.

---

## 6.7. Deterministic Reference Resolution

Section 6.4 fixes what a Definition Reference denotes. Deterministic realization additionally requires physical
ordering,
allocation, scheduling, cache state, compiler generation, and publication layout to leave that resolution unchanged.

A later HIR generation may form different Candidate References or dense handles for current compiler work without
minting
a new authoritative Definition when the owning identity law establishes the same Definition. Previous compiler state may
provide reuse evidence, but it cannot supply inherited Contract identity.

---

## 6.8. Identity Across Linking

Linking preserves the exact Definition Reference of unchanged source-owned meaning and the Version Binding already fixed
by that reference. Physical relocation, aggregation into a larger compiler world, or artifact regeneration does not mint
a new Contract Version or Definition. A higher-scope authority may establish new meaning without rewriting the identity
of unchanged source definitions it consumes.

---

## 6.9. Current Establishment and Durable History Publication

Current semantic Establishment and durable project-history publication are different boundaries. An Established
Definition may exist as an authoritative result of the current compilation before the complete compilation reaches the
history-publication boundary defined by ADR-0053.

If later compiler work produces an unsuccessful result, that later result does not rewrite an already-established
Definition in the current compilation. If the compilation does not reach successful history publication, however, no new
durable Version record or History Revision is created merely because an intermediate Establishment succeeded.

A later compiler invocation therefore derives current authoritative identity from current explicit semantic inputs and
valid retained Contract history. Transient Candidate References, failed-run backing storage, cache entries, and previous
compiler-generation identity cannot transfer authority into the new invocation.

---

# 7. Applicability

## 7.1. Applicable Context

Established Material is authoritative under its source meaning. That does not make it valid Basis for every later
semantic use.

**Applicable Context** is the exact Contract context that the owning applicability law uses to decide one dependent
application. It is sparse because unrelated coordinates are excluded, and complete because every coordinate required by
that law must be available before the judgment can be entered.

```text
Applicable Context

Version
    → V4

State
    → Ready
```

Another applicability law may require different context.

```text
Applicable Context

Governance Binding
    → G17

Policy World
    → Emergency
```

A law that requires no additional context has no additional context requirement.

```text
Applicable Context
    → none
```

`none` means that the owning law requires no additional context. It does not name one universal empty context object.
Likewise, required context that is unavailable is not semantic absence. Explicit absence is meaningful only when the
owning law defines that absence as one of its legal semantic inputs.

A universal ambient context is not Contract law. Version, Policy, Governance, State, or another coordinate participates
only when the owning applicability law makes it meaning-determining. Source provenance and compiler-owned execution,
storage, reuse, or query state never enter Applicable Context merely because they are available to the implementation.

When one required coordinate is already Established Material, Applicability may preserve its exact semantic reference
rather than copy its complete meaning.

---

## 7.2. Applicability Judgment

**Applicability** decides whether one exact Basis Binding may participate in one exact dependent semantic application.
The judgment may be entered only when the exact Basis Binding, dependent application, owning applicability law, and the
complete Applicable Context required by that law are legally available.

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
    → complete meaning-determining context

Result
    → Applicable
    or
    → Inapplicable
```

Applicability observes the Basis Binding already established under Section 8.2. It does not accept another Required
Basis
or another source material that could disagree with that binding.

The dependent application identifies the exact semantic use being judged. It may be a Definition-time judgment, one
Contract Occurrence, or a higher-scope composition judgment. A runtime call, compiler query, source container, parent
object, or physical scope does not define that semantic use by itself.

The owning applicability law must be exactly identifiable. When the preserved authority, Definition, Required Basis, and
dependent application determine one law uniquely, no extra law coordinate is required. When distinct laws would remain
possible, the exact semantic law coordinate must remain recoverable. Declaration order, function order, table position,
or lookup path cannot resolve that ambiguity.

If required semantic input is unavailable, the Applicability judgment is not entered and therefore establishes neither
`Applicable` nor `Inapplicable`. If compiler machinery cannot produce its own required compiler result, ADR-0074 governs
that compiler-side unsuccessful result. Neither case may be rewritten as Contract-level `Inapplicable`.

---

## 7.3. Applicability Result

An entered Applicability judgment has exactly the two semantic results defined by its law.

```text
Applicability Judgment
    ↓
Applicable
or
Inapplicable
```

The result belongs to the exact dependent use. The same Established Material may therefore be Applicable for one use and
Inapplicable for another without acquiring a permanent applicability property.

```text
Established Material M

Use U1
    → Applicable

Use U2
    → Inapplicable
```

`Applicable` permits the exact Basis Binding to participate as Applicable Basis under Section 8.3. `Inapplicable` means
only that this Binding cannot serve that exact use. It does not establish Failure for the dependent judgment. If the
dependent judgment consequently lacks Complete Basis, Section 8.4 governs whether that judgment may be entered.

This ADR does not require one permanent Established Applicability object for every check.

---

## 7.4. Deterministic Applicability

Applicability depends only on semantic inputs owned by the applicable law.

```text
same Owning Applicability Law
    +
same Basis Binding
    +
same Dependent Application
    +
same complete Applicable Context
    ↓
same Applicability Result
```

Changing an unrelated Contract coordinate or any compiler-only representation, scheduling, provenance, cache, query, or
storage fact cannot change Applicability meaning. When one meaning-determining Contract coordinate changes, a later use
may require a new Applicability judgment.

Applicability validity is distinct from Basis Binding validity. A source connection may remain the same while a change
in
Applicable Context invalidates only the Applicability result. Reuse and invalidation machinery may exploit that
separation, but a compiler dependency graph does not define the semantic dependency.

---

## 7.5. Candidate Set, Singularity, and Arbitration

Applicability judges each exact Basis Binding for one dependent use. It does not select one Binding merely because
several
are Applicable.

```text
Basis Binding B1
    → Applicable

Basis Binding B2
    → Applicable
```

The presence of both results is passed to the owning Basis shape, completeness, singularity, or explicit arbitration law
that determines whether plurality is legal. Applicability itself supplies no first-match, nearest, declaration-order,
hash-order, handle-order, or other implicit preference.

A compiler may need sufficient knowledge of the legal Binding domain to prove a singularity or completeness claim. That
closure evidence does not make the discovered candidate set Contract meaning. Candidate membership becomes determining
Contract Basis only when the owning law explicitly makes that set part of the judgment.

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

A solver trace, search order, rejected alternatives, or compiler proof path remains compiler or Diagnostic material even
when it was useful to establish domain closure.

---

## 7.6. Applicability Attribution in Established Occurrence Material

When Applicability contributes to an Established Occurrence, the occurrence preserves the direct semantic relations that
determined the successful use.

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

The non-duplication rule of Section 4.4.1 applies. When a determinant already exists as Established Material with an
exact
reference, the occurrence preserves that reference rather than copying the source authority's complete meaning unless
the
owning occurrence law requires additional local meaning.

```text
Occurrence O
    → Governance Binding G17
        → Governance owns its own complete meaning
```

An occurrence preserves the exact material that determined it. It does not preserve a later lookup rule such as `current
Governance Binding`, `latest State`, or `current Version`. The Applicable Basis relation already records the successful
Applicability relation, so the occurrence does not require a second mutable applicability flag.

Candidate search order, rejected alternatives, proof traces, query dependencies, cache entries, compiler generations,
and general provenance are not occurrence attribution unless the owning Contract law independently makes an exact
coordinate determining semantic material.

### Example

```text
Invariant Occurrence O17

Definition
    → PositiveBalance / V3

Required Basis

target
    → exact Fact kind Balance

Applicable Basis

target
    → Basis Binding B42
        → Balance Fact F42

Applicable Context

Policy World
    → Normal

State
    → Active

Result
    → invariant satisfied
```

A later context change does not rewrite `O17`. A later semantic application may establish another occurrence under the
new context.

---

## 7.7. Applicability After Change

Section 7.4 owns rejudgment when meaning-determining Applicability context changes. Section 7.6 owns the fixed
attribution
of an already-established occurrence, and Section 9.3 records that integrity boundary.

---

## 7.8. Version and Other Established Context

Section 6.5 owns the Version already fixed by an occurrence's Definition Reference, so Applicability does not repeat
that
Version merely because the Definition is versioned. If an Applicability law independently consumes another Versioned or
otherwise Established relation, that exact relation belongs to Applicable Context.

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

A **Required Basis** is the exact semantic requirement that one Contract judgment must have satisfied before that
judgment
may be entered and establish its result. The requirement belongs to the judgment that needs the material.

```text
Definition judgment
    └── Definition-time Required Basis

Occurrence judgment
    └── Occurrence-time Required Basis

Higher-scope composition judgment
    └── Higher-scope Required Basis
```

A Definition may establish a reusable **Basis Requirement Law** as part of its meaning. That law describes the semantic
requirement shape that later exact judgments must instantiate. It does not itself become one shared Required Basis for
all
future applications.

```text
Basis Requirement Law
    ↓ instantiated for
exact dependent judgment U
    ↓
Required Basis R
```

Each Required Basis remains exactly attributable to its owning judgment or application and to the owning-law requirement
coordinate that distinguishes the need. Two coordinates may require the same semantic kind while remaining different
requirements.

```text
Required Basis

leftOperand
    → Fact

rightOperand
    → Fact
```

`leftOperand` and `rightOperand` are different semantic requirements even though both require Fact material. Declaration
order, array position, table position, storage order, or discovery order cannot create that distinction.

A Required Basis states what semantic meaning is required. It does not acquire a producer from compiler topology,
composition convenience, or whichever matching material is found first. When the owning Contract law explicitly makes an
exact Definition or other exact Established source part of the required meaning, that exact semantic reference is part
of
the requirement itself rather than an inferred producer preference.

A Definition owns a Required Basis only when that Basis is required for the Definition judgment itself. A requirement
law that governs later occurrences may belong to Definition meaning while each occurrence owns its own exact Required
Basis instance. Higher-scope requirements likewise remain with the higher-scope judgment that instantiates them.

---

## 8.2. Basis Resolution and Basis Binding

**Basis Resolution** applies the exact law that owns one cross-authority semantic source connection. It establishes an
exact **Basis Binding** between one Required Basis and one already-Established source material. A Basis Binding states
that
source connection; it does not by itself establish that the source is Applicable or that the Required Basis is complete.

```text
Required Basis R
    ↓ exact source connection
Established Material M

Basis Binding B
    → R
    → M
```

The binding-owning law determines the legal source domain for that connection. That domain may already be narrowed to
one
exact semantic source, or it may admit several Established Materials that must later be judged for Applicability and
completeness. Compiler reachability, source containment, query traversal, call edges, storage adjacency, and search
order
do not enlarge or define the legal source domain.

Basis Resolution is therefore semantic resolution under the binding-owning law, not generic compiler lookup. A compiler
may use indexes, summaries, solvers, or other machinery to find or prove legal connections, but a returned match or
model
is not Basis authority unless the owning law establishes the corresponding exact relation.

A Required Basis may consequently have no Basis Binding, one Basis Binding, or several Basis Bindings when its owning
shape permits that possibility. No intermediate semantic layer is inserted between the Required Basis and the exact
Basis
Bindings. Section 7 judges the Applicability of each Binding, and Section 8.4 determines whether the resulting
Applicable
Basis relations satisfy the owning completeness law.

Each Basis Binding preserves its exact Required Basis and exact Established source. When the Required Basis already
identifies its dependent judgment or application, an implementation may repeat that application coordinate for fast
access, but the duplicate field does not become a second semantic determinant.

A Basis Binding is immutable semantic relation material. A later application or later composition decision establishes a
new exact relation rather than mutating an earlier Binding. It may bind only to source material whose owning
Establishment has already succeeded. If the binding-owning law cannot establish an exact legal source connection, the
Required Basis remains unresolved.

---

## 8.3. Applicable Basis

A Basis Binding becomes **Applicable Basis** only when Section 7 establishes that the exact Binding is Applicable to its
exact dependent use.

```text
Required Basis
    ↓
Basis Binding
    ↓ Section 7
Applicable Basis
```

Existence, reachability, publication, successful compiler lookup, or Basis Binding alone does not establish Applicable
Basis. A bound but Inapplicable source remains an exact Basis Binding and cannot satisfy that use.

Section 7 owns Applicability meaning. This section does not restate its context, result, or arbitration laws.

---

## 8.4. Complete Basis

A judgment has **Complete Basis** only when the exact Required Basis instances owned by that judgment are satisfied
under
the owning Basis shape and completeness law. Complete Basis is a positive semantic condition over those exact relations.
It does not require a universal material object, and this ADR does not create parallel material merely to represent
incompleteness.

```text
Required Basis R1
    → Applicable Basis B1

Required Basis R2
    → unresolved

Result
    → Basis not complete
    → dependent judgment not entered
```

One satisfied requirement grants no authority for another unsatisfied requirement. The owning Contract law determines
which requirement shapes are legal and what makes them complete. An exactly-one requirement, a permitted absence, a
plural set, or alternatives have only the semantics explicitly assigned by that law. The common Establishment law does
not give `A or B` an implicit fallback order and does not make the first Applicable Binding preferred.

For an exactly-one requirement, Complete Basis requires proof that exactly one Applicable Basis exists in the legal
Binding domain. Finding one Applicable Binding is not sufficient when another legal Applicable Binding may still exist.
For a law that admits several Basis relations, plurality is preserved or reduced only according to that law's explicit
selection, equivalence, singularity, or arbitration meaning.

The compiler must therefore observe enough of the legal Binding domain to establish the exact completeness claim made by
the owning law. That requirement is semantic closure, not mandatory exhaustive traversal. A sound index, validated
summary, exact direct reference, or another replaceable mechanism may prove the same closure without enumerating every
Established Material. The candidate set or closure proof does not become Contract material merely because the compiler
needed it, unless the owning law itself makes that set determining semantic Basis.

Provisional discovery of one or more Applicable Bindings is compiler work. Complete Basis becomes authoritatively
observable only after the owning completeness law can establish its complete result. A missing, unresolved, or
Inapplicable Basis does not create a synthetic unsuccessful result for the dependent judgment. The dependent judgment is
not entered until its Required Basis is complete; ADR-0057 and ADR-0074 remain responsible for unsuccessful meaning at
the boundaries they own.

Complete Basis validity may depend on legal-domain membership even when the Applicability result of an existing Binding
has not changed. A newly legal Applicable Binding can invalidate an exactly-one completeness result without changing the
older Binding. Compiler reuse may track such closure dependencies, but its invalidation graph is not Contract meaning.

---

## 8.5. Basis States

The semantic distinctions below must remain recoverable even when one implementation encodes them compactly.

```text
Not Owned
    → the judgment has no such Required Basis

Permitted Absence
    → the owning law makes absence legal

Required, Unresolved
    → the requirement exists but no exact Basis Binding is established

Bound, Inapplicable
    → an exact Basis Binding exists but Section 7 rejects that use

Bound, Applicable
    → an exact Basis Binding may participate in completeness

Complete
    → the owning completeness law is satisfied for the judgment
```

`Not Owned`, `Permitted Absence`, and `Required, Unresolved` are different semantic states. Required context that is
unavailable also cannot be reinterpreted as permitted absence. These distinctions determine whether later semantic work
may be entered; they do not require separate heap objects or one universal state enumeration in physical storage.

---

## 8.6. Basis and Definition Identity

Section 8.1 owns the Required Basis relation. Establishing a source connection under Section 8.2 does not automatically
make that source part of the identity of the consuming Definition.

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

If the owning Contract defines `D` as requiring any source that validly satisfies `R`, the two worlds may preserve the
same Definition Meaning and Authority-Owned Definition Identity while holding different Basis Bindings.

```text
Definition Meaning
    → same

Authority-Owned Definition Identity
    → same

Basis Binding
    → different
```

If the owning Contract instead defines `D` specifically against exact Definition `F1`, that exact reference is part of
Definition Meaning. Changing the required exact source then changes meaning according to the owning law. The compiler
must not decide identity significance merely because one relation is convenient to hash, store, cache, or reuse.

---

## 8.7. Semantic Prerequisite Order

Basis creates semantic prerequisite order.

```text
Basis Requirement Law
    ↓ exact judgment instantiation
Required Basis
    ↓
Basis Binding
    ↓
Applicability
    ↓
Complete Basis
    ↓
Owning Judgment may enter
    ↓
Establishment
```

The diagram states semantic prerequisites, not compiler pass order. A compiler may fuse, parallelize, summarize, cache,
or incrementally repair those computations while preserving the same legal observations.

An owning judgment that requires Basis is not entered before its Basis is complete. Later Establishment may consume only
already-Established source material. A missing or Inapplicable prerequisite therefore blocks dependent entry; it does
not
become an automatic rejection or Failure of the unentered judgment.

An unresolved semantic prerequisite cycle cannot be completed by provisional mutual Bindings or fixed-point recovery.

```text
A requires B
B requires C
C requires A

No member has Complete Basis.
    ↓
No member may establish from that cycle.
```

The semantic prohibition belongs to Contract law. Cycle detection, scheduling, query dependencies, and recovery
mechanics
remain compiler realization.

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

Section 4.7.2 owns the entry, non-entry, unsuccessful-result, and indeterminate-outcome law. ADR-0057 remains the owner
of
Contract Failure.

---

## 9.3. Occurrence-Time Integrity

Sections 7.6 and 7.7 own occurrence attribution and the rule that later context cannot rewrite an already-established
occurrence.

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

## 10.1. Role and Authority Boundary

The Canonical Contract World is the compiler substrate through which complete Established Definition Material becomes
legally observable to later compiler consumers. It preserves authority-owned meaning; it does not establish that meaning
again and it does not receive, replace, or aggregate the source Contract authorities that established it.

Making an Established Definition visible through the World therefore creates no new Contract identity, no transfer of
source authority, and no generic `World Membership` meaning. If Policy, Governance, Composition, or another Contract law
owns a semantic membership or binding relation, that relation must already have been established by that law.

The Policy-owned Contract World defined by Policy law is Contract meaning. The Canonical Contract World in this ADR is a
compiler substrate for observing already-established authority-owned Definition meaning. Physical co-location in the
Canonical
Contract World cannot create Policy membership or another Contract relation.

---

## 10.2. Resolved Contract HIR Boundary

Sections 1 and 4.7.1 own the HIR-to-Establishment boundary. The Canonical Contract World introduces no alternate path
for
reconstructing authoritative meaning from HIR representation and cannot upgrade HIR Candidate References into authority.

---

## 10.3. Definition Handoff and Authoritative Closure

Only a complete Established Definition may enter the Canonical Contract World visibility boundary. The Definition
Meaning, complete authoritative identity from Section 6, exact Definition Reference, and every direct established
relation owned by that Definition meaning must already be coherent.

```text
Authority-Owned Establishment
    ↓
complete Established Definition D
    ↓ compiler visibility handoff
Canonical Contract World
```

A relation required by the Definition meaning cannot remain unresolved as a normal World state. If such a relation is
required before Definition Establishment, unresolved material prevents successful Establishment and therefore prevents
that Definition handoff.

This rule does not require future application material to exist. A Definition may preserve a Basis Requirement Law or an
Applicability Law for later semantic applications while no occurrence-specific Required Basis, Basis Binding,
Applicable Context, Applicability result, or Complete Basis yet exists. Absence of a future application is not an
unresolved Definition relation.

The physical implementation may form the backing in several steps. Those intermediate steps do not become a partially
Established Definition or a partially authoritative World observation.

---

## 10.4. Definition Preservation and Exact Definition Surface

The Canonical Contract World preserves each Established Definition independently of the physical or aggregate identity
of the World representation. A change to one independently established Definition does not by itself rewrite the
Definition Meaning, Definition identity, Owning Authority Binding, Version Binding, or Definition Reference of an
unchanged sibling Definition.

For each visible Established Definition, the World preserves the material contract from Section 4.3.1. Its direct
relation surface distinguishes only semantic states that the owning Definition law actually defines:

```text
relation not owned
    ≠
owned and validly absent
    ≠
owned and present
```

A Definition-owned required relation that is unresolved is not a fourth successful World state; Section 10.3 prevents
that Definition from crossing the visibility boundary.

Physical linking, packing, relocation, deduplication, indexing, or re-visibility may change compiler representation
without changing an unchanged Definition Reference or its source-owned meaning.

---

## 10.5. Occurrence Separation

Section 4.4 remains the owner of Established Occurrence Material. Occurrence-specific material is not automatically
inserted into the Canonical Contract World merely because diagnostics, execution, or another compiler consumer may later
need to observe it.

```text
Canonical Contract World
    → Established Definition Material substrate

Established Occurrence Material
    → separate authority-owned semantic family
      when an owning Contract law defines occurrence meaning
```

A compiler evaluation, query result, runtime call, diagnostic observation, or World lookup cannot create a Contract
Occurrence. Item-specific storage and the later Established Semantic Protocol may provide legal observation of
occurrence
material without turning the Canonical Contract World into a universal semantic-event container.

---

## 10.6. Composition, Linking, and World Visibility

Coexistence inside one Canonical Contract World does not establish a semantic relation between Definitions. Physical
linking likewise cannot create Basis Binding, Composition, Policy membership, Whole-Machine meaning, or another
Contract-owned connection.

```text
Established Material A
+
Established Material B
    ↓ owning Composition law
Established composed meaning or relation
    ↓
Canonical Contract World visibility when that result is Definition meaning
```

The direction does not reverse. A higher-scope law may establish new meaning from already-established sources; World
assembly only preserves the resulting meaning after that establishment succeeds.

A compiler may call a Definition visible in one logical World generation, partition, segment, or product. Such
visibility
coordinates are compiler lifecycle material, not Contract identity or semantic membership.

---

## 10.7. Coherent Visibility and Consumer Closure

The Canonical Contract World does not require one monolithic project-wide object or one physical all-or-nothing build.
Independent complete Established Definitions may become available when the semantic closure required by a legal consumer
is complete and coherent.

```text
partial World coverage
    → may be legal

partial Established Definition meaning
    → illegal
```

A consumer-visible observation must not mix incompatible or partially formed authoritative state. Every exact
Definition Reference and every authoritative direct relation observed together must resolve within one valid logical
World view or through an explicitly validated equivalent projection.

Physical realization may use immutable generations, segmented tables, persistent chunks, mapped pages, copy-on-write,
lazy materialization, shared backing, or another scheme. Old and new physical backing may coexist, and unchanged backing
may be reused across compiler generations, when the current logical World observation is validated as coherent.

A compiler World generation, visibility epoch, segment identifier, manifest, dense handle, or backing address is not
Contract identity. Reusing one of those mechanisms does not inherit Contract authority from an earlier compiler run.

Consumer completeness is consumer-specific. One unavailable Definition need not erase independent Established
Definitions from all legal observation, while a consumer whose required semantic closure includes that unavailable
Definition cannot enter merely because other World material is visible. This ADR therefore creates no universal
`CompleteCanonicalWorld` Contract entity.

---

## 10.8. World Formation Failure and Durable History Boundary

Canonical World formation and visibility are compiler responsibilities. If compiler machinery cannot form or expose a
coherent World observation after a Definition has already been established, the unsuccessful compiler result is governed
by ADR-0074. It does not become Contract Failure and does not revoke the already-established Definition in the current
compilation.

A dependent compiler judgment that cannot obtain its required World material is not given a synthetic unsuccessful
Contract result merely because the visibility machinery failed. Section 4.7.2 owns the common non-entry law.

Section 6.9 and ADR-0053 remain the owners of durable Version-history publication. Current-compilation Establishment,
Canonical World visibility, and durable project history are distinct boundaries. A failed compilation may therefore
contain an already-established current Definition without creating a new durable Version record or History Revision.

A later compiler invocation cannot acquire authority merely from a failed run's World generation, persistent segment,
cache entry, or retained backing. Such material is at most reuse evidence until current explicit semantic inputs and
valid
retained Contract history establish the current meaning again.

World reclamation likewise changes availability, not past semantic truth. Removing an old World generation or its
backing
does not retroactively undo an Establishment that occurred while that material was validly established.

---

## 10.9. World Boundary

The Canonical Contract World contains already-established Definition meaning and only the direct established relations
owned by that meaning. Source provenance remains an adjacent relation under Section 4.3.1 rather than Definition
identity.

The World does not absorb downstream analysis, verification, optimization, diagnostics, query state, cache state,
coverage state, backend state, execution state, or consumer-specific summaries. Those are later compiler products even
when they physically share backing or indexes with World material.

Section 11 owns the replaceable representation boundary. `Canonical` in `Canonical Contract World` does not require one
canonical byte encoding, sort order, hash layout, object graph, table schema, or storage format.

---

# 11. Contract and Representation Boundary

## 11.1. Semantic Separation

Compiler realization must preserve the semantic distinctions and exact relations defined by Sections 4 through 10. This
section does not restate that catalog. Those distinctions are logical and do not prescribe object, allocation, table,
compiler-product, or pointer topology.

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

Section 4.2.2 decides when a semantic meaning constitutes separate Established Material. Whether that material receives
a
separate compiler product or physical storage unit remains a realization decision.

A compiler may split or fuse physical products according to independent consumption, invalidation, reuse, lifetime,
publication, or materialization needs, provided the semantic boundaries remain recoverable. Contract decomposition does
not determine compiler product granularity.

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

The coordinate-family boundary is owned by Section 6.6. Realization may encode, index, compare, cache, duplicate,
transport, split, or fuse Contract material, but it may not establish, revoke, merge, or rewrite Contract authority. A
backend that cannot preserve an established law is not a valid realization of that law.

---

# 12. Relation to Existing Authorities

This ADR supplies the common Establishment relations. Section 4.8 preserves source ownership, Section 8.8 owns common
composition authority, and each 1D ADR remains the owner of its local candidate meaning, judgment, established result,
and authority-specific bindings.

---

# 13. Governance and Diagnostic Consequences

## 13.1. Governance

Governance keeps the semantics defined by ADR-0056. When it consumes another authority's Established Material, the
common
Basis and Applicability laws in Sections 8 and 7 apply without transferring source authority.

---

## 13.2. Diagnostic

Establishment does not create a diagnostic cause chain, compiler trace, or root-cause ordering. A Diagnostic explains an
existing authority-owned result through its exact semantic references; when it explains a compiler-side unsuccessful
judgment, it observes that result through the ADR-0074 Compiler Result Protocol. Contract Failure remains governed by
ADR-0057.

Diagnostic processing may combine those result observations with separately owned provenance and compiler evidence.
Source selection, causal expansion, conflict reduction, rendering, and engineering traces belong to Diagnostic or other
compiler products and cannot strengthen or rewrite Establishment meaning. Retention controls whether such evidence
remains
available; it does not recreate authority.

---

# 14. Whole-Machine Consequences

Section 8.9 owns the Whole-Machine composition rule. Physical linking alone establishes neither the cross-unit semantic
connection nor any larger Whole-Machine meaning.

---

# 15. Semantic Validity Requirements

The compiler must reject any state that cannot satisfy the exact law owned by the preceding sections. HIR admission and
legal candidate observation are governed by Sections 4.7.1 and 4.7.2; identity and Version exactness by Section 6;
Basis,
Applicability, singularity, and semantic prerequisite cycles by Sections 7 and 8; occurrence attribution by Sections 7.6
through 9.3; and composition by Section 8.8.

A violation is attributed to the judgment that owns the violated requirement. It does not become a generic
`Invalid Establishment` result, and compiler identity, lookup, cache state, reachability, physical representation, or an
implicit default cannot repair missing semantic meaning. Where an owning law permits absence or another cardinality,
that
law remains the validity authority.

---

# 16. Compiler Realization Obligation

Section 11 owns the realization boundary. A conforming compiler must make every semantic relation required by this ADR
exactly recoverable without forcing a consumer to reconstruct Contract meaning from implementation topology. Verified
projections may avoid repeated semantic resolution when they preserve the same owning meaning. Appendix A illustrates
one
non-normative V1 realization.

---

# 17. Evolution Boundary

Future compiler generations may replace identity encoding, storage, publication, dependency tracking, reuse,
incremental algorithms, serialization, product granularity, and physical split or fusion without changing this ADR when
they preserve the semantic laws defined above. Section 6 owns authoritative identity, Section 11 owns representation
freedom, and ADR-0053 keeps Contract Version distinct from compiler or representation evolution.

This boundary leaves V2 incremental architecture and later performance work open without redefining Establishment
semantics.

---

# 18. Rejected Directions

## 18.1. Universal Semantic Containers

A universal `EstablishedMaterial`, Reference, Applicable Context, or lifetime schema is rejected because Sections 4.2.2,
6, and 7 leave semantic coordinates with their owning laws. A common physical representation does not change that
result.

## 18.2. Representation as Contract Authority

Using host objects, source containment, compiler handles, table position, hashes, or completion order as Contract
identity
is rejected by Sections 6.6 and 11.6.

## 18.3. Implicit Semantic Reconstruction

Reconstructing authority from generated artifacts, reflection, KSP shape, unresolved Version claims, implementation
containment, or another non-owning source is rejected by Sections 4.7.1 and 6.3.

## 18.4. Derived Compiler Work as Contract Authority

Query dependencies, build edges, cache state, verification products, optimization proofs, and specialized execution
forms
remain compiler-owned under Sections 8.10 and 10.5. They cannot establish Contract dependency or authority.

## 18.5. Version Conflation

Contract Version cannot be replaced by compiler schema, compiler generation, backend artifact version, or compatibility
judgment. ADR-0053 and Section 6.3 own this distinction.

## 18.6. Semantic Decomposition as Mandatory Physical Topology

Mapping every semantic distinction to a separate object, table, allocation, compiler product, or repeated reference hop
is rejected by Section 11. The first implementation layout likewise acquires no semantic permanence.

---

# 19. Consequences

The model requires the compiler to preserve exact semantic identity, Basis, Applicability, composition, and occurrence
attribution rather than recovering them from implementation topology. That increases semantic bookkeeping, but Section
11
allows those relations to be packed, fused, projected, summarized, or addressed through dense compiler structures when
the authoritative meaning remains unchanged.

The resulting separation supports precise diagnostics, reuse, invalidation, reference checking, and optimized execution
without making those compiler mechanisms Contract authority.

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

2026-09-19
    → aligned Establishment entry and unsuccessful-result boundaries with ADR-0057 and ADR-0074
    → defined the pre-authority Candidate Reference to authoritative Definition Reference transition
    → separated current semantic Establishment from durable Version-history publication
    → removed later restatements where an earlier section already owns the same law
    → separated reusable Basis Requirement Law from exact judgment-owned Required Basis instances
    → refined Basis Binding as exact source connection and closed Applicability / Complete Basis entry and closure laws
    → defined Canonical Contract World handoff as compiler visibility rather than authority transfer
    → restricted the World substrate to complete Established Definition meaning and Definition-owned direct relations
    → kept Established Occurrence Material outside the default Definition World and required coherent logical visibility
    → separated World formation failure, compiler generation, and durable Version-history publication from Contract authority
```

---

# Appendix A. Non-Normative Kontrakt V1 Implementation Reference

## A.1. Status

**This appendix is implementation reference only. It is not Contract law.** Sections 1–20 remain normative. The examples
below give one plausible V1 realization without fixing Kotlin class shape, JVM object topology, table or slab count, IR
operation names, HID encoding, dense-handle width, query API, serialization schema, or physical split / fuse choice.

---

## A.2. Reading Form

Each example identifies a producer, the material it consumes or exposes, one human-readable dump where useful, a
possible
V1 backing, and downstream consumers. These labels describe compiler dataflow and storage candidates only. They do not
create semantic ownership, reference identity, or authority beyond the normative section cited by the example.

A table row, slab offset, dense handle, HID, or query key shown below is therefore physical realization unless the body
of
this ADR explicitly gives the represented relation semantic meaning. Later appendix sections rely on this rule instead
of
repeating the same authority disclaimer.

---

## A.3. End-to-End Material Map

```text
Contract Source
    ↓
Source / Syntax / Provenance
    ↓
Resolved Contract HIR
    ↓
Resolved HIR Candidate Protocol
    ↓
Authority-Owned Establishment
    ↓
Established Definition Material
    ↓ compiler visibility handoff
Canonical Contract World
    ├── Generated API Product
    ├── Reference Judgment
    ├── PBT / Fixture / Coverage
    ├── Diagnostics / Evidence
    └── Contract-Aware consumers

Authority-Owned semantic application
    ↓
Established Occurrence Material
    → separate occurrence semantic family

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

Basis Requirement Law
    target
        → exact Fact kind Balance
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
Source / Recovery Material
Visible Resolved Contract HIR
```

These materials may be produced by the same frontend, but they do not share one semantic surface. Recovery material does
not become Visible Resolved HIR, and source provenance remains a separate relation from HIR candidate meaning.

### Produced by

```text
.kontrakt
    ↓
Source Manager / Parser / Recovery
    ↓
Resolution
    ├── valid resolved meaning → Visible Resolved Contract HIR
    └── invalid / recovery material → compiler result and recovery surfaces
```

### Legal observation

The legal observation boundary is Section 4.7.1 and ADR-0071. The following producer dump illustrates backing that may
realize that boundary; it is not itself the consumer contract.

---

### Example producer-internal dump

The following dump illustrates one possible HIR producer representation. `%d17` is an internal handle in this example;
it is not the Protocol-level Candidate Reference and is not observable semantic identity.

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

basisRequirementLaw
    target
        → exact Fact kind Balance
        → exactly one

applicabilityInputs
    → State
```

The source relation is represented separately even if a physical implementation stores its compact handle beside the HIR
row.

```text
HIR provenance relation
    internal HIR handle %d17
        → src#91
```

### Possible V1 backing

```text
ResolvedDefinitionHIR

kind[]
authorityRef[]
versionClaimRef[]
localDefinitionRef[]

basisRequirementLawBase[]
basisRequirementLawCount[]

applicabilityInputBase[]
applicabilityInputCount[]

HIRProvenanceRelation
    hirHandle[]
    provenanceHandle[]

FrontendRecoveryState
    sourceUnitHandle[]
    recoveryTag[]
```

Example:

```text
ResolvedDefinitionHIR[17]

kind                     = INVARIANT
authorityRef             = 8
versionClaimRef          = 3
localDefinitionRef       = 12
basisRequirementLawBase   = 40
basisRequirementLawCount  = 1
applicabilityInputBase   = 70
applicabilityInputCount  = 1

HIRProvenanceRelation
    hirHandle             = 17
    provenanceHandle      = 91
```

`FrontendRecoveryState` is construction or recovery material. It is not part of a legal Visible Resolved HIR Candidate
Protocol observation. The arrays above are replaceable physical storage and do not define the Protocol surface.

### Consumed by

Producer-internal verification may use the physical backing directly. Establishment, diagnostics, and tooling consume
the
legal semantic surfaces assigned to them by Section 4.7.1, ADR-0071, and ADR-0074.

---

## A.6. Establishment Observation and Established Definition Output

### Material

```text
Resolved HIR Candidate Protocol observation
Established Definition Material
```

There is no required semantic `EstablishmentInput` DTO between these materials. A physical implementation may stage the
observations needed for one judgment, but that staging material does not become another semantic layer.

### Produced by

Formation follows Sections 4.7 and 6. The example below shows one judgment episode; it does not introduce a semantic
`EstablishmentInput` product.

### Example observation

```text
Definition Candidate Reference
    → exact current HIR candidate for PositiveBalance

Definition Candidate Projection
    → Invariant candidate meaning for PositiveBalance
    → authority-scoped version candidate V3
    → Basis Requirement Law: exactly one exact Fact kind Balance
    → Applicability law uses State
```

If the owning judgment needs only a legal subset of this meaning, the HIR producer may expose a Fine-Grained Semantic
Projection instead. If the judgment needs the IDL selection relation, it observes the separate IDL Binding Candidate
Projection. Neither observation imports generic source provenance into Definition Candidate meaning.

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

basisRequirementLaw
    target
        → basisRequirementLaw#30

directRelations
    → relationRange[120..122)
```

Source provenance remains a separate relation to the established semantic material. In this example it may still relate
`established.definition @def:42` to `src#91`. A physical table may carry that compact provenance handle for locality,
but
the fusion does not make provenance part of Definition Meaning or Definition Reference.

### Possible V1 backing

```text
DefinitionTable

authorityHandle[]
versionHandle[]
localDefinitionCoordinate[]
meaningKind[]
meaningPayloadHandle[]
basisRequirementLawBase[]
basisRequirementLawCount[]
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
basisRequirementLawBase   = 30
basisRequirementLawCount  = 1
relationBase              = 120
relationCount             = 2
provenanceHandle          = 91
```

Here `provenanceHandle` physically realizes the separate provenance relation described by Section 4.3.1. The row may be
filled in several physical steps, but it is not exposed as an Established Definition until the identity closure required
by Section 6.4 is coherent.

### Consumed by

```text
Canonical Contract World visibility
Reference Judgment
PBT planning
diagnostics
Contract-Aware Analysis
Execution Formation
```

The consumer list is illustrative; Sections 4.2.2 and 11.3 decide semantic and physical product boundaries.

---

## A.7. Canonical Contract World, Stable Keys, and Dense Handles

### Material

```text
Canonical Contract World logical visibility generation G12
```

`G12` is a compiler visibility and lifetime coordinate. It is not part of Definition identity.

### Produced by

```text
complete Established Definition Material
    ↓
World formation / exact-reference validation
    ↓
coherent seal / freeze
    ↓
make logical World generation G12 visible
```

The physical formation may be segmented or incremental. No incomplete Definition row or incoherent direct relation set
is exposed merely because some backing has already been written.

### Example world view

```text
CanonicalContractWorld G12

authorities
    → AuthorityTable

versions
    → VersionTable

definitions
    → DefinitionTable

relations
    → DefinitionRelationSlab

stableLookup
    → StableKeyIndex

provenance
    → ProvenanceStore
```

The Definition table may point to reusable Basis Requirement Law material because that law is Definition meaning. Exact
judgment-owned Required Basis instances, Basis Bindings, Applicability material, Complete Basis results, and Established
Occurrences remain with their owning semantic families rather than becoming default Definition-World contents.

### Identity use

```text
Compiler Stable Lookup Key
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

Section 6.6 governs both compiler stable keys and generation-local handles; neither is Definition identity.

---

## A.8. Required Basis and Basis Binding Material

### Contract view

The running Definition establishes this requirement law as part of its meaning.

```text
Basis Requirement Law

target
    → exact Fact kind Balance
    → exactly one
```

One exact dependent judgment instantiates its own Required Basis from that law.

```text
Required Basis

owner
    → occurrence judgment @j17

requirement
    → target
```

Later, the binding-owning law may establish an exact source connection.

```text
Basis Binding

target
    → Balance Fact F42
```

The Binding is not yet an Applicability result.

### Possible V1 backing

One physical realization may keep reusable requirement law and exact judgment-owned requirement material separately.

```text
BasisRequirementLawSlab

definitionHandle[]
requirementCoordinate[]
requiredMeaningKind[]
requiredMeaningPayload[]
shapeKind[]
shapePayload[]
```

```text
BasisRequirementLawSlab[30]

definitionHandle         = 42
requirementCoordinate    = TARGET
requiredMeaningKind      = FACT
requiredMeaningPayload   = Balance
shapeKind                = EXACTLY_ONE
shapePayload             = 0
```

```text
RequiredBasisSlab

ownerJudgmentKind[]
ownerJudgmentHandle[]
requirementLawHandle[]
```

```text
RequiredBasisSlab[80]

ownerJudgmentKind    = OCCURRENCE_JUDGMENT
ownerJudgmentHandle  = 17
requirementLawHandle = 30
```

The exact semantic requirement coordinate remains recoverable through the referenced law. A fused representation may
store that coordinate directly instead.

An exact source connection may use a separate relation slab.

```text
BasisBindingSlab

requiredBasisHandle[]
sourceKind[]
sourceHandle[]
applicationHandle[]
```

```text
BasisBindingSlab[100]

requiredBasisHandle = 80
sourceKind           = FACT
sourceHandle         = 205
applicationHandle    = 17
```

Here `applicationHandle` is a denormalized access aid because `RequiredBasisSlab[80]` already determines the owning
judgment. It does not become an independent semantic determinant and must agree with the exact Required Basis when
stored.

The compiler may keep source-domain indexes or closure summaries beside these slabs. They are evidence used to establish
or validate the Binding and completeness law, not Contract Basis material merely because they accelerate the search.

### Consumed by

```text
Applicability evaluation
Complete Basis check
Occurrence establishment
diagnostics
Reference Judgment
Execution Formation when runtime-relevant
```

These slabs may later be fused when the semantic distinctions remain exactly recoverable.

---

## A.9. Applicability, Applicable Context, and Complete Basis

### Produced by

```text
Basis Binding
+
Dependent Application
+
complete Applicable Context required by the law
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
    → occurrence judgment @j17

context
    State
        → Active
```

The context range is legal only when it contains every meaning-determining coordinate required by that law and no
compiler-only ambient state.

### Example transient result

```text
applicability.result

basisBinding
    → basisBinding#100

application
    → occurrence judgment @j17

result
    → Applicable
```

This transient representation does not imply that the source material gains a permanent applicability field.

### Possible V1 backing for sparse complete context

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
Complete Basis for occurrence judgment @j17

target
    → basisBinding#100
    → Applicable

Completeness Law
    → exactly one target

result
    → Complete
```

A separate permanent object for Complete Basis is not required. V1 may establish completeness from exact requirement and
Applicable Basis ranges plus the owning completeness law. For an exactly-one requirement, the compiler must also have
sufficient
closure evidence to know that no second legal Applicable Binding exists. That evidence may be an index, count, summary,
or direct-domain proof and remains compiler material unless the owning law independently makes the candidate set
semantic
Basis.

Incomplete or unavailable prerequisites do not produce a synthetic result for the dependent occurrence judgment. That
judgment is simply not entered until its Required Basis is complete.

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

Contract Diagnostic Evidence and a Compiler Diagnostic Record may coexist. The example below explains an established
semantic occurrence rather than an ADR-0074 compiler-side unsuccessful result. A diagnostic that explains such a
compiler
result instead observes the owning Compiler Result Protocol as required by Section 13.2.

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
    failure.establish %j0

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

These examples realize the semantics owned by ADR-0057, ADR-0058, and ADR-0059; Appendix A.2 applies throughout.

### Failure projection

```text
FailureProjection

failure
    → Established Failure F17

source
    → PositiveBalance judgment at occurrence @occ:17

failureMeaning
    → required PositiveBalance meaning not satisfied

applicableContext
    → contextRange[55..56)

boundary
    → active boundary B7

executionTarget
    → block 7
```

```text
FailureProjectionTable

failureHandle[]
sourceKind[]
sourceHandle[]
contextBase[]
contextCount[]
boundaryHandle[]
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

The hot table above is one realization of the pre-resolved projection freedom defined by Section 11.4.

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
    BasisRequirementLawSlab
    DefinitionRelationSlab
    StableKeyIndex

Occurrence / semantic-use material
    OccurrenceTable
    RequiredBasisSlab
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
BasisRequirementLawSlab

requirementCoordinate[]
requiredMeaningKind[]
shapeKind[]
```

```text
RequiredBasisSlab

ownerJudgmentHandle[]
requirementLawHandle[]
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

ownerJudgmentHandle[]
requirementCoordinate[]
requiredMeaningKind[]
shapeKind[]
sourceHandle[]
applicationHandle[]
applicabilityTag[]
```

Both may realize the same semantics when the fused form preserves the distinction between reusable requirement law,
exact judgment-owned Required Basis, source Binding, and use-local Applicability. Repeated application coordinates may
serve lookup locality but cannot create an additional semantic determinant.

---

## A.26. Query / Product View and Frozen Publication

### Query / product view

```text
resolvedContractHir(sourceUnit)
    → Resolved Contract HIR

establishedDefinition(definitionRef)
    → visible Definition handle in one valid logical World view

basisRequirementLaw(definitionHandle)
    → Basis Requirement Law range

requiredBasis(judgmentHandle)
    → exact Required Basis range

referenceJudgment(subject, basis, context)
    → Reference Result

verifyRealization(operationDefinition, realizationBinding)
    → Verification Overlay

wholeMachineSummary(machine)
    → Whole-Machine Summary

formExecution(interactionDefinition, admittedBinding, target)
    → Execution IR generation
```

Query dependency semantics are governed by Section 8.10.

### Frozen visibility

```text
worker-local / private builders
    ↓
compiler validation of the World representation
exact-reference closure for the visible projection
collision verification where required
deterministic merge
    ↓
seal / freeze
    ↓
Visible logical World generation G12
    ↓
read-only consumers
```

This compiler visibility boundary adds no Contract authority. Section 10.7 governs coherent logical observation and
Section 6.6 governs the distinction between compiler generation and Contract identity.

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

Resolved HIR Candidate Protocol
    → A.5 / A.6

Authority-Owned Establishment
    → A.6

Established Definition Material
    → A.6

Canonical Contract World
    → A.7

coherent World visibility
    → A.7 / A.26


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

generation visibility
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

Use an Appendix A shape only after identifying the normative owner of the represented meaning. Physical split, fusion,
dense addressing, summaries, query boundaries, materialization strategy, and hot/cold placement remain compiler-design
choices under Sections 6.6 and 11. A representation-only change does not require ADR-0063 to change; a change to a
normative semantic relation does.