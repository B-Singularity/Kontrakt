# ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary

## Status

Proposed

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

The outward side has the opposite direction but the same need for a boundary. An Operation Result Fact is authoritative
Core material. A Failure established under ADR-0057 is also machine material. Neither is an external result merely
because implementation can return, serialize, print, or otherwise expose its runtime representation.

ADR-0058 closes the first part of the outward path. Publication receives the authoritative Core exit result and decides
which of its material receives outward authority. The result of Publication is still semantic material, not the final
form on which outside software may rely.

```text
Contract Core
    -> authoritative Core exit result
    -> Publication
    -> closed published material
    -> Output Presentation
    -> outward result established at the machine boundary
    -> outside
```

The distinction is necessary because the machine may know more than it may expose, and the shape of internal truth does
not have to be the shape of an external result.

Serious interface engineering follows the same boundary discipline even when the individual obligations are grouped in
different documents. A component distinguishes local material from output material. A build system knows declared
outputs before execution. An interface specification fixes what must hold at the interface without making the receiving
system's later behavior part of the producing system. Abstract interface types are also routinely separated from the ABI
or encoding that realizes them.

Kontrakt keeps those responsibilities separate as Contracts rather than adopting one large interface description.
Publication owns outward authority. Output Presentation owns the final outward structure. Backend machinery realizes
that structure. What happens after the machine boundary belongs to the outside system unless that system enters another
Kontrakt boundary as new Input.

ADR-0046 already requires one Input and one Output binding for the minimum Operation. This ADR gives that required
Output slot its current Contract meaning after ADR-0057 and ADR-0058.

---

## 2. Problem

Without an Output Presentation Contract, the machine has no authoritative answer to a basic question:

> What exact result has this machine established for the outside world?

Using the Operation Result Fact directly does not answer it. It turns a Core representation into a public dependency.
Internal coordinate names, internal structure, or later Core changes then become accidental external Contract meaning.

Letting a serializer, framework, generated DTO, RPC library, or backend decide the result shape is also insufficient.
That makes implementation behavior the authority for the public surface.

Publication cannot absorb the missing responsibility. Publication answers which established material may receive outward
authority. If it also decides external names and structure, outward permission and outward presentation become one
authority again.

```text
Operation Result Fact
    != outward result

Publication
    != outward result shape

serializer or transport
    != Contract authority
```

The machine therefore needs one final Contract boundary that turns already-published material into one completely
declared outward form without changing its meaning.

This boundary must also make absence explicit. Host-language `null`, omitted object members, default values, empty
strings, zeroes, missing serializer fields, and similar conventions do not all mean the same thing. Treating any of them
as implicit absence would make representation conventions part of Contract meaning.

The opposite mistake is to make Output responsible for what happens after the result leaves the machine. Delivery,
storage, interpretation, later transformation, and consumer behavior are not properties of the producing machine's
Output Presentation. Extending Output authority into those activities would erase the boundary the Contract is meant to
create.

---

## 3. Decision Drivers

Output Presentation must remain separate from Publication. Only material already granted outward authority may enter it.

The outward result must be completely declared before realization begins. Runtime discovery cannot extend the Contract
surface.

Internal Result or Failure representation must never become the public shape by convenience.

Absence must be Contract meaning when the outward shape distinguishes it. It cannot be inferred from `null`, a default
value, omission by a serializer, or another backend convention.

An Output Presentation must preserve the meaning of published material. It may give that material an outward name and
position, but it cannot calculate new domain meaning.

Input and Output are boundary mirrors. Their direction differs, while both require a finite, inspectable presentation
that prevents outside representation from becoming Core authority or Core representation from becoming outside
authority.

The machine's Contract responsibility ends when a conforming outward result is established at the Output boundary. Later
use of that result is outside this Contract Machine.

Host-language declarations are frontend evidence only. Canonical Kontrakt material owns final Output meaning.

Determinism remains mandatory. The same published material under the same bound Output Presentation must establish the
same outward structural result.

---

## 4. Contract Decision

### 4.1. Output Presentation Is the Final Outward Contract Boundary

Output Presentation is the Contract authority over the structural form of the result established at the machine's
outward boundary.

```text
closed published material
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

### 4.3. Output Presentation Is Required

Every Kontrakt Operation must bind one Output Presentation Contract.

Absence of meaningful payload does not remove that obligation. An Operation whose outward result carries no coordinates
uses an explicit empty Output Presentation.

```text
output with material
    -> explicit non-empty Output Presentation

output with no material
    -> explicit empty Output Presentation

missing Output Presentation
    -> invalid Operation definition
```

An empty Output is not `null`, missing configuration, an omitted slot, or an implementation `void`. It is a declared
zero-coordinate outward form.

ADR-0046's required Output slot therefore remains part of the minimum Operation rather than becoming optional after
Publication was separated in ADR-0058.

### 4.4. Input and Output Are Boundary Mirrors

Input Presentation and Output Presentation solve the same structural problem in opposite directions.

```text
outside
    -> Input Presentation
    -> Contract Machine

Contract Machine
    -> Output Presentation
    -> outside
```

Input makes outside material finite and inspectable before the machine can reason from it. Output makes
already-authorized machine material finite and inspectable before the outside can rely on it.

The symmetry does not merge their authorities. Input does not grant Core factual authority, and Output does not grant
outward authority. Those judgments belong to the surrounding Contracts.

### 4.5. Output Receives Only Published Material

Output Presentation consumes only the closed material surface produced by the applicable Publication Contract.

It cannot inspect arbitrary Core material, recover sealed coordinates, query diagnostic evidence, or reach another
Contract to fill a missing outward coordinate.

A non-empty outward coordinate therefore requires published source material. If no Publication material reaches the
Output boundary, the only valid outward form is one that requires no published value.

Output cannot silently omit published material merely to narrow disclosure. That would repeat Publication selection. The
resolved Output relation must account for the complete published surface applicable to the outward alternative.

### 4.6. The Outward Structural Space Is Closed

An Output Presentation completely determines every structural form that it permits.

`Closed` does not mean that every runtime result has the same physical byte length or that every declared coordinate
must always be present. It means that every permitted structural alternative is known from the Contract before
realization.

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

Output Presentation may give published material an outward coordinate identity and place it in the declared outward
form. That act changes presentation, not the established source meaning.

Every outward value-bearing coordinate must resolve to exact published material. Same spelling does not create a hidden
binding, and a different spelling does not create a new domain value.

Output does not derive another value by calculation, parsing, lookup, defaulting, normalization, or runtime choice. If a
new domain fact is required, the Contract authority that owns that fact must establish it before Publication.

V1 keeps this boundary deliberately narrow: one published value is presented as one outward value. Duplication,
aggregation, or value-producing transformation is not Output authority.

### 4.8. Presence and Absence Are Explicit Structural Meaning

Presence is part of Output meaning when the outward Contract distinguishes whether a coordinate exists.

Canonical Output material therefore preserves the distinction between:

```text
present(value)
none
```

`none` is Kontrakt Contract vocabulary for explicit absence. It is not a host-language value and is not recovered from a
runtime sentinel.

A required coordinate permits only `present(value)`. An absence-capable coordinate must declare every allowed presence
case as part of the closed Output shape.

The following do not establish `none`:

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

Any of those may be meaningful values or implementation states in another system. Output Presentation does not guess
which interpretation was intended.

V1 does not use host `null` as Output Contract material. If the outward domain needs a distinct state such as unknown,
not applicable, unavailable, or no value, that distinction must be declared explicitly through the Contract vocabulary
that owns it. A physical backend may use a null reference or another sentinel internally when its encoding is proven to
preserve the canonical distinction, but that representation never becomes Output authority.

This law avoids collapsing `none`, a present default value, and a present domain value into the same outward meaning.

### 4.9. Finite Alternatives Are Declared, Not Discovered

An Output Presentation may admit a finite set of outward alternatives when the published exit material itself has
corresponding explicit alternatives.

Output does not evaluate business conditions to choose among them. The applicable alternative must already follow from
the authoritative exit material and Publication result.

Successful Result material and published Failure material may therefore occupy different declared outward alternatives
without turning Failure into a new taxonomy. Several independently published Failures remain distinct even when one
outward result structurally carries material from more than one of them.

Output structure must not create `AggregateFailure`, `PrimaryFailure`, or another semantic Failure solely to make the
external form convenient.

### 4.10. Empty Output Is a Real Output Form

A zero-coordinate Output Presentation declares that the machine establishes an outward result with no value-bearing
material.

That form is useful when the existence of successful completion is enough and no published coordinate is required by the
outside Contract.

```text
Output Presentation
    coordinates: none
```

This is distinct from a coordinate whose presence state is `none`. The first declares an empty outward form. The second
is an explicit absence case inside a non-empty presentation surface.

Backend languages may realize an empty Output as `void`, `Unit`, an empty record, a zero-sized value, or no returned
payload. None of those representations defines the Contract.

### 4.11. Output Does Not Own Delivery or Later Use

The Output Contract ends when its conforming outward result is established at the machine boundary.

A later transport may fail. A database may reject the material. A consumer may ignore a coordinate, misinterpret the
result, store it, transform it, or send it elsewhere. Those events do not rewrite the already-established Output result.

If Kontrakt is separately asked to govern one of those later activities, that activity needs its own applicable Contract
boundary. It is not implicitly inherited from the producing Operation.

This ADR therefore gives no delivery guarantee and no consumer-behavior guarantee.

### 4.12. Output Realization Failure Remains Inside the Boundary

A required Output realization that cannot establish the declared outward result has failed before the machine boundary
has completed.

When enough machine authority remains to establish that fact, ADR-0057 governs it as Realization Failure. A partially
formed carrier does not become a partial Output result merely because some implementation work occurred.

```text
Output requirement established
        ↓
required realization entered
        ├─ conforming outward result established
        │      -> Output boundary complete
        │
        └─ required realization not completed
               -> no Output result
               -> Realization Failure when ADR-0057 authority remains
```

If the running machine is destroyed before it can establish the failure, ADR-0057's Crash boundary applies instead.

### 4.13. Re-entry Creates No Shortcut

An outward result that later returns to the same machine is outside material again.

Prior Publication or Output Presentation does not grant it Input, Admission, Fact, State, or other Core authority. It
must cross the inbound boundary under the Contract that applies to the new interaction.

### 4.14. Output Presentation Is Deterministic

For the same closed published material under the same bound Output Presentation and Version, the authoritative outward
structure is the same.

Source discovery order, object identity, serializer configuration, reflection order, thread timing, backend layout, and
transport choice cannot change Output meaning.

---

## 5. V1 User Authoring API and Canonical Output Material

### 5.1. Frontend Discipline

The V1 frontend should make Output look like what it is: one immutable outward structural declaration.

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

The class name nominates the Output Presentation source declaration. The direct property names nominate outward
coordinates. Their exact admitted scalar profiles provide source evidence for the outward value sorts.

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

### 5.4. Empty Kotlin Form

A zero-coordinate Output remains an explicit declaration.

```kotlin
class AcknowledgeOutput
```

This class declares no outward value coordinate. Its existence satisfies the required Output slot for an Operation whose
outward Contract contains no value-bearing material.

The runtime does not have to instantiate this class. A backend may erase the carrier completely while retaining the
canonical empty Output Contract.

### 5.5. Explicit Absence Source Law

A source declaration that allows absence must express that distinction through dedicated Kontrakt frontend vocabulary.
Kotlin nullable types, Java nullable references, constructor defaults, serializer annotations, and omitted members do
not provide Output absence authority.

The canonical meaning is fixed here as `present(value)` versus `none`. The final Java and Kotlin token spelling for an
absence-capable coordinate is deferred until the frontend can express that distinction without generic type tricks,
`null`, hidden defaults, or recursive carrier composition.

Until that source form is ratified, the zero-adapter V1 Kotlin form in Section 5.2 admits required direct coordinates
only. A user who needs absence cannot obtain it by weakening a coordinate to a nullable host type.

### 5.6. Published-to-Outward Binding

The compiler must resolve how each outward value coordinate receives its exact published source material.

That binding is Contract evidence because Output may rename a published coordinate while preserving the underlying
value. It cannot be inferred solely from matching text, type equality, constructor position, or backend assignment.

The same requirement applies in the other direction: every published value applicable to one Output alternative must be
accounted for by that alternative. Otherwise Output would silently repeat Publication selection.

The final Kotlin and Java spelling for an explicit renamed binding is deferred. A same-name source form may be offered
as a shorthand only if the compiler can prove one exact unambiguous published source. Ambiguity is definition-time
rejection, not runtime choice.

### 5.7. Canonical Output Material

Canonical Output Presentation material preserves only what is required to reproduce the Contract boundary:

```text
Output Presentation identity
Output Version
finite outward alternatives
exact outward coordinate identities
outward value sorts and approved scalar profiles
presence law for each coordinate
exact published source binding for each value-bearing coordinate
outward structural bounds required by the selected scalar profiles
```

Canonical absence is represented explicitly as `none`. It is not encoded as a missing canonical field.

Host class names may remain source attribution material where needed by tooling, but JVM construction rules, object
identity, nullable-reference representation, serializer configuration, framework annotations, and physical layout do not
survive as Output authority.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning

Output Presentation owns the outward structural requirement. It does not own the physical procedure that realizes the
requirement.

A backend may allocate a host object, write a primitive layout, materialize a generated carrier, or erase an
intermediate carrier entirely. Those choices are valid only when the same canonical outward result is established.

### 6.2. Representation Realization

Representation realization begins from already-resolved Publication and Output material.

It may fuse Publication access and Output construction into one optimized physical path, but the backend must preserve
the logical wall between sealed Core material and published material. No fused path may read a sealed coordinate merely
because it is physically convenient.

The realization may use physical sentinels, presence bits, tags, offsets, or language-specific references. Such details
remain implementation. In particular, a backend may represent canonical `none` with a null reference only when that
choice is private to the backend and preserves every Contract-visible distinction.

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
Output Presentation -> sealed Core material        forbidden
Output realization  -> undeclared outward material forbidden
external adapter     -> Output Contract authority   forbidden
```

Compiler caches may reuse resolved Output material but cannot create or alter authority. Incremental and clean builds
must lower the same source Contract to the same canonical Output meaning.

---

## 7. Verification, Determinism, and Incremental Extensibility

The verifier must require exactly one Output Presentation binding for every Operation.

The selected Output declaration must resolve to one finite closed outward structural space. Runtime discovery, inherited
membership, recursive host traversal, open extension fields, and undeclared alternatives are invalid.

Every outward value-bearing coordinate must have one exact published source binding. The source must belong to the
closed published material applicable to that Output alternative. No sealed Core coordinate may resolve through Output.

The verifier must reject a published value that disappears from the applicable Output alternative without an explicit
Contract reason already established before Output. Output cannot act as another publication filter.

A required coordinate cannot resolve to canonical `none`. An absence-capable coordinate must have its allowed presence
cases explicitly declared. Host `null`, constructor defaults, serializer omission, and backend sentinels do not satisfy
that declaration.

An empty Output declaration contains zero value-bearing coordinates and is valid only as that explicit form. Missing
Output binding remains invalid.

Alternative selection must follow already-established exit material. An Output declaration cannot contain a predicate,
callback, lookup, or arbitrary expression that decides which alternative applies.

The compiler must reject output calculations, constants used as undeclared domain material, source duplication,
ambiguous source binding, and any relation that requires runtime name matching or reflection discovery.

Renaming an outward coordinate changes Output Presentation material even when its published source does not change.
Changing allowed presence, value sort, scalar bound, finite alternatives, or structural membership is also an Output
Contract change and is subject to Version rules.

Build strategy does not alter the result. Clean compilation, incremental compilation, cache reuse, and parallel
execution must agree on canonical Output material and on verifier decisions.

Incremental invalidation follows exact dependencies. A Publication surface change invalidates Output material that binds
to the changed published material. A pure backend representation change does not invalidate the Output Contract when
canonical meaning is unchanged.

---

## 8. Deferred Decisions

The exact Kotlin and Java source spelling for absence-capable coordinates remains open. The semantic distinction is not
open: Output absence is explicit `none`, not host `null` or hidden omission.

The exact frontend spelling for renamed published-to-outward coordinate binding remains open. The semantic requirement
is not open: the binding must be exact, deterministic, and independent of runtime name matching.

The final source form for several outward alternatives, including shapes that may carry several independently published
Failures, remains open. Those alternatives must preserve Failure independence and cannot create a new Failure taxonomy.

V1 remains flat. Rich nested outward presentation is not admitted by this ADR. If a later version adds a controlled
nested structural vocabulary, it must do so without recursive user-defined Contract composition or hidden host object
graph authority.

The exact package names of Kontrakt-owned Output frontend vocabulary are implementation-facing API work and do not
become canonical Contract identity.

Diagnostic Evidence / Retention remains the next Contract decision. Diagnostic material does not become Output material
unless Publication first grants outward authority under ADR-0058.

ADR-0048 and ADR-0049 contain older Output and Publication assumptions. They require later reconciliation where they use
nullable Input/Output presentation conventions, direct Publication-to-Output mapping, `data class` examples, or other
source rules that conflict with the authority boundaries ratified by ADR-0058 and this ADR.

These deferred decisions do not reopen mandatory Output, the closed structural-space law, explicit absence, Publication
separation, or the rule that Output authority ends at the machine boundary.

---

## 9. Consequences

### Positive

The Contract Machine has one explicit final outward boundary. Core Result and Failure representations cannot become
external APIs by convenience.

Publication and Output retain separate authorities: one decides what may leave, while the other fixes the form in which
that already-authorized material becomes an outward result.

Absence is no longer dependent on `null`, defaults, serializer behavior, or sentinel conventions. Empty output and
coordinate absence also remain distinct Contract meanings.

The same canonical Output may be realized by different backends without changing the Contract. External protocols and
frameworks remain replaceable because they begin after the Output boundary.

The required Input and Output slots again form a clear pair around the machine without pretending that their surrounding
processing is symmetric.

### Negative

Explicit outward structure requires more source information than returning an arbitrary host object or letting a
serializer discover fields.

A non-empty Output depends on resolved Publication material, so compiler verification must close Publication and Output
before executable machine publication.

Explicit absence requires dedicated frontend vocabulary rather than familiar nullable host types. V1 deliberately
rejects some convenient host-language forms until that vocabulary is ratified.

Flat V1 Output may require adapters when an outside protocol expects nested or open-ended structures.

### Neutral

Output Presentation does not promise that an outside consumer will receive, understand, store, or correctly use the
result. Those are different obligations beyond this machine boundary.

A backend may physically fuse Publication and Output realization, erase carriers, or use backend-specific presence
representations. Such optimization does not merge their Contract authorities.

Output remains versioned Contract material. External evolution is expressed through explicit Contract change rather than
through undeclared fields under an open shape.