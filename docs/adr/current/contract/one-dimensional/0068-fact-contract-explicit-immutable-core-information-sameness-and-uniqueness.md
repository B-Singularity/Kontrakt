# ADR-0068: Fact Contract, Explicit Immutable Core Information, Sameness, and Vocabulary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation

## Related

- `../../../../the-most-important-thing/what-contract-is.md`
- ADR-0069: Invariant Contract
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0079: Type Interpretation and Semantic Binding
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0074: Compiler Result, Explicit Unsuccessful Result, Recovery, and Observation Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Core Fact Establishment, Change Proposal, Bound Fact Authority, and Outward Handoff
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary

---

## 1. Context

Fact expresses the machine's own information. As described in `What Contract Is`, Section 12.1,
Operations perform work while Facts describe what the machine knows. Only lawfully established immutable
Facts belong to the Contract Core as factual Contract material (Sections 4–5). Transient realization
values may exist without acquiring that authority.

A Fact need not express a final domain value. It may describe a claim or observation without establishing
the truth of its content beyond the declared meaning. Its factual meaning belongs to the machine
independently of external material and its formation path.

---

## 2. Problem

Implementation mechanisms alone establish neither factual authority nor factual meaning. The machine
needs an explicit factual surface whose meaning survives representation replacement.

An exact sameness law must prevent repeated establishment of identical information from creating hidden
multiplicity. Occurrence distinctions, including count, provenance and repetition, must be explicit
whenever they determine factual meaning (Sections 10–12).

---

## 3. Decision Drivers

Fact must be the explicit immutable information of the Contract Core, not an implementation object
promoted to authority. Native host declarations provide source evidence without requiring proprietary
wrappers (Sections 6 and 9). Vocabulary selection is explicit in the IDL (Section 8).

The design separates information from relations and authority over its use (Sections 7 and 14–18). Exact
sameness preserves all declared distinctions independently of representation, including when compact
physical keys are used (Sections 10–13 and 20).

---

## 4. Fact Meaning

A Fact is an immutable proposition expressed by complete canonical factual material under one exact
Versioned Fact Definition.

**Fact Contract**

Declares what information may hold factual authority inside the Contract Core and requires its complete,
immutable expression (Section 6).

**Established Fact**

Information that holds authority under that Contract after lawful establishment (Section 5).

No Input, Output, Lowering or Operation declaration determines Fact meaning merely by supplying or
consuming its values. Fact carries no behavior and performs neither computation nor its own validation.
It owns neither movement decisions nor Publication authority. Kontrakt performs Fact-owned judgments
under the declared law.

Canonical factual material preserves every declared distinction (Section 6). Scope and Version remain
explicit under their owning laws. Canonical material is representation-independent; it requires neither a
universal runtime carrier nor a particular encoding. It does not apply Canonicalization to erase
distinctions. Physical representation artifacts, including addresses, padding and unused storage, are not
factual information (Sections 6.3 and 13).

---

## 5. Core Authority

Boundary material does not become Fact through Admission or Canonicalization. Lowering establishes a
complete Fact Candidate set under ADR-0067, not Fact authority. Result-side formation may likewise supply
proposed factual material under the applicable Operation and Change laws. The formation path introduces
no hidden factual distinction (Section 11).

Completed Definition meaning, complete candidate information and Established Fact are distinct. A
candidate must express the information required by its exact Definition before judgment; completeness
alone establishes neither legality nor authority.

### 5.1. Candidate Completion and Coherent Judgment

Formation belongs to its applicable owner. The candidate must satisfy Section 6.2 before judgment; Fact
must not complete missing information through implicit computation, lookup, defaults or normalization.

All required judgments and establishment must concern the same complete, stable information. No carrier
or alias may change it across that boundary. Subject to ADR-0073, formation may use temporary internal
mutation beforehand. Physical sharing and deferred materialization of already determined information are
permitted when they preserve exact observation; decisions about the information itself cannot be
deferred.

### 5.2. Lawful Establishment

Fact authority requires success of the separately owned judgments under the applicable Composition. Fact
judgments remain distinct from standing Invariant and movement judgments (ADR-0067 Section 17; ADR-0069
Section 16).

Where a complete candidate set or Change Proposal requires indivisible establishment, all required
judgments must succeed before any part acquires Fact authority. Establishment is then indivisible. This
law prescribes neither physical execution order nor a common establishment unit for independent
formations.

Refusal places none of the proposed material in the Core. Failed prerequisites prevent legal entry
without creating a Fact refusal. Neither success nor refusal may be invented when the exact result is
indeterminate. Result attribution and compiler failure follow ADR-0063 Section 4.7.2 and ADR-0074.

### 5.3. Continuity and Physical Restoration

An Established Fact retains its information and cannot be rewritten by subsequent processing. New
information must be expressed separately and lawfully established under the applicable change authority (Section 15).
Immutability requires neither permanent retention nor fixed physical representation.

Restoration, including bootstrap or loading existing backing, may make an Established Fact available
without repeating its original establishment. It creates no new Fact authority. It must preserve the
exact established meaning and authority under the applicable Version, World and validity laws. Physical
existence, equal payload or a matching digest alone is insufficient. Unavailable or damaged material must
not be silently completed or promoted to authority. Restoration mechanisms remain realization decisions (Section 21).

### 5.4. Declared Immutability and Realization Guarantee

The user declares immutable information through Section 9. Mutable Fact declarations and uses that change
factual information are not admitted. Host syntax expresses this intent but does not prove immutability.
Kontrakt must construct and verify a machine that actually satisfies the Fact law.

Definition Establishment judges the declared meaning, not execution conformity. Compiler realization must
fulfill Sections 5–7 and 10–13 in governed execution (Section 20.1). This requires neither a separate
runtime Candidate architecture nor a universal carrier, runtime Definition lookup or repeated dynamic
Contract interpretation. The distinction in Section 5 concerns authority, not an intermediate
representation.

An Established Fact and all its constituents are immutable. Mutation through an alias or replacement by
observationally equivalent mutable state is forbidden. Neither determinism nor preservation of selected
observations substitutes for immutability; abstract immutability is not adopted as a substitute.

The compiler may change its own working representations. Section 5.1 permits temporary mutation during
formation; Sections 5.3, 13 and 20 govern physical representation changes. Such changes must preserve the
same immutable information, valid observations and authority boundaries. Backing reclamation must not
invalidate a still-lawful observation.

Source qualification and realization verification differ from Contract judgments over actual values. A
calculation may be eliminated only when its obligation is established under the applicable law and its
semantic result and failure attribution are preserved. The generated machine must realize any remaining
necessary actual-value judgment. Verification is not runtime mediation (ADR-0073 Section 8.7): unresolved
realization closure cannot be replaced by continuous interception or repeated permission checks. An
optimization whose meaning preservation cannot be shown must not be admitted.

---

## 6. Fact Surface

The declared surface is the source evidence for canonical Fact material. Each Fact Definition must
declare at least one direct factual coordinate. Its direct surface is flat, finite and closed. Their
declared names identify them within the Definition; duplicates are unlawful. Declaration order and
physical position do not determine meaning.

Fact Kind cannot substitute for a factual value or mark another Fact. Neither presence nor absence
establishes State authority (Section 17). Additional identity, equality or hashing declarations are
neither required nor admitted (Sections 10 and 13).

Whether a Fact describes an entity, event or other domain concept depends solely on its declared
information.

### 6.1. Resolved Value Meaning

Fact consumes completed source interpretation under ADR-0079. The Type Subsystem resolves value meaning;
Fact judges its eligibility as the machine's information. Interpretation alone establishes no Fact
authority.

Before Visible HIR, each coordinate must have complete value meaning, including constituent relations and
all meaning-determining semantic bases. A versioned or external basis must be resolved under its owning
law rather than completed later by ambient provider state. Runtime values may vary within the resolved
meaning but cannot select or extend the schema, coordinate meanings or comparison law.

Generic values are permitted when their complete constituent meaning is resolved at compile time.
Unresolved type arguments or open value meanings that prevent completion are inadmissible; runtime type
information cannot complete the Definition.

A resolved meaning is eligible if it satisfies this ADR, regardless of value family. Fact maintains no
separate inventory. Expansion of interpretation support does not by itself change an existing Definition.
Platform ratification and preservation follow ADR-0073.

Value domains and limits follow the completed interpretation; Fact adds or relaxes no size or range
limit. Invariant owns standing integrity conditions (Section 16). Budget and Capacity own resource
obligations under ADR-0051 and ADR-0052, distinct from compiler resource safety.

### 6.2. Constituent Closure and Information Completion

Every constituent and meaning-determining relation must satisfy the Fact law. Nested constituents belong
to the containing Fact, not to additional direct coordinates or independent authorities. The value schema
must be finite and closed. V1 requires acyclicity, excluding semantic recursion, including mutual
recursion. A complete relation value may describe cycles without introducing recursive value meaning or
references to other Facts (Section 7).

Actual information must be complete and finite before judgment (Section 5.1). Its possible domain need
not be finite: a complete descriptor may describe an infinite domain without containing an infinite
collection. Live streams, pending computations and other sources of undetermined constituents are not
complete material.

Presence is explicit in the declaration or resolved constituent meaning. Required material must be
present; absence is lawful only where permitted. An absent coordinate, a present value expressing
absence, an empty value and a selected alternative without payload are distinct. Unresolved material,
failed formation, unavailable backing and absence of an Established Fact are not value absence.

Fact supplies no missing coordinate by default. Complete material must satisfy ADR-0073 Sections 5.1–5.5,
including Closed Value, Authority Isolation, Resource and No Laundering. All constituents remain subject
to Section 7. Physical immutability, host alias topology or read-only wrappers alone do not establish
semantic isolation.

### 6.3. Exact Observation and Value Distinctions

Every factual distinction must support exact, deterministic observation under its resolved value law.
Host equality, custom comparators and ambient environment state cannot redefine these distinctions.
Incomplete or unsupported observation must not be approximated.

Discrimination of members, tuples and keys must preserve the exact distinctions of their constituents.
Intrinsic or declared order and multiplicity are preserved where meaningful; physical storage and
processing order add no factual meaning.

Fact performs no implicit conversion, representative selection, constituent deduplication or
normalization. The owning formation authority may supply explicitly formed values without requiring Fact
to reconstruct earlier input distinctions. Platform preservation follows ADR-0073.

Presence and selection preserve their declared distinctions. Only material belonging to a present value
or selected alternative contributes value distinctions. Unused storage must not be observed as factual
information, including in comparison, derived keys, diagnostics or Publication. Whole-Fact sameness and
repeated establishment are governed by Sections 10–12; implementation choices by Sections 13 and 20.

---

## 7. No Hidden Relations Between Facts

Facts cannot include or semantically reference other Facts or include another Fact Definition. Importing
its surface through inheritance, nesting or flattening does not circumvent this prohibition. This
prohibition applies throughout the constituent structure, irrespective of interface or World selection.
The compiler must resolve the actual declaration and Contract role; aliases and generic applications
cannot conceal inclusion.

A closed structured value under the common value law is permitted. An ordinary data declaration may serve
as such a value in multiple Facts (Section 9), but neither host types nor object models create
independent Core value authority.

A descriptive coordinate such as `accountId` does not establish its target's existence, identity, content
or accessibility. Equality of coordinate values across Facts establishes no implicit relation or
obligation, including:

- Object or Fact references, joins, lookups and existence obligations.
- Graph or ownership edges and automatic cross-Fact binding.
- Invariant scope or Operation participation.

Another Contract may explicitly use an exposed coordinate as an operand and owns the resulting relation.
A value lawfully obtained from a participating Fact may form part of a different Fact under the
applicable formation law, provided the result is complete independently of continued access to the
original. Participation requires separate authority (Section 15). Physical sharing under Section 20
grants no further observation or participation authority.

A descriptive value must not retain capability or resource authority over its denoted target, regardless
of carrier (ADR-0073 Sections 5.3–5.5). This governs resolved meaning and lawful use, not a general
ability to discover every possible external use of arbitrary values.

Fact supplies no implicit currentness, supersession, population or cardinality across Facts. Nor does it
supply grouping, continuity, history, sequence, interval or topology relations. Such meaning must belong
to an explicit applicable Contract authority (including Operation, Change, State or Transition) or
factual material wholly within one Fact. Fact does not invent that owner.

---

## 8. Fact Vocabulary

The enclosing interface declares one exact vocabulary by directly selecting Fact surfaces in the IDL.
Definition and Version are resolved under their applicable laws. No separate host vocabulary class or
named vocabulary declaration is admitted.

```text
interface AccountService {
    facts {
        DepositOperationInput
        DepositRecorded
        AccountOpened
        Balance
        WithdrawalRecorded
        AccountClosed
    }
    invariants AccountInvariants

    operation deposit(...)
    operation withdraw(...)
    operation close(...)
}
```

Only explicit selection changes membership. Package or directory placement, neighboring declarations,
inheritance, class-literal collections, computed membership and discovery (including reflection or
classpath scanning) cannot add kinds implicitly.

Selection has no semantic order. Repeating the same exact Definition is unlawful, even under different
source names. Each occurrence remains observable until judgment; the established vocabulary is
duplicate-free (Sections 19.4 and 19.9).

Membership declares the Fact kinds eligible for establishment in the Core, not universal participation
authority (Section 15).

---

## 9. Host Frontend Evidence

Host declarations provide source evidence under Sections 6–7 and ADR-0073. V1 admits these native data
declarations:

- Kotlin: a `data class` with direct coordinates declared as primary-constructor `val` properties.
- Java: a `record` under the supported Java language profile (Java 17 or later), with direct coordinates declared as
  record components.

```kotlin
data class Balance(
    val accountId: Long,
    val amountMinor: Long,
)
```

```java
public record Balance(
        long accountId,
        long amountMinor
) {
}
```

Ordinary classes are not an alternative Fact authoring surface in V1. The declaration is data-only; the
following are not admitted:

- User-authored constructor bodies, additional constructors, default coordinate values and initialization blocks.
- Additional data members, methods, custom accessors and delegation.
- Explicit inheritance, including interface implementation, companion objects and user-authored static members.

These restrictions also apply to user-defined data classes and records used as structured values within a
Fact. Their complete constituent meaning must satisfy Sections 6–7. Language-generated members provide no
behavioral authority; host declaration alone proves neither immutability nor platform eligibility.

No proprietary scalar wrapper or user-authored Value Object is required merely because information
belongs to the Core. Selection requires no annotation or marker interface on the declaration (Section 8).

Resolution retains domain-neutral canonical Contract material, not the authored object model. The exact
Versioned Definition retains the value meaning and distinctions of Section 6; applicable Contract World
material is preserved under the establishment and applicability laws, not as an additional sameness
determinant (Section 10.1). Diagnostic and attribution source coordinates remain separately owned unless
explicitly declared as factual material (Section 19.2).

Native construction and coordinate access may express and observe immutable data in a lawful realization.
Construction alone establishes no Fact or change authority. A lawfully bound Operation may consume this
data in the Core without acquiring independent Contract authority. Qualification of language-generated
data operations belongs to the common Type Subsystem and applicable operation laws; Fact supplies no
separate operation set.

A host object is not itself the Fact in the Core. Host execution mechanics acquire no Fact authority.
Construction syntax does not require physical object allocation, and the backend remains replaceable (Section 20).

---

## 10. Fact Sameness

Fact sameness compares complete information. Fact Kind identifies one exact Established Fact Definition
under its Authority and Version (ADR-0053 Sections 3.1 and 3.5; ADR-0063 Section 6), not an independent
Fact identity or user-selected identity coordinate.

```text
same exact Versioned Fact Definition
+ same complete canonical factual material
= same Fact
```

Different Authorities or Versions denote different kinds even if their surfaces and values match. Version
binds the Definition, not a factual coordinate. Recompilation and relocation preserve Definition identity
under ADR-0063.

Sameness is a deterministic equivalence relation (reflexive, symmetric and transitive) over complete
lawful material. Comparison preserves all distinctions under Section 6.3. Incomplete or unavailable
information alone establishes neither sameness nor difference.

User-defined `equals`, `hashCode`, identity selectors and comparison laws are inadmissible.
Host-generated methods have no Fact authority. Neither host equality nor another Contract's equivalence
may merge distinct material or divide identical material. Section 6.3 excludes normalization, conversion
and representative selection from comparison.

```text
Balance(accountId = 42, currency = KRW, amountMinor = 1000)
Balance(accountId = 42, currency = KRW, amountMinor = 1000)

    -> the same Fact
```

The example assumes the same exact Versioned Definition. Implementation identity and history, including
repeated construction or delivery, do not determine sameness. This is an internal Contract law, not a
separately authored comparison API.

### 10.1. World and Authority Boundary

Informational sameness is independent of the surrounding Contract World, Governance selection and
compiler World generation. Other Contracts' changes do not alter an unchanged Fact Definition's
information; its own meaning-determining bases remain part of that Definition.

Sameness establishes neither authority in a machine nor current participation. Establishment and
applicability retain their exact World and Scope bindings. World selection alone neither establishes nor
removes Fact authority. Identical information in another World or Scope grants no automatic authority,
compatibility, conversion, reinterpretation or participation. Different Versions are different Fact kinds
and may enter subsequent processing only under applicable explicit laws.

---

## 11. Explicit Occurrence Distinction

Any occurrence distinction that determines factual meaning must be explicit, including count, time,
source or submission identity.

```text
WithdrawalSubmitted(
    submissionId = 1001,
    accountId = 42,
    amountMinor = 1000,
)

WithdrawalSubmitted(
    submissionId = 1002,
    accountId = 42,
    amountMinor = 1000,
)

    -> different Facts
```

The Facts differ because their declared material differs. An occurrence coordinate has no special
identity authority; its value is supplied by the formation owner, not generated by Fact. If two
occurrences must remain distinct while all coordinates are equal, the surface does not yet express the
required distinction. Hidden implementation identity or history cannot supply the distinction (Section
10).

Fact owns no independent Established Occurrence Material under ADR-0063 Section 4.4.1. Formation,
standing judgments and Operation application results retain their respective owners. Repeated
applications acquire no hidden Fact Occurrence Identity, separate runtime Material family or Occurrence
Projection.

---

## 12. Set-Like Fact Semantics

Within the same established information context, repeated establishment of the same Fact under Section 10
creates no implicit multiplicity.

```text
{ F, F } = { F }
```

This is an observation law, not a required set data structure or a ban on repeated lawful establishment.
Storage or delivery duplicates add no meaning. Authority scopes remain independent (Section 10.1).

Repeated formation or establishment may have independent occurrences and judgments. Sameness alone
neither refuses repeated establishment nor permits omission of judgments or required observations. Other
Contract obligations remain independent of informational multiplicity; reuse follows Section 20.

Explicit occurrence distinctions follow Section 11. Constituent order and multiplicity remain intact
under Section 6.3. Internal reference counts or incremental weights neither become factual occurrence
counts nor authorize removal of Established Facts.

---

## 13. Derived Identity and Physical Keys

Kontrakt may derive physical keys from Section 10's canonical information. These are representation
material, not logical Fact identifiers. Fact prescribes no hash algorithm, canonical byte encoding or
interned representation.

Compact key equality establishes at most a candidate match. Key inequality may reject sameness only when
valid, comparable keys share a derivation basis that preserves informational equality. Keys from
different derivation or encoding bases do not establish difference merely by differing. Candidate matches
require exact verification regardless of collision probability; collisions must never merge different
Facts.

Comparison may use the resolved structure or previously verified immutable material. Physical reference
identity suffices only when known to denote the same exact information with backing valid for the
observation. Reused addresses or unverified handles are insufficient (Section 20).

Changes to hashing, encoding or key layout do not themselves change meaning or Version. ADR-0041's
metadata identity design imposes no encoding or HID suite on runtime Facts. Section 20 governs security
and reuse.

---

## 14. No Fact-Owned Coexistence Restriction

Fact declares no uniqueness law over factual coordinates or tuples and owns no restriction on coexistence
of different Facts. Shared coordinate values are no ground for refusing, merging or replacing Facts.
Section 12 applies only to identical information.

This does not establish legality under other applicable Contracts. Fact grants no authority over
replacement, supersession, currentness or relations (Sections 7 and 15). No corresponding coexistence
obligation is introduced into Invariant or another Contract.

---

## 15. Fact and Operation-Specific Change

Fact defines information, not the means of forming a Change Proposal. The selected Operation's explicit
bindings own its factual participation and change meaning. Fact vocabulary membership alone grants no
permission to consume or change Facts.

Operation Result Material not established as Fact remains outside Fact authority. Calculated values do
not become Facts merely because an Invariant could use them; Kontrakt must not synthesize summary or
aggregate Facts to expose calculated material to another Contract.

---

## 16. Relationship to Invariant

Invariant declares a standing integrity law over one exact Fact Kind. The interface-level catalog
declares these laws separately; Fact surfaces do not list all applicable Invariants.

Invariant may inspect declared coordinates under its own law but neither creates them nor defines Fact
sameness or cross-Fact relations. Its Fact-local law imposes no coexistence restriction on different
Facts (Section 14).

---

## 17. Relationship to State and Transition

State expresses an explicit machine condition for movement; Transition declares movement between States.
Fact carries no hidden lifecycle State, and State labels own no factual meaning.

A Change Proposal may bind factual change and State movement requiring joint establishment, without
merging their authorities.

---

## 18. Relationship to Publication and Output

Established Facts and Fact authority remain inside the Contract Core and grant no outward authority.
Publication under ADR-0058 determines which authoritative exit material may cross the outward semantic
boundary; Output Presentation under ADR-0059 declares the outward result shape.

A published presentation is not Fact. On return it is external material again.

---

## 19. Definition-Time Law

Kontrakt must resolve every Fact surface selected under Section 8. The HIR seal certifies complete
resolved meaning and reference integrity, not Fact legality (Section 19.6). Definition Establishment
applies the Fact law without reopening source interpretation (Section 19.7).

Hidden membership and unsupported host behavior are inadmissible under Sections 8–9. Open shape and
unresolved distinctions prevent legal completion under Section 6; complete violations of the Fact law
belong to its owning judgment. Platform-use and formation failures retain their own attribution (ADR-0063
Section 4.7.2).

Diagnostic source coordinates must remain available through separately owned provenance (Sections 9 and
19.2). Executable machine formation and publication require complete, coherent observations of the
necessary Established Definitions (Sections 19.11–19.12) and vocabulary selection under its owning law.

### 19.1. Resolved Fact Definition Candidate

Fact specializes Resolved Contract HIR under ADR-0071. Its Definition Candidate identifies the exact Fact
Authority and resolved Version candidate coordinate, and expresses the complete resolved Definition
meaning without establishing Definition or actual Fact authority. IDL selection remains a separate
Binding Candidate (Section 19.4).

Two components determine informational Definition meaning:

**Direct Factual Coordinate Surface**

Identifies every direct coordinate by its nominal identity within the Definition. The complete
composition is independent of declaration order and physical position.

**Resolved Factual Value Meaning**

Provides the completed meaning of each direct coordinate under Section 6, including all
meaning-determining semantic bases and constituent relations. It may be expressed directly or through an
exact producer-owned reference that exposes the completed meaning; an unresolved type or source reference
is insufficient.

Authority and Version identify the Definition under their owning laws, not as factual coordinates.
Selection-only context, formation, standing Invariant and Operation participation do not determine its
meaning. The Candidate requires no separate formation law, judgment expression, relation schema or
occurrence requirement. Common value meaning retains its owner.

The Candidate must preserve completed but unlawful surfaces, including empty surfaces, multiplicity and
coordinate collisions expressible after source interpretation. Section 6 determines legality rather than
representability. Unresolved coordinates or meanings are not completed violations and cannot be supplied
by Establishment.

### 19.2. Information Retention and Semantic Equality

The Fact-owned Definition Candidate Projection exposes the complete meaning of Section 19.1 under
ADR-0071. Direct semantic references preserve the referenced meaning's ownership and permit no
reconstruction from source, IDL selection or unresolved Type Subsystem material.

Source qualification and interpretation must complete under ADR-0079. Additional resolved evidence
required by a Fact judgment must remain legally observable, particularly the evidence needed to enforce
Section 7 after structural representation or alias resolution. Such evidence is not necessarily a
Definition determinant. Separately owned Host Binding and diagnostic provenance must remain available for
their required observations even when excluded from informational meaning.

Fact Definition Meaning Equality compares both complete determinants in Section 19.1 under their owning
laws, including resolved multiplicity or collisions. Direct declaration order and physical representation
are immaterial; constituent order and multiplicity follow Section 6.3.

Equal Definition meaning does not identify the same Candidate Reference, merge independent Definitions or
establish equality of broader observations containing different required evidence. Whole-Fact sameness (Section 10) and
current reuse qualification (Section 20) are separate. Physical sharing is permitted
only while preserving these distinctions, validity and security.

### 19.3. Typed Fact HIR Candidate References

Fact owns these compiler-semantic reference domains under ADR-0071:

```text
FactDefinitionCandidateRef
ResolvedFactCoordinateRef
FactIDLBindingCandidateRef
```

`FactDefinitionCandidateRef` designates one exact current Definition Candidate.
`ResolvedFactCoordinateRef` designates one direct coordinate by its exact owner reference and nominal
identity. Neither source position, physical index nor a nested constituent determines that identity.
Collisions must remain observable; an ambiguous name cannot be resolved by arbitrarily selecting an
occurrence.

`FactIDLBindingCandidateRef` designates one exact current selection occurrence (Section 19.4). It is
distinct from its target Definition Candidate Reference and the common selecting `ContractInterfaceRef`.

These references are neither factual values nor references between Facts. Lowering consumes the
Fact-owned Definition and coordinate meanings under ADR-0067 Section 11.1. Candidate references establish
neither a Definition, Required Basis nor actual Fact authority. Their logical domains prescribe no
physical layout or encoding.

### 19.4. Fact IDL Binding Candidate and Vocabulary Selection

Each direct vocabulary selection forms a distinct Fact IDL Binding Candidate targeting one exact
Definition Candidate. The complete selection relation belongs to the interface, not an independent
Definition or Contract authority. Multiple selections may designate the same target before judgment under
ADR-0071.

A Binding Candidate expresses its selection occurrence through:

- The exact selecting interface (`ContractInterfaceRef`).
- The Fact vocabulary role.
- The exact target `FactDefinitionCandidateRef`.

No further binding-owned qualifier is required. The Binding neither copies Definition meaning nor absorbs
unrelated interface context. Its own `FactIDLBindingCandidateRef` designates the occurrence; the logical
distinction from its selecting interface requires neither a self-reference field nor redundant storage.

The exact interface and role must expose every selected Binding Candidate Reference, not merely a single
reference or a set of distinct targets. The observation preserves multiplicity until judgment,
independently of declaration order. Omission and explicit absence follow the Interface and common
selection laws; unresolved selection is not absence. Section 8 governs established vocabulary legality.
Complete selection observation establishes neither a Binding nor participation authority and introduces
no additional Primary HIR subject.

### 19.5. Resolved Fact HIR Candidate Protocol

Fact specializes the read-only Resolved HIR Candidate Protocol of ADR-0071 using Section 19.3's reference
domains and the common `ContractInterfaceRef`:

```text
Fact Definition Candidate Projection
    exact FactDefinitionCandidateRef
    complete Fact Definition Candidate meaning

Fact IDL Binding Candidate Projection
    exact FactIDLBindingCandidateRef
    complete Fact IDL Binding Candidate meaning

Fine-Grained Semantic Projections
    Fact Candidate Coordinate Surface Projection
    Fact Selected Coordinate Meaning Projection
    Fact Vocabulary Selection Projection
```

The complete Definition Candidate Projection exposes the exact Authority, resolved Version candidate
coordinate and both determinants of Section 19.1, with required evidence under Section 19.2. The complete
Binding Candidate Projection exposes the selection occurrence defined by Section 19.4. Neither requires
redundant reference fields or copies of separately owned meaning.

**Fact Candidate Coordinate Surface Projection**

Exposes the entire direct surface of one exact `FactDefinitionCandidateRef`. Each nominal coordinate has
its exact `ResolvedFactCoordinateRef` where unambiguously referenceable. Completed multiplicity and
collisions remain observable under Section 19.1. This projection provides Lowering's coverage observation
without exposing every value meaning (ADR-0067).

Equality compares nominal composition and multiplicity, not declaration order. A change confined to value
meaning does not by itself change this observation. Empty or conflicting surfaces remain observable;
ambiguity follows Section 19.3.

**Fact Selected Coordinate Meaning Projection**

Exposes one exact `ResolvedFactCoordinateRef` and its complete resolved value meaning under Section 6.
Equality preserves the selected coordinate and that meaning. Lowering may consume an individual mapping's
required meaning without observing unrelated coordinates. Nested constituents are not direct Fact
coordinates.

**Fact Vocabulary Selection Projection**

Observes the complete selection of one exact `ContractInterfaceRef` at the Fact role (Section 19.4). It
exposes every `FactIDLBindingCandidateRef` occurrence; the Binding Candidate Projection supplies each
target. Equality preserves selecting subject, role and the complete relation, including multiplicity but
not declaration order. This is a multi-selection observation. Neither unresolved nor unavailable
selection is Explicit Absence or a completed smaller vocabulary.

The following example uses the `AccountService` vocabulary in Section 8 and the `Balance` declaration
in Section 9. Assume completed source qualification and value interpretation. `F1` denotes an illustrative
resolved Version candidate coordinate; the reference labels below designate exact references, not source names
or physical handles.

```text
Fact Definition Candidate Projection
    FactDefinitionCandidateRef C
    Authority Balance
    resolved Version candidate coordinate F1
    accountId -> complete signed 64-bit integer meaning
    amountMinor -> complete signed 64-bit integer meaning

Fact Candidate Coordinate Surface Projection
    Definition C
    accountId -> ResolvedFactCoordinateRef A
    amountMinor -> ResolvedFactCoordinateRef M

Fact Selected Coordinate Meaning Projection
    coordinate M
    complete signed 64-bit integer meaning

Fact IDL Binding Candidate Projection
    FactIDLBindingCandidateRef B4
    selecting ContractInterfaceRef I (AccountService)
    role facts
    target FactDefinitionCandidateRef C

Fact Vocabulary Selection Projection
    selecting ContractInterfaceRef I
    role facts
    B1 (DepositOperationInput selection)
    B2 (DepositRecorded selection)
    B3 (AccountOpened selection)
    B4 (Balance selection)
    B5 (WithdrawalRecorded selection)
    B6 (AccountClosed selection)
```

Each `B1`–`B6` denotes a distinct exact `FactIDLBindingCandidateRef`. The selection observation is
complete for this interface; the example expands only the `Balance` Binding Candidate. It illustrates
semantic observations rather than a physical HIR layout. Required evidence remains observable under
Section 19.2.

**Observation and Reuse**

Completeness and equality apply to each projection's declared observation, including evidence exposed by
complete observations. Comparison across generations requires exact subject correspondence and current
reference validity. Equality alone does not establish reuse eligibility (Section 20). An omission from a
narrower observation is not semantic absence; only an observation complete for that question can
establish coordinate absence.

Projections introduce no new Primary HIR subject or authority. Bulk access, shared immutable backing and
fine-grained dependencies are permitted without separate records or Query nodes. Coherence, availability,
reference lifetime and Protocol evolution follow ADR-0071.

### 19.6. Fact HIR Seal and Establishment Handoff

A Fact Definition Candidate may become Visible HIR only when:

- Its exact Fact Authority and resolved Version candidate coordinate are known.
- Its entire surface and value meanings are resolved under Sections 19.1–19.2.
- Required source interpretation and qualification have completed under ADR-0079.
- Required semantic references are exact and valid, and required Protocol observations are complete within a coherent
  generation.

Source Eligibility (Section 9) precedes lawful HIR formation. A forbidden default cannot be erased to
manufacture eligibility. An unresolved type prevents completion; an otherwise eligible, resolved empty
surface remains available for Fact judgment. Seal certifies resolution and integrity, not satisfaction of
Fact law.

A Binding Candidate must resolve its complete selection occurrence under Section 19.4, including one
legally referenceable target under ADR-0071. Complete vocabulary observation requires every occurrence to
be resolved and legally observable. Repeated completed selections remain; unresolved selections cannot be
dropped.

Independent sealed Definition or Binding Candidates may be visible within a coherent generation despite
unrelated completion failure, under ADR-0071. Individual visibility neither completes the vocabulary nor
authorizes machine publication. The entire vocabulary observation requires its scope to be sealed.
Continuation requires valid inputs and a trustworthy compiler domain; recovery or poison material,
unknown required semantic extensions and failed visibility follow common HIR law.

Definition Establishment requires legal HIR observations and the authoritative Version prerequisite (ADR-0063 and
ADR-0053). A complete Definition Candidate Projection, or producer-defined Fine-Grained
Semantic Projections with required evidence, must supply every observation needed for judgment.
Incomplete input prevents entry; Establishment cannot recover missing meaning through source, Type
Subsystem interpretation, host behavior, IDL selection or layout.

Early static verification is permitted when required meaning and legal prerequisites are available.
Moving or eliminating a calculation must preserve judgment ownership, entry conditions and result
attribution (ADR-0063 Section 4.7; ADR-0071).

### 19.7. Fact Definition Establishment Judgment and Result

Fact Definition judgment applies Sections 4, 6 and 7 to one exact complete Candidate, including its
entire coordinate composition and the admissibility of completed value meanings. It consumes completed
interpretation and qualification rather than repeating those owners' judgments.

Legal entry follows Section 19.6. The Version law establishes or supplies an existing exact binding under
ADR-0063 Section 6.3. Neither a Binding Candidate nor complete vocabulary selection is required for this
judgment; this independence authorizes neither acquisition of unselected source nor actual Fact
establishment.

The two Fact-owned results are:

```text
complete resolved Candidate
+ legal Fact Definition meaning
    -> Established Fact Definition

complete resolved Candidate
+ meaning that violates the Fact Definition law
    -> Fact Definition Refusal
```

The minimum successful establishment unit is the entire Definition, not independent coordinate
authorities. Partial verification or parallel evaluation may support judgment but cannot establish a
subset of the meaning as authoritative. Failure creates no partial authority or entitlement to publish or
reuse unestablished material as established. Independent established Definitions and Version Bindings are
not revoked. Temporary backing may be recovered or discarded without changing semantic results.

Failed prerequisites prevent entry and retain their owners' attribution; Source Eligibility,
interpretation, HIR Seal and Version failures are not Fact Definition Refusal. Vocabulary violation
belongs to Section 19.9. If compiler failure makes the exact result indeterminate, neither success nor
refusal may be fabricated (ADR-0063 Section 4.7.2; ADR-0074).

Subsequent World failure does not undo a Definition established in the current compilation.
Establishment, visibility and durable Version-history publication are distinct (ADR-0063 Sections 6.9 and
10.8; Section 19.12). Retained material from unsuccessful work is at most reuse evidence unless current
validity and authority are established. Failure atomicity prescribes no physical transaction, rollback or
crash-recovery mechanism.

### 19.8. Definition-Time Required Basis and Applicability Boundary

The following ADR-0063 categories are `NOT-APPLICABLE` to Fact Definition Establishment:

- Required Basis Law, Basis Resolution, Basis Binding and Complete Basis.
- Separate Definition Applicability, arbitration and Composition judgments.

No Established Input, Lowering, Invariant or actual Fact is required. Compiler interpretation
dependencies alone are not Contract Required Basis. Meaning-determining semantic bases belong to the
resolved Definition (Section 19.1), and the Version prerequisite follows ADR-0053 and ADR-0063 without
introducing a Fact-owned Basis law. Actual Fact establishment remains subject to Section 5.

### 19.9. Fact Vocabulary Selection Legality

Section 8's duplicate check applies to one exact interface's complete selection relation, separately from
individual Definition judgment. Repeating the same exact Versioned Definition is unlawful regardless of
spelling or physical Candidate Reference encoding. Equal structures of independently identified
Definitions are not duplicates. The judgment consumes the complete selection under Sections 19.4–19.6.

Failure concerns selection, not the meaning of selected Definitions. Successful selection legality alone
establishes neither a Definition, authoritative interface binding nor participation. Interface and
Composition laws own authoritative selection and its prerequisites. Fact introduces no independent
vocabulary Definition, Version or Established Material family.

### 19.10. Established Fact Definition Material

Successful Definition Establishment produces one complete Established Fact Definition with two
components:

**Identity**

The exact `EstablishedFactDefinitionRef` identifies the Definition under its Owning Fact Authority
Binding and Version Binding.

**Meaning**

Preserves both complete determinants of Section 19.1, including the direct established relation to each
coordinate and all exact meaning-determining semantic-law and constituent relations. Separately owned
meanings and relations may be observed directly or through exact semantic references without loss,
duplication of authority or renewed interpretation. Definition Establishment introduces no additional
constraint, formation meaning or actual factual information.

Fact owns these Established reference domains:

```text
EstablishedFactDefinitionRef
EstablishedFactCoordinateRef
```

One Fact Authority owns one independently addressable Definition per Version. The exact Authority and
Version Binding determine `EstablishedFactDefinitionRef`; no Authority-Local Definition Coordinate is
required. The Owning Authority Binding is directly recoverable without inference from an interface, host
declaration or physical containment. ADR-0063 Section 6 and ADR-0053 govern identity conflicts and
Version history.

`EstablishedFactCoordinateRef` identifies a direct coordinate through its exact
`EstablishedFactDefinitionRef` and nominal identity. This is a Definition-owned relation, not a separate
coordinate authority or Version. Order and physical location do not determine identity. Lowering consumes
the exact Definition as Required Basis under ADR-0067 Section 11.6 and uses its coordinate meaning
without re-establishing it.

The authoritative Reference becomes legally observable only after the entire Definition and required
identity relations are coherently established. Candidate references cannot be promoted or retyped into
Established References. Distinct Candidates may correspond to the same established identity and meaning
after lawful Establishment or currently qualified reuse. Neither enduring one-to-one correspondence nor
retention of originating HIR is required; Established meaning and references must be interpretable
independently of it.

These observability requirements prescribe no record layout. Shared immutable backing may represent
Candidate and Established meaning without transferring authority. Relocation, encoding and reclamation
must preserve legal observation and reference validity under ADR-0063; Section 19.7 governs atomicity.

Fact establishes no separate Definition Material for vocabulary or coordinates. Interface selection,
Formation, Invariant and participation retain their owners; Definitions need not retain relations to
every consumer. Actual Fact establishment follows Section 5.

### 19.11. Established Fact Semantic Protocol

Fact specializes the Established Semantic Protocol of ADR-0063 Section 11 using the reference domains of
Section 19.10:

```text
Established Fact Definition Projection
Direct Established Relation Projection

Fine-Grained Established Fact Projections
    Established Fact Coordinate Surface Projection
    Established Fact Selected Coordinate Meaning Projection
```

**Established Fact Definition Projection**

Exposes one exact `EstablishedFactDefinitionRef` and the entire Definition of Section 19.10, including
Authority, Version, complete Meaning and direct established relations. Equality preserves the whole
observation. Consumers must not recover meaning by reinterpreting HIR, source or Host Binding.

**Established Fact Coordinate Surface Projection**

Exposes the entire direct surface of one exact Definition, with each `EstablishedFactCoordinateRef`.
Established legality guarantees the nonempty, duplicate-free surface of Section 6. The projection is
complete for coverage without exposing every value meaning. Equality preserves the exact Definition,
nominal composition and relations independently of order or representation. Lowering owns the mapping
coverage judgment under ADR-0067.

**Established Fact Selected Coordinate Meaning Projection**

Exposes one exact `EstablishedFactCoordinateRef` and its complete value meaning under Sections 6 and
19.10. Equality preserves both. Nested constituents are not separately referenceable direct coordinates.
Lowering may consume the projection to judge a mapping's formation meaning. Execution Formation may use
it to select a lawful representation without reinterpreting authored types.

**Direct Established Relation Projection**

The common ADR-0063 Section 11.4 projection observes the exact source, relation and target of Section
19.10's direct relations. It creates no relation, authority, graph model or inferred inverse ownership.
Vocabulary selection retains its Interface and Composition owners; Section 19.5's vocabulary projection
is HIR-only.

The following example observes `Balance` after lawful Definition Establishment under Section 19.7.
Assume the authoritative Version Binding corresponding to `F1` is available. As in the HIR example,
the reference labels designate exact references. They do not specify their encoding or promote Candidate
References into Established References.

```text
Fact Definition Establishment
    complete sealed Balance Candidate C
    authoritative Version Binding F1
    legal Fact Definition meaning
        -> Established Fact Definition

Established Fact Definition Projection
    EstablishedFactDefinitionRef E
    Owning Fact Authority Binding Balance
    Version Binding F1
    accountId -> complete signed 64-bit integer meaning
    amountMinor -> complete signed 64-bit integer meaning
    direct established relation to coordinate EA
    direct established relation to coordinate EM

Established Fact Coordinate Surface Projection
    Definition E
    accountId -> EstablishedFactCoordinateRef EA
    amountMinor -> EstablishedFactCoordinateRef EM

Established Fact Selected Coordinate Meaning Projection
    coordinate EM
    complete signed 64-bit integer meaning

Direct Established Relation Projection
    source E
    relation direct coordinate
    target EM
```

The established observation preserves the same Definition meaning. It establishes no actual `Balance`
value or authoritative interface selection. The example specifies neither an Established Material layout
nor a runtime representation.

**Observation and Reuse**

Completeness is relative to each projection. A narrower omission does not establish semantic absence. A
complete surface may establish coordinate absence; invalid references, unavailable backing and failed
observations cannot. Coherence, availability and attribution follow ADR-0063 Sections 11.5–11.7 and
ADR-0074.

Equality preserves the exact exposed identity and meaning. Equal value meanings do not equate distinct
Versioned Definitions or coordinate references. Calculations observing only common value meaning may be
reused under current-validity law without equating broader observations. Complete surface reuse requires
evidence of current closure, not merely validity of previously observed members.

Consumers select existing legal observations; their derived compiler products are not Fact Projections.
Additional Host Binding and provenance requirements retain their owners. No Occurrence or runtime
Candidate Projection or other runtime material family is introduced. The Protocol requires neither
runtime interface calls nor separate allocations, records, Query nodes or mandatory hashing. Bulk and
fine-grained access may share immutable backing under ADR-0063 Section 11.

### 19.12. Canonical Contract World Placement

The Canonical Contract World (ADR-0063 Section 10) exposes complete Established Fact Definitions through
Section 19.11. Each Definition's identity, meaning and required direct relations must be coherent. HIR
Candidates, runtime factual values and occurrence-specific material are outside this Definition-world
substrate. Provenance remains adjacent; downstream products retain their owners.

Visibility creates no new authority and does not re-establish Definitions. The compiler's World is
distinct from the Policy-owned Contract World. Co-location or linking establishes neither Policy
membership nor vocabulary selection, Fact relations or other Contract bindings. Established selections
and composed relations are observed under their owning laws.

Assembly, linking and representation changes preserve unchanged Definitions' identity and meaning.
Compiler World generation, placement and aggregate identity do not determine Definition identity or
actual Fact sameness (Section 10.1). Direct relations retain their owning states; unresolved required
relations prevent visibility, whereas absence of a future application is not an incomplete Definition.

Independent complete Definitions may become visible without unrelated work when consumer closure is
coherent (ADR-0063 Section 10.7). Partial World coverage may be lawful; partial Established Definition
meaning is not. Consumers require complete, current, coherent observations before entry. Physical
assembly may combine validated backing without exposing inconsistency.

World formation or observation failure belongs to the compiler and prevents dependent entry where
required meaning is unavailable. Independent establishment is preserved under Section 19.7, which also
distinguishes current authority from durable publication. Retained backing requires current qualification
on later invocations. Storage, linking and reclamation mechanisms remain replaceable.

---

## 20. Optimization Boundary

Any representation compatible with complete canonical material and deterministic legal observation is
permitted. Comparison may be accelerated without changing distinctions (Sections 10 and 13); no
particular serialization, interning, storage layout or algorithm is required.

Realization must preserve Section 5's stability throughout comparison and use. Sharing, relocation or
reclamation must neither change information observed by a valid reference nor let a stale reference
establish sameness. Equivalent valid backing may be selected physically; acquisition, routing, iteration
and scheduling order cannot determine meaning, authority or Contract observations.

Comparison must remain exact in the presence of collisions (Section 13). Collision verification alone
does not protect against adversarial comparison or lookup costs. Resource safety is separate, with Budget
and Capacity obligations retained by their owners. Resource or compiler failure must not become an
invented sameness result or Fact refusal.

Sameness grants no cross-trust-boundary sharing authority. Sharing and derived representations must
preserve information and authority isolation, including against unauthorized inference through observable
reuse behavior. Internal keys, handles and cache observations confer no automatic outward or diagnostic
authority; disclosure follows the applicable Publication and diagnostic laws.

Matching information may support material reuse, not the current validity of earlier judgment,
establishment or participation. Reuse requires exact observations and current validity under ADR-0063
Section 11.7 and ADR-0075 Sections 13 and 22. Optimized paths must preserve the deterministic clean
path's legal observations. No universal cache key or new factual coordinate follows from this law.
Derived structures may not introduce undeclared Fact relations, multiplicity, currentness, participation
authority or other cross-Fact semantics (Sections 7, 12 and 15).

### 20.1. Compiler Conformance and Independent Verification

Conformance extends beyond value comparison to the entire compiler realization. Each seam must preserve
required observations, authority boundaries and result attribution:

- Source interpretation and HIR formation: Sections 6, 9 and 19.1–19.6, including evidence required by owning judgments.
- Definition Establishment: Sections 19.7–19.10, including complete authority and failure atomicity.
- World visibility, linking and Established Protocol: Sections 19.11–19.12.
- Execution formation, optimization and backend: runtime obligations of Sections 5–7 and 10–13.

Fusion and reuse must preserve independent judgments, their required meaning and failure boundaries. No
optimization may weaken these obligations or change Established information (Section 5.4).

Conformance must support comparison with the deterministic clean path and independent verification
against the Fact Contract (ADR-0071; ADR-0075). Agreement between two defective paths is insufficient.
Verification products check conformity rather than create Fact authority or replace owning judgments.
This verification requirement imposes no mandatory duplicate computation during governed execution.

Verification covers exact reference kinds, current validity, value equality and complete observation
closure. Stale or wrong-kind handles, corrupted backing, poisoned caches and partial publication must not
supply authoritative material. The collision, resource and isolation laws of Section 20 also govern HIR
and Established Protocol realization. Compiler failure, absence, non-entry and Contract refusal retain
their distinct meanings and owners.

Semantic correctness, security conformity and performance are distinct verification concerns. A correct
result establishes neither resource safety, information isolation nor efficiency. Compiler Design owns
realization choices, verification methods and performance measurements. V1 and V2 may differ in
realization while preserving Contract meaning and satisfying current-validity and security requirements.

---

## 21. Open in This Section

The V1 Fact authoring surface is defined in Section 9. Support for a generic Fact declaration as a source of specialized
Fact Definitions remains open; it is distinct from the use of fully resolved generic values under Section 6.1.

The Version authoring surface remains with the Version Contract. Common value authoring and the qualification of
language-generated data operations remain with their respective owners.

The complete Operation authoring surface for existing-Fact participation, additional Fact changes, Operation Result
Material, and Change Proposal binding is outside this ADR.

Persistence layout, cache layout, storage engine, physical deduplication, lookup index, restoration mechanism, and
backend carrier shape remain realization decisions.

---

## 22. Consequences

The Core holds explicit immutable information rather than implementation objects promoted to authority.
The finite, explicit vocabulary and native authoring surface allow representation replacement without
changing meaning or identity (Sections 8–9).

Exact sameness and set-like semantics preserve information without hidden multiplicity (Sections 10–12).
Coexistence, cross-Fact relations and participation are not implicit consequences of factual authority (Sections 7 and
14–15).

