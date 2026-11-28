# ADR-0069: Invariant Contract, Fact-Local Standing Integrity Law, Deterministic Judgment, and Establishment Gate

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation

## Related

- `../../../../the-most-important-thing/what-contract-is.md`
- ADR-0068: Fact Contract
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Core Fact Establishment, Change Proposal, Bound Fact Authority, and Outward Handoff
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry

---

## 1. Context

Invariant declares the standing internal integrity law that factual information must satisfy to hold authority inside
the Contract Core. Fact declares the immutable information, while Invariant declares the property that information
must satisfy. The two Contracts remain independent.

An Invariant Definition declares its required factual observations and integrity law independently of a particular
Fact Kind. The IDL selects the Fact to which the law applies (Section 4). Each application concerns exactly one
complete canonical candidate of that Fact Kind, regardless of its formation path.

Every lawfully established Fact satisfies all Invariants applicable to its establishment. The obligation is to
preserve this guarantee for the same immutable information under the applicable authority and validity laws, not to
execute a validator repeatedly.

Invariant owns only Fact-local integrity. It is distinct from Admission and from Operation-specific obligations,
including postconditions, result formation and the law governing how an Operation computes its result. A constructor
guard or callback validator is not an Invariant Contract. Operation-specific obligations remain in the complete
Operation Contract; Fact Sameness, State and Transition movement, Policy, Budget, Capacity, Governance and
Publication retain their own authority.

---

## 2. Problem

Without an explicit Invariant Contract, standing Fact integrity is easily hidden in implementation entry points such
as constructors, validators, services, repository hooks, Operation implementations, state managers, callbacks or
object methods. This makes the law dependent on those entry points and encourages Invariant to become a general
validator for obligations that belong elsewhere.

Allowing such a validator to inspect multiple Facts, populations, histories, repositories, State, Operation inputs,
implementation-local values or arbitrary callbacks would turn Invariant into a hidden query and computation engine.
The machine therefore needs a narrower authority: each Invariant application must be completely judgeable from one
complete canonical candidate Fact.

Anything that needs another Fact, another machine surface, an external source, a history of separately established
Facts or Operation-specific material is outside Invariant. This boundary still admits calculations over collections
and structured values contained in the one Fact.

---

## 3. Decision Drivers

Invariant is selected at the enclosing interface's explicit Core scope, through the Fact row in the IDL (Section 4).
An Operation does not select, own, repeat or bypass the standing law. The Definition is reusable across Fact Kinds,
but each declared application selects one exact Fact Kind and obtains all factual observations from one complete
canonical candidate of that kind.

The user declares the required observations, not a Change Proposal, current/proposed wrappers, populations,
histories, graphs or backend coordinates. Invariant neither grants Fact meaning nor defines Fact changes. Fact
Sameness and any coexistence restriction are outside its authority, as are State movement and the Operation
algorithm. It cannot acquire another Fact or construct populations or graphs from separately established Facts.

The admitted relation must be total, deterministic, terminating and completely refinable into Kontrakt-owned
material. Host runtime behavior must not remain Contract authority. A backend may optimize judgment only when it
preserves the same Fact-local law, proposal-level atomicity, attribution and deterministic outcome.

---

## 4. Interface-Level Invariant Selection

Facts and Invariants share the enclosing interface's explicit Core scope under ADR-0046 Section 12 and ADR-0047
Sections 4.6 and 6. The `facts : invariants` block selects the complete Fact vocabulary and the applicable standing
laws (ADR-0068 Section 8).

```text
interface AccountService {
    facts : invariants {
        DepositOperationInput
        DepositRecorded
        AccountOpened
        Balance : NonNegativeBalance
        WithdrawalRecorded
        AccountClosed
    }

    operation deposit(...)
    operation withdraw(...)
    operation close(...)
}
```

Each row selects one exact Fact Definition. A colon followed by a comma-separated list selects the Invariants for
that Fact; a row without such a list selects none. Neither row order nor Invariant list order establishes judgment
order. No separate Fact vocabulary class or Invariant catalog is required or admitted.

The same Invariant Definition may be selected for different Fact Kinds. Each selection retains its own exact Fact
association. The shared authoring position neither composes their Definitions nor transfers authority between them.
In particular, Invariant selection does not become Fact Binding meaning (ADR-0068 Section 19.4).

The IDL selection supplies the declared association; it does not by itself establish the legality of that association.
Each Invariant selection targets one exact Invariant Definition and identifies the exact Fact selection in the enclosing
interface (Section 19.5). The complete selection for that Fact is the scope of its standing-law Composition.
Repeating the same exact Versioned Invariant Definition for the same Fact selection is unlawful. Reusing that
Definition for another Fact is permitted. Independently identified Invariants with equal meaning are not duplicate
selections; established redundancy is diagnosed under Section 5.4.

The existing selection and Composition laws govern resolution and judgment (Section 19.9). No name, type resemblance,
package placement, inheritance, annotation, or runtime discovery adds an Invariant implicitly.

Whenever complete canonical candidate material of a selected Fact Kind may receive Fact authority in the applicable
Core, every standing law selected for that Fact applies. It is not repeated or selected inside an Operation manifest.
An Operation cannot bypass an applicable Invariant by omitting a slot.

---

## 5. One Flat Invariant Contract

### 5.1. Declaration and Judgment Composition

The authoring model follows ADR-0065 Section 5.2. One selectable Java or Kotlin class declares one flat Invariant
Contract. The complete integrity law may be written as one Boolean method, as several independent Boolean condition
methods, or through exact-target helper calls. Independent conditions are conjoined into one judgment. A Boolean
helper contributes where it is called, not a second time as an independent condition. Helpers may also compute
non-Boolean values within the judgment.

No privileged root method, annotation, marker interface, or additional IDL method selection is required. Method names,
source order and visibility do not establish independent conditions or Contract authority. Exact resolved declarations
and calls distinguish the contributing conditions and helpers. Getter and setter members of the selected class are not
permitted authoring forms. Closed constants and local values may contribute to the judgment under Section 13.

Several declarations may coexist in a source file. Reuse occurs through explicit selection of the same declaration,
not through inheritance, overriding, or virtual specialization. Each independently selected Invariant remains a
separate Contract even when its computation can be shared with another law.

The source class and its methods supply frontend evidence. Kontrakt does not instantiate the class as a validator or
retain its receiver, allocation, generated JVM shape, or executable host behavior as Contract authority.

### 5.2. Required Factual Observations

An independent condition declares its required factual observations through ordinary named Java or Kotlin parameters.
As in Admission, each parameter addresses a coordinate by its exact declared name, not by argument position, similar
host types, or an inferred synonym. The frontend resolves the required value meaning and the complete meaning of the
judgment under the common Type and SDK laws. Helper parameters follow the exact authored call; their names do not
independently select Fact coordinates.

The Factual Observation Requirement Law defines the factual information required by the integrity judgment. It is
independent of the particular Fact Definition used during source interpretation or selected in the IDL. Source
coordinates for attribution remain distinct from the law's meaning. The same Definition may therefore be selected
for different Facts under Section 4. Section 19.1 defines the complete Definition meaning.

Several independent conditions may require the same named coordinate. They address one factual coordinate, not
independent values that happen to have the same name. The requirement law retains the resolved meaning of each
declared requirement and its relation to the condition that declares it. Equal requirements may share a representation;
that sharing must not erase a declaration's meaning or its use in the judgment. A physical observation of the same
coordinate may also serve several conditions.

For example, conditions requiring `amount: Long` may independently constrain its lower and upper bounds. Both
judge the same factual amount. Neither the number of conditions nor the number of physical reads determines how
many coordinates the Fact must provide.

Different resolved requirements for one name remain distinct where their meanings differ. The requirement law does
not infer a common type, intersect the declared types into a replacement requirement, or arbitrarily select one of
them. The provided coordinate must satisfy every requirement under the common Type and Composition laws. A
nullable and a non-null requirement, for example, must be judged against the offered value meaning under those laws.
A presence test in one independent condition does not make a nullable coordinate admissible as a non-null operand
of another (Section 5.3). A lawful observation relation does not authorize an implicit transformation of the Fact.

A legal application must supply every required observation from the same complete canonical candidate Fact. A matching
name alone does not establish compatibility. The applicable Fact law, resolved value meaning and existing Composition
law determine whether the observation is legal. During composition, the required information about the selected Fact
is obtained through its producer-owned semantic protocol (ADR-0068 Sections 19.5 and 19.11), not by reconstructing
Fact meaning inside Invariant. Compatibility must hold throughout the Fact's permitted domain (Section 19.9.3). Kontrakt
must not supply missing information, infer a coordinate relation or implicitly
convert or normalize the Fact to make the judgment applicable (ADR-0068 Sections 5.1 and 6).

Every explicitly declared observation parameter of an independent condition remains a requirement, even when its
value does not contribute to the judgment. The declaration therefore still requires that observation from the
selected Fact. Section 5.4 governs the diagnostic for an unused requirement; removing a physical read or calculation
must not remove the declared requirement.

The complete declared requirement and the observations needed to compute the judgment serve different purposes.
A condition may use only part of the declared value, but the required value meaning must still be provided lawfully.
For a structured value, a nested read retains the resolved constituent relation within that value. A collection
predicate retains the member observations and the collection meaning on which it depends, including membership,
multiplicity, order or presence distinctions where relevant. These meanings follow ADR-0068 Section 6 and the
common Type and SDK laws. Nested constituents are not additional direct Fact coordinates, and neither a host
carrier nor a reference to the whole Core becomes an additional operand.

A narrower observation may suffice for a particular computation or comparison without weakening the complete
requirement. This allows shared reads and fine-grained reuse where the relevant meaning and its validity are
preserved. It does not permit an incomplete candidate to enter judgment or replace the obligation to supply every
declared requirement. Judgment and establishment remain subject to Sections 10 and 16.

### 5.3. Independent Conditions and Authored Evaluation

Within a condition, the exact Source operation determines evaluation. A short-circuit guard can establish the domain of
a later partial operation; an eager conjunction does not establish the same evaluation dependency. Separate independent
conditions, or separate Invariants selected on the same Fact row, do not establish implicit guards for one another.
Each must satisfy the applicable completion law. A compiler may optimize their realization only under Section 14.

### 5.4. Integrity Law Legality and Declaration Diagnostics

The complete integrity law of an Invariant must be satisfiable over its required observation domain. A Definition
may be established only after satisfiability has been established. The corresponding obligation for the combined
laws selected for a Fact is defined in Section 19.3. Neither obligation is a substitute for the totality, determinism
and termination requirements of Section 14.

#### 5.4.1. Definition-Local Satisfiability

The Definition is satisfiable if at least one lawful assignment of its required factual observations makes its
complete integrity judgment true. This is a property of the reusable Definition, independent of a particular
Fact Kind or a formation path. The domain includes all declared requirements, even those not used in the
calculation. Observations of the same named coordinate must describe the same factual value under lawful
observation relations. Constituent and collection observations retain the relations of the value from which they
are obtained (Section 5.2). Treating those observations as independent variables may change the judgment's domain
and is not a sound analysis of the Definition.

A lawful assignment must satisfy the observation requirements and the applicable resolved value laws. The
compiler may establish satisfiability with a witness whose admissibility and complete judgment are independently
verified, or with another sound argument that establishes existence in the legal domain. No particular Fact
Definition, actual Fact occurrence, Lowering or Operation reachability is required for this conclusion.

#### 5.4.2. Analysis Conclusions and Legality

Satisfiability analysis has three relevant conclusions:

| Conclusion                   | Consequence                                                                            |
|------------------------------|----------------------------------------------------------------------------------------|
| Satisfiability established   | The satisfiability prerequisite is met; all other owning obligations still apply.      |
| Unsatisfiability established | The law is inadmissible and must produce a compilation error.                          |
| Neither established          | The prerequisite remains unmet; the Definition or application must not be established. |

An explicitly false law, a contradiction within one condition and a contradiction between independent conditions
are inadmissible when unsatisfiability is established. The absence of a witness found by a search is not evidence
that no witness exists. An inconclusive, unsupported or interrupted analysis remains a compiler-owned result
under ADR-0074 and ADR-0075. It does not establish a Contract-law violation or a runtime Invariant refusal. A
compiler analysis establishes evidence for the owning judgment; it neither establishes Definition authority nor
replaces that judgment. Sections 19.7–19.9 define the Establishment handoff and the attribution of these conclusions.

The analysis must retain the exact resolved calculation and value meaning. For instance, fixed-width integer
arithmetic cannot be replaced by mathematical integer arithmetic where overflow changes the result. An analysis
may reason over an approximation only when the claimed conclusion follows soundly for the legal domain and
judgment. Unsatisfiability of a sound over-approximation of the satisfying assignments may establish actual
unsatisfiability. A witness from a restricted domain may establish satisfiability if it is legal and its complete
judgment is verified. A model in an enlarged domain or failure to find a witness in a restricted search does not
alone justify the corresponding conclusion about the actual law.

#### 5.4.3. Declaration Diagnostics

Kontrakt must provide compile-time diagnostics for established semantic redundancy, subsumption, tautologies and
other unnecessary or ineffective declarations. A condition is tautological when it holds throughout the legal
domain; subsumption establishes that satisfying one condition entails another. Redundancy establishes that a
condition adds no restriction to the conjunction. These conclusions concern the exact judgment domain, not the
appearance of the source expressions. An explicitly true law imposes no integrity restriction; the compiler
must explain this and guide the author toward removing or correcting it. Unused declared observations are diagnosed
without weakening the requirements of Section 5.2.

Unlike satisfiability, exhaustive discovery of these diagnostic relations is not an Establishment prerequisite.
Failure to establish an additional redundancy or tautology does not prevent an otherwise lawful Definition or
application from being established. An established relation must still be diagnosed; a compiler may not silently
remove a condition or requirement on the strength of that relation.

For example, over the resolved `Int` domain, the following independent conditions express the same restriction:

```kotlin
class ValidAmount {

    fun belowOne(a: Int): Boolean =
        a < 1

    fun nonPositive(a: Int): Boolean =
        a <= 0
}
```

The diagnostic must explain why the conditions are equivalent for that integer domain and guide the author in
reviewing the redundant declaration. Such a conclusion cannot be transferred to another value domain merely because
the source expressions look similar. Analysis must preserve the applicable resolved meaning, including distinctions
in value domains, arithmetic, evaluation and completion (Sections 13–14).

Diagnostics must identify the relevant declarations and explain the established semantic relation, its consequences
and a useful direction for correction. Where applicable, they identify the related source locations and the value
domain on which the conclusion depends. The compiler must not infer the intended correction or silently remove
conditions, selections or independent Contract authority. Diagnostic realization follows the existing diagnostic
architecture (ADR-0060 and ADR-0061); the obligation does not prescribe an analysis algorithm or diagnostic format.

#### 5.4.4. Analysis Realization and Reuse

The compiler owns the choice of analysis, its evidence and its resource limits. These do not determine the
Invariant's meaning. A solver, witness, proof or intermediate analysis result may be retained as a compiler
product, subject to its own validity law. Neither the availability of a particular solver nor its internal
representation is a Definition determinant.

Analysis must preserve the compiler's determinism and result-attribution laws. Reproducible work limits may be
used with a fixed analysis configuration; physical elapsed-time limits may interrupt work but cannot establish a
semantic conclusion. Exhausted resources and unsupported analysis retain their compiler-owned attribution.
Parallel analysis must not turn a worker's completion order into a determinant of Contract legality.

A conclusion used by an owning judgment requires a qualified justification for the actual resolved law and domain.
The analysis producer and the applicable verification owners establish that justification; Invariant consumes their
results under ADR-0075. The analysis encoding must preserve the meaning needed for the claimed conclusion.
A SAT witness must be admissible and its complete judgment verified. For joint satisfiability, a partial assignment
must also be shown to extend to a complete lawful Fact. An UNSAT conclusion requires a sound argument covering the
entire legal domain. A solver status, an unsatisfiable core alone, an unchecked assumption or an unresolved proof
obligation cannot substitute for that justification. Different analyses may use different qualified verification
methods; no single proof format or solver is prescribed.

Reuse requires the exact determining meaning and applicable validity context. A retained witness may be checked
against a changed law and domain instead of repeating a search; it cannot prove the changed law merely because it
was valid for the previous one. Reused unsatisfiability evidence and diagnostic conclusions likewise require a
sound current justification. The common compiler product and preservation laws apply to clean, incremental and
parallel computation. Reused evidence must retain its determining assumptions and proof scope. An unchanged subset
of a law does not establish the validity of an argument that also depends on changed material.

A clean analysis may exhaust its resources where an incremental analysis can verify an available witness. This is
a difference in computational completion, not a different semantic truth or a relaxation of the Establishment
obligation. Neither path may manufacture a negative conclusion from lack of evidence. Conflicting purportedly
established conclusions for the same exact law and domain indicate an analysis, interpretation or verification
failure; scheduling order cannot resolve that conflict. The applicable compiler owner handles it under the common
Result laws. No particular analysis algorithm, evidence format or persistent store is mandated here.

---

## 6. Example

A single Boolean condition may express the entire integrity law.

```kotlin
class NonNegativeBalance {

    fun preserved(amountMinor: Long): Boolean =
        amountMinor >= 0L
}
```

When selected beside `Balance` in Section 4, the law constrains the exact `amountMinor` coordinate of each complete
candidate. The same declaration may be selected beside another Fact that lawfully provides the required observation.
The body acquires no additional Fact information and performs no authoritative formation or movement.

Coordinate ordering may be declared in either supported source language:

```kotlin
class ValidPeriod {

    fun preserved(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Boolean =
        startEpochMillis <= endEpochMillis
}
```

```java
public final class ValidPeriod {
    public boolean preserved(long startEpochMillis, long endEpochMillis) {
        return startEpochMillis <= endEpochMillis;
    }
}
```

These are alternative source forms, not two selections of the same declaration. The Kotlin or Java class must resolve
exactly under the IDL selection and the applicable source profile.

```text
facts : invariants {
    ReservationPeriod : ValidPeriod
    MaintenancePeriod : ValidPeriod
}
```

Arithmetic coherence may compare explicit coordinates already present in one Fact:

```kotlin
class ValidAmountBreakdown {

    fun preserved(
        totalMinor: Long,
        principalMinor: Long,
        feeMinor: Long,
    ): Boolean =
        totalMinor == principalMinor + feeMinor
}
```

The arithmetic retains its exact qualified Source meaning; the example does not replace fixed-width arithmetic with
mathematical integer arithmetic.

Several independent conditions may contribute to the same law, including conditions that use exact-target helpers:

```kotlin
class ValidRange {

    fun ordered(min: Long, max: Long): Boolean =
        min <= max

    fun nonNegative(min: Long): Boolean =
        isNonNegative(min)

    private fun isNonNegative(value: Long): Boolean =
        value >= 0L
}
```

The two independent conditions form one integrity judgment. The helper contributes only where called. The following
selection shows reuse of that Definition and the separate selection of another Invariant:

```text
facts : invariants {
    MemoryRange : ValidRange, ValidAlignment
    IndexRange  : ValidRange
}
```

Each application requires a legal association with the corresponding Fact and obtains all its observations from one
complete candidate of that Fact Kind.

---

## 7. Intra-Fact Integrity Functions

Invariant provides Fact-local integrity over explicit factual material. Representative laws include the following:

- **Value-domain integrity:** confines a factual value to its declared domain or range.
- **Coordinate ordering:** relates explicit positions or bounds inside the same Fact.
- **Arithmetic coherence:** expresses an exact relation among factual quantities already contained by that Fact.
- **Coordinate compatibility:** requires individually admitted factual values to form an allowed combination.
- **Conditional presence and absence:** requires an explicit coordinate to be present or absent under another
  explicit factual condition.
- **Finite-alternative coherence:** keeps the selected alternative consistent with the explicit material carried by
  the same Fact.
- **Flag and bit compatibility:** constrains explicit factual flags or bits contained by that Fact.

These examples describe integrity laws, not a separate value or operation inventory. A law may also judge the
constituents, cardinality, order or relations of a complete structured value owned by the same Fact. Section 13
governs the supported source and calculation meanings. None of these functions authorizes acquisition of material
outside that one complete Fact.

---

## 8. No Redundant Fact Material for Invariant Convenience

A derived coordinate should not be added merely to create an Invariant. When one authoritative factual coordinate is
sufficient, duplicating a calculable value creates unnecessary material and an unnecessary standing law. Invariant
preserves explicit factual meaning that must exist; it does not justify redundant representation.

---

## 9. Aggregate Meaning and Fact-Local Computation

A Fact may contain explicit coordinates whose declared meaning is an aggregate, total, count, balance, or other
summary. Invariant may compare those already formed coordinates because they are part of one complete canonical Fact.
It may also derive a temporary aggregate from a complete collection contained in that same Fact, subject to the
qualified calculation and completion laws of Section 13. Such computation establishes no new factual coordinate.

For example, an explicitly declared total may be compared with a reduction of the Fact's own complete entries:

```kotlin
class ValidTotal {

    fun coherent(amounts: List<Long>, totalMinor: Long): Boolean =
        amounts.sum() == totalMinor
}
```

The example presupposes eligible, completely interpreted Fact value meanings and qualified Source operations. It
declares coherence within the one Fact; it does not establish whether the entries or the total were formed correctly
from external sources. Invariant may not acquire or collect other Facts, external entries, or historical material to
perform an aggregation. Formation remains the responsibility of its owning law and replaceable realization.

If a relation between explicit Operation material and a produced aggregate is contractually meaningful, that relation
needs an explicit owner elsewhere in the Operation pipeline. This ADR creates no Result Contract and assigns no such
obligation automatically.

When a Fact independently declares scalar coordinates with aggregate meaning, they lower as ordinary canonical
coordinates. An arithmetic relation over those coordinates needs no aggregate-specific IR node merely because the
coordinates carry summary meaning. Kontrakt must not synthesize a summary Fact, aggregate-specific Fact declaration,
aggregate validator, or another Fact Kind merely to expose calculated values to Invariant judgment (Section 8).

If the Fact vocabulary does not independently declare those values as factual material, calculated totals remain
Operation Result Material, processing material, or backend implementation material according to their actual role.

---

## 10. Exactly One Fact

Each Invariant application judges exactly one Fact, even when its Definition is selected for other Fact Kinds.
Its user-facing parameters expose only the required factual observations under Section 5.2.

Kontrakt rejects several Fact parameters and collections of Facts, including `Set<Fact>`, `List<Fact>`,
`Sequence<Fact>`, `Stream<Fact>`, arrays and iterators of Facts. It also rejects acquiring another Fact through
lookup, navigation, a repository or a callback. Facts do not reference other Facts.

Shared coordinate names, types and values create no implicit relation, join, lookup obligation, existence obligation
or Invariant scope. Kontrakt must not construct an implicit collection of all Facts of one kind or a group from equal
coordinate values. The same prohibition applies to any sequence, history, interval set, relation or graph constructed
from separately established Facts for Invariant judgment.

---

## 11. No Cross-Fact Standing Law

Invariant provides no standing law over several separately established Facts. In particular, it cannot count or
aggregate a Fact population, impose conservation, order independently established Facts, compare interval sets or
enforce continuity across Facts. Coexistence, coverage, partition, population and historical monotonicity obligations
across Facts are outside its authority as well.

Excluding such a law from Invariant does not automatically assign it to Admission, Lowering, Change, State, Transition,
Publication or another Contract. If the machine needs that obligation, its exact subject, material, scope and owning
Contract must be determined separately. This ADR decides only that an implicit cross-Fact judgment is not Invariant.

---

## 12. Fact Sameness and Coexistence Are Outside Invariant

Fact Sameness belongs to ADR-0068. Invariant does not restate or recheck it. Fact owns no declared uniqueness or
other restriction on the coexistence of different Facts, and this decision introduces no such obligation into
Invariant. Sharing some factual values does not grant supersession, replacement, currentness change, or a relation
between Facts.

The decisive test for an Invariant is:

```text
Can this law be judged completely from one complete canonical candidate Fact
without observing another Fact, Operation Input, Operation Result Material,
State, a collection or history of separate Facts, repository, or external source?

yes:
    it may be an Invariant

no:
    it is not an Invariant
```

---

## 13. Admitted Source Expressions

Invariant is defined by its standing integrity law, not by a catalog of Java or Kotlin spellings. The source may use
the ordinary judgment expressions and closed computations governed by ADR-0065 Sections 7–10, with lawful factual
observations taking the place of Input observations. Invariant adds the Fact-local boundary of Sections 5, 10 and 15;
it does not introduce another calculation language or independently define the meaning of an operation.

### 13.1. Common Interpretation

Kontrakt resolves the author's selected source operation under ADR-0079 and consumes qualified standard-library meaning
under ADR-0084. Numeric, character and text, collection and structured value, and temporal calculations follow
ADR-0078 and ADR-0080–0082. The applicable common language and expression laws govern ordinary operators, control flow,
and their exact evaluation. Platform qualification and preservation remain governed by ADR-0073.

Users express their intent through ordinary Java or Kotlin, including its supported standard-library calls. The
frontend must preserve the exact Source-bound meaning rather than substitute a Kontrakt-defined operation merely
because both are similar. Invariant maintains no independent value or operation inventory. It consumes the completed
meaning, subject to its own integrity and observation law. Source interpretation and eligible realization are separate
responsibilities.

### 13.2. Closed Judgment Computation

The source may express finite control flow, exact-target helpers, and temporary derived computations according to
ADR-0065 Sections 7.2–7.5. These include local calculations over closed structured values, collection transformations,
quantification, and reductions. A qualified lambda or method reference contributes through its exact invocation law;
it must not escape the judgment or acquire additional authority. A finite loop may contribute when its domain and
completion are established. Unproved general recursion or open-ended computation is unsupported under the existing
V1 coverage rule, not a separate permanent prohibition on source syntax.

Fresh judgment-local state may be mutated under the closure and effect rules of ADR-0065 Section 7.2. It must not
mutate the candidate, its constituents, or Definition material, become shared, or escape the judgment. The compiler
must resolve every relevant update. A temporary transformation, such as parsing, ordering or normalization, is not a
formation or canonicalization authority and does not change the factual information being judged.

Collection observations retain the Fact-owned constituent meaning (ADR-0068 Sections 6.2–6.3). A host implementation
cannot invent an order, membership or multiplicity distinction. A temporary order or reduction follows the qualified
Source law, including any relevant tie behavior, evaluation and completion. Side-effect freedom alone does not permit
reassociation or parallelization. No additional Fact may be acquired through a collection, pipeline or callback.

### 13.3. Source Qualification

The exact callable and all relevant reachable behavior must be resolved, including implicit getters, operator calls,
initialization and invocation effects, under ADR-0065 Sections 7.3 and 7.6. Unknown behavior is not admitted merely
because it compiles or returns `Boolean`. Kontrakt does not execute user code or application initialization to discover
Invariant meaning. Closed constant initializers may contribute without executing the application; an effectful
constructor or initializer is not a lawful authoring form even when unused. The same closure and finite-completion
requirements apply to pattern and regex operations (ADR-0065 Section 7.7).

The fully refined law, not the host execution mechanism, supplies Invariant meaning. Qualification of a particular
source profile or callable remains the responsibility of the existing common subsystems.

---

## 14. Totality, Determinism, and Termination

Every Invariant declaration must be total, deterministic, and terminating over its legal observation domain. The
completion and deterministic evaluation rules of ADR-0065 Sections 7.5 and 10 apply to the integrity judgment.

A partial operation requires an established domain on each path that reaches it, an explicit non-exceptional semantic
result, or an exactly qualified source-authored handler that preserves the complete judgment. Authored exceptional
control follows its exact Source meaning; an unhandled exception is not an implicit Invariant result. Source guards,
mandatory evaluations and normal or abrupt completion must be preserved. Separate conditions supply no implicit
inter-condition guard (Section 5.3).

Overflow, `NaN`, locale, timezone, comparator behavior and other value distinctions follow the applicable resolved
Source and semantic laws. Object identity, incidental traversal order or unspecified host behavior cannot determine the
Contract. Where a Source operation permits multiple outcomes, the existing deterministic-refinement law applies: a
lawful deterministic choice or indistinguishability under the complete judgment and mandatory observations must be
established. The choice must not invent a factual distinction or acquire forbidden material.

If the required semantics, closure or completion cannot be established, the source is unsupported. This does not
establish a runtime Invariant refusal. Resource limits on finite work remain the responsibility of Budget, Capacity,
compiler safety and backend policy; finite work need not have a fixed Definition-time step count.

Every realization must preserve the law and the exact factual observations under the applicable selection. A cached,
parallel, fused or optimized calculation must not substitute information from another candidate or World. Equal Boolean
results alone do not establish conformity. Required failure attribution and Diagnostic Evidence obligations must remain
intact. The common compiler verification and preservation laws govern such transformations.

---

## 15. Forbidden Observation and Behavior

The judgment is closed over the declared factual observations and the eligible computation of Section 13. The
following observations and behaviors are forbidden in Invariant source:

- **Operation or proposal material:** Operation Input, Operation Result Material, implementation-local values, Change
  Proposal, current/proposed pairs, candidate wrappers or overlay views.
- **Other factual information:** another Fact, a Fact reference, a Fact collection or a hidden Fact context.
- **Other Contract authorities:** State, Transition, Policy, Budget, Capacity, Governance, Publication or Diagnostic
  material.
- **External information and execution state:** database rows, repositories, services, network, files, clock,
  randomness, environment, thread, lock or transaction state.
- **Host mechanisms:** reflection, runtime class discovery, proxy behavior, object identity as a semantic discriminator
  or
  unqualified custom `equals`, `hashCode`, `compareTo` or `Comparator` behavior.
- **Changes to authoritative information:** Fact mutation (including through an alias), repair or authoritative
  normalization, replacement, removal, currentness change or State movement.
- **Unclosed source behavior:** instance state, mutable globals, application initialization behavior, inheritance or
  override dispatch as Contract authority, unresolved interface-provided behavior, open or escaping callbacks and
  closures, unqualified collection, stream or sequence behavior, unresolved method calls or references, unproved
  recursion or open-ended work, and unqualified exceptional control.
- **Backend state:** indexes, cache state, counters, accumulators, delta state, incremental-maintenance state or an
  evaluation plan.

An implementation detail does not become legal Invariant material because it is deterministic in one backend.
Closed judgment-local calculations under Section 13 do not acquire these prohibited observations or exercise these
authorities. User logging, metrics, tracing and event publication remain external effects under the common judgment
source law (ADR-0065 Section 8).

---

## 16. Change Proposal Judgment

The user does not declare or observe a Change Proposal.

Standing Invariant judgment applies to complete candidate information at both Core entry and result-side establishment.
Lowering supplies a complete Fact Candidate set, not Fact authority (ADR-0067 Section 17). When the complete Operation
Contract causes Kontrakt to form one Change Proposal internally, Kontrakt resolves every proposed Fact independently
under
its declared Fact Kind and canonical factual material.

For each proposed Fact, Kontrakt applies the Fact-owned judgments required by ADR-0068 and every interface-level
Invariant selected for that exact Fact Kind. Fact Sameness governs informational equality and multiplicity under
ADR-0068; neither identical information nor shared factual values alone justify refusal or remove an independent
judgment obligation.

All required judgments and the resulting establishment concern the same complete, stable information (ADR-0068
Section 5.1). Partial observations, asynchronous computation and parallel evaluation must preserve that correspondence.
The applicable Composition determines the indivisible establishment boundary; judgment success alone grants no Fact
authority or Operation participation.

The correspondence covers both the information judged and the particular application to which the result belongs.
Equal information in another candidate may permit calculation sharing, but does not transfer the established
judgment to that candidate. Formation, judgment and use must preserve the same complete information and exact
application, including through shared backing or deferred physical materialization. Deferred materialization may
represent information already determined; it cannot defer the choice of factual meaning until after judgment.

The applicable parts of the proposal complete under the Boundary Processing Completion law (ADR-0057 Section 4.3.3).
One Invariant refusal does not suppress another independently reachable judgment in the same active boundary.
The shared Composition consumes each owning judgment and alone determines indivisible establishment under ADR-0049.
Neither successful integrity judgment nor successful movement grants partial establishment. Results already established
by other owners remain their results when the proposal cannot be established. This requires no new transaction or
post-Crash persistence guarantee.

The law also applies independently of the formation path. Physical restoration of an already Established Fact is not
new establishment when its exact information, authority and validity are preserved under ADR-0068 Section 5.3. Equal
payload or backing identity alone is insufficient to preserve the guarantee. The existing applicable World and
Governance laws govern selection and continuity; Invariant does not acquire those authorities or observe ambient
Policy state.

```text
one complete internally formed Change Proposal
    -> each proposed Fact material
        -> resolved Fact kind and complete canonical factual material
        -> applicable Fact-owned judgments under ADR-0068
        -> every interface-level Invariant selected for that Fact Kind
            -> all preserved
                -> proposed Fact remains eligible for indivisible establishment
            -> any refused
                -> whole Change Proposal is refused
    -> every applicable State / Transition movement judgment
    -> only complete success permits indivisible establishment
```

The diagram names judgment dependencies, not a required physical execution order. A backend may fuse, reorder,
specialize or statically discharge judgments only when the same Fact-local law, proposal-level atomicity, refusal
attribution and deterministic outcome remain unchanged.

---

## 17. Relationship to State and Transition

Invariant returns only the standing-law judgment over one proposed Fact. It neither defines how Facts change nor
inspects whether an Operation used the correct algorithm. Comparing independently established Facts or repairing a
refused Fact also falls outside its authority.

State and Transition independently judge movement legality over the same internal Change Proposal. Neither authority
substitutes for the other. Only complete success under all required authorities permits establishment.

---

## 18. Cycles Are Not Invariant Violations

Contract cycles are not Invariant violations. Recursive Contract composition and cyclic Contract dependency are
structurally inadmissible and must be rejected by compiler validation before establishment. A cyclic State Machine
Manifest is rejected by its own movement law. Kontrakt does not admit a semantic cycle and ask an Invariant to
detect it afterward.

Factual coordinates such as `fromId` and `toId` do not cause Kontrakt to construct an implicit graph from separately
established Facts.

---

## 19. Definition-Time Law

At definition time, Kontrakt resolves each exact Invariant declaration and its IDL selections under ADR-0046 and
ADR-0047. Source interpretation must produce the complete integrity law and required factual observations under
Sections 5 and 13–15, without making the selected Fact a determinant of the reusable Invariant Definition.

### 19.1. Definition Meaning and Semantic Equality

Two components determine the Invariant Definition meaning.

**Integrity Judgment Law**

Defines the complete judgment that determines whether a candidate Fact satisfies the Invariant. It preserves the
meaning of every declared independent condition and the qualified computation on which that condition depends.
Closed constants, helper computations and meaning-determining operations contribute their resolved meaning rather
than their host execution mechanism. Evaluation and completion follow Sections 5.1, 5.3 and 13–14. The law
retains declared conditions even when an analysis establishes redundancy (Section 5.4).

**Factual Observation Requirement Law**

Defines the factual information required by the judgment under Section 5.2. It preserves the complete declared
requirements, their resolved value meanings and their relations to the judgment. Requirements that do not affect
the Boolean result remain part of this meaning.

Any semantic basis that determines either component is preserved as part of that component under its owning law.
It may be observed through an exact producer-owned reference to completed meaning; an unresolved source or type
reference does not supply that meaning. No additional determinant is introduced merely because the frontend
consulted another compiler product. The selected Fact Definition, its Version, the selecting Interface and the
particular IDL association remain outside Invariant Definition meaning. Source attribution, qualification and
analysis evidence, solver configuration and derived realizations are also distinct from that meaning. Required
compiler evidence remains available under its own law.

Invariant owns semantic equality for its Definition meaning. Two Definition meanings are equal only when both
complete determinants are equal under their owning laws. Common source refinement may eliminate source-only
differences, including names of condition methods or helpers and declaration order where they do not determine
meaning. It must retain authored evaluation wherever it is significant. Neither the original host syntax nor a
particular IR structure prescribes semantic equality.

Equal Boolean results, even an established equivalence over the entire observation domain, do not alone establish
Definition Meaning Equality. Two declarations may impose the same restriction yet differ in their declared
conditions or observation requirements. An optimization may share their computation when preservation is
established; it cannot thereby identify the two Definition meanings. A resolved but unlawful law or requirement
remains visible for its owning judgment rather than being repaired by refinement.

Definition Meaning Equality is distinct from reference identity, Version equality, equality of application or
judgment results, and current reuse validity. Interning, fingerprints and physical sharing may accelerate the
comparison and realization of meaning but cannot establish these relations independently of their owning laws.

### 19.2. Definition Identity and Version

ADR-0053 owns Invariant Authority continuity and Version semantics. ADR-0063 owns the common Definition identity
and reference law. One Invariant Authority owns one independently addressable Definition per Contract Version in
the flat declaration model of Section 5.1. No additional Authority-Local Definition Coordinate is needed.

```text
exact Invariant Contract Authority
+
exact Contract Version
    -> exact versioned Invariant Definition
```

The condition methods, helpers and observation requirements do not own independent Contract Versions. The
resolved pre-authority Candidate and Established Definition remain distinct under ADR-0063 and ADR-0071.
Selection by another Fact or another Interface does not change the Definition identity. The Invariant-specific
Candidate reference domains and HIR Protocol are defined in Sections 19.5–19.6. The established semantic protocol
remains open (Section 22).

A Contract-visible change to either determinant follows the Version law when the Authority continues. Adding an
explicitly required observation is such a change even if the Boolean judgment is unchanged. Source-only and
realization-only changes do not require a new Version when canonical Contract meaning remains identical under
ADR-0053. Changes to analysis evidence or strategy do not themselves change that meaning. Equal Definition meaning
does not merge distinct Authorities or Versions; conflicting meaning at the same Authority and Version is governed
by ADR-0053 and ADR-0063. Renames and continuity follow the same owning Version law.

### 19.3. Fact-Scoped Application and Joint Satisfiability

The exact selected Fact association and its observation requirements are judged under the existing Composition law.
Relation information is acquired and prepared under ADR-0077, using the exact producer-owned semantic observations
of the selected Fact (Section 5.2). CRI neither selects the law nor owns the judgment of its applicability or
integrity. Kontrakt derives stable Fact-kind and coordinate dependency indexes from the resolved selections and
requirements without giving these indexes Contract authority.

The joint law concerns the complete selection of Invariants for one exact Fact. It is satisfiable if at least one
complete factual value permitted by that Fact Definition lawfully supplies every selected Invariant's declared
observations and satisfies every selected integrity judgment. The domain is the Fact's permitted factual meaning,
not merely the observation domain of an individual Definition. The independently applicable laws must observe the
same value, including its constituent and coordinate relations. Neither Lowering's formation range nor the values
reachable through an Operation determine this domain.

Joint satisfiability must be established before the application of that complete selection can be established.
Individual Definition satisfiability is not sufficient. If the combined obligations are established to be
unsatisfiable, the application is inadmissible and must produce a compilation error, even when every selected
Definition is independently lawful. For example, selecting one law requiring `amount > 0L` and another requiring
`amount < 0L` for the same `amount` coordinate of one Fact makes their joint application impossible. This does not
make either independent Definition inadmissible in isolation. Even one selected Invariant may be unsatisfiable
within the narrower domain offered by the selected Fact despite being satisfiable independently.

The conclusions and diagnostic obligations of Section 5.4 apply to the combined obligations. Established
contradictions require errors; established redundancy or subsumption must be explained without silently removing
a selected Contract. An inconclusive analysis prevents the application from being established, but is not proof
that the selection violates the law. No application result may be inferred from an incomplete selection or an
incomplete observation requirement.

The analysis is distinct from judging whether an actual candidate Fact satisfies the selected laws (Section 16).
Its evidence is a compiler product, not Fact authority or an independently established Invariant Definition.
Current reuse must account for the complete selection, the relevant factual meaning and the selected Definition
meanings under the applicable validity laws. Equality of an observed coordinate or reuse of an individual
Definition's witness does not establish joint satisfiability of a changed selection. A narrower observation may
avoid recomputing an unaffected analysis when its exact determinants are preserved; it cannot replace the
complete applicability requirements. Section 19.9 defines the Composition judgment, its Required Basis and the
conditions for establishing the application.

### 19.4. Common HIR and Establishment Boundary

The common Resolved Contract HIR and Seal law (ADR-0071) and Contract Establishment law (ADR-0063) apply. A complete
resolved Candidate is not itself established authority. Only established meaning may become part of the Canonical
Contract World under the applicable owning law. Legal consumption follows ADR-0075. Host source mechanics must be
erased before authority begins.

### 19.5. Typed Invariant HIR References and Selection

Invariant owns two Primary HIR Candidate kinds: the Definition Candidate and the IDL Binding Candidate. Their
reference domains under ADR-0071 are:

```text
InvariantDefinitionCandidateRef
InvariantIDLBindingCandidateRef
```

`InvariantDefinitionCandidateRef` designates one exact current Definition Candidate. Its Authority and resolved
Version candidate coordinate follow Section 19.2. Neither equal Definition meaning nor a shared representation
identifies distinct Candidates. Conflicting identity claims are handled by the common Resolution and Version laws.

`InvariantIDLBindingCandidateRef` designates one exact current Invariant selection occurrence. Each selection
expresses the following relation:

- The selecting `ContractInterfaceRef` and the Invariant role.
- The subject `FactIDLBindingCandidateRef`, identifying the Fact selection within that interface.
- The target `InvariantDefinitionCandidateRef`.

The subject is the Fact selection, not merely its name or target Definition. Fact owns that reference domain under
ADR-0068 Section 19.3. The selecting context must agree with the subject's owning interface; a reference to a
Fact selection in a different interface cannot be repaired by substituting a similarly named Fact. The Binding
Candidate neither copies Definition meaning nor adds the Invariant selection to Fact Binding meaning. Context that
is already supplied through the exact subject need not be stored again.

Every selection occurrence remains distinct before judgment, including repeated selections targeting the same
Definition. The complete relation is observed for one exact Fact Binding Candidate, independently of list order.
A row without Invariant selections expresses absence under Section 4 and the common selection law. Unresolved or
unavailable selection is not Explicit Absence. This relation introduces no third Primary Candidate.

**Observation Requirement Selector**

A requirement addresses a nominal factual coordinate within one exact Invariant Definition Candidate. The Definition
reference and coordinate name suffice to select the complete group of declared requirements for that coordinate.
Invariant introduces neither a separate Requirement Reference Domain nor independent condition or helper reference
domains. It owns the requirements, not the Fact coordinates that may satisfy them. Source locations, method names
and physical ordinals do not establish the selector's identity. The selector must not designate a requirement in
another Definition. Internal dense handles or other access paths remain replaceable implementations under ADR-0071.

### 19.6. Resolved Invariant HIR Candidate Protocol

Invariant specializes the read-only Resolved HIR Candidate Protocol of ADR-0071 using the reference domains in
Section 19.5 and the common and Fact-owned references that section identifies.

```text
Invariant Definition Candidate Projection
    exact InvariantDefinitionCandidateRef
    complete Invariant Definition Candidate meaning

Invariant IDL Binding Candidate Projection
    exact InvariantIDLBindingCandidateRef
    complete Invariant IDL Binding Candidate meaning

Fine-Grained Semantic Projections
    Invariant Factual Observation Requirement Surface Projection
    Invariant Selected Factual Observation Requirement Meaning Projection
    Invariant Selection Projection
```

**Invariant Definition Candidate Projection**

Exposes the Authority, resolved Version candidate coordinate and both complete determinants of Section 19.1.
Required interpretation and qualification evidence remains legally observable, distinct from Definition meaning.
Exact references to completed producer-owned meaning may supply these observations without copying it. An unresolved
reference, source expression or later Type calculation cannot supply missing Candidate meaning.

The Integrity Judgment Law preserves the distinction between independent conditions and the evaluation within each
condition. It retains exact helper computation, meaningful evaluation order, guards and partial-operation semantics
under Sections 5.1, 5.3 and 13–14. Erasing host invocation mechanics or sharing a helper must not remove those
relations. In particular, conjoining two independent conditions cannot manufacture a guard that neither condition
established within its own computation. Source-only differences may be erased under Section 19.1; declaration
multiplicity and the relations that determine the complete law remain observable.

The Factual Observation Requirement Law retains every declared requirement and its relation to the declaring
condition, including unused requirements. The relation identifies the condition within the complete resolved law;
it does not give a method name, source order or an independent condition its own Contract identity. Equal requirement
meanings may share backing without losing their multiplicity or condition relations (Section 5.2).

**Invariant IDL Binding Candidate Projection**

Exposes the exact selection occurrence and its complete meaning under Section 19.5. Equality preserves the selecting
context, role, exact Fact association and target relation. A change confined to the target's meaning does not by
itself change the selection relation, provided the exact references and their correspondence remain valid. Separate
selections are not merged because they have equal target meanings.

**Invariant Factual Observation Requirement Surface Projection**

Exposes the complete nominal coordinate range required by one exact `InvariantDefinitionCandidateRef`. Each name is
observed once at this surface: the number of conditions requiring the same coordinate does not change which factual
coordinate must be provided. This projection supplies the coverage observation without exposing every requirement's
value meaning or the judgment body.

Equality compares the nominal coordinate set, not source order, value meaning or the number of declarations requiring
a coordinate. Adding a requirement for an existing name may leave this observation unchanged while changing the
Selected Requirement Meaning. A complete empty surface is distinguishable from incomplete or unavailable material;
it does not itself establish Definition legality.

**Invariant Selected Factual Observation Requirement Meaning Projection**

Exposes the complete group of declared requirements selected by one exact Definition Candidate and nominal coordinate
under Section 19.5. It retains each resolved required value meaning, multiplicity and the relation to the declaring
condition. Distinct requirements for the same name are not replaced by an inferred common type. Nested constituent,
presence and collection requirements follow Section 5.2 and the applicable common value laws. Calculation relations
between supplied values belong to the Integrity Judgment Law unless they determine the required observation meaning
itself.

Equality preserves the selected coordinate and its complete group of requirement meanings and condition relations,
independently of source-only ordering or naming. A change in the judgment body alone need not change this observation
when its requirements and relevant relations are preserved. Observation of one group does not establish the completeness
of the entire requirement surface or the absence of another group.

**Invariant Selection Projection**

Observes the complete selection for one exact `FactIDLBindingCandidateRef` at the Invariant role. It exposes every
`InvariantIDLBindingCandidateRef` occurrence; the Binding Candidate Projection supplies each association and target.
Equality preserves the exact subject, role and complete selection relation, including multiplicity but not
non-semantic list order. Duplicate selections remain observable until judgment under Section 19.9.1. Incomplete
selection is neither absence nor a complete smaller selection. A separate interface-wide Invariant selection
projection is not required to observe the entire vocabulary: consumers may obtain the Fact vocabulary and each
required Fact-scoped selection under their owning protocols.

The following example uses the `Balance` selection in Section 4. Assume completed source interpretation of an
Invariant requiring `amountMinor` as a signed 64-bit integer and judging it non-negative. The labels designate exact
references; `V1` is an illustrative resolved Version candidate coordinate. The example describes logical observations,
not an IR instruction vocabulary or storage layout.

```text
Invariant Definition Candidate Projection
    InvariantDefinitionCandidateRef D
    Authority NonNegativeBalance
    resolved Version candidate coordinate V1
    Integrity Judgment Law
        amountMinor >= 0L
    Factual Observation Requirement Law
        amountMinor -> complete signed 64-bit integer meaning

Invariant Factual Observation Requirement Surface Projection
    Definition D
    amountMinor

Invariant Selected Factual Observation Requirement Meaning Projection
    Definition D
    coordinate amountMinor
    complete signed 64-bit integer meaning
    relation to the declaring condition

Invariant IDL Binding Candidate Projection
    InvariantIDLBindingCandidateRef B
    selecting ContractInterfaceRef I (AccountService)
    role invariants
    subject FactIDLBindingCandidateRef F (Balance selection)
    target InvariantDefinitionCandidateRef D

Invariant Selection Projection
    subject F
    role invariants
    B
```

The complete observations retain required evidence even where the example omits its detail. None supplies a
Compatibility or Satisfiability conclusion. Those are separately owned judgments or analysis results, not HIR
projections. This Protocol does not require separate projections for an isolated condition, helper, Integrity Judgment
Law or Requirement-to-Fact mapping. A new observation requires a legal consumer demand under the common Protocol law.

**Observation Equality, Reuse and Visibility**

Completeness and equality apply to the declared observation of each projection. Complete Candidate observations
include any required evidence they expose; evidence equality is distinct from Definition Meaning Equality.
Cross-generation
comparison requires exact subject correspondence and current reference validity. An equal projection is not sufficient
by itself for current reuse.

A consumer judging complete requirement coverage observes the whole Requirement Surface and every needed Selected
Requirement Meaning, together with the required offered meaning from Fact. Observing only previously known coordinates
cannot establish that no new requirement has been added. The same rule applies to complete Fact-scoped Selection:
validity of the previously observed Binding references alone cannot establish that the current selection is unchanged.

A narrower observation may prevent unnecessary recomputation when a different part of the Definition changes. Such reuse
must preserve the exact Version, selection and validity obligations of the current consumer. Bulk access, shared
backing,
internal coordinate indexing and fine-grained dependency recording may realize these observations without separate
physical records or Query nodes. Reference lifetime, coherent visibility, required extensions, Protocol evolution and
representation preservation follow ADR-0071. An unknown required extension or stale reference cannot be treated as a
complete observation or absence. The Protocol creates no authority, and no new global equality or persistence law.

### 19.7. Invariant HIR Seal and Establishment Handoff

The Seal certifies completed interpretation and representation integrity, not satisfaction of Invariant law. A
Definition Candidate may become Visible HIR only when:

- Its exact Authority and resolved Version candidate coordinate are known.
- Both complete determinants are resolved and preserve the meaning required by Sections 5, 13–15 and 19.1.
- The responsible source interpretation and qualification producers have supplied their required legal results.
- Required references are exact and valid, and all required Protocol observations are complete in a coherent generation.

Required qualification is not performed by the Invariant HIR Producer in place of its owning producer. An unresolved
callable or a computation whose required eligibility or completion cannot be established prevents lawful formation.
A forbidden initializer cannot be erased to manufacture qualification. An eligible and completely interpreted law
whose contradiction is established remains a Candidate for the owning judgment; satisfiability is not a Seal
condition.

A Binding Candidate must resolve the complete selection occurrence of Section 19.5, with its exact selecting context,
role, subject and target legally referenceable. Its Definition need not yet be established. Seal does not decide
Observation Compatibility, selection legality, Joint Satisfiability or authoritative Binding.

The complete Selection Projection requires all Invariant selections of the exact Fact Binding to be resolved and
legally observable. It preserves completed duplicates. Independent sealed Candidates may remain visible despite an
unrelated completion failure under ADR-0071. If one required selection is incomplete, the other completed Candidates
may be observed where independently legal, but the whole selection cannot be exposed as complete or consumed for Joint
Satisfiability. These are three logical completeness scopes, not additional Candidate kinds or mandatory physical Seal
stages. Recovery material, failed publication and mixed generations follow the common HIR laws; an older generation
cannot silently stand in for the current complete result.

Definition Establishment requires legal HIR observations and the authoritative Version prerequisite under ADR-0063
and ADR-0053. The complete Definition Candidate Projection, or producer-defined fine-grained observations with the
required evidence, supplies every observation necessary for entry and judgment. Missing interpretation cannot be
recovered by Establishment through source, Type Subsystem, host behavior or layout. A failed prerequisite retains
its owning result; if the Seal judgment does not enter, no additional Seal rejection is invented. An actual failed
Seal judgment belongs to HIR. Neither is an Invariant Definition Refusal. Section 19.8 defines that result.

The common independent conformance seam of ADR-0071 Section 19.1 applies to these observations. In particular, an
implementation must preserve declared requirement multiplicity, independent condition relations, the complete
Fact-scoped selection and its distinction from absence across clean, optimized and reused formation. Non-semantic
source changes or physical relocation cannot change these observations; incomplete, stale or conflicting material
cannot acquire visibility by representation alone.

### 19.8. Invariant Definition Establishment Judgment and Result

The Invariant Definition Judgment applies its own Definition laws to one complete resolved Candidate. It does not
require an IDL Binding Candidate, a selected Fact, a complete Interface selection or an actual Fact Candidate.
This independence does not authorize acquisition of unselected source or grant authority to a runtime value.

Each contributing judgment remains the responsibility of its owner. Declaration resolution, Type interpretation,
SDK and Source Qualification, HIR Seal and Version Establishment supply their legally consumable results under
ADR-0075. Invariant consumes those results and the completed meaning rather than performing or re-establishing those
judgments itself. It judges its own obligations, including the flat integrity law, the declared factual requirements
and the completion obligations of Sections 5 and 13–15 to the extent assigned to Invariant. Legal consumption neither
transfers authority nor requires a second qualification or verification by Invariant. The owners of analysis results
likewise retain responsibility for the correctness of their conclusions.

Definition-local Satisfiability is an obligation of successful Definition Establishment (Section 5.4). The owning
judgment consumes the qualified analysis conclusion for the exact law and domain. It must not require an established
SAT conclusion merely to enter and thus prevent judgment of a proven contradiction. Analysis may be prepared before
entry, evaluated on demand or reused when valid; its physical placement does not change ownership. SAT alone does
not establish the Definition.

The two Invariant-owned Definition results are:

```text
complete resolved Candidate
+ satisfaction of the Invariant Definition law
    -> Established Invariant Definition

complete resolved Candidate
+ established violation of the Invariant Definition law
    -> Invariant Definition Refusal
```

Both require legal judgment entry. An inconclusive required analysis prevents successful Establishment but does not
establish a Definition Refusal. If another owning-law violation is independently established in a lawfully entered
judgment, analysis uncertainty does not erase that violation. Failed prerequisites prevent the dependent judgment's
entry where required and retain their owners' attribution. Source interpretation, qualification, Seal and Version
failures are not Invariant Definition Refusal. Compiler interruption or indeterminate realization cannot fabricate
an authoritative result (ADR-0063 Section 4.7.2 and ADR-0074).

The entire Definition is the minimum successful establishment unit. Independent conditions may be analyzed separately,
but no subset acquires Definition authority. Failure creates no partial authority. Independent Established Definitions
and authoritative Version bindings are not revoked by a failure elsewhere. Establishment, legal visibility and durable
Version-history publication remain distinct under ADR-0063 Sections 6.9 and 10.8. A subsequent Composition or World
failure does not undo a Definition established in the current compilation, and retained material from unsuccessful
work is not current authority merely because it remains stored. These laws prescribe no physical transaction or
rollback mechanism.

**Definition-Time Required Basis and Applicability**

The following categories of ADR-0063 are `NOT-APPLICABLE` to Invariant Definition Establishment:

- Required Basis Law, Basis Resolution, Basis Binding, Applicable Basis and Complete Basis.
- Separate Definition Applicability and arbitration.

The Definition requires no Established Fact, Fact Definition or other Invariant as Contract Required Basis. Compiler
interpretation and analysis dependencies alone do not create such a requirement. The Version prerequisite follows
ADR-0053 and ADR-0063; it is not an additional Invariant-owned Basis Requirement. The separate application judgment
in Section 19.9 has its own requirements.

### 19.9. Fact-Scoped Composition, Required Basis and Applicability

#### 19.9.1. Selection Legality and Composition Ownership

Interface-scoped Composition owns the authoritative application relation. Fact owns its factual meaning, and
Invariant owns its Observation Requirement and Integrity Judgment laws. Neither Definition Authority absorbs the
other, and CRI prepares exact legal observations rather than owning the selection or judgment (ADR-0077).

The minimum complete scope for the standing-law Composition is the whole Invariant selection of one exact Fact
Binding, not a single Invariant or all Facts of the interface. Individual or pairwise satisfiability cannot establish
Joint Satisfiability of that scope (Section 19.3). Interface-wide Composition and World publication retain their
separate obligations.

Selection legality consumes the complete HIR relation of Sections 19.5–19.7. Repetition of the same exact Versioned
Invariant Definition for that Fact is a selection violation regardless of spelling or physical reference encoding.
The check may establish that violation before the selected Definitions have been established. It does not remove the
repeated occurrences from HIR. Equal meanings of independently identified Definitions are not duplicates. Redundancy
among distinct selected laws follows Section 5.4. Successful selection legality alone establishes neither an
authoritative application nor a new Definition.

#### 19.9.2. Higher-Scope Required Basis and Entry

For a nonempty, complete selection, the Fact-scoped Composition judgment requires one exact Established Fact Definition
and the exact Established Invariant Definition targeted by each selection. These requirements belong to the dependent
Composition under ADR-0063 Section 8, not to either independent Definition. The exact HIR selection is the relation
being judged; it is not duplicated as another Required Basis.

```text
requirement coordinate
    selectedFactDefinition

required meaning
    one Established Fact Definition targeted by the exact Fact Binding

cardinality
    exactly one

permitted absence
    no
```

For each exact Invariant selection in that scope:

```text
requirement coordinate
    selectedInvariantDefinition (for that exact selection)

required meaning
    one Established Invariant Definition targeted by that selection

cardinality
    exactly one

permitted absence
    no
```

A requirement is identified within the exact dependent Composition by its coordinate and, where applicable, its
selection relation. Two Compositions may consume the same Definition without making their required relations identical.
These logical coordinates require no independent global identifier or record family. If no Invariant is selected,
there is no Invariant application or Invariant-specific Composition Basis to resolve. The Fact vocabulary and its
other Composition obligations still apply.

**Basis Resolution and Binding**

The legal source domain of each requirement is fixed by the exact selection and its target Authority and Version.
Resolution obtains the corresponding Established Definition under its producer-owned protocol. A Candidate, another
Version, a similarly shaped Definition or an older result without current validity cannot satisfy the requirement.
A legal Binding establishes the exact required relation; finding a source alone does not establish that relation.
There is no alternative Definition selection or arbitration. CRI may prepare the relation and its required
observations under ADR-0077. It cannot establish the application or repair a failed binding by substitution.

**Applicable Basis and Complete Basis**

Applicability under ADR-0063 Section 7 concerns whether an already lawfully bound Established Definition can be
consumed for the exact dependent Composition. No additional Applicable Context is required. State, Operation,
time, other Facts or ambient Policy are not additional inputs. The exact selection and governing World determined by
their owners remain binding; current generation and reuse validity follow common compiler laws. Applicability does
not judge whether the Fact provides compatible factual observations. That is an owning Composition obligation.

Entry to authoritative Composition requires a complete legal selection and a Complete Basis: an Applicable Basis
for the exact Fact Definition and one for every required Invariant selection. A missing, unavailable, unestablished
or inapplicable required source prevents entry. Incomplete selection cannot establish this condition. A legal
selection check may independently establish its own result before these prerequisites are available. Complete Basis
is a semantic condition, not a mandatory materialization of another record.

#### 19.9.3. Observation Compatibility and Joint Satisfiability

Composition consumes the complete factual observation requirements of each selected Invariant and the corresponding
Fact-owned offered meanings under their established semantic protocols. Every required name must correspond to an
exact direct factual coordinate, and that coordinate must lawfully supply all declared requirements for the name
(Section 5.2). Compatibility follows the applicable Type, Value and Composition laws. The owning producers' results
are consumed without reinterpreting the values or transferring their authority. A lawful observation must preserve
factual information; an implicit conversion or a guard supplied by another independent condition cannot repair a
missing or incompatible requirement.

Compatibility must hold for every complete value permitted by the selected Fact Definition, before the standing
integrity law restricts which candidates may be established. Joint Satisfiability, by contrast, requires the existence
of at least one such complete value satisfying the entire selection under Section 19.3. A satisfying witness cannot
replace universal compatibility or silently narrow the offered factual domain. For example, a nullable factual
coordinate cannot supply a non-null requirement merely because a non-null witness satisfies the integrity law.

Both compatibility and established Joint Satisfiability are obligations of successful Composition. Analysis may
occur earlier when legal meaning is available, but only conclusions valid for the exact current Established Definitions
and complete selection can support authority. Failure of compatibility or an established joint contradiction belongs
to the Composition judgment, not Basis Applicability or the independent Definition judgments.

#### 19.9.4. Application Establishment and Result Boundary

The complete Fact-scoped Composition must succeed before its selected laws acquire authoritative standing application
to that Fact. Individual selection legality, compatibility or partial verification may be established or reused
without granting that authority. Success preserves each exact Fact–Invariant application relation and the independent
identity of every selected Definition. It does not introduce a Composite Invariant Definition, separate Contract
Version or new Contract Authority.

An established violation of selection law belongs to the selection judgment. Failed Basis prerequisites retain their
own attribution and prevent the dependent Composition judgment's entry. A lawfully entered Composition establishes
its own violation where observation requirements cannot be met or the joint law is proved unsatisfiable. An inconclusive
required analysis prevents application establishment without proving that the composition is unlawful. No partial
standing application may be published as a complete one when the Composition fails. Independently established
Definitions are unaffected. Common Result and World laws govern the exact result form and legal publication.
Established Invariant Definition and application observations follow Sections 19.10–19.12. The exact common
reference and protocol form of the Interface-owned application relation remains with its owning Composition law (Section
22).

An actual integrity judgment uses the complete canonical candidate Fact and the applicable standing meaning
established by that Composition (Section 16). The candidate is its judgment input, not an Established Fact required
as a prior Basis. No independent runtime Occurrence Basis Requirement Law, formation-path-specific Lowering or
Operation Basis, or additional runtime Applicable Context is introduced. The applicable World and exact relations
must still be valid for the judgment. This distinction requires neither a new runtime Basis resolution nor a repeated
Composition check for each candidate. The actual judgment and the boundary-local result consumption follow Section 21;
no independent Established Invariant Occurrence family is introduced for this Fact-local law (Section 19.10).

#### 19.9.5. Analysis Preservation and Reuse

Selection legality, requirement correspondence, compatibility and joint analysis have distinct observation scopes.
An unchanged requirement may permit reuse of compatibility computation even when the complete Integrity Judgment Law
changes. Joint analysis must account for the complete selection and exact factual domain, including any unobserved
coordinates or value relations needed to establish the existence of a complete lawful witness. Equality of only the
coordinates used by a condition cannot establish that domain's continued satisfiability.

For an unchanged domain and retained laws, removal of a condition may preserve an existing SAT witness; addition
requires checking it against the new obligations. A verified contradiction may survive an added condition, but removal
requires evidence that the contradiction still follows from the remaining laws. Changes to the domain or meaning
require current validity to be established. Incremental solver state and proof or witness reuse remain compiler-owned
under Section 5.4.4. They must not acquire authority from retained assertions, worker completion order or a stale
context. Sharing analysis or runtime computation does not merge the authoritative application relations.

These obligations permit static specialization and direct coordinate access without mandatory runtime lookup,
separate analysis records or a particular solver. The compiler's exact current observations, result attribution and
preservation laws remain the criteria for reuse.

### 19.10. Established Invariant Definition Material

Successful Definition Establishment produces one complete Established Invariant Definition with two components:

**Identity**

The `EstablishedInvariantDefinitionRef` identifies the Definition under its Owning Invariant Authority Binding and
Version Binding.

**Meaning**

Preserves the complete Integrity Judgment Law and Factual Observation Requirement Law of Section 19.1, including
the direct established semantic relations needed to observe that meaning. Separately owned meanings remain with
their producers and may be observed through exact references. Establishment does not introduce another constraint,
a Fact association or an actual integrity judgment into the Definition.

Invariant owns one Established reference domain:

```text
EstablishedInvariantDefinitionRef
```

One Authority owns one independently addressable Definition per Version (Section 19.2). No Authority-Local Definition
Coordinate is required. The Owning Authority Binding is directly recoverable without inferring it from the enclosing
interface, source declaration or physical containment. Identity conflicts and Version history follow ADR-0063
Section 6 and ADR-0053.

A nominal requirement coordinate selects the complete group of declared requirements within one exact
`EstablishedInvariantDefinitionRef`. As in Section 19.5, this is a Definition-local semantic selection rather than
another Reference Domain or independent authority. Conditions and helpers likewise have no independent Established
Reference Domain. Their meaning and relevant declaration relations remain observable through the complete law.

The Established Reference becomes observable only after the complete Definition and its required identity relations
are coherently established. A Candidate Reference cannot be promoted or retyped into an Established Reference.
Distinct Candidates may correspond to the same established identity and meaning through lawful Establishment or
currently qualified reuse. Established meaning must be interpretable without retaining the originating HIR or source.
Shared immutable backing, relocation and reclamation remain permitted under the common observation and validity laws;
sharing representation transfers no authority.

Interface-scoped Composition owns the standing application relations established under Section 19.9.4. The Definition
need not contain its consumers or a reverse relation to every selected Fact. Neither a separate Composite Invariant
Definition nor an Invariant-owned Established Binding family is introduced.

**Occurrence Boundary**

An actual application of an Established Invariant Definition is a distinct semantic judgment under ADR-0063
Sections 4.5–4.6 and Section 21 of this ADR. For the Fact-local law, the owning judgment result is consumed through
the applicable processing and establishment relations. No independently addressable Established Invariant Occurrence
Material, `EstablishedInvariantOccurrenceRef`, Occurrence Projection or independent Occurrence history is required.
The current law identifies no consumer that requires such an independently addressable authority material. The
Occurrence Material family and its independent reference protocol are NOT-APPLICABLE to this design; the actual
application, its authoritative judgment and its attribution are not.

This boundary does not permit the result to disappear before its legal consumption. The meaning required by Fact
Establishment, Failure and other authorized consumers must remain available for their applicable processing lifetime
(Section 21). It prescribes neither an occurrence object nor a particular storage, transfer or retention mechanism.
An extension beyond Fact-local judgment would require its own assessment of this boundary (Section 22).

### 19.11. Established Invariant Semantic Protocol

Invariant specializes the read-only Established Semantic Protocol of ADR-0063 Section 11 using the reference domain
of Section 19.10:

```text
Established Invariant Definition Projection
Direct Established Relation Projection

Fine-Grained Established Invariant Projections
    Established Invariant Factual Observation Requirement Surface Projection
    Established Invariant Selected Factual Observation Requirement Meaning Projection
```

**Established Invariant Definition Projection**

Exposes one exact `EstablishedInvariantDefinitionRef` and the entire Definition of Section 19.10. The observation
includes Authority, Version, both complete determinants and their required direct established relations. Consumers
obtain the judgment meaning without reinterpreting HIR, source or Host Binding. Proofs, witnesses, test products
and executable realizations remain products of their respective owners, not additional Definition determinants.

**Established Invariant Factual Observation Requirement Surface Projection**

Exposes the complete nominal coordinate set required by one exact Established Definition. Each name appears once
at this surface, as in Section 19.6. The observation is complete for requirement coverage without exposing every
requirement's value meaning. An empty complete surface establishes that no nominal observations are required;
it is distinct from unavailable or incomplete meaning. Equality preserves the exposed Definition and nominal set
independently of source order. Multiplicity and the meaning of individual declarations belong to the selected
requirement observation.

**Established Invariant Selected Factual Observation Requirement Meaning Projection**

Exposes the complete group of declared requirements selected by an exact Established Definition and nominal coordinate.
It preserves each resolved required meaning, declaration multiplicity and relation to the declaring condition.
Sections 5.2 and 19.6 govern the distinction between complete declared requirements and narrower computation
observations. Different requirements for a name cannot be replaced by a common inferred type. Equality preserves
the exposed identity and the entire selected meaning; one such observation cannot establish the absence of others.

**Direct Established Relation Projection**

The common ADR-0063 Section 11.4 projection observes the source, relation and target of a direct relation already
established as part of the Definition. It creates no inferred relation, reverse ownership or transitive graph.
Fact-scoped applications retain their Interface and Composition owners. The HIR-only Binding Candidate and Selection
Projections of Section 19.6 are not Established Invariant Projections.

The following example observes `NonNegativeBalance` after lawful Definition Establishment under Section 19.8.
`V1` identifies its authoritative Version Binding and `E` denotes an exact Established Reference. The notation
shows logical observations, not the encoding of the Definition or an executable instruction vocabulary.

```text
Invariant Definition Establishment
    complete sealed NonNegativeBalance Candidate D
    authoritative Version Binding V1
    legal Invariant Definition meaning
        -> Established Invariant Definition

Established Invariant Definition Projection
    EstablishedInvariantDefinitionRef E
    Owning Invariant Authority Binding NonNegativeBalance
    Version Binding V1
    Integrity Judgment Law
        amountMinor >= 0L
    Factual Observation Requirement Law
        amountMinor -> complete signed 64-bit integer meaning

Established Invariant Factual Observation Requirement Surface Projection
    Definition E
    amountMinor

Established Invariant Selected Factual Observation Requirement Meaning Projection
    Definition E
    coordinate amountMinor
    complete signed 64-bit integer meaning
    relation to the declaring condition
```

The example retains the established meaning of the original declaration. It does not establish an application to
`Balance`, create actual factual information or grant execution authority. Required direct relations are available
under the common protocol even when their detail is not illustrated.

**Observation Equality and Reuse**

Equality of a complete projection preserves all exposed identity and meaning. Equality of the nominal set or
required value meaning is a narrower comparison: two different Versioned Definitions can have equal requirement
meaning without their complete projections or authoritative references being equal. Calculations depending on the
narrower meaning may be reused under current-validity law. Rebinding the result to a current application requires
its own legal correspondence; the narrower comparison does not establish that correspondence.

Consumers judging complete coverage must obtain the whole requirement surface and all required meanings. Validity
of previously observed requirements alone cannot establish that the current surface is unchanged. Coherence,
availability and legal consumption follow ADR-0063 Sections 11.5–11.7 and ADR-0075. Narrow omissions, invalid
references or unavailable backing cannot establish absence. Projection boundaries prescribe neither separate
allocations nor mandatory runtime calls, hashing or Query nodes. Bulk access and qualified shared backing may
supply the same observations.

**Standing Application Observation**

A consumer requiring standing application obtains the relation established by the Interface-scoped Composition under
Section 19.9.4. The owning protocol must provide the exact authoritative selecting context, the exact selected Fact
and its Established Definition, and the complete applicable Invariant relations to their Established Definitions.
This is a requirement on the meaning supplied by that owner, not a new Invariant-owned Projection or reference type.

An individual relation does not establish the completeness of the application scope. Execution Formation and any
consumer claiming complete integrity coverage require the current complete scope for that Fact. A newly selected
Invariant cannot be omitted merely because all previously observed relations remain valid. A completed application
scope with no selected Invariants is distinguishable from partial, failed or unavailable Composition; an empty lookup
cannot establish it. No artificial always-true Invariant or Established Material for an absent selection is created.
The exact common form of the owning protocol remains outside this ADR (Section 22).

### 19.12. Canonical Contract World Placement

The Canonical Contract World (ADR-0063 Section 10) exposes complete Established Invariant Definitions through
Section 19.11. Each Definition's identity, meaning and required direct relations must be coherent. Established
application relations are observed under their Interface and Composition owners. HIR Candidates, runtime candidate
Facts, actual integrity results and occurrence-specific information do not belong to the Definition-world substrate.
Provenance and compiler products, including proof and execution products, retain their adjacent ownership.

Visibility does not establish new authority. The compiler's World is distinct from the Policy-owned Contract World;
co-location establishes neither Policy membership nor a Fact–Invariant application. Assembly, linking and changes
of representation preserve existing authority and meaning. World generation, physical location and aggregate identity
are not Definition identity or actual application identity.

Independent complete Definitions may become visible without unrelated Composition work when the consumer's closure
is coherent (ADR-0063 Section 10.7). Partial World coverage may be lawful; partial Definition or standing application
meaning is not. Consumers that require a complete application must not enter using an incomplete scope. World
formation or observation failure retains compiler attribution, and independent Definition Establishment is preserved.
Previous backing remains usable only within its valid scope or after current qualification, not as a substitute for
failed publication of a new World. Storage, linking and reclamation remain replaceable.

---

## 20. Compiler-Derived Realization

From the same canonical Invariant material, Kontrakt may derive and optimize the realization of the standing law.
This includes applicable-law selection by exact Fact Kind, direct coordinate dependency selection and specialized
runtime gates. Calculation may use primitive comparisons, constant folding, shared coordinate reads and static
discharge where stronger canonical material proves the law.

The same material may support generated valid, boundary and violating fixtures, property-based tests and shrinking
guidance. Refusal and diagnostic attribution, as well as deterministic backend-specific realization, are also
compiler-derived products. None of these becomes another authored Invariant.

This Contract neither requires nor authorizes a cross-Fact collection scan, group index, aggregate counter, interval
structure, history traversal or graph algorithm. Fact-local calculations under Sections 9 and 13 may use
compiler-derived realization without acquiring any additional authority. Backend machinery used for Fact storage or
Sameness belongs to those separate concerns and may not enlarge Invariant meaning.

### 20.1. Judgment Preservation and Static Discharge

Compiler-derived execution must preserve the Contract-observable meaning of the original qualified law. Equality of
the final Boolean alone is insufficient. In particular, a transformation must preserve the legal evaluation domain
and completion meaning, the declared requirements and mandatory observations, and each independent judgment's
result and attribution. The compiler need not reproduce every physical operation or execution trace.

A source guard may make a partial operation legal only within its applicable evaluation domain (Sections 5.3 and
14). Speculation, reordering or vectorization must not introduce an exception, an invalid operation or an altered
numeric meaning that the qualified source did not admit. Within one Invariant, a computation may be omitted when
the complete judgment and required observations are already established without it. This cannot excuse incomplete
source qualification, remove a declared requirement or suppress evidence required by its owning law. Across separate
Invariant applications, the completion obligation of Section 21 remains in force even when a common kernel realizes
the judgments.

Definition-local and Joint Satisfiability establish existence, not that every actual candidate satisfies the law.
Static discharge requires a sound justification for the judgment of the exact application over its applicable
candidate domain. The compiler may use qualified formation or realization knowledge for that justification without
making the Invariant Definition depend on a particular Lowering or Operation. The resulting realization preserves
the applicable obligation and its exact result even when no separate runtime comparison is needed.

Fusion, batching, vectorization and compaction must preserve the legal evaluation domain and the correspondence
between each actual application and its result. A masked or inactive lane must not introduce an inadmissible
operation or an additional judgment. Masking only a result is insufficient when the earlier operation is not legal
in that lane. Physical ordering and lane placement cannot determine Failure membership or attribution. The applicable
platform qualification and backend laws determine the legality of the chosen realization.

### 20.2. Current Validity, Reuse and Security

The reusable computations of Sections 5.4, 19.6, 19.9 and 19.11 have distinct determining observations. Definition
analysis, requirement compatibility, joint analysis and executable specialization may share eligible calculations
without sharing authority or actual application identity. A consumer of complete coverage must retain current
closure of the required surface or application scope, not merely the validity of known members. Reuse of a proof,
witness or discharge requires its current justification, including the assumptions that make an omitted change
irrelevant. The compiler's common Result and preservation laws govern these decisions.

Physical sharing, indexing and relocation must preserve the exact reference and validity context. An address,
dense ordinal or digest match cannot substitute for exact meaning or authority; collisions must not merge distinct
meanings. Direct access through a currently qualified observation may avoid repeated validation within that valid
scope. A stale handle, corrupted backing or poisoned cache cannot supply authoritative meaning. No particular
cache key, hashing schedule, storage layout or Query granularity is required.

The correspondence of Section 16 must survive copying, shared backing, publication, reclamation and any delayed
physical materialization. Equality of inputs may support shared calculation but cannot transfer a previous actual
judgment's attribution. Reclamation must respect the legal consumption lifetime of the results in Section 21.
Physical representation and reuse must preserve trust-boundary isolation; equal meaning grants no permission to
share across scopes. Internal keys, timing, addresses and cache observations acquire no automatic outward or
diagnostic authority. Disclosure remains subject to the owning Publication and diagnostic laws.

Resource safety is distinct from exact semantic comparison. Analysis and verification work must respect their
compiler-owned resource governance. Runtime Budget and Capacity retain their own accounting and result laws.
Optimization may change physical resource use but must not change the authority or meaning of those obligations.
A resource failure, incomplete analysis or interrupted realization must not manufacture an Invariant refusal or
success. The common security and platform laws govern the protection and qualified execution of these products;
Invariant consumes their results rather than re-establishing their judgments.

### 20.3. Compiler Conformance and Independent Verification

Conformance extends across the entire realization of Invariant. The applicable verification owners must be able to
check preservation at the following seams:

- Source interpretation and qualification: the exact value domain, computation, evaluation and completion obligations
  of Sections 5 and 13–15.
- Resolved HIR and its protocol: complete determinants, declaration relations, Selection multiplicity, reference
  validity, Seal and visibility under Sections 19.1–19.7.
- Definition Establishment and Composition: exact Version, owned results, required Basis, universal compatibility
  and qualified satisfiability evidence under Sections 19.8–19.9.
- Established material, protocol and World: complete observations, identity, authority and application closure under
  Sections 19.10–19.12.
- Execution Formation, optimization and backend: every applicable reachable judgment, actual application attribution,
  Failure membership and judgment–use correspondence under Sections 16 and 20–21.

Verification must support both comparison with the deterministic clean path and independent checking against the
Invariant Contract (ADR-0071 and ADR-0075). Agreement between two defective implementations cannot establish
conformance. Verification products do not create Invariant or Fact authority, and independent checking does not
require duplicate computation during every governed execution. The comparison must distinguish an inconclusive or
interrupted compiler computation from an established semantic result (Section 5.4.4).

Analysis encoding, evidence and transformation conclusions require qualified verification of their claimed scope.
An independently checked witness or a sound proof must concern the actual legal domain. An incomplete proof or
bounded test is not a proof of an unrestricted conclusion. Local, incremental or grouped transformation verification
may establish preservation when the verified scope and its surrounding relations justify the full claimed result.
Verification at one IR seam does not establish correctness of an unchecked later backend transformation. The actual
emitted executable must realize the qualified meaning; structural host verification alone is insufficient to establish
Contract conformance.

Conformance includes negative cases: missing requirements and newly selected laws, wrong-kind or stale references,
partial publication, corrupted or misbound evidence, guard violations, omitted independent judgments and attribution
loss during packed or parallel evaluation. Collision handling, resource failure and isolation obligations apply to
clean and optimized paths alike. Compiler and runtime failure, absence, non-entry and Contract refusal retain their
own meanings and owners.

Semantic correctness, security conformity and performance are distinct verification concerns. A correct result does
not prove resource safety, isolation or efficiency. Compiler Design owns the verification strategy, implementation
choices and performance measurements. V1 and V2 may use different representations and algorithms while preserving
Contract meaning, current validity and applicable security obligations.

---

## 21. Result and Attribution

Each application of a selected Invariant produces one standing-law judgment for one proposed Fact. Its outcome is
visible to the machine and attributable to the exact named Invariant and its application to that Fact. Independent
condition methods and helpers do not acquire separate Contract judgment authority, nor do the declaration class,
source methods, receiver object or generated JVM mechanics receive runtime Contract authority.

### 21.1. Integrity Judgment Results

A lawfully completed application establishes one of two results:

| Result    | Meaning                                                                                          |
|-----------|--------------------------------------------------------------------------------------------------|
| Preserved | The complete integrity law holds for the exact candidate under the applicable standing relation. |
| Refused   | The complete candidate exists but that applicable integrity law does not hold.                   |

`Preserved` grants neither Fact authority nor Operation participation. `Refused` establishes no authority to change the
candidate or judge another Contract's law. An unsatisfied prerequisite, unresolved source meaning or indeterminate
realization does not establish either result merely by preventing judgment (ADR-0063 and ADR-0074). Where no
Invariant applies, no such judgment occurs; there is no synthetic successful application.

### 21.2. Completion and Exact Attribution

Every applicable and reachable independent Invariant judgment in the active processing boundary must complete under
ADR-0057 Section 4.3.3. One refusal cannot suppress another judgment that does not require the failed meaning.
Independent conditions within one Invariant compose a single judgment; they do not establish separate Contract
results. Section 20.1 governs lawful computation elimination within and across those judgments.

Each established result retains the exact applied Versioned Invariant Definition, standing application relation,
actual candidate attribution and applicable processing boundary. The exact distinguishing relation is supplied by
the existing Formation and Composition laws; no new Fact identity, global Occurrence ID or independent Result
schema is introduced. The result belongs to that application even when its calculation is shared or statically
discharged. Independent actual applications are not collapsed by equal information; repeated physical execution of
one judgment does not create another application.

A packed or sparse result representation may avoid materializing successful results separately. An empty refusal
representation denotes complete success only when every required judgment in its exact scope is established as
successful. Non-entry, unperformed work and interrupted or uncertain execution cannot be treated as success.
Failure membership and result attribution must remain independent of discovery or worker completion order, subject
to the existing canonical processing and accounting laws.

### 21.3. Failure and Legal Consumption

Each exact `Refused` judgment establishes one semantic Failure under ADR-0057 Section 4.3.2. Several false conditions
within one Invariant do not create several Failures; separate independent refused applications retain their distinct
Failures. Non-entry does not establish a Failure attributed to Invariant. Realization failures and interruptions
retain the attribution assigned by their own laws; Invariant does not reconstruct them as a false judgment.

Results must remain legally available for the applicable establishment and Failure processing and for any other
consumer authorized to observe them. A successful result is not reclassified when another judgment prevents the
whole proposal's establishment. Reclamation or disposal of unestablished candidate backing must not destroy the
result meaning still needed by authorized consumers. This obligation concerns valid processing and handoff, not
mandatory independent Occurrence Material or indefinite retention. ADR-0057 Sections 4.3.4–4.3.7 govern finality,
availability, boundary completion and Crash.

Failure consumes the source-established meaning using its own source, context and boundary law. Diagnostic Evidence,
Retention and Publication retain their own responsibilities for additional observations, their lifetime and disclosure.
An independent condition, helper or failed diagnostic computation cannot create an additional Invariant Failure.
A diagnostic must not present an unobserved condition as an observed failure, or describe an earlier result using
an unrelated later Definition. The applicable diagnostic law determines whether more detailed observations, such as
a complete list of violating collection members, must be supplied. A physical discovery order cannot establish an
otherwise undeclared semantic ordering or Failure priority.

The exact result and its legal handoff require neither a specific result container nor independent durable history.

Invariant refusal is distinct from malformed Input, Admission rejection, Canonicalization refusal, Lowering refusal,
State movement
refusal, Operation realization failure, Publication decision, and Output realization failure.

---

## 22. Open in This Section

The final Java and Kotlin token spelling and detailed authoring qualification of admitted Invariant expressions may
evolve. The authoring boundary is fixed: one flat Contract with one complete integrity judgment, expressed through
required factual observations and closed computation. Every application is selected by the IDL and judged from one
complete canonical candidate Fact.

Sections 5 and 19 define the factual requirements, Definition meaning and identity, satisfiability, IDL selection,
Resolved HIR, Establishment and Fact-scoped Composition for the current Fact-local law. Sections 19.10–19.12 define
its Established Definition, reference and protocol, application observation and World placement. Sections 16 and
20–21 govern actual judgment, result consumption, attribution, preservation and compiler conformance. Independent
Established Occurrence Material is not required for that scope. The exact common Interface-owned Established
Application reference and protocol form, the shared Composition's further common realization details and the
applicable common Result and diagnostic specifications remain with their respective owners. No additional Material
family or Version API decision is introduced.

No future syntax may silently add another Fact, a population or history of separately established Facts, proposal,
Operation-specific material, hidden lookup, Fact mutation, escaping callback or backend evaluation state without a
new Contract decision.

The complete authoring surface for cross-Fact or operation-specific obligations remains outside the current law.
A separate design review is required for an explicit many-to-many association of Facts and Invariants in the
`facts : invariants` IDL block and for an authoring API that can refer to more than one exact Fact. That review must
determine the meaning of the association, how participating Facts and their observations are selected, and the
resulting judgment, applicability, establishment and occurrence boundaries. No syntax, Fact-reference semantics,
relation authority or cross-Fact execution is established by this open item. Sections 4, 10–11 and 19–21 continue
to describe the current Fact-local law until a new Contract decision changes its scope. Operation-specific
obligations remain outside that law.

---

## 23. Consequences

Invariant provides a narrow standing Fact integrity authority rather than a general validator framework. The same law
may be selected for different Fact Kinds, and it applies automatically to every proposed establishment for which the
law is selected under the applicable Core binding.

Users write ordinary supported Java or Kotlin judgment code while the compiler owns semantic refinement and runtime
realization. The one-Fact rule prevents Invariant from becoming an implicit query engine over the Core: cross-Fact
aggregation, history, population, repository lookup and graph traversal remain outside its authority, even behind a
Boolean return type. Fact-local aggregate and structured-value computations remain within the closed judgment
boundary. State and Transition retain their independent movement authorities.

The closed Invariant surface gives the compiler opportunities for static discharge, primitive specialization,
generated tests and direct diagnostic attribution.

---

## 24. Migration History

This ADR was extracted from the Invariant-owned material of ADR-0049, preserving its latest Accepted Invariant law
as of the 2026-08-05 revision. The extraction did not intentionally introduce a new Invariant authority or change the
accepted Invariant meaning. ADR-0049 remains the owner of the shared Change Proposal and indivisible-establishment
relation in which Invariant participates.