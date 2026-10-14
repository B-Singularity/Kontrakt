# ADR-0077: Contract Relation Infrastructure — Relation Information Preparation, Source Candidates, and Coverage

## Status

Accepted

## Date

2026-10-11

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend
- ADR-0047: One-Dimensional Contract Presentations and Selection
- ADR-0048: Inbound Airlock Composition, Boundary Refinement, and Core Entry (`Migration Pending`)
- ADR-0050: State, State Transition, and State-Machine Axis
- ADR-0051: Budget Contract
- ADR-0052: Capacity Contract
- ADR-0053: Version Contract
- ADR-0054: Policy Contract
- ADR-0055: Whole-Machine Pipeline Composition and Contract Concurrency (`Accepted`)
- ADR-0056: Governance Contract (`Proposed`)
- ADR-0057: Failure Contract
- ADR-0063: Contract Establishment, Identity, Applicability, and Composition (`Accepted`)
- ADR-0064: Input Contract
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0069: Invariant Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary (`Accepted`)
- ADR-0073: JVM Platform and External Technology Boundary (`Accepted`)
- ADR-0074: Compiler Result and Unsuccessful Result Boundary (`Proposed`)
- ADR-0075: Compiler-Produced Material, Protocol-Mediated Consumption, Validity, and Reuse (`Accepted`)
- `docs/design/overview/kontrakt-compiler-total-architecture-map-design-draft.md`
- `docs/todo/compiler-engine/kontrakt-compiler-architecture-closure-and-implementation-readiness-todo.md`
- `docs/todo/compiler-engine/kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `docs/verification/checklists/kontrakt-1d-hir-establishment-master-checklist.md`
- *Modern Compiler Architecture 01–15*, especially its separation of Contract semantic relations, compiler computation
  dependencies, producer–consumer protocols, and subsystem responsibilities.

---

# 1. Context

Kontrakt's 1D pipeline has explicit relations between separately owned Contract judgments. An Admission
Definition declares the Input observations it needs, while the Input Definition and applicable IDL
selection expose their own meaning. Preparing that Admission judgment still requires the compiler to
obtain those observations and connect them for the exact composition. Canonicalization, when selected,
adds a distinct selection and observation requirement. Policy, Budget, Capacity, State, Failure, and
Diagnostic consumers likewise need legally available information from earlier responsibilities.

ADR-0071 exposes resolved HIR candidates and IDL selections. ADR-0063 governs Required Basis, Basis
Binding, and Establishment. ADR-0075 supplies legal cross-responsibility observation mediation. These
laws do not require each 1D implementation to know another Producer's lookup or storage mechanism.

This ADR introduces **Contract Relation Infrastructure (CRI)** to prepare information for compiler-time
relation uses within a 1D pipeline. Directly declared connections and questions requiring candidate
or coverage calculation use the same preparation responsibility. Whole-Machine and Cross-Core relations
remain outside V1 (§14.8). Runtime acquisition of concrete Occurrence Material is a separate
architecture problem.


---

# 2. Problem

A resolved relation does not by itself obtain the observations needed to use it. Without a shared
preparation responsibility, each 1D implementation must navigate other Contracts' projections,
reconcile the exact selection context, and assemble the information required for its judgment. That
repeats compiler work and couples independent authorities to one another's acquisition paths.

A discovered source may also have the right kind but the wrong occurrence. Finding one matching source
cannot rule out others when the owning law permits several. A universal relation manager would create
the opposite problem by duplicating Frontend, Establishment, and general compiler mediation.

CRI therefore needs one narrowly defined information-request and preparation boundary. Sections 4 and
6 define how the observations are obtained; §§7–11 define the resulting correspondence, sufficiency,
and handoff. CRI does not retain a pipeline-wide aggregate of the acquired material.


---

# 3. Decision Drivers

The relation use and required information must be identifiable before acquisition. Exact selection
context and source attribution must survive the correspondence calculation. The adequate observation
depends on the question, not on how much material was physically scanned.

Already established meaning remains with its Producer. CRI must not own a second lifetime for that
material. V1 recomputes its preparation on request; optional reuse of underlying producer observations
is an optimization, not a CRI semantic prerequisite (§12).

The subsystem must use authorized, coherent observations. It must validate the correspondence it
prepares and remain independently verifiable. Shared preparation must not displace Contract authority
or duplicate ADR-0075 mediation.


---

# 4. Decision and Responsibility Model

Kontrakt recognizes **Contract Relation Infrastructure (CRI)** as the compiler-time subsystem that
prepares the information needed by one exact Contract relation use. The owning law supplies the legal
question. CRI determines which already-defined observations that question requires, acquires them
through ADR-0075, and prepares their exact correspondence for the owning judgment. Source-candidate
calculation and requirement-relative coverage are part of this responsibility when the law requires
them.

**CRI owns the accuracy of its preparation, not the legal meaning or Establishment of the relation.**
The Required Basis retains the meaning defined by its owning law. The Binding-owning law defines the
legal Source Domain and establishes Basis Binding. Applicability and Complete Basis remain with their
owners under ADR-0063. HIR resolution, IDL selection, Inbound Composition, and other compiler relations
retain their existing owners. Runtime relation-material acquisition is excluded from CRI V1.

The compiler-time responsibility boundary is:

```text
Owning relation use / judgment
    ↓ exact semantic context + required information
CRI
    ↓ requests lawful producer-owned observations through ADR-0075
HIR Candidate Protocol / Established Semantic Protocol
    ↓ exact references + producer-complete authorized projections
CRI
    ↓ prepared correspondence + sufficient observations and evidence
Owning composition / judgment
    ↓ authoritative relation, if the owning law establishes one
Established Material under its original authority
```

This is a logical boundary, not a mandatory additional physical pass or a new material store. Where one
producer-owned direct relation already fulfills the request, preparation may consist solely of
obtaining and forwarding that observation; CRI must not manufacture a duplicate relation. Section 11
governs that immediate handoff.

## 4.1. Primary Responsibility One: Exact Relation-Use Demand

A CRI demand identifies one **exact owning relation use** and the information question defined by
its owning law. An existing typed HIR selection, Established relation, or Required Basis reference may
already determine the exact use and question, without repeating those determinants as extra fields.
For a composition demand, the exact Dependent Application and owning composition law identify its
use. If they admit several independent uses, that law supplies the local coordinate needed to
distinguish them.

A selected IDL Binding Candidate can identify its particular slot selection, but it does not by
itself identify an entire Inbound Composition. In particular, a Composition use must not depend on
the presence of any one optional role. Nor may preparation require a reference to the Established
Composition result that it is supposed to help establish. The owning law determines the exact
Dependent Application and any necessary local coordinate; CRI issues no new Contract identity.

A reusable Definition Requirement Law is not an occurrence-specific Required Basis. A Definition-time
request must not invent a future Occurrence or demand runtime values. Source references, Producer
lists, physical Query Keys, and storage handles are not mandatory demand fields: they are obtained or
resolved under the existing law and Protocols. Section 6 defines the acquisition path for HIR
candidates and already-established compiler-time meaning.

## 4.2. Primary Responsibility Two: Authorized Relation Information Acquisition

CRI requests the narrowest sufficient observations through ADR-0075. It follows exact selecting or
direct references supplied by the relevant Producer Protocols; it does not discover their meaning
through host identifiers, compiler object containment, or unrestricted World traversal.

A relation may need observations from more than one Producer. CRI relates those observations to the
same exact owning question, and passes them onward only when they can form a coherent, legal handoff.
The source Producers retain the original observations and their lifetimes. Section 6 owns the detailed
acquisition rules.

## 4.3. Primary Responsibility Three: Correspondence and Candidate Preparation

CRI prepares the exact correspondence between the required observation and the observed source
coordinates or material. It preserves the distinctions supplied by the relevant owning law. Where
several sources are lawfully permitted, CRI also prepares the candidate distinctions needed for the
later judgment. Discovery order grants no preference. The calculation rules are in §7.

## 4.4. Primary Responsibility Four: Requested Sufficiency

CRI checks whether the prepared observations support the precise relation question. An already
complete declared connection need not trigger wider source enumeration. Questions about absence or
singularity may require further producer-guaranteed domain evidence. Section 7.3 owns the coverage
rule; §11 owns completed delivery.

## 4.5. Four Boundary Obligations

The obligations in §§9–12 govern every completed preparation. They do not assign common trust
qualification, retention, or independent verification to CRI.

---

# 5. Scope and Terminology

## 5.1. Contract Relations and Compiler Work Relations

A **Contract relation** carries the meaning defined by its law. IDL Slot Binding, Required-Basis
Binding, and Applicability are not interchangeable even if they refer to similar material.

A **relation preparation demand** is a compiler-time information request for one exact relation use;
it is not a new Contract Required Basis. Its owning question may already be determined by that use
and need not be restated. A **prepared correspondence** associates approved observations with that use
without establishing its legal Binding.

A **compiler relation candidate** is a proposed source for an exact requirement when candidate
calculation is necessary.

**Coverage evidence** supports the candidate set or absence claim for one relation question. Its scope
must remain distinguishable from the question's final legal outcome.

A **compiler computation dependency** records which inputs affect a compiler calculation. It does not
define a Contract relation.

Physical representations may share storage without merging these meanings. V1 CRI serves relations
within a 1D pipeline; this scope does not grant it authority over Whole-Machine composition.
Runtime Occurrence Material lookup and runtime relation assistance remain separately owned.

## 5.2. CRI Is Not the Composition Authority

**Composition** retains its meaning under ADR-0063 and ADR-0055. A composing judgment may establish new
material from earlier Established Material. Neither a prepared source set nor a valid Binding alone
creates a universal `CompositionResult`.

## 5.3. No Universal Relationship Schema

Relation laws differ in their coordinates, cardinality, and relevant context. CRI must preserve these
requirements without imposing one common semantic relation type.

It may consume observations of IDL Binding, Realization Binding, or other subsystem relations
when the owning compiler-time use requires them. Those subsystems retain their Producer Protocols
and the authority or compiler responsibilities to which the observations belong.


---

# 6. CRI Consumption Boundary and Authorized Observations

CRI uses the authorized producer observations needed to fulfill the exact demand in §4.1, under
ADR-0075. A source reference alone is not permission to inspect its material. CRI determines which
producer-defined projections supply the required observations, but general request resolution,
qualification, acquisition, and coherent delivery remain ADR-0075 mediation responsibilities.
The request's sufficiency condition is defined in §7.3; physical access paths remain Design choices.

## 6.1. Definition-Time Requirements

Before Establishment, CRI consumes exact selections and resolved candidates through ADR-0071's HIR
Candidate Protocol. The IDL Binding Candidate Projection exposes the selecting context, selected role,
and target Definition Candidate Reference for an actual selection. It is not by itself a complete view
of roles that were not selected. The Selection-owning HIR subject must also expose, through its
producer-owned Fine-Grained Semantic Projection, a complete observation of selection or explicit
Absence at an exact role coordinate in the correct owning scope. CRI assumes this observation
contract; ADR-0071 and the selection-owning laws must provide it. The owning 1D's other Fine-Grained
Semantic Projections expose only the observations needed for the relation question. HIR carries
resolved candidate meaning but not Established Definition authority.

An inbound composition preparation may require the Input Candidate Coordinate Surface Projection of
ADR-0064 §5.8, the Canonicalization Declared Selection Surface and Selected Law Relation Projections
of ADR-0066 §9.4, and the Admission Input Observation Requirement Projection of ADR-0065 §6.4.
These are distinct observations. An IDL Binding Candidate Projection alone does not establish current
Input compatibility or carry an Admission judgment's requirement law.

CRI cannot fill gaps by reparsing source, refining a 1D's definition, or treating generated host
artifacts as new Contract meaning. The source observations must be authorized for the definition-time
question and must not be represented as already-established material.

## 6.2. Occurrence-Time Requirements

Where a compiler-time preparation concerns an exact application, it preserves the dependent
application and its Required Basis coordinates. Its Definition may be reusable, but the exact
Required Basis belongs to that application. Only material already established by its source
authority can support an authoritative Basis Binding.

CRI may consume such material through ADR-0063 §11's Typed Established Semantic References, Direct
Established Relation Projections, and relevant Fine-Grained Established Projections. Established
Definition meaning is not a substitute for an actual Occurrence result; an earlier Occurrence is not
a substitute for the current one merely because its value is equal. This ADR does not prescribe how
runtime Occurrence Material will be located (§5.1).

## 6.3. Applicable Context and Scoped Input

Where the requirement depends on an Applicable Context, CRI uses that exact context. It cannot
substitute ambient compiler state such as a process-wide `currentWorld`.

Conversely, it must not add Version or scope coordinates that the requirement does not make relevant.
ADR-0063 assigns those determinants to their respective laws.

## 6.4. Source and Material Availability

A known source reference may still be unavailable or ineligible for the requested observation.
Availability cannot justify substituting another source unless the requirement permits it.

Ambiguous references cannot be resolved through incidental host identity or compiler storage layout.

## 6.5. Request-to-Observation Acquisition

For one requested 1D relation use, CRI starts with the exact owning use and information question
defined in §4.1. It obtains that use's lawful selection or requirement observations through
ADR-0075 and follows the exact Producer references they expose. It may acquire observations in
stages: an earlier observation may determine which later Projections are needed. Once independent
requests are known, mediation may acquire them together. This is a semantic acquisition dependency,
not a prescribed two-pass algorithm or fixed physical evaluation order.

The following illustrates existing inbound Protocol surfaces, not a new common relation payload:

```text
Exact Interaction / owning Inbound Composition use
    → complete role-selection observations in their owning HIR scopes
    → Input Candidate Coordinate Surface Projection, if Input is selected and needed
    → Canonicalization selection and selected-law projections, if selected and needed
    → Admission Input Observation Requirement Projection, if Admission is selected and needed
    → exact correspondence prepared for the composition-owning judgment
```

The same demand may require only a Direct Established Relation Projection instead of these HIR
surfaces. CRI requests the observations required by the owning use at its compiler stage; it does
not obtain future Occurrence values for Definition-time preparation. An exact direct reference is
followed through its Producer Protocol, not by searching for a similar source.

All observations used for one prepared answer must satisfy ADR-0075 §15, including observations
acquired at different steps. CRI cannot repair incompatibility by comparing physical generation
numbers or selecting the latest-looking source. If the requested guarantee cannot be supported by
coherent, legally qualified observations, CRI does not complete the handoff (§§11, 15).

## 6.6. Complete Selection and Optional Roles

Every 1D role, including Input and Output, may be explicitly absent in its selection surface. CRI
assumes that HIR and Establishment preserve that declared meaning and expose the complete lawful
observation through their respective Producer Protocols. The selecting scope may belong to an
Interface, Interaction, Operation, or another subject established by its owning selection law. CRI
must request the role coordinate from that scope rather than assuming every role belongs to the
Interaction.

An exact role is observed as selected through its Binding reference or as explicitly absent through
the complete selection observation. A missing Binding row, partial projection, unavailable
observation, or failed lookup is not evidence of Explicit Absence. The legality of omitting a role,
and whether another selected contract requires that role, belong to the relevant declaration and
composition laws; CRI neither approves the absence nor turns it into a compiler acquisition failure.

When a role is selected, CRI acquires only the producer-owned observations needed by the owning
question. It preserves duplicate or invalid candidate distinctions for that role's own judgment.
For selected Canonicalization, the inbound law supplies the applicable coordinate correspondence
and final-presentation requirement. CRI prepares the requested Input and Canonicalization
observations, without establishing the Presentation or resolving ADR-0065's pending Basis migration (§14.2). Detailed
Absence representation and acceptance law remain with HIR and Establishment.

# 7. Source Candidate Preparation and Legal Domains

The owning relation law determines which source observations may satisfy a demand. CRI must not
use additional compiler-visible sources to enlarge that domain. Preparing an exact declared connection
and calculating candidates from a plural domain use the same request boundary in §6; neither creates
a new Contract relation.

Sections 7.1–7.3 determine what CRI must preserve when it correlates those observations.

## 7.1. Nominal Source Coordinates

When an IDL role or an already-established direct relation supplies an exact source reference, CRI
prepares that reference for the current use rather than searching by kind or name. In HIR it preserves
the selecting Binding Candidate and its target; after Establishment it preserves the corresponding
exact authoritative reference without treating the earlier Candidate as its authority.

For example, the Input Candidate Coordinate Surface of ADR-0064 and the Admission Input Observation
Requirement of ADR-0065 can be related only under the applicable inbound composition law. Equal field
names or host-compatible representations do not establish correspondence. If a requirement names Input
Occurrence `I1`, material from `I2` cannot satisfy it even when both presentations have equal values.
The inbound-composition law owns the legal source definition; its pending migration is not settled
here.

## 7.2. Plural Candidate Domains

When several sources are permitted, CRI obtains the observations authorized for the owning law's
source domain and prepares their correspondence to the exact Required Basis coordinate. The same
Established source may support distinct Required Basis uses without merging those requirements.
Discovery order does not supply arbitration. Neither structural similarity nor compiler cost permits
an unapproved reduction of the candidate domain. CRI may combine overlapping complete observations
only with producer-owned reference identity and a lawful scope/coverage relation; physical duplicate
reads do not create additional semantic candidates.

## 7.3. Requirement-Relative Domain Coverage

Coverage is sufficient for the precise relation question. An exact, lawfully declared connection
may be prepared from its direct observation. When a complete selected-role or coordinate surface
already defines the full legal extent, CRI uses that producer-owned closure; it does not independently
scan the whole Contract World. A claim that any 1D role is explicitly absent requires the
complete, scope-correct selection observation of §6.6.

For a plural domain, a positive existence question may be supported by an exact qualified candidate.
An absence, singularity, or complete-enumeration claim instead requires evidence accounting for every
lawful source that could change that answer. One discovered candidate is not proof of Exactly-one
Applicable Basis. A source Producer may provide complete observation of its own extent without
requiring physical enumeration by CRI. Where several such extents are needed, CRI may combine their
claims only when the owning law's observable domain relation proves they cover the question's legal
extent; producer-local completeness alone cannot prove an unobserved extent absent.

A partial projection cannot establish completeness beyond its declared scope. Inconsistent or
unsupported extent relations are not repaired with generic set semantics invented by CRI. When the
current observations are insufficient, CRI requests further authorized evidence through ADR-0075 or
does not complete the preparation. No fixed acquisition order, universal extent algebra, or mandatory
full-domain scan is required. Section 12 governs any later reuse of this evidence.


---

# 8. Basis, Applicability, and Coverage

A single dependent judgment may declare multiple Required Basis coordinates, including requirements
for the same Fact kind. Each coordinate must remain distinct throughout preparation and judgment.

## 8.1. Binding Is Not Applicability

A Basis Binding identifies a source, but that source may be Inapplicable for the dependent judgment.
An inapplicable bound source is distinguishable from a requirement with no Binding. CRI must retain
that distinction in the evidence requested for either case.

## 8.2. Applicable Basis Is Not Complete Basis

One Applicable Basis cannot satisfy another Required Basis coordinate. A requirement's cardinality
and permitted absence depend on its declared law. Even several Applicable sources may fail that law's
Complete Basis condition.

If Complete Basis cannot be established, the dependent judgment does not enter. Section 15
distinguishes that non-entry from unsuccessful compiler work.

## 8.3. Coverage and Permitted Absence

A completed observation of zero candidates can support a requirement that permits absence. It may
leave a different requirement unresolved. A role's explicit absence in HIR is a distinct selection
fact, not automatically a permitted absence of the dependent Required Basis. CRI preserves those
observations without determining the legal outcome for either requirement.

## 8.4. No Implicit Recovery or Arbitration

An unsettled requirement cannot be repaired with an old Binding or a source from another World.
Compiler-result unavailability is not Contract-permitted absence (§15).


---

# 9. Establishment Integration and Authority Separation

The handoff to an owning composition or Establishment judgment preserves the exact relation use,
the authorized observations it requires, and the scope of supporting evidence. A completed
observation, including an explicitly absent selected role, does not establish that a dependent
Required Basis is satisfied or that the declaring composition is valid. A computed correspondence
or candidate cannot silently acquire the status of an Established relation.

An already Established relation may be observed through its Producer Protocol and prepared for a
later use without repeating its legal judgment. A fresh application with a distinct Required Basis
still needs its own authoritative judgment.

## 9.1. Handoff Non-Amplification

The handoff identifies whether each prerequisite observation is still a resolved candidate or an
Established source. It may not expose more information than the receiving requirement permits.
Permission to inspect a broader Source Domain does not authorize disclosure of its members or
absence claims beyond the receiving requirement. A compiler-derived connection cannot add
determinants to the Contract law.

## 9.2. Semantic Cycles

An unresolved semantic prerequisite cycle cannot be completed through provisional candidates or
repeated calculation. A compiler computation cycle is not necessarily a semantic prerequisite cycle.
CRI must not report a compiler computation cycle as a Contract semantic cycle.

## 9.3. Authority-Specific Composition

ADR-0063 §8.8 governs any new meaning that a higher-scope judgment may establish from earlier
material. Common candidate preparation does not create a new Composition Occurrence for every
valid source connection.


---

# 10. Local Relation Correctness, Trust, and Independent Verification

CRI must validate its relation claims before delivering them. Optional independent verification
cannot replace this production obligation.

## 10.1. Local Correctness at the Production Boundary

CRI verifies that every prepared correspondence matches the owning question's exact references,
selection context, and permitted observation scope (§§6–7). Where it computes candidates, it checks
the required attribution. Where it claims coverage, it checks the lawfully supported extent (§7.3).
Unsupported claims cannot enter a completed handoff. Section 15 governs unsuccessful preparation.

## 10.2. Integrity and Trust of Consumed Material

The original Producer and the trust services of ADR-0075 §§13 and 19 qualify consumed material.
An intact source does not make CRI's subsequent calculation correct. Conversely, a correct
calculation does not make an ineligible source usable.

## 10.3. Independent Relation Verification

An independent Verifier must check the targeted CRI claim against lawful source observations
obtained through the original Producer Protocols. A coverage claim cannot verify itself.

If the available independent observations are insufficient, the Verifier must state the assurance
limit rather than certify completeness. Its result follows the Verification Architecture and
ADR-0075 §20.

## 10.4. Assurance Boundary and Cost

The Verification Architecture determines the optional assurance profile. Each assurance claim must
state what was independently checked. Source access is limited to evidence lawfully available for
that check. Conformance cases are tracked separately from this ADR; resource policy remains with
its owner (§13.4).


---

# 11. Relation-Specific Handoff and Legal Consumption

V1 prepares information on demand and hands it directly to the owning compiler-time use. It does
not create a CRI Result Family, a retained relation aggregate, or a second owner for the HIR and
Established Material it has observed. The demand in §4.1 and the observations acquired under §6
remain separately attributable. Any temporary correspondence table or working buffer ends with the
preparation rather than establishing persistent CRI semantic material.

If another responsibility later needs the same Established information, it obtains the required
producer-owned observation again through ADR-0075. An independently consumed new guarantee derived
by CRI would require a CRI-owned Producer boundary under ADR-0075, but that future choice is not a
V1 prerequisite and does not imply a new physical Result Family.

## 11.1. Completed Preparation Handoff

A completed handoff supplies the exact owning relation-use attribution, the approved references
and projections needed for the requested information question, and any correspondence or
requirement-relative coverage evidence that CRI actually computed. The receiving judgment need
not know which Producer lookup mechanisms were used. It receives neither unrelated material nor
a guarantee stronger than the requested observation. This is request-local delivery using existing
Protocol observations, not a new CRI result schema or persistent Material.

One existing direct established observation may be the whole handoff. Where several observations
are required, CRI completes the handoff only after the acquired information is coherent and
sufficient for the **information question** under ADR-0075 §15. A complete role-selection
observation may report Explicit Absence; that is a completed observation, not proof that another
Contract's Required Basis is satisfied or that the declaration is legal. A complete zero-candidate
observation likewise remains distinct from missing source material or failed acquisition (§15).
CRI does not invent a replacement source or classify an owning law's declaration error.

## 11.2. No Second Universal Mediation Layer

CRI specifies the observations demanded by a relation use; ADR-0075 retains general protocol
resolution, legal acquisition, qualification, and coherent observation responsibilities. Neither a
new universal gateway nor a mandated physical call path is introduced. A direct implementation call
cannot bypass the logical Producer–Consumer boundary.

## 11.3. Information Preservation and Safe Loss

A handoff may omit details only if the requested guarantee and source permissions still hold. Exact
Requirement and Source attribution must be recoverable from the delivered legal observations, not
from CRI-private lookup state. The inbound-presentation obligation remains with its own composition
law (§14.2).

---

# 12. Relation Validity, Reuse, and Incremental Evolution

CRI's V1 work is request-local. HIR and Established Material remain under their Producers' lifetimes;
subsequent requests re-obtain the legal observations through ADR-0075 instead of reading a retained
CRI pipeline context. If later Design introduces independently consumable CRI guarantees or reuse,
ADR-0075 qualifies their current use. The following rules preserve the same meaning across strategies.

## 12.1. V1 Guarantee

V1 performs CRI correspondence and sufficiency calculation when the relation information is
requested. It retains no CRI-owned completed Result, pipeline-wide observation cache, or cross-request
lifecycle. Producer-side material and ordinary compiler-query reuse may still avoid repeated work
behind ADR-0075; those optimizations must provide observations equivalent to clean acquisition.
Definition-level reuse does not replace an occurrence-specific Required Basis (§6.2).

Compiler-derived result identity remains distinct from Contract identity. Physical frozen and cache
arrangements belong to Compiler Design, not the CRI contract.

## 12.2. V2 Guarantee

V2 incremental reuse must account for changes to the legal candidate domain. A retained
Exactly-one coverage claim cannot be justified by checking only the formerly selected source.

Newly eligible sources or changes in eligibility conditions can invalidate an earlier absence or
coverage guarantee. The incremental algorithm and change-propagation policy remain Design choices.

## 12.3. Coherent Observation

Candidate observations and coverage evidence from different generations may form one completed
result only when their compatibility is established. Incompatible selections cannot be combined.
ADR-0063 and ADR-0075 define the visibility requirements.

## 12.4. Equality and Current Use

If a future CRI guarantee is independently produced and retained, CRI must define its equality
and current-use conditions under ADR-0075. This does not require V1 to mint such a result.
Unchanged backing data alone cannot establish unchanged domain coverage.

A provenance-only edit need not change Contract identity, but a Provenance Consumer may still
require an updated observation.


---

# 13. Determinism, Scope, and Cost

Equivalent legally determining inputs must yield equivalent permitted CRI observations regardless
of computation order or acquisition path.

## 13.1. Determinism Under Concurrent Resolution

Concurrent work cannot alter candidate or coverage guarantees. A completed observation cannot
become visible before its supporting evidence is sufficient.

## 13.2. Lawful Scope of Observation

CRI begins with the exact relation use and its declared references. The default acquisition path
follows their producer-defined projections, not a scan of every HIR subject or Established Material.
A wider physical calculation is allowed only through lawful observations. Its broader working scope
must not change the projection the Consumer may obtain (§6), or the evidence's current-use conditions (§12).

## 13.3. Logical Boundaries Without Physical Mandates

Section 4 defines logical responsibilities, not separate JVM modules or passes. One producer-owned
projection may satisfy several observations in one request, and mediation may batch acquisitions.
CRI need not create a query node per coordinate, field, or source reference. Nor does a logically
complete composition require one large retained relation object. Temporary scratch for a request
may reuse immutable backing without obtaining ownership of that backing. Physical sharing must
preserve the Producer–Consumer boundaries of §§6 and 11. Cost and storage belong to Design.
Implementation Design must not settle an unresolved owning law through a storage or lookup choice.

## 13.4. Resource Exhaustion Is Not Contract Refusal

Resource exhaustion may prevent CRI from completing a request. Its unsuccessful-result treatment
is specified in §15; budget enforcement is left to Compiler and Security Design.


---

# 14. Relation-Family Applicability

The following families test which compiler-time relation information CRI can prepare from existing
legal observations, including direct relations. CRI involvement need not mean additional candidate
search or a new Result. These cases do not add Required Basis declarations to existing 1Ds.
Completion of every future
1D law and its Projection audit is not a prerequisite for V1 CRI; each new owner must supply the
exact observations its relation law requires.

## 14.1. IDL Selection and Definition Binding

Frontend resolves IDL references and produces exact Slot Binding Candidates. The binding-owning
law establishes the Slot Binding. CRI consumes the corresponding HIR or Established observation
when a later composition asks which selected Definition provides required information. It does not
redo IDL reference resolution or establish the Slot Binding. Selection formation itself remains
with Frontend and its owning law.

## 14.2. Input, Canonicalization, and Admission

ADR-0066 §4.5 orders selected Canonicalization after Input and before Admission; ADR-0048 owns the
complete inbound wiring. ADR-0065 §6.7 still describes a direct `presentedInput` Required Basis, so
the owning ADRs must reconcile the precise final-presentation relation. CRI may prepare the
compiler-time observations without deciding that open law.

For a specific Interaction, CRI uses the exact owning Inbound Composition use, not an Admission
Binding Candidate as a surrogate identity for that composition (§4.1). It obtains complete
scope-correct selection observations for the roles relevant to the owning question. Input,
Canonicalization, and Admission may each be selected or explicitly absent (§6.6). It obtains
Input's coordinate surface, Canonicalization's declared selection, and Admission's Input
observation requirement only where those roles are actually selected and the question requires
them. An observed absent role supplies no fabricated Definition or Occurrence.

When the applicable roles are selected, the law-owned inbound composition determines which
selected coordinates use Canonicalization representatives and which unselected coordinates
retain Input-owned meaning. CRI prepares these correspondences; it neither constructs runtime
Presentation values nor permits raw Input to replace a refused Canonicalization result. The
owning law must decide what to do if a required role is absent. A separately declared additional
Required Basis may entail further source candidates under §7.2.

## 14.3. Lowering and Operation Coordinates

Lowering declares its source-to-Operation coordinate relation. At compiler time CRI can prepare
its exact Input/Presentation source correspondence and Operation Parameter coordinates from the
appropriate owning projections, without searching for similar host fields. It must preserve the
V1 one-to-one mapping law of ADR-0067. The owning Lowering law determines validity and execution
meaning; compiler IR Lowering does not inherit Contract authority merely because it transforms
values.

## 14.4. Invariant and Fact

An Invariant may require an exact Established Fact. CRI can prepare the exact relation observation
for the requesting compiler-time judgment without recomputing the Fact's meaning. If the owning law
already supplies the complete direct reference, no candidate search is needed. Equal Fact payloads
do not erase distinct relevant occurrence coordinates.

## 14.5. Governance and Policy Worlds

CRI can obtain the current legal Policy World selection and its exact established Contract
bindings when a compiler-time relation use requires them. It cannot select a Policy World from
matching names, broaden a selected World, or establish Governance validity or singularity.
Governance remains open under ADR-0056; any additional information demand must follow that owner's
completed law rather than assumptions introduced by CRI.

## 14.6. Budget and Capacity

Budget and Capacity have separately established Definition meaning and explicit subject/scope
selection. CRI may prepare those compiler-time subject and Definition relations for another legal
compiler judgment using their producer-owned projections. It does not perform resource accounting,
apply runtime allowances, or obtain current occupancy observations; those are outside this ADR (§5.1).

## 14.7. State Machine

CRI may prepare the compiler-time references relating a declared State Machine to its stated
roles, transitions, and required subject context. It cannot insert an undeclared transition or
interpret the runtime's current State. Runtime transition judgment remains outside this ADR (§5.1); compiler and
semantic cycles retain the distinction of §9.2.

## 14.8. Whole-Machine Relations

ADR-0055 connects independent Cores through declared surfaces. A published result entering a
second Core is new external Input, not shared internal Basis or an extension of the first flow.

ADR-0063 §8.9 describes a broader internal Cross-Core Basis. Until the owning ADRs are reconciled,
CRI cannot use that reading to justify a Cross-Core candidate requirement. No Contract-level
waiting or joining is introduced. Whole-Machine relation preparation belongs to a separate
relation responsibility or a later explicitly approved extension; its unresolved owner laws do
not block adoption of this 1D-pipeline CRI.


---

# 15. Failure, Non-Entry, and Diagnostics

A preparation is complete only when the information requested for the exact relation use has
been legally obtained and any required correspondence or coverage calculation is supported.
Explicit Absence in a complete selection observation is a valid information answer, not a failed
lookup or CRI's ruling on declaration validity. A completed plural-candidate preparation may
report zero candidates when its requested coverage is sufficient. Zero candidates under an
incomplete observation is not a confirmed absence. Further evidence may be requested under §7.3;
otherwise the guarantee remains unfinished.

A source Producer retains ownership of its own failure. CRI cannot wrap it as a new root failure.
CRI owns unsuccessful work arising from its preparation. Cancellation or resource exhaustion cannot
produce a completed relation result. Failed preparation cannot establish Contract-permitted absence.
An unentered dependent judgment has no `Rejected` occurrence of its own.

Retry and recovery follow existing compiler ownership. Neither an earlier unsuccessful attempt
nor an unqualified retained result can stand in for current evidence (§12).

ADR-0074 remains Proposed. This ADR defines no compiler-wide failure taxonomy. Diagnostics may
observe failure evidence only through authorized projections, without disclosing restricted
domain information.