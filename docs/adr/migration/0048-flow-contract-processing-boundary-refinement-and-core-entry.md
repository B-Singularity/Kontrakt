# ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Status

Migration Pending

## Date

2026-07-12

## Related

- `docs/what-contract-is.md`
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
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

The operation manifest selects operation-local one-dimensional roles through explicit slots. A source does not declare
itself to be `Input`, `Admission`, `Canonicalization`, or `Lowering`. The operation supplies the boundary, and the slot
supplies the role. Lowering is one of the two V1 coordinate-relation exceptions fixed by ADR-0046 and ADR-0047: its
manifest slot selects the role, while one sibling `lowering` declaration inside the same operation states the exact
source-to-target relations.

A single `.kontrakt` interface scope may declare a closed set of operations that enter the same machine. `Policy`,
`Governance`, `Budget`, `Capacity`, `facts`, and `invariants` are bound once at that enclosing scope. The resource
contracts coordinate decisions and finite resources shared among those operations. `facts` declares the closed Fact
vocabulary of the core, and `invariants` declares its standing Fact laws. None is repeated as an operation-manifest
slot. Each operation manifest continues to declare its own flow, failure, movement, version, and diagnostics.

The remaining problem is processing.

The four contract roles addressed here form the inbound airlock of the machine. `Input` establishes judgeable immutable
boundary presentation. `Admission` decides whether that presentation may continue. `Canonicalization` optionally
produces its stable representative under a selected equivalence law. `Lowering` removes external-presentation authority
and forms complete candidate material for the ordinary Operation parameter types. The corresponding Fact kinds come from
the enclosing interface's `facts` declaration, and their standing laws come from `invariants`. Only after the applicable
judgments and legal movement succeed does the generated machine establish input Fact authority and invoke the ordinary
user Operation.

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
performs work behind declared boundaries. The state pipeline declares when each judgment, realization, refusal, seal,
and handoff is legally available. JVM objects, interfaces, callbacks, and intermediate allocations are frontend and
implementation mechanisms; they are not the authority model of the machine. Slot-selected Lowering consists of explicit
immutable IDL relation material plus one replaceable realization behind a generated plain host-language port. The
relation owns the permission and dependency. The supplied implementation owns physical formation only and must never
become contract authority.

Input and Admission are a direct contract adjacency in this ADR. Input establishes the immutable presentation that
Admission judges, so no user implementation transformation exists between them. Input enters the declared external
boundary, not the core. Kontrakt neither owns the surrounding user system nor observes or classifies processing that may
have occurred before the Input presentation was formed; the submitted presentation is the raw Input material of this
flow.

The `canonicalization` slot is optional in V1. When it is selected, Admission and Canonicalization have no user-supplied
transformation region: a Kontrakt-owned realization applies the selected Canonicalization law to the same
contract-visible presentation and produces its stable representative. When it is omitted, Kontrakt inserts no semantic
Canonicalization Contract, no `ExactCanonicalization`, and no user callback, proxy, or interceptor. The admitted Input
presentation passes unchanged to the selected Lowering formation.

In both branches, Lowering is declared through immutable one-to-one coordinate bindings authored in one operation-local
`lowering` body beside the operation manifest. Each target address begins with an Operation parameter slot and continues
to one coordinate of that parameter's declared Fact kind. The compiler derives one semantic `LoweringPlan`, generates a
retained plain host-language realization port, and requires exactly one implementation during machine assembly. It does
not silently close that port through equal names, equal host types, catalog lookup, or backend convention. The generated
pipeline invokes the supplied realization, forms complete candidate material, applies the standing judgments selected by
the resolved Fact kind and movement world, and grants input Fact authority only after those obligations succeed.

This ADR defines the processing profiles for those four contracts, their relationship to the implementation and state
axes, and the handoff from untrusted external presentation to explicit immutable core information.

ADR-0049 defines Fact, Invariant, Publication, and Output Presentation. Fact is not merely the result of an
implementation operation and is not a host wrapper added by Lowering. The manifest-selected Input Presentation does not
enter the user Operation directly. Lowering forms ordinary host material for the Operation parameter types, and Kontrakt
applies the interface-level Invariants and applicable movement judgments before that material receives input Fact
authority. The implementation still receives ordinary Java or Kotlin values. Core realization may produce declared
Operation Result Material and an ordinary Operation return, while ADR-0049 governs result establishment, outward claim
authority, and the separate outward presentation.

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
knows by executing implementation. That is the implicit machine Kontrakt rejects.

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
- permit Lowering authors to declare only finite immutable one-to-one bindings from selected Input coordinates to
  coordinates addressed through the selected Operation parameter slots, with no same-name inference, expression,
  callback, lookup, business algorithm, or executable implementation inside the IDL relation body;
- generate one retained plain host-language Lowering realization port from that relation and require exactly one
  explicitly supplied implementation, without silently selecting same-type copying, a conversion catalog entry, a naming
  convention, or backend behavior;
- allow ordinary primitive and closed immutable host types to nominate Fact coordinates when they already express the
  required information, without manufacturing Kontrakt-specific core wrappers or a rich object model;
- derive source-read closure, target coverage, port ABI, candidate sealing, judgment handoff, verification, generated
  tests, diagnostic attribution, cache dependency, and backend optimization from the ratified relation and explicit
  realization binding without treating implementation code as contract authority; and
- optimize Kontrakt-owned verification, canonical representative production, Lowering orchestration, testing, planning,
  state enforcement, diagnostics, caching, and physical layout, while optimizing the supplied Lowering implementation
  only where equivalent behavior is proven and otherwise preserving the explicit port call.

The result must be usable as software and still satisfy the discipline of *What Contract Is*.

## 3. Decision Drivers

The machine must reject as early as possible without moving a later contract's judgment into an earlier role.

A source declaration may nominate external contract evidence, but only ratified Kontrakt-owned material may receive
contract authority.

A user must not be required to declare the same contract twice. The operation-local IDL relation is the one Lowering
contract source. The generated port implementation is a replaceable realization of that declared relation, not a second
contract declaration.

The core must not read external DTOs, mutable objects, lazy values, proxies, framework contexts, repositories,
callbacks, or implementation-owned object graphs in order to discover information. Everything available to the core must
already exist as explicit immutable Fact material.

Policy, Governance, Budget, and Capacity must remain machine-scope contracts when they arbitrate finite resources shared
by several operation pipelines. Their declarations may state the total machine walls and explicit operation allocations
or grants, but they must not bind contract authority to internal implementation functions, stages, or call-graph shape.
The enclosing interface scope binds those four contracts once; an operation manifest does not repeat them.

Fact is information itself. It is not necessarily an implementation result, a domain event, a persistence row, an
entity, or a Value Object. A record-like host declaration may nominate Fact coordinates, but the declaration and its
object instances do not own factual meaning.

Input presentation must be explicit and immutable before it reaches Admission. Runtime snapshot timing, lazy
materialization, proxy activation, and framework lifecycle must not become hidden Input meaning. Lowering must likewise
be declared only through immutable finite relation material. The author explicitly binds each target Operation-parameter
Fact coordinate to exactly one selected Input coordinate, even when their names happen to match.

The selected source and target types may be identical. Lowering does not require a new target type, a new object, or a
Kontrakt-specific scalar wrapper. Equal host types do not close the realization port implicitly. When the types differ,
the supplied realization performs the declared representation formation explicitly; the compiler does not infer a
conversion merely from the type pair.

A supplied Lowering implementation may not become the authority that defines the relation, target participation, factual
meaning, or establishment. The generated port exposes only the declared realization boundary. The generated machine owns
stage order, candidate completeness, applicable Invariant and movement judgment, establishment, refusal routing, and the
handoff to the user Operation.

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
Operation Result Material surfaces, failure surface, and state participation are governed by explicit contracts.
Slot-selected Admission and Canonicalization declarations remain restricted immutable frontend source evidence that
Kontrakt must refine completely and erase or reject before ContractImage publication. Lowering relation material is
likewise fully ratified, but its physical representation formation is supplied through one generated port. Kontrakt may
inspect, devirtualize, inline, specialize, or erase that port only where the closed binding and implementation body
permit a proven equivalent path; otherwise the explicit call remains.

Kontrakt-owned validation, deterministic planning, generated port and assembly production, automatic test generation and
execution, state enforcement, diagnostics, cache planning, frozen material, and generated gates remain optimization
targets and must be implemented to a state-of-the-art performance standard. A rich contract declaration and closed
realization binding expose enough whole-pipeline knowledge for mechanically sympathetic specialization, pass fusion,
allocation control, structural reuse, and deterministic caching. Any optimization that changes declared meaning,
observes undeclared implementation state, or weakens determinism is invalid.

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

When `canonicalization` is omitted, the contract-axis handoff is directly from Admission to Lowering. This ordering
expresses authority handoff. It is not a complete execution pipeline and does not imply that one contract performs the
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

Lowering declares how that boundary presentation may form the ordinary Operation parameter material addressed by its
one-to-one relation body. Each parameter type resolves to one Fact kind from the enclosing interface's `facts`
declaration. Lowering does not define an independent Operation Start object, a backend layout, a domain object graph, or
a Result. The compiler derives the semantic plan and generated port ABI, and exactly one supplied realization forms
complete candidate material. Fact formation is complete only after the generated machine has verified candidate
completeness, applied the applicable interface-level Invariants and movement judgments, established input Fact
authority, and erased external authority. The state axis governs the legal moment at which that established material
becomes available to the user Operation.

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
    admitted Input coordinates are supplied unchanged through the generated Lowering port

Canonicalization -> Lowering realization, when selected:
    stable same-shape representative coordinates are supplied through the same generated port

Lowering realization:
    exactly one explicitly bound implementation forms complete candidate Operation-parameter material

Generated judgment and establishment path:
    verifies target completion and declared relation coverage
    applies every applicable interface-level Invariant and movement judgment
    establishes input Fact authority
    invokes the ordinary user Operation

Core realization:
    consumes established input Facts and other explicitly bound contract material
    performs replaceable implementation work
    produces its ordinary declared result
```

When Canonicalization is selected, a Kontrakt backend generates its realization because the ratified representative law
fully determines its permitted meaning. That generated code remains implementation and is wrong if it disagrees with the
canonical law or its canonical byte protocol. When Canonicalization is omitted, no replacement canonical realization is
generated; the admitted Input is simply the source material of Lowering.

In both branches, the selected Lowering declaration contains immutable explicit one-to-one coordinate bindings only. The
bindings do not rely on matching names and contain no conversion implementation. The compiler resolves the exact source
and target coordinates, derives the fixed `LoweringPlan`, generates one retained plain host-language realization port,
and closes exactly one supplied implementation into the machine assembly. The implementation may use ordinary reusable
libraries, but no library, equal type pair, naming rule, classpath discovery, or backend convention selects itself. The
relation remains contract authority; the implementation remains replaceable boundary machinery.

The internal realization that later consumes Facts may be one function, many functions, an object graph, an AOT plan, or
another replaceable implementation. Its structure is not the Fact Contract and is not defined by this ADR.

### 4.3. State Pipeline

The state pipeline runs in parallel with both other axes. It declares when Input formation, Admission judgment,
Canonicalization, Lowering realization, candidate completion, input Fact establishment, Operation invocation, ordinary
result availability, result Fact establishment, refusal, and Publication are legal.

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

The four profiles in this ADR end at established input Fact authority and the legal invocation of the ordinary user
Operation. They do not define the internal implementation graph and do not require an Operation Start DTO, candidate
wrapper, or established-Fact wrapper.

One interface scope may contain several operation pipelines. Each operation has its own external boundary flow, while
Policy, Governance, Budget, and Capacity coordinate the finite resources and decisions shared by the closed operation
set. The enclosing `facts` declaration names the closed Fact vocabulary once, and `invariants` names the standing laws
once. Operation manifests do not repeat either declaration. Successful Lowering from any operation establishes only the
input Facts explicitly addressed through that Operation's parameter slots; it does not divide the shared core or grant
universal Fact participation.

The inbound airlock is:

```text
Untrusted external region

Boundary Input
    -> Input judgment
    -> Admission judgment
    -> optional Canonicalization
    -> operation-local Lowering relation
    -> generated retained Lowering port
    -> exactly one supplied realization
    -> complete candidate Operation input material
    -> every applicable interface-level Invariant judgment
    -> every applicable movement judgment
    -> established input Fact authority
    -> ordinary user Operation invocation
```

The explicit core machine is:

```text
Established input Facts for the selected Operation
+ other explicitly bound existing Facts
+ machine-wide Policy, Governance, Budget, and Capacity material
+ explicit Version and State material
    -> ordinary replaceable user Operation
    -> ordinary declared result material
    -> proposed result Fact of the Operation return kind
    -> every applicable interface-level Invariant judgment
    -> every applicable State / Transition judgment
    -> established Operation return Fact
```

At operation start, Governance resolves the active contract world, Policy supplies the allocation and reaction law,
Capacity supplies the shared machine walls and the operation's applicable allocation, and Budget supplies the run grant.
The resulting material is fixed for that run before core work depends on it. Core realization may consume the granted
resources, but it does not define their limits, redistribute them by hidden implementation structure, or create a new
contract pipeline for each internal step.

The user Operation does not know the external Input declaration, transport names, serializers, canonicalizer source,
Lowering relation, source-coordinate bindings, generated port metadata, mutable carriers, or framework lifecycle. It
receives ordinary Java or Kotlin parameter values whose Fact authority has already been established outside their host
representation.

Fact is not restricted to the output of core realization. Boundary input Facts are established by the inbound airlock.
Existing Facts may already be available in the current core world and may participate only through explicit bindings.
Policy, Version, Governance, and other contract material may also be presented explicitly where their own contracts
require it.

The ordinary user Operation returns its declared host result without invoking Kontrakt. That returned material is not
successful contractual output merely because a method returned an object. Kontrakt treats it as proposed material for
the Fact kind resolved from the Operation return. Every applicable interface-level Invariant and movement obligation
must succeed before the result receives established Fact authority and the contractual Operation completes.

The outbound boundary is defined by ADR-0049:

```text
established Operation return Fact
    -> Publication judgment and explicit claim relation
    -> permitted outward claim or declared publication stop
    -> declared outward presentation and replaceable realization
```

Publication does not infer its source from runtime type, Operation Result Material, serializer availability, or carrier
shape.

A concrete flow may therefore be:

```text
WithdrawInputPresentation
    -> Admission
    -> optional Canonicalization
    -> WithdrawLowering relation
    -> WithdrawLoweringPort implementation
    -> WithdrawalRequest candidate
    -> WithdrawalRequest Invariant
    -> established WithdrawalRequest Fact
    -> ordinary withdraw Operation

The Operation also sees, through explicit participation:
    established AccountBalance Fact
    WithdrawalPolicy material

The Operation returns:
    WithdrawalRecorded candidate

Invariant:
    judges the complete WithdrawalRecorded candidate under the standing law for that exact Fact kind

State / Transition:
    governs the movement bound to the same proposed result

Successful establishment:
    grants WithdrawalRecorded Fact authority
    completes the contractual Operation

Publication:
    judges the established Operation return Fact and the explicitly declared outward claim
```

`amount text -> command.amountMinor` may belong to Lowering when the Input Contract already declares the amount meaning.
`current balance - requested amount -> recorded remaining balance` belongs to the user Operation. `recorded remaining
balance is non-negative` may belong to the standing Invariant for `WithdrawalRecorded` when that law is complete from
that one candidate Fact. Legal account movement belongs to State and Transition. The permitted external response belongs
to Publication and the outward presentation boundary.

Contracts may exist without an executable Operation implementation, but executable machine publication requires every
required generated port and Operation realization to be closed exactly once. An interface operation handle selects and
coordinates one external contract flow inside the enclosing machine scope; it does not become the source of Fact
authority and does not prescribe the shape of the internal implementation graph. Internal core work may be split, fused,
or reordered behind its declared obligations without becoming another IDL operation or recursively opening another
contract pipeline. A new operation is required only when the machine intentionally declares another external operation
boundary, not merely because an implementation contains another function or stage.

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
-> generated realization or explicitly bound generated-port realization behind that material
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
contract. Each successful judgment narrows or authorizes material before the next implementation region pays its
computation, allocation, normalization, lowering, or global coherence cost.

This authority path is not a mandatory physical runtime schedule. Kontrakt-owned reads, gates, canonical producers,
Lowering orchestration, candidate-completion checks, establishment, and state checks may be specialized or fused where
equivalent. The exactly bound Lowering realization may be optimized only where analysis proves an equivalent result;
otherwise its explicit generated-port call remains. Unrelated user-supplied implementation code remains outside that V1
optimization authority, and factual meaning, refusal attribution, core-entry handoff, and state movement must remain
explicit.

---

## 5. Contract Processing Profiles

### 5.1. Input Contract

Input is the boundary presentation contract.

It declares which outside presentation may appear for a flow, which distinctions the boundary must preserve, and which
values later contracts may judge. It does not place that outside material inside the core. Admission still decides
whether presented material may continue. Canonicalization produces stable representation only when the flow selects that
contract. Lowering declares which directly named Input coordinates may form the candidate Fact coordinates addressed
through the selected Operation parameter slots. After the applicable standing judgments and legal handoff establish that
material, the user Operation may consume the same ordinary host values without observing the external presentation.

A host declaration may already state an explicit external contract. Primitive values, strings, enums, nullability, and
approved closed immutable scalar types do not become silent merely because Kotlin or Java carries them. But neither the
declaration nor an object instance receives Kontrakt authority directly. The declaration nominates external contract
evidence that Kontrakt must refine and ratify; the object is only one way material may arrive.

V1 deliberately keeps that presentation flat. The selected Input presentation exposes one finite set of directly named
coordinates. A user-owned nested carrier, embedded Value Object, inherited part, interface-typed part, sealed hierarchy,
recursive type, collection object graph, or shared-reference graph does not become a second contract inside Input. Such
outside material must be flattened by an explicit replaceable adapter before it reaches the Kontrakt boundary.

Input does not introduce a second acquisition pipeline, a second frozen image, a second planning engine, or a mandatory
runtime material layer. It enters the machinery already established by ADR-0039, ADR-0040, and ADR-0043 on two separate
timelines.

At contract-definition time, the path is:

```text
slot-selected flat Input declaration
-> existing adapter-neutral metamodel acquisition
-> FrozenMetamodelImage
-> planning-facing frozen providers
-> direct-coordinate projection and source-profile judgment inside planning
-> adapter-erased lowered Input material
-> ratified contract graph unit
-> ContractImage-visible Input material and generated boundary realization
```

`FrozenMetamodelImage` remains the operation-neutral authority for source and metamodel facts. Input planning does not
re-open reflection, KSP, bytecode, source AST, PSI, or backend-local handles. It decides, for the selected operation,
which frozen declaration and direct member facts form the Input presentation and how those facts must be interpreted
under the Input role.

Frozen acquisition and planning remain separate passes. Input does not merge them and does not create another
`TypeReference` closure frontier, cycle-identity acquisition path, raw-fact image, graph engine, readiness bridge,
budget ledger, or frozen publication protocol. Existing planning mechanics may be reused, but Input owns its own
projection law. Generation-oriented constructor selection, polymorphic expansion, recursive member traversal, collection
expansion, and cycle truncation do not become Input semantics merely because those mechanisms already exist elsewhere.

The operation-specific result is lowered through the existing contract graph protocol. A source declaration, DTO object,
backend handle, or planning node cannot become Input identity directly. Input meaning becomes authoritative only after
it has been lowered into ratified Kontrakt-owned contract material. The resolved Input presentation is therefore the
logical operation-specific planning and lowering result; it is not another frozen metamodel image.

At invocation time, the path is shorter:

```text
already-formed flat immutable Input presentation
-> generated boundary reader or static gate
   -> declared presentation formed -> Admission
   -> declared presentation refused -> declared Input stop
   -> delegated early gate stops -> result owned by the supplying contract
```

The Input boundary accepts only explicitly declared immutable presentation material. A mutable object, lazy value,
proxy, framework-bound object, live collection view, nested carrier graph, or lifecycle-dependent carrier is not an
Input presentation. It must be transformed into one flat immutable presentation before it reaches the Input boundary, or
the direct source profile must reject it.

That transformation may be handwritten or generated, but it is an explicit adapter or presentation-formation operation
outside the Input runtime boundary. The generated boundary realization must not choose a capture moment, invoke lazy
behavior, snapshot mutable state, traverse an outside object graph, or derive stable meaning from a live carrier. It
reads and verifies already-formed immutable presentation material.

The immutable presentation is not a second frozen image, contract graph unit, or mandatory universal
`FormedBoundaryMaterial` domain type. Runtime invocation must not repeat metamodel acquisition, source resolution,
planning, graph ratification, canonical identity derivation, or contract publication.

The V1 admissibility law is:

```text
Input may accept an external presentation contract only when it can be deterministically refined into one explicit,
finite, inspectable, loss-accounted, flat set of directly named immutable coordinates without allowing external
implementation mechanics, nested contract composition, pointer topology, hidden movement, or another contract role to
survive refinement.
```

The authoring law is:

```text
Do not make the user declare the same flat presentation twice.
Treat the selected host declaration as external contract evidence.
Refine its direct coordinates through one deterministic Kontrakt law or reject it.
```

This law is how the Input API carries the discipline of *What Contract Is*. Kontrakt must not force users to restate a
flat external presentation they already selected through a host declaration. For each supported direct coordinate sort,
the selected frontend must provide one deterministic refinement law, remove external implementation mechanics from the
result, and reject the source or require an adapter when no safe refinement exists.

Three forms of hidden meaning are rejected.

**Hidden authority** exists when Kontrakt lets external implementation structure decide Kontrakt contract meaning. A
host source may contain inheritance, override dispatch, constructor execution, getter algorithms, framework annotations,
serializer conventions, collection implementations, `equals`, `hashCode`, or proxy behavior, but those mechanics cannot
survive refinement as Input authority.

**Hidden choice** exists when the same selected declaration admits more than one Kontrakt interpretation and the machine
silently chooses one. A direct nullable coordinate, enum, approved scalar, or bounded opaque leaf may be accepted only
through one fixed frontend profile. A user-owned nested type, collection carrier, sealed hierarchy, generic object
structure, or interface-based part is not guessed into a flat Input law. It is rejected or flattened by an explicit
adapter before Input.

**Hidden movement** exists when observing the alleged input performs behavior or depends on time. Callbacks, lazy
loading, live streams, futures, suppliers, services, capabilities, and resource handles are not Input material. A source
profile must exclude, project, or reject them; it must not silently ratify them as boundary data.

Slot nomination does not guarantee ratification. Input owns two distinct refusal boundaries. A source that cannot be
deterministically refined is rejected during planning or lowering and never receives ContractImage-visible authority. A
ratified Input contract may still refuse an invocation when the actual carrier cannot make the declared presentation
available. Admission begins only after Input formation succeeds.

An Input source candidate is ratifiable only when it satisfies the conditions below.

**Root selection and exact coordinate closure.** The operation manifest selects one Input declaration through the
`input` slot. Planning must resolve the complete operation-specific presentation from `FrozenMetamodelImage` before
runtime realization is generated. The lowered Input material contains the exact declaration identity, direct coordinate
set, coordinate order, coordinate sorts, presence distinctions, approved scalar-profile decisions, bounds, and every
resolution failure. No nested member path, inherited member, runtime subtype, accidental getter, or dynamically
discovered coordinate may extend that surface.

**Existing-machine integration.** Input source resolution consumes planning-facing providers backed by
`FrozenMetamodelImage`. It must not perform backend lookup or maintain an Input-owned metamodel cache, closure table,
cycle table, raw-fact table, frozen image, or planning session. Missing frozen coverage is a frozen-acquisition or image
integrity problem; disagreement about which covered direct members belong to Input is an Input planning or lowering
problem.

**Declared observation surface.** Kontrakt must know every coordinate the generated boundary realization may observe
before an invocation begins. The coordinate set is finite, flat, directly named, and inspectable. Runtime property
discovery, dynamic reachability, nested traversal, and accidental members do not extend the contract surface.

**Presentation-only authority.** Input may declare coordinate names, direct sorts, presence, finite scalar choices,
approved opaque scalar profiles, declared references represented as values, and bounds. It must not decide validity,
canonical identity, core meaning, state legality, publication permission, policy selection, or movement. Those
authorities belong to other contracts.

**Supported direct-coordinate condition.** A direct coordinate may use a primitive, `String`, a closed enum, or an
explicitly approved closed immutable scalar profile such as a pinned UUID, decimal, date, instant, bounded text, bounded
bytes, token, identifier, path, or URI presentation. Approval belongs to the frontend profile, not to a familiar host
type name. Host methods, object identity, equality, hashing, locale, timezone, scale, parsing, normalization, and
serialization behavior do not become Input law.

**Flatness condition.** A direct coordinate may not expose a user-owned product, entity, Value Object, record, data
class, interface, abstract class, sealed hierarchy, array, ordinary `List`, `Set`, `Map`, recursive node,
shared-reference graph, or dynamically typed container as further Input structure. V1 does not recursively compose
another contract through a coordinate type. An outside payload with that shape must be flattened into directly named
coordinates or converted into one explicitly approved bounded opaque leaf before submission.

**Explicit immutable boundary access.** Runtime Input formation must observe only the selected carrier's already-formed
direct values. It must not execute custom getters, delegated properties, lazy initializers, serializers, callbacks,
constructor defaults, object traversal, collection iteration, repository reads, environment access, or framework
lifecycle hooks. A read that can perform work or change with time is not a boundary coordinate read.

**Source carrier and contract separation.** A Kotlin data class, a final Kotlin class with primary-constructor `val`
properties, or a Java record may carry the flat presentation. Kontrakt retains only the resolved direct coordinate
names, sorts, presence, declaration order, approved scalar profiles, and bounds. Constructors, generated `copy`,
`componentN`,
`equals`, `hashCode`, `toString`, accessors, class ancestry, annotations, and object identity remain implementation
artifacts.

**Interface and inheritance boundary.** A user-defined interface, inherited carrier shape, interface-typed coordinate,
override relation, default method, or runtime implementation relationship contributes no Input material. A final carrier
may implement an unrelated interface only when every Input coordinate is independently declared on the selected flat
carrier and the interface relation is completely erased from authority.

**Nullability and default refinement.** A nullable direct coordinate explicitly allows a value or `null`. Kontrakt does
not invent an absent-versus-present-null distinction the source does not carry. A host or serializer default does not
become Input authority; only the value actually present at the boundary is observed. If omission or default application
must itself be judged, the flat presentation must carry that distinction as another direct coordinate.

**References and opaque values.** Input may carry identifiers, tokens, paths, URIs, source text, bounded bytes, and
other declared values. A live object reference, callback target, service, repository, file handle, session, transaction,
executor, clock, or capability does not become Input material merely because a field can hold it.

The V1 user-facing policy has four classes.

| Authoring class                                 | V1 treatment                                                                         | Examples                                                                                                                                                           | Contract effect                                                                                                                                                            |
|-------------------------------------------------|--------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct flat immutable presentation              | Refine and ratify directly when every coordinate is direct, immutable, and supported | Primitive and `String` coordinates, closed enums, approved closed immutable scalar leaves, flat Kotlin data class, flat final Kotlin carrier, flat Java record     | The selected declaration supplies external contract evidence and the runtime value already satisfies the Input boundary; no duplicate presentation declaration is required |
| Outside structured presentation                 | Require explicit flattening or presentation formation before Input                   | Nested DTO, embedded Value Object, inherited product, sealed hierarchy, array, ordinary `List`, `Set`, or `Map`, recursive tree, graph, dynamic JSON object        | An adapter removes outside structure and produces the declared flat Input coordinates; Kontrakt does not recursively adopt the outside contract                            |
| Implementation-shaped carrier                   | Require explicit adapter or reject the source                                        | Mutable JavaBean, framework DTO, proxy, entity, custom getter, delegated property, interface root, runtime-discovered implementation, third-party lifecycle object | Host conventions, capture timing, lifecycle, and implementation relationships are removed before the Input boundary                                                        |
| Behavior, capability, movement, or role leakage | Reject from Input                                                                    | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core Fact authority, State, backend handle        | The material belongs to another role or is not contract data                                                                                                               |

For the JVM V1 zero-adapter source profile, Kontrakt uses the selected host declaration itself as source evidence rather
than requiring a second generated presentation declaration. A Kotlin data class, a final Kotlin class whose selected
coordinates are primary-constructor `val` properties, or a Java 17+ record may satisfy the direct-source conditions only
when it is flat and every direct coordinate uses a supported immutable sort. `data class`, `val`, `final`, and `record`
are shape evidence; none grants authority by itself.

The frontend does not admit the host class or constructor as a contract unit. It interprets selected primary-constructor
`val` declarations and record components as one flat external presentation surface. Contract lowering retains only
direct coordinates, declared sorts, presence, declaration order, approved profile decisions, and bounds. Constructor
execution and generated class machinery remain on the implementation axis and are erased from contract authority.

Kotlin coordinate order is the selected primary-constructor property order. Java coordinate order is record-component
order. A root declared as a user-defined interface, a coordinate whose Input meaning depends on interface dispatch, or a
carrier whose visible Input surface requires nested traversal does not satisfy the zero-adapter profile.

These declarations are source conveniences, not contract authority. Equivalent flat presentation material may later come
from another language, schema compiler, serialization system, or generated frontend without changing the Input Contract.

Nested payloads, inherited carriers, framework objects, collection-shaped payloads, proxy objects, third-party objects,
recursive graphs, and dynamically typed containers may remain in the application outside Kontrakt. Before invocation
they must be converted through an explicit replaceable adapter or presentation-formation surface into the same flat
immutable Input material resolved and lowered through the frozen-image and planning path. Kontrakt V1 does not accept
those live objects and snapshot, flatten, or traverse them at the boundary.

If the source cannot be resolved under these laws, or if no safe deterministic direct-coordinate refinement exists, the
contract source is rejected during planning or lowering. No ratified Input material, ContractImage-visible Input
authority, or runtime boundary realization is produced for that source.

If a ratified Input contract exists but the supplied flat immutable presentation cannot satisfy the declared boundary
because required material is unavailable, structurally incompatible, malformed under the declared representation, or
missing a required distinction, the machine produces the declared Input failure before Admission begins. A mutable,
lazy, proxied, nested, collection-backed, or lifecycle-dependent object is not repaired at this point; it was not a
legal direct Input presentation. A Capacity, Budget, Policy, Governance, or other cross-cutting gate may also stop
execution during boundary access, but that result remains owned by the supplying contract and is not Input failure.

### 5.2. Admission Contract

Admission is the continuation judgment over the immutable presentation made available under the ratified Input Contract.

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
implementation handler, evaluator, adapter, factory, or instance.

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

**Input dependency condition.** Admission is compiled against the ratified flat Input material of the same operation.
Every runtime operand must resolve to one directly named Input coordinate, a value exposed by an approved direct scalar
profile, or a statically ratifiable literal. Admission does not traverse a nested carrier, collection object graph,
interface relation, runtime subtype, or outside reference graph. Policy, Governance, Capacity, Budget, environment, and
implementation objects do not become implicit Admission operands. Those contracts may select the active contract world
or stop under their own authority, but Admission does not inspect them as undeclared runtime data.

A literal or constant may participate only when the frontend can ratify its complete value and semantic type at
definition time. A `val` or `final` field is not sufficient when its value depends on initializer execution, external
state, class loading, framework injection, or another runtime capability.

**Carrier and judgment separation condition.** A V1 direct Input DTO remains an inert immutable presentation carrier.
Except for compiler-generated carrier machinery and inert formation, it must not declare validation, normalization,
derived access, Admission judgment, custom getter behavior, delegated access, semantic constructor behavior, or helper
methods. Admission is declared in its own selected class or object.

The same immutable Input presentation may participate in different operations with different Admission laws. Therefore a
carrier-wide `isValid`, `validate`, `isAdmissible`, or `canProceed` method cannot own continuation judgment. Kotlin-
generated accessors, `componentN`, `copy`, `equals`, `hashCode`, and `toString`, and Java record accessors, `equals`,
`hashCode`, and `toString`, remain host artifacts and are erased from Input and Admission authority.

**Host declaration profile.** The JVM V1 frontend accepts a dedicated source-visible Kotlin `object`, a dedicated closed
Kotlin class with no runtime instance state, or a dedicated Java final class with no runtime instance state. The
selected declaration must expose exactly one eligible root judgment that consumes the selected Input presentation and
yields a Boolean continuation result. The root member name is a frontend convention, not contract identity; uniqueness
and the operation-slot binding determine the root.

The declaration may contain statically ratifiable immutable constants, immutable local bindings, and private
source-visible helper operations. A helper is accepted only when its complete body is available, its call graph is
closed and acyclic, it cannot dispatch virtually, and the frontend can inline or otherwise refine all of its meaning
into the same flat Admission material. A helper is source decomposition, not a second contract. Constructor parameters,
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
operations, judgment relations, composition, and evaluation law. A Java declaration with equivalent value semantics must
lower to equivalent Kontrakt material.

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
- direct-coordinate relations over the flat ratified Input presentation without invoking user-defined `equals`,
  `hashCode`, or `compareTo` implementations;
- fixed-index and bounded relations inside approved text, binary, identifier, numeric, or temporal scalar profiles with
  explicit index and definedness laws; and
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
when the selected frontend owns a stable, versioned semantic profile for that exact operation. For example,
`String.startsWith` or a finite numeric operation may serve as source syntax only when the host call is removed and
replaced by backend-independent prefix or numeric material.

Unknown calls, user-defined receiver methods, custom predicates, user-defined equality or ordering, extension functions
whose bodies are unavailable, method references, virtual calls, framework callbacks, and library operations without a
complete Kontrakt semantic profile are rejected. Method purity is not assumed from naming, annotations, finality, or a
Boolean return type.

**Bounded direct-value operation condition.** Admission may inspect the internal units of an approved direct scalar or
opaque-leaf profile only when the Input Contract and active Capacity or Budget material close the required access bound
and Kontrakt owns the complete semantic operation. Examples include bounded text prefix, bounded binary index, numeric
classification, or another fixed profile operation.

A Kotlin or Java lambda, `Predicate`, `Function`, method reference, or functional-interface instance is not Admission
material. V1 does not use such a value to traverse user-owned collections or nested Input structure. A recognized
frontend operation must lower directly to finite Kontrakt judgment material; no runtime function object, iterator,
callback, or external carrier traversal survives.

**Totality and termination condition.** Every accepted Admission judgment must be total for every presentation admitted
by the selected Input Contract and must terminate under a definition-time-known bound. Division by zero, invalid shifts,
invalid indices, narrowing loss, exact-arithmetic overflow, malformed patterns, unsupported encodings, and similar
undefined or exceptional paths must be ruled out by static proof, represented by an explicit total relation, or
rejected. A JVM exception must never become an implicit Admission refusal.

Finite processing internal to an approved direct scalar or opaque-leaf profile is allowed only under its ratified bound.
Arbitrary `while` or `do-while` loops, runtime-dependent unbounded loops, recursion, user-owned carrier traversal,
cyclic helper calls, blocking operations, waiting, synchronization, and termination that relies on application behavior
are prohibited in V1. The semantic judgment surface may be rich; the machine must still know before publication that
every invocation completes under the declared bounds.

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
-> validate type, null, numeric, ordering, approved scalar-profile, totality, and bound laws
-> erase class, object, method, getter, lambda, iterator, and library-call mechanics
-> canonicalize judgment structure, literals, source coordinates, and evaluation law
-> derive stable Admission material identity
-> ratify and publish the material in the ContractImage
-> generate the deterministic Admission evaluator
```

Contract identity must change when a frontend profile, numeric law, string law, approved scalar-profile law, evaluation
law, or any other semantic refinement changes contract meaning. Source formatting, local variable names, equivalent host
syntax, and backend instruction choice must not change identity when they lower to the same material.

**Deterministic evaluation condition.** At invocation time, the generated evaluator reads only the already-formed
immutable Input presentation through fixed ratified coordinates. Runtime symbol lookup, reflection, property discovery,
method dispatch, callback construction, literal parsing, operator selection, and failure-policy selection are forbidden.

Boolean composition and any bounded direct-value inspection must have a fixed evaluation law. V1 preserves a
deterministic declared or canonical order wherever order can affect first decisive judgment, diagnostic evidence, budget
consumption, or failure attribution. A backend may fuse branches, use primitive comparisons and bit instructions,
specialize bounded profile operations, vectorize, or return allocation-free outcome codes only when the observable
admitted or rejected result and its contract-owned attribution remain identical.

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

When the `canonicalization` slot is selected, its law receives the same immutable contract-visible presentation after
Admission and deterministically produces the one representative permitted by that law. Successful production grants
stable representative authority immediately. There is no user-authored post-canonical validation contract. If a
successful Kontrakt realization produces noncanonical material, Kontrakt is defective.

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

**Optional slot and selected-source condition.** The authored `canonicalization` slot may be omitted in V1. Omission
means that this operation declares no semantic Canonicalization Contract. Kontrakt does not insert
`ExactCanonicalization`, infer an identity representative law, invoke a user canonicalizer, or create a proxy,
interceptor, callback, or hidden implementation stage. After Admission, the same submitted Input presentation is
supplied unchanged to the selected Lowering relation, its compiler-derived plan, and the explicitly bound realization
behind the generated port.

Deterministic encoding of Kontrakt-owned material remains mandatory, but that protocol is not an implicit
Canonicalization Contract. Fixed scalar encodings, coordinate order, presence markers, framing, schema identity, and
version material may still be required for identity, ordering, hashing, caching, publication, or verification. They
determine how existing material is represented inside Kontrakt; they do not collapse semantic distinctions or produce a
new representative value.

When authored, the independently resolved `canonicalization` slot names exactly one Kotlin or Java symbol as external
source evidence for one flat Canonicalization Contract. A source-layout label such as `flow` grants no meaning. The
selected symbol, file, package, method name, annotation, type relation, and source location do not own Canonicalization
authority. The slot supplies the role; frontend refinement decides whether the selected source can be ratified; the
resulting Kontrakt-owned material supplies authority.

V1 supports exactly two selected source forms.

```text
Direct law selection:
    select one complete Kontrakt-provided built-in Canonicalization symbol

Coordinate-law type declaration:
    select one inert Java or Kotlin signature declaration whose parameter names bind every
    directly named Input coordinate and whose parameter types are exact Kontrakt-owned nominal
    canonical type symbols
```

When present, the manifest names one declaration only:

```text
canonicalization  UnicodeNfcCaseFoldCanonicalization
```

or:

```text
canonicalization  CustomerCanonicalization
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
values, or descriptor instances inside the contract manifest or selected declaration. The interface IDL selects the
Canonicalization source. A coordinate-law declaration carries only coordinate names and references to nominal type
symbols supplied by the Kontrakt authoring API.

**Flat built-in law condition.** A built-in symbol names one complete, closed, versioned Canonicalization law. V1 does
not expose one parameterized configuration template from which users compose a law by choosing normalization, case,
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
exact Kontrakt-owned nominal canonical type symbols. The declaration contains no law value, enum constant, singleton
instance, factory call, method body, callback, property initializer, or descriptor object.

The binding law is:

```text
selected Input coordinate name
+ selected Input coordinate sort
+ one exact Kontrakt-owned nominal canonical type symbol
= one coordinate law inside one flat Canonicalization Contract
```

The matching occurs only within the operation that independently selected the Input and Canonicalization declarations.
It is resolved at definition time from exact source symbols, declared parameter names, and nominal parameter types. It
is not a runtime string lookup, package convention, reflection search, annotation scan, assignability rule, or type-wide
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
data class CustomerInput(
    val customerId: String,
    val name: String,
    val regionCode: String,
)

// User-authored source declaration. It is never instantiated.
class CustomerCanonicalization private constructor(
    customerId: ExactText,
    name: UnicodeNfcCaseFold,
    regionCode: AsciiUppercase,
)
```

The Kontrakt-provided declarations at the top exist in the real authoring API rather than in user source; they are shown
only to make the example compiler-complete. The user writes the Input and the selected coordinate-law declaration. The
private constructor prevents formation of a `CustomerCanonicalization` object, and its parameters are not properties.
Only their names and exact nominal types are declaration evidence. The frontend resolves and erases the declaration
class, inaccessible constructor, parameter symbols, generic signatures, and every referenced host type before authority
begins.

The coordinate-law signature follows the already-ratified flat Input presentation. Each constructor parameter binds one
directly named Input coordinate to one exact nominal canonical type. The signature does not create values, runtime
descriptors, nested Canonicalization Contracts, parent-child authority, inheritance, recursive contract composition, or
a second presentation shape. It is finite source evidence from which Kontrakt ratifies one flat Canonicalization
Contract for the selected operation.

V1 rejects declaration properties, `val` or `var` law bindings, enum law categories, enum constants, law singleton
objects, public constructors, factory calls, arbitrary generic types, executable initializers, methods, callbacks,
lambdas, property references, custom comparators, user-defined equality or ordering, inheritance, interface-based role
acquisition, annotations on the Input DTO, mutable fields, lazy or delegated values, captured dependencies, and values
acquired from runtime execution.

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
supported Input sort and direct-coordinate position
preserved and collapsed distinctions
unique representative law
semantic profile and version
null, absence, finite-alternative, ordering, and duplicate behavior where applicable
work, source-size, expansion, and output bounds
canonical byte law and protocol version
refusal domain and cross-contract stop attribution
normative examples and conformance-vector reference
```

The type name and documentation are public projections of the same versioned Kontrakt-owned canonical law material. The
documentation explains the law to users but does not replace that material as authority. A law meaning change requires a
new or versioned canonical type identity, corresponding specification changes, and updated normative conformance
material.

**No user-authored output-presentation condition.** V1 does not require or permit a second user-authored canonical
output presentation. The contract-visible shape remains the Input shape. The canonical result may have a radically
different Kontrakt-owned physical layout and byte encoding, but that internal material is not another user DTO and does
not grant Canonicalization permission to perform mapping.

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
payload, signed-zero, timezone, or other differences only when the selected law explicitly declares them irrelevant. It
must never erase a distinction merely because a backend finds the smaller result convenient.

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

**Protocol canonical-byte condition.** Kontrakt alone owns the exact canonical bytes. Users select representative laws
through built-in symbols or coordinate-law nominal type declarations; they do not write byte encoders. After semantic
representative production, Kontrakt emits one implementation-erased byte sequence under a versioned protocol that fixes
at least:

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
approved bounded leaf framing and ordering where applicable
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

A public type such as `UnicodeNfcCaseFold` is not a request to call the current JDK normalizer or lowercase method. It
selects a complete Kontrakt semantic law with pinned Unicode tables, locale behavior, expansion bounds, totality,
complexity, representative rules, and canonical-byte encoding. The generated canonicalizer must execute the ratified
law, not inherit meaning from the host library available on the current machine.

The coordinate-law declaration contains no values to calculate. It may not invoke `Normalizer`, `lowercase`,
`uppercase`, sorting, parsing, environment reads, class initialization, helper calls, or another host operation.
Environment-independent execution is therefore not inferred from user code; it is guaranteed by the selected nominal
type and Kontrakt's generated realization.

The complete semantic definitions, host-operation recognition used by compiler internals, legality matrices, byte plans,
and generated evaluators remain compiler protocol. They are not exposed as user IR, runtime registries, expression
nodes, or generated coordinate APIs. The public surface exposes only complete built-in Canonicalization symbols, opaque
nominal canonical type names, precise definition-time diagnostics, and the API specification and user documentation that
explain each type.

**Canonical-type applicability condition.** Rich canonical behavior is admitted through exact nominal type symbols
rather than executable source expressions or runtime law values. Every nominal type symbol must resolve to enough
ratified material for Kontrakt to determine, before publication:

```text
supported input sort and structural position
preserved and collapsed distinctions
unique representative law
null, absence, finite-alternative, and duplicate behavior
numeric, floating-point, Unicode, temporal, binary, or collection semantics where applicable
work, source-size, expansion, and output bounds
canonical byte schema and protocol version
refusal domain and contract-owned attribution
normative conformance material
```

The frontend rejects:

```text
unknown, application-defined, aliased, imitated, dynamically registered, or incompletely specified canonical type symbols
missing, duplicate, renamed, unknown, or sort-incompatible coordinate bindings
law values, enum constants, singleton instances, constructor calls, factories, methods, callbacks, lambdas, helper
execution, control flow, or transformation bodies
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
accepted law must establish finite source size, intermediate storage, output size, expansion, and work bounds before
ContractImage publication. General imperative iteration over undeclared structure and recursive object-graph traversal
are prohibited. Bounded processing inside an approved opaque leaf profile is accepted only when its size and traversal
semantics have already been ratified.

For an approved bounded opaque leaf whose own canonical law includes unordered elements, Kontrakt must not use JVM
object references, `hashCode`, host iteration order, or an unbounded structural comparison. Any element ordering,
duplicate handling, byte production, comparison work, and intermediate memory remain fixed by that leaf profile and the
selected Capacity and Budget world. This is an implementation strategy inside one approved direct-coordinate sort, not
permission to traverse a user-owned nested Input structure. Exceeding those walls produces the owning Capacity or Budget
result, not a generic Canonicalization failure.

**Unicode and adversarial-text condition.** Unicode normalization and case operations must use pinned tables and a
versioned, table-driven, environment-independent algorithm. Kontrakt must not implement authoritative normalization with
an unconstrained regular expression or delegate meaning to the current host normalizer. Input, Capacity, and Budget must
bound at least encoded length, code-point count, combining-sequence depth where applicable, canonical output size, and
intermediate work. Pathological combining-mark payloads may produce a diagnostic classification, but the stop result
must remain owned by the contract whose declared wall was crossed.

**Floating-point condition.** Floating-point Canonicalization never inherits ordinary `==`, boxed equality, host
sorting, or incidental NaN behavior. Every selected law must explicitly state whether it:

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
-> verify complete direct-coordinate coverage, declared order, and sort compatibility
-> resolve every exact nominal canonical type to pinned semantic material
-> reject law values, constructors, factories, execution, environment, dispatch, state, effect, imitation, and dynamic-registration paths
-> ratify preserved and collapsed distinctions for every coordinate and the complete presentation
-> prove declared work, source-size, expansion, intermediate-storage, and output bounds
-> derive the canonical byte schema and protocol version
-> derive stable contract identity and ContractImage material
-> generate the deterministic canonicalizer and byte emitter
```

Runtime performs no declaration lookup, reflection, member discovery, method dispatch, operator resolution, locale or
Unicode-table selection, comparator acquisition, hash-policy selection, or byte-schema construction. It executes only
the generated realization over fixed ratified coordinates and declared cross-contract bounds.

**Version, identity, and conformance condition.** A built-in or declaration-derived Canonicalization identity is derived
from ratified meaning, not the selected class name alone. Identity material includes at least:

```text
law kind
semantic profile version
source presentation identity
preserved and collapsed distinctions
coordinate-law bindings, representative laws, and their versions
Unicode, temporal, decimal, floating, and approved opaque-leaf laws where applicable
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

Lowering is the final representation boundary of the inbound airlock. It re-forms admitted external presentation as
complete candidate material for the ordinary host types declared by the selected Operation parameters.

```text
external presentation authority
-> explicit one-to-one source-to-parameter-Fact relation
-> exactly one supplied realization behind a generated port
-> complete candidate Operation input material
-> applicable Invariant and movement judgment
-> established input Fact authority
-> ordinary user Operation invocation
```

Input, Admission, and selected Canonicalization operate over the contract-visible boundary presentation. Admission may
permit that presentation to continue, and Canonicalization may select its stable same-shape representative, but neither
action ends external-presentation authority. Lowering ends that authority only when its declared relation has been
realized, the candidate material is complete, and every applicable standing judgment and legal movement has succeeded.

Lowering does not execute core computation, create a rich domain object model, derive a business Result, decide an
Invariant, perform a State Transition, or publish an outward presentation. The supplied realization forms candidate
representation only. The generated machine owns judgment, establishment, stage order, refusal routing, and the handoff
to the user Operation.

Lowering is not an arbitrary mapper. The IDL relation declares the complete permitted source-to-target dependency. The
compiler derives one semantic `LoweringPlan` and one retained plain host-language realization port. Exactly one
implementation is supplied during machine assembly. That implementation may realize representation formation, but it
does not own the relation, Fact meaning, target participation, establishment, or pipeline authority.

#### 5.4.1. Boundary Source and Operation Input Fact Target

The Lowering Contract and its generated realization port are the only Lowering surfaces allowed to know both the
selected boundary presentation coordinates and the Operation parameter coordinates formed from them.

```text
interface scope:
    one closed `facts` vocabulary
    one standing `invariants` declaration

boundary side:
    selected Input presentation
    optional same-shape canonical representative

Lowering Contract:
    immutable explicit one-to-one source-to-Operation-parameter-Fact relation

generated implementation boundary:
    retained plain host-language realization port
    exactly one explicitly supplied implementation
    compiler-derived LoweringPlan
    candidate-material completion and declared failure surface

judgment and core-entry side:
    applicable interface-level Invariant judgment
    applicable movement judgment
    established input Fact authority
    ordinary user Operation parameter values
```

The selected Input schema remains the source schema even when Canonicalization is present because Canonicalization does
not change contract-visible coordinates. The selected representative values become the source values supplied to
Lowering. When Canonicalization is absent, the admitted Input values are supplied unchanged.

Lowering does not introduce an independent `CoreMaterial`, `OperationStart`, `LoweringTarget`, or user-visible candidate
schema. Its targets are coordinates addressed through the ordinary parameter slots already declared by the selected
Operation. Each parameter type must resolve to one Fact kind declared through the enclosing interface's `facts`
vocabulary. The operation manifest does not repeat that vocabulary, and it has no `fact` or `invariant` slot.

The target Fact surface must not name, import, embed, extend, or retain a reference to the Input declaration, source
DTO, transport protocol, host object, Lowering declaration, framework context, adapter type, or source-coordinate
handle. It must be intelligible solely through its own Fact coordinates, sorts, presence, alternatives, relations,
ordering, bounds, schema, and version material.

The Lowering declaration may name both sides because it belongs to the operation boundary. Its generated port exposes
only the declared realization surface. A supplied implementation may physically read the declared source values and
construct the ordinary target material, but source type names, getters, constructors, collection implementations, object
identity, runtime references, and realization machinery have no factual authority. The generated port source is a
retained build artifact and may remain as an ordinary adapter boundary if Kontrakt is removed.

#### 5.4.2. Immutable IDL Relation and Explicit Realization Boundary

*What Contract Is* requires the Lowering Contract itself to contain immutable data only. It contains no translation
implementation and no business algorithm.

The enclosing interface declares `facts` and `invariants` once. The operation manifest selects one Lowering handle, and
one sibling `lowering` declaration inside the same operation states the exact coordinate relations:

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

`facts` and `invariants` remain at interface scope. Inside the operation, `manifest` and `lowering` are parallel sibling
declarations. Publication uses the same operation-local sibling placement under ADR-0049, but its outward processing is
outside this ADR.

The Lowering body may carry only a finite set of exact coordinate pairs:

```text
one exact selected Input coordinate for each binding
one exact Operation parameter slot plus target Fact coordinate for each binding
```

The selected Input surface and Operation signature supply the two resolved sides. The relation body does not repeat
carrier declarations and does not contain executable conversion code.

Every binding is explicit. Equal spelling does not create a relation, and different spelling does not prevent one.
`accountIdText` and `command.accountId` remain unrelated until the operation-local Lowering declaration binds them. This
preserves the independent language of the external presentation, the Operation surface, and the core Fact vocabulary.

The binding cardinality is fixed in V1:

```text
one selected Input coordinate
-> one Operation-parameter Fact coordinate
```

One Input coordinate must not form multiple contract-visible Fact coordinates. Multiple Input coordinates must not be
combined to form one target coordinate. Such `1:N` and `N:1` derivations introduce cross-coordinate meaning and belong
to core computation. A backend may still split, pack, flatten, or combine physical storage behind the same ratified
one-to-one relation; physical layout does not change mapping cardinality.

The selected source and target sorts may be identical. Equal types do not authorize implicit copying and do not remove
the port. The selected source and target sorts may also differ. In either case, the generated port reflects the declared
source and target surface, and the explicitly supplied implementation performs the physical representation formation.
The compiler does not infer a conversion from the raw host-type pair, select a catalog entry, or authorize backend
convention as hidden meaning.

The `->` relation is not assignment, a cast, a parser, a constructor call, or an implementation. It declares that the
source coordinate is the permitted factual basis for the target coordinate. The realization behind the generated port
must preserve the already-declared source and target meaning.

The relation body rejects every executable surface, including:

```text
transformation methods or constructor procedures
method bodies, callbacks, lambdas, predicates, comparators, or user-defined equality
control flow, loops, recursion, pattern matching, or rewrite expressions
repository, service, framework context, state store, filesystem, network, process, clock, or randomness access
runtime registry lookup, reflection, dependency injection, classpath discovery, or class initialization
host-library calls, normalization, serialization, parsing, or conversion code
mutable, lazy, delegated, captured, global, or thread-local state
core computation, Operation Result Material derivation, Invariant judgment, State movement, or Publication
```

Those prohibitions apply to the contract body. Physical conversion belongs only behind the generated port. The supplied
implementation may use ordinary reusable libraries, but it must be deterministic for the declared input domain, use only
the source material exposed by the port, return only the declared target candidate or declared Lowering failure, and
perform no repository lookup, environmental resolution, business calculation, State movement, Invariant judgment, or
Publication. A library assists implementation; it does not select itself and does not become contract authority.

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
contract-visible boundary, V1 forms each Operation-parameter Fact coordinate from exactly one explicitly bound Input
coordinate.

Permitted one-to-one refinement families include:

```text
declared external scalar presentation
    -> immutable Fact scalar of the same declared meaning

nullable or optional presentation
    -> one explicit optional Fact coordinate

closed external alternative
    -> one explicit finite-alternative Fact coordinate

approved bounded opaque leaf presentation
    -> one immutable Fact coordinate under the same declared leaf meaning

direct selected Input coordinate
    -> one explicitly addressed Operation-parameter Fact coordinate

declared external identifier or reference presentation
    -> one Fact representation of that same declared identifier or reference meaning
```

The supplied realization may decode, parse, range-check, copy, freeze an approved bounded leaf, make an already-declared
presence distinction explicit, or select a fixed internal representation. Those actions remain Lowering only when the
Input Contract already declares the meaning being represented and the target Fact kind preserves that meaning. A generic
text coordinate does not become a date, account identity, money, or another business concept merely because an
implementation or library can parse it.

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
several contract-visible meanings. The following are not Lowering:

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
Lowering Contract:
    one boundary coordinate -> one Operation-parameter Fact coordinate of the same declared meaning

Lowering realization:
    explicitly performs the permitted representation formation behind the generated port

core realization:
    combines, separates, and computes over established Facts and other declared immutable material

Invariant:
    judges one complete candidate Fact under the standing interface-level law for its exact kind

State and Transition:
    govern legal movement and the availability of material in a machine condition

Publication:
    governs the permitted outward claim
```

A conforming Lowering realization must be deterministic over its declared source domain and return either complete
candidate material or one declared Lowering failure. The generated machine supplies only the declared source
coordinates. Budget, Capacity, Version, Governance, and other cross-cutting gates remain outside the implementation and
retain their own authority and attribution.

A declared reference may cross Lowering only as one explicit source coordinate refined into one explicit target
coordinate of the same declared reference meaning. Resolution against another coordinate, a mutable registry, or current
core state belongs to core realization or a later judgment and must not be disguised as formation.

#### 5.4.5. Definition-Time Resolution and LoweringPlan

The selected `lowering` slot names exactly one operation-local sibling Lowering declaration. The IDL body contains the
exact one-to-one coordinate relations. The slot grants the role; the sibling declaration supplies the immutable contract
material. Neither selects or contains the implementation.

Before ContractImage publication and executable machine closure, the frontend/compiler and linker must resolve and
ratify at least:

```text
the flow-selected Input schema
the selected Canonicalization law, when present
the enclosing interface's Fact vocabulary and standing Invariants
the selected Operation signature and every target parameter slot
the resolved Fact kind of every target Operation parameter
every explicit Input-coordinate to Operation-parameter-Fact-coordinate binding
one source coordinate and one target address for every binding
explicit binding even when source and target names or host types are equal
source-sort and target-sort structural compatibility
target-coordinate completeness and uniqueness
Input source-coordinate uniqueness across bindings
the generated plain host-language realization-port ABI
exactly one implementation binding for that port
finite depth, cardinality, intermediate storage, output, and work bounds
applicable schema, Version, and Governance material
declared failure and cross-contract stop attribution
```

Every target coordinate formed by this airlock must be formed exactly once under one complete flat Lowering Contract,
and every Input coordinate may appear in at most one binding. An Input coordinate may remain unused; that does not
implicitly create a binding or require the Operation parameter Fact surface to expose external material it does not
need. Silent target defaults, fallback constructors, same-name auto-mapping, same-type auto-copying, catalog selection,
structural guessing, package scanning, annotations, assignability, inheritance, and discovery from implementation shape
are prohibited.

The frontend must reject missing or duplicate target formation, reused source coordinates, unknown source or target
coordinates, `1:N` or `N:1` relations, structurally incompatible sorts, hidden absence, recursive host-object traversal,
unbounded collection work, environment-dependent contract meaning, executable material inside the relation body, or a
target type absent from the enclosing interface's Fact vocabulary. Missing or duplicate realization implementations,
port ABI mismatch, or unresolved assembly bindings are link-time definition failures, not runtime Lowering refusals.

The accepted IDL declaration is lowered into one flat, immutable, adapter-erased Lowering Contract material. The
Lowering Contract identity includes the source-presentation schema identity, optional Canonicalization law identity,
selected Operation identity, target parameter slots and Fact-kind identities, the canonicalized explicit one-to-one
binding set, bounds, refusal law, and relevant flow-world coordinates. The supplied implementation identity is not part
of contract meaning; it belongs to executable machine assembly and must conform to the generated port.

From that ratified material, the compiler derives one immutable semantic `LoweringPlan`. The plan closes the exact
source read set, target write set, port ABI and invocation set, candidate-completion checks, declared failure branches,
Invariant and movement handoff, establishment requirements, cache dependencies, and backend layout requirements that are
legitimately implied by the contract. The plan does not own conversion semantics and does not acquire contract
authority. It is a compiler-owned implementation artifact derived from the ratified obligation.

#### 5.4.6. Generated Realization Port, Explicit Binding, and Fact Establishment

The compiler generates one plain host-language Lowering realization port from the ratified `LoweringPlan`. It does not
silently generate or select the complete conversion implementation. Exactly one implementation is supplied during
machine assembly.

The generated port source is a retained build artifact. It may be committed, inspected, implemented, and called as
ordinary Kotlin or Java code. It contains no contract authority and no Kontrakt-specific Fact wrapper. If Kontrakt is
removed, the retained port and its implementation may remain as ordinary adapter code, while automatic contract
validation, judgment orchestration, establishment, diagnostics, generated tests, and optimization disappear.

After assembly is closed, runtime performs no classpath scan, reflection, symbol lookup, coordinate-name search,
type-pair catalog lookup, service discovery, DI resolution, or implementation selection. The generated execution path
uses the one closed implementation reference or an equivalent direct backend binding.

The supplied realization receives only the source values admitted by the declared relation and returns complete
candidate material for the declared Operation parameter surface or one declared Lowering failure. It does not receive a
repository, service, state store, framework context, runtime registry, Fact population, Invariant callback, Publication
callback, or arbitrary machine context.

Candidate formation is not Fact establishment. The generated pipeline must then verify that:

```text
every declared target coordinate is complete exactly once
the returned material conforms to the resolved Operation parameter type and Fact kind
every target coordinate is justified by its one explicitly bound Input coordinate
the material is immutable and no mutable external alias remains
presence, alternatives, relations, and ordering are explicit where the Fact surface declares them
applicable schema, Version, Governance, Budget, and Capacity gates have succeeded under their own authority
every interface-level Invariant applicable to the candidate Fact kind holds
every applicable movement judgment succeeds
no back-reference to the external presentation or realization machinery remains
```

Only after those obligations succeed does the generated machine establish input Fact authority and invoke the user
Operation with the same ordinary host value. Allocation, constructor completion, port return, or builder completion
alone does not create Fact authority.

The supplied implementation may use temporary mutable builders, scratch buffers, ordinary parsing libraries, offset
tables, sorting workspaces, or staged regions. Those objects belong exclusively to implementation. They hold no factual
authority and must never be exposed as established Facts.

The compiler owns the complete relation and closed call plan, so the backend may derive mechanically sympathetic
realizations where equivalence is proven, including:

```text
devirtualization of the exactly bound port implementation
inlining or specialization of a statically analyzable realization
port-object erasure after closed linking
Canonicalization-Lowering orchestration fusion
intermediate presentation elimination
dead source-coordinate read elimination
direct source access and direct candidate-region writes
exact buffer and region sizing
single-allocation or bounded-allocation formation
primitive and finite-alternative specialization
packed relation and presence layouts
structural cache keys from the exact declared read set
AOT-generated invocation, completion, judgment, and establishment paths
```

When implementation behavior cannot be proven safe for a transformation, the backend preserves the explicit port call.
Optimization authority does not permit guessing, replacing the supplied implementation with a catalog entry, or changing
the declared relation.

The same ratified relation and closed implementation binding may supply automatic verification and test generation:
target-totality checks, source-target structural compatibility checks, one-to-one binding checks, deterministic replay,
declared refusal-path tests, backend-equivalence tests, boundary-value tests, and regression tests for generated
artifacts. Generated tests improve assurance; they do not turn implementation into contract authority or prove arbitrary
business semantics that the relation does not declare.

The determinism law is:

```text
same ratified ContractImage
+ same immutable admitted Input presentation
+ same selected canonical representative, when present
+ same closed Lowering realization binding
+ same declared Budget, Capacity, Version, and Governance world
= same Lowering outcome
+ same complete candidate material
+ same judgment and establishment outcome
+ same contract-owned refusal or stop attribution
```

A conforming realization must satisfy that law. Undeclared environment, time, randomness, mutable global state, or
acquisition-order dependence is an implementation defect.

The explicit port invocation may be fused with upstream Kontrakt-owned canonical production or downstream candidate
completion only when the same logical Lowering boundary, explicit one-to-one relations, outcome, attribution, standing
judgments, establishment point, and state-visible handoff remain explicit. Physical fusion must not allow the user
Operation to observe external presentation or allow implementation staging to acquire factual authority.

#### 5.4.7. Refusal and Defect Boundary

Lowering refuses when the declared candidate material cannot be formed and established under the selected relation.
Representative cases include:

```text
required bound source material is absent
the supplied realization returns one declared Lowering refusal
the returned target material is incomplete or structurally incompatible
a source or target coordinate is missing, duplicated, or reused illegally
an alternative cannot be represented by the resolved Operation parameter Fact surface
an explicitly declared one-coordinate reference representation cannot be formed
candidate-coordinate completeness cannot be achieved
a depth, cardinality, output, or work bound is exceeded
applicable schema, Version, Governance, Budget, or Capacity material stops the passage under its own authority
an applicable Invariant or movement judgment refuses establishment
```

A missing implementation, duplicate implementation, ABI mismatch, or unresolved binding is a definition or link failure
and prevents executable machine publication. It is not a runtime Lowering refusal.

A Budget or Capacity wall retains the result and diagnostic authority of the supplying contract. It is not rewritten as
a Lowering refusal. A supplied implementation that throws an undeclared exception, reads hidden environment, performs
business computation, returns material inconsistent with the relation, or violates determinism is defective. Contract
source is not blamed for an implementation defect, and the defect does not become a declared Lowering refusal
automatically.

Lowering refusal, user Operation failure, Invariant refusal, and Publication refusal are distinct:

```text
Lowering refusal:
    no complete and establishable input Fact material exists

user Operation failure:
    established input Facts exist,
    but the replaceable implementation does not complete with its declared result

Invariant refusal:
    complete candidate Fact material exists,
    but a standing law for that exact Fact kind does not hold

Publication refusal:
    the Operation result Fact exists,
    but the declared outward claim is not permitted
```

#### 5.4.8. Core Entry Handoff

The supplied Lowering realization yields complete candidate material in the ordinary host types declared by the
Operation parameters. The generated machine applies candidate completion, the interface-level Invariants for each exact
Fact kind, and every applicable movement judgment. Successful Lowering ends only when those obligations succeed and the
candidate receives established input Fact authority.

The user Operation then receives the same ordinary Java or Kotlin values it would receive without Kontrakt. It does not
receive the Input presentation, canonical presentation object, Lowering declaration, source-to-target relation table,
`LoweringPlan`, generated port metadata, candidate wrapper, established-Fact wrapper, staging object, or host-language
execution context.

```text
external presentation authority:
    ended

candidate Operation input material:
    complete under the declared relation

standing judgment:
    every applicable Invariant and movement obligation succeeded

input Fact authority:
    established outside the host value representation

user Operation invocation:
    ordinary parameter values only

Result and Publication:
    not yet established or authorized
```

The Operation may now use those established input Facts together with other explicitly bound Facts and applicable
Policy, Budget, Capacity, Version, Governance, and State material. How replaceable implementation derives its ordinary
result is outside this Lowering Contract. The implementation does not invoke Lowering, Invariant, establishment,
Publication, or Output Presentation machinery itself.

ADR-0049 defines the standing Fact and Invariant law applied at this handoff, the establishment of the ordinary
Operation return as the successful result Fact, Publication, and Output Presentation. It must not reopen the external
presentation, repeat Lowering, or derive authority from the erased IDL relation or supplied realization implementation.

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
without an inserted contract, callback, proxy, or interceptor. Lowering then uses the operation-local immutable
one-to-one relation, a derived `LoweringPlan`, one retained generated port, and exactly one explicitly bound realization
to form candidate Operation input material. The generated machine—not the realization implementation—owns completion,
standing judgment, establishment, and the legal handoff to the user Operation.

### 6.2. Earliest Authoritative Rejection

Each of these contracts stops only the material it has authority to judge.

Input stops material that cannot be formed as the declared immutable boundary presentation. Admission stops formed
material that may not continue. When selected, Canonicalization refuses admitted material only when its law cannot
produce one unique bounded representative within its declared domain. A Budget or Capacity wall crossed during selected
canonical production retains the supplying contract's result. When Canonicalization is omitted, no Canonicalization
refusal is possible. Lowering refuses when its immutable relation, permitted source material, explicitly bound
realization, and applicable standing judgments cannot form and establish the selected Operation input Fact material.

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
later reject it.

### 6.4. Optional Canonical Production and Core Fact Entry Authority

When Canonicalization is selected, its production is owned by Kontrakt because the representative law fully determines
its permitted result. Fact-forming representation remains outside Canonicalization and is declared by Lowering.

```text
selected Canonicalization:
    admitted presentation
    -> Kontrakt-generated Canonicalization realization
    -> stable same-shape representative
    -> exact canonical bytes
    -> operation-local Lowering relation
    -> generated retained Lowering port
    -> exactly one supplied realization
    -> complete candidate Operation input material
    -> applicable Invariant and movement judgment
    -> established input Fact authority
    -> ordinary user Operation invocation

omitted Canonicalization:
    admitted presentation
    -> operation-local Lowering relation
    -> generated retained Lowering port
    -> exactly one supplied realization
    -> complete candidate Operation input material
    -> applicable Invariant and movement judgment
    -> established input Fact authority
    -> ordinary user Operation invocation
```

In the selected branch, Canonicalization owns representative meaning and grants authority to the result produced by its
generated realization. In the omitted branch, no semantic representative authority is granted and the admitted Input
remains the Lowering source. Neither branch permits an arbitrary user canonicalizer, proxy, interceptor, or hidden
transformation stage.

Lowering owns the explicit one-to-one source-to-Operation-parameter-Fact relation in both branches. Its IDL body is
immutable contract material authored beside the operation manifest. The generated port and exactly one supplied
implementation form candidate representation. The implementation is wrong if it introduces undeclared meaning, observes
hidden state, performs business computation, judges an Invariant, moves State, publishes material, or returns candidate
material that disagrees with the ratified relation and target Fact surface. The generated machine owns completion,
judgment, establishment, and Operation invocation.

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
implementation code. That implementation remains a replaceable realization region. Kontrakt governs which established
input Facts may be visible, whether the ordinary Operation return becomes the successful result Fact, which failures are
declared, and which state movements and Publication judgments are legal, but it does not make implementation classes,
functions, dispatch, allocations, effects, or object layout contract authority.

Slot-selected Admission and selected Canonicalization remain restricted frontend sources, not opaque implementation
callbacks. Admission may use a finite judgment-expression source. Selected Canonicalization uses either one closed
built-in law symbol or one uninstantiable coordinate-to-nominal-type signature declaration.

Lowering has two deliberately separated surfaces. Its operation-local sibling IDL body contains only finite immutable
one-to-one Input-to-Operation-parameter-Fact relations. Its generated retained port carries one explicitly supplied
physical realization. The relation is completely resolved and erased into canonical contract material. The
implementation remains replaceable and acquires no authority.

Kontrakt owns and optimizes the generated evaluator, canonicalizer, canonical byte emitter, relation plan, port ABI,
assembly binding, candidate-completion checks, standing-judgment dispatch, establishment path, generated verification
and tests, cache plan, and associated deterministic state machinery. Canonicalization omission generates no replacement
canonicalizer or runtime interception point.

Because the Lowering implementation is exactly bound behind a generated port, Kontrakt may devirtualize, inline,
specialize, fuse around, or erase that port only when analysis proves behavior equivalent under the ratified relation
and declared deterministic world. If that proof is unavailable, the explicit call remains. Kontrakt never substitutes a
same-type copy, catalog entry, naming convention, or backend conversion for the supplied implementation.

V1 optimization is concentrated on machinery Kontrakt owns and closed realization boundaries it can prove safe:
contract acquisition, deterministic planning, frozen material, generated verification gates, state enforcement,
automatic test generation and execution, diagnostics, structural cache planning, port linking, and publication control.
These facilities remove validation, test, diagnostic, control, and assembly boilerplate while preserving the user's
explicit adapter implementation. Any broader optimization of arbitrary core implementation requires separate authority
and analysis over effects, aliasing, escape, ownership, identity observability, concurrency, and representation.

### 6.7. Handoff to the Explicit Core

Successful Lowering means that every explicit one-to-one relation has been realized through the exactly bound generated
port implementation, the candidate Operation input material is complete, every applicable interface-level Invariant and
movement judgment has succeeded, input Fact authority has been established, and external-presentation authority has
ended.

The legal core-entry handoff invokes the ordinary user Operation with its declared parameter values under established
input Fact authority. It does not require an Operation Start DTO, candidate wrapper, established-Fact wrapper, or
Kontrakt-specific parameter type. The implementation receives no Input presentation, Lowering relation table, generated
port metadata, `LoweringPlan`, or judgment machinery.

The core thereafter operates only over explicitly bound established Facts and other declared immutable contract material
under the machine-wide Policy, Governance, Budget, and Capacity material fixed for the selected operation run. Internal
functions or stages remain replaceable implementation and do not open another airlock or contract pipeline. ADR-0049
defines result Fact establishment, standing Invariant application, legal availability, Publication, and Output
Presentation. It must not reopen the erased Input or Lowering relation as sources of core meaning.

## 7. Deferred Decisions

This ADR does not freeze the final token spelling, exact public canonical type names, or the exact Java or Kotlin
carrier syntax for host-facing Input, Admission, or Canonicalization declarations. Lowering relation placement and
`source -> parameter.coordinate` authoring are fixed here; the exact generated port ABI and assembly API remain deferred
below.

It does fix the Canonicalization authoring boundary: V1 may omit the `canonicalization` slot, select one complete flat
built-in symbol, or select one uninstantiable Java/Kotlin coordinate-law signature declaration. Omission declares no
semantic Canonicalization Contract and causes no implicit `ExactCanonicalization`, user callback, proxy, interceptor, or
generated replacement stage. A selected declaration must cover every selected Input coordinate exactly once by parameter
name and an applicable exact Kontrakt-owned nominal canonical type, without any shape-directed generic or
nested-signature inference. V1 exposes no canonical-law values, enums, singleton objects, constructors, factories,
application-defined canonical types, user-authored canonical output presentation, transformation method, `T -> T`
canonicalizer, or shape mapping. Each public canonical type must be accompanied by API specification and user
documentation derived from the same versioned law material.

It also fixes the Lowering authoring boundary. The operation manifest's `lowering` slot selects one exact handle. One
sibling `lowering` declaration inside the same operation contains the complete finite `source -> parameter.coordinate`
relation body. `facts` and `invariants` remain enclosing-interface declarations and are not repeated in the manifest or
the Lowering body. Every target coordinate formed by the inbound airlock is bound to exactly one selected Input
coordinate, even when names or host types are equal. Each Input coordinate may appear in at most one binding. V1 permits
no `1:N` or `N:1` contract-visible relation and no same-name inference.

Every selected Lowering declaration generates one required retained plain host-language realization port. Exactly one
implementation is supplied during machine assembly. Equal source and target types, a conversion catalog, a naming
convention, classpath contents, or backend support do not close the port implicitly. The implementation may use ordinary
libraries to realize the declared representation formation, but it may not perform repository lookup, environmental
resolution, business computation, Invariant judgment, State movement, or Publication.

The exact generated port method decomposition, result carrier, declared-failure encoding, and assembly API remain
backend/API decisions. Whatever form is selected must expose only the declared source material and target candidate
surface, preserve the one-to-one relation, permit exact implementation binding without runtime discovery, and remain
ordinary retained Java or Kotlin source.

The compiler derives the semantic `LoweringPlan`, port ABI, source-read closure, target-completion checks, judgment and
establishment handoff, verification, tests, diagnostics, cache plan, and optimization opportunities. It does not infer
or own the supplied conversion implementation. A backend may inline or erase the closed port only when equivalent
behavior is proven; otherwise the explicit implementation call remains.

This ADR does not define the complete authoring surface for additional Fact participation, Operation Result Material,
result-side Change formation, Publication, or Output Presentation. It fixes the inbound airlock, the ordinary Operation
parameter handoff, and the fact that the core may depend on explicitly established immutable information rather than
hidden object state.

It does not define the complete state and transition sets of the three-axis machine. State-machine manifests,
cross-contract movement, diagnostic evidence production, retention, failure representation, and Version remain
operation-flow decisions that must be designed across the complete flow rather than independently inside one processing
profile. Policy, Governance, Budget, and Capacity are instead bound once at the enclosing interface scope for the closed
set of operations because they coordinate shared finite resources. Their declarations may include machine-wide walls and
explicit operation allocations or run-grant profiles, but their complete processing languages remain outside this ADR.
They are not operation-manifest slots and do not acquire authority from implementation-stage placement.

It does not define a projection callback SPI. Mutable and framework-owned objects must be converted before Input through
an explicit adapter or presentation-formation operation, but the final adapter, factory, builder, or generated formation
surface and compatibility profiles remain frontend decisions. Direct flat immutable Kotlin data classes and Java records
require no second presentation declaration.

It does not select the final internal IR classes or backend data structures. Those implementations must realize the
authority and handoff fixed here without becoming new sources of meaning.

Allocation elimination, callback removal, cross-region fusion, and optimization of arbitrary unrelated core code are
intentionally outside V1. Kontrakt may optimize the compiler-derived `LoweringPlan`, generated port call path,
candidate-completion and establishment machinery, cache plan, and backend layout because their meaning is derived from
ratified contract material. It may optimize the supplied realization body only where analysis proves an equivalent
result. A later version may investigate a broader implementation IR or restricted frontend for core realization, but
that work requires separate authority, analysis, and ADRs.

## 8. Consequences

Boundary refinement and core entry now have explicit contract owners while the flow remains a three-axis machine. The
contract pipeline declares material and judgment, the implementation pipeline realizes boundary formation and performs
replaceable work, and the state pipeline permits legal movement and availability. None can impersonate another.

Input accepts explicitly formed immutable presentation material. Kontrakt does not repair mutable, lazy, proxied, or
framework-owned objects by taking a runtime snapshot. A flat immutable Kotlin data class or Java record may serve
directly as both host source evidence and runtime presentation without a duplicate declaration. Its directly named
coordinate shape is refined into contract material, while constructor execution and generated class machinery remain
implementation artifacts. Other ordinary Kotlin and Java carriers require explicit presentation formation before the
Input boundary.

Input and Admission share one immutable presentation and require no user transformation between them. Kontrakt treats
that submitted presentation as the flow's raw Input and makes no claim about values or processing that may have existed
before submission. After Admission, a selected Canonicalization law produces the stable representative and canonical
bytes. If the `canonicalization` slot is omitted, no semantic canonicalization occurs and the admitted presentation is
supplied unchanged to Lowering.

In both branches, one operation-local sibling Lowering declaration provides explicit one-to-one
Input-to-Operation-parameter-Fact coordinate bindings. The compiler derives one semantic `LoweringPlan` and one retained
plain host-language port; exactly one implementation is supplied explicitly. Equal coordinate names or equal host types
create no implicit binding or copy. Catalog lookup, naming convention, and backend support do not close the port. The
generated machine owns candidate completion, standing judgment, establishment, and Operation invocation.

Invalid or unratifiable material stops before later work pays its cost. Input formation failure does not reach
Admission. Admission rejection reaches neither selected canonical production nor Lowering realization. When
Canonicalization is selected, its refusal or an attributed Budget or Capacity stop during canonical production does not
enter Lowering. When it is omitted, an admitted presentation proceeds directly to the explicitly bound Lowering
realization behind the generated port. Lowering refusal means that no complete candidate input Fact can be established
for the selected Operation.

Successful Lowering completes the selected operation's inbound airlock and invokes the ordinary user Operation with
established input Fact authority attached outside the host value representation. An enclosing interface may expose
several operation pipelines, but their successful Lowering handoffs enter the same explicit core under the shared
machine-wide Policy, Governance, Budget, and Capacity contracts. The implementation sees ordinary Java or Kotlin
parameters and no external presentation, candidate wrapper, established-Fact wrapper, or Kontrakt orchestration. Result
Fact establishment, movement, Publication, and Output Presentation remain outside this ADR. Internal functions, objects,
stages, and call graph remain implementation and do not become nested IDL operations.

Fact richness no longer depends on a rich object model. Ordinary primitives and finite immutable products may nominate
Fact coordinates when they preserve the required information. The Fact Contract, not a class constructor, method, custom
equality implementation, Value Object name, or backend layout, owns factual meaning.

Canonicalization authoring exposes no executable law API. Users place only Kontrakt-owned nominal canonical type names
in an uninstantiable coordinate signature, while the API specification and user documentation explain the meaning of
each name. The frontend resolves exact symbols, erases the host signature, and grants authority only to the resulting
flat versioned law material.

V1 leaves arbitrary core implementation opaque and does not optimize its callbacks, allocations, dispatch, control flow,
or effects. The Lowering relation body contains no implementation, while its generated port closes exactly one
explicitly supplied realization. Kontrakt owns the relation plan, port ABI, assembly, candidate-completion checks,
judgment handoff, and establishment machinery. It may devirtualize, inline, specialize, or erase the supplied port only
when equivalence is proven; otherwise the ordinary adapter call remains. This preserves implementation replaceability
and Kontrakt removal while still enabling pass fusion, direct layout generation, structural caching, allocation control,
and other SOTA-grade specialization.

Kontrakt also derives verification, automatic tests, diagnostics, deterministic planning, state enforcement, frozen
material, generated gates, caching, and publication control from the same explicit contract source. The existing frozen
acquisition, planning, contract graph lowering, identity, and ContractImage machinery remain the physical basis. This
ADR adds role-specific law and three-axis coordination, not a parallel metamodel compiler, hidden snapshot layer, rich
domain object model, or implicit implementation machine.