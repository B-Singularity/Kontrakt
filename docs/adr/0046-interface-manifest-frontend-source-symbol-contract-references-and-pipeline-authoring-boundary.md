# ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary

## Status

Draft

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

The frontend must keep the contract interface and its pipeline visible in one authored surface.

The generated host interface must remain ordinary JVM/Kotlin code. It exists for implementation and calling, not for
contract authority.

The vocabulary must come from the Kontrakt pipeline. Stage names carry the judgment role, so Design-by-Contract terms
are not the primary frontend model.

References must begin as source symbols and end as Kontrakt-owned material. Runtime host handles and string lookup
cannot own identity.

V1 must stay narrow. The IDL owns the interface manifest. One-dimensional contract bodies may initially remain ordinary
typed Java/Kotlin presentation declarations.

Machine sympathy belongs to the backend. The user-facing surface declares meaning; Kontrakt owns the physical form.

---

## 4. Alternatives

### 4.1. Handwritten host interface plus top-level Kotlin InteractionManifest

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

```kotlin
interface CalculateContract {
    fun calculate(input: CalculateInput): CalculateOutput

    val calculateManifest: InteractionManifest
        get() = InteractionManifest()
}
```

This keeps the text nearby, but it reintroduces host getter/default-body mechanics.

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

The IDL declares interface operations and binds each operation to an explicit pipeline. One-dimensional contract
presentations may be authored outside the IDL through typed Java/Kotlin presentation declarations or through future
presentation sources.

References in the IDL are compile-time source symbols. They are not strings, runtime type references, reflection
handles, or object identity.

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

A method is only an operation handle. The interface earns contract status when that handle is bound to an explicit
pipeline manifest.

The frontend shape is:

```text
interface contract
    operation handle
        explicit pipeline binding
```

The operation handle and its pipeline binding are authored together in `.kontrakt` source.

---

## 9. Explicit Pipeline Law

Every operation in the interface contract must bind one explicit pipeline.

The pipeline is flat. Its stage names are the contract vocabulary:

```text
input -> admission -> canonicalization -> lowering -> invariant -> state -> transition -> publication -> diagnostic -> governance
```

The pipeline must not hide behavior behind host-language execution or framework lifecycle.

If a stage has no additional user-defined condition, that absence must be declared.

```text
invariant NoAdditionalInvariant
state Stateless
transition NoTransition
```

Declared absence is contract material. Hidden absence is not.

---

## 10. One-Dimensional Contract Presentation Law

One-dimensional contract presentations may be authored outside the interface contract.

For v1, ordinary Java/Kotlin typed declarations are allowed. They must behave as presentation data, not as
implementation polymorphism.

Allowed direction:

```kotlin
val XGreaterThanOne = AdmissionContract(
    subject = CalculateInput::x,
    accept = { x: Int -> x > 1 },
    onReject = X_MUST_BE_GREATER_THAN_1,
)
```

Rejected direction:

```kotlin
object XGreaterThanOne : AdmissionContract<CalculateInput> {
    override fun accept(input: CalculateInput): Boolean {
        return input.x > 1
    }
}
```

The first form is authored presentation material that Kontrakt may acquire and lower. The second makes
inheritance-shaped implementation look like the contract.

Helper APIs may reduce boilerplate, but helpers are not authority.

---

## 11. Source-Symbol Reference Law

References in the IDL are source-level symbols.

```text
admission XGreaterThanOne
publication ResultGreaterThanInput
```

These entries are not strings. The compiler must resolve them before lowering.

Resolution fails if a symbol is missing, ambiguous, inaccessible, or used in the wrong pipeline stage.

After resolution, the reference is lowered into protocol-owned canonical material. No runtime reference object survives.

Short form:

```text
source symbol -> compile-time resolution -> canonical material -> stable HID
```

---

## 12. Operation Symbol Law

Operation handles in the IDL are source-level operation declarations.

```text
operation calculate(input: CalculateInput): CalculateOutput
```

This declaration is not a mirror of a handwritten host method. It is the authored contract operation. Kontrakt lowers it
into a canonical operation selector and generates the host method from it.

The generated host method may later be implemented by user code, but that realization cannot redefine the contract
operation.

---

## 13. Predicate Grade Law

V1 may allow executable predicates inside one-dimensional Java/Kotlin presentation declarations.

```kotlin
val XGreaterThanOne = AdmissionContract(
    subject = CalculateInput::x,
    accept = { x: Int -> x > 1 },
    onReject = X_MUST_BE_GREATER_THAN_1,
)
```

This has executable-predicate grade. It supports execution and sampling, but it is not symbolic predicate material.

A future symbolic predicate form may enable stronger analysis and generation. V1 must leave room for that grade without
requiring it for every contract body.

---

## 14. V1 Frontend Shape

A v1 `.kontrakt` interface contract may look like this:

```text
interface CalculateContract {
    operation calculate(input: CalculateInput): CalculateOutput {
        pipeline {
            input CalculateInput
            admission XGreaterThanOne
            canonicalization DefaultPrimitiveCanonicalization
            lowering CalculateLowering
            invariant NoAdditionalInvariant
            state Stateless
            transition NoTransition
            publication ResultGreaterThanInput
            diagnostic CalculateDiagnostics
            governance DefaultGovernance
        }
    }
}
```

The corresponding one-dimensional presentations may be ordinary Kotlin:

```kotlin
val XGreaterThanOne = AdmissionContract(
    subject = CalculateInput::x,
    accept = { x: Int -> x > 1 },
    onReject = X_MUST_BE_GREATER_THAN_1,
)

val ResultGreaterThanInput = PublicationContract(
    subject = CalculateOutput::result,
    reference = CalculateOutput::x,
    accept = { result: Int, x: Int -> result > x },
    onDeny = RESULT_MUST_BE_GREATER_THAN_INPUT,
)
```

Kontrakt compiles the interface contract into a host interface and lowers the contract material into canonical form.

---

## 15. V1 Parser Scope

The v1 parser covers only the IDL interface contract subset.

It reads interface shape, operation shape, pipeline entries, source references, and source locations.

It does not own predicate language depth, host-language expression parsing, advanced composition, policy language,
state-machine depth, or editor tooling.

The frontend must remain a contract interface notation, not a general programming language.

---

## 16. Lowering Law

The lowering flow is:

```text
.kontrakt interface contract
-> parsed interface contract
-> unresolved source symbols
-> compile-time resolution
-> lowered facts
-> canonical material
-> stable identity
-> generated host interface
-> verifier and realization projections
```

Parser objects, source-file artifacts, helper objects, runtime handles, generated host artifacts, and classloader
identity must be erased before canonical authority is established.

Only Kontrakt-owned material may survive.

---

## 17. Generated Artifact Law

Generated host interfaces are reproducible build outputs.

They must be regenerated from `.kontrakt` source and must not be manually edited. If generated files are committed for
consumer convenience, the committed files are still artifacts, not authority.

Removing Kontrakt removes regeneration and verification. It does not force users to maintain Kontrakt syntax inside
handwritten host interfaces, because this model has no handwritten host interface source for generated contracts.

---

## 18. Rejected Forms

The following forms are rejected as primary frontend authority.

### 18.1. Handwritten host interface as the contract source

```kotlin
interface CalculateContract {
    fun calculate(input: CalculateInput): CalculateOutput
}
```

Rejected as complete contract source because a method signature alone does not present the contract machine.

### 18.2. Sidecar mirror of a handwritten host interface

```text
contract interface CalculateContractPresentation presents CalculateContract { ... }
```

Rejected because it creates two authored descriptions of the same operation surface.

### 18.3. Class-reference manifest binding

```kotlin
@Admission(XGreaterThanOne::class)
```

Rejected because it routes identity through the host runtime type universe.

### 18.4. Runtime function-reference operation identity

```kotlin
CalculateContract::calculate
```

Rejected as primary operation identity because it can become runtime reflection authority. Operation identity must come
from `.kontrakt` source and canonical lowering.

### 18.5. String references

```text
admission "XGreaterThanOne"
```

Rejected because identity becomes lookup-rule dependent.

### 18.6. Default method contract bodies

```kotlin
fun calculate(input: CalculateInput): CalculateOutput {
    pipeline { }
}
```

Rejected because host-language method bodies must not become contract bodies.

### 18.7. Inheritance-shaped one-dimensional contracts

```kotlin
object XGreaterThanOne : AdmissionContract<CalculateInput> {
    override fun accept(input: CalculateInput): Boolean = input.x > 1
}
```

Rejected because inheritance-shaped implementation must not define contract authority.

---

## 19. Compiler and Resolver Requirements

The frontend implementation must fail before canonical material is accepted when the interface source is inconsistent,
incomplete, ambiguous, or stage-incompatible.

The resolver must establish operation symbols, type symbols, contract references, pipeline completeness, generated
artifact consistency, and diagnostic source positions deterministically.

No runtime host handle may survive into canonical material.

---

## 20. Architecture Tests

The frontend is stable only when its boundary properties are tested.

Generated host interfaces must be reproducible from the same `.kontrakt` source. Contract references must reject string
lookup and runtime class-reference authority. Source-symbol resolution must fail deterministically for missing,
ambiguous, or wrong-stage references. Equivalent interface contracts must produce stable canonical identity across
repeated runs and must not change with runtime class loading. Declared absence must remain explicit material. Generated
host artifacts must not become authority.

---

## 21. Consequences

The accepted frontend makes the interface contract explicit, keeps operation handles and pipeline bindings together,
avoids handwritten host-interface drift, rejects host-runtime identity, keeps the custom syntax narrow, allows reusable
one-dimensional presentations, and leaves backend optimization to Kontrakt.