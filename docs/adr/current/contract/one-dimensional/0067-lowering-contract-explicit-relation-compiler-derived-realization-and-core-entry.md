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

This amendment removes the earlier model in which every selected Lowering declaration produced a retained
generated realization port with exactly one user-supplied implementation.

Instead, the user declares a one-dimensional Contract: the exact source-to-target relation and the Contract
material needed to complete it. Kontrakt resolves and establishes that meaning, then derives the knowledge
the compiler/backend needs to form actual values. The user does not implement a Lowering mapper, adapter,
converter port, callback, or generated Lowering interface.

Operation remains the ordinary user-supplied realization boundary. The backend forms complete candidates for
the Core-owned Fact kinds fixed by Lowering; the selected Operation has its own IDL Binding and applicable
Core judgments for Fact parameter participation. An Operation parameter is not a Lowering target.

Internal helpers remain permissible under Section 12. The amendment preserves the V1 mapping law (Section
8), independence of source and target (Sections 5–6), explicit conversion authority (Sections 7 and 9), the
prohibition on hidden cross-coordinate business computation (Section 10), and the separate candidate
formation, standing Invariant, movement, Fact establishment, and refusal boundaries (Sections 14–15).

---

## 1. Context

Lowering is the final representation boundary of the inbound airlock. Input, Admission, and selected
Canonicalization operate over the Contract-visible external presentation. Admission may permit continuation,
and Canonicalization may establish a stable same-shape representative. Neither ends external-presentation
authority.

Lowering declares how selected boundary coordinates may form candidates for one or more explicitly fixed
Core-owned Fact kinds. This complete permitted relation is independent of the selecting Operation's
parameter surface. It is Contract expression, not a request for user-written representation-formation code.

Kontrakt resolves the Definition's nominal source requirements, exact Fact targets, and qualified formation
meaning. The operation-local IDL Binding separately qualifies the Definition against the selected Input,
optional Canonicalization, enclosing Fact vocabulary, and Operation context. From the lawfully established
meaning, the compiler derives the realization obligations in Sections 12–13; the backend performs physical
value formation.

The generated machine ensures relation and candidate completeness and preserves refusal routing. It also
preserves the independently owned standing, movement, and Fact establishment judgments and the later lawful
Operation handoff. Lowering does not acquire those judgment authorities.

---

## 2. Problem

An arbitrary mapper lets implementation code decide which source values matter, which target coordinates
participate, how Facts are formed, and when the core may trust the result. A generated mapper interface
merely moves that hidden Contract authority into a callback selected at assembly time.

Compiler inference poses the same problem. Matching names, equal types, raw host-type pairs, classpath
discovery, backend support, and framework conventions cannot supply conversion authority (Section 9).

Lowering therefore separates one declarative Contract surface from a replaceable compiler/backend
realization. The Contract owns the permitted relation and every semantic refinement needed to complete it;
realization applies that established meaning to actual runtime values.

If the meaning is insufficient to determine a lawful realization, Kontrakt must reject the Definition or the
Contract language must gain an explicit way to express the missing meaning. User implementation cannot
silently fill the gap.

---

## 3. Decision Drivers

The design rests on three constraints:

- **Contract scope.** Lowering contains only immutable Contract material. It is neither business computation nor an
  Invariant, State Transition, Publication rule, or result producer (Section 10).
- **Explicit relation.** Each mapping has one source and one target. V1 permits source reuse through distinct mappings,
  but neither multi-source formation nor fan-out within one mapping. Names and host types confer no implicit relation.
  Each fixed Fact kind has a complete factual surface independent of the Input declaration, source object graph, and
  Operation parameter surface (Sections 6–9).
- **Realization freedom.** The compiler may derive planning, completion checks, verification, diagnostics, caching, and
  optimization from the established relation. The backend may choose any physical strategy that preserves it and its
  representation law. Physical work does not require a Java or Kotlin user implementation, and implementation
  convenience cannot supply missing Contract meaning.

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

Lowering forms candidates only by realizing the declared relation over actual source values. For Core entry,
external-presentation authority ends only after the entire target candidate set is complete and every
required independently owned judgment succeeds. Incomplete or partially successful sets acquire no Fact
authority.

Backend value formation owns neither Fact meaning nor establishment. The user Operation does not perform Lowering.

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

The selected Input schema remains the source schema: Canonicalization does not change Contract-visible
coordinates. When selected, its representative values become the source values; otherwise, the admitted
Input values continue unchanged.

No separate `CoreMaterial`, `OperationStart`, `LoweringTarget`, or user-visible candidate schema is introduced.

Each Lowering Definition explicitly fixes one or more exact Core-owned Fact kinds and the factual
coordinates it forms for each. Their Definition meaning is independently determined in the Core, not
produced by Input, Lowering, Output, or Publication compilation. A successful occurrence forms exactly one
complete candidate of each fixed kind.

Two separate relations qualify use of the Definition:

- The enclosing interface's `facts` vocabulary neither creates nor reselects its targets.
- The selected Operation supplies the IDL Binding context and declares its Fact parameters independently. Neither its
  parameter list nor its current signature determines or silently changes those targets.

Targets are Fact-owned addresses, not Operation parameters. The Operation manifest neither repeats the Fact
vocabulary nor gains a `fact` or `invariant` slot merely for Lowering.

---

## 6. Target Fact Independence

The Fact surface must be intelligible from its own declared material: coordinates and sorts; presence and
alternatives; relations and ordering; bounds, schema, and Version.

It must not name, import, embed, extend, or retain a reference to any of the following:

- the Input declaration, source DTO, or transport protocol;
- a host object, framework context, or adapter type;
- the Lowering declaration or source-coordinate handle.

The backend may read source values and materialize ordinary target values. Neither source type names,
getters, constructors, collection implementations, object identity, runtime references, generated helpers,
nor backend machinery have factual authority.

A generated host carrier may exist because the Fact representation or selected Operation ABI requires it.
Such a carrier is representation, not Fact authority. Eliminating an intermediate carrier is a physical
optimization only, and is lawful if the backend preserves the host-visible Operation call and every Contract
obligation.

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

Each application names its selected source coordinate exactly once, either as an explicit argument or as the
receiver. All other arguments must be literals or statically resolved constants; their values and meanings
must be complete before Establishment. The DSL neither inserts a source argument nor infers its position,
and it does not use implicit default values.

The right-hand side permits one callable application, not a general expression body. It excludes:

- nested calls;
- another runtime source coordinate or runtime-computed arguments;
- open dispatch or user callbacks.

Imports shorten names without changing the requirement to resolve one exact callable. Ambiguous or
unresolved applications are rejected. Resolution and interpretation remain with the Resolver, Type Engine,
and library semantic interpretation, not Lowering.

The relation body contains only Lowering-owned Contract material. It does not repeat Input carriers,
Operation parameters, Fact declarations, or Invariant declarations. Joint formation of several distinct Fact
kinds establishes no relation between those Facts.

Every mapping is explicit, whether the source and target spellings are equal or different. If qualified
callable meaning and the applicable Contract meaning do not determine the required formation, the
declaration is incomplete. A user-written converter cannot extend it implicitly.

---

## 8. V1 Cardinality Law

The Contract-visible V1 relation is one explicit mapping at a time:

```text
one selected Input coordinate
-> one coordinate of one explicitly fixed Core Fact kind
```

The V1 law applies to each explicit mapping and to the complete candidate set:

| Subject                | Law                                                                                                                                                                                                                                       |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| One mapping            | Exactly one selected Input coordinate forms exactly one factual coordinate. Neither fan-out nor multi-source formation is permitted.                                                                                                      |
| Source coordinate      | May be unused or reused in distinct explicit mappings, including mappings to different Fact kinds. Such reuse does not authorize cross-coordinate computation within one mapping.                                                         |
| Target coordinate      | Must be formed exactly once within the occurrence. Duplicate formation of the same Fact-kind coordinate is prohibited.                                                                                                                    |
| Fixed target Fact kind | A Definition may fix several distinct kinds, each requiring one complete candidate factual surface, independently of the number and coverage of Operation parameters. Joint formation establishes no implicit relation between the Facts. |

These are Contract cardinalities, not storage restrictions. Physical storage may be split, packed,
flattened, or combined behind the same relation.

---

## 9. Type Relation and Compiler-Derived Realization

Source and target host types may be equal or different. Equality does not authorize implicit copying or
remove the explicit relation; difference does not require a user implementation. Their sorts need not be
directly structurally compatible if a qualified conversion can form the target from the source.

The declared relation identifies both endpoints. Three compiler responsibilities determine whether its
conversion is valid for the declared source and target meanings and the applicable Contract-owned
representation law:

- The Resolver selects the exact callable.
- The Type Engine judges argument and result type applicability.
- Library semantic interpretation supplies qualified conversion meaning under the applicable semantic basis.

Lowering consumes that completed judgment to establish the explicit coordinate relation, without rejudging the
conversion.

Supported methods may use qualified Java/Kotlin standard-library operations or Kontrakt-supported formation-library
operations, including parsing and decoding. An open or dynamically selected callable cannot complete Lowering meaning.
The same applies when any required callable dependency remains unresolved or its behavior cannot be interpreted.

Conversion authority cannot be inferred from a raw host-type pair, a compatible catalog entry, a library's
mere presence, naming conventions, classpath contents, or backend support.

Once the established Contract and qualified callable interpretation completely determine a permitted
representation refinement, the compiler may realize it using any supported operation that preserves it.
Examples include library primitives, generated helpers, tables, parsers, copiers, constructor paths, and
direct machine representations. Incomplete meaning must cause compilation to fail rather than trigger a
request for a user mapper.

Lowering realization must not perform:

- repository lookup or environmental resolution;
- business computation or undeclared capability access;
- Invariant judgment, State movement, or Publication.

A selecting Operation parameter supplies neither target authority nor an implicit conversion rule.

---

## 10. Representation Refinement, Not Core Computation

Lowering changes representation and authority domain without deriving new cross-coordinate business meaning.
Each mapping method determines how its source's declared meaning is formed in the target representation. The
V1 cardinality law in Section 8 applies throughout.

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

Only when the established Contract already determines the exact refinement may the compiler/backend:

- decode, parse, range-check, or copy a value;
- freeze an approved bounded leaf;
- make an already-declared presence distinction explicit;
- select a fixed internal representation.

A generic text coordinate does not become a date, account identity, money, or another business concept
merely because a backend library can parse it.

Section 8 prohibits fan-out and multi-source formation within a mapping. The restriction is semantic: a
backend may decompose one Fact coordinate into several machine words or pack several coordinates into one
region. Reuse of a source through separate lawful mappings is not a single `1:N` mapping.

Lowering must not:

- create a new business proposition, execute core computation, or produce Operation Result Material;
- consult mutable current machine state or apply business Policy;
- decide legal movement, evaluate an Invariant, select a Transition, or grant Publication authority;
- infer missing material, combine source coordinates within one mapping, or split one mapping into several
  Contract-visible meanings.

Distinct explicit mappings may lawfully reuse a source coordinate.

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

All five determinants must be resolved before the Candidate becomes Visible HIR. Establishment cannot
reconstruct missing Definition meaning from the IDL Binding Candidate, current Operation, source syntax, or
backend behavior. Frontend resolution must retain every resolved mapping occurrence, even when its
multiplicity violates Lowering law; it must not silently repair or discard mappings. Unresolved meaning
cannot enter Visible HIR.

The declared relation and its multiplicity, including repeated identical mappings, determine meaning.
Independent mapping order, source provenance, and physical mapping indices do not determine identity or
equality. Any order explicitly belonging to a Fact coordinate, callable argument application, or another
separately owned law remains governed by that law.

A nominal Source Requirement does not incorporate the currently selected Input Definition. The Target
Requirement, by contrast, fixes every exact Core Fact kind and its required factual coordinates
independently of the current Operation Parameter Reference. Those Fact meanings and their applicable
versions remain Definition dependencies. Current Input and Operation selections must satisfy their own
Binding and application requirements; they cannot introduce an unrecorded alternate target, callable, or
Formation Meaning.

Lowering consumes the lawful Presentation supplied for the actual application. If Canonicalization is
selected, its selected coordinates use final representatives while unselected coordinates retain
Input-established values. Lowering neither selects Canonicalization nor reconstructs the Presentation or
judges upstream refusal or continuation. Current upstream sources and entry results are application
prerequisites, not Definition determinants merely by being prerequisites. The reusable Occurrence Basis
Requirement Law in Section 14.1 does belong to Definition meaning.

For a selected Operation, the Definition and its separate operation-local Binding, together with the applicable
separately owned judgments, must close at least:

**Selection and context**

- the flow-selected Input schema
- the selected Canonicalization law, when present
- the enclosing interface's Fact vocabulary and standing Invariants
- the selected Operation signature for its own IDL Binding and application obligations, not as a Mapping Target

**Relation and target coverage**

- every explicitly fixed target Core Fact kind and its complete factual surface
- every explicit Input-coordinate to fixed Fact-kind-coordinate mapping
- one source coordinate and one Fact-owned target address for every mapping
- explicit mapping even when source and target names or host types are equal

**Formation meaning**

- completed conversion applicability for the source and target, as judged by the Type Engine and library semantic
  interpretation
- the selected mapping method's exact resolved callable and qualified formation meaning under the applicable semantic
  basis
- the explicit source argument or receiver and every meaning-determining constant argument
- the Contract-owned representation refinement required by each non-identical representation

**Completion and limits**

- complete factual coverage and unique formation for every fixed target Fact candidate
- explicit source-coordinate reuse only through distinct, independently lawful mappings
- finite depth, cardinality, intermediate storage, output, and work bounds
- applicable schema, Version, and Governance material
- Lowering-owned refusal meaning and separately owned cross-contract stop attribution

Every factual coordinate of every fixed target Fact candidate must be formed exactly once under the complete flat
Lowering Definition. Each target Fact kind must have one complete candidate; no target may be left partial.

An Input coordinate may appear in several distinct, explicit mappings. An Input coordinate may also remain unused.
Neither case implicitly creates a relation or requires an Operation parameter Fact surface to reproduce external
material.

Implicit formation is prohibited. This includes silent target defaults, fallback constructors, same-name
auto-mapping, and same-type auto-copying. Nor may catalog selection, structural guessing, package scanning,
annotations, assignability, inheritance, or implementation-shape discovery supply the relation.

A Lowering Definition cannot be established as lawful if it contains any of the following:

- missing or duplicate formation of a target Fact coordinate;
- multiple targets in one mapping or a target requiring combined runtime source coordinates;
- hidden absence, recursive host-object traversal, or unbounded collection work;
- environment-dependent Contract meaning;
- executable user material inside the relation body;
- representation refinement not closed by the declared Contract and qualified callable interpretation.

Fully resolved duplicate mappings must remain visible to the owning Lowering judgment. Repeated source
coordinates in distinct explicit mappings are not a rejection reason.

By contrast, unresolved nominal Source Requirements, unresolved or falsely attributed Fact kinds or
coordinates, and incomplete or unsuccessful qualified conversion resolution cannot supply complete Visible
HIR meaning. The owning compiler subsystems remain responsible for exact callable resolution, source/target
sort applicability, and qualified Java/Kotlin library interpretation. Lowering consumes the completed
meaning without rejudging it.

A fixed Fact kind absent from the enclosing interface's current vocabulary is a Binding/Composition
compatibility defect, not a missing or alternate Definition target. Section 11.5 distinguishes HIR Seal from
Definition Refusal: fully resolved target multiplicity and coverage defects are Definition-law violations
rather than missing HIR meaning.

Because the user supplies no Lowering implementation, there is no missing-Lowering-implementation link
failure. A backend that cannot realize a semantically complete Contract with the required guarantees must
reject that realization; its inability does not change the Contract.

The established Lowering Definition is one flat, immutable, adapter-erased Contract material. It preserves
the complete meaning of all five determinants above, including the exact argument application, complete
target surfaces, Mapping multiplicities, bounds and refusal meaning. It also preserves the Occurrence Basis
Requirement Law in Section 14.1.

One independently declared Lowering Contract denotes one Lowering Authority. That Authority and one exact
Contract Version identify one complete Definition under ADR-0063 and ADR-0053. Conflicting resolved meanings
cannot be established for the same Authority and Version; equal meanings under different Authorities or
Versions do not merge their identities. Import spelling and source location do not change Definition
meaning.

Selection by an Operation creates a separate Binding, preserving the exact Lowering Definition and selected
Operation References. Before use, that Binding must resolve the selected Input, optional Canonicalization,
Operation signature, and applicable flow-world basis. Fact-parameter participation remains separate from the
Definition's fixed targets and target coverage.

Contextual resolutions do not become Definition determinants merely through selection. If an exact basis
determines the Definition's own meaning, however, ADR-0063 requires that dependency to remain with the
Definition.

CRI may prepare exact compiler-time source and target correspondences from legal producer projections. It
neither establishes Binding validity nor invents mappings or forms runtime source values. A Binding mismatch
must be rejected rather than repaired by changing the declared relation or qualified callable meaning.
Definition and Binding identities remain distinct even when only one Operation uses the Definition.

Backend helper identity, generated class identity, host object identity, allocation strategy, and
machine-code shape are not part of Contract meaning.

From ratified material, the compiler derives one immutable semantic `LoweringPlan` or equivalent realization
knowledge. It closes:

- the exact source read set and target Fact-coordinate write set;
- required representation operations and completion checks for each fixed candidate and the entire set;
- declared refusal branches;
- lawful Invariant and movement handoff and establishment requirements;
- cache dependencies and backend layout requirements legitimately implied by the Contract.

This derived material is not another authored Lowering Contract.

### 11.1. Resolved Lowering HIR Candidate References and Core Fact Dependency

Lowering specializes the ADR-0071 Typed HIR Semantic Reference Domain with two Lowering-owned primary Candidate
reference kinds when their exact subjects must be observed:

```text
LoweringDefinitionCandidateRef
LoweringIDLBindingCandidateRef
```

`LoweringDefinitionCandidateRef` identifies one exact, current, resolved Definition Candidate. The
independently declared Definition and its applicable Version determine the nominal identity scope within the
owning Lowering Authority. The resolved Version candidate coordinate is scoped to that Authority. If several
independent Definitions ever need to be distinguished within one Authority and Version, the authority-local
coordinate law of ADR-0063 applies. Source order, host symbols, IDL selections, and physical indices cannot
supply that distinction.

This is a pre-authority reference, not an Established Definition Reference.

`LoweringIDLBindingCandidateRef` identifies one exact operation-local IDL selection occurrence. Existing
IDL/HIR reference laws govern observation of its selecting Contract Interface/Interaction/Operation context,
Lowering role, exact target Definition Candidate, and any independently required Binding qualifier.

Distinct Operation selections may target the same Definition Candidate without merging their Binding
Candidates or changing its Fact targets. Section 11.4 governs the exact owning-role distinction between
selection and Explicit Absence under ADR-0071.

The nominal Source Requirement belongs to the Lowering Definition Candidate, not the selected Input
Definition. Every required Source Coordinate must be exactly selectable within the owning resolved
Definition; every explicit Mapping occurrence must likewise remain observable with its exact multiplicity.
No separate primary `SourceRequirementRef`, new primary Mapping Reference, or independent Mapping Authority
is required.

Source position, declaration order, array slot, generated helper, and generation-local handle do not govern
semantic selection or identity. Section 11.3 defines target-qualified selection without prescribing a
physical selector.

A fixed Target Requirement consumes the exact pre-authority meaning of a separately Core-compiled Fact Kind
and its Fact-owned factual coordinates. The Fact HIR Protocol owns the reference. Lowering neither mints nor
reinterprets it, and leaves the representation, name, and encoding to Fact. For example, Fact may identify a
Kind directly or by an exact Fact Definition Candidate with a Fact-owned nominal Kind coordinate.

The Kind and its coordinates are not inferred from Operation parameters, the Input surface, interface
vocabulary membership, host class names, or physical addresses. Where complete coverage requires the full
Fact-owned surface, that surface must be legally observable without copying or recursively embedding the
Fact Definition in Lowering HIR.

As established in Sections 5–6, Fact Definition compilation and authority belong to the Core, independently
of inbound, output, and publication contracts. The Fact-to-Lowering reference expresses semantic dependency
rather than a mandatory serial pass schedule. A Fact HIR Candidate reference, an Established Fact Definition
Reference, and a runtime Established Fact are distinct. HIR lookup does not establish the Fact Definition or
grant its Required Basis; ADR-0063 and the later Lowering Establishment judgment govern that Basis.

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

A Projection expresses Lowering-owned meaning and exact direct references to separately owned subjects. The
Fact, Input, Type Engine, and library retain their own meaning; a legal reference and producer-owned
projection can expose it completely without redundant copies or recursive expansion.

The reusable Occurrence Basis Requirement Law is part of Definition meaning. Its instantiated requirements
and exact Basis Bindings belong to the application (Section 14.1). Current Operation selection, Input or
Canonicalization occurrences, runtime values, and Basis Bindings do not become Definition meaning merely
because a later application needs them.

Mapping observations preserve every declared occurrence, including exactly resolved duplicates. Section 11
governs which parts of the relation determine meaning. The Projection must not deduplicate, overwrite,
default, or normalize away an invalid but fully resolved mapping; it does not perform Definition Judgment or
alter the resolution and Fact laws.

The complete Definition Projection governs comparison of the entire Definition meaning. This covers all five
determinants, including the full Fact Target requirements and exact semantic dependencies, under the owning
Lowering law. Source-only import spelling, provenance, physical layout, compiler partition, and generated
helpers do not determine equality. Equal Definition meanings do not merge Candidate References, Authorities,
Versions, Established Definitions, or IDL Binding occurrences.

Section 11.3 defines narrower Mapping observations. Each must be complete for its stated extent under
ADR-0071, not for the entire Definition. Equal Mapping Surface does not establish equal Formation Meaning;
equal selected Mapping Meaning does not establish equal complete Surface or Definition Meaning. An
unobserved, unavailable, or omitted Projection does not establish semantic absence. Neither narrow
observation requires a separate primary Mapping Reference or a particular physical selector.

Every observation must be coherent in its applicable HIR snapshot and comply with ADR-0071 visibility,
lifetime, and semantic equality rules.

Implementation may compute observations on demand, share immutable backing, or persist and validate them for
reuse. None of these choices creates another IR, Authority, or Contract Judgment. V2 may compare narrow
observations for local reuse or early cutoff only after checking current references and dependency validity.
Such comparison never replaces complete Definition Establishment or the clean recomputation reference path.
Query architecture, cache granularity, physical Mapping layout, and incremental update algorithms remain
open.

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

Section 11.1 governs identification of the exact Fact Kind and coordinate through Fact-owned HIR references
and legal observation. Operation parameters, host types, vocabulary indices, and generated addresses do not
identify the Target.

The selected observation retains all Mapping occurrences for that Target. For each occurrence, it preserves:

- the exact nominal Source Requirement and Source/Target relation;
- the qualified callable application, including receiver and argument placement and meaning-determining constants;
- completed conversion applicability and meaning;
- Lowering-owned partiality/refusal meaning;
- exact semantic dependencies.

External meanings remain direct references or legal observations of their respective producers. Lowering
does not repeat Resolver, Type Engine, or library semantic interpretation judgments.

Before Definition Judgment, a target-qualified selection may lawfully observe zero, one, or several declared
Mapping occurrences. Zero establishes absence only if the exact Target is legally referenceable and its
complete Mapping observation is available in the coherent HIR snapshot. An unknown Target, incomplete
Surface, inaccessible Projection, or failed reference resolution does not establish absence.

The complete Fact-owned target-coordinate surface remains independently necessary to prove that every
required coordinate has exactly one Mapping. Under Section 8, two or more declarations for the same exact
Fact-kind coordinate violate the V1 law, whether their Sources or Formation Meanings differ or coincide.
Identical declarations still count separately; independent mappings from one Source Requirement to different
Targets remain lawful.

Observation neither establishes legality nor repairs invalid declarations. It may not merge duplicates,
select an arbitrary winner, or infer a missing mapping. Complete but unlawful duplicate or
incomplete-coverage declarations are refused by the owning Definition Judgment (Section 11.5).

A legal consumer may use an exact Source Requirement as a reverse observation index to discover its uses.
Under ADR-0071, that index only reorganizes the existing relation: it creates neither a primary Mapping
subject nor new authority or a Source uniqueness restriction.

Persistent or primary Mapping References are not required, but the producer must preserve exact multiplicity
even for equal Mapping Meanings. A transient selector or diagnostic provenance may distinguish physical
occurrences without giving them Contract identity. Declaration order, source locations, and storage indices
do not determine Mapping Meaning; deterministic producer-selected observation order does not become semantic
precedence.

The two projections have different equality scopes:

| Projection               | Equality covers                                                             | Equality does not establish                                                             |
|--------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| Complete Mapping Surface | The whole declared Source/Target relation and multiplicity                  | Qualified Formation Meaning of each mapping                                             |
| Selected Mapping Meaning | The exact Target's complete multiset of Mapping Meanings and multiplicities | Other Targets' mappings, the complete Target Fact surface, or entire Definition Meaning |

Neither equality implies Candidate Reference equality, authoritative Definition identity, Establishment
success, or current physical reuse eligibility. For example, unchanged Mapping projections do not discharge
complete target coverage when a referenced Fact Kind's surface or another semantic dependency changes.

ADR-0071 governs current references, snapshot coherence, visibility, completeness, Protocol compatibility,
and equality. V1 may materialize and recompute the entire relation; V2 may reuse complete observations or
valid derived indexes after checking current dependencies. No Query Graph, persistent Mapping ID, cache-key
design, storage layout, enumeration order, or delta algorithm is mandated. Narrow observations never replace
the complete Definition Candidate Projection (Section 11.2).

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

Preserve the selecting Interface or Interaction context where the existing IDL/HIR reference law needs it to
identify the exact selection. Structural reachability from the Operation does not by itself require a
duplicate context.

Several Operations may select the same Definition. Each has a distinct Binding Candidate without copying,
changing, or merging its Source Requirements, fixed Fact targets, Mapping relations, or Formation Meanings.
Multiple fixed Fact kinds within one Definition do not themselves create separate Binding Candidates.

The **Lowering Role Selection observation** is read from the selection-owning IDL/HIR Operation subject at its
exact Lowering role coordinate. In valid Visible HIR the complete observation for that role is exactly one of:

```text
Selected: exact LoweringIDLBindingCandidateRef
Explicit Absence: no LoweringIDLBindingCandidateRef exists for that selection
```

Explicit Absence is an observation of non-selection at the owning role, not a fictitious Binding Candidate.
Separate Contract or Composition law determines whether non-selection is permitted. None of the following is
a valid alternative selection state or may be silently treated as Explicit Absence:

- a missing or unresolved selection;
- competing selections for the same role;
- inability to observe the current Projection.

Unresolved or conflicting selections cannot be published as completed Role Selections in Visible HIR.
Frontend resolution or HIR Seal retains its own unsuccessful result under the common laws.

Completeness of an actual Binding Candidate requires its selecting occurrence, role, required qualifiers,
and exact Definition Candidate target to be resolved and referenceable under current HIR visibility and
coherence rules. Completeness does not prove Definition legality, Establishment, or lawful use in the
current Operation context. For example, a fully resolved Binding may target a complete Definition Candidate
whose duplicate Mapping is later refused by the owning Lowering Definition Judgment.

Binding Establishment remains separately owned. So do any applicable compatibility judgments concerning the
Interface, Input, Canonicalization, Fact vocabulary, Operation participation, Version, Governance, or
Contract World.

Only exact qualifiers independently owned by this IDL selection belong to Binding Candidate meaning. A later
legality judgment may need the following without making them part of the Binding Projection:

- the selecting Operation signature and current Input or Canonicalization Definition or occurrence;
- the Interface Fact vocabulary, runtime values, or present World;
- the selected Definition's payload.

Neither World selection nor a current Binding may silently substitute a different Lowering Definition,
Version, Fact target, or qualified Formation Meaning.

The following remain independent: exact Binding Candidate Reference equality, complete Binding
selection-meaning equality, complete Lowering Definition meaning equality, authoritative Binding identity,
and physical reuse eligibility.

Another Operation role or the current Interface vocabulary may change without affecting the Lowering IDL
selection observation, yet require a separate Composition or application judgment to be reconsidered. V2 may
exploit that unchanged narrow observation only with current semantic dependencies, correct ownership, and
ADR-0071 snapshot/Protocol-validity checks. Local Binding equality does not establish successful
Establishment or current use legality.

### 11.5. HIR Seal, Early Static Verification, and Legal Establishment Entry

ADR-0071 governs HIR Seal, visibility, lifecycle, and Protocol conformance for Sections 11.1–11.4. Seal
verifies that pre-authority Candidate meaning is complete, resolved, and coherent. It is neither a Lowering
Definition, Binding, or Fact Judgment nor a new Contract Authority.

The compiler may detect static Contract violations as soon as the required resolved meaning and
authoritative prerequisites are lawfully available. Early physical execution neither transfers the owning
law nor relaxes legal Judgment entry conditions.

For a Lowering Definition Candidate, Seal requires:

- the exact owning Authority and Version candidate coordinate;
- complete nominal Source Requirements;
- all exact fixed Fact Kind candidate references and Fact-owned coordinate references;
- the complete declared Mapping relation, preserving multiplicity;
- completed qualified Formation Meaning and exact semantic dependencies;
- coherent legal observations of every required Projection.

Every direct reference must be resolved and referenceable in its producer's pre-authority HIR domain. Seal
does not require Fact Definition Establishment merely because it references exact Fact Candidate meaning.

Unknown or unsupported required meaning, incompatible Protocol observations, incomplete or dangling
references, poison/recovery placeholders, and mixed or invalid HIR snapshots cannot be silently sealed or
made Visible.

For an IDL Binding Candidate, Seal requires the exact operation-local selecting subject, Lowering role,
complete required qualifiers, and an exact referenceable Definition Candidate target. The selection-owning
subject must support a complete `Selected` or `Explicit Absence` observation. Missing or conflicting
selections cannot be normalized into successful Visible HIR selection.

Fact vocabulary membership and the current Operation's Input/Canonicalization compatibility remain separate
lawful-use judgments; Seal neither infers nor establishes them.

**Completeness is not legality.** A sealable pre-authority Candidate may contain a completely resolved declaration that
violates Lowering Definition law. This includes distinct Formation Meanings assigned to one Target, identical Mapping
declarations for that Target, or omission of formation for a required Fact coordinate in an otherwise complete
declaration.

HIR construction, Projection, and Seal must preserve those violations. They may not deduplicate, insert
implicit target defaults, select a winning Formation, change a Fact target, or invent missing relations. The
owning Lowering Definition Judgment must refuse complete but unlawful declarations; Seal does not make them
lawful or authorize successful code generation.

Unresolved Fact kinds or coordinates, nominal Source Requirements, or required qualified callable/semantic
interpretation cannot supply complete Visible HIR meaning. The distinction matters for failure attribution:

| Situation                                                                                                                                           | Owning result                                                |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| A compiler producer or entered Seal verifier detects failure of its own resolution or completeness obligation                                       | Its own unsuccessful compiler result.                        |
| A required producer fails before legal Seal entry                                                                                                   | No synthetic HIR Seal rejection.                             |
| A separately owned authoritative prerequisite fails before Lowering Definition Judgment entry                                                       | Its own result; no fabricated Lowering Definition Refusal.   |
| An entered Lowering Definition Judgment detects duplicate formation, incomplete Target coverage, or another Lowering-owned Definition-law violation | Definition Refusal, not a warning followed by Establishment. |

Legal Definition Judgment entry requires both a complete Resolved Definition Candidate available through the
valid Visible HIR Candidate Protocol and every authoritative Definition-time Required Basis in Section 11.6
to be complete under ADR-0063.

The current Lowering IDL Binding, selected Input or Canonicalization Occurrence, Operation parameter
surface, and application World do not automatically become Definition-time prerequisites merely through
later selection. An HIR Candidate Reference cannot satisfy an Established Fact Definition requirement.

Early static verification may execute the owning Definition Judgment during frontend processing when its
lawful inputs are ready. This includes immediate compile-time refusal of fully resolved duplicate or
incomplete Target formation. It requires neither a separate frontend pass nor a second duplicate check or
physically distinct HIR-builder, Seal, and Judgment execution.

Pass fusion, parallelism, and V2 incremental work must preserve the distinct legal meanings of resolution
failure, HIR Seal failure, Lowering Definition Refusal, and current Binding/Composition incompatibility.
Each is attributed to its owning law; no result is invented for a dependent judgment that never legally
entered.

V1 clean recomputation and V2 incremental, parallel, lazy, or reused observations have identical Seal and
Establishment entry obligations. Neither an earlier Generation's successful Seal nor a cached selected
Mapping Meaning certifies the current HIR Candidate or Fact Target Coverage. Every reused observation and
prerequisite must be current, compatible, complete for its declared extent, and coherent under ADR-0071. If
safe reuse cannot be established, the compiler must follow an applicable legal re-resolution or
deterministic clean recomputation path.

Execution schedules, physical HIR storage, validator placement, and incremental algorithms remain open.

### 11.6. Definition-Time Required Basis and Established Definition Direct Relations

For each distinct fixed Core Fact Kind, the Definition requires its exact Established Fact Definition
Meaning. The Kind and applicable Version determine one mandatory Definition-time Required Basis coordinate,
regardless of how many mappings target it.

Exactly one Applicable Established Fact Definition must satisfy that requirement, with no permitted absence.
No other Kind or Version may satisfy it, even with an equal factual surface. An HIR Candidate Reference does
not establish this Basis; a resolved reference or successful compiler lookup alone cannot satisfy it. Each
Required Basis uses the exact source domain determined by its owning law. Exact Basis Binding,
Applicability, and Complete Basis follow ADR-0063 Sections 7 and 8. An incomplete prerequisite prevents
lawful Definition Establishment entry; it does not create a Lowering Definition Refusal.

A separately owned Contract Law adds a Definition-time Required Basis only when its owning law requires
prior authoritative Establishment for Lowering. A refinement or bound stated completely within the
Definition needs no such external Basis. Resolved callable, Type Engine, and qualified library
interpretation remain compiler-owned inputs to complete HIR, not Contract Required Basis merely because
Formation Meaning consumes them. Lowering's own Version Binding follows ADR-0063 and ADR-0053 without
introducing another universal Basis.

A successfully Established Lowering Definition preserves a direct Established relation to the exact
Established Fact Definition of each fixed target Kind. Mapping targets retain their Fact-owned coordinate
meanings. When a separately Established Contract Law determines Lowering Definition meaning, it is directly
referenced as well.

These relations retain the source Authorities without copying or re-establishing their Definitions. They
neither duplicate Basis Binding or its internal representation nor create independent Mapping Authorities.
Current Operation Binding and compiler realization material do not thereby enter Definition Meaning. Section
11 and ADR-0063 govern the complete immutable Definition Meaning and authoritative identity.

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

A selected Lowering declaration creates no user implementation interface. The backend directly realizes the
obligations Kontrakt derives from the selected Contract to form the complete candidate set for its fixed
Core Fact targets. Operation parameters are supplied separately under their applicable Fact participation
and realization obligations.

Possible internal realizations include:

- generated functions or methods, tables, parsers, constructor paths, and value builders;
- direct field or array access, primitive slabs, stack locals, registers, and other internal representations.

These replaceable structures neither become retained user-facing Contract surfaces nor select Lowering
meaning or own Fact authority. Selecting a Lowering implementation does not require runtime classpath
scanning, reflection, service discovery, coordinate-name search, type-pair lookup, or DI resolution.

The actual user Operation implementation is bound separately under ADR-0046 and admitted as User-System
Realization under ADR-0070. The pipeline may invoke it only after the required candidate and independently
owned establishment judgments succeed and its Fact parameter requirements are lawfully satisfied.

Internal method decomposition, helper ABI, carrier materialization, refusal encoding, and JVM lowering
remain compiler/backend decisions. Any realization must preserve the explicit relation and the absence of
hidden implementation authority.

---

## 13. Compiler-Derived Material

From the established Lowering Definition and its resolved Operation Binding, including the qualified callable meaning
supplied by the frontend, the compiler may derive:

- **Formation:** semantic `LoweringPlan`, source-read closure, target Fact-coordinate write closure,
  representation-refinement operations, and a complete candidate-set formation plan for each fixed Fact kind.
- **Judgment and verification:** judgment and establishment handoff, verification obligations, automatic tests, and
  diagnostic mapping.
- **Realization:** cache dependencies, optimization opportunities, and backend formation material.

All of these are compiler-derived material, not another authored Lowering Contract. The compiler may choose
how to execute an already-established refinement, but it cannot create one whose meaning is absent from the
Contract.

---

## 14. Judgment and Establishment Boundary

One Lowering Occurrence applies the Established Lowering Definition to the lawful source Presentation of one
exact inbound application. Equal source values from distinct Input Occurrences do not merge those applications,
and physical re-evaluation of the same application does not create a new Occurrence.

The generated machine verifies declared Mapping coverage, every exact target Fact surface, and completion of
the entire candidate set. Formation is all-or-nothing: any required mapping or target formation failure
prevents successful completion. No partial set is passed as a successful result.

Successful value formation alone establishes neither Fact authority nor lawful Core entry or Operation
handoff. Applicable Fact, standing Invariant, and movement authorities must perform their independently
owned judgments. Only after every required judgment succeeds may Fact authority be established and the
separate Operation Fact participation obligations be satisfied for lawful invocation, when applicable.
Atomic establishment, rollback, and publication mechanics remain outside this ADR.

The generated pipeline must preserve at least these obligations:

**Formation and completeness**

- Every coordinate of every fixed target Fact kind is formed exactly once by one explicit mapping from one Input
  coordinate.
- Every candidate conforms to its fixed Fact kind and entire declared factual surface.
- Source reuse occurs only through separately declared lawful mappings.
- All target candidates form one complete occurrence result, or the occurrence is refused.
- Every required representation refinement follows the established Lowering law.

**Fact representation**

- Material is immutable; no mutable external alias survives into Fact authority.
- Presence, alternatives, relations, and ordering are explicit wherever declared by the Fact surface.
- No back-reference to external presentation or backend realization machinery remains.

**Independent judgments**

- Applicable schema, Version, Governance, Budget, and Capacity judgments succeed under their own authority.
- Every applicable interface-level Invariant holds for the candidate Fact kind.
- Every applicable movement judgment succeeds.

Only after those obligations succeed may the machine establish the corresponding Fact authority and, if the selected
Operation requires them, provide lawfully participating established Fact values through its separately declared
Operation realization parameters. Lowering targets need not coincide with every Operation Fact parameter.

Allocation, constructor or builder completion, parsing, and backend materialization alone cannot create Fact
authority. The backend may nevertheless use temporary mutable builders, scratch buffers, ordinary libraries,
offset tables, sorting workspaces, or staged regions if they preserve the established Lowering law. These
implementation objects must never be exposed as established Facts.

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

Runtime refusal differs from incomplete Definition meaning. An incomplete Contract is rejected before
executable realization; a semantically complete Contract may still specify an actual-value refusal
condition. For example, the presented value may not admit the declared representation under the exact
Lowering law.

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

Input refusal, Admission rejection, Canonicalization refusal, Invariant refusal, and Budget or Capacity
results remain distinct from Lowering refusal. So do subsequent Fact and movement refusals. They retain
their own authority and attribution. An incomplete target is never partially established Fact material, and
formation of some candidates cannot authorize establishment or publication of that subset after a failed
occurrence.

| Result                 | Meaning                                                                                                            |
|------------------------|--------------------------------------------------------------------------------------------------------------------|
| Lowering refusal       | The full declared set of complete target Fact candidates cannot be formed under Lowering law.                      |
| User Operation failure | Established input Facts exist, but admitted user Operation realization does not complete with its declared result. |
| Invariant refusal      | Complete candidate Fact material exists, but the standing law for that exact Fact kind does not hold.              |
| Publication refusal    | The Operation result Fact exists, but its declared outward exposure is not permitted.                              |

The machine must preserve exact Failure and diagnostic attribution to the owning authority.

---

## 16. V1 Optimization Boundary

Kontrakt owns the Lowering Contract and the realization obligations derived in Sections 11 and 13. Since no
user Lowering implementation call must be preserved, the backend may optimize generated formation directly,
provided it preserves the established Contract.

Possible realizations include:

- Canonicalization-Lowering orchestration fusion
- intermediate presentation elimination
- dead unused source-coordinate elimination
- direct source access and direct target writes
- exact buffer and region sizing
- single-allocation or bounded-allocation formation
- primitive and finite-alternative specialization
- packed relation and presence layouts
- structural cache keys from the exact declared read set
- specialized parsing or decoding for an established representation law
- carrier construction only at the host ABI boundary
- direct binding of lawfully established and participating Fact values at the generated Operation ABI boundary
- AOT-generated completion, judgment, establishment, and invocation paths

If the generated host Operation ABI requires a carrier instance, the backend must materialize that actual
value before invocation. An intermediate carrier may be erased if it is not host-observable and removal
changes neither the ABI nor Contract meaning.

Optimization cannot authorize same-name mapping, raw type-pair conversion inference, undeclared coordinate
synthesis, or business computation within Lowering. Lowering authority ends before user core realization;
ADR-0070 separately governs analysis and optimization of the admitted Operation after lawful handoff.

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

The Operation receives ordinary generated or declared host parameter values, with separate lawful Fact
participation. It does not receive:

- the Input presentation or canonical presentation object;
- the Lowering declaration, source-to-target relation table, `LoweringPlan`, or backend helper metadata;
- candidate or established-Fact wrappers, staging objects, or host-language execution context.

At handoff, the candidate set was complete before the required standing and movement judgments. Those
judgments have succeeded, and external-presentation authority has ended. Fact authority has been established
outside the host value representation by its owning law. Result and Publication are not yet established or
authorized.

The Operation may produce its ordinary declared result candidate.

Result-side Fact establishment, result-side movement, Publication, and Output Presentation belong to later authorities.

The core must not reopen the erased Input or Lowering declaration as sources of factual meaning.

---

## 18. Open in This Section

The following design decisions remain open or belong to separate work:

| Area                 | Open scope                                                                                                                                                      |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Authoring            | Exact Fact-qualified target spelling; supported mapping-method inventory; additional authoring, if needed, for nontrivial one-to-one representation refinement. |
| Compiler realization | Internal Lowering representation, physical Mapping selector encoding, concrete frontend execution architecture.                                                 |
| Backend realization  | Helper decomposition, JVM ABI, carrier materialization, allocation strategy, and encoding of declared Lowering refusal.                                         |
| Other authority      | Fact-owned HIR Protocol internals, Binding Establishment, and detailed refusal Material and Failure observations.                                               |

The DSL remains `source -> target : method`, with explicit callable references and arguments under Section

7. No open decision may weaken the established law. In particular, the single immutable Definition selected
   through an operation-local Binding, explicit one-to-one mappings with source reuse, fixed Core Fact targets,
   complete candidate sets, independent judgments and establishment handoff, and the prohibitions on runtime
   implementation discovery, user-supplied implementations, and implicit conversion authority remain in force.

The defined semantic scope is:

- Sections 11.1–11.5: HIR Candidate References, complete Definition Candidate Projection, Mapping Surface and
  target-qualified Selected Mapping Meaning, operation-local Binding Candidate and Role Selection observations, and the
  HIR Seal/Definition Judgment entry boundary. These definitions do not themselves establish any successful Contract
  Establishment result.
- Sections 11, 11.6, and 11.7: Established Definition meaning, Definition-time Required Basis, direct Established
  relations, and Established Definition observations.
- Sections 14.1–14.3: Occurrence Basis Requirement Law, whole outcome, successful Established Occurrence Material, and
  Occurrence observations. Detailed refusal Material and Failure observations remain for the Failure design.

The exact atomic Fact establishment, rollback, and publication mechanics remain outside this ADR, as does
the complete authoring surface for additional Fact participation, Operation Result Material, result-side
change formation, Publication, and Output Presentation.

---

## 19. Consequences

Lowering becomes an explicit representation-boundary Contract rather than a mapper callback. External and
core vocabularies can remain independent because every Contract-visible relation is authored directly. Fixed
Core Fact targets belong to the Definition, rather than being derived from Operation parameters, while the
selected Operation remains the center of the IDL pipeline Binding.

The closed relation gives the compiler a basis for verification, diagnostics, tests, caching, candidate
completion, and backend formation. It also gives the backend freedom to realize the established relation
without retaining a user implementation port. The user implements the Operation business algorithm, not
Lowering.

This shifts cost into Contract expressiveness and compiler/backend responsibility. Kontrakt must reject
incomplete meaning, support the declared representation law on the selected backend, and preserve refusal
and establishment boundaries. V1 deliberately rules out fan-out in a single mapping, multi-source coordinate
derivation, implicit conversion inference, and hidden callbacks because they obscure Contract meaning. The
permitted source reuse and multiple complete Fact targets in Section 8 preserve explicitness without
requiring a single target kind.

---