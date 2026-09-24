# ADR-0075: Compiler-Produced Material and Knowledge, Protocol-Mediated Consumption, Validity, Reuse, and Incremental Boundaries

## Status

Accepted

## Date

2026-09-23

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- ADR-0046: IDL-First Interface Contract Frontend and Generated Host Boundary
- ADR-0047: One-Dimensional Contract Presentations and Pipeline-Slot Selection
- ADR-0053: Version Contract
- ADR-0056: Governance Contract
- ADR-0057: Failure Contract
- ADR-0058: Publication Contract
- ADR-0063: Contract Establishment, Occurrence, Applicability, and Semantic Dependency
- ADR-0064: Input Contract
- ADR-0065: Admission Contract
- ADR-0066: Canonicalization Contract
- ADR-0067: Lowering Contract
- ADR-0068: Fact Contract
- ADR-0069: Invariant Contract
- ADR-0071: Resolved Contract HIR Semantic Boundary, Deterministic Visibility, Lifecycle, and Reuse
- ADR-0073: JVM Platform, External Technology, and Realization Boundary
- ADR-0074: Compiler Result, Explicit Unsuccessful Result, Recovery, and Observation Boundary
- `kontrakt-compiler-total-architecture-map-design-draft.md`
- `kontrakt-compiler-reuse-incremental-v1-v2-todo.md`
- `kontrakt-v2-incremental-architecture-research-todo.md`
- `adr-0075-pre-taxonomy-research-inventory-produced-knowledge-reuse-validity-incremental-survey.md`
- *Modern Compiler Architecture 01-15*

---

## 1. Context

Kontrakt does not stop producing useful information after source resolution or Establishment. Later compiler work forms
new representations and derives knowledge that other responsibilities may need. Some of that work is cheap and local.
Other results are expensive enough to share between consumers or retain for a later compilation.

This problem appeared before the current compiler architecture was complete. In the earlier runtime-oriented
implementation, repeated reads of classes and attributes were already being replaced by L1/L2 reuse of information that
Kontrakt had interpreted once and could use again. The same question reappeared when Definition and Occurrence were
separated. A retry can require a fresh semantic Occurrence while still allowing definition-level or compiler-derived
knowledge from an earlier computation to remain useful. That showed that semantic freshness and computational reuse are
related but are not the same decision.

HIR and Establishment then made the distinction more explicit. Their semantic and observation laws remain with
ADR-0071, ADR-0063, and the applicable authority-specific ADRs. ADR-0075 relies on those boundaries and starts where
compiler-produced material or knowledge is consumed across responsibility boundaries.

The same family of questions will recur at lower compiler levels without making those levels identical. MIR may maintain
CFG and SSA while also producing data-flow knowledge. LIR and the JVM backend add target-dependent material. Downstream
responsibilities can read overlapping upstream information and still require different validity rules. A result that
remains sufficient for verification, for example, can already be stale for backend generation.

The pre-taxonomy survey found related problems well beyond compiler caches. The evidence ranges from LLVM analysis
preservation and rustc incremental queries to ThinLTO summaries, persistent build artifacts, database snapshots, and OS
lifetime mechanisms. These systems all reuse previously produced information, but they do not solve one identical
problem. Treating them as instances of one universal cache or product rule would hide distinctions that matter for
correctness.

ADR-0075 therefore does not begin by declaring one generic reuse mechanism. It defines the horizontal compiler
architecture in which produced material and knowledge cross responsibility boundaries through explicit protocol
mediation, remain valid or become stale, be reused or recomputed, and later participate in incremental execution. Common
law is introduced only where the same obligation actually survives across those different cases.

---

## 2. Problem

The compiler needs to avoid two opposite failures.

The first is repeated reconstruction. Several later responsibilities may need the same upstream knowledge. If each one
reopens source, reacquires the same realization body, or rebuilds the same analysis, the compiler repeats expensive work
and can even produce divergent interpretations of one compiler state.

The second is unsound reuse. Once information has been retained, the fact that it still exists says nothing by itself
about whether it is valid for the current computation. A body may have changed while an old summary remains in memory. A
transformation may leave retained analysis material available even though one of its assumptions is no longer true. A
persisted artifact may still decode after the producer schema or target capability has changed. An earlier dependency
trace may no longer describe the reads performed by a later execution.

The cross-responsibility problem is not a direct producer-consumer call relation. A compiler responsibility states the
information or guarantee it requires. Kontrakt-controlled mediation resolves that requirement against the legal protocol
surface of the responsibility that can provide it, obtains or forms the required material, performs the checks owned by
the relevant compiler layer, and exposes only an approved observation to the requester.

```text
requesting responsibility
    ↓ states required information or guarantee
Kontrakt-controlled mediation
    ↓ resolves and qualifies
producer-owned protocol surface
    ↓ provides legal material or knowledge
approved observation
```

This direction is important for change containment. A producer can change its internal computation, representation, or
local dependency structure without forcing that change across the responsibility boundary when the protocol-visible
meaning required by downstream work remains unchanged. Comparison, fingerprinting, recomputation, or another compiler
mechanism can help establish that fact, but those mechanisms do not become the protocol meaning.

The same requirement does not imply that the compiler must avoid dependency graphs. Some problems are naturally graphs,
and a deep graph can be the right realization when its semantic or computational benefit justifies its cost. The
architecture problem arises when chains of calculated results become the default compiler-wide topology even though the
same relationship could be expressed through a shallower, explicit protocol boundary or sufficient summary.

Other cases have a different shape. A transformation can mutate one IR unit in place and invalidate only some analyses.
A whole-machine responsibility can depend on a compact summary while the backend depends on the full body. Under
ADR-0063 and the applicable 1D ADR, a fresh runtime Occurrence can coexist with reuse of definition-level compiler
knowledge without reusing the old Occurrence. An independent checker can deliberately recompute information that another
subsystem would normally share because sharing would weaken its value as a correctness oracle.

The architecture therefore has to preserve the boundary between what a producer guarantees, what the requesting
responsibility is allowed to obtain, which Kontrakt-controlled systems mediate that transfer, what changes can alter the
protocol-visible meaning, what retained representation can still be trusted, and what work must be repeated when those
conditions no longer hold.

The difficulty is that these questions occur in many compiler domains with different semantics. A rule that is too weak
permits stale knowledge or hidden coupling to cross subsystem boundaries. A rule that is too strong can force unrelated
responsibilities into one lifetime, comparison, dependency, invalidation, or management model and can destroy the
scaling and replacement freedom that the boundary was intended to provide.

---

## 3. Scope

ADR-0075 applies when compiler-produced material or knowledge leaves the private computation that formed it and another
compiler responsibility can rely on it independently. The same boundary can support immediate consumption, later reuse,
or retention across a wider compiler lifetime. Persistence extends that lifetime across sessions without changing the
ownership rule.

The scope covers derived knowledge that later compiler work consumes. Analyses and summaries are common examples. It
also covers realization-facing results such as verification material or backend output. Retained representations enter
scope when they are used for later reuse. These boundaries can occur around HIR, MIR, LIR, or backend work, while the
meaning and invariants of those families remain with their owning ADRs.

Cross-responsibility consumption is mediated by Kontrakt-controlled compiler systems rather than by direct management
between the requesting and providing responsibilities. This ADR defines that mediation contract and the principles by
which mediation responsibilities may later be distributed across compiler layers. It does not prescribe one manager,
one query engine, or one physical subsystem topology.

The scope also includes cases in which a subsystem does not use the common query or cache machinery internally. A JVM
backend, external adapter, or local pass pipeline can remain independently implemented while still participating in the
same cross-responsibility protocol and validity architecture at its boundary.

Private scratch values are outside this ADR unless they become independently observable or reusable outside the
computation that owns them. The same is true for worker-local temporary state whose lifetime ends with one operation and
whose contents cannot affect a later requester after that operation completes.

Semantic and representation-specific laws remain with their owning ADRs. ADR-0075 refers to ADR-0071, ADR-0063,
the applicable authority-specific ADRs, and future MIR, LIR, or backend ADRs where those laws are needed instead of
restating them here.

The ADR also stops above concrete reuse and orchestration machinery. Cache and query architecture remain replaceable, as
do dependency storage, fingerprinting, persistence, incremental repair, routing, and physical protocol dispatch. A later
compiler architecture or Design document can allocate those responsibilities to concrete systems once their workload,
coherence, lifetime, concurrency, and failure characteristics are known.

---

## 4. Existing Ownership Boundaries

This ADR relies on existing owners rather than copying their laws. Resolved Contract HIR remains under ADR-0071;
Establishment and Established observation remain under ADR-0063 and the applicable authority-specific ADRs;
compiler-side
unsuccessful-result handling remains under ADR-0074. Future MIR, LIR, analysis, optimization, and backend ADRs likewise
own the meaning and invariants of the result families they introduce.

Those owners retain their family-specific laws for identity, semantic or result equality, completeness, applicability,
Definition or Occurrence freshness, representation invariants, and other conditions that determine what their material
means. ADR-0075 does not generalize those laws into one compiler-wide identity, equality, validity, or dependency
schema.
When later sections require one of those judgments, they refer back to the owning law through this section.

ADR-0075 begins only at the horizontal boundary created when owner-defined material or compiler-derived knowledge is
requested, retained, qualified for current use, or consumed by another compiler responsibility. It defines the common
cross-responsibility obligations that survive family-specific specialization. Concrete reuse, storage, dependency,
comparison, and repair mechanisms remain Design under Section 23.

---

## 5. Architecture Decision

Kontrakt will not use one universal `CompilerProduct` semantic model for every reusable result. Compiler-produced
material and knowledge differ too much in scope, lifetime, change sensitivity, failure consequence, and operational
characteristics for a single product schema or one invalidation rule to remain sound.

Independently consumed compiler material is exposed through a producer-owned protocol boundary. The producer family owns
the meaning of the guarantee available at that boundary and the legal information that can be provided through it. A
requesting responsibility states the information or guarantee it needs; it does not directly manage the producer,
retained material, comparison machinery, or formation lifecycle.

Cross-responsibility consumption passes through Kontrakt-controlled mediation. The mediation can resolve a request to an
appropriate protocol surface, obtain or form required material, apply the qualification and validation required by the
relevant layer, perform comparison or fingerprint-based machinery where permitted, and provide the approved observation
to the requester. These are logical mediation responsibilities. ADR-0075 does not require them to live in one manager or
even in one compiler layer.

Subsystem allocation is constrained first by Contract authority and determinism. Physical placement, fusion, caching,
retention, scheduling, or reuse must not transfer, merge, or create Contract authority, widen a legal observation
surface, or introduce an undeclared determinant. For the same applicable legal basis, different execution schedules,
cache outcomes, retention paths, or physical realizations must not change the approved meaning exposed through the
protocol boundary.

Correctness constraints define the legal allocation space inside that constitutional boundary. Responsibilities whose
independent progress can form an invalid requester-visible combination share a coherence obligation. Failure and trust
boundaries must likewise prevent unqualified, corrupt, incompatible, or insufficiently trusted material from entering
ordinary successful consumption merely because it is physically reachable or retained. These obligations can require an
explicit coordination or qualification mechanism, but they do not by themselves require one physical subsystem.

Within the legal allocation space, physical placement is chosen by the costs and benefits of lifetime and retention,
execution and traffic, replacement and evolution, and resource and scaling characteristics. Responsibilities can be
placed close together when separation would require disproportionate synchronization, duplicated state, routing,
qualification, copying, or hot-path crossings. They can remain separate when independent lifetime, replacement, scaling,
resource use, concurrency, or failure containment justifies the coordination cost.

Logical separation therefore does not require physical separation, and physical fusion does not merge ownership. Two
logical responsibilities can share one in-process structure, execution region, or storage layout for efficiency while
their legal protocol relation remains explicit. Conversely, one logical relation can use distinct control and material
paths when their traffic or resource characteristics justify different physical treatment.

The opposite extremes are both avoided. Excessively fine subsystem boundaries can turn useful separation into repeated
routing, state duplication, synchronization, qualification, and boundary-crossing cost. Excessively broad systems can
couple unrelated change, retention, replacement, scaling, resource, and failure domains. No single allocation criterion
dominates all others: constitutional and correctness constraints determine what is legal, while the remaining criteria
select among legal allocations.

Protocol meaning is also a change-containment boundary. If internal computation, representation, or local dependency
structure changes while the protocol-visible meaning required by other responsibilities remains the same, that internal
change does not by itself propagate across the boundary. The mechanism used to establish sameness remains compiler
machinery and can differ by result family and compiler layer.

Kontrakt likewise does not define the compiler as one mandatory fine-grained graph of calculated results. Deep graphs
are legal when the problem itself or the computational benefit justifies their depth and scope, provided that their
dependency relation remains explicit enough for the owning system to determine the legal basis and preserve
deterministic
observation. Otherwise, cross-responsibility architecture should prefer explicit and comparatively shallow protocol
relations or sufficient summaries, while graph machinery remains local to the responsibilities that benefit from it.
Graph depth is therefore a costed architectural choice, not a default consequence of chaining compiler results.

The overall relation is:

```text
requesting responsibility
    ↓ declares required information or guarantee
Kontrakt-controlled mediation
    ↓ resolves, obtains, qualifies, and delivers as required
producer-owned protocol boundary
    ↓ exposes legal material or knowledge
approved observation
```

A later reuse or incremental path can avoid work behind this relation, but it cannot transfer semantic ownership to the
mediation machinery, introduce hidden basis through retained or ambient state, or require the requester to understand
the
producer's internal realization.

---

## 6. Working Classification Boundary

The research inventory deliberately collected more cases than this ADR should own. The cases are now separated by where
their law belongs rather than by forcing them into one universal runtime taxonomy.

Family-specific meaning and validity remain with the owners identified by Section 4. Compiler-wide obligations that are
necessary whenever independently consumed material crosses a responsibility boundary remain in this ADR. Concrete cache,
dependency, comparison, retention, preservation, repair, and incremental mechanisms remain Design under Section 23.

This separation does not require a normative enum of reuse families. Direct material reuse, evidence reuse, analysis
preservation, maintained updates, summaries, plans, specialized products, backend artifacts, incremental repair, and
deliberate recomputation can remain distinct Design strategies while conforming to the same cross-responsibility laws.
Their different lifetimes, change sources, and failure consequences remain relevant to implementation and measurement,
but they do not justify a second semantic model.

---

## 7. Produced-Material Boundary

A cross-responsibility boundary exists when one compiler responsibility makes completed material or knowledge available
through a legal observation surface and another responsibility can request that information without entering the
producer's private construction state. Section 5 governs the mediated request path; this section defines only when the
produced material has become an independently consumable boundary.

The boundary uses the subject or reference law supplied by the result's owner. It does not create a new compiler-wide
identity system, and it requires only enough stable reference information for mediation to resolve the requested
material
without treating incidental storage as the meaning of that result.

One completed observation can span several structures, and several logical results can share backing storage. Physical
fusion does not merge ownership, while physical separation does not create a new semantic distinction. A result that
remains private to one computation is outside this boundary unless it later becomes independently observable or retained
for another responsibility.

---

## 8. Legal Consumption and Derived Knowledge

Legal consumption follows the mediated relation defined by Section 5. A requester states the information or guarantee it
needs and receives only an approved observation; it does not select a producer implementation, reopen producer-private
state, or reconstruct a stronger upstream result from storage topology, retained representation, or generated artifacts.

When a responsibility combines approved observations and computes new information, that new knowledge belongs to the
responsibility that produced it. A summary, proof result, optimization decision, or other derived product therefore does
not inherit the authority or ownership of the material from which it was derived.

The compiler can intentionally introduce a shared derived producer when several responsibilities need the same expensive
knowledge. The producer must be selected by architecture rather than accidental execution order. The fact that one
requester happened to compute equivalent information first is not sufficient reason to make that requester the owner for
unrelated consumers. A producer likewise need not encode requester identity when one legal observation is intentionally
usable by several consumers.

---

## 9. Change, Validity, and Preservation

Produced knowledge can remain usable after change only when the guarantee required by its later requester is still
valid. Section 13 governs qualification for current reuse, while Section 11 governs whether a re-established unchanged
observation contains change at a protocol boundary.

A transformation can preserve an earlier result, update it as part of the transformation, invalidate it for later
recomputation, or make repair possible. The exact strategy and the result-specific preservation law belong to the owning
IR, analysis, or product family under Section 4. A preservation claim is a correctness claim for the exact guarantee
that later requesters are allowed to obtain, not a performance hint that may be wrong without consequence.

The same rule applies when a logical subject is replaced or deleted. Retained analysis material, a dense handle, or a
table slot that survives the change does not prove that the old result still belongs to the current subject. Physical
survival can support implementation bookkeeping, but it cannot substitute for the current subject relation required by
the owning result law.

---

## 10. Structural Knowledge and IR Transformation

CFG and SSA are clear examples of why validity cannot be reduced to cache lookup. Related knowledge such as dominance or
memory state can be maintained alongside the IR or derived on demand. A transform can preserve one guarantee while
invalidating another.

CFG, SSA, and related structural invariants remain with their owning IR and analysis work. ADR-0075 requires only that
later consumers do not observe a structure after a relevant mutation unless its owner has preserved, updated, repaired,
or re-established the guarantee they rely on.

The rule also applies across IR levels. Derived knowledge from one representation does not automatically remain valid
after lowering merely because the lower representation was produced from the higher one. A later stage can explicitly
carry, translate, summarize, or discard earlier knowledge. Reuse across that transition needs the corresponding
producer-owned relation instead of an assumption that ancestry alone preserves validity.

In-place mutation and immutable replacement are both legal realization strategies. A validity protocol must work for
both. Kontrakt therefore does not make immutable generations a prerequisite for every optimizer or force every
transformation to allocate a new complete IR.

---

## 11. Dependency Topology and Change Containment

ADR-0075 does not require one compiler-wide dependency graph. A compiler responsibility can use a deep or fine-grained
graph when the problem itself is graph-shaped or when the computational benefit justifies the bookkeeping, scheduling,
state, and coordination cost. Such graph machinery should remain local to the responsibility or compiler system that
benefits from it unless a wider graph is itself justified.

Across responsibility boundaries, dependencies should be expressed at the narrowest sufficient legal protocol boundary.
A requester that needs one summary or one protocol-visible guarantee should not become dependent on an entire upstream
representation merely because that representation was convenient to access. This reduces change amplification and keeps
provider implementation changes from becoming requester changes when the required protocol meaning is stable.

This is a preference for shallow and explicit cross-responsibility topology, not a prohibition on depth. A deep graph is
legal when it provides real semantic or computational value that a shallower relation would lose or reproduce at greater
cost. Depth that appears only because calculated results repeatedly depend on other calculated results is not sufficient
justification by itself.

Protocol boundaries act as change-containment cuts. When internal changes re-establish the same protocol-visible
meaning,
those changes stop at that boundary for responsibilities that depend only on that meaning. A sufficient summary can
serve
the same architectural purpose when carrying the complete upstream material would create an unnecessarily wide
relationship.

Local dependency capture still has to be sound for the guarantee it supports. A previous dynamic read set can be useful
machinery without becoming permanent architecture, and a complete-set result can require a closure condition that is not
visible from previously present members alone. The owning subsystem can use dynamic tracking, explicit dependency
records, delta maintenance, or another mechanism appropriate to its problem.

The physical graph representation, graph engine, and invalidation algorithm remain Design or narrower compiler
architecture work. Compiler dependency machinery does not redefine Contract, HIR, IR, program, artifact, or provenance
relations owned elsewhere.

---

## 12. Scope, Context, and Assumptions

Reusable compiler knowledge is not automatically valid outside the scope in which it was produced. A function-local
analysis, an SCC summary, a whole-machine result, and a target-specific backend plan answer different questions even
when they inspect overlapping material.

A consumer request does not grant permission to widen the producer's scope implicitly. If a local consumer needs a wider
result, that wider analysis must be a separately owned computation with its own cost, validity, and dependency boundary.
This prevents convenience calls from turning local work into repeated whole-machine computation.

Context sensitivity follows the same rule. A helper summary can be reusable across several Operations when the summary
is intentionally parameter-relative or context-independent. A context-specialized result may require a different
current-validity basis. The owner of the result family defines that distinction rather than leaving it to the cache key
chosen by an implementation.

Compiler assumptions can also participate in validity. Target capabilities, a closed-world realization assumption, a
selected profile, or a backend feature can affect a compiler result without becoming Contract meaning. When such an
assumption is allowed to affect the result, it must be visible to the producer's validity law. Ambient state cannot
silently play that role.

---

## 13. Result Comparison and Current Reuse Qualification

ADR-0075 does not define Contract, HIR, Established, IR, or artifact equality. Section 4 keeps those family-specific
meaning and equality laws with their owners. An independently consumed compiler-derived result family likewise owns the
legal observation it exposes and any result-specific comparison or preservation relation needed to state whether that
observation changed.

Current reuse eligibility is not the producer's semantic law alone. Kontrakt-controlled mediation qualifies an earlier
result for the current request by applying the owner-defined observation and result conditions together with the
compiler-owned conditions relevant to that reuse path. Depending on the result family, those additional conditions can
include producer or schema compatibility, target or configuration compatibility, retained evidence, coherent observation
under Section 15, and the integrity or trust judgments governed by Section 19.

These conditions remain distinct even when one implementation stores them in one metadata structure. Retained presence,
decodability, semantic or result currentness, integrity, trust, and final reuse eligibility answer different questions.
No universal metadata tuple or one compiler-wide `valid` predicate is introduced by this ADR.

A fingerprint, content hash, HID, byte comparison, generation marker, or physical-reference comparison can accelerate
qualification when the owning result law permits it. Such machinery is evidence or routing information; it does not
become semantic equality, result meaning, or current validity merely because it is cheap to compare. Exact key
composition, comparison acceleration, validation order, and fallback strategy remain Design under Section 23.

---

## 14. Derived Summary, Plan, and Specialized-Product Reuse

Producer-defined projections remain governed by the result family that exposes them. A separately derived summary or
plan is different: when downstream work can request it independently and rely on its guarantee, it is a compiler-derived
result with its own declared basis, legal observation, and producer ownership under Sections 7 and 8.

A derived result need not share the invalidation boundary of all material from which it was formed. Wider upstream
material can change while a summary or plan remains reusable when the basis that actually determines that derived result
is current under Section 13. This is the same change-containment principle defined by Section 11, applied to an explicit
derived boundary rather than to the producer's full internal material.

Planning, application, specialization, and emitted material can be separate reusable units. An optimization plan can
remain current while local transformation or backend material is formed again. A specialized product can be reused only
while the assumptions and realization context that justified that specialization remain current; the exact context is
owned by that product family rather than by a universal cache key.

A useful summary remains complete for its declared question without becoming a miniature copy of every upstream detail.
Summary schemas, plan representations, specialization keys, aggregation algorithms, indexes, and physical storage remain
Design under Section 23.

---

## 15. Completion, Visibility, and Coherent Observation

An ordinary requester must not receive a successful result before the producer has completed the guarantee associated
with that result boundary. Internal partial state can exist while a producer is working, but it remains private unless a
separate protocol explicitly gives that partial state meaning.

A request that needs several related observations must receive a coherent set permitted by their producers. Combining an
old observation from one view with a new observation from another can create a compiler state that no completed producer
ever exposed. A matching numeric generation alone is not proof of coherence if the underlying results have different
validity domains.

Responsibilities whose independently changing state can form an invalid requester-visible combination share a coherence
obligation. This is a correctness constraint on system allocation under Section 5. It does not command one physical
subsystem; a design can satisfy the obligation either by close placement or by an explicit mechanism that preserves a
legal coherent observation across separated systems.

The requester does not reconcile that coherence for itself. Kontrakt-controlled mediation must provide an observation
that satisfies the required coherence boundary regardless of whether the implementation uses snapshot-style replacement,
in-place mutation, reader pinning, versioned state, or another safe mechanism. A physical split is legal only if the
coordination mechanism prevents the split from making an invalid combination observable.

A failed or cancelled replacement must not expose a partially completed successful result. Whether an older completed
result can still be used is decided by current qualification under Section 13, not by the fact that a newer attempt was
started.

Retention and reclamation are separate from coherent current observation. Keeping old backing storage alive for an
existing reader does not make it current for new requests, while reclaiming unused backing does not retroactively change
a result that was validly consumed earlier. Section 16 owns the retention distinctions.

---

## 16. Retention, Cache, and Persistence

L1, L2, persisted state, content-addressed storage, and future remote caches are mechanisms for making earlier compiler
work available again. They do not define the reusable result family or decide current reuse eligibility; Section 13 owns
that qualification boundary.

The earlier L1/L2 direction remains useful because it can retain information Kontrakt already interpreted instead of
forcing later requesters to reread raw inputs. The reusable unit is nevertheless defined by the result boundary rather
than by the cache container in which its representation happens to be stored.

Currentness, legal visibility, physical retention, future reusability, reclaimability, and cross-session persistence are
distinct questions. They do not need one universal lifecycle enum. Material can cease to be current while an existing
reader can still observe it, remain physically retained while no new request may use it, or be reusable within one
compiler lifetime while being unsuitable for persistence across sessions.

Retained result material and retained reuse evidence can also have different lifetimes. Compact fingerprints, dependency
evidence, or validated summaries can remain useful after a payload is discarded, but evidence alone does not become a
consumable current result. Conversely, retaining the payload does not remove the Section 13 qualification requirement.

Different retention lifetimes are therefore a system-allocation pressure under Section 5. A short-lived result must not
acquire a longer persistence, compatibility, or recovery obligation merely because it shares a physical subsystem with
longer-lived material. The opposite extreme is also unnecessary: logical lifetime distinctions do not require a separate
physical system when that would only duplicate state and coordination.

Persistent, external, or remote material can require compatibility, integrity, and trust checks beyond ordinary
in-process retention. Section 19 owns those distinctions. Section 22 owns the clean deterministic fallback when retained
state is absent, and Section 23 owns hashing, cache topology, serialization, admission, eviction, storage layout, and
other concrete retention machinery.

---

## 17. Reuse, Early Cutoff, and Incremental Evolution

ADR-0075 does not define one normative reuse lifecycle. A result family can use direct material reuse, retained
evidence,
preservation, maintained update, recomputation with early cutoff, summary or plan reuse, specialized or backend product
reuse, incremental repair, or deliberate recomputation when those strategies are legal and profitable. These are Design
families, not compiler-wide semantic states.

Direct reuse and early cutoff are different work-avoidance decisions. Direct reuse qualifies earlier material for the
current request without forming the same result again, while early cutoff can follow validation, recomputation,
maintained
update, or repair after the relevant current observation has been re-established. Downstream work can stop propagating a
change only when the owner-defined legal observation on which it depends is unchanged; an upstream edit, cache hit,
matching fingerprint, or unchanged physical representation is not sufficient by itself.

Incremental repair forms a current result from previous state and change information; it does not make the previous
result
current by identity. The repaired result must satisfy the same owner-defined legal observation required from any other
legal path. Section 22 defines the deterministic clean-path equivalence and fallback that constrain every such strategy.

Dependency tracking and propagation mechanisms remain governed by the topology law in Section 11 and the Design boundary
in Section 23. A local system can use red-green validation, reverse change frontiers, delta maintenance, SCC-local
repair,
or another algorithm without forcing that graph or algorithm on unrelated result families.

V1 is not required to persist every result or implement the final V2 incremental engine. It must preserve explicit
result
boundaries, current qualification, deterministic clean formation, and enough replaceable dependency and measurement
seams
for V2 to choose stronger product-local reuse or repair strategies without changing producer or requester meaning.

---

## 18. Backend, External Subsystems, and Target-Specific Products

A subsystem does not leave this architecture merely because it is implemented outside a common query engine. The JVM
backend, JDK Classfile facilities, generated-artifact code, or another external adapter can use their own local
implementation while exposing a result boundary that states what upstream observations and target conditions determine
the product.

Backend or artifact reuse eligibility is not the same question as upstream semantic equality. Target baseline, classfile
version, backend capability, compiler or schema compatibility, debug mode, optimization configuration, or profile
information can invalidate an artifact even when the upstream Contract or compiler-semantic observation remains
unchanged. Those artifact-specific conditions belong to the backend product family and participate in current
qualification through Section 13.

Cross-stage and cross-tool reuse therefore needs an explicit boundary. Earlier high-level knowledge can be translated or
summarized for a backend, but ancestry does not make every earlier analysis valid after target lowering. An opaque tool
output can be retained only under the compatibility and validity conditions of the adapter that owns that result.

Profile and cost information can guide profitability or product selection without becoming a proof that a transformation
is legal. Where a product depends on such information, that dependence must be explicit enough that profile change can
invalidate the affected product without becoming a hidden determinant for unrelated semantic judgments.

---

## 19. Failure, Cancellation, Integrity, and Trust

ADR-0074 owns the representation of unsuccessful compiler results. ADR-0075 defines only the boundary conditions
required
by reusable material and cross-responsibility consumption.

A failed or cancelled producer must not leave ordinary consumers with a partially formed successful result. Recovery
cannot turn incomplete construction state into a completed result merely to preserve reuse continuity. Whether a
previous
completed result remains usable is a Section 13 qualification question and does not follow from the newer attempt's
failure alone.

Persistent, external, or remotely obtained material can cross a different failure and trust boundary from ordinary
in-process results. It must pass an explicit qualification boundary before participating in successful compiler
consumption. Physical reachability, successful decoding, cache presence, or a matching storage key is not sufficient.

Integrity, authenticity or provenance trust, representation compatibility, and semantic or result currentness are
distinct judgments. One physical stage can perform several checks, but their meanings must remain distinguishable so
that
Section 13 can compose them without creating one generic `valid` authority.

Failure and trust containment remain system-allocation constraints under Section 5. Reuse qualification also does not
prove that the original producer was correct: a dependency-perfect result can still have been produced by a buggy
analysis or transformation. Verification, translation validation, differential testing, and other correctness mechanisms
therefore remain distinct from reuse qualification.

---

## 20. Independent Validation and Deliberate Non-Reuse

Reuse is an optimization opportunity, not an obligation imposed on every consumer. Some consumers exist partly to
provide independence from the path that produced the result being checked.

A reference judgment, critical verifier, or transformation checker can intentionally recompute selected knowledge
instead of sharing the optimizer's analysis. This duplicate work is justified when sharing would cause the checker and
optimized path to inherit the same implementation error or invalid assumption.

The architecture must therefore permit a result family to be shareable for ordinary consumers while another consumer
chooses an independent path. A future reuse engine must not assume that every matching subject and valid retained result
should automatically be injected into every subsystem.

---

## 21. Information Preservation and Loss

Whether an IR or another compiler representation may discard a distinction is owned by that representation's ADR and
its legal consumer requirements. ADR-0075 does not define those representation invariants.

If a later subsystem still needs high-level information that will disappear during lowering, the compiler must consume
that information before the loss or preserve the needed derived knowledge through an explicit product boundary. A later
consumer must not reconstruct lost Contract meaning from backend shape, generated code, or other implementation residue.

The same principle applies to summaries and plans. Preserving a compact derived result can be preferable to carrying the
entire earlier representation forward, provided that the derived result is sufficient for its declared consumers and
remains independently valid under its own producer law.

---

## 22. Deterministic Reuse and Clean Reference Path

Reusable computation requires a stable relationship between the producer's declared basis and the legal observation it
exposes. Hidden execution state cannot silently change that observation. Worker scheduling, iteration order, cache
presence, retained state, locale, mutable global state, or previous execution history are implementation conditions
unless
an owning result law explicitly admits them as declared inputs.

For the same applicable legal basis, every supported legal path must expose the same owner-defined legal observation. A
clean formation, direct or persisted reuse, preservation path, maintained update, incremental repair, parallel
execution,
or different physical fusion of mediation responsibilities can change cost and execution history without changing what
the requester is legally allowed to observe. This requirement is observation-level by default; byte-for-byte equality is
required only when an owning artifact law explicitly requires reproducible bytes.

A conceptually complete deterministic clean path remains the correctness reference and legal fallback whenever the
result
is otherwise computable from its explicit inputs. Missing cache entries, dependency history, retained evidence, or
persistent state can increase work, but they cannot become the only source from which the correct result exists. When
the
clean result cannot be produced because required compiler inputs are unavailable or another compiler failure occurs,
ADR-0074 owns that unsuccessful-result boundary.

Any compiler-specific condition that is allowed to change the result must enter through a declared basis appropriate to
that result family. Time, randomness, runtime profile, external capability information, or another genuinely variable
input cannot remain ambient if it can alter the approved observation.

Determinism constrains the exposed result rather than the scheduling algorithm. Independent work can run in parallel,
physical completion order can vary, adaptive algorithms can be used, and storage layout can change. Section 11
separately
governs dependency topology, so a large or dynamic graph remains legal only without turning graph history or discovery
order into an undeclared determinant.

---

## 23. Design Boundary

This ADR deliberately stops before concrete reuse, orchestration, storage, incremental, and subsystem-allocation
machinery. Section 5 defines the allocation constraints, Section 11 defines dependency-topology constraints, Section 13
defines current reuse qualification, Sections 16 and 19 define retention and trust boundaries, Section 17 defines the
common incremental seam, and Section 22 defines deterministic path equivalence.

Design can choose query orchestration, pass managers, direct recomputation, cache tiers, persistent or remote stores,
content addressing, HID or Merkle evidence, fingerprint algorithms, dependency-edge representation, red-green
validation,
push frontiers, SCC-local repair, delta maintenance, shared indexes, summary or plan representation, backend artifact
caches, prediction-guided scheduling, and repair-versus-rebuild policies. No item in that set becomes mandatory merely
because another result family benefits from it.

Mediation responsibilities such as requirement handling, protocol resolution, acquisition, qualification, coherence,
comparison, retention, and delivery can be physically fused or split. Their physical topology is chosen under Section 5
without erasing the logical ownership and observation boundaries defined by this ADR.

Physical representation remains replaceable. Primitive tables, slabs, arenas, immutable snapshots, memory-mapped
storage, or other layouts can be selected according to measured access and lifetime patterns. A later Design can also
change graph granularity, storage topology, or incremental strategy without changing this ADR when the same legal
observations and deterministic results remain preserved.

Measurement and verification belong with those design choices. Work avoided must be compared against hashing, lookup,
validation, retained-state, synchronization, persistence, and memory cost, and optimized paths must remain testable
against the clean reference path without making the test mechanism a second authority.

---

## 24. Remaining Work Before Acceptance

The reuse and incremental boundary is no longer waiting for one universal taxonomy or one universal qualification model.
Section 4 keeps family-specific meaning with its owners, Sections 13 and 17 close the compiler-wide qualification and
work-avoidance laws, and Section 23 leaves concrete mechanisms to Design.

The remaining acceptance dependency is first the unsuccessful-result boundary inherited from ADR-0074. Retention,
qualification, cancellation, corruption, and external availability must continue to map to one consistent compiler-side
failure model once ADR-0074 is accepted or its ownership is reassigned.

A final owner audit is also required against the active HIR, Establishment, 1D, realization, and later MIR/LIR/backend
boundaries. That audit must remove any old text that gives query, cache, graph, or storage infrastructure ownership of
product meaning, semantic equality, or Contract authority, while preserving valid infrastructure responsibilities such
as
scheduling, dependency observation, retention, and comparison acceleration.

The final acceptance pass should then verify terminology and references across this ADR and those owners. It should not
reopen the allocation, protocol-containment, graph-proportionality, qualification, or mechanism-plurality principles
unless a concrete owner-specific counterexample shows that one of those compiler-wide laws is unsound.

---

## 25. Consequences

Kontrakt can reuse compiler-produced knowledge without turning cache state, query topology, retained bytes, physical
placement, or a compiler-wide dependency graph into a second source of meaning. Owner-specific semantic and result laws
remain with the owners identified by Section 4, while cross-responsibility consumption follows the mediated boundary in
Sections 5 through 8.

Current reuse is explicit rather than implied by retention. Section 13 combines the applicable owner-defined conditions
with compiler-owned compatibility and reuse evidence, while Sections 15, 16, and 19 keep coherence, lifetime, integrity,
and trust distinctions visible. Derived summaries, plans, specialized products, and backend artifacts can therefore have
reuse boundaries appropriate to their own declared basis instead of inheriting the widest upstream invalidation scope.

Incremental execution remains a work-avoidance realization rather than a semantic topology. Section 17 permits different
result families to use different reuse or repair strategies, Section 11 prevents those local strategies from becoming an
accidental compiler-wide graph, and Section 22 requires every supported path to agree with the deterministic clean
reference at the owner-defined legal observation boundary.

The cost is that reusable result families need explicit ownership, legal observations, and current-qualification inputs,
and Design must measure whether retention or incremental bookkeeping is actually cheaper than recomputation. The benefit
is that V1 can use simple L1/L2 and local reuse where profitable while V2 remains free to adopt persistent,
differential,
frontier-based, graph-compressed, or other domain-specific techniques without changing Contract authority or consumer
meaning.

This ADR remains Proposed pending the remaining work in Section 24.