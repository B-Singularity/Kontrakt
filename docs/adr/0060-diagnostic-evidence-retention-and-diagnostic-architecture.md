# ADR-0060: Diagnostic Evidence, Retention, and Three-Layer Diagnostic Architecture

## Status

Proposed

## Date

2026-08-23

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/constitution/canonical-ir-stage-and-lowering-protocol.md`
- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/todo/kontrakt-frontend-and-contract-refactor-plan.md`
- `docs/todo/kontrakt-verifier-implementation-plan.md`
- ADR-0059: Output Presentation Contract, Explicit Outward Result Shape, and Machine Exit Boundary
- ADR-0058: Publication Contract, Explicit Outward Exposure Authority, and Core Exit Boundary
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency
- ADR-0054: Policy Contract, Explicit Operating Modes, Self-Contained Contract Worlds, and Interface Binding Boundary
- ADR-0053: Version Contract, Sovereign Contract Revision History, and Realization Boundary
- ADR-0052: Capacity Contract, Explicit Safe Operating Memory Limits, and Realization Boundary
- ADR-0051: Budget Contract, Explicit Allowance, Contract-Scoped Resource Limits, and Backend Realization Boundary
- ADR-0050: State, State Transition, Explicit State Machine Manifest, and the State-Machine Axis
- ADR-0049: Flow Contract Processing — Fact, Invariant, Publication, and Output Presentation
- ADR-0048: Flow Contract Processing — Boundary Refinement and Core Entry
- ADR-0047: One-Dimensional Contract Presentations, Pipeline-Slot Selection, and Backend Realization Boundary
- ADR-0046: IDL-First Interface Contract Frontend and Retained Generated Host Interface and Realization Port Boundary
- ADR-0045: Contract Pipeline Package Architecture, Explicit State-Machine Axis, and Compiler Realization Mirror

---

## 1. Context

Kontrakt already requires the Contract Machine to state its authoritative results explicitly. A valid result cannot
depend on a later observer reconstructing meaning from a stack trace, a log line, an exception hierarchy, or a generated
JVM object. ADR-0057 applies that rule to Failure and deliberately removes diagnostic material from Failure identity.
Failure states what required machine meaning was not satisfied. Diagnostic material may help explain that result, but it
must not become a second authority that decides what the Failure meant.

This separation creates the next problem. Once a machine can state a result correctly, a serious engineering system must
still be able to answer why the result was established, which source material was relevant, how the explanation relates
to the exact authority that judged it, and whether the supporting material is still available. If those answers are left
to arbitrary logging, the Contract remains semantically explicit while investigation returns to implementation
reconstruction.

The problem is larger than Failure. A successful judgment may need explanation. A verifier can reject a composition
before user code exists. An optimizer can legally choose not to perform a transformation. A backend can refuse a target
because the target cannot realize a required guarantee. A generated system can encounter a realization fault after
Contract meaning has already been established. These cases do not share one result kind, but they all create a need to
connect an observed or derived explanation to the exact source of a decision without giving that explanation control
over the decision.

Kontrakt also has to solve the problem at three different authorities.

The first authority is the user's Contract. The user may need to declare which diagnostic material is part of the
machine's accountable behavior and how long that material must remain available. That declaration must lower into
canonical Contract material and remain independent of the JVM representation used to realize it.

The second authority is the Kontrakt compiler itself. Parser errors, resolution failures, inconsistent Contract worlds,
verifier rejections, missed optimizations, backend capability failures, incremental-cache invalidation, and internal
compiler errors are tool diagnostics. They are not user Contract Diagnostic Evidence. A production compiler still needs
a coherent architecture for them because a compiler that can enforce strong contracts but cannot explain its own
decisions is not a high-quality compiler.

The third authority is the generated user system. The runtime realization must capture any Diagnostic Evidence that the
Contract actually requires while still allowing richer backend-specific investigation through JVM, operating-system,
hardware, profiler, tracing, crash-dump, or external observability facilities. Those facilities are valuable precisely
because they can expose physical behavior that a backend-neutral Contract cannot know in advance. Their usefulness does
not make them Contract authority.

```text
User Contract
    declares diagnostic obligation
        ↓
Canonical Contract material
        ↓
Generated realization
    establishes required Diagnostic Evidence
        ↓
Retention / authorized use

Kontrakt source and compiler state
        ↓
Compiler diagnostic analysis
        ↓
Structured compiler diagnostic
        ↓
CLI / IDE / CI / explanation / reproducer

Generated realization
        ↓
Optional operational observation
        ↓
JVM / OS / hardware / external diagnostic material
```

These three paths meet inside the compiler but must not be merged into one diagnostic object model with one authority.
The Contract path governs machine meaning. Compiler diagnostics govern the tool that constructs the machine. Operational
diagnostics describe physical realization behavior. A shared infrastructure may transport or render all three, but
shared storage does not make their semantics identical.

The architecture must also be designed with the same seriousness as an optimizer. A production optimizer is not a bag of
rewrites. It combines analyses, legality checks, cost models, transformations, preservation rules, invalidation, and
feedback. Diagnostic quality has the same structural property. A production diagnostic subsystem needs reliable source
and semantic provenance before it can recognize a useful diagnostic condition. It needs a structured result before it
can render text. Transformations have to preserve or invalidate diagnostic relations. Incremental compilation must know
when a semantic result is reusable but a source location is stale. Parallel compilation must not allow worker completion
order to change diagnostic output. Expensive runtime capture must be chosen according to a cost boundary instead of
being inserted indiscriminately into every path.

```text
source / semantic / IR state
        ↓
analysis and provenance
        ↓
diagnostic condition recognition
        ↓
evidence or explanation selection
        ↓
correctness / completeness decision
        ↓
structured diagnostic product
        ↓
retention or presentation
```

This architecture is also consistent with high-reliability engineering. Mature fault-management systems distinguish a
fault indication from the material captured around the fault. They distinguish a time-tagged event from continuous
telemetry and from a larger diagnostic artifact stored for later investigation. Hardware error formats mark fields as
valid or unavailable instead of inventing values. Some records explicitly state when overflow means information was
lost. Safety systems do not assume that self-diagnostics are infallible; the diagnostic mechanism itself is tested and
verified.

The same engineering systems also expose the cost problem. Continuous high-detail observation can disturb the system it
is supposed to explain. A bounded circular history may be kept cheaply while a deeper snapshot is triggered only around
an anomaly. Production compilers and runtimes similarly keep cheap provenance continuously and enable expensive dumps,
traces, or reproducer material only when needed. Diagnostic quality therefore does not mean collecting everything. It
means preserving the right relationship between authoritative meaning and trustworthy explanation while making the cost
of deeper observation explicit.

ADR-0060 intentionally keeps the whole strategy in one document before the design is split. Diagnostic Evidence,
Retention, compiler diagnostics, and generated-system diagnostics are likely to become two or three documents once their
final ownership boundaries are clear. The purpose of this draft is to retain the detailed constraints while those
boundaries are still being compared. A later split must reduce document scope without deleting the architecture that
makes the parts fit together.

---

## 2. Problem

A weak diagnostic design can be semantically correct and still make the system practically unmaintainable.

One common failure is post-hoc reconstruction. The machine states only that something failed, while the reason is hidden
in implementation artifacts. An engineer must correlate a message string with a call stack, guess which Contract
judgment was executing, find the applicable Version and Policy World, and then reconstruct the values used by the
judgment. This is exactly the authority leakage that explicit Failure is meant to remove. A named Failure does not solve
it if all useful investigation still depends on accidental runtime structure.

The opposite failure is to record every available detail. That sounds safer until the diagnostic system becomes one of
the most expensive parts of the machine. Every value copy extends lifetime. Every always-on trace consumes memory,
bandwidth, storage, and cache. Sensitive input can be retained far beyond the purpose that justified its original use.
Large traces may alter scheduling enough to hide a concurrency problem. A diagnostic subsystem that can stall a producer
in order to avoid losing logs may have different liveness behavior from one that drops evidence under load. Those are
real engineering choices, not formatting details.

Compiler architecture creates a further problem because source meaning and physical compiler state diverge as the
pipeline advances. A parser sees source tokens. Resolution replaces names with identities. Canonicalization removes
frontend representation. optimization may fuse or eliminate checks. JVM lowering introduces backend structures that did
not exist in the Contract. A diagnostic that points to whatever object is physically present at the moment of emission
cannot remain reliable across these transformations.

Stale diagnostic information is especially dangerous. A missing source location is visibly incomplete. A wrong source
location looks authoritative. The same applies to a stale explanation after an optimization pass or incremental cache
reuse. If a compiler reuses a semantic result after an edit but reuses an obsolete source projection with it, the
compiler can report the right error on the wrong text. Diagnostic provenance must therefore participate in compiler
preservation and invalidation rules rather than riding along as optional metadata.

Incremental compilation makes that distinction unavoidable. Consider a source edit that changes whitespace and moves a
declaration without changing its Contract meaning. The canonical semantic result may remain green. A source span tied to
byte offsets cannot. Invalidating the whole semantic graph because the span moved wastes the value of incremental
compilation, while keeping the old span produces a stale diagnostic. Semantic dependency and diagnostic-provenance
dependency need separate tracking.

Parallel compilation introduces another axis. Independent queries may finish in a different order on each run. If each
worker writes directly to a shared diagnostic stream, scheduling becomes observable through error order. That violates
Kontrakt's determinism requirements and makes golden tests, CI comparison, IDE refresh, and reproducer analysis harder.
Diagnostics therefore have to be assembled as products and published through a deterministic merge.

Optimization raises a similar preservation problem. Kontrakt intends to specialize constant World and Version choices,
fuse small generated gates, eliminate dead generated paths, reduce allocation, compact tables, and later expand into
richer whole-machine and profile-guided optimization. A transformation is not valid merely because the generated result
has the same acceptance behavior. If the Contract promises Diagnostic Evidence for a rejected judgment, an optimizer
that erases the only material needed to establish that evidence has changed the observable Contract. Diagnostic
preservation is therefore one part of transformation legality.

The optimizer also needs diagnostics about itself. A commercial compiler must be able to answer why a transformation was
applied, why it was missed, which analysis or target capability blocked it, which cost assumption made it unprofitable,
and which pass first changed the relevant IR. Dumping every internal decision by default would be too expensive and too
unstable. The diagnostic architecture needs enough structured decision material to support explanation without making
optimizer implementation details part of the language or Contract.

Constraint verification creates another form of diagnostic difficulty. A verifier can know that a combination of
Contracts is impossible without having a useful human explanation. A raw solver proof may be correct but unreadable. A
small conflicting set of declarations can be far more useful, while a minimal conflicting set may cost additional solver
work. The compiler must distinguish the authoritative verification result from the optional work spent producing a
smaller or richer explanation.

Compiler failure itself must also remain diagnosable. An internal compiler error can occur while the current IR is
partially transformed or corrupted. A useful reproducer should therefore not blindly trust the object graph at the crash
site. The compiler needs identities for the compilation session, the last verified stage, the active pass or query, the
normalized inputs, the target and capability state, and a deterministic representation from a known-valid boundary. Deep
crash material can then supplement that stable core.

The generated user system faces a different failure mode. Contract-required Diagnostic Evidence cannot be treated like a
best-effort trace. If the Contract says a particular observation is retained, sampling cannot silently remove it. At the
same time, JVM stacks, JFR events, operating-system traces, hardware RAS records, sanitizers, and profiler data may be
too expensive or too target-specific to promise universally. The runtime architecture must preserve both a small
guaranteed path and a richer optional path instead of forcing one guarantee level onto all diagnostics.

Retention makes this distinction persistent. Evidence can be correctly established and later expire. Expiry does not
rewrite the result that the evidence explained. A diagnostic file can also survive longer than promised because an
implementation has not reclaimed it yet. Accidental survival does not create a stronger Contract. Availability needs its
own authority.

The design therefore has to answer four different questions without collapsing them:

```text
What machine meaning was established?
    -> existing Contract / State-Machine / realization authority

What declared material may account for that occurrence?
    -> Diagnostic Evidence

What availability of that evidence is guaranteed?
    -> Retention

What additional information may a tool or backend collect for investigation?
    -> compiler or operational diagnostic architecture
```

The rest of this ADR defines how these questions fit together while keeping their authority separate.

---

## 3. Decision Drivers

Diagnostic Evidence must explain an already-established machine result rather than create a competing result. The source
that judged the original obligation remains authoritative even if the diagnostic subsystem later fails, loses data, or
produces no human-readable message.

An evidence occurrence must have exact provenance. Material is not useful as Contract Evidence merely because it is near
the failure in time or appears in the same stack. The compiler must be able to resolve the evidence definition to an
exact judgment source and the runtime occurrence to the exact result occurrence it explains.

Contract-required evidence must be closed. If the definition requires three coordinates, an occurrence with only two
cannot silently become a weaker valid occurrence. A backend may separately retain a partial dump, but partial
operational material must not masquerade as complete Contract Evidence.

Material that describes a value at the judgment boundary must be captured from that boundary. Reading mutable state
later and presenting it as the earlier value is reconstruction, not preservation. Later analysis may still be useful,
but the system must not erase the difference between a frozen observation and a reconstructed explanation.

Diagnostic Evidence must not steal existing semantic coordinates. Failure `applicable context` remains Failure meaning
where ADR-0057 requires it. If a diagnostic uses that material, the evidence definition refers to the established
coordinate rather than defining a competing copy with a new meaning.

The Contract must remain backend-neutral. `Throwable`, JVM frame, native address, thread ID, profiler sample, ETW event,
eBPF record, PCIe error register, or spacecraft telemetry packet can be valuable realization evidence. None may become
required Contract vocabulary merely because one backend can provide it.

Useful backend evidence must nevertheless remain reachable. Portability is not an excuse to throw away low-level
observability. Generated artifacts need stable correlation points that allow Contract occurrences to be related to JVM,
operating-system, hardware, or external traces without converting those traces into Contract authority.

Compiler diagnostics must be structured before rendering. A message string is a presentation artifact. Stable codes,
source relationships, semantic subjects, causal relations, fix data, and reproducer handles need independent identities
so the compiler can support CLI, IDE, CI, machine-readable output, localization if it is ever added, and testing without
making English text a protocol.

The default explanation must speak in the user's Contract vocabulary rather than expose the vocabulary of compiler
implementation. A verifier may internally reason about canonical ordinals, graph nodes, solver clauses, lowered gates,
or pass-local analysis objects. Those objects can remain available for compiler engineering, but the ordinary diagnostic
projects the relevant result back to the declared Contract subjects and rules that the user can inspect and change.
Richness therefore means semantic precision, not an internal-state dump.

This projection is itself a correctness boundary. The renderer cannot invent a friendly story that is merely plausible.
The explanation must be derivable from structured semantic subjects, source provenance, and the exact compiler judgment
that established the diagnostic. The compiler can change wording while the relation from internal evidence to the
user-visible Contract explanation remains deterministic and testable.

Compiler diagnostic generation must participate in the same determinism rules as compilation. Worker count, scheduling,
hash-table iteration, cache hit order, object address, and wall-clock completion order cannot decide which diagnostics
exist or how independent diagnostics are ordered.

Incremental reuse must be precise. A cached semantic result cannot drag stale diagnostic source state with it, while a
source-only edit should not force recomputation of unrelated semantic meaning. Query keys and fingerprints must leave
room for that distinction from V1 even if the first implementation recomputes more than V2 eventually will.

Optimization must preserve required diagnosability. A pass may erase intermediate structure, but it must preserve the
semantic relation needed to establish Contract-required evidence and the compiler provenance needed for trustworthy
source diagnostics. If it cannot prove or validate that preservation, the transformation is not legal for that input.

Deep diagnostics have a cost. The architecture must support cheap always-available anchors and selectively acquired rich
material. Costly tracing, full dumps, detailed optimizer histories, and verifier proof objects should be available when
they justify their cost rather than silently becoming mandatory hot-path work.

The cost boundary must not weaken a required diagnostic guarantee. A Diagnostic Evidence Contract fixes the material
that must exist for the selected world and occurrence. Above that guaranteed core, the user or compiler operator may
select a deeper diagnostic profile that enables more expensive explanation, tracing, reproduction, or backend
observation. A smaller profile may reduce optional work, but it cannot change Contract validity, erase an established
Failure, or turn required evidence into sampled best-effort data.

The compiler and generated runtime should therefore optimize diagnostic work as aggressively as they optimize other
non-semantic work. They should retain compact identities, reuse existing analysis, intern repeated context, delay human
formatting, and materialize expensive evidence only when the selected diagnostic depth or an exact trigger requires it.
The user-visible explanation can remain rich because semantic compression is not the same thing as permanently retaining
every internal object that helped produce it.

The diagnostic mechanism itself cannot be assumed correct. Source mappings, evidence capture, retention, rendering,
incremental reuse, and backend correlation require independent verification and regression testing. A system that
verifies contracts but does not test the machinery that explains those verifications has only moved the trust problem.

V1 must establish the architectural seams required by a commercial compiler. V2 may improve precision, reuse, adaptive
capture, and richer explanation, but it must not require replacing a V1 string logger with a real diagnostic engine or
replacing pass-local side effects with query products after the rest of the compiler already depends on them.

---

## 4. Contract Decision — Diagnostic Evidence

### 4.1. Diagnostic Evidence Is a Contract over Explanation Material

Diagnostic Evidence is Contract meaning about the material that a machine must be able to establish as evidence for an
exact diagnosable occurrence.

It does not repeat the judgment. It states which supporting material belongs to the diagnostic account of that judgment
when the evidence obligation applies.

```text
exact machine judgment
    establishes Result or Failure
        ↓
Diagnostic Evidence definition
    identifies allowed supporting material
        ↓
Diagnostic Evidence occurrence
    freezes the material for this exact occurrence
```

The evidence occurrence is authoritative only about its own diagnostic statement. It can authoritatively state that a
declared value was the value captured from an exact source for this occurrence. It cannot reinterpret the original
judgment or replace the authority that established it.

This distinction is important. Calling evidence entirely non-authoritative would make declared Contract Evidence no
stronger than a log. Giving it authority over the source judgment would recreate the Failure problem. Diagnostic
Evidence therefore has a narrow authority: the truth of the declared diagnostic material and its relation to the exact
source occurrence.

### 4.2. Evidence Is Not Failure Meaning

ADR-0057 remains authoritative over Failure.

A Failure already states its source, failed meaning, applicable context, and boundary according to that ADR. Diagnostic
Evidence may refer to those coordinates when they are relevant to investigation. It does not move them out of Failure or
make Failure depend on a retained diagnostic record.

Consider a stock obligation that establishes `RequiredStockUnavailable`. If requested quantity is part of the Failure's
applicable context because the Failure cannot be interpreted without it, that value remains Failure meaning. If an
additional observed inventory sample is useful only to explain how the source judged the obligation, that sample may be
Diagnostic Evidence instead. The ownership is decided by whether the material is required to state the Failure itself,
not by whether an operator would like to see it.

The diagnostic layer therefore cannot be used to shrink Failure into a code plus an evidence bag. The same rule applies
to successful Results and State-Machine judgments. Material essential to their semantic meaning stays with the owning
Contract.

### 4.3. Evidence Is Not Limited to Failure

Diagnostic Evidence may be declared for an established judgment that is useful to explain even when no Failure exists.

A compiler or operator may need to know why a particular policy branch was selected, why a transition was accepted, or
which canonical values caused an invariant to pass. This does not require every judgment to emit evidence. It only means
that the Diagnostic Evidence Contract is not defined as a Failure attachment.

This avoids an artificial distinction where the machine is explainable only when it fails. It also allows verification,
audit, and controlled explanation of successful machine behavior without inventing success-flavored Failure objects.

V1 may support a narrower set of diagnosable sources than the theory permits. That limitation belongs to frontend and
backend capability, not to the definition of Diagnostic Evidence.

### 4.4. Definition and Occurrence Are Different

A Diagnostic Evidence definition is static Contract material. It identifies the diagnosable source and the material that
an occurrence must establish.

A Diagnostic Evidence occurrence belongs to one concrete machine occurrence. It contains or references the frozen values
established for that occurrence and is tied to the exact source result it explains.

```text
Diagnostic Evidence Definition
    source = exact canonical judgment authority
    material = closed declared selection

Diagnostic Evidence Occurrence
    definition identity
    source-result occurrence identity
    frozen selected material
```

The definition must not depend on runtime object identity. The occurrence must not depend on the address of an evidence
buffer or the sequence number chosen by a storage backend.

Frontend shorthand may eventually allow concise selection, but canonical lowering must resolve that shorthand to exact
sources before it becomes authority. No runtime wildcard may decide which judgment an evidence record belongs to.

### 4.5. Evidence Material Comes from Declared Sources

A Diagnostic Evidence definition may select only material that the exact source can establish or expose to diagnostic
lowering.

That material may already exist as a result coordinate, a Failure context coordinate, a canonical judgment input, or a
declared observation produced while the source judges its obligation. The evidence layer cannot call arbitrary user code
to manufacture a value after the fact. It cannot inspect a private field, walk an object graph, invoke a callback, or
infer a value from a stack frame and then claim Contract authority for it.

The owning source therefore needs a finite diagnostic surface. This does not require every internal intermediate value
to become a public Contract coordinate. It requires the compiler to know which material may legally be selected when a
Diagnostic Evidence declaration asks for it.

A future frontend may offer broad authoring conveniences. Canonical material still contains the exact source and exact
selected coordinates. The machine never performs dynamic discovery of diagnostic fields.

### 4.6. Evidence Selection Is Closed

A Diagnostic Evidence definition declares a closed set of evidence coordinates.

Runtime processing cannot append an undeclared debug field because it looks useful. A backend cannot silently add a
stack trace to the Contract occurrence. An adapter cannot inject environment variables or raw payload bytes into the
same Contract Evidence object.

This law serves two purposes. It makes the evidence guarantee finite, and it limits accidental retention of sensitive
material. Rich implementation diagnostics may carry more information in their own records, but they remain a different
plane.

The closed shape also lets the compiler reason about cost before realization. A required evidence coordinate has a known
sort and source relation. That can participate in allocation, retention, publication, and backend-capability planning
without inspecting runtime logger configuration.

### 4.7. Evidence Preserves Exact Names and Meanings

Diagnostic Evidence does not rename source coordinates or derive new factual values through arbitrary expressions.

When evidence selects an existing Contract coordinate, its canonical meaning is the source coordinate's meaning. A
renderer may display a friendly label later. A backend may encode the value differently. Neither changes the evidence
identity.

A derived explanation such as `remaining = allowance - consumed` is not automatically Diagnostic Evidence merely because
the operands are evidence. If the machine needs `remaining` as authoritative evidence, that meaning must already exist
at an owning authority that can establish it. Otherwise the subtraction belongs to an explanation or presentation tool.

This follows the same discipline as Output strict projection: a later boundary may present established meaning but may
not quietly create new factual authority.

### 4.8. Evidence Is Frozen to the Occurrence It Explains

Material that claims to describe a judgment occurrence is frozen to that occurrence.

If a value can change after the judgment, reading it later is not equivalent. The diagnostic subsystem may perform later
analysis, but it must not label a later observation as the earlier source value.

```text
judgment uses value = 4
        ↓
required evidence captures value = 4
        ↓
state later changes to value = 7

later value = 7
    cannot rewrite the evidence occurrence
```

This is the diagnostic counterpart of ADR-0057's frozen Failure context. The two freezes serve different authorities.
Failure context freezes the material needed to interpret Failure. Diagnostic Evidence freezes the declared supporting
material for the evidence occurrence.

If the same physical value is already frozen in Failure or Result material, the diagnostic definition should reference
that material rather than create a second semantic copy. A realization may physically duplicate bytes for storage, but
that duplication does not create another coordinate identity.

### 4.9. Occurrence Time and Observation Time Are Not Automatically the Same

Diagnostic systems frequently observe an event after the event occurred. Hardware may report a corrected error later. A
trace collector may timestamp receipt after the producer timestamp. A buffered runtime event may be drained by another
thread. Those times are useful, but the Contract must not silently treat one as the other.

Diagnostic Evidence therefore does not gain an implicit wall-clock coordinate. If event time is Contract meaning, it
must come from an existing declared time authority. Backend observation or collection time may be attached as
operational metadata without becoming that Contract time.

Where both are available, tooling may preserve both so investigators can reason about latency and ordering. Their
presence is a diagnostic capability, not a universal identity rule.

### 4.10. Provenance Is Part of the Evidence Relation

An evidence occurrence must retain enough canonical provenance to identify the exact source definition and source result
occurrence it explains.

The provenance is semantic, not a JVM stack. It survives backend replacement because it refers to Contract authorities,
Version-sensitive canonical definitions, selected World where applicable, and stable occurrence correlation owned by
Kontrakt rather than by a particular logging facility.

Source provenance does not imply that every diagnostic record repeats all of the source's context bytes. A compact
identity may resolve to immutable canonical material already known by the machine. The logical relation is required; the
physical encoding remains a backend decision.

This distinction allows V1 to use compact tables and V2 to use content-addressed or persistent identity without changing
what the evidence means.

### 4.11. Contract Evidence Is All-or-Nothing with Respect to Its Declared Shape

A Diagnostic Evidence occurrence is established only when its required coordinates have been established according to
the definition.

The Contract does not invent `PartialEvidence`, `TruncatedEvidence`, or `MaybeEvidence` as universal semantic states. If
the definition explicitly contains optional or absent coordinates through existing Contract absence law, that is part of
the closed shape. An unexpected capture failure is different.

This rule prevents a storage or tracing failure from silently weakening the Contract. A backend-specific dump may still
state that some frames were unavailable or some events were lost. That is useful operational evidence, but it is not a
complete occurrence of a stricter Contract Evidence definition.

### 4.12. Failure to Establish Evidence Does Not Rewrite the Source Result

A source Result or Failure remains established even if a later diagnostic obligation cannot be completed.

If the Diagnostic Evidence Contract itself has an applicable required obligation and the machine retains enough
authority to judge that obligation, inability to establish the required evidence may establish a separate Failure owned
by that diagnostic obligation. It does not replace, merge with, or retroactively invalidate the original source result.

```text
source Failure A is established
        ↓
Diagnostic Evidence obligation for A is evaluated
        ↓
evidence cannot be established
        ↓
possible Failure of the evidence obligation

Failure A remains Failure A
```

If abrupt realization loss prevents the evidence obligation from being judged at all, the machine must not invent a
secondary Failure merely to explain the missing diagnostic material. ADR-0057's rule about unknowable results still
applies.

### 4.13. Later Unreached Processing Remains Execution Evidence

ADR-0057 already rejects semantic outcomes such as `Skipped`, `Blocked`, or `NotEvaluated` for processing that was never
reached because an earlier Failure made it unreachable.

Diagnostic tooling may still explain the dependency relation. It may state that a particular source Failure prevented a
later dependent boundary from being entered, or show the last reachable machine boundary. That statement belongs to
execution evidence or an explanation graph. It does not establish a synthetic result for the unexecuted Contract.

The distinction is especially important for compiler and Whole-Machine diagnostics. A causal path can explain why a node
has no result without pretending that the node ran.

### 4.14. Explanation and Remediation Are Not Evidence

A diagnostic can contain more than evidence without giving every part the same authority.

A human explanation may summarize several evidence coordinates. A note may add context from another declaration. A help
message may suggest a change. A compiler fix may propose an edit. Those are tool products derived from the diagnostic
record.

The underlying Evidence remains the material that was actually established. A suggestion is not evidence that the
suggested edit is correct. An automatic fix therefore needs its own compiler-side applicability and validation rules.
The Contract Diagnostic Evidence model does not acquire a `hint` or `fix-it` semantic field merely because compiler UIs
need those concepts.

### 4.15. Diagnostic Evidence Is Selective, Not Universal

Kontrakt does not require every declared judgment to retain a full diagnostic record.

Some judgments may already be sufficiently explained by their Result or Failure material. Others may be so frequent that
retaining additional evidence would dominate runtime cost. A machine designer declares Diagnostic Evidence where
accountability requires more than the source result already provides.

The compiler may still generate its own source diagnostics or optional runtime instrumentation for undeclared cases.
Those facilities cannot be promoted to Contract guarantee after deployment by configuration alone.

Selective declaration keeps diagnostic cost visible and makes retained sensitive material reviewable before execution.
It also creates a clean V2 path for richer capture without turning V1 into an always-on flight recorder.

---

## 5. Contract Decision — Retention

### 5.1. Retention Governs Availability, Not Semantic Existence

Retention is separate from Diagnostic Evidence because it answers a different question.

Diagnostic Evidence states what evidence occurrence was established. Retention states the availability that the machine
must preserve for an established occurrence after that establishment.

```text
Evidence occurrence established
        ↓
Retention obligation applies
        ↓
evidence remains available within the declared boundary
```

Expiry, reclamation, or external archival behavior cannot rewrite the original evidence occurrence. Conversely, bytes
remaining in a cache or file after the guaranteed boundary do not extend the Contract.

This separates historical semantic truth from storage lifetime.

### 5.2. Retention Does Not Create Evidence

A Retention declaration cannot cause an evidence occurrence to exist when the corresponding Diagnostic Evidence was not
established.

It also cannot strengthen a partial operational trace into Contract Evidence. Retention operates only on material whose
own authority has already been established.

The compiler must therefore resolve Retention against an exact retainable source. A storage implementation cannot define
a new retention subject merely because it can write arbitrary objects.

### 5.3. Retention Guarantees Must Be Realizable Before They Are Promised

A Contract retention guarantee is stronger than best-effort logging.

If a target cannot provide the requested availability under the required boundary, the backend must reject the
realization or require a different declared Contract. It cannot silently downgrade `must remain available` to `normally
stays in a ring buffer`.

This is the same fail-closed capability boundary used elsewhere in Kontrakt. The frontend declares semantic intent. The
backend states what it can realize. Capability matching decides whether the combination is valid.

### 5.4. Retention Duration and Storage Capacity Are Different Responsibilities

Retention describes how long or through which Contract lifecycle boundary evidence must remain available. The amount of
physical storage required to honor that promise belongs to resource planning and Capacity.

A declaration such as `retain this evidence through boundary X` does not become `keep the newest 1000 records` simply
because the backend uses a ring buffer. A ring buffer of insufficient size would fail to realize the retention guarantee
under the expected admission bounds.

Likewise, a count limit is not automatically a retention semantic. If the product later needs a Contract that promises
`the latest N occurrences`, that is a separate meaning that should be designed explicitly rather than inferred from
storage implementation.

### 5.5. Expiry and Eviction Must Not Be Confused

Expiry is the end of a declared availability obligation.

Eviction is a physical act used to reclaim storage. A backend may evict an occurrence after its retention obligation has
ended. Evicting it earlier violates the guarantee unless another retained representation still satisfies the same
availability obligation.

Priority-based displacement, oldest-record overwrite, compression, external archival, or tiered storage are realization
strategies. They can be used only when their worst-case behavior still satisfies the declared Contract.

This distinction matters because mature diagnostic systems often use bounded storage and overwrite policies. Those
mechanisms are suitable for optional operational evidence. They are not sufficient by themselves to prove Contract
retention.

### 5.6. Retention Does Not Imply Persistence Across Restart

Retention across a logical boundary and durability across physical failure are not synonyms.

A V1 backend may be able to retain evidence during an Interaction or another supported in-process lifetime without being
able to guarantee survival across process restart, host reboot, disk loss, or machine loss. Those stronger guarantees
require explicit lifecycle and capability semantics.

This ADR therefore does not infer persistence from words such as `store`, `record`, or `retain`. If later ADRs add
restart-stable or failure-domain-stable retention, the backend must state the persistence mechanism and capability
separately.

### 5.7. Retention Storage Is Not Publication

Evidence can be retained internally without being authorized for an outside consumer.

A database row, diagnostic file, telemetry buffer, or remote archival system is not automatically a Publication
boundary. If retained Contract Evidence is to become an outward claim, ADR-0058 and ADR-0059 still apply.

The eventual path is conceptually:

```text
retained Diagnostic Evidence
        ↓
Publication authorization for that evidence source
        ↓
Output strict projection
        ↓
outside
```

ADR-0058 does not yet define the final Diagnostic Evidence publication source grammar. Until that refinement exists,
retention provides internal availability only. A debug endpoint must not bypass Publication by exposing a storage record
directly.

### 5.8. Retention and Confidentiality Are Related but Not the Same Contract

Retained diagnostics can be more sensitive than ordinary Output because they may contain fault-time values that were
never intended for external use.

The safest first rule is minimization: do not declare evidence that the Contract does not need. Retention cannot solve
confidentiality by itself, and this ADR does not introduce a generic redaction transform that would conflict with strict
projection law.

A backend may encrypt storage or restrict operator access as an implementation control. Future Contract work may add a
separate confidentiality authority if machine-level guarantees are required. Those controls do not alter the meaning of
the retained evidence.

### 5.9. V1 Retention Must Stay within Closed Lifecycle Semantics

The current Scope and Lifecycle model is not yet complete enough to promise every possible retention boundary.

V1 therefore should support only retention forms whose beginning and end are already explicit in the Contract Machine or
can be added without inventing process-specific semantics. Unsupported stronger requests fail during realization rather
than being interpreted through JVM process lifetime.

The canonical Retention model must still be designed so V2 can add stronger lifecycle and persistence capabilities
without changing existing evidence identity. Physical storage location, queue index, filename, and object address cannot
be part of Retention identity.

---

## 6. User Diagnostic API and Frontend

### 6.1. The User Declares Accountability, Not Logging Behavior

The user-facing Diagnostic API exists to declare which machine occurrences require additional accountable material.

It is not a logging DSL. The user does not choose a logger class, message template, sink, stack depth, serialization
format, or callback. Those are realization and presentation choices.

A conceptual declaration has two semantic parts: an exact diagnosable source and a closed selection of material owned by
that source. Retention is declared separately because an evidence shape and an availability promise are different
Contracts.

The following is an illustrative semantic form, not final `.kontrakt` syntax:

```text
Diagnostic Evidence:
    source = InventoryAdmission.RequiredStock
    material = requestedQuantity, observedAvailableQuantity

Retention:
    evidence = InventoryAdmission.RequiredStock
    boundary = declared supported lifecycle boundary
```

The frontend may later provide more ergonomic authoring. Resolution must lower it to the same exact canonical relation.

### 6.2. Diagnostic Source Selection Resolves Before Runtime

A Diagnostic Evidence declaration cannot wait until runtime to decide which authority it refers to.

If the frontend eventually allows a broad selector for author convenience, the compiler expands it during definition
processing into exact diagnosable sources according to a closed rule. The canonical representation contains those exact
sources. Declaration order and runtime registration do not affect the result.

This avoids a diagnostic version of dynamic AOP matching where a new class or implementation method silently starts
producing Contract Evidence because it happens to match a pattern.

### 6.3. The Frontend Selects Existing Diagnostic Material

The user may select only material the owning source declares as available for diagnostic use.

A source can expose a diagnostic coordinate without exposing its whole internal representation. The compiler must reject
a request to inspect hidden user implementation state or to run an arbitrary expression after the judgment.

This creates a useful boundary for Contract authors. The author can make an important actual value diagnosable without
turning every internal computation into a Fact or Output coordinate.

The exact declaration mechanism for source-owned diagnostic coordinates remains a frontend design detail to be closed
after the semantic model is accepted. The canonical rule is already fixed: the source relationship is explicit and
finite.

### 6.4. No Executable Diagnostic Callback Enters Contract Meaning

The frontend must reject executable diagnostic callbacks as Contract material.

A declaration such as `onFailure { inspectObjectGraph() }` would make evidence depend on user implementation behavior
and runtime object shape. It would also make cost and determinism impossible to reason about before execution.

Generated code may call backend-owned capture primitives chosen by the compiler. That is realization of a static
evidence obligation, not user-supplied diagnostic control flow.

### 6.5. Human Message Text Is Not Contract Identity

The Contract declaration should not require a human error message to serve as semantic identity.

Documentation may eventually attach explanatory text for tooling, but text can change for clarity without creating a new
Contract definition. Stable identity comes from canonical source and evidence material, not punctuation or English
wording.

This leaves room for multiple renderers and later localization while preserving one Contract meaning.

### 6.6. Severity Is Not a Universal Diagnostic Contract Coordinate

Many engineering systems classify events by severity, but those scales mean different things in different authorities. A
compiler warning severity, a spacecraft event severity, a hardware correctable-error class, and an application business
failure are not one taxonomy.

ADR-0060 therefore does not add a universal Contract `severity`. If a particular Contract domain later needs severity as
machine meaning, that domain can own it. Compiler diagnostics and backend operational events may use their own severity
models without leaking them into Diagnostic Evidence.

### 6.7. Policy Worlds May Select Different Diagnostic Contracts

Diagnostic Evidence remains a Contract and therefore participates in existing Policy composition rather than inventing a
runtime enable flag as semantic authority.

If one Policy World requires richer Contract Evidence than another, the difference must be visible in the selected world
before the governed scope begins. An operator cannot disable required Contract Evidence by mutating a logger setting
inside an active boundary.

Backend operational tracing may still be enabled dynamically because it is not Contract authority. The two controls must
not share one switch whose meaning changes depending on which consumer reads it.

### 6.8. Versioning Applies to Evidence Definitions

Changing the diagnosable source or closed evidence material changes the Diagnostic Evidence Contract definition.

A later compiler cannot interpret an old retained occurrence through a new definition merely because the display name is
the same. The occurrence remains bound to the canonical definition applicable when it was established.

This is important for long-lived retention. Storage schemas may evolve, but semantic decoding must preserve the Version
relation rather than applying the latest frontend declaration retrospectively.

### 6.9. Publication and Output Remain Explicit

A user may want internal evidence for engineering and a smaller external error response for service consumers. Those are
not contradictory requirements.

Diagnostic Evidence first exists inside the machine. Publication later determines which evidence material, if any, may
receive outward authority. Output then projects the exact outward shape. A backend cannot expose all retained evidence
through an administrative endpoint and call that equivalent to Publication.

This preserves least disclosure without adding a diagnostic-specific transformation language.

### 6.10. Generated Host APIs Are Artifacts

If Kontrakt generates Kotlin or Java accessors for retained evidence, those accessors are host artifacts. They do not
become the Contract definition.

The same applies to generated numeric IDs, table offsets, event classes, or storage handles. Canonical Contract identity
must survive if V2 changes the generated API or another backend uses a different representation.

### 6.11. Diagnostic Depth Is Configurable above the Guaranteed Evidence Core

A user needs control over how much diagnostic work the generated system performs, but one control cannot mean both
Contract guarantee and operational verbosity.

If richer evidence is part of accountable machine behavior, the user declares that material through Diagnostic Evidence
and, where applicable, selects it through the existing Policy World. The compiler resolves and lowers that obligation
before execution. A runtime setting cannot weaken it after the governed boundary begins.

Additional investigation is different. The user may choose an operational diagnostic profile that enables deeper JVM,
operating-system, hardware, trace, reproducer, or recent-history capture without changing the Contract. The exact API
and profile names remain open, but the semantic split is fixed: required evidence is a Contract choice; optional
diagnostic depth is an implementation choice.

A profile should describe meaningful capture behavior rather than expose a numeric verbosity scale whose cost and
semantics change between backends. One backend may obtain a stack cheaply while another cannot provide one at all. The
common configuration therefore names the diagnostic intent and lets backend capability resolution determine which
optional enrichments can realize it. Backend-specific overrides can exist outside Contract identity.

The lowest supported operational profile still preserves stable correlation to the authoritative occurrence. Higher
profiles may retain more context or activate targeted observation. None may replace the declared evidence shape with a
weaker partial occurrence.

---

## 7. Kontrakt Compiler Diagnostic Architecture

### 7.1. Compiler Diagnostics Are a First-Class Compiler Subsystem

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

### 7.2. Diagnostic Definition and Compiler Occurrence Are Separate

Each stable compiler diagnostic kind needs an identity independent of one source occurrence.

The definition identifies what compiler condition is being reported. The occurrence binds that definition to a
particular Compilation Session, semantic subject, source projection, and causal context.

A source line number is therefore not the diagnostic identity. Line numbers move. A pass object address is not the
identity. Workers change. Human wording is not the identity. The compiler needs a stable code that can survive renderer
changes and support CI suppression or IDE correlation where policy allows it.

The occurrence may carry a revision-scoped identifier for incremental tooling. That identifier is not required to remain
stable across unrelated builds unless a later protocol explicitly promises it.

### 7.3. A Compiler Diagnostic Record Is Structured before Text

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

### 7.4. SourceManager and Provenance Are Mandatory Compiler Infrastructure

Kontrakt V1 requires a real source manager rather than storing raw path and line strings inside diagnostics.

A source anchor must be able to identify source text, an exact range or point, and the relationship between
user-authored text and generated or transformed material. As the frontend grows, the compiler may need to distinguish a
written location from a derived location in generated host artifacts or expansion-like frontend constructs.

The SourceManager must not make filesystem paths semantic identity. Paths are presentation and workspace coordinates.
Internally the compiler should use stable source-file identities within the compilation input and explicit revision
state.

Canonical Contract material may retain a compact origin handle even after frontend syntax authority has been erased.
That handle supports diagnostics and debugging; it does not turn source syntax back into Contract authority.

### 7.5. Semantic Provenance Survives Transformation Differently from Source Spans

Source provenance and semantic provenance are related but not identical.

A canonical Contract definition can remain semantically identical after a source-only edit while its source span moves.
A lowering pass can replace one IR node with another while preserving the same Contract authority. A fusion pass can
combine several generated gates while each original Contract judgment still needs distinct attribution.

The compiler therefore needs provenance mappings that can say which canonical authority a transformed object realizes
without using the transformed object's storage identity as the authority.

This is also the basis for reliable generated-system diagnostics. A JVM gate can report a compact canonical authority ID
even if optimization has removed the original high-level IR node.

### 7.6. Diagnostic Preservation Is a Pass Obligation

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

### 7.7. Diagnostic Correctness Is Stronger than Diagnostic Presence

The compiler must treat a wrong diagnostic relation as a correctness defect.

A source anchor may be stale. A related note may point to a declaration that no longer participates in the conflict. A
cached optimization explanation may refer to a target model that is no longer active. A crash dump may come from an IR
state that had already violated its verifier.

For this reason, a diagnostic product needs explicit dependencies and provenance. Where the compiler cannot establish a
trustworthy location, it is better to emit a location-less but correctly attributed issue than a precise-looking false
location.

The same principle applies to generated runtime evidence. Missing operational data can be reported as unavailable.
Invented or stale data cannot be reported as if it were captured at the source occurrence.

### 7.8. Root Cause and Cascade Diagnostics Need a Causal Model

Parser recovery, type-like resolution, composition verification, and backend selection can create cascades. One missing
symbol may make twenty later checks fail only because they lack the original meaning.

Kontrakt should not suppress every dependent issue blindly, because some later diagnostics may be independently real. It
should instead record causal relations where the compiler knows that one diagnostic depends on an earlier invalid state.

The renderer can then keep the root issue prominent and attach dependent notes or suppress redundant cascades according
to policy. Machine output retains the relation so an IDE or CI tool does not have to guess based on message order.

Causal relation is not runtime temporal order. It is a compiler explanation that one invalid state prevented or
invalidated another analysis.

### 7.9. Contract Conflict Diagnostics Need Reduced Conflict Material

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

### 7.10. Notes, Help, and Fixes Have Different Trust Levels

A note adds related information. Help suggests a direction. A fix proposes a concrete source edit.

The compiler should not represent all three as message strings because automated tooling needs to know which actions are
safe. A fix has to carry structured edit information and an applicability level. The strongest applicability should be
reserved for edits that the compiler can apply without placeholders or hidden semantic guesses.

Kontrakt should validate automatically applied fixes by reparsing and recompiling the edited input in tests. A fix that
changes Contract meaning may still be useful, but it cannot be labeled machine-safe merely because it makes the current
error disappear.

V1 can be conservative. A small number of high-confidence fixes is better than broad speculative rewriting.

### 7.11. Compiler Remarks Are Not Errors

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

### 7.12. Optimization Explanation Must Separate Legality from Profitability

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

### 7.13. Transformation History Is Selective

Recording the entire IR history of every compilation would be prohibitively expensive.

The compiler should retain stable phase and pass boundaries, transformation counters, and enough provenance to request a
deeper explanation. Detailed before/after IR, decision traces, or proof artifacts can be enabled for selected passes or
sources when investigating a problem.

This mirrors production runtime diagnostics: keep cheap anchors continuously and increase detail around the question
that actually matters.

A deterministic compiler also has a second option that many runtime systems do not. It can reproduce a compilation from
normalized inputs and recapture a deeper trace later. This makes reproducer quality part of diagnostic architecture and
reduces pressure to persist every internal state.

### 7.14. Incremental Diagnostics Are Query Products, Not Side Effects

A query or pass must not publish permanent diagnostics directly while it is still speculative or cacheable.

The computation returns a diagnostic product tied to explicit dependencies. The driver or diagnostic engine commits
those products only for the current compilation revision. If the query is invalidated or cancelled, its unpublished
products disappear with it.

This rule prevents stale diagnostics from surviving a source change simply because an earlier worker already wrote them
to a global sink.

It also lets V2 reuse a semantic query result while recomputing only its source projection. The semantic fingerprint can
remain green while a provenance or presentation fingerprint changes.

### 7.15. Semantic and Diagnostic Dependencies Are Tracked Separately

The query graph needs to know when diagnostic state depends on source details that the semantic result does not.

A whitespace edit may preserve a canonical definition but change a source range. A comment edit may change an excerpt
without changing a diagnostic code. A renamed source file may change a rendered path while preserving semantic identity.
Conversely, a Contract Version change may leave the same text range but invalidate the diagnostic's semantic subject.

V1 may recompute both sides conservatively. It must still model them separately so V2 red/green invalidation can stop at
the correct boundary instead of storing source spans inside every semantic cache value.

### 7.16. Incremental Publication Needs Revision Discipline

IDE diagnostics are observed over a moving source document. A result computed for revision N may arrive after revision
N+1 has already become current.

Kontrakt tooling therefore needs revision-aware publication. A diagnostic product must be attached to the input revision
that justified it, and stale asynchronous products must be discarded rather than rendered on a newer buffer.

A future LSP layer may use pull-style result identities or unchanged reports to avoid sending stable diagnostics again.
Those protocol choices sit above the compiler diagnostic engine. The engine itself must already know which revision and
dependency state a diagnostic belongs to.

### 7.17. Parallel Diagnostic Merge Is Deterministic

Workers accumulate structured diagnostics locally or through concurrency-safe buffers that do not expose completion
order.

After the relevant compiler boundary is complete, diagnostics are merged by deterministic keys derived from semantic and
source order. Causal children remain attached to their root. Duplicate products are removed only when they represent the
same compiler occurrence under an explicit equivalence rule.

The merge key must not become diagnostic semantic identity. It is a publication order for reproducibility and human
stability.

Single-worker and multi-worker builds must produce the same diagnostic set and the same deterministic presentation when
the source and compiler configuration are otherwise identical.

### 7.18. Deduplication Must Not Collapse Independent Causes

Two diagnostics with the same code on the same line are not necessarily the same occurrence.

Deduplication needs semantic subject and causal context, not just rendered location and message. Otherwise independent
Contract authorities can be collapsed into one issue and hide useful information.

The opposite problem is repeated emission of the same root condition through multiple analysis paths. Stable occurrence
keys allow the compiler to collapse those duplicates without relying on string equality.

### 7.19. Max-Error and Recovery Policy Are Presentation Controls over Compiler Work

Parser recovery and semantic analysis may continue after an error to expose more useful independent issues.

The compiler should have an error budget so pathological input cannot cause unbounded diagnostic work. Reaching the
budget stops further diagnostic production according to compiler policy; it does not change the semantic status of
errors already established.

Recovery nodes and poisoned compiler values must carry explicit state so later analyses know when an error is dependent
on invalid input. They cannot be ordinary semantic values that accidentally pass verifiers.

### 7.20. Human Rendering Is a Consumer of Diagnostic Data

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

### 7.21. IDE and CI Output Share the Same Source Diagnostic Product

Kontrakt should not implement one diagnostic logic for CLI and another for IDE.

An IDE needs revision-aware ranges, related locations, code actions, and stable machine data. CI needs stable codes,
structured severity, source coordinates, and deterministic output. A command-line user needs good text. The diagnostic
engine provides one product that each frontend adapts.

This also keeps fixes honest. The same structured edit that appears as a CLI suggestion can be offered as an IDE action
without reparsing a human string.

### 7.22. Compiler Self-Observability Is Adjacent but Separate

Phase timers, allocation volume, query hits, invalidation counts, frozen-image size, planner work, pass statistics, and
artifact size describe compiler operation.

They are essential for making Kontrakt itself fast and scalable, but they are performance observability rather than user
semantic diagnostics. The compiler can correlate an expensive diagnostic or pass with these metrics, yet a build warning
should not be created merely because an internal timer exists.

The separation allows aggressive measurement in compiler engineering builds without stabilizing every metric as a public
diagnostic protocol.

### 7.23. Internal Compiler Errors Need a Stable Reproducer Core

An internal compiler error is different from an invalid user Contract. The compiler should state that distinction
clearly and preserve a reproducer core that does not depend on the potentially corrupted state at the crash point.

The core should identify the compiler build, normalized driver options, input subset or dependency identities, target
and backend capability snapshot, selected Version and Policy inputs where relevant to compilation, the active phase or
pass, and the last successfully verified IR boundary.

A deterministic dump of that last valid boundary is more trustworthy than a large dump of an already-corrupted mutable
object graph. Optional stack, heap, JVM, or OS crash material can be attached as supplemental implementation evidence.

V2 may automate reduction and pass bisection. V1 must already retain enough identity to reproduce and manually isolate
the failure.

### 7.24. Reproducer Material and Diagnostic Records Are Different Products

A diagnostic record explains what the compiler observed. A reproducer bundle contains enough input and configuration to
make the compiler observe it again.

One compiler issue may have a small diagnostic record and a large reproducer. The reproducer may include source subset,
frozen IR, capability state, or seed information that should not be emitted in normal terminal output.

The two products can reference each other through stable artifact handles. Retention and privacy policy for reproducer
bundles belongs to compiler tooling, not the user's Diagnostic Evidence Contract.

### 7.25. Diagnostic Testing Is Part of Compiler Correctness

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

### 7.26. Diagnostic Quality Needs Engineering Metrics

Commercial quality cannot be measured by the number of diagnostic codes.

Kontrakt should track whether the primary location points near the true source, how often one root issue causes
cascades, how often a machine-applicable fix actually recompiles, whether clean and incremental diagnostics agree,
whether parallel execution changes order, and how much time and memory diagnostic production adds to compilation.

These metrics belong to compiler engineering. They are not Contract coordinates. They give the project a way to improve
diagnostic quality without turning subjective message wording into semantic law.

### 7.27. Machine-Readable Diagnostic Schema Has Its Own Compatibility Boundary

Once CI systems and IDEs consume structured compiler diagnostics, the schema itself becomes a tool protocol. It still is
not Contract meaning, but arbitrary field changes can break external compiler tooling.

Kontrakt should therefore version the machine-readable diagnostic schema separately from Contract Version. Stable
diagnostic codes can survive a schema revision, while new optional fields can be introduced without changing the issue
being reported. A breaking representation change requires a compiler-protocol compatibility decision rather than a new
user Contract definition.

The schema should prefer typed fields over opaque extension strings for information that tooling is expected to consume.
Backend-specific attachments can use explicitly namespaced extensions so a JVM reproducer does not force JVM concepts
into the common compiler diagnostic core.

### 7.28. Compiler Event Streams Are Not Diagnostic Results

A long compilation may emit progress events that say which phase started, which artifact completed, or which worker is
active. Build systems also need invocation lifecycle events so remote or distributed tooling can understand whether a
compilation finished normally.

Those events are not compiler diagnostic authority. A crash may leave an announced phase without a matching completion
event. An upstream error may prevent an expected artifact from ever being attempted. The final compilation result and
structured diagnostics must remain authoritative even when the progress stream is incomplete.

This distinction prevents the compiler driver from inventing semantic states for work that was not reached. Progress and
execution history can explain absence without turning absence into a new Contract or compiler verdict.

### 7.29. Long-Lived Compiler Processes Need Diagnostic Session Boundaries

A daemon or language server can perform thousands of compilations or partial analyses in one process. Diagnostic state
must therefore belong to an explicit compilation or analysis revision rather than to process lifetime.

A new session cannot inherit an old error merely because the same diagnostic engine object remains alive. Crash
reproducer material, performance traces, and retained diagnostic caches may outlive one request, but each product needs
an explicit session or revision relation.

This is the compiler-side analogue of Retention separation: physical longevity of a daemon object does not define the
availability or validity of a diagnostic product.

### 7.30. Compiler Diagnostic Work Is Lazy, Bounded, and Selectively Enriched

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

## 8. Generated User-System Diagnostic Architecture

### 8.1. Generated Diagnostics Have Two Planes

The generated system has a Contract Evidence plane and an operational diagnostic plane.

The Contract Evidence plane realizes declarations from the user's Contract. Its capture and retention guarantees are
part of machine behavior and cannot be weakened by an operator's tracing preference.

The operational plane contains backend observations that help engineers investigate physical execution. It may use JVM
recording, stacks, profilers, operating-system events, hardware records, network traces, or external telemetry. Those
observations may be sampled, unavailable, target-specific, or intentionally disabled.

The planes can be correlated. They cannot be treated as interchangeable.

### 8.2. Contract Evidence Uses a Small Generated Kernel

V1 should realize required Diagnostic Evidence through compact generated machinery close to the owning judgment.

The hot path should capture only the material required by the selected evidence definition and append or publish it into
a bounded backend-owned representation. Human formatting remains cold. Reflection, runtime annotation lookup, arbitrary
object serialization, and stack walking must not be required to establish ordinary Contract Evidence.

The compiler already knows the exact source definition and closed coordinate set. It can specialize generated capture
for those coordinates instead of constructing a generic map at runtime.

This keeps the guarantee compatible with the broader Kontrakt optimization strategy: closed Contract material is lowered
into compact target-friendly machinery, while rendering and exploratory analysis stay off the hot path.

### 8.3. Generated Optimization Must Preserve Evidence Observability

A generated gate can be fused, specialized, or simplified only if its required evidence remains attributable to the
original Contract source.

If two checks are fused into one branch, a rejection still needs to identify which exact judgment established the result
and capture the material required by that judgment's evidence definition. If a constant specialization removes a value
from runtime computation, the compiler can use the constant canonical value instead of retaining a dead runtime object.

This means diagnostic preservation can sometimes make generated code cheaper rather than more expensive. The compiler
should preserve meaning, not obsolete implementation structure.

### 8.4. Contract Evidence Is Not Sampled

Sampling is useful for high-volume operational tracing. It is not a valid implementation of a required Diagnostic
Evidence occurrence.

If a declared evidence obligation applies to every qualifying source occurrence, every such occurrence must be handled
according to the Contract. A backend that can only provide one-percent sampling does not satisfy that Contract.

A later Contract could explicitly define a statistical diagnostic obligation if the product ever needs one. This ADR
does not infer statistical semantics from an observability sampler.

### 8.5. Operational Evidence May Use Selective Capture

Rich runtime evidence should be acquired according to explicit operational policy and cost.

A low-cost baseline may retain stable Contract occurrence IDs, coarse phase or boundary markers, and a small ring of
runtime events. When an anomaly is detected, the backend can temporarily enable deeper JFR events, method profiles,
selected object traces, heap or thread dumps, or another target-specific mechanism.

This strategy is preferable to permanent maximum-detail instrumentation because it preserves production performance
while keeping a path to deep investigation.

Operational capture policy is not a Contract Policy World unless the user explicitly declares a Contract obligation that
uses it. It belongs to deployment and backend tooling.

### 8.6. Current Diagnosis and Fault-Time Snapshot Are Different

A diagnostic command executed now may inspect current state. A fault-time evidence occurrence describes state captured
when the relevant event was judged.

The runtime must not blur those two sources. A health endpoint can say that the subsystem is healthy now while a
retained fault-time snapshot shows why it failed earlier. Both are useful.

The distinction also matters for repair. Recovery may mutate the state immediately after a Failure. Reading the repaired
state later cannot substitute for evidence of the failed state.

### 8.7. Correlation Does Not Create Causality

The generated system should expose stable correlation identities that let tools connect a Contract occurrence with
backend traces.

An Interaction or other active boundary may carry a run-scoped correlation token. Contract Evidence can refer to the
source occurrence. JVM or operating-system events can attach the same token where the backend controls both sides.

Sharing a token does not prove that two events caused one another. Causal authority still comes from explicit machine
dependency or a diagnostic analysis that can establish the relation. This rule is critical in distributed systems where
trace adjacency is often mistaken for semantic dependency.

### 8.8. Backend Event Time Does Not Become Contract Time

JVM events, OS traces, hardware records, and remote collectors can use different clocks.

A backend may preserve timestamps and clock-domain metadata to support investigation. Kontrakt must not sort Contract
meaning by those timestamps or infer an authoritative whole-machine order from them.

Where the Contract itself includes an explicit time coordinate, diagnostic correlation can relate an operational event
to that coordinate. It cannot substitute a profiler timestamp for a Contract time source without an explicit lowering
rule.

### 8.9. Operational Diagnostics Must Expose Loss and Unavailability Honestly

High-volume buffers overflow. Trace providers can be disabled. Native symbolization can fail. Hardware records may mark
fields invalid. A crash can happen before a dump is complete.

The operational diagnostic plane should preserve these facts rather than fill missing fields with defaults. Where the
underlying facility provides lost-event counts, overflow markers, field-validity bits, or capture-status information,
Kontrakt adapters should keep that information available to engineering tools.

This metadata describes the operational evidence quality. It does not create a universal Contract `quality` enum.

### 8.10. JVM Recording Is a Backend Capability

The JVM backend can use JVM Flight Recorder and related runtime facilities for optional operational evidence.

JFR is attractive because event definitions, event settings, recording lifetime, and retention are separable. Expensive
event work can be guarded so disabled recordings do not pay full materialization cost. Recordings can be bounded by age
or size without changing Contract Evidence semantics.

Kontrakt should attach stable Contract correlation metadata only where doing so is cheap and safe. A JFR event type or
recording file is not a canonical Contract definition.

### 8.11. JIT Compiler Diagnostics Are Useful for Realization Investigation

A JVM backend may rely on HotSpot or Graal compilation decisions that affect generated-code performance.

The runtime can expose JIT phase, compilation, deoptimization, or uncommon-trap information through backend diagnostics.
Graal-style retry of a failed compilation with a deeper diagnostic mode demonstrates a useful pattern: the normal path
stays cheap, while only the failing compilation is rerun with graph dumps and richer evidence.

Kontrakt should preserve enough generated-method and canonical-authority mapping to connect those JIT records to
Kontrakt-owned generated machinery. It should not promise that a specific JIT compiler, graph format, or deoptimization
reason exists on every backend.

### 8.12. Sanitizers Are Diagnostic Realizations, Not Production Contract Evidence by Default

Compiler instrumentation systems such as memory and race sanitizers show the trade-off clearly. They can provide highly
valuable allocation history, conflicting accesses, or thread creation paths, but their execution and memory overhead can
be substantial. Partial instrumentation can also reduce diagnostic accuracy.

Kontrakt may integrate sanitizer-enabled builds into verification and test products. The Contract cannot assume the same
evidence exists in an ordinary production artifact unless the backend explicitly realizes such a guarantee.

This keeps heavy dynamic diagnosis available without forcing its cost into every deployment.

### 8.13. Operating-System Tracing Is Supplemental Evidence

Operating systems offer provider-based event tracing, ring buffers, health reporters, and crash storage. These
facilities can expose scheduling, I/O, driver, kernel, or process behavior that Kontrakt cannot derive from Contract IR.

Adapters should preserve source identifiers and loss metadata when possible. They may use target-specific filters to
collect only events correlated with the generated system.

An OS trace cannot establish a Contract Failure merely because it observed an error code. The Contract source must still
establish its own result if enough machine authority remains.

### 8.14. Hardware and Firmware RAS Form Another Evidence Layer

Modern platforms may report memory, processor, PCIe, CXL, firmware, or other hardware errors through structured records.
Those records commonly distinguish the error record identity from sections containing source-specific information. They
also distinguish fields that are valid from fields that were not captured, and some formats report overflow when earlier
error information may have been lost.

The collection timestamp may describe when system software gathered the record rather than when the physical fault
actually occurred. This is exactly why backend diagnostic adapters must preserve provenance and timing semantics instead
of flattening every low-level record into `hardware error at time T`.

Kontrakt V1 does not need to implement every RAS adapter. The generated-system architecture must leave a correlation
seam so future backends can attach this evidence without changing Contract Failure meaning.

### 8.15. Crash-Persistent Operational Evidence Is a Backend Feature

Some operating systems can place crash logs or traces in reserved persistent memory so they survive reboot. Similar
mechanisms exist in embedded and spacecraft systems because the most useful evidence can disappear with volatile state.

A JVM backend may expose such a facility when the platform supports it. This does not mean the Contract Retention model
implicitly promises reboot survival. Strong persistence remains an explicit capability decision.

The distinction lets an operator gain post-crash evidence opportunistically without misleading Contract users about the
guarantees of another target.

### 8.16. External Observability Platforms Remain Outside the Core

A tracing collector, log service, metric backend, or incident-analysis system can correlate operational evidence across
many processes.

Kontrakt may emit stable machine-readable adapters for those systems. The external platform remains outside the Core
just like another storage or network adapter. Its sampling, retention, aggregation, redaction, and query behavior cannot
silently become Contract meaning.

If an outside diagnostic service feeds new material into another Kontrakt machine, that material must enter through that
machine's normal Input boundary.

### 8.17. Whole-Machine Correlation Does Not Flatten Core Boundaries

A Whole Machine can include several Cores whose outputs and inputs are connected through outside adapters.

Operational tracing may correlate events across those Cores for investigation. It must not make the trace collector a
new shared Contract authority. Each Core's Result, Failure, Evidence, Publication, and Output boundaries remain intact.

A cross-Core diagnostic view is therefore a projection over several authoritative records and external observations. It
is not a hidden pipeline through which one Core reads another Core's internals.

### 8.18. Required Capture Cannot Depend on Silent Drop or Unbounded Backpressure

A diagnostic sink has to choose what happens when producers create evidence faster than storage can accept it. Some
real-world systems drop records to protect the producer. Others block the producer to avoid losing records. Neither
choice can be hidden when the material is required Contract Evidence.

Kontrakt should include required evidence storage in precomputed resource planning wherever the backend owns that
resource. Capacity admission should reject work before entering a governed boundary when the machine already knows it
cannot preserve the required evidence guarantee. A backend must not wait until a hot path is full and then silently drop
the occurrence.

Unexpected substrate loss is different from ordinary planned capacity. If the required evidence path itself becomes
unavailable after admission, the resulting behavior follows the evidence obligation and realization-failure laws rather
than a logger's overflow setting.

Optional operational tracing can choose drop, overwrite, or backpressure policies appropriate to deployment. Those
policies need explicit loss statistics so an investigator can understand the resulting evidence.

### 8.19. Throttling and Deduplication Cannot Erase Required Occurrences

Repeated operational events are often throttled because a fault can produce thousands of identical messages. That is a
valid implementation policy for best-effort observation.

A Contract Evidence obligation is different. If each source occurrence is semantically required to have evidence, a
throttle cannot collapse ten occurrences into one record and still claim that all ten evidence occurrences were
retained. The backend may compress storage only if it preserves a lossless representation from which the exact required
occurrences and their relations can be recovered.

If the Contract later defines aggregate evidence instead, that aggregate needs its own meaning. The implementation must
not infer aggregation from a logger's flood-control policy.

### 8.20. Audit Records and Diagnostic Evidence Are Not Automatically the Same Thing

An audit ledger usually has a stronger concern with historical completeness, actor attribution, append-only ordering,
and long-lived review. Diagnostic Evidence is focused on explaining exact machine occurrences and may have a much
shorter Retention boundary.

One physical record can potentially serve both purposes when both Contracts are satisfied, but ADR-0060 does not make
every diagnostic occurrence an audit record. Doing so would force archival and ordering obligations onto diagnostic hot
paths that do not need them.

If Kontrakt later adds an explicit audit or compliance Contract, it can consume or reference Diagnostic Evidence without
changing the evidence's original meaning.

### 8.21. Source-Supplied Validity and Uncertainty Must Not Be Normalized Away

Some low-level diagnostic sources know that only part of a record is valid, that a reported address is imprecise, that
an overflow may have lost earlier errors, or that a measurement carries uncertainty. Those facts are part of the
operational record's interpretation.

A backend adapter should preserve such source-supplied validity instead of converting unavailable fields into zero or
assigning false precision. The common Kontrakt Contract does not need one global confidence scale to follow this rule.
It only needs to avoid destroying the semantics of the backend evidence it chooses to expose to diagnostic tooling.

### 8.22. Operational Diagnostic Depth Is Explicit and Aggressively Selective

The generated system should keep the normal hot path close to the cost of the machine without rich operational
diagnostics. Contract-required evidence is planned separately and remains guaranteed. Everything beyond that core is a
candidate for selective acquisition.

An operational profile can keep cheap correlation anchors continuously while delaying expensive material until a known
trigger occurs. The trigger may be an established Failure, a selected realization anomaly, an operator request, or a
backend condition whose diagnostic semantics are explicitly defined by that backend. Once triggered, capture can narrow
to the affected Contract authority, realization region, or bounded time window instead of tracing the whole process.

This approach is deliberately asymmetric. Normal successful traffic should not pay for full stacks, object snapshots,
large trace histories, or hardware counters that no investigation is using. When the machine reaches an occurrence that
justifies deeper investigation, the backend may temporarily increase detail and preserve the relevant recent context.
The increased depth remains bounded by explicit resource and retention policy.

Sampling and throttling are allowed only in the optional operational plane. They must expose their own loss when that
loss matters to interpretation. A sampling decision cannot erase an occurrence whose Diagnostic Evidence Contract
requires material, and a throttle cannot merge distinct required occurrences into one representative record.

The user API may offer named operational profiles and targeted overrides. Those controls select cost and investigation
depth; they do not form a hidden fourth Contract axis. If the user needs a diagnostic guarantee to differ by Policy
World, that difference belongs in the Contract selection described in Section 6 rather than an operational switch.

---

## 9. Compiler Pipeline Integration

### 9.1. Diagnostic Architecture Follows the Compiler Stages

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

### 9.2. Frontend Diagnostics Preserve User-Written Context

Lexer and parser diagnostics operate while source syntax is still authoritative evidence for what the user wrote.

Recovery should create explicit poisoned or missing syntax states so later stages know the source is incomplete. The
parser may continue to report independent issues, but it must not fabricate semantic declarations just to keep the tree
well-typed.

Resolution diagnostics then add semantic information: ambiguous name, missing authority, duplicate declaration,
forbidden composition, incompatible selector, or unresolved Contract Version. Their primary location should still point
to user source, while related locations show the declarations that caused the conflict.

### 9.3. Canonicalization Erases Frontend Authority but Retains Origin Handles

Once Contract meaning has been canonicalized, host syntax no longer owns the semantics.

The compiler can still keep compact source-origin handles as diagnostic metadata. Those handles allow a canonical
verifier error to return to the declarations that produced the material. They must not affect canonical equality or
Contract identity.

This separation is required for caching. Two source forms that lower to the same canonical Contract can share semantic
material while retaining different source presentations for diagnostics.

### 9.4. Verifier Diagnostics Operate on Canonical Authority

The verifier should diagnose against canonical Contract subjects, not re-read the frontend to guess what was intended.

When a contradiction exists, the verifier identifies the exact canonical obligations that conflict. Source provenance
then projects those authorities back to user declarations.

This ordering matters. If the diagnostic engine starts from source text patterns, it can report a plausible conflict
that is not the one the canonical machine actually rejected.

### 9.5. Diagnostic Evidence Definitions Are Verified Like Other Contract Material

The compiler must reject an evidence definition whose source does not exist, whose selected material is not diagnosable,
whose coordinate sorts are inconsistent, or whose reference becomes ambiguous after Version and Policy resolution.

Retention must resolve to an evidence definition that can actually be retained under the chosen lifecycle semantics.
Publication compatibility must be checked before evidence can be exposed outward.

These checks are ordinary compiler verification of Contract material. They are not runtime validation hidden in a
logger.

### 9.6. Lowering Produces an Evidence Realization Plan

Backend-neutral lowering should translate a Diagnostic Evidence definition into the minimum machine work required to
establish an occurrence.

The plan identifies the canonical source, selected values, occurrence correlation, and required retention handoff. It
does not choose JVM object classes or an external logging sink.

The JVM backend can then specialize this plan into primitive table indices, compact carriers, generated code, or another
efficient physical layout.

Keeping this seam backend-neutral allows a future native or embedded backend to realize the same evidence obligation
without reproducing JVM concepts.

### 9.7. Analysis Infrastructure Serves Both Optimization and Diagnostic Explanation

A compiler should not recompute expensive semantic relations independently for errors, optimization, and explanation.

If dominance-like dependency, Contract reachability, World selection, capability resolution, or conflict analysis is
already available as an analysis result, the diagnostic engine can consume that result through explicit dependencies.
The analysis remains owned by its subsystem; the diagnostic product is one consumer.

This architecture also makes invalidation honest. When a transformation invalidates the analysis, diagnostics that
depend on it are invalidated through the same graph rather than silently reusing an old explanation.

### 9.8. Optimization Records Are Bounded Side Products

A transform can optionally produce a compact structured decision record containing the opportunity identity, the
analyses relevant to legality, the cost decision when applicable, and the resulting transformation identity.

The record is not required for every ordinary build. It can be enabled selectively for explain modes, regression
tracking, or profile-guided compiler development.

The pass manager should not retain full object graphs simply to support future explanations. Stable IDs and
deterministic IR dumps provide a much cheaper long-term seam.

### 9.9. Diagnostic Provenance Participates in Analysis Invalidation

When a pass changes source correspondence or authority mapping, it invalidates the relevant provenance analysis even if
the semantic transformation itself is valid.

A later pass that needs a source diagnostic either consumes preserved provenance or requests a recomputed mapping. It
cannot guess a source location from a nearby transformed node.

This rule should be encoded in pass interfaces from V1 because adding it after many transformations exist would require
a compiler-wide retrofit.

### 9.10. Backend Capability Diagnostics Need Exact Refusal Reasons

A backend may be unable to realize a Contract for reasons that are neither frontend errors nor user-system Failures.

The compiler should report the exact required capability and the target fact that makes it unavailable. For example, a
retention guarantee may require persistence the current JVM deployment target does not promise. A resource guarantee may
exceed the backend's controllable region. A generated-output feature may require a classfile or runtime capability not
present in the selected target.

The diagnostic must attribute the refusal to the Contract requirement and backend capability boundary rather than emit a
generic `unsupported` message.

### 9.11. Generated Artifact Mapping Supports Post-Deployment Diagnosis

Generated JVM methods and fields may carry compact metadata that maps them back to canonical Contract authorities.

This mapping supports stack symbolization, JFR correlation, crash reports, and backend debugging without making
generated names semantic identity. A different code-generation strategy may emit different methods while preserving the
same mapping relation.

The mapping should be emitted only to the extent needed for the selected diagnostic capability and artifact size budget.
A production artifact does not need to carry every compiler-internal debug structure.

### 9.12. Deterministic Recompilation Is a Diagnostic Capability

Kontrakt's determinism requirements can make later investigation substantially stronger.

Given the same authoritative inputs, compiler version, target capability snapshot, and explicit configuration, the
compiler should be able to reproduce the same semantic and generated results. This lets an engineer rerun a failure with
extra diagnostic tracing instead of having to retain every internal compiler event from the original build.

V2 incremental and parallel compilation must preserve this property at externally observable compiler boundaries. Cache
state may change physical work, not diagnostic meaning.

### 9.13. Diagnostic Planning Separates Guarantee from Cost

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

## 10. V1 Architecture

### 10.1. V1 Must Build the Real Diagnostic Skeleton

V1 may support fewer Diagnostic Evidence sources and simpler retention boundaries than later versions. It must not use a
throwaway diagnostic architecture.

The frontend needs explicit evidence declarations that resolve to exact sources. Canonical material must represent the
definition independently from runtime occurrence. Backend-neutral lowering needs an evidence realization plan. The JVM
backend needs a compact capture path whose required work can be included in Budget and Capacity planning where Kontrakt
owns those resources.

At the compiler level, V1 needs a SourceManager, structured diagnostic records, stable diagnostic codes, deterministic
merge, machine-readable output, and a renderer built on the same records. Pass and query APIs must return or register
structured diagnostic products through explicit context rather than write irreversible global strings.

### 10.2. V1 Source Provenance Is Non-Negotiable

The first compiler release already performs multiple lowering stages. Postponing provenance until an IDE is built would
make every intermediate representation and pass API harder to repair later.

V1 should attach compact origin handles to semantic and lower IR where diagnostics can still arise. These handles
resolve through a centralized provenance service rather than storing copied path/line strings in every node.

The representation can be conservative and recompute mappings more often than V2. The semantic boundary between origin
metadata and Contract identity must already be correct.

### 10.3. V1 Diagnostic Engine Supports Structured Relations

The baseline engine should support one primary source anchor and related anchors, stable code, compiler phase, semantic
subject, severity, structured message arguments, and deterministic notes.

Help and fix data can be added gradually, but the record format should not force them into one text blob. Machine output
must expose stable structure suitable for CI and future IDE use.

Diagnostic causes should be representable so parser and verifier cascades do not require string heuristics later.

### 10.4. V1 Query APIs Leave Incremental Seams

V1 does not need the full persistent red/green engine to gain value from query discipline.

Compiler computations should have explicit inputs and immutable results where practical. Diagnostic products should be
associated with the query or phase that established them. Source revision is an explicit input to source presentation.

The first implementation may invalidate more than necessary. It must not hide semantic dependencies or diagnostic
publication in mutable singletons that V2 cannot observe.

### 10.5. V1 Parallel Work Uses Deterministic Publication

Independent compiler work can run in parallel, but it must not write directly to user-visible output in completion
order.

Each work item produces diagnostics with deterministic semantic keys. The driver merges them after the appropriate
boundary. The same inputs compiled with one or many workers must yield equivalent diagnostic sets.

This rule also applies to generated verification and PBT results where the compiler owns deterministic merge.

### 10.6. V1 Optimizer Emits Explainable Decisions on Demand

V1 optimization does not need a full profile-guided profitability framework, but each nontrivial transform should have a
stable pass identity and a clear preservation contract.

When explain mode is enabled, the compiler should be able to record whether the transformation was applied, blocked by
legality, or declined by a current heuristic. Diagnostic preservation blockers deserve an explicit reason because they
can otherwise look like optimizer weakness.

This is the seed for later optimization remarks rather than a separate debug print system.

### 10.7. V1 Verifier Can Produce Conflict Sets

For composition failures, V1 should preserve the exact canonical obligations that participated in the rejection.

The first conflict set need not be globally minimal. It should be deterministic and small enough that a renderer can
show which declarations must be examined together.

If a solver is used internally, proof checking and minimal-core reduction may remain later work. The authoritative
verifier result must not depend on whether rich explanation was enabled.

### 10.8. V1 Internal Compiler Error Support Is Real

V1 should capture a structured internal-error record and a reproducer manifest.

The manifest includes normalized compiler inputs and the last successfully verified phase. Deterministic IR dumps at
selected boundaries, `--verify-each`, pass timing, pass statistics, and before/after dumps provide the manual isolation
path.

Automatic reduction can wait. A user should not be asked to reconstruct which hidden compiler mode produced an internal
error from an unstructured stack trace.

### 10.9. V1 Runtime Evidence Is Compact

The generated runtime should capture required evidence into compact typed storage and defer human formatting.

A first backend can use fixed or bounded in-memory structures when the declared Retention boundary permits that
realization. The compiler must account for the memory region as Kontrakt-owned resource where Capacity law applies.

JVM stacks or generic maps are not required for normal Contract Evidence.

### 10.10. V1 Operational Diagnostics Remain Optional

The JVM backend may provide hooks for JFR, structured logging, crash reporting, or other operational systems. Those
hooks should accept stable Contract correlation identities so operators can connect physical events to machine
occurrences.

The absence of an optional backend integration does not weaken Contract Evidence. Conversely, enabling a rich profiler
does not expand the Contract.

### 10.11. V1 Tests Establish Diagnostic Invariants

V1 release gates should compare clean and repeated builds, worker counts, source-order perturbations, and relevant
optimization settings.

Diagnostic golden tests cover human rendering, while structured tests cover codes and provenance. Pass regression tests
check that source attribution survives transformation. Compiler fuzzing includes malformed input and transformation
paths. Runtime tests verify that required evidence is not dropped by the backend under supported bounds.

These tests are part of the commercial compiler foundation rather than polish to be added after feature completion.

### 10.12. V1 Stores Compact Diagnostic Relations and Renders Rich Contract Explanations

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

### 10.13. V1 Exposes Diagnostic Depth without Making It Semantic Ambiguity

V1 should expose an explicit diagnostic-depth control for compiler investigation and generated-system operational
diagnostics. The exact names can be finalized with the frontend, but the modes must have defined behavior rather than
mean "print more stuff."

The normal mode favors precise Contract-language explanations and low overhead. A deeper mode may ask the compiler for
conflict reduction, optimizer decisions, source-to-IR provenance, or reproducer material. Generated systems may likewise
activate richer backend observation around selected occurrences. Required Contract Evidence remains outside these
optional depth controls.

The first implementation can support only a small number of profiles. The architecture must already prevent a backend
from interpreting a lower profile as permission to sample or drop declared Contract Evidence.

---

## 11. V2 Extension Path

### 11.1. Red/Green Incremental Diagnostics

V2 can make the dependency distinctions from V1 operational.

Semantic query fingerprints, source-provenance fingerprints, and presentation products may be reused independently when
their exact inputs remain green. Early cutoff can stop invalidation when a recomputed semantic result is identical even
though the source input changed.

The diagnostic engine must still publish against the current source revision. A green semantic result with a moved
source declaration receives a refreshed source projection before it reaches an IDE.

### 11.2. Persistent Diagnostic Caching

Once schema and dependency rules are stable, V2 may cache structured diagnostic products across compiler sessions.

A cache entry needs compiler schema version, semantic dependencies, target/capability dependencies, and provenance
requirements. Human-rendered text should not be the cache key.

Remote caches can reuse the same products only if source identity and privacy rules permit it. A diagnostic containing
local absolute paths or sensitive excerpts must not be uploaded accidentally because the semantic cache is shareable.

### 11.3. Richer Constraint Explanations

V2 can add stronger conflict minimization, proof checking, counterexample construction, and explanation search around
complex Contract composition.

The compiler may choose an explanation based on usefulness and cost after the authoritative verifier result is known. A
small unsatisfiable core, a counterexample witness, and a machine-checkable proof are different products and need not
all be produced for every failure.

This makes formal verification machinery useful without exposing raw solver internals as the public language of Kontrakt
diagnostics.

### 11.4. Translation Validation and Diagnostic Preservation

Selected optimization passes may gain translation-validation checks that compare pre- and post-transform meaning for the
specific input.

The same framework can validate required diagnostic observability. If a transform fuses or eliminates a generated gate,
the checker can verify that each externally required evidence occurrence remains derivable from the lowered result.

V2 can run these checks continuously in compiler QA and selectively in debug builds without paying full cost in every
normal compilation.

### 11.5. Profile-Guided Compiler Remarks

When Kontrakt begins using target cost models or profile information, optimization diagnostics can explain the evidence
behind profitability decisions.

A remark may identify that an Interaction is hot, that a generated branch is cold, or that allocation pressure changed a
fusion decision. Profile data is compiler optimization input, not Contract truth. Recompiling with another profile may
change the generated artifact without changing Contract meaning.

The diagnostic schema should distinguish static legality from profile-dependent profitability so users know which part
of the decision is stable.

### 11.6. Adaptive Operational Capture

Generated systems can increase operational diagnostic depth after an anomaly or explicit operator request.

A baseline event can trigger a temporary recording window around the relevant Interaction, JVM compilation, storage
subsystem, or target device. Differential or targeted tracing can focus on the first point where a healthy and unhealthy
execution diverge instead of recording every event system-wide.

This remains operational unless a future Contract explicitly requires a particular adaptive capture guarantee.

### 11.7. Stronger Retention Capabilities

V2 may add persistence across process restart, host reboot, or stronger failure domains once Lifecycle and backend
capability semantics are explicit.

The logical Evidence occurrence remains the same even if V2 stores it in durable files, a database, replicated storage,
or a platform error repository. Storage migration cannot change the canonical definition it references.

### 11.8. Cross-Backend Correlation

A future backend may run on native, embedded, distributed, or accelerator targets.

The same canonical occurrence identities should be usable to correlate Contract Evidence with target-specific telemetry
without changing the Contract source. Hardware RAS, spacecraft events, embedded fault memory, or native tracing can all
be adapters over that seam.

This is why JVM-specific identifiers cannot become the V1 canonical evidence model.

### 11.9. Automated Reproducer Reduction

V2 can add compiler-aware reduction that understands `.kontrakt`, canonical IR, pass boundaries, and dependency graphs.

A reducer should preserve the exact diagnostic or internal compiler error being investigated rather than merely preserve
non-zero exit status. Stable diagnostic codes and semantic subjects from V1 provide the oracle needed for that
reduction.

Pass bisection can then find the first transformation that changes the relevant property, while deterministic replay
keeps the investigation reproducible.

### 11.10. Diagnostic Cost Models

Later compiler versions may make diagnostic acquisition itself cost-aware.

An ordinary build can keep stable source and semantic anchors. An explain build can spend more time minimizing conflict
cores or retaining pass decisions. A compiler engineering run can collect detailed allocation and transformation traces.
The user chooses the depth appropriate to the question without changing Contract validity.

Generated systems can use a similar layered model for operational diagnostics. Contract-required evidence remains
outside that optional cost decision because its cost is already part of realizing the declared Contract.

The compiler cost model can consider the price of explanation itself. Conflict-core reduction, source reconstruction, IR
snapshotting, proof production, renderer work, and persistent cache traffic can have very different costs. A diagnostic
planner can choose the cheapest material that satisfies the requested explanation quality, then stop when further detail
would not change the actionable result.

Runtime profiles can evolve in the same direction. A target may use anomaly-triggered depth, bounded recent-history
freeze, differential comparison, or backend-native sampling for optional evidence while retaining the same Contract
correlation anchors. These strategies remain interchangeable realization choices unless a future Contract explicitly
requires one of them.

---

## 12. Verification and Quality Requirements

### 12.1. Contract-Material Verification

The compiler verifies that every Diagnostic Evidence definition resolves to an allowed exact source and a closed set of
diagnosable material. Selected coordinates must exist at the applicable Version and Policy World. A definition cannot
bind through hidden implementation state or executable user callbacks.

Retention resolves only after its evidence source is valid. An unsupported lifecycle boundary or backend capability is a
realization error rather than an inferred weaker guarantee.

### 12.2. Evidence-Occurrence Verification

Reference judgment machinery should be able to validate that an evidence occurrence corresponds to the exact source
result occurrence and contains the declared material.

Optimized generated capture should be tested against that reference behavior. If the optimized runtime representation
uses compact ordinals or fused gates, those storage choices must still decode to the same canonical evidence relation.

### 12.3. Optimization Preservation Verification

Every transform that can affect Contract Evidence or compiler provenance has a preservation obligation.

Regression tests compare pre- and post-transform attribution. Selected transforms can use differential or
translation-validation checks. A transform that changes acceptance behavior, Failure attribution, required evidence, or
source provenance is rejected or fixed before release.

### 12.4. Clean and Incremental Diagnostic Equivalence

For the same current source state, a clean compilation and an incremental compilation must agree on compiler diagnostic
meaning.

The physical amount of work may differ. Cache-hit remarks may differ when explicitly requested. Stable error codes,
semantic subjects, source anchors after current projection, and Contract validity cannot differ merely because cached
queries existed.

### 12.5. Single-Worker and Multi-Worker Equivalence

Parallel execution may reorder physical work but not diagnostic results.

Tests should perturb worker counts and scheduling while comparing the final deterministic diagnostic set. This includes
causal attachment and deduplication, not only sorted message text.

### 12.6. Diagnostic-Provenance Metamorphic Tests

Source edits that preserve Contract meaning are useful adversarial tests.

Whitespace, comments, unrelated declaration order, path relocation, and other presentation changes can verify that
semantic identity remains stable while current source anchors update correctly. Conversely, semantic changes at a stable
text position verify that cached diagnostics do not survive merely because the span is unchanged.

### 12.7. Invalid-Program Diagnostic Fuzzing

Parser and semantic fuzzing should evaluate diagnostic quality as well as crash resistance.

Generated invalid inputs can test whether the compiler reports the intended root condition, whether recovery produces
unbounded cascades, and whether different invalid constructions accidentally collapse to an unrelated generic message.
The diagnostic code and semantic subject provide a stronger oracle than comparing human strings.

### 12.8. Fix Validation

Machine-applicable fixes must be applied in tests and compiled again.

The test does not merely assert that the original error disappears. It checks that the edit is syntactically valid, does
not introduce placeholder text, and satisfies the diagnostic's stated applicability conditions.

### 12.9. Reproducer Validation

Internal-error tests should verify that a generated reproducer can invoke the same compiler phase with normalized
inputs.

The reproducer manifest must not depend on temporary object addresses or worker-local filenames. Where privacy-sensitive
source cannot be bundled automatically, the manifest should still preserve dependency identities and state clearly what
material is missing.

### 12.10. Runtime Evidence Stress Tests

Generated-system tests must exercise evidence capture at the declared resource bounds.

A backend that promises required evidence cannot rely on an unchecked queue that silently drops records under burst
load. Tests should cover capacity exhaustion, retention expiry, concurrent occurrences, and optional operational tracing
being disabled.

The original Contract result must remain identical in all cases where diagnostic configuration is not itself part of the
selected Contract.

### 12.11. Diagnostic-System Fault Injection

High-reliability engineering treats the diagnostic mechanism as a component that can fail. Kontrakt should do the same.

Compiler tests can corrupt cached provenance, truncate machine-readable diagnostic payloads, or interrupt crash-bundle
creation. Runtime tests can simulate loss of an optional trace sink or failure of a retention backend. The system must
fail according to the authority of the affected diagnostic obligation and must not fabricate evidence from incomplete
material.

### 12.12. Performance Regression Tests

Diagnostic improvements are not free if they multiply compile latency or runtime allocation.

Compiler benchmarks should measure diagnostic-heavy invalid builds separately from successful builds. Source mapping,
conflict explanation, rendering, and machine-output serialization need their own profiles. Generated runtime benchmarks
should compare Contract Evidence capture against the same path with no evidence declaration so the marginal cost is
visible.

Tests should also compare supported diagnostic-depth profiles. Lower optional depth must reduce or bound the expected
work without changing Contract validity, required evidence, stable compiler error identity, or the semantic explanation
of the root cause. A deeper profile can add internal evidence and investigation artifacts; it cannot reveal that the
normal profile had reported a different Contract reason.

This makes diagnostic performance an engineered property rather than an anecdote.

---

## 13. Contract and Implementation Boundary

### 13.1. Contract Authority

The Contract owns the existence and meaning of Diagnostic Evidence definitions. It owns the exact source relation, the
closed evidence material, occurrence freezing, and any Retention guarantee that is explicitly declared.

The Contract does not own a compiler message, a JVM frame, a file format, a trace provider, a ring buffer, or a hardware
record layout.

### 13.2. Compiler Authority

The Kontrakt compiler owns diagnostics about compilation. It decides how parser, resolver, verifier, optimizer, backend,
and internal-error conditions are represented as structured tool diagnostics.

Those diagnostics can refer to Contract authorities because the compiler understands them. The reference does not make
the compiler diagnostic part of the user's Contract Machine.

### 13.3. Backend Authority

The backend owns the physical realization of required evidence within declared capability. It also owns optional
operational integrations with the JVM, OS, hardware, or external observability systems.

A backend may choose a more efficient representation without changing evidence semantics. It may not weaken an explicit
guarantee and call the change an optimization.

### 13.4. Presentation Authority

Human-readable messages, IDE layouts, terminal colors, JSON/SARIF-like interchange, dashboards, and debug UIs are
presentations over structured diagnostic material.

A presentation can omit low-value detail for readability if another machine consumer retains the complete structured
record required by its protocol. It cannot claim that omitted material never existed or that added explanatory text is
new Contract evidence.

### 13.5. Storage Authority

Files, databases, in-memory arenas, persistent RAM, remote collectors, and archival systems are storage mechanisms.

They may realize Retention when their guarantees are sufficient. Their physical lifecycle and index structure do not
become the Retention Contract.

---

## 14. Interaction with Existing Contracts

### 14.1. Failure

Failure remains the authoritative unsuccessful machine result. Diagnostic Evidence cannot redefine its source, failed
meaning, applicable context, or boundary.

A diagnostic failure is separate from the original Failure when an evidence obligation itself cannot be satisfied.
Unreachable later processing remains unexecuted rather than receiving synthetic failure-like statuses.

### 14.2. Publication

Diagnostic material remains internal unless Publication explicitly grants outward authority to the applicable evidence
source.

Internal availability, retention, operator access, or debugger visibility does not imply Publication.

### 14.3. Output

Once Diagnostic Evidence is publication-authorized, Output can expose only a strict projection of that authorized
material according to ADR-0059.

Output does not rename, redact, derive, or format new diagnostic facts. Consumer-specific transformation remains outside
the Core unless later Contract work explicitly adds another authority.

### 14.4. Policy and Governance

Policy may select different Diagnostic Evidence and Retention Contracts in different Policy Worlds because it already
selects combinations of one-dimensional Contracts.

Governance selects the applicable world. It does not dynamically edit an evidence definition inside a governed scope.
Operational tracing controls remain implementation controls unless represented through existing Contract selection.

### 14.5. Version

Evidence definition identity is Version-sensitive like other one-dimensional Contracts.

Retained occurrences decode against the definition that was applicable when they were established. A new compiler or
runtime cannot reinterpret historical evidence through a later Version.

### 14.6. Budget and Capacity

Required evidence capture consumes resources. Where Kontrakt owns the relevant resource region, Budget and Capacity
planning account for that realization cost.

Budget does not decide what evidence means. Capacity does not decide how long evidence must remain. They constrain the
physical realization that must satisfy those Contracts.

Optional operational diagnostics may have separate deployment budgets. Their exhaustion cannot silently remove required
Contract Evidence.

### 14.7. Whole Machine

Cross-Core diagnostic correlation may help reconstruct a system-level incident, but it does not create a hidden shared
Core or new causal Contract.

Each Core preserves its own evidence authority and outward boundary. External traces can be composed for investigation
without becoming Whole-Machine semantic communication.

---

## 15. Likely Document Split after the Umbrella Decision

This draft deliberately carries more detail than one final ADR should own.

The first likely document owns **Diagnostic Evidence Contract semantics and user-facing authoring**. It would retain the
rules for exact source binding, closed evidence material, frozen occurrence, distinction from Failure, Version and
Policy interaction, and the Publication/Output relationship.

A second document is likely to own **Retention** if lifecycle, persistence, eviction, and capability questions prove
independent enough to justify their own Contract. Retention already has a different authority because it governs later
availability rather than evidence meaning.

The compiler and generated-system implementation material can then live in a third ADR or compiler architecture design
document. That document would own structured compiler diagnostics, source provenance, incremental invalidation,
deterministic parallel merge, optimization remarks, crash reproduction, runtime evidence realization, and backend
correlation.

The split should follow authority rather than document length. If Retention remains small after Lifecycle is closed, the
first two semantic documents may stay together. If compiler diagnostics and generated-system operational diagnostics
diverge enough in implementation, they may become separate design documents without creating another Contract kind.

No split may erase the three-way distinction established here:

```text
user Contract diagnostic authority
    != Kontrakt compiler diagnostic authority
    != generated-system operational diagnostic authority
```

---

## 16. Deferred Decisions

The exact `.kontrakt` spelling for Diagnostic Evidence and Retention remains open. The semantic model must be accepted
before frontend convenience determines the shape.

The final set of diagnosable source categories also remains open. The theory permits evidence for non-Failure judgments,
but V1 may expose only the sources whose diagnostic material can be lowered deterministically with the current IR.

The source-owned declaration mechanism for additional diagnostic observations needs a separate frontend pass. The
important current restriction is that the material be finite, declarative, and owned by the source rather than produced
through arbitrary callback execution.

The exact Retention lifecycle vocabulary waits for the remaining Scope and Lifecycle work. This ADR does not invent JVM
process lifetime as a Contract scope.

Persistence across restart, host loss, power loss, or distributed failure remains a backend-capability and later
Lifecycle problem.

A universal Contract severity, confidence score, diagnostic coverage percentage, or partial-evidence enum is not added
here. Those concepts have value in particular engineering domains, but their meanings are not uniform enough to become
one generic Contract coordinate without a stronger use case.

The exact Publication selector for Diagnostic Evidence still requires a refinement to ADR-0058. ADR-0060 establishes the
boundary law but does not silently extend the existing Publication source grammar.

Generic redaction is also deferred. Strict projection and source minimization remain the current Contract tools. A later
privacy/confidentiality Contract may define stronger transformation authority if needed.

Automatic solver proof production, globally minimal conflict cores, proof-carrying diagnostics, and full translation
validation are not V1 requirements. The compiler architecture keeps a place for them.

Full persistent diagnostic query caching, remote diagnostic caches, distributed IDE diagnostics, and cross-build issue
matching are V2 or later compiler work.

Profile-guided diagnostic capture, anomaly-triggered runtime tracing, automatic pass bisection, and compiler-aware
reproducer minimization remain later optimizations over the V1 architecture.

The exact JVM integration with JFR, HotSpot, Graal, sanitizers, operating-system tracing, hardware RAS, or external
observability products is backend design. The canonical correlation seam must exist before choosing those adapters.

---

## 17. Consequences

### Positive

Kontrakt gains an accountable diagnostic model without turning diagnostics into a second judgment system. Failure and
other machine results keep their existing authority, while declared evidence can still be trustworthy Contract material
rather than best-effort logging.

The compiler gets a commercial-grade architecture in which diagnostics are structured products with source and semantic
provenance. That supports CLI, IDE, CI, machine output, optimizer explanation, incremental compilation, parallel work,
and crash reproduction without creating separate error systems for each consumer.

User-facing diagnostics can remain rich without exposing compiler internals. The compiler projects structured evidence
back into the vocabulary of the declared Contract Machine, so source locations and severity guide attention while the
actual explanation speaks in terms the user can change.

Diagnostic cost becomes an explicit engineering dimension. Compact references and lazy enrichment let normal compiler
and runtime paths remain small, while named deeper modes can spend additional work on the exact investigation that needs
it. Required Contract Evidence stays outside that optional trade-off.

Optimization remains free to change implementation shape because diagnosability is expressed as a preservation
obligation over meaning and provenance rather than as a requirement to keep particular JVM frames or IR nodes alive.

The V1 design remains open to V2 red/green reuse. Source presentation can be refreshed independently from semantic
results, and structured diagnostic products can later participate in persistent or remote caches without treating human
strings as compiler state.

Generated systems can provide a small guaranteed Contract Evidence path while retaining access to much richer JVM,
operating-system, hardware, and external diagnostics. Portability therefore does not require deliberately poor
observability.

Retention becomes an explicit availability guarantee instead of accidental storage behavior. Evidence can expire without
rewriting history, and optional operational logs can use bounded overwrite policies without pretending to satisfy a
stronger Contract.

### Negative

The compiler must build source provenance, structured diagnostics, deterministic merge, and preservation rules earlier
than a simple string-based implementation would require.

The compiler also needs a diagnostic planning boundary instead of a single global verbosity switch. Keeping user-facing
semantic projection, internal engineering evidence, and generated-system operational depth coherent adds design work to
frontend, pass, cache, backend, and tooling APIs.

Transform passes carry more obligations because source and diagnostic provenance can be invalidated even when semantic
IR remains legal. This adds verifier and testing work to optimization engineering.

A real evidence guarantee may require memory and retention capacity that a best-effort logger would avoid. Some backend
targets will have to reject stronger Diagnostic Evidence or Retention Contracts instead of pretending that ordinary
logging is sufficient.

The separation between Contract Evidence and operational evidence means users cannot turn an arbitrary production trace
into authoritative Contract material after the fact. Required information has to be declared at a source that can
actually guarantee it.

### Neutral

This ADR does not require every judgment to emit Diagnostic Evidence.

It does not require every retained evidence occurrence to be published outward.

It does not make stack traces, logs, telemetry, profiler events, crash dumps, or hardware records useless. They remain
important implementation diagnostics with explicit correlation to Contract occurrences where possible.

It does not choose the final number of ADRs. This document is intentionally the detailed pre-split strategy from which
the final authority-specific documents will be extracted.