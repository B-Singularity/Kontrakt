# ADR-0049: Flow Contract Processing — Fact, Invariant, and Publication

## Status

Draft

## Date

2026-07-12

## Related

- `docs/what-contract-is.md`
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Interface Boundary
- ADR-0045: Contract-First Package Architecture and Explicit Machine Refactoring Boundary
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

ADR-0048 defines the inbound airlock.

Input establishes judgeable boundary presentation. Admission decides whether that material may continue.
Canonicalization optionally establishes a stable representative. Lowering is the authorized passage across the
Boundary by which admitted material is established as explicit immutable Fact inside the contract core. It begins only
after the proposed factual meaning is explicit. It completes only after the selected Fact declaration and every
applicable
Invariant and movement obligation have been satisfied. A refused Lowering places nothing in the core.

The Boundary is not merely a line. It is the controlled passage in which external or otherwise non-factual material may
be inspected, admitted, canonicalized, bound, judged, or refused. Material under judgment at the Boundary carries no
Fact authority. The contract core contains established immutable Facts only. Core membership and Fact authority are the
same contract decision.

A Fact is not limited to a final domain value. It is an immutable proposition upon which the machine may rely under
declared factual values, provenance, scope, and version. It carries no hidden object or occurrence identity. Boundary
material may therefore be lowered into a Fact that
a request, claim, measurement, or observation occurred without granting the claimed content a stronger factual meaning
than the contract declares.

An Operation consumes only the established Facts explicitly bound to that Operation by contract. Operation Input is
therefore a consumption role over bound Facts, not a separate kind of core material. Contract Core membership does not
make a Fact available to every Operation or judgment. Core realization may calculate transient implementation values and
may produce declared immutable Operation Result Material, but neither becomes core material merely by existing. When the
operation declares a factual change from that result, it issues one Change Proposal. The proposal binds the declared
Fact changes and any State movement that must be judged and established together. It is not Fact. Every applicable
interface-level
Invariant judges the complete Fact world that would result if the proposal were established, while State and Transition
independently judge the same proposal's movement. Only a proposal that satisfies every required obligation may be
successfully lowered as one indivisible change. No material becomes a member of the core except as established Fact.
Classes, records, rows, object graphs, repositories, callbacks, and backend layouts may carry or realize information,
but they do not own factual meaning.

A single enclosing interface scope may declare several operation pipelines that enter the same explicit core. Policy,
Governance, Budget, and Capacity are bound once at that machine scope because they coordinate finite resources and
decisions shared among the closed operation set. They are not repeated in each operation manifest. Internal core work
remains replaceable implementation and does not recursively open another operation pipeline.

This ADR defines the remaining flow contracts:

```text
Fact
Invariant
Publication
```

These contracts answer three different questions:

```text
Fact:
    what explicit immutable information exists for the core?

Invariant:
    what standing law must remain true in every established Fact world within its declared factual scope?

Publication:
    what outward claim may be produced from core information?
```

Failure and diagnostic representation, movement, and bounds remain separate category concerns. This ADR preserves their
attribution boundaries but does not define their complete processing profiles.

---

## 2. Problem

Without an explicit Fact declaration, core information is easily hidden inside implementation classes, mutable fields,
getters, repository lookups, object identity, or backend storage. The machine then has to execute implementation to
discover what it knows.

Without a separate Invariant contract, standing laws over the established Fact world are easily hidden inside
constructors, validators, services, persistence hooks, operation implementations, or state managers. The machine cannot
distinguish information, operation-specific change meaning, and the standing law that every authoritative Fact world
must preserve.

Without a separate Publication contract, internal Fact material or declared Operation Result Material can leak outward
merely because it is immutable or easy to serialize. That would couple external users to core representation and would
collapse internal knowledge into public claim.

The machine therefore needs explicit immutable factual material, explicit judgment over that material, explicit legal
movement of availability, and a separate outward-claim boundary.

---

## 3. Decision Drivers

Fact must be contract material, not a class, row, DTO, record, Value Object, entity, object graph, repository entry, or
storage layout.

Everything the contract core contains as material must be available as explicit immutable Fact material. An Operation
receives only established Facts explicitly bound to it under an operation-input role; Operation Input is not a second
core material category. Core membership does not grant universal Fact access, and no Operation or judgment may use an
established Fact outside its declared binding.

Ordinary primitive and closed immutable host types may nominate Fact coordinates when they preserve the required
information. Kontrakt must not require a proprietary wrapper merely to make a value look internal.

The Fact kinds admitted to an enclosing interface's explicit core must be declared through exact source-symbol
references. Package membership, directory placement, classpath discovery, reflection scanning, or a runtime collection
may not grant Fact eligibility. Adding a neighboring immutable and behaviorless class must not change the Fact
vocabulary
unless an explicit named declaration is edited.

A named data-only host declaration may group those exact Fact surface references once for the enclosing interface. The
host declaration is frontend evidence only. It is not Fact, a runtime collection, or core material, and its class
mechanics carry no contract authority after resolution and Lowering.

Fact meaning must remain separate from physical representation. A backend may replace object fields with primitive
arrays, packed bytes, generated tables, or another deterministic layout without changing the Fact. Two realizations of
the same Fact kind with the same canonical factual material are the same Fact. Host object identity and allocation
history may not create another factual distinction. Host-language equality and hashing are not Fact authority. Kontrakt
does not consult user-defined equality or hashing when determining Fact sameness. They may neither create a factual
distinction between identical canonical material nor erase a factual distinction between different canonical material.

Invariant must be declared once at the enclosing interface's explicit core scope beside the Fact vocabulary. It
protects Fact Integrity by declaring a standing law over explicit factual scope. An Operation does not select, own, or
repeat that law. When an internally formed Change Proposal may affect that scope, Kontrakt must automatically judge the
complete hypothetical resulting Fact world before any part of the proposal receives authority.

The user must not declare, receive, or inspect Change Proposal, current-versus-proposed wrappers, overlay views, Fact
population containers, or backend storage coordinates. A direct Invariant declaration names one Fact kind and one total
deterministic Boolean relation over that Fact's factual coordinates. A population Invariant declaration names only its
participating Fact kinds, direct factual scope and value projections, and the closed standing condition required by one
compiler-recognized law family. Neither form may grant information its factual meaning, invent how Facts change, inspect
operation behavior, acquire or traverse Fact populations, query hidden state, or use runtime object relations as
authority.

State and Transition must remain separate from Fact and Invariant. They judge whether State movement declared in the
same Change Proposal is legal, while Invariant judges Fact Integrity under that proposal. Neither may substitute for the
other, and neither makes a class or return value factual.

Publication must be an explicit judgment. Immutability and internal availability do not make core material public.

Established Facts and Fact authority do not leave the Contract Core. Publication may use explicitly bound Fact authority
only to produce a new outward presentation under a declared external meaning. That presentation is not Fact material and
does not retain Fact authority. No other contract role may transfer established Fact or Fact authority out of the
Contract Core, and no Fact-derived meaning may become outward material without an applicable Publication Contract.

External publication must expose a publication presentation, not the internal Fact carrier or backend representation.

A backend may optimize Fact storage, Invariant evaluation, movement checks, and emission, but it may not change factual
meaning, judgment law, state movement, or publication permission.

---

## 4. Decision

ADR-0049 defines the core information and outward-claim half of flow processing. Distinct operation pipelines use their
own ADR-0048 Boundaries. Successful Lowering establishes the boundary-derived Facts required by the selected operation,
while the enclosing machine scope fixes the shared Policy, Governance, Budget, and Capacity contracts for that operation
run:

```text
admitted and canonical boundary material with explicit proposed Fact meaning
    -> Lowering under the selected Fact declaration
        -> every applicable Invariant judgment
        -> every applicable movement judgment
    -> established immutable Fact in the contract core

established Facts explicitly bound to the operation-input role
+ machine-wide Policy, Governance, Budget, and Capacity contracts
    -> replaceable core realization
    -> declared immutable Operation Result Material

when the selected operation declares a factual change from that result:
    Operation Result Material
        -> one Change Proposal
            -> declared Fact changes
            -> declared State movement where present
        -> every affected interface-level Invariant judgment over Fact Integrity
        -> every applicable State / Transition judgment over movement
        -> indivisible successful Lowering of the whole proposal
        -> new Fact authority and legal movement

explicitly selected Fact or Operation Result Material source
    -> Publication judgment
    -> outward presentation or declared publication stop
```

The declared Fact surface provides the immutable information definition used by the core. It does not describe one
implementation object, does not require that the information was produced by an Operation, and does not require a
second user-authored Fact Contract, identity declaration, equality function, or hash function. Its factual meaning may
include declared provenance, so the Fact that a source made a claim, submitted a request, reported a measurement, or
produced an observation does not make the claim's content a stronger Fact than the declaration permits.

The enclosing interface binds one named Fact vocabulary declaration and one named Invariant declaration carrier at
interface scope. The Fact vocabulary declaration is authored through a restricted Kotlin or Java frontend law: a named,
uninstantiable type-signature declaration lists exact Fact surface type references, and the interface IDL references
only
that declaration symbol through `facts`. The Invariant carrier is also a named, uninstantiable Kotlin or Java
type-signature
declaration. It lists exact Invariant declaration symbols, and the interface IDL references only that carrier symbol
through `invariants`. Facts and Invariants are declared once for the explicit core and are not repeated inside each
Operation manifest.

Fact vocabulary membership declares which Fact kinds may be established in that core. It does not grant every Operation,
Transition, Lowering, or Publication universal participation authority. Each such contract still owns the explicit Fact
bindings through which its declared roles may participate. Each Invariant declaration instead fixes its own factual
scope
through one direct Fact parameter or through the participating Fact kinds and factual projections required by one closed
population law family. Kontrakt applies that standing law automatically to every establishment that may affect that
scope.

The Invariant Contract declares a standing law over the established Fact world within that resolved factual scope. The
user writes direct coordinate projections and Boolean relations against ordinary Fact carriers and does not observe a
Change Proposal or receive a Fact population. When an Operation causes Kontrakt to form one Change Proposal internally,
Kontrakt derives the complete hypothetical resulting Fact world, determines which interface-level Invariants may be
affected, and judges those laws against that resulting world. Invariant protects Fact Integrity, but it does not define
the Fact changes, inspect the Operation's algorithm, manufacture Fact meaning, authorize State movement, or convert an
implementation object into information.

The State and Transition axis governs legal movement and availability. It judges any State movement bound to the same
Change Proposal under authority separate from Invariant. Before successful Lowering, the proposal's proposed Fact
changes are not Facts and its proposed movement has not
occurred. If any required Invariant or State / Transition judgment refuses the proposal, Lowering is refused and no part
of that proposal is established.

A Change Proposal is explicit Operation-produced judgment material held at the Boundary of Fact authority, not a new
contract authority and not core material. The selected Operation's declared bindings must already state the Fact changes
and any State movement that the proposal carries. The proposal may not invent change meaning through callbacks, hidden
mutation, runtime discovery, or Invariant evaluation. Indivisible successful Lowering is a contract-observation
rule: no declared part becomes authoritative unless every required judgment succeeds.

The Publication Contract is the outward-claim authority. Established Facts and Fact authority do not leave the Contract
Core. Declared Operation Result Material is machine-internal material outside Fact authority and does not become outward
material merely because it exists. Publication decides whether a new outward presentation is permitted, which explicitly
bound source may participate, and which external meaning that presentation may contain. The presentation is not the Fact
or Operation Result Material from which it was produced and carries no authority to re-enter the Contract Core as Fact.

The common role law is:

```text
Fact declares established immutable information that belongs to the core.
Operation Input binds the established Fact roles that may participate in one Operation; it is not a separate core material category.
Operation Result Material declares the explicit immutable machine-internal material an Operation has produced outside Fact authority.
Change Proposal carries one complete possible machine change under declared Operation bindings and binds the Fact changes and State movement that must be judged together.
Invariant declares what must remain true in every established Fact world within its declared factual scope; Kontrakt applies that law automatically to the complete world that the proposal would establish.
State and Transition declare whether the same proposal's State movement is legal.
Successful Lowering grants Fact authority and performs legal movement only after every required judgment succeeds.
Publication declares the permitted new outward presentation from an explicitly bound source without transferring Fact authority out of the core.
```

No implementation class, generated serializer, repository row, object reference, cache entry, or frozen storage layout
may replace any of those roles. Core realization may contain one function or many functions, but that decomposition does
not create another IDL operation, another airlock, or another contract pipeline. The machine-wide resource contracts
continue to govern the selected operation run independently of the internal implementation graph.

---

## 5. Flow Processing Profiles

### 5.1. Fact

Fact is explicit immutable information established inside the contract core.

A Fact is not the object, row, message, record, field, or value instance that may carry it in software. Those are source
evidence or realizations. The Fact is the immutable proposition declared by its factual surface and made available under
its canonical factual values, provenance, scope, and version. It carries no hidden object or occurrence identity.

A boundary-derived Fact may truthfully state that a request was submitted, a claim was made, a measurement was reported,
or an observation occurred. Such provenance-bearing meaning does not promote the request, claim, measurement, or
observation content into a stronger Fact than the contract declares.

Fact belongs to the core and is available there under the legal machine world. As contract material, only established
Facts belong to the core. Implementation may calculate transient values, but those values do not become core material.

```text
existing core world
    -> already available Fact

core realization
    -> declared immutable Operation Result Material
    -> declared factual change
    -> one Change Proposal
        -> declared Fact changes
        -> declared State movement where present
    -> required Invariant and State / Transition judgments over that same proposal
    -> indivisible successful Lowering
    -> new Facts and legal movement
```

Boundary presentation does not create a separate kind of Fact and does not enter the core merely by being admitted or
canonicalized. At the Boundary, its proposed factual meaning must be explicit. Successful Lowering establishes that
meaning as immutable Fact under the selected Fact declaration and every applicable Invariant and movement obligation. A
refused Lowering places nothing in the core. Physical bootstrap, restoration, storage, or backend loading may realize
how
a Fact is present, but those mechanisms do not create another contract category or own factual meaning.

A declared Fact surface provides the coordinates, sorts, distinctions, bounds, provenance, scope, version meaning, and
other factual values required by that information. It does not require a separate user-authored Fact Contract body. A
Fact
declaration may also state which factual coordinate tuples must be unique within that Fact kind. Declared uniqueness is
not Fact sameness and does not imply automatic replacement: two Facts that differ in any canonical factual value remain
different Facts, but a resulting Fact world may not contain both when they collide on a declared unique tuple.

Currentness, general cardinality, supersession, grouping, or continuity are not implicit properties of every Fact; when
required, they belong to the applicable Invariant, Change, or Transition declaration. Facts do not reference other
Facts.
A factual coordinate such as `accountId` is factual material only. The same coordinate value appearing in another Fact
does not create an object relation, lookup obligation, existence obligation, graph edge, or automatic cross-Fact
binding.
The declared Fact surface exposes factual coordinates that another contract may explicitly use as operands, but it does
not list every Invariant that may apply.
The interface-level Invariant carrier declares those standing laws separately, and each referenced Invariant declaration
fixes its own factual scope. The declared Fact surface does not define Operation-specific change meaning. The selected
Operation's explicit bindings declare what Fact changes its Change Proposal carries. Fact is not universally an entity,
event, identifier, persisted record, or state snapshot.

A host frontend may use primitives, strings, enums, arrays under an immutable profile, finite products, Kotlin data
classes, or Java records as declaration evidence when their complete visible shape can be refined. Kontrakt does not
require `CoreInt`, `KontraktText`, or a user-authored Value Object merely because information exists inside the core.

The Fact vocabulary for an enclosing interface is also authored through a restricted host declaration rather than a
package selector, class literal collection, or new IDL body language. A Kotlin frontend may express the declaration as
an
uninstantiable type signature:

```kotlin
class AccountFacts private constructor(
    accountOpened: AccountOpened,
    balance: Balance,
    withdrawalRecorded: WithdrawalRecorded,
    accountClosed: AccountClosed,
)
```

The constructor is never invoked and the class is not the contract. Its parameter type positions provide exact source
symbols for the Fact surface declarations. Parameter names, source order, constructor identity, allocation, generated
JVM
shape, and host class identity carry no contract meaning. Resolution and Lowering retain only an order-independent,
duplicate-free canonical Fact vocabulary. An equivalent restricted Java frontend may provide the same declaration
material.

The declaration must contain no properties, methods, initialization, inheritance, callbacks, computed membership,
package selectors, `KClass` or `Class` values, reflection, or runtime discovery. Moving another class into the same
package does not make it Fact. Only editing the named declaration changes its source membership.

The interface IDL binds that declaration once:

```text
interface AccountService {
    facts AccountFacts
    invariants AccountInvariants

    operation deposit(input: DepositInput): DepositResult
    operation withdraw(input: WithdrawInput): WithdrawResult
    operation close(input: CloseInput): CloseResult
}
```

`facts AccountFacts` declares the Fact vocabulary eligible for establishment in that interface's explicit core.
`invariants AccountInvariants` declares the standing Fact laws that govern that same core. Neither declaration is an
Operation participation list. Every Operation remains limited by its own explicit Fact bindings, while each Invariant is
limited by the direct Fact tuple or closed population scope resolved from its own declaration and is applied
automatically
when an establishment may affect that factual scope.

Host constructors, methods, custom equality, custom hashing, inheritance, object identity, allocation, and storage
layout do not enter Fact authority. Two established Facts are the same Fact when their resolved Fact kind and complete
canonical factual material are identical under the same applicable contract world. Different host instances, repeated
construction, repeated delivery, or different storage locations do not create additional factual distinction or
multiplicity. Host-language equality and hashing may neither merge different canonical factual material nor divide
identical canonical factual material.

Repeated boundary occurrence and repeated Fact establishment are separate questions. The same external presentation may
arrive more than once, but when successful Lowering produces the same resolved Fact kind and complete canonical factual
material under the same applicable contract world, it establishes the same Fact:

```text
Balance(accountId = 42, currency = KRW, amountMinor = 1000)
Balance(accountId = 42, currency = KRW, amountMinor = 1000)

    -> the same Fact
```

If receipt count, receipt time, source, or another occurrence distinction is itself meaningful, that distinction must be
represented by explicit factual coordinates or by a separate Fact:

```text
WithdrawalSubmitted(submissionId = 1001, accountId = 42, amountMinor = 1000)
WithdrawalSubmitted(submissionId = 1002, accountId = 42, amountMinor = 1000)

    -> different Facts because their declared factual material is different
```

Kontrakt must not preserve occurrence through hidden message identity, object identity, allocation history, delivery
history, or storage identity. If two occurrences must remain distinct while every declared factual coordinate remains
the same, the declared Fact surface does not yet express the required factual distinction.

Kontrakt may derive internal digests, HIDs, intern keys, routing keys, or storage keys from canonical material for fast
comparison, interning, indexing, routing, deduplication, or lookup. These are material-derived representation keys, not
logical identifiers assigned to Facts. Digest or key equality establishes at most a candidate match. Fact sameness is
established only by exact equality of the resolved Fact kind and complete canonical factual material under the
applicable
contract world. A collision must never merge different Facts.

For Fact authority, the core therefore has set-like semantics:

```text
{ F, F } = { F }
```

This is a contract observation law, not a requirement to use one particular set data structure or storage layout. When
count or repeated observation is factual meaning, that meaning must be represented explicitly rather than inferred from
duplicate delivery, construction, or storage. The same Fact may be realized through different languages and backend
layouts without changing its meaning.

### 5.2. Invariant Contract

Invariant is the standing integrity law of established canonical factual material.

Fact declares what immutable information may hold authority inside the core. Invariant declares what must remain true of
that information whenever the Fact world is authoritative. It is not an Operation postcondition, an Admission rule, a
constructor guard, a callback validator, or a rule for how one Operation computes or forms its result.
Operation-specific
obligations remain in the complete operation contract pipeline. Invariant owns only the remaining standing integrity of
established Facts after Fact sameness and declared uniqueness, State and Transition movement, Policy, Budget, Capacity,
Governance, Publication, and operation-specific formation retain their own questions.

Facts and Invariants share the enclosing interface's explicit core scope:

```text
interface AccountService {
    facts AccountFacts
    invariants AccountInvariants

    operation deposit(input: DepositInput): DepositResult
    operation withdraw(input: WithdrawInput): WithdrawResult
    operation close(input: CloseInput): CloseResult
}
```

`invariants AccountInvariants` names one restricted Kotlin or Java source catalog. It is declared once for the core and
is
not repeated or selected inside an Operation manifest. An Operation does not own an Invariant and cannot bypass one by
omitting a slot. When an internally formed change may affect an Invariant's factual scope, Kontrakt applies that law
automatically.

The catalog is an uninstantiable type-signature declaration that lists exact Invariant declaration symbols:

```kotlin
class AccountInvariants private constructor(
    nonNegativeBalance: NonNegativeBalance,
    validPeriod: ValidPeriod,
    settlementCurrencyCoherence: SettlementCurrencyCoherence,
    settlementBalance: SettlementBalance,
    reservationNonOverlap: ReservationNonOverlap,
    dependencyAcyclicity: DependencyAcyclicity,
    historicalMonotonicity: HistoricalMonotonicity,
)
```

The constructor is never invoked. Parameter names, source order, allocation, receiver identity, and generated JVM shape
carry no contract meaning. The parameter type positions identify the exact Invariant declarations. The catalog contains
no properties, executable roots, computed discovery, inheritance, annotations, callbacks, reflection, or runtime
collection. An equivalent restricted Java declaration may provide the same type-signature material.

Each referenced symbol declares one named Invariant under one of two closed frontend profiles:

```text
direct Fact law:
    a total Boolean relation over exactly one Fact's factual coordinates

population law family:
    a closed structural declaration over a bounded population owned by the same Contract Core,
    using exact participating Fact kinds, direct factual projections, and one declared standing condition
```

These profiles preserve ordinary Java and Kotlin authoring without turning Invariant into an open validator API, an
object-relation model, or a new mathematical expression language. The source classes and methods are frontend evidence
only. Kontrakt must completely resolve and lower their admitted material and erase their host execution mechanics before
ContractImage publication.

A direct Fact law uses an ordinary restricted Boolean root:

```kotlin
class NonNegativeBalance private constructor() {

    fun preserved(
        balance: Balance,
    ): Boolean =
        balance.amountMinor >= 0L
}
```

This declaration means that every established `Balance` in the law's factual scope must satisfy the stated coordinate
relation. The source body contains no population acquisition, lookup, grouping, iteration, or reduction. It states only
what must be true of one Fact.

The direct profile covers the standing internal integrity of one Fact, including:

```text
value-domain and range law
coordinate ordering and bounded comparison
exact arithmetic coherence among explicit coordinates
conditional presence and absence
finite-alternative compatibility
flag and bit compatibility
declared length, count, digest, or checksum coherence when those values are themselves explicit factual material
```

A derived coordinate should not be added merely to create an Invariant. When one authoritative factual coordinate is
sufficient, duplicating a calculable value creates unnecessary material and an unnecessary law. Invariant preserves
explicit factual meaning that must exist; it does not justify redundant representation.

A direct Invariant receives exactly one Fact. Multiple Fact parameters are not interpreted as a Cartesian product, an
operation tuple, an object relation, or an instruction to find matching Facts. A relation that exists only while one
Operation admits input, forms a result, or proposes Fact changes belongs to that Operation's existing contracts. A
standing relation across a variable Fact world belongs to a closed population law family. Kontrakt rejects an arbitrary
multi-Fact Boolean method rather than inventing a join, reference, pairing, or hidden binding rule.

Facts do not reference other Facts. Shared coordinate names, shared coordinate types, and equal coordinate values create
no implicit relation. A population Invariant may explicitly declare factual projections by which participating Facts are
grouped, compared, ordered, aggregated, or interpreted as edges, but that declaration does not turn Facts into objects
or
references. Backend lookup and indexing remain implementation.

Declared uniqueness is not restated as an Invariant. Fact owns the coordinate tuples declared unique for its Fact kind.
Invariant neither discovers nor rechecks that law. Declared uniqueness forbids conflicting coexistence; it does not
silently grant supersession, replacement, currentness change, or a reference relation.

Direct Fact bodies may use ordinary Kotlin or Java syntax only when the frontend can completely refine the body into
total, deterministic Kontrakt-owned relation material. The admitted surface includes:

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

A direct body may not acquire another Fact, traverse a relation, call a repository or service, or receive a Fact
population. Whether Java or Kotlin syntax can execute is irrelevant; only the closed relation completely lowered from
that syntax is authoritative.

Some standing laws range over a variable population rather than one Fact. These remain Invariants only when their truth
is determined solely by a bounded Fact population owned and completely judgeable by the same explicit Contract Core.
They do not include rows held only by an external database, repository results, another service's data, or any
population
that Kontrakt does not own under the applicable scope, version, governance, and Capacity contracts.

The population law families in scope are:

```text
group coherence and non-unique functional dependency
general cardinality not already owned by Fact-declared uniqueness
aggregate and conservation relations, within one Fact kind or across several Fact kinds
population ordering
global non-overlap and interval exclusion
continuity and declared gaplessness
mutual exclusion and coexistence law
coverage and completeness, within one Fact kind or across several Fact kinds
partition completeness, exclusivity, and disjointness
topology, acyclicity, reachability, and rootedness
explicit historical coherence and monotonic factual law
```

These families cover the remaining standing Fact-world integrity:

```text
group coherence:
    Facts sharing declared grouping coordinates must preserve declared coordinate coherence

general cardinality:
    a bounded factual group must contain an allowed number of Facts

aggregate and conservation:
    declared factual contributions must satisfy an exact aggregate relation

ordering, non-overlap, and continuity:
    declared positions or intervals must preserve the selected population structure

mutual exclusion:
    declared factual conditions may not coexist within one declared scope

coverage and partition:
    declared populations must cover, classify, or divide the required factual domain without forbidden omission or overlap

topology:
    factual edge coordinates must preserve the declared acyclic, reachable, or rooted structure

explicit historical law:
    Facts that explicitly carry historical coordinates must preserve declared coherence or monotonicity
```

Hidden previous objects, repository history, or an implicit before-and-after pair are not historical Facts. Machine
movement belongs to State and Transition. Historical Invariant applies only when the relevant historical material is
itself explicit established Fact material.

A population law is not authored by passing `Set<Fact>`, `List<Fact>`, `Sequence<Fact>`, `Stream<Fact>`, or another
population container to user code. The user must not write `all`, `any`, `count`, `sumOf`, `groupBy`, `fold`, sorting,
pairwise traversal, graph walk, recursion, or another evaluation algorithm. Those operations state how the law is
computed rather than only what the law requires.

Instead, every population family is a closed compiler-recognized declaration schema. The schema exposes only the
contract information required by that family:

```text
participating Fact kind or kinds:
    which established factual materials belong to the law

scope or grouping projection, where applicable:
    which direct factual coordinates place Facts in the same judgment scope

value, position, interval, edge, category, or historical projection, where applicable:
    which direct factual coordinates provide the meaning judged by the family

family condition:
    which cardinality, equality, aggregate, ordering, exclusion, continuity, coverage,
    partition, topology, or historical relation must remain true
```

The user declares only exact Fact kinds, direct factual projections, constants, and terminal conditions admitted by that
family. Kontrakt owns population acquisition, grouping, comparison, aggregation, ordering, interval evaluation,
reachability, and evaluation planning.

A projection such as `entry.settlementId`, `allocation.amount`, `reservation.startEpochMillis`, or
`dependency.parentId` directly names factual material already present on one Fact. It is not a search algorithm or an
object link. A family-defined operand such as cardinality or aggregate total is not a user-maintained counter, running
total, cache, or backend object. It is semantic judgment material defined by the canonical family law and may be
realized
by any correct backend strategy.

The exact Java and Kotlin token spelling of every population-family schema is deferred, but the authoring boundary is
fixed here. A valid surface must use ordinary named Java or Kotlin declarations; must make the family, participating
Fact
kinds, and every required factual projection explicit; must contain no Fact population, traversal, callback, or hidden
lookup; and must be completely lowerable into closed canonical Invariant material. The compiler may not infer a family
from arbitrary user behavior or accept a general predicate as a substitute.

Every direct Fact law and population-family declaration must be total, deterministic, and terminating under declared
bounds. Host overflow, exception, `NaN`, locale, timezone, comparator behavior, collection implementation, or
unspecified
iteration order may not decide Invariant meaning. When numeric or aggregate totality cannot be proved or fixed by an
explicit Kontrakt semantic profile, the source is rejected rather than interpreted through accidental JVM behavior.

The Invariant source may not observe or perform any of the following:

```text
Operation Input, Operation Result Material, or implementation-local values
Change Proposal, current/proposed pairs, candidate wrappers, or overlay views
State, Transition, Policy, Budget, Capacity, Governance, Publication, or Diagnostic material
external database rows, repository, service, network, file, clock, randomness, environment, thread, lock, or transaction state
reflection, runtime class discovery, proxy behavior, object identity, custom equals, hashCode, compareTo, or Comparator
Fact mutation, repair, normalization, parsing, replacement, removal, currentness change, or State movement
Fact-to-Fact object references, hidden navigation, implicit joins, or undeclared factual grouping
instance state, mutable globals, initialization behavior, inheritance, override dispatch, or interface-provided behavior
Fact population parameters, collection operations, streams, sequences, loops, recursion, callbacks, lambdas, method references,
escaping closures, exception-driven choice, or arbitrary user method calls
backend index, cache, counter, accumulator, delta, incremental-maintenance state, or evaluation plan
```

The user does not declare or observe a Change Proposal. The user declares a law over an authoritative Fact world. When
an
Operation's complete contract pipeline causes Kontrakt to form one Change Proposal internally, Kontrakt computes the
whole hypothetical resulting Fact world as though every declared Fact change had succeeded. It never exposes a partially
applied world to Invariant. It then determines the affected Invariants from their resolved Fact kinds, factual scopes,
family classifications, and coordinate dependencies and judges those laws automatically.

```text
current established Fact world
+ complete internally formed Change Proposal
    -> hypothetical resulting Fact world
    -> applicable Fact-owned uniqueness judgments
    -> affected interface-level Invariant laws
        -> all preserved
            -> continue toward indivisible establishment
        -> any refused
            -> no Fact change and no State movement from that proposal is established
```

The diagram names judgment dependencies, not a required execution order. A backend may fuse, reorder, index, or
discharge
judgments only when the complete resulting-world meaning and refusal attribution remain identical.

Invariant does not define how Facts change, inspect whether an Operation used the correct algorithm, or repair a refused
world. It returns only the standing-law judgment. State and Transition independently judge movement legality over the
same internal proposal. Neither authority substitutes for the other.

At definition time, Kontrakt must:

```text
resolve the exact interface-level Invariant catalog symbol
resolve every exact Invariant declaration symbol listed by that catalog
classify each declaration under one closed direct-Fact or population-family profile
resolve every participating Fact kind against the interface-level Fact vocabulary
refine every admitted factual projection, family operand, constant, and terminal condition
reject arbitrary multi-Fact methods, population containers, traversal, unknown behavior, hidden observation,
partiality, nondeterminism, and unbounded work
lower each declaration into canonical Invariant material
derive stable Fact-kind, factual-scope, family, and coordinate dependency indexes
publish the resulting laws in ContractImage-visible form
```

From the same canonical Invariant material, Kontrakt owns and may optimize:

```text
affected-law selection
hypothetical resulting-world plans
population acquisition inside the owned core scope
grouping, comparison, aggregation, ordering, interval, and graph evaluation
specialized runtime gates and primitive indexes
full evaluation, incremental evaluation, or static discharge
constant folding and shared coordinate reads
generated valid, boundary, and violating fixtures
generated property-based tests and shrinking guidance
refusal and diagnostic attribution
deterministic backend-specific realization
```

Full scans, counters, indexes, interval structures, graph algorithms, materialized intermediate values, and incremental
maintenance are replaceable implementation. None may appear in the user declaration or replace the canonical standing
law. An optimization is correct only when it preserves the same factual scope, same relation, same complete
resulting-world judgment, same refusal attribution, and deterministic outcome.

Invariant is not a validator drawer and not a constructor guard. Its outcome is visible to the machine and attributable
to the named standing law that produced it, but the source catalog, declaration classes, host methods, and generated JVM
mechanics receive no runtime contract authority.

### 5.3. Publication Contract

Publication is the outward claim.

Facts and declared Operation Result Material are not automatically public material. The machine may know more than it
is allowed to say, and Operation Result Material may use a representation inappropriate for external consumers.

Kontrakt lowers Publication into the law that permits or denies a new outward presentation from an explicitly bound Fact
or Operation Result Material source. Publication may select, rename, omit, and re-present information only under its
explicit contract. It must not expose backend coordinates, storage layout, hidden diagnostic evidence, mutable aliases,
or implementation objects merely because an emitter can reach them.

Established Fact never becomes outward material. Publication produces a distinct presentation under declared external
meaning, boundary, scope, version, and governance. Permission to produce that presentation does not authorize another
outward path and does not transfer the underlying Fact authority. If the presentation later returns to the machine, it
is external material again and must pass through the inbound Boundary and successful Lowering before any Fact authority
may be established.

A backend may serialize, encode, buffer, or emit the permitted presentation. Emission is implementation. It is not
Publication authority.

Diagnostic material remains internal unless Publication allows a corresponding public diagnostic claim. Diagnostic,
failure, evidence, retention, movement, or any other contract role may not substitute for Publication as an outward
claim authority.

---

## 6. Cross-Profile Boundaries

### 6.1. Fact and Carrier

Fact and carrier must not collapse into one role.

A Kotlin class, Java record, database row, event object, primitive array, or packed region may carry Fact material. The
carrier does not become factual authority. Factual meaning remains in the resolved and lowered Fact declaration and its
explicit coordinates.

Changing carrier, allocation strategy, field layout, packing, or backend language does not change the Fact when the
declared information and distinctions remain identical.

The same separation applies to the named Fact vocabulary declaration. A host declaration such as `AccountFacts` is
only a source coordinate from which exact Fact surface symbols are acquired. It is not a behavior-bearing aggregate, a
runtime set, a Fact carrier, or the authority that makes its member types factual. After resolution and Lowering, only
the
canonical Fact vocabulary and the canonical factual material of its members remain authoritative.

### 6.2. Fact and Invariant

Fact and Invariant must not collapse into one role.

Fact declares the information that exists, including any Fact-owned uniqueness tuple. Invariant declares the remaining
standing integrity law over the established Fact world within the direct-Fact or closed population scope resolved from
its own declaration. Facts and Invariants are both declared once at the enclosing interface's explicit core scope. A
Fact
may exist without a particular Invariant, and one population Invariant may judge laws across several Fact kinds without
taking ownership of Fact sameness, declared uniqueness, operation-specific formation, or movement.

Invariant success does not create Fact meaning, define Fact-change meaning, grant Fact authority, or authorize State
movement by itself. Invariant refusal means that the proposal would break the selected law and therefore blocks the
whole
proposal from establishment under that law. It does not authorize the machine to mutate, repair, or hide information.

### 6.3. Fact, Result, and Publication

Fact and Operation Result Material are not alternative output kinds.

```text
Operation Result Material:
    explicit immutable machine-internal material produced by an Operation outside Fact authority

Fact:
    explicit immutable information that holds Fact authority inside the core
```

An Operation produces declared Operation Result Material from explicitly bound Fact participation. That material is new
machine-internal non-Fact material; it does not carry an established Fact or Fact authority out of the Contract Core. If
the operation declares a factual change from that result, the result contributes to one Change Proposal. That proposal
binds the declared Fact changes and any State movement that must be judged and established together. The proposed
changes
receive Fact authority only after every applicable Invariant and State / Transition obligation over that same proposal
succeeds and the whole proposal is established as one indivisible change. Without a declared factual change, the
material
remains Operation Result Material and may proceed only through the contracts explicitly selected for it.

Publication may derive an external presentation from an explicitly bound Fact source, Operation Result Material source,
or declared combination when the Publication Contract permits it. The external presentation is neither the Fact nor the
Operation Result Material itself and carries no Fact authority. Internal digest, provenance, relation, scope, version,
backend layout, and diagnostic evidence remain absent unless the Publication Contract explicitly declares their outward
meaning. A published presentation that later returns to the machine has only external-material authority and must pass
through the inbound Boundary again.

### 6.4. Fact, State, and Transition

Fact is information. State is the explicit machine condition governing what movement is available. Transition is the
declared movement between conditions.

A state label does not own Fact meaning, and a Fact does not secretly carry lifecycle state. One Change Proposal may
bind
both declared Fact changes and State movement that must succeed or fail together. Before establishment, the proposed
Fact
changes are not Facts and the proposed movement has not occurred. No second host type is required merely to represent
that difference.

Invariant and State / Transition judge the same Change Proposal under separate authority. Invariant judges Fact
Integrity. State and Transition judge movement legality. Neither consumes, replaces, or controls the other's judgment.
Only when every required judgment succeeds may the whole proposal be established.

### 6.5. Publication and Diagnostics

Diagnostic evidence may explain Fact formation, Invariant refusal, movement refusal, or Publication refusal, but
evidence does not create or override any of those roles.

Retention decides what diagnostic material may survive. If retained diagnostic material is ever exposed, Publication
must judge that outward claim separately. Failure, evidence, retention, movement, and any other contract role may not
become an undeclared Fact-egress path. Those contracts remain distinct from Publication even when one runtime path
realizes them together.

### 6.6. Handoff from ADR-0048

ADR-0048 defines the selected operation's Boundary presentations, Admission, optional Canonicalization, and the declared
material from which Lowering begins. This ADR fixes the authority reached by that passage: successful Lowering
establishes explicit immutable Fact inside the same contract core used by the enclosing interface scope. Several
operation pipelines may exist under that scope, but they do not create separate factual cores. The shared machine-wide
Policy, Governance, Budget, and Capacity contracts govern the selected operation run.

The Boundary is the controlled passage, not merely a line. Material may be inspected, admitted, canonicalized, bound,
judged, or refused there, but it carries no Fact authority there. Lowering is the authorized crossing from that Boundary
into the contract core. It completes only after the selected Fact declaration and every applicable Invariant and
movement
obligation have been satisfied. Any defect that should have been stopped by Input, Admission, Canonicalization, or
factual-meaning binding remains a defect at the inbound Boundary; Invariant is not a catch-all validator for malformed
external presentation or failed conversion.

The core does not receive the Input object, Canonicalization source, Lowering declaration, mapping table, staging
object,
or external framework context. It receives only the established immutable Facts produced by successful Lowering.
Operation Input is the role under which an Operation consumes the established Fact roles explicitly bound to it; it is
not another material kind. Internal
realization may be divided or fused freely behind those obligations, but it does not create nested operation manifests
or
require Publication and Lowering between implementation steps. When internal realization proposes new Facts or State
movement, that Change Proposal remains outside Fact authority and must satisfy the same required judgments before
successful indivisible Lowering.

### 6.7. Bound Fact Authority

Contract Core membership does not grant universal participation authority to a Fact.

Every Operation, State or Transition judgment, Lowering judgment, and Publication judgment must explicitly bind the
factual roles that may participate in it. Each Invariant instead declares its complete factual scope through one direct
Fact kind or through the participating
Fact kinds and direct factual projections of one closed population law family. An established Fact may participate only
through an applicable Operation or judgment binding, or through that resolved Invariant scope. A Fact outside the
declared scope carries no authority to participate, even when it exists in the same Contract Core or appears relevant to
the work being performed.

A Fact binding declares participation authority, not ownership of the Fact and not general access to the Contract Core.
It does not transfer Fact authority out of the core and does not authorize the bound Fact to participate in another
Operation, judgment, movement, scope, version, governance world, or outward claim.

Each Invariant may judge only the Fact kinds, factual scopes, coordinates, and family conditions resolved from its own
declaration. The user declares no proposal binding and receives no Fact population. Kontrakt supplies the complete
hypothetical resulting Fact world internally and may not enlarge the Invariant's factual scope merely because another
Fact
appears relevant. Equal coordinate values, matching names, matching types, or apparent domain relevance create no
implicit relation, grouping, or participation authority.

Fact leakage occurs when an established Fact participates in an Operation, judgment, movement, or Publication without
an applicable explicit binding, when its participation exceeds the role, scope, version, governance, or purpose declared
by that binding, when an established Fact or its Fact authority leaves the Contract Core, or when meaning derived from
established Facts becomes outward material without an applicable Publication Contract.

The mechanism by which an implementation locates, presents, isolates, or optimizes access to bound Facts is outside this
decision.

### 6.8. Fact Egress Authority

Established Facts and Fact authority do not leave the Contract Core.

An Operation may produce declared immutable Operation Result Material from explicitly bound Facts. That material remains
inside the machine, outside Fact authority, and is not outward material. It is not an established Fact leaving the core
and carries no authority to represent or recover one.

Publication is the only contract authority by which meaning derived from established Facts or Operation Result Material
may become an outward presentation. It does not release a Fact or Operation Result Material. It produces a new outward
presentation under an explicit Publication Contract. The presentation is not Fact material, does not retain the Fact's
internal authority, and may not represent itself as a continuation of core membership.

A Publication Contract must explicitly bind the factual roles that may participate and the outward meaning, boundary,
scope, version, and governance under which the presentation may be produced. Authority to produce one presentation does
not authorize disclosure through another presentation, Operation result, Change Proposal, Diagnostic, failure, evidence,
retention record, movement record, or other contract role.

Internal digest, provenance, relation, scope, version, or other factual distinctions do not become outward meaning
merely
because the Fact participates in Publication. They may appear only when the Publication Contract declares their
external meaning explicitly.

A published presentation carries only the authority declared for that outward presentation. If it later returns to the
machine, it is external material again. Prior publication grants no Fact authority on re-entry and does not bypass
Input,
Admission, Canonicalization, Lowering, or any applicable judgment.

Bound Fact Authority governs which established Facts may participate inside an Operation or judgment. Fact Egress
Authority governs which new outward meaning may be produced from explicitly bound sources. Neither authority implies the
other.

The mechanism by which an implementation prevents direct Fact escape or realizes an authorized outward presentation is
outside this decision.

---

## 7. Deferred Decisions

This ADR does not decide the final authoring restrictions for individual Fact surfaces or the final authoring syntax for
Publication bodies. It does decide the interface-level Fact and Invariant catalog patterns. The interface references one
named, restricted Kotlin or Java source declaration through `facts`; that declaration names exact Fact surface types and
is completely lowered away. The same interface references one named, restricted, uninstantiable Kotlin or Java catalog
through `invariants`; that catalog names exact Invariant declaration types and is completely lowered away. Each
referenced
declaration must satisfy one closed direct or population-family profile. Package membership, classpath scanning, class
literals, runtime collections, and computed discovery are not Fact or Invariant membership authority.

It does not freeze the final Java and Kotlin token spelling or the complete V1 catalog of population-family schemas.
It does fix their authoring boundary: direct laws use restricted Boolean relations over exactly one Fact; population
laws
expose only their participating Fact kinds, direct factual projections, constants, and family-defined standing
condition.
No Invariant declaration receives a Fact population or contains traversal, collection reduction, callback, lambda,
hidden lookup, mutation, proposal access, operation-specific material, or backend evaluation state. The compiler must
recognize a closed declaration family and may not infer contract meaning from arbitrary user behavior.

This ADR does not define the complete authoring surface by which the enclosing machine contract and its operation flows
declare required existing Fact dependencies, possible Operation Result Material, Change Proposals, their declared Fact
changes, State movement bound to the same proposal, or the replaceable implementation realization between them.

It does not define the complete state and transition sets for Fact availability, Result completion, Invariant refusal,
movement refusal, or publication. Those movement surfaces must be designed with the complete operation flow, failure,
diagnostic, and Version material together with the enclosing machine-wide Policy, Governance, Budget, and Capacity
contracts.

It also does not define final persistence layout, cache layout, core Fact storage, the mechanism used to locate or
present explicitly bound Facts, public serialization schema, or emitter implementation. Those are replaceable
realizations behind the contract boundaries fixed here.

---

## 8. Consequences

The core is now defined as an explicit Fact machine rather than an object graph or execution container that discovers
knowledge through implementation behavior. One enclosing interface may expose several operation pipelines without
dividing that core. Each operation retains its own controlled Boundary and Publication boundary, while machine-wide
Policy, Governance, Budget, and Capacity coordinate the finite resources shared among all of them.

Each enclosing interface names its core Fact vocabulary once through `facts` and its standing Fact laws once through
`invariants`. The referenced restricted host catalogs list the exact Fact surface symbols and exact Invariant
declaration
symbols in one place and disappear after canonical Lowering. Operation manifests do not repeat either declaration,
package placement cannot add a Fact or Invariant implicitly, and vocabulary membership does not grant an Operation or
judgment undeclared participation authority.

Fact is explicit immutable information holding authority inside the core, not an Operation return type, boundary
presentation, operation-input object, a Change Proposal awaiting judgment, a Value Object, a persistence row, or a
backend layout. Successful Lowering is the only authorized passage by which Boundary material becomes core Fact. A
refused Lowering places nothing in the core. Existing Facts are already present in the core and may participate only
through an explicit Operation Input binding. Declared Operation Result Material may contribute to a factual change only
through one explicit
Change Proposal, and the proposed Facts receive authority only when every required Invariant and State / Transition
judgment over that same proposal succeeds and the proposal is successfully lowered as one indivisible change.

Invariant has one visible Fact Integrity judgment role without becoming the source of Fact meaning, Fact-owned
uniqueness, Fact-change meaning, Operation behavior, or State movement. The user declares either a direct relation over
exactly one Fact or the participating Fact kinds, direct factual projections, constants, and standing condition of one
closed population law family. The user never receives a Fact population and never declares or observes the Change
Proposal.
Kontrakt applies every affected interface-level Invariant automatically to the complete hypothetical resulting Fact
world.
State and Transition judge legal movement separately over the same internal Change Proposal. Publication
remains the sole outward-claim authority rather than an automatic side effect of immutability, result production,
persistence, diagnostics, failure, retention, movement, or serialization. Established Facts and Fact authority remain
inside the Contract Core. Operation Result Material remains machine-internal material outside Fact authority, while
Publication produces a distinct outward presentation carrying only its declared external authority.

Primitive and ordinary closed immutable host types may serve as frontend evidence when they preserve the required
information. Fact sameness is determined by the resolved Fact kind and complete canonical factual material, not by
hidden
identity, object allocation, `equals`, or `hashCode`. Repeated establishment of identical canonical material does not
add
another Fact or implicit multiplicity. The core therefore has set-like Fact semantics without requiring a set-shaped
backend. Meaningful count or occurrence remains explicit factual material rather than an effect of repeated delivery,
construction, or storage. Core richness comes from explicit Facts, Operation Result Material, laws, judgments, states,
and transitions—not from behavior-bearing classes or semantically authoritative Value Objects.

Internal Fact carriers do not need to appear in Input or public output contracts. External users may receive Publication
presentations while established Facts and their authority remain inside the Contract Core and core factual
representation
remains replaceable. A published presentation that returns to the machine is external material again and receives no
implicit Fact authority from its origin.

The split between ADR-0048 and ADR-0049 keeps optimization honest. The selected operation's Boundary rejects malformed
or inadmissible material before core work is paid. Lowering crosses that Boundary only after the proposed Fact meaning,
selected Fact declaration, and every applicable Invariant and movement obligation have been resolved; successful
Lowering
establishes immutable Fact, while refused Lowering places nothing in the core. The core operates over established Facts
explicitly bound to the operation-input role under the machine-wide contracts
fixed for that run. Internal functions and stages
remain implementation rather than nested operation pipelines. Core realization produces declared Operation Result
Material and may issue one Change Proposal that binds the Fact changes and State movement to be judged together.
Invariant evaluates Fact Integrity, while State and Transition independently judge movement legality over that same
proposal. Only the whole accepted proposal is successfully lowered. Publication pays outward transformation and
emission cost only after an outward claim is permitted.