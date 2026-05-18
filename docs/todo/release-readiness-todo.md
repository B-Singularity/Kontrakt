# Kontrakt Release Readiness TODO

## Status

Living document.

This document tracks work that must be completed or reviewed before a stable Kontrakt release.

It is not an ADR.

It is not a protocol authority.

It is a release-readiness tracking document for deferred work, quality gates, calibration tasks, benchmark evidence, and
documentation cleanup.

## 1. Core compiler-style refactor

Status: In progress.

TODO:

- Complete planning refactor.
- Complete linking refactor.
- Complete VM / execution refactor.
- Complete reporting refactor.
- Remove accidental dependency on JUnit lifecycle from core logic.
- Keep JUnit as an adapter, not as the owner of Kontrakt's verification lifecycle.
- Add architecture tests for package boundaries.

## 2. Contract definition work

Status: Deferred until top-level contract meaning is clarified.

TODO:

- Define what counts as software contract meaning in Kontrakt.
- Separate interface contract, input contract, output contract, state contract, transition contract, protocol contract,
  policy contract, boundary contract, and failure contract.
- Define which contract surfaces are user-facing.
- Define which contract surfaces are lowered into core semantic material.
- Define how frontend syntax such as annotations, DSLs, compiler metadata, or generated indexes lowers into
  Kontrakt-owned contract facts.
- Define effective/default value semantics.
- Define contract fact taxonomy.
- Define contract frontend parity rules.

## 3. Metadata identity and canonical byte protocol

Status: ADR-0041 in progress.

TODO:

- Finish CanonicalEnvelopeHeaderV1.
- Finish common wire type registry.
- Finish unknown-tag compatibility law.
- Finish SCC-local reference encoding.
- Finish version-bundle fingerprint golden vectors.
- Finish shared metadata interner cap relationships.
- Verify that metadata identity caps are not confused with planning budgets.
- Verify that planning consumes metadata identity only through approved provider boundaries.

## 4. Planning cache and interning

Status: Deferred until planning refactor stabilizes.

TODO:

- Define final PlanCacheKey semantic equality material.
- Separate route key from equality key.
- Define planning cache hit/miss equivalence tests.
- Define L2 interner governance tests.
- Verify that cache state cannot change contract meaning.
- Verify that intern id assignment is deterministic within its declared scope.
- Decide which value-slab migration steps belong before release and which belong after v1.

## 5. Resource profile calibration

Status: TODO before stable release.

TODO:

- Define expected project-size envelopes for SMALL, STANDARD, LARGE, and future AUTO.
- Estimate interface count, implementation count, member count, TypeReference count, RawFactRecord count, and intern
  candidate count per profile.
- Define expected local developer-machine and CI-machine envelopes.
- Run corpus measurements against representative Kotlin/JVM projects.
- Run synthetic boundary fixtures.
- Publish resource profile coverage notes.
- Confirm that per-unit identity fuses are not scaled by ordinary resource profiles.
- Confirm that aggregate budgets scale by admitted units, table rows, graph edges, and total bytes.

## 6. Benchmarking

Status: TODO after v1 pipeline stabilizes.

TODO:

- Benchmark metadata acquisition.
- Benchmark canonical byte encoding.
- Benchmark BLAKE3 / HID derivation.
- Benchmark protocol-owned interning.
- Benchmark planning key generation.
- Benchmark planning cache hit/miss paths.
- Benchmark verification planning.
- Benchmark report manifest generation.
- Record allocation rate, GC pressure, p50/p95/p99 latency where meaningful, and memory envelope.

## 7. Profiling

Status: TODO before release candidate.

TODO:

- Profile allocation hotspots.
- Profile canonical byte size distribution.
- Profile intern candidate count distribution.
- Profile frozen table memory.
- Profile planning session memory.
- Profile report size.
- Profile fixture generation cost.
- Profile integration example execution.

## 8. Golden vectors and reproducibility

Status: Required before stable release.

TODO:

- Add golden vectors for canonical encoding.
- Add golden vectors for HID derivation.
- Add golden vectors for version-bundle fingerprinting.
- Add golden vectors for TypeReference identity.
- Add golden vectors for shared metadata interner ordering.
- Add golden vectors for planning key stability.
- Add bit-exact reproducibility tests.
- Verify repeated-run determinism.
- Verify shuffled input order determinism.
- Verify parallel completion order determinism.
- Verify report stability.

## 9. Integration examples

Status: TODO before public release.

TODO:

- Build one realistic Kotlin/JVM example module.
- Show interface contract declarations.
- Show implementation verification.
- Show generated verification work.
- Show report output.
- Show how repetitive unit tests are reduced.
- Keep examples honest and aligned with current feature state.

## 10. Documentation cleanup

Status: Ongoing.

TODO:

- Ensure ADR-0040 and ADR-0041 terminology is aligned.
- Remove references to documents that do not yet exist.
- Move speculative future work into reserved sections or release-readiness TODOs.
- Keep current v1 behavior separate from v2/v3 roadmap.
- Ensure user-facing docs do not overclaim domain-specific contract support before it exists.
- Add known limitations.

## 11. Release gates

A stable release must not be claimed until:

- compiler-style core boundaries are enforced by tests;
- contract lowering boundaries are documented;
- metadata identity golden vectors pass;
- planning cache/interner equivalence tests pass;
- report reproducibility tests pass;
- example module runs;
- resource profile calibration notes exist;
- benchmark/profiling notes exist;
- known limitations are documented;
- license and contribution files are clear;
- CI is reproducible.

## 12. Deferred post-v1 work

Not required for v1 stable foundation:

- KSP full integration.
- Query-based incremental engine.
- Full deterministic compiler-style mocking module.
- Complete JUnit independence.
- Static-analysis-only verification.
- External JSON contract export.
- Domain-specific contract plugins.
- Advanced off-heap / direct-memory identity tables.
- Full value-slab migration.
- Hardware-aware AUTO solver.

## 13. Error Taxonomy, Diagnostics, and Budgeted Reporting

Status: TODO before release candidate.

Goal:

Ensure that failure handling, diagnostics, and reporting remain deterministic, bounded, sanitized, and safe under large
or malformed contract-verification failures.

TODO:

- Define a deterministic Kontrakt failure taxonomy.
- Separate failure kind, failure phase, identity domain, violated invariant, and diagnostic evidence.
- Ensure hot verification paths do not construct diagnostic strings eagerly.
- Avoid string concatenation, exception-message formatting, stack-trace expansion, or object-heavy diagnostic assembly
  on hot paths.
- Route hot-path failures through compact failure codes and structured evidence handles.
- Enforce `maxDiagnosticEvidenceBytes` for every diagnostic report.
- Ensure cyclic, recursive, or massive contract violations cannot trigger OOM while assembling failure evidence.
- Add truncation rules for oversized diagnostic evidence.
- Add sanitization rules for backend/source evidence.
- Ensure every fail-closed path leaves no half-published metadata, intern table, planning state, or report state.
- Ensure failed intermediate states cannot leak into adjacent worker threads, planning lanes, or JUnit adapter output.
- Add golden vectors for:
    - truncated diagnostics;
    - sanitized diagnostics;
    - cyclic failure diagnostics;
    - malformed metadata diagnostics;
    - cap violation diagnostics;
    - collision diagnostics;
    - SCC seal failure diagnostics;
    - report budget exhaustion diagnostics.

Release gate:

- No stable release may claim deterministic reporting until failure diagnostics are structured, budgeted, golden-vector
  covered, and reproducible across repeated runs.

---

## 14. Build Tool Daemon Hygiene and Slab Reclamation

Status: Critical for v1 local developer experience.

Goal:

Ensure that Kontrakt can run repeatedly inside long-lived JVM build processes without leaking large primitive arrays,
interner registries, frozen images, planning state, or adapter-local execution artifacts.

TODO:

- Define a Kontrakt engine teardown protocol.
- Define which resources are request-local, run-local, image-local, planning-run-local, adapter-local, and
  process-global protocol constants.
- Ensure large `ByteArray`, `LongArray`, `IntArray`, primitive slabs, canonical byte buffers, and interner registries
  are not retained after the owning run ends.
- Ensure JUnit adapter completion does not keep Kontrakt core state alive through listener references, static fields,
  lambdas, closures, or report objects.
- Verify that Gradle Daemon and Maven repeated executions do not accumulate stale Kontrakt state.
- Add leak tests that repeat verification in the same JVM process.
- Add a stress scenario such as repeated `gradle test` execution in one daemon process.
- Verify zero-residue semantics for:
    - frozen acquisition state;
    - metadata identity seal state;
    - protocol-owned interner state;
    - planning session state;
    - generated fixture/value state;
    - reporting buffers;
    - adapter-local lifecycle state.
- Prevent cross-session contamination where metadata or identity material from a previous run influences a fresh run.
- Keep immutable global protocol tables allowed only for ratified constants, not user metadata.

Suggested validation command category:

```text
repeat same verification run N times in one JVM process
compare memory retention, identity output, report output, and state visibility
```

Release gate:

- No public beta should claim daemon-safe local developer experience until repeated-run leak profiling exists.

---

## 15. Decoder Robustness and Malformed Binary Fuzzing

Status: Required for security and robustness gates.

Goal:

Ensure that canonical byte decoding fails closed under malformed, corrupted, truncated, oversized, or adversarial binary
input.

TODO:

- Build a malformed-binary fuzzing suite for canonical byte decoding.
- Target:
    - `CanonicalEnvelopeHeaderV1`;
    - field table parsing;
    - tag dispatch;
    - wire type decoding;
    - offset/length validation;
    - SCC-local reference decoding;
    - unknown tag handling;
    - reserved bit handling;
    - canonical byte slice validation.
- Inject malformed inputs:
    - invalid `magic32`;
    - invalid `headerSize16`;
    - non-zero reserved fields;
    - non-zero unratified `headerFlags16`;
    - invalid payload offset;
    - overflowing offset + length;
    - field table overlapping the header;
    - payload slice pointing outside the envelope;
    - out-of-order field tags;
    - duplicate critical fields;
    - invalid wire type;
    - `WIRE_TYPE_RESERVED`;
    - `WIRE_TYPE_SCC_LOCAL_REF` outside SCC seal boundary;
    - trailing garbage bytes;
    - truncated payload;
    - overlong length prefix;
    - malformed UTF-8;
    - unpaired surrogate after defensive decode.
- Verify that every malformed input fails closed.
- Verify that malformed input never causes:
    - array out-of-bounds access;
    - integer wraparound;
    - infinite decode loop;
    - unbounded allocation;
    - unbounded diagnostic expansion;
    - partial semantic publication;
    - process-wide crash as the ordinary failure path.
- Verify unknown tag behavior:
    - unknown tags fail closed by default;
    - unknown tags may be skipped only when the active compatibility matrix explicitly classifies the tag as skippable
      and non-critical;
    - ratified skip must preserve parse alignment and canonical identity meaning;
    - ratified skip must be golden-vector covered.

Release gate:

- No stable canonical byte protocol release should be accepted without malformed-binary fuzzing and fail-closed decoder
  tests.

---

## 16. ClassLoader Isolation and Runtime Coherence Verification

Status: TODO before public beta.

Goal:

Ensure that Kontrakt does not accidentally collapse different runtime type universes into the same identity or cache
state when JVM ClassLoaders are involved.

TODO:

- Define Kontrakt behavior for the same fully qualified class name loaded through different ClassLoaders.
- Cover scenarios such as:
    - Gradle test workers;
    - Spring Boot DevTools reloads;
    - plugin systems;
    - nested ClassLoaders;
    - dynamic module environments;
    - test isolation runners.
- Decide which ClassLoader facts are:
    - semantic contract material;
    - runtime binding material;
    - diagnostic-only material;
    - adapter-local evidence;
    - forbidden identity material.
- Ensure `TypeReference` identity does not blindly collapse unrelated runtime types with the same qualified name when
  runtime binding semantics require separation.
- Ensure ClassLoader provenance does not leak into canonical metadata identity unless explicitly ratified by the
  relevant runtime-binding or implementation-selection domain.
- Verify that `RuntimeBindingSnapshot` handles ClassLoader-sensitive implementation selection deterministically.
- Add tests proving that raw JVM `Class`, `KClass`, reflection handles, or backend handles do not escape into:
    - long-lived global registries;
    - shared metadata interners;
    - frozen image identity material;
    - planning cache keys;
    - public DTOs;
    - persistent artifacts.
- Add negative tests for class-name aliasing across ClassLoaders.
- Add diagnostics that explain when two visually identical type names are rejected because their runtime binding
  contexts are not equivalent.

Release gate:

- Public beta should not claim safe runtime verification in ordinary JVM build environments until ClassLoader isolation
  behavior is documented and tested.

---

## 17. Multi-threaded Lane Isolation and State Barrier Validation

Status: TODO before stable release.

Goal:

Ensure that parallel verification, fixture generation, metadata staging, identity sealing, and reporting do not publish
semantic state before deterministic verification barriers have completed.

TODO:

- Define thread/lane ownership rules for:
    - acquisition staging;
    - canonical byte preparation;
    - HID derivation;
    - interner candidate staging;
    - planning cache lookup;
    - fixture generation;
    - VM/execution;
    - reporting.
- Ensure lane-local primitive staging arenas cannot cross-contaminate.
- Ensure worker-local or lane-local physical order never becomes semantic identity order.
- Add deterministic merge rules for lane-local candidates.
- Add state-barrier assertions for:
    - before frozen image publication;
    - before stable intern id publication;
    - before planning-visible provider publication;
    - before PlanCacheKey reuse;
    - before report manifest publication.
- Add `StateBarrierValidator` or equivalent test utility to detect premature semantic publication.
- Verify that speculative physical acceleration may prepare data but cannot publish semantic identity.
- Verify memory visibility and safe publication boundaries for immutable tables, reports, and provider bundles.
- Add tests for:
    - shuffled lane completion order;
    - delayed worker completion;
    - cancellation during identity seal;
    - failure during deterministic merge;
    - collision during parallel candidate staging;
    - report generation after partial execution failure.
- Ensure final output remains identical under different thread counts where the contract and resolved policy are
  identical.

Release gate:

- No stable release should claim deterministic parallel execution until lane isolation, deterministic merge, and
  publication barriers are tested.

---

## 18. Resource Profile Calibration and Release Evidence

Status: TODO before stable release.

Goal:

Calibrate `SMALL`, `STANDARD`, `LARGE`, and future `AUTO` against real project sizes and hardware envelopes.

TODO:

- Define expected project-size envelopes for each resource intent.
- Estimate:
    - interface count;
    - implementation count;
    - method/property/constructor count;
    - TypeReference count;
    - RawFactRecord count;
    - ActiveMemberKey count;
    - LocalSelectorTuple count;
    - RuntimeBindingSnapshot count;
    - active metadata intern candidate count;
    - frozen table bytes;
    - diagnostic evidence bytes.
- Measure representative Kotlin/JVM projects.
- Measure synthetic worst-case fixtures.
- Record local developer-machine and CI-machine envelopes.
- Publish profile coverage notes before stable release.
- Confirm that ordinary resource profiles scale aggregate budgets, not per-unit identity fuses.
- Confirm that `AUTO` v1 remains deterministic and maps to the bootstrap `STANDARD` policy.
- Defer hardware-aware `AUTO` to a later deterministic pre-admission solver.

Release gate:

- Stable release documentation must not claim calibrated profile coverage until benchmark and corpus evidence exists.

---

## 19. Supply Chain, Dependency, and Reproducible Build Hygiene

Status: TODO before public release.

Goal:

Ensure that Kontrakt's verification engine is not undermined by unstable dependencies, unreproducible builds, or unclear
release artifacts.

TODO:

- Pin dependency versions.
- Document dependency update policy.
- Ensure build outputs are reproducible where practical.
- Add CI checks for:
    - dependency lock consistency;
    - license compatibility;
    - generated artifact stability;
    - test reproducibility.
- Verify that canonical identity golden vectors are stable across clean checkouts.
- Ensure release artifacts include:
    - license;
    - source;
    - changelog;
    - known limitations;
    - compatibility notes;
    - reproducibility notes.

Release gate:

- No stable release should be published without dependency and license review.

---

## 20. Deferred Post-v1 Work

Status: Deferred after v1 stable foundation.

The following work is intentionally not required for the v1 stable foundation.

Deferred work:

- KSP full integration.
- Query-based incremental engine.
- Full deterministic compiler-style mocking module.
- Complete JUnit independence.
- Static-analysis-only verification.
- External JSON contract export.
- Domain-specific contract plugins.
- Advanced off-heap / direct-memory identity tables.
- Full value-slab migration.
- Hardware-aware `AUTO` solver.

Rule:

- Deferred work must not be pulled into the v1 release gate unless it becomes necessary to preserve v1 correctness,
  determinism, or safe publication.