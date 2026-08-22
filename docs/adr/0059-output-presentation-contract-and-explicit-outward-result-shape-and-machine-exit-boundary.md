# ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary

## Status

Accepted

## Date

2026-08-21

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

A Contract Machine needs an explicit boundary on both sides of its Core.

Input Presentation declares the finite form in which outside material may first appear to the machine. That material is
still outside Core authority until the later Contracts that own admission and factual formation complete their work.

The outward side is symmetric with the inbound boundary, but its direction is reversed. An Operation Result Fact is
authoritative Core material. A Failure established under ADR-0057 is also machine material. Neither is an external
result merely because implementation can return, serialize, print, or otherwise expose its runtime representation.

ADR-0058 closes outward exposure authority. Publication decides which material of an authoritative Core exit may receive
outward authority. That decision does not itself declare which authorized material forms the actual outward result for a
successful or failed exit, so outside software would still have to infer that result unless Output Presentation states
it explicitly.

```text
Contract Core
    -> authoritative Core exit material
    -> Publication authorization
    -> explicit Output binding
    -> Output Presentation
    -> outward result established at the machine boundary
    -> outside
```

The distinction is necessary because the machine may know more than it may expose, and Publication may authorize more
material than one outward result actually contains. Material that Publication does not authorize remains unavailable to
Output; preserving the names of authorized coordinates therefore does not expose the rest of the Core.

Serious interface engineering follows the same boundary discipline even when the individual obligations are grouped in
different documents. A component distinguishes local material from output material. A build system knows declared
outputs before execution. An interface specification fixes what must hold at the interface without making the receiving
system's later behavior part of the producing system. Abstract interface types are also routinely separated from the ABI
or encoding that realizes them.

Kontrakt keeps those responsibilities separate as Contracts rather than adopting one large interface description.
Publication owns outward authority, and Output Presentation owns the final outward structure. Kontrakt processing checks
those authorities and establishes the outward result. What happens after the machine boundary belongs to the outside
system unless that system enters another Kontrakt boundary as new Input.

ADR-0046 already gives the minimum Operation explicit Input and Output positions. This ADR gives the Output position its
current Contract meaning after ADR-0057 and ADR-0058, including how existing explicit-absence law applies there.

---

## 2. Problem

Without an Output Presentation Contract, the machine has no authoritative answer to a basic question:

> What exact result has this machine established for the outside world?

Using the whole Operation Result Fact or Failure directly does not answer it. It would expose more Core material than
the outward result actually declares. Output instead selects only Publication-authorized coordinates from the bound
exit; all other Core structure remains unavailable outside the machine.

Letting a serializer, framework, generated DTO, RPC library, or backend decide the result shape is also insufficient.
That makes implementation behavior the authority for the public surface.

Publication cannot absorb the missing responsibility. Publication answers which established material may receive outward
authority. If it also decides the outward result structure, outward permission and outward presentation become one
authority again.

```text
Operation Result Fact
    != outward result

Publication
    != outward result shape

serializer or transport
    != Contract authority
```

The machine therefore needs one final Contract boundary that explicitly declares an outward form from
already-established exit material without changing its meaning. Publication and Output remain separate authorities:
Publication sets the maximum outward-authorized material, while Output selects the actual outward shape for a bound
successful or Failure exit. The compiler requires every Output coordinate to resolve to material allowed by the
applicable Publication.

This boundary must also make absence explicit. Host-language `null`, omitted object members, default values, empty
strings, zeroes, missing serializer fields, and similar conventions do not all mean the same thing. Treating any of them
as implicit absence would make representation conventions part of Contract meaning.

The opposite mistake is to make Output responsible for what happens after the result leaves the machine. Delivery,
storage, interpretation, later transformation, and consumer behavior are not properties of the producing machine's
Output Presentation. Extending Output authority into those activities would erase the boundary the Contract is meant to
create.

---

## 3. Decision Drivers

Output Presentation must remain separate from Publication. Output declares the outward result independently, while the
compiler rejects any Output material that lacks outward authority under the applicable Publication.

The Output Presentation must completely declare the outward result. Machine processing cannot add undeclared outward
material or extend the Contract surface at runtime.

Internal Result or Failure representation must never become the public shape by convenience. Only material explicitly
authorized by Publication may participate in Output, and Output may select only the subset required by its declared
shape.

Absence must be Contract meaning when the outward shape distinguishes it. It cannot be inferred from `null`, a default
value, omission by a serializer, or another backend convention.

An Output Presentation must preserve established exit material. It may select existing Result or Failure coordinates,
but it cannot rename them, change their sorts or values, or establish new factual meaning.

Input and Output are symmetric boundary Contracts. Input closes the form crossing into the machine, while Output closes
the form crossing out. Both require a finite, inspectable presentation so that outside representation cannot become Core
authority and Core representation cannot become outside authority.

The machine's Contract responsibility ends when a conforming outward result is established at the Output boundary. Later
use of that result is outside this Contract Machine.

Host-language declarations are frontend evidence only. Canonical Kontrakt material owns final Output meaning.

Determinism remains mandatory. The same established exit material under the same Publication authority and Output
binding must establish the same outward structural result.

---

## 4. Contract Decision

### 4.1. Output Presentation Is the Final Outward Contract Boundary

Output Presentation is the Contract authority over the structural form of the result established at the machine's
outward boundary.

```text
Publication-authorized exit material
        ↓
explicit Output binding
        ↓
Output Presentation
        ↓
outward result
════════════════════════════════
machine boundary
════════════════════════════════
        ↓
outside
```

The Output boundary is final for this machine. Once a conforming outward result has been established there, later use of
that result does not remain under this Output Presentation Contract.

An outside consumer may itself be another Contract Machine. In that case the same material is outside material again and
must enter the other machine through its own Input Presentation and applicable inbound Contracts. The first machine does
not gain authority over the second machine by producing the value.

### 4.2. Core Result Is Not Outward Result

An Operation Result Fact remains Core material even when it is the successful authoritative exit result. A Failure also
remains the Failure established by its owning source.

Neither becomes an outward result by return type, language visibility, runtime reachability, serializability, or object
identity.

```text
established inside
    -> Core authority

published
    -> outward authority

presented through Output
    -> outward result
```

This separation allows Core structure to evolve without silently changing the external result and prevents outside
software from acquiring accidental authority over internal coordinates.

### 4.3. Successful Output and Failure Output Bindings Are Explicit

Every Operation keeps an explicit successful Output position. That position selects one exact Output Presentation when
the Operation declares a successful outward result. When the existing IDL slot law permits absence, the position uses
that explicit absence material instead.

A Failure that may itself become an outward result also requires an explicit Output binding. Publication authorization
alone does not select a Failure Output Presentation, and a Failure does not inherit the successful Output Presentation.
Several distinct Failure exits may bind to the same Output Presentation when that presentation can be resolved against
each Failure's own authorized material.

```text
successful exit
    -> exact Output Presentation
    -> or explicit IDL absence where the slot law permits it

Failure exit
    -> explicit Output Presentation binding when exposed as an outward result
```

An Output Presentation is never inferred from the Operation return type, Publication selection, Failure source,
serializer shape, `null`, `void`, or backend behavior. The binding states which outward shape applies; the presentation
itself remains a structural declaration rather than a table of Result or Failure sources.

### 4.4. Input and Output Are Symmetric Boundary Contracts

Input Presentation and Output Presentation are symmetric boundary Contracts on opposite sides of the Contract Machine.

```text
outside
    -> Input Presentation
    -> Contract Machine

Contract Machine
    -> Output Presentation
    -> outside
```

Input makes outside material finite and inspectable before the machine can reason from it. Output makes
already-authorized machine material finite and inspectable before the outside can rely on it. The same boundary
discipline is applied in reverse directions.

This symmetry belongs to the presentation boundary, not to the surrounding authorities. Input does not grant Core
factual authority, and Output does not grant outward authority. Those judgments belong to the surrounding Contracts.

### 4.5. Output May Use Only Publication-Authorized Material

Publication and Output Presentation remain independent Contracts over authoritative Core exit material.

Publication decides which established Result and Failure material may receive outward authority. A bound Output
Presentation independently declares the subset that forms the actual outward result for that exit. Publication does not
require Output to expose everything it authorizes, and Output does not expand Publication authority.

```text
Publication-authorized material
    -> maximum material eligible for outward use

Output-selected material
    -> actual outward result material for one bound exit

valid composition
    -> Output-selected material is within the applicable Publication authority
```

Publication authorization is also the reason name preservation does not leak unapproved Core structure. Output can refer
only to coordinates that Publication has already admitted to the outward Contract surface; every other Core coordinate
remains unavailable.

The compiler checks that composition separately for each bound successful or Failure exit. Output cannot use sealed Core
material, reach into diagnostic evidence, combine material from another exit, or obtain another value from a different
Contract merely to fill its outward form.

### 4.6. The Outward Structural Space Is Closed

An Output Presentation completely determines every structural form that it permits.

`Closed` does not mean that every runtime result has the same physical byte length or that every declared coordinate
must always be present. It means that the Contract already contains every permitted structural alternative.

```text
declared outward forms
    -> complete allowed structural space

undeclared outward form
    -> not an Output Presentation result
```

A serializer, framework, adapter, backend, or consumer cannot extend that structural space under the same Output
Contract. A new outward coordinate or new structural alternative is a Contract change unless it was already explicitly
part of the declared form.

Version evolution remains governed by the Version Contract. Openness is not obtained by leaving the current Output shape
incomplete.

### 4.7. Output Owns Structure, Not New Meaning

Output Presentation is a closed projection of the authoritative material of its bound exit. It declares a finite outward
shape whose coordinates must resolve one-to-one to existing Result or Failure coordinates of that exit. Coordinate
membership defines that projection; declaration position does not contribute to Output meaning.

Each selected coordinate preserves the source coordinate's declared name, sort, and factual value. The name is frontend
evidence for resolving the exact existing coordinate; it does not create a second factual identity. Output admits no
alias or rename.

Output does not derive another factual value by calculation, parsing, lookup, defaulting, normalization, or a new
business judgment. If a value is required as part of the outward result, that value must already exist in the
established Result or Failure before Output.

V1 therefore introduces no user-authored mapper, callback, expression language, or source-to-target transformation
Contract. Kontrakt resolves the declared shape against the bound exit and processes only that projection; implementation
may choose its physical representation but cannot create additional meaning.

### 4.8. Presence and Absence Are Explicit Structural Meaning

Presence is part of Output meaning when the outward Contract distinguishes whether a coordinate is present.

That distinction must be explicit Contract material. It is not inferred from a runtime sentinel or from behavior of a
serializer.

The following therefore do not define Output absence by themselves:

```text
null
zero
false
empty text
empty bytes
omitted serializer field
default constructor value
uninitialized property
missing map entry
backend-specific sentinel
```

Any of those may be a meaningful value or an implementation state in another system. Output Presentation does not guess
which interpretation was intended.

V1 does not use host `null` as Output Contract material. If an outward coordinate admits absence, the frontend must
state that fact through explicit Contract vocabulary. The exact Java and Kotlin spelling remains a frontend decision; a
backend may use its own physical representation only when it preserves the declared distinction.

### 4.9. Exit Shapes Are Bound, Not Discovered

Successful Result and Failure exits remain distinct authoritative results. Output does not evaluate business conditions
to decide which exit occurred and does not collapse those exits into one semantic result.

The applicable Output Presentation follows from the explicit binding for the already-established exit. Several distinct
Failures may share one Output Presentation when that same declared shape can be resolved independently against each
Failure's Publication-authorized material. Sharing a presentation does not merge the Failures or create a common Failure
identity.

Output structure must not create `AggregateFailure`, `PrimaryFailure`, or another semantic Failure solely to make the
external form convenient.

### 4.10. Output Absence Uses the Existing IDL Law

The absence of an Output Presentation is governed by the existing IDL slot law rather than by a special empty carrier.

When the Output position declares absence, no outward Result Presentation is required for that Operation. Kontrakt does
not introduce `NoOutput`, `EmptyOutput`, a zero-coordinate sentinel type, or another Output-specific absence taxonomy.

When an Output Presentation is selected, its declared outward result is required. Failure to establish that required
result is not reinterpreted as absence.

### 4.11. Output Does Not Own Delivery or Later Use

The Output Contract ends when its conforming outward result is established at the machine boundary.

A later transport may fail. A database may reject the material. A consumer may ignore a coordinate, misinterpret the
result, store it, transform it, or send it elsewhere. Those events do not rewrite the already-established Output result.

If Kontrakt is separately asked to govern one of those later activities, that activity needs its own applicable Contract
boundary. It is not implicitly inherited from the producing Operation.

This ADR therefore gives no delivery guarantee and no consumer-behavior guarantee.

### 4.12. Failure Does Not Become the Successful Output

A Failure established before the successful Output boundary makes that successful exit unreachable. The Failure does not
become the selected successful Output Presentation, and partially formed successful material does not become a partial
Output result.

If that established Failure has Publication-authorized material and an explicit Failure Output binding, the machine may
instead establish the Failure's own outward result through its bound Output Presentation. That presentation projects
only material of that Failure and does not rewrite, aggregate, or reclassify it.

```text
successful exit
    -> Failure established
    -> successful Output not reached

established Failure
    -> Publication-authorized Failure material
    -> explicit Failure Output binding
    -> conforming Failure outward result
```

If the Failure's bound Output cannot itself be established, the successful Output is still not revived. Applicable
Failure law governs the failed processing. If the running machine is destroyed before it can establish a Failure,
ADR-0057's Crash boundary applies instead.

### 4.13. Re-entry Creates No Shortcut

An outward result that later returns to the same machine is outside material again.

Prior Publication or Output Presentation does not grant it Input, Admission, Fact, State, or other Core authority. It
must cross the inbound boundary under the Contract that applies to the new interaction.

### 4.14. Output Presentation Is Deterministic

For the same established exit material under the same Publication authority, Output binding, Output Presentation, and
Version, the authoritative outward structure is the same.

Source discovery order, object identity, serializer configuration, reflection order, thread timing, backend layout, and
transport choice cannot change Output meaning.

---

## 5. V1 User Authoring API and Canonical Output Material

### 5.1. Frontend Discipline

The V1 frontend should make Output look like what it is: one immutable outward structural declaration.

Publication authoring remains a Core-facing manifest of material eligible for outward use. Output authoring does not
repeat that manifest or carry Result and Failure selectors. It declares reusable outward shapes, while the Operation's
explicit bindings associate already-established exits with those shapes.

The frontend responsibilities are therefore visibly different even when they mention some of the same coordinates. The
following is conceptual structure, not final IDL spelling:

```text
Publication manifest
    Result material
        orderId
        amountMinor
        currency

    Stock Failure material
        meaning
        availableQuantity

Output Presentations
    PlaceOrderOutput
        orderId
        amountMinor

    AvailabilityFailureOutput
        meaning
        availableQuantity

Operation exit bindings
    successful exit
        -> PlaceOrderOutput

    Stock Failure exit
        -> AvailabilityFailureOutput
```

Publication names the maximum material that is eligible for outward use. The Output declarations contain only the actual
closed shapes, and the exit bindings state where those shapes apply. Output does not repeat Publication's Failure-source
selection rules inside the carrier declaration.

Ordinary host classes are preferred when the host language can express the required finite shape directly. The class is
source evidence for the compiler, not final Contract authority and not a required runtime carrier.

Output authoring contains no executable behavior. A constructor declaration provides structural source evidence; its
execution has no Contract meaning.

The V1 source profile does not use inheritance, generic type parameters, annotations, callbacks, lambdas, runtime DSLs,
reflection discovery, or user-defined nested carrier graphs to encode Output meaning.

### 5.2. Required-Coordinate Kotlin Form

A simple one-form Output may be declared as an ordinary Kotlin class whose primary-constructor `val` properties expose
the complete direct outward surface.

```kotlin
class PlaceOrderOutput(
    val orderId: String,
    val amountMinor: Long,
    val currency: String,
)
```

The class name nominates the Output Presentation source declaration. Each direct property name must exactly preserve the
name of the selected Operation Result coordinate. The spelling is source evidence for resolving that existing
coordinate, not a second factual identity. The property's admitted scalar profile must preserve the selected Result
coordinate's sort.

Property declaration order does not contribute to Output Contract meaning. The Output Presentation is a closed set of
named outward coordinates rather than an ordered tuple; reordering the same declared coordinates does not change the
Output Presentation.

The compiler does not obtain Contract meaning from constructor invocation, generated accessors, object identity, JVM
field layout, `equals`, `hashCode`, or `toString` behavior.

A Java frontend may use a different natural host form when it lowers to the same canonical Output material. Java and
Kotlin do not need identical surface spelling.

### 5.3. V1 Flatness Law

V1 Output Presentation is flat for the same reason as V1 Input Presentation: a presentation is one boundary Contract,
not a recursive container for more user-defined Contracts.

The selected Output exposes one finite set of directly named coordinates. A direct coordinate may use an admitted
primitive or approved closed immutable scalar leaf. A user-owned nested product, entity, record, collection graph,
interface hierarchy, recursive node, or shared-reference structure does not become a nested Output Contract.

External systems may use richer structures after the Kontrakt boundary. A replaceable adapter may also encode the flat
Output into another protocol-specific structure outside that boundary. Neither changes the Output Contract.

If a later Kontrakt version admits richer Output structure, that extension must define its Contract authority directly
rather than inheriting structure from host object graphs.

### 5.4. Output Absence Uses the IDL Slot

Output absence is not represented by an empty Kotlin carrier.

The Operation's Output position uses the existing IDL explicit-absence law when no Output Presentation applies. This ADR
does not introduce an Output-specific `NoOutput`, `EmptyOutput`, nullable return convention, or marker class.

An omitted source declaration is not permission for the compiler or backend to infer outward-result absence.

### 5.5. Explicit Absence Source Law

A source declaration that permits coordinate absence must express that distinction through explicit Kontrakt frontend
vocabulary.

Kotlin nullable types, Java nullable references, constructor defaults, serializer annotations, and omitted members do
not provide Output absence authority. The exact Java and Kotlin spelling for an absence-capable coordinate remains open
until the frontend can express it without hidden defaults, host `null`, or recursive carrier composition.

This coordinate-level distinction is separate from explicit absence of the entire Output position in the IDL.

### 5.6. Output Has No User Mapping Layer

V1 introduces no user-authored published-to-outward mapping layer.

An Output Presentation declares only its outward shape. The successful Operation Result or an established Failure
remains the authoritative source material, and the Operation's explicit Output binding states which presentation applies
to that exit. The presentation does not contain Result or Failure selectors and does not create a second target-value
domain that must be populated through mapper code.

Output does not introduce separate outward aliases. For each binding, every declared property must resolve to an
existing coordinate of that exact exit with the same declared name and compatible sort. Equal spelling is resolution
evidence only; canonical Result or Failure identity remains the factual authority.

The compiler resolves each binding as a deterministic one-to-one projection and checks that every selected coordinate is
allowed by the applicable Publication. A declaration that would require renaming, a new value, cross-exit material,
ambiguous runtime lookup, or transformation is invalid rather than delegated to a user mapper.

### 5.7. Failure Output Uses the Same Shape Discipline

A Failure Output Presentation is authored with the same flat immutable shape discipline as a successful Output
Presentation. It does not embed the Failure source selector that causes it to apply.

For example, when the bound Failure already contains Publication-authorized coordinates named `meaning` and
`availableQuantity`, an outward Failure shape may be declared as:

```kotlin
class AvailabilityFailureOutput(
    val meaning: String,
    val availableQuantity: Long,
)
```

The same `AvailabilityFailureOutput` may be bound to several distinct Failures when each one independently provides
those same named coordinates with compatible sorts and Publication authorizes them. A Failure that needs a different
outward shape binds to another Output Presentation. Failure count therefore does not imply an equal number of Output
Presentation classes.

The exact IDL spelling used to state those Failure-to-Output bindings remains frontend work. The semantic requirement is
not deferred: the binding is explicit, belongs outside the Output carrier, and never follows merely from Publication or
Policy.

### 5.8. Canonical Output Presentation and Binding Material

Canonical Output Presentation material preserves only what is required to reproduce the declared outward shape:

```text
Output Presentation identity
Output Version
closed set of outward coordinate names
outward value sorts and approved scalar profiles
presence law for each coordinate
outward structural bounds required by the selected scalar profiles
```

Source-specific projection belongs to canonical Output binding material rather than to the reusable presentation shape:

```text
Operation and exit identity
bound Output Presentation identity
resolved source coordinate identities corresponding one-to-one to the Output coordinates
```

Host class names may remain source attribution material where needed by tooling, but JVM construction rules, object
identity, nullable-reference representation, serializer configuration, framework annotations, user mapper code, and
physical layout do not survive as Output authority.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Processing

Output Presentation owns the outward structural requirement. Kontrakt resolves each explicit Output binding against the
authoritative material of its established successful or Failure exit, checks the applicable Publication authority, and
establishes only a conforming outward result. These judgments belong to Contract-machine processing rather than to user
implementation.

The physical form used while carrying out that processing is not Contract authority. A backend may allocate a host
object, write a primitive layout, materialize a generated carrier, or erase an intermediate carrier entirely without
changing the canonical outward result.

### 6.2. Backend Representation

A backend receives resolved Contract material and implements the physical representation needed by Kontrakt processing.
It does not decide which Result material is selected, whether sealed material may leave, or what outward shape is valid.

Publication checking and Output materialization may be fused into one optimized physical path, but that optimization
must preserve the logical wall around sealed Core material. No fused path may use a sealed coordinate merely because it
is physically convenient.

Physical sentinels, presence bits, tags, offsets, or language-specific references remain implementation details. A
backend may represent a declared absence state with a null reference only when that choice is private to the backend and
preserves every Contract-visible distinction.

### 6.3. Encoding and Transport Are Outside Output Authority

Output Presentation does not choose a wire encoding or transport.

JSON, a binary protocol, an RPC framework, a file format, a database row, a network frame, or another carrier may be
used afterward. Their rules do not become Output Contract meaning merely because they carry the result.

A separate system may impose an encoding or transport contract of its own. This ADR does not deny that such obligations
can be Contracts; it only keeps them out of Output Presentation authority.

### 6.4. External Adapters Begin After the Output Boundary

An external adapter receives only a conforming outward result. It must not reach back into Publication material or Core
material to enrich that result.

If the adapter adds protocol metadata, wraps the result, changes names, drops fields, invents defaults, or otherwise
changes the shape for its own environment, that new representation belongs to the outside system. It is not another
Kontrakt Output result unless a separate Contract boundary explicitly governs it.

### 6.5. Backend Architecture Constraints

This ADR does not choose one runtime Output IR or carrier representation.

Any backend must preserve these dependency laws:

```text
Output Presentation -> sealed Core material       forbidden
Output processing   -> undeclared outward material forbidden
external adapter    -> Output Contract authority   forbidden
```

Compiler caches may reuse resolved Output material but cannot create or alter authority. Incremental and clean builds
must lower the same source Contract to the same canonical Output meaning.

---

## 7. Verification, Determinism, and Incremental Extensibility

The verifier must require the successful Output position to resolve explicitly according to the IDL slot law: to one
exact Output Presentation when selected, or to explicit absence where that position permits absence. Every Failure that
is intended to establish its own outward result must likewise have an explicit Output binding.

Each Output declaration must define one finite closed outward structural space. Runtime discovery, inherited membership,
recursive host traversal, open extension fields, and undeclared alternatives are invalid.

For every binding, each Output-selected value must resolve one-to-one by preserved name and compatible sort to existing
material of that exact established Result or Failure. That material must be within the outward authority granted by the
applicable Publication. Publication may authorize material that a particular Output does not select; this does not make
Output another Publication judgment.

No sealed Core material may appear through Output, and no Output declaration may require creation of a new factual
value.

An absence-capable coordinate must have its allowed presence cases explicitly declared. Host `null`, constructor
defaults, serializer omission, and backend sentinels do not satisfy that declaration.

Whole-Output absence follows the existing IDL slot law. It is not represented by an empty carrier or inferred from a
host return convention.

Exit selection follows already-established authoritative material and explicit Output binding. An Output declaration
cannot contain a predicate, callback, lookup, Failure selector, or arbitrary expression that decides which exit or
presentation applies.

The compiler must reject Output calculations, constants used as undeclared domain material, value-producing
transformations, and any declaration that requires runtime name matching, reflection discovery, or user-authored mapper
behavior to determine the outward result.

Renaming an Output coordinate is invalid because Output must preserve the selected Result or Failure coordinate name.
Changing declaration order alone does not change the Output Contract. Changing allowed presence, value sort, scalar
bound, or structural membership does change the Output Contract and is subject to Version rules.

Build strategy does not alter the result. Clean compilation, incremental compilation, cache reuse, and parallel
execution must agree on canonical Output material and on verifier decisions.

Incremental invalidation follows exact dependencies. A Publication change invalidates the compatibility result for any
Output binding that selects affected material. A binding change invalidates its resolved projection without changing the
reusable Output Presentation when that presentation's shape is unchanged. A pure backend representation change does not
invalidate the Output Contract when canonical meaning is unchanged.

---

## 8. Deferred Decisions

The exact Kotlin and Java source spelling for absence-capable coordinates remains open. The semantic distinction is not
open: coordinate absence must be explicit Contract material rather than host `null` or hidden omission.

V1 introduces no user-authored source-to-target mapping language. Output preserves selected Result and Failure
coordinate names rather than creating aliases or a second naming domain.

The exact frontend spelling for binding many Failure exits to Output Presentations remains open. The semantics are
fixed:
Publication authorization does not imply an Output binding, the binding is explicit, several distinct Failures may reuse
one compatible Output Presentation, and that reuse does not merge their Failure identities.

V1 remains flat. Rich nested outward presentation is not admitted by this ADR. If a later version adds a controlled
nested structural vocabulary, it must do so without recursive user-defined Contract composition or hidden host object
graph authority.

The exact package names of Kontrakt-owned Output frontend vocabulary are implementation-facing API work and do not
become canonical Contract identity.

Diagnostic Evidence / Retention remains the next Contract decision. Diagnostic material does not become Output material
unless Publication first grants outward authority under ADR-0058.

ADR-0046, ADR-0048, and ADR-0049 contain older Output assumptions that require later reconciliation where they conflict
with this ADR, including a universally required concrete Output Presentation, nullable Input/Output presentation
conventions, direct Publication-to-Output mapping, `data class` examples, or other superseded source rules.

These deferred decisions do not reopen the explicit Output-position law, the closed structural-space law, explicit
absence, Publication separation, or the rule that Output authority ends at the machine boundary.

---

## 9. Consequences

### Positive

The Contract Machine has one explicit final outward boundary. Core Result and Failure representations cannot become
external APIs by convenience.

Publication and Output retain separate authorities: Publication defines the maximum Result and Failure material eligible
for outward use, while Output fixes the actual shape selected from that authorized material for each bound exit.

Absence is no longer dependent on `null`, defaults, serializer behavior, or sentinel conventions. Whole-Output absence
continues to use the existing IDL slot law, while coordinate absence remains explicit presentation meaning when used.

The same canonical Output may be realized by different backends without changing the Contract. External protocols and
frameworks remain replaceable because they begin after the Output boundary.

Input and Output remain symmetric presentation positions around the machine, while the surrounding Contracts on each
side retain their own authorities.

### Negative

Explicit outward structure requires more source information than returning an arbitrary host object or letting a
serializer discover fields.

Publication and Output are independently authored, so compiler verification must check each success or Failure Output
binding against the applicable outward authority before the machine can establish that outward result. Failure-rich
Operations also require explicit binding information, although compatible Failures may share one Output Presentation.

Explicit absence requires dedicated frontend vocabulary rather than familiar nullable host types. V1 deliberately
rejects some convenient host-language forms until that vocabulary is ratified.

Flat V1 Output may require adapters when an outside protocol expects nested or open-ended structures.

### Neutral

Output Presentation does not promise that an outside consumer will receive, understand, store, or correctly use the
result. Those are different obligations beyond this machine boundary.

A backend may physically fuse Publication checking and Output materialization, erase carriers, or use backend-specific
presence representations. Such optimization does not merge their Contract authorities.

Output remains versioned Contract material. External evolution is expressed through explicit Contract change rather than
through undeclared fields under an open shape.