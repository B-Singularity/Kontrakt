# ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Status

Accepted

## Date

2026-07-12

## Related

- `docs/what-contract-is.md`
- ADR-0049: Flow Contract Processing — Fact Acceptance and Publication
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure

---

## 1. Context

ADR-0046 fixes the first frontend boundary.

The interface remains the user-facing contract surface. The operation remains the handle through which the machine is
selected. A generated Kotlin or Java interface may make that handle convenient to call, but generated host machinery
does not own contract meaning.

ADR-0047 fixes the selection boundary after that.

The operation manifest selects one-dimensional roles through explicit slots. A source does not declare itself to be
`Input`, `Admission`, `Canonicalization`, `Lowering`, or `Fact`. The operation supplies the boundary, and the slot
supplies the role.

A single `.kontrakt` interface scope may declare a closed set of operations that enter the same machine. `Policy`,
`Governance`, `Budget`, and `Capacity` are bound once at that enclosing scope because they coordinate decisions and
finite resources shared among those operations. They are not repeated as operation-manifest slots. Each operation
manifest continues to declare its own flow, failure, movement, version, and diagnostics.

The remaining problem is processing.

The four contract roles addressed here form the inbound airlock of the machine. `Input` establishes judgeable immutable
boundary presentation. `Admission` decides whether that presentation may continue. `Canonicalization` optionally
produces its stable representative under a selected equivalence law. `Lowering` removes external-presentation authority
and forms the explicit immutable Fact material that may enter the core.

The core is not another application layer and is not a graph of domain objects. It is the isolated space of the machine
in which untrusted external representation no longer exists. Everything the core may know must be present as explicit
immutable information. That information is Fact material. A host class, record, object, field, getter, repository,
callback, or runtime reference may carry or produce information, but none becomes the hidden place from which the core
discovers what is true.

These contracts do not form one self-executing pipeline. The same selected flow advances on three parallel axes:

```text
contract pipeline
implementation pipeline
state pipeline
```

The contract pipeline defines authority and judgment. The implementation pipeline realizes ratified material and
performs
work behind declared boundaries. The state pipeline declares when each judgment, realization, refusal, seal, and handoff
is legally available. JVM objects, interfaces, callbacks, and intermediate allocations are frontend and implementation
mechanisms; they are not the authority model of the machine. Slot-selected Lowering is not an opaque user implementation
region: it is immutable frontend source that Kontrakt must refine, erase, and realize itself.

Input and Admission are a direct contract adjacency in this ADR. Input establishes the immutable presentation that
Admission judges, so no user implementation transformation exists between them. Input enters the declared external
boundary, not the core. Kontrakt neither owns the surrounding user system nor observes or classifies processing that may
have occurred before the Input presentation was formed; the submitted presentation is the raw Input material of this
flow.

The `canonical` slot is optional in V1. When it is selected, Admission and Canonicalization have no user-supplied
transformation region: a Kontrakt-owned realization applies the selected Canonicalization law to the same
contract-visible presentation and produces its stable representative. When it is omitted, Kontrakt inserts no semantic
Canonicalization Contract, no `ExactCanonicalization`, and no user callback, proxy, or interceptor. The admitted Input
presentation passes unchanged to the selected Lowering formation.

In both branches, Lowering is declared through immutable one-to-one coordinate bindings. The target coordinates belong
to the Fact Contract selected for the same flow. The Kontrakt compiler resolves the permitted meaning-preserving type
refinements, derives one semantic Lowering plan, and a backend generates the automatic realization. No user mapper,
callback, proxy, independently authored lowering implementation, or mandatory Kontrakt-specific scalar wrapper exists
between the external presentation and the core Fact boundary.

This ADR defines the processing profiles for those four contracts, their relationship to the implementation and state
axes, and the handoff from untrusted external presentation to explicit immutable core information.

ADR-0049 defines Fact, Invariant, and Publication. Fact is not merely the result of an implementation operation and is
not a candidate waiting for Invariant to make it true. Fact is the immutable information available to the explicit core
machine. Core realization may consume boundary Facts together with existing Facts and other explicit contract material
to produce declared immutable Operation Result Material. When the selected operation binds a Fact Contract for that
result, the same material enters the Fact Candidate path and may receive Fact authority only after applicable Invariant
judgment and legal State Transition. Publication governs the outward claim.

The manifest labels `failure`, `movement`, `bounds`, and `diagnostics` exist only in authored source layout and
disappear before contract resolution. The individual contracts selected by the slots written beneath those labels retain
their own contract kinds and independent authority. This ADR names relevant interaction points but does not define those
contracts' complete processing profiles.

## 2. Problem

Outside material arrives through host-language and framework contracts that Kontrakt does not own.

Those contracts may be useful and explicit, but their implementation mechanics must not become Kontrakt authority. At
the same time, forcing users to restate every host contract in a second Kontrakt vocabulary would make the frontend
needlessly hostile and would ignore the practical limits of Kotlin and the JVM.

JVM languages also encourage machines to hide information behind object identity, mutable fields, getter behavior,
service calls, framework lifecycle, and callback completion. A core built from those surfaces must discover what it
knows
by executing implementation. That is the implicit machine Kontrakt rejects.

Kontrakt therefore needs processing that can:

- accept ordinary external contract evidence;
- require explicitly formed immutable Input presentation before Admission;
- refine external declaration meaning into explicit Kontrakt-owned obligation material;
- reject unratifiable sources before runtime;
- refuse malformed or unavailable invocation material before Admission;
- preserve three parallel axes for contract authority, implementation work, and legal state movement;
- stop material at the earliest contract that already owns enough authority to stop it;
- avoid repeating acquisition, planning, identity, and image work already performed by the existing machine;
- treat the submitted presentation as the flow's raw Input without inferring or governing any prior external value
  history;
- permit omission of semantic Canonicalization without inserting an implicit representative contract or runtime hook;
- preserve a strict boundary between boundary formation, continuation judgment, optional same-shape representative
  production, immutable Lowering formation, core Fact availability, Invariant judgment, state movement, Operation Result
  Material production, and Publication;
- permit Lowering authors to declare only finite immutable one-to-one bindings from selected Input coordinates to the
  coordinates of the selected Fact Contract, with no same-name inference, mapper function, callback, DI surface, state
  lookup, business algorithm, or executable implementation;
- allow ordinary primitive and closed immutable host types to nominate Fact coordinates when they already express the
  required information, without manufacturing Kontrakt-specific core wrappers or a rich object model;
- derive the required meaning-preserving type refinement, generated mapping, Fact sealing, verification, generated
  tests, diagnostic attribution, cache dependency, and backend optimization from that one ratified Lowering declaration
  instead of requiring duplicate handwritten boilerplate; and
- optimize Kontrakt-owned verification, canonical representative production, Lowering realization, testing, planning,
  state enforcement, diagnostics, caching, and physical layout without rewriting unrelated user-supplied implementation
  code in V1.

The result must be usable as software and still satisfy the discipline of *What Contract Is*.

## 3. Decision Drivers

The machine must reject as early as possible without moving a later contract's judgment into an earlier role.

A source declaration may nominate external contract evidence, but only ratified Kontrakt-owned material may receive
contract authority.

A user must not be required to declare the same contract twice. A supported host contract must be refined by one
deterministic frontend law or rejected.

The core must not read external DTOs, mutable objects, lazy values, proxies, framework contexts, repositories,
callbacks,
or implementation-owned object graphs in order to discover information. Everything available to the core must already
exist as explicit immutable Fact material.

Policy, Governance, Budget, and Capacity must remain machine-scope contracts when they arbitrate finite resources shared
by several operation pipelines. Their declarations may state the total machine walls and explicit operation allocations
or grants, but they must not bind contract authority to internal implementation functions, stages, or call-graph shape.
The enclosing interface scope binds those four contracts once; an operation manifest does not repeat them.

Fact is information itself. It is not necessarily an implementation result, a domain event, a persistence row, an
entity, or a Value Object. A record-like host declaration may nominate Fact coordinates, but the declaration and its
object instances do not own factual meaning.

Input presentation must be explicit and immutable before it reaches Admission. Runtime snapshot timing, lazy
materialization, proxy activation, and framework lifecycle must not become hidden Input meaning. Lowering must likewise
be declared only through immutable finite material. The author explicitly binds each selected Fact coordinate to exactly
one selected Input coordinate, even when their names happen to match, while Kontrakt resolves the unique supported
type-refinement profile and generates the mapping realization.

The selected source and target types may be identical. Lowering does not require a new target type, a new object, or a
Kontrakt-specific scalar wrapper. A type change is justified only when it preserves already-declared meaning while
removing external representation authority and forming the representation required by the Fact surface.

No user mapper implementation may become the authority that defines Fact formation. The generated mapping must stop at
sealed immutable Fact material and must not execute core computation, derive a Result, judge an Invariant, perform a
State Transition, or publish an outward presentation.

Input refusal, Admission rejection, selected Canonicalization refusal, Budget or Capacity stop during selected canonical
production, and Lowering refusal must remain distinguishable because they report different failed obligations. A flow
that omits Canonicalization has no Canonicalization refusal surface.

The contract, implementation, and state pipelines must remain separate. Completion of implementation work does not grant
contract authority and does not by itself perform a legal state transition.

Existing frozen acquisition, planning, graph lowering, identity derivation, and ContractImage publication must be
reused. These four contract roles must not create parallel metamodel or planning machines.

A backend may fuse or specialize Kontrakt-owned verification and state machinery, but fusion must not merge declared
contract authority, factual meaning, or state meaning.

V1 must not optimize, rewrite, fuse, devirtualize, or remove allocations inside unrelated user-supplied implementation
code. Such implementation remains an opaque realization region whose legal inputs, visible Facts, possible declared
Operation Result Material surfaces,
failure surface, and state participation are governed by explicit contracts. Slot-selected Admission,
Canonicalization, and Lowering declarations are different: they are restricted immutable frontend source evidence that
Kontrakt must either refine completely and erase or reject before ContractImage publication. Kontrakt owns the generated
evaluators, canonical producers, and Lowering realizations derived from that material.

Kontrakt-owned validation, deterministic planning, automatic mapping generation, automatic test generation and
execution, state enforcement, diagnostics, cache planning, frozen material, and generated gates remain optimization
targets and must be implemented to a state-of-the-art performance standard. A rich contract declaration must remove
boilerplate and expose enough whole-pipeline knowledge for mechanically sympathetic specialization, pass fusion,
allocation control, structural reuse, and deterministic caching. Any optimization that changes declared meaning or
weakens determinism is invalid.

## 4. Decision

ADR-0048 defines four contract processing profiles on the contract axis, while permitting a flow to omit the
Canonicalization profile in V1. These profiles belong to each operation's inbound flow. The enclosing interface scope
may declare several operations and binds the shared Policy, Governance, Budget, and Capacity contracts once for that
closed machine scope:

```text
Input
-> Admission
-> Canonicalization, when selected
-> Lowering
```

When `canonical` is omitted, the contract-axis handoff is directly from Admission to Lowering. This ordering expresses
authority handoff. It is not a complete execution pipeline and does not imply that one contract performs the
implementation work needed to reach the next contract.

The selected flow advances on three parallel pipelines.

### 4.1. Contract Pipeline

The contract pipeline defines what material may receive authority at each boundary:

```text
explicit immutable boundary presentation
-> Input authority
-> Admission continuation judgment over the same presentation
-> optional canonical-representative authority over the same contract-visible shape
-> Lowering formation obligation
-> sealed boundary Fact material
-> legal handoff into the explicit core machine
```

Input and Admission inspect the same formed presentation. When selected, Canonicalization applies a ratified
representative law to that same contract-visible shape and produces Kontrakt-owned canonical material. When omitted,
there is no semantic representative-production boundary and the admitted presentation remains the source material for
Lowering.

Lowering declares how that boundary presentation forms the immutable Fact surface selected for the same flow. It does
not define an Operation Start object, a backend layout, a domain object graph, or a Result. The Kontrakt compiler
derives
the semantic plan and a backend generates the one-to-one mapping realization. Fact formation is complete only when the
material is sealed and external authority has been erased. The state axis governs the legal moment at which that sealed
material becomes available to the core.

A successful judgment, production, or seal grants only the declared authority. It does not retroactively turn source
syntax, generated machinery, host objects, or backend storage into contract authority.

### 4.2. Implementation Pipeline

The implementation pipeline performs the work that contracts intentionally leave open:

```text
Input -> Admission:
    no user transformation region

Admission -> Canonicalization, when selected:
    Kontrakt-owned generated realization applies the selected representative law

Admission -> Lowering realization, when Canonicalization is omitted:
    admitted Input is supplied unchanged to a Kontrakt-generated fixed formation plan

Canonicalization -> Lowering realization, when selected:
    the stable same-shape representative is supplied to the same kind of generated formation plan

Lowering:
    a generated one-to-one realization forms and seals the selected boundary Fact material

Core realization:
    consumes explicit Facts and other declared immutable contract material
    performs replaceable implementation work
    produces declared immutable Operation Result Material
```

When Canonicalization is selected, a Kontrakt backend generates its realization because the ratified representative law
fully determines its permitted meaning. That generated code remains implementation and is wrong if it disagrees with the
canonical law or its canonical byte protocol. When Canonicalization is omitted, no replacement canonical realization is
generated; the admitted Input is simply the source material of Lowering.

In both branches, the selected Lowering declaration contains immutable explicit one-to-one coordinate bindings only.
The bindings do not rely on matching names and contain no conversion implementation. The Kontrakt compiler resolves the
unique supported meaning-preserving refinement from each bound source sort to its target Fact sort, derives the fixed
`LoweringPlan`, and the selected backend generates the mapping that forms and seals the declared Fact material. This
generated mapping is boundary implementation, not contract authority and not core computation.

The internal realization that later consumes Facts may be one function, many functions, an object graph, an AOT plan, or
another replaceable implementation. Its structure is not the Fact Contract and is not defined by this ADR.

### 4.3. State Pipeline

The state pipeline runs in parallel with both other axes. It declares when Input formation, Admission judgment,
Canonicalization, Lowering formation, core-entry handoff, implementation execution, Operation Result Material
availability, Fact-authority availability, refusal, and Publication are legal.

```text
implementation completed
    != contract authority granted

contract judgment succeeded
    != legal state transition completed

state label changed
    != contract judgment succeeded
```

The exact state and transition sets are deferred to the ADRs that define the State Contract, State Transition Contract,
and Explicit State Machine Manifest. This ADR requires every contract judgment and implementation handoff to occur under
that explicit state machine; neither contract order nor callback completion may implicitly create movement.

### 4.4. Complete Flow Model

The four profiles in this ADR end at core entry. They do not define the internal implementation graph and do not require
an Operation Start DTO.

One interface scope may contain several operation pipelines. Each operation has its own external boundary flow, while
Policy, Governance, Budget, and Capacity coordinate the finite resources and decisions shared by the closed operation
set. Successful Lowering from any of those operation pipelines hands explicit immutable Fact material into the same
core. The core is not divided merely because the machine exposes several operation handles.

The inbound airlock is:

```text
Untrusted external region

Boundary Input
    -> Input judgment
    -> Admission judgment
    -> optional Canonicalization
    -> Lowering
    -> sealed boundary Fact material
    -> legal core-entry handoff
```

The explicit core machine is:

```text
Boundary Facts from the selected operation
+ existing Facts
+ machine-wide Policy, Governance, Budget, and Capacity material
+ explicit Version and State material
    -> replaceable core realization
    -> declared immutable Operation Result Material
       -> when a Fact Contract is selected for that result:
          Fact Candidate
          -> applicable Invariant judgment
          -> legal State Transition
          -> Fact authority
```

At operation start, Governance resolves the active contract world, Policy supplies the allocation and reaction law,
Capacity supplies the shared machine walls and the operation's applicable allocation, and Budget supplies the run grant.
The resulting material is fixed for that run before core work depends on it. Core realization may consume the granted
resources, but it does not define their limits, redistribute them by hidden implementation structure, or create a new
contract pipeline for each internal step.

The core does not know the external Input declaration, transport names, serializers, canonicalizer source,
Lowering declaration, source-coordinate bindings, mutable carriers, or framework lifecycle. It receives only explicit
immutable information and explicit laws.

Fact is not restricted to the output of core realization. Boundary Facts are formed by the inbound airlock. Existing
Facts may already be available in the current core world. Policy, Version, Governance, and other contract material may
also be presented explicitly where their own contracts require it. Core realization operates over that visible material.

Core realization produces declared immutable Operation Result Material. Result Material names what the operation has
produced; it is not a competing material kind beside Fact. When the selected operation binds a Fact Contract for that
result, the same material enters the Fact Candidate path. Applicable Invariant judgment may test its declared relations,
and a legal State Transition governs whether it receives Fact authority and becomes available for later core use.
Invariant does not manufacture Fact meaning, and the implementation does not obtain authority merely by returning an
object. Without a selected Fact path, the material remains declared Operation Result Material and may proceed only
through the contracts explicitly selected for it.

The outbound boundary is:

```text
material explicitly selected by the Publication Contract
    -> Publication judgment
    -> permitted external presentation
       or declared publication stop
```

The selected publication source may be declared Operation Result Material, including that same material after it has
acquired Fact authority, or another explicit Fact. Publication does not infer its source from a runtime type.

A concrete flow may therefore be:

```text
WithdrawInput
    -> Admission
    -> optional Canonicalization
    -> Lowering
    -> WithdrawalRequest Fact

Core machine sees:
    WithdrawalRequest Fact
    AccountBalance Fact
    WithdrawalPolicy material

Core realization produces:
    WithdrawalCompletion Result Material

Fact Contract, when selected:
    gives the same material the AccountBalanceChanged Fact Candidate role

Invariant:
    judges declared relations over the visible Facts and the Fact Candidate when selected

State / Transition:
    governs legal movement and whether that material receives Fact authority

Publication:
    judges the explicitly selected source and produces the permitted withdrawal result presentation
```

`amount text -> bounded decimal amount Fact coordinate` may belong to Lowering when the Input Contract already declares
the decimal meaning. `current balance - amount -> new balance` belongs to core realization. `new balance satisfies the
declared minimum and conservation laws` belongs to Invariant. `the new balance information becomes available in the
next legal machine condition` belongs to State and Transition. `the permitted external response contains the withdrawn
amount and remaining balance` belongs to Publication.

Contracts may exist without an Operation implementation. An interface operation handle selects and coordinates one
external contract flow inside the enclosing machine scope; it does not become the source of Fact authority and does not
prescribe the shape of the internal implementation graph. Internal core work may be split, fused, or reordered behind
its declared obligations without becoming another IDL operation or recursively opening another contract pipeline. A new
operation is required only when the machine intentionally declares another external operation boundary, not merely
because an implementation contains another function or stage.

### 4.5. Common Definition-Time Authority Path

The common contract-definition authority path is:

```text
slot-selected source coordinate
-> existing adapter-neutral frozen acquisition
-> FrozenMetamodelImage
-> role-specific planning and adapter-erased lowering
-> ratified Kontrakt-owned contract material
-> ContractImage-visible material
-> compiler-derived semantic plan
-> generated realization behind that material
```

A role does not own a duplicate acquisition engine, closure frontier, cycle table, frozen image, planning lifecycle,
identity protocol, or publication protocol. Frozen acquisition establishes operation-neutral source facts. Role-specific
planning decides which facts matter for the selected obligation. Contract graph lowering and ratification remove carrier
and backend authority from the result.

The optimization law is:

```text
Reject at the earliest stage that already owns enough declared authority to reject.
```

A contract must not defer a judgment it already owns. It must also not pull forward a judgment owned by another
contract.
Each successful judgment narrows or authorizes material before the next implementation region pays its computation,
allocation, normalization, lowering, or global coherence cost.

This authority path is not a mandatory physical runtime schedule. Kontrakt-owned reads, gates, canonical producers,
Lowering realizations, Fact seals, and state checks may be specialized or fused where equivalent. Unrelated
user-supplied implementation code remains outside that V1 optimization authority, and factual meaning, refusal
attribution, core-entry handoff, and state movement must remain explicit.

---

## 5. Contract Processing Profiles

### 5.1. Input Contract

Input is the boundary presentation contract.

It declares which outside presentation may appear for a flow, which distinctions the boundary must preserve, and
which values later contracts may judge. It does not place that outside material inside the core. Admission still decides
whether presented material may continue. Canonicalization produces stable representation only when the flow selects that
contract. Lowering decides which immutable information may cross the airlock as the boundary Fact material selected for
the same flow. After legal handoff, the core may use that Fact without observing the external presentation.

A host declaration may already state an explicit external contract. A `Set`, nullable type, sealed hierarchy, or
default argument is not silent merely because it belongs to Kotlin or Java. But neither the declaration nor an object
instance receives Kontrakt authority directly. The declaration nominates external contract evidence that Kontrakt must
refine and ratify; the object is only one way material may arrive.

Input does not introduce a second acquisition pipeline, a second frozen image, a second planning engine, or a mandatory
runtime material layer. It enters the machinery already established by ADR-0039, ADR-0040, and ADR-0043 on two separate
timelines.

At contract-definition time, the path is:

```text
slot-selected input root coordinate
-> existing adapter-neutral metamodel acquisition
-> FrozenMetamodelImage
-> planning-facing frozen providers
-> input-specific projection and source-profile judgment inside planning
-> adapter-erased lowered Input material
-> ratified contract graph unit
-> ContractImage-visible Input material and generated boundary realization
```

`FrozenMetamodelImage` remains the operation-neutral authority for source and metamodel facts. Input planning does not
re-open reflection, KSP, bytecode, source AST, PSI, or backend-local handles. It decides, for the selected operation,
which
frozen facts form the Input presentation and how those facts must be interpreted under the Input role.

Frozen acquisition and planning remain separate passes. Input does not merge them and does not create another
`TypeReference` closure frontier, cycle-identity acquisition path, raw-fact image, graph engine, readiness bridge,
budget
ledger, or frozen publication protocol. Existing planning mechanics may be reused, but Input owns its own projection
law.
Generation-oriented constructor selection, property eligibility, active-member projection, polymorphic expansion, and
cycle truncation do not become Input semantics merely because those mechanisms already exist.

The operation-specific result is lowered through the existing contract graph protocol. A source declaration, DTO object,
backend handle, or planning node cannot become Input identity directly. Input meaning becomes authoritative only after
it
has been lowered into ratified Kontrakt-owned contract material. The resolved Input presentation is therefore the
logical
operation-specific planning and lowering result; it is not another frozen metamodel image.

At invocation time, the path is shorter:

```text
already-formed immutable Input presentation
-> generated boundary reader or static gate
   -> declared presentation formed -> Admission
   -> declared presentation refused -> declared Input stop
   -> delegated early gate stops -> result owned by the supplying contract
```

The Input boundary accepts only explicitly declared immutable presentation material. A mutable object, lazy value,
proxy,
framework-bound object, live collection view, or lifecycle-dependent carrier is not an Input presentation. It must be
transformed into an immutable presentation before it reaches the Input boundary, or the direct source profile must
reject
it.

That transformation may be handwritten or generated, but it is an explicit adapter or presentation-formation
operation outside the Input runtime boundary. The generated boundary realization must not choose a capture moment,
invoke
lazy behavior, snapshot mutable state, or derive stable meaning from a live carrier. It reads and verifies
already-formed
immutable presentation material.

The immutable presentation is not a second frozen image, contract graph unit, or mandatory universal
`FormedBoundaryMaterial` domain type. Runtime invocation must not repeat metamodel acquisition, source resolution,
planning, graph ratification, canonical identity derivation, or contract publication.

The V1 admissibility law is:

```text
Input may accept an external presentation contract only when it can be deterministically refined into explicit, finite,
inspectable, loss-accounted, root-owned, transitively immutable boundary material without allowing external
implementation mechanics, pointer topology, hidden movement, or another contract role to survive refinement.
```

The authoring law is:

```text
Do not make the user declare the same contract twice.
Treat the host declaration as explicit external contract evidence.
Refine it through one deterministic Kontrakt law or reject it.
```

This law is how the Input API carries the discipline of *What Contract Is*. Kontrakt must not force users to restate an
external contract they already selected through a host declaration. For each supported external contract, the selected
frontend must provide one deterministic refinement law, remove external implementation mechanics from the result, and
reject the source or require projection when no safe refinement exists.

Three forms of hidden meaning are rejected.

**Hidden authority** exists when Kontrakt lets external implementation structure decide Kontrakt contract meaning. A
host source may contain inheritance, override dispatch, constructor execution, getter algorithms, framework annotations,
serializer conventions, collection implementations, `equals`, `hashCode`, or proxy behavior, but those mechanics cannot
survive refinement as Input authority.

**Hidden choice** exists when the same explicitly declared external contract admits more than one Kontrakt refinement
and the machine silently selects one. `Set`, `Map`, nullable fields, default arguments, sealed hierarchies, and external
scalar types are not hidden merely because they are host forms; their declarations are already external contract
choices.
V1 must refine each supported form through one fixed law. If no safe unique refinement exists, the source is rejected or
projected rather than asking the user to restate the same contract.

**Hidden movement** exists when observing the alleged input performs behavior or depends on time. Callbacks, lazy
loading,
live streams, futures, suppliers, services, capabilities, and resource handles are not Input material. A source profile
must exclude, project, or reject them; it must not silently ratify them as boundary data.

Slot nomination does not guarantee ratification. Input owns two distinct refusal boundaries. A source that cannot be
deterministically refined is rejected during planning or lowering and never receives ContractImage-visible authority. A
ratified Input contract may still refuse an invocation when the actual carrier cannot make the declared presentation
available. Admission begins only after Input formation succeeds.

An Input source candidate is ratifiable only when it satisfies the conditions below.

**Root selection and explicit resolution.** The operation manifest selects the root input through the `input` slot. The
authoring surface may name only that root, but planning must resolve the complete operation-specific Input presentation
from `FrozenMetamodelImage` before runtime realization is generated. Every visible part, variant, collection shape,
generic argument, presence distinction, source coordinate, external-contract refinement decision, and resolution failure
belongs to the lowered Input material. Authoring may be compact. Contract material may not be implicit.

**Existing-machine integration.** Input source resolution consumes planning-facing providers backed by
`FrozenMetamodelImage`. It must not perform backend lookup or maintain an Input-owned metamodel cache, closure table,
cycle table, raw-fact table, frozen image, or planning session. Missing frozen coverage is a frozen-acquisition or image
integrity problem; disagreement about which covered facts belong to Input is an Input planning or lowering problem.

**Declared observation surface.** Kontrakt must know what the generated boundary realization may observe before an
invocation begins. The declaration graph must be finite and inspectable. Runtime collection cardinality may vary, but
capacity and budget must bound access. Runtime property discovery, dynamic reachability, and accidental members do not
extend the contract surface.

**Presentation-only authority.** Input may declare names, part shapes, value kinds, absence, multiplicity, finite
variants,
collection presentation, symbolic references, and coordinates. It must not decide validity, canonical identity, core
meaning, state legality, publication permission, policy selection, or movement. Those authorities belong to other slots.

**Declared external contracts are refined, not restated.** Kontrakt may refine a supported host declaration
automatically when the selected frontend defines one deterministic mapping from its external contract to Input
obligations. Primitive and scalar values, enums, closed products, finite nested products, closed generic products,
ordered sequences, set membership, map association, nullable presentation, and source-visible sealed alternatives may
all be refined when their subordinate material closes under the same law. Automatic refinement removes repeated
authoring; it does not grant the host declaration or its implementation Kontrakt authority.

**Refinement must be deterministic.** The user does not repeat a host contract in Kontrakt vocabulary. For every
supported external source contract, the selected frontend must define one canonical refinement law. When a source form
cannot be refined without inventing meaning, preserving external implementation authority, or losing a required
distinction, Kontrakt rejects the source or requires adapter projection. The refinement result must be visible in the
resolved Input material and included in contract identity where it changes meaning.

**Distinction preservation.** A directly ratifiable carrier must preserve every distinction the declared presentation
may
need. Missing, present-null, present-value, empty, duplicate, order, discriminator, and numeric precision are distinct
when the contract says they are distinct. Input realization must not pretend to preserve information that a serializer,
container, default expression, or framework binder has already erased. When an earlier carrier has removed a required
distinction, the adapter must supply a less lossy presentation such as raw bytes, a token stream, an entry sequence, a
presence bitmap, or an explicit discriminator.

**Explicit immutable boundary access.** External carrier lifecycle must not influence later judgment. Admission may
observe only an explicitly formed, transitively immutable Input presentation. Mutable, lazy, proxied, framework-owned,
live-view, or lifecycle-dependent carriers must be converted before the Input boundary through an explicit adapter or
presentation-formation operation. Input runtime machinery must not snapshot them, select a capture time, or execute
behavior in order to manufacture stability.

**Input-owned refusal boundary.** Input may refuse an invocation only when the declared boundary presentation cannot be
formed: required material is unavailable, the carrier is structurally incompatible, a declared encoding is malformed, a
ratified alternative cannot be identified, a required distinction is unavailable, or stable observation cannot be
established. Input must not refuse a presentation merely because Admission, Canonicalization, Lowering, or Invariant
will
later reject it. A successful Input formation is therefore the precondition for Admission; an Input refusal produces no
Admission candidate and stops the operation before Admission begins.

**Delegated early-stop attribution.** Capacity, Budget, Policy, Governance, or another ratified gate may be executed
while
the boundary reader is still acquiring material. Early execution does not transfer authority to Input. The stop retains
the declared result and diagnostic attribution of the contract that supplied the gate and must not be converted into
Input failure or repeated by a later stage.

**Root ownership.** Input does not inherit or recursively compose contracts. A nested declaration or generic argument
may
describe the shape of a root part, but it does not introduce independent admission, lowering, invariant, publication,
governance, lifecycle, or movement authority. Kontrakt expands such declarations into one root-owned Input presentation.
This is material expansion, not contract composition.

**Declared material traversal.** Input may follow declared, finite, acyclic material paths such as
`order.customer.address.city`. That is ordinary product expansion. Input must not discover meaning from aliasing, shared
object identity, cycles, runtime reachability, reference equality, or pointer topology. Planning may later process
references and cycles inside Kontrakt-owned material under its own rules; the Input boundary does not import the host
object graph as law.

**Constructor and accessor boundary.** Constructor shape may provide declaration coordinates. Constructor execution,
factory execution, builder execution, initialization logic, and default argument evaluation do not define Input meaning.
Accessors may read already-ratified parts, but accessor discovery and arbitrary getter behavior do not define the part
table.

**Host declaration co-location boundary.** Kotlin data classes and Java records necessarily co-locate record-like
coordinate declarations with host constructor syntax. V1 accepts that source co-location instead of requiring the user
to
declare the same presentation again through a generated or parallel Kontrakt type. The frontend reads
primary-constructor
`val` properties or record components as explicit external shape evidence and separates them during lowering. Coordinate
names, declared kinds, presence, supported refinement decisions, and declaration order may enter Input material. JVM
`<init>`, allocation, field writes, compact or secondary constructor execution, `init` blocks, default-expression
execution, generated `copy`, `componentN`, `equals`, `hashCode`, and `toString` methods, accessor methods, class
ancestry,
and object identity remain implementation artifacts and do not enter ratified contract material. Source co-location is
allowed. Authority co-location is not.

**Interface implementation boundary.** A user-defined interface, an interface-typed part, or a runtime implementation
relationship does not provide Input authority. V1 must not derive Input parts or meaning through interface ancestry,
runtime implementation discovery, default methods, override dispatch, or implementation-provided access behavior. A
final closed declaration may still be ratified when it independently satisfies the direct-source and transitive
immutability conditions; any interface relation is erased and contributes no Input material. Recognized standard
collection interfaces may nominate external shape semantics only through dedicated deterministic refinement profiles.
Their runtime implementations do not prove immutable Input presentation and contribute no contract meaning. Any other
interface-based source requires an already-formed immutable presentation supplied through an explicit formation boundary
or is rejected.

**Generic closure.** Every generic argument in the resolved graph must itself resolve as Input material. The outer
carrier
may describe structure, but its arguments cannot remain raw, wildcarded, star-projected, platform-erased, or otherwise
open. A reusable generic declaration is a source template until the selected operation closes every argument.

**Ordered multiplicity.** `List<T>` and array-like declarations may nominate ordered multiplicity when `T` resolves.
That declaration-level refinement does not prove runtime immutability. Ordinary Kotlin or Java lists may be backed by
mutable storage, and arrays are mutable. Direct Input therefore requires a supported immutable sequence presentation or
an explicitly formed immutable presentation that preserves order and ownership. Collection nesting contributes to
declaration depth; runtime element count belongs to Capacity and Budget. The concrete collection implementation
contributes no contract meaning.

**Set refinement.** `Set<T>` is an explicit external contract choice. Under the JVM V1 profile, Kontrakt refines the
declaration as membership presentation of `T`: repeated occurrence and sequence position are not observable at this
boundary. The ordinary runtime `Set` implementation is not direct Input authority and does not prove immutable
ownership.
A supported immutable membership presentation or explicit immutable formation must supply the runtime material.
Kontrakt does not adopt host equality, hashing, iteration, or storage mechanics as its own law. If the operation's Input
obligations require repeated occurrences or order, `Set<T>` cannot realize them and a loss-preserving presentation is
required.

**Map refinement.** `Map<K, V>` is an explicit external contract choice. Under the JVM V1 profile, Kontrakt refines the
declaration as a key-value association presentation in which each represented key has one represented value. Duplicate-
key occurrence and original entry order are not observable at this boundary. The ordinary runtime `Map` implementation
is not direct Input authority and does not prove immutable ownership. A supported immutable association presentation or
explicit immutable formation must supply the runtime material. Kontrakt does not adopt host equality, hashing,
comparator, iteration, or storage mechanics. If the operation's Input obligations require duplicate-key evidence or
entry
order, `Map<K, V>` cannot realize them and an entry sequence presentation is required.

**Closed-choice refinement.** An enum or source-visible sealed hierarchy is an explicit external finite-choice
contract. Under the JVM V1 profile, Kontrakt may refine it automatically when the complete alternative set and every
variant payload are visible and resolvable. The ratified Input material owns the explicit alternative set,
frontend-fixed
discriminator coordinates, and unknown-variant failure; ancestry is erased from contract authority. Adding or removing a
variant changes the source candidate and contract identity and therefore requires compatibility and version judgment. It
must never mutate an active contract silently. Open polymorphic hierarchies and runtime subtype discovery do not satisfy
the V1 Input condition.

**Nullability and default refinement.** A nullable host part is an explicit external contract allowing a value or
`null`. Kontrakt refines it automatically to that presentation and does not invent an absent-versus-present-null
distinction that the source does not carry. If the operation requires that distinction, the source cannot realize the
Input obligation and a less-lossy carrier or adapter is required. A host-language or serializer default is also an
explicit external carrier contract, but V1 refines only the value that reaches the boundary. Construction-time or
serialization-time default execution does not become Input authority. If omission or default application itself must be
judged, the carrier must preserve that event explicitly.

**External scalar refinement.** A foreign scalar type is an explicit external contract candidate. For every supported
foreign scalar, the selected frontend profile must define one canonical mapping into declared Input scalar material.
Host-specific equality, scale, timezone, locale, normalization, parsing, or rounding mechanics do not survive merely
because the host type provides them. If the profile cannot erase those mechanics while preserving the Input obligation,
the source is rejected or an adapter projects it into supported scalar material. The user does not restate the same
scalar contract in Kontrakt vocabulary.

**References as values.** Input may carry declared references such as identifiers, tokens, paths, URIs, parent
coordinates, callback coordinates, and resource coordinates. These are values. A live object reference, callback target,
service object, file handle, session object, or capability does not become Input material merely because it is carried
in
a field.

**Explicit graph and tree presentation.** Tree and graph-shaped Input may be represented through nodes, edges,
identifiers,
parent coordinates, and other finite rows. V1 does not accept recursive or shared-reference JVM object graphs as direct
Input sources. Graph identity, reachability, parent validity, cycle absence, and accepted core shape belong to
canonicalization, lowering, and invariant.

**Opaque material boundary.** Opaque data may be Input material when its presentation is still declared: bounded bytes,
bounded text, tokens, coordinates, or a foreign payload with an explicit content kind and size boundary. An arbitrary
live JVM object is not opaque data. Keeping such an object for later execution would preserve external lifecycle and
capability inside the pipeline.

The V1 user-facing policy has four classes.

| Authoring class                                                                                                            | V1 treatment                                                                         | Examples                                                                                                                                                                                                                                                                                         | Contract effect                                                                                                                                                                                                         |
|----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct immutable presentation                                                                                              | Refine and ratify directly when the complete visible graph is transitively immutable | Scalar, enum, transitively immutable Kotlin data class or Java record, closed immutable product, finite immutable nested product, generated Input presentation, supported immutable sequence, membership, or association presentation                                                            | The declaration supplies explicit external contract evidence and the runtime value already satisfies the immutable Input boundary; no duplicate presentation declaration is required                                    |
| Refinable declaration without direct immutable runtime proof, or a declaration that cannot preserve a required distinction | Require explicit immutable presentation formation or reject the source               | Ordinary `List<T>`, `Set<T>`, `Map<K, V>`, array, mutable-backed read-only view, Java record or Kotlin data class containing mutable parts, lossy serializer product, nullable carrier when absence must remain distinct, unsupported scalar semantics, open generic or hierarchy                | Kontrakt may refine declaration meaning, but it does not snapshot runtime state or invent missing meaning; an explicit adapter or generated formation surface supplies immutable material that satisfies the obligation |
| Implementation-shaped carrier                                                                                              | Require explicit adapter or presentation formation before Input                      | Mutable JavaBean, inherited DTO, user-defined interface root or interface-typed part, runtime implementation-discovered source, framework DTO, serializer object, proxy, entity, custom getter, delegated property, third-party object, dynamic JSON object, recursive or shared-reference graph | Host conventions, lifecycle, and implementation relationships are removed before the Input boundary                                                                                                                     |
| Behavior, capability, movement, or role leakage                                                                            | Reject from Input                                                                    | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core fact, state, backend handle                                                                                                                                                | The material belongs to another role or is not contract data                                                                                                                                                            |

For the JVM V1 zero-adapter source profile, Kontrakt uses the host declaration itself as source evidence rather than
requiring a second generated presentation declaration. A Kotlin data class, a Kotlin final class whose ratified parts
are
primary-constructor `val` properties, or a Java 17+ record may satisfy the direct-source conditions only when the entire
visible part graph is transitively immutable. `data class`, `val`, `final`, and `record` are evidence about declaration
shape; none proves deep immutability by itself. Ordinary `List`, `Set`, `Map`, arrays, mutable nested objects, custom
getters, delegated properties, proxies, and live views disqualify direct runtime presentation unless the selected
frontend recognizes a concrete immutable external contract with a fixed refinement profile.

The frontend does not admit the host class or constructor as a contract unit. It interprets primary-constructor `val`
declarations and record components as a record-like external presentation surface. Contract lowering retains only the
resolved coordinates, declared kinds, presence, declaration order, supported refinement decisions, and immutable-closure
obligations. Constructor execution and generated class machinery remain on the implementation axis and are erased from
contract authority. The same host declaration may therefore supply source evidence and a JVM construction surface
without
mixing their authority in the ratified machine.

Kotlin part order is the declared primary-constructor property order. Java part order is record-component order. A
declaration does not lose eligibility merely because it implements an interface, but no part or meaning may be obtained
from that interface relationship. A root declared as a user-defined interface, or a part whose Input surface depends on
interface dispatch, does not satisfy the zero-adapter profile.

A source-visible sealed hierarchy may participate in the zero-adapter profile when the selected frontend can close the
complete alternative set and every payload from frozen source facts. It is refined automatically under the fixed
closed-choice law above; the user does not restate the hierarchy in Kontrakt vocabulary. The explicit finite alternative
set becomes Input material, while Kotlin or Java ancestry does not.

These declarations are source conveniences, not contract authority. Equivalent presentation material may later come from
another language, schema compiler, serialization system, or generated frontend without changing the Input Contract.

Inherited carrier shape, JavaBean discovery, mutable framework objects, proxy objects, custom getter surfaces, delegated
properties, third-party objects, recursive graphs, and dynamically typed containers may remain in the application
outside
Kontrakt. Before invocation they must be converted through an explicit adapter or presentation-formation surface into
immutable Input material whose operation-specific meaning was resolved and lowered through the same frozen-image and
planning path.
Kontrakt V1 does not accept those live objects and snapshot them at the boundary. The rule is not to forbid ordinary
host
objects. The rule is to stop ordinary implementation structure and capture timing from becoming contract law.

Callbacks, capabilities, live resources, and asynchronous control surfaces are not rescued by calling them opaque. If an
operation needs to refer to one, Input may carry an explicit identifier, token, coordinate, source text, byte sequence,
or other declared data representation. Execution and resource ownership remain outside the Input Contract.

If the source cannot be resolved under these laws, or if no safe deterministic refinement exists, the contract source
is rejected during planning or lowering. No ratified Input graph, ContractImage-visible Input authority, or runtime
boundary realization is produced for that source.

If a ratified Input contract exists but the supplied immutable presentation cannot satisfy the declared boundary
because required material is unavailable, structurally incompatible, malformed under the declared representation, or
missing a required distinction, the machine produces the declared Input failure before Admission begins. A mutable,
lazy, proxied, or lifecycle-dependent object is not repaired at this point; it was not a legal direct Input
presentation.
A Capacity, Budget, Policy, Governance, or other cross-cutting gate may also stop execution during boundary access, but
that result remains owned by the supplying contract and is not Input failure.

### 5.2. Admission Contract

Admission is the continuation judgment over the immutable presentation made available under the ratified Input
Contract.

It asks whether those same boundary values may continue through this operation. There is no user implementation
transformation between Input and Admission. Admission depends on Input, but it must not create, copy, snapshot, parse,
coerce, normalize, discover, or otherwise reconstruct Input material. A source that has to perform such work before it
can judge is in the wrong slot.

Admission follows the same authority law as Input:

```text
ordinary Java or Kotlin declaration
-> selected by an interface-operation contract slot
-> acquired by the matching host-language frontend
-> rejected or refined under one deterministic law
-> lowered into implementation-erased Kontrakt material
-> ratified as Admission authority
```

The user does not author Kontrakt IR, generated coordinate objects, expression nodes, evaluator instructions, handlers,
adapters, or runtime assembly. Java and Kotlin are frontend languages. Their declarations are external evidence, not the
final contract representation.

An Admission source is ratifiable only when it satisfies the admission conditions below.

**Manifest-slot selection condition.** Under the interface shape fixed by ADR-0046, the operation manifest binds only a
contract declaration to the Admission position:

```text
manifest {
    flow:
        input      CalculateInput
        admission  XGreaterThanOne
}
```

The manifest headings `flow`, `failure`, `movement`, `bounds`, and `diagnostics` exist only in source layout to help the
author locate slots. They introduce no contract material, identity, authority, ownership, hierarchy, composition,
ordering, namespace, processing boundary, or lowering unit, and they disappear before contract resolution. Each slot is
resolved independently according to its own contract kind; no contract meaning is derived from textual co-location under
a heading.

The `admission` slot grants the Admission role to the named source declaration for that operation. The declaration does
not possess Admission authority because of its class name, method name, package, file, annotation, parameter type,
inheritance relation, runtime type, or placement beneath the `flow` heading. The slot also does not bind an
implementation
handler, evaluator, adapter, factory, or instance.

The manifest may use an imported simple name, but definition-time resolution must end at one exact class or object
symbol. A file, package, naming convention, parameter type, annotation scan, assignable-type search, service registry,
or runtime lookup must not create or complete the binding.

**Independent declaration condition.** One selectable class or object names one flat Admission Contract. A selected type
must not act as a container of multiple independently selectable Admission contracts. Multiple clauses, intermediate
expressions, immutable constants, and private helper operations may participate in the one root judgment, but they do
not become nested contracts, member contracts, inherited contracts, or independently selectable Admission identities.

Multiple independent Admission declarations may coexist in one source file because a file is only a source-organization
unit. When several operations use the same Admission law, each operation explicitly binds the same declaration in its
own `admission` slot. The lowered material may be structurally shared, but operation-slot binding, state participation,
rejection, failure, and diagnostic attribution remain operation-specific.

Admission inheritance, marker-interface membership, override, virtual specialization, member selection from a common
holder, and type-hierarchy reuse are prohibited. Shared meaning is reused by selecting the same flat declaration, not by
deriving another Admission type.

**Input dependency condition.** Admission is compiled against the ratified Input material of the same operation. Every
runtime operand must resolve to a coordinate, nested part, finite alternative, collection element, or other value
already
made available by that Input Contract. Policy, Governance, Capacity, Budget, environment, and implementation objects do
not become implicit Admission operands. Those contracts may select the active contract world or stop under their own
authority, but Admission does not inspect them as undeclared runtime data.

A literal or constant may participate only when the frontend can ratify its complete value and semantic type at
definition time. A `val` or `final` field is not sufficient when its value depends on initializer execution, external
state, class loading, framework injection, or another runtime capability.

**Carrier and judgment separation condition.** A V1 direct Input DTO remains an inert immutable presentation carrier.
Except for compiler-generated carrier machinery and inert formation, it must not declare validation, normalization,
derived access, Admission judgment, custom getter behavior, delegated access, semantic constructor behavior, or helper
methods. Admission is declared in its own selected class or object.

The same immutable Input presentation may participate in different operations with different Admission laws. Therefore
a carrier-wide `isValid`, `validate`, `isAdmissible`, or `canProceed` method cannot own continuation judgment. Kotlin-
generated accessors, `componentN`, `copy`, `equals`, `hashCode`, and `toString`, and Java record accessors, `equals`,
`hashCode`, and `toString`, remain host artifacts and are erased from Input and Admission authority.

**Host declaration profile.** The JVM V1 frontend accepts a dedicated source-visible Kotlin `object`, a dedicated closed
Kotlin class with no runtime instance state, or a dedicated Java final class with no runtime instance state. The
selected
declaration must expose exactly one eligible root judgment that consumes the selected Input presentation and yields a
Boolean continuation result. The root member name is a frontend convention, not contract identity; uniqueness and the
operation-slot binding determine the root.

The declaration may contain statically ratifiable immutable constants, immutable local bindings, and private
source-visible helper operations. A helper is accepted only when its complete body is available, its call graph is
closed and acyclic, it cannot dispatch virtually, and the frontend can inline or otherwise refine all of its meaning
into
the same flat Admission material. A helper is source decomposition, not a second contract. Constructor parameters,
injected dependencies, mutable fields, delegated state, runtime initialization, and captured application objects are
prohibited.

For example, the following is ordinary Kotlin source. It imports no Kontrakt API and constructs no Kontrakt expression
node:

```kotlin
package example.calculate

data class CalculateInput(
    val x: Int,
    val limit: Int,
    val flags: Int,
)

object XGreaterThanOne {
    private const val MINIMUM: Int = 1
    private const val REQUIRED_FLAGS: Int = 0b0011

    fun admit(input: CalculateInput): Boolean {
        val requiredFlagsPresent =
            (input.flags and REQUIRED_FLAGS) == REQUIRED_FLAGS

        return input.x > MINIMUM &&
                input.x <= input.limit &&
                requiredFlagsPresent
    }
}
```

The Kotlin frontend does not ratify the singleton object, function invocation, local-variable layout, property accessor,
or JVM bit-operation call. It refines the selected source into bound Input coordinates, canonical literals, typed value
operations, judgment relations, composition, and evaluation law. A Java declaration with equivalent value semantics
must lower to equivalent Kontrakt material.

**Full supported judgment-surface condition.** Admission is not defined by a small closed list of primitive predicates.
V1 must accept the full legitimate continuation-judgment surface of the supported JVM presentation profile whenever the
frontend can reduce it to finite, total, deterministic material over ratified Input values and statically ratifiable
literals.

The required semantic surface includes at least:

- Boolean values, negation, conjunction, disjunction, exclusive-or, implication, equivalence, and finite conditional
  judgment;
- signed and unsigned integral arithmetic, comparison, equality, range relations, conversions, and primitive integral
  bit operations with explicit width, signedness, overflow, narrowing, and shift laws;
- floating-point classification, ordering, and equality under an explicit IEEE, total-order, or raw-bit law, including
  explicit treatment of NaN and signed zero;
- finite-alternative and enum relations lowered to ratified alternative identities rather than runtime enum-object
  identity;
- explicit null, absence, presence, and value relations without silently collapsing distinctions preserved by Input;
- character, string, pattern, and binary relations under an explicit unit, normalization, case, locale, charset, and
  pattern-semantics profile where those distinctions matter;
- nested closed-product reads and structural relations over ratified parts without invoking user-defined `equals`,
  `hashCode`, or `compareTo` implementations;
- fixed-index and bounded relations over arrays and binary material with explicit index-definedness;
- size, membership, equality, ordering where declared, bounded quantification, and bounded aggregation over ratified
  arrays, sequences, sets, and maps; and
- relations over supported JVM value profiles such as large numbers, identifiers, and temporal values when Kontrakt owns
  a complete versioned semantic profile for the selected type.

This catalog describes semantic coverage, not permission to execute arbitrary JVM code. A Java or Kotlin expression is
accepted because the frontend knows its complete contract meaning and can erase the host operation, not because the JVM
can execute it.

**Ordinary-expression condition.** The frontend may accept ordinary host-language literals, direct Input part access,
immutable local bindings, arithmetic expressions, comparisons, Boolean expressions, bit expressions, exhaustive
`if`/`when`/`switch` forms, and other finite expressions whose meaning can be completely refined.

An expression may derive a temporary value solely for the Admission judgment. That does not grant Canonicalization or
Lowering authority and must not publish a transformed presentation. Parsing, coercion, normalization, default
substitution, representation repair, and production of a replacement value remain outside Admission unless an earlier
contract has already established the interpreted value as Input material.

**Known-operation refinement condition.** A source-level operation call is accepted only under one of two laws.

First, a private non-overridable helper inside the selected declaration may be accepted when its entire acyclic body is
refined into the root judgment as described above. Second, a Java or Kotlin standard-library surface may be accepted
when
the selected frontend owns a stable, versioned semantic profile for that exact operation. For example,
`String.startsWith`, `List.all`, or a finite numeric operation may serve as source syntax only when the host call is
removed and replaced by backend-independent prefix, bounded-quantifier, or numeric material.

Unknown calls, user-defined receiver methods, custom predicates, user-defined equality or ordering, extension functions
whose bodies are unavailable, method references, virtual calls, framework callbacks, and library operations without a
complete Kontrakt semantic profile are rejected. Method purity is not assumed from naming, annotations, finality, or a
Boolean return type.

**Bounded collection and binder condition.** Collection judgment is part of Admission when the Input Contract and the
active Capacity or Budget material close the required access bound. The frontend may refine source forms such as
`all`, `any`, `none`, `count`, finite membership, bounded sum, bounded minimum, and bounded maximum into explicit binder
and aggregation material.

A Kotlin or Java lambda used syntactically inside such a recognized bounded operation is not preserved as a runtime
function object. It is accepted only when it does not escape, its captures are limited to ratified Input coordinates or
statically ratifiable constants, and its body independently satisfies the Admission source law. The frontend lowers it
to an explicit finite binder. A lambda, `Predicate`, `Function`, method reference, or functional-interface instance used
as a stored runtime value, dynamically supplied callback, or dispatch surface is rejected.

Iteration order, duplicate treatment, element equality, null-element treatment, and cardinality bounds must be ratified
where they can affect judgment or evidence. A mutable-backed read-only view, live collection, runtime-discovered
container, or collection without the required bound cannot become Admission material merely because a host library can
iterate it.

**Totality and termination condition.** Every accepted Admission judgment must be total for every presentation admitted
by the selected Input Contract and must terminate under a definition-time-known bound. Division by zero, invalid shifts,
invalid indices, narrowing loss, exact-arithmetic overflow, malformed patterns, unsupported encodings, and similar
undefined or exceptional paths must be ruled out by static proof, represented by an explicit total relation, or
rejected.
A JVM exception must never become an implicit Admission refusal.

Finite iteration over contract-bounded material is allowed. Arbitrary `while` or `do-while` loops, runtime-dependent
unbounded loops, recursion, cyclic helper calls, blocking operations, waiting, synchronization, and termination that
relies on application behavior are prohibited in V1. The semantic judgment surface may be rich; the machine must still
know before publication that every invocation completes under the declared bounds.

**No hidden observation condition.** Admission may not observe or invoke repositories, services, clocks, randomness,
environment variables, system properties, files, networks, transactions, threads, executors, locks, mutable globals,
framework context, dependency-injected objects, lazy values, delegated properties, proxies, reflection, runtime class
inspection, object identity, resource handles, streams, futures, or other capabilities. If such information is required,
it must first become explicit immutable presentation under the authority of the appropriate contract boundary.

Exception-driven choice, catch-based validation, runtime type discovery, inheritance-dependent behavior, and callback
completion are also rejected. They hide judgment, movement, or implementation authority behind host execution.

**Deterministic refinement condition.** Definition-time processing must perform the following work before Admission can
receive authority:

```text
resolve the exact class or object named by the operation's `admission` slot, which is written beneath the `flow` source-layout label
-> identify the unique eligible root judgment
-> close and validate all accepted helper and binder bodies
-> bind every value read to ratified Input material or a canonical literal
-> resolve every host expression to a versioned Kontrakt semantic operation
-> validate type, null, numeric, ordering, collection, totality, and bound laws
-> erase class, object, method, getter, lambda, iterator, and library-call mechanics
-> canonicalize judgment structure, literals, source coordinates, and evaluation law
-> derive stable Admission material identity
-> ratify and publish the material in the ContractImage
-> generate the deterministic Admission evaluator
```

Contract identity must change when a frontend profile, numeric law, string law, collection law, evaluation law, or any
other semantic refinement changes contract meaning. Source formatting, local variable names, equivalent host syntax, and
backend instruction choice must not change identity when they lower to the same material.

**Deterministic evaluation condition.** At invocation time, the generated evaluator reads only the already-formed
immutable Input presentation through fixed ratified coordinates. Runtime symbol lookup, reflection, property discovery,
method dispatch, callback construction, literal parsing, operator selection, and failure-policy selection are forbidden.

Boolean composition and bounded quantification must have a fixed evaluation law. V1 preserves a deterministic declared
or canonical order wherever order can affect first decisive judgment, diagnostic evidence, budget consumption, or
failure attribution. A backend may fuse branches, use primitive comparisons and bit instructions, specialize bounded
loops, vectorize, or return allocation-free outcome codes only when the observable admitted or rejected result and its
contract-owned attribution remain identical.

The determinism law is:

```text
same ratified ContractImage
+ same immutable Input presentation
+ same declared cross-contract world
= same Admission outcome and the same contract-owned attribution
```

**Result condition.** The logical V1 result is admitted or rejected. A source Boolean `true` maps to admitted and
`false`
maps to rejected only after the complete expression has been refined and ratified. The canonical material must preserve
enough judgment structure and source coordination for deterministic Failure and Diagnostic contracts to attribute the
rejection without executing the source method.

Deferred, capacity-shaped, policy-shaped, or governance-shaped outcomes remain owned by their respective contracts. An
early stop supplied by another contract must retain that contract's result and must not be converted into Admission
rejection.

If the source cannot be completely refined under these laws, the contract definition is rejected before ContractImage
publication. If a ratified Input presentation fails the generated judgment, Admission produces the declared rejection
result. The generated evaluator is implementation-axis machinery and is wrong if it disagrees with the ratified
Admission material.

### 5.3. Canonicalization Contract

Canonicalization is the optional stable-representative production contract.

Kontrakt does not ask how the submitted Input was produced, whether another system transformed an earlier value, or
whether the submitted presentation was considered canonical outside this operation. Those matters do not exist inside
the Kontrakt contract world. The submitted presentation is the raw Input material observed by this operation.

When the `canonical` slot is selected, its law receives the same immutable contract-visible presentation after Admission
and deterministically produces the one representative permitted by that law. Successful production grants stable
representative authority immediately. There is no user-authored post-canonical validation contract. If a successful
Kontrakt realization produces noncanonical material, Kontrakt is defective.

The defining boundary is:

```text
same contract-visible presentation shape
-> declared equivalence and distinction law
-> one stable representative
-> one Kontrakt-owned canonical byte representation
```

Canonicalization may replace a coordinate value with an equivalent representative, impose deterministic order where the
selected contract declares source order irrelevant, and collapse only distinctions that the selected law explicitly
names as irrelevant. It does not add, remove, rename, retype, parse, flatten, project, or otherwise remap
contract-visible coordinates. Those are shape-changing obligations and belong to Lowering or another explicit
transformation boundary.

**Optional slot and selected-source condition.** The authored `canonical` slot may be omitted in V1. Omission means that
this operation declares no semantic Canonicalization Contract. Kontrakt does not insert `ExactCanonicalization`, infer
an
identity representative law, invoke a user canonicalizer, or create a proxy, interceptor, callback, or hidden
implementation stage. After Admission, the same submitted Input presentation is supplied unchanged to the selected
Lowering formation, its compiler-derived plan, and its backend-generated realization.

Deterministic encoding of Kontrakt-owned material remains mandatory, but that protocol is not an implicit
Canonicalization Contract. Fixed scalar encodings, coordinate order, presence markers, framing, schema identity, and
version material may still be required for identity, ordering, hashing, caching, publication, or verification. They
determine how existing material is represented inside Kontrakt; they do not collapse semantic distinctions or produce a
new representative value.

When authored, the independently resolved `canonical` slot names exactly one Kotlin or Java symbol as external source
evidence for one flat Canonicalization Contract. A source-layout label such as `flow` grants no meaning. The selected
symbol, file, package, method name, annotation, type relation, and source location do not own Canonicalization
authority.
The slot supplies the role; frontend refinement decides whether the selected source can be ratified; the resulting
Kontrakt-owned material supplies authority.

V1 supports exactly two selected source forms.

```text
Direct law selection:
    select one complete Kontrakt-provided built-in Canonicalization symbol

Coordinate-law type declaration:
    select one inert Java or Kotlin signature declaration whose coordinate names bind every
    selected Input coordinate and whose coordinate types are exact Kontrakt-owned nominal
    canonical type symbols or finite shape-directed generic type signatures
```

When present, the manifest names one declaration only:

```text
canonical      UnicodeNfcCaseFoldCanonicalization
```

or:

```text
canonical      CustomerCanonicalization
```

When absent, the manifest simply continues from Admission to Lowering:

```text
input          CustomerInput
admission      CustomerAdmission
lowering       CustomerLowering
```

That omission says nothing about values that may have existed before `CustomerInput` was submitted. Kontrakt observes
only `CustomerInput`, and that submitted presentation is its raw Input.

Users do not declare canonical output DTOs, byte encoders, transformation methods, generated coordinate objects,
Kontrakt IR, expression nodes, rewrite-rule collections, implementation handlers, runtime assembly, canonical-law
values,
or descriptor instances inside the contract manifest or selected declaration. The interface IDL selects the
Canonicalization source. A coordinate-law declaration carries only coordinate names and references to nominal type
symbols supplied by the Kontrakt authoring API.

**Flat built-in law condition.** A built-in symbol names one complete, closed, versioned Canonicalization law. V1 does
not
expose one parameterized configuration template from which users compose a law by choosing normalization, case,
whitespace, floating-point, collection, or temporal options. Different meanings require different selectable symbols.

```text
one built-in symbol
= one flat representative law
= one ratified identity world
```

A built-in law does not inherit, override, recursively compose, or acquire meaning from another built-in law. Internal
implementation reuse is permitted, but it must not appear as contract inheritance or composition. This keeps each law's
normative vectors, canonical bytes, version, HID, security bounds, and failure domain independently fixed.

Representative built-in families may include:

```text
ExactCanonicalization
RecursiveExactCanonicalization

UnicodeNfcCanonicalization
UnicodeNfdCanonicalization
UnicodeNfkcCanonicalization
UnicodeNfkdCanonicalization
AsciiCaseFoldCanonicalization
UnicodeCaseFoldCanonicalization
UnicodeNfcCaseFoldCanonicalization
LineEndingLfCanonicalization

RawBitFloatCanonicalization
RawBitDoubleCanonicalization
CanonicalNaNPreserveSignedZeroCanonicalization
CanonicalNaNCollapseSignedZeroCanonicalization
RejectNaNCanonicalization
RejectNonFiniteCanonicalization
IeeeTotalOrderCanonicalization

DecimalScalePreservingCanonicalization
DecimalNumericValueCanonicalization
DecimalFixedScaleCanonicalization

OrderPreservingSequenceCanonicalization
OrderAgnosticSetCanonicalization
OrderAgnosticBagCanonicalization
CanonicalMapKeyOrderCanonicalization
ExactBinaryCanonicalization

ExactZonedTimeCanonicalization
InstantPreservingZonedTimeCanonicalization
InstantOnlyCanonicalization
FixedPrecisionInstantCanonicalization
```

This list is illustrative, not permission to publish a profile before its semantics, bounds, byte law, refusal domain,
security properties, and conformance material are complete. A convenient host operation is not enough to justify a
built-in law.

`ExactCanonicalization` remains an explicitly selectable contract, not the meaning of slot omission. When selected, it
ratifies the submitted presentation as the representative under its declared exact-distinction law and participates in
Canonicalization identity, conformance, refusal, and canonical-material production. When the slot is omitted, no such
semantic contract or representative authority exists.

**Coordinate-law nominal-type declaration condition.** A coordinate-law declaration is neither a canonicalizer
implementation nor a runtime data object. It is an uninstantiable frontend signature whose declared parameter names
identify coordinates of the Input Contract already selected for the same operation and whose parameter types select
exact
Kontrakt-owned nominal canonical type symbols. The declaration contains no law value, enum constant, singleton instance,
factory call, method body, callback, property initializer, or descriptor object.

The binding law is:

```text
selected Input coordinate name
+ selected Input coordinate sort
+ one exact Kontrakt-owned nominal canonical type symbol
+ compatible shape-directed generic type arguments where the Input shape requires them
= one coordinate law inside one flat Canonicalization Contract
```

The matching occurs only within the operation that independently selected the Input and Canonicalization declarations.
It is resolved at definition time from exact source symbols, declared parameter names, and nominal parameter types. It
is
not a runtime string lookup, package convention, reflection search, annotation scan, assignability rule, or type-wide
contract.

V1 requires complete explicit coverage. Every contract-visible Input coordinate must appear exactly once in the selected
coordinate-law declaration. A missing coordinate, duplicate coordinate, unknown coordinate, renamed coordinate, or
canonical type whose supported sort does not match the Input coordinate rejects the contract definition. Adding,
removing, renaming, reordering, or retyping an Input coordinate invalidates the previous binding and requires
re-ratification. No undeclared default silently applies to a newly added coordinate.

The public authoring vocabulary consists only of nominal type names supplied by Kontrakt. A public canonical type is a
closed name, not a usable runtime abstraction. It has no accessible constructor, factory, singleton value, method,
callback, mutable state, extension point, or user-implementable interface. It is final and cannot be subclassed or
implemented by application code. The frontend accepts the exact fully qualified Kontrakt-owned symbol, not a matching
simple name, user-defined imitation, subtype, alias, or dynamically registered replacement.

The exact public type names remain a frontend decision, but the source form is conceptually equivalent to the following
compilable Kotlin declaration:

```kotlin
// Kontrakt-provided authoring API. Application code imports these names;
// it does not define, instantiate, implement, or extend them.
class ExactText private constructor()
class UnicodeNfcCaseFold private constructor()
class AsciiUppercase private constructor()
class ByCanonicalElementBytes private constructor()
class CanonicalSet<Order, Element> private constructor()

data class CustomerInput(
    val customerId: String,
    val name: String,
    val regionCode: String,
    val tags: Set<String>,
)

// User-authored source declaration. It is never instantiated.
class CustomerCanonicalization private constructor(
    customerId: ExactText,
    name: UnicodeNfcCaseFold,
    regionCode: AsciiUppercase,
    tags: CanonicalSet<
            ByCanonicalElementBytes,
            UnicodeNfcCaseFold,
            >,
)
```

The Kontrakt-provided declarations at the top exist in the real authoring API rather than in user source; they are shown
only to make the example compiler-complete. The user writes the Input and the selected coordinate-law declaration. The
private constructor prevents formation of a `CustomerCanonicalization` object, and its parameters are not properties.
Only their names and exact nominal types are declaration evidence. The frontend resolves and erases the declaration
class, inaccessible constructor, parameter symbols, generic signatures, and every referenced host type before authority
begins.

A shape-directed generic type signature follows the already-ratified Input shape. A set signature may name an order type
and an element-law type; a map signature may name key, value, entry-order, and duplicate-key types; a nested record
signature may carry nominal coordinate-law types for that finite nested shape. These signatures do not create values,
runtime descriptors, nested Canonicalization Contracts, parent-child authority, inheritance, or recursive contract
composition. They are finite source signatures from which Kontrakt ratifies one flat Canonicalization Contract for the
selected operation.

V1 rejects declaration properties, `val` or `var` law bindings, enum law categories, enum constants, law singleton
objects, public constructors, factory calls, arbitrary generic types, executable initializers, methods, callbacks,
lambdas,
property references, custom comparators, user-defined equality or ordering, inheritance, interface-based role
acquisition,
annotations on the Input DTO, mutable fields, lazy or delegated values, captured dependencies, and values acquired from
runtime execution.

A canonical type is not accepted merely because an application class uses a familiar name or compatible generic shape.
Each selectable type must be an exact Kontrakt-provided symbol whose complete semantics, version, applicable sorts,
preserved and collapsed distinctions, representative law, bounded-work law, canonical-byte law, refusal domain, and
conformance material are known before ContractImage publication. V1 exposes no application-defined canonical type
extension point. Adding a new type is a Kontrakt canonical-protocol and authoring-API change, not application plugin
registration.

**Public canonical-type specification condition.** Because the exposed type carries only a name, its behavior must be
specified outside executable host code. For every public canonical type, the Kontrakt API specification and user
documentation must state at least:

```text
supported Input sort and structural position
preserved and collapsed distinctions
unique representative law
semantic profile and version
null, absence, finite-alternative, ordering, and duplicate behavior where applicable
work, depth, cardinality, expansion, and output bounds
canonical byte law and protocol version
refusal domain and cross-contract stop attribution
normative examples and conformance-vector reference
```

The type name and documentation are public projections of the same versioned Kontrakt-owned canonical law material. The
documentation explains the law to users but does not replace that material as authority. A law meaning change requires a
new or versioned canonical type identity, corresponding specification changes, and updated normative conformance
material.

**No user-authored output-presentation condition.** V1 does not require or permit a second user-authored canonical
output
presentation. The contract-visible shape remains the Input shape. The canonical result may have a radically different
Kontrakt-owned physical layout and byte encoding, but that internal material is not another user DTO and does not grant
Canonicalization permission to perform mapping.

```text
user-visible shape:
    preserved

representative values:
    may change under the selected equivalence law

Kontrakt-owned physical material:
    may use a different frozen layout and canonical byte encoding
```

A need to declare a different user-visible output shape is evidence that the obligation belongs to Lowering.

**Distinction-preservation condition.** Every Canonicalization law must declare which distinctions remain observable and
which distinctions it collapses. Canonicalization may remove spelling, ordering, scale, case, normalization-form, NaN
payload, signed-zero, timezone, or other differences only when the selected law explicitly declares them irrelevant.
It must never erase a distinction merely because a backend finds the smaller result convenient.

The ratified operation graph must reject an incompatible downstream binding when a later selected contract requires a
distinction that the Canonicalization law explicitly collapses. A law that publishes instant-only meaning cannot satisfy
a later obligation that requires original region-zone meaning. A law that collapses signed zero cannot later claim
raw-bit floating identity.

```text
preserved distinction:
    remains available to downstream contracts

collapsed distinction:
    cannot be recovered or silently reintroduced later
```

Source co-location or a host type's available fields do not prove preservation. The selected canonical law owns the
preservation statement.

**Protocol canonical-byte condition.** Kontrakt alone owns the exact canonical bytes. Users select representative
laws through built-in symbols or coordinate-law nominal type declarations; they do not write byte encoders. After
semantic representative production, Kontrakt emits one
implementation-erased byte sequence under a versioned protocol that fixes at least:

```text
domain separation
Canonicalization law identity and version
canonical material schema identity
coordinate count and canonical coordinate order
per-coordinate sort tags
null, absence, and presence markers
integer width, signedness, and endianness
floating-point bit and NaN law
Boolean and finite-alternative encoding
character encoding and Unicode version
length framing for variable material
sequence order
set, bag, and map ordering and duplicate law
nested depth and framing
```

The final criterion for canonical equality, ordering, structural interning, and identity is the exact canonical material
and its bytes, not JVM `equals`, `hashCode`, `compareTo`, object identity, field layout, iteration order, serialization,
or generated bytecode shape.

A domain-separated BLAKE3 digest or another ratified HID may accelerate routing, ordering, and lookup after canonical
bytes exist. A digest never replaces exact material equality. Digest ties are resolved by canonical length and exact
canonical-byte comparison under declared bounds. Hashing source objects before canonicalization does not create
canonical identity.

**Environment-independence condition.** Host execution defaults never own representative meaning. Default locale,
default timezone, default charset, operating-system behavior, filesystem case rules, current JDK Unicode tables, process
environment, system properties, clock, randomness, thread state, and acquisition order are prohibited sources of
Canonicalization meaning.

A public type such as `UnicodeNfcCaseFold` is not a request to call the current JDK normalizer or lowercase method.
It selects a complete Kontrakt semantic law with pinned Unicode tables, locale behavior, expansion bounds, totality,
complexity, representative rules, and canonical-byte encoding. The generated canonicalizer must execute the ratified
law,
not inherit meaning from the host library available on the current machine.

The coordinate-law declaration contains no values to calculate. It may not invoke `Normalizer`, `lowercase`,
`uppercase`,
sorting, parsing, environment reads, class initialization, helper calls, or another host operation.
Environment-independent
execution is therefore not inferred from user code; it is guaranteed by the selected nominal type and Kontrakt's
generated
realization.

The complete semantic definitions, host-operation recognition used by compiler internals, legality matrices, byte plans,
and generated evaluators remain compiler protocol. They are not exposed as user IR, runtime registries, expression
nodes,
or generated coordinate APIs. The public surface exposes only complete built-in Canonicalization symbols, opaque nominal
canonical type names, finite shape-directed generic type signatures, precise definition-time diagnostics, and the API
specification and user documentation that explain each type.

**Canonical-type applicability condition.** Rich canonical behavior is admitted through exact nominal type symbols
rather
than executable source expressions or runtime law values. Every nominal type symbol or shape-directed generic signature
must resolve to enough ratified material for Kontrakt to determine, before publication:

```text
supported input sort and structural position
preserved and collapsed distinctions
unique representative law
null, absence, finite-alternative, and duplicate behavior
numeric, floating-point, Unicode, temporal, binary, or collection semantics where applicable
work, depth, cardinality, expansion, and output bounds
canonical byte schema and protocol version
refusal domain and contract-owned attribution
normative conformance material
```

The frontend rejects:

```text
unknown, application-defined, aliased, imitated, dynamically registered, or incompletely specified canonical type symbols
missing, duplicate, renamed, unknown, or sort-incompatible coordinate bindings
law values, enum constants, singleton instances, constructor calls, factories, methods, callbacks, lambdas, helper execution, control flow, or transformation bodies
user-defined equals, hashCode, compareTo, comparator, predicate, or ordering execution
virtual dispatch, inheritance-dependent behavior, interface-based role acquisition, or runtime subtype selection
mutable or captured state, runtime initialization, lazy or delegated observation
dependency injection, repository, service, filesystem, network, process, or environment access
clock, randomness, thread scheduling, blocking, waiting, or synchronization
reflection, runtime class inspection, object identity, or host reference graph traversal
default locale, timezone, charset, collation, normalization table, or platform path behavior
JVM hash-table iteration order, acquisition order, or unstable sorting tie-break
shape-changing output, parsing, projection, flattening, business-value derivation, or arbitrary repair logic
```

A source declaration does not become canonical authority merely because it uses a private constructor, a final class, a
familiar type name, or a compatible generic signature. If every referenced nominal type cannot be resolved to exact
Kontrakt-owned law material and represented as one ratified flat contract, the definition is rejected.

**Finite-work condition.** Canonicalization is a producer contract, but it is not permission for unbounded labor. Every
accepted law must establish finite source size, nesting depth, collection cardinality, intermediate storage, output
size,
and work bounds before ContractImage publication. General imperative iteration and recursive object-graph traversal are
prohibited. Bounded element-wise representative production, ordering, duplicate handling, and aggregation are accepted
only over material whose bounds
and traversal semantics have already been ratified.

For nested unordered material, Kontrakt must not use JVM object references, `hashCode`, host set order, or an unbounded
structural comparison. A valid implementation may:

```text
produce canonical bytes for each bounded child
produce a domain-separated BLAKE3 HID from those bytes
order children by HID
resolve HID ties by canonical length and exact canonical-byte comparison
apply the declared set or bag duplicate law
emit the bounded canonical collection bytes
```

This is an implementation strategy, not user-visible contract syntax. Maximum source bytes, output bytes, cardinality,
depth, comparison work, and intermediate memory must be supplied by the selected Capacity and Budget world. Exceeding
those walls produces the owning Capacity or Budget result, not a generic Canonicalization failure.

**Unicode and adversarial-text condition.** Unicode normalization and case operations must use pinned tables and a
versioned, table-driven, environment-independent algorithm. Kontrakt must not implement authoritative normalization with
an unconstrained regular expression or delegate meaning to the current host normalizer. Input, Capacity, and Budget must
bound at least encoded length, code-point count, combining-sequence depth where applicable, canonical output size, and
intermediate work. Pathological combining-mark payloads may produce a diagnostic classification, but the stop result
must
remain owned by the contract whose declared wall was crossed.

**Floating-point condition.** Floating-point Canonicalization never inherits ordinary `==`, boxed equality, host
sorting,
or incidental NaN behavior. Every selected law must explicitly state whether it:

```text
preserves raw bits
uses an IEEE total order
preserves or collapses signed zero
preserves or collapses NaN payloads
maps all NaNs to one representative
rejects NaN
rejects all non-finite values
```

`+0.0`, `-0.0`, quiet NaNs, signaling NaNs where observable, and NaN payload distinctions remain separate unless the
selected flat law explicitly collapses them. Undefined or unsupported values are refused under the selected law; they
are not repaired by host equality.

**Canonicalizable-domain condition.** A source is not refused merely because it is not already canonical. Noncanonical
spelling, order, scale, case, normalization form, or other declared drift is the ordinary input to Canonicalization. A
Canonicalization refusal occurs only when the source lies outside the selected canonicalizable domain, the selected law
cannot define one unique representative, an operation is undefined under that law, or required semantic material is
unavailable. Capacity and Budget stops remain separate as described above.

```text
noncanonical but canonicalizable source:
    produce the representative

source outside the selected canonicalizable domain:
    Canonicalization refusal

successful result that is not canonical:
    Kontrakt defect
```

There is no `isCanonical`, `validateCanonical`, or user-supplied verifier step after successful production.

**Definition-time processing condition.** The Canonicalization definition path is:

```text
resolve the exact slot-selected built-in symbol or coordinate-law declaration
-> acquire the selected source through the existing frozen frontend machinery
-> determine direct-law or coordinate-law nominal-type source form
-> verify one flat selectable contract and an uninstantiable signature declaration when applicable
-> bind every declared parameter name to exactly one ratified Input coordinate
-> verify complete coordinate coverage, declared order, sort compatibility, and nested-shape agreement
-> resolve every exact nominal canonical type and shape-directed generic type signature to pinned semantic material
-> reject law values, constructors, factories, execution, environment, dispatch, state, effect, imitation, and dynamic-registration paths
-> ratify preserved and collapsed distinctions for every coordinate and the complete presentation
-> prove declared work, depth, cardinality, expansion, intermediate-storage, and output bounds
-> derive the canonical byte schema and protocol version
-> derive stable contract identity and ContractImage material
-> generate the deterministic canonicalizer and byte emitter
```

Runtime performs no declaration lookup, reflection, member discovery, method dispatch, operator resolution, locale or
Unicode-table selection, comparator acquisition, hash-policy selection, or byte-schema construction. It executes only
the generated realization over fixed ratified coordinates and declared cross-contract bounds.

**Version, identity, and conformance condition.** A built-in or declaration-derived Canonicalization identity is derived
from ratified
meaning, not the selected class name alone. Identity material includes at least:

```text
law kind
semantic profile version
source presentation identity
preserved and collapsed distinctions
coordinate-law bindings, representative laws, and their versions
Unicode, temporal, decimal, floating, and collection laws where applicable
canonicalizable domain and refusal law
work and output bound law
canonical byte protocol version and schema identity
```

A frontend change that alters accepted meaning or generated canonical material is a contract identity change. A source
symbol remains an acquisition coordinate and does not substitute for this material.

Every built-in law must have a versioned normative conformance-vector set and a separate implementation-verification
suite. Changing semantic meaning or a normative expected result changes the law identity. Adding attack cases, property
tests, differential tests, or other verification coverage without changing the law does not.

The determinism law is:

```text
same ratified ContractImage
+ same immutable admitted Input presentation
+ same declared Budget and Capacity world
= same canonical representative
+ same canonical bytes
+ same Canonicalization outcome and contract-owned attribution
```

The generated canonicalizer and byte emitter are implementation-axis machinery. They may be specialized, fused,
vectorized, allocation-disciplined, or replaced only when the canonical representative, exact bytes, outcome,
attribution, work walls, and state-visible behavior remain identical.

### 5.4. Lowering Contract

Lowering is the final contract boundary of the inbound airlock. It re-forms admitted external presentation as explicit
immutable Fact material that the core may use without knowing or trusting the external world.

```text
external presentation authority
-> one-to-one meaning-preserving refinement
-> sealed Fact material
-> core-entry handoff
```

Input, Admission, and selected Canonicalization operate over the contract-visible boundary presentation. Admission may
permit that presentation to continue, and Canonicalization may select its stable same-shape representative, but neither
action ends external-presentation authority. Successful Lowering removes that authority and completes the immutable
information surface selected by the flow's Fact Contract.

Lowering does not execute core computation, create a rich domain object model, derive a business Result, judge an
Invariant, perform a State Transition, or publish an outward presentation. Those are separate obligations or
implementation regions.

Lowering is not an arbitrary mapper and is not a judgment over material constructed by user-supplied transformation
code. The Lowering Contract declares the complete immutable formation obligation. The Kontrakt compiler derives one
semantic `LoweringPlan`, and a backend generates the one-to-one mapping realization from that plan. The user does not
implement the same translation a second time.

#### 5.4.1. Boundary Source and Core Fact Target

The Lowering Contract and its generated boundary realization are the only Lowering components allowed to know both the
selected boundary presentation and the Fact surface selected for the same flow.

```text
boundary side:
    selected Input presentation
    optional same-shape canonical representative

Lowering Contract:
    immutable explicit one-to-one source-to-Fact binding obligation

compiler/backend boundary implementation:
    derived LoweringPlan
    generated one-to-one mapping realization
    Fact-material seal

core side:
    declared immutable Fact surface
    sealed Fact material only
```

The selected Input schema remains the source schema even when Canonicalization is present because Canonicalization does
not change contract-visible coordinates. The selected representative values become the source values supplied to
Lowering. When Canonicalization is absent, the admitted Input values are supplied unchanged.

Lowering does not introduce an independent `CoreMaterial`, `OperationStart`, `LoweringTarget`, or candidate schema. Its
target is the Fact surface independently selected by the flow's `fact` slot. The Fact Contract declares the information
that may exist in the core; the Lowering Contract declares how this external presentation may form that information.

The Fact surface must not name, import, embed, extend, or retain a reference to the Input declaration, source DTO,
transport protocol, host object, Lowering declaration, framework context, adapter type, or source-coordinate handle. It
must be intelligible solely through its own Fact coordinates, sorts, presence, alternatives, relations, ordering,
bounds, schema, and version material.

The Lowering declaration may name both sides because it belongs to the airlock boundary. Its generated realization may
physically read the source and write a staging region because it is boundary implementation. Once sealed and handed into
the core, source type names, source coordinate names, getters, constructors, collection implementations, object
identity,
runtime references, and mapping machinery have no factual authority.

#### 5.4.2. Immutable Authoring Surface

*What Contract Is* requires the Lowering Contract to contain immutable data only. It contains no translation
implementation and no business algorithm.

The user declares the inert immutable Fact surface separately under the Fact role. The user then authors one
host-language Lowering declaration whose only meaning is the explicit one-to-one relation between the selected Input
coordinates and those Fact coordinates. The operation IDL does not contain mapping syntax. Its `lowering` slot names the
selected Lowering declaration, and its `fact` slot independently names the target Fact declaration.

The selected Lowering declaration may carry only a finite set of explicit coordinate pairs:

```text
one exact selected Input coordinate reference for each binding
one exact selected Fact coordinate reference for each binding
```

The selected Input and Fact surfaces are resolved from the flow context. They are not repeated as executable mapping
content inside the Lowering declaration.

Every binding is explicit. Equal spelling does not create a relation, and different spelling does not prevent one.
`input.account` and `fact.account` remain unrelated until the selected Lowering declaration binds them. This preserves
the independent language of the external presentation and the core's factual information.

The binding cardinality is fixed in V1:

```text
one selected Input coordinate
-> one selected Fact coordinate
```

One Input coordinate must not form multiple contract-visible Fact coordinates. Multiple Input coordinates must not be
combined to form one Fact coordinate. Such `1:N` and `N:1` derivations introduce cross-coordinate meaning and belong to
core computation. A backend may still split, pack, flatten, or combine physical storage behind the same ratified
one-to-one contract relation; physical layout does not change mapping cardinality.

The selected source and target sorts may be identical. When an ordinary primitive, string, enum, or other closed
immutable type already expresses the required Fact coordinate, Kontrakt does not require a second wrapper type or a
Kontrakt-specific core representation. The host declaration is frontend evidence; ratified Fact material is the
authority; backend storage remains replaceable.

The selected source and target sorts may also differ. Kontrakt may refine an external presentation sort into a Fact sort
only when one complete, deterministic, versioned, meaning-preserving refinement profile is defined for that exact
source-target pair. The user declares neither the conversion algorithm nor a formation-law symbol. The absence of a
supported profile rejects the binding. More than one applicable profile is ambiguous and also rejects the binding.

A type change is not required merely because material crosses into the core. If the admitted or canonical presentation
already has the exact information and immutable form declared by the Fact Contract, Lowering may be a logical authority
handoff with no new user-visible type, no new object, and no physical copy.

This ADR deliberately does not freeze the exact Java or Kotlin token spelling of the immutable coordinate-binding
carrier. Whatever host-language syntax is selected must contain only the explicit source-target relations above, must be
selectable by its class or object symbol from the IDL `lowering` slot, and must be completely acquired and erased before
ContractImage authority begins. It must not require a mapping body in IDL, a generated coordinate API as authority, a
user-written mapper, callback, constructor procedure, or target class created only for Lowering.

The frontend rejects every executable surface, including:

```text
T -> U transformation methods
method bodies, custom getters, init blocks, factories, or constructor procedures
callbacks, lambdas, predicates, comparators, or user-defined equality
control flow, loops, recursion, pattern-matching implementations, or rewrite functions
repository, service, framework context, state store, filesystem, network, process, clock, or randomness access
runtime registry lookup, reflection, dynamic dispatch, dependency injection, or class initialization
host-library parsing, normalization, serialization, or conversion calls
mutable, lazy, delegated, captured, global, or thread-local state
core computation, Operation Result Material derivation, or Publication
```

A `pure` user function is still prohibited. Referential transparency does not make implementation code a declaration of
which external coordinate corresponds to which Fact coordinate, whether the selected source and target sorts preserve
the same meaning, which bounds hold, or which refusal belongs to the contract. Permitting the function would restore
implementation as contract authority and would make the airlock's final formation step an opaque call.

#### 5.4.3. Core Fact Surface

A Fact surface declares explicit immutable information available to the core machine. It is not a domain service, active
record, entity, aggregate, behavior-bearing Value Object, command handler, framework message, repository row, or backend
storage record.

A host frontend may use primitive types and record-like declarations to nominate Fact coordinates. Those declarations
must contain no method, validation, normalization, derived access, capability, lifecycle behavior, inheritance,
framework annotation, mutable property, custom equality authority, or construction logic. The frontend extracts and
erases their declared information shape.

```text
Fact surface:
    declares what immutable information exists for the core

core realization:
    reads explicit Facts and applies replaceable implementation

backend layout:
    decides how Fact material is physically represented
```

A primitive host type is not too weak merely because it is primitive. If `Int`, `Long`, `Boolean`, `String`, a closed
enum, or a finite immutable product preserves the required distinctions, it is a valid frontend surface. Kontrakt must
not manufacture `KontraktInt`, `CoreString`, or nominal wrappers merely to make the material look more internal.

Conversely, a user-authored class name does not make a Fact richer or more authoritative. Factual meaning belongs to the
ratified Fact Contract and its coordinates, not to constructors, methods, object identity, inheritance, or a rich domain
object graph.

A backend is free to realize the same Fact surface through primitive arrays, packed bytes, object arrays, off-heap
regions, generated static tables, or another deterministic layout. Flattening, allocation removal, pointer elimination,
fixed-width packing, and cache-oriented layout are backend techniques. They do not define Fact meaning and must remain
replaceable behind the ratified surface.

#### 5.4.4. Representation Refinement, Not Core Computation

Lowering changes representation and authority domain without deriving new cross-coordinate business meaning. At the
contract-visible boundary, V1 forms each Fact coordinate from exactly one explicitly bound Input coordinate.

Permitted one-to-one refinement families include:

```text
declared external scalar presentation
    -> immutable Fact scalar of the same declared meaning

nullable or optional presentation
    -> one explicit optional Fact coordinate

closed external alternative
    -> one explicit finite-alternative Fact coordinate

immutable sequence, set, bag, or map presentation
    -> one bounded immutable Fact collection coordinate under the same declared collection meaning

nested source coordinate path
    -> one explicitly bound Fact coordinate

declared external identifier or reference presentation
    -> one Fact representation of that same declared identifier or reference meaning
```

A refinement may decode, parse, range-check, copy, freeze, replace host collection mechanics, make an already-declared
presence distinction explicit, or select a fixed internal representation. Those actions remain Lowering only when the
Input Contract already declares the meaning being represented and the source-target profile preserves that meaning. A
generic text coordinate does not become a date, account identity, money, or another business concept merely because a
parser can produce such a value.

The following cardinalities are prohibited in V1:

```text
one source coordinate -> multiple contract-visible Fact coordinates
multiple source coordinates -> one contract-visible Fact coordinate
```

The prohibition is semantic, not physical. A backend may decompose one Fact coordinate into several machine words or
pack several coordinates into one region. Those choices remain backend layout and do not create `1:N` or `N:1`
Lowering relations.

Lowering must not create a new business proposition, consult mutable current machine state, execute core computation,
produce Operation Result Material, decide whether movement is legal, evaluate an Invariant, select a Transition, grant
Publication authority, apply business policy, infer missing material, combine coordinates, or split one coordinate into
several
contract-visible meanings. The following are not Lowering:

```text
birth date -> current age
price + customer grade -> discounted price
score -> risk category
year + month + day -> business date coordinate
name text -> database lookup -> current company identity
timestamp -> separate business-visible seconds and nanos coordinates
current balance - withdrawal amount -> new balance
Fact set -> Result
Result -> outward presentation
```

The distinction is:

```text
Lowering:
    one boundary coordinate -> one immutable Fact coordinate of the same declared meaning

core realization:
    combines, separates, and computes over explicit Facts and other declared immutable material

Invariant:
    judges declared laws and relations over explicit Facts and declared Operation Result Material selected for a Fact
    path

State and Transition:
    govern legal movement and the availability of material in a machine condition

Publication:
    governs the permitted outward claim
```

Lowering must be total over each declared one-to-one refinement domain using only the bound admitted or canonical source
coordinate and explicit Budget, Capacity, Version, and Governance material where those contracts lawfully supply a gate.
If a Fact coordinate requires information not present in its one bound source coordinate, it cannot be formed by
Lowering. Kontrakt does not prescribe or observe what occurred before Input, and the generated Lowering realization does
not enrich itself through hidden lookup.

A declared reference may cross Lowering only as one explicit source coordinate refined into one explicit Fact coordinate
of the same declared reference meaning. Resolution against another coordinate, a mutable registry, or current core state
belongs to core realization or a later judgment and must not be disguised as formation.

#### 5.4.5. Definition-Time Refinement and LoweringPlan

The selected `lowering` slot names exactly one immutable Java or Kotlin Lowering declaration for the flow. The IDL
selects that declaration by symbol only; it contains no mapping body. The slot grants the role, and the selected class
or
object symbol remains an acquisition coordinate only. The exact host-language carrier syntax remains deferred, but it
must express explicit one-to-one coordinate bindings and must not introduce an independent target schema or executable
mapper.

Before ContractImage publication, the frontend/compiler must resolve and ratify at least:

```text
the flow-selected Input schema
the selected Canonicalization law, when present
the selected Fact Contract and its coordinate surface
every explicit Input-coordinate to Fact-coordinate binding
one source coordinate and one target coordinate for every binding
explicit binding even when source and target names are equal
source-sort and Fact-sort compatibility
one unique supported meaning-preserving type-refinement profile for each binding
Fact-coordinate completeness and uniqueness
Input source-coordinate uniqueness across bindings
finite depth, cardinality, intermediate storage, output, and work bounds
applicable schema, Version, and Governance material
refusal and cross-contract stop attribution
```

Every selected Fact coordinate formed by this airlock must be formed exactly once under one complete flat Lowering
Contract, and every Input coordinate may appear in at most one binding. An Input coordinate may remain unused; that does
not implicitly create a binding or require the core Fact surface to expose external material it does not need. Silent
target defaults, fallback constructors, same-name auto-mapping, structural guessing, package scanning, annotations,
assignability, inheritance, and discovery from implementation shape are prohibited.

The frontend must reject missing or duplicate target formation, reused source coordinates, unknown source or target
coordinates, `1:N` or `N:1` relations, incompatible sorts, absent or ambiguous type-refinement profiles, hidden absence,
recursive host-object traversal, unbounded collection work, environment-dependent meaning, mapping syntax placed in the
IDL, and any executable member in the selected declaration or Fact surface.

The accepted declaration is lowered into one flat, immutable, adapter-erased Lowering Contract material. The Lowering
Contract identity includes the source-presentation schema identity, optional Canonicalization law identity, target Fact
Contract identity, the canonicalized explicit one-to-one binding set, resolved type-refinement profile identities and
versions, bounds, refusal law, and relevant flow-world coordinates. The authoring declaration is then erased. This is
the identity of the contract definition, not an identity imposed on every Fact value.

From that ratified material, the compiler derives one immutable semantic `LoweringPlan`. The plan closes the exact
source read set, Fact write set, type-refinement dependencies, traversal structure, bound propagation, refusal branches,
seal requirements, cache dependencies, and backend layout requirements that are legitimately implied by the contract.
The plan does not acquire contract authority of its own; it is a compiler-owned implementation artifact derived from the
ratified obligation.

#### 5.4.6. Generated One-to-One Mapping and Fact Seal

A backend generates the complete Lowering realization from the ratified `LoweringPlan`. Runtime performs no user mapper
invocation, reflection, symbol lookup, coordinate-name search, type-refinement registry lookup, DI resolution, callback
dispatch, or host operation interpretation.

The generated realization is airlock implementation. It may read the submitted Input presentation or selected canonical
representative and may construct temporary Fact staging material, but it is not core computation and holds no contract
authority. The core may observe only sealed Fact material after the legal handoff.

The generated realization may use temporary mutable builders, scratch buffers, offset tables, sorting workspaces, or
staged regions. Those objects belong exclusively to implementation. They hold no factual authority and must never be
exposed to the core.

Fact formation is complete only after the material is sealed. The seal requires that:

```text
every declared target Fact coordinate is complete exactly once
the material conforms to the selected Fact Contract
every coordinate was formed from its one explicitly bound Input coordinate
every source-target pair used its one ratified meaning-preserving type-refinement profile
the Fact material is immutable and no mutable external alias remains
presence, alternatives, relations, and ordering are explicit where the Fact surface declares them
applicable schema, Version, and Governance material is bound
all declared bounds and cross-cutting gates have succeeded
no back-reference to the external presentation or generated staging machinery remains
```

Allocation or builder completion alone does not create the seal. A partially filled target, mutable staging object, host
wrapper, source-backed view, or generated mapper object is implementation material and cannot enter the core as Fact.

Because the compiler owns the complete plan, the backend may derive mechanically sympathetic realizations including:

```text
Canonicalization-Lowering pass fusion
intermediate presentation elimination
dead source-coordinate read elimination
direct source access and direct Fact-region writes
exact buffer and region sizing
single-allocation or bounded-allocation formation
primitive and finite-alternative specialization
packed relation and presence layouts
stable ordering and type-refinement specialization
structural cache keys from the exact declared read set
partial Fact-material reuse under the same schema, law, and version world
AOT-generated formation and seal code
```

The same ratified material also supplies automatic verification and test generation: target-totality checks,
source-target compatibility checks, one-to-one binding checks, type-refinement conformance vectors, refusal-path tests,
determinism tests, backend-equivalence tests, boundary-value tests, and regression tests for generated artifacts.
Generated mapping, verification, tests, diagnostics, and optimization are different artifacts derived from one
obligation; none becomes an independent source of meaning.

The determinism law is:

```text
same ratified ContractImage
+ same immutable admitted Input presentation
+ same selected canonical representative, when present
+ same declared Budget, Capacity, Version, and Governance world
= same Lowering outcome
+ same sealed Fact material
+ same contract-owned refusal or stop attribution
```

The generated realization may be fused with upstream Kontrakt-owned canonical production or with the first core read
only
when the same logical Lowering boundary, explicit one-to-one relations, outcome, immutable Fact material, attribution,
and state-visible handoff remain explicit. Physical fusion must not allow the core to observe external presentation or
allow implementation staging to acquire factual authority. Kontrakt V1 does not rewrite or optimize arbitrary core
implementation code.

#### 5.4.7. Refusal and Defect Boundary

Lowering refuses when the declared Fact material cannot be formed and sealed. Representative cases include:

```text
required bound source material is absent
no unique supported meaning-preserving type refinement exists for the bound source-target sorts
a source or target coordinate is missing, duplicated, or reused illegally
an alternative cannot be represented by the selected Fact surface
an explicitly declared one-coordinate reference representation cannot be formed
Fact-coordinate completeness cannot be achieved
a depth, cardinality, output, or work bound is exceeded
applicable schema, Version, or Governance material is incompatible
```

A Budget or Capacity wall retains the result and diagnostic authority of the supplying contract. It is not rewritten as
a Lowering refusal. A backend that produces material inconsistent with the ratified Lowering law is defective; user
source is not blamed for an implementation error in compiler-generated machinery.

Lowering refusal, core implementation failure, Invariant refusal, and Publication refusal are distinct:

```text
Lowering refusal:
    no formation-valid sealed boundary Fact exists

core implementation failure:
    required Facts exist,
    but the replaceable realization does not produce its declared Operation Result Material

Invariant refusal:
    declared Operation Result Material exists and has entered a selected Fact Candidate path,
    but a selected law does not hold

Publication refusal:
    core material exists,
    but the declared outward claim is not permitted
```

#### 5.4.8. Core Entry Handoff

Successful Lowering yields immutable, formation-valid, contract-governed Fact material. The core receives only that
sealed information under the legal core-entry movement. It does not receive the Input object, canonical presentation
object, Lowering declaration, source-to-target binding table, `LoweringPlan`, generated mapper, staging object, or
host-language execution context.

```text
external presentation authority:
    ended

boundary Fact material:
    formed and sealed

core visibility:
    granted only by the legal handoff

core computation:
    not implied by Lowering

Result:
    not yet produced

new Fact material:
    not yet produced

publication authority:
    not yet granted
```

The core may now use that boundary Fact together with other explicit Facts and applicable Policy, Budget, Capacity,
Version, Governance, and State material. How replaceable implementation derives declared Operation Result Material from
that explicit material is outside this Lowering Contract. Whether the produced material later enters a selected Fact
Candidate path and receives Fact authority is also outside Lowering.

ADR-0049 begins from Fact as explicit core information. It defines the Fact role, Invariant judgment over explicit
material, the relationship between Operation Result Material and Fact authority, and Publication. It must not reopen the
external presentation, repeat Lowering, or derive authority from the erased source declaration or generated mapping
implementation.

## 6. Cross-Profile Boundaries

### 6.1. Three-Axis Coordination

The contract, implementation, and state pipelines are parallel authorities over one selected flow.

```text
Contract pipeline:
    declares material, judgment, and authority

Implementation pipeline:
    realizes boundary formation and performs replaceable core work

State pipeline:
    permits and records legal movement and availability
```

No axis substitutes for another. Generated formation does not make its staging objects factual. Core implementation does
not make its classes or return values contract authority. A successful contract judgment does not move the machine
unless the declared transition is legal. A state label does not prove that either implementation work or contract
judgment occurred.

Input and Admission are directly adjacent because Admission judges the immutable presentation formed under Input without
an intervening user transformation. When Canonicalization is selected, Admission and Canonicalization likewise have no
user-supplied transformation region: a Kontrakt-owned realization applies the selected representative law to that same
contract-visible shape. When Canonicalization is omitted, Admission hands the same presentation directly to Lowering
without an inserted contract, callback, proxy, or interceptor. Lowering then uses immutable explicit one-to-one
coordinate bindings, compiler-resolved meaning-preserving type refinements, a derived `LoweringPlan`, and a
backend-generated realization to seal the selected boundary Fact material. No user mapper region exists inside the
airlock.

### 6.2. Earliest Authoritative Rejection

Each of these contracts stops only the material it has authority to judge.

Input stops material that cannot be formed as the declared immutable boundary presentation. Admission stops formed
material that may not continue. When selected, Canonicalization refuses admitted material only when its law cannot
produce one unique bounded representative within its declared domain. A Budget or Capacity wall crossed during selected
canonical production retains the supplying contract's result. When Canonicalization is omitted, no Canonicalization
refusal is possible. Lowering refuses when its immutable declaration and permitted source material cannot form and seal
the selected boundary Fact material.

A condition must have one owning contract. Executing the same condition in multiple stages is not defensive safety; it
is duplicate authority and duplicate cost.

### 6.3. Input Refusal and Admission Rejection

Input refusal and Admission rejection are different results.

```text
Input refusal:
    no judgeable immutable boundary presentation was formed

Admission rejection:
    a judgeable immutable boundary presentation was formed,
    but continuation was not permitted
```

Admission must not run after Input refusal. Conversely, Input must not reject material merely because Admission will
later
reject it.

### 6.4. Optional Canonical Production and Core Fact Entry Authority

When Canonicalization is selected, its production is owned by Kontrakt because the representative law fully determines
its permitted result. Fact-forming shape or type refinement remains outside Canonicalization and is declared by
Lowering.

```text
selected Canonicalization:
    admitted presentation
    -> Kontrakt-generated Canonicalization realization
    -> stable same-shape representative
    -> exact canonical bytes
    -> compiler-derived LoweringPlan
    -> backend-generated one-to-one mapping realization
    -> immutable boundary Fact formation
    -> Fact-material seal
    -> legal core-entry handoff

omitted Canonicalization:
    admitted presentation
    -> compiler-derived LoweringPlan
    -> backend-generated one-to-one mapping realization
    -> immutable boundary Fact formation
    -> Fact-material seal
    -> legal core-entry handoff
```

In the selected branch, Canonicalization owns representative meaning and grants authority to the result produced by its
generated realization. In the omitted branch, no semantic representative authority is granted and the admitted Input
remains the Lowering source. Neither branch permits an arbitrary user canonicalizer, mapper, callback, proxy,
interceptor, DI object, or transformation function inside the airlock.

Lowering owns the explicit one-to-one source-to-Fact binding obligation and the Fact seal in both branches. Its
host-language authoring declaration is immutable data only, while the IDL selects that declaration by symbol and
contains no mapping body. The compiler resolves each supported source-target type refinement, derives the semantic plan,
and the backend-generated mapping is implementation machinery. That machinery is wrong if it introduces undeclared
meaning, observes hidden state, preserves external authority, performs core computation, or produces Fact material that
disagrees with the ratified Lowering and Fact contracts.

### 6.5. Cross-Cutting Gates

Policy, Governance, Budget, and Capacity are selected once at the enclosing interface scope for the closed set of
operations. They coordinate finite resources and decisions shared by those operation pipelines; they are not
operation-manifest slots and are not attached to internal implementation stages. Their declarations may contain both
machine-wide walls and explicit operation allocations or run-grant profiles.

At operation start, the applicable Governance world, Policy material, Capacity allocation, and Budget grant are resolved
and fixed for the run. Version or another cross-cutting contract may also be bound where its own scope requires it. A
gate owned by any of those contracts may be executed during an Input, Admission, selected Canonicalization, Lowering, or
core implementation region when the needed evidence becomes available.

Early execution does not transfer authority. A Capacity stop observed during Input remains a Capacity result. When
Canonicalization is selected, a Budget or Capacity stop observed during generated canonical production remains the
supplying contract's result rather than a Canonicalization refusal. The supplying contract owns the declared outcome and
its diagnostic attribution, and a later boundary must not repeat the same gate. Physical fusion with implementation does
not make the implementation function, stage, or call graph the scope or owner of the contract.

### 6.6. V1 Implementation Optimization Boundary

Kontrakt V1 does not optimize, rewrite, fuse, devirtualize, or otherwise reinterpret arbitrary user-supplied core
implementation code. That implementation remains a replaceable realization region. Kontrakt governs which Facts may be
visible, which declared Operation Result Material may be produced, whether a selected Fact path may grant Fact
authority,
which failures are declared, and which state movements and Publication judgments are legal, but it does not make
implementation classes, functions, dispatch, allocations,
effects, or object layout contract authority.

Slot-selected Admission, selected Canonicalization, and Lowering declarations are restricted frontend sources, not
opaque implementation callbacks. Admission may use a finite judgment-expression source. Selected Canonicalization uses
either one closed built-in law symbol or one uninstantiable coordinate-to-nominal-type signature declaration. Lowering
uses only a finite immutable host-language declaration of explicit one-to-one Input-to-Fact coordinate bindings. The
Fact surface is declared independently. Machine-scope Policy, Governance, Budget, and Capacity, together with
Version where applicable, remain separate contract material. The IDL only selects the Lowering declaration; it contains
no binding body.

Kontrakt resolves the unique supported meaning-preserving source-target type refinement for each binding, erases the
source forms completely before publication, then owns and optimizes the generated evaluator, canonicalizer, canonical
byte emitter, compiler-derived `LoweringPlan`, mapping realization, Fact seal, generated verification and tests, cache
plan, and associated deterministic state machinery. Canonicalization omission generates no replacement canonicalizer or
runtime interception point.

V1 optimization is concentrated on machinery Kontrakt owns: contract acquisition, deterministic planning, automatic
mapping generation, frozen material, generated verification gates, state enforcement, automatic test generation and
execution, diagnostics, structural cache planning, and publication control. These facilities remove mapping,
validation, test, diagnostic, and control boilerplate that users would otherwise build repeatedly. Because all of them
are derived from the same rich contract material, Kontrakt can perform whole-pipeline optimization that an opaque mapper
or ordinary validation framework cannot safely perform. They are the SOTA-grade optimization target of V1.

Optimization of arbitrary core implementation requires different authority and analysis over effects, aliasing, escape,
ownership, identity observability, concurrency, and representation. It is deferred to a later version and must not
expand the V1 runtime or contract surface. If introduced later, its implementation plan must remain derived and
replaceable; it must not become Fact or contract authority.

### 6.7. Handoff to the Explicit Core

Successful Lowering means that every explicit one-to-one binding and its ratified meaning-preserving type refinement has
been realized through the compiler-derived plan and backend-generated mapping, the selected boundary Fact material has
been completed and sealed, and external-presentation authority has ended.

The legal core-entry handoff makes that immutable information available to the explicit core machine. It does not select
or invoke one mandatory Operation object, does not require an Operation Start DTO, and does not imply that declared
Operation Result Material has already been produced or that Fact authority has been granted.

The core thereafter operates only over explicit Facts and other declared immutable contract material under the
machine-wide Policy, Governance, Budget, and Capacity material fixed for the selected operation run. Internal functions
or stages remain replaceable implementation and do not open another airlock or contract pipeline. ADR-0049 defines the
Fact role, Invariant judgment, the relationship between Operation Result Material and Fact authority, legal availability
of material that has acquired Fact authority, and Publication. ADR-0049 must not reopen the erased Input or Lowering
declaration as sources of core meaning.

## 7. Deferred Decisions

This ADR does not freeze the final token spelling, exact public canonical type names, shape-directed generic type names,
or the exact Java or Kotlin carrier syntax for host-facing Input, Admission, Canonicalization, Lowering, or Fact
declarations.

It does fix the Canonicalization authoring boundary: V1 may omit the `canonical` slot, select one complete flat built-in
symbol, or select one uninstantiable Java/Kotlin coordinate-law signature declaration. Omission declares no semantic
Canonicalization Contract and causes no implicit `ExactCanonicalization`, user callback, proxy, interceptor, or
generated replacement stage. A selected declaration must cover every selected Input coordinate exactly once by
parameter name and an applicable exact Kontrakt-owned nominal canonical type, including finite shape-directed generic
type arguments where required. V1 exposes no canonical-law values, enums, singleton objects, constructors, factories,
application-defined canonical types, user-authored canonical output presentation, transformation method, `T -> T`
canonicalizer, or shape mapping. Each public canonical type must be accompanied by API specification and user
documentation derived from the same versioned law material.

It also fixes the Lowering authoring boundary. The `lowering` slot selects one immutable Java or Kotlin declaration by
class or object symbol. The IDL contains no mapping body. The selected declaration explicitly binds every target Fact
coordinate formed by the inbound airlock to exactly one selected Input coordinate, even when their names are equal.
Each Input coordinate may appear in at most one binding. V1 permits no `1:N` or `N:1` contract-visible relation and no
same-name inference.

The compiler resolves one unique supported meaning-preserving type-refinement profile from each bound source sort to its
target Fact sort; the user does not author or select conversion code or formation-law symbols. The source and target
sorts may be equal, and no new user-visible type or Kontrakt-specific wrapper is required merely because material enters
the core. Lowering introduces no independent core target schema, Operation Start schema, rich domain object model, or
Result schema.

V1 exposes no user mapper, `T -> U` function, callback, lambda, strategy object, constructor procedure, runtime plugin,
DI surface, repository access, state lookup, application-defined formation law, executable target type, core
computation, Operation Result Material production, or source-backed Fact view. The exact host-language carrier syntax
remains a frontend decision, but the declaration must be finite, immutable, fully ratifiable, and completely erasable
before ContractImage
publication. From the ratified material, the compiler must derive the complete semantic `LoweringPlan`, and the selected
backend must generate the mapping realization. V1 does not permit a user-authored runtime mapper as an alternative
execution path.

This ADR does not define the complete authoring surface by which a core pipeline declares all Fact dependencies, Result
surfaces, or the implementation realization between them. It fixes only the inbound airlock and the fact that the core
may depend on explicit immutable information rather than hidden object state.

It does not define the complete state and transition sets of the three-axis machine. State-machine manifests,
cross-contract movement, diagnostic evidence production, retention, failure representation, and Version remain
operation-flow decisions that must be designed across the complete flow rather than independently inside one processing
profile. Policy, Governance, Budget, and Capacity are instead bound once at the enclosing interface scope for the closed
set of operations because they coordinate shared finite resources. Their declarations may include machine-wide walls
and explicit operation allocations or run-grant profiles, but their complete processing languages remain outside this
ADR. They are not operation-manifest slots and do not acquire authority from implementation-stage placement.

It does not define a projection callback SPI. Mutable and framework-owned objects must be converted before Input through
an explicit adapter or presentation-formation operation, but the final adapter, factory, builder, or generated formation
surface and compatibility profiles remain frontend decisions. Direct transitively immutable Kotlin data classes and Java
records require no second presentation declaration.

It does not select the final internal IR classes or backend data structures. Those implementations must realize the
authority and handoff fixed here without becoming new sources of meaning.

Allocation elimination, callback removal, cross-region fusion, and optimization of arbitrary unrelated core code are
intentionally outside V1. Kontrakt may optimize the compiler-derived `LoweringPlan`, generated mapping, Fact seal, cache
plan, and backend layout because their complete meaning is derived from ratified contract material. A later version may
investigate an implementation IR or restricted frontend for core realization, but that work requires separate
authority, analysis, and ADRs.

## 8. Consequences

Boundary refinement and core entry now have explicit contract owners while the flow remains a three-axis machine. The
contract pipeline declares material and judgment, the implementation pipeline realizes boundary formation and performs
replaceable work, and the state pipeline permits legal movement and availability. None can impersonate another.

Input accepts explicitly formed immutable presentation material. Kontrakt does not repair mutable, lazy, proxied, or
framework-owned objects by taking a runtime snapshot. A transitively immutable Kotlin data class or Java record may
serve directly as both host source evidence and runtime presentation without a duplicate declaration. Its record-like
shape is refined into contract material, while constructor execution and generated class machinery remain implementation
artifacts. Other ordinary Kotlin and Java carriers require explicit presentation formation before the Input boundary.

Input and Admission share one immutable presentation and require no user transformation between them. Kontrakt treats
that submitted presentation as the flow's raw Input and makes no claim about values or processing that may have existed
before submission. After Admission, a selected Canonicalization law produces the stable representative and canonical
bytes. If the `canonical` slot is omitted, no semantic canonicalization occurs and the admitted presentation is supplied
unchanged to Lowering.

In both branches, the selected immutable host-language Lowering declaration provides explicit one-to-one
Input-to-Fact coordinate bindings. The compiler resolves the unique supported meaning-preserving type refinement for
each binding and derives one semantic `LoweringPlan`; the backend generates the mapping and Fact seal. Equal coordinate
names create no implicit binding. No user transformation call, mapper, callback, proxy, independently authored lowering
implementation, mandatory core DTO, or Kontrakt-specific scalar wrapper exists.

Invalid or unratifiable material stops before later work pays its cost. Input formation failure does not reach
Admission. Admission rejection reaches neither selected canonical production nor Lowering realization. When
Canonicalization is selected, its refusal or an attributed Budget or Capacity stop during canonical production does not
enter Lowering. When it is omitted, an admitted presentation proceeds directly to the generated Lowering realization.
Lowering refusal means that no formation-valid sealed boundary Fact exists.

Successful Lowering does not hand material to one mandatory Operation object. It completes the selected operation's
inbound airlock. An enclosing interface may expose several operation pipelines, but their successful Lowering handoffs
enter the same explicit core under the shared machine-wide Policy, Governance, Budget, and Capacity contracts. The core
sees immutable Fact information and no external presentation. Core realization may use those Facts with existing Facts
and other explicit contract material to produce declared immutable Operation Result Material. When a Fact Contract is
selected for that result, the same material may enter the Fact Candidate path and receive Fact authority only after the
applicable Invariant and legal State Transition. Its internal functions, objects, stages, and call graph remain
implementation and do not become nested IDL operations.

Fact richness no longer depends on a rich object model. Ordinary primitives and finite immutable products may nominate
Fact coordinates when they preserve the required information. The Fact Contract, not a class constructor, method,
custom equality implementation, Value Object name, or backend layout, owns factual meaning.

Canonicalization authoring exposes no executable law API. Users place only Kontrakt-owned nominal canonical type names
in an uninstantiable coordinate signature, while the API specification and user documentation explain the meaning of
each name. The frontend resolves exact symbols, erases the host signature, and grants authority only to the resulting
flat versioned law material.

V1 leaves arbitrary core implementation opaque and does not optimize its callbacks, allocations, dispatch, control
flow, or effects. Lowering declarations are not part of that opaque region: they contain no implementation. Kontrakt
owns the compiler plan and generated airlock realization and may optimize them without changing the ratified Fact
material. This removes handwritten mapper boilerplate while enabling pass fusion, direct layout generation, structural
caching, allocation control, and other SOTA-grade specialization.

Kontrakt also derives verification, automatic tests, diagnostics, deterministic planning, state enforcement, frozen
material, generated gates, caching, and publication control from the same explicit contract source. The existing frozen
acquisition, planning, contract graph lowering, identity, and ContractImage machinery remain the physical basis. This
ADR adds role-specific law and three-axis coordination, not a parallel metamodel compiler, hidden snapshot layer, rich
domain object model, or implicit implementation machine.