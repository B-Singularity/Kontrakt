# ADR-0065: Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0066: Canonicalization Contract
- ADR-0064: Input Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary

---

## 1. Context

Admission is the continuation judgment over the immutable presentation established under Input.

It asks one question:

```text
May this already-formed boundary presentation continue through this Operation?
```

Admission judges the same presentation that Input made judgeable.

There is no user transformation region between Input and Admission.

Admission must not create, copy, snapshot, parse, coerce, normalize, discover, or reconstruct Input material. A source
that must perform those actions before it can decide continuation belongs in another responsibility.

Admission source code may be ordinary Java or Kotlin syntax. That source is frontend evidence. The source object, method
call, lambda object, getter, JVM operator, or runtime execution path does not become Admission authority.

---

## 2. Problem

A simple Boolean callback is too weak to be Contract authority.

It can hide runtime lookup, exception-driven choice, mutable state, library semantics, object identity, virtual
dispatch, environment access, unbounded work, or implementation-dependent evaluation order.

At the same time, requiring users to build Kontrakt IR or custom expression nodes would duplicate compiler work and make
authoring artificial.

Admission therefore needs an ordinary source surface that can be completely refined into finite, total, deterministic
Kontrakt-owned judgment material.

The frontend must reject source whose complete meaning cannot be known.

The runtime evaluator must operate only on ratified Input values and fixed Contract material.

---

## 3. Decision Drivers

Admission is judgment, not transformation.

The selected source declaration is evidence, not the final Contract representation.

The role comes from the explicit `admission` slot.

One selectable declaration names one flat Admission Contract.

Inheritance, member selection, runtime subtype choice, and implementation discovery must not create Admission identity.

Every runtime operand must come from ratified Input material or statically ratifiable literal material.

The supported source language may be expressive, but every accepted path must be finite, total, deterministic, and
implementation-erased.

Hidden capabilities and runtime lookup are forbidden.

A JVM exception must not become an implicit Admission result.

The generated evaluator may be optimized only when its observable outcome and attribution remain identical.

---

## 4. Authority Path

Admission follows this definition-time law:

```text
ordinary Java or Kotlin declaration
-> selected by the Operation's `admission` slot
-> acquired by the matching frontend
-> rejected or refined under one deterministic source law
-> lowered into implementation-erased Kontrakt judgment material
-> ratified as Admission authority
-> generated deterministic evaluator
```

The user does not author Kontrakt IR, generated coordinate objects, evaluator instructions, handler objects, adapters,
or runtime assembly.

Host source syntax disappears as authority after refinement.

Equivalent Java and Kotlin source with equivalent refined meaning must produce equivalent Admission material.

---

## 5. Selection and Declaration Law

### 5.1. Manifest-Slot Selection

The operation manifest selects one exact Admission declaration.

```text
manifest {
    flow:
        input      CalculateInput
        admission  XGreaterThanOne
}
```

The source-layout heading does not create authority, hierarchy, ownership, processing boundary, namespace, or
composition.

The `admission` slot grants the role.

Class name, method name, package, file, annotation, parameter type, inheritance relation, runtime type, or source
co-location does not grant the role.

The manifest may use an imported simple name, but resolution must end at one exact symbol.

### 5.2. One Flat Admission Contract

One selectable class or object names one flat Admission Contract.

A selected declaration must not be a container of independently selectable child Admission contracts.

Private constants, local values, accepted helper expressions, and other source conveniences may participate in the one
root judgment when the frontend refines them completely. They do not become nested Contracts.

Several independent Admission declarations may coexist in one file because a file is source organization only.

Several Operations may explicitly select the same Admission declaration. The lowered definition may be structurally
shared, while operation binding, applicability, rejection, failure, and diagnostic attribution remain exact to each use.

Admission inheritance, marker-interface membership, override, virtual specialization, member selection from a common
holder, and type-hierarchy reuse are prohibited as Contract meaning.

Shared meaning is reused by selecting the same flat declaration.

---

## 6. Input Dependency

Admission judges ratified Input material from the same Operation.

Every runtime operand must resolve to one declared Input coordinate, a value exposed through an approved direct scalar
or opaque-leaf profile, or a statically ratifiable literal.

Admission must not discover operands through nested carriers, interface relations, runtime subtype inspection, reference
graphs, repositories, services, environment, or implementation objects.

Policy, Governance, Budget, and Capacity do not become undeclared Admission operands. They retain their own authority
and may stop the flow under their own laws.

Admission may derive temporary values solely for its judgment when the frontend can erase the source computation into
deterministic judgment material.

A temporary judgment value does not become Canonicalization or Lowering output.

### 6.1. Illustrative Source

A source declaration may remain ordinary host code when the frontend can erase it completely into Admission material.

```kotlin
package example.calculate

data class CalculateInput(
    val x: Int,
    val limit: Int,
    val flags: Int,
)

object XGreaterThanOne {
    private const val MINIMUM = 1
    private const val REQUIRED_FLAGS = 0b0011

    fun admit(input: CalculateInput): Boolean {
        val requiredFlagsPresent =
            (input.flags and REQUIRED_FLAGS) == REQUIRED_FLAGS

        return input.x > MINIMUM &&
                input.x <= input.limit &&
                requiredFlagsPresent
    }
}
```

The object, method, local variable, JVM operators, and returned host Boolean are source mechanics. Authority begins only
after their complete judgment meaning has been refined and ratified.

---

## 7. Supported Judgment Source Law

Admission is not restricted to a tiny fixed list of primitive predicates.

The frontend may accept ordinary source expressions when their complete meaning can be reduced to finite, total,
deterministic material over ratified Input coordinates and literals.

This can include Boolean composition, arithmetic and comparison, explicit numeric relations, finite alternatives, null
and presence relations, supported string or binary relations, and operations belonging to versioned Kontrakt semantic
profiles.

The source form is accepted because Kontrakt knows the complete meaning and erases the host operation.

It is not accepted merely because the JVM can execute it.

### 7.1. Complete V1 Judgment Surface

The V1 semantic judgment surface may include the following when the complete meaning is ratified:

```text
Boolean values and explicit Boolean composition
signed and unsigned integral arithmetic and comparison
explicit-width bit relations and conversions
floating classification, ordering, equality, and raw-bit relations under declared IEEE law
finite alternative and enum identity relations
explicit null, absence, presence, and value relations
character, text, pattern, and binary relations under declared units and encodings
direct-coordinate equality and ordering that do not invoke user `equals`, `hashCode`, or `compareTo`
fixed-index and bounded operations over approved text, binary, identifier, numeric, and temporal scalar profiles
versioned Kontrakt semantic profiles for supported JVM large-number, identifier, and temporal operations
```

Boolean composition includes negation, conjunction, disjunction, exclusive-or, implication, equivalence, and finite
conditional choice where their semantics are explicit.

Integral operations must make width, signedness, overflow, narrowing, and shift behavior explicit where those
distinctions matter. Floating operations must make NaN, signed zero, total ordering, or raw-bit treatment explicit
rather than inheriting host defaults.

The catalog describes semantic coverage. It is not permission to execute arbitrary JVM behavior.

### 7.2. Ordinary Expressions

The frontend may refine supported literals, direct Input coordinate reads, immutable local bindings, arithmetic
expressions, comparisons, Boolean expressions, bit expressions, and finite exhaustive conditional forms.

Parsing, coercion, normalization, default substitution, representation repair, or replacement-value production are not
Admission responsibilities unless the interpreted value already exists as Input material under an earlier declared
boundary.

### 7.3. Known Operation Refinement

A source-level helper or library call may participate only when Kontrakt can eliminate the call as semantic authority.

A private non-overridable helper may be accepted only when its entire acyclic body is closed and refined into the root
judgment.

A Java or Kotlin standard-library surface may be accepted only when the selected frontend owns a stable, versioned
semantic profile for that exact operation. For example, `String.startsWith` or a finite numeric operation may serve as
source syntax only when the host call is removed and replaced by backend-independent prefix or numeric material.

Unknown calls, user-defined receiver behavior, custom predicates, user-defined equality or ordering, unavailable
extension bodies, method references, virtual calls, framework callbacks, and unprofiled library operations are rejected.

Purity is not inferred from naming, annotation, finality, or Boolean return type.

### 7.4. Bounded Direct-Value Operation Condition

Admission may inspect the internal units of an approved direct scalar or opaque-leaf profile only when the Input
Contract and active Capacity or Budget material close the required access bound and Kontrakt owns the complete semantic
operation. Examples include bounded text prefix, bounded binary index, numeric classification, or another fixed profile
operation.

A Kotlin or Java lambda, `Predicate`, `Function`, method reference, or functional-interface instance is not Admission
material. V1 does not use such a value to traverse user-owned collections or nested Input structure. A recognized
frontend operation must lower directly to finite Kontrakt judgment material; no runtime function object, iterator,
callback, or external carrier traversal survives.

### 7.5. Totality and Termination

Every accepted Admission judgment must be total for every presentation admitted by the selected Input Contract and must
terminate under a definition-time-known bound.

Division by zero, invalid shifts, invalid indices, narrowing loss, exact-arithmetic overflow, malformed patterns,
unsupported encodings, and similar undefined or exceptional paths must be ruled out by static proof, represented by an
explicit total relation, or rejected. A JVM exception must never become an implicit Admission refusal.

Finite processing internal to an approved direct scalar or opaque-leaf profile is allowed only under its ratified bound.
Arbitrary `while` or `do-while` loops, runtime-dependent unbounded loops, recursion, user-owned carrier traversal,
cyclic helper calls, blocking operations, waiting, synchronization, and termination that relies on application behavior
are prohibited in V1. The semantic judgment surface may be rich; the machine must still know before publication that
every invocation completes under the declared bounds.

---

## 8. No Hidden Observation

Admission may not observe or invoke repositories, services, clocks, randomness, environment variables, system
properties, files, networks, transactions, threads, executors, locks, mutable globals, framework context,
dependency-injected objects, lazy values, delegated properties, proxies, reflection, runtime class inspection, object
identity, resource handles, streams, futures, or other undeclared capabilities.

If information is required for the judgment, it must first become explicit Contract material through an owning boundary.

Exception-driven choice, catch-based validation, runtime type discovery, inheritance-dependent behavior, and callback
completion are also forbidden as Admission authority.

---

## 9. Deterministic Refinement

Before Admission receives authority, definition-time processing must resolve and erase the host source.

The required path is:

```text
resolve the exact class or object named by the Operation's `admission` slot, which is written beneath the `flow` source-layout label
-> identify the one eligible root judgment
-> close and validate every accepted helper body
-> bind every value read to ratified Input material or a canonical literal
-> resolve every accepted host expression to versioned Kontrakt semantic material
-> validate type, null, numeric, ordering, approved scalar-profile, totality, and bound laws
-> erase class, object, method, getter, lambda, iterator, and library-call mechanics
-> canonicalize judgment structure, literals, source coordinates, and evaluation law
-> derive stable Admission material identity
-> ratify and publish the material in the ContractImage
-> generate the deterministic Admission evaluator
```

Contract identity must change when a frontend profile, numeric law, string law, approved scalar-profile law, evaluation
law, or any other semantic refinement changes Contract meaning. Source formatting, local variable names, equivalent host
syntax, and backend instruction choice must not change identity when they lower to the same material.

---

## 10. Deterministic Evaluation

At invocation time, the generated evaluator reads only the already-formed Input presentation through fixed ratified
coordinates.

Runtime symbol lookup, reflection, property discovery, method dispatch, callback construction, literal parsing, operator
selection, and failure-policy selection are forbidden.

Boolean composition and any bounded direct-value inspection must have a fixed evaluation law. V1 preserves a
deterministic declared or canonical order wherever order can affect first decisive judgment, Diagnostic Evidence, Budget
consumption, or Failure attribution.

A backend may fuse branches, use primitive instructions, specialize profiled operations, vectorize, or return
allocation-free outcome codes only when the Contract-visible outcome and attribution remain identical.

The determinism law is:

```text
same ratified ContractImage
+ same immutable Input presentation
+ same declared cross-Contract world
= same Admission outcome
+ same Contract-owned attribution
```

---

## 11. Result Law

The logical V1 result is admitted or rejected. A source Boolean `true` maps to admitted and `false` maps to rejected
only after the complete expression has been refined and ratified.

The canonical Admission material must preserve enough judgment structure and source coordination for deterministic
Failure and Diagnostic Contracts to attribute rejection without executing the source method.

Deferred, Capacity-shaped, Policy-shaped, or Governance-shaped outcomes remain owned by their respective Contracts. An
early stop supplied by another Contract must retain that Contract's result and must not be converted into Admission
rejection.

If the source cannot be completely refined under these laws, the Contract definition is rejected before ContractImage
publication. If a ratified Input presentation fails the generated judgment, Admission produces the declared rejection
result. The generated evaluator is implementation-axis machinery and is wrong if it disagrees with the ratified
Admission material.

Admission rejection stops the presented material. Rejected material does not continue under another name. Any retained
explanation belongs to Diagnostic law.

---

## 12. Relationship to Neighboring Contracts

Input establishes judgeable presentation.

Admission judges whether that presentation may continue.

Admission does not reconstruct Input and does not create a canonical representative.

If Canonicalization is selected, only admitted material reaches it.

If Canonicalization is omitted, admitted material reaches Lowering unchanged.

Admission does not perform Lowering and does not establish core Fact authority.

A Budget, Capacity, Policy, Governance, or Version result observed while Admission is active remains owned by the
supplying Contract.

---

## 13. Open in This Section

The exact public Java or Kotlin syntax for Admission declarations may change as long as the source can still be
completely refined under this law.

Frontend expansion to additional host operations requires a complete versioned semantic profile. Runtime execution
permission alone is insufficient.

---

## 14. Consequences

Admission becomes a real Contract judgment rather than a Boolean callback.

Users may write ordinary supported host expressions without constructing Kontrakt IR.

The compiler pays the cost of proving that the source can be erased into finite, total, deterministic material.

Unsupported convenience code is rejected rather than becoming hidden runtime authority.

The generated evaluator can be specialized aggressively because its semantic surface is already ratified and closed.

---

## 15. Migration History

This ADR was extracted mechanically from the Admission-owned material of ADR-0048.

The extraction itself does not change the accepted Admission Contract semantics.

ADR-0048 remains the owner of the shared inbound-airlock composition and direct Input-to-Admission adjacency.