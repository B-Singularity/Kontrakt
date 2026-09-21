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
May this already-formed Input presentation continue past Admission?
```

Admission judges the same presentation that Input made judgeable. It does not replace that presentation or establish a
transformed presentation under another name.

There is no user transformation region between Input and Admission. Admission source may nevertheless derive temporary
values solely to compute its judgment when the frontend can refine the complete computation into finite, total,
deterministic Admission meaning. Temporary parsing, conversion, normalization, filtering, mapping, aggregation, or
another pure derivation does not become new Input, Canonicalization, Lowering, or other downstream Contract material.

Admission source code should remain ordinary Java or Kotlin syntax. Kontrakt does not require a separate user-facing
Admission expression language, predicate builder, or custom rule DSL. The source declaration, method call, lambda
object, getter, JVM operator, standard-library implementation, iterator, stream pipeline, regex engine, or runtime
execution path is frontend evidence only and does not become Admission authority.

---

## 2. Problem

A simple Boolean callback is too weak to be Contract authority.

It can hide runtime lookup, exception-driven choice, mutable state, library semantics, object identity, virtual
dispatch, environment access, unbounded work, or implementation-dependent evaluation order.

At the same time, requiring users to learn a Kontrakt-specific predicate language, build Kontrakt IR, or construct
custom expression nodes would duplicate compiler work and make ordinary Java or Kotlin authoring artificial.

Admission therefore needs an ordinary Java or Kotlin source surface that can be completely refined into finite, total,
deterministic Kontrakt-owned judgment material. The compiler should understand familiar host-language expressions and
standard value APIs rather than require users to restate the same judgment through a second language.

The frontend must reject source whose complete meaning cannot be known. Frontend coverage may expand as the compiler
matures, but accepting a new source form does not by itself expand Admission authority.

The runtime evaluator must operate only on ratified Input values and fixed Contract material. Its realization is owned
by Kontrakt and need not preserve the host library call graph, iteration machinery, regex engine, allocation pattern,
source control-flow shape, or other frontend execution mechanics.

---

## 3. Decision Drivers

Admission is judgment, not downstream transformation. It may compute temporary derived values, but it does not establish
those values as a replacement presentation or another Contract's material.

Admission owns exactly two V1 judgment outcomes: `Admitted` and `Rejected`. Exception, throwing completion, catch
selection, or another exceptional control path is not a third Admission outcome.

The selected source declaration is evidence, not the final Contract representation.

Ordinary Java or Kotlin syntax is the preferred authoring surface. A Kontrakt-specific user language is not required.

The role comes from the explicit `admission` slot.

One selectable declaration names one flat Admission Contract.

Inheritance, member selection, runtime subtype choice, and implementation discovery must not create Admission identity.

Every semantic operand must come from ratified Input material or statically ratifiable literal material.

The supported source language may be expressive, but every accepted semantic path must be finite, total, deterministic,
side-effect-free with respect to Contract meaning, and implementation-erased.

Hidden capabilities and runtime lookup are forbidden. Ambient locale, timezone, charset, clock, randomness, filesystem,
network, service, or process state must not silently determine Admission meaning.

`throw`, `try`, `catch`, and `finally` do not form Admission judgment meaning in V1. A JVM exception must not become an
implicit Admission rejection or an implicit Contract Failure.

A recognized Java or Kotlin library operation is frontend syntax only. Its implementation strategy does not survive as
authority after refinement.

The generated evaluator may be optimized, fused, specialized, reordered, vectorized, or otherwise replaced only when
every Contract-visible Admission result and any explicitly owned attribution remain identical.

---

## 4. Authority Path

Admission follows this definition-time law:

```text
ordinary Java or Kotlin declaration
-> selected by the Operation's `admission` slot
-> acquired by the matching frontend
-> rejected or refined under one deterministic source law
-> translated into implementation-erased, backend-independent Admission judgment material
-> ratified as Admission authority
-> formed and optimized under Kontrakt-owned execution law
-> generated deterministic evaluator
```

The user does not author Kontrakt IR, generated coordinate objects, evaluator instructions, handler objects, adapters,
runtime assembly, or a separate Admission DSL.

Host source syntax disappears as authority after refinement. A recognized collection pipeline, stream, regex call,
helper call, or standard-library operation may disappear completely or be replaced by a different internal algorithm.

Equivalent Java and Kotlin source with equivalent refined meaning must produce equivalent Admission material. Equivalent
refined meaning does not require equivalent host call graphs, temporary allocations, iteration order, regex
implementation, or bytecode shape when those distinctions are not Contract-visible.

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

Every semantic operand must resolve to declared Input presentation meaning, a constituent legally observable through
that established Input presentation, or statically ratifiable literal material. Nested products, choices, sequences,
memberships, associations, and their constituents are observed through Input-owned semantic presentation law rather than
through arbitrary host-object traversal.

Admission must not discover operands through undeclared carrier fields, runtime subtype inspection, reference graphs,
repositories, services, environment, implementation objects, or another hidden capability.

Policy, Governance, Budget, and Capacity do not become undeclared Admission operands. They retain their own authority
and may stop the flow under their own laws.

Admission may derive temporary values solely for its judgment when the frontend can erase the complete source
computation into deterministic judgment material. Such temporary computation may include supported parsing, conversion,
normalization, filtering, mapping, projection, or aggregation when the result remains internal to the judgment.

A temporary judgment value, collection, parsed value, normalized value, or aggregate does not become new Input,
Canonicalization output, Lowering output, Fact material, or any other downstream Contract material merely because
Admission computed it.

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

Admission is not restricted to a tiny fixed list of primitive predicates. Its semantic authority is the continuation
judgment, not a fixed catalog of Java or Kotlin method names.

The frontend may accept ordinary source expressions when their complete meaning can be reduced to finite, total,
deterministic material over ratified Input presentation meaning and literals. The accepted computation may be richer
than a primitive predicate when every intermediate result remains internal to the judgment.

The source form is accepted because Kontrakt knows the complete meaning and erases the host operation. It is not
accepted merely because the JVM can execute it.

Frontend coverage may grow as the compiler matures. Supporting an additional Java or Kotlin source form, library
overload, collection idiom, or control-flow shape does not expand Admission authority when it refines to already-defined
Admission meaning. Expanding what Admission itself may observe or establish is a separate Contract decision.

### 7.1. V1 Judgment Coverage Target

V1 should support ordinary Java and Kotlin value-oriented judgment code broadly enough that users do not need to learn a
second validation language. A source operation is included only when its complete meaning is available through an exact
frontend semantic profile and can be erased from authority.

The V1 coverage target includes, where the exact semantic law is closed:

```text
Boolean values and explicit Boolean composition
signed and unsigned integral arithmetic, comparison, conversion, and bit relations
floating classification, ordering, equality, arithmetic, and raw-bit relations under declared IEEE law
finite alternative, enum, presence, absence, null, and value relations
closed product and choice constituent observation through established Input presentation meaning
character, text, binary, prefix, suffix, containment, indexing, slicing, and related bounded value operations
range and bounded positional relations
array, sequence, membership, and association size, membership, lookup, and observable-order relations where owned by Input
bounded collection quantification such as all, any, none, and count
bounded aggregation such as sum, minimum, maximum, and other fully profiled finite reductions
temporary filter, map, projection, and similar finite pipelines whose results do not escape the Admission judgment
versioned semantic profiles for supported large-number and decimal operations
versioned semantic profiles for supported temporal value operations
versioned semantic profiles for supported UUID, URI-reference, identifier-like, codec, and other closed value operations
supported pattern and regular-expression relations that can be refined into bounded non-backtracking Admission evaluation
recognized Java Stream, Kotlin collection, and locally derived Kotlin Sequence source forms when the complete finite pipeline is erased before execution
```

Boolean composition includes negation, conjunction, disjunction, exclusive-or, implication, equivalence, and finite
conditional choice where their semantics are explicit.

Integral operations must make width, signedness, overflow, narrowing, and shift behavior explicit where those
distinctions matter. Floating operations must make NaN, signed zero, total ordering, or raw-bit treatment explicit
rather than inheriting an accidental host default. Decimal source forms may expose different exact relations for
presentation-sensitive equality and numeric comparison when the selected semantic profile distinguishes them.

The catalog describes a V1 frontend coverage target. It is not permission to execute arbitrary JVM behavior, and it does
not make a host type or library implementation part of Admission authority. Exact supported overloads and profiles
remain compiler/frontend knowledge.

### 7.2. Ordinary Expressions and Temporary Computation

The frontend may refine supported literals, Input presentation reads, immutable local bindings, arithmetic expressions,
comparisons, Boolean expressions, bit expressions, finite `if`, `when`, or `switch` forms, and other closed value
computations whose complete meaning is known.

A finite `for` traversal over an exact finite Input-derived domain may be accepted when the frontend can refine the
complete loop into finite Admission judgment material. General `while` or `do-while` execution is not admitted merely
because the source happens to terminate in ordinary tests.

Parsing, conversion, normalization, case mapping, default substitution, filtering, mapping, projection, aggregation, and
other derivations may participate as temporary judgment computation when the selected frontend owns their complete
semantic meaning. Such a temporary result does not replace the established Input presentation and does not become
Canonicalization, Lowering, Fact, or other Contract material.

The fact that source syntax constructs a temporary object, collection, string, parsed value, or wrapper does not require
the Admission realization to allocate or retain that host object. Kontrakt may fuse or eliminate the temporary
completely.

### 7.3. Known Operation Refinement

A source-level helper or library call may participate only when Kontrakt can eliminate the call as semantic authority.

A private non-overridable helper may be accepted only when its entire acyclic body is closed and refined into the root
judgment. The helper name, call frame, or generated JVM target does not become Admission meaning.

Java and Kotlin standard-library operations should be supported broadly when the selected frontend owns a stable,
versioned semantic profile for the exact operation. Examples include text relations, total parsing forms, numeric
operations, collection predicates and transformations, large-number relations, temporal value operations, UUID or
URI-reference relations, and other closed value APIs. The host call is removed and replaced by backend-independent
Admission semantic material.

An exact overload whose behavior depends on ambient locale, timezone, charset, clock, randomness, process state, or
another undeclared capability is not admitted merely because a nearby overload has a closed semantic profile. The
profile belongs to the exact source operation and its explicit semantic inputs.

Unknown calls, unresolved receiver behavior, unavailable extension bodies, arbitrary user-defined equality or ordering,
virtual calls whose target meaning is not closed, framework callbacks, and unprofiled library operations are rejected.

Purity is not inferred from naming, annotation, finality, standard-library membership, or Boolean return type.

### 7.4. Finite Collection, Pipeline, and Binder Condition

Admission may inspect the constituents of an established Input aggregate when the Input presentation law makes those
constituents legally observable and the complete operation is finite under a definition-time-known bound. Sequence
position, Membership or Association order, duplicate treatment, key relation, constituent sameness, presence, and
cardinality follow the established Input law rather than host collection conventions.

A Kotlin or Java lambda, `Predicate`, `Function`, method reference, or functional-interface instance is not Admission
material. Such syntax may participate in a recognized finite operation such as `all`, `any`, `none`, `count`, `filter`,
`map`, `sum`, minimum, maximum, or another profiled reduction only when the lambda does not escape and its complete body
is independently refinable under Admission law. The frontend lowers the source form to explicit finite binder,
projection, and reduction meaning.

A Java Stream or Kotlin Sequence source form may be accepted only when it is locally derived from exact finite Admission
inputs, every intermediate operation is recognized, and no live stream, iterator, lazy pipeline, callback object,
spliterator, or external carrier traversal survives as runtime authority. Parallel, externally supplied, open-ended,
stateful, or capability-bearing pipelines are not admitted.

Temporary filtered, mapped, or aggregated results are judgment-local. They need not be materialized physically. Kontrakt
may fuse a pipeline into one scan, use primitive or columnar access, vectorize a legal relation, specialize constants,
or choose another proven-equivalent execution form.

### 7.5. Totality, Exceptional Control, and Termination

Every accepted Admission judgment must be total for every presentation admitted by the selected Input Contract and must
terminate under a definition-time-known bound.

Division by zero, invalid shifts, invalid indices, narrowing loss, exact-arithmetic overflow, malformed patterns,
unsupported encodings, and similar partial or exceptional paths must be ruled out by static proof, represented by an
explicit total semantic relation, or rejected.

A host operation that may throw may participate only when every exceptional path is proven unreachable for the exact
admitted domain before Admission authority is established. Catching an exception and converting it to `Rejected`,
`Admitted`, or another judgment is not allowed.

```text
throw
try
catch
finally
exception-driven branching
```

are not V1 Admission judgment forms. A JVM exception is not an Admission result and does not become Contract Failure
merely because it occurred while evaluating Admission source.

Finite processing over established bounded Input material is allowed when the frontend can close the complete work.
Finite `for` traversal may therefore be refined when its domain and body are closed. Runtime-dependent unbounded loops,
recursion, cyclic helper calls, blocking operations, waiting, synchronization, and termination that relies on
application behavior are prohibited in V1.

### 7.6. Pattern and Regex Source Condition

Pattern and regular-expression source forms are frontend syntax only. Admission does not grant authority to
`java.util.regex`, Kotlin `Regex`, or another host matching engine.

A supported pattern must be completely refined into a closed Admission semantic profile with a statically bounded
evaluation law. V1 must not retain host backtracking behavior as the runtime judgment mechanism. A pattern form whose
complete meaning cannot be realized under the supported bounded non-backtracking law is rejected rather than delegated
to the host regex engine.

The backend may realize the same accepted pattern meaning with a deterministic automaton, specialized matcher, fused
scan, or another proven-equivalent bounded mechanism.

---

## 8. No Hidden Observation

Admission may not observe or invoke repositories, services, clocks, randomness, environment variables, system
properties, files, networks, transactions, threads, executors, locks, mutable globals, framework context,
dependency-injected objects, externally supplied lazy values, delegated properties with hidden observation, proxies,
reflection, runtime class inspection, object identity, resource handles, live streams, futures, or other undeclared
capabilities.

If information is required for the judgment, it must first become explicit Contract material through an owning boundary.
A default locale, timezone, charset, clock, random source, or process setting is hidden observation unless the exact
semantic input is explicitly part of a legal Admission basis or fixed semantic profile.

A locally written Java Stream or Kotlin Sequence expression may be accepted only under Section 7.4 when it is fully
refined away. The runtime Stream, Sequence, iterator, lazy pipeline, or callback does not become an Admission operand or
execution authority.

Exception-driven choice, `try`/`catch` validation, runtime type discovery, inheritance-dependent behavior, and callback
completion are forbidden as Admission authority.

---

## 9. Deterministic Refinement

Before Admission receives authority, definition-time processing must resolve and erase the host source.

The required path is:

```text
resolve the exact class or object named by the Operation's `admission` slot, which is written beneath the `flow` source-layout label
-> identify the one eligible root judgment
-> close and validate every accepted helper body
-> bind every semantic value read to ratified Input material or a closed literal
-> resolve every accepted host expression and exact library operation to versioned Kontrakt semantic material
-> validate type, null, numeric, ordering, aggregate, pattern, totality, exceptional-path, and bound laws
-> erase class, object, method, getter, lambda, iterator, stream, regex-engine, exception-control, and library-call mechanics
-> prepare deterministic compiler representation for the complete judgment meaning while keeping source provenance separate
-> derive stable Admission material identity
-> ratify and publish the material in the ContractImage
-> form and optimize the deterministic Admission evaluator under Kontrakt-owned execution law
```

Contract identity must change when a frontend profile, numeric law, string law, collection law, pattern law, approved
scalar-profile law, or any other semantic refinement changes Contract meaning. Source formatting, local variable names,
equivalent host syntax, helper factoring, standard-library call shape, temporary allocation shape, regex-engine choice,
iterator strategy, and backend instruction choice must not change identity when they refine to the same Admission
meaning. Source provenance may remain available for diagnostics without becoming Definition identity or semantic
equality.

---

## 10. Deterministic Evaluation

At invocation time, the generated evaluator reads only the already-formed Input presentation through fixed ratified
semantic access and fixed Contract material. Temporary derived values are evaluator-internal unless another owning
Contract has independently established them.

Runtime symbol lookup, reflection, property discovery, hidden virtual dispatch, callback construction, dynamic
semantic-profile selection, host regex-engine delegation, exception-driven result selection, and failure-policy
selection are forbidden.

Java or Kotlin source evaluation strategy is not Admission authority. Source short-circuit structure, collection
iterator shape, Stream pipeline machinery, temporary allocation, and regex matching algorithm may be replaced whenever
those distinctions are not Contract-visible. An explicitly owned order distinction must still be preserved when
Admission law or another exact supplying Contract makes that order observable.

A backend may fuse branches and pipelines, eliminate temporaries, use primitive or columnar instructions, specialize
profiled operations, reorder pure total predicates, vectorize, compile patterns into bounded non-backtracking matchers,
or return allocation-free outcome codes only when the Contract-visible Admission outcome and any explicitly owned
attribution remain identical.

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

The logical V1 result is exactly `Admitted` or `Rejected`. A source Boolean `true` maps to `Admitted` and `false` maps
to `Rejected` only after the complete expression has been refined and ratified.

Exception, throwing completion, catch selection, host regex failure, iterator failure, library callback completion, or
another implementation event is not a third Admission result and must not be mapped implicitly to `Rejected`. It also
does not become Contract Failure without the separate law owned by the Failure Contract.

The canonical Admission material must preserve enough semantic judgment structure and separate source provenance for
deterministic Failure and Diagnostic Contracts to attribute rejection without executing the source method or depending
on the original host evaluation strategy.

Deferred, Capacity-shaped, Policy-shaped, or Governance-shaped outcomes remain owned by their respective Contracts. An
early stop supplied by another Contract must retain that Contract's result and must not be converted into Admission
rejection.

If the source cannot be completely refined under these laws, the Contract definition is rejected before ContractImage
publication. If a ratified Input presentation fails the established Admission judgment, Admission produces `Rejected`.
The generated evaluator is implementation-axis machinery and is wrong if it disagrees with the ratified Admission
material.

Admission rejection stops the presented material. Rejected material does not continue under another name. Any retained
explanation belongs to Diagnostic law and must not make a discarded host execution strategy authoritative.

---

## 12. Relationship to Neighboring Contracts

Input establishes judgeable presentation.

Admission judges whether that presentation may continue.

Admission does not replace, re-establish, or publish a transformed Input presentation and does not create a canonical
representative. Temporary parsing, normalization, filtering, mapping, aggregation, or other judgment-local computation
does not change that law.

If Canonicalization is selected, only admitted material reaches it.

If Canonicalization is omitted, admitted material reaches Lowering unchanged.

Admission does not perform Lowering and does not establish core Fact authority.

A Budget, Capacity, Policy, Governance, or Version result observed while Admission is active remains owned by the
supplying Contract.

---

## 13. Open in This Section

The exact public Java or Kotlin declaration shape may change as long as ordinary host-language authoring remains
refinable under this law. V1 does not require users to learn a separate Kontrakt Admission expression language.

The exact supported Java and Kotlin API catalog is frontend/compiler coverage rather than Admission Contract authority.
V1 should cover common value-oriented standard-library operations broadly, but each exact operation or overload requires
a complete versioned semantic profile. Runtime execution permission alone is insufficient.

Frontend expansion to additional language constructs, library operations, collection idioms, finite loop shapes, or
equivalent source forms may occur without changing Admission meaning when the new form refines to existing Admission
semantics. Expanding Admission's semantic inputs, outputs, or authority requires a separate Contract decision.

---

## 14. Consequences

Admission becomes a real two-result Contract judgment rather than a Boolean callback or exception-driven validation
hook.

Users may write ordinary supported Java or Kotlin expressions and familiar standard-library code without constructing
Kontrakt IR or learning a separate Admission DSL.

The compiler pays the cost of proving that the complete source meaning can be erased into finite, total, deterministic
Admission material. Broader frontend coverage therefore increases compiler responsibility rather than user-visible
Contract syntax.

Temporary source collections, stream pipelines, parsed values, normalized values, regex objects, helper calls, and
similar host mechanics need not survive execution formation.

Unsupported convenience code is rejected rather than becoming hidden runtime authority. `try`/`catch`, exception-driven
validation, ambient-state operations, and uncontrolled runtime capabilities do not become escape hatches.

The generated evaluator can be specialized aggressively because its semantic surface is already ratified and closed.
Host collection algorithms, iterator protocols, temporary allocation patterns, source control-flow shape, and host
regex-engine behavior are erased frontend mechanics when they are not Contract-visible. Regex realization still obeys
the bounded non-backtracking law in Section 7.6.

---

## 15. Migration History

This ADR was extracted mechanically from the Admission-owned material of ADR-0048.

The extraction itself does not change the accepted Admission Contract semantics.

ADR-0048 remains the owner of the shared inbound-airlock composition and direct Input-to-Admission adjacency.