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

Input is the first one-dimensional Contract of the inbound airlock.

The primary semantic subject of Input is one complete inbound presentation Contract. It states the presentation meaning
that outside material must realize at an Interaction boundary.

The Input Contract is not the Interaction that selects it, the Operation that may later consume material derived from
it, the host carrier that expresses source evidence, or the binding relation that connects it to one Interaction.

Admission still decides whether a successfully presented value may continue. Canonicalization may establish a stable
same-shape representative when selected. Lowering later owns the explicit relation from Input coordinates toward
Operation-parameter and Fact coordinates.

A host declaration may already state useful external contract evidence. Closed scalar values, finite alternatives,
presence distinctions, bounded presentations, and other explicitly defined Input forms do not become silent merely
because Kotlin, Java, or another frontend carries them.

The host declaration and its object instances do not receive Kontrakt authority directly.

The declaration nominates external evidence. Kontrakt must refine and ratify that evidence before it becomes Input
authority.

## 2. Problem

The boundary must accept ordinary external presentations without allowing the host representation to become Contract
authority.

Host mechanics can influence what a program happens to observe even when the declared data appears unchanged. Input
therefore needs a refinement boundary that removes those mechanics before authority is granted. Sections 7.3 through 7.7
define the relevant immutability, carrier, inheritance, presence, and opaque-value rules.

A supported host declaration may already express one Input presentation completely. In that case the frontend should
refine that declaration rather than require a second Kontrakt-specific DTO that restates the same Contract evidence.

The V1 boundary therefore needs a narrow ratifiable presentation law. The result must be finite and directly inspectable
before Admission, while material that cannot meet that law must be formed into a supported presentation before
invocation.

## 3. Decision Drivers

Input is presentation, not truth. Section 7.8 owns that authority boundary.

Source evidence may nominate an Input Contract, but only Input-owned Establishment grants authority. The runtime
boundary therefore receives an already-formed presentation rather than choosing when or how a live object should be
captured.

V1 keeps the directly addressable Input coordinate surface flat under Section 4.2. A direct coordinate may contain a
closed value topology under Section 7.9, but nested constituents do not become nested Contract authorities or additional
direct Input coordinates.

The owning Input law alone determines Definition Meaning. Selection context remains outside that meaning unless this ADR
explicitly makes the context determinant. Section 5.4 closes that split.

Several Interactions may select one Input Definition when they intentionally require the same complete Contract meaning.
Section 4.4 permits that relation, while Section 8 prevents reuse from becoming an authoring objective.

Definition-time refinement must be deterministic. Section 9 distinguishes compiler-side non-entry from Input-owned
Definition Refusal and occurrence-time `Refused`, and Admission remains unreachable after a Refused Input occurrence.

Policy, Governance, Budget, and Capacity retain their own authority when they independently apply. Different declared
Policy Worlds may select different Input Definitions for one Interaction under Section 4.4; the Governance API that
makes an applicable world usable by an external caller remains outside this ADR.

## 4. Decision

### 4.1. Input Meaning

Input is the boundary presentation Contract.

```text
outside presentation evidence
-> Resolved Input Definition Candidate
-> Input Definition judgment
-> Established Input Definition
-> one fresh already-formed immutable Input boundary application
-> Input Occurrence judgment
-> Established Input Occurrence
-> Admission when the occurrence result is Presented
```

Input declares the complete finite presentation surface that may appear at the Interaction boundary.

It preserves only the distinctions that the Input law makes observable. Later Contracts may observe those distinctions
through their own lawful relations, but they do not retroactively add Input meaning.

Input does not decide whether presented values may continue. It does not canonicalize them, lower them into core Fact
meaning, or invoke the user Operation.

### 4.2. Flat V1 Presentation

V1 keeps the directly addressable Input coordinate surface flat.

The presentation exposes one finite set of direct Input coordinates. Each direct coordinate may itself carry a finite,
closed, acyclic value presentation under Section 7.9.

Nested members, alternatives, elements, keys, and values are constituents of that coordinate's Input meaning. They do
not become nested Input Definitions, independent Input Occurrences, or additional direct Input coordinates merely
because the presentation has internal structure.

A host carrier may contain richer topology, but Input does not recursively adopt host object ancestry, alias structure,
runtime implementation graphs, or lifecycle relations. Section 7.9 owns the legal value topology. Section 7.4 owns
carrier separation.

Material whose complete presentation cannot be resolved under those laws must be formed into a supported declared
presentation before it reaches the Input boundary.

### 4.3. Source Evidence Is Not Authority

A supported Java or Kotlin declaration may nominate Input evidence.

Section 7.4 owns the carrier-and-Contract separation law. Section 7.11 owns the direct-carrier exception for a host form
that already realizes a legal Input presentation.

A different frontend may resolve to the same Input Definition Meaning without making frontend syntax, host type
identity, or source location part of that meaning. Section 5 owns the separate laws for Definition identity, semantic
equality, and physical reuse.

### 4.4. Input Definition and IDL Selection Binding

ADR-0071 separates Definition Candidate meaning from the IDL Binding Candidate that selects it. Input uses that common
separation and does not introduce a second selection model.

An Input Definition Candidate contains the complete resolved Input presentation meaning described in Section 5. An Input
IDL Binding Candidate records one exact frontend selection occurrence that selects that candidate for the Input role.

The selecting semantic context follows the Contract World authoring form already owned by the Interface and Policy laws.
An Interface without Policy has one static Contract arrangement. An Interface with named Policies has a separate
complete Contract World declaration under each exact Policy Definition Candidate. This ADR does not introduce a
universal `ContractWorldRef` or another authority between those declarations and their selections.

Under ADR-0054, a Policy Definition Candidate preserves the exact constituent Contract selections that determine its
Contract World meaning. For Input, the pre-authority relation therefore reaches one exact Input Definition Candidate
Reference. The relation does not establish the Input Definition and does not transfer Input authority into Policy. After
the independently owned establishments complete, the authoritative Policy World preserves the corresponding exact
established Contract relation under ADR-0054 and ADR-0063.

Conceptually, each legal form expresses one exact Input selection relation.

```text
static Interface arrangement
    Interaction A / Input
        -> exact Input Definition Candidate D

Policy Definition Candidate P
    Interaction A / Input
        -> exact Input Definition Candidate D
```

The Binding Candidate preserves only the semantic context required to identify that exact selection occurrence. It also
preserves the exact Interaction, the Input role, and the exact target Input Definition Candidate Reference when those
distinctions are not already carried structurally by typed HIR references. Physical encoding does not create a universal
Binding record.

One exact selection occurrence has one exact target. Duplicate declarations or competing targets for the same exact
selection position are invalid; declaration order, source order, HID order, or another realization order does not
arbitrate the conflict.

Several legal Input IDL Binding Candidates may select the same Input Definition Candidate when they intentionally select
the same complete Input Contract meaning. This permission does not make shared Input extraction a default authoring
pattern.

Different declared Policy Worlds may also select different Input Definition Candidates for the same Interaction. Those
are different Contract World selections and may lawfully denote different Input Contracts. They do not merge merely
because they occupy the same Interaction Input role.

The Binding Candidate owns the selection relation. It does not copy the Input Definition payload. The selecting
Interaction does not become an Input Definition determinant merely because it participates in the selection. A
containing Policy context, when present, likewise does not transfer Policy authority into the Input Definition.

This ADR does not decide how Governance exposes or acquires the applicable Policy World for an external caller, nor how
a generated external API projects Policy-variable Input. Section 11 leaves that Governance and Interface-realization
question open without restricting the Policy-owned selection law above.

Operation remains the separate action or realization-callback Contract surface. Lowering owns any later relation from
direct Input coordinates to Operation parameters or Fact coordinates.

## 5. Definition Establishment

### 5.1. Resolved Input Definition Candidate

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

### 5.2. Complete Input Candidate Meaning

The Input Definition Meaning is the complete Input-owned presentation meaning defined by this ADR.

The Candidate preserves the complete direct coordinate surface required by Sections 7.1 and 7.2. Section 7.6 owns
presence and absence. Section 7.8 owns form-defining bounds. Section 7.9 owns the closed value-presentation algebra,
nested constituent law, collection observation law, and closed Leaf-presentation boundary.

A structural distinction enters Candidate meaning only when the owning Input law makes that distinction observable. The
Candidate therefore retains the exact presentation family, constituent topology, presence law, form-defining
cardinality, Presentation Sameness law, and any order-observation obligation required by Section 7.9. Membership
uniqueness and Association key uniqueness are determined by the applicable constituent or key Presentation Sameness
rather than by a separately selectable Input equivalence law. Compiler observation order, physical storage order, host
traversal order, host equality, and backend hashing never substitute for those laws.

When one of those laws depends on an exact semantic basis or another exact semantic reference, the Candidate retains
enough resolved meaning or exact legally owned reference material to interpret that law before Visible HIR. It cannot
postpone unresolved relation meaning to arbitrary later traversal, ambient platform state, or executable host behavior.

The Candidate contains candidate-specific meaning rather than a property bag of common invariants. A common Input rule
should be enforced by HIR formation or its typed projection when that is sufficient.

### 5.3. Information Retention and Loss

Frontend resolution may erase source or host details after their contribution to Input meaning has been resolved
completely. Section 7.4 identifies carrier mechanics that do not survive into Contract meaning, and Section 7.11 applies
that rule to the zero-adapter path.

An Input-visible distinction cannot be erased when Establishment or another legal HIR observer would then need to reopen
source to recover it. Sections 7.1, 7.6, 7.8, and 7.9 own the semantic distinctions that must survive when applicable.

Visible Input HIR cannot use a recovery placeholder in place of required meaning. It also cannot silently prune an
undeclared semantic coordinate. If richer external material must be projected into the declared Input surface, that
transformation happens before Input unless a future Input law explicitly owns it.

Compiler mechanisms used to acquire, order, index, compare, or verify the source remain realization unless an owning
Input law explicitly makes their result observable.

### 5.4. Definition Meaning Determinants

Input Definition Meaning is determined only by Contract-visible material that this ADR makes part of the declared
presentation.

The direct coordinate surface is a determinant under Sections 7.1 and 7.2. Presentation-family and constituent meaning
are determinants under Section 7.9 whenever changing them would change the Input-visible presentation. Presentation
Sameness is determined by that complete Input-owned meaning. An ordering law or exact semantic basis is an additional
determinant only when Section 7.9 makes that distinction part of the presentation meaning. An implementation or provider
version is not a determinant merely because it realizes an unchanged semantic law.

The surrounding selection context is not a determinant merely because it selects or can later consume the Input. The
exact selecting context required by Section 4.4 remains Binding meaning. The selecting Interaction therefore does not
enter Input Definition Meaning merely because it occupies that relation. When a Policy Definition Candidate provides the
Contract World declaration context, that Policy context also remains outside Input Definition Meaning. The later
Operation and Lowering targets remain outside Input Definition Meaning under Section 10.

Source provenance remains separate from semantic meaning under ADR-0071. Host identity and host behavior remain outside
meaning under Section 7.4. Compiler lookup and reuse identities remain realization under ADR-0071 and ADR-0063. The
common HIR law also excludes ambient compiler or process state from silently becoming an Input determinant.

### 5.5. Definition Identity, Version, and Candidate Reference

ADR-0053 owns Contract Authority continuity and Contract Version semantics. ADR-0063 owns authoritative Definition
identity and Definition Reference. This section states only the Input specialization required by those common laws.

One explicitly declared Input Contract denotes one Input Contract Authority. In the current Input model, one Input
Authority owns one complete Input Definition for each Contract Version. Input therefore requires no additional
Authority-Local Definition Coordinate.

```text
exact Input Contract Authority
+
exact Contract Version
    ↓
exact versioned Input Definition
```

The same exact Input Authority and Version cannot establish conflicting Input Definition Meaning. Equal Definition
Meaning under different Authorities or different Versions does not merge those Definitions. ADR-0063 and ADR-0053 own
the common conflict, Version, and history rules.

Before Establishment, the corresponding Input Definition Candidate Reference designates the exact resolved Input
Contract Authority coordinate together with the exact authority-scoped Resolved Version Candidate Coordinate. The Input
reference kind is preserved by the typed HIR reference domain; it need not be repeated as a generic scalar tag.

The authored Version Claim is resolved to the exact authority-scoped Resolved Version Candidate Coordinate before the
Input Candidate becomes visible. That coordinate participates in exact versioned candidate designation rather than in
the presentation payload described by Section 5.2. Establishment forms the authoritative Version Binding and Definition
Reference under ADR-0063; a Candidate Reference is not cast or promoted into a Definition Reference. ADR-0053's
prohibition on implicit Version selection applies unchanged.

A change to Contract-visible Input presentation meaning requires a new Contract Version when the user continues the same
Authority. This includes a change to Presentation Sameness, an ordering law, or semantic basis material whose change can
alter the resolved Input meaning. A frontend-only, carrier-only, library-only, or provider-only change that preserves
the exact resolved Input meaning does not create a new Input meaning merely because realization changed. ADR-0053
remains the owner of rename, replacement, continuity, and history rules. Current HIR identity is independently resolved
from current semantic inputs; it does not inherit identity from a previous compiler generation under ADR-0071.

### 5.6. Semantic Equality, Presentation Sameness, Merge, and Physical Reuse

Input Definition Meaning equality is producer-owned. Two meanings are equal only when every Input-owned determinant is
equal under the laws that define that meaning.

Section 7.9 separately defines Input Presentation Sameness for values judged under one exact presentation meaning.
Membership uniqueness and Association key uniqueness use that same applicable Presentation Sameness law; Input does not
introduce a second user-selectable equality or equivalence relation for those purposes. Presentation Sameness does not
establish Candidate Reference equality, authoritative Definition identity, Occurrence identity, Fact sameness, or
Canonicalization equivalence. ADR-0053 likewise keeps different Contract Versions distinct even when their
contract-specific presentation meaning happens to compare equal.

Input defines no implicit semantic merge from structural equality. Section 4.4 covers the different case in which
several Binding Candidates deliberately select one already-identified Input Definition.

Physical reuse remains available to the compiler. ADR-0071 and ADR-0063 govern the rule that caching, hashing,
interning, or representation sharing cannot create semantic equality or Contract identity.

### 5.7. Definition Establishment Judgment and Result

An Input Definition judgment legally enters only after the complete Input Definition Candidate is available as valid
Visible HIR. The judgment consumes the Input Definition Candidate meaning through the Resolved Input HIR Candidate
Protocol defined in Section 5.8 together with the exact authoritative Version prerequisite permitted by ADR-0053 and
ADR-0063. The Input IDL Binding Candidate is not a Definition Establishment input.

The judgment has two Input-owned semantic results.

```text
complete resolved Candidate
+ legal Input Definition meaning
    -> Established Input Definition

complete resolved Candidate
+ meaning that violates the Input Definition law
    -> Input Definition Refusal
```

The judgment applies the already-resolved Presentation Sameness, collection uniqueness, order-observation law, and any
exact semantic-law reference required by Section 7.9. It does not discover those meanings by calling host equality, a
comparator, a provider, or another runtime behavior.

Failure to form valid Visible HIR is different. Unresolved source, recovery material, poison, a dangling semantic
reference, or another HIR-seal failure prevents the Input Definition judgment from legally entering. Such a case is a
compiler-side unsuccessful result owned by the applicable frontend or HIR judgment rather than an Input Definition
Refusal. The exact common compiler-result representation remains outside this ADR and currently depends on Proposed
ADR-0074.

Successful Establishment preserves the exact Definition Meaning and identity relations required by ADR-0063. Compiler
acquisition, planning, storage, visibility, and code generation may use replaceable representations, but none of those
representations creates a second Input authority path.

### 5.8. Resolved Input HIR Candidate Protocol

Input specializes the common ADR-0071 Resolved HIR Candidate Protocol without creating another IR, semantic stage,
serialization layer, or mandatory copy. The Input-facing Protocol contains these typed semantic reference domains when
that reference is required by the legal observation:

```text
ContractInterfaceRef
InteractionRef
InputDefinitionCandidateRef
InputIDLBindingCandidateRef
ResolvedInputCoordinateRef
```

`ResolvedInputCoordinateRef` identifies one direct Input coordinate under one exact Input Definition Candidate. Its
semantic form is the exact Candidate Reference plus the exact nominal direct coordinate defined by Section 7.2. A source
ordinal, constructor position, HIR row, dense handle, table offset, or storage address cannot substitute for either
part.
Nested members, alternatives, elements, keys, and values do not automatically enter this direct-coordinate reference
domain.

The complete Input Protocol catalog is:

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

The **Input Candidate Coordinate Surface Projection** is complete for the direct externally addressable coordinate
surface
of one exact Input Definition Candidate. It preserves each direct nominal coordinate and the complete presentation
meaning required to interpret that coordinate.

The **Input Candidate Presentation Law Projection** is complete for the presentation topology owned by that Candidate,
including presence and absence, form-defining cardinality or extent, Presentation Sameness, collection uniqueness,
observable collection order, closed Leaf laws, and exact semantic-law references when those references determine Input
meaning.

The **Input Binding Target Projection** is complete for one exact Input selection occurrence and its one exact target
Input Definition Candidate. It does not copy the Definition Candidate payload.

Projection semantics are producer-owned. Full Candidate semantic equality follows Section 5.6. A fine-grained projection
compares only the exact Input observation for which that projection is declared complete. Equal projection payload does
not merge Candidate References, Binding Candidate References, or later authoritative Definitions. Omitted material is
not
semantic absence.

One logical projection does not imply one object, query, cache entry, table, allocation, or physical range. Several
projections may share immutable backing, and one bulk physical read may serve several legal projections while preserving
their distinct semantic observation boundaries.

### 5.9. Input HIR Seal and Establishment Handoff

Visible Input HIR must already satisfy the common ADR-0071 resolution and visibility laws. For Input specifically, a
Definition Candidate cannot become visible until the exact Input authority and Resolved Version Candidate Coordinate are
known, its complete Definition Meaning and direct coordinate surface are closed, every required semantic reference is
exact, and the presentation topology required by Section 7.9 is complete.

The corresponding Input Binding Candidate must identify its selecting context, Interaction, Input role, and exact target
Candidate without a competing target for the same exact selection occurrence. Recovery nodes, poison, unresolved
placeholders, and incomplete semantic material are not legal Input HIR.

HIR seal verification does not decide whether the resolved Candidate is a legal Input Definition. A fully resolved,
coherent Candidate may pass the HIR seal and still receive Input Definition Refusal under Section 5.7. Conversely,
material that cannot satisfy the HIR seal does not enter that Input judgment.

Establishment may derive Input authority only from legal Resolved HIR observations, separately Established authoritative
prerequisites admitted by the Input identity law, and the explicit Input judgment input. It must not reopen authored
source, traverse host topology, consult query or cache topology, infer meaning from physical HIR containment, or obtain
an ambient mutable value to complete Candidate meaning.

Input adds no separate HIR lifecycle or Protocol-evolution model. ADR-0071's visibility, generation, stale-reference,
coherence, extension, migration, and compatibility laws apply unchanged. An unknown required Input semantic extension
fails closed unless ADR-0071 permits a compatible projection or validated migration that preserves the required Input
observation.

### 5.10. Required Basis and Applicability Boundary

Input Definition meaning does not own an ADR-0063 Required Basis Law. Input also does not own Basis Resolution, Basis
Binding, Complete Basis, a separate Input Applicability judgment, Input arbitration, or a separate Input Composition
judgment. Those checklist categories are `NOT-APPLICABLE` for the current Input law.

An exact semantic law or semantic reference may still determine a closed Leaf, ordering, or other Input presentation
law.
When it does, that exact law is direct Input Definition meaning and is preserved by the Candidate under Sections 5.2 and
5.4. It is not a request to find an Established Basis later at occurrence time.

The established Contract World relation that selects the exact Input Definition for one Interaction is also not an Input
Basis or Input Applicability result. Interface, Policy, and Governance retain ownership of selection and governing
applicability. Once that relation supplies the exact Established Input Definition, the Input occurrence judgment does
not
reselect the Policy World or copy Policy, Governance, State, Budget, Capacity, or an ambient run context into Input
meaning merely because those values exist elsewhere in the machine.

Any exact semantic-law reference that determines Input Definition meaning participates in the common ADR-0063 semantic
prerequisite law. Input does not introduce a fixed-point or cyclic establishment exception merely because the referenced
law is represented compactly in HIR; a semantic dependency cycle remains illegal. Compiler query or analysis cycles do
not become Input dependency for that reason.

### 5.11. Established Input Definition Material

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

The complete Definition Meaning is the same source-owned meaning closed by Sections 5 and 7: the direct coordinate
surface, presentation topology, presence and absence, form-defining cardinality or extent, Presentation Sameness,
collection uniqueness, observable collection order when owned, closed Leaf laws, and exact semantic-law references when
those references determine the presentation.

Each direct coordinate may be observed through an `EstablishedInputCoordinateRef`. Its semantic form is the exact
`EstablishedInputDefinitionRef` plus the exact nominal direct Input coordinate. The compiler may encode that reference
with a dense handle, packed value, slab row, or another current representation, but the encoding is not the coordinate
identity and cannot make nested constituents into direct Input coordinates.

Input establishes no third Input-specific material family beside Definition and Occurrence material. Selection bindings,
world material, Basis material, and application-context containers remain owned by their respective authorities or are
not semantic material at all.

## 6. Invocation Boundary

For one applicable Interaction, the established Contract World relation identifies the exact Established Input
Definition
that governs that Interaction's Input boundary. Invocation does not query or reinterpret the pre-authority Input IDL
Binding Candidate.

An Interface without Policy obtains the relation from its static Contract arrangement. When Policy participates, the
applicable established Policy World supplies the corresponding relation under the Policy and Governance laws. Section
4.4 permits different declared Policy Worlds to reach different Input Definitions for the same Interaction. Input does
not own that selection.

### 6.1. Occurrence-Time Input Presentation Judgment

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

`Presented` means that the exact complete immutable presentation satisfies the applicable Input law. The occurrence then
owns that Established Presentation as Input meaning, and Admission may judge that occurrence-bound presentation.

`Refused` means that the Input judgment was legally entered but the supplied material did not satisfy the declared Input
presentation law. The occurrence establishes the refusal result, but no Input-authoritative presentation is established
from the malformed or incompatible material. Admission is not reached.

The judgment includes the presentation-level uniqueness and relation obligations owned by Section 7.9 when they apply.
It does not decide whether an otherwise correctly presented value may continue. Section 7.8 owns that boundary between
Input and Admission.

Carrier formation and the separation between carrier mechanics and Contract meaning follow Section 7.4. Immutability
follows Section 7.3. Input does not repair, snapshot, normalize, or reinterpret material that reaches the boundary in a
form those laws reject.

Definition-time verification establishes that a declaration can produce a legal Input Definition. It does not authorize
arbitrary runtime material merely because the host type appears compatible. Occurrence-time judgment uses only the exact
Established Input Definition reached through the applicable established Contract relation; it does not reconstruct that
definition from source, HIR Binding material, graph state, cache state, or backend representation.

### 6.2. Input Occurrence Identity and Fresh Re-Entry

Every legally entered Input application has one typed `EstablishedInputOccurrenceRef` identifying that exact semantic
application. Occurrence identity is nominal application identity owned by Input. It is not computed from presentation
value equality and is not supplied by a JVM invocation object, compiler query, thread, timestamp, cache entry, object
identity, presentation hash, or another realization coordinate.

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

### 6.3. Established Input Occurrence Material

A legally entered Input judgment establishes one complete Input Occurrence result.

For a successful presentation:

```text
Established Input Occurrence O
    OccurrenceRef
        -> O
    DefinitionRef
        -> exact Established Input Definition
    InteractionRef
        -> exact Interaction whose Input boundary was entered
    Result
        -> Presented
    Established Presentation
        -> exact complete immutable Input presentation
```

For a refusal:

```text
Established Input Occurrence O
    OccurrenceRef
        -> O
    DefinitionRef
        -> exact Established Input Definition
    InteractionRef
        -> exact Interaction whose Input boundary was entered
    Result
        -> Refused
```

The occurrence preserves only direct Input-owned attribution that actually determines its meaning. The current Input law
has no Required Basis or Input-owned Applicable Context under Section 5.10, so it does not copy Policy World,
Governance Binding, State, Budget, Capacity, run state, or the surrounding transitive semantic world into every
occurrence.

`InteractionRef` is direct occurrence attribution because one Input Definition may be used by several Interactions and
the occurrence belongs to one exact Interaction Input boundary. A separate Input-role field is unnecessary in the
occurrence meaning: the semantic category is already an Input Occurrence and the Interaction relation identifies the
boundary to which it belongs.

A Refused occurrence does not retain the raw mutable carrier, malformed partial presentation, thrown exception, or
runtime object as Input-authoritative payload. Separate Diagnostic or provenance products may preserve permitted
evidence under their own laws without turning that evidence into Input meaning.

Once established, the occurrence's Definition, Interaction, result, and any Input-owned determining attribution are
fixed for that occurrence. A later Version, Policy, Governance, State, world replacement, retry, or compiler generation
may affect a later application, but it cannot rewrite the earlier Input Occurrence.

After a Presented occurrence is established, later inbound processing may change physical representation only through
the declared Contract relations that own those changes. The material eventually supplied to the user Operation must
preserve the meaning lawfully derived through the complete declared inbound pipeline. Undeclared substitution, mutation,
reinterpretation, or introduction of different meaning is not a valid realization.

The compiler and runtime must prove or check the required integrity at the boundaries where the applicable law cannot be
guaranteed otherwise. This obligation does not require one generated reader, wrapper, token, or mandatory recheck
strategy. Equivalent realization may use static proof, generated checks, controlled immutable material, specialized
invocation, or another mechanism that preserves the same Contract result.

Policy, Governance, Budget, Capacity, and other independently applicable authorities retain their own results. Their
negative or stopping results do not become Input Refusal and do not create an Input Occurrence when they prevent the
Input judgment from legally entering.

## 7. V1 Ratifiability Law

The V1 Input boundary accepts only presentation meaning that can be resolved completely before authority is granted. The
resulting surface must be finite, directly inspectable, immutable at the boundary, and closed against hidden host
behavior.

The frontend may use a selected host declaration as evidence when that declaration already expresses the required Input
presentation. This avoids duplicate carrier authoring for one Contract; it does not create a reuse rule between
different Interactions.

Ratification rejects three kinds of hidden meaning. Hidden authority occurs when implementation behavior is allowed to
decide Contract meaning; Sections 7.3 through 7.5 close that route. Hidden choice occurs when one source form admits
more than one Contract interpretation and the compiler selects one implicitly; Sections 7.1, 7.2, and 7.6 close that
route. Hidden movement occurs when observation performs behavior or depends on a live external capability; Sections 7.7
and 7.12 close that route.

Slot selection therefore nominates a candidate but does not guarantee ratification. Sections 5.7 and 9 distinguish
compiler-side non-entry, Input Definition Refusal, and an entered Input Occurrence whose result is Refused.

An Input source candidate is ratifiable only when it satisfies the conditions below.

### 7.1. Coordinate Closure

One exact Input IDL selection occurrence identifies the source material that is refined into its target Input Definition
Candidate under ADR-0071 and ADR-0047. Section 4.4 owns the exact selecting context.

Input does not infer, broaden, replace, merge, or arbitrate that selection. One exact selection occurrence must resolve
to one exact target before the resulting Binding Candidate can become Visible HIR.

Every contract-visible direct Input coordinate must resolve to one complete and closed presentation meaning.

No direct coordinate may remain semantically open, erased, dynamically unresolved, or dependent on runtime type
discovery.

Frontend-specific source forms are admissible only when the frontend can refine them completely into that closed
meaning. Otherwise the Input definition is not established. Java raw or wildcarded types and Kotlin star-projected forms
are examples of source forms that require such complete refinement.

The closed Input surface does not silently accept or prune an undeclared semantic coordinate. Material that contains
additional transport- or application-level structure must be projected into the declared Input presentation before this
boundary unless a future Input law explicitly owns that projection.

### 7.2. Direct Coordinate Identity and Meaning

Every direct Input coordinate has one exact nominal identity within its Input Definition and resolves to one complete
and closed presentation meaning.

The resolved nominal coordinate, not its source declaration ordinal, constructor position, record-component position,
HIR row, table position, storage offset, or traversal order, identifies that direct coordinate. Structural resemblance
also does not create coordinate identity.

Changing a direct coordinate's nominal identity changes Input presentation meaning. Reordering the declarations of the
same nominal coordinates does not change Input meaning unless another explicitly declared Input law independently makes
an order observable.

Before Establishment, the HIR Protocol may refer to one direct coordinate through `ResolvedInputCoordinateRef`. That
reference is semantically anchored by the exact `InputDefinitionCandidateRef` and the exact nominal direct coordinate;
it is not the source ordinal, JVM member identity, dense handle, or physical address. Section 5.8 owns its Protocol
exposure.

Nested presentation members may have Input-local constituent identity under Section 7.9. They do not thereby become
direct Input coordinates, cross-authority coordinate references, or Lowering source coordinates.

Input authority depends on the resolved coordinate meaning, not on the host type used to present it.

A frontend may admit a source form only when it preserves every distinction required by the declared Input meaning.
Primitive types, strings, enum forms, nullable forms, and other host-language categories are frontend evidence, not
Input Contract vocabulary by themselves.

### 7.3. Explicit Immutability

The presentation judged at an Interaction's Input boundary is immutable.

Material participating in an Input occurrence must already provide the complete declared presentation without
contract-visible mutation when it reaches the Input boundary.

Input judges that presentation. It does not create an admissible Input by copying, snapshotting, freezing, repairing,
normalizing, or otherwise transforming mutable incoming material.

Construction, adaptation, copying, or other formation may occur before the Input boundary. Such work remains outside
Input authority.

The immutability obligation covers the complete declared Input presentation, including every constituent value of an
admitted collection or bounded presentation.

The presentation must not depend on mutable backing state, externally mutable aliases, live views, lazy materialization,
proxy activation, or framework lifecycle.

Host-language immutability mechanisms are evidence used by a frontend or realization. They do not define the Contract
obligation.

### 7.4. Carrier and Contract Separation

A host carrier may express Input evidence, but it does not own the resulting Input meaning.

The carrier's runtime identity and storage are realization. The mechanisms that constructed it are also realization,
even when they are convenient for acquisition.

Executable host behavior cannot create Input coordinates or change their meaning. An accessor may expose
already-declared evidence, but observing that accessor does not give its implementation Contract authority. Reflection
or generated access may acquire the same evidence; neither mechanism defines the Contract surface.

A frontend may therefore interpret a supported host form only as evidence for meaning that the Input law already
defines. Section 7.11 covers the case where the same host declaration also serves as the direct runtime carrier.

### 7.5. Inheritance and Polymorphism Boundary

Input meaning must be complete in the selected Input definition.

Inheritance does not supply missing Input meaning. The same rule applies when a runtime subtype or executable dispatch
would be needed to discover the presentation after the definition was established.

A frontend may admit a host source only when it can resolve the complete Input presentation without depending on
inherited semantic material or runtime implementation choice. Host interfaces and default behavior remain evidence at
most; they do not enlarge the authoritative Input definition.

At invocation time, the actual material is judged against that already-established definition. Runtime realization
cannot add or reinterpret coordinates through a more specific implementation type.

### 7.6. Presence, Absence, and Finite Choice

Input may declare presence, absence, and finite alternatives as explicit presentation distinctions.

Input must preserve every distinction required by the declared presentation meaning and must not silently collapse
distinct alternatives.

Presence or absence is Contract meaning only when the Input law declares that distinction. A host-language `null`,
nullable type, optional wrapper, sentinel value, or another source representation does not acquire Input meaning by
itself. A frontend may map such a source form to declared Input meaning only when that mapping is complete and
unambiguous.

Input does not apply defaults. A constructor default, language default, factory default, serializer default, or another
creation-time default remains pre-boundary formation and does not become Input authority. Input judges the actual
presentation supplied at its boundary.

If omission is legal, absence must be an explicit declared alternative. If a concrete coordinate is required, the
supplied presentation must already contain it.

A finite choice must be closed. Every declared alternative must be completely and unambiguously distinguishable under
the Input law. The actual runtime material may not enlarge, replace, or reinterpret the declared alternative set through
subtype discovery or implementation-specific behavior.

How a frontend or backend represents those alternatives is realization.

### 7.7. Opaque Values and External Denotation

Host object-reference identity does not provide Input meaning or semantic identity.

An Input coordinate may carry a declared immutable value that denotes something outside the Input presentation. Input
owns only the declared presentation of that value. The existence, identity, validity, ownership, or behavior of the
denoted target is not established by Input.

A reference-valued Input coordinate does not by itself create a Definition Reference, Occurrence Reference, object
relation, lookup obligation, or dereference authority under ADR-0063.

An opaque value is admissible only when its complete Input-visible presentation distinctions are fixed by an exact Input
presentation law. Opaque treatment does not permit Input to adopt an arbitrary user object while ignoring its behavior,
mutability, lifecycle, inheritance, graph structure, or runtime identity.

A frontend may map a supported host type to that already-defined presentation meaning only when the mapping is complete.
The host type does not define the Input law, and its methods, equality, hashing, parsing, normalization, serialization
behavior, provider access, or other operations do not become Input meaning merely because they are associated with the
carrier.

Input does not establish canonical identity or normalized meaning for an opaque value. Those meanings remain with the
Contract that owns them.

### 7.8. Presentation-Only Authority

Input owns only the distinctions required to determine whether the actual material supplied at the Input boundary
realizes the declared presentation shape.

Sections 7.1, 7.2, 7.6, and 7.9 define the presentation distinctions that Input may own. This section does not add
another shape vocabulary; it only fixes the boundary between presentation authority and later Contract judgment.

A presentation bound constrains the form in which Input material may be presented. It does not decide whether an
otherwise correctly presented value may continue. Structural extent, cardinality, or another bound may therefore belong
to Input when that bound is part of the declared presentation form; value-domain, business, Policy, or
operation-specific admissibility conditions do not.

A value may denote something outside the Input presentation. Input owns only the declared presentation of that value and
does not acquire authority over the denoted target.

Input does not decide whether a correctly presented value may continue. That judgment belongs to Admission.

Input does not establish canonical identity, Fact or core meaning, State or Transition legality, Policy or Governance
selection, Publication authority, Output meaning, or machine movement. Those meanings remain with their owning Contract
authorities or, for machine movement, the State-Machine axis.

### 7.9. V1 Presentation Algebra

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

Their exact authoring tokens and physical HIR encoding remain open under Section 11.

A **Leaf** is a complete presentation meaning that Input does not further decompose into constituent topology. Its
complete legal domain, Input-visible distinctions, and Presentation Sameness are fixed by the Kontrakt-owned Input law.
When that meaning genuinely consumes versioned or external semantic data, the exact meaning-determining semantic law
material must be explicitly ratified and closed rather than supplied by ambient provider state. Section 5.10
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

#### 7.9.1. Presentation Sameness and Input Uniqueness

Every exact Input presentation meaning defines **Presentation Sameness** from the complete set of distinctions that
Input owns for that presentation. Presentation Sameness asks whether two legal values preserve the same Input-visible
meaning. It is not JVM `equals`, Candidate semantic equality, Definition identity, Occurrence identity, Fact sameness,
Canonicalization equivalence, HID equality, serialized-byte equality, or backend representation equality.

Presentation Sameness is intrinsic to the resolved presentation meaning and is fixed by the Kontrakt-owned Input
semantic law. It is not selected by the user through a semantic option, comparator, callback, collation, normalization
policy, or equality strategy. A Leaf obtains its sameness from its exact resolved presentation meaning. Named Product,
Closed Tagged Choice, and Sequence lift constituent Presentation Sameness through their declared structure. Membership
and Association preserve the exact presented constituent or key representatives and every Input-visible order
distinction.

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

A JVM `equals` method, Kotlin equality behavior, comparator object, lambda, callback, object identity, hash function,
collation, normalization routine, ambient locale, current provider state, or backend lookup structure cannot redefine
Input Presentation Sameness or collection uniqueness. A host carrier is usable only when the supported boundary mapping
can preserve every Input distinction required by the Kontrakt-owned presentation law. If the carrier has already
collapsed values that Input distinguishes, direct binding is not a lossless Input presentation and must be rejected or
formed through another legal boundary before Input. ADR-0073 owns that platform-support and preservation gate.

A later Contract may intentionally establish a different relation without retroactively changing Input. In particular,
Canonicalization may explicitly declare an equivalence and establish a stable representative that collapses distinctions
preserved by Input. When Canonicalization is absent, no hidden equality, normalization, or representative law is
inserted; the admitted Input presentation continues unchanged toward the next selected authority. Fact sameness, HIR
semantic equality, Definition and Occurrence identity, and other later relations remain owned by their respective
authorities.

#### 7.9.2. Constituent Closure

A direct coordinate may contain these families recursively only as a finite, closed, acyclic presentation graph.

Nested members, cases, elements, keys, and values belong to the containing Input Definition. They do not create
independent Contract authorities, independent Input Definitions, or independent Input Occurrences.

The actual value judged at the Input boundary must be complete and finite. A semantic recursive type, mutually recursive
presentation, runtime cyclic graph used as presentation meaning, lazy constituent source, or live stream is not a V1
Input presentation.

Host aliasing and shared-reference topology do not change Input meaning. If an external relation must be presented, it
is carried as an explicitly declared descriptive value under Section 7.7 rather than as pointer identity.

#### 7.9.3. Collection Order Observation

Sequence owns positional order intrinsically.

Membership and Association may own one of the following order-observation laws:

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

The compiler may sort an unordered presentation to produce deterministic HIR encoding, fingerprints, diagnostics, tests,
or backend material. That realization order does not become Input meaning. Conversely, a declared encounter or
ordered-by obligation cannot be erased in the name of deterministic implementation.

An exact JVM collection surface never selects one of these Input order-observation laws. Sequenced, sorted, navigable,
implementation-specific, or other platform behavior is platform evidence only. ADR-0073 may admit such a surface for an
Input role only when it preserves the already-declared Input order, constituent Presentation Sameness, uniqueness, and
every other required Input distinction without loss. Additional platform observations remain platform meaning; they do
not enlarge Input meaning.

An effective platform comparator, natural ordering, insertion or access order, or traversal behavior is not adopted as
Input authority. A platform order may realize `Ordered By` only when its zero-equivalence and observable order preserve
the exact Input ordering law. A carrier that has already collapsed Input-distinct members or keys cannot be repaired at
the Input boundary. Order compatibility alone does not establish direct-carrier legality; immutability, constituent
closure, alias safety, capability isolation, and the other applicable Input and ADR-0073 laws still apply.

Lookup, navigation, first/last access, reverse traversal, range views, mutation behavior, random-access complexity,
hashing strategy, and similar host or generated operations are not Input presentation relations and do not create
additional fundamental presentation families. When a supported platform or generated API exposes such operations, its
owning API law and ADR-0073 own the separate preservation and use-legality obligation. No such operation may silently
introduce a broader equality, ordering, or key-equivalence relation into Input.

#### 7.9.4. Derived Presentations and Deliberate V1 Exclusions

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
whole-Input absence.

V1 deliberately does not add a generic positional heterogeneous Tuple, Multiset, or generic Result family. A future
revision may add one only after demonstrating a Contract-visible distinction that cannot be represented without semantic
loss and after passing the same HIR, Establishment, platform-preservation, and API review applied here. Unsupported
meaning is not approximated through a supported family.

Open records, extensible tables, flexible or unknown alternatives, open rows, dynamic `Any` or document values, untagged
unions selected by runtime shape, recursive presentations, object/reference topology, capabilities, resources, futures,
streams, callbacks, and executable values are not V1 Input presentation families.

#### 7.9.5. Closed Leaf Presentations

A Leaf is ratifiable only when its complete legal presentation domain, every Input-visible distinction, and Presentation
Sameness are closed by the submitted presentation together with fixed Kontrakt-owned law and, only when that meaning
actually requires it, an explicitly ratified closed semantic basis. Runtime-selected provider state, ambient
environment, host defaults, user-selected comparison policy, or executable application behavior cannot complete the Leaf
meaning.

This section fixes semantic Leaf meanings. It does not ratify a Java or Kotlin carrier, constructor, parser, resolver,
comparison operation, or other platform callable. ADR-0073 independently determines whether an exact platform surface is
Native, Carrier Only, Adapter Required, or Unsupported in the requested Input role and whether every remaining legal
platform and Contract observation can be preserved.

The following baseline V1 Leaf semantics are fixed at the Input semantic level. A concrete published Leaf is ratifiable
only after every range, bound, epoch, unit, grammar, or other meaning-affecting determinant referenced by its law is
closed. The names below are semantic labels in this ADR; they do not require the same public authoring tokens or
generated API type names.

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
| Instant                                | one exact coordinate under the fixed Kontrakt Instant epoch, unit, range, and resolution law                                    | the complete epoch coordinate is equal                                          | source timestamp spelling, source offset, zone, clock provenance, or leap-second parse evidence              |
| Zone Descriptor                        | one exact closed zone descriptor, preserving its descriptor kind and payload                                                    | the descriptor kind and exact descriptor payload are the same                   | current zone rules, current offset, provider availability, alias resolution, or resource authority           |
| Resolved Zoned Date-Time               | one exact resolved zoned date-time whose retained distinctions include local date-time, numeric UTC offset, and Zone Descriptor | all retained temporal and zone-descriptor distinctions are the same             | current time-zone-rule consistency, same-instant equivalence, or future rule interpretation                  |
| URI Reference                          | one syntactically legal closed URI-reference presentation under the applicable grammar                                          | the retained component presence and character presentation are exactly the same | resource identity, dereference result, scheme-specific resolution, or URI normalization                      |

For Decimal, scale is an Input-visible distinction. `2.0` and `2.00` are therefore not Presentation Same merely because
a numerical comparison could place them in one cohort. Any later collapse of that distinction requires an explicitly
selected later authority such as Canonicalization.

For Binary32 and Binary64, signed zero and every admitted NaN bit distinction remain Input-visible because Presentation
Sameness is exact over the admitted binary datum. A JVM or Kotlin equality rule, boxing rule, numerical comparator, or
arithmetic operation cannot replace that law. If an exact JVM carrier or operation cannot preserve the admitted
distinctions for the requested role, ADR-0073 must classify that platform use accordingly rather than weakening Input
meaning.

Text is a sequence of Unicode scalar values rather than a JVM UTF-16 code-unit sequence. A Java or Kotlin `String` is
therefore only a possible carrier. A source value that contains material outside the legal Text presentation domain does
not become legal merely because `String` can physically contain it. Normalization and case folding remain later explicit
meaning-changing operations when selected by an owning Contract.

The temporal Leaf meanings separate already-formed values from operations that acquire or derive them. `Instant` owns
only its closed epoch coordinate. `Zone Descriptor` owns only the descriptor and does not acquire the time-zone rules
denoted by a named zone. `Resolved Zoned Date-Time` preserves the submitted local date-time, resolved offset, and zone
descriptor even when a later time-zone rule basis would judge that combination differently. Current `ZoneRulesProvider`
contents or current TZDB consistency do not decide whether that already-formed presentation is a legal Input value.

Operations that obtain the current time or default zone, resolve a Zone Descriptor to rules, form a resolved zoned value
from a local time and named zone, convert an Instant through named-zone rules, or otherwise consume versioned temporal
data are separate platform or Contract questions. ADR-0073 requires the exact semantic basis for any such legal
operation and forbids ambient provider state from entering merely because the resulting value type is closed. A future
local schedule expressed as a Local Date-Time plus a Zone Descriptor is therefore not silently reinterpreted as a
Resolved Zoned Date-Time.

Generic `Token`, `Identifier`, `Path`, and `URL` are not universal V1 Leaf meanings. A specific closed descriptive value
may be ratified separately when its complete presentation and sameness law are actually closed, or it may be represented
through another existing family such as Text, Bytes, or URI Reference. A token or identifier that grants authority over
a live resource remains subject to Section 7.12 and ADR-0073 rather than becoming a Leaf merely because its carrier is
scalar-shaped. A generic filesystem `Path` does not import provider, filesystem, case, root, or resolution semantics
into Input by name alone.

Period-like calendar amounts do not require a new fundamental Leaf merely because a host library provides a `Period`
class. When their Contract-visible meaning is exactly a finite set of calendar components, the existing Named Product
family can preserve that meaning. A future dedicated Leaf would require an independently justified Contract-visible
distinction.

Section 7.8 determines whether a bound belongs to Input or to a later authority. A concrete V1 Leaf law that requires a
finite range or bound must fix that bound completely before Visible HIR; an unspecified implementation limit cannot
complete the law later. The internal compiler representation, catalog, table schema, lookup mechanism, or other reusable
compiler structure used to resolve these Leaf laws is Design work and is not a user-selectable Input semantic surface.

### 7.10. V1 User-Facing Authoring Classes

The V1 user-facing policy classifies host forms only to determine whether they can express an already-declared Input
presentation. The host category does not create Contract meaning.

| Authoring class                                                  | V1 treatment                                                                                                           | Examples                                                                                                                                                                                          | V1 frontend consequence                                                                                                                                              |
|------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct closed immutable presentation                             | Refine and ratify directly when every declared distinction is complete, immutable, supported, and legal under ADR-0073 | Primitive and `String` source forms where the exact mapping is lossless, closed enum source forms, approved immutable Leaves, flat Kotlin data class, flat final Kotlin carrier, flat Java record | The selected declaration may supply external Contract evidence without a duplicate carrier declaration; actual invocation material remains subject to Input judgment |
| Closed structured meaning without a ratified direct host mapping | Require explicit formation into a supported presentation before Input                                                  | Nested DTO, embedded Value Object, nested collection carrier, unsupported sealed hierarchy, host collection surface whose full observable law cannot be preserved                                 | The semantic presentation may be legal under Section 7.9, but this host carrier does not become authority merely because its shape resembles that presentation       |
| Implementation-shaped carrier                                    | Require explicit formation or reject the source                                                                        | Mutable JavaBean, framework DTO, proxy, entity, custom getter, delegated property, interface root, runtime-discovered implementation, third-party lifecycle object                                | Host conventions, mutable observation, lifecycle, and implementation relationships do not become Input meaning                                                       |
| Behavior, capability, movement, or role leakage                  | Reject from Input                                                                                                      | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core Fact authority, State, backend handle                                       | The material belongs to another role or is not Input presentation data                                                                                               |

Arrays, `List`, `Set`, `Map`, sequenced or sorted collections, and other platform collection surfaces are not accepted
or rejected by host category alone. Section 7.9 defines Input semantic meaning. ADR-0073 owns the platform-surface
ratification and constituent-closure gate that determines whether a particular host surface can realize that meaning
without loss.

The exact V1 direct-support matrix for nested and collection-shaped Java or Kotlin carriers remains
compiler/platform-support work. A legal Input presentation does not imply that every familiar host carrier for a similar
shape is a legal zero-adapter carrier.

### 7.11. Zero-Adapter Direct Carrier

A selected host declaration may serve as both source evidence and runtime carrier when it already realizes a legal
immutable V1 presentation under this ADR and passes the applicable ADR-0073 gate.

For the current JVM V1 zero-adapter path, a flat Kotlin data class may provide that evidence directly when every
selected coordinate has a supported presentation meaning. The same is true for a flat final Kotlin class whose selected
coordinates are primary-constructor `val` properties, and for a flat Java 17+ record whose components satisfy the same
requirement. These forms describe the current direct-support path, not a restriction on the nested semantic
presentations permitted by Section 7.9.

Only the resolved Input-visible coordinate surface survives into meaning. Constructor behavior and generated host
machinery remain outside authority under Section 7.4.

Source declaration order may help the frontend acquire the surface deterministically, but it is not Input semantic order
and does not identify a direct coordinate. Section 7.2 owns direct coordinate identity. Section 7.9 owns any order that
is actually observable inside a presentation.

A direct carrier cannot depend on runtime implementation discovery or recursive host traversal that has not been
ratified as a closed constituent mapping. Material that needs unsupported traversal or external lifecycle behavior must
be formed into a supported presentation before invocation.

Another frontend may later realize semantically equal Input presentation meaning. Section 5.6 prevents that equality
from merging independently declared Contract identities.

### 7.12. Behavior and Capability Are Not Opaque Leaves

Callbacks, services, repositories, live resources, async control surfaces, and other executable capabilities are not
rescued by labeling them opaque.

Input may carry a specifically declared descriptive identifier, token-like value, coordinate, source text, bounded
bytes, URI Reference, Zone Descriptor, or another approved closed value that refers to something outside the machine.
The execution capability, live resource authority, provider state, or resource ownership denoted by that value remains
outside Input authority. A bearer or capability token is not rescued by scalar-shaped presentation.

---

## 8. Authoring Boundary

Each declared Input selection is authored from the complete Contract meaning intended for that selection rather than
from a reuse target.

Section 4.4 permits several legal selection occurrences to select one Input Definition when they intentionally select
the same complete meaning. Those occurrences may belong to different Interactions or to different declared Contract
World contexts. Similar host shape is insufficient. If two Inputs were declared independently, Section 5.6 prevents
later structural equality from merging them.

A supported host-facing declaration may still remove the need for a second carrier declaration when it completely
expresses one selected Input Contract. That frontend convenience is separate from sharing one Input Definition across
several selection occurrences.

Richer application objects remain legal outside Kontrakt, but they must be formed into the applicable Input presentation
before invocation unless their exact host surface passes the applicable direct-carrier gate. Section 7.9 defines the
semantic presentation independently of that platform support. The authoring surface therefore does not force the
application object model into the Contract Core.

## 9. Refusal Boundary

Input distinguishes compiler-side non-entry from two Input-owned negative results.

A source or Candidate that cannot satisfy the Resolved HIR seal does not enter the Input Definition judgment. Unresolved
source, recovery or poison material, an invalid Protocol handoff, or another compiler-owned failure therefore does not
create `Input Definition Refusal`. The exact unsuccessful compiler result is owned by the applicable compiler judgment;
its common representation and recovery behavior currently depend on Proposed ADR-0074.

`Input Definition Refusal` occurs only after a complete resolved Candidate legally enters the Input Definition judgment
and fails the Input-owned Definition law. No authoritative Input Definition is established from that Candidate.

`Refused` at invocation time occurs only after an exact Established Input Definition and exact Interaction have allowed
a
fresh Input semantic application to enter the occurrence-time judgment. That judgment still establishes one Input
Occurrence with result `Refused`, but it establishes no Input-authoritative presentation and Admission is not reached.
A supplied boundary value that omits a required coordinate or violates the declared presentation law is handled here.

A transport, acquisition, or orchestration failure that prevents the material from entering the Input semantic
application creates no Input Occurrence. Likewise, failure to obtain an applicable Established Input Definition or a
stop
owned by Policy, Governance, Budget, Capacity, or another authority remains the result of that owner rather than an
Input Refusal.

Sections 7.3 through 7.7 define material that Input does not repair into legality after entry. Neither Input Definition
Refusal nor an Input Occurrence result of `Refused` automatically becomes ADR-0057 Failure; the Failure Contract retains
its own authority. Diagnostic evidence may explain either Input result, but Diagnostic does not create or rewrite that
meaning.

## 10. Relationship to Later Contracts

Input establishes only Input-owned Definition and Occurrence meaning. It creates no later pipeline authority.

Admission owns continuation, as stated in Section 7.8. Canonicalization, when selected, may establish a stable
same-shape representative without redefining Input. Lowering owns the later relation from direct Input coordinates
toward
Operation parameters and Fact coordinates. Nested constituents under Section 7.9 do not become Lowering source
coordinates merely because they are addressable inside the Input presentation.

Those later authorities do not enter Input Definition identity. Their negative results also remain distinct from Input
Definition Refusal and Input Occurrence `Refused` under Section 9.

### 10.1. World and Backing Placement

Established Input Definition Material is definition-world meaning and is made available through the Canonical Contract
World under ADR-0063. Direct Input coordinate meaning belongs to that complete Established Definition surface.

Established Input Occurrences are occurrence-specific material. They remain outside the Canonical Contract World unless
a future owning law establishes an explicit relation requiring otherwise.

```text
Canonical Contract World
    -> Established Input Definition Material
    -> direct established Input coordinate meaning

Occurrence backing
    -> Established Input Occurrence Material
```

This is a semantic placement law, not a storage-topology mandate. Definition and occurrence observations may use
separate
or shared physical storage when the required semantic boundaries remain intact. Reclaiming occurrence backing under a
legal retention policy does not mean the occurrence was never established, and retaining backing does not extend
semantic authority by itself.

For Input-owned relations, legal observation keeps `relation not owned`, explicitly permitted absence, present relation,
and required-but-unresolved relation distinct. A required unresolved coordinate or relation cannot appear as successful
Established Input meaning. Likewise, the fact that a future Input Occurrence does not yet exist is not incompleteness of
the Established Input Definition. Independently complete Input Definitions may become visible without making any one
Definition partially authoritative. Source provenance remains an adjacent relation rather than Definition or Occurrence
identity.

### 10.2. Established Input Semantic Protocol

Input specializes the common ADR-0063 Established Semantic Protocol through these typed reference domains:

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

### 10.3. Projection Completeness and Availability

The **Established Input Definition Projection** is complete for one exact Definition: its
`EstablishedInputDefinitionRef`, Owning Input Authority Binding, Version Binding, complete Input Definition Meaning, and
the complete direct established coordinate relation owned by that Definition.

The **Established Input Occurrence Projection** is complete for one exact Input application: its
`EstablishedInputOccurrenceRef`, exact Definition Reference, exact Interaction Reference, exact occurrence result, and,
when that result is `Presented`, the exact Established Presentation.

The **Established Input Presentation Projection** exists only for a `Presented` occurrence and exposes the exact
Occurrence Reference, Definition Reference, Interaction Reference, and complete immutable Established Presentation. Its
absence is not an alternative way to infer Contract result meaning; the occurrence result remains observable through the
Input Occurrence Outcome Projection.

The **Input Direct Coordinate Value Projection** observes one direct coordinate of one Presented occurrence through the
exact `EstablishedInputOccurrenceRef` and `EstablishedInputCoordinateRef`. It exposes the coordinate's explicit presence
state and, when present, the exact presented coordinate value. It does not promote a nested member, element, key, or
value
into a direct Input coordinate.

Projection equality follows the Input-owned observation that each projection declares complete. Equal projection payload
does not merge distinct Definition or Occurrence References. In particular, two distinct Input Occurrences may expose
semantically equal Established Presentations while remaining different occurrences.

Observing these projections neither transfers Input authority nor grants Publication authority. Semantic absence is also
not the same as backing not retained, a projection not yet physically materialized, unsupported observation machinery,
or corrupt compiler storage. If a required legal observation cannot be supplied, Section 10.6 and ADR-0074 govern the
compiler-side availability boundary rather than fabricating Input absence or Refusal.

### 10.4. Direct Relations and Consumer Law

Input owns only these direct established relation families:

```text
Established Input Definition
    -> direct Input coordinate

Established Input Occurrence
    -> exact Established Input Definition

Established Input Occurrence
    -> exact Interaction
```

A Policy World relation that selects an Input Definition is owned by Policy, Interface, or Governance law rather than by
Input. A later relation from an Input coordinate toward Lowering is owned by Lowering. The Established Input Semantic
Protocol does not reverse those ownership directions merely because a consumer wants convenient navigation.

Admission consumes the Established Input Presentation Projection. It does not reopen the JVM carrier, call host
`equals` or `hashCode`, recover a source declaration, inspect runtime object identity, or rediscover the collection law.
Canonicalization may consume the same legal presentation observation when its own law requires the Input presentation.
Lowering may consume `EstablishedInputCoordinateRef` together with the Input Direct Coordinate Value Projection.
Diagnostics may consume the full occurrence or the Input Occurrence Outcome Projection. Generated API formation,
verification, and PBT planning may consume the Definition projection.

Consumers select producer-defined legal projections. They do not create `Diagnostic Input Meaning`, `Backend Input
Meaning`, `Test Input Meaning`, or another consumer-specific Contract semantics.

If a consumer joins several Input observations, computes transitive reachability, proves a property, builds a summary,
or performs another new judgment, that result belongs to the consuming compiler product or later owning authority. It is
not retroactively an Established Input Projection, and a compiler product dependency does not become an Input authority
chain.

### 10.5. Reuse and Current Validity

Input Definition and HIR work may be reused when current validity is established under ADR-0071 and ADR-0063. A retained
Definition or projection is reusable only when the current Input authority and Version still correspond, every
meaning-determining input remains valid, every direct semantic reference still denotes the required exact meaning, and
the complete direct coordinate membership and presentation law remain unchanged.

Because an Input Definition claims a complete direct coordinate set, reuse validation must cover negative space as well
as surviving members. It must establish that no new direct coordinate was added and no required direct coordinate was
removed; validating only the old coordinates is insufficient.

Occurrence authority follows a different law. A fresh Input boundary entry never reuses an older occurrence as the new
semantic occurrence, even when the same Definition and Presentation yield the same deterministic result. The compiler
may reuse an Input checker, a comparison accelerator, a current Definition handle, a coordinate access plan, or
immutable
presentation backing. Two occurrences may even share physical immutable presentation storage. None of that makes their
Occurrence References equal.

Conversely, duplicated physical computation of one already-identified exact occurrence does not create two semantic
occurrences. Parallel or speculative workers may converge on one occurrence result when they are evaluating the same
exact application.

Clean formation, validated in-memory reuse, persistent reload, future incremental repair, and parallel formation must
produce the same legal Input Protocol observations. Cache state, fingerprints, HIDs, dependency topology, or retained
bytes may justify work avoidance only through separately validated current-validity evidence; they do not create Input
meaning or authority.

A cache miss or rejected reuse attempt is not itself an Input or compiler unsuccessful result. The compiler may return
to
the clean deterministic path when retained material cannot be validated. Successfully decoding or locating persisted
bytes likewise does not establish current semantic validity without the checks above.

### 10.6. Coherent Observation, Representation Freedom, and Verification

One legal Input Protocol observation must be coherent. A consumer cannot combine a Definition from one semantic
generation with a stale presentation-law projection or coordinate relation from another merely because all backing is
still physically reachable. Cross-generation backing may participate only after current validity has established one
coherent logical observation.

Protocol crossing does not require a wrapper allocation or full copy. Resolved HIR, Establishment, the Canonical
Contract
World, occurrence backing, and consumers may use primitive arrays, typed slabs, columnar ranges, dense generation-local
handles, shared immutable backing, memory-mapped storage, or another representation that preserves the same legal
observations. Physical sharing does not merge semantic entities.

Lazy physical materialization is permitted only after the semantic unit and its legal membership are already complete. A
consumer cannot treat unresolved semantic meaning as a value that will become complete on first access. Typed reference
kinds remain distinct even when their physical handles use the same width, and stale generation-local handles or
wrong-kind references must fail closed rather than resolve through unchecked numeric substitution.

Every optimized or reused path remains checkable against the clean deterministic path and the producer-owned Input
Protocol laws. The exact conformance harness, differential oracle, fuzzing strategy, and adversarial-resource tests are
Verification or Design work. Oversized input, extreme nesting, malformed persistent data, corrupted handles, hostile
hash behavior, or cyclic physical carrier graphs must not be solved by weakening the Input semantics. Semantic bounds
remain distinct from compiler Budget and resource-safety limits.

If already-Established Input meaning exists but compiler machinery cannot supply a required legal observation, that is a
compiler availability problem rather than an Input Refusal. The observation failure does not revoke or rewrite the
already-Established Input meaning. The exact common unsuccessful-result representation remains owned by Proposed
ADR-0074.

### 10.7. Non-Normative Compiler-Native Access Sketch

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

Occurrence processing may likewise keep application identity separate from presentation storage.

```text
definition_ref = established_input_definition_for_interaction[interaction_ref]
occurrence_ref = fresh_input_occurrence_ref()

result = judge_input_presentation(
    definition_ref,
    submitted_material
)

if result == Presented:
    establish occurrence_ref with
        definition_ref
        interaction_ref
        Presented
        complete immutable presentation
else:
    establish occurrence_ref with
        definition_ref
        interaction_ref
        Refused
```

A later Admission read is defined over the legal Established Input Presentation Projection, not over the physical
carrier used to form it.

## 11. Open in This Section

No further V1 Input semantic question is left open by this ADR. The Input HIR bundle, Definition and Occurrence
Establishment bundle, Established Material family, and Established Input Semantic Protocol are closed by Sections 5, 6,
and 10. The remaining items are API, platform-support, Governance, HIR realization, Verification, or Design work that
must preserve that closed Input law.

```text
Input / ADR-0064
    HIR Bundle                     -> DEFINED
    Establishment Bundle           -> DEFINED
    Established Protocol           -> DEFINED
    Input Semantic Status          -> CLOSED
    Common Compiler-Result Detail  -> PROVISIONAL-COMMON / ADR-0074
    Physical Realization           -> DESIGN
```

ADR-0074 remains Proposed. Compiler-owned unsuccessful results, recovery, unentered-later-stage representation,
availability aggregation, and Compiler Result Protocol details therefore remain a common provisional dependency rather
than an Input-specific semantic open. Sections 5.7, 9, and 10 fix only the Input-side ownership boundary that those
common compiler-result laws must preserve.

The final authoring token spelling and exact public Java or Kotlin carrier syntax for Input remain open. Section 7.9
fixes semantic families and relation semantics without requiring those family or relation names to appear in generated
APIs.

Section 7.9.5 closes the baseline Input Presentation Sameness laws for the V1 Leaf meanings listed there, including
Decimal scale, floating-point signed zero and NaN bit distinctions, Unicode scalar Text, closed temporal values, Zone
Descriptor, Resolved Zoned Date-Time, and URI Reference. The exact public authoring token names and the exact set of
concrete bounded Leaf variants shipped by V1 remain API and platform-support work. Every concrete published Leaf law
must nevertheless close its own legal range, bound, epoch, unit, grammar, or other meaning-affecting determinant before
Visible HIR.

The internal catalog, table, lookup, or other reusable compiler representation used to resolve those Leaf laws is Design
work rather than an additional Input Contract concept. It cannot become a user-selectable Input equality or
normalization surface. The Input Candidate must already contain the exact resolved presentation meaning and every exact
semantic basis actually consumed by that meaning.

The exact Java and Kotlin direct-support matrix for the Leaf meanings in Section 7.9.5 remains ADR-0073 work. Ratifying
an Input meaning does not automatically ratify `String`, `BigDecimal`, floating wrappers, `java.time` values,
`java.net.URI`, collection carriers, their constructors, or their associated operations. Platform Use Verification must
decide the exact role-specific result without weakening the Input law.

Different Policy Worlds are permitted to select different Input Definitions for the same Interaction under Section 4.4.
What remains open is the Governance and Interface-realization API that lets an external caller operate under the
applicable world and the generated host API strategy that projects any resulting Policy-variable Input. This ADR does
not require those distinct Input Definitions to share one host carrier.

The exact HIR representation of Policy-owned Contract World constituent relations remains Policy and HIR work. Input
requires an exact legal selection relation and forbids reconstruction of Input Definition Meaning from that relation,
but this ADR does not decide whether Policy exposes those constituent relations through dedicated IDL Binding Candidate
projections, direct Policy Candidate relations, or another semantically equivalent producer-owned projection.

The physical HIR schema, table layout, dense handles, Presentation Sameness encoding, canonical compiler traversal
order, equality acceleration, collection realization, and the exact Java/Kotlin support matrix remain Design or ADR-0073
work. Those choices may optimize the established meaning but cannot redefine Section 7.9. Hashes, fingerprints, indexes,
and precomputed comparison keys may accelerate comparison only after collision-safe or otherwise sound validation under
the exact resolved semantic law.

Any future presentation-family expansion must preserve the authority boundaries established here. In particular, it
cannot make runtime graph traversal, snapshot timing, open-world evolution, or backend representation a new source of
Input meaning.

## 12. Consequences

Input becomes an explicit immutable boundary presentation rather than a host object treated as Contract authority.

The directly addressable Input surface remains flat, while one direct coordinate may carry a finite, closed, acyclic
nested value presentation. This keeps Contract authority topology simple without forcing Input values to be scalar-only.

The six-family V1 presentation algebra gives HIR and Establishment a closed semantic target while leaving Java/Kotlin
carrier choice, storage layout, collection realization, and generated API projection replaceable.

Collection order is represented only when the Input presentation law makes that order observable. ADR-0073 separately
determines whether a platform surface can preserve that already-declared order in the requested role. Compiler canonical
order and physical storage order remain realization.

Presentation Sameness is derived from complete Input-visible meaning and is the sole V1 uniqueness relation for
Membership constituents and Association keys. Input exposes no user-selectable equality, collation, normalization, or
coarser equivalence surface. A later Canonicalization Contract may explicitly collapse Input-preserved distinctions and
establish a representative under its own authority; omission creates no hidden replacement relation.

A meaning-affecting relation or semantic-basis change changes Input meaning. A backend, library, or provider upgrade
that provably preserves the same exact resolved law remains realization.

Some application objects therefore cannot cross the boundary directly even when their business data could be represented
by a legal Input presentation. They need an ADR-0073-compatible direct mapping or explicit presentation formation before
invocation. That cost preserves the distinction between application representation and Input authority.

Input now preserves application multiplicity explicitly. Retry, restart, redelivery, or another fresh boundary re-entry
creates a new Input Occurrence even when deterministic judgment produces the same semantic result. Later Fact identity
may converge equal factual meaning without collapsing those earlier boundary occurrences.

The HIR and Established Semantic Protocols expose producer-owned legal observations instead of consumer-specific
semantic
copies. This lets Admission, Canonicalization, Lowering, diagnostics, verification, generated APIs, and test synthesis
share authoritative Input meaning without rebuilding it from source or host carriers.

Definition reuse and occurrence reuse are intentionally asymmetric. Stable Definition projections may avoid compiler
work after complete current-validity checks, while a fresh boundary entry always has fresh occurrence identity. Physical
presentation backing may still be shared when doing so preserves the same immutable observation.

Logical Protocol boundaries do not impose physical indirection. A V1 compiler may use dense handles, primitive slabs,
columnar storage, or shared immutable ranges while keeping Candidate, Established Definition, Established Occurrence,
and
consumer observations semantically distinct.

## 13. Migration History

This ADR was extracted mechanically from the Input-owned material of ADR-0048.

The extraction itself does not change the accepted Input Contract semantics.

A 2026-09-02 review clarified the Contract/frontend boundary for coordinate closure, direct presentation meaning,
immutability, carrier separation, inheritance and polymorphism, presence and finite choice, opaque values, presentation
bounds, and supported presentation profiles. The review also removed the scalar-only implication from flatness without
yet fixing the exact V1 collection taxonomy.

A 2026-09-20 review aligned Input with ADR-0071 and ADR-0063. The review made the Definition Candidate complete before
Establishment and separated Definition meaning from the IDL Binding Candidate that selects it. It also specialized the
common identity model so the current Input law has one Definition per Authority and Version, with no additional
authority-local Definition coordinate. Semantic equality no longer implies reference or Contract identity, implicit
merge is prohibited, and host declaration order is nonsemantic by default. Shared selection remains legal only when the
complete Input meaning is intentionally shared; reuse itself is not an authoring objective.

A later 2026-09-20 review corrected the Binding specialization after reconciling Input with ADR-0054. Input selection is
no longer modeled as inherently Interaction-side. The exact selecting context follows the static Interface form or the
applicable Policy-world declaration form, while Input Definition Meaning remains independent of that surrounding
selection context. The review also separated pre-authority IDL Binding Candidates from invocation-time established
Contract relations.

A subsequent 2026-09-20 review closed the cross-Policy Input-target question in favor of explicit Policy-variable Input.
Different declared Policy Worlds may select different Input Definitions for the same Interaction. The remaining question
belongs to Governance and Interface realization: how an external caller obtains the applicable world and how generated
host APIs project that variation.

The same review closed the V1 direct-coordinate and presentation-topology laws. Direct Input coordinates are nominal and
declaration order is nonsemantic. Flatness now means a flat directly addressable authority surface rather than
scalar-only value topology. A direct coordinate may contain a finite, closed, acyclic presentation built from Leaf,
Named Product, Closed Tagged Choice, Sequence, Membership, and Association. Nested constituents do not become new Input
authorities or Lowering coordinates. Collection-visible order is separated from Presentation Sameness, deterministic
compiler order, and storage order. Tuple, Multiset, generic Result, open or dynamic structures, recursive presentations,
capabilities, resources, and executable or time-dependent forms are not V1 fundamental Input families.

A 2026-09-21 review closed the V1 Membership and Association equivalence authority model. Input Presentation Sameness is
intrinsic to the complete resolved presentation meaning and is the default uniqueness relation only when it satisfies
the required equivalence law. Membership uniqueness equivalence and Association key equivalence remain subordinate
Input-owned laws and may instead select one exact ratified semantic equivalence resolved before Visible HIR. Such a law
is homogeneous, deterministic, purpose-bounded, complete in its meaning-affecting semantic basis, and cannot be supplied
by application callbacks, host `equals`, comparator objects, ambient locale or provider state, hashes, or backend
indexes. Presentation Sameness must imply the selected uniqueness equivalence. Duplicate-equivalent members or keys are
invalid rather than silently deduplicated. V1 `Ordered By` zero-equivalence must coincide with the applicable uniqueness
equivalence. Canonicalization, Fact sameness, HIR semantic equality, Definition and Occurrence identity, platform
equality observation, and physical equality remain separate authorities or relations.

A later 2026-09-21 review superseded the user-selectable/coarser Input-equivalence part of that decision. V1 Input now
fixes Membership uniqueness and Association key uniqueness exclusively through the applicable Kontrakt-owned
Presentation Sameness law. Input exposes no separate equality, collation, normalization, comparator, or equivalence
selection. A platform carrier may be used only when the ADR-0073 support boundary can preserve every required Input
distinction; carrier behavior does not redefine Input uniqueness. Any deliberate collapse of Input-preserved
distinctions belongs to an explicitly selected later authority such as Canonicalization. The earlier history entry is
retained to record the superseded design rather than to remain normative.

A subsequent 2026-09-21 Leaf audit closed the baseline V1 Leaf-presentation laws after a reverse review against
ADR-0073. Boolean, fixed-width signed and unsigned Integer, Bytes, Unicode-scalar Text, UUID, Decimal, Binary32,
Binary64, ISO Date, Local Time, Local Date-Time, Numeric UTC Offset, Offset Date-Time, Duration, Instant, Zone
Descriptor, Resolved Zoned Date-Time, and URI Reference now have fixed Input Presentation Sameness laws independent of
host equality, normalization, provider behavior, or generated representation. Decimal preserves scale. Binary32 and
Binary64 preserve the complete admitted bit datum, including signed-zero and admitted NaN distinctions. Text preserves
exact Unicode scalar order rather than JVM UTF-16 storage. Instant preserves only its fixed epoch coordinate. Zone
Descriptor is separated from the time-zone rules it denotes, and Resolved Zoned Date-Time preserves the submitted local
date-time, offset, and descriptor without revalidating them against the current TZDB. The review also corrected an
earlier over-broad exclusion of temporal values: closed temporal values may be Input presentations even when separate
formation or resolution operations consume a ratified temporal basis. Runtime-selected clocks, default zones, provider
contents, and other ambient sources remain outside Input authority. Generic Token, Identifier, Path, and URL are not
universal V1 Leaf meanings, while specific closed descriptors may be ratified separately. The internal compiler
representation for these laws is deferred to Design and does not become a user-selectable Input surface. This review
also clarifies that the earlier exclusion of time-dependent forms refers to live, ambient, lazy, or provider-dependent
sources rather than already-formed closed temporal values.

A later 2026-09-21 Input closure audit reconciled collection order and carrier preservation with ADR-0073. Sequence
position and the three Membership and Association order-observation laws remain Input-owned meaning. A JVM collection
surface, implementation, comparator, equality relation, insertion or access order, traversal behavior, or constructor
does not select those laws. Such a surface is usable only when Platform Use Verification can preserve the
already-declared Input order, Presentation Sameness, uniqueness, immutability, constituent closure, and other applicable
obligations without loss. Platform-only observations remain platform meaning. Lookup, navigation, range, mutation, and
complexity operations remain separate API or platform-operation questions and do not enlarge Input presentation
semantics. The same audit removed residual normative use of `profile` as an Input semantic concept; any reusable
compiler representation for closed Leaf law material remains Design work. No further V1 Input semantic question remains
open in this ADR.

A subsequent 2026-09-21 HIR–Establishment closure audit applied the common ADR-0071/ADR-0063 master checklist to Input.
The audit defined the Input-specific Resolved HIR Candidate Protocol, including typed Definition, Binding, and direct
coordinate references and the complete producer-owned projection catalog. It separated HIR seal failure from Input
Definition Refusal, marked Required Basis, Basis Binding, Complete Basis, Input-owned Applicability, and Input-owned
arbitration as not applicable, and closed the complete Established Input Definition surface. It also established Input
Occurrence as an independent semantic application: every fresh boundary re-entry creates a new occurrence, `Presented`
and `Refused` are the occurrence result vocabulary, and equal presentations or equal results do not merge occurrences.
Established Definition material remains in the Canonical Contract World while occurrence material remains separately
backed and reclaimable. The audit defined the Established Input Semantic Protocol, direct relation ownership, consumer
access law, complete-set reuse validation, coherent observation, and the distinction between reusable physical backing
and fresh occurrence authority. Compiler unsuccessful-result and recovery details remain a provisional common dependency
on Proposed ADR-0074; physical HIR schema, query, cache, persistence, and JVM carrier realization remain Design or
ADR-0073 work rather than Input meaning.

ADR-0048 remains the owner of the shared inbound-airlock composition and core-entry relation.