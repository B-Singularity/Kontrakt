# ADR-0049: Flow Contract Processing — Fact, Invariant, and Publication

## Status

Draft

## Date

2026-07-12

## Related

- `docs/what-contract-is.md`
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

ADR-0048 defines the inbound airlock.

Input establishes judgeable boundary presentation. Admission decides whether that material may continue.
Canonicalization optionally establishes a stable representative. Lowering removes external-presentation authority and
forms sealed immutable Fact material for legal handoff into the core.

The core is the isolated space of the machine in which untrusted external representation no longer exists. Everything
the core may know must be present as explicit immutable information. A Fact is that information itself.

Fact is therefore not limited to the output of an implementation operation. Boundary Facts may enter through the
airlock. Existing Facts may already be present in the current core world. Core realization may produce a Result or new
Fact material. Classes, records, rows, object graphs, repositories, callbacks, and backend layouts may carry or realize
information, but they do not own factual meaning.

A single enclosing interface scope may declare several operation pipelines that enter the same explicit core. Policy,
Governance, Budget, and Capacity are bound once at that machine scope because they coordinate finite resources and
decisions shared among the closed operation set. They are not repeated in each operation manifest. Internal core work
remains replaceable implementation and does not recursively open another operation pipeline.

This ADR defines the remaining flow contracts:

```text
Fact
Invariant
Publication
```

These contracts answer three different questions:

```text
Fact:
    what explicit immutable information exists for the core?

Invariant:
    what declared law or relation must hold over explicit Facts or Results?

Publication:
    what outward claim may be produced from core information?
```

Failure and diagnostic representation, movement, and bounds remain separate category concerns. This ADR preserves their
attribution boundaries but does not define their complete processing profiles.

---

## 2. Problem

Without a separate Fact contract, core information is easily hidden inside implementation classes, mutable fields,
getters, repository lookups, object identity, or backend storage. The machine then has to execute implementation to
discover what it knows.

Without a separate Invariant contract, laws over Facts and Results are easily hidden inside constructors, validators,
services, persistence hooks, or state managers. The machine cannot distinguish information from the judgment applied to
that information.

Without a separate Publication contract, internal Fact or Result material can leak outward merely because it is
immutable or easy to serialize. That would couple external users to core representation and would collapse internal
knowledge into public claim.

The machine therefore needs explicit immutable factual material, explicit judgment over that material, explicit legal
movement of availability, and a separate outward-claim boundary.

---

## 3. Decision Drivers

Fact must be contract material, not a class, row, DTO, record, Value Object, entity, object graph, repository entry, or
storage layout.

Everything the core may know must be available as explicit immutable Fact material. The core must not discover
information through implementation behavior or external capabilities.

Ordinary primitive and closed immutable host types may nominate Fact coordinates when they preserve the required
information. Kontrakt must not require a proprietary wrapper merely to make a value look internal.

Fact meaning must remain separate from physical representation. A backend may replace object fields with primitive
arrays, packed bytes, generated tables, or another deterministic layout without changing the Fact.

Invariant must judge explicit declared material. It must not grant information its factual meaning, wander through
implementation object graphs, query hidden state, or use runtime references as authority.

State and Transition must remain separate from Fact and Invariant. They govern when information is legally available and
how the machine moves; they do not make a class or return value factual.

Publication must be an explicit judgment. Immutability and internal availability do not make core material public.

External publication must expose a publication presentation, not the internal Fact carrier or backend representation.

A backend may optimize Fact storage, Invariant evaluation, movement checks, and emission, but it may not change factual
meaning, judgment law, state movement, or publication permission.

---

## 4. Decision

ADR-0049 defines the core information and outward-claim half of flow processing. Distinct operation pipelines may
supply boundary Facts through their own ADR-0048 airlocks, while the enclosing machine scope supplies the shared Policy,
Governance, Budget, and Capacity material fixed for the selected operation run:

```text
boundary Facts from the selected ADR-0048 operation flow
+ existing Facts
+ machine-wide Policy, Governance, Budget, and Capacity material
+ other explicit immutable contract material
    -> replaceable core realization
    -> Result Material
       and/or
    -> new Fact material

Facts and Results
    -> Invariant judgment where selected
    -> legal State / Transition movement where required

Fact or publishable Result
    -> Publication judgment
    -> outward presentation or declared publication stop
```

The Fact Contract declares immutable information that may exist in the core. It does not describe one implementation
object and does not require that the information was produced by an Operation.

The Invariant Contract declares a law over explicit Fact or Result material. Its judgment may be a prerequisite for a
later transition, result completion, or publication, but it does not manufacture Fact meaning and does not convert an
implementation object into information.

The State and Transition axis governs legal movement and availability. New Fact material may be formed before it becomes
visible in a later machine condition. That distinction is movement, not a second Fact schema and not an
Invariant-created truth status.

The Publication Contract is the outward-claim authority. Core Fact and Result material do not leave the machine
directly. Publication decides whether an outward presentation is permitted and which information that presentation may
contain.

The common authority law is:

```text
Fact declares information.
Invariant declares judgment over information.
State and Transition declare legal movement and availability.
Publication declares the permitted outward claim.
```

No implementation class, generated serializer, repository row, object reference, cache entry, or frozen storage layout
may replace any of those roles. Core realization may contain one function or many functions, but that decomposition does
not create another IDL operation, another airlock, or another contract pipeline. The machine-wide resource contracts
continue to govern the selected operation run independently of the internal implementation graph.

---

## 5. Flow Processing Profiles

### 5.1. Fact Contract

Fact is explicit immutable information available to the core machine.

A Fact is not the object, row, message, record, field, or value instance that may carry it in software. Those are source
evidence or realizations. The Fact is the information declared by the contract and made available under the legal
machine world.

Fact material may arise through several paths:

```text
external presentation
    -> ADR-0048 airlock
    -> boundary Fact

existing core world
    -> already available Fact

core realization
    -> new immutable Fact material
    -> legal movement into availability where required
```

These paths do not create different kinds of Fact. They describe different formation and availability histories for
explicit immutable information.

A Fact Contract declares the coordinates, sorts, distinctions, bounds, version meaning, and other factual laws required
by that information. Identity is included only when that Fact actually declares identity. Fact is not universally an
entity, event, identifier, persisted record, or state snapshot.

A host frontend may use primitives, strings, enums, arrays under an immutable profile, finite products, Kotlin data
classes, or Java records as declaration evidence when their complete visible shape can be refined. Kontrakt does not
require `CoreInt`, `KontraktText`, or a user-authored Value Object merely because information exists inside the core.

Host constructors, methods, custom equality, inheritance, object identity, allocation, and storage layout do not enter
Fact authority. The same Fact may be realized through different languages and backend layouts without changing its
meaning.

### 5.2. Invariant Contract

Invariant is an explicit judgment over Fact or Result material.

Invariant does not decide whether a piece of immutable information is a Fact merely because it accepts it. Fact meaning
comes from the Fact Contract and the legal boundary by which the material is made available. Invariant instead asks
whether a declared law or relation holds over the explicit information it is permitted to inspect.

An Invariant may judge:

```text
relations among coordinates of one Fact
relations among several explicit Facts
relations between existing Facts and newly formed Fact material
relations between Facts and a declared Result
conservation, coherence, uniqueness, ordering, ownership, or other declared law
```

The judgment surface must name its allowed Fact and Result coordinates explicitly. Those coordinates are not object
references and do not invite graph wandering. Invariant may not query repositories, services, state managers, mutable
globals, runtime class hierarchies, callbacks, or hidden implementation state.

Invariant and State remain separate. Invariant judges whether a law holds. State and Transition decide whether a
movement is legal and perform the movement. An accepted Invariant judgment does not itself change machine condition or
make new material available.

Invariant is not a validator drawer and not a constructor guard. It is an explicit contract presentation whose outcome
is visible to the machine and attributable to the law that produced it.

### 5.3. Publication Contract

Publication is the outward claim.

Core Fact material and Result material are not automatically public material. The machine may know more than it is
allowed to say, and a core Result may use a representation inappropriate for external consumers.

Kontrakt lowers Publication into the law that permits or denies an outward claim from declared Fact or Result material.
Publication may select, rename, omit, and re-present information only under its explicit contract. It must not expose
backend coordinates, storage layout, hidden diagnostic evidence, mutable aliases, or implementation objects merely
because an emitter can reach them.

A backend may serialize, encode, buffer, or emit the permitted presentation. Emission is implementation. It is not
Publication authority.

Diagnostic material remains internal unless Publication allows a corresponding public diagnostic claim.

---

## 6. Cross-Profile Boundaries

### 6.1. Fact and Carrier

Fact and carrier must not collapse into one role.

A Kotlin class, Java record, database row, event object, primitive array, or packed region may carry Fact material. The
carrier does not become factual authority. Factual meaning remains in the ratified Fact Contract and its explicit
coordinates.

Changing carrier, allocation strategy, field layout, packing, or backend language does not change the Fact when the
declared information and distinctions remain identical.

### 6.2. Fact and Invariant

Fact and Invariant must not collapse into one role.

Fact declares the information that exists. Invariant declares a law over that information. A Fact may exist without a
particular Invariant, and one Invariant may judge relations across several Facts.

Invariant success does not create Fact meaning. Invariant failure means that the selected law does not hold and may
block a Result, Publication, or State Transition according to the declared machine. It does not authorize the machine to
mutate, repair, or hide the information.

### 6.3. Fact, Result, and Publication

Fact and Result are both explicit immutable material, but they have different roles.

```text
Fact:
    information available to the core machine

Result:
    information produced for a declared completion or outward use
```

A Result does not automatically become a Fact merely because it is immutable. If the same material must become
available to later core processing, the flow must declare the corresponding Fact role and legal movement explicitly.

Publication may derive an external presentation from a Fact, a Result, or both when the Publication Contract permits it.
The external presentation is neither the Fact nor the Result itself. Internal coordinates, backend layout, and
diagnostic evidence remain hidden unless a corresponding outward claim is explicitly allowed.

### 6.4. Fact, State, and Transition

Fact is information. State is the explicit machine condition governing what movement is available. Transition is the
declared movement between conditions.

A state label does not own Fact meaning, and a Fact does not secretly carry lifecycle state. New Fact material may be
formed while not yet visible in a later machine condition. The legal Transition governs that availability without
changing the information merely to create a second host type.

Invariant may supply a judgment required by a Transition, but it does not inspect or control the State Machine. The
State and Transition axis consumes the bound judgment and performs the movement under its own authority.

### 6.5. Publication and Diagnostics

Diagnostic evidence may explain Fact formation, Invariant refusal, movement refusal, or Publication refusal, but
evidence does not create or override any of those roles.

Retention decides what diagnostic material may survive. If retained diagnostic material is ever exposed, Publication
must judge that outward claim separately. Failure, evidence, retention, and publication therefore remain distinct
contracts even when one runtime path realizes them together.

### 6.6. Handoff from ADR-0048

This ADR begins after ADR-0048 has formed sealed boundary Fact material and the selected operation's legal core-entry
handoff makes that information available. Several operation pipelines may exist under one enclosing interface scope, but
those handoffs enter the same explicit core governed by the shared machine-wide Policy, Governance, Budget, and Capacity
contracts.

It does not repeat Input refinement, Input formation, Admission, Canonicalization, or Lowering. Any defect that should
have been stopped by those contracts remains a defect in the inbound airlock; Invariant is not a catch-all validator for
malformed boundary presentation or failed conversion.

The core does not receive the Input object, Canonicalization source, Lowering declaration, mapping table, staging
object,
or external framework context. It receives only explicit immutable Fact information and other declared core material.
Internal realization may be divided or fused freely behind those obligations, but it does not create nested operation
manifests or require Publication and Lowering between its internal steps.

---

## 7. Deferred Decisions

This ADR does not decide the final authoring syntax for Fact, Invariant, or Publication bodies.

It does not define the complete authoring surface by which the enclosing machine contract and its operation flows
declare
all required Facts, possible Results, new Fact surfaces, or the replaceable implementation realization between them.

It does not define the complete state and transition sets for Fact availability, Result completion, Invariant refusal,
movement refusal, or publication. Those movement surfaces must be designed with the complete operation flow, failure,
diagnostic, and Version material together with the enclosing machine-wide Policy, Governance, Budget, and Capacity
contracts.

It also does not define final persistence layout, cache layout, core Fact storage, public serialization schema, or
emitter
implementation. Those are replaceable realizations behind the contract boundaries fixed here.

---

## 8. Consequences

The core is now defined as an explicit information machine rather than an object graph that discovers knowledge through
implementation behavior. One enclosing interface may expose several operation pipelines without dividing that core.
Each operation retains its own external airlock and Publication boundary, while machine-wide Policy, Governance, Budget,
and Capacity coordinate the finite resources shared among all of them.

Fact is explicit immutable information, not an Operation return type, a candidate waiting for Invariant acceptance, a
Value Object, a persistence row, or a backend layout. Boundary Facts may enter through the ADR-0048 airlock, existing
Facts may already be available, and core realization may form new Fact material.

Invariant has one visible judgment role without becoming the source of Fact meaning or State movement. State and
Transition govern legal availability separately. Publication remains an outward judgment rather than an automatic side
effect of immutability, result production, persistence, or serialization.

Primitive and ordinary closed immutable host types may serve as frontend evidence when they preserve the required
information. Core richness comes from explicit Facts, laws, judgments, states, transitions, and Results—not from
behavior-bearing classes or semantically authoritative Value Objects.

Internal Fact carriers do not need to appear in Input or public output contracts. External users may receive Publication
presentations while core factual representation remains replaceable.

The split between ADR-0048 and ADR-0049 keeps optimization honest. The selected operation's inbound airlock rejects
malformed or inadmissible material before core work is paid. The core operates only over explicit immutable information
under the machine-wide contracts fixed for that run. Internal functions and stages remain implementation rather than
nested operation pipelines. Invariant evaluates the remaining declared laws. State and Transition govern movement.
Publication pays outward transformation and emission cost only after an outward claim is permitted.