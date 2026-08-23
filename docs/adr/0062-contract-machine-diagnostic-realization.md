# ADR-0062: Contract-Machine Diagnostic Realization and Operational Evidence Architecture

## Status

Proposed

## Date

2026-08-23

## Related

- `docs/todo/kontrakt-v1-commercial-compiler-foundation-candidate-architecture.md`
- `docs/constitution/canonical-ir-stage-and-lowering-protocol.md`
- ADR-0060: Diagnostic Evidence and Retention Contract
- ADR-0061: Kontrakt Compiler Diagnostic Architecture
- ADR-0057: Failure Contract, Explicit Machine Failure, Attribution, and Realization Boundary
- ADR-0056: Governance Contract, Policy-World Control, and Selection Boundary
- ADR-0055: Whole Machine, Pipeline Composition, and Contract Concurrency

---

## 1. Context

ADR-0060 defines what Diagnostic Evidence means as Contract material. A generated Contract Machine still needs a
physical architecture that can establish required evidence cheaply and can correlate the same machine occurrence with
richer implementation diagnostics when investigation needs them.

This responsibility is distinct from ADR-0061. The Kontrakt compiler diagnoses the program it is compiling. ADR-0062
concerns the production system produced by that compiler. Its evidence sources may include generated contract-machine
code, user realization code, the JVM, JIT compiler, native libraries, operating system, storage stack, hardware, or
external observability systems.

The generated system must therefore preserve two properties at once. Required Contract Evidence cannot become sampled
best-effort telemetry, while optional operational observation must be aggressively selective so diagnostics do not
dominate runtime cost. Stable correlation between the two planes is more important than forcing them into one schema.

## 2. Decision Drivers

Required Contract Evidence and optional operational diagnostics have different guarantees. A production backend cannot
use a profiler or trace sampler as the sole realization of evidence that ADR-0060 requires for every applicable
occurrence.

Portability is not an excuse for weak observability. Generated artifacts need stable correlation points so a Contract
occurrence can be related to JVM, native, operating-system, hardware, or external diagnostic material without giving
those systems Contract authority.

Runtime diagnostics must be aggressively lightweight by default. Cheap stable anchors can remain always available, while
stack walking, detailed tracing, large snapshots, JIT graph dumps, or external collection are enabled only for an exact
trigger or selected operational depth.

Loss and uncertainty are themselves observable properties of optional evidence. Buffer overflow, dropped events,
unavailable fields, incomplete symbolization, and backend capture failure cannot be normalized into complete-looking
data.

Generated optimization may change physical shape, but it cannot remove the ability to establish required evidence. If an
optimization fuses or removes gates, exact occurrence attribution still has to survive.

The diagnostic mechanism can fail. Backend tests therefore treat evidence capture, retention handoff, correlation, and
optional diagnostic adapters as components with their own failure modes rather than assuming observation is infallible.

## 3. Generated User-System Diagnostic Architecture

### 3.1. Generated Diagnostics Have Two Planes

The generated system has a Contract Evidence plane and an operational diagnostic plane.

The Contract Evidence plane realizes declarations from the user's Contract. Its capture and retention guarantees are
part of machine behavior and cannot be weakened by an operator's tracing preference.

The operational plane contains backend observations that help engineers investigate physical execution. It may use JVM
recording, stacks, profilers, operating-system events, hardware records, network traces, or external telemetry. Those
observations may be sampled, unavailable, target-specific, or intentionally disabled.

The planes can be correlated. They cannot be treated as interchangeable.

### 3.2. Contract Evidence Uses a Small Generated Kernel

V1 should realize required Diagnostic Evidence through compact generated machinery close to the owning judgment.

The hot path should capture only the material required by the selected evidence definition and append or publish it into
a bounded backend-owned representation. Human formatting remains cold. Reflection, runtime annotation lookup, arbitrary
object serialization, and stack walking must not be required to establish ordinary Contract Evidence.

The compiler already knows the exact source definition and closed coordinate set. It can specialize generated capture
for those coordinates instead of constructing a generic map at runtime.

This keeps the guarantee compatible with the broader Kontrakt optimization strategy: closed Contract material is lowered
into compact target-friendly machinery, while rendering and exploratory analysis stay off the hot path.

### 3.3. Generated Optimization Must Preserve Evidence Observability

A generated gate can be fused, specialized, or simplified only if its required evidence remains attributable to the
original Contract source.

If two checks are fused into one branch, a rejection still needs to identify which exact judgment established the result
and capture the material required by that judgment's evidence definition. If a constant specialization removes a value
from runtime computation, the compiler can use the constant canonical value instead of retaining a dead runtime object.

This means diagnostic preservation can sometimes make generated code cheaper rather than more expensive. The compiler
should preserve meaning, not obsolete implementation structure.

### 3.4. Contract Evidence Is Not Sampled

Sampling is useful for high-volume operational tracing. It is not a valid implementation of a required Diagnostic
Evidence occurrence.

If a declared evidence obligation applies to every qualifying source occurrence, every such occurrence must be handled
according to the Contract. A backend that can only provide one-percent sampling does not satisfy that Contract.

A later Contract could explicitly define a statistical diagnostic obligation if the product ever needs one. This ADR
does not infer statistical semantics from an observability sampler.

### 3.5. Operational Evidence May Use Selective Capture

Rich runtime evidence should be acquired according to explicit operational policy and cost.

A low-cost baseline may retain stable Contract occurrence IDs, coarse phase or boundary markers, and a small ring of
runtime events. When an anomaly is detected, the backend can temporarily enable deeper JFR events, method profiles,
selected object traces, heap or thread dumps, or another target-specific mechanism.

This strategy is preferable to permanent maximum-detail instrumentation because it preserves production performance
while keeping a path to deep investigation.

Operational capture policy is not a Contract Policy World unless the user explicitly declares a Contract obligation that
uses it. It belongs to deployment and backend tooling.

### 3.6. Current Diagnosis and Fault-Time Snapshot Are Different

A diagnostic command executed now may inspect current state. A fault-time evidence occurrence describes state captured
when the relevant event was judged.

The runtime must not blur those two sources. A health endpoint can say that the subsystem is healthy now while a
retained fault-time snapshot shows why it failed earlier. Both are useful.

The distinction also matters for repair. Recovery may mutate the state immediately after a Failure. Reading the repaired
state later cannot substitute for evidence of the failed state.

### 3.7. Correlation Does Not Create Causality

The generated system should expose stable correlation identities that let tools connect a Contract occurrence with
backend traces.

An Interaction or other active boundary may carry a run-scoped correlation token. Contract Evidence can refer to the
source occurrence. JVM or operating-system events can attach the same token where the backend controls both sides.

Sharing a token does not prove that two events caused one another. Causal authority still comes from explicit machine
dependency or a diagnostic analysis that can establish the relation. This rule is critical in distributed systems where
trace adjacency is often mistaken for semantic dependency.

### 3.8. Backend Event Time Does Not Become Contract Time

JVM events, OS traces, hardware records, and remote collectors can use different clocks.

A backend may preserve timestamps and clock-domain metadata to support investigation. Kontrakt must not sort Contract
meaning by those timestamps or infer an authoritative whole-machine order from them.

Where the Contract itself includes an explicit time coordinate, diagnostic correlation can relate an operational event
to that coordinate. It cannot substitute a profiler timestamp for a Contract time source without an explicit lowering
rule.

### 3.9. Operational Diagnostics Must Expose Loss and Unavailability Honestly

High-volume buffers overflow. Trace providers can be disabled. Native symbolization can fail. Hardware records may mark
fields invalid. A crash can happen before a dump is complete.

The operational diagnostic plane should preserve these facts rather than fill missing fields with defaults. Where the
underlying facility provides lost-event counts, overflow markers, field-validity bits, or capture-status information,
Kontrakt adapters should keep that information available to engineering tools.

This metadata describes the operational evidence quality. It does not create a universal Contract `quality` enum.

### 3.10. JVM Recording Is a Backend Capability

The JVM backend can use JVM Flight Recorder and related runtime facilities for optional operational evidence.

JFR is attractive because event definitions, event settings, recording lifetime, and retention are separable. Expensive
event work can be guarded so disabled recordings do not pay full materialization cost. Recordings can be bounded by age
or size without changing Contract Evidence semantics.

Kontrakt should attach stable Contract correlation metadata only where doing so is cheap and safe. A JFR event type or
recording file is not a canonical Contract definition.

### 3.11. JIT Compiler Diagnostics Are Useful for Realization Investigation

A JVM backend may rely on HotSpot or Graal compilation decisions that affect generated-code performance.

The runtime can expose JIT phase, compilation, deoptimization, or uncommon-trap information through backend diagnostics.
Graal-style retry of a failed compilation with a deeper diagnostic mode demonstrates a useful pattern: the normal path
stays cheap, while only the failing compilation is rerun with graph dumps and richer evidence.

Kontrakt should preserve enough generated-method and canonical-authority mapping to connect those JIT records to
Kontrakt-owned generated machinery. It should not promise that a specific JIT compiler, graph format, or deoptimization
reason exists on every backend.

### 3.12. Sanitizers Are Diagnostic Realizations, Not Production Contract Evidence by Default

Compiler instrumentation systems such as memory and race sanitizers show the trade-off clearly. They can provide highly
valuable allocation history, conflicting accesses, or thread creation paths, but their execution and memory overhead can
be substantial. Partial instrumentation can also reduce diagnostic accuracy.

Kontrakt may integrate sanitizer-enabled builds into verification and test products. The Contract cannot assume the same
evidence exists in an ordinary production artifact unless the backend explicitly realizes such a guarantee.

This keeps heavy dynamic diagnosis available without forcing its cost into every deployment.

### 3.13. Operating-System Tracing Is Supplemental Evidence

Operating systems offer provider-based event tracing, ring buffers, health reporters, and crash storage. These
facilities can expose scheduling, I/O, driver, kernel, or process behavior that Kontrakt cannot derive from Contract IR.

Adapters should preserve source identifiers and loss metadata when possible. They may use target-specific filters to
collect only events correlated with the generated system.

An OS trace cannot establish a Contract Failure merely because it observed an error code. The Contract source must still
establish its own result if enough machine authority remains.

### 3.14. Hardware and Firmware RAS Form Another Evidence Layer

Modern platforms may report memory, processor, PCIe, CXL, firmware, or other hardware errors through structured records.
Those records commonly distinguish the error record identity from sections containing source-specific information. They
also distinguish fields that are valid from fields that were not captured, and some formats report overflow when earlier
error information may have been lost.

The collection timestamp may describe when system software gathered the record rather than when the physical fault
actually occurred. This is exactly why backend diagnostic adapters must preserve provenance and timing semantics instead
of flattening every low-level record into `hardware error at time T`.

Kontrakt V1 does not need to implement every RAS adapter. The generated-system architecture must leave a correlation
seam so future backends can attach this evidence without changing Contract Failure meaning.

### 3.15. Crash-Persistent Operational Evidence Is a Backend Feature

Some operating systems can place crash logs or traces in reserved persistent memory so they survive reboot. Similar
mechanisms exist in embedded and spacecraft systems because the most useful evidence can disappear with volatile state.

A JVM backend may expose such a facility when the platform supports it. This does not mean the Contract Retention model
implicitly promises reboot survival. Strong persistence remains an explicit capability decision.

The distinction lets an operator gain post-crash evidence opportunistically without misleading Contract users about the
guarantees of another target.

### 3.16. External Observability Platforms Remain Outside the Core

A tracing collector, log service, metric backend, or incident-analysis system can correlate operational evidence across
many processes.

Kontrakt may emit stable machine-readable adapters for those systems. The external platform remains outside the Core
just like another storage or network adapter. Its sampling, retention, aggregation, redaction, and query behavior cannot
silently become Contract meaning.

If an outside diagnostic service feeds new material into another Kontrakt machine, that material must enter through that
machine's normal Input boundary.

### 3.17. Whole-Machine Correlation Does Not Flatten Core Boundaries

A Whole Machine can include several Cores whose outputs and inputs are connected through outside adapters.

Operational tracing may correlate events across those Cores for investigation. It must not make the trace collector a
new shared Contract authority. Each Core's Result, Failure, Evidence, Publication, and Output boundaries remain intact.

A cross-Core diagnostic view is therefore a projection over several authoritative records and external observations. It
is not a hidden pipeline through which one Core reads another Core's internals.

### 3.18. Required Capture Cannot Depend on Silent Drop or Unbounded Backpressure

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

### 3.19. Throttling and Deduplication Cannot Erase Required Occurrences

Repeated operational events are often throttled because a fault can produce thousands of identical messages. That is a
valid implementation policy for best-effort observation.

A Contract Evidence obligation is different. If each source occurrence is semantically required to have evidence, a
throttle cannot collapse ten occurrences into one record and still claim that all ten evidence occurrences were
retained. The backend may compress storage only if it preserves a lossless representation from which the exact required
occurrences and their relations can be recovered.

If the Contract later defines aggregate evidence instead, that aggregate needs its own meaning. The implementation must
not infer aggregation from a logger's flood-control policy.

### 3.20. Audit Records and Diagnostic Evidence Are Not Automatically the Same Thing

An audit ledger usually has a stronger concern with historical completeness, actor attribution, append-only ordering,
and long-lived review. Diagnostic Evidence is focused on explaining exact machine occurrences and may have a much
shorter Retention boundary.

One physical record can potentially serve both purposes when both Contracts are satisfied, but ADR-0060 does not make
every diagnostic occurrence an audit record. Doing so would force archival and ordering obligations onto diagnostic hot
paths that do not need them.

If Kontrakt later adds an explicit audit or compliance Contract, it can consume or reference Diagnostic Evidence without
changing the evidence's original meaning.

### 3.21. Source-Supplied Validity and Uncertainty Must Not Be Normalized Away

Some low-level diagnostic sources know that only part of a record is valid, that a reported address is imprecise, that
an overflow may have lost earlier errors, or that a measurement carries uncertainty. Those facts are part of the
operational record's interpretation.

A backend adapter should preserve such source-supplied validity instead of converting unavailable fields into zero or
assigning false precision. The common Kontrakt Contract does not need one global confidence scale to follow this rule.
It only needs to avoid destroying the semantics of the backend evidence it chooses to expose to diagnostic tooling.

### 3.22. Operational Diagnostic Depth Is Explicit and Aggressively Selective

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

## 4. Lowering and Backend Integration

### 4.1. Lowering Produces an Evidence Realization Plan

Backend-neutral lowering should translate a Diagnostic Evidence definition into the minimum machine work required to
establish an occurrence.

The plan identifies the canonical source, selected values, occurrence correlation, and required retention handoff. It
does not choose JVM object classes or an external logging sink.

The JVM backend can then specialize this plan into primitive table indices, compact carriers, generated code, or another
efficient physical layout.

Keeping this seam backend-neutral allows a future native or embedded backend to realize the same evidence obligation
without reproducing JVM concepts.

### 4.2. Generated Artifact Mapping Supports Post-Deployment Diagnosis

Generated JVM methods and fields may carry compact metadata that maps them back to canonical Contract authorities.

This mapping supports stack symbolization, JFR correlation, crash reports, and backend debugging without making
generated names semantic identity. A different code-generation strategy may emit different methods while preserving the
same mapping relation.

The mapping should be emitted only to the extent needed for the selected diagnostic capability and artifact size budget.
A production artifact does not need to carry every compiler-internal debug structure.

### 4.3. Diagnostic Planning Separates Guarantee from Cost

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

## 5. V1 Realization Boundary

### 5.1. V1 Runtime Evidence Is Compact

The generated runtime should capture required evidence into compact typed storage and defer human formatting.

A first backend can use fixed or bounded in-memory structures when the declared Retention boundary permits that
realization. The compiler must account for the memory region as Kontrakt-owned resource where Capacity law applies.

JVM stacks or generic maps are not required for normal Contract Evidence.

### 5.2. V1 Operational Diagnostics Remain Optional

The JVM backend may provide hooks for JFR, structured logging, crash reporting, or other operational systems. Those
hooks should accept stable Contract correlation identities so operators can connect physical events to machine
occurrences.

The absence of an optional backend integration does not weaken Contract Evidence. Conversely, enabling a rich profiler
does not expand the Contract.

### 5.3. V1 Operational Diagnostic Depth

V1 may expose only a small number of operational diagnostic profiles. The normal profile keeps required Contract
Evidence and stable correlation anchors while avoiding unnecessary runtime observation. Deeper profiles may enable
richer backend capture around selected occurrences.

The exact profile names, switches, and backend-specific attachments remain open. A lower profile cannot be interpreted
as permission to sample or drop evidence declared by ADR-0060.

## 6. V2 Extension Path

### 6.1. Adaptive Operational Capture

Generated systems can increase operational diagnostic depth after an anomaly or explicit operator request.

A baseline event can trigger a temporary recording window around the relevant Interaction, JVM compilation, storage
subsystem, or target device. Differential or targeted tracing can focus on the first point where a healthy and unhealthy
execution diverge instead of recording every event system-wide.

This remains operational unless a future Contract explicitly requires a particular adaptive capture guarantee.

### 6.2. Stronger Retention Capabilities

V2 may add persistence across process restart, host reboot, or stronger failure domains once Lifecycle and backend
capability semantics are explicit.

The logical Evidence occurrence remains the same even if V2 stores it in durable files, a database, replicated storage,
or a platform error repository. Storage migration cannot change the canonical definition it references.

### 6.3. Cross-Backend Correlation

A future backend may run on native, embedded, distributed, or accelerator targets.

The same canonical occurrence identities should be usable to correlate Contract Evidence with target-specific telemetry
without changing the Contract source. Hardware RAS, spacecraft events, embedded fault memory, or native tracing can all
be adapters over that seam.

This is why JVM-specific identifiers cannot become the V1 canonical evidence model.

### 6.4. Runtime Diagnostic Cost Models

Generated systems may later choose optional diagnostic work according to backend cost and selected diagnostic intent. An
anomaly can trigger a bounded recent-history freeze, targeted tracing, differential comparison, or another
backend-native investigation strategy.

Required Contract Evidence remains outside this optional cost decision because its cost is already part of realizing the
declared Contract.

## 7. Verification and Quality Requirements

### 7.1. Evidence-Occurrence Verification

Reference judgment machinery should be able to validate that an evidence occurrence corresponds to the exact source
result occurrence and contains the declared material.

Optimized generated capture should be tested against that reference behavior. If the optimized runtime representation
uses compact ordinals or fused gates, those storage choices must still decode to the same canonical evidence relation.

### 7.2. Optimization Preservation Verification

Every transform that can affect Contract Evidence or compiler provenance has a preservation obligation.

Regression tests compare pre- and post-transform attribution. Selected transforms can use differential or
translation-validation checks. A transform that changes acceptance behavior, Failure attribution, required evidence, or
source provenance is rejected or fixed before release.

### 7.3. Runtime Evidence Stress Tests

Generated-system tests must exercise evidence capture at the declared resource bounds.

A backend that promises required evidence cannot rely on an unchecked queue that silently drops records under burst
load. Tests should cover capacity exhaustion, retention expiry, concurrent occurrences, and optional operational tracing
being disabled.

The original Contract result must remain identical in all cases where diagnostic configuration is not itself part of the
selected Contract.

### 7.4. Diagnostic-System Fault Injection

High-reliability engineering treats the diagnostic mechanism as a component that can fail. Kontrakt should do the same.

Compiler tests can corrupt cached provenance, truncate machine-readable diagnostic payloads, or interrupt crash-bundle
creation. Runtime tests can simulate loss of an optional trace sink or failure of a retention backend. The system must
fail according to the authority of the affected diagnostic obligation and must not fabricate evidence from incomplete
material.

### 7.5. Performance Regression Tests

Compiler benchmarks should measure diagnostic-heavy invalid builds separately from successful builds. Source mapping,
conflict explanation, rendering, and machine-output serialization need their own profiles. Generated runtime benchmarks
should compare Contract Evidence capture against the same path with no evidence declaration so the marginal cost is
visible.

Tests should also compare supported diagnostic-depth profiles. Lower optional depth must reduce or bound the expected
work without changing Contract validity, required evidence, stable compiler error identity, or the semantic explanation
of the root cause. A deeper profile can add internal evidence and investigation artifacts; it cannot reveal that the
normal profile had reported a different Contract reason.

This makes diagnostic performance an engineered property rather than an anecdote.

## 8. Authority Boundary

The backend owns the physical realization of required Diagnostic Evidence within declared capability. It may use compact
tables, bounded memory, generated carriers, or another representation without changing Contract meaning.

JVM frames, JFR events, JIT compilation records, sanitizer reports, OS traces, crash dumps, hardware RAS records, and
external observability data remain operational evidence. They may be correlated with a Contract occurrence but do not
become Contract authority by proximity.

Storage is also a realization mechanism. Files, databases, in-memory arenas, remote collectors, or platform repositories
may satisfy a Retention guarantee only when their actual capability is sufficient. Their physical lifecycle does not
define the Retention Contract.

## 9. Open Decisions

### 9.1. Exact JVM Evidence Kernel Representation

The V1 path must be compact and typed and should avoid mandatory stack walking, text formatting, or generic map
allocation.

### 9.2. Correlation Identity Encoding in Generated Artifacts

Generated names are not semantic identity. The backend needs a stable mapping relation from physical artifacts to
canonical Contract occurrences.

### 9.3. Operational Diagnostic Profile Names and Semantics

Profiles must control optional capture depth without weakening ADR-0060 guarantees.

### 9.4. JFR / HotSpot / Graal Integration

These are JVM backend integrations rather than Contract vocabulary.

### 9.5. Native, OS, and Hardware Evidence Adapters

Target-specific evidence remains supplemental and should preserve source-provided validity or loss information where
available.

### 9.6. Fault-Time Recent-History Buffer Design

A bounded recent-history mechanism is a candidate realization for low-cost context around an anomaly. Its exact shape is
not decided.

### 9.7. Optional Sampling, Throttling, and Deduplication Policy

These mechanisms may reduce optional operational cost. They cannot erase occurrences that the Contract requires to be
captured.

### 9.8. Persistent Retention Backend after Lifecycle Semantics Are Closed

Durability across stronger failure domains remains dependent on ADR-0060 lifecycle semantics and backend capability.

### 9.9. External Observability Integration

External collectors remain outside Core authority even when they receive stable correlation identities.

## 10. Consequences

### Positive

Generated systems can satisfy a small guaranteed Contract Evidence path without carrying the full cost of rich
operational tracing on every execution.

Low-level JVM, operating-system, native, and hardware diagnostics remain available through explicit correlation instead
of being discarded for portability or promoted into Contract authority.

The realization architecture leaves room for anomaly-triggered and differential diagnostic techniques in V2 without
changing ADR-0060 semantics.

### Negative

The backend needs explicit resource planning for required evidence and must reject guarantees it cannot realize instead
of silently dropping data.

Stable correlation across generated code, user realization, JVM, operating-system, and hardware layers adds metadata and
testing obligations.

### Neutral

This ADR does not define the meaning of Diagnostic Evidence or Retention and does not define Kontrakt compiler source
diagnostics.