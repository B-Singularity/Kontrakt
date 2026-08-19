# ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary

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

A Contract Machine must distinguish what is authoritative inside its Core from what it is allowed to expose outside that
Core. Accepted material is not automatically public material.

The inbound side already follows the same discipline. External material does not gain Contract authority merely because
an adapter supplied it. It enters through the explicit inbound contracts and receives Core authority only through the
processing that owns that meaning.

The outward side needs the corresponding boundary.

```text
outside material
    -> adapter / realization
    -> Input Contract
    -> Admission / Canonicalization / Lowering
    -> Contract Core

Contract Core
    -> authoritative Core exit result
    -> Publication
    -> closed published material
    -> Output Presentation
    -> external adapter / realization
    -> outside world
```

The symmetry is an authority-boundary symmetry, not a requirement for identical stages in both directions. Inbound
Canonicalization exists because outside representation may be unstable or non-canonical before Core authority is gained.
Publication begins from material that is already authoritative. It therefore does not perform a second Canonicalization
merely to make the pipeline look symmetric.

The adapter is not Contract Authority on either side. An inbound adapter cannot decide what external material means to
the Core, and an outbound adapter cannot decide which Core material may be exposed.

`What Contract Is` introduced Publication because the machine may know more than it is allowed to say. ADR-0049 then
separated accepted Fact authority from Publication and Output Presentation. Later Contract work made Failure an explicit
machine result as well. Publication therefore has to govern successful and failed Core exits without gaining authority
to inspect the Core generally.

Successful processing presents its established Operation Result Fact at the Core exit. Failed processing may present one
or more distinct Failures established under ADR-0057. Those Failures remain separate; Publication does not merge them
into another failure result.

Publication therefore sits at the outward exit of Core processing. The Core is sealed from public visibility, and
Publication receives only the authoritative result that Core processing has contractually established for that exit.
Publication does not create a new outward semantic layer. It positively selects which material of that
already-established result may cross the public boundary. Output Presentation then decides how that closed published
material is represented externally.

ADR-0058 defines that boundary and its V1 authoring surface.

---

## 2. Problem

Without a separate Publication Contract, internal material can become public merely because implementation makes it easy
to expose.

A host-language return value may be serializable, a Failure carrier may be reachable, a logger may be able to print an
internal value, or an adapter may be able to send it. None of those implementation facts grants outward Contract
authority.

Host-language access modifiers do not solve this boundary. `private`, `protected`, and `public` are useful for
controlling which implementation units may directly access a program element, but software often makes them carry a
second duty:
deciding whether information is allowed to cross a Contract boundary. Those duties do not stay aligned. Reflection,
serializers, proxies, generated accessors, and framework conventions may observe material that is lexically hidden,
while Core code may expose getters or similar access paths simply to share material internally. Language-level
accessibility therefore cannot determine outward Contract authority.

Accessors extend the same confusion from observation into mutation. Framework binding, deserialization, generated
mutators, or other reachable implementation paths may invoke setters or equivalent mutation hooks even when the Core
material was intended to remain behind a language-level boundary. Conversely, internal collaboration or framework
integration often causes getters, setters, or similar methods to be opened merely so implementation can proceed, making
their lexical accessibility a poor statement of either outward authority or authority to change Core meaning.

Replacing a raw setter with a behaviorally named object method can constrain the mutation, but it still leaves a mutable
object identity owning semantic change. For Contract material, a different required value is a newly established value;
whether a backend realizes that difference through allocation, storage reuse, or an in-place physical write is an
implementation decision rather than semantic mutation authority.

```text
implementation can expose X
    -> X becomes public
```

must instead be:

```text
Core establishes authoritative Contract material
        ↓
Publication positively selects the material allowed across the public boundary
        ↓
Output Presentation consumes only that closed material
        ↓
replaceable external realization carries the closed presentation outside
```

The earlier Publication model was too closely coupled to direct coordinate transfer from one Operation result Fact into
one selected Output Presentation. That coupling makes Publication aware of presentation structure and gives Output
Presentation a path back toward Core coordinates.

The opposite expansion is also wrong. Publication must not become a second semantic decision system that re-establishes
Core truth, synthesizes a new outward taxonomy, performs value mapping, or redoes Invariant or State-Machine judgment.
Those meanings already belong to other Contract authorities, and external representation belongs to Output Presentation
and its realization.

Publication needs a narrower authority. It must decide which material of the authoritative Core exit result is allowed
to cross the public boundary. Anything not positively selected remains sealed, regardless of host visibility or backend
reachability.

Kontrakt therefore needs an explicit outward exposure boundary that preserves Core authority, closes disclosure before
Output Presentation begins, and leaves physical representation and external transport replaceable.

---

## 3. Decision Drivers

Publication must remain Contract meaning rather than implementation behavior. The ability to read, return, serialize,
log, or transmit material grants no publication authority.

The entire Core is sealed from public visibility by default. Internal authority and outward authority are different
surfaces.

Publication does not inspect the Core generally. Its source is the authoritative result that Core processing has
contractually established for the outward exit.

Publication is positive selection. Presence in the Publication declaration grants outward authority to that selected
material. Omission keeps material sealed. Negative hiding lists, wildcards, and automatic expansion are unnecessary and
would weaken the boundary.

Publication does not transform the selected material into an external shape. Output Presentation owns the closed
external form, and replaceable realization owns physical mapping where a representation conversion is required.

Failure is provided by Kontrakt under ADR-0057. Users do not define a Failure taxonomy for Publication. A Publication
declaration may select a Failure already established within its enclosing Operation and Policy World, then select only
the Failure material that may be exposed.

The Publication scope must not be re-declared. The enclosing Operation and the already-bound Policy World determine the
scope, and the Publication slot binds one exact Publication declaration into that scope.

The semantic model must remain independent of Kotlin or Java source shape. Host-language declarations are frontend
evidence only; authority begins after resolution and lowering into Kontrakt-owned material.

Determinism remains mandatory. The same authoritative Core exit material under the same bound Contract must expose the
same material across valid compiler and backend executions.

---

## 4. Contract Decision

### 4.1. Publication Is Outward Exposure Authority

Publication is the Contract authority that decides which material of the authoritative Core exit result may receive
outward Contract authority.

```text
Core processing
        ↓
authoritative Core exit result
        ↓
Publication
        ↓
closed published material
```

The authoritative Core exit result is a Contract role, not a new carrier or aggregate. Successful processing presents
the established Operation Result Fact in that role. Failed processing may present one or more distinct Failures
established under ADR-0057.

Publication does not make that result more true, convert it into another Core Fact, or create a second outward meaning.
It grants outward authority only to the selected material already carried by the authoritative exit result.

### 4.2. The Entire Core Is Sealed from Public Visibility by Default

No Core Contract meaning becomes public merely by being established, retained, returned, reachable, serializable, or
observable by implementation. The sealing rule applies to the Core as a whole, not only to an Operation Result Fact.

Publication does not receive general visibility into that sealed Core. The only material presented to Publication is the
authoritative result of the Core processing that has reached the outward exit.

Within that result, only positively selected material receives outward Contract authority. Everything else remains
sealed without requiring `private`, `hide`, exclusion lists, or other negative publication rules.

This sealing concerns outward Contract visibility. Diagnostic processing may observe material under its own Contract
without making that material public.

### 4.3. Publication Uses Only the Authoritative Core Exit Result

Publication does not scan the Core, select arbitrary Facts, inspect unrelated State or Transition meaning, follow
internal references, or acquire additional material because a backend can reach it.

A successful exit uses the established Operation Result Fact. A failed exit uses the distinct Failure results
established under ADR-0057.

If information from internal Core meaning must become externally available, the owning Core processing must first make
that information part of its authoritative exit result. Publication cannot repair an insufficient result by reaching
back into the Core.

### 4.4. Publication Is Positive Selection, Not Mapping

Publication selects existing authoritative material. It does not rename, derive, reduce, aggregate, encode, classify, or
otherwise map that material into a new outward meaning.

For a successful Result Fact:

```text
Result
    orderId
    amount
    currency
    providerCode
    internalRiskScore

Publication
    orderId
    amount
    currency
```

only `orderId`, `amount`, and `currency` receive outward authority. `providerCode` and `internalRiskScore` remain
sealed.

If Output Presentation needs a different external field name, nesting, encoding, textual spelling, wire code, or carrier
shape, that is Output Presentation or realization work. Publication does not know those target fields.

Representation mapping may be implemented by ordinary replaceable code after both the published source material and the
required Output Presentation shape are already contractually fixed. Implementation does not decide which Core material
is published.

### 4.5. Published Material Is Closed Before Output Presentation

Publication closes the material surface available to Output Presentation.

The closed published material is a semantic Contract surface, not a required runtime collection, wrapper, generated DTO,
or new user-defined meaning type.

```text
authoritative Core exit result
    ↓
Publication
    ↓
closed published material

---------------- outward semantic wall ----------------

closed published material
    ↓
Output Presentation
    ↓
closed external representation
```

Output Presentation may see only the material admitted to that published surface. It has no independent authority to
inspect the Core, the authoritative Core exit result, or Publication metadata that would reveal sealed material.

### 4.6. Successful Result Publication Is Partial and Positive

The enclosing Operation already determines the exact successful Result Fact. Publication does not repeat that Result
type or create another success-result declaration.

A successful Publication declaration lists only the Result coordinates that receive outward authority. Result
coordinates omitted from the declaration remain sealed.

This is intentionally different from a complete-coverage frontend such as Canonicalization. Publication is a positive
partial projection of the already-known Result surface.

Adding a new Result coordinate does not silently widen an existing Publication declaration. The new coordinate remains
sealed until it is explicitly added to Publication.

### 4.7. Failure Publication Uses the Kontrakt Failure Shape

Users do not author Failure kinds, Failure classes, public error taxonomies, or Failure mappings for Publication.
ADR-0057 provides one Failure semantic shape:

```text
Failure
    source
    failure meaning
    applicable context
    boundary
```

The concrete content changes with the exact Failure source. `applicable context` remains source-specific and frozen to
the Contract context that was applicable when that Failure was established.

Publication may select a Failure already established within its enclosing Operation and bound Policy World, then select
only the Failure material that may receive outward authority. Material not selected remains sealed.

### 4.8. Failure Sources Are Addressed Through Existing Mounted Paths

Publication does not introduce a new Failure identity or a `from` coordinate.

The enclosing Operation and Policy World already define the mounted Contract and State-Machine paths. A Failure
selection uses that existing path to identify the exact authority whose Failure may be exposed.

Conceptually:

```text
failure.invariant
    -> the exact Invariant mounted in this Operation and Policy World

failure.budgets.requestTime
    -> the exact request-time Budget mounted in this Operation and Policy World

failure.stateMachine
    -> the exact State-Machine authority mounted for this Operation and Policy World
```

The source path is selection evidence. It does not mean the Failure `source` coordinate itself is published. If `source`
is to receive outward authority, it must be selected as Failure material in its own right.

A Publication declaration cannot address another Operation, another Policy World, an unmounted Contract declaration, or
an external adapter merely because such material exists elsewhere in the project.

### 4.9. Failure Material Is Also Positive Selection

The same sealed-by-default rule applies inside each selected Failure.

A Publication declaration may select common Failure coordinates and may select coordinates from that source's applicable
context. The compiler resolves the mounted source first, then validates the context coordinates against the exact
source-specific Failure context available there.

A context coordinate that is absent from that source cannot be named. A context container is not an implicit wildcard:
new context material added later does not become public unless the Publication declaration is changed to select it.

When several independent Failures are established in the same active processing boundary and several matching Failure
selections exist, each Failure is processed independently. Publication does not choose a primary Failure, aggregate
them, or apply declaration-order priority.

### 4.10. Publication Scope Comes from Operation and Policy World Binding

Publication does not declare its own `scope`, `operation`, `world`, recipient, channel, or runtime selector.

Governance selects and binds the Policy World. Policy defines the Contract composition of that World. The enclosing
Operation supplies the operation-local result and mounted Contract paths. The Publication slot selects one exact
Publication declaration inside that already-defined scope.

```text
Operation
    + bound Policy World
    + Publication slot
        ↓
exact Publication scope
```

This containment is not runtime inference. It is the explicit result of the already-authored Contract graph.

### 4.11. Publication Does Not Own Core Mutation or Re-entry

Publication cannot establish a new Core Fact, revise Failure, redo Admission or Invariant judgment, decide State or
Transition meaning, or supply hidden input to another Core authority.

Published material has no direct re-entry path into Core or State-Machine judgment. Once material leaves the Contract
Machine, it is external material. If it later enters this or another Contract Machine, it must pass through the
receiving machine's ordinary inbound Contract boundary and gain authority again there.

### 4.12. Publication and Diagnostic Meaning Are Separate

Diagnostic Evidence explains machine processing. Its existence does not make it outward Contract material.

An established Failure can support Diagnostic processing without Publication. Logs, traces, stack material, exceptions,
backend observations, and retained evidence do not become published merely because implementation can observe them.

The later Diagnostic Evidence / Retention ADR decides what evidence exists and what may survive. Publication remains the
authority over outward exposure of the authoritative Core exit result.

### 4.13. External Adapters Are Outside Publication Authority

Publication and Output Presentation complete Kontrakt's outward Contract work before an external adapter acquires
physical control of transport, storage, framework delivery, or another external system.

```text
Contract Machine
    -> authoritative Core exit result
    -> Publication
    -> closed published material
    -> Output Presentation

external implementation
    -> framework / serializer / transport / network / storage adapter
    -> outside system
```

A failure in that external adapter is not turned back into a Failure of this Publication Contract. Kontrakt does not
create an HTTP, network, serializer, broker, or remote-peer Failure vocabulary merely because the published material is
later carried through those systems.

If another living Contract Machine needs to govern that external work, it must do so under its own explicit Contract
boundary.

### 4.14. Inbound and Outbound Authority Remain Separate

External representation has no inward Contract authority merely because it arrives, and Core material has no outward
Contract authority merely because it can be emitted.

Inbound contracts govern how outside material gains Core authority. Publication governs which authoritative Core exit
material gains outward authority. The two boundaries are opposite sides of the Core, but they do not require mirrored
stage vocabularies or reverse transformations.

### 4.15. Publication Is Deterministic Selection

Publication does not use first-match rules, runtime priority, declaration order, callback behavior, external lookup, or
backend preference.

For the same authoritative Core exit result under the same bound Publication Contract, the selected published material
is the same in every valid execution.

Malformed Publication selection is definition-time invalidity. The compiler must reject references to unavailable Result
coordinates, invalid Failure paths, unavailable Failure material, duplicate conflicting declarations, or source material
outside the enclosing Operation and Policy World.

---

## 5. V1 User Authoring API and Canonical Publication Material

### 5.1. Publication Uses the Same Frontend Discipline as Other One-Dimensional Contracts

V1 Publication is authored in a separate Kotlin or Java source declaration and selected through the `publication` slot
of the interface IDL, following the existing one-dimensional frontend discipline.

The host declaration is source evidence only. It is not instantiated, its constructors do not execute, and its class
identity, package placement, runtime reflection handle, inheritance, or object identity does not become Publication
authority.

```text
restricted host-language Publication declaration
    ↓
IDL publication slot selection
    ↓
resolution inside enclosing Operation and bound Policy World
    ↓
canonical Kontrakt Publication material
    ↓
host frontend authority erased
```

The IDL gives the selected declaration its Publication role. The enclosing Operation and Policy World give it its scope.

### 5.2. V1 Publication Source Grammar

The V1 source grammar is a closed, declaration-only projection of the already-known Operation result and Failure source
paths.

The following is grammar illustration rather than a commitment to package names of generated frontend support symbols:

```text
class PlaceOrderPublication private constructor(
    result: Result,
    failures: Failures,
) {
    class Result private constructor(
        orderId: Long,
        amount: Long,
        currency: String,
    )

    class InvariantContext private constructor(
        requestedQuantity: Int,
        availableQuantity: Int,
    )

    class Invariant private constructor(
        meaning: FailureMeaning,
        context: InvariantContext,
    )

    class RequestTimeBudgetContext private constructor(
        allowance: Duration,
    )

    class RequestTimeBudget private constructor(
        meaning: FailureMeaning,
        context: RequestTimeBudgetContext,
    )

    class Budgets private constructor(
        requestTime: RequestTimeBudget,
    )

    class Failures private constructor(
        invariant: Invariant,
        budgets: Budgets,
    )
}
```

`Result` is a fixed frontend section. Its coordinates must resolve to coordinates of the enclosing Operation's exact
successful Result Fact. Presence means publication; omission means sealed.

`Failures` mirrors only the mounted Failure-source paths selected for outward exposure. `invariant` resolves through the
current Operation and Policy World to the exact mounted Invariant authority. `budgets.requestTime` resolves through the
same scope to the exact mounted request-time Budget authority. No `from` coordinate repeats those bindings.

The nested context declaration is not a new runtime Failure context type. It is frontend evidence selecting coordinates
from the source-specific `applicable context` already established by ADR-0057. The compiler resolves the Failure source
before validating those context coordinates.

The section and helper class names are frontend grammar and source diagnostics. They do not create Result identity,
Failure identity, Contract identity, or runtime object hierarchy.

### 5.3. IDL Binding

The interface IDL selects one exact Publication source symbol through the existing Publication slot.

```text
operation placeOrder(...) : OrderResult {
    contractPipeline {
        ...
        publication PlaceOrderPublication
        ...
    }
}
```

Where Policy World composition supplies different Publication declarations for the same Operation, each declaration is
resolved only inside the World in which it is bound. The Publication source does not repeat `placeOrder`, the Policy
World name, Result type, or mounted Contract declarations.

The exact textual Policy World binding syntax is owned by the Policy and Governance frontend work. This ADR requires the
resolved Publication scope to be the enclosing Operation under the already-bound Policy World.

### 5.4. Positive Selection Law

The Publication frontend contains no `publish`, `private`, `hide`, `exclude`, or wildcard operator.

Presence is the positive declaration.

```text
Result declares amount
    -> amount receives outward authority

Result omits internalRiskScore
    -> internalRiskScore remains sealed
```

The same law applies to Failure material.

```text
Failures.invariant declares meaning
    -> that Failure meaning may be exposed

Failures.invariant omits source and boundary
    -> source and boundary remain sealed
```

The absence of a declaration is therefore meaningful and stable. Adding a new Result field or Failure context coordinate
does not widen an existing Publication declaration.

### 5.5. Failure Source Resolution

A Failure section is valid only when its structural path resolves to an exact Failure-capable authority mounted in the
enclosing Operation and bound Policy World.

The compiler resolves the path before it resolves selected Failure material.

```text
Failures.invariant
    -> current Operation
    -> current Policy World
    -> invariant slot
    -> exact mounted Invariant authority

Failures.budgets.requestTime
    -> current Operation
    -> current Policy World
    -> budgets.requestTime binding
    -> exact mounted Budget authority
```

A declaration cannot use a class name, string, package scan, runtime registry, or another World's binding to widen that
scope.

Contract-owned realization remains an implementation axis beside the exact Contract position it realizes. Publication
does not create a separate user-authored external-realization namespace. Failures from adapters after Output
Presentation are outside this Contract Machine and cannot be selected here.

### 5.6. Failure Coordinate Resolution

After the exact Failure source has been resolved, the compiler exposes only the Failure coordinates valid for that
source.

Common Failure coordinates come from ADR-0057:

```text
source
failure meaning
applicable context
boundary
```

The selected source determines the valid shape of `applicable context`. A Budget Failure context and a State-Machine
Failure context are not forced into one universal nullable schema.

A Publication declaration that names a context coordinate unavailable for the resolved source is invalid. Context
selection is explicit to the leaf coordinate; selecting a context container does not implicitly publish later-added
material.

### 5.7. Canonical Publication Material

Canonical Publication material preserves only the semantic selections required to reproduce the outward boundary.

At minimum, it preserves:

```text
exact Publication declaration identity
exact enclosing Operation
exact bound Policy World
selected successful Result coordinates
selected Failure source paths resolved to exact authorities
selected common Failure coordinates
selected source-specific applicable-context coordinates
```

The canonical form does not preserve host-language nesting, constructors, helper class identity, parameter reflection
handles, Output Presentation fields, serializer mappings, external adapter configuration, or runtime framework
vocabulary as Publication authority.

A backend may encode these selections with stable identifiers, frozen tables, indexes, or another deterministic
representation. Those are implementation choices.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning and Representation

Fact objects, Failure carriers, canonical tables, runtime records, generated classes, HIDs, and backend handles may
represent the authoritative Core exit result or Publication material. None of those representations grants outward
authority.

The Contract owns the exact positive selections and the closed published surface. A backend may change how those
semantics are represented as long as no unselected material becomes visible to Output Presentation.

### 6.2. Publication Realization

A backend realizes an already-resolved Publication selection.

It may erase a runtime Publication object entirely, precompute selected coordinate indexes, fuse selection with Output
Presentation access, or specialize the physical path. Those implementation choices cannot inspect arbitrary Core
material, add selected coordinates, remove selected coordinates, or make host-language accessibility part of Publication
meaning.

Publication is not modeled as a host-language wrapper, proxy interception layer, callback DSL, serializer annotation,
reflection scan, or runtime access-control object.

### 6.3. Handoff to Output Presentation

Publication and Output Presentation may be physically fused by an optimized backend, but their Contract authorities
remain distinct.

The semantic handoff is only the closed published material. Output Presentation determines the external shape from that
material and cannot receive a back-reference to sealed Core material as additional Contract authority.

```text
semantic Publication
        -> closed published material

semantic Output Presentation
        -> closed external representation

optimized backend
        -> may realize both in one physical path
```

Fusion is valid only when the Publication boundary remains recoverable and the resulting external representation uses no
material outside the published surface.

### 6.4. External Adapters and Frameworks

Outbound adapters and frameworks may consume only the material provided after Publication and Output Presentation. They
do not receive direct Contract authority over Core Result Facts, Failure carriers, State, Transition, or other sealed
Core material.

This prevents serializer, reflection, framework binding, or generated-accessor reachability from silently widening the
public Contract surface.

Package, module, and language access modifiers remain useful implementation tools for ordinary source organization. They
no longer have to carry Publication authority.

Once the closed Output Presentation has crossed into an external adapter, failures of that adapter belong to the
external implementation or to another Contract Machine that explicitly governs it. They are not sent backward through
this Publication Contract.

### 6.5. Backend Architecture Constraints

This ADR does not choose the concrete Publication IR, published-surface representation, or runtime carrier.

Any backend design must preserve the exact positive selections and must keep both forbidden dependency directions
closed:

```text
Publication -> arbitrary Core material          forbidden
Output Presentation -> sealed Core material     forbidden
```

Caching is reuse rather than authority. A cached Publication compiler product may be reused only while the canonical
material on which its selections depend remains valid.

V1 may realize Publication through ordinary generated JVM machinery. Later backends may specialize or incrementally
reuse those results, but the Publication Contract must remain unchanged across backend choices.

---

## 7. Verification, Determinism, and Incremental Extensibility

The compiler or verifier must resolve every successful Result coordinate named by Publication to the enclosing
Operation's authoritative Result Fact. Unknown, duplicate, or structurally invalid Result selections are compile-time
invalidity.

The compiler must resolve every Failure structural path through the enclosing Operation and already-bound Policy World.
A path that reaches another Operation, another World, an unmounted Contract, an external adapter, or no exact authority
is invalid.

After a Failure source is resolved, every selected common Failure coordinate and applicable-context coordinate must be
valid for that exact source. The compiler must not fill missing coordinates with `null`, infer them from similarly named
material, or widen a context selection when the context later gains new fields.

The frontend must remain declaration-only. Constructors are never invoked. Host methods, callbacks, lambdas, arbitrary
expressions, runtime lookup, inheritance, annotations, package scans, reflection order, and backend conventions cannot
add Publication meaning.

Publication does not target Output Presentation fields. The verifier must reject any Publication source form that binds
a Core coordinate directly to an Output field, transport field, serializer property, external error code, or adapter
channel.

Multiple independent Failures remain independent. If several established Failures each match an explicitly selected
Failure source path, each is projected under its own Publication selection. Declaration order, first-Failure arrival,
worker scheduling, hash iteration, or backend preference cannot suppress another selected Failure.

For the same authoritative Core exit result and the same bound Publication Contract, every valid compiler and backend
execution must produce the same closed published material.

```text
clean full compilation
incremental compilation
cache reuse
recomputation
single-threaded execution
parallel execution
```

remain semantically equivalent for Publication.

Future incremental invalidation follows exact dependencies. A Result coordinate change invalidates only Publication
material that resolves against that Result surface. A mounted Failure-source binding or selected Failure-context shape
change invalidates only the Publication material that depends on it. Unrelated sealed Core changes do not widen or alter
an existing Publication selection.

Persisted Publication IR, generated code, caches, indexes, and verification products remain implementation material.
Their storage or cache versions are separate from Contract Version.

---

## 8. Deferred Decisions

Output Presentation owns the next boundary. The following ADR must define its user authoring API, how it consumes the
closed published material, how several independently published Failure results are represented, and how representation
mapping remains implementation without recovering sealed Core material.

The exact source spelling of frontend support symbols such as standard Failure coordinate handles is an implementation
and frontend packaging decision. Their semantic roles are fixed by this ADR; host package names are not Contract
authority.

The exact scheduling relation between active processing boundary completion and establishment of the authoritative Core
exit result remains Whole-Machine work. This ADR requires only that Publication receive already-established exit
material.

Diagnostic Evidence / Retention may later define how diagnostic material is retained and observed. That work must
preserve the rule that diagnostic availability does not itself grant outward Contract authority.

After ADR-0058 is accepted, ADR-0047, ADR-0049, and other older documents must be revised where they still couple
Publication directly to Output Presentation coordinates, keep Publication as an operation-local mapping body, or assume
Publication-specific mapping and realization semantics superseded here.

These deferred decisions do not reopen sealed-by-default Core visibility, positive Publication selection, Operation and
Policy World scope, or the separation between Publication and Output Presentation.

---

## 9. Consequences

### Positive

Publication becomes a small explicit outward authority rather than a second semantic engine. Core truth remains distinct
from what external code may observe or serialize.

Sealed-by-default visibility covers the entire Core, while Publication itself sees only the authoritative Core exit
result. Successful Result publication is a positive partial projection; Failure publication reuses Kontrakt's existing
Failure shape and exact mounted authority paths rather than introducing a public error taxonomy.

The user API is consistent with the broader one-dimensional frontend discipline. One restricted host declaration is
selected by the IDL, the enclosing Operation and Policy World determine scope, and compiler-owned canonical material
replaces host syntax as authority after lowering.

Omitted material stays sealed even when Result or Failure context surfaces grow later. Publication therefore does not
silently expand when new internal fields are added.

Output Presentation receives a closed material surface without gaining Core visibility. External frameworks and adapters
can work with the resulting presentation without using `private`, getters, setters, reflection rules, or serializer
reachability as Contract authority.

### Negative

The compiler must maintain exact structural resolution from Publication Failure paths into the enclosing Operation and
bound Policy World, then validate source-specific Failure context selections without turning them into one universal
Failure schema.

The positive-selection frontend is intentionally explicit. Publishing many Result or Failure coordinates requires naming
them, because convenience wildcards would make later internal expansion an outward disclosure change.

ADR-0047 and ADR-0049 retain older Publication assumptions and require correction after ADR-0058 is accepted.

### Neutral

Publication remains user-authored because outward authority cannot be inferred from Core reachability. Kotlin or Java
class syntax is only a frontend presentation and may be replaced later without changing canonical Publication meaning.
Package and module access modifiers remain ordinary implementation-visibility tools, while Publication owns the separate
question of what authoritative Core exit material may cross the public boundary.