# ADR-0075: Compiler-Produced Material and Knowledge, Consumption, Validity, Reuse, and Incremental Boundaries

## Status

Proposed

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
architecture in which produced material and knowledge can cross responsibility boundaries, remain valid or become stale,
be reused or recomputed, and later participate in incremental execution. Common law is introduced only where the same
obligation actually survives across those different cases.

---

## 2. Problem

The compiler needs to avoid two opposite failures.

The first is repeated reconstruction. Several later responsibilities may need the same upstream knowledge. If each one
reopens source, reacquires the same realization body, or rebuilds the same analysis, the compiler repeats expensive work
and can even produce divergent interpretations of one compiler state.

The second is unsound reuse. Once information has been retained, the fact that it still exists says nothing by itself
about whether it is valid for the current computation. A body may have changed while an old summary remains in memory. A
transformation may leave an analysis object allocated even though one of its assumptions is no longer true. A persisted
artifact may still decode after the producer schema or target capability has changed. An earlier dependency trace may no
longer describe the reads performed by a later execution.

A simple producer-consumer example exposes only part of the problem.

```text
upstream material U
    ↓
producer A
    ↓
observation P
    ↓
consumer B
    ↓
result Q
```

If `U` changes, the compiler must determine whether the observation of `P` used by `B` is still current. A changed
observation can force `Q` to be re-established, but that does not always mean a full rebuild. The producer can keep a
proven-preserved result, maintain it through the change, or form it again. If the downstream observation of `Q` is then
unchanged, later work may be able to stop. This is the familiar incremental propagation case.

Other cases have a different shape. A transformation can mutate one IR unit in place and invalidate only some analyses.
A whole-machine consumer can depend on a compact summary while the backend depends on the full body. Under ADR-0063
and the applicable 1D ADR, a fresh runtime Occurrence can coexist with reuse of definition-level compiler knowledge
without reusing the old Occurrence. An independent checker can deliberately recompute information that another subsystem
would normally share because sharing would weaken its value as a correctness oracle.

The architecture therefore has to answer more than whether a cache entry is present. It has to preserve the boundary
between what a producer guarantees, what a particular consumer actually observes, what changes can invalidate that
observation, what retained representation can still be trusted, and what work must be repeated when those conditions no
longer hold.

The difficulty is that these questions occur in many compiler domains with different semantics. A single rule that is
too weak permits stale knowledge to cross subsystem boundaries. A rule that is too strong forces unrelated products to
share the same lifetime, comparison, dependency, or invalidation model and can destroy the scaling benefits that reuse
was intended to provide.

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

The scope also includes cases in which a subsystem does not use the common query or cache machinery internally. A JVM
backend, external adapter, or local pass pipeline can remain independently implemented while still participating in the
same cross-responsibility validity and reuse architecture at its boundary.

Private scratch values are outside this ADR unless they become independently observable or reusable outside the
computation that owns them. The same is true for worker-local temporary state whose lifetime ends with one operation and
whose contents cannot affect a later consumer after that operation completes.

Semantic and representation-specific laws remain with their owning ADRs. ADR-0075 refers to ADR-0071, ADR-0063,
the applicable authority-specific ADRs, and future MIR, LIR, or backend ADRs where those laws are needed instead of
restating them here.

The ADR also stops above concrete reuse machinery. Cache and query architecture remain replaceable, as do dependency
storage, fingerprinting, persistence, and incremental repair mechanisms. A later compiler ADR can give one of those
mechanisms a narrower architectural role when necessary.

---

## 4. Existing Ownership Boundaries

This ADR relies on existing owners rather than copying their laws. Resolved Contract HIR remains under ADR-0071;
Establishment and Established observation remain under ADR-0063 and the applicable authority-specific ADRs;
compiler-side
unsuccessful-result handling remains under ADR-0074. Future MIR, LIR, and backend ADRs likewise own the meaning and
invariants of the representations and results they introduce.

ADR-0075 begins at the cross-responsibility consumption, validity, reuse, and change boundary created when those
owner-defined results are consumed or retained by other compiler responsibilities. Family-specific comparison, validity,
and change rules remain with the producer family. ADR-0075 defines only obligations that survive those
specializations.

---

## 5. Architecture Decision

Kontrakt will not use one universal `CompilerProduct` semantic model for every reusable result. Compiler-produced
material and knowledge differ too much in scope, lifetime, change sensitivity, and failure consequence for a single
product schema or one invalidation rule to remain sound.

Instead, independently consumed compiler material has a producer-owned boundary. That boundary states what completed
guarantee is exposed to consumers and which observations are legal at that boundary. The producer family also owns the
conditions under which the exposed result is current, together with any family-specific comparison needed for reuse or
change propagation.

The compiler-wide architecture supplies the rules that connect those producer-owned boundaries. It governs how a
consumer records its dependence on an upstream observation, how retained material is qualified before reuse, how stale
knowledge is kept out of ordinary consumption, how transformations preserve or invalidate derived knowledge, and how
incremental work may stop when an observed result has been re-established unchanged.

This is a stratified architecture rather than a universal ontology. HIR observations, derived analyses, summaries,
backend artifacts, and retained representations can all participate while keeping their own internal laws. Shared
storage or orchestration does not transfer ownership of the guarantee or validity rule being executed.

The local relation is therefore:

```text
producer-owned guarantee
    ↓ exposes
legal observation
    ↓ consumed by
another compiler responsibility
    ↓ may form
new compiler-owned material or knowledge
```

A later reuse path may avoid some of that work, but only after the relevant producer-owned validity conditions have been
satisfied.

---

## 6. Working Classification Boundary

The research inventory deliberately collected more cases than this ADR should eventually own. Before the ADR is
accepted, those cases must be classified so that a rule introduced for one family is not silently applied to another.

The classification is not based on class names or storage objects. It examines what kind of material is being consumed,
how long it can outlive the computation that produced it, what kinds of change can affect its guarantee, and what the
consequence of stale reuse would be. A run-local analysis result and a cross-session backend artifact can both be
reusable while requiring very different qualification and integrity checks.

The current survey also shows several legitimate responses to change. Some results remain valid. Others are invalidated
and formed again. Certain families can be maintained during the change or repaired incrementally, while retained
material can be loaded again after qualification. These are possible responses, not universal lifecycle states that
every result must implement.

The final taxonomy remains open. The rest of this Proposed ADR records common obligations only where the current
evidence is strong enough, and it keeps family-specific questions visible instead of forcing them into one generic
schema.

---

## 7. Produced-Material Boundary

A cross-responsibility boundary exists when one compiler responsibility exposes completed material or knowledge on which
another responsibility may rely without entering the producer's private construction state. The boundary identifies the
producer and the guarantee being exposed. It also identifies the logical subject of that guarantee to the extent needed
by that result family.

This requirement does not create a new compiler-wide identity system. The boundary uses the subject or reference law
supplied by the result's owner and requires only that a consumer can identify the producer-owned result it uses without
treating incidental storage as the meaning of that result.

A boundary does not require a one-to-one physical representation. One completed observation can span several structures,
and several logical results can share backing storage. Physical fusion does not merge their ownership, while physical
separation does not create a new semantic distinction.

A result that never leaves one private computation does not need this boundary merely because it is expensive. It
becomes relevant to ADR-0075 when another responsibility is allowed to observe it independently, when it is retained for
later use, or when change propagation depends on it as a stable compiler result.

---

## 8. Legal Consumption and Derived Knowledge

A consumer uses the observation exposed by the producer boundary it actually consumes. It must not reopen
producer-private state to reconstruct a stronger result, and it must not replace an upstream semantic observation with
facts inferred from an incidental storage layout or generated artifact.

When a consumer combines legal observations and computes new information, the resulting knowledge belongs to that
compiler responsibility. This is important for analyses and summaries because they can be widely reused without becoming
a second semantic authority.

The compiler can intentionally create a shared producer when several subsystems need the same expensive derived
knowledge. For example, a realization analysis can produce an effect summary that verification and optimization both
consume. The fact that diagnostics happened to compute the same information first is not a sufficient architectural
reason to make diagnostics the producer for the backend.

Shared knowledge is therefore explicit. Accidental execution order must not decide which consumer becomes the owner of
information used by other subsystems.

---

## 9. Change, Validity, and Preservation

Produced knowledge is valid only while the guarantee exposed by its producer remains true for the current computation.
Storage lifetime and object lifetime do not establish that condition.

A transformation is one common source of change. It can preserve an earlier result, update the result as part of the
transformation, invalidate it for later recomputation, or make incremental repair possible. The exact strategy belongs
to the producer and transformation design, but a stale result must not remain available to ordinary consumers as if no
relevant change had occurred.

This rule applies beyond classical middle-end analyses. A changed realization body can stale a transitive summary. A
changed target capability can stale a backend plan. A changed obligation can stale a generated PBT plan. A changed
compiler schema can make a persisted result unreadable or incompatible even when the clean result would otherwise be the
same.

A claim that a result was preserved is itself a correctness claim. The compiler cannot treat preservation as a
performance hint that may be wrong without consequence. The claim must be sound for the exact guarantee that later
consumers are allowed to observe.

The same requirement applies when a logical subject is replaced or deleted. An allocated analysis object, dense handle,
or table slot that survives the change does not prove that the old result still belongs to the current subject. Reuse
must not depend on stale physical identity after the subject relation that justified the result has ended.

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

## 11. Dependency Observation

A derived result that may be reused after change needs enough information to determine which upstream observations can
affect its guarantee. Compiler dependency observation does not redefine relations owned elsewhere. Contract and HIR
semantic relations remain under their owning ADRs, while program and artifact dependencies retain their own owners. One
computation can observe several such relations without merging their meanings.

Dependency capture must be sound. Omitting an upstream observation that can change the result allows stale material to
survive under-invalidation. At the same time, making a narrow result depend on an unrelated whole-world fingerprint can
turn every small change into global invalidation. The former is a correctness failure. The latter can make the reuse
architecture operationally useless even when it remains correct.

A previous dynamic read set is evidence about one execution, not a complete law for all future executions. If changed
input can alter control flow or which upstream result is consulted, the dependency mechanism must validate or rediscover
the current relation rather than treating the old trace as permanent truth.

Some results make a closed-world or complete-set claim. Revalidating only the members previously present is then
insufficient. A call-target set, whole-machine reachability result, or other complete collection can become stale when a
new member appears even if every old member remains unchanged. The producer must include whatever closure condition is
necessary for the completeness guarantee it exposes.

The physical graph representation remains Design. A producer can track dependencies more finely inside its
implementation than it exposes to other responsibilities. A subsystem outside a future common query engine can also
declare dependencies explicitly at its boundary.

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

## 13. Result Comparison and Reuse Qualification

ADR-0075 does not define semantic equality. Semantic producers keep the equality laws already established by their
owning ADRs. Compiler-derived result families can nevertheless need a producer-owned comparison to decide whether an
earlier result and a newly established result are equivalent for that result family's exposed guarantee.

The comparison belongs to the producer boundary, not to an arbitrary consumer. If verification depends on a compact
effect summary while the backend depends on the executable body, the two consumers can observe different producer-owned
results or projections. The verifier does not obtain the right to declare the complete body equal merely because the
change is irrelevant to its question.

Retained material introduces another distinction. A representation can exist and still be incompatible with the current
producer implementation. It can be compatible to decode and still fail the current validity rule. It can be
current-valid and still be rejected because integrity or trust validation failed. These conditions must not collapse
into a single cache-hit predicate.

The final modeling of result-determining inputs and reuse-only compatibility conditions remains open. The architecture
must preserve the distinction even if a later Design represents them in one metadata structure. A schema revision, for
example, can invalidate an old serialized representation without changing the result that clean computation would
produce.

A fingerprint, content hash, HID, byte comparison, or object identity can support reuse evidence. None becomes semantic
equality merely because it is fast to compare.

---

## 14. Summary and Multi-Consumer Reuse

Owner-defined observations remain governed by the result family that exposes them. HIR and Established projections in
particular remain under ADR-0071 and ADR-0063. This section concerns separately derived summaries and other compiler
knowledge formed so that multiple consumers can reuse a bounded result without reopening the producer's private state.

A wider consumer does not necessarily need the full body. Whole-machine verification or planning can often depend on
local summaries, and a backend can still require body-level material. This allows one local change to invalidate backend
work while leaving a higher-level verifier unchanged when the summary that verifier consumes is re-established
unchanged.

A useful summary is complete for its declared question without becoming a miniature copy of all upstream material. If it
omits information required by that question, the consumer will eventually be forced to reopen upstream internals or will
make an unsound decision. If it carries every upstream detail, it loses much of the memory, invalidation, and
persistence advantage that motivated the summary.

Summary schemas, aggregation algorithms, and physical indexes remain owned by the subsystem that defines them. ADR-0075
governs only their role as independently consumed derived knowledge.

---

## 15. Completion, Visibility, and Coherent Observation

An ordinary consumer must not observe a successful result before the producer has completed the guarantee associated
with that result boundary. Internal partial state can exist while a producer is working, but it remains private unless a
separate protocol explicitly gives that partial state meaning.

A consumer that needs several related observations must see a coherent set permitted by their producers. Combining an
old value from one generation with a new value from another can create a compiler state that no completed producer ever
exposed. A matching numeric generation alone is not proof of coherence if the underlying products have different
validity domains.

The architecture must support both snapshot-style replacement and in-place mutation. A daemon or future IDE compiler may
keep an older coherent view alive while a new view is formed. A local optimizer may mutate one IR unit in place and
invalidate selected analyses. ADR-0075 requires coherent legal observation in both cases without prescribing one
memory-management mechanism.

A failed or cancelled replacement must not expose a partially completed successful result. Whether an older completed
result remains usable after that failure depends on its own current-validity law, not on the mere fact that a newer
attempt was started.

Retention and reclamation are separate from current validity. Keeping old backing storage alive for a reader does not
make it current for new consumers, while reclaiming unused backing does not retroactively change the meaning of a result
that was validly consumed earlier.

---

## 16. Retention, Cache, and Persistence

L1, L2, persisted state, content-addressed storage, and future remote caches are mechanisms for making earlier compiler
work available again. They are not the law that decides whether that work can satisfy the current request.

The earlier L1/L2 direction remains useful in this architecture because it can retain information Kontrakt already
interpreted instead of forcing later consumers to reread classes, attributes, or other raw inputs. The reusable unit,
however, is defined by the producer boundary and its validity law rather than by the container in which the
representation happens to be stored.

A retained representation must be qualified before use. The required checks depend on the result family and retention
lifetime. In-memory generation reuse may need a different compatibility check from a cross-session serialized artifact.
An externally obtained or remote artifact adds integrity and trust questions that an in-process cache may not have.

Missing retained state is not a semantic event. If the requested result is otherwise computable, the compiler can form
it again through a legal clean path. Corrupt or incompatible retained state is likewise a compiler problem and follows
ADR-0074 where it prevents the requested compiler result from being produced.

Concrete retention machinery remains Design. The compiler can change its hashing, cache layout, serialization, or
storage engine without changing the producer-owned guarantee or the validity law they implement.

---

## 17. Incremental Reuse and Change Propagation

Incremental execution builds on the producer, dependency, and validity boundaries defined earlier. It is not a separate
source of meaning and it is not equivalent to persistence.

A previous result can avoid work in several ways. The compiler may directly reuse retained material after establishing
that its validity still holds. It may recompute a changed producer and discover that the result observed downstream is
unchanged. It may update or repair a result incrementally rather than rebuild it from the beginning. Different result
families can choose different strategies.

The common requirement is that downstream work stops only after the observation on which that downstream work depends
has been re-established as current and unchanged. An upstream source edit, a changed cache key, or a different physical
generation is not enough by itself to determine whether propagation must continue.

```text
upstream change
    ↓
validate / preserve / update / repair / recompute producer result
    ↓
consumer-visible observation changed?
    ├─ no  → downstream may remain reusable
    └─ yes → downstream validity must be reconsidered
```

This is the architecture seam required for early cutoff. The particular mechanism can be red-green validation,
generation comparison, delta maintenance, explicit invalidation, or another algorithm that preserves the same
obligations.

V1 is not required to persist every result or to implement fine-grained incremental repair everywhere. It must avoid
boundaries that make later incremental reuse impossible without changing the semantic model. V2 can add stronger
dependency tracking and retained state behind those boundaries.

---

## 18. Backend, External Subsystems, and Target-Specific Products

A subsystem does not leave this architecture merely because it is implemented outside a common query engine. The JVM
backend, JDK Classfile facilities, generated-artifact code, or another external adapter can use their own local
implementation while exposing a result boundary that states what upstream observations and target conditions determine
the product.

Backend products often have a different reuse radius from semantic or verification products. A helper body change can
leave a verification summary unchanged while still requiring new bytecode. Target baseline, classfile version, backend
capability, debug mode, or profile information can affect one backend product without changing upstream Contract
meaning.

Cross-stage and cross-tool reuse therefore needs an explicit boundary. Earlier high-level knowledge can be translated or
summarized for a backend, but ancestry does not make every earlier analysis valid after target lowering. An opaque tool
output can be retained only under the compatibility and validity conditions of the adapter that owns that result.

Profile and cost information require additional care. They can guide profitability or product selection without becoming
a proof that a transformation is legal. Where a producer uses such information, its role in the compiler result must
remain explicit so that profile change does not accidentally invalidate unrelated semantic judgments or, in the opposite
direction, leave a profile-dependent optimization plan current after its basis changed.

---

## 19. Failure, Cancellation, Integrity, and Trust

ADR-0074 owns the representation of unsuccessful compiler results. ADR-0075 defines only the boundary conditions
required by reusable material.

A failed or cancelled producer must not leave ordinary consumers with a partially formed successful result. A previous
completed result can remain available only if its own current-validity law still permits use. Recovery cannot turn
incomplete construction state into a completed result merely to preserve cache continuity.

Persistent and external material adds integrity risk. Successful decoding proves only that bytes can be read. It does
not prove that the producer schema is compatible, that the dependency basis is current, or that the material came from a
trusted source. A failed integrity or trust check cannot be converted into successful reuse.

Reuse validity is also not proof that the original producer was correct. A result can be perfectly reusable according to
its dependency metadata and still have been produced by a buggy analysis or transformation. Verification, translation
validation, differential testing, and other correctness mechanisms therefore remain distinct from reuse qualification.

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

## 22. Deterministic Reuse and Hidden State

Reusable computation requires a stable relationship between the producer's declared basis and the result it exposes.
Hidden execution state cannot silently change that result. Worker scheduling, iteration order, cache presence, locale,
or mutable global state are examples of implementation conditions that must not become undeclared determinants.

This section does not restate Contract semantic determinism. It applies to compiler-owned results that are intended to
participate in sharing, retention, or incremental execution. Any compiler-specific condition that is allowed to change
such a result must enter through an explicit producer-owned basis appropriate to that result family.

Legal nondeterministic input, if a future subsystem genuinely needs it, cannot remain ambient. Time or randomness must
enter through a producer-owned form that makes reuse qualification possible. Runtime profile and external capability
information require the same explicit treatment when they affect the result.

Determinism constrains the exposed result, not the scheduling algorithm. Independent work can still run in parallel, and
physical completion order can vary, provided that the resulting legal observations satisfy the producer's declared
guarantee.

---

## 23. Design Boundary

This ADR deliberately stops before concrete reuse machinery.

A query engine can automate dependency capture and memoization, while a pass manager can remain a better fit for ordered
local transformations. A data-flow engine can update one analysis incrementally, while another product is cheaper to
recompute. A backend can expose coarse artifact dependencies without moving its entire implementation into the query
system.

Physical storage remains free to follow measured access patterns. Primitive tables, slabs, arenas, or snapshot-like
structures are all possible realizations. Dependency capture can likewise be dynamic or explicit, and persistent
material can use any compatible format that respects the producer boundary.

Those choices become Design work after the architecture has closed the relevant result family, validity basis,
dependency boundary, and failure behavior. ADR-0075 must not freeze an algorithm merely because the first implementation
needs one.

---

## 24. Open Decisions Before Acceptance

The research inventory is intentionally broader than the current normative body of this ADR. Several classification
questions still need to be closed before the document can move to Accepted.

The first is the final taxonomy of compiler-produced material. It must separate kinds of material whose ownership,
lifetime, and invalidation behavior are materially different. The categories should follow those behaviors rather than
the current class layout.

The second is the exact reusable-result qualification model. The ADR still needs to decide how result-determining
conditions, reuse-only compatibility conditions, assumptions, scope, and producer versioning are represented without
creating one universal metadata tuple.

The third is the cross-responsibility dependency contract. The current direction requires sound dependency on the
producer-owned observations actually used by a consumer, but the boundary between public observation dependency and
finer producer-private dependency still needs to be closed carefully.

The fourth is preservation and repair. The common rule that stale knowledge cannot remain visible is clear, while the
exact architecture contract for preservation claims, incremental updates, stage-crossing translation, and complete-set
validity still needs further review against the surveyed compiler families.

The fifth is coherent multi-product observation. V1 can remain simpler than a persistent IDE compiler, but the API must
not assume one mutable global state if V2 will require readers to remain on one coherent view while another view is
being formed.

The sixth is the relationship with ADR-0074. Unavailable, corrupt, unsupported, cancelled, and internally failed
compiler results must have one consistent unsuccessful-result boundary before retention and incremental recovery can be
considered closed.

---

## 25. Consequences

This structure lets Kontrakt reuse compiler knowledge without turning cache, query topology, or retained bytes into a
second source of meaning. It also avoids a different failure mode in which every subsystem invents its own unrelated
rules for stale knowledge, persistence, and incremental change.

The architecture is intentionally more demanding than a generic memoization layer. A reusable boundary has to state what
a producer guarantees, what a consumer observes, what can invalidate that observation, and how retained material is
qualified. That work is required only where material actually crosses a responsibility or reuse boundary; private local
computation is not forced into a heavyweight protocol.

The benefit is that semantic and IR families can evolve independently while later compiler subsystems still share one
horizontal architecture for valid reuse. The same architecture can support analysis and verification today, then extend
to backend retention or future incremental execution without forcing those subsystems into one product model.

This ADR remains Proposed. The next work is to classify the collected internal and external cases, test the candidate
categories against known failure modes, and then close only the common laws that remain valid across those categories.
The pre-taxonomy research inventory remains the evidence base for that review rather than being copied into this ADR.