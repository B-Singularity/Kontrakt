# ADR-0077: Contract Relation Infrastructure — Requirement Resolution, Qualification, and Legal Consumption

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

Kontrakt's Contract authorities can use results established by other authorities. Admission may depend on
established Input. If Canonicalization was selected, it may also require that authority's result. Invariant
can depend on an established Fact, while higher-scope laws can relate independent Cores.

Each relation derives its meaning from its owning Contract law. That law decides which earlier result may be
used and when the dependent judgment may begin. Establishment is where the owning law makes the result
authoritative.

ADR-0063 governs the establishment and legal use of required basis, including its applicability and
completeness. ADR-0055 governs Whole-Machine composition without allowing implicit synchronization. ADR-0075
governs legal consumption of compiler-produced material. These laws do not prescribe a central manager or a
universal relation graph.

The compiler already separates contract meaning from the work used to realize it. Relation processing also
needs a defined owner. Otherwise, each 1D implementation could develop its own source-resolution rules and
produce inconsistent judgments.

This ADR proposes **Contract Relation Infrastructure (CRI)** as the compiler subsystem responsible for that
shared work. Its obligations concern compiler processing, not a new one-dimensional Contract authority.
Existing Contract laws retain their authority.

CRI prepares the relation evidence required by the owning authorities without performing their judgments.
Sections 4 and 9–12 define the resulting subsystem boundary.

---

# 2. Problem

An earlier result may be used only when the dependent law permits the exact source and all required
conditions for entry are met. Finding a source is not enough to establish that it applies or that the
required basis is complete.

The compiler can resolve a declared requirement and prepare evidence for the owning law. That work can make
the subsequent judgment possible, but it cannot establish a Basis Binding or a composition result by itself.

Independent authorities expose several ways for a relation lookup to be wrong. It might match the source kind
but use the wrong occurrence. It might find one source without ruling out a second. A reused answer might be
stale because the lawful domain changed. A computation dependency might also be mistaken for a Contract
relation. Sections 7, 10, and 12 address these failures.

The responsibility can be drawn either too narrowly or too broadly.

A lookup-only CRI would leave each Consumer to establish the accuracy and completeness of its own
relation inputs. A CRI that assumed Contract judgments or general compiler services would instead duplicate
their owners.

CRI therefore needs one coherent responsibility: preparing legally constrained relations without taking over
unrelated compiler services.

---

# 3. Decision Drivers

The owning Contract law determines which relation is legal. CRI must produce equivalent permitted
observations for the same legal inputs, regardless of how the computation is performed.

Relation preparation must preserve the distinction between a candidate and an established source connection.
Even an established connection may be inapplicable, and applicable sources may still fail the owning law's
completeness requirement. Section 8 specifies these boundaries.

CRI's guarantees must survive changes to its implementation and reuse strategy. Neither V1 nor V2 may
change the meaning of Required Basis or the identity of an established relation. Section 12 defines the
conditions for valid reuse.

A relation request does not authorize a wider semantic observation than its owning law permits. When that
law requires knowledge of a wider domain, CRI must obtain sufficient authorized evidence. Section 13
separates this obligation from the cost of obtaining it.

CRI is accountable for the correctness of its own relation claims. The compiler's common trust services and
independent verification retain their existing ownership under Section 10.

The existing architecture is the boundary for this subsystem. CRI cannot become another World Assembly
authority, general consumer mediator, or runtime orchestrator.

---

# 4. Decision and Responsibility Model

Kontrakt will recognize **Contract Relation Infrastructure** as the compiler subsystem that prepares
relations required by laws owned elsewhere. CRI consumes authorized observations under ADR-0075. It becomes a
Producer only when it derives independently usable compiler knowledge from those observations.

CRI has three primary tasks. It reads an already resolved relation requirement, finds lawful source
candidates, and prepares the evidence needed by the owning judgment. Section 6 governs its inputs; Sections 7
and 8 govern its work.

CRI must preserve its guarantees when a prepared relation crosses into another subsystem. Sections 9–12
govern the handoff and use of its results without giving CRI the authority of their recipients.

These responsibilities define observable guarantees rather than a fixed arrangement of compiler
components. Section 17 leaves their physical realization open.

The proposed logical relationship is:

```text
Owning Contract law and producer-owned semantic observations
    ↓ legal requirement-specific consumption under ADR-0075
CRI as Consumer: requirement interpretation and source observation
    ↓ exact source resolution and qualification preparation
Prepared compiler evidence
    ├──→ owning law and Authority-Owned Establishment
    │        ↓ established Contract relations under original producer protocols
    └──→ CRI-owned derived result, only when independently needed
             ↓ producer protocol and ADR-0075 mediation
          authorized compiler consumers
```

An independent Verifier may consume CRI's claims alongside observations obtained from the original producers.
Section 10 defines that distinction. The diagram specifies legal information flow, not a required call
sequence.

## 4.1. Primary Responsibility One: Requirement Interpretation

CRI consumes the resolved relation requirement through its owning producer's permitted protocol. That
observation must identify the exact dependent judgment and the restrictions governing its eligible sources.
CRI does not reinterpret the source language.

If the permitted observations do not determine the requirement, CRI must not guess. It cannot infer a missing
source from naming conventions or incidental compiler structure. Section 6 governs any further observations
it may request.

## 4.2. Primary Responsibility Two: Lawful Source Resolution

CRI may report a source candidate only when the owning law permits that source to be considered for the
specified relation. Reachability by compiler machinery alone does not establish eligibility.

When a requirement identifies one exact source, CRI must not widen that requirement into a search for equivalent-looking
material. When a law permits several candidates, CRI may prepare several exact candidates without treating discovery
order as a preference.

## 4.3. Primary Responsibility Three: Qualification Preparation

CRI prepares the basis on which an owning law can decide whether a source connection is applicable and
whether the whole required basis is complete. Where completeness depends on other potential sources, CRI must
provide evidence about the legal domain rather than just the source already found.

A successful compiler qualification is not an authoritative Contract judgment. Only the owning law may
establish Applicability, Complete Basis, or composition meaning through the appropriate Establishment
boundary.

## 4.4. Four Boundary Obligations

Sections 9–12 define CRI's four boundary obligations. Shared trust qualification and independent verification
retain their existing owners under Section 10.

---

# 5. Scope and Terminology

## 5.1. Contract Relations and Compiler Work Relations

A **Contract relation** has the meaning assigned by its owning law. An IDL Slot Binding cannot substitute for
a Required-Basis Binding. Likewise, Applicability and Whole-Machine composition are different judgments even
if a compiler stores similar references for them.

A **compiler relation candidate** is a compiler-prepared possibility that may be used as input to the appropriate owning
judgment. Its discovery does not establish that relationship.

**Qualification evidence** is compiler information supporting a claim about a candidate or source domain. The owning law
determines whether that evidence is sufficient for its specific judgment. Evidence storage or a successful compiler
check alone cannot confer Contract authority.

A **compiler computation dependency** records which work a result depends on. That dependency may be informed
by an established Contract relation, but a compiler graph edge cannot create the Contract relation that it
describes.

Physical representation cannot erase these semantic distinctions.

## 5.2. CRI Is Not the Composition Authority

The word **Composition** retains its Contract-owned meaning under ADR-0063 and ADR-0055. When several Established
Materials lawfully establish new meaning, a composing Contract authority owns that result.

CRI may help resolve sources and prepare the complete basis for that judgment. It does not thereby own the resulting
material. No new universal `CompositionResult` or `EstablishedRelation` authority is created by this ADR.

## 5.3. No Universal Relationship Schema

The owning law determines the relation's source coordinates, cardinality, and applicable context. CRI
must preserve those differences rather than imposing one universal relation meaning. Section 14 tests the
shared obligations across existing relation families.

The producer protocols under ADR-0075 remain the basis for any common observation; this ADR does not
require one representation for every relation.

---

# 6. CRI Consumption Boundary and Authorized Observations

CRI consumes only observations authorized for its relation requirement under ADR-0075. A reference does
not grant authority to reinterpret the original producer's material.

The permitted observations must be sufficient for the claim CRI is preparing. An exact-source requirement
needs evidence for that source; a domain-completeness claim also needs evidence for the relevant domain.
Section 7.3 governs completeness. Physical request size and retrieval strategy remain Design choices
under Section 17.

## 6.1. Definition-Time Requirements

Before Establishment, CRI may consume resolved candidates through the HIR Candidate Protocol of ADR-0071. HIR
establishes what a candidate means, not that the candidate has already become an established definition or
relation.

A definition-time requirement must come from the observations allowed by its owning law. CRI may not fill
gaps by reading source again or treating generated host artifacts as new meaning. Incidental HIR layout is
not a substitute for the resolved protocol.

CRI relies on producer-approved semantic observations. This ADR imposes no additional mandatory
consumption boundary beyond ADR-0075.

## 6.2. Occurrence-Time Requirements

An occurrence-time request is about one exact dependent application. Its definition may be reusable, but a
fresh occurrence establishes its own required basis. CRI must not confuse that new basis with the
definition-level work reused to prepare it.

Only material already established by its owning authority can become the source of a Basis Binding under ADR-0063. CRI
may find candidate sources or prepare evidence before that point, but such work remains compiler work until the owning
law establishes the relevant semantic relation.

An earlier occurrence's established material does not become the current occurrence merely because its value is
equivalent or its computation is reusable.

## 6.3. Applicable Context and Scoped Input

When the owning law requires an Applicable Context, CRI must use that precise context. It may not infer one
from ambient compiler state such as a process-wide `currentWorld` value.

Conversely, CRI must not add the same Version or scope coordinate to every relation merely because that coordinate
exists somewhere in the compiler. ADR-0063 keeps context determinants with the law that makes them relevant.

## 6.4. Source and Material Availability

Knowing a source reference does not mean that its material is available or legally usable for the
current judgment. An unavailable source cannot be replaced by a different available source unless the
owning law permits that substitution.

An ambiguous semantic reference cannot be completed by an incidental compiler or host identity. Section 7
governs lawful source selection.

---

# 7. Relation Resolution and Legal Source Domains

The law owning the source connection defines the permissible domain. CRI must use only evidence that
supports membership in that domain, even when the compiler can observe additional sources.

A source candidate is not preferred by the order or cost of discovery. A nominal relation cannot be
satisfied by structural equivalence unless the owning law expressly allows it.

The owning law may name one source, allow several candidates, or narrow eligibility by context. CRI must
honor that defined domain. Section 17 leaves source-discovery algorithms open.

## 7.1. Nominal Source Coordinates

Consider an Admission requirement whose current Composition identifies Input Occurrence `I1`. A Canonicalization
Occurrence `C2` derived from another Input Occurrence `I2` cannot be used as part of that Admission merely because `I1`
and `I2` have the same presented value.

CRI must retain the actual occurrence relation and must prevent a candidate for another dependent application from being
promoted into the current one. Whether the Admission requirement names one or several exact sources remains a decision
of Admission and the owning inbound-composition law, not this infrastructure ADR.

## 7.2. Plural Candidate Domains

When a law admits multiple candidates, CRI prepares candidates within the declared source domain and
the evidence required by that law. It does not determine their Applicability or exercise the law's
selection or singularity judgment; those judgments remain with the owning authority.

CRI must preserve plurality until the owning law permits a particular reduction. Any law-owned equivalence
or arbitration is applied through that authority's judgment, not inferred from the compiler's choice of
candidate.

## 7.3. Completeness of the Candidate Domain

An exactly-one judgment needs evidence sufficient to exclude another applicable source in the legal
domain. Finding one candidate is not enough.

CRI must supply sound evidence covering the domain relevant to the owning law's question. The law retains
authority over which candidates are Applicable and whether Complete Basis is established under ADR-0063.
CRI's domain-coverage claim is evidence for that judgment, not the judgment itself.

Evidence of coverage must remain valid when domain membership changes. If a second eligible source appears,
the earlier evidence may no longer support an exactly-one judgment even though the original source is
unchanged. Section 12 governs the current use of such evidence.

---

# 8. Basis, Applicability, and Qualification

ADR-0063 defines Required Basis and governs when a bound source is applicable and the required basis is
complete. CRI prepares evidence for those judgments; it does not redefine their legal outcomes. The same ADR
also distinguishes a legally permitted absence from an unresolved requirement.

Two requirements for the same Fact kind can still be distinct operands of one judgment. CRI must preserve
the declared identity of each requirement rather than deriving it from processing order.

## 8.1. Binding Is Not Applicability

A lawful Basis Binding identifies the source, but does not by itself establish that the source is applicable.
CRI must provide the exact information the applicability-owning law needs for that judgment.

A source already bound but found Inapplicable remains distinguishable from a requirement for which no binding exists.
CRI may not erase that distinction simply because both situations currently block dependent entry.

## 8.2. Applicable Basis Is Not Complete Basis

A judgment may require multiple independently owned Basis coordinates. One applicable source cannot satisfy
another unresolved coordinate. The owning law determines the cardinality of each requirement. It also decides
when absence is permitted and whether alternatives have legal meaning.

If a required source is missing or inapplicable, the dependent judgment cannot enter. CRI must not report
`Rejected` as though that judgment had been performed. Section 15 assigns unsuccessful outcomes to their
proper owner.

## 8.3. Qualification Does Not Become a New Authority

CRI may provide evidence of a candidate's permitted source characteristics or of the legal domain's
coverage. The owning law decides whether that evidence is sufficient for its judgment, as Sections 7.3 and
9 require. Completion of CRI's work does not establish Basis Binding or Complete Basis.

## 8.4. No Implicit Recovery or Arbitration

CRI cannot resolve an unsettled requirement by substituting an older Binding or a source from another
World. An unavailable or invalid compiler result is not an absence permitted by Contract law; Section 15
specifies the failure boundary.

---

# 9. Establishment Integration and Authority Separation

CRI supplies evidence to Authority-Owned Establishment without becoming the authority that issues the
judgment. Its relation candidates remain non-authoritative until the owning law establishes their meaning.
The continued availability of compiler evidence does not change its status.

Once an owning relation is established, CRI may use its exact established reference where the owning
law permits. A later application requiring a different Binding cannot redefine the earlier relation;
its owning law must establish the new relation.

## 9.1. Handoff Non-Amplification

The handoff must contain only the observations permitted by the owning law. CRI must preserve whether the
material is still a resolved candidate or has already been established as a prerequisite.

Compiler-derived connections cannot add a determinant to the owning Contract law.

## 9.2. Semantic Cycles

An unresolved semantic prerequisite cycle cannot establish Complete Basis under ADR-0063. CRI must not
present a provisional or repeatedly calculated result as a lawful completion of that cycle.

A compiler computation cycle is not automatically an illegal semantic prerequisite cycle. CRI must preserve
the distinction when reporting or assisting detection.

## 9.3. Authority-Specific Composition

A higher-scope authority owns any new meaning it establishes from earlier material. CRI may prepare the
source relations for that law, but it does not create a composition result when no composing judgment exists.

The existence of a common relation infrastructure therefore does not imply that every valid Binding generates a new
Composition Occurrence.

---

# 10. Local Relation Correctness, Trust, and Independent Verification

CRI owns the correctness of the relation information it produces from the legal observations of Section 6. It must
apply the checks necessary to avoid publishing or delivering an unsupported relation claim. This production obligation
cannot be delegated to an optional independent verifier. Sections 7 and 8 define the relation claims that CRI is
permitted to prepare; Section 9 reserves authoritative judgment to the owning Contract law.

## 10.1. Local Correctness at the Production Boundary

Before a CRI result can be consumed, CRI must establish that its relation claims are supported by the
observations they use. Exact attribution follows Section 7.1, and domain-coverage evidence follows
Section 7.3.

If the available evidence cannot support the current question, CRI cannot report a completed qualification.
It may obtain further lawful evidence or return an unsuccessful compiler result under Section 15.
Conflicting or partial evidence cannot be treated as complete.

## 10.2. Integrity and Trust of Consumed Material

CRI consumes material under the common trust conditions of ADR-0075 §§13 and 19. The original producer and
the applicable trust or mediation boundary determine whether that material is intact, compatible, and
eligible for the requested use. CRI does not repeat those general checks as a new trust authority.

An intact source does not make CRI's calculation correct. Nor can an accurate calculation authorize
consumption of a source that failed the original producer's conditions. Section 10.1 governs CRI's local
correctness independently of those conditions.

## 10.3. Independent Relation Verification

An independent Verifier checks CRI's claims without becoming part of their Contract authority. It must obtain
any required source observations through the original producers' legal protocols under ADR-0075. A closure
claim cannot be independently verified by trusting that same unverified claim. If the Verifier cannot obtain
adequate independent observations, it must limit its assurance.

ADR-0075 §20 permits independent checking based on the original producers' lawful observations. The
Verifier may accept evidence only when its adequacy can be checked without assuming the claim under test.
Its assurance is compiler-owned and cannot establish a Contract Basis relation.

## 10.4. Assurance Boundary and Cost

Section 10.1 defines the condition CRI must satisfy before a result is consumed. Independent assurance
is a separate claim governed by the Verification Architecture. Its scope must be explicit, and absence of
that assurance cannot excuse a CRI correctness failure. Section 20.6 leaves assurance policy open.

Independent checking must still respect each source producer's access rules. The verifier needs enough
information to detect the targeted error, not unrestricted access to the producer's material. Section 18
defines test cases; Section 20.6 keeps the precise assurance profile open.

---

# 11. Relation-Specific Output and Legal Consumption

CRI's consumption obligations are defined in Section 6. When relation preparation produces knowledge that
another compiler subsystem must consume independently, CRI becomes the owner of that derived result under
ADR-0075. Private intermediate work does not require a new producer protocol. Section 20.2 leaves the
necessary result families open.

CRI does not acquire ownership of an established relation by helping a Consumer locate it. The original
Contract authority retains that relation and its protocol under Section 9.

This ADR does not require a universal CRI `RelationResult`. A separate result family must be justified by an
actual Consumer need under Section 20.2.

## 11.1. Consumer Requirement Comes First

A downstream Consumer must identify the relation guarantee it needs, just as CRI does under Section 6. CRI
can help locate an established relation but cannot expand its legal projection. When a Consumer needs several
established relations, it consumes the permitted observations from their owners instead of relying on CRI's
private search state.

## 11.2. No Second Universal Mediation Layer

ADR-0075 already governs legal consumption across the compiler. CRI must use that mediation rather than
introduce another mandatory gateway.

CRI provides the relation-specific handoff required by Section 9. If it owns an independently consumed
result, that result must be exposed through its producer protocol under ADR-0075.

## 11.3. Information Preservation and Safe Loss

CRI may expose a narrower projection only when the originating producer permits it and no Consumer
requirement loses a legally significant distinction. Exact source attribution cannot be reconstructed from
incidental computation state.

In the inbound pipeline, Admission and later Lowering must refer to the same legally completed Boundary
Presentation. The exact Admission Required Basis remains an open decision for its owning law and inbound
composition; this ADR preserves the relation without choosing its shape.

---

# 12. Relation Validity, Reuse, and Incremental Evolution

The owning Contract law determines when an established relation applies. Any CRI-owned derived result must
also be qualified for the current Consumer request under ADR-0075. Reuse does not create a current Basis
Binding.

CRI may reuse earlier resolution work only under ADR-0075's current-use conditions. Sections 10.2 and 10.3
separately govern trust in consumed material and optional independent verification.

## 12.1. V1 Guarantee

V1 must preserve the legal result when computation is reused. A clean computation from the same lawful
observations and one relying on retained compiler knowledge must agree on the semantic observations
required by their Consumers. Section 18 states the corresponding conformance case.

Compiler-derived result identity does not become the identity of Required Basis or an Established Relation.
The ability to reuse definition-level work cannot substitute an earlier Occurrence for a fresh one. The
physical V1 reuse arrangements remain governed by Compiler Design.

## 12.2. V2 Guarantee

V2 must preserve the same legal observations under incremental reuse. Any retained result claiming
exactly-one must remain sensitive to changes in the legal candidate domain. Checking the formerly selected
source alone is insufficient when another eligible source can change the judgment.

A result dependent on a source's absence is no longer justified if that source becomes eligible. Similarly,
a change to the conditions governing source eligibility can introduce a determinant not used by the
previous computation. Current-use qualification must account for both cases. The incremental algorithm
remains a Design decision.

## 12.3. Coherent Observation

A Consumer must not receive a combination of relation output and qualification evidence taken from incompatible compiler
generations. An answer formed under one current selection cannot be paired with context or source-domain evidence from a
different selection and then presented as one complete result.

ADR-0063 and ADR-0075 govern coherent visibility. Section 17 leaves its physical realization open.

## 12.4. Equality and Current Use

A previously produced relation result may be reused only when its producer establishes the equality
required by the Consumer's protocol and its current-use conditions still hold. An unchanged physical
representation is not sufficient if a legal determinant has changed.

A provenance-only change does not alter Contract identity when the owning semantic law leaves the
semantic projection unchanged. Provenance Consumers may nevertheless require updated observations.

---

# 13. Determinism, Scope, and Cost

For the same legally determining inputs, CRI must provide equivalent permitted observations regardless of
its computation path. Only the owning law can determine Contract-visible selection or refusal attribution.

## 13.1. Determinism Under Concurrent Resolution

Concurrency cannot change the candidate or completeness guarantees in Section 7. A Consumer cannot observe
a completed qualification until the evidence required for its claim is complete.

## 13.2. Lawful Scope of Observation

The required relation determines which source domain CRI must cover; it does not give CRI permission to
consume unrelated Contract meaning. When evidence from a wider scope is necessary, CRI may consume it only
through the responsible Producer's authorized projection under Section 6.

Whether a wider result is calculated once, reused, or recomputed is a cost decision for Compiler Design.
The legality and completeness conditions remain those of Sections 7 and 12.

## 13.3. Logical Boundaries Without Physical Mandates

The responsibilities in Section 4 need not correspond to separate physical components. Their
producer–consumer boundaries must nevertheless preserve the legal observations defined in Sections 6
and 11. Compiler Design determines the cost and physical realization under ADR-0075.

## 13.4. Resource Exhaustion Is Not Contract Refusal

A relation request may remain unresolved when compiler resources are exhausted. CRI must represent that
condition through the compiler-owned unsuccessful-result boundary. It cannot turn an unfinished computation
into `Inapplicable` or a Contract Refusal without the owning judgment. Resource limits and their enforcement
belong to later Compiler and Security Design.

---

# 14. Relation-Family Applicability

CRI must be tested against several distinct relation families before its complete scope is accepted. The examples below
demonstrate required distinctions; they do not add Required Basis declarations to any owning 1D.

## 14.1. IDL Selection and Definition Binding

An IDL Slot Binding identifies which Contract Definition governs one declared machine position. Its
meaning comes from the owning IDL law, not the compiler's representation.

CRI may assist exact IDL reference resolution, but it does not establish an IDL Slot Binding. That binding
follows its own Frontend and Establishment laws; it is not a Required-Basis Binding.

## 14.2. Input, Canonicalization, and Admission

A successful Input occurrence establishes Presentation meaning. When Canonicalization is selected, its successful
occurrence establishes the selected representatives under the exact law, while unselected coordinates preserve Input
meaning in the complete Boundary Presentation admitted by the inbound composition law.

Admission uses the final lawful Boundary Presentation, not whichever raw Input object happens to be reachable. CRI may
prepare the evidence required for the owning law to establish that handoff. This ADR does not select between a single
conditional source requirement and multiple explicitly bound sources; that must be decided using the owning Admission
and inbound-composition laws.

A Canonicalization refusal blocks Admission entry under the current pipeline law. It cannot be bypassed by taking
successful coordinate fragments or retrieving a previous Admission result.

## 14.3. Lowering and Operation Coordinates

Lowering owns the legal transformation from its declared source to the specified Operation coordinate. CRI
may prepare the needed source connection, but it does not perform the Lowering judgment.

A Compiler IR Lowering Pass must not be treated as the same kind of Contract relation merely because it also moves
information between representations.

## 14.4. Invariant and Fact

An Invariant law may need the exact Established Fact material on which the judgment applies. CRI can resolve the source
relation required by that law, but it cannot decide Fact Identity or declare an Invariant satisfied.

If two Fact occurrences have the same payload but different law-owned occurrence coordinates, CRI must retain whichever
distinctions the Invariant requirement legally uses.

## 14.5. Governance and Policy Worlds

Governance has its own selection, scope, and binding responsibilities. CRI must not select the current Policy World on
Governance's behalf because it found one matching name or stored an earlier World binding.

Where Governance supplies an established relation that another authority needs, CRI may assist lawful delivery of that
relation. Any unresolved Governance-specific meaning remains with ADR-0056 and its later ratification; this proposed ADR
cannot settle it by implication.

## 14.6. Budget and Capacity

Budget and Capacity may govern different execution subjects or scopes. CRI can prepare the exact connection
declared for a resource contract, but it cannot decide the runtime resource outcome.

The current elapsed time, concurrent occupancy, and resource-stop outcome may depend on runtime observation. A
compile-time relation plan cannot establish those runtime judgments in advance. CRI must not become a Resource Manager
merely because it helped bind those contracts to execution subjects.

## 14.7. State Machine

A State-Machine transition can require a particular established State and a lawful transition relation. CRI can prepare
the exact references needed by the State-Machine law, but it cannot insert an undeclared transition or treat another
flow's current state as the subject's own state.

Section 9.2 governs the distinct treatment of semantic prerequisite cycles and compiler computation cycles.

## 14.8. Whole-Machine Relations

Whole-Machine Composition can relate explicit Contract surfaces across Cores under ADR-0055. CRI may resolve the legal
cross-unit connections and provide their exact prerequisites.

CRI cannot infer a Whole-Machine obligation from the way independent flows happen to communicate. Any
required wait or join must be expressly governed by its owning law under ADR-0055.

---

# 15. Failure, Non-Entry, and Diagnostics

An unsuccessful outcome belongs to the boundary where it arose. A compiler request that could not be
processed must not be reported as a Contract Refusal. Section 8 distinguishes missing prerequisites from a
negative Contract judgment.

A judgment that never entered cannot be assigned its own `Rejected` result merely because its prerequisite
failed. Section 8 governs missing or inapplicable basis.

Compiler-owned resolution failure follows the compiler-result boundary. ADR-0074 is still Proposed, so CRI
must not assume its result taxonomy is final. In particular, compiler failure cannot be translated into the
unsuccessful judgment of a Contract authority.

Diagnostics may explain why a source requirement could not be met using legally observable provenance. They
must not reconstruct authoritative meaning from raw source or reveal details that the Consumer is not
permitted to observe.

A diagnostic should identify the requirement that could not be satisfied and explain the relevant
source-domain rule when such information may lawfully be disclosed. The Diagnostic and Security Designs own
presentation and retention, including appropriate limits on sensitive material.

---

# 16. Security and Trust Boundary

CRI must defend against false relation claims. A result may name the wrong application or claim complete
coverage of an incomplete domain. Cached evidence can also be applied outside its valid context. Sections
7.3, 10, and 12 define the relevant correctness obligations.

ADR-0075 and the Security Architecture govern common trust checks. CRI's local correctness and its
independent verification boundary are specified in Section 10. Intact or authenticated data cannot repair an
unsound source-domain conclusion.

Untrusted inputs may make relation preparation expensive or inconclusive. Section 13.4 requires such
failures to remain compiler-owned. Security and Verification Design will determine resource safeguards
without changing Contract meaning.

---

# 17. Subsystem Contract Versus Implementation Design

This ADR specifies CRI's observable obligations. It does not prescribe a relation-solving algorithm,
physical representation, execution schedule, or reuse mechanism. Such decisions belong to later Compiler
Design after the relevant Contract laws are closed.

For example, an exact-source requirement and a plural-domain requirement may use different retrieval
strategies. Both must preserve the owning law's source domain and produce evidence sufficient for the
claim made under Section 7. This example does not select either strategy.

The later CRI Design must demonstrate that its chosen realization satisfies this ADR and the general
producer–consumer obligations of ADR-0075. A physical handle cannot acquire Contract identity or
judgment authority.

---

# 18. Conformance and Verification Obligations

This proposal can be accepted as a subsystem contract only if its guarantees can be tested separately from one
particular CRI implementation.

## 18.1. Correctness Cases

**Exact-source mismatch.** Two Input occurrences have equal values but different occurrence identities. A
Canonicalization result established from one occurrence must not be used as the other occurrence's Admission source
unless the owning law explicitly permits that relation.

**Exactly-one violation.** A legal source domain first contains one applicable source and later contains two. The later
qualification must not preserve the earlier complete-Basis claim solely because the original source remains unchanged.

**Permitted absence.** A law explicitly permits one absent source. CRI must preserve that lawful absence as distinct
from a failed lookup for a required source.

**Non-entry.** An Input or Canonicalization refusal prevents the dependent Admission judgment from entering. No
fabricated Admission `Rejected` occurrence may be produced from this absence.

**Higher-scope binding.** A Whole-Machine law binds a legal source across Core boundaries. CRI must not infer the same
relation from transport, call, or physical linking without that law.

**Semantic cycle.** A set of dependent judgments requires material that could only be established by the same unresolved
cycle. CRI may diagnose that cycle but cannot complete it by a provisional fixed point.

## 18.2. Reuse and Concurrency Cases

**Cached candidate after domain expansion.** A new legal source appears while an old source remains unchanged. A cached
exactly-one qualification must be revalidated against the changed source domain or rejected as currently ineligible.

**Changed assumption.** A qualification product is retained after its owning semantic profile or compiler assumption
changes. The product may be reused only if its producer establishes current compatibility for the new request.

**Worker-order variation.** Two legal computation schedules discover the same candidates in different orders. The
resulting law-owned relation must not depend on discovery order.

**Cancelled producer.** A newer request supersedes or cancels a previous computation. A late result from the cancelled
request must not be accepted as complete current evidence without the current-use checks.

**Independent reference comparison.** Derive a relation from current lawful source observations without
assuming a retained CRI result. The independently derived observation must agree with a legally reused
CRI result under the same Consumer requirements.

**Provenance-only edit.** A change in source location that leaves the producer-owned semantic projection unchanged must
not become a change in Contract identity. Separate provenance products may still require updating.

## 18.3. Security and Cross-Subsystem Cases

**Candidate authority laundering.** A CRI-derived candidate must remain non-authoritative even if cached in the same
physical store as Established Material.

**Mismatched generation.** A relation and its supporting qualification evidence taken from incompatible generations must
not be published to a Consumer as a coherent result.

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

**Production check without optional assurance.** When a separate Verifier is not run, CRI still must not publish a
result whose necessary local attribution or completeness condition has failed.

These cases are obligations for later implementation and QA. This Proposed ADR does not claim that a particular compiler
currently passes them.

---

# 19. Relationship to Existing ADRs and Ownership Transfer

ADR-0063 retains the Accepted laws of Establishment and required basis. Acceptance of this ADR would not
revoke them.

After ADR-0077 is accepted, a separately reviewed amendment may move CRI-specific compiler obligations out of
ADR-0063. ADR-0063 must still own the general Required Basis law and the meaning of its applicability and
completeness. Its semantic prerequisite and identity laws remain there as well, together with Establishment,
unless an explicit normative decision changes their ownership. Any moved composition-specific rule requires a
lossless audit.

ADR-0063 §8.8 must continue to state that newly composed meaning belongs to the composing Contract authority.
That law cannot be weakened merely to give CRI a convenient common execution model.

ADR-0055 retains Whole-Machine composition law. ADR-0048 must be migrated under its own inbound-ordering
decision, and ADR-0065 must determine Admission's exact Required Basis after Canonicalization. CRI cannot
close these decisions for their owners.

HIR candidate meaning remains with ADR-0071, and general legal consumption remains with ADR-0075. Sections 6
and 11 specialize those boundaries for CRI. Section 10 states how CRI's local correctness differs from the
compiler's existing trust and independent verification obligations.

The amendment must compare the original ADR-0063 with both resulting documents. Repeated text may be removed
only when every normative requirement still has an explicit owner and a discoverable location. Matching
section titles are not proof of a lossless move.

---

# 20. Decisions Left Open by This Proposal

## 20.1. Relation-Family Coverage

Section 14 tests this proposal against representative relation families but does not claim exhaustive
coverage. Before acceptance, every affected 1D or higher-scope law must be examined to determine whether it
needs CRI and which owner already governs its relation.

A coverage audit must add genuinely shared obligations that are missing. It must not force different relation
laws into one generic schema simply to simplify the inventory.

## 20.2. Compiler-Produced Result Boundary

Which CRI results deserve independent Producer status remains open. Their definitions must follow ADR-0075
and actual Consumer needs. This ADR does not prescribe universal `ResolutionResult`, `ClosureProof`, or
`RelationHandle` semantics.

An independently consumed CRI result needs a defined guarantee and a legal projection. Its Producer must also
define when the result can be reused and how an unsuccessful computation is represented. CRI cannot create a
duplicate product for a relation already established by another authority.

## 20.3. Establishment and Mediation Interface Shape

Sections 6, 9, and 11 define the lawful information boundaries. Any additional semantic observation needed
by an owning authority or Consumer remains subject to its Producer's protocol. The physical interface
shape is deferred to Design.

## 20.4. Runtime Relation Assistance

It remains open whether CRI's obligations extend to any runtime relation preparation. That question must
be reviewed against the laws governing Budget, Capacity, and State Machine. No such extension can transfer
a runtime judgment to CRI. Any shared physical mechanism is a later Design choice.

## 20.5. V1 and V2 Realization

Section 12 requires current-use correctness across V1 and V2, including changes to formerly absent
candidates. Calculation granularity, evidence retention, and incremental repair mechanisms are deferred
to Compiler Design. None may weaken the guarantees of this ADR.

## 20.6. Security Assurance and Resource Envelope

The independent assurance profile remains open. Each claimed profile must state which CRI properties it
checks and which authorized source observations support its independence. It must not claim success when
adequate evidence is unavailable. Sections 10 and 18 define the correctness and conformance obligations;
resource limits and verification scheduling belong to later Design.

---

# 21. Consequences and Proposed Adoption Boundary

CRI becomes the compiler owner of relation preparation without becoming the authority that establishes the
relation. It consumes producer-approved observations and publishes only derived knowledge that another
Consumer independently needs. Sections 4, 6, and 11 define that responsibility.

Local correctness is required whenever CRI produces a result. Independent verification may add assurance
without replacing those mandatory checks. Separately owned summaries are justified when they meet a real
Consumer requirement and reduce repeated work.

Acceptance requires the relation-family audit in Section 20.1 and the lossless ownership review in Section

19. The consumption and security reviews follow Sections 6 and 16. Until these checks are complete, the seven
    responsibilities remain **Proposed** rather than ratified Contract law.

The proposal does not decide Admission's remaining basis question or change Whole-Machine composition law. It
also leaves the V2 incremental algorithm open. Its contribution is a replaceable compiler subsystem that
prepares exact relations under the laws that own them.