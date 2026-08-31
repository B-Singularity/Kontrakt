# ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary

## Status

Accepted

## Date

2026-07-05

## Related

- `docs/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
- `../../todo/release-readiness-todo.md`
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

The frontend has two jobs. It makes the contract machine visible to the user and lets Kontrakt lower that surface into
canonical material. It also turns open-ended system design into a finite set of visible questions. Users declare the
answers they know. Kontrakt owns the machine-facing representation and the verification, testing, enforcement,
diagnostic, and optimization consequences that those answers permit.

The key correction is this:

```text
Interface = closed operation handles
          + required Input and Output Presentation bindings
          + selected shared, standing-core, operation-local, and movement bindings
```

A method alone is only an operation handle. An operation becomes a valid Kontrakt Operation when its handle binds an
`Input Contract` and an `Output Presentation Contract`. Other contract and movement positions enrich that minimum
operation. When selected, shared `Policy`, `Governance`, `Budget`, and `Capacity` contracts are bound once at the
enclosing interface scope, `Facts` and `Invariants` are declared once for the same explicit core, and operation-local
slots bind the additional obligations required by that operation.

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

Writing the implementation and its tests is also difficult when the author does not yet know which obligations the
system must make explicit. A blank programming surface asks the author to invent the system and its proof strategy at
the same time. An all-or-nothing contract language merely moves that burden into a larger form.

The frontend therefore needs one authored contract interface, ordinary generated host interfaces and realization ports,
and a lowering path that gives final authority to Kontrakt material. It must expose the available contract positions as
questions, allow a useful operation to begin with only Input and Output Presentation, and let the author strengthen the
machine by answering additional questions when they matter.

---

## 3. Decision Drivers

The frontend must keep the contract interface and every selected shared, contract, and movement binding visible in one
authored surface without requiring every available position to be filled.

A valid minimum operation must require only an operation handle, an `Input Contract`, and an `Output Presentation
Contract`. Additional positions must be optional contract enrichment, not prerequisites for entering Kontrakt.

The slot board must guide authoring. Each slot must present one bounded design question, preserve unanswered positions
as explicit absence, and let Kontrakt derive only the verification, tests, enforcement, diagnostics, and optimization
supported by the answers actually declared.

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

The IDL declares a closed set of interface operations. Each operation binds one `Input Contract` and one `Output
Presentation Contract`; that minimum is sufficient to form a valid Kontrakt Operation and generate its ordinary host
boundary. The operation may then select additional contract-axis and state-machine-axis positions. Every selected
position adds only its declared authority. An unselected optional position does not invalidate the operation, does not
receive an inferred contract, and does not authorize Kontrakt to claim a guarantee that was not declared.

When selected, the enclosing interface binds `Policy`, `Governance`, `Budget`, and `Capacity` once because those
contracts coordinate the finite resources and decisions shared among its operations. It likewise declares selected
`Facts` and `Invariants` once for the same explicit core. None of those interface-scoped declarations is repeated inside
an operation manifest. Each operation that selects Lowering or Publication declares the corresponding relation beside
its manifest, writes the exact source-to-target coordinate relation directly in the interface IDL, and produces one
generated realization port for that selected declaration.

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
verified port call in the final physical path, but that optimization does not remove the explicit source-level
implementation boundary. This ADR does not decide the final machine-assembly or implementation-binding protocol.

---

## 8. Interface Manifest Law

An interface is the software-visible contract presentation for interaction.

A method is only an operation handle. The interface earns minimum contract status when that handle binds one explicit
Input Contract and one explicit Output Presentation Contract. Contract-axis and movement material beyond those boundary
presentations strengthens the operation but is not required for its existence.

The frontend shape is:

```text
interface contract
    selected shared machine bindings
        policy
        governance
        budget
        capacity

    selected standing core bindings
        facts
        invariants

    operation handle
        flat operation manifest
            flow
                input required
                output required
                selected flow positions

            selected movement positions
            selected bounds positions
            selected diagnostic positions

        selected lowering relation
        selected publication relation
```

The operation handle gives the ordinary callable surface. The manifest's required `input` and `output` slots select the
boundary presentations that make the minimum operation judgeable and callable. Selected interface bindings declare a
shared machine world or standing core laws. Selected operation-manifest slots add operation-local contract or movement
material. Lowering and Publication relation bodies are owned by the operation that selects them and remain structurally
beside the manifest rather than inside it.

The operation manifest is a slot board. The slot names on the left are IDL keywords, not user-defined labels. Each slot
is both a binding position and a bounded design question. The author supplies material on the right when that question
matters. This keeps the operation shape visible without asking the user to invent the shape, the development checklist,
and the test model from an empty file. `Policy`, `Governance`, `Budget`, and `Capacity` are not operation-manifest
slots. When selected, they are bound once for the interface's closed operation set, and their declarations may express
machine-wide limits together with explicit operation allocations or run-grant profiles.

Lowering and Publication are the only one-dimensional authoring exceptions fixed by this ADR. Their exact coordinate
relations are written directly in the interface IDL as `source -> target`. Each declaration belongs to the operation in
which that movement occurs and is written beside that operation's manifest at the same structural level. The arrow
declares an allowed factual formation relation. It does not declare assignment, automatic copying, implicit conversion,
or physical implementation. Every selected Lowering or Publication declaration produces a generated realization port
through which exactly its declared relations may be implemented. No relation is satisfied implicitly by same-type
copying, catalog lookup, or backend convention.

The four regions are there for visibility, like areas on a game equipment screen. They have no contract meaning of their
own. They do not create parent contracts, nested structure, processing order, or shared authority. Kontrakt still lowers
each bound presentation into its own material.

---

## 9. Minimum Operation and Guided Contract Enrichment

The minimum valid Kontrakt Operation is:

```text
operation handle
+ Input Contract
+ Output Presentation Contract
= valid Kontrakt Operation
```

This minimum lets Kontrakt generate the ordinary host interface, bind an implementation, and govern the declared input
and output boundary. It does not silently claim Admission, Canonicalization, Fact, Invariant, State, Transition,
Publication, Diagnostic, Version, Policy, Budget, Capacity, or Governance authority.

Every other position is optional enrichment. Selecting a position adds the declared contract material and allows
Kontrakt to derive the corresponding machine capability. More declared material gives the machine more knowledge, but no
position receives authority merely because a backend could guess a useful behavior.

```text
more declared contract material
    -> more machine knowledge
    -> stronger generated verification, testing, enforcement, diagnostics, and optimization
```

An unselected optional slot lowers to canonical explicit absence. It is not an unresolved reference, an invitation to
structural inference, or permission to insert a convenient law. A named default may apply only when the authored
contract explicitly selects that default.

The slot system is therefore also an authoring system. Software and tests are often difficult to construct because the
author does not yet know what must be decided, what may fail, what must always remain true, what movement is legal, or
what evidence should survive. The board does not merely name mechanisms. Input and Output ask what may enter and leave.
Admission asks what must be refused. Invariant asks what must remain true. Movement asks what may happen next.
Diagnostics asks what the machine must explain. Bounds asks where it must stop.

The slot board exposes those questions without requiring every answer at the start. The author may begin with Input and
Output Presentation, inspect the unanswered positions, and fill only the contracts needed by the desired system. Each
answer becomes material from which Kontrakt can generate code, checks, fixtures, properties, movement guards,
diagnostics, or optimized realization.

The frontend guides the author toward a richer machine. It does not make richness a condition of entry.

---

## 10. Three Pipeline Axes

Every operation has an ordinary callable boundary and the minimum Input and Output Presentation material. Additional
selected contract and movement material populates distinct axes, but Kontrakt does not require every optional position
and does not treat the operation as one linear implementation flow.

An operation may populate three axes to different depths.

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

The user-facing interface contract declares the selected contract-axis and state-machine-axis material. An operation
with no selected movement material grants no State-Machine authority; Kontrakt does not infer one from implementation
behavior. Lowering and Publication implementations enter the implementation axis only through their generated
realization ports when those relations are selected. Kontrakt remains free to build, fuse, specialize, replace, or
optimize the physical implementation axis behind the declared contract material.

At the authoring surface, the operation manifest may be grouped for readability. `Flow` carries the material path.
`Movement` carries the state surface. `Bounds` carries the operation's version coordinate. `Diagnostics` carries
explanation and retention. These names do not create another axis, and `bounds` is not an operation stage. The shared
`Policy`, `Governance`, `Budget`, and `Capacity` bindings remain at the enclosing interface scope.

The stage names used in the contract axis are contract vocabulary, not a physical schedule. A backend may use any
equivalent structure as long as the declared obligation remains intact.

The required `input` and `output` slots must be selected. Every other slot and interface-scope binding is optional
unless a selected contract explicitly requires another declaration. Kontrakt validates those declared dependencies
rather than requiring the whole catalog.

An unselected optional position becomes canonical explicit absence before lowering. The user may omit ceremony; the
compiler may not infer a contract, insert a hidden default, or claim the missing authority.

```text
input        required
output       required
invariant    unselected
movement     unselected
```

Declared absence is contract material. Hidden absence is not.

---

## 11. One-Dimensional Contract Catalog

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
    declares what presentation shape may appear at the inbound boundary

Output Presentation Contract:
    declares the closed outward presentation shape that an ordinary Operation may produce and an authorized public
    claim may occupy

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
verification, and backend projection.

The catalog remains flat. A user-facing operation manifest may group operation-local presentations as `flow`,
`movement`, `bounds`, and `diagnostics`, but those groups only help the author read the operation. They do not compose,
inherit, or own the presentations inside them. `Policy`, `Governance`, `Budget`, and `Capacity` remain independent
one-dimensional contracts bound once at the enclosing interface scope for the closed operation set.

---

## 12. Illustrative Interface Shape

A minimum v1 `.kontrakt` interface contract may contain only the required operation and boundary slots:

```text
interface CalculateContract {
    operation calculate(input: CalculateInput): CalculateOutput {
        manifest {
            flow:
                input   CalculateInput
                output  CalculateOutput
        }
    }
}
```

This is a valid Kontrakt Operation. Kontrakt can generate the ordinary host interface and bind the implementation
without pretending that Admission, Invariant, State, Publication, or another unselected obligation has been declared.

The same operation may be enriched as its requirements become explicit:

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
                output            CalculateOutput
                failure           CalculateFailure

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

This sketch shows the same required Input and Output Presentation slots together with selected machine-wide bindings,
interface-scoped Fact vocabulary and Invariant laws, and additional operation-local slots. Inside the manifest, the left
side is the fixed operation-slot vocabulary and the right side is the material bound to each selected slot. `flow` is
shown as ordered slots, not as transition arrows. `Policy`, `Governance`, `Budget`, `Capacity`, `facts`, and
`invariants`
apply to the enclosing interface scope when selected and are not inherited operation slots.

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

If no movement region is selected, the compiler lowers canonical absence of declared movement authority. It does not
infer State from a returned value, a method completion, a field named `status`, or backend behavior. An explicitly
selected
`Stateless` declaration remains available when the author needs to assert deliberate statelessness rather than merely
leave movement unspecified.

The four manifest regions are not final syntax and do not change the one-dimensional catalog. The bound presentations
remain separate after resolution. The interface-level `Policy`, `Governance`, `Budget`, and `Capacity` contracts also
remain separate material even though they are bound once for the shared machine scope.

Kontrakt compiles the interface contract into a host operation interface and realization ports and lowers resolved
contract material into canonical form.

---

## 13. V1 Parser Scope

The v1 parser covers only the IDL interface contract subset.

It reads interface shape, required Input and Output Presentation slot references, selected shared `Policy`,
`Governance`,
`Budget`, and `Capacity` references, selected interface-scoped `Facts` and `Invariants` references, operation shape,
selected axis entries, operation-local Lowering and Publication coordinate relations, slot occupancy, source references,
and source locations. Resolution records every unselected optional position as canonical explicit absence.

The parser stops before the deeper languages: predicate bodies, host expressions, policy, state-machine detail, other
one-dimensional authoring, composition, and editor tooling.

The frontend remains a contract interface notation, not a general programming language.

---

## 14. Generated Artifact Law

Generated host interfaces and realization ports are retained reproducible build outputs.

They must be regenerated from `.kontrakt` source and must not be manually edited. If generated files are committed for
consumer convenience, the committed files are still artifacts, not authority.

Removing Kontrakt removes regeneration, verification, contract-aware pipeline assembly, and compiler-owned optimization.
Retained generated host interfaces and realization ports, together with ordinary implementations compiled against them,
remain ordinary host-language compatibility code. Removing Kontrakt does not force users to maintain Kontrakt syntax
inside handwritten host interfaces, because this model has no handwritten host interface source for generated contracts.

---

## 15. Consequences

The accepted frontend makes the interface contract explicit without making the complete catalog a condition of entry. An
operation may begin with one Input Contract and one Output Presentation Contract. Additional interface bindings and
operation slots strengthen that machine only where the author selects them. The slot board exposes the questions that
usually remain scattered across implementation, tests, reviews, and conventions, so users can discover and declare the
system they want instead of inventing its structure from an empty code surface.

One interface may declare a closed set of operation pipelines that enter the same core. When selected, `Policy`,
`Governance`, `Budget`, and `Capacity` are bound once at that machine scope to coordinate shared finite resources.
Operation handles stay with their required boundary bindings and selected operation-local contract and movement
bindings. Unselected positions remain canonical explicit absence and grant no inferred authority. Internal core
functions, stages, and call graphs remain implementation and do not create nested IDL operations. Handwritten
host-interface drift and host-runtime identity are rejected. The custom syntax stays narrow. The one-dimensional catalog
is recorded without freezing its authoring syntax except for the exact Lowering and Publication coordinate relations.
Their generated realization ports keep implementation explicit and replaceable while allowing Kontrakt to verify,
specialize, fuse, inline, or erase the physical call path after binding. If Kontrakt is removed, the retained ports and
their ordinary host-language adapters remain available even though contract-aware regeneration, verification, assembly,
and optimization disappear.