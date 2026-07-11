# ADR-0048: One-Dimensional Machine Obligations and Processing Profiles

## Status

Draft

## Date

2026-07-09

## Related

- `docs/what-contract-is.md`
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

The interface remains the user-facing contract surface. The method remains the operation handle. A generated host
interface may make that handle pleasant to call, but it is still machinery. It does not own the contract.

ADR-0047 fixes the selection boundary after that.

Once an operation exists, Kontrakt must not ask the user's material to mark itself as a Kontrakt contract. The role does
not come from the carrier. The role comes from the operation slot. The operation gives the boundary. The slot gives the
role.

That still leaves one question.

After a slot selects a role, Kontrakt must know how to process the material that fills that role. `Input` cannot be
processed like `Invariant`. `State` cannot be processed like `Publication`. `Budget` cannot be processed like
`Diagnostic Evidence`. Their names alone do not tell the machine what to acquire, what to lower, or where implementation
must stop speaking.

This ADR defines those processing profiles for the remaining eighteen one-dimensional presentations.

It does not choose the final authoring syntax or host API shape. It decides the boundary of meaning: what kind of
material a role may receive, what Kontrakt must lower into its own canonical material, and which part is only backend
realization.

---

## 2. Problem

The remaining one-dimensional presentations are all contract presentations, but they do not have the same physical
character.

Some describe the shape of material at a boundary. Some produce a judgment. Some form stable meaning. Some govern
movement. Some explain what happened. Some declare the limits and the valid contract world under which the machine may
operate.

If Kontrakt processes all of them as ordinary host code, implementation regains authority. If it treats them as backend
features, the contract becomes a feature catalog. If it forces every small body into one new language surface, the
surface becomes heavier than the obligation.

The machine needs role-specific processing without letting role-specific processing become implementation authority.

---

## 3. Decision Drivers

A processing profile must start from the machine obligation.

The source may be carried by software, but the contract meaning must become Kontrakt-owned material before backend
machinery acts on it. Data-like roles should stay data-like. Judgment roles may need richer expression, but opaque host
behavior must not become the source of truth.

Lowering and invariant must stay apart. Lowering forms a candidate. Invariant decides whether the machine may believe
what the candidate claims.

The cross-cutting roles must stay on their own surfaces. They touch the same machine, but they do not answer the same
question.

Backend machinery may change whenever the canonical material does not change.

---

## 4. Decision

Kontrakt will define a processing profile for each remaining one-dimensional presentation.

The Interface Surface Contract is already decided by ADR-0046. This ADR covers the other eighteen presentations.

A processing profile decides the nature of the role, the character of the source material it may receive, the canonical
material Kontrakt must own after lowering, and the line after which backend machinery is only realization.

The common authority path is:

```text
slot-selected source coordinate
-> existing adapter-neutral frozen acquisition
-> FrozenMetamodelImage
-> role-specific planning and adapter-erased lowering
-> ratified Kontrakt-owned contract material
-> backend realization behind that material
```

A role does not own a duplicate acquisition engine or frozen image. Frozen acquisition establishes operation-neutral
source facts. Role-specific planning decides which facts matter for the selected obligation. Lowering and ratification
turn that decision into contract authority.

This is not a runtime schedule. It is the path by which meaning stops belonging to the carrier and starts belonging to
Kontrakt.

---

## 5. Presentation Profiles

### 5.1. Input Contract

Input is the boundary presentation contract.

It declares which outside presentation may appear for an operation, which distinctions the boundary must preserve, and
which values later contracts may judge. It does not declare that outside material is true. Admission still decides
whether presented material may continue. Canonicalization still decides stable representation. Lowering and invariant
still decide what the core may believe.

A DTO or other host object therefore has no contract authority. It is only one way outside material may arrive.

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
-> adapter-erased lowered input fact
-> ratified contract graph unit
-> published contract material and generated boundary realization
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
-> optional realization-local capture
-> Admission
```

The generated boundary realization may read an immutable carrier directly or may create a transient snapshot when
mutation, lazy access, or framework lifecycle makes direct reading unsafe. That capture may be fused, specialized, or
omitted. It is not a new contract stage, a frozen image, a graph unit, or a mandatory `FormedBoundaryMaterial` domain
type. Runtime invocation must not repeat metamodel acquisition, source resolution, planning, graph ratification,
canonical identity derivation, or contract publication.

The V1 admissibility law is:

```text
Input may accept any presentation that can be resolved into explicit, finite, inspectable, loss-accounted,
root-owned boundary material without importing hidden behavior, pointer topology, external authority,
or another contract role.
```

The authoring law is:

```text
Repeat no fact whose contract interpretation is already unique.
Require every choice whose contract interpretation is not unique.
```

This law is how the Input API carries the discipline of *What Contract Is*. Kontrakt must not force users to restate a
closed product shape that the selected source already determines uniquely. It must also refuse to make a silent semantic
choice merely because a host language or framework has a convenient convention.

Three forms of hidden meaning are rejected.

**Hidden authority** exists when implementation structure decides contract meaning. Inheritance, override dispatch,
constructor execution, getter algorithms, framework annotations, serializer conventions, collection implementations,
`equals`, `hashCode`, and proxy behavior cannot become Input authority.

**Hidden choice** exists when one host form admits more than one contract interpretation. `Set`, `Map`, nullable fields,
default arguments, sealed hierarchies, external scalar types, and lossy serializer products require an explicit semantic
choice whenever their contract meaning is not already unique.

**Hidden movement** exists when observing the alleged input performs behavior or depends on time. Callbacks, lazy
loading,
live streams, futures, suppliers, services, capabilities, and resource handles are not Input material.

An input implementation is acceptable only when it satisfies the conditions below.

**Root selection and explicit resolution.** The operation manifest selects the root input through the `input` slot. The
authoring surface may name only that root, but planning must resolve the complete operation-specific Input presentation
from `FrozenMetamodelImage` before runtime realization is generated. Every visible part, variant, collection shape,
generic argument, presence distinction, source coordinate, explicit semantic choice, and resolution failure belongs to
the lowered Input material. Authoring may be compact. Contract material may not be implicit.

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

**Unique interpretation may be resolved.** Kontrakt may resolve a source fact automatically only when the Input
interpretation is singular under the selected profile. Primitive and scalar values, enums as scalar choices, closed
products, finite nested products, closed generic products, and ordered sequences are examples when all subordinate
material also resolves. Automatic resolution removes repeated authoring; it does not make the host declaration contract
authority.

**Semantic choice must be declared.** When a host form can carry more than one contract meaning, Kontrakt must not
choose
silently. The user must select the intended Input interpretation through an explicit contract surface. ADR-0048 does not
fix the final syntax, but the choice must be visible in authored material, preserved in the resolved Input graph, and
included in contract identity where it changes meaning.

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

**Generic closure.** Every generic argument in the resolved graph must itself resolve as Input material. The outer
carrier
may describe structure, but its arguments cannot remain raw, wildcarded, star-projected, platform-erased, or otherwise
open. A reusable generic declaration is a source template until the selected operation closes every argument.

**Ordered multiplicity.** `List<T>` and array-like declarations may resolve automatically as ordered multiplicity when
`T` resolves and the carrier can provide stable ordered access. Collection nesting contributes to declaration depth;
runtime element count belongs to capacity and budget. The concrete collection implementation contributes no contract
meaning.

**Set acknowledgment.** `Set<T>` may be used only when authored contract material explicitly accepts unordered
multiplicity and acknowledges that duplicate evidence may already have been removed by the carrier. Kontrakt does not
adopt the carrier's equality, hashing, or iteration behavior as contract law. If duplicate evidence or contract-owned
equality matters, the boundary must present an ordered element sequence instead.

**Map acknowledgment.** `Map<K, V>` may be used only when authored contract material explicitly accepts a
carrier-collapsed
key-value presentation and acknowledges that duplicate-key evidence and original entry order may be unavailable. If key
collapse, duplicate selection, or entry order remains contract-relevant, the boundary must present an entry sequence.

**Closed choice acknowledgment.** A finite variant presentation is valid Input material, but a host inheritance relation
does not automatically become that presentation. An enum may resolve directly as a finite scalar choice. A
source-visible
sealed hierarchy may carry a tagged choice only after authored contract material explicitly selects that interpretation,
closes the variant set, declares the relevant discriminator law, and defines the handling of an unknown variant.
Planning
then flattens every accepted variant into root-owned material and removes ancestry from contract authority. Open
polymorphic hierarchies and runtime subtype discovery do not satisfy the V1 Input condition.

**Absence and default acknowledgment.** A nullable host part does not by itself decide whether absence and present-null
are
the same presentation. When that distinction matters, the contract must declare it and the carrier must preserve it. A
host-language constructor default or serializer default is not a contract default. Default meaning enters Input only
through explicit contract material; otherwise defaulting remains adapter convenience and cannot change contract
identity.

**External scalar acknowledgment.** A foreign scalar type may carry Input data when its boundary representation is
explicit. Host-specific equality, scale, timezone, locale, normalization, parsing, or rounding rules are not inherited.
When those rules affect meaning, the contract must declare the chosen representation or an adapter must project the
value
into declared scalar material.

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

| Authoring class                                 | V1 treatment                                                    | Examples                                                                                                                                                                                           | Contract effect                                                                        |
|-------------------------------------------------|-----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| Uniquely interpretable data                     | Resolve automatically                                           | Scalar, enum, closed product, finite nested product, closed generic, `List<T>`, stable array                                                                                                       | Kontrakt records the unique root-owned presentation without redundant authoring        |
| Semantically ambiguous data                     | Require explicit Input choice                                   | `Set<T>`, `Map<K, V>`, sealed hierarchy as tagged choice, nullable absence semantics, host default, external scalar semantics, accepted information loss                                           | The chosen meaning becomes visible authored material and part of the resolved contract |
| Implementation-shaped carrier                   | Require adapter projection or an explicit compatibility profile | Mutable JavaBean, inherited DTO, framework DTO, serializer product, proxy, entity, custom getter, delegated property, third-party object, dynamic JSON object, recursive or shared-reference graph | Host conventions are removed before Input meaning is ratified                          |
| Behavior, capability, movement, or role leakage | Reject from Input                                               | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core fact, state, backend handle                                                  | The material belongs to another role or is not contract data                           |

For the JVM V1 zero-adapter source profile, Kotlin data classes, Kotlin final classes whose ratified parts are
primary-constructor `val` properties, and Java 17+ records may satisfy the direct-source conditions. Kotlin part order
is
the declared primary-constructor property order. Java part order is record-component order.

A source-visible sealed hierarchy is not part of the automatic zero-adapter interpretation merely because it is sealed.
It may become a direct carrier after the authored Input contract explicitly selects the closed-choice interpretation and
all variants resolve under the same source profile.

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

If the source cannot be resolved under these laws, or if a required semantic choice is absent, the contract source is
rejected during planning or lowering. If the ratified Input contract exists but invocation-time boundary access cannot
make the declared values available within presence, capacity, budget, and stability conditions, the machine stops with
declared Input failure before Admission begins.

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

### 5.5. Fact Contract

Fact is core factual material.

A fact is not the object, row, message, or value that may carry it in software. Those are carriers or realizations. The
fact is the material the core may stand on.

Kontrakt lowers the fact contract into the law for factual material: how it is identified, what meaning it belongs to,
and why it remains immutable once accepted.

A candidate fact becomes an accepted immutable fact only after the invariant accepts it.

### 5.6. Invariant Contract

Invariant is the acceptance judgment over candidate material.

Lowering says what the candidate claims to be. Invariant decides whether the machine may believe that claim.

A lowered candidate must arrive with a declared meaning. It may claim to stand under a named fact contract, with
identity
material, version meaning, and the core coordinates this pipeline has already named for judgment. Those coordinates are
not object references. They do not invite graph wandering. They only name the accepted core material this judgment is
allowed to consider.

The invariant decides whether the claim is coherent, whether identity collides with accepted material, and whether
accepting the candidate would make the core lie. If no declared meaning fits, or more than one meaning fits, the machine
must stop with declared failure.

Invariant is not a validator drawer. It is the point where a candidate either becomes believable core material or stops.

### 5.7. State Contract

State is the declared condition that governs legal next movement.

It is not a status word discovered after the fact. A label becomes state only when it changes what the machine may
legally do next.

Kontrakt lowers state into a closed, flat movement vocabulary. The vocabulary must be known before it governs movement.

A backend may realize the state surface with compact physical machinery. That machinery is not state authority.

### 5.8. State Transition Contract

Transition is permission to move.

A machine does not earn permission by having already moved. The move must be declared before the machine follows it.

Kontrakt lowers transition into one-way movement law inside a declared state surface. If a move needs a different
meaning, it needs a different transition. Repetition must be governed as repetition, not hidden as a cycle.

A backend may build transition tables or checkers. It may not invent movement.

### 5.9. Explicit State Machine Manifest

The explicit state machine manifest makes the movement surface visible.

State and transition already form a machine. This manifest prevents that machine from being reconstructed later from
implementation residue.

Kontrakt lowers the manifest into the closed map of one state surface: where it begins, where it can stop, and which
one-way moves are allowed.

A backend may analyze or compact that map. It does not become a new authority above state and transition.

### 5.10. Failure Contract

Failure is a declared stop result.

It is not a cleaned-up crash. It is the machine saying which obligation could not be satisfied and what consequence is
allowed after the stop.

Kontrakt lowers failure into material tied to the obligation that produced it. A destroyed execution does not perform a
final judgment after it is gone; only durable obligations before loss and recovery obligations after loss can be
contracted honestly.

A backend may translate platform failure only when a failure law allows that translation.

### 5.11. Publication Contract

Publication is the outward claim.

Accepted core material is not automatically public material. The machine may know more than it is allowed to say.

Kontrakt lowers publication into the law that permits or denies a public claim from accepted material. Diagnostic
material remains internal unless publication allows a public diagnostic claim.

A backend may serialize or emit the claim. Emission is not publication authority.

### 5.12. Diagnostic Evidence Contract

Diagnostic evidence is declared explanation.

It explains a judgment result. It does not produce the judgment, continue rejected material, or become another path
through the machine.

Kontrakt lowers evidence into candidate explanation material tied to the judgment that produced it. Evidence should come
from the judgment point, not from a later observer scraping machinery.

A backend may record or display evidence. It may not turn evidence into authority.

### 5.13. Diagnostic Retention Contract

Retention decides what evidence survives.

A judgment may create explanation during a run. That does not mean the explanation may remain after the run.

Kontrakt lowers retention into the boundary that decides what explanation may survive, what must be reduced, and what
must disappear. If retained material later leaves the machine, publication must judge that separately.

A backend may choose storage. The store is not the retention contract.

### 5.14. Version Coordinate

Version is a coordinate of meaning.

It matters when familiar-looking material may be read under different contract meaning.

Kontrakt lowers version into meaning coordinates attached to the material that needs them. The coordinate tells the
machine which meaning governed the material it is reading.

A backend may use tables or key material. It may not let the newest code silently reinterpret old material.

### 5.15. Policy Contract

Policy declares active judgment criteria.

Configuration may select policy, but it does not become policy. Runtime behavior may realize policy, but it does not
write the policy.

Kontrakt lowers policy into named, governed judgment material. Other judgments can read that material without depending
on an implementation option bag.

A backend may dispatch on policy. Dispatch is not policy meaning.

### 5.16. Budget Contract

Budget is consumable allowance.

It tells the machine how much a governed run may spend before a declared result is required.

Kontrakt lowers budget into allowance material with scope and exhaustion meaning. Exhaustion must be part of the
contract before the machine overruns it.

A backend may implement cheap accounting. The contract says what may be consumed and what happens when it is gone.

### 5.17. Capacity Contract

Capacity is admissible limit.

A finite machine may refuse valid material if accepting it would exceed what the machine can bear. That is not a
validation failure. It is capacity discipline.

Kontrakt lowers capacity into limit material and the declared outcome when the limit is reached. The limit must exist
before damage proves it was needed.

A backend may realize the limit physically. The physical shape is not the contract.

### 5.18. Governance Contract

Governance declares the active contract world.

Without governance, the machine does not know which law book it is reading.

Kontrakt lowers governance into the material that says what contract world is active, where it is active, and under
which coordinate it may be used.

An administration tool may install or replace governed material. The tool is machinery. Governance is the rule that
decides which world is valid.

---

## 6. Cross-Profile Boundaries

### 6.1. Candidate and Accepted Material

Lowering may produce candidate material. It does not produce accepted truth.

A candidate fact is still only a candidate. A candidate transition is still only a candidate move. Invariant and state
movement decide whether the machine may accept or follow those claims.

### 6.2. Presentation Equivalence and Core Coherence

Canonicalization handles declared equivalence in external presentation.

Invariant handles core coherence after lowering. It decides whether the candidate can stand under the named core basis.
These are different judgments.

### 6.3. Evidence and Retention

Evidence and retention stay separate.

Evidence explains a declared result. Retention decides what explanation may survive. Publication decides whether any
surviving explanation may become an outward claim.

### 6.4. Limits and Validity

Policy, budget, capacity, and governance are not late runtime decorations.

They are contract material because a finite machine must know its active rules and limits before it acts.

---

## 7. Unresolved

This ADR does not decide final authoring syntax for the eighteen bodies.

It also does not decide the future judgment language or the final host-facing API. Those decisions must follow the
boundary fixed here: source may carry contract material, judgment must be lowerable, and backend machinery must remain
replaceable.

---

## 8. Consequences

The remaining one-dimensional presentations are no longer only a catalog of names. They now have processing profiles.

Each profile begins with a machine obligation. Software realization comes only after the role has been selected by a
slot and lowered into Kontrakt-owned material.

This keeps ADR-0047's slot-selection boundary intact. It also keeps `What Contract Is` from shrinking into a Kotlin
framework vocabulary.

A user can still write ordinary software-facing material. Kontrakt may still remove boilerplate and build efficient
backend machinery. The authority sits in the lowered contract material, not in the carrier or the generated machinery.

The next ADR may decide authoring syntax or frontend carrier forms for these profiles. It must not change the processing
boundary decided here.