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

Definition-time refinement must be deterministic. Invocation material that fails the already-established presentation
law stops before Admission, and Section 9 owns the two refusal times.

Policy, Governance, Budget, and Capacity retain their own authority when they independently apply. Different declared
Policy Worlds may select different Input Definitions for one Interaction under Section 4.4; the Governance API that
makes an applicable world usable by an external caller remains outside this ADR.

## 4. Decision

### 4.1. Input Meaning

Input is the boundary presentation Contract.

```text
outside presentation evidence
-> resolved Input Definition Candidate
-> Input-owned Establishment
-> authoritative Input Definition
-> already-formed immutable Input presentation
-> Input boundary judgment
-> Admission
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
nested constituent law, collection observation law, and semantic-profile boundary.

A structural distinction enters Candidate meaning only when the owning Input law makes that distinction observable. The
Candidate therefore retains the exact presentation family, constituent topology, presence law, form-defining
cardinality, and any exact order or equivalence obligation required by Section 7.9. Compiler observation order, physical
storage order, and host traversal order never substitute for those laws.

When an exact semantic reference is required to interpret one of those distinctions, the Candidate retains that
reference before Visible HIR. It cannot postpone unresolved interpretation to arbitrary later traversal or executable
host behavior.

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
are determinants under Section 7.9 whenever changing them would change the Input-visible presentation. An exact semantic
profile, equivalence law, or ordering law becomes a determinant only when the applicable Input presentation requires
that distinction.

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

Before Establishment, the corresponding Input Definition Candidate Reference designates the exact resolved Input
Contract Authority coordinate together with the exact authority-scoped Resolved Version Candidate Coordinate. The Input
reference kind is preserved by the typed HIR reference domain; it need not be repeated as a generic scalar tag.

The authored Version Claim is resolved to the exact authority-scoped Resolved Version Candidate Coordinate before the
Input Candidate becomes visible. That coordinate participates in exact versioned candidate designation rather than in
the presentation payload described by Section 5.2. Establishment forms the authoritative Version Binding and Definition
Reference under ADR-0063; a Candidate Reference is not cast or promoted into a Definition Reference. ADR-0053's
prohibition on implicit Version selection applies unchanged.

A change to Contract-visible Input presentation meaning requires a new Contract Version when the user continues the same
Authority. A frontend-only or carrier-only change that preserves the exact resolved Input meaning does not create a new
Input meaning merely because source representation changed. ADR-0053 remains the owner of rename, replacement,
continuity, and history rules. Current HIR identity is independently resolved from current semantic inputs; it does not
inherit identity from a previous compiler generation under ADR-0071.

### 5.6. Semantic Equality, Merge, and Physical Reuse

Input Definition Meaning equality is producer-owned. Two meanings are equal only when every Input-owned determinant is
equal under the laws that define that meaning. Presentation-value equality inside a collection family is a separate
relation governed by Section 7.9; it does not by itself define complete Input observability or Definition identity.

Meaning equality does not establish Candidate Reference equality or authoritative Definition identity. ADR-0053 also
keeps different Contract Versions distinct even when their contract-specific presentation meaning happens to compare
equal.

Input defines no implicit semantic merge from structural equality. Section 4.4 covers the different case in which
several Binding Candidates deliberately select one already-identified Input Definition.

Physical reuse remains available to the compiler. ADR-0071 and ADR-0063 govern the rule that caching, hashing,
interning, or representation sharing cannot create semantic equality or Contract identity.

### 5.7. Establishment Result

An Input definition receives authority only after the complete resolved Candidate has been judged under the Input law.

A Candidate that is incomplete, ambiguous, unsupported, open, or otherwise incompatible with the Input law establishes
no Input definition authority.

Successful Establishment preserves the exact Definition Meaning and the identity relations required by ADR-0063.
Compiler acquisition, planning, storage, publication, and code generation may use replaceable representations, but none
of those representations can become a second Input authority path.

## 6. Invocation Boundary

For one applicable Interaction, Input judges the actual material supplied at that Interaction's Input boundary under the
exact authoritative Input relation applicable to that flow.

An Input IDL Binding Candidate is pre-authority frontend material. Invocation does not query that Candidate. The
applicable established Contract World provides the authoritative relation to the exact Established Input Definition. An
Interface without Policy obtains that relation from its static Contract arrangement. When Policy participates, the
applicable established Policy World supplies the corresponding world relation under the Policy and Governance laws.
Section 4.4 permits different Policy Worlds to reach different Input Definitions for the same Interaction.

```text
applicable established Contract World relation
+
exact Interaction Input position
    -> exact Established Input Definition

exact Established Input Definition
+
actual material supplied at the Input boundary
    ↓
Input judgment
    ↓
declared presentation shape satisfied
    -> Input success
    -> Admission may judge the presentation

declared presentation shape not satisfied
    -> Input refusal
    -> Admission is not reached
```

Input judges only whether the supplied material realizes the declared Input presentation. It does not judge whether the
presented values may continue. Section 7.8 owns that authority boundary.

Carrier formation and the separation between carrier mechanics and Contract meaning follow Section 7.4. Immutability and
occurrence stability follow Section 7.3. Input does not repair, snapshot, or reinterpret material that reaches the
boundary in a form those laws reject.

Definition-time verification establishes that the selected declaration can provide a legal Input definition. It does not
authorize arbitrary material merely because that material has a compatible host type. The actual material participating
in the Interaction must satisfy the applicable Input law.

After successful Input judgment, later inbound processing may change physical representation only through the declared
Contract relations that own those changes. The material eventually supplied to the user Operation must preserve the
meaning lawfully derived through the complete declared inbound pipeline. Undeclared substitution, mutation,
reinterpretation, or introduction of different meaning is not a valid realization.

The compiler and runtime must prove or check the required integrity at the boundaries where the applicable law cannot be
guaranteed otherwise. This obligation does not require one generated reader, static gate, wrapper, token, or mandatory
recheck strategy. Equivalent realization may use static proof, generated checks, controlled immutable material,
specialized invocation, or another mechanism that preserves the same Contract result.

Policy and Governance retain the authority assigned to them by their own ADRs. Policy defines a Contract World and
Governance controls governing applicability where Governance participates. Budget, Capacity, and other independently
applicable judgment authorities retain their own results. None of those results becomes Input refusal.

Occurrence-time Input judgment uses the already-authoritative Input definition reached through the applicable
established Contract relation. Section 5.3 forbids reconstructing that definition from source, an HIR Binding Candidate,
compiler acquisition state, graph state, cache state, or backend representation.

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

Slot selection therefore nominates a candidate but does not guarantee ratification. Section 9 owns the distinction
between definition-time refusal and invocation-time refusal.

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

An opaque value is admissible only when its complete Input-visible presentation distinctions are explicitly defined by
an applicable semantic profile. Opaque treatment does not permit Input to adopt an arbitrary user object while ignoring
its behavior, mutability, lifecycle, inheritance, graph structure, or runtime identity.

A frontend may map a supported host type to an applicable presentation profile. The host type does not define the
profile, and its methods, equality, hashing, parsing, normalization, or serialization behavior do not become Input law.

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

A **Leaf** is a complete presentation meaning that Input does not further decompose into constituent topology. A Leaf
may rely on an exact semantic profile when that profile is required to state its Input-visible distinctions.

A **Named Product** is a finite closed set of nominally identified members. Each member has one complete presentation
meaning and its own explicit presence law when absence is permitted. Product member declaration order is not semantic
order.

A **Closed Tagged Choice** has one finite closed domain of nominal alternatives. Exactly one alternative is selected in
one occurrence. A case may have no payload or one complete constituent presentation. Case declaration order is not
semantic order. Runtime subtype discovery and untagged structural matching do not select a case.

A **Sequence** contains zero or more occurrences of one constituent presentation. Position is intrinsic semantic
meaning, multiplicity is retained, and every actual Input occurrence is finite. Fixed or bounded extent is expressed
through the presentation cardinality law rather than by creating another fundamental family.

A **Membership** contains zero or more occurrences of one constituent presentation under one exact uniqueness
equivalence. Membership itself has no intrinsic position. Its order-observation law is independent of uniqueness and
value equality.

An **Association** contains zero or more key/value associations under one exact key equivalence. Keys are unique under
that equivalence. Key and value are presentation constituents rather than Input coordinate identities. Association
itself has no intrinsic entry position.

The exact source and ownership model for Membership uniqueness equivalence and Association key equivalence remains open
under Section 11. Until that law is closed, no arbitrary executable `equals`, comparator, callback, runtime object
identity, or backend hash may supply the missing authority.

#### 7.9.1. Constituent Closure

A direct coordinate may contain these families recursively only as a finite, closed, acyclic presentation graph.

Nested members, cases, elements, keys, and values belong to the containing Input Definition. They do not create
independent Contract authorities, independent Input Definitions, or independent Input Occurrences.

The actual value judged at the Input boundary must be complete and finite. A semantic recursive type, mutually recursive
presentation, runtime cyclic graph used as presentation meaning, lazy constituent source, or live stream is not a V1
Input presentation.

Host aliasing and shared-reference topology do not change Input meaning. If an external relation must be presented, it
is carried as an explicitly declared descriptive value under Section 7.7 rather than as pointer identity.

#### 7.9.2. Collection Order Observation

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
Association the ordering law applies to keys. The ordering law must be closed, stable for the presented value, free of
ambient state and executable callback authority, and coherent with the applicable uniqueness equivalence. The exact
semantic source of such ordering laws remains subject to the open equivalence-law work in Section 11.

An observable order does not automatically participate in collection value equality. Two Membership or Association
values may compare equal under their applicable value-equality law while exposing different legal encounter order.
Equality therefore does not authorize the compiler to erase an Input-visible order distinction.

Contract-visible order, deterministic compiler observation order, and physical storage order are separate:

```text
Contract-visible order
    != deterministic compiler observation order
    != physical storage order
```

The compiler may sort an unordered presentation to produce deterministic HIR encoding, fingerprints, diagnostics, tests,
or backend material. That realization order does not become Input meaning. Conversely, a declared encounter or
ordered-by obligation cannot be erased in the name of deterministic implementation.

Navigation, first/last access, reverse traversal, random-access complexity, hashing strategy, and similar host
operations do not create additional fundamental presentation families. When a supported platform surface exposes such
operations, ADR-0073 owns the separate preservation and use-legality obligation.

#### 7.9.3. Derived Presentations and Deliberate V1 Exclusions

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

#### 7.9.4. Semantic Profiles

Semantic profiles state closed Input-visible meaning that is not supplied by structural family names alone.

Profiles may define Leaf-level or other explicitly owned presentation distinctions such as bounded text, bounded bytes,
UUID, decimal, date, instant, identifier, token, path, or URI meaning. A familiar host type name does not grant the
profile.

A profile cannot import an Admission predicate, Policy decision, external resource state, ambient locale, provider
state, normalization authority, or arbitrary executable validation into Input merely by calling that material
presentation meaning.

Section 7.8 determines whether a bound belongs to Input or to a later authority. ADR-0073 separately determines whether
one Java or Kotlin surface can lawfully preserve an already-declared Input presentation in the requested role.

### 7.10. V1 User-Facing Authoring Classes

The V1 user-facing policy classifies host forms only to determine whether they can express an already-declared Input
presentation. The host category does not create Contract meaning.

| Authoring class                                                  | V1 treatment                                                                                                           | Examples                                                                                                                                                                                    | V1 frontend consequence                                                                                                                                              |
|------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Direct closed immutable presentation                             | Refine and ratify directly when every declared distinction is complete, immutable, supported, and legal under ADR-0073 | Primitive and `String` source forms, closed enum source forms, approved immutable Leaves, qualifying semantic profiles, flat Kotlin data class, flat final Kotlin carrier, flat Java record | The selected declaration may supply external Contract evidence without a duplicate carrier declaration; actual invocation material remains subject to Input judgment |
| Closed structured meaning without a ratified direct host mapping | Require explicit formation into a supported presentation before Input                                                  | Nested DTO, embedded Value Object, nested collection carrier, unsupported sealed hierarchy, host collection surface whose full observable law cannot be preserved                           | The semantic presentation may be legal under Section 7.9, but this host carrier does not become authority merely because its shape resembles that presentation       |
| Implementation-shaped carrier                                    | Require explicit formation or reject the source                                                                        | Mutable JavaBean, framework DTO, proxy, entity, custom getter, delegated property, interface root, runtime-discovered implementation, third-party lifecycle object                          | Host conventions, mutable observation, lifecycle, and implementation relationships do not become Input meaning                                                       |
| Behavior, capability, movement, or role leakage                  | Reject from Input                                                                                                      | Callback, lambda, validator, service, repository, clock, executor, transaction, resource handle, stream, future, core Fact authority, State, backend handle                                 | The material belongs to another role or is not Input presentation data                                                                                               |

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
requirement. These forms are a direct-support profile, not a restriction on the nested semantic presentations permitted
by Section 7.9.

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

Input may carry a declared identifier, token, coordinate, source text, bounded bytes, or another approved value that
refers to something outside the machine. The execution capability or resource ownership represented by that value
remains outside Input authority.

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

Input can refuse at definition time or at invocation time.

Definition-time refusal means that the selected source could not form a legal Input Definition Candidate or could not be
established under the Input law. No authoritative Input Definition exists for that selection.

Invocation-time refusal presupposes an established Input Definition. It occurs when required material is unavailable or
when the supplied material fails the declared presentation law. Admission is not entered after that refusal.

Sections 7.3 through 7.7 define presentations that are not repaired into legality at invocation time. A stop established
by another applicable Contract retains that Contract's authority and is not reclassified as Input refusal.

## 10. Relationship to Later Contracts

Input establishes judgeable boundary presentation and no later pipeline authority.

Admission owns continuation, as stated in Section 7.8. Canonicalization, when selected, may establish a stable
same-shape representative without redefining Input. Lowering owns the later relation from direct Input coordinates
toward Operation parameters and Fact coordinates. Nested constituents under Section 7.9 do not become Lowering source
coordinates merely because they are addressable inside the Input presentation.

Those later authorities do not enter Input Definition identity. Their refusals also remain distinct from Input refusal
under Section 9.

## 11. Open in This Section

The final authoring token spelling and exact public Java or Kotlin carrier syntax for Input remain open. Section 7.9
fixes semantic families without requiring those family names to appear in generated APIs.

Membership uniqueness equivalence and Association key equivalence still require one ownership decision. The remaining
question is whether the constituent presentation itself always supplies the required equivalence or whether an Input
collection presentation may select a separate exact semantic equivalence profile. Arbitrary executable equality, runtime
`equals`, object identity, backend hash, or comparator callbacks are not an answer.

The corresponding exact semantic source and reference form for an `Ordered By` law remains open with that equivalence
work. Section 7.9 already fixes the separation between no observable order, encounter order, semantic ordered-by law,
deterministic compiler order, and physical storage order.

The authority model for reusable semantic presentation profiles remains open when a profile requires more than
compiler-known closed meaning. Until that work is complete, an exact profile reference is legal only when some
already-defined semantic owner makes the reference meaningful.

Different Policy Worlds are permitted to select different Input Definitions for the same Interaction under Section 4.4.
What remains open is the Governance and Interface-realization API that lets an external caller operate under the
applicable world and the generated host API strategy that projects any resulting Policy-variable Input. This ADR does
not require those distinct Input Definitions to share one host carrier.

The exact HIR representation of Policy-owned Contract World constituent relations remains Policy and HIR work. Input
requires an exact legal selection relation and forbids reconstruction of Input Definition Meaning from that relation,
but this ADR does not decide whether Policy exposes those constituent relations through dedicated IDL Binding Candidate
projections, direct Policy Candidate relations, or another semantically equivalent producer-owned projection.

The physical HIR schema, table layout, dense handles, canonical compiler traversal order, equality acceleration,
collection realization, and the exact Java/Kotlin support matrix remain Design or ADR-0073 work. Those choices may
optimize the established meaning but cannot redefine Section 7.9.

Any future presentation-family expansion must preserve the authority boundaries established here. In particular, it
cannot make runtime graph traversal, snapshot timing, open-world evolution, or backend representation a new source of
Input meaning.

## 12. Consequences

Input becomes an explicit immutable boundary presentation rather than a host object treated as Contract authority.

The directly addressable Input surface remains flat, while one direct coordinate may carry a finite, closed, acyclic
nested value presentation. This keeps Contract authority topology simple without forcing Input values to be scalar-only.

The six-family V1 presentation algebra gives HIR and Establishment a closed semantic target while leaving Java/Kotlin
carrier choice, storage layout, collection realization, and generated API projection replaceable.

Collection order is represented only when Input or a ratified platform surface makes it observable. Compiler canonical
order and physical storage order remain realization.

Some application objects therefore cannot cross the boundary directly even when their business data could be represented
by a legal Input presentation. They need an ADR-0073-compatible direct mapping or explicit presentation formation before
invocation. That cost preserves the distinction between application representation and Input authority.

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
authorities or Lowering coordinates. Collection-visible order is separated from value equality, deterministic compiler
order, and storage order. Tuple, Multiset, generic Result, open or dynamic structures, recursive presentations,
capabilities, resources, and executable or time-dependent forms are not V1 fundamental Input families. Exact Membership
and Association equivalence-source ownership remains open.

ADR-0048 remains the owner of the shared inbound-airlock composition and core-entry relation.