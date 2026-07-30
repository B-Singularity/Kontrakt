# ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis

## Status

Proposed

## Date

2026-07-26

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary

---

## 1. Context

ADR-0046 defines three axes for one explicit software machine.

```text
Contract Pipeline
    declares obligations, judgment authority, and declared results

Implementation Pipeline
    realizes those obligations and remains replaceable

State-Machine Pipeline
    declares the machine condition under which the selected State Transition is legal
```

The axes describe different authority. They may meet around one interaction, but none is another spelling of the others.

`What Contract Is` also separates the public interface surface, the Operation handle, and the Interaction Manifest.

```text
Interface Contract
    presents the public reliance surface
    and exposes selectable Operation handles

Operation
    identifies which interaction was selected

Interaction Manifest
    binds the exact closed contract presentations
    and required coordinates governing that interaction
```

The Operation is not the Contract Pipeline. It is not a parent contract that owns Input, Admission, Fact, Invariant,
State, Transition, or Publication. The Interaction Manifest binds those independent contract presentations flatly for
one selected interaction.

State, State Transition, and Explicit State Machine Manifest are among those independent presentations. Their purpose is
not to restate which Contract Pipeline position has completed. Their purpose is to declare one closed movement relation
and
judge whether a selected Transition is legal from the explicit current establishment condition.

Before the first State is established, that condition is `Unestablished`. Afterward it is `Established(State)` for one
exact declared State. `Unestablished` is not a State, is never inferred from null or missing material, and may appear
only
as the explicit source condition of a Transition that establishes the first State.

This distinction matters because several contract authorities may stop one interaction for different reasons.

```text
Admission
    may refuse presented material

Policy, Budget, Capacity, and Governance
    may refuse continuation under their own declared limits or active contract world

Invariant
    may refuse factual establishment

State-Machine Axis
    may refuse the selected movement when the Transition's declared source condition
    does not match the explicit current establishment condition

Publication
    may refuse an outward claim
```

These are not aliases for one generic validation step. Each owns a different question and must retain its own refusal
attribution.

A State-Machine movement refusal therefore does not mean that Admission refused the Input. Admission may have had no
opportunity to judge, or it may have admitted the Input correctly while the selected movement remained illegal.
Transition
applicability does not become an Admission clause merely because both can stop the same interaction.

State also does not automatically correspond one-to-one with Admission, Canonicalization, Lowering, Invariant, or
Publication. A label such as `Admitted` or `Lowered` is not State merely because that Contract position completed. A
condition is State only when declaring and establishing it changes which declared Transition may legally leave that
condition.

The movement axis therefore runs beside the Contract Pipeline without copying it. An interaction participates in that
axis only when its Interaction Manifest selects one declared Transition. The explicit current establishment condition is
then compared with that Transition's declared source condition. Contract positions still own their own judgments. The
State-Machine Axis owns only the selected movement's legality.

A Transition does not rerun Admission, Policy, Capacity, Governance, Invariant, Failure, or Publication. It declares one
permitted move between two declared machine conditions. The relevant judgment result may be necessary before movement,
but Transition does not recreate that result or hide it inside a generic Boolean wrapper.

The Explicit State Machine Manifest closes the State vocabulary and Transition relation so that no Operation body,
callback, backend, storage value, or generated path may invent another State or movement later.

This ADR decides that contract authority only. It does not decide the public Kotlin or Java authoring API, storage or
observation of the current establishment condition, recovery, synchronization, atomic commit, or backend realization.
Those decisions follow after the contract model is reviewed.

---

## 2. Problem

Without an explicit State Contract, a mutable field, enum value, object subtype, callback phase, pipeline index, or
implementation marker can quietly become movement authority. The machine then discovers its condition by inspecting what
the implementation happened to leave behind.

Without an explicit State Transition Contract, a stored-value rewrite, successful judgment, completed method, or later
pipeline position can be mistaken for permission to move. Execution progress begins to stand in for legal movement.

Without an Explicit State Machine Manifest, State and Transition remain separate declarations without one closed
movement surface. Nothing prevents an Operation body, generated dispatcher, callback, backend convention, or hidden
permission table from adding another move later.

The contract structure can also be collapsed in several wrong directions.

```text
Interface Contract
    may be reduced to a method bag

Operation
    may be mistaken for the whole Contract Pipeline

Interaction Manifest
    may be mistaken for an executable workflow

Contract judgment result
    may be mistaken for State establishment

Pipeline position
    may be mistaken for current State

State-Machine movement refusal
    may be misattributed as Admission, Policy, or generic validation refusal

Implementation progress
    may be mistaken for Transition permission
```

The stage-mirroring error is particularly dangerous.

```text
Admission completed
    -> invent Admitted State

Lowering completed
    -> invent Lowered State

Invariant completed
    -> invent Validated State
```

If those labels add no independent movement law, they merely repeat facts already expressed by declared results and
material. The machine now has two descriptions of the same authority. They will eventually disagree.

The opposite error is to remove State entirely and allow Admission, Policy, Capacity, Governance, Invariant, or ordinary
control flow to decide movement legality. That collapses movement authority into whichever contract or implementation
step happens to run nearby.

Kontrakt needs one closed movement model that answers a separate question:

> Does the selected Transition's declared source establishment condition exactly match the explicit current
> establishment condition?

That question must remain distinct from:

```text
Is the presented Input admissible?
Is the active Policy satisfied?
Is Budget available?
Is Capacity available?
Does Governance admit this contract world?
May candidate material become Fact?
May an outward claim be published?
```

Several of those authorities may apply to one interaction. They may all permit continuation, or one may stop it. The
machine must preserve which authority decided what.

---

## 3. Decision Drivers

Kontrakt must preserve the three axes decided by ADR-0046. Contract judgment, movement authority, and physical
realization may be linked, but none may become another name for the others.

Interface Contract, Operation, and Interaction Manifest must remain separate.

```text
Interface Contract
    owns the public reliance surface

Operation
    is the selectable interaction handle

Interaction Manifest
    binds the exact contract authorities governing that interaction
```

State and Transition meaning is not owned by the Operation. The Interaction Manifest selects one exact Transition and
its Machine, or declares explicit State-Machine absence, without redefining either.

State must remain a finite vocabulary of actual machine conditions rather than a copy of every value, judgment result,
contract position, callback, or observed detail in the system.

A condition belongs to the State vocabulary only when its establishment independently changes which declared
Transitions may legally leave it.

The State-Machine Axis must remain an independent movement stop authority. It may stop a movement-carrying interaction
when its selected Transition's source establishment condition does not match the explicit current establishment
condition,
without pretending that Admission, Policy, Budget, Capacity, Governance, Invariant, or Publication made that decision.

Admission must continue to judge presented material. It may not absorb Transition applicability merely because a
source-incompatible Transition also prevents the interaction from continuing.

The movement model must not collapse all continuation decisions into one generic Boolean such as `canContinue`.
Contract authority and refusal attribution must remain typed by the contract that owns the question.

Transitions must be explicit, one-way, and closed. No move may arise from host behavior, inheritance, structural
similarity, object mutation, result-name matching, pipeline position, or backend convention.

One declared source-establishment-condition-to-target-State move has one Transition authority. Business cause,
Operation provenance, diagnostic reason, or factual explanation does not split one movement into several aliases. Those
meanings remain in their own contracts and Facts.

A Transition must not require an artificial `result`, `enabledBy`, or `on` wrapper merely to repeat that another
declared
judgment succeeded. The prior contract result remains owned by its contract.

Implicit initialization and topological exhaustion are different from explicit movement authority. The Machine Contract
must not choose a first State through declaration order, invocation order, backend defaulting, storage absence, or
another
hidden rule. The first State is established only by an exact Transition whose declared source condition is
`Unestablished`.
Zero-outgoing States may be derived from the closed relation, but topology must not be silently promoted into successful
or intended completion.

V1 remains flat. One selected Machine governs one closed State surface, and exactly one current establishment condition
exists at a movement judgment boundary: either `Unestablished` or `Established(State)` for one State of that surface. An
interaction either selects one Transition on that surface or declares explicit State-Machine absence. Coordination among
independent State surfaces requires a later explicit coordination contract.

Backend representation, current-establishment-condition acquisition, persistence, synchronization, recovery, and
commit strategy are not contract semantics and remain deferred.

---

## 4. Decision

Kontrakt will treat `State`, `State Transition`, and `Explicit State Machine Manifest` as ordinary one-dimensional
contract presentations on the State-Machine Axis.

The contract structure is:

```text
Interface Contract
    presents one public reliance surface
    and exposes selectable Operation handles

Operation
    selects one interaction

Interaction Manifest
    flatly binds the applicable Contract Pipeline material
    and the applicable State-Machine material
```

The Operation does not own the State Machine. The Interaction Manifest binds a movement-carrying Operation to one exact
Machine surface and one exact Transition, or declares explicit State-Machine absence for an Operation that carries no
State movement.

The State-Machine Axis governs one form of legality:

```text
movement legality
    whether the selected Transition belongs to the selected Machine
    and whether its declared source establishment condition exactly matches
    the explicit current establishment condition
```

A source-incompatible Transition may stop the movement-carrying interaction at its declared movement boundary. An
undeclared Transition or a Transition outside the selected Machine is rejected as a definition-time contract failure.
This statement defines contract authority, not a fixed runtime schedule.

The State-Machine Axis does not judge Input admissibility. Admission does not judge Transition applicability. Policy,
Budget, Capacity, Governance, Invariant, and Publication retain their own questions. One interaction continues only when
every applicable authority has produced the result required at its own judgment boundary.

Conceptually:

```text
selected Operation
    -> Interaction Manifest resolves applicable contract authorities

State-Machine Axis
    -> permits or refuses the selected declared movement

Contract Pipeline
    -> each applicable contract judges its own obligation

one authority refuses
    -> the interaction stops under that authority's declared result

all required authorities permit
    -> the interaction may continue to its next declared boundary
```

This is not a single linear validator. The diagram states dependency and authority only.

The canonical State Machine contains:

```text
one Machine identity
one closed State vocabulary
one closed one-way Transition relation
one explicit establishment-condition domain
    Unestablished
    Established(State)
Operation-to-Transition selection material from Interaction Manifests
movement-legality law
current-establishment-condition judgment law
```

A State corresponding to a Contract Pipeline position is not inferred. It is legal only if it has an independent machine
meaning and changes the legal outgoing Transition relation.

The Machine does not calculate another contract's question again. It receives exact declared State-Machine material and
judges only machine-condition and movement legality.

No separately authored generic continuation result is introduced. A State-Machine movement refusal, Admission refusal,
Policy refusal, Capacity refusal, Governance refusal, Invariant refusal, and Publication refusal remain distinct.

The Machine Contract does not declare or infer one global initial State. First establishment remains inside the same
closed Transition relation: an exact Transition may declare `Unestablished` as its source establishment condition and
one
exact declared State as its target. Different Operations may select different explicit first Transitions. No declaration
order, invocation order, backend choice, or hidden default may select among them.

The Machine Contract does not author terminal States. Kontrakt derives zero-outgoing States from the complete Transition
relation. That derived topology does not mean success, failure, acceptance, publication, or intended completion.

V1 admits either one selected explicit Machine surface with one selected Transition or explicit State-Machine absence
for an interaction. An Operation that selects no Transition does not participate in the State-Machine Axis.

---

## 5. State-Machine Axis

### 5.1. Axis Authority

The State-Machine Axis answers these questions:

```text
Which explicit current establishment condition holds at this movement boundary?

Does the selected Transition belong to the selected Machine?

Does the Transition's source establishment condition exactly match
that current establishment condition?

If the movement is permitted and every required contract result exists,
which declared target State may become established?
```

The axis owns selected-movement legality, movement authorization, and target-State establishment authority.

It does not decide whether presented material is admissible, whether a Policy permits an option, whether Budget or
Capacity remains, whether Governance selected the applicable contract world, whether candidate material satisfies an
Invariant, or whether an outward claim may be published.

A State-Machine movement refusal may nevertheless stop the same interaction. Shared ability to stop does not create
shared judgment authority.

```text
State-Machine movement refusal
    the selected Transition's declared source establishment condition
    does not match the explicit current establishment condition

Admission refusal
    presented material may not enter under the Admission Contract

Policy refusal
    the declared policy does not permit the selected choice

Capacity refusal
    the declared capacity wall does not admit the work

Governance refusal
    the active contract world does not admit the interaction

Invariant refusal
    candidate material may not become Fact

Publication refusal
    the proposed outward claim may not be published
```

### 5.2. Relation to the Contract Pipeline

The Contract Pipeline and State-Machine Axis govern one interaction from different directions.

```text
Contract Pipeline
    declares what question is judged
    and what result that judgment may establish

State-Machine Axis
    declares under which explicit establishment condition
    the selected Transition remains legal
```

State is not another Contract Pipeline stage. It is not inserted as:

```text
Input -> Admission -> State -> Lowering -> Invariant
```

Nor does every Contract Pipeline position receive a matching State.

```text
Admission -> Admitted
Lowering -> Lowered
Invariant -> Validated
```

is invalid when the right-hand labels merely restate the left-hand results.

The State-Machine Axis does not decide whether Admission is the next legal Contract Pipeline position. It does not
arrange
or schedule Operations or Contract judgments. It participates only when the Interaction Manifest selects a Transition.

```text
selected Transition source establishment condition
matches the explicit current establishment condition
    -> State-Machine movement authority

Admission accepts or refuses the presented Input
    -> Admission authority
```

A selected Transition may depend on declared results produced by Admission, Invariant, Policy, Capacity, Governance, or
another contract. It uses the existence of those results; it does not recreate their judgments.

The exact placement of the movement judgment boundary is explicit interaction material. It is not inferred from Contract
Pipeline order, method order, or implementation control flow.

### 5.3. Independent Continuation Authority

An interaction does not continue merely because one judge returned success.

```text
Admission accepted
    does not imply the selected Transition is legal from the explicit current establishment condition

The selected Transition is legal from the explicit current establishment condition
    does not imply the Input is admissible

Invariant accepted candidate material
    does not imply Publication is authorized

Policy permitted a choice
    does not imply Capacity is available
```

Every applicable authority retains its own declared result.

Kontrakt must not reduce these results to:

```text
canContinue: Boolean
```

Such a reduction removes contract identity, refusal attribution, and the meaning of the decision.

The Interaction Manifest binds which authorities apply to one interaction. It does not create a new master judge above
them.

### 5.4. Interface, Operation, and Interaction Manifest

The Interface Contract is the software-visible public reliance surface. It declares which Operation handles the outside
may select and what public presentation may be relied upon.

An Operation is only the interaction selector at that surface. It is not the pipeline and does not privately own State,
Transition, Invariant, Fact, or Publication law.

The Interaction Manifest binds one selected Operation to the exact closed contract presentations and coordinates that
govern it.

```text
Interface Contract
    exposes Operation handle

Operation selected
    identifies the interaction

Interaction Manifest
    binds one applicable Transition Contract and Explicit State Machine Manifest
    or declares explicit State-Machine absence
    binds the other applicable contract presentations
```

The manifest does not repeat State or Transition meaning. It selects exact handles and declares participation.

Several Operations may select Transitions leaving the same established State. Several Operations may also select
different explicit Transitions whose source condition is `Unestablished`, thereby declaring different deterministic
first
establishment paths. One Operation handle may select only the exact Transition bound by its Interaction Manifest for the
applicable contract world.

```text
Transition selected
    -> State-Machine Axis participates

no Transition selected
    -> explicit State-Machine absence
    -> no State-Machine judgment
```

The State-Machine Axis does not introduce a separate Operation-permission matrix. It does not decide the placement,
ordering, or general executability of Operations that carry no State movement.

### 5.5. Separation from the Implementation Pipeline

The Implementation Pipeline realizes declared judgments and permitted movement. It carries no authority to invent
either.

A callback phase, program counter, transaction status, stored value, thread state, workflow node, object field, method
return, or generated branch does not become State by itself.

An implementation may not treat successful execution as proof that the selected Transition was legal. It may not
convert a generic validation result into movement authority. It may not change refusal attribution.

This ADR does not decide how State is represented, observed, stored, synchronized, recovered, or committed.

### 5.6. Three-Axis Linkage

The three axes meet without becoming one axis.

```text
Contract Pipeline
    asks and answers each declared contract question

State-Machine Axis
    permits or refuses the selected declared movement
    under the explicit current establishment condition

Implementation Pipeline
    physically realizes the permitted work
```

Generated or handwritten assembly may later connect these axes. That assembly is not contract authority and is outside
the decision made here.

---

## 6. State Contract

### 6.1. Meaning

State is explicitly declared contract material for selected-movement legality.

```text
State Contract
    the explicitly declared contract surface that defines
    the machine conditions from which declared Transitions
    may or may not legally depart
```

A State is not a completed Contract Pipeline position. It is not a verdict, Fact, progress marker, or diagnostic label.

A condition qualifies as State only when removing it would make the legal outgoing Transition relation impossible to
determine completely.

```text
same legal outgoing Transitions before and after the label
    -> not State

different legal outgoing Transitions under the condition
    -> State
```

Conditions such as `Open`, `Frozen`, `Waiting`, `Running`, `Failed`, or `Closed` may be States when they perform that
role.
Their names do not make them State. Their declared movement authority does.

A pipeline-adjacent name such as `Admitted`, `Lowered`, `Established`, or `Published` is not prohibited by spelling, but
it receives no State authority from the corresponding Contract result. It must satisfy the same outgoing-Transition
test as every other candidate State.

### 6.2. Closed and Flat Vocabulary

A State Contract declares a finite set of directly named conditions. Each State is unique within the selected Machine,
and the vocabulary is closed before movement judgment.

State inheritance, nested State trees, recursive composition, open subtype discovery, and structural State inference are
forbidden. Similar names do not create shared meaning or movement.

Host enum ordinal, object identity, allocation history, package placement, source-file name, and backend representation
do
not participate in State meaning.

### 6.3. State Contract Material

A State declaration carries only the nominal material needed to identify one machine condition. It contains no factual
payload, movement method, transition predicate, mutable current-State field, callback, lookup, validation rule, or
backend encoding.

The exact Kotlin, Java, or IDL authoring surface is deferred. Any later authoring model must preserve the following law:

```text
State source material
    names one exact declared machine condition
    and carries no behavior or implementation authority
```

### 6.4. State and Fact

Fact answers what explicit immutable information is true. State answers from which declared machine condition a selected
Transition may legally depart.

The same interaction may use factual material that explains why a State was established or why a move was requested.
That explanation does not belong in the State name or Transition identity.

```text
Frozen
    State condition

fraud detected
user requested suspension
policy version
operator identity
    Fact, Policy, or Diagnostic material
```

Fact does not secretly carry State authority. State does not own factual payload.

The existence of admitted material, lowered candidate material, established Fact, or publication material does not by
itself establish a State. If that material already determines the legal outgoing Transition relation completely, a
duplicate State label is forbidden.

### 6.5. Current Establishment Condition

Exactly one explicit current establishment condition exists for one selected flat Machine surface at a movement judgment
boundary.

```text
Unestablished

Established(State)
```

`Unestablished` is not a State and is not a member of the Machine's State vocabulary. It is the unique origin condition
before the first State has been established. `Established(State)` names one exact declared State of the selected
Machine.
The current establishment condition is never nullable and is never inferred from omission, a failed lookup, missing
storage material, declaration order, invocation order, or backend defaulting. `Unestablished` must therefore be
positive,
canonical contract material at the judgment boundary rather than a convenient interpretation of absence.

Only an explicitly permitted Transition may replace the current establishment condition. A Transition whose source is
`Unestablished` establishes the first declared target State. A Transition whose source is `Established(Source)` replaces
that established State with its declared target State. A returned value, completed method, callback, stored label, or
successful Contract judgment does not establish State by itself.

`Unestablished` is origin-only. No Transition may target it, and once one State has been established for the same
Machine
occurrence, that occurrence may never return to `Unestablished` through State movement.

This ADR does not decide how the current establishment condition is physically represented, acquired, synchronized, or
committed. Any later realization must positively distinguish `Unestablished` from unavailable, corrupt, inconsistent, or
missing implementation material.

### 6.6. State-Machine Movement Refusal

A State-Machine movement refusal is a declared result of the State-Machine Axis.

It means:

```text
the selected Transition's declared source establishment condition
does not match the explicit current establishment condition
```

It does not mean:

```text
the Input was inadmissible
Policy refused
Budget was exhausted
Capacity was unavailable
Governance rejected the contract world
Invariant failed
Publication was refused
```

Those results remain owned by their respective contracts.

Diagnostic Evidence may later explain the State-Machine movement refusal. Failure presentation and retention remain
separate decisions.

---

## 7. State Transition Contract

### 7.1. One-Way Movement

A State Transition Contract declares a permitted one-way move from one explicit source establishment condition to one
different declared target State on the same flat Machine surface.

The source establishment condition is exactly one of:

```text
Unestablished

Established(Source State)
```

The target always establishes one exact declared State. `Unestablished` is never a target.

A field rewrite, successful judgment, completed method, callback, exception, log marker, pipeline advance, or
implementation step does not create a Transition.

### 7.2. Transition Material

Each Transition has one unique contract handle, one source establishment condition, and one exact target State.

```text
Transition handle
    source establishment condition
        Unestablished
        or Established(Source State)
    target State
```

The handle allows an Interaction Manifest to select the exact movement without repeating its endpoints. It is not a
business cause, event history, implementation command, Operation result, or generic continuation token.

For one Machine and applicable contract world, one exact source-condition-to-target movement is declared once. Different
business causes that request the same movement do not create aliases.

```text
FraudFreeze        Open -> Frozen
UserSuspension     Open -> Frozen
```

is rejected when the only difference is provenance. The cause remains Fact, Policy, Failure, or Diagnostic material.

A Transition contains no Admission rule, Invariant predicate, Policy expression, Capacity judgment, Governance rule,
Failure selection, result wrapper, callback, guard body, or implementation action.

### 7.3. Closed Transition Relation

The Transition set is complete and closed. A move absent from the set is illegal.

Duplicate handles, duplicate aliases for the same source-condition-to-target move, undeclared State references,
`Unestablished` targets, same-established-State self-transitions, and ambiguous movement selection are rejected.

A Transition selected from another Machine does not become legal because its endpoint names happen to match.

### 7.4. No Regression or Cycle

Transitions are condition-changing forward moves on one State surface. A Transition sourced from `Unestablished`
establishes the first State. A Transition sourced from `Established(State)` changes one established State to a different
State. V1 rejects both a same-State self-transition and a Transition graph that returns to an earlier established State.
An Operation that preserves State selects no Transition and declares explicit State-Machine absence. Correction, retry,
restart, and repetition must enter through a new governed run, epoch, or separately declared contract surface.

`Unestablished` is outside the State graph and may appear only as an origin source. It does not create a cycle, may not
be
re-entered, and may not be reconstructed after a State has been established.

Branching remains legal. One State may have several outgoing Transitions when the Machine declares genuinely different
target conditions.

### 7.5. Transition Judgment

Transition judgment receives exact declared movement material and asks only:

```text
Does this Transition belong to the selected Machine?
Does its source establishment condition exactly match the explicit current establishment condition?
Is its target a declared State of the same surface?
```

It does not rerun the contract judgments that produced material required before movement.

```text
Admission result exists
Policy result exists
Invariant result exists
    -> those results retain their own authority

selected Transition source matches the current establishment condition
    -> Transition authority
```

No generic `result`, `enabledBy`, `on`, or Boolean wrapper is introduced solely to bridge these authorities.

Permission to move does not establish the target State by itself. Target establishment requires the complete declared
contract outcome for that movement boundary. The exact realization protocol remains deferred.

A backend may lower the closed Transition relation into generated guards, indexes, tables, bitsets, or specialized
branches. Such material is derived realization only. It may not become an authored Operation-permission surface, add
State-dependent restrictions to Operations with explicit State-Machine absence, or acquire contract authority
independent of the declared Transition source and target.

---

## 8. Explicit State Machine Manifest

### 8.1. Purpose

The Explicit State Machine Manifest names one complete flat movement surface. It does not become the Contract Pipeline,
a workflow engine, or a parent contract above State and Transition.

State declarations name the conditions the Machine may occupy. Transition declarations name the permitted one-way moves.
The Machine Manifest closes those declarations under one exact authority so that no interaction or implementation path
may reconstruct or extend the movement surface later.

### 8.2. One Surface

Each selected Machine governs one flat State surface, and exactly one explicit current establishment condition exists at
a movement judgment boundary: `Unestablished` or `Established(State)` for one State of that surface.

V1 does not combine several independent movement authorities into a Cartesian-product State vocabulary. It does not
compose Machines recursively or infer cross-surface coordination.

The Interface Contract remains the public reliance surface; the Machine Manifest remains the closed movement surface.
Neither becomes the other.

Several Interaction Manifests exposed by the same Interface Contract may bind their Operation handles to different
Transitions on the same Machine surface. An Operation that carries no State movement declares State-Machine absence
rather
than remaining attached to the Machine through a separate permission rule.

### 8.3. No Global Initial-State Default

The Machine Manifest does not declare or infer one global initial State.

The first State is selected by the exact Transition chosen for the interaction. Such a Transition declares
`Unestablished` as its source establishment condition and one exact declared State as its target.

```text
CreateDraft
    selects UnestablishedToDraft

ImportExisting
    selects UnestablishedToActive
```

Both paths may exist when their Operations select exact, distinct Transitions. The backend does not choose between them.
The first declared State, the first invoked ordinary Transition, declaration order, storage absence, and runtime
defaulting
have no initial-State authority.

An ordinary Transition whose source is `Established(State)` is illegal while the current establishment condition is
`Unestablished`. Conversely, a Transition sourced from `Unestablished` is illegal after any State has been established.

### 8.4. Derived Sink Topology

The Machine Manifest does not author terminal States.

Kontrakt derives the zero-outgoing State set from the complete Transition relation.

```text
sink State
    = declared State with no outgoing Transition in this Machine
```

This set is topological information. It does not mean success, failure, acceptance, publication, or intended completion.

### 8.5. Machine Definition Validation

Kontrakt rejects a Machine Contract when:

- a State symbol is ambiguous within the selected Machine;
- a Transition handle is duplicated;
- a Transition refers to a State outside the selected Machine;
- a Transition targets `Unestablished`;
- two declarations alias the same source-condition-to-target move;
- the established-State relation contains a cycle or backward return within the same movement surface;
- an Interaction Manifest selects a Transition outside the selected Machine;
- one Operation resolves to more than one Transition for the same applicable contract world;
- a Transition sourced from `Established(State)` declares that same State as its target;
- a source condition is omitted, nullable, inferred, or represented by storage absence;
- source material relies on behavior, inheritance, runtime lookup, package scanning, file identity, or hidden defaults.

These are definition-time contract failures. They are not runtime State-Machine movement refusals.

### 8.6. Explicit State-Machine Participation and Absence

State-Machine participation is explicit for every interaction.

The valid semantic cases are:

```text
Movement-participating
    the Interaction Manifest selects one exact Machine
    and one exact condition-changing Transition

State-Machine absent
    the Operation selects no Transition
    and the Interaction Manifest declares explicit State-Machine absence
```

An Operation that selects no Transition is not implicitly State-governed. The State-Machine Axis does not decide whether
that Operation may be placed, ordered, or executed under particular States. Any restriction on such an Operation must
belong to the contract authority that actually owns the reason; it may not be hidden in a generic State permission
table.

Omission, nullable handles, empty lookup results, inferred applicability, and hidden defaults do not express absence.

---

## 9. Authoring and Processing Boundary

### 9.1. Interaction Manifest Selection

The Interface Contract exposes Operation handles. For each selectable Operation, one flat Interaction Manifest binds the
closed contract presentations and coordinates governing that interaction.

The State-Machine part of that binding identifies:

```text
exact Machine handle and exact Transition handle
or
explicit State-Machine absence
```

The manifest does not declare State or Transition bodies. It does not repeat Transition endpoints. It does not hide
Transition selection in Input values, method names, return types, exceptions, or implementation branches.

The same Operation handle must not resolve to different hidden State-Machine contracts through overload, receiver type,
object subtype, runtime registration, or backend choice.

### 9.2. Public Authoring Shape Deferred

This ADR does not select the final Kotlin, Java, or IDL authoring API.

The later authoring design must preserve these semantic requirements:

```text
State
    one exact nominal condition handle

Transition
    one exact unique handle
    one exact source establishment condition
        Unestablished
        or Established(Source State)
    one exact target State

Machine
    one exact flat movement surface

Interaction Manifest
    one exact Operation selection
    one exact Machine and Transition selection
    or explicit State-Machine absence
```

No proposed syntax in earlier drafts is retained as a decision by this ADR.

### 9.3. Source Declaration Law

Any accepted frontend must remain declaration-only contract material.

It must not obtain authority from:

- executable bodies;
- runtime instances;
- mutable fields;
- callbacks or lambdas;
- annotation behavior;
- inheritance or subtype discovery;
- package or file scanning;
- object identity;
- reflection order;
- runtime registries;
- storage layout;
- hidden defaults.

The frontend may carry exact names, an explicit `Unestablished` source marker or exact source-State reference, one exact
target-State reference, exact Machine and Transition selection, explicit State-Machine absence, and other closed
coordinates required by the selected authoring model.

### 9.4. Canonical Contract Material

Whatever source form is later selected, lowering must remove host-language shape and retain only canonical contract
material.

Conceptually:

```text
State declarations
    -> closed canonical State vocabulary

Transition declarations
    -> unique handle / source establishment condition / target-State relation

Machine declaration
    -> exact Machine identity and closed membership

Interaction Manifest binding
    -> exact Operation participation
    -> exact Machine and Transition selection
    -> or explicit State-Machine absence
```

This section defines the semantic result of lowering. It does not decide backend representation or runtime ABI.

---

## 10. Interaction and Movement Judgment

### 10.1. Contract Inputs

Contract-level State-Machine judgment requires:

```text
selected Interaction Manifest
selected Machine
explicit current establishment condition
selected Operation
selected Transition
applicable contract world
```

When the Interaction Manifest declares explicit State-Machine absence, no State-Machine judgment is formed.

It does not receive an arbitrary Operation object, callback, repository, log, host exception, storage row, generic
Boolean continuation result, or implementation phase.

### 10.2. State-Machine Applicability

The Interaction Manifest declares exactly one of two participation cases:

```text
Movement-participating
    one exact Machine and one exact Transition are selected

State-Machine absent
    no Transition is selected
    no State-Machine judgment exists
```

Kontrakt does not derive or author a separate State-to-Operation permission relation. The selected Transition's explicit
source establishment condition is the only State applicability law for a movement-carrying Operation.

### 10.3. Movement-Legality Judgment

For a movement-participating interaction, the movement question is:

```text
Does the selected Transition belong to the Machine,
and does its source establishment condition exactly match
the explicit current establishment condition?
```

An Operation that preserves State selects no Transition and declares explicit State-Machine absence.

### 10.4. Continuation Across Contract Authorities

One interaction may require several independent results.

```text
Admission admits the Input
Policy permits the choice
Budget and Capacity admit consumption
Governance admits the contract world
Invariant permits Fact establishment
State-Machine Axis permits the selected Transition
Publication permits the outward claim
```

The exact applicable set is declared by the Interaction Manifest and the closed contract presentations it binds.

No authority becomes the master judge of the others. The interaction continues only when the results required at the
next declared boundary exist.

The order in which an implementation evaluates independent judgments is not decided here. Any later realization must
preserve dependencies, authority, refusal attribution, and externally observable contract results.

### 10.5. Target-State Establishment

A permitted Transition identifies the only target State that may become established for that movement.

The target State is not established merely because:

- an Operation body completed;
- a method returned;
- Admission accepted;
- Invariant held;
- Policy permitted the work;
- a backend wrote a value;
- an implementation advanced to a later phase.

The target may become authoritative only when the complete declared movement outcome exists. For a Transition sourced
from `Unestablished`, this is the first State establishment. For a Transition sourced from `Established(State)`, this is
ordinary State replacement. The physical establishment and indivisibility protocol is deferred.

### 10.6. Refusal Categories

This ADR preserves at least these distinct categories:

```text
Machine definition rejection
current-establishment-condition acquisition failure
State-Machine movement refusal
Admission refusal
Policy / Budget / Capacity / Governance refusal
Invariant refusal
Publication refusal
physical realization failure
```

The complete Failure Contract, public presentation, Diagnostic Evidence, and Diagnostic Retention rules remain deferred.

---

## 11. Contract and Implementation Boundary

### 11.1. Contract Decisions Made Here

This ADR decides:

- the meaning of State as declared source-condition material for selected-movement legality;
- the meaning of Transition as one explicit source-establishment-condition-to-target-State permission;
- the meaning of one flat Explicit State Machine Manifest;
- the separation of Interface Contract, Operation, and Interaction Manifest;
- the State-Machine Axis as an independent selected-movement stop authority;
- the separation of State-Machine movement refusal from Admission and other contract refusals;
- explicit movement participation or State-Machine absence for each interaction;
- the prohibition on stage-mirroring State authority;
- the prohibition on hidden or implementation-derived movement.

### 11.2. Implementation Decisions Not Made Here

This ADR does not decide:

- how many runtime realizations of one Machine exist;
- how a realization is identified;
- where the current establishment condition is stored;
- how the explicit current establishment condition is observed or acquired;
- how State is synchronized;
- how concurrent movements are serialized;
- how restart or recovery reconstructs authority;
- how State movement joins factual change or publication;
- whether a backend uses memory, database rows, WAL, CAS, transactions, actors, generated tables, or remote
  coordination;
- the realization ABI;
- the public user API.

None of those mechanisms may later redefine State, `Unestablished`, Transition, Machine membership, Transition
applicability, or refusal attribution. Missing, unavailable, corrupt, or inconsistent implementation material may not be
silently converted into `Unestablished`.

### 11.3. No Implementation Authority

A runtime object, session, workflow execution, process instance, database row, actor, method call, callback, or
generated
branch may realize a Machine, but none is contract authority merely because an implementation uses it.

The contract remains valid if the realization mechanism is replaced while the declared authority remains unchanged.

---

## 12. Determinism and Verification

The same selected Interaction Manifest, Machine, explicit current establishment condition, selected Operation, selected
Transition, and applicable contract world must produce the same State-Machine judgment. When the current condition is
`Unestablished`, the exact selected Transition determines one exact first target State or produces a source mismatch.
The
backend may not search among first Transitions, choose a target, or resolve ambiguity at runtime. Explicit State-Machine
absence must produce no State-Machine judgment regardless of backend state.

Canonical State and Transition ordering may not depend on source acquisition order, host enum ordinal, reflection order,
allocation history, hash iteration, file order, or backend layout.

Verification may derive:

- the complete State vocabulary;
- the complete Transition relation;
- incoming and outgoing movement indexes;
- zero-outgoing sink topology;
- legal Transition sets per explicit establishment condition;
- all explicit first-State establishment paths sourced from `Unestablished`;
- one exact first target per Operation and applicable contract world;
- Operation-to-Transition bindings;
- interactions with explicit State-Machine absence;
- unreachable or contradictory manifest selections;
- stage-mirroring candidates whose State label adds no distinct outgoing Transition relation.

Optimization and backend realization are deferred. Any later optimization must remain equivalent to the canonical
contract judgment and may not change refusal attribution.

---

## 13. Complete Three-Axis Contract Model

The Contract Pipeline exposes declared judgment authority. The State-Machine Axis exposes declared selected-movement
legality. The Implementation Pipeline later realizes both.

They are neither one linear list nor three unrelated systems.

```text
Interface Contract
    exposes selectable Operation handles

Interaction Manifest
    binds one selected interaction to its exact authorities

Contract Pipeline
    each contract judges its own obligation

State-Machine Axis
    judges whether the selected Transition is legal
    under the explicit current establishment condition

Implementation Pipeline
    later realizes the permitted work without acquiring authority
```

The conceptual contract relation is:

```text
Operation selected
    -> Interaction Manifest identifies applicable authorities

State-Machine judgment
    -> selected movement permitted / refused

Admission, Policy, Budget, Capacity, Governance,
Invariant, Publication, and other applicable judgments
    -> each produces its own declared result

all results required for the declared boundary exist
    -> interaction may continue

one required authority refuses
    -> interaction stops under that authority
```

This does not require one fixed evaluation sequence. It requires exact authority, dependencies, and attribution.

The key separations are:

```text
Operation selection
    != Interaction Manifest

Interaction Manifest
    != executable workflow

Admission success
    != Transition applicability

Operation selection
    != permission to invent a State movement

Contract judgment success
    != implementation completion

implementation completion
    != contract authority
```

When State-Machine participation is explicitly absent, no State-Machine judgment or movement applies to that
interaction.
When one Transition is selected, hidden implementation progress or backend permission material cannot replace it.

---

## 14. Deferred Decisions

This ADR does not define the complete Failure Contract, Diagnostic Evidence, Diagnostic Retention, Version, Policy,
Budget, Capacity, Governance, or semantic Completion Contract.

It also defers:

- the final Kotlin, Java, and IDL user authoring API;
- the exact spelling of Machine-and-Transition selection or State-Machine absence in an Interaction Manifest;
- whether Interface Contract V1 permits more than one independent Machine surface and, if so, how that coordination is
  made explicit without recursive composition;
- the exact public authoring syntax for an `Unestablished` Transition source and its generated host representation;
- the physical protocol that positively distinguishes `Unestablished` from unavailable, corrupt, inconsistent, or
  missing realization material;
- the exact declared dependency relation between prior contract results and a selected Transition;
- whether another Contract may explicitly consume established State as factual judgment material without transferring
  movement authority to that Contract;
- the generated or supplied realization ABI;
- current-establishment-condition storage, observation, synchronization, recovery, and commit protocols;
- coordination among several independent State surfaces;
- governed retry, restart, repetition, and epoch models;
- public presentation of State-Machine movement refusal.

These omissions do not reopen the authority law decided here. State and Transition remain independent contract
presentations that together determine selected-movement legality. `Unestablished` remains an explicit origin source
condition rather than a State, null, missing value, or backend default. Admission, Policy, Budget, Capacity, Governance,
Invariant, and Publication retain separate judgment authority. Operation remains a selectable handle, and the
Interaction
Manifest remains the flat binding surface.

---

## 15. Consequences

State, State Transition, and Explicit State Machine Manifest now have a contract-level V1 model without returning
authority to runtime objects, pipeline progress, or generic validation.

Interface Contract, Operation, and Interaction Manifest remain distinct. The Interface presents the public reliance
surface. The Operation identifies the selected interaction. The Interaction Manifest binds the exact closed contract
presentations governing it.

The State-Machine Axis becomes an independent selected-movement stop authority. It may refuse a movement-carrying
interaction when the selected Transition's source establishment condition does not match the explicit current
establishment condition. Selection of a Transition outside the Machine is rejected as a definition-time contract
failure.
Neither result is renamed as Admission, Policy, Capacity, Governance, Invariant, or Publication refusal.

Admission continues to judge presented Input. Policy, Budget, Capacity, and Governance continue to judge their declared
rules and walls. Invariant continues to judge factual establishment. Publication continues to judge outward claims. The
State-Machine Axis judges only the exact selected Transition. No generic `canContinue` or Operation-permission result
replaces those contracts.

Contract Pipeline positions no longer receive automatic matching States. `Admitted`, `Lowered`, `Established`, or
`Published` receive no State authority merely because a corresponding judgment completed. A candidate condition must
change the legal outgoing Transition relation independently.

An Operation that changes State selects one exact Transition. An Operation that preserves State selects no Transition
and declares explicit State-Machine absence. Kontrakt does not create a State-to-Operation permission matrix for such
Operations and does not treat State preservation as a self-transition.

Transition meaning remains one unique handle with one explicit source establishment condition and one target State under
one Machine. Business cause, Operation provenance, and explanation remain outside Transition identity.

A global initial State is not authored by the Machine Contract. The first State is established by an exact selected
Transition sourced from `Unestablished`. Multiple deterministic first paths may exist through different exact
Transitions,
but no hidden rule may select one. Once established, the same Machine occurrence cannot return to `Unestablished`.
Terminal State is not authored either; zero-outgoing States are derived sink topology without semantic completion.

The public authoring API and backend realization are intentionally not decided. Reviewing this contract model comes
first. A later backend may derive guards, indexes, tables, bitsets, or specialized branches from the closed Transition
relation, but that derived material may not become a second permission source. Later syntax, generation, storage,
synchronization, recovery, and optimization must preserve this authority without adding a second source of truth.