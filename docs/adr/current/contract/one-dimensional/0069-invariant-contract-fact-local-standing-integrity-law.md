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
The existing selection and Composition laws govern its resolution and judgment. No name, type resemblance, package
placement, inheritance, annotation, or runtime discovery adds an Invariant implicitly.

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

The Invariant Definition contains the required observations and the integrity law, not the particular Fact Definition
used during source interpretation or selected in the IDL. Source coordinates for attribution remain distinct from the
law's meaning. The same Definition may therefore be selected for different Facts under Section 4.

A legal application must supply every required observation from the same complete canonical candidate Fact. A matching
name alone does not establish compatibility. The applicable Fact law, resolved value meaning and existing Composition
law determine whether the observation is legal. At definition time, the required information about the selected Fact
is obtained through its producer-owned semantic protocol (ADR-0068 Sections 19.5 and 19.11), not by reconstructing
Fact meaning inside Invariant. Kontrakt must not supply missing information, infer a coordinate relation or implicitly
convert or normalize the Fact to make the judgment applicable (ADR-0068 Sections 5.1 and 6).

Every explicitly declared observation parameter of an independent condition remains a requirement, even when its
value does not contribute to the judgment. The declaration therefore still requires that observation from the
selected Fact. Section 5.4 governs the diagnostic for an unused requirement; removing a physical read or calculation
must not remove the declared requirement.

A condition may observe only part of the factual material. This does not permit an incomplete candidate to enter
judgment. Nested observations retain the meaning of constituents owned by that Fact, and no host carrier or reference
to the whole Core becomes an additional operand. Judgment and establishment remain subject to Sections 10 and 16.

### 5.3. Independent Conditions and Authored Evaluation

Within a condition, the exact Source operation determines evaluation. A short-circuit guard can establish the domain of
a later partial operation; an eager conjunction does not establish the same evaluation dependency. Separate independent
conditions, or separate Invariants selected on the same Fact row, do not establish implicit guards for one another.
Each must satisfy the applicable completion law. A compiler may optimize their realization only under Section 14.

### 5.4. Integrity Law Legality and Declaration Diagnostics

The complete integrity law of an Invariant must be satisfiable over its required observation domain. A law whose
unsatisfiability is established is inadmissible and must produce a compilation error. This includes an explicitly
false law, a contradiction within one condition and a contradiction between independent conditions. The same rule
applies to the combined integrity obligations selected for a Fact (Section 19). Failure to establish satisfiability
or unsatisfiability must not be reported as proof of a contradiction.

Kontrakt must provide compile-time diagnostics for established semantic redundancy, subsumption, tautologies and
other unnecessary or ineffective declarations. An explicitly true law imposes no integrity restriction; the compiler
must explain this and guide the author toward removing or correcting it. Unused declared observations are diagnosed
without weakening the requirements of Section 5.2.

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

The exact selected Fact association and its observation requirements are judged under the existing Composition law.
Relation information is acquired and prepared under ADR-0077, using the exact producer-owned semantic observations
of the selected Fact (Section 5.2). CRI neither selects the law nor owns the judgment of its applicability or
integrity. Kontrakt derives stable Fact-kind and coordinate dependency indexes from the resolved selections and
requirements without giving these indexes Contract authority.

All Invariants selected for the same Fact must be jointly satisfiable over the factual
meaning of that Fact. If the combined obligations are established to be unsatisfiable, the application is inadmissible
and must produce a compilation error, even when every selected Definition is independently lawful. For example,
selecting one law requiring `amount > 0L` and another requiring `amount < 0L` for the same `amount` coordinate of one
Fact makes their joint application impossible. This does not make either independent Definition inadmissible in
isolation.

The Definition legality and diagnostic obligations of Section 5.4 also govern analysis of the combined obligations:
established contradictions require errors, while established redundancy or subsumption must be explained without
silently removing a selected Contract. A conclusion must be based on the exact selected laws and factual meaning,
not on incomplete information or a failed analysis. The precise judgment and result boundaries follow the existing
owning laws; their detailed Invariant specification remains open (Section 22).

The common Resolved Contract HIR and Seal law (ADR-0071) and Contract Establishment law (ADR-0063) apply. A complete
resolved Candidate is not itself established authority. Only established meaning may become part of the Canonical
Contract World under the applicable owning law. Legal consumption follows ADR-0075. Host source mechanics must be
erased before authority begins. The detailed Invariant HIR, Establishment and established semantic observations remain
open (Section 22).

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

---

## 21. Result and Attribution

Each application of a selected Invariant produces one standing-law judgment for one proposed Fact. Its outcome is
visible to the machine and attributable to the exact named Invariant and its application to that Fact. Independent
condition methods and helpers do not acquire separate Contract judgment authority, nor do the declaration class,
source methods, receiver object or generated JVM mechanics receive runtime Contract authority.

Invariant refusal means complete candidate Fact material exists but an applicable standing law does not hold. An
unsatisfied prerequisite, unresolved source meaning or an indeterminate realization does not establish such a refusal
(ADR-0063 and ADR-0074).

It is distinct from malformed Input, Admission rejection, Canonicalization refusal, Lowering refusal, State movement
refusal, Operation realization failure, Publication decision, and Output realization failure.

---

## 22. Open in This Section

The final Java and Kotlin token spelling and detailed authoring qualification of admitted Invariant expressions may
evolve. The authoring boundary is fixed: one flat Contract with one complete integrity judgment, expressed through
required factual observations and closed computation. Every application is selected by the IDL and judged from one
complete canonical candidate Fact.

The detailed observation requirement cases not settled by Section 5, the Invariant-specific HIR and semantic protocol,
Definition Establishment, applicability and established material, and the required compiler conformance detail remain
open. The common laws continue to apply. No additional Material family or Version API decision is introduced.

No future syntax may silently add another Fact, a population or history of separately established Facts, proposal,
Operation-specific material, hidden lookup, Fact mutation, escaping callback or backend evaluation state without a
new Contract decision.

The complete authoring surface for cross-Fact or operation-specific obligations remains outside this ADR because this
ADR intentionally does not assign them an owner.

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
