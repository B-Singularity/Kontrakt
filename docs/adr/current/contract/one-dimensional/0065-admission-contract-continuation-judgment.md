# ADR-0065: Admission Contract, Explicit Continuation Judgment, and Deterministic Evaluation Boundary

## Status

Accepted

## Date

2026-09-01

## Extracted From

ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0066: Canonicalization Contract
- ADR-0064: Input Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform-Native Contract Ratification and External Contract Infiltration Boundary
- ADR-0075: Compiler-Produced Material and Knowledge, Protocol-Mediated Consumption, Validity, Reuse, and Incremental
  Boundaries
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0053: Version Contract
- ADR-0057: Failure Contract
- ADR-0060: Diagnostic Evidence and Retention Contract (Proposed)
- ADR-0067: Lowering Contract
- ADR-0077: Contract Relation Infrastructure

---

> **Revision record.** The 2026-10-16 and 2026-10-17 reviews completed the two inbound Basis paths, HIR and
> Established Protocols, Definition and Occurrence Establishment, and the separation of Admission outcomes from Failure,
> Diagnostic, and Compiler Verification. Section 15 records the sequence. The normative rules are in Sections 5–11.

## 1. Context

Admission judges whether the complete presentation supplied by a legal inbound application may continue. It consumes
Input's `Presented` occurrence and, when Canonicalization is selected, the successful whole `Canonicalized` occurrence.
The two Required Basis paths and the meaning of the final judged presentation are specified in Section 6.7 and
ADR-0048; Input and Canonicalization retain their respective authorities under ADR-0064 and ADR-0066.

Users author an ordinary Java or Kotlin judgment and select it through the IDL. The declaration is not itself Contract
authority. Admission's complete Definition meaning is formed and established through Sections 5 and 6; source refinement
and the prohibition on hidden observation are specified in Sections 7 and 8. Admission-local computations do not create
material for later Contracts (Section 7.2).

---

## 2. Problem

A Boolean callback is too weak to act as Contract authority. A callback can return a Boolean while obtaining its answer
from hidden runtime state, exception control flow, unresolved dispatch, or implementation-specific library behavior. The
return type alone says nothing about whether the judgment is closed or deterministic.

Admission therefore needs a host-language source surface whose complete relevant meaning can be resolved before
authority is granted. Accepted source must become a closed, terminating, deterministic Admission judgment over legal
Input observations and closed Admission Definition material. When the frontend cannot establish that meaning, it rejects
or reports unsupported source through the compiler boundary rather than allowing runtime behavior to supply the missing
semantics.

Frontend coverage may grow without changing the Contract. Supporting another source spelling or library operation
expands what the compiler can understand; it does not expand what Admission is allowed to observe or establish.

---

## 3. Decision Drivers

Admission owns a single flat continuation judgment with occurrence outcomes `Admitted` and `Rejected` (Sections 5 and
11).
The Admission role is explicitly selected, and neither source organization nor host runtime identity grants it. The
selected Definition states its own Input Observation Requirement without adopting a particular Input Definition
(Sections 5 and 6.3).

The judgment must have closed, deterministic, completing meaning. Sections 7–10 specify which ordinary source operations
may form that meaning and prohibit hidden state, exceptional control as a result, and undeclared effects. Finite
computation
is permitted, but Budget, Capacity, and compiler or backend resource limits retain separate authority (Sections 7.4,
7.5, and 10). Definition Refusal, occurrence Rejection, and compiler-owned unsuccessful results remain distinct
(Sections 6.5 and 11).

---

## 4. Authority Path

Definition-time authority follows ADR-0071 and ADR-0063, specialized by Sections 6.3–6.6:

```text
ordinary Java or Kotlin Admission declaration
-> exact Admission role selection
-> frontend resolution
-> complete implementation-erased Admission Definition Candidate
-> Resolved HIR seal and visible handoff
-> Admission Definition judgment
-> Established Admission Definition or Admission Definition Refusal
-> execution formation for an Established Definition
```

The IDL Binding Candidate records selection, not Input-to-Admission compatibility. ADR-0048 owns that composition;
ADR-0075 owns mediation and reuse of compiler-produced information. Neither creates Admission meaning or substitutes
for the owning Establishment judgment.

Occurrence-time authority is separate:

```text
exact Established Admission Definition
+ fresh legal Admission application
+ applicable complete boundary presentation
+ legally satisfied Required Basis
-> Admission judgment
-> Admitted or Rejected
-> Established Admission Occurrence
```

Sections 6.7 and 6.8 define its exact Basis and attribution. Physical evaluation without a completed trustworthy
judgment cannot establish an Occurrence (Section 11).

---

## 5. Selection and Declaration Law

### 5.1. Explicit Admission Selection

The applicable IDL or Contract World arrangement selects one exact declaration for one exact Admission role occurrence.

```text
exact selecting context
    Interaction A / Admission
        -> example.OrderAdmission
```

The source-layout heading does not create semantic hierarchy or composition. Role authority comes from the resolved
selection relation itself. The selected name may use ordinary source imports, but resolution must end at one exact
declaration.

Host organization is not a substitute for selection. File placement, annotations, parameter types, inheritance, or
runtime discovery cannot grant the Admission role. An unselected Admission role has no Definition or Occurrence merely
because its source declaration exists.

### 5.2. One Flat Admission Contract

One selectable class declaration names one flat Admission Contract. Its authored methods belong to that one Admission
source; they do not become independently selectable Contracts. The complete continuation judgment may be written as one
Boolean method, as several independent Boolean condition methods, or through exact-target helper calls. Independent
Boolean method conditions are conjoined into the one judgment. A Boolean method used as a helper contributes where it is
called, not a second time as an independent condition. No privileged root method or additional IDL method selection is
required. For example, independent `positive(v)` meaning `v > 0` and `small(v)` meaning `v < 1000`, when legally bound
to
the same Input observation, compose `(v > 0) && (v < 1000)`.

The frontend must resolve the complete meaning of these methods and their legal Input observations under Sections 6 and

7. Their names, source order, and host calls do not determine Admission authority. All authored member forms remain
   subject to source qualification; getter and setter members of the selected Admission class are not permitted
   authoring
   forms. Closed constants and local values may contribute to the same judgment without becoming separate Contracts.

Several Admission declarations may coexist in one source file. Conversely, several legal selection occurrences may
deliberately select the same Admission declaration. In that case the Admission Definition may be shared, while selection
attribution and later occurrence relations remain exact to each use.

Inheritance, overrides, marker interfaces, and virtual specialization do not create Admission meaning. Reuse of
Admission meaning occurs by selecting the same flat declaration, not by deriving another Contract through a host type
hierarchy.

### 5.3. Selected Roles and Explicit Absence

ADR-0046 and ADR-0047 preserve `Selected` or `Explicit Absence` for each role at its owning scope. An absent role has
no Definition, Occurrence, success result, or refusal. Absence itself does not establish that the surrounding flow is
legal. ADR-0048 judges the exact combination.

When Input and Admission are selected, the two Canonicalization choices give distinct paths:

```text
Input Presented -> Admission                 (Canonicalization explicitly absent)
Input Presented -> Canonicalization Canonicalized -> Admission  (Canonicalization selected)
```

These paths require legal composition and completed upstream judgments. In the second path, a refused whole
Canonicalization Occurrence cannot supply Admission, even if some coordinate assessments succeeded. If Input is
`Refused`, neither path enters Admission.

If Admission is explicitly absent, it establishes no judgment or outcome. If Input is explicitly absent, it supplies
no Presentation to a selected Admission or Canonicalization. Neither missing role is replaced by a default stage or
fictitious successful result. The owning composition law determines the legality of each declared combination. An
absent constituent inside a selected Input Presentation is different from an absent Input role.

---

## 6. Input Observation and Composition Boundary

Admission Definition meaning declares the complete Input observations required by the judgment, independently of any
particular Input Definition. ADR-0064 owns the offered presentation; ADR-0048 determines whether an exact legal
composition satisfies Admission's requirements. Resemblance between host fields or JVM types is not sufficient.

The frontend may obtain Input HIR observations to form that requirement, but the Input Candidate consulted during
formation does not thereby become an Admission Definition determinant (Section 6.3). At occurrence time, the judged
presentation follows the two paths in Section 6.7, without reopening the host carrier. User Admission code observes
the applicable values without branching on their internal producer references. Constituent observations retain their
Input-owned meaning, and temporary Admission computations do not establish downstream values (Sections 7.2 and 8).

For example, if Input establishes `code = "A"` and `amount = 10`, and selected Canonicalization establishes the
representative `code = "a"`, then `code == "a" && amount > 0` is true for Admission. With Canonicalization explicitly
absent it is false; a refused whole Canonicalization occurrence prevents Admission entry. This assumes a legal selected
Canonicalization Law for the coordinate.

### 6.1. Ordinary Source as Authoring Evidence

The illustrative Kotlin source formerly in this section is preserved in
`../../../../design/admission/admission-authoring-api-and-source-surface-working-design.md`. A separate Authoring API
document
will describe supported source forms.

An ordinary host declaration supplies authoring evidence, not Admission authority. Authority begins only after the
complete judgment meaning has been resolved and established.

### 6.2. Input Occurrence and Admission Occurrence

Input and Admission establish different Occurrences. A fresh legal Admission application has its own semantic Occurrence
identity even when its values and outcome equal an earlier application's. Repeated or parallel physical evaluation of
the same exact application does not create additional Occurrence authority. ADR-0063 §4.4–4.5 governs this common
identity boundary.

An Admission Occurrence is Established only when its judgment produces one trustworthy `Admitted` or `Rejected` result.
It preserves the exact application and applied Definition, the complete Applicable Basis attribution specified in
Section 6.7, and its outcome. Section 6.8 defines the authoritative observation of that material. Reclamation of
physical backing does not undo Establishment; retained backing alone does not confer current validity (ADR-0063 §§9,
11.6–11.7).

### 6.3. Definition Candidate Meaning and Semantic Equality

A complete Admission Definition Candidate contains only Admission-owned meaning. It contains the complete judgment law,
the complete Input Observation Requirement Law, and the Occurrence Basis Requirement Law defined in Section 6.7. It also
contains any other closed Admission-local meaning whose change can alter that Definition.

The judgment law preserves the exact meaning of every contributing condition and operation. Closed constants and helper
computations are resolved as meaning, not retained as host calls. Control flow matters where it changes the judgment or
its completion. Frontend refinement does not have to remove redundant conditions or prescribe one optimized expression
form. For example, a private constant `minimum = 100` may resolve into a judgment `amount >= 100` without executing a
class instance. Eliminating an additional `amount > 0` condition requires a separate valid meaning-preserving
optimization; it is not a prerequisite for Candidate formation.

The Input Observation Requirement Law preserves every distinction the judgment actually requires. A nested read must
retain the necessary constituent observation. A finite collection predicate must retain the required member
observations and the applicable collection meaning. A presence test requires the exact absence distinction. A key
comparison requires the applicable key sameness. Order matters only when the judgment observes it. The current Input
Definition and its particular coordinate binding do not enter Admission Definition meaning merely because the frontend
used them to resolve the source.

The Occurrence Basis Requirement Law retains the mandatory `presentedInput` requirement and the conditional
`selectedCanonicalization` requirement of Section 6.7. It states which Established results a later Admission application
must have. It does not contain the current role selection, a concrete Input or Canonicalization Occurrence, a Basis
Binding, or an Applicability result. These are later application and composition meanings, not Definition determinants.

Formation context remains outside that meaning unless the Admission law explicitly says otherwise. Compiler formation
may consult material that does not belong to Admission meaning. The selected Input Candidate is one example, and source
provenance is another. Host representation and compiler bookkeeping likewise remain outside Definition determinants
unless an Admission law explicitly says otherwise.

Admission owns semantic equality for its Definition Meaning. Two meanings are equal only when every Admission-owned
determinant is equal under its owning law. This includes the Occurrence Basis Requirement Law. Frontend refinement may
remove source-only differences under ADR-0071, but it cannot invent an Admission equality law. Equal results on sample
inputs do not prove equal Definition meanings, and optimizer rewrites do not confer Contract equality. This semantic
equality does not imply CandidateRef equality, Definition identity, Version equality, or Occurrence identity. Compiler
interning or cached reuse cannot create any of those semantic relations.

### 6.4. Resolved Admission HIR Candidate Protocol

Admission specializes ADR-0071 with two typed HIR references. It retains the two complete projections and exposes
Input observation requirements through two narrower producer-owned projections:

```text
AdmissionDefinitionCandidateRef
AdmissionIDLBindingCandidateRef

Admission Definition Candidate Projection
Admission IDL Binding Candidate Projection

Admission Input Observation Requirement Surface Projection
Admission Input Observation Requirement Meaning Projection
```

The Definition Candidate Projection is complete for the full Candidate meaning described in Section 6.3, including the
conditional Occurrence Basis Requirement Law. The IDL Binding Candidate Projection is complete only for its exact
selection relation and does not carry Input-to-Admission compatibility meaning. Two different Interaction selections
may target the same Admission Definition Candidate while retaining distinct Binding Candidate references. Neither
selection makes its Input or Canonicalization Definition an Admission Definition determinant.

The Admission Input Observation Requirement Surface Projection exposes the complete set of observations required by
one exact Candidate. It lets a consumer discover the whole required extent without reading the judgment body. Adding a
requirement changes that extent even if every previously observed requirement remains the same.

The Admission Input Observation Requirement Meaning Projection exposes the complete meaning of one exact requirement
selected from the surface. A presence check must preserve the possibility of an absent value. For example, a judgment
that accepts absent `code` must not become a requirement that `code` be present. A collection observation must preserve
the member meaning needed by the judgment. A requirement that depends on another observation must retain that relation
or provide an exact legal way to observe it.

The producer must make each requested requirement exactly selectable from its surface. Source position does not define
that selection. Neither does a physical HIR index. The precise subdivision and selector form remain for narrower design
when a legal observation requires them. No additional Primary HIR subject or Requirement authority is introduced.

CRI and other legal consumers may combine several projections. The surface must account for the whole requirement
extent. Each selected meaning must be complete. A partial read does not prove that other requirements are absent. An
unavailable projection does not mean an absent requirement. Combined observations must be coherent under ADR-0071.
CRI prepares the resulting correspondence; ADR-0048 composition owns the compatibility judgment.

The equality of a projection covers only its declared observation. Equal surface membership does not prove equal
requirement meanings. Equal meaning for one requirement does not prove the whole surface equal. Equal Input
requirements do not prove equal Admission Definitions, since their judgments may differ. These equalities also do not
establish identity or current reuse validity.

The former single Input Observation Requirement Projection is replaced by the Surface and Meaning Projections. The
consumer path described in ADR-0077 may obtain the complete requirement by combining those observations. The
conditional Occurrence Basis Requirement Law remains visible through the complete Definition Candidate Projection.
No separate Basis or Judgment Law Projection is introduced. Source operators and helpers do not each need a Projection.
Several logical projections may share backing or be served by one bulk read. An additional narrow projection requires
a separate legal observation need.

### 6.5. Admission Definition Establishment Judgment

The Admission Definition judgment enters only after one complete Admission Definition Candidate is available as valid
Visible HIR. It consumes the complete Candidate meaning through the Admission HIR protocol together with the
authoritative Version prerequisite required by ADR-0053 and ADR-0063.

Neither the Admission nor the Input IDL Binding Candidate is an input to this judgment. An Input Definition Candidate or
Established Input Definition is not an input either. Those materials belong to selection, frontend formation, or later
composition rather than Admission Definition Establishment.

The owning judgment determines whether the resolved Candidate defines one lawful, flat Admission continuation judgment.
It applies the Definition obligations owned by Admission under Sections 5 through 10. The judgment must have one
complete
Boolean continuation meaning. Its declared Input Observation Requirement must be lawful. Its conditional Occurrence
Basis Requirement Law must also be lawful under Section 6.7. These are Admission-owned judgments, not decisions about
whether a particular Input Definition is compatible or an actual Occurrence Basis is available.

Admission requires closed, deterministic, and completing judgment meaning under Sections 7 through 10. Compiler source
qualification and verification establish the technical obligations needed to form and trust that meaning. The Admission
Definition judgment does not redo source analysis or execute a compiler proof algorithm. A successful compiler
verification cannot itself establish an Admission Definition.

The entered Definition judgment has two Admission-owned outcomes:

```text
complete resolved Candidate that satisfies Admission Definition law
    -> Established Admission Definition

complete resolved Candidate that violates Admission Definition law
    -> Admission Definition Refusal
```

A lawful Admission Definition may reject every actual Input. The satisfiability of its continuation condition is not
an additional Definition Establishment requirement. Its truth is judged at occurrence time.

`Admission Definition Refusal` presupposes that a complete Candidate legally entered the judgment. If the frontend
cannot resolve required meaning, the HIR handoff is invalid, or recovery-tainted material cannot become valid Visible
HIR, the Definition judgment never enters. That condition remains compiler-owned rather than being rewritten as a
Contract refusal. An inconclusive compiler verification does not prove an Admission Definition violation. The
responsible compiler boundary retains its own unsuccessful result. A definite Admission-law violation yields Admission
Definition Refusal only when the complete Candidate has legally entered the owning judgment.

ADR-0071 §§4.8 and 8.9 owns HIR Seal, coherent observation, extension compatibility, and the prohibition on
recovering hidden meaning from source or compiler storage. For Admission, Visible HIR must retain exact Candidate and
Binding references, a complete Judgment Law, and the complete Requirement Surface with every exact Requirement Meaning
obtainable under Section 6.4. An unresolved requirement is neither an empty Surface nor a legal recovery value.
A Binding Candidate must identify one exact selection without arbitration. The Seal does not judge Admission legality;
that belongs to the entered Definition judgment above. Actual Basis Binding and Version Binding remain separately owned
under ADR-0063.

A successful Admission Definition judgment establishes this complete semantic material:

```text
Established Admission Definition
    exact EstablishedAdmissionDefinitionRef
    exact Owning Admission Authority Binding
    exact authoritative Version Binding

    complete Established Admission Definition Meaning
        one complete Admission Judgment Law
        complete Input Observation Requirement Law
            complete requirement surface
            exact meaning of each requirement
        complete Occurrence Basis Requirement Law
            mandatory presentedInput
            conditional selectedCanonicalization
        other Admission-owned meaning when required

    complete direct established relations
        only relations owned by the Admission Definition law
```

The Judgment Law retains the precise continuation meaning, not a result for any actual Input. The Observation
Requirement
preserves what the judgment may legally observe. The Basis Requirement Law states the prerequisites of future Admission
applications without binding them here. No actual Input or Canonicalization Occurrence is established by this Definition
judgment. Neither is current Input compatibility or inbound composition.

The exact Authority and Version bindings follow ADR-0053 and ADR-0063. Admission does not choose a Version or take the
binding authority of its prerequisites. Source qualification evidence and compiler verifier results remain
compiler-owned.
They are not Definition Meaning or separate Admission Established Material. The Definition directly establishes only its
own relations; it does not duplicate a relation owned by another Contract.

Successful Establishment creates one complete authoritative Admission Definition; there is no partially visible
Admission Definition. Its identity and reference follow Section 6.6. Reuse or persistence may avoid compiler work, but
only the current legal judgment can grant current Admission Definition authority. The same rule applies to a retained
Definition Refusal: compiler reuse cannot transfer that Contract result to a different current Candidate.

Only that complete Established Definition may become visible through the Canonical Contract World. Visibility creates
no new authority. A future Occurrence Basis is not required for Definition visibility because only its requirement law
belongs to Definition Meaning. Later realization verification cannot retroactively change the Established Definition.

### 6.6. Definition Identity, Version, and References

ADR-0053 owns Admission Authority continuity and Contract Version semantics. ADR-0063 owns the common Established
Definition identity and reference law. Admission only specializes those laws.

One Admission Authority owns one independently addressable Admission Definition per Contract Version in the current flat
model. No additional Authority-Local Definition Coordinate is therefore required.

```text
exact Admission Contract Authority
+
exact Contract Version
    -> exact versioned Admission Definition
```

Before Establishment, `AdmissionDefinitionCandidateRef` denotes the exact resolved Admission Authority together with the
exact authority-scoped Resolved Version Candidate Coordinate. The authored Version Claim must resolve to that coordinate
before the Candidate becomes valid Visible HIR. Source location and compiler representation identity cannot complete the
reference. The selected Input identity is also outside this reference.

After Establishment, `EstablishedAdmissionDefinitionRef` denotes the exact Owning Admission Authority together with the
exact Version Binding. CandidateRef and DefinitionRef remain different reference domains; success does not cast or
promote one into the other.

Operation, Interaction, exact Input Definition, and the ADR-0048 composition relation stay outside Admission Definition
identity. The same Admission Definition may therefore be selected by several legal operation arrangements or composed
with different exact Input Definitions without becoming a new Definition.

A Contract-visible change to Admission Definition meaning follows the normal Version law when the Authority continues.
Changing its Occurrence Basis Requirement Law is such a change even if its Boolean condition is unchanged. A source-only
or realization-only change does not create a new Definition when every legal Admission observation remains unchanged.
Equal Definition Meaning also does not merge distinct Contract Versions. If conflicting meaning is presented for the
same Authority and Version coordinate, ADR-0053 and ADR-0063 own the conflict law.

For example, two Interaction selections may reuse the same versioned Admission Definition with different legal Input
Definitions. A new Version with unchanged Admission Meaning remains a distinct versioned Definition. Submitting a
different judgment or Basis Requirement Law for the same Authority and Version is instead a conflict under the owning
Version and Establishment laws.

### 6.7. Occurrence Basis, Applicability, and Composition Boundary

Every Admission Occurrence requires the exact Input Occurrence that established its original Presentation. When
Canonicalization is selected for the same inbound application, Admission also requires its successful whole Occurrence.
These are two distinct Required Basis coordinates. The reusable requirement law belongs to Admission Definition meaning;
each exact Admission application owns the Required Basis instances that the selected path requires.

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

When Canonicalization is explicitly absent, `selectedCanonicalization` is not instantiated. It is not an unresolved
requirement and does not use permitted absence. The `presentedInput` requirement remains mandatory in either path.
Individual fields or collection constituents are observations of one coherent Input Presentation, not separate
Required Basis instances.

ADR-0048 owns inbound-airlock composition. It determines the legal upstream sources for the exact dependent application.
Basis Resolution follows that established relation. It does not select a producer by host type, compiler reachability,
physical adjacency, or search order. The same Admission Definition may participate in different legal compositions.
If an exact upstream Definition changes, the owning composition law must establish the relation for the current
subjects. An older relation cannot be inherited.

For an exact Admission application, legal Basis Resolution binds `presentedInput` to the exact already-Established
`Presented` Input Occurrence. When Canonicalization is selected, it also binds `selectedCanonicalization` to the exact
already-Established `Canonicalized` Occurrence supplied by that composition. The Canonicalization Occurrence must have
judged the same Input Occurrence bound to `presentedInput`. Two individually successful upstream Occurrences do not
satisfy Admission if they belong to different Input origins or different legal inbound applications. An occurrence
from an Input Definition outside the legal upstream source domain does not form a Basis Binding. It is not bound first
and then repaired by an `Inapplicable` judgment.

For example, Input Occurrence `I-1` establishes `Presented`, and Canonicalization Occurrence `C-1` establishes
`Canonicalized` using `I-1`. Both can supply their respective Required Basis for the same legal Admission application.
If `C-1` instead used `I-2`, it cannot supply that application's Canonicalization Basis. Equal values do not repair the
source mismatch. When Canonicalization is explicitly absent, the application requires only `I-1`.

A `Refused` Input Occurrence cannot supply `presentedInput`. A `Refused` Canonicalization Occurrence cannot supply
`selectedCanonicalization`, even if some coordinate assessments succeeded. The missing successful source is not
replaced with the original Input values or an invented identity Canonicalization.

Basis Binding and Applicability remain separate under ADR-0063. The exact source connection must first satisfy the
binding-owning Composition law. Applicability then judges each exact Binding for the dependent Admission application.
V1 needs no additional Applicable Context. Once a legal Binding exists for this exact application, it is Applicable
under the owning law. An unavailable legal observation causes compiler-side non-entry; it is not a missing Applicable
Context. Nor does the absence of additional context permit ambient reads of Policy, State, Version, or compiler state.

Complete Basis requires exactly one Applicable Basis for every instantiated requirement coordinate. Finding one
Applicable source is not enough if the legal source domain may contain another. An unresolved or Inapplicable
requirement prevents Admission entry. Multiple Applicable sources for an exactly-one coordinate also prevent entry.
Admission chooses none of them by priority or discovery order. These cases do not establish `Rejected` or an Admission
Contract Failure.

CRI may prepare the exact selection, source correspondence, and coverage evidence under ADR-0077. It does not establish
Composition, Basis Binding, Applicability, or Complete Basis. These retain the authorities assigned by ADR-0048 and
ADR-0063. In particular, a complete compiler observation is not itself an Established source relation.

The two paths use the already-Established Input and Canonicalization meanings. Neither path requires a new
Final Presentation Authority or a copied whole-presentation material. The exact legal composition makes the complete
Presentation available for Admission judgment. The semantic relations may share a physical representation when their
exact meaning remains recoverable. Compiler dependency or cache structures cannot replace them. Section 6.8 exposes
the selected Canonicalization predecessor through the Established Admission protocol.

### 6.8. Established Admission Semantic Protocol

Admission exposes Established meaning through two typed reference domains:

```text
EstablishedAdmissionDefinitionRef
EstablishedAdmissionOccurrenceRef
```

Requirement meaning remains part of Definition meaning, and outcome meaning remains part of Occurrence meaning. V1
therefore needs no third Admission-specific authority reference. The Established Semantic Protocol of ADR-0063 is
read-only and does not grant the consumer new judgment or source authority.

The Established Admission Definition Projection is complete for one exact Definition. It exposes the DefinitionRef, its
Owning Admission Authority and Version Binding, the complete local Definition Meaning, and its directly established
relations. Definition-reference equality and Definition-meaning equality remain different relations.

The Established Admission Occurrence Projection is complete for one exact semantic application. It exposes the
OccurrenceRef, the applied Admission DefinitionRef, every Required Basis instantiated for that application and its
exact Applicable Basis attribution, and the single `Admitted` or `Rejected` result. Its Admission-owned meaning is:

```text
Established Admission Occurrence
    exact EstablishedAdmissionOccurrenceRef
    exact legal Admission application
    exact applied EstablishedAdmissionDefinitionRef

    complete Applicable Basis attribution
        presentedInput
            exact established Basis Binding
            exact EstablishedInputOccurrenceRef
            required established outcome: Presented

        selectedCanonicalization
            only when Canonicalization was Selected
                exact established Basis Binding
                exact EstablishedCanonicalizationOccurrenceRef
                required whole established outcome: Canonicalized

    exact legal same-Input-origin relation of the selected sources
        when selectedCanonicalization is instantiated

    one complete Admission-owned outcome
        Admitted or Rejected
```

The Direct Relation Projection exposes the exact links specified in the complete Occurrence material above.
Admission owns its link to the applied Definition and its occurrence attribution. The upstream Composition, Basis, and
Canonicalization source relations remain with their respective owners under ADR-0048, ADR-0063, and ADR-0066 (Section
6.7). These links do not embed upstream presentations or create inverse source authority. One observed link
does not prove the complete set of required Applicable Basis relations.

V1 provides the following fine-grained Established observations:

```text
Established Admission Input Observation Requirement Surface Projection
Established Admission Input Observation Requirement Meaning Projection
Admission Occurrence Outcome Projection
```

The two Established Requirement Projections retain the Surface/Meaning completeness, exact selection, and equality
boundaries of Section 6.4, but use Established Definition authority rather than Candidate references. Their legal
combination and the difference between an unqueried requirement and an absent requirement follow that section.

The Occurrence Outcome Projection exposes only the exact `Admitted` or `Rejected` result of one exact Occurrence. It
is not by itself a complete account of applicable Basis or diagnostic explanation. Outcome equality, Requirement
Surface equality, one Requirement Meaning equality, whole Definition equality, and Occurrence identity are distinct.
Equal narrow observations do not merge their enclosing Definitions or Occurrences and do not establish reuse validity.

Reuse of Established Requirement Projections must preserve the complete current Surface, including additions and
removals, not just previously selected meanings. Coherent observation and reuse qualification follow ADR-0063 §11.7
and ADR-0075 §§13–17; equal retained observations alone do not establish current validity.

For a current Admission application, a reused Complete Basis observation must cover the legal source domain for every
instantiated exactly-one requirement. Finding the previously Applicable source is insufficient if another source may
now be legally Applicable. This can invalidate reuse of the earlier completeness result without undoing the earlier
Established Occurrence. A selected Canonicalization requirement cannot become explicitly absent because its projection
is unavailable. Basis completeness and current reuse qualification remain with their existing owners, not this Protocol.

The exact Occurrence and Judgment Meaning remain legally observable for Failure and Diagnostic under Section 11 and
ADR-0057. Source locations, compiler evaluation records, and Diagnostic Evidence do not thereby become Admission-owned
direct relations.

Admission defines no field-specific or consumer-specific Contract projection. A consumer may select a legal
producer-defined observation but may not redefine its completeness, equality, or source authority. If a consumer
combines Admission observations into new compiler knowledge, that derived result belongs to the compiler producer or
to another explicit Contract authority that actually owns the new meaning. Failure, Diagnostic, and Lowering may
consume only their respectively authorized observations; none can reopen the raw Input carrier or reinterpret a
Canonicalization representative.

Protocol availability does not alter semantic authority. Semantic absence is different from a projection that exists
but is not retained or not yet materialized. Unavailable, unsupported, stale, or corrupt backing is different again.
None of those states can revoke already Established Admission meaning or fabricate Definition Refusal, `Rejected`, or
Failure. ADR-0075 owns qualification, retention, persistence, mediation, and reuse across responsibility boundaries.
Any valid reused, restored, repaired, or parallel path must expose the same legal observation as clean formation.

---

## 7. Supported Judgment Source Law

Admission is defined by its continuation judgment, not by a fixed catalog of Java or Kotlin spellings. The frontend may
accept ordinary source whenever it can resolve the complete Admission-visible meaning from legal Input observations and
closed Admission Definition material.

A source form is legal because Kontrakt knows and preserves its semantics, not because the JVM happens to execute it
successfully. Frontend support may expand to new source forms without changing Admission authority, provided they
resolve to the same already-defined semantic surface.

### 7.1. V1 Judgment Coverage Target

V1 should cover ordinary value-oriented judgment code broadly enough that users do not need a second validation
language. Boolean logic is in scope when its operators have explicit semantics, including finite conditional choice and
the ordinary closed Boolean algebra. Numeric judgments are also in scope. Integral semantics must fix width and
signedness, and they must define overflow, narrowing, and shift behavior wherever those distinctions can affect the
judgment. The selected floating law must define classification and ordering as well as equality and arithmetic. It must
also preserve raw-bit relations when that law exposes them, so source purity alone does not permit a reassociation that
changes the result. Decimal operations likewise keep presentation-sensitive equality separate from numeric comparison
when the chosen semantic profile distinguishes them.

Admission may judge finite alternatives such as enums, presence or absence, nullability, closed product members, and
`Closed Tagged Choice` payloads when Input exposes those distinctions. Character, text, and binary values may be
inspected through exact finite relations; containment and slicing are representative examples. Range and positional
operations are legal only when the required position or order is itself observable.

Finite arrays and the Input `Sequence`, `Membership`, and `Association` forms are also within the intended V1 surface.
Admission may derive collection size from the complete finite Input Presentation. Input does not define a separate size
operation; form-defining cardinality constraints, when present, remain Input-owned.

Keyed lookup may be accepted when its complete meaning can be derived from the Association's exact key/value relations
and
Key Presentation Sameness. Quantification and reduction, such as `all` and `count`, may likewise be accepted over exact
finite presented domains under Section 7.4.

Judgment-local pipelines follow the same rule. An unordered `Membership` does not acquire encounter order merely because
a host API offers `first` or `last`; Admission may derive a
temporary order only under its own exact supported ordering law.

### 7.2. Ordinary Expressions and Temporary Computation

The frontend may refine ordinary closed control flow when its complete meaning is known. Ordinary value expressions and
finite control flow may disappear after refinement. Literals and finite branches are representative source forms rather
than separate Contract meaning. Early exit is also legal when it remains inside the same closed control-flow meaning.
They do not create nested Contract outcomes.

Admission-local mutation is legal only for fresh judgment-local state. That state must not alias mutable Input or
Definition material, escape the judgment, or become shared. The compiler must also know how every relevant update
contributes to the final result. Host immutability markers such as `val` or `final` do not prove these properties.

A `for` traversal over an exact finite Input-derived domain may be accepted directly. A `while`-style loop may also be
accepted when the frontend can establish its finite progress and completion from closed material. General recursion or
open-ended state-dependent loops remain unsupported in V1 when that proof is unavailable; this is a frontend coverage
limit rather than a permanent semantic ban on their syntax.

Temporary derived computation is permitted when its operations have exact supported semantics and its result does not
escape the judgment. The rule covers scalar interpretation as well as judgment-local collection reshaping or
aggregation. If such computation introduces a relation that Input did not provide, that relation must become explicit
Admission meaning. A new equality or ordering rule is a representative case; host conventions cannot silently supply the
missing relation.

### 7.3. Known Operation Refinement

A helper or library call may participate only when Kontrakt can remove that call as a source of Admission authority or
preserve a separately ratified platform obligation under ADR-0073.

An exact-target helper is acceptable when all semantically reachable behavior that can affect the judgment can be
resolved into the one composed Admission judgment. The closure includes implicit behavior, not just the visible body.
For example, a property read may execute a getter and an operator may resolve to user code. The helper's name, call
frame, and JVM target do not become Admission meaning.

An overload that depends on undeclared ambient state is not accepted merely because a neighboring overload is closed.
Behavior whose exact callable meaning cannot be closed remains unsupported. This includes unresolved dispatch and
unprofiled callback or library behavior.

Purity is not inferred from annotations, finality, familiar naming, or Boolean return type. Legality follows the
resolved semantic operation and all reachable effects, as further constrained by Section 8.

### 7.4. Finite Collection, Pipeline, and Binder Condition

Admission may inspect collection constituents only through distinctions owned by the Established Input presentation.
Input also decides the collection's membership or key relation and whether order or cardinality is semantic. If the
Input law does not expose one of those distinctions, a host collection implementation cannot supply it implicitly.

A finite Input collection need not have one fixed maximum cardinality. A traversal is legal when the compiler can prove
that it ranges only over the actual finite presented domain through supported operations. Resource limits on that work
belong to Budget, Capacity, compiler safety, or backend policy rather than to Admission semantics.

A lambda or function object is not itself Admission material. Such syntax may appear inside a recognized finite binder
when its body is closed, does not escape, and has exact invocation semantics. This matters for mutation. A sequential
binder with a known invocation law may use a fresh local accumulator. An API that may reorder or duplicate callbacks
cannot make captured mutation semantic unless that invocation law is itself closed.

Order-sensitive operations require a semantic order. If Admission derives a temporary order, its ordering and tie
behavior must resolve every distinction that can affect the result. A host sort being stable does not manufacture a
deterministic first element when the comparator itself leaves Input-distinct values tied. Conversely, if the final
Admission result is invariant under every legal tied permutation, Admission need not invent an observable tie order.

Reduction legality is operation-specific. Side-effect freedom does not imply associativity; floating arithmetic is a
representative case. A backend may reassociate or parallelize a reduction only when the exact result law permits it.

### 7.5. Completion, Exceptional Control, and Termination

Every legally entered Admission occurrence judgment must complete with exactly one of `Admitted` or `Rejected` for every
legal presentation to which its Definition applies.

Only semantically reachable source paths contribute to the judgment. A compiler may discard a provably unreachable
branch without lowering or realizing its otherwise lawful operations. Unreachability does not make a source form
forbidden by the applicable authoring law, or invalid under the source language, lawful. If the compiler cannot
establish the unreachability, rejecting the source is a conservative V1 coverage decision.

A partial operation is legal only when its domain is established on every path that reaches it or when its partiality is
represented by an explicit non-exceptional semantic result. A guard that establishes the domain of a later operation is
therefore semantically relevant even if the original source evaluation order is not. Optimizations may not move that
operation onto a path where the guard no longer protects it.

Exceptions are not branches of the Admission result. Catching a host exception and translating it into `Rejected` or
`Admitted` is therefore illegal in V1. The same rule applies when realization fails for unrelated resource or VM
reasons: Section 11 keeps those outcomes outside Admission.

Finite processing may scale with the actual finite Input extent. A fixed Definition-time step count is not required. A
loop form whose progress depends on external progress, blocking, synchronization, or an unclosed recursive cycle remains
unsupported because the compiler cannot establish completion from Admission-owned semantics alone.

### 7.6. Hermetic Source Acquisition and Initialization

Kontrakt must not execute user Admission code to discover Admission meaning. The frontend may inspect source,
compiler-owned representations, and ratified platform knowledge, but application initialization is not a discovery
mechanism.

A closed literal or constant initializer may contribute Definition material when its complete meaning can be resolved
without running application behavior. Host declarations such as `static final` or Kotlin `val` are insufficient by
themselves because their initialization may still execute effects or refer to mutable state.

The selected class must be qualified as a whole for forbidden initialization behavior. A constructor or initializer
block that performs application behavior is illegal even if the judgment never calls it. For example, an unused `init`
block that calls `System.out.println` is not permitted. This class-level source rule does not change Section 7.5:
provably unreachable paths within an otherwise legal judgment do not contribute to its judgment meaning. The compiler
may erase such a path but cannot execute it to decide whether it is harmless.

Source qualification failure does not by itself establish Admission Definition Refusal. The compiler and the entered
Definition judgment retain their separate result ownership under Section 6.5.

### 7.7. Pattern and Regex Source Condition

Pattern syntax is frontend evidence. Admission does not grant authority to a particular host regex engine or to its
matching strategy.

A supported pattern must be reducible to exact Admission-visible matching semantics together with a supported work law
over the finite pattern and presented text or bytes. If result semantics, capture behavior, Unicode basis, or required
work cannot be closed, the source is unsupported rather than delegated to arbitrary host behavior.

---

## 8. No Hidden Observation

Admission may depend only on semantic input declared by its own Definition and by legal Contract relations. Admission
cannot read meaning directly from the surrounding application or process. External resources, ambient runtime state, and
dynamic or asynchronous capabilities must stay outside the operand set unless another law first turns the required
information into semantic material. A repository or clock is therefore no more admissible as hidden judgment input than
reflection over a live application object.

The Input protocol is not a reflective escape hatch. Internal references and compiler coordinates may support exact
interpretation or attribution, but user Admission code cannot inspect compiler storage, generations, caches, or semantic
references as arbitrary data.

If Admission needs additional information, that information must first become explicit semantic material under an owning
law. The implementation cannot silently capture the current locale, Policy World, State, provider setting, or other
ambient value merely because it is reachable.

Section 7.4 is the only path by which Stream- or Sequence-like syntax can participate: the finite source and operation
semantics must be closed, and the live host pipeline must disappear as authority.

Open runtime subtype discovery and callback completion are likewise forbidden. Ordinary host syntax may still express a
type test or pattern when the frontend resolves it to an Input-owned `Closed Tagged Choice` or another exact closed
relation rather than to dynamic host identity.

User logging, metrics, tracing, or event publication are externally observable effects and do not participate in V1
Admission source meaning. Kontrakt may add its own observation instrumentation separately when that instrumentation
cannot alter Admission-visible semantics.

---

## 9. Deterministic Refinement

Admission Definition Establishment requires a complete, legally resolved Candidate as defined in Section 6.5. Source
refinement must preserve the Admission-owned judgment and the observation requirements defined in Sections 6 through 8.
Frontend acquisition steps, capability coverage, and implementation techniques are recorded in the Admission working
Design.

---

## 10. Deterministic Evaluation

At occurrence time, Admission evaluates the Established Definition against the exact applicable presentation
specified in Section 6.7, using legally qualified observations under ADR-0075. Section 8's hidden-observation
prohibition applies during realization as well as source refinement.

Host evaluation strategy is replaceable. Short-circuit syntax, iteration shape, temporary allocation, and matcher
implementation may disappear when they are not Contract-visible. Any Input-visible order, guarded domain, exact numeric
result law, or other distinction that can affect the Admission result must still be preserved.

A host operation with several specification-permitted semantic outcomes is not made deterministic by compiler
preference. V1 may accept it only when Admission selects one exact semantic operation, when all permitted outcomes are
indistinguishable to the final judgment, or when another explicit preservation law justifies the narrowing.

Resource accounting remains separate from semantic legality. An operation can be finite and semantically valid while
expensive. Budget, Capacity, compiler-safety, or backend limits retain the stopping result when such work exceeds their
own law.

An optimized, fused, cached, restored, or parallel realization must judge the same lawful Presentation supplied by the
current inbound composition. It cannot replace a selected Canonicalization representative with the original Input value
or change a guarded operation's applicable domain. The exact collection, numeric, and other Admission-visible laws
remain
binding even if a transformation preserves the Boolean result on sample inputs. When a Diagnostic Evidence obligation
is legally established, its required attribution and observations must also be preserved. Compiler Verification owns
these preservation checks; neither an equal Boolean alone nor a cache hit establishes Contract conformance.

Admission determinism is defined by its semantic determinants:

```text
same Established Admission Definition meaning
+ same complete applicable boundary Presentation meaning
+ same additional Admission-owned determinant, if one is explicitly defined
= same Admission result under the same legally applicable relations
```

Compiler scheduling, cache state, storage layout, worker count, and unrelated Contract results do not add determinants
to this equation. The surrounding Contract World likewise contributes only through semantic material that an owning law
explicitly makes relevant.

Equal results and duplicate physical evaluations do not change the Occurrence identity law of Section 6.2.

---

## 11. Result Law

The occurrence-time result is exactly `Admitted` or `Rejected`. The one composed Boolean continuation judgment maps to
that result only after all contributing conditions and helper computations have been resolved into legal Admission
meaning. Definition-time `Admission Definition Refusal` remains the separate result defined in Section 6.5.

`Admitted` means that the complete applicable boundary presentation satisfies this Admission Contract's continuation
condition. It does not authorize the whole Interaction or predict the result of later Contracts.

`Rejected` means that a legally entered Admission judgment completed and found its own continuation condition
unsatisfied. The determining Input Occurrence remains a valid upstream occurrence and is not rewritten as `Refused` or
erased.

An Admission Occurrence becomes Established only when the judgment completes with one trustworthy result. Starting
execution or failing to obtain `Admitted` is insufficient. The exceptional and resource conditions described in Section
7.5 are not a third Admission result and do not automatically become Failure Contract meaning.

ADR-0057 determines the Failure meaning and cardinality of a rejected Admission judgment. One exact unsuccessful
Admission Occurrence judgment establishes one semantic Failure with its exact source, unsuccessful continuation meaning,
applicable context, and active processing boundary. Several false source subexpressions do not become several
Admission judgments or several Failures. Conversely, other independently established unsuccessful judgments at the
same boundary retain their distinct Failures. Failure does not choose a primary failure, and independently reachable
boundary processing is not suppressed by discovery order.

Diagnostic explanation may describe one or several unsatisfied conditions within that one unsuccessful judgment.
A false subexpression is not automatically an unsatisfied obligation: for example, when `A || B` is true because `A`
is true, the false value of `B` does not explain an Admission rejection. Negation, choice, and guarded computation must
be interpreted under the complete established Judgment Meaning. Admission does not establish separate condition
results or a new source-coordinate identity merely to produce an explanation.

The proposed Admission-oriented default for Diagnostic Evidence is to explain every independently evaluable ordinary
condition that contributes to a rejection and is legally applicable to the same exact Presentation. A source
short-circuit alone should not silently omit another independent, safely observable unsatisfied condition. This is a
Diagnostic coverage proposal, not a new Admission Outcome or unconditional Evidence Establishment law in this ADR.
ADR-0060 owns whether and where that default is a required Diagnostic Evidence obligation, its closed selection, and
the exact scope of completeness; until that decision is made, a consumer may not claim universal complete condition
coverage merely from the existence of `Rejected`.

An additional condition may be observed for explanation only when its domain and dependencies are satisfied without
changing the Admission judgment or reading undeclared state. When `code != null && code.startsWith("A")` rejects an
absent `code`, the presence condition can be reported unsatisfied; `startsWith` is inapplicable, not a second false
judgment. For `if` or a closed choice, unselected branches are not failures. Any additional observation is tied to the
same exact occurrence and applicable Presentation; it is not arbitrary replay of user code or an assertion that an
unreached source expression executed.

For finite collection predicates and repeated computation, the correct aggregate Boolean result remains mandatory,
but a Diagnostic obligation need not enumerate every failing member or iteration. A deterministic representative
witness may be given when its exact semantic selection is legal. For an Input-owned Sequence, an exact semantic
position can select a witness. An unordered Membership does not permit the backend's first encountered element to
become a stable witness. If no lawful deterministic member selection exists, the aggregate predicate itself remains
an exact explanatory location; a member-level witness is not fabricated. A result such as `any == false` still requires
whatever complete evaluation its result law needs. Omission is permitted only for repetitive diagnostic detail, never
for the Admission result or required evidence under the applicable Diagnostic definition.

A required explanation must report the exact coverage it guarantees. Within a declared complete ordinary-condition
scope, independent conditions cannot be dropped by runtime schedule, evaluation order, or resource preference. A
partial or unavailable Diagnostic observation cannot be reported as a complete account. Diagnostic explanation and
Compiler Provenance may identify source expressions, but source line numbers, physical iteration order, and evaluation
traces do not become Failure identities or Admission semantic determinants. Compiler verification owns the semantic
preservation and non-interference of any evidence capture or additional explanatory evaluation. The Diagnostic law
owns evidence establishment, availability, and lawful disclosure.

Diagnostic evidence may explain an Admission result, but it does not promote discarded host execution mechanics into
Admission authority. Likewise, a stop owned by another Contract keeps that owner's result rather than being translated
into `Rejected`. Section 12 identifies the neighboring Contract authorities.

The Definition-time non-entry and Refusal boundary is specified in Section 6.5. At occurrence time, Section 5.3 and
Section 6.7 govern role absence, upstream refusal, mismatched origins, incomplete or non-singular Basis, and unavailable
required observations. None establishes Admission `Rejected` or an additional Admission Failure. ADR-0057 preserves
other independently reached failures. `Rejected` stops continuation without revoking its upstream Input or
Canonicalization occurrences.

---

## 12. Relationship to Neighboring Contracts

ADR-0064 and ADR-0066 own the Input Presentation and selected Canonicalization representatives; ADR-0048 owns the
complete applicable presentation and its composition. Section 6.7 states which of their Established occurrences
Admission requires. A refused Canonicalization cannot provide a partially successful Admission operand.

Admission-local parsing or normalization remains temporary under Section 7.2; it cannot supply an established
presentation for a later Contract. Any later Contract needing such derived meaning must establish or consume it under
its own law. Selected Lowering must consume the same lawful presentation meaning Admission judged (ADR-0067), without
receiving Admission's authority to perform Lowering or establish core Facts. Policy, Governance,
Budget, Capacity, State, Version, Failure, and Diagnostic retain their own authority under their respective ADRs.
Their material is not an implicit Admission operand (Section 8).

---

## 13. Closure and Deferred Work

Sections 5–11 contain Admission's normative law. The remaining items below belong to authoring, Diagnostic, Compiler,
or another Contract owner; their unfinished implementation or separately owned decisions do not redefine Admission.

### 13.1. Closed Admission-Specific Decisions

The Definition, HIR, identity, and Basis decisions are in Sections 6.3–6.7; the Established Protocol and current-use
conditions are in Section 6.8. Section 11 owns the Admission outcome and its connection to ADR-0057. These clauses,
not the closure summary, are the authoritative statements of their meanings.

### 13.2. Frontend and Capability Work That Does Not Block Semantic Closure

The frontend Design and later Authoring API document own exact Java or Kotlin spellings and the V1 source-capability
matrix. New forms do not change Admission meaning when they resolve to the same closed judgment law under Section 7. A
change to Admission's semantic input, output, authority, or result vocabulary requires a separate Contract decision. The
transferred implementation work is recorded in the Admission working Design.

### 13.3. Owned Elsewhere or Derived

ADR-0064 owns Input observation meaning; ADR-0048 owns inbound composition; ADR-0053 and ADR-0063 own Version,
Establishment, Basis, Applicability, and the Established Semantic Protocol. ADR-0071 owns the common HIR Protocol and
Seal. ADR-0075 governs compiler-produced observations and reuse. Sections 6.3–6.8 contain only Admission-specific
specializations of these laws.

Compiler-derived compatibility, transitive or Whole-Machine knowledge, optimization results, and unsuccessful compiler
results do not become Admission-established meaning or authority (ADR-0075). In particular, a reused correspondence
cannot grant a current Composition or Basis judgment; Section 6.8 governs the exact current observations required.

### 13.4. Moved to Design or Narrower Compiler Architecture

Compiler Design chooses how Admission is represented and realized. It also chooses analysis and verification mechanisms,
but those mechanisms must preserve Admission meaning. Source qualification and compiler verification own the technical
analysis needed to establish supported closed judgment meaning. They neither perform the Admission-owned Definition
judgment nor become its authority. Later realization verification checks implementation against Established meaning; it
cannot rewrite that meaning. Resource enforcement remains subject to its separately owning law.

Diagnostic realization may plan additional explanation of independent ordinary conditions and deterministic finite
collection witnesses, or capture eligible evidence during the original evaluation. It must preserve the original
Judgment Meaning, guards, Input-owned collection relations, and the exact source Occurrence. The physical plan may
combine evidence collection with outcome evaluation or perform a legal separate observation; no second Admission
judgment is established. Compiler Verification owns the non-interference and preservation checks. The default
Diagnostic Evidence declaration, required coverage, exact witness-selection capability, and completeness behavior
remain for the owning Diagnostic design and ADR-0060 review rather than an Admission-specific verifier or evidence
schema.

Compiler Design and Verification must test that clean, reused, repaired, and parallel paths preserve the same legal
Admission observation. Conformance testing must check the exact applied Presentation, outcome, Basis attribution,
Failure source, and any required Diagnostic Evidence, not only the final Boolean. Changes to requirement membership or
legal Basis candidates, stale or corrupt retained material, and transformations of guarded or collection computations
must not silently validate an incomplete observation. These tests do not become Admission judgments.

Source analysis and realization must also account for expensive but finite collection work and pattern matching without
turning resource stops into `Rejected`. Compiler and backend limits, cache integrity checks, and their physical cost
remain implementation responsibilities. Independent logical Projections need not imply individual queries, dependency
nodes, or cache entries; V1 may share compact backing, and V2 may replace scheduling or reuse mechanisms under the same
current-valid Protocol law. Narrow observations may permit compiler early cutoff without reusing a complete Definition,
subject to ADR-0075's current-validity and authority boundary.

Admission determinism does not by itself promise constant-time execution, confidentiality, or side-channel resistance.
Detailed Diagnostic Evidence may also contain sensitive Input observations. The owning Diagnostic and outward disclosure
laws determine who may receive those details; an exact internal explanation is not permission to publish it. Any
additional timing or information-flow guarantee requires its separately owning law.

---

## 14. Consequences

Admission is an explicit, flat continuation Contract rather than a general Boolean callback. Ordinary supported Java
and Kotlin remain authoring inputs; the compiler forms and verifies their complete meaning without making user code
or compiler state authoritative (Sections 6 and 7).

The separation of Outcome, Applicable Basis, and current reuse preserves independent Contract authority and permits
replaceable implementation, subject to Sections 6.7–6.8, 10–11, and ADR-0075. `Rejected` affects continuation only;
it does not undo the upstream occurrences that determine its application.

---

## 15. Migration History

This history records prior reviews, not additional normative Admission law. The current decisions are in Sections 5–11.

The ADR originated in the Admission-owned part of ADR-0048. Its extraction left inbound-airlock composition with
ADR-0048 and Admission judgment with ADR-0065.

The **2026-09-22 Input review** replaced a direct-coordinate-only operand with the complete Input-owned presentation.
It permitted nested observations without new nested authority, kept temporary computations local, and settled the
finite traversal, local mutation, exception, hermetic-acquisition, and pattern conditions now in Sections 7 and 8.
A subsequent occurrence review separated Admission occurrence identity from physical evaluation and retention and
required a prior `Presented` Input Occurrence (Sections 6.2 and 11).

The following **binding review** removed exact Input identity from Admission Definition meaning and assigned
Input-to-Admission compatibility to ADR-0048 (Sections 6.3 and 6.7). The **2026-09-25 HIR and compiler-product review**
introduced producer-owned semantic equality, the originally single narrow observation-requirement projection, and
compiler mediation under ADR-0071, ADR-0063, and ADR-0075. It also separated Definition Refusal, compiler non-entry,
and occurrence Rejection. The subsequent identity review fixed Definition identity at Authority plus Version, required
exactly one `presentedInput`, and retained no extra V1 Applicable Context (Sections 6.5–6.8).

The **2026-10-16 flow review** incorporated Canonicalization-before-Admission and Explicit Absence. At that stage the
selected-Canonicalization Basis and attribution still awaited detail. The subsequent Basis review added the conditional,
exactly-one whole `Canonicalized` Basis from the same Input origin, with composition owned by ADR-0048 and common Basis
machinery by ADR-0063. The Candidate review included that conditional requirement in Definition HIR and distinguished
source refinement from optimizer-owned rewriting. It also qualified selected-class initialization.

The later **2026-10-16 HIR Protocol review** separated the requirement Surface from each exact requirement Meaning.
The **2026-10-17 Definition review** completed Admission-owned Definition Establishment while separating Compiler
Verification. The **2026-10-17 Occurrence review** completed the Established Basis attribution and the matching
Established requirement projections. It preserved one Admission outcome per Occurrence and ADR-0057's Failure
cardinality; detailed Diagnostic coverage remained separately owned. The final closure review connected current-valid
reuse, non-entry, conformance, and compiler-security responsibilities to their existing authorities.