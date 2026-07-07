# ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary

## Status

Draft

## Date

2026-07-07

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure

---

## 1. Context

ADR-0046 decides the interface contract frontend.

A `.kontrakt` interface manifest declares operation handles and binds those handles to contract-axis and
state-machine-axis material. Kontrakt then generates an ordinary host interface for implementation. The generated host
interface is a build artifact. It is not contract authority.

ADR-0046 also records the initial one-dimensional contract catalog. It does not decide how those presentations are
authored.

This ADR continues from that edge.

The problem is not that Kontrakt needs source material. Of course it does. A machine cannot lower a contract it cannot
see.

The problem is where the mark is placed.

The user's system already has its own contracts before Kontrakt arrives. They may show up as data shapes, messages,
schemas, records, names, or domain rules. If Kontrakt requires those materials to implement Kontrakt types, carry
Kontrakt annotations, or be assembled from Kontrakt objects, then Kontrakt is no longer just reading the user's
contract.
It is making the user's contract wear Kontrakt's shape.

That is another authority leak.

But the opposite is also false. Kontrakt cannot scan a project and pretend that shape alone tells the truth. Most data
is
not contract material for a given operation. A value is not input because it looks like input. A value is not
publication
because it looks like a response. Shape can help after a role has been selected. Shape cannot select the role by itself.

The selection point is already in the design.

ADR-0046 gives each operation an explicit pipeline. That pipeline has slots. A slot is not decoration. It is the
declared
place where a kind of contract material enters, is judged, moves, or leaves.

So the mark belongs there.

A user type does not have to be annotated. It does not have to implement a marker. It does not have to join a host
inheritance tree. A second wiring document does not have to say that A means B. The operation pipeline already says
which
material is being requested and under which one-dimensional role it is being requested.

The same one-dimensional material may be referenced from more than one operation or more than one slot when that is a
declared choice. That does not require host inheritance, host composition, or nominal type tricks. The slot gives the
role. The source gives candidate material. Lowering produces Kontrakt-owned material.

This also changes how the author sees the contract. The manifest is not a blank page. It is a visible pipeline with
empty places that must be filled or explicitly left empty. The author does not start by inventing a pile of code. The
author sees the operation, sees the slots, and decides what belongs in each place. That makes replacement visible too:
changing an admission rule, an invariant, or a publication rule is a slot change, not a hunt through host machinery.

This ADR decides that boundary for the remaining one-dimensional presentations.

---

## 2. Problem

The naive solutions all leak.

A host annotation puts a Kontrakt mark on user-owned material. It is easy to discover, but it makes the user's contract
wear Kontrakt's surface.

A host marker interface gives the mark a nominal type. It may be cleaner than an annotation, but it still makes the user
system carry Kontrakt's contract shape.

Host inheritance and host composition are worse. They allow contract meaning to hide inside host-language relation
mechanics.

A separate projection or mapping file avoids touching the user type, but it creates another authored wiring surface. If
it repeats source facts by hand, it becomes a sidecar mirror. If the user source moves and the wiring does not, it
drifts.

Scanning all user data is not a solution either. Kontrakt cannot decide contract role by looking at shape alone.

The frontend therefore needs a selection law that does not tattoo user types, does not create a bureaucratic mirror, and
does not ask Kontrakt to guess.

---

## 3. Decision Drivers

The role of a one-dimensional presentation must come from the declared contract machine, not from incidental host form.

User-owned source material must remain user-owned. Kontrakt may acquire from it, but it must not require that material
to become Kontrakt's own contract surface.

The final authority must be Kontrakt-owned canonical material. Source carriers, host types, annotations, markers,
generated adapters, compiler metadata, call-site helpers, caches, and backend tables are not final authority.

Boilerplate removal matters, but it must not move authority into convenience machinery.

Kontrakt should be removable as machinery. Removing it may remove checks, generated gates, deterministic backend paths,
verification, diagnostics, and optimization. It must not erase the user's own contract source or require the user's
model
to be rewritten back out of Kontrakt's shape.

The interface manifest should make the pipeline visible. A visible slot gives a small responsibility a home. It gives
cohesion without hiding the work inside a host type, callback, annotation, inheritance edge, or helper object.

V1 must not decide more user API than necessary. The selection law is decided here. The final authoring format for each
one-dimensional body, the host API, and compiler-plugin airlock mechanics remain open.

---

## 4. Alternatives

### 4.1. `.kontrakt` syntax for every one-dimensional body

Every one-dimensional contract body could be written directly in `.kontrakt` syntax.

This keeps all contract source in one language, but it makes the user learn a new surface for every small piece of
contract data. Most one-dimensional presentations are not algorithms. Many are data-shaped declarations.

Decision: rejected as the only model.

### 4.2. Host annotations as the primary mark

A user type could be marked with host annotations.

This is easy to scan, but it puts the selection mark inside the user-owned source. It also routes source meaning through
host annotation mechanics.

Decision: rejected as the primary selection mechanism.

### 4.3. Host marker interfaces as the primary mark

A user type could implement an empty host interface used as a contract marker.

This gives the contract a nominal handle, but it still makes the user system carry Kontrakt's contract shape. It also
invites inheritance and composition unless heavily restricted.

Decision: rejected as the primary selection mechanism.

### 4.4. Sidecar projection mapping as the primary boundary

A separate projection file could point from user-owned source material to Kontrakt roles.

This avoids modifying user source, but if it becomes the main way to connect everything, it creates a second wiring
surface. If it copies facts, it becomes the same sidecar mirror problem ADR-0046 rejected for interfaces.

Decision: rejected as the primary selection mechanism.

### 4.5. Structural discovery over all user material

Kontrakt could scan user types and treat matching shapes as candidates.

Shape alone does not declare role. The same shape may be input for one operation, publication for another, internal
storage for a third, and irrelevant implementation data for the rest.

Decision: rejected.

### 4.6. Pipeline-slot selection

A one-dimensional presentation is selected by the slot it occupies in the declared operation pipeline.

The slot gives the role. Host material entering that slot may then be acquired, structurally checked, rejected, lowered,
or realized by generated machinery.

Decision: accepted.

---

## 5. Decision

Kontrakt will use pipeline-slot selection for one-dimensional contract presentations.

The mark is the slot, not the user's type.

A one-dimensional contract presentation is not selected because a class carries an annotation, implements a marker,
participates in a host inheritance tree, appears in a mapping file, or happens to have the right shape.

It is selected because an operation's declared pipeline binds a role at a slot.

Short form:

```text
operation handle
-> declared pipeline slot
-> one-dimensional role
-> acquired source material
-> resolved and lowered canonical material
-> generated or static realization
```

The interface manifest names the operation and the slots. The slot declares why material matters. Host material gives
raw source facts only after it crosses that slot.

Kontrakt must acquire only material selected by declared slots or by slot-owned references. Unreferenced user material
is
ignored.

This ADR does not decide the final user API for providing host material to a slot. That API may later be an explicit
generated helper, a generated adapter, a host-language declaration surface, a compiler-plugin call-site airlock, or a
combination of those mechanisms.

Whatever form is chosen, it must obey the same law: the slot gives the role; lowering gives the material; backend
machinery realizes the material.

---

## 6. Pipeline-Slot Selection Law

A pipeline slot is a declared position in an operation's contract axis or state-machine axis.

A slot does not describe implementation order. It declares a contract role.

Input material is not input because a user type says so. It is input because it enters the input slot of an operation
whose input obligation has been declared.

Publication material is not publication because a response DTO says so. It is publication because it is judged at the
publication slot.

State movement is not movement because a method changed a status field. It is movement because the state-machine axis
contains a declared state and a permitted transition.

The slot gives the role. The source gives candidate material. Lowering produces Kontrakt material.

---

## 7. Source Carrier Law

One-dimensional bodies are not required to use `.kontrakt` syntax in V1.

That does not make them code.

A contract source may be carried by a host language, but it must arrive as contract data. It may give names, shapes,
coordinates, finite choices, limits, versions, declared absence, and simple facts. It must not ask the host program to
run
private behavior and then call the result a contract.

The line is plain. A source carrier says what is there and what must be exposed to Kontrakt. It does not hide the reason
inside a method, a callback, a constructor trick, a default mask, or a runtime lookup.

Some one-dimensional presentations are almost all data. Input shape, publication shape, version coordinate, policy
limit,
budget limit, capacity limit, retained diagnostic boundary, state name, and transition name should be written as
material,
not behavior. They are things the machine must be able to read, compare, lower, and remember.

A boolean is near the edge. It can say that something is required, absent, terminal, retained, public, or enabled. That
is
still data. Once the answer has to be computed through host control flow, the source has crossed the line.

The rule is not no host language. Kotlin/JVM may provide data classes, compiler metadata, generated source, or a typed
declaration surface. Those are carriers. Kontrakt reads them, resolves them, and lowers them. Their host mechanics do
not
speak for the contract.

If a presentation needs judgment logic, the logic must be declared in a form Kontrakt can lower. It must not remain an
opaque host algorithm. Predicate grade, helper APIs, and the future judgment language remain unresolved by this ADR.

The carrier may help the author say the material. It does not get to decide the meaning.

---

## 8. Canonical Material Law

Authority begins after acquisition, resolution, and lowering into Kontrakt-owned canonical material.

Canonical material must be deterministic. Identity, ordering, equality, version coordinates, declared absence, and
failure outcomes must not depend on source traversal accidents or host runtime mechanics.

A source carrier may be convenient. It may be typed. It may be generated. It may be host-language native. It may be easy
for a user to write.

That does not make it the contract.

The contract-bearing result is the lowered material that Kontrakt owns.

---

## 9. Backend Realization Boundary

Kontrakt may remove boilerplate after role selection and lowering are controlled.

It may generate host adapters, static gates, extraction plans, access tables, verifier payloads, diagnostic labels,
source-coordinate maps, capacity checks, policy dispatch, publication emitters, and specialized backend paths.

It may also use compiler-side caching, canonical material indexes, deterministic ordering, allocation-light layouts, and
machine-sympathetic realization strategies.

None of this machinery owns contract meaning.

A backend may fuse, split, inline, specialize, cache, precompute, or replace realization paths as long as the declared
contract material remains unchanged.

If generated machinery and canonical material disagree, the generated machinery is wrong.

---

## 10. Host Boundary Airlock

The slot gives the role. A host boundary airlock may make the host compiler accept the crossing.

Kotlin/JVM is nominal. It does not accept an arbitrary object at a boundary merely because the object structurally
contains the material a slot needs.

Therefore boilerplate removal may require generated boundary machinery.

In V1, that machinery may be an explicit generated helper or adapter.

In a later compiler-backed form, a compiler plugin may verify call sites, check structural fit, and lower direct host
calls into generated extraction code.

This ADR does not decide the final airlock API.

Both forms obey the same law. The user type is not the mark. The slot is the mark. The airlock is machinery. The lowered
material is the contract-bearing result.

---

## 11. Common Processing Model

Each one-dimensional presentation follows the same abstract path.

```text
declared operation slot
-> selected source carrier or declared absence
-> acquisition under the slot role
-> resolution
-> explicit absence/default closure
-> lowering into canonical material
-> generated or static realization
-> bounded diagnostics when judgment is made
```

This path is not a physical runtime schedule. It is the authority path.

A backend may realize it through a different physical layout if the canonical material, judgment result, failure
meaning,
publication claim, state movement, diagnostic boundary, and governance limits remain unchanged.

---

## 12. One-Dimensional Presentations

ADR-0046 already decides the Interface Surface Contract as the manifest-bound operation surface. This section covers the
remaining presentations.

### 12.1. Input Contract

Input declares what presentation shape may appear at an operation boundary.

The input slot selects candidate material. A host object, structured source, schema, or generated carrier may provide
raw
facts for that slot, but the host shape is not the input contract.

Kontrakt lowers input material into canonical input material: input identity, operation binding, member identity,
member order, presence, cardinality, presentation kind, and source coordinates.

Admission, canonical equivalence, lowering into core facts, invariants, state movement, and publication are not decided
by the input slot.

### 12.2. Admission Contract

Admission declares when boundary presentation may enter the contract pipeline.

The admission slot selects the judgment criteria. Kontrakt may realize those criteria as generated gates, static checks,
primitive comparisons, verifier payloads, or runtime checks, but the gate is not authority.

Admission produces declared admit, reject, or stop meaning. Hidden acceptance is not allowed.

The final predicate grade and authoring form remain unresolved.

### 12.3. Canonicalization Contract

Canonicalization declares how admitted presentation becomes a stable system representation.

It must account for equivalence, the system-owned representative, tolerated source drift, and failure when stable
representation cannot be produced.

The canonicalization slot selects the rule. Kontrakt may generate normalization, ordering, encoding, and representative
selection machinery, but tolerated drift and failure meaning must be declared material.

### 12.4. Lowering Contract

Lowering declares how canonical representation becomes core-readable candidate material.

It is not a helper conversion. It is the boundary where external presentation stops being raw and begins to become
machine material.

Kontrakt may generate extractors, tables, byte encoders, field plans, and internal candidate builders. Those artifacts
realize lowering; they do not define what lowering means.

### 12.5. Fact Contract

Fact declares what kind of factual material may exist inside the core.

A fact is not the same thing as an input value, a DTO, a host object, a database row, or a runtime instance. It is
accepted material in the contract machine's own vocabulary.

Kontrakt must give fact material stable identity, stable ordering, version coordinates where needed, and exact equality
material when hash identity is not enough.

### 12.6. Invariant Contract

Invariant declares whether lowered candidate material may become accepted core material.

The invariant slot is a judgment slot. It may be realized by generated checks, verifier facts, static gates, or runtime
machinery, but the invariant is not a host assertion object.

If an operation has no additional invariant, that absence must be declared.

### 12.7. State Contract

State declares the finite, closed, flat machine conditions that govern legal next moves.

A state is not a mutable object field, status enum convenience, lifecycle label, or callback phase. It is a declared
machine condition.

Kontrakt must lower state vocabulary into closed canonical state material. Open-ended movement vocabulary is not
allowed.

### 12.8. State Transition Contract

State transition declares permitted one-way movement between declared machine conditions.

A transition is not every stored-value rewrite. It exists only when the contract permits a move between flat declared
conditions.

Kontrakt must reject hidden transition creation through callbacks, host methods, inheritance, composition, or framework
lifecycle mechanics.

### 12.9. Explicit State Machine Manifest

The explicit state machine manifest declares the state set, initial condition, terminal conditions, and permitted
transitions of one movement surface.

It belongs to the state-machine axis, not to a normal linear stage in the contract pipeline.

The manifest does not own behavior. It names the closed movement surface that state and transition material must obey.

### 12.10. Failure Contract

Failure declares contract-governed stop results.

A failure is not just an exception and not every thrown host error. It is a declared result of a judgment refusing to
continue.

Kontrakt may map backend errors into declared failures only through a ratified failure law. Host exception type alone
must not become failure authority.

### 12.11. Publication Contract

Publication declares whether accepted material may become an outward public claim.

Publication is not serialization. Serialization may be one realization path, but publication decides whether a claim may
leave the machine and what it is allowed to claim.

Kontrakt may generate publication views, emitters, response adapters, or external presentation forms. Those artifacts do
not own publication meaning.

### 12.12. Diagnostic Evidence Contract

Diagnostic evidence declares what explanation may be offered by a declared judgment.

Evidence exists because a machine must be able to account for its own judgment. It is not logging decoration.

Kontrakt may generate source-coordinate maps, judgment traces, bounded explanation payloads, and diagnostic labels. The
evidence candidate is still governed by the diagnostic evidence contract.

### 12.13. Diagnostic Retention Contract

Diagnostic retention declares what evidence may remain after the run, how it is bounded, and what must be discarded.

Existence is not retention.

A judgment may produce diagnostic evidence candidate material. Retention decides what survives. Kontrakt must keep that
boundary explicit and bounded.

### 12.14. Version Coordinate

Version coordinate declares which contract meaning governed a judgment, material, claim, or evidence.

It is not a release tag convenience. It is how the machine says which meaning was active when a result was produced.

Kontrakt must lower version coordinates into canonical material where meaning can evolve, be compared, rejudged, or
published across time.

### 12.15. Policy Contract

Policy declares which judgment criteria are active under a machine context.

A policy is not an implementation option bag. It decides which declared law applies.

Kontrakt may specialize backend paths for active policy material, but the specialization does not become the policy.

### 12.16. Budget Contract

Budget declares finite consumable allowance for a run, operation, stage, or diagnostic path.

A budget is not an incidental timeout or loop counter. It is a declared finite allowance.

Kontrakt may realize budgets through counters, primitive ledgers, static caps, generated debit paths, or compiler-side
sizing. The budget meaning remains canonical material.

### 12.17. Capacity Contract

Capacity declares the admissible limit of a machine, surface, stage, queue, or storage region.

Capacity is not merely an implementation buffer size. It says what the machine is allowed to admit, hold, process, or
publish under declared limits.

Kontrakt may realize capacity through preflight checks, static sizing, bounded queues, slabs, arenas, or storage plans.
Those structures do not own the capacity contract.

### 12.18. Governance Contract

Governance declares which contract set, policy set, version, capacity, budget, and manifest binding is valid.

Governance is the rule for which law book the machine is reading.

Kontrakt may realize governance through epochs, immutable snapshots, generated registries, pinned policy handles, or
compiler material indexes. Those mechanisms are realization, not governance authority.

---

## 13. Unresolved

This ADR does not decide the final authoring syntax for one-dimensional contract bodies.

This ADR does not decide the final host user API.

This ADR does not decide whether V1 uses explicit generated helpers, generated adapters, host-language declaration
surfaces, compiler plugins, or several of them.

This ADR does not decide the final predicate language, predicate grade, expression subset, or verifier IR.

This ADR does not decide the final source carrier set for every one-dimensional presentation.

This ADR does not decide final structural airlock mechanics. Structural checking may be used inside a slot, but
structure
is not the selection law.

This ADR does not decide how much source naming, aliasing, or external encoding detail belongs to Input versus
Canonicalization.

This ADR does not decide whether annotations or host markers are ever accepted as optional acquisition hints. They are
not the primary selection mechanism.

---

## 14. Consequences

One-dimensional contract selection becomes pipeline-centered.

Kontrakt does not need to mark every user type. It does not need to scan all user data. It does not need a sidecar
mirror
for every contract body.

The declared slot gives the role. Source material enters through that role. Kontrakt acquires only selected material,
lowers it into flat canonical material, and realizes it through replaceable backend machinery.

The operation surface becomes readable as a contract pipeline. The reader can see which pieces are present, which pieces
are absent by declaration, and which piece may be replaced without pretending that a class body is the contract.

This gives responsibility a physical place. Input belongs in the input slot. Admission belongs in the admission slot.
State movement belongs on the state-machine axis. Publication belongs at publication. A design question becomes a slot
question before it becomes a code question.

That also makes authoring less empty. The author is not asked to start from a blank implementation and discover the
architecture by writing code. The pipeline already gives the shape of the work. Filling the slots, or explicitly leaving
them empty, produces the contract machine.

User-owned source remains user-owned. Host-language material may be used, but it must be cleaned before it carries
authority.

Kontrakt remains an adapter and a compiler-grade realization layer around the user's system. If it is removed, the user
loses what Kontrakt adds: verification, deterministic identity and ordering, generated gates, bounded diagnostics,
backend specialization, and optimization. The user's own contract source should still be the user's source.

This keeps ADR-0046's interface decision intact while opening a practical path for the remaining one-dimensional
presentations.
