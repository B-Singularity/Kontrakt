# ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation

## Status

Migration Pending

## Date

2026-07-12

## Related

- `docs/what-contract-is.md`
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0041: Stable Metadata Identity, BLAKE3, HID, and Protocol-Owned Interning
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication

---

## 1. Context

ADR-0048 defines the inbound airlock.

Input establishes judgeable boundary presentation. Admission decides whether that material may continue.
Canonicalization optionally establishes a stable representative. Lowering is the authorized passage across the Boundary
by which admitted material is established as explicit immutable Fact inside the contract core. It begins only after the
proposed factual meaning is explicit. It completes only after the selected Fact declaration and every applicable
Invariant and movement obligation have been satisfied. A refused Lowering places nothing in the core.

The Boundary is not merely a line. It is the controlled passage in which external or otherwise non-factual material may
be inspected, admitted, canonicalized, bound, judged, or refused. Material under judgment at the Boundary carries no
Fact authority. The contract core contains established immutable Facts only. Core membership and Fact authority are the
same contract decision.

A Fact is not limited to a final domain value. It is an immutable proposition upon which the machine may rely under
declared factual values, provenance, scope, and version. It carries no hidden object or occurrence identity. Boundary
material may therefore be lowered into a Fact that a request, claim, measurement, or observation occurred without
granting the claimed content a stronger factual meaning than the contract declares.

The manifest-selected Input Presentation does not enter the user Operation directly. Lowering forms ordinary host
material for the Operation parameter's declared type. Kontrakt judges that material under the corresponding Fact kind
and every applicable Invariant before invoking the implementation. Only established input Fact authority may cross the
contract-machine boundary into the Operation call. The implementation still receives the same ordinary Java or Kotlin
parameter it would receive without Kontrakt; Fact authority is not represented by a wrapper, marker, proposal type, or
behavior added to that host value. The Operation may consume other established Facts only through explicit Fact
participation bindings. Contract Core membership does not make a Fact available to every Operation or judgment.

The user implementation remains an ordinary host-language implementation. It accepts its declared input and returns its
declared result without calling Kontrakt, forming a Change Proposal, establishing a Fact, invoking an Invariant, or
performing Publication. Core realization may calculate transient implementation values and may produce declared
immutable Operation Result Material, but neither becomes core material merely by existing. The Operation return
declaration names one Fact kind from the enclosing interface's Fact vocabulary. After the implementation returns
ordinary result material, Kontrakt judges that material outside the implementation as proposed material for the declared
result Fact kind. When the return or another factual change participates in one machine change, Kontrakt forms one
Change Proposal internally. The proposal binds the proposed return Fact, any additional declared Fact changes, and any
State movement that must be judged and established together. It is not Fact. Every applicable interface-level Invariant
judges each proposed Fact of its declared kind solely from that Fact's complete canonical factual material, while State
and Transition independently judge the same proposal's movement. Only a proposal that satisfies every required
obligation may be established as one indivisible change. Only then does the contractual Operation complete successfully
with its declared result as established Fact. If any required judgment refuses, the Operation fails, no successful
result crosses the Operation boundary, and no Publication begins. Candidate and established are internal
contract-machine authority states applied to ordinary host material, not different user-visible types. No material
becomes a member of the Contract Core except as established Fact.

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
Output Presentation
```

These contracts answer four different questions:

```text
Fact:
    what explicit immutable information exists for the core?

Invariant:
    what standing law must every established Fact of its declared kind satisfy?

Publication:
    which exact public claim may receive outward authority from one established Operation return Fact?

Output Presentation:
    what closed external shape may carry that authorized public claim?
```

Failure and diagnostic representation, movement, and bounds remain separate category concerns. This ADR preserves their
attribution boundaries but does not define their complete processing profiles.

---

## 2. Problem

Without an explicit Fact declaration, core information is easily hidden inside implementation classes, mutable fields,
getters, repository lookups, object identity, or backend storage. The machine then has to execute implementation to
discover what it knows.

Without a separate Invariant contract, the standing internal integrity of established Facts is easily hidden inside
constructors, validators, services, persistence hooks, operation implementations, or state managers. The machine cannot
distinguish information, operation-specific change meaning, and the standing law that every authoritative Fact of one
declared kind must satisfy.

Without a separate Publication contract, internal Fact material or Fact-derived meaning can leak outward merely because
an implementation result is easy to return or serialize. That would couple external users to core representation and
would collapse internal knowledge into public claim.

Without a separate Output Presentation contract, the external claim shape is easily hidden inside a Publication mapper,
host constructor, generated return carrier, serializer schema, or transport format. The machine could then know that an
outward claim is authorized without having a separate contract declaration for the closed external shape in which that
claim may appear.

The machine therefore needs explicit immutable factual material, explicit judgment over that material, explicit legal
movement of availability, a separate outward-claim authority, and a separate outward-presentation boundary.

---

## 3. Decision Drivers

Fact must be contract material, not a class, row, DTO, record, Value Object, entity, object graph, repository entry, or
storage layout.

Everything the contract core contains as material must be available as explicit immutable Fact material. The
manifest-selected Input Presentation is admitted, canonicalized where required, lowered into ordinary host material for
the Operation parameter type, and judged as the corresponding input Fact before the implementation is invoked. The same
host parameter remains an ordinary Java or Kotlin value; established Fact authority is a contract-machine decision, not
a wrapper or user-visible subtype. An Operation may consume other established Facts only through separate explicit Fact
participation bindings. Core membership does not grant universal Fact access, and no Operation or judgment may use an
established Fact outside its declared binding.

Ordinary primitive and closed immutable host types may nominate Fact coordinates when they preserve the required
information. Kontrakt must not require a proprietary wrapper merely to make a value look internal.

The Fact kinds admitted to an enclosing interface's explicit core must be declared through exact source-symbol
references. Package membership, directory placement, classpath discovery, reflection scanning, or a runtime collection
may not grant Fact eligibility. Adding a neighboring immutable and behaviorless class must not change the Fact
vocabulary unless an explicit named declaration is edited.

A named data-only host declaration may group those exact Fact surface references once for the enclosing interface. The
host declaration is frontend evidence only. It is not Fact, a runtime collection, or core material, and its class
mechanics carry no contract authority after resolution and Lowering.

Fact meaning must remain separate from physical representation. A backend may replace object fields with primitive
arrays, packed bytes, generated tables, or another deterministic layout without changing the Fact. Two realizations of
the same Fact kind with the same canonical factual material are the same Fact. Host object identity and allocation
history may not create another factual distinction. Host-language equality and hashing are not Fact authority. Kontrakt
does not consult user-defined equality or hashing when determining Fact sameness. They may neither create a factual
distinction between identical canonical material nor erase a factual distinction between different canonical material.

Invariant must be declared once at the enclosing interface's explicit core scope beside the Fact vocabulary. It protects
Fact Integrity by declaring a standing law over one exact Fact kind. An Operation does not select, own, or repeat that
law. When an internally formed Change Proposal contains proposed material for that Fact kind, Kontrakt must
automatically judge each proposed Fact before any part of the proposal receives authority.

The user must not declare, receive, or inspect Change Proposal, current-versus-proposed wrappers, overlay views, Fact
collections, populations, histories, graphs, or backend storage coordinates. An Invariant declaration names one Fact
kind and one total deterministic Boolean relation over that Fact's complete canonical factual material. It may not grant
information its factual meaning, invent how Facts change, inspect operation behavior, acquire another Fact, construct a
population, query hidden state, or use runtime object relations as authority.

State and Transition must remain separate from Fact and Invariant. They judge whether State movement declared in the
same Change Proposal is legal, while Invariant judges Fact Integrity under that proposal. Neither may substitute for the
other, and neither makes a class or return value factual.

Publication must be an explicit judgment. Immutability and internal availability do not make core material public.

Established Facts and Fact authority do not leave the Contract Core. Every Fact coordinate is non-public by default.
Publication may grant outward claim authority only from the one established Fact kind named by the Operation return and
only through exact positive source-to-target coordinate relations declared beside the operation manifest. A source
coordinate omitted from those relations has no publication path. The relation owns permission and factual dependency,
not physical representation formation. Every selected Publication therefore produces one required retained plain
host-language realization port, and exactly one implementation must be bound during machine assembly. Publication does
not own the outward presentation shape, and the supplied implementation does not own Publication authority.

Output Presentation must be a separate explicit contract role. It declares the closed external shape in which an
authorized public claim may appear. It owns public coordinate names, public value shapes, explicit absence, and finite
external alternatives. It does not decide whether a claim is authorized and does not acquire Fact authority.

The interface contract separates five surfaces: the manifest-selected external Input Presentation, the ordinary
Operation parameter whose resolved kind must hold established input Fact authority before invocation, the ordinary
Operation return whose resolved kind must be established as the successful result Fact before contractual completion,
the operation-local Publication relation selected by the manifest, and the selected Output Presentation Contract that
declares the external shape. The user Operation implementation exposes no candidate, proposal, established-Fact wrapper,
or Kontrakt orchestration. It remains the same ordinary Java or Kotlin implementation when Kontrakt is removed. The
generated Publication port and its ordinary adapter may remain as retained host-language compatibility artifacts.
Kontrakt governs the invocation and Publication orchestration outside those implementations and treats returned result
material as proposed Fact material until every applicable Fact, Invariant, State, and Transition judgment succeeds and
the complete change is established.

External publication must expose only material formed under the selected Output Presentation Contract, not the internal
Fact carrier, canonical Fact IR, Change Proposal, Operation Result Material, or backend representation.

A backend may optimize Fact storage, Invariant evaluation, movement checks, the closed Publication realization binding,
Output Presentation assembly, and emission, but it may not change factual meaning, judgment law, state movement,
publication permission, declared coordinate dependency, or outward shape. It may devirtualize, inline, specialize, or
erase the retained port only where equivalent behavior is proven; otherwise the explicit port call remains.

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

manifest-selected Input Presentation
    -> Admission
    -> optional Canonicalization
    -> Lowering into ordinary host material for the Operation parameter type
    -> Fact sameness and declared uniqueness for the proposed input Fact
    -> every interface-level Invariant judgment applicable to that proposed input Fact
    -> established input Fact authority
    -> ordinary user implementation invocation

ordinary implementation result material
+ any declared immutable Operation Result Material
+ any additional declared Fact changes
+ any declared State movement
    -> one internally formed Change Proposal where factual or State change is proposed
    -> Fact sameness and declared uniqueness for each proposed Fact
    -> every interface-level Invariant judgment applicable to each proposed Fact
    -> every applicable State / Transition judgment over movement
    -> indivisible establishment of the whole accepted change
    -> successful contractual Operation completion with the declared return as established Fact authority

one established Operation return Fact in the selected Publication context
    -> finite Publication applicability judgment
        -> declared publication stop when no outward claim is authorized
        -> operation-local positive source-to-target coordinate relations when publication is authorized
    -> generated retained Publication realization port
    -> exactly one explicitly bound realization
    -> authorized target-coordinate material
    -> selected Output Presentation Contract
    -> generated canonical outward presentation assembly
    -> replaceable backend encoding and emission
```

The declared Fact surface provides the immutable information definition used by the core. It does not describe one
implementation object, does not require that the information was produced by an Operation, and does not require a second
user-authored Fact Contract, identity declaration, equality function, or hash function. Its factual meaning may include
declared provenance, so the Fact that a source made a claim, submitted a request, reported a measurement, or produced an
observation does not make the claim's content a stronger Fact than the declaration permits.

The enclosing interface binds one named Fact vocabulary declaration and one named Invariant declaration carrier at
interface scope. The Fact vocabulary declaration is authored through a restricted Kotlin or Java frontend law: a named,
uninstantiable type-signature declaration lists exact Fact surface type references, and the interface IDL references
only that declaration symbol through `facts`. The Invariant carrier is also a named, uninstantiable Kotlin or Java
type-signature declaration. It lists exact Invariant declaration symbols, and the interface IDL references only that
carrier symbol through `invariants`. Facts and Invariants are declared once for the explicit core and are not repeated
inside each Operation manifest.

Fact vocabulary membership declares which Fact kinds may be established in that core. It does not grant every Operation,
Transition, Lowering, or Publication universal participation authority. Each such contract still owns the explicit Fact
bindings through which its declared roles may participate. Each Invariant declaration instead fixes one exact Fact kind
through one direct Fact parameter. Kontrakt applies that standing law automatically to every proposed establishment of
that Fact kind.

The Invariant Contract declares a standing law over one established Fact kind. The user writes one total Boolean
relation against one ordinary Fact carrier and does not observe a Change Proposal, another Fact, or a Fact collection.
When an Operation causes Kontrakt to form one Change Proposal internally, Kontrakt resolves each proposed Fact, selects
the interface-level Invariants declared for that Fact kind, and judges each law solely against that Fact's complete
canonical factual material. Invariant protects Fact Integrity, but it does not define the Fact changes, inspect the
Operation's algorithm, manufacture Fact meaning, authorize State movement, or convert an implementation object into
information.

The State and Transition axis governs legal movement and availability. It judges any State movement bound to the same
Change Proposal under authority separate from Invariant. Before successful Lowering, the proposal's proposed Fact
changes are not Facts and its proposed movement has not occurred. If any required Invariant or State / Transition
judgment refuses the proposal, Lowering is refused and no part of that proposal is established.

A Change Proposal is explicit Operation-produced judgment material held at the Boundary of Fact authority, not a new
contract authority and not core material. The selected Operation's declared bindings must already state the Fact changes
and any State movement that the proposal carries. The proposal may not invent change meaning through callbacks, hidden
mutation, runtime discovery, or Invariant evaluation. Indivisible successful Lowering is a contract-observation rule: no
declared part becomes authoritative unless every required judgment succeeds.

The Publication Contract is the outward-claim authority. Established Facts and Fact authority do not leave the Contract
Core. Declared Operation Result Material is machine-internal material outside Fact authority and is never Publication
source authority. The selected Operation's contractual return is the Fact kind declared by its return surface only after
the returned result material has been established. The operation manifest selects one Publication handle, while one
sibling `publication` declaration inside the same operation states the exact positive source-to-target coordinate
relations. Publication receives the established return Fact through operation context, decides whether one of its finite
declared public-claim alternatives is applicable, and grants authority only to the factual meaning admitted by those
relations.

The Output Presentation Contract is the outward-shape authority. It is selected separately and declares the closed
external coordinates, value shapes, absence, and finite alternatives that may carry the authorized claim. It is not
Fact, does not retain Fact authority, and does not decide Publication permission. The compiler resolves the Publication
relations against that shape, generates one retained plain host-language realization port, and requires exactly one
implementation during machine assembly. The implementation performs physical representation formation only. The
generated machine owns applicability judgment, coordinate authority, target completeness, Output Presentation assembly,
failure routing, and the handoff to backend encoding and emission.

This ADR therefore extends the one-dimensional catalog with `Output Presentation Contract`. The new role is required
because the ordinary Operation return is the internal result surface governed as Fact under Kontrakt rather than the
external output shape. Publication remains one judgment role; Output Presentation is the separate outward shape that
follows it.

The common role law is:

```text
Fact declares established immutable information that belongs to the core.
Input Presentation declares the external material shape selected at the inbound Boundary.
The Operation parameter remains an ordinary host value whose resolved kind must receive established input Fact authority before the user implementation is invoked.
Operation Result Material declares explicit immutable machine-internal material produced during realization outside Fact authority; it is not the Operation return and is not Publication source authority.
The Operation return remains an ordinary host result whose resolved kind constitutes the established output Fact of successful contractual completion.
Change Proposal is formed internally by Kontrakt when returned result material, additional Fact changes, and State movement must be judged together; it is not part of the user implementation surface.
Invariant declares what every established Fact of one declared kind must satisfy; Kontrakt applies that law automatically to every proposed Fact of that kind.
State and Transition declare whether the same proposal's State movement is legal.
Successful establishment grants Fact authority and completes the Operation only after every required judgment succeeds; refusal means Operation failure.
Publication grants outward claim authority through exact non-public-by-default source-to-target coordinate relations from the established Operation return Fact without transferring Fact authority out of the core.
The generated Publication port realizes those declared relations physically without owning their authority.
Output Presentation declares the closed external shape that may carry the authorized public claim without deciding Publication permission.
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
how a Fact is present, but those mechanisms do not create another contract category or own factual meaning.

A declared Fact surface provides the coordinates, sorts, distinctions, bounds, provenance, scope, version meaning, and
other factual values required by that information. It does not require a separate user-authored Fact Contract body. A
Fact declaration may also state which factual coordinate tuples must be unique within that Fact kind. Declared
uniqueness is not Fact sameness and does not imply automatic replacement: two Facts that differ in any canonical factual
value remain different Facts, but a resulting Fact world may not contain both when they collide on a declared unique
tuple.

Currentness, cardinality across separately established Facts, supersession, grouping, continuity, history, and topology
are not implicit properties of every Fact and are not synthesized by Invariant. When such meaning is required, it must
be expressed by the applicable Operation, Change, State, or Transition contract, or as explicit factual material wholly
contained by one Fact. Facts do not reference other Facts. A factual coordinate such as `accountId` is factual material
only. The same coordinate value appearing in another Fact does not create an object relation, lookup obligation,
existence obligation, graph edge, or automatic cross-Fact binding. The declared Fact surface exposes factual coordinates
that another contract may explicitly use as operands, but it does not list every Invariant that may apply. The
interface-level Invariant carrier declares those standing laws separately, and each referenced Invariant declaration
fixes its own factual scope. The declared Fact surface does not define Operation-specific change meaning. The selected
Operation's explicit bindings declare what Fact changes its Change Proposal carries. Fact is not universally an entity,
event, identifier, persisted record, or state snapshot.

A host frontend may use primitives, strings, enums, arrays under an immutable profile, finite products, Kotlin data
classes, or Java records as declaration evidence when their complete visible shape can be refined. Kontrakt does not
require `CoreInt`, `KontraktText`, or a user-authored Value Object merely because information exists inside the core.

A domain-named host declaration remains frontend evidence. Kontrakt does not carry a Kotlin data class, Java record, or
another domain object into the Contract Core as the Fact itself. Resolution and Lowering retain domain-neutral canonical
contract IR: the resolved Fact kind symbol, canonical coordinate declarations, value sorts, factual distinctions,
applicable contract-world material, and source coordinates required for attribution. Source names may remain as resolved
symbols, but host constructors, methods, receivers, object identity, and class layout do not survive as Fact authority.
A backend may realize the lowered material through generated classes, primitive arrays, packed bytes, tables, or another
deterministic layout. Those realizations are implementation and may not replace the canonical Fact material from which
they were generated.

The Fact vocabulary for an enclosing interface is also authored through a restricted host declaration rather than a
package selector, class literal collection, or new IDL body language. A Kotlin frontend may express the declaration as
an uninstantiable type signature:

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

The constructor is never invoked and the class is not the contract. Its parameter type positions provide exact source
symbols for the Fact surface declarations. Parameter names, source order, constructor identity, allocation, generated
JVM shape, and host class identity carry no contract meaning. Resolution and Lowering retain only an order-independent,
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

    operation deposit(input: DepositOperationInput): DepositRecorded {
        manifest {
            flow:
                input          DepositInputPresentation
                admission      DepositAdmission
                canonical      DepositCanonicalization
                lowering       DepositLowering
                publication    DepositPublication
                presentation   DepositOutputPresentation
        }

        lowering DepositLowering {
            presented.accountId    -> input.accountId
            presented.amountMinor  -> input.amountMinor
        }

        publication DepositPublication {
            result.depositId    -> output.depositId
            result.amountMinor  -> output.amountMinor
        }
    }
}
```

`facts AccountFacts` declares the Fact vocabulary eligible for establishment in that interface's explicit core.
`invariants AccountInvariants` declares the standing Fact laws that govern that same core. Neither declaration is an
Operation participation list. Every Operation remains limited by its own explicit Fact bindings, while each Invariant is
limited to the one exact Fact kind resolved from its direct Fact parameter and is applied automatically when that Fact
kind is proposed for establishment.

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
applicable contract world. A collision must never merge different Facts.

For Fact authority, the core therefore has set-like semantics:

```text
{ F, F } = { F }
```

This is a contract observation law, not a requirement to use one particular set data structure or storage layout. When
count or repeated observation is factual meaning, that meaning must be represented explicitly rather than inferred from
duplicate delivery, construction, or storage. The same Fact may be realized through different languages and backend
layouts without changing its meaning.

### 5.2. Invariant Contract

Invariant is the standing internal integrity law of one exact Fact kind.

Fact declares what immutable information may hold authority inside the core. Invariant declares what one complete
canonical candidate for one exact declared Fact kind must satisfy before that material may receive Fact authority. An
established Fact is therefore material that has already satisfied every applicable Invariant. Invariant is not an
Operation postcondition, an Admission rule, a constructor guard, a callback validator, or a rule for how one Operation
computes or forms its result. Operation-specific obligations remain in the complete operation contract pipeline.
Invariant owns only the remaining intra-Fact integrity after Fact sameness and declared uniqueness, State and Transition
movement, Policy, Budget, Capacity, Governance, Publication, and operation-specific formation retain their own
questions.

Facts and Invariants share the enclosing interface's explicit core scope:

```text
interface AccountService {
    facts AccountFacts
    invariants AccountInvariants

    operation deposit(input: DepositOperationInput): DepositRecorded {
        manifest {
            flow:
                input          DepositInputPresentation
                admission      DepositAdmission
                canonical      DepositCanonicalization
                lowering       DepositLowering
                publication    DepositPublication
                presentation   DepositOutputPresentation
        }

        lowering DepositLowering {
            presented.accountId    -> input.accountId
            presented.amountMinor  -> input.amountMinor
        }

        publication DepositPublication {
            result.depositId    -> output.depositId
            result.amountMinor  -> output.amountMinor
        }
    }
}
```

`invariants AccountInvariants` names one restricted Kotlin or Java source catalog. It is declared once for the core and
is not repeated or selected inside an Operation manifest. An Operation does not own an Invariant and cannot bypass one
by omitting a slot. Whenever complete canonical candidate material for an Invariant's declared Fact kind may receive
Fact authority, Kontrakt applies that law automatically.

The catalog is an uninstantiable type-signature declaration that lists exact Invariant declaration symbols:

```kotlin
class AccountInvariants private constructor(
    nonNegativeBalance: NonNegativeBalance,
)
```

The constructor is never invoked. Parameter names, source order, allocation, receiver identity, and generated JVM shape
carry no contract meaning. The parameter type positions identify the exact Invariant declarations. The catalog contains
no properties, executable roots, computed discovery, inheritance, annotations, callbacks, reflection, or runtime
collection. An equivalent restricted Java declaration may provide the same type-signature material.

Each referenced symbol declares one direct Fact law:

```text
a total deterministic Boolean relation
with exactly one parameter
whose type is one Fact kind declared by the same interface-level Fact vocabulary
and whose admitted operands come only from that Fact's complete canonical factual material
```

The source class and method are frontend evidence only. Kontrakt must completely resolve and lower the admitted relation
and erase its host execution mechanics before ContractImage publication. The class is not an object-oriented contract,
the method is not runtime authority, and the Fact parameter is not an object reference. The parameter type resolves one
Fact kind symbol; field access resolves canonical coordinate operands; and the admitted Boolean body lowers to
domain-neutral Invariant relation IR. Kontrakt does not retain a domain-specific validator class or invoke the source
method as the contract at runtime.

The canonical ContractImage may therefore retain IR equivalent to:

```text
subject Fact kind symbol
canonical coordinate operands
lowered total Boolean relation
authorized constants and value sorts
source coordinates for attribution
```

It does not retain the host class as the Fact, the method receiver as judgment authority, or a domain-specific compiler
node whose behavior owns the law.

A direct Fact law uses an ordinary restricted Boolean root:

```kotlin
class NonNegativeBalance private constructor() {

    fun preserved(
        balance: Balance,
    ): Boolean =
        balance.amountMinor >= 0L
}
```

This declaration means that complete canonical material proposed as `Balance` may receive Fact authority only when the
stated coordinate relation is preserved. The source body states only what must be true of one complete Fact. It performs
no lookup, population acquisition, grouping, iteration, reduction, navigation, formation, or movement.

The Invariant Contract provides only the following intra-Fact integrity functions:

```text
value-domain integrity:
    one factual value must remain inside its declared domain or range

coordinate ordering:
    factual positions or bounds contained by the same Fact must preserve their declared order

arithmetic coherence:
    explicit factual quantities contained by the same Fact must satisfy an exact declared relation

coordinate compatibility:
    individually admitted factual values contained by the same Fact must form an allowed combination

conditional presence and absence:
    one factual condition contained by the same Fact may require another explicit material to be present or absent

finite-alternative coherence:
    the selected finite alternative and the explicit material carried by the same Fact must agree

flag and bit compatibility:
    explicit factual flags or bits contained by the same Fact must satisfy required, forbidden, and mutually exclusive combinations
```

Examples include:

```kotlin
class ValidPeriod private constructor() {

    fun preserved(
        period: Period,
    ): Boolean =
        period.startEpochMillis <= period.endEpochMillis
}

class ValidAmountBreakdown private constructor() {

    fun preserved(
        amount: AmountBreakdown,
    ): Boolean =
        amount.totalMinor ==
                amount.principalMinor + amount.feeMinor
}
```

A derived coordinate should not be added merely to create an Invariant. When one authoritative factual coordinate is
sufficient, duplicating a calculable value creates unnecessary material and an unnecessary law. Invariant preserves
explicit factual meaning that must exist; it does not justify redundant representation.

Aggregate is not an Invariant operation. A Fact may contain explicit scalar coordinates whose domain meaning is an
aggregate, total, count, balance, or other summary. Invariant may compare those already formed coordinates under
arithmetic coherence because they are part of the one complete canonical Fact. It may not derive them by acquiring,
collecting, traversing, filtering, grouping, counting, summing, reducing, or otherwise interpreting nested, external, or
separately established material. Invariant does not establish that an aggregate coordinate was calculated correctly from
source entries. The calculation belongs to replaceable realization, and any contractually meaningful relation between
explicit operation material and the produced outcome must have an explicit owner elsewhere in the operation pipeline.
This ADR creates no Result Contract and assigns no such obligation automatically.

When an authored Fact declaration independently includes scalar coordinates with aggregate meaning, those coordinates
lower as ordinary canonical coordinates of that resolved Fact kind. Any applicable arithmetic law lowers as a
domain-neutral relation over those coordinate operands. The canonical IR does not retain a domain object, a summary
object, or an aggregate-specific contract node. It retains only the resolved Fact-kind symbol, the admitted canonical
coordinate operands, and the lowered relation.

Kontrakt must not synthesize a summary Fact kind, an aggregate-specific Fact declaration, a domain-specific aggregate
validator, or another Fact kind merely to expose calculated values to Invariant judgment. When the authored Fact
vocabulary does not independently declare those values as factual material, calculated totals remain Operation Result
Material, processing IR, or backend implementation material according to their actual role and receive no Fact
authority.

An Invariant receives exactly one Fact. Kontrakt rejects all of the following rather than inventing missing contract
material:

```text
several Fact parameters
Set<Fact>, List<Fact>, Sequence<Fact>, Stream<Fact>, array, iterator, cursor, or another Fact container
another Fact obtained through lookup, navigation, repository, callback, or hidden context
an implicit collection of all Facts of one kind
an implicit group formed from equal coordinate values
an implicit sequence, history, interval set, relation, or graph formed from separately established Facts
```

Facts do not reference other Facts. Shared coordinate names, shared coordinate types, and equal coordinate values create
no implicit relation, join, lookup obligation, existence obligation, or judgment scope. Kontrakt does not construct a
collection, population, group, sequence, history, interval set, or graph from separately established Facts for Invariant
judgment.

Accordingly, Invariant does not provide standing laws over several separately established Facts. It does not count,
aggregate, conserve, order, compare intervals, enforce continuity, judge coexistence, establish coverage, partition a
population, or derive historical monotonicity across Facts. Removing such a law from Invariant does not assign it
automatically to Admission, Lowering, Change, State, Transition, Publication, or any other contract role. When a domain
requires such an obligation, its explicit subject, material, scope, and owning contract must be determined separately.
This ADR decides only that an implicit cross-Fact judgment is not Invariant.

Declared uniqueness is not restated as an Invariant. Fact owns the coordinate tuples declared unique for its Fact kind.
Invariant neither discovers nor rechecks that law. Declared uniqueness forbids conflicting coexistence; it does not
silently grant supersession, replacement, currentness change, or a relation between Facts.

The decisive test is simple:

```text
Can this law be judged completely from one complete canonical candidate Fact
without observing another Fact, Operation Input, Operation Result Material,
State, history, collection, repository, or external source?

yes:
    it may be an Invariant

no:
    it is not an Invariant
```

Invariant bodies may use ordinary Kotlin or Java syntax only when the frontend can completely refine the body into
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

Whether Java or Kotlin syntax can execute is irrelevant; only the closed relation completely lowered from that syntax is
authoritative. The source may not call arbitrary user behavior merely because that behavior returns `Boolean`.

Every Invariant declaration must be total, deterministic, and terminating. Host overflow, exception, `NaN`, locale,
timezone, comparator behavior, collection implementation, object identity, or unspecified evaluation behavior may not
decide Invariant meaning. When numeric totality cannot be proved or fixed by an explicit Kontrakt semantic profile, the
source is rejected rather than interpreted through accidental JVM behavior.

The Invariant source may not observe or perform any of the following:

```text
Operation Input, Operation Result Material, or implementation-local values
Change Proposal, current/proposed pairs, candidate wrappers, or overlay views
another Fact, a Fact reference, a Fact collection, or a hidden Fact context
State, Transition, Policy, Budget, Capacity, Governance, Publication, or Diagnostic material
external database rows, repository, service, network, file, clock, randomness, environment, thread, lock, or transaction state
reflection, runtime class discovery, proxy behavior, object identity, custom equals, hashCode, compareTo, or Comparator
Fact mutation, repair, normalization, parsing, replacement, removal, currentness change, or State movement
instance state, mutable globals, initialization behavior, inheritance, override dispatch, or interface-provided behavior
collection operations, streams, sequences, loops, recursion, callbacks, lambdas, method references, escaping closures,
exception-driven choice, or arbitrary user method calls
backend index, cache, counter, accumulator, delta, incremental-maintenance state, or evaluation plan
```

The user does not declare or observe a Change Proposal. When an Operation's complete contract pipeline causes Kontrakt
to form one Change Proposal internally, Kontrakt resolves every proposed Fact independently under its declared Fact kind
and canonical factual material. For each proposed Fact, Kontrakt selects every interface-level Invariant declared for
that exact Fact kind and judges those laws before Fact authority is granted.

```text
one complete internally formed Change Proposal
    -> each proposed Fact material
        -> resolved Fact kind and complete canonical factual material
        -> applicable Fact-owned sameness and uniqueness judgments
        -> every interface-level Invariant declared for that Fact kind
            -> all preserved
                -> that proposed Fact remains eligible for indivisible establishment
            -> any refused
                -> the whole Change Proposal is refused
    -> every applicable State / Transition movement judgment
    -> only complete success permits indivisible establishment
```

The diagram names judgment dependencies, not a required execution order. A backend may fuse, reorder, specialize, or
statically discharge judgments only when the same Fact-local law, same proposal-level atomicity, same refusal
attribution, and deterministic outcome remain unchanged.

Invariant does not define how Facts change, inspect whether an Operation used the correct algorithm, compare
independently established Facts, or repair a refused Fact. It returns only the standing-law judgment over one proposed
Fact. State and Transition independently judge movement legality over the same internal proposal. Neither authority
substitutes for the other.

Contract cycles are not Invariant violations. Recursive contract composition and cyclic contract dependency are
structurally inadmissible and must be rejected by compiler validation before Lowering. A cyclic State Machine Manifest
is likewise rejected by its own structural validation under the one-way movement law. Kontrakt does not admit a cycle
and then ask an Invariant to detect it. Factual coordinates such as `fromId` and `toId` do not cause Kontrakt to
construct an implicit graph from separately established Facts.

At definition time, Kontrakt must:

```text
resolve the exact interface-level Invariant catalog symbol
resolve every exact Invariant declaration symbol listed by that catalog
resolve exactly one direct Fact parameter against the interface-level Fact vocabulary
refine the complete admitted Boolean relation over that Fact's canonical factual coordinates
reject additional Fact parameters, Fact containers, hidden Fact acquisition, unknown behavior, partiality,
nondeterminism, recursion, unbounded work, aggregation, collection traversal, and unsupported operations
lower each declaration into canonical Fact-local Invariant material
derive stable Fact-kind and coordinate dependency indexes
publish the resulting laws in ContractImage-visible form
```

From the same canonical Invariant material, Kontrakt owns and may optimize:

```text
applicable-law selection by exact Fact kind
direct coordinate dependency selection
specialized runtime gates and primitive comparisons
constant folding and shared coordinate reads
static discharge where the law follows from stronger canonical material
generated valid, boundary, and violating fixtures
generated property-based tests and shrinking guidance
refusal and diagnostic attribution
deterministic backend-specific realization
```

No collection scan, group index, aggregate counter, interval structure, history traversal, or graph algorithm is
required or authorized by the Invariant Contract defined here. Any backend machinery used for Fact storage or Fact-owned
uniqueness remains implementation of those separate concerns and may not enlarge Invariant meaning.

Invariant is not a validator drawer and not a constructor guard. Its outcome is visible to the machine and attributable
to the named standing law that produced it, but the source catalog, declaration classes, host methods, and generated JVM
mechanics receive no runtime contract authority.

### 5.3. Publication Contract

Publication is the only outward-claim authority.

Every established Fact and every Fact coordinate is non-public by default. Core membership, immutability, successful
Operation completion, persistence, host return compatibility, and serializer reachability grant no outward authority. A
Fact kind for which no Publication Contract is selected has no legal egress path.

The interface contract keeps the relevant surfaces separate:

```text
manifest input slot
    -> external Input Presentation

Operation parameter
    -> ordinary host input whose resolved kind has established Fact authority before invocation

Operation return
    -> ordinary host result whose resolved kind becomes established Fact only on successful contractual completion

manifest publication slot
    -> selects one operation-local Publication handle

operation-local sibling publication declaration
    -> exact positive source-to-target coordinate relations
       from the established Operation return Fact

generated retained Publication realization port
    -> replaceable physical formation of the declared target-coordinate material

manifest presentation slot
    -> closed Output Presentation shape for the authorized claim
```

The manifest does not repeat `facts` or `invariants`. Those declarations belong once at the enclosing interface scope.
It does not introduce a `result` slot either. The Operation return type is already the ordinary declared result surface.
The user implementation returns that result exactly as an ordinary Java or Kotlin implementation would and contains no
Kontrakt orchestration. Kontrakt judges the returned material outside the implementation; the contractual Operation does
not complete successfully until every applicable Fact, Invariant, State, and Transition judgment succeeds and the
complete change is established. Operation Result Material remains machine-internal and is not Publication source
authority.

One `publication` slot selects one complete Publication Contract. The slot is not divided into separate source,
exposure, serializer, or emitter slots. One sibling `publication` declaration inside the same operation owns the exact
positive source-to-target coordinate relations. Its source Fact kind is resolved from the Operation return declaration,
and its target coordinates are resolved against the separately selected Output Presentation Contract. The user does not
repeat either type inside the manifest. Publication also owns finite applicability bindings where required, finite
alternative and absence claim bindings, and declared publication stops. It does not own the Output Presentation shape.
Every selected Publication generates one required retained plain host-language realization port, and exactly one
implementation must be supplied during machine assembly.

The operation-local shape is:

```text
operation deposit(input: DepositOperationInput): DepositRecorded {
    manifest {
        flow:
            publication    DepositPublication
            presentation   DepositOutputPresentation
    }

    publication DepositPublication {
        result.depositId    -> output.depositId
        result.amountMinor  -> output.amountMinor
    }
}
```

The `manifest` and `publication` declarations are siblings inside the operation. The manifest selects the handle. The
sibling body declares the exact coordinate authority relation. It does not implement that relation.

The relation body is positive authority, not a list of fields to hide and not an executable mapper. Only an explicitly
bound Fact coordinate may justify one target coordinate declared by the selected Output Presentation. A Fact coordinate
omitted from the Publication Contract is not conditionally hidden; it has no publication authority and no egress path.
Adding a new Fact coordinate does not change an existing public claim until the applicable Publication relation is
explicitly edited. Equal names, equal host types, catalog availability, serializer compatibility, and backend support do
not create an implicit relation or close the required realization port.

Publication applicability, coordinate authority, and physical realization are distinct:

```text
applicability:
    whether this already-declared public-claim alternative is authorized
    for finite machine material judged elsewhere

coordinate relation:
    which exact established Fact coordinate is the permitted factual basis
    for which exact coordinate of the selected Output Presentation

realization:
    how one explicitly bound Publication adapter physically forms
    the declared target-coordinate material
```

Applicability may bind only to finite machine material already declared and judged elsewhere. Publication may not
execute an arbitrary exposure predicate, inspect hidden State, query Policy or Governance through a callback, discover
Facts, perform repository lookup, or infer public meaning. No Fact coordinate becomes public merely because an
applicability alternative is selected.

The coordinate relation is not assignment, a cast, a parser, a constructor call, or a fallible runtime mapping
algorithm. It declares permitted factual basis and target dependency. During Publication and Output Presentation
resolution, Kontrakt must reject a contract whose target coordinate is unbound, whose source or target coordinate cannot
be resolved, whose relation is ambiguous or duplicated, whose factual distinctions are incompatible, whose alternative
or absence relation is not total, or whose source and target sorts cannot be exposed through one valid generated port
ABI. These are structurally inadmissible contracts, not runtime Publication refusals. A valid canonical Publication IR
therefore contains only resolved deterministic authority relations. Missing, duplicate, or ABI-incompatible supplied
implementations are machine-assembly failures, not Publication stops.

A runtime publication stop means that no declared public-claim alternative is authorized for the already-established
machine alternative. It is not a realization failure. A declared refusal from the bound Publication implementation,
undeclared exception, Output Presentation assembly defect, encoding, buffering, transport, or I/O failure is likewise
not Publication judgment; each retains separate realization, failure, and diagnostic attribution.

Kontrakt lowers a valid Publication declaration into domain-neutral canonical material such as the resolved Operation
return Fact kind, exact source-to-target authority relations, finite applicability and alternative bindings, explicit
absence claim material, declared publication stops, applicable scope and version, generated port ABI identity, and
source coordinates required for attribution. Source classes, Fact objects, property references, adapter methods,
serializers, and generated carrier layouts do not survive as Publication authority. The explicitly bound implementation
identity belongs to closed machine realization, not to Publication contract meaning.

Publication authorizes exact target-coordinate material under its declared relations. After applicability succeeds, the
generated machine invokes the exactly bound Publication realization through the retained port, accepts only the declared
target-coordinate material, and assembles the separately selected Output Presentation. Established Fact never becomes
outward material. The adapter does not receive Fact authority, decide applicability, enlarge the source set, authorize
another target, or own the resulting Output Presentation contract.

Diagnostic material remains internal and is never a direct Publication source. When an outward diagnostic or failure
claim is required, its public factual basis must be part of the established Operation return Fact, must receive explicit
claim authority in Publication, and must fit the selected Output Presentation. Diagnostic, Failure, Evidence, Retention,
movement, or any other contract role may not substitute for Publication as outward-claim authority.

### 5.4. Output Presentation Contract

Output Presentation declares the closed external shape that may carry an authorized public claim.

It is selected through a separate `presentation` slot after `publication`:

```text
manifest {
    flow:
        publication    DepositPublication
        presentation   DepositOutputPresentation
}
```

The slot gives the role. A host class, record, schema, serializer, response type, or transport payload is not Output
Presentation merely because it looks public. The selected source declaration is frontend evidence that Kontrakt must
resolve and lower into canonical Output Presentation material.

The user authors that source as an ordinary closed immutable host declaration. A restricted Kotlin frontend may use a
plain data class whose primary constructor declares the complete outward coordinate surface:

```kotlin
data class DepositOutputPresentation(
    val depositId: Long,
    val amountMinor: Long,
)
```

An equivalent Java record may declare the same outward shape. The declaration is selected directly by the operation's
`presentation DepositOutputPresentation` slot; Output Presentation needs no interface-level catalog because it is one
operation-specific outward surface. The primary-constructor or record-component positions provide the exact public
coordinate symbols and their host value shapes. Source order, constructor invocation, allocation, receiver identity,
generated `copy`, `componentN`, `equals`, `hashCode`, and `toString` methods, and JVM layout carry no contract
authority.

The admitted source form is shape-only. It must be a finite immutable product or another explicitly admitted finite
alternative composed from supported outward value sorts. It may contain no mutable property, custom method, initializer,
computed or delegated getter, inheritance-based membership, callback, lambda, runtime lookup, reflection discovery,
hidden serializer field, or default argument that supplies undeclared outward material. Explicit absence and finite
alternatives must be visible in the declared shape rather than introduced by a serializer or constructor default.

This declaration does not name a source Fact and does not perform Publication binding. It states only that an authorized
public claim may be carried through outward coordinates such as `depositId` and `amountMinor`. The separately selected
Publication Contract must justify each required coordinate through an exact positive claim binding. If no binding grants
`amountMinor` outward authority, the existence of `amountMinor` in the data class does not make it publishable; the
Publication and Output Presentation pair is structurally unclosed and compilation is refused.

Kontrakt lowers the example to canonical material equivalent to:

```text
presentation symbol:
    DepositOutputPresentation

public coordinates:
    depositId: Int64, required
    amountMinor: Int64, required
```

The canonical material retains the resolved presentation symbol, coordinate identities, value sorts, required or absent
status, finite alternatives, outward bounds, public version meaning, and source coordinates for attribution. It does not
retain Kotlin construction semantics or treat the data-class instance as the contract.

Output Presentation owns only outward shape material:

```text
public coordinate names
public value shapes and distinctions
required and explicitly absent coordinates
finite public alternatives
outward structural bounds
applicable public shape version
```

It does not own Publication permission, Fact participation, Fact-to-claim authority, business calculation, State or
Policy lookup, carrier allocation, serialization, or emission. A complete Output Presentation may exist while no public
claim is authorized for a particular machine alternative. Conversely, Publication cannot authorize a coordinate that the
selected Output Presentation does not declare.

The selected Publication Contract and Output Presentation Contract must close each other statically. Every required
presentation coordinate must receive exactly one admitted claim binding or one explicit absence binding. Every
Publication target coordinate must exist in the selected presentation. Finite alternatives must be complete and
compatible. Failure to close these materials is compiler rejection before ContractImage publication.

Kontrakt lowers the selected declaration into domain-neutral canonical material such as the resolved presentation
symbol, canonical public coordinates, value sorts and distinctions, required and absent coordinates, finite
alternatives, outward bounds, public version material, and source coordinates required for attribution. Host
constructors, default arguments, getters, record mechanics, serializers, annotations, reflection, object identity, and
class layout do not survive as Output Presentation authority.

After Publication has authorized one public claim, the generated machine invokes the exactly bound Publication
realization, verifies that only declared target-coordinate material was formed, and assembles the canonical outward
shape defined by the Output Presentation Contract. A backend may then encode, buffer, return, or transmit a Kotlin data
class, Java record, primitive layout, packed bytes, schema carrier, or another deterministic representation. Those
mechanisms realize transport and storage; they do not define the shape, authorize the claim, or replace the explicit
Publication port binding.

A published Output Presentation carries only its declared external meaning. If it later enters the machine, it is
external material again and must be selected as Input Presentation and pass Admission, optional Canonicalization,
Lowering, and all applicable judgment. Prior Publication grants no Fact authority on re-entry.

---

## 6. Cross-Profile Boundaries

### 6.1. Fact and Carrier

Fact and carrier must not collapse into one role.

A Kotlin class, Java record, database row, event object, primitive array, or packed region may carry Fact material. The
carrier does not become factual authority. Factual meaning remains in the resolved and lowered Fact declaration and its
explicit coordinates.

Changing carrier, allocation strategy, field layout, packing, or backend language does not change the Fact when the
declared information and distinctions remain identical.

The same separation applies to the named Fact vocabulary declaration. A host declaration such as `AccountFacts` is only
a source coordinate from which exact Fact surface symbols are acquired. It is not a behavior-bearing aggregate, a
runtime set, a Fact carrier, or the authority that makes its member types factual. After resolution and Lowering, only
the canonical Fact vocabulary and the canonical factual material of its members remain authoritative.

### 6.2. Fact and Invariant

Fact and Invariant must not collapse into one role.

Fact declares the information that exists, including any Fact-owned uniqueness tuple. Invariant declares the remaining
standing internal integrity law over one exact Fact kind resolved from its own direct Fact parameter. Facts and
Invariants are both declared once at the enclosing interface's explicit core scope. A Fact may exist without a
particular Invariant, and no Invariant takes ownership of Fact sameness, declared uniqueness, operation-specific
formation, or movement.

Invariant success does not create Fact meaning, define Fact-change meaning, grant Fact authority, or authorize State
movement by itself. Invariant refusal means that the proposal would break the selected law and therefore blocks the
whole proposal from establishment under that law. It does not authorize the machine to mutate, repair, or hide
information.

### 6.3. Fact, Operation Return, Publication, and Output Presentation

Operation Result Material, the Operation return, Publication, and Output Presentation are four different roles.

```text
Operation Result Material:
    explicit immutable machine-internal material produced during realization
    outside Fact authority

Operation return:
    ordinary host-language result surface;
    under Kontrakt, its resolved kind is the established output Fact of successful contractual completion

Publication:
    outward-claim authority granted through exact source-to-target coordinate relations
    from that established return Fact

Publication realization port:
    retained plain host-language implementation boundary that physically forms
    only the declared target-coordinate material

Output Presentation:
    closed external shape that may carry the authorized public claim
```

The user implementation remains ordinary: it accepts its declared parameter and returns its declared result without
calling Kontrakt or handling a proposal, candidate wrapper, established-Fact wrapper, Invariant, or Publication object.
Kontrakt governs the invocation outside that implementation. After the ordinary result is returned, Kontrakt may combine
its proposed factual meaning with declared Operation Result Material, additional Fact changes, and State movement in one
internally formed Change Proposal. The returned result does not complete the contractual Operation before every
applicable Invariant and State / Transition obligation succeeds and the whole change is established as one indivisible
decision. If any required judgment refuses, the Operation fails and the returned material is neither a successful result
Fact nor a Publication source.

Operation Result Material is never an alternative Publication source. It remains machine-internal even when its shape is
convenient for serialization or resembles the selected Output Presentation. If information is intended to support an
outward contract claim, that information must first belong to the exact established Fact kind declared by the Operation
return. A temporary value, backend handle, cache key, serialization hint, or implementation-only calculation is not
promoted to Fact merely to make it publishable.

The operation keeps Publication selection, Publication relation, and Output Presentation shape separate:

```text
manifest publication slot
    -> selects one Publication handle

sibling publication declaration
    -> exact positive source-to-target coordinate authority

manifest presentation slot
    -> selects one exact closed outward shape

generated Publication port
    -> realizes only the declared target-coordinate material
```

The Publication relation forms a closed positive whitelist against the selected Output Presentation:

```text
explicitly bound established Fact coordinate
    -> explicitly declared Output Presentation coordinate

unbound Fact coordinate
    -> no publication authority and no egress path
```

Publication does not expose the Fact, join several Facts, combine source roots, traverse a Fact population, calculate an
aggregate, inspect Operation implementation, or derive business meaning. The supplied Publication implementation is
restricted by the same authority boundary: it receives only the declared source material required by its generated port,
forms only declared target-coordinate material, and may not acquire another Fact, perform repository or environment
lookup, inspect State or Policy, or introduce business meaning. Output Presentation does not perform those operations
either. When a required outward claim needs calculated factual meaning, the Operation must establish that meaning in its
declared return Fact before Publication.

The Output Presentation is neither the established Fact nor its carrier and carries no Fact authority. Internal digest,
provenance, relation, scope, version, backend layout, and diagnostic evidence remain absent unless their exact Fact
coordinates receive explicit Publication authority and the selected Output Presentation declares matching outward
coordinates. A presentation that later returns to the machine has only external-material authority and must pass through
the inbound Boundary again.

### 6.4. Fact, State, and Transition

Fact is information. State is the explicit machine condition governing what movement is available. Transition is the
declared movement between conditions.

A state label does not own Fact meaning, and a Fact does not secretly carry lifecycle state. One Change Proposal may
bind both declared Fact changes and State movement that must succeed or fail together. Before establishment, the
proposed Fact changes are not Facts and the proposed movement has not occurred. No second host type is required merely
to represent that difference.

Invariant and State / Transition judge the same Change Proposal under separate authority. Invariant judges Fact
Integrity. State and Transition judge movement legality. Neither consumes, replaces, or controls the other's judgment.
Only when every required judgment succeeds may the whole proposal be established.

### 6.5. Publication and Diagnostics

Diagnostic evidence may explain Fact formation, Invariant refusal, movement refusal, or a declared runtime publication
stop, but evidence does not create or override any of those roles.

A malformed Publication relation or an incompatible Output Presentation is not a runtime refusal. Missing, ambiguous,
duplicated, incompatible, or incomplete relations and unclosed presentation coordinates are compiler rejection and must
never reach an executable egress path. A missing, duplicate, or ABI-incompatible Publication implementation is a
machine-assembly failure. A declared publication stop instead means that no public-claim alternative is authorized for
the established operation outcome or other finite machine material to which Publication applicability is bound.
Publication adapter refusal, undeclared adapter exception, Output Presentation assembly, encoding, allocation,
transport, and I/O failures remain realization failures rather than Publication or Output Presentation judgment.

Retention decides what diagnostic material may survive. Retained diagnostic material is never a direct Publication
source. When an outward diagnostic or failure claim is required, its public factual basis must first belong to the
Operation's established Fact result and must be positively whitelisted by the selected Publication Contract. Failure,
evidence, retention, movement, and any other contract role may not become an undeclared Fact-egress path. Those
contracts remain distinct from Publication even when one runtime path realizes them together.

### 6.6. Handoff from ADR-0048

ADR-0048 defines the selected operation's Boundary presentations, Admission, optional Canonicalization, and the declared
material from which Lowering begins. This ADR fixes the authority reached by that passage: successful Lowering
establishes explicit immutable Fact inside the same contract core used by the enclosing interface scope. Several
operation pipelines may exist under that scope, but they do not create separate factual cores. The shared machine-wide
Policy, Governance, Budget, and Capacity contracts govern the selected operation run.

The Boundary is the controlled passage, not merely a line. Material may be inspected, admitted, canonicalized, bound,
judged, or refused there, but it carries no Fact authority there. Lowering is the authorized crossing from that Boundary
into the contract core. It completes only after the selected Fact declaration and every applicable Invariant and
movement obligation have been satisfied. Any defect that should have been stopped by Input, Admission, Canonicalization,
or factual-meaning binding remains a defect at the inbound Boundary; Invariant is not a catch-all validator for
malformed external presentation or failed conversion.

The core does not receive the Input object, Canonicalization source, Lowering declaration, mapping table, staging
object, or external framework context. It receives only established immutable Fact meaning produced by successful
Lowering and judgment. The ordinary host value passed to the Operation parameter is the realization of the established
input Fact kind declared by that parameter under the selected contract world. Without Kontrakt it remains only the same
ordinary host value; no wrapper, marker, or behavior is added to preserve Fact authority. The Operation may consume
other established Facts only through separate explicit participation bindings. Internal realization may be divided or
fused freely behind those obligations, but the user implementation remains ordinary and does not create nested operation
manifests or invoke Publication, Output Presentation, Lowering, Invariant, or establishment machinery between
implementation steps. When the ordinary implementation result or other declared changes require factual or State
judgment, Kontrakt forms the Change Proposal outside the implementation. That proposal remains outside Fact authority
and must satisfy every required judgment before indivisible establishment and successful Operation completion.

### 6.7. Bound Fact Authority

Contract Core membership does not grant universal participation authority to a Fact.

Every Operation, State or Transition judgment, and Lowering judgment must explicitly bind the factual roles that may
participate in it. Each Invariant instead declares exactly one Fact kind through one direct Fact parameter. Publication
receives exactly the established Fact kind declared by the selected Operation return and grants outward authority only
through the source-to-target coordinate relations declared in the operation-local Publication body. Output Presentation
separately declares the target outward coordinates. The generated port implementation receives only the source material
admitted by those relations and gains no broader Fact participation authority. An established Fact may participate only
through an applicable Operation or judgment binding, as the sole Fact judged by an Invariant declared for its exact
kind, or as that one explicit Publication source. A Fact of another kind carries no authority to participate, even when
it exists in the same Contract Core or appears relevant to the work being performed.

A Fact binding declares participation authority, not ownership of the Fact and not general access to the Contract Core.
It does not transfer Fact authority out of the core and does not authorize the bound Fact to participate in another
Operation, judgment, movement, scope, version, governance world, or outward claim.

Each Invariant may judge only the one Fact kind and factual coordinates resolved from its own direct Fact parameter. The
user declares no proposal binding and receives no other Fact or Fact collection. Kontrakt may not enlarge the
Invariant's factual scope merely because another Fact appears relevant. Equal coordinate values, matching names,
matching types, or apparent domain relevance create no implicit relation, grouping, or participation authority.

Fact leakage occurs when an established Fact participates in an Operation, judgment, movement, or Publication without an
applicable explicit binding, when its participation exceeds the role, scope, version, governance, or purpose declared by
that binding, when an established Fact or its Fact authority leaves the Contract Core, when a source coordinate not
named by the Publication relation reaches an Output Presentation, when a public coordinate appears outside the selected
Output Presentation shape, when a Publication implementation reads or forms undeclared material, or when Fact-derived
meaning becomes outward material without both an applicable Publication Contract and a selected Output Presentation
Contract.

The mechanism by which an implementation locates, presents, isolates, or optimizes access to bound Facts is outside this
decision.

### 6.8. Fact Egress Authority

Established Facts and Fact authority do not leave the Contract Core.

An Operation may produce declared immutable Operation Result Material during realization. That material remains outside
Fact authority and is not outward material or Publication source authority. It may contribute to the internal Change
Proposal by which the ordinary Operation return is judged under its resolved Fact kind, but it cannot bypass
establishment.

Publication is the only contract authority by which selected meaning from the established Operation return Fact may
become an authorized public claim. It does not release the Fact, its carrier, canonical Fact IR, Change Proposal, or
Operation Result Material. Output Presentation is the only role in this ADR that declares the closed external shape in
which that authorized claim may appear. Neither role substitutes for the other.

Fact egress is closed by default. Publication grants positive authority only through the exact source-to-target
coordinate relations declared in its operation-local body. All unbound source coordinates remain non-public regardless
of matching names, compatible host types, catalog availability, serializer behavior, reflection reachability, or backend
convenience. Every selected Publication generates one required retained realization port, but that port grants no
additional Fact authority. Output Presentation likewise grants no additional Fact authority; it only declares the
external coordinates and alternatives available to carry an already-authorized claim.

One operation therefore keeps three distinct outward surfaces:

```text
manifest publication slot
    -> selects outward claim authority and finite applicability

sibling publication declaration
    -> exact positive source-to-target coordinate relations
       and declared publication stops

manifest presentation slot
    -> closed outward coordinates, value shapes, explicit absence,
       finite alternatives, bounds, and public version material
```

The compiler additionally generates one retained Publication realization port from the resolved relation. That generated
port is implementation ABI, not a fourth contract role and not contract authority.

Publication does not execute an arbitrary guard, discover sensitive material, inspect hidden State, query Policy,
combine Facts, or infer external purpose. Its supplied realization may only perform the physical representation
formation admitted by the generated port and declared coordinate relation. Output Presentation does not calculate
business meaning, select source Facts, or authorize disclosure. The user declares the Publication relation and Output
Presentation shape; Kontrakt validates and lowers them under separate authority and closes exactly one implementation
binding for the generated Publication port.

Malformed coordinate relations, unclosed presentation coordinates, incompatible distinctions, and incomplete finite
alternatives are structurally inadmissible and must be rejected before ContractImage publication. Missing, duplicate, or
ABI-incompatible Publication implementations are rejected during machine assembly. Runtime Publication judgment selects
only among valid declared applicability alternatives and may produce an explicit publication stop when no outward claim
is authorized. After authorization, the generated machine invokes the exactly bound Publication realization, verifies
declared target coverage, and assembles the canonical outward shape fixed by the Output Presentation Contract. Backend
encoding, buffering, and emission then realize transport without acquiring Publication or Output Presentation authority.

Authority to produce one Output Presentation does not authorize disclosure through another presentation, Operation
Result Material, Change Proposal, Diagnostic, Failure, Evidence, Retention record, movement record, or other contract
role. Internal digest, provenance, relation, scope, version, or other factual distinctions may appear only when their
exact Fact coordinates receive explicit Publication authority and the selected Output Presentation declares compatible
public coordinates.

A published Output Presentation carries only its declared external meaning. If it later returns to the machine, it is
external material again. Prior publication grants no Fact authority on re-entry and does not bypass Input, Admission,
Canonicalization, Lowering, or any applicable judgment.

Bound Fact Authority governs which established Facts may participate inside an Operation or judgment. Fact Egress
Authority governs which exact source-to-target relations of the established Operation return may support one declared
public claim. The generated Publication port realizes only those relations physically. Output Presentation governs the
external shape that may carry that claim. None of those authorities implies another.

The generated port ABI, explicit implementation binding, target-coverage gate, and retained generated source are fixed
here. Backend-specific encoding, transport, and storage mechanics remain outside this decision.

## 7. Deferred Decisions

This ADR does not decide the final host-language authoring body for individual Fact surfaces or every admitted Output
Presentation shape. It does decide the IDL role structure: `facts` and `invariants` remain interface-scoped
declarations; the Operation parameter and return remain ordinary host-language surfaces whose resolved kinds participate
as the established input Fact and successful output Fact under Kontrakt; the operation manifest selects the external
`input`, the outward `publication`, and the following `presentation` role without introducing `fact`, `invariant`, or
`result`
slots; and one sibling `publication` declaration inside the same operation owns the exact positive source-to-target
coordinate relations. Candidate and established authority remain internal contract-machine states and do not appear as
user-visible types.

One `publication` slot selects one complete outward-claim contract. Its sibling operation-local `publication` body
contains the exact positive source-to-target coordinate relations from the established Operation return Fact, finite
applicability bindings where required, finite alternative and absence claim bindings, and declared publication stops.
One separate `presentation` slot selects one complete Output Presentation Contract containing the closed external
coordinates, value shapes, explicit absence, finite alternatives, outward bounds, and public version material.
Publication does not own that shape, and Output Presentation does not grant Publication authority. Every selected
Publication generates one required retained realization port and requires exactly one implementation binding during
machine assembly.

This ADR also decides that Operation Result Material is not Publication source authority, malformed coordinate relations
and unclosed Output Presentation shapes are compile-time rejection, missing or duplicate Publication implementations are
machine-assembly rejection, and backend encoding and emission remain implementation. The interface references one named,
restricted Kotlin or Java source declaration through `facts`; that declaration names exact Fact surface types and is
completely lowered away. The same interface references one named, restricted, uninstantiable Kotlin or Java catalog
through `invariants`; that catalog names exact Invariant declaration types and is completely lowered away. Each
referenced Invariant declaration must contain one restricted Boolean law over exactly one Fact kind declared by the same
Fact vocabulary. Package membership, classpath scanning, class literals, runtime collections, and computed discovery are
not Fact or Invariant membership authority.

It does not freeze the final Java and Kotlin token spelling or every admitted expression in the direct Invariant Boolean
body. It does fix the authoring boundary: one exact Fact parameter, one total deterministic Boolean judgment, and
operands derived only from that Fact's complete canonical factual material. No Invariant declaration receives another
Fact, a Fact collection, population, history, graph, proposal, operation-specific material, callback, lambda, hidden
lookup, mutation, or backend evaluation state. The compiler may not infer cross-Fact contract meaning from shared
coordinates or arbitrary user behavior.

This ADR does not freeze the exact generated Publication port method decomposition, result carrier, declared
realization-refusal encoding, or machine-assembly factory API. It does fix their authority boundary: the port is
generated only from the resolved operation-local coordinate relations, its source is restricted to declared Operation
return Fact coordinates, its result is restricted to declared Output Presentation target-coordinate material, exactly
one implementation is bound, and the generated source is retained as an ordinary host-language build artifact.

The V1 Output Presentation source boundary remains separate: one operation-specific `presentation` slot selects one
ordinary closed immutable Kotlin or Java shape declaration, and only its admitted constructor coordinates, record
components, explicit absence, and finite alternatives enter canonical Output Presentation material. Publication
authoring may not contain executable mapping, constructor procedure, callback, lambda, repository lookup, State lookup,
Policy lookup, serializer, or emitter. The supplied Publication implementation may perform only physical representation
formation through the generated port and may not decide source authority, claim permission, target participation, or
business calculation. Output Presentation authoring may not decide those matters either. Kontrakt must fully resolve the
contract declarations before ContractImage publication and close the realization binding before executable machine
publication.

This ADR does not define the complete authoring surface by which the enclosing machine contract and its operation flows
declare required existing Fact dependencies, declared Operation Result Material, Change Proposals, additional Fact
changes, State movement bound to the same proposal, or the replaceable implementation realization between them. It fixes
only the semantic separation among external Input Presentation, the ordinary Operation parameter carrying established
input Fact authority under Kontrakt, the ordinary Operation return governed as the successful output Fact, Publication
claim authority, Output Presentation shape, and backend realization.

It does not define the complete state and transition sets for Fact availability, return completion, Invariant refusal,
movement refusal, publication stop, or presentation realization. Those movement surfaces must be designed with the
complete operation flow, failure, diagnostic, and Version material together with the enclosing machine-wide Policy,
Governance, Budget, and Capacity contracts.

It also does not define final persistence layout, cache layout, core Fact storage, backend-specific Output Presentation
carrier layout, public serialization schema implementation, or emitter implementation. Those remain replaceable
realizations behind the declared Publication relation, generated port boundary, and Output Presentation contract fixed
here.

---

## 8. Consequences

The core is now defined as an explicit Fact machine rather than an object graph or execution container that discovers
knowledge through implementation behavior. One enclosing interface may expose several operation pipelines without
dividing that core. Each operation retains its own controlled inbound Boundary, operation-local Publication relation,
and Output Presentation boundary, while machine-wide Policy, Governance, Budget, and Capacity coordinate the finite
resources shared among all of them.

Each enclosing interface names its core Fact vocabulary once through `facts` and its standing Fact laws once through
`invariants`. The referenced restricted host catalogs list the exact Fact surface symbols and exact Invariant
declaration symbols in one place and disappear after canonical Lowering. Operation manifests do not repeat either
declaration, package placement cannot add a Fact or Invariant implicitly, and vocabulary membership does not grant an
Operation or judgment undeclared participation authority.

Fact is explicit immutable information holding authority inside the core, not the ordinary host object that carries an
Operation parameter or return, an Input Presentation, an Output Presentation, a Change Proposal awaiting judgment, a
Value Object, a persistence row, or a backend layout. Successful Lowering and establishment are the authorized passage
by which Boundary material receives input Fact authority before implementation invocation. A refused passage places
nothing in the core and the implementation is not invoked with that material. Existing Facts are already present in the
core and may participate only through an explicit Fact participation binding. Declared Operation Result Material and
ordinary implementation return material may contribute to a factual change only through one internally formed Change
Proposal, and the proposed Facts receive authority only when every required Invariant and State / Transition judgment
over that same proposal succeeds and the change is established as one indivisible decision. Refusal means Operation
failure.

Invariant has one visible Fact Integrity judgment role without becoming the source of Fact meaning, Fact-owned
uniqueness, Fact-change meaning, Operation behavior, or State movement. The user declares one total deterministic
Boolean relation over exactly one Fact and never receives another Fact, a Fact collection, or the Change Proposal.
Kontrakt applies every interface-level Invariant declared for each proposed Fact's exact kind automatically before that
Fact may receive authority. State and Transition judge legal movement separately over the same internal Change Proposal.
Publication remains the sole outward-claim authority rather than an automatic side effect of immutability, return
production, persistence, diagnostics, failure, retention, movement, or serialization. Its operation-local relation
grants that authority only through exact positive source-to-target coordinate bindings. Output Presentation remains the
separate outward-shape authority rather than a hidden part of Publication or a serializer schema. Established Facts and
Fact authority remain inside the Contract Core. Operation Result Material remains machine-internal material outside Fact
authority and outside Publication source authority. The ordinary implementation return becomes the successful
contractual Operation result only after its resolved Fact kind and every associated change hold established authority.
Every selected Publication then generates one required retained plain host-language port, exactly one supplied adapter
physically realizes the declared target-coordinate material, and the selected Output Presentation declares the distinct
external shape that the generated machine assembles.

Primitive and ordinary closed immutable host types may serve as frontend evidence when they preserve the required
information. Resolution and Lowering replace those host declarations with domain-neutral canonical Fact, coordinate, and
Invariant relation IR before ContractImage publication. Domain names may remain as resolved symbols and source
coordinates, but host classes and domain-specific validator objects do not become Contract Core material. Fact sameness
is determined by the resolved Fact kind and complete canonical factual material, not by hidden identity, object
allocation, `equals`, or `hashCode`. Repeated establishment of identical canonical material does not add another Fact or
implicit multiplicity. The core therefore has set-like Fact semantics without requiring a set-shaped backend. Meaningful
count or occurrence remains explicit factual material rather than an effect of repeated delivery, construction, or
storage. Core richness comes from explicit Facts, Operation Result Material, laws, judgments, states, and
transitions—not from behavior-bearing classes or semantically authoritative Value Objects.

Internal Fact authority does not appear in Input or Output Presentations. The interface contract distinguishes the
manifest-selected external Input Presentation, the ordinary Operation parameter whose resolved kind is established as
input Fact before invocation, the ordinary Operation return whose resolved kind is established as output Fact before
successful completion, the manifest-selected Publication handle, its sibling operation-local relation body, the retained
generated Publication port, and the selected Output Presentation Contract. The same Java or Kotlin Operation interface
and implementation remain usable without Kontrakt. The retained Publication port and its ordinary adapter may also
remain as a direct compatibility boundary, but none of those host artifacts then carries Fact, Invariant, movement,
Publication, or Presentation authority beyond ordinary host-language behavior. External users receive only material
formed under the selected Output Presentation from factual meanings explicitly authorized by Publication, while
established Facts and their authority remain inside the Contract Core and core factual representation remains
replaceable. A published presentation that returns to the machine is external material again and receives no implicit
Fact authority from its origin.

The split between ADR-0048 and ADR-0049 keeps optimization honest. The selected operation's Boundary rejects malformed
or inadmissible material before core work is paid. Lowering crosses that Boundary only after the proposed input Fact
meaning and selected Fact declaration have been resolved; every applicable Invariant and movement obligation is then
satisfied before the ordinary host value is passed to the user implementation with established input Fact authority. A
refused passage places nothing in the core and does not invoke the implementation. The user implementation itself
remains ordinary Java or Kotlin code and may consume other established Facts only through explicit Fact participation
bindings under the machine-wide contracts fixed for that run. Internal functions and stages remain implementation rather
than nested operation pipelines. After the implementation returns its ordinary declared result, Kontrakt may form one
internal Change Proposal that binds the proposed result Fact, any additional Fact changes, and State movement to be
judged together. Invariant evaluates Fact Integrity, while State and Transition independently judge movement legality
over that same proposal. Only the whole accepted change is established, at which point the contractual Operation
completes successfully with its declared result as established Fact. Any refusal means Operation failure. Publication
begins only from that established result and then applies its finite applicability judgment and statically closed
source-to-target authority relations. The generated machine invokes the exactly bound Publication adapter through the
retained port, verifies target coverage, and assembles the selected Output Presentation. Backend encoding and emission
cost is paid only after an outward claim is authorized, the realization succeeds, and the presentation shape is closed.
A backend may devirtualize, inline, specialize, or erase the port only where equivalent behavior is proven; otherwise
the explicit call remains.