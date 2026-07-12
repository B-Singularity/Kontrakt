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
`Input` establishes judgeable boundary presentation. `Admission` decides continuation. `Canonicalization` establishes a
stable representative. `Lowering` forms a core-owned candidate. Treating them as one generic validation or conversion
stage would erase their separate authority and would force expensive or invalid material farther into the machine than
necessary.

This ADR defines the processing profiles for those four flow contracts and their handoff to core acceptance.

Fact, Invariant, and Publication are defined separately by ADR-0049 because they govern the point at which a lowered
candidate receives core authority and the point at which accepted material may become externally visible.

Failure and diagnostics, movement, and bounds remain separate category concerns. This ADR names their interaction points
but does not define their complete processing profiles.

---

## 2. Problem

Outside material arrives through host-language and framework contracts that Kontrakt does not own.

Those contracts may be useful and explicit, but their implementation mechanics must not become Kontrakt authority. At
the same time, forcing users to restate every host contract in a second Kontrakt vocabulary would make the frontend
needlessly hostile and would ignore the practical limits of Kotlin and the JVM.

Kontrakt therefore needs a flow that can:

- accept ordinary external contract evidence;
- refine it into explicit Kontrakt-owned obligation material;
- reject unratifiable sources before runtime;
- refuse malformed or unavailable invocation material before Admission;
- stop material at the earliest contract that already owns enough authority to stop it;
- avoid repeating acquisition, planning, identity, and image work already performed by the existing machine; and
- preserve a strict boundary between boundary formation, continuation judgment, equivalence, and core candidate
  formation.

The result must be usable as software and still satisfy the discipline of *What Contract Is*.

---

## 3. Decision Drivers

The machine must reject as early as possible without moving a later contract's judgment into an earlier role.

A source declaration may nominate external contract evidence, but only ratified Kontrakt-owned material may receive
contract authority.

A user must not be required to declare the same contract twice. A supported host contract must be refined by one
deterministic frontend law or rejected.

Input refusal, Admission rejection, canonicalization failure, and lowering failure must remain distinguishable because
they report different failed obligations.

Existing frozen acquisition, planning, graph lowering, identity derivation, and ContractImage publication must be
reused.
The flow roles must not create parallel metamodel or planning machines.

A backend may fuse or specialize runtime execution, but fusion must not merge the declared authority of the four
contracts.

---

## 4. Decision

ADR-0048 defines four flow processing profiles:

```text
Input
-> Admission
-> Canonicalization
-> Lowering
```

Their contract handoff is:

```text
outside presentation
-> Input
judgeable boundary presentation
-> Admission
continuation-permitted material
-> Canonicalization
stable representative
-> Lowering
core-owned candidate
```

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

A stage must not defer a judgment it already owns. It must also not pull forward a judgment owned by a later contract.
Each successful stage narrows the material before the next stage pays its normalization, allocation, lowering, or global
coherence cost.

This authority path is not a mandatory runtime schedule. Generated execution may fuse reads and gates where equivalent,
but the result and failure attribution must remain owned by the contract whose judgment was executed.

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
outside carrier instance
-> generated boundary reader or static gate
   -> declared presentation formed -> Admission
   -> declared presentation refused -> declared Input stop
   -> delegated early gate stops -> result owned by the supplying contract
```

The generated boundary realization may read an immutable carrier directly or may create a transient snapshot when
mutation, lazy access, or framework lifecycle makes direct reading unsafe. That capture may be fused, specialized, or
omitted. It is not a new contract stage, a frozen image, a graph unit, or a mandatory `FormedBoundaryMaterial` domain
type. Runtime invocation must not repeat metamodel acquisition, source resolution, planning, graph ratification,
canonical identity derivation, or contract publication.

The V1 admissibility law is:

```text
Input may accept any external presentation contract that can be deterministically refined into explicit, finite,
inspectable, loss-accounted, root-owned boundary material without allowing external implementation mechanics, pointer
topology, hidden movement, or another contract role to survive refinement.
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

**Stable boundary access.** External carrier lifecycle must not influence later judgment. Generated machinery may read a
stable immutable carrier directly. A mutable, lazy, proxied, or framework-owned carrier requires adapter projection or a
transient snapshot before its values are used by Admission. That snapshot is realization-local; the Input Contract owns
the declared values and distinctions, not a particular snapshot object or storage layout.

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

**Interface implementation boundary.** A user-defined interface, an interface-typed part, or a runtime implementation
relationship does not provide Input authority. V1 must not derive Input parts or meaning through interface ancestry,
runtime implementation discovery, default methods, override dispatch, or implementation-provided access behavior. A
final closed declaration may still be ratified when it independently satisfies the direct-source conditions; any
interface relation is erased and contributes no Input material. Recognized standard carrier interfaces are admitted only
through dedicated deterministic refinement profiles, and their concrete runtime implementations contribute no contract
meaning. Any other interface-based source requires projection or is rejected.

**Generic closure.** Every generic argument in the resolved graph must itself resolve as Input material. The outer
carrier
may describe structure, but its arguments cannot remain raw, wildcarded, star-projected, platform-erased, or otherwise
open. A reusable generic declaration is a source template until the selected operation closes every argument.

**Ordered multiplicity.** `List<T>` and array-like declarations may resolve automatically as ordered multiplicity when
`T` resolves and the carrier can provide stable ordered access. Collection nesting contributes to declaration depth;
runtime element count belongs to capacity and budget. The concrete collection implementation contributes no contract
meaning.

**Set refinement.** `Set<T>` is an explicit external contract choice. Under the JVM V1 profile, Kontrakt refines it as
a membership presentation of `T`: repeated occurrence and sequence position are not observable at this boundary.
Kontrakt accepts the members the carrier presents but does not adopt the carrier's equality, hashing, iteration, or
storage mechanics as its own law. If the operation's Input obligations require repeated occurrences or order, `Set<T>`
cannot realize them and a loss-preserving carrier or adapter is required.

**Map refinement.** `Map<K, V>` is an explicit external contract choice. Under the JVM V1 profile, Kontrakt refines it
as a key-value association presentation in which each represented key has one represented value. Duplicate-key
occurrence and original entry order are not observable at this boundary. Kontrakt does not adopt the carrier's equality,
hashing, comparator, iteration, or storage mechanics. If the operation's Input obligations require duplicate-key
evidence or entry order, `Map<K, V>` cannot realize them and an entry sequence or adapter is required.

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

| Authoring class                                                      | V1 treatment                                                     | Examples                                                                                                                                                                                                                                                                                          | Contract effect                                                                                                                          |
|----------------------------------------------------------------------|------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| Deterministically refinable external contract                        | Refine automatically                                             | Scalar, enum, closed product, finite nested product, closed generic, `List<T>`, `Set<T>`, `Map<K, V>`, nullable part, source-visible sealed hierarchy, stable array                                                                                                                               | The host declaration is treated as explicit external contract evidence and lowered through one fixed profile without duplicate authoring |
| External contract that cannot preserve the required Input obligation | Reject the direct source or require a loss-preserving projection | Lossy serializer product, `Set<T>` when repeated occurrences or order are required, `Map<K, V>` when duplicate keys or entry order are required, nullable carrier when absence must remain distinct, unsupported scalar semantics, open generic or hierarchy                                      | Kontrakt invents no missing meaning; an adapter supplies a presentation that can satisfy the obligation                                  |
| Implementation-shaped carrier                                        | Require adapter projection or an explicit compatibility profile  | Mutable JavaBean, inherited DTO, user-defined interface root or interface-typed part, runtime implementation-discovered source, framework DTO, serializer product, proxy, entity, custom getter, delegated property, third-party object, dynamic JSON object, recursive or shared-reference graph | Host conventions and implementation relationships are removed before Input meaning is ratified                                           |
| Behavior, capability, movement, or role leakage                      | Reject from Input                                                | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core fact, state, backend handle                                                                                                                                                 | The material belongs to another role or is not contract data                                                                             |

For the JVM V1 zero-adapter source profile, Kotlin data classes, Kotlin final classes whose ratified parts are
primary-constructor `val` properties, and Java 17+ records may satisfy the direct-source conditions. Kotlin part order
is
the declared primary-constructor property order. Java part order is record-component order. A declaration does not lose
eligibility merely because it implements an interface, but no part or meaning may be obtained from that interface
relationship. A root declared as a user-defined interface, or a part whose Input surface depends on interface dispatch,
does not satisfy the zero-adapter profile.

A source-visible sealed hierarchy may participate in the zero-adapter profile when the selected frontend can close the
complete alternative set and every payload from frozen source facts. It is refined automatically under the fixed
closed-choice law above; the user does not restate the hierarchy in Kontrakt vocabulary. The explicit finite alternative
set becomes Input material, while Kotlin or Java ancestry does not.

These declarations are source conveniences, not contract authority. Equivalent presentation material may later come from
another language, schema compiler, serialization system, or generated frontend without changing the Input Contract.

Inherited carrier shape, JavaBean discovery, mutable framework objects, proxy objects, custom getter surfaces, delegated
properties, third-party objects, recursive graphs, and dynamically typed containers remain usable through adapter
projection or a separately declared compatibility profile. The adapter must project them into a source shape whose
operation-specific Input meaning can be resolved and lowered through the same frozen-image and planning path. The rule
is
not to reject ordinary host objects. The rule is to stop ordinary implementation structure from becoming contract law.

Callbacks, capabilities, live resources, and asynchronous control surfaces are not rescued by calling them opaque. If an
operation needs to refer to one, Input may carry an explicit identifier, token, coordinate, source text, byte sequence,
or other declared data representation. Execution and resource ownership remain outside the Input Contract.

If the source cannot be resolved under these laws, or if no safe deterministic refinement exists, the contract source
is rejected during planning or lowering. No ratified Input graph, ContractImage-visible Input authority, or runtime
boundary realization is produced for that source.

If a ratified Input contract exists but invocation-time boundary access cannot form the declared presentation because
required material is unavailable, structurally incompatible, malformed under the declared representation, distinction-
losing, or unstable, the machine produces the declared Input failure before Admission begins. A Capacity, Budget,
Policy,
Governance, or other cross-cutting gate may also stop execution during boundary access, but that result remains owned by
the supplying contract and is not Input failure.

### 5.2. Admission Contract

Admission is the continuation judgment over values made available under the ratified Input Contract.

It asks whether those boundary values may continue through this operation. Whether generated machinery reads them
directly from a stable carrier or from a transient realization-local capture is not Admission meaning. Admission depends
on Input, but it must not create Input. A source that has to parse, coerce, discover, or construct the material it
judges
is in the wrong slot.

An admission implementation is acceptable only when it satisfies the admission conditions below.

**Slot selection condition.** The operation manifest selects a judgment source through the `admission` slot. The
selected
source is only a candidate until Kontrakt lowers it into finite judgment material.

**Input dependency condition.** Admission is compiled against the ratified Input part table of the same operation. Every
read must resolve to material declared by that table or explicitly declared boundary context.

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
Kotlin source-visible object or companion object function
Java source-visible static method on a final class
```

They are implementation choices, not contracts. They are chosen because Kotlin and Java users can write them naturally
while Kontrakt can still read the source body instead of trusting a runtime function object.

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

Canonicalization gives one declared meaning one stable representative.

It is not cleanup. It is the machine refusing to let outside presentation decide internal identity.

Kontrakt lowers canonicalization into the law that decides which presentation differences preserve meaning and which
representative stands for that meaning. It must also know when a representative cannot be produced safely.

A backend may choose the algorithm. It may not merge different meanings for convenience.

### 5.4. Lowering Contract

Lowering forms core-readable candidate material.

Canonicalization gives the machine a stable representative. Lowering uses that representative to form the candidate the
core can discuss. The result is not truth.

Kontrakt lowers this role into candidate material with a declared claim. The candidate may claim to be factual material,
a movement candidate, or another closed candidate form named by the pipeline.

Lowering says what the candidate claims to be. It does not make the machine believe the claim.

## 6. Cross-Profile Boundaries

### 6.1. Earliest Authoritative Rejection

Each flow contract stops only the material it has authority to judge.

Input stops material that cannot be formed as the declared boundary presentation. Admission stops formed material that
may not continue. Canonicalization stops material for which the declared equivalence law cannot produce a safe stable
representative. Lowering stops stable material that cannot be expressed as the required core-owned candidate.

A condition must have one owning contract. Executing the same condition in multiple stages is not defensive safety; it
is
duplicate authority and duplicate cost.

### 6.2. Input Refusal and Admission Rejection

Input refusal and Admission rejection are different results.

```text
Input refusal:
    no judgeable boundary presentation was formed

Admission rejection:
    a judgeable boundary presentation was formed,
    but continuation was not permitted
```

Admission must not run after Input refusal. Conversely, Input must not reject material merely because Admission will
later
reject it.

### 6.3. Presentation Equivalence and Core Entry

Canonicalization handles declared equivalence in presentation. Lowering uses the stable representative to form a
core-owned candidate.

Canonicalization must not claim core truth. Lowering must not claim invariant acceptance. The core candidate may be
structurally ready for core judgment while still being false, incoherent, conflicting, or otherwise unacceptable.

### 6.4. Cross-Cutting Gates

Policy, Budget, Capacity, Governance, Version, or another cross-cutting contract may be executable during any of these
stages when the needed evidence becomes available.

Early execution does not transfer authority. A Capacity stop observed during Input remains a Capacity result. A Budget
stop observed during Canonicalization remains a Budget result. The supplying contract owns the declared outcome and
its diagnostic attribution, and a later stage must not repeat the same gate.

### 6.5. Handoff to Core Acceptance

Successful Lowering produces a core-owned candidate in the representation required by the selected contract world.

The candidate is not yet an accepted Fact. ADR-0049 defines the factual claim under which the candidate is presented,
the invariant judgment that may grant accepted core authority, and the Publication contract that governs any outward
claim derived from accepted material.

---

## 7. Deferred Decisions

This ADR does not decide the final host-facing syntax for Input, Admission, Canonicalization, or Lowering bodies.

It does not define the complete state and transition sets of the flow pipeline. State-machine manifests, cross-contract
movement, diagnostic evidence production, retention, failure representation, policy reaction, budget, capacity, version,
and governance are category-level decisions and must be designed across the complete pipeline rather than independently
inside one processing profile.

This ADR also does not select the final internal IR classes or backend data structures. Those implementations must
realize
the authority and handoff fixed here without becoming new sources of meaning.

---

## 8. Consequences

Boundary refinement and core entry now have explicit contract owners.

Ordinary Kotlin and Java declarations remain usable as external contract evidence. Kontrakt refines supported forms
through deterministic laws and rejects or projects forms that cannot preserve the required Input obligations. Users are
not required to repeat a host contract in a second vocabulary.

Invalid or unratifiable material stops before later stages pay their cost. Input formation failure does not reach
Admission. Admission rejection does not reach Canonicalization. Canonicalization failure does not reach Lowering. A
successfully lowered candidate reaches ADR-0049 without being mislabeled as accepted truth.

The existing frozen acquisition, planning, graph lowering, identity, and ContractImage machinery remain the physical
basis. This ADR adds role-specific law, not a parallel compiler or runtime pipeline.