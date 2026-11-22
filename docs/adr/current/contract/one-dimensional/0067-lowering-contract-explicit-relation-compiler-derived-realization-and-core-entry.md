# ADR-0067: Lowering Contract, Explicit Source-to-Core-Fact Relation, Compiler-Derived Realization, and Core Entry Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `../../../../the-most-important-thing/what-contract-is.md`
- ADR-0070: Realization Axis, Core Realization Closure, and JVM-Ahead Optimization
- ADR-0069: Invariant Contract, Fact-Local Standing Integrity Law, Deterministic Judgment, and Establishment Gate
- ADR-0068: Fact Contract, Explicit Immutable Core Information, Sameness, and Uniqueness
- ADR-0066: Canonicalization Contract, Stable Representative, Canonical Bytes, and Explicit Omission
- ADR-0065: Admission Contract, Continuation Judgment
- ADR-0064: Input Contract, Explicit Boundary Presentation
- ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency
- ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary
- ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary
- ADR-0057: Failure Contract
- ADR-0050: State / Transition Contract
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend, Generated Host Interface, and Operation Realization Boundary

---

## Amendment

This ADR previously required every selected Lowering declaration to produce one retained generated realization port with
exactly one user-supplied implementation.

That model is removed.

Lowering is a declarative one-dimensional Contract expression. The user declares the exact source-to-target relation and
the Contract material required to make that relation complete. Kontrakt resolves and establishes that meaning, derives
the required Lowering realization knowledge, and the compiler/backend performs the actual value formation.

The user does not implement a Lowering mapper, adapter, converter port, callback, or generated Lowering interface.

The ordinary user-supplied realization boundary is Operation. The backend forms the complete candidate material for the
Core-owned Fact kinds explicitly targeted by the
established Lowering Contract. The selected Operation and its fact-parameter participation remain subject to their
separate IDL Binding and applicable Core judgments; Lowering does not select an Operation parameter as its target.

An internal generated helper, ABI, table, specialized function, or direct bytecode path may exist when useful. Such
material belongs to compiler/backend realization. It is not a retained user implementation requirement and it does not
acquire Contract authority.

The V1 relation remains one explicit source coordinate to one Fact coordinate **per mapping**. A source may be
reused by distinct explicit mappings, and one Definition may target several explicitly fixed Fact kinds, each with one
complete candidate. This amendment preserves source and target independence, prohibition on same-name or same-type
inference, prohibition on hidden cross-coordinate business computation, candidate-versus-Fact separation, independent
standing Invariant and movement judgments, and refusal attribution decided below.

---

## 1. Context

Lowering is the final representation boundary of the inbound airlock.

Input, Admission, and selected Canonicalization operate over the Contract-visible external presentation.

Admission may permit that presentation to continue. Canonicalization may establish a stable same-shape representative.

Neither ends external-presentation authority.

Lowering declares the complete permitted relation by which selected boundary coordinates may form candidate material for
one or more explicitly fixed Core-owned Fact kinds, independently of the parameter surface of the selecting Operation.

The declaration is Contract expression. It is not a request for the user to write representation-formation code.

Kontrakt resolves the Definition's nominal source requirements, exact target Fact kinds and coordinates, and qualified
formation meaning. The operation-local IDL Binding separately qualifies that Definition against the selected Input,
optional Canonicalization, enclosing Fact vocabulary, and Operation context. From the lawfully established meaning the
compiler derives the exact source reads, target writes, required representation refinements, candidate-completion
obligations, and the later judgment handoff.

The backend performs the physical value formation.

The generated machine realizes relation and candidate completeness, preserves the separate authorities for applicable
standing judgment, legal movement, and Fact establishment, and preserves refusal routing and the later lawful handoff
to the user Operation. These obligations do not grant Lowering their owning judgments.

---

## 2. Problem

Lowering can easily collapse into an arbitrary mapper.

If that happens, implementation code starts deciding which source values matter, which target coordinates participate,
how Facts are formed, and when the core may trust the result.

That would return Contract authority to user code.

A generated mapper interface does not solve the problem. It merely moves the hidden meaning into a callback selected at
assembly time.

The opposite extreme is also wrong. The compiler must not infer conversion authority from matching names, equal types, a
raw host-type pair, classpath discovery, backend support, or framework convention.

Lowering therefore needs one declarative Contract surface and one replaceable compiler/backend realization boundary.

The Contract surface owns the exact permitted relation and every semantic refinement required to make that relation
complete.

The compiler/backend realizes that established meaning over actual runtime values.

If the declared Contract does not contain enough meaning to determine a legal Lowering realization, the definition is
incomplete. Kontrakt must reject it or the Contract language must gain an explicit way to state the missing meaning.
User implementation must not silently fill the gap.

---

## 3. Decision Drivers

Lowering is not business computation.

Lowering is not an Invariant, State Transition, Publication rule, or result producer.

The Lowering declaration must contain immutable Contract material only.

Every target Fact coordinate formed by one Lowering occurrence must have one explicit mapping from one source
coordinate.

Equal names and equal host types do not create hidden relations.

One mapping must not fan out into several Contract-visible target coordinates in V1; distinct mappings may reuse the
same source coordinate.

Several source coordinates must not be combined within one mapping to form one target coordinate in V1.

Each explicitly fixed target Fact kind and its entire factual surface must be intelligible without retaining the
external Input declaration, source object graph, or selecting Operation parameter surface.

The user must not implement Lowering in Java or Kotlin merely because physical representation work is required.

The compiler may derive planning, completion checks, verification, diagnostics, caching, and optimization from the
established relation.

The backend may choose any physical formation strategy that preserves that established relation and its required
representation law.

The compiler must not invent missing Contract meaning from raw implementation convenience.

---

## 4. Decision

The Lowering authority path is:

```text
external presentation authority
-> explicit source-to-Core-Fact-coordinate mappings (one-to-one per mapping)
-> established Lowering Definition and operation-local IDL Binding
-> compiler-derived Lowering realization knowledge
-> backend candidate formation over the actual source values
-> complete candidate set for every explicitly targeted Fact kind
-> separately owned applicable Fact, standing Invariant, and movement judgments
-> lawful Fact establishment / Core entry
-> separate Operation Fact participation and ordinary user Operation invocation, when applicable
```

Lowering forms candidate material only after the declared relation has been realized over the actual source values.
External-presentation authority ends for Core entry only after the complete target Fact candidate set and every required
separately owned judgment succeed. No incomplete or partially successful candidate set obtains Fact authority.

Backend value formation does not own Fact meaning or establishment.

The user Operation does not perform Lowering.

---

## 5. Boundary Source and Target

The Lowering Contract owns the explicit formation relation between selected boundary coordinates and its fixed
Core-owned Fact targets. Operation Fact participation is not a Lowering mapping.

```text
interface scope:
    one closed `facts` vocabulary
    one standing `invariants` declaration

boundary side:
    selected Input presentation
    optional same-shape canonical representative

Lowering Contract:
    immutable explicit mappings to one or more fixed Core-owned Fact kinds

compiler/backend realization:
    compiler-derived LoweringPlan
    exact source reads and target writes
    permitted representation refinement
    complete candidate-set formation for all target Fact kinds
    completion and declared failure paths

judgment and core-entry side:
    applicable interface-level Invariant judgment
    applicable movement judgment
    Fact establishment under its owning law
    separately bound user Operation Fact parameter values, when required
```

The selected Input schema remains the source schema even when Canonicalization is present because Canonicalization does
not change the Contract-visible coordinates.

When Canonicalization is selected, its representative values become the source values.

When Canonicalization is absent, the admitted Input values continue unchanged.

Lowering does not introduce a separate `CoreMaterial`, `OperationStart`, `LoweringTarget`, or user-visible candidate
schema.

Each Lowering Definition explicitly fixes one or more exact Fact kinds declared through the enclosing interface
`facts` vocabulary and the exact factual coordinates it forms for each kind. These are Fact-owned targets, not
Operation-parameter addresses. A successful occurrence forms exactly one complete candidate of each fixed Fact kind.

The selected Operation still supplies the IDL Binding context and declares its Fact parameters independently.
Neither that parameter list nor the current Operation signature determines or silently changes the Lowering targets.

The Operation manifest does not repeat that vocabulary and does not gain a `fact` or `invariant` slot merely for
Lowering.

---

## 6. Target Fact Independence

The target Fact surface must not name, import, embed, extend, or retain a reference to the Input declaration, source
DTO, transport protocol, host object, Lowering declaration, framework context, adapter type, or source-coordinate
handle.

The target must be intelligible through its own Fact coordinates, sorts, presence, alternatives, relations, ordering,
bounds, schema, and version material.

The backend may physically read source values and materialize ordinary target values.

Source type names, getters, constructors, collection implementations, object identity, runtime references, generated
helpers, and backend machinery have no factual authority.

A generated host carrier may exist because a target Fact representation or the selected Operation ABI requires one.
That carrier is representation. It
is not Fact authority.

If a backend can lawfully eliminate an intermediate carrier while preserving the host-visible Operation call and every
Contract obligation, that elimination is a physical optimization only.

---

## 7. Lowering Definition and Operation Selection

The Lowering Contract contains no user implementation callback and no business algorithm.

The enclosing interface declares `facts` and `invariants`.

The Operation manifest selects a Lowering Definition by name from the definitions declared in the Lowering DSL. Each
relation in that definition uses `source -> target : method`. The selected Operation identifies the pipeline Binding,
not the owned address or identity of a target Fact.

```text
interface DepositContract {
    policy        DepositPolicy
    governance    DepositGovernance
    budget        DepositBudget
    capacity      DepositCapacity
    facts         DepositFacts
    invariants    DepositInvariants

    operation deposit(command: DepositCommand): DepositRecorded {
        manifest {
            flow:
                input             DepositInput
                admission         DepositAdmission
                canonicalization  DepositCanonicalization
                lowering          DepositLowering
        }

    }
}
```

The selected Lowering Definition is written separately in the Lowering DSL. A mapping method uses an explicit callable
application. In the following illustrative target notation, `DepositCommand` denotes a fixed Fact kind from the Core
Fact vocabulary, not the Operation parameter named `command`; the final Fact-qualified target spelling is still open.
For example, Java static methods can be referenced through an import:

```text
import java.lang.Long

lowering DepositLowering {
    accountIdText -> DepositCommand.accountId   : Long.parseLong(accountIdText)
    amountText    -> DepositCommand.amountMinor : Long.parseLong(amountText, 10)
}
```

A Kotlin extension function uses its source coordinate as the receiver. Here `QuantityFact` denotes the exact target
Fact kind, not an Operation parameter:

```text
import kotlin.text.toInt

lowering QuantityLowering {
    quantityText -> QuantityFact.quantity : quantityText.toInt(10)
}
```

Each application names its selected source coordinate exactly once, either as an explicit argument or as the receiver.
Other arguments must be literals or statically resolved constants whose values and meanings are complete before
Establishment. The DSL does not insert a source argument, infer an argument position, or use implicit default values.

The right-hand side is one callable application, not a general expression body. Nested calls, another runtime source
coordinate, runtime-computed arguments, open dispatch, and user callbacks are not permitted. Imports shorten names;
resolution still must select one exact callable. Ambiguous or unresolved applications are rejected. The Resolver,
Type Engine, and library semantic interpretation own that resolution and its meaning, not Lowering.

The relation body does not repeat Input carriers, Operation parameter declarations, Fact declarations, or Invariant
declarations. A single Definition may explicitly target several distinct Fact kinds, but does not acquire a relation
between those Facts merely by forming their candidates together.

It contains only the exact Contract material owned by Lowering.

Every source-to-target relation is explicit.

Equal spelling does not create a relation.

Different spelling does not prevent a relation.

If the selected callable's qualified meaning and the applicable Contract meaning do not determine the required
source-to-target formation, the declaration is incomplete. A user-written converter is not an implicit extension of the
Contract.

---

## 8. V1 Cardinality Law

The Contract-visible V1 relation is one explicit mapping at a time:

```text
one selected Input coordinate
-> one coordinate of one explicitly fixed Core Fact kind
```

One mapping selects exactly one source coordinate and forms exactly one target factual coordinate. The same selected
Input coordinate may be reused in separate, explicit mappings, including mappings into different target Fact kinds.
Source reuse does not make any single mapping a multi-target computation.

Each factual coordinate of each target Fact candidate must be formed exactly once. Duplicate formation of the same
Fact-kind coordinate within the occurrence is prohibited. Unused Input coordinates are permitted.

A single Lowering Definition may explicitly fix more than one distinct Fact kind. Each target kind must have one
complete candidate factual surface; the number and coverage of Operation parameters do not determine that surface.
The joint formation of several Fact candidates establishes no implicit relation between those Facts.

V1 still forbids one mapping from producing multiple factual coordinates and forbids combining multiple source
coordinates to form one target coordinate. Such cross-coordinate computation is not authorized merely by source reuse
or multiple Fact targets.

Physical storage may still be split, packed, flattened, or combined behind the same Contract relation. Physical layout
does not change Contract cardinality.

---

## 9. Type Relation and Compiler-Derived Realization

Source and target host types may be identical.

Equal types do not authorize implicit copying and do not remove the explicit Lowering relation.

Source and target host types may also differ.

A raw type difference does not ask the user to provide an implementation.

The declared relation identifies the source and target. Their sorts need not be directly structurally compatible when
a qualified conversion can form the target from the source.

The Resolver selects the exact callable. The Type Engine judges its argument and result type applicability. Library
semantic interpretation supplies the qualified conversion meaning under the applicable semantic basis. Together, those
subsystems determine whether the conversion is valid for the declared source and target meanings and applicable
Contract-owned representation law. Lowering consumes the completed judgment to establish the explicit coordinate
relation. It does not rejudge the conversion.

Supported methods may use qualified Java/Kotlin standard-library operations or Kontrakt-supported formation-library
operations, including parsing and decoding. An open or dynamically selected callable cannot complete Lowering meaning.
The same applies when any required callable dependency remains unresolved or its behavior cannot be interpreted.

The compiler does not select meaning from the raw host-type pair.

A conversion catalog does not provide Contract authority merely because it contains a compatible function.

A library does not select itself.

A naming convention does not provide authority.

Classpath contents do not provide authority.

Backend support does not create missing Contract meaning.

When the established Lowering Contract and qualified callable interpretation completely determine one permitted
representation refinement, the compiler may lower that meaning to any supported backend operation, library primitive,
generated helper, table, parser, copier, constructor path, or direct machine representation that preserves it.

When that meaning remains incomplete, compilation must fail rather than requesting a user mapper.

The backend realization must not perform repository lookup, environmental resolution, business computation, Invariant
judgment, State movement, Publication, or undeclared capability access as part of Lowering. A selecting Operation
parameter is neither a target source of authority nor an implicit conversion rule.

---

## 10. Representation Refinement, Not Core Computation

Lowering changes representation and authority domain without deriving new cross-coordinate business meaning.

Each selected mapping method determines how the source's declared meaning is formed in its target representation.

At the Contract-visible boundary, V1 forms each coordinate of each explicitly fixed target Fact candidate through
exactly one explicit mapping from one Input coordinate. That Input coordinate may be reused in other explicit mappings.

Permitted one-to-one refinement families include:

```text
declared external scalar presentation
    -> immutable Fact scalar of the same declared meaning

nullable or optional presentation
    -> one explicit optional Fact coordinate

closed external alternative
    -> one explicit finite-alternative Fact coordinate

approved bounded opaque leaf presentation
    -> one immutable Fact coordinate under the same declared leaf meaning

direct selected Input coordinate
    -> one explicitly addressed Core-owned Fact coordinate

declared external identifier or reference presentation
    -> one Fact representation of that same declared identifier or reference meaning
```

Compiler/backend realization may decode, parse, range-check, copy, freeze an approved bounded leaf, make an
already-declared presence distinction explicit, or select a fixed internal representation only when that exact
refinement is already determined by the established Contract meaning.

A generic text coordinate does not become a date, account identity, money, or another business concept merely because a
backend library can parse it.

The following cardinalities are prohibited in V1:

```text
one mapping -> multiple Contract-visible Fact coordinates
multiple source coordinates within one mapping -> one Contract-visible Fact coordinate
```

The prohibition is semantic, not physical. A backend may decompose one Fact coordinate into several machine words or
pack several coordinates into one region. Those choices remain backend layout. Reusing the same source coordinate
through separate, lawful one-to-one mappings is permitted; it does not make one mapping a `1:N` relation.

Lowering must not create a new business proposition, consult mutable current machine state, execute core computation,
produce Operation Result Material, decide whether movement is legal, evaluate an Invariant, select a Transition, grant
Publication authority, apply business Policy, infer missing material, combine coordinates within one mapping, or split
one mapping into
several Contract-visible meanings. Distinct explicit mappings may lawfully reuse a source coordinate.

The following are not Lowering:

```text
birth date -> current age
price + customer grade -> discounted price
score -> risk category
year + month + day -> business date coordinate
name text -> database lookup -> current company identity
timestamp -> separate business-visible seconds and nanos coordinates
current balance - withdrawal amount -> new balance
Fact set -> Result
Result -> outward presentation
```

The distinction is:

```text
Lowering Contract:
    each explicit mapping: one boundary coordinate -> one fixed Core Fact coordinate of the same declared meaning

Lowering realization:
    compiler/backend formation of the actual target value under the established Lowering Contract

core realization:
    user Operation computation over established Facts and other lawfully available immutable material

Invariant:
    judgment over one complete candidate Fact under the standing interface-level law for its exact kind

State and Transition:
    legal movement authority under their own Contract law

Publication:
    permitted outward exposure authority
```

A conforming Lowering realization must be deterministic over its declared source domain and form the complete set of
all declared target Fact candidates or refuse the Lowering occurrence under its declared law. Individual mapping
refusals retain their exact attribution under the owning failure law.

The generated machine supplies only the source coordinates selected by the established relation.

Budget, Capacity, Version, Governance, and other cross-cutting judgments remain under their own authority and
attribution.

A declared reference may cross Lowering only as one explicit source coordinate refined into one explicit target
coordinate of the same declared reference meaning. Resolution against another coordinate, a mutable registry, or current
core state belongs elsewhere and must not be disguised as formation.

---

## 11. Definition-Time Completeness and Identity

A Lowering Definition Candidate owns the complete resolved meaning of its explicit one-to-one-per-mapping formation
relations for every fixed target Fact kind.
Its Definition determinants are:

```text
Source Requirement: each declared nominal source coordinate and its required Input-visible presentation meaning
Target Requirement: each exact, explicitly fixed Core-owned Fact kind and its factual coordinates, complete Fact
    surface, applicable Fact meaning, version, and required representation meaning
Mapping Relation: the complete declared source-to-Fact-coordinate relations for all fixed target Fact kinds,
    preserving every mapping occurrence, source reuse, target uniqueness, and multiplicity
Formation Meaning: the exact qualified callable, explicit source receiver or argument placement, meaning-determining
    arguments, completed conversion meaning and applicability, and Lowering-owned partiality and refusal meaning
Semantic Dependencies: the exact target Fact meaning/version and Contract-owned refinement, bounds, and semantic
    Law or Basis references that determine the declared formation meaning
```

These determinants must be resolved before the Candidate becomes Visible HIR. Establishment cannot reconstruct missing
Definition meaning from the IDL Binding Candidate, a current Operation, source syntax, or backend behavior. Resolved
mapping occurrences retain their full multiplicity even when a complete declaration violates a Lowering
law. Frontend resolution must not silently repair or discard mappings; unresolved meaning cannot enter Visible HIR.

A nominal source requirement does not incorporate the current selected Input Definition. The target requirement,
however, fixes each exact Core Fact kind and its required factual coordinates independently of the current Operation
Parameter Reference. Those exact Fact meanings and their applicable versions remain Definition dependencies. The
current selected Input and Operation must satisfy their own Binding and application requirements; selection cannot
give one Definition an unrecorded alternate Fact target, callable, or formation meaning.

Lowering consumes the lawful boundary Presentation supplied for its actual application. When Canonicalization is
selected, selected coordinates use its final representatives and unselected coordinates retain Input-established
values. Lowering does not select Canonicalization, reconstruct that Presentation, or judge an upstream refusal or
continuation outcome. The upstream source and entry laws do not become Definition determinants by being prerequisites
for use.

For a selected Operation, the Definition and its separate operation-local Binding, together with the applicable
separately owned judgments, must close at least:

```text
the flow-selected Input schema
the selected Canonicalization law, when present
the enclosing interface's Fact vocabulary and standing Invariants
the selected Operation signature for its own IDL Binding and application obligations, not as a Mapping Target
every explicitly fixed target Core Fact kind and its complete factual surface
every explicit Input-coordinate to fixed Fact-kind-coordinate mapping
one source coordinate and one Fact-owned target address for every mapping
explicit mapping even when source and target names or host types are equal
completed conversion applicability for the source and target, as judged by the Type Engine and library semantic interpretation
the selected mapping method's exact resolved callable and qualified formation meaning under the applicable semantic basis
the explicit source argument or receiver and every meaning-determining constant argument
the Contract-owned representation refinement required by each non-identical representation
complete factual coverage and unique formation for every fixed target Fact candidate
explicit source-coordinate reuse only through distinct, independently lawful mappings
finite depth, cardinality, intermediate storage, output, and work bounds
applicable schema, Version, and Governance material
Lowering-owned refusal meaning and separately owned cross-contract stop attribution
```

Every factual coordinate of every fixed target Fact candidate must be formed exactly once under the complete flat
Lowering Definition. Each target Fact kind must have one complete candidate; no target may be left partial.

An Input coordinate may appear in several distinct, explicit mappings. An Input coordinate may also remain unused.
Neither case implicitly creates a relation or requires an Operation parameter Fact surface to reproduce external
material.

Silent target defaults, fallback constructors, same-name auto-mapping, same-type auto-copying, catalog selection,
structural guessing, package scanning, annotations, assignability, inheritance, and discovery from implementation shape
are prohibited.

The frontend must reject missing or duplicate formation of a target Fact coordinate, unknown source coordinates or
unknown/falsely attributed target Fact kinds or coordinates, a single mapping with multiple targets, a target coordinate
requiring combined runtime source coordinates, incomplete or invalid qualified conversion judgments, hidden absence,
recursive host-object traversal, unbounded collection work, environment-dependent Contract meaning, executable user
material inside the relation body, a fixed target Fact kind absent from the enclosing interface's Fact vocabulary, or
a representation refinement whose meaning is not closed by the declared Contract and qualified callable interpretation.
Repeated source coordinates in different explicit mappings are not a rejection reason.

There is no missing-Lowering-implementation link failure because the user does not supply a Lowering implementation.

A backend that cannot realize a semantically complete Lowering Contract with the required guarantees must reject that
backend realization. Backend inability does not change the Contract.

The established Lowering Definition is represented as one flat, immutable, adapter-erased Lowering Contract material.
Its complete Definition meaning preserves the nominal source requirements, the exact fixed target Fact kinds and
complete target coordinate requirements, the complete declared mapping relations and their multiplicities, the qualified
callable formation meaning and argument application, and all meaning-determining refinement, bounds, refusal, and
semantic dependencies. One independently declared Lowering Contract denotes one Lowering Authority;
that Authority and one exact Contract Version identify one complete Definition under ADR-0063 and ADR-0053. Conflicting
resolved meanings cannot be established for the same Authority and Version. Equal meanings under different Authorities
or Versions do not merge their identities. Import spelling and source location do not change Definition meaning.

Selection by an Operation is a separate Binding. It preserves the exact Lowering Definition Reference and the selected
Operation Reference. The selected Input, optional Canonicalization, selecting Operation signature, and applicable
flow-world basis must
be resolved for that Binding before use. Its fact-parameter participation obligations are separate from the
Definition's fixed target Fact kinds and target coverage. Those contextual resolutions do not become Definition
determinants merely because an Operation selects the Definition. If an exact basis determines the
Definition's
own meaning, ADR-0063 still requires that dependency to remain with the Definition. CRI may prepare exact compiler-time
source and target correspondences from legal producer projections; it does not establish Binding validity, invent a
mapping, or form runtime source values.

The Binding must not change the Definition's declared relation or qualified callable meaning. A mismatch is rejected,
not repaired through inference. The Definition's identity and the Binding's identity remain distinct even when one
Operation is the only current user of that Definition.

Backend helper identity, generated class identity, host object identity, allocation strategy, and machine-code shape are
not part of Lowering Contract meaning.

From ratified Lowering material, the compiler derives one immutable semantic `LoweringPlan` or equivalent realization
knowledge.

That derived material closes the exact source read set, target Fact-coordinate write set, required representation
operations, completion checks for every fixed candidate Fact and their whole set, declared refusal branches, lawful
Invariant and movement handoff, establishment requirements, cache dependencies, and backend layout requirements
legitimately implied by the Contract.

Derived material does not become another authored Lowering Contract.

---

## 12. Compiler and Backend Realization Boundary

A selected Lowering declaration creates no user implementation interface.

The user does not implement `DepositLowering`, `PlaceOrderLowering`, a generated mapper, or another Lowering callback.

Kontrakt resolves the selected Lowering Contract and derives the executable obligations required to form the
complete candidate set for its fixed Core Fact targets. Operation parameters are supplied separately under their
applicable Fact participation and realization obligations.

The backend realizes those obligations directly.

A backend may use an internal generated function, method, table, parser, constructor path, value builder, direct field
or array access, primitive slab, stack local, register value, or another internal representation.

Those structures are replaceable implementation.

They are not retained user-facing Contract surfaces.

They do not select Lowering meaning.

They do not own Fact authority.

They do not require runtime classpath scanning, reflection, service discovery, coordinate-name search, type-pair lookup,
or DI resolution to select a Lowering implementation.

The actual user Operation implementation is bound separately under ADR-0046 and admitted as User-System Realization
under ADR-0070.

The selected pipeline may invoke that Operation only after the required candidate and independently owned
establishment judgments succeed and the Operation's Fact parameter requirements are lawfully satisfied.

The exact internal method decomposition, helper ABI, carrier materialization strategy, refusal encoding, and JVM
lowering remain compiler/backend decisions.

Any chosen realization must preserve the explicit relation and absence of hidden implementation authority.

---

## 13. Compiler-Derived Material

From the established Lowering Definition and its resolved Operation Binding, including the qualified callable meaning
supplied by the frontend, the compiler may derive:

```text
semantic LoweringPlan
source-read closure
target-Fact-coordinate-write closure
representation-refinement operations
complete candidate-set formation plan for each fixed Fact kind
judgment and establishment handoff
verification obligations
automatic tests
diagnostic mapping
cache dependencies
optimization opportunities
backend formation material
```

These are derived compiler material.

They do not become another authored Lowering Contract.

The compiler may choose how to execute an already-established refinement.

It may not create a refinement whose meaning is absent from the Contract.

---

## 14. Judgment and Establishment Boundary

Backend value formation does not establish Fact authority merely by producing target-shaped values.

The generated machine verifies the declared mapping coverage, each exact target Fact surface, and completion of
the entire declared candidate set. A Lowering occurrence forms its target candidates as one all-or-nothing set: failure
of any required mapping or target formation prevents successful completion of that occurrence and no partial candidate
set is passed as a successful result.

The applicable Fact, standing Invariant, and movement authorities then perform their independently owned judgments.
Only after every required judgment succeeds may the corresponding Fact authority be established and the separate
Operation Fact participation obligations be satisfied for a lawful invocation, when applicable. The exact atomic
establishment, rollback, and publication mechanism remains governed outside this ADR.

Successful value formation therefore does not equal successful Core entry or Operation handoff.

Candidate formation is not Fact establishment.

The generated pipeline must preserve at least these obligations:

```text
every coordinate of every fixed target Fact kind is formed exactly once
every formed candidate conforms to its fixed Fact kind and entire declared factual surface
every target coordinate is justified by one explicit mapping from one Input coordinate
repeated source coordinates are permitted only through separately declared lawful mappings
all declared target Fact candidates form one complete occurrence result or the occurrence is refused
every required representation refinement follows the established Lowering law
the material is immutable and no mutable external alias survives into Fact authority
presence, alternatives, relations, and ordering are explicit where the Fact surface declares them
applicable schema, Version, Governance, Budget, and Capacity judgments succeed under their own authority
every interface-level Invariant applicable to the candidate Fact kind holds
every applicable movement judgment succeeds
no back-reference to the external presentation or backend realization machinery remains
```

Only after those obligations succeed may the machine establish the corresponding Fact authority and, if the selected
Operation requires them, provide lawfully participating established Fact values through its separately declared
Operation realization parameters. Lowering targets need not coincide with every Operation Fact parameter.

Object allocation, constructor completion, builder completion, parsing completion, or backend materialization alone does
not create Fact authority.

The backend may use temporary mutable builders, scratch buffers, ordinary libraries, offset tables, sorting workspaces,
or staged regions when those choices preserve the established Lowering law.

Such objects belong exclusively to implementation and must never be exposed as established Facts.

---

## 15. Refusal Boundary

Lowering refuses when the established Lowering law cannot form the complete candidate set for every explicitly
fixed target Fact kind from the actual admitted source values. This is distinct from whether the selecting Operation
has separately satisfied all of its Fact parameter requirements.

A runtime refusal is distinct from a definition that failed to state enough Lowering meaning.

An incomplete Contract is rejected before executable realization.

A semantically complete Contract may still define an actual-value refusal condition, such as a declared representation
that cannot be formed for the presented value under the exact Lowering law.

One mapping refusal does not suppress another mapping that remains independently reachable. All such mappings must
complete their required judgments, regardless of discovery or execution order. If any required mapping or target Fact
candidate formation refuses, the declared complete candidate set does not exist and the whole Lowering occurrence is
refused. No successfully formed subset is published or established as a partial Lowering success.

The refusal preserves the exact attribution of each failed mapping. These attributions do not automatically establish
separate Failures. ADR-0057 governs Failure cardinality according to the exact authoritative unsuccessful judgments;
no first-failure priority or aggregate Failure identity is introduced here.

A backend or library exception is not itself a Lowering refusal. Only a failure defined by the qualified mapping-method
meaning may be attributed to Lowering. Failure of required realization and Crash retain their separate meanings under
ADR-0057.

Lowering refusal does not mean Input refusal, Admission rejection, Canonicalization refusal, Invariant refusal, or a
Budget or Capacity result.

A target that is incomplete does not become partially established Fact material. Formation success for some target
Fact candidates does not authorize the establishment or publication of that subset under a failed occurrence.
Subsequent Fact, Invariant, or movement refusals retain their own authority and attribution, not Lowering refusal.

Lowering refusal, user Operation failure, Invariant refusal, and Publication refusal are distinct:

```text
Lowering refusal:
    the full declared set of complete target Fact candidates cannot be formed under the Lowering law

user Operation failure:
    established input Facts exist,
    but the admitted user Operation realization does not complete with its declared result

Invariant refusal:
    complete candidate Fact material exists,
    but a standing law for that exact Fact kind does not hold

Publication refusal:
    the Operation result Fact exists,
    but the declared outward exposure is not permitted
```

The machine must preserve exact Failure and diagnostic attribution to the owning authority.

---

## 16. V1 Optimization Boundary

Kontrakt owns the Lowering Contract, derived `LoweringPlan`, exact source and target relation, completion checks,
generated judgment handoff, verification, diagnostics, test derivation, cache dependencies, and backend realization
requirements.

There is no user Lowering implementation whose call must be preserved.

The backend may therefore optimize the generated formation path directly when it preserves the established Contract.

Possible realizations include:

```text
Canonicalization-Lowering orchestration fusion
intermediate presentation elimination
dead unused source-coordinate elimination
direct source access and direct target writes
exact buffer and region sizing
single-allocation or bounded-allocation formation
primitive and finite-alternative specialization
packed relation and presence layouts
structural cache keys from the exact declared read set
specialized parsing or decoding for an established representation law
carrier construction only at the host ABI boundary
direct binding of lawfully established and participating Fact values at the generated Operation ABI boundary
AOT-generated completion, judgment, establishment, and invocation paths
```

If the generated host Operation ABI requires a carrier instance, the backend materializes that actual value before the
user Operation call.

If an intermediate carrier is not host-observable and can be removed without changing the Operation ABI or Contract
meaning, the backend may erase it.

Optimization does not permit same-name mapping, raw type-pair conversion inference, undeclared coordinate synthesis, or
business computation inside Lowering.

Lowering authority does not extend into the user core realization.

ADR-0070 separately governs analysis and optimization of the admitted user Operation realization after the legal
Operation handoff.

---

## 17. Handoff to the User Operation

Successful Lowering candidate formation and lawful Operation handoff are distinct:

```text
Lowering occurrence completion:
    every declared mapping realized over the actual source values
    + one complete candidate per fixed target Fact kind
    + entire candidate set complete (all-or-nothing)

Subsequent Core entry and Operation handoff, when applicable:
    required separately owned Fact, standing Invariant, and movement judgments succeeded
    + lawful establishment of the complete applicable candidate set
    + external-presentation authority ended
    + separately satisfied Operation Fact participation and parameter requirements
    = legal Operation handoff
```

The compiler/backend forms the actual target Fact candidate representations and, separately, the ordinary host
values required by the Operation parameters from lawfully participating established Facts. An Operation Fact parameter
need not have been a target of the selected Lowering Definition.

For an object-shaped generated Fact carrier, this means the backend supplies an actual value of that generated type. The
user does not construct that input value merely to perform Lowering.

Conceptually:

```text
actual Input presentation value
    -> established Lowering relation
    -> complete target Fact candidate set
    -> independently owned candidate judgments
    -> lawful Fact establishment
    -> separately bound Operation Fact participation
    -> user Operation receives its declared host parameter values
```

The Operation does not receive the Input presentation, canonical presentation object, Lowering declaration,
source-to-target relation table, `LoweringPlan`, backend helper metadata, candidate wrapper, established-Fact wrapper,
staging object, or host-language execution context.

```text
external presentation authority:
    ended

target Fact candidate set:
    complete under the declared relation before subsequent judgments

standing judgment:
    every applicable Invariant and movement obligation succeeded

Fact authority:
    established outside the host value representation by its owning law

user Operation invocation:
    ordinary generated or declared host parameter values, with separate lawful Fact participation

Result and Publication:
    not yet established or authorized
```

The Operation may produce its ordinary declared result candidate.

Result-side Fact establishment, result-side movement, Publication, and Output Presentation belong to later authorities.

The core must not reopen the erased Input or Lowering declaration as sources of factual meaning.

---

## 18. Open in This Section

The exact compiler-internal Lowering representation is open.

The exact backend helper decomposition and JVM ABI are open.

The exact carrier materialization and allocation strategy are open.

The exact encoding of declared Lowering refusal is open.

The Lowering DSL expresses each binding as `source -> target : method`.

Mapping-method references and arguments use the explicit callable applications defined in Section 7. The supported
mapping-method inventory and any additional authoring needed for nontrivial one-to-one representation refinement remain
open. User implementation callbacks remain prohibited.

Those decisions must not weaken the fixed Lowering law: one immutable Lowering Definition selected through an
operation-local Binding, one-to-one formation per explicit mapping with permitted source reuse, one or more explicitly
fixed Core Fact targets with complete candidate surfaces, all-or-nothing candidate-set completion, no runtime
implementation discovery, no user-supplied Lowering implementation, no implicit conversion authority, and independent
judgment and establishment handoff.

The exact Fact-qualified target spelling and the HIR Candidate, Seal, Established Definition, Binding/Occurrence,
and Established Semantic Protocol refinements remain separate follow-up work. This amendment does not establish those
protocol details.

The exact atomic Fact establishment, rollback, and publication mechanics and the complete authoring surface for
additional Fact participation, Operation Result Material, result-side change formation, Publication, and Output
Presentation remain outside this ADR.

---

## 19. Consequences

Lowering becomes an explicit representation-boundary Contract instead of a mapper callback.

External and core vocabularies may remain independent because every Contract-visible relation is authored directly.
Each Lowering Definition fixes its Core Fact targets rather than deriving them from Operation parameters; the selected
Operation remains the center of the IDL pipeline Binding.

Equal host types do not create hidden copying authority.

Different host types do not force the user to supply a converter.

The Contract must instead state enough meaning for Kontrakt to determine the permitted representation refinement.

The compiler gains a closed relation from which it can derive verification, diagnostics, tests, caching,
candidate-completion checks, and backend formation.

The backend gains freedom to realize that established relation without exposing a user implementation port.

The user implements the Operation business algorithm rather than reimplementing Lowering.

The cost moves into Contract expressiveness and compiler/backend responsibility. Kontrakt must reject incomplete
Lowering meaning, support the declared representation law on the selected backend, and preserve exact refusal and
establishment boundaries.

V1 deliberately rejects a single `1:N` mapping, multi-source `N:1` coordinate derivation, same-name inference,
raw type-pair converter selection, and hidden callbacks because those mechanisms would obscure Contract meaning.
Distinct explicit mappings may reuse a source coordinate, and a Definition may target several complete Fact candidates.

---