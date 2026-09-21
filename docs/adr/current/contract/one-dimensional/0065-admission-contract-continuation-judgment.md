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
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary

---

## 1. Context

Admission is the continuation judgment over the immutable presentation established under Input.

At the user surface, Admission asks one question:

```text
May this already-formed Input presentation continue past Admission?
```

The user authors this judgment through the normal Kontrakt authoring surface: IDL selection, the generated host-facing
interface or artifact, and one ordinary Java or Kotlin 1D Admission declaration. The user does not author or manage HIR
nodes, Established references, occurrence references, semantic projections, dense handles, protocol records, or backend
execution material.

Internally, Input remains the producer of the presentation meaning that Admission consumes. Admission does not reopen
the JVM carrier or reconstruct Input meaning. The producer-owned Input semantic protocol is a Kontrakt-internal boundary
used to preserve authority, identity, and observation law between the two 1D Contracts; it is not part of the Admission
user API.

Admission judges the same complete Input presentation that Input made judgeable. It does not replace that presentation
or establish a transformed presentation under another name. Nested products, choices, sequence elements, membership
constituents, association keys or values, presence distinctions, collection order, and Presentation Sameness remain
Input-owned meaning even when Admission observes them while computing its judgment.

There is no user transformation stage between Input and Admission. Admission source may nevertheless derive temporary
values solely to compute its judgment when the frontend can close the complete computation under Admission law.
Temporary parsing, conversion, normalization, filtering, mapping, sorting, aggregation, or another local derivation does
not become new Input, Canonicalization, Lowering, or other downstream Contract material.

Admission source code should remain ordinary Java or Kotlin syntax. Kontrakt does not require a separate user-facing
Admission expression language, predicate builder, custom rule DSL, semantic-reference API, or proof syntax. The source
declaration, method call, lambda object, getter, JVM operator, standard-library implementation, iterator, stream
pipeline, regex engine, or runtime execution path is frontend evidence only and does not become Admission authority.

---

## 2. Problem

A simple Boolean callback is too weak to be Contract authority.

It can hide runtime lookup, exception-driven choice, mutable external state, library semantics, object identity, virtual
dispatch, environment access, uncontrolled work, or implementation-dependent evaluation behavior.

At the same time, requiring users to learn a Kontrakt-specific judgment language, build Kontrakt IR, manipulate internal
semantic references, or construct custom expression nodes would duplicate compiler work and make ordinary Java or Kotlin
authoring artificial.

Admission therefore needs an ordinary Java or Kotlin source surface whose complete Admission-visible meaning can be
resolved into closed, terminating, deterministic Kontrakt-owned judgment material. The compiler should understand
familiar host-language expressions and standard value APIs rather than require users to restate the same judgment
through a second language.

The frontend must reject source whose complete Admission-visible meaning cannot be determined under the supported laws.
Frontend coverage may expand as the compiler matures, but accepting a new source form does not by itself expand
Admission authority.

The user-facing source model and the compiler-internal semantic protocol are separate. Rich internal HIR, Establishment,
Established Material, semantic references, projections, analysis summaries, and physical execution structures may be
required to implement the Contract, but none of them becomes an authoring obligation or ordinary user-visible Admission
API.

The runtime evaluator operates only on the exact established Input meaning legally supplied to Admission and the exact
closed material owned by the Admission Definition. Its realization is owned by Kontrakt and need not preserve the host
library call graph, collection implementation, iteration machinery, regex engine, allocation pattern, source
control-flow shape, or other frontend execution mechanics.

---

## 3. Decision Drivers

Admission is judgment, not downstream transformation. It may compute temporary derived values, but it does not establish
those values as a replacement presentation or another Contract's material.

Admission owns exactly two V1 judgment outcomes: `Admitted` and `Rejected`. Every legally entered Admission judgment
completes with exactly one of them. Exception, throwing completion, catch selection, resource exhaustion, compiler
unavailability, or another authority's stopping result is not a third Admission outcome.

The selected source declaration is evidence, not the final Contract representation.

Ordinary Java or Kotlin syntax is the preferred authoring surface. A Kontrakt-specific user language, semantic-handle
API, internal protocol API, or proof notation is not required.

The role comes from the explicit Admission selection in the applicable IDL or Contract World arrangement.

One selectable declaration names one flat Admission Contract.

Inheritance, member selection, runtime subtype choice, and implementation discovery must not create Admission identity.

At occurrence time, Admission obtains presented data only from the exact Input-owned presentation observation legally
supplied for that Input application. Internal Input Definition, Occurrence, Interaction, and projection references may
anchor that observation and attribution inside Kontrakt, but they are not ordinary judgment operands exposed for user
branching.

Admission Definition-owned constants and other closed definition material may participate when their complete meaning is
resolved before authority. Policy, Governance, Budget, Capacity, State, ambient run context, and unrelated established
material do not become hidden Admission operands merely because they are reachable elsewhere in the machine.

The supported source language may be expressive, but every accepted semantic path must be closed, terminating over the
legal presented material, deterministic, free of Contract-visible external side effects, and implementation-erased.

A fixed maximum work count is not automatically Admission meaning. When the established Input presentation is finite but
its exact extent varies by occurrence, a supported Admission computation may scale with that actual finite extent.
Budget, Capacity, and realization resource limits remain separately owned and must not be converted into Admission
rejection.

Hidden capabilities and runtime lookup are forbidden. Ambient locale, timezone, charset, clock, randomness, filesystem,
network, service, provider state, or process state must not silently determine Admission meaning.

`throw`, `try`, `catch`, and `finally` do not form Admission judgment meaning in V1. A JVM exception must not become an
implicit Admission rejection or an implicit Contract Failure.

A recognized Java or Kotlin library operation is frontend syntax only. Its source implementation strategy does not
survive as authority after the operation's complete relevant meaning has been resolved.

The generated evaluator may be fused, specialized, reordered, vectorized, allocation-elided, or otherwise replaced only
when every Contract-visible Admission result and any explicitly owned attribution remain identical.

---

## 4. Authority Path

Admission follows this conceptual definition-time path under the common HIR and Establishment architecture:

```text
ordinary Java or Kotlin Admission declaration
-> selected for the Admission role by the applicable IDL / Contract World relation
-> acquired by the matching frontend
-> rejected or resolved under the supported source law
-> complete implementation-erased, backend-independent Admission meaning
-> common Resolved HIR handoff
-> Admission-owned Establishment
-> Established Admission meaning
-> Kontrakt-owned execution formation and optimization
```

This path states the authority direction only. The exact Admission-specific HIR references, projections, Established
reference families, table shapes, handles, and protocol encodings are internal Kontrakt architecture and are not part of
the user-facing Admission API.

The user does not author Kontrakt IR, generated semantic-reference objects, evaluator instructions, handler objects,
adapters, runtime assembly, or a separate Admission DSL. The user declares the 1D Contract with ordinary supported Java
or Kotlin and selects it through the normal IDL surface.

Host source syntax disappears as authority after resolution. A recognized collection pipeline, stream, regex call,
helper call, standard-library operation, or local control-flow shape may disappear completely or be replaced by a
different internal algorithm.

Equivalent Java and Kotlin source with equivalent resolved Admission meaning may share the same semantic result without
requiring equivalent host call graphs, temporary allocations, iteration machinery, regex implementation, or bytecode
shape when those distinctions are not Contract-visible.

---

## 5. Selection and Declaration Law

### 5.1. Explicit Admission Selection

The applicable IDL or declared Contract World arrangement selects one exact Admission declaration for one exact
Admission role occurrence.

```text
exact selecting context
    Interaction A / Admission
        -> XGreaterThanOne
```

The diagram states the semantic selection relation rather than public token spelling. Source-layout headings do not
create authority, hierarchy, ownership, processing boundaries, namespaces, or composition.

The explicit Admission selection grants the role.

Class name, method name, package, file, annotation, parameter type, inheritance relation, runtime type, or source
co-location does not grant the role.

The manifest may use an imported simple name, but resolution must end at one exact symbol.

### 5.2. One Flat Admission Contract

One selectable class or object names one flat Admission Contract.

A selected declaration must not be a container of independently selectable child Admission contracts.

Private constants, local values, accepted helper expressions, and other source conveniences may participate in the one
root judgment when the frontend refines them completely. They do not become nested Contracts.

Several independent Admission declarations may coexist in one file because a file is source organization only.

Several legal selection occurrences may explicitly select the same Admission declaration when they intentionally require
the same Admission meaning. Physical or compiler representation may be shared while selection attribution,
applicability, occurrence meaning, rejection, failure, and diagnostic attribution remain exact to each use.

Admission inheritance, marker-interface membership, override, virtual specialization, member selection from a common
holder, and type-hierarchy reuse are prohibited as Contract meaning.

Shared meaning is reused by selecting the same flat declaration.

---

## 6. Input Dependency

Admission judges the exact presentation already established by Input for the applicable Interaction boundary.

The user-facing Admission declaration receives the ordinary generated or supported Java/Kotlin Input-facing surface. The
user does not receive `EstablishedInputOccurrenceRef`, `EstablishedInputDefinitionRef`, `EstablishedInputCoordinateRef`,
protocol projections, dense handles, or other internal semantic coordinates as the normal Admission programming model.

Internally, Admission consumes the Input-owned legal presentation observation defined by ADR-0064. That observation
supplies the exact complete immutable Established Presentation for one `Presented` Input application together with the
internal attribution needed to identify the source application. Admission must not reopen the JVM carrier, source
declaration, storage topology, or backend representation to recover Input meaning.

Admission may observe only distinctions that the exact Input presentation law makes legally available. Named Product
members, Closed Tagged Choice cases and payloads, Sequence elements and position, Membership constituents, Association
keys and values, presence distinctions, cardinality, Presentation Sameness, and observable collection order remain
Input-owned meaning. Admission may use them; it does not redefine them.

Nested constituents do not become direct Input coordinates or independent Contract authorities merely because Admission
can observe them. Likewise, Input-internal Definition, Occurrence, Interaction, and projection references provide
internal attribution and interpretation; they do not become arbitrary values that user Admission code may branch on.

Admission must not discover additional operands through undeclared carrier fields, runtime subtype inspection, host
reference graphs, repositories, services, environment, implementation objects, or another hidden capability.

Policy, Governance, Budget, Capacity, State, and unrelated Contract results do not become undeclared Admission operands.
They retain their own authority and may stop or constrain whole-machine processing under their own laws.

Admission Definition-owned constants and other closed definition-local values may participate when the frontend resolves
their complete meaning. A mutable configuration source, environment lookup, provider, service, or runtime registry is
not converted into closed Definition material merely because source code can reach it.

Admission may derive temporary values solely for its judgment when the frontend can erase the complete source
computation into closed Admission meaning. Such temporary computation may include supported parsing, conversion,
normalization, filtering, mapping, sorting, projection, aggregation, or local accumulation when the result remains
internal to the judgment.

A temporary judgment value, collection, parsed value, normalized value, sorted view, or aggregate does not become new
Input, Canonicalization output, Lowering output, Fact material, or any other downstream Contract material merely because
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

Admission is not restricted to a tiny fixed list of primitive checks. Its semantic authority is the continuation
judgment, not a catalog of Java or Kotlin method names.

The frontend may accept ordinary source expressions when their complete Admission-visible meaning can be resolved from
the exact Input-owned presentation observation and exact closed Admission Definition material. The accepted computation
may be richer than a primitive comparison when every intermediate result remains Admission-local and no additional
authority is introduced.

The source form is accepted because Kontrakt knows the complete relevant meaning and can remove the host operation as
authority. It is not accepted merely because the JVM can execute it.

Frontend coverage may grow as the compiler matures. Supporting an additional Java or Kotlin source form, library
overload, collection idiom, local mutation pattern, or control-flow shape does not expand Admission authority when it
resolves to already-defined Admission meaning. Expanding what Admission itself may observe or establish is a separate
Contract decision.

### 7.1. V1 Judgment Coverage Target

V1 should support ordinary Java and Kotlin value-oriented judgment code broadly enough that users do not need to learn a
second validation language. A source operation is included only when its complete Admission-visible meaning is closed
under an exact supported operation law and the host implementation can be erased as authority.

The V1 coverage target includes, where the exact semantic law is closed:

```text
Boolean values and explicit Boolean composition
signed and unsigned integral arithmetic, comparison, conversion, and bit relations
floating classification, ordering, equality, arithmetic, and raw-bit relations under an exact supported IEEE law
finite alternative, enum, presence, absence, null, and value relations
closed product and choice constituent observation through Input-owned presentation meaning
character, text, binary, prefix, suffix, containment, indexing, slicing, and related finite value operations
range and positional relations where the required position or order is legally observable
array, Sequence, Membership, and Association size, membership, lookup, and order-sensitive relations when the Input law exposes the required distinction
finite collection quantification such as all, any, none, and count
finite aggregation such as sum, minimum, maximum, and other fully profiled reductions
temporary filter, map, sorting, projection, grouping, and similar pipelines whose results remain Admission-local
supported large-number and decimal operations
supported temporal value operations
supported UUID, URI-reference, codec, and other closed value operations
supported pattern and regular-expression relations whose complete pattern meaning and work behavior can be closed by Kontrakt
recognized Java Stream, Kotlin collection, and locally derived Kotlin Sequence source forms when the finite Input-derived pipeline is erased as host execution authority
closed local helper bodies and non-escaping local accumulation patterns when their complete effects are determined
```

Boolean composition includes negation, conjunction, disjunction, exclusive-or, implication, equivalence, and finite
conditional choice where their semantics are explicit.

Integral operations must make width, signedness, overflow, narrowing, and shift behavior explicit where those
distinctions matter. Floating operations must preserve every result distinction required by the selected operation law;
source purity alone does not permit reassociation or another transformation that changes admitted floating results.
Decimal source forms may expose different exact relations for presentation-sensitive equality and numeric comparison
when those relations are distinct.

Input collection semantics constrain what a source operation may observe. An unordered Membership does not acquire
`first`, `last`, encounter position, JVM hash traversal, or another hidden order merely because a familiar host API
exposes one. Admission may compute a new temporary order under an exact supported ordering operation, but that derived
order remains Admission-local and does not retroactively become Input meaning.

The catalog describes a V1 frontend coverage target. It is not permission to execute arbitrary JVM behavior, and it does
not make a host type, library implementation, internal semantic protocol, or compiler analysis object part of Admission
authority. Exact supported callables and reusable compiler operation knowledge remain compiler/frontend and ADR-0073
concerns rather than user-authored Contract vocabulary.

### 7.2. Ordinary Expressions and Temporary Computation

The frontend may refine supported literals, Input presentation observations, immutable local bindings, arithmetic
expressions, comparisons, Boolean expressions, bit expressions, finite `if`, `when`, or `switch` forms, and other closed
value computations whose complete relevant meaning is known.

Admission-local mutation is not automatically external effect. A local accumulator or temporary mutable variable may be
accepted when its storage is confined to the judgment, cannot escape, cannot be observed by another authority or thread,
and the compiler can determine every read and write that contributes to the final Admission result. Mutable globals,
shared state, captured live objects, externally observable mutation, and lifecycle-bearing state remain prohibited.

A `for` traversal over an exact finite Input-derived domain may be accepted when the traversal source and body are
closed under Admission law. The Input law guarantees that an actual V1 collection presentation is finite; Admission does
not require that every such collection also have one fixed maximum cardinality unless an owning semantic law declares
one. General source `while` or `do-while` computation, recursion, or another control shape whose completion cannot be
determined under the V1 frontend remains unsupported.

Parsing, conversion, normalization, case mapping, default substitution, filtering, mapping, sorting, grouping,
projection, aggregation, and other derivations may participate as temporary judgment computation when the exact
operation meaning is supported and every derived value remains local to the judgment. Such a temporary result does not
replace the Established Input presentation and does not become Canonicalization, Lowering, Fact, or other Contract
material.

The fact that source syntax constructs a temporary object, collection, string, parsed value, wrapper, Stream, Sequence,
or regex object does not require the Admission realization to allocate or retain that host object. Kontrakt may fuse,
scalarize, precompute, specialize, or eliminate the temporary completely when the Admission-visible result is preserved.

### 7.3. Known Operation Refinement

A source-level helper or library call may participate only when Kontrakt can eliminate the call itself as Admission
authority or explicitly preserve a separately ratified platform obligation under ADR-0073.

A private or otherwise statically closed helper may be accepted only when its complete reachable body relevant to the
judgment is acyclic and can be resolved into the root Admission meaning. The helper name, call frame, generated JVM
target, or source factoring does not become Admission meaning.

Java and Kotlin standard-library operations should be supported broadly when Kontrakt has exact reusable knowledge for
the selected callable and the requested Admission use passes the applicable platform-use boundary. Examples include text
relations, total parsing forms, numeric operations, collection predicates and transformations, large-number relations,
temporal value operations, UUID or URI-reference relations, codecs, and other closed value APIs.

The user does not select a `Semantic Operation Profile`, platform-basis reference, HIR operation node, or similar
internal compiler concept. Those are Kontrakt implementation mechanisms for understanding ordinary source operations. If
an exact operation needs a meaning-affecting platform semantic basis, Kontrakt must resolve and track that basis
internally under ADR-0073 rather than exposing an internal reference as routine Admission authoring syntax.

An exact overload whose behavior depends on ambient locale, timezone, charset, clock, randomness, provider state,
process state, or another undeclared capability is not admitted merely because a nearby overload has closed semantics.
Exact callable identity and explicit semantic inputs matter.

Unknown calls, unresolved receiver behavior, unavailable helper bodies, arbitrary user-defined equality or ordering,
virtual calls whose target meaning is not closed, framework callbacks, and unprofiled library operations are rejected.

Purity is not inferred from naming, annotation, finality, standard-library membership, or Boolean return type.

### 7.4. Finite Collection, Pipeline, and Binder Condition

Admission may inspect constituents only through distinctions legally exposed by the established Input presentation.
Sequence position, Membership or Association order, uniqueness, key relation, constituent Presentation Sameness,
presence, and cardinality follow the exact Input law rather than host collection conventions.

A finite Input collection need not carry one fixed maximum cardinality merely to be usable by Admission. A recognized
traversal is legal when the compiler can determine that its work follows the actual finite presented domain through
supported operations and that no hidden iterator, provider, callback, external resource, or unbounded source extends
that domain. Fixed semantic bounds remain Input meaning only when Input owns them; execution resource limits remain
Budget, Capacity, compiler-safety, or realization concerns as applicable.

A Kotlin or Java lambda, `Predicate`, `Function`, method reference, or functional-interface instance is not Admission
material. Such syntax may participate in a recognized finite operation such as `all`, `any`, `none`, `count`, `filter`,
`map`, sorting, grouping, `sum`, minimum, maximum, or another supported reduction only when the closure does not escape
and its complete relevant body is independently resolvable under Admission law. The frontend removes the runtime
function object as authority and retains only the required Admission computation meaning.

A Java Stream or Kotlin Sequence source form may be accepted only when it is locally derived from exact finite Input or
Admission-local values, every intermediate operation is recognized, and no live Stream, iterator, lazy source, callback
object, spliterator, provider, or external carrier traversal survives as semantic authority. `parallelStream`,
externally supplied or potentially open-ended Sequence sources, stateful external pipelines, and capability-bearing
streams are not admitted as V1 Admission source merely because a terminal operation eventually returns a Boolean.

Order-sensitive operations are legal only when the required order is Input-visible or when Admission explicitly derives
a new temporary order under an exact supported ordering law. Host hash iteration, unspecified Stream encounter behavior,
physical storage order, and compiler traversal order cannot fill a missing semantic order.

Temporary filtered, mapped, sorted, grouped, or aggregated results are judgment-local. They need not be materialized
physically. Kontrakt may fuse a pipeline into one scan, use primitive or columnar access, vectorize a legal relation,
specialize constants, construct an index, or choose another equivalent execution form when every Admission-visible
distinction is preserved.

### 7.5. Completion, Exceptional Control, and Termination

Every legally entered Admission judgment must complete with exactly one of `Admitted` or `Rejected` for every legal
Input presentation to which that Admission Definition applies.

A supported operation must have a complete Admission-visible result law over the admitted source domain. Division by
zero, invalid shifts, invalid indices, narrowing loss, exact-arithmetic overflow, malformed patterns, unsupported
encodings, and similar partial or exceptional behavior must either be excluded by the compiler's supported analysis,
represented by an explicit non-exceptional result relation, or make the source unsupported. The Contract law does not
require one theorem prover, SMT engine, or proof object; the compiler may use exact operation knowledge, control-flow
analysis, range analysis, constant reasoning, origin/effect summaries, or another sound supported technique.

A host operation whose specified legal execution may complete exceptionally cannot use that exception as a branch of the
Admission judgment. Catching an exception and converting it to `Rejected`, `Admitted`, or another result is not allowed.

```text
throw
try
catch
finally
exception-driven branching
```

are not V1 Admission judgment forms. A JVM exception is not an Admission result and does not become Contract Failure
merely because it occurred while realizing Admission computation.

Finite processing over the exact finite Established Input presentation and Admission-local derived values is allowed
when the compiler can determine the traversal and call closure that may contribute to the result. A fixed
definition-time maximum step count is not required merely because the actual Input extent varies. Runtime-dependent
open-ended loops, recursion, cyclic helper calls, blocking operations, waiting, synchronization, and termination that
relies on application behavior or external progress are prohibited in V1.

Budget or Capacity exhaustion, compiler observation unavailability, VM failure, or another realization inability is not
silently converted into `Rejected`. The owner of that stopping condition retains its own result law.

### 7.6. Pattern and Regex Source Condition

Pattern and regular-expression source forms are frontend syntax only. Admission does not grant semantic authority to
`java.util.regex`, Kotlin `Regex`, another host matching engine, or that engine's backtracking strategy.

A supported pattern must be completely resolved into exact Admission-visible pattern meaning and a Kontrakt-supported
work law over the finite pattern and finite presented text or bytes. V1 support is therefore defined by the pattern
constructs and matching meaning that Kontrakt can realize predictably; merely identifying an engine as non-backtracking
is not sufficient by itself.

A pattern construct whose result semantics, resource behavior, capture behavior, Unicode or other semantic basis, or
required work cannot be closed under the supported V1 matcher law is rejected rather than delegated to an arbitrary host
regex engine.

The backend may realize the same accepted pattern meaning with a deterministic automaton, tagged automaton, specialized
matcher, fused scan, precomputed transition structure, or another equivalent bounded-resource mechanism. No particular
algorithm becomes Contract authority.

---

## 8. No Hidden Observation

Admission may not observe or invoke repositories, services, clocks, randomness, environment variables, system
properties, files, networks, transactions, threads, executors, locks, mutable globals, framework context,
dependency-injected objects, externally supplied lazy values, delegated properties with hidden observation, proxies,
reflection, runtime class inspection, object identity, resource handles, live streams, futures, or other undeclared
capabilities.

The Input-owned semantic protocol that supplies the Established Presentation is not an escape hatch for additional
observation. Internal Definition, Occurrence, Interaction, projection, handle, storage, generation, or cache coordinates
may support exact attribution and interpretation inside Kontrakt, but they do not give user Admission code a general
reflective surface over compiler state.

If additional information is required for an Admission judgment, it must belong to exact Admission Definition meaning or
arrive through another explicitly owning Contract relation. A default locale, timezone, charset, clock, random source,
provider, process setting, current Policy World, current State, or ambient run object is not silently captured merely
because the implementation can reach it.

A locally written Java Stream or Kotlin Sequence expression may be accepted only under Section 7.4 when its source
domain is exact and its host pipeline mechanics are fully removed as authority. The runtime Stream, Sequence, iterator,
lazy source, or callback does not become an Admission operand or execution authority.

Exception-driven choice, `try`/`catch` validation, runtime type discovery, inheritance-dependent behavior, and callback
completion are forbidden as Admission authority.

---

## 9. Deterministic Refinement

Before Admission authority is established, frontend and HIR formation must resolve and erase host source mechanics that
do not belong to Admission meaning.

The conceptual path is:

```text
resolve the exact Admission declaration selected by the applicable IDL / Contract World relation
-> identify the one eligible root judgment
-> resolve every Input-facing source read against the Input-owned presentation law
-> close every accepted helper and local computation that contributes to the judgment
-> resolve every accepted exact host callable through supported compiler / ADR-0073 operation knowledge
-> determine type, presence, numeric, ordering, collection, effect, exceptional-path, completion, pattern, and work-shape legality
-> erase class, object, getter, lambda, iterator, Stream, Sequence, regex-engine, exception-control, and ordinary library-call mechanics when they are not Contract-visible
-> retain only complete backend-independent Admission meaning and separate source provenance needed by legal consumers
-> hand complete pre-authority meaning through the common HIR boundary
-> Admission-owned Establishment grants authority
-> execution formation chooses the optimized realization
```

The user does not observe or manage the internal HIR references, producer projections, Established references, analysis
summaries, or physical handles used by this path.

A change to actual Admission-owned meaning must participate in the authority's normal Version and identity laws. Source
formatting, local variable names, equivalent host syntax, helper factoring, temporary allocation shape, regex-engine
choice, iterator strategy, analysis implementation, table layout, and backend instruction choice do not create new
Admission meaning when every legal Admission observation remains unchanged. Source provenance may remain available for
diagnostics without becoming Definition identity or semantic equality.

---

## 10. Deterministic Evaluation

At occurrence time, the Admission realization consumes only the exact Established Input presentation observation legally
supplied by Input and the exact closed material owned by the Established Admission Definition. The user-facing Java or
Kotlin parameter is an authoring and generated-API surface; runtime correctness does not depend on reopening that host
carrier as Contract authority.

Runtime symbol lookup, reflection, property discovery, hidden virtual dispatch, callback construction, dynamic
semantic-operation selection, arbitrary host regex-engine delegation, exception-driven result selection, and
failure-policy selection are forbidden.

Java or Kotlin source evaluation strategy is not Admission authority by itself. Source short-circuit structure,
collection iterator shape, Stream pipeline machinery, temporary allocation, local variable storage, and matching
algorithm may be replaced whenever those distinctions are not Contract-visible. An exact Input-visible order,
operation-result distinction, floating result law, or other explicitly owned observation must still be preserved.

A backend may fuse branches and pipelines, eliminate temporaries, scalarize local state, use primitive or columnar
instructions, specialize operations, construct indexes, reorder computations when their exact result law permits it,
vectorize, compile patterns into another matcher, or return allocation-free outcome codes only when every
Contract-visible Admission result and explicitly owned attribution remain identical.

Execution resource accounting may depend on the actual finite Input extent and the chosen legal realization. Budget,
Capacity, compiler-safety, or realization limits do not become hidden Admission determinants and their stopping results
do not become `Rejected`.

The Admission determinism law is expressed only over meaning that actually determines Admission:

```text
same exact Established Admission Definition meaning
+ same exact Input-owned Established Presentation meaning observed for the application
+ same exact additional Admission-owned determinant, if this ADR explicitly admits one
= same Admission result
+ same Admission-owned attribution
```

The surrounding Contract World, cache state, worker count, thread schedule, physical storage, compiler query graph, or
unrelated Contract result does not become an Admission determinant merely because it is present during execution.

---

## 11. Result Law

The logical V1 result is exactly `Admitted` or `Rejected`. A source Boolean `true` maps to `Admitted` and `false` maps
to `Rejected` only after the complete source computation has been resolved into legal Admission meaning.

`Admitted` means only that the exact presented Input satisfies the continuation condition owned by the selected
Admission Contract. It does not mean that the whole Interaction is globally authorized, that another Contract cannot
stop processing, or that downstream Contract judgments are guaranteed to succeed.

`Rejected` means only that a legally entered Admission judgment completed and determined that its own continuation
condition was not satisfied.

Exception, throwing completion, catch selection, resource exhaustion, host regex failure, iterator failure, compiler
observation failure, library callback completion, or another implementation event is not a third Admission result and
must not be mapped implicitly to `Rejected`. Such an event also does not become Contract Failure without the separate
law owned by the Failure Contract.

Established Admission meaning must preserve the exact judgment distinctions required by Admission itself. Diagnostic
evidence and source provenance may preserve permitted explanation material under their own laws without turning source
evaluation order, first-failed-clause mechanics, host exception behavior, or another discarded execution strategy into
Admission authority.

Deferred, Capacity-shaped, Budget-shaped, Policy-shaped, Governance-shaped, State-shaped, or other authority-specific
outcomes remain owned by their respective Contracts. An early stop supplied by another Contract must retain that
Contract's result and must not be converted into Admission rejection.

If source cannot be resolved into complete legal Admission meaning, Admission authority is not established from that
source. The exact compiler-owned unsuccessful-result and recovery representation remains a common compiler concern
rather than an Admission result. If a legally entered Admission occurrence evaluates its established continuation
condition as unsatisfied, Admission establishes `Rejected`.

Admission rejection stops that presented material from continuing through Admission. Rejected material does not continue
under another name. Any retained explanation belongs to Diagnostic law and must not make a discarded host execution
strategy authoritative.

---

## 12. Relationship to Neighboring Contracts

Input establishes judgeable presentation meaning and, for a `Presented` application, makes the exact complete
presentation legally observable through its producer-owned internal semantic protocol.

Admission consumes that Input-owned meaning internally and judges whether the presentation satisfies the Admission
continuation condition. The user still writes ordinary Input-facing Java or Kotlin; the internal Input protocol is not
an additional user API.

Admission does not replace, re-establish, or publish a transformed Input presentation and does not create a canonical
representative. Temporary parsing, normalization, filtering, mapping, sorting, aggregation, or other judgment-local
computation does not change that law.

If Canonicalization is selected, only admitted material reaches it.

If Canonicalization is omitted, the same Input-established presentation meaning proceeds toward Lowering without
Admission establishing a replacement value.

Admission does not perform Lowering and does not establish core Fact authority.

Policy, Governance, Budget, Capacity, State, Version, Failure, Diagnostic, and other independently applicable Contracts
retain their own authority and results. Their material does not become an implicit Admission operand merely because
whole-machine execution composes several judgments around the same Interaction.

---

## 13. Open in This Section

The exact public Java or Kotlin Admission declaration shape may change as long as the user-facing model remains ordinary
host-language authoring selected through the normal Kontrakt IDL surface. V1 does not require users to learn a separate
Admission expression language, internal semantic-reference model, or compiler protocol.

The exact supported Java and Kotlin API catalog is frontend/compiler and ADR-0073 coverage rather than Admission
Contract authority. V1 should cover common value-oriented standard-library operations broadly, but each exact operation
or overload still requires complete supported semantic knowledge and legal use in the Admission role. Runtime
executability alone is insufficient.

The exact V1 capability matrix remains to be closed. It must evaluate source operations against the Input presentation
family and observable distinctions they consume, partial or exceptional completion, external effects or ambient basis,
temporary-result escape, work behavior, and whether Kontrakt can erase or lawfully preserve the host operation without
importing host implementation authority.

Frontend expansion to additional language constructs, library operations, collection idioms, local mutation patterns,
finite loop shapes, or equivalent source forms may occur without changing Admission meaning when the new form resolves
to existing Admission semantics. Expanding Admission's semantic inputs, outputs, authority, or result vocabulary
requires a separate Contract decision.

The exact Admission-specific HIR Candidate catalog, Establishment specialization, Established reference and projection
catalog, occurrence model, and physical protocol realization remain internal architecture to be closed under ADR-0071,
ADR-0063, and the 1D master checklist. Whatever internal references and projections those decisions introduce, they are
not public authoring obligations and must not leak into the ordinary Admission declaration API.

The exact execution-cost accounting, runtime resource enforcement, and optimized matcher or collection realization
remain Design, Budget, Capacity, Verification, or backend work as applicable. Those mechanisms must preserve Admission
meaning without turning resource exhaustion or implementation availability into a third Admission result.

---

## 14. Consequences

Admission becomes a real two-result Contract judgment rather than a Boolean callback, exception-driven validation hook,
or general policy engine.

Users may write ordinary supported Java or Kotlin expressions and familiar standard-library code through the normal IDL,
generated interface or artifact, and 1D declaration surface. They do not construct Kontrakt IR, Established references,
semantic projections, operation-profile handles, proof objects, or backend execution structures.

The compiler carries the complexity of determining whether the complete source meaning is legal for Admission and of
translating that meaning into internal HIR, Establishment, Established Material, and execution products. Broader
frontend coverage therefore increases compiler responsibility rather than user-visible Contract syntax.

Temporary source collections, local accumulators, Stream or Sequence pipelines, parsed values, normalized values, regex
objects, helper calls, and similar host mechanics need not survive execution formation.

Unsupported convenience code is rejected rather than becoming hidden runtime authority. `try`/`catch`, exception-driven
validation, ambient-state operations, external capabilities, and uncontrolled execution sources do not become escape
hatches.

The generated evaluator can be specialized aggressively because its semantic surface is closed before execution
formation. Host collection algorithms, iterator protocols, temporary allocation patterns, local variable storage, source
control-flow shape, and host regex-engine behavior are replaceable frontend or realization mechanics when they are not
Contract-visible.

Finite actual Input extent and machine resource policy are separate concerns. Admission can legally process a finite
occurrence whose size varies by invocation without owning an arbitrary global maximum, while Budget, Capacity,
compiler-safety, and backend mechanisms retain responsibility for their own limits and stopping conditions.

---

## 15. Migration History

This ADR was extracted mechanically from the Admission-owned material of ADR-0048.

The extraction itself does not change the accepted Admission Contract semantics.

A 2026-09-22 review aligned Admission with the closed ADR-0064 Input presentation and consumer law and separated the
public authoring model from Kontrakt-internal semantic protocols. Users remain on IDL, generated host surfaces, and
ordinary Java or Kotlin 1D declarations; Input and Admission references, projections, HIR, Establishment material, and
physical handles remain internal. The review replaced the stale direct-coordinate-only Admission operand model with
consumption of Input-owned complete presentation meaning, allowed nested presentation observation without promoting
constituents to Input coordinates, and kept Input Presentation Sameness and collection-order law authoritative. It also
broadened Admission-local temporary computation while keeping derived values non-authoritative, admitted non-escaping
local accumulation and finite Input-derived traversal, separated finite semantic completion from Budget and Capacity
resource limits, prohibited exception-driven judgment, strengthened regex support around closed pattern meaning and
known work behavior rather than a host backtracking engine, and reaffirmed that Java and Kotlin library syntax is
frontend evidence whose execution strategy may be replaced by Kontrakt.

ADR-0048 remains the owner of the shared inbound-airlock composition and direct Input-to-Admission adjacency.