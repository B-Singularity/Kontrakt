# ADR-0064: Input Contract, Explicit Boundary Presentation, and External-Authority Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0067: Lowering Contract
- ADR-0066: Canonicalization Contract
- ADR-0065: Admission Contract
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0074: Compiler Result, Explicit Unsuccessful Result, Recovery, and Observation Boundary
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0056: Governance Contract, Policy-World Control, Whole-Machine Coordination, and Selection Boundary
- ADR-0054: Policy Contract, Explicit Operating Policies, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0039: Adapter-Neutral Metamodel Acquisition, Frozen Fact Image, and Backend-Handle Erasure
- `docs/verification/checklists/kontrakt-1d-hir-establishment-master-checklist.md`

---

## 1. Context

Input is the first one-dimensional Contract of the inbound airlock. It defines one complete presentation meaning that
external material must realize at an Interaction boundary. Input does not own the selecting Interaction or its Binding,
downstream Operations or Contracts, or the host carrier that supplies evidence.

A supported host declaration may already express that presentation completely, so a duplicate Kontrakt-specific DTO is
not required. Host representation and runtime behavior may affect program observations but cannot become Contract
authority. Kontrakt must resolve, refine, and ratify the source evidence before it becomes authoritative Input meaning,
independent of the source language or carrier.

Input admits only finite, closed, directly inspectable presentations. Its established meaning remains available to later
authorities that may lawfully observe it. Material that cannot meet this law must be formed into a supported
presentation
before invocation.

Input does not own later judgments, transformations, composition, or ordering. Those remain with the Contracts and
composition law that own them.

## 2. Decision Drivers

- Supported host declarations should express Input without requiring duplicate carriers, while Contract authority
  remains with Kontrakt.
- Input must support finite, closed, structured values without introducing nested Contract authorities or direct Input
  coordinates.
- Input Definition meaning must be complete and deterministic, independent of the Interaction or Policy World that
  selects it.
- Invocation must judge already-formed immutable material without making Input responsible for acquisition or snapshot
  timing.
- Compiler-side non-entry and Input-owned refusal must remain distinct. Refused Input material cannot proceed to later
  inbound Contracts.

## 3. Decision

### 3.1. Input Meaning

Input is the boundary presentation Contract, with the following authority path:

```text
outside presentation evidence
-> Resolved Input Definition Candidate
-> Input Definition judgment
-> Established Input Definition
-> one fresh Input semantic application over an already-formed immutable presentation
-> Input Occurrence judgment
-> Established Input Occurrence
-> the next selected inbound authority when the occurrence result is Presented
```

Input declares the complete finite presentation surface that may appear at the Interaction boundary and preserves only
the distinctions that its law makes observable. Later Contracts may observe those distinctions through their own lawful
relations, but they do not retroactively add Input meaning. Input does not decide whether presented values may continue,
canonicalize them, lower them into core Fact meaning, or invoke the user Operation.

### 3.2. Flat V1 Presentation

V1 keeps the directly addressable Input coordinate surface flat, exposing one finite set of direct Input coordinates.
Each direct coordinate may itself carry a finite,
closed, acyclic value presentation under Section 6.9.

Nested members, alternatives, elements, keys, and values are constituents of that coordinate's Input meaning. They do
not become nested Input Definitions, independent Input Occurrences, or additional direct Input coordinates merely
because the presentation has internal structure.

A host carrier may contain richer topology, but Input does not adopt host runtime structure or
lifecycle relations as presentation meaning. Section 6.9 owns the legal value topology. Section 6.4
owns carrier separation.

Material whose complete presentation cannot be resolved under those laws must be formed into a supported declared
presentation before it reaches the Input boundary.

### 3.3. Source Evidence Is Not Authority

A supported Java or Kotlin declaration may nominate Input evidence under the carrier-and-Contract separation law in
Section 6.4. Section 6.11 owns the direct-carrier exception for a host form
that already realizes a legal Input presentation.

A different frontend may resolve to the same Input Definition Meaning. Frontend-specific source
evidence does not become a determinant of that meaning. Section 4 owns the separate laws for
Definition identity, semantic equality, and physical reuse.

## 4. Definition Establishment

### 4.1. Resolved Input Definition Candidate

ADR-0071 owns the common HIR boundary. Input specializes it through the Resolved HIR Candidate Protocol rather than
creating an Input-specific second IR.

```text
role-qualified authored Input material
    ↓
frontend resolution
    ↓
Resolved Input Definition Candidate
    ↓
Input-owned Definition judgment
    ↓
Established Input Definition
```

A Definition Candidate is pre-authority material, but it is not a partial Contract. Before it becomes Visible HIR, it
must contain enough resolved Input meaning for Establishment to apply the Input law without reopening source syntax,
rediscovering host structure, or obtaining missing meaning from an Input IDL Binding Candidate. Under ADR-0071, the same
legal resolved inputs must produce the same observable Candidate meaning.

The Definition Candidate Projection exposes one exact Input Definition Candidate Reference together with the complete
producer-owned Input Definition Meaning. A consumer that needs less material uses a legal fine-grained projection under
ADR-0071; consumer convenience does not weaken or redefine the Candidate. Candidate formation does not create an
Established Occurrence, which remains a separate ADR-0063 category when the owning law gives one application meaning of
its own.

### 4.2. Complete Input Candidate Meaning

The Resolved Input Definition Candidate must preserve the complete declared direct coordinate surface and every
Input-visible distinction of its closed presentation meaning.

Every meaning-determining semantic law or reference must be fully resolved before Visible HIR, through complete resolved
meaning or an exact legally owned reference. Establishment cannot recover missing meaning through later source
resolution,
host behavior, or ambient state.

Common Input rules need not be duplicated as Candidate payload when HIR formation or typed projections already enforce
them.

### 4.3. Information Retention and Loss

Frontend resolution may erase source or host details after their contribution to Input meaning has been resolved
completely. Section 6.4 identifies carrier mechanics that do not survive into Contract meaning, and Section 6.11 applies
that rule to the zero-adapter path.

An Input-visible distinction cannot be erased when Establishment or another legal HIR observer would then need to reopen
source to recover it. Sections 6.1, 6.6, 6.8, and 6.9 own the semantic distinctions that must survive when applicable.

Visible Input HIR cannot use a recovery placeholder in place of required meaning. It also cannot silently prune an
undeclared semantic coordinate. If richer external material must be projected into the declared Input surface, that
transformation happens before Input unless a future Input law explicitly owns it.

Compiler processing of source evidence remains realization unless an owning Input law explicitly
makes its result observable.

### 4.4. Definition Meaning Determinants

Input Definition Meaning is determined by its exact direct coordinate surface and complete Input-visible presentation
meaning. Presentation Sameness follows from that meaning. Ordering laws and semantic bases are determinants only when
they affect an Input-visible distinction.

The selecting Interaction, IDL Binding, Policy World, downstream Contracts, source provenance, and host or compiler
realization do not determine Input Definition Meaning. Changes to those contexts alter the Definition Meaning only when
they change the exact resolved Input presentation meaning.

### 4.5. Definition Identity, Version, and Candidate Reference

ADR-0053 governs Contract Version and Authority continuity. ADR-0063 governs authoritative Definition identity and
Establishment.

One explicitly declared Input Contract denotes one Input Authority. Each exact Input Authority and Contract Version
identifies one complete Input Definition without an additional Authority-Local Definition Coordinate. Conflicting
meanings
cannot be established for the same pair, and equal meanings under different Authorities or Versions remain distinct.

```text
exact Input Contract Authority
+
exact Contract Version
    ↓
exact versioned Input Definition
```

Before Visible HIR, the authored Version Claim must resolve to an exact authority-scoped Resolved Version Candidate
Coordinate. The `InputDefinitionCandidateRef` designates that coordinate together with the exact resolved Input
Authority.
It is distinct from the authoritative Definition Reference and cannot be promoted into one. Establishment creates the
authoritative Version Binding and Definition Reference; Version selection is never implicit.

A change to Contract-visible Input presentation meaning under the same Authority requires a new Contract Version. Source
or realization changes do not require a new Version when the complete resolved Input meaning remains unchanged.

### 4.6. Semantic Equality, Presentation Sameness, Merge, and Physical Reuse

Input Definition Meaning is equal only when all Input-owned meaning determinants are equal under their owning laws.

Presentation Sameness, defined in Section 6.9, compares legal values under one exact Input presentation meaning. It
governs Membership uniqueness and Association key uniqueness without a separate user-selectable equality law.

Presentation Sameness does not determine Candidate Reference equality, Definition or Occurrence identity, Fact sameness,
or Canonicalization equivalence. Equal Input meanings under different Authorities or Versions remain distinct.

Input does not merge Definitions through structural equality. Multiple legal Binding Candidates may select the same
already-identified Input Definition, but that selection is not a semantic merge. Physical reuse likewise cannot
establish
semantic equality or Contract identity.

### 4.7. Definition Establishment Judgment and Result

An Input Definition judgment may enter only when its complete Candidate is available as valid Visible HIR. It consumes
the Candidate meaning through the Input HIR Protocol and the exact authoritative Version prerequisite under ADR-0053 and
ADR-0063. The Input IDL Binding Candidate is not an input to this judgment.

The judgment has two Input-owned semantic results:

```text
complete resolved Candidate
+ legal Input Definition meaning
    -> Established Input Definition

complete resolved Candidate
+ meaning that violates the Input Definition law
    -> Input Definition Refusal
```

The judgment applies the exact presentation laws already resolved in the Candidate. It cannot obtain missing meaning
from host behavior or ambient state.

A Candidate that fails HIR sealing cannot enter this judgment. Such failure belongs to the responsible compiler
judgment,
not to Input Definition Refusal. The common unsuccessful-result representation remains a dependency on Proposed
ADR-0074.

Successful Establishment preserves the complete Input Definition Meaning and its authoritative identity under ADR-0063.
Compiler representation does not create another Input authority path.

### 4.8. Resolved Input HIR Candidate Protocol

Input specializes the ADR-0071 Resolved HIR Candidate Protocol without introducing another IR, semantic stage, or
mandatory physical copy.

The Protocol exposes these typed reference domains when required by a legal observation:

```text
ContractInterfaceRef
InteractionRef
InputDefinitionCandidateRef
InputIDLBindingCandidateRef
ResolvedInputCoordinateRef
```

`ResolvedInputCoordinateRef` identifies one direct Input coordinate through its exact `InputDefinitionCandidateRef` and
nominal coordinate identity. Source positions, physical storage, and nested presentation constituents do not determine
that reference.

The Input Protocol catalog is:

```text
Input Definition Candidate Projection
    exact InputDefinitionCandidateRef
    complete Input Definition Candidate meaning

Input IDL Binding Candidate Projection
    exact InputIDLBindingCandidateRef
    exact selecting semantic context
    exact InteractionRef
    Input role coordinate
    exact target InputDefinitionCandidateRef

Fine-Grained Semantic Projections
    Input Candidate Coordinate Surface Projection
    Input Candidate Presentation Law Projection
    Input Binding Target Projection
```

The **Input Candidate Coordinate Surface Projection** exposes the complete direct coordinate surface of one exact
Candidate, including each nominal coordinate and its resolved presentation meaning.

The **Input Candidate Presentation Law Projection** exposes the complete Candidate-owned presentation topology and its
applicable laws:

- presence and absence;
- form-defining cardinality or extent;
- Presentation Sameness and collection uniqueness;
- observable collection order;
- closed Leaf laws; and
- exact semantic-law references when they determine Input meaning.

The **Input Binding Target Projection** exposes one exact Input selection occurrence and its target Input Definition
Candidate without copying the Definition payload.

Each projection is complete only for its declared observation. A fine-grained projection compares only that exact
observation; equal projection payload does not merge Candidate References, Binding Candidate References, or
authoritative
Definitions. Omitted material is not semantic absence.

Logical projections do not require separate physical representations. They may share immutable backing or physical reads
while preserving their distinct semantic observation boundaries.

### 4.9. Input HIR Seal and Establishment Handoff

An Input Definition Candidate may become Visible HIR only when:

- its exact Input Authority and Resolved Version Candidate Coordinate are known;
- its complete Definition Meaning and direct coordinate surface are closed;
- every required semantic reference is exact; and
- its presentation topology is complete under Section 6.9.

The corresponding Input Binding Candidate must resolve the selecting context, Interaction, Input role, and exact target
Candidate. The same selection occurrence cannot have competing targets.

Recovery or poison material, unresolved references, or incomplete meaning prevent HIR sealing. HIR sealing verifies
resolution and
completeness, not whether the Candidate satisfies the Input Definition law. A sealed Candidate may still receive Input
Definition Refusal under Section 4.7.

Establishment may use only legal Resolved HIR observations, authoritative prerequisites permitted by the Input identity
law, and its explicit judgment input. It cannot reconstruct missing Candidate meaning from source, host behavior,
compiler storage, or ambient state.

ADR-0071 governs HIR lifecycle, generation validity, coherence, and Protocol evolution. An unknown required Input
semantic extension fails closed unless a permitted compatible projection or validated migration preserves the required
Input observation.

### 4.10. Required Basis and Applicability Boundary

Input owns no ADR-0063 Required Basis Law, Basis Resolution, Basis Binding, Complete Basis, separate Input Applicability
judgment, Input arbitration, or Input Composition judgment. These categories are `NOT-APPLICABLE` to the current Input
law.

An exact semantic law or reference may still determine Input presentation meaning. When required, it is part of the
complete Input Definition Meaning and must be resolved before Visible HIR. It is not a deferred Required Basis to be
discovered at occurrence time.

Selection of an Established Input Definition for an Interaction belongs to Interface, Policy, and Governance law. Input
does not reselect the Contract World or incorporate independent governing context into its Definition or Occurrence
meaning.

Meaning-determining semantic references follow the ADR-0063 prerequisite law. Input provides no fixed-point exception:
cyclic semantic Establishment dependencies remain illegal. Compiler query or analysis cycles do not themselves
constitute such dependencies.

### 4.11. Established Input Definition Material

Successful Definition Establishment produces one complete Established Input Definition. Its Input-owned semantic surface
contains:

```text
Established Input Definition
    exact EstablishedInputDefinitionRef
    exact Owning Input Authority Binding
    exact Version Binding
    complete Input Definition Meaning
    direct established relation to each direct Input coordinate
```

The complete Definition Meaning is the same source-owned meaning closed by Sections 4 and 6. It
includes:

- the direct coordinate surface and presentation topology;
- presence and absence;
- form-defining cardinality or extent;
- Presentation Sameness and collection uniqueness;
- observable collection order when owned;
- closed Leaf laws; and
- exact semantic-law references when they determine the presentation.

Each direct coordinate may be observed through an `EstablishedInputCoordinateRef`. Its semantic form
is the exact `EstablishedInputDefinitionRef` plus the exact nominal direct Input coordinate. The
compiler may use a replaceable physical encoding for that reference. The encoding is not coordinate
identity and cannot make nested constituents into direct Input coordinates.

Input establishes no third Input-specific material family beside Definition and Occurrence material. Selection bindings,
world material, Basis material, and application-context relations remain owned by their respective authorities or are
not semantic material at all.

## 5. Invocation Boundary

For one applicable Interaction, the established Contract World relation identifies the exact Established Input
Definition
that governs that Interaction's Input boundary. Invocation does not query or reinterpret the pre-authority Input IDL
Binding Candidate.

### 5.1. Occurrence-Time Input Presentation Judgment

Input has independent occurrence meaning. One Input semantic application is one fresh entry of actual boundary material
into the Input judgment for one exact Established Input Definition at one exact applicable Interaction Input boundary.

```text
exact Established Input Definition
+
exact InteractionRef
+
one fresh Input boundary entry
+
actual already-formed presentation material
    ↓
Input Occurrence judgment
    ↓
Presented
or
Refused
```

`Presented` means that the exact complete immutable presentation satisfies the applicable Input law. Input Establishment
therefore establishes a direct occurrence-specific relation from the exact `EstablishedInputOccurrenceRef` to that
Established Presentation. A later inbound authority may observe the presentation only through a legal producer-owned
Established Input projection and only when the selected composition makes that Input material available to it.

`Refused` means that the Input judgment was legally entered but the supplied material did not satisfy the declared Input
presentation law. Input Establishment establishes `Refused` as the result of that exact Input semantic application, but
no Input-authoritative presentation relation is established from the malformed or incompatible material, so no later
inbound authority is reached.

The judgment includes the presentation-level uniqueness and relation obligations owned by Section 6.9 when they apply,
but it does not decide whether an otherwise correctly presented value may continue. Section 6.8 defines Input's side of
that boundary, while Admission owns the continuation judgment under its own Contract.

Carrier formation and the separation between carrier mechanics and Contract meaning follow Section 6.4. Immutability
follows Section 6.3. Input does not repair, snapshot, normalize, or reinterpret material that reaches the boundary in a
form those laws reject.

Definition-time verification establishes that a declaration can produce a legal Input Definition. It does not authorize
arbitrary runtime material merely because the host type appears compatible. Occurrence-time judgment uses only the exact
Established Input Definition reached through the applicable established Contract relation; it does not reconstruct that
definition from source, HIR Binding material, graph state, cache state, or backend representation.

### 5.2. Input Occurrence Identity and Fresh Re-Entry

Every legally entered Input application has one typed `EstablishedInputOccurrenceRef` identifying
that exact semantic application. Occurrence identity is nominal application identity owned by Input.
Neither presentation value equality nor a physical execution or storage coordinate supplies that
identity.

The same Definition and the same Presentation may therefore participate in several distinct Input Occurrences.

```text
Occurrence O1 != Occurrence O2

Definition(O1) = Definition(O2)
Interaction(O1) = Interaction(O2)
Presentation(O1) PresentationSame Presentation(O2)
Result(O1) = Result(O2)
```

Every fresh re-entry into the Input boundary creates a new Input Occurrence. Retry, restart, or redelivery therefore
creates a new occurrence even when the same Definition, Interaction, determining semantic material, and Presentation
Sameness lead deterministically to the same Input result. For the result law, the same exact Established Input
Definition
and the same Input-owned meaning-determining presentation distinctions produce the same `Presented` or `Refused` result;
that determinism does not collapse occurrence identity. A duplicate transport event eliminated before the Input boundary
does not create a second Input Occurrence because no second Input semantic application entered.

Repeated physical evaluation of the same exact occurrence does not create additional occurrence authority. Parallel,
speculative, retried compiler work may converge on one semantic occurrence when it is evaluating that same exact
application. This is different from two fresh boundary entries, which remain distinct occurrences even when every
presentation distinction is equal.

### 5.3. Established Input Occurrence Material

A legally entered Input judgment establishes one complete occurrence-specific semantic result under
ADR-0063. This material consists of exact Input-owned semantic relations. It does not require a
dedicated physical object.

For a successful presentation, the established relations are:

```text
EstablishedInputOccurrenceRef
    -> exact Input semantic application

Applied Input Definition relation
    EstablishedInputOccurrenceRef
        -> exact EstablishedInputDefinitionRef

Interaction attribution relation
    EstablishedInputOccurrenceRef
        -> exact InteractionRef whose Input boundary was entered

Input occurrence result relation
    EstablishedInputOccurrenceRef
        -> Presented

Established Presentation relation
    EstablishedInputOccurrenceRef
        -> exact complete immutable Input presentation
```

For a refusal, the established relations are:

```text
EstablishedInputOccurrenceRef
    -> exact Input semantic application

Applied Input Definition relation
    EstablishedInputOccurrenceRef
        -> exact EstablishedInputDefinitionRef

Interaction attribution relation
    EstablishedInputOccurrenceRef
        -> exact InteractionRef whose Input boundary was entered

Input occurrence result relation
    EstablishedInputOccurrenceRef
        -> Refused
```

These relations preserve only direct Input-owned attribution that actually determines the application meaning. The
current Input law has no Required Basis or Input-owned Applicable Context under Section 4.10, so Policy World,
Governance Binding, State, Budget, Capacity, run state, and the surrounding transitive semantic world do not become
occurrence-specific Input relations.

The direct relation to `InteractionRef` is required because one Input Definition may be used by several Interactions and
one Input semantic application occurs at one exact Interaction Input boundary. A separate Input-role semantic relation
is unnecessary: the reference domain already identifies an Input Occurrence and the Interaction relation identifies the
boundary to which that application belongs.

A `Refused` application establishes no Input-authoritative relation from its
`EstablishedInputOccurrenceRef` to rejected material, runtime exceptions, or the runtime carrier.
Separate Diagnostic or provenance products may preserve permitted evidence under their own laws
without turning that evidence into Input meaning.

Once established, these occurrence-specific semantic relations are immutable. A later application may
be affected by:

- a later Version, Policy, Governance, or State;
- Contract World replacement;
- retry; or
- a later compiler generation.

None of these can rewrite the meaning already established for the earlier Input Occurrence Reference.

The producer-owned Established Input Semantic Protocol defined in Sections 9.2 through 9.4 is the legal observation
boundary for these relations. Consumers do not obtain occurrence meaning by dereferencing an occurrence object or by
traversing compiler storage topology.

After a Presented occurrence is established, later inbound processing may change physical representation only through
the declared Contract relations that own those changes. The material eventually supplied to the user Operation must
preserve the meaning lawfully derived through the complete declared inbound pipeline. Undeclared substitution, mutation,
reinterpretation, or introduction of different meaning is not a valid realization.

The compiler and runtime must prove or check the required integrity at the boundaries where the
applicable law cannot be guaranteed otherwise. This obligation does not prescribe a particular
generated access or recheck strategy. Any realization must preserve the same Contract result, whether
it proves the obligation statically or checks it at the required boundary.

Policy, Governance, Budget, Capacity, and other independently applicable authorities retain their own results. Their
negative or stopping results do not become Input Refusal and do not create an Input Occurrence when they prevent the
Input judgment from legally entering.

## 6. V1 Ratifiability Law

V1 Input ratifies only finite, closed, directly inspectable presentation meaning that is fully resolved before Visible
HIR. Actual Input material must already be immutable when it enters the Input boundary.

Ratification cannot depend on implicit interpretation, executable host behavior, or live external state. A selected host
declaration may provide evidence, but selection alone does not establish Input authority.

### 6.1. Coordinate Closure

One exact Input IDL selection occurrence identifies the source material that is refined into its target Input Definition
Candidate under ADR-0047 and ADR-0071.

Input does not infer, broaden, replace, merge, or arbitrate that selection. One exact selection occurrence must resolve
to one exact target before the resulting Binding Candidate can become Visible HIR.

Every contract-visible direct Input coordinate must resolve to one complete and closed presentation meaning; no direct
coordinate may remain semantically open, erased, dynamically unresolved, or dependent on runtime type discovery. A
frontend-specific source form is admissible only when the frontend can refine it completely into that closed meaning.
Otherwise the Input definition is not established. Java raw or wildcarded types and Kotlin star-projected forms
are examples of source forms that require such complete refinement.

The closed Input surface does not silently accept or prune an undeclared semantic coordinate. Material that contains
additional transport- or application-level structure must be projected into the declared Input presentation before this
boundary unless a future Input law explicitly owns that projection.

### 6.2. Direct Coordinate Identity and Meaning

Every direct Input coordinate has one exact nominal identity within its Input Definition and resolves
to one complete and closed presentation meaning. The resolved nominal coordinate identifies that
direct coordinate. Source declaration positions, traversal order, and physical storage coordinates do
not. Structural resemblance also does not create coordinate identity.

Changing a direct coordinate's nominal identity changes Input presentation meaning. Reordering the declarations of the
same nominal coordinates does not change Input meaning unless another explicitly declared Input law independently makes
an order observable.

Before Establishment, the HIR Protocol may refer to one direct coordinate through
`ResolvedInputCoordinateRef`. That reference is semantically anchored by the exact
`InputDefinitionCandidateRef` and the exact nominal direct coordinate. Source or physical coordinates
cannot define it. Section 4.8 owns its Protocol exposure.

Nested presentation members may have Input-local constituent identity under Section 6.9. They do not thereby become
direct Input coordinates, cross-authority coordinate references, or Lowering source coordinates.

Because Input authority depends on the resolved coordinate meaning rather than the host type used to
present it, a frontend may admit a source form only when it preserves every distinction required by
the declared Input meaning. Host-language type categories are frontend evidence, not Input Contract
vocabulary by themselves.

### 6.3. Explicit Immutability

The complete Input presentation, including all nested constituents, must already be formed and immutable when it enters
the Input boundary. Input judges that presentation; it does not make mutable material legal through copying,
snapshotting,
freezing, or transformation. Such formation must occur before the boundary.

The presentation must not depend on mutable backing state, external aliases, or lazy materialization. Host immutability
mechanisms may provide evidence, but they do not define the Contract obligation.

### 6.4. Carrier and Contract Separation

A host carrier may express Input evidence without owning the resulting Input meaning. Its runtime identity and storage
are realization. The mechanisms that constructed it are also realization,
even when they are convenient for acquisition.

Executable host behavior cannot create Input coordinates or change their meaning. An accessor may expose
already-declared evidence, but observing that accessor does not give its implementation Contract authority. Reflection
or generated access may acquire the same evidence; neither mechanism defines the Contract surface.

A frontend may therefore interpret a supported host form only as evidence for meaning that the Input law already
defines. Section 6.11 covers the case where the same host declaration also serves as the direct runtime carrier.

### 6.5. Inheritance and Polymorphism Boundary

ADR-0079 governs source-language inheritance and generic type resolution. A frontend may use fully resolved inherited
member information as evidence for an explicitly declared Input presentation only when the applicable Input carrier and
closure laws permit it. Host inheritance does not select Input coordinates or add Contract meaning.

The selected Input Definition must contain its complete presentation independently of host inheritance or runtime
implementation choice. At invocation, runtime subtype discovery or dispatch cannot add, select, or reinterpret Input
coordinates or alternatives.

### 6.6. Presence, Absence, and Finite Choice

Input may declare presence, absence, and finite alternatives as explicit presentation distinctions, but it must
preserve every distinction required by the declared presentation meaning and cannot silently collapse distinct
alternatives.

Presence or absence is Contract meaning only when the Input law declares that distinction. A host-language `null`,
nullable type, optional wrapper, sentinel value, or another source representation does not acquire Input meaning by
itself. A frontend may map such a source form to declared Input meaning only when that mapping is complete and
unambiguous.

Input does not apply defaults. Source-language and construction-time defaults remain pre-boundary
formation; they do not become Input authority. Input judges the actual presentation supplied at its
boundary.

If omission is legal, absence must be an explicit declared alternative. If a concrete coordinate is required, the
supplied presentation must already contain it.

A finite choice must be closed. Every declared alternative must be completely and unambiguously distinguishable under
the Input law. The actual runtime material may not enlarge, replace, or reinterpret the declared alternative set through
subtype discovery or implementation-specific behavior.

Frontend or backend representation of those alternatives remains realization.

### 6.7. Opaque Values and External Denotation

Host object-reference identity does not provide Input meaning or semantic identity. An Input coordinate may nevertheless
carry a declared immutable value that denotes something outside the Input presentation. Input
owns only the declared presentation of that value. The existence, identity, validity, ownership, or behavior of the
denoted target is not established by Input.

A reference-valued Input coordinate does not by itself create a Definition Reference, Occurrence Reference, object
relation, lookup obligation, or dereference authority under ADR-0063.

An opaque value is admissible only when its complete Input-visible presentation distinctions are
fixed by an exact Input presentation law. Opaque treatment cannot admit an arbitrary user object by
disregarding its behavior, mutability, identity, or lifecycle.

A frontend may map a supported host type to that already-defined presentation meaning only when the
mapping is complete. The host type does not define the Input law. Its operations do not become Input
meaning merely because they are associated with the carrier.

Input does not establish canonical identity or normalized meaning for an opaque value. Those meanings remain with the
Contract that owns them.

### 6.8. Presentation-Only Authority

Input judges whether boundary material realizes its declared presentation. A structural bound belongs to Input when it
defines the required presentation form, such as extent or cardinality. Conditions governing whether correctly presented
values may continue belong to Admission or another owning authority.

Input establishes no authority over a target denoted by a presented value. It also does not establish downstream
Contract
meaning, governing selection, or machine movement. Those judgments remain with their respective authorities.

### 6.9. V1 Presentation Algebra

Input uses a small closed value-presentation algebra. These semantic families describe Contract-visible value topology.
They are not Java or Kotlin classes, and they do not prescribe one generated host API form.

The V1 fundamental families are:

```text
Leaf

Named Product

Closed Tagged Choice

Sequence

Membership

Association
```

Their exact authoring tokens and physical HIR encoding remain open under Section 10.

A **Leaf** is a complete presentation meaning that Input does not further decompose into constituent topology. Its
complete legal domain, Input-visible distinctions, and Presentation Sameness are fixed by the Kontrakt-owned Input law.
When that meaning genuinely consumes versioned or external semantic data, the exact meaning-determining semantic law
material must be explicitly ratified and closed rather than supplied by ambient provider state. Section 4.10
distinguishes
that Definition determinant from ADR-0063 Required Basis.

A **Named Product** is a finite closed set of nominally identified members. Each member has one complete presentation
meaning and its own explicit presence law when absence is permitted. Product member declaration order is not semantic
order.

A **Closed Tagged Choice** has one finite closed domain of nominal alternatives. Exactly one alternative is selected in
one occurrence. A case may have no payload or one complete constituent presentation. Case declaration order is not
semantic order. Runtime subtype discovery and untagged structural matching do not select a case.

A **Sequence** contains zero or more occurrences of one constituent presentation. Position is intrinsic semantic
meaning, multiplicity is retained, and every actual Input occurrence is finite. Fixed or bounded extent is expressed
through the presentation cardinality law rather than by creating another fundamental family.

A **Membership** contains zero or more occurrences of one constituent presentation. Two members cannot be distinct
within the same Membership when they are the same under that constituent presentation's exact Presentation Sameness law.
Membership itself has no intrinsic position. Its order-observation law is independent of uniqueness.

An **Association** contains zero or more key/value associations. Keys are unique under the key presentation's exact
Presentation Sameness law. Key and value are presentation constituents rather than Input coordinate identities.
Association itself has no intrinsic entry position.

#### 6.9.1. Presentation Sameness and Input Uniqueness

Every exact Input presentation meaning defines **Presentation Sameness** from the complete set of
distinctions that Input owns for that presentation. Presentation Sameness asks whether two legal
values preserve the same Input-visible meaning. It remains distinct from Host equality, Candidate
semantic equality, Definition and Occurrence identity, Fact sameness, Canonicalization equivalence,
HID equality, and physical representation equality.

Presentation Sameness is intrinsic to the resolved presentation meaning and is fixed by the
Kontrakt-owned Input semantic law. V1 provides no user-selectable Input equality, collation,
normalization, or equivalence strategy. A Leaf obtains its sameness from its exact resolved
presentation meaning. Named Product, Closed Tagged Choice, and Sequence lift constituent Presentation
Sameness through their declared structure. Membership and Association preserve the exact presented
constituent or key representatives and every Input-visible order distinction.

For every legal complete value in one exact presentation domain, Presentation Sameness must be deterministic, reflexive,
symmetric, and transitive. Missing, unresolved, malformed, unsupported, or otherwise illegal material is outside that
legal value domain rather than merely unequal. Any meaning-affecting semantic basis required to determine Presentation
Sameness must already be fixed or established as explicitly compatible under the applicable owning law before Visible
HIR. An explicitly ratified closed semantic basis may participate when the presentation meaning actually consumes it.
Runtime-selected provider state, ambient locale, host defaults, service discovery, or another unbound environment source
cannot complete the relation later.

Membership uniqueness is defined only by the constituent presentation's Presentation Sameness. Association key
uniqueness is defined only by the key presentation's Presentation Sameness. V1 Input provides no independently
selectable coarser equality or equivalence law for Membership or Association. Within one occurrence, two members that
are the same under constituent Presentation Sameness cannot coexist as distinct Membership members, and two keys that
are the same under key Presentation Sameness cannot coexist as distinct Association keys. Input does not choose
first-wins, last-wins, silent deduplication, or a representative.

Host operations, ambient environment state, and backend implementation details cannot redefine Input
Presentation Sameness or collection uniqueness. A host carrier is usable only when the supported
boundary mapping can preserve every Input distinction required by the Kontrakt-owned presentation
law. If the carrier has already collapsed values that Input distinguishes, direct binding is not a
lossless Input presentation and must be rejected or formed through another legal boundary before
Input. ADR-0073 owns that platform-support and preservation gate.

A later Contract may intentionally establish a different relation without retroactively changing Input. In particular,
Canonicalization may explicitly declare an equivalence and establish a stable representative that collapses distinctions
preserved by Input. When Canonicalization is absent, no hidden equality, normalization, or representative law is
inserted; the Established Input Presentation remains unchanged when observed by the next selected inbound authority.
Fact sameness, HIR semantic equality, Definition and Occurrence identity, and other later relations remain owned by
their respective authorities.

#### 6.9.2. Constituent Closure

A direct coordinate may contain these families recursively only as a finite, closed, acyclic presentation graph. Nested
members, cases, elements, keys, and values belong to the containing Input Definition. They do not create
independent Contract authorities, independent Input Definitions, or independent Input Occurrences.

The actual value judged at the Input boundary must be complete and finite. A semantic recursive type, mutually recursive
presentation, runtime cyclic graph used as presentation meaning, lazy constituent source, or live stream is not a V1
Input presentation.

Host aliasing and shared-reference topology do not change Input meaning. If an external relation must be presented, it
is carried as an explicitly declared descriptive value under Section 6.7 rather than as pointer identity.

#### 6.9.3. Collection Order Observation

Sequence owns positional order intrinsically, while Membership and Association may own one of the following
order-observation laws:

```text
No Observable Order

Encounter Order

Ordered By an Exact Total Ordering Law
```

`Encounter Order` means that one occurrence preserves an observable linear encounter sequence. It does not imply a
comparison relation between arbitrary constituent values.

`Ordered By` means that an exact deterministic total ordering law determines the observable constituent order. For
Association the ordering law applies to keys. The law must have an exact closed domain and every semantic-basis
distinction required to determine its result. It must be stable for the presented value and free of ambient state and
executable callback authority.

For V1 Membership and Association, ordering zero-equivalence must coincide exactly with the applicable constituent or
key Presentation Sameness. An ordering law that groups two Input-distinct values at one ordering position, or separates
values that Presentation Sameness identifies, is not a legal V1 `Ordered By` relation.

Observable order participates in Presentation Sameness whenever this Input presentation makes that order visible.
Membership and Association uniqueness still use the applicable constituent or key Presentation Sameness rather than a
separate host equality operation. A host platform's `equals` operation does not authorize the compiler to erase an
Input-visible encounter or ordered-by distinction.

Contract-visible order, deterministic compiler observation order, and physical storage order are separate:

```text
Contract-visible order
    != deterministic compiler observation order
    != physical storage order
```

The compiler may sort an unordered presentation for deterministic compiler processing or
representation. That realization order does not become Input meaning. Conversely, deterministic
implementation cannot erase a declared encounter or ordered-by obligation.

An exact JVM collection surface never selects one of these Input order-observation laws. Platform
collection behavior is platform evidence only. ADR-0073 may admit such a surface for an Input role
only when it preserves the already-declared Input order, constituent Presentation Sameness,
uniqueness, and every other required Input distinction without loss. Additional platform observations
remain platform meaning; they do not enlarge Input meaning.

Host collection ordering behavior is not adopted as Input authority. A platform order may realize
`Ordered By` only when its zero-equivalence and observable order preserve the exact Input ordering
law. A carrier that has already collapsed Input-distinct members or keys cannot be repaired at the
Input boundary. Order compatibility alone does not establish direct-carrier legality; immutability,
constituent closure, alias safety, capability isolation, and the other applicable Input and ADR-0073
laws still apply.

Host or generated collection operations are not Input presentation relations and do not create
additional fundamental presentation families. When a supported platform or generated API exposes such
operations, its owning API law and ADR-0073 own the separate preservation and use-legality
obligation. No such operation may silently introduce a broader equality, ordering, or key-equivalence
relation into Input.

#### 6.9.4. Derived Presentations and Deliberate V1 Exclusions

A semantic shorthand does not require a new fundamental family when the existing algebra preserves every Input-visible
distinction.

Examples include:

```text
Enum
    -> payloadless Closed Tagged Choice

Flags
    -> Membership over a finite nominal case domain

Option-like value
    -> two-case Closed Tagged Choice

Fixed-extent sequence
    -> Sequence + exact presentation cardinality
```

Coordinate absence remains distinct from an Option-like value, an empty collection, a null-like Leaf value, and
whole-Input absence. V1 deliberately does not add a generic positional heterogeneous Tuple, Multiset, or generic Result
family. A future
revision may add one only after demonstrating a Contract-visible distinction that cannot be represented without semantic
loss and after passing the same HIR, Establishment, platform-preservation, and API review applied here. Unsupported
meaning is not approximated through a supported family.

V1 excludes the following Input presentation categories:

- Open structures: open records, extensible tables, open rows, and flexible or unknown alternatives.
- Dynamically interpreted structures: dynamic `Any` or document values and untagged unions selected by runtime shape.
- Recursive or reference-based structures: recursive presentations and object/reference topology.
- Executable or externally controlled material: capabilities, resources, futures, streams, callbacks, and executable
  values.

#### 6.9.5. Closed Leaf Presentations

A Leaf is ratifiable only when its complete legal presentation domain, every Input-visible distinction, and Presentation
Sameness are closed by the submitted presentation together with fixed Kontrakt-owned law and, only when that meaning
actually requires it, an explicitly ratified closed semantic basis. Runtime-selected provider state, ambient
environment, host defaults, user-selected comparison policy, or executable application behavior cannot complete the Leaf
meaning.

This section defines V1 Input Leaf presentation profiles and their Presentation Sameness. Shared Numeric, Text, and
Temporal value meanings remain governed by ADR-0078, ADR-0080, and ADR-0082. ADR-0073 governs the separate legality
of Java and Kotlin carriers and operations.

The following table defines the baseline V1 Input Leaf profiles and their Presentation Sameness. All meaning-determining
requirements must be closed before a concrete Leaf can be published. The listed names are semantic labels, not required
authoring or generated API names.

| Leaf meaning                           | Legal Input presentation                                                                                                        | Presentation Sameness                                                           | Distinctions not supplied by Input                                                                           |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| Boolean                                | one legal truth value                                                                                                           | the truth values are equal                                                      | source spelling or numeric source convention                                                                 |
| Fixed-width signed or unsigned Integer | one value in the exact declared integer domain                                                                                  | the mathematical integer values are equal within that same presentation domain  | source radix, sign spelling, leading zeroes, or cross-Leaf numeric equality                                  |
| Bytes                                  | one finite bounded octet sequence                                                                                               | the sequences have the same length and the same octet at every position         | Base64, hexadecimal, transport framing, or source encoding spelling                                          |
| Text                                   | one finite bounded sequence of Unicode scalar values                                                                            | the sequences contain the same scalar values in the same positions              | UTF-8/UTF-16 storage, source spelling, Unicode normalization, case folding, collation, or locale             |
| UUID                                   | one exact 128-bit UUID datum                                                                                                    | all 128 bits are equal                                                          | textual case or punctuation spelling, lookup of any denoted entity, or resource authority                    |
| Decimal                                | one finite decimal coefficient together with its exact scale under the applicable closed bounds                                 | coefficient and scale are both equal                                            | lexical spelling, numerical-cohort equality, trailing-zero collapse, NaN, or infinity                        |
| Binary32 or Binary64                   | one exact IEEE binary interchange datum admitted by the applicable Leaf law                                                     | the complete admitted bit datum is equal                                        | host numerical equality or ordering, NaN canonicalization, signed-zero collapse, or arithmetic-result policy |
| ISO Date                               | one date in the fixed proleptic Gregorian presentation domain                                                                   | year, month, and day are equal                                                  | source date spelling or another calendar system                                                              |
| Local Time                             | one closed local civil time value under the fixed V1 time-of-day domain                                                         | hour, minute, second, and fractional-second value are equal                     | source precision spelling, time zone, offset, or leap-second lexical evidence                                |
| Local Date-Time                        | one exact local civil date-time under the fixed ISO Date and Local Time laws                                                    | all Input-visible local date-time distinctions are the same                     | offset, zone, instant interpretation, or source timestamp spelling                                           |
| Numeric UTC Offset                     | one exact numeric offset in the applicable closed offset domain                                                                 | the numeric offsets are equal                                                   | zone identity, zone rules, or source offset spelling                                                         |
| Offset Date-Time                       | one exact offset date-time whose retained distinctions include local civil date-time and numeric UTC offset                     | the retained local date-time and offset distinctions are the same               | same-instant equivalence, zone identity, or source timestamp spelling                                        |
| Duration                               | one exact signed duration quantity under the fixed unit and resolution law                                                      | the duration quantities are equal under that law                                | lexical duration spelling or calendar-relative period meaning                                                |
| Instant                                | one position under the fixed Kontrakt Instant Temporal Reference (origin, time scale, leap-second law, unit, range, resolution) | the complete coordinate is equal under the exact fixed Temporal Reference       | source timestamp spelling, source offset, zone, clock provenance, or leap-second parse evidence              |
| Zone Descriptor                        | one exact closed zone descriptor, preserving its descriptor kind and payload                                                    | the descriptor kind and exact descriptor payload are the same                   | current zone rules, current offset, provider availability, alias resolution, or resource authority           |
| Resolved Zoned Date-Time               | one exact resolved zoned date-time whose retained distinctions include local date-time, numeric UTC offset, and Zone Descriptor | all retained temporal and zone-descriptor distinctions are the same             | current time-zone-rule consistency, same-instant equivalence, or future rule interpretation                  |
| URI Reference                          | one syntactically legal closed URI-reference presentation under the applicable grammar                                          | the retained component presence and character presentation are exactly the same | resource identity, dereference result, scheme-specific resolution, or URI normalization                      |

Input Text follows the Unicode Scalar meaning defined by ADR-0080. A Java or Kotlin `String` is only a possible carrier
and must preserve the admitted Input Text meaning. Normalization and case folding are not implicit Input operations.

Temporal Input Leaves represent already-formed values under ADR-0082. `Instant` requires its exact fixed Temporal
Reference. `Zone Descriptor` does not establish zone rules. `Resolved Zoned Date-Time` preserves its declared local
date-time, offset, and zone descriptor independently of later changes to time-zone rules.

Temporal acquisition, zone-rule resolution, and conversions using versioned temporal data remain separate operations
under ADR-0082 and ADR-0073. They cannot introduce ambient provider state into Input meaning. Local Date-Time and Zone
Descriptor do not automatically establish a Resolved Zoned Date-Time.

A host type name or scalar-shaped carrier does not establish an Input Leaf. A separately ratified descriptive value
must have closed presentation meaning and Presentation Sameness, and cannot import external resource authority. An
existing Input family may represent that value when it preserves the declared meaning.

### 6.10. V1 User-Facing Authoring Classes

The V1 user-facing policy classifies host forms only to determine whether they can express an already-declared Input
presentation. The host category does not create Contract meaning.

| Authoring class                                                  | V1 treatment                                                                                                           | Examples                                                                                         | V1 frontend consequence                                                                                                                                              |
|------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct closed immutable presentation                             | Refine and ratify directly when every declared distinction is complete, immutable, supported, and legal under ADR-0073 | Losslessly mapped primitive or `String`; closed enum form; flat Kotlin data class or Java record | The selected declaration may supply external Contract evidence without a duplicate carrier declaration; actual invocation material remains subject to Input judgment |
| Closed structured meaning without a ratified direct host mapping | Require explicit formation into a supported presentation before Input                                                  | Nested DTO; unsupported sealed hierarchy; collection carrier with non-preserved observations     | The semantic presentation may be legal under Section 6.9, but this host carrier does not become authority merely because its shape resembles that presentation       |
| Implementation-shaped carrier                                    | Require explicit formation or reject the source                                                                        | Mutable JavaBean; proxy; runtime-discovered implementation                                       | Host conventions, mutable observation, lifecycle, and implementation relationships do not become Input meaning                                                       |
| Behavior, capability, movement, or role leakage                  | Reject from Input                                                                                                      | Callback; service; live resource; stream; core Fact authority; State; backend handle             | The material belongs to another role or is not Input presentation data                                                                                               |

A platform collection category does not by itself determine whether a carrier is accepted or
rejected. Section 6.9 defines Input semantic meaning. ADR-0073 owns the platform-surface ratification
and constituent-closure gate that determines whether a particular host surface can realize that
meaning without loss.

The exact V1 direct-support matrix for nested and collection-shaped Java or Kotlin carriers remains
compiler/platform-support work. A legal Input presentation does not imply that every familiar host carrier for a similar
shape is a legal zero-adapter carrier.

### 6.11. Zero-Adapter Direct Carrier

A selected host declaration may serve as both source evidence and runtime carrier when it already realizes a legal
immutable V1 presentation under this ADR and passes the applicable ADR-0073 gate.

For the current JVM V1 zero-adapter path, a flat Kotlin data class may provide that evidence directly when every
selected coordinate has a supported presentation meaning. The same is true for a flat final Kotlin class whose selected
coordinates are primary-constructor `val` properties, and for a flat Java 17+ record whose components satisfy the same
requirement. These forms describe the current direct-support path, not a restriction on the nested semantic
presentations permitted by Section 6.9.

Only the resolved Input-visible coordinate surface survives into meaning. Constructor behavior and generated host
machinery remain outside authority under Section 6.4.

Source declaration order may help the frontend acquire the surface deterministically, but it is not Input semantic order
and does not identify a direct coordinate. Section 6.2 owns direct coordinate identity. Section 6.9 owns any order that
is actually observable inside a presentation.

A direct carrier cannot depend on runtime implementation discovery or recursive host traversal that has not been
ratified as a closed constituent mapping. Material that needs unsupported traversal or external lifecycle behavior must
be formed into a supported presentation before invocation.

Another frontend may later realize semantically equal Input presentation meaning. Section 4.6 prevents that equality
from merging independently declared Contract identities.

### 6.12. Behavior and Capability Are Not Opaque Leaves

Labeling an executable capability as opaque does not make it legal Input. Input may instead carry an
approved closed descriptive value that denotes something outside the machine, under the applicable
Leaf or structured-presentation law. The execution capability, live resource authority, provider
state, or resource ownership denoted by that value remains outside Input authority. A bearer or
capability token is not rescued by scalar-shaped presentation.

---

## 7. Authoring Boundary

Each declared Input selection is authored from the complete Contract meaning intended for that selection rather than
from a reuse target. Several legal selection occurrences may deliberately select one already-identified Input Definition
under ADR-0047 and ADR-0071. Those occurrences may belong to different Interactions or declared Contract World
contexts. Similar host shape is insufficient. If two Inputs were declared independently, Section 4.6 prevents later
structural equality from merging them.

A supported host-facing declaration may still remove the need for a second carrier declaration when it completely
expresses one selected Input Contract. That frontend convenience is separate from sharing one Input Definition across
several selection occurrences.

Richer application objects remain legal outside Kontrakt, but they must be formed into the applicable Input presentation
before invocation unless their exact host surface passes the applicable direct-carrier gate. Section 6.9 defines the
semantic presentation independently of that platform support. The authoring surface therefore does not force the
application object model into the Contract Core.

## 8. Refusal Boundary

Input distinguishes compiler-side non-entry from two Input-owned negative results. A source or Candidate that cannot
satisfy the Resolved HIR seal does not enter the Input Definition judgment. Unresolved
source, recovery or poison material, an invalid Protocol handoff, or another compiler-owned failure therefore does not
create `Input Definition Refusal`. The exact unsuccessful compiler result is owned by the applicable compiler judgment;
its common representation and recovery behavior currently depend on Proposed ADR-0074.

`Input Definition Refusal` occurs only after a complete resolved Candidate legally enters the Input Definition judgment
and fails the Input-owned Definition law. No authoritative Input Definition is established from that Candidate.

`Refused` at invocation time occurs only after an exact Established Input Definition and exact Interaction have allowed
a fresh Input semantic application to enter the occurrence-time judgment. That judgment establishes a fresh
`EstablishedInputOccurrenceRef` and the exact result relation `Refused`, but it establishes no Input-authoritative
presentation relation, so no later inbound authority is reached. A supplied boundary value that omits a required
coordinate or violates the declared presentation law is handled here.

A transport, acquisition, or orchestration failure that prevents the material from entering the Input semantic
application creates no Input Occurrence. Likewise, failure to obtain an applicable Established Input Definition or a
stop
owned by Policy, Governance, Budget, Capacity, or another authority remains the result of that owner rather than an
Input Refusal.

Sections 6.3 through 6.7 define material that Input does not repair into legality after entry. Neither Input Definition
Refusal nor an Input Occurrence result of `Refused` automatically becomes ADR-0057 Failure; the Failure Contract retains
its own authority. Diagnostic evidence may explain either Input result, but Diagnostic does not create or rewrite that
meaning.

## 9. Relationship to Later Contracts

Input establishes only Input-owned Definition and Occurrence meaning and creates no later pipeline authority. Admission
owns continuation under Section 6.8, Canonicalization may establish a stable same-shape representative when selected,
and Lowering owns the later declared relation from Input coordinates toward Operation parameters and Fact coordinates.
Which later authority observes an Established Input Presentation directly is determined by the selected inbound
composition rather than by Input. Nested constituents under Section 6.9 do not become Lowering source coordinates
merely because they are addressable inside the Input presentation. None of those later authorities enters Input
Definition identity, and their negative results remain distinct from Input Definition Refusal and Input Occurrence
`Refused` under Section 8.

### 9.1. World and Occurrence Semantic Placement

Established Input Definition Material is definition-world meaning and is made available through the Canonical Contract
World under ADR-0063. Direct Input coordinate meaning belongs to that complete Established Definition surface.

Established Input Occurrence Material is occurrence-specific established meaning. It remains semantically separate from
the Canonical Contract World unless a future owning law establishes an explicit relation requiring otherwise. That
separation does not prescribe a second object store, occurrence repository, or dedicated allocation domain.

```text
Established Input Definition Material
    -> made available through the Canonical Contract World

Established Input Occurrence Material
    -> remains occurrence-specific established meaning

Established Input Semantic Protocol
    -> legal observation of the Input-owned Definition and Occurrence projections
```

These placement and observation rules do not mandate a storage topology. Physical retention, sharing, and reclamation
remain realization concerns. Reclaiming physical representation for an occurrence does not mean the semantic
application was never established, and retaining physical representation does not extend semantic authority by itself.

For Input-owned relations, legal observation keeps `relation not owned`, explicitly permitted absence, present relation,
and required-but-unresolved relation distinct. A required unresolved coordinate or relation cannot appear as successful
Established Input meaning. Likewise, the fact that a future Input Occurrence does not yet exist is not incompleteness of
the Established Input Definition. Independently complete Input Definitions may become visible without making any one
Definition partially authoritative. Source provenance remains an adjacent relation rather than Definition or Occurrence
identity.

### 9.2. Established Input Semantic Protocol

Input specializes the common ADR-0063 Established Semantic Protocol as the legal observation boundary over
Input-owned Established meaning. The Protocol is not an occurrence object model, DTO layer, storage schema, or mandatory
copy boundary. It exposes these typed reference domains:

```text
EstablishedInputDefinitionRef
EstablishedInputOccurrenceRef
EstablishedInputCoordinateRef
```

The Input projection catalog is:

```text
Established Input Definition Projection
Established Input Occurrence Projection
Direct Established Relation Projection

Fine-Grained Established Input Projections
    Input Definition Coordinate Surface Projection
    Input Definition Presentation Law Projection
    Established Input Presentation Projection
    Input Occurrence Outcome Projection
    Input Direct Coordinate Value Projection
```

Input has no additional authority-specific Established Material family that requires a separate projection category.
Definition and Occurrence material are sufficient for the Input-owned meaning closed by this ADR.

### 9.3. Projection Completeness and Availability

Each Established Input Projection is complete only for its declared observation:

| Projection                                    | Complete observation and availability                                                                                                                                                                                                                        |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Established Input Definition Projection**   | Exact `EstablishedInputDefinitionRef`, Owning Input Authority Binding, Version Binding, complete Input Definition Meaning, and all direct established coordinate relations of that Definition.                                                               |
| **Established Input Occurrence Projection**   | Exact `EstablishedInputOccurrenceRef`, its Definition and Interaction relations, its result, and the Established Presentation relation only when the result is `Presented`.                                                                                  |
| **Established Input Presentation Projection** | Available only for `Presented` occurrences. Exact Occurrence, Definition, and Interaction References and the complete immutable Established Presentation relation.                                                                                           |
| **Input Direct Coordinate Value Projection**  | For one direct coordinate of a `Presented` occurrence, exact `EstablishedInputOccurrenceRef` and `EstablishedInputCoordinateRef`, explicit presence state, and the presented value when present. Nested constituents do not become direct Input coordinates. |

When the Established Input Presentation Projection is unavailable, the occurrence result must still be observed through
the Input Occurrence Outcome Projection. Projection equality follows each declared observation; equal payloads do not
merge Definition or Occurrence References.

Observing Input projections does not transfer Input or Publication authority. Semantic absence is distinct from
unretained physical representation, delayed physical materialization, unsupported observation machinery, or corrupt
storage. Failure to provide a required legal observation is a compiler-side availability problem under Section 9.6 and
ADR-0074, not Input absence or Refusal.

### 9.4. Direct Relations and Consumer Law

Input owns only these direct established relation families:

```text
Established Input Definition
    -> direct Input coordinate

Established Input Occurrence
    -> exact Established Input Definition

Established Input Occurrence
    -> exact Interaction
```

Contract World selection of an Input Definition belongs to the selecting authority, and subsequent relations toward
Lowering belong to Lowering. Consumer navigation does not change those ownership directions.

A later inbound Contract may consume the Established Input Presentation Projection only when the selected composition
makes it that Contract's direct semantic input. Canonicalization may be that consumer when selected; otherwise Admission
may consume it under its own law. No consumer may bypass an intervening established result, reopen the host carrier, or
rederive Input meaning from Source or host operations.

Lowering may observe Definition-level Input coordinates where its own law requires them. Its occurrence-time value
source
remains governed by Lowering and the selected composition. Diagnostics may use Occurrence or Outcome projections;
generated API formation, Verification, and PBT planning may use Definition projections when legal.

Consumers observe Input through its producer-owned projections. Any additional judgment belongs to the responsible
consumer or derived-result producer, not to Input or an Established Input Projection. A compiler product dependency does
not create an Input authority relation.

### 9.5. Reuse and Current Validity

Input Definition and HIR work may be reused when current validity is established under ADR-0071 and ADR-0063. A retained
Definition or projection is reusable only when the current Input authority and Version still correspond, every
meaning-determining input remains valid, every direct semantic reference still denotes the required exact meaning, and
the complete direct coordinate membership and presentation law remain unchanged.

Because an Input Definition claims a complete direct coordinate set, reuse validation must cover negative space as well
as surviving members. It must establish that no new direct coordinate was added and no required direct coordinate was
removed; validating only the old coordinates is insufficient.

Occurrence authority follows a different law. A fresh Input boundary entry never reuses an older occurrence as the new
semantic occurrence, even when the same Definition and Presentation yield the same deterministic result. Realization may
reuse validated non-authoritative computation or physical representation under Section 9.6, but such reuse cannot
substitute an older `EstablishedInputOccurrenceRef` for the fresh application or make two Occurrence References equal.

Conversely, duplicated physical computation of one already-identified exact occurrence does not create two semantic
occurrences. Parallel or speculative realizations may converge on the same legal occurrence result and Protocol
observation when they are evaluating that same exact application.

Clean formation and every validated reuse or reconstruction path must produce the same legal Input
Protocol observations. Cache state, fingerprints, HIDs, dependency topology, or retained bytes may
justify work avoidance only through separately validated current-validity evidence; they do not
create Input meaning or authority.

A cache miss or rejected reuse attempt is not itself an Input or compiler unsuccessful result. The compiler may return
to
the clean deterministic path when retained material cannot be validated. Successfully decoding or locating persisted
bytes likewise does not establish current semantic validity without the checks above.

### 9.6. Coherent Observation, Representation Freedom, and Verification

One legal Input Protocol observation must be coherent. A consumer cannot combine a Definition from one semantic
generation with a stale presentation-law projection or coordinate relation from another merely because old physical
representation remains reachable. Cross-generation physical reuse may participate only after current validity has
established one coherent logical observation.

Protocol crossing does not require a dedicated object or full copy. The physical realization behind
Resolved HIR and Established Input Protocol observations is replaceable, provided it preserves the
same legal observations. Physical sharing does not merge semantic entities or turn a storage
coordinate into an Input semantic reference.

Lazy physical materialization is permitted only after the semantic unit and its legal membership are already complete. A
consumer cannot treat unresolved semantic meaning as a value that will become complete on first access. Typed reference
kinds remain distinct even when their physical handles use the same width, and stale generation-local handles or
wrong-kind references must fail closed rather than resolve through unchecked numeric substitution.

Every optimized or reused path remains checkable against the clean deterministic path and the
producer-owned Input Protocol laws. The conformance and adversarial-testing strategy remains
Verification or Design work. Malformed or resource-hostile input and physical representations must
not be handled by weakening Input semantics. Semantic bounds remain distinct from compiler Budget and
resource-safety limits.

If already-Established Input meaning exists but compiler machinery cannot supply a required legal observation, that is a
compiler availability problem rather than an Input Refusal. The observation failure does not revoke or rewrite the
already-Established Input meaning. The exact common unsuccessful-result representation remains owned by Proposed
ADR-0074.

### 9.7. Non-Normative Compiler-Native Access Sketch

The semantic laws above do not require an object graph. A V1 realization may use a shape such as the following without
making these arrays or handles Contract meaning.

```text
candidate_handle = current_dense_handle[input_definition_candidate_ref]

coord_base  = input_candidate_coord_base[candidate_handle]
coord_count = input_candidate_coord_count[candidate_handle]

for i in coord_base ..< coord_base + coord_count:
    coord_ref      = input_candidate_coord_ref[i]
    presentation   = input_candidate_coord_presentation[i]
    consume legal HIR observation

input_definition_judgment(candidate_handle)
    -> established_definition_ref
    or Input Definition Refusal
```

Occurrence realization may likewise use flat typed relations rather than an occurrence object. The following is one
non-normative columnar/slab-shaped realization of the same semantic protocol.

```text
definition_ref = established_input_definition_for_interaction[interaction_ref]
occurrence_ref = fresh_input_occurrence_ref()
occurrence_handle = current_dense_handle[occurrence_ref]

result = judge_input_presentation(
    definition_ref,
    submitted_material
)

input_occurrence_definition_ref[occurrence_handle] = definition_ref
input_occurrence_interaction_ref[occurrence_handle] = interaction_ref
input_occurrence_result[occurrence_handle] = result

if result == Presented:
    presentation_ref = retain_or_reuse_immutable_presentation(submitted_material)
    input_occurrence_presentation_ref[occurrence_handle] = presentation_ref

expose legal Established Input Occurrence Projection
```

The arrays, handles, and presentation references in this sketch are physical realization only. A different realization
may replace them without changing the Contract meaning or the producer-owned Protocol observations. Any later legal
consumer reads the presentation through the Established Input Presentation Projection rather than through these physical
columns or the carrier used to form them.

## 10. Open in This Section

No further V1 Input semantic question is left open by this ADR. Sections 4, 5, and 9 define its HIR, Establishment,
Established Material, and Semantic Protocol boundaries. Remaining implementation and integration work must preserve the
closed Input law.

```text
Input / ADR-0064
    HIR Bundle                     -> DEFINED
    Establishment Bundle           -> DEFINED
    Established Protocol           -> DEFINED
    Input Semantic Status          -> CLOSED
    Common Compiler-Result Detail  -> PROVISIONAL-COMMON / ADR-0074
    Physical Realization           -> DESIGN
```

Remaining work is owned outside Input semantics:

- **Compiler Result (ADR-0074, Proposed):** Common unsuccessful-result and recovery representation. Input retains the
  distinction between compiler-side non-entry, Definition Refusal, and occurrence-time `Refused`.
- **Authoring and API:** Final Input authoring tokens, public Java/Kotlin carrier syntax, the exact V1 set of concrete
  bounded Leaf variants, and the generated Host API strategy for Policy-variable Input. Distinct Input Definitions
  selected by different Contract Worlds need not share a host carrier.
- **Platform (ADR-0073):** The exact Java/Kotlin direct-support matrix for Input Leaves and carrier operations. Legal
  Input meaning does not by itself make a host carrier or operation legal.
- **Interface, Policy, Governance, and HIR:** Exact Contract World selection and its HIR representation. That relation
  must not be used to reconstruct missing Input Definition Meaning.
- **Design:** Physical HIR representation, reusable Leaf-law representation, and compiler optimizations. None changes
  Input meaning or introduces a user-selectable Input equality or normalization law.

Concrete V1 bounded Leaf variants remain subject to the complete closure requirements of Section 6.9.5 before Visible
HIR. A Candidate must retain all resolved meaning and exact semantic bases needed by its presentation law. Compiler
comparison acceleration, including hashes or indexes, must be validated against that exact law.

Future presentation-family additions must preserve Input's closed meaning and external-authority boundaries. Host
behavior,
snapshot timing, open-world evolution, and backend representation cannot become Input meaning through such an extension.

## 11. Consequences

Input establishes an immutable boundary presentation with a flat direct coordinate surface. Each coordinate may contain
a finite, closed, acyclic structured value. Host carriers remain evidence rather than Contract authority, and physical
representation and generated APIs remain replaceable when they preserve the same Input meaning.

Input owns the Presentation Sameness, uniqueness, and observable order of its declared values. Host equality or ordering
cannot redefine those laws. Canonicalization may explicitly establish a different equivalence and representative;
without it, no such relation is implied.

Changes to meaning-determining presentation laws or semantic bases change Input meaning. Source, library, or provider
changes that preserve the exact resolved meaning remain realization. An application carrier that cannot preserve Input
meaning must be adapted into a legal presentation before invocation or use an ADR-0073-qualified direct mapping.

Every fresh boundary entry establishes a distinct Input Occurrence, even when an earlier occurrence has the same
presentation and result. Later Fact identity may converge without merging those occurrences. Validated Definition or
physical computation reuse cannot replace a fresh Occurrence identity. HIR and Established Semantic Protocols let
consumers observe permitted Input meaning without reconstructing it from source or host carriers, while physical sharing
remains separate from Contract identity.