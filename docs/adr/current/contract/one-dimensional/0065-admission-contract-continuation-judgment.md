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
- ADR-0075: Compiler-Produced Material and Knowledge, Protocol-Mediated Consumption, Validity, Reuse, and Incremental
  Boundaries
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary

---

## 1. Context

Admission is the continuation judgment over the immutable presentation established under Input.

Input owns the occurrence-level presentation judgment before Admission begins. A legally entered Input application
therefore establishes its own `Presented` or `Refused` occurrence result under ADR-0064. Admission does not authorize,
create, or retroactively cancel that Input Occurrence. Only an exact `Presented` Input Occurrence can become the
determining source of an Admission semantic application.

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

The Admission occurrence judgment owns exactly two V1 outcomes: `Admitted` and `Rejected`. A legally entered Admission
semantic application whose owning judgment completes establishes exactly one of them. Exception, throwing completion,
catch
selection, resource exhaustion, compiler unavailability, trust loss, or another authority's stopping result is not a
third Admission outcome. If the machinery cannot establish a trustworthy Admission result, it must not fabricate
`Admitted` or `Rejected`; the common compiler or realization unsuccessful-result boundary remains separate.

The selected source declaration is evidence, not the final Contract representation.

Ordinary Java or Kotlin syntax is the preferred authoring surface. A Kontrakt-specific user language, semantic-handle
API, internal protocol API, or proof notation is not required.

The role comes from the explicit Admission selection in the applicable IDL or Contract World arrangement.

One selectable declaration names one flat Admission Contract.

Inheritance, member selection, runtime subtype choice, and implementation discovery must not create Admission identity.

At occurrence time, Admission obtains presented data only from the exact Input-owned presentation observation legally
supplied for that Input application. The source Input Occurrence must already be established with the result
`Presented`; an Input Occurrence established as `Refused` does not reach Admission. One Admission judgment observes one
coherent already-established presentation. It does not refresh, reopen, or re-read the originating host carrier while
judging. Internal Input Definition, Occurrence, Interaction, and projection references may anchor that observation and
attribution inside Kontrakt, but they are not ordinary judgment operands exposed for user branching.

Admission Definition-owned constants and other closed definition material may participate when their complete meaning is
resolved before authority. The compiler must acquire that meaning without executing user class initialization, object
initialization, constructor side effects, service lookup, provider lookup, or another application behavior merely to
discover the Contract. A source `final`, Kotlin `val`, object member, or static member is not closed Definition material
merely because its reference cannot be reassigned.

Policy, Governance, Budget, Capacity, State, ambient run context, and unrelated established material do not become
hidden Admission operands merely because they are reachable elsewhere in the machine.

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

Java or Kotlin syntax does not grant legality by itself. Property access, operators, equality, indexing, destructuring,
interpolation, casts, type tests, collection syntax, and other concise forms must first resolve to the exact operation
or Input-owned observation they denote. A recognized Java or Kotlin library operation is frontend syntax only. Its
source implementation strategy does not survive as authority after the operation's complete relevant meaning has been
resolved.

A Closed Tagged Choice case may be observed through ordinary Java or Kotlin syntax when the frontend resolves that
syntax to the Input-owned closed choice relation. That is distinct from arbitrary runtime subtype discovery, reflection,
or host class-hierarchy authority.

The generated evaluator may be fused, specialized, reordered, vectorized, allocation-elided, or otherwise replaced only
when every Contract-visible Admission result, required guarded legality, operation-result distinction, and any
explicitly owned attribution remain identical.

---

## 4. Authority Path

Admission follows this conceptual definition-time path under the common HIR and Establishment architecture:

```text
ordinary Java or Kotlin Admission declaration
-> selected for the Admission role by the applicable IDL / Contract World relation
-> acquired by the matching frontend
-> resolved into complete frontend-known meaning or ends in a compiler-owned unsuccessful result
-> complete implementation-erased, backend-independent Admission Definition Candidate meaning
-> common Resolved HIR seal and visible handoff
-> Admission Definition judgment
-> Established Admission Definition or Admission Definition Refusal
-> successful Established Admission Definition may enter Kontrakt-owned execution formation and optimization
```

This path states the authority direction only. Admission Definition formation and Establishment do not make one exact
Input Definition part of Admission Definition identity or Admission-owned meaning merely because an Admission
occurrence will later consume Input. A complete Admission Definition instead owns the complete Input-observation
requirements needed by its judgment. The exact selected Input Definition and exact selected Admission Definition are
connected by the owning
post-Establishment composition relation, which must determine that the selected Input-owned semantic surface satisfies
those already-complete Admission requirements without supplying missing Admission Definition meaning.

This relation must not arise from accidental structural similarity, host type coincidence, source co-location, or
compiler convenience. Each Input and Admission IDL Binding Candidate retains only its own exact selection relation under
ADR-0071. Exact Input-to-Admission compatibility is a separate Contract-owned composition relation and does not become
HIR Binding Candidate meaning.

Cross-responsibility formation, compatibility, or reuse work may request legal Input or Admission observations through
Kontrakt-controlled mediation under ADR-0075. Mediation may resolve, obtain, qualify, compare, retain, or reuse compiler
material, but it does not acquire Admission authority, add a semantic determinant, or establish the Input-to-Admission
Contract relation. The exact Admission-specific HIR references, semantic projections, and Established reference families
remain internal Kontrakt architecture. Their physical tables, handles, protocol dispatch, and encodings remain
replaceable realization and are not part of the user-facing Admission API.

Admission follows a separate occurrence-time authority path:

```text
exact Established Admission Definition
+ fresh Admission application requiring one Presented Input Occurrence
-> ADR-0048 inbound-airlock composition supplies the exact legal upstream relation
-> exact Basis Binding to one already-Established Presented Input Occurrence
-> Applicability confirms that binding for the exact dependent application; no additional Applicable Context is required in V1
-> Complete Basis
-> one Admission semantic application legally enters
-> obtain one coherent approved Input-owned Established Presentation observation for that exact occurrence
   through Kontrakt-controlled mediation
-> Admission-owned judgment completes
-> Admitted or Rejected
-> Established Admission Occurrence Material for that exact application
-> Admitted may continue; Rejected stops continuation
```

Starting physical evaluation does not by itself publish Established Admission Occurrence Material. If evaluation cannot
establish one trustworthy Admission result, no `Admitted` or `Rejected` occurrence result is fabricated. Admission also
does not rewrite or erase the already-established Input Occurrence that supplied the presentation.

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

## 6. Input Observation and Composition Boundary

Admission judges the exact presentation already established by Input for the applicable Interaction boundary.

Admission Definition meaning is not defined by, owned by, or identified through one exact Input Definition. The
Admission Definition must be complete before exact Input/Admission composition and must state the complete Input
semantic
observations that its judgment requires. Input independently owns the semantic surface it provides. The separate
composition relation connects the exact selected definitions and determines whether the selected Input-owned surface
satisfies the already-complete Admission requirements.

The composition relation does not create missing Admission meaning, specialize Admission by silently importing Input
Definition identity, or make Input meaning Admission-owned. Conversely, Admission does not become a structural
constraint system that accepts any producer merely because host fields, JVM types, collection shapes, or source names
appear compatible.
Compatibility must follow an explicit Contract-owned matching law rather than accidental structural duck typing.

The user-facing Admission declaration may receive the ordinary generated or supported Java/Kotlin Input-facing surface
as authoring evidence. That host type or generated artifact does not make the corresponding exact Input Definition part
of Admission Definition identity. During resolution, the Admission frontend states the Input semantic observations it
needs, and Kontrakt-controlled mediation obtains approved observations from the producer-owned Input HIR protocol. The
selected Input Candidate may therefore participate in frontend formation without becoming an Admission Definition
determinant. The resulting Admission Definition Candidate retains only the complete Admission-owned observation
requirements and other Admission meaning that survive source erasure.

The user does not receive `EstablishedInputOccurrenceRef`, `EstablishedInputDefinitionRef`,
`EstablishedInputCoordinateRef`,
protocol projections, dense handles, or other internal semantic coordinates as the normal Admission programming model.

Internally, Admission requires the Input-owned legal presentation observation defined by ADR-0064. Kontrakt-controlled
mediation supplies one coherent approved observation of the exact complete immutable Established Presentation for the
`Presented` Input application together with the internal attribution required by that protocol. Admission does not
select an Input implementation, reconcile stale producer state, or reopen the JVM carrier, source declaration, storage
topology, or backend representation to recover Input meaning.

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
their complete meaning without executing user initialization as an acquisition mechanism. A mutable configuration
source, environment lookup, provider, service, runtime registry, class-initialization side effect, or
object-construction side effect is not converted into closed Definition material merely because source code can reach
it.

Admission may derive temporary values solely for its judgment when the frontend can erase the complete source
computation into closed Admission meaning. Such temporary computation may include supported parsing, conversion,
normalization, filtering, mapping, sorting, grouping, projection, aggregation, or local accumulation when the result
remains internal to the judgment.

A temporary judgment value, collection, parsed value, decoded value, normalized value, sorted view, grouped value, or
aggregate does not become new Input, Canonicalization output, Lowering output, Fact material, or any other downstream
Contract material merely because Admission computed it. A later Contract or realization consumer may not treat that
temporary interpretation as already established merely because Admission used it successfully. If later semantics
require the same parsed, decoded, normalized, or otherwise derived meaning, the authority that owns that later meaning
must establish or consume it under its own law.

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

### 6.2. Input Occurrence and Admission Occurrence

Input and Admission own separate occurrence meaning. Input first completes its own occurrence judgment. A `Presented`
Input Occurrence may then become the exact determining source of one Admission semantic application. A `Refused` Input
Occurrence remains an established Input result and does not create an Admission semantic application.

A fresh `Presented` Input Occurrence that reaches Admission creates a fresh Admission semantic application even when an
earlier Input Occurrence exposed Presentation-Same material and the same Admission Definition deterministically produces
the same result. Repeated physical evaluation of the same exact Admission application does not create additional
occurrence authority. Parallel evaluation, speculative execution, cache validation, or another realization technique may
converge on the result of that same application without creating another semantic occurrence.

Established Admission Occurrence Material exists only after the Admission-owned judgment for that application completes
and establishes `Admitted` or `Rejected`. Its occurrence meaning preserves the exact applied Admission Definition, the
exact determining `Presented` Input Occurrence, and the exact Admission result. It does not copy the complete Input
presentation, the surrounding Contract World, or transitive attribution merely because a downstream compiler subsystem
may later need to observe the occurrence.

Occurrence establishment and physical retention are separate. The physical backing of an Admission Occurrence may later
be reclaimed under the applicable retention and realization laws without changing the fact that the Admission result was
established. Conversely, retaining bytes, logs, cache entries, or diagnostic evidence does not extend Admission
authority
or make an occurrence current again.

### 6.3. Definition Candidate Meaning and Semantic Equality

One complete Admission Definition Candidate contains only meaning owned by Admission. Its logical meaning consists of
the
complete Admission judgment law, the complete Input-observation requirement needed by that judgment, and any closed
Admission-local material or exact semantic-law reference whose change can change Admission Definition meaning. The
selected Input Candidate, IDL selection context, source provenance, host representation, query dependency, cache state,
HID, fingerprint, execution plan, and physical storage topology do not enter that meaning merely because they
participate
in formation or realization.

Admission Definition Meaning equality is producer-owned. Two Admission Definition Meanings are semantically equal only
when every Admission-owned Definition determinant is equal under the law that owns that determinant. Equal meaning does
not establish Candidate Reference equality, authoritative Definition identity, Contract Version equality, or Occurrence
identity. It also does not merge separately identified Candidates or Definitions. Physical interning, shared backing,
cached work, or another reuse mechanism remains compiler realization and cannot create semantic equality or authority.

### 6.4. Resolved Admission HIR Candidate Protocol

Admission specializes the common ADR-0071 HIR Protocol with typed Admission references and producer-owned projections.
The minimum logical catalog is:

```text
AdmissionDefinitionCandidateRef
AdmissionIDLBindingCandidateRef

Admission Definition Candidate Projection
Admission IDL Binding Candidate Projection

Fine-Grained Semantic Projection
    Admission Input Observation Requirement Projection
```

The **Admission Definition Candidate Projection** is complete for the full Admission Definition Candidate meaning. The
**Admission IDL Binding Candidate Projection** exposes only the exact selection relation owned by ADR-0071 and does not
copy Candidate payload or Input-to-Admission compatibility meaning.

The **Admission Input Observation Requirement Projection** is complete for the Input semantic observations required by
one exact Admission Definition Candidate. Admission owns that requirement and its semantic equality. Two complete
Admission Candidates may differ while their Requirement Projections remain semantically equal when the distinction that
changed does not alter what Input meaning the Admission judgment requires. Equal Requirement Projection meaning does not
merge Candidate References or Definitions and does not by itself establish current reuse validity.

This fine-grained projection exists because compatibility and other legal consumers can require the Admission-owned
Input
requirement without depending on the complete Admission judgment body. It is not a consumer-specific
`CompatibilityProjection`, `DiagnosticProjection`, `PBTProjection`, or backend view. Additional fine-grained Admission
projections require an independently useful legal observation boundary; this ADR does not create one projection for each
source read, member, operator, method, query, or physical field.

A logical projection does not imply one object, record, table, query, cache entry, allocation, or physical read. Several
logical projections may share immutable backing or be computed together. Kontrakt-controlled mediation resolves and
qualifies cross-responsibility requests against these producer-owned surfaces; query routing, dependency recording,
fingerprinting, persistence, current-validity checks, and physical protocol dispatch remain compiler architecture or
Design rather than Admission meaning.

The Input HIR observations used while forming an Admission Candidate are compiler formation dependencies. They are not
the Admission-owned Input Observation Requirement itself, and the exact Input Candidate observed during formation does
not survive as an Admission determinant unless a separate Admission law explicitly makes that identity semantic.

### 6.5. Admission Definition Establishment Judgment

The Admission Definition judgment legally enters only after one complete Admission Definition Candidate is available as
valid Visible HIR. It consumes the complete Admission Definition Candidate meaning through the Resolved Admission HIR
Candidate Protocol together with the exact authoritative Version prerequisite required by ADR-0053 and ADR-0063. The
Admission IDL Binding Candidate, the Input IDL Binding Candidate, an exact Input Definition Candidate, and an exact
Established Input Definition are not inputs to Admission Definition Establishment merely because later composition or
occurrence judgment will relate them.

The judgment has two Admission-owned Definition-time semantic results:

```text
complete resolved Admission Candidate
+ meaning that satisfies the Admission Definition law
    -> Established Admission Definition

complete resolved Admission Candidate
+ meaning that violates the Admission Definition law
    -> Admission Definition Refusal
```

`Admission Definition Refusal` exists only after a complete resolved Candidate legally enters the owning Definition
judgment. Frontend refinement may preserve a completely known Candidate that the Admission Definition law will later
refuse; semantic illegality that can be represented exactly must not be collapsed into compiler unsupported merely to
avoid the owning judgment. If source meaning cannot be resolved, a required semantic reference is unresolved, the HIR
handoff is invalid, material is recovery-tainted, or the compiler otherwise cannot provide valid Visible HIR, the
Admission Definition judgment does not enter. That condition remains a compiler-owned unsuccessful result rather than
Admission Definition Refusal.

HIR seal verification does not perform this Admission judgment. Conversely, Admission Establishment must not reopen
source, host structure, query topology, cache state, backing containment, or producer-private state to recover missing
Candidate meaning. Successful Establishment creates one complete authoritative Admission Definition; partial
Establishment is not visible. `Admission Definition Refusal` is distinct from occurrence-time `Rejected`, from Input
`Refused`, and from the separately owned Failure Contract.

Reuse, persistence, parallel formation, or incremental repair may avoid repeated compiler work only after the applicable
current-validity and reference conditions are satisfied. Such machinery cannot turn an earlier Established Definition or
Definition Refusal into authority for a different current Candidate. The owning current Admission Definition judgment
remains the authority source.

### 6.6. Definition Identity, Version, and References

ADR-0053 owns Admission Contract Authority continuity and Contract Version semantics. ADR-0063 owns the common
Established Definition identity and reference law. Admission specializes those laws without adding a second identity
system.

One explicitly declared Admission Contract denotes one Admission Contract Authority. In the current flat Admission
model, one Admission Authority owns one independently addressable Admission Definition for each Contract Version.
Admission therefore requires no additional Authority-Local Definition Coordinate.

```text
exact Admission Contract Authority
+
exact Contract Version
    -> exact versioned Admission Definition
```

Before Establishment, `AdmissionDefinitionCandidateRef` designates the exact resolved Admission Contract Authority
coordinate together with the exact authority-scoped Resolved Version Candidate Coordinate. The authored Version Claim
must resolve to that exact candidate coordinate before the Admission Definition Candidate becomes valid Visible HIR.
Source declaration order, operation position, selected Input identity, HIR generation, HID, fingerprint, dense handle,
or table position does not complete this reference.

Establishment forms `EstablishedAdmissionDefinitionRef` from the exact Owning Admission Authority Reference and the
exact
Version Binding under ADR-0063. `AdmissionDefinitionCandidateRef` is not cast, promoted, or physically reinterpreted as
`EstablishedAdmissionDefinitionRef` merely because Establishment succeeds.

```text
AdmissionDefinitionCandidateRef
    = exact resolved Admission Authority coordinate
    + exact Resolved Version Candidate Coordinate

EstablishedAdmissionDefinitionRef
    = exact Owning Admission Authority Reference
    + exact Version Binding
```

Operation, Interaction, exact Input Definition, and the ADR-0048 Input-to-Admission composition relation remain outside
Admission Definition identity. Several legal operation selections may therefore select the same Admission Definition
without minting another Definition. Different compositions may connect that same Definition to different exact Input
Definitions without redefining Admission.

A Contract-visible change to Admission Definition meaning participates in the normal Admission Version law when the same
Authority continues. Source refactoring, equivalent host syntax, helper factoring, backend choice, physical layout, or
another realization-only change does not create a new Admission Definition merely because the compiler representation
changed. Equal Definition Meaning also does not merge distinct Contract Versions or authoritative Definition References.
If conflicting Definition Meaning is presented for the same exact Admission Authority and Version coordinate, the common
ADR-0053 and ADR-0063 conflict law applies; compiler storage or discovery order cannot choose between them.

### 6.7. Occurrence Basis, Applicability, and Composition Boundary

Admission owns one occurrence-time Basis Requirement Law. It requires one coherent Input occurrence rather than one
Required Basis per field, member, constituent, or source read.

```text
Admission occurrence Basis Requirement Law

requirement coordinate
    presentedInput

required meaning
    one Established Input Occurrence
    whose Input result is Presented

cardinality
    exactly one

permitted absence
    no
```

The reusable requirement law belongs to Admission Definition meaning. The exact Required Basis instance belongs to one
fresh Admission semantic application. It does not preselect one Input producer or make one exact Input Definition a
Definition determinant.

ADR-0048 owns the shared inbound-airlock composition and the direct Input-to-Admission adjacency. That composition
relation supplies the exact legal upstream relation used by occurrence-time Basis Resolution. Basis Resolution therefore
does not search the Contract World, choose the first compatible producer, inspect host types, follow query reachability,
or infer a source from physical adjacency.

For one exact Admission application, legal Basis Resolution forms one exact Basis Binding from its `presentedInput`
requirement to the exact already-Established `Presented` Input Occurrence supplied by the applicable inbound-airlock
composition. Individual Input observations remain observations of that one coherent source occurrence; they do not
become separate Required Basis instances.

Basis Binding and Applicability remain distinct under ADR-0063. Current V1 Admission requires no additional
meaning-determining Applicable Context beyond the exact Basis Binding and exact dependent Admission application. The
absence of additional Applicable Context is a semantic decision, not permission to consult ambient Policy, State,
Governance, current Version, runtime state, or compiler state.

A legal Basis Binding formed under the exact ADR-0048 composition to one `Presented` Input Occurrence is Applicable for
that exact Admission application. A `Refused` Input Occurrence or an occurrence from an Input Definition that is not the
legal upstream source under the applicable composition does not become a convenient bound source and then get repaired
by an `Inapplicable` result; the legal Basis Binding is not formed in the first place.

Exactly one Applicable Basis constitutes Complete Basis for the occurrence judgment. Zero Applicable Basis instances
leave the prerequisite incomplete and prevent the Admission occurrence judgment from legally entering. More than one
violates the Admission exactly-one singularity law. Admission defines no arbitration or first-match rule for this
requirement. Incomplete or invalid prerequisite formation does not fabricate `Rejected`, Contract Failure, or another
Admission occurrence result.

The exact representation and placement of the ADR-0048 composition relation, Basis Binding, Applicability evidence, or
Complete Basis are not fixed by this ADR. Semantic distinctions may share a physical table, slab, relation encoding, or
execution plan when all required references and legal observations remain recoverable. A compiler dependency edge,
cache entry, query edge, or retained compatibility proof cannot replace the Contract-owned composition or Basis
relation.

### 6.8. Established Admission Semantic Protocol

Admission exposes Established meaning through a small producer-owned Protocol. The minimum typed reference domains are:

```text
EstablishedAdmissionDefinitionRef
EstablishedAdmissionOccurrenceRef
```

No third Admission-specific exact reference domain is required in V1. Requirement meaning and outcome meaning remain
parts of Definition or Occurrence meaning rather than independently established authority subjects.

The **Established Admission Definition Projection** is complete for one exact Established Admission Definition. It
exposes the exact `EstablishedAdmissionDefinitionRef`, its exact Owning Admission Authority Binding and Version Binding,
and the complete local Admission Definition Meaning established by Section 6.5. Reference equality identifies the exact
Definition subject. Producer-owned Definition Meaning equality compares its complete semantic content. Neither equality
relation substitutes for the other.

The **Established Admission Occurrence Projection** is complete for one exact Established Admission Occurrence. It
exposes the exact `EstablishedAdmissionOccurrenceRef`, the exact applied `EstablishedAdmissionDefinitionRef`, the exact
determining `Presented` Input Occurrence through the occurrence's Applicable Basis attribution, and the exact
`Admitted` or `Rejected` result. It does not recursively embed the complete Input presentation, surrounding Contract
World, provenance graph, or transitive semantic dependency graph.

The **Direct Established Relation Projection** may expose the two direct relations owned by Admission occurrence
meaning:

```text
Established Admission Occurrence
    -> exact Established Admission Definition

Established Admission Occurrence
    -> exact determining Presented Input Occurrence
       through Applicable Basis attribution
```

A reverse index from one Input Occurrence to later Admission Occurrences is not an inverse Contract relation merely
because a compiler representation can provide that lookup. If such an index is useful, it remains a derived compiler
product.

Admission defines only two fine-grained Established projections in V1.

The **Established Admission Input Observation Requirement Projection** is anchored to one exact
`EstablishedAdmissionDefinitionRef` and is complete for the Admission-owned Input Observation Requirement Law of that
Definition. Its semantic equality is producer-owned and compares exactly that requirement meaning. Two Admission
Definitions may have equal Requirement Projection meaning while their complete Definition Meaning differs. This equality
does not merge Definitions, Versions, or authoritative references and does not by itself establish current reuse
validity.

The **Admission Occurrence Outcome Projection** is anchored to one exact `EstablishedAdmissionOccurrenceRef` and exposes
only its established `Admitted` or `Rejected` result. Equal outcome values do not collapse distinct Occurrences or
permit
an older Occurrence to substitute for a fresh semantic application.

Admission does not define field-specific, operator-specific, `DiagnosticProjection`, `PBTProjection`, verifier-specific,
or backend-specific Contract projections. A consumer may select a legal producer-defined projection, but it may not
redefine its completeness, semantic equality, or source authority. A consumer that combines Admission observations with
other material and derives new compiler knowledge owns a new compiler product unless another explicit Contract authority
owns that new meaning.

Protocol availability and semantic authority remain separate. Not retained, not materialized, unavailable, unsupported,
stale, corrupt, and semantically absent states must not be collapsed. Failure to obtain a required legal observation
does
not revoke already Established Admission meaning and does not fabricate `Admission Definition Refusal`, `Rejected`, or
Contract Failure. ADR-0075 owns cross-responsibility mediation, current-validity qualification, retention, persistence,
and reuse. Clean, cached, persistent, incrementally repaired, and parallel realization paths must expose the same legal
Admission observation for the same current-valid semantic basis.

No Protocol projection requires one object, query, table, record, allocation, or physical read. Primitive arrays, slabs,
dense handles, columnar relations, shared immutable backing, or another representation may realize several logical
observations together as long as typed reference domains, semantic completeness, coherence, and producer-owned equality
remain recoverable.

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
expressions, comparisons, Boolean expressions, bit expressions, finite `if`, `when`, or `switch` forms, early `return`,
local `break` or `continue`, and other closed value computations whose complete relevant meaning is known. These source
constructs are control-flow evidence; they do not become separate Contract outcomes or nested Admission authorities.

Admission-local mutation is not automatically external effect. A local accumulator or temporary mutable value may be
accepted when the state is freshly formed for the judgment, is not an alias for mutable Input or Definition material, is
not shared, cannot escape, cannot be observed by another authority or thread, and the compiler can determine every read
and write that contributes to the final Admission result. `val` or `final` on a reference does not by itself satisfy
this law. Mutable globals, shared state, aliased live objects, externally observable mutation, and lifecycle-bearing
state remain prohibited.

A `for` traversal over an exact finite Input-derived domain may be accepted when the traversal source and body are
closed under Admission law. The Input law guarantees that an actual V1 collection presentation is finite; Admission does
not require that every such collection also have one fixed maximum cardinality unless an owning semantic law declares
one. A simple `while` or `do-while` source form may also be accepted when the V1 frontend can determine its progress,
legal observation domain, and completion from supported closed material. General state-dependent loops, recursion, or
another control shape whose completion cannot be determined under the V1 frontend remain unsupported. That unsupported
status is a compiler-coverage limit, not a declaration that every such Java or Kotlin syntax form is permanently outside
Admission semantics.

Parsing, conversion, normalization, case mapping, default substitution, filtering, mapping, sorting, grouping,
projection, aggregation, and other derivations may participate as temporary judgment computation when the exact
operation meaning is supported and every derived value remains local to the judgment. Such a temporary result does not
replace the Established Input presentation and does not become Canonicalization, Lowering, Fact, or other Contract
material.

If an Admission-local operation introduces equality, ordering, uniqueness, key equivalence, grouping, tie-breaking,
duplicate resolution, or another relation that was not already supplied by Input, that relation must itself be an exact
supported semantic operation. Host `equals`, `hashCode`, comparator behavior, declaration order, hash iteration,
stable-sort accident, or library duplicate policy cannot silently fill a missing relation.

The fact that source syntax constructs a temporary object, collection, string, parsed value, wrapper, Stream, Sequence,
or regex object does not require the Admission realization to allocate or retain that host object. Kontrakt may fuse,
scalarize, precompute, specialize, or eliminate the temporary completely when the Admission-visible result is preserved.

### 7.3. Known Operation Refinement

A source-level helper or library call may participate only when Kontrakt can eliminate the call itself as Admission
authority or explicitly preserve a separately ratified platform obligation under ADR-0073.

A private, top-level, static, final, or otherwise exact-target helper may be accepted when its complete semantically
reachable behavior relevant to the judgment can be resolved into the root Admission meaning. Visibility alone is not the
semantic criterion. Multi-target runtime dispatch remains unsupported unless the frontend can resolve the complete legal
behavior without making host dispatch authority.

Helper closure includes implicit execution that can affect meaning: property getters, delegated access, constructors
used by the helper, class or object initialization, operator overloads, default-argument machinery, synthetic bridge
behavior when relevant, nested calls, and platform operations. A helper is not closed merely because its explicit source
body appears short. Ratified platform operations may satisfy this requirement through exact ADR-0073 operation knowledge
rather than by reopening or reimplementing their library bodies.

The helper name, call frame, generated JVM target, or source factoring does not become Admission meaning.

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

Purity is not inferred from naming, annotation, finality, standard-library membership, `const`, `val`, `final`, or
Boolean return type. Likewise, a familiar source spelling is not presumed safe: property access may invoke a getter or
delegate, an operator may resolve to user code, equality may invoke host equality, indexing may invoke a callable, and a
field read may trigger class initialization. Legality follows the resolved semantic operation and reachable effects, not
surface syntax.

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

Lambda legality also depends on invocation semantics, not capture syntax alone. A known sequential inline binder may
permit a fresh non-escaping Admission-local accumulator when its invocation count and order are closed by the recognized
operation. A Stream or callback API that may elide, duplicate, reorder, parallelize, or otherwise vary invocation cannot
make captured mutation part of Admission meaning unless that exact invocation law is itself closed and legal. General
side-effecting Stream pipelines are therefore unsupported in V1 even when an equivalent explicit `for` loop would be
legal.

A Java Stream or Kotlin Sequence source form may be accepted only when it is locally derived from exact finite Input or
Admission-local values, every intermediate operation is recognized, and no live Stream, iterator, lazy source, callback
object, spliterator, provider, or external carrier traversal survives as semantic authority. `parallelStream`,
externally supplied or potentially open-ended Sequence sources, stateful external pipelines, and capability-bearing
streams are not admitted as V1 Admission source merely because a terminal operation eventually returns a Boolean.

Order-sensitive operations are legal only when the required order is Input-visible or when Admission explicitly derives
a new temporary order under an exact supported ordering law. Host hash iteration, unspecified Stream encounter behavior,
physical storage order, and compiler traversal order cannot fill a missing semantic order.

When a derived ordering over previously unordered material becomes observable through `first`, `last`, `min`, `max`,
tie-sensitive grouping, or another operation, the ordering and tie law must resolve every Admission-visible distinction
required by that observation. A comparator that reports equality for Input-distinct values does not create a
deterministic first element merely because a host sort is stable. Conversely, a later result that is invariant to all
legal tied permutations need not invent an observable tie order.

Reduction legality is operation-specific. Side-effect freedom does not imply that reassociation, tree reduction,
vectorization, or parallel reduction preserves meaning. Floating arithmetic, rounded Decimal operations, saturating
arithmetic, string concatenation, and other non-associative or order-sensitive operations retain their exact result law.
A backend may reorder or reassociate only where the exact semantic operation permits it.

Temporary filtered, mapped, sorted, grouped, or aggregated results are judgment-local. They need not be materialized
physically. Kontrakt may fuse a pipeline into one scan, use primitive or columnar access, vectorize a legal relation,
specialize constants, construct an index, or choose another equivalent execution form when every Admission-visible
distinction is preserved.

### 7.5. Completion, Exceptional Control, and Termination

Every legally entered Admission judgment must complete with exactly one of `Admitted` or `Rejected` for every legal
Input presentation to which that Admission Definition applies.

A supported operation must have a complete Admission-visible result law over every semantically reachable admitted
source path. Source that is unreachable under already-resolved closed conditions does not become Admission meaning
merely because a forbidden token or operation appears textually in the body; a compiler that cannot establish that
unreachability may still reject the source conservatively as a V1 coverage limitation.

Division by zero, invalid shifts, invalid indices, narrowing loss, exact-arithmetic overflow, malformed patterns,
unsupported encodings, and similar partial or exceptional behavior must either be excluded by the compiler's supported
analysis, represented by an explicit non-exceptional result relation, or make the source unsupported. A guard that makes
a later observation or operation legal is semantically relevant control dependence even when source evaluation order
itself is not authority. An optimizer must not speculate, hoist, duplicate, or reorder a partial operation onto a path
where its legal domain has not been established.

The Contract law does not require one theorem prover, SMT engine, or proof object; the compiler may use exact operation
knowledge, control-flow analysis, range analysis, constant reasoning, origin/effect summaries, or another sound
supported technique.

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
definition-time maximum step count is not required merely because the actual Input extent varies. V1 may accept
recognized `while` or `do-while` forms when their progress and completion follow a closed finite domain that the
compiler can determine. Runtime-dependent open-ended loops, recursion, cyclic helper calls, blocking operations,
waiting, synchronization, and termination that relies on application behavior or external progress remain unsupported in
V1. Recursion or a more general loop form is not thereby declared permanently outside Admission semantics; future
frontend coverage may admit it only if the same closure, completion, effect, and determinism laws remain satisfied.

Budget or Capacity exhaustion, compiler observation unavailability, VM failure, or another realization inability is not
silently converted into `Rejected`. The owner of that stopping condition retains its own result law.

### 7.6. Hermetic Source Acquisition and Initialization

Kontrakt must not execute user Admission code in order to discover Admission meaning. Frontend acquisition may inspect
and resolve source, compiler metadata, bytecode or other compiler-owned representation, and ratified platform knowledge,
but user class initialization, object initialization, constructors, getters, delegates, service registries, or
application callbacks are not Contract-discovery mechanisms.

A compile-time literal or another supported closed initializer may contribute Definition material when the frontend can
resolve its complete meaning without executing application behavior. A `static final` field, Kotlin `val`, object
member, cached singleton, or immutable reference is not automatically such material. The referent may be mutable,
initialization may perform effects, and class initialization may itself execute arbitrary user code.

Only semantically reachable initialization or access behavior that contributes to the selected Admission meaning
matters. The compiler may erase a provably unreachable source path, but it must not execute that path to discover
whether it is harmless.

### 7.7. Pattern and Regex Source Condition

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
reflection, arbitrary runtime class inspection, object identity, resource handles, live streams, futures, coroutines,
`Flow`, channels, reactive publishers, asynchronous completion, or other undeclared capabilities.

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

Exception-driven choice, `try`/`catch` validation, arbitrary runtime type discovery, inheritance-dependent behavior, and
callback completion are forbidden as Admission authority. Ordinary Java or Kotlin type-test or pattern syntax may still
be accepted when the frontend resolves it to an Input-owned Closed Tagged Choice case or another exact closed relation
rather than to open host subtype discovery.

User logging, metrics emission, tracing calls, event publication, and similar externally observable instrumentation are
effects and do not participate in V1 Admission source meaning. Kontrakt-generated profiling, tracing, or diagnostics
instrumentation may exist separately when it cannot change Admission-visible meaning.

---

## 9. Deterministic Refinement

Before Admission authority is established, frontend and HIR formation must resolve and erase host source mechanics that
do not belong to Admission meaning.

The conceptual path is:

```text
resolve the exact Admission declaration selected by the applicable IDL / Contract World relation
-> identify the one eligible root judgment
-> request approved Input HIR observations needed to resolve every Input-facing source read through Kontrakt-controlled mediation
-> erase exact provider identity from Admission Definition meaning unless a separate Contract law makes that identity Admission-visible
-> retain the complete Admission-owned Input-observation requirements needed by the judgment
-> close every semantically reachable helper, implicit access path, initializer contribution, and local computation that contributes to the judgment
-> resolve property access, operator syntax, equality, indexing, casts or type tests, destructuring, helpers, and exact host callables to their actual supported semantic operations
-> acquire closed Definition material without executing user initialization or application behavior
-> resolve every accepted exact platform callable through supported compiler / ADR-0073 operation knowledge
-> determine type, presence, numeric, relation, ordering, collection, alias/escape, effect, exceptional-path, guarded-domain, completion, pattern, and work-shape legality
-> erase class, object, getter, lambda, iterator, Stream, Sequence, regex-engine, exception-control, and ordinary library-call mechanics when they are not Contract-visible
-> retain only complete backend-independent Admission meaning and separate source provenance needed by legal consumers
-> hand complete pre-authority Admission meaning through the common HIR boundary
-> keep each exact IDL selection in its own ADR-0071 Binding Candidate
-> Admission Definition judgment establishes an Established Admission Definition or Admission Definition Refusal without importing exact Input Definition identity
-> keep exact Input-to-Admission compatibility in the ADR-0048 inbound-airlock composition relation
-> that composition judgment connects exact established definitions when the declared compatibility law is satisfied
-> successful Established Admission Definition may enter optimized execution formation
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

At occurrence time, Admission requires only the exact Established Input presentation observation legally exposed by
Input and the exact closed material owned by the Established Admission Definition. Kontrakt-controlled mediation
supplies
one coherent approved Input observation under the producer-owned Established Input Protocol. The requester does not
reconcile generations, retained state, or producer implementation details for itself. The user-facing Java or Kotlin
parameter is an authoring and generated-API surface; runtime correctness does not depend on reopening that host carrier
as
Contract authority.

Runtime symbol lookup, reflection, property discovery, hidden virtual dispatch, callback construction, dynamic
semantic-operation selection, arbitrary host regex-engine delegation, exception-driven result selection, and
failure-policy selection are forbidden.

Java or Kotlin source evaluation strategy is not Admission authority by itself. Source short-circuit structure,
collection iterator shape, Stream pipeline machinery, temporary allocation, local variable storage, and matching
algorithm may be replaced whenever those distinctions are not Contract-visible. An exact Input-visible order, guarded
operation domain, operation-result distinction, floating result law, tie law, or other explicitly owned observation must
still be preserved.

An operation whose host specification intentionally permits several results, encounter choices, reduction orders, or
other meaning-distinct outcomes is not made deterministic by compiler preference alone. V1 may accept such a source
operation only when the chosen Admission semantic operation is exact, when all platform-permitted outcomes are
indistinguishable to the final Admission judgment under the supported analysis, or when another explicit preservation
law makes the narrowing legal. Unspecified host behavior, `findAny`-style choice, hash order, or
implementation-dependent reduction order cannot silently become Admission meaning.

A backend may fuse branches and pipelines, eliminate temporaries, scalarize local state, use primitive or columnar
instructions, specialize operations, construct indexes, reorder computations when their exact result law permits it,
vectorize, compile patterns into another matcher, or return allocation-free outcome codes only when every
Contract-visible Admission result, guarded legality relation, and explicitly owned attribution remain identical.
Side-effect freedom by itself does not authorize reassociation or parallel reduction.

Execution resource accounting may depend on the actual finite Input extent and the chosen legal realization. Semantic
legality, compiler work-shape knowledge, and the resource allowance imposed by Budget, Capacity, compiler-safety, or
realization policy are separate concerns. Finite does not mean cheap. Sorting, grouping, large-number arithmetic,
normalization, pattern matching, and nested traversal may be semantically legal while remaining resource-amplifying.
Their resource limits do not become hidden Admission determinants and their stopping results do not become `Rejected`.

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

Deterministic result equality does not collapse occurrence identity. A fresh `Presented` Input Occurrence that reaches
Admission remains a fresh Admission application even when its complete presentation meaning and final Admission result
match an earlier application. A future incremental or cached realization may reuse validated non-authoritative
computation or physical representation, but it must not substitute an older Admission Occurrence for that fresh
application. Conversely, repeated physical evaluation of one already-identified exact application does not create
additional Admission Occurrences.

---

## 11. Result Law

The occurrence-time logical V1 result is exactly `Admitted` or `Rejected`. A source Boolean `true` maps to `Admitted`
and `false` maps to `Rejected` only after the complete source computation has been resolved into legal Admission
meaning.
`Admission Definition Refusal` is the separate Definition-time result defined by Section 6.5 and is not a third
occurrence
outcome.

`Admitted` means only that the exact presented Input satisfies the continuation condition owned by the selected
Admission Contract. It does not mean that the whole Interaction is globally authorized, that another Contract cannot
stop processing, or that downstream Contract judgments are guaranteed to succeed.

`Rejected` means only that a legally entered Admission judgment completed and determined that its own continuation
condition was not satisfied. It does not rewrite the determining Input Occurrence as `Refused`, erase that Input
Occurrence, or imply that the Input presentation never existed.

A legally entered Admission semantic application does not acquire Established Admission Occurrence Material merely
because evaluation started. The occurrence result becomes Established only when the owning Admission judgment completes
and establishes exactly `Admitted` or `Rejected`. If trustworthy completion is lost before that point, the machinery
must not infer `Rejected` from the absence of `Admitted` or otherwise fabricate an Admission result.

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

If source cannot be resolved into a complete valid Admission Definition Candidate, the Admission Definition judgment
does
not enter. The exact compiler-owned unsuccessful-result and recovery representation remains a common compiler concern
rather than an Admission result. If a complete Candidate legally enters Definition Establishment and violates the
Admission Definition law, Admission establishes `Admission Definition Refusal`. If a legally entered Admission
occurrence
evaluates its established continuation condition as unsatisfied, Admission establishes `Rejected`.

Admission rejection stops that presented material from continuing through Admission. Rejected material does not continue
under another name. The source Input Occurrence remains the already-established Input result that determined the
Admission application. Admission rejection does not retroactively delete it.

The established Admission Occurrence result may be observed through the legal Established semantic boundary while its
physical backing remains available. Physical reclamation does not undo earlier Establishment, and physical retention
does
not by itself preserve current validity or extend Admission authority. Any retained explanation belongs to Diagnostic
Evidence and Retention law and must not make a discarded host execution strategy, full presentation payload, or audit
record part of Admission meaning merely because it is retained.

---

## 12. Relationship to Neighboring Contracts

Input establishes judgeable presentation meaning and, for a `Presented` application, makes the exact complete
presentation legally observable through its producer-owned internal semantic protocol. The Input Occurrence and its
result are established before Admission begins. A `Refused` Input Occurrence does not reach Admission.

Admission consumes that Input-owned meaning internally and judges whether the presentation satisfies the Admission
continuation condition. `Admitted` permits that exact presented material to continue to the next applicable inbound
judgment; `Rejected` stops continuation without undoing the determining Input Occurrence. The user still writes ordinary
Input-facing Java or Kotlin; the internal Input protocol is not an additional user API.

Admission does not replace, re-establish, or publish a transformed Input presentation and does not create a canonical
representative. Temporary parsing, decoding, normalization, filtering, mapping, sorting, grouping, aggregation, or other
judgment-local computation does not change that law. A downstream Contract or realization consumer must not reuse an
Admission-local derived interpretation as if Admission had established it. This prevents the Admission check and later
use from silently applying different parsers, normalizers, equality laws, or other interpretations to the same presented
material.

If Canonicalization is selected, only admitted material reaches it.

If Canonicalization is omitted, the same Input-established presentation meaning proceeds toward Lowering without
Admission establishing a replacement value.

Admission does not perform Lowering and does not establish core Fact authority.

Policy, Governance, Budget, Capacity, State, Version, Failure, Diagnostic, and other independently applicable Contracts
retain their own authority and results. Their material does not become an implicit Admission operand merely because
whole-machine execution composes several judgments around the same Interaction.

---

## 13. Closure and Deferred Work

Current V1 Admission-specific semantic law is closed under the common ADR-0053, ADR-0063, ADR-0071, and ADR-0075
boundaries together with the shared inbound-airlock composition owned by ADR-0048. The remaining work in this section is
frontend coverage, common compiler architecture, verification, resource enforcement, or physical Design. Those items do
not remain Admission semantic OPENs merely because their mechanisms are not yet selected.

### 13.1. Closed Admission-Specific Decisions

Sections 6.3 through 6.8 close the Admission Definition Candidate meaning, producer-owned semantic equality, typed HIR
references and projections, Definition Establishment judgment, Definition identity and Version specialization,
occurrence-time Basis Requirement Law, Basis/Applicability boundary, and Established Admission Semantic Protocol.

One Admission Authority owns one independently addressable Admission Definition per Contract Version in the current flat
model. `AdmissionDefinitionCandidateRef` therefore uses the exact resolved Admission Authority coordinate and exact
Resolved Version Candidate Coordinate, while `EstablishedAdmissionDefinitionRef` uses the exact Owning Admission
Authority Reference and exact Version Binding. No additional Authority-Local Definition Coordinate is required in V1.

Admission owns one occurrence Required Basis requirement, `presentedInput`, whose required meaning is exactly one
already-Established `Presented` Input Occurrence. ADR-0048 owns the shared inbound-airlock composition that supplies the
legal upstream relation used by Basis Resolution. Admission requires no additional Applicable Context in V1. Exactly one
Applicable Basis completes the occurrence prerequisite; incomplete or invalid prerequisite formation prevents legal
entry rather than fabricating `Rejected` or Failure.

The Established Protocol contains typed Definition and Occurrence references, complete Definition and Occurrence
projections, direct occurrence-to-Definition and occurrence-to-determining-Input relation observations, and only two
fine-grained Established projections: the Established Admission Input Observation Requirement Projection and the
Admission Occurrence Outcome Projection. Consumer-specific Contract projections and one-projection-per-field or
one-projection-per-query proliferation are not part of the V1 law.

Input and Admission Definition authority remain independent. Composition does not import exact Input identity into
Admission Definition meaning, and compiler reuse does not reuse old Contract composition, Definition, or Occurrence
authority for different current subjects.

### 13.2. Frontend and Capability Work That Does Not Block Semantic Closure

The exact public Java or Kotlin Admission declaration shape may change as long as the user-facing model remains ordinary
host-language authoring selected through the normal Kontrakt IDL surface. V1 does not require users to learn a separate
Admission expression language, internal semantic-reference model, or compiler protocol.

The exact supported Java and Kotlin API catalog is frontend/compiler and ADR-0073 coverage rather than Admission
Contract authority. V1 should cover common value-oriented standard-library operations broadly, but each exact operation
or overload still requires complete supported semantic knowledge and legal use in the Admission role. Runtime
executability alone is insufficient.

The exact V1 capability matrix remains frontend/compiler work. It must evaluate source operations against the Input
presentation family and observable distinctions they consume, exact resolved callable or language operation,
class/object initialization and other implicit execution, aliasing and escape, partial or exceptional completion,
guarded legal domains, external effects or ambient basis, relation or order introduced by temporary computation, host
underspecification, invocation semantics for lambdas or binders, temporary-result escape, work behavior, and whether
Kontrakt can erase or lawfully preserve the host operation without importing host implementation authority.

Frontend expansion to additional language constructs, library operations, collection idioms, local mutation patterns,
finite loop shapes, recursive forms, or equivalent source forms may occur without changing Admission meaning when the
new form resolves to existing Admission semantics and still satisfies the same closure laws. Compiler inability to
analyze a source form is a compiler-side unsupported result, not evidence that the syntax itself is permanently outside
Admission. Expanding Admission's semantic inputs, outputs, authority, or result vocabulary requires a separate Contract
decision.

### 13.3. Owned Elsewhere or Derived

ADR-0064 owns the Input semantic observations and their projection meaning, completeness, and equality. Admission states
what Input observations it requires; it does not redefine Input meaning for composition, diagnostics, PBT, verification,
or another consumer.

ADR-0071 owns the common HIR Binding Candidate law, HIR visibility and seal, typed Protocol behavior, and the rule that
fine-grained projections are producer-owned observations rather than new authority. ADR-0063 and ADR-0053 own common
Established identity, Version Binding, Required Basis, Basis Binding, Applicability, authoritative Definition reference,
and semantic prerequisite laws. ADR-0048 owns the shared inbound-airlock composition and direct Input-to-Admission
adjacency. ADR-0065 consumes that composition relation where Admission occurrence Basis Resolution requires the exact
legal upstream Input source; it does not create a second composition owner.

ADR-0075 owns cross-responsibility mediation, current-validity qualification, reuse, retention, and change containment.
A requester states the information or guarantee it needs; Kontrakt-controlled mediation may resolve, obtain, qualify,
compare, retain, or deliver the applicable producer-owned observation. Cache state, dependency edges, fingerprints,
HIDs, retained material, or compatibility work do not establish Admission meaning or a Contract composition relation.
ADR-0075 does not require one physical manager or subsystem for those responsibilities.

The exact compiler-owned unsuccessful-result and recovery representation remains a common compiler concern. A missing,
unsupported, stale, corrupt, or unavailable compiler observation must not be converted into `Admission Definition
Refusal`, `Rejected`, or Contract Failure merely to keep processing moving.

Whole-Machine summaries, transitive reachability, conflict analyses, optimization judgments, verifier proofs, rankings,
cost models, and predictions are derived compiler products or separately owned higher-scope meaning. They are not
silently added to the Admission Established Protocol.

### 13.4. Moved to Design or Narrower Compiler Architecture

The physical representation of the Input-observation requirement, projection materialization strategy, table or slab
layout, handle encoding, protocol dispatch, query mapping, dependency recording, cache layout, fingerprint or HID use,
persistence, retention, comparison fast path, incremental repair algorithm, and parallel collection strategy remain
replaceable compiler realization. Their implementation must preserve the logical Protocol surfaces and deterministic
clean-path result defined by this ADR, ADR-0071, and ADR-0075.

The exact physical placement of inbound-airlock composition formation, Basis relation encoding, Applicability evidence,
and Protocol mediation is likewise not selected here. Logical ownership does not require a dedicated
`CompositionManager`, `AdmissionManager`, store, query engine, or object graph. Existing compiler responsibilities may
be
physically fused when doing so preserves authority, coherence, failure containment, determinism, and the legal Protocol
surface.

The exact execution-cost accounting, runtime resource enforcement, verifier strategy, and optimized matcher or
collection realization remain Design, Budget, Capacity, Verification, or backend work as applicable. No single theorem
prover, verifier algorithm, runtime meter, SSA form, effect system, or loop-bound analysis is required by this ADR.
Those
mechanisms must preserve Admission meaning without turning resource exhaustion or implementation availability into a
third occurrence result.

Constant-time execution, confidentiality, and side-channel resistance are not implied by Admission determinism. If such
properties become required, they need an explicitly owning security or realization law rather than being inferred from
`Admitted` / `Rejected` semantics.

---

## 14. Consequences

The Admission occurrence judgment becomes a real two-result Contract judgment rather than a Boolean callback,
exception-driven validation hook, or general policy engine. Definition-time Establishment remains a separate judgment
with the success/refusal boundary defined by Section 6.5.

Users may write ordinary supported Java or Kotlin expressions and familiar standard-library code through the normal IDL,
generated interface or artifact, and 1D declaration surface. They do not construct Kontrakt IR, Established references,
semantic projections, operation-profile handles, proof objects, or backend execution structures.

The compiler carries the complexity of determining whether the complete source meaning is legal for Admission and of
translating that meaning into internal HIR, Establishment, Established Material, and execution products. Broader
frontend coverage therefore increases compiler responsibility rather than user-visible Contract syntax.

Temporary source collections, local accumulators, Stream or Sequence pipelines, parsed values, normalized values, regex
objects, helper calls, and similar host mechanics need not survive execution formation.

Unsupported convenience code is rejected rather than becoming hidden runtime authority. `try`/`catch`, exception-driven
validation, ambient-state operations, external capabilities, asynchronous completion, logging or metrics side effects,
and uncontrolled execution sources do not become escape hatches. Source syntax, `val`/`final`, familiar standard-library
membership, or a Boolean result is never sufficient evidence of legality by itself.

The generated evaluator can be specialized aggressively because its semantic surface is closed before execution
formation. Host collection algorithms, iterator protocols, temporary allocation patterns, local variable storage, source
control-flow shape, and host regex-engine behavior are replaceable frontend or realization mechanics when they are not
Contract-visible. Optimization freedom stops at exact operation semantics, guarded legal domains, Input-visible order,
derived tie or relation law, and any other distinction that can change the Admission result.

Finite actual Input extent and machine resource policy are separate concerns. Admission can legally process a finite
occurrence whose size varies by invocation without owning an arbitrary global maximum, while Budget, Capacity,
compiler-safety, and backend mechanisms retain responsibility for their own limits and stopping conditions.

Input and Admission Definition authority remain independent. Admission Definition reuse is not tied to one exact Input
Definition merely because a particular composition selects the two together. When either exact selected Definition
changes, the owning composition law must establish the relation for the current exact subjects; an older authoritative
composition relation is not inherited merely because retained compatibility work still exists.

Compiler work used to determine compatibility may nevertheless be reused through ADR-0075 when the current Admission
Input Observation Requirement Projection and the relevant producer-owned Input observations are current-valid and
semantically equivalent for that request. Propagation may then stop at the relevant protocol or compatibility-product
boundary. This optimization does not reuse old Contract relation identity and does not make the incremental dependency
graph Contract authority.

Input and Admission occurrence authority remain separate. A `Presented` Input Occurrence survives an Admission
`Rejected` result as the exact upstream occurrence that determined that judgment, while rejected material does not reach
later inbound Contracts. Diagnostics, verification, reference judgment, PBT, tracing, or future incremental machinery
may
observe producer-defined semantic projections without forcing occurrence material into the Definition World or retaining
the full presentation indefinitely. Future incremental execution may reuse validated work, but fresh semantic
applications still receive fresh occurrence identity.

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

The same review was extended after cross-domain audit of compiler, operating-system verifier, database,
protocol-security, hermetic-build, capability, deterministic-execution, and data-processing designs. It added coherent
one-presentation observation, hermetic acquisition of Definition material without executing user initialization, exact
resolution of implicit source operations, Closed Tagged Choice observation distinct from arbitrary runtime subtype
discovery, freshness/alias/escape conditions for Admission-local mutation, guarded partial-operation preservation,
semantically reachable-path closure, exact relations for derived equality/order/grouping/ties/duplicates,
operation-specific reduction determinism, protection against host-underspecified outcome choices, explicit non-escape of
parsed or normalized interpretations to downstream authority, and a sharper separation between semantic legality,
work-shape analysis, and Budget/Capacity or realization resource enforcement.

A later 2026-09-22 occurrence review aligned Admission with ADR-0063 occurrence establishment, ADR-0064 Input occurrence
timing, and the 1D verification checklist. It made the Input Occurrence an independently established upstream result,
required `Presented` before Admission may enter, and prohibited Admission from creating, cancelling, or rewriting Input
occurrence authority. It gave Admission independent occurrence meaning, separated semantic application entry from
publication of Established Admission Occurrence Material, required trustworthy `Admitted` or `Rejected` completion
before
that material exists, and kept non-entry, trust loss, resource stopping, and implementation failure outside the
Admission
result vocabulary. It also separated fresh occurrence identity from physical re-evaluation, kept rejected occurrence
meaning outside the default Definition World, separated retention from Establishment, and left future incremental or
cached realizations free to reuse validated work without reusing fresh occurrence authority.

A subsequent 2026-09-22 binding review removed the stronger candidate model in which Admission Definition meaning or
Definition Establishment directly depended on one exact Input Definition. Admission Definition now remains complete and
independent by owning the Input-observation requirements needed by its judgment. Input independently owns the semantic
surface it provides. The owning IDL / Contract World binding or composition relation connects the exact selected Input
and Admission Definitions and determines compatibility without supplying missing Admission meaning. The review also
forbids accidental structural duck typing, keeps exact provider identity out of Admission Definition meaning unless a
separate Contract law explicitly requires it, preserves the exact Input Occurrence relation at occurrence time, and
keeps V2 incremental dependency tracking subordinate to the binding and protocol boundaries rather than Contract
authority.

A 2026-09-25 HIR–Establishment and compiler-product review aligned Admission with ADR-0071, ADR-0063, the 1D master
checklist, and ADR-0075. It made Admission Definition Meaning equality explicitly producer-owned, introduced the
Admission Input Observation Requirement Projection as the narrow HIR observation boundary for consumers that do not need
the full judgment body, and kept projection granularity independent from source reads and physical query or storage
granularity. It separated each IDL Binding Candidate from the later Input-to-Admission composition relation, placed
cross-responsibility observation, qualification, reuse, and coherence behind Kontrakt-controlled mediation, and kept
current-validity machinery outside Contract authority. The same review defined the Admission Definition judgment,
separated `Admission Definition Refusal` from compiler-side non-entry and occurrence-time `Rejected`, and clarified that
reuse may preserve compiler work but cannot transfer old Definition, Occurrence, or composition authority to current
subjects.

A subsequent 2026-09-25 Admission closure review specialized Definition identity and Version references, confirmed that
the current flat Admission Authority requires no Authority-Local Definition Coordinate, and closed occurrence-time Basis
and Applicability. It defined one exactly-one `presentedInput` Required Basis, used the ADR-0048 inbound-airlock
composition as the exact legal source relation for Basis Resolution, required no additional V1 Applicable Context, and
made incomplete prerequisites prevent occurrence entry rather than fabricate a Contract result. The review also closed
the Established Admission Semantic Protocol with typed Definition and Occurrence references, complete Definition and
Occurrence projections, direct source-owned occurrence relations, and only the independently useful Requirement and
Outcome fine-grained projections. Remaining public syntax, API coverage, verifier, resource, reuse mechanism, storage,
and subsystem-placement questions were reclassified as frontend, common compiler architecture, or Design rather than
Admission semantic OPENs.

ADR-0048 remains the owner of the shared inbound-airlock composition and direct Input-to-Admission adjacency.