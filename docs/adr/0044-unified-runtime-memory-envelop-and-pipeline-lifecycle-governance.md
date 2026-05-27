# ADR-0044: Unified Runtime Memory Envelope and Pipeline Lifecycle Governance

## Status

Draft

## Date

2026-05-27

## Related

- ADR-0041: Stable Metadata Identity, Digest/HID, and Protocol-Owned Interning
- ADR-0042: Mechanical Sympathy, Primitive Lifecycle, and Async Ownership Governance
- ADR-0043: Contract Graph Canonicalization, Sealed Structural References, and Incremental Identity Derivation
- ADR-0040: Deterministic Frozen Acquisition Pipeline, Explicit Readiness, and Memory-Disciplined Publication
- ADR-0035: Deterministic M:N Dispatch Lanes for Tier-2 Join Completion Delivery
- ADR-0034: Explicit Dual-Axis L2 Join Lifecycle State Machine and Single Terminalization Authority
- ADR-0032: Capacity Law, Resource Policy Resolution, Identity Hierarchy, and Zero-Residue Semantics
- ADR-0031: Cache-Blind Determinism and Tier-2 Governance
- `docs/design/stable-metadata-identity-protocol.md`
- `docs/design/protocol-owned-metadata-interning.md`
- `docs/constitution/compiler-core-protocols.md`

---

## 1. Context

Kontrakt now has several deterministic subsystems with their own resource laws.

These include:

- metadata identity canonicalization;
- protocol-owned metadata interning;
- mechanical primitive substrate lifecycle;
- frozen acquisition;
- contract graph canonicalization;
- incremental graph identity derivation;
- planning L1;
- L2 plan cache / plan interner;
- VM execution;
- reporting and diagnostics;
- async delivery/reclamation;
- quarantine and continuation scopes.

Each subsystem can be locally correct while still causing global resource failure if they do not share a unified runtime
memory envelope.

A system that separately admits each pipeline stage may still overcommit memory when the stages overlap.

This ADR defines the unified memory-envelope and pipeline lifecycle governance needed to make the entire runtime
deterministic, bounded, and failure-classified.

---

## 2. Problem

Without a unified memory envelope, Kontrakt risks the following failures.

### 2.1. Additive hidden memory

Each pipeline allocates its own staging and scratch memory as if it were alone.

The total run exceeds the process or configured resource envelope.

### 2.2. Double-booked transient reserves

Two subsystems assume they can reuse the same transient reserve.

At runtime, both need it at once.

### 2.3. Reclamation lag

A subsystem logically releases memory.

Another subsystem assumes that memory is physically reusable.

The backend or JVM has not actually made it reusable.

### 2.4. Priority inversion

A low-priority diagnostic or reporting path holds memory needed to fail closed, publish, or reclaim a higher-priority
identity or planning path.

### 2.5. Fallback cascade

An incremental path exceeds budget and falls back to full rebuild.

The full rebuild needs more memory than the failed incremental path and also fails.

### 2.6. Quarantine starvation

A repeated faulty source or domain slice consumes retry/quarantine/diagnostic memory repeatedly, starving healthy
scopes.

### 2.7. VM/reporting omission

Planning and identity budgets are controlled, but VM execution and reporting are admitted later without being included
in
the same run envelope.

### 2.8. Background reclaimer optimism

Retired epoch memory is assumed to become available quickly.

Readers, leases, or backend reclamation delay prevent reuse.

---

## 3. Decision

Kontrakt will introduce a unified runtime memory envelope.

Every admitted run, pipeline, or top-level orchestration scope must resolve one immutable memory-envelope snapshot
before
expensive work begins.

The envelope owns:

- total memory budget;
- stage-specific slices;
- transient reserve;
- retired memory reserve;
- diagnostic reserve;
- quarantine reserve;
- continuation/restart reserve;
- publication reserve;
- reclamation reserve;
- and failure-priority policy.

The envelope is not a loose advisory budget.

It is a deterministic admission contract.

A subsystem may consume only the slice, reserve, or continuation granted by the envelope.

A subsystem that cannot proceed within its granted envelope must fail closed, quarantine, degrade through a non-semantic
path, or return a caller-owned continuation request according to resolved policy.

---

## 4. Authority and Boundaries

ADR-0044 owns:

- cross-pipeline memory envelope resolution;
- memory slice allocation;
- transient reserve coordination;
- retired epoch reserve coordination;
- publication/diagnostic/quarantine/continuation reserve coordination;
- lifecycle phase ordering across pipelines;
- memory-priority and failure-priority policy;
- full rebuild preflight at system level;
- VM/reporting memory admission at system level;
- and cross-pipeline reclamation governance.

ADR-0044 does not own:

- canonical metadata bytes;
- HID derivation;
- metadata interning equality;
- primitive substrate mechanics;
- contract graph identity;
- L2 cache exact key law;
- VM bytecode/execution semantics;
- report format schema.

Ownership split:

| Surface                                  | Owner                                         |
|------------------------------------------|-----------------------------------------------|
| canonical metadata identity              | stable metadata identity protocol design note |
| metadata interning                       | protocol-owned metadata interning design note |
| primitive lifecycle / physical substrate | ADR-0042                                      |
| contract graph identity                  | ADR-0043                                      |
| L1 planning session budgets              | ADR-0032 and planning policy docs             |
| L2 cache/interner governance             | ADR-0031/0034/0035 and L2 design docs         |
| unified cross-pipeline memory envelope   | ADR-0044                                      |
| enforcement hooks                        | compiler constitution                         |

---

## 5. Vocabulary

### 5.1. Runtime envelope

A runtime envelope is the resolved immutable budget for a top-level execution unit.

Examples:

- one frozen acquisition;
- one planning run;
- one contract graph canonicalization run;
- one VM execution run;
- one report/replay materialization;
- one end-to-end compiler/runtime orchestration episode.

### 5.2. Slice

A slice is a named sub-budget inside the runtime envelope.

A slice is owned by a pipeline stage or function class.

### 5.3. Reserve

A reserve is memory that is not assigned to ordinary steady-state work but is held for publication, transient placement,
diagnostics, quarantine, continuation, or reclamation.

### 5.4. Retired memory

Retired memory is logically unreachable from new semantic operations but not yet proven physically reusable.

### 5.5. Reclaimable memory

Reclaimable memory is retired memory whose backend lifecycle proof says it can be reused inside the current envelope.

### 5.6. Continuation scope

A continuation scope is a new caller-owned scope admitted after a previous scope cannot continue within its envelope.

It is not an automatic expansion of the failed scope.

### 5.7. Emergency diagnostic reserve

Emergency diagnostic reserve is a small deterministic reserve for bounded failure reporting after ordinary diagnostics
are exhausted.

It must not be consumed by normal success paths.

---

## 6. Runtime Envelope Vector

A resolved runtime envelope MUST define at least:

``````text
totalEnvelopeBytes

metadataIdentityBytes
metadataInterningBytes
frozenAcquisitionBytes
contractGraphBytes
incrementalDerivationBytes
planningL1Bytes
l2CacheAndInternerBytes
vmExecutionBytes
reportingBytes

transientPlacementBytes
publicationBytes
retiredEpochBytes
reclamationScratchBytes
quarantineBytes
continuationBytes
diagnosticBytes
emergencyDiagnosticBytes

maxSimultaneouslyOpenScopes
maxContinuationScopes
maxQuarantinedScopes
maxRetiredEpochs
maxPublicationBarriers
maxFullRebuildPreflights
``````

All values are resolved integers.

All arithmetic is checked.

All relationships must be proven before the top-level envelope is admitted.

---

## 7. Envelope Feasibility Law

A runtime envelope is feasible only if all required slices and reserves fit inside the total envelope.

Required relationship:

``````text
sum(activeStageSlices)
+ transientPlacementBytes
+ publicationBytes
+ retiredEpochBytes
+ reclamationScratchBytes
+ quarantineBytes
+ continuationBytes
+ diagnosticBytes
+ emergencyDiagnosticBytes
    <= totalEnvelopeBytes
``````

If some slices are mutually exclusive by lifecycle, the envelope may use a deterministic phase-overlap matrix.

A phase-overlap matrix MUST define which slices may be simultaneously live.

It MUST be resolved before admission.

It MUST NOT be selected by runtime observation.

---

## 8. Phase-Overlap Matrix Law

Not all pipeline slices are live at the same time.

However, overlap must be explicit.

A resolved envelope MAY define:

``````text
phaseId
liveSlices[phaseId]
requiredReserves[phaseId]
entryPreconditions[phaseId]
exitPreconditions[phaseId]
``````

The implementation may reuse memory between phases only after:

- the previous phase has logically closed;
- all publication obligations are complete;
- all reader leases that can access the memory are closed or bounded;
- retired memory is accounted;
- and ADR-0042 backend evidence proves reuse or the retired bytes remain charged.

Forbidden:

``````text
phase A releases reference
-> phase B immediately assumes bytes are physically reusable
``````

Required:

``````text
phase A closes
-> retired accounting
-> backend reuse proof or continued retired charge
-> phase B admission
``````

---

## 9. Slice Ownership Law

Each slice has one owning subsystem.

The owning subsystem may subdivide its slice according to its own ADR/design law.

It may not borrow from another slice unless the runtime envelope declares a lawful transfer path.

A slice transfer requires:

- source slice;
- target slice;
- transfer amount;
- transfer reason;
- caller-owned authority;
- failure behavior;
- and deterministic ordering when multiple transfers contend.

Implicit borrowing is forbidden.

---

## 10. Metadata Identity and Interning Slices

The metadata identity slice covers:

- canonical material staging;
- canonical byte encoding;
- domain separation payload material;
- digest/HID descriptor material;
- TypeReference pre-SCC/final identity material;
- protocol golden-vector instrumentation where enabled.

The metadata interning slice covers:

- candidate records;
- staged verification payloads;
- provisional handles;
- collision verification records;
- cold collision structures;
- stable id assignment tables;
- publication metadata;
- intern-scope diagnostics.

The two slices may be co-admitted or separated.

They must remain semantically distinct.

Canonical byte protocol failure must not be hidden as interner failure.

Interner table failure must not reinterpret canonical bytes.

---

## 11. Frozen Acquisition Slice

The frozen acquisition slice covers:

- raw fact acquisition staging;
- readiness tracking;
- frozen fact image staging;
- freeze publication;
- frozen table metadata;
- acquisition diagnostics;
- adapter-handle erasure proof material.

Frozen acquisition may consume published metadata intern references only after their interning scope is published.

Frozen acquisition MUST NOT consume provisional metadata handles.

If frozen acquisition cannot obtain required metadata identity publication, frozen image publication fails closed.

---

## 12. Contract Graph and Incremental Slices

The contract graph slice covers:

- graph unit staging;
- structural/contextual identity material;
- sealed structural references;
- graph SCC sealing;
- graph snapshot/materialization;
- graph interning where graph-local;
- graph diagnostics.

The incremental derivation slice covers:

- affected-set traversal;
- dependency frontier;
- invalidation scratch;
- reused sealed references;
- full rebuild preflight;
- incremental diagnostics.

A full rebuild fallback is lawful only if the runtime envelope has preflighted the full rebuild slice and its overlap
with
existing live material.

The implementation MUST NOT enter full rebuild merely because incremental budget failed.

---

## 13. Planning L1 Slice

The planning L1 slice covers:

- planner session arenas;
- primitive ledgers;
- explicit frame stacks;
- local deterministic state machines;
- zero-residue rollback material;
- planning diagnostics;
- local verification material.

Planning L1 may consume stable metadata and graph references.

It may not consume pre-publication metadata candidates, provisional handles, or graph SCC-local temporary references.

Planning failure must return memory according to zero-residue semantics and ADR-0042 lifecycle rules.

---

## 14. L2 Cache and Interner Slice

The L2 slice covers:

- PlanCacheKey material where L2-owned;
- L2 join state;
- in-flight slots;
- waiters;
- builder handles;
- dispatch-lane infrastructure;
- L2 exact-key verification material;
- L2 diagnostics;
- optional PlanCacheKey interning if ratified.

L2 cache warmness is not memory-envelope authority.

L2 may be bypassed, degraded, or circuit-open under its governance policy, but such behavior must not alter metadata
identity, graph identity, or planning semantics.

---

## 15. VM Execution Slice

The VM execution slice covers:

- execution frames;
- runtime state-machine material;
- fixture state;
- controlled runtime allocations where admitted;
- VM diagnostics;
- execution trace material where enabled;
- resource cleanup material.

VM execution must be admitted before it starts.

VM execution MUST NOT borrow from metadata/interner/frozen/planning/reporting slices implicitly.

If execution requires additional memory beyond its slice, it must use caller-owned continuation, fail closed, or degrade
through a non-semantic path according to resolved policy.

---

## 16. Reporting and Diagnostics Slice

The reporting slice covers:

- report event staging;
- trace/journal material;
- diagnostic summaries;
- bounded failure evidence;
- artifact manifest staging;
- report publication buffers.

Diagnostics are important but not unbounded.

The reporting slice must define:

- success-path report budget;
- failure-path diagnostic budget;
- emergency diagnostic reserve;
- truncation policy;
- redaction policy where applicable;
- and publication behavior.

If ordinary diagnostics are exhausted, the system may use emergency diagnostic reserve only for bounded terminal failure
summaries.

Emergency reserve must not be consumed by success-path reporting.

---

## 17. Transient Reserve Law

Transient reserve covers temporary simultaneous-liveness events.

Examples:

- segment/page directory transition;
- publication barrier material;
- migration metadata;
- snapshot/materialization overlap;
- verification scratch;
- quarantine transfer;
- report artifact finalization.

Transient reserve is high-water reserve, not the sum of all sequential events.

However, it may be reused only after the prior event's memory is proven reusable or remains charged as retired memory.

Runtime memory release is not assumed from logical reference dropping.

---

## 18. Retired Epoch Reserve Law

Retired memory remains charged until proven reclaimable.

A runtime envelope MUST reserve space for retired epochs when ADR-0042 reader leases or async reclaimers may delay
reuse.

The retired epoch reserve must cover:

- maximum concurrently retired slabs/pages/segments;
- maximum reader epoch delay;
- maximum unpublished retired diagnostics;
- maximum reclaimer backlog;
- backend allocator retention where applicable.

If retired memory exceeds reserve, the system must apply resolved policy:

- stop admitting new scopes;
- force safe-point convergence where lawful;
- quarantine offending source/scope;
- fail current scope closed;
- or return caller-owned continuation request.

It MUST NOT rely on GC timing.

---

## 19. Publication Reserve Law

Publication reserve covers memory required to make a stage visible safely.

A stage may not consume all remaining memory before publication if it still needs publication metadata, integrity proof,
diagnostics, or safe-publication structures.

Each stage that publishes must declare:

- publication metadata bytes;
- integrity proof bytes;
- compatibility proof bytes;
- publication barrier bytes;
- rollback metadata bytes;
- publication diagnostics bytes.

If publication reserve is unavailable, the stage must fail before partial visibility.

---

## 20. Quarantine Reserve Law

Quarantine is a bounded containment mechanism.

Quarantine reserve covers:

- quarantine state;
- source/domain/scope identifiers;
- bounded diagnostic summaries;
- isolation metadata;
- cleanup/reclaim handles;
- blocked-continuation records where applicable.

Quarantine MUST NOT hold arbitrary original input, full graph dumps, unbounded canonical bytes, or backend handles
unless
explicitly budgeted and ratified.

Repeated quarantine pressure must be attributed to logical source/domain/scope before physical worker/lane quarantine.

---

## 21. Continuation and Restart Reserve Law

Continuation reserve covers caller-owned scope continuation.

It is used when a lawful pipeline cannot continue inside its current scope but may request a separately admitted scope.

The current subsystem MUST NOT open the continuation scope by itself.

It returns a bounded continuation request.

The caller-owned boundary may admit a new scope only after resolving a new envelope or a sub-envelope.

Continuation reserve must cover:

- continuation request record;
- summary of completed sealed material;
- unresolved boundary references;
- restart-safe diagnostics;
- and new-scope preflight material.

---

## 22. Memory Priority Law

When memory pressure occurs, resolution order must be deterministic.

The runtime envelope MUST define priority among:

1. semantic publication safety;
2. rollback / fail-closed safety;
3. reclamation safety;
4. emergency diagnostics;
5. quarantine containment;
6. continuation request;
7. ordinary diagnostics;
8. optional cache retention;
9. optional physical acceleration structures;
10. speculative work.

Optional caches and accelerators must yield before semantic publication safety is compromised.

L2 cache retention is lower priority than correctness.

Reporting success-path detail is lower priority than fail-closed diagnostics.

---

## 23. Admission Modes

A pipeline may use one of these memory admission modes:

``````text
AOT_EXACT
    all required memory known before stage starts.

PREPASS_EXACT
    deterministic prepass computes exact budget.

BOUNDED_STREAMING
    stage admits discovered work under strict caps.

INCREMENTAL_AFFECTED_SET
    only dirty/affected work admitted under traversal and staging caps.

CONTINUATION_REQUEST
    current scope cannot proceed; caller must admit a new scope.

NO_ADMISSION
    stage is disabled or bypassed by resolved policy.
``````

The mode is selected before stage admission.

It MUST NOT change opportunistically after budget pressure is observed.

---

## 24. Full Rebuild Fallback Preflight Law

Full rebuild fallback is not free.

Before switching from incremental mode to full rebuild, the caller-owned boundary MUST preflight:

- full rebuild candidate count;
- full rebuild canonical byte budget;
- full rebuild graph traversal budget;
- full rebuild interning budget;
- full rebuild planning budget;
- transient overlap with existing live material;
- retired memory impact;
- reporting/diagnostic budget;
- and publication reserve.

If full rebuild preflight fails, the implementation must fail or quarantine according to resolved policy.

It MUST NOT begin full rebuild and discover failure by OOM.

---

## 25. Lifecycle Closure Law

A stage may release its slice only after:

- no consumer-visible references require the staging material;
- publication or rollback is complete;
- reader leases are closed or bounded;
- async reclamation has taken ownership where applicable;
- retired bytes are accounted;
- diagnostics are bounded;
- and the phase-overlap matrix admits the next phase.

Lifecycle closure is a protocol event, not a garbage collector event.

---

## 26. Cross-Pipeline Non-Interference Law

One pipeline's optimization must not weaken another pipeline's identity or lifecycle law.

Examples:

- reporting truncation must not alter identity;
- L2 cache bypass must not alter planning result;
- VM execution allocation pressure must not retroactively change metadata interning;
- frozen acquisition failure must not publish partial metadata identity;
- contract graph fallback must not reuse stale structural references;
- physical substrate compaction must not change stable ids.

---

## 27. Diagnostics Law

Diagnostics are bounded protocol outputs.

A runtime envelope MUST define:

- max diagnostic bytes per stage;
- max diagnostic bytes per failure class;
- emergency diagnostic bytes;
- truncation order;
- redaction policy where applicable;
- and diagnostic publication behavior.

Diagnostic failure must not cause unbounded allocation.

If diagnostic budget is exhausted, the system emits a bounded summary or fails silently according to resolved policy.

It MUST NOT throw unbounded exception stacks on hot failure paths.

---

## 28. Compliance Rules

1. Every top-level runtime scope must have a resolved immutable memory envelope.
2. Stage slices and reserves must fit inside the envelope or declared phase-overlap matrix.
3. Slice borrowing requires explicit transfer law.
4. Logical release is not physical reclamation.
5. Retired memory remains charged until reuse is proven.
6. Publication reserve must be held before publication starts.
7. Full rebuild fallback requires preflight.
8. VM execution memory must be admitted before execution begins.
9. Reporting and diagnostics are bounded.
10. Emergency diagnostic reserve is not success-path reporting budget.
11. Continuation scopes are caller-owned.
12. Subsystems must not silently expand semantic scope.
13. L2 cache retention yields before correctness.
14. Optional physical accelerators yield before semantic publication safety.
15. Quarantine reserve is bounded and source/domain/scope attributed where possible.
16. GC timing is not a memory-envelope event.
17. All envelope arithmetic is checked.
18. Runtime profiling must not change envelope policy inside an admitted scope.

---

## 29. Required Golden Vectors / Fixtures

A released implementation must provide deterministic fixtures for:

- envelope feasibility success;
- envelope feasibility overflow;
- phase-overlap reuse success;
- phase-overlap reuse rejected due to retired bytes;
- metadata identity + interning co-admission;
- frozen acquisition blocked on unpublished metadata interning;
- incremental affected-set budget exhaustion;
- full rebuild preflight success;
- full rebuild preflight failure;
- VM execution admission success;
- VM execution admission failure;
- reporting ordinary diagnostic truncation;
- emergency diagnostic reserve use;
- quarantine source attribution;
- continuation request without automatic scope open;
- retired epoch reserve exhaustion;
- L2 cache retention sacrificed under pressure;
- publication reserve protected under pressure;
- physical accelerator disabled under pressure;
- deterministic priority order under simultaneous pressure.

---

## 30. Architecture Tests

Architecture tests SHOULD assert:

1. no stage allocates outside its admitted envelope;
2. no subsystem opens continuation scope by itself;
3. no phase reuses retired memory without proof;
4. no publication begins without publication reserve;
5. no full rebuild begins without preflight;
6. no VM execution begins without memory admission;
7. reporting cannot consume emergency reserve on success path;
8. L2 cache retention can be reduced without semantic change;
9. physical accelerator disablement cannot change identity;
10. diagnostics remain bounded under repeated failures;
11. stage closure waits for reader-lease/reclamation accounting where applicable;
12. all envelope arithmetic is checked.

---

## 31. Alternatives Considered

### 31.1. Keep per-stage memory budgets independent

Rejected.

Independent budgets can be locally correct but globally overcommitted.

### 31.2. Let the JVM/GC be the global memory governor

Rejected.

GC timing is not deterministic protocol governance.

### 31.3. Let L2 eviction handle memory pressure

Rejected.

L2 eviction is useful but cannot govern metadata identity, frozen acquisition, VM execution, publication reserve, or
diagnostic reserve.

### 31.4. Allow fallback to full rebuild without preflight

Rejected.

Full rebuild can require more memory than the incremental path that failed.

### 31.5. Treat diagnostics as unbounded because failures are rare

Rejected.

Adversarial or pathological input can make failures frequent.

Diagnostics must be bounded.

---

## 32. Consequences

Positive:

- cross-pipeline memory accounting becomes explicit;
- fallback and continuation are deterministic;
- VM and reporting are included in the same resource model;
- physical reclamation lag is accounted;
- L2 and optional accelerators can yield without semantic drift;
- full rebuild fallback becomes safe rather than hopeful;
- quarantine and diagnostics become bounded.

Negative:

- policy resolution becomes more complex;
- every major pipeline must declare memory slices;
- some memory may be reserved but unused in successful runs;
- implementation must provide more preflight and fixture coverage;
- documentation must coordinate ADR-0041/0042/0043 with this ADR.

---

## 33. Final Rule

Kontrakt runtime memory is a governed envelope, not a collection of independent allocations.

Every pipeline stage must be admitted into a resolved immutable memory envelope.

Semantic publication, fail-closed rollback, reclamation, quarantine, continuation, VM execution, L2 retention, and
reporting must be budgeted before they are needed.

No subsystem may buy progress with unbounded memory, GC timing, implicit borrowing, or silent scope expansion.