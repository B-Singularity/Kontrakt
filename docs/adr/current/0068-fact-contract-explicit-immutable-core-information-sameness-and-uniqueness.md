# ADR-0068: Fact Contract, Explicit Immutable Core Information, Sameness, Uniqueness, and Vocabulary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0069: Invariant Contract
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Core Fact Establishment, Change Proposal, Bound Fact Authority, and Outward Handoff
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary

---

## 1. Context

Fact is explicit immutable information established inside the Contract Core.

A Fact is not limited to a final domain value.

It is an immutable proposition upon which the machine may rely under declared factual values, provenance, scope,
version, and other applicable factual material.

A Fact carries no hidden object or occurrence identity.

A request, claim, measurement, report, or observation may itself be factual material. Declaring that such an event
occurred does not make the content of the request, claim, measurement, report, or observation stronger than the Fact
declaration permits.

Fact belongs to the core.

Only established Facts belong to the core as Contract material.

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

Ordinary primitive and closed immutable host types may nominate factual coordinates when they preserve the required
information.

Kontrakt must not require a proprietary wrapper merely to make a value look internal.

Fact vocabulary membership must be explicit.

Package membership, directory placement, classpath discovery, reflection scanning, runtime collections, inheritance, and
neighboring source declarations must not add Fact kinds implicitly.

A named host declaration may group exact Fact surface references as frontend evidence. It must disappear as authority
after resolution.

Fact meaning must remain separate from physical representation.

Host equality and hashing must not own Fact sameness.

Fact-owned uniqueness must remain distinct from Fact sameness.

Currentness, supersession, history, topology, and cross-Fact relations must not be synthesized from Fact identity or
uniqueness.

Facts must not contain semantic references to other Facts.

A backend may derive compact keys and use any deterministic storage layout. Those mechanisms may not create or erase
factual distinction.

---

## 4. Fact Meaning

A Fact is the immutable factual proposition declared by one Fact kind and its complete canonical factual material.

```text
Fact Contract:
    declares what factual material may hold authority inside the Contract Core

Established Fact:
    material that holds authority under that Fact Contract
```

Fact does not decide, validate itself, perform movement, authorize Publication, or hide behavior through host methods.

Fact material may include:

```text
coordinates
value sorts
factual distinctions
bounds
provenance
scope
version meaning
explicit occurrence distinctions
other declared factual values required by that Fact kind
```

The set is source-specific.

Fact does not require one universal runtime carrier schema.

---

## 5. Core Authority

Boundary presentation is not Fact merely because it was admitted or canonicalized.

At the Boundary, proposed factual meaning must already be explicit.

Successful Lowering and the applicable standing and movement judgments establish that meaning as Fact.

A refused establishment places nothing in the Contract Core.

Physical bootstrap, restoration, storage loading, cache loading, or backend reconstruction may realize how established
Fact material becomes physically available.

Those mechanisms do not create another Contract category and do not own factual meaning.

An existing established Fact may already be present in the Contract Core.

Its physical persistence or loading history does not create another Fact.

---

## 6. Fact Surface

A declared Fact surface provides the complete factual coordinates and distinctions required by that information.

The user does not need to author a second separate Fact Contract body merely to repeat identity, equality, or hashing.

The declared surface itself is the source evidence from which canonical Fact material is resolved.

Fact is not universally an entity, event, identifier, persisted record, message, or state snapshot.

Those meanings exist only when the declared factual material says so.

---

## 7. No Hidden Relations Between Facts

Facts do not reference other Facts as semantic objects.

A factual coordinate such as `accountId` is factual material only.

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

That later Contract owns the relation.

Fact does not.

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

A host frontend may use primitives, strings, enums, arrays under an immutable profile, finite products, Kotlin data
classes, Java records, and other approved closed immutable shapes as declaration evidence when their complete visible
shape can be refined.

Kontrakt does not require `CoreInt`, `KontraktText`, or a user-authored Value Object merely because information exists
inside the core.

A domain-named host declaration remains frontend evidence.

Kontrakt does not carry a Kotlin data class, Java record, or another domain object into the Contract Core as the Fact
itself.

Resolution and Lowering retain domain-neutral canonical Contract material:

```text
resolved Fact kind symbol
canonical coordinate declarations
value sorts
factual distinctions
applicable contract-world material
source coordinates required for attribution
```

Host constructors, methods, receivers, object identity, and class layout do not survive as Fact authority.

A backend may realize the same canonical material through generated classes, primitive arrays, packed bytes, tables, or
another deterministic layout.

Those realizations are implementation.

---

## 10. Fact Sameness

Fact sameness is determined by the resolved Fact kind and complete canonical factual material under the same applicable
Contract world.

```text
same resolved Fact kind
+ same complete canonical factual material
+ same applicable Contract world
= same Fact
```

Host object identity is irrelevant.

Allocation history is irrelevant.

Repeated construction is irrelevant.

Repeated delivery is irrelevant.

Storage location is irrelevant.

User-defined `equals` and `hashCode` are not Fact authority.

They may neither merge different canonical factual material nor divide identical canonical factual material.

For example:

```text
Balance(accountId = 42, currency = KRW, amountMinor = 1000)
Balance(accountId = 42, currency = KRW, amountMinor = 1000)

    -> the same Fact
```

Repeated boundary occurrence and repeated Fact establishment are different questions.

The same external presentation may arrive more than once.

If each successful establishment produces the same resolved Fact kind and complete canonical factual material under the
same applicable Contract world, the established material is the same Fact.

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

The two Facts differ because declared factual material differs.

Kontrakt must not preserve occurrence through hidden message identity, host object identity, allocation history,
delivery history, runtime reference, or storage identity.

If two occurrences must remain distinct while every declared factual coordinate remains equal, the Fact surface does not
yet express the required distinction.

---

## 12. Set-Like Fact Semantics

For Fact authority, repeated establishment of identical canonical factual material does not create implicit
multiplicity.

```text
{ F, F } = { F }
```

This is a Contract observation law.

It does not require one particular set data structure.

When count or repeated observation is factual meaning, that meaning must be represented explicitly.

Storage duplicates do not create additional factual meaning.

Delivery duplicates do not create additional factual meaning.

---

## 13. Derived Identity and Physical Keys

Kontrakt may derive internal digests, HIDs, intern keys, routing keys, cache keys, indexing keys, or storage keys from
canonical material.

These keys are derived representation material.

They are not logical identifiers assigned to Facts.

Compact key equality establishes at most a candidate match.

Fact sameness requires exact equality of the resolved Fact kind and complete canonical factual material under the
applicable Contract world.

A digest collision, HID collision, intern-key collision, or storage-key collision must never merge different Facts.

---

## 14. Declared Uniqueness

A Fact declaration may state which factual coordinate tuples must be unique within that Fact kind.

Declared uniqueness is not Fact sameness.

Two Facts that differ in any canonical factual value remain different Facts even if they collide on a declared unique
tuple.

The resulting Fact world may forbid their coexistence because of the declared uniqueness law.

Uniqueness does not silently establish:

```text
replacement
supersession
currentness
history
continuity
relation
ownership
transition
```

Those meanings require their own explicit authority.

Invariant does not restate or rediscover Fact-owned uniqueness.

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

It does not create the coordinates, define Fact sameness, own Fact uniqueness, or create cross-Fact relations.

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

Each admitted Fact surface must be completely refinable into finite canonical factual material.

The compiler must reject hidden membership, runtime discovery, unsupported host behavior, open shape, unresolved factual
distinctions, and any source form whose complete factual meaning cannot be established.

Canonical Fact material must preserve source coordinates required for diagnostics without preserving host execution
mechanics as authority.

Fact vocabulary and Fact surfaces must be available in ContractImage-visible material before executable machine
publication.

---

## 20. Optimization Boundary

A backend may choose any deterministic representation compatible with the canonical Fact material.

It may use primitive arrays, packed bytes, tables, generated classes, columnar layouts, interned material, or other
optimized forms.

It may derive indexes for exact Fact lookup and Fact-owned uniqueness.

Those structures remain implementation.

They may not add Fact relations, Fact multiplicity, Fact currentness, participation authority, or cross-Fact semantics
that the Contract does not declare.

---

## 21. Open in This Section

The final host-language authoring restrictions for every individual Fact surface remain open where the existing closed
immutable profiles do not already decide them.

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

Uniqueness remains a separate factual law instead of silently becoming entity identity or replacement semantics.

Cross-Fact relations remain explicit responsibilities of the Contract that actually needs them.

Backend representation remains replaceable.

---

## 23. Migration History

This ADR was extracted from the Fact-owned material of ADR-0049.

The extraction preserves the latest Accepted ADR-0049 Fact law as of the 2026-08-05 revision.

The extraction does not intentionally introduce a new Fact authority or change the accepted Fact meaning.

ADR-0049 remains the owner of the shared core-flow relation in which Fact establishment participates.