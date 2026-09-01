# ADR-0069: Invariant Contract, Fact-Local Standing Integrity Law, Deterministic Judgment, and Establishment Gate

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0068: Fact Contract
- ADR-0067: Lowering Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Core Fact Establishment, Change Proposal, Bound Fact Authority, and Outward Handoff
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry

---

## 1. Context

Invariant is the standing internal integrity law of one exact Fact kind.

Fact declares what immutable information may hold authority inside the Contract Core.

Invariant declares what one complete canonical candidate for one exact declared Fact kind must satisfy before that
material may receive Fact authority.

An established Fact has already satisfied every applicable Invariant.

Invariant is not an Operation postcondition.

It is not Admission.

It is not a constructor guard.

It is not a callback validator.

It is not a result-formation function.

It is not the law for how one Operation computes its result.

Operation-specific obligations remain in the complete Operation Contract.

Invariant owns only Fact-local integrity that remains after Fact sameness, Fact-owned uniqueness, State and Transition
movement, Policy, Budget, Capacity, Governance, Publication, and Operation-specific formation keep their own authority.

---

## 2. Problem

Without an explicit Invariant Contract, standing Fact Integrity is easily hidden in constructors, validators, services,
repository hooks, Operation implementations, state managers, callbacks, or object methods.

That makes the law dependent on implementation entry points.

It also encourages Invariant to become a validator drawer containing every rule that does not fit elsewhere.

A broad Invariant can then inspect multiple Facts, populations, histories, repositories, State, Operation inputs,
implementation-local values, or arbitrary callbacks.

That would make Invariant another hidden query and computation engine.

The machine needs a narrower authority.

One Invariant must be completely judgeable from one complete canonical candidate Fact.

Anything that needs another Fact, another machine surface, external source, history, collection, or Operation-specific
material is not this Invariant.

---

## 3. Decision Drivers

Invariant is declared once at the enclosing interface's explicit core scope.

An Operation does not select, own, repeat, or bypass the standing law.

Each Invariant names exactly one Fact kind.

The judgment receives exactly one complete canonical candidate Fact.

The user does not receive Change Proposal, current/proposed wrappers, populations, histories, graphs, or backend
coordinates.

Invariant does not grant Fact meaning.

Invariant does not define Fact changes.

Invariant does not own Fact sameness or Fact-owned uniqueness.

Invariant does not authorize State movement.

Invariant does not inspect the Operation algorithm.

Invariant does not acquire another Fact.

Invariant does not construct a population or graph.

The admitted relation must be total, deterministic, terminating, and completely refinable into Kontrakt-owned material.

Host runtime behavior must not remain Contract authority.

A backend may optimize judgment only when the same Fact-local law, proposal-level atomicity, attribution, and
deterministic outcome remain unchanged.

---

## 4. Interface-Level Invariant Catalog

Facts and Invariants share the enclosing interface's explicit core scope.

```text
interface AccountService {
    facts AccountFacts
    invariants AccountInvariants

    operation deposit(...)
    operation withdraw(...)
    operation close(...)
}
```

`invariants AccountInvariants` names one restricted Kotlin or Java source catalog.

It is declared once for the core.

It is not repeated or selected inside an Operation manifest.

An Operation cannot bypass an applicable Invariant by omitting a slot.

Whenever complete canonical candidate material for an Invariant's declared Fact kind may receive Fact authority,
Kontrakt applies that law automatically.

The catalog is an uninstantiable type-signature declaration:

```kotlin
class AccountInvariants private constructor(
    nonNegativeBalance: NonNegativeBalance,
)
```

The constructor is never invoked.

Parameter names do not create Contract meaning.

Source order does not create Contract meaning.

Allocation does not create Contract meaning.

Receiver identity does not create Contract meaning.

Generated JVM shape does not create Contract meaning.

The parameter type positions identify exact Invariant declarations.

The catalog contains no properties, executable roots, computed discovery, inheritance, annotations, callbacks,
reflection, or runtime collection.

An equivalent restricted Java declaration may provide the same material.

---

## 5. One Direct Fact Law

Each referenced Invariant symbol declares one direct Fact law.

```text
one total deterministic Boolean relation
with exactly one parameter
whose type is one Fact kind declared by the same interface-level Fact vocabulary
and whose admitted operands come only from that Fact's complete canonical factual material
```

The source class and method are frontend evidence only.

Kontrakt must completely resolve and lower the admitted relation before ContractImage publication.

The class is not an object-oriented Contract.

The method is not runtime authority.

The Fact parameter is not an object reference.

The parameter type resolves one exact Fact kind.

Direct field or component access resolves canonical coordinate operands.

The admitted Boolean body lowers to domain-neutral Invariant relation material.

The canonical material may therefore retain:

```text
subject Fact kind symbol
canonical coordinate operands
lowered total Boolean relation
authorized constants and value sorts
source coordinates for attribution
```

It does not retain a host class as the Fact.

It does not retain method receiver identity as judgment authority.

It does not retain domain-specific executable behavior as the law.

---

## 6. Example

A direct Fact law may use an ordinary restricted Boolean root.

```kotlin
class NonNegativeBalance private constructor() {

    fun preserved(
        balance: Balance,
    ): Boolean =
        balance.amountMinor >= 0L
}
```

This declaration states that complete canonical material proposed as `Balance` may receive Fact authority only when the
coordinate relation holds.

The source body performs no lookup, population acquisition, grouping, iteration, reduction, navigation, formation, or
movement.

Another example is coordinate ordering:

```kotlin
class ValidPeriod private constructor() {

    fun preserved(
        period: Period,
    ): Boolean =
        period.startEpochMillis <= period.endEpochMillis
}
```

Arithmetic coherence may compare explicit coordinates already present in one Fact:

```kotlin
class ValidAmountBreakdown private constructor() {

    fun preserved(
        amount: AmountBreakdown,
    ): Boolean =
        amount.totalMinor ==
                amount.principalMinor + amount.feeMinor
}
```

---

## 7. Intra-Fact Integrity Functions

Invariant provides Fact-local integrity over explicit factual material.

Supported semantic families include:

```text
value-domain integrity
coordinate ordering
arithmetic coherence
coordinate compatibility
conditional presence and absence
finite-alternative coherence
flag and bit compatibility
```

Value-domain integrity states that one factual value remains inside its declared domain or range.

Coordinate ordering states a relation among explicit positions or bounds inside the same Fact.

Arithmetic coherence states an exact relation among explicit factual quantities already contained by the same Fact.

Coordinate compatibility states that individually admitted factual values inside the same Fact form an allowed
combination.

Conditional presence and absence state that one explicit factual condition may require another explicit coordinate to be
present or absent.

Finite-alternative coherence keeps a selected alternative consistent with the explicit material carried by the same
Fact.

Flag and bit compatibility constrains explicit factual flags or bits contained by the same Fact.

These functions do not authorize acquisition of material outside that one complete Fact.

---

## 8. No Redundant Fact Material for Invariant Convenience

A derived coordinate should not be added merely to create an Invariant.

When one authoritative factual coordinate is sufficient, duplicating a calculable value creates unnecessary material and
an unnecessary standing law.

Invariant preserves explicit factual meaning that must exist.

It does not justify redundant representation.

---

## 9. Aggregate Is Not an Invariant Operation

A Fact may contain explicit scalar coordinates whose declared meaning is an aggregate, total, count, balance, or other
summary.

Invariant may compare those already formed coordinates because they are part of one complete canonical Fact.

Invariant may not derive them by acquiring, collecting, traversing, filtering, grouping, counting, summing, reducing, or
otherwise interpreting nested, external, or separately established material.

Invariant does not establish that an aggregate coordinate was calculated correctly from source entries.

The calculation belongs to replaceable realization.

If a relation between explicit Operation material and a produced aggregate is contractually meaningful, that relation
needs an explicit owner elsewhere in the Operation pipeline.

This ADR creates no Result Contract and assigns no such obligation automatically.

When an authored Fact independently includes scalar coordinates with aggregate meaning, those coordinates lower as
ordinary canonical coordinates.

An applicable arithmetic law lowers as a domain-neutral relation over those coordinates.

The canonical IR does not need an aggregate-specific node.

Kontrakt must not synthesize a summary Fact, aggregate-specific Fact declaration, aggregate validator, or another Fact
kind merely to expose calculated values to Invariant judgment.

If the Fact vocabulary does not independently declare those values as factual material, calculated totals remain
Operation Result Material, processing material, or backend implementation material according to their actual role.

---

## 10. Exactly One Fact

An Invariant receives exactly one Fact.

Kontrakt rejects:

```text
several Fact parameters
Set<Fact>
List<Fact>
Sequence<Fact>
Stream<Fact>
arrays or iterators of Facts
another Fact obtained through lookup
another Fact obtained through navigation
another Fact obtained through repository or callback
an implicit collection of all Facts of one kind
an implicit group formed from equal coordinate values
an implicit sequence
an implicit history
an implicit interval set
an implicit relation
an implicit graph
```

Facts do not reference other Facts.

Shared coordinate names, types, and values create no implicit relation, join, lookup obligation, existence obligation,
or Invariant scope.

Kontrakt does not construct a collection, population, group, sequence, history, interval set, relation, or graph from
separately established Facts for Invariant judgment.

---

## 11. No Cross-Fact Standing Law

Invariant does not provide standing laws over several separately established Facts.

It does not count a Fact population.

It does not aggregate several Facts.

It does not enforce conservation across Facts.

It does not order independently established Facts.

It does not compare interval sets across Facts.

It does not enforce continuity across Facts.

It does not judge coexistence, coverage, partition, population, or historical monotonicity across Facts.

Removing such a law from Invariant does not assign it automatically to Admission, Lowering, Change, State, Transition,
Publication, or another Contract.

When the machine needs such an obligation, its exact subject, material, scope, and owning Contract must be determined
separately.

This ADR decides only that an implicit cross-Fact judgment is not Invariant.

---

## 12. Fact Uniqueness Is Not Invariant

Declared uniqueness belongs to Fact.

Invariant does not restate or recheck that law.

Declared uniqueness forbids conflicting coexistence under the Fact law.

It does not silently grant supersession, replacement, currentness change, or a relation between Facts.

The decisive test for an Invariant is:

```text
Can this law be judged completely from one complete canonical candidate Fact
without observing another Fact, Operation Input, Operation Result Material,
State, history, collection, repository, or external source?

yes:
    it may be an Invariant

no:
    it is not an Invariant
```

---

## 13. Admitted Source Expressions

Invariant bodies may use ordinary Kotlin or Java syntax only when the frontend can completely refine the body into total
deterministic Kontrakt-owned relation material.

The admitted surface includes:

```text
direct coordinate access on the declared Fact parameter
literals and ratified constants
immutable local bindings derived only from admitted operands
canonical equality and inequality
finite arithmetic with explicit total semantics
Boolean conjunction, disjunction, and negation
bit relations over explicit factual bit coordinates
exhaustive if / when / switch judgment
explicit presence, absence, and finite-alternative judgment
```

Whether Java or Kotlin can execute a construct is irrelevant.

Only the closed relation completely lowered from the source is authoritative.

The source may not call arbitrary user behavior merely because that behavior returns `Boolean`.

---

## 14. Totality, Determinism, and Termination

Every Invariant declaration must be total, deterministic, and terminating.

Host overflow must not silently decide Invariant meaning.

Exception behavior must not silently decide Invariant meaning.

`NaN` behavior, locale, timezone, comparator behavior, collection implementation, object identity, or unspecified
evaluation behavior must not define the Contract accidentally.

When numeric or value semantics cannot be proved total or fixed by an explicit Kontrakt semantic profile, the source is
rejected.

Runtime exception is not an implicit Invariant result.

---

## 15. Forbidden Observation and Behavior

Invariant source may not observe or perform:

```text
Operation Input
Operation Result Material
implementation-local values

Change Proposal
current/proposed pairs
candidate wrappers
overlay views

another Fact
a Fact reference
a Fact collection
a hidden Fact context

State
Transition
Policy
Budget
Capacity
Governance
Publication
Diagnostic material

database rows
repository access
service access
network
files
clock
randomness
environment
thread
lock
transaction state

reflection
runtime class discovery
proxy behavior
object identity
custom equals
custom hashCode
custom compareTo
Comparator

Fact mutation
repair
normalization
parsing
replacement
removal
currentness change
State movement

instance state
mutable globals
initialization behavior
inheritance
override dispatch
interface-provided behavior

collection operations
streams
sequences
loops
recursion
callbacks
lambdas
method references
escaping closures
exception-driven choice
arbitrary user method calls

backend indexes
cache state
counters
accumulators
delta state
incremental-maintenance state
evaluation plan
```

An implementation detail does not become legal Invariant material because it is deterministic in one backend.

---

## 16. Change Proposal Judgment

The user does not declare or observe a Change Proposal.

When the complete Operation Contract causes Kontrakt to form one proposal internally, Kontrakt resolves every proposed
Fact independently under its declared Fact kind and canonical factual material.

For each proposed Fact, Kontrakt applies Fact-owned sameness and uniqueness and every interface-level Invariant declared
for that exact Fact kind.

```text
one complete internally formed Change Proposal
    -> each proposed Fact material
        -> resolved Fact kind and complete canonical factual material
        -> applicable Fact-owned sameness and uniqueness judgments
        -> every interface-level Invariant declared for that Fact kind
            -> all preserved
                -> proposed Fact remains eligible for indivisible establishment
            -> any refused
                -> whole Change Proposal is refused
    -> every applicable State / Transition movement judgment
    -> only complete success permits indivisible establishment
```

The diagram names judgment dependencies.

It does not require one physical execution order.

A backend may fuse, reorder, specialize, or statically discharge judgments only when the same Fact-local law,
proposal-level atomicity, refusal attribution, and deterministic outcome remain unchanged.

---

## 17. Relationship to State and Transition

Invariant does not define how Facts change.

It does not inspect whether an Operation used the correct algorithm.

It does not compare independently established Facts.

It does not repair a refused Fact.

It returns only the standing-law judgment over one proposed Fact.

State and Transition independently judge movement legality over the same internal Change Proposal.

Neither authority substitutes for the other.

Only complete success under all required authorities permits establishment.

---

## 18. Cycles Are Not Invariant Violations

Contract cycles are not Invariant violations.

Recursive Contract composition and cyclic Contract dependency are structurally inadmissible and must be rejected by
compiler validation before establishment.

A cyclic State Machine Manifest is rejected by its own movement law.

Kontrakt does not admit a semantic cycle and ask an Invariant to detect it afterward.

Factual coordinates such as `fromId` and `toId` do not cause Kontrakt to construct an implicit graph from separately
established Facts.

---

## 19. Definition-Time Law

At definition time, Kontrakt must:

```text
resolve the exact interface-level Invariant catalog symbol
resolve every exact Invariant declaration symbol listed by that catalog
resolve exactly one direct Fact parameter against the interface-level Fact vocabulary
refine the complete admitted Boolean relation over that Fact's canonical factual coordinates
reject additional Fact parameters
reject Fact containers
reject hidden Fact acquisition
reject unknown behavior
reject partiality
reject nondeterminism
reject recursion
reject unbounded work
reject aggregation
reject collection traversal
reject unsupported operations
lower each declaration into canonical Fact-local Invariant material
derive stable Fact-kind and coordinate dependency indexes
publish the resulting laws in ContractImage-visible form
```

Host source mechanics must be erased before authority begins.

---

## 20. Compiler-Derived Realization

From the same canonical Invariant material, Kontrakt may derive and optimize:

```text
applicable-law selection by exact Fact kind
direct coordinate dependency selection
specialized runtime gates
primitive comparisons
constant folding
shared coordinate reads
static discharge where stronger canonical material proves the law
generated valid fixtures
generated boundary fixtures
generated violating fixtures
generated property-based tests
shrinking guidance
refusal attribution
diagnostic attribution
deterministic backend-specific realization
```

These are compiler-derived products.

They do not become another authored Invariant.

No collection scan, group index, aggregate counter, interval structure, history traversal, or graph algorithm is
required or authorized by this Invariant Contract.

Backend machinery used for Fact storage or Fact-owned uniqueness belongs to those separate concerns and may not enlarge
Invariant meaning.

---

## 21. Result and Attribution

Invariant produces one standing-law judgment for one proposed Fact.

Its outcome is visible to the machine and attributable to the exact named Invariant that produced it.

The source catalog, declaration class, source method, receiver object, and generated JVM mechanics do not receive
runtime Contract authority.

Invariant refusal means complete candidate Fact material exists but the standing law for that exact Fact kind does not
hold.

It is distinct from malformed Input, Admission rejection, Canonicalization refusal, Lowering refusal, State movement
refusal, Operation realization failure, Publication decision, and Output realization failure.

---

## 22. Open in This Section

The final Java and Kotlin token spelling for every admitted direct Invariant expression may evolve.

The authoring boundary is fixed: one exact Fact parameter, one total deterministic Boolean judgment, and operands
derived only from that Fact's complete canonical factual material.

No future syntax may silently add another Fact, population, history, graph, proposal, Operation-specific material,
callback, hidden lookup, mutation, or backend evaluation state without a new Contract decision.

The complete authoring surface for cross-Fact or operation-specific obligations remains outside this ADR because this
ADR intentionally does not assign them an owner.

---

## 23. Consequences

Invariant becomes a narrow standing Fact Integrity authority rather than a general validator framework.

The same law applies automatically to every proposed establishment of its exact Fact kind.

The user writes ordinary restricted Boolean expressions while the compiler owns semantic refinement and runtime
realization.

The one-Fact rule prevents Invariant from becoming an implicit query engine over the Contract Core.

Aggregate computation, history, population, repository lookup, and graph traversal remain outside the authority instead
of being hidden behind a Boolean return type.

State and Transition remain independent movement authorities.

The compiler gains strong opportunities for static discharge, primitive specialization, generated tests, and direct
diagnostic attribution because the Invariant surface is closed.

---

## 24. Migration History

This ADR was extracted from the Invariant-owned material of ADR-0049.

The extraction preserves the latest Accepted ADR-0049 Invariant law as of the 2026-08-05 revision.

The extraction does not intentionally introduce a new Invariant authority or change the accepted Invariant meaning.

ADR-0049 remains the owner of the shared Change Proposal and indivisible-establishment relation in which Invariant
participates.