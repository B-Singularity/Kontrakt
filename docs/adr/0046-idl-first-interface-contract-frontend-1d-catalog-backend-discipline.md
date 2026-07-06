# ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary

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
Interface = operation handle + manifest binding
```

A method alone is only an operation handle. An interface becomes a Kontrakt interface when its operations are bound to
explicit pipeline manifests.

This ADR decides how that interface is authored, how host-language code receives a usable interface, and where authority
moves after lowering.

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

The frontend therefore needs one authored contract interface, an ordinary generated host interface, and a lowering path
that gives final authority to Kontrakt material.

---

## 3. Decision Drivers

The frontend must keep the contract interface, the contract axis, and the explicit movement axis visible in one authored
surface.

The generated host interface must remain ordinary JVM/Kotlin code. It exists for implementation and calling, not for
contract authority.

The vocabulary must come from the Kontrakt pipeline. Stage names carry the judgment role, so Design-by-Contract terms
are not the primary frontend model.

References must begin as source symbols and end as Kontrakt-owned material. Runtime host handles and string lookup
cannot own identity.

V1 must stay narrow. The IDL owns the interface manifest. This ADR records the initial one-dimensional contract
catalog, but it does not decide the final authoring form for those presentations.

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

Kontrakt compiles that source into a plain host-language interface. Implementations realize the generated interface. The
generated interface is a build artifact, not contract authority, and users must not hand-edit it.

The IDL declares interface operations and binds each operation to an explicit contract axis and an explicit
state-machine movement axis. One-dimensional contract presentations are named as closed obligation kinds by this ADR,
but their final authoring form remains unresolved. The implementation axis is produced behind that surface and carries
no authority.

References in the IDL are compile-time source symbols, not host-runtime handles or lookup names.

Short form:

```text
.kontrakt interface contract
-> generated host interface
-> implementation realizes generated interface
-> canonical contract material owns authority
```

---

## 6. IDL-First Interface Law

The authored interface contract lives in `.kontrakt` source.

The generated Kotlin or JVM interface is an output of the contract compiler. It lets implementations and callers use
ordinary host-language tooling.

The generated artifact must not become the authority. If generated source and canonical material disagree, the generated
source is wrong.

The IDL source is also not final authority. Authority begins only after it has been resolved and lowered into
Kontrakt-owned material.

---

## 7. Generated Host Interface Boundary

A generated host interface is a compatibility surface.

It may be committed, inspected, implemented, and called as ordinary Kotlin or JVM code. It must not contain hidden
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

---

## 8. Interface Manifest Law

An interface is the software-visible contract presentation for interaction.

A method is only an operation handle. The interface earns contract status when that handle is bound to explicit contract
and movement material.

The frontend shape is:

```text
interface contract
    operation handle
        explicit contract-axis binding
        explicit state-machine-axis binding
```

The operation handle, contract axis, and state-machine axis are authored together in `.kontrakt` source.

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

The user-facing interface contract declares the contract axis and the state-machine axis. Kontrakt is free to build the
implementation axis behind them.

The stage names used in the contract axis are contract vocabulary, not a physical schedule. A backend may use any
equivalent structure as long as the declared obligation remains intact.

If a contract position or movement position has no additional user-defined condition, that absence must be declared.

```text
invariant NoAdditionalInvariant
state Stateless
transition NoTransition
```

Declared absence is contract material. Hidden absence is not.

---

## 10. One-Dimensional Contract Catalog

This ADR does not decide the final authoring form for one-dimensional contract presentations.

Authoring syntax comes later. First, Kontrakt names the closed obligation kinds an interface contract can bind. A
one-dimensional presentation declares one obligation kind before an operation binds it.

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
    declares how canonical representation becomes core-readable candidate material

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
    declares whether accepted material may become an outward public claim

Diagnostic Evidence Contract:
    declares what explanation may be offered by a declared judgment

Diagnostic Retention Contract:
    declares what evidence may remain after the run, how it is bounded, and what must be discarded

Version Coordinate:
    declares which contract meaning governed a judgment, material, claim, or evidence

Policy Contract:
    declares which judgment criteria are active under a machine context

Budget Contract:
    declares finite consumable allowance for a run, operation, stage, or diagnostic path

Capacity Contract:
    declares the admissible limit of a machine, surface, stage, queue, or storage region

Governance Contract:
    declares which contract set, policy set, version, capacity, budget, and manifest binding is valid
```

The catalog is not an authoring syntax decision. It names the obligation kinds that Kontrakt must recognize across
frontend, resolution, lowering, verification, and backend projection.

---



---

## 11. Illustrative Interface Shape

A v1 `.kontrakt` interface contract may show the contract axis and state-machine axis like this:

```text
interface CalculateContract {
    operation calculate(input: CalculateInput): CalculateOutput {
        contractPipeline {
            input CalculateInput
            admission XGreaterThanOne
            canonicalization DefaultPrimitiveCanonicalization
            lowering CalculateLowering
            invariant NoAdditionalInvariant
            publication ResultGreaterThanInput
            diagnostic CalculateDiagnostics
            governance DefaultGovernance
        }

        stateMachinePipeline {
            state Stateless
            transition NoTransition
        }
    }
}
```

This sketch shows only the interface contract surface and its axis binding. The one-dimensional presentation form
remains open.

Kontrakt compiles the interface contract into a host interface and lowers resolved contract material into canonical
form.

---

## 12. V1 Parser Scope

The v1 parser covers only the IDL interface contract subset.

It reads interface shape, operation shape, axis entries, source references, and source locations.

The parser stops before the deeper languages: predicate bodies, host expressions, policy, state-machine detail,
one-dimensional authoring, composition, and editor tooling.

The frontend remains a contract interface notation, not a general programming language.

---

## 13. Generated Artifact Law

Generated host interfaces are reproducible build outputs.

They must be regenerated from `.kontrakt` source and must not be manually edited. If generated files are committed for
consumer convenience, the committed files are still artifacts, not authority.

Removing Kontrakt removes regeneration and verification. It does not force users to maintain Kontrakt syntax inside
handwritten host interfaces, because this model has no handwritten host interface source for generated contracts.

---

## 14. Consequences

The accepted frontend makes the interface contract explicit. Operation handles stay with their contract and movement
bindings. Handwritten host-interface drift and host-runtime identity are rejected. The custom syntax stays narrow. The
one-dimensional catalog is recorded without freezing its authoring syntax. The implementation axis remains replaceable
backend work owned by Kontrakt.