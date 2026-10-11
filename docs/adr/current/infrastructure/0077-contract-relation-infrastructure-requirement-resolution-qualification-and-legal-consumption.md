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

Kontrakt has independent Contract authorities whose established material can be required by later judgments. An
Admission judgment may require Input material and, when selected, the authoritative result of Canonicalization. An
Invariant judgment may require a Fact that was established under another authority. Other relationships arise when
Governance selects a Policy World, a State-Machine judgment consumes an established State, or a Whole Machine relates
independently operating Cores.

These relationships are not instances of one interchangeable Contract. Each owning law decides what the relationship
means, what exact source material is admissible, and when a dependent judgment may be entered. Establishment is the
boundary at which the owning authority makes an admissible result authoritative.

ADR-0063 already defines Required Basis, Basis Resolution, Basis Binding, Applicability, Complete Basis, and the
restriction that new composition meaning must be owned by a Contract law. ADR-0055 defines Whole-Machine composition
without permitting implicit joining or synchronization of otherwise independent flows. ADR-0075 defines the
compiler-wide producer-owned protocol and legal consumption boundary. None of these laws requires that every
relationship be resolved through a single physical manager or through one universal graph.

The current compiler architecture distinguishes resolved HIR, Authority-Owned Establishment, Canonical Contract World,
compiler-derived knowledge, verification, realization, product orchestration, and reuse. The relationship work that
occurs between these responsibilities must have a clear compiler-subsystem obligation. Without that boundary, each 1D
implementation could construct its own relation lookup and silently introduce different rules for source selection,
completeness, validity, or failure.

This ADR proposes **Contract Relation Infrastructure**, abbreviated **CRI**, as a logical compiler subsystem with a
contract of its own. This is a **compiler-subsystem contract**. It does not, by naming a subsystem contract, establish a
new What Contract Is one-dimensional authority or transfer any existing authority to the compiler.

The proposal is deliberately narrower than a universal Composition Manager. CRI performs relation-specific preparation
and carries explicit obligations at its handoff boundaries. It does not become the owner of every judgment, global
verification, consumer request, work schedule, cache, or runtime transition.

---

# 2. Problem

A dependent judgment cannot use an earlier result merely because that result exists. The judgment must identify its
required meaning, connect that requirement to an exact lawful source, establish that the source is applicable where the
owning law demands applicability, and satisfy the owning completeness law before entering the dependent judgment.

The compiler has useful work to perform at each boundary. It may interpret a resolved requirement, locate a candidate
within the permitted source domain, construct information needed by the owning binding law, and obtain enough evidence
to evaluate a cardinality or completeness condition. Yet none of these computations alone establishes a Contract Basis
Binding or an authoritative composition result.

The distinction is especially important when multiple authorities participate. A relation lookup may return a source
with the correct kind but the wrong occurrence. An index may find one applicable source without proving that another
applicable source does not exist. A cache may return a previously sound answer after the legal source domain has
changed. A derived graph may contain a dependency edge that was never authorized as a Contract relation.

There are also two opposite subsystem-design failures.

If CRI owns only an unqualified lookup operation, the responsibility for complete source-domain reasoning and correct
handoff is scattered across unrelated consumers. If CRI owns the entire lifecycle of Establishment, verification,
protocol mediation, scheduling, and incremental repair, it becomes a second compiler-wide control plane that duplicates
existing owners and is difficult to replace.

The problem is therefore to specify the **smallest coherent relation-processing responsibility** that can preserve exact
semantic requirements without centralizing unrelated compiler services.

---

# 3. Decision Drivers

Contract meaning must remain explicit and deterministic. A relationship is legal because its owning Contract law permits
it, not because the compiler found a convenient source or selected a particular computation path. Search order, worker
schedule, hash-table order, physical storage, and cache history must not become hidden semantic determinants.

The compiler must also preserve the distinction between a relation that has been proposed, one whose exact source
connection has been established, one that is applicable to an exact use, and a set of applicable relations that
satisfies a completeness requirement. These stages cannot be compressed into a generic successful lookup when doing so
would hide a Contract-visible distinction.

Subsystem placement must preserve replaceability. V1 may use a query-oriented compiler and frozen, HID-assisted reuse;
V2 may use a different incremental computation, dependency tracking, or repair strategy. Neither implementation may
redefine Required Basis, semantic identity, or the authority of a Contract relation.

An effective boundary should reduce duplicate resolution work without requiring every small relation request to compute
a whole-machine graph. CRI should permit the same exact legal relation to be discovered through different implementation
strategies while preserving the same qualified observation. It should also allow a sound summary to establish
source-domain completeness without mandating exhaustive traversal.

Security obligations must be specific to the boundary being protected. CRI is accountable for the correctness of
relation information it supplies. Independent validation, broader integrity assurance, trust of stored products, and
resource enforcement retain their appropriate owners.

Finally, the subsystem must serve the compiler architecture that already exists. It may not introduce a competing World
Assembly authority, a second universal producer–consumer mediator, or an additional runtime orchestrator simply to make
relation handling convenient.

---

# 4. Decision and Responsibility Model

Kontrakt will recognize **Contract Relation Infrastructure** as the logical compiler responsibility for preparing exact
cross-authority relation work under laws owned elsewhere. CRI first acts as a Consumer: it requests the relation meaning
and source observations it needs through the producer-owned protocols and Kontrakt-controlled mediation of ADR-0075.
Only knowledge that CRI derives from those authorized observations can become its own compiler-produced result.

CRI has three primary responsibilities. It interprets an already resolved relation requirement within the boundaries of
its owning law, resolves source candidates within that law's permitted domain, and prepares the evidence required for
the
owning Binding, Applicability, or completeness judgment. Section 6 specifies CRI's legal input boundary, while Sections
7 and 8 specify its relation-processing obligations.

Four interface obligations constrain this work. CRI is responsible for the correctness of the relation information it
produces. It must hand prepared evidence to the appropriate authority without conferring authority on the evidence. It
must supply any independently consumable result through its own lawful producer protocol while leaving established
relations with their original producers. It must define the current-use conditions of its derived results. Sections 9
through 12 assign these obligations without creating a second general mediation or verification system.

The three primary responsibilities and four interface obligations do not imply seven classes, processes, tables, or
independently cached compiler products. Logical separation permits physical fusion when the guarantees are preserved;
physical separation remains available when independent lifetime, failure, or scaling requirements justify its cost.

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

Independent Verification may consume both the CRI claim and independently authorized source observations. Its separate
correctness obligation is defined in Section 10. The diagram describes semantic ownership and legal consumption, not a
mandatory call order or physical materialization sequence.

## 4.1. Primary Responsibility One: Requirement Interpretation

CRI interprets the relation requirement supplied by its owning law or the relevant resolved protocol; it does not
perform a new Frontend resolution. The observation must identify the exact requirement coordinate, its owning judgment
or application where relevant, and the source-domain constraints actually declared by that law.

If those observations leave a necessary distinction unresolved, CRI cannot repair the requirement through an inferred
source name, containment relation, call topology, or default. Section 6 defines which producer observations CRI may
request to perform this work.

## 4.2. Primary Responsibility Two: Lawful Source Resolution

CRI must produce only candidate source connections admitted by the relation's owning law. It must distinguish a legal
source-domain member from an item that is merely reachable through a compiler index.

When a requirement identifies one exact source, CRI must not widen that requirement into a search for equivalent-looking
material. When a law permits several candidates, CRI may prepare several exact candidates without treating discovery
order as a preference.

## 4.3. Primary Responsibility Three: Qualification Preparation

CRI must make available the information needed by the owning laws to distinguish a resolved source connection, an
applicable relation, and a complete set of required basis relations. This includes evidence concerning the legal source
domain when the completeness law depends on the absence or presence of other applicable sources.

The qualifier's work may establish compiler-derived evidence. It must not declare a successful Applicability judgment,
Complete Basis, or new Composition meaning merely because its internal checks returned a positive answer. Those
conclusions belong to the applicable owning laws and their Establishment boundary.

## 4.4. Four Boundary Obligations

CRI's first interface obligation is local relation correctness, including exact attribution and defensible source-domain
claims under Section 10. Its Establishment handoff is governed by Section 9. Section 11 limits its producer role to the
results it actually derives, while Section 12 governs the current-use conditions of those results. Independent
verification and shared trust qualification retain the owners distinguished in Section 10.

---

# 5. Scope and Terminology

## 5.1. Contract Relations and Compiler Work Relations

A **Contract relation** has the meaning and authority assigned by its owning Contract law. An IDL Slot Binding, a Basis
Binding, an Applicability judgment, and a Whole-Machine composition are not interchangeable simply because they can be
represented as edges between references.

A **compiler relation candidate** is a compiler-prepared possibility that may be used as input to the appropriate owning
judgment. Its discovery does not establish that relationship.

**Qualification evidence** is compiler information supporting a claim about a candidate or source domain. The owning law
determines whether that evidence is sufficient for its specific judgment. Evidence storage or a successful compiler
check alone cannot confer Contract authority.

A **compiler computation dependency** records what work must be performed or revalidated for a compiler result. The
compiler may derive one from an established Contract relation, but it may not infer a Contract relation from a query,
build, analysis, or scheduling edge.

These distinctions remain meaningful even if a single physical table stores their handles side by side.

## 5.2. CRI Is Not the Composition Authority

The word **Composition** retains its Contract-owned meaning under ADR-0063 and ADR-0055. When several Established
Materials lawfully establish new meaning, a composing Contract authority owns that result.

CRI may help resolve sources and prepare the complete basis for that judgment. It does not thereby own the resulting
material. No new universal `CompositionResult` or `EstablishedRelation` authority is created by this ADR.

## 5.3. No Universal Relationship Schema

Different relation families have different semantic coordinates, cardinality, context, and occurrence behavior. This ADR
does not require them to fit one universally extensible node or edge schema.

Common compiler services may operate over typed protocol observations and family-owned projections. A family-specific
representation may remain compact and specialized if the same obligations are maintained at the boundary.

---

# 6. CRI Consumption Boundary and Authorized Observations

CRI is a requesting compiler responsibility under ADR-0075, not a privileged reader of every producer's internal
material. For each relation question it must identify the observation and guarantee required from each producer, obtain
those observations through legal mediation, and use them only for the purpose and context approved by the applicable
producer protocol. A reference or an accessible table does not itself grant the right to observe or reinterpret the
referenced meaning.

The requesting boundary must not demand more source material than the relation law needs. A producer-approved domain
summary may suffice where whole-domain knowledge is required; an exact reference may suffice where the law names one
source. The former cannot be replaced by the latter when the owning law requires completeness. Sections 7.3 and 13.2
govern the resulting correctness and scope-cost obligations.

## 6.1. Definition-Time Requirements

When CRI works before Authority-Owned Establishment, it may consume resolved semantic candidates and binding candidates
through the Resolved HIR Candidate Protocol of ADR-0071. HIR resolution provides candidate meaning and provenance under
its own law. It is not proof that a definition, relation, or occurrence has been established.

A definition-time relation requirement must be derived from exactly the observations the owning law permits. CRI must
not reopen authored source, traverse incidental HIR containment, or inspect generated host artifacts to supply omitted
meaning.

The admissible input is a typed semantic observation, not a compulsory in-memory `RelationRequest` object. This ADR
does not require an additional CRI-specific gateway or material format behind ADR-0075 mediation.

## 6.2. Occurrence-Time Requirements

When CRI assists an occurrence judgment, it must preserve the distinction between reusable Definition meaning and the
exact dependent application. A requirement law may be established once while each occurrence instantiates a fresh
Required Basis with its own exact attribution.

Only material already established by its owning authority can become the source of a Basis Binding under ADR-0063. CRI
may find candidate sources or prepare evidence before that point, but such work remains compiler work until the owning
law establishes the relevant semantic relation.

An earlier occurrence's established material does not become the current occurrence merely because its value is
equivalent or its computation is reusable.

## 6.3. Applicable Context and Scoped Input

CRI must use the precise Applicable Context when one is independently required by the owning law. It must not invent a
universal ambient context by reading a process singleton named `currentWorld`, `currentState`, or `currentPolicy`.

Conversely, CRI must not add the same Version or scope coordinate to every relation merely because that coordinate
exists somewhere in the compiler. ADR-0063 keeps context determinants with the law that makes them relevant.

## 6.4. Source and Material Availability

An authorized reference, the present availability of physical material, and its current usability are different facts. A
retained or historically established source may fail current applicability or reuse eligibility. An unavailable
materialization must not be silently replaced by the nearest currently loaded source.

No physical object identity, dense ordinal, allocation address, worker-local cache slot, or host class identity may
complete an otherwise ambiguous semantic reference.

---

# 7. Relation Resolution and Legal Source Domains

The law owning the source connection defines the permissible domain. CRI must preserve that domain throughout candidate
discovery, even when an index or query engine offers a broader set of reachable results.

A source candidate is not preferred merely because its lookup is cheaper or because it was discovered first. If a
relation is nominal, a structurally equivalent source does not satisfy it unless the owning law explicitly provides that
equivalence.

A source domain can be exact, scoped, conditionally narrowed, or plural when the owning Contract permits those forms.
CRI need not use one candidate-discovery algorithm for each form. It does need to produce observations sufficient for
the applicable source law.

## 7.1. Nominal Source Coordinates

Consider an Admission requirement whose current Composition identifies Input Occurrence `I1`. A Canonicalization
Occurrence `C2` derived from another Input Occurrence `I2` cannot be used as part of that Admission merely because `I1`
and `I2` have the same presented value.

CRI must retain the actual occurrence relation and must prevent a candidate for another dependent application from being
promoted into the current one. Whether the Admission requirement names one or several exact sources remains a decision
of Admission and the owning inbound-composition law, not this infrastructure ADR.

## 7.2. Plural Candidate Domains

When a law admits multiple candidates, CRI may resolve them concurrently or through an index. It cannot declare one
candidate authoritative without applying the owning selection and singularity law.

An allowed plural domain must not be reduced to one source merely because the implementation optimizes for a common
case. If equivalence or arbitration is part of the owning law, that law decides whether reduction is permitted and which
distinctions remain observable.

## 7.3. Completeness of the Candidate Domain

Exactly-one requires a statement about the whole legal candidate domain. A successful lookup of one applicable source is
not sufficient if another legally applicable source could also exist.

CRI must be able to provide the legal evidence needed for this claim. That evidence may come from direct source
identification, an index with a proven coverage condition, a validated summary, or a complete search. This ADR does not
demand enumeration when another sound method proves the same fact.

The completeness claim must remain sensitive to changes in domain membership. If a second applicable source appears, an
earlier exactly-one claim may cease to be usable even if the original source and its attributes remain unchanged.

This requirement must not be confused with a compiler claim that a particular search data structure has finished
traversal. The legal candidate domain belongs to the owning Contract law; the completion of a particular algorithm
belongs to the implementation.

---

# 8. Basis, Applicability, and Qualification

ADR-0063 remains the normative source for Required Basis, Basis Binding, Applicable Basis, Complete Basis, and the
distinctions between unresolved and legally absent relations. CRI does not redefine these states.

For each requirement instance CRI must preserve the owning requirement coordinate. Two requirements may demand the same
Fact kind while expressing distinct left and right operands. Their identity cannot be inferred from array position,
field offset, or discovery order.

## 8.1. Binding Is Not Applicability

An exact Basis Binding records a lawful source connection. It does not automatically make the bound source applicable to
a dependent judgment. CRI must carry enough exact information for the applicability-owning law to make that later
judgment.

A source already bound but found Inapplicable remains distinguishable from a requirement for which no binding exists.
CRI may not erase that distinction simply because both situations currently block dependent entry.

## 8.2. Applicable Basis Is Not Complete Basis

A judgment may require several independently owned Basis coordinates. One applicable source cannot satisfy an unrelated
unresolved coordinate. The law that owns the judgment specifies whether absence is legal, how many sources are required,
and whether alternatives are meaningful.

If a required source is missing or inapplicable, CRI cannot manufacture a `Rejected` result from the unentered dependent
judgment. Missing prerequisites prevent that judgment from entering. The unsuccessful result, where one exists, belongs
to its own legal boundary.

## 8.3. Qualification Does Not Become a New Authority

The subsystem may produce evidence that a candidate satisfies a syntax-independent source constraint or that an index
covered the legal domain. Its evidence may be used by the owning law when the law permits such use. That evidence is
never a generic substitute for Contract Establishment.

The infrastructure must not convert `qualification completed` into `Basis established` solely through an implementation
status flag. The final source-binding and completeness meanings remain governed by ADR-0063 and the applicable 1D or
higher-scope law.

## 8.4. No Implicit Recovery or Arbitration

The plural-domain and completeness rules in Sections 7.2 and 7.3 govern ambiguity. An earlier Binding or a
same-shaped source from another World cannot replace an unresolved relation.

Compiler unavailability or corruption is not a permitted absence under the owning law. Section 15 assigns the resulting
unsuccessful outcome to its proper boundary.

---

# 9. Establishment Integration and Authority Separation

CRI participates in the path that prepares legal inputs for Authority-Owned Establishment. It is not a replacement for
Establishment and does not establish a definition or occurrence in its own name.

A candidate produced by relation resolution must remain visibly non-authoritative until the proper owning law has
completed the necessary judgment. CRI may retain a prepared candidate or compute a summary, but those compiler products
cannot acquire authority through publication, serialization, caching, or a change of type name.

Once an owning relation is established, CRI may use its exact established reference for subsequent legal work. It must
not mutate that relation in place when another application makes a different Binding. The later law establishes a new
exact relation when new relation meaning is required.

## 9.1. Handoff Non-Amplification

The Establishment handoff must be explainable using only legal observations of resolved candidates, independently
established prerequisites, and explicit judgment inputs admitted by the owning law.

If CRI consulted an index or query graph to locate a source, the graph itself is not a new semantic determinant. Only
the source relation law's legal observations can determine the Contract result.

## 9.2. Semantic Cycles

An unresolved semantic prerequisite cycle is not completed by CRI's search procedure, a provisional Binding, or
iterative fixed-point evaluation. ADR-0063 forbids such cycles from creating Complete Basis.

The compiler may use graph algorithms to detect the cycle. A cycle in a compiler calculation graph, however, is not
automatically the same as an illegal Contract prerequisite cycle. CRI must preserve that distinction when reporting or
assisting detection.

## 9.3. Authority-Specific Composition

When a higher-scope law establishes new meaning from established inputs, the resulting material remains owned by that
higher-scope authority. When only an existing relation is used, CRI must not manufacture a higher-scope result to
simplify downstream access.

The existence of a common relation infrastructure therefore does not imply that every valid Binding generates a new
Composition Occurrence.

---

# 10. Local Relation Correctness, Trust, and Independent Verification

CRI owns the correctness of the relation information it produces from the legal observations of Section 6. It must
apply the checks necessary to avoid publishing or delivering an unsupported relation claim. This production obligation
cannot be delegated to an optional independent verifier. Sections 7 and 8 define the relation claims that CRI is
permitted to prepare; Section 9 reserves authoritative judgment to the owning Contract law.

## 10.1. Local Correctness at the Production Boundary

Before a candidate or qualification result is treated as complete compiler information, CRI must establish that its
attribution matches the actual requirement and dependent application. When CRI supplies a domain-completeness claim, it
must have a sound basis for the legal domain described in Section 7.3. A completed search of an incomplete index cannot
support that claim.

If retained evidence is insufficient or incompatible for the present question, CRI may recompute or return an explicit
compiler-owned unsuccessful outcome. It cannot arbitrate between incompatible summaries by discovery order or strengthen
a partial conclusion to make progress. Section 15 governs failure and non-entry; the representation of compiler-owned
unsuccessful results remains subject to the Proposed ADR-0074.

## 10.2. Integrity and Trust of Consumed Material

CRI uses the same consumption and trust boundaries as other compiler responsibilities. ADR-0075 Sections 13 and 19
separate current-use eligibility from integrity, provenance trust, and representation compatibility. The appropriate
storage, producer, and mediation responsibilities qualify those properties before an observation is admitted for the
relevant use. CRI must not recreate their general checks under a second relation-specific trust authority.

A trusted or intact representation does not prove that a CRI relation calculation is correct. Conversely, a logically
correct relation computed from material that was not eligible for consumption cannot be promoted into an authorized
observation. Local relation correctness remains CRI's responsibility under Section 10.1.

## 10.3. Independent Relation Verification

Independent Verification owns the assessment of CRI's correctness claims; CRI does not own the Verifier or issue its
result. The Verifier is itself a Consumer under ADR-0075 and must obtain the source-domain observations needed for its
check through the appropriate producer protocols. It must not use CRI's unverified closure assertion as the sole premise
for validating that same assertion. Where the legal source information cannot be observed independently, the claimed
assurance must be limited rather than inferred from CRI's success.

A verifier may independently recompute selected knowledge as ADR-0075 Section 20 permits, or evaluate evidence whose
coverage can be checked without accepting the conclusion under examination. Verification evidence and its result remain
compiler-owned assurance material. They do not establish a Basis Binding, Applicable Basis, or Complete Basis.

## 10.4. Assurance Boundary and Cost

Mandatory production checks under Section 10.1 must remain distinguishable from independent assurance. The Verification
and Security Designs determine whether a particular independent check runs during ordinary compilation, in a stricter
verification mode, or as part of compiler QA. This ADR does not silently make a costly full-domain recomputation
mandatory for every relation request or permit optional assurance to replace essential production correctness.

Any independently required source observation must be narrow enough to respect the original producer's access law and
sufficient to detect the error being tested. Section 18 specifies the conformance cases; Section 20.6 retains the exact
assurance profile and resource envelope as open Design decisions.

---

# 11. Relation-Specific Output and Legal Consumption

CRI is a Consumer under Section 6 before it is a Producer. Candidate discovery and qualification may remain private to
one computation. When CRI makes newly derived compiler knowledge independently consumable, it owns that result's
meaning, legal projection, and current-use conditions under ADR-0075 Sections 7 and 8. An unsuccessful operation follows
the compiler-result boundary addressed in Section 15.

Connecting a Consumer to an already established relation does not make CRI the Producer of that relation. The owning
Contract authority retains its meaning and protocol, even when CRI prepared its source connection or shares the same
physical storage. Establishment remains governed by Section 9.

No universal CRI `RelationResult` is mandated. Section 20.2 leaves the exact CRI-owned result families open until their
independent Consumer requirements justify a separate producer boundary.

## 11.1. Consumer Requirement Comes First

As Section 6 requires of CRI itself, a downstream Consumer must state the relation information or guarantee it needs.
When the requested meaning belongs to another Producer, CRI may help resolve its exact reference but cannot enlarge the
permitted observation. A Consumer requiring a chain of established relations obtains the necessary approved projections
from their respective producers; CRI does not reconstruct that chain from its private working state.

## 11.2. No Second Universal Mediation Layer

ADR-0075 already owns the general requirement for Kontrakt-controlled consumption mediation. CRI must not construct a
second mandatory gateway through which every compiler product must pass.

CRI must preserve the traceable relation handoff specified in Section 9 and, for any result it independently produces,
provide the legal producer observation described above. ADR-0075 determines the common request and delivery mechanism.

## 11.3. Information Preservation and Safe Loss

A representation may omit a relation detail only when the producer's consumption law allows the omission and no
downstream guarantee depends on reconstructing that detail. It cannot discard the exact application or source
attribution and later recover them from execution order, cache identity, or a guessed parent pointer.

For the inbound pipeline, this includes preserving the relationship that proves Admission judged the same complete
Boundary Presentation that later Lowering is legally permitted to use. This ADR does not determine Admission's exact
Required Basis shape; that remains an open Admission and inbound-composition decision.

---

# 12. Relation Validity, Reuse, and Incremental Evolution

The law owning a Contract relation determines the semantic conditions under which that relation is meaningful and
applicable. CRI-derived compiler results separately require current-use qualification. A cached lookup result is not
inherently a current Basis Binding.

Expensive candidate discovery and qualification evidence may be reused only when eligible for the current request under
ADR-0075. The trust and independent-verification questions remain separate under Sections 10.2 and 10.3.

## 12.1. V1 Boundary

V1 may use frozen material, HID-based equality evidence, indexed resolution, query computation, and L1/L2 retention
where appropriate. The chosen optimization must not appear in the semantic identity or source-domain law.

CRI must make it possible to perform a clean relation computation that does not rely on an earlier cache answer. That
reference path is needed for conformance checking, and its meaning must agree with a legally reused result.

A query result's identity does not become the identity of a Required Basis or an Established Relation. A retained
previous occurrence cannot replace a fresh current occurrence merely because the expensive definition-level work was
reusable.

## 12.2. V2 Boundary

V2 may introduce more precise change propagation, maintained indexes, conditional dependency repair, or delta updates
without changing CRI's external guarantees. No particular red-green, pull, push, or hybrid algorithm is made normative
here.

For an exactly-one claim, the current-use conditions must include sufficient evidence that changes to the **legal
candidate domain** have been accounted for. Validation of the previously selected source alone is inadequate when a new
eligible candidate could change completeness.

If a relationship depends on a previously absent source, the appearance of that source must be capable of invalidating
an older result. The compiler may realize this with negative dependencies, domain summaries, explicit source-set
versions, or another sound strategy. The implementation technique is not Contract meaning.

Conditional dependencies require similar care. A changed branch can make a previously unreachable source or helper
relevant. A reused relation summary cannot omit that new determinant merely because the earlier computation never read
it.

## 12.3. Coherent Observation

A Consumer must not receive a combination of relation output and qualification evidence taken from incompatible compiler
generations. An answer formed under one current selection cannot be paired with context or source-domain evidence from a
different selection and then presented as one complete result.

The semantic coherence and visibility constraints follow ADR-0063 and ADR-0075. A particular snapshot, generation,
transaction, or publication mechanism is Design.

## 12.4. Early Cutoff

A producer may stop change propagation when it has established the equality required by the relevant downstream
protocol. Identical physical bytes or a matching local HID are not sufficient if the source-domain closure,
applicability, or current-context assumptions necessary to the Consumer have changed.

Conversely, a provenance-only edit should not force semantic downstream recomputation when the legally required semantic
projection remains unchanged and the applicable producer has proved that fact.

---

# 13. Determinism, Scope, and Cost

The same exact legal requirement, admissible source-domain meaning, and applicable context must not lead to different
approved results merely because CRI selected another index, visited candidates in a different order, or ran work on
different workers.

Compiler-internal work traces may differ. Contract-visible selection, refusal attribution, and established reference
meaning may not vary with that scheduling unless the owning law explicitly makes another input a legal determinant.

## 13.1. Determinism Under Concurrent Resolution

The candidate and completeness conditions in Section 7 must hold regardless of iteration order or worker scheduling.
Concurrent resolution may publish partial compiler work privately, but it cannot expose a completed qualification until
the whole legal domain is covered by the evidence required for that claim.

## 13.2. Scope-Limited Work

A function-, Interaction-, Interface-, Core-, and Whole-Machine-level relation request may have very different costs.
CRI must not let a local request silently trigger repeated global analysis when the request does not own such a need.

Where an owning law requires wider knowledge, the compiler can produce an independently owned wider-scope summary and
provide the approved projection. The scope-specific summary's cost and validity remain visible to its compiler producer;
the relation semantics remain unchanged.

This proposal adopts the architectural lesson of LLVM's scoped analysis managers without requiring LLVM's manager
topology. Avoiding accidental repeated whole-machine work is a subsystem-performance obligation; the exact query or
scheduling technique is not.

## 13.3. Compact Representations and Protocol Granularity

CRI's semantic responsibilities do not mandate a query node for each Required Basis, one heap object for each Binding,
or a runtime allocation for each applicability check.

A compiler can place several relation families in shared primitive storage, reuse a compact immutable summary, or fuse
related computations if exact semantic distinctions remain observable where needed. Conversely, a physically separate
relation service is legitimate when its different lifetime, scaling, or failure boundary justifies the cost.

The allocation decision must balance additional routing and synchronization against the risk of coupling unrelated
results. ADR-0075 owns that horizontal tradeoff; this ADR supplies the relation-specific constraints.

## 13.4. Resource Exhaustion Is Not Contract Refusal

A legal relation question may be computationally expensive. An attacker or an unusually large contract world might make
candidate discovery or qualification exceed a compiler resource limit.

CRI must not convert an unfinished calculation into `Inapplicable`, `Rejected`, or a completed Contract Refusal. The
unsuccessful compiler path and its diagnostics remain distinct. Resource envelopes, algorithm choice, and admission
control for compiler work belong to Compiler Design and Security Architecture.

---

# 14. Relation-Family Applicability

CRI must be tested against several distinct relation families before its complete scope is accepted. The examples below
demonstrate required distinctions; they do not add Required Basis declarations to any owning 1D.

## 14.1. IDL Selection and Definition Binding

An IDL Slot Binding identifies which Contract Definition governs one declared machine position. Its meaning is not
inferred from the location of that Definition inside a compiler object graph.

CRI may support the resolution work needed to identify the exact semantic references in such a relation. However, an IDL
Slot Binding is not a Required-Basis Binding. The existing frontend, selection, HIR, and Establishment authorities
continue to own its exact law.

## 14.2. Input, Canonicalization, and Admission

A successful Input occurrence establishes Presentation meaning. When Canonicalization is selected, its successful
occurrence establishes the selected representatives under the exact law, while unselected coordinates preserve Input
meaning in the complete Boundary Presentation admitted by the inbound composition law.

Admission uses the final lawful Boundary Presentation, not whichever raw Input object happens to be reachable. CRI may
help establish the exact relations required to make this handoff complete. This ADR does not select between a single
conditional source requirement and multiple explicitly bound sources; that must be decided using the owning Admission
and inbound-composition laws.

A Canonicalization refusal blocks Admission entry under the current pipeline law. It cannot be bypassed by taking
successful coordinate fragments or retrieving a previous Admission result.

## 14.3. Lowering and Operation Coordinates

Lowering requires a lawful source and a declared target relation. CRI may prepare or connect the exact prerequisite
references, but Lowering owns the transformation relation and its result meaning.

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

Budget and Capacity may bind to different Subjects and scopes. CRI can assist the connection between a declared resource
contract and the exact Subject or execution boundary where its law applies.

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

It cannot invent waiting, aggregation, direct inter-pipeline communication, or implicit merging. Synchronization and
transport in a particular realization do not themselves create a new Whole-Machine Contract relation.

---

# 15. Failure, Non-Entry, and Diagnostics

Failure attribution follows the boundary that actually owns the unsuccessful condition. A malformed compiler request,
unavailable computation, corrupt retained product, unsupported implementation technique, and resource exhaustion are not
identical to Contract Refusal or to the negative result of an entered owning judgment.

Sections 8.1, 8.2, and 8.4 distinguish inapplicability, unresolved Basis, and permitted absence. A dependent judgment
that never entered must not be assigned its own `Rejected` outcome because a prerequisite was unsuccessful.

When a compiler-owned resolution or qualification operation fails, the relevant compiler result boundary owns the
failure. ADR-0074 remains Proposed, so this ADR does not presume that its complete result taxonomy has already been
ratified. No CRI-specific shortcut may reinterpret a compiler failure as an authority-owned judgment result.

Diagnostics may use exact source and relation provenance to explain why a requirement could not be satisfied. It may
also report when an otherwise valid Binding is inapplicable or when cardinality is violated. Diagnostic presentation
must not reopen raw source material to reconstruct authoritative meaning or disclose information outside its permitted
observation scope.

A useful diagnostic should identify the failed requirement and its owning application when that information is lawfully
available. It should explain the relevant domain constraint rather than only reporting that an internal lookup returned
no entries. The precise user-facing wording, source-range presentation, evidence retention, and privacy controls remain
Diagnostic and Security Design.

---

# 16. Security and Trust Boundary

CRI crosses relation domains whose producers and Contract authorities can differ. The important threats arise when a
result is attributed to the wrong application, when candidate-domain coverage is overstated, or when retained evidence
is replayed outside the context for which it was qualified. Sections 7.3, 10, and 12 define the correctness and
current-use obligations that constrain these threats.

The common trust boundary remains with ADR-0075 and the Security Architecture, as distinguished in Section 10.2.
Independent verification follows Section 10.3 rather than creating a CRI-owned verifier. A checksum, signed artifact,
or authorized physical reference cannot compensate for an incorrect source-domain conclusion.

Security review must also consider resource amplification through wide repeated scans, adversarial candidate-set
changes, or excessive evidence retention. Section 13 defines the cost and unsuccessful-computation boundaries. The
Security and Verification Designs may specify additional controls and assurance profiles without adding new Contract
meaning or dictating one CRI algorithm.

---

# 17. Subsystem Contract Versus Implementation Design

This ADR specifies what CRI must preserve across its observable boundaries. The compiler Design determines how to
satisfy those obligations.

An implementation may use a direct array lookup when a nominal requirement identifies one source. It may use an index
when many legal candidates exist. It may maintain a scope-specific summary when the completeness question otherwise
requires expensive repeated scans. These are alternative ways to realize the same law-owned requirement.

V1 implementation work must consider storage locality, batching, memory limits, cancellation, and the number of protocol
crossings. A design that represents every relation as a heap node and every lookup as an independent query is not
required. Nor does this ADR forbid a graph when the relation problem or incremental computation genuinely benefits from
that graph.

The chosen physical layout can use primitive tables, slabs, immutable generations, local handles, or other structures
allowed by the compiler architecture. Their identities and layout cannot become Contract determinants. This ADR does not
require a generic object-oriented manager with mutable maps, one global lock, or one runtime service controlling every
judgment.

A future dedicated CRI Design should detail the physical realization and show how independently owned relation families
meet this ADR's interface obligations. It must account for the costs of Section 6 consumption and Section 10 assurance
without creating parallel HIR, Establishment, Mediation, Trust, or Verification infrastructures.

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

**Clean reference comparison.** A clean relation computation and a cached or incrementally maintained computation must
agree on every producer-approved semantic observation for the same current lawful inputs.

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

**Independent domain verification.** Inject an incomplete CRI index that omits an additional lawful candidate. A
Verifier using independently authorized domain observations must detect the unsupported completeness claim rather than
accepting CRI's cached summary as its only evidence.

**Verification access discipline.** The Verifier must not obtain producer-private material merely because CRI can access
it internally. If the source Producer lacks a lawful observation sufficient for the desired independent check, the
Verifier must report the assurance limit rather than invent a stronger guarantee.

**Production check without optional assurance.** When a separate Verifier is not run, CRI still must not publish a
result whose necessary local attribution or completeness condition has failed.

These cases are obligations for later implementation and QA. This Proposed ADR does not claim that a particular compiler
currently passes them.

---

# 19. Relationship to Existing ADRs and Ownership Transfer

ADR-0063 remains the currently Accepted owner of the shared Establishment, Required Basis, Applicability, and
Composition laws. This Proposed ADR does not silently revoke or overwrite those provisions.

If this ADR is accepted, a separate, loss-checked amendment may move the **CRI-specific compiler-subsystem obligations**
and appropriate Composition-specialization wording out of ADR-0063. The generic Required Basis, Basis Binding,
Applicable Basis, Complete Basis, semantic prerequisite, identity, and Establishment laws should remain in ADR-0063
unless a distinct normative decision justifies another division.

In particular, ADR-0063 §8.8 currently provides the fundamental rule that new meaning created by composing established
sources belongs to the composing Contract authority. That rule must remain normative and discoverable during migration.
It must not be weakened to make a common Compiler Composition Engine appear authoritative.

ADR-0055 retains Whole-Machine semantics, including its prohibition on implicit waiting and merging. ADR-0048 requires
its own inbound ordering migration. ADR-0065 must still determine its exact Required Basis after Canonicalization. This
ADR does not decide those contracts in their place.

ADR-0071 continues to own the meaning and visibility of HIR Candidate observations. ADR-0075 continues to own generic
producer–consumer mediation, result-currentness, reuse, and incremental constraints. The CRI consumption requirements in
Section 6 and independently produced results in Section 11 specialize those boundaries; Section 10 separates CRI's own
correctness from existing trust and independent verification responsibilities without transferring their ownership.

The final amendment process should compare the full original text of ADR-0063 against both resulting documents. Deleting
duplicated text is acceptable only when its entire normative meaning remains explicitly owned and reachable. This
separation must not be judged complete merely because the same section headings appear elsewhere.

---

# 20. Decisions Left Open by This Proposal

## 20.1. Relation-Family Coverage

The current proposal covers the shared relationship requirements illustrated in Section 14. It does not declare one
final exhaustive list of all Relation Families. Each new 1D or higher-scope contract must still show which relations are
law-owned, whether CRI can assist their resolution, and whether another subsystem already has the relevant
responsibility.

A complete relation-family audit should precede acceptance. Missing coverage should be added where it reflects a genuine
common obligation, not through a universal schema that erases family meaning.

## 20.2. Compiler-Produced Result Boundary

The exact families of CRI-owned compiler-derived products remain to be defined in Design and validated against ADR-0075.
This ADR does not prescribe a universal `ResolutionResult`, `ClosureProof`, or `RelationHandle` semantic domain.

If CRI-derived knowledge becomes independently consumable, the owning result family must define the guarantee required
by its consumers, the permitted projection, comparison and current-use conditions, and unsuccessful behavior before
ordinary Consumer reliance is allowed. CRI must not create a second product for an already established relation simply
to make it convenient to access.

## 20.3. Establishment and Mediation Interface Shape

This ADR fixes the legal upstream consumption boundary of Section 6 and the downstream handoff of Sections 9 and 11.
It does not fix physical call direction or interface layout among CRI, the source producers, Establishment, and ADR-0075
mediation. Design must preserve their authority and coherent-observation boundaries without unnecessary protocol
crossings or intermediate materialization.

## 20.4. Runtime Relation Assistance

It remains open how much runtime relation preparation, if any, should share CRI's physical realization. Budget,
Capacity, and State-Machine laws must be reviewed before deciding whether to reuse a compact relation primitive or
introduce a separate runtime-oriented service.

This decision must not give CRI authority to establish runtime state transitions or resource outcomes.

## 20.5. V1 and V2 Calculation Strategies

Query granularity, index structures, closure-summary maintenance, domain-change tracking, physical caching, and V2
repair algorithms remain design choices. V2 may need negative dependencies or another equivalent source-domain tracking
mechanism; the normative obligation is to preserve correct current-use conditions, not to mandate one algorithm.

## 20.6. Security Assurance and Resource Envelope

The exact independent assurance profile remains to be set in Verification and Security Design. It must decide which
claims require an independent check during ordinary compilation, which may be checked in a stricter mode or compiler
QA, and which lawful source observations are sufficient for that independence. The design must also bound verification
cost, avoid repeated unqualified whole-domain scans, and specify what happens when independent evidence is unavailable.
These choices cannot weaken the production-time correctness obligation in Section 10.1 or turn an inconclusive check
into
an established Contract result. Sections 13, 16, and 18 provide the related cost and conformance requirements.

---

# 21. Research and Architecture Basis

The starting point for this ADR is the existing Kontrakt compiler architecture, not a decision to imitate one external
system. *Modern Compiler Architecture 01–15* provides the whole-system reference model: Contract semantics are separate
from compiler dependency and execution graphs; producers own their meaning; consumers use explicit protocols; compiler
work and reuse stay replaceable.

LLVM's [New Pass Manager](https://llvm.org/docs/NewPassManager.html) shows why analysis ownership and scope matter for
performance and concurrency. In particular, it restricts inner-level passes from computing mutable outer-level analyses
on demand because repeated global work can produce quadratic cost and interfere with deterministic concurrent execution.
CRI should likewise avoid a local relation request triggering uncontrolled whole-machine analysis.

MLIR's [Dialect Conversion](https://mlir.llvm.org/docs/DialectConversion/) separates the target's legality requirements
from rewrite machinery and documents the cost and complexity of rollback. CRI should distinguish law-owned relation
legality from candidate preparation, rather than making every relation search an authority-bearing speculative
transaction.

FoundationDB's [Developer Guide](https://apple.github.io/foundationdb/developer-guide.html) describes transactional
conflict ranges and the different consequences of snapshot reads. A source-domain closure claim likewise cannot be
secured by revalidating only the one source previously found when other legal candidates might have appeared. This
analogy informs compiler Design, but it does not turn a Contract Basis into a database transaction.

The Bazel [Skyframe documentation](https://bazel.build/reference/skyframe) and the broader work on incremental builds
illustrate the cost of missing dependencies and the need to track changes that alter a candidate set. The compiler may
use such mechanisms to maintain qualified relations, but a dependency edge must never become Contract authority.

LLVM's separation of analysis production from verification and the independent checking approach exemplified by
[Alive2](https://github.com/AliveToolkit/alive2) motivate the distinct correctness and assurance boundaries of Section

10. They do not justify making the CRI resolver its own independent verifier or forcing verification of each request
    through a full duplicate computation.

The research behind DBSP and maintained relational views provides candidate strategies for V2 relation maintenance.
These techniques are relevant because a stable relation projection can be computed through different paths. They do not
justify treating a delta-updated view as a newly established Contract judgment without the owning law's conditions.

These external references support architectural constraints and failure analysis. They do not replace What Contract Is,
Accepted ADRs, or the pending decisions explicitly named in this document.

---

# 22. Consequences and Proposed Adoption Boundary

The proposal makes CRI a lawful Consumer of existing producer observations and a specialized Producer only of
independently required compiler-derived relation knowledge. It gives relation processing a recognizable owner without
turning CRI into a compiler-wide mediator, verification service, or Contract authority. Established meanings remain
available from the producers that own them.

The proposal requires relation correctness at the production boundary and independent assurance paths capable of
checking its claims when appropriate. Some expensive reusable summaries may justify their own compiler result family.
The cost is justified by actual consumer guarantees and avoided repeated work, not by a universal object graph.

A decision to accept this ADR should follow a complete relation-family audit, an HIR-to-Establishment ownership review,
an ADR-0063 migration mapping, a protocol-consumer check against ADR-0075, and a security review of candidate-domain and
cross-scope attacks. Pending those checks, the subsystem boundary and its seven responsibilities remain a **Proposed
architectural direction** rather than ratified Contract law.

This ADR does not create new 1D Contract authority, settle Admission's unresolved source-Basis shape, replace
Whole-Machine composition law, or select the V2 incremental engine. Its intended contribution is narrower: **a
replaceable compiler responsibility that prepares exact relations while preserving the existing ownership of their
meaning and the legality of their consumption**.