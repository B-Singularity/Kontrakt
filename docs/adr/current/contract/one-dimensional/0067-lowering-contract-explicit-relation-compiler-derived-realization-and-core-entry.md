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

Each Lowering Definition explicitly fixes one or more exact Core-owned Fact kinds and the exact factual
coordinates it forms for each kind. Fact Definition meaning is independently determined in the Core, not produced by
Input, Lowering, Output, or Publication compilation. These are Fact-owned targets, not Operation-parameter addresses.
The enclosing interface's `facts` vocabulary is a separate use-qualification relation; it neither creates nor
reselects the Lowering Definition's target. A successful occurrence forms exactly one complete candidate of each
fixed Fact kind.

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
The declaration order of independent mappings is not Lowering Definition meaning. Their exact declared multiplicity
and source-to-target/formation relations remain meaning, including repeated identical mappings. Source provenance
and physical mapping index do not define Mapping identity or equality. An order explicitly belonging to a Fact
coordinate, callable argument application, or another separately owned law remains governed by that law.

A nominal source requirement does not incorporate the current selected Input Definition. The target requirement,
however, fixes each exact Core Fact kind and its required factual coordinates independently of the current Operation
Parameter Reference. Those exact Fact meanings and their applicable versions remain Definition dependencies. The
current selected Input and Operation must satisfy their own Binding and application requirements; selection cannot
give one Definition an unrecorded alternate Fact target, callable, or formation meaning.

Lowering consumes the lawful boundary Presentation supplied for its actual application. When Canonicalization is
selected, selected coordinates use its final representatives and unselected coordinates retain Input-established
values. Lowering does not select Canonicalization, reconstruct that Presentation, or judge an upstream refusal or
continuation outcome. The current upstream sources and entry results do not become Definition determinants by being
prerequisites
for use. The reusable Occurrence Basis Requirement Law in Section 14.1 belongs to Lowering Definition meaning.

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

A Lowering Definition with missing or duplicate formation of a target Fact coordinate, a single mapping with
multiple targets, a target coordinate requiring combined runtime source coordinates, hidden absence, recursive
host-object traversal, unbounded collection work, environment-dependent Contract meaning, executable user material
inside the relation body, or a representation refinement whose meaning is not closed by the declared Contract and
qualified callable interpretation cannot be established as a lawful Lowering Definition. Fully resolved duplicate
mappings remain visible for the owning Lowering judgment rather than being repaired or discarded by the frontend.
Unresolved authored nominal Source Requirement coordinates, unresolved or falsely attributed target Fact kinds or
coordinates, and incomplete or
unsuccessful qualified conversion resolution cannot supply complete Visible HIR meaning. Exact callable resolution,
source/target sort applicability, and qualified Java/Kotlin library interpretation remain the responsibility of their
owning compiler subsystems; Lowering consumes their completed meaning without rejudging it. A fixed target Fact kind
absent from the current enclosing interface's Fact vocabulary is a separate Binding/Composition compatibility defect,
not a missing or alternate Lowering Definition target. Repeated source coordinates in different explicit mappings are
not a rejection reason. The HIR Seal and owning Definition refusal boundary is specified in Section 11.5. Fully resolved
target
multiplicity and coverage defects remain violations of the Lowering Definition law, not missing HIR meaning.

There is no missing-Lowering-implementation link failure because the user does not supply a Lowering implementation.

A backend that cannot realize a semantically complete Lowering Contract with the required guarantees must reject that
backend realization. Backend inability does not change the Contract.

The established Lowering Definition is represented as one flat, immutable, adapter-erased Lowering Contract material.
Its complete Definition meaning preserves the nominal source requirements, the exact fixed target Fact kinds and
complete target coordinate requirements, the complete declared mapping relations and their multiplicities, the qualified
callable formation meaning and argument application, and all meaning-determining refinement, bounds, refusal, and
semantic dependencies. It also preserves the complete Occurrence Basis Requirement Law in Section 14.1.
One independently declared Lowering Contract denotes one Lowering Authority;
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

### 11.1. Resolved Lowering HIR Candidate References and Core Fact Dependency

Lowering specializes the ADR-0071 Typed HIR Semantic Reference Domain with two Lowering-owned primary Candidate
reference kinds when their exact subjects must be observed:

```text
LoweringDefinitionCandidateRef
LoweringIDLBindingCandidateRef
```

`LoweringDefinitionCandidateRef` identifies one exact, current, resolved Lowering Definition Candidate under its
owning Lowering Authority and authority-scoped resolved Version candidate coordinate. The independently declared
Definition and its applicable Version determine the candidate's nominal identity scope. A future need to distinguish
several independent Definitions under one Authority and Version must follow the owning authority-local coordinate law
of ADR-0063; source order, a host symbol, an IDL selection, or a physical index cannot supply that distinction.
This pre-authority Candidate Reference is not an Established Definition Reference.

`LoweringIDLBindingCandidateRef` identifies one exact operation-local IDL selection occurrence. Its selecting
Contract Interface/Interaction/Operation context, Lowering role coordinate, exact target
`LoweringDefinitionCandidateRef`, and any independently required Binding qualifier are observed through the
existing IDL/HIR reference laws. Two distinct Operation selections may target the same Definition Candidate without
merging their Binding Candidates or changing the Definition's Fact targets. Selection observation at the owning role
coordinate distinguishes an exact selected Binding Candidate from Explicit Absence under ADR-0071. An unresolved
selection is not Explicit Absence, and absence does not require a fabricated Lowering Reference.

A nominal Source Requirement belongs to the Lowering Definition Candidate, not to the currently selected Input
Definition. Every required Source Coordinate must be exactly selectable within the owning Definition's resolved
meaning, but no separate primary `SourceRequirementRef` is mandated. Every explicit Mapping occurrence must likewise
remain exactly observable with its multiplicity, without requiring a new primary Mapping Reference or an independent
Mapping Authority. Source position, declaration order, array slot, generated helper, and generation-local handle are
not semantic selection or identity laws. Section 11.3 fixes target-qualified Mapping Meaning selection without
mandating a physical selector representation.

A fixed Target Requirement consumes the exact pre-authority meaning of a separately Core-compiled Fact Kind and its
Fact-owned factual coordinates. The target reference is owned by the Fact HIR Protocol, not minted or reinterpreted
by Lowering. The Fact Protocol may identify an exact Fact Kind directly or through an exact Fact Definition Candidate
and its Fact-owned nominal Kind coordinate; Lowering does not mandate either representation, name, or encoding.
The exact Fact Kind and its coordinates are not inferred from a current Operation parameter, the Input surface,
interface vocabulary membership, a host class name, or a physical address. The full Fact-owned surface must be
legally observable where required for complete target coverage, without recursively embedding or copying its
separately owned Definition into Lowering HIR.

Fact Definition compilation and its Contract authority are Core-owned and independent of inbound, output, and
publication contracts. The Fact-to-Lowering reference is a semantic dependency, not a mandatory serial compiler-pass
schedule. A Fact HIR Candidate reference does not establish that Fact Definition; an Established Fact Definition
Reference and a runtime Established Fact are distinct from it and from each other. Any required Established Fact
Definition Basis is governed by ADR-0063 and the later Lowering Establishment judgment, not granted by HIR lookup.

The current Input Coordinate is observed through Input-owned `ResolvedInputCoordinateRef` where needed by a lawful
Binding or Composition judgment; it is not the Lowering Definition's nominal Source Requirement. Exact qualified
callable and library meaning is observed from the responsible resolver, Type Engine, and qualified semantic
interpretation as already required in Sections 7, 9, and 11. Their internal reference forms, resolution algorithms,
and library interpretation authority are not redefined by Lowering.

HIR Candidate Reference equality, complete Definition Meaning equality, authoritative Definition identity, IDL
Binding equality, and physical reuse eligibility remain distinct. Candidate References are exact in their current
reference domain and cannot be inherited from an earlier HIR generation. A dense handle, HID, fingerprint, Query Key,
serialized position, or reuse artifact does not confer semantic Reference identity or Contract authority.

### 11.2. Resolved Lowering Definition Candidate Projection

The Lowering Definition Candidate Projection is the complete, read-only, producer-owned observation of one exact
`LoweringDefinitionCandidateRef` and all five Definition determinants in this Section. It also exposes the complete
Occurrence Basis Requirement Law in Section 14.1. Its complete observation must retain, without loss or implicit
reconstruction:

```text
Source Requirement
    all nominal source requirements and each required Input-visible presentation meaning
Target Requirement
    every explicitly fixed Core Fact Kind, its exact Fact-owned factual coordinates,
    complete target-surface dependency, meaning/version, and required representation meaning
Mapping Relation
    every declared source-to-Fact-coordinate mapping occurrence, source reuse,
    target-formation multiplicity, and the complete declared relation for every target kind
Formation Meaning
    the completed, exact qualified callable application and meaning-determining arguments,
    conversion applicability and Lowering-owned partiality/refusal meaning
Semantic Dependencies
    exact direct Fact and other Contract-owned Law/Basis references and the refinements,
    bounds, versions, and other dependencies that determine this Lowering Definition
```

The following example illustrates semantic observations, not a physical HIR layout. It uses the
`DepositLowering` declaration in Section 7. For this example, the complete Fact-owned
`DepositCommand` surface has exactly `accountId` and `amountMinor`. All exact external meanings
are observed through their owning producer Protocols.

```text
LoweringDefinitionCandidateRef
LoweringIDLBindingCandidateRef

Lowering Definition Candidate Projection
    Definition: DepositLowering
    Version: exact resolved Version candidate
    Source Requirement: accountIdText -> exact Input-visible presentation meaning
    Source Requirement: amountText -> exact Input-visible presentation meaning
    Target Requirement: DepositCommand -> exact Fact-owned HIR Candidate meaning
    Complete Target Surface: DepositCommand.accountId, DepositCommand.amountMinor
    Mapping: accountIdText -> DepositCommand.accountId
    Mapping: amountText -> DepositCommand.amountMinor
    Formation: accountId <- qualified Long.parseLong(accountIdText)
    Formation: amountMinor <- qualified Long.parseLong(amountText, 10)
    Semantic Dependencies: exact meaning-determining producer references
    Occurrence Basis Requirement Law as defined in Section 14.1

Lowering Mapping Surface Projection
    DepositCommand.accountId -> accountIdText (1 occurrence)
    DepositCommand.amountMinor -> amountText (1 occurrence)

Lowering Selected Mapping Meaning Projection
    Target: DepositCommand.amountMinor
    Source: amountText
    Callable: exact java.lang.Long.parseLong(String, int)
    Receiver: none (static method)
    Argument 0: amountText
    Argument 1: constant 10
    Conversion: exact qualified formation meaning and applicability
    Partiality / Refusal: exact applicable Lowering-owned meaning
    Semantic Dependencies: exact meaning-determining producer references
```

The named Source Requirements arise from the declared mappings; no separate `source` declaration is
introduced. The Projection does not confer Establishment authority or promote any Fact Candidate Reference.

The Projection expresses Lowering-owned meaning and exact direct references to separately owned semantic subjects.
It does not assume ownership of the referenced Fact, Input, Type Engine, or library meaning. A legal direct reference
and producer-owned projection may expose the required complete meaning without a redundant copy or recursive
expansion. No current Operation selection, current Input or Canonicalization occurrence, runtime value, or Basis
Binding enters Definition meaning merely because it is needed by a later application. The reusable Occurrence Basis
Requirement Law remains part of the Definition observation. Its instantiated
requirements and exact Basis Bindings belong to the application under Section 14.1.

Every declared Mapping occurrence is preserved even when an exactly resolved duplicate makes the Definition unlawful.
Independent Mapping declaration order is not semantic order; Mapping multiplicity and the source, target, and
qualified Formation relation are semantic. The Projection must not silently deduplicate, overwrite, default, or
normalize away an invalid but fully resolved mapping. It neither performs the later Lowering Definition Judgment nor
changes the separately owned resolution and Fact laws.

The complete Definition Projection determines comparison of the entire Definition meaning under the owning Lowering
law, including the full Fact Target requirements, Mapping relation, qualified Formation meaning, and exact semantic
dependencies. Source-only import spelling, provenance, physical layout, compiler partition, and generated backend
helpers do not determine that equality. Equality of Definition meaning does not merge distinct Candidate References,
Authorities, Versions, Established Definitions, or IDL Binding occurrences.

Fine-grained Mapping Surface and selected Mapping Meaning observations may expose less than the complete Definition
Projection, but only as complete observations of their stated extent under ADR-0071. Equal Mapping Surface does not
prove equal Formation Meaning; equal selected Mapping Meaning does not prove equal complete Mapping Surface or
Definition Meaning. An unobserved, unavailable, or omitted projection does not prove semantic absence. Section 11.3
fixes the Mapping Surface and target-qualified selected Mapping Meaning observation laws without
requiring separate primary Mapping References or a particular physical selector.

All observations must be coherent within the applicable HIR snapshot and obey ADR-0071 visibility, lifetime, and
semantic equality rules. The same observations may be computed on demand, share immutable physical backing, or be
persisted and validated for reuse without creating another IR, Authority, or Contract Judgment. V2 may compare narrow
observations for local reuse or early cutoff only under current-reference and dependency-validity checks; no such
comparison substitutes for complete Definition Establishment or the clean recomputation reference path. No Query
architecture, cache granularity, physical Mapping layout, or incremental update algorithm is mandated.

### 11.3. Mapping Surface and Target-Qualified Mapping Meaning Projections

Lowering specializes the ADR-0071 Fine-Grained Semantic Projection law with two producer-owned observations over
one exact `LoweringDefinitionCandidateRef`:

```text
Lowering Mapping Surface Projection
Lowering Selected Mapping Meaning Projection
```

The **Lowering Mapping Surface Projection** exposes the complete declared Source Requirement-to-Fact-target relation
of that Definition. It preserves every declared Mapping occurrence, including source reuse, distinct Fact Kind
ownership, repeated Source/Target pairs, and their exact multiplicity. Each Target is qualified by the exact fixed
Fact Kind and its Fact-owned factual coordinate. The Surface may expose occurrence counts or equivalent complete
multiplicity-preserving meaning; it does not silently reduce a multiset to unique pairs. It observes the Mapping
relation, not a second copy of the complete Fact-owned factual surface or every selected Formation Meaning.

The **Lowering Selected Mapping Meaning Projection** uses one exact Target selection within the Definition:

```text
exact LoweringDefinitionCandidateRef
+ exact fixed Core Fact Kind candidate meaning
+ exact Fact-owned target coordinate
    -> complete multiset of declared Mapping meanings for that target
```

The Fact Kind and coordinate are identified through the Fact-owned HIR reference and legal observation law of
Section 11.1, not through an Operation parameter, a host type, an interface vocabulary index, or a generated
address. One selected observation preserves all Mapping occurrences for that Target and, for each occurrence, its
exact nominal Source Requirement, Source/Target relation, qualified callable application, receiver and argument
placement, meaning-determining constants, completed conversion applicability and meaning, Lowering-owned
partiality/refusal meaning, and exact semantic dependencies. Resolved external meanings remain direct references or
legal observations of their respective producers; Lowering does not repeat resolver, Type Engine, or library
semantic interpretation judgments.

A target-qualified selection may lawfully observe zero, one, or several declared Mapping occurrences before the
Lowering Definition Judgment. Zero is a positive statement about absence only when the exact Target is legally
referenceable and the complete Mapping observation for that Target is available in the coherent HIR snapshot. An
unknown Target, incomplete Surface, inaccessible Projection, or failed reference resolution does not establish
absence. The complete Fact-owned target-coordinate surface remains an independent prerequisite to any assertion
that every required factual coordinate has exactly one Mapping.

Under the V1 Lowering Definition law, exactly one declared Mapping must form each factual coordinate of every
fixed target Fact candidate. Two or more declarations for one exact Fact-kind coordinate violate that law whether
their Source Requirements or Formation Meanings differ or coincide. Two identical declarations are still two
occurrences. Multiple independent mappings from the same Source Requirement to *different* Target coordinates remain
permitted. Mapping selection and observation neither establish legality nor repair an invalid declaration by
merging duplicates, arbitrarily selecting a winner, or inferring a missing mapping. The owning Lowering Definition
Judgment rejects complete but unlawful duplicate or incomplete target-coverage declarations under Section 11.5.

An exact Source Requirement may also be used as an observation index when a legal consumer needs to discover its
uses. Such a reverse index merely reorganizes the existing declared relation under ADR-0071; it does not create an
additional primary Mapping subject, new authority, or Source uniqueness restriction. Individual Mapping occurrences
do not require persistent or primary Mapping References. The producer must preserve exact multiplicity even when
two occurrences have equal meanings; a transient observation selector or diagnostic provenance may distinguish
physical occurrences without giving them Contract identity. Declaration order, source locations, and storage
indices do not determine Mapping Meaning. Deterministic observation ordering may be selected by the HIR producer
without becoming semantic precedence.

Equality of the complete Mapping Surface covers the whole declared Source/Target relation and multiplicity, not the
qualified Formation Meaning of each mapping. Equality of one Selected Mapping Meaning covers exactly that Target's
complete multiset of Mapping Meanings and multiplicities, not any other Target's mapping, the complete Target Fact
surface, or the entire Lowering Definition. Neither equality implies Candidate Reference equality, authoritative
Definition identity, Establishment success, or current physical reuse eligibility. In particular, unchanged
Mapping projections do not discharge the owning Definition Judgment's complete coverage obligation when a referenced
Fact Kind's complete surface or other semantic dependencies change.

All such observations obey ADR-0071's current-reference, snapshot-coherence, visibility, completeness, Protocol
compatibility, and equality laws. V1 may materialize the entire relation and compute it afresh; V2 may reuse
individual complete observations or valid derived indexes after checking current dependencies. Neither a Query
Graph, persistent Mapping ID, cache-key design, storage layout, physical enumeration order, nor delta algorithm is
mandated. A narrow observation does not replace the complete Definition Candidate Projection of Section 11.2.

### 11.4. Lowering IDL Binding Candidate and Role Selection Projections

Under ADR-0071 Sections 4.13 and 8.9, the **Lowering IDL Binding Candidate Projection** exposes an actual,
exactly resolved operation-local IDL selection occurrence. Its complete selection meaning includes:

```text
exact LoweringIDLBindingCandidateRef
exact selecting OperationRef and independently required owning IDL selection context
exact Lowering role coordinate at the Operation-local selection scope
exact selected LoweringDefinitionCandidateRef
only independently meaning-determining Binding-owned selection qualifiers, if any
```

The selecting Interface or Interaction context is preserved where necessary to identify the exact selection under
its existing IDL/HIR reference law; it is not duplicated merely because it is structurally reachable from the
Operation. A Definition may be selected by several Operations, yielding distinct Binding Candidates without
copying, changing, or merging the selected Definition's Source Requirements, fixed Core Fact targets, Mapping
relations, or Formation Meanings. Several fixed Fact Kind targets within one Definition do not themselves generate
separate IDL Binding Candidates.

The **Lowering Role Selection observation** is read from the selection-owning IDL/HIR Operation subject at its
exact Lowering role coordinate. In valid Visible HIR the complete observation for that role is exactly one of:

```text
Selected: exact LoweringIDLBindingCandidateRef
Explicit Absence: no LoweringIDLBindingCandidateRef exists for that selection
```

This owning-role observation is not a fictitious Binding Candidate Projection for an absent selection. Explicit
Absence means only non-selection; whether absence is permitted is governed by the applicable separate Contract or
Composition law. Missing or unresolved selection, competing selections for one role, and inability to observe the
current Projection are not alternate valid selection states and cannot be silently converted into Explicit Absence.
An unresolved or conflicting selection cannot be published as a normal completed Role Selection in Visible HIR;
frontend resolution or HIR Seal retains its own unsuccessful result under the common laws.

An actual Binding Candidate is complete only when its selecting occurrence, role, required qualifiers, and exact
target Definition Candidate are resolved and referenceable under current HIR visibility and coherence rules. Such
completeness does not prove that the selected Lowering Definition satisfies its own law, is Established, or is
lawfully usable in the current Operation context. A fully resolved Binding may reference a complete Definition
Candidate whose duplicate Mapping will later be refused by the owning Lowering Definition Judgment. Binding
Establishment and any applicable Interface, Input, Canonicalization, Fact vocabulary, Operation participation,
Version, Governance, or Contract-World compatibility judgment remain separately owned.

The Binding Projection does not absorb the selecting Operation signature, current Input or Canonicalization
Definition or occurrence, Interface Fact vocabulary, runtime values, a present World, or the selected Definition's
payload merely because these may be needed in a later legality judgment. Only an exact qualifier independently
owned by this IDL selection belongs to its Binding Candidate meaning. Neither World selection nor a current
Binding may silently substitute another Lowering Definition, Version, Fact target, or qualified Formation Meaning.

Exact Binding Candidate Reference equality, complete Binding selection-meaning equality, complete Lowering
Definition meaning equality, authoritative Binding identity, and physical reuse eligibility are independent.
A change in another Operation role or current Interface vocabulary may leave the Lowering IDL selection observation
unchanged while requiring a separate Composition or application judgment to be reconsidered. V2 may exploit that
narrow unchanged observation only with current semantic dependencies, correct ownership, and ADR-0071
snapshot/Protocol-validity checks; it cannot infer successful Establishment or current use legality from local
Binding equality.

### 11.5. HIR Seal, Early Static Verification, and Legal Establishment Entry

The Lowering Resolved HIR Candidate Protocol of Sections 11.1-11.4 is subject to the common ADR-0071 HIR Seal,
visibility, lifecycle, and Protocol conformance laws. HIR Seal verifies complete, resolved, coherent pre-authority
candidate meaning; it is not a Lowering Definition Judgment, Binding Judgment, Fact Judgment, or new Contract
Authority. The compiler may detect static Contract violations at the earliest lawful point where the required
resolved meaning and authoritative prerequisites are available. Such early physical execution does not transfer
the owning law or relax a Judgment's legal entry conditions.

For a Lowering Definition Candidate, Seal requires the exact owning Authority and Version candidate coordinate,
complete nominal Source Requirements, all exact fixed Fact Kind candidate references and Fact-owned coordinate
references, complete declared Mapping relation with its multiplicity, completed qualified Formation Meaning,
exact semantic dependencies, and coherent legal observations of every required Projection. Each direct reference
must be resolved and referenceable in its own producer's pre-authority HIR domain. Seal does not require a Fact
Definition to acquire Establishment authority merely because its exact Candidate meaning is referenced. Unknown
or unsupported required semantic material, incompatible Protocol observation, incomplete or dangling references,
poison/recovery placeholders, and mixed or invalid HIR snapshots cannot be silently sealed or made Visible.

For a Lowering IDL Binding Candidate, Seal requires the exact operation-local selecting subject, Lowering role,
complete selection qualifiers where required, and an exact referenceable target Lowering Definition Candidate.
The selection-owning subject must support the complete `Selected` or `Explicit Absence` observation for its role;
missing or conflicting selections must not be normalized into a successful Visible HIR selection. Fact vocabulary
membership and the current Operation's Input/Canonicalization compatibility are not inferred or established by
HIR Seal; they remain separate lawful use judgments.

A declaration may be completely resolved yet violate the Lowering Definition law. In particular, two distinct
Formation Meanings assigned to one Target, two identical Mapping declarations assigned to that Target, or an
otherwise complete declaration that omits formation for a required Fact coordinate may remain exactly represented
in a sealable pre-authority Candidate. HIR construction, projection, and Seal must not deduplicate mappings,
insert implicit target defaults, choose a winning Formation, change a Fact target, or invent missing declared
relations to conceal the violation. The complete but unlawful declaration is rejected by the owning Lowering
Definition Judgment. This does not make it lawful or permit successful code generation.

Conversely, an unresolved Fact Kind or factual coordinate, nominal Source Requirement, or required qualified
callable/semantic interpretation cannot supply complete Visible HIR meaning. A compiler producer or entered HIR
Seal verifier that detects failure of its own resolution or completeness obligation retains its own unsuccessful
compiler result. A failed required producer before legal Seal entry does not cause a synthetic HIR Seal rejection.
When a separately owned authoritative prerequisite fails before Lowering Definition Judgment entry, no Lowering
Definition Refusal is fabricated; that prerequisite's result remains its owner's. An entered Lowering Definition
Judgment that finds duplicate Mapping formation, missing complete Target coverage, or another violation of
Lowering-owned Definition law must refuse the Definition rather than silently warn and establish it.

A Lowering Definition Judgment may legally enter only after its complete Resolved Definition Candidate is available
through the valid Visible HIR Candidate Protocol and every authoritative Definition-time Required Basis specified in
Section 11.6 is complete under ADR-0063. The current Lowering IDL Binding, selected Input or Canonicalization
Occurrence, current Operation parameter surface, and current application World do not automatically become
Definition-time prerequisites merely because a selection later uses that Definition. An HIR Candidate Reference does
not satisfy an Established Fact Definition requirement.

Early static verification may execute the owning Lowering Definition Judgment during frontend processing as soon as
its lawful inputs are ready, including an immediate compile-time refusal of fully resolved duplicate or incomplete
Target formation. This does not require a separate frontend pass, a second duplicate check, or physically distinct
HIR-builder, Seal, and Judgment execution. Conversely, pass fusion, parallelism, or V2 incremental work may not
merge the legal meanings of resolution failure, HIR Seal failure, Lowering Definition Refusal, or current Binding/
Composition incompatibility. Each remains attributed to its owning law; a dependent judgment that never legally
entered has no invented result.

V1 clean recomputation and V2 incremental, parallel, lazy, or reused observations must satisfy the same Seal and
Establishment entry obligations. An old Generation's successful Seal or a cached selected Mapping Meaning does not
certify the current HIR Candidate or current Fact Target Coverage. Every reused observation and prerequisite must be
current, compatible, complete for its declared extent, and coherent under ADR-0071. Where safe reuse cannot be
established, the compiler must follow an applicable legal re-resolution or deterministic clean recomputation path;
implementation schedules, physical HIR storage, frontend validator placement, and incremental algorithm choices
remain open.

### 11.6. Definition-Time Required Basis and Established Definition Direct Relations

Every distinct fixed Core Fact Kind targeted by a Lowering Definition requires its exact Established Fact Definition
Meaning as a mandatory Definition-time Required Basis. The exact Fact Kind and its applicable Version determine one
Required Basis coordinate. Multiple Mapping occurrences for that Kind do not create additional Fact Definition
requirements. No other Fact Kind or Version may satisfy it, even when its factual surface is equal. A Fact HIR
Candidate Reference does not establish this Basis.

A separately owned Contract Law is an additional Definition-time Required Basis only when its owning law requires
prior authoritative Establishment for the Lowering Definition. A refinement or bound stated completely within the
Lowering Definition does not create such an external Basis. Resolved callable, Type Engine, and qualified library
interpretation remain compiler-owned semantic inputs to complete HIR; they are not Contract Required Basis merely
because Formation Meaning consumes them. The Lowering Definition's own Version Binding follows ADR-0063 and ADR-0053
without inventing another universal Basis.

Each Required Basis uses the exact source domain determined by its owning law. The fixed Fact requirement admits
exactly one Applicable Established Fact Definition and no permitted absence. Its exact Basis Binding, Applicability,
and Complete Basis follow ADR-0063 Sections 7 and 8. A resolved reference or successful compiler lookup alone cannot
satisfy it. An incomplete prerequisite prevents lawful Definition Establishment entry; it does not produce a
Lowering Definition Refusal.

A successfully Established Lowering Definition preserves a direct Established relation to the exact Established
Fact Definition of each fixed target Kind. Its Mapping targets retain their Fact-owned coordinate meanings. A
separately Established Contract Law is directly referenced when its meaning determines this Lowering Definition.
These direct relations preserve the source Authorities without copying or re-establishing their Definitions. They do
not duplicate Basis Binding or its internal representation, add independent Mapping Authorities, or introduce current
Operation Binding or compiler realization material into Definition Meaning. The complete immutable Definition Meaning
and authoritative identity remain as specified in Section 11 and ADR-0063.

### 11.7. Established Lowering Definition Semantic Protocol

Lowering uses the read-only Established Semantic Protocol of ADR-0063 Section 11. The complete Established
Definition Projection observes one exact `EstablishedLoweringDefinitionRef` and all its established
Definition meaning under Section 11, including the direct relations fixed by Section 11.6 and the
Occurrence Basis Requirement Law in Section 14.1. This observation
does not require the source declaration or Resolved HIR Candidate.

The **Established Mapping Surface Projection** exposes the complete established Mapping relation. The **Established
Selected Mapping Meaning Projection** exposes the complete Formation Meaning for one exact
fixed Fact Kind and its factual coordinate. Their observation scopes follow Sections 11.2-11.3, applied
only to lawfully Established Definition meaning. Fact-owned meaning remains accessible through its direct
Established relation rather than a copy owned by Lowering.

The following example observes the same Definition after lawful Establishment. `L1` and `F3`
are illustrative Version coordinates, not new DSL syntax. The complete target surface is the same
as in the HIR example above.

```text
Established Lowering Definition Reference
    Authority: DepositLowering
    Version: L1

Established Lowering Definition Projection
    Source Requirement: accountIdText -> exact required presentation meaning
    Source Requirement: amountText -> exact required presentation meaning
    Fixed Target: DepositCommand at Fact Version F3
    Mapping: accountIdText -> DepositCommand.accountId
    Mapping: amountText -> DepositCommand.amountMinor
    Formation: accountId <- qualified Long.parseLong(accountIdText)
    Formation: amountMinor <- qualified Long.parseLong(amountText, 10)
    Semantic Dependencies: exact established meaning-determining relations
    Occurrence Basis Requirement Law as defined in Section 14.1
    Direct Established Relation: exact DepositCommand F3 Fact Definition

Established Mapping Surface Projection
    DepositCommand.accountId -> accountIdText (1 occurrence)
    DepositCommand.amountMinor -> amountText (1 occurrence)

Established Selected Mapping Meaning Projection
    Target: DepositCommand.amountMinor
    Source: amountText
    Callable: exact java.lang.Long.parseLong(String, int)
    Receiver: none (static method)
    Argument 0: amountText
    Argument 1: constant 10
    Conversion: exact qualified formation meaning and applicability
    Partiality / Refusal: exact applicable Lowering-owned meaning
    Semantic Dependencies: exact established meaning-determining relations
```

The direct Fact relation does not copy the Fact Definition into Lowering. This example does not
specify an Established Material data structure or a physical Protocol representation.

These observations grant no new Authority. Their reference, completeness, equality, availability, and
current-validity laws follow ADR-0063 Section 11. They require no additional Mapping Reference or physical
Protocol representation.

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

One Lowering Occurrence applies the Established Lowering Definition to the lawful source Presentation of one
exact inbound application. Equal source values from distinct Input Occurrences do not merge those applications,
and physical re-evaluation of the same application does not create a new Occurrence.

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

### 14.1. Occurrence Basis, Applicability, and Composition Boundary

Every Lowering Occurrence requires the exact Presented Input Occurrence of its inbound application.
When Canonicalization is selected for that application, Lowering also requires its successful whole Occurrence.
The reusable requirement law belongs to Lowering Definition meaning. Each application owns the requirements
instantiated for that application.

```text
requirement coordinate
    presentedInput

required meaning
    one Established Input Occurrence whose result is Presented

cardinality
    exactly one

permitted absence
    no

requirement coordinate
    selectedCanonicalization

instantiation
    only when Canonicalization is selected for the exact inbound application

required meaning
    one Established Canonicalization Occurrence whose whole result is Canonicalized

cardinality
    exactly one

permitted absence
    no
```

When Canonicalization is explicitly absent, `selectedCanonicalization` is not instantiated. This is not permitted
absence of an instantiated requirement. The `presentedInput` requirement remains mandatory.

ADR-0048 determines the legal upstream sources. Basis Binding must identify the exact Occurrences supplied by that
Composition. The Canonicalization Occurrence must have applied to the same Input Occurrence bound to
`presentedInput`. Equal values from another Input origin do not satisfy that relation. Section 5 determines which
Presentation values Lowering consumes.

Admission continuation remains an obligation of inbound Composition under ADR-0048. Lowering does not instantiate
an Admission Required Basis or rejudge Admission's result. If the selected Admission has not established the
required continuation, Lowering cannot enter.

Basis Binding, Applicability, and Complete Basis follow ADR-0063 Sections 7 and 8. V1 requires no additional
Applicable Context for these Lowering requirements. A legal Binding for the exact application is Applicable under
this law. Complete Basis requires exactly one Applicable Basis for every instantiated requirement. An incomplete
or ambiguous Basis prevents entry; it does not establish a Lowering Refusal. The original Input cannot replace a
missing successful selected Canonicalization Occurrence.

### 14.2. Established Lowering Occurrence Material

The exact `EstablishedLoweringOccurrenceRef` identifies the legal semantic application described in Section 14.
The Material follows ADR-0063 Sections 4.4.1 and 7.6. Lowering establishes one whole outcome for its completed
Occurrence Judgment:

- **`Lowered`:** The complete candidate set has been formed under the Lowering law.
- **`Refused`:** The Lowering Judgment establishes a refusal under Section 15.

Only an entered Lowering Judgment may establish its own outcome. A prerequisite that prevents entry does not
establish either result. A subsequent unsuccessful judgment does not change an established `Lowered` outcome.

The successful Occurrence has the following complete semantic material:

```text
Established Lowering Occurrence
    exact EstablishedLoweringOccurrenceRef
    exact legal Lowering application
    exact applied EstablishedLoweringDefinitionRef
    complete Applicable Basis attribution
    whole outcome Lowered
    complete Fact Candidate set
```

The Basis attribution preserves the exact Binding and Established Input Occurrence for `presentedInput`.
When `selectedCanonicalization` is instantiated, it also preserves the exact Binding and Established
Canonicalization Occurrence. The same-Input-origin relation in Section 14.1 remains observable. These references
retain the upstream Authorities without copying their Material.

The complete Fact Candidate set contains exactly one complete candidate for every fixed target Fact kind.
It establishes no Fact authority. For `Refused`, the whole outcome is established without a complete Fact Candidate
set. Section 15 governs refusal attribution; its detailed Established refusal Material and Failure integration
remain for the Failure design.

The exact application and its established relations remain immutable. Reusing equal candidate formation does not
merge distinct Occurrences or replace their exact Basis attribution.

### 14.3. Established Lowering Occurrence Semantic Protocol

The Established Semantic Protocol of ADR-0063 Section 11 exposes Lowering through two typed reference domains:

```text
EstablishedLoweringDefinitionRef
EstablishedLoweringOccurrenceRef
```

Section 11.7 governs Definition observations. The following projections observe one exact Lowering Occurrence:

```text
Established Lowering Occurrence Projection
Lowering Occurrence Outcome Projection
Complete Fact Candidate Set Projection
```

The **Established Lowering Occurrence Projection** exposes the complete Occurrence Material under Section 14.2.
Its direct relations are observable through the Direct Established Relation Projection of ADR-0063. The **Lowering
Occurrence Outcome Projection** exposes only the whole `Lowered` or `Refused` result.

The **Complete Fact Candidate Set Projection** is available only when the whole Occurrence is `Lowered`.
It exposes the entire formed set, not a selected subset. For `Refused`, the projection is unavailable rather
than an empty successful result.

For example, the `DepositLowering` declaration in Section 7 forms its complete candidate from the lawful
source values `"12"` and `"100"`:

```text
Lowering Occurrence Outcome Projection
    Lowered

Complete Fact Candidate Set Projection
    DepositCommand(accountId = 12, amountMinor = 100)
```

For a refused occurrence:

```text
Lowering Occurrence Outcome Projection
    Refused
```

The complete candidate set is not available in the second example. Neither example represents a host carrier
or a physical Protocol layout. Subsequent authorities consume the candidate set under Section 17 without acquiring
Lowering's judgment authority.

Projection completeness, equality, availability, and reuse follow ADR-0063 Section 11. Equal outcomes or candidate
sets do not establish equal Occurrence identity or current Basis validity. No separate Mapping Occurrence Reference
or Candidate-specific Lowering Authority is introduced. Detailed refusal observations remain for the Failure design.

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

The exact Fact-qualified target spelling and physical Mapping selector encoding remain open. Sections 11.1-11.5
fix Lowering's HIR Candidate References, complete Definition Candidate Projection, complete Mapping Surface and
target-qualified Selected Mapping Meaning Projections, operation-local Binding Candidate and Role Selection
observations, and HIR Seal/Definition Judgment entry boundary. They do not establish the Fact-owned HIR Protocol
internals, a concrete frontend execution architecture, or any successful Contract Establishment result.

Sections 11, 11.6, and 11.7 fix Established Lowering Definition meaning, Definition-time Required Basis,
direct Established relations, and Established Definition observations. Sections 14.1-14.3 fix the Occurrence Basis
Requirement Law, whole outcome, successful Established Occurrence Material, and Occurrence observations.
Binding Establishment remains separate follow-up work. Detailed refusal Material and Failure observations remain
for the Failure design.

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