# ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary

## Status

Accepted

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

A `.kontrakt` interface manifest names operation handles. Kontrakt may generate an ordinary host interface from that
manifest, but the generated interface is only a build artifact. It is not the contract authority.

ADR-0046 also records the first one-dimensional catalog. It does not decide how those presentations are selected,
authored, acquired, or lowered. This ADR starts there.

The operation is the surface handle. It is not itself the pipeline. For each operation, the manifest exposes the places
where contract material belongs. Those places are slots.

This matters because Kontrakt has two bad roads in front of it.

One road makes the user's system wear Kontrakt. That happens when user-owned types must carry Kontrakt annotations,
implement Kontrakt marker interfaces, inherit Kontrakt shapes, or be assembled from Kontrakt objects before they can be
understood. The user may already have a contract in its own types, messages, schemas, records, and names. Kontrakt
should
not colonize that contract just to read it.

The other road asks Kontrakt to guess. That is not better. A project contains ordinary data, storage shapes, fixtures,
transport payloads, cache entries, and many values that are not contract material for a given operation. Shape can help
once a role is known. Shape cannot choose the role.

The missing mark is already present.

A slot marks the role. Material is input because it enters the input slot. Material is publication because it is judged
at
the publication slot. State movement belongs to the state-machine axis because the manifest says so, not because a host
method changed a field.

The mark is the slot, not the user's type.

This gives a practical authoring shape without turning Kontrakt into a pile of annotations or a second wiring language.
The manifest shows the operation and the empty places around it. The author fills those places, or explicitly leaves
them
empty. A contract can be replaced at a slot. Responsibility stays visible.

Kontrakt can then remain around the user's system as an adapter and compiler-grade realization layer. If Kontrakt is
removed, its checks, generated gates, deterministic backend paths, diagnostics, and optimizations disappear. The user's
own contract source should not have to be rewritten out of Kontrakt's shape.

---

## 2. Problem

One-dimensional presentations need a selection law.

Annotations and marker interfaces are convenient marks, but they put the mark on user-owned material. Host inheritance
and composition are worse because they let contract meaning hide inside host relation mechanics. A separate projection
file avoids touching the type, but it easily becomes bureaucratic wiring or a sidecar mirror. Structural discovery over
the whole project fails for a simpler reason: structure does not say role.

Kontrakt needs an explicit mark, but the mark must belong to the declared contract machine.

---

## 3. Decision Drivers

A one-dimensional role must come from the contract manifest, not from incidental host form.

User-owned source should stay user-owned. Kontrakt may acquire from it, but the user should not have to reshape the
system around Kontrakt types.

Authority must begin after acquisition, resolution, and lowering into Kontrakt-owned canonical material. Source
carriers,
helpers, compiler metadata, generated adapters, caches, and backend tables may help the machine. They do not get to own
the meaning.

Boilerplate removal is valuable only after that boundary is clear.

V1 should decide the selection law, not the whole user API.

---

## 4. Alternatives

### 4.1. `.kontrakt` syntax for every one-dimensional body

This keeps the source under one language, but it makes users learn a new surface for every small piece of contract data.
Most one-dimensional bodies do not need that much ceremony.

Decision: rejected as the only model.

### 4.2. Host annotations or marker interfaces

These are easy to find, but they move the mark into the user's material. The user's contract starts wearing Kontrakt.

Decision: rejected as the primary selection mechanism.

### 4.3. Host inheritance or composition

This gives the host language too many places to hide meaning.

Decision: rejected.

### 4.4. Sidecar projection mapping

This keeps user types clean, but it creates another document whose job is to remember that one thing means another. If
it
copies source facts, it becomes the mirror ADR-0046 avoided.

Decision: rejected as the primary selection mechanism.

### 4.5. Structural discovery over all user material

Shape is useful inside a known role. It cannot select the role.

Decision: rejected.

### 4.6. Pipeline-slot selection

The operation manifest already has the right mark. A slot says what role the material plays. After that, Kontrakt may
acquire, check, lower, or reject the material under that role.

Decision: accepted.

---

## 5. Decision

Kontrakt will use pipeline-slot selection for one-dimensional contract presentations.

The interface manifest names operation handles. For each operation, the manifest binds contract slots. A slot is the
selection point for a one-dimensional role.

A presentation is not selected because a host class has an annotation, a marker interface, an inheritance edge, a
mapping
entry, or a lucky shape. It is selected because the operation manifest places material in a declared slot.

Kontrakt acquires only material selected by declared slots or by slot-owned references. Unreferenced user material is
ignored.

This ADR does not decide the final user API for getting host material into a slot. That may later be done with generated
helpers, generated adapters, host declaration surfaces, compiler-plugin airlocks, or another ratified frontend. The law
is
unchanged: the slot gives the role; lowering gives the material; backend machinery realizes it.

---

## 6. Pipeline-Slot Selection Law

A pipeline slot is a declared position in an operation's contract axis or state-machine axis.

It is not a runtime scheduling statement. It is a role statement.

Input is not input because a DTO says so. Publication is not publication because a response object says so. A transition
is not a transition because a method rewrites a status field.

The operation gives the boundary. The slot gives the role. The source gives candidate material. Lowering gives Kontrakt
material.

---

## 7. Source Carrier Law

One-dimensional bodies are not required to use `.kontrakt` syntax in V1. That does not turn them into arbitrary host
code.

A contract source may be carried by a host language, but it must arrive as material the contract machine can read. It
may
name, shape, bound, version, choose, or declare absence. It may carry simple yes-or-no facts. That is about the edge.
Beyond that edge, behavior starts pretending to be contract.

A source carrier must not hide the contract inside a method, callback, constructor trick, runtime lookup, host control
flow, or opaque algorithm. If judgment logic is needed, it must be declared in a form Kontrakt can resolve and lower.
Predicate grade, helper APIs, and the future judgment language remain unresolved here.

The rule is not no host language. Kotlin/JVM may provide data classes, compiler metadata, generated source, or typed
declaration surfaces. They are carriers. Kontrakt reads them, cleans them, and lowers them. Their host mechanics do not
speak for the contract.

The carrier may help the author say the material. It does not decide the meaning.

---

## 8. Canonical Material Law

Authority begins after acquisition, resolution, and lowering into Kontrakt-owned canonical material.

That material must be deterministic. Identity, ordering, equality, version, absence, and failure meaning cannot depend
on
source traversal accidents or host runtime mechanics.

A source carrier may be pleasant to write. That does not make it the contract.

The contract-bearing result is the lowered material that Kontrakt owns.

---

## 9. Backend Realization Boundary

Kontrakt may remove boilerplate only after role selection and lowering are controlled.

After that, the backend may do its job. It may generate code, specialize paths, cache compiler material, arrange memory,
precompute stable data, and choose a faster physical layout.

That is machinery. It is allowed to change when the contract material does not change.

If generated machinery disagrees with canonical material, the machinery is wrong.

---

## 10. Host Boundary Airlock

The slot gives the role. A host boundary airlock may make the host compiler accept the crossing.

Kotlin/JVM is nominal. It will not accept every structurally suitable object just because a Kontrakt slot could read it.
If Kontrakt wants less user boilerplate, it needs generated boundary machinery or a compiler-backed form.

V1 may use explicit helpers or adapters. A later compiler plugin may verify call sites and lower direct host calls. This
ADR does not choose that API.

The user type is not the mark. The airlock is machinery. The lowered material is the contract-bearing result.

---

## 11. Common Processing Model

Each one-dimensional presentation follows the same authority path: a declared slot selects source material or declared
absence; Kontrakt acquires it under the slot role, resolves it, closes explicit absence and defaults, lowers it into
canonical material, and realizes it through generated or static machinery when needed.

This is not a physical runtime schedule. It is the path by which authority leaves source form and becomes Kontrakt-owned
material.

A backend may realize the path differently when the same contract is preserved.

---

## 12. One-Dimensional Presentations

ADR-0046 already decides the Interface Surface Contract. The remaining presentations use the slot law defined here.

### 12.1. Input Contract

Input declares the presentation shape that may appear at an operation boundary. The input slot selects candidate
material;
lowering turns that material into stable input facts. Admission, canonical equivalence, core fact meaning, state
movement,
and publication are not decided here.

### 12.2. Admission Contract

Admission declares when boundary presentation may enter the contract pipeline. It produces declared admit, reject, or
stop
meaning. The final predicate grade and authoring form remain unresolved.

### 12.3. Canonicalization Contract

Canonicalization declares how admitted presentation becomes a stable system representation. It must cover equivalence,
the
representative, tolerated source drift, and failure when a stable representation cannot be produced.

### 12.4. Lowering Contract

Lowering declares how canonical representation becomes core-readable machine material. It is not a helper conversion. It
is the point where external presentation stops being raw.

### 12.5. Fact Contract

Fact declares what factual material may exist inside the core. A fact is not a DTO, database row, runtime instance, or
host value with a nicer name. It belongs to the contract machine's own vocabulary.

### 12.6. Invariant Contract

Invariant declares whether lowered candidate material may become accepted core material. If an operation has no
additional
invariant, that absence must be declared.

### 12.7. State Contract

State declares finite, closed, flat machine conditions. It is not a mutable field, lifecycle label, callback phase, or
open vocabulary.

### 12.8. State Transition Contract

State transition declares permitted one-way movement between declared conditions. It is not every stored-value rewrite.
Hidden transition creation through host behavior is rejected.

### 12.9. Explicit State Machine Manifest

The explicit state machine manifest declares the closed movement surface: states, initial condition, terminal
conditions,
and permitted transitions. It belongs to the state-machine axis, not to a normal linear stage.

### 12.10. Failure Contract

Failure declares contract-governed stop results. It is not just an exception. Host errors may be mapped only through a
declared failure law.

### 12.11. Publication Contract

Publication declares whether accepted material may leave the machine as a public claim. Serialization may realize that
claim; it does not decide the claim.

### 12.12. Diagnostic Evidence Contract

Diagnostic evidence declares what explanation a judgment may offer. It exists because the machine must account for its
own judgment, not because logging is convenient.

### 12.13. Diagnostic Retention Contract

Diagnostic retention declares what evidence may survive after the run. Existence is not retention.

### 12.14. Version Coordinate

Version coordinate declares which contract meaning governed a judgment, material, claim, or evidence. It is not a
release
tag convenience.

### 12.15. Policy Contract

Policy declares which judgment criteria are active under a machine context. It is not an implementation option bag.

### 12.16. Budget Contract

Budget declares finite consumable allowance. It is not an incidental timeout or loop counter.

### 12.17. Capacity Contract

Capacity declares what a machine, surface, stage, queue, or storage region is allowed to admit, hold, process, or
publish.
It is not merely a buffer size.

### 12.18. Governance Contract

Governance declares which contract set, policy set, version, capacity, budget, and manifest binding is valid. It is the
rule for which law book the machine is reading.

---

## 13. Unresolved

This ADR does not decide final authoring syntax for one-dimensional bodies, the final host user API, the predicate
language, the source carrier set for every presentation, or compiler-plugin airlock mechanics.

Structural checking may be used inside a slot, but structure is not the selection law.

Annotations and host markers may later be accepted as optional acquisition hints. They are not the primary selection
mechanism.

---

## 14. Consequences

One-dimensional contract selection becomes pipeline-centered.

Kontrakt does not need to mark every user type, scan every data shape, or maintain a sidecar mirror for every contract
body. The declared slot gives the role. Source material enters under that role. Lowering turns it into flat canonical
material. Backend machinery stays replaceable.

The operation surface becomes readable as a contract machine. The reader can see what is present, what is absent by
declaration, and what may be replaced without pretending that a class body is the contract.

This gives responsibility a place. A design question becomes a slot question before it becomes a code question.

That also makes authoring less empty. The author does not start from a blank implementation. The manifest shows the
places that need material. Filling them, or explicitly leaving them empty, produces the contract machine.

Kontrakt remains an adapter and compiler-grade realization layer around the user's system. Removing it removes what it
adds. It should not erase the user's own contract source.

This keeps ADR-0046's interface decision intact while giving the remaining one-dimensional presentations a practical
selection law.