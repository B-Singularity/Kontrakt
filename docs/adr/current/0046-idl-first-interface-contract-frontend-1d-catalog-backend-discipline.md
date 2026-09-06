# ADR-0046: IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary

## Status

Accepted

## Date

2026-07-05

## Related

- `docs/what-contract-is.md`
- `../../todo/kontrakt-verifier-implementation-plan.md`
- `../../todo/release-readiness-todo.md`
- ADR-0069: Invariant Contract
- ADR-0068: Fact Contract
- ADR-0067: Lowering Contract
- ADR-0064: Input Contract
- ADR-0059: Output Presentation Contract
- ADR-0058: Publication Contract
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- ADR-0025: Interface-First Design and Test Interface Pattern

---

## Amendment

This ADR previously treated selected Lowering and Publication declarations as user-supplied generated realization-port
boundaries and used one generated host interface as both the external interaction surface and the user implementation
surface.

That conflated Contract expression with user realization. One-dimensional Contract declarations are declarative source
evidence that Kontrakt resolves, establishes, and realizes. Within the generated Kontrakt Contract surface, the
user-supplied implementation boundary is the declared Operation realization. The external interface remains the IDL
interface itself and is implemented by Kontrakt backend realization.

The one-dimensional ADRs own their individual Contract meaning and authoring forms. This ADR owns the interface-level
frontend split and the generated host boundaries.

---

## 1. Context

Kontrakt is moving from a test-framework-shaped system toward an explicit contract machine.

`What Contract Is` places contract authority outside host-language implementation mechanics. The frontend must preserve
that boundary while remaining small enough for ordinary use.

The frontend has two jobs. It makes the contract machine visible to the user and lets Kontrakt lower that surface into
canonical material. It also turns open-ended system design into a finite set of visible questions. Users declare the
answers they know. Kontrakt owns the machine-facing representation and the verification, testing, enforcement,
diagnostic, realization, and optimization consequences that those answers permit.

The key correction is this:

```text
IDL interface
    = closed external Interaction surface
    + closed Operation handles
    + required Input and Output Presentation bindings
    + selected shared, standing-core, operation-local, and movement bindings

selected one-dimensional Contract
    = declarative Contract expression
    != user implementation SPI

Operation
    = the user-supplied business realization boundary
```

A method name alone is only an operation handle. The external callable surface becomes a Kontrakt interaction when the
IDL binds that handle to the applicable boundary presentations and other selected Contract material. The Operation
signature separately states the ordinary host values that the user business realization receives and the candidate
result material it returns.

When selected, shared `Policy`, `Governance`, `Budget`, and `Capacity` contracts are bound once at the enclosing
interface scope. `Facts` and `Invariants` are declared once for the same explicit core. Operation-local slots bind the
additional obligations required by that operation.

The slot declaration does not create a user callback boundary. The selected Contract is source evidence. Kontrakt
resolves it into authoritative Contract material and realizes the required judgment or material movement through the
compiler and backend.

This ADR decides how that interface is authored, what generated host surfaces users receive, which generated surface a
user implements, and where authority moves after resolution and establishment.

---

## 2. Problem

Kontrakt needs a frontend that exposes the explicit contract machine without returning authority to host-language
mechanics.

A handwritten Kotlin or Java method signature identifies a call, not the machine that makes the call a contract.

Putting Kontrakt syntax inside a user's handwritten host interface is also wrong. If removing Kontrakt invalidates that
source, Kontrakt has become part of the user's own handwritten contract surface.

A sidecar mirror is not sufficient either. It makes the same interface fact appear in two authored places and then asks
a compiler to police the drift.

Annotation carriers and string references fail for different reasons. The first routes the manifest through host runtime
type handles. The second hands meaning to lookup rules.

There is another boundary problem. The values seen outside the machine are not the same authority surface as the values
seen by the user Operation. External software supplies an Input Presentation. The Contract machine must judge and refine
that material before the Operation may receive established core material. A successful Operation return is likewise
candidate result material rather than an Output Presentation merely because a host method returned it.

Using one generated host interface for both sides collapses those boundaries:

```text
outside Input Presentation
    != Operation parameter Fact material

Operation return candidate
    != outward Output Presentation
```

Generating user implementation interfaces for each selected one-dimensional Contract is also wrong. It turns declarative
Contract law into a callback framework. A user-written Admission validator, Lowering mapper, Invariant checker, or
Publication adapter would make the implementation participate in meaning that the Contract declaration is supposed to
own.

Writing the Operation and its tests is already difficult when the author does not know which obligations the system must
make explicit. A blank programming surface asks the author to invent the system and its proof strategy at the same time.
An all-or-nothing contract language merely moves that burden into a larger form.

The frontend therefore needs one authored contract interface, one generated external host surface that preserves the IDL
interface name, one generated Operation realization surface for user business code, and compiler-owned realization of
selected one-dimensional Contracts. It must expose the available Contract positions as questions, preserve explicit
absence, and let the author strengthen the machine by answering additional questions when they matter.

---

## 3. Decision Drivers

The frontend must keep the contract interface and every selected shared, contract, and movement binding visible in one
authored surface without requiring every available position to be filled.

The interface must distinguish the external interaction boundary from the user Operation realization boundary. Input
Presentation and Output Presentation belong to the external surface. Established Operation parameter material and
Operation result candidate material belong to the user realization surface.

The generated external interface must preserve the IDL interface name. External application code should depend on that
interface rather than on a second generated `Interaction` type.

The generated Operation surface must remain ordinary JVM/Kotlin code so user business logic can implement it with normal
host-language tooling. The generated surface is realization ABI, not Contract authority.

A selected one-dimensional Contract must not require a user implementation merely because runtime work is necessary. The
declaration supplies semantic authority. Kontrakt supplies the compiler/backend realization. If the declared Contract is
not sufficiently closed to determine a valid realization under its own law, compilation must reject the definition or
the Contract language must be extended. A hidden mapper, validator, policy callback, or framework convention must not
fill the gap.

The slot board must guide authoring. Each slot must present one bounded design question, preserve unanswered positions
as explicit absence, and let Kontrakt derive only the verification, tests, enforcement, diagnostics, realization, and
optimization supported by the answers actually declared.

The vocabulary must come from the Kontrakt pipeline. Stage names carry the judgment role, so Design-by-Contract terms
are not the primary frontend model.

References must begin as source symbols and end as Kontrakt-owned material. Runtime host handles and string lookup
cannot own identity.

V1 must stay narrow. The interface IDL owns interface shape, Operation signatures, slot selection, and explicit
bindings. Each one-dimensional ADR owns the authoring grammar and semantic closure of its own Contract. This ADR does
not duplicate those grammars.

Machine sympathy belongs to the backend. The user-facing surface declares meaning and business computation. Kontrakt
owns the physical Contract-machine form and may lower declared material into compiler-grade realization paths without
requiring source-level machine classes.

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

    operation calculate(command: CalculateCommand): CalculateRecorded {
        manifest { ... }
    }
}
```

This avoids modifying a handwritten host interface, but it still mirrors the same operation surface in another authored
file.

### 4.5. IDL-first Kontrakt interface contract

A `.kontrakt` file is the authored interface contract source. Kontrakt compiles it into ordinary host-language boundary
surfaces and Contract-machine realization material.

Decision: accepted.

---

## 5. Decision

Kontrakt will use an IDL-first interface contract frontend.

A `.kontrakt` interface contract is source material. It is not a mirror of a handwritten host interface.

Kontrakt compiles that source into two distinct host-facing artifacts.

The first is the external interface. It preserves the IDL interface name. Its operation handle accepts the selected
Input Presentation and returns the selected outward Output Presentation according to the applicable Output and Failure
surface.

The second is the Operation realization interface. It mirrors the resolved IDL Operation signature. The user implements
this surface to provide business computation over the material lawfully handed into the Core.

The two artifacts serve different directions:

```text
external application
    -> generated interface named by the IDL interface
    -> Kontrakt Contract-machine realization
    -> generated Operation realization interface
    -> user Operation implementation
```

The user does not implement the external interface as the business realization. Kontrakt backend realization implements
that external surface or provides an equivalent executable product behind it.

The user also does not implement selected one-dimensional Contracts. `Input`, `Admission`, `Canonicalization`,
`Lowering`, `Invariant`, `State`, `Transition`, `Failure`, `Publication`, `Output`, `Policy`, `Budget`, `Capacity`,
`Governance`, diagnostics, and other declared Contract positions remain declarative Contract material under their own
ADRs. Kontrakt resolves, establishes, verifies, and realizes them through compiler/backend machinery.

Within the generated Kontrakt Contract surface, the user-supplied implementation extension point is Operation
realization. External adapters and ordinary application composition remain outside this statement.

The Operation implementation receives only the host values that the declared inbound Contract path has lawfully formed
for that Operation occurrence. Returning a host value does not establish Fact, State, Publication, Failure, or Output
authority. The returned material is candidate result material until the applicable Contract authorities judge it.

The IDL declares a closed set of interface operations. Each operation binds one `Input Contract` and one explicit Output
position under the existing Output law. Those boundary declarations are enough to make the external interaction shape
visible. They do not by themselves guarantee that a complete executable business path exists. Every selected Contract
and every declared Operation parameter or outward result must satisfy its own dependency and completeness law before the
machine is valid.

When selected, the enclosing interface binds `Policy`, `Governance`, `Budget`, and `Capacity` once because those
contracts coordinate the finite resources and decisions shared among its operations. It likewise declares selected
`Facts` and `Invariants` once for the same explicit core. None of those interface-scoped declarations is repeated inside
an operation manifest.

Operation-local slots select the applicable one-dimensional Contract material. The slot is not an implementation hook.
The source form for each selected Contract is owned by that Contract's ADR. A source may be IDL material or another
restricted declaration form, but after resolution it becomes Kontrakt-owned Contract material rather than a user
callback.

References in the IDL are compile-time source symbols, not host-runtime handles or lookup names.

Short form:

```text
.kontrakt interface contract
    -> resolve and establish Contract material
    -> generate external interface with the IDL interface name
    -> generate Operation realization interface
    -> user implements Operation only
    -> Kontrakt verifies/adopts that Operation realization
    -> backend realizes selected 1D Contracts and the interaction machine
    -> external code calls the generated IDL-named interface
```

---

## 6. IDL-First Interface Law

The authored interface contract lives in `.kontrakt` source.

The IDL interface name is also the name of the generated external host interface. The generated type is an artifact of
that contract source; it is not a second authored interface and does not create separate Contract identity.

The IDL Operation declaration serves another purpose. Its parameter and result surface describes the business
realization boundary inside the Contract machine. The generated Operation interface mirrors that resolved signature for
ordinary host-language implementation.

For example, the conceptual distinction is:

```text
IDL interface Order
    external generated surface:
        Order
        PlaceOrderInput -> PlaceOrderOutput

IDL operation place(command: PlaceOrder): OrderPlaced
    generated user-realization surface:
        OrderOperation
        PlaceOrder -> OrderPlaced candidate
```

`OrderOperation` is an illustrative generated host name. Package naming, collision mangling, and multi-operation host
ABI are compiler API details. They do not contribute to Contract meaning.

The generated artifacts must not become the authority. If generated source or backend product disagrees with established
Contract material, the generated material is wrong.

The IDL source is also not final authority. Authority begins only after the applicable definition has been resolved and
established under the owning Contract law.

---

## 7. Generated External Interface and Operation Realization Boundary

The generated external interface and generated Operation interface are ordinary host-language compatibility surfaces.
They must not contain hidden Contract authority.

A simple IDL may declare:

```text
interface CalculateContract {
    facts         CalculateFacts
    invariants    CalculateInvariants

    operation calculate(command: CalculateCommand): CalculateRecorded {
        manifest {
            flow:
                input       CalculateInput
                admission   CalculateAdmission
                lowering    CalculateLowering
                publication CalculatePublication
                output      CalculateOutput
        }

        lowering CalculateLowering {
            value -> command.value
        }
    }
}
```

The generated external surface keeps the IDL interface name:

```java
// GENERATED CODE - DO NOT MODIFY
public interface CalculateContract {
    CalculateOutput calculate(CalculateInput input);
}
```

The generated Operation realization surface is separate:

```java
// GENERATED CODE - DO NOT MODIFY
public interface CalculateOperation {
    CalculateRecorded calculate(CalculateCommand command);
}
```

The user implements the Operation surface:

```java
public final class CalculateService implements CalculateOperation {

    @Override
    public CalculateRecorded calculate(CalculateCommand command) {
        long result = Math.multiplyExact(command.value, 2L);
        return new CalculateRecorded(result);
    }
}
```

The user does not construct the inbound `CalculateCommand` merely to call the business implementation. For an actual
interaction occurrence, Kontrakt receives the actual `CalculateInput`, applies the selected inbound Contracts, forms the
actual Operation parameter material according to the declared Lowering relation, establishes the required authority, and
then invokes the user Operation with that actual value.

Conceptually:

```text
actual CalculateInput
    -> Input
    -> Admission
    -> optional Canonicalization
    -> Lowering
    -> candidate CalculateCommand
    -> applicable judgment and establishment
    -> CalculateOperation.calculate(actual CalculateCommand)
    -> CalculateService.calculate(...)
    -> CalculateRecorded candidate
    -> applicable result-side judgment
    -> Publication
    -> Output
    -> actual CalculateOutput
```

The physical backend does not need to emit a source-level `CalculateInteractionMachine` or equivalent orchestration
class. It may lower the same established Contract path directly into bytecode, static calls, compiler-owned tables,
specialized method handles, or another deterministic executable form. Those choices are realization details.

No selected one-dimensional slot creates a generated user implementation interface. A backend may internally generate
helpers, evaluator code, tables, guards, or specialized routines for those Contracts. Those are compiler-owned
realization artifacts rather than user SPIs.

The `override` in `CalculateService` belongs to host-language Operation realization. It does not author Contract
meaning. The exact machine-assembly, DI capture, and realization-admission protocol is outside this ADR.

---

## 8. Interface Manifest Law

An interface is the software-visible contract presentation for interaction.

A method name is only an operation handle. The IDL gives that handle an explicit external boundary and an explicit
business Operation signature. Selected contract-axis and movement material then closes the obligations that apply to
that interaction.

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

    operation
        Operation signature
            established/core-facing parameter surface
            candidate result surface

        flat operation manifest
            flow
                input
                selected flow positions
                output

            selected movement positions
            selected bounds positions
            selected diagnostic positions
```

The external generated method uses the selected boundary presentations. The generated Operation realization method uses
the resolved Operation signature. The two surfaces are related by the Contract machine, not by a user-authored mapper.

The operation manifest is a slot board. The slot names on the left are IDL keywords, not user-defined labels. Each slot
is both a binding position and a bounded design question. The author supplies Contract material on the right when that
question matters.

A slot selects Contract material. It does not ask the user to implement the selected Contract. If execution requires a
judgment, projection, refinement, accounting action, capacity gate, or other physical work, Kontrakt realizes that work
from the established Contract material and the applicable backend capabilities.

`Policy`, `Governance`, `Budget`, and `Capacity` are not operation-manifest slots. When selected, they are bound once
for the interface's closed operation set, and their declarations may express machine-wide limits together with explicit
operation allocations or run-grant profiles.

The source grammar of each one-dimensional Contract is owned by its specific ADR. For example, current Lowering owns an
explicit source-to-Operation coordinate relation, while Publication owns outward exposure selection and Output owns the
closed outward result shape. This ADR only owns how those declarations are selected and connected to the interface
surface.

The manifest regions exist for visibility. They have no Contract meaning of their own. They do not create parent
Contracts, nested structure, processing order, or shared authority. Kontrakt resolves each bound presentation under its
own owner.

---

## 9. Minimum Interface Skeleton and Guided Contract Enrichment

The minimum authoring skeleton is:

```text
operation handle
+ explicit Input position
+ explicit Output position
= visible external interaction skeleton
```

This minimum is an authoring boundary, not permission to bypass the Contracts required by the declared Operation
signature or outward result.

If an Operation parameter must be formed from external Input, the applicable inbound Contracts must lawfully form and
establish that parameter before user code can run. If an outward result uses Core result material, the applicable
Publication and Output laws must close before outside software can rely on that result. The compiler validates those
dependencies rather than assuming that Input and Output alone make every business path executable.

Every other position remains explicit Contract enrichment rather than a universal mandatory slot. Selecting a position
adds the declared Contract material and allows Kontrakt to derive the corresponding machine capability. More declared
material gives the machine more knowledge, but no position receives authority merely because a backend could guess a
useful behavior.

```text
more declared contract material
    -> more machine knowledge
    -> stronger generated verification, testing, enforcement, diagnostics, realization, and optimization
```

An unselected optional slot lowers to canonical explicit absence. It is not an unresolved reference, an invitation to
structural inference, or permission to insert a convenient law. A named default may apply only when the authored
contract explicitly selects that default.

The slot system is therefore also an authoring system. Software and tests are often difficult to construct because the
author does not yet know what must be decided, what may fail, what must always remain true, what movement is legal, or
what evidence should survive. The board does not merely name mechanisms. Input asks what may appear at the inbound
boundary. Admission asks whether that presentation may continue. Invariant asks what standing Fact law must hold.
Movement asks what may happen next. Publication asks which established exit material may receive outward authority.
Output asks what final outward shape exists. Diagnostics asks what the machine may explain. Bounds asks which explicit
limits and coordinates apply.

The frontend guides the author toward a richer machine. It does not create hidden implementations for undeclared
meaning, and it does not make user-written implementations substitutes for missing Contract declarations.

---

## 10. Three Pipeline Axes

Every interaction has an ordinary external callable boundary and the selected Contract material that governs that
boundary. The user Operation is reached only after the applicable inbound authority has established the material that
may participate in the Core.

An operation may populate three axes to different depths.

The first axis is the contract pipeline. This is the authority axis. It declares the logical obligations that make the
interaction a contract. Its positions are Contract positions, not user implementation callbacks.

The second axis is the implementation pipeline. This is the realization axis. It has no Contract authority. Kontrakt may
generate, fuse, split, specialize, replace, or optimize compiler-owned realization as long as the declared Contract
material remains unchanged. The user Operation implementation participates in this axis only at the explicit Operation
realization boundary.

The third axis is the state-machine pipeline. State and Transition already belong to the Contract world, but once made
explicit they form their own movement surface beside the contract pipeline. This axis declares which machine condition
is active, which move is legal, and where movement must stop.

These axes must not be collapsed.

```text
contract pipeline:
    declares obligation and judgment authority

implementation pipeline:
    compiler/backend realization
    + explicit user Operation realization
    no Contract authority

state-machine pipeline:
    declares legal movement through explicit machine conditions
```

The user-facing interface contract declares selected Contract-axis and State-Machine-axis material. An operation with no
selected movement material grants no State-Machine authority; Kontrakt does not infer one from implementation behavior.
Selected one-dimensional Contracts enter the implementation axis through compiler/backend realization, not through
user-supplied per-Contract implementations.

At the authoring surface, the operation manifest may be grouped for readability. `Flow` carries the material path.
`Movement` carries the state surface. `Bounds` carries the operation's version coordinate. `Diagnostics` carries
explanation and retention. These names do not create another axis, and `bounds` is not an operation stage. The shared
`Policy`, `Governance`, `Budget`, and `Capacity` bindings remain at the enclosing interface scope.

The stage names used in the contract axis are Contract vocabulary, not a physical schedule. A backend may use any
equivalent structure as long as the declared obligation remains intact.

Every selected position must satisfy its own applicability and dependency law. Every unselected optional position
becomes canonical explicit absence before machine realization. The compiler may not infer a Contract, insert a hidden
default, or claim missing authority.

```text
input        selected
output       selected or explicitly absent under its owning law
invariant    unselected
movement     unselected
```

Declared absence is Contract material. Hidden absence is not.

---

## 11. One-Dimensional Contract Catalog

This ADR records the interface-visible catalog but does not own the detailed Contract law or final source grammar of
each one-dimensional Contract. The current per-Contract ADR owns that material.

A one-dimensional presentation declares one obligation kind before the enclosing interface or an operation manifest
binds it according to that obligation's scope. Its source declaration is Contract expression. It is not a generated
implementation interface.

The initial catalog is:

```text
Interface Surface Contract:
    declares the public reliance surface of an interface contract

Input Contract:
    declares the finite presentation shape that may appear at the inbound boundary

Output Presentation Contract:
    declares the closed outward result shape of an applicable established exit

Admission Contract:
    declares whether a valid Input presentation may continue beyond the boundary

Canonicalization Contract:
    declares equivalence, the system-owned representative, tolerated source drift, and refusal when stable
    representation cannot be produced

Lowering Contract:
    declares the explicit lawful relation by which selected inbound presentation coordinates may form Operation
    parameter Fact coordinates

Fact Contract:
    declares what immutable factual material may exist with Fact authority inside the Core

Invariant Contract:
    declares a standing Fact-local integrity law that candidate material must satisfy where applicable

State Contract:
    declares finite, closed, flat machine conditions that govern legal next moves

State Transition Contract:
    declares permitted one-way movement between declared machine conditions

Explicit State Machine Manifest:
    declares the state set, initial condition, terminal conditions, and permitted transitions of one movement surface

Failure Contract:
    declares explicit contract-governed stop meaning and attribution

Publication Contract:
    declares which established Result or Failure material may receive outward exposure authority

Diagnostic Evidence Contract:
    declares what Contract-owned explanation material may be established for a declared judgment

Diagnostic Retention Contract:
    declares what diagnostic evidence may remain after the run, how it is bounded, and what must be discarded

Version Coordinate:
    declares which contract meaning governed a judgment, material, claim, or evidence

Policy Contract:
    declares the explicit operating world and applicable cross-Contract criteria for the bound interface machine

Budget Contract:
    declares contract-scoped consumable allowance and explicit allocation under the applicable machine world

Capacity Contract:
    declares finite simultaneous operating limits and applicable admission walls

Governance Contract:
    declares the applicable selection, scope, binding, validity, and singularity material for the governed machine
```

The catalog names the obligation kinds that Kontrakt must recognize across frontend, resolution, establishment,
verification, realization, and backend projection. Later ADRs may refine the exact vocabulary within their own semantic
ownership.

The catalog remains flat. A user-facing operation manifest may group operation-local presentations as `flow`,
`movement`, `bounds`, and `diagnostics`, but those groups only help the author read the operation. They do not compose,
inherit, or own the presentations inside them. `Policy`, `Governance`, `Budget`, and `Capacity` remain independent
one-dimensional contracts bound at the scope defined by their own current laws.

---

## 12. Illustrative Interface Shape

A small `.kontrakt` interface may begin by declaring the external boundary and the business Operation signature:

```text
interface CalculateContract {
    facts CalculateFacts

    operation calculate(command: CalculateCommand): CalculateRecorded {
        manifest {
            flow:
                input   CalculateInput
                output  CalculateOutput
        }
    }
}
```

This is an authoring skeleton. `CalculateInput` and `CalculateOutput` describe the external boundary. `CalculateCommand`
and `CalculateRecorded` belong to the Operation realization surface. A complete executable machine must still satisfy
the dependency laws required to form the declared Operation input and establish the declared outward result. The
compiler does not bridge those domains by shape inference.

The same operation may be enriched as its requirements become explicit:

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
            value -> command.value
        }
    }
}
```

This sketch shows the external Input and Output Presentation bindings together with selected machine-wide bindings,
interface-scoped Fact vocabulary and Invariant laws, and additional operation-local slots. Inside the manifest, the left
side is the fixed operation-slot vocabulary and the right side is the Contract material bound to each selected slot.
`flow` is shown as a readable grouping, not as transition authority.

`facts CalculateFacts` declares the Fact vocabulary eligible for establishment in the interface's explicit Core.
`invariants CalculateInvariants` declares the standing laws that govern applicable Facts in that same Core. Neither
declaration is repeated through an operation-manifest slot.

The Lowering relation declares permitted source-to-Operation-coordinate formation under the Lowering Contract. It does
not ask the user for a `CalculateLowering` implementation. Kontrakt resolves the relation and the backend realizes the
actual value formation. Physical construction, allocation, copying, parsing, specialization, or elimination remains
backend realization subject to the Lowering law.

`CalculatePublication` is selected by the Publication slot. Its source declaration and positive selection law are owned
by ADR-0058. The interface IDL does not turn Publication into a user mapper. Output remains a separate closed outward
shape under ADR-0059.

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
infer State from a returned value, method completion, a field named `status`, or backend behavior. An explicitly
selected
`Stateless` declaration remains available when its owning State law permits deliberate statelessness.

The manifest regions do not change the one-dimensional catalog. Bound presentations remain separate after resolution.
The interface-level `Policy`, `Governance`, `Budget`, and `Capacity` contracts also remain separate material even when
bound at a shared scope.

From the enriched example, the generated host surfaces are conceptually:

```java
public interface CalculateContract {
    CalculateOutput calculate(CalculateInput input);
}

public interface CalculateOperation {
    CalculateRecorded calculate(CalculateCommand command);
}
```

Only `CalculateOperation` is implemented by user business code. `CalculateContract` is the external interface that host
application code calls. Kontrakt backend realization connects them through the established Contract machine.

---

## 13. V1 Parser Scope

The v1 parser covers only the IDL interface contract subset.

It reads interface shape, Operation signatures, explicit Input and Output position bindings, selected shared `Policy`,
`Governance`, `Budget`, and `Capacity` references, selected interface-scoped `Facts` and `Invariants` references,
selected axis entries, IDL-owned one-dimensional declarations such as the current Lowering relation form, slot
occupancy, source references, and source locations. Resolution records every permitted unselected optional position as
canonical explicit absence.

The parser does not absorb the source grammar owned by another frontend merely because the Contract is selected from the
IDL. Restricted host declarations used by current one-dimensional ADRs remain source evidence acquired by their own
frontend path and resolved into the same Contract world.

The parser stops before deeper languages that their owning ADRs have not ratified. The frontend remains a contract
interface notation, not a general programming language.

---

## 14. Generated Artifact Law

The generated external interface and generated Operation realization interface are reproducible build outputs.

They must be regenerated from `.kontrakt` source and must not be manually edited. If generated files are committed for
consumer convenience, the committed files are still artifacts, not authority.

The external interface preserves the IDL interface name. The Operation realization interface is a generated host ABI for
business implementation. Neither generated surface owns Contract meaning.

Selected one-dimensional Contracts do not produce user-owned implementation artifacts merely because the backend needs
code to execute them. Compiler-generated evaluators, guards, materialization paths, tables, and specialized helpers are
backend products.

The complete Contract machine also does not need a retained generated source class. A backend may directly produce the
executable class, bytecode, static binding, method-handle structure, or other JVM-facing product that implements the
external interface and invokes the admitted user Operation realization.

Removing Kontrakt removes regeneration, resolution, establishment, verification, Contract-machine assembly, backend
realization, and contract-aware optimization. Retained external and Operation interface sources remain ordinary host
artifacts, and retained user Operation code remains ordinary host code. Those artifacts alone do not reconstruct the
Contract machine or grant the external interface a valid implementation.

---

## 15. Consequences

The accepted frontend keeps one authored interface contract while separating two host boundaries that previously looked
like one method surface. External application code sees the IDL interface itself. User business code implements the
generated Operation realization surface. Kontrakt owns the machine between them.

One-dimensional Contract declarations remain expressions of Contract meaning rather than user implementation SPIs. A
selected Admission does not require a user validator. A selected Lowering does not require a user mapper. A selected
Publication does not require a user publication adapter. The same rule applies to the other one-dimensional Contracts
under their own semantics. Physical realization remains replaceable because backend machinery may change without moving
Contract authority into user code.

The separation also makes the inbound and outward authority boundaries explicit. External Input Presentation material
must be judged and lawfully formed before the user Operation receives Core material. An Operation return remains
candidate result material until the applicable Contract authorities establish what may continue and what may leave.

The slot board still exposes the questions that would otherwise be scattered across implementation, tests, reviews, and
conventions. Optional positions remain explicit absence where their owning law permits it. The compiler derives only the
verification, testing, enforcement, diagnostics, realization, and optimization justified by declared material.

The cost is that Kontrakt must own more compiler/backend work. It must generate the external and Operation ABI surfaces,
resolve every selected one-dimensional Contract, synthesize or lower the corresponding machine realization, verify the
user Operation realization, and produce the executable path that connects the two surfaces. That cost is intentional.
The alternative would move Contract meaning back into callbacks, mappers, validators, framework conventions, or hand
written orchestration.

Internal Core functions, helper classes, stages, and call graphs remain realization and do not create nested IDL
operations. Generated API names, backend classes, bytecode layout, DI framework shape, and runtime object identity do
not create Contract authority.