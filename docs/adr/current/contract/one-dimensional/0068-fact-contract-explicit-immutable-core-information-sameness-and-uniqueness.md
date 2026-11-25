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

A Fact carries no hidden object or occurrence identity.

A request, claim, measurement, report, or observation may itself be factual material. Declaring that such an event
occurred does not make the content of the request, claim, measurement, report, or observation stronger than the Fact
declaration permits.

Only established Facts belong to the core as factual Contract material.

Transient implementation values may exist during realization. Their existence does not give them Fact authority.

---

## 2. Problem

Without an explicit Fact Contract, factual authority can collapse into whatever implementation happens to store.

A class may be treated as fact because it has immutable-looking fields.

A database row may be treated as fact because it persisted.

A DTO may be treated as fact because it crossed a boundary.

A returned object may be treated as fact because an Operation produced it.

Object identity, allocation history, repeated delivery, custom equality, repository membership, or backend layout can
then become hidden factual meaning.

Those mechanisms are implementation.

The machine instead needs an explicit factual surface that can survive representation replacement without changing what
the machine knows.

It also needs an exact sameness law.

Repeated establishment of the same factual material must not create hidden multiplicity.

When occurrence, count, provenance, or repetition matters, that distinction must itself be factual material.

---

## 3. Decision Drivers

Fact must be Contract material rather than a class, row, DTO, record, Value Object, entity, object graph, repository
entry, message identity, or storage layout.

Everything the Contract Core contains as factual material must be available as explicit immutable Fact material.

A host declaration may nominate factual coordinates when completed source interpretation and platform ratification
preserve their required meaning (Sections 6 and 9). Fact does not maintain its own inventory of value forms.

Kontrakt must not require a proprietary wrapper merely to make a value look internal.

Fact vocabulary membership must be explicit.

Package membership, directory placement, classpath discovery, reflection scanning, runtime collections, inheritance, and
neighboring source declarations must not add Fact kinds implicitly.

A named host declaration may group exact Fact surface references as frontend evidence. It must disappear as authority
after resolution.

Fact meaning must remain separate from physical representation.

Fact sameness must preserve the complete information without depending on host equality or hashing. Fact owns no
independent object identity or restriction on the coexistence of different Facts.

Currentness, supersession, history, topology, and cross-Fact relations must not be synthesized from Fact sameness or
derived physical keys.

Facts must not contain semantic references to other Facts.

A backend may derive compact keys and use any deterministic storage layout. Those mechanisms may not create or erase
factual distinction.

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
recursive value schemas are outside its scope. This does not prohibit a complete relation value describing cyclic
relations, provided it contains no
recursive value meaning or semantic reference to another Fact (Section 7).

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
Fact authority. Neither a domain-named host type nor a user-defined object model supplies an independent Core value
authority (Section 9).

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

The declared Fact surface may expose a coordinate that another Contract explicitly uses as an operand.

That later Contract owns the relation. Fact does not.

A value lawfully obtained from a participating Fact may be expressed as part of a different Fact under the applicable
formation law. The resulting information must be complete in its own right and must not depend on continued access to
the original Fact. Participation is separately authorized under Section 15. Physical sharing of immutable backing is
permitted under Section 20, but grants no additional observation or participation authority.

A descriptive value must not retain capability or resource authority over its denoted target. This distinction follows
ADR-0073 Sections 5.3–5.5, regardless of the carrier used. The law concerns the resolved meaning and its lawful use;
it does not establish a general ability to discover every possible external use of arbitrary factual values.

Currentness, cardinality across separately established Facts, supersession, grouping, continuity, history, population,
sequence, interval relation, and topology are not implicit properties of every Fact.

When that meaning is required, it must belong to an applicable Operation, Change, State, Transition, another explicit
Contract authority, or explicit factual material contained wholly within one Fact.

The Fact Contract does not invent that owner.

---

## 8. Fact Vocabulary

The enclosing interface declares one exact Fact vocabulary.

The vocabulary is authored through a restricted host-language declaration rather than package selection, class-literal
collections, runtime registration, or a separate rich IDL body.

A Kotlin frontend may express the declaration as an uninstantiable type signature:

```kotlin
class AccountFacts private constructor(
    depositOperationInput: DepositOperationInput,
    depositRecorded: DepositRecorded,
    accountOpened: AccountOpened,
    balance: Balance,
    withdrawalRecorded: WithdrawalRecorded,
    accountClosed: AccountClosed,
)
```

The constructor is never invoked.

The class is not the Contract.

Its parameter type positions nominate exact Fact surface declarations.

Parameter names do not create Fact meaning.

Source order does not create Fact meaning.

Constructor identity does not create Fact meaning.

Allocation does not create Fact meaning.

Generated JVM shape does not create Fact meaning.

Host class identity does not become Fact identity.

Resolution and Lowering retain an order-independent, duplicate-free canonical Fact vocabulary.

An equivalent restricted Java declaration may provide the same material.

The vocabulary declaration contains no properties, methods, initialization, inheritance, callbacks, computed membership,
package selectors, `KClass`, `Class`, reflection, or runtime discovery.

Moving another declaration into the same package does not make it Fact.

Only explicit vocabulary declaration changes Fact eligibility.

The interface binds that vocabulary once:

```text
interface AccountService {
    facts AccountFacts
    invariants AccountInvariants

    operation deposit(...)
    operation withdraw(...)
    operation close(...)
}
```

Fact vocabulary membership declares which Fact kinds may be established in the core.

It does not grant universal participation authority.

---

## 9. Host Frontend Evidence

A host declaration may provide evidence of a closed factual surface when its meaning can be fully interpreted under
Section 6 and its use preserves that meaning under ADR-0073. Host category, class identity or an immutable-looking
carrier does not determine eligibility.

Kontrakt does not require `CoreInt`, `KontraktText`, or a user-authored Value Object merely because information exists
inside the core.

A domain-named host declaration remains frontend evidence. Kontrakt does not carry a Kotlin data class, Java record, or
another domain object into the Contract Core as the Fact itself. A carrier used to express a structured value must not
import another Fact or its authority (Section 7).

Resolution retains domain-neutral canonical Contract material. The resolved Fact kind identifies the exact Versioned
Fact Definition, whose coordinates retain the value meanings and factual distinctions under Section 6. Applicable
Contract World material is preserved for establishment and applicability under their owning laws, not as an additional
determinant of informational sameness (Section 10). Source coordinates required for attribution remain separate from
factual meaning unless explicitly declared as factual information. Candidate formation realizes the actual values
under that resolved meaning (Section 5).

Host constructors, methods, receivers, object identity, and class layout do not survive as Fact authority.

A backend may realize the same canonical material through generated classes, primitive arrays, packed bytes, tables, or
another deterministic layout.

Those realizations are implementation.

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

Fact defines factual material.

It does not define which Operation changes that Fact.

It does not define how a Change Proposal is formed.

It does not decide whether one Operation may consume or change another Fact.

The selected Operation's explicit bindings own its factual participation and change meaning.

Fact vocabulary membership alone grants none of those permissions.

Declared Operation Result Material that is not established as Fact remains outside Fact authority.

Calculated values do not become Facts merely because an Invariant might find them useful.

Kontrakt must not synthesize a new summary Fact or aggregate Fact merely to expose calculated material to another
Contract.

---

## 16. Relationship to Invariant

Fact and Invariant remain separate.

Fact declares what complete canonical factual material may hold authority.

Invariant declares one standing integrity law over one exact Fact kind.

The Fact surface does not list every Invariant that may apply.

The interface-level Invariant catalog declares those laws separately.

Invariant may inspect declared factual coordinates under its own law.

It does not create the coordinates, define Fact sameness or create cross-Fact relations. Its Fact-local integrity law
does not impose a restriction on the coexistence of different Facts (Section 14).

---

## 17. Relationship to State and Transition

Fact is information.

State is explicit machine condition for movement.

Transition is declared movement between States.

A Fact does not secretly carry lifecycle State.

A State label does not own Fact meaning.

A Change Proposal may bind factual change and State movement that must be established together, but the authorities
remain separate.

---

## 18. Relationship to Publication and Output

Established Facts and Fact authority remain inside the Contract Core.

Fact does not grant outward authority.

Publication under ADR-0058 decides which authoritative exit material may cross the outward semantic boundary.

Output Presentation under ADR-0059 declares the actual outward result shape.

A published presentation is not Fact.

If it returns to the machine, it is external material again.

---

## 19. Definition-Time Law

At definition time, Kontrakt must resolve the exact Fact vocabulary symbol and every exact Fact surface symbol listed by
that vocabulary.

Each admitted Fact surface must have complete resolved meaning under Section 6 before Visible HIR. The seal under
ADR-0071 certifies completeness of the resolved candidate meaning and its required references, not Fact legality. Fact
Definition judgment applies the
owning law to that resolved meaning. It must not recover missing meaning by reopening the source or Type Subsystem.

The compiler must reject hidden membership, runtime discovery, unsupported host behavior, open shape, unresolved factual
distinctions, and any source form whose complete factual meaning cannot be established. A missing interpretation or
failed prerequisite prevents legal entry; a complete candidate that violates the Fact law is judged by the owning
authority. ADR-0063 Section 4.7.2 governs the distinction. The applicable Platform-use and formation failures remain
with their owners, rather than becoming a Fact refusal by implication.

Canonical Fact material must preserve source coordinates required for diagnostics without preserving host execution
mechanics as authority (Section 9).

Fact vocabulary and Fact surfaces must be available in ContractImage-visible material before executable machine
publication.

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
judgment,
establishment or participation. Reuse must satisfy the exact observation and current-validity requirements of ADR-0063
Section 11.7 and ADR-0075 Sections 13 and 22. The optimized path must preserve the legal observation of the
deterministic
clean path. These requirements do not prescribe a universal cache key or introduce new factual coordinates.

Derived structures remain implementation. They may not add Fact relations, Fact multiplicity, Fact currentness,
participation authority or cross-Fact semantics that the Contract does not declare.

---

## 21. Open in This Section

The final host-language authoring restrictions for every individual Fact surface remain open where completed source
interpretation, the law of Section 6 and platform ratification do not already decide them.

The complete Operation authoring surface for existing-Fact participation, additional Fact changes, Operation Result
Material, and Change Proposal binding is outside this ADR.

Persistence layout, cache layout, storage engine, physical deduplication, lookup index, restoration mechanism, and
backend carrier shape remain realization decisions.

---

## 22. Consequences

The Contract Core contains explicit immutable factual meaning rather than implementation objects promoted to authority.

Fact vocabulary is finite and explicit.

Host source declarations remain convenient frontend evidence without becoming runtime Contract identity.

Fact sameness becomes deterministic and independent of user-defined equality, object identity, allocation history, and
storage.

Repeated identical establishment does not create hidden multiplicity.

Occurrence distinction remains possible when it is declared as factual material.

Fact owns no user-declared uniqueness or other coexistence restriction on different information. Informational sameness
does not itself confer authority across Worlds or scopes.

Cross-Fact relations remain explicit responsibilities of the Contract that actually needs them.

Backend representation remains replaceable.

---

## 23. Migration History

This ADR was extracted from the Fact-owned material of ADR-0049.

The extraction preserves the latest Accepted ADR-0049 Fact law as of the 2026-08-05 revision.

The extraction does not intentionally introduce a new Fact authority or change the accepted Fact meaning.

ADR-0049 remains the owner of the shared core-flow relation in which Fact establishment participates.

The later decision in Sections 10–14 supersedes the extracted World-sensitive sameness and declared uniqueness laws.