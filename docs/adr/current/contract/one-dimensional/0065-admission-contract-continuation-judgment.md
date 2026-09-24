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

Admission is the continuation judgment over the immutable presentation established by Input. Input completes its own
occurrence judgment first. An Input application therefore establishes either `Presented` or `Refused` under ADR-0064
before Admission can begin. Admission neither creates that result nor changes it afterward. Only an exact `Presented`
Input Occurrence can determine an Admission semantic application.

At the user surface, Admission answers one question:

```text
May this already-formed Input presentation continue past Admission?
```

The user writes that judgment as an ordinary Java or Kotlin 1D declaration and selects it through the normal Kontrakt
IDL surface. Compiler-internal semantic references, protocol observations, and execution representations remain internal
to Kontrakt.

Input remains the authority for the presentation that Admission observes. Admission consumes the Input-owned semantic
presentation; it does not reopen the original JVM carrier or reconstruct Input meaning from host structure. The internal
Input protocol exists to preserve this ownership boundary and is not a second user API.

Admission judges the same complete presentation that Input established. Input decides how that presentation's internal
structure can be observed and how `Presentation Sameness` is interpreted. Admission may use those distinctions, but it
does not redefine them. Section 7 gives the collection and closed-choice cases that matter to source refinement.

There is no user transformation stage between Input and Admission. Admission may still compute temporary values while
deciding its result. Such computation is judgment-local: it does not replace the Input presentation and does not
establish Canonicalization, Lowering, Fact, or other downstream Contract material.

The host program is therefore authoring evidence rather than authority. Kontrakt may erase or replace the source method,
helper structure, collection pipeline, regular-expression engine, or other host execution mechanics once their complete
Admission-visible meaning has been resolved.

---

## 2. Problem

A Boolean callback is too weak to act as Contract authority. A callback can return a Boolean while obtaining its answer
from hidden runtime state, exception control flow, unresolved dispatch, or implementation-specific library behavior. The
return type alone says nothing about whether the judgment is closed or deterministic.

The opposite design is also undesirable. Requiring users to construct Kontrakt IR or learn a second judgment language
would duplicate work already performed by the compiler and would make ordinary Java or Kotlin authoring artificial.

Admission therefore needs a host-language source surface whose complete relevant meaning can be resolved before
authority is granted. Accepted source must become a closed, terminating, deterministic Admission judgment over legal
Input observations and closed Admission Definition material. When the frontend cannot establish that meaning, it rejects
or reports unsupported source through the compiler boundary rather than allowing runtime behavior to supply the missing
semantics.

Frontend coverage may grow without changing the Contract. Supporting another source spelling or library operation
expands what the compiler can understand; it does not expand what Admission is allowed to observe or establish.

The user-facing language and the compiler-internal protocol are separate concerns. Kontrakt may need rich internal
material from HIR through execution formation. None of that internal representation becomes an ordinary Admission
authoring obligation.

---

## 3. Decision Drivers

Admission is a judgment, not a transformation. It may derive temporary values to reach that judgment, but only the
Admission result receives Admission authority.

The occurrence-time result vocabulary contains exactly two values: `Admitted` and `Rejected`. Section 11 defines their
meaning. Definition-time `Admission Definition Refusal`, compiler-owned unsuccessful results, and stopping results owned
by other Contracts are separate and must not be folded into that pair.

Admission authority comes from explicit role selection and the Admission-owned Establishment path. A class name, method
shape, runtime type, or inheritance relation cannot create the role. One selected declaration denotes one flat Admission
Contract; helper decomposition does not create child Contracts.

At occurrence time, Admission sees one coherent presentation from one exact `Presented` Input Occurrence. It does not
refresh that presentation from the original carrier, and internal Input references used for attribution do not become
user-visible branch operands.

Definition-local material may contribute to the judgment only when the frontend can establish its complete meaning
without executing application initialization or consulting an undeclared capability. The same closure rule applies to
helper calls and platform operations.

Accepted source may be expressive, but every semantically reachable path must remain closed, terminating, deterministic,
and free of undeclared Contract-visible effects. Section 7 defines the supported source law; Section 8 owns the
hidden-observation prohibition.

A finite Input presentation does not imply one fixed semantic work budget. Admission may process the actual finite
extent of an occurrence when the operation law permits it. Budget, Capacity, and compiler or backend resource limits
retain their own authority and cannot be translated into `Rejected`.

Exception control is not Admission judgment meaning in V1. A JVM exception may indicate a realization defect or another
separately owned stopping condition, but it cannot silently become an Admission result.

Host syntax must resolve to exact semantics before it is trusted. For example, an ordinary type test is legal only when
the frontend can resolve it to a closed Input-owned choice relation; open runtime subtype discovery is not equivalent.

The backend may replace host execution mechanics aggressively. It may do so only when every Admission-visible result and
attribution remains the same under the exact semantic law.

---

## 4. Authority Path

Admission follows the common HIR and Establishment architecture at Definition time:

```text
ordinary Java or Kotlin Admission declaration
-> exact Admission role selection
-> frontend resolution
-> complete implementation-erased Admission Definition Candidate
-> Resolved HIR seal and visible handoff
-> Admission Definition judgment
-> Established Admission Definition or Admission Definition Refusal
-> execution formation for an Established Definition
```

An unresolved or invalid frontend result does not enter the Admission Definition judgment. Section 6.5 defines that
boundary.

An Admission Definition is complete without embedding one exact Input Definition. It states the Input observations its
judgment requires. After Input and Admission Definitions have been established independently, ADR-0048 owns the
inbound-airlock composition that decides whether the exact selected Input surface satisfies those requirements. The HIR
Binding Candidate remains only the ADR-0071 selection relation; it does not also become the later compatibility
relation.

Cross-responsibility compiler work may obtain producer-owned observations through ADR-0075 mediation. That mediation may
obtain and qualify compiler material and may reuse prior work when legal. It cannot create Admission meaning or
establish the ADR-0048 composition relation.

Occurrence-time authority follows a different path:

```text
exact Established Admission Definition
+ fresh Admission application
+ exactly one legal Presented Input Occurrence basis
-> Complete Basis
-> Admission judgment
-> Admitted or Rejected
-> Established Admission Occurrence
```

Section 6.7 defines the Required Basis and the role of the ADR-0048 composition relation. Section 6.8 defines the
Established protocol through which the result can later be observed.

Starting physical evaluation is not enough to establish an Admission Occurrence. If the judgment cannot complete with
one trustworthy result, no occurrence result is fabricated.

---

## 5. Selection and Declaration Law

### 5.1. Explicit Admission Selection

The applicable IDL or Contract World arrangement selects one exact declaration for one exact Admission role occurrence.

```text
exact selecting context
    Interaction A / Admission
        -> XGreaterThanOne
```

The source-layout heading does not create semantic hierarchy or composition. Role authority comes from the resolved
selection relation itself. The selected name may use ordinary source imports, but resolution must end at one exact
declaration.

Host organization is not a substitute for selection. File placement, annotations, parameter types, inheritance, or
runtime discovery cannot grant the Admission role.

### 5.2. One Flat Admission Contract

One selectable declaration names one flat Admission Contract. Private constants, local values, and helper expressions
may contribute to its root judgment when the frontend closes their meaning, but they do not become independently
selectable Contracts.

Several Admission declarations may coexist in one source file. Conversely, several legal selection occurrences may
deliberately select the same Admission declaration. In that case the Admission Definition may be shared, while selection
attribution and later occurrence relations remain exact to each use.

Inheritance, overrides, marker interfaces, and virtual specialization do not create Admission meaning. Reuse of
Admission meaning occurs by selecting the same flat declaration, not by deriving another Contract through a host type
hierarchy.

---

## 6. Input Observation and Composition Boundary

Admission judges the presentation already established by Input for the applicable Interaction. Its Definition is
independent of the exact Input Definition that will later supply an occurrence. Instead, the Admission Definition states
the complete Input observations required by its judgment.

Input owns the semantic surface it provides. ADR-0048 composition connects the exact established Input and Admission
Definitions and determines whether that Input surface satisfies the Admission requirement. The composition relation
cannot fill in missing Admission meaning or import Input identity into the Admission Definition.

Compatibility is semantic rather than structural. Similar host fields or compatible JVM types do not establish the
relation. The match must follow the explicit Contract-owned law of the inbound-airlock composition.

During frontend formation, Admission may need to understand ordinary reads from the selected Input-facing source. It
states those needs as Admission-owned requirements and obtains legal Input HIR observations through Kontrakt-controlled
mediation. The exact Input Candidate used during that work is a formation dependency, not automatically an Admission
Definition determinant.

At occurrence time, Admission receives one coherent Input-owned Established Presentation observation for the exact
`Presented` Input Occurrence. It does not select an Input implementation or recover meaning from source declarations,
storage layout, or backend representation.

Admission can observe only distinctions that Input legally exposed. Nested constituents remain constituents of the Input
presentation; observing them does not turn them into new direct coordinates or independent authorities. Internal Input
references may support interpretation and attribution inside Kontrakt, but user Admission code does not branch on those
references as ordinary data.

No additional operand may be discovered from undeclared application or environment state. Section 8 owns this
prohibition. Results owned by neighboring Contracts remain outside the Admission operand set unless a separate law
explicitly makes one of them semantic input. Section 12 identifies those neighboring authorities.

Definition-local constants or values may participate only when their meaning is closed without executing application
initialization. A value reachable through a mutable configuration source or an effectful initializer does not become
Definition material merely because the host language can name it.

Admission may derive temporary values while deciding the judgment. Such values remain local to that judgment. If a later
Contract needs meaning that Admission derived only for its judgment, that later authority must establish or consume that
meaning under its own law. Admission-local computation is not already-authoritative downstream material.

### 6.1. Illustrative Source

A declaration may remain ordinary host code when the frontend can erase it completely into Admission meaning.

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

The object, method, local variable, operators, and host Boolean are source mechanics. Authority begins only after the
complete judgment meaning has been resolved and established.

### 6.2. Input Occurrence and Admission Occurrence

Input and Admission own separate occurrence meaning. A `Presented` Input Occurrence may determine a later Admission
application; a `Refused` Input Occurrence remains an Input result and creates no Admission application.

A fresh `Presented` Input Occurrence creates a fresh Admission semantic application even when its presentation is
semantically equal to an earlier one. By contrast, repeated physical evaluation of the same already-identified
application does not create another occurrence. Parallel or speculative computation may converge on the same occurrence
result without minting duplicate authority.

An Established Admission Occurrence exists only after the owning judgment establishes `Admitted` or `Rejected`. Its
meaning preserves the exact applied Admission Definition, the exact determining Input Occurrence, and the result.
Occurrence material remains separate from the default Definition World. It does not copy the whole Input presentation or
surrounding Contract World merely because a later compiler consumer may need attribution.

Occurrence authority and physical retention are independent. Reclaiming the backing does not undo the earlier
Establishment, and retaining bytes or diagnostic evidence does not make an old occurrence current again.

### 6.3. Definition Candidate Meaning and Semantic Equality

A complete Admission Definition Candidate contains only Admission-owned meaning. It contains the complete judgment law,
the complete Input Observation Requirement Law, and any closed Admission-local semantic material whose change can alter
that Definition.

Formation context remains outside that meaning unless the Admission law explicitly says otherwise. Compiler formation
may consult material that does not belong to Admission meaning. The selected Input Candidate is one example, and source
provenance is another. Host representation and compiler bookkeeping likewise remain outside Definition determinants
unless an Admission law explicitly says otherwise.

Admission owns semantic equality for its Definition Meaning. Two meanings are equal only when every Admission-owned
determinant is equal under its owning law. This semantic equality does not imply CandidateRef equality, Definition
identity, Version equality, or Occurrence identity. Compiler interning or cached reuse cannot create any of those
semantic relations.

### 6.4. Resolved Admission HIR Candidate Protocol

Admission specializes ADR-0071 with two typed HIR references and one narrow producer-owned projection in addition to the
complete projections:

```text
AdmissionDefinitionCandidateRef
AdmissionIDLBindingCandidateRef

Admission Definition Candidate Projection
Admission IDL Binding Candidate Projection
Admission Input Observation Requirement Projection
```

The Definition Candidate Projection is complete for the full Candidate meaning described in Section 6.3. The IDL Binding
Candidate Projection is complete only for its exact selection relation and does not carry Input-to-Admission
compatibility meaning.

The Admission Input Observation Requirement Projection exposes exactly the Input observations required by one Candidate.
It exists because composition and other legal consumers may need that requirement without depending on the entire
judgment body. Two Candidates may therefore have equal requirement projections while differing elsewhere in their
Definition Meaning. Such equality does not merge their references and is not by itself a reuse-validity decision.

Admission does not define a separate projection for every source read or operator, nor does it create consumer-specific
Contract projections. Any additional narrow projection would require an independently useful legal observation boundary.

Projection is a semantic protocol concept rather than a storage prescription. One backing structure may realize several
projections, and ADR-0075 may mediate their delivery and reuse. Query layout, dependency recording, persistence,
fingerprinting, and other physical mechanisms remain compiler architecture rather than Admission meaning.

### 6.5. Admission Definition Establishment Judgment

The Admission Definition judgment enters only after one complete Admission Definition Candidate is available as valid
Visible HIR. It consumes the complete Candidate meaning through the Admission HIR protocol together with the
authoritative Version prerequisite required by ADR-0053 and ADR-0063.

Neither the Admission nor the Input IDL Binding Candidate is an input to this judgment. An Input Definition Candidate or
Established Input Definition is not an input either. Those materials belong to selection, frontend formation, or later
composition rather than Admission Definition Establishment.

The entered Definition judgment has two Admission-owned outcomes:

```text
complete resolved Candidate that satisfies Admission Definition law
    -> Established Admission Definition

complete resolved Candidate that violates Admission Definition law
    -> Admission Definition Refusal
```

`Admission Definition Refusal` presupposes that a complete Candidate legally entered the judgment. If the frontend
cannot resolve required meaning, the HIR handoff is invalid, or recovery-tainted material cannot become valid Visible
HIR, the Definition judgment never enters. That condition remains compiler-owned rather than being rewritten as a
Contract refusal.

HIR seal verification does not perform the Admission judgment. Conversely, Establishment cannot reopen source or
compiler-private backing to recover Candidate meaning that the legal HIR observation did not provide.

Successful Establishment creates one complete authoritative Admission Definition; there is no partially visible
Admission Definition. Its identity and reference follow Section 6.6. Reuse or persistence may avoid compiler work, but
only the current legal judgment can grant current Admission Definition authority. The same rule applies to a retained
Definition Refusal: compiler reuse cannot transfer that Contract result to a different current Candidate.

### 6.6. Definition Identity, Version, and References

ADR-0053 owns Admission Authority continuity and Contract Version semantics. ADR-0063 owns the common Established
Definition identity and reference law. Admission only specializes those laws.

One Admission Authority owns one independently addressable Admission Definition per Contract Version in the current flat
model. No additional Authority-Local Definition Coordinate is therefore required.

```text
exact Admission Contract Authority
+
exact Contract Version
    -> exact versioned Admission Definition
```

Before Establishment, `AdmissionDefinitionCandidateRef` denotes the exact resolved Admission Authority together with the
exact authority-scoped Resolved Version Candidate Coordinate. The authored Version Claim must resolve to that coordinate
before the Candidate becomes valid Visible HIR. Source location and compiler representation identity cannot complete the
reference. The selected Input identity is also outside this reference.

After Establishment, `EstablishedAdmissionDefinitionRef` denotes the exact Owning Admission Authority together with the
exact Version Binding. CandidateRef and DefinitionRef remain different reference domains; success does not cast or
promote one into the other.

Operation, Interaction, exact Input Definition, and the ADR-0048 composition relation stay outside Admission Definition
identity. The same Admission Definition may therefore be selected by several legal operation arrangements or composed
with different exact Input Definitions without becoming a new Definition.

A Contract-visible change to Admission Definition meaning follows the normal Version law when the Authority continues. A
source-only or realization-only change does not create a new Definition when every legal Admission observation remains
unchanged. Equal Definition Meaning also does not merge distinct Contract Versions. If conflicting meaning is presented
for the same Authority and Version coordinate, ADR-0053 and ADR-0063 own the conflict law.

### 6.7. Occurrence Basis, Applicability, and Composition Boundary

Admission owns one occurrence-time Basis Requirement Law:

```text
requirement coordinate
    presentedInput

required meaning
    one Established Input Occurrence whose result is Presented

cardinality
    exactly one

permitted absence
    no
```

The reusable law belongs to Admission Definition meaning. One fresh Admission application owns the corresponding exact
Required Basis instance. Individual fields or collection constituents are observations of that one coherent Input
Occurrence, not separate Basis instances.

ADR-0048 owns the inbound-airlock composition and the direct Input-to-Admission adjacency. Occurrence-time Basis
Resolution uses that established relation to identify the legal upstream Input source. It does not search for a
compatible producer or infer one from host types, query reachability, or physical adjacency. If either exact Definition
changes, ADR-0048 must establish the composition relation for the current subjects; an older relation is not inherited.

For an exact Admission application, legal Basis Resolution binds `presentedInput` to the exact already-Established
`Presented` Input Occurrence supplied by the applicable composition. A `Refused` occurrence, or an occurrence from an
Input Definition that is not the legal upstream source, is not first bound and then repaired through `Inapplicable`; the
legal Basis Binding is not formed.

Basis Binding and Applicability remain distinct under ADR-0063, but Admission needs no additional Applicable Context in
V1. Once the exact legal Binding exists for the exact dependent application, it is Applicable. "No additional context is
required" is different from "required material is unavailable": an unavailable legal observation is a compiler-side
non-entry condition, not hidden Applicable Context. The absence of extra context also does not authorize ambient reads
of current Policy, State, Version, or compiler state.

Exactly one Applicable Basis makes the prerequisite complete. Zero leaves the judgment unentered; more than one violates
the exactly-one law. Admission defines no arbitration rule for this requirement. Neither case fabricates `Rejected` or
Contract Failure.

This ADR fixes the semantic relations, not their storage. The composition relation, Basis Binding, and Applicability
evidence may share physical representation as long as their exact legal meaning remains recoverable. Compiler dependency
or cache structures cannot substitute for those Contract-owned relations.

### 6.8. Established Admission Semantic Protocol

Admission exposes Established meaning through two typed reference domains:

```text
EstablishedAdmissionDefinitionRef
EstablishedAdmissionOccurrenceRef
```

Requirement meaning remains part of Definition meaning, and outcome meaning remains part of Occurrence meaning. V1
therefore needs no third Admission-specific authority reference.

The Established Admission Definition Projection is complete for one exact Definition. It exposes the DefinitionRef, its
Owning Admission Authority and Version Binding, and the complete local Definition Meaning. Definition-reference equality
and Definition-meaning equality remain different relations.

The Established Admission Occurrence Projection is complete for one exact Occurrence. It exposes the OccurrenceRef, the
applied Admission DefinitionRef, the exact determining Input Occurrence through Applicable Basis attribution, and the
`Admitted` or `Rejected` result. It does not recursively embed the Input presentation or the surrounding semantic world.

The direct relation projection exposes only the source-owned relations needed to interpret that occurrence:

```text
Admission Occurrence -> exact Admission Definition
Admission Occurrence -> exact determining Presented Input Occurrence
```

A compiler may build the reverse lookup for efficiency. Doing so creates derived compiler knowledge, not an inverse
Contract relation.

V1 defines two fine-grained Established projections. The Established Admission Input Observation Requirement Projection
exposes the requirement owned by one exact Definition. The Admission Occurrence Outcome Projection exposes only the
`Admitted` or `Rejected` result of one exact Occurrence. Their equality laws are producer-owned. Equal narrow
observations do not merge their enclosing Definitions or Occurrences and do not by themselves establish reuse validity.

Admission defines no field-specific or consumer-specific Contract projection. A consumer may select a legal
producer-defined observation but may not redefine its completeness, equality, or source authority. If a consumer
combines Admission observations into new compiler knowledge, that derived result belongs to the compiler producer or to
another explicit Contract authority that actually owns the new meaning.

Protocol availability does not alter semantic authority. Semantic absence is different from a projection that exists but
is not retained or not yet materialized. Unavailable, unsupported, stale, or corrupt backing is different again. None of
those states can revoke already Established Admission meaning or fabricate Definition Refusal, `Rejected`, or Failure.
ADR-0075 owns qualification, retention, persistence, mediation, and reuse across responsibility boundaries. Any valid
reused, restored, repaired, or parallel path must expose the same legal observation as clean formation.

Logical projections do not prescribe one object, query, or table each. Several observations may share compact backing as
long as typed references, completeness, coherence, and producer-owned equality remain recoverable.

---

## 7. Supported Judgment Source Law

Admission is defined by its continuation judgment, not by a fixed catalog of Java or Kotlin spellings. The frontend may
accept ordinary source whenever it can resolve the complete Admission-visible meaning from legal Input observations and
closed Admission Definition material.

A source form is legal because Kontrakt knows and preserves its semantics, not because the JVM happens to execute it
successfully. Frontend support may expand to new source forms without changing Admission authority, provided they
resolve to the same already-defined semantic surface.

### 7.1. V1 Judgment Coverage Target

V1 should cover ordinary value-oriented judgment code broadly enough that users do not need a second validation
language. Boolean logic is in scope when its operators have explicit semantics, including finite conditional choice and
the ordinary closed Boolean algebra. Numeric judgments are also in scope. Integral semantics must fix width and
signedness, and they must define overflow, narrowing, and shift behavior wherever those distinctions can affect the
judgment. The selected floating law must define classification and ordering as well as equality and arithmetic. It must
also preserve raw-bit relations when that law exposes them, so source purity alone does not permit a reassociation that
changes the result. Decimal operations likewise keep presentation-sensitive equality separate from numeric comparison
when the chosen semantic profile distinguishes them.

Admission may judge finite alternatives such as enums, presence or absence, nullability, closed product members, and
`Closed Tagged Choice` payloads when Input exposes those distinctions. Character, text, and binary values may be
inspected through exact finite relations; containment and slicing are representative examples. Range and positional
operations are legal only when the required position or order is itself observable.

Finite arrays and the Input `Sequence`, `Membership`, and `Association` forms are also within the intended V1 surface.
Direct observations such as size or keyed lookup are legal only when Input exposes the required relation. Quantification
and reduction may be accepted under the same rule; `all` and `count` are representative examples. Judgment-local
pipelines follow the same rule. An unordered `Membership` does not acquire encounter order merely because a host API
offers `first` or `last`; Admission may derive a temporary order only under its own exact supported ordering law.

Closed value APIs beyond primitive data may be supported under the same rule. This includes large-number or decimal
values, temporal values, closed identifiers or references, codecs, and pattern operations when Kontrakt owns the
relevant semantics. Java Stream, Kotlin collection, or locally derived Sequence syntax is acceptable only when Section
7.4 can erase the host pipeline as authority.

Local helpers and non-escaping accumulation may participate when their complete reachable effects are known. Exact
callable coverage and reusable platform knowledge remain frontend/compiler and ADR-0073 concerns, not new user-authored
Admission vocabulary.

### 7.2. Ordinary Expressions and Temporary Computation

The frontend may refine ordinary closed control flow when its complete meaning is known. Ordinary value expressions and
finite control flow may disappear after refinement. Literals and finite branches are representative source forms rather
than separate Contract meaning. Early exit is also legal when it remains inside the same closed control-flow meaning.
They do not create nested Contract outcomes.

Admission-local mutation is legal only for fresh judgment-local state. That state must not alias mutable Input or
Definition material, escape the judgment, or become shared. The compiler must also know how every relevant update
contributes to the final result. Host immutability markers such as `val` or `final` do not prove these properties.

A `for` traversal over an exact finite Input-derived domain may be accepted directly. A `while`-style loop may also be
accepted when the frontend can establish its finite progress and completion from closed material. General recursion or
open-ended state-dependent loops remain unsupported in V1 when that proof is unavailable; this is a frontend coverage
limit rather than a permanent semantic ban on their syntax.

Temporary derived computation is permitted when its operations have exact supported semantics and its result does not
escape the judgment. The rule covers scalar interpretation as well as judgment-local collection reshaping or
aggregation. If such computation introduces a relation that Input did not provide, that relation must become explicit
Admission meaning. A new equality or ordering rule is a representative case; host conventions cannot silently supply the
missing relation.

Temporary host objects need not survive realization. Kontrakt may fuse, scalarize, precompute, specialize, or eliminate
them when the Admission-visible result remains unchanged.

### 7.3. Known Operation Refinement

A helper or library call may participate only when Kontrakt can remove that call as a source of Admission authority or
preserve a separately ratified platform obligation under ADR-0073.

An exact-target helper is acceptable when all semantically reachable behavior that can affect the judgment can be
resolved into the root Admission meaning. The closure includes implicit behavior, not just the visible body. For
example, a property read may execute a getter and an operator may resolve to user code. The helper's name, call frame,
and JVM target do not become Admission meaning.

Standard-library operations may be supported broadly when Kontrakt has exact reusable knowledge for the selected
callable. The user does not select internal semantic profiles or HIR operation nodes; any platform semantic basis needed
by the operation is resolved internally under ADR-0073.

An overload that depends on undeclared ambient state is not accepted merely because a neighboring overload is closed.
Behavior whose exact callable meaning cannot be closed remains unsupported. This includes unresolved dispatch and
unprofiled callback or library behavior.

Purity is not inferred from annotations, finality, familiar naming, or Boolean return type. Legality follows the
resolved semantic operation and all reachable effects, as further constrained by Section 8.

### 7.4. Finite Collection, Pipeline, and Binder Condition

Admission may inspect collection constituents only through distinctions owned by the Established Input presentation.
Input also decides the collection's membership or key relation and whether order or cardinality is semantic. If the
Input law does not expose one of those distinctions, a host collection implementation cannot supply it implicitly.

A finite Input collection need not have one fixed maximum cardinality. A traversal is legal when the compiler can prove
that it ranges only over the actual finite presented domain through supported operations. Resource limits on that work
belong to Budget, Capacity, compiler safety, or backend policy rather than to Admission semantics.

A lambda or function object is not itself Admission material. Such syntax may appear inside a recognized finite binder
when its body is closed, does not escape, and has exact invocation semantics. This matters for mutation. A sequential
binder with a known invocation law may use a fresh local accumulator. An API that may reorder or duplicate callbacks
cannot make captured mutation semantic unless that invocation law is itself closed.

Java Stream or Kotlin Sequence syntax is accepted only when the source is locally derived from finite legal values and
every intermediate operation is recognized. The host pipeline itself must disappear as authority; a live iterator or
external lazy source cannot survive the refinement. Parallel or potentially open-ended pipelines remain outside the V1
profile unless the same closure guarantees can be established.

Order-sensitive operations require a semantic order. If Admission derives a temporary order, its ordering and tie
behavior must resolve every distinction that can affect the result. A host sort being stable does not manufacture a
deterministic first element when the comparator itself leaves Input-distinct values tied. Conversely, if the final
Admission result is invariant under every legal tied permutation, Admission need not invent an observable tie order.

Reduction legality is operation-specific. Side-effect freedom does not imply associativity; floating arithmetic is a
representative case. A backend may reassociate or parallelize a reduction only when the exact result law permits it.

Judgment-local pipelines need not be materialized as host collections. Equivalent scans, primitive or columnar access,
indexes, and vectorized implementations remain available to the backend when they preserve every Admission-visible
distinction.

### 7.5. Completion, Exceptional Control, and Termination

Every legally entered Admission occurrence judgment must complete with exactly one of `Admitted` or `Rejected` for every
legal presentation to which its Definition applies.

Only semantically reachable source paths contribute to the judgment. A compiler may discard a provably unreachable
branch even if that branch contains unsupported syntax. If the compiler cannot establish the unreachability, rejecting
the source is a conservative V1 coverage decision.

A partial operation is legal only when its domain is established on every path that reaches it or when its partiality is
represented by an explicit non-exceptional semantic result. A guard that establishes the domain of a later operation is
therefore semantically relevant even if the original source evaluation order is not. Optimizations may not move that
operation onto a path where the guard no longer protects it.

This ADR does not mandate one proof technology. The compiler may use any sound supported analysis that establishes the
required domain, effect, and completion facts.

Exceptions are not branches of the Admission result. Catching a host exception and translating it into `Rejected` or
`Admitted` is therefore illegal in V1. The same rule applies when realization fails for unrelated resource or VM
reasons: Section 11 keeps those outcomes outside Admission.

Finite processing may scale with the actual finite Input extent. A fixed Definition-time step count is not required. A
loop form whose progress depends on external progress, blocking, synchronization, or an unclosed recursive cycle remains
unsupported because the compiler cannot establish completion from Admission-owned semantics alone.

### 7.6. Hermetic Source Acquisition and Initialization

Kontrakt must not execute user Admission code to discover Admission meaning. The frontend may inspect source,
compiler-owned representations, and ratified platform knowledge, but application initialization is not a discovery
mechanism.

A closed literal or initializer may contribute Definition material when its complete meaning can be resolved without
running application behavior. Host declarations such as `static final` or Kotlin `val` are insufficient by themselves
because their initialization may still execute effects or refer to mutable state.

Only semantically reachable initialization or access behavior matters. The compiler may erase a provably unreachable
path, but it cannot execute that path merely to decide whether the path is harmless.

### 7.7. Pattern and Regex Source Condition

Pattern syntax is frontend evidence. Admission does not grant authority to a particular host regex engine or to its
matching strategy.

A supported pattern must be reducible to exact Admission-visible matching semantics together with a supported work law
over the finite pattern and presented text or bytes. If result semantics, capture behavior, Unicode basis, or required
work cannot be closed, the source is unsupported rather than delegated to arbitrary host behavior.

The backend remains free to use an automaton, a specialized matcher, a fused scan, or another equivalent implementation.
The algorithm is realization, not Contract meaning.

---

## 8. No Hidden Observation

Admission may depend only on semantic input declared by its own Definition and by legal Contract relations. Admission
cannot read meaning directly from the surrounding application or process. External resources, ambient runtime state, and
dynamic or asynchronous capabilities must stay outside the operand set unless another law first turns the required
information into semantic material. A repository or clock is therefore no more admissible as hidden judgment input than
reflection over a live application object.

The Input protocol is not a reflective escape hatch. Internal references and compiler coordinates may support exact
interpretation or attribution, but user Admission code cannot inspect compiler storage, generations, caches, or semantic
references as arbitrary data.

If Admission needs additional information, that information must first become explicit semantic material under an owning
law. The implementation cannot silently capture the current locale, Policy World, State, provider setting, or other
ambient value merely because it is reachable.

Section 7.4 is the only path by which Stream- or Sequence-like syntax can participate: the finite source and operation
semantics must be closed, and the live host pipeline must disappear as authority.

Open runtime subtype discovery and callback completion are likewise forbidden. Ordinary host syntax may still express a
type test or pattern when the frontend resolves it to an Input-owned `Closed Tagged Choice` or another exact closed
relation rather than to dynamic host identity.

User logging, metrics, tracing, or event publication are externally observable effects and do not participate in V1
Admission source meaning. Kontrakt may add its own observation instrumentation separately when that instrumentation
cannot alter Admission-visible semantics.

---

## 9. Deterministic Refinement

Frontend refinement has one purpose: turn ordinary source evidence into complete Admission meaning before authority
begins. The detailed legality rules are already defined in Sections 6 through 8 and are not repeated here.

Conceptually, the frontend first resolves the exact selected Admission declaration and its single root judgment. It then
forms the Admission-owned Input Observation Requirement from approved Input HIR observations, closes every semantically
reachable helper and operation, and resolves any platform semantics through ADR-0073. Host mechanics that do not survive
as Contract-visible meaning are erased, while provenance remains separate.

The resulting complete Candidate crosses the ADR-0071 HIR boundary. Admission Definition Establishment then applies
Section 6.5. The exact IDL selections remain Binding Candidate meaning, while Input-to-Admission compatibility is
established separately by ADR-0048 after both Definitions exist. Only an Established Admission Definition can proceed to
execution formation.

The user does not observe the internal references, projections, summaries, or physical handles used by this path.

A change to Admission-owned meaning follows the normal Version and identity laws. A change that affects only source
formatting, helper factoring, temporary allocation, analysis implementation, or backend layout does not create new
Admission meaning when all legal observations remain unchanged.

---

## 10. Deterministic Evaluation

At occurrence time, Admission consumes the exact Established Input presentation observation and the exact closed
material of the Established Admission Definition. ADR-0075 mediation may deliver the Input observation, but the
requester does not manage producer generations or retained backing directly.

The hidden-observation law of Section 8 continues to apply during execution. Runtime correctness cannot depend on
reopening the user-facing carrier, resolving symbols dynamically, or discovering semantics from execution state.

Host evaluation strategy is replaceable. Short-circuit syntax, iteration shape, temporary allocation, and matcher
implementation may disappear when they are not Contract-visible. Any Input-visible order, guarded domain, exact numeric
result law, or other distinction that can affect the Admission result must still be preserved.

A host operation with several specification-permitted semantic outcomes is not made deterministic by compiler
preference. V1 may accept it only when Admission selects one exact semantic operation, when all permitted outcomes are
indistinguishable to the final judgment, or when another explicit preservation law justifies the narrowing.

The backend may fuse or reorder work, eliminate temporary state, vectorize, or substitute a different matcher when the
exact semantic operation permits the transformation. Side-effect freedom alone is not enough to justify reassociation or
parallel reduction.

Resource accounting remains separate from semantic legality. An operation can be finite and semantically valid while
expensive. Budget, Capacity, compiler-safety, or backend limits retain the stopping result when such work exceeds their
own law.

Admission determinism is defined by its semantic determinants:

```text
same Established Admission Definition meaning
+ same Established Input Presentation meaning observed for the application
+ same additional Admission-owned determinant, if one is explicitly defined
= same Admission result and Admission-owned attribution
```

Compiler scheduling, cache state, storage layout, worker count, and unrelated Contract results do not add determinants
to this equation. The surrounding Contract World likewise contributes only through semantic material that an owning law
explicitly makes relevant.

Deterministic result equality does not collapse occurrence identity. A fresh Presented Input Occurrence still creates a
fresh Admission application, while repeated physical evaluation of the same exact application does not create another
Occurrence.

---

## 11. Result Law

The occurrence-time result is exactly `Admitted` or `Rejected`. A source Boolean maps to that result only after its
complete computation has been resolved into legal Admission meaning. Definition-time `Admission Definition Refusal`
remains the separate result defined in Section 6.5.

`Admitted` means that the exact presented Input satisfies this Admission Contract's continuation condition. It does not
authorize the whole Interaction or predict the result of later Contracts.

`Rejected` means that a legally entered Admission judgment completed and found its own continuation condition
unsatisfied. The determining Input Occurrence remains a valid upstream occurrence and is not rewritten as `Refused` or
erased.

An Admission Occurrence becomes Established only when the judgment completes with one trustworthy result. Starting
execution or failing to obtain `Admitted` is insufficient. The exceptional and resource conditions described in Section
7.5 are not a third Admission result and do not automatically become Failure Contract meaning.

Diagnostic evidence may explain an Admission result, but it does not promote discarded host execution mechanics into
Admission authority. Likewise, a stop owned by another Contract keeps that owner's result rather than being translated
into `Rejected`. Section 12 identifies the neighboring Contract authorities.

The three failure boundaries are therefore distinct. If a complete Candidate cannot be formed, the Definition judgment
does not enter and the compiler owns the unsuccessful result. If a complete Candidate enters Definition Establishment
but violates Admission Definition law, the result is `Admission Definition Refusal`. If a valid occurrence enters and
its continuation condition is false, the result is `Rejected`.

`Rejected` stops that presented material from continuing beyond Admission. It does not retroactively delete the Input
Occurrence that determined the application.

Physical retention is separate from this result law. Reclaiming occurrence backing does not undo Establishment, while
retaining logs or diagnostic material does not extend current validity or Admission authority.

---

## 12. Relationship to Neighboring Contracts

Input establishes the presentation before Admission runs. A `Presented` occurrence exposes the complete presentation
through Input's producer-owned protocol; a `Refused` occurrence never creates an Admission application.

Admission consumes that Input-owned meaning and decides only whether it may continue. Judgment-local parsing or
normalization does not create a replacement presentation that downstream Contracts may reuse as already established
meaning. A later consumer therefore cannot silently substitute a different parser, normalization rule, or equality law
for the interpretation used only inside Admission.

If Canonicalization is selected, only admitted material reaches it. If it is omitted, the Input-established presentation
proceeds toward Lowering unchanged by Admission. Admission itself neither performs Lowering nor establishes core Fact
authority.

Policy, Governance, Budget, Capacity, State, Version, Failure, and Diagnostic are independently applicable neighboring
Contracts. Each retains its own authority. Their presence around the same Interaction does not make their material an
implicit Admission operand.

---

## 13. Closure and Deferred Work

The Admission-specific semantic law is closed for V1 under ADR-0053, ADR-0063, ADR-0071, ADR-0075, and the ADR-0048
inbound-airlock composition. Remaining questions concern frontend coverage, common compiler architecture, verification,
resource enforcement, or physical realization; they do not reopen Admission meaning.

### 13.1. Closed Admission-Specific Decisions

Sections 6.3 through 6.8 are the normative closure for the path from Admission Candidate meaning to Established
Definition and Occurrence meaning. They also close the prerequisite and protocol boundaries needed by that path. Section
6.6 fixes the current identity law at one independently addressable Admission Definition per Authority and Version.
Section 6.7 fixes one exactly-one `presentedInput` Required Basis and no additional V1 Applicable Context. ADR-0048
remains the sole owner of the Input-to-Admission composition used by Basis Resolution.

Section 6.8 also closes the Established protocol. It defines complete Definition and Occurrence projections, the direct
occurrence relations, and the two narrow projections that have independent legal consumers. No consumer-specific
Contract projections are added.

Input and Admission Definition authority remain independent throughout. Neither composition nor compiler reuse imports
Input identity into Admission Definition meaning or transfers old Definition, Occurrence, or composition authority to a
different current subject.

### 13.2. Frontend and Capability Work That Does Not Block Semantic Closure

The exact Java or Kotlin declaration spelling may evolve while the user-facing model remains ordinary host-language
authoring selected through IDL. Likewise, the set of supported library operations and source constructs is
frontend/compiler coverage. Section 7 states the semantic conditions that any such expansion must continue to satisfy.

The V1 capability matrix therefore belongs to frontend/compiler work. For each source form it must account for implicit
execution and for whether temporary state can alias or escape. It must also close partial or exceptional behavior, any
guard that makes an operation legal, and any external effect. Binder-style operations require their invocation and
ordering law to be known, and derived temporary material must not escape with new authority. Failure to establish these
properties is compiler unsupported, not a new Admission result.

Future support for additional loop forms, recursive source, collection idioms, or language constructs does not change
Admission semantics when those forms resolve to the same closed judgment law. Expanding Admission's actual semantic
inputs, outputs, authority, or result vocabulary would require a separate Contract decision.

### 13.3. Owned Elsewhere or Derived

ADR-0064 owns Input observation meaning, completeness, and equality. ADR-0071 owns the common HIR Binding Candidate,
seal, visibility, and producer-owned projection rules. ADR-0053 and ADR-0063 own the common versioning, Established
identity, and semantic-prerequisite machinery. The common Basis and Applicability laws belong there rather than being
redefined by Admission. ADR-0048 owns inbound-airlock composition. This ADR specializes those laws only where Admission
needs its own meaning.

ADR-0075 owns the cross-responsibility lifecycle of compiler observations. That includes qualification and reuse without
turning compiler retention into Contract authority. When the current Admission requirement and relevant Input
observations are semantically equivalent, compiler compatibility work may be reused. Propagation may also stop at that
legal product boundary. The old Contract composition relation itself is not reused as current authority. Those
mechanisms may reuse compiler work, but cache state or retained compatibility evidence cannot establish Admission
meaning or a Contract composition relation.

Compiler-owned unsuccessful results remain outside Admission. Derived compiler or higher-scope knowledge stays outside
the Admission Established protocol. Whole-Machine or transitive analyses are one case; verifier or optimization results
are another.

### 13.4. Moved to Design or Narrower Compiler Architecture

Physical representation remains open. This ADR fixes logical ownership, not subsystem placement. Protocol observations
and the composition or Basis relations may share backing when their semantics remain recoverable. Dependency, reuse,
persistence, and incremental machinery may likewise be fused or separated by compiler Design. No dedicated Admission or
Composition manager is required.

Verification and execution mechanisms also remain replaceable. The compiler may choose different analyses, IR forms,
matchers, or resource-enforcement strategies as long as they preserve Admission semantics and keep resource or
implementation failure outside the two-result occurrence vocabulary.

Admission determinism does not by itself promise constant-time execution, confidentiality, or side-channel resistance.
Any such guarantee requires an explicit owning security or realization law.

---

## 14. Consequences

Admission is a Contract judgment rather than a Boolean callback or general validation hook. The user can still write
ordinary supported Java or Kotlin because the compiler, not the user, carries the burden of resolving that source into
closed HIR, Establishment input, and executable material.

This separation gives the backend broad optimization freedom. Temporary host structures and source execution mechanics
may disappear once their semantics are known, while the Contract-visible judgment law remains fixed.

The same separation keeps resource policy and reuse machinery from becoming hidden Admission authority. Variable finite
Input extent remains legal, and compatibility work may be reused under ADR-0075, but current Contract relations and
fresh occurrence identity still follow their own semantic laws.

Finally, rejection is local to Admission. It stops continuation but leaves the determining Input Occurrence intact for
exact attribution and permitted later observation.

---

## 15. Migration History

This section is historical only; the normative Admission law is stated in the sections above. It records how that law
reached its current form without creating a second source of semantics.

This ADR was mechanically extracted from the Admission-owned material of ADR-0048. The extraction did not change the
accepted Admission semantics, and ADR-0048 continues to own the shared inbound-airlock composition and direct
Input-to-Admission adjacency.

The 2026-09-22 review aligned Admission with the closed Input model. It replaced the earlier direct-coordinate-only
operand model with consumption of the complete Input-owned presentation and allowed nested observation without creating
nested Input authority. It also made temporary Admission computation explicitly non-authoritative. The review then
closed the source-law questions now stated in Sections 7 and 8. Finite traversal and local mutation are allowed only
when their semantics are closed. Exceptions cannot become judgment control, and source acquisition must remain hermetic.
Pattern meaning is independent of a host engine, while resource enforcement remains outside Admission.

A later 2026-09-22 occurrence review gave Admission independent occurrence meaning. It required a prior `Presented`
Input Occurrence, separated occurrence Establishment from physical evaluation and retention, and kept non-entry or
realization failure outside the `Admitted`/`Rejected` result vocabulary. Section 6.2 and Section 11 now contain that
law.

The subsequent binding review removed exact Input identity from Admission Definition meaning. Admission now owns only
its Input Observation Requirement, while ADR-0048 composition connects exact established Input and Admission
Definitions. Section 6 states the resulting authority boundary.

The 2026-09-25 HIR and compiler-product review aligned Admission with ADR-0071, ADR-0063, and ADR-0075. It made
Definition equality producer-owned, introduced the narrow Input Observation Requirement projection, separated IDL
selection from later composition, and placed cross-responsibility qualification and reuse behind compiler mediation. It
also separated Definition Refusal from compiler non-entry and occurrence-time `Rejected`.

The final 2026-09-25 closure review fixed Admission Definition identity at Authority plus Version. It also closed the
exactly-one `presentedInput` Basis law and confirmed that V1 needs no additional Applicable Context. The same review
completed the Established Admission protocol. Syntax coverage and compiler mechanisms such as verification, storage,
reuse, or subsystem placement remain with their compiler or Design owners.