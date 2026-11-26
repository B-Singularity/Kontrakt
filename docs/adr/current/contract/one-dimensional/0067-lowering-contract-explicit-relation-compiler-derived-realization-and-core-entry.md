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
- ADR-0068: Fact Contract, Explicit Immutable Core Information, Sameness, and Vocabulary
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
the compiler/backend needs to form actual values. The user supplies no Lowering implementation, including through a
generated interface or callback.

Operation remains the ordinary user-supplied realization boundary. The backend forms complete candidates for
the Core-owned Fact kinds fixed by Lowering; the selected Operation has its own IDL Binding and applicable
Core judgments for Fact parameter participation. An Operation parameter is not a Lowering target.

Internal helpers remain permissible under Section 12. The amendment leaves the existing semantic laws in Sections 5–10
and the judgment and establishment boundaries in Sections 14–15 unchanged.

---

## 1. Context

Lowering is the final representation boundary of the inbound airlock. Input, Admission, and selected
Canonicalization operate over the Contract-visible external presentation. Admission may permit continuation,
and Canonicalization may establish a stable same-shape representative. Neither ends external-presentation
authority.

Lowering declares how selected boundary coordinates may form candidates for one or more explicitly fixed
Core-owned Fact kinds. This complete permitted relation is independent of the selecting Operation's
parameter surface. It is Contract expression, not a request for user-written representation-formation code.

Kontrakt resolves the Definition under Section 11 and qualifies its operation-local IDL Binding separately. From the
lawfully established meaning, the compiler derives realization obligations (Sections 12–13); the backend performs value
formation.

The generated machine preserves completeness, refusal routing and lawful Operation handoff under Sections 14–17.
Lowering does not acquire the authority of any subsequent judgment.

---

## 2. Problem

An arbitrary mapper hides the formation relation and the conditions for trusting its result in implementation code. A
generated mapper interface merely moves that authority into a callback selected at assembly time.

Compiler inference poses the same problem. Conversion authority must be explicit (Sections 7 and 9); framework
conventions cannot supply it.

Lowering therefore separates one declarative Contract surface from a replaceable compiler/backend
realization. The Contract owns the permitted relation and every semantic refinement needed to complete it;
realization applies that established meaning to actual runtime values.

If the meaning is insufficient to determine a lawful realization, Kontrakt must reject the Definition or the
Contract language must gain an explicit way to express the missing meaning. User implementation cannot
silently fill the gap.

---

## 3. Decision Drivers

The design rests on three constraints:

- **Contract scope.** Lowering contains only immutable Contract material. Its authority stops at representation
  formation (Section 10).
- **Explicit relation.** Each mapping has one source and one target. V1 permits reuse through distinct mappings. The
  relation must be explicit, and each fixed Fact kind defines its own factual surface (Sections 6–9).
- **Realization freedom.** The compiler may derive an implementation from the established relation (Sections 12–13). The
  backend may choose any strategy that preserves the relation and its representation law. Implementation convenience
  cannot supply missing Contract meaning.

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

Lowering introduces none of the following separate surfaces:

- `CoreMaterial`
- `OperationStart`
- `LoweringTarget`
- A user-visible candidate schema

Each Lowering Definition explicitly fixes one or more exact Core-owned Fact kinds and the factual
coordinates it forms for each. Their Definition meaning is determined independently in the Core. None of the boundary
contracts below produces it through compilation:

- Input
- Lowering
- Output
- Publication

A successful occurrence forms exactly one
complete candidate of each fixed kind.

Two separate relations qualify use of the Definition:

- The enclosing interface's `facts` vocabulary neither creates nor reselects its targets.
- The selected Operation supplies the IDL Binding context and declares its Fact parameters independently. Neither its
  parameter list nor its current signature determines or silently changes those targets.

Targets are Fact-owned addresses, not Operation parameters. The Operation manifest neither repeats the Fact
vocabulary nor gains a `fact` or `invariant` slot merely for Lowering.

---

## 6. Target Fact Independence

The Fact surface must be intelligible from its own declared material:

- Coordinates and sorts
- Presence and alternatives
- Relations and ordering
- Bounds
- Schema
- Version

The Fact surface must remain independent of the subjects listed below. It must not name or import them. Neither
embedding nor extension is permitted, and no reference to them may be retained:

- the Input declaration;
- the source DTO;
- the transport protocol;
- a host object;
- a framework context;
- an adapter type;
- the Lowering declaration or source-coordinate handle.

The backend may read source values and materialize ordinary target values. Host-language representation and
implementation machinery confer no factual authority.

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
    facts {
        DepositCommand
        DepositRecorded
    }
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
unresolved applications are rejected. Resolution and interpretation remain with their respective producers, not
Lowering:

- Resolver
- Type Engine
- Library semantic interpretation

The relation body contains only Lowering-owned Contract material. It does not repeat any of the following:

- Input carriers
- Operation parameters
- Fact declarations
- Invariant declarations

Joint formation of several distinct Fact
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

These cardinalities constrain Contract meaning, not the physical storage of the values (Section 10).

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

Conversion authority cannot be inferred from any of the following:

- A raw host-type pair
- A compatible catalog entry
- A library’s mere presence
- Naming conventions
- Classpath contents
- Backend support

Once the established Contract and qualified callable interpretation completely determine a permitted representation
refinement, the compiler may realize it using any supported operation that preserves it. Incomplete meaning must cause
compilation to fail rather than trigger a request for a user mapper.

Lowering realization must not perform:

- repository lookup or environmental resolution;
- business computation or undeclared capability access;
- Invariant judgment
- State movement
- Publication.

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

- decode a value;
- parse a value;
- range-check a value;
- copy a value;
- freeze an approved bounded leaf;
- make an already-declared presence distinction explicit;
- select a fixed internal representation.

Parsing support alone does not give a generic text coordinate business meaning.

Section 8 prohibits fan-out and multi-source formation within a mapping. The restriction is semantic: a
backend may decompose one Fact coordinate into several machine words or pack several coordinates into one
region. Reuse of a source through separate lawful mappings is not a single `1:N` mapping.

Lowering must not:

- create a new business proposition;
- execute core computation;
- produce Operation Result Material;
- consult mutable current machine state or apply business Policy;
- decide legal movement;
- evaluate an Invariant;
- select a Transition;
- grant Publication authority;
- infer missing material;
- combine source coordinates within one mapping;
- split one mapping into several Contract-visible meanings.

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

Cross-cutting judgments retain their own authority and attribution (Section 14).

A declared reference may cross Lowering only as one explicit source coordinate refined into one explicit target
coordinate of the same declared reference meaning. Reference resolution belongs elsewhere and must not be disguised as
formation. This applies to resolution against:

- Another coordinate
- A mutable registry
- Current core state

---

## 11. Definition-Time Completeness and Identity

A Lowering Definition Candidate owns the complete resolved meaning of its explicit one-to-one-per-mapping formation
relations for every fixed target Fact kind.
Its Definition determinants are:

**Source Requirement**

Defines each declared nominal source coordinate and the Input-visible presentation meaning it requires.

**Target Requirement**

Identifies each explicitly fixed Core-owned Fact Kind at its exact Version. This requirement includes the complete
factual coordinate surface, applicable Fact meaning and required representation meaning.

**Mapping Relation**

Defines the complete relation from declared source coordinates to the factual coordinates of every fixed target Fact
Kind. It preserves every mapping occurrence under the cardinality law of Section 8, including its declared multiplicity
before judgment.

**Formation Meaning**

Determines how each declared mapping forms its target value through an exact qualified callable. Its meaning includes:

- Explicit placement of the source as a receiver or argument.
- Arguments that determine the conversion meaning.
- Completed conversion meaning and applicability.
- Lowering-owned partiality and refusal meaning.

**Semantic Dependencies**

Identifies the exact target Fact meaning and Version. It also preserves references to any Contract-owned refinement,
bounds, Law or Basis that determines the declared formation meaning.

All five determinants must be resolved before the Candidate becomes Visible HIR. Establishment cannot reconstruct
missing Definition meaning from any of these sources:

- The IDL Binding Candidate
- The current Operation
- Source syntax
- Backend behavior

Frontend resolution must retain every resolved mapping occurrence, even when its
multiplicity violates Lowering law; it must not silently repair or discard mappings. Unresolved meaning
cannot enter Visible HIR.

The declared relation and its multiplicity, including repeated identical mappings, determine meaning.
Neither identity nor equality depends on:

- Independent mapping order
- Source provenance
- Physical mapping indices

Explicit order remains governed by its owning law, whether it belongs to a Fact coordinate or a callable argument
application. The same holds for order under any other separately owned law.

A nominal Source Requirement does not incorporate the currently selected Input Definition. The Target
Requirement, by contrast, fixes every exact Core Fact kind and its required factual coordinates
independently of the current Operation Parameter Reference. Those Fact meanings and their applicable
versions remain Definition dependencies. Current Input and Operation selections must satisfy their own Binding and
application requirements. They cannot introduce an unrecorded alternative to any of the following:

- Target
- Callable
- Formation Meaning

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
- finite bounds on:
    - depth
    - cardinality
    - intermediate storage
    - output
    - work
- applicable schema material
- applicable Version material
- applicable Governance material
- Lowering-owned refusal meaning and separately owned cross-contract stop attribution

Every factual coordinate of every fixed target Fact candidate must be formed exactly once under the complete flat
Lowering Definition. Each target Fact kind must have one complete candidate; no target may be left partial.

An Input coordinate may appear in several distinct, explicit mappings. An Input coordinate may also remain unused.
Neither case implicitly creates a relation or requires an Operation parameter Fact surface to reproduce external
material.

Implicit formation is prohibited. This includes:

- Silent target defaults
- Fallback constructors
- Same-name auto-mapping
- Same-type auto-copying

Nor may any of the following supply the relation:

- Catalog selection
- Structural guessing
- Package scanning
- Annotations
- Assignability
- Inheritance
- Implementation-shape discovery

A Lowering Definition cannot be established as lawful if it contains any of the following:

- missing or duplicate formation of a target Fact coordinate;
- multiple targets in one mapping or a target requiring combined runtime source coordinates;
- hidden absence;
- recursive host-object traversal;
- unbounded collection work;
- environment-dependent Contract meaning;
- executable user material inside the relation body;
- representation refinement not closed by the declared Contract and qualified callable interpretation.

Fully resolved duplicate mappings must remain visible to the owning Lowering judgment. Repeated source
coordinates in distinct explicit mappings are not a rejection reason.

By contrast, the following cannot supply complete Visible HIR meaning:

- Unresolved nominal Source Requirements
- Unresolved or falsely attributed Fact kinds or coordinates
- Incomplete or unsuccessful qualified conversion resolution

The owning compiler subsystems remain responsible for:

- Exact callable resolution
- Source/target sort applicability
- Qualified Java/Kotlin library interpretation

Lowering consumes the completed
meaning without rejudging it.

A fixed Fact kind absent from the enclosing interface's current vocabulary is a Binding/Composition
compatibility defect, not a missing or alternate Definition target. Section 11.5 distinguishes HIR Seal from
Definition Refusal: fully resolved target multiplicity and coverage defects are Definition-law violations
rather than missing HIR meaning.

Because the user supplies no Lowering implementation, there is no missing-Lowering-implementation link
failure. A backend that cannot realize a semantically complete Contract with the required guarantees must
reject that realization; its inability does not change the Contract.

The established Lowering Definition is one flat, immutable, adapter-erased Contract material. It preserves the complete
meaning of all five determinants above and the Occurrence Basis Requirement Law in Section 14.1.

One independently declared Lowering Contract denotes one Lowering Authority. That Authority and one exact
Contract Version identify one complete Definition under ADR-0063 and ADR-0053. Conflicting resolved meanings
cannot be established for the same Authority and Version; equal meanings under different Authorities or
Versions do not merge their identities. Import spelling and source location do not change Definition
meaning.

Selection by an Operation creates a separate Binding, preserving the exact Lowering Definition and selected
Operation References. Before use, that Binding must resolve:

- The selected Input
- Canonicalization, when selected
- The Operation signature
- The applicable flow-world basis

Fact-parameter participation remains separate from the
Definition's fixed targets and target coverage.

Contextual resolutions do not become Definition determinants merely through selection. If an exact basis
determines the Definition's own meaning, however, ADR-0063 requires that dependency to remain with the
Definition.

CRI may prepare exact compiler-time source and target correspondences from legal producer projections. It
neither establishes Binding validity nor invents mappings or forms runtime source values. A Binding mismatch
must be rejected rather than repaired by changing the declared relation or qualified callable meaning.
Definition and Binding identities remain distinct even when only one Operation uses the Definition.

Contract meaning is independent of physical implementation and host object identity.

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

`LoweringDefinitionCandidateRef` identifies one exact Definition Candidate that is current and resolved. The
independently declared Definition and its applicable Version determine the nominal identity scope within the
owning Lowering Authority. The resolved Version candidate coordinate is scoped to that Authority. If several
independent Definitions ever need to be distinguished within one Authority and Version, the authority-local
coordinate law of ADR-0063 applies. The following cannot supply that distinction:

- Source order
- Host symbols
- IDL selections
- Physical indices

This is a pre-authority reference, not an Established Definition Reference.

`LoweringIDLBindingCandidateRef` identifies one exact operation-local IDL selection occurrence. Existing IDL/HIR
reference laws govern observation of:

- The selecting Contract Interface/Interaction/Operation context
- The Lowering role
- The exact target Definition Candidate
- Any independently required Binding qualifier

Distinct Operation selections may target the same Definition Candidate without merging their Binding
Candidates or changing its Fact targets. Section 11.4 governs the exact owning-role distinction between
selection and Explicit Absence under ADR-0071.

The nominal Source Requirement belongs to the Lowering Definition Candidate, not the selected Input
Definition. Every required Source Coordinate must be exactly selectable within the owning resolved
Definition; every explicit Mapping occurrence must likewise remain observable with its exact multiplicity.
This requires neither a separate primary `SourceRequirementRef` nor a new primary Mapping Reference. No independent
Mapping Authority is required either.

Semantic selection and identity do not depend on:

- Source position
- Declaration order
- Array slots
- Generated helpers
- Generation-local handles

Section 11.3 defines target-qualified selection without prescribing a
physical selector.

A fixed Target Requirement consumes the exact pre-authority meaning of a separately Core-compiled Fact Kind
and its Fact-owned factual coordinates. The Fact HIR Protocol owns the reference. Lowering neither mints nor
reinterprets it. Fact owns its representation. The reference name and encoding remain Fact decisions. For example, Fact
may identify a
Kind directly or by an exact Fact Definition Candidate with a Fact-owned nominal Kind coordinate.

Neither the Kind nor its coordinates may be inferred from:

- Operation parameters
- The Input surface
- Interface vocabulary membership
- Host class names
- Physical addresses

Where complete coverage requires the full
Fact-owned surface, that surface must be legally observable without copying or recursively embedding the
Fact Definition in Lowering HIR.

Fact Definition compilation and authority belong to the Core (Sections 5–6). The Fact-to-Lowering reference expresses
semantic dependency rather than a mandatory serial pass schedule. The Fact HIR Candidate reference is distinct from an
Established Fact Definition Reference. Neither is a runtime Established Fact. HIR lookup does not establish the Fact
Definition or
grant its Required Basis; ADR-0063 and the later Lowering Establishment judgment govern that Basis.

The current Input Coordinate is observed through Input-owned `ResolvedInputCoordinateRef` where needed by a lawful
Binding or Composition judgment; it is not the Lowering Definition's nominal Source Requirement. Qualified callable and
library meaning remains observable through the responsible producers (Sections 7, 9 and 11). Lowering does not redefine
their reference forms, interpretation authority or resolution algorithms.

Keep the following notions distinct:

- HIR Candidate Reference equality
- Complete Definition Meaning equality
- Authoritative Definition identity
- IDL Binding equality
- Physical reuse eligibility

Candidate References are exact in their current
reference domain and cannot be inherited from an earlier HIR generation. None of the following confers semantic
Reference identity or Contract authority:

- A dense handle
- An HID
- A fingerprint
- A Query Key
- A serialized position
- A reuse artifact

### 11.2. Resolved Lowering Definition Candidate Projection

The Lowering Definition Candidate Projection is a read-only observation owned by its producer. It covers one exact
`LoweringDefinitionCandidateRef` and all five Definition determinants in this Section in full. It also exposes the
complete
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

A Projection expresses Lowering-owned meaning and exact direct references to separately owned subjects. Producer-owned
projections expose external meaning completely without redundant copies or recursive expansion.

The reusable Occurrence Basis Requirement Law is part of Definition meaning. Its instantiated requirements
and exact Basis Bindings belong to the application (Section 14.1). A later application may need the following without
making them part of Definition meaning:

- Current Operation selection
- Input or Canonicalization occurrences
- Runtime values
- Basis Bindings

Mapping observations preserve every declared occurrence, including exactly resolved duplicates. Section 11
governs which parts of the relation determine meaning. For an invalid but fully resolved mapping, the Projection must
not:

- Deduplicate it
- Overwrite it
- Supply a default
- Normalize it away

Nor does the Projection perform Definition Judgment or alter the resolution and Fact laws.

The complete Definition Projection governs comparison of the entire Definition meaning. This covers all five
determinants, including the full Fact Target requirements and exact semantic dependencies, under the owning
Lowering law. Equality does not depend on:

- Source-only import spelling
- Provenance
- Physical layout
- Compiler partition
- Generated helpers

Equal Definition meanings do not merge:

- Candidate References
- Authorities
- Versions
- Established Definitions
- IDL Binding occurrences

Section 11.3 defines narrower Mapping observations. Each must be complete for its stated extent under
ADR-0071, not for the entire Definition. Equal Mapping Surface does not establish equal Formation Meaning;
equal selected Mapping Meaning does not establish equal complete Surface or Definition Meaning. A Projection that has
not been observed does not establish semantic absence. Neither an unavailable nor an omitted Projection establishes it.
Neither narrow
observation requires a separate primary Mapping Reference or a particular physical selector.

ADR-0071 governs snapshot coherence, visibility, lifetime and semantic equality. Observation implementation remains open
(Section 18); it creates neither another IR nor Contract Authority or Judgment.

V2 may compare narrow observations for local reuse or early cutoff only after checking current references and dependency
validity. Such comparison never replaces complete Definition Establishment or the clean recomputation reference path.

### 11.3. Mapping Surface and Target-Qualified Mapping Meaning Projections

Lowering specializes the ADR-0071 Fine-Grained Semantic Projection law with two producer-owned observations over
one exact `LoweringDefinitionCandidateRef`:

```text
Lowering Mapping Surface Projection
Lowering Selected Mapping Meaning Projection
```

The **Lowering Mapping Surface Projection** exposes the complete declared Source Requirement-to-Fact-target relation
of that Definition. It preserves every declared Mapping occurrence, including:

- Source reuse
- Distinct Fact Kind ownership
- Repeated Source/Target pairs and their exact multiplicity

Each Target is qualified by the exact fixed
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
and legal observation. None of the following identifies the Target:

- Operation parameters
- Host types
- Vocabulary indices
- Generated addresses

The selected observation retains all Mapping occurrences for that Target. For each occurrence, it preserves:

- the exact nominal Source Requirement and Source/Target relation;
- the qualified callable application, including receiver and argument placement and meaning-determining constants;
- completed conversion applicability and meaning;
- Lowering-owned partiality/refusal meaning;
- exact semantic dependencies.

External meanings remain direct references or legal observations of their respective producers. Lowering consumes these
completed judgments without repeating them, whether they come from the Resolver or the Type Engine. It likewise does not
repeat library semantic interpretation judgments.

Before Definition Judgment, a target-qualified selection may lawfully observe zero, one, or several declared
Mapping occurrences. Zero establishes absence only if the exact Target is legally referenceable and its
complete Mapping observation is available in the coherent HIR snapshot. None of the following establishes absence:

- An unknown Target
- An incomplete Surface
- An inaccessible Projection
- Failed reference resolution

The complete Fact-owned target-coordinate surface remains independently necessary to prove that every
required coordinate has exactly one Mapping. Under Section 8, two or more declarations for the same exact
Fact-kind coordinate violate the V1 law, whether their Sources or Formation Meanings differ or coincide.
Identical declarations still count separately; independent mappings from one Source Requirement to different
Targets remain lawful.

Observation neither establishes legality nor repairs invalid declarations. It may not:

- Merge duplicates
- Select an arbitrary winner
- Infer a missing mapping

Complete but unlawful duplicate or
incomplete-coverage declarations are refused by the owning Definition Judgment (Section 11.5).

A legal consumer may use an exact Source Requirement as a reverse observation index to discover its uses.
Under ADR-0071, that index only reorganizes the existing relation: it creates neither a primary Mapping
subject nor new authority or a Source uniqueness restriction.

Persistent or primary Mapping References are not required, but the producer must preserve exact multiplicity
even for equal Mapping Meanings. A transient selector or diagnostic provenance may distinguish physical
occurrences without giving them Contract identity. Mapping Meaning does not depend on declaration order or source
locations. Storage indices do not determine it either; deterministic producer-selected observation order does not become
semantic
precedence.

The two projections have different equality scopes:

| Projection               | Equality covers                                                             | Equality does not establish                                                             |
|--------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| Complete Mapping Surface | The whole declared Source/Target relation and multiplicity                  | Qualified Formation Meaning of each mapping                                             |
| Selected Mapping Meaning | The exact Target's complete multiset of Mapping Meanings and multiplicities | Other Targets' mappings, the complete Target Fact surface, or entire Definition Meaning |

Neither equality implies:

- Candidate Reference equality
- Authoritative Definition identity
- Establishment success
- Current physical reuse eligibility

For example, unchanged Mapping projections do not discharge
complete target coverage when a referenced Fact Kind's surface or another semantic dependency changes.

The common observation laws of ADR-0071 apply. V1 may recompute the entire relation; V2 may reuse complete observations
or valid derived indexes after checking current dependencies. The Protocol prescribes neither an observation
implementation nor an incremental algorithm. Narrow observations never replace the complete Definition Candidate
Projection (Section 11.2).

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

Several Operations may select the same Definition. Each has a distinct Binding Candidate. Selection must neither copy
nor change nor merge any of the Definition’s following material:

- Source Requirements
- Fixed Fact targets
- Mapping relations
- Formation Meanings

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

An actual Binding Candidate is complete only if all of the following are resolved and referenceable under current HIR
visibility and coherence rules:

- Its selecting occurrence
- Its role
- Its required qualifiers
- Its exact Definition Candidate target

Completeness does not prove Definition legality or Establishment. Nor does it establish lawful use in the current
Operation context. For example, a fully resolved Binding may target a complete Definition Candidate
whose duplicate Mapping is later refused by the owning Lowering Definition Judgment.

Binding Establishment remains separately owned. Separate ownership also applies to any applicable compatibility
judgments concerning:

- The Interface
- Input
- Canonicalization
- Fact vocabulary
- Operation participation
- Version
- Governance
- Contract World

Only exact qualifiers independently owned by this IDL selection belong to Binding Candidate meaning. A later
legality judgment may need the following without making them part of the Binding Projection:

- the selecting Operation signature and current Input or Canonicalization Definition or occurrence;
- the Interface Fact vocabulary;
- runtime values;
- the present World;
- the selected Definition's payload.

Neither World selection nor a current Binding may silently substitute a different:

- Lowering Definition
- Version
- Fact target
- Qualified Formation Meaning

The following remain independent:

- Exact Binding Candidate Reference equality
- Complete Binding selection-meaning equality
- Complete Lowering Definition meaning equality
- Authoritative Binding identity
- Physical reuse eligibility

Another Operation role or the current Interface vocabulary may change without affecting the Lowering IDL
selection observation, yet require a separate Composition or application judgment to be reconsidered. V2 may exploit
that unchanged narrow observation only if its semantic dependencies are current and ownership is correct. The ADR-0071
snapshot/Protocol-validity checks must also hold. Local Binding equality does not establish successful
Establishment or current use legality.

### 11.5. HIR Seal, Early Static Verification, and Legal Establishment Entry

HIR Seal follows ADR-0071 for Sections 11.1–11.4. The same ADR governs visibility and lifecycle as well as Protocol
conformance. Seal verifies completeness of pre-authority Candidate meaning. The meaning must be resolved and coherent.
Seal creates no new Contract Authority and performs none of these judgments:

- Lowering Definition Judgment
- Binding Judgment
- Fact Judgment

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

The following cannot be silently sealed or made Visible:

- Unknown or unsupported required meaning
- Incompatible Protocol observations
- Incomplete or dangling references
- Poison/recovery placeholders
- Mixed or invalid HIR snapshots

For an IDL Binding Candidate, Seal requires:

- The exact operation-local selecting subject
- The Lowering role
- Complete required qualifiers
- An exact referenceable Definition Candidate target

The selection-owning
subject must support a complete `Selected` or `Explicit Absence` observation. Missing or conflicting
selections cannot be normalized into successful Visible HIR selection.

Fact vocabulary membership and the current Operation's Input/Canonicalization compatibility remain separate
lawful-use judgments; Seal neither infers nor establishes them.

**Completeness is not legality.** A sealable pre-authority Candidate may contain a completely resolved declaration that
violates Lowering Definition law. Examples of violations include:

- Distinct Formation Meanings assigned to one Target
- Identical Mapping declarations for that Target
- Omission of formation for a required Fact coordinate in an otherwise complete declaration

Those violations must survive HIR construction and remain observable through Projection and Seal. None may:

- Deduplicate declarations
- Insert implicit target defaults
- Select a winning Formation
- Change a Fact target
- Invent missing relations

The
owning Lowering Definition Judgment must refuse complete but unlawful declarations; Seal does not make them
lawful or authorize successful code generation.

Visible HIR meaning cannot be complete if any of the following remains unresolved:

- Fact kinds or coordinates
- Nominal Source Requirements
- Required qualified callable/semantic interpretation

The distinction matters for failure attribution:

| Situation                                                                                                                                           | Owning result                                                |
|-----------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|
| A compiler producer or entered Seal verifier detects failure of its own resolution or completeness obligation                                       | Its own unsuccessful compiler result.                        |
| A required producer fails before legal Seal entry                                                                                                   | No synthetic HIR Seal rejection.                             |
| A separately owned authoritative prerequisite fails before Lowering Definition Judgment entry                                                       | Its own result; no fabricated Lowering Definition Refusal.   |
| An entered Lowering Definition Judgment detects duplicate formation, incomplete Target coverage, or another Lowering-owned Definition-law violation | Definition Refusal, not a warning followed by Establishment. |

Legal Definition Judgment entry requires both a complete Resolved Definition Candidate available through the
valid Visible HIR Candidate Protocol and every authoritative Definition-time Required Basis in Section 11.6
to be complete under ADR-0063.

Later selection alone does not make any of the following a Definition-time prerequisite:

- The current Lowering IDL Binding
- The selected Input or Canonicalization Occurrence
- The Operation parameter surface
- The application World

An HIR Candidate Reference cannot satisfy an Established Fact Definition requirement.

Early static verification may execute the owning Definition Judgment during frontend processing when its
lawful inputs are ready. This includes immediate compile-time refusal of fully resolved duplicate or
incomplete Target formation. It requires neither a separate frontend pass nor a second duplicate check. There is no
requirement for physically distinct execution of HIR construction, Seal and Judgment.

Whether execution uses pass fusion or parallelism, or V2 performs incremental work, it must preserve the distinct legal
meanings of:

- Resolution failure
- HIR Seal failure
- Lowering Definition Refusal
- Current Binding/Composition incompatibility

Each is attributed to its owning law; no result is invented for a dependent judgment that never legally
entered.

Seal and Establishment entry obligations are identical for V1 clean recomputation and every V2 observation strategy.

Neither an earlier Generation's successful Seal nor a cached selected
Mapping Meaning certifies the current HIR Candidate or Fact Target Coverage. Every reused observation and prerequisite
must satisfy all of the following under ADR-0071:

- Current validity
- Compatibility
- Completeness for its declared extent
- Coherence

If
safe reuse cannot be established, the compiler must follow an applicable legal re-resolution or
deterministic clean recomputation path.

Physical realization of Seal remains open (Section 18).

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

A separately owned Contract Law adds a Definition-time Required Basis only when its owning law requires prior
authoritative Establishment for Lowering. A refinement or bound stated completely within the Definition needs no such
external Basis. Compiler-owned inputs to Formation Meaning (Section 9) are needed to complete HIR, but that dependency
alone does not make them Contract Required Basis. Lowering's own Version Binding follows ADR-0063 and ADR-0053 without
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

These observations grant no new Authority and follow ADR-0063 Section 11. They require no additional Mapping Reference
or particular physical Protocol representation.

---

## 12. Compiler and Backend Realization Boundary

A selected Lowering declaration creates no user implementation interface. The backend directly realizes the
obligations Kontrakt derives from the selected Contract to form the complete candidate set for its fixed
Core Fact targets. Operation parameters are supplied separately under their applicable Fact participation
and realization obligations.

Internal realization is replaceable. It creates no retained user-facing Contract surface, selects no Lowering meaning
and owns no Fact authority. Selecting a Lowering implementation requires no runtime discovery or lookup.

The actual user Operation implementation is bound separately under ADR-0046 and admitted as User-System
Realization under ADR-0070. The pipeline may invoke it only after the required candidate and independently
owned establishment judgments succeed and its Fact parameter requirements are lawfully satisfied.

Physical realization remains the compiler/backend's responsibility (Section 18). Any implementation must preserve the
explicit relation without introducing hidden implementation authority.

---

## 13. Compiler-Derived Material

From the established Lowering Definition and its resolved Operation Binding, including the qualified callable meaning
supplied by the frontend, the compiler may derive:

- **Formation**
    - Semantic `LoweringPlan`
    - Source-read closure
    - Target Fact-coordinate write closure
    - Representation-refinement operations
    - A complete candidate-set formation plan for each fixed Fact kind
- **Judgment and verification**
    - Judgment and establishment handoff
    - Verification obligations
    - Automatic tests
    - Diagnostic mapping
- **Realization**
    - Cache dependencies
    - Optimization opportunities
    - Backend formation material

All of these are compiler-derived material, not another authored Lowering Contract. The compiler may choose
how to execute an already-established refinement, but it cannot create one whose meaning is absent from the
Contract.

---

## 14. Judgment and Establishment Boundary

One Lowering Occurrence applies the Established Lowering Definition to the lawful source Presentation of one
exact inbound application. Equal source values from distinct Input Occurrences do not merge those applications,
and physical re-evaluation of the same application does not create a new Occurrence.

The generated machine verifies:

- Declared Mapping coverage
- Every exact target Fact surface
- Completion of the entire candidate set

Formation is all-or-nothing: any required mapping or target formation failure
prevents successful completion. No partial set is passed as a successful result.

Successful value formation alone establishes neither Fact authority nor lawful Core entry or Operation
handoff. The applicable authorities listed below must perform their independently owned judgments:

- Fact
- Standing Invariant
- Movement

Only after every required judgment succeeds may Fact authority be established and the
separate Operation Fact participation obligations be satisfied for lawful invocation, when applicable.
This ADR leaves atomic establishment and rollback mechanics to separate work. Publication mechanics are also outside its
scope.

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
- Wherever the Fact surface declares any of the following, it must be explicit:
    - Presence
    - Alternatives
    - Relations
    - Ordering
- No back-reference to external presentation or backend realization machinery remains.

**Independent judgments**

- Each applicable judgment below succeeds under its own authority:
    - Schema
    - Version
    - Governance
    - Budget
    - Capacity
- Every applicable interface-level Invariant holds for the candidate Fact kind.
- Every applicable movement judgment succeeds.

Only after those obligations succeed may the machine establish the corresponding Fact authority and, if the selected
Operation requires them, provide lawfully participating established Fact values through its separately declared
Operation realization parameters. Lowering targets need not coincide with every Operation Fact parameter.

Physical value formation alone creates no Fact authority. The backend may use temporary mutable material or other
implementation machinery if it preserves the established Lowering law. Such implementation objects must never be exposed
as established Facts.

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

ADR-0063 Section 11 governs each of the following aspects of Projection:

- Completeness
- Equality
- Availability
- Reuse

Equal outcomes or candidate
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

Lowering refusal remains distinct from the following results:

- Input refusal
- Admission rejection
- Canonicalization refusal
- Invariant refusal
- Budget or Capacity results
- Subsequent Fact and movement refusals

They retain
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

For example, the backend may fuse Canonicalization and Lowering or eliminate an intermediate presentation. It may
specialize formation to the established representation law and derive structural cache keys from the exact declared read
set. AOT realization may include the subsequent judgment and handoff paths without changing their semantic boundaries.

If the generated host Operation ABI requires a carrier instance, the backend must materialize that actual
value before invocation. An intermediate carrier may be erased if it is not host-observable and removal
changes neither the ABI nor Contract meaning.

Optimization cannot introduce meaning prohibited by Sections 7–10. Lowering authority ends before user core realization;
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
- the Lowering declaration;
- the source-to-target relation table;
- `LoweringPlan`;
- backend helper metadata;
- candidate or established-Fact wrappers;
- staging objects;
- host-language execution context.

At handoff, the candidate set was complete before the required standing and movement judgments. Those
judgments have succeeded, and external-presentation authority has ended. Fact authority has been established
outside the host value representation by its owning law. Result and Publication are not yet established or
authorized.

The Operation may produce its ordinary declared result candidate.

The following belong to later authorities:

- Result-side Fact establishment
- Result-side movement
- Publication
- Output Presentation

The core must not reopen the erased Input or Lowering declaration as sources of factual meaning.

---

## 18. Open in This Section

The following design decisions remain open or belong to separate work:

| Area                 | Open scope                                                                                                                                                                                                                 |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Authoring            | Exact Fact-qualified target spelling; supported mapping-method inventory; additional authoring, if needed, for nontrivial one-to-one representation refinement.                                                            |
| Compiler realization | Internal Lowering representation and physical Mapping selector encoding; frontend execution architecture (including validator placement and scheduling); query architecture, cache granularity and incremental algorithms. |
| Backend realization  | Helper decomposition, JVM ABI, carrier materialization, allocation strategy, and encoding of declared Lowering refusal.                                                                                                    |
| Other authority      | Binding Establishment and detailed refusal Material and Failure observations.                                                                                                                                              |

The Fact-owned HIR Protocol is defined in ADR-0068 Sections 19.3–19.6; Lowering consumes it under Section 11.

The DSL remains `source -> target : method`, with explicit callable references and arguments under Section 7. No open
decision may weaken the established law, including its prohibitions on runtime implementation discovery, user-supplied
implementations and implicit conversion authority.

The defined semantic scope is:

- Sections 11.1–11.5
    - HIR Candidate References
    - Complete Definition Candidate Projection
    - Mapping Surface and target-qualified Selected Mapping Meaning
    - Operation-local Binding Candidate and Role Selection observations
    - HIR Seal/Definition Judgment entry boundary

  These definitions do not themselves establish any successful Contract Establishment result.
- Sections 11, 11.6 and 11.7
    - Established Definition meaning
    - Definition-time Required Basis
    - Direct Established relations
    - Established Definition observations
- Sections 14.1–14.3
    - Occurrence Basis Requirement Law
    - Whole outcome
    - Successful Established Occurrence Material
    - Occurrence observations

  Detailed refusal Material and Failure observations remain for the Failure design.

The exact mechanics of atomic Fact establishment remain outside this ADR, as do rollback and publication mechanics. The
complete authoring surface for the following also remains outside its scope:

- Additional Fact participation
- Operation Result Material
- Result-side change formation
- Publication
- Output Presentation

---

## 19. Consequences

Lowering becomes an explicit representation-boundary Contract rather than a mapper callback. External and
core vocabularies can remain independent because every Contract-visible relation is authored directly. Fixed
Core Fact targets belong to the Definition, rather than being derived from Operation parameters, while the
selected Operation remains the center of the IDL pipeline Binding.

The closed relation gives the compiler a basis for realization under Sections 12–13 without retaining a user
implementation port. The user implements the Operation business algorithm, not Lowering.

This shifts cost into Contract expressiveness and compiler/backend responsibility. Kontrakt must reject incomplete
meaning. On the selected backend, it must support the declared representation law without losing the refusal or
establishment boundaries. The V1 mapping law (Section 8) preserves explicitness without restricting a Definition to one
target Fact kind.

---