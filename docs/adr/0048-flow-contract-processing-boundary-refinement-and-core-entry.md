# ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Status

Draft

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
does
not own contract meaning.

ADR-0047 fixes the selection boundary after that.

The operation manifest selects one-dimensional roles through explicit slots. A source does not declare itself to be
`Input`, `Admission`, `Canonicalization`, or `Lowering`. The operation supplies the boundary, and the slot supplies the
role.

The remaining problem is processing.

The first four flow roles all operate before accepted core fact exists, but they do not perform the same judgment.
`Input` establishes judgeable immutable boundary presentation. `Admission` decides continuation over that same
presentation. `Canonicalization` governs stable representative authority. `Lowering` governs the core-readable material
that may be handed toward Fact formation. Treating them as one generic validation or conversion stage would erase their
separate authority and would force expensive or invalid material farther into the machine than necessary.

These contracts do not form one self-executing pipeline. The same operation advances on three parallel axes:

```text
contract pipeline
implementation pipeline
state pipeline
```

The contract pipeline defines authority and judgment. The implementation pipeline performs the work between contract
boundaries and produces the material submitted to the next judgment. The state pipeline declares when each judgment,
implementation region, submission, refusal, and handoff is legally available. JVM objects, interfaces, callbacks, and
intermediate allocations are frontend and implementation mechanisms; they are not the authority model of the machine.

Input and Admission are the one direct contract adjacency in this ADR. Input establishes the immutable presentation that
Admission judges, so no user implementation transformation exists between them. After Admission, implementation regions
produce the candidates judged by Canonicalization and Lowering.

This ADR defines the processing profiles for those four flow contracts, their relationship to the implementation and
state axes, and their handoff to core acceptance.

Fact, Invariant, and Publication are defined separately by ADR-0049. Fact is not a stage placed before Invariant. An
implementation produces a Fact candidate, and successful Invariant judgment grants that candidate Fact authority before
Publication may use it.

Failure and diagnostics, movement, and bounds remain separate category concerns. This ADR names their interaction points
but does not define their complete processing profiles.

---

## 2. Problem

Outside material arrives through host-language and framework contracts that Kontrakt does not own.

Those contracts may be useful and explicit, but their implementation mechanics must not become Kontrakt authority. At
the same time, forcing users to restate every host contract in a second Kontrakt vocabulary would make the frontend
needlessly hostile and would ignore the practical limits of Kotlin and the JVM.

JVM languages also encourage implementation pipelines to be expressed through object allocation, interface dispatch,
callback invocation, and intermediate carrier objects. Kontrakt must accept those forms as implementation frontends
without mistaking them for contract stages, state authority, or an optimal physical machine.

Kontrakt therefore needs a flow that can:

- accept ordinary external contract evidence;
- require explicitly formed immutable Input presentation before Admission;
- refine external declaration meaning into explicit Kontrakt-owned obligation material;
- reject unratifiable sources before runtime;
- refuse malformed or unavailable invocation material before Admission;
- preserve three parallel axes for contract authority, implementation work, and legal state movement;
- stop material at the earliest contract that already owns enough authority to stop it;
- avoid repeating acquisition, planning, identity, and image work already performed by the existing machine;
- preserve a strict boundary between boundary formation, continuation judgment, equivalence, and core candidate
  authority; and
- optimize Kontrakt-owned verification, testing, planning, state enforcement, and diagnostics without rewriting the
  user-supplied implementation pipeline in V1.

The result must be usable as software and still satisfy the discipline of *What Contract Is*.

---

## 3. Decision Drivers

The machine must reject as early as possible without moving a later contract's judgment into an earlier role.

A source declaration may nominate external contract evidence, but only ratified Kontrakt-owned material may receive
contract authority.

A user must not be required to declare the same contract twice. A supported host contract must be refined by one
deterministic frontend law or rejected.

Input presentation must be explicit and immutable before it reaches Admission. Runtime snapshot timing, lazy
materialization,
proxy activation, and framework lifecycle must not become hidden Input meaning.

Input refusal, Admission rejection, canonicalization failure, and lowering failure must remain distinguishable because
they report different failed obligations.

The contract, implementation, and state pipelines must remain separate. Completion of implementation work does not grant
contract authority and does not by itself perform a legal state transition.

Existing frozen acquisition, planning, graph lowering, identity derivation, and ContractImage publication must be
reused.
The flow roles must not create parallel metamodel or planning machines.

A backend may fuse or specialize Kontrakt-owned verification and state machinery, but fusion must not merge declared
contract authority or state meaning.

V1 must not optimize, rewrite, fuse, devirtualize, or remove allocations inside user-supplied implementation code. The
implementation pipeline is an opaque realization region whose legal entry, supplied material, candidate output, failure
surface, and state participation are governed by Kontrakt.

Kontrakt-owned validation, deterministic planning, automatic test generation and execution, state enforcement,
diagnostics, frozen material, and generated gates remain optimization targets and must be implemented to a
state-of-the-art performance standard.

---

## 4. Decision

ADR-0048 defines four contract processing profiles on the contract axis:

```text
Input
-> Admission
-> Canonicalization
-> Lowering
```

This ordering expresses authority handoff. It is not a complete execution pipeline and does not imply that one contract
performs the implementation work needed to reach the next contract.

The operation advances on three parallel pipelines.

### 4.1. Contract Pipeline

The contract pipeline defines what material may receive authority at each boundary:

```text
explicit immutable boundary presentation
-> Input judgment
-> Admission judgment over the same presentation
-> Canonicalization judgment over an implementation-produced candidate
-> Lowering judgment over an implementation-produced candidate
-> lowered core material handed toward ADR-0049
```

Input and Admission inspect the same formed presentation. Canonicalization and Lowering judge candidates produced by the
implementation pipeline. A successful contract judgment grants the declared authority to submitted material; it does not
retroactively turn implementation code into contract authority.

### 4.2. Implementation Pipeline

The implementation pipeline performs the work that contracts intentionally leave open:

```text
Input -> Admission:
    no user transformation region

Admission -> Canonicalization:
    implementation produces a canonicalization candidate

Canonicalization -> Lowering:
    implementation produces a lowering candidate

Lowering -> ADR-0049:
    later implementation may use lowered material to produce a Fact candidate
```

A Kontrakt backend may generate a contract gate or a mechanical realization when the declared material fully determines
it. That generated code remains implementation. In V1, arbitrary user implementation remains opaque and is not rewritten
or optimized by Kontrakt.

### 4.3. State Pipeline

The state pipeline runs in parallel with both other axes. It declares when Input formation, Admission judgment,
implementation execution, candidate submission, contract ratification, refusal, and handoff are legal.

```text
implementation completed
    != contract authority granted

contract judgment succeeded
    != legal state transition completed

state label changed
    != contract judgment succeeded
```

The exact state and transition sets are deferred to the Movement category ADR. This ADR requires every contract judgment
and implementation handoff to occur under that explicit state machine; neither contract order nor callback completion
may
implicitly create movement.

### 4.4. Common Definition-Time Authority Path

The common contract-definition authority path is:

```text
slot-selected source coordinate
-> existing adapter-neutral frozen acquisition
-> FrozenMetamodelImage
-> role-specific planning and adapter-erased lowering
-> ratified Kontrakt-owned contract material
-> ContractImage-visible material
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
Each successful judgment narrows the material before the next implementation region pays its computation, allocation,
normalization, lowering, or global coherence cost.

This authority path is not a mandatory physical runtime schedule. Kontrakt-owned reads, gates, and state checks may be
specialized or fused where equivalent. User-supplied implementation code remains outside that V1 optimization authority,
and result ownership, failure attribution, and state movement must remain explicit.

---

## 5. Flow Processing Profiles

### 5.1. Input Contract

Input is the boundary presentation contract.

It declares which outside presentation may appear for an operation, which distinctions the boundary must preserve, and
which values later contracts may judge. It does not declare that outside material is true. Admission still decides
whether presented material may continue. Canonicalization still decides stable representation. Lowering and invariant
still decide what the core may believe.

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
coerce, discover, or otherwise reconstruct Input material. A source that has to perform such work before it can judge is
in the wrong slot.

An Admission source is ratifiable only when it satisfies the admission conditions below.

**Slot selection condition.** The operation manifest selects a judgment source through the `admission` slot. The
selected
source is only a candidate until Kontrakt lowers it into finite judgment material.

**Input dependency condition.** Admission is compiled against the ratified Input part table of the same operation. Every
read must resolve to material declared by that table or explicitly declared boundary context.

**Carrier and judgment separation condition.** A V1 direct Input DTO is an inert immutable presentation carrier. Except
for compiler-generated carrier machinery and inert canonical formation, it must not declare executable behavior.
User-declared validation, normalization, derived access, judgment, helper methods, custom getters, delegated properties,
`init` behavior, compact-constructor behavior, or semantic secondary constructors disqualify the type from the direct
Input source profile.

Admission belongs to the operation slot, not to the carrier type. A method such as `isValid`, `validate`,
`isAdmissible`, or `canProceed` does not become Admission authority because it is declared on the same host type, and V1
does not silently ignore that co-location while continuing to treat the type as a direct Input DTO. The same immutable
presentation may participate in different operations with different Admission laws; therefore no carrier-wide method may
own continuation judgment.

The frontend distinguishes unavoidable host-generated carrier machinery from user-declared behavior. Kotlin-generated
property accessors, `componentN`, `copy`, `equals`, `hashCode`, and `toString`, and Java record accessors, `equals`,
`hashCode`, and `toString`, are implementation artifacts and are erased from contract authority. A Kotlin primary
constructor or Java canonical record constructor is admissible only as inert formation that receives and stores the
already-declared coordinates. It must not perform validation, normalization, default substitution, capability access,
lazy execution, or lifecycle-dependent observation.

**Finite judgment condition.** V1 admission lowers to bounded boolean judgment material. Primitive comparison, primitive
equality, enum checks, null checks, boolean composition, and primitive integral bit operations are allowed when every
operand is material made available under the ratified Input Contract or a literal.

**No executable authority condition.** A user function or method may carry the source text. It is not executed as the
contract authority. The generated Kontrakt gate may run only after the frontend has lowered the source into judgment
material.

**Source visibility condition.** The frontend must see enough source to reject unsupported constructs before runtime. A
method that can only be found and invoked from bytecode is implementation machinery.

For the JVM V1 implementation profile, the following declarations may satisfy these conditions:

```text
Kotlin source-visible top-level function
Kotlin source-visible function in a dedicated object that is not the Input carrier
Java source-visible static method on a dedicated final judgment holder
```

They are frontend authoring choices, not contracts. They are chosen because Kotlin and Java users can write them
naturally while Kontrakt can still read the source body instead of trusting a runtime function object. The Admission
source may be located near the carrier in the project, but it must remain a separate declaration and authority surface.

Java record component access and Kotlin primary-constructor property access are valid only as reads of already ratified
input parts. Kotlin bit-operation functions are valid only when the frontend lowers them as primitive integer
operations.

Java lambdas, Kotlin lambdas, method references, `Predicate`, `Function`, and functional-interface instances remain
outside the V1 admission profile. They are runtime objects with capture and dispatch behavior, not named source bodies
selected by the slot.

A V1 boolean admission maps `true` to admitted and `false` to rejected. Deferred or capacity-shaped outcomes must come
from an explicit capacity, budget, policy, or later admission-result profile.

If the admission source cannot be lowered into finite judgment material, the contract source is rejected. If Input
material fails the lowered judgment, Admission returns the declared rejection result. A backend may realize Admission as
a gate. The gate is wrong if it disagrees with the lowered admission material.

### 5.3. Canonicalization Contract

Canonicalization declares the law under which an implementation-produced candidate may receive stable representative
authority.

It is not cleanup, and the contract does not perform the implementation work. After Admission, the implementation
pipeline may compute, normalize, aggregate, select, or otherwise construct a candidate. Canonicalization then judges
that
candidate against the declared equivalence and representative law.

The contract must declare which presentation differences preserve meaning, which distinctions must remain observable,
which representative is admissible, and when no safe representative exists. The implementation may choose mechanics only
inside those declared obligations. It must not merge different meanings for convenience.

A successful Canonicalization judgment grants stable representative authority to the submitted material. Implementation
completion alone does not. In V1, Kontrakt governs the entry material, candidate boundary, judgment, failure
attribution,
and state participation, but does not rewrite or optimize arbitrary user canonicalization code.

### 5.4. Lowering Contract

Lowering declares the law under which an implementation-produced candidate may receive core-readable lowered-material
authority.

A canonical representative does not lower itself. The implementation pipeline performs the representation work between
Canonicalization and Lowering: removing host-facing shape, constructing the required core layout, supplying declared
coordinates, and preserving the meaning fixed by the upstream contracts. Lowering judges the resulting candidate.

The candidate may claim to be factual input material, movement material, or another closed core-facing form named by the
operation. Lowering declares what representation and claim are admissible under the selected contract world. It does not
make the machine believe a factual claim and does not produce Fact authority.

A successful Lowering judgment grants lowered core-material authority to the submitted candidate. Implementation
completion alone does not. Fact candidate formation, Invariant judgment, Fact authority, and Publication are governed by
ADR-0049.

## 6. Cross-Profile Boundaries

### 6.1. Three-Axis Coordination

The contract, implementation, and state pipelines are parallel authorities over one operation.

```text
Contract pipeline:
    declares judgment and grants or refuses authority

Implementation pipeline:
    performs work and submits candidate material

State pipeline:
    permits and records legal movement
```

No axis substitutes for another. An implementation result is only a candidate until the owning contract judges it. A
successful contract judgment does not move the machine unless the declared transition is legal. A state label does not
prove that either implementation work or contract judgment occurred.

Input and Admission are the only direct contract adjacency in this ADR because Admission judges the immutable
presentation formed under Input without an intervening user transformation. The later contract boundaries receive
candidates from the implementation pipeline.

### 6.2. Earliest Authoritative Rejection

Each flow contract stops only the material it has authority to judge.

Input stops material that cannot be formed as the declared immutable boundary presentation. Admission stops formed
material that may not continue. Canonicalization refuses a submitted candidate that cannot receive stable representative
authority under the declared equivalence law. Lowering refuses a submitted candidate that cannot receive the required
core-readable lowered-material authority.

A condition must have one owning contract. Executing the same condition in multiple stages is not defensive safety; it
is
duplicate authority and duplicate cost.

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

### 6.4. Candidate Production and Contract Authority

After Admission, implementation work is required to produce the material submitted to the later contracts.

```text
admitted presentation
-> implementation
-> canonicalization candidate
-> Canonicalization judgment
-> stable representative
-> implementation
-> lowering candidate
-> Lowering judgment
-> lowered core material
```

The implementation pipeline owns computation and construction. Canonicalization and Lowering own authority over the
submitted result. Neither contract should be described as silently performing the user's implementation work, and the
implementation must not grant its own result contract authority.

### 6.5. Cross-Cutting Gates

Policy, Budget, Capacity, Governance, Version, or another cross-cutting contract may be executable during any of these
judgments or implementation regions when the needed evidence becomes available.

Early execution does not transfer authority. A Capacity stop observed during Input remains a Capacity result. A Budget
stop observed during canonicalization implementation remains a Budget result. The supplying contract owns the declared
outcome and its diagnostic attribution, and a later boundary must not repeat the same gate.

### 6.6. V1 Implementation Optimization Boundary

Kontrakt V1 does not optimize, rewrite, fuse, devirtualize, or otherwise reinterpret user-supplied implementation code.
The implementation pipeline remains an opaque realization region. Kontrakt governs its legal entry, supplied material,
state prerequisites, declared failure surface, candidate completion boundary, and subsequent contract judgment, but it
does not alter internal dispatch, allocation, control flow, effect ordering, object layout, or capability interaction.

V1 optimization is concentrated on machinery Kontrakt owns: contract acquisition, deterministic planning, frozen
material, generated verification gates, state enforcement, automatic test generation and execution, diagnostics, cache
and publication control. These facilities replace validation, test, and control work that users would otherwise have to
build repeatedly, and they are the SOTA-grade optimization target of V1.

Optimization of arbitrary implementation code requires different authority and analysis over effects, aliasing, escape,
ownership, identity observability, concurrency, and representation. It is deferred to a later version and must not
expand
the V1 runtime or contract surface.

### 6.7. Handoff to Core Acceptance

Successful Lowering means that an implementation-produced lowering candidate has received lowered core-material
authority under the selected contract world.

That material is not yet a Fact. A later implementation region may use it and runtime-only information to produce a Fact
candidate. ADR-0049 defines the Fact Contract, the Invariant judgment whose successful branch grants Fact authority, and
the Publication contract that governs outward claims derived from Fact material.

---

## 7. Deferred Decisions

This ADR does not decide the final host-facing syntax for Input, Admission, Canonicalization, or Lowering bodies.

It does not define the complete state and transition sets of the three-axis flow machine. State-machine manifests,
cross-contract movement, diagnostic evidence production, retention, failure representation, policy reaction, Budget,
Capacity, Version, and Governance are category-level decisions and must be designed across the complete operation rather
than independently inside one processing profile.

It does not define a projection callback SPI. Mutable and framework-owned objects must be converted before Input through
an explicit adapter or presentation-formation operation, but the final adapter, factory, builder, or generated formation
surface and compatibility profiles remain frontend decisions. Direct transitively immutable Kotlin data classes and Java
records require no second presentation declaration.

It does not select the final internal IR classes or backend data structures. Those implementations must realize the
authority and handoff fixed here without becoming new sources of meaning.

Implementation lowering, allocation elimination, callback removal, cross-region fusion, and optimization of arbitrary
user code are intentionally outside V1. A later version may investigate an implementation IR or restricted frontend, but
that work requires separate authority, analysis, and ADRs.

---

## 8. Consequences

Boundary refinement and core entry now have explicit contract owners while the operation remains a three-axis machine.
The contract pipeline grants authority, the implementation pipeline performs work and submits candidates, and the state
pipeline permits legal movement. None can impersonate another.

Input accepts explicitly formed immutable presentation material. Kontrakt does not repair mutable, lazy, proxied, or
framework-owned objects by taking a runtime snapshot. A transitively immutable Kotlin data class or Java record may
serve
directly as both host source evidence and runtime presentation without a duplicate declaration. Its record-like shape is
refined into contract material, while constructor execution and generated class machinery remain implementation
artifacts.
Other ordinary Kotlin and Java carriers require explicit presentation formation before the Input boundary.

Input and Admission share one immutable presentation and require no user transformation between them. After Admission,
implementation regions produce candidates for Canonicalization and Lowering. A successful implementation call does not
grant authority; the owning contract judgment and legal state transition remain required.

Invalid or unratifiable material stops before later work pays its cost. Input formation failure does not reach
Admission.
Admission rejection does not enter canonicalization implementation. Canonicalization refusal does not enter lowering
implementation. Successful Lowering reaches ADR-0049 as lowered core material, not as Fact.

V1 leaves user implementation opaque and does not optimize its callbacks, allocations, dispatch, control flow, or
effects.
Kontrakt instead concentrates SOTA-grade optimization on the verification system, deterministic planning, automatic test
generation and execution, state enforcement, diagnostics, frozen material, generated gates, caching, and publication
control that it owns.

The existing frozen acquisition, planning, contract graph lowering, identity, and ContractImage machinery remain the
physical basis. This ADR adds role-specific law and three-axis coordination, not a parallel metamodel compiler, hidden
snapshot layer, or implementation optimizer.