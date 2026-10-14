# ADR-0046: IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary

## Status

Accepted

## Date

2026-07-05

## Related

- `docs/what-contract-is.md`
- `../../../../todo/verification/kontrakt-verifier-implementation-plan.md`
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

The 2026-10-14 revision removes mandatory Input and Output bindings. It preserves Explicit Absence in 1D role selection,
separates HIR candidates (ADR-0071) from Establishment (ADR-0063), and leaves the legality of each absence to its owning
Contract laws.

---

## 1. Context

Kontrakt is moving from a test-framework-shaped system toward an explicit contract machine.

`What Contract Is` places contract authority outside host-language implementation mechanics. The frontend must preserve
that boundary while remaining small enough for ordinary use.

The frontend has two jobs. It makes the contract machine visible to the user and resolves authored declarations into
HIR candidates. It also turns open-ended system design into a finite set of visible questions. Users declare the
answers they know. Owning Contract laws establish meaning from valid candidates. Kontrakt owns the machine-facing
representation and the verification, testing, enforcement, diagnostic, realization, and optimization that established
meaning permits.

The key correction is this:

```text
IDL interface
    = closed external Interaction surface
    + closed Operation handles and signatures
    + explicit role selection at each applicable owning scope
    + selected bindings or Explicit Absence for each applicable 1D role

selected one-dimensional Contract
    = declarative Contract expression
    != user implementation SPI

Operation
    = the user-supplied business realization boundary
```

A method name alone is only an operation handle. The IDL declares Interaction and Operation surfaces independently of
Input or Output selection. Resolution forms their exact semantic subjects and preserves role selection at each owning
scope. An absent boundary role neither erases an Interaction nor supplies a substitute Contract. The Operation signature
separately declares the host values received by user business code and its candidate result.

When selected, shared `Policy`, `Governance`, `Budget`, and `Capacity` contracts are bound once at the enclosing
interface scope. `Facts` and `Invariants` are declared once for the same explicit core. Operation-local slots bind the
additional obligations required by that operation.

The slot declaration does not create a user callback boundary. A selected Contract declaration is source evidence.
Resolution forms HIR candidates; only the owning Contract laws establish their authority. Kontrakt realizes the
established judgments and material movement through the compiler and backend.

This ADR decides interface authoring and generated host boundaries. It preserves the distinctions needed by HIR and
Establishment without transferring authority between Contracts or defining their judgments.

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

There is another boundary problem. The external Interaction and user Operation have different authority boundaries.
When Input is selected, external software supplies its Presentation; applicable Contract judgments must establish the
material passed into the Operation. When Input is explicitly absent, Kontrakt does not invent an Input Presentation.
A successful Operation return is candidate material, not an Output Presentation merely because a host method returned
it, even when Output is explicitly absent.

Using one generated host interface for both sides collapses those boundaries:

```text
selected external Input Presentation
    != Operation parameter Fact material

Operation return candidate
    != selected outward Output Presentation

Explicit Absence of Input or Output
    != absence of Interaction or Operation
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

The interface must distinguish the external Interaction boundary from the user Operation boundary. Selected Input and
Output Presentations belong to the external surface; their Explicit Absence does not erase either boundary. Established
Operation parameter material and Operation result candidates belong to the user realization surface.

The generated external interface must preserve the IDL interface name. External application code should depend on that
interface rather than on a second generated `Interaction` type.

The generated Operation surface must remain ordinary JVM/Kotlin code so user business logic can implement it with normal
host-language tooling. The generated surface is realization ABI, not Contract authority.

A selected one-dimensional Contract must not require a user implementation merely because runtime work is necessary. The
declaration supplies candidate meaning to be judged by its owning law. Kontrakt supplies the compiler/backend
realization. If the declared Contract is
not sufficiently closed to determine a valid realization under its own law, compilation must reject the definition or
the Contract language must be extended. A hidden mapper, validator, policy callback, or framework convention must not
fill the gap.

The slot board must guide authoring. Each slot presents a bounded design question and preserves explicit nonselection
without confusing it with unresolved declarations. Kontrakt derives only the verification, tests, enforcement,
diagnostics, realization, and optimization supported by established meaning.

The vocabulary must come from the Kontrakt pipeline. Stage names carry the judgment role, so Design-by-Contract terms
are not the primary frontend model.

Source symbols resolve into exact semantic references in Visible HIR. HIR candidates are distinct from established
Contract references. Runtime host handles, generated API symbols, physical indexes, and string lookup cannot own
semantic identity.

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

The first is the external interface. It preserves the IDL interface name and exposes declared Interaction handles.
Its host ABI represents the applicable boundary meaning, including Explicit Absence of Input or Output, without defining
that absence through a host parameter or return type. Selected Presentations and outward Failure handling remain subject
to their owning laws.

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

The IDL declares a closed set of interface operations and their Interaction surfaces. Each applicable 1D role,
including Input and Output, has an exact selected binding or Explicit Absence at its owning scope. Interaction and
Operation subjects remain identifiable independently of those selections. Complete selection makes the declared
boundary visible but does not make every combination legal or executable. Each selected Contract, Operation parameter,
and declared outward result must satisfy its applicable dependency and completeness laws. The laws requiring a role
determine whether its absence is legal.

When selected, the enclosing interface binds `Policy`, `Governance`, `Budget`, and `Capacity` once because those
contracts coordinate the finite resources and decisions shared among its operations. It likewise declares selected
`Facts` and `Invariants` once for the same explicit core. None of those interface-scoped declarations is repeated inside
an operation manifest.

Operation-local slots select the applicable one-dimensional Contract material. The slot is not an implementation hook.
The source form for each selected Contract is owned by that Contract's ADR. A source may be IDL material or another
restricted declaration form; resolution creates Kontrakt-owned HIR candidate meaning, not a user callback or an
established Contract judgment.

References in the IDL are compile-time source symbols, not host-runtime handles or lookup names.

Short form:

```text
.kontrakt interface contract
    -> resolve Interface, Interaction, and Operation subjects
    -> resolve 1D role selections at their owning scopes
    -> form valid Resolved Contract HIR candidates
    -> establish applicable meaning under owning Contract laws
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

The IDL Operation declaration describes the business realization boundary inside the Contract machine. Resolution
forms its independent nominal Operation, parameter, and result coordinates before Lowering targets them. Their identity
does not derive from Input or Output selection, host parameter order, or a JVM descriptor. The generated Operation
interface mirrors the resolved signature for ordinary host-language implementation.

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

The user does not construct `CalculateCommand` merely to call the business implementation. This example selects Input
and Lowering. Kontrakt receives the actual `CalculateInput`, applies the applicable inbound judgments, and forms the
Operation parameter material through the declared Lowering relation. Required material must be established before the
user Operation runs. When Input is explicitly absent, Kontrakt neither invents an Input Presentation nor infers a
replacement Lowering source. Any required Basis remains subject to its owning law.

Conceptually:

```text
actual CalculateInput
    -> Input
    -> Canonicalization (when selected)
    -> Admission
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

This shows a selected Input, Admission, Lowering, Publication, and Output path. When Canonicalization is selected,
Admission judges the applicable representative-bearing Presentation; when absent, no identity Canonicalization is
established. ADR-0048 owns inbound Composition, including the exact basis supplied to each selected stage.

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

A method name is only an operation handle. The IDL declares separate Interaction and Operation surfaces; resolution
forms their semantic subjects independently of optional role bindings. Each applicable role is selected or explicitly
absent at its owning scope. Selected Contract and movement declarations supply obligations governed by their owning
laws.

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

Input and Output in this diagram name role positions, not mandatory bindings. A role is selected or explicitly absent
at its owning scope; interface-scoped selections are not copied into operation manifests. The external generated method
represents the applicable boundary meaning. The generated Operation realization method uses its independently resolved
Operation signature. The Contract machine relates these surfaces, not a user-authored mapper.

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
declared Interaction and Operation surfaces
+ complete 1D role selection at each applicable owning scope
= visible external Interaction skeleton
```

This minimum is an authoring boundary, not permission to bypass obligations required by the Operation signature,
outward result, or another applicable law.

If an Operation parameter must be formed from external Input, the selected inbound Contracts must lawfully establish
that parameter before user code runs. Absent Input does not satisfy that requirement or supply another source. When a
selected Output presents Core result material, the applicable Publication and Output laws must establish outward
meaning before external reliance. Complete role selection is not Complete Basis or proof of a legal executable path.

No 1D role, including Input or Output, is universally required merely to declare an Interaction. A selected role adds
declared Contract material; its actual capabilities depend on lawful Establishment. The owning laws decide whether a
particular absence is permitted. No role gains authority from a backend guess.

```text
more declared contract material
    -> more machine knowledge
    -> stronger generated verification, testing, enforcement, diagnostics, realization, and optimization
```

An unselected 1D role remains Explicit Absence in its owning selection surface. Failed lookup, missing Binding
Candidate, or unresolved reference does not prove absence. No Definition, Occurrence, default, or inferred Contract is
established for that unselected role. A named default applies only if explicitly selected; another Contract's Required
Basis remains its own concern.

The slot system is therefore also an authoring system. Software and tests are often difficult to construct because the
author does not yet know what must be decided, what may fail, what must always remain true, what movement is legal, or
what evidence should survive. The board does not merely name mechanisms. A role is first selected or explicitly absent
at its owning scope; absence does not answer the role's Contract question. Input asks what may appear at the inbound
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

The operation manifest may be grouped for readability. `Flow` presents material roles, `Movement` presents state
roles, `Bounds` groups applicable bounds, and `Diagnostics` groups explanation and retention. The groups create no
Contract authority, semantic axis, or processing stage. `Bounds` does not introduce a separate Operation Version or
repeat the Version belonging to each selected Contract under ADR-0053. Shared `Policy`, `Governance`, `Budget`, and
`Capacity` bindings remain subject to their owning scope laws.

The stage names used in the contract axis are Contract vocabulary, not a physical schedule. A backend may use any
equivalent structure as long as the declared obligation remains intact.

Every selected position must satisfy its applicable dependency and applicability laws. Every unselected 1D role,
including Input and Output, remains Explicit Absence at its owning scope. Selection and the legality of the resulting
composition are distinct. The compiler cannot infer a Contract, insert a hidden default, or satisfy missing Basis.

```text
input        selected or Explicit Absence
output       selected or Explicit Absence
invariant    selected or Explicit Absence at its owning scope
movement     selected or Explicit Absence at its owning scope
```

Explicit Absence is resolved selection meaning, not an Established Definition, Occurrence, judgment, or Basis.

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

Policy Contract:
    declares the explicit operating world and applicable cross-Contract criteria for the bound interface machine

Budget Contract:
    declares contract-scoped consumable allowance and explicit allocation under the applicable machine world

Capacity Contract:
    declares finite simultaneous operating limits and applicable admission walls

Governance Contract:
    declares the applicable selection, scope, binding, validity, and singularity material for the governed machine
```

Version is not a separate 1D Contract. Under ADR-0053, each independently versioned Contract owns its Version
coordinate. The interface manifest does not repeat it; governing Version attribution remains available to later
judgments, material, claims, and evidence.

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

This example selects both Input and Output. `CalculateInput` and `CalculateOutput` describe external Presentations;
`CalculateCommand` and `CalculateRecorded` belong to the independent Operation realization surface. Neither
Presentation is universally required. A complete executable machine must still satisfy the laws governing Operation
parameters and any outward result. The compiler does not bridge those domains by shape inference.

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
                canonicalization  DefaultPrimitiveCanonicalization
                admission         XGreaterThanOne
                lowering          CalculateLowering
                publication       CalculatePublication
                output            CalculateOutput
                failure           CalculateFailure

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
`flow` is a readable grouping, not judgment order or transition authority. This is one selected-role example, not a
minimum required selection set; each selected Contract retains its own Version under ADR-0053.

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

If movement is not selected, its Explicit Absence is preserved; no State-Machine authority is inferred from a returned
value, method completion, a field named `status`, or backend behavior. Explicit `Stateless` remains available when its
owning State law permits it.

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

It reads Interface, Interaction, and Operation declarations and signatures; role selections, including absent Input
and Output; selected shared `Policy`, `Governance`, `Budget`, and `Capacity` references; selected interface-scoped
`Facts` and `Invariants` references; selected axis entries; IDL-owned 1D declarations such as the current Lowering
relation form; slot occupancy; and source references and locations.

Resolution preserves complete role selection at each owning scope: exact selected Binding Candidate relations or
Explicit Absence. It cannot infer absence from failed lookup, incomplete resolution, or a missing Binding Candidate.
ADR-0071 owns the resulting HIR observation protocol.

The parser does not absorb the source grammar owned by another frontend merely because the Contract is selected from the
IDL. Restricted host declarations used by current one-dimensional ADRs remain source evidence acquired by their own
frontend path and resolved into the same Contract world.

The parser stops before deeper languages that their owning ADRs have not ratified. The frontend remains a contract
interface notation, not a general programming language.

---

## 14. Generated Artifact Law

The generated external interface and generated Operation realization interface are reproducible build outputs.

They are regenerated from resolved `.kontrakt` declarations and applicable selected 1D declarations acquired through
their owning frontend paths, under established Contract meaning. They must not be manually edited. Committed generated
files remain artifacts, not authority.

The external interface preserves the IDL interface name. Its ABI represents selected Presentations and Explicit
Absence without inventing Input or Output Contracts; the host parameter or return encoding is a backend/API choice.
The Operation realization ABI follows the independently resolved Operation signature regardless of absent external
roles. Neither generated surface owns Contract meaning.

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

The separation also makes the inbound and outward authority boundaries explicit. Selected Input Presentation material
must undergo the applicable judgments before it can supply established Operation parameters. Absent Input does not waive
required Basis. An Operation return remains candidate material until the applicable authorities establish what may
continue and what may leave. Absent Output neither establishes outward Presentation nor removes internal judgments.

The slot board still exposes questions otherwise scattered across implementation, tests, reviews, and conventions.
Each applicable 1D role, including Input and Output, is selected or explicitly absent. Owning laws determine when
absence
is legal. The compiler derives only the verification, testing, enforcement, diagnostics, realization, and optimization
justified by established Contract meaning.

The cost is that Kontrakt must own more compiler/backend work. It must generate the external and Operation ABI surfaces,
resolve every selected one-dimensional Contract, synthesize or lower the corresponding machine realization, verify the
user Operation realization, and produce the executable path that connects the two surfaces. That cost is intentional.
The alternative would move Contract meaning back into callbacks, mappers, validators, framework conventions, or hand
written orchestration.

Internal Core functions, helper classes, stages, and call graphs remain realization and do not create nested IDL
operations. Generated API names, backend classes, bytecode layout, DI framework shape, and runtime object identity do
not create Contract authority.