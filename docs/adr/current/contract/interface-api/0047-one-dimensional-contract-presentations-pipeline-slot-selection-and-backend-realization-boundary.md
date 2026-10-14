# ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary

## Status

Accepted

## Date

2026-07-07

## Related

- `../../../../the-most-important-thing/what-contract-is.md`
- ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency
- ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary
- ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary
- ADR-0046: IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure

---

## Amendment

This ADR previously treated Lowering and Publication as special one-dimensional presentations whose selected
declarations produced retained generated realization ports that users implemented during machine assembly.

That model is removed.

A selected one-dimensional Contract is declarative Contract expression. Its source form may differ by Contract, but
selection never creates a user implementation SPI. Kontrakt resolves and establishes the declared meaning under the
owning Contract law, then realizes the required executable work through compiler and backend machinery.

The user-supplied realization boundary is Operation. ADR-0046 owns the generated external interface and Operation
realization surfaces. ADR-0070 owns verification and optimization of the admitted user Operation realization.

This ADR continues to own the common selection law: operation-local roles are selected explicitly through their declared
operation positions, interface-scoped roles are selected through explicit enclosing-interface declarations or bindings,
and no one-dimensional role is inferred from host structure or implementation behavior.

The detailed source grammar and semantic closure of each one-dimensional Contract remain owned by its current
per-Contract ADR.

The 2026-10-15 revision extends Explicit Absence to every 1D role, including Input and Output. It separates role
selection from the legality of omission and aligns frontend resolution with HIR and Establishment.

---

## 1. Context

ADR-0046 decides the interface contract frontend.

A `.kontrakt` interface names Operation handles and the explicit Contract material that governs the interface. Kontrakt
may generate ordinary host surfaces from that source, but generated host code is build material. It is not Contract
authority.

The one-dimensional catalog has since been split into Contract-specific ADRs. Those ADRs decide their own meaning,
source grammar, applicability, establishment law, and realization constraints. This ADR does not duplicate those
semantics. It decides how one-dimensional material receives a role in the enclosing interface and Operation surface.

The Operation is a declared business-computation handle, not the one-dimensional pipeline. Its manifest provides
explicit authoring positions for applicable Contract roles. These slots do not by themselves determine each role's
semantic owning scope.

Not every Contract is operation-local. `Policy`, `Governance`, `Budget`, and `Capacity` are bound at the enclosing
interface scope under their own laws because they coordinate the interface's closed Operation set. `Facts` and
`Invariants` are also declared at the enclosing interface scope as standing Core vocabulary and law. Their exact scope
and applicability remain owned by their current ADRs.

This matters because Kontrakt has two bad roads in front of it.

One road makes the user's system wear Kontrakt. That happens when user-owned types must carry Kontrakt annotations,
implement Kontrakt marker interfaces, inherit Kontrakt shapes, or be assembled from Kontrakt runtime objects before they
can be understood. The user may already have types, messages, schemas, records, and names. Kontrakt should not move
Contract selection authority into those host mechanics just to read them.

The other road asks Kontrakt to guess. That is not better. A project contains ordinary data, storage shapes, fixtures,
transport payloads, cache entries, helper values, and implementation objects that are not Contract material for a given
Operation. Shape can help once a role is known. Shape cannot choose the role.

The missing mark is already present in the declared machine.

A manifest slot names a declared role; resolution preserves its selection at the scope required by the owning law.
An enclosing interface declaration or binding marks an interface-scoped role. State movement belongs to the
State-Machine Axis because the Interaction Manifest binds the applicable movement material, not because a host method
changed a field. A Publication declaration receives its role through its explicit binding under Publication law, not
because a result object can be serialized. A Lowering declaration receives its role through its explicit binding under
Lowering law, not because implementation happens to copy matching values.

The mark belongs to the declared Contract machine, not to the user's runtime type.

This gives Kontrakt a practical authoring law without turning the system into a pile of annotations, callbacks, or a
second implementation wiring language. The author states Contract material in the source form owned by each
one-dimensional ADR. The interface and Operation authoring surfaces bind it for resolution at its applicable owning
scope.

Selection does not create implementation authority. Once selected, the declaration remains Contract expression. Kontrakt
resolves it, establishes the applicable Contract meaning, and realizes the required machine behavior. The only user
business realization boundary inside this interface model is the declared Operation.

---

## 2. Problem

One-dimensional presentations need a selection law.

Annotations and marker interfaces are convenient marks, but they put the mark on user-owned material. Host inheritance
and composition are worse because they let Contract meaning hide inside host relation mechanics. A separate projection
file avoids touching the type, but it easily becomes bureaucratic wiring or a sidecar mirror. Structural discovery over
the whole project fails for a simpler reason: structure does not say role.

A generated implementation interface for every selected one-dimensional Contract also fails. It turns Contract
expression into user callback implementation. A user-written validator, mapper, publication adapter, policy hook, or
other implementation can then become the practical source of meaning even though the IDL selected a Contract obligation.

Kontrakt needs an explicit role mark, but that mark must belong to the declared Contract machine. Runtime work required
by the selected Contract must remain compiler/backend realization unless the owning Contract itself explicitly defines a
different law.

---

## 3. Decision Drivers

A one-dimensional role selection must be explicit at the scope fixed by its owning Contract. An authoring slot or
interface declaration may express it; incidental host form cannot.

User-owned source should stay user-owned. Kontrakt may acquire supported declaration evidence from it, but the user
should not have to reshape ordinary implementation around Kontrakt marker types or callback interfaces.

A selected one-dimensional Contract is declarative source evidence. It is not a request for the user to implement the
selected Contract in Java or Kotlin.

Contract authority must not reside in source carriers, helpers, compiler metadata, generated adapters, runtime
objects, caches, or backend tables. Resolution forms HIR candidates; only the owning Contract judgment establishes
authoritative meaning.

Physical realization must remain replaceable. Kontrakt may generate evaluators, guards, value-formation paths,
materialization code, tables, specialized helpers, or other executable structures as long as they preserve the
established Contract meaning.

The user-supplied implementation extension point is Operation. That realization performs business computation over the
material lawfully handed to it. It does not implement the selected one-dimensional Contracts around it.

V1 should decide the common selection and realization boundary without taking source-grammar ownership away from the
per-Contract ADRs.

---

## 4. Alternatives

### 4.1. One `.kontrakt` body model for every one-dimensional Contract

This keeps every declaration under one language, but it forces one source grammar onto Contracts with different semantic
needs.

Some Contracts are naturally expressed in the interface IDL. Others currently use restricted host-language declaration
surfaces as source evidence. That choice belongs to the owning Contract ADR.

Decision: rejected as a universal authoring rule.

### 4.2. Host annotations or marker interfaces

These are easy to find, but they move the role mark into the user's material. The user's runtime type starts speaking
for the Contract.

Decision: rejected as the primary selection mechanism.

### 4.3. Host inheritance or composition

This gives the host language too many places to hide meaning.

Decision: rejected.

### 4.4. Sidecar projection mapping

This keeps user types clean, but it creates another document whose job is to remember that one thing means another. If
it copies authored Contract facts, it becomes the mirror ADR-0046 avoided.

Decision: rejected as the primary selection mechanism.

### 4.5. Structural discovery over all user material

Shape is useful inside a known role. It cannot select the role.

Decision: rejected.

### 4.6. Explicit Contract-scope selection

The operation manifest provides an authoring position for applicable roles. The enclosing interface provides
positions for its standing Core and shared roles. Resolution preserves each selection at the semantic scope set by its
owning Contract law, which also controls the permitted source declaration.

After explicit selection, Kontrakt may acquire, resolve, establish, verify, reject, and realize the material under that
role. No implementation callback is needed merely because the Contract requires executable work.

Decision: accepted.

---

## 5. Decision

Kontrakt will use explicit declared scope as the selection law for one-dimensional Contract presentations.

Applicable roles are authored through explicit manifest positions or enclosing-interface declarations. Resolution
preserves each role selection at its exact owning Interface, Interaction, Operation, or other scope under the relevant
Contract law.

The interface declares a closed set of Interaction and Operation surfaces independently of optional role bindings.
The enclosing interface binds material applicable to the shared Core or closed Operation set under its owning laws.

A presentation is not selected because a host class has an annotation, a marker interface, an inheritance edge, a
mapping entry, a matching name, or a convenient shape. It is selected because the authored Contract surface explicitly
binds it at the role and scope where its owning law applies.

Kontrakt acquires only material reachable from those explicit selections and from references lawfully owned by them.
Unreferenced user material is ignored as Contract source.

Every applicable 1D role, including Input and Output, resolves to an exact selected binding or Explicit Absence at
its owning scope. Absence records non-selection; its legality belongs to the applicable declaration and composition
laws. It is not a missing Binding Candidate, failed lookup, incomplete resolution, or satisfied Required Basis.
No default or inferred Contract fills an absent role. A named default applies only when explicitly selected under its
owning law.

The source grammar of the selected declaration remains owned by its current per-Contract ADR. This ADR does not require
Lowering, Publication, Invariant, Admission, Policy, or another one-dimensional Contract to share one physical authoring
form.

A selected one-dimensional Contract does not create a user implementation interface.

```text
selected one-dimensional Contract
    -> acquire supported source evidence
    -> resolve Definition and Binding Candidates in HIR
    -> owning Contract judgment and Establishment
    -> compiler/backend realization
```

The user-supplied realization boundary is separate:

```text
declared Operation
    -> generated Operation realization surface
    -> user business implementation
```

ADR-0046 owns that generated host boundary. This ADR only establishes that one-dimensional selection does not create
additional user implementation SPIs beside it.

An illustrative selection surface is:

```text
interface CalculateContract {
    policy        DefaultPolicy
    governance    DefaultGovernance
    budget        DefaultBudget
    capacity      DefaultCapacity
    facts         CalculateFacts
    invariants    CalculateInvariants

    operation calculate(command: CalculateCommand): CalculateRecorded {
        manifest {
            flow:
                input             CalculateInput
                canonicalization  CalculateCanonicalization
                admission         CalculateAdmission
                lowering          CalculateLowering
                publication       CalculatePublication
                output            CalculateOutput
        }
    }
}
```

The example selects both Input and Output; neither is mandatory for every Interaction. It shows selection, not
processing order or the source grammar of the selected declarations. Their owning ADRs define that grammar.

The user does not implement these declarations. They are Contracts selected into the machine.

---

## 6. Pipeline-Slot Selection Law

A pipeline slot is an explicit authoring position in the Operation manifest's Contract Axis or the corresponding
State-Machine Axis surface. It is not a runtime schedule or proof that every role belongs to the Operation. Resolution
preserves the selection at the owning scope, which may instead be the Interaction or enclosing Interface.

`Policy`, `Governance`, `Budget`, and `Capacity` are not ordinary operation-local pipeline slots. Their applicable
bindings are made at the enclosing interface scope under their current laws. `Facts` and `Invariants` are also standing
interface-scope declarations rather than per-Operation implementation hooks.

Input is not Input because a DTO says so. Publication is not Publication because a response object says so. A Transition
is not a Transition because a method rewrites a status field. Lowering is not Lowering because a helper happens to
convert compatible types.

The IDL resolves separate Interaction and Operation subjects. A slot names an authored role, while the applicable
Contract law fixes its owning scope. The selected declaration supplies source evidence, not established authority.

Several Operation interactions under one enclosing interface may enter the same Core. Internal functions, helper
methods, stages, or call-graph nodes do not open another IDL Operation or create another slot board. They remain
replaceable realization unless another explicit Contract boundary says otherwise.

A slot is not a callback position.

```text
slot selection
    != generated user implementation point
```

If a selected Contract needs executable work, Kontrakt realizes that work after the applicable Contract meaning has been
resolved and established.

---

## 7. Source Carrier Law

One-dimensional Contracts are not required to share one V1 source carrier.

The owning Contract ADR decides whether its declaration is written directly in `.kontrakt`, carried by a restricted
Kotlin or Java declaration, produced by another supported frontend, or represented by another ratified source form.

A host-language declaration may carry Contract evidence, but it must arrive as material the Contract machine can read.
It may name, shape, bound, version, select, relate, or declare absence as permitted by the owning law.

The source carrier must not hide Contract meaning inside arbitrary methods, callbacks, constructor tricks, runtime
lookup, mutable object identity, host control flow, or opaque algorithms. If a judgment or transformation meaning is
required, the owning Contract must expose that meaning in a form Kontrakt can resolve and verify.

The rule is not no host language. Kotlin/JVM may provide data classes, compiler metadata, generated source, or typed
declaration surfaces. They remain carriers. Their host mechanics do not speak for the Contract.

The carrier may help the author state the material. It does not select its own role and it does not become a user
realization SPI.

---

## 8. HIR Candidate and Establishment Boundary

Selection makes source evidence relevant, not authoritative. Kontrakt resolves that evidence into exact HIR Definition
and Binding Candidates under ADR-0071. The owning Contract law judges the applicable candidates and establishes
Contract meaning under ADR-0063. Compiler materialization does not supply authority.

Identity, ordering, equality, Version, absence, dependency, and failure meaning must not depend on source traversal
accidents or host runtime mechanics. Source carriers, generated declarations, and backend tables remain
non-authoritative
regardless of their convenience.

The authoritative result is the applicable Contract material established under its owning law.

---

## 9. Backend Realization Boundary

Kontrakt may remove boilerplate only after role selection, resolution, and Contract authority are controlled.

A selected one-dimensional Contract does not require a user-supplied validator, mapper, adapter, policy hook,
canonicalizer, publication implementation, or other per-Contract callback merely because its meaning must be executed.

Instead:

```text
declared Contract expression
    -> resolved HIR candidates
    -> owning judgment and applicable Establishment
    -> compiler-derived realization knowledge
    -> backend executable realization
```

The backend may generate a direct predicate, material-formation path, projection, accounting path, capacity gate,
diagnostic producer, static table lookup, specialized helper, or another structure appropriate to the owning Contract.
That structure is implementation material.

A backend may also fuse compatible work, statically discharge a judgment, inline generated helpers, eliminate temporary
objects, specialize primitive paths, cache compiler results, or choose a different physical layout. Those changes are
legal only when the same Contract meaning and observable boundary are preserved.

An internal generated function or ABI may exist when useful. It is not a retained user implementation requirement and it
must not become a second authority source.

If the selected Contract declaration is not semantically sufficient to determine a legal realization under its own law,
Kontrakt must not ask user implementation to fill the missing meaning implicitly. The definition must be rejected or the
owning Contract language must be extended explicitly.

If the Contract is semantically complete but the selected backend cannot preserve its required guarantee, realization
must fail under the applicable backend/Contract law. Backend inability does not weaken the Contract.

The only ordinary user-supplied business realization boundary in this interface model is Operation.

```text
established inbound material
    -> user Operation realization
    -> candidate result material
```

That Operation realization is verified and admitted under ADR-0070. It does not acquire authority over the
one-dimensional Contracts that surround it.

If generated machinery disagrees with established Contract material, the machinery is wrong.

---

## 10. Host Boundary Airlock

The declared Contract surface gives the role. A host boundary airlock may make the host compiler accept the crossing.

Kotlin/JVM is nominal. It will not accept every structurally suitable value merely because Kontrakt can describe the
same Contract coordinates. Generated carriers, generated host interfaces, compiler-backed checks, adapters, or direct
backend products may therefore be needed at integration boundaries.

Those mechanisms are machinery. They do not select Contract roles and they do not create Contract meaning.

V1 may use explicit generated boundary surfaces where the owning frontend requires them. The external interface and the
Operation realization surface are owned by ADR-0046. One-dimensional Contracts may also have generated compiler support
internally, but selection of a Contract does not expose another user implementation interface.

The user type is not the mark. The airlock is not the authority. The established Contract material remains the source of
machine meaning.

---

## 11. Common Processing Model

One-dimensional Contracts share a common authority discipline even though their detailed semantics differ.

Conceptually:

```text
supported source evidence
    -> explicit authoring and scope-correct role selection
    -> acquisition and resolution
    -> visible HIR candidates and complete selection / Explicit Absence
    -> owning judgment, applicability, and Establishment
    -> compiler/backend realization
```

The diagram is not a physical runtime schedule.

An owning Contract may perform a definition-time judgment, a run-time judgment, material formation, selection,
accounting, retention, or another obligation. This ADR does not flatten those differences into one evaluator protocol.

`Policy`, `Governance`, `Budget`, `Capacity`, standing `Facts`, and `Invariants` retain their scope under their
owning laws. Flow, movement, bounds, and diagnostic authoring positions do not override the semantic selection scope
of any role.

The common rule is narrower: source evidence does not select itself, user implementation does not fill a selected
one-dimensional Contract, and backend realization cannot change established meaning. An unselected role remains
Explicit Absence; a required but absent role is judged under the law that requires it.

---

## 12. One-Dimensional Presentations

ADR-0046 owns the Interface Surface Contract and generated external/Operation host split. The per-Contract ADRs
own the detailed meanings below. This catalog records interface-visible obligations; §12.15 records Version only as
a cross-cutting Contract coordinate, not as another 1D role.

### 12.1. Input Contract

Input declares the finite presentation shape that may appear at the inbound boundary. It does not itself grant
continuation, canonicalize presentation values, form Operation parameter Facts, or invoke the user Operation.

### 12.2. Admission Contract

Admission owns the continuation judgment over the applicable inbound Presentation. After successful selected
Canonicalization, it judges the final Presentation supplied by inbound composition; when Canonicalization is absent,
it judges the Input-established Presentation. Its Required Basis remains governed by its own law.

### 12.3. Canonicalization Contract

Canonicalization defines and judges the declared representative of an Input-established Presentation. Its successful
result precedes Admission, and it does not perform shape-changing Operation parameter formation. Omission establishes
neither an identity law nor a Canonicalization Occurrence.

### 12.4. Lowering Contract

Lowering declares the explicit lawful relation by which selected boundary-presentation coordinates may form declared
Operation-parameter Fact coordinates.

The declaration is Contract expression. It is not a user mapper. Kontrakt resolves the relation and the backend realizes
the actual value formation required by the established Lowering law.

### 12.5. Fact Contract

Fact declares what immutable factual material may exist with Fact authority inside the Core. A host object, DTO,
database row, implementation-local value, or runtime identity does not become a Fact merely because user code can reach
it.

### 12.6. Invariant Contract

Invariant declares a standing Fact-local integrity law. It is judged under its own applicability law and does not become
a general user validation callback.

### 12.7. State Contract

State declares finite, closed, flat machine conditions on the State-Machine Axis. It is not a mutable field, lifecycle
label, callback phase, or open host vocabulary.

### 12.8. State Transition Contract

State Transition declares permitted one-way movement between declared machine conditions. Hidden movement inferred from
host behavior is not Contract movement.

### 12.9. Explicit State Machine Manifest

The Explicit State Machine Manifest declares the closed movement surface: the State set, establishment conditions, and
permitted Transition membership required by its owning State-Machine law.

### 12.10. Failure Contract

Failure declares explicit Contract-governed failure meaning, attribution, applicable context, and stopped scope. Host
exceptions or backend failures do not acquire that meaning merely because they interrupted execution.

### 12.11. Publication Contract

Publication declares which established Result or Failure material receives outward exposure authority.

Publication is positive outward selection, not a user mapper and not Output shape. A backend realizes an
already-resolved Publication decision without gaining authority to widen it.

### 12.12. Output Presentation Contract

Output declares the closed outward result shape that may leave the machine after applicable Publication authority has
been established. Output does not infer its shape from a serializer, response class, Operation return object, or backend
convention.

### 12.13. Diagnostic Evidence Contract

Diagnostic Evidence declares what Contract-owned explanation material may be established for a declared judgment. It
exists because the machine must be able to account for its own decisions, not because logging is convenient.

### 12.14. Diagnostic Retention Contract

Diagnostic Retention declares what diagnostic evidence may remain after the run and under what bounds. Evidence
existence does not imply retention.

### 12.15. Version Coordinate (Not a One-Dimensional Presentation)

Version belongs to each independently versioned Contract Authority under ADR-0053. It identifies the governing
Contract meaning for a judgment, material, claim, or evidence. It is not a separately selected 1D role or an
interface-level release tag.

### 12.16. Policy Contract

Policy declares the explicit operating world and the applicable criteria that govern the bound interface machine under
its current Contract law. It is not an implementation option bag.

### 12.17. Budget Contract

Budget declares finite consumable allowance and its explicit attribution or allocation under the applicable Contract
world. It is not an incidental timeout, loop counter, or backend limit.

### 12.18. Capacity Contract

Capacity declares finite simultaneous operating limits and applicable admission walls over the subjects and quantities
owned by its current Contract law. It is not merely a buffer size or implementation-stage quota.

### 12.19. Governance Contract

Governance declares the explicit selection, scope, binding, validity, and singularity material that determines which
Contract world is valid for the governed machine. It does not infer authority from implementation topology.

---

## 13. Unresolved

This ADR does not decide one universal source syntax for one-dimensional Contract bodies.

Each current per-Contract ADR owns its supported source evidence, definition-time closure, applicability, and semantic
realization requirements. Those decisions may use `.kontrakt`, restricted host declarations, generated declaration
surfaces, or another ratified frontend without changing the selection law defined here.

This ADR also does not decide the final compiler-plugin airlock mechanics, internal backend ABI, generated helper
layout, DI framework integration, or optimization representation.

Structural checking may be used after a role is explicitly known. Structure is not the selection law.

Annotations and host metadata may be accepted as source evidence or acquisition hints only where an owning Contract ADR
permits them. They do not become the primary role-selection mechanism.

No unresolved implementation detail reopens a user implementation port for a selected one-dimensional Contract.

---

## 14. Consequences

One-dimensional Contract selection becomes explicit-scope-centered.

Applicable roles are authored through declared positions and preserved at their owning semantic scopes. Standing
Core and shared roles retain their enclosing-interface bindings under their current laws. Host structure does not
select a role.

The source grammar of each Contract can evolve independently without changing the common selection law. Lowering may use
its explicit relation form. Publication may use its own positive exposure declaration. Invariant, Admission,
Canonicalization, Budget, Capacity, Governance, Failure, Diagnostics, and the other Contracts retain the source form and
semantic law owned by their current ADRs.

Selected one-dimensional Contracts remain declaration, not user implementation SPI.

```text
user declares Contract meaning
    -> Kontrakt resolves HIR candidates
    -> owning Contract law establishes applicable meaning
    -> compiler/backend realizes it

user implements Operation
    -> Kontrakt verifies and admits that realization
```

This keeps Contract authority out of validators, mappers, publication adapters, framework hooks, and generated callback
interfaces.

It also makes the interface surface easier to read. The reader can see selected and explicitly absent roles in their
owning scopes without confusing Contract declarations with runtime object wiring. An unresolved or incomplete
declaration is not Explicit Absence.

The author therefore does not begin from a blank set of implementation callbacks. The declared surface shows where
Contract material may be supplied and which roles are unselected. Responsibility stays visible without turning the
slot board into executable wiring.

The cost moves deliberately into the compiler/backend. Kontrakt must be able to realize the executable consequences of
selected Contract declarations, reject declarations that are semantically incomplete under their owning law, and reject
backend combinations that cannot preserve the required guarantee.

Backend implementation remains free to fuse judgments, eliminate intermediate values, specialize direct paths, cache
resolved knowledge, choose compact layouts, or generate JVM-facing executable structures. None of those choices can add,
remove, or redefine selected Contract meaning.

The user's business code remains concentrated at Operation. Internal helper functions and ordinary implementation
structure remain replaceable realization unless another explicit Contract boundary applies.

This preserves ADR-0046's frontend split while giving the one-dimensional catalog one common selection and realization
boundary. The Contract is declared once. Kontrakt realizes the machine around the Operation rather than asking the user
to reimplement each selected Contract in host code.