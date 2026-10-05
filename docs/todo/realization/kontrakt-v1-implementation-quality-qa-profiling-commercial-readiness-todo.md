# Kontrakt V1 — Implementation Quality, QA, Profiling, and Commercial-Readiness TODO

## Status

Implementation TODO for V1.

This document does not define new Contract semantics.

It turns the already accepted Contract laws and compiler architecture into implementation obligations, QA evidence,
profiling evidence, and release gates.

The main goal is simple:

> Production code must not become the de facto specification merely because it was implemented first.

Kontrakt should be implemented so that semantic correctness, physical realization quality, and performance claims can
each be checked independently.

---

# 1. Working Rule

Every production subsystem must have four things before it is considered complete.

```text
declared semantic or compiler obligation
    ↓
production implementation
    ↓
independent or structurally separate verification path
    ↓
measured physical / performance evidence
```

The four concerns must remain separate.

Correct semantic behavior does not prove that the physical layout is good.

A good physical layout does not prove semantic correctness.

A fast benchmark does not prove either one.

---

# 2. Priority Order

The implementation order should be:

```text
P0  QA and measurement foundation
P1  one complete vertical semantic slice
P2  physical storage and access-path validation
P3  reuse / query / cache validation
P4  realization verification and generated verification
P5  execution IR / optimization / JVM backend
P6  hardening, scale testing, and release readiness
```

Do not build the whole verifier first.

Do not build the whole optimizer first.

Do not wait until the compiler is nearly complete before adding QA or profiling.

The QA and measurement skeleton should exist before large amounts of production compiler code are written.

---

# 3. P0 — Build the QA Control Plane First

## 3.1. Create an Implementation Obligation Ledger

Create one machine-readable or easily audited ledger that connects accepted semantic law to implementation and evidence.

Each entry should record:

```text
obligation id
owning ADR / Design section
plain-language obligation
implementation owner
affected compiler boundary
reference / oracle path
positive test
negative test
metamorphic or differential test where useful
profiling evidence where physical behavior is claimed
current status
```

Example:

```text
Obligation
    equal semantic payload must not merge independent References

Implementation owner
    HIR reference formation

Evidence
    semantic golden vector
    cross-generation reference test
    reuse differential test
```

The ledger is QA metadata.

It is not Contract authority.

### TODO

- [ ] Define the ledger format.
- [ ] Give every closed V1 semantic rule a stable QA obligation id.
- [ ] Link each obligation to one owning document.
- [ ] Reject duplicate ownership.
- [ ] Allow one obligation to have several implementation owners when preservation crosses boundaries.
- [ ] Require every production subsystem PR to update the ledger when it implements a new obligation.
- [ ] Add a CI check that reports accepted obligations with no implementation evidence.
- [ ] Add a CI check that reports production features with no declared owner.

---

## 3.2. Establish the Test Taxonomy

Do not use one generic `test` bucket.

Separate at least these classes:

```text
semantic conformance
compiler regression
negative boundary
differential
metamorphic
property-based
fuzz
concurrency / schedule perturbation
fault injection
performance regression
physical-layout validation
end-to-end integration
```

The same case may participate in several classes, but its purpose must be explicit.

### TODO

- [ ] Define directories and naming rules for each test class.
- [ ] Make regression tests minimal and local to the bug or invariant they protect.
- [ ] Keep whole-machine examples separate from narrow regression tests.
- [ ] Give every fixed bug a permanent regression case.
- [ ] Record the exact Contract or compiler invariant protected by important tests.
- [ ] Prevent generated golden files from silently becoming the semantic source of truth.

---

## 3.3. Build a Deterministic Semantic Inspection Path

Every major semantic compiler product needs a stable QA inspection surface.

This is not a public user format.

It is a derived compiler-QA product.

Initial targets:

```text
Resolved Contract HIR
Established Definition Material
Established Occurrence Material where applicable
Canonical Contract World relations
Verification result
Execution IR
JVM plan
```

The inspection path should expose semantic observations rather than slab addresses or object identities.

### TODO

- [ ] Define one deterministic inspection format.
- [ ] Sort only where semantic or QA observation requires stable order.
- [ ] Keep dense handles and physical offsets explicitly non-normative.
- [ ] Support clean-build versus reused-build comparison.
- [ ] Support same-semantics / changed-provenance comparison.
- [ ] Support semantic-diff output for changed projections.
- [ ] Add golden vectors for the first vertical slice.
- [ ] Ensure replacing the physical backing does not require changing expected semantic observations.

---

## 3.4. Build the Reference Judgment Skeleton

The reference path should prioritize clarity and correctness.

It should not share every optimization with production execution.

Its purpose is to catch correlated implementation mistakes.

### TODO

- [ ] Define the smallest deterministic reference evaluator needed by the first vertical slice.
- [ ] Keep its control flow simple.
- [ ] Keep reference semantics independent from production optimization passes.
- [ ] Reuse authoritative Established Material, not production analysis conclusions.
- [ ] Produce minimal structured evidence.
- [ ] Support differential comparison against generated or optimized execution.
- [ ] Add exhaustive testing for tiny finite domains where practical.
- [ ] Add boundary and property-style generation for larger domains.

---

# 4. P0 — Build Profiling and Measurement Infrastructure Before Optimization

Kontrakt uses slabbing, dense handles, offsets, compact identity material, and selective projections.

Profiling therefore serves two purposes.

```text
performance measurement
physical architecture validation
```

The second purpose is essential.

A slab implementation is not successful merely because it exists in source code.

The hot path must actually use it as intended.

---

## 4.1. Add Phase-Level Timing

Measure the compiler at stable architectural boundaries.

Initial phase families:

```text
source acquisition
parsing / resolution
HIR formation
HIR verification / seal
Establishment
Canonical Contract World publication
realization acquisition
realization analysis
verification
execution formation
optimization
JVM planning
classfile emission
```

### TODO

- [ ] Add phase timers with stable phase ids.
- [ ] Measure wall time and CPU time separately where practical.
- [ ] Support nested timing without changing semantic execution.
- [ ] Record invocation count.
- [ ] Record work-item count beside elapsed time.
- [ ] Make profiling disabled-by-default or low-overhead in normal builds.
- [ ] Ensure timing data never affects semantic decisions.

---

## 4.2. Add Physical-Access Counters

The compiler should be able to show whether the intended storage architecture is actually being used.

Track at least:

```text
slab touches
bytes read
bytes written
handle-to-offset resolutions
cross-partition reads
temporary materializations
heap allocations on hot paths
scratch bytes
off-heap bytes
copy count
projection read count
```

Do not promise all counters as permanent public API.

They are engineering evidence.

### TODO

- [ ] Add optional counters at HIR projection boundaries.
- [ ] Add optional counters at Established Material projection boundaries.
- [ ] Count unexpected reads from unrelated partitions.
- [ ] Count fallback object-path access where a slab path is expected.
- [ ] Count intermediate copies during formation and transformation.
- [ ] Detect object allocation in declared allocation-sensitive hot paths.
- [ ] Measure retained versus temporary memory separately.
- [ ] Add peak memory measurements for major phases.

---

## 4.3. Use JVM-Level Profiling as Independent Evidence

Use JVM tooling to verify the compiler's own counters.

Recommended evidence includes:

```text
allocation profile
GC activity
CPU samples
lock contention
thread stalls
JIT compilation behavior
native / off-heap pressure where observable
```

JFR is appropriate for continuous low-overhead JVM evidence.

Use deeper sampling or hardware-counter tooling when investigating a confirmed hotspot.

### TODO

- [ ] Define a standard JFR recording profile for compiler benchmark runs.
- [ ] Capture allocation and GC evidence for representative workloads.
- [ ] Capture thread and lock evidence for parallel compiler runs.
- [ ] Store selected recordings for major regressions.
- [ ] Correlate JFR evidence with Kontrakt phase ids.
- [ ] Keep profiler output out of semantic identity and cache keys.

---

# 5. P0 — Build the Benchmark Corpus Early

Do not wait for the optimizer.

The initial benchmark corpus exists to detect structural mistakes.

Use stable synthetic workloads first.

Add realistic projects when the compiler can run them.

## 5.1. Required Benchmark Shapes

Create at least:

```text
tiny
small
medium
large

few wide definitions
many narrow definitions

many interactions
many 1D definitions

large flat coordinate sets
closed aggregate-heavy values

high reference reuse
low reference reuse

clean build
warm in-memory reuse
cache disabled
cache enabled when cache exists

single worker
multiple workers

valid input
invalid input
hostile but bounded input
```

### TODO

- [ ] Freeze benchmark generators with deterministic seeds.
- [ ] Record semantic size separately from source byte size.
- [ ] Record HIR row counts.
- [ ] Record Established Material counts.
- [ ] Record reference counts.
- [ ] Record relation counts.
- [ ] Record Execution IR size when available.
- [ ] Keep at least one pathological but legal benchmark.
- [ ] Keep malformed and rejected cases in a separate robustness corpus.

---

## 5.2. Separate Microbenchmarks and Compiler Benchmarks

Microbenchmarks answer narrow questions.

Compiler benchmarks answer architectural questions.

Example microbenchmarks:

```text
handle lookup
offset decode
slab scan
exact equality
HID pre-screen
projection read
small canonical encoder
```

Example compiler benchmarks:

```text
complete frontend-to-HIR
HIR-to-Established World
verification
full clean compile
full reused compile
```

### TODO

- [ ] Use JMH or an equivalently disciplined harness for JVM microbenchmarks.
- [ ] Never infer whole-compiler performance from a microbenchmark.
- [ ] Record warmup and measurement configuration.
- [ ] Pin benchmark JDK and hardware metadata.
- [ ] Keep historical performance results comparable.
- [ ] Mark benchmark changes separately from compiler changes.

---

# 6. P1 — Implement One Complete Vertical Slice Before Broadening

Do not implement every subsystem horizontally.

Choose one narrow but real path that exercises the architecture.

Target shape:

```text
source
→ resolution
→ Resolved HIR
→ HIR QA inspection
→ Establishment
→ Established Material
→ Reference Judgment
→ generated / production path
→ differential comparison
```

### TODO

- [ ] Select the smallest closed Contract path with meaningful success and failure.
- [ ] Build semantic golden vectors first.
- [ ] Build negative vectors.
- [ ] Implement HIR formation.
- [ ] Run HIR conformance checks before visibility.
- [ ] Implement Establishment.
- [ ] Compare Established Material against golden observations.
- [ ] Execute through Reference Judgment.
- [ ] Add the first production execution path.
- [ ] Differentially compare reference and production results.
- [ ] Profile the complete slice.
- [ ] Record allocations, slab access, copies, and phase timings.
- [ ] Refuse to broaden the compiler until the slice has deterministic QA and profiling evidence.

---

# 7. P2 — Validate the Slab / Offset Physical Architecture

This phase proves that the mechanical design is real rather than aspirational.

## 7.1. Access-Path Invariants

For each hot semantic projection, define an expected physical-access contract in Design or QA.

Example:

```text
Admission semantic projection

allowed:
    Admission semantic slabs
    shared exact reference index

unexpected:
    provenance rendering storage
    Lowering-only slabs
    diagnostic text
    source AST
```

### TODO

- [ ] Define expected partition reads for hot consumers.
- [ ] Add counters that detect unrelated partition reads.
- [ ] Add tests that replace or relocate physical backing without changing semantic results.
- [ ] Add bounds checks for every offset decode boundary.
- [ ] Add stale-generation handle tests.
- [ ] Add corrupted-offset tests.
- [ ] Add maximum-size index tests.
- [ ] Add segment / arena lifetime tests when FFM is used.
- [ ] Add tests for zero-length and one-element slabs.
- [ ] Add tests across segment boundaries.
- [ ] Verify that hot projection reads do not require object graph traversal.

---

## 7.2. Memory Discipline

### TODO

- [ ] Measure bytes per semantic Definition.
- [ ] Measure bytes per semantic relation.
- [ ] Measure bytes per occurrence where occurrences exist.
- [ ] Measure temporary bytes per phase.
- [ ] Measure retained bytes after each publication boundary.
- [ ] Detect accidental duplicate storage of semantic material.
- [ ] Detect accidental boxed collections in high-cardinality paths.
- [ ] Detect diagnostic string creation on hot success paths.
- [ ] Verify that rejected construction does not leave published residue.
- [ ] Verify that failed generations release reclaimable physical backing.

---

# 8. P2 — Add Invariant Checkers Around Major Compiler Products

Each major compiler product should have an optional expensive verifier for development and CI.

These verifiers are compiler QA.

They are not Contract authorities.

Targets:

```text
HIR verifier
Established World verifier
Realization analysis verifier
Execution IR verifier
JVM plan verifier
```

### TODO

- [ ] Verify reference-kind correctness.
- [ ] Verify generation coherence.
- [ ] Verify cardinality and range constraints.
- [ ] Verify relation targets exist.
- [ ] Verify forbidden cycles where the product requires acyclicity.
- [ ] Verify no partial visible product is published.
- [ ] Verify sorted/indexed physical views agree with authoritative semantic observations.
- [ ] Verify cached summaries agree with recomputation in sampled QA runs.
- [ ] Run expensive verifiers in CI even when disabled in production builds.

---

# 9. P3 — Query, Reuse, Cache, and Incremental-Seam QA

Do not optimize reuse before semantic equality and validity are stable.

## 9.1. No-Cache Equivalence

The cache-disabled path is a critical reference configuration.

### TODO

- [ ] Make cache-disabled execution a supported QA mode.
- [ ] Require cache-on and cache-off semantic equivalence.
- [ ] Require reuse-on and clean recomputation semantic equivalence.
- [ ] Reject reuse on hash equality alone.
- [ ] Validate exact equality after fingerprint matches where required.
- [ ] Test deliberate fingerprint collisions in a controlled harness.
- [ ] Test stale-generation cache entries.
- [ ] Test dependency invalidation.
- [ ] Test appearance of a new legal member in complete-set results.
- [ ] Test disappearance of a previously legal member.
- [ ] Test changed provenance with unchanged semantic meaning.
- [ ] Measure the actual work skipped by successful reuse.

---

## 9.2. Dependency Precision

Bad dependency granularity can make a correct compiler commercially unusable.

### TODO

- [ ] Record semantic product reads rather than incidental physical reads.
- [ ] Detect broad dependency edges created by convenience APIs.
- [ ] Measure fan-in and fan-out distributions.
- [ ] Measure invalidation radius on controlled edits.
- [ ] Add edit scenarios for comments, provenance-only changes, one-law changes, and whole-interface changes.
- [ ] Keep query identity distinct from Contract identity.
- [ ] Keep result fingerprint distinct from both.

---

# 10. P4 — Realization Verification

Build the user-realization verifier only after the authoritative Contract products it consumes are stable.

## 10.1. Independence

### TODO

- [ ] Define which verifier inputs are authoritative Established Material.
- [ ] Define which inputs are compiler-derived analysis.
- [ ] Prevent compiler-derived analysis from becoming Contract authority.
- [ ] Keep an independent reference path for high-risk legality decisions.
- [ ] Avoid using one buggy analysis result as both optimization proof and verification oracle.
- [ ] Differentially compare independent analyses where the risk justifies the cost.

---

## 10.2. User Contract PBT

User Contract PBT is not compiler QA.

### TODO

- [ ] Derive test plans from Established obligations.
- [ ] Keep plan formation deterministic.
- [ ] Separate test plan identity from concrete generated cases.
- [ ] Use stable seed partitioning.
- [ ] Generate boundary cases.
- [ ] Generate declared failure cases.
- [ ] Generate legal and illegal state movement cases where applicable.
- [ ] Record uncovered semantic partitions.
- [ ] Never treat absence of a generated counterexample as proof of legality.

---

# 11. P5 — Execution IR and Optimization QA

Optimization is allowed only after legality has been established.

## 11.1. Before / After Equivalence

### TODO

- [ ] Define the observable semantics for each transform family.
- [ ] Verify every transform against the reference path on generated cases.
- [ ] Add IR verifier runs after transformation groups.
- [ ] Add randomized legal pass-order testing where useful.
- [ ] Add transformation idempotence tests where expected.
- [ ] Add no-op transform stability tests.
- [ ] Preserve Failure attribution.
- [ ] Preserve Budget and Capacity-visible outcomes.
- [ ] Preserve State-visible movement.
- [ ] Preserve diagnostic ownership even if diagnostic rendering changes.
- [ ] Reject an optimization when the compiler cannot prove the required preservation law.

---

## 11.2. Track Optimization Effectiveness

A transform that costs more than it saves can still be semantically correct and commercially bad.

### TODO

- [ ] Count transform attempts.
- [ ] Count successful transforms.
- [ ] Measure compile-time cost by transform family.
- [ ] Measure removed work or reduced IR size.
- [ ] Measure generated bytecode size change.
- [ ] Measure runtime impact on representative programs.
- [ ] Disable or redesign transforms with consistently negative cost-benefit.

---

# 12. P5 — JVM Backend Quality

The direct classfile backend needs its own correctness boundary.

### TODO

- [ ] Build golden classfile tests for stable low-level cases.
- [ ] Verify emitted classfiles with JVM/classfile verification tooling.
- [ ] Run generated artifacts on every supported JDK baseline.
- [ ] Add malformed-internal-plan negative tests.
- [ ] Add verifier-failure tests.
- [ ] Add stack / local / frame edge cases.
- [ ] Add large-method and constant-pool boundary tests.
- [ ] Add deterministic emission tests.
- [ ] Separate semantic equality from byte-identical classfile reproducibility.
- [ ] Keep a slower reference/bootstrap backend long enough for differential testing if practical.

---

# 13. Cross-Cutting Fuzzing and Adversarial QA

Fuzzing should begin as soon as a stable input boundary exists.

Do not wait for V1 completion.

Initial fuzz targets:

```text
IDL parser
host-evidence decoder
HIR formation boundary
persistent or binary decoders if introduced
reference resolution
canonical encoders
classfile planning / emission inputs
diagnostic evidence rendering
```

### TODO

- [ ] Add coverage-guided fuzz targets.
- [ ] Keep minimized regression inputs for every fixed fuzz bug.
- [ ] Fuzz malformed nesting.
- [ ] Fuzz extreme cardinality.
- [ ] Fuzz duplicate and conflicting coordinates.
- [ ] Fuzz corrupted offsets and lengths.
- [ ] Fuzz unknown tags and versions where binary formats exist.
- [ ] Fuzz cyclic or recursion-like hostile structures at external boundaries.
- [ ] Fuzz diagnostic rendering with malicious source text.
- [ ] Run long fuzz campaigns outside per-commit CI.
- [ ] Run small deterministic fuzz smoke sets in CI.

---

# 14. Concurrency and Schedule Perturbation

Determinism must survive legal concurrency.

### TODO

- [ ] Randomize independent construction completion order.
- [ ] Randomize worker scheduling in QA mode.
- [ ] Re-run the same semantic workload with different worker counts.
- [ ] Compare semantic inspection output.
- [ ] Compare artifact reproducibility where reproducibility is promised.
- [ ] Test publication races.
- [ ] Test cancellation at legal interruption points.
- [ ] Test failure of one independent product while others continue.
- [ ] Test stale-handle access across generation replacement.
- [ ] Test shared immutable backing under parallel readers.
- [ ] Detect accidental global mutable state.

---

# 15. Fault Injection and Resource Failure

Commercial quality requires deliberate failure testing.

### TODO

- [ ] Inject allocation refusal at controlled boundaries.
- [ ] Inject off-heap allocation failure.
- [ ] Inject I/O failure where persistence exists.
- [ ] Inject cache corruption.
- [ ] Inject invalid persisted metadata.
- [ ] Inject cancellation.
- [ ] Inject worker failure.
- [ ] Inject diagnostic-budget exhaustion.
- [ ] Verify no half-published HIR.
- [ ] Verify no half-published Established Material.
- [ ] Verify no poisoned reusable cache entry.
- [ ] Verify failure ownership is not rewritten into another Contract result.
- [ ] Verify resource failure does not fabricate semantic meaning.

---

# 16. Diagnostics as a Production Subsystem

Diagnostics should be tested for correctness, boundedness, determinism, and usability.

### TODO

- [ ] Separate diagnostic evidence from rendered text.
- [ ] Give diagnostic evidence explicit size bounds.
- [ ] Keep hot-path success free of diagnostic string construction.
- [ ] Add truncation rules.
- [ ] Add sanitization rules.
- [ ] Add deterministic ordering.
- [ ] Add golden diagnostics for major user errors.
- [ ] Add hostile-source-text tests.
- [ ] Add cyclic evidence tests.
- [ ] Add oversized evidence tests.
- [ ] Test diagnostics with missing optional provenance.
- [ ] Ensure diagnostics never become the only source of semantic truth.

---

# 17. Performance Regression Infrastructure

Performance should be tracked continuously once the first vertical slice is stable.

Follow the compiler-project model:

```text
per-change targeted run
regular full benchmark run
historical trend storage
regression investigation
```

### TODO

- [ ] Store benchmark results by compiler revision.
- [ ] Track clean compile time.
- [ ] Track reused compile time.
- [ ] Track peak heap.
- [ ] Track peak off-heap.
- [ ] Track allocated bytes.
- [ ] Track GC count and pause time.
- [ ] Track HIR formation cost.
- [ ] Track Establishment cost.
- [ ] Track verification cost.
- [ ] Track classfile emission cost.
- [ ] Track output artifact size.
- [ ] Track work counts beside elapsed time.
- [ ] Add statistical noise handling before enforcing tight thresholds.
- [ ] Require targeted performance runs for changes to storage, identity, query, verification, optimization, or backend
  hot paths.
- [ ] Do not make unstable microbenchmark thresholds hard release gates prematurely.

---

# 18. Profiling Evidence for Slabbing and Offsets

This is a Kontrakt-specific release-quality requirement.

A slabbing claim should have evidence.

For every major slab-backed subsystem, record:

```text
expected physical access pattern
observed access pattern
allocation profile
copy behavior
memory footprint
hotspot profile
comparison against a simpler baseline where useful
```

### TODO

- [ ] Create one profiling note per major slab family.
- [ ] Show that intended consumers avoid unrelated partitions.
- [ ] Show that handle lookup is bounded and cheap.
- [ ] Show that offset decoding is not a dominant cost.
- [ ] Show allocation reduction compared with an object-heavy baseline where the comparison is meaningful.
- [ ] Measure locality-sensitive scans with representative data sizes.
- [ ] Investigate hardware cache behavior for confirmed hot loops.
- [ ] Record cases where slabbing loses and explain why.
- [ ] Permit a simpler representation when profiling disproves the more complex one.
- [ ] Keep these findings in Design / performance notes, not Contract ADRs.

---

# 19. CI Structure

Use different cadences for different evidence.

## Per commit / PR

- [ ] Build.
- [ ] Unit tests.
- [ ] Narrow regression tests.
- [ ] semantic golden vectors.
- [ ] architecture-boundary tests.
- [ ] deterministic smoke tests.
- [ ] fast differential tests.
- [ ] fast fuzz corpus replay.

## Scheduled

- [ ] full regression suite.
- [ ] larger differential suite.
- [ ] property-based suite.
- [ ] multi-worker schedule perturbation.
- [ ] long-running fuzzing.
- [ ] leak tests.
- [ ] benchmark suite.
- [ ] JFR profiling capture for standard workloads.

## Release candidate

- [ ] full supported-JDK matrix.
- [ ] clean/reuse/cache equivalence.
- [ ] reproducibility runs.
- [ ] stress and soak.
- [ ] corrupted artifact tests.
- [ ] resource-failure injection.
- [ ] benchmark comparison against the previous baseline.
- [ ] diagnostic stability.
- [ ] example-project verification.
- [ ] known limitations review.

---

# 20. Architecture Boundary Tests

Some Kontrakt rules are better enforced structurally than dynamically.

### TODO

- [ ] Add forbidden-dependency checks between semantic and backend packages.
- [ ] Prevent HIR consumers from reopening frontend source.
- [ ] Prevent Contract semantic packages from depending on JVM backend packages.
- [ ] Prevent physical handle types from crossing semantic APIs where exact semantic refs are required.
- [ ] Prevent diagnostics from becoming an authority dependency.
- [ ] Prevent cache/query packages from being required to determine Contract meaning.
- [ ] Prevent user realization callbacks from entering Contract authority paths.
- [ ] Keep generated API packages downstream of Established semantics.
- [ ] Add architecture tests for these rules.

---

# 21. Compatibility and Environment Matrix

V1 should explicitly define what it supports.

### TODO

- [ ] Select supported JDK versions.
- [ ] Select supported host OS targets for V1.
- [ ] Define classfile target policy.
- [ ] Define Kotlin / Java frontend compatibility policy where applicable.
- [ ] Test filesystem and path edge cases on supported platforms.
- [ ] Test locale independence.
- [ ] Test timezone independence.
- [ ] Test default-charset independence.
- [ ] Test different CPU counts.
- [ ] Record unsupported environments clearly.
- [ ] Keep environment differences from becoming semantic inputs unless explicitly declared.

---

# 22. Reproducibility and Supply-Chain Discipline

### TODO

- [ ] Make release builds reproducible where practical.
- [ ] Pin build-tool and dependency versions.
- [ ] Record toolchain versions.
- [ ] Produce dependency inventory / SBOM for releases.
- [ ] Scan dependencies for known vulnerabilities.
- [ ] Define artifact signing / checksum release procedure.
- [ ] Verify release artifacts from clean environments.
- [ ] Keep build metadata from entering Contract semantic identity.
- [ ] Test that source path differences do not change semantic products.

---

# 23. User-Facing Quality

Commercial compiler quality is not only internal correctness.

### TODO

- [ ] Build one realistic end-to-end example early.
- [ ] Make common compile errors actionable.
- [ ] Preserve exact Contract attribution in diagnostics.
- [ ] Avoid leaking compiler-internal terminology into ordinary user errors.
- [ ] Provide machine-readable diagnostics only after their compatibility boundary is explicit.
- [ ] Add compile-time summaries useful for debugging performance.
- [ ] Document known limitations honestly.
- [ ] Document unsupported Contract forms with the reason they are unsupported.

---

# 24. Definition of Done for Every New Compiler Subsystem

A subsystem is not complete when its happy path works.

Before marking it V1-complete, confirm:

```text
semantic owner identified
implementation boundary documented
QA obligations linked
positive tests present
negative tests present
differential / metamorphic tests where applicable
fuzz target where the boundary accepts structured untrusted input
expensive invariant checker available where valuable
profiling counters available
benchmark scenario present
failure cleanup tested
determinism tested
concurrency behavior tested when concurrent
resource bounds tested
diagnostics tested
no forbidden authority dependency introduced
```

---

# 25. What Not to Do Early

Do not front-load complexity that has no evidence yet.

### Do not

- [ ] build the complete production verifier before the first authoritative semantic slice exists.
- [ ] implement persistent incremental caching before semantic equality and dependency boundaries are stable.
- [ ] tune cache-line layout before a hotspot is measured.
- [ ] introduce a sophisticated cost model before baseline measurements exist.
- [ ] use HID, hash, or fingerprint equality as semantic equality.
- [ ] use benchmark results to decide semantic legality.
- [ ] make profiler output part of cache or Contract identity.
- [ ] write only happy-path unit tests.
- [ ] rely only on code coverage percentage as a quality measure.
- [ ] rely only on microbenchmarks for compiler performance.
- [ ] allow optimized and reference paths to share every legality analysis.
- [ ] postpone fuzzing until after the parser / decoder architecture is fixed.
- [ ] postpone diagnostics until the end.
- [ ] allow production code shape to become the undocumented specification.

---

# 26. Immediate Implementation Sequence

When semantic ADR closure is far enough to begin production implementation, execute this sequence first.

```text
1. Create the implementation-obligation ledger.

2. Create the deterministic semantic inspection format.

3. Create the test taxonomy and test runner layout.

4. Create the benchmark corpus generator and result format.

5. Add phase timing and allocation / memory profiling hooks.

6. Add physical-access counters for the first HIR slab prototype.

7. Implement the smallest frontend → HIR vertical slice.

8. Add HIR invariant checking and golden vectors.

9. Implement the matching Establishment slice.

10. Add Established Material golden vectors.

11. Build the minimal Reference Judgment path.

12. Build one production execution path.

13. Differentially test reference versus production.

14. Profile the complete slice.

15. Verify that slab / offset access matches the intended physical design.

16. Add the first fuzz targets.

17. Add cache-disabled / reuse-disabled reference configurations.

18. Only then broaden to more 1D authorities and more compiler products.
```

This sequence should be repeated incrementally.

Every additional major subsystem should enter through the same loop:

```text
law
→ implementation
→ independent evidence
→ physical evidence
→ performance evidence
```

---

# 27. V1 Commercial-Quality Gates

Before calling V1 stable, require evidence in all of these areas.

```text
semantic conformance
reference / production differential agreement
determinism
physical representation validation
memory discipline
performance regression tracking
fuzzing
concurrency
fault containment
diagnostic boundedness
cache / reuse equivalence
backend correctness
supported-environment compatibility
reproducible release process
end-to-end example
known limitations
```

No single benchmark, test coverage percentage, or verifier can substitute for these gates.

---

# 28. External SOTA Practices Used as Engineering Reference

These are engineering references, not Kontrakt semantic authorities.

LLVM separates unit tests, focused regression tests, and whole-program test-suite coverage. Compiler transformation bugs
are expected to gain small permanent regression cases.

MLIR exposes pass timing, statistics, and instrumentation because compiler phase cost needs to be observable during
development.

rustc maintains a dedicated compiler-performance benchmark infrastructure, compares compiler revisions, and tracks clean
and incremental-like configurations over time.

LLVM fuzzes compiler components and retains dedicated fuzz targets for parsers and transformations.

JFR provides low-overhead JVM profiling evidence for allocation, GC, thread, lock, and execution behavior.

Kontrakt should adopt these principles without copying their internal architectures.

---

# 29. Final Rule

Kontrakt should not ask only:

> Does the compiler produce the correct answer?

It must also ask:

> Did authority remain with the declared Contract?

> Did the compiler preserve the correct semantic boundary?

> Did the physical realization actually use the intended compact path?

> Did reuse preserve the answer?

> Does the implementation remain bounded under hostile input?

> Can a second path expose a bug in the production path?

> Can performance regressions be detected before release?

V1 commercial quality is reached when these questions have repeatable evidence rather than developer confidence alone.