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

ADR-0046 separates the interface axis, contract axis, and state-machine axis. ADR-0047 identifies `State`, `State
Transition`, and `Explicit State Machine Manifest` as one-dimensional contract presentations selected through operation
manifest slots. ADR-0048 and ADR-0049 then close the main factual path from external Input to established Fact and from
an established Operation result to outward presentation.

The movement axis remains open.

Kontrakt treats software as a machine that performs real work through an explicit pipeline rather than hiding the
whole process behind one function. A State declares an actual condition that the machine can occupy while that work
proceeds. `Open`, `Frozen`, `Waiting`, `Running`, `Success`, `Failed`, and `Closed` may all be States when the condition
changes which declared movement is legal next.

A State is not merely another factual value carried through the pipeline. A Transition does not judge factual integrity.
It permits one declared move between two machine conditions after the contract result on which that move depends already
exists. The explicit State Machine names the whole movement surface so that no implementation path may invent another
condition or movement later.

These contracts do not need operation-local IDL bodies. Lowering and Publication received that narrow exception because
they declare exact coordinate relations across two different surfaces. State material declares a finite vocabulary and
finite relations inside one movement surface. It remains ordinary one-dimensional source material: the operation
manifest selects a handle, a data-only host declaration carries candidate material, and Kontrakt lowers that material
into its own canonical form.

Existing Kontrakt code already demonstrates several useful separations: a closed condition vocabulary is distinct from
movement legality; movement legality is distinct from physical state storage; factual payload is distinct from movement
condition; and independent movement authorities should not be collapsed into one product of every possible label. This
ADR adopts those contract principles only. Existing domain names, package structures, concurrency mechanisms, and
storage layouts remain implementation material.

---

## 2. Problem

Without an explicit State Contract, a mutable field, enum value, object subtype, callback phase, or implementation
marker
can quietly become movement authority. The machine then discovers its condition by inspecting what the implementation
happened to leave behind.

Without an explicit State Transition Contract, a stored-value rewrite or completed method can be mistaken for permission
to move. Successful execution begins to stand in for legal movement.

Without an explicit State Machine Manifest, the State and Transition declarations remain a loose collection. Initial and
terminal conditions may be inferred, undeclared moves may appear in callbacks, and the complete movement surface can no
longer be verified before execution.

The state-machine axis must also stay separate from the other two axes. An Operation implementation may produce ordinary
result material, and an Invariant may judge the integrity of proposed Facts, but neither may decide which machine move
is
legal. Conversely, a legal Transition does not establish a Fact or prove its integrity.

Kontrakt therefore needs one closed movement model whose meaning is fixed before realization and whose physical form can
be replaced without changing the contract.

---

## 3. Decision Drivers

State must remain a finite vocabulary of actual machine conditions rather than a copy of every value, clause, or
observed detail in the system.

A declared condition belongs to the State vocabulary when the machine can actually occupy it and it changes which next
movement is legal. A pipeline position, field value, or implementation phase does not become State merely because the
implementation passes through or stores it. Information that explains a run but does not alter movement belongs
elsewhere.

Transitions must be explicit, one-way, and closed. No move may arise from host behavior, inheritance, structural
similarity, object mutation, or backend convention.

State, Transition, and the explicit State Machine must follow the ordinary one-dimensional authoring law. V1 does not
require another IDL language inside each operation.

The state-machine axis must judge movement independently from factual integrity while remaining capable of joining the
same internal change outcome.

Backend realization may change representation, storage, lookup, dispatch, and commit strategy. It may not change the
State vocabulary, legal movement relation, initial condition, terminal conditions, or judgment result.

V1 must stay narrow enough to compile into a closed table. Each stateful Operation selects exactly one State Machine.
That machine governs one flat State surface, and exactly one State of that surface is established at a time.
Multiple-surface composition and cross-surface coordination are not admitted by this ADR.

---

## 4. Decision

Kontrakt will treat `State`, `State Transition`, and `Explicit State Machine Manifest` as ordinary one-dimensional
contract presentations on the state-machine axis.

The operation manifest selects their source declarations:

```text
movement:
    state          TransferStates
    transitions    TransferTransitions
    machine        TransferStateMachine
```

The selected declarations are immutable host-language carrier material. They contain names, finite relations, explicit
absence, and simple closed attributes only. Methods, callbacks, inheritance, runtime lookup, hidden defaults, and
implementation control flow are rejected.

Kontrakt resolves the selected symbols, removes host-language shape, and produces one canonical State Machine. That
canonical material owns movement legality.

```text
State Contract
+ State Transition Contract
+ Explicit State Machine Manifest
    -> Canonical State Machine
```

An implementation may request movement. A backend may observe and physically realize movement. Neither may decide that
an undeclared move is legal or establish State authority through another path.

V1 admits either one explicit State Machine or an explicit Stateless declaration for an operation. Each stateful
Operation selects exactly one State Machine. That machine contains one flat State surface, and exactly one State of that
surface is established at a time. A future extension may define several coordinated surfaces, but V1 does not infer or
compose them.

---

## 5. State-Machine Axis

### 5.1. Axis Authority

The state-machine axis answers one question:

```text
Under the currently established State, which declared move may occur next?
```

It does not decide whether external material is admitted, whether proposed Fact material satisfies an Invariant, whether
a Failure is public, or whether an established Fact may be published. Those decisions remain with their own contracts.

The axis contains the canonical State vocabulary, the canonical Transition relation, the explicit initial and terminal
conditions, movement proposals, Transition judgment, and established movement.

### 5.2. Separation from the Interface Axis

The interface axis identifies the selected Operation and the ordinary host implementation that realizes it. The
Operation implementation does not own current State, Transition legality, or State establishment.

An Operation may return ordinary material from which the generated machine forms an internal change proposal. It does
not call a State Machine API, mutate a contract State object, or return a Kontrakt movement wrapper.

Generated assembly links the Operation outcome to the applicable movement law. That linkage does not move authority into
the implementation.

### 5.3. Separation from the Contract Axis

The contract axis judges factual material. The state-machine axis judges movement.

```text
Invariant holds
    does not imply
Transition is legal

Transition is legal
    does not imply
proposed Fact material is valid
```

When one internal change proposal contains both proposed Fact changes and proposed movement, the two axes judge the same
proposal under separate authority. Neither consumes or replaces the other's result.

### 5.4. Three-Axis Linkage

The three axes meet only through generated machine assembly.

```text
Interface Axis
    selects and invokes the Operation

Contract Axis
    judges proposed factual material

State-Machine Axis
    judges proposed movement

Generated Machine Assembly
    establishes the declared outcome only after every required judgment succeeds
```

Assembly is not a fourth semantic axis. It owns ordering and linkage, not contract meaning.

---

## 6. State Contract

### 6.1. Meaning

State is explicitly declared contract material for an actual condition that the machine can occupy.

The established State tells where the machine currently is on one movement surface and determines which declared
movement may legally occur next. Conditions such as `Open`, `Frozen`, `Waiting`, `Running`, `Success`, `Failed`, and
`Closed` are valid States when the machine can actually be in those conditions and their legal next movements differ.

A State is not every possible combination of values. An object snapshot, a carried functional value, a field named
`status`, or an implementation stage does not become State by itself. It may physically represent or help establish a
State only when it resolves to a declared machine condition under the selected State Machine.

A pipeline exposes the real intermediate work of the machine. A condition reached during that pipeline may be State, but
only when the machine can actually occupy it and it governs what may happen next. Input, canonical material, proposed
Facts, established Facts, Failures, and diagnostics otherwise keep their own authority.

### 6.2. Closed and Flat Vocabulary

A State Contract declares a finite set of directly named conditions. Each declared State name is unique within the
selected State Machine, and the vocabulary is closed before execution.

State inheritance, nested State trees, recursive composition, open subtype discovery, and structural State inference are
forbidden. A parent label cannot lend movement meaning to a child label. Similar names do not create a relation.

Within one selected State Machine and applicable contract world, the resolved State name is the nominal contract
material
that distinguishes one declared State from another. Host enum ordinal, object identity, allocation history, package
placement, and backend code do not participate. Changing a declared State name changes the State vocabulary; whether a
Version contract admits that change as a compatible rename is deferred.

### 6.3. State Source Material

The host declaration may carry only the material needed to name and classify the closed vocabulary. In V1 that means the
State symbols and any finite attribute required by the machine declaration.

The source carrier contains no movement method, transition predicate, lookup, mutable current-State field, callback, or
backend encoding. Convenience behavior is not contract material.

Kontrakt lowers the source into canonical State entries distinguished by their resolved names and discards the carrier
shape.

### 6.4. State and Fact

Fact answers what explicit immutable information is true for the machine. State answers which declared condition the
machine currently occupies and therefore what movement is legal now.

The same physical system may have both factual material and State, but their authority remains separate. A Fact does not
secretly carry State authority, and a State label does not own factual payload. State judgment may depend on an already
declared contract result, but the dependency must be explicit and may not be inferred from object shape or stored
values.

### 6.5. Established State

Exactly one State of the selected surface is established at a time.

A current physical value is not established State merely because a backend can read it. It receives authority only as a
realization of one canonical State entry under the selected machine.

A backend representation that cannot be resolved to exactly one declared State is invalid. Unknown, ambiguous, or
out-of-world values are realization failures, not new States.

---

## 7. State Transition Contract

### 7.1. One-Way Movement

A State Transition Contract declares a permitted one-way move from one declared State to another declared State on the
same surface.

A field rewrite, completed method, callback, exception, log marker, or implementation step does not create a Transition.
The move exists only because the contract declared it before execution.

### 7.2. Transition Material

Each Transition declares one source State and one target State. Multiple Transitions may share the same source State,
the same target State, or both.

Where shared endpoints represent different movements, an explicit enabling outcome or movement request kind must
distinguish them. The enabling outcome must already belong to another declared contract result. Transition does not
compute Admission, Invariant, Policy, Failure, or Operation results. It only refuses to move without the exact result on
which the declared move depends.

A target State alone is insufficient. The source and target must resolve inside the selected State Machine, and one
established State with one movement proposal must not leave more than one conflicting Transition enabled.

### 7.3. Closed Transition Relation

The canonical Transition set is complete and closed. A move absent from the set is illegal.

Duplicate aliases for the same movement are rejected. A backend may not add a shortcut, recovery edge, or side-channel
movement. If another meaning is required, it needs another declared Transition or another contract role.

### 7.4. No Regression or Cycle

Transitions are forward moves on one State surface. V1 rejects a Transition graph that returns to an earlier State.
Correction, retry, restart, and repetition must enter through a new governed run or another explicitly modeled contract
surface. They are not backward movement inside the same machine.

Branching remains legal. One State may have several outgoing Transitions when distinct declared outcomes permit distinct
next moves. Branching does not create hierarchy.

### 7.5. Transition Proposal and Establishment

A movement proposal identifies a declared Transition that may be attempted under the current State and available
contract
results. The proposal has no State authority.

```text
Movement Proposal
    -> Transition Judgment
    -> refused or permitted
    -> physical realization
    -> established Transition and target State
```

Permission alone does not mean that the physical move has occurred. Physical completion alone does not make an illegal
move authoritative.

---

## 8. Explicit State Machine Manifest

### 8.1. Purpose

The explicit State Machine Manifest closes one State surface. It does not become a workflow engine or a parent contract
above State and Transition.

The manifest binds the selected State Contract and State Transition Contract, names the initial State, names every
terminal State, and fixes the complete movement relation.

### 8.2. One Surface

Each stateful Operation selects exactly one explicit State Machine. That machine governs one flat State surface, and
exactly one State of that surface is established at a time.

V1 does not combine several independent movement authorities into a Cartesian-product State vocabulary. It also does not
compose several State Machines or infer cross-surface coordination. An operation requiring those semantics is outside
the
V1 authoring surface until an explicit coordination contract is defined.

This restriction does not prevent Kontrakt's own implementation from using several internal machines. Internal runtime
coordination is backend design unless it is exposed as user contract authority.

### 8.3. Initial State

A stateful machine declares exactly one initial State. The initial State is not inferred from enum order, constructor
default, zero value, first observation, or storage initialization.

The initial State declares the actual condition occupied when movement on that machine surface begins. It does not
assert
that an Operation has succeeded or that any Fact has been established.

### 8.4. Terminal States

A terminal State has no outgoing Transition inside the selected surface. Terminal does not mean successful, accepted,
failed, or public. It means only that movement on that surface has ended. A State named `Success` is therefore not
terminal when another declared movement, such as publication or archival, remains legal from it.

Every declared terminal State must be terminal in the canonical Transition relation. A State with an outgoing move may
not be marked terminal, and an unreachable terminal label does not close the machine.

Once a terminal State is established, no physical mechanism may establish a later movement on the same surface.

### 8.5. Closure Validation

Kontrakt rejects a machine when:

- a State name is duplicated within the selected machine;
- a Transition refers to an undeclared State;
- the initial State is absent or duplicated;
- a declared terminal State has an outgoing Transition;
- a Transition is duplicated or ambiguous;
- the graph contains a cycle;
- a non-initial State is unreachable;
- a nonterminal State has no declared continuation unless the machine explicitly classifies it as terminal;
- source material relies on behavior, inheritance, lookup, or hidden defaults.

These are definition-time contract failures. They do not become runtime movement refusals.

### 8.6. Stateless Machine

An operation with no movement surface declares that absence explicitly.

```text
movement:
    state          Stateless
    transitions    none
    machine        StatelessMachine
```

Stateless is canonical contract material, not omission. It declares that the operation has no State vocabulary, no
Transition relation, and no movement establishment.

---

## 9. Authoring and Processing Boundary

### 9.1. Manifest Selection

The operation manifest selects the three movement declarations by exact source symbol. It does not contain the State or
Transition bodies.

```text
operation
└── manifest
    └── movement
        ├── state
        ├── transitions
        └── machine
```

Unlike Lowering and Publication, these contracts declare no coordinate relation across two operation surfaces. They
therefore receive no operation-local IDL body.

### 9.2. Host Carrier Law

The selected host declarations are frontend evidence only. Their admissible material is finite, immutable, and directly
inspectable.

A declaration may carry State names, Transition names, source and target references, initial and terminal references,
explicit absence, and other finite attributes approved by the State-Machine frontend. It may not carry algorithms,
constructors with hidden policy, callbacks, lambdas, runtime discovery, mutable registries, or object dispatch.

Host annotations, inheritance, marker interfaces, and package scanning do not grant a State-Machine role. The operation
manifest slot does.

### 9.3. Canonical Lowering

Kontrakt resolves the three selected declarations together. It verifies their symbol closure, removes host-language
shape, forms canonical State and Transition entries, and produces one deterministic machine image.

Conceptually:

```text
State source
    -> canonical State table

Transition source
    -> canonical Transition table

Machine source
    -> canonical initial, terminal, and closure material

all three
    -> Canonical State Machine
```

The canonical material, not the source carrier, becomes the authority used by verification and execution.

---

## 10. Movement Judgment

### 10.1. Inputs to Judgment

Transition judgment receives the canonical machine identity, the currently established State, one movement proposal, and
the exact declared contract result required by that Transition.

It does not inspect arbitrary Operation objects, callbacks, repositories, logs, host exceptions, or backend layout. Any
required factual or judgment material must arrive through an explicit contract binding.

### 10.2. Judgment

The reference judgment is finite:

```text
current State
+ proposed Transition
+ declared enabling outcome
    -> permitted or refused
```

Permission requires an exact source-State match, a declared Transition in the selected machine, the required enabling
outcome where applicable, and a target that belongs to the same State surface.

The judgment neither mutates storage nor establishes the target State.

### 10.3. Single Establishment Authority

Several paths may propose movement, but only the generated machine may establish the winning Transition.

An Operation implementation, adapter, callback, timeout mechanism, Failure handler, repository, or backend object may
not change State through a side channel. Every authoritative movement passes through the same canonical judgment and
establishment boundary.

### 10.4. Refusal

A movement refusal means that a well-formed movement proposal is not legal under the currently established State and the
selected machine. It does not mean that the State Machine declaration was malformed or that physical storage failed.

The complete Failure and Diagnostic Evidence contracts are deferred. This ADR preserves only the distinction between
movement refusal, definition-time rejection, and realization failure.

---

## 11. Physical State Realization

### 11.1. Replaceable Realization

The canonical State Machine owns movement meaning. A backend owns physical observation and realization.

A backend may use generated local material, Kontrakt-owned runtime storage, or an explicitly supplied external
realization. The choice does not change the State vocabulary or legal Transition relation.

This ADR does not require one user-supplied port for every selected machine. It requires every executable machine to
have
one closed physical realization before publication of the machine image.

### 11.2. Observation

Physical observation must resolve to exactly one canonical State of the selected machine. Observation cannot invent a
State or repair an unknown value by guessing.

A physical representation may contain additional implementation material, but that material carries no State authority.

### 11.3. Realization of Movement

After Transition judgment permits a proposal, the backend attempts the physical move. The target State receives
authority
only when that realization succeeds under the machine's required indivisibility and visibility law.

The concrete strategy may use generated code, a local structure, a transaction, or another replaceable mechanism. This
ADR does not standardize that mechanism.

### 11.4. No Partial Authority

When one internal change proposal binds Fact changes and State movement, the contractual outcome is indivisible.

```text
all required Invariant judgments succeed
+ Transition judgment succeeds
+ required physical realization succeeds
    -> Fact changes and target State become authoritative together
```

A physical intermediate result may exist during realization, but it may not become visible as partial contract
authority.
The detailed commit protocol is a backend concern and remains outside this ADR.

### 11.5. Terminal Sealing

A backend must prevent later authoritative movement after a terminal State has been established. A writable terminal
representation violates the canonical machine even when no ordinary code path intends to use it.

---

## 12. Determinism and Optimization

The same canonical State Machine, established source State, movement proposal, enabling outcome, and contract world must
produce the same Transition judgment.

Canonical State and Transition ordering may not depend on source acquisition order, host enum ordinal, reflection order,
allocation history, hash iteration, or backend layout.

A backend may lower the canonical relation into dense tables, generated branches, primitive identifiers, specialized
commands, or another deterministic form. It may eliminate abstractions when equivalent behavior is proven. An optimized
path that permits an undeclared move, changes refusal attribution, or weakens terminal sealing is incorrect.

Verification uses the canonical reference machine. Optimized realization must remain equivalent to it.

---

## 13. Complete Movement Model

The contract pipeline exposes the machine's real intermediate work. The State-Machine Axis does not duplicate that
pipeline. It tracks the actual condition occupied by the machine and judges the next declared movement where State is
applicable.

For a stateful operation, the conceptual path is:

```text
operation manifest selects State, Transition, and Machine declarations
-> source carriers are resolved and lowered
-> one Canonical State Machine is sealed
-> current State is observed under that machine
-> ordinary Operation execution and contract judgments produce declared outcomes
-> generated assembly forms an internal movement proposal where required
-> State-Machine Axis judges the proposal
-> backend realizes the permitted move
-> target State is established with the rest of the declared change outcome
```

For a Stateless operation, the movement path is explicitly absent and no State observation, Transition judgment, or
movement establishment occurs.

---

## 14. Deferred Decisions

This ADR does not define the complete Failure Contract, Diagnostic Evidence, Diagnostic Retention, Version, Policy,
Budget, Capacity, or Governance processing profiles.

It also defers:

- the concrete Kotlin and Java carrier APIs for State, Transition, and Machine declarations;
- the generated or supplied physical realization ABI;
- durable and distributed commit protocols;
- coordination among several independent State surfaces;
- governed retry, restart, and repetition models;
- public presentation of movement refusal.

These omissions do not reopen the authority law decided here. State-machine meaning belongs to canonical contract
material, one machine governs one flat State surface in V1, and no implementation path may create movement authority.

---

## 15. Consequences

State, State Transition, and Explicit State Machine Manifest now have the same V1 authoring discipline as the other
ordinary one-dimensional presentations. The interface IDL selects their handles; immutable host declarations carry
candidate material; Kontrakt lowers that material into its own machine form.

The state-machine axis is independent from the interface and contract axes without becoming a workflow engine. Operation
execution, Fact Integrity, and movement legality remain separate authorities joined only by generated assembly.

The machine can reject malformed movement surfaces before execution, generate a reference Transition judge, verify an
optimized backend against that judge, and attribute runtime movement refusal without reading implementation behavior.

The V1 restriction to one flat State surface prevents hidden composition and product-state explosion. Systems requiring
several coordinated movement authorities must wait for an explicit coordination contract rather than smuggling that
coordination into State names or callbacks.

Physical realization remains replaceable. Removing or changing a storage mechanism does not change the contract as long
as observation, Transition judgment, establishment, and terminal sealing remain equivalent to the canonical State
Machine.