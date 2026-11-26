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

Fact expresses the machine's own information. As described in `What Contract Is`, Section 12.1, Operations perform
work while Facts describe what the machine knows. A Fact is established inside the Contract Core and remains immutable.
Its meaning belongs to the machine, independently of external material or the path by which that information is formed.

A Fact is not limited to a final domain value. It is an immutable proposition upon which the machine may rely under
its declared meaning.

A Fact carries no hidden object or occurrence identity. A request, claim, measurement, report, or observation may itself
be factual material, but declaring its occurrence does not make its content stronger than the Fact declaration permits.

Only established Facts belong to the core as factual Contract material. Transient implementation values may exist
during realization without acquiring Fact authority.

---

## 2. Problem

Without an explicit Fact Contract, factual authority can collapse into whatever the implementation happens to store.
A class may look immutable, a database row may persist, a DTO may cross a boundary, or an Operation may return an
object. None of these conditions establishes factual authority. Object identity, allocation history, repeated delivery,
custom equality, repository membership and backend layout are implementation mechanisms, not hidden factual meaning.

The machine needs an explicit factual surface that survives representation replacement without changing what it knows.
An exact sameness law is also required: repeated establishment of identical information must not create hidden
multiplicity.
Occurrence, count, provenance or repetition must be expressed as factual material whenever they determine meaning.

---

## 3. Decision Drivers

Fact must be Contract material rather than a class, row, DTO, record, Value Object, entity, object graph, repository
entry, message identity, or storage layout.

Everything the Contract Core contains as factual material must be available as explicit immutable Fact material.

A host declaration may nominate factual coordinates when completed source interpretation and platform ratification
preserve their required meaning (Sections 6 and 9). Fact does not maintain its own inventory of value forms.

Kontrakt must not require a proprietary wrapper merely to make a value look internal. The interface selects Fact
surfaces directly in the IDL; no separate host vocabulary declaration is required (Section 8). Membership must be
explicit. Package membership, directory placement, classpath discovery, reflection scanning, runtime collections,
inheritance and neighboring source declarations must not add Fact kinds implicitly.

Fact meaning must remain independent of physical representation. Sameness must preserve the complete information without
depending on host equality or hashing. Fact owns neither independent object identity nor a restriction on the
coexistence of different Facts. Sameness and derived physical keys must not synthesize currentness, supersession,
history, topology or cross-Fact relations. Facts must not contain semantic references to other Facts.

A backend may derive compact keys and use any deterministic storage layout, provided these mechanisms neither create
nor erase factual distinctions.

---

## 4. Fact Meaning

A Fact is an immutable factual proposition under one exact Versioned Fact Definition, expressed by its complete
canonical factual material.

**Fact Contract**

Declares what information may hold factual authority inside the Contract Core and requires its complete, immutable
expression under the law of Section 6.

**Established Fact**

Information that holds authority under that Fact Contract after lawful establishment (Section 5).

Fact meaning is independent of its formation path. No Input, Output, Lowering or Operation declaration determines
what a Fact means merely by supplying or consuming its values.

Fact carries no behavior. It does not perform computation, validate itself, decide movement or authorize Publication.
Fact-owned judgments are performed by Kontrakt under the declared law; they are not methods of the factual value.

Canonical factual material preserves every distinction that the Fact declares, including provenance or occurrence
when these are themselves factual information. The coordinate surface and its resolved value meaning are governed by
Section 6. Scope and Version remain explicit under their applicable laws. No universal runtime carrier schema is
required.

Canonical material is independent of physical representation. It preserves the complete information without requiring
a particular byte encoding or applying Canonicalization to remove distinctions. Internal addresses, padding and unused
storage are not factual material. Exact comparison and derived keys remain governed by Sections 10 and 13.

---

## 5. Core Authority

The machine owns its factual meaning independently of the boundary. Boundary presentation does not become Fact by
being admitted or canonicalized. Lowering establishes a complete Fact Candidate set under ADR-0067, not Fact authority.
Result-side formation may also supply proposed factual material under the applicable Operation and Change laws. The
formation path does not change the Fact Definition or introduce a hidden factual distinction (Section 11).

Completed Definition meaning, complete candidate material and Established Fact are distinct. A candidate must already
express the information required by its exact Fact Definition before the applicable authority judgments begin. This
completeness does not establish its legality or give it factual authority.

### 5.1. Candidate Completion and Coherent Judgment

A candidate must satisfy the information-completion boundary of Section 6.2. Its formation is owned by the applicable
formation law, not by Fact. Missing information must not be supplied through implicit computation, lookup, default
selection or normalization during Fact judgment.

All required judgments must concern the same complete, stable candidate information. No mutation through a carrier or
alias may change the information between judgment and establishment. Formation may use temporary internal mutation
before that boundary, subject to ADR-0073. Physical sharing or deferred materialization of already determined
information is permitted when it preserves the exact observation; it must not defer a decision about the information
itself.

### 5.2. Lawful Establishment

Fact authority is established only after the required, separately owned judgments succeed under the applicable
Composition. ADR-0067 Section 17 defines this boundary for Lowering and the subsequent Operation handoff. The
applicable Fact judgments remain distinct from standing Invariant and movement judgments.

When a complete candidate set or Change Proposal requires indivisible establishment, all required judgments must
succeed before any part acquires Fact authority. Establishment of that set or proposal must then be indivisible.
ADR-0069 Section 16 governs the standing judgments over a complete Change Proposal. These dependencies do not prescribe
a physical execution order or impose one establishment unit on independent formations.

A refused establishment places none of its proposed material in the Contract Core. A prerequisite failure that prevents
entry does not create a Fact refusal. Result attribution, compiler failure and an indeterminate establishment result
follow ADR-0063 Section 4.7.2 and ADR-0074. No successful establishment or refusal may be invented when the exact result
cannot be established.

### 5.3. Continuity and Physical Restoration

An Established Fact retains its information. Subsequent processing cannot rewrite it; new information must be expressed
as new factual material and lawfully established under the applicable change authority (Section 15). Immutability does
not require permanent retention or a fixed physical representation.

Physical bootstrap, restoration, storage loading, cache loading and backend reconstruction may make an existing
Established Fact available again. These are realization choices, not new sources of Fact authority. Restoration must
preserve the exact established meaning and its authority under the applicable Version, World and validity laws.

Physical existence, equal payload or a matching digest alone does not establish that material as an existing Fact.
Unavailable or damaged material must not be silently completed or promoted to authority. An exact, valid restoration
need not repeat the original establishment merely because the physical backing has changed. The detailed realization
and restoration mechanism remains outside this ADR (Section 21).

### 5.4. Declared Immutability and Realization Guarantee

The user declares immutable information through the authoring surface of Section 9. Mutable Fact declarations and
uses that change a Fact's information are not admitted. A host language's immutable-looking declaration is evidence
of this intent, not proof that its realization preserves the information. Kontrakt is responsible for constructing
and verifying a machine that actually satisfies the declared Fact law.

Definition Establishment determines the legality of the declared meaning. It does not establish the conformity of
an execution realization. Compiler analysis, execution formation, optimization and backend realization must preserve
the established Contract meaning and fulfill the obligations of Sections 5–7 and 10–13 in governed execution.
These obligations do not require a separate runtime Candidate architecture, universal carrier, runtime Definition
lookup or repeated dynamic interpretation of the Contract. The distinction between proposed information and
Established Fact in Section 5 concerns authority, not a mandatory intermediate representation.

The information of an Established Fact is immutable, including every constituent that belongs to it. Neither
mutation through an alias nor replacement of that information by an observationally equivalent mutable state is
permitted. Determinism and preservation of selected observations do not substitute for immutability. This law does
not adopt abstract immutability as a substitute for immutable factual material.

The compiler may change its own working representations and use temporary mutation during formation as permitted by
Section 5.1. Physical relocation, storage reuse and other representation changes remain implementation when they
preserve the same immutable information, valid observations and authority boundaries. Reclaiming backing must not
invalidate an observation that is still lawful. Sections 5.3, 13 and 20 govern these realization freedoms; they
permit no change to an Established Fact itself.

Source qualification and realization verification remain distinct from the declared Contract judgments over actual
values. A calculation may be eliminated when its obligation is established under the applicable law and the same
semantic result and failure attribution are preserved. Actual-value judgments that remain necessary must be realized
by the generated machine. Verification under ADR-0073 Section 8.7 is not runtime mediation: unresolved realization
closure cannot be replaced with continuous interception or repeated permission checks. When a proposed optimization
cannot be shown to preserve the required meaning, it must not be used as an admitted realization.

---

## 6. Fact Surface

A declared Fact surface provides the complete factual coordinates and distinctions required by that information. Each
Fact Definition must declare at least one direct factual coordinate. The direct surface is flat, finite and closed;
each coordinate is identified by its declared name within that Definition. Duplicate coordinate names are unlawful.
Declaration order and physical position do not determine coordinate meaning.

Fact Kind does not substitute for a factual value or serve as a marker attached to another Fact. Neither the presence
nor the absence of a Fact establishes State authority (Section 17).

The declared surface is the source evidence from which canonical Fact material is resolved. No additional identity,
equality or hashing declaration is required or admitted as Fact meaning (Sections 10 and 13).

Fact is not universally an entity, event, identifier, persisted record, message, or state snapshot.
Those meanings exist only when the declared factual material says so.

### 6.1. Resolved Value Meaning

Fact consumes completed source interpretation under ADR-0079 and the applicable value laws. The Type Subsystem resolves
what each authored value means; Fact determines whether that meaning is lawful as the machine's own information.
Successful interpretation alone establishes no Fact authority.

Each coordinate must have a complete resolved value meaning before Visible HIR. This includes the meaning of its
constituents and relations and any semantic basis required to interpret them. A versioned or external semantic basis
must be resolved under its owning law; ambient provider state cannot complete the meaning later. Runtime values may
vary within that resolved meaning, but must not select or extend the schema, coordinate meanings or comparison law.

Generic value forms are permitted when interpretation resolves their complete constituent meaning at compile time. An
unresolved type argument or open value meaning that prevents this completion must not be admitted. Runtime type
information cannot supply the missing Definition meaning.

Fact does not maintain an independent inventory of value forms. A resolved meaning is eligible if it satisfies this
ADR, regardless of its value family. Expansion of interpretation support does not by itself change an existing Fact
Definition. Platform ratification and preservation remain subject to ADR-0073.

Value domains and their limits follow the completed interpretation. Fact neither introduces an independent size or
range limit nor relaxes a limit of that meaning. Standing integrity conditions on factual values belong to Invariant
(Section 16). Resource obligations belong to Budget and Capacity under ADR-0051 and ADR-0052; compiler resource safety
remains a separate realization obligation.

### 6.2. Constituent Closure and Information Completion

The law applies to every constituent and meaning-determining relation of the resolved value. Nested constituents
remain part of the containing Fact and do not become additional direct coordinates or independent Contract authorities.
The value schema must be finite and closed. V1 requires an acyclic schema; semantic recursive types and mutually
recursive value schemas are outside its scope. A complete relation value may describe cyclic relations, provided
it contains no recursive value meaning or semantic reference to another Fact (Section 7).

Actual factual material must be complete and finite before judgment (Section 5.1). This concerns the information itself,
not the cardinality of the possible domain. A complete descriptor may describe an infinite domain without containing
an infinite collection. A live stream, pending computation or source of as-yet-undetermined constituents cannot serve
as complete factual material.

Presence is explicit in the direct coordinate declaration or the resolved constituent meaning. Required material must
be present; absence is lawful only where the applicable meaning permits it. An absent coordinate is distinct from a
present value that expresses absence, an empty value or a selected alternative with no payload. Unresolved material,
failed formation, unavailable backing and absence of an Established Fact are not value absence. Fact does not fill a
missing coordinate with a default.

Complete factual material must satisfy the Closed Value, Authority Isolation, Resource and No Laundering laws of
ADR-0073 (Sections 5.1–5.5). All constituents remain subject to Section 7. Physical immutability, host alias topology
and a read-only wrapper are not sufficient evidence of semantic isolation. Section 5 governs the stability required
through judgment and establishment.

### 6.3. Exact Observation and Value Distinctions

Every factual distinction must support exact, deterministic observation. Fact consumes the resolved distinctions of
the applicable value law, including its structural relations. Host equality, a user-defined comparator or ambient
environment state cannot redefine those distinctions. An unsupported or incomplete observation cannot be
approximated by a supported one.

Where a resolved structure distinguishes members, tuples or keys, its discrimination must preserve the exact factual
value distinctions of the corresponding constituents. Distinctions required by Fact must not be collapsed by a coarser
comparison. Intrinsic or declared order and multiplicity must be preserved where they belong to the resolved meaning;
physical storage or compiler processing order does not add factual meaning.

Fact does not perform implicit conversion, representative selection, deduplication of constituents or normalization.
An explicitly formed value may be supplied by the owning formation authority; Fact need not reconstruct distinctions
from that formation's earlier inputs. Platform preservation of the value presented to Fact remains subject to ADR-0073.

Presence and selection retain their declared distinctions. Only the material belonging to a present value or selected
alternative contributes its value distinctions. Unused storage must not be observed as factual information, including
for comparison, derived keys, diagnostic evidence or Publication.

This law fixes the preservation of value distinctions. Section 10 defines whole-Fact sameness, and Section 12 governs
the repeated establishment of identical information without changing its constituent meaning. Exact comparison does
not prescribe an encoding, full physical traversal or a particular access strategy (Sections 13 and 20).

---

## 7. No Hidden Relations Between Facts

Facts do not reference other Facts as semantic objects. A Fact must not include another Fact Definition or import its
surface through a source relation. Inheritance, nesting or flattening a Fact declaration into a product cannot make
that inclusion lawful. This restriction applies throughout the constituent structure (Section 6.2).

A closed structured value interpreted under the applicable common value law may be used without creating another
Fact authority. An ordinary data declaration may be reused as such a value in several Facts (Section 9). Neither a
domain-named host type nor a user-defined object model supplies an independent Core value authority.

The compiler must resolve the actual declaration and its applicable Contract role when checking this boundary. A type
alias or a generic application must not conceal the inclusion of another Fact. The prohibition does not depend on
whether the two Facts are selected by the same interface or on World selection.

A factual coordinate such as `accountId` is factual material only. It may describe a target without establishing the
target's existence, identity, content or accessibility.

The same coordinate value appearing in another Fact does not create:

```text
object relation
Fact reference
join
lookup obligation
existence obligation
graph edge
ownership edge
automatic cross-Fact binding
Invariant scope
Operation participation
```

Another Contract may explicitly use an exposed factual coordinate as an operand. That Contract owns the resulting
relation, not Fact.

A value lawfully obtained from a participating Fact may be expressed as part of a different Fact under the applicable
formation law. The resulting information must be complete in its own right and must not depend on continued access to
the original Fact. Participation is separately authorized under Section 15. Physical sharing of immutable backing is
permitted under Section 20, but grants no additional observation or participation authority.

A descriptive value must not retain capability or resource authority over its denoted target. This distinction follows
ADR-0073 Sections 5.3–5.5, regardless of the carrier used. The law concerns the resolved meaning and its lawful use;
it does not establish a general ability to discover every possible external use of arbitrary factual values.

Currentness, cardinality across separately established Facts, supersession, grouping, continuity, history, population,
sequence, interval relation and topology are not implicit properties of every Fact. When such meaning is required, it
must belong to an applicable Operation, Change, State, Transition, another explicit Contract authority, or explicit
factual material contained wholly within one Fact. The Fact Contract does not invent that owner.

---

## 8. Fact Vocabulary

The enclosing interface declares one exact Fact vocabulary through direct selection in the IDL. Each entry names an
exact
Fact surface, with its Definition and Version resolved under the applicable laws. A separate host vocabulary class or
named vocabulary declaration is not part of this authoring surface.

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

Membership is explicit. Package selection, class-literal collections, computed membership and runtime discovery cannot
add Fact kinds. Only explicit vocabulary selection changes membership; moving a declaration into the same package
does not make it Fact.

The vocabulary has no semantic order. Selecting the same exact Fact Definition more than once is unlawful, even when
the source references use different names. Resolution must retain each selection occurrence until the applicable
judgment; it must not silently remove duplicates. The established vocabulary is order-independent and duplicate-free.

Fact vocabulary membership declares which Fact kinds may be established in the core. It does not grant universal
participation authority.

---

## 9. Host Frontend Evidence

A host declaration provides evidence of a closed factual surface when its meaning can be fully interpreted under
Section 6 and its use preserves that meaning under ADR-0073. The V1 Fact authoring surface uses the native data
declaration of each supported language:

- Kotlin: a `data class` with direct coordinates declared as primary-constructor `val` properties.
- Java: a `record` under the supported Java language profile (Java 17 or later), with direct coordinates declared as
  record components.

For example, the same form of factual information may be authored in either language:

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

The declaration expresses information, not an object model. Ordinary classes are not an alternative Fact authoring
surface in V1. User-authored constructor bodies, additional constructors, default coordinate values, initialization
blocks and additional data members are not admitted. The declaration must not define methods, custom accessors,
delegation or explicit inheritance, including interface implementation. Companion objects and user-authored static
members are outside this surface. The mere presence of language-generated members confers no behavioral authority.

The same data-only authoring restrictions apply to user-defined data classes and records used as structured values
within a Fact. Their complete constituent meaning must be resolved under Sections 6 and 7; a host declaration alone
does not establish immutability or platform eligibility.

Kontrakt does not require `CoreInt`, `KontraktText`, or a user-authored Value Object merely because information exists
inside the core. Fact selection requires no annotation or marker interface on the data declaration (Section 8).

A domain-named host declaration remains frontend evidence. Kontrakt does not carry a Kotlin data class, Java record, or
another domain object into the Contract Core as the Fact itself. A carrier used to express a structured value must not
import another Fact or its authority (Section 7).

Resolution retains domain-neutral canonical Contract material. The resolved Fact kind identifies the exact Versioned
Fact Definition, whose coordinates retain the value meanings and factual distinctions under Section 6. Applicable
Contract World material is preserved for establishment and applicability under their owning laws, not as an additional
determinant of informational sameness (Section 10). Source coordinates required for attribution remain separate from
factual meaning unless explicitly declared as factual information. Candidate formation realizes the actual values
under that resolved meaning (Section 5).

The native constructor and coordinate access syntax may express and observe immutable data in a lawful realization.
Construction alone establishes no Fact or change authority. A lawfully bound Operation realization may consume such
data within the Core without becoming an independent Contract authority. The realization remains replaceable.

The interpretation and qualification of language-generated data operations belong to the common Type Subsystem and
the applicable operation laws. Fact imposes its own information restrictions but does not provide a separate set of
data operations. Host constructors, methods, receivers, object identity, and class layout do not survive as Fact
authority.

A backend may realize the same canonical material through generated classes, primitive arrays, packed bytes, tables, or
another deterministic layout. Host construction syntax does not require physical object allocation. These choices remain
implementation.

---

## 10. Fact Sameness

Fact sameness determines whether two complete factual materials express the same information. The resolved Fact kind
identifies one exact Established Fact Definition under its owning Authority and Version (ADR-0053 Sections 3.1 and 3.5;
ADR-0063 Section 6). It introduces no independent Fact identity or user-selected identity coordinate.

```text
same exact Versioned Fact Definition
+ same complete canonical factual material
= same Fact
```

Two different Authorities or Versions denote different Fact kinds even when their factual surfaces and actual values
are identical. Version is part of the Definition binding, not an additional factual coordinate. The same Definition
retains its identity across physical recompilation or relocation under ADR-0063.

Sameness is a deterministic equivalence relation over complete lawful factual material: it is reflexive, symmetric
and transitive. The comparison preserves every distinction of the declared coordinates and their constituents under
Section 6.3. If necessary information is incomplete or unavailable, no sameness result may be inferred from that
condition alone. In particular, failure to observe material does not establish that it differs.

User-defined `equals`, `hashCode`, identity selectors and custom comparison laws are not admitted on the Fact authoring
surface. Host-generated methods have no Fact authority. Neither host equality nor another Contract's equivalence may
merge different canonical factual material or divide identical material. Comparison must not normalize, convert or
select a representative in order to establish sameness.

For example:

```text
Balance(accountId = 42, currency = KRW, amountMinor = 1000)
Balance(accountId = 42, currency = KRW, amountMinor = 1000)

    -> the same Fact
```

This assumes the same exact Versioned Fact Definition. Host object identity, allocation history, repeated construction,
delivery and storage location do not determine the result. Fact sameness is an internal Contract law, not a separately
authored comparison API.

### 10.1. World and Authority Boundary

Informational sameness is independent of the surrounding Contract World, Governance selection and compiler World
generation. Changes to other Contracts do not change the information expressed under an unchanged Fact Definition.
Any meaning-determining basis of that Definition remains part of its exact resolved meaning.

Sameness does not establish that the information holds authority in a particular machine or may participate in a current
application. Establishment and applicability retain their exact World and Scope bindings under their owning laws.
World selection alone neither establishes nor removes Fact authority. The existence of identical information in another
World or Scope grants no automatic authority, compatibility, conversion, reinterpretation or participation. Material
from a different Version is not the same Fact and must enter any later processing under the applicable explicit laws.

Repeated boundary occurrence and repeated establishment remain separate from information sameness (Sections 11–12).

---

## 11. Explicit Occurrence Distinction

If receipt count, receipt time, source, submission identity, observation identity, or another occurrence distinction is
factual meaning, that distinction must be explicit.

For example:

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

The two Facts differ because declared factual material differs. A coordinate used to express occurrence has no special
identity authority by virtue of its name or value domain. Its actual value must be supplied by the applicable formation
law; Fact does not generate an occurrence identifier.

Kontrakt must not preserve occurrence through hidden message identity, host object identity, allocation history,
delivery history, runtime reference, or storage identity.

If two occurrences must remain distinct while every declared factual coordinate remains equal, the Fact surface does not
yet express the required distinction.

Fact owns no independent Established Occurrence Material under ADR-0063 Section 4.4.1. Occurrence distinctions that
belong to factual information are expressed by the declared coordinates as above. The results of independent
formation, standing judgments and Operation applications remain with their respective owners. Repeated applications
are not given a hidden Fact Occurrence Identity, and this distinction introduces no separate runtime Material family
or Occurrence Projection. Section 12 governs the information resulting from lawful establishment.

---

## 12. Set-Like Fact Semantics

For Fact authority within the same established information context, repeated establishment of the same Fact under
Section 10 does not create implicit multiplicity.

```text
{ F, F } = { F }
```

This is a Contract observation law, not a requirement for a particular set data structure or a prohibition on repeated
lawful establishment. Storage duplicates and delivery duplicates do not create additional factual meaning. Information
sameness does not merge independent authority scopes or grant authority in another scope (Section 10.1).

A repeated formation or establishment may have its own occurrence and applicable judgments. Sameness alone neither
refuses the repeated establishment nor permits the compiler to omit those judgments or their required observations.
Other Contract obligations remain independent of the resulting informational multiplicity. Any reuse must satisfy
Section 20.

When count or repeated observation is factual meaning, it must be expressed explicitly (Section 11). Multiplicity and
order belonging to a constituent value remain governed by Section 6.3; the set-like law does not alter them. Internal
reference counts or incremental weights do not become factual occurrence counts and do not authorize removal of
Established Facts.

---

## 13. Derived Identity and Physical Keys

Kontrakt may derive physical keys from the canonical information under Section 10. A digest, HID, intern key or index
key is derived representation material, not a logical identifier assigned to a Fact. No hash algorithm, canonical byte
encoding or interned representation is required by Fact.

Compact key equality establishes at most a candidate match. A comparison may reject sameness on key inequality only
when the keys are valid and comparable under a common derivation basis that preserves equality of the information being
compared. Keys produced under different derivation or encoding bases cannot establish factual difference merely by
differing. A candidate match requires exact verification; the probability of a collision does not discharge this
obligation. No key collision may merge different Facts.

An exact comparison may use the resolved structure directly or exploit already verified immutable material. A physical
reference can establish sameness only where its identity is known to denote the same exact information and its backing
remains valid for the observation (Section 20). Neither a reused address nor an unverified handle provides such
evidence.

A change in the hash algorithm, physical encoding or key layout does not itself change Fact meaning or Version. The
metadata identity design of ADR-0041 does not impose its encoding and HID suite on every runtime Fact. Section 20
governs
the security and reuse conditions of any derived representation.

---

## 14. No Fact-Owned Coexistence Restriction

Fact expresses individual information. It does not declare unique coordinates or coordinate tuples, nor does it own a
restriction on the coexistence of different Facts. Sharing some factual values is not a ground for refusing one Fact,
merging it with another or replacing existing information. Section 12 governs identical information only.

The absence of a Fact-owned coexistence restriction does not establish the legality of a formation or change under
other applicable Contracts. Fact grants no replacement, supersession, currentness or relationship authority (Sections
7 and 15). No corresponding coexistence obligation is introduced into Invariant or another Contract by this decision.

---

## 15. Fact and Operation-Specific Change

Fact defines factual material, not which Operation may consume or change a Fact or how a Change Proposal is formed.
The selected Operation's explicit bindings own its factual participation and change meaning. Vocabulary membership
alone grants none of those permissions.

Declared Operation Result Material that is not established as Fact remains outside Fact authority. Calculated values
do not become Facts merely because an Invariant might find them useful. Kontrakt must not synthesize a summary or
aggregate Fact merely to expose calculated material to another Contract.

---

## 16. Relationship to Invariant

Fact declares what complete canonical factual material may hold authority. Invariant declares one standing integrity
law over one exact Fact kind. The interface-level Invariant catalog declares those laws separately; the Fact surface
does not list every Invariant that may apply.

Invariant may inspect declared factual coordinates under its own law, but does not create them, define Fact sameness
or create cross-Fact relations. Its Fact-local integrity law does not impose a restriction on the coexistence of
different Facts (Section 14).

---

## 17. Relationship to State and Transition

Fact is information; State is an explicit machine condition for movement; Transition is declared movement between
States. A Fact does not carry hidden lifecycle State, and a State label does not own Fact meaning.

A Change Proposal may bind factual change and State movement that must be established together, but their authorities
remain separate.

---

## 18. Relationship to Publication and Output

Established Facts and Fact authority remain inside the Contract Core and grant no outward authority. Publication
under ADR-0058 decides which authoritative exit material may cross the outward semantic boundary; Output Presentation
under ADR-0059 declares the actual outward result shape.

A published presentation is not Fact. If it returns to the machine, it is external material again.

---

## 19. Definition-Time Law

At definition time, Kontrakt must resolve each Fact surface selected directly by the interface under Section 8.

Each admitted Fact surface must have complete resolved meaning under Section 6 before Visible HIR. The seal under
ADR-0071 certifies completeness of the resolved candidate meaning and its required references, not Fact legality. Fact
Definition judgment applies the owning law to that resolved meaning. It must not recover missing meaning by reopening
the source or Type Subsystem.

The compiler must reject hidden membership, runtime discovery, unsupported host behavior, open shape, unresolved factual
distinctions, and any source form whose complete factual meaning cannot be established. A missing interpretation or
failed prerequisite prevents legal entry; a complete candidate that violates the Fact law is judged by the owning
authority. ADR-0063 Section 4.7.2 governs the distinction. The applicable Platform-use and formation failures remain
with their owners, rather than becoming a Fact refusal by implication.

Source coordinates required for diagnostics must remain available through the appropriate separately owned provenance
material (Sections 9 and 19.2). They do not participate in canonical factual meaning or sameness unless explicitly
declared as factual information. Host execution mechanics do not acquire Contract authority.

Executable machine formation and publication require complete, coherent observations of the necessary Established
Fact Definitions through Sections 19.11–19.12 and of the vocabulary selection through its owning law.

### 19.1. Resolved Fact Definition Candidate

Fact specializes the Resolved Contract HIR of ADR-0071. A Fact Definition Candidate expresses the complete resolved
Definition meaning before Fact Definition Establishment. It identifies the exact Fact Authority and resolved Version
candidate coordinate under their applicable laws, but establishes neither a Definition nor actual Fact authority.
The IDL selection remains a separate Binding Candidate (Section 19.4).

Two components determine the Fact Definition meaning:

**Direct Factual Coordinate Surface**

Identifies every declared direct factual coordinate by its nominal identity within the Definition. The surface preserves
its complete composition, independently of declaration order or physical position.

**Resolved Factual Value Meaning**

Defines the completed value meaning of each direct coordinate under Section 6, including its constituent distinctions
and relations. Any semantic basis that determines this meaning must be preserved. The meaning may be represented
directly or through an exact producer-owned semantic reference that makes the completed meaning observable. A reference
to an unresolved type or source declaration is insufficient.

These components determine informational Definition meaning. Authority and Version identify the Definition under their
owning laws; they are not factual coordinates. Selection-only context, formation, standing Invariant laws and Operation
participation do not determine Fact Definition meaning. Fact requires no separate formation law, judgment expression,
relation schema or occurrence requirement in its Definition Candidate. Common value meaning remains with its owner.

The Candidate must retain the entire resolved surface, including a completed empty surface or any resolved multiplicity
and conflicting coordinate declarations, where such material is expressible after source interpretation. The
nonempty, unique-name requirement of Section 6 determines legality, not the ability to represent a completed violation.
An unresolved coordinate or value meaning is not a completed violation and cannot be supplied by Establishment.

### 19.2. Information Retention and Semantic Equality

The complete Definition Candidate meaning must be observable through the Fact-owned Definition Candidate Projection
under ADR-0071. Coverage includes every direct coordinate and its complete value meaning. Exact direct semantic
references preserve the referenced meaning's ownership; they do not permit a consumer to reconstruct it from source,
IDL selection or unresolved Type Subsystem material.

Source qualification and interpretation must be completed at their owning boundaries under ADR-0079. Where a Fact-owned
judgment requires additional resolved evidence, that evidence must remain legally observable at the judgment boundary.
In particular, the information needed to enforce Section 7 must not be lost through structural representation or type
alias resolution. Evidence does not become a Definition determinant merely because a judgment requires it. Host
binding and diagnostic provenance remain separately owned where needed; removing them from informational Definition
meaning does not authorize their loss from other required compiler observations.

Fact Definition Meaning Equality compares the complete direct coordinate surface and the corresponding resolved value
meanings under their owning laws. It preserves nominal distinctions, all meaning-determining relations and semantic
bases, and any resolved multiplicity or collision in the Candidate surface. Direct coordinate declaration order and
physical representation do not participate in this comparison. Constituent order and multiplicity remain governed by
Section 6.3.

Equal Definition meaning does not identify the same Candidate Reference or merge independent Definitions. It also
does not establish equality of a broader Candidate observation that contains different required evidence. Whole-Fact
sameness under Section 10 and current reuse qualification remain separate. Physical sharing of completed meaning is
permitted when it preserves these distinctions and the applicable validity and security requirements.

### 19.3. Typed Fact HIR Candidate References

Fact owns the following reference domains within the common HIR protocol of ADR-0071:

```text
FactDefinitionCandidateRef
ResolvedFactCoordinateRef
FactIDLBindingCandidateRef
```

`FactDefinitionCandidateRef` identifies one exact current Fact Definition Candidate. `ResolvedFactCoordinateRef`
identifies one direct factual coordinate through its exact `FactDefinitionCandidateRef` and nominal coordinate
identity. The same coordinate name in different Definitions does not identify the same coordinate. Source position,
physical index and nested value constituents do not determine this reference. A resolved coordinate collision must
remain observable for the applicable judgment; no ambiguous coordinate name may be resolved by choosing an arbitrary
occurrence.

`FactIDLBindingCandidateRef` identifies one exact current IDL selection occurrence under Section 19.4. It is distinct
from both the selected Definition Candidate Reference and the selecting `ContractInterfaceRef`, which belongs to the
common HIR protocol.

These are compiler-semantic references, not factual values or references between Facts. Fact owns the meaning of its
Definition and coordinate references consumed by Lowering under ADR-0067 Section 11.1. A Candidate Reference is
not an Established Definition Reference and establishes no Required Basis or actual Fact authority. The reference
domains impose no particular encoding, physical field set or storage strategy.

### 19.4. Fact IDL Binding Candidate and Vocabulary Selection

Each direct selection in the interface's Fact vocabulary (Section 8) forms a distinct Fact IDL Binding Candidate
whose target is one exact Fact Definition Candidate. The vocabulary is the complete selection relation of the interface,
not an independent Definition or Contract authority. Different selections may designate the same Definition Candidate
under the common Definition and Binding laws of ADR-0071.

A Fact IDL Binding Candidate expresses the selecting interface, the Fact vocabulary role and the exact target
`FactDefinitionCandidateRef`, preserving the identity of the selection occurrence. No additional binding-owned
qualifier is required by the current Fact vocabulary law. The Binding does not copy the Definition meaning or absorb
unrelated interface context.

`FactIDLBindingCandidateRef` designates the Binding Candidate itself; `ContractInterfaceRef` identifies its selecting
subject. This distinction does not require a self-reference field or a separately stored interface reference when the
same exact relation is represented structurally.

The entire vocabulary selection must be observable from the exact interface and its Fact role. For a present vocabulary,
this observation includes every selected `FactIDLBindingCandidateRef`, rather than only one selected reference or a
set of distinct Definition targets. Omission and explicit absence remain governed by the applicable Interface and
common selection laws; unresolved selection is not explicit absence.

The observation preserves each selection occurrence until the applicable judgment, including distinct occurrences with
the same resolved target. Its semantic order is immaterial. The established vocabulary remains duplicate-free under
Section 8. The complete selection observation does not introduce another Primary HIR subject, establish a Binding or
grant participation authority.

### 19.5. Resolved Fact HIR Candidate Protocol

Fact specializes the read-only Resolved HIR Candidate Protocol of ADR-0071. The reference domains of Section 19.3
and the common `ContractInterfaceRef` support the following observations:

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

The complete Definition Candidate Projection exposes the Authority and resolved Version candidate coordinate, the
entire direct factual coordinate surface and the completed value meaning of each coordinate (Section 19.1). Required
resolved evidence remains observable under Section 19.2. The complete Binding Candidate Projection exposes the
selecting interface, Fact role and exact target of one selection occurrence under Section 19.4. Neither observation
requires a redundant reference field or a copy of separately owned meaning.

**Fact Candidate Coordinate Surface Projection**

Exposes the entire direct coordinate surface of one exact `FactDefinitionCandidateRef`. It preserves each nominal
coordinate, its exact `ResolvedFactCoordinateRef` when unambiguously referenceable, and any resolved multiplicity
or collision required for judgment. This observation is complete for coordinate coverage without exposing the value
meaning of every coordinate. Lowering may use it to determine coverage of its target surface under ADR-0067.

Equality compares the nominal composition and multiplicity of the surface, independently of declaration order. A
change confined to a coordinate's value meaning does not by itself change this observation. A completed empty or
conflicting surface remains observable under Section 19.1; an ambiguous coordinate is not made exact by selection
of an arbitrary occurrence.

**Fact Selected Coordinate Meaning Projection**

Exposes the exact coordinate designated by one `ResolvedFactCoordinateRef` and its complete resolved factual value
meaning. The observation includes the distinctions, constituent relations and semantic bases required by Section 6.
Equality preserves the selected coordinate and its meaning under their owning laws. A nested constituent is not
another direct Fact coordinate. Lowering may consume this observation for the formation meaning of an individual
mapping without acquiring unrelated coordinate meanings.

**Fact Vocabulary Selection Projection**

Observes the complete Fact selection of one exact `ContractInterfaceRef` at its Fact vocabulary role. A present
selection exposes every `FactIDLBindingCandidateRef` occurrence under Section 19.4. The complete Binding Candidate
Projection provides the exact target of each occurrence. Equality preserves the selecting subject, role and complete
selection relation, including multiplicity but not declaration order. The observation is multi-selection, rather
than the single-reference alternative used by single-selection roles. Explicit Absence follows the applicable
selection law; an unresolved or unavailable selection cannot be treated as absence or as a completed smaller
vocabulary.

Each Projection is complete only for its declared observation. Complete Definition and Binding observations compare
all meaning they expose, including required evidence where it is part of the observation. Fine-grained equality
compares only the meaning of that observation, under Section 19.2 and ADR-0071. A consumer must preserve exact
subject correspondence and current reference validity when comparing observations across generations. Equality alone
does not establish current reuse eligibility. An omission from a narrower observation is not semantic absence;
absence of a coordinate may be concluded only from an observation complete for that question.

Projections create no additional Primary HIR subject or authority. Their logical distinction permits bulk access,
shared immutable backing and fine-grained dependency tracking without requiring separate physical records or Query
nodes. Observation coherence, availability, reference lifetime and Protocol evolution follow ADR-0071.

### 19.6. Fact HIR Seal and Establishment Handoff

A Fact Definition Candidate may become Visible HIR only when:

- Its exact Fact Authority and resolved Version candidate coordinate are known.
- The entire direct coordinate surface and all its value meanings are resolved under Sections 19.1 and 19.2.
- Required source interpretation and qualification have completed at their owning boundaries under ADR-0079.
- Every required semantic reference is exact and valid, and the required Protocol observations are complete within a
  coherent HIR generation.

Source Eligibility under Section 9 is a prerequisite to lawful HIR formation. A forbidden default, for example, must
not be erased to produce an apparently eligible declaration. An unresolved type prevents completion. A resolved empty
coordinate surface, when otherwise eligible for HIR, instead preserves the completed meaning for Fact Definition
judgment. Seal verifies resolution and integrity rather than satisfaction of the Fact Definition law.

A Binding Candidate must resolve its exact selecting interface, Fact role, selection occurrence and single Definition
Candidate target. Its target must be referenceable under the common HIR seal and visibility laws. The entire vocabulary
observation is complete only when all of its selection occurrences have been resolved and are legally observable.
Completed repeated selections remain present for the applicable judgment; an unresolved selection must not be dropped
from that observation.

Under ADR-0071, independently sealed Definition or Binding Candidates may be visible within a coherent generation even
when another independent Candidate fails to complete. A complete vocabulary observation requires its entire scope to
be sealed; visibility of an individual selection does not establish completeness of that scope or authorize machine
publication. Continuation requires valid inputs and a trustworthy compiler domain. Recovery or poison material,
unknown required semantic extensions and failed visibility follow the common HIR laws.

Fact Definition Establishment consumes legal HIR observations and the authoritative Version prerequisite under
ADR-0063 and ADR-0053. The complete Definition Candidate Projection may be used, or a combination of producer-defined
Fine-Grained Semantic Projections and required resolved evidence that supplies every observation necessary for the
judgment. An incomplete input prevents legal entry. Establishment applies the Fact law to this meaning; it cannot
recover a missing part through source, Type Subsystem interpretation, host behavior, IDL selection or compiler layout.

Early static verification is permitted when the required meaning and legal judgment prerequisites are already
available. Moving or eliminating a physical calculation cannot change its owning judgment, entry conditions or
result attribution. ADR-0063 Section 4.7 and ADR-0071 govern this handoff.

### 19.7. Fact Definition Establishment Judgment and Result

The Fact Definition judgment determines whether one exact, complete Fact Definition Candidate satisfies the factual
information law of Sections 4, 6 and 7. It judges the entire direct coordinate composition and the admissibility of
its completed value meanings. These include constituent closure, the V1 acyclic schema requirement, exact factual
distinctions and authority isolation. The judgment consumes the completed interpretation and qualification of the
applicable value meanings under Sections 6 and 9, rather than repeating their owners' judgments.

Legal entry requires the observations of Section 19.6 and the authoritative Version prerequisite. The Version law may
establish the required binding or supply an already-established exact binding under ADR-0063 Section 6.3. A Fact IDL
Binding Candidate or complete vocabulary selection is not an input to this Definition judgment. This independence
does not authorize acquisition of unselected source declarations or establish actual factual material.

The judgment has two Fact-owned semantic results:

```text
complete resolved Candidate
+ legal Fact Definition meaning
    -> Established Fact Definition

complete resolved Candidate
+ meaning that violates the Fact Definition law
    -> Fact Definition Refusal
```

The minimum successful establishment unit is one entire Definition. Its coordinates are not independent authorities.
Partial verification or parallel evaluation may support the judgment but cannot establish a subset of its Definition
meaning as authoritative. An unsuccessful establishment creates no partial Definition authority, and material that
has not been established cannot be published or reused as if it were established. Failure does not revoke independent
established Definitions or an independently established Version Binding. Temporary backing and other physical
resources may be discarded or recovered without altering these semantic results.

A failed prerequisite prevents legal entry and retains its own result attribution. Source Eligibility, interpretation,
HIR Seal and Version failures are not Fact Definition Refusal. A violation of the vocabulary selection law belongs to
the selection judgment of Section 19.9. If compiler failure leaves the exact judgment result indeterminate, neither
success nor refusal may be fabricated. ADR-0063 Section 4.7.2 and ADR-0074 govern the common unsuccessful-result and
continuation boundaries.

A subsequent failure to form or expose the Canonical Contract World does not undo an already-established Definition
in the current compilation. Current Establishment, World visibility and durable Version-history publication remain
distinct under ADR-0063 Sections 6.9 and 10.8. Retained material from unsuccessful compiler work is at most reuse
evidence unless current validity and authority are established under the applicable laws. Failure atomicity does not
prescribe a physical transaction, rollback algorithm or crash-recovery mechanism.

### 19.8. Definition-Time Required Basis and Applicability Boundary

Fact Definition Establishment owns no ADR-0063 Required Basis Law, Basis Resolution, Basis Binding, Complete Basis,
separate Definition Applicability judgment, arbitration or Composition judgment. These categories are
`NOT-APPLICABLE` to the current Fact Definition law. No Established Input, Lowering, Invariant or actual Fact is
required for this judgment.

A semantic basis that determines factual value meaning remains part of the resolved Definition meaning under
Section 19.1. Compiler interpretation dependencies alone are not Contract Required Basis. Fact's own Version
prerequisite follows ADR-0053 and ADR-0063 without introducing another Fact-owned Basis law. These distinctions concern
Definition-time Establishment only; the independent judgments and composition needed for actual Fact establishment
remain governed by Section 5.

### 19.9. Fact Vocabulary Selection Legality

The Fact vocabulary law of Section 8 applies to the complete selection relation of one exact interface. Its duplicate
check is separate from the judgment of any individual Fact Definition. Repeated selections of the same exact
Versioned Fact Definition are unlawful, regardless of source spelling or physical Candidate Reference encoding.
Equal information structures of independently identified Definitions do not constitute a duplicate selection.

The complete resolved selection preserves the occurrences required for that judgment under Sections 19.4–19.6.
An unsuccessful vocabulary judgment concerns the selection relation, not the Definition meaning of any selected
Fact. Successful selection legality alone does not establish a Definition, an authoritative interface binding or
participation authority. The applicable Interface and Composition laws govern authoritative selection and its
prerequisites. Fact introduces no independent vocabulary Definition, Version or Established Material family.

### 19.10. Established Fact Definition Material

Successful Fact Definition Establishment produces one complete Established Fact Definition. Its semantic material
has two components:

**Identity**

The exact `EstablishedFactDefinitionRef` identifies the Definition under its Owning Fact Authority Binding and
Version Binding.

**Meaning**

The complete Established Fact Definition Meaning preserves the entire factual coordinate surface and the complete
value meaning of every coordinate defined by Sections 6 and 19.1, including the direct established relation to
each coordinate. It retains the exact semantic-law and constituent relations that determine
those meanings. The completed meanings and their separately owned relations may be observed directly or through
exact semantic references without loss, duplication of authority or renewed interpretation. No additional constraint,
formation meaning or factual information is established merely by Definition Establishment.

Fact owns the following Established Definition reference domains:

```text
EstablishedFactDefinitionRef
EstablishedFactCoordinateRef
```

A Fact Authority owns one independently addressable Definition per Version. Its exact Authority and Version Binding
therefore determine `EstablishedFactDefinitionRef`; no Authority-Local Definition Coordinate is required. The
Owning Authority Binding is directly recoverable by a semantic consumer without inference from an interface, host
declaration or physical containment. ADR-0063 Section 6 and ADR-0053 govern identity, conflicting meanings under one
identity, and Version history.

`EstablishedFactCoordinateRef` identifies one direct factual coordinate through its exact
`EstablishedFactDefinitionRef` and nominal coordinate identity. This relation is part of the Definition's established
meaning, not a separate coordinate authority. The coordinate has no independent Version; declaration order and
physical location do not determine its identity. Lowering consumes the exact Established Fact Definition as its
Required Basis under ADR-0067 Section 11.6 and uses the Fact-owned coordinate meaning without re-establishing it.

The authoritative Definition Reference becomes legally observable only when the complete Definition and its required
identity relations have been coherently established. A Candidate Reference cannot be promoted or retyped into an
Established Reference. Distinct Candidates may correspond to the same established identity and meaning after lawful
Establishment or currently qualified reuse under the owning laws. Neither an enduring one-to-one correspondence nor
retention of the originating HIR is required. The Definition meaning and reference must remain interpretable
independently of that HIR.

These bindings and relations specify what must be observable from the Definition's identity and meaning, not a
mandatory record layout. Shared immutable backing may represent both Candidate and
Established meaning without transferring authority. Physical relocation, encoding or reclamation must preserve legal
observation and reference validity under ADR-0063. Section 19.7 governs failure atomicity.

Fact establishes no separate Definition Material for its vocabulary or direct coordinates. Interface selection,
Formation, Invariant and participation remain with their respective owners; the Fact Definition need not retain a
relation to every consumer. Actual Fact establishment under Section 5 is separate from Definition Establishment.

### 19.11. Established Fact Semantic Protocol

Fact specializes the Established Semantic Protocol of ADR-0063 Section 11. The two typed Established reference
domains of Section 19.10 designate the Definition and its direct coordinates. The projection catalog is:

```text
Established Fact Definition Projection
Direct Established Relation Projection

Fine-Grained Established Fact Projections
    Established Fact Coordinate Surface Projection
    Established Fact Selected Coordinate Meaning Projection
```

**Established Fact Definition Projection**

Exposes the complete Established Fact Definition designated by one exact `EstablishedFactDefinitionRef`. This
includes its owning Authority and Version, complete Definition Meaning and all direct established relations of
Section 19.10. Equality preserves the entire observation under the respective owning laws. A consumer must not
recover any part of this meaning by reinterpreting HIR, source or Host Binding.

**Established Fact Coordinate Surface Projection**

Exposes the entire direct factual coordinate surface of one exact `EstablishedFactDefinitionRef`, including the
exact `EstablishedFactCoordinateRef` of each coordinate. The surface is nonempty and contains no duplicate coordinate
names under Section 6. The observation is complete for coordinate coverage; it need not expose every coordinate's
value meaning. Equality compares the exact Definition and the full nominal coordinate composition and its relations,
independently of declaration order and physical representation. Lowering may consume this surface to judge coverage
under ADR-0067; Fact does not perform the mapping judgment.

**Established Fact Selected Coordinate Meaning Projection**

Exposes the coordinate designated by one exact `EstablishedFactCoordinateRef` and its complete resolved factual
value meaning, including the constituent distinctions and meaning-determining relations of Sections 6 and 19.10.
Equality preserves the selected coordinate and its value meaning. Nested constituents remain part of that meaning,
not separately referenceable direct Fact coordinates. Lowering may consume this projection to judge a mapping's
formation meaning. Execution Formation may use the completed meaning to select a lawful representation without
reinterpreting the authored type.

**Direct Established Relation Projection**

The common projection of ADR-0063 Section 11.4 exposes the exact direct relations preserved by Section 19.10.
It preserves their source, relation and target distinctions without creating a new relation or authority. Fact
requires no separate graph model or inferred inverse ownership. Vocabulary selection belongs to the applicable
Interface and Composition laws; the HIR vocabulary selection projection of Section 19.5 is not an Established Fact
Projection.

Each projection is complete only for its declared observation. An omission from a narrower projection is not
semantic absence. A complete coordinate surface may establish the absence of a direct coordinate, but an invalid
reference, unavailable backing or failed observation cannot establish that absence. Observation coherence,
availability and failure attribution follow ADR-0063 Sections 11.5–11.7 and ADR-0074.

Semantic equality follows the exact identity and meaning exposed by each projection. Equal value meanings do not
make distinct Versioned Definition or coordinate references equal. A calculation concerned only with a common value
meaning may be reused under its own current-validity law without equating the broader Established observations.
Reuse of a complete surface also requires evidence of its current closure, not just the validity of the previously
observed members.

Compiler consumers select existing legal observations according to their own requirements. Analysis, verification,
execution formation and generated API formation may derive their own products; those products are not new Fact
Projections. Additional Host Binding or provenance requirements remain with their owners. The Protocol does not
introduce an Occurrence Projection, runtime Fact Candidate Projection or other runtime material family. It imposes
no runtime interface calls, separate physical records, allocations, Query nodes or mandatory hash calculations.
Bulk access, fine-grained observation and shared immutable backing may realize the same semantic boundaries under
ADR-0063 Section 11.

### 19.12. Canonical Contract World Placement

The Canonical Contract World of ADR-0063 Section 10 makes complete Established Fact Definitions legally observable
to compiler consumers through Section 19.11. Each Definition's authoritative identity, complete meaning and required
direct established relations must be coherent before this handoff. HIR Candidates, actual runtime factual values and
occurrence-specific material do not become part of the Definition-world substrate. Source provenance remains adjacent
material; downstream compiler products retain their own ownership.

World visibility does not establish a Fact Definition again or create a new Contract authority. The compiler's
Canonical Contract World is distinct from the Policy-owned Contract World. Co-location or linking establishes no
Policy membership, vocabulary selection, Fact relation or other Contract-owned binding. A lawfully established
selection or composed relation is observed under its owning law, not inferred from the presence of Definitions.

World assembly, linking and physical representation changes preserve the identity and complete meaning of each
unchanged Established Fact Definition. The compiler's World generation, physical placement or aggregate identity does
not determine that Definition's identity or the sameness of actual Facts (Section 10.1). Direct relations retain the
states defined by their owning laws. An unresolved required Definition relation prevents lawful visibility;
absence of a future application is not an incomplete Definition.

Independent complete Definitions may become visible without waiting for unrelated compiler work when the required
consumer closure is coherent under ADR-0063 Section 10.7. Partial World coverage may be lawful; partial Established
Fact Definition meaning is not. Consumers must obtain their own complete, current and coherent required observations
before entering their work. Physical assembly may combine validated backing without exposing an inconsistent view.

World formation or observation failure is compiler-owned. It prevents dependent work from entering when required
meaning is unavailable, but does not revoke an independently established Definition in the current compilation.
Current Definition Establishment, World visibility and durable Version-history publication remain distinct under
Section 19.7 and ADR-0063 Sections 6.9 and 10.8. Retained backing does not confer authority on a later invocation
without current qualification. Physical storage, linking and reclamation mechanisms remain replaceable realization.

---

## 20. Optimization Boundary

A backend may choose any representation compatible with the complete canonical Fact material and deterministic legal
observation. Direct comparison, SIMD, indexing and immutable sharing may accelerate Section 10 without changing the
information it distinguishes. Section 13 governs the use of derived keys. No particular serialization, interning,
storage layout or comparison algorithm is required.

The realization must preserve the stability required by Section 5 throughout comparison and use. Sharing, relocation
and physical reclamation must not cause a reference to observe different information, or permit a stale reference to
establish sameness. Selection among equivalent valid physical backings is implementation; acquisition or scheduling
order cannot establish factual meaning or authority.

Exact comparison must remain sound in the presence of collisions. Resource safety is a separate realization obligation:
collision verification alone does not protect against adversarial comparison or lookup cost. Applicable Budget and
Capacity obligations remain with their owners. A resource or compiler failure must not be converted into an invented
sameness result or a Fact refusal. Routing and physical iteration order must not determine a Contract observation.

Physical sharing and derived representation material remain subject to the applicable security boundary. Sameness does
not authorize sharing across independent trust boundaries, and a sharing optimization must preserve information and
authority isolation, including against unauthorized inference through observable reuse behavior. Internal keys, handles
and cache observations have no automatic outward or diagnostic authority. Their disclosure remains subject to the
applicable Publication and diagnostic laws. Concrete isolation, comparison and resource-protection mechanisms remain
realization decisions.

A matching Fact may support reuse of its material, but it does not establish the current validity of an earlier
judgment, establishment or participation. Reuse must satisfy the exact observation and current-validity requirements
of ADR-0063 Section 11.7 and ADR-0075 Sections 13 and 22. The optimized path must preserve the legal observation of
the deterministic clean path. These requirements do not prescribe a universal cache key or introduce new factual
coordinates.

Derived structures remain implementation. They may not add Fact relations, Fact multiplicity, Fact currentness,
participation authority or cross-Fact semantics that the Contract does not declare.

### 20.1. Compiler Conformance and Independent Verification

The optimization boundary applies throughout the realization of the Fact Contract, not only to actual value
comparison. The compiler must preserve the required semantic observations, authority boundaries and result
attribution at each of the following seams:

- Source interpretation and HIR formation preserve complete resolved meaning, precise references and the evidence
  required by the owning judgments (Sections 6, 9 and 19.1–19.6).
- Definition Establishment preserves judgment ownership, the complete authoritative identity and failure atomicity
  (Sections 19.7–19.10).
- World visibility, linking and Established Protocol realization preserve the complete Definition and its legal
  observations (Sections 19.11–19.12).
- Execution formation, optimization and backend realization preserve the runtime obligations of Sections 5–7 and
  10–13, including immutability, exact factual distinctions, independent judgments and indivisible establishment.

No optimization may weaken those obligations. A fused execution or reused computation must preserve the owning
judgments and their required meaning and failure boundaries; physical scheduling cannot supply semantic distinctions.
The freedom to choose a representation does not permit changing the information of an Established Fact (Section 5.4).

Conformance must support both comparison with the deterministic clean path and independent verification against the
Fact Contract itself, as required by ADR-0071 and ADR-0075. Agreement between two realization paths is insufficient
when they share a defect in interpretation or judgment. A verification product checks conformity; it does not create
Fact authority or replace an owning judgment. This requirement imposes no mandatory duplicate computation during
governed execution.

Conformance must cover the exact reference kinds and current validity of observations as well as value equality and
complete observation closure. In particular, a stale or wrong-kind handle, corrupted backing, poisoned cache or
partial publication must not supply authoritative material. The collision, resource and isolation laws above also
apply to the HIR and Established Protocol realizations. Compiler failure, absence, non-entry and Contract refusal
retain their distinct meanings and owners.

Semantic correctness, security conformity and performance are separate verification concerns. A valid semantic result
does not establish resource safety, information isolation or efficiency. Compiler Design owns the specific algorithms,
verification methods, storage choices and performance measurements. V1 and V2 may use different realization strategies
provided they preserve the same Contract meaning and satisfy the applicable current-validity and security requirements.

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

The Contract Core contains explicit immutable factual meaning rather than implementation objects promoted to authority.
The vocabulary is finite and explicit. Host source declarations remain convenient frontend evidence without becoming
runtime Contract identity, and backend representation remains replaceable.

Fact sameness is deterministic and independent of user-defined equality, object identity, allocation history and
storage. Repeated identical establishment creates no hidden multiplicity; occurrence distinctions remain possible
when declared as factual material.

Fact owns no user-declared uniqueness or other coexistence restriction on different information. Informational sameness
does not itself confer authority across Worlds or scopes. Cross-Fact relations remain explicit responsibilities of the
Contract that actually needs them.

