# ADR-0077: Contract Relation Infrastructure — Resolved Requirements, Source Candidates, and Coverage

## Status

Proposed

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

Kontrakt's Contract authorities can use results established by other authorities. Admission may
consume Input-related material. Invariant may require an exact Established Fact. Other requirements
need evidence about a permitted Source Domain.

ADR-0063 governs Required Basis and its Establishment. ADR-0055 defines Whole-Machine composition.
ADR-0075 governs consumption of compiler-produced material. None requires a universal relation graph.

When a relation needs candidate discovery beyond direct observation, that compiler work needs a defined
owner. Otherwise, each 1D implementation may apply different lookup rules to the same requirement.

This ADR introduces **Contract Relation Infrastructure (CRI)** as the independent compiler subsystem for
that shared preparation. Section 4 defines its responsibility.


---

# 2. Problem

A discovered source can have the right kind but the wrong occurrence. Finding one matching source
cannot rule out others in a permitted domain. Retained candidate knowledge may also become stale after a
change in domain membership.

Without a shared responsibility, each Consumer must independently check such candidate and coverage
claims. A universal relation manager would cause a different problem: it would duplicate Frontend,
Establishment, and other subsystem judgments.

The required boundary is therefore a shared compiler calculation with exact inputs and verifiable
claims. Sections 6–12 specify those guarantees.


---

# 3. Decision Drivers

Candidate discovery must preserve exact source attribution and the domain defined for the requirement.
The adequacy of an observation depends on the question being answered, not on the amount of material
physically scanned.

Compiler reuse must preserve the same guarantees across V1 and V2. An earlier result must not remain
usable merely because its previously discovered sources are unchanged.

The subsystem must operate through authorized producer observations. Its own calculations need mandatory
correctness checks and must remain independently verifiable.

Common computation must not displace existing Contract authorities or other compiler subsystems.


---

# 4. Decision and Responsibility Model

Kontrakt recognizes **Contract Relation Infrastructure (CRI)** as an independent compiler subsystem.
CRI consumes resolved relation requirements from their owners. It prepares source candidates.
It obtains domain-coverage evidence when the current question needs it.

**CRI owns the accuracy of that preparation, not the legal meaning or Establishment of a relation.**
The Required Basis retains the meaning defined by its owning law. The Binding-owning law defines
the legal Source Domain and establishes Basis Binding. Applicability and Complete Basis remain with
their respective owners under ADR-0063. Established Material keeps its original Contract authority.
Other compiler subsystems retain their own relations.

The responsibility boundary is:

```text
Owning law + producer-owned observations
    ↓ resolved requirement + authorized source observations
CRI
    ↓ source candidates + required coverage evidence
Owning judgment
    ↓ legally established relation, if any
Established Material under its original authority
```

A direct established reference need not pass through CRI. When another compiler responsibility
independently consumes a guarantee CRI produced, the Producer boundary follows ADR-0075 (§11).

## 4.1. Primary Responsibility One: Resolved Requirement Consumption

CRI takes the requirement in the meaning already resolved by its owner. The permitted observation must
identify the exact dependent use and the applicable source restrictions. Missing meaning cannot be
inferred from host names, generated artifacts, or compiler topology.

CRI may request additional authorized observations when the current evidence is insufficient (§6).

## 4.2. Primary Responsibility Two: Source Candidate Preparation

CRI prepares the candidates supported by the declared domain. An exact nominal source must not be
replaced by an equivalent-looking one. Where the requirement permits alternatives, CRI preserves the
candidates needed for the later judgment. Discovery order grants no preference.

## 4.3. Primary Responsibility Three: Required Coverage Evidence Preparation

CRI prepares enough domain-coverage evidence to support the requested question. It may use an already
sufficient observation or seek further evidence (§7.3). The request does not require a physical
full-domain scan.

## 4.4. Four Boundary Obligations

The obligations in §§9–12 govern every completed CRI result. They do not reassign common trust
qualification or independent verification to CRI.


---

# 5. Scope and Terminology

## 5.1. Contract Relations and Compiler Work Relations

A **Contract relation** carries the meaning defined by its law. IDL Slot Binding, Required-Basis
Binding, and Applicability are not interchangeable even if they refer to similar material.

A **compiler relation candidate** is a proposed source for an exact requirement.

**Coverage evidence** supports the candidate set or absence claim for one relation question. Its scope
must remain distinguishable from the question's final legal outcome.

A **compiler computation dependency** records which inputs affect a compiler calculation. It does not
define a Contract relation.

Physical representations may share storage without merging these meanings.

## 5.2. CRI Is Not the Composition Authority

**Composition** retains its meaning under ADR-0063 and ADR-0055. A composing judgment may establish new
material from earlier Established Material. Neither a prepared source set nor a valid Binding alone
creates a universal `CompositionResult`.

## 5.3. No Universal Relationship Schema

Relation laws differ in their coordinates, cardinality, and relevant context. CRI must preserve these
requirements without imposing one common semantic relation type.

It may consume results from IDL Binding, Realization Binding, or other subsystems when a separate
source-preparation request requires them. Their existing Producer Protocols remain authoritative for
those observations.


---

# 6. CRI Consumption Boundary and Authorized Observations

CRI uses the authorized producer observations needed to fulfill its request, under ADR-0075.
A source reference alone is not permission to inspect its material. The request's sufficient
observation is defined in §7.3; physical acquisition remains a Design choice.

## 6.1. Definition-Time Requirements

Before Establishment, CRI may consume resolved candidates through the HIR Candidate Protocol of
ADR-0071. HIR carries resolved candidate meaning but not Established Definition authority.

CRI cannot fill gaps by reparsing source or treating generated host artifacts as new Contract meaning.
The source observations must be authorized for the definition-time requirement.

## 6.2. Occurrence-Time Requirements

An occurrence-time request identifies one dependent application. Its Definition may be reusable,
but the exact Required Basis belongs to that Occurrence.

Only material already established by its source authority can become the source of an authoritative
Basis Binding. Earlier occurrence material cannot stand in for the current occurrence merely
because its value or compiler calculation is reusable.

## 6.3. Applicable Context and Scoped Input

Where the requirement depends on an Applicable Context, CRI uses that exact context. It cannot
substitute ambient compiler state such as a process-wide `currentWorld`.

Conversely, it must not add Version or scope coordinates that the requirement does not make relevant.
ADR-0063 assigns those determinants to their respective laws.

## 6.4. Source and Material Availability

A known source reference may still be unavailable or ineligible for the requested observation.
Availability cannot justify substituting another source unless the requirement permits it.

Ambiguous references cannot be resolved through incidental host identity or compiler storage layout.


---

# 7. Source Candidate Preparation and Legal Domains

The source domain restricts which observations can support a candidate claim. CRI must not use
additional compiler-visible sources to enlarge that domain.

A requirement may name one exact source or permit several sources under stated conditions. Sections
7.1–7.3 determine what CRI must preserve in each case.

## 7.1. Nominal Source Coordinates

If a requirement names Input Occurrence `I1`, material from `I2` cannot satisfy that exact reference
even when both presentations have equal values. The inbound-composition law owns its source definition;
its pending ADR-0065 migration is not settled here.

## 7.2. Plural Candidate Domains

When several sources are permitted, CRI maintains the candidate distinctions necessary for the
owning judgment. Discovery order does not supply arbitration. Neither structural similarity nor
compiler cost permits an unapproved reduction of the candidate domain.

## 7.3. Requirement-Relative Domain Coverage

Coverage is sufficient for the precise relation question. An exact-source requirement can be
satisfied by authorized observations of that source, without inspecting unrelated material.

An absence or singularity claim needs evidence accounting for every lawful source that could change
the conclusion. One discovered candidate is not proof of Exactly-one Applicable Basis. A producer
may supply the necessary coverage guarantee without requiring CRI to enumerate every source.
A partial projection cannot establish completeness beyond its declared scope.

When the current observations are insufficient, CRI may request further authorized evidence through
ADR-0075. It cannot report completed coverage while the necessary guarantee is unavailable.
No fixed acquisition order or two-stage algorithm is required.

Section 12 governs the continued validity of coverage after domain changes.


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
leave a different requirement unresolved. CRI reports the same observed absence without treating
those two Contract outcomes as equivalent.

## 8.4. No Implicit Recovery or Arbitration

An unsettled requirement cannot be repaired with an old Binding or a source from another World.
Compiler-result unavailability is not Contract-permitted absence (§15).


---

# 9. Establishment Integration and Authority Separation

The handoff to Establishment must preserve the exact requirement and the scope of the supporting
evidence. A prepared candidate cannot silently acquire the status of an Established relation.

An already Established relation may be consumed later under its Producer Protocol. A fresh
application with a distinct Required Basis needs its own legal judgment.

## 9.1. Handoff Non-Amplification

The handoff identifies whether each prerequisite observation is still a resolved candidate or an
Established source. It may not expose more information than the receiving requirement permits.
A compiler-derived connection cannot add determinants to the Contract law.

## 9.2. Semantic Cycles

An unresolved semantic prerequisite cycle cannot be completed through provisional candidates or
repeated calculation. A compiler computation cycle is not necessarily a semantic prerequisite cycle.
CRI must not report a compiler computation cycle as a Contract semantic cycle.

## 9.3. Authority-Specific Composition

A higher-scope judgment may establish new meaning from earlier material when its law permits that
composition. Common candidate preparation does not create a new Composition Occurrence for every
valid source connection.


---

# 10. Local Relation Correctness, Trust, and Independent Verification

CRI must validate its relation claims before delivering them. Optional independent verification
cannot replace this production obligation.

## 10.1. Local Correctness at the Production Boundary

CRI must verify that every candidate has the required attribution (§7.1) and that any coverage
claim is supported at the stated scope (§7.3). Unsupported claims cannot enter a completed
CRI observation. Section 15 governs unsuccessful preparation.

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
that check. Testing obligations appear in §18; resource policy remains with its owner (§20.6).


---

# 11. Relation-Specific Output and Legal Consumption

CRI ordinarily supplies prepared source evidence for one judgment. If another responsibility
independently consumes a guarantee derived by CRI, the result has a CRI-owned Producer boundary under
ADR-0075, even with one Consumer.

Private intermediate work needs no independent Result. Candidate and coverage claims remain distinct
in meaning but need not use different material families or physical products. Additional result
families require a separately consumable guarantee, not merely an expensive calculation.

## 11.1. Consumer Requirement Comes First

A Consumer requests an exact guarantee and receives only its lawful projection. If the needed
relation is already Established, it can be observed through the owning Producer without a duplicate
CRI result.

## 11.2. No Second Universal Mediation Layer

CRI uses the common ADR-0075 mediation boundary for authorized inputs and independent outputs.
Neither a new universal gateway nor a mandated physical call path is introduced. A direct
implementation call does not bypass the logical Producer–Consumer boundary.

## 11.3. Information Preservation and Safe Loss

An output projection may omit details only if the requested guarantee and source permissions still
hold. Exact Requirement and Source attribution must not be reconstructed from CRI-private lookup
state. The inbound-presentation obligation remains with its own composition law (§14.2).


---

# 12. Relation Validity, Reuse, and Incremental Evolution

CRI guarantees independently consumed by other responsibilities require current-use qualification
under ADR-0075. The following obligations apply to both retained and newly obtained observations.

## 12.1. V1 Guarantee

V1 reuse must agree with a clean calculation on the observations guaranteed to the same Consumer.
Definition-level reuse does not replace an occurrence-specific Required Basis (§6.2).

Compiler-derived result identity remains distinct from Contract identity. V1's physical frozen and
cache arrangements belong to Compiler Design.

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

CRI defines equality and current-use conditions for each guarantee it independently produces.
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

A wider physical calculation is allowed only through lawful observations. Its broader working
scope must not change the projection the Consumer may obtain (§6), or the evidence's current-use
conditions (§12).

## 13.3. Logical Boundaries Without Physical Mandates

Section 4 defines logical responsibilities, not separate JVM modules or passes. Physical sharing
must preserve the Producer–Consumer boundaries of §§6 and 11. Cost and storage belong to Design.

## 13.4. Resource Exhaustion Is Not Contract Refusal

Resource exhaustion may prevent CRI from completing a request. Its unsuccessful-result treatment
is specified in §15; budget enforcement is left to Compiler and Security Design.


---

# 14. Relation-Family Applicability

The following families test where source preparation is needed and where direct observations are
sufficient. These cases do not add new Required Basis declarations to existing 1Ds.

## 14.1. IDL Selection and Definition Binding

Frontend resolves IDL references and prepares Slot Binding Candidates. The binding-owning law
establishes the Slot Binding. CRI can consume that result for a separate source-preparation question,
but ordinary IDL selection needs no CRI operation.

## 14.2. Input, Canonicalization, and Admission

ADR-0066 orders selected Canonicalization after Input and before the later inbound judgment.
The exact Admission Required Basis remains subject to the ADR-0048/0065 migration.

Unselected Input coordinates retain their Input-owned meaning. Direct source connections are
sufficient unless the completed inbound law declares another candidate requirement. A raw Input
object does not substitute for a legally refused source.

## 14.3. Lowering and Operation Coordinates

Lowering declares its source-to-Operation coordinate relation. Such an exact connection can be
consumed directly. CRI becomes relevant only for an additional declared candidate question.

Compiler IR Lowering does not inherit this Contract relation merely because it transforms values.

## 14.4. Invariant and Fact

An Invariant may refer directly to its exact Established Fact. CRI is needed only if the law
requires further candidate discovery. Equal Fact payloads do not erase distinct relevant
occurrence coordinates.

## 14.5. Governance and Policy Worlds

Existing Governance or Policy World bindings can be observed directly. CRI cannot pick a Policy
World from matching names. A separate candidate question requires an explicit law and domain;
Governance remains open under ADR-0056.

## 14.6. Budget and Capacity

Budget and Capacity have their own subject declarations and runtime judgments. CRI can prepare
only a separately declared compiler-time source relation. Runtime Resource Management is outside
the current scope (§20.4).

## 14.7. State Machine

State transitions depend on lawfully established State. Runtime transition judgment does not
become a CRI request, and CRI cannot insert an undeclared transition. Compiler and semantic cycles
retain the distinction of §9.2.

## 14.8. Whole-Machine Relations

ADR-0055 connects independent Cores through declared surfaces. A published result entering a
second Core is new external Input, not shared internal Basis or an extension of the first flow.

ADR-0063 §8.9 describes a broader internal Cross-Core Basis. Until the owning ADRs are reconciled,
CRI cannot use that reading to justify a Cross-Core candidate requirement. No Contract-level
waiting or joining is introduced.


---

# 15. Failure, Non-Entry, and Diagnostics

A completed CRI preparation may report zero candidates when the requested coverage is sufficient.
Zero candidates under an incomplete observation is not a confirmed absence. Additional evidence
may be requested under §7.3; otherwise the requested guarantee remains unfinished.

A source Producer retains ownership of its own failure. CRI cannot wrap it as a new root failure.
CRI owns unsuccessful work arising from its preparation. Cancellation or resource exhaustion cannot
produce a completed relation result. Failed preparation cannot establish Contract-permitted absence.
An unentered dependent judgment has no `Rejected` occurrence of its own.

Retry and recovery follow existing compiler ownership. Neither an earlier unsuccessful attempt
nor an unqualified retained result can stand in for current evidence (§12).

ADR-0074 remains Proposed. This ADR defines no compiler-wide failure taxonomy. Diagnostics may
observe failure evidence only through authorized projections, without disclosing restricted
domain information.


---

# 16. Security and Trust Boundary

Access to a Source Domain does not authorize disclosure of its contents or absence claims to a
Consumer whose Protocol permits only a narrower projection. A partial observation cannot support
a stronger domain-coverage assertion (§7.3).

Common source trust checks follow ADR-0075; local computation checks follow §10. Untrusted
requests can exhaust resources or prevent completion, which is handled under §15. Resource
safeguards remain Security and Compiler Design responsibilities.


---

# 17. Subsystem Contract Versus Implementation Design

This ADR fixes the guarantees of an independent CRI subsystem, not its physical realization.
The relevant owning laws must be closed before Design fixes their implementation requirements.
A request may finish with its first sufficient observation or require more evidence. No fixed
two-stage algorithm, full scan, lookup structure, or mandatory pass is imposed.

Later Design must show conformance to these obligations and ADR-0075 without assigning
Contract identity to physical handles.


---

# 18. Conformance and Verification Obligations

This proposal can be accepted as a subsystem contract only if its guarantees can be tested separately from one
particular CRI implementation.

## 18.1. Correctness Cases

**Exact-source mismatch.** Two Input occurrences have equal values but different occurrence identities.
A source derived from one occurrence must not satisfy an exact requirement for the other.

**Exactly-one domain expansion.** A legal domain first contains one Applicable Source and later contains two.
CRI must not reuse outdated coverage as proof of uniqueness.

**Exact-source sufficiency.** The owning law limits the relation to one exact source. CRI may complete the
relevant source check without demanding a complete observation of unrelated material.

**Partial observation.** An initial legal projection reveals one candidate, but another legal candidate is
not yet accounted for. CRI must not report singularity without sufficient further evidence.

**Producer-guaranteed coverage.** An authorized protocol provides a complete domain observation for the
current requirement. CRI may rely on that guarantee without physically enumerating every source again.

**Completed zero-candidate observation.** A complete legal domain contains no matching sources. CRI may finish its
preparation and report the absence evidence.

**Incomplete zero-candidate observation.** A partial legal projection contains no match. CRI must not
deliver this as a complete absence claim.

**Permitted absence.** A law explicitly permits one absent source. CRI must preserve that lawful absence as distinct
from a failed lookup for a required source.

**Non-entry.** An Input or Canonicalization refusal prevents the dependent Admission judgment from entering. No
fabricated Admission `Rejected` occurrence may be produced from this absence.

**Independent Core handoff.** One Core publishes material that later enters another Core as external Input.
CRI must not infer a shared internal Basis Binding, joining, or an extension of either admitted flow.

**Unresolved Cross-Core Binding.** A hypothetical internal Cross-Core Basis is not admitted as a CRI
use case while ADR-0055 §3.4 and ADR-0063 §8.9 remain unreconciled.

**Semantic cycle.** A set of dependent judgments requires material that could only be established by the same unresolved
cycle. CRI may diagnose that cycle but cannot complete it by a provisional fixed point.

## 18.2. Reuse and Concurrency Cases

**Cached candidate after domain expansion.** A new legal source appears while an old source remains unchanged.
Previous coverage used for an Exactly-one judgment must be qualified against the changed domain.

**Changed assumption.** A retained CRI guarantee depends on an assumption that changes. It may be reused only
if its Producer establishes current compatibility.

**Worker-order variation.** Two legal computation schedules discover the same candidates in different orders. The
resulting law-owned relation must not depend on discovery order.

**Equivalent acquisition paths.** One calculation completes with an already sufficient observation.
Another obtains further authorized evidence. For the same determining inputs, both must preserve the
same relation guarantee regardless of the work performed.

**Cancelled producer.** A newer request supersedes or cancels a previous computation. Partial work must not
be exposed as a completed CRI guarantee. An older completed result still requires current-use qualification.

**Previously unsuccessful preparation.** A source observation unavailable on one attempt becomes available
later. The earlier unsuccessful attempt must not be reused as a current absence result.

**Independent reference comparison.** Derive a relation from current lawful source observations without
assuming a retained CRI result. The independently derived observation must agree with a legally reused
CRI result under the same Consumer requirements.

**Provenance-only edit.** A change in source location that leaves the producer-owned semantic projection unchanged must
not become a change in Contract identity. Separate provenance products may still require updating.

## 18.3. Security and Cross-Subsystem Cases

**Candidate authority laundering.** A CRI-derived candidate must remain non-authoritative even if cached in the same
physical store as Established Material.

**Mismatched generation.** A relation and supporting coverage evidence from incompatible generations must
not be delivered as a coherent result.

**Unauthorized observation.** A Consumer requiring a narrow relation projection must not obtain unrelated producer
material through CRI's internal access path.

**Resource stop.** Exhausting a compiler analysis budget must not be converted into `Inapplicable`, an owning Contract
Refusal, or an established negative judgment.

**Independent domain verification.** Supply CRI with an incomplete candidate view that omits another
eligible source. A Verifier using independently authorized source observations must detect an unsupported
completeness claim rather than accept CRI's claim as its only evidence.

**Verification access discipline.** The Verifier must not obtain producer-private material merely because CRI can access
it internally. If the source Producer lacks a lawful observation sufficient for the desired independent check, the
Verifier must report the assurance limit rather than invent a stronger guarantee.

**Production check without optional assurance.** CRI must not deliver unsupported attribution or coverage
evidence when an independent Verifier is not run.

**Upstream producer failure.** A source Producer fails during an otherwise valid relation request. CRI
must preserve the owning failure without turning the dependent Contract judgment into `Rejected`.

**Direct established relation.** A Consumer obtains an already established relation through its owning
Producer. The compiler must not require a duplicate CRI Result or a CRI lookup for that consumption.

These cases are obligations for later implementation and QA. This Proposed ADR does not claim that a particular compiler
currently passes them.

---

# 19. Relationship to Existing ADRs and Ownership Transfer

ADR-0063 remains Accepted and unchanged. The compiler preparation allocated to CRI here must be
compared clause by clause with ADR-0063 before any duplicated implementation wording is removed.
That comparison must preserve every normative obligation and its discoverable owner; similarly
named sections alone do not prove a lossless move.

ADR-0063 §8.8 continues to govern newly composed meaning. ADR-0055 §3.4 and ADR-0063 §8.9 still
require owner-level reconciliation on internal Cross-Core Basis. The inbound-order migration of
ADR-0048 and the Admission Required Basis in ADR-0065 also remain unresolved.

HIR Candidate observations follow ADR-0071. General legal consumption follows ADR-0075.
This ADR specializes their use for CRI without amending either Protocol.


---

# 20. Decisions and Remaining Acceptance Work

The following items record the decision status. Their rules are defined in the cited sections.

## 20.1. Relation-Family Coverage — Acceptance Audit Required

The Relation Inventory covers B+ architecture relations; it is not a CRI ownership register.
The 19-authority audit is a first pass, not proof of complete 1D coverage. Acceptance requires
checking each law's actual requirements. Open Governance, Diagnostic, inbound, and Cross-Core
decisions cannot be counted as CRI use cases (§14).

## 20.2. Compiler-Produced Result Boundary — Decided

The independent Producer boundary is decided in §11 and ADR-0075. Physical representation and
retention remain Design choices.

## 20.3. Establishment and Mediation Interface — Decided

The existing handoff and Mediation responsibilities of §§9 and 11 are sufficient. Physical APIs
and scheduling remain Design.

## 20.4. Runtime Relation Assistance — Excluded

Runtime assistance is outside this ADR (§14.6–14.7). Any future extension needs a separate
architectural decision.

## 20.5. V1 and V2 Guarantees — Decided

The V1/V2 guarantees are fixed in §12. Reuse algorithms and cache layout remain Design.

## 20.6. Security and Resource Boundaries — Assigned

Mandatory production checks are in §10. Independent assurance belongs to Verification.
Resource safeguards remain with Security and Compiler Design (§16).


---

# 21. Consequences and Proposed Adoption Boundary

CRI has a defined role from V1 without being a mandatory step for every relation. Its results
can support multiple owning laws through ADR-0075 without introducing another Contract authority.
V2 may replace the calculation strategy while preserving the same guarantees.

Final acceptance remains blocked by the incomplete 1D coverage check and the unresolved
Cross-Core and inbound owner laws (§19–20). Until then, the ADR remains **Proposed**.
