# ADR-0061: Kontrakt Compiler Diagnostic Architecture

## Status

Proposed

## Date

2026-08-23

## Related

- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/kontrakt-frontend-and-contract-refactor-plan.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- `docs/constitution/canonical-ir-stage-and-lowering-protocol.md`
- ADR-0060: Diagnostic Evidence and Retention Contract
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

Kontrakt needs a commercial-grade diagnostic subsystem for the compiler itself. Parser failures, resolution conflicts,
verifier rejections, backend capability refusals, optimization remarks, incremental invalidation, and internal compiler
errors are not occurrences of the user's Diagnostic Evidence Contract. They are diagnostics produced by the tool that
constructs the Contract Machine.

The compiler nevertheless knows more semantic structure than an ordinary source checker. A diagnostic can identify the
Contract authority that was written, the canonical obligations that conflicted, the selected Policy World, and the exact
verifier or optimization judgment. The user-facing explanation should project that structured information back into the
user's Contract vocabulary instead of exposing compiler implementation names.

Rich explanation must not imply a large hot-path diagnostic representation. The compiler should preserve compact source
and semantic relations, reuse existing analyses, and materialize expensive explanation only when a consumer requests it.
This ADR therefore treats diagnostics as a compiler subsystem with provenance, preservation, invalidation, deterministic
merge, cost control, and rendering boundaries.

## 2. Decision Drivers

Compiler diagnostics are structured products before they are text. Stable identity, source relationships, semantic
subjects, causal relations, fix data, and reproducer handles cannot depend on an English message string.

Ordinary diagnostics speak in the vocabulary of the Contract that the user wrote. Canonical ordinals, solver clauses,
pass-local nodes, lowered gates, and other compiler implementation objects remain engineering evidence. They are not the
default explanation language.

The projection from internal evidence to a user-facing Contract explanation is itself a correctness boundary. A friendly
explanation must be derivable from the exact semantic subject, source provenance, and compiler judgment that established
the diagnostic.

Diagnostic generation follows compiler determinism. Worker count, scheduling, hash iteration, cache hit order, object
address, and wall-clock completion order cannot decide which diagnostics exist or how independent diagnostics are
ordered.

Incremental reuse tracks semantic and diagnostic provenance separately. A semantic result may remain reusable after a
source-only edit while its source projection becomes stale. The reverse mistake is also invalid: an unchanged span
cannot keep a diagnostic alive after its semantic dependency changed.

Compiler transformations preserve trustworthy diagnostic provenance or invalidate it explicitly. A precise-looking stale
location is a correctness defect, not merely poor presentation.

Deep compiler diagnostics have a cost. Normal compilation keeps compact identities and relations, while conflict
reduction, pass histories, proof objects, IR snapshots, and reproducer enrichment are materialized selectively.

V1 must establish these seams directly. V2 may improve reuse and explanation quality, but it must not require replacing
a global string logger or pass-local output side effects after the compiler architecture already depends on them.

## 3. Kontrakt Compiler Diagnostic Architecture

### 3.1. Compiler Diagnostics Are a First-Class Compiler Subsystem

Kontrakt compiler diagnostics are not `println` calls attached to parser and verifier branches.

The compiler must construct a structured diagnostic product from semantic and provenance information, then render that
product for each consumer. The architecture is comparable to an optimizer because it has analyses, preservation rules,
invalidation, cost decisions, and multiple output forms.

```text
compiler input
    ↓
semantic / IR analysis
    ↓
source and authority provenance
    ↓
diagnostic recognition
    ↓
structured diagnostic occurrence
    ↓
causal assembly and deterministic merge
    ↓
CLI / IDE / CI / machine output / reproducer
```

The compiler's diagnostic engine may share low-level storage utilities with Contract Evidence implementation. It may not
reuse Contract authority simply because the records have similar fields.

### 3.2. Diagnostic Definition and Compiler Occurrence Are Separate

Each stable compiler diagnostic kind needs an identity independent of one source occurrence.

The definition identifies what compiler condition is being reported. The occurrence binds that definition to a
particular Compilation Session, semantic subject, source projection, and causal context.

A source line number is therefore not the diagnostic identity. Line numbers move. A pass object address is not the
identity. Workers change. Human wording is not the identity. The compiler needs a stable code that can survive renderer
changes and support CI suppression or IDE correlation where policy allows it.

The occurrence may carry a revision-scoped identifier for incremental tooling. That identifier is not required to remain
stable across unrelated builds unless a later protocol explicitly promises it.

### 3.3. A Compiler Diagnostic Record Is Structured before Text

The canonical compiler diagnostic product should be able to carry a stable code and severity together with the phase
that recognized the condition. It also needs a primary semantic subject so the issue can be attributed to a Contract
authority or compiler artifact instead of only a file offset.

Source presentation is attached separately. One primary source anchor identifies the best place for a user to start.
Additional anchors can show related declarations or conflicting sources. Structured arguments provide the values needed
by renderers without embedding those values into a preformatted sentence.

Notes, help, fix proposals, causal links, and optional artifact handles extend the occurrence. They do not all need to
be present for every diagnostic, and their absence does not change the diagnostic code.

This structure allows a CLI renderer to produce a compact explanation while an IDE uses exact ranges and a CI system
uses stable codes from the same occurrence.

The structured record is not a serialized dump of the compiler's private model. Internal evidence can name a canonical
definition, a conflict set, an analysis fact, or a lowered artifact through compact references, but the ordinary
user-facing explanation projects those references back into the vocabulary of the declared Contract Machine. A user who
wrote an Output and a Publication should read about that Output and Publication, not about a frozen table ordinal or a
subset-check implementation class.

This gives compiler diagnostics two distinct views over one occurrence. The engineering view can expose pass identity,
query dependency, raw conflict material, and backend state when compiler developers need them. The user view answers a
different question: which Contract was involved, what that Contract required, what the compiler established, why the two
are incompatible or noteworthy, and which declaration the user can inspect. Both views originate from the same
structured diagnostic product, so readability does not require reconstructing semantics from strings.

Severity and source location remain part of the compiler diagnostic envelope rather than the explanation itself.
Severity tells tooling how the compiler classifies the issue. Source anchors tell the user where to start looking.
Neither field substitutes for the semantic evidence that explains why the diagnostic exists.

### 3.4. SourceManager and Provenance Are Mandatory Compiler Infrastructure

Kontrakt V1 requires a real source manager rather than storing raw path and line strings inside diagnostics.

A source anchor must be able to identify source text, an exact range or point, and the relationship between
user-authored text and generated or transformed material. As the frontend grows, the compiler may need to distinguish a
written location from a derived location in generated host artifacts or expansion-like frontend constructs.

The SourceManager must not make filesystem paths semantic identity. Paths are presentation and workspace coordinates.
Internally the compiler should use stable source-file identities within the compilation input and explicit revision
state.

Canonical Contract material may retain a compact origin handle even after frontend syntax authority has been erased.
That handle supports diagnostics and debugging; it does not turn source syntax back into Contract authority.

### 3.5. Semantic Provenance Survives Transformation Differently from Source Spans

Source provenance and semantic provenance are related but not identical.

A canonical Contract definition can remain semantically identical after a source-only edit while its source span moves.
A lowering pass can replace one IR node with another while preserving the same Contract authority. A fusion pass can
combine several generated gates while each original Contract judgment still needs distinct attribution.

The compiler therefore needs provenance mappings that can say which canonical authority a transformed object realizes
without using the transformed object's storage identity as the authority.

This is also the basis for reliable generated-system diagnostics. A JVM gate can report a compact canonical authority ID
even if optimization has removed the original high-level IR node.

### 3.6. Diagnostic Preservation Is a Pass Obligation

Pass infrastructure already needs analysis preservation and invalidation. Diagnostic provenance must participate in the
same discipline.

A pass that changes an IR region must declare which diagnostic relations remain valid. If it moves or combines semantic
material, it must update the mapping used for source and authority attribution. If it cannot preserve a relation, it
must invalidate that relation so a later query recomputes it.

A transformation that preserves generated execution semantics but leaves a diagnostic pointing at the wrong Contract is
not fully correct. An optimization that removes required Contract Evidence is not legal. These are two different
preservation checks, and both belong in the transformation contract.

The verifier should be able to run a stronger `verify-each` mode that checks provenance invariants at pass boundaries.
V1 does not need a formal proof for every pass, but it needs a place for translation-validation and differential checks
to attach later.

### 3.7. Diagnostic Correctness Is Stronger than Diagnostic Presence

The compiler must treat a wrong diagnostic relation as a correctness defect.

A source anchor may be stale. A related note may point to a declaration that no longer participates in the conflict. A
cached optimization explanation may refer to a target model that is no longer active. A crash dump may come from an IR
state that had already violated its verifier.

For this reason, a diagnostic product needs explicit dependencies and provenance. Where the compiler cannot establish a
trustworthy location, it is better to emit a location-less but correctly attributed issue than a precise-looking false
location.

### 3.8. Root Cause and Cascade Diagnostics Need a Causal Model

Parser recovery, type-like resolution, composition verification, and backend selection can create cascades. One missing
symbol may make twenty later checks fail only because they lack the original meaning.

Kontrakt should not suppress every dependent issue blindly, because some later diagnostics may be independently real. It
should instead record causal relations where the compiler knows that one diagnostic depends on an earlier invalid state.

The renderer can then keep the root issue prominent and attach dependent notes or suppress redundant cascades according
to policy. Machine output retains the relation so an IDE or CI tool does not have to guess based on message order.

Causal relation is not runtime temporal order. It is a compiler explanation that one invalid state prevented or
invalidated another analysis.

### 3.9. Contract Conflict Diagnostics Need Reduced Conflict Material

Some verifier errors arise from a set of declarations that cannot all hold at once.

The authoritative result is that the composition is invalid. The most useful diagnostic is usually not the complete
internal solver state. It is a small set of user-visible Contract obligations sufficient to demonstrate the conflict.

Kontrakt should therefore leave an architectural seam for conflict-core extraction. A first implementation may return a
non-minimal but deterministic conflict set. Later versions may spend additional solver work to reduce it when that
improves usability.

If an external solver is used, its proof, unsatisfiable core, or internal lemma trace is compiler verification material,
not automatically user presentation. A proof certificate can independently validate a result while a smaller conflict
core explains it. These two outputs solve different problems.

An `unknown`, timeout, or unsupported solver result must never be rendered as proof of inconsistency. The diagnostic
must state that verification could not establish the required conclusion at the relevant compiler boundary.

### 3.10. Notes, Help, and Fixes Have Different Trust Levels

A note adds related information. Help suggests a direction. A fix proposes a concrete source edit.

The compiler should not represent all three as message strings because automated tooling needs to know which actions are
safe. A fix has to carry structured edit information and an applicability level. The strongest applicability should be
reserved for edits that the compiler can apply without placeholders or hidden semantic guesses.

Kontrakt should validate automatically applied fixes by reparsing and recompiling the edited input in tests. A fix that
changes Contract meaning may still be useful, but it cannot be labeled machine-safe merely because it makes the current
error disappear.

V1 can be conservative. A small number of high-confidence fixes is better than broad speculative rewriting.

### 3.11. Compiler Remarks Are Not Errors

A serious compiler needs structured explanations for successful or missed optimization decisions as well as rejection
errors.

Kontrakt should support remarks for its own optimization space. A World specialization may be applied because the
selected policy is compile-time constant. A gate fusion may be missed because diagnostic preservation would be lost. A
cache entry may be unusable because a dependency fingerprint changed. A backend specialization may be rejected because
the target capability does not satisfy a Contract guarantee.

These remarks are not Contract results and do not affect build validity unless another rule explicitly promotes the
underlying condition to an error. They are compiler explanation products.

The default build should not emit every remark. Users and compiler engineers need filters by pass, Contract authority,
source region, or remark class so the explanation cost stays bounded.

### 3.12. Optimization Explanation Must Separate Legality from Profitability

Kontrakt's optimizer should be able to explain two different reasons for not transforming IR.

A transformation can be illegal because it would change Contract meaning, break diagnostic preservation, violate a
backend capability, or invalidate a required boundary. It can also be legal but not profitable according to code size,
allocation, compile time, runtime work, target behavior, or another cost model.

```text
opportunity found
    ↓
legality analysis
    ├─ illegal -> explain preservation or capability blocker
    ↓
profitability analysis
    ├─ not profitable -> explain cost decision
    ↓
transformation
```

A missed-optimization diagnostic that mixes these categories becomes misleading. The user needs to know whether the
compiler could not legally perform the optimization or merely chose not to.

V2 may add target-aware or profile-guided cost information. The V1 diagnostic schema should already have a place to
identify the pass, opportunity, blocking analysis, and decision class without exposing unstable internal pointer graphs.

### 3.13. Transformation History Is Selective

Recording the entire IR history of every compilation would be prohibitively expensive.

The compiler should retain stable phase and pass boundaries, transformation counters, and enough provenance to request a
deeper explanation. Detailed before/after IR, decision traces, or proof artifacts can be enabled for selected passes or
sources when investigating a problem.

This mirrors production runtime diagnostics: keep cheap anchors continuously and increase detail around the question
that actually matters.

A deterministic compiler also has a second option that many runtime systems do not. It can reproduce a compilation from
normalized inputs and recapture a deeper trace later. This makes reproducer quality part of diagnostic architecture and
reduces pressure to persist every internal state.

### 3.14. Incremental Diagnostics Are Query Products, Not Side Effects

A query or pass must not publish permanent diagnostics directly while it is still speculative or cacheable.

The computation returns a diagnostic product tied to explicit dependencies. The driver or diagnostic engine commits
those products only for the current compilation revision. If the query is invalidated or cancelled, its unpublished
products disappear with it.

This rule prevents stale diagnostics from surviving a source change simply because an earlier worker already wrote them
to a global sink.

It also lets V2 reuse a semantic query result while recomputing only its source projection. The semantic fingerprint can
remain green while a provenance or presentation fingerprint changes.

### 3.15. Semantic and Diagnostic Dependencies Are Tracked Separately

The query graph needs to know when diagnostic state depends on source details that the semantic result does not.

A whitespace edit may preserve a canonical definition but change a source range. A comment edit may change an excerpt
without changing a diagnostic code. A renamed source file may change a rendered path while preserving semantic identity.
Conversely, a Contract Version change may leave the same text range but invalidate the diagnostic's semantic subject.

V1 may recompute both sides conservatively. It must still model them separately so V2 red/green invalidation can stop at
the correct boundary instead of storing source spans inside every semantic cache value.

### 3.16. Incremental Publication Needs Revision Discipline

IDE diagnostics are observed over a moving source document. A result computed for revision N may arrive after revision
N+1 has already become current.

Kontrakt tooling therefore needs revision-aware publication. A diagnostic product must be attached to the input revision
that justified it, and stale asynchronous products must be discarded rather than rendered on a newer buffer.

A future LSP layer may use pull-style result identities or unchanged reports to avoid sending stable diagnostics again.
Those protocol choices sit above the compiler diagnostic engine. The engine itself must already know which revision and
dependency state a diagnostic belongs to.

### 3.17. Parallel Diagnostic Merge Is Deterministic

Workers accumulate structured diagnostics locally or through concurrency-safe buffers that do not expose completion
order.

After the relevant compiler boundary is complete, diagnostics are merged by deterministic keys derived from semantic and
source order. Causal children remain attached to their root. Duplicate products are removed only when they represent the
same compiler occurrence under an explicit equivalence rule.

The merge key must not become diagnostic semantic identity. It is a publication order for reproducibility and human
stability.

Single-worker and multi-worker builds must produce the same diagnostic set and the same deterministic presentation when
the source and compiler configuration are otherwise identical.

### 3.18. Deduplication Must Not Collapse Independent Causes

Two diagnostics with the same code on the same line are not necessarily the same occurrence.

Deduplication needs semantic subject and causal context, not just rendered location and message. Otherwise independent
Contract authorities can be collapsed into one issue and hide useful information.

The opposite problem is repeated emission of the same root condition through multiple analysis paths. Stable occurrence
keys allow the compiler to collapse those duplicates without relying on string equality.

### 3.19. Max-Error and Recovery Policy Are Presentation Controls over Compiler Work

Parser recovery and semantic analysis may continue after an error to expose more useful independent issues.

The compiler should have an error budget so pathological input cannot cause unbounded diagnostic work. Reaching the
budget stops further diagnostic production according to compiler policy; it does not change the semantic status of
errors already established.

Recovery nodes and poisoned compiler values must carry explicit state so later analyses know when an error is dependent
on invalid input. They cannot be ordinary semantic values that accidentally pass verifiers.

### 3.20. Human Rendering Is a Consumer of Diagnostic Data

The default terminal presentation should optimize for root-cause proximity, stable source context, and readable causal
structure.

It may show excerpts, labels, notes, and suggestions. None of those rendered characters are the stored compiler
protocol. Machine-readable consumers receive structured codes and ranges directly.

Changing colors, wording, wrapping, excerpt size, or terminal capabilities does not change the compiler diagnostic
identity.

The default renderer performs semantic compression. It selects the smallest user-visible Contract cause that still
explains the compiler judgment, then presents related declarations only when they change what the user must understand.
An internal verifier can traverse a much larger dependency graph without forcing that graph into the terminal output.
When several internal facts support one explanation, the renderer names the Contract rule and the declarations that make
the rule fail rather than narrating the compiler's search procedure.

This is not lossy with respect to the diagnostic truth. Deeper compiler modes can follow artifact handles back to the
conflict set, analysis result, pass decision, or reproducer when an engineer needs the internal account. Ordinary users
receive the Contract-level explanation because that is the language in which they can correct the source.

### 3.21. IDE and CI Output Share the Same Source Diagnostic Product

Kontrakt should not implement one diagnostic logic for CLI and another for IDE.

An IDE needs revision-aware ranges, related locations, code actions, and stable machine data. CI needs stable codes,
structured severity, source coordinates, and deterministic output. A command-line user needs good text. The diagnostic
engine provides one product that each frontend adapts.

This also keeps fixes honest. The same structured edit that appears as a CLI suggestion can be offered as an IDE action
without reparsing a human string.

### 3.22. Compiler Self-Observability Is Adjacent but Separate

Phase timers, allocation volume, query hits, invalidation counts, frozen-image size, planner work, pass statistics, and
artifact size describe compiler operation.

They are essential for making Kontrakt itself fast and scalable, but they are performance observability rather than user
semantic diagnostics. The compiler can correlate an expensive diagnostic or pass with these metrics, yet a build warning
should not be created merely because an internal timer exists.

The separation allows aggressive measurement in compiler engineering builds without stabilizing every metric as a public
diagnostic protocol.

### 3.23. Internal Compiler Errors Need a Stable Reproducer Core

An internal compiler error is different from an invalid user Contract. The compiler should state that distinction
clearly and preserve a reproducer core that does not depend on the potentially corrupted state at the crash point.

The core should identify the compiler build, normalized driver options, input subset or dependency identities, target
and backend capability snapshot, selected Version and Policy inputs where relevant to compilation, the active phase or
pass, and the last successfully verified IR boundary.

A deterministic dump of that last valid boundary is more trustworthy than a large dump of an already-corrupted mutable
object graph. Optional stack, heap, JVM, or OS crash material can be attached as supplemental implementation evidence.

V2 may automate reduction and pass bisection. V1 must already retain enough identity to reproduce and manually isolate
the failure.

### 3.24. Reproducer Material and Diagnostic Records Are Different Products

A diagnostic record explains what the compiler observed. A reproducer bundle contains enough input and configuration to
make the compiler observe it again.

One compiler issue may have a small diagnostic record and a large reproducer. The reproducer may include source subset,
frozen IR, capability state, or seed information that should not be emitted in normal terminal output.

The two products can reference each other through stable artifact handles. Retention and privacy policy for reproducer
bundles belongs to compiler tooling, not the user's Diagnostic Evidence Contract.

### 3.25. Diagnostic Testing Is Part of Compiler Correctness

Kontrakt's compiler QA must test diagnostic semantics rather than only snapshot terminal text.

A test can assert the stable code, primary semantic subject, source anchor, related anchors, and structured fix. A
separate golden test can cover the terminal rendering. This avoids making whitespace in a pretty-printer the only test
of a semantic diagnostic.

Metamorphic tests are especially important. Reordering unrelated declarations, changing whitespace, varying worker
count, taking a cache hit instead of a miss, or compiling clean versus incrementally must not change diagnostic meaning.
Optimization-level changes may change internal remarks but cannot move an error to a different Contract authority.

Invalid-input fuzzing should check for missing, contradictory, duplicated, or misleading diagnostics in addition to
compiler crashes. Pass fuzzing should verify that transformations preserve source and Contract provenance. Fix-it tests
should apply the edit and compile again.

### 3.26. Diagnostic Quality Needs Engineering Metrics

Commercial quality cannot be measured by the number of diagnostic codes.

Kontrakt should track whether the primary location points near the true source, how often one root issue causes
cascades, how often a machine-applicable fix actually recompiles, whether clean and incremental diagnostics agree,
whether parallel execution changes order, and how much time and memory diagnostic production adds to compilation.

These metrics belong to compiler engineering. They are not Contract coordinates. They give the project a way to improve
diagnostic quality without turning subjective message wording into semantic law.

### 3.27. Machine-Readable Diagnostic Schema Has Its Own Compatibility Boundary

Once CI systems and IDEs consume structured compiler diagnostics, the schema itself becomes a tool protocol. It still is
not Contract meaning, but arbitrary field changes can break external compiler tooling.

Kontrakt should therefore version the machine-readable diagnostic schema separately from Contract Version. Stable
diagnostic codes can survive a schema revision, while new optional fields can be introduced without changing the issue
being reported. A breaking representation change requires a compiler-protocol compatibility decision rather than a new
user Contract definition.

The schema should prefer typed fields over opaque extension strings for information that tooling is expected to consume.
Backend-specific attachments can use explicitly namespaced extensions so a JVM reproducer does not force JVM concepts
into the common compiler diagnostic core.

### 3.28. Compiler Event Streams Are Not Diagnostic Results

A long compilation may emit progress events that say which phase started, which artifact completed, or which worker is
active. Build systems also need invocation lifecycle events so remote or distributed tooling can understand whether a
compilation finished normally.

Those events are not compiler diagnostic authority. A crash may leave an announced phase without a matching completion
event. An upstream error may prevent an expected artifact from ever being attempted. The final compilation result and
structured diagnostics must remain authoritative even when the progress stream is incomplete.

This distinction prevents the compiler driver from inventing semantic states for work that was not reached. Progress and
execution history can explain absence without turning absence into a new Contract or compiler verdict.

### 3.29. Long-Lived Compiler Processes Need Diagnostic Session Boundaries

A daemon or language server can perform thousands of compilations or partial analyses in one process. Diagnostic state
must therefore belong to an explicit compilation or analysis revision rather than to process lifetime.

A new session cannot inherit an old error merely because the same diagnostic engine object remains alive. Crash
reproducer material, performance traces, and retained diagnostic caches may outlive one request, but each product needs
an explicit session or revision relation.

This is the compiler-side analogue of Retention separation: physical longevity of a daemon object does not define the
availability or validity of a diagnostic product.

### 3.30. Compiler Diagnostic Work Is Lazy, Bounded, and Selectively Enriched

A commercial compiler cannot pay the maximum diagnostic cost on every successful query or every rejected source form.
Kontrakt therefore keeps the persistent diagnostic core small and treats expensive explanation as material that can be
produced from that core when needed.

The cheap path records compact identities for the diagnostic definition, semantic subject, source provenance, causal
root, and dependencies that are already available from compiler analysis. Repeated Version, Policy World, source-file,
or canonical-definition context should be referenced through interned or frozen material rather than copied into every
occurrence. Human sentences, large source excerpts, complete IR histories, solver proofs, and stack traces do not belong
in hot semantic objects merely because a later renderer might want them.

When a diagnostic is actually published, the engine can enrich it according to the consumer and selected mode. A normal
user build performs the semantic projection needed for a precise actionable explanation. An explain build can spend more
time reducing a conflict set or reporting optimization blockers. A compiler-engineering run can request pass-local IR,
query traces, cache decisions, or a reproducer. Work that is not requested and is not needed to satisfy a correctness
obligation should not be materialized.

The compiler may also stop enrichment when additional detail no longer improves the explanation. A globally minimal
conflict core is unnecessary if a deterministic small core already points to the two declarations the user must change.
A full before-and-after IR dump is unnecessary if a compact transformation record answers why an optimization was
missed. This is diagnostic profitability engineering: after correctness and provenance are secured, the compiler spends
additional work only where the extra diagnostic value justifies the cost.

Lazy enrichment must not make diagnostics nondeterministic. The same requested diagnostic mode over the same
authoritative inputs produces the same semantic diagnostic product even if caches or worker schedules change the
physical work needed to obtain it.

---

## 4. Compiler Pipeline Integration

### 4.1. Diagnostic Architecture Follows the Compiler Stages

Kontrakt's planned compiler already separates frontend acquisition, resolution, canonical material, lower IR,
verification, optimization, backend lowering, and artifact emission. Diagnostic infrastructure must mirror those stages
without becoming another semantic pipeline.

Each stage can produce tool diagnostics about its own work. Each transformation also carries provenance needed by later
stages. Contract Diagnostic Evidence definitions flow through semantic lowering like other Contract material and are
realized only when the backend reaches generated-machine construction.

```text
source
    ↓
parse / acquire
    ↓
resolve
    ↓
semantic Contract IR
    ↓
canonical Contract material
    ↓
verifier / analyses
    ↓
optimization and lowering
    ↓
backend-neutral lowered machine IR
    ↓
JVM IR / artifacts

compiler diagnostic products
    are generated beside these stages

Contract Diagnostic Evidence
    is lowered through these stages as machine meaning
```

The visual parallel must not be mistaken for one shared authority.

### 4.2. Frontend Diagnostics Preserve User-Written Context

Lexer and parser diagnostics operate while source syntax is still authoritative evidence for what the user wrote.

Recovery should create explicit poisoned or missing syntax states so later stages know the source is incomplete. The
parser may continue to report independent issues, but it must not fabricate semantic declarations just to keep the tree
well-typed.

Resolution diagnostics then add semantic information: ambiguous name, missing authority, duplicate declaration,
forbidden composition, incompatible selector, or unresolved Contract Version. Their primary location should still point
to user source, while related locations show the declarations that caused the conflict.

### 4.3. Canonicalization Erases Frontend Authority but Retains Origin Handles

Once Contract meaning has been canonicalized, host syntax no longer owns the semantics.

The compiler can still keep compact source-origin handles as diagnostic metadata. Those handles allow a canonical
verifier error to return to the declarations that produced the material. They must not affect canonical equality or
Contract identity.

This separation is required for caching. Two source forms that lower to the same canonical Contract can share semantic
material while retaining different source presentations for diagnostics.

### 4.4. Verifier Diagnostics Operate on Canonical Authority

The verifier should diagnose against canonical Contract subjects, not re-read the frontend to guess what was intended.

When a contradiction exists, the verifier identifies the exact canonical obligations that conflict. Source provenance
then projects those authorities back to user declarations.

This ordering matters. If the diagnostic engine starts from source text patterns, it can report a plausible conflict
that is not the one the canonical machine actually rejected.

### 4.5. Diagnostic Evidence Definitions Are Verified Like Other Contract Material

The compiler must reject an evidence definition whose source does not exist, whose selected material is not diagnosable,
whose coordinate sorts are inconsistent, or whose reference becomes ambiguous after Version and Policy resolution.

Retention must resolve to an evidence definition that can actually be retained under the chosen lifecycle semantics.
Publication compatibility must be checked before evidence can be exposed outward.

These checks are ordinary compiler verification of Contract material. They are not runtime validation hidden in a
logger.

### 4.6. Analysis Infrastructure Serves Both Optimization and Diagnostic Explanation

A compiler should not recompute expensive semantic relations independently for errors, optimization, and explanation.

If dominance-like dependency, Contract reachability, World selection, capability resolution, or conflict analysis is
already available as an analysis result, the diagnostic engine can consume that result through explicit dependencies.
The analysis remains owned by its subsystem; the diagnostic product is one consumer.

This architecture also makes invalidation honest. When a transformation invalidates the analysis, diagnostics that
depend on it are invalidated through the same graph rather than silently reusing an old explanation.

### 4.7. Optimization Records Are Bounded Side Products

A transform can optionally produce a compact structured decision record containing the opportunity identity, the
analyses relevant to legality, the cost decision when applicable, and the resulting transformation identity.

The record is not required for every ordinary build. It can be enabled selectively for explain modes, regression
tracking, or profile-guided compiler development.

The pass manager should not retain full object graphs simply to support future explanations. Stable IDs and
deterministic IR dumps provide a much cheaper long-term seam.

### 4.8. Diagnostic Provenance Participates in Analysis Invalidation

When a pass changes source correspondence or authority mapping, it invalidates the relevant provenance analysis even if
the semantic transformation itself is valid.

A later pass that needs a source diagnostic either consumes preserved provenance or requests a recomputed mapping. It
cannot guess a source location from a nearby transformed node.

This rule should be encoded in pass interfaces from V1 because adding it after many transformations exist would require
a compiler-wide retrofit.

### 4.9. Backend Capability Diagnostics Need Exact Refusal Reasons

A backend may be unable to realize a Contract for reasons that are neither frontend errors nor user-system Failures.

The compiler should report the exact required capability and the target fact that makes it unavailable. For example, a
retention guarantee may require persistence the current JVM deployment target does not promise. A resource guarantee may
exceed the backend's controllable region. A generated-output feature may require a classfile or runtime capability not
present in the selected target.

The diagnostic must attribute the refusal to the Contract requirement and backend capability boundary rather than emit a
generic `unsupported` message.

### 4.10. Deterministic Recompilation Is a Diagnostic Capability

Kontrakt's determinism requirements can make later investigation substantially stronger.

Given the same authoritative inputs, compiler version, target capability snapshot, and explicit configuration, the
compiler should be able to reproduce the same semantic and generated results. This lets an engineer rerun a failure with
extra diagnostic tracing instead of having to retain every internal compiler event from the original build.

V2 incremental and parallel compilation must preserve this property at externally observable compiler boundaries. Cache
state may change physical work, not diagnostic meaning.

### 4.11. Diagnostic Planning Separates Guarantee from Cost

Diagnostic planning follows the same separation that an optimizer uses between legality and profitability. The compiler
first determines what evidence or provenance must be preserved for correctness. Only after that boundary is satisfied
may it decide whether additional diagnostic acquisition is worth its compilation or runtime cost.

```text
semantic occurrence
    ↓
required diagnostic guarantee
    ↓
minimal evidence / provenance plan
    ↓
optional enrichment requested?
    ├─ no -> structured occurrence
    ↓
cost and capability decision
    ├─ skip unavailable or unjustified enrichment
    └─ acquire selected rich material
    ↓
retention / presentation
```

The evidence realization plan generated by lowering therefore does not need to allocate the richest representation in
advance. It can encode the guaranteed capture operations and compact correlation points while leaving optional backend
attachments behind conditional or on-demand paths. Compiler diagnostics use the same principle with source and semantic
references: preserve enough to explain correctly, then materialize expensive human or engineering detail when a consumer
actually requests it.

This separation keeps diagnostic optimization subordinate to correctness. A cost model can refuse an optional full dump.
It cannot decide that required evidence is too expensive after the compiler has already accepted the Contract. If the
backend cannot realize the guaranteed diagnostic plan within supported resources, compilation fails at the capability
boundary instead of silently degrading the obligation.

---

## 5. V1 Compiler Architecture

### 5.1. V1 Must Build the Real Diagnostic Skeleton

V1 may support fewer Diagnostic Evidence sources and simpler retention boundaries than later versions. It must not use a
throwaway diagnostic architecture.

The frontend needs explicit evidence declarations that resolve to exact sources. Canonical material must represent the
definition independently from runtime occurrence. At the compiler level, V1 needs a SourceManager, structured diagnostic
records, stable diagnostic codes, deterministic merge, machine-readable output, and a renderer built on the same
records. Pass and query APIs must return or register structured diagnostic products through explicit context rather than
write irreversible global strings.

### 5.2. V1 Source Provenance Is Non-Negotiable

The first compiler release already performs multiple lowering stages. Postponing provenance until an IDE is built would
make every intermediate representation and pass API harder to repair later.

V1 should attach compact origin handles to semantic and lower IR where diagnostics can still arise. These handles
resolve through a centralized provenance service rather than storing copied path/line strings in every node.

The representation can be conservative and recompute mappings more often than V2. The semantic boundary between origin
metadata and Contract identity must already be correct.

### 5.3. V1 Diagnostic Engine Supports Structured Relations

The baseline engine should support one primary source anchor and related anchors, stable code, compiler phase, semantic
subject, severity, structured message arguments, and deterministic notes.

Help and fix data can be added gradually, but the record format should not force them into one text blob. Machine output
must expose stable structure suitable for CI and future IDE use.

Diagnostic causes should be representable so parser and verifier cascades do not require string heuristics later.

### 5.4. V1 Query APIs Leave Incremental Seams

V1 does not need the full persistent red/green engine to gain value from query discipline.

Compiler computations should have explicit inputs and immutable results where practical. Diagnostic products should be
associated with the query or phase that established them. Source revision is an explicit input to source presentation.

The first implementation may invalidate more than necessary. It must not hide semantic dependencies or diagnostic
publication in mutable singletons that V2 cannot observe.

### 5.5. V1 Parallel Work Uses Deterministic Publication

Independent compiler work can run in parallel, but it must not write directly to user-visible output in completion
order.

Each work item produces diagnostics with deterministic semantic keys. The driver merges them after the appropriate
boundary. The same inputs compiled with one or many workers must yield equivalent diagnostic sets.

This rule also applies to generated verification and PBT results where the compiler owns deterministic merge.

### 5.6. V1 Optimizer Emits Explainable Decisions on Demand

V1 optimization does not need a full profile-guided profitability framework, but each nontrivial transform should have a
stable pass identity and a clear preservation contract.

When explain mode is enabled, the compiler should be able to record whether the transformation was applied, blocked by
legality, or declined by a current heuristic. Diagnostic preservation blockers deserve an explicit reason because they
can otherwise look like optimizer weakness.

This is the seed for later optimization remarks rather than a separate debug print system.

### 5.7. V1 Verifier Can Produce Conflict Sets

For composition failures, V1 should preserve the exact canonical obligations that participated in the rejection.

The first conflict set need not be globally minimal. It should be deterministic and small enough that a renderer can
show which declarations must be examined together.

If a solver is used internally, proof checking and minimal-core reduction may remain later work. The authoritative
verifier result must not depend on whether rich explanation was enabled.

### 5.8. V1 Internal Compiler Error Support Is Real

V1 should capture a structured internal-error record and a reproducer manifest.

The manifest includes normalized compiler inputs and the last successfully verified phase. Deterministic IR dumps at
selected boundaries, `--verify-each`, pass timing, pass statistics, and before/after dumps provide the manual isolation
path.

Automatic reduction can wait. A user should not be asked to reconstruct which hidden compiler mode produced an internal
error from an unstructured stack trace.

### 5.9. V1 Tests Establish Diagnostic Invariants

V1 release gates should compare clean and repeated builds, worker counts, source-order perturbations, and relevant
optimization settings.

Diagnostic golden tests cover human rendering, while structured tests cover codes and provenance. Pass regression tests
check that source attribution survives transformation. Compiler fuzzing includes malformed input and transformation
paths. Runtime tests verify that required evidence is not dropped by the backend under supported bounds.

These tests are part of the commercial compiler foundation rather than polish to be added after feature completion.

### 5.10. V1 Stores Compact Diagnostic Relations and Renders Rich Contract Explanations

V1 should not encode the full terminal explanation in every parser node, canonical definition, or lower-IR object. The
compiler stores compact references to the semantic subject, source anchors, diagnostic definition, and causal material
that already exist in the compilation. Repeated context is interned where practical.

The user renderer resolves those references back to the declared Contract vocabulary. It names the Contract that was
written, states the requirement that applies, contrasts it with the material the compiler established, and points to the
source declarations that the user can inspect. Compiler implementation names remain available only in explicit
engineering modes or internal-error reports where they are actually useful.

This lets V1 deliver rich diagnostics without making rich strings or large evidence graphs part of every compiler hot
path. It also gives V2 a stable structured base for localization, IDE projection, persistent caching, and richer
explanation search.

### 5.11. V1 Exposes Diagnostic Depth without Making It Semantic Ambiguity

V1 should expose an explicit diagnostic-depth control for compiler investigation and generated-system operational
diagnostics. The exact names can be finalized with the frontend, but the modes must have defined behavior rather than
mean "print more stuff."

The normal mode favors precise Contract-language explanations and low overhead. A deeper mode may ask the compiler for
conflict reduction, optimizer decisions, source-to-IR provenance, or reproducer material. Required Contract Evidence
remains outside these optional depth controls.

The first implementation can support only a small number of profiles.

---

## 6. V2 Extension Path

### 6.1. Red/Green Incremental Diagnostics

V2 can make the dependency distinctions from V1 operational.

Semantic query fingerprints, source-provenance fingerprints, and presentation products may be reused independently when
their exact inputs remain green. Early cutoff can stop invalidation when a recomputed semantic result is identical even
though the source input changed.

The diagnostic engine must still publish against the current source revision. A green semantic result with a moved
source declaration receives a refreshed source projection before it reaches an IDE.

### 6.2. Persistent Diagnostic Caching

Once schema and dependency rules are stable, V2 may cache structured diagnostic products across compiler sessions.

A cache entry needs compiler schema version, semantic dependencies, target/capability dependencies, and provenance
requirements. Human-rendered text should not be the cache key.

Remote caches can reuse the same products only if source identity and privacy rules permit it. A diagnostic containing
local absolute paths or sensitive excerpts must not be uploaded accidentally because the semantic cache is shareable.

### 6.3. Richer Constraint Explanations

V2 can add stronger conflict minimization, proof checking, counterexample construction, and explanation search around
complex Contract composition.

The compiler may choose an explanation based on usefulness and cost after the authoritative verifier result is known. A
small unsatisfiable core, a counterexample witness, and a machine-checkable proof are different products and need not
all be produced for every failure.

This makes formal verification machinery useful without exposing raw solver internals as the public language of Kontrakt
diagnostics.

### 6.4. Translation Validation and Diagnostic Preservation

Selected optimization passes may gain translation-validation checks that compare pre- and post-transform meaning for the
specific input.

The same framework can validate required diagnostic observability. If a transform fuses or eliminates a generated gate,
the checker can verify that each externally required evidence occurrence remains derivable from the lowered result.

V2 can run these checks continuously in compiler QA and selectively in debug builds without paying full cost in every
normal compilation.

### 6.5. Profile-Guided Compiler Remarks

When Kontrakt begins using target cost models or profile information, optimization diagnostics can explain the evidence
behind profitability decisions.

A remark may identify that an Interaction is hot, that a generated branch is cold, or that allocation pressure changed a
fusion decision. Profile data is compiler optimization input, not Contract truth. Recompiling with another profile may
change the generated artifact without changing Contract meaning.

The diagnostic schema should distinguish static legality from profile-dependent profitability so users know which part
of the decision is stable.

### 6.6. Automated Reproducer Reduction

V2 can add compiler-aware reduction that understands `.kontrakt`, canonical IR, pass boundaries, and dependency graphs.

A reducer should preserve the exact diagnostic or internal compiler error being investigated rather than merely preserve
non-zero exit status. Stable diagnostic codes and semantic subjects from V1 provide the oracle needed for that
reduction.

Pass bisection can then find the first transformation that changes the relevant property, while deterministic replay
keeps the investigation reproducible.

### 6.7. Diagnostic Cost Models

Later compiler versions may make diagnostic acquisition itself cost-aware.

An ordinary build can keep stable source and semantic anchors. An explain build can spend more time minimizing conflict
cores or retaining pass decisions. A compiler engineering run can collect detailed allocation and transformation traces.
The user chooses the depth appropriate to the question without changing Contract validity.

## 7. Verification and Quality Requirements

### 7.1. Clean and Incremental Diagnostic Equivalence

For the same current source state, a clean compilation and an incremental compilation must agree on compiler diagnostic
meaning.

The physical amount of work may differ. Cache-hit remarks may differ when explicitly requested. Stable error codes,
semantic subjects, source anchors after current projection, and Contract validity cannot differ merely because cached
queries existed.

### 7.2. Single-Worker and Multi-Worker Equivalence

Parallel execution may reorder physical work but not diagnostic results.

Tests should perturb worker counts and scheduling while comparing the final deterministic diagnostic set. This includes
causal attachment and deduplication, not only sorted message text.

### 7.3. Diagnostic-Provenance Metamorphic Tests

Source edits that preserve Contract meaning are useful adversarial tests.

Whitespace, comments, unrelated declaration order, path relocation, and other presentation changes can verify that
semantic identity remains stable while current source anchors update correctly. Conversely, semantic changes at a stable
text position verify that cached diagnostics do not survive merely because the span is unchanged.

### 7.4. Invalid-Program Diagnostic Fuzzing

Parser and semantic fuzzing should evaluate diagnostic quality as well as crash resistance.

Generated invalid inputs can test whether the compiler reports the intended root condition, whether recovery produces
unbounded cascades, and whether different invalid constructions accidentally collapse to an unrelated generic message.
The diagnostic code and semantic subject provide a stronger oracle than comparing human strings.

### 7.5. Fix Validation

Machine-applicable fixes must be applied in tests and compiled again.

The test does not merely assert that the original error disappears. It checks that the edit is syntactically valid, does
not introduce placeholder text, and satisfies the diagnostic's stated applicability conditions.

### 7.6. Reproducer Validation

Internal-error tests should verify that a generated reproducer can invoke the same compiler phase with normalized
inputs.

The reproducer manifest must not depend on temporary object addresses or worker-local filenames. Where privacy-sensitive
source cannot be bundled automatically, the manifest should still preserve dependency identities and state clearly what
material is missing.

### 7.7. Performance Regression Tests

Diagnostic improvements are not free if they multiply compile latency or runtime allocation.

Compiler benchmarks should measure diagnostic-heavy invalid builds separately from successful builds. Source mapping,
conflict explanation, rendering, and machine-output serialization need their own profiles. This makes diagnostic
performance an engineered property rather than an anecdote.

---

## 8. Authority Boundary

Kontrakt compiler diagnostics are tool products. They may refer to Contract authorities because the compiler understands
them, but the reference does not make a compiler diagnostic part of the user's Contract Machine.

Human-readable messages, IDE layouts, terminal colors, and machine interchange are presentations over structured
compiler diagnostic material. The text may change without changing the stable diagnostic identity or semantic subject.

Compiler self-observability remains adjacent. Pass timing, allocation profiling, JVM telemetry, and compiler-process
traces can explain the physical behavior of the compiler, but they are not the same product as a source diagnostic
unless an explicit engineering diagnostic relates them.

## 9. Open Decisions

### 9.1. Final Compiler Diagnostic Record Schema

The record remains structured before rendering and must support stable identity, source relations, semantic subjects,
causality, and future tooling without turning human text into protocol identity.

### 9.2. Exact SourceManager and Provenance Representation

V1 needs centralized source provenance and compact origin handles. The exact storage representation remains open.

### 9.3. Diagnostic Cause Graph Representation

Causal attachment must distinguish a root condition from diagnostics that exist only because earlier source or semantic
state became invalid.

### 9.4. Machine-Readable Schema and Compatibility Policy

Machine output has its own compatibility boundary and cannot rely on parsing terminal text.

### 9.5. IDE Publication Protocol

IDE publication must respect source revision and cannot publish a structurally valid diagnostic against stale text.

### 9.6. Exact Diagnostic-Depth Profiles

A profile must name useful investigation intent rather than mean only "print more." Required semantic correctness
remains identical at every profile.

### 9.7. Solver Proof and Conflict-Core Strategy

The authoritative verifier result does not depend on rich explanation. Proof production, globally minimal conflict
cores, counterexamples, and proof checking may be added independently.

### 9.8. Persistent and Remote Diagnostic Cache Policy

Persistent reuse requires stable schema and dependency rules. Human-rendered text is not the semantic cache key, and
privacy-sensitive source presentation cannot be uploaded merely because a semantic result is shareable.

### 9.9. Automated Reproducer Reduction

A future reducer must preserve the exact diagnostic identity or internal compiler failure rather than only a non-zero
compiler exit.

## 10. Consequences

### Positive

The compiler gets one structured diagnostic architecture for parser, resolver, verifier, optimizer, backend, IDE, CI,
and internal-error use instead of separate string systems.

User-facing diagnostics can remain rich while compiler hot paths store compact relations. Semantic projection lets
ordinary diagnostics speak in the vocabulary of the declared Contract Machine while engineering modes retain access to
compiler internals.

The V1 architecture remains open to red/green incremental reuse, parallel compilation, persistent caching, optimizer
remarks, and automated reduction without replacing a string logger later.

### Negative

Source provenance, structured records, deterministic merge, invalidation rules, and diagnostic regression tests have to
be designed early.

Transforms and queries carry additional preservation obligations even when semantic IR remains otherwise valid.

### Neutral

This ADR does not define the user's Diagnostic Evidence Contract and does not decide how generated production systems
capture JVM, operating-system, hardware, or external operational evidence.