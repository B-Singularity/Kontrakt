# ADR-0060: Diagnostic Evidence and Retention Contract

## Status

Proposed

## Date

2026-08-23

## Related

- `docs/the-most-important-thing/what-contract-is.md`
- `docs/constitution/canonical-ir-stage-and-lowering-protocol.md`
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

---

## 1. Context

Kontrakt already requires the Contract Machine to state its authoritative results explicitly. ADR-0057 applies that rule
to Failure and deliberately removes diagnostic material from Failure identity. Failure states what required machine
meaning was not satisfied. Diagnostic material may explain that result, but it must not become a second authority that
decides what the Failure meant.

The same separation is required outside Failure. A successful judgment may need explanation, a State-Machine refusal may
need supporting material, and a Realization Failure may need evidence from the physical execution boundary. These cases
do not share one result kind, but they all create a need to relate explanation material to an exact diagnosable
occurrence without transferring authority to the diagnostic system.

Kontrakt has three machine axes that matter here: Contract, State Machine, and Realization. Diagnostic Evidence may
relate to any of them, but this ADR defines only the Contract meaning of Diagnostic Evidence and Retention. It does not
define how the Kontrakt compiler diagnoses source programs or how a generated backend captures JVM, operating-system,
hardware, profiler, tracing, or crash material. Those implementation responsibilities are separated into ADR-0061 and
ADR-0062.

A rich Contract creates an unusual opportunity. The machine already knows the exact authority, applicable world,
selected Version, canonical material, and judgment that produced a result. Diagnostic Evidence therefore does not need
to reconstruct Contract meaning from logs or stack traces. It can be bound directly to the semantic occurrence that it
explains.

That strength also creates cost. Declared evidence may require values to be frozen at the judgment boundary and retained
after the source result is established. The Contract must say what is guaranteed without turning optional observability
into mandatory machine work. Required evidence and optional diagnostic depth therefore have to remain distinct.

## 2. Scope and Authority

ADR-0060 owns the meaning of a Diagnostic Evidence definition, the occurrence established from that definition, the
relation between evidence and the source occurrence, and the meaning of an explicit Retention guarantee. It also owns
the user-facing Contract distinction between required evidence and optional diagnostic depth.

The Kontrakt compiler may use the Contract definitions in this ADR while checking a program, but compiler errors and
warnings are tool diagnostics rather than occurrences of the user's Diagnostic Evidence Contract. ADR-0061 owns that
tool architecture.

A generated Contract Machine may realize required evidence with backend-specific storage and may correlate the same
occurrence with richer operational diagnostics. Those mechanisms remain realization choices unless this ADR explicitly
makes a material part of Contract meaning. ADR-0062 owns that architecture.

## 3. Decision Drivers

Diagnostic Evidence must explain an already-established machine result rather than create a competing result. The source
that judged the original obligation remains authoritative even if diagnostic capture later fails or no human-readable
explanation is available.

An evidence occurrence needs exact provenance. Material is not Contract Evidence merely because it was observed near a
Failure in time or appears in the same runtime stack. The evidence definition must resolve to an exact diagnosable
source, and the occurrence must remain related to the exact source occurrence it explains.

Contract-required evidence is closed. If a definition requires a complete shape, a partial capture cannot silently
become a weaker valid occurrence. Optional operational material may be incomplete, but that incompleteness cannot be
used to satisfy a stronger Contract Evidence obligation.

Material that describes a value at the judgment boundary must come from that boundary. Reading mutable state later is
reconstruction. Later investigation may still be useful, but a reconstructed value cannot be presented as the frozen
observation used by the original judgment.

Diagnostic Evidence must not steal semantic coordinates from the source authority. Failure `applicable context` remains
Failure meaning where ADR-0057 requires it. Evidence may refer to that established material without redefining it.

The Contract remains backend-neutral. A `Throwable`, stack frame, native address, profiler sample, operating-system
trace, hardware error record, or telemetry packet can be useful realization evidence without becoming required Contract
vocabulary.

A diagnostic depth choice may reduce optional observation cost. It cannot change Contract validity, erase an established
Failure, or turn declared evidence into sampled best-effort data.

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

## 7. Interaction with Existing Contracts

### 7.1. Failure

Failure remains the authoritative unsuccessful machine result. Diagnostic Evidence cannot redefine its source, failed
meaning, applicable context, or boundary.

A diagnostic failure is separate from the original Failure when an evidence obligation itself cannot be satisfied.
Unreachable later processing remains unexecuted rather than receiving synthetic failure-like statuses.

### 7.2. Publication

Diagnostic material remains internal unless Publication explicitly grants outward authority to the applicable evidence
source.

Internal availability, retention, operator access, or debugger visibility does not imply Publication.

### 7.3. Output

Once Diagnostic Evidence is publication-authorized, Output can expose only a strict projection of that authorized
material according to ADR-0059.

Output does not rename, redact, derive, or format new diagnostic facts. Consumer-specific transformation remains outside
the Core unless later Contract work explicitly adds another authority.

### 7.4. Policy and Governance

Policy may select different Diagnostic Evidence and Retention Contracts in different Policy Worlds because it already
selects combinations of one-dimensional Contracts.

Governance selects the applicable world. It does not dynamically edit an evidence definition inside a governed scope.
Operational tracing controls remain implementation controls unless represented through existing Contract selection.

### 7.5. Version

Evidence definition identity is Version-sensitive like other one-dimensional Contracts.

Retained occurrences decode against the definition that was applicable when they were established. A new compiler or
runtime cannot reinterpret historical evidence through a later Version.

### 7.6. Budget and Capacity

Required evidence capture consumes resources. Where Kontrakt owns the relevant resource region, Budget and Capacity
planning account for that realization cost.

Budget does not decide what evidence means. Capacity does not decide how long evidence must remain. They constrain the
physical realization that must satisfy those Contracts.

Optional operational diagnostics may have separate deployment budgets. Their exhaustion cannot silently remove required
Contract Evidence.

### 7.7. Whole Machine

Cross-Core diagnostic correlation may help reconstruct a system-level incident, but it does not create a hidden shared
Core or new causal Contract.

Each Core preserves its own evidence authority and outward boundary. External traces can be composed for investigation
without becoming Whole-Machine semantic communication.

---

## 8. V1 Boundary

V1 must represent Diagnostic Evidence definitions as explicit Contract material and keep definition identity separate
from runtime occurrence. It may support only the diagnosable sources that the current canonical IR and backend can
realize deterministically. A smaller V1 source set is acceptable; a hidden logging fallback is not.

The first frontend does not need the final convenience syntax. It must still resolve evidence definitions to exact
authorities, preserve the distinction between required evidence and optional diagnostic depth, and leave generated host
APIs outside Contract authority.

V1 Retention remains limited to lifecycle semantics that are already explicit. Stronger persistence guarantees are not
inferred from JVM process lifetime, file storage, or an operational collector.

## 9. Verification Requirements

### 9.1. Definition Verification

A Diagnostic Evidence definition must resolve to an exact source, a closed diagnosable material set, and the applicable
Version and Policy World. Hidden implementation state or executable callbacks cannot complete the definition.

Retention is checked only after its evidence source is valid. An unsupported lifecycle boundary or backend capability
does not imply a weaker guarantee.

### 9.2. Occurrence Conformance

An implementation that claims to establish Contract Evidence must be checkable against the canonical definition. The
exact physical representation may vary, but the decoded occurrence must refer to the exact source occurrence and contain
the complete declared material.

## 10. Open Decisions

### 10.1. Exact `.kontrakt` Diagnostic Evidence Syntax

The semantic model is decided before frontend convenience. The final spelling must not introduce executable diagnostic
callbacks or make human message text part of Contract identity.

### 10.2. Final V1 Diagnosable Source Set

Diagnostic Evidence is not limited to Failure in the theory. V1 may expose only source categories whose evidence can be
resolved and lowered deterministically with the current IR.

### 10.3. Source-Owned Additional Diagnostic Observation Syntax

Any additional diagnosable observation must remain finite, declarative, and owned by its source authority.

### 10.4. Retention Lifecycle Vocabulary

The exact lifecycle vocabulary waits for the remaining Scope and Lifecycle work. JVM process lifetime is not inferred as
a Contract scope.

### 10.5. Persistence across Restart and Stronger Failure Domains

Persistence across process restart, machine restart, power loss, host loss, or distributed failure requires explicit
lifecycle and backend capability semantics.

### 10.6. Diagnostic Evidence Publication Selector Refinement

Diagnostic Evidence cannot become outward authority merely because it is retained or internally available. The exact
Publication selector extension remains to be designed against ADR-0058.

### 10.7. Generic Severity, Confidence, Coverage, and Partial-Evidence Taxonomies

No universal Contract taxonomy is introduced merely because particular compilers, safety systems, hardware formats, or
observability systems use such concepts.

### 10.8. Generic Redaction or Diagnostic Transformation Authority

Strict projection and source minimization remain the current Contract tools. A stronger transformation or
confidentiality authority is not defined here.

## 11. Consequences

### Positive

Kontrakt gains an accountable diagnostic model without turning diagnostics into a second judgment system. Failure and
other machine results keep their existing authority, while declared evidence can be trustworthy Contract material rather
than best-effort logging.

The explicit Contract pipeline gives Diagnostic Evidence a stronger source relation than ordinary post-hoc
observability. Evidence can refer to the exact semantic occurrence it explains instead of reconstructing authority from
runtime shape.

A user can require a guaranteed evidence core without making every rich diagnostic facility part of Contract meaning.
Policy, Version, Publication, Output, Budget, and Capacity keep their existing responsibilities.

Retention becomes an availability guarantee rather than accidental storage behavior. Expiry or eviction cannot rewrite
the semantic history of the source occurrence.

### Negative

A real evidence guarantee can require memory, capture work, and retention capacity that a best-effort logger would
avoid. Some backend targets may have to reject stronger contracts instead of silently degrading them.

The distinction between Failure meaning, applicable context, Diagnostic Evidence, optional operational observation, and
Retention creates more semantic boundaries that the frontend and verifier must preserve correctly.

### Neutral

This ADR does not require every judgment to emit Diagnostic Evidence.

It does not require every retained occurrence to be published outward.

It does not define Kontrakt compiler diagnostics or generated-system operational diagnostic mechanisms.