# ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary

## Status

Accepted

## Date

2026-07-05

## Related

- `docs/what-contract-is.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- `docs/todo/release-readiness-todo.md`
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0025: Interface-First Design and Test Interface Pattern

---

## 1. Context

Kontrakt is moving from a test-framework-shaped system toward an explicit contract machine.

`What Contract Is` places contract authority outside host-language implementation mechanics. The frontend must preserve
that boundary while remaining small enough for ordinary use.

The frontend has one job: make the contract machine visible to the user, then let Kontrakt lower that surface into
canonical material. Users declare the contract shape. Kontrakt owns the machine-facing representation and its execution
consequences.

The key correction is this:

```text
Interface = shared machine bindings + standing core bindings + operation handles + operation-local bindings
```

A method alone is only an operation handle. An interface becomes a Kontrakt interface when it declares a closed set of
operations, binds the shared `Policy`, `Governance`, `Budget`, and `Capacity` contracts once at the enclosing interface
scope, declares its `Facts` and `Invariants` once for the same explicit core, and binds each operation to an explicit
pipeline manifest and its operation-local Lowering and Publication relations.

This ADR decides how that interface is authored, how host-language code receives a usable interface and retained
realization ports, and where authority moves after lowering.

---

## 2. Problem

Kontrakt needs a frontend that exposes the explicit contract machine without returning authority to host-language
mechanics.

A handwritten Kotlin or Java method signature identifies a call, not the machine that makes the call a contract.

Putting Kontrakt syntax inside a user's handwritten host interface is also wrong. If removing Kontrakt invalidates that
source, Kontrakt has become part of the user's own contract surface.

A sidecar mirror is not sufficient either. It makes the same interface fact appear in two authored places and then asks
a compiler to police the drift.

Annotation carriers and string references fail for different reasons. The first routes the manifest through host runtime
type handles. The second hands meaning to lookup rules.

The frontend therefore needs one authored contract interface, ordinary generated host interfaces and realization ports,
and a lowering path that gives final authority to Kontrakt material.

---

## 3. Decision Drivers

The frontend must keep the contract interface, its shared `Policy`, `Governance`, `Budget`, and `Capacity` bindings,
the contract axis of each operation, and the explicit movement axis visible in one authored surface.

The generated host interface and generated realization ports must remain ordinary JVM/Kotlin code. They exist for
implementation and calling, not for contract authority.

The vocabulary must come from the Kontrakt pipeline. Stage names carry the judgment role, so Design-by-Contract terms
are not the primary frontend model.

References must begin as source symbols and end as Kontrakt-owned material. Runtime host handles and string lookup
cannot own identity.

V1 must stay narrow. The IDL owns the interface manifest and the exact coordinate relations declared by Lowering and
Publication. This ADR records the initial one-dimensional contract catalog, but it does not decide the final authoring
form for the other presentations.

Machine sympathy belongs to the backend. The user-facing surface declares meaning; Kontrakt owns the physical form and
may lower declared material into compiler-grade realization paths.

---

## 4. Alternatives

### 4.1. Handwritten host interface plus top-level Kotlin `InteractionManifest`

```kotlin
interface CalculateContract {
    fun calculate(input: CalculateInput): CalculateOutput
}

val CalculateManifest = InteractionManifest(
    operation = CalculateContract::calculate,
    admission = XGreaterThanOne,
    publication = ResultGreaterThanInput,
    governance = DefaultGovernance,
)
```

This is easy to write and benefits from Kotlin tooling.

It is not the primary frontend because the interface and the manifest are authored as separate facts. The interface does
not itself present the contract machine.

### 4.2. Method annotation manifest

```kotlin
interface CalculateContract {
    @InteractionManifest(
        admission = [XGreaterThanOne::class],
        publication = [ResultGreaterThanInput::class]
    )
    fun calculate(input: CalculateInput): CalculateOutput
}
```

This keeps the binding near the method, but it carries the manifest through annotation and class-reference mechanics.

### 4.3. Interface-local Kotlin manifest value

```text
interface-local manifest value inside the host interface
```

This keeps the text nearby, but it gives host getter/default-body mechanics a path back into contract authoring.

### 4.4. Handwritten host interface plus `.kontrakt` sidecar mirror

```text
contract interface CalculateContractPresentation
    presents com.example.CalculateContract {

    operation calculate(input: CalculateInput): CalculateOutput {
        pipeline { ... }
    }
}
```

This avoids modifying a handwritten host interface, but it still mirrors the same operation surface in another authored
file.

### 4.5. IDL-first Kontrakt interface contract

A `.kontrakt` file is the authored interface contract source. Kontrakt compiles it into an ordinary host-language
interface artifact.

Decision: accepted.

---

## 5. Decision

Kontrakt will use an IDL-first interface contract frontend.

A `.kontrakt` interface contract is source material. It is not a mirror of a handwritten host interface.

Kontrakt compiles that source into a plain host-language operation interface and plain host-language realization ports.
Implementations realize those generated surfaces. The generated sources are retained reproducible build artifacts, not
contract authority, and users must not hand-edit them.

The IDL declares a closed set of interface operations and binds each operation to an explicit contract axis and an
explicit state-machine movement axis. The enclosing interface binds `Policy`, `Governance`, `Budget`, and `Capacity`
once because those contracts coordinate the finite resources and decisions shared among its operations. It also binds
`Facts` and `Invariants` once for the same explicit core. None of those interface-scoped declarations is repeated inside
an operation manifest. Each operation declares its Lowering and Publication beside its manifest, writes their exact
source-to-target coordinate relations directly in the interface IDL, and produces a generated realization port for each
selected Lowering or Publication declaration.
One-dimensional contract presentations are otherwise named as closed obligation kinds by this ADR, but their final
authoring form remains unresolved. The implementation axis is produced behind that surface and carries no authority.

References in the IDL are compile-time source symbols, not host-runtime handles or lookup names.

Short form:

```text
.kontrakt interface contract
-> generated host operation interface and realization ports
-> implementations realize generated host surfaces
-> retained generated source remains ordinary compatibility material
-> canonical contract material owns authority
```

---

## 6. IDL-First Interface Law

The authored interface contract lives in `.kontrakt` source.

The generated Kotlin or JVM operation interface and realization ports are outputs of the contract compiler. They let
implementations and callers use ordinary host-language tooling.

The generated artifacts must not become the authority. If generated source and canonical material disagree, the
generated source is wrong.

The IDL source is also not final authority. Authority begins only after it has been resolved and lowered into
Kontrakt-owned material.

---

## 7. Generated Host Interface and Realization Port Boundary

Generated host interfaces and realization ports are compatibility surfaces.

They may be committed, inspected, implemented, and called as ordinary Kotlin or JVM code. They must not contain hidden
contract behavior.

The implementation boundary is therefore ordinary:

```kotlin
// GENERATED CODE - DO NOT MODIFY
interface CalculateContract {
    fun calculate(input: CalculateInput): CalculateOutput
}
```

A realization candidate may implement it in ordinary Kotlin:

```kotlin
class CalculateService : CalculateContract {
    override fun calculate(input: CalculateInput): CalculateOutput {
        // implementation computes only inside the boundary controlled by Kontrakt
        TODO()
    }
}
```

The `override` here belongs to host-language realization. It is not part of contract authoring authority.

Lowering and Publication realization ports are generated from exact IDL coordinate relations. They use ordinary host
types and must not expose Kontrakt runtime wrappers, compiler-internal identities, or hidden contract behavior. Their
implementations remain replaceable host-language adapters. A backend may analyze, specialize, inline, or erase a
verified
port call in the final physical path, but that optimization does not remove the explicit source-level implementation
boundary. This ADR does not decide the final machine-assembly or implementation-binding protocol.

---

## 8. Interface Manifest Law

An interface is the software-visible contract presentation for interaction.

A method is only an operation handle. The interface earns contract status when that handle is bound to explicit contract
and movement material.

The frontend shape is:

```text
interface contract
    policy
    governance
    budget
    capacity
    facts
    invariants

    operation handle
        flat operation manifest
            flow
            movement
            bounds
            diagnostics

        lowering relation
        publication relation
```

The interface bindings declare the shared machine world and standing core laws. The method gives the handle. The
manifest selects the operation-local contract and movement material. The Lowering and Publication relation bodies are
owned by that same operation and remain structurally beside the manifest rather than inside it.

The operation manifest is a slot board. The slot names on the left are IDL keywords, not user-defined labels. The author
supplies material on the right. This keeps the operation shape visible without asking the user to invent the shape
again. `Policy`, `Governance`, `Budget`, and `Capacity` are not operation-manifest slots. They are bound once for the
interface's closed operation set, and their declarations may express machine-wide limits together with explicit
operation allocations or run-grant profiles.

Lowering and Publication are the only one-dimensional authoring exceptions fixed by this ADR. Their exact coordinate
relations are written directly in the interface IDL as `source -> target`. Each declaration belongs to the operation in
which that movement occurs and is written beside that operation's manifest at the same structural level. The arrow
declares an allowed factual formation relation. It does not declare assignment, automatic copying, implicit conversion,
or physical implementation. Every selected Lowering or Publication declaration produces a generated realization port
through which exactly its declared relations may be implemented. No relation is satisfied implicitly by same-type
copying, catalog lookup, or backend convention.

The four regions are there for visibility, like areas on a game board. They have no contract meaning of their own. They
do not create parent contracts, nested structure, processing order, or shared authority. Kontrakt still lowers each
bound
presentation into its own material.

---

## 9. Three Pipeline Axes

Every operation in the interface contract must bind explicit pipeline material, but Kontrakt does not treat the
operation as one linear implementation flow.

An operation has three axes.

The first axis is the contract pipeline. This is the authority axis. It declares the logical obligations that make the
operation a contract. Its positions are contract positions, not implementation steps.

The second axis is the implementation pipeline. This is the realization axis. It follows the contract pipeline like a
mirror image, but it has no authority. It may be generated, fused, split, specialized, replaced, or optimized as long as
the declared contract material remains unchanged.

The third axis is the state-machine pipeline. State and transition already belong to the contract world, but once they
are made explicit, they form their own movement surface beside the contract pipeline. This axis declares which machine
condition is active, which move is legal, and where movement must stop.

These axes must not be collapsed.

```text
contract pipeline:
    declares obligation and judgment authority

implementation pipeline:
    realizes the contract and remains replaceable

state-machine pipeline:
    declares legal movement through explicit machine conditions
```

The user-facing interface contract declares the contract axis and the state-machine axis. Lowering and Publication
implementations enter the implementation axis only through their generated realization ports. Kontrakt remains free to
build, fuse, specialize, replace, or optimize the physical implementation axis behind the declared contract material.

At the authoring surface, the operation manifest may be grouped for readability. `Flow` carries the material path.
`Movement` carries the state surface. `Bounds` carries the operation's version coordinate. `Diagnostics` carries
explanation and retention. These names do not create another axis, and `bounds` is not an operation stage. The shared
`Policy`, `Governance`, `Budget`, and `Capacity` bindings remain at the enclosing interface scope.

The stage names used in the contract axis are contract vocabulary, not a physical schedule. A backend may use any
equivalent structure as long as the declared obligation remains intact.

If a manifest slot or required standing interface declaration is left empty at the authoring surface, Kontrakt must
fill it with the declared default for that position before lowering. The user may omit ceremony; the compiler may not
leave absence implicit.

```text
invariants   NoAdditionalInvariant
state        Stateless
transitions  none
```

Declared absence is contract material. Hidden absence is not.

---

## 10. One-Dimensional Contract Catalog

This ADR does not decide the final authoring form for one-dimensional contract presentations except for the exact
coordinate relations owned by Lowering and Publication.

Authoring syntax for the other presentations comes later. Lowering and Publication use only exact `source -> target`
coordinate relations in this frontend. First, Kontrakt names the closed obligation kinds an interface contract can bind.
A one-dimensional presentation declares one obligation kind before the enclosing interface or an operation manifest
binds it according to that obligation's scope.

The initial catalog is:

```text
Interface Surface Contract:
    declares the public reliance surface of an interface contract

Input Contract:
    declares what presentation shape may appear at the boundary

Admission Contract:
    declares when boundary presentation may enter the contract pipeline

Canonicalization Contract:
    declares equivalence, the system-owned representative, tolerated source drift, and failure when stable
    representation cannot be produced

Lowering Contract:
    declares which selected Input coordinates may serve as factual formation sources for which Operation input Fact
    coordinates

Fact Contract:
    declares what kind of factual material may exist inside the core

Invariant Contract:
    declares whether lowered candidate material may become accepted core material

State Contract:
    declares finite, closed, flat machine conditions that govern legal next moves

State Transition Contract:
    declares permitted one-way movement between declared machine conditions

Explicit State Machine Manifest:
    declares the state set, initial condition, terminal conditions, and permitted transitions of one movement surface

Failure Contract:
    declares contract-governed stop results

Publication Contract:
    declares which authorized source coordinates may form which outward presentation coordinates

Diagnostic Evidence Contract:
    declares what explanation may be offered by a declared judgment

Diagnostic Retention Contract:
    declares what evidence may remain after the run, how it is bounded, and what must be discarded

Version Coordinate:
    declares which contract meaning governed a judgment, material, claim, or evidence

Policy Contract:
    declares allocation, priority, and reaction criteria across a machine's closed operation set

Budget Contract:
    declares machine-wide consumable allowance and explicit operation or run-grant profiles

Capacity Contract:
    declares machine-wide finite resource walls and explicit operation allocations inside those walls

Governance Contract:
    declares which contract set, policy set, version, capacity, budget, interface binding, and operation set is valid
```

Except for the exact coordinate relations owned by Lowering and Publication, the catalog is not an authoring syntax
decision. It names the obligation kinds that Kontrakt must recognize across frontend, resolution, lowering,
verification,
and backend projection.

The catalog remains flat. A user-facing operation manifest may group operation-local presentations as `flow`,
`movement`, `bounds`, and `diagnostics`, but those groups only help the author read the operation. They do not compose,
inherit, or own the presentations inside them. `Policy`, `Governance`, `Budget`, and `Capacity` remain independent
one-dimensional contracts bound once at the enclosing interface scope for the closed operation set.

---



---

## 11. Illustrative Interface Shape

A v1 `.kontrakt` interface contract may group an operation manifest like this:

```text
interface CalculateContract {
    policy        DefaultPolicy
    governance    DefaultGovernance
    budget        DefaultBudget
    capacity      DefaultCapacity
    facts         CalculateFacts
    invariants    CalculateInvariants

    operation calculate(input: CalculateInput): CalculateOutput {
        manifest {
            flow:
                input             CalculateInput
                admission         XGreaterThanOne
                canonicalization  DefaultPrimitiveCanonicalization
                lowering          CalculateLowering
                publication       CalculatePublication
                failure           CalculateFailure

            movement:
                state             Stateless
                transitions       none
                machine           StatelessMachineManifest

            bounds:
                version           CalculateContractVersion

            diagnostics:
                evidence          CalculateDiagnostics
                retention         DefaultDiagnosticRetention
        }

        lowering CalculateLowering {
            value -> input.value
        }

        publication CalculatePublication {
            value -> result
        }
    }
}
```

This sketch shows the interface contract surface, its machine-wide bindings, its interface-scoped Fact vocabulary and
Invariant laws, and one operation containing a manifest beside its exact Lowering and Publication coordinate relations.
Inside the manifest, the left side is the fixed operation-slot vocabulary and the right side is the material bound to
each slot. `flow` is shown as ordered slots, not as transition arrows. `Policy`, `Governance`, `Budget`, `Capacity`,
`facts`, and `invariants` apply to the enclosing interface scope and are not inherited operation slots.

`facts CalculateFacts` declares the Fact vocabulary eligible for establishment in the interface's explicit core.
`invariants CalculateInvariants` declares the standing laws that govern Facts in that same core. Neither declaration is
repeated through an operation-manifest slot. The operation manifest selects `CalculateLowering` and
`CalculatePublication`, while their relation bodies remain beside the manifest because both movements occur at that
operation's boundary.

The Lowering and Publication arrows declare permitted source-to-target relations only. They do not declare assignment,
automatic copying, implicit conversion, or physical implementation. The generated Lowering and Publication ports retain
the replaceable implementation boundary for the actual representation work.

Movement is different. A real state move may be written as an arrow because the arrow is the declared transition itself.
For example:

```text
movement:
    state          OrderState

    transitions:
        Draft --submit--> Submitted
        Submitted --cancel--> Cancelled

    machine        OrderStateMachine
```

If no movement is written, the compiler lowers the default no-movement material. The source may stay small; the
canonical material must stay explicit.

The four manifest regions are not final syntax and do not change the one-dimensional catalog. The bound presentations
remain separate after resolution. The interface-level `Policy`, `Governance`, `Budget`, and `Capacity` contracts also
remain separate material even though they are bound once for the shared machine scope.

Kontrakt compiles the interface contract into a host operation interface and realization ports and lowers resolved
contract material into canonical form.

---

## 12. V1 Parser Scope

The v1 parser covers only the IDL interface contract subset.

It reads interface shape, shared `Policy`, `Governance`, `Budget`, and `Capacity` references, interface-scoped `Facts`
and `Invariants` references, operation shape, axis entries, operation-local Lowering and Publication coordinate
relations, source references, and source locations.

The parser stops before the deeper languages: predicate bodies, host expressions, policy, state-machine detail, other
one-dimensional authoring, composition, and editor tooling.

The frontend remains a contract interface notation, not a general programming language.

---

## 13. Generated Artifact Law

Generated host interfaces and realization ports are retained reproducible build outputs.

They must be regenerated from `.kontrakt` source and must not be manually edited. If generated files are committed for
consumer convenience, the committed files are still artifacts, not authority.

Removing Kontrakt removes regeneration, verification, contract-aware pipeline assembly, and compiler-owned
optimization. Retained generated host interfaces and realization ports, together with ordinary implementations compiled
against them, remain ordinary host-language compatibility code. Removing Kontrakt does not force users to maintain
Kontrakt syntax inside handwritten host interfaces, because this model has no handwritten host interface source for
generated contracts.

---

## 14. Consequences

The accepted frontend makes the interface contract explicit. One interface may declare a closed set of operation
pipelines that enter the same core, while `Policy`, `Governance`, `Budget`, and `Capacity` are bound once at that
machine
scope to coordinate their shared finite resources. Operation handles stay with their operation-local contract and
movement bindings. Internal core functions, stages, and call graphs remain implementation and do not create nested IDL
operations. Handwritten host-interface drift and host-runtime identity are rejected. The custom syntax stays narrow. The
one-dimensional catalog is recorded without freezing its authoring syntax except for the exact Lowering and Publication
coordinate relations. Their generated realization ports keep implementation explicit and replaceable while allowing
Kontrakt to verify, specialize, fuse, inline, or erase the physical call path after binding. If Kontrakt is removed, the
retained ports and their ordinary host-language adapters remain available even though contract-aware regeneration,
verification, assembly, and optimization disappear.