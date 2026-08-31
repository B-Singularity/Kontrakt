# ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary

## Status

Accepted

## Date

2026-08-17

## Related

- `../../the-most-important-thing/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
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

The inbound side already follows this rule. External material does not gain Core authority merely because an adapter
supplied it. It must pass through the Contract processing that owns its meaning.

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

This is a symmetry of authority boundaries, not of pipeline stages. Inbound Canonicalization exists because outside
representation may be unstable before it gains Core authority. Publication starts from material that is already
authoritative, so it does not perform a second Canonicalization merely to mirror the inbound path.

An adapter is not Contract Authority on either side. It cannot decide what incoming material means to the Core, and it
cannot decide which Core material may leave it.

`What Contract Is` introduced Publication because a machine may know more than it is allowed to say. ADR-0049 separated
Fact authority from Publication and Output Presentation. ADR-0057 later made Failure an explicit machine result.
Publication must therefore govern both successful and failed exits without gaining general access to the Core.

A successful exit presents the established Operation Result Fact. A failed exit may present one or more distinct
Failures established under ADR-0057. Publication does not merge those Failures.

Publication sits at the outward exit of Core processing. It receives only the authoritative result established for that
exit and positively selects which of its material may cross the public boundary. Output Presentation then gives the
selected material its external form.

ADR-0058 defines that authority and its V1 authoring surface.

---

## 2. Problem

Without a separate Publication Contract, internal material can become public merely because implementation makes it easy
to expose.

A return value may be serializable. A reachable internal carrier may be printable or transferable. Those implementation
facts do not grant outward Contract authority.

Host-language access control does not solve this problem. Language visibility answers which implementation code can
reach a program element. Publication answers whether authoritative Core material may cross the Contract boundary. The
two questions are different, and framework machinery can make their difference visible in practice.

Accessors create the same confusion for mutation. A reachable mutator may let implementation change an object even
though that reachability says nothing about authority to change Contract meaning. Giving the mutator a more meaningful
method name does not fix the deeper problem if semantic change is still owned by mutable object identity. For Contract
material, a different required value is a newly established value. How a backend realizes that change physically is an
implementation choice.

```text
implementation can expose X
    -> X becomes public
```

must instead be:

```text
Core establishes authoritative Contract material
        ↓
Publication selects what may cross the public boundary
        ↓
Output Presentation forms the external representation
        ↓
replaceable realization carries it outside
```

The earlier Publication model was also too close to direct mapping from an Operation Result Fact into one Output
Presentation. That made Publication aware of presentation structure and gave presentation logic a path back toward Core
coordinates.

The opposite expansion is equally wrong. Publication must not become another semantic decision system. It does not
re-establish Core truth or invent outward business meaning. It also does not redo judgment owned by another Contract.
External representation belongs to Output Presentation and its realization.

Publication needs a narrower authority: decide which material of the authoritative Core exit result may cross the public
boundary. Anything not positively selected remains sealed.

Kontrakt therefore needs an explicit outward exposure boundary that closes disclosure before Output Presentation begins
and keeps external representation replaceable.

---

## 3. Decision Drivers

Publication is Contract meaning, not implementation behavior. Implementation reachability grants no publication
authority.

The Core is sealed from public visibility by default. Outward authority must be established separately.

Publication receives only the authoritative Core exit result. It does not inspect the Core generally.

Publication uses positive selection. Selected material receives outward authority; omitted material remains sealed.

Publication does not shape external representation. Output Presentation owns that boundary.

Failure meaning remains owned by ADR-0057. Publication only selects already-established Failure material for outward
exposure.

Publication does not repeat its scope. The enclosing Operation and bound Policy World already provide it.

Host-language declarations are frontend evidence only. Canonical Kontrakt material owns the final meaning.

Publication must be deterministic for the same authoritative exit result under the same bound Contract.

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

The authoritative Core exit result is a Contract role, not a new aggregate carrier. Successful processing presents the
established Operation Result Fact in that role. Failed processing may present distinct Failures established under
ADR-0057.

Publication does not make that result more true or convert it into another Core meaning. It only grants outward
authority to selected material already present in the result.

### 4.2. The Core Is Sealed Until Material Reaches Publication

No Core meaning becomes public merely because it has been established or can be reached by implementation.

Publication receives only the authoritative result that reaches the outward exit. A successful exit supplies the
established Operation Result Fact. A failed exit supplies the distinct Failures already established under ADR-0057.
Publication cannot reach back into the Core to obtain other material.

Within the received result, only selected material gains outward authority. Everything else remains sealed without a
negative hiding rule.

If internal meaning must become externally available, the processing that owns that meaning must first establish it as
part of the authoritative exit result.

This sealing concerns outward Contract authority. Diagnostic processing may observe Core material under its own Contract
without making that material public.

### 4.3. Publication Is Positive Selection, Not Mapping

Publication selects existing authoritative material. It does not turn that material into a new outward meaning.

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

only `orderId`, `amount`, and `currency` receive outward authority. The other coordinates remain sealed.

If the external representation needs different names or structure, Output Presentation owns that decision. Replaceable
implementation may realize the mapping only after both the published material and the required output shape are fixed.

### 4.4. Publication Closes the Surface Before Output Presentation

Publication produces a closed semantic surface for Output Presentation.

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

The published surface is Contract meaning. It does not require a particular runtime wrapper or generated carrier.

Output Presentation may consume only that surface. It has no Contract authority to recover sealed Core material.

### 4.5. Successful Result Publication Is Partial

The enclosing Operation already determines the successful Result Fact. Publication does not repeat that Result type.

The Publication declaration names only the Result coordinates that receive outward authority. Omitted coordinates remain
sealed.

This is intentionally different from a complete-coverage frontend such as Canonicalization. A new Result coordinate does
not widen an existing Publication declaration until the user adds it.

### 4.6. Failure Publication Uses the Existing Failure Shape

Users do not create a separate public Failure taxonomy for Publication. ADR-0057 already defines the Failure shape:

```text
Failure
    source
    failure meaning
    applicable context
    boundary
```

The content of `applicable context` depends on the exact Failure source and is frozen when the Failure is established.

Publication first selects an already-established Failure source domain, then selects which material from that Failure
may receive outward authority.

### 4.7. Failure Source Selection Reuses Existing Authority Declarations

Unlike the successful Result, an Operation may establish Failures from several independent authorities. Every Failure
exposure entry therefore identifies exactly one Failure source domain.

Contract Failure selection reuses existing nominal declarations. A selected one-dimensional Contract names the whole
source domain when it owns several direct exact authorities. One direct nominal entry selects one exact authority. If a
Contract has no direct exact authorities, its top-level declaration is already the exact selector.

```text
StockInvariant
    -> the selected StockInvariant authority

CalculateBudget
    -> the selected CalculateBudget domain

CalculateBudget.CanonicalizationElapsed
    -> that exact Budget authority
```

The authority surface stays flat. Publication does not add an intermediate selector hierarchy.

State-Machine Failure follows the same law. The selected Machine names the broad State-Machine domain, while one direct
Transition declaration names one exact Transition authority.

Realization Failure has no user-authored hierarchy of exact realization declarations. The Kontrakt-provided
`RealizationFailure` marker therefore selects the already-established Realization Failure domain. The marker does not
establish or reinterpret the Failure.

Each Failure exposure entry contains exactly one source selector. The selector is determined by the resolved nominal
type. Its local parameter name has no Publication meaning.

A broad Contract selector follows the direct authority membership of the selected Contract version. If a new Budget
version adds another direct Budget entry, selecting that Budget as a whole includes the new entry. This is the meaning
of the broad selection, not wildcard material publication.

Exact selection reuses the existing nominal source symbol. For example,
`CalculateBudget.CanonicalizationElapsed` refers to the Budget authority already defined by ADR-0051; Publication does
not restate its Budget coordinates.

Selecting a Failure source does not publish the Failure `source` coordinate. That coordinate must be selected separately
if it is allowed to leave the Core.

A selector must resolve inside the enclosing Operation and bound Policy World. Publication cannot address authority that
is not part of that scope.

### 4.8. Failure Material Is Positive Selection

The sealed-by-default rule also applies inside a selected Failure.

Publication may select common Failure coordinates and leaf coordinates from that source's `applicable context`. A
context leaf must exist for every exact Failure source to which the entry applies.

Selecting a context container does not publish its contents implicitly. New context material remains sealed until it is
selected explicitly.

The same source selector may appear in only one Failure exposure entry. Duplicate broad selectors and duplicate exact
selectors are invalid.

A broad selector and one of its direct exact selectors may coexist. The exact entry is complete for that exact source.
The broad entry does not contribute material to it and continues to govern the remaining direct authorities.

```text
CalculateBudget
    -> failure meaning

CalculateBudget.CanonicalizationElapsed
    -> allowance
```

therefore means:

```text
other CalculateBudget Failures
    -> failure meaning

CalculateBudget.CanonicalizationElapsed Failure
    -> allowance
```

This completion is resolved at definition time. It is not runtime priority or inheritance, and the two entries are not
combined.

If several independent Failures are established in the same active processing boundary, each matching Failure is
published independently under its one effective entry. Publication does not choose a primary Failure or aggregate them.

### 4.9. Publication Scope Comes from Existing Binding

Publication does not declare another Operation or Policy World scope. It does not choose the external recipient.
Delivery channel and runtime selection also belong elsewhere.

Governance binds the Policy World. Policy determines the Contract composition of that World. The enclosing Operation
provides the operation-local result and applicable authorities. The Publication slot then selects one Publication
declaration inside that scope.

```text
Operation
    + bound Policy World
    + Publication slot
        ↓
exact Publication scope
```

This scope comes from the authored Contract graph rather than runtime inference.

### 4.10. Publication Does Not Own Core Mutation or Re-entry

Publication cannot establish new Core meaning or revise an existing Failure. It does not redo Admission or Invariant
judgment. State and Transition meaning also remain with the State-Machine authority. Publication cannot feed published
material back into another Core judgment.

Once published material leaves the Contract Machine, it is external material. If it later enters this or another
Contract Machine, it must gain authority again through the receiving machine's inbound boundary.

### 4.11. Diagnostic Meaning Is Separate

Diagnostic Evidence explains machine processing. Its existence does not make it outward Contract material.

An established Failure may support Diagnostic processing without Publication. Runtime observations belong to Diagnostic
Evidence rather than Failure publication unless they are already part of the established Failure meaning under ADR-0057.

The later Diagnostic Evidence / Retention ADR decides how such evidence is retained. Publication remains limited to the
authoritative Core exit result.

### 4.12. External Adapters Are Outside Publication Authority

Publication and Output Presentation complete Kontrakt's outward Contract work before an external adapter takes physical
control of delivery.

```text
Contract Machine
    -> authoritative Core exit result
    -> Publication
    -> closed published material
    -> Output Presentation

external implementation
    -> adapter
    -> outside system
```

A failure after that handoff is not turned back into a Failure of this Publication Contract. If another Contract Machine
must govern that external work, it needs its own explicit boundary.

### 4.13. Inbound and Outbound Authority Remain Separate

External representation has no inward Contract authority merely because it arrives. Core material has no outward
Contract authority merely because it can be emitted.

Inbound contracts govern how outside material gains Core authority. Publication governs how authoritative Core exit
material gains outward authority. These boundaries do not require mirrored stages or reverse transformations.

### 4.14. Publication Is Deterministic

Broad and exact Failure selections are resolved at definition time. Runtime order does not change which entry is
effective.

For the same authoritative Core exit result under the same bound Publication Contract, every valid execution selects the
same published material. Runtime priority and external lookup cannot change that result.

Malformed Publication selection is definition-time invalidity.

---

## 5. V1 User Authoring API and Canonical Publication Material

### 5.1. Frontend Discipline

V1 Publication is authored in a separate Kotlin or Java source declaration and selected through the interface IDL's
`publication` slot.

The host declaration is source evidence only. It is never instantiated for Contract authority. Package placement and
runtime class identity do not survive as authority. Resolution lowers the declaration into canonical Kontrakt material.

The manifest is declaration-only. Inheritance cannot add Publication meaning, and generic type arguments cannot encode
it. An annotation cannot establish Publication authority. Methods and callbacks are not Publication entries. The
constructors never execute.

```text
restricted host-language Publication declaration
    ↓
IDL publication slot selection
    ↓
resolution inside enclosing Operation and bound Policy World
    ↓
canonical Kontrakt Publication material
```

The IDL supplies the Publication role. Existing Operation and Policy World binding supply the scope.

### 5.2. V1 Publication Source Grammar

A V1 Publication is one uninstantiable manifest class with an empty outer constructor. The outer class names the table.
Its direct nested classes are the entries. The outer constructor does not repeat their membership.

The following example illustrates the grammar without fixing package names for frontend support symbols:

```kotlin
class PlaceOrderPublication private constructor() {

    class Result private constructor(
        orderId: Long,
        amount: Long,
        currency: String,
    )

    class StockInvariantFailure private constructor(
        invariant: StockInvariant,
        meaning: FailureMeaning,
        requestedQuantity: FailureContext,
        availableQuantity: FailureContext,
    )

    class BudgetFailures private constructor(
        budget: CalculateBudget,
        meaning: FailureMeaning,
    )

    class CanonicalizationElapsedBudgetFailure private constructor(
        budget: CalculateBudget.CanonicalizationElapsed,
        allowance: FailureContext,
    )

    class SubmitTransitionFailure private constructor(
        transition: OrderMachine.SubmitDraft,
        meaning: FailureMeaning,
    )

    class RealizationFailures private constructor(
        realization: RealizationFailure,
        meaning: FailureMeaning,
        boundary: FailureBoundary,
    )
}
```

`Result` is the fixed successful-result entry. Its parameters must resolve to coordinates of the enclosing Operation's
Result Fact with matching admitted sorts. The Result source is not repeated because the Operation already fixes it.
Different Policy Worlds may bind different Publication declarations when they require different Result exposure.

Every other direct nested class is one Failure exposure entry. Exactly one parameter type must resolve to an admitted
Failure source selector under Section 4.7. The selector parameter's local name has no Contract meaning.

The other parameters select Failure material. `FailureMeaning`, `FailureSource`, and `FailureBoundary` select the
matching common Failure coordinates. `FailureContext` uses its parameter name to select one leaf from the resolved
source's
`applicable context`.

Failure-entry class names are source handles for authoring and diagnostics. They do not create Failure identity or
priority. Publication entries exist at one direct manifest level and cannot contain further Publication entries.

In the example, `BudgetFailures` publishes `failure meaning` for Budget authorities without an exact Publication entry.
`CanonicalizationElapsedBudgetFailure` is complete for its exact Budget source, so that Failure publishes `allowance`
only.

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

The Publication declaration does not repeat the Operation or Policy World. It also does not repeat source declarations
that are already selected in that scope.

The exact textual syntax for Policy World composition remains owned by the Policy and Governance frontend work.

### 5.4. Positive Selection in Source

The frontend uses presence as the publication declaration. It has no separate publish command and no negative exclusion
rule. It also has no material wildcard.

```text
Result declares amount
    -> amount receives outward authority

Result omits internalRiskScore
    -> internalRiskScore remains sealed
```

The same rule applies to Failure material.

A later Result coordinate or Failure-context leaf remains sealed until it is named. Broad Failure source selection
follows the source membership rule already defined in Section 4.7; it does not imply broad material selection.

### 5.5. Failure Source Resolution

The compiler resolves the single source-selector type of each Failure entry inside the enclosing Operation and bound
Policy World.

Contract and State-Machine selectors must be genuine source symbols already present in the selected Contract graph.
`RealizationFailure` resolves only the established Realization Failure domain.

Source resolution does not use text matching or discovery by source layout. A parameter name cannot point into another
declaration and cannot establish Failure source meaning. Runtime registries and reflection order also have no role in
source resolution.

Failures that occur after Output Presentation has handed material to an external adapter are outside this Publication
Contract and cannot be selected here.

### 5.6. Failure Coordinate Resolution

After source resolution, the compiler validates the selected Failure material against every exact source to which the
entry is effective.

ADR-0057 defines the common Failure coordinates:

```text
source
failure meaning
applicable context
boundary
```

Each exact source determines its own `applicable context`. Publication does not flatten those contexts into one nullable
schema.

Realization Failure exposes only the limited Contract material preserved by ADR-0057. Diagnostic observations do not
become Publication material merely because a backend can observe them.

An exact entry may select only context leaves available for that source. A broad entry may select a context leaf only
when that leaf is valid for every exact source still governed by the broad entry after exact entries are resolved.

### 5.7. Canonical Publication Material

Canonical Publication material preserves the semantic information needed to reproduce the outward boundary:

```text
Publication identity
Operation
bound Policy World
selected Result coordinates
resolved Failure source selectors
effective Failure entry for each exact source
selected common Failure coordinates
selected applicable-context leaves
```

Host declaration structure does not survive as Publication authority. A backend may lower broad selectors against the
finite direct authority set of the bound Contract version and replace them with the effective exact entries required by
Section 4.8.

The backend may encode this canonical material in any deterministic representation.

---

## 6. Contract and Implementation Boundary

### 6.1. Contract Meaning and Representation

Runtime carriers may represent the authoritative Core exit result or Publication material, but representation does not
grant outward authority.

The Contract owns the positive selections and the closed published surface. Backend representation may change as long as
sealed material never becomes available to Output Presentation as Contract material.

### 6.2. Publication Realization

A backend realizes an already-resolved Publication selection.

It may precompute or specialize the physical access path. It may also erase a runtime Publication object entirely. Those
choices cannot change the selected material or make host-language accessibility part of Publication meaning.

Publication is not a runtime interception mechanism or an access-control object. A wrapper, annotation, or reflection
scan cannot establish Publication meaning.

### 6.3. Handoff to Output Presentation

Publication and Output Presentation may be physically fused by an optimized backend, but their Contract authorities stay
distinct.

```text
semantic Publication
        -> closed published material

semantic Output Presentation
        -> closed external representation

optimized backend
        -> may realize both in one physical path
```

Fusion is valid only when the Publication boundary remains recoverable and the resulting representation uses no sealed
Core material.

### 6.4. External Adapters and Frameworks

External implementation receives material only after Publication and Output Presentation have completed the outward
Contract boundary. It does not gain direct Contract authority over sealed Core material.

Language access control remains an implementation concern rather than a substitute for Publication authority.

Once Output Presentation has handed material to an external adapter, failures in that adapter are outside this
Publication Contract.

### 6.5. Backend Architecture Constraints

This ADR does not choose a concrete Publication IR or runtime carrier.

Any backend must preserve two dependency boundaries:

```text
Publication -> arbitrary Core material          forbidden
Output Presentation -> sealed Core material     forbidden
```

Caching reuses compiler products but cannot create authority. A cached Publication product is valid only while the
canonical material on which it depends remains valid.

V1 may use ordinary generated JVM machinery. Later backends may specialize the same Contract without changing its
meaning.

---

## 7. Verification, Determinism, and Incremental Extensibility

The verifier must resolve every Result coordinate against the enclosing Operation's Result Fact. Unknown or duplicate
Result selections are invalid.

Every Failure entry must resolve exactly one admitted source selector under Section 4.7. A selector outside the current
Operation and Policy World is invalid. The selector's local parameter name cannot supply that meaning.

Duplicate source selectors are invalid. When a broad selector and one of its direct exact selectors coexist, the
verifier must compute the effective entries according to Section 4.8 before validating Failure material.

Every selected Failure coordinate must be valid for each exact source governed by its effective entry. The verifier does
not invent missing context or recover diagnostic observations as Failure material.

The selected Publication declaration must remain an uninstantiable one-level manifest. The outer constructor is empty.
`Result` appears at most once. Each Failure entry has one source selector and only admitted positive material
selections. No deeper Publication-entry nesting or executable authoring form may add Publication meaning.

Publication cannot target Output Presentation fields or external representation details. Such binding belongs to the
next boundary.

Independent Failures remain independent. Runtime arrival order and backend execution strategy cannot suppress a selected
Failure or change its published material.

Build strategy does not change Publication meaning. Clean and incremental compilation must agree. Cache reuse must agree
with recomputation. Parallel execution cannot change the selected material.

Incremental invalidation follows exact dependencies. A Result change invalidates Publication material that depends on
that Result surface. A Failure source or applicable-context change invalidates only Publication material that resolves
through it. A broad selector also depends on the direct authority membership of its selected Contract version.

Persisted Publication IR and compiler caches remain implementation material. Their storage versions are separate from
Contract Version.

---

## 8. Deferred Decisions

Output Presentation owns the next boundary. Its ADR must define how closed published material becomes a closed external
representation, including how independently published Failures are represented without recovering sealed Core material.

The package spelling of frontend support symbols is deferred. This includes the standard Failure coordinate handles and
the `RealizationFailure` marker. Their package names do not become Contract authority.

The scheduling relation between active processing boundary completion and establishment of the authoritative Core exit
result remains Whole-Machine work. This ADR requires only that Publication receive already-established exit material.

Diagnostic Evidence / Retention will define how diagnostic material is retained and observed while preserving the rule
that diagnostic availability does not grant outward authority.

After ADR-0058 is accepted, ADR-0047 and ADR-0049 must be revised where they still couple Publication directly to Output
Presentation coordinates or preserve older Publication mapping semantics.

These deferred decisions do not reopen the sealed-by-default rule, positive Publication selection, existing scope
binding, or the separation between Publication and Output Presentation.

---

## 9. Consequences

### Positive

Publication becomes a small outward authority rather than another semantic engine. Internal truth and public authority
remain separate.

The successful Result surface is explicit, and Failure publication reuses the source authorities and Failure shape that
already exist elsewhere in the Contract Machine. Exact Budget entries can be referenced through their nominal ADR-0051
source declarations without restating their coordinates.

A broad Failure selector keeps uniform publication concise. An exact entry can give one direct authority a different
complete surface without adding negative publication rules.

Output Presentation receives a closed material surface and does not need access to the Core to form the external
representation.

### Negative

The compiler must resolve source-selector types in the current Operation and Policy World before it can validate Failure
material. Broad selectors also require effective-entry resolution when exact entries coexist.

Positive selection is intentionally explicit. A source that has an exact Failure entry must state its complete outward
material in that entry.

Older ADRs that still describe Publication as direct output mapping require correction.

### Neutral

Publication remains user-authored because outward authority cannot be inferred from implementation reachability.

Kotlin or Java syntax is only the V1 frontend. Another frontend may replace it later without changing canonical
Publication meaning.