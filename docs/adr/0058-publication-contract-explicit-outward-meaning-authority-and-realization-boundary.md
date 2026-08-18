# ADR-0058: Publication Contract, Explicit Outward Meaning Authority, and Realization Boundary

## Status

Proposed

## Date

2026-08-17

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

A Contract Machine must control not only what meaning becomes authoritative inside its core, but also what meaning may
leave that core with Contract authority.

The inbound side already follows this law. External material does not become Contract meaning merely because an adapter
or another outside mechanism supplied it. It must cross an explicit Input boundary and become authoritative only through
the Contract processing that owns that meaning.

The outward side requires the same discipline.

```text
outside material
    -> adapter / realization
    -> Input Contract
    -> Contract Machine

Contract Machine
    -> Publication
    -> Output Presentation
    -> adapter / realization
    -> outside world
```

The adapter is not Contract Authority on either side. An inbound adapter cannot decide what external material means to
the core, and an outbound adapter cannot decide what the core is authorized to make authoritative outside itself.

Mature engineering practice separates an established internal condition from the later decision that authorizes an
outward disposition. Evidence that an internal determination exists does not by itself create authority to release an
external result. Authorization is also distinct from the physical act that follows it. Kontrakt requires the same
separation without importing any particular industry's release procedure into the Contract.

ADR-0049 introduced Publication as the outward-claim authority and correctly separated it from Output Presentation. It
also made internal Fact material non-public by default. A successfully completed Operation establishes its declared
result as an immutable Fact inside the Core, and that Fact remains a natural source of successful outward meaning.
ADR-0049 nevertheless modeled Publication too narrowly around that one source and exact coordinate transfer into one
selected Output Presentation.

Later Contract work widened the machine model beyond successful Operation result Facts, especially through the
State-Machine axis and Failure semantics. ADR-0057 also established that an internal Failure may remain internal or may
support a different outward failure meaning appropriate to an external consumer.

```text
internal Failure
    -> remains internal

or

internal Failure
    -> Publication
    -> outward meaning
```

Publication therefore cannot remain a special path for copying coordinates from one return Fact. It is the Contract
authority over outward Contract meaning at the boundary between the Core and Output Presentation.

That authority begins only from explicitly declared Contract material already established inside the machine. The Core
is sealed from outward visibility by default, while Publication may interpret immutable declared basis material to
establish a reduced, abstract, or selectively disclosed outward meaning. It does not repeat or revise the Core judgment
that made the source authoritative.

External effects remain outside that question. Any physical effect on an external system belongs to realization outside
the core. Such machinery may carry an already-authorized outward meaning, but it cannot create or revise Publication
authority.

ADR-0058 refines Publication around that boundary.

---

## 2. Problem

Without a separate Publication Contract, internal meaning can become outward meaning merely because implementation makes
it easy to expose.

A host-language return value may be serializable, an adapter may be able to send it, and an external system may be ready
to receive it. None of those implementation facts answers whether the Contract Machine is authorized to establish an
outward meaning.

If implementation reachability determines publication, the Contract boundary collapses into backend shape.

```text
implementation can expose X
    -> X becomes public
```

must instead be:

```text
Core establishes authoritative Contract material
        ↓
Publication establishes outward meaning
        ↓
Output Presentation defines the closed outward shape
        ↓
replaceable realization carries that shape outside
```

The earlier Fact-only model is also too narrow. Successful Operation results are immutable Facts inside the Core, but
Failure and other established Contract meaning may also need an outward interpretation. At the same time, the Core may
hold more detail than an external consumer is authorized or required to receive.

Publication therefore needs enough authority to interpret explicitly declared Core material without becoming a second
Core judgment system. If a result Fact contains detailed coordinates, Publication may use the exact coordinates named by
its Contract to form a reduced or abstract outward meaning. Those coordinates remain non-public unless the Publication
Contract explicitly selects them into the outward meaning. It must not search the Core for undeclared material, change
the Fact, rewrite Failure, or establish new internal truth.

The outward relation must also be coherent before execution. If the authored Publication Contract can require
incompatible outward meaning under the same permitted basis, runtime Publication must not invent priority, first-match,
or conflict-resolution state. The invalidity belongs to the Contract definition and must be rejected by compilation or
verification.

The same separation matters at the physical boundary. An external acknowledgement or side effect cannot make an
unauthorized meaning valid after the fact, while failure of later transmission cannot retroactively turn a successful
Publication judgment into a different judgment.

Kontrakt must therefore make the exact outward dependency, outward interpretation, and boundary explicit while leaving
physical realization replaceable.

---

## 3. Decision Drivers

Publication must remain Contract meaning rather than implementation behavior. External implementation can carry
material, but it cannot determine outward Contract authority.

The Core must remain sealed by default so implementation reachability or internal authority cannot silently become
outward authority.

Every Publication judgment must declare the exact Contract material on which it depends, and any Core material that is
to cross the boundary must be positively selected into the resulting outward meaning.

Publication must be able to use declared coordinates of immutable Core Facts and other explicitly declared established
Contract meaning, including Failure, without changing any of them.

Publication may establish outward meaning at a different level of detail from its basis. Information reduction,
abstraction, and selective disclosure belong to Publication when they change what the machine means outwardly rather
than merely how that meaning is represented.

Outward authority is exact rather than global. The same Core meaning may support one outward meaning at one boundary and
no outward meaning at another.

Governance selects and binds the Policy World, while Policy defines the Contract composition of that World. Publication
performs its outward judgment inside the already-bound World; it does not choose the World or replace its binding.

Publication and Output Presentation remain separate. Publication owns outward meaning; Output Presentation owns the
closed outward shape. Physical interaction with the outside world belongs to realization.

Malformed or contradictory Publication declarations are Contract-definition errors. Runtime order, arbitrary callback
behavior, or external consumer choice cannot repair a Contract whose outward law is incoherent.

An unsuccessful required Publication judgment is ordinary Failure under ADR-0057, so Publication needs no parallel
result family for that case.

Determinism remains mandatory. Equivalent authoritative basis material must establish the same Publication meaning
across valid compiler execution modes.

The semantic model must remain independent of the concrete frontend and backend so that future canonical forms can
represent Publication without inheriting JVM, adapter, transport, or external-system structure.

---

## 4. Contract Decision

### 4.1. Publication Is the Outward Meaning Authority

Publication is the Contract authority that determines what authoritative outward meaning the Contract Machine may
establish from declared Core meaning at an exact outward boundary.

```text
Core Contract meaning
        ↓
Publication judgment
        ↓
authorized outward meaning
```

Inside the already-bound Policy World, that judgment applies the declared Publication law. Publication is not the
physical act that carries material outside the Core; the term names semantic authority at the outward boundary, not an
emission mechanism.

The formal concept is `outward meaning`, not only an outward `claim`. A Core result may support authoritative outward
intent as well as descriptive meaning without making its external realization part of the Contract.

### 4.2. The Core Is Sealed and Non-Public by Default

No Contract meaning receives outward authority merely by existing. The Core is sealed from outward visibility unless a
bound Publication Contract positively establishes outward meaning.

Neither internal authority, physical availability, nor Publication-basis participation implies disclosure. Material that
is not explicitly selected into outward meaning remains inside the Core even when Publication is authorized to read it
for judgment.

Publication therefore does not require an exhaustive negative list of hidden material. Absence of positive outward
authority is sufficient: anything not explicitly established as outward meaning remains sealed.

### 4.3. Publication Basis Is Explicit and Does Not Grant Disclosure

Publication may depend only on exact Contract material that has already been established inside the machine and is
explicitly named by the Publication Contract.

A successfully completed Operation result is already an immutable Fact inside the Core. Publication may name the exact
coordinates of that declared result whose values participate in the outward judgment. Other established Contract
meaning, including an established Failure, may participate only when the Publication Contract names it explicitly.

Naming material as Publication basis grants judgment dependency, not outward visibility. A basis coordinate remains
sealed unless the Publication Contract explicitly selects that material into the authorized outward meaning. Material
elsewhere in the Core does not participate merely because it exists or appears relevant.

Publication may not scan the Core, follow undeclared relations, or acquire additional basis through implementation
access. The declaration therefore owns both the dependency and any disclosure from that dependency. The backend only
realizes access and outward transfer already authorized by the Contract.

### 4.4. Publication May Interpret Declared Values Into Different Outward Meaning

Publication is not limited to exposing declared Core meaning unchanged. It may use the values of its explicitly bound
Fact coordinates and other declared Contract meaning to establish the outward meaning required at the boundary.

```text
Core Fact
    temperature = 92
        ↓
Publication
        ↓
outward meaning
    Warning
```

`Warning` need not become a new Core Fact merely because an external consumer requires that classification. The Core
Fact remains `temperature = 92`; Publication establishes only the outward interpretation.

The same rule permits selective disclosure and reduction of detail. A detailed Operation result may support a smaller
outward meaning, and an internal Failure may support a consumer-facing failure meaning without exposing its internal
identity or all of its material.

This authority does not permit Publication to establish new internal truth, redo Invariant, Admission, Lowering, or
State-Machine judgment, or mutate the source material. Fact immutability and Failure finality remain unchanged across
the Publication boundary.

Publication criteria are therefore declarative outward law over the exact basis named by the Contract. Arbitrary user
callbacks, external lookup, hidden implementation state, or host-language control flow cannot become Publication
authority.

### 4.5. Publication Authority Is Exact to the Outward Boundary

Publication authority is established for an exact outward boundary.

The same Contract meaning can have different outward relations at different declared boundaries. Neither structural
similarity nor shared implementation makes those relations interchangeable.

```text
Contract meaning X
    -> outward boundary A
        -> outward meaning Y

Contract meaning X
    -> outward boundary B
        -> no outward meaning
```

A Publication relation is therefore not a global `public` flag on Contract meaning. Adding internal meaning cannot
silently widen an existing outward Contract, and disclosure authorized at one outward boundary does not authorize the
same material at another.

### 4.6. Success and Failure Use the Same Publication Authority

Publication owns the outward surface for successful and unsuccessful Contract meaning.

```text
successful Contract meaning
        ↓
Publication
        ↓
authorized outward success meaning

Failure
        ↓
Publication
        ↓
authorized outward failure meaning
```

An internal Failure may remain entirely internal while Diagnostic processing still records or explains it. When an
outward failure meaning is required, Publication determines that external meaning without changing the internal Failure.

A normal host return path cannot bypass Publication merely because the implementation treats it as success, and an
exception path cannot create a separate implicit publication channel for failure.

### 4.7. Several Declared Basis Items May Support One Publication Judgment

One Publication judgment may depend on several explicitly declared pieces of established Contract material when the
outward meaning genuinely depends on all of them.

```text
declared Core meaning A
exact Fact coordinate B
exact Fact coordinate C
        ↓
Publication
        ↓
one outward meaning X
```

Participation in one outward judgment does not merge the source meanings or transfer their internal authority. Distinct
Facts remain distinct Facts, and distinct Failures remain distinct Failures under ADR-0057.

Runtime arrival order cannot decide which basis items participate. The declaration fixes that dependency before
execution.

### 4.8. Publication Uses Already-Established Basis

Publication may use only declared basis that has already been established by the authority that owns that meaning. If
the basis is Failure, it must already be established under ADR-0057.

Material that has not become authoritative Contract meaning is not Publication basis. Publication cannot create missing
basis material or replace it with another result.

This ADR does not define the exact processing order around Publication. It requires only that the declared basis already
exists before the Publication judgment that uses it.

### 4.9. Established Publication Is Non-Retroactive

Once a Publication judgment establishes an outward meaning, later changes to Contract material do not rewrite that
judgment.

Later processing under changed Contract material may establish a different Publication judgment. The earlier outward
meaning remains the meaning established by the earlier judgment.

### 4.10. Publication Does Not Create a New Scope Ontology

The exact outward boundary is part of Publication meaning, but Publication does not introduce a second general scope
system.

Its boundary is resolved against the machine structure and authorities already defined elsewhere. Runtime topology or
adapter placement cannot invent Publication scope.

### 4.11. Publication and Output Presentation Are Separate

Publication owns what the machine means outwardly. Output Presentation owns the closed external form available to carry
that already-authorized meaning.

```text
immutable Core material
        ↓
Publication
        ↓
authorized outward meaning
        ↓
Output Presentation
        ↓
closed outward form
```

If Publication reduces a detailed internal Fact to `Warning`, every Core detail not selected into that outward meaning
remains sealed before Output Presentation begins. Output Presentation does not rediscover Core detail and choose whether
to hide or expose it; it presents only the outward meaning already established by Publication through the closed shape
defined by its own Contract.

Output Presentation therefore cannot independently reference, obtain, or expose Core material merely because that
material existed in Publication basis. A presentation shape may exist without granting Publication authority, and
representation convenience cannot widen the meaning Publication authorized. The next ADR finalizes the closure rules
between these two Contracts.

### 4.12. Publication and Diagnostic Meaning Are Separate

Diagnostic Evidence explains machine processing. Its existence does not make it outward Contract meaning.

An established Failure can therefore support Diagnostic processing without Publication. If some diagnostic fact must
become part of an outward Contract meaning, that exposure requires an explicit Publication relation rather than direct
leakage from evidence storage or logging.

The later Diagnostic Evidence / Retention ADR decides what evidence exists and what may survive. Publication remains the
authority over outward Contract meaning.

### 4.13. Publication Does Not Own External Effects

Publication ends before external implementation acquires physical control over the outside world.

```text
Contract Machine
    -> Publication
    -> Output Presentation
    -> adapter / realization
    -> external system
```

An external side effect is not Publication Contract meaning. Once outward meaning has been established and presented,
the receiving system decides what action to take under its own authority. It may retry, actuate, store, compensate,
notify, or do nothing without changing what Kontrakt published.

That external freedom does not let a consumer repair ambiguous Contract meaning. The Contract Machine must establish a
coherent outward meaning before the external boundary; the receiver is responsible for action, not for choosing which of
contradictory publications should count.

An external acknowledgement or physical side effect cannot retroactively establish, cancel, or rewrite Publication. If
material from the external system must later matter to the Contract Machine, it must enter again through an explicit
inbound Contract boundary.

### 4.14. Inbound and Outbound Authority Are Symmetric

Kontrakt applies the same authority discipline in both directions.

```text
outside -> core
    external representation has no Contract authority
    Input boundary begins declared inward processing
    owning authorities establish internal meaning

core -> outside
    external realization has no Contract authority
    Publication establishes outward meaning
```

This symmetry keeps adapters replaceable and prevents external system vocabulary from becoming hidden Contract meaning.

### 4.15. Publication Judges Within the Bound Policy World

Governance selects and binds the Policy World. Policy defines the Contract composition of that World, including the
Publication Contract that participates in it.

Publication does not select the World or decide which Publication Contract should replace the bound one. Once the World
is bound, Publication applies that Contract to its own explicitly declared Core basis.

Criteria inside the Publication Contract may interpret declared Fact values or other declared Contract meaning only for
outward judgment. Criteria that choose a Policy World or alter its binding belong to Governance and Policy instead.

### 4.16. Unsuccessful Required Publication Is Failure

Publication does not define a separate denial or stop result family. Mere absence of outward meaning is not itself a
Failure.

Where bound Contract obligations require an outward meaning and the Publication judgment establishes that the
requirement was not satisfied, ADR-0057 governs the resulting Contract Failure. Publication does not create a parallel
failure vocabulary for that case.

Later physical realization failure remains separate. If an already-authorized outward meaning cannot be realized and
Kontrakt can establish that required realization did not complete, that is Realization Failure under ADR-0057 rather
than a revision of the Publication judgment.

---

## 5. Authored and Canonical Publication Material

### 5.1. Publication Is Explicitly Authored Contract Meaning

Unlike Failure, Publication is not intrinsic meaning that can always be derived from another authority.

The existence of internal meaning does not determine whether it may leave the Contract Machine, which parts of an
immutable Fact may participate, or what outward interpretation should be established from them. Those are
application-specific Contract choices, so Publication requires explicit authoring.

Every Publication declaration must name its exact basis. When the Operation result Fact participates, the declaration
must name the exact result coordinates whose values may influence the outward judgment. The declaration must also
express any exact source-to-outward selection required by its outward meaning. Undeclared Contract meaning and
undeclared Fact coordinates have no Publication participation authority.

The frontend must express that dependency and the outward law without requiring the implementation mechanism that
realizes it. The exact IDL placement and surface syntax remain open until the semantic model is complete.

### 5.2. Canonical Publication Material

Any canonical form of Publication must preserve every semantic distinction needed to reproduce the declared outward
judgment.

At minimum, it must preserve:

```text
exact Publication declaration
exact declared Contract-meaning basis
exact declared Fact-coordinate basis where applicable
exact declarative outward criteria
exact outward selection from declared basis where applicable
exact authorized outward meaning
exact outward boundary
```

This is a semantic preservation requirement, not a record-layout decision. The compiler may choose references, tables,
indexes, or another deterministic representation, but no physical form may widen the declared basis or erase a
distinction that can change outward meaning.

Publication does not own a universal `applicable context` object. Governance, Policy, Version, State, Failure, and other
authorities retain their own meaning; Publication preserves only the dependencies that its own declared judgment
actually uses.

### 5.3. Backend Vocabulary Is Not Publication Authority

No host-language or adapter vocabulary defines Publication meaning.

Backend vocabulary may carry stable references to canonical Publication material, but replacing the implementation must
not change the Contract when the same semantic relation remains valid.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning and Representation

Fact objects, Failure carriers, canonical tables, runtime records, generated classes, HIDs, and backend handles may
represent material used by Publication. None of those representations is Publication meaning by itself.

The Contract owns the declared basis and outward relation. A backend is free to change how those semantics are
represented as long as the same Publication law remains recoverable.

### 6.2. Realization

A backend realizes an already-declared Publication law.

It may lower declarative criteria and source access into any deterministic mechanism that preserves the same judgment. A
valid backend may fuse checks, precompute relations, specialize value tests, or eliminate a runtime Publication object
entirely. These choices are implementation.

Publication is not modeled as a host-language call wrapper with pre/post checks, whether implemented through proxies or
interception. Arbitrary user behavior cannot become hidden Publication authority merely because a backend can invoke it.

Physical realization may read only the material admitted by the declared Publication basis and cannot consult external
systems or undeclared machine state to decide what should be outward. Basis access does not authorize realization to
carry that material across the boundary; only the exact outward meaning established by Publication may proceed to Output
Presentation.

### 6.3. Output Presentation Realization

Publication authorization and Output Presentation formation remain distinct even when an optimized backend realizes them
through one physical path.

```text
semantic Publication judgment
        +
semantic Output Presentation contract
        ↓
optimized physical realization
```

Fusion is valid only when both Contract authorities remain semantically recoverable and neither acquires authority from
the other's implementation.

### 6.4. Adapters and External Systems

Adapters stand outside the Contract authority defined here.

An adapter may translate a closed Output Presentation into the mechanism expected by an external system. It may also
translate later external material toward a new Input boundary. Those translations are implementation.

The adapter cannot widen published meaning or reinterpret external acknowledgement as a change to Publication. If a
required external realization does not complete and Kontrakt can establish that fact, ADR-0057 governs the resulting
Realization Failure.

Where execution disappears before the relevant realization outcome can be established, ADR-0057's Crash and
indeterminate-outcome boundary still applies.

### 6.5. Backend Architecture Constraints

This ADR does not choose the concrete Publication IR or its backend representation.

Any backend design must preserve the semantic distinctions established by this ADR and the dependencies needed to
reproduce them deterministically.

Publication lowering cannot derive semantic authority from backend discovery or external-system state.

Caching is reuse rather than authority. A cached Publication compiler product may be reused only while the canonical
material on which its judgment depends remains valid.

V1 may realize Publication through ordinary generated JVM boundaries. V2 may specialize or incrementally reuse those
results, but the Publication Contract must remain unchanged across backend choices.

---

## 7. Verification, Determinism, and Incremental Extensibility

The compiler or verifier must resolve every Contract meaning and Fact coordinate named by Publication to exact declared
Contract material. Implementation-only values cannot substitute for a missing source, and an undeclared coordinate
cannot become relevant because a backend can reach it. It must also reject any outward relation whose source-to-outward
selection is not explicitly authorized by the authored Publication Contract.

Publication declarations that depend on provisional, unresolved, or unauthorized material are invalid before machine
execution. The same rule applies when a declaration attempts to establish internal Contract truth rather than outward
meaning.

The compiler must also reject a Publication Contract whose declared relations are internally contradictory. If the
Contract permits one basis under which two declarations require outward meanings that are explicitly incompatible under
the outward Contract structure, the machine is not allowed to defer that contradiction to runtime.

```text
one permitted declared basis
    -> outward requirement A
    -> incompatible outward requirement B

    = invalid Publication Contract
```

Publication itself does not collect prior results into runtime conflict state and does not choose a winner. Declaration
order, first-match behavior, priority by execution order, worker arrival, hash iteration, or adapter preference have no
Contract authority. The compiler may not infer incompatibility from names such as `Allowed` and `Forbidden`; the
relevant distinction must be present in declared Contract structure.

How a compiler proves overlap or contradiction is implementation. A backend or verifier may use any sound technique
appropriate to the eventual declaration language, but the Contract law remains the same: malformed outward authority is
rejected before it becomes machine behavior.

Once the exact declared basis and Publication relation are the same, every valid compiler and backend execution must
establish the same result.

```text
clean full compilation
incremental compilation
cache reuse
recomputation
single-threaded execution
parallel execution
```

remain semantically equivalent for Publication.

Future incremental invalidation must follow the exact declared dependencies. Adding an unrelated Fact coordinate or
other Core meaning cannot change an existing Publication judgment until the Publication Contract itself acquires a
dependency on that material.

Persisted Publication IR, solver products, generated code, caches, and indexes remain implementation material. Their
storage or cache versions are separate from Contract Version.

Malformed Publication declarations are compile-time invalidity. Runtime Failure remains reserved for unsuccessful
machine judgments and realizations established during actual Contract Machine processing under ADR-0057.

---

## 8. Deferred Decisions

The exact Publication authoring syntax remains open. The frontend must be able to name exact Core basis material, exact
Fact coordinates, declarative value-sensitive outward criteria, any exact selection of basis material that enters the
outward meaning, the outward meaning itself, and the outward boundary without becoming a general-purpose host-language
rule system.

The representation of multiple compatible outward meanings remains open. This ADR decides that contradictory outward
requirements are invalid and that runtime priority is not a repair mechanism, but it does not yet decide whether several
compatible outward meanings are represented as separate Publication judgments, one structured outward meaning, or
another closed form. That question must be resolved together with the outward model rather than by implementation order.

The distinction between permission to establish outward meaning and an obligation to establish it also remains open.
Failure already governs an unsuccessful required Contract judgment, but the frontend model need not introduce separate
Publication categories before that distinction proves necessary.

The exact relation between Publication sites and active processing boundary completion needs additional Whole-Machine
work. This ADR establishes only that every declared Publication basis must already be authoritative before the outward
judgment that depends on it.

Output Presentation owns the next unresolved boundary. The following ADR must determine how an authorized outward
meaning closes over a presentation without moving semantic interpretation back into representation.

Diagnostic Evidence / Retention may later introduce explicit outward use of selected evidence. That work must preserve
the rule that evidence is not public merely because it exists or is retained.

The redesigned frontend and IR must eventually choose concrete representations for Publication and its exact
dependencies. Once that work begins, ADR-0049 and older documents must be revised where they still restrict Publication
to the Operation-return-Fact-only model, direct source-to-target coordinate transfer, Publication-owned runtime stops,
or other assumptions superseded here.

These deferred decisions do not reopen Publication's separation from Core establishment, Output Presentation, compiler
validation, or external realization.

---

## 9. Consequences

### Positive

Publication can use successful Operation result Facts without forcing outward-only classifications back into the Core.
An immutable Fact remains the internal truth while Publication may expose a reduced, abstract, or selectively disclosed
meaning for the external boundary.

Failure also remains intact. Outward failure vocabulary can differ from internal Failure meaning without creating an
aggregate Failure, rewriting failure identity, or turning diagnostic evidence into public authority.

Sealed-by-default outward authority prevents accidental disclosure. Adding new Core material does not silently widen an
existing outward Contract, while Publication can still use explicitly declared internal detail to produce a smaller or
more suitable outward meaning.

Contract coherence is checked before execution rather than delegated to runtime Publication or an external consumer.
This preserves deterministic semantics while leaving the compiler free to choose the verification technique.

### Negative

The Publication declaration language must be rich enough to express value-sensitive outward interpretation while still
remaining closed, declarative, and statically verifiable. This is more demanding than the earlier direct coordinate
relation model.

ADR-0049 is now too narrow where it treats the successful Operation return Fact as the only Publication basis, forbids
broader explicit Contract meaning participation, couples Publication closely to target presentation coordinates, or
introduces Publication-specific runtime stop behavior. Those parts require revision after ADR-0058 is accepted.

The compiler must detect malformed dependencies and contradictory outward relations without turning its chosen analysis
algorithm into Contract meaning.

### Neutral

Publication remains user-authored because outward authority cannot be inferred from Core truth alone. Output
Presentation remains a separate Contract, and adapters remain replaceable realization outside both authorities.